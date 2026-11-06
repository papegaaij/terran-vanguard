#!/usr/bin/env python3
"""Production art: the Level 43 backdrop, The Tether Gate (design/campaign/act-7-beyond-the-gate/
level-43-the-tether-gate; M11 part B batch, concept round 89, step ART-B): the gate kit, Act 7's first.

Straight to production from round 84's chosen look: Q10 b's **one Vrell palette** with Level 43's light (design/world/
vrell-space, *Light per level*: the gate's dark stone and cold teal-white glyphs under the violet and teal nebula, the
fleet's white running lights behind; the ring a step darker and less saturated than the Gate Wardens, its glyphs never
pulsing) laid out as the level's Layout has it, and the act still A.9 b "the mouth" (``still_mouth``, written by
tools/art/act_stills.py ``act-7``). The ring and the event plane are round 84's painter (tools/concept/act7_r84.py
``ring_paint``, ``space_rgb``, ``plane_rgb``, imported unchanged), so the backdrop, the still and the transit storyboard
agree. Vacuum: no haze. Every nebula, glow and soft edge is a float field written unquantised (far more than 32 steps,
no ordered dither: DitherGuardTest's measure); glows fade to 0 inside their canvas.

The layers (``scroll_factors``): ``deep`` 0.03 (one continuous field: space, stars and the violet and teal nebula, a
step dimmer through section 3, the ring's shadow; cut into the sections' tile sets at the rows the game shows them,
``LevelData.seam``, so a section change has no seam), the deep pieces held on the screen by ``path``s (a 0.03 layer
moves 30 px a minute); ``ground`` 1 (section 4 only: the ring's surface strip); ``low-air`` 1.3 (spore sacs in section
1, dead tendrils in section 2).

Tile sets (480 wide, rows y-up as the game reads them):
  deep-1 .. deep-5     ``deep``, opaque: the nebula field cut at the five sections' seams
  ring-surface         ``ground``, 480x1024, section 4: the ring's flat face as a band 376 px wide (x 52-428): dark
                       basalt in plates, two glyph channels along the run (x 148, 332) with cold teal-white glyph marks
                       cut in them, a cross channel every 256 rows; outside the band transparent (the deep shows the
                       nebula and the rim beside it); wraps in y
Pieces (``-glow`` = additive at its piece's places, steady unless framed):
  ring-far             200x200 deep, section 1: the ring small ahead (outer 150 px), the event plane dim in its mouth
  ring-third           288x288 deep, section 2: the ring a third of the screen (outer 244 px), its glyphs readable
  ring-top             480x300 deep, section 3: the ring's near arc filling the top of the screen, the plane beyond it
  ring-rim             480x240 deep, section 4: the mouth's inner rim rising at the top like a horizon, the plane above
  mouth                480x560 deep, section 5: the event plane filling the ring's centre ahead, pale violet, its rim
                       round it (the transit flies into it; the warp is the game's post-effect)
  steadfast-group      128x96 deep (+ ``-glow_0..7``, 4 fps: white strobes and blue engines): the *Steadfast*'s group
                       behind (Level 40's CDF carrier group, tools/art/backdrop_l40.py ``fleet_far``, unchanged), at
                       the bottom of the screen at the start, sliding off by t=20
  spore-sac-a..c       40x52 low-air: drifting Vrell spore sacs, a dark violet membrane round a dim mint core (never a
                       bullet-sized dot), soft edged
  tendril-a, tendril-b 56x176 low-air: dead tendrils, withered grey-violet strands fading at both ends
  ring-surface-lead    480x128 ground: the strip's leading edge (its centre on section 4's ground seam): the band's
                       end bevel below, fading into the tile above (hides the tile's straight start)
  ring-surface-lip     480x192 ground: the strip's inner lip at section 5's ground seam: the tile fading in below, the
                       bevelled lip with a glyph row above, then nothing (the mouth beyond)

The ring's three scales (m11b-gaps §4: three placed pieces cross-faded by section, not a continuous scale): each piece
is parked off the screen outside its section by its path and swapped at the section change; **the 2 s cross-fade at
t = 48, 124, 200, 256 is step B4's** (BackdropPieces), the proposal's comments name it.

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout. ``--proposal`` writes the ``backdrop:`` block and the
sections to concept/backdrop-proposal.yaml (never into data.yaml; step B2 places them). **Re-run after the data moves a
section** (the tiles are cut at the sections' rows and the pieces are placed by time).

Review: concept/backdrop-final-r89-a.png (eight screens with stand-ins: the Wardens, the glyph stones and the ship; the
pieces; the tiles) and .gif (the level in a time-lapse, 2 s a frame).
Run: python3 tools/art/backdrop_l43.py [--proposal | --review | --check]   (all: about 1 min)
"""
import sys
from pathlib import Path

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the clock, cut_tile, the checks' measures)
import l23kit  # noqa: E402  (held sprites)
from jovkit import additive_rgb, checker_share, cut_tile, glow, rgba, smoothstep, wrap_score  # noqa: E402

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: the ring painter, the nebula and the plane, imported unchanged)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l43.py"
BATCH = "M11 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r89"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-43-the-tether-gate"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-43"
W, SCREEN, SPEED = 480, 540, 120.0
FACTORS = {"deep": 0.03, "ground": 1.0, "low-air": 1.3}

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "The Mouth", "end": 48, "speed": 120, "atmosphere": "clear", "tiles": ["deep-1"]},
    {"name": "The Pickets", "end": 124, "speed": 110, "ramp": 2, "atmosphere": "clear", "tiles": ["deep-2"]},
    {"name": "The Ring's Shadow", "end": 200, "speed": 100, "ramp": 2, "atmosphere": "clear", "tiles": ["deep-3"]},
    {"name": "Along the Ring", "end": 256, "speed": 140, "ramp": 2, "atmosphere": "clear",
     "tiles": ["deep-4", "ring-surface"]},
    {"name": "The Transit", "end": 268, "speed": 140, "atmosphere": "clear", "tiles": ["deep-5"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam, section_at_row = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row
DEEP = [f"deep-{i + 1}" for i in range(len(SECTIONS))]
SURFACE_SECTION = 3                                                   # section 4 (index 3): the ring's surface

SEED = 4301
PLANE_DIM = 0.55                                                      # the plane in the mouth before section 4
GLYPHS = 0.8                                                          # the ring's glyphs: a step under the Wardens'


def deep_row(t):
    return scroll(t) * FACTORS["deep"]


# =========================================================================== the deep field

def deep_span():
    return -40, int(np.ceil(deep_row(END + 4) + SCREEN))


def deep_field():
    """The whole deep layer as one float RGB field, row 0 = layer row r0 (y-up): the nebula, a step dimmer in
    section 3 (the ring's shadow)."""
    lo, hi = deep_span()
    r0, r1 = lo - 400, hi + 400
    n = r1 - r0
    rgb = a84.space_rgb(W, n, SEED)
    rows = np.arange(n) + r0
    s3, s4 = seam("deep", 2), seam("deep", 3)
    dim = 1 - 0.22 * smoothstep(s3 - 300, s3 + 60, rows) * (1 - smoothstep(s4 - 60, s4 + 300, rows))
    return np.clip(rgb * dim[:, None, None], 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


# =========================================================================== the ring pieces

def ring_piece(size, cx, cy, R, half, seed, plane_k=PLANE_DIM, plane_glow=0.0):
    """A ring piece: ``ring_paint`` over a clear canvas, the event plane (``plane_rgb``, mixed toward the nebula by
    ``1 - plane_k``) in its mouth; alpha = the stone band and the mouth, antialiased; the plane's light spilling past
    the rim as an additive ``-glow`` image (strength ``plane_glow``)."""
    w, h = size
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    r = np.hypot(xx - cx, yy - cy)
    plane = a84.plane_rgb(w, h, seed + 1) * plane_k + a84.space_rgb(w, h, seed + 2, k=1.3) * (1 - plane_k)
    rgb = np.zeros((h, w, 3))
    a84.ring_paint(rgb, cx, cy, R, half, seed, glyphs=GLYPHS, plane=plane)
    inside = smoothstep(0.0, 1.0, (R - half - r) / 3.0 + 0.5)
    band = np.clip(half - np.abs(r - R) + 0.5, 0, 1)
    alpha = np.clip(np.maximum(inside, band), 0, 1)
    body = rgba(rgb, alpha)
    light = np.zeros((h, w, 3))
    if plane_glow > 0:
        outer = np.exp(-((r - (R + half)) / (0.35 * half + 6)) ** 2) * (r > R + half - 1)
        light += (outer * plane_glow)[..., None] * a84.EV_VIOLET * 0.55
        light *= jovkit.soft_box(w, h, 6)[..., None]
    return body, additive_rgb(light)


# MOUTH 560 tall (step B5; was 420): the whole ring (outer radius 270) inside the canvas with a 10 px clear margin at
# the top and bottom, so no cut edge shows on the screen (BackdropAssetsTest); its sides run off the screen's edges
RING_FAR, RING_THIRD, RING_TOP, RING_RIM, MOUTH = (200, 200), (288, 288), (480, 300), (480, 240), (480, 560)


def ring_pieces():
    out = {}
    out["ring-far"] = ring_piece(RING_FAR, 100, 100, 64, 11, SEED + 10, plane_glow=0.5)
    out["ring-third"] = ring_piece(RING_THIRD, 144, 144, 104, 18, SEED + 20, plane_glow=0.5)
    # R 516, not 520 (step B5): the band's outer edge ends 4 px inside the canvas, not on its bottom row
    # (BackdropAssetsTest: a deep piece's bottom edge on the screen must be transparent)
    out["ring-top"] = ring_piece(RING_TOP, 240, -300, 516, 80, SEED + 30)
    out["ring-rim"] = ring_piece(RING_RIM, 240, -760, 900, 90, SEED + 40, plane_k=0.8)
    out["mouth"] = ring_piece(MOUTH, 240, 280, 236, 34, SEED + 50, plane_k=1.0, plane_glow=0.6)
    return out


# =========================================================================== low-air: spore sacs and dead tendrils

SAC, TENDRIL = (40, 52), (56, 176)


def spore_sac(seed):
    w, h = SAC
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    cx, cy, rx, ry = w / 2 + rng.uniform(-2, 2), h / 2 + 2, rng.uniform(12, 15), rng.uniform(15, 19)
    q = np.hypot((xx - cx) / rx, (yy - cy) / ry)
    tex = raster.fbm(w, h, 8, seed, octaves=3, period=False)
    shell = smoothstep(1.02, 0.86, q)
    lit = np.clip(0.5 + 0.5 * (-(xx - cx) / rx * 0.6 - (yy - cy) / ry * 0.6), 0, 1)
    rgb = raster.ramp(a84.R06, np.clip(0.18 + 0.2 * lit + 0.08 * tex, 0, 1))
    core = np.exp(-(q / 0.42) ** 2)
    rgb = rgb * (1 - 0.6 * core[..., None]) + np.array((96, 170, 160.0)) * 0.6 * core[..., None]
    rim = np.exp(-((q - 0.9) / 0.08) ** 2) * lit
    rgb += rim[..., None] * np.array((60, 40, 90.0))
    stalk = (np.abs(xx - cx) < 1.2) & (yy < cy - ry * 0.9) & (yy > 2)
    alpha = np.clip(shell * (0.55 + 0.35 * (1 - core)) + stalk * 0.6 * smoothstep(2, 10, yy), 0, 0.92)
    return rgba(rgb, alpha * jovkit.soft_box(w, h, 2))


def tendril(seed):
    w, h = TENDRIL
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    alpha = np.zeros((h, w))
    shade = np.zeros((h, w))
    for k in range(3):
        ph, amp, x0, th = rng.uniform(0, TAU), rng.uniform(5, 12), w / 2 + rng.uniform(-8, 8), rng.uniform(1.6, 3.0)
        cxs = x0 + amp * np.sin(yy / h * TAU * rng.uniform(0.6, 1.1) + ph)
        thick = th * (0.5 + 0.5 * np.sin(np.pi * yy / h))
        d = np.abs(xx - cxs)
        a = smoothstep(thick + 0.8, thick - 0.6, d)
        alpha = np.maximum(alpha, a)
        shade = np.maximum(shade, a * np.clip(0.5 - (xx - cxs) / (thick + 1e-6) * 0.4, 0, 1))
    ends = smoothstep(0, 30, yy) * smoothstep(h, h - 30, yy)
    rgb = raster.ramp([(0, (22, 16, 30)), (0.6, (60, 52, 72)), (1, (104, 96, 114))], shade)
    return rgba(rgb, alpha * ends * 0.85)


# =========================================================================== ground: the ring's surface

SURF_H = 1024
BAND = (52, 428)


def surface_field(h, seed=SEED + 60, wrap=True):
    """The ring's flat face (rows y-down within the image): plates of dark basalt, two glyph channels along the run,
    cross channels every 256 rows; rgb and alpha (the band, antialiased)."""
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64) + 0.5
    tex = raster.fbm(W, h, 64, seed, octaves=5, period=wrap)
    fine = raster.fbm(W, h, 16, seed + 1, octaves=3, period=wrap)
    x0, x1 = BAND
    edge = np.minimum(xx - x0, x1 - xx)
    alpha = np.clip(edge + 0.5, 0, 1)
    bevel = smoothstep(0, 14, edge)
    lit = np.where(xx < (x0 + x1) / 2, 1.0, 0.0) * (1 - bevel) * 0.35        # the left bevel catches the violet light
    shade = 0.32 + 0.22 * (tex - 0.5) + 0.1 * (fine - 0.5) - 0.18 * (1 - bevel) + lit
    plates = (np.abs(((yy + 37) % 128) - 64) > 62.6) | (np.abs(((xx - x0 + 23) % 94) - 47) > 45.6)
    shade = np.where(plates & (edge > 6), shade * 0.62, shade)
    light = np.zeros((h, W))
    for cxn in (148.0, 332.0):
        d = np.abs(xx - cxn)
        groove = smoothstep(5.5, 3.5, d)
        shade = shade * (1 - 0.55 * groove) + 0.05 * groove
        cells = yy / 22.0
        ci = np.floor(cells)
        hsh = (np.sin(ci * 12.9898 + cxn) * 43758.5453) % 1.0
        mark = (d < 2.2) & (np.abs(cells - ci - 0.5) < 0.22) & (hsh > 0.35)
        light = np.maximum(light, mark * 1.0)
    for ry in (64.0, 320.0, 576.0, 832.0):
        d = np.abs(yy - ry)
        groove = smoothstep(4.5, 2.5, d) * (edge > 10)
        shade = shade * (1 - 0.5 * groove) + 0.04 * groove
    stone = raster.ramp([(0, (10, 9, 13)), (0.35, (26, 24, 31)), (0.65, (40, 37, 46)), (1, (66, 62, 74))],
                        np.clip(shade, 0, 1))
    halo = jovkit.blur(light, 1.6, wrap_rows=wrap) if wrap else jovkit.blur(light, 1.6)
    stone += (light * GLYPHS)[..., None] * np.array((110, 186, 178.0)) * 0.55
    stone += (halo * GLYPHS)[..., None] * np.array((40, 110, 104.0)) * 0.8
    return np.clip(stone, 0, 255), alpha


def surface_tile():
    rgb, alpha = surface_field(SURF_H)
    return rgba(rgb, alpha)


LEAD, LIP = (480, 128), (480, 192)


def surface_edge(size, lead):
    """The strip's leading edge (``lead``: the band's end below the middle row, the tile above) or its inner lip (the
    tile below, the lip and its glyph row above). The tile side fades out so the tile under it shows through."""
    w, h = size
    rgb, alpha = surface_field(h, seed=SEED + 61 + lead, wrap=False)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    rng = np.random.default_rng(SEED + 62 + lead)
    edge_y = h / 2 + 6 * np.sin(xx / 37.0 + rng.uniform(0, TAU)) + 3 * np.sin(xx / 11.0 + rng.uniform(0, TAU))
    if lead:
        past = yy - edge_y                                    # > 0 below the edge: nothing there
        keep = smoothstep(0.5, -0.5, past)
        bevel = smoothstep(-12, 0, past) * keep
        fade_tile = smoothstep(0, h / 2 - 4, h / 2 - yy + 0.5)  # the tile side fading to 0 at the top
        a = keep * np.maximum(1 - fade_tile, bevel)
        rgb = rgb * (1 - 0.6 * bevel[..., None])
    else:
        past = edge_y - yy                                    # > 0 above the edge: the mouth
        keep = smoothstep(0.5, -0.5, past)
        bevel = smoothstep(-16, 0, past) * keep
        fade_tile = smoothstep(0, h / 2 - 4, yy - h / 2 + 0.5)
        a = keep * np.maximum(1 - fade_tile, bevel)
        rgb = rgb * (1 - 0.35 * bevel[..., None]) + (bevel * 0.35)[..., None] * np.array((60, 54, 70.0))
        row = (np.abs(yy - (edge_y + 9)) < 1.6) & ((((xx / 13.0) % 1.0) - 0.5) ** 2 < 0.04)
        rgb += (row * GLYPHS)[..., None] * np.array((110, 186, 178.0)) * 0.6
    return rgba(rgb, a * alpha)


# =========================================================================== the fleet

def steadfast_group():
    import backdrop_l40 as bd40  # noqa: E402  (Level 40's carrier group, unchanged)
    artkit.REVIEW_ROUND = ROUND                                        # (the import set its own round)
    return bd40.fleet_far()


# =========================================================================== build

PIECES = {"ring-far": RING_FAR, "ring-third": RING_THIRD, "ring-top": RING_TOP, "ring-rim": RING_RIM, "mouth": MOUTH}
GLOWS = {"ring-far", "ring-third", "mouth"}


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
    save("ring-surface", surface_tile())
    for name, (body, glow_img) in ring_pieces().items():
        save(name, body)
        if name in GLOWS:
            save(name + "-glow", glow_img)
    body, frames = steadfast_group()
    save("steadfast-group", body)
    for i, f in enumerate(frames):
        save(f"steadfast-group-glow_{i}", f)
    for k, s in enumerate("abc"):
        save(f"spore-sac-{s}", spore_sac(SEED + 70 + k))
    for k, s in enumerate("ab"):
        save(f"tendril-{s}", tendril(SEED + 80 + k))
    save("ring-surface-lead", surface_edge(LEAD, True))
    save("ring-surface-lip", surface_edge(LIP, False))
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


def tile_heights():
    return {p.stem: Image.open(p).height for p in OUT.glob("*.png") if p.stem in DEEP + ["ring-surface"]}


def pieces_spec():
    spec = {}
    for name, size in PIECES.items():
        spec[name] = {"layer": "deep", "size": list(size)}
        if name in GLOWS:
            spec[name + "-glow"] = {"layer": "deep", "size": list(size)}
    spec["steadfast-group"] = {"layer": "deep", "size": [128, 96]}
    spec["steadfast-group-glow"] = {"layer": "deep", "size": [128, 96], "frames": 8, "fps": 4}
    for s in "abc":
        spec[f"spore-sac-{s}"] = {"layer": "low-air", "size": list(SAC)}
    for s in "ab":
        spec[f"tendril-{s}"] = {"layer": "low-air", "size": list(TENDRIL)}
    spec["ring-surface-lead"] = {"layer": "ground", "size": list(LEAD)}
    spec["ring-surface-lip"] = {"layer": "ground", "size": list(LIP)}
    return spec


# =========================================================================== the places

PARK = 3000.0                                                          # px off to the right while not in its section


def hold_path(target, times, f, park=None):
    """A placement time and a path putting the piece's centre at screen y ``target(t)`` at each of ``times`` (y down;
    the offsets are px up the screen); ``park`` = (before, after): parked PARK px off to the right before ``before``
    and after ``after`` (the swap at a section change)."""
    t_ref = times[0]
    pt = CLOCK.t_centre_at(t_ref, target(t_ref), f)

    def dy(t):
        return 270.0 + (scroll(t) - scroll(pt)) * f - target(t)
    path = [[round(t, 3), 0, round(dy(t), 1)] for t in times]
    if park:
        before, after = park
        if before is not None:
            path = [[round(before - 0.05, 3), PARK, round(dy(before), 1)]] + path
        if after is not None:
            path = path + [[round(after + 0.05, 3), PARK, round(dy(after), 1)]]
    return round(pt, 3), path


def lerp_target(points):
    ts, ys = zip(*points)
    return lambda t: float(np.interp(t, ts, ys))


def at_row(row, f):
    return CLOCK.t_for_layer_row(row, f)


def placements():
    s = STARTS + [END]
    f = FACTORS["deep"]
    out = []

    def held(piece, sec, y0, y1, note, extra_times=()):
        a, b = s[sec], s[sec + 1]
        times = sorted({a, b, *extra_times})
        t, path = hold_path(lerp_target([(a, y0), (b, y1)]), times, f,
                            park=(a if sec > 0 else None, b if sec < len(SECTIONS) - 1 else None))
        out.append({"piece": piece, "t": t, "x": 240 if piece != "ring-far" else 240, "path": path, "note": note})

    held("ring-far", 0, 140, 170, "section 1: the ring small ahead, held at y 140-170 (swapped at t=48: B4's 2 s "
                                  "cross-fade to ring-third)")
    held("ring-third", 1, 150, 190, "section 2: the ring a third of the screen, y 150-190 (cross-fade at t=48 in, "
                                    "t=124 out)")
    held("ring-top", 2, 120, 148, "section 3: the ring's near arc filling the top of the screen (cross-fade at t=124 "
                                  "in, t=200 out)")
    held("ring-rim", 3, 60, 118, "section 4: the mouth's inner rim rising at the top like a horizon (cross-fade at "
                                 "t=200 in, t=256 out)")
    held("mouth", 4, 230, 250, "section 5: the event plane filling the ring's centre ahead (cross-fade at t=256); "
                               "the transit's warp from t=266.5 is the game's post-effect")
    # the fleet behind: bottom of the screen at the start, off the bottom by t=20
    t, path = hold_path(lerp_target([(0.0, 470.0), (20.0, 610.0)]), [0.0, 20.0], f)
    out.insert(0, {"piece": "steadfast-group", "t": t, "x": 150, "path": path,
                   "note": "section 1: the Steadfast's group behind, at the bottom at the start, sliding off by t=20"})
    rng = np.random.default_rng(SEED + 90)
    fl = FACTORS["low-air"]
    for k, tc in enumerate((5.0, 10.5, 16.0, 22.5, 29.0, 35.5, 41.5, 46.0)):
        x = int(rng.integers(40, 440))
        out.append({"piece": f"spore-sac-{'abc'[k % 3]}", "t": at_row(scroll(tc) * fl + SCREEN / 2, fl), "x": x,
                    "note": "section 1: a spore sac drifting past" if k == 0 else None})
    for k, tc in enumerate((54.0, 63.0, 73.0, 84.0, 95.0, 106.0, 117.0)):
        x = int(rng.integers(40, 440))
        out.append({"piece": f"tendril-{'ab'[k % 2]}", "t": at_row(scroll(tc) * fl + SCREEN / 2, fl), "x": x,
                    "note": "section 2: a dead tendril drifting past" if k == 0 else None})
    out.append({"piece": "ring-surface-lead", "t": at_row(seam("ground", SURFACE_SECTION), 1.0), "x": 240,
                "note": "section 4: the strip's leading edge, its centre on the ground seam (hides the tile's start)"})
    out.append({"piece": "ring-surface-lip", "t": at_row(seam("ground", SURFACE_SECTION + 1), 1.0), "x": 240,
                "note": "section 5: the strip's inner lip at the ground seam (the mouth beyond)"})
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
             f"  # Presentation only: Level 43's gate approach (tools/art/backdrop_l43.py, step ART-B, sections from {src}).",
             "  # Vacuum: no haze. The deep layer is one continuous field cut at the rows each section's tiles begin",
             "  # (LevelData.seam): re-run the generator after moving a section. The ring's five pieces are held on the",
             "  # screen by paths and parked 3000 px to the right outside their section; step B4 cross-fades them over",
             "  # 2 s at each section change (m11b-gaps §4). The ground strip (section 4) is a tile set with its two edge",
             "  # pieces on the ground seams. Steady glows: the glyphs never pulse.",
             "  scroll_factors: {deep: 0.03, far: 0.3, ground: 1, low-air: 1.3, high-air: 1.7}   # far, high-air unused",
             "  ramp: 2",
             "  haze_colour: 06050c",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        lines.append(f"    {name}: {{layer: deep, height: {h[name]}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    lines.append(f"    ring-surface: {{layer: ground, height: {h['ring-surface']}}}   # section 4: the ring's surface strip")
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
    lines.append("sections:   # ART-B's reading of the README's Layout (2 s eases); B2 writes the real ones")
    for sec in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k != "notes") + "}")
    lines.append(f"# ground objects: the glyph stones sit on the strip, x {BAND[0] + 30}-{BAND[1] - 30} (the band is "
                 f"x {BAND[0]}-{BAND[1]}); rows: the end's bottom edge {int(round(scroll(END)))} (ground px)")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces_spec(), placements()), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"]


