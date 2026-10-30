#!/usr/bin/env python3
"""Production art: the Level 35 backdrop, Iron Sovereign (design/campaign/act-5-the-belt/level-35-iron-sovereign; M9
part H batch, concept round 68, step ART-H2).

Straight to production in the belt's lighter A.18 a grade and **Q.11 b** (round 60: gunmetal/charcoal with gold): Ceres
orbit, the Sovereign's escort screen over open space, then the battle station's **outer hull**, a long charcoal keel the
ship crosses for 30 s (plating, armour belts, frames with gold trim, conduits, vents, hatches, red running lights) under
the top-left sun, ending in a bulkhead and a **neck** that runs north under the ring assembly to the station's centre:
the rings, the core and the base disc are ART-H1's sprites (tools/art/iron_sovereign.py), anchored to the ground, so the
neck and the station scroll in together and nothing pops in. In the arena the neck shows under the ship band and Ceres's
curve lies far below in the deep layer, so the station's black stays distinct from space behind the outer ring.
Heightfields hill-shaded under the top-left sun with cast shadows (tools/art/backdrop_l29.py ``shade``, its rows wrap),
posterized per image; nothing lit is mirrored.

The hull is a **tile set** (``sovereign-hull``, section 3); its two ends are pieces cut from the same field
(``hull-start``, ``hull-end``): their rows the tile's own (exact rows, the tile's pixels copied where nothing changes and
the tile's palette elsewhere), so the seams into and out of the tile never show.

The layers (``scroll_factors``): ``deep`` 0.12 (space: the sunlit dust band, Ceres's sunlit curve with Ceres Hub's red
lights at the start, Ceres's north pole under the station at the end), ``far`` 0.5 (the Sovereign's tenders and a
docking spar far below, section 2: "far below", so the far layer, not the README's low-air), ``ground`` 1 (the hull, its
gun pads, the lifeboat's clamps, the beacon's wreck, the neck, the wreck's fragments), ``low-air`` 1.35 (the loyalist
wrecks and the dead Ceres patrol boat drifting, dimmed, section 1), ``high-air`` 2.2 (sparse close grit).

Tile sets (480 wide, wrapping):
  orbit-space     deep 480x1024: belt space (backdrop_l30.py ``space_tile``), the sunlit dust band, stars
  sovereign-hull  ground 480x960, opaque: the outer hull (section 3)
  orbit-dust      high-air 480x960, transparent: close grit (backdrop_l30.py ``dust_tile``)
Pieces (the layer named; ``frames`` loop at their fps):
  ceres-curve       deep 480x760: Ceres's sunlit limb low on the left, the night side with Ceres Hub's lights red
  ceres-pole        deep 480x760: Ceres's north polar region far below the halted station (sections 4-6)
  tender-a, -b      far 72x148 / 64x124: the Sovereign's tenders (charcoal and gold), hazed
  docking-spar      far 64x420: a docking spar far below, two tenders' berths, red lights, hazed
  patrol-boat-wreck low-air 96x176: the dead Ceres patrol boat (Coalition grey, its stern torn), dimmed
  loyalist-wreck-a, -b  low-air 64x72 / 56x64: wrecked Ceres Wing fighters, dimmed
  hull-start        ground 480x480: the hull's south bulkhead entering over space (two spar stubs, a mast); its top
                    rows the tile's own (covers the seam into section 3)
  hull-end          ground 480x1280: the hull's north bulkhead, the shoulders narrowing into the neck that runs to the
                    station's centre (its collar under the core), two docking spars over space; its bottom rows the
                    tile's own (covers the seam out of section 3)
  sam-pad           ground 64x64: a SAM Nest's mount (a hexagonal socket, a gold ring, two red lamps)
  bunker-pad        ground 96x96: a Rail Bunker's mount (an octagonal socket, gold corners)
  lifeboat-clamps   ground 112x80: the docking clamps that caught the Ceres lifeboat (its sprite is the trigger's,
                    tools/art/l35_props.py ``ceres-lifeboat``, drawn by the game between the jaws)
  beacon-wreck      ground 128x112: a loyalist fighter crashed on the hull; the CDF beacon (Level 33's ``nav-buoy``,
                    the drop's sprite, drawn by the game) stands on it
  red-blink         ground 12x12, 2 frames at 1 fps: a hull light blinking red (at most two on the screen)
  fragment-a, -b, -c  ground 176x120 / 136x152 / 112x96: broken hull plates drifting after the kill (section 6)

Review: design/campaign/act-5-the-belt/level-35-iron-sovereign/concept/backdrop-final-r68-a.png (eight screens with
stand-ins: the nests and bunkers on their pads, the lifeboat in its clamps, the beacon, the station from ART-H1's held
sprites at the halt, the relay's line and flash (tools/art/l35_props.py), the ship) and .gif (t 82-93 at half size: the
hull's end, the neck and the station rolling in to the halt). ``--proposal`` writes the ``backdrop:`` block, the
sections, the arena's rows, the ground targets' places (the nests and bunkers on the pads, the lifeboat), the beacon's
drop and a light debris block to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections. Re-run after
the data moves a section.
Run: python3 tools/art/backdrop_l35.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, TAU

import backdrop_l29 as l29  # noqa: E402  (tools/art: the wrapping hill shade, exact rows)
import backdrop_l30 as l30  # noqa: E402  (tools/art: the belt's space kit)
import backdrop_l34 as l34  # noqa: E402  (tools/art: the wrecks' hull kit)
l34.HELIX = l34.HELIX_GREY_GOLD  # the tenders keep the grey-gold Helix they were built with (L34 went black in FIX4)
import l23kit  # noqa: E402
from render import raster  # noqa: E402

SCRIPT = "backdrop_l35.py"
BATCH = "M9 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r68"
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-35-iron-sovereign"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-35"
W, SCREEN = 480, 540
SPACE_H, DUST_H, TILE_H = 1024, 960, 960
FACTORS = {"deep": 0.12, "far": 0.5, "ground": 1.0, "low-air": 1.35, "high-air": 2.2}
SUN = l29.SUN
STATION = [(0.0, (12, 12, 15)), (0.3, (30, 31, 36)), (0.55, (52, 54, 60)), (0.78, (80, 82, 90)),
           (1.0, (122, 122, 128))]
GOLD = np.array((200, 152, 58.0))
GOLD_LIT = np.array((236, 190, 92.0))
RED = np.array((255, 58, 40.0))
STEEL = np.array((62, 64, 70.0))
DARK = np.array((16, 16, 20.0))
CENTRE = (240, 160)                       # the station's centre on the halted screen (y down): sim (240, 380)

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OPEN = ["orbit-space", "orbit-dust"]
HULL = ["orbit-space", "sovereign-hull", "orbit-dust"]
PROPOSED_SECTIONS = [
    {"name": "Ceres Orbit", "end": 20, "atmosphere": "clear", "tiles": OPEN},
    {"name": "The Escort Screen", "end": 50, "atmosphere": "clear", "tiles": OPEN},
    {"name": "The Outer Hull", "end": 80, "speed": 150, "ramp": 2, "atmosphere": "clear", "tiles": HULL},
    {"name": "The Rings' Edge", "end": 91.5, "speed": 150, "atmosphere": "clear", "tiles": OPEN},
    {"name": "The Halt", "end": 92, "speed": 0, "ramp": 6, "atmosphere": "clear", "tiles": OPEN},
    {"name": "The Sovereign", "end": 93, "speed": 0, "arena": True, "hold_after_boss": 6, "atmosphere": "clear",
     "tiles": OPEN},
    {"name": "The Wreck", "end": 130, "speed": 60, "ramp": 2, "atmosphere": "clear", "tiles": OPEN},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 160)) if DATA else 160.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speed(i):
    s = SECTIONS[i]
    return float(s["speed"]) if s.get("speed") is not None else BASE_SPEED


def _arena(i):
    return bool(SECTIONS[i].get("arena"))


def _timed_halt(i):
    return _speed(i) == 0 and not _arena(i)


def _ramp(i):
    return float(SECTIONS[i].get("ramp", 2) or 2)


def _ease(i):
    return float(SECTIONS[i].get("ramp", 0) or 0)


def _u(t, start, ramp):
    return min(max((t - start) / ramp, 0.0), 1.0) if ramp > 0 else (1.0 if t >= start else 0.0)


def _ease_in(u):
    return u ** 3 - u ** 4 / 2


def _ease_out(u):
    return u - u ** 3 + u ** 4 / 2


def _travelled(i, a, b):
    """LevelScroll.travelled: the px section i scrolls from a to b s."""
    speed, start, end = _speed(i), STARTS[i], float(SECTIONS[i]["end"])
    px = (b - a) * speed
    if speed == 0:
        return px
    if i + 1 < len(SECTIONS) and _timed_halt(i + 1):
        ramp = min(_ramp(i + 1), end - start)
        px -= speed * ramp * (_ease_in(_u(b, end - ramp, ramp)) - _ease_in(_u(a, end - ramp, ramp)))
    if i > 0 and _timed_halt(i - 1):
        ramp = min(_ramp(i - 1), end - start)
        px -= speed * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    elif i > 0 and _ease(i) > 0:
        ramp = min(_ease(i), end - start)
        px -= (speed - _speed(i - 1)) * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    return px


def scroll(t):
    """LevelData.scrollAt: the ground layer's scroll at level time t, px (before the start at the first speed)."""
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


