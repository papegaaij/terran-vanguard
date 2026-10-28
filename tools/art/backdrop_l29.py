#!/usr/bin/env python3
"""Production art: the Level 29 backdrop, Ceres Hub (design/campaign/act-5-the-belt/level-29-betrayal-at-ceres; M9 part
B batch, concept round 62, step ART-B).

Straight to production from round 60's belt look (tools/concept/belt_r60.py / enemies_r60.py: the warm sunlit rock
``WARM_ROCK``, the Coalition hull ``GREY_HULL``, the sunlit dust band of ``space_bg``) in **A.18 a**, the lighter belt
grade the user picked: harsh sun from the top left, very high contrast, but no near-black ground, so the gunmetal
units always have a lighter ground behind them. Coalition blocks grey-white under cool white lights; Helix facilities
charcoal and gold with red lights. Heightfields hill-shaded from the top-left sun with cast shadows, posterized per
image (median cut), wide gradients ordered-dithered; nothing lit is mirrored (a right-hand piece is its own render).

The layers (``scroll_factors``): ``deep`` 0.12 (space: the dust band, stars; Ceres's limb; the CDF transport at the
rendezvous), ``ground`` 1 (the hub), ``low-air`` 1.35 (loyalist ore haulers, CDF patrols and Talons: traffic on paths).

Tile sets (480 wide, wrapping; a tile's pixel row is the layer row mod its height, so every piece cut from the same
field runs on seamlessly):
  belt-space    deep 480x1024: black space, the warm sunlit dust band across it, stars, a few far rocks (all sections)
  dock-canyon   ground 480x960: the dock canyon cut into Ceres's crust: the crust's sunlit regolith and hull plating
                on both rims (x < ~100, > ~380), the left wall in shadow, the right wall lit, the canyon floor's deck
                plating with its guide lights, the left rim's shadow across it, small cool-white rim lamps (sections 2-6)
  outer-docks   ground 480x960, transparent: the outer docks' girder frames and berth trusses over open space (section 7)
Pieces (ground unless named; exact rows; ``-glow`` additive, drawn by the game at the piece's placements):
  crust-edge             480x640: the crust's ragged sunlit edge where the hub begins, the canyon's mouth opening into
                         space; its top rows are the canyon tile's own (covers the seam into section 2)
  docking-arm-1, -2      112x420 / 112x360: girder arms reaching out from the crust edge, a berthed hauler on one
  dock-1 .. dock-12      96x128 (left wall) / -r 96x128 (right wall): a berth cut into the canyon wall, its number
                         stencilled big, a white work lamp (+ -glow)
  helix-block (+ -glow)  208x176 at t~34, x 110: Helix's dock block on the left rim, charcoal and gold, red lights; the
                         roof's two nest pads (x 90, 140)
  helix-pad (+ -glow)    96x96 at t~34, x 400: a Helix turret pad on the right rim (the third nest)
  coalition-block-1..4 (+ -glow)  128x144: grey-white Coalition modules on the rims, window strips, white lamps
  block-c (+ -glow, -glow-red)  480x224 at t~60: Block C bridging the canyon, its roof's three dormant nests' pads
                         (x 120, 240, 360); -glow its white lamps and their pools, -glow-red the same lamps red: the game
                         draws -glow until the turn (t=60) and crossfades to -glow-red over 1 s (B4)
  red-block-d (+ -glow)  208x176 at t~84, x 100: a Coalition block turned red (nests x 70, 120), its lamps red
  red-pad (+ -glow)      96x96 at t~84, x 410: a turned turret pad (the third nest)
  red-block-e (+ -glow)  480x200 at t~100: a bridging block turned red (nests x 130, 240, 350)
  dock-6 (+ -glow)       160x208 at t~108, x 420: Dock 6, a crane gantry over a locker (the secret's place: the
                         locker's door is the ground trigger's look, x 430)
  dock-9 (+ -glow)       480x540: Dock 9 filling the halted screen: the berth's cradle, its rails and work lamps, the
                         four clamp seats (the clamp guns' sprites stand on them: x 150 / 330, y 170 / 330)
  prison-tender          128x288: Helix's prison tender in the berth, the halted screen's middle third (centre 240,
                         250): a charcoal hull with gold bands, the hold's two doors shut
  prison-tender-hold     128x288, 6 frames (not placed: the game draws them over prison-tender when the last clamp
                         falls, 4 steps a frame, the last held): the hold's doors sliding apart, the lit hold below
  canyon-mouth           480x1280: the canyon running on out of the crust into the breakout (its lower rows the canyon
                         tile's own: covers the seam into outer-docks), the crust ending in a ragged lit edge
  outer-dock-1 .. -3     144x112: the outer docks' berth platforms (the breakout's nests, t~131: x 90, 240, 390)
  outer-ring             480x240: the hub's outer ring, a truss band across the screen with its beacons; its lower
                         edge on the bottom edge at t=172 (the level's end)
  ceres-limb             deep 480x760: Ceres's sunlit limb on the right, cratered, the night side dark (lens: the top
                         and bottom rows clear)
  cdf-transport          deep 112x176: the CDF transport waiting at the rendezvous, its running lights
  ore-hauler             low-air 64x64, 16 headings: a loyalist ore hauler (industrial yellow-grey, ochre ore boxes)
  cdf-patrol             low-air 56x56, 16 headings: two CDF Stormhawks in echelon (the CDF grey and blue)
  talon-pair             low-air 48x48, 16 headings: two Talons (gunmetal and gold) in echelon
Review: design/campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/backdrop-final-r62-a.png (the tile sets, the
pieces, eight screens with stand-ins for the nests, clamp guns and the ship) and .gif (t 54-66 at half size: Block C
turns red; then the halted Dock 9). ``--proposal`` writes the ``backdrop:`` block, the sections and the ground targets'
places to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections (the rescue
halt an arena section of speed 0, as Level 28's). Re-run after the data moves a section.
Run: python3 tools/art/backdrop_l29.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r03 as e3  # noqa: E402  (concept scripts, imported unchanged)
import enemies_r60 as e60  # noqa: E402
from render import models, raster  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GOLD, A_HULL, hx  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, union  # noqa: E402

SCRIPT = "backdrop_l29.py"
BATCH = "M9 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r62"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-29-betrayal-at-ceres"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-29"
W, SCREEN, TILE_H, SPACE_H = 480, 540, 960, 1024
OUTRO = 15
FACTORS = {"deep": 0.12, "ground": 1.0, "low-air": 1.35}
MAX_PIECE = 2040
SUN = np.array((-0.55, -0.6, 0.62))
SUN = SUN / np.linalg.norm(SUN)
WHITE_LAMP = np.array((235, 244, 255), float)
RED_LAMP = np.array((255, 52, 40), float)
GOLD = np.array((236, 186, 72), float)
WARM_ROCK = [(k, raster.hexrgb(c)) for k, c in e60.WARM_ROCK]
GREY_HULL = [(k, raster.hexrgb(c)) for k, c in e60.GREY_HULL]
CHARCOAL = [(0.0, (16, 16, 20)), (0.35, (40, 40, 46)), (0.7, (72, 72, 80)), (1.0, (120, 118, 122))]

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
PROPOSED_SECTIONS = [
    {"name": "Ceres Approach", "end": 28, "tiles": ["belt-space"]},
    {"name": "The Dock Canyon", "end": 56, "speed": 100, "ramp": 2, "tiles": ["belt-space", "dock-canyon"]},
    {"name": "Block C", "end": 76, "speed": 60, "ramp": 2, "tiles": ["belt-space", "dock-canyon"]},
    {"name": "The Red Canyon", "end": 116, "speed": 100, "ramp": 2, "tiles": ["belt-space", "dock-canyon"]},
    {"name": "Dock 9", "end": 120, "speed": 100, "tiles": ["belt-space", "dock-canyon"]},
    {"name": "The Rescue", "end": 121, "speed": 0, "arena": True, "tiles": ["belt-space", "dock-canyon"]},
    {"name": "Breakout", "end": 172, "speed": 120, "ramp": 2, "tiles": ["belt-space", "outer-docks"]},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speed(i):
    s = SECTIONS[i]
    return float(s["speed"]) if s.get("speed") is not None else BASE_SPEED


def _arena(i):
    return bool(SECTIONS[i].get("arena"))


def _timed_halt(i):
    return _speed(i) == 0 and not _arena(i)


def _ease(i):
    if _timed_halt(i) or _speed(i) == 0 or (_arena(i) and SECTIONS[i].get("speed") is None):
        return 0.0
    return float(SECTIONS[i].get("ramp", 0) or 0)


def _u(t, start, ramp):
    return min(max((t - start) / ramp, 0.0), 1.0) if ramp > 0 else (1.0 if t >= start else 0.0)


def _ease_in(u):
    return u ** 3 - u ** 4 / 2


def _ease_out(u):
    return u - u ** 3 + u ** 4 / 2


def _held_arena(i):
    """A held arena (its ``holds`` group, step B2's data): the scroll eases down across it from the speed before."""
    return _arena(i) and _speed(i) == 0 and bool(SECTIONS[i].get("holds")) and i > 0


def _travelled(i, a, b):
    speed, start, end = _speed(i), STARTS[i], float(SECTIONS[i]["end"])
    px = (b - a) * speed
    if _held_arena(i):  # content LevelScroll (M9 part B): before x (1 - S(u)) over the whole section
        span = end - start
        return _speed(i - 1) * span * (_ease_out(_u(b, start, span)) - _ease_out(_u(a, start, span)))
    if speed == 0:
        return px
    if i + 1 < len(SECTIONS) and _timed_halt(i + 1):
        ramp = min(float(SECTIONS[i + 1].get("ramp", 2)), end - start)
        px -= speed * ramp * (_ease_in(_u(b, end - ramp, ramp)) - _ease_in(_u(a, end - ramp, ramp)))
    if i > 0 and _timed_halt(i - 1):
        ramp = min(float(SECTIONS[i - 1].get("ramp", 2)), end - start)
        px -= speed * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    elif i > 0 and _ease(i) > 0:
        ramp = min(_ease(i), end - start)
        px -= (speed - _speed(i - 1)) * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    return px


def scroll(t):
    """LevelData.scrollAt: the ground layer's scroll at level time t, px."""
    s = 0.0
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]) or i == len(SECTIONS) - 1:
            return s + _travelled(i, STARTS[i], t)
        s += _travelled(i, STARTS[i], float(sec["end"]))
    raise AssertionError


