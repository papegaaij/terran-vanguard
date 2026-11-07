#!/usr/bin/env python3
"""Production art: Level 49 "Echoes", the Choir's memory (design/campaign/act-7-beyond-the-gate/level-49-echoes; M11 part
G batch, concept round 94, step ART-G): the memory backdrop kit, the remembered Hellas basin floor, the supply pod, the
spectral tint's reference sheet.

Straight to production from round 84's chosen look (**A.8 a**, tools/concept/act7_r84.py ``memory_bg``,
``spectral``, ``dissolve_mask``, ``tissue_rgb``, ``spectral_sprite``, imported unchanged): the earlier acts' worlds in
their own colours under the echo tint (desaturated 40 % toward a cold violet-white of their own brightness, a step
darker than the units so that the tinted units read a step brighter), dissolving into the palette's dark tissue at
their sides with a thin violet rim. Every field is a float written unquantised (far more than 32 steps, no ordered
dither: DitherGuardTest's measure); every translucent edge fades to 0 inside its canvas; nothing is ruler-straight
(``--check`` runs act7_citadel.py's straight-line scan, step FIXC-SPINE's measure). Vacuum: no haze.

The layers (``scroll_factors``): ``deep`` 0.12 (the tissue, one tile set the whole level; the memories' big pictures
as pieces), ``ground`` 1 (gate 2 only: the remembered Hellas basin floor), ``low-air`` 1.3 (each memory's near
scenery, translucent). ``far`` and ``high-air`` unused.

How the game draws a memory (step G4, ``MemoryLooks``; the yaml's comments say it again): a memory is its stretch's
deep piece, its low-air tile set and (gate 2) its ground tile set, drawn **over the tissue** with one visibility mask:
it forms from the top edge down over 2 s at its stretch's start and melts at its end (sides first), both read from
``memory-mask.png``; the next memory forms after. The tile sets therefore never meet at a seam on screen (the mask
hides the switch), and every tile set wraps in y so a crawl of any length is covered.

Tile sets (480 wide, wrapping in y; ``tissue`` and ``hellas-floor`` in x too):
  tissue          deep 480x1024, opaque: the hive's dark tissue (near-black violet, faint veins), the whole level
  luna-regolith   low-air 480x1024: fragments of Luna's grey regolith (craters) with violet rims, the L1 point's wreck
                  lights (section 1), at most 80 % opaque, thinning toward the sides
  l1-debris       low-air 480x1024: the L1 picket's wreck shards with a few dim lights (gate 1)
  valles-veils    low-air 480x1024: Valles Marineris's walls far below along both sides (at most 55 %) and thin dust
                  veils between them (at most 16 %; scenery under every bullet) (section 2)
  belt-rocks      low-air 480x1024: tumbling dark rocks, partly dissolved, violet rims (section 3)
  hellas-floor    ground 480x1344 (>= 1,300 px for the crawl; it wraps, so any crawl is covered): the remembered Hellas
                  basin floor, ripples, old breach scars and stones, opaque between x 36 and 444, its sides dissolving
                  into the tissue (gate 2)
Pieces (deep, centred on their stretch at its middle; alpha fading to 0 inside the canvas at all four sides):
  luna-earth      480x1072: Luna's grey limb across the lower half, Earth's crescent above it (section 1)
  mars-disc       480x1060: Mars's rust disc, Valles Marineris's scar, the polar cap, the dust limb, Phobos (section 2)
  belt-ceres      480x1052: the belt's dark rock mass and rocks, Ceres far off at the top with its station lights (3)
  l1-point        480x904: the L1 point: Earth's crescent, the Moon, the picket's wreck lights in a broken ring (gate 1)
  ceres-orbit     480x760: Ceres's curve from the right with the station's lights (gate 3, the halt)
  core-far        224x224 + core-far-glow 320x320 (additive): the hive's core far ahead, its red-violet glow (the
                  unmaking)
Data image (not drawn):
  memory-mask     480x540 RGB: R = the forming threshold (0 at the top edge .. 1 at the bottom, smooth noise), G = the
                  melting threshold (0 at the sides .. 1 in the middle, smooth noise). A memory's visibility at a pixel
                  is smoothstep(R - 0.08, R + 0.08, p * 1.16 - 0.08) while forming (p 0 -> 1 over 2 s) and
                  1 - smoothstep(G - 0.08, G + 0.08, q * 1.16 - 0.08) while melting (q 0 -> 1 over 2 s): smooth, the
                  luminance one-way, no flash.
Sprites (build/art-held/sprites/ until a level's data claims them; vanguard.pipeline.SpriteUse):
  debris-supply-pod_0..15        36x36: a remembered Coalition supply pod (Act 1's convoys: rust-brown container,
                                 hazard-striped ends, grey frame bands) tumbling (16 headings, a fixed 30 degree tilt),
                                 its upper right dissolving into violet motes; in its own colours: the game's echo tint
                                 (G4's EchoTint) tints it like every remembered unit
  debris-supply-pod-break_0..7   64x64: broken open: its chips flung out and dissolving into violet motes, a violet
                                 puff; no flash
Review (round 94): the level's concept/ ``backdrop-final-r94-a`` (.png: every stretch's screen with tinted stand-ins and
untinted bullets; the tiles and pieces; .gif: Luna melting and Mars forming), ``echo-tint-final-r94-a.png`` (the tint's
reference sheet for G4's shader: every remembered unit and the three echoes, original and tinted over its world at 1:1,
the constants, the shimmer, the measured step), ``supply-pod-final-r94-a`` (.png/.gif).

``--proposal`` writes the ``backdrop:`` block and a section proposal to concept/backdrop-proposal.yaml (step G2 owns
the sections; the pieces are placed at their stretch's middle by the data's clock: re-run after the data moves a
section). The backdrop is held in build/art-held/backdrop/level-49/ until the level's data.yaml has a ``backdrop``
block, then written to assets/backdrop/level-49/ (re-run this script).
Run: nice -n 19 python3 tools/art/act7_memory.py [backdrop] [pod] [--review] [--proposal] [--check]   (~1-2 min)
"""
import os
import sys
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
import yaml
from PIL import Image, ImageDraw

import act7_citadel as cit
import artkit
import backdrop_l44 as b44
import belt_units as bu
import jovkit
import l23kit
from artkit import DESIGN, ROOT, TAU
from jovkit import additive_rgb, checker_share, rgba, smoothstep, wrap_score, wrap_score_alpha

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: A.8 a's spectral tint, dissolve and tissue, imported unchanged)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_sphere, union  # noqa: E402

SCRIPT = "act7_memory.py"
BATCH = "M11 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r94"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-49-echoes"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
ASSETS_OUT = ROOT / "assets" / "backdrop" / "level-49"
HELD_OUT = ROOT / "build" / "art-held" / "backdrop" / "level-49"
OUT = ASSETS_OUT if DATA and DATA.get("backdrop") else HELD_OUT
W, SCREEN, SPEED = 480, 540, 100.0
FACTORS = {"deep": 0.12, "far": 0.35, "ground": 1.0, "low-air": 1.3, "high-air": 2.0}
SEED = 4900
WORKERS = min(6, os.cpu_count() or 1)

# The tint (A.8 a; the reference for G4's EchoTint): the world a step darker than the units.
TINT_K = 0.4                                   # desaturation toward violet-white of the colour's own brightness
UNIT_LIFT = 0.26                               # the units: rgb x 0.74 + violet-white x 0.26 (a step brighter)
WORLD_AMB = 0.8                                # the worlds: the tinted colour x 0.8
SHIMMER_HZ, SHIMMER_AMP = 0.35, 0.06           # a smooth brightness shimmer, <= 0.5 Hz
VW = np.array(a84.VIOLET_W, float)             # (226, 206, 255)
RIM = np.array((150, 110, 200), float)         # the violet rim where a memory dissolves (memory_bg's)
TISSUE = [(0.0, (5, 3, 10)), (0.6, (26, 12, 34)), (1.0, (58, 28, 64))]   # tissue_rgb's base ramp
SPACE = np.array((5, 5, 9), float)
LIGHT = np.array((-0.5, -0.56, 0.66))
LIGHT = LIGHT / np.linalg.norm(LIGHT)


# =========================================================================== fields

def pf(h, cx, cy, seed, octaves=4, w=W):
    """Smooth noise periodic in x over w and in y over h (backdrop_l44.pfbm; cx >> o must divide w, cy >> o h)."""
    return b44.pfbm(w, h, cx, cy, seed, octaves)


def nf(w, h, cell, seed, octaves=4):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=False)


def grid(h, w=W):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    return yy + 0.5, xx + 0.5


def shade_of(hgt, k, wrap=True):
    """Lambert-like relief shade from a height field, lit from the top left."""
    if wrap:
        gx = (np.roll(hgt, -1, 1) - np.roll(hgt, 1, 1)) / 2
        gy = (np.roll(hgt, -1, 0) - np.roll(hgt, 1, 0)) / 2
    else:
        gy, gx = np.gradient(hgt)
    return np.clip(0.85 + (-gx - gy) * k, 0.35, 1.4)


def wrapped(d, period):
    return (d + period / 2) % period - period / 2


