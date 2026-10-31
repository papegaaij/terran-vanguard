#!/usr/bin/env python3
"""Production art: the Level 26 backdrop, Blackwater (design/campaign/act-4-deep-water/level-26-blackwater; M8 part F
batch, concept round 55, step ART-F).

Straight to production from round 48's darkness look **a** (tools/concept/europa_r48.py ``black_still``, the hard
cone; europa's *Darkness*): kilometres of black water over a sea floor out of sight. Almost everything is black: the
ground carries the unlit albedo and the game's light map (LX's headlights at Level 26's values, the 6 % ambient, the
static lamps, gap check m8f §1.4) decides what shows; the only light that is the art's own is the pieces' additive
``-glow`` (added over the grade: the lamp posts' sodium lamps, the trench's last beacons, the amber seep, the vent
field's red glow) and the scarp's sparse growth nodules (``ice-wall-glow``). The structure is backdrop_l25.py's (its
scroll helpers, its wall textures, cave mouth and the walls' projection are imported and reused, never edited); the
beacons and their pulse are tools/art/l24_props.py's relay beacon.

**The terrain this art is drawn for** (``proposal_terrain``, written into the proposal file as a ``terrain:`` block
for step F2; the data's when it exists): a single **right** wall, the fault scarp (gap check §5 F5), rising at the
field's edge from t=56, 400 px of flyable width from t=61, closing to 280 px by t=80, holding to t=92, then sinking
out of the field by t=103; three notches (90 x 70) in it, one per `turret nest` wave (t=62, 72, 86: the notch's middle
150 px below the top edge at the wave's t). No left wall (open water).

Tile sets (480x960, wrapping, ground; unlit albedo):
  pipeline-floor  section 1: dark silt, the drowned Thera Deep pipeline down the left third (flanged sections every
                  120 px, silt drifts over it, a few anchor blocks), a few anchor blocks
  black-water     sections 2-6: the sea floor far below, barely there: near-black silt with faint broad relief and
                  a few pale stones, so the cone reveals only a dim murk
Wall textures (CanyonProfile's convention, backdrop_l25.py's ice path, the right wall only):
  ice-wall        128x1280  the scarp's fractured rock (backdrop_l25's rock, its own seed), Vrell growth sparse
  ice-wall-glow   128x1280  additive: a few teal growth nodules (no lip rim: the game's rim light draws the edge)
  ice-wall-foot   24x512    the foot band (backdrop_l25's)
Pieces (ground; exact rows):
  seam-black-water 480x320  section 2's seam: the pipeline broken off, its last flanged section half buried
  relay-beacon    128x192, 16 frames at 4 fps (D's piece, Level 25's pulse): two of the trench's last beacons at the
                  start, falling behind; relay-beacon-glow its light (added over the grade)
  lamp-post       64x96, 4 frames at 4 fps: a Thera Deep lamp post beside the pipeline (a leaning mast, an arm over the
                  pipe, a sodium lamp), one every 360 px to t~14; lamp-post-glow its light (the lamp's core and halo;
                  the light pool on the floor is the game's static light, ``lights:``)
  lamp-post-dying 64x96, 8 frames at 6 fps: the last lamp post, its lamp failing (flicker, out, a stutter back)
                  with lamp-post-dying-glow
  amber-seep      320x320: a mineral seep far below in section 5 (a pale crust and its cracks, mostly transparent);
                  amber-seep-glow its faint amber glow (from t~140)
  vent-field      480x2040: the vent field's far chimneys at the top of section 6 (mostly transparent);
                  vent-field-glow its red glow rising on the horizon from t~168 to the end
  cave-mouth-right 112x124, NOT placed: drawn by the game over the scarp at each terrain notch (E5's rule)
Review: design/campaign/act-4-deep-water/level-26-blackwater/concept/backdrop-final-r55-a.png/.gif (the dark look's
stand-in, tools/art/l26kit.py: the ambient ramp 20 -> 6 % over t=8-18, the headlights, Rook's lamp, the lamp posts'
pools; the scarp projected as the game does, approximately); `--proposal` writes the `backdrop:` block, the proposed
`terrain:` block and the static `lights:` to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Run: python3 tools/art/backdrop_l26.py [--proposal | --review | --check]   (all: about 2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image

import artkit
import backdrop_l25 as b25            # noqa: E402  (tools/art: scroll helpers, the walls, cave mouth, projection)
import l24_props as props
import l26kit as kit
from artkit import DESIGN, ROOT, TAU, raster

from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "backdrop_l26.py"
BATCH = "M8 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r55"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-26-blackwater"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-26"
W, SCREEN, TILE_H = 480, 540, 960
FEATHER = 32
OUTRO = 15
MAX_PIECE = 2040
BEACON_FPS = 4
LAMP = (64, 96)
LAMP_BASE = (40, 78)                         # the mast's foot on the floor (px in the piece)
LAMP_HEAD = (22, 26)                         # the lamp under its hood, over the pipeline
PIPE_X = 150                                 # the pipeline's axis (px from the left)
LAMP_X = PIPE_X + 18                         # the lamp posts' piece centre (the lamp over the pipe)
LAMP_ROWS = [115, 475, 835, 1195, 1555, 1915]   # one every 360 px; the last at the ship's row at t~14
NOTCH_W, NOTCH_D = b25.NOTCH_W, b25.NOTCH_D
NEST_WAVES = [62, 72, 86]
SODIUM = np.array((255, 196, 110), float)
AMBER = np.array((255, 150, 40), float)
VENT_RED = np.array((230, 50, 24), float)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
FALLBACK_SECTIONS = [{"end": 20, "speed": 130}, {"end": 60, "speed": 120, "ramp": 2}, {"end": 100, "speed": 120},
                     {"end": 130, "speed": 120}, {"end": 155, "speed": 120}, {"end": 182, "speed": 110, "ramp": 2}]
SECTIONS = DATA["sections"] if DATA else FALLBACK_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 130)) if DATA else 130.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
SECTION_TILES = ["pipeline-floor"] + ["black-water"] * (len(SECTIONS) - 1)
LOOK1 = ("{ambient: 0.20, ramp: {to: 0.06, from: 8, until: 18}, tint: 80aceb, caustics: 0.0, snow: 110, "
         "headlights: {reach: 260, angle: 70, strength: 1.0}, rim: 0.15}")
LOOK = "{ambient: 0.06, tint: 80aceb, caustics: 0.0, snow: 110, headlights: {reach: 260, angle: 70, strength: 1.0}, rim: 0.15}"


# --------------------------------------------------------------------------- the clock (as backdrop_l25)

def _speeds():
    out, prev = [], None
    for start, s in zip(STARTS, SECTIONS):
        sp = float(s.get("speed", BASE_SPEED))
        out.append((start, float(s["end"]), sp, float(s.get("ramp", 2)), prev))
        prev = sp
    return out


SPEEDS = _speeds()
DT = 1 / 600


def speed_at(t):
    v = SPEEDS[0][2]
    for start, end, sp, ramp, prev in SPEEDS:
        if t >= start:
            if ramp and prev is not None and prev != sp and t < start + ramp:
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


def place_t(centre_row):
    return time_at(centre_row - SCREEN / 2)


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


def ambient_at(t):
    """The proposed look's ambient: 20 % in the lamp stretch, ramping to 6 % over t=8-18 (gap check §5 F4)."""
    u = np.clip((t - 8) / 10, 0, 1)
    return float(0.20 + (0.06 - 0.20) * u * u * (3 - 2 * u))