def time_at(s):
    lo, hi = -40.0, END + 60
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll(mid) < s:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def seam_row(index):
    return scroll(STARTS[index]) + SCREEN


def exact_t(bottom, h, factor=1.0):
    """The placement time putting a piece h px tall with its bottom row on layer row ``bottom`` (centre = scroll(t) x
    factor + 270)."""
    t = time_at((bottom + h / 2 - SCREEN / 2) / factor)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) * factor + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def row_at(t, y_down):
    """The ground row on screen y (down) at level time t."""
    return int(round(scroll(t))) + SCREEN - 1 - int(y_down)


def section_index(name):
    return next(i for i, s in enumerate(SECTIONS) if s["name"] == name)


ARENA_INDEX = next(i for i in range(len(SECTIONS)) if _arena(i))
ARENA_T = float(SECTIONS[ARENA_INDEX]["end"]) if _held_arena(ARENA_INDEX) else STARTS[ARENA_INDEX]   # the halt
ARENA_SCROLL = int(round(scroll(ARENA_T)))            # the halted screen (a held arena: at its end): ground rows ARENA_SCROLL .. +539
CANYON_SEAM = int(round(seam_row(1)))                 # the canyon tile begins (section 2)
BREAK_SEAM = int(round(seam_row(ARENA_INDEX + 1)))    # the outer docks begin (the breakout)
END_ROW = int(round(scroll(END)))                     # the bottom edge's row at the level's end

# =========================================================================== the places (README, ART-B's proposal)

# Ground targets (kind, t entering at the top edge, x). README *Ground targets*; x where the README leaves it to the
# build: ART-B's proposal, drawn under them (pads on the blocks).
TARGETS = ([("sam-nest", 34, x) for x in (90, 140, 400)]
           + [("sam-nest (dormant until 60)", None, x) for x in (120, 240, 360)]
           + [("sam-nest", 84, x) for x in (70, 120, 410)]
           + [("sam-nest", 100, x) for x in (130, 240, 350)]
           + [("supply-cache (trigger, 20 HP)", 108, 430)]
           + [("sam-nest", 131, x) for x in (90, 240, 390)])
BLOCK_C_ROW = row_at(60, 160)                          # Block C's nests at the turn: 160 px below the top edge
CLAMPS = [(150, 170), (330, 170), (150, 330), (330, 330)]
TENDER = (240, 250)
NEST = 44


def target_row(t, h=NEST):
    """The ground row of the centre of a target h px tall entering at the top edge at t."""
    return int(round(scroll(t))) + SCREEN + h // 2


# =========================================================================== fields and shading

def periodic_noise(w, h, cell, seed, octaves=4):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=True)


def shade(hgt, albedo, k=1.0, ambient=0.42, shadow_len=70):
    """Hill-shade a heightfield (px units) under the top-left sun with cast shadows; rows wrap (tiles)."""
    gy = (np.roll(hgt, -1, axis=0) - np.roll(hgt, 1, axis=0)) / 2          # rows wrap (tiles)
    gx = np.gradient(hgt, axis=1)
    n = np.dstack([-gx * k, -gy * k, np.ones_like(hgt)])
    n /= np.linalg.norm(n, axis=-1, keepdims=True)
    diff = np.clip(n @ SUN, 0, 1)
    lit = np.ones_like(hgt)
    sx, sy, sz = -SUN[0] / np.hypot(SUN[0], SUN[1]), -SUN[1] / np.hypot(SUN[0], SUN[1]), \
        SUN[2] / np.hypot(SUN[0], SUN[1])
    for s in range(2, shadow_len, 2):
        dx, dy = int(round(-sx * s)), int(round(-sy * s))
        shifted = np.roll(np.roll(hgt, dy, axis=0), dx, axis=1)
        if dx > 0:
            shifted[:, :dx] = hgt[:, :1]
        lit = np.minimum(lit, np.where(shifted > hgt + s * sz, 0.0, 1.0))
    light = ambient + (1 - ambient) * diff * (0.35 + 0.65 * lit)
    return albedo * light[..., None]


def walls(rows):
    """The canyon's rim and floor edges (x) per ground row (periodic over the tile height)."""
    r = np.asarray(rows, float)
    p = TAU * r / TILE_H
    left = 96 + 8 * np.sin(2 * p + 0.6) + 5 * np.sin(5 * p + 1.3)
    right = 384 + 7 * np.sin(2 * p + 2.1) + 5 * np.sin(4 * p + 0.2)
    return left, left + 30, right - 30, right