def craters(h, seed, n, rmin, rmax, w=W, periodic=True):
    """A crater height field (a84.luna_mem's bowls and rims), wrapping in both directions when periodic."""
    rng = np.random.default_rng(seed)
    yy, xx = grid(h, w)
    out = np.zeros((h, w))
    for _ in range(n):
        cx, cy, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(rmin, rmax)
        dx, dy = (wrapped(xx - cx, w), wrapped(yy - cy, h)) if periodic else (xx - cx, yy - cy)
        d = np.hypot(dx, dy) / r
        out += (-0.6 * np.clip(1 - d, 0, 1) ** 1.5 + 0.35 * np.exp(-((d - 1) / 0.18) ** 2)) * (r / 46) ** 0.5
    return out


def world_tint(rgb):
    """A remembered world's colours under the echo tint (a84.spectral, k 0.4), a step darker than the units."""
    return np.clip(a84.spectral(np.asarray(rgb, float) / 255.0, TINT_K) * 255 * WORLD_AMB, 0, 255)


def side_mask(h, seed, periodic=True, w=W, edge=0.26):
    """a84.dissolve_mask: 1 = the world, 0 = the tissue at its sides (0 at the canvas's left and right edges)."""
    n = pf(h, 48, 64, seed, 4, w) if periodic else nf(w, h, 46, seed)
    xx = np.arange(w)[None, :] + 0.5
    side = np.minimum(xx, w - xx) / (w * edge)
    return a84.smooth01((side - 0.4 + (n - 0.5) * 0.8) * 2.6)


def end_mask(h, w, seed, fade=90.0):
    """A piece's top and bottom ends melting to 0 inside the canvas along a noisy line (never a straight cut)."""
    n = nf(w, h, 40, seed, 3)
    yy = np.arange(h)[:, None] + 0.5
    e = np.minimum(yy, h - yy)
    return a84.smooth01((e - 4 - n * fade * 0.6) / (fade * 0.6))


def rim(m, k=0.5):
    return np.exp(-((m - 0.5) / 0.16) ** 2) * k


def memory_piece(rgb, seed, fade=90.0):
    """A deep piece: its world tinted, its sides dissolving into the tissue and its ends melting, the violet rim."""
    h, w = rgb.shape[:2]
    m = side_mask(h, seed, periodic=False, w=w) * end_mask(h, w, seed + 1, fade)
    out = world_tint(rgb) + rim(m)[..., None] * RIM
    return rgba(out, m)


def stars(h, w, seed, density=0.0009, periodic=False):
    rng = np.random.default_rng(seed)
    light = np.zeros((h, w, 3))
    for _ in range(int(h * w * density)):
        x, y, k = rng.uniform(0, w), rng.uniform(0, h), 0.25 + 0.75 * rng.random() ** 3
        for oy in ((-h, 0, h) if periodic else (0,)):
            jovkit.glow(light, x, y + oy, 0.75, (235, 235, 250), k)
    return light


def over(rgb, a, src, sa):
    """Straight-alpha 'over' of float fields (rgb 0..255, alpha 0..1)."""
    out_a = sa + a * (1 - sa)
    out = (src * sa[..., None] + rgb * (a * (1 - sa))[..., None]) / np.maximum(out_a, 1e-6)[..., None]
    return out, out_a


def sphere(h, w, cx, cy, r, albedo, light=LIGHT, ambient=0.06, power=0.9):
    """A lit sphere over a flat albedo field (h, w, 3): returns (rgb, coverage, normal z, light term)."""
    yy, xx = grid(h, w)
    dx, dy = (xx - cx) / r, (yy - cy) / r
    rr = dx * dx + dy * dy
    nz = np.sqrt(np.clip(1 - rr, 0, 1))
    lam = np.clip(dx * light[0] + dy * light[1] + nz * light[2], 0, 1) ** power
    cov = np.clip((1 - np.sqrt(rr)) * r + 0.5, 0, 1)
    return albedo * (ambient + (1 - ambient) * lam)[..., None], cov, nz, lam


def blob(h, w, cx, cy, r, seed, lump=0.3):
    """A lumpy round mass (coverage 0..1, soft 1.5 px edge) and its relief shade."""
    yy, xx = grid(h, w)
    ang = np.arctan2(yy - cy, xx - cx)
    rng = np.random.default_rng(seed)
    wob = sum(rng.uniform(-1, 1) * np.cos(k * ang + rng.uniform(0, TAU)) / k for k in range(2, 7))
    rad = r * (1 + lump * wob * 0.6)
    d = np.hypot(xx - cx, yy - cy)
    cov = np.clip((rad - d) / 1.5 + 0.5, 0, 1)
    nzv = np.sqrt(np.clip(1 - (d / np.maximum(rad, 1)) ** 2, 0, 1))
    lam = np.clip(((xx - cx) / np.maximum(rad, 1)) * LIGHT[0] + ((yy - cy) / np.maximum(rad, 1)) * LIGHT[1]
                  + nzv * LIGHT[2], 0, 1)
    return cov, 0.18 + 0.82 * lam


def load_asset(path, size=None):
    im = Image.open(path).convert("RGBA")
    if size:
        im = im.resize(size, Image.LANCZOS)
    return np.array(im).astype(np.float64)


# =========================================================================== tile sets

TILE_H, HELLAS_H = 1024, 1344


def tissue_tile(h=TILE_H, seed=SEED):
    """a84.tissue_rgb made periodic: near-black violet with faint curving veins."""
    n = pf(h, 96, 128, seed, 4)
    v = pf(h, 120, 128, seed + 1, 3) + 0.12 * (pf(h, 40, 64, seed + 2, 3) - 0.5)   # the veins meander
    vein = np.exp(-(np.abs(np.sin(v * 18)) / 0.1) ** 2)
    rgb = raster.ramp(TISSUE, n) + (vein * 0.36)[..., None] * np.array((90, 40, 110.0))
    return rgba(rgb, np.ones((h, W)))


def regolith(h, seed, periodic=True):
    """Luna's grey regolith (a84.luna_mem's ramp, craters and relief), periodic when asked."""
    hgt = (pf(h, 96, 128, seed, 5) if periodic else nf(W, h, 60, seed, 5)) * 0.5
    hgt = hgt + craters(h, seed + 1, int(26 * h / 540), 6, 40, periodic=periodic)
    shade = shade_of(jovkit.blur(hgt, 1.0, wrap_rows=periodic) * 30, 0.9, wrap=periodic)
    return raster.ramp([(0, (42, 42, 46)), (0.5, (106, 106, 110)), (1, (164, 164, 166))],
                       np.clip(0.35 + 0.4 * hgt, 0, 1)) * shade[..., None]


def lights_field(h, w, seed, n, cols, r=(0.8, 1.6), periodic=True, where=None):
    """Small lights (glows in a float field): wreck and station lights."""
    rng = np.random.default_rng(seed)
    light = np.zeros((h, w, 3))
    for i in range(n):
        x, y = where(rng) if where else (rng.uniform(0, w), rng.uniform(0, h))
        col = cols[i % len(cols)]
        rad, k = rng.uniform(*r), rng.uniform(0.6, 1.0)
        for oy in ((-h, 0, h) if periodic else (0,)):
            jovkit.glow(light, x, y + oy, rad, col, k)
            jovkit.glow(light, x, y + oy, rad * 3.2, col, k * 0.18)
    return light


def light_over(rgb, a, light):
    """Lights laid into a translucent tile: their colour (tinted) where they add, alpha raised to their strength."""
    lt = world_tint(np.clip(light, 0, 255)) / WORLD_AMB
    k = np.clip(light.max(axis=-1) / 255.0, 0, 1)
    return over(rgb, a, lt, k)


def luna_regolith(seed=SEED + 10):
    h = TILE_H
    tex = world_tint(regolith(h, seed))
    isl = a84.smooth01((pf(h, 96, 128, seed + 3, 4) - 0.56) * 6)
    side = smoothstep(0, 150, np.minimum(np.arange(W) + 0.5, W - 0.5 - np.arange(W)))[None, :]
    a = isl * 0.8 * (0.35 + 0.65 * side) * side ** 0.5
    rgb = tex + rim(isl, 0.6)[..., None] * RIM
    light = lights_field(h, W, seed + 4, 12, [(255, 214, 170), (255, 240, 220), (255, 120, 90)], where=lambda g: (
        g.uniform(90, 390), g.uniform(0, h)))
    rgb, a = light_over(rgb, a, light)
    return rgba(rgb, a)


