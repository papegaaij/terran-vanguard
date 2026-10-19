#!/usr/bin/env python3
"""Production art: the Level 16 backdrop, Valles Canyon Run (design/campaign/act-3-red-dust/
level-16-valles-canyon-run; M7 part C batch, concept round 39, step ART-C).

Straight to production from the chosen canyon walls **b** of round 36 (`canyon-walls-r36-b`, MA.1:
the canyon floor is the ground at 1.0 and the collision line is the walls' foot; the walls rise toward
the camera to the plateau at 1.25, leaning outward round (240, 297) like Level 08's towers) and the
Valles mood still `mars-valles-r36-a` (tools/concept/mars_r36.py, imported unchanged: the Mars ramps,
the strata colours, the Helix site model). Lit from the upper left, receding (rule 3), posterized per
image, wide gradients ordered-dithered. Nothing lit is mirrored.

**The walls are not pieces.** Their geometry is the level's `terrain` (Sortie.terrain(), never a placed
art piece, so art and collision cannot drift); the game (step G1, WallProjection) projects these
textures onto it each frame:

  canyon-wall   128x1280 (round 40, step CW: "make it look more like a real canyon", the Grand Canyon from
                above): the wall descends from the rim in steps (the canyon model below, CanyonProfile's twin:
                SEGMENTS, cliff bands and talus benches, each set back from the collision line toward the rock,
                the side canyons biting back into the plateau). Its columns are the steps' strata from the
                foot (column 0) to the rim (127): the dark inner gorge (24), its grey-brown talus bench (20),
                the red-orange middle cliffs (32), the red talus slope (24), the pale cap cliff (16), the cream
                cap slope with its lip (12); its rows run along the wall (the arc length of each step's middle
                contour), row 0 of the 1,024-row period at the bottom, the top 256 rows repeating the period's
                last ones. Cliffs fluted, strata lines along the wall; benches talus. Not lit here: the game
                shades each step by its normal (the key light from the upper left: the left wall's cliffs in
                deep shadow, its benches partly in their cast shadow, the right wall's lit).
  wall-foot     24x512, vertically tiling the same way: the 24 px foot band on the canyon floor along
                the collision line, drawn at 1.0 without lean (the README's "wall's foot"): column 0 is
                the collision line (a 2 px near-black crag line), columns 2-4 the lit scree edge, then
                scree and boulders thinning into the floor, fading out by column 23 (ordered-dithered
                1-bit alpha). Lit from straight up the screen only, so the right wall's band is this
                image mirrored in x (column 0 at its collision line) with nothing lit wrong.
  mesa-face     128x528 and pillar-face 128x272: the buttes' steps, canyon-wall's columns; rows along the
                perimeter (period 512 / 256, the top 16 rows repeating its last ones).
  mesa-top_0..2 the fork mesa's cap at 1.25 (the butte's last ring, CanyonProfile.Butte) on the canvas of the
                island's polygon (README: x 190-290, scroll 12,830-15,350) x 1.25 = 125x3150 px, cut into three
                1050-row slices, _0 the far (top) end; transparent outside the cap, a light lip on its
                upper-left edges. The game draws the three over the projected polygon's bounding box.
  pillar-top    the rock pillar's cap (x 200-250, scroll 19,200-19,476, a rounded block) likewise: 63x345.

Tile sets (480 wide, 960 tall, wrapping like every tile set): `noctis-chasm` (far, x0.6: the chasm
floor deep below Noctis's cracks, hazed), `noctis-plateau` (ground, section 1: the plateau east of
Lycus, cracking into the labyrinth's graben, the cracks transparent onto the far layer), `canyon-floor`
(ground, sections 2, 3, 5: the floor, wind ripples, a dry channel, boulders), `riverbed` (ground,
section 4, the Coprates rapids: braided scoured channels, streamlined bars pointing downstream, up
the screen, cobbles), `eos-chaos` (ground, section 6: the outflow's sand floor in the middle,
x 130-350, and the chaos terrain's jumbled blocks toward the edges: the Burrowers' sand), `plateau`
(layer `plateau`, x1.25: the plateau's pale top beyond the walls' rims, drawn only beyond them, with
dendritic gullies draining into the big side canyons, which repeat with the tile), `dust-veil` (low-air, the `light` look's banks, ~10 %, at most 30 %, along the canyon),
`narrows-dust` (low-air, `medium`: the narrows' dust, ~25 %, at most 45 %), `dust-streaks` (high-air,
additive at 40 %: streaks blown along the canyon).

Pieces: `canyon-mouth` (ground, 480x1264, exact rows: the Noctis plateau's cracks closing into the
canyon floor just after the walls begin at scroll 2,850), `seam-rapids`, `seam-narrows`, `seam-eos`
(ground, 480x320, exact rows: the section seams); `cliff-road` (ground, the colonists' road along the
right wall's foot in the rapids, its shape from the wall), `habitat-pad` (ground, a burnt cliff
habitat's landing pad at a wall's foot: the turret pair's); `helix-station` (layer `plateau`: Helix's
research station on the north (right) plateau, black and gold, lit, unlabelled, untouched);
`cliff-habitat` (64x160, a FACE PATCH: a burnt colony habitat cut into the red middle cliff, windows, scorch
streaks, a balcony; the game draws it on that step of a wall in place of the texture's rows round the
placement, its columns from the cliff's foot to its top; the proposal lists them as placements of the
piece, the side by x);
`dust-plume` (low-air, a dust plume in the canyon, at most 45 %); `arch-a`, `arch-b` (high-air, two
rock arches over the canyon; their alpha is at most 53 %, so the game's overhead pass at 75 % (normal
blending, Backdrop.OVERHEAD_PASSES, which needs the two names) draws them at most 40 % opaque where
they cross the field).

Outputs: assets/backdrop/level-16/*.png; the review sheet and GIF
design/campaign/act-3-red-dust/level-16-valles-canyon-run/concept/backdrop-final-r39-a.png/.gif (the
review composites draw the canyon as the game does: the steps per wall, painted by height, the plateau
beyond the rims, the buttes, the cast shadows on the floor); `--proposal` writes the level's `backdrop:`
block to briefs/m7c-backdrop.yaml (for the main agent to merge; never into the level's data.yaml).

Reads the level's data.yaml when it exists (sections, terrain, ground targets), otherwise the README's
values (the Layout, the Wall table, Ground targets, the gates of Waves). Run:
python3 tools/art/backdrop_l16.py [--proposal | --review] [id ...]   (all: about 2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, blit, quantize, YAML flow)
import backdrop_l15 as b15  # noqa: E402  (tools/art: periodic noise, lighting, posterizing helpers)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps, strata, the Helix site)
from render import raster, sprite  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "backdrop_l16.py"
BATCH = "M7 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-16-valles-canyon-run"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = ROOT / "briefs" / "m7c-backdrop.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-16"
REVIEW = CONCEPT / "backdrop-final-r39-a.png"
HELD = ROOT / "build" / "art-held" / "sprites"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
CX, CY = 240.0, 297.0                   # the projection centre, px from the field's left and top edges
PLATEAU_K = 1.25                        # the plateau's scale (h 1.2 camera units)
WALL_TEX = (128, 1280)                  # TEX_W x (TEX_PERIOD + TEX_PAD)
FOOT_TEX = (24, 512)
MESA_FACE_TEX = (128, 512)
PILLAR_FACE_TEX = (128, 256)
MESA_SLICES = 3

FACTORS = {"far": 0.6, "ground": 1.0, "plateau": PLATEAU_K, "low-air": 1.35, "high-air": 2.2}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
SKY = np.array(m36.SKY, float)
RAMP = 3
ATMOSPHERE = {
    "clear": {"haze": 0.06},                                 # the outflow's clear air from t=178
    "light": {"banks": "dust-veil", "haze": 0.12},           # thin dust along the canyon
    "medium": {"banks": "narrows-dust", "haze": 0.2},        # the narrows' dust (and the rapids' 110-120)
}
RUST, STRATA, DUST, BASALT = m36.RUST, m36.STRATA, m36.DUST, m36.BASALT
GOLD = m36.GOLD

pn, aniso, sample, pdist, smooth, ramp = b15.pn, b15.aniso, b15.sample, b15.pdist, b15.smooth, b15.ramp
lit, recede, posterize_rgb, dither_alpha = b15.lit, b15.recede, b15.posterize_rgb, b15.dither_alpha
jr = k8.java_round


# =========================================================================== the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists ("each change eased over 1 s")
    {"name": "Noctis Edge", "end": 25, "speed": 190, "atmosphere": "light"},
    {"name": "Into the Canyon", "end": 60, "speed": 200, "atmosphere": "light", "ease": 1},
    {"name": "Fork One", "end": 95, "speed": 180, "atmosphere": "light", "ease": 1},
    {"name": "Coprates Rapids", "end": 125, "speed": 230, "atmosphere": "light", "ease": 1,
     "peak": {"atmosphere": "medium", "from": 110, "to": 120}},
    {"name": "The Narrows", "end": 160, "speed": 180, "atmosphere": "medium", "ease": 1},
    {"name": "Eos Outflow", "end": 190, "speed": 200, "atmosphere": "light", "ease": 1,
     "peak": {"atmosphere": "clear", "from": 178, "to": 190}},
]
SECTION_TILES = [["noctis-chasm", "noctis-plateau", "plateau", "dust-streaks"], ["canyon-floor", "plateau", "dust-streaks"],
                 ["canyon-floor", "plateau"], ["riverbed", "plateau", "dust-streaks"], ["canyon-floor", "plateau"],
                 ["eos-chaos", "plateau"]]


def _load(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


DATA = _load(LEVEL_DIR / "data.yaml")
SECTIONS = [dict(s) for s in (DATA.get("sections") or DEFAULT_SECTIONS)]
SPEED = float(DATA.get("scroll_speed", 200))
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
END = float(SECTIONS[-1]["end"])


def speed(i):
    return float(SECTIONS[i].get("speed", SPEED))


def ease(i):
    if i == 0 or speed(i) == 0 or speed(i - 1) == 0:
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
    """LevelData.scrollAt with the M7 part B ease (as backdrop_l15.scroll_at)."""
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
    """Where section s's tile set begins on the layer (as backdrop_l15.seam)."""
    return jr(scroll_at(STARTS[s]) * FACTORS[layer] + SCREEN)


def t_for_bottom(layer, bottom, h):
    """A placement time whose piece (height h) has its bottom edge exactly at layer position ``bottom``."""
    f = FACTORS[layer]
    t = round(t_at((bottom + h / 2 - MID) / f), 4)
    for dt in (0, 1e-4, -1e-4, 2e-4, -2e-4, 3e-4, -3e-4):
        if jr(scroll_at(t + dt) * f + MID - h / 2) == bottom:
            return round(t + dt, 4)
    raise AssertionError((layer, bottom, h, t))


def t_for_row(row):
    """The time a ground piece centred on terrain row ``row`` (the scroll at which it reaches the top
    edge) has to be placed at: its centre passes the middle of the screen when the scroll is row + 270."""
    return round(t_at(row + MID), 3)


# =========================================================================== the terrain

DEFAULT_LEFT = [(2850, 0), (4180, 40), (5750, 70), (7750, 90), (9750, 80), (11150, 70), (12470, 40),
                (12830, 40), (15350, 40), (16070, 60), (18050, 70), (19200, 70), (19476, 70), (20350, 90),
                (21500, 50), (22420, 40), (23800, 60), (24950, 90), (26210, 110), (27650, 150), (29090, 150),
                (29810, 130), (31250, 80), (33250, 30), (34850, 0)]
DEFAULT_RIGHT = [(2850, 480), (4180, 440), (5750, 410), (7750, 390), (9750, 380), (11150, 400), (12470, 440),
                 (12830, 440), (15350, 440), (16070, 420), (18050, 400), (19200, 400), (19476, 400),
                 (20350, 370), (21500, 330), (22420, 320), (23800, 340), (24950, 390), (26210, 370),
                 (27650, 330), (29090, 330), (29810, 350), (31250, 400), (33250, 450), (34850, 480)]
HEIGHT_RAMP = [(2850, 0.0), (4180, 1.0), (31250, 1.0), (34850, 0.0)]   # the mouth and the outflow (risk 9)
DEFAULT_ISLANDS = {
    "mesa": [(240, 12830), (190, 13370), (190, 14810), (240, 15350), (290, 14810), (290, 13370)],
    "pillar": None,                         # a rounded block x 200-250, scroll 19,200-19,476
}
PILLAR_BOX = (200, 250, 19200, 19476, 20)


def rounded_box(x0, x1, s0, s1, r, n=6):
    """A rounded rectangle's polygon [x, scroll], clockwise seen from above, from its top-left corner."""
    pts = []
    corners = [(x0 + r, s1 - r, 180), (x1 - r, s1 - r, 90), (x1 - r, s0 + r, 0), (x0 + r, s0 + r, 270)]
    for cx_, cs, a0 in corners:
        for k in range(n + 1):
            a = math.radians(a0 - 90 * k / n)
            pts.append((cx_ + r * math.cos(a), cs + r * math.sin(a)))
    return pts