# =========================================================================== composite (the review's screens)

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


def layer_tiles(t, layer="deep"):
    f = FACTORS[layer]
    off = scroll(t) * f
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec.get("tiles") or [] if (n in DEEP) == (layer == "deep")]
        if not names:
            continue
        arr, rows = tile_rows(names[0], off)
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


def piece_xy(p, spec, t):
    f = FACTORS[spec["layer"]]
    dx, dy = path_offset(p, t)
    centre = scroll(p["t"]) * f + SCREEN / 2 + dy
    return p["x"] + dx, SCREEN - (centre - scroll(t) * f)


def blit(img, p, pieces, t, layer, glow_layer=False):
    name = p["piece"] + ("-glow" if glow_layer else "")
    spec = pieces.get(name)
    if not spec or pieces[p["piece"]]["layer"] != layer:
        return img
    ims = images(name, spec)
    frame = ims[int(np.floor(t * spec.get("fps", 0))) % len(ims)] if spec.get("frames") else ims[0]
    x, y = piece_xy(p, pieces[p["piece"]], t)
    if y - frame.height / 2 > SCREEN or y + frame.height / 2 < 0 or x - frame.width / 2 > W or x + frame.width / 2 < 0:
        return img
    return l23kit.add_c(img, frame, x, y) if glow_layer else l23kit.paste_c(img, frame, x, y)