def section_index(name):
    return next(i for i, s in enumerate(SECTIONS) if s["name"] == name)


def seam_row(index):
    """The ground row entering at the top edge when section ``index`` starts (its tile set begins there)."""
    return scroll(STARTS[index]) + SCREEN


def exact_t(bottom, h, factor=1.0):
    """The placement time putting a piece h px tall with its bottom row on layer row ``bottom`` (backdrop_l29.py's)."""
    return l29_exact(bottom, h, factor)


def l29_exact(bottom, h, factor):
    t = time_at((bottom + h / 2 - SCREEN / 2) / factor)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) * factor + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def t_for_layer_row(row, factor):
    """The placement time putting a piece's centre on layer row ``row`` (centre = scroll(t) x factor + 270)."""
    return round(time_at((row - SCREEN / 2) / factor), 3)


def t_centre_at(t, y_down, factor):
    """The placement time of a piece whose centre is at screen y (down) at level time t."""
    return t_for_layer_row(scroll(t) * factor + (SCREEN - y_down), factor)


def row_at(t, y_down):
    """The ground row on screen y (down) at level time t."""
    return int(round(scroll(t))) + SCREEN - 1 - int(y_down)


HULL_INDEX = next(i for i, s in enumerate(SECTIONS) if "sovereign-hull" in (s.get("tiles") or []))
ARENA_INDEX = next(i for i in range(len(SECTIONS)) if _arena(i))
ARENA_T = STARTS[ARENA_INDEX]
ARENA_SCROLL = int(round(scroll(ARENA_T)))            # the halted screen: ground rows ARENA_SCROLL .. +539
CENTRE_ROW = ARENA_SCROLL + SCREEN - CENTRE[1]        # the station's centre (a pixel corner: rows below it < this)
HULL_SEAM = int(round(seam_row(HULL_INDEX)))          # the hull tile begins
EDGE_SEAM = int(round(seam_row(HULL_INDEX + 1)))      # the hull tile ends (open space beyond: the neck is a piece)
END_ROW = int(round(scroll(END)))


# =========================================================================== the hull tile (periodic in its rows)

def _pnoise(cell, seed, octaves=3, w=W, h=TILE_H):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=True)


def _rect(hgt, alb, x0, y0, x1, y1, z, col, bev=2.0, mode="add", wrap=True):
    """A raised (z > 0) or sunk (z < 0) rectangle; ``wrap`` draws it again a tile height above and below."""
    h, w = hgt.shape
    for dy in ((-h, 0, h) if wrap else (0,)):
        ya, yb = int(max(0, y0 + dy)), int(min(h, y1 + dy))
        xa, xb = int(max(0, x0)), int(min(w, x1))
        if yb <= ya or xb <= xa:
            continue
        yy, xx = np.mgrid[ya:yb, xa:xb].astype(float)
        edge = np.minimum.reduce([xx - x0 + 0.5, x1 - xx - 0.5, yy - (y0 + dy) + 0.5, (y1 + dy) - yy - 0.5])
        prof = z * np.clip(edge / bev, 0, 1) if bev else np.full_like(xx, z)
        if mode == "add":
            hgt[ya:yb, xa:xb] += prof
        else:
            hgt[ya:yb, xa:xb] = prof
        if col is not None:
            alb[ya:yb, xa:xb] = col


def _disc(hgt, alb, x, y, r, z, col, ring=None, wrap=True):
    h, w = hgt.shape
    for dy in ((-h, 0, h) if wrap else (0,)):
        ya, yb = int(max(0, y + dy - r - 2)), int(min(h, y + dy + r + 3))
        xa, xb = int(max(0, x - r - 2)), int(min(w, x + r + 3))
        if yb <= ya or xb <= xa:
            continue
        yy, xx = np.mgrid[ya:yb, xa:xb].astype(float)
        d = np.hypot(xx - x, yy - (y + dy))
        m = d <= r
        prof = z * np.sqrt(np.clip(1 - (d / r) ** 2, 0, 1)) if z > 0 else np.where(m, z, 0.0)
        hgt[ya:yb, xa:xb] += np.where(m, prof, 0)
        if col is not None:
            alb[ya:yb, xa:xb][m] = col
        if ring is not None:
            rm = np.abs(d - r) < 1.2
            alb[ya:yb, xa:xb][rm] = ring
            hgt[ya:yb, xa:xb][rm] += 1.5


