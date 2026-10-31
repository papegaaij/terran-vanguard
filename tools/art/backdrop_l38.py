#!/usr/bin/env python3
"""Production art: the Level 38 backdrop, Lightning Belt (design/campaign/act-6-jovian-storm/level-38-lightning-belt;
M10 part D batch, concept round 76, step ART-D): the storm layer under the cloud cities.

Straight to production from round 72's chosen look: Q10 a's **L38 grade** (tools/concept/act6_r72.py ``sky_l38``:
storm-dark blue-grey and slate bands, purple-grey cloud masses lit from the top left, the Red Spot's rim a far rust
glow; mean ~82, the readability set over its darkest window ~52). The user's round-72 rule, "clouds at different
altitudes do not have different colors", holds inside the level: every cloud here - the floor's billows, the walls, the
cells, the Red Spot's wall, the breakout's tops - is drawn from one purple-grey ramp (``CLOUD``, sky_l38's tones). Every
band, haze, cloud and glow is a float field written unquantised (far more than 32 steps, no ordered dither: the
2026-10-30 dither audit, ``DitherGuardTest``); every glow is additive (premultiplied on black) and drawn once.

The layers (``scroll_factors``): ``deep`` 0.15 (the storm's floor far below: one continuous field down the whole level,
cut into the five sections' tile sets at the rows the game shows them (``LevelData.seam``), so the section changes have
no seam on the screen; each tile's rows past its section crossfade back to its own first rows, so it wraps), the deep
pieces over it (Aurelia's wreck, the cloud walls and the storm cells, each with its inner lightning as a ``-glow``
piece the game draws additively at the piece's places on the real clock: Level 36's storm wall's mechanism),
``low-air`` 1.3 (section 1: Aurelia's deck plates, a cable coil and torn balloon fabric falling past, scenery without a
hit box), ``high-air`` 1.7 (the heavy atmosphere's ``storm-scud``: pale scud streaks drawn additively at the layer's
40 %, the walls' rolling over the play plane). No ground (``ground: none``); no foreground banks (Level 36's).

The deep field by section (the README's Layout): 1 **Under Aurelia** the storm's top, its cloud heads a step lighter
under the cool light from above; 2 **The Storm Wall** the heavy grade, deep creases between the masses; 3 **The Cells**
the same floor under the towering cells (pieces); 4 **The Red Spot's Rim** a vast curving wall of cloud far off on the
right, an arc of the Great Red Spot's rim, its rust swirl inside and a rust glow out of it over the floor; 5 **The
Breakout** the cloud tops thinning over the section, the gaps opening on black sky and stars, **Io's disc** (sulphur
yellow, lit from the top left) rising into the top half by the end. **Re-run after the data moves a section** (the
tiles are cut at the sections' rows and the pieces are placed by time).

Tile sets (480 wide; ``deep`` opaque):
  storm-top     deep: section 1, the storm's top (lighter heads)
  storm-floor   deep: section 2, the heavy storm floor
  storm-cells   deep: section 3, the floor under the cells
  red-spot-rim  deep: section 4, the Red Spot's curving wall and its rust glow on the right
  breakout      deep: section 5, the tops thinning to black sky, stars and Io's disc
  storm-scud    high-air 480x960: the heavy atmosphere's ``wisps``: pale scud streaks (additive at 40 %)
Pieces (``-glow`` = additive at its piece's placements on the real clock; frame 0 is each glow's brightest flash, the
frame the game's ``StormLooks`` can draw brighter on ``STRIKE_TOLD``; the dark frames are empty and alias in the atlas):
  aurelia-wreck        deep 256x208: ART-C2's ``sinking-city`` (tools/art/backdrop_l37.py, Level 37's files copied
                       here) in the storm's grade: the city a dark burning shape far above, smoke rising
  aurelia-wreck-glow   deep 256x208, 4 frames at 6 fps: its fires (Level 37's glow at 70 %: its lights going out)
  cloud-wall-a, -b     deep 320x480, 288x448: towering cloud walls (the one purple-grey), soft edges all round
  cloud-wall-a/-b-glow deep, 24 frames at 6 fps (a 4 s loop): lightning inside the wall (blue-white light where the
                       cloud is thick, a bolt fragment on the strongest; three flashes a loop, each a peak frame and
                       at most one fading one, so few distinct frames reach the atlas)
  storm-cell-a, -b     deep 256x320, 224x288: storm cells from above: an overshooting dome with rings of billows, the
                       anvil smeared out on the wind, the cell's shadow on the floor (bottom right)
  storm-cell-a/-b-glow deep, 24 frames at 6 fps: lightning lighting the dome from inside
  debris-plate-a/-b/-c, debris-cable, balloon-torn  low-air: Level 37's fall debris (backdrop_l37.py) in the storm's
                       shade, falling past in section 1
Motion budget (checked): at most two animated pieces on the screen at once; a deep piece's edges never on the screen
opaque.

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout (five sections at 140 px/s). ``--proposal`` writes the
``backdrop:`` block (tile sets, pieces, the placements, the atmospheres), the sections and the cargo pod's and the
canister's ``debris`` to concept/backdrop-proposal.yaml (never into data.yaml).

Review: design/campaign/act-6-jovian-storm/level-38-lightning-belt/concept/backdrop-final-r76-a.png (twelve screens with
stand-ins, the readability crops at 1x, the pieces, the tile sets) and .gif (t 24-34 at half size: the first strike
alone, then the first Harrows).
Run: python3 tools/art/backdrop_l38.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the shared Jovian pieces: the clock, clouds, glows, the checks' measures)
import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
from jovkit import (additive_rgb, billow_height, blur, checker_share, cloud_mass, cut_tile, glow,  # noqa: E402
                    mass_blobs, rgba, smoothstep, soft_box, streaks, wrap_score, wrap_score_alpha)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l38.py"
BATCH = "M10 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r76"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-38-lightning-belt"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-38"
L37_OUT = ROOT / "assets" / "backdrop" / "level-37"
W, SCREEN = 480, 540
FACTORS = {"deep": 0.15, "low-air": 1.3, "high-air": 1.7}
SCUD_H = 960

# The L38 grade (Q10 a, act6_r72 sky_l38): the slate bands and the one cloud ramp of the level.
STORM = [(0.0, (16, 18, 28)), (0.3, (40, 44, 62)), (0.6, (72, 76, 98)), (0.85, (104, 106, 130)),
         (1.0, (140, 140, 162))]
CLOUD = [(0.0, (14, 14, 24)), (0.3, (34, 33, 48)), (0.55, (58, 56, 76)), (0.8, (88, 86, 108)),
         (1.0, (122, 120, 142))]
RUST = [(0.0, (40, 16, 12)), (0.35, (92, 38, 22)), (0.65, (150, 70, 38)), (1.0, (206, 128, 76))]
IO = [(0.0, (60, 48, 22)), (0.35, (138, 116, 52)), (0.65, (196, 172, 88)), (0.85, (226, 208, 128)),
      (1.0, (246, 236, 176))]
SKY = np.array((5, 7, 16.0))
HAZE = np.array((38, 42, 58.0))
FLASH = np.array((150, 172, 232.0))                 # the inner lightning's blue-white (never an enemy laser's red)
GLOW_RUST = np.array((210, 84, 44.0))

# =========================================================================== the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
DEEP = ["storm-top", "storm-floor", "storm-cells", "red-spot-rim", "breakout"]
SCUD = "storm-scud"
PROPOSED_SECTIONS = [
    {"name": "Under Aurelia", "end": 22, "speed": 140, "atmosphere": "medium", "tiles": ["storm-top"]},
    {"name": "The Storm Wall", "end": 62, "speed": 140, "atmosphere": "heavy", "tiles": ["storm-floor"]},
    {"name": "The Cells", "end": 109, "speed": 140, "atmosphere": "heavy", "tiles": ["storm-cells"]},
    {"name": "The Red Spot's Rim", "end": 150, "speed": 140, "atmosphere": "heavy", "tiles": ["red-spot-rim"]},
    {"name": "The Breakout", "end": 196, "speed": 140, "atmosphere": "medium", "tiles": ["breakout"],
     "peak": {"atmosphere": "light", "from": 172, "to": 196}},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", 140)) if DATA else 140.0
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam, section_at_row = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row
t_centre_at = CLOCK.t_centre_at
ATMOS = {"light": {"haze": 0.0}, "medium": {"haze": 0.0}, "heavy": {"haze": 0.1, "wisps": SCUD}}


def atmosphere_at(t):
    for i, s in enumerate(SECTIONS):
        if t < float(s["end"]) or i == len(SECTIONS) - 1:
            pk = s.get("peak")
            if pk and pk["from"] <= t < pk["to"]:
                return pk["atmosphere"]
            return s["atmosphere"]


# =========================================================================== the deep field (rows = layer rows, y-up)

def deep_span():
    """Layer rows the level shows on the deep layer: from the start's bottom edge to the end's top (+4 s)."""
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def io_place():
    """Io's disc: centre (x, layer row) and radius - at y 170 (down) at the level's end."""
    return 330.0, scroll(END) * FACTORS["deep"] + SCREEN - 170, 46.0


def red_spot():
    """The Red Spot's rim: a circle far off on the right, its centre (x, layer row) and radius."""
    s3, s4 = seam("deep", 3), seam("deep", 4)
    return 1040.0, (s3 + s4) / 2, 750.0