# --------------------------------------------------------------------------- the terrain (data, or the proposal)

def _f(t):
    return int(round(scroll(t) / 10.0) * 10)


def proposal_right():
    return [(_f(56), 480, 0.0), (_f(59), 430, 0.7), (_f(61), 400), (_f(66), 400), (_f(74), 330), (_f(80), 290),
            (_f(92), 280), (_f(97), 300), (_f(100), 360, 0.5), (_f(103), 480, 0.0)]


def _terrain():
    if DATA and DATA.get("terrain"):
        tr = DATA["terrain"]
        right = [tuple(p) for p in tr["right"]]
        notches = [{"side": n["side"], "scroll": int(n["scroll"]), "why": "data"} for n in tr.get("notches") or []]
        return right, notches, True
    right = proposal_right()
    notches = [{"side": "right", "scroll": _f(t) - 150, "why": f"the t={t:g} turret nest wave"} for t in NEST_WAVES]
    return right, notches, False


RIGHT, NOTCHES, FROM_DATA = _terrain()


def foot(side, trow):
    if side == 0:
        return np.full(np.shape(trow), np.nan) if np.ndim(trow) else float("nan")
    return b25._wall_x(RIGHT, trow)


def wall_height(side, trow):
    if side == 0:
        return np.zeros(np.shape(trow)) if np.ndim(trow) else 0.0
    return b25._wall_h(RIGHT, trow)


# --------------------------------------------------------------------------- tiles