def _terrain():
    t = DATA.get("terrain") or {}

    def wall(key, default):
        pts = t.get(key)
        if not pts:
            return [(float(s), float(x), None) for s, x in default]
        return [(float(p[0]), float(p[1]), float(p[2]) if len(p) > 2 else None) for p in pts]
    islands = {}
    for i, isl in enumerate(t.get("islands") or []):
        pts = isl.get("points") if isinstance(isl, dict) else isl
        name = (isl.get("look") if isinstance(isl, dict) else None) or ("mesa" if i == 0 else "pillar")
        islands[name] = [(float(x), float(s)) for x, s in pts]
    if "mesa" not in islands:
        islands["mesa"] = [(float(x), float(s)) for x, s in DEFAULT_ISLANDS["mesa"]]
    if "pillar" not in islands:
        islands["pillar"] = rounded_box(*PILLAR_BOX)
    return wall("left", DEFAULT_LEFT), wall("right", DEFAULT_RIGHT), islands


LEFT, RIGHT, ISLANDS = _terrain()


def wall_x(wall, rows):
    """The collision line's x at terrain rows (NaN: no wall there)."""
    s = np.array([p[0] for p in wall])
    x = np.array([p[1] for p in wall])
    out = np.interp(rows, s, x)
    return np.where((rows < s[0]) | (rows > s[-1]), np.nan, out)


def wall_h(wall, rows):
    """The wall's height share (the data's third value where given, else the mouth/outflow ramp)."""
    if any(p[2] is not None for p in wall):
        s = np.array([p[0] for p in wall])
        h = np.array([1.0 if p[2] is None else p[2] for p in wall])
        return np.interp(rows, s, h)
    s, h = zip(*HEIGHT_RAMP)
    return np.interp(rows, s, h)


def k_of(u, h):
    """The scale of a point a share u of the way up a wall of height share h (TowerProjection)."""
    return 6.0 / (6.0 - 1.2 * h * u)


# =========================================================================== the canyon model (round 40, step CW)
# The twin of the game's CanyonProfile (game/src/main/java/vanguard/game/render/CanyonProfile.java): the same
# constants, hash and functions, so the review composites, the plateau's gullies and the islands' tops match what
# the game draws. Change one, change the other.
#
# Seen from above like the Grand Canyon: each wall descends from the rim in steps (SEGMENTS), steep cliff bands
# and talus benches, each its own stratum (the pale cream cap, the red-orange middle cliffs, the dark inner gorge
# at the foot). Every step's edge is set back from the collision line (the foot, never moved) toward the rock by
# a setback that grows with the room between the wall and the field's edge and with the side canyons (notches,
# three sizes, the big ones repeating every PERIOD rows so the plateau tile's gullies drain into them), so the
# rim is ragged and the strata run round the amphitheatres as contour bands. Lit from the upper left: the left
# wall's cliffs in deep shadow and its benches partly in their cast shadow, the right wall's cliffs lit.

WALL_PX = 120.0                         # a full wall's rise in px (the normals, the cast shadows)
SHADOW_RATIO = 0.45                     # px of cast shadow per px of rise
SHADOW_DIR = (0.82, -0.57)              # the cast shadows' direction: x right, terrain rows up (down-right)
LIGHT = np.array((-0.66, 0.46, 0.60)) / np.linalg.norm((-0.66, 0.46, 0.60))   # toward the key light: x, rows, z up
AMBIENT, DIFFUSE = 0.45, 0.55
FLAT = 0.9                              # a flat bench's shade; the lit cliffs clamp at 1
PERIOD = 768                            # rows per plateau tile (960 / 1.25): the big side canyons repeat with it
PLATEAU_ROW0 = 614.25                   # the plateau layer's row of the rim of terrain row S is 1.25 S + this
TEX_W, TEX_PERIOD, TEX_PAD = 128, 1024, 256
FACE_PAD = 16                           # the buttes' faces: rows repeated above the period (perimeter steps <= 3 px)
# The wall's steps from the foot up to the rim: (kind, rise share, setback px, the side canyons' share at its top
# edge, texture columns).
SEGMENTS = (
    ("cliff", 0.36, 3.0, 0.10, 24),     # the dark inner gorge
    ("bench", 0.05, 14.0, 0.30, 20),    # its talus bench
    ("cliff", 0.30, 4.0, 0.50, 32),     # the red-orange middle cliffs
    ("bench", 0.07, 16.0, 0.80, 24),    # the red talus slope
    ("cliff", 0.16, 3.0, 0.92, 16),     # the pale cap cliff
    ("bench", 0.06, 8.0, 1.00, 12),     # the cream cap slope up to the rim
)
NODES = len(SEGMENTS) + 1
NODE_Z = np.concatenate([[0.0], np.cumsum([s[1] for s in SEGMENTS])])
NODE_U = np.concatenate([[0.0], np.cumsum([s[4] for s in SEGMENTS])]) / TEX_W
NODE_SHARE = np.array([0.0] + [s[3] for s in SEGMENTS])
HABITAT_SEGMENT = 2                     # a face patch covers the red middle cliff
ISLAND_SCALE = {"mesa": 0.72, "pillar": 0.36}   # a butte's steps against a wall's (no side canyons)
M32 = 0xFFFFFFFF


def hash01(i, seed):
    """[0, 1) from integers (CanyonProfile.hash)."""
    h = (np.asarray(i, dtype=np.int64) * 0x9E3779B1 + seed * 0x85EBCA77) & M32
    h = h ^ (h >> 15)
    h = (h * 0x2C1B3C6D) & M32
    h = h ^ (h >> 12)
    h = (h * 0x297A2D39) & M32
    h = h ^ (h >> 15)
    return h / 4294967296.0


def vnoise(x, cell, seed):
    f = np.asarray(x, float) / cell
    i = np.floor(f).astype(np.int64)
    t = f - i
    t = t * t * (3 - 2 * t)
    return hash01(i, seed) * (1 - t) + hash01(i + 1, seed) * t


def fbm1(x, cell, seed, octaves=3):
    """0..1, octaves halving the cell."""
    total, amp, norm = 0.0, 1.0, 0.0
    for o in range(octaves):
        total = total + amp * vnoise(x, cell / (1 << o), seed + 101 * o)
        norm += amp
        amp *= 0.5
    return total / norm


def bump(u):
    u = np.asarray(u, float)
    return np.where(np.abs(u) < 1, np.clip(1 - u * u, 0, 1) ** 0.7, 0.0)


NOTCH_OCTAVES = ((112, 0.7, 8, 12, 8, 18, 5), (40, 0.6, 4, 5, 3, 6, 9))   # cell, presence, width, depth, salt


def big_notches(side):
    """The big side canyons in one PERIOD: (centre row, half width, depth px)."""
    base = 40 + 20 * side
    return [((k + 0.2 + 0.6 * hash01(k, base + 1)) * PERIOD / 4, 24 + 26 * hash01(k, base + 2),
             30 + 34 * hash01(k, base + 3)) for k in range(4)]


def notches(side, rows):
    """The side canyons biting back into the plateau at terrain rows: px of extra setback at the rim."""
    rows = np.asarray(rows, float)
    out = np.zeros_like(rows)
    base = 40 + 20 * side
    for c, w, d in big_notches(side):
        du = (rows - c + PERIOD / 2) % PERIOD - PERIOD / 2
        out += d * bump(du / w)
    for cell, present, w0, w1, d0, d1, salt in NOTCH_OCTAVES:
        k0 = np.floor(rows / cell).astype(np.int64)
        for dk in (-1, 0, 1):
            k = k0 + dk
            on = hash01(k, base + salt) < present
            c = (k + 0.15 + 0.7 * hash01(k, base + salt + 1)) * cell
            w = w0 + w1 * hash01(k, base + salt + 2)
            d = d0 + d1 * hash01(k, base + salt + 3)
            out += np.where(on, d * bump((rows - c) / w), 0.0)
    return out


def room_factor(side, foot):
    """The benches' width against the room between the wall's foot and the field's edge."""
    room = foot if side == 0 else W - foot
    return np.clip((room - 40) / 60, 0.3, 1.4)


def setbacks(side, rows, foot, height, cap=None, calm=None):
    """Each step edge's setback (px from the foot toward the rock) at terrain rows: (len(rows), NODES)."""
    r = room_factor(side, foot)
    n = notches(side, rows) * r
    if calm is not None:
        n = n * (1 - calm)
    s = np.zeros((len(rows), NODES))
    for g, (kind, _, base, _, _) in enumerate(SEGMENTS):
        wobble = fbm1(rows, 48, 300 + 20 * side + g)
        inc = base * r * (0.6 + 0.8 * wobble) if kind == "bench" else base * (0.7 + 0.6 * wobble)
        s[:, g + 1] = s[:, g] + inc + (NODE_SHARE[g + 1] - NODE_SHARE[g]) * n
    if cap is not None:
        s = s * np.minimum(1, cap / np.maximum(s[:, -1], 1e-9))[:, None]
    return s * height[:, None]


def shade_of(n):
    """A surface's shade from its normal(s) (..., 3): the key light and the ambient, a flat bench FLAT."""
    n = n / np.linalg.norm(n, axis=-1, keepdims=True)
    lum = AMBIENT + DIFFUSE * np.clip(n @ LIGHT, 0, None)
    return np.clip(lum / (AMBIENT + DIFFUSE * LIGHT[2]) * FLAT, 0, 1)


SHADOWED = AMBIENT / (AMBIENT + DIFFUSE * LIGHT[2]) * FLAT


class CanyonWall:
    """A wall's steps along its rows (per whole row, interpolated between): the foot, the height share, the
    setbacks, each segment's v (the arc length of its middle contour) and the edges' x."""

    def __init__(self, side, pts, patches=(), caps=()):
        self.side = side
        s0, s1 = pts[0][0], pts[-1][0]
        self.r0 = int(math.floor(s0))
        rows = np.arange(self.r0, int(math.ceil(s1)) + 1, dtype=float)
        cl = np.clip(rows, s0, s1)
        self.foot = wall_x(pts, cl)
        self.h = np.clip(wall_h(pts, cl), 0, 1)
        calm = np.zeros_like(rows)
        for row in patches:
            calm = np.maximum(calm, np.clip((190 - np.abs(rows - row)) / 70, 0, 1))
        cap = None
        if caps:
            cap = np.full_like(rows, np.inf)
            for a, b, edge in caps:     # a plateau piece's rows and its edge toward the canyon (screen x)
                limit = ((edge - CX) / PLATEAU_K + CX - self.foot) * (1 if side == 1 else -1)
                out = np.maximum(0, np.maximum(a - rows, rows - b))
                cap = np.minimum(cap, np.maximum(0.5, limit - 4) + 1.5 * out)
        self.s = setbacks(side, rows, self.foot, self.h, cap, calm)
        sign = -1 if side == 0 else 1
        self.x = self.foot[:, None] + sign * self.s
        mid = (self.x[:, :-1] + self.x[:, 1:]) / 2
        dl = np.sqrt(1 + np.diff(mid, axis=0) ** 2)
        self.v = np.vstack([np.zeros((1, NODES - 1)), np.cumsum(dl, axis=0)])
        self.slope = np.gradient(mid, axis=0)
        self.rows = rows

    def at(self, rows, table):
        f = np.clip(np.asarray(rows, float) - self.r0, 0, len(self.rows) - 1.000001)
        i = np.floor(f).astype(int)
        t = f - i
        if table.ndim == 1:
            return table[i] * (1 - t) + table[i + 1] * t
        return table[i] * (1 - t)[:, None] + table[i + 1] * t[:, None]

    def valid(self, rows):
        rows = np.asarray(rows, float)
        return (rows >= self.rows[0]) & (rows <= self.rows[-1])

    def segment_shades(self, rows):
        """Per row and segment: the shade, and for a bench the share of it (from its back) in the cliff's
        cast shadow above it."""
        x = self.at(rows, self.x)
        s = self.at(rows, self.s)
        h = self.at(rows, self.h)
        slope = self.at(rows, self.slope)
        z = NODE_Z[None, :] * h[:, None] * WALL_PX
        dz = np.diff(z, axis=1)
        dx = np.diff(x, axis=1)
        n = np.stack([-dz, dz * slope, dx], axis=-1)
        n = np.where(n[..., 2:3] < 0, -n, n)
        n = np.where((np.abs(dx) + dz)[..., None] < 1e-9, np.array((0.0, 0.0, 1.0)), n)
        shade = shade_of(n)
        lh = LIGHT[:2] / np.linalg.norm(LIGHT[:2])
        cast = np.zeros_like(shade)
        for g in range(len(SEGMENTS) - 1):
            if SEGMENTS[g][0] != "bench":
                continue
            nh = n[:, g + 1, :2]
            facing = (nh @ lh) / np.maximum(np.linalg.norm(nh, axis=-1), 1e-9)
            width = np.maximum(s[:, g + 1] - s[:, g], 1e-6)
            reach = np.zeros(len(rows))
            for j in range(g + 2, NODES):
                reach = np.maximum(reach, (z[:, j] - z[:, g + 1]) * SHADOW_RATIO * np.clip(-facing, 0, 1)
                                   - (s[:, j] - s[:, g + 1]))
            cast[:, g] = np.clip(reach / width, 0, 1)
        return shade, cast

    def floor_shadow(self, rows):
        """The x the wall's cast shadow reaches on the floor at terrain rows (the foot: none)."""
        best = self.at(rows, self.foot)
        if self.side == 1:
            return best
        for j in range(1, NODES):
            hj = self.at(rows, self.h)
            reach = NODE_Z[j] * hj * WALL_PX * SHADOW_RATIO
            src = np.asarray(rows, float) + reach * -SHADOW_DIR[1]
            ok = self.valid(src)
            xj = self.at(src, self.x[:, j]) + reach * SHADOW_DIR[0]
            best = np.where(ok, np.maximum(best, xj), best)
        return best