def shard_field(h, seed, n, rmin, rmax, cols, xr=(70, 410)):
    """Lumpy shards or rocks (wrapping in y): coverage and colour fields."""
    rng = np.random.default_rng(seed)
    a = np.zeros((h, W))
    rgb = np.zeros((h, W, 3))
    for i in range(n):
        x, y, r = rng.uniform(*xr), rng.uniform(0, h), rng.uniform(rmin, rmax)
        base = np.array(cols[i % len(cols)], float) * rng.uniform(0.8, 1.1)
        y0, y1 = int(y - 2 * r - 4), int(y + 2 * r + 4)
        x0, x1 = max(0, int(x - 2 * r - 4)), min(W, int(x + 2 * r + 4))
        rows = np.arange(y0, y1) % h
        sub_h, sub_w = y1 - y0, x1 - x0
        cov, sh = blob(sub_h, sub_w, x - x0, y - y0, r, seed + 17 * i, lump=0.45)
        tex = nf(sub_w, sub_h, max(2, int(r // 2)), seed + 31 * i, 3)
        col = base * (sh * (0.8 + 0.4 * tex))[..., None]
        cur_rgb, cur_a = rgb[rows, x0:x1], a[rows, x0:x1]
        nr, na = over(cur_rgb, cur_a, col, cov)
        rgb[rows, x0:x1], a[rows, x0:x1] = nr, na
    return rgb, a


def l1_debris(seed=SEED + 20):
    h = TILE_H
    rgb, a = shard_field(h, seed, 30, 3, 10, [(70, 76, 90), (96, 92, 84), (54, 58, 70)], xr=(60, 420))
    dis = a84.smooth01((pf(h, 48, 64, seed + 2, 3) - 0.3) * 4)            # parts of each shard already gone
    a = a * dis * 0.85
    rgb = world_tint(rgb) + rim(dis, 0.5)[..., None] * RIM * (a > 0.02)[..., None]
    light = lights_field(h, W, seed + 4, 14, [(255, 200, 150), (255, 110, 80), (240, 240, 255)],
                         where=lambda g: (g.uniform(70, 410), g.uniform(0, h)))
    rgb, a = light_over(rgb, a, light)
    return rgba(rgb, a)


def valles_veils(seed=SEED + 30):
    """Valles Marineris's walls far below along both sides (terraced rust cliffs, at most 55 %) and dust veils."""
    h = TILE_H
    yy, xx = grid(h)
    u = yy / h * TAU
    left = 62 + 22 * np.sin(2 * u + 0.7) + 11 * np.sin(5 * u + 1.3) + 14 * (pf(h, 48, 64, seed, 3) - 0.5)
    right = W - (62 + 20 * np.sin(2 * u + 2.9) + 12 * np.sin(4 * u + 0.2) + 14 * (pf(h, 48, 64, seed + 1, 3) - 0.5))
    a = np.zeros((h, W))
    rgb = np.zeros((h, W, 3))
    terr = pf(h, 40, 64, seed + 2, 4)
    for c, sgn in ((left, 1), (right, -1)):
        d = (xx - c) * sgn                                                # > 0 toward the canyon's middle
        band = smoothstep(-34, -18, d) * (1 - smoothstep(14, 32, d))
        steps = np.sin((d * 0.22 + terr * 5.0) * 2.0)                      # terraces bending with the noise
        shade = 0.55 + 0.25 * steps + 0.25 * smoothstep(-30, 20, d) * (sgn > 0)
        col = raster.ramp([(0, (70, 30, 18)), (0.5, (138, 68, 38)), (1, (194, 122, 74))], np.clip(shade, 0, 1))
        rgb, a = over(rgb, a, col, band * 0.55)
    edge = np.minimum(xx, W - xx)
    a = a * smoothstep(0, 22, edge)
    v = pf(h, 160, 128, seed + 3, 4)
    veil = np.clip((v - 0.55) * 2.5, 0, 1) * 0.16 * smoothstep(20, 120, edge)
    rgb, a = over(rgb, a, np.ones((h, W, 3)) * np.array((206, 160, 120.0)), veil)
    return rgba(world_tint(rgb), a)


def belt_rocks(seed=SEED + 40):
    h = TILE_H
    rgb, a = shard_field(h, seed, 11, 8, 26, [(84, 76, 66), (64, 60, 56), (102, 90, 74)], xr=(50, 430))
    dis = a84.smooth01((pf(h, 48, 64, seed + 2, 3) - 0.25) * 3.5)
    a = a * dis * 0.92
    rgb = world_tint(rgb) + rim(dis, 0.55)[..., None] * RIM * (a > 0.02)[..., None]
    return rgba(rgb, a)


def hellas_floor(seed=SEED + 50):
    """The remembered Hellas basin floor (Levels 20-21's sand) in the tint: dune undulation, ripples whose crests bend
    with the noise, old breach scars, stones; opaque between x 36 and 444, its sides dissolving into the tissue."""
    h = HELLAS_H
    yy, xx = grid(h)
    dune = pf(h, 96, 192, seed, 5)
    warp = pf(h, 80, 192, seed + 1, 3)
    patch = pf(h, 96, 96, seed + 2, 3)
    r1 = np.sin(yy / 24.0 * TAU + (warp - 0.5) * 11.0 + (dune - 0.5) * 4.0)
    r2 = np.sin(yy / 16.0 * TAU + (pf(h, 60, 96, seed + 6, 3) - 0.5) * 14.0 + xx / 37.0)
    ripple = r1 * smoothstep(0.35, 0.75, patch) + 0.7 * r2 * smoothstep(0.65, 0.25, patch)
    hgt = dune * 10.0 + ripple * 0.3
    rng = np.random.default_rng(seed + 3)
    for _ in range(6):                                                    # old breaches: shallow bowls, soft rims
        cx, cy, r = rng.uniform(70, 410), rng.uniform(0, h), rng.uniform(30, 64)
        d = np.hypot(xx - cx, wrapped(yy - cy, h)) / r
        hgt += -2.2 * np.clip(1 - d, 0, 1) ** 2 + 0.9 * np.exp(-((d - 1.05) / 0.2) ** 2)
    shade = shade_of(hgt, 0.75)
    tone = np.clip(0.18 + 0.5 * dune + 0.25 * (pf(h, 160, 192, seed + 7, 4) - 0.5) + 0.05 * ripple, 0, 1)
    rgb = raster.ramp([(0, (98, 48, 26)), (0.45, (150, 84, 48)), (0.8, (196, 136, 88)), (1, (214, 170, 120))],
                      tone) * shade[..., None]
    srgb, sa = shard_field(h, seed + 4, 26, 2, 5, [(92, 60, 44), (120, 80, 56)], xr=(40, 440))
    rgb, _ = over(rgb, np.ones((h, W)), srgb, sa)
    rgb = world_tint(rgb)
    n = 0.6 * pf(h, 48, 64, seed + 5, 4) + 0.4 * pf(h, 24, 32, seed + 8, 3)
    edge = np.minimum(xx, W - xx)
    off = 12 + 26 * np.clip((n - 0.5) * 2 + 0.5, 0, 1) + 9 * np.sin(yy / h * TAU * 10 + 6 * n)
    m = a84.smooth01((edge - off) / 12.0)
    rgb = rgb + rim(m, 0.45)[..., None] * RIM
    return rgba(rgb, m)


# =========================================================================== pieces

def piece_h(seconds, factor=FACTORS["deep"], speed=SPEED, extra=160):
    span = seconds * speed * factor + SCREEN + extra
    return int(np.ceil(span / 4) * 4)


def luna_earth(seed=SEED + 60):
    """Luna's grey limb across the lower half, Earth's crescent above it in the dark."""
    h = piece_h(31)
    yy, xx = grid(h)
    rgb = np.ones((h, W, 3)) * SPACE + stars(h, W, seed)
    cx, cy, R = 230.0, h * 0.56 + 420, 420.0                             # the limb's arc: top near 56 % height
    d = np.hypot(xx - cx, yy - cy)
    cov = np.clip(R - d + 0.5, 0, 1)
    depth = np.clip((R - d) / 200.0, 0, 1)                                # 0 at the limb .. 1 far below it
    surf = regolith(h, seed + 1, periodic=False)
    surf = surf * (0.62 + 0.38 * depth ** 0.5)[..., None] + (np.exp(-((R - d) / 3.0) ** 2) * 60)[..., None]
    rgb, a = over(rgb, np.ones((h, W)), surf, cov)
    er = 118.0
    ex, ey = 336.0, h * 0.56 - 250
    earth = load_asset(ROOT / "assets" / "backdrop" / "level-07" / "earth.png", (int(2 * er * 1.06), int(2 * er * 1.06)))
    alb = np.zeros((h, W, 3))
    ew = earth.shape[0]
    x0, y0 = int(ex - ew / 2), int(ey - ew / 2)
    alb[y0:y0 + ew, x0:x0 + ew] = earth[..., :3] * 1.5
    side = np.array((-0.86, -0.32, -0.25))
    lit, ecov, enz, lam = sphere(h, W, ex, ey, er, alb, light=side / np.linalg.norm(side), ambient=0.015, power=0.7)
    ed = np.hypot(xx - ex, yy - ey) / er
    atmo = np.exp(-((ed - 1.0) / 0.035) ** 2) * np.clip(-(xx - ex) / er * 0.8 - (yy - ey) / er * 0.4 + 0.25, 0, 1)
    rgb, _ = over(rgb, np.ones((h, W)), lit, ecov * (yy < h * 0.56 - 20))
    rgb = rgb + atmo[..., None] * np.array((110, 160, 255.0)) * 0.9
    return memory_piece(rgb, seed + 2)


def mars_disc(seed=SEED + 70):
    h = piece_h(30)
    yy, xx = grid(h)
    rgb = np.ones((h, W, 3)) * SPACE + stars(h, W, seed)
    cx, cy, r = 250.0, h * 0.5, 172.0
    tex = a84.mars_mem(seed + 1, h) * 0.78
    dx, dy = (xx - cx) / r, (yy - cy) / r
    vy = cy + 22 + 26 * np.sin((xx - cx) / 58.0) + 8 * np.sin((xx - cx) / 17.0)   # Valles Marineris's scar
    scar = np.exp(-((yy - vy) / 5.5) ** 2) * smoothstep(-0.75, -0.45, dx) * (1 - smoothstep(0.35, 0.7, dx))
    tex = tex * (1 - 0.6 * scar)[..., None]
    cap = smoothstep(-0.84, -0.93, dy + 0.06 * np.sin(xx / 21.0))
    tex = tex * (1 - cap[..., None]) + np.array((236, 226, 220.0)) * cap[..., None]
    lit, cov, nz, lam = sphere(h, W, cx, cy, r, tex, ambient=0.05)
    rgb, _ = over(rgb, np.ones((h, W)), lit, cov)
    dd = np.hypot(dx, dy)
    limb = np.exp(-((dd - 1.0) / 0.03) ** 2) * np.clip(lam * 3 + 0.15 * (dd > 1), 0, 1)
    rgb = rgb + limb[..., None] * np.array((230, 150, 110.0)) * 0.55
    pc, ps = blob(h, W, 404.0, cy - 250, 9.0, seed + 3, lump=0.5)                   # Phobos
    rgb, _ = over(rgb, np.ones((h, W)), np.array((92, 80, 72.0)) * ps[..., None], pc)
    return memory_piece(rgb, seed + 2)


def ceres_body(h, w, cx, cy, r, seed):
    """Ceres: grey-brown with craters and its two bright spots, lit from the top left."""
    tex = nf(w, h, 40, seed, 5)
    hgt = tex * 0.5 + craters(h, seed + 1, int(30 * h * w / (480 * 540)), 5, 34, w=w, periodic=False)
    shade = shade_of(jovkit.blur(hgt, 1.0) * 26, 0.8, wrap=False)
    alb = raster.ramp([(0, (52, 48, 44)), (0.5, (98, 90, 80)), (1, (140, 130, 116))],
                      np.clip(0.3 + 0.45 * hgt, 0, 1)) * shade[..., None]
    yy, xx = grid(h, w)
    for sx, sy, sr in ((0.18, 0.05, 0.02), (0.22, 0.08, 0.012)):
        alb = alb + (np.exp(-((xx - cx - sx * r) ** 2 + (yy - cy - sy * r) ** 2) / (sr * r) ** 2) * 120)[..., None]
    return sphere(h, w, cx, cy, r, alb, ambient=0.05)


def station_lights(h, w, seed, cx, cy, r, n, a0, a1, spread=10.0):
    """Station lights on an arc round (cx, cy) at radius r, angles a0..a1 (radians), warm and white."""
    def where(g):
        t = g.uniform(a0, a1)
        rr = r + g.normal(0, spread)
        return cx + np.cos(t) * rr, cy + np.sin(t) * rr
    return lights_field(h, w, seed, n, [(255, 220, 160), (255, 250, 236), (255, 160, 110)], r=(0.7, 1.3),
                        periodic=False, where=where)


def belt_ceres(seed=SEED + 80):
    h = piece_h(29)
    yy, xx = grid(h)
    rgb = np.ones((h, W, 3)) * SPACE + stars(h, W, seed, 0.0007)
    cx, cy, r = 150.0, 250.0, 132.0
    lit, cov, _, _ = ceres_body(h, W, cx, cy, r, seed + 1)
    rgb, _ = over(rgb, np.ones((h, W)), lit, cov)
    rgb = rgb + station_lights(h, W, seed + 2, cx, cy, r * 0.82, 16, 0.2, 1.4, 14)
    dust = np.clip((nf(W, h, 120, seed + 3, 4) - 0.45) * 1.6, 0, 1) * smoothstep(h * 0.25, h * 0.6, yy)
    rgb = rgb + dust[..., None] * np.array((60, 50, 40.0)) * 0.35
    mc, ms = blob(h, W, 320.0, h * 0.74, 230.0, seed + 4, lump=0.35)              # the belt's dark rock
    rtex = nf(W, h, 30, seed + 5, 5) * 0.6 + craters(h, seed + 6, 22, 5, 30, periodic=False) * 0.6
    rsh = shade_of(jovkit.blur(rtex, 1.0) * 24, 0.8, wrap=False)
    rock = raster.ramp([(0, (26, 24, 22)), (0.5, (62, 56, 50)), (1, (96, 86, 74))], np.clip(0.25 + 0.5 * rtex, 0, 1))
    rgb, _ = over(rgb, np.ones((h, W)), rock * (rsh * ms)[..., None], mc)
    srgb, sa = shard_field(h, seed + 7, 9, 6, 18, [(70, 64, 58), (88, 80, 70)], xr=(60, 420))
    sa = sa * (yy < h * 0.62)
    rgb, _ = over(rgb, np.ones((h, W)), srgb, sa)
    return memory_piece(rgb, seed + 8)


def l1_point(seed=SEED + 90):
    h = 904
    yy, xx = grid(h)
    rgb = np.ones((h, W, 3)) * SPACE + stars(h, W, seed)
    er, ex, ey = 150.0, 150.0, 300.0
    earth = load_asset(ROOT / "assets" / "backdrop" / "level-07" / "earth.png", (int(2 * er * 1.06),) * 2)
    alb = np.zeros((h, W, 3))
    ew = earth.shape[0]
    x0, y0 = int(ex - ew / 2), int(ey - ew / 2)
    ys, xs = slice(max(0, y0), y0 + ew), slice(max(0, x0), x0 + ew)
    alb[ys, xs] = earth[max(0, -y0):, max(0, -x0):][:alb[ys, xs].shape[0], :alb[ys, xs].shape[1], :3] * 1.5
    side = np.array((-0.8, -0.45, -0.1))
    lit, ecov, _, _ = sphere(h, W, ex, ey, er, alb, light=side / np.linalg.norm(side), ambient=0.015, power=0.7)
    ed = np.hypot(xx - ex, yy - ey) / er
    atmo = np.exp(-((ed - 1.0) / 0.03) ** 2) * np.clip(-(xx - ex) / er * 0.7 - (yy - ey) / er * 0.5 + 0.2, 0, 1)
    rgb, _ = over(rgb, np.ones((h, W)), lit, ecov)
    rgb = rgb + atmo[..., None] * np.array((110, 160, 255.0)) * 0.9
    mr, mx, my = 44.0, 380.0, 640.0
    moon = load_asset(ROOT / "assets" / "backdrop" / "level-07" / "moon.png", (int(2 * mr * 1.04),) * 2)
    malb = np.zeros((h, W, 3))
    mw = moon.shape[0]
    malb[int(my - mw / 2):int(my - mw / 2) + mw, int(mx - mw / 2):int(mx - mw / 2) + mw] = moon[..., :3] * 1.3
    mlit, mcov, _, _ = sphere(h, W, mx, my, mr, malb, ambient=0.05)
    rgb, _ = over(rgb, np.ones((h, W)), mlit, mcov)

    def ring(g):                                                         # the picket's wreck lights, a broken ring
        t = g.choice([g.uniform(0.2, 1.6), g.uniform(2.3, 3.4), g.uniform(4.1, 5.6)])
        return 262 + np.cos(t) * 118 + g.normal(0, 5), 470 + np.sin(t) * 46 + g.normal(0, 4)
    rgb = rgb + lights_field(h, W, seed + 3, 22, [(255, 200, 150), (255, 110, 80), (240, 240, 255)],
                             r=(0.7, 1.4), periodic=False, where=ring)
    srgb, sa = shard_field(h, seed + 4, 14, 3, 7, [(70, 76, 90), (96, 92, 84)], xr=(140, 390))
    sa = sa * np.exp(-((yy - 470) / 70.0) ** 2)
    rgb, _ = over(rgb, np.ones((h, W)), srgb, sa)
    return memory_piece(rgb, seed + 5)


def ceres_orbit(seed=SEED + 100):
    h = 760
    rgb = np.ones((h, W, 3)) * SPACE + stars(h, W, seed)
    cx, cy, r = 700.0, 380.0, 560.0
    lit, cov, _, _ = ceres_body(h, W, cx, cy, r, seed + 1)
    rgb, _ = over(rgb, np.ones((h, W)), lit, cov)
    rgb = rgb + station_lights(h, W, seed + 2, cx, cy, r * 0.93, 34, 2.75, 3.6, 10)
    rgb = rgb + station_lights(h, W, seed + 3, 360.0, 330.0, 30.0, 14, 0, TAU, 4)
    return memory_piece(rgb, seed + 4)


CORE, CORE_GLOW = 224, 320
RV = np.array(a84.RV, float)                   # the Heart's red-violet (200, 60, 150)


def core_far(seed=SEED + 110):
    h = w = CORE
    yy, xx = grid(h, w)
    cov, sh = blob(h, w, w / 2, h / 2, 78.0, seed, lump=0.35)
    ribs = 0.5 + 0.5 * np.sin(np.arctan2(yy - h / 2, xx - w / 2) * 9 + nf(w, h, 16, seed + 1, 3) * 4)
    col = raster.ramp(TISSUE, np.clip(0.35 + 0.45 * sh * (0.7 + 0.3 * ribs), 0, 1))
    d = np.hypot(xx - w / 2, yy - h / 2)
    col = col + (np.exp(-(d / 30.0) ** 2) * 0.5)[..., None] * RV
    soft = jovkit.blur(cov, 3.0)
    return rgba(col, soft * smoothstep(w / 2, w / 2 - 14, d))


def core_far_glow():
    h = w = CORE_GLOW
    light = np.zeros((h, w, 3))
    jovkit.glow(light, w / 2, h / 2, 70, RV, 0.75)
    jovkit.glow(light, w / 2, h / 2, 24, (255, 140, 210), 0.45)
    yy, xx = grid(h, w)
    d = np.hypot(xx - w / 2, yy - h / 2)
    return additive_rgb(light * smoothstep(w / 2 - 4, w / 2 - 60, d)[..., None])


def memory_mask(seed=SEED + 120):
    h = SCREEN
    yy, xx = grid(h)
    n1 = nf(W, h, 48, seed, 4)
    n2 = nf(W, h, 48, seed + 1, 4)
    form = np.clip(yy / h * 0.82 + (n1 - 0.5) * 0.3 + 0.09, 0, 1)
    side = np.minimum(xx, W - xx) / (W / 2)
    melt = np.clip(side * 0.78 + (n2 - 0.5) * 0.34 + 0.06, 0, 1)
    rgb = np.dstack([form * 255, melt * 255, np.zeros((h, W))])
    return Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")


TILES = {"tissue": ("deep", tissue_tile), "luna-regolith": ("low-air", luna_regolith),
         "l1-debris": ("low-air", l1_debris), "valles-veils": ("low-air", valles_veils),
         "belt-rocks": ("low-air", belt_rocks), "hellas-floor": ("ground", hellas_floor)}
PIECES = {"luna-earth": luna_earth, "mars-disc": mars_disc, "belt-ceres": belt_ceres, "l1-point": l1_point,
          "ceres-orbit": ceres_orbit, "core-far": core_far}
DATA_IMAGES = {"memory-mask": memory_mask}
GLOWS = {"core-far-glow": core_far_glow}
ALL = {**{k: v[1] for k, v in TILES.items()}, **PIECES, **GLOWS, **DATA_IMAGES}


def _make(name):
    return name, ALL[name]()


def build_backdrop():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()
    with ProcessPoolExecutor(max_workers=WORKERS) as pool:
        for name, img in pool.map(_make, list(ALL)):
            artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


# =========================================================================== the clock, the stretches, the proposal

# The paper timeline (README Layout, real seconds at medium): the approach at 100 px/s, gate 1 and gate 2 crawling at
# 20 px/s (their gaps included), gate 3 halted, the unmaking drifting at 40 px/s. Used for the review's screens.
PAPER = [{"end": 31}, {"end": 61}, {"end": 90}, {"end": 154, "speed": 20, "ramp": 2}, {"end": 212, "speed": 20},
         {"end": 270, "speed": 0, "ramp": 2}, {"end": 288, "speed": 40, "ramp": 2}]
PAPER_CLOCK = jovkit.Clock(PAPER, SPEED, FACTORS, SCREEN)
# The memories on the paper timeline: (name, start, end, deep piece, low-air set, ground set); a memory forms over
# 2 s from its start and melts over 2 s to its end.
MEMORIES = [("luna", 0.0, 31.0, "luna-earth", "luna-regolith", None),
            ("mars", 31.5, 61.0, "mars-disc", "valles-veils", None),
            ("ceres", 61.5, 90.0, "belt-ceres", "belt-rocks", None),
            ("l1", 91.0, 153.0, "l1-point", "l1-debris", None),
            ("hellas", 154.0, 211.0, None, None, "hellas-floor"),
            ("orbit", 212.0, 272.0, "ceres-orbit", None, None),
            ("unmaking", 272.0, 290.0, "core-far", None, None)]

# The data's sections as proposed (clock time: an arena's clock stands while its echo lives; step G2 owns them).
PROPOSED_SECTIONS = [
    {"name": "Luna remembered", "end": 31, "atmosphere": "clear", "tiles": ["tissue", "luna-regolith"]},
    {"name": "Mars remembered", "end": 61, "atmosphere": "clear", "tiles": ["tissue", "valles-veils"]},
    {"name": "Ceres remembered", "end": 90, "atmosphere": "clear", "tiles": ["tissue", "belt-rocks"]},
    {"name": "Echo of the Carrier", "end": 98, "speed": 20, "ramp": 2, "arena": True, "atmosphere": "clear",
     "tiles": ["tissue", "l1-debris"]},
    {"name": "First gap", "end": 101, "speed": 20, "atmosphere": "clear", "tiles": ["tissue"]},
    {"name": "Echo of the Colossus", "end": 106, "speed": 20, "arena": True, "atmosphere": "clear",
     "tiles": ["tissue", "hellas-floor"]},
    {"name": "Second gap", "end": 109, "speed": 0, "ramp": 2, "atmosphere": "clear", "tiles": ["tissue"]},
    {"name": "Echo of the Sovereign", "end": 117, "speed": 0, "arena": True, "atmosphere": "clear",
     "tiles": ["tissue"]},
    {"name": "The unmaking", "end": 135, "speed": 40, "ramp": 2, "atmosphere": "clear", "tiles": ["tissue"]},
]
SECTIONS = DATA["sections"] if DATA and DATA.get("sections") else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
# Each deep piece's stretch, by a word of its section's name (the data's names may differ: re-run --proposal).
PIECE_SECTION = {"luna-earth": "luna", "mars-disc": "mars", "belt-ceres": "ceres", "l1-point": "carrier",
                 "ceres-orbit": "sovereign", "core-far": "unmaking"}
PIECE_NOTES = {"luna-earth": "section 1: Earth's crescent over Luna's grey limb",
               "mars-disc": "section 2: Mars's rust disc, Valles Marineris, the polar cap, Phobos",
               "belt-ceres": "section 3: the belt's dark rock, Ceres far off with its station lights",
               "l1-point": "gate 1 (the Carrier): the L1 point, Earth, the Moon, the picket's wreck lights",
               "ceres-orbit": "gate 3 (the Sovereign, the halt): Ceres's curve and the station's lights",
               "core-far": "the unmaking: the hive's core far ahead (its -glow added by the game)"}


def data_clock():
    """A clock over the data's sections as LevelData.scrollAt runs them (an arena section at its own speed)."""
    secs = [{"end": s["end"], "speed": s.get("speed"), "ramp": s.get("ramp", 0)} for s in SECTIONS]
    return jovkit.Clock(secs, BASE_SPEED, FACTORS, SCREEN)


def placements():
    clock = data_clock()
    out = []
    for piece, word in PIECE_SECTION.items():
        i = next((k for k, s in enumerate(SECTIONS) if word in s["name"].lower()), None)
        if i is None:
            continue
        a, b = clock.starts[i], float(SECTIONS[i]["end"])
        out.append({"piece": piece, "t": round((a + b) / 2, 3), "x": 240, "note": PIECE_NOTES[piece]})
    return out


def sizes():
    return {p.stem: Image.open(p).size for p in OUT.glob("*.png")}


def fmt(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, str) and any(c in v for c in "':,"):
        return '"' + v + '"'
    return str(v)


def proposal_text():
    sz = sizes()
    src = "the level's data.yaml" if DATA and DATA.get("sections") else "this proposal's own sections"
    lines = [
        "backdrop:",
        f"  # Presentation only: Level 49's memory (tools/art/act7_memory.py, step ART-G; sections from {src}).",
        "  # The tissue (deep) is the one opaque layer, the whole level. Each memory = its stretch's deep piece + its",
        "  # low-air tile set (+ gate 2's ground tile set), drawn over the tissue under ONE visibility mask (step G4,",
        "  # MemoryLooks, m11g-gaps.md #14): it forms from the top edge down over 2 s at its stretch's start and melts",
        "  # (sides first) over 2 s at its end, read from memory-mask.png (a data image, not a region to draw: R = the",
        "  # forming threshold, G = the melting one; visibility = smoothstep(R - 0.08, R + 0.08, p * 1.16 - 0.08) while",
        "  # forming, 1 - smoothstep(G - 0.08, G + 0.08, q * 1.16 - 0.08) while melting, p and q 0 -> 1 over 2 s): no",
        "  # flash, the luminance one-way. MemoryLooks draws a memory's tile sets over the WHOLE screen while it shows",
        "  # (not from their seam: gate 2's floor must lie under the Colossus at once) and keeps a melting memory's sets",
        "  # until its melt ends, so a section's tile switch always happens under the mask and no seam shows. The",
        "  # approach's memories change on the clock (31, 61, 90); the gates' on FIGHT_NEXT (the warning",
        "  # forms the gate's memory) and ECHO_DISSOLVE (it melts). Every tile set wraps in y: a crawl of any length is",
        "  # covered. The gates' pieces are placed by the clock; a crawl moves the ground on while the clock stands",
        "  # (BackdropTiles' crawl lays out the next section's pieces from the actual ground: G4 extends it to every",
        "  # gate). Gate 3's station is the Iron Sovereign's own anchored set piece (no ground tile under it). The",
        "  # echoes, their units and the remembered units get the echo tint in the game (EchoTint, never a bullet);",
        "  # the backdrop is tinted in its pixels.",
        "  scroll_factors: {deep: 0.12, far: 0.35, ground: 1, low-air: 1.3, high-air: 2.0}   # far, high-air unused",
        "  ramp: 2",
        "  haze_colour: 1a0c22",
        "  atmosphere:",
        "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
        "  tile_sets:"]
    for name, (layer, _) in TILES.items():
        lines.append(f"    {name}: {{layer: {layer}, height: {sz[name][1]}}}")
    lines.append("  pieces:")
    for name in PIECES:
        lines.append(f"    {name}: {{layer: deep, size: [{sz[name][0]}, {sz[name][1]}]}}")
    lines.append("  placed:   # each at its stretch's middle (the piece's centre at the screen's middle)")
    for p in placements():
        lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {p['x']}}}")
    lines.append("sections:   # proposed (step G2 owns them; the gates' times are clock times: the clock stands while "
                 "an echo lives)")
    for sec in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items() if k != "notes") + "}")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== the tint (the reference for G4)