pn, finish, padded, dither_alpha, posterize = b25.pn, b25.finish, b25.padded, b25.dither_alpha, b25.posterize


def black_water():
    """The far floor: near-black silt (b25's trench floor at a fraction of its light), broad faint relief, few stones."""
    img = b25.trench_tile(2661, 0.42, rubble=0, boulders=3, stones=18, relief=0.16)
    a = np.array(img).astype(np.float64)
    n = pn(W, TILE_H, 160, 2663, 3)
    a[..., :3] = a[..., :3] * (0.30 + 0.18 * n)[..., None] + np.array((2, 4, 7.0))
    return posterize(a[..., :3], 24)


def pipeline_floor():
    """Section 1: dark silt and the drowned pipeline (wrapping): a corroded pipe lit from the upper left, a flange
    every 120 px, silt drifted over it in places, anchor blocks."""
    img = b25.trench_tile(2671, 0.55, rubble=10, boulders=5, stones=30, relief=0.2)
    a = np.array(img).astype(np.float64)[..., :3]
    yy, xx = np.mgrid[0:TILE_H, 0:W].astype(np.float64)
    n = pn(W, TILE_H, 16, 2673, 3)
    r = 11.0
    u = (xx - PIPE_X) / r
    pipe = np.abs(u) < 1
    lam = np.clip(0.55 - 0.65 * u + 0.25 * np.sqrt(np.clip(1 - u * u, 0, 1)), 0.15, 1.2)
    rust = np.array((92, 70, 52.0)) * (0.8 + 0.4 * n)[..., None]
    steel = np.array((86, 94, 98.0))
    body = np.where((n > 0.55)[..., None], rust, steel) * lam[..., None]
    flange = (np.mod(yy, 120) < 5) & (np.abs(xx - PIPE_X) < r + 2.5)
    fl_lam = np.where(np.mod(yy, 120) < 2, 1.15, 0.75)
    body = np.where(flange[..., None], np.array((104, 110, 112.0)) * fl_lam[..., None], body)
    drift = pn(W, TILE_H, 48, 2675, 2) > 0.62
    shadow = (xx > PIPE_X + r) & (xx < PIPE_X + r + 5) & ~drift
    a = np.where(shadow[..., None], a * 0.55, a)
    cover = (pipe | flange) & ~drift
    a = np.where(cover[..., None], body, a)
    silt_edge = (pipe | flange) & drift & (n > 0.45)
    a = np.where(silt_edge[..., None], a * 1.08 + 4, a)
    for row in range(60, TILE_H, 240):                    # anchor blocks astride the pipe
        blk = (np.abs(yy - row) < 6) & (np.abs(xx - PIPE_X) < r + 7) & ~pipe
        a = np.where(blk[..., None], np.array((74, 78, 76.0)) * (0.9 + 0.2 * n)[..., None], a)
    return posterize(a, 32)


TILES = {"pipeline-floor": pipeline_floor, "black-water": black_water}


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(t.shape[0] - 1 - np.mod(rows, t.shape[0])).astype(int)]


def feather_alpha(h, w):
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    return dither_alpha(np.broadcast_to(a, (h, w)).copy(), 2)


# --------------------------------------------------------------------------- walls

def ice_wall():
    rgb, _ = b25.rock_wall_rgb(seed=2681)
    rgb = rgb * 0.82                                      # the scarp's darker basalt-grey
    return finish(padded(rgb), 32, amount=0.04)


def ice_wall_glow():
    yy, xx, G, T = b25._wall_fields()
    _, nods = b25.rock_wall_rgb(seed=2681)
    f = np.zeros((b25.TEX_PERIOD, b25.TEX_W))
    for x, y, r in nods[::2]:                             # half the trench's nodules: the scarp's growth is sparse
        for dy in (-b25.TEX_PERIOD, 0, b25.TEX_PERIOD):
            d = np.hypot(xx - x, yy - y - dy)
            f = np.maximum(f, np.clip(1 - d / r, 0, 1) + 0.35 * np.clip(1 - d / (r + 2.5), 0, 1))
    v = artkit.ordered_dither(np.clip(f, 0, 1), 4)
    rgb = padded(b25.TEAL * 0.55 * v[..., None])         # no lip rim: in the black the game's rim light does it
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


WALLS = {"ice-wall": ice_wall, "ice-wall-glow": ice_wall_glow, "ice-wall-foot": b25.ice_wall_foot}


# --------------------------------------------------------------------------- pieces