def island_rings(name):
    """A butte's steps: the foot polygon resampled (x, row) every ~3 px, the perimeter coordinate, the inward
    offset directions (an edge's points: its corners' miters interpolated, so a ring at setback s is the
    convex polygon offset by s) and each step edge's ring (NODES, n, 2); the last ring is the cap's edge."""
    poly = ISLANDS[name]
    n = len(poly)
    area = sum(poly[i][0] * poly[(i + 1) % n][1] - poly[(i + 1) % n][0] * poly[i][1] for i in range(n))
    orient = 1 if area > 0 else -1        # counter-clockwise (x right, rows up): the inside on the left
    miters = []
    for i in range(n):
        (xp, sp), (x0, s0), (x1, s1) = poly[i - 1], poly[i], poly[(i + 1) % n]
        e0 = np.array((-(s0 - sp), x0 - xp)) / math.hypot(x0 - xp, s0 - sp) * orient
        e1 = np.array((-(s1 - s0), x1 - x0)) / math.hypot(x1 - x0, s1 - s0) * orient
        miters.append((e0 + e1) / (1 + float(e0 @ e1)))
    pts, per, nrm = [], [], []
    p = 0.0
    for i in range(n):
        (x0, s0), (x1, s1) = poly[i], poly[(i + 1) % n]
        seg = math.hypot(x1 - x0, s1 - s0)
        steps = max(1, int(round(seg / 3)))
        for k in range(steps):
            a = k / steps
            pts.append((x0 + (x1 - x0) * a, s0 + (s1 - s0) * a))
            per.append(p + seg * a)
            nrm.append(miters[i] * (1 - a) + miters[(i + 1) % n] * a)
        p += seg
    pts, per, nrm = np.array(pts), np.array(per), np.array(nrm)
    scale = ISLAND_SCALE[name]
    total = sum(sg[2] for sg in SEGMENTS) * scale
    cum = np.concatenate([[0.0], np.cumsum([sg[2] for sg in SEGMENTS])]) * scale
    seed = 700 + (0 if name == "mesa" else 50)
    rings = np.zeros((NODES, len(pts), 2))
    prev = np.zeros(len(pts))
    for j in range(NODES):
        if j == 0:
            sj = np.zeros(len(pts))
        elif j == NODES - 1:
            sj = np.full(len(pts), total)
        else:
            wob = vnoise(per, 20, seed + j) - 0.5
            sj = np.clip(cum[j] * (1 + 0.5 * wob), prev, total)
        rings[j] = pts + nrm * sj[:, None]
        prev = sj
    return pts, per, nrm, rings, p


def island_cap(name):
    """The cap's polygon (x, row): the last ring."""
    return island_rings(name)[3][-1]


# =========================================================================== ground targets (the pieces sit under them)

DEFAULT_TARGETS = {    # [t, x] (the README's Ground targets; the target's top edge at the top edge at t)
    "pad": [[104.0, 99.0], [104.4, 101.0]],                  # the Spine Turret pair on a burnt habitat's pad
    "rovers": [[107.0, 338.0], [107.5, 334.0], [108.0, 330.0], [108.5, 326.0]],   # the cargo rovers on the road
    "hut": [[71.0, 60.0]],                                   # the Candor relay hut (left channel, left foot)
    "turrets": [[43, 101], [43.4, 101], [43.8, 100], [70, 304], [70.4, 304], [70.8, 304], [170, 200]],
    "mortars": [[53, 375], [53.6, 376], [138, 155], [138.6, 322]],
}
DEFAULT_GATES = [(34, 90, 390), (46, 96, 372), (57, 82, 388), (58.5, 75, 397), (132, 123, 357), (133.5, 130, 350),
                 (144, 162, 318), (152, 141, 339), (153.5, 132, 348)]   # (t, left x, right x), medium


def _targets():
    """The ground targets the pieces sit under and the review draws, from the level's data when it exists."""
    gt = DATA.get("ground_targets") or []
    if not gt:
        return {k: [list(v) for v in vs] for k, vs in DEFAULT_TARGETS.items()}, list(DEFAULT_GATES)
    out = {k: [] for k in DEFAULT_TARGETS}
    gates = []
    for g in gt:
        at = [list(a) for a in (g.get("at") or [])]
        name, enemy, spr = str(g.get("target", "")), g.get("enemy"), str(g.get("sprite") or "")
        if g.get("link") == "pairs":
            gates += [(at[i][0], at[i][1], at[i + 1][1]) for i in range(0, len(at) - 1, 2)]
        elif spr.startswith("canyon-rover"):
            out["rovers"] += at
        elif spr.startswith("relay-hut"):
            out["hut"] += at
        elif "pad" in name:
            out["pad"] += at
        elif enemy == "spine-turret":
            out["turrets"] += at
        elif enemy == "polyp-mortar":
            out["mortars"] += at
    for k in out:
        if not out[k]:
            out[k] = [list(v) for v in DEFAULT_TARGETS[k]]
    return out, gates or list(DEFAULT_GATES)


TARGETS, GATES = _targets()


def row_of(t, h=0):
    """The terrain row of a target's centre: its top edge at the top edge at t."""
    return scroll_at(t) - h / 2


# =========================================================================== small helpers

def tex_rows(h):
    return np.arange(h, dtype=np.float64)[:, None]


def lit_up(height, albedo, ambient=0.5, key=0.55):
    """A relief lit from straight up the screen only (mirror-safe in x): rgb 0..255."""
    gy = (np.roll(height, -1, 0) - np.roll(height, 1, 0)) / 2
    shade = np.clip(ambient + key * (0.6 + gy * 0.5), 0.2, 1.4)
    return albedo * shade[..., None]


def finish(rgb, colours, alpha=None, amount=0.2):
    return posterize_rgb(recede(rgb, amount), colours, alpha)


# =========================================================================== far and the Noctis plateau

def noctis_chasm():
    """The far layer (x0.6): the chasm floor deep below the cracks: talus fans, dark rubble, hazed."""
    h, w = TILE_H, W
    rh, ralb = b15.regolith(w, h, 1611)
    n = pn(w, h, 96, 1613, 4)
    talus = smooth(0.45, 0.7, aniso(w, h, 1, 4, 24, 1615, 3))
    hgt = rh * 0.6 + 4 * n + 2 * talus
    alb = ralb * 0.78 + np.array(RUST[1], float) * 0.22
    rgb = lit(hgt, alb, ambient=0.48, key=0.5)
    rgb = rgb * 0.42 + np.array((70, 40, 30)) * 0.3                # deep below, in the graben's shadow
    return finish(rgb, 20, amount=0.3)


def crack_field(w, h, seed, n_long=5, n_cross=9):
    """Noctis's graben: long troughs wandering along the canyon's axis and short ones across, a depth field
    0..1 (periodic both ways)."""
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    depth = np.zeros((h, w))
    wob = pn(w, h, 48, seed + 1, 3)
    for path in b15.periodic_paths(h, n_long, seed, 30, 450, 90, cycles=(1, 2, 3)):
        d = b15.path_dist(path, yy, xx)
        width = 9 + 12 * pn(w, h, 96, seed + 3, 2)
        depth = np.maximum(depth, np.clip(1 - (d + 3 * (wob - 0.5)) / width, 0, 1))
    rng = np.random.default_rng(seed + 5)
    for _ in range(n_cross):
        cy, cx = rng.uniform(0, h), rng.uniform(0, w)
        length, ang = rng.uniform(60, 180), rng.uniform(-0.5, 0.5)
        width = rng.uniform(7, 14)
        dx = (xx - cx + w / 2) % w - w / 2
        dy = (yy - cy + h / 2) % h - h / 2
        along = dx * math.cos(ang) + dy * math.sin(ang)
        across = -dx * math.sin(ang) + dy * math.cos(ang)
        cap = np.clip(1 - (np.abs(along) - length / 2) / 12, 0, 1)
        depth = np.maximum(depth, np.clip(1 - (np.abs(across) + 3 * (wob - 0.5)) / width, 0, 1) * cap)
    return depth


def noctis_plateau():
    """Section 1's ground: the plateau east of Lycus cracking into the labyrinth: rust regolith, darker lava
    patches, small craters; the graben's walls lit and shaded, their floors transparent (the far layer)."""
    h, w = TILE_H, W
    rh, ralb = b15.regolith(w, h, 1621)
    depth = crack_field(w, h, 1623)
    craters, ej = b15.crater_field(w, h, 22, 3.0, 12.0, 1625, depth=1.0)
    patch = smooth(0.55, 0.62, pn(w, h, 160, 1627, 4))
    hgt = rh + craters - 14 * smooth(0.0, 0.8, depth)
    alb = ralb * (1 - 0.25 * patch[..., None]) + np.array(BASALT[2], float) * 0.25 * patch[..., None]
    alb += 12 * ej[..., None]
    rgb = lit(hgt, alb)
    alb_scale = np.array((0.9, 0.76, 0.68))                       # the plateau's red (as `plateau`)
    rgb = rgb * alb_scale
    alpha = np.where(depth > 0.5, 0, 255)
    return finish(rgb, 32, alpha)


PLATEAU_ROCK = [(152, 98, 60), (186, 130, 82), (208, 154, 100), (224, 174, 118), (236, 192, 136)]


def gully_field(w, h, seed):
    """Dendritic gullies draining into the big side canyons (CanyonProfile's notches repeat every PERIOD rows,
    the tile's 960 rows): per notch a channel from the field's edge to the notch's head, branching upstream;
    a depth field 0..1, periodic along the rows."""
    rng = np.random.default_rng(seed)
    img = Image.new("L", (w, h * 3), 0)
    d = ImageDraw.Draw(img)

    def channel(x, y, ang, length, width, depth_left):
        steps = max(2, int(length / 4))
        pts = [(x, y)]
        for i in range(steps):
            ang += rng.normal(0, 0.18)
            x += 4 * math.cos(ang)
            y += 4 * math.sin(ang)
            pts.append((x, y))
            if depth_left > 0 and i > 1 and rng.random() < 0.16:
                side = rng.choice((-1, 1))
                channel(x, y, ang + side * rng.uniform(0.5, 1.0), length * rng.uniform(0.3, 0.55),
                        max(1.0, width * 0.6), depth_left - 1)
        for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
            for off in (0, h, 2 * h):
                d.line([(x0, y0 + off), (x1, y1 + off)], fill=255, width=max(1, int(round(width))))
    for side in (0, 1):
        for c, wd, dep in big_notches(side):
            row = (PLATEAU_K * c + PLATEAU_ROW0) % h
            y = h - 1 - row
            if side == 0:
                channel(236, y, math.pi + rng.normal(0, 0.1), 236, 4.0, 3)
            else:
                channel(W - 236, y, rng.normal(0, 0.1), 236, 4.0, 3)
    a = np.array(img, float) / 255
    a = np.maximum(np.maximum(a[:h], a[h:2 * h]), a[2 * h:])
    soft = np.array(Image.fromarray((a * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.2)), float) / 255
    return np.clip(soft * 1.4, 0, 1)


def plateau_rock(w, h, seed):
    """The plateau's (and the buttes' caps') rock: pale butterscotch regolith, lighter than the canyon's floor
    (the cap stratum): height and albedo."""
    rh, _ = b15.regolith(w, h, seed)
    n = pn(w, h, 96, seed + 3, 3)
    t = np.clip(0.55 + 0.4 * (n - 0.5) + 0.03 * (rh - 2), 0, 1)
    return rh, ramp(PLATEAU_ROCK, t)


def plateau():
    """The `plateau` layer (x1.25): the flat plateau beyond the rims, pale butterscotch (the cap stratum), small
    craters, wind streaks, and dendritic gullies draining into the big side canyons (they repeat with the tile)."""
    h, w = TILE_H, W
    rh, alb = plateau_rock(w, h, 1631)
    craters, ej = b15.crater_field(w, h, 18, 3.0, 12.0, 1635, depth=0.8)
    streak = smooth(0.62, 0.7, aniso(w, h, 1, 6, 16, 1637, 2)) * 0.5
    gul = gully_field(w, h, 1639)
    hgt = rh * 0.8 + craters - 4.5 * gul
    alb = alb + 10 * ej[..., None] + 8 * streak[..., None]
    alb = alb * (1 - 0.22 * gul[..., None])
    rgb = lit(hgt, alb, ambient=0.5, key=0.62)
    return finish(rgb, 32, amount=0.12)


# =========================================================================== the canyon floor

