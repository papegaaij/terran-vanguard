#!/usr/bin/env python3
"""Production art: the Level 42 backdrop, Ascendant (design/campaign/act-6-jovian-storm/level-42-ascendant; M10 part H
batch, concept round 80, step ART-H2): Callisto orbit in vacuum, the Act 6 finale.

Straight to production from round 72's chosen look: Q10 a's **L42 grade** (design/world/jovian, *Moods per level*: black
space, Jupiter's limb and Callisto's curve, both fleets' running lights; the boss's gold and teal over black, the
friendly fleet marked) laid out as the level's Layout has it. Level 42 is in **space** (no ground), so Jupiter's limb
shows (the user's no-Jupiter rule is for the top-down ground levels): black space and sparse stars, Jupiter's cream limb
along the right edge the whole level (a giant disc far off: its lit crescent and a thin bright line, the night side
behind it), **Callisto's curve below** the whole level (its night side: dark blue-grey ice in Jupiter's warm shine from
the right, a thin cool rim along the limb, the black citadel's gold lights on it, steady), the CDF fleet closing from
below in section 1 and again after the escape, the home fleet's lights far ahead in sections 1-2, **burning hulks
falling** toward Callisto in section 3 (two broken Regent-class hulls, Level 28's cruiser model unchanged, cut along a
jagged break, venting fire at the breaches and trailing sparks; they fall behind Callisto's limb), the **Ascendant's
gold glint** high ahead from section 3, its **drive glow** growing as the scroll slows in section 4 and swinging away
(it comes about) before the bow slides in at t=92, and in the **empty orbit** after the escape the lifeboat's last flash
toward the gate at the top and the CDF fleet's lights closing in. Every haze, limb and glow is a float field written
unquantised (far more than 32 steps, no ordered dither: the 2026-10-30 dither audit, DitherGuardTest's measure); every
glow is additive (premultiplied on black) and drawn once.

**The motion budget** (the README: the waves and the cruisers' broadsides in the approach; in the arena the bow's parts
and the eruption; after the escape the wreck's halves): the deep layer scrolls at **0.06** (Level 40's: 9.6 px/s at 160
px/s, the bodies drift), at most two animated pieces on the screen at once (checked), nothing animated in the arena.
**The bodies held on the screen**: Callisto's curve rides a ``path`` that holds its limb at y~360-400 through the
approach (it sinks 40 px over 92 s: the wing climbing away from the moon); the Ascendant's glint and drive glow likewise
hold near the top edge. Every path ends before the arena (a path running before the halt runs on the real clock: the
fight never moves them), and the lifeboat's flash and the second fleet's path start after it (at t=93.05: the clock reads
93 at ``ESCAPED``, so they keep the level clock). Pending the user's review in round 80: the curve held instead of
scrolling with the layer (a 0.06 layer would carry it off the bottom by t~30), Jupiter's limb as a near-straight edge
(a disc that fills the right edge for the whole level must be huge), the drive glow leaving upward 89.5-91.5 (the
Ascendant coming about) before the hull's slide-in, no red in the backdrop (the spinal lance's line and the batteries'
rims are red).

The layers (``scroll_factors``): ``deep`` 0.06 (one continuous field: space, stars, Jupiter's limb, cut into the
sections' tile sets at the rows the game shows them (``LevelData.seam``), so the section changes have no seam on the
screen; each tile's rows past its section crossfade back to its own first rows, so it wraps) and the deep pieces over it
(their ``-glow`` lights additive at the piece's places; draw order = the placements' order: the hulks first, so they
fall behind Callisto's limb). No ground (``ground: none``); the cruisers are Level 28's final art (``CruiserLooks``), the
CDF frigates and the dead tender are tools/art/l42_props.py's sprites on the play plane.

Tile sets (480 wide, ``deep``, opaque; one per section, cut from the one field):
  orbit-start    section 1 (0-22): stars, Jupiter's limb at the right edge
  fleet-lane     section 2 (22-54)
  the-screen     section 3 (54-84)
  berth-line     section 4 (84-86, at 160)
  berth-brake    section 4's halt (86-92, eased to 1 px/s over 6 s: Level 35's)
  arena          section 5 (the arena, 92-93): held behind the fight
  empty-orbit    section 6 (93-113, eased up to 60 px/s)
Pieces (``deep``; ``-glow`` = additive at its piece's placements; frames loop at their fps on the real clock):
  callisto-curve        480x320: Callisto's night side below its limb (craters, Jupiter-shine, the cool rim) with
                        the citadel's gold window strips and lamps baked in (steady: not animated)
  hulk-a, hulk-b        96x96 / 80x80: a home-fleet Regent cruiser's bow half and stern half, broken along a jagged
                        fracture, gloss black and gold under soot (Level 28's model, tools/concept/bosses_r48.py
                        ``cruiser_b``, its scene turned and cut, never the image rotated)
  hulk-a-glow, hulk-b-glow  the same size, 8 frames at 8 fps: fire venting at the breaches (orange-white, flickering)
                        and a trail of glowing sparks up behind the fall
  home-fleet            192x64: the home fleet far ahead: seven gloss-black specks with a gold rim
  home-fleet-glow       192x64, one image: their gold lamps and amber drives (steady)
  fleet-far             128x96: the CDF carrier group far off, nose up (tools/art/backdrop_l40.py's, re-rendered)
  fleet-far-glow        128x96, 8 frames at 4 fps: white strobes and blue engines
  ascendant-far         64x96: the Ascendant far ahead, a gilded speck stern-on
  ascendant-far-glow    64x96, 12 frames at 6 fps: its drive point and the sun glinting off its gold once in 2 s
  ascendant-drive       192x192: the closer stern (a dark gold wedge)
  ascendant-drive-glow  192x192, one image: the drive's white-gold core, its gold halo and plume (Level 41's
                        drive glow, larger)
  gate-flash            48x64: the lifeboat far off, a teal-black speck
  gate-flash-glow       48x64, 12 frames at 6 fps: its last flash toward the gate (a white-teal star, then a dwindling
                        drive point)

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout read as seven sections (section 4 split at t=86: "the
scroll at 160 until t=86, eased to 0 by t=92" as Level 35's halt, the brake at 1 px/s since the simulation halts with
speed 0 only in the arena). ``--proposal`` writes the ``backdrop:`` block, the sections and the tender's ``tows`` row
(and a commented ``frigates`` row for step H2) to concept/backdrop-proposal.yaml (never into data.yaml). **Re-run after
the data moves a section** (the tiles are cut at the sections' rows and the pieces are placed by time).

Review: design/campaign/act-6-jovian-storm/level-42-ascendant/concept/backdrop-final-r80-a.png (twelve screens with
stand-ins: the units, Level 28's cruiser, l42_props.py's frigates and tender, an outline of the bow in the arena; the
readability crops at 1x, the pieces, the tile sets) and .gif (t 82-92 at half size: the glint, the drive glow growing and
swinging away, the halt; then t 93-98 after the escape: the lifeboat's last flash, the fleet closing).
Run: python3 tools/art/backdrop_l42.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the shared Jovian pieces: the clock, the fields, the checks' measures)
import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
import l42_props  # noqa: E402  (the tender's hull offset)
from jovkit import (JOV_SUN, additive_rgb, blur, checker_share, cut_tile, glow, rgba, smoothstep,  # noqa: E402
                    wrap_score)
from render import raster  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "backdrop_l42.py"
BATCH = "M10 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r80"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-42-ascendant"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-42"
W, SCREEN, SPEED = 480, 540, 160.0
FACTORS = {"deep": 0.06}

SPACE = np.array((3, 4, 9.0))
ICE = [(0.0, (8, 10, 16)), (0.3, (24, 30, 42)), (0.6, (48, 56, 72)), (0.85, (80, 90, 108)), (1.0, (120, 130, 148))]
JUP_NIGHT = np.array((20, 13, 10.0))
CREAM_LIMB = np.array((236, 206, 160.0))
JSHINE = np.array((150, 118, 84.0))                              # Jupiter's warm light on Callisto's night side
RIM = np.array((150, 166, 196.0))
GOLD = np.array((255, 196, 64.0))
AMBER = np.array((255, 170, 70.0))
WHITE = np.array((255, 255, 255.0))
BLUE = np.array((110, 170, 255.0))
FIRE = np.array((255, 150, 60.0))
TEAL = np.array((0, 255, 154.0))

# =========================================================================== the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "Callisto Orbit", "end": 22, "atmosphere": "clear", "tiles": ["orbit-start"]},
    {"name": "The Home Fleet", "end": 54, "atmosphere": "clear", "tiles": ["fleet-lane"]},
    {"name": "The Screen", "end": 84, "atmosphere": "clear", "tiles": ["the-screen"]},
    {"name": "The Berth Line", "end": 86, "atmosphere": "clear", "tiles": ["berth-line"]},
    {"name": "The Berth Line, braking", "end": 92, "speed": 1, "ramp": 6, "atmosphere": "clear",
     "tiles": ["berth-brake"]},
    {"name": "The Ascendant", "end": 93, "speed": 0, "arena": True, "hold_after_boss": 0, "atmosphere": "clear",
     "tiles": ["arena"]},
    {"name": "The Empty Orbit", "end": 113, "speed": 60, "ramp": 2, "atmosphere": "clear", "tiles": ["empty-orbit"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam, section_at_row, t_centre_at = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row, CLOCK.t_centre_at
DEEP = [(s.get("tiles") or [f"deep-{i + 1}"])[0] for i, s in enumerate(SECTIONS)]
ARENA_T = next((float(s["end"]) for s in SECTIONS if s.get("arena")), 93.0)
E_T = ARENA_T + 0.05                                               # a path starting here keeps the level clock


def deep_row(t):
    return scroll(t) * FACTORS["deep"]


# =========================================================================== the deep field (rows = layer rows, y-up)

def deep_span():
    """Layer rows the level shows on the deep layer: from the start's bottom edge to the end's top (+4 s)."""
    return -40, int(np.ceil(deep_row(END + 4) + SCREEN))


def jupiter():
    """Jupiter's disc: centre (x, layer row), radius - far off to the right, its limb nearest the field (x 446) on the
    arena's centre row and at x~474 on the start's: a near-straight edge along the right side the whole level."""
    row = deep_row(ARENA_T) + SCREEN / 2
    lo, _ = deep_span()
    dy = row - (lo + SCREEN / 2)
    R = dy * dy / (2 * 28.0)
    return 446.0 + R, row, R


def stars(n, r0, seed):
    """Sparse stars on black: most faint single points, a few brighter with a soft 1 px glow; Ganymede as a point."""
    rgb = np.zeros((n, W, 3)) + SPACE
    rng = np.random.default_rng(seed)
    count = int(n * W / 1500)
    ys, xs = rng.integers(0, n, count), rng.integers(0, W, count)
    b = rng.uniform(26, 120, count)
    tint = rng.uniform(-0.08, 0.08, count)
    for y, x, v, c in zip(ys, xs, b, tint):
        rgb[y, x] += (v * (1 + c), v, v * (1 - c))
    light = np.zeros((n, W, 3))
    for _ in range(int(count * 0.06)):
        y, x = rng.uniform(0, n), rng.uniform(0, W)
        glow(light, x, y, 0.9, (235, 240, 255), rng.uniform(0.6, 1.4))
    glow(light, 120, 700 - r0, 2.0, (214, 206, 186), 1.2)                    # Ganymede far off
    return rgb + light


def jupiter_limb(x, r, seed):
    """(rgb, coverage, light) of Jupiter's edge: the bands (the L36 grade's cream, ochre and rust) running across the
    limb, lit in a crescent along it (the sun low behind the ship), the night side brown-black behind, a thin bright
    line on the limb and a faint halo outside it."""
    cx, cr, R = jupiter()
    rho = np.hypot(x - cx, r - cr)
    depth = R - rho                                                         # px inside the limb
    disc = smoothstep(-1.0, 1.0, depth)
    h, w = x.shape
    warp = raster.fbm(w, h, 48, seed, octaves=4, period=False)
    bands = 0.5 + 0.5 * np.sin(r / 23.0 + (warp - 0.5) * 3.2) * (0.7 + 0.3 * np.sin(r / 61.0 + 1.3))
    st = jovkit.streaks(h, w, seed + 1)
    col = raster.ramp(JOV_SUN, np.clip(0.2 + 0.55 * bands + 0.2 * (st - 0.5), 0, 1))
    lit = 0.08 + 0.62 * np.exp(-np.clip(depth, 0, None) / 9.0)
    rgb = col * (0.04 + 0.72 * lit[..., None]) + JUP_NIGHT * (1 - lit[..., None]) * 0.5
    rim = np.exp(-(depth / 1.8) ** 2) * 0.32
    halo = np.exp(-np.clip(-depth, 0, None) / 5.0) * smoothstep(0.0, -2.0, depth) * 0.14
    return rgb, disc, (rim + halo)[..., None] * CREAM_LIMB


def deep_field(seed=4231):
    """The whole deep layer as one float RGB field, row 0 = layer row ``r0`` (y-up). Returns (rgb, r0)."""
    lo, hi = deep_span()
    r0, r1 = lo - 600, hi + 600
    n = r1 - r0
    r = (np.arange(n) + r0).astype(np.float64)[:, None] * np.ones((1, W))
    x = np.ones((n, 1)) * np.arange(W, dtype=np.float64)[None, :]
    rgb = stars(n, r0, seed)
    jcol, jdisc, jlight = jupiter_limb(x, r, seed + 10)
    rgb = rgb * (1 - jdisc[..., None]) + jcol * jdisc[..., None] + jlight
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


# =========================================================================== deep pieces

CALLISTO = (480, 320)
LIMB_TOP, LIMB_R = 40.0, 820.0                                          # the limb's top (piece y) and its radius
HULK_A, HULK_B = (96, 96), (80, 80)
HOME = (192, 64)
FLEET = (128, 96)
FAR = (64, 96)
DRIVE = (192, 192)
FLASH = (48, 64)
CITADEL_AT = (122.0, 168.0)                                             # the keep on the curve (piece px, y down)


def limb_y(x):
    return LIMB_TOP + LIMB_R - np.sqrt(np.clip(LIMB_R ** 2 - (x - W / 2) ** 2, 0, None))


def callisto_curve(seed=4241):
    """Callisto's night side below its limb: dark blue-grey ice saturated with craters (bright fresh rims faint in the
    dark), lit only by Jupiter's warm shine from the right and a thin cool rim along the limb; the black citadel's
    lights baked in: gold window strips, a ring of lamps round the keep, the outer line's lamps on an arc, a few white
    lamps along the causeways (steady)."""
    w, h = CALLISTO
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    ly = limb_y(xx)
    depth = yy - ly                                                     # px below the limb
    cov = smoothstep(-0.8, 0.8, depth)
    rng = np.random.default_rng(seed)
    hgt = np.zeros((h, w))
    fresh = np.zeros((h, w))
    craters = [(rng.uniform(-20, w + 20), rng.uniform(20, h + 20), rng.uniform(1.4, 4.0) ** 1.3) for _ in range(700)]
    craters += [(rng.uniform(-20, w + 20), rng.uniform(40, h + 20), rng.uniform(7, 15)) for _ in range(40)]
    for ccx, ccy, rad in craters:
        # foreshortened toward the limb: the crater squashed vertically by the surface's tilt
        sq = float(np.clip((ccy - float(limb_y(np.array(ccx)))) / 120.0, 0.25, 1.0))
        x0, x1 = int(max(0, ccx - 2.2 * rad)), int(min(w, ccx + 2.2 * rad + 1))
        y0, y1 = int(max(0, ccy - 2.2 * rad * sq)), int(min(h, ccy + 2.2 * rad * sq + 1))
        if x0 >= x1 or y0 >= y1:
            continue
        cy_, cx_ = np.mgrid[y0:y1, x0:x1].astype(np.float64)
        d = np.hypot(cx_ - ccx, (cy_ - ccy) / sq) / rad
        hgt[y0:y1, x0:x1] += np.where(d < 1, -(1 - d * d), 0.0) * rad * 0.22 + np.exp(-((d - 1) / 0.25) ** 2) * rad * 0.08
        if rad < 4.5 and rng.uniform() < 0.3:
            fresh[y0:y1, x0:x1] = np.maximum(fresh[y0:y1, x0:x1], np.exp(-(d / 1.3) ** 2) * rng.uniform(0.4, 0.9))
    hgt = blur(hgt, 0.7)
    gy, gx = np.gradient(hgt)
    tex = raster.fbm(w, h, 34, seed + 1, octaves=5, period=False)
    tex2 = raster.fbm(w, h, 90, seed + 2, octaves=3, period=False)
    alb = np.clip(0.35 + 0.3 * (tex - 0.5) + 0.25 * (tex2 - 0.5) + 0.35 * fresh, 0, 1)
    # Jupiter-shine from the right (warm, low), stronger toward the right edge and near the limb
    shine = (0.18 + 0.5 * smoothstep(60, 470, xx)) * (0.55 + 0.45 * np.exp(-np.clip(depth, 0, None) / 120.0))
    relief = np.clip(0.5 + gx * 0.35 - gy * 0.1, 0, 1)
    v = np.clip(shine * (0.35 + 0.65 * alb) * (0.7 + 0.6 * relief), 0, 1)
    rgb = raster.ramp(ICE, v * 0.62) + (shine * alb * 0.18)[..., None] * JSHINE
    rim = np.exp(-(depth / 2.0) ** 2) * (0.4 + 0.25 * smoothstep(0, 480, xx))
    rgb = rgb + rim[..., None] * RIM
    light = np.zeros((h, w, 3))
    kx, ky = CITADEL_AT
    lrng = np.random.default_rng(seed + 5)
    for bx, by, bw in ((-26, -6, 14), (22, -8, 16), (-18, 10, 10), (20, 9, 12), (0, -16, 18)):
        for j in range(int(bw // 2)):                                   # gold window strips on the blocks
            glow(light, kx + bx - bw / 2 + 2 * j, ky + by + lrng.uniform(-0.4, 0.4), 0.6, GOLD, 0.7)
    for a in np.arange(8) * TAU / 8:                                    # the ring of lamps round the keep
        glow(light, kx + 9 * np.cos(a), ky + 5 * np.sin(a), 0.7, GOLD, 1.0)
    glow(light, kx, ky, 5.0, GOLD, 0.08)
    for k in range(7):                                                  # the outer line's lamps on an arc below
        ox = -36 + 12 * k
        glow(light, kx + ox, ky + 30 - 0.006 * ox * ox, 0.7, GOLD, 0.9)
    for k in range(9):                                                  # white lamps along two causeways
        glow(light, kx + 14 + 9 * k, ky - 4 - 2.2 * k, 0.6, (230, 236, 255), 0.6)
        glow(light, kx - 14 - 8 * k, ky + 2 + 1.6 * k, 0.6, (230, 236, 255), 0.5)
    for x, y in ((330, 120), (338, 124), (402, 96), (64, 250)):        # far outposts
        glow(light, x, y, 0.7, GOLD, 0.8)
    rgb = rgb + light * cov[..., None]
    return rgba(rgb, cov)


def _hulk(job):
    """A Regent cruiser hull (Level 28's model and materials, bosses_r48.cruiser_b, as ascendancy_cruiser.py's hull)
    turned by ``turn`` and cut along a jagged fracture: ``keep`` 'bow' keeps y > cut, 'stern' y < cut."""
    import bosses_r48 as b48  # noqa: E402  (concept script, unchanged)
    size, px, turn, keep, cut = job
    scene, mats = b48.cruiser_b()

    def broken(p):
        q = rotate_z(p, -turn)
        d, m = scene(q)
        jag = cut + 0.45 * np.sin(q[:, 0] * 3.1 + 1.0) + 0.25 * np.sin(q[:, 0] * 7.3) + 0.12 * np.sin(q[:, 0] * 15.0)
        side = (jag - q[:, 1]) if keep == "bow" else (q[:, 1] - jag)
        return np.maximum(d, side * 0.6), m
    hi, f = artkit.render_hi(broken, mats, size, size[0] / px)
    return artkit.native(hi, f, 60)


HULKS = {
    # name: (size, px per unit, turn (rad, model to screen, clockwise > 0), kept half, the cut's mean y, breaches (model x, y))
    "hulk-a": (HULK_A, 7.2, 0.55, "bow", -0.6, [(-1.6, -0.2), (0.9, -0.9), (2.2, -0.4), (0.0, 1.8)]),
    "hulk-b": (HULK_B, 6.0, -0.75, "stern", 0.8, [(-1.2, 0.5), (1.4, 0.9), (0.2, -2.6)]),
}


def model_px(size, px, turn, x, y):
    """Model (x, y) of a turned hull -> piece px (y down): the scene turned by ``turn`` (rotate_z(p, -turn) samples)."""
    c, s = np.cos(turn), np.sin(turn)
    sx, sy = c * x + s * y, -s * x + c * y
    return size[0] / 2 + sx * px, size[1] / 2 - sy * px


def scorch(img, seed, k=0.55):
    """Burnt: darker, mottled by soot (the gold dulled with the black)."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    a[..., :3] *= (k + 0.4 * n)[..., None]
    a[~op] = 0
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def hulks():
    with ProcessPoolExecutor() as pool:
        jobs = [(v[0], v[1], v[2], v[3], v[4]) for v in HULKS.values()]
        rend = list(pool.map(_hulk, jobs))
    out = {}
    for (name, (size, px, turn, keep, cut, breaches)), body in zip(HULKS.items(), rend):
        body = artkit.quantize_set([scorch(body, 4251 + len(out))], 40)[0]
        body = soften(body, 3)
        rng = np.random.default_rng(4261 + len(out))
        spots = [model_px(size, px, turn, bx, by) for bx, by in breaches]
        # the fracture's edge: hot points along the cut line
        for k in range(6):
            mx = -2.4 + 4.8 * k / 5
            my = cut + 0.45 * np.sin(mx * 3.1 + 1.0) + 0.25 * np.sin(mx * 7.3) + 0.12 * np.sin(mx * 15.0)
            spots.append(model_px(size, px, turn, mx, my))
        phase = rng.uniform(0, TAU, len(spots))
        size_k = rng.uniform(0.7, 1.3, len(spots))
        sparks = [(rng.uniform(-10, 10), rng.uniform(4, size[1] * 0.42), rng.uniform(0.3, 1.0)) for _ in range(26)]
        frames = []
        w, h = size
        for f in range(8):
            light = np.zeros((h, w, 3))
            for (x, y), ph, sk in zip(spots, phase, size_k):
                fl = 0.75 + 0.25 * np.sin(ph + f * TAU / 8 * 2) + 0.12 * np.sin(ph * 3 + f * TAU / 8 * 3)
                glow(light, x, y, 1.0 * sk, (255, 236, 200), 1.3 * fl)
                glow(light, x, y, 3.2 * sk, FIRE, 0.5 * fl)
            cx, cy = w / 2, h / 2                                        # sparks trailing up behind the fall
            for k, (dx, up, v) in enumerate(sparks):
                yy = cy - ((up + f * 3.5) % (h * 0.45))
                fade = 1 - ((up + f * 3.5) % (h * 0.45)) / (h * 0.45)
                glow(light, cx + dx + 0.3 * np.sin(k + f), yy, 0.7, (255, 190, 110), 0.9 * v * fade)
            fade = jovkit.soft_box(w, h, 6)[..., None]
            frames.append(additive_rgb(light * fade))
        out[name] = (body, frames)
    return out