def deep_field(seed=3831):
    """The whole deep layer as one float RGB field, row 0 = layer row ``r0`` (y-up). Returns (rgb, r0)."""
    lo, hi = deep_span()
    r0, r1 = lo - 1600, hi + 1600
    n = r1 - r0
    r = (np.arange(n) + r0).astype(np.float64)[:, None] * np.ones((1, W))
    x = np.ones((n, 1)) * np.arange(W, dtype=np.float64)[None, :]
    s1, s2, s3, s4 = [seam("deep", i) for i in range(1, len(SECTIONS))]

    def w_after(s, width=150):
        return smoothstep(s - width / 2, s + width / 2, r)
    w1, w2, w3, w4 = w_after(s1), w_after(s2), w_after(s3), w_after(s4)
    k1, k5 = 1 - w1, w4
    k4 = w3 * (1 - w4)
    storm = w1 * (1 - w4)                                          # sections 2-4: the heavy grade
    span5 = max(1.0, hi - s4)
    thin = smoothstep(s4 + 0.05 * span5, s4 + 0.72 * span5, r)    # section 5: the tops thinning
    # the slate bands (sky_l38's bands: warped, streaked), a little finer on the storm's top
    period = 74 * k1 + 96 * (1 - k1)
    phase = np.cumsum(TAU / period[:, 0])[:, None]
    warp = raster.fbm(W, n, 96, seed, octaves=3, period=False)
    st = streaks(n, W, seed + 2)
    v = 0.5 + 0.5 * np.sin(phase + (warp - 0.5) * 3.2 + 0.35 * np.sin(x / 90 + r / 410))
    v = np.clip(0.5 * v + 0.5 * st, 0, 1)
    base = raster.ramp(STORM, np.clip(0.2 + 0.42 * v + 0.06 * k1, 0, 1))
    # section 5: the gaps open on black sky with stars; Io's disc
    sky = np.zeros((n, W, 3)) + SKY
    srng = np.random.default_rng(seed + 9)
    for _ in range(int(span5 * W / 900)):
        i, j = srng.integers(int(s4 - r0), n), srng.integers(0, W)
        b = srng.uniform(40, 210)
        sky[i, j] += (b, b * 0.97, b * 0.9)
    ix, ir, irad = io_place()
    d_io = np.hypot(x - ix, r - ir)
    on_io = smoothstep(irad + 0.8, irad - 0.8, d_io)
    if on_io.any():
        nx, ny = (x - ix) / irad, (r - ir) / irad                 # y-up: +ny toward the top
        nz = np.sqrt(np.clip(1 - nx ** 2 - ny ** 2, 0, 1))
        sun = np.array((-0.62, 0.5, 0.6))
        sun /= np.linalg.norm(sun)
        lit = np.clip(nx * sun[0] + ny * sun[1] + nz * sun[2], 0, 1)
        mott = raster.fbm(W, n, 10, seed + 21, octaves=4, period=False)
        spots = smoothstep(0.62, 0.72, raster.fbm(W, n, 7, seed + 22, octaves=3, period=False))
        tone = np.clip(0.35 + 0.5 * mott - 0.35 * spots, 0, 1)
        io = raster.ramp(IO, tone) * (0.12 + 0.95 * lit ** 0.8)[..., None]
        io += (smoothstep(0.82, 0.9, mott) * lit)[..., None] * np.array((60, 26, 4.0))   # orange vents
        sky = sky * (1 - on_io[..., None]) + io * on_io[..., None]
    sky_w = thin * k5
    base = base * (1 - sky_w[..., None]) + sky * sky_w[..., None]
    # the cloud masses (the one purple-grey): broad towering masses with dark creases between them, the floor's
    # bands showing in the gaps; thinning in section 5
    rng = np.random.default_rng(seed + 7)
    blobs = []
    for _ in range(int(n / 40)):
        cy = rng.uniform(0, n)
        row = float(r0 + cy)
        if row > s4 and rng.uniform() < 0.85 * smoothstep(s4, s4 + 0.7 * span5, row):
            continue
        k = 0.85 if row < s1 else 1.0
        blobs += mass_blobs(rng, rng.uniform(-60, W + 60), cy, rng.uniform(60, 170) * k, rng.uniform(36, 90) * k, 26,
                            (16 * k + 3, 38 * k + 4))
    shade, cov = cloud_mass(n, W, blobs, seed + 8, detail=0.55)
    shade = np.clip(0.06 + shade + 0.08 * k1, 0, 1)                 # the storm's top: the heads lit a step more
    clouds = raster.ramp(CLOUD, shade)
    cov = cov * (0.9 * k1 + 0.94 * storm + k5 * 0.94 * (1 - 0.96 * thin))
    rgb = base * (1 - cov[..., None]) + clouds * cov[..., None]
    # the Red Spot's rim (section 4): its rust swirl inside, a curving wall of cloud on the rim, a rust glow out of it
    cx, cr, R = red_spot()
    d = np.hypot(x - cx, r - cr) - R                              # < 0 inside the spot
    ang = np.arctan2(r - cr, x - cx)
    swirl = 0.5 + 0.5 * np.sin(d / 16 + (warp - 0.5) * 4 + ang * 6)
    inside = smoothstep(10, -40, d) * k4
    spot = raster.ramp(RUST, np.clip(0.12 + 0.22 * blur(swirl, 2.0) + 0.5 * np.exp(d / 55), 0, 1)) * 0.55
    spot = spot * 0.8 + HAZE * 0.2
    rgb = rgb * (1 - inside[..., None]) + spot * inside[..., None]
    rim_rng = np.random.default_rng(seed + 31)
    rim_blobs = []
    for a in np.linspace(np.pi * 0.6, np.pi * 1.4, 70):
        px, py = cx + np.cos(a) * (R + 10), cr + np.sin(a) * (R + 10)
        if not (-80 < px < W + 80 and r0 < py < r0 + n):
            continue
        rim_blobs += mass_blobs(rim_rng, px, py - r0, 46, 40, 9, (12, 32))
    if rim_blobs:
        rshade, rcov = cloud_mass(n, W, rim_blobs, seed + 32, detail=0.35)
        rcov = rcov * k4
        under = smoothstep(40, -10, d)                             # the wall's side toward the spot, lit rust
        wall = raster.ramp(CLOUD, np.clip(rshade, 0, 1))
        wall = wall * (1 - 0.3 * under[..., None]) + (0.3 * under)[..., None] * GLOW_RUST * (0.25 + 0.6 * rshade[..., None])
        rgb = rgb * (1 - rcov[..., None]) + wall * rcov[..., None]
    rim_glow = np.exp(-np.clip(d, 0, None) / 140) * smoothstep(-50, 20, d) * 0.2 * k4
    rgb += rim_glow[..., None] * GLOW_RUST * (0.5 + 0.5 * (1 - cov[..., None]))
    # the haze (a step over the far floor) and the light from the top left
    rgb = rgb * 0.9 + HAZE * 0.1 * (1 - sky_w[..., None] * 0.8)
    rgb *= (1.0 + 0.07 * (1 - x / W))[..., None]
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], bounds[i + 1]) for i, name in enumerate(DEEP)}