def seam_piece(index, seed, h=320):
    """Section 2's seam: the pipeline floor giving way to the black water along a ragged line; the pipe ends broken."""
    lower, upper = SECTION_TILES[index - 1], SECTION_TILES[index]
    bottom = int(round(seam_row(index))) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    line = np.where(np.abs(xx - PIPE_X) < 16, h / 2 + 50, line)    # the pipe reaches a little further, then ends
    rgb = np.where((yy < line)[..., None], up, lo)
    end = (np.abs(yy - (h / 2 + 50)) < 2.5) & (np.abs(xx - PIPE_X) < 12)
    rgb = np.where(end[..., None], np.array((20, 18, 16.0)), rgb)
    return posterize(rgb, 40, feather_alpha(h, W)), bottom


def lamp_mats(on=1.0):
    return [Material((0.42, 0.44, 0.46), metal=0.6, shininess=60, spec=0.7),     # 0 the mast, hood
            Material((0.18, 0.19, 0.2), metal=0.5, shininess=30, spec=0.4),      # 1 dark fittings
            Material((0.55, 0.54, 0.5), metal=0.1, shininess=15, spec=0.2),      # 2 the footing
            Material((0.4 + 0.5 * on, 0.3 + 0.36 * on, 0.18 + 0.12 * on), metal=0.1, shininess=120, spec=1.2),  # 3 lens
            Material((0.62, 0.42, 0.12), metal=0.3, shininess=50, spec=0.6)]     # 4 a hazard band


def lamp_scene():
    bx, by = props._model(LAMP, *LAMP_BASE)
    hx, hy = props._model(LAMP, *LAMP_HEAD)
    top = 40.0
    lean = 0.42                                       # the mast leans toward the camera's top, per its height

    def along(u):
        return (bx, by + lean * top * u, top * u)

    def sc(p):
        ax, ay, az = along(1.0)
        items = [(sd_cylinder_z(p, (bx, by, 1.0), 7.0, 1.0), 2),
                 (sd_capsule(p, along(0.0), along(1.0), 2.4, 1.7), 0),
                 (sd_capsule(p, along(0.45), along(0.5), 2.7, 2.6), 4),
                 (sd_capsule(p, (ax, ay, az), (hx, hy, az + 1.0), 1.1), 0),          # the arm over the pipe
                 (sd_box(p, (hx, hy, az + 1.6), (5.0, 3.6, 1.6), 0.8), 0),           # the hood
                 (sd_sphere(p, (hx, hy - 0.4, az - 0.6), 2.6), 3),                    # the lens under it
                 (sd_capsule(p, (bx + 5, by - 3, 1.0), (ax + 1, ay - 1, az - 6), 0.4), 1)]   # the cable
        return union(*items, k=0.4)
    return sc


@lru_cache(maxsize=None)
def _lamp_render(on):
    return props.render(lamp_scene(), lamp_mats(on), LAMP, z_top=56)


FLICKER = [1.0, 0.92, 1.0, 0.96]
DYING = [1.0, 0.3, 0.9, 0.0, 0.0, 0.55, 0.0, 0.0]


def lamp_glow(level):
    """The lamp's light (additive, ungraded): the lens's core and a sodium halo under the hood."""
    w, h = LAMP
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    lx, ly = LAMP_HEAD[0], LAMP_HEAD[1] + 2
    r = np.hypot(xx - lx, yy - ly)
    core = np.exp(-(r / 1.8) ** 2)
    halo = np.exp(-(r / 9.0) ** 2) * 0.55 + np.exp(-(r / 20.0) ** 2) * 0.22
    v = np.clip((core + halo) * level, 0, 1)
    q = artkit.smooth_steps(v)
    rgb = SODIUM[None, None] * q[..., None] + 80 * np.clip(core * level - 0.6, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def lamp_frames(levels):
    body = artkit.quantize_set([_lamp_render(round(min(1.0, lv), 2)) for lv in levels], 32)
    return body, [lamp_glow(lv) for lv in levels]


def seep():
    """A mineral seep far below: a pale crust patch, its cracks, mostly transparent; the glow apart."""
    s = 320
    yy, xx = np.mgrid[0:s, 0:s].astype(np.float64)
    n = raster.fbm(s, s, 40, 2691, octaves=4, period=False)
    r = np.hypot(xx - s / 2, (yy - s / 2) * 1.2) / (s / 2)
    cover = np.clip((1 - r) * 1.6 + (n - 0.5), 0, 1) * np.clip((1 - r) * 4, 0, 1)
    f1, f2, _ = b25._worley(s, s, 24, 24, 2693)
    crack = np.clip(1 - (f2 - f1) / 0.1, 0, 1)
    rgb = np.array((52, 46, 38.0)) * (0.8 + 0.4 * n)[..., None] * (1 - 0.6 * crack)[..., None]
    return posterize(rgb, 16, artkit.smooth_steps(cover * 0.9) * 255), crack, cover     # DITH8: was dither_alpha(.., 2)


def seep_glow(crack, cover):
    s = crack.shape[0]
    yy, xx = np.mgrid[0:s, 0:s].astype(np.float64)
    r = np.hypot(xx - s / 2, (yy - s / 2) * 1.2) / (s / 2)
    soft = np.clip(1 - r, 0, 1) ** 1.6 * 0.3
    v = np.clip(soft + crack * cover * soft * 0.5, 0, 1)
    q = artkit.smooth_steps(v)
    rgb = AMBER[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((s, s), 255.0)]).astype(np.uint8)))