def soften(img, edge=6):
    """A deep piece's alpha eased to 0 toward its frame (BackdropAssetsTest: no opaque edge on the screen)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    a[..., 3] *= jovkit.soft_box(img.width, img.height, edge)
    a[a[..., 3] < 0.5] = 0
    return Image.fromarray(np.clip(np.round(a), 0, 255).astype(np.uint8), "RGBA")


def wedge(xx, yy, x, y, half_w, length, down=True):
    """A small wedge's coverage: its broad end at (x, y), narrowing over ``length`` px (down the screen if ``down``)."""
    u = (yy - y) / length if down else (y - yy) / length
    width = half_w * (1 - np.clip(u, 0, 1))
    return smoothstep(0.6, -0.6, np.abs(xx - x) - width) * smoothstep(-0.6, 0.6, u) * smoothstep(1.0, 0.92, u)


HOME_SHIPS = [(28, 30, 3.0), (60, 22, 2.2), (84, 36, 3.4), (110, 26, 2.4), (134, 40, 2.8), (160, 28, 2.0),
              (100, 48, 1.8)]                                           # (x, y, half beam) px


def home_fleet():
    """The home fleet far ahead: gloss-black specks, stern toward the wing (their bows up: a wedge narrowing up), a
    gold fleck on each; their gold lamps and amber drives (steady, additive)."""
    w, h = HOME
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    cov = np.zeros((h, w))
    rgb = np.zeros((h, w, 3))
    light = np.zeros((h, w, 3))
    for x, y, hw in HOME_SHIPS:
        c = wedge(xx, yy, x, y + hw * 1.6, hw, hw * 3.6, down=False)
        cov = np.maximum(cov, c)
        rgb += c[..., None] * np.array((12, 11, 14.0))
        rgb += (c * smoothstep(0.5, -0.5, xx - x + hw * 0.3) * 0.6)[..., None] * np.array((60, 46, 20.0))
        glow(light, x, y + hw * 1.8, 0.8, AMBER, 1.3)                   # the drive, toward the wing
        glow(light, x, y + hw * 1.8, 2.6, AMBER, 0.15)
        glow(light, x - hw * 0.8, y + hw * 0.6, 0.6, GOLD, 0.8)
        glow(light, x + hw * 0.8, y + hw * 0.6, 0.6, GOLD, 0.8)
    fade = jovkit.soft_box(w, h, 6)
    return rgba(rgb / np.maximum(cov, 1e-6)[..., None], cov * fade), additive_rgb(light * fade[..., None])