def edge_px(sp):
    return float(np.clip(0.08 * min(sp.size), 1.5, 10.0))


def echo_tint(sp, phase=0.0):
    """A.8 a (a84.spectral_sprite, v 'a'): the colours desaturated 40 % toward violet-white of their own brightness, a
    step brighter (x 0.74 + violet-white x 0.26), the alpha eroded smoothly to 0 inside the canvas over the edge
    (min(10, max(1.5, 8 % of the sprite's smaller side)) px; Gaussian sigma = edge / 2.5, smoothstep((a - 0.3) / 0.55)),
    and the shimmer: the colour x (1 + 0.06 sin(2 pi 0.35 t))."""
    a = np.array(sp.convert("RGBA")).astype(np.float64)
    rgb = a84.spectral(a[..., :3] / 255.0, TINT_K) * 255 * (1 - UNIT_LIFT) + VW * UNIT_LIFT
    rgb = rgb * (1 + SHIMMER_AMP * np.sin(phase))
    al = a[..., 3] / 255.0
    pad = int(np.ceil(edge_px(sp)))
    inner = jovkit.blur(np.pad(al, pad), edge_px(sp) / 2.5)[pad:-pad, pad:-pad]
    fall = a84.smooth01((inner - 0.3) / 0.55)
    return rgba(np.clip(rgb, 0, 255), al * fall)