def _plating(w, h, seed, pw, ph, periodic=True, phase=0):
    """Staggered hull panels of slightly different tone, dark seams, a lit lip beside each (1 = the mean); ``phase``
    rows shift the grid (the tile keeps its seams off its wrap)."""
    rng = np.random.default_rng(seed)
    rows, cols = np.arange(h)[:, None] + phase, np.arange(w)[None, :]
    r = rows // ph
    off = (r % 2) * (pw // 2)
    c = (cols + off) // pw
    vals = rng.uniform(0.84, 1.1, (h // ph + 2, w // pw + 3))
    lum = vals[r % (h // ph), c]
    seam = ((rows % ph) == 0) | (((cols + off) % pw) == 0)
    lip = ((rows % ph) == 1) | (((cols + off) % pw) == 1)
    lum = np.where(seam, 0.6, np.where(lip, lum * 1.08, lum))
    grain = _pnoise(8, seed + 1, 2, w, h) if periodic else raster.fbm(w, h, 8, seed + 1, octaves=2, period=False)
    return lum * (0.95 + 0.1 * grain), seam


BELTS = ((22, 64), (416, 458))            # the longitudinal armour belts (x ranges)
FRAME_EVERY = 240                         # a transverse frame every 240 rows (four a tile)


def _trim(alb, hgt, x0, y0, x1, y1, col=GOLD, width=2, sides="tlbr"):
    """Gold trim along a rectangle's sides (t, l, b, r), rows wrapping."""
    h = alb.shape[0]
    rows = np.arange(y0, y1) % h
    if "t" in sides:
        for k in range(width):
            alb[(y0 + k) % h, x0:x1] = col
    if "b" in sides:
        for k in range(width):
            alb[(y1 - 1 - k) % h, x0:x1] = col * 0.8
    for side, x in (("l", x0), ("r", x1 - width)):
        if side in sides:
            alb[rows, x:x + width] = col if side == "l" else col * 0.8
    del hgt


def hull_field(seed=3501):
    """The outer hull's (height px, albedo RGB, light points) in image rows, periodic over TILE_H rows: plating in
    panels of slightly different height and tone, two armour belts along the edges with gold inner trim and red
    running lights, a gold-trimmed transverse frame every 240 rows, and in each bay between two frames a raised armour
    slab with gold trim, a conduit run, and vent grilles, hatches, heat-sink fins or sensor domes."""
    h, w = TILE_H, W
    rng = np.random.default_rng(seed)
    pw, ph = 60, 40
    lum, seam = _plating(w, h, seed, pw, ph, phase=ph // 2)
    rows, cols = np.arange(h)[:, None] + ph // 2, np.arange(w)[None, :]
    r = rows // ph
    c = (cols + (r % 2) * (pw // 2)) // pw
    steps = rng.choice([0.0, 1.5, 3.0], size=(h // ph + 2, w // pw + 3))
    hgt = 10 + steps[r % (h // ph), c] + 1.2 * _pnoise(48, seed + 2, 3) - 2.0 * seam
    tone = 0.66 * lum * (0.9 + 0.2 * _pnoise(96, seed + 3, 2))
    alb = raster.ramp(STATION, np.clip(tone, 0, 1))
    lights = []
    for x0, x1 in BELTS:                                            # the armour belts
        _rect(hgt, alb, x0, -4, x1, h + 4, 10, None, bev=3, wrap=False)
        alb[:, x0 + 3:x1 - 3] = raster.ramp(STATION, np.full((h, x1 - x0 - 6), 0.58))
        inner = x1 - 3 if x0 < 240 else x0
        alb[:, inner:inner + 3] = GOLD
        hgt[:, inner:inner + 3] += 1
        for y in range(30, h, 120):
            lights.append((x0 + (x1 - x0) // 2, y))
        for y in range(20, h, 40):                                   # the belt's plate joints
            alb[y, x0 + 3:x1 - 3] = alb[y, x0 + 3:x1 - 3] * 0.45
            hgt[y, x0 + 3:x1 - 3] -= 2
    for y in range(0, h, FRAME_EVERY):                              # the frames
        _rect(hgt, alb, 64, y - 10, 416, y + 10, 9, STEEL * 0.9, bev=3)
        _trim(alb, hgt, 64, y - 10, 416, y + 10, GOLD, 2, "tb")
        for x in range(76, 410, 24):                                 # bolts
            for yy in ((y - 4) % h, (y + 3) % h):
                alb[yy, x:x + 2] = STEEL * 1.7
    for cell in range(h // FRAME_EVERY):
        y0 = cell * FRAME_EVERY + 14
        y1 = y0 + FRAME_EVERY - 28
        left = cell % 2 == 0
        sx0, sx1 = (80, int(rng.integers(220, 260))) if left else (int(rng.integers(220, 260)), 400)
        sy0, sy1 = y0 + int(rng.integers(8, 30)), y1 - int(rng.integers(8, 30))
        _rect(hgt, alb, sx0, sy0, sx1, sy1, 8, raster.ramp(STATION, np.full((1, 1), 0.5))[0, 0], bev=3)
        _trim(alb, hgt, sx0, sy0, sx1, sy1, GOLD, 2, "tl" if left else "tr")
        for yy in range(sy0 + 30, sy1 - 10, 44):                      # the slab's plate seams
            alb[yy % h, sx0 + 4:sx1 - 4] *= 0.55
        lights.append((sx1 - 10 if left else sx0 + 10, sy0 + 10))
        cx = (sx1 + 18) if left else (sx0 - 18 - 3 * 9)              # a conduit run beside the slab
        n = 3
        for k in range(n):
            px = cx + k * 9
            _rect(hgt, alb, px, y0 - 6, px + 6, y1 + 6, 6, STEEL * (0.95 + 0.12 * k), bev=3)
            for y in range(y0 + 10, y1, 46):
                _rect(hgt, alb, px - 1, y, px + 7, y + 4, 7.5, STEEL * 1.4, bev=1)
        side = (cx + n * 9 + 16, 400) if left else (80, cx - 16)
        ys = np.linspace(y0 + 30, y1 - 30, 3)
        for fy in ys:
            kind = rng.choice(["vent", "hatch", "fins", "dome"])
            fx = int(rng.integers(side[0] + 26, max(side[0] + 27, side[1] - 26)))
            fy = int(fy + rng.integers(-10, 10))
            if kind == "vent":
                vw, vh = int(rng.integers(30, 50)), int(rng.integers(18, 28))
                _rect(hgt, alb, fx - vw // 2, fy - vh // 2, fx + vw // 2, fy + vh // 2, -5, DARK * 1.3, bev=1)
                for sxx in range(fx - vw // 2 + 3, fx + vw // 2 - 2, 5):
                    _rect(hgt, alb, sxx, fy - vh // 2 + 2, sxx + 2, fy + vh // 2 - 2, 4, STEEL * 1.1, bev=0)
            elif kind == "hatch":
                rr = int(rng.integers(10, 15))
                _disc(hgt, alb, fx, fy, rr, -3, STEEL * 0.8, ring=GOLD * 0.9)
                _rect(hgt, alb, fx - rr + 3, fy - 1, fx + rr - 3, fy + 1, 1, DARK, bev=0)
            elif kind == "fins":
                fw, fh = int(rng.integers(36, 56)), int(rng.integers(24, 36))
                for syy in range(fy - fh // 2, fy + fh // 2, 4):
                    _rect(hgt, alb, fx - fw // 2, syy, fx + fw // 2, syy + 2, 5, STEEL * 1.15, bev=1)
            else:
                rr = int(rng.integers(7, 10))
                _disc(hgt, alb, fx, fy, rr + 3, 2, STEEL * 0.7)
                _disc(hgt, alb, fx, fy, rr, 7, GOLD * 0.9)
    return hgt, alb, lights


def shade_rgb(hgt, alb, lights=(), ambient=0.4):
    """Light a field under the top-left sun (rows wrap: backdrop_l29.py ``shade``), the red lights baked in."""
    rgb = l29.shade(hgt, alb, k=0.9, ambient=ambient, shadow_len=40) * 1.15
    for x, y in lights:
        h = rgb.shape[0]
        for dy in (-2, -1, 0, 1, 2, 3):
            for dx in (-2, -1, 0, 1, 2, 3):
                yy, xx = (y + dy) % h, x + dx
                if 0 <= xx < rgb.shape[1]:
                    k = 0.45 if max(abs(dy - 0.5), abs(dx - 0.5)) < 2 else 0.25
                    rgb[yy, xx] = rgb[yy, xx] * (1 - k) + RED * k
        for dy in (0, 1):
            for dx in (0, 1):
                if 0 <= x + dx < rgb.shape[1]:
                    rgb[(y + dy) % h, x + dx] = (255, 150, 120)
    return rgb


_TILE = {}


def hull_tile():
    """(the tile image, its float RGB before posterizing, height, albedo)."""
    if "img" not in _TILE:
        hgt, alb, lights = hull_field()
        rgb = shade_rgb(hgt, alb, lights)
        img = l30.posterize(rgb, 64)
        _TILE.update(img=img, rgb=rgb, hgt=hgt, alb=alb, lights=lights)
    return _TILE["img"], _TILE["rgb"], _TILE["hgt"], _TILE["alb"]


def tile_index(rows):
    """The tile's image row of each ground row (a tile's pixel row is the row mod its height, from the bottom)."""
    return TILE_H - 1 - (np.asarray(rows) % TILE_H)


def to_palette(rgb, alpha, palette_img):
    """Float RGB quantized to an existing image's palette (no dithering), with an alpha."""
    pal = palette_img.convert("RGB").quantize(256, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    q = np.array(img.quantize(palette=pal, dither=Image.Dither.NONE).convert("RGBA"))
    q[..., 3] = alpha
    q[alpha == 0] = 0
    return q


# =========================================================================== the hull's ends (exact rows)

def _end_fields(bottom, h, pad=64):
    """The tile's height and albedo over ground rows [bottom - pad, bottom + h + pad), image rows (top = highest)."""
    _, _, hgt, alb = hull_tile()
    rows = np.arange(bottom + h + pad - 1, bottom - pad - 1, -1)
    idx = tile_index(rows)
    return hgt[idx].copy(), alb[idx].copy(), rows


def _finish(hgt, alb, op, keep, rows, pad, lights, light_rows=()):
    """Shade a hull end, crop the pad, copy the tile's own pixels where ``keep``, the rest in the tile's palette."""
    tile, _, _, _ = hull_tile()
    hgt = np.where(op, hgt, -40.0)
    rgb = shade_rgb(hgt, np.where(op[..., None], alb, 0), lights)
    edge = l34.erode(op, 1)
    tl = op & ~edge & (np.roll(~op, 1, axis=1) | np.roll(~op, 1, axis=0))
    br = op & ~edge & ~tl
    rgb[tl] = rgb[tl] * 0.45 + np.array((236, 214, 160)) * 0.55          # the sunward rim catches gold-white light
    rgb[br] = rgb[br] * 0.5
    sl = slice(pad, -pad)
    rgb, op, keep, rows = rgb[sl], op[sl], keep[sl], rows[sl]
    alpha = np.where(op, 255, 0).astype(np.uint8)
    out = to_palette(rgb, alpha, tile)
    own = np.array(tile)[tile_index(rows)]
    out[keep] = own[keep]
    out[0] = 0                                                          # never an opaque edge row beyond the hull
    return Image.fromarray(out, "RGBA")


def _truss(hgt, alb, op, x0, y0, x1, y1, width=10, z=14):
    """A docking spar: two rails and their cross bracing from (x0, y0) to (x1, y1), red lamps at its tip."""
    h, w = hgt.shape
    d = np.array((x1 - x0, y1 - y0), float)
    length = np.hypot(*d)
    d /= length
    n = np.array((-d[1], d[0]))
    m = l34.poly_mask(w, h, [[(x0 + n[0] * width / 2, y0 + n[1] * width / 2), (x1 + n[0] * width / 2,
                                                                             y1 + n[1] * width / 2)],
                             [(x0 - n[0] * width / 2, y0 - n[1] * width / 2), (x1 - n[0] * width / 2,
                                                                             y1 - n[1] * width / 2)]], width=3)
    for s in np.arange(0, length, 14):
        a = np.array((x0, y0)) + d * s
        b = a + d * 14
        m |= l34.poly_mask(w, h, [[tuple(a + n * width / 2), tuple(b - n * width / 2)]], width=2)
    hgt[m] = np.maximum(hgt[m], z)
    alb[m] = STEEL * 1.25
    op |= m
    return (int(x1), int(y1))


def hull_start():
    """Ground rows [bottom, bottom + 480): the hull's south bulkhead about 330 rows below the seam into section 3, its
    rows above the tile's own; below it open space with two spar stubs and a mast."""
    h, pad = 480, 64
    bottom = HULL_SEAM - h + 60
    hgt, alb, rows = _end_fields(bottom, h, pad)
    hh = hgt.shape[0]
    yy, xx = np.mgrid[0:hh, 0:W].astype(float)
    edge_row = HULL_SEAM - 330                                          # the bulkhead's outer face (ground row)
    e = (bottom + h + pad - 1) - edge_row                               # its image row
    face = e - 6 * np.abs(np.sin(xx / 120 * np.pi)) ** 8                # bays let into the face between the bastions
    op = yy <= face
    band = op & (yy > face - 30)
    hgt = np.where(band, 22 + 4 * np.clip((face - yy) / 4, 0, 1), hgt)
    alb = np.where(band[..., None], raster.ramp(STATION, np.full(yy.shape, 0.5))[..., :3], alb)
    lip = op & (np.abs(yy - (face - 30)) < 1.5)
    alb[lip] = GOLD
    gold = op & (np.abs(yy - (face - 3)) < 1.2)
    alb[gold] = GOLD_LIT
    for x in range(40, W, 80):                                           # the bastions' armour blocks
        _rect(hgt, alb, x - 18, e - 46, x + 18, e - 4, 10, STEEL * 0.9, bev=3, wrap=False)
    lights = [(x, int(e - 16)) for x in range(80, W, 80)]
    for x0, x1 in ((120, 108), (360, 376)):
        lights.append(_truss(hgt, alb, op, x0, e - 2, x1, e + 150))
    m = l34.poly_mask(W, hh, [[(250, e - 2), (252, e + 90)]], width=3)  # a mast with a lamp
    hgt[m], alb[m] = 16, STEEL * 1.4
    op |= m
    lights.append((252, int(e + 90)))
    keep = yy < e - 120
    return _finish(hgt, alb, op, keep, rows, pad, lights), bottom


NECK = (152, 328)                         # the neck's x range
COLLAR_R = 96


def hull_end():
    """Ground rows [bottom, bottom + 1280): the tile's rows from 40 below the seam out of section 3, the north bulkhead
    100 rows past it, the shoulders narrowing over 150 rows into the neck (x 152-328), the neck running north to the
    station's centre and its collar (r 96, under the core), two docking spars out over space with red lamps."""
    h, pad = 1280, 64
    bottom = EDGE_SEAM - 40
    hgt, alb, rows = _end_fields(bottom, h, pad)
    hh = hgt.shape[0]
    yy, xx = np.mgrid[0:hh, 0:W].astype(float)
    top = bottom + h + pad - 1

    def img_row(r):
        return top - r

    full = img_row(EDGE_SEAM + 100)                                     # the full hull ends here (image row)
    shoulder = img_row(EDGE_SEAM + 250)
    centre = img_row(CENTRE_ROW)
    u = np.clip((full - yy) / (full - shoulder), 0, 1)                   # 0 at the bulkhead, 1 at the shoulder's top
    half = (W / 2 + 40) * (1 - u) + (NECK[1] - NECK[0]) / 2 * u
    neck = (np.abs(xx - W / 2) <= half) & (yy >= centre)
    collar = np.hypot(xx - W / 2, yy - centre) <= COLLAR_R
    op = (yy > full) | neck | collar
    # the bulkhead: a raised band along the cut where the full hull ends, following the shoulders
    bulk = op & (yy <= full + 26) & (yy > full - 4) & (np.abs(xx - W / 2) > (NECK[1] - NECK[0]) / 2 - 2)
    shoulder_rim = op & ~bulk & (yy <= full) & (np.abs(xx - W / 2) > half - 22)
    region = op & (yy <= full + 26)
    plate_lum, seam = _plating(W, hh, 3511, 44, 48, periodic=False)
    neck_alb = raster.ramp(STATION, np.clip(0.6 * plate_lum, 0, 1))
    hgt = np.where(region & ~bulk, 12 + 2 * seam * -1.0, hgt)
    alb = np.where(region[..., None] & ~bulk[..., None], neck_alb, alb)
    hgt = np.where(bulk, 24.0, hgt)
    alb = np.where(bulk[..., None], STEEL, alb)
    alb[bulk & (np.abs(yy - (full + 25)) < 1.2)] = alb[bulk & (np.abs(yy - (full + 25)) < 1.2)] * 0.5
    hgt = np.where(shoulder_rim, 18.0, hgt)
    alb = np.where(shoulder_rim[..., None], STEEL * 0.92, alb)
    trim = op & (yy <= full) & (np.abs(np.abs(xx - W / 2) - (half - 22)) < 1.3)
    alb[trim] = GOLD
    lights = []
    # the neck: a central spine with conduits, ribs every 48 rows, gold edge trim, walkway lights
    spine = neck & (np.abs(xx - W / 2) < 20) & (yy < shoulder + 40)
    hgt = np.where(spine, 22.0, hgt)
    alb = np.where(spine[..., None], STEEL * 1.05, alb)
    for k in (-14, -6, 6, 14):
        c = spine & (np.abs(xx - (W / 2 + k)) < 2.5)
        hgt[c] += 3
        alb[c] = STEEL * 1.35
    rib = neck & (((top - yy) % 48) < 6) & (yy < shoulder)
    hgt[rib] += 4
    alb[rib] = alb[rib] * 1.18
    for side in (-1, 1):
        x_edge = W / 2 + side * ((NECK[1] - NECK[0]) / 2 - 3)
        e = neck & (np.abs(xx - x_edge) < 2.0) & (yy < shoulder)
        alb[e] = GOLD
        for y in range(int(centre) + COLLAR_R + 20, int(shoulder), 64):
            lights.append((int(W / 2 + side * 62), y))
    # the collar under the core: a raised ring and its gold band
    d = np.hypot(xx - W / 2, yy - centre)
    ring = collar & (d > COLLAR_R - 20)
    hgt = np.where(collar, np.where(ring, 26.0, 18.0), hgt)
    alb = np.where(collar[..., None], np.where(ring[..., None], STEEL * 1.1, STEEL * 0.7), alb)
    alb[collar & (np.abs(d - (COLLAR_R - 20)) < 1.3)] = GOLD
    for k in range(12):
        a = TAU * k / 12
        lights.append((int(W / 2 + np.cos(a) * (COLLAR_R - 10)), int(centre + np.sin(a) * (COLLAR_R - 10))))
    # docking spars from the shoulders out over space
    lights.append(_truss(hgt, alb, op, 120, full - 70, 24, full - 260))
    lights.append(_truss(hgt, alb, op, 360, full - 90, 452, full - 300))
    for x in range(40, W, 80):                                           # the bulkhead's lamps
        if abs(x - W / 2) > (NECK[1] - NECK[0]) / 2 + 10:
            lights.append((x, int(full + 12)))
    keep = yy > full + 70
    return _finish(hgt, alb, op, keep, rows, pad, lights), bottom


# =========================================================================== small ground pieces

def _small(w, h, build, seed, colours=40, ambient=0.42):
    hgt = np.zeros((h, w))
    alb = np.zeros((h, w, 3))
    op = np.zeros((h, w), bool)
    lights = build(hgt, alb, op, np.random.default_rng(seed))
    op[0], op[-1] = False, False
    rgb = l29.shade(np.where(op, hgt, -10), alb, k=0.9, ambient=ambient, shadow_len=24) * 1.15
    for x, y in lights:
        rgb[y - 1:y + 2, x - 1:x + 2] = rgb[y - 1:y + 2, x - 1:x + 2] * 0.5 + RED * 0.35
        rgb[y:y + 1, x:x + 2] = (255, 120, 96)
    return l30.posterize(rgb, colours, np.where(op, 255, 0).astype(np.uint8))


def sam_pad():
    """A SAM Nest's mount: a hexagonal socket 56 px across, a gold ring round the nest's 44 px base, two red lamps."""
    def build(hgt, alb, op, rng):
        yy, xx = np.mgrid[0:64, 0:64].astype(float) - 31.5
        a = np.arctan2(yy, xx)
        r = np.hypot(xx, yy)
        hexr = 28 / np.cos((a % (TAU / 6)) - TAU / 12)
        m = r <= hexr
        op |= m
        hgt[m] = 10 + 6 * np.clip((hexr[m] - r[m]) / 4, 0, 1)
        alb[m] = STEEL * 0.85
        well = r < 23
        hgt[well] = 8
        alb[well] = DARK * 2.2
        gold = np.abs(r - 23.5) < 1.2
        alb[gold] = GOLD
        return [(14, 46), (49, 46)]
    return _small(64, 64, build, 3521)


def bunker_pad():
    """A Rail Bunker's mount: an octagonal socket 88 px across round the bunker's 80 px octagon, gold corners."""
    def build(hgt, alb, op, rng):
        yy, xx = np.mgrid[0:96, 0:96].astype(float) - 47.5
        a = np.arctan2(yy, xx) + TAU / 16
        r = np.hypot(xx, yy)
        octr = 46 / np.cos((a % (TAU / 8)) - TAU / 16)
        m = r <= octr
        op |= m
        hgt[m] = 10 + 6 * np.clip((octr[m] - r[m]) / 4, 0, 1)
        alb[m] = STEEL * 0.8
        well = r <= octr - 5
        hgt[well] = 8
        alb[well] = DARK * 2.4
        for k in range(8):
            ang = TAU * k / 8
            cx, cy = 47.5 + np.cos(ang) * 42, 47.5 + np.sin(ang) * 42
            c = np.hypot(xx + 47.5 - cx, yy + 47.5 - cy) < 3
            alb[c] = GOLD
        return [(10, 48), (86, 48)]
    return _small(96, 96, build, 3523)


def lifeboat_clamps():
    """The docking clamps that caught the Ceres lifeboat: a cradle plate 112x80 with two gunmetal arms closing on the
    boat's place (its sprite 64x48 at the centre), gold jaws, a red lock lamp each."""
    def build(hgt, alb, op, rng):
        _rect(hgt, alb, 8, 16, 104, 64, 12, STEEL * 0.75, bev=3, wrap=False)
        op[16:64, 8:104] = True
        _rect(hgt, alb, 24, 26, 88, 54, -4, DARK * 2, bev=1, wrap=False)          # the cradle's well
        for x0, x1 in ((2, 22), (90, 110)):                                       # the arms
            _rect(hgt, alb, x0, 30, x1, 50, 20, STEEL * 1.05, bev=3, wrap=False)
            op[30:50, x0:x1] = True
        for x0, x1 in ((18, 26), (86, 94)):                                       # the gold jaws
            _rect(hgt, alb, x0, 28, x1, 52, 22, GOLD, bev=1, wrap=False)
        for x0, x1 in ((40, 44), (68, 72)):                                       # hydraulic rams
            _rect(hgt, alb, x0, 6, x1, 18, 8, STEEL * 1.3, bev=1, wrap=False)
            op[6:18, x0:x1] = True
        return [(12, 40), (100, 40)]
    return _small(112, 80, build, 3525)


def beacon_wreck():
    """A loyalist fighter crashed on the hull (Coalition grey and Ceres Wing ochre, nose crumpled, a wing torn), the
    beacon's mount on its back at the piece's centre (the nav buoy stands there), a scorched skid mark behind it."""
    def build(hgt, alb, op, rng):
        yy, xx = np.mgrid[0:112, 0:128].astype(float)
        skid = (np.abs((xx - 64) * 0.5 + (yy - 80) * 0.9) < 9) & (yy > 60) & (yy < 110)
        op |= skid
        hgt[skid] = 6
        alb[skid] = DARK * 1.3
        body = l34.poly_mask(128, 112, [[(64, 14), (74, 40), (76, 78), (68, 92), (60, 92), (52, 78), (54, 40)]])
        wing_l = l34.poly_mask(128, 112, [[(54, 48), (14, 70), (16, 78), (54, 66)]])
        wing_r = l34.poly_mask(128, 112, [[(74, 48), (100, 60), (98, 66), (74, 62)]])        # torn short
        m = body | wing_l | wing_r
        m = l34.torn(m, 3527, 0.3, r=2, cell=4)
        op |= m
        hgt[m] = 10 + 10 * l34.blur(m, 6)[m]
        alb[m] = np.array((128, 132, 134.0))
        stripe = m & (np.abs(yy - 58) < 3)
        alb[stripe] = np.array((168, 120, 52.0))
        can = m & (np.hypot(xx - 64, yy - 30) < 6)
        alb[can] = np.array((40, 52, 64.0))
        mount = np.hypot(xx - 64, yy - 56) < 8
        hgt[mount] = 18
        alb[mount] = STEEL * 1.2
        rng2 = np.random.default_rng(3529)
        for _ in range(6):
            x, y, r = rng2.uniform(30, 98), rng2.uniform(20, 96), rng2.uniform(6, 14)
            k = np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / r ** 2)
            alb *= (1 - 0.55 * k)[..., None]
        return []
    return _small(128, 112, build, 3527)


def red_blink(i):
    img = Image.new("RGBA", (12, 12), (0, 0, 0, 0))
    if i == 0:
        a = np.zeros((12, 12, 4))
        yy, xx = np.mgrid[0:12, 0:12].astype(float) - 5.5
        r = np.hypot(xx, yy)
        halo = np.floor(np.clip(1 - r / 6, 0, 1) * 3) / 3
        a[..., :3] = RED
        a[..., 3] = halo * 200
        core = r < 1.6
        a[core, :3] = (255, 150, 120)
        a[core, 3] = 255
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
    else:
        d = ImageDraw.Draw(img)
        d.rectangle([5, 5, 6, 6], fill=(90, 30, 26, 255))
    return img


def fragment(name):
    """A broken hull plate after the kill: plating torn on every side, a gold-trimmed edge, scorched."""
    w, h, seed = {"fragment-a": (176, 120, 3531), "fragment-b": (136, 152, 3533), "fragment-c": (112, 96, 3535)}[name]

    def build(hgt, alb, op, rng):
        yy, xx = np.mgrid[0:h, 0:w].astype(float)
        m = l30_blob(w, h, seed)
        m = l34.torn(m, seed, 0.45, r=3, cell=5)
        op |= m
        lum, seam = _plating(w, h, seed, 40, 30, periodic=False)
        hgt[m] = (8 + 8 * l34.blur(m, 10))[m] - 2 * seam[m]
        alb[m] = raster.ramp(STATION, np.clip(0.62 * lum, 0, 1))[m]
        band = m & (np.abs(yy - h * 0.35 - (xx - w / 2) * 0.2) < 2)
        alb[band] = GOLD
        for _ in range(4):
            x, y, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(10, 24)
            k = np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / r ** 2)
            alb *= (1 - 0.6 * k)[..., None]
        return []
    return _small(w, h, build, seed)


def l30_blob(w, h, seed):
    import backdrop_l31 as l31  # noqa: E402  (tools/art: the irregular outline)
    return l31.blob_mask(w, h, w / 2, h / 2, w * 0.44, h * 0.42, seed, wobble=0.22) > 0.5


# =========================================================================== deep, far and low-air pieces

def ceres_disc(w, h, cx, cy, R, seed, hub=None, lit_dir=(-0.62, -0.5, 0.35)):
    """Ceres from above (a cratered disc lit from the top left, its night side dark), ``hub`` (x, y): Ceres Hub's
    lights on the night side, red since Level 29. Top and bottom rows clear (the deep layer's edge rule)."""
    import enemies_r03 as e3  # noqa: E402  (concept script: the regolith texture)
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    d = np.hypot(xx - cx, yy - cy)
    tex = np.array(e3.regolith(w, h, seed, craters=40).convert("L")).astype(float) / 255
    nz = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    nx, ny = (xx - cx) / R, (yy - cy) / R
    lit = np.clip(lit_dir[0] * nx + lit_dir[1] * ny + lit_dir[2] * nz, 0.03, 1.0)
    rgb = raster.ramp(l30.WARM_ROCK, np.clip(0.35 + 0.6 * tex, 0, 1)) * (0.16 + 0.95 * lit[..., None])
    limb = np.abs(d - R) < 1.6
    rgb[limb] = rgb[limb] * 0.5 + np.array((214, 194, 164)) * 0.5 * np.clip(lit[limb, None] * 2, 0.1, 1)
    inside = d < R
    inside[0], inside[-1] = False, False
    img = l30.posterize(rgb, 40, np.where(inside, 255, 0).astype(np.uint8))
    img = l30.hazed(img, 0.9, haze=0.18)
    if hub:
        rng = np.random.default_rng(seed + 9)
        dr = ImageDraw.Draw(img)
        hx, hy = hub
        for k in range(34):                                             # the hub's lights, red, along the canyon
            x, y = hx - 40 + k * 2.4 + rng.normal(0, 2), hy - 14 + k * 0.8 + rng.normal(0, 3)
            if np.hypot(x - cx, y - cy) < R - 4:
                c = (255, 80, 60, 255) if k % 3 else (255, 170, 140, 255)
                dr.rectangle([x, y, x + (1 if k % 4 else 0), y + 1], fill=c)
        img = l30.strobe(img, hx, hy, 0.6, (255, 60, 40), 16)
    return img


def tender(w, h, seed):
    """A Sovereign tender far below: a Helix-style charcoal hull with gold trim and a drive block, hazed (far)."""
    hull = l34.Hull(w, h, seed)
    m = hull.part(l34.ship(w / 2, 6, h - 8, w / 2 - 8, bow=h * 0.3, bow_kind="point", stern_kind="blunt", seed=seed),
                  "helix", tear=0.0, panels=(20, 14))
    hull.edge_trim(m, 2, l34.GOLD)
    hull.boxes([(w / 2 - 10, h * 0.55, w / 2 + 10, h * 0.75, 6, (60, 58, 56), 2)])
    hull.lights([(int(w / 2 - 12), int(h * 0.4)), (int(w / 2 + 12), int(h * 0.4))])
    return l30.hazed(hull.image(32), 0.62, haze=0.35)


def docking_spar(w=64, h=420):
    """A docking spar far below: a long truss with two berths, red lamps, hazed (far)."""
    hgt = np.zeros((h, w))
    alb = np.zeros((h, w, 3))
    op = np.zeros((h, w), bool)
    lights = [_truss(hgt, alb, op, w / 2, 8, w / 2, h - 8, width=16, z=14)]
    for y in (110, 290):
        _rect(hgt, alb, 8, y - 10, w - 8, y + 10, 10, STEEL * 0.9, bev=2, wrap=False)
        op[y - 10:y + 10, 8:w - 8] = True
        lights.append((10, y))
        lights.append((w - 11, y))
    op[0], op[-1] = False, False
    rgb = l29.shade(np.where(op, hgt, -10), alb, k=0.9, ambient=0.45, shadow_len=16) * 1.15
    for x, y in lights:
        rgb[max(0, y - 1):y + 2, max(0, x - 1):x + 2] = (255, 110, 90)
    img = l30.posterize(rgb, 24, np.where(op, 255, 0).astype(np.uint8))
    return l30.hazed(img, 0.62, haze=0.35)


def patrol_boat_wreck():
    """The dead Ceres patrol boat: Coalition grey, its stern torn open, scorched, a dark hole; dimmed (low-air)."""
    w, h = 96, 176
    hull = l34.Hull(w, h, 3541)
    m = hull.part(l34.ship(48, 6, 160, 30, bow=50, bow_kind="point", stern_kind="torn", seed=3541), "cdf",
                  tear=0.4, panels=(22, 16))
    hull.boxes([(38, 60, 58, 92, 8, l34.CDF_SUPER, 2)])
    hull.cavity([(30, 120), (64, 116), (60, 150), (34, 154)], seed=1)
    hull.scorch(5, (8, 18))
    del m
    return l30.hazed(hull.image(32), 0.7, haze=0.3)


def loyalist_wreck(w, h, seed):
    """A wrecked Ceres Wing fighter (grey with the wing's ochre band), a wing torn off, dimmed (low-air)."""
    hull = l34.Hull(w, h, seed)
    cx = w / 2
    hull.part([(cx, 4), (cx + 7, 24), (cx + 8, h - 12), (cx - 8, h - 12), (cx - 7, 24)], "cdf", tear=0.2,
              panels=(12, 10))
    hull.part([(cx - 6, h * 0.45), (4, h * 0.7), (6, h * 0.8), (cx - 6, h * 0.72)], "cdf", tear=0.3, panels=(12, 10))
    hull.bands(hull.op, [int(h * 0.62)], 3, np.array((150, 108, 48.0)))
    hull.scorch(3, (6, 12))
    return l30.hazed(hull.image(24), 0.7, haze=0.3)


# =========================================================================== build

def _render_job(job):
    kind = job[0]
    if kind == "start":
        return hull_start()
    if kind == "end":
        return hull_end()
    if kind == "ceres-curve":
        return ceres_disc(W, 760, 620, 700, 520, 3551, hub=(420, 620)), None
    if kind == "ceres-pole":
        return ceres_disc(W, 760, 330, 1130, 740, 3553, lit_dir=(-0.55, -0.6, 0.4)), None
    raise ValueError(kind)


PIECE_SIZES = {
    "ceres-curve": ("deep", (480, 760)), "ceres-pole": ("deep", (480, 760)),
    "tender-a": ("far", (72, 148)), "tender-b": ("far", (64, 124)), "docking-spar": ("far", (64, 420)),
    "patrol-boat-wreck": ("low-air", (96, 176)), "loyalist-wreck-a": ("low-air", (64, 72)),
    "loyalist-wreck-b": ("low-air", (56, 64)),
    "hull-start": ("ground", (480, 480)), "hull-end": ("ground", (480, 1280)),
    "sam-pad": ("ground", (64, 64)), "bunker-pad": ("ground", (96, 96)), "lifeboat-clamps": ("ground", (112, 80)),
    "beacon-wreck": ("ground", (128, 112)), "fragment-a": ("ground", (176, 120)), "fragment-b": ("ground", (136, 152)),
    "fragment-c": ("ground", (112, 96)),
}


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    save("orbit-space", l30.space_tile(seed=3561))
    save("orbit-dust", l30.dust_tile(seed=3562))
    tile, _, _, _ = hull_tile()
    save("sovereign-hull", tile)
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(_render_job, [("start",), ("end",), ("ceres-curve",), ("ceres-pole",)], chunksize=1))
    (start, start_bottom), (end, end_bottom), (curve, _), (pole, _) = res
    save("hull-start", start)
    save("hull-end", end)
    save("ceres-curve", curve)
    save("ceres-pole", pole)
    save("tender-a", tender(72, 148, 3571))
    save("tender-b", tender(64, 124, 3573))
    save("docking-spar", docking_spar())
    save("patrol-boat-wreck", patrol_boat_wreck())
    save("loyalist-wreck-a", loyalist_wreck(64, 72, 3575))
    save("loyalist-wreck-b", loyalist_wreck(56, 64, 3577))
    save("sam-pad", sam_pad())
    save("bunker-pad", bunker_pad())
    save("lifeboat-clamps", lifeboat_clamps())
    save("beacon-wreck", beacon_wreck())
    for i in range(2):
        save(f"red-blink_{i}", red_blink(i))
    for name in ("fragment-a", "fragment-b", "fragment-c"):
        save(name, fragment(name))
    return pieces_spec(), placements(start_bottom, end_bottom)


def pieces_spec():
    out = {name: {"layer": layer, "size": list(size)} for name, (layer, size) in PIECE_SIZES.items()}
    out["red-blink"] = {"layer": "ground", "size": [12, 12], "frames": 2, "fps": 1}
    return out


# =========================================================================== the places

NEST, BUNKER, BOAT = 44, 72, 32


def target_row(t, h):
    """The ground row of the centre of a target h px tall entering at the top edge at t (backdrop_l33.py's)."""
    return scroll(t) + SCREEN + h / 2


# The README's hull guns (t entering at the top edge, x); x and the t=70 stagger are ART-H2's (the decided trivia: the
# t~56 pair at 55.5, so no bunker is on the screen at the flash).
SAM_GROUPS = [(52.0, (90, 150, 390)), (64.0, (80, 140, 260)), (74.0, (120, 330, 390))]
BUNKER_PAIRS = [(55.5, ((110, 0.0), (370, 0.0))), (70.0, ((100, 0.0), (380, 1.0))), (78.0, ((90, 0.0), (390, 0.0)))]
LIFEBOAT = (66.0, 400)
BEACON = (80.0, 240, 160)                 # the drop: t, x, screen y down (380 y-up)
FRAGMENTS = [("fragment-a", 104.0, 110), ("fragment-c", 112.0, 360), ("fragment-b", 121.0, 140),
             ("fragment-c", 127.0, 400)]
BLINKS = [(54.0, 34, 300), (60.0, 437, 200), (67.0, 437, 420), (72.0, 34, 120), (76.0, 437, 300), (88.0, 200, 300)]


def placements(start_bottom, end_bottom):
    """Every placed piece: dict(name, layer, t, x, note)."""
    out = []
    hs, he = PIECE_SIZES["hull-start"][1][1], PIECE_SIZES["hull-end"][1][1]
    out.append(dict(name="hull-start", t=exact_t(start_bottom, hs), x=240,
                    note=f"the hull's south bulkhead; its top rows the tile's own (exact rows {start_bottom}.."
                         f"{start_bottom + hs - 1}: covers the seam at {HULL_SEAM})"))
    out.append(dict(name="hull-end", t=exact_t(end_bottom, he), x=240,
                    note=f"the hull's north bulkhead, the neck and the collar under the station's centre (row "
                         f"{CENTRE_ROW}); bottom rows the tile's own (exact rows {end_bottom}..{end_bottom + he - 1}: "
                         f"covers the seam at {EDGE_SEAM})"))
    first = True
    for t, xs in SAM_GROUPS:
        for x in xs:
            out.append(dict(name="sam-pad", t=t_for_layer_row(target_row(t, NEST), 1.0), x=x,
                            note="the SAM Nests' pads (under the nests)" if first else ""))
            first = False
    first = True
    for t, pair in BUNKER_PAIRS:
        for x, dt in pair:
            out.append(dict(name="bunker-pad", t=t_for_layer_row(target_row(t + dt, BUNKER), 1.0), x=x,
                            note="the Rail Bunkers' pads" if first else ""))
            first = False
    out.append(dict(name="lifeboat-clamps", t=t_for_layer_row(target_row(LIFEBOAT[0], BOAT), 1.0), x=LIFEBOAT[1],
                    note="the clamps holding the Ceres lifeboat (the secret's trigger sprite between the jaws)"))
    bt, bx, by = BEACON
    out.append(dict(name="beacon-wreck", t=t_centre_at(bt, by, 1.0), x=bx,
                    note=f"the crashed loyalist fighter: the beacon (nav-buoy) on its back at ({bx}, {by} down) at t={bt:g}"))
    for t, x, y in BLINKS:
        out.append(dict(name="red-blink", t=t_centre_at(t, y, 1.0), x=x, note="a red hull light blinking"
                        if t == BLINKS[0][0] else ""))
    for name, t, x in FRAGMENTS:
        out.append(dict(name=name, t=t_centre_at(t, SCREEN / 2, 1.0), x=x,
                        note="the station's broken plates after the kill" if t == FRAGMENTS[0][1] else ""))
    out.append(dict(name="ceres-curve", t=t_centre_at(2.0, 300, FACTORS["deep"]), x=240,
                    note="Ceres's sunlit curve low on the left, Ceres Hub's red lights on the night side"))
    out.append(dict(name="ceres-pole", t=t_centre_at(ARENA_T, 380, FACTORS["deep"]), x=240,
                    note=f"Ceres's north pole far below the halted station (centred 380 down at t={ARENA_T:g})"))
    far = [("docking-spar", 24.0, 380, 300), ("tender-a", 30.0, 120, 270), ("tender-b", 41.0, 330, 270),
           ("tender-a", 47.0, 70, 200)]
    for name, t, x, y in far:
        out.append(dict(name=name, t=t_centre_at(t, y, FACTORS["far"]), x=x,
                        note="the Sovereign's tenders and a docking spar far below" if t == far[0][1] else ""))
    low = [("loyalist-wreck-a", 4.0, 110, 270), ("patrol-boat-wreck", 9.0, 360, 270), ("loyalist-wreck-b", 14.0, 90, 270),
           ("loyalist-wreck-a", 18.0, 410, 270)]
    for name, t, x, y in low:
        out.append(dict(name=name, t=t_centre_at(t, y, FACTORS["low-air"]), x=x,
                        note="loyalist wrecks and the dead Ceres patrol boat drifting" if t == low[0][1] else ""))
    pieces = pieces_spec()
    for p in out:
        p["layer"] = pieces[p["name"]]["layer"]
        p["t"] = float(p["t"])
    return out


def targets():
    """The proposed ground targets: the nests and bunkers on the pads, the lifeboat in its clamps."""
    out = []
    for t, xs in SAM_GROUPS:
        out.append({"target": f"SAM Nest group x{len(xs)} on the hull", "section": HULL_INDEX + 1, "enemy": "sam-nest",
                    "count": len(xs), "at": [(t, x) for x in xs]})
    for t, pair in BUNKER_PAIRS:
        out.append({"target": "Rail Bunker pair on the hull", "section": HULL_INDEX + 1, "enemy": "rail-bunker",
                    "count": 2, "at": [(t + dt, x) for x, dt in pair]})
    out.append({"target": "The Ceres garrison lifeboat (the secret)", "section": HULL_INDEX + 1,
                "at": [LIFEBOAT]})
    out.sort(key=lambda g: g["at"][0][0])
    return out


def debris_lines():
    """Two small belt rocks in section 1 (the README's hazards: few in orbit), part B's small rocks."""
    return ["debris:",
            "  # Proposal (ART-H2): the README's two small asteroids in section 1 (part B's small belt rocks, 6 HP; no",
            "  # large rock in orbit). H2 checks them against the waves (no rock within 40 px of a spawn point).",
            "  chunks:",
            "    belt-a: {size: [40, 36], hp: 6}",
            "    belt-b: {size: [36, 32], hp: 6}",
            "  clearance: 120",
            "  max_large: 0",
            "  placed:",
            "    - {t: 8.8, x: 330, chunk: belt-a, drift: [-14, -170]}",
            "    - {t: 16.2, x: 140, chunk: belt-b, drift: [18, -185]}"]


def proposal_text(pieces, placed):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after H2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 35's Ceres orbit and the Sovereign's hull (tools/art/backdrop_l35.py, step",
             f"  # ART-H2, from {src}). Open space is the deep layer (the base) with Ceres's curve below; the ground",
             "  # is the outer hull's tile set in section 3 only, its two ends pieces with the tile's exact rows",
             "  # (hull-start, hull-end: the neck runs on to the station's centre under ART-H1's base disc). The far",
             "  # layer holds the tenders far below (section 2), low-air the loyalist wrecks (section 1). Animated: the",
             "  # red blinks only (at most two on the screen).",
             "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}",
             "  ramp: 2",
             "  haze_colour: 09090e",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:",
             f"    orbit-space: {{layer: deep, height: {SPACE_H}}}   # space, the sunlit dust band, stars",
             f"    sovereign-hull: {{layer: ground, height: {TILE_H}}}   # the outer hull (opaque), section 3",
             f"    orbit-dust: {{layer: high-air, height: {DUST_H}}}   # close grit (transparent)",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}}}")
    lines.append("sections:   # ART-H2's reading of the README's Layout (160, the hull 150 eased over 2 s; the halt eased "
                 "down over [85.5, 91.5] as a 0.5 s timed halt before the 1 s arena, the wreck eased up to 60); H2 writes "
                 "the real ones")
    for s in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {('[' + ', '.join(v) + ']') if isinstance(v, list) else v}"
                                         for k, v in s.items()) + "}")
    lines += [f"# rows: the hull tile from {HULL_SEAM} to {EDGE_SEAM} (ground px); the halted screen {ARENA_SCROLL}.."
              f"{ARENA_SCROLL + SCREEN - 1} (t={ARENA_T:g}); the end's bottom edge {END_ROW}",
              "arena:   # for H2/H4: where the boss's arena starts and the station stands",
              f"  section: {SECTIONS[ARENA_INDEX]['name']}",
              f"  halt_t: {ARENA_T:g}",
              f"  scroll: {ARENA_SCROLL}            # the ground's scroll while halted (the bottom edge's row)",
              f"  centre_row: {CENTRE_ROW}        # the station's centre: screen ({CENTRE[0]}, {CENTRE[1]}) down = sim "
              f"({CENTRE[0]}, {SCREEN - CENTRE[1]})",
              f"  disc_enters_t: {round(time_at(CENTRE_ROW - 228 - SCREEN), 2):g}   # ART-H1's 456 px base disc's "
              "bottom edge reaches the top edge (it scrolls in with the ground)",
              f"  neck: [{NECK[0]}, {NECK[1]}]     # the neck under the ship band in the arena (screen y > 388)"]
    lines += ["ground_targets:  # at: [t, x], when the target enters at the top edge and its x (on the pads; a nest 44 px,",
              "  # a bunker 72 px, the lifeboat 32 px tall: tools/art/l35_props.py `ceres-lifeboat`, 20 HP, 28 on hard,",
              "  # the crate 100). The t=70 pair staggered 1 s (their lines never both on the ship's row); the t~56",
              "  # pair at 55.5 (off the screen by t=59.6, the flash at 61). Every x >= 80 px inside the field."]
    for grp in targets():
        lines.append(f"  - target: {grp['target']}")
        lines.append(f"    section: {grp['section']}")
        if grp.get("enemy"):
            lines += [f"    enemy: {grp['enemy']}", f"    count: {grp['count']}"]
        else:
            lines += ["    sprite: ceres-lifeboat", "    size: [56, 32]", "    hp: 20   # hard 28"]
        lines.append("    at: [" + ", ".join(f"[{t:g}, {x}]" for t, x in grp["at"]) + "]")
    bt, bx, by = BEACON
    lines += ["drops:   # the CDF beacon: Level 33's nav buoy (tools/art/belt_units_f.py `nav-buoy`; H4 adds it to "
              "BuoyLooks.MOORED as F4 did) on the crashed loyalist fighter (`beacon-wreck`)",
              f"  - {{t: {bt:g}, x: {bx}, y: {SCREEN - by}, pickup: armour patch, sprite: nav-buoy}}"]
    lines += debris_lines()
    return "\n".join(lines) + "\n"


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["ground_targets"], data["drops"]


# =========================================================================== composite, review, check

def piece_images(name, spec):
    n = spec.get("frames") or spec.get("headings")
    if n:
        return [Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)]
    return [Image.open(OUT / f"{name}.png").convert("RGBA")]


_CACHE = {}


def images(name, spec):
    if name not in _CACHE:
        _CACHE[name] = piece_images(name, spec)
    return _CACHE[name]


def tile_rows(name, height, offset):
    key = "tile:" + name
    if key not in _CACHE:
        _CACHE[key] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[key]
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[height - 1 - (rows % height)]


def section_at(t):
    for i, s in enumerate(SECTIONS):
        if t < float(s["end"]):
            return i
    return len(SECTIONS) - 1


def place_of(p, spec, t):
    """(x, y_down of the centre, frame index) of a placed piece at level time t."""
    f = FACTORS[spec["layer"]]
    centre = scroll(float(p["t"])) * f + SCREEN / 2
    k = int(t * spec["fps"]) % spec["frames"] if spec.get("frames") else 0
    return p["x"], scroll(t) * f + SCREEN - centre, k


def blit(img, p, pieces, t, layer):
    spec = pieces[p["piece"]]
    if spec["layer"] != layer:
        return img
    x, y, k = place_of(p, spec, t)
    ims = images(p["piece"], spec)
    frame = ims[k % len(ims)]
    if y - frame.height / 2 > SCREEN or y + frame.height / 2 < 0:
        return img
    return l23kit.paste_c(img, frame, x, y)


def ground_tile(img, t):
    """The hull tile where the section that holds the screen's rows lays it: from the seam into section 3 to the seam
    out of it (the game: a section's tile set begins at the seam entering at the top edge when it starts)."""
    g = scroll(t)
    arr = tile_rows("sovereign-hull", TILE_H, g)
    rows = int(round(g)) + SCREEN - 1 - np.arange(SCREEN)
    on = (rows >= HULL_SEAM) & (rows < EDGE_SEAM)
    a = np.array(img)
    a[on] = arr[on]
    return Image.fromarray(a, "RGBA")


def _held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def station(t):
    """ART-H1's station (its held sprites) at the ground's place: centred on CENTRE_ROW, turning slowly."""
    try:
        import iron_sovereign as isv  # noqa: E402  (tools/art: ART-H1's composer, imported unchanged)
        angles = {"outer": 6 * t, "middle": -10 * t, "inner": 0.0}
        lay = isv.station_layer(angles, size=(480, 1000), centre=(240, 500))
    except Exception as e:                                          # noqa: BLE001  (H1 mid-edit: the base alone)
        print(f"(station stand-in: {type(e).__name__}: {e})")
        base = _held("iron-sovereign-base")
        if not base:
            return None
        lay = Image.new("RGBA", (480, 1000))
        lay.alpha_composite(base[0], (240 - base[0].width // 2, 500 - base[0].height // 2))
    return lay


_STATION = {}


def station_stand_in(img, t):
    y = scroll(t) + SCREEN - CENTRE_ROW          # screen y of the centre
    if y < -500 or y > SCREEN + 500:
        return img
    key = round(t, 1) if t < ARENA_T else ARENA_T
    if key not in _STATION:
        _STATION[key] = station(min(t, ARENA_T + 8))
    lay = _STATION[key]
    if lay is None:
        return img
    return l23kit.paste_c(img, lay, 240, y)


def ground_stand_ins(img, t, targets_, drops):
    base, rack = _held("sam-nest-base"), _held("sam-nest")
    bunker, tur = _held("rail-bunker"), _held("rail-bunker-turret")
    boat = _held("ceres-lifeboat")
    for grp in targets_:
        h = NEST if grp.get("enemy") == "sam-nest" else BUNKER if grp.get("enemy") else BOAT
        for nt, x in grp["at"]:
            y = scroll(t) - scroll(nt) - h / 2
            if not -80 < y < SCREEN + 80:
                continue
            if grp.get("enemy") == "sam-nest" and base and rack:
                img = l23kit.paste_c(img, base[0], x, y)
                img = l23kit.paste_c(img, rack[0], x, y)
            elif grp.get("enemy") == "rail-bunker" and bunker and tur:
                img = l23kit.paste_c(img, bunker[0], x, y)
                img = l23kit.paste_c(img, tur[24 if x < 240 else 8], x, y)
            elif not grp.get("enemy") and boat:
                img = l23kit.paste_c(img, boat[int(t * 4) % len(boat)], x, y)
    buoy = _held("nav-buoy")
    for d in drops:
        y = SCREEN - d["y"] + scroll(t) - scroll(d["t"])
        if buoy and -40 < y < SCREEN + 40:
            img = l23kit.paste_c(img, buoy[0 if t < d["t"] else 2], d["x"], y)
    return img


def relay_stand_in(img, t, variant="a"):
    """The relay's telegraph and flash (tools/art/l35_props.py's held sprites) through the ship's row."""
    try:
        import l35_props as lp  # noqa: E402
    except ImportError:
        return img
    return lp.relay_overlay(img, t, 440, variant)


def compose(t, pieces, placed, targets_=None, drops=(), units=True):
    """The screen at level time t (480x540): the deep tile and pieces, far, the hull tile and the ground pieces, the
    ground targets and the station (stand-ins), low-air, the ship, high-air grit."""
    g = scroll(t)
    img = Image.fromarray(tile_rows("orbit-space", SPACE_H, g * FACTORS["deep"]), "RGBA")
    for layer in ("deep", "far"):
        for p in placed:
            img = blit(img, p, pieces, t, layer)
    img = ground_tile(img, t)
    for p in placed:
        img = blit(img, p, pieces, t, "ground")
    if units:
        img = ground_stand_ins(img, t, targets_ or [], drops)
        img = station_stand_in(img, t)
    for p in placed:
        img = blit(img, p, pieces, t, "low-air")
    if units:
        img = l23kit.paste_c(img, artkit.load_frames("ship")[2], 240 + 140 * np.sin(t * 0.5), 440)
        if 59.8 <= t <= 63:
            img = relay_stand_in(img, t)
    dust = Image.fromarray(tile_rows("orbit-dust", DUST_H, g * FACTORS["high-air"]), "RGBA")
    img.alpha_composite(dust)
    return img


SHOTS = [(6.0, "CERES ORBIT: THE CURVE, THE HUB RED, WRECKS"), (30.0, "ESCORT SCREEN: TENDERS FAR BELOW"),
         (49.0, "THE HULL'S SOUTH BULKHEAD"), (55.5, "THE HULL: NESTS, A BUNKER PAIR"),
         (60.6, "THE RELAY'S TELEGRAPH (A)"), (67.0, "THE LIFEBOAT IN ITS CLAMPS"),
         (84.0, "THE BULKHEAD, THE NECK, THE RINGS ROLL IN"), (ARENA_T + 0.5, "THE HALT: THE STATION OVER CERES")]


def review():
    pieces, placed, targets_, drops = load_proposal()
    shots = [(compose(t, pieces, placed, targets_, drops), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    names = ["sam-pad", "bunker-pad", "lifeboat-clamps", "beacon-wreck", "fragment-a", "tender-a", "docking-spar",
             "patrol-boat-wreck", "loyalist-wreck-a", "red-blink"]
    pieces_h = 170
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + pieces_h + 30 + 340,
                         "LEVEL 35 BACKDROP (FINAL R68): CERES ORBIT, THE SOVEREIGN'S HULL",
                         f"PRODUCTION ART, {BATCH.upper()} - R68")
    raster.draw_text(sheet, 16, 38, "EIGHT SCREENS AT HALF SIZE WITH STAND-INS (NESTS, BUNKERS, THE LIFEBOAT, THE BEACON, "
                                    "ART-H1'S STATION, THE RELAY LINE, THE SHIP)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 10
    raster.draw_text(sheet, 16, y, "THE SMALL PIECES AT 1X ON THE HULL TILE", raster.LABEL)
    x = 16
    tile = Image.open(OUT / "sovereign-hull.png").convert("RGBA")
    for name in names:
        fr = images(name, pieces[name])[0]
        if x + fr.width + 8 > sheet.width - 16:
            break
        cell = tile.crop((40, 300, 40 + fr.width + 8, 300 + fr.height + 8)) if pieces[name]["layer"] == "ground" else \
            Image.open(OUT / "orbit-space.png").convert("RGBA").crop((0, 0, fr.width + 8, fr.height + 8))
        cell.alpha_composite(fr, (4, 4))
        sheet.alpha_composite(cell, (x, y + 16))
        x += fr.width + 12
    y += pieces_h + 10
    raster.draw_text(sheet, 16, y, "THE HULL TILE, HULL-END AND CERES AT A THIRD; A 1:1 CROP OF THE TILE", raster.LABEL)
    y += 16
    x = 16
    for name in ("sovereign-hull", "hull-start", "hull-end", "ceres-curve", "ceres-pole"):
        im = Image.open(OUT / f"{name}.png").convert("RGBA")
        sm = im.resize((im.width // 3, im.height // 3), Image.BOX)
        if sm.height > 300:
            sm = sm.crop((0, 0, sm.width, 300))
        bg = Image.new("RGBA", sm.size, (9, 9, 14, 255))
        bg.alpha_composite(sm)
        sheet.alpha_composite(bg, (x, y))
        x += sm.width + 12
    crop = tile.crop((0, 200, sheet.width - x - 16 if sheet.width - x - 16 < 480 else 480, 500))
    sheet.alpha_composite(crop, (x, y))
    artkit.REVIEW_ROUND = ROUND
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    import enemies_r60 as e60  # noqa: E402  (concept script: the GIF writer)
    frames = [compose(82 + i / 7, pieces, placed, targets_, drops).resize((sw, sh), Image.BOX) for i in range(78)]
    size = e60.rig.write_gif(frames, gif, fps=7, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def check():
    """The tiles wrap (BackdropSeamsTest's measure) and the deep and hull tiles are opaque, sizes match the proposal,
    Source chunks, the deep pieces' top and bottom rows transparent, the small ground pieces' top and bottom rows clear,
    the hull ends' overlap rows equal the tile's, every placement on the screen, at most two animated pieces on the
    screen, every target on the hull's opaque pixels and on its pad, >= 40 px inside the field."""
    problems = []
    pieces, placed, targets_, drops = load_proposal()
    for name in ("orbit-space", "sovereign-hull"):
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if l30.wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {l30.wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    for f in OUT.glob("*.png"):
        if Image.open(f).text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
    for name, spec in pieces.items():
        for im in piece_images(name, spec):
            if list(im.size) != list(spec["size"]):
                problems.append(f"{name}: {im.size} vs {spec['size']}")
            al = np.array(im)[..., 3] > 0
            if spec["layer"] == "deep" and (al[0].any() or al[-1].any()):
                problems.append(f"{name}: an opaque top or bottom row on the deep (base) layer")
            if spec["layer"] == "deep" and im.width < W and (al[:, 0].any() or al[:, -1].any()):
                problems.append(f"{name}: an opaque side column on the deep layer")
            if spec["layer"] == "ground" and name not in ("hull-start", "hull-end") and (al[0].any() or al[-1].any()):
                problems.append(f"{name}: an opaque top or bottom row on the ground")
    tile = np.array(Image.open(OUT / "sovereign-hull.png").convert("RGBA"))
    for name, rng_ in (("hull-start", (HULL_SEAM - 4, HULL_SEAM + 30)), ("hull-end", (EDGE_SEAM - 30, EDGE_SEAM + 4))):
        p = next(q for q in placed if q["piece"] == name)
        im = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        h = im.shape[0]
        bottom = int(round(scroll(float(p["t"])) + SCREEN / 2 - h / 2))
        for r in range(*rng_):
            i = bottom + h - 1 - r
            if not 0 <= i < h:
                problems.append(f"{name}: does not cover row {r} (rows {bottom}..{bottom + h - 1})")
                break
            if (im[i] != tile[tile_index(r)]).any():
                problems.append(f"{name}: row {r} differs from the tile's")
                break
    seen = set()
    worst = (0, 0.0)
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = 0
        for i, p in enumerate(placed):
            spec = pieces[p["piece"]]
            _, y, _ = place_of(p, spec, t)
            if y + spec["size"][1] / 2 > 0 and y - spec["size"][1] / 2 < SCREEN:
                seen.add(i)
                animated += bool(spec.get("frames"))
        worst = max(worst, (animated, t))
    if worst[0] > 2:
        problems.append(f"t={worst[1]:.1f}: {worst[0]} animated pieces on the screen")
    for i, p in enumerate(placed):
        if i not in seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")
    pads = {"sam-nest": "sam-pad", "rail-bunker": "bunker-pad", None: "lifeboat-clamps"}
    for grp in targets_:
        h = NEST if grp.get("enemy") == "sam-nest" else BUNKER if grp.get("enemy") else BOAT
        for nt, x in grp["at"]:
            if not 40 <= x <= W - 40:
                problems.append(f"{grp['target']} x {x}: not 40 px inside the field")
            row = target_row(nt, h)
            if not HULL_SEAM + 40 <= row < EDGE_SEAM - 40:
                problems.append(f"{grp['target']} (t {nt}): row {row:.0f} off the hull tile")
            pad = pads[grp.get("enemy")]
            if not any(q["piece"] == pad and q["x"] == x and abs(scroll(float(q["t"])) + SCREEN / 2 - row) < 1.5
                       for q in placed):
                problems.append(f"{grp['target']} x {x} (t {nt}): no {pad} under it")
            if grp.get("enemy") == "rail-bunker" and nt + (SCREEN + h) / 150 > 61 and nt < 61:
                problems.append(f"{grp['target']} (t {nt}): on the screen at the relay's flash (t 61)")
    for msg in problems:
        print("PROBLEM", msg)
    px = sum(Image.open(f).width * Image.open(f).height for f in OUT.glob("*.png"))
    print(f"{len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}, {px / 1e6:.2f} M px "
          f"(~{px / 2048 ** 2:.2f} pages); {len(problems)} problems")
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