def compose(t, units=True):
    pieces, placed = load_proposal()
    img = layer_tiles(t, "deep")
    for layer in ("deep", "ground", "low-air"):
        if layer == "ground":
            img.alpha_composite(layer_tiles(t, "ground"))
        for p in placed:
            img = blit(img, p, pieces, t, layer)
            img = blit(img, p, pieces, t, layer, glow_layer=True)
        if layer == "ground" and units:
            img = stand_ins(img, t)
    return img


def load_held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def stand_ins(img, t):
    wd, gl = load_held("gate-warden"), load_held("gate-warden-glyphs")
    st = load_held("glyph-stone")
    if st and 200 <= t <= 240:
        for k, x in enumerate((110, 200, 290, 370)):
            img = l23kit.paste_c(img, st[0], x, 140 + (t - 204) * 140 + k * 30 - 260 * (t > 220))
    if wd and 30 <= t <= 230:
        o = 5 if int(t) % 4 == 1 else 0
        for x in ((240,) if t < 60 else (150, 330)):
            img = l23kit.paste_c(img, wd[o], x, 150)
            if gl:
                img = l23kit.add_c(img, gl[0], x, 150, 0.18 + 0.62 * o / 5)
    ship = load_held("ship")
    if ship:
        img = l23kit.paste_c(img, ship[2] if len(ship) > 2 else ship[0], 240, 470)
    return img


