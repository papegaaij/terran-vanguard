#!/usr/bin/env python3
"""Production art: the Level 25 backdrop, Siren Trench (design/campaign/act-4-deep-water/level-25-siren-trench;
M8 part E batch, concept round 54, step ART-E).

Straight to production from the round-48 mood still `europa-siren-trench-r48-a` (tools/concept/europa_r48.py
``trench_still``: ice-rock strata walls with purple Vrell growth, the foot the collision line, a cave notch, dark
silt between, the beacons' red-gold pulse, ~14 % blue ambient), imported unchanged for its colours (PLAIN); the
structure as backdrop_l24.py (exact rows, the scroll helpers, its review grade via l24kit), the wall textures in
backdrop_l23.py's ice-shaft convention (so the game's ``ice-wall`` path of WallProjection draws them, smooth walls,
no side canyons; nothing wet: Level 25 has no waterline), the island's steps from backdrop_l16.py's butte model
(look ``pillar``: CanyonProfile's pillar setbacks x 0.36, seed 750), the beacons from tools/art/l24_props.py (D's
relay beacon, unchanged, at a 4 s loop). Posterized per image, wide gradients ordered-dithered.

**The terrain this art is drawn for.** The level's ``terrain`` (walls, the pillar island, the cave notches) is the
data's when it exists; until then this script's proposal (``proposal_terrain``, written into the proposal file as a
``terrain:`` block for step E3): the trench mouth (the walls rising at the field's edges from t=5, 400 px wide by
t=12), 320 px by t=20, 260 px from t=34 (sections 2-3), the fork at 330 px (t=96.6-106.4) round the pillar island
(x 215-265, t=98-106: two 140 px channels), the narrows at 190 px (t=108.2-117.8), 260 px again from t=119.6 to 160,
the walls sinking and leaving the field by t=169 (section 6). Rows are terrain rows (the ground scroll at which the row
reaches the top edge). **Notches** (gap check §1.1: ``terrain.notches: [{side, scroll, width, depth}]``, ``scroll``
the terrain row of the notch's middle, 90 wide along the wall, 70 deep): one for each cave wave of the README, its
middle 150 px below the top edge at the wave's t (on the screen when it spawns), the survey sub's in the left wall at
t~78, and fillers so each wall has a notch about every 1,400 px (none where the wall stands closer than 76 px to the
field's edge, none on a bending stretch, none within 140 px of a ledge or the pillar's rows).

**The grade is the game's** (``underwater_look``; proposal: ambient 0.14 throughout, a bit darker over the floor of
section 6). The tiles carry the unlit floor; the walls' and pillar's textures are unlit albedo (WallProjection shades
each step by its normal, key light from the upper left); ``ice-wall-glow`` (additive, ungraded) holds the Vrell
growth's sparse teal nodules, so the walls' line reads in the dark; the beacons' pulse is ``relay-beacon-glow``.

Wall textures (CanyonProfile's convention, backdrop_l23.py's names; not pieces):
  ice-wall        128x1280  the trench's ice-rock: segment 0 the dark wet foot cliff, 1 a narrow silted ledge, 2 the
                            main strata face with Vrell growth, 3 a ledge, 4 the upper face, 5 the pale frosted lip
  ice-wall-glow   128x1280  additive: the growth's teal nodules (a few per period) and short vein glints
  ice-wall-foot   24x512    the foot band (column 0 the collision line: a dark crack, the lit rock edge, rubble and
                            silt thinning out by column 23; lit from straight up only: mirrored for the right wall)
  pillar-face     128x528   the island's steps (rows along its perimeter, period 512, 16 rows repeated)
  pillar-top_0..1           the island's cap at 1.25 on the canvas of its foot polygon x 1.25, far end first
Tile sets (480x960, wrapping, ground):
  trench-floor    sections 1-3: dark silt between rubble fans shed by the walls, stones, faint current lines
  trench-scour    section 4 (the fork and the narrows): current-scoured rock, ripples, boulders
  trench-song     section 5 (the Song): the silt grown over with Vrell root mats spreading from the walls
  abyss-floor     section 6: the floor opening into flat black silt, almost bare
Pieces (ground; exact rows):
  trench-lip      480 wide at t=0: Level 24's plain at the bottom edge, its ragged lip, the slope down into the
                  trench floor (continuing Level 24's last piece, trench-mouth)
  seam-trench-scour, seam-trench-song, seam-abyss-floor   480x320 on the section seams
  rock-ledge-left, rock-ledge-right   64x56: a rock ledge at a wall's (or the pillar's) foot under each Reef Spitter
                  (the mount at the piece's centre; -left juts from rock on its left)
  relay-beacon    128x192, 16 frames at 4 fps (the 4 s pulse, D's piece), its foot on the floor 26 px off a wall's
                  foot line; relay-beacon-glow its additive light (not placed: the game adds it over the grade, D4)
  cave-mouth-left, cave-mouth-right   112x124, NOT placed (the walls are drawn after the ground pieces): the game draws
                  one over the projected wall at each notch (E5): the collision opening's middle row at the image's
                  row 62, the foot line at column 84 (left) / 27 (right); the recess (its floor darkening into the
                  overhang, the back wall, the side walls lit or shaded by the key light), the rock round it dithering
                  out into the wall, the rubble spilling over the foot band
Review: design/campaign/act-4-deep-water/level-25-siren-trench/concept/backdrop-final-r54-a.png/.gif (the walls and the
pillar projected as WallProjection does, approximately; stand-ins for the units); `--proposal` writes the `backdrop:`
and the proposed `terrain:` block to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections, speeds, ramps, terrain incl. notches and islands, ground
targets), otherwise the README's values and the proposal. Re-run after the data moves a section, a wall or a target.
Run: python3 tools/art/backdrop_l25.py [--proposal | --review | --check]   (all: about 3 min)
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

import backdrop_l16 as b16  # noqa: E402  (tools/art: the butte model, finish, padded, dither_alpha)
import backdrop_l22 as b22  # noqa: E402  (tools/art: posterize)
import europa_r48 as eu  # noqa: E402  (concept script, unchanged: Europa's colours, PLAIN)

SCRIPT = "backdrop_l25.py"
BATCH = "M8 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r54"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-25-siren-trench"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-25"
L24 = ROOT / "assets" / "backdrop" / "level-24"
W, SCREEN = 480, 540
TILE_H = 960
FEATHER = 32
SHIP_Y = 96
OUTRO = 15
FACTORS = {"ground": 1.0, "low-air": 1.35, "high-air": 2.2}
MAX_PIECE = 2040
CX, CY = 240.0, 297.0                       # the projection centre (BackdropData.Tower.CENTRE_X/Y, px from the top)
BEACON_FPS = 4                              # D's 16-frame pulse at a 4 s loop (gap check E11)
TEAL = np.array((0, 255, 154), float)
ROCK = np.array((96, 124, 140), float)      # europa_r48 trench_albedo: the ice-rock, the Vrell growth
GROWTH = np.array((120, 70, 128), float)
NOTCH_W, NOTCH_D = 90, 70
CAVE = (112, 124)
CAVE_FOOT = {0: 84, 1: 27}                  # the foot line's column in cave-mouth-left / -right
CAVE_MID = 62

# --------------------------------------------------------------------------- the level's data (or the README)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
FALLBACK_SECTIONS = [{"end": 22, "speed": 140}, {"end": 60, "speed": 140, "ramp": 2},
                     {"end": 95, "speed": 140, "ramp": 2}, {"end": 130, "speed": 120, "ramp": 2},
                     {"end": 160, "speed": 130, "ramp": 2}, {"end": 173, "speed": 140, "ramp": 2}]
SECTIONS = DATA["sections"] if DATA else FALLBACK_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 140)) if DATA else 140.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
SECTION_TILES = ["trench-floor", "trench-floor", "trench-floor", "trench-scour", "trench-song", "abyss-floor"]
LOOK = "{ambient: 0.14, tint: a0c8e8, caustics: 0.0, snow: 100, headlights: 0.9}"
LOOK6 = "{ambient: 0.1, tint: 98c0e0, caustics: 0.0, snow: 80, headlights: 0.9}"


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


# --------------------------------------------------------------------------- the terrain (data, or the proposal)

def _f(t):
    return int(round(scroll(t) / 10.0) * 10)


CAVE_WAVES = [("left", 29), ("right", 39), ("left", 49), ("right", 58.5), ("left", 72), ("right", 86),
              ("left", 101), ("right", 117.5), ("left", 133.5), ("right", 149.5)]
SUB_T = 78
SUB_H = 40
DEFAULT_SPITTERS = [(30, "left"), (30.7, "left"), (73, "right"), (73.7, "right"), (100, "pillar-left"),
                    (100.6, "pillar-right"), (140, "right"), (140.7, "right"), (141.4, "right")]


def proposal_walls():
    left = [(_f(5), 0, 0.0), (_f(9), 40, 0.7), (_f(12), 40), (_f(20), 80), (_f(26), 80), (_f(34), 110),
            (_f(95), 110), (_f(96.6), 75), (_f(106.4), 75), (_f(108.2), 145), (_f(117.8), 145), (_f(119.6), 110),
            (_f(160), 110), (_f(164), 40, 0.6), (_f(169), 0, 0.0)]
    right = [(p[0], W - p[1]) + tuple(p[2:]) for p in left]
    return left, right


def proposal_pillar():
    r0, r1 = _f(98), _f(106)
    return [(240, r0), (262, r0 + 70), (265, r0 + 180), (265, r1 - 180), (262, r1 - 70), (240, r1),
            (218, r1 - 70), (215, r1 - 180), (215, r0 + 180), (218, r0 + 70)]


def _wall_x(pts, rows):
    s = np.array([p[0] for p in pts], float)
    x = np.array([p[1] for p in pts], float)
    out = np.interp(rows, s, x)
    return np.where((np.asarray(rows) < s[0]) | (np.asarray(rows) > s[-1]), np.nan, out)


def _wall_h(pts, rows):
    s = np.array([p[0] for p in pts], float)
    h = np.array([float(p[2]) if len(p) > 2 else 1.0 for p in pts])
    return np.interp(rows, s, h, left=0.0, right=0.0)


def _bends(pts):
    return [p[0] for p in pts]


def proposal_notches(left, right, ledges, pillar):
    """The cave waves' notches (fixed), the survey sub's, then fillers about every 1,400 px per wall."""
    walls = {"left": left, "right": right}
    fixed = [(side, _f(t) - 150, f"the t={t:g} cave wave ({side})") for side, t in CAVE_WAVES]
    fixed.append(("left", int(round(scroll(SUB_T) + SUB_H / 2)), f"the survey sub (t~{SUB_T})"))
    p_rows = (min(p[1] for p in pillar) - 140, max(p[1] for p in pillar) + 140)

    def ok(side, row, strict):
        pts = walls[side]
        lo, hi = row - NOTCH_W / 2 - 60, row + NOTCH_W / 2 + 60
        rows = np.linspace(lo, hi, 13)
        xs = _wall_x(pts, rows)
        if np.isnan(xs).any():
            return False
        room = xs if side == "left" else W - xs
        if room.min() < 72 or np.ptp(xs) > 12:
            return False
        if any(lo < b < hi for b in _bends(pts)) and strict:
            return False
        if strict and p_rows[0] < row < p_rows[1]:
            return False
        if strict and any(s == side and abs(r - row) < 140 + NOTCH_W / 2 for s, r in ledges):
            return False
        if strict and any(n["side"] != side and abs(n["scroll"] - row) < 300 for n in notches):
            return False
        return True

    notches = []
    for side, row, why in fixed:
        if not ok(side, row, False):
            print(f"warning: notch for {why} at row {row} does not fit its wall")
        notches.append({"side": side, "scroll": int(row), "why": why})
    first, last = _f(22), _f(158)
    for side in ("left", "right"):
        rows = sorted(n["scroll"] for n in notches if n["side"] == side)
        bounds = [first - 700] + rows + [last + 700]
        for a, b in zip(bounds, bounds[1:]):
            gap = b - a
            k = int(round(gap / 1400.0)) - 1
            for j in range(1, k + 1):
                r = int(round((a + gap * j / (k + 1)) / 10) * 10)
                if first <= r <= last:
                    for d in (0, 80, -80, 160, -160, 240, -240, 320, -320):
                        if ok(side, r + d, True):
                            notches.append({"side": side, "scroll": r + d, "why": "filler"})
                            break
    notches.sort(key=lambda n: n["scroll"])
    return notches


def _ats(g):
    a = g.get("at")
    if a is None:
        return []
    return a if isinstance(a, list) and a and isinstance(a[0], list) else [a]


def _terrain():
    if DATA and DATA.get("terrain"):
        tr = DATA["terrain"]
        left = [tuple(p) for p in tr["left"]]
        right = [tuple(p) for p in tr["right"]]
        isl = tr.get("islands") or []
        pillar = [tuple(p) for p in isl[0]["points"]] if isl else proposal_pillar()
        notches = [{"side": n["side"], "scroll": int(n["scroll"]), "why": "data",
                    "width": n.get("width", NOTCH_W), "depth": n.get("depth", NOTCH_D)}
                   for n in tr.get("notches") or []]
        return left, right, pillar, notches, True
    left, right = proposal_walls()
    return left, right, proposal_pillar(), None, False


LEFT, RIGHT, PILLAR, _NOTCHES, FROM_DATA = _terrain()
b16.ISLANDS["pillar"] = [tuple(p[:2]) for p in PILLAR]   # this process only: backdrop_l16's butte model on our foot
PILLAR_ROWS = (min(p[1] for p in PILLAR), max(p[1] for p in PILLAR))


def foot(side, trow):
    """The wall's collision line x at terrain rows (side 0 left, 1 right; NaN: no wall)."""
    return _wall_x(LEFT if side == 0 else RIGHT, trow)