def ascendant_far():
    """The Ascendant far ahead, stern-on: a gilded wedge speck (its bow away, up the screen), its drive point below it,
    the sun glinting off its gold once in 2 s (a soft four-spiked star); 12 frames at 6 fps."""
    w, h = FAR
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    cx, cy = w / 2, 40.0
    c = wedge(xx, yy, cx, cy + 4, 3.6, 10, down=False)
    rgb = c[..., None] * np.array((22, 18, 12.0)) + (c * smoothstep(0.6, -0.6, xx - cx))[..., None] * np.array((90, 66, 24.0))
    body = rgba(rgb / np.maximum(c, 1e-6)[..., None], c)
    frames = []
    for f in range(12):
        light = np.zeros((h, w, 3))
        fl = 1.0 + 0.08 * np.sin(f * TAU / 12 * 3)
        glow(light, cx, cy + 5.5, 1.1, (255, 246, 220), 1.6 * fl)       # the drive point and its plume toward the wing
        glow(light, cx, cy + 6.5, 4.0, (255, 190, 90), 0.32 * fl)
        u = np.clip((yy - cy - 6) / 40.0, 0, 1)
        plume = np.exp(-((xx - cx) / (1.2 + 3.5 * u)) ** 2) * (1 - u) ** 2 * (yy > cy + 6)
        light += plume[..., None] * np.array((230, 160, 70.0)) * 0.28
        v = {0: 1.0, 1: 0.45, 11: 0.3}.get(f, 0.0)
        if v:
            gx, gy = cx - 1.5, cy - 2
            glow(light, gx, gy, 1.2, (255, 236, 190), 2.4 * v)
            glow(light, gx, gy, 4.0, GOLD, 0.45 * v)
            for sx, sy, ln in ((1, 0, 9), (0, 1, 7)):
                d_al = (xx - gx) * sx + (yy - gy) * sy
                d_ac = (xx - gx) * sy - (yy - gy) * sx
                spike = np.exp(-(d_ac / 0.6) ** 2) * np.exp(-np.abs(d_al) / (ln * 0.35))
                light += (spike * 1.4 * v)[..., None] * np.array((255, 220, 150.0))
        frames.append(additive_rgb(light * jovkit.soft_box(w, h, 4)[..., None]))
    return body, frames