VENT_H = 2040


def vent_field():
    """The far vents at the top of section 6: chimney silhouettes against the glow, mostly transparent; the bottom
    fades in from nothing, the top 120 rows fade out (the top row stays transparent)."""
    w, h = W, VENT_H
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    up = 1 - yy / h                                       # 0 at the bottom, 1 at the top
    rise = np.clip((up - 0.05) / 0.5, 0, 1)
    rng = np.random.default_rng(2701)
    cover = np.zeros((h, w))
    rgb = np.zeros((h, w, 3))
    for _ in range(46):
        cx, cy = rng.uniform(0, w), rng.uniform(h * 0.08, h * 0.9)
        rad = rng.uniform(6, 16) * (0.6 + 0.8 * (1 - cy / h))
        d = np.hypot(xx - cx, (yy - cy) * 1.1)
        m = d < rad
        cover = np.where(m, 1.0, cover)
        lit = np.clip(1 - (xx - cx + yy - cy) / (rad * 1.5), 0.4, 1.0)
        rgb = np.where(m[..., None], np.array((40, 30, 28.0)) * lit[..., None], rgb)
        mouth = d < rad * 0.35
        rgb = np.where(mouth[..., None], np.array((10, 6, 6.0)), rgb)
    top = np.clip((yy - 4) / 120, 0, 1)
    cover = cover * rise * top
    return posterize(rgb, 16, dither_alpha(np.clip(cover, 0, 1), 2))