def wall_height(side, trow):
    return _wall_h(LEFT if side == 0 else RIGHT, trow)


def pillar_span(trow):
    """The pillar's foot x range at terrain row ``trow`` (None outside it); the polygon is convex."""
    if not PILLAR_ROWS[0] <= trow <= PILLAR_ROWS[1]:
        return None
    xs = []
    n = len(PILLAR)
    for i in range(n):
        (x0, s0), (x1, s1) = PILLAR[i][:2], PILLAR[(i + 1) % n][:2]
        if (s0 - trow) * (s1 - trow) <= 0 and s0 != s1:
            xs.append(x0 + (x1 - x0) * (trow - s0) / (s1 - s0))
    return (min(xs), max(xs)) if xs else None


def _targets():
    """(kind, t, x) of the ground targets: the data's, else the README's (this proposal's positions)."""
    if DATA:
        out = []
        for g in DATA.get("ground_targets") or []:
            kind = g.get("enemy") or ("survey-sub" if g.get("trigger") or g.get("reveals") else g.get("sprite", "?"))
            for at in _ats(g):
                out.append((str(kind), float(at[0]), float(at[1])))
        return out
    out = []
    for t, where in DEFAULT_SPITTERS:
        row = scroll(t) + 28
        if where == "left":
            x = float(foot(0, row)) + 18
        elif where == "right":
            x = float(foot(1, row)) - 18
        else:
            span = pillar_span(row)
            x = span[0] - 12 if where == "pillar-left" else span[1] + 12
        out.append(("reef-spitter", float(t), round(x)))
    sub_row = scroll(SUB_T) + SUB_H / 2
    out.append(("survey-sub", float(SUB_T), round(float(foot(0, sub_row)) - 32)))
    return out


TARGETS = _targets()


def ledge_side(x, trow):
    """'left' when the target's rock is on its left (a left wall or the pillar's right side), else 'right'."""
    xl, xr = foot(0, trow), foot(1, trow)
    span = pillar_span(trow)
    cands = []
    if not np.isnan(xl):
        cands.append((abs(x - xl), "left"))
    if not np.isnan(xr):
        cands.append((abs(x - xr), "right"))
    if span:
        cands += [(abs(x - span[0]), "right"), (abs(x - span[1]), "left")]
    return min(cands)[1] if cands else "left"


LEDGES = [(ledge_side(x, scroll(t) + 28), t, x) for k, t, x in TARGETS if k == "reef-spitter"]
NOTCHES = _NOTCHES if _NOTCHES is not None else proposal_notches(
    LEFT, RIGHT, [(s, scroll(t) + 28) for s, t, x in LEDGES if s in ("left", "right")], PILLAR)


def _beacons():
    """(t, side) of the relay beacons: three on the left wall in section 1, then every 900 px alternating walls
    through section 5 (none near a notch, a ledge, the fork or the narrows)."""
    out = [(12.5, 0), (16.0, 0), (19.5, 0)]
    side, row = 1, scroll(24)
    while row < scroll(157):
        t = time_at(row)
        good = not (95 <= t <= 121)
        good &= all(not (n["side"] == ("left", "right")[side] and abs(n["scroll"] - row) < 150) for n in NOTCHES)
        x = float(foot(side, row)) + (26 if side == 0 else -26)
        good &= all(math.hypot(x - tx, (scroll(tt) + 28) - row) > 110 for k, tt, tx in TARGETS)
        if good:
            out.append((round(t, 2), side))
            side = 1 - side
        row += 900 if good else 150
    return out