# =========================================================================== high-air: the scud

def storm_scud(seed=3841):
    """Pale scud streaks along the wind (additive at the layer's 40 %): soft, ragged, a little longer than the ice
    haze of Level 36; wraps top to bottom."""
    h = SCUD_H
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    alpha = np.zeros((h, W))
    for _ in range(18):
        cx, cy = rng.uniform(-80, W + 80), rng.uniform(0, h)
        length, thick = rng.uniform(160, 360), rng.uniform(6, 16)
        tilt = rng.uniform(-0.08, 0.04)
        for dy in (-h, 0, h):
            u = (xx - cx) / (length / 2)
            vv = (yy - cy - dy - (xx - cx) * tilt) / thick
            alpha = np.maximum(alpha, np.exp(-vv ** 2) * np.clip(1 - u ** 2, 0, 1) ** 1.4 * rng.uniform(0.6, 1))
    tex = raster.fbm(W, h, 20, seed + 1, octaves=4, period=True)
    alpha = np.clip(alpha * (0.35 + 1.0 * tex), 0, 1)
    alpha = blur(alpha, 1.2, True) * 0.4
    rgb = np.zeros((h, W, 3)) + np.array((150, 160, 192.0))
    return rgba(rgb, alpha)


# =========================================================================== deep pieces

WALLS = {"cloud-wall-a": ((320, 480), 3851), "cloud-wall-b": ((288, 448), 3853)}
CELLS = {"storm-cell-a": ((256, 320), 3861), "storm-cell-b": ((224, 288), 3863)}
# lightning inside a mass, 24 frames at 6 fps (a 4 s loop): frame -> (spot, strength); frame 0 the brightest
FLASHES = {"cloud-wall-a": {0: (0, 1.0), 1: (0, 0.4), 9: (1, 0.75), 16: (2, 0.9), 17: (2, 0.35)},
           "cloud-wall-b": {0: (1, 1.0), 1: (1, 0.4), 7: (2, 0.7), 14: (0, 0.85)},
           "storm-cell-a": {0: (0, 1.0), 1: (0, 0.45), 12: (1, 0.8)},
           "storm-cell-b": {0: (0, 1.0), 1: (0, 0.4), 15: (1, 0.7)}}
GLOW_FRAMES, GLOW_FPS = 24, 6