def vent_glow():
    w, h = W, VENT_H
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    up = 1 - yy / h
    n = raster.fbm(w, h, 80, 2703, octaves=3, period=False)
    rise = np.clip((up - 0.12) / 0.88, 0, 1) ** 1.8
    top = np.clip((yy - 4) / 160, 0, 1)
    v = np.clip(rise * (0.2 + 0.12 * n) * top, 0, 1)
    q = artkit.smooth_steps(v)
    rgb = VENT_RED[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# --------------------------------------------------------------------------- build

def _job(name):
    if name in TILES:
        return TILES[name]()
    return WALLS[name]()


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

    img, bottom = seam_piece(1, 2711)
    put("seam-black-water", img, f"section 2's seam (ground {seam_row(1):.0f}): the pipeline floor into the black "
        "water, the pipe broken off; exact rows", exact_t(bottom, img.height), 240)
    body, glow = b25._beacon_frames()
    for i, (row, x) in enumerate(((330, 64), (470, 420))):
        bb = int(round(row - (props.BEACON[1] - 1 - props.BEACON_BASE[1])))
        put("relay-beacon", None, f"the trench's last beacon {i + 1}, falling behind at the start (its foot at ground "
            f"row {row}); its light relay-beacon-glow, added over the grade", exact_t(bb, props.BEACON[1]),
            x - props.BEACON_BASE[0] + props.BEACON[0] // 2, frames=body, fps=BEACON_FPS)
    put("relay-beacon-glow", None, "", None, None, frames=glow, fps=BEACON_FPS)
    lb, lg = lamp_frames(FLICKER)
    db, dg = lamp_frames(DYING)
    for i, row in enumerate(LAMP_ROWS):
        name = "lamp-post-dying" if i == len(LAMP_ROWS) - 1 else "lamp-post"
        bb = int(round(row - (LAMP[1] - 1 - LAMP_BASE[1])))
        put(name, None, f"lamp post {i + 1} beside the pipeline (its foot at ground row {row}"
            + ("; the last, failing" if name.endswith("dying") else "") + f"); its light {name}-glow",
            exact_t(bb, LAMP[1]), LAMP_X, frames=lb if name == "lamp-post" else db,
            fps=4 if name == "lamp-post" else 6)
    put("lamp-post-glow", None, "", None, None, frames=lg, fps=4)
    put("lamp-post-dying-glow", None, "", None, None, frames=dg, fps=6)
    simg, crack, cover = seep()
    sg = seep_glow(crack, cover)
    for t, x in ((143.0, 130), (149.5, 352)):
        put("amber-seep", simg, f"an amber seep far below at t={t:g} (section 5); its light amber-seep-glow", t, x)
    put("amber-seep-glow", sg, "", None, None)
    vimg = vent_field()
    bottom = int(round(scroll(166.0) + SCREEN))
    put("vent-field", vimg, "the vent field's far chimneys and red glow (vent-field-glow) rising on the horizon from "
        "t~168; exact rows", exact_t(bottom, VENT_H), 240)
    put("vent-field-glow", vent_glow(), "", None, None)
    put("cave-mouth-right", b25.cave_mouth(1, 2721), "", None, None)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets, {len(WALLS)} wall images and {len(pieces)} pieces to "
          f"{OUT.relative_to(ROOT)}")


# --------------------------------------------------------------------------- proposal

DESC = {"pipeline-floor": "section 1: dark silt, the drowned Thera Deep pipeline and its anchor blocks",
        "black-water": "sections 2-6: the far floor, near black (the cone reveals a dim murk)"}


def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def _pt(p):
    return "[" + ", ".join(f"{v:g}" if isinstance(v, float) else str(v) for v in p) + "]"


def terrain_block():
    src = "the level's data.yaml" if FROM_DATA else "ART-F's proposal (README values, gap check §5 F5)"
    lines = [f"# terrain this art is drawn for ({src}); F2: adopt it or re-run the generator after changing it",
             "terrain:",
             "  right:   # the fault scarp; no left wall (open water)"]
    for p in RIGHT:
        lines.append(f"    - {_pt(p)}   # t={time_at(p[0]):.1f}")
    lines.append("  notches:   # side, scroll = the terrain row of the notch's middle, width along the wall, depth into it")
    for nt in NOTCHES:
        lines.append(f"    - {{side: {nt['side']}, scroll: {nt['scroll']}, width: {NOTCH_W}, depth: {NOTCH_D}}}"
                     f"   # {nt['why']}, t={time_at(nt['scroll']):.1f} at the top edge")
    lines.append("  contact: {damage: {easy: 6, medium: 10, hard: 10}, push: 28, push_seconds: 0.12, cooldown: 0.5}")
    return "\n".join(lines) + "\n"


def lights_block():
    """The static light pools the look's ``lights:`` key (F2) could carry: x, the ground row, radius, colour."""
    lines = ["# static lights this art assumes (F2's `lights:`; the pools are the game's light map, not baked):"]
    for i, row in enumerate(LAMP_ROWS):
        lx = LAMP_X - LAMP[0] / 2 + LAMP_HEAD[0]
        lines.append(f"#   lamp post {i + 1}: x {lx:.0f}, ground row {row + LAMP[1] - 1 - LAMP_BASE[1] - LAMP_HEAD[1]:.0f} "
                     f"(on the screen's top at t={time_at(row - SCREEN):.1f}), radius 90, colour ffc46e"
                     + (" (failing: its piece loops a stutter)" if i == len(LAMP_ROWS) - 1 else ""))
    lines.append("#   the two beacons: radius 40, colour ff6040 (the 4 s pulse); the farm barge's lamp (l26_props.py, t~84, "
                 "x 60): radius 80, ffc46e; the survey buoy (t=130): radius 30, ffe0a0")
    return "\n".join(lines) + "\n"


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if DATA else "the README and ART-F's terrain proposal (no data.yaml yet: re-run after F2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 26's black water (tools/art/backdrop_l26.py, step ART-F, from {src}).",
             "  # Almost all black: the ground is unlit albedo, the light map shows it. The scarp is the terrain's right",
             "  # wall (WallProjection, the ice path): ice-wall, ice-wall-glow, ice-wall-foot; cave-mouth-right is drawn by",
             "  # the game at each notch (not placed). Every piece's -glow is added over the grade on its frame clock.",
             "  scroll_factors: {far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused, given as the loader asks",
             "  ramp: 2",
             "  haze_colour: 02080e  # the black water",
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
    lines.append(f"# underwater_look (ART-F's proposal, round 55): section 1 {LOOK1}; sections 2-6 {LOOK}")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_block() + "\n" + terrain_block() + lights_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite (b25's projection, our walls)

def _bind_b25():
    """backdrop_l25's wall projection reads its module's scroll, foot and wall_height and its tile folder: point them
    at this level for this process (backdrop_l25.py itself is not changed)."""
    b25.scroll, b25.foot, b25.wall_height, b25.OUT = scroll, foot, wall_height, OUT
    b25.tile.cache_clear()


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


def ground_view(t, pieces, real):
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
        k = int(real * fps) % len(arrs) if fps else 0
        _paste_rows(img, arrs[k], x0, y_top)
        if glow is not None:
            g = glow[k % len(glow)]
            glows.append((Image.fromarray(g, "RGBA"), x0 + g.shape[1] / 2, y_top + g.shape[0] / 2, 1.0))
        if name.startswith("lamp-post"):
            lv = (DYING if name.endswith("dying") else FLICKER)[k]
            glows.append(("lamp", x0 + LAMP_HEAD[0], y_top + LAMP_HEAD[1], lv))
    return img, glows


def draw_notches(img, t):
    base = scroll(t)
    cave = np.array(Image.open(OUT / "cave-mouth-right.png").convert("RGBA"))
    for nt in NOTCHES:
        y = base - 1 - nt["scroll"]
        if -80 < y < SCREEN + 80:
            fx = float(foot(1, nt["scroll"]))
            _paste_rows(img, cave, int(round(fx - b25.CAVE_FOOT[1])), int(round(y - b25.CAVE_MID)))


def composite(t, pieces, real=None, ship=(240.0, 444.0), rook=(300.0, 470.0), solids=(), glows=(), over=(),
              ambient=None, sonar=False, lamps=()):
    """The screen at level time t in the dark (l26kit's stand-in): ground, pieces, the scarp; the light map; the
    pieces' and the wall's glows added after it."""
    _bind_b25()
    real = t if real is None else real
    img, pglows = ground_view(t, pieces, real)
    wall_glow = np.zeros((SCREEN, W, 3))
    img = b25.draw_walls(img, t, wall_glow)
    draw_notches(img, t)
    lamps = list(lamps)
    adds = []
    for g, x, y, k in pglows:
        if isinstance(g, str):
            if k > 0.05:
                lamps.append((x, y + 30, 92, 0.85 * k, kit.LAMP))
        else:
            adds.append((g, x, y, k))
    wg = Image.fromarray(np.dstack([np.clip(wall_glow, 0, 255), np.full((SCREEN, W), 255.0)]).astype(np.uint8), "RGBA")
    adds.append((wg, W / 2, SCREEN / 2, 1.0))
    amb = ambient_at(t) if ambient is None else ambient
    frame, L = kit.frame(img, list(solids), list(glows), ship, rook, ambient=amb, lamps=lamps, t=real, sonar=sonar,
                         adds=adds, over=list(over))
    return frame


# --------------------------------------------------------------------------- checks and review

def check():
    """Wraps (BackdropSeamsTest's measure), sizes, the edge rule of BackdropAssetsTest, the geometry rules this art
    assumes (width >= 180, a wall moving <= 0.5 px per px), the notches inside the field."""
    problems = []
    for name in TILES:
        t = tile(name)
        a = t[..., :3].astype(float)
        jump = np.abs(a[0] - a[-1]).mean()
        inner = np.abs(a[1:] - a[:-1]).mean()
        if jump > 3 * inner + 6:
            problems.append(f"{name}: wrap seam {jump:.1f} vs {inner:.1f}")
        if b25.wrap_score(t) > 1.5:
            problems.append(f"{name}: wrap score {b25.wrap_score(t):.2f} (BackdropSeamsTest's limit 1.5)")
        if (t[..., 3] < 255).any():
            problems.append(f"{name}: a ground tile must be opaque")
    for name, size in (("ice-wall", (b25.TEX_W, b25.TEX_PERIOD + b25.TEX_PAD)),
                       ("ice-wall-glow", (b25.TEX_W, b25.TEX_PERIOD + b25.TEX_PAD)),
                       ("ice-wall-foot", b25.b16.FOOT_TEX)):
        arr = tile(name)
        if (arr.shape[1], arr.shape[0]) != tuple(size):
            problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]}, wants {size}")
    for name, spec in meta()["pieces"].items():
        for arr in piece_frames(name):
            if arr.shape[0] > MAX_PIECE or tuple(spec["size"]) != (arr.shape[1], arr.shape[0]):
                problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]} (spec {spec['size']})")
                break
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
    rows = np.arange(0, scroll(END), 5.0)
    xr = np.nan_to_num(foot(1, rows), nan=float(W))
    if xr.min() < 280 - 0.5:
        problems.append(f"flyable width {xr.min():.0f} < 280")
    for p, q in zip(RIGHT, RIGHT[1:]):
        if abs(q[1] - p[1]) > 0.5 * (q[0] - p[0]):
            problems.append(f"right wall {p} -> {q}: steeper than 0.5")
    for nt in NOTCHES:
        fx = float(foot(1, nt["scroll"]))
        if np.isnan(fx) or fx + NOTCH_D > W:
            problems.append(f"notch {nt}: its back at x {fx + NOTCH_D:.0f} leaves the field")
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in sorted(set(problems)):
        print("  " + p)
    return problems