def ascendant_drive():
    """The Ascendant nearer, stern-on: a dark gold wedge (16 px), its drive's white-gold core, a soft gold halo and the
    plume toward the wing (Level 41's drive glow, larger; additive, one image)."""
    w, h = DRIVE
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    cx, cy = w / 2, 70.0
    c = wedge(xx, yy, cx, cy + 4, 9, 30, down=False)
    rgb = c[..., None] * np.array((18, 15, 12.0)) + (c * smoothstep(1.0, -1.0, xx - cx))[..., None] * np.array((110, 80, 28.0))
    body = rgba(rgb / np.maximum(c, 1e-6)[..., None], c)
    light = np.zeros((h, w, 3))
    glow(light, cx, cy + 6, 3.0, (255, 250, 230), 1.7)
    glow(light, cx, cy + 6, 9.0, (255, 196, 90), 0.8)
    glow(light, cx, cy + 6, 22.0, (200, 130, 40), 0.32)
    glow(light, cx, cy + 6, 46.0, (140, 90, 30), 0.12)
    u = np.clip((yy - cy - 6) / (h - cy - 20), 0, 1)
    plume = np.exp(-((xx - cx) / (3.0 + 12 * u)) ** 2) * (1 - u) ** 1.6 * (yy > cy + 6)
    light += plume[..., None] * np.array((230, 160, 70.0)) * 0.55
    fade = smoothstep(0, 16, yy) * smoothstep(h, h - 24, yy) * smoothstep(0, 16, xx) * smoothstep(w, w - 16, xx)
    return body, additive_rgb(light * fade[..., None])


def gate_flash():
    """The lifeboat far off, a teal-black seed-pod speck with its gold fleck, and its last flash toward the gate: a
    white-teal four-spiked star (frames 0-1), then a dwindling drive point flickering (12 frames at 6 fps)."""
    w, h = FLASH
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    cx, cy = w / 2, 30.0
    c = smoothstep(2.6, 1.8, np.hypot(xx - cx, (yy - cy) * 0.8))
    rgb = c[..., None] * np.array((10, 30, 26.0))
    rgb[int(cy) - 1, int(cx)] = (150, 116, 50)
    body = rgba(rgb / np.maximum(c, 1e-6)[..., None], c)
    frames = []
    for f in range(12):
        light = np.zeros((h, w, 3))
        v = {0: 1.0, 1: 0.5}.get(f, 0.0)
        k = 1.0 - 0.5 * f / 11
        glow(light, cx, cy + 3, 1.3, (230, 255, 245), 1.6 * k)
        glow(light, cx, cy + 4, 5.0, TEAL * 0.8, 0.35 * k)
        if v:
            glow(light, cx, cy, 1.6, (255, 255, 250), 2.6 * v)
            glow(light, cx, cy, 6.0, (120, 255, 220), 0.5 * v)
            for sx, sy, ln in ((1, 0, 18), (0, 1, 16)):
                d_al = (xx - cx) * sx + (yy - cy) * sy
                d_ac = (xx - cx) * sy - (yy - cy) * sx
                spike = np.exp(-(d_ac / 0.7) ** 2) * np.exp(-np.abs(d_al) / (ln * 0.35))
                light += (spike * 1.6 * v)[..., None] * np.array((210, 255, 240.0))
        frames.append(additive_rgb(light * jovkit.soft_box(w, h, 4)[..., None]))
    return body, frames


def fleet_far():
    """Level 40's CDF carrier group (tools/art/backdrop_l40.py ``fleet_far``, unchanged): dark hulls nose up, white
    strobes and blue engines."""
    import backdrop_l40 as bd40  # noqa: E402
    return bd40.fleet_far()


# =========================================================================== build