def cloud_wall(name):
    """A towering wall of cloud: column clusters of heads, lit from the top left, its core dark; soft edges all round.
    Returns (image, height field, flash spots)."""
    (w, h), seed = WALLS[name]
    rng = np.random.default_rng(seed)
    blobs = []
    for k in range(10):
        blobs += mass_blobs(rng, rng.uniform(0.38 * w, 0.62 * w), 50 + k * (h - 110) / 9, 0.28 * w, 34, 36, (16, 40))
    hgt = billow_height(h, w, blobs)
    shade, cov = cloud_mass(h, w, blobs, seed + 1)
    rgb = raster.ramp(CLOUD, np.clip(0.04 + 1.0 * shade, 0, 1))
    # its shadow on the floor (the wall towers: offset further to the bottom right than a cell's)
    sh = np.roll(np.roll(cov, 18, 0), 16, 1)
    sh[:18] = 0
    sh[:, :16] = 0
    shadow = blur(sh, 5.0) * 0.45 * (1 - cov)
    alpha = np.clip(cov + shadow, 0, 1)
    rgb = (rgb * cov[..., None] + np.array((6, 6, 12.0)) * shadow[..., None]) / np.maximum(alpha, 1e-6)[..., None]
    a = alpha * soft_box(w, h, 26) * 0.97
    spots = [(0.45 * w, 0.3 * h, 0.15 * w), (0.6 * w, 0.58 * h, 0.13 * w), (0.4 * w, 0.78 * h, 0.11 * w)]
    return rgba(rgb, a), hgt, spots


def storm_cell(name):
    """A storm cell from above: the overshooting dome (the tallest heads, lit), rings of lower billows round it, the
    anvil smeared out to the left on the wind (smooth streaks), the cell's shadow on the floor to the bottom right.
    Returns (image, height field, flash spots)."""
    (w, h), seed = CELLS[name]
    rng = np.random.default_rng(seed)
    cx, cy = 0.56 * w, 0.46 * h
    blobs = mass_blobs(rng, cx, cy, 0.16 * w, 0.14 * h, 30, (16, 30))
    for k in range(14):
        a = TAU * k / 14 + rng.uniform(-0.2, 0.2)
        rr = rng.uniform(0.24, 0.32) * w
        blobs += mass_blobs(rng, cx + np.cos(a) * rr, cy + np.sin(a) * rr * 0.9, 24, 20, 6, (7, 15))
    hgt = billow_height(h, w, blobs)
    shade, cov = cloud_mass(h, w, blobs, seed + 1)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    # the anvil: a smooth smear of cloud off the dome to the left
    u = (cx - xx) / (0.55 * w)
    vv = (yy - cy - 0.1 * (cx - xx)) / (0.16 * h + 0.12 * h * np.clip(u, 0, 1))
    anvil = np.exp(-vv ** 2) * smoothstep(-0.1, 0.15, u) * np.exp(-np.clip(u, 0, None) * 1.5)
    st = streaks(h, w, seed + 3, cell=6, squeeze=8)
    anvil = np.clip(anvil * (0.75 + 0.5 * st), 0, 1) * 0.85
    a_cov = np.maximum(cov, anvil * (1 - cov))
    a_shade = shade * cov + (0.42 + 0.25 * st) * anvil * (1 - cov)
    a_shade = a_shade / np.maximum(a_cov, 1e-6)
    dome = np.exp(-((xx - cx) ** 2 + (yy - cy) ** 2) / (0.14 * w) ** 2)
    rgb = raster.ramp(CLOUD, np.clip(0.08 + 0.9 * a_shade + 0.12 * dome, 0, 1))
    # the shadow on the floor (offset to the bottom right), under the cloud
    sh = np.roll(np.roll(a_cov, 14, 0), 12, 1)
    sh[:14] = 0
    sh[:, :12] = 0
    shadow = blur(sh, 4.0) * 0.42 * (1 - a_cov)
    alpha = np.clip(a_cov + shadow, 0, 1)
    col = (rgb * a_cov[..., None] + np.array((6, 6, 12.0)) * shadow[..., None]) / np.maximum(alpha, 1e-6)[..., None]
    alpha = alpha * soft_box(w, h, 22) * 0.97
    spots = [(cx, cy, 0.13 * w), (cx - 0.22 * w, cy + 0.04 * h, 0.1 * w)]
    return rgba(col, alpha), hgt, spots


def flash_frames(hgt, spots, plan, seed):
    """24 frames: soft blue-white light inside the cloud (strongest where it is thick), a short bolt fragment on the
    strongest flashes; frames without a flash are empty (alpha 0)."""
    h, w = hgt.shape
    thick = smoothstep(4, 30, hgt)
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    frames = []
    for f in range(GLOW_FRAMES):
        light = np.zeros((h, w, 3))
        if f in plan:
            spot, k = plan[f]
            sx, sy, sr = spots[spot]
            field = np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / (sr * sr * 2.2)) * thick
            light += field[..., None] * FLASH * k
            if k >= 0.75:
                x, y = sx + rng.uniform(-8, 8), sy - sr * 0.5
                for _ in range(9):
                    nx, ny = x + rng.uniform(-6, 6), y + rng.uniform(5, 10)
                    for uu in np.linspace(0, 1, 8):
                        glow(light, x + (nx - x) * uu, y + (ny - y) * uu, 0.9, (230, 240, 255), 1.0 * k)
                    x, y = nx, ny
        frames.append(additive_rgb(light))
    return frames


# =========================================================================== Level 37's pieces, in the storm

WRECK = (256, 208)
LOW_COPIES = ["debris-plate-a", "debris-plate-b", "debris-plate-c", "debris-cable", "balloon-torn"]


def graded(img, k, tint, mix):
    a = np.array(img.convert("RGBA")).astype(np.float64)
    a[..., :3] = a[..., :3] * k * (1 - mix) + np.asarray(tint, float) * mix
    a[..., :3] *= a[..., 3:4] > 0
    return Image.fromarray(np.clip(np.round(a), 0, 255).astype(np.uint8), "RGBA")