SCREENS = (4.0, 30.0, 80.0, 150.0, 190.0, 205.0, 250.0, 262.0)


def review():
    shots = [compose(t) for t in SCREENS]
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * 290 + 30 + 360, "LEVEL 43 BACKDROP (FINAL R89): THE TETHER GATE",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, (t, s) in enumerate(zip(SCREENS, shots)):
        x, y = 12 + (i % 4) * 246, 40 + (i // 4) * 290
        raster.draw_text(sheet, x, y, f"T={t:g} S ({SECTIONS[section_at_t(t)]['name'].upper()})", raster.LABEL)
        sheet.alpha_composite(s.resize((240, 270), Image.LANCZOS), (x, y + 12))
    y = 40 + 2 * 290 + 10
    raster.draw_text(sheet, 12, y, "PIECES (HALF SIZE) AND THE RING'S SURFACE TILE", raster.LABEL)
    x = 12
    for name in ("ring-far", "ring-third", "ring-top", "ring-rim", "mouth", "steadfast-group", "spore-sac-a",
                 "spore-sac-b", "tendril-a", "ring-surface-lead", "ring-surface-lip"):
        im = Image.open(OUT / f"{name}.png").convert("RGBA")
        bg = Image.new("RGBA", im.size, (6, 5, 12, 255))
        bg.alpha_composite(im)
        g = OUT / f"{name}-glow.png"
        if g.exists():
            bg = l23kit.add_c(bg, Image.open(g), im.width / 2, im.height / 2)
        sm = bg.resize((max(1, im.width // 2), max(1, im.height // 2)), Image.LANCZOS)
        if x + sm.width > w - 12:
            break
        sheet.alpha_composite(sm, (x, y + 14))
        x += sm.width + 8
    tile = Image.open(OUT / "ring-surface.png").convert("RGBA").crop((0, 0, 480, 300)).resize((240, 150))
    tb = Image.new("RGBA", tile.size, (6, 5, 12, 255))
    tb.alpha_composite(tile)
    sheet.alpha_composite(tb, (12, y + 14 + 216))
    frames = [compose(t).resize((240, 270), Image.LANCZOS) for t in np.arange(0, END, 4.0)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=4)
    print("review: backdrop-final-r89-a")


def section_at_t(t):
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]):
            return i
    return len(SECTIONS) - 1


# =========================================================================== the act still (A.9 b "the mouth")

def still_mouth(seed=8411):
    """The act still: the ring head-on and centred, its mouth filled with the event plane's violet-white, the
    Steadfast's group's lights strung in a line toward it from the lower left; this backdrop's painter (ring_paint,
    plane_rgb, space_rgb) and glyph strength, 960x540, unquantised."""
    sw, sh = 960, 540
    rgb = a84.space_rgb(sw, sh, seed)
    plane = a84.plane_rgb(sw, sh, seed + 1)
    a84.ring_paint(rgb, 480, 270, 300, 70, seed + 2, glyphs=GLYPHS, plane=plane)
    light = np.zeros((sh, sw, 3))
    for k in range(7):
        u = k / 6
        x, y = 160 + 300 * u, 470 - 190 * u
        glow(light, x, y, 2.4 - 1.4 * u, (235, 240, 255), 0.95)
        glow(light, x, y, 7.0 - 4.0 * u, (130, 160, 255), 0.22)
        glow(light, x - 9 * (1 - u) - 3, y + 6 * (1 - u) + 2, 3.0 - 1.6 * u, (110, 150, 255), 0.35)
    return Image.fromarray(np.clip(rgb + light, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


# =========================================================================== check

def check():
    bad = 0
    pieces = pieces_spec()
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        src = im.text.get(artkit.SOURCE_KEY) == SOURCE
        v, c = checker_share(im)
        share = c / max(v, 1)
        msg, ok = [], src and share < 0.05
        if p.stem in DEEP:
            ws = wrap_score(np.array(im.convert("RGBA")))
            ok &= ws < 3.0
            msg.append(f"wrap {ws:.2f}")
        if p.stem == "ring-surface":
            ws = jovkit.wrap_score_alpha(np.array(im.convert("RGBA")))
            ok &= ws < 1.0
            msg.append(f"wrap {ws:.2f}")
        if "-glow" in p.stem:
            a = np.array(im.convert("RGBA"))[..., :3].max(axis=-1)
            lit = max(a[0].max(), a[-1].max(), a[:, 0].max(), a[:, -1].max())
            ok &= lit == 0
            msg.append(f"edge {lit}")
        base = p.stem.split("_")[0].removesuffix("-glow")
        if base in pieces:
            ok &= list(im.size) == pieces[base]["size"]
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {p.name}: {im.size}, checker {share * 100:.1f} %, {', '.join(msg)}")
    for name in ("deep", "ground"):
        pass
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