def canyon_floor():
    """Sections 2, 3, 5: the floor: rust sand with wind ripples along the canyon, a dry channel wandering
    near the middle, scattered boulders and small craters."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rh, ralb = b15.regolith(w, h, 1641)
    warp = pn(w, h, 48, 1643, 3)
    ripple = np.sin(xx * 0.28 + 2 * np.pi * 12 * yy / h + 9 * warp) * (0.25 + 0.5 * pn(w, h, 96, 1645, 2))   # whole cycles: wraps
    chan = np.zeros((h, w))
    for path in b15.periodic_paths(h, 1, 1647, 220, 260, 26, cycles=(1, 2, 3)):
        d = b15.path_dist(path, yy, xx)
        chan += -2.5 * np.exp(-(d / 7) ** 2) + 0.8 * np.exp(-((d - 9) / 2.5) ** 2)
    craters, ej = b15.crater_field(w, h, 10, 2.5, 8.0, 1649, depth=0.8)
    bould = (np.random.default_rng(1651).random((h, w)) > 0.993) * 2.2
    bould = np.maximum(bould, np.roll(bould, 1, 1) * 0.7)
    hgt = rh + ripple + chan + craters + bould
    sand = ramp([(150, 94, 58), (184, 126, 82), (210, 156, 106), (226, 180, 128)],
                np.clip(0.5 + 0.3 * (pn(w, h, 96, 1653, 3) - 0.5) + 0.15 * ripple, 0, 1))
    alb = ralb * 0.35 + sand * 0.65 + 10 * ej[..., None]
    alb = np.where((chan < -1.2)[..., None], alb * 0.84, alb)
    rgb = lit(hgt, alb)
    return finish(rgb, 32)


def riverbed():
    """Section 4, the Coprates rapids: braided scoured channels across the floor, streamlined bars with their
    blunt ends downstream-facing (the old flow ran east: up the screen), cobble fields, boulders."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rh, ralb = b15.regolith(w, h, 1661)
    scour = np.zeros((h, w))
    for path in b15.periodic_paths(h, 7, 1663, 50, 430, 70, cycles=(1, 2, 4)):
        d = b15.path_dist(path, yy, xx)
        scour = np.minimum(scour, -2.8 * np.exp(-(d / 10) ** 2))
    bars = np.zeros((h, w))
    rng = np.random.default_rng(1665)
    for _ in range(16):
        bx, by = rng.uniform(40, 440), rng.uniform(0, h)
        L, Wd = rng.uniform(50, 110), rng.uniform(12, 22)
        dy = (yy - by + h / 2) % h - h / 2
        u = -dy / L                     # screen y down: u > 0 up the screen (downstream), the tail
        width = np.where(u < 0, Wd * np.clip(1 + 4 * u, 0, 1) ** 0.5, Wd * np.clip(1 - u, 0, 1) ** 1.5)
        inside = np.clip(1 - np.abs(xx - bx) / np.maximum(width, 1e-3), 0, 1)
        bars = np.maximum(bars, inside * (width > 0.5) * 3.0)
    cob = pn(w, h, 16, 1667, 2)
    cobbles = (np.random.default_rng(1669).random((h, w)) > 0.97) * 1.2 * (scour < -1.0)
    hgt = rh * 0.7 + scour + bars + cobbles + 0.6 * cob
    t = np.clip(0.5 + 0.12 * (cob - 0.5) + 0.12 * bars - 0.06 * scour, 0, 1)
    alb = ramp([(84, 42, 26), (118, 64, 40), (150, 92, 60), (184, 126, 86), (204, 150, 106)], t)
    alb = alb * 0.75 + ralb * 0.25 + np.array((22, 16, 10))
    rgb = lit(hgt, alb)
    return finish(rgb, 32)


def eos_chaos():
    """Section 6: the outflow's sand floor (ripples, dark troughs) in the middle, x 130-350, the Burrowers'
    sand; toward the edges the chaos terrain's jumbled blocks with their shadows."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    warp = pn(w, h, 96, 1671, 3)
    dune = np.sin(2 * np.pi * 14 * yy / h + xx * 0.039 + 5.5 * warp)                    # whole cycles: wraps
    sand_h = 1.6 * dune + 0.8 * pn(w, h, 32, 1673, 2)
    edge = np.clip((np.abs(xx - 240) - 105) / 40, 0, 1)    # 0 in the sand field, 1 toward the edges
    rng = np.random.default_rng(1675)
    blocks = np.zeros((h, w))
    for _ in range(70):
        side = rng.choice([-1, 1])
        bx = 240 + side * rng.uniform(130, 260)
        by = rng.uniform(0, h)
        r = rng.uniform(10, 28)
        rot = rng.uniform(0, np.pi / 2)
        dx = xx - bx
        dy = (yy - by + h / 2) % h - h / 2
        ux, uy = dx * math.cos(rot) + dy * math.sin(rot), -dx * math.sin(rot) + dy * math.cos(rot)
        jag = pn(w, h, 32, 1679, 2)
        dd = (0.6 * np.maximum(np.abs(ux), np.abs(uy) * rng.uniform(0.7, 1.3)) + 0.4 * np.hypot(ux, uy)) / r
        dd = dd + 0.35 * (jag - 0.5)
        top = rng.uniform(5, 10)
        blocks = np.maximum(blocks, np.clip((1.05 - dd) * 6, 0, 1) * top)
    blocks *= np.clip((np.abs(xx - 240) - 95) / 25, 0, 1)
    hgt = sand_h * (1 - 0.6 * edge) + blocks + 0.8 * pn(w, h, 16, 1677, 2) * edge
    t = np.clip(0.55 + 0.18 * dune * (1 - edge), 0, 1)
    sand = ramp([(120, 62, 36), (168, 104, 64), (204, 144, 96), (226, 176, 124)], t)
    rock = ramp([BASALT[2], BASALT[3], (138, 94, 70)], np.clip(blocks / 10, 0, 1))
    alb = np.where((blocks > 0.8)[..., None], rock, sand)
    rgb = lit(hgt, alb)
    return finish(rgb, 32)


# =========================================================================== seams (exact rows)

FEATHER = 32


def seam_rows():
    """Each ground section seam's layer position (sections 4, 5, 6: rapids, narrows, outflow)."""
    return {"seam-rapids": seam("ground", 3), "seam-narrows": seam("ground", 4), "seam-eos": seam("ground", 5)}


def mouth_rows():
    """The canyon mouth piece's rows: from 700 px after the walls begin up past section 2's seam (the ground's
    position of a terrain row is the row + 540). Its top FEATHER + 32 rows lie over canyon-floor's own rows
    above the seam, so the faded top edge sits on the same pixels and the seam itself stays covered."""
    top = seam("ground", 1) + FEATHER + 32
    bottom = jr(LEFT[0][0] + 540 + 700)
    return bottom, top - bottom


SEAM_H = 320


def seam_piece(name):
    lower, upper = {"seam-rapids": ("canyon-floor", "riverbed"), "seam-narrows": ("riverbed", "canyon-floor"),
                    "seam-eos": ("canyon-floor", "eos-chaos")}[name]
    g = seam_rows()[name]
    return blend(lower, upper, g - SEAM_H // 2, SEAM_H, g, {"seam-rapids": 1681, "seam-narrows": 1683,
                                                           "seam-eos": 1685}[name])


def tile_strip(name, bottom, h):
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def blend(lower, upper, bottom, h, boundary, seed, ragged=(14, 9, 5, 3)):
    """backdrop_l15.blend_piece on this level's tile sets: ``lower`` below a ragged boundary, ``upper``
    above, the ends faded FEATHER rows into the tile sets (ordered-dithered 1-bit alpha)."""
    up = tile_strip(upper, bottom, h)
    lo = tile_strip(lower, bottom, h)
    rows = bottom + h - 1 - np.arange(h)[:, None]
    xs = np.arange(W)[None, :]
    rng = np.random.default_rng(seed)
    wob = sum(a * np.sin(2 * np.pi * c * xs / W + rng.uniform(0, 6.3)) for c, a in zip((1, 2, 5, 11), ragged))
    edge = boundary + wob + (np.random.default_rng(seed + 1).random((1, W)) - 0.5) * 3
    above = rows >= edge
    out = np.where(above[..., None], up, lo)
    d = rows - edge
    r = (d >= 0) & (d < 2)
    out[..., :3] = np.where(r[..., None], out[..., :3] * 0.7, out[..., :3])
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where((alpha > 0) & (out[..., 3] > 0), 255, 0)     # the tiles' own holes (Noctis's cracks) kept
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def canyon_mouth():
    """The Noctis plateau's cracks closing into the canyon floor: the cracks fill with talus and end, the
    plateau's regolith gives way to the floor's sand (ragged, rising over the piece's lower half)."""
    bottom, h = mouth_rows()
    return blend("noctis-plateau", "canyon-floor", bottom, h, bottom + h * 0.42, 1687, ragged=(60, 30, 14, 5))


# =========================================================================== walls and islands

# Each step's stratum: (colour at its foot, colour at its top edge); cliffs fluted, benches talus.
STEP_COLOURS = (
    ((80, 50, 41), (122, 78, 58)),      # the dark inner gorge (basalt and schist)
    ((134, 96, 70), (112, 78, 56)),     # its grey-brown talus bench
    ((148, 64, 36), (182, 92, 52)),     # the red-orange middle cliffs
    ((200, 128, 78), (178, 108, 64)),   # the red talus slope
    ((204, 158, 108), (226, 186, 134)), # the pale cap cliff
    ((226, 186, 134), (238, 202, 150)), # the cream cap slope, its lip at the rim
)
FLUTES = (88, 0, 64, 0, 104, 0)         # grooves per 1,024 rows on each cliff


def strata_wall(th, seed, flutes=FLUTES):
    """A face texture in the steps' layout (CanyonProfile, SEGMENTS): columns from the foot (0) to the rim
    (TEX_W - 1), each step its columns; rows along the wall (the arc length of the step's middle contour),
    wrapping. Cliffs: vertical fluting (row bands: a dark groove, lighter ridges), thin strata lines running
    along the wall (columns); benches: talus, pebbles and boulders, a light lip at the front edge, a dark toe
    under the cliff above. Lit by the game per step (the vertex shade), so only the fine relief is baked here,
    symmetric along the rows."""
    tw = TEX_W
    widths = [s[4] for s in SEGMENTS]
    g_of = np.repeat(np.arange(len(SEGMENTS)), widths)
    start = np.concatenate([[0], np.cumsum(widths)])[:-1]
    tcol = (np.arange(tw) - start[g_of] + 0.5) / np.array(widths)[g_of]
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    G = np.broadcast_to(g_of, (th, tw))
    T = np.broadcast_to(tcol, (th, tw))
    lo = np.array([c[0] for c in STEP_COLOURS], float)[G]
    hi = np.array([c[1] for c in STEP_COLOURS], float)[G]
    rgb = lo + (hi - lo) * T[..., None]
    fine = pn(tw, th, 8, seed, 3)
    mid = pn(tw, th, 32, seed + 1, 3)
    along = pn(tw, th, 64, seed + 2, 2)
    rgb *= (0.9 + 0.2 * mid)[..., None] * (0.94 + 0.12 * fine)[..., None]
    cliff = np.isin(G, [g for g, s in enumerate(SEGMENTS) if s[0] == "cliff"])
    bench = ~cliff
    # cliffs: fluting
    n_fl = np.array(flutes, float)[G] * th / 1024.0
    phase = (yy / th * n_fl + 0.35 * (along - 0.5) + 0.12 * T * (mid - 0.5)) % 1.0
    wid = 0.1 + 0.06 * fine
    groove = np.clip(1 - np.abs(phase - 0.5) / wid, 0, 1) ** 1.4
    ridge = np.clip(1 - np.abs(np.abs(phase - 0.5) - 0.32) / 0.12, 0, 1)
    deep = 0.55 + 0.45 * (1 - T)
    f = 1 - 0.34 * groove * deep + 0.08 * ridge
    rgb = np.where(cliff[..., None], rgb * f[..., None], rgb)
    # cliffs: strata lines along the wall (constant columns), wandering a little
    wander = 0.08 * (along - 0.5) + 0.03 * (fine - 0.5)
    band = np.floor((T + wander) * np.where(G == 2, 9, 5)) % 2
    rgb = np.where(cliff[..., None], rgb * (0.96 + 0.06 * band)[..., None], rgb)
    dark = (G == 2) & (np.abs(T + wander - 0.58) < 0.045)
    rgb = np.where(dark[..., None], rgb * 0.78, rgb)
    white = (G == 4) & (np.abs(T + wander - 0.35) < 0.06)
    rgb = np.where(white[..., None], rgb * 1.07, rgb)
    rgb = np.where((cliff & (T > 0.9))[..., None], rgb * 1.08, rgb)        # the cliff's top edge catches light
    rgb = np.where((cliff & (T < 0.08))[..., None], rgb * 0.82, rgb)       # its foot in the debris
    # benches: talus, rills, boulders, the lip and the toe
    rng = np.random.default_rng(seed + 3)
    peb = np.zeros((th, tw))
    for _ in range(th * 3):
        px, py, r = rng.uniform(0, tw), rng.uniform(0, th), rng.uniform(0.6, 2.2)
        y0, y1, x0, x1 = int(py - 3), int(py + 4), int(px - 3), int(px + 4)
        ys, xs = np.arange(y0, y1) % th, np.clip(np.arange(x0, x1), 0, tw - 1)
        d = np.hypot(xs[None, :] - px, ((ys[:, None] - py + th / 2) % th) - th / 2)
        peb[np.ix_(ys, xs)] = np.maximum(peb[np.ix_(ys, xs)], np.clip(1 - d / r, 0, 1))
    pebble_lit = np.roll(peb, 1, 1) - peb                                      # lit toward the rim (column +)
    rgb = np.where(bench[..., None], rgb * (1 + 0.16 * pebble_lit - 0.1 * peb)[..., None], rgb)
    rill = np.clip(1 - np.abs(((yy / th * 38 + 0.5 * (along - 0.5)) % 1.0) - 0.5) / 0.06, 0, 1)
    rgb = np.where(bench[..., None], rgb * (1 - 0.12 * rill * T)[..., None], rgb)
    rgb = np.where((bench & (T < 0.12))[..., None], rgb * 1.12, rgb)       # the front lip
    rgb = np.where((bench & (T > 0.82))[..., None], rgb * (0.86 + 0.1 * fine)[..., None], rgb)   # the toe
    last = len(SEGMENTS) - 1
    rim = (G == last) & (T > 0.84)
    rgb = np.where(rim[..., None], np.array((242, 206, 152), float) * (0.95 + 0.08 * fine)[..., None], rgb)
    return np.clip(rgb, 0, 255)