GLOWING = {"hulk-a": (HULK_A, 8, 8), "hulk-b": (HULK_B, 8, 8), "home-fleet": (HOME, 1, 0), "fleet-far": (FLEET, 8, 4),
           "ascendant-far": (FAR, 12, 6), "ascendant-drive": (DRIVE, 1, 0), "gate-flash": (FLASH, 12, 6)}


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
        print(f"{name}: {img.width}x{img.height}")
    save("callisto-curve", callisto_curve())
    made = dict(hulks())
    made["home-fleet"] = home_fleet()
    made["fleet-far"] = fleet_far()
    made["ascendant-far"] = ascendant_far()
    made["ascendant-drive"] = ascendant_drive()
    made["gate-flash"] = gate_flash()
    for name, (body, glows) in made.items():
        save(name, body)
        if isinstance(glows, list):
            for i, f in enumerate(glows):
                save(f"{name}-glow_{i}", f)
        else:
            save(f"{name}-glow", glows)
        print(f"{name}: {body.size}, {len(glows) if isinstance(glows, list) else 1} glow image(s)")
    return pieces_spec(), placements()


def tile_heights():
    return {name: Image.open(OUT / f"{name}.png").height for name in DEEP}


def pieces_spec():
    spec = {"callisto-curve": {"layer": "deep", "size": list(CALLISTO)}}
    for name, (size, n, fps) in GLOWING.items():
        spec[name] = {"layer": "deep", "size": list(size)}
        spec[name + "-glow"] = {"layer": "deep", "size": list(size)}
        if n > 1:
            spec[name + "-glow"].update(frames=n, fps=fps)
    return spec


# =========================================================================== the places

def hold_path(target, times, f=FACTORS["deep"]):
    """A placement time and a path putting the piece's centre at screen y ``target(t)`` at each of ``times`` (y down;
    the path's offsets are px up the screen): linear between the waypoints, as the game's path."""
    t_ref = times[0]
    pt = t_centre_at(t_ref, target(t_ref), f)

    def dy(t):
        return 270.0 + (scroll(t) - scroll(pt)) * f - target(t)
    return round(pt, 3), [[round(t, 3), 0, round(dy(t), 1)] for t in times]


def lerp_target(points):
    """A target y(t) through (t, y) points, linear between them, held at the ends."""
    ts, ys = zip(*points)
    return lambda t: float(np.interp(t, ts, ys))


EASE = [86.0 + 0.5 * k for k in range(13)]                               # the halt's ease, sampled every 0.5 s


def placements():
    curve_t = sorted({0.0, 22.0, 54.0, 84.0, *EASE})
    ct, cpath = hold_path(lerp_target([(0, CALLISTO[1] / 2 + 360 - LIMB_TOP), (92, CALLISTO[1] / 2 + 400 - LIMB_TOP)]),
                          curve_t)
    ha_t, ha_path = hold_path(lerp_target([(53.0, -50.0), (65.0, 430.0), (67.0, 720.0)]), [53.0, 54.0, 65.0, 67.0])
    hb_t, hb_path = hold_path(lerp_target([(66.0, -42.0), (77.0, 410.0), (79.0, 700.0)]), [66.0, 77.0, 79.0])
    far_t, far_path = hold_path(lerp_target([(63.0, 58.0), (84.0, 58.0), (89.5, 64.0), (91.5, -700.0)]),
                                [63.0, 84.0, *[t for t in EASE if t <= 89.5], 91.5])
    dr_t, dr_path = hold_path(lerp_target([(83.0, -110.0), (88.5, 46.0), (89.5, 50.0), (91.5, -700.0)]),
                              [83.0, 84.0, *[t for t in EASE if t <= 89.5], 91.5])
    gf_t, gf_path = hold_path(lerp_target([(E_T, -500.0), (E_T + 0.05, 36.0), (E_T + 4.0, 30.0), (E_T + 5.5, -600.0)]),
                              [E_T, E_T + 0.05, E_T + 1.95, E_T + 2.0, E_T + 4.0, E_T + 5.5])
    f2_t, f2_path = hold_path(lerp_target([(E_T, 700.0), (E_T + 0.05, 600.0), (END, 410.0)]),
                              [E_T, E_T + 0.05, E_T + 1.95, E_T + 2.0, END])
    f = FACTORS["deep"]
    return [
        {"piece": "hulk-a", "t": ha_t, "x": 300, "path": [[a, round(-10 + 30 * (a - 53) / 12, 1), b] for a, _, b in ha_path],
         "note": "section 3: a burning bow half falling from the top edge toward Callisto (t~53-65), behind its limb "
                 "(drawn before the curve)"},
        {"piece": "hulk-b", "t": hb_t, "x": 150, "path": [[a, round(-20 * (a - 66) / 11, 1), b] for a, _, b in hb_path],
         "note": "section 3: a burning stern half falling on the left (t~66-77), behind the limb"},
        {"piece": "callisto-curve", "t": ct, "x": 240, "path": cpath,
         "note": "the whole level: Callisto's night side below its limb, held by its path at y~360 (t=0) to 400 (t=92),\n"
                 "    # then held through the fight (the path ends before the halt), sinking 72 px over the empty orbit"},
        {"piece": "home-fleet", "t": round(t_centre_at(14.0, 70.0, f), 3), "x": 250,
         "note": "sections 1-2: the home fleet's lights far ahead, in from the top at t~6, drifting down past y~380 by t~45"},
        {"piece": "fleet-far", "t": round(t_centre_at(0.0, 470.0, f), 3), "x": 352,
         "path": [[0, 0, 0], [30, 0, 450], [44, 0, 250]],
         "note": "section 1: the CDF carrier group closing from below over Callisto's night side (y 470 at t=0, y~310 at\n"
                 "    # t=30), then falling back out at the bottom by t~44"},
        {"piece": "ascendant-far", "t": far_t, "x": 240, "path": far_path,
         "note": "sections 3-4: the Ascendant's gold glint high ahead (in from the top at t~53, held at y~58 from t=63),\n"
                 "    # climbing away out of the top over 89.5-91.5 (it comes about), parked far up after"},
        {"piece": "ascendant-drive", "t": dr_t, "x": 240, "path": dr_path,
         "note": "section 4: its drive glow growing over the glint as the wing closes (in from above the top edge,\n"
                 "    # t=83-88.5), swinging away with it over 89.5-91.5, before the bow slides in at t=92"},
        {"piece": "gate-flash", "t": gf_t, "x": 240, "path": gf_path,
         "note": "section 6: the lifeboat's last flash toward the gate at the top edge from ESCAPED (the clock reads 93):\n"
                 "    # its path starts at 93.05 on the level clock (parked far up before), gone over the top by E+5.5"},
        {"piece": "fleet-far", "t": f2_t, "x": 150,
         "path": [[a, 700 if i == 0 else 0, b] for i, (a, _, b) in enumerate(f2_path)],
         "note": "section 6: the CDF fleet's lights closing in from below after the escape (y~600 at E, ~410 at the end);\n"
                 "    # parked 700 px off the right edge until then (its path starts after the fight: the level clock)"},
    ]


# =========================================================================== proposal

HULL_OFFSET = l42_props.hull_offset()                                # the tender's centre from its strongbox, y up

TOWS = """tows:
  # Proposal (ART-H2, tools/art/l42_props.py's sprites, held in build/art-held/sprites/ until the data claims them):
  # the dead home-fleet tender (the secret; README: t~42, x~330, drifting down the right half; housing 4 hits,
  # `beam_touch`, crate 100). Level 30's buoy rule: the tow's place is the strongbox (its housing = the cable box,
  # tether [0, 0]); the look (step H4a: TowLooks.LOOKS 42 = `debris-home-tender`) draws the dead tender
  # `debris-home-tender-hull` centred (""" + f"{HULL_OFFSET[0]:+d}, {HULL_OFFSET[1]:+d}" + """) px from it (y up, pivots/debris-home-tender.json: the strongbox on its spine's cradle; the hull
  # never turns), the strongbox `debris-home-tender` over it, its seal lamp `debris-home-tender-glow` (additive, 8 frames
  # at 4 fps: the gold lamp breathing), then `debris-home-tender-break` (0.07 s a frame) at the cut; the hull drifts on
  # at the tow's place after the break.
  - {t: 42, x: 330, drift: [-3, -80], boat: [24, 24], pod: [18, 18], tether: [0, 0], cable: [22, 22], hits: 4, reveals: the home-fleet tender's strongbox, beam_touch: true}
# frigates:   # Proposal for step H2/H4a (not a data key yet): the CDF frigate pair trading fire with cruiser A (left)
#   # from the right flank, t~26-36, friendly scenery on `low-air` with the friendly marker (FriendlyMarks), drawn in
#   # l42_props.py's `cdf-frigate` (+ `-glow` frames, `-muzzle`, `-tracer`; pivots/cdf-frigate.json: the turrets'
#   # muzzles, the engines): each in from the bottom edge over 3 s, on station ~6 s, falling back; its two turrets fire a
#   # 3-round tracer burst toward cruiser A every ~1.6 s (presentation only: they hit nothing)
#   - {t: 26, end: 36, x: 446, y: 300, sprite: cdf-frigate, marked: true}
#   - {t: 26.6, end: 36, x: 418, y: 170, sprite: cdf-frigate, marked: true}
"""