def canyon_field(seed=2901):
    """(rgb float, height) of the canyon tile, image rows top = highest row (row index i <-> ground row 959 - i)."""
    h, w = TILE_H, W
    rows = TILE_H - 1 - np.arange(h)
    xx = np.arange(w)[None, :].repeat(h, 0).astype(float)
    lo, lf, rf, ro = (a[:, None] for a in walls(rows))
    crust = periodic_noise(w, h, 64, seed) * 0.6 + periodic_noise(w, h, 16, seed + 7) * 0.4
    fine = periodic_noise(w, h, 4, seed + 11, octaves=2)
    rng = np.random.default_rng(seed)
    hgt = np.zeros((h, w))
    rim_l = (xx < lo)
    rim_r = (xx > ro)
    wall_l = (xx >= lo) & (xx < lf)
    wall_r = (xx > rf) & (xx <= ro)
    floor = ~(rim_l | rim_r | wall_l | wall_r)
    depth = 46.0
    hgt = np.where(rim_l | rim_r, depth + crust * 10, hgt)
    hgt = np.where(wall_l, depth * (1 - (xx - lo) / (lf - lo)) + crust * 4, hgt)
    hgt = np.where(wall_r, depth * ((xx - rf) / (ro - rf)) + crust * 4, hgt)
    # craters on the rims
    yy = np.arange(h)[:, None].repeat(w, 1).astype(float)
    for _ in range(26):
        cx, cy, cr = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(5, 22)
        for oy in (-h, 0, h):
            d = np.hypot(xx - cx, yy - cy - oy) / cr
            bowl = np.where(d < 1, -(1 - d * d) * 0.45 * cr, 0) + np.exp(-((d - 1) / 0.2) ** 2) * 0.18 * cr
            hgt = hgt + np.where(rim_l | rim_r, bowl, 0)
    # the floor's deck plating: panels with grooves, a raised central guide strip; cargo stacks and pipe runs
    panel = ((xx.astype(int) - 120) % 40 < 1) | ((yy.astype(int) + 24) % 48 < 1)        # no groove on the wrap
    hgt = np.where(floor & panel, hgt - 1.2, hgt)
    guide = floor & (np.abs(xx - (lf + rf) / 2) < 3)
    hgt = np.where(guide, hgt + 0.8, hgt)
    pipe = floor & ((np.abs(xx - lf - 7) < 3) | (np.abs(xx - rf + 7) < 3))
    hgt = np.where(pipe, hgt + 3 * np.sqrt(np.clip(1 - ((np.minimum(np.abs(xx - lf - 7), np.abs(xx - rf + 7))) / 3) ** 2,
                                                   0, 1)), hgt)
    cargo = np.zeros((h, w), bool)
    cargo_col = np.zeros((h, w))
    for k in range(14):
        side = 1 if k % 2 else -1
        cy = rng.uniform(0, h)
        cw, ch = rng.choice([14, 18, 22]), rng.choice([24, 36, 48])
        for oy in (-h, 0, h):
            rowc = int(cy) % h
            cxk = (lf[rowc, 0] + 24 + cw / 2) if side < 0 else (rf[rowc, 0] - 24 - cw / 2)
            m = (np.abs(xx - cxk) < cw / 2) & (np.abs(yy - cy - oy) < ch / 2) & floor
            cargo |= m
            cargo_col = np.where(m, rng.uniform(0, 1), cargo_col)
    hgt = np.where(cargo, hgt + 9 + (((yy.astype(int) + 6) % 12) == 0) * -1.0, hgt)
    tanks = np.zeros((h, w), bool)
    for _ in range(10):
        cx, cy, cr = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(7, 13)
        for oy in (-h, 0, h):
            d = np.hypot(xx - cx, yy - cy - oy) / cr
            m = (d < 1) & ((xx < lo - 18) | (xx > ro + 18))
            tanks |= m
            hgt = np.where(m, np.maximum(hgt, depth + 10 + 10 * np.sqrt(np.clip(1 - d * d, 0, 1))), hgt)
    # rim plating patches near the rims (Coalition hull on the crust)
    plate = np.zeros((h, w), bool)
    for _ in range(18):
        px0, py0 = rng.uniform(0, w), rng.uniform(0, h)
        pw, ph = rng.uniform(24, 60), rng.uniform(30, 90)
        for oy in (-h, 0, h):
            plate |= (np.abs(xx - px0) < pw / 2) & (np.abs(yy - py0 - oy) < ph / 2)
    plate &= (rim_l & (xx > lo - 46)) | (rim_r & (xx < ro + 46))
    hgt = np.where(plate, depth + 12 + ((xx.astype(int) % 12 == 0) | ((yy.astype(int) + 8) % 16 == 0)) * -1.0, hgt)
    # albedo
    rock = raster.ramp(WARM_ROCK, np.clip(0.42 + (crust - 0.5) * 0.55 + (fine - 0.5) * 0.25, 0, 1))
    wallc = raster.ramp(WARM_ROCK, np.clip(0.36 + (crust - 0.5) * 0.4 + 0.08 * np.sin(yy * TAU * 53 / TILE_H + xx * 0.2), 0, 1))
    pv = np.sin((xx // 40) * 12.9898 + ((yy + 24) // 48 % (TILE_H // 48)) * 78.233) * 43758.5453
    pv = pv - np.floor(pv)
    deck = raster.ramp(GREY_HULL, np.clip(0.46 + (fine - 0.5) * 0.15 + 0.06 * pv, 0, 1))
    crate = raster.ramp([(0.0, (120, 92, 50)), (0.5, (150, 128, 96)), (1.0, (96, 104, 112))], cargo_col)
    hull = raster.ramp(GREY_HULL, np.clip(0.62 + (fine - 0.5) * 0.12, 0, 1))
    alb = np.where((rim_l | rim_r)[..., None], rock, deck)
    alb = np.where((wall_l | wall_r)[..., None], wallc, alb)
    alb = np.where(plate[..., None], hull, alb)
    alb = np.where(cargo[..., None], crate, alb)
    alb = np.where(pipe[..., None], np.array((120, 124, 130.0)), alb)
    alb = np.where(tanks[..., None], hull * 1.08, alb)
    alb = np.where(guide[..., None], np.array((150, 140, 110.0)), alb)
    rgb = shade(hgt, alb / 255.0, k=0.9) * 255 * 1.08
    # guide lights on the floor's strip and the rims' small cool-white lamps
    for y in range(24, h, 48):
        cx = int(round(float(((lf + rf) / 2)[y, 0])))
        rgb[y:y + 2, cx - 1:cx + 1] = (255, 214, 140)
    for y in range(16, h, 96):
        for x in (int(lo[y, 0]) - 6, int(ro[y, 0]) + 5):
            rgb[y:y + 2, x:x + 2] = WHITE_LAMP
    return rgb, hgt


def posterize(rgb, colours=64, alpha=None):
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    img = img.quantize(colours, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert("RGBA")
    if alpha is not None:
        a = np.array(img)
        a[..., 3] = alpha
        a[alpha == 0] = 0
        img = Image.fromarray(a, "RGBA")
    return img


_CANYON = {}


def canyon():
    if "rgb" not in _CANYON:
        _CANYON["rgb"], _CANYON["hgt"] = canyon_field()
        _CANYON["img"] = np.array(posterize(_CANYON["rgb"], 72))
    return _CANYON


def canyon_rows(bottom, h):
    """The canyon tile's pixels for ground rows bottom .. bottom+h-1, as an image (top = highest row)."""
    tile = canyon()["img"]
    rows = bottom + h - 1 - np.arange(h)
    return tile[TILE_H - 1 - (rows % TILE_H)].copy()


def space_tile(seed=2902):
    """The deep layer: black space, the sunlit warm dust band crossing it, stars, far rocks (wraps)."""
    h, w = SPACE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    p = TAU * yy / h
    band_c = 0.5 * h + 120 * np.sin(p) + (xx - 240) * 0.55
    band = np.exp(-(((yy - band_c + h / 2) % h - h / 2) / 150) ** 2)
    neb = periodic_noise(w, h, 64, seed) * 0.6 + periodic_noise(w, h, 16, seed + 3) * 0.4
    dust = band * (0.55 + 0.6 * (neb - 0.5))
    rgb = np.zeros((h, w, 3)) + np.array((6, 6, 10.0))
    rgb += np.clip(dust, 0, 1)[..., None] * np.array((92, 72, 50.0))
    q = np.dstack([artkit.ordered_dither(np.clip(rgb[..., c] / 110, 0, 1), 14) for c in range(3)]) * 110
    rng = np.random.default_rng(seed)
    for _ in range(420):
        x, y = rng.integers(0, w), rng.integers(0, h)
        v = rng.uniform(80, 255)
        q[y, x] = (v, v * 0.97, v * 0.92)
    for _ in range(9):
        x, y, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(1.5, 4)
        d = np.hypot(xx - x, ((yy - y + h / 2) % h) - h / 2)
        m = d < r
        lit = np.clip(0.4 - ((xx - x) + (yy - y)) / (2 * r), 0.15, 1)
        q[m] = np.array((120, 104, 86.0)) * lit[m, None]
    return Image.fromarray(np.clip(q, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def outer_docks_tile(seed=2903):
    """The outer docks over open space: two long girder rails down the rims and berth trusses across (transparent
    between); wraps."""
    h, w = TILE_H, W
    img = Image.new("RGBA", (w, h))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)

    def girder(x0, y0, x1, y1, width):
        d.rectangle([x0, y0, x1, y1], fill=(98, 104, 112, 255))
        d.rectangle([x0, y0, x0 + 1, y1], fill=(196, 202, 206, 255))           # lit left edge
        d.rectangle([x1 - 1, y0, x1, y1], fill=(40, 42, 48, 255))
        step = max(8, width)
        if y1 - y0 > x1 - x0:
            for y in range(y0, y1, step):
                d.line([(x0 + 2, y), (x1 - 2, y + step)], fill=(64, 68, 76, 255))
        else:
            for x in range(x0, x1, step):
                d.line([(x, y0 + 2), (x + step, y1 - 2)], fill=(64, 68, 76, 255))
    for x0 in (60, 404):
        girder(x0, 0, x0 + 16, h - 1, 16)
    for y in range(40, h, 240):
        girder(76, y, 404, y + 10, 10)
        for x in (150, 330):
            girder(x - 5, y + 10, x + 5, y + 70 + int(rng.integers(0, 40)), 10)
    a = np.array(img)
    for y in range(20, h, 120):                                               # beacons on the rails
        for x in (68, 412):
            a[y:y + 2, x:x + 2] = (255, 210, 120, 255)
    return Image.fromarray(a, "RGBA")


# =========================================================================== lit pieces (structures)

def box_relief(w, h, boxes, base=None):
    """A height map and material index of boxes: (x0, y0, x1, y1, height, material, bevel)."""
    hgt = np.zeros((h, w)) if base is None else base.copy()
    mat = np.zeros((h, w), int)
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    for x0, y0, x1, y1, z, m, bev in boxes:
        inside = (xx >= x0) & (xx <= x1) & (yy >= y0) & (yy <= y1)
        edge = np.minimum.reduce([xx - x0, x1 - xx, yy - y0, y1 - yy])
        top = z * np.clip(edge / max(bev, 1e-6), 0, 1) if bev else np.full_like(xx, z)
        hgt = np.where(inside, np.maximum(hgt, top), hgt)
        mat = np.where(inside & (top >= hgt - 1e-6), m, mat)
    return hgt, mat


MATS = {1: GREY_HULL, 2: CHARCOAL, 3: [(0.0, (60, 40, 10)), (0.5, (170, 120, 40)), (1.0, (255, 214, 120))],
        4: [(0.0, (30, 32, 38)), (1.0, (90, 96, 110))], 5: [(0.0, (60, 50, 20)), (1.0, (220, 180, 60))]}


def structure(w, h, boxes, ground=None, alpha=None, seed=7, windows=(), colours=48):
    """A lit piece: boxes on a ground (an rgb array of the same size, e.g. the canyon's rows), hill-shaded with cast
    shadows onto the ground; ``windows`` (x0, y0, x1, y1) lit strips."""
    base_h = np.zeros((h, w))
    hgt, mat = box_relief(w, h, boxes, base_h)
    noise = raster.fbm(w, h, 8, seed, octaves=2, period=False)
    alb = np.zeros((h, w, 3))
    for m, stops in MATS.items():
        sel = mat == m
        if sel.any():
            alb[sel] = raster.ramp(stops, np.clip(0.55 + (noise[sel] - 0.5) * 0.25, 0, 1))
    lit = shade(hgt + (ground is not None) * 0, alb / 255.0, k=1.2, ambient=0.4, shadow_len=40) * 255
    if ground is not None:
        g = ground[..., :3].astype(float)
        sh = shade(hgt, np.ones((h, w, 3)), k=0.0, ambient=0.55, shadow_len=40)[..., 0]
        sh = sh / sh.max()
        rgb = np.where((mat > 0)[..., None], lit, g * sh[..., None])
    else:
        rgb = lit
    for x0, y0, x1, y1 in windows:
        rgb[y0:y1, x0:x1] = (210, 222, 196)
    a = np.where(mat > 0, 255, 0) if alpha is None else alpha
    if ground is not None and alpha is None:
        a = np.full((h, w), 255)
    return posterize(rgb, colours, a.astype(np.uint8)), mat


def lamp_glow(w, h, lamps, col, pool=0.0):
    """Additive lamps (x, y, r) with an optional soft pool round each, ordered-dithered steps."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    f = np.zeros((h, w))
    for x, y, r in lamps:
        d = np.hypot(xx - x, yy - y)
        f = np.maximum(f, np.exp(-(d / r) ** 2) + pool * np.exp(-(d / (r * 6)) ** 2) * 0.35)
    q = artkit.ordered_dither(np.clip(f, 0, 1), 8)
    rgb = np.asarray(col, float)[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)])
                                           .astype(np.uint8), "RGBA"))


def coalition_block(i, w=128, h=144):
    rng = np.random.default_rng(40 + i)
    boxes = [(6, 8, w - 8, h - 10, 30, 1, 3), (20, 24, w - 24, 60, 38, 1, 2), (w - 40, h - 46, w - 18, h - 22, 44, 4, 2)]
    wins = [(12, y, w - 14, y + 2) for y in range(70, h - 16, 14) if rng.random() < 0.8]
    img, _ = structure(w, h, boxes, windows=wins, seed=50 + i)
    lamps = [(10, 12, 2.2), (w - 12, 12, 2.2), (10, h - 14, 2.2), (w - 12, h - 14, 2.2)]
    return img, lamps


def helix_block(w=208, h=176, pads=(90 - 110 + 104, 140 - 110 + 104)):
    boxes = [(4, 6, w - 6, h - 8, 34, 2, 4), (8, 10, w - 10, 18, 40, 3, 1), (8, h - 20, w - 10, h - 12, 40, 3, 1)]
    for px in pads:
        boxes.append((px - 24, h // 2 - 24, px + 24, h // 2 + 24, 38, 4, 3))
    boxes.append((w - 46, 30, w - 16, h - 34, 46, 2, 3))
    img, _ = structure(w, h, boxes, seed=61)
    lamps = [(14, 14, 2.4), (w - 14, 14, 2.4), (14, h - 16, 2.4), (w - 14, h - 16, 2.4), (w - 31, 34, 2.0)]
    return img, lamps


def pad(w=96, h=96, mat=2, trim=3):
    boxes = [(6, 6, w - 6, h - 6, 26, mat, 4), (14, 14, w - 14, h - 14, 30, 4, 3)]
    boxes += [(8, 8, w - 8, 12, 30, trim, 1)]
    img, _ = structure(w, h, boxes, seed=71 + mat)
    return img, [(10, 10, 2.2), (w - 10, 10, 2.2), (10, h - 10, 2.2), (w - 10, h - 10, 2.2)]


def bridge_block(bottom, h, nests, seed=81):
    """A block bridging the canyon (480 wide) over the canyon's own rows: its deck, its towers on the rims, the nests'
    pads on its roof."""
    ground = canyon_rows(bottom, h)
    boxes = [(0, 26, W - 1, h - 26, 32, 1, 3), (0, 18, 70, h - 18, 44, 1, 4), (W - 71, 18, W - 1, h - 18, 44, 1, 4)]
    for x in nests:
        boxes.append((x - 26, h // 2 - 26, x + 26, h // 2 + 26, 40, 4, 3))
    wins = [(78, y, W - 78, y + 2) for y in (34, h - 36)]
    img, _ = structure(W, h, boxes, ground=ground, windows=wins, seed=seed)
    lamps = [(x, y, 2.4) for x in range(30, W, 60) for y in (30, h - 30)]
    return img, lamps


def dock_bay(n, side):
    """A berth cut into a canyon wall (96x128): a dark recess with a cradle, its number stencilled big."""
    w, h = 96, 128
    hgt, mat = box_relief(w, h, [(0, 0, w - 1, h - 1, 30, 4, 4)])
    hgt[12:h - 12, 8:w - 8] = 4
    mat[12:h - 12, 8:w - 8] = 2
    hgt[34:h - 34, 26:w - 26] = 10
    mat[34:h - 34, 26:w - 26] = 1
    alb = np.zeros((h, w, 3))
    for m, stops in MATS.items():
        alb[mat == m] = raster.ramp(stops, np.full((mat == m).sum(), 0.55))
    rgb = shade(hgt, alb / 255, k=1.0, ambient=0.4, shadow_len=30) * 255
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(img)
    txt = str(n)
    tw = raster.text_width(txt, scale=4)
    raster.draw_text(img, (w - tw) // 2, h - 30, txt, (236, 232, 214), scale=4)
    d.rectangle([4, 4, w - 5, 7], fill=(210, 170, 70, 255))
    d.rectangle([0, 0, w - 1, h - 1], outline=(150, 158, 166, 255), width=2)
    d.line([(1, 1), (w - 2, 1)], fill=(214, 220, 224, 255))
    d.line([(1, 1), (1, h - 2)], fill=(214, 220, 224, 255))
    a = np.array(img)
    return posterize(a[..., :3].astype(float), 40, np.full((h, w), 255, np.uint8)), [(w // 2, 9, 2.4)]


def dock6(w=160, h=208):
    boxes = [(10, 10, w - 10, h - 10, 6, 1, 2), (16, 20, 28, h - 20, 52, 4, 2), (w - 28, 20, w - 16, h - 20, 52, 4, 2),
             (16, 20, w - 16, 34, 56, 5, 2), (w // 2 - 4 + 10, 60, w // 2 + 30 + 10, 110, 20, 2, 2)]
    img, _ = structure(w, h, boxes, seed=91)
    return img, [(22, 26, 2.2), (w - 22, 26, 2.2), (w // 2 + 27, 64, 1.8)]


def dock9():
    """Dock 9 over the halted screen's rows: the canyon's rows with the berth cradle, rails and clamp seats."""
    h = SCREEN
    bottom = ARENA_SCROLL
    ground = canyon_rows(bottom, h)
    boxes = [(170, 90, 310, 410, 8, 4, 3)]
    for x, y in CLAMPS:
        boxes.append((x - 34, y - 26, x + 34, y + 26, 22, 2, 4))
        boxes.append((x - 22, y - 16, x + 22, y + 16, 26, 4, 3))
    for y in (110, 390):
        boxes.append((120, y - 4, 360, y + 4, 12, 5, 1))
    img, _ = structure(W, h, boxes, ground=ground, seed=99)
    lamps = [(x, y, 2.6) for x, y in ((176, 96), (304, 96), (176, 404), (304, 404))] + \
            [(x + s * 30, y, 2.0) for x, y in CLAMPS for s in (-1, 1)]
    return img, lamps


def tender_model(hold=0.0):
    """Helix's prison tender (+Y forward = up the screen): a long charcoal hull with gold bands, a blunt bow, two
    drive bells aft, the hold's two doors amidships (``hold`` slides them apart), a lit hold under them."""
    def scene(p):
        items = [(sd_box(p, (0, 0.0, 0.3), (1.2, 2.7, 0.3), 0.25), A_HULL),
                 (sd_box(p, (0, 2.6, 0.28), (0.9, 0.5, 0.26), 0.3), A_FACET),
                 (sd_box(p, (0, 1.55, 0.62), (1.22, 0.07, 0.04)), A_GOLD),
                 (sd_box(p, (0, -1.55, 0.62), (1.22, 0.07, 0.04)), A_GOLD),
                 (sd_box(p, (0, 0.0, 0.5), (0.95, 1.3, 0.08), 0.04), 7),
                 (sd_cylinder_z(p, (-0.55, -2.85, 0.3), 0.32, 0.25), A_ENGINE),
                 (sd_cylinder_z(p, (0.55, -2.85, 0.3), 0.32, 0.25), A_ENGINE),
                 (sd_box(p, (0, 2.0, 0.66), (0.35, 0.3, 0.06), 0.03), A_FACET)]
        for s in (-1, 1):
            off = s * hold * 0.9
            items.append((sd_box(p, (s * 0.47 + off, 0.0, 0.62), (0.46, 1.25, 0.04), 0.02), A_HULL))
        return union(*items)
    mats = e60.belt_mats("3a3e46")
    mats[A_HULL] = Material(hx("3c3e46"), metal=0.6, shininess=90, spec=0.9)
    mats[A_FACET] = Material(hx("2c2e34"), metal=0.6, shininess=80, spec=0.8)
    mats[A_ENGINE] = Material((0.05, 0.05, 0.06), emission=(0.25, 0.12, 0.05))
    hold_light = Material((0.2, 0.2, 0.2), emission=(0.95, 0.9, 0.78))
    return scene, list(mats) + [hold_light]


def _render_tender(hold):
    scene, mats = tender_model(hold)
    hi, f = artkit.render_hi(scene, mats, (128, 288), 128 / 40.0)
    return artkit.native(hi, f, 70)


def hauler_model():
    """A loyalist ore hauler (+Y forward): a cab, a spine with ochre ore boxes lashed on, two drive bells."""
    def scene(p):
        items = [(sd_box(p, (0, 1.1, 0.25), (0.34, 0.32, 0.22), 0.08), A_HULL),
                 (sd_box(p, (0, 0.0, 0.12), (0.12, 1.2, 0.1)), A_FACET),
                 (sd_cylinder_z(p, (-0.2, -1.25, 0.18), 0.17, 0.15), A_ENGINE),
                 (sd_cylinder_z(p, (0.2, -1.25, 0.18), 0.17, 0.15), A_ENGINE)]
        for y in (0.45, 0.0, -0.45, -0.9):
            items.append((sd_box(p, (0, y, 0.24), (0.38, 0.18, 0.16), 0.03), A_GOLD))
        return union(*items)
    mats = [Material((0.7, 0.66, 0.42), metal=0.3, shininess=40, spec=0.5),
            Material((0.36, 0.36, 0.38), metal=0.5, shininess=60, spec=0.6),
            Material((0.62, 0.42, 0.2), metal=0.1, shininess=20, spec=0.3),
            Material((0.1, 0.1, 0.1)), Material((0.05, 0.08, 0.1), emission=(0.5, 0.8, 1.0)),
            Material((0.2, 0.2, 0.2)), Material((0.2, 0.2, 0.2))]
    return scene, mats


def pair_model(make, offsets, scale):
    scene0, mats = make()

    def scene(p):
        best = None
        for ox, oy in offsets:
            q = (p - np.array((ox, oy, 0.0))) / scale
            d, m = scene0(q)
            d = d * scale
            if best is None:
                best = (d, m)
            else:
                best = (np.minimum(best[0], d), np.where(d < best[0], m, best[1]))
        return best
    return scene, mats


def _cdf_model():
    return models.ship_c_model(bank=0.0)


def _talon_model():
    import belt_units  # noqa: E402  (tools/art: the Talon's gunmetal livery)
    scene, mats = e60.talon_model("b", False)
    return scene, belt_units.gunmetal(mats)


TRAFFIC = {"ore-hauler": (64, lambda: hauler_model(), 64 / 22.0),
           "cdf-patrol": (56, lambda: pair_model(_cdf_model, [(-0.42, 0.3), (0.42, -0.3)], 0.42), 56 / 24.0),
           "talon-pair": (48, lambda: pair_model(_talon_model, [(-0.36, 0.28), (0.36, -0.28)], 0.4), 48 / 20.0)}


def _render_traffic(job):
    name, k = job
    size, make, extent = TRAFFIC[name]
    scene, mats = make()
    rot = model_rotation(np.pi / 2 + TAU * k / 16)
    sc = (lambda p: scene(rotate_z(p, rot)))
    hi, f = artkit.render_hi(sc, model_space_materials(mats, rot), (size, size), extent)
    return artkit.native(hi, f, 60)


def ceres_limb(w=W, h=760, seed=2905):
    """Ceres's sunlit limb on the right: a cratered disc (centre right of the piece), lit from the top left, its
    night side dark; a lens touching only the right edge."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    cx, cy, R = 760.0, 380.0, 420.0
    d = np.hypot(xx - cx, yy - cy)
    inside = d < R
    tex = np.array(e3.regolith(w, h, seed, craters=36).convert("L")).astype(float) / 255
    nz = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    nx, ny = (xx - cx) / R, (yy - cy) / R
    lit = np.clip(-0.62 * nx - 0.5 * ny + 0.35 * nz, 0.04, 1.0)
    rgb = raster.ramp(WARM_ROCK, np.clip(0.35 + 0.6 * tex, 0, 1)) * (0.22 + 0.95 * lit[..., None])
    limb = np.abs(d - R) < 1.5
    rgb[limb] = rgb[limb] * 0.5 + np.array((210, 190, 160)) * 0.5 * lit[limb, None]
    a = np.where(inside, 255, 0).astype(np.uint8)
    return posterize(rgb, 40, a)


def transport_piece():
    w, h = 112, 176
    boxes = [(36, 10, 76, 150, 20, 1, 4), (22, 40, 90, 120, 14, 1, 3), (44, 150, 68, 168, 18, 4, 2)]
    img, _ = structure(w, h, boxes, seed=101, colours=24)
    return img, [(56, 12, 1.8), (24, 80, 1.6), (88, 80, 1.6), (56, 166, 2.2)]


def crust_edge(h=1100):
    """Rows [CANYON_SEAM - h + 120, CANYON_SEAM + 120): the canyon's rows, ending below in the crust's ragged lit edge
    (and the canyon's mouth opening into space) about 900 rows below the seam; transparent beyond the edge."""
    bottom = CANYON_SEAM - h + 120
    img = canyon_rows(bottom, h)
    yy, xx = np.mgrid[0:h, 0:W].astype(float)
    rng = np.random.default_rng(29)
    edge_y = h - 160 + 40 * np.sin(xx / 70 + 0.4) + 18 * np.sin(xx / 23 + 1.1) + rng.normal(0, 2, W)[None, :]
    mouth = np.abs(xx - 240) < 120
    edge_y = np.where(mouth, edge_y - 180 * np.clip(1 - np.abs(xx - 240) / 120, 0, 1) ** 0.5, edge_y)
    keep = yy < edge_y
    rim = keep & (yy > edge_y - 3)
    rgb = img[..., :3].astype(float)
    rgb[rim] = rgb[rim] * 0.5 + np.array((230, 214, 186)) * 0.5
    a = np.where(keep, 255, 0).astype(np.uint8)
    return posterize(rgb, 64, a), bottom


def canyon_mouth(h=1280):
    """Rows [BREAK_SEAM, BREAK_SEAM + h): the canyon running on from the seam (its rows the tile's own), then the crust
    ending in a ragged lit edge (space and the outer docks' frames beyond)."""
    bottom = BREAK_SEAM
    img = canyon_rows(bottom, h)
    yy, xx = np.mgrid[0:h, 0:W].astype(float)
    edge_y = 260 + 50 * np.sin(xx / 60 + 2.0) + 20 * np.sin(xx / 17 + 0.3)
    mouth = np.abs(xx - 240) < 150
    edge_y = np.where(mouth, edge_y + 160 * np.clip(1 - np.abs(xx - 240) / 150, 0, 1) ** 0.5, edge_y)
    keep = yy > edge_y
    rim = keep & (yy < edge_y + 3)
    rgb = img[..., :3].astype(float)
    rgb[rim] = rgb[rim] * 0.5 + np.array((236, 220, 190)) * 0.5
    return posterize(rgb, 64, np.where(keep, 255, 0).astype(np.uint8)), bottom


def outer_dock(i, w=144, h=112):
    """An outer berth platform (docks 10-12), its number stencilled beside the nest's seat."""
    boxes = [(6, 10, w - 6, h - 10, 18, 1, 3), (w // 2 - 28, h // 2 - 28, w // 2 + 28, h // 2 + 28, 24, 4, 3),
             (6, 10, 18, h - 10, 24, 5, 2)]
    img, _ = structure(w, h, boxes, seed=110 + i)
    raster.draw_text(img, w - 30, h - 30, str(10 + i), (236, 232, 214), scale=2)
    return img, [(12, 14, 2.0), (w - 12, 14, 2.0), (12, h - 14, 2.0), (w - 12, h - 14, 2.0)]


def outer_ring(w=W, h=240):
    boxes = [(0, 60, w - 1, 180, 26, 1, 4), (0, 76, w - 1, 90, 32, 5, 2), (0, 150, w - 1, 164, 32, 5, 2)]
    for x in range(30, w, 90):
        boxes.append((x - 14, 40, x + 14, 200, 36, 4, 3))
    img, _ = structure(w, h, boxes, seed=120)
    return img, [(x, y, 2.6) for x in range(30, w, 90) for y in (44, 196)]


# =========================================================================== the build

def place(name, layer, size, t=None, x=240, bottom=None, note="", **extra):
    """A placement: by exact bottom row (rows of the layer) or by t."""
    if bottom is not None:
        t = exact_t(bottom, size[1], FACTORS[layer])
    return dict(name=name, layer=layer, t=t, x=x, note=note, **extra)


def exact_centre(row_centre, h, layer="ground"):
    return exact_t(int(round(row_centre - h / 2)), h, FACTORS[layer])


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()
    pieces, placed = {}, []

    def save(name, img, layer, glow=None, frames=None, headings=None, **spec):
        if frames is not None:
            for k, f in enumerate(frames):
                artkit.save_png(f, OUT / f"{name}_{k}.png", SOURCE)
            size = list(frames[0].size)
        else:
            artkit.save_png(img, OUT / f"{name}.png", SOURCE)
            size = list(img.size)
        entry = {"layer": layer, "size": size, **spec}
        if headings:
            entry["headings"] = headings
        elif frames is not None:
            entry["frames"] = len(frames)
        pieces[name] = entry
        if glow is not None:
            for gname, gimg in glow.items():
                artkit.save_png(gimg, OUT / f"{gname}.png", SOURCE)

    # tiles
    artkit.save_png(space_tile(), OUT / "belt-space.png", SOURCE)
    artkit.save_png(Image.fromarray(canyon()["img"]), OUT / "dock-canyon.png", SOURCE)
    artkit.save_png(outer_docks_tile(), OUT / "outer-docks.png", SOURCE)
    # deep
    img = ceres_limb()
    save("ceres-limb", img, "deep")
    placed.append(place("ceres-limb", "deep", img.size, t=4.0, x=240, note="Ceres's sunlit limb on the right"))
    img, lamps = transport_piece()
    save("cdf-transport", img, "deep", glow={"cdf-transport-glow": lamp_glow(*img.size, lamps, (255, 214, 160))})
    placed.append(place("cdf-transport", "deep", img.size, t=168.0, x=330, note="the CDF transport at the rendezvous"))
    # section 1: the crust's edge, the docking arms
    img, bottom = crust_edge()
    save("crust-edge", img, "ground")
    placed.append(place("crust-edge", "ground", img.size, bottom=bottom,
                        note=f"the crust's edge, its top 120 rows over the canyon tile's seam (ground {CANYON_SEAM})"))
    edge_row = CANYON_SEAM - 900
    for i, (x, hgt) in enumerate(((64, 420), (420, 360))):
        t = exact_t(edge_row - hgt + 40, hgt)
        arm = Image.new("RGBA", (112, hgt))
        d = ImageDraw.Draw(arm)
        d.rectangle([48, 0, 64, hgt - 1], fill=(104, 110, 118, 255))
        d.rectangle([48, 0, 49, hgt - 1], fill=(200, 206, 210, 255))
        d.rectangle([63, 0, 64, hgt - 1], fill=(40, 42, 48, 255))
        for y in range(0, hgt, 16):
            d.line([(50, y), (62, y + 16)], fill=(66, 70, 78, 255))
        for y in (40, hgt // 2, hgt - 40):
            d.rectangle([20, y - 6, 92, y + 6], fill=(96, 100, 108, 255), outline=(40, 42, 48, 255))
        if i == 0:
            hl = _render_traffic(("ore-hauler", 8))
            arm.alpha_composite(hl, (56 - 32, hgt // 2 - 90))
        save(f"docking-arm-{i + 1}", arm, "ground")
        placed.append(place(f"docking-arm-{i + 1}", "ground", arm.size, t=t, x=x, note="a docking arm off the crust"))
    # section 2: docks 1-5, the Helix block and pad, Coalition blocks
    glow_of = {}

    def lit(name, img, lamps, col=WHITE_LAMP, pool=0.0, red=False):
        g = {f"{name}-glow": lamp_glow(*img.size, lamps, col, pool)}
        if red:
            g[f"{name}-glow-red"] = lamp_glow(*img.size, lamps, RED_LAMP, pool)
        glow_of[name] = g
        save(name, img, "ground", glow=g)

    docks = [(1, 30, "l"), (2, 36, "r"), (3, 42, "l"), (4, 48, "r"), (5, 53, "l"), (6, None, None), (7, 94, "l"),
             (8, 102, "r")]
    for n, t, side in docks:
        if t is None:
            continue
        img, lamps = dock_bay(n, side)
        lit(f"dock-{n}", img, lamps)
        x = 52 if side == "l" else 428
        placed.append(place(f"dock-{n}", "ground", img.size, t=t, x=x, note=f"dock {n}, on the {'left' if side == 'l' else 'right'} rim"))
    img, lamps = helix_block()
    lit("helix-block", img, lamps, RED_LAMP, 0.4)
    placed.append(place("helix-block", "ground", img.size, x=110, note="Helix's dock block (nests x 90, 140 at t~34)",
                        t=exact_centre(target_row(34), img.height)))
    img, lamps = pad()
    lit("helix-pad", img, lamps, RED_LAMP, 0.4)
    placed.append(place("helix-pad", "ground", img.size, x=400, note="a Helix turret pad (the nest x 400 at t~34)",
                        t=exact_centre(target_row(34), img.height)))
    for i, (t, x) in enumerate(((40, 440), (47, 40), (66, 44), (70, 436))):
        img, lamps = coalition_block(i)
        lit(f"coalition-block-{i + 1}", img, lamps, WHITE_LAMP, 0.6, red=i >= 2)
        placed.append(place(f"coalition-block-{i + 1}", "ground", img.size, t=t, x=x,
                            note="a Coalition block on the rim" + (" (turns red with Block C)" if i >= 2 else "")))
    # section 3: Block C
    h = 224
    img, lamps = bridge_block(BLOCK_C_ROW - h // 2, h, (120, 240, 360), seed=83)
    lit("block-c", img, lamps, WHITE_LAMP, 0.7, red=True)
    placed.append(place("block-c", "ground", img.size, bottom=BLOCK_C_ROW - h // 2,
                        note="Block C across the canyon: its nests (x 120, 240, 360) 160 px below the top edge at t=60"))
    # section 4: the red blocks, dock 6
    img, lamps = coalition_block(9, 208, 176)
    lit("red-block-d", img, lamps, RED_LAMP, 0.6)
    placed.append(place("red-block-d", "ground", (208, 176), x=100, note="a turned block (nests x 70, 120 at t~84)",
                        t=exact_centre(target_row(84), 176)))
    img, lamps = pad(mat=1, trim=4)
    lit("red-pad", img, lamps, RED_LAMP, 0.5)
    placed.append(place("red-pad", "ground", img.size, x=410, note="a turned turret pad (the nest x 410 at t~84)",
                        t=exact_centre(target_row(84), img.height)))
    h = 200
    row_e = target_row(100)
    img, lamps = bridge_block(row_e - h // 2, h, (130, 240, 350), seed=87)
    lit("red-block-e", img, lamps, RED_LAMP, 0.7)
    placed.append(place("red-block-e", "ground", img.size, bottom=row_e - h // 2,
                        note="a turned bridging block (nests x 130, 240, 350 at t~100)"))
    img, lamps = dock6()
    lit("dock-6", img, lamps, WHITE_LAMP, 0.5)
    placed.append(place("dock-6", "ground", img.size, x=420, t=exact_centre(target_row(108, 24), img.height),
                        note="Dock 6: the crane gantry over the loyalists' locker (the secret, x 430 at t~108)"))
    # section 5: Dock 9 and the tender
    img, lamps = dock9()
    lit("dock-9", img, lamps, WHITE_LAMP, 0.8)
    placed.append(place("dock-9", "ground", img.size, bottom=ARENA_SCROLL,
                        note=f"Dock 9 on the halted screen (ground rows {ARENA_SCROLL}..+539): the clamp seats"))
    with ProcessPoolExecutor() as pool:
        tender = list(pool.map(_render_tender, [0.0, 0.2, 0.4, 0.6, 0.8, 1.0]))
        traffic = {n: list(pool.map(_render_traffic, [(n, k) for k in range(16)])) for n in TRAFFIC}
    tender = artkit.quantize_set(tender, 40)
    save("prison-tender", tender[0], "ground")
    tender_row = ARENA_SCROLL + SCREEN - 1 - TENDER[1]
    placed.append(place("prison-tender", "ground", tender[0].size, x=TENDER[0],
                        t=exact_centre(tender_row, tender[0].height),
                        note="the prison tender in the berth (screen 240, 250 on the halted screen)"))
    save("prison-tender-hold", None, "ground", frames=tender, fps=15)
    # section 7: the canyon's mouth, the outer docks, the ring
    img, bottom = canyon_mouth()
    save("canyon-mouth", img, "ground")
    placed.append(place("canyon-mouth", "ground", img.size, bottom=bottom,
                        note=f"the canyon's mouth from the outer docks' seam (ground {BREAK_SEAM}) on"))
    for i, x in enumerate((90, 240, 390)):
        img, lamps = outer_dock(i)
        lit(f"outer-dock-{i + 1}", img, lamps, WHITE_LAMP, 0.4)
        placed.append(place(f"outer-dock-{i + 1}", "ground", img.size, x=x, t=exact_centre(target_row(131), img.height),
                            note=f"an outer berth platform (the nest x {x} at t~131)"))
    img, lamps = outer_ring()
    lit("outer-ring", img, lamps, (255, 200, 120), 0.4)
    placed.append(place("outer-ring", "ground", img.size, bottom=END_ROW,
                        note="the outer ring: its lower edge on the bottom edge at the level's end"))
    # low-air traffic
    for name, frames in traffic.items():
        frames = artkit.quantize_set(frames, 32)
        save(name, None, "low-air", frames=frames, headings=16)
    paths = [("ore-hauler", 13.0, 420, [[11.0, 0, 0], [19.0, -340, 80]], "a loyalist hauler under Talon fire"),
             ("talon-pair", 13.4, 470, [[11.4, 0, 0], [19.4, -340, 80]], "the Talons chasing it"),
             ("cdf-patrol", 42.0, 40, [[39.0, 0, 0], [47.0, 400, -60]], "a CDF patrol crossing the canyon"),
             ("ore-hauler", 96.0, 60, [[93.0, 0, 0], [101.0, 360, 40]], "a hauler fleeing the red canyon"),
             ("cdf-patrol", 140.0, 440, [[137.0, 0, 0], [145.0, -400, 60]], "CDF patrol and mutineers dogfighting"),
             ("talon-pair", 140.5, 480, [[137.5, 0, 0], [145.5, -400, 60]], "the Talons on its tail"),
             ("ore-hauler", 150.0, 30, [[146.0, 0, 0], [154.0, 420, 0]],
              "the hauler that releases the armour patch (B2: a drop at (240, 380) at t=150)")]
    for name, t, x, path, note in paths:
        placed.append(dict(name=name, layer="low-air", t=t, x=x, path=path, note=note))
    return pieces, placed


# =========================================================================== proposal

def proposal_text(pieces, placed):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after B2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 29's Ceres Hub (tools/art/backdrop_l29.py, step ART-B, from {src}).",
             "  # The deep layer is space (the dust band, Ceres's limb, the transport); the canyon tile is opaque, the",
             "  # outer docks' tile transparent over space. Glows (-glow) are added by the game at their pieces: Block C's",
             "  # -glow until the turn (t=60), then -glow-red crossfaded over 1 s; coalition-block-3/-4 turn with it (B4).",
             "  # prison-tender-hold (6 frames) is not placed: the game draws it over prison-tender when the last clamp",
             "  # gun falls (4 steps a frame, the last held), then rook-launch (sprites) rises out of it.",
             "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far, high-air unused",
             "  ramp: 2",
             "  haze_colour: 06060a",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:",
             f"    belt-space: {{layer: deep, height: {SPACE_H}}}   # space, the sunlit dust band, stars, far rocks",
             f"    dock-canyon: {{layer: ground, height: {TILE_H}}}   # the dock canyon in Ceres's crust (sections 2-6)",
             f"    outer-docks: {{layer: ground, height: {TILE_H}}}   # the outer docks' girders over space (section 7)",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = ""
        if spec.get("headings"):
            extra = f", headings: {spec['headings']}"
        elif spec.get("frames"):
            extra = f", frames: {spec['frames']}, fps: {spec.get('fps', 15)}"
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        extra = f", path: {p['path']}" if p.get("path") else ""
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}{extra}}}")
    lines.append("# sections (ART-B's reading of the README's Layout; the rescue halt as Level 28's arena section):")
    for s in SECTIONS:
        lines.append("#   - " + ", ".join(f"{k}: {v}" for k, v in s.items()))
    lines.append(f"# rows: canyon seam {CANYON_SEAM}, the halted screen {ARENA_SCROLL}..{ARENA_SCROLL + SCREEN - 1}, "
                 f"breakout seam {BREAK_SEAM}, the end's bottom edge {END_ROW}")
    lines.append("# ground targets drawn for (kind, t at the top edge, x; ART-B's x where the README leaves it):")
    for kind, t, x in TARGETS:
        lines.append(f"#   {kind}: t {t if t is not None else 'Block C (at t=60, 160 px below the top edge)'}, x {x}")
    lines.append("#   clamp-gun x4 on the halted screen: " + ", ".join(f"({x}, {y})" for x, y in CLAMPS)
                 + " (screen px, y down); their clamps' sprites face the tender (left / right)")
    lines.append("# debris (the Mites' rocks, tools/art/belt_units.py; Level 03's small-chunk rules, 16 spin frames each):")
    lines.append("#   chunks: {belt-a: {hp: 6, size: [40, 36]}, belt-b: {hp: 6, size: [36, 32]}, belt-c: {hp: 6, size: [32, 28]}}")
    return "\n".join(lines) + "\n"


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return data["pieces"], data["placed"]


# =========================================================================== composite, review, check

def piece_images(name, spec):
    n = spec.get("headings") or spec.get("frames")
    if n:
        return [Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)]
    return [Image.open(OUT / f"{name}.png").convert("RGBA")]


def compose(t, pieces, placed, red_c=False, units=True):
    """The screen at level time t (480x540): deep tile and pieces, ground tiles by section and pieces with their
    glows, low-air traffic on its paths; stand-ins for the ground targets."""
    img = Image.new("RGBA", (W, SCREEN), (0, 0, 0, 255))
    g = scroll(t)
    deep = Image.open(OUT / "belt-space.png").convert("RGBA")
    ds = int(round(g * FACTORS["deep"]))
    arr = np.array(deep)
    rows = ds + SCREEN - 1 - np.arange(SCREEN)
    img = Image.fromarray(arr[SPACE_H - 1 - (rows % SPACE_H)], "RGBA")
    tiles = {"dock-canyon": np.array(Image.open(OUT / "dock-canyon.png").convert("RGBA")),
             "outer-docks": np.array(Image.open(OUT / "outer-docks.png").convert("RGBA"))}
    gs = int(round(g))
    base = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        r = gs + SCREEN - 1 - y
        sec = 0
        for i in range(1, len(SECTIONS)):
            if r >= seam_row(i):
                sec = i
        tid = next((x for x in SECTIONS[sec]["tiles"] if x in tiles), None)
        if tid:
            base[y] = tiles[tid][TILE_H - 1 - (r % TILE_H)]
    for p in placed:
        if pieces[p["piece"]]["layer"] == "deep":
            img = blit(img, p, pieces, t)
    img.alpha_composite(Image.fromarray(base, "RGBA"))
    for p in placed:
        if pieces[p["piece"]]["layer"] == "ground":
            img = blit(img, p, pieces, t, red_c=red_c)
    if units:
        img = stand_ins(img, t)
    for p in placed:
        if pieces[p["piece"]]["layer"] == "low-air":
            img = blit(img, p, pieces, t)
    return img


def blit(img, p, pieces, t, red_c=False):
    spec = pieces[p["piece"]]
    f = FACTORS[spec["layer"]]
    ims = piece_images(p["piece"], spec)
    h = ims[0].height
    centre = scroll(float(p["t"])) * f + SCREEN / 2
    dx = dy = 0.0
    k = 0
    if p.get("path"):
        pts = p["path"]
        if t < pts[0][0] or t > pts[-1][0]:
            return img
        for a, b in zip(pts, pts[1:]):
            if a[0] <= t <= b[0]:
                u = (t - a[0]) / (b[0] - a[0])
                dx, dy = a[1] + (b[1] - a[1]) * u, a[2] + (b[2] - a[2]) * u
                mx, my = b[1] - a[1], -(b[2] - a[2])         # screen-down motion against the layer
                k = int(round(np.arctan2(-mx, my) / TAU * 16)) % 16
    bottom = centre - h / 2 + dy
    y_top = scroll(t) * f + SCREEN - (bottom + h)
    x0 = p["x"] + dx - ims[0].width / 2
    im = ims[k] if len(ims) > 1 else ims[0]
    if y_top > SCREEN or y_top + h < 0:
        return img
    img = l_paste(img, im, x0, y_top)
    name = p["piece"]
    gname = f"{name}-glow-red" if (red_c and (OUT / f"{name}-glow-red.png").exists()) else f"{name}-glow"
    if (OUT / f"{gname}.png").exists():
        gl = Image.open(OUT / f"{gname}.png").convert("RGBA")
        img = artkit.add_light(img, gl, (int(round(x0)), int(round(y_top))))
    return img


def l_paste(dst, src, x0, y0):
    x0, y0 = int(round(x0)), int(round(y0))
    x1, y1 = max(0, x0), max(0, y0)
    x2, y2 = min(dst.width, x0 + src.width), min(dst.height, y0 + src.height)
    if x2 > x1 and y2 > y1:
        dst.alpha_composite(src.crop((x1 - x0, y1 - y0, x2 - x0, y2 - y0)), (x1, y1))
    return dst


def _held(name):
    import l23kit  # noqa: E402
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def stand_ins(img, t):
    """The nests (held production frames when written), the clamp guns on the halted screen, the ship."""
    base, rack = _held("sam-nest-base"), _held("sam-nest")
    dormant = _held("sam-nest-base-dormant")
    for kind, tt, x in TARGETS:
        if kind.startswith("sam-nest") and base:
            if tt is None:
                y = scroll(t) + SCREEN - 1 - BLOCK_C_ROW
                b = (dormant if t < 60 else base)[0]
            else:
                y = scroll(t) + SCREEN - 1 - target_row(tt)
                b = base[0]
            if -30 < y < SCREEN + 30:
                img = l_paste(img, b, x - 24, y - 24)
                img = l_paste(img, rack[0], x - 24, y - 24)
    gun = _held("clamp-gun")
    if gun and abs(t - ARENA_T) < 1e-6:
        for side, (x, y) in zip(("left", "right", "left", "right"), CLAMPS):
            cl = _held(f"clamp-gun-clamp-{side}")
            if cl:
                img = l_paste(img, cl[0], x - 48, y - 28)
            img = l_paste(img, gun[0], x - 20, y - 20)
    ship = artkit.load_frames("ship")[2]
    return l_paste(img, ship, 240 - 24, 480 - 24)


def ground_strip(r0, n, red_c=False):
    """The ground layer as laid, rows r0 .. r0+n-1 (an RGBA array, top = highest row): the sections' tile sets and the
    placed ground pieces with their glows (Block C white unless ``red_c``); for act_stills.py's Act 5 still."""
    pieces, placed = load_proposal()
    tiles = {"dock-canyon": np.array(Image.open(OUT / "dock-canyon.png").convert("RGBA")),
             "outer-docks": np.array(Image.open(OUT / "outer-docks.png").convert("RGBA"))}
    out = np.zeros((n, W, 4), np.uint8)
    seams = [seam_row(i) for i in range(len(SECTIONS))]
    for i in range(n):
        r = r0 + n - 1 - i
        sec = max([k for k in range(len(SECTIONS)) if k == 0 or r >= seams[k]])
        tid = next((x for x in SECTIONS[sec]["tiles"] if x in tiles), None)
        if tid:
            out[i] = tiles[tid][TILE_H - 1 - (r % TILE_H)]
    img = Image.fromarray(out, "RGBA")
    for p in placed:
        spec = pieces[p["piece"]]
        if spec["layer"] != "ground" or spec.get("headings") or spec.get("frames"):
            continue
        im = piece_images(p["piece"], spec)[0]
        bottom = int(round(scroll(float(p["t"])) + SCREEN / 2 - im.height / 2))
        top_i = r0 + n - 1 - (bottom + im.height - 1)
        if top_i > n or top_i + im.height < 0:
            continue
        img = l_paste(img, im, p["x"] - im.width / 2, top_i)
        name = p["piece"]
        g = OUT / (f"{name}-glow-red.png" if red_c and (OUT / f"{name}-glow-red.png").exists() else f"{name}-glow.png")
        if g.exists():
            img = artkit.add_light(img, Image.open(g).convert("RGBA"), (int(round(p["x"] - im.width / 2)), top_i))
    return np.array(img)


SHOTS = [(2.0, "THE APPROACH: CERES'S LIMB"), (24.0, "THE CRUST'S EDGE, THE DOCKING ARMS"),
         (34.0, "THE HELIX BLOCK AND PAD"), (59.0, "BLOCK C, WHITE (BEFORE THE TURN)"),
         (61.5, "BLOCK C, RED (AFTER THE TURN)"), (84.0, "THE RED CANYON"), (ARENA_T, "DOCK 9, HALTED"),
         (126.0, "THE CANYON'S MOUTH"), (140.0, "THE OUTER DOCKS"), (END, "THE OUTER RING AT THE END")]


def review():
    pieces, placed = load_proposal()
    shots = [(compose(t, pieces, placed, red_c=t > 60), lab) for t, lab in SHOTS]
    cols = 5
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    tiles = [Image.open(OUT / f"{n}.png").convert("RGBA") for n in ("belt-space", "dock-canyon", "outer-docks")]
    tile_h = 240
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + tile_h + 40,
                         "LEVEL 29 BACKDROP (FINAL R62): CERES HUB", f"PRODUCTION ART, {BATCH.upper()} - R62")
    raster.draw_text(sheet, 16, 38, "TEN SCREENS AT HALF SIZE WITH STAND-INS (THE NESTS, THE CLAMP GUNS, THE SHIP)",
                     raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 10
    raster.draw_text(sheet, 16, y, "THE TILE SETS (QUARTER SIZE): BELT-SPACE, DOCK-CANYON, OUTER-DOCKS (ON THE PLATE)",
                     raster.LABEL)
    x = 16
    for tl in tiles:
        small = tl.resize((tl.width // 4, tl.height // 4), Image.BOX)
        plate = Image.new("RGBA", small.size, (40, 44, 60, 255))
        plate.alpha_composite(small)
        sheet.alpha_composite(plate, (x, y + 16))
        x += small.width + 12
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    frames = []
    for i in range(0, 12 * 10):
        t = 54.0 + i / 10
        frames.append(compose(t, pieces, placed, red_c=t >= 60).resize((sw, sh), Image.BOX))
    for i in range(20):
        frames.append(compose(ARENA_T, pieces, placed).resize((sw, sh), Image.BOX))
    size = e60.rig.write_gif(frames, gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def wrap_score(a):
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def check():
    """Tiles wrap (BackdropSeamsTest's measure), the canyon tile is opaque, sizes match the proposal, Source chunks,
    the deep pieces' opaque edges never on the screen (BackdropAssetsTest's edge rule), pieces fit a page."""
    problems = []
    pieces, placed = load_proposal()
    for name in ("belt-space", "dock-canyon"):
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    for name, spec in pieces.items():
        for im in piece_images(name, spec):
            if list(im.size) != list(spec["size"]) or im.height > MAX_PIECE:
                problems.append(f"{name}: {im.size} vs {spec['size']}")
                break
    for f in OUT.glob("*.png"):
        if Image.open(f).text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
    for p in placed:
        spec = pieces[p["piece"]]
        if spec["layer"] != "deep":
            continue
        a = np.array(piece_images(p["piece"], spec)[0])[..., 3] > 0
        if a[0].any() or a[-1].any():
            problems.append(f"{p['piece']}: an opaque top or bottom row on the deep layer")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"{len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}; {len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        pc, pl = build_all()
        write_proposal(pc, pl)
    if "--proposal" not in args:
        review()