def padded(rgb, pad=TEX_PAD):
    """The periodic texture with its last `pad` rows repeated above it: a quad whose rows run past the period
    still reads inside the image (CanyonProfile's texture convention)."""
    return np.vstack([rgb[-pad:], rgb])


def canyon_wall():
    return finish(padded(strata_wall(TEX_PERIOD, 1701)), 32, amount=0.08)


def mesa_face():
    tw, th = MESA_FACE_TEX
    return finish(padded(strata_wall(th, 1711, flutes=(96, 0, 72, 0, 110, 0)), FACE_PAD), 28, amount=0.08)


def pillar_face():
    tw, th = PILLAR_FACE_TEX
    return finish(padded(strata_wall(th, 1721, flutes=(100, 0, 80, 0, 120, 0)), FACE_PAD), 28, amount=0.08)


def wall_foot():
    """The 24 px foot band: column 0 the collision line (crag), the lit scree edge, scree thinning out."""
    tw, th = FOOT_TEX
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    n = pn(tw, th, 8, 1731, 2)
    rng = np.random.default_rng(1733)
    peb = np.zeros((th, tw))
    for _ in range(320):
        px, py = abs(rng.normal(0, 8)), rng.uniform(0, th)
        r = rng.uniform(0.8, 2.6) * (1.3 - px / 30)
        dy = (yy - py + th / 2) % th - th / 2
        peb = np.maximum(peb, np.clip(1 - np.hypot(xx - px, dy) / max(r, 0.5), 0, 1) * r)
    hgt = 2.2 * peb + 0.8 * n + 3 * np.clip(1 - xx / 12, 0, 1)
    t = np.clip(0.45 + 0.35 * (n - 0.5) + 0.12 * peb, 0, 1)
    alb = ramp([(70, 38, 24), (120, 72, 46), (158, 104, 70), (188, 134, 94)], t)
    rgb = lit_up(hgt, alb)
    rgb[:, 0:2] = np.array((30, 16, 10), float)
    rgb[:, 2:5] = ramp([(196, 136, 90), (226, 170, 120)], n[:, 2:5])
    cover = np.clip(1.25 - xx / 18, 0, 1)
    cover = np.maximum(cover, (peb > 0.4) * np.clip(1.4 - xx / 26, 0, 1))
    alpha = dither_alpha(cover, 2)
    return finish(rgb, 20, alpha, amount=0.12)


def polygon_mask(poly, w, h, x0, s1, scale):
    """1 inside the island's polygon on an image of the polygon x ``scale`` (rows from the far end s1 down)."""
    img = Image.new("L", (w * 4, h * 4), 0)
    pts = [((x - x0) * scale * 4, (s1 - s) * scale * 4) for x, s in poly]
    ImageDraw.Draw(img).polygon(pts, fill=255)
    return np.array(img.resize((w, h), Image.BOX), float) / 255


def island_bbox(poly):
    xs, ss = [p[0] for p in poly], [p[1] for p in poly]
    return min(xs), max(xs), min(ss), max(ss)