def fmt(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, dict):
        return "{" + ", ".join(f"{k}: {fmt(x)}" for k, x in v.items()) + "}"
    if isinstance(v, str) and ("'" in v or ":" in v or "," in v):
        return '"' + v + '"'
    return str(v)


def proposal_text(pieces, placed):
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    h = tile_heights()
    lines = ["backdrop:",
             f"  # Presentation only: Level 42's Callisto orbit (tools/art/backdrop_l42.py, step ART-H2, sections from {src}).",
             "  # No ground (`ground: none`): the deep layer is the base, scrolling at 0.06 (the bodies drift). Its tile sets",
             "  # are cut from one continuous field at the rows each section's tiles begin (LevelData.seam): re-run the",
             "  # generator after moving a section. The pieces draw in the placements' order (the hulks before the curve, so",
             "  # they fall behind Callisto's limb); the -glow pieces are additive at their piece's placements, at most two",
             "  # animated on the screen at once (checked). Every path before the arena ends by t=92 (the fight moves",
             "  # nothing); the paths after it start at 93.05 (the clock reads 93 at ESCAPED: they keep the level clock).",
             "  scroll_factors: {deep: 0.06, far: 0.3, ground: 1, low-air: 1.3, high-air: 1.7}   # far, ground, high-air unused",
             "  ramp: 2",
             "  haze_colour: 03040a",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        lines.append(f"    {name}: {{layer: deep, height: {h[name]}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    lines.append("  pieces:")
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p.get("note"):
            lines.append(f"    # {p['note']}")
        extra = f", path: {fmt(p['path'])}" if p.get("path") else ""
        lines.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {p['x']}{extra}}}")
    lines.append("sections:   # ART-H2's reading of the README's Layout (section 4 split at t=86 so the 6 s ease to the halt")
    lines.append("            # starts there: Level 35's halt at 1 px/s, then the 1 s arena at 0); H2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - " + "{" + ", ".join(f"{k}: {fmt(v)}" for k, v in s.items() if k != "notes") + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); deep {int(round(deep_row(END)))}")
    return "\n".join(lines) + "\n" + TOWS


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["tows"]


# =========================================================================== composite

_CACHE = {}


def images(name, spec):
    if name not in _CACHE:
        n = spec.get("frames")
        _CACHE[name] = ([Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)] if n
                        else [Image.open(OUT / f"{name}.png").convert("RGBA")])
    return _CACHE[name]


def tile_rows(name, offset):
    key = ("tile", name)
    if key not in _CACHE:
        _CACHE[key] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[key]
    height = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[height - 1 - (rows % height)], rows