def wreck_images():
    """ART-C2's sinking city (Level 37's files) in the storm's grade: cooler and a step darker; its fires at 70 %."""
    body = graded(Image.open(L37_OUT / "sinking-city.png"), 0.82, (30, 32, 48), 0.2)
    glows = []
    for k in range(4):
        g = np.array(Image.open(L37_OUT / f"sinking-city-glow_{k}.png").convert("RGBA")).astype(np.float64)
        rgb = np.round(g[..., :3] * 0.7)
        alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
        glows.append(Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA"))
    return body, glows


def low_copies():
    return {n: graded(Image.open(L37_OUT / f"{n}.png"), 0.6, (28, 30, 44), 0.22) for n in LOW_COPIES}


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
    save(SCUD, storm_scud())
    body, glows = wreck_images()
    save("aurelia-wreck", body)
    for i, g in enumerate(glows):
        save(f"aurelia-wreck-glow_{i}", g)
    for name in WALLS:
        img, hgt, spots = cloud_wall(name)
        save(name, img)
        for i, f in enumerate(flash_frames(hgt, spots, FLASHES[name], WALLS[name][1] + 5)):
            save(f"{name}-glow_{i}", f)
    for name in CELLS:
        img, hgt, spots = storm_cell(name)
        save(name, img)
        for i, f in enumerate(flash_frames(hgt, spots, FLASHES[name], CELLS[name][1] + 5)):
            save(f"{name}-glow_{i}", f)
    for name, img in low_copies().items():
        save(name, img)
    return pieces_spec(), placements()


def tile_heights():
    return {name: Image.open(OUT / f"{name}.png").height for name in DEEP + [SCUD]}


def pieces_spec():
    spec = {"aurelia-wreck": {"layer": "deep", "size": list(WRECK)},
            "aurelia-wreck-glow": {"layer": "deep", "size": list(WRECK), "frames": 4, "fps": 6}}
    for group in (WALLS, CELLS):
        for name, (size, _) in group.items():
            spec[name] = {"layer": "deep", "size": list(size)}
            spec[name + "-glow"] = {"layer": "deep", "size": list(size), "frames": GLOW_FRAMES, "fps": GLOW_FPS}
    for name in LOW_COPIES:
        spec[name] = {"layer": "low-air", "size": list(Image.open(OUT / f"{name}.png").size)}
    return spec


# =========================================================================== the places

# deep pieces: (piece, time its top enters the screen's top, x, note); at 21 px/s a piece crosses in (540 + h) / 21 s
DEEP_PLAN = [("cloud-wall-a", 18.0, 120, "section 2's first wall, on the left; on the screen t~18-67"),
             ("cloud-wall-b", 38.0, 368, "the second wall, on the right; t~38-85"),
             ("storm-cell-a", 67.5, 120, "section 3's first cell, on the left; t~67-108"),
             ("storm-cell-b", 86.0, 362, "the second cell, on the right; t~86-125"),
             ("cloud-wall-a", 109.0, 112, "section 4: a wall on the left, the Red Spot's rim on the right; t~109-158"),
             ("storm-cell-a", 132.0, 150, "section 4's cell over the rim's edge; t~132-173")]
# low-air: (piece, t its centre crosses the middle, x): section 1's debris falling past (no hit box)
LOW_PLAN = [("debris-plate-a", 2.6, 96), ("balloon-torn", 5.0, 384), ("debris-cable", 7.6, 206),
            ("debris-plate-b", 10.2, 420), ("debris-plate-c", 12.4, 62), ("balloon-torn", 15.2, 300),
            ("debris-plate-a", 18.0, 150), ("debris-cable", 20.4, 404)]


def placements():
    f = FACTORS["deep"]
    out = [{"name": "aurelia-wreck", "t": round(t_centre_at(0.0, 150.0, f), 3), "x": 300,
            "note": "Aurelia's wreck sinking far above: centred at y 150 at the start, gone by t~23 (section 1); its "
                    "glow (the fires) is drawn at this place"}]
    for name, te, x, note in DEEP_PLAN:
        h = (WALLS.get(name) or CELLS.get(name))[0][1]
        out.append({"name": name, "t": round(t_centre_at(te, -h / 2 + 6, f), 3), "x": x, "note": note})
    for name, t, x in LOW_PLAN:
        out.append({"name": name, "t": t, "x": x, "note": None})
    return out


# =========================================================================== proposal

DEBRIS = """debris:
  # Proposal (ART-D, tools/art/storm_layer_units.py's sprites): Aurelia's cargo pod (the secret, `reveals`, 20 HP;
  # README: t~118 down the left edge, lights dead; a strike crossing it opens it too, D1) and the CDF supply canister
  # (t~100 down the middle on its parachute, the friendly brackets; its 10 HP latch frees the armour patch: D1's drop
  # and marked flag, the keys D2's). Sprites debris-cargo-pod_0..15 (36x36, tumble) with -outline_0..15 (additive:
  # the lightning flash's 0.5 s outline) and -break_0..7; debris-canister_0..7 (64x128 centred on the canister, the
  # sway at 4 fps), -latch_0..7 (the overlay: QD-4 b leaves it out) and -release_0..9.
  chunks:
    cargo-pod: {size: [30, 30], hp: 20}
    canister: {size: [22, 30], hp: 10}   # the canister and its latch; the canopy above it is no target
  clearance: 0
  placed:
    - {t: 118, x: 40, chunk: cargo-pod, drift: [4, -70], reveals: Aurelia cargo pod}   # the crate: 100 (README)
    - {t: 100, x: 240, chunk: canister, drift: [0, -60]}   # + D1's drop: armour-patch, marked: true
"""


def proposal_text(pieces, placed):
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    h = tile_heights()
    lines = ["backdrop:",
             f"  # Presentation only: Level 38's storm layer (tools/art/backdrop_l38.py, step ART-D, sections from {src}).",
             "  # No ground (`ground: none`): the deep layer is the base. Its five tile sets are cut from one continuous",
             "  # field at the rows each section's tiles begin (LevelData.seam): re-run the generator after moving a",
             "  # section. Animated: the -glow pieces (the walls' and cells' inner lightning, the wreck's fires), additive",
             "  # at their piece's placements on the real clock, at most two on the screen at once (checked); frame 0 of",
             "  # each wall/cell glow is its brightest flash (StormLooks' brighter pulse on STRIKE_TOLD can draw it).",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, ground unused",
             "  ramp: 2",
             "  haze_colour: 262a3a   # the storm's slate",
             "  atmosphere:",
             "    light: {haze: 0.0}   # the breakout's end",
             "    medium: {haze: 0.0}   # sections 1 and 5: the grade is in the tiles",
             f"    heavy: {{haze: 0.1, wisps: {SCUD}}}   # sections 2-4: a step of slate over the floor, the scud passing over",
             "  tile_sets:"]
    notes = {"storm-top": "section 1: the storm's top", "storm-floor": "section 2: the heavy storm floor",
             "storm-cells": "section 3: the floor under the cells", "red-spot-rim": "section 4: the Red Spot's rim",
             "breakout": "section 5: thinning to black sky, stars, Io"}
    for name in DEEP:
        lines.append(f"    {name}: {{layer: deep, height: {h[name]}}}   # {notes[name]}")
    lines.append(f"    {SCUD}: {{layer: high-air, height: {h[SCUD]}}}   # the heavy atmosphere's scud (additive)")
    lines.append("  pieces:")
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}}}")
    lines.append("sections:   # ART-D's reading of the README's Layout (140 px/s throughout); D2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - " + "{" + ", ".join(f"{k}: {fmt(v)}" for k, v in s.items() if k != "notes") + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); deep "
                 f"{int(round(scroll(END) * FACTORS['deep']))}, high-air {int(round(scroll(END) * FACTORS['high-air']))}")
    return "\n".join(lines) + "\n" + DEBRIS