TIMES = [(1, "1 THE LAST BEACONS, LAMP POSTS"), (9, "1 LAMPS, AMBIENT FALLING"), (16, "1 THE LAST LAMP FAILING"),
         (40, "2 THE DARK: CONE AND SNOW"), (63.5, "3 THE SCARP, A CAVE"), (84, "3 THE SCARP AT 280 PX"),
         (101, "3 THE SCARP SINKING"), (143.5, "5 AMBER SEEP BELOW"), (172, "6 THE VENTS' RED GLOW"),
         (181, "6 THE END: THE GLOW HIGH")]


def review():
    g = placed_pieces()
    ship_x = {63.5: 330.0, 84: 300.0, 101: 300.0}
    shots = [composite(t, g, ship=(ship_x.get(t, 240.0), 444.0), rook=(ship_x.get(t, 240.0) - 70, 470.0))
             for t, _ in TIMES]
    cols = 5
    cw, ch = 240, 270
    right = 2 * (100 + 8) + 16
    sheet = raster.sheet(16 + cols * (cw + 8) + 8 + right, 60 + 2 * (ch + 24) + 330,
                         "LEVEL 26 BACKDROP - BLACKWATER (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R55")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/2 IN THE DARK LOOK'S STAND-IN (AMBIENT 20 -> 6 % OVER T=8-18, THE "
                     "70 DEG / 260 PX HEADLIGHTS, ROOK'S LAMP, THE LAMP POSTS' POOLS); THE SCARP PROJECTED (APPROX.)",
                     raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t:g} {label}"[:40], raster.LABEL_DIM)
    x0 = 16 + cols * (cw + 8) + 8
    raster.draw_text(sheet, x0, 38, "TILES 1/4, UNLIT", raster.LABEL)
    for k, name in enumerate(TILES):
        im = Image.fromarray(tile(name), "RGBA").resize((100, 200), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + k * 108, 56))
        raster.draw_text(sheet, x0 + k * 108, 56 + 204, name.upper()[:16], raster.LABEL_DIM)
    wall = Image.fromarray(tile("ice-wall")[b25.TEX_PAD:b25.TEX_PAD + 400], "RGBA").resize((64, 200),
                                                                                         Image.Resampling.BOX)
    sheet.alpha_composite(wall, (x0, 56 + 226))
    cl = Image.fromarray(piece_frames("cave-mouth-right")[0], "RGBA")
    sheet.alpha_composite(cl, (x0 + 72, 56 + 226))
    lp = Image.fromarray(piece_frames("lamp-post")[0], "RGBA")
    sheet.alpha_composite(lp, (x0 + 72, 56 + 226 + 130))
    gl = Image.fromarray(piece_frames("lamp-post-glow")[0], "RGBA")
    sheet.alpha_composite(artkit.add_light(Image.new("RGBA", gl.size, artkit.PLATE), gl), (x0 + 140, 56 + 226 + 130))
    raster.draw_text(sheet, x0, 486, "SCARP, CAVE, LAMP POST", raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24)
    raster.draw_text(sheet, 16, y, "AT 1X: LAMP POSTS ALONG THE PIPELINE (T=9), A CAVE IN THE SCARP AT THE CONE'S EDGE (T=63.5), "
                     "THE VENTS' GLOW (T=181)", raster.LABEL)
    sheet.alpha_composite(shots[1].crop((40, 120, 320, 420)), (16, y + 16))
    sheet.alpha_composite(shots[4].crop((180, 180, 480, 480)), (16 + 288, y + 16))
    sheet.alpha_composite(shots[9].crop((0, 0, 360, 300)), (16 + 288 + 308, y + 16))
    frames = [composite(t, g).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(10, 18, 0.125)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


def main(argv):
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv and (OUT / "black-water.png").exists() and PROPOSAL.exists():
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