BEACONS = _beacons()

# --------------------------------------------------------------------------- helpers

pn, ramp, finish, padded, dither_alpha = b16.pn, b16.ramp, b16.finish, b16.padded, b16.dither_alpha
posterize = b22.posterize
TEX_W, TEX_PERIOD, TEX_PAD = b16.TEX_W, b16.TEX_PERIOD, b16.TEX_PAD
WIDTHS = [s[4] for s in b16.SEGMENTS]                    # 24, 20, 32, 24, 16, 12
FACE = (True, False, True, False, True, False)
FACE_PAD = b16.FACE_PAD
PILLAR_PERIOD = 512


def grid(h=TILE_H, w=W):
    return np.mgrid[0:h, 0:w].astype(np.float64)


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(t.shape[0] - 1 - np.mod(rows, t.shape[0])).astype(int)]


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


# --------------------------------------------------------------------------- tile sets

PLAIN = [tuple(int(c) for c in s) for s in eu.PLAIN]


def trench_tile(seed, gain, rubble=60, boulders=14, ripples=0.0, roots=0, current=0.0, stones=50, relief=0.3):
    """The trench floor: dark silt dunes and current lines, rubble fans, boulders and stones (wrapping), Vrell root
    mats (purple-brown, a few teal-white buds) for the Song."""
    yy, xx = grid()
    n = pn(W, TILE_H, 96, seed, 5)
    rd = 1 - np.abs(2 * pn(W, TILE_H, 48, seed + 1, 2) - 1)
    hgt = 0.42 + relief * n + 0.12 * rd ** 4
    if ripples:
        hgt += ripples * np.sin(TAU * (yy + 0.2 * xx + 40 * pn(W, TILE_H, 96, seed + 2, 2)) * 64 / TILE_H)
    if current:
        hgt += current * (pn(W, TILE_H, 16, seed + 3, 2) - 0.5) * (0.5 + 0.5 * np.sin(TAU * xx / 60 + 3 * n))
    stops = [tuple(min(255, int(c * gain)) for c in s) for s in PLAIN[:5]]
    rgb = eu.m36.shade_rgb(hgt, stops, strength=16, shadow=0.004, periodic=True)
    rgb *= (0.9 + 0.2 * pn(W, TILE_H, 8, seed + 5, 2))[..., None]
    img = posterize(rgb, 32)
    lay = Image.new("RGBA", (W, TILE_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rng = np.random.default_rng(seed + 9)

    def wrap(fn):
        for dy in (-TILE_H, 0, TILE_H):
            fn(dy)

    for _ in range(roots):                                         # Vrell root mats: branching purple-brown
        x, y, a = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(0, TAU)
        for _ in range(int(rng.integers(2, 5))):
            pts = [(x, y)]
            aa = a + rng.normal(0, 0.9)
            for _ in range(int(rng.integers(6, 14))):
                aa += rng.normal(0, 0.45)
                pts.append((pts[-1][0] + math.cos(aa) * 7, pts[-1][1] + math.sin(aa) * 7))
            col = (78, 40, 80, 255) if rng.random() < 0.6 else (62, 34, 58, 255)
            wrap(lambda dy, pts=pts, col=col: d.line([(px, py + dy) for px, py in pts], fill=col, width=2))
            if rng.random() < 0.35:
                ex, ey = pts[-1]
                wrap(lambda dy, ex=ex, ey=ey: (d.ellipse([ex - 2, ey + dy - 2, ex + 2, ey + dy + 2],
                                                         fill=(96, 56, 100, 255)),
                                               d.point([(ex, ey + dy)], fill=(150, 214, 196, 255))))
    for _ in range(rubble):                                        # rubble fans: clusters of angular chips
        cx, cy = rng.uniform(0, W), rng.uniform(0, TILE_H)
        for _ in range(int(rng.integers(4, 10))):
            px, py = cx + rng.normal(0, 9), cy + rng.normal(0, 6)
            s, a = rng.uniform(1.2, 3.2), rng.uniform(0, TAU)
            pts = [(px + s * math.cos(a + k * TAU / 5) * rng.uniform(0.7, 1.3),
                    py + s * math.sin(a + k * TAU / 5) * rng.uniform(0.7, 1.3)) for k in range(5)]
            c = tuple(int(v * rng.uniform(0.8, 1.15)) for v in (70, 86, 92)) + (255,)
            wrap(lambda dy, pts=pts, c=c: d.polygon([(qx, qy + dy) for qx, qy in pts], fill=c,
                                                    outline=(20, 28, 32, 255)))
    for _ in range(boulders):                                      # boulders: lit upper left, a shadow lower right
        cx, cy, r = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(4, 10)
        wrap(lambda dy, cx=cx, cy=cy, r=r: (
            d.ellipse([cx - r + 2, cy + dy - r * 0.8 + 3, cx + r + 3, cy + dy + r * 0.8 + 3], fill=(10, 16, 20, 200)),
            d.ellipse([cx - r, cy + dy - r * 0.8, cx + r, cy + dy + r * 0.8], fill=(64, 80, 86, 255),
                      outline=(24, 32, 36, 255)),
            d.ellipse([cx - r * 0.6, cy + dy - r * 0.6, cx + r * 0.1, cy + dy - r * 0.05], fill=(92, 110, 112, 255))))
    for _ in range(stones):
        cx, cy, r = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(0.8, 1.8)
        wrap(lambda dy, cx=cx, cy=cy, r=r: d.ellipse([cx - r, cy + dy - r, cx + r, cy + dy + r],
                                                     fill=(96, 110, 104, 255)))
    img.alpha_composite(lay)
    a = np.array(img)
    a[..., 3] = 255
    return Image.fromarray(a, "RGBA")


TILES = {"trench-floor": lambda: trench_tile(2501, 0.78, rubble=70, boulders=16, current=0.05),
         "trench-scour": lambda: trench_tile(2511, 0.82, rubble=40, boulders=26, ripples=0.03, current=0.12,
                                             relief=0.36),
         "trench-song": lambda: trench_tile(2521, 0.74, rubble=50, boulders=12, roots=70, current=0.04),
         "abyss-floor": lambda: trench_tile(2531, 0.6, rubble=8, boulders=3, stones=24, relief=0.18)}

# --------------------------------------------------------------------------- the walls (CanyonProfile convention)

ROCK_STEPS = (((36, 46, 56), (58, 72, 84)),             # the dark wet foot cliff
              ((70, 82, 88), (86, 98, 102)),           # a narrow silted ledge
              ((80, 104, 120), (120, 146, 162)),       # the main strata face
              ((92, 106, 110), (108, 122, 124)),       # a ledge
              ((104, 128, 146), (140, 162, 178)),      # the upper face
              ((150, 170, 186), (190, 204, 214)))      # the pale frosted lip


def _wall_fields(th=TEX_PERIOD, tw=TEX_W):
    g_of = np.repeat(np.arange(6), WIDTHS)
    start = np.concatenate([[0], np.cumsum(WIDTHS)])[:-1]
    tcol = (np.arange(tw) - start[g_of] + 0.5) / np.array(WIDTHS)[g_of]
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    return yy, xx, np.broadcast_to(g_of, (th, tw)), np.broadcast_to(tcol, (th, tw))


def _worley(th, tw, cw, ch, seed):
    """Jittered-cell distances (F1, F2) and the cell id on a th x tw texture, periodic in rows (ch divides th)."""
    rng = np.random.default_rng(seed)
    ny, nx = th // ch, tw // cw + 2
    jy, jx = rng.uniform(0.15, 0.85, (ny, nx)), rng.uniform(0.15, 0.85, (ny, nx))
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    cy, cx = np.floor(yy / ch).astype(int), np.floor(xx / cw).astype(int)
    f1 = np.full((th, tw), 1e9)
    f2 = np.full((th, tw), 1e9)
    ids = np.zeros((th, tw), np.int64)
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            gy, gx = cy + dy, np.clip(cx + dx, 0, nx - 1)
            py = (gy + jy[gy % ny, gx]) * ch
            px = (gx + jx[gy % ny, gx]) * cw
            d = np.hypot((xx - px) / cw, (yy - py) / ch)
            closer = d < f1
            f2 = np.where(closer, f1, np.minimum(f2, d))
            ids = np.where(closer, (gy % ny) * nx + gx, ids)
            f1 = np.minimum(f1, d)
    return f1, f2, ids


def _growth(th, seed, tw=TEX_W):
    """The Vrell growth's coverage (0..1) and its nodules (centres) on a wall texture: blotchy patches climbing from
    the foot, long bare stretches between them along the wall, a few curtains reaching the upper face."""
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    big = pn(tw, th, 64, seed, 3)
    patch = pn(tw, th, 16, seed + 1, 3)
    along = 0.5 + 0.5 * np.sin(TAU * (yy / th) * 3 + TAU * pn(tw, th, 64, seed + 3, 2))
    climb = np.clip(1.1 - xx / (50 + 50 * big), 0, 1)
    cover = np.clip((0.55 * big + 0.45 * patch + 0.25 * along - 0.78) * 6, 0, 1) * climb
    rng = np.random.default_rng(seed + 2)
    nods = []
    for _ in range(th // 40):
        x, y = rng.uniform(2, 64), rng.uniform(0, th)
        if cover[int(y) % th, int(x)] > 0.6:
            nods.append((x, y, rng.uniform(0.9, 1.7)))
    return cover, nods


def rock_wall_rgb(th=TEX_PERIOD, seed=2541, tw=TEX_W):
    """Fractured ice-rock: blocks (jittered cells, longer along the wall) with dark cracks and a lit upper-left bevel,
    each block its own shade, faint strata within; silted ledges; the frosted lip; Vrell growth over it."""
    yy, xx, G, T = _wall_fields(th, tw)
    lo = np.array([c[0] for c in ROCK_STEPS], float)[G]
    hi = np.array([c[1] for c in ROCK_STEPS], float)[G]
    rgb = lo + (hi - lo) * (0.35 + 0.3 * T)[..., None]
    fine = pn(tw, th, 8, seed, 3)
    mid = pn(tw, th, 32, seed + 1, 3)
    face = np.isin(G, [g for g in range(6) if FACE[g]])
    f1, f2, ids = _worley(th, tw, 9, 32, seed + 4)
    rnd = np.random.default_rng(seed + 5).uniform(0.72, 1.22, ids.max() + 1)[ids]
    crack = np.clip(1 - (f2 - f1) / 0.12, 0, 1)
    gy, gx = np.gradient(f1)
    bevel = np.clip(-(gx * 3 + gy * 1.2) * 8, -1, 1) * (f1 > 0.25)
    blk = rnd * (1 + 0.12 * bevel) * (0.9 + 0.2 * mid) * (0.94 + 0.12 * fine)
    rgb = np.where(face[..., None], rgb * blk[..., None] * (1 - 0.6 * crack)[..., None], rgb)
    wav = 3 * (pn(tw, th, 64, seed + 2, 2) - 0.5)
    strata = face & (np.abs(((xx + wav) % 13) - 6.5) < 0.5) & (mid > 0.55) & (crack < 0.3)
    rgb = np.where(strata[..., None], rgb * 1.12 + 6, rgb)
    # ledges: silt (grey-green) with rubble specks, the front lip lit, the toe under the face above dark
    silt = np.array((54, 64, 62.0)) * (0.85 + 0.3 * fine)[..., None]
    rgb = np.where((~face)[..., None], rgb * 0.45 + silt * 0.55, rgb)
    specks = (~face) & (pn(tw, th, 4, seed + 6, 2) > 0.68)
    rgb = np.where(specks[..., None], np.array((104, 118, 120.0)), rgb)
    rgb = np.where((~face & (T < 0.15))[..., None], rgb * 1.15 + 6, rgb)
    rgb = np.where((~face & (T > 0.82))[..., None], rgb * 0.75, rgb)
    rgb = np.where((face & (T < 0.06))[..., None], rgb * 0.75, rgb)
    rim = (G == 5) & (T > 0.8)
    rgb = np.where(rim[..., None], np.array((196, 208, 218.0)) * (0.92 + 0.1 * fine)[..., None], rgb)
    frost = (G >= 4) & (pn(tw, th, 8, seed + 8, 2) > 0.66)
    rgb = np.where(frost[..., None], rgb * 0.6 + np.array((200, 214, 224.0)) * 0.4, rgb)
    cover, nods = _growth(th, seed + 7, tw)
    vein = 1 - np.abs(2 * pn(tw, th, 8, seed + 9, 2) - 1)
    gcol = GROWTH * (0.75 + 0.35 * mid)[..., None] * (0.8 + 0.3 * vein)[..., None]
    rgb = np.where((cover >= 0.55)[..., None], gcol, rgb)
    rim_g = (cover > 0.3) & (cover < 0.55)
    rgb = np.where(rim_g[..., None], rgb * 0.55 + gcol * 0.3, rgb)
    for x, y, r in nods:                                    # the nodules: bone-rimmed, a dark pit (the glow is apart)
        for dy in (-th, 0, th):
            d = np.hypot(xx - x, yy - y - dy)
            rgb = np.where((d < r + 1.2)[..., None], np.array((190, 176, 160.0)), rgb)
            rgb = np.where((d < r)[..., None], np.array((40, 90, 80.0)), rgb)
    return np.clip(rgb, 0, 255), nods


def ice_wall():
    rgb, _ = rock_wall_rgb()
    return finish(padded(rgb), 32, amount=0.04)


def ice_wall_glow():
    yy, xx, G, T = _wall_fields()
    _, nods = rock_wall_rgb()
    f = np.zeros((TEX_PERIOD, TEX_W))
    for x, y, r in nods:
        for dy in (-TEX_PERIOD, 0, TEX_PERIOD):
            d = np.hypot(xx - x, yy - y - dy)
            f = np.maximum(f, np.clip(1 - d / r, 0, 1) + 0.35 * np.clip(1 - d / (r + 2.5), 0, 1))
    v = artkit.ordered_dither(np.clip(f, 0, 1), 4)
    rgb = padded(TEAL * 0.6 * v[..., None])
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def ice_wall_foot():
    tw, th = b16.FOOT_TEX
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    n = pn(tw, th, 8, 2551, 2)
    rng = np.random.default_rng(2553)
    peb = np.zeros((th, tw))
    for _ in range(300):
        px, py = abs(rng.normal(0, 7)), rng.uniform(0, th)
        r = rng.uniform(0.8, 2.6) * (1.3 - px / 30)
        dy = (yy - py + th / 2) % th - th / 2
        peb = np.maximum(peb, np.clip(1 - np.hypot(xx - px, dy) / max(r, 0.5), 0, 1) * r)
    hgt = 2.2 * peb + 0.8 * n + 3 * np.clip(1 - xx / 12, 0, 1)
    t = np.clip(0.45 + 0.35 * (n - 0.5) + 0.14 * peb, 0, 1)
    alb = ramp([(44, 56, 62), (70, 86, 92), (100, 118, 122), (130, 146, 148)], t)
    rgb = b16.lit_up(hgt, alb)
    tend = (pn(tw, th, 8, 2555, 2) > 0.62) & (xx < 10)       # growth tendrils creeping over the scree
    rgb = np.where(tend[..., None], GROWTH * 0.8, rgb)
    rgb[:, 0:2] = np.array((10, 14, 20), float)
    rgb[:, 2:5] = ramp([(150, 170, 182), (190, 206, 214)], n[:, 2:5])
    cover = np.clip(1.25 - xx / 18, 0, 1)
    cover = np.maximum(cover, (peb > 0.4) * np.clip(1.4 - xx / 26, 0, 1))
    return finish(rgb, 20, dither_alpha(cover, 2), amount=0.04)


def pillar_face():
    rgb, _ = rock_wall_rgb(PILLAR_PERIOD, 2561)
    return finish(padded(rgb, FACE_PAD), 28, amount=0.04)


def _pillar_cap():
    return b16.island_cap("pillar")


def pillar_top():
    """The pillar's cap at 1.25: frosted ice-rock with a few growth patches, a light lip toward the upper left."""
    k = b16.PLATEAU_K
    x0, x1 = min(p[0] for p in PILLAR), max(p[0] for p in PILLAR)
    s0, s1 = PILLAR_ROWS
    w, h = math.ceil((x1 - x0) * k), math.ceil((s1 - s0) * k)
    cap = _pillar_cap()
    cover = b16.polygon_mask([tuple(p) for p in cap], w, h, x0, s1, k)
    cover = np.array(Image.fromarray((cover * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(3)), float) / 255
    yy, xx = grid(h, w)
    n = raster.fbm(w, h, 12, 2571, octaves=4, period=False)
    alb = ramp([(110, 130, 144), (150, 168, 180), (186, 200, 210)], n)
    patch = raster.fbm(w, h, 20, 2573, octaves=3, period=False) > 0.62
    alb = np.where(patch[..., None], GROWTH * 0.9, alb)
    inner = np.array(Image.fromarray((cover * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(5)), float) / 255
    edge = np.clip(cover - inner, 0, 1)
    gy, gx = np.gradient(inner)
    facing = -(gx + gy)
    lip = np.where(facing[..., None] > 0, np.array((214, 226, 234), float), np.array((120, 136, 146), float))
    rgb = np.where((edge > 0.15)[..., None], lip, alb * (0.9 + 0.2 * n)[..., None])
    img = finish(rgb, 24, np.where(cover > 0.5, 255, 0), amount=0.04)
    cut = [round(h * i / 2) for i in range(3)]
    return [img.crop((0, cut[i], w, cut[i + 1])) for i in range(2)]


WALLS = {"ice-wall": ice_wall, "ice-wall-glow": ice_wall_glow, "ice-wall-foot": ice_wall_foot,
         "pillar-face": pillar_face}

# --------------------------------------------------------------------------- pieces


def trench_lip():
    """t=0: Level 24's deep-silt plain at the bottom edge (its tile, so the levels join), the plain's ragged lip at
    ground row ~250 (the ship crosses it at t~1.1), the slope falling into the trench's darker floor, fading into
    trench-floor."""
    h = 800
    bottom = 0
    yy, xx = grid(h)
    rows = bottom + h - 1 - yy
    floor = floor_rows(bottom, h)[..., :3].astype(float)
    try:
        plain = np.array(Image.open(L24 / "deep-silt.png").convert("RGB")).astype(float)
        pl = plain[(plain.shape[0] - 1 - np.mod(rows.astype(int), plain.shape[0]))[:, 0]]
    except FileNotFoundError:
        pl = floor * 1.4
    n = raster.fbm(W, h, 32, 2581, octaves=4, period=False)
    lip = 250 + 14 * np.sin(xx / 37 + 0.6) + 8 * np.sin(xx / 11) + 20 * (n - 0.5)
    d = rows - lip                                            # px beyond the lip
    slope = np.clip(d / 220, 0, 1)
    strata = 0.5 + 0.5 * np.sin(rows * 0.07 + 3 * raster.fbm(W, h, 16, 2582, octaves=3, period=False))
    rock = np.array((52, 66, 72.0)) * (0.7 + 0.45 * strata)[..., None]
    down = rock * (1 - slope[..., None]) + floor * slope[..., None]
    rgb = np.where((d > 0)[..., None], down, pl)
    near = (d <= 0) & (d > -50)
    rgb = np.where(near[..., None], rgb * (0.7 + 0.3 * np.clip(-d / 50, 0, 1))[..., None], rgb)
    blocks = (raster.fbm(W, h, 6, 2583, octaves=2, period=False) > 0.5) & (d > -8) & (d < 4)
    rgb = np.where(blocks[..., None], np.array((96, 108, 100.0)) * (0.8 + 0.4 * n)[..., None], rgb)
    rgb = np.where(((d > 0) & (d < 2))[..., None], np.array((126, 136, 124.0)), rgb)
    shade = np.clip(d / 30, 0, 1) * np.clip((260 - d) / 120, 0, 1)
    h_ = h
    rgb = rgb * (1 - 0.35 * shade[..., None])                 # the slope's shadow under the lip
    k = np.arange(h)[:, None]
    fade = np.clip((k + 0.5) / FEATHER, 0, 1)                 # only the top fades into the trench floor
    alpha = dither_alpha(np.broadcast_to(fade, (h, W)).copy(), 2)
    img = posterize(rgb, 40, alpha)
    a = np.array(img)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA"), bottom


def seam_piece(index, seed, h=320):
    lower, upper = SECTION_TILES[index - 1], SECTION_TILES[index]
    seam = seam_row(index)
    bottom = int(round(seam)) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = grid(h)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    rgb = np.where((yy < line)[..., None], up, lo)
    return posterize(rgb, 40, feather_alpha(h, W)), bottom


def ledge(side, seed=2591):
    """A rock ledge at a wall's or the pillar's foot: a slab of strata rock jutting from the rock side (left: from
    column 0), silt on it, a ring of Vrell growth round the mount at the piece's centre (32, 28); tapering at its ends
    (the foot band covers the join). Lit from the upper left; the right ledge is its own render."""
    w, h, ss = 64, 56, 4
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64) / ss
    u = xx if side == 0 else (w - 1 - xx)
    n = raster.fbm(w * ss, h * ss, 24, seed + side, octaves=3)
    taper = np.sqrt(np.clip(1 - ((yy - h / 2) / (h / 2 - 2.5)) ** 2, 0, 1))
    edge = (46 + 8 * np.sin(yy / 6.0 + side) + 10 * (n - 0.5)) * taper
    inside = (u < edge) & (u >= 1)
    hgt = np.where(inside, 6 + 3 * n - 0.06 * u + 0.6 * np.sin(u * 1.1), 0)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1 - (gx + gy) * 1.8, 0.45, 1.35)
    base = np.array((92, 110, 120.0))
    rgb = base * shade[..., None] * (0.9 + 0.2 * n)[..., None]
    rgb = np.where((n > 0.62)[..., None] & inside[..., None], rgb * 0.6 + np.array((60, 68, 64.0)) * 0.5, rgb)
    rim = inside & (u > edge - 2.5)
    rgb = np.where(rim[..., None], rgb * 0.6, rgb)
    r = np.hypot(xx - 32, yy - 28)
    ring = inside & (r > 12) & (r < 18) & (n > 0.35)
    rgb = np.where(ring[..., None], GROWTH * (0.85 + 0.3 * n)[..., None], rgb)
    a = inside.astype(float)
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    a = a.reshape(h, ss, w, ss).mean(axis=(1, 3))
    return posterize(rgb, 24, (a >= 0.5) * 255)


def cave_mouth(side, seed=2601):
    """A cave notch seen from above (left wall: the rock on the left, the trench on the right): the recess 90 rows by
    70 px with rounded back corners and a ragged edge, its floor darkening into the overhang toward the back, a back
    wall and two side walls (lit where they face the upper left), Vrell growth round the mouth, the rock rim dithering
    out into the projected wall, rubble spilling a little over the foot band; the right wall's is its own render
    (nothing lit mirrored)."""
    w, h = CAVE
    fc = CAVE_FOOT[side]
    ss = 2
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64) / ss + 0.25
    into = (fc - xx) if side == 0 else (xx - fc)              # px into the rock from the foot line
    dv = yy - CAVE_MID                                        # rows from the opening's middle (down = toward ship)
    n = raster.fbm(w * ss, h * ss, 12, seed + side, octaves=4, period=False)
    n2 = raster.fbm(w * ss, h * ss, 5, seed + 3 + side, octaves=2, period=False)
    b = np.clip(into / NOTCH_D, 0, 1)
    half = NOTCH_W / 2 * (1 - 0.4 * np.clip((b - 0.5) / 0.5, 0, 1) ** 2) + 5 * (n - 0.5) + 2 * (n2 - 0.5)
    back = NOTCH_D * (1 - 0.12 * (dv / (NOTCH_W / 2)) ** 2) + 6 * (n - 0.5)
    hole = (into > -0.5) & (into < back) & (np.abs(dv) < half)
    dist_side = half - np.abs(dv)
    dist_back = back - into
    edge_d = np.minimum(dist_side, dist_back)
    k = np.clip(into / np.maximum(back, 1), 0, 1)
    floor = np.array((44, 56, 62.0)) * (0.8 + 0.4 * n2)[..., None]
    rgb = floor * (1 - 0.9 * k ** 0.7)[..., None]             # into the overhang's dark
    band = hole & (edge_d < 4 + 2 * n2)
    lit = ((dv > 0) & (dist_side < dist_back)) | ((side == 1) & (dist_back <= dist_side))
    rock = ROCK * (0.55 + 0.45 * n)[..., None]
    rgb = np.where(band[..., None], np.where(lit[..., None], rock * 0.85, rock * 0.32), rgb)
    shade = 0.6 if side == 0 else 0.95
    f1, f2, ids = _worley(h * ss, w * ss, 18, 20, seed + 9)
    crack = np.clip(1 - (f2 - f1) / 0.14, 0, 1)
    blk = np.random.default_rng(seed + 10).uniform(0.75, 1.15, ids.max() + 1)[ids]
    ex = np.hypot(np.maximum(np.abs(dv) - half, 0), np.maximum(into - back, 0))
    reach = 9 + 9 * n + 4 * n2
    rim = ~hole & (into > -0.5) & (ex < reach)
    rimd = np.where(rim, reach - ex, 0)
    rcol = np.array((70, 90, 102.0)) * (blk * (1 - 0.55 * crack))[..., None] * shade
    gr = (raster.fbm(w * ss, h * ss, 10, seed + 5, octaves=3, period=False) + 0.25 * np.clip(1 - rimd / 14, 0, 1))
    growth = rim & (gr > 0.62)
    rcol = np.where(growth[..., None], GROWTH * (0.7 + 0.4 * n2)[..., None], rcol)
    rgb = np.where(rim[..., None], rcol, rgb)
    out_ = (into <= -0.5) & (into > -22) & (np.abs(dv) < half - 4 + 10 * n) & (n2 > 0.42 - 0.02 * into)
    spill = np.clip(1 + into / 22, 0, 1) * (0.6 + 0.6 * n2)
    chips = n2 > 0.55
    scol = np.where(chips[..., None], np.array((84, 100, 106.0)), np.array((36, 48, 54.0)))
    rgb = np.where(out_[..., None], scol * (0.8 + 0.3 * n)[..., None], rgb)
    cover = np.where(hole, 1.0, 0.0)
    cover = np.maximum(cover, rim * np.clip(rimd / 6, 0, 1))
    cover = np.maximum(cover, out_ * (spill > 0.45))
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    cover = cover.reshape(h, ss, w, ss).mean(axis=(1, 3))
    holed = hole.reshape(h, ss, w, ss).mean(axis=(1, 3)) > 0.5
    alpha = np.where(holed, 255, dither_alpha(np.clip(cover, 0, 1), 2))
    a = np.array(posterize(rgb, 32, alpha))
    a[0, :, 3] = a[-1, :, 3] = 0
    a[:, 0, 3] = a[:, -1, 3] = 0
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


# --------------------------------------------------------------------------- build

def _job(name):
    if name in TILES:
        return TILES[name]()
    return WALLS[name]()


def _beacon_frames():
    body = [props.beacon_body(props.PULSE[k]) for k in range(props.PULSE_FRAMES)]
    body = artkit.quantize_set(body, 32)
    glow = [props.beacon_glow(k) for k in range(props.PULSE_FRAMES)]
    return body, glow


META = None


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in list(OUT.glob("*.png")):
        old.unlink()
    names = list(TILES) + list(WALLS)
    with ProcessPoolExecutor() as ex:
        imgs = list(ex.map(_job, names))
    for name, img in zip(names, imgs):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    for i, img in enumerate(pillar_top()):
        artkit.save_png(img, OUT / f"pillar-top_{i}.png", SOURCE)
    tile.cache_clear()
    pieces, placed = {}, []

    def put(name, img, note, t, x, frames=None, fps=None):
        if name not in pieces:
            if frames:
                for k, f in enumerate(frames):
                    artkit.save_png(f, OUT / f"{name}_{k}.png", SOURCE)
                pieces[name] = {"layer": "ground", "size": [frames[0].width, frames[0].height],
                                "frames": len(frames), "fps": fps}
            else:
                artkit.save_png(img, OUT / f"{name}.png", SOURCE)
                pieces[name] = {"layer": "ground", "size": [img.width, img.height]}
        if t is not None:
            placed.append((note, name, t, x))

    img, bottom = trench_lip()
    put("trench-lip", img, "1. Level 24's plain and its lip at the bottom edge, the slope into the trench; exact rows",
        exact_t(bottom, img.height), 240)
    for i in (3, 4, 5):
        if i >= len(SECTIONS) or SECTION_TILES[i] == SECTION_TILES[i - 1]:
            continue
        img, bottom = seam_piece(i, 2610 + i)
        put(f"seam-{SECTION_TILES[i]}", img, f"section {i + 1}'s seam (ground {seam_row(i):.0f}): "
            f"{SECTION_TILES[i - 1]} into {SECTION_TILES[i]}; exact rows", exact_t(bottom, img.height), 240)
    led = {"left": ledge(0), "right": ledge(1)}
    for side, t, x in LEDGES:
        img = led[side]
        bottom = int(round(target_centre(t, 56) - 56 / 2))
        put(f"rock-ledge-{side}", img, f"the Reef Spitter at t={t:g} x {x:g} on its ledge", exact_t(bottom, 56), x)
    body, glow = _beacon_frames()
    for i, (t, side) in enumerate(BEACONS):
        row = scroll(t)
        x = int(round(float(foot(side, row)) + (26 if side == 0 else -26)))
        foot_row = target_centre(t, 0)
        bb = int(round(foot_row - (props.BEACON[1] - 1 - props.BEACON_BASE[1])))
        put("relay-beacon", None, f"relay beacon {i + 1} at the {('left', 'right')[side]} wall's foot (its foot "
            f"enters at the top edge at t={t:g}; the 4 s pulse; its light relay-beacon-glow, added over the grade)",
            exact_t(bb, props.BEACON[1]), x - props.BEACON_BASE[0] + props.BEACON[0] // 2, frames=body,
            fps=BEACON_FPS)
    put("relay-beacon-glow", None, "", None, None, frames=glow, fps=BEACON_FPS)
    put("cave-mouth-left", cave_mouth(0), "", None, None)
    put("cave-mouth-right", cave_mouth(1), "", None, None)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets, {len(WALLS) + 1} wall/island images and {len(pieces)} pieces to "
          f"{OUT.relative_to(ROOT)}")


# --------------------------------------------------------------------------- proposal

DESC = {
    "trench-floor": "sections 1-3: dark silt between rubble fans shed by the walls, boulders, current lines",
    "trench-scour": "section 4: current-scoured rock, ripples, boulders (the fork and the narrows)",
    "trench-song": "section 5: the silt grown over with Vrell root mats",
    "abyss-floor": "section 6: the floor opening into flat black silt",
}


def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def _pt(p):
    return "[" + ", ".join(f"{v:g}" if isinstance(v, float) else str(v) for v in p) + "]"


def terrain_block():
    src = "the level's data.yaml" if FROM_DATA else "ART-E's proposal (README values)"
    lines = [f"# terrain this art is drawn for ({src}); E3: adopt it or re-run the generator after changing it",
             "terrain:",
             "  left:"]
    for p in LEFT:
        lines.append(f"    - {_pt(p)}   # t={time_at(p[0]):.1f}")
    lines.append("  right:")
    for p in RIGHT:
        lines.append(f"    - {_pt(p)}")
    lines += ["  islands:",
              "    - name: rock pillar   # the fork: two 140 px channels",
              "      look: pillar",
              "      points: [" + ", ".join(_pt(p[:2]) for p in PILLAR) + "]",
              "  notches:   # side, scroll = the terrain row of the notch's middle, width along the wall, depth into it"]
    for nt in NOTCHES:
        lines.append(f"    - {{side: {nt['side']}, scroll: {nt['scroll']}, width: {NOTCH_W}, depth: {NOTCH_D}}}"
                     f"   # {nt['why']}, t={time_at(nt['scroll']):.1f} at the top edge")
    lines.append("  contact: {damage: {easy: 6, medium: 10, hard: 10}, push: 28, push_seconds: 0.12, cooldown: 0.5}")
    lines.append("# ground targets this art assumes (t, x): " + "; ".join(f"{k} ({t:g}, {x:g})" for k, t, x in TARGETS))
    return "\n".join(lines) + "\n"


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if DATA else "the README and ART-E's terrain proposal (no data.yaml yet: re-run after E3)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 25's trench (tools/art/backdrop_l25.py, step ART-E, from {src}).",
             "  # No deep layer (the ground is opaque; the walls' rims project beyond the field's edges). The walls and the",
             "  # pillar are the terrain's (WallProjection, the ice path): ice-wall, ice-wall-glow (additive growth nodules),",
             "  # ice-wall-foot, pillar-face, pillar-top_0..1, in CanyonProfile's convention. cave-mouth-left/right are drawn",
             "  # by the game over the walls at each terrain notch (not placed); relay-beacon-glow is added over the grade at",
             "  # relay-beacon's placements on the same frame clock (D4). Re-run the generator after the data moves a",
             "  # section, a wall, a notch or a target.",
             "  scroll_factors: {far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused, given as the loader asks",
             "  ramp: 2",
             "  haze_colour: 041c26  # the trench's blue-black water",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:"]
    for name in TILES:
        lines.append(f"    {name}: {{layer: ground, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in m["pieces"].items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']:g}" if spec.get("frames") else ""
        if name == "relay-beacon":
            extra += ", keep_clear: 100"
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for note, piece, t, x in m["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    lines.append("# sections' tiles: " + "; ".join(f"{i + 1}: [{t}]" for i, t in enumerate(SECTION_TILES)))
    lines.append(f"# underwater_look (ART-E's proposal, round 54): level-wide {LOOK}; section 6 (the floor opening, the "
                 f"lights going out): {LOOK6}")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.write_text(proposal_block() + "\n" + terrain_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite (the walls projected)

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
        out[y] = tile(SECTION_TILES[section_at_row(row)])[TILE_H - 1 - row % TILE_H]
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


NODE_Z = b16.NODE_Z
NODE_U = b16.NODE_U
SETBACK_CUM = np.concatenate([[0.0], np.cumsum([s[2] for s in b16.SEGMENTS])])
ZS = np.linspace(0, 1, 33)


def _z_col(z):
    return np.interp(z, NODE_Z, NODE_U) * (TEX_W - 1)


def _shade(side, z):
    """CanyonProfile's look in short: cliffs facing away from the upper-left key light dark, benches light."""
    g = min(5, int(np.searchsorted(NODE_Z, z, side="right") - 1))
    if not FACE[g]:
        return 0.88
    return 0.55 if side == 0 else 1.05


def project_walls(t):
    """Per screen pixel the wall surface it shows: (side, z, terrain row) or none, by painting the walls' height
    levels from the foot up (each level's foot line set back and scaled round the projection centre)."""
    base = scroll(t)
    ys = np.arange(-160, SCREEN + 160)
    trows = base - 1 - ys                                      # the terrain row at screen y (foot level)
    zmap = np.full((SCREEN, W), -1.0)
    smap = np.zeros((SCREEN, W), np.int8)
    vmap = np.zeros((SCREEN, W))
    xx = np.arange(W)[None, :]
    for side in (0, 1):
        fx = foot(side, trows)
        hh = wall_height(side, trows)
        valid = ~np.isnan(fx)
        for z in ZS:
            k = 6.0 / (6.0 - 1.2 * z * hh)
            sb = np.interp(z, NODE_Z, SETBACK_CUM) * np.minimum(hh * 2, 1)
            x = np.where(side == 0, fx - sb, fx + sb)
            px = CX + (x - CX) * k
            py = CY + (ys - CY) * k
            ok = valid & (hh > 0.02)
            if not ok.any():
                continue
            yy = np.arange(SCREEN)
            order = np.argsort(py[ok])
            edge = np.interp(yy, py[ok][order], px[ok][order], left=np.nan, right=np.nan)
            vrow = np.interp(yy, py[ok][order], trows[ok][order], left=np.nan, right=np.nan)
            e = edge[:, None]
            m = (xx < e) if side == 0 else (xx > e)
            m &= ~np.isnan(e)
            zmap = np.where(m, z, zmap)
            smap = np.where(m, side, smap)
            vmap = np.where(m, vrow[:, None], vmap)
    return zmap, smap, vmap


def draw_walls(img, t, glow_out):
    zmap, smap, vmap = project_walls(t)
    a = np.array(img).astype(np.float64)
    tex = tile("ice-wall")
    glow = tile("ice-wall-glow")
    footi = tile("ice-wall-foot")
    base = scroll(t)
    # the foot band on the floor first (under the rock)
    for y in range(SCREEN):
        trow = base - 1 - y
        frow = int(footi.shape[0] - 1 - (trow % footi.shape[0]))
        for side in (0, 1):
            fx = float(foot(side, trow))
            if np.isnan(fx) or wall_height(side, trow) < 0.05:
                continue
            for k in range(24):
                x = int(fx) + k if side == 0 else int(fx) - 1 - k
                if 0 <= x < W and footi[frow, k, 3]:
                    a[y, x, :3] = footi[frow, k, :3]
    on = zmap >= 0
    col = np.clip(_z_col(np.clip(zmap, 0, 1)), 0, TEX_W - 1).astype(int)
    vr = (TEX_PERIOD + TEX_PAD - 1 - np.mod(np.nan_to_num(vmap), TEX_PERIOD)).astype(int)
    gidx = np.clip(np.searchsorted(NODE_Z, np.clip(zmap, 0, 1), side="right") - 1, 0, 5)
    cliff = np.array(FACE)[gidx]
    shade = np.where(cliff, np.where(smap == 0, 0.55, 1.05), 0.88)
    rgb = tex[vr, col, :3].astype(float) * shade[..., None]
    a[..., :3] = np.where(on[..., None], rgb, a[..., :3])
    g = glow[vr, col, :3].astype(float)
    glow_out[...] = np.where(on[..., None], g, 0)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def draw_notches(img, t):
    base = scroll(t)
    caves = {0: np.array(Image.open(OUT / "cave-mouth-left.png").convert("RGBA")),
             1: np.array(Image.open(OUT / "cave-mouth-right.png").convert("RGBA"))}
    for nt in NOTCHES:
        side = 0 if nt["side"] == "left" else 1
        y = base - 1 - nt["scroll"]
        if -80 < y < SCREEN + 80:
            fx = float(foot(side, nt["scroll"]))
            _paste_rows(img, caves[side], int(round(fx - CAVE_FOOT[side])), int(round(y - CAVE_MID)))


def draw_pillar(img, t, glow_out):
    """The pillar as WallProjection draws a butte (approx.): its rings from the foot up, each the foot polygon offset
    inward by the steps' setbacks x 0.36 and scaled round the projection centre, painted with pillar-face's column for
    that height (the side facing the upper-left key light lit, the other shaded); then the cap at 1.25."""
    base = scroll(t)
    if not (PILLAR_ROWS[0] - SCREEN - 200 < base < PILLAR_ROWS[1] + 400):
        return img
    pts, per, nrm, rings, perimeter = b16.island_rings("pillar")
    face = np.array(Image.open(OUT / "pillar-face.png").convert("RGBA"))
    a = np.array(img)
    sc = b16.ISLAND_SCALE["pillar"]
    yy, xx = np.mgrid[0:SCREEN, 0:W]
    for z in ZS:
        s = np.interp(z, NODE_Z, SETBACK_CUM) * sc
        k = 6.0 / (6.0 - 1.2 * z)
        ring = pts + nrm * s
        px = CX + (ring[:, 0] - CX) * k
        py = CY + ((base - 1 - ring[:, 1]) - CY) * k
        m = Image.new("L", (W, SCREEN), 0)
        ImageDraw.Draw(m).polygon([(px[i], py[i]) for i in range(len(ring))], fill=255)
        mm = np.array(m) > 0
        if not mm.any():
            continue
        u = int(np.clip(_z_col(z), 0, TEX_W - 1))
        vr = (face.shape[0] - 1 - np.mod(base - 1 - yy, PILLAR_PERIOD)).astype(int)
        c = face[vr, u, :3].astype(float)
        mid = CX + (np.mean(pts[:, 0]) - CX) * k
        sh = np.where(xx < mid, _shade(1, z), _shade(0, z))
        a[mm, :3] = np.clip(c[mm] * sh[mm][:, None], 0, 255).astype(np.uint8)
        a[mm, 3] = 255
    out = Image.fromarray(a, "RGBA")
    k = 1.25
    x0 = min(p[0] for p in PILLAR)
    left = CX + (x0 - CX) * k
    edge = CY + ((base - 1 - PILLAR_ROWS[1]) - CY) * k
    for i in range(2):
        sl = Image.open(OUT / f"pillar-top_{i}.png").convert("RGBA")
        lay = Image.new("RGBA", (W, SCREEN), (0, 0, 0, 0))
        lay.paste(sl, (int(round(left)), int(round(edge))))
        out.alpha_composite(lay)
        edge += sl.height
    return out


def held_frames(name):
    try:
        return kit.load(name)
    except FileNotFoundError:
        return []


@lru_cache(maxsize=None)
def stand_ins():
    floor = held_frames("reef-spitter-floor")
    gun = artkit.load_frames("reef-spitter")
    nest = None
    if floor and gun:
        nest = floor[0].copy()
        nest.alpha_composite(gun[0], (28 - gun[0].width // 2, 27 - gun[0].height // 2))
    sub = held_frames("survey-sub")
    return {"reef-spitter": nest, "survey-sub": sub[0] if sub else None}


def composite(t, gpieces, frame=None, ship=True):
    k = int(t * BEACON_FPS) % props.PULSE_FRAMES if frame is None else frame
    spots = []
    img = ground_view(t, gpieces, k, spots)
    base = scroll(t)
    st = stand_ins()
    for kind, tt, x in TARGETS:
        im = st.get(kind)
        if im is None:
            continue
        y = base + SCREEN - target_centre(tt, im.height)
        if -60 < y < SCREEN + 60 and kind != "survey-sub":
            artkit.sprite.paste_center(img, im, x, y)
    wall_glow = np.zeros((SCREEN, W, 3))
    img = draw_walls(img, t, wall_glow)
    draw_notches(img, t)
    for kind, tt, x in TARGETS:                     # the sub lies inside its notch: over the cave piece
        im = st.get(kind)
        if kind == "survey-sub" and im is not None:
            y = base + SCREEN - target_centre(tt, im.height)
            if -60 < y < SCREEN + 60:
                artkit.sprite.paste_center(img, im, x, y)
    img = draw_pillar(img, t, wall_glow)
    if ship:
        artkit.sprite.paste_center(img, kit.ship(2), 240, SCREEN - SHIP_Y)
    img = kit.grade(img, 4)
    a = np.array(img).astype(np.float64)
    a[..., :3] += wall_glow
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    glow = piece_frames("relay-beacon-glow")[k]
    for x0, y_top in spots:
        img = kit.add_c(img, Image.fromarray(glow, "RGBA"), x0 + glow.shape[1] / 2, y_top + glow.shape[0] / 2)
    return img


# --------------------------------------------------------------------------- checks and review

def wrap_score(a):
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def check():
    """Wraps (BackdropSeamsTest's measure), sizes, the edge rule of BackdropAssetsTest, the geometry rules this art
    assumes (width >= 180, channels >= 130, a wall moving <= 0.5 px per px), the notches inside the field and clear of
    the ledges, the beacons' clear 100 px."""
    problems = []
    for name in TILES:
        t = tile(name)
        a = t[..., :3].astype(float)
        jump = np.abs(a[0] - a[-1]).mean()
        inner = np.abs(a[1:] - a[:-1]).mean()
        if jump > 3 * inner + 6:
            problems.append(f"{name}: wrap seam {jump:.1f} vs {inner:.1f}")
        if wrap_score(t) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(t):.2f} (BackdropSeamsTest's limit 1.5)")
        if (t[..., 3] < 255).any():
            problems.append(f"{name}: a ground tile must be opaque")
    for name, size in (("ice-wall", (TEX_W, TEX_PERIOD + TEX_PAD)), ("ice-wall-glow", (TEX_W, TEX_PERIOD + TEX_PAD)),
                       ("ice-wall-foot", b16.FOOT_TEX), ("pillar-face", (TEX_W, PILLAR_PERIOD + FACE_PAD))):
        arr = tile(name)
        if (arr.shape[1], arr.shape[0]) != tuple(size):
            problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]}, wants {size}")
    for name, spec in meta()["pieces"].items():
        for arr in piece_frames(name):
            if arr.shape[0] > MAX_PIECE or tuple(spec["size"]) != (arr.shape[1], arr.shape[0]):
                problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]} (spec {spec['size']})")
                break
    for name, bottom, x0, arrs in placed_pieces():
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
    rows = np.arange(0, scroll(END), 5.0)
    xl, xr = foot(0, rows), foot(1, rows)
    xl, xr = np.nan_to_num(xl, nan=0.0), np.nan_to_num(xr, nan=float(W))
    width = xr - xl
    if width.min() < 180:
        problems.append(f"flyable width {width.min():.0f} < 180")
    for r, a_, b_ in zip(rows, xl, xr):
        span = pillar_span(r)
        if span and min(span[0] - a_, b_ - span[1]) < 130:
            problems.append(f"channel at row {r:.0f}: {min(span[0] - a_, b_ - span[1]):.0f} < 130")
            break
    for pts, nm in ((LEFT, "left"), (RIGHT, "right")):
        for p, q in zip(pts, pts[1:]):
            if abs(q[1] - p[1]) > 0.5 * (q[0] - p[0]):
                problems.append(f"{nm} wall {p} -> {q}: steeper than 0.5")
    for nt in NOTCHES:
        side = 0 if nt["side"] == "left" else 1
        fx = float(foot(side, nt["scroll"]))
        back = fx - NOTCH_D if side == 0 else fx + NOTCH_D
        if np.isnan(fx) or back < 0 or back > W:
            problems.append(f"notch {nt}: its back at x {back:.0f} leaves the field")
        for s, t, x in LEDGES:
            if s == nt["side"] and abs(scroll(t) + 28 - nt["scroll"]) < NOTCH_W / 2 + 28:
                problems.append(f"notch at {nt['scroll']} over the ledge at t={t:g}")
    for kind, t, x in TARGETS:
        for bt, side in BEACONS:
            bx = float(foot(side, scroll(bt))) + (26 if side == 0 else -26)
            if kind != "survey-sub" and math.hypot(x - bx, scroll(t) + 28 - scroll(bt)) < 100:
                problems.append(f"{kind} at t={t:g} x {x:g}: within 100 px of the beacon at t={bt}")
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in sorted(set(problems)):
        print("  " + p)
    return problems


def _nt(side):
    return next(time_at(n["scroll"] + 150) for n in NOTCHES if n["side"] == side and "cave" in n["why"])


TIMES = [(1, "1 THE PLAIN'S LIP, THE TRENCH AHEAD"), (14, "1 WALLS RISING, BEACONS"), (28.5, "2 THE WALLS, A CAVE"),
         (40, "2 A RIGHT CAVE, A BEACON"), (78.5, "3 THE SURVEY SUB'S CAVE"), (100, "4 THE FORK ROUND THE PILLAR"),
         (112, "4 THE NARROWS, 190 PX"), (139.5, "5 THE SONG: ROOT MATS"), (163, "6 THE WALLS SINKING"),
         (171, "6 THE OPEN FLOOR")]


def review():
    g = placed_pieces()
    shots = [composite(t, g, frame=0 if i % 2 == 0 else 2) for i, (t, _) in enumerate(TIMES)]
    cols = 5
    cw, ch = 240, 270
    right = 3 * (100 + 8) + 16
    sheet = raster.sheet(16 + cols * (cw + 8) + 8 + right, 60 + 2 * (ch + 24) + 330,
                         "LEVEL 25 BACKDROP - SIREN TRENCH (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R54")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/2 UNDER THE REVIEW GRADE (AMBIENT 0.14); THE WALLS AND THE PILLAR "
                     "PROJECTED AS THE GAME DOES (APPROX.); STAND-INS: REEF SPITTERS, SHIP", raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t:g} {label}"[:40], raster.LABEL_DIM)
    x0 = 16 + cols * (cw + 8) + 8
    raster.draw_text(sheet, x0, 38, "TILES 1/4, WALL 1/2 (UNGRADED)", raster.LABEL)
    for k, name in enumerate(TILES):
        im = Image.fromarray(tile(name), "RGBA").resize((100, 200), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + (k % 3) * 108, 56 + (k // 3) * 226))
        raster.draw_text(sheet, x0 + (k % 3) * 108, 56 + (k // 3) * 226 + 204, name.upper()[:16], raster.LABEL_DIM)
    wall = Image.fromarray(tile("ice-wall")[TEX_PAD:TEX_PAD + 400], "RGBA").resize((64, 200), Image.Resampling.BOX)
    sheet.alpha_composite(wall, (x0 + 108, 56 + 226))
    cl = Image.fromarray(piece_frames("cave-mouth-right")[0], "RGBA")
    sheet.alpha_composite(cl, (x0 + 2 * 108 - 8, 56 + 226))
    raster.draw_text(sheet, x0 + 108, 486, "ICE-WALL, CAVE", raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24)
    raster.draw_text(sheet, 16, y, "AT 1X: A CAVE MOUTH AND A BEACON (T=40), THE FORK (T=100), THE NARROWS (T=112)",
                     raster.LABEL)
    sheet.alpha_composite(shots[3].crop((240, 0, 480, 300)), (16, y + 16))
    sheet.alpha_composite(shots[5].crop((60, 60, 420, 360)), (16 + 248, y + 16))
    sheet.alpha_composite(shots[6].crop((80, 100, 400, 400)), (16 + 248 + 368, y + 16))
    frames = [composite(t, g).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(96, 104, 0.125)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


def main(argv):
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv and (OUT / "trench-floor.png").exists() and PROPOSAL.exists():
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