def island_top(name, seed):
    """A butte's cap at 1.25 on the canvas of its foot's polygon x 1.25 (the game draws it there): the cap's
    polygon (CanyonProfile's last ring, grown 1 px under the cap slope's top edge) in the plateau's pale rock
    (regolith, a crack or two), a light lip on the edges facing the upper left and a darker one on the others,
    1-bit alpha."""
    x0, x1, s0, s1 = island_bbox(ISLANDS[name])
    w, h = math.ceil((x1 - x0) * PLATEAU_K), math.ceil((s1 - s0) * PLATEAU_K)
    cover = polygon_mask([tuple(p) for p in island_cap(name)], w, h, x0, s1, PLATEAU_K)
    cover = np.array(Image.fromarray((cover * 255).astype(np.uint8)).filter(ImageFilter.MaxFilter(3)), float) / 255
    hp, wp = (h + 479) // 480 * 480, (w + 479) // 480 * 480       # the noise cells (160, 96) divide them
    rh, alb = plateau_rock(wp, hp, seed)
    rh, alb = rh[:h, :w], alb[:h, :w]
    cracks = crack_field(wp, hp, seed + 7, n_long=1, n_cross=max(2, h // 300))[:h, :w]
    inner = np.array(Image.fromarray((cover * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(5)), float) / 255
    edge = np.clip(cover - inner, 0, 1)
    hgt = rh - 3 * cracks + 3 * inner
    rgb = lit(hgt, alb, wrap=False, ambient=0.5, key=0.62)        # the plateau's pale cap rock
    gy, gx = np.gradient(inner)
    facing = -(gx + gy)                                      # > 0: the edge faces the upper left
    lip = np.where(facing[..., None] > 0, np.array((242, 206, 152), float), np.array((196, 156, 110), float))
    rgb = np.where((edge > 0.15)[..., None], lip, rgb)
    alpha = np.where(cover > 0.5, 255, 0)
    return finish(rgb, 28, alpha, amount=0.12)


def mesa_top():
    img = island_top("mesa", 1741)
    h = img.height
    cut = [round(h * i / MESA_SLICES) for i in range(MESA_SLICES + 1)]
    return [img.crop((0, cut[i], img.width, cut[i + 1])) for i in range(MESA_SLICES)]


def pillar_top():
    return island_top("pillar", 1751)


# =========================================================================== pieces

def cliff_habitat():
    """A face patch (64 x 160): a burnt colony habitat cut into the red middle cliff (CanyonProfile's
    HABITAT_SEGMENT: its columns from the cliff's foot to its top edge, rows along the wall, the top rows further
    up the canyon): two storeys of window bands, a balcony slab, a door at the foot, black scorch streaks up the
    rock. The rock round it is canyon-wall's cliff."""
    tw, th = 64, 160
    g = HABITAT_SEGMENT
    c0 = int(round(NODE_U[g] * TEX_W))
    c1 = int(round(NODE_U[g + 1] * TEX_W))
    rock = strata_wall(TEX_PERIOD, 1761)[:th, c0:c1]
    rgb = np.array(Image.fromarray(rock.astype(np.uint8)).resize((tw, th), Image.NEAREST), float)
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    n = pn(tw, th, 16, 1763, 2)
    body = (np.abs(yy - th / 2) < 54) & (xx > 8) & (xx < 50)
    rgb = np.where(body[..., None], ramp([(96, 92, 90), (132, 126, 120), (160, 154, 146)], n), rgb)
    for c in (16, 32):                                         # window bands (dark, a few burnt-out orange)
        win = body & (xx >= c) & (xx < c + 6) & ((yy.astype(int) % 9) < 6)
        rgb = np.where(win[..., None], np.array((26, 22, 24), float), rgb)
        ember = win & (n > 0.66)
        rgb = np.where(ember[..., None], np.array((150, 70, 34), float), rgb)
    slab = body & (xx >= 25) & (xx < 29)
    rgb = np.where(slab[..., None], np.array((176, 168, 158), float), rgb)
    door = (np.abs(yy - th / 2) < 9) & (xx < 10) & (xx > 1)
    rgb = np.where(door[..., None], np.array((20, 16, 16), float), rgb)
    scorch = np.clip(1 - np.abs(yy - th / 2 - 14 * (n - 0.5)) / (16 + 20 * xx / tw), 0, 1) * smooth(30, 60, xx)
    rgb *= (1 - 0.7 * scorch * (n > 0.35))[..., None]
    return finish(rgb, 24, amount=0.08)


def habitat_pad():
    """A burnt habitat's landing pad at a wall's foot (the turret pair's): scorched concrete, a broken rail,
    soot, a collapsed mast."""
    w, h = 96, 64
    yy, xx = b15.piece_canvas((w, h))
    f = 2
    n = pn(w * f, h * f, 32, 1771, 3)
    pad = (np.abs(xx - 48) < 42) & (np.abs(yy - 32) < 26)
    hgt = 2.0 * pad + 0.5 * n
    alb = np.where(pad[..., None], ramp([(96, 92, 88), (128, 122, 116), (150, 144, 136)], n),
                   ramp([(98, 52, 32), (140, 84, 54)], n))
    soot = np.clip(1 - np.hypot(xx - 40, yy - 30) / (26 + 10 * n), 0, 1)
    alb = alb * (1 - 0.65 * soot[..., None])
    stripe = pad & ((np.abs(xx - 48) > 38) | (np.abs(yy - 32) > 22)) & ((xx.astype(int) // 4 + yy.astype(int) // 4) % 2 == 0)
    alb = np.where(stripe[..., None], np.array((150, 120, 50), float), alb)
    cover = np.clip(1.2 - b15.soft_blob(yy, xx, 48, 32, 47, 31, n, 0.3) ** 6, 0, 1)
    return b15.finish_piece(hgt, alb, cover, f=f, colours=24)


ROAD_W = 120


def road_rows():
    """The cliff road's terrain rows: round the rovers, 1,600 rows."""
    rows = [row_of(t, 24) for t, _ in TARGETS["rovers"]]
    centre = (min(rows) + max(rows)) / 2
    return centre - 800, centre + 800


def cliff_road():
    """The colonists' road along the right wall's foot in the rapids: a graded strip 18 px wide hugging the
    foot (its shape from the wall), tyre ruts, a broken guard rail on the canyon side, rockfall; it fades
    out at both ends. Centred x: ROAD_X."""
    s0, s1 = road_rows()
    w, h = ROAD_W, int(s1 - s0)
    f = 1
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rows = s1 - yy[:, :1]
    foot = wall_x(RIGHT, rows) - (road_x() - w / 2)            # the foot's x in the piece
    d = foot - xx                                               # px in front of the foot
    n = pn(w, (h + 63) // 64 * 64, 8, 1781, 3)[:h]
    road = (d > 8) & (d < 34)
    hgt = 1.5 * road + 0.6 * n + 2.0 * (d < 6)
    alb = np.where(road[..., None], ramp([(150, 120, 96), (178, 150, 122), (198, 172, 144)], n),
                   ramp([(120, 80, 56), (150, 104, 74)], n))
    ruts = road & ((np.abs(d - 15) < 1.0) | (np.abs(d - 25) < 1.0))
    alb = np.where(ruts[..., None], alb * 0.72, alb)
    rail = (np.abs(d - 35) < 0.8) & ((rows.astype(int) % 40) > 6)
    alb = np.where(rail[..., None], np.array((160, 160, 166), float), alb)
    hgt = hgt + 1.2 * rail
    rock = (np.random.default_rng(1783).random((h, w)) > 0.985) & (d > 4) & (d < 38)
    hgt = hgt + 2.0 * rock
    k = np.arange(h)[:, None]
    fade = np.minimum(np.clip(k / 80, 0, 1), np.clip((h - k) / 80, 0, 1))
    cover = ((d > -4) & (d < 38)).astype(float) * fade
    rgb = lit(hgt, alb, wrap=False)
    alpha = dither_alpha(cover, 2)
    return finish(rgb, 24, alpha, amount=0.16)


def road_x():
    s0, s1 = road_rows()
    rows = np.linspace(s0, s1, 50)
    return float(np.round(np.nanmean(wall_x(RIGHT, rows)) - 26))


def helix_station():
    """Helix's research station on the north (right) plateau at 1.25: mars_r36.helix_site (unchanged)
    rendered at the plateau's scale and turned with its long side along the canyon; black and gold, its
    skylights, windows and the mast lit; no lettering; its shadow on the plateau to the lower right."""
    orig = m36.sdf_sprite

    def turned(scene, mats, w, h, factor=4, colors=40, z_top=None, crisp=60):
        rot = (lambda p: scene(rotate_z(p, -math.pi / 2)))
        k = PLATEAU_K
        hi = m36.sdf.render(rot, mats, (int(h * k) * factor, int(w * k) * factor), float(h),
                            z_top=z_top or max(w, h), steps=160)
        return m36.sprite.make_sprite(hi, factor, colors, crisp=crisp)
    m36.sdf_sprite = turned
    try:
        st = m36.helix_site(116, 88)
    finally:
        m36.sdf_sprite = orig
    w, h = st.width + 16, st.height + 16
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    sh = sprite.shadow_of(st, opacity=0.5, blur=1.2)
    a = np.array(sh)
    a[..., 3] = (np.array(dither_alpha(a[..., 3] / 255 * 1.0, 3)) * 0.5).astype(np.uint8)
    img.alpha_composite(Image.fromarray(a, "RGBA"), (10, 11))
    img.alpha_composite(st, (4, 4))
    return k8.quantize_lit([img], 48)[0]


def dust_tile(seed, cover, soft, max_alpha, stretch, levels=6, colours=12, cell=20, octaves=3):
    """Dust stretched along the canyon (the wind runs down it), wrapping both ways."""
    import parallax_r03 as r03
    w, h = W, TILE_H
    n = aniso(w, h, 1, stretch, cell, seed, octaves)
    big = np.tile(n, (3, 3))
    img = r03.bank(big, cover, soft, DUST[1:], max_alpha=max_alpha, light=3.0)
    img = img.crop((w, h, 2 * w, 2 * h))
    return r03.step_alpha(artkit.quantize_set([img], colours)[0], levels)


def dust_veil():
    return dust_tile(1791, 0.7, 0.14, 64, 6, levels=8)


def narrows_dust():
    return dust_tile(1793, 0.6, 0.2, 92, 6, levels=8)


def dust_streaks():
    """High-air (additive at 40 % by the game): short pale streaks blown along the canyon."""
    rng = np.random.default_rng(1795)
    img = Image.new("RGBA", (W, TILE_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for _ in range(28):
        x, y = rng.uniform(0, W), rng.uniform(0, TILE_H)
        ln, b = rng.uniform(18, 70), rng.uniform(0.35, 1.0)
        col = tuple(int(c * b) for c in DUST[4]) + (255,)
        for dy in (-TILE_H, 0, TILE_H):
            d.line([(x, y + dy), (x + rng.uniform(-3, 3) * 0, y + dy + ln)], fill=col, width=1 + int(b > 0.8))
    return artkit.additive(img)


def dust_plume():
    """A dust plume in the canyon (low-air): a lumpy ochre cloud, stepped translucency, at most 45 %."""
    import parallax_r03 as r03
    r = 36
    blob = raster.fbm(r * 2, r * 2, 18, 1797, octaves=3, period=False)
    yy, xx = np.mgrid[0:2 * r, 0:2 * r]
    fall = np.clip(1 - np.hypot(xx - r, (yy - r) * 0.8) / r, 0, 1)
    a = np.clip(blob * fall * 2.4 - 0.3, 0, 1) * 115
    shade = np.clip(1.1 - (yy / (2 * r)) * 0.4 + 0.3 * (blob - 0.5), 0.6, 1.2)
    rgb = np.array(DUST[4], float)[None, None, :] * shade[..., None]
    img = raster.to_rgba_image(rgb, a)
    return r03.step_alpha(artkit.quantize_set([img], 10)[0], 5)


ARCHES = {"arch-a": (480, 160), "arch-b": (480, 192)}
ARCH_ALPHA = 0.53                       # x the 75 % overhead pass = at most 40 % on screen


def arch(name):
    """A rock arch passing over the canyon (high-air, x2.2): a natural bridge from rim to rim, thick at the
    abutments, its top lit and its underside dark, strata along it; `arch-b` thinner and broken-backed with
    a second, lower span. Normal blending; the image's alpha caps at ARCH_ALPHA."""
    w, h = ARCHES[name]
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    seed = 1801 if name == "arch-a" else 1803
    n = pn(w * f, h * f, 32, seed, 3)
    u = (xx - w / 2) / (w / 2)                                  # -1 .. 1 across
    if name == "arch-a":
        mid_y, thick = h * 0.5 + 18 * u ** 2, 26 + 46 * np.abs(u) ** 4
    else:
        mid_y, thick = h * 0.45 + 34 * u ** 2 - 10 * np.sin(u * 2.2), 16 + 60 * np.abs(u) ** 5
    d = np.abs(yy - mid_y) - thick / 2 - 4 * (n - 0.5)
    cover = np.clip(0.5 - d, 0, 1)
    if name == "arch-b":                                        # a lower span on the left
        d2 = np.abs(yy - (h * 0.78 + 20 * (u + 0.6) ** 2)) - (10 + 30 * np.clip(-u - 0.6, 0, 1) ** 2) - 3 * (n - 0.5)
        cover = np.maximum(cover, np.clip(0.5 - d2, 0, 1) * (u < -0.15))
    hgt = 6 * cover + 2 * n
    band = (yy - mid_y) / np.maximum(thick, 1)
    alb = ramp([STRATA[1], STRATA[0], STRATA[4], STRATA[3]], np.clip(0.5 + band * 0.8 + 0.1 * (n - 0.5), 0, 1))
    a = k8.relief(hgt, alb, np.zeros_like(alb), np.ones(hgt.shape), f, ambient=0.4, key=0.8, ao=0.1, gradient=0.0)
    nat = k8.reduce(a, f)
    cov = k8.reduce(np.dstack([np.zeros(cover.shape + (3,)), cover]), f)[..., 3]
    rgb = recede(nat[..., :3] * 255, 0.1)
    alpha = np.where(cov > 0.5, ARCH_ALPHA * 255, 0)
    img = posterize_rgb(rgb, 24, alpha)
    return img


GENERATORS = {
    "noctis-chasm": noctis_chasm, "noctis-plateau": noctis_plateau, "plateau": plateau,
    "canyon-floor": canyon_floor, "riverbed": riverbed, "eos-chaos": eos_chaos,
    "canyon-wall": canyon_wall, "wall-foot": wall_foot, "mesa-face": mesa_face, "pillar-face": pillar_face,
    "pillar-top": pillar_top, "cliff-habitat": cliff_habitat, "habitat-pad": habitat_pad, "cliff-road": cliff_road,
    "helix-station": helix_station, "dust-veil": dust_veil, "narrows-dust": narrows_dust,
    "dust-streaks": dust_streaks, "dust-plume": dust_plume,
    "arch-a": lambda: arch("arch-a"), "arch-b": lambda: arch("arch-b"),
}
FRAME_SETS = {"mesa-top": mesa_top}
SECOND = {"canyon-mouth": canyon_mouth, "seam-rapids": lambda: seam_piece("seam-rapids"),
          "seam-narrows": lambda: seam_piece("seam-narrows"), "seam-eos": lambda: seam_piece("seam-eos")}
TILE_SETS = {   # id -> (layer, drift, note)
    "noctis-chasm": ("far", None, "section 1: the chasm floor deep below the cracks (talus, rubble), hazed"),
    "noctis-plateau": ("ground", None, "section 1: the plateau east of Lycus cracking into the labyrinth's graben; the cracks transparent onto the far layer"),
    "canyon-floor": ("ground", None, "sections 2, 3, 5: the floor, wind ripples along the canyon, a dry channel, boulders"),
    "riverbed": ("ground", None, "section 4, the Coprates rapids: braided scoured channels, streamlined bars, cobbles"),
    "eos-chaos": ("ground", None, "section 6: the outflow's sand floor (x 130-350, the Burrowers') between the chaos terrain's blocks"),
    "plateau": ("plateau", None, "the plateau's top beyond the walls' rims (drawn only beyond the strips' outer edge; the islands' tops are their own)"),
    "dust-streaks": ("high-air", None, "sections 1, 2, 4: dust streaks blown along the canyon (additive at 40 %)"),
    "dust-veil": ("low-air", 0, "light: thin dust veils along the canyon, ~10 %, at most 30 % opaque"),
    "narrows-dust": ("low-air", 0, "medium: the narrows' dust (and the rapids' 110-120), ~25 %, at most 45 % opaque"),
}


def render_job(name):
    fn = GENERATORS.get(name) or FRAME_SETS.get(name) or SECOND[name]
    return name, fn()


def save(name, img):
    OUT.mkdir(parents=True, exist_ok=True)
    if isinstance(img, list):
        for old in OUT.glob(f"{name}_*.png"):
            old.unlink()
        for i, frame in enumerate(img):
            artkit.save_png(frame, OUT / f"{name}_{i}.png", SOURCE)
        print(f"  {name}: {len(img)} x {img[0].size[0]}x{img[0].size[1]}")
        return
    artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    print(f"  {name}: {img.size[0]}x{img.size[1]}")


def write_all(wanted):
    first = [n for n in list(GENERATORS) + list(FRAME_SETS) if not wanted or n in wanted]
    second = [n for n in SECOND if not wanted or n in wanted
              or any(w in wanted for w in ("noctis-plateau", "canyon-floor", "riverbed", "eos-chaos"))]
    with ProcessPoolExecutor(max_workers=8) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
        for name, img in pool.map(render_job, second):
            save(name, img)


# =========================================================================== the backdrop block

def piece_sizes():
    sizes = {}
    for name in ("cliff-habitat", "habitat-pad", "cliff-road", "helix-station", "dust-plume", "arch-a", "arch-b",
                 "canyon-mouth", "seam-rapids", "seam-narrows", "seam-eos"):
        p = OUT / f"{name}.png"
        if p.exists():
            sizes[name] = list(Image.open(p).size)
    return sizes


HELIX_ROW = 22600.0


def helix_t():
    """The station's placement: beside the rim of terrain row HELIX_ROW, the widest stretch of the right
    plateau (rows 21,500-23,800, Helix's glimpse t~113-124). The plateau layer is drawn as a plain layer at
    1.25 (its rows = scroll x 1.25), while the rim of row R is projected round y 297: the piece's centre meets
    that rim when the scroll is R + (270 + 297 x 0.25) / 1.25."""
    return round(t_at(HELIX_ROW + (MID + CY * (PLATEAU_K - 1)) / PLATEAU_K), 3)


def placements():
    sizes = piece_sizes()
    out = []
    b, h = mouth_rows()
    out.append(("# 1. the canyon mouth: Noctis's cracks close into the canyon floor after the walls begin (exact rows)",
                {"piece": "canyon-mouth", "t": t_for_bottom("ground", b, h), "x": 240}))
    for name, g in seam_rows().items():
        out.append((f"# the section seam under {name} (exact rows; re-run after the data moves a section's start)",
                    {"piece": name, "t": t_for_bottom("ground", g - SEAM_H // 2, SEAM_H), "x": 240}))
    s0, s1 = road_rows()
    out.append(("# 4. the cliff road along the right wall's foot under the cargo rovers",
                {"piece": "cliff-road", "t": t_for_row((s0 + s1) / 2), "x": road_x()}))
    pad = TARGETS["pad"]
    px = sum(p[1] for p in pad) / len(pad)
    out.append(("# 4. a burnt habitat's pad at the left wall's foot under the Spine Turret pair",
                {"piece": "habitat-pad", "t": t_for_row(row_of(pad[0][0], 32)), "x": round(px)}))
    out.append(("# 4. the Helix research station on the north (right) plateau, beside the widest stretch (plateau layer x1.25)",
                {"piece": "helix-station", "t": helix_t(), "x": 412}))
    out.append(("# 4. two rock arches over the rapids (overhead pass: arch-a, arch-b in Backdrop.OVERHEAD_PASSES)",
                {"piece": "arch-a", "t": 101.5, "x": 240}))
    out.append(("", {"piece": "arch-b", "t": 117.0, "x": 240}))
    out.append(("# 2-5. dust plumes in the canyon (low-air), every 6.5 s along the middle",
                {"piece": "dust-plume", "t": 28.0, "x": 210, "repeat": {"count": 20, "every": 6.5}}))
    out.append(("", {"piece": "dust-plume", "t": 31.3, "x": 270, "repeat": {"count": 19, "every": 6.5}}))
    return out, sizes


def habitat_faces():
    """The face patches G1 draws (not regular pieces): burnt cliff habitats on section 2's faces."""
    return [("left", 6600), ("right", 8350), ("left", 10400), ("right", 20480 - 300)]


def block():
    places, sizes = placements()
    pieces = {
        "canyon-mouth": ("ground", None, "the Noctis plateau's cracks closing into the canyon floor (exact rows)"),
        "seam-rapids": ("ground", None, "section 4's seam: the floor into the rapids' riverbed (exact rows)"),
        "seam-narrows": ("ground", None, "section 5's seam: the riverbed back into the floor (exact rows)"),
        "seam-eos": ("ground", None, "section 6's seam: the floor into the outflow's sand and chaos (exact rows)"),
        "cliff-road": ("ground", None, "the colonists' road along the right wall's foot, ruts, a broken rail (the cargo rovers)"),
        "habitat-pad": ("ground", None, "a burnt cliff habitat's landing pad at a wall's foot (the Spine Turret pair)"),
        "cliff-habitat": ("ground", None, "a FACE PATCH, not a flat piece: a burnt habitat cut into a wall's red middle cliff (64 columns the cliff's foot to its top, 160 rows); the game draws it on that step (placements below)"),
        "helix-station": ("plateau", None, "Helix's research station on the north plateau: black and gold, lit, unlabelled, untouched"),
        "dust-plume": ("low-air", None, "a dust plume in the canyon, at most 45 %"),
        "arch-a": ("high-air", None, "a rock arch over the canyon, alpha <= 53 % (x the 75 % overhead pass: <= 40 %)"),
        "arch-b": ("high-air", None, "a thinner, broken-backed arch with a lower span, alpha <= 53 %"),
    }
    return places, sizes, pieces


def write_proposal():
    places, sizes, pieces = block()
    flow = k8.flow
    lines = ["backdrop:",
             "  # Presentation only: Valles Canyon Run (tools/art/backdrop_l16.py, from canyon-walls-r36-b and mars-valles-r36-a).",
             "  # The walls are NOT pieces: their geometry is the level's `terrain`, projected by the game (WallProjection) from",
             "  # the foot (1.0) to the plateau's rim (1.25) in steps (cliffs and talus benches set back toward the rock, side",
             "  # canyons in the rim: CanyonProfile, round 40) with the textures canyon-wall (128x1280: columns the steps' strata",
             "  # foot -> rim, rows along the wall), wall-foot (24x512, the foot band, column 0 at the collision line), the",
             "  # buttes' mesa-face/pillar-face and caps mesa-top_0..2 (three slices, far end first) and pillar-top; the",
             "  # `plateau` layer (x1.25) shows only beyond the rims, its pieces kept clear of the rock. No deep",
             "  # layer: the ground covers the screen; section 1's Noctis cracks show the far layer (x0.6) through them.",
             f"  scroll_factors: {flow(FACTORS)}",
             f"  ramp: {RAMP}",
             f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
             "  atmosphere:"]
    notes = {"clear": "the outflow's clear air (t 178-190): haze only",
             "light": "thin dust veils along the canyon",
             "medium": "the narrows' dust (section 5; the rapids' peak 110-120)"}
    for k, v in ATMOSPHERE.items():
        lines.append(f"    {k}: {flow(v)}   # {notes[k]}")
    lines.append("  tile_sets:")
    for name, (layer, drift, note) in TILE_SETS.items():
        spec = {"layer": layer, "height": TILE_H}
        if drift is not None:
            spec["drift"] = drift
        lines.append(f"    {name}: {flow(spec)}   # {note}")
    lines.append("  pieces:")
    for name, (layer, _, note) in pieces.items():
        if name not in sizes:
            continue
        lines.append(f"    {name}: {flow({'layer': layer, 'size': sizes[name]})}   # {note}")
    lines.append("  placed:")
    for comment, p in places:
        if comment:
            lines.append(f"    {comment}")
        lines.append(f"    - {flow(p)}")
    rows = ", ".join(f"{row:,}" for _, row in habitat_faces())
    lines.append("    # face patches: cliff-habitat drawn on a wall's face (the game's WallProjection), not as a flat piece; the side")
    lines.append(f"    # from x (left of 240: the left wall), t puts its centre on terrain rows {rows}")
    for side, row in habitat_faces():
        lines.append(f"    - {flow({'piece': 'cliff-habitat', 't': t_for_row(row), 'x': 32 if side == 'left' else 448})}")
    lines.append("  # Suggested section tiles (C2's `sections[].tiles`): " + "; ".join(
        f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("  # Atmospheres as the README's Layout: 1-4 light (4: a medium peak 110-120 for the rapids' dust), 5 medium, 6 light with a clear peak 178-190.")
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def proposal_backdrop():
    """The proposal's block as data (for the review's compositor)."""
    places, sizes, pieces = block()
    return {
        "scroll_factors": FACTORS, "atmosphere": ATMOSPHERE,
        "tile_sets": {n: {"layer": l} for n, (l, _, _) in TILE_SETS.items()},
        "pieces": {n: {"layer": pieces[n][0], "size": sizes[n]} for n in sizes},
        "placed": [p for _, p in places],
    }


# =========================================================================== the game's draw (review)

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def expanded(placed):
    out = []
    for p in placed:
        rep = p.get("repeat")
        for i in range((rep or {}).get("count", 1)):
            q = dict(p)
            q.pop("repeat", None)
            q["t"] = p["t"] + i * (rep or {}).get("every", 0)
            out.append(q)
    return out


def draw_tiles(bd, frame, layer, view, mask=None):
    f = bd["scroll_factors"][layer]
    scroll = jr(view * f)
    rows = scroll + (SCREEN - 1 - np.arange(SCREEN))
    for s in range(len(SECTIONS)):
        names = [n for n in SECTION_TILES[s] if bd["tile_sets"][n]["layer"] == layer]
        if not names:
            continue
        lo = -math.inf if s == 0 else seam(layer, s)
        hi = seam(layer, s + 1) if s + 1 < len(SECTIONS) else math.inf
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(names[0])
            hgt = img.shape[0]
            part = img[[hgt - 1 - (int(r) % hgt) for r in rows[sel]]]
            sub = frame[sel]
            if layer == "high-air":
                k8.add(sub, part, 0.4)
            else:
                k8.over(sub, part)
            frame[sel] = sub


def draw_pieces(bd, frame, layer, view, alpha=1.0):
    f = bd["scroll_factors"][layer]
    for p in expanded(bd["placed"]):
        spec = bd["pieces"][p["piece"]]
        if spec["layer"] != layer:
            continue
        w, h = spec["size"]
        bottom = jr(scroll_at(p["t"]) * f + MID - h / 2)
        y = bottom - jr(view * f)
        if y > SCREEN or y + h < 0:
            continue
        img = cached(p["piece"])
        if alpha < 1:
            img = img.copy()
            img[..., 3] *= alpha
        k8.blit(frame, img, jr(p["x"] - w / 2), y)


def project(x, y, k):
    return CX + (x - CX) * k, CY + (y - CY) * k


def k_share(zshare):
    """The scale of a point at a share of the plateau's height (1.2 camera units)."""
    return 6.0 / (6.0 - 1.2 * zshare)


@lru_cache(maxsize=None)
def canyon_walls():
    patches = {0: [], 1: []}
    for side, row in habitat_faces():
        patches[0 if side == "left" else 1].append(row)
    caps = {0: [], 1: []}
    for _, p in placements()[0]:
        if p["piece"] != "helix-station":
            continue
        w, h = piece_sizes().get("helix-station", (126, 161))
        centre = scroll_at(p["t"]) * PLATEAU_K + MID
        a, b = (centre - h / 2 - PLATEAU_ROW0) / PLATEAU_K, (centre + h / 2 - PLATEAU_ROW0) / PLATEAU_K
        side = 0 if p["x"] < CX else 1
        caps[side].append((a, b, p["x"] + (w / 2 if side == 0 else -w / 2)))
    return {side: CanyonWall(side, LEFT if side == 0 else RIGHT, patches[side], caps[side]) for side in (0, 1)}


class Splat:
    """A 2x canvas painted by points in the order of their height (the higher one is nearer the camera)."""

    def __init__(self, base):
        self.ss = 2
        self.h, self.w = SCREEN * 2, W * 2
        big = Image.fromarray(np.clip(base, 0, 255).astype(np.uint8), "RGB").resize((self.w, self.h), Image.NEAREST)
        self.rgb = np.array(big, float).reshape(-1, 3)
        self.z = np.full(self.h * self.w, -np.inf)

    def paint(self, x, y, z, col):
        xi = np.floor(x * self.ss).astype(np.int64).ravel()
        yi = np.floor(y * self.ss).astype(np.int64).ravel()
        z = np.asarray(z, float).ravel()
        col = col.reshape(-1, 3)
        ok = (xi >= 0) & (xi < self.w) & (yi >= 0) & (yi < self.h)
        xi, yi, z, col = xi[ok], yi[ok], z[ok], col[ok]
        idx = yi * self.w + xi
        order = np.lexsort((z, idx))                       # by pixel, then height
        idx, z, col = idx[order], z[order], col[order]
        last = np.ones(len(idx), bool)
        last[:-1] = idx[1:] != idx[:-1]                    # the highest point per pixel
        idx, z, col = idx[last], z[last], col[last]
        win = z >= self.z[idx]
        self.rgb[idx[win]] = col[win]
        self.z[idx[win]] = z[win]

    def image(self):
        return Image.fromarray(np.clip(self.rgb.reshape(self.h, self.w, 3), 0, 255).astype(np.uint8), "RGB")


def tex_lookup(tex, u, v, period):
    th, tw = tex.shape[:2]
    col = np.clip(np.floor(u * tw).astype(int), 0, tw - 1)
    row = th - 1 - (np.floor(v).astype(int) % period)
    return tex[row, col, :3]


def paint_wall(sp, wall, view):
    tex = cached("canyon-wall")
    patch = cached("cliff-habitat")
    rows = np.arange(view - 700, view + 120, 0.25)
    rows = rows[wall.valid(rows)]
    if len(rows) == 0:
        return None
    x = wall.at(rows, wall.x)
    h = wall.at(rows, wall.h)
    v = wall.at(rows, wall.v)
    shade, cast = wall.segment_shades(rows)
    yd = view - rows
    patches = [r for s, r in habitat_faces() if (s == "left") == (wall.side == 0)]
    for g in range(len(SEGMENTS)):
        width = np.nanmax(np.abs(x[:, g + 1] - x[:, g])) * 1.3 * 2 + 4
        nt = int(min(240, max(6, width)))
        t = np.linspace(0, 1, nt)[None, :]
        X = x[:, g:g + 1] + (x[:, g + 1:g + 2] - x[:, g:g + 1]) * t
        zs = (NODE_Z[g] + (NODE_Z[g + 1] - NODE_Z[g]) * t) * h[:, None]
        k = k_share(zs)
        sx = CX + (X - CX) * k
        sy = CY + (yd[:, None] - CY) * k
        u = NODE_U[g] + (NODE_U[g + 1] - NODE_U[g]) * t
        col = tex_lookup(tex, np.broadcast_to(u, X.shape), np.broadcast_to(v[:, g:g + 1], X.shape), TEX_PERIOD)
        if g == HABITAT_SEGMENT:
            for r in patches:
                ph = patch.shape[0]
                inside = np.abs(rows - r) < ph / 2
                if inside.any():
                    pr = np.clip(np.floor(ph / 2 - (rows[inside] - r)).astype(int), 0, ph - 1)
                    pc = np.clip(np.floor(t[0] * patch.shape[1]).astype(int), 0, patch.shape[1] - 1)
                    pix = patch[pr[:, None], pc[None, :]]
                    a = pix[..., 3:4] / 255
                    col[inside] = col[inside] * (1 - a) + pix[..., :3] * a
        sh = np.broadcast_to(shade[:, g:g + 1], X.shape)
        if SEGMENTS[g][0] == "bench":
            sh = np.where(t > 1 - cast[:, g:g + 1], SHADOWED, sh)
        sp.paint(sx, sy, zs, col * sh[..., None])
    k = k_share(h)
    return CX + (x[:, -1] - CX) * k, CY + (yd - CY) * k


def paint_island(sp, name, view):
    pts, per, nrm, rings, perim = island_rings(name)
    s_lo, s_hi = pts[:, 1].min(), pts[:, 1].max()
    if view - s_hi > SCREEN + 150 or view - s_lo < -150:
        return
    tex = cached("mesa-face" if name == "mesa" else "pillar-face")
    period = tex.shape[0] - FACE_PAD
    n = len(pts)
    sub = 6
    for g in range(len(SEGMENTS)):
        a, b = rings[g], rings[g + 1]
        a2, b2 = np.roll(a, -1, 0), np.roll(b, -1, 0)
        p0, p1 = per, np.roll(per, -1)
        p1 = np.where(p1 < p0, perim, p1)
        dz = (NODE_Z[g + 1] - NODE_Z[g]) * WALL_PX
        # the normal per perimeter step: (b - a, dz) x (a2 - a, 0)
        pa = np.concatenate([b - a, np.full((n, 1), dz)], axis=1)
        pb = np.concatenate([a2 - a, np.zeros((n, 1))], axis=1)
        nn = np.cross(pa, pb)
        nn = np.where(nn[:, 2:3] < 0, -nn, nn)
        shade = shade_of(nn)
        for q in range(sub):
            w = q / sub
            ca = a + (a2 - a) * w
            cb = b + (b2 - b) * w
            pp = p0 + (p1 - p0) * w
            nt = 24
            t = np.linspace(0, 1, nt)[None, :]
            X = ca[:, 0:1] + (cb[:, 0:1] - ca[:, 0:1]) * t
            S = ca[:, 1:2] + (cb[:, 1:2] - ca[:, 1:2]) * t
            zs = (NODE_Z[g] + (NODE_Z[g + 1] - NODE_Z[g]) * t) + 0 * X
            k = k_share(zs)
            sx = CX + (X - CX) * k
            sy = CY + ((view - S) - CY) * k
            u = NODE_U[g] + (NODE_U[g + 1] - NODE_U[g]) * t
            col = tex_lookup(tex, np.broadcast_to(u, X.shape), np.broadcast_to(pp[:, None], X.shape), period)
            sp.paint(sx, sy, zs, col * shade[:, None, None])
    # the cap at the plateau's height
    top = island_top_image(name)
    x0, x1, s0, s1 = island_bbox(ISLANDS[name])
    tx, ty = project(x0, view - s1, PLATEAU_K)
    arr = np.array(top, float)
    yy, xx = np.nonzero(arr[..., 3] > 127)
    sp.paint((tx + xx + 0.5) / 1, (ty + yy + 0.5), np.full(len(xx), 1.01), arr[yy, xx, :3])
    # 2x density for the top: paint each texel's four sub-pixels
    for ox, oy in ((0.25, 0.25), (0.75, 0.25), (0.25, 0.75), (0.75, 0.75)):
        sp.paint(tx + xx + ox, ty + yy + oy, np.full(len(xx), 1.01), arr[yy, xx, :3])


def draw_terrain(bd, frame, view, t):
    """The canyon as the game draws it (WallProjection with CanyonProfile): the foot bands and the cast
    shadows on the floor, the walls' steps from the foot to the rim, the plateau layer beyond the rims, the
    buttes' steps and caps."""
    walls = canyon_walls()
    # foot bands
    foot = cached("wall-foot")
    fh = foot.shape[0]
    for side, wall in ((-1, LEFT), (1, RIGHT)):
        yrows = np.arange(SCREEN)
        r = view - yrows
        x = wall_x(wall, r)
        for y in yrows:
            if np.isnan(x[y]):
                continue
            row = foot[fh - 1 - int(r[y]) % fh]
            seg = row if side < 0 else row[::-1]
            x0 = int(round(x[y])) if side < 0 else int(round(x[y])) - seg.shape[0]
            a, b = max(0, x0), min(W, x0 + seg.shape[0])
            if b > a:
                sub = frame[y:y + 1, a:b]
                k8.over(sub, seg[None, a - x0:b - x0])
                frame[y:y + 1, a:b] = sub
    # cast shadows on the floor: the left wall's steps, the buttes' rings
    ss = 2
    sh = Image.new("L", (W * ss, SCREEN * ss), 0)
    dsh = ImageDraw.Draw(sh)
    yrows = np.arange(0, SCREEN, 0.5) + 0.25
    rows = view - yrows
    lw = walls[0]
    ok = lw.valid(rows)
    if ok.any():
        fx = lw.at(rows, lw.foot)
        ex = lw.floor_shadow(rows)
        for y, a, b, o in zip(yrows, fx, ex, ok):
            if o and b > a:
                dsh.rectangle([a * ss, (y - 0.25) * ss, b * ss, (y + 0.25) * ss - 1], fill=255)
    for name in ISLANDS:
        rings = island_rings(name)[3]
        for j in range(NODES):
            reach = NODE_Z[j] * WALL_PX * SHADOW_RATIO
            dx, ds = reach * SHADOW_DIR[0], reach * SHADOW_DIR[1]
            for f in (0.0, 0.5, 1.0):
                pts = [((x + dx * f) * ss, (view - (s + ds * f)) * ss) for x, s in rings[j]]
                dsh.polygon(pts, fill=255)
    m = np.array(sh.resize((W, SCREEN), Image.BOX), float)[..., None] / 255
    frame[..., :3] *= 1 - (1 - SHADOWED / FLAT) * m
    sp = Splat(frame[..., :3])
    rims = {}
    for side in (0, 1):
        rims[side] = paint_wall(sp, walls[side], view)
    img = sp.image()
    # the plateau layer beyond the rims (it covers the rock below the rim's height)
    plat = np.zeros((SCREEN, W, 4))
    draw_tiles(bd, plat, "plateau", view)
    draw_pieces(bd, plat, "plateau", view)
    plat_img = Image.fromarray(np.clip(plat, 0, 255).astype(np.uint8), "RGBA").resize((W * ss, SCREEN * ss), Image.NEAREST)
    mask = Image.new("L", (W * ss, SCREEN * ss), 0)
    dm = ImageDraw.Draw(mask)
    for side, rim in rims.items():
        if rim is None:
            continue
        rx, ry = rim
        far = -2000 if side == 0 else W * ss + 2000
        pts = [(a * ss, b * ss) for a, b in zip(rx[::4], ry[::4])]
        dm.polygon(pts + [(far, pts[-1][1]), (far, pts[0][1])], fill=255)
    img.paste(plat_img.convert("RGB"), (0, 0), mask)
    sp.rgb = np.array(img, float).reshape(-1, 3)
    sp.z[np.array(mask).ravel() > 0] = 1.0
    for name in ISLANDS:
        paint_island(sp, name, view)
    out = np.array(sp.image().resize((W, SCREEN), Image.BOX)).astype(np.float64)
    frame[..., :3] = out


@lru_cache(maxsize=None)
def island_top_image(name):
    if name == "mesa":
        parts = [Image.open(OUT / f"mesa-top_{i}.png").convert("RGBA") for i in range(MESA_SLICES)]
        img = Image.new("RGBA", (parts[0].width, sum(p.height for p in parts)))
        y = 0
        for p in parts:
            img.paste(p, (0, y))
            y += p.height
        return img
    return Image.open(OUT / "pillar-top.png").convert("RGBA")


@lru_cache(maxsize=None)
def unit_frame(name, i=0):
    for base in (artkit.SPRITES, HELD):
        f = base / f"{name}_{i}.png"
        if not f.exists():
            f = base / f"{name}.png"
        if f.exists():
            return np.array(Image.open(f).convert("RGBA")).astype(np.float64)
    return None


def blit_c(frame, img, x, y_down, additive=None):
    if img is None:
        return
    h, w = img.shape[:2]
    k8.blit(frame, img, jr(x - w / 2), jr(SCREEN - y_down - h / 2), additive)


def tendril_line(frame, ax, ay, bx, by, i=0):
    strip = unit_frame("tendril", i)
    if strip is None:
        return
    th, tw = strip.shape[:2]
    length = math.hypot(bx - ax, by - ay)
    for k in range(int(length)):
        x = ax + (bx - ax) * k / length
        y = ay + (by - ay) * k / length
        col = strip[:, k % tw][:, None, :]
        blit_c(frame, col, x, y)


def draw_units(frame, view, t):
    """Stand-ins at their spots: the gates' anchor pairs with the tendril (their sprites where they exist),
    the relay hut, the rovers, the turret pair, the ship."""
    for g, gxl, gxr in GATES:
        y = view - row_of(g, 40)
        if not -40 < y < SCREEN + 40:
            continue
        tendril_line(frame, gxl + 12, y, gxr - 12, y, int(t * 8) % 4)
        blit_c(frame, unit_frame("tendril-anchor", 24 * 3), gxl, y)   # heading 24: facing right
        blit_c(frame, unit_frame("tendril-anchor", 8 * 3), gxr, y)    # heading 8: facing left
    for tt, x in TARGETS["hut"]:
        blit_c(frame, unit_frame("relay-hut"), x, view - row_of(tt, 40))
    for i, (tt, x) in enumerate(TARGETS["rovers"]):
        blit_c(frame, unit_frame("canyon-rover", 0), x, view - row_of(tt, 24))
    for key, name in (("pad", "spine-turret"), ("turrets", "spine-turret"), ("mortars", "polyp-mortar")):
        for tt, x in TARGETS[key]:
            blit_c(frame, unit_frame(name), x, view - row_of(tt, 32))
    blit_c(frame, unit_frame("ship", 2), 240 + 30 * math.sin(t * 0.7), 430)


def composite(bd, t, units=True):
    view = scroll_at(t)
    frame = np.zeros((SCREEN, W, 4))
    frame[..., 3] = 255
    draw_tiles(bd, frame, "far", view)
    frame[..., :3] = frame[..., :3] * 0.9 + SKY * 0.1
    draw_tiles(bd, frame, "ground", view)
    draw_pieces(bd, frame, "ground", view)
    draw_terrain(bd, frame, view, t)
    if units:
        draw_units(frame, view, t)
    look = next((s for s in stretches() if s[0] <= t < s[1]), stretches()[-1])[2]
    banks = ATMOSPHERE[look].get("banks")
    if banks:
        f = FACTORS["low-air"]
        rows = jr(view * f) + (SCREEN - 1 - np.arange(SCREEN))
        img = cached(banks)
        part = img[[img.shape[0] - 1 - (int(r) % img.shape[0]) for r in rows]]
        k8.over(frame, part)
    draw_pieces(bd, frame, "low-air", view)
    draw_tiles(bd, frame, "high-air", view)
    draw_pieces(bd, frame, "high-air", view, alpha=0.75)
    return Image.fromarray(np.clip(frame[..., :3], 0, 255).astype(np.uint8), "RGB")


def stretches():
    out = []
    for i, s in enumerate(SECTIONS):
        start, end = STARTS[i], float(s["end"])
        pk = s.get("peak")
        if pk:
            if pk["from"] > start:
                out.append((start, pk["from"], s["atmosphere"]))
            out.append((pk["from"], pk["to"], pk["atmosphere"]))
            if pk["to"] < end:
                out.append((pk["to"], end, s["atmosphere"]))
        else:
            out.append((start, end, s.get("atmosphere", "light")))
    return out


TIMES = [(8, "1 THE NOCTIS PLATEAU, ITS CRACKS"), (18.5, "1 THE CANYON MOUTH: THE WALLS RISE"),
         (34, "2 GATE 1 ALONE"), (44, "2 INTO THE CANYON"), (57.5, "2 GATE 3: TWO PAIRS"),
         (68, "3 THE FORK MESA, TWO CHANNELS"), (72.6, "3 THE RELAY HUT (LEFT CHANNEL)"),
         (100.4, "4 THE ROCK PILLAR, AN ARCH"), (109.2, "4 THE CLIFF ROAD, THE ROVERS"),
         (115.8, "4 THE HELIX STATION ON THE RIGHT RIM"), (143, "5 THE NARROWS (180 PX)"),
         (166, "6 THE EOS OUTFLOW OPENS"), (181, "6 THE WALLS SINK, THE SAND")]


def atlas_area():
    return sum(Image.open(p).size[0] * Image.open(p).size[1] for p in OUT.glob("*.png"))


def review():
    bd = proposal_backdrop()
    artkit.REVIEW_ROUND = "r39"
    half = lambda n, d=2: Image.open(OUT / f"{n}.png").convert("RGBA").resize(
        (Image.open(OUT / f"{n}.png").width // d, Image.open(OUT / f"{n}.png").height // d), Image.NEAREST)
    tiles = [half(n) for n in ("noctis-chasm", "noctis-plateau", "canyon-floor", "riverbed", "eos-chaos", "plateau")]
    walls = [Image.open(OUT / f"{n}.png").convert("RGBA").crop((0, 0, Image.open(OUT / f"{n}.png").width, 256)).resize(
        (Image.open(OUT / f"{n}.png").width * 2, 512), Image.NEAREST) for n in ("canyon-wall", "wall-foot", "mesa-face", "pillar-face", "cliff-habitat")]
    tops = [half("mesa-top_0", 3), half("mesa-top_1", 3), half("mesa-top_2", 3), Image.open(OUT / "pillar-top.png")]
    seams = [half(n, 3) for n in ("canyon-mouth", "seam-rapids", "seam-narrows", "seam-eos")]
    pieces = [Image.open(OUT / f"{n}.png") for n in ("helix-station", "habitat-pad", "dust-plume")] + [half("cliff-road", 3)]
    air = [half(n) for n in ("dust-veil", "narrows-dust")] + [Image.open(OUT / f"{n}.png") for n in ("arch-a", "arch-b")]
    comps = []
    for t, label in TIMES:
        img = composite(bd, t).convert("RGBA")
        raster.draw_text(img, 4, SCREEN - 12, f"T={t:g} {label}", raster.LABEL)
        comps.append(img)
    px = atlas_area()
    rows = [
        ("TILE SETS (HALF): NOCTIS-CHASM (FAR), NOCTIS-PLATEAU, CANYON-FLOOR, RIVERBED, EOS-CHAOS, PLATEAU (X1.25)", tiles, 1, False),
        ("WALL TEXTURES (2X, 256 OF THEIR ROWS; FOOT AT THE LEFT): CANYON-WALL, WALL-FOOT, MESA-FACE, PILLAR-FACE, CLIFF-HABITAT (FACE PATCH)", walls, 1, False),
        ("ISLAND TOPS AT 1.25: MESA-TOP_0..2 (1/3), PILLAR-TOP (1X)", tops, 1, False),
        ("SEAM PIECES (1/3): CANYON-MOUTH, SEAM-RAPIDS, SEAM-NARROWS, SEAM-EOS", seams, 1, False),
        ("PIECES: HELIX-STATION (PLATEAU), HABITAT-PAD, DUST-PLUME; CLIFF-ROAD (1/3)", pieces, 1, False),
        ("LOW-AIR (HALF): DUST-VEIL, NARROWS-DUST; HIGH-AIR ARCHES (ALPHA <= 53 %, DRAWN AT 75 %)", air, 1, False),
        ("COMPOSITES: THE WALLS PROJECTED AS WALLPROJECTION WILL (STAND-INS: ANCHORS, TENDRIL, HUT, ROVERS, SHIP)", comps[0:3], 1, False),
        ("", comps[3:6], 1, False), ("", comps[6:9], 1, False), ("", comps[9:12], 1, False), ("", comps[12:], 1, False),
    ]
    sheet = artkit.review_sheet(f"LEVEL 16 BACKDROP - VALLES CANYON RUN ({px / 1e6:.2f} M PX, ABOUT {px / 2048 ** 2:.1f} PAGES)",
                                rows, width=1520, batch=BATCH)
    REVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(REVIEW, optimize=True)
    frames = [composite(bd, float(t)).resize((240, 270), Image.BOX)
              for t in list(np.arange(14.0, 24.0, 0.25)) + list(np.arange(64.0, 72.0, 0.25)) + list(np.arange(98.0, 104.0, 0.25))]
    from render.enemy_rigs import write_gif
    write_gif(frames, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif ({px / 1e6:.2f} M px of backdrop images)")


def main(argv):
    if "--proposal" in argv:
        write_proposal()
        return
    if "--review" in argv:
        review()
        return
    if "--strip" in argv:
        i = argv.index("--strip")
        bd = proposal_backdrop()
        imgs = [composite(bd, float(v)) for v in argv[i + 1].split(",")]
        out = Image.new("RGB", (W * len(imgs) + 10 * (len(imgs) - 1), SCREEN))
        for k, im in enumerate(imgs):
            out.paste(im, (k * (W + 10), 0))
        out.save(argv[i + 2])
        return
    wanted = [a for a in argv if not a.startswith("--")]
    write_all(wanted)
    if not wanted:
        write_proposal()
        review()


if __name__ == "__main__":
    main(sys.argv[1:])