def layer_tiles(t):
    """The screen's rows of the deep layer at t, each from its section's tile set (the game's seams)."""
    off = deep_row(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i in range(len(SECTIONS)):
        arr, rows = tile_rows(DEEP[i], off)
        sel = np.array([section_at_row("deep", r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def path_offset(p, t):
    """BackdropData.PlacedPiece.offsetX/offsetY: linear between the waypoints, held before and after."""
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


def piece_xy(p, spec, t):
    f = FACTORS[spec["layer"]]
    dx, dy = path_offset(p, t)
    centre = scroll(p["t"]) * f + SCREEN / 2 + dy
    return p["x"] + dx, SCREEN - (centre - scroll(t) * f)


def on_screen(p, spec, t):
    x, y = piece_xy(p, spec, t)
    w, h = spec["size"]
    return -h / 2 < y < SCREEN + h / 2 and -w / 2 < x < W + w / 2


def blit(img, p, pieces, t, glow_layer=False, clock=None):
    name = p["piece"] + ("-glow" if glow_layer else "")
    spec = pieces.get(name)
    if not spec:
        return img
    ims = images(name, spec)
    real = t if clock is None else clock
    frame = ims[int(np.floor(real * spec.get("fps", 0))) % len(ims)] if spec.get("frames") else ims[0]
    x, y = piece_xy(p, pieces[p["piece"]], t)
    if y - frame.height / 2 > SCREEN or y + frame.height / 2 < 0:
        return img
    return l23kit.add_c(img, frame, x, y) if glow_layer else l23kit.paste_c(img, frame, x, y)


def load_held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def compose(t, units=True, real=None, arena=None):
    """The screen at t (480x540): the deep tiles, the deep pieces in order (glows added); ``arena`` = seconds into the
    fight (the backdrop held) for the bow's outline; the play plane's stand-ins."""
    pieces, placed, tows = load_proposal()
    img = layer_tiles(t)
    for p in placed:
        img = blit(img, p, pieces, t)
        img = blit(img, p, pieces, t, glow_layer=True, clock=real)
    if units:
        img = stand_ins(img, t, tows)
        if arena is not None:
            img = bow_outline(img, arena)
        img = ship_and_shots(img, t)
    return img


# the stand-ins: (t0, kind, n, edge) - a few of the README's waves
STANDS = [(3.0, "talon", 5, "top"), (8.0, "chimera", 5, "top"), (14.0, "skitter", 8, "top"), (27.0, "talon", 5, "top"),
          (44.0, "talon", 5, "bottom"), (55.0, "gilded-gunship", 1, "top"), (61.0, "mirror-interceptor", 2, "sides"),
          (67.0, "harrow", 8, "cross"), (75.0, "hornet", 2, "top")]


def stand_ins(img, t, tows):
    body = {k: load_held(k) for k in ("talon", "chimera", "mirror-interceptor", "harrow", "gilded-gunship", "hornet",
                                      "skitter")}
    sac = load_held("harrow-sac")
    cr, crg = load_held("ascendancy-cruiser"), load_held("ascendancy-cruiser-glow")
    for side, t0, t1 in ((-1, 24.0, 38.0), (1, 40.0, 54.0)):            # the cruisers, one flank at a time
        if cr and t0 <= t <= t1:
            u = min(t - t0, 4.0) / 4.0 if t - t0 < 4 else (1.0 if t < t1 - 3 else (t1 - t) / 3)
            y = 300 + (1 - u) * 600
            x = -6 if side < 0 else 486
            img = l23kit.paste_c(img, cr[0], x, y)
            if crg:
                img = l23kit.add_c(img, crg[0], x, y)
    fr, frg = load_held("cdf-frigate"), load_held("cdf-frigate-glow")
    for k, (x, y, t0) in enumerate(((446, 300, 26.0), (418, 170, 26.6))):   # the CDF frigate pair, right flank
        if fr and t0 <= t <= 36.0:
            u = min(t - t0, 3.0) / 3.0
            yy = y + (1 - u) * 400 + max(0.0, t - 33.0) * 130
            img = l23kit.paste_c(img, fr[0], x, yy)
            if frg:
                img = l23kit.add_c(img, frg[int(t * 4) % len(frg)], x, yy)
            img = marker(img, x, yy, fr[0].width, fr[0].height)
    for t0, kind, n, edge in STANDS:
        u = t - t0
        fr = body.get(kind)
        if not 0 <= u <= 6 or not fr:
            continue
        for k in range(n):
            if kind == "harrow":
                uu = u - 0.4 * (k // 4)
                if uu < 0:
                    continue
                side = ("top", "left", "right", "bottom")[k % 4]
                sx, sy, dx, dy = {"top": (200 + 30 * (k // 4), -20, 0, 1), "left": (-20, 160 + 40 * (k // 4), 1, 0.3),
                                  "right": (500, 140 + 40 * (k // 4), -1, 0.3),
                                  "bottom": (280, 560, -0.15, -1)}[side]
                x, y = sx + dx * 120 * uu, sy + dy * 120 * uu
                hk = int(round((np.degrees(np.arctan2(-dx, dy)) % 360) / 22.5)) % 16
                img = l23kit.paste_c(img, fr[hk], x, y)
                if sac:
                    img = l23kit.add_c(img, sac[hk], x, y, 0.6)
            elif kind == "mirror-interceptor":
                side = (-1, 1)[k % 2]
                x, y = 240 + side * (300 - 90 * min(u, 2.4)), 150 + 40 * k
                img = l23kit.paste_c(img, fr[4 if side > 0 else 12], x, y)
            elif edge == "bottom":
                x = 240 + (k - (n - 1) / 2) * 56
                y = 580 - 130 * u + abs(k - (n - 1) / 2) * 24
                img = l23kit.paste_c(img, fr[8], x, y)
            else:
                x = 240 + (k - (n - 1) / 2) * (30 if kind == "skitter" else 56) + (60 if kind == "gilded-gunship" else 0)
                y = -30 + 130 * u - abs(k - (n - 1) / 2) * 24
                if kind in ("hornet", "gilded-gunship"):
                    y = min(y, 140)
                img = l23kit.paste_c(img, fr[0], x, y)
    hull, box, boxg = (load_held(n) for n in ("debris-home-tender-hull", "debris-home-tender", "debris-home-tender-glow"))
    for row in tows:
        u = t - row["t"]
        if not 0 <= u < 9:
            continue
        x, y = row["x"] + row["drift"][0] * u, -20 - row["drift"][1] * u
        if hull:
            img = l23kit.paste_c(img, hull[0], x + HULL_OFFSET[0], y - HULL_OFFSET[1])
        if box:
            img = l23kit.paste_c(img, box[0], x, y)
            if boxg:
                img = l23kit.add_c(img, boxg[int(u * 4) % len(boxg)], x, y)
    return img


def marker(img, x, y, w, h):
    """The friendly marker's stand-in (FriendlyMarks: HUD-green corner brackets round the craft, drawn in code)."""
    from PIL import ImageDraw
    d = ImageDraw.Draw(img)
    hw, hh = min(w, 40) / 2 + 6, min(h, 60) / 2 + 6
    g = (90, 255, 140, 255)
    for sx in (-1, 1):
        for sy in (-1, 1):
            cx, cy = x + sx * hw, y + sy * hh
            d.line([(cx, cy), (cx - sx * 8, cy)], fill=g, width=2)
            d.line([(cx, cy), (cx, cy - sy * 8)], fill=g, width=2)
    return img


def bow_outline(img, u):
    """ART-H1's hull is not drawn here: the bow's wedge as an outline (top edge x 40-440, the tip at y 250) and its
    parts' spots, for the composition only."""
    from PIL import ImageDraw
    over = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(over)
    slide = min(u / 6.0, 1.0)
    dy = -250 * (1 - slide)
    d.polygon([(40, 0 + dy), (440, 0 + dy), (240, 250 + dy)], fill=(20, 16, 10, 150), outline=(236, 186, 72, 255))
    for x, y in ((240, 235), (110, 60), (370, 60), (150, 130), (185, 190), (330, 130), (295, 190)):
        d.ellipse([x - 5, y - 5 + dy, x + 5, y + 5 + dy], outline=(236, 186, 72, 255))
    img.alpha_composite(over)
    return img


def ship_and_shots(img, t):
    ship = artkit.load_frames("ship")
    orb, needle = load_held("orb"), load_held("needle")
    if 10 < t and orb and needle:
        for k in range(4):
            img = l23kit.paste_c(img, needle[0], 196 + 22 * k, 250 + 34 * k)
        img = l23kit.paste_c(img, orb[0], 330, 300)
    if ship:
        img = l23kit.paste_c(img, ship[2], 240, 470)
    return img


# =========================================================================== review

SHOTS = [(0.5, None, "T=0.5 CALLISTO'S CURVE, JUPITER, THE FLEET"),
         (9.0, None, "T=9 CHIMERAS, THE HOME FLEET AHEAD"),
         (30.0, None, "T=30 CRUISER A, THE CDF FRIGATES"),
         (44.0, None, "T=44 THE DEAD TENDER, A REAR WING"),
         (48.0, None, "T=48 CRUISER B ON THE RIGHT"),
         (58.0, None, "T=58 A HULK FALLING, A GUNSHIP"),
         (69.0, None, "T=69 THE HARROW CROSS, THE GLINT"),
         (87.0, None, "T=87 THE PAUSE: THE DRIVE GLOW"),
         (90.6, None, "T=90.6 IT COMES ABOUT"),
         (ARENA_T, 8.0, "THE ARENA (BOW OUTLINE, HELD)"),
         (E_T + 0.3, None, "E+0.3 THE LIFEBOAT'S LAST FLASH"),
         (110.0, None, "T=110 THE EMPTY ORBIT")]


def windows(t, w=200, h=96):
    """The screen's darkest and brightest 200x96 windows at t, from the backdrop alone."""
    img = compose(t, units=False)
    lum = np.array(img.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    best = []
    for y in range(0, SCREEN - h, 12):
        for x in range(0, W - w, 12):
            best.append((lum[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return best[0], best[-1], lum.mean(), img


def read_cell(bg, x, y):
    """The readability set at 1x over a 200x96 window: a Talon, a Chimera, a Harrow, needles, an orb, the ship."""
    c = bg.crop((x, y, x + 200, y + 96)).copy()
    tal, chi, har = load_held("talon"), load_held("chimera"), load_held("harrow")
    orb, needle = load_held("orb"), load_held("needle")
    if tal:
        c = l23kit.paste_c(c, tal[0], 26, 28)
    if chi:
        c = l23kit.paste_c(c, chi[0], 80, 36)
    if har:
        c = l23kit.paste_c(c, har[2], 126, 26)
    for k in range(3):
        c = l23kit.paste_c(c, needle[0], 150 + 10 * k, 20 + 12 * k)
    c = l23kit.paste_c(c, orb[0], 184, 30)
    ship = artkit.load_frames("ship")
    if ship:
        c = l23kit.paste_c(c, ship[2], 150, 72)
    return c


def gif_frames():
    frames = []
    for i in range(100):
        t = 82.0 + i / 10
        frames.append(compose(t).resize((W // 2, SCREEN // 2), Image.BOX))
    for i in range(20):
        frames.append(compose(ARENA_T, arena=i / 10 * 3, real=ARENA_T + i / 10).resize((W // 2, SCREEN // 2), Image.BOX))
    for i in range(50):
        t = E_T + i / 10
        frames.append(compose(t).resize((W // 2, SCREEN // 2), Image.BOX))
    return frames


def review():
    pieces, placed, _ = load_proposal()
    shots = [(compose(t, arena=a, real=None if a is None else t + a), lab) for t, a, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    reads_t = [(8.0, "S1"), (45.0, "S2"), (70.0, "S3"), (88.0, "S4"), (ARENA_T, "ARENA"), (105.0, "S6")]
    read_h = 16 + 2 * (96 + 20)
    pieces_h = 430
    tile_h = 200
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + read_h + 30 + pieces_h + tile_h + 30,
                         "LEVEL 42 BACKDROP (FINAL R80): CALLISTO ORBIT", f"PRODUCTION ART, {BATCH.upper()} - R80")
    raster.draw_text(sheet, 16, 38, "TWELVE SCREENS AT HALF SIZE WITH STAND-INS (HELD FRAMES: THE UNITS, LEVEL 28'S "
                                    "CRUISER, THE CDF FRIGATES AND THE TENDER; THE BOW AS AN OUTLINE)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X: THE DARKEST (TOP) AND BRIGHTEST (BOTTOM) 200X96 WINDOW WITH A "
                                   "TALON, A CHIMERA, A HARROW, NEEDLES, AN ORB, THE SHIP", raster.LABEL)
    means = []
    for j, (t, lab) in enumerate(reads_t):
        (dl, dx, dy), (bl, bx, by), mean, bg = windows(t)
        means.append((lab, t, mean, dl, bl))
        for i, (lum, wx, wy) in enumerate(((dl, dx, dy), (bl, bx, by))):
            c = read_cell(bg, wx, wy).resize((160, 77), Image.BOX)
            x0, y0 = 16 + j * 166, y + 16 + i * 116
            sheet.alpha_composite(c, (x0, y0))
            raster.draw_text(sheet, x0, y0 + 80, f"{lab} {'DARK' if i == 0 else 'BRIGHT'} {lum:.0f}", raster.LABEL_DIM)
    y += read_h + 14
    raster.draw_text(sheet, 16, y, "MEANS: " + "  ".join(f"{lab} {m:.0f} ({d:.0f}-{b:.0f})" for lab, _, m, d, b in means),
                     raster.LABEL_DIM)
    y += 16
    raster.draw_text(sheet, 16, y, "THE PIECES (1X, GLOW FRAME 0 ADDED) ON THE LAYER UNDER THEM: HULKS, HOME FLEET, "
                                   "FLEET, GLINT, DRIVE, FLASH; CALLISTO'S CURVE (HALF SIZE)", raster.LABEL)
    cc = images("callisto-curve", pieces["callisto-curve"])[0]
    c = layer_tiles(0.0).crop((0, 0, cc.width, cc.height))
    c.alpha_composite(cc)
    c = c.resize((cc.width // 2, cc.height // 2), Image.BOX)
    sheet.alpha_composite(c, (16, y + 16))
    x, row = 16 + c.width + 8, 0
    for name, t in (("hulk-a", 60.0), ("hulk-b", 70.0), ("home-fleet", 20.0), ("fleet-far", 2.0),
                    ("ascendant-far", 70.0), ("ascendant-drive", 88.0), ("gate-flash", 100.0)):
        body = images(name, pieces[name])[0]
        g = images(name + "-glow", pieces[name + "-glow"])[0]
        c = layer_tiles(t).crop((60, 0, 60 + body.width, body.height))
        c = l23kit.add_c(l23kit.paste_c(c, body, body.width / 2, body.height / 2), g, body.width / 2, body.height / 2)
        if x + c.width > sheet.width - 16:
            x, row = 16 + 248, row + 200
        sheet.alpha_composite(c, (x, y + 16 + row))
        x += c.width + 8
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (EIGHTH SIZE): " + ", ".join(n.upper() for n in DEEP), raster.LABEL)
    x = 16
    for name in DEEP:
        tl = Image.open(OUT / f"{name}.png").convert("RGBA")
        small = tl.resize((tl.width // 8, max(1, tl.height // 8)), Image.BOX)
        sheet.alpha_composite(small, (x, y + 16))
        x += small.width + 10
    artkit.REVIEW_ROUND = ROUND                                         # an imported generator may have set its own
    artkit.save_review(sheet, gif_frames(), CONCEPT, "backdrop", fps=10)
    for lab, t, m, d, b in means:
        print(f"{lab} t={t}: mean {m:.0f}, darkest window {d:.0f}, brightest {b:.0f}")


# =========================================================================== check

def seam_jump(i):
    t = STARTS[i] + 0.4
    arr = np.array(layer_tiles(t)).astype(np.float64)
    lum = arr[..., :3] @ np.array((0.299, 0.587, 0.114))
    y = int(round(SCREEN - (seam("deep", i) - deep_row(t)))) - 1
    if not 2 <= y < SCREEN - 3:
        return 0.0
    d = lambda a, b: np.abs(lum[a] - lum[b]).mean()  # noqa: E731
    ref = np.median([d(k, k + 1) for k in range(max(0, y - 30), min(SCREEN - 1, y + 30))])
    return d(y, y + 1) / max(ref, 0.5)


def glow_levels(img):
    """Distinct non-zero levels of a glow frame's brightest channel (the smooth-steps measure: >= 32)."""
    a = np.array(img.convert("RGBA"))[..., :3].max(axis=-1)
    return len(np.unique(a[a > 0]))


def opaque_edges(p, spec, im, t):
    """BackdropAssetsTest's rule: an edge row/column of the piece with an opaque pixel strictly inside the screen."""
    x, y = piece_xy(p, spec, t)
    w, h = spec["size"]
    left, top = x - w / 2, y - h / 2                                    # y down
    a = im[..., 3] > 0
    c0, c1 = int(max(0, -left)), int(min(w, W - left))
    r0, r1 = int(max(0, -top)), int(min(h, SCREEN - top))
    if c0 >= c1 or r0 >= r1:
        return []
    out = []
    if 0 < top < SCREEN and a[0, c0:c1].any():
        out.append("top")
    if 0 < top + h < SCREEN and a[-1, c0:c1].any():
        out.append("bottom")
    if 0 < left < W and a[r0:r1, 0].any():
        out.append("left")
    if 0 < left + w < W and a[r0:r1, -1].any():
        out.append("right")
    return out


def check():
    """The deep tiles opaque and wrapping (BackdropSeamsTest's measure), the section seams invisible, sizes as the
    proposal, Source chunks, no checker (DitherGuardTest's measure under 5 %), every glow in >= 32 levels, the deep
    pieces' edges never on the screen opaque (paths included), at most two animated pieces on the screen at once,
    nothing animated or moving in the arena, the atlas area."""
    problems = []
    pieces, placed, _ = load_proposal()
    for name in DEEP:
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    for i in range(1, len(SECTIONS)):
        j = seam_jump(i)
        if j > 2.0:
            problems.append(f"deep seam {i}: a visible jump ({j:.2f}x the rows round it)")
    for name, spec in pieces.items():
        n = spec.get("frames")
        files = [OUT / f"{name}_{k}.png" for k in range(n)] if n else [OUT / f"{name}.png"]
        for f in files:
            im = Image.open(f)
            if list(im.size) != list(spec["size"]):
                problems.append(f"{f.name}: {im.size} vs {spec['size']}")
        if name.endswith("-glow"):
            lv = max(glow_levels(Image.open(f)) for f in files)
            if lv < 32:
                problems.append(f"{name}: its brightest frame has {lv} levels (< 32)")
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
    worst = 0
    animated_spec = {k for k, s in pieces.items() if s.get("frames")}
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = {(p["piece"], p["t"]) for p in placed
                    if p["piece"] + "-glow" in animated_spec and on_screen(p, pieces[p["piece"]], t)}
        worst = max(worst, len(animated))
        if len(animated) > 2:
            problems.append(f"t={t}: {sorted(animated)} animated at once")
            break
        if abs(t - ARENA_T) < 1e-6 and animated:
            problems.append(f"the arena: {sorted(animated)} animated behind the fight")
    for p in placed:
        if p.get("path"):
            ts = [w[0] for w in p["path"]]
            if ts[0] < ARENA_T - 1 and ts[-1] > ARENA_T - 1:
                problems.append(f"{p['piece']} at t={p['t']}: its path runs into the halt (ends {ts[-1]})")
    arrays = {name: np.array(images(name, spec)[0]) for name, spec in pieces.items() if not name.endswith("-glow")}
    for p in placed:
        spec = pieces[p["piece"]]
        seen = False
        for step in range(0, int((END + 4) * 10)):
            t = step / 10
            if not on_screen(p, spec, t):
                continue
            seen = seen or t < END
            e = opaque_edges(p, spec, arrays[p["piece"]], t)
            if e:
                problems.append(f"{p['piece']} at t={p['t']}: an opaque {e} edge on the screen at t={t}")
                break
        if not seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")
    area = sum(distinct.values())
    print(f"atlas: {len(distinct)} distinct images, {area / 1e6:.2f} M px with padding = "
          f"{area / 2048 ** 2:.2f} pages of 2048^2 before packing losses; checker {chk / max(vis, 1) * 100:.2f} % "
          f"of {vis} px; at most {worst} animated pieces on the screen")
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