def frame(name, i=0):
    for cand in (f"{name}_{i}", name):
        p = ROOT / "assets" / "sprites" / f"{cand}.png"
        if p.exists():
            return Image.open(p).convert("RGBA")
    return None


def lum(arr, alpha=None):
    l_ = arr[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    if alpha is None:
        return float(l_.mean())
    return float((l_ * alpha).sum() / max(alpha.sum(), 1e-6))


# =========================================================================== composition (the review)

_CACHE = {}


def img(name):
    if name not in _CACHE:
        p = OUT / f"{name}.png"
        _CACHE[name] = np.array(Image.open(p).convert("RGBA")).astype(np.float64) if p.exists() else None
    return _CACHE[name]


def tile_rows(name, offset):
    arr = img(name)
    hh = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[hh - 1 - (rows % hh)]


def visibility(mem, t):
    """The memory's mask at time t: forming over 2 s from its start, melting over 2 s to its end."""
    _, a, b, *_ = mem
    mk = img("memory-mask")
    R, G = mk[..., 0] / 255.0, mk[..., 1] / 255.0
    p = np.clip((t - a) / 2.0, 0, 1) if a > 0 else 1.0
    q = np.clip((t - (b - 2.0)) / 2.0, 0, 1)
    v = smoothstep(R - 0.08, R + 0.08, p * 1.16 - 0.08) if p < 1 else np.ones((SCREEN, W))
    if q > 0:
        v = v * (1 - smoothstep(G - 0.08, G + 0.08, q * 1.16 - 0.08))
    return v


def piece_at(name, mem, t, factor=FACTORS["deep"]):
    """The piece centred on the screen's middle at its memory's middle (paper clock), moved by the layer since."""
    _, a, b, *_ = mem
    tm = (a + b) / 2
    y = SCREEN / 2 + (PAPER_CLOCK.scroll(t) - PAPER_CLOCK.scroll(tm)) * factor
    return img(name), 240.0, y


def put(base_rgb, base_a, arr, x, y, vis=None, add=False):
    if arr is None:
        return base_rgb, base_a
    h, w = arr.shape[:2]
    x0, y0 = int(round(x - w / 2)), int(round(y - h / 2))
    x1, y1 = max(0, x0), max(0, y0)
    x2, y2 = min(W, x0 + w), min(SCREEN, y0 + h)
    if x2 <= x1 or y2 <= y1:
        return base_rgb, base_a
    src = arr[y1 - y0:y2 - y0, x1 - x0:x2 - x0]
    k = src[..., 3] / 255.0 * (vis[y1:y2, x1:x2] if vis is not None else 1.0)
    if add:
        base_rgb[y1:y2, x1:x2] += src[..., :3] * (vis[y1:y2, x1:x2, None] if vis is not None else 1.0)
        return base_rgb, base_a
    region = base_rgb[y1:y2, x1:x2]
    base_rgb[y1:y2, x1:x2] = region * (1 - k[..., None]) + src[..., :3] * k[..., None]
    return base_rgb, base_a


def compose(t, units=True):
    s = PAPER_CLOCK.scroll(t)
    rgb = tile_rows("tissue", s * FACTORS["deep"])[..., :3].copy()
    a = np.ones((SCREEN, W))
    for mem in MEMORIES:
        if not (mem[1] - 0.01 <= t <= mem[2] + 0.01):
            continue
        vis = visibility(mem, t)
        _, _, _, piece, low, ground = mem
        if piece:
            arr, x, y = piece_at(piece, mem, t)
            rgb, a = put(rgb, a, arr, x, y, vis)
            if piece == "core-far":
                rgb, a = put(rgb, a, img("core-far-glow"), x, y, vis, add=True)
        if ground:
            g = tile_rows(ground, s)
            k = g[..., 3] / 255.0 * vis
            rgb = rgb * (1 - k[..., None]) + g[..., :3] * k[..., None]
        if low:
            g = tile_rows(low, s * FACTORS["low-air"])
            k = g[..., 3] / 255.0 * vis
            rgb = rgb * (1 - k[..., None]) + g[..., :3] * k[..., None]
    out = Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    if units:
        out = stand_ins(out, t)
    return out


ACT_UNITS = {"luna": [("skitter", 2), ("needler", 0), ("stinger", 0), ("spore-bomber", 0)],
             "mars": [("dust-kite", 0), ("ghost-drone", 0), ("mote-swarm", 0), ("choir-herald", 0)],
             "ceres": [("dart-drone", 0), ("talon", 2), ("gilded-gunship", 0), ("hornet", 0)]}


def memory_at(t):
    for mem in MEMORIES:
        if mem[1] <= t <= mem[2]:
            return mem[0]
    return None


def stand_ins(out, t):
    mem = memory_at(t)
    units = ACT_UNITS.get({"l1": "luna", "hellas": "mars", "orbit": "ceres"}.get(mem, mem), [])
    if mem in ("luna", "mars", "ceres"):
        for k, (name, i) in enumerate(units):
            sp = frame(name, i)
            if sp:
                l23kit.paste_c(out, echo_tint(sp, phase=TAU * SHIMMER_HZ * t), 110 + k * 88, 120 + (k % 2) * 60)
    bul = [frame("orb"), frame("orb-medium"), frame("needle-violet")]
    for k in range(6):
        b = bul[k % 3]
        if b:
            l23kit.paste_c(out, b, 160 + k * 30, 260 + k * 14)
    ship = frame("ship", 2)
    if ship:
        l23kit.paste_c(out, ship, 240, 470)
    return out


SCREENS = (6.0, 22.0, 46.0, 76.0, 95.0, 128.0, 156.0, 185.0, 240.0, 281.0)
LABELS = {6.0: "LUNA", 22.0: "LUNA, EARTH OVER THE LIMB", 46.0: "MARS", 76.0: "CERES", 95.0: "GATE 1 FORMS",
          128.0: "GATE 1 L1 POINT", 156.0: "HELLAS FORMS", 185.0: "GATE 2 HELLAS FLOOR", 240.0: "GATE 3 CERES ORBIT",
          281.0: "THE UNMAKING"}


def review_backdrop():
    _CACHE.clear()
    sw, sh = 192, 216
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * (sh + 18) + 20 + 300, "LEVEL 49 BACKDROP (FINAL R94): THE CHOIR'S MEMORY",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, t in enumerate(SCREENS):
        x, y = 12 + (i % 5) * (sw + 6), 38 + (i // 5) * (sh + 18)
        raster.draw_text(sheet, x, y, f"T={t:g} {LABELS[t]}", raster.LABEL)
        sheet.alpha_composite(compose(t).resize((sw, sh), Image.LANCZOS), (x, y + 11))
    y = 38 + 2 * (sh + 18) + 6
    raster.draw_text(sheet, 12, y, "THE TILE SETS OVER THE TISSUE (TOP 400 ROWS) AND THE PIECES (1/4), THE MASK",
                     raster.LABEL)
    x = 12
    tis = img("tissue")
    for name in TILES:
        arr = img(name)
        part = arr[:400].copy()
        base = tis[:400]
        k = part[..., 3:] / 255.0
        comp = base[..., :3] * (1 - k) + part[..., :3] * k
        im = Image.fromarray(np.clip(comp, 0, 255).astype(np.uint8), "RGB").resize((96, 80), Image.LANCZOS)
        sheet.paste(im, (x, y + 14))
        raster.draw_text(sheet, x, y + 98, name.upper()[:14], raster.LABEL_DIM)
        x += 102
    x, yy = 12, y + 112
    for name in list(PIECES) + ["core-far-glow", "memory-mask"]:
        p = OUT / f"{name}.png"
        im = Image.open(p).convert("RGBA")
        bg = Image.new("RGBA", im.size, (20, 10, 26, 255))
        bg.alpha_composite(im)
        sm = bg.resize((max(1, im.width // 6), max(1, im.height // 6)), Image.LANCZOS)
        if x + sm.width > w - 8:
            break
        sheet.alpha_composite(sm, (x, yy))
        x += sm.width + 6
    frames = [compose(28.5 + 0.25 * k).resize((240, 270), Image.LANCZOS) for k in range(24)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


def world_crop(mem, t):
    return compose(t, units=False)


def review_tint():
    """The tint's reference sheet: per remembered act its units over its world at 1:1, original | tinted, the
    measured step; the echoes (scaled); the shimmer; the constants."""
    _CACHE.clear()
    w = 1000
    sheet = raster.sheet(w, 1150, "THE ECHO TINT (FINAL R94): REFERENCE FOR THE GAME'S SHADER",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    consts = ["A.8 A: RGB' = SPECTRAL(RGB, 0.4) X 0.74 + VIOLET-WHITE (226,206,255) X 0.26;",
              "SPECTRAL(C, K) = C (1 - K) + LUM(C) X VIOLET-WHITE/255 X 1.08 X K, LUM = 0.30 R + 0.59 G + 0.11 B",
              "ALPHA' = ALPHA X SMOOTHSTEP((BLUR(ALPHA, EDGE/2.5) - 0.3) / 0.55), EDGE = CLAMP(8 % OF THE SMALLER SIDE,"
              " 1.5, 10) PX",
              f"SHIMMER: RGB' X (1 + {SHIMMER_AMP:g} SIN(2 PI {SHIMMER_HZ:g} T)) (<= 0.5 HZ); BULLETS, BEAMS AND THEIR"
              " GLOWS NEVER TINTED",
              f"THE WORLDS (BAKED): SPECTRAL(RGB, 0.4) X {WORLD_AMB:g}, THEIR SIDES DISSOLVING INTO THE TISSUE"]
    for i, c in enumerate(consts):
        raster.draw_text(sheet, 12, 36 + 11 * i, c, raster.LABEL)
    y = 100
    report = []
    for mem, t in (("luna", 20.0), ("mars", 46.0), ("ceres", 76.0)):
        world = np.array(world_crop(mem, t)).astype(np.float64)
        raster.draw_text(sheet, 12, y, f"{mem.upper()} REMEMBERED (T={t:g}): EACH UNIT ORIGINAL | TINTED, AT 1:1 OVER"
                         " ITS WORLD; UNTINTED BULLETS; LUMINANCE UNIT / UNDER IT / WORLD P90", raster.LABEL)
        plate = Image.fromarray(np.clip(world[100:260, :, :3], 0, 255).astype(np.uint8)).convert("RGBA")
        x = 12
        for name, i in ACT_UNITS[mem]:
            sp = frame(name, i)
            if sp is None:
                continue
            cell = plate.crop((60, 0, 60 + 220, 160)).copy()
            l23kit.paste_c(cell, sp, 60, 70)
            tinted = echo_tint(sp)
            l23kit.paste_c(cell, tinted, 160, 70)
            for k, b in enumerate(("orb", "needle-violet")):
                bb = frame(b)
                if bb:
                    l23kit.paste_c(cell, bb, 120 + 30 * k, 140)
            ta = np.array(tinted).astype(np.float64)
            al = ta[..., 3] / 255.0
            under = np.array(plate.crop((160 - sp.width // 2 + 60, 70 - sp.height // 2,
                                         160 - sp.width // 2 + 60 + sp.width, 70 - sp.height // 2 + sp.height)))
            lu, lw = lum(ta, al), lum(under.astype(np.float64))
            p90 = float(np.percentile(np.array(plate).astype(np.float64)[..., :3] @ np.array((0.299, 0.587, 0.114)), 90))
            report.append((mem, name, lu, lw, p90))
            sheet.alpha_composite(cell, (x, y + 12))
            raster.draw_text(sheet, x, y + 176, f"{name.upper()[:14]} {lu:.0f} / {lw:.0f} / P90 {p90:.0f}", raster.LABEL_DIM)
            x += 242
        y += 196
    raster.draw_text(sheet, 12, y, "THE ECHOES (1/2 SIZE): ORIGINAL | TINTED, OVER THEIR WORLDS", raster.LABEL)
    x = 12
    for name, i, mem, t in (("brood-carrier-hull", 8, "l1", 128.0), ("dust-colossus-mouth", 0, "hellas", 185.0),
                            ("iron-sovereign-base", 0, "orbit", 240.0)):
        sp = frame(name, i)
        if sp is None:
            continue
        if name == "iron-sovereign-base":
            for part in ("iron-sovereign-arc-outer", "iron-sovereign-core"):
                pp = frame(part, 0)
                if pp:
                    l23kit.paste_c(sp, pp, sp.width / 2, sp.height / 2)
        world = compose(t, units=False)
        cell = world.crop((0, 0, 480, 400)).copy()
        half = sp.resize((sp.width // 2, sp.height // 2), Image.LANCZOS)
        th = echo_tint(sp)
        th = th.resize((th.width // 2, th.height // 2), Image.LANCZOS)
        l23kit.paste_c(cell, half, 120, 200)
        l23kit.paste_c(cell, th, 360, 200)
        cell = cell.resize((320, 267), Image.LANCZOS)
        sheet.alpha_composite(cell, (x, y + 12))
        raster.draw_text(sheet, x, y + 282, name.upper(), raster.LABEL_DIM)
        x += 328
    y += 300
    raster.draw_text(sheet, 12, y, f"THE SHIMMER ({SHIMMER_HZ:g} HZ, +-{SHIMMER_AMP * 100:.0f} %): A CHOIR HERALD AT"
                     " 0, 1/4, 1/2 AND 3/4 OF A PERIOD (1X)", raster.LABEL)
    herald = frame("choir-herald", 0)
    if herald:
        for k in range(4):
            cell = Image.new("RGBA", (herald.width + 16, herald.height + 16), (20, 10, 26, 255))
            l23kit.paste_c(cell, echo_tint(herald, phase=TAU * k / 4), cell.width / 2, cell.height / 2)
            sheet.alpha_composite(cell, (12 + k * (cell.width + 10), y + 12))
    path = CONCEPT / f"echo-tint-final-{ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")
    worst = min(lu - p for _, _, lu, _, p in report) if report else 0
    for mem, name, lu, lw, p in report:
        print(f"  {mem:6s} {name:16s} tinted unit {lu:5.1f}, under it {lw:5.1f}, the world's 90th percentile {p:5.1f}")
    print(f"  the smallest step over the world's 90th percentile: {worst:+.1f} luminance")


# =========================================================================== the supply pod

POD, POD_EXT = 36, 36 / 20.0
TILT = np.radians(30.0)
P_BODY, P_YEL, P_BLK, P_FRAME, P_RIB = range(5)


def pod_model():
    """A Coalition supply pod of Act 1's convoys (L01's cargo container's livery): a rust-brown container, its ends
    hazard-striped yellow and black, grey frame bands, ribbed sides; long axis +Y, tilted 30 degrees about x."""
    def scene(p):
        c, s = np.cos(TILT), np.sin(TILT)
        q = np.stack([p[:, 0], p[:, 1] * c - p[:, 2] * s, p[:, 1] * s + p[:, 2] * c], axis=-1)
        x, y, z = q[:, 0], q[:, 1], q[:, 2]
        body = sd_box(q, (0, 0, 0), (0.3, 0.56, 0.26), 0.06)
        frame_ = sd_box(q, (0, 0, 0), (0.32, 0.6, 0.28), 0.05)
        frame_ = np.maximum(frame_, np.abs(np.abs(y) - 0.42) - 0.035)
        lug = sd_sphere(np.stack([np.abs(x), y, z], axis=-1), (0.18, 0.6, 0.18), 0.05)
        d, m = union((body, P_BODY), (frame_, P_FRAME), k=0.0)
        d, m = union((d, m), (lug, P_FRAME), k=0.01)
        ends = (m == P_BODY) & (np.abs(y) > 0.44)
        stripe = np.floor((x + z) * 3.0 + y * 2.0) % 2 == 0
        m = np.where(ends & stripe, P_YEL, np.where(ends, P_BLK, m))
        rib = (m == P_BODY) & (np.sin(y * 34.0) > 0.55)
        m = np.where(rib, P_RIB, m)
        return d, m
    mats = [Material((0.42, 0.20, 0.11), metal=0.2, shininess=30, spec=0.3),        # rust-brown body
            Material((0.86, 0.66, 0.10), metal=0.1, shininess=40, spec=0.4),        # hazard yellow
            Material((0.06, 0.06, 0.06), metal=0.1, shininess=30, spec=0.3),        # hazard black
            Material((0.46, 0.46, 0.48), metal=0.7, shininess=70, spec=0.7),        # grey frame bands, lugs
            Material((0.30, 0.14, 0.08), metal=0.2, shininess=20, spec=0.2)]        # the ribs' shade
    return scene, mats


def _pod(k):
    scene, mats = pod_model()
    return bu.render(*bu.turned(scene, mats, k, 16), POD, POD_EXT)


def dissolve_pod(frames, seed=SEED + 130):
    """The pod's upper right dissolving: 2 px blobs of it gone toward that corner, a few violet motes beyond (1-bit
    alpha, no 1 px checker)."""
    out = []
    n = nf(POD // 2, POD // 2, 3, seed, 3)
    n = np.kron(n, np.ones((2, 2)))
    yy, xx = grid(POD, POD)
    ramp_ = np.clip(((xx - POD / 2) - (yy - POD / 2)) / POD + 0.5, 0, 1)
    for f, im in enumerate(frames):
        a = np.array(im.convert("RGBA"))
        gone = n < (ramp_ * 1.3 - 0.55)
        a[..., 3] = np.where(gone, 0, a[..., 3])
        rng = np.random.default_rng(seed + f)
        for _ in range(4):
            x = int(np.clip(POD / 2 + rng.uniform(4, 15), 0, POD - 2))
            y = int(np.clip(POD / 2 - rng.uniform(4, 15) - f * 0.3 % 4, 0, POD - 2))
            a[y:y + 2, x:x + 2] = (226, 206, 255, 255)
        out.append(Image.fromarray(a, "RGBA"))
    return out


def pod_break(n=8, w=64, seed=SEED + 140):
    """Broken open: its chips (rust, hazard yellow, grey) flung out turning violet-white as they dissolve, a soft violet
    puff; no flash (storm_layer_units.pod_break's rig in the memory's colours)."""
    rng = np.random.default_rng(seed)
    chips = [(rng.uniform(0, TAU), rng.uniform(40, 95), rng.uniform(2.2, 4.6), rng.uniform(0, TAU),
              rng.choice([(110, 52, 28), (220, 168, 30), (118, 118, 122)])) for _ in range(14)]
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        im = Image.new("RGBA", (w, w), (0, 0, 0, 0))
        yy, xx = grid(w, w)
        r = 6 + 18 * u ** 0.7
        puff = np.exp(-((xx - w / 2) ** 2 + (yy - w / 2) ** 2) / (r * r)) * 0.55 * (1 - u) ** 1.1
        tex = nf(w, w, 8, seed + f, 3)
        puff = np.clip(puff * (0.7 + 0.6 * tex), 0, 1) * smoothstep(w / 2, w / 2 - 6, np.hypot(xx - w / 2, yy - w / 2))
        im.alpha_composite(rgba(np.ones((w, w, 3)) * np.array((150, 110, 200.0)), puff))
        d = ImageDraw.Draw(im)
        for ang, speed, size, spin, base in chips:
            dist = speed * 0.3 * (1 - (1 - u) ** 2)
            x, y = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist
            a_ = spin + u * 6
            pts = [(x + np.cos(a_ + k * 2.1) * size, y + np.sin(a_ + k * 2.1) * size * 0.7) for k in range(3)]
            lit = 0.55 + 0.45 * np.cos(a_ + 0.8)
            col = np.array(base, float) * (0.6 + 0.6 * lit)
            col = col * (1 - u) + VW * u                                    # the memory letting go of it
            if u < 0.88 - 0.2 * (size < 3):
                d.polygon(pts, fill=tuple(int(c) for c in np.clip(col, 0, 255)) + (255,))
        frames.append(im)
    return frames


def build_pod():
    with ProcessPoolExecutor(max_workers=WORKERS) as pool:
        res = list(pool.map(_pod, range(16)))
    frames = artkit.quantize_set(dissolve_pod(res), 32)
    bu.write("debris-supply-pod", frames, SOURCE)
    bu.write("debris-supply-pod-break", pod_break(), SOURCE)


def review_pod():
    _CACHE.clear()
    pod, brk = l23kit.load("debris-supply-pod"), l23kit.load("debris-supply-pod-break")
    w = 1000
    sheet = raster.sheet(w, 520, "SUPPLY POD (FINAL R94): THE REMEMBERED COALITION POD", f"PRODUCTION ART, "
                         f"{BATCH.upper()} - {ROUND.upper()}")
    raster.draw_text(sheet, 12, 36, "THE 16 TUMBLE FRAMES (3X) IN ITS OWN COLOURS, THEN IN THE ECHO TINT (THE GAME'S "
                     "SHADER, G4); THE BREAK (8 FRAMES, 2X)", raster.LABEL)
    for row, tint in enumerate((False, True)):
        for k, fr in enumerate(pod):
            cell = Image.new("RGBA", (40, 40), (20, 10, 26, 255))
            l23kit.paste_c(cell, echo_tint(fr) if tint else fr, 20, 20)
            sheet.alpha_composite(cell.resize((60, 60), Image.NEAREST), (12 + k * 61, 50 + row * 66))
    for k, fr in enumerate(brk):
        cell = Image.new("RGBA", (64, 64), (20, 10, 26, 255))
        cell.alpha_composite(fr)
        sheet.alpha_composite(cell.resize((112, 112), Image.NEAREST), (12 + k * 120, 190))
    raster.draw_text(sheet, 12, 312, "IN PLACE AT 1:1 (T=24, X=380 OVER LUNA REMEMBERED), TINTED", raster.LABEL)
    world = compose(24.0, units=False)
    l23kit.paste_c(world, echo_tint(pod[3]), 380, 200)
    sheet.alpha_composite(world.crop((240, 100, 480, 300)), (12, 326))
    gif = []
    for k in range(32):
        cell = compose(24.0, units=False).crop((300, 140, 460, 260))
        if k < 24:
            l23kit.paste_c(cell, echo_tint(pod[(k // 2) % 16], phase=TAU * SHIMMER_HZ * k / 8), 80, 60 + k * 0.3)
        else:
            l23kit.paste_c(cell, brk[k - 24], 80, 67)
        gif.append(cell.resize((320, 240), Image.NEAREST))
    artkit.save_review(sheet, gif, CONCEPT, "supply-pod", fps=8)


# =========================================================================== check

def straight(im, tile):
    rs, _, cs, _ = cit.straight_scan(im, wrap=tile)
    return max(rs, cs)


def check():
    bad = 0
    worst = (0, "")
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        arr = np.array(im.convert("RGBA"))
        v, c = checker_share(im)
        share = c / max(v, 1)
        ok, msg = im.text.get(artkit.SOURCE_KEY) == SOURCE and share < 0.05, []
        stem = p.stem
        if stem in TILES:
            ws = max(wrap_score(arr), wrap_score_alpha(arr))
            ok &= ws <= 1.5
            msg.append(f"y wrap {ws:.2f}")
        if stem in GLOWS:
            lit = max(arr[0, :, :3].max(), arr[-1, :, :3].max(), arr[:, 0, :3].max(), arr[:, -1, :3].max())
            levels = len(np.unique(arr[..., :3].max(axis=-1)))
            ok &= lit == 0 and levels >= 32
            msg.append(f"edge {lit}, {levels} light levels")
        elif stem not in DATA_IMAGES:
            edge = max(arr[0, :, 3].max(), arr[-1, :, 3].max()) if stem not in TILES else 0
            side = max(arr[:, 0, 3].max(), arr[:, -1, 3].max()) if stem != "tissue" else 0
            ok &= edge == 0 and side == 0
            run = straight(im, stem in TILES)
            lim = 60 if stem in TILES else 120
            ok &= run < lim
            worst = max(worst, (run, stem))
            msg.append(f"edges {edge}/{side}, longest straight run {run} px (limit {lim})")
            levels = len(np.unique(arr[..., 3]))
            msg.append(f"{levels} alpha levels")
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {p.relative_to(ROOT)}: {im.size}, checker {share * 100:.1f} %, "
              f"{', '.join(msg)}")
    for name, n, size in (("debris-supply-pod", 16, (POD, POD)), ("debris-supply-pod-break", 8, (64, 64))):
        try:
            frames = l23kit.load(name)
        except FileNotFoundError:
            frames = []
        where = bu.where(name)
        files = sorted((ROOT / where).glob(f"{name}_*.png"))
        files = [f for f in files if f.stem.rsplit("_", 1)[-1].isdigit() and f.stem.rsplit("_", 1)[0] == name]
        src = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        run = max((straight(fr, False) for fr in frames), default=0)
        ok = len(frames) == n and all(fr.size == size for fr in frames) and src and share < 0.05 and run < 30
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)}/{n} frames, checker {share * 100:.1f} %, "
              f"longest straight run {run} px")
    print(f"straight-line scan (backdrop): longest {worst[0]} px ({worst[1]})")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if not a.startswith("--")] or ["backdrop", "pod"]
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    if "--review" not in args:
        if "backdrop" in subjects:
            build_backdrop()
            write_proposal()
        if "pod" in subjects:
            build_pod()
    if "backdrop" in subjects:
        review_backdrop()
        review_tint()
    if "pod" in subjects:
        review_pod()