def fmt(v):
    if isinstance(v, list):
        return "[" + ", ".join(str(x) for x in v) + "]"
    if isinstance(v, dict):
        return "{" + ", ".join(f"{k}: {fmt(x)}" for k, x in v.items()) + "}"
    if isinstance(v, str) and ("'" in v or ":" in v):
        return '"' + v + '"'
    return str(v)


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["debris"]


# =========================================================================== composite

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


def layer_tiles(layer, t):
    """The screen's rows of the deep layer at t, each from its section's tile set (the game's seams)."""
    off = scroll(t) * FACTORS[layer]
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec["tiles"] if n in DEEP]
        if not names:
            continue
        arr, rows = tile_rows(names[0], off)
        sel = np.array([section_at_row(layer, r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def scud_rows(t):
    arr, _ = tile_rows(SCUD, scroll(t) * FACTORS["high-air"])
    return arr


def blit(img, p, pieces, t, layer, glow=False):
    name = p["piece"] + ("-glow" if glow else "")
    spec = pieces[name]
    if spec["layer"] != layer:
        return img
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


def compose(t, units=True, difficulty="medium", strike=None):
    """The screen at t (480x540): deep tiles under the atmosphere's haze, the deep pieces (glows added), the low-air
    debris, the play plane's stand-ins, a told or striking column (``strike`` = (x, t0) of its telegraph), the
    high-air scud (additive at 40 % in the heavy atmosphere)."""
    pieces, placed, debris = load_proposal()
    img = layer_tiles("deep", t)
    haze = ATMOS[atmosphere_at(t)]["haze"]
    if haze:
        a = np.array(img).astype(np.float64)
        a[..., :3] = a[..., :3] * (1 - haze) + np.array((0x26, 0x2a, 0x3a), float) * haze
        img = Image.fromarray(np.round(a).astype(np.uint8), "RGBA")
    for p in placed:
        if pieces[p["piece"]]["layer"] != "deep":
            continue
        img = blit(img, p, pieces, t, "deep")
        if p["piece"] + "-glow" in pieces:
            img = blit(img, p, pieces, t, "deep", glow=True)
    for p in placed:
        img = blit(img, p, pieces, t, "low-air")
    if units:
        img = stand_ins(img, t, debris)
    if strike:
        img = column(img, t, *strike)
    if units:
        img = ship_and_shots(img, t)
    if ATMOS[atmosphere_at(t)].get("wisps"):
        a = np.array(img).astype(np.float64)
        s = scud_rows(t).astype(np.float64)
        a[..., :3] = np.clip(a[..., :3] + s[..., :3] * (s[..., 3:4] / 255) * 0.4, 0, 255)
        img = Image.fromarray(np.round(a).astype(np.uint8), "RGBA")
    return img


def column(img, t, x, t0):
    """A strike's column: the tell tile down the column from t0 for 1.0 s (brightening), then the strike 0.25 s with a
    sky flash (Level 12's look, at the review's 0.3)."""
    tell, strike = load_held("lightning-tell"), load_held("lightning-strike")
    if not tell or not strike:
        return img
    u = t - t0
    if 0 <= u < 1.0:
        tile = tell[1 if u > 0.6 else 0]
        for y in range(0, SCREEN + tile.height, tile.height):
            img = l23kit.add_c(img, tile, x, y + tile.height / 2, 0.4 + 0.6 * u)
    elif 1.0 <= u < 1.25:
        f = min(2, int((u - 1.0) / 0.25 * 3))
        img = l23kit.add_c(img, strike[f], x, SCREEN / 2, 1.0 if u < 1.15 else (1.25 - u) / 0.1)
        a = np.array(img).astype(np.float64)
        k = 0.3 * (1 - (u - 1.0) / 0.25)
        a[..., :3] = np.clip(a[..., :3] * (1 - k) + np.array((200, 220, 255.0)) * k, 0, 255)
        img = Image.fromarray(np.round(a).astype(np.uint8), "RGBA")
    return img


def heading_k(dx, dy):
    return int(round((np.degrees(np.arctan2(-dx, dy)) % 360) / 22.5)) % 16


# the stand-ins: (t0, kind, n, edge) - a few of the README's waves
STANDS = [(3.0, "chimera", 4, "top"), (14.0, "talon", 5, "top"), (30.5, "harrow", 4, "top"),
          (45.5, "chimera", 5, "top"), (66.0, "harrow", 3, "left"), (78.0, "buzzsaw-drone", 4, "top"),
          (111.0, "hornet", 2, "top"), (117.0, "harrow", 4, "right"), (137.0, "harrow", 4, "cross"),
          (142.0, "wraith", 3, "top"), (168.0, "chimera", 5, "top")]


def stand_ins(img, t, debris):
    body = {k: load_held(k) for k in ("harrow", "chimera", "talon", "buzzsaw-drone", "hornet", "wraith")}
    sac = load_held("harrow-sac")
    for t0, kind, n, edge in STANDS:
        u = t - t0
        if not 0 <= u <= 7 or not body.get(kind):
            continue
        fr = body[kind]
        for k in range(n):
            if kind == "harrow":
                uu = u - 0.6 * k
                if uu < 0:
                    continue
                if edge == "cross":
                    side = ("top", "left", "right", "bottom")[k % 4]
                else:
                    side = edge
                sx, sy, dx, dy = {"top": (180 + 40 * k, -20, 0, 1), "left": (-20, 140 + 50 * k, 1, 0.35),
                                  "right": (500, 120 + 50 * k, -1, 0.35), "bottom": (300, 560, -0.2, -1)}[side]
                x, y = sx + dx * 110 * uu, sy + dy * 110 * uu
                hk = heading_k(dx, dy)
                img = l23kit.add_c(l23kit.paste_c(img, fr[hk], x, y), sac[hk] if sac else fr[hk], x, y,
                                   0.6 + 0.15 * np.sin(TAU * t))
            elif kind == "buzzsaw-drone":
                ang = TAU * (u / 3.0 + k / n)
                x, y = 240 + 110 * np.cos(ang), 200 + 70 * np.sin(ang) + 10 * u
                img = l23kit.paste_c(img, fr[int(t * 12) % len(fr)], x, y)
            else:
                x = 240 + (k - (n - 1) / 2) * 56
                y = -20 + 140 * u - abs(k - (n - 1) / 2) * 24 - 50 * max(0, u - 2.6) ** 2
                if kind == "hornet":
                    y = min(y, 150)
                img = l23kit.paste_c(img, fr[0], x, y)
    pod, can, latch = load_held("debris-cargo-pod"), load_held("debris-canister"), load_held("debris-canister-latch")
    for row in debris["placed"]:
        u = t - row["t"]
        if not 0 <= u < 9:
            continue
        x, y = row["x"] + row["drift"][0] * u, -20 - row["drift"][1] * u
        if row["chunk"] == "cargo-pod" and pod:
            img = l23kit.paste_c(img, pod[int(u * 4) % 16], x, y)
        if row["chunk"] == "canister" and can:
            img = l23kit.paste_c(l23kit.paste_c(img, can[int(u * 4) % 8], x, y), latch[int(u * 4) % 8], x, y)
    return img


def ship_and_shots(img, t):
    ship = artkit.load_frames("ship")
    orb, needle = load_held("orb"), load_held("needle")
    if 40 < t < 190 and orb and needle:
        for k in range(4):
            img = l23kit.paste_c(img, needle[0], 196 + 22 * k, 250 + 34 * k)
        img = l23kit.paste_c(img, orb[0], 330, 300)
    if ship:
        img = l23kit.paste_c(img, ship[2], 240, 470)
    return img


# =========================================================================== review

SHOTS = [(1.0, "UNDER AURELIA: THE WRECK, DEBRIS", None),
         (12.0, "UNDER AURELIA: THE STORM'S TOP", None),
         (26.6, "THE FIRST STRIKE'S TELL, ALONE (T=26)", (380, 26.0)),
         (27.08, "THE STRIKE AND THE SKY FLASH", (380, 26.0)),
         (32.0, "THE FIRST HARROWS (T=30.5)", None),
         (50.0, "THE STORM WALL: LIGHTNING INSIDE", None),
         (80.0, "THE CELLS: A BUZZSAW CIRCLE", None),
         (101.0, "THE CANISTER ON ITS PARACHUTE", None),
         (119.5, "THE RED SPOT'S RIM, THE POD", None),
         (140.0, "THE RIM: A HARROW CROSS, THE WALL", (300, 139.0)),
         (172.0, "THE BREAKOUT: TOPS THIN, STARS", None),
         (END - 0.5, "THE END: IO'S DISC", None)]


def windows(t, w=200, h=96):
    """The screen's darkest and brightest 200x96 windows at t (the moods sheet's readability windows), from the
    backdrop alone; returns ((lum, x, y), (lum, x, y)) and the screen's mean."""
    img = compose(t, units=False)
    lum = np.array(img.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    best = []
    for y in range(0, SCREEN - h, 12):
        for x in range(0, W - w, 12):
            best.append((lum[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return best[0], best[-1], lum.mean(), img


def read_cell(bg, x, y):
    """The readability set at 1x over a 200x96 window: a Harrow hunting and one in its tell, a Buzzsaw, needles and
    an orb, the ship."""
    c = bg.crop((x, y, x + 200, y + 96)).copy()
    har, sac, tell = load_held("harrow"), load_held("harrow-sac"), load_held("harrow-tell")
    buzz = load_held("buzzsaw-drone")
    orb, needle = load_held("orb"), load_held("needle")
    if har:
        c = l23kit.add_c(l23kit.paste_c(c, har[1], 24, 30), sac[1], 24, 30, 0.6)
        c = l23kit.add_c(l23kit.add_c(l23kit.paste_c(c, har[15], 64, 28), sac[15], 64, 28, 1.0), tell[8], 64, 28)
    if buzz:
        c = l23kit.paste_c(c, buzz[0], 106, 30)
    for k in range(3):
        c = l23kit.paste_c(c, needle[0], 140 + 10 * k, 18 + 12 * k)
    c = l23kit.paste_c(c, orb[0], 180, 30)
    ship = artkit.load_frames("ship")
    if ship:
        c = l23kit.paste_c(c, ship[2], 150, 70)
    return c


def review():
    pieces, placed, _ = load_proposal()
    shots = [(compose(t, strike=s), lab) for t, lab, s in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    reads_t = [(10.0, "S1"), (45.0, "S2"), (95.0, "S3"), (128.5, "S4"), (165.0, "S5")]
    read_h = 16 + 2 * (96 + 20)
    pieces_h = 300
    tile_h = 1300 // 6 + 40
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + read_h + 30 + pieces_h + tile_h + 30,
                         "LEVEL 38 BACKDROP (FINAL R76): LIGHTNING BELT", f"PRODUCTION ART, {BATCH.upper()} - R76")
    raster.draw_text(sheet, 16, 38, "TWELVE SCREENS AT HALF SIZE WITH STAND-INS (HELD FRAMES: HARROWS, CHIMERAS, TALONS, "
                                    "BUZZSAWS, HORNETS, WRAITHS, THE POD, THE CANISTER; THE STRIKE FROM ACT6_GEAR)",
                     raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X: EACH SECTION'S DARKEST (TOP) AND BRIGHTEST (BOTTOM) 200X96 WINDOW "
                                   "WITH A HARROW HUNTING AND ONE IN ITS TELL, A BUZZSAW, NEEDLES, AN ORB, THE SHIP",
                     raster.LABEL)
    means = []
    for j, (t, lab) in enumerate(reads_t):
        (dl, dx, dy), (bl, bx, by), mean, bg = windows(t)
        means.append((lab, t, mean, dl, bl))
        for i, (lum, wx, wy) in enumerate(((dl, dx, dy), (bl, bx, by))):
            c = read_cell(bg, wx, wy)
            x0, y0 = 16 + j * 204, y + 16 + i * 116
            sheet.alpha_composite(c, (x0, y0))
            raster.draw_text(sheet, x0, y0 + 98, f"{lab} T={t:.0f} {'DARKEST' if i == 0 else 'BRIGHTEST'} {lum:.0f}",
                             raster.LABEL_DIM)
    y += read_h + 14
    raster.draw_text(sheet, 16, y, "MEANS: " + "  ".join(f"{lab} {m:.0f} ({d:.0f}-{b:.0f})" for lab, _, m, d, b in means)
                     + "  (ROUND 72'S L38 GRADE: MEAN 82, DARKEST 52, BRIGHTEST 115)", raster.LABEL_DIM)
    y += 16
    raster.draw_text(sheet, 16, y, "THE DEEP PIECES (HALF SIZE): THE WRECK, THE WALLS, THE CELLS, EACH WITH ITS BRIGHTEST "
                                   "FLASH (FRAME 0) ADDED; THE LOW-AIR DEBRIS (1X)", raster.LABEL)
    x = 16
    plate = layer_tiles("deep", 60.0)
    for name in ["aurelia-wreck"] + list(WALLS) + list(CELLS):
        body = images(name, pieces[name])[0]
        g = images(name + "-glow", pieces[name + "-glow"])[0]
        c = plate.crop((0, 0, body.width, body.height)) if body.height <= SCREEN else plate.copy()
        c = l23kit.add_c(l23kit.paste_c(c.copy(), body, body.width / 2, body.height / 2), g, body.width / 2,
                         body.height / 2)
        c = c.resize((c.width // 2, c.height // 2), Image.BOX)
        sheet.alpha_composite(c, (x, y + 16))
        x += c.width + 10
    for name in LOW_COPIES:
        im = images(name, pieces[name])[0]
        c = plate.crop((0, 0, im.width + 4, im.height + 4)).copy()
        c.alpha_composite(im, (2, 2))
        sheet.alpha_composite(c, (x, y + 16))
        x += c.width + 6
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (SIXTH SIZE): STORM-TOP, STORM-FLOOR, STORM-CELLS, RED-SPOT-RIM, "
                                   "BREAKOUT; STORM-SCUD (ADDITIVE, ON BLACK)", raster.LABEL)
    x = 16
    for name in DEEP + [SCUD]:
        tl = Image.open(OUT / f"{name}.png").convert("RGBA")
        small = tl.resize((tl.width // 6, tl.height // 6), Image.BOX)
        pl = Image.new("RGBA", small.size, (0, 0, 0, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl, (x, y + 16))
        x += small.width + 10
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    frames = [compose(24.0 + i / 10, strike=(380, 26.0)).resize((sw, sh), Image.BOX) for i in range(100)]
    import enemies_r60 as e60  # noqa: E402
    size = e60.rig.write_gif(frames, gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")
    for lab, t, m, d, b in means:
        print(f"{lab} t={t}: mean {m:.0f}, darkest window {d:.0f}, brightest {b:.0f}")


# =========================================================================== check

def seam_jump(layer, i):
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
    """The deep tiles opaque and wrapping (BackdropSeamsTest's measure), the section seams invisible, the scud's wrap,
    sizes as the proposal, Source chunks, no checker (DitherGuardTest's measure under 5 %), the deep pieces' edges never
    on the screen opaque, at most two animated pieces on the screen at once, the atlas area."""
    problems = []
    pieces, placed, _ = load_proposal()
    for name in DEEP:
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    a = np.array(Image.open(OUT / f"{SCUD}.png").convert("RGBA"))
    if wrap_score_alpha(a) > 1.5:
        problems.append(f"{SCUD}: wrap score {wrap_score_alpha(a):.2f}")
    for i in range(1, len(SECTIONS)):
        j = seam_jump("deep", i)
        if j > 2.0:
            problems.append(f"deep seam {i}: a visible jump ({j:.2f}x the rows round it)")
    for name, spec in pieces.items():
        n = spec.get("frames")
        files = [OUT / f"{name}_{k}.png" for k in range(n)] if n else [OUT / f"{name}.png"]
        for f in files:
            im = Image.open(f)
            if list(im.size) != list(spec["size"]):
                problems.append(f"{f.name}: {im.size} vs {spec['size']}")
    vis = chk = 0
    distinct = {}
    for f in sorted(OUT.glob("*.png")):
        im = Image.open(f)
        if im.text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
        v, c = checker_share(im)
        if v and c / v >= 0.05:
            problems.append(f"{f.name}: {c / v * 100:.1f} % checker")
        vis, chk = vis + v, chk + c
        distinct.setdefault(im.tobytes() + bytes(str(im.size), "ascii"), (im.width + 2) * (im.height + 2))
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = set()
        for p in placed:
            for name in (p["piece"], p["piece"] + "-glow"):
                spec = pieces.get(name)
                if not spec or not spec.get("frames"):
                    continue
                f = FACTORS[spec["layer"]]
                y = SCREEN - (scroll(p["t"]) * f + SCREEN / 2 - scroll(t) * f)
                if y + spec["size"][1] / 2 > 0 and y - spec["size"][1] / 2 < SCREEN:
                    animated.add((name, p["t"]))
        if len(animated) > 2:
            problems.append(f"t={t}: {sorted(animated)} animated at once")
            break
    for p in placed:
        spec = pieces[p["piece"]]
        if spec["layer"] != "deep":
            continue
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
    area = sum(distinct.values())
    print(f"atlas: {len(distinct)} distinct images, {area / 1e6:.2f} M px with padding = "
          f"{area / 2048 ** 2:.2f} pages of 2048^2 before packing losses; checker {chk / max(vis, 1) * 100:.2f} % "
          f"of {vis} px")
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
