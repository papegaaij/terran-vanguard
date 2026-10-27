#!/usr/bin/env python3
"""Concept round 60 - the new Act 5 "The Belt" units, a/b proposals (step ART-P-units).

Each subject gets a PNG sheet (the source render, the sprites at play size with their states, the
mechanic's key frames, the readability check over the darkest backdrop of its first level, a frame
from the play field at 1x) and a GIF of it in the 480x540 play field under the belt's light, with the
player for scale. Models are built in this file on the frozen ``render`` package (sdf, enemy_rigs,
enemy_models, archetype_models), like enemies_r48.py.

The Ascendancy look is the gap analysis's Q11 a (pending the user's review in round 60): gloss black
hulls (the L28 cruisers' black, ``belt_mats``), gold trim on at least 20 % of the silhouette (each
sheet prints the measured share), a strong top-left rim light (gold, 2 px on variant-a units) with
the red far-side rim, and red running lights. The belt backdrops are kept a step lighter than Act 4's
(a sunlit dust band in space, warm sunlit rock, cool-white station hulls). Vrell units stay insectoid
in their role colours. Every enemy homing missile is the art direction's homing diamond (the a/b
look of ``homing-diamond``; each unit sheet draws the a diamond with variant a, b with b).

Units whose look is chosen (Talon and Gilded Gunship r04, Buzzsaw Drone and Rail Serpent r05) keep
their model: a and b differ in how the livery and the new parts sit on it (a: gold trim and a strong
rim, b: gold panels). Rail Bunker and Strider propose only what changed (the rail line; the topple).

Outputs (design/enemies/<layer>/<unit>/concept/, each <unit>-r60-{a,b}.{png,gif}):
  air/talon            V-wing behind a gold-striped leader, pair shots, the hex shield; kill the
                       leader and the wing scatters (L29 Ceres Hub)
  air/gilded-gunship   frontal plate (x0.5 sparks), white-gold engines (x2), ring of 12 with the gold
                       ring-lamp tell, Dart streams from the belly bays (L30 shipping lane)
  air/hornet           hovering missile boat, gold hatch tell, salvos of 4 homing diamonds (L31)
  air/buzzsaw-drone    chase, 0.6 s spin-up tell (rim flickers red), locked dash, ricochets off the
                       edges and a big rock (L31)
  air/rail-serpent     10-car train from the side (edge-warned), turret cars, the machine split (L30)
  air/dart-drone       tiny streams launched from a Gunship and from the top (L30)
  space/asteroid-mite  Vrell riders on a small rock, leap tell and dive; a shot rock frees them (L29)
  space/minelayer      rear entry, a line of 3 x 4 mines with one gap, arming, blink and ring, a shot
                       mine's chain (L30)
  space/shard-drone    a line of four with energy links (flicker tell), a killed drone breaks its
                       links; a turning circle of four (L31)
  space/void-leech     black-violet leeches from behind, latch and drain, the shake-off; a Talon
                       with a leech drifts (L33 Leech Field)
  ground/sam-nest      a dormant nest wakes (white -> red), rack turns, salvos of 2 diamonds (L29)
  ground/sentinel-tower  arc sweeps and the full turn with their crimson telegraphs (L32 galleries)
  ground/strider       the chosen r05 walker: knee glow, pod salvo, stagger + fall line, topple
                       (a rigid fall / b buckle and crumple), the crush of a Rail Bunker (L32)
  ground/rail-bunker   the chosen r04 bunker: the rail line (a solid brightening / b dashed with
                       running chevrons), the shot (L33 asteroid surface)
  ground/warden-tank   the chosen r05 tank in Ascendancy livery on derelict hulls (L34)
  design/enemies/concept/homing-diamond-r60-{a,b}  the homing diamond sheet and GIF

Run: python3 tools/concept/enemies_r60.py [unit ...] [a|b]   (no args = everything)
"""
import sys
from dataclasses import replace
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import enemies_r04 as e4  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import enemies_r05b as r5b  # noqa: E402
import enemies_r06 as e6  # noqa: E402
from render import archetype_models as am  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import (A_ENGINE, A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED,  # noqa: E402
                                 V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SAC, V_SEAM, hx)
from render.sdf import (Material, mirror_x, rotate_y, rotate_z, sd_box, sd_capsule,  # noqa: E402
                        sd_cylinder_y, sd_cylinder_z, sd_ellipsoid, sd_plate, sd_sphere, subtract,
                        union)

r5.SUB = "CONCEPT ROUND 60 - 960X540"
FW, FH, FPS, TAU = r5.FW, r5.FH, r5.FPS, 2 * np.pi
grid = r5.grid
source_panel = r5b.source_panel
DOWN, UP, RIGHT, LEFT = np.pi / 2, -np.pi / 2, 0.0, np.pi
DESIGN = ROOT / "design" / "enemies"

GOLD_RIM = np.array((255, 206, 84), dtype=np.float64)
GOLD_HI = np.array((255, 236, 160), dtype=np.float64)
RED_RIM = np.array((230, 50, 40), dtype=np.float64)
CRIMSON = (255, 48, 56)
TEAL = (0, 255, 154)
VIOLET = (154, 77, 255)
GOLD = (255, 196, 64)
TEAL_F, VIOLET_F = (np.array(c) / 255.0 for c in (TEAL, VIOLET))
GOLD_F = np.array((1.0, 0.72, 0.2))
WHITE_GOLD_F = np.array((1.0, 0.9, 0.62))
A_X = 7                                                 # first extra material slot


# --------------------------------------------------------------------------- small helpers

def times(seconds):
    return [i / FPS for i in range(int(seconds * FPS))]


def smooth(x):
    x = np.clip(x, 0.0, 1.0)
    return x * x * (3 - 2 * x)


def emissive(col, k=1.0):
    return Material((0.03, 0.03, 0.03), emission=tuple(np.asarray(col, dtype=np.float64) * k))


def light(img, x, y, r, col, k=1.0):
    """Additive light blob on the crop around (x, y) only (faster than raster.add_light)."""
    x0, y0 = int(max(0, x - 2 * r)), int(max(0, y - 2 * r))
    x1, y1 = int(min(img.width, x + 2 * r + 1)), int(min(img.height, y + 2 * r + 1))
    if x1 <= x0 or y1 <= y0 or k <= 0:
        return img
    box = (x0, y0, x1, y1)
    a = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[y0:y1, x0:x1]
    f = np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r) * 2.0) * k
    a[..., :3] += f[..., None] * np.asarray(col, dtype=np.float64)
    img.paste(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), img.mode), box)
    return img


def fade(sp, alpha):
    a = sp.copy()
    a.putalpha(a.getchannel("A").point(lambda v: int(v * alpha)))
    return a


def tint(sp, col, k):
    return e6.tint(sp, col, k)


def flash(sp, on, col=(255, 255, 255), k=0.55):
    return tint(sp, col, k) if on else sp


def label(img, x, y, text, col=(240, 230, 200)):
    raster.draw_text(img, int(x), int(y), text, col)


def caption(img, text, col=(255, 210, 120)):
    w = raster.text_width(text) + 12
    d = ImageDraw.Draw(img)
    d.rectangle([8, 8, 8 + w, 22], fill=(10, 14, 22, 200), outline=(80, 110, 140, 255))
    raster.draw_text(img, 14, 12, text, col)


def rot_sprite(sp, deg):
    return sp.rotate(deg, resample=Image.BICUBIC, expand=True)


def flip_model(make):
    """A round-04 model built facing down (``_face_down``) as a +Y-forward model for AngleSprites."""
    def make2(**kw):
        scene, mats = make(**kw)
        return (lambda p, s=scene: s(rotate_z(p, np.pi))), mats
    return make2


def belt_mats(accent="c8202a", glow=1.0, red=1.0, extra=()):
    """The Q11 a livery: the L28 cruisers' gloss black (hex-panelled hull, darker facets), gold,
    the unit's accent on the gun slot, bright red running lights; extra slots from 7 on."""
    mats = em.asc_mats(glow)
    mats[A_HULL] = replace(mats[A_HULL], albedo=hx("1a1622"), shininess=120, spec=1.25)
    mats[A_FACET] = replace(mats[A_FACET], albedo=hx("26202e"))
    mats[A_GUN] = Material(hx(accent), metal=0.7, shininess=100, spec=1.0)
    mats[A_RED] = Material((0.05, 0.02, 0.02), emission=tuple(em.RED * 2.0 * glow * red))
    return mats + list(extra)


def belt_rim(strong=True):
    """The Q11 a rim light: gold on the edges facing the top-left sun (2 px, brighter, on
    ``strong``), red on the edges facing away."""
    def post(sp):
        a = np.array(sp)
        op = a[..., 3] > 0
        pad = np.pad(op, 1)
        left, up = ~pad[1:-1, :-2], ~pad[:-2, 1:-1]
        right, down = ~pad[1:-1, 2:], ~pad[2:, 1:-1]
        tl = (left | up) & op
        br = (right | down) & op & ~tl
        out = a.astype(np.float64)
        k = 0.88 if strong else 0.7
        out[tl, :3] = out[tl, :3] * (1 - k) + (GOLD_HI if strong else GOLD_RIM) * k
        if strong:
            inner = op & ~tl & ~br & (np.roll(tl, 1, axis=1) | np.roll(tl, 1, axis=0))
            out[inner, :3] = out[inner, :3] * 0.55 + GOLD_RIM * 0.45
        out[br, :3] = out[br, :3] * 0.45 + RED_RIM * 0.55
        img = Image.fromarray(out.astype(np.uint8), "RGBA")
        return sprite.quantize(img, 32 if img.width <= 44 else 48)
    return post


def gold_share(sp):
    """Share of the silhouette drawn gold (the Q11 a rule asks for >= 20 %)."""
    a = np.array(sp).astype(np.float64)
    op = a[..., 3] > 0
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    gold = op & (r > 110) & (g > 0.42 * r) & (g < 0.95 * r) & (b < 0.66 * g)
    return gold.sum() / max(1, op.sum())


def vrell_mats(mid, dark, bone, glow_col, weak_col, glow=1.0, extra=()):
    """Vrell chitin slots (V_BODY .. V_SAC) from explicit colours (hex strings)."""
    mid, dark, bone = hx(mid), hx(dark), hx(bone)
    gl, wk = np.asarray(glow_col) / 255.0, np.asarray(weak_col) / 255.0
    return [
        Material(mid, metal=0.3, shininess=110, spec=1.0, pattern=em._ridges(18, 0.2)),
        Material(dark, metal=0.35, shininess=80, spec=0.8),
        Material(mid, metal=0.3, shininess=110, spec=1.0, pattern=em._ridges(18, 0.15),
                 emission=tuple(gl * 1.5 * glow), emission_pattern=em._seam(0.055)),
        Material((0.04, 0.04, 0.04), emission=tuple(gl * 1.35 * glow)),
        Material((0.04, 0.04, 0.04), emission=tuple(wk * 1.5 * glow)),
        Material(bone, metal=0.2, shininess=70, spec=0.7),
        Material(em._mix(mid, dark, 0.35), metal=0.1, shininess=60, spec=0.9,
                 emission=tuple(gl * 1.0 * glow), emission_pattern=em._veins(9, 0.3, 0.35)),
    ] + list(extra)


def Turning(make, size, count=16, factor=5, colors=32, post=None, sym=1, extent=2.3):
    return rig.ModelSpaceAngleSprites(make, size, count, extent=extent, factor=factor,
                                      colors=colors, sym=sym, post=post)


# --------------------------------------------------------------------------- production sprites

SPR = ROOT / "assets" / "sprites"
EXPL = {k: [Image.open(SPR / f"explosion-{k}_{i}.png").convert("RGBA") for i in range(12)]
        for k in ("tiny", "small", "medium", "large")}


def rock_sprite(r, seed, col=(150, 128, 104)):
    """A drifting belt rock: a lumpy warm-grey body lit hard from the top-left, cratered."""
    n = int(2 * r + 6)
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float64) - n / 2 + 0.5
    ang, rad = np.arctan2(yy, xx), np.hypot(xx, yy)
    edge = r * (1 + sum(rng.uniform(0.03, 0.06) * np.sin(k * ang + rng.uniform(0, TAU)) for k in (2, 3, 5, 7)))
    inside = rad < edge
    u = np.clip(rad / edge, 0, 1)
    nz = np.sqrt(np.clip(1 - u ** 2, 0, 1))
    nx, ny = xx / (edge + 1e-9), yy / (edge + 1e-9)
    tex = raster.fbm(n, n, 6, seed, octaves=3, period=False)
    lt = np.clip(-0.62 * nx - 0.62 * ny + 0.62 * nz + 0.25 * (tex - 0.5), 0.06, 1.2)
    for _ in range(int(r // 9) + 1):
        cx, cy, cr = rng.uniform(-r * 0.6, r * 0.6), rng.uniform(-r * 0.6, r * 0.6), rng.uniform(2, r * 0.25)
        dd = np.hypot(xx - cx, yy - cy) / cr
        lt = np.where(dd < 1, lt * (0.55 + 0.45 * (xx - cx + yy - cy > 0)), lt)
    rgb = np.asarray(col, dtype=np.float64) * (0.18 + 0.95 * lt)[..., None]
    a = np.zeros((n, n, 4))
    a[..., :3] = rgb
    a[..., 3] = inside * 255
    return sprite.quantize(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"), 16)


ROCK_L = [rock_sprite(38, s) for s in (3, 5, 8)]


def boom(img, x, y, s, size="small", sparks=GOLD, n=10):
    """A kill in vacuum: the game's explosion frames and a ring of flung sparks (no smoke)."""
    if not 0 <= s < 1:
        return img
    sprite.paste_center(img, EXPL[size][min(11, int(s * 12))], x, y)
    if sparks:
        d = ImageDraw.Draw(img)
        rr = {"tiny": 14, "small": 26, "medium": 44, "large": 70}[size] * (0.3 + s)
        for k in range(n):
            a = k * TAU / n + 0.3 + 0.2 * np.sin(k * 7.1)
            px, py = x + np.cos(a) * rr, y + np.sin(a) * rr
            al = int(255 * (1 - s))
            d.line([px, py, px - np.cos(a) * 3, py - np.sin(a) * 3], fill=tuple(sparks) + (al,), width=1)
    return img


def spark(img, x, y, s, col=(255, 230, 170), n=5, r=8):
    """A short hit spark (0 <= s <= 1)."""
    if not 0 <= s <= 1:
        return img
    d = ImageDraw.Draw(img)
    for k in range(n):
        a = k * TAU / n + 1.1
        rr = r * (0.3 + s)
        d.line([x + np.cos(a) * rr * 0.4, y + np.sin(a) * rr * 0.4, x + np.cos(a) * rr,
                y + np.sin(a) * rr], fill=tuple(col) + (int(255 * (1 - s)),), width=1)
    return light(img, x, y, 5, col, 0.8 * (1 - s))


# --------------------------------------------------------------------------- belt backdrops

WARM_ROCK = [(0.0, "0c0a08"), (0.2, "2a241f"), (0.45, "4e443b"), (0.7, "7c6d5c"), (0.9, "a8977f"),
             (1.0, "cfc0a6")]
GREY_HULL = [(0.0, "1c2229"), (0.25, "353f4a"), (0.5, "56626f"), (0.72, "7d8a97"), (0.9, "aab5bf"),
             (1.0, "dde4ea")]


def remap(img, stops, lo=1.0, hi=99.5, gain=1.0):
    """Re-colour an image by its luminance along a ramp (the shared generators' palettes are
    other acts' colours)."""
    a = np.array(img.convert("RGB")).astype(np.float64)
    lum = a @ np.array((0.3, 0.55, 0.15))
    p0, p1 = np.percentile(lum, lo), np.percentile(lum, hi)
    t = np.clip((lum - p0) / (p1 - p0 + 1e-9) * gain, 0, 1)
    rgb = raster.ramp([(k, raster.hexrgb(c)) for k, c in stops], t)
    return Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def _post(rgb, colors=20, blur=1.0):
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    return img.filter(ImageFilter.GaussianBlur(blur)).quantize(colors, dither=Image.Dither.NONE) \
        .convert("RGBA")


def _rock_blobs(arr, rng, n, rmin, rmax, col, w, h, tex=None):
    yy, xx = np.mgrid[0:h, 0:w]
    for _ in range(n):
        cx, cy, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(rmin, rmax)
        dx, dy = (xx - cx) / r, (yy - cy) / r
        wob = 0.12 * np.sin(np.arctan2(dy, dx) * 3 + cx)
        dist = np.sqrt(dx * dx + dy * dy) + wob
        inside = dist < 1.0
        nz = np.sqrt(np.clip(1 - dist ** 2, 0, 1))
        lt = np.clip(-0.6 * dx - 0.6 * dy + 0.7 * nz, 0.1, 1.25)
        c = np.asarray(col, dtype=np.float64)
        arr[inside, :3] = c * (0.25 + 0.85 * lt[inside, None])


def space_bg(seed, h=FH, band=1.0):
    """Belt space a step lighter than Act 4: a sunlit warm dust band across the deep layer,
    stars, a few distant rocks lit from the top-left."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    base = np.zeros((h, w, 3)) + np.array((10, 10, 16))
    # the band runs from the upper left to the lower right, repeating down a tall image
    phase = (xx * 0.55 - yy) % 900.0
    dist = np.minimum(phase, 900.0 - phase) - 160
    dust = np.exp(-(dist / 150.0) ** 2) * (0.55 + 0.6 * raster.fbm(w, h, 64, seed, octaves=4,
                                                                     period=False))
    base += dust[..., None] * np.array((92, 74, 58)) * band
    stars = np.array(raster.starfield(w, h, seed + 1, density=0.0022)).astype(np.float64)[..., :3]
    base = np.maximum(base, stars * (1 - 0.5 * dust[..., None]) * 0.9)
    _rock_blobs(base, rng, max(3, h // 140), 5, 16, (70, 60, 52), w, h)
    return _post(base, 24, 0.6)


def rock_bg(seed, h=FH, growth=False):
    """Sunlit warm asteroid rock (harsh top-left light), a step lighter than Act 4; ``growth``
    adds the Leech Field's dark Vrell growth with teal and violet specks."""
    out = remap(e3.regolith(FW, h, seed, craters=max(8, h // 50)), WARM_ROCK)
    out = out.convert("RGB").filter(ImageFilter.GaussianBlur(0.8)).quantize(18, dither=Image.Dither.NONE).convert("RGBA")
    if growth:
        rng = np.random.default_rng(seed + 9)
        lay = Image.new("RGBA", out.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(lay)
        for _ in range(h // 60):
            cx, cy = rng.uniform(0, FW), rng.uniform(0, h)
            for _ in range(26):
                a_, r_ = rng.uniform(0, TAU), rng.uniform(0, 26)
                x, y, s = cx + np.cos(a_) * r_, cy + np.sin(a_) * r_ * 0.7, rng.uniform(3, 8)
                d.ellipse([x - s, y - s, x + s, y + s], fill=(46, 22, 54, 235))
            for _ in range(8):
                a_, r_ = rng.uniform(0, TAU), rng.uniform(0, 24)
                x, y = cx + np.cos(a_) * r_, cy + np.sin(a_) * r_ * 0.7
                col = (0, 200, 130) if rng.uniform() < 0.5 else (140, 70, 230)
                d.rectangle([x - 1, y - 1, x, y], fill=col + (255,))
        out.alpha_composite(lay.filter(ImageFilter.GaussianBlur(0.6)))
    return out


def hull_bg(seed, h=FH):
    """Ceres Hub's station hull (L29): cool grey-white Coalition plating, window strips and
    white work lamps."""
    img = remap(e3.hull_plating(FW, h, seed), GREY_HULL, gain=0.95)
    rng = np.random.default_rng(seed + 3)
    d = ImageDraw.Draw(img)
    for _ in range(h // 170):
        x, y = rng.uniform(10, FW - 120), rng.uniform(0, h)
        d.rectangle([x - 2, y - 2, x + 110, y + 8], fill=(40, 46, 54, 255))
        for k in range(0, 108, 6):
            if rng.uniform() < 0.6:
                d.rectangle([x + k, y + 1, x + k + 3, y + 5], fill=(196, 202, 186, 255))
    for _ in range(h // 45):
        x, y = rng.uniform(0, FW), rng.uniform(0, h)
        d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=(250, 250, 240, 255))
    return img


def wall_x(y, side, seed=0.0):
    base = 64 + 20 * np.sin(y / 90.0 + seed + (0 if side < 0 else 2.0)) + 8 * np.sin(y / 31.0 + side)
    return base if side < 0 else FW - base


def tunnel_bg(seed, h=FH, galleries=()):
    """L32's hollowed asteroid: a tunnel floor between dark rock walls with sunless cool work
    lamps; ``galleries`` (y, side, depth) cut side galleries into a wall."""
    floor = np.array(remap(e3.regolith(FW, h, seed, craters=h // 60), WARM_ROCK,
                           gain=0.85)).astype(np.float64)
    yy = np.arange(h)[:, None].astype(np.float64)
    xx = np.arange(FW)[None, :].astype(np.float64)
    xl, xr = wall_x(yy, -1, seed), wall_x(yy, 1, seed)
    for gy, side, depth in galleries:
        bump = depth * np.exp(-((yy - gy) / 70.0) ** 4)
        if side < 0:
            xl = xl - bump
        else:
            xr = xr + bump
    strata = 0.6 + 0.12 * np.sin((xx + yy * 0.3) / 6.0) + 0.06 * np.sin(yy / 3.0)
    rock = np.array((44, 38, 34)) * strata[..., None]
    wall = (xx < xl) | (xx > xr)
    floor[..., :3] = np.where(wall[..., None], rock, floor[..., :3])
    lit = ((xx >= xl) & (xx - xl < 10))                       # wall foot shadow / lit faces
    floor[lit, :3] *= 0.62
    face = (xx > xr - 5) & (xx <= xr)
    floor[face, :3] = floor[face, :3] * 0.6 + np.array((150, 138, 120)) * 0.4
    img = _post(floor[..., :3], 22, 0.8)
    d = ImageDraw.Draw(img)
    for y in range(20, h, 96):                                # work lamps on the walls
        for side in (-1, 1):
            x = float(wall_x(y, side, seed)) + (6 if side < 0 else -6)
            d.rectangle([x - 2, y - 2, x + 2, y + 2], fill=(40, 44, 50, 255))
            d.point((x, y), fill=(240, 246, 255, 255))
    for y in range(20, h, 96):
        for side in (-1, 1):
            img = light(img, float(wall_x(y, side, seed)) + (8 if side < 0 else -8), y, 22,
                        (90, 96, 104), 0.5)
    return img


def derelict_bg(seed, h=FH):
    """L34's ship graveyard: dark derelict hull slabs over belt space, scorched and torn."""
    img = space_bg(seed, h, band=0.8)
    plate = remap(e3.hull_plating(FW, h, seed + 4), GREY_HULL, gain=0.7)
    a = np.array(plate).astype(np.float64)
    a[..., :3] = a[..., :3] * np.array((0.8, 0.78, 0.76))
    rng = np.random.default_rng(seed + 2)
    mask = Image.new("L", (FW, h), 0)
    d = ImageDraw.Draw(mask)
    pts_l = [(60 + 30 * np.sin(y / 40.0 + rng.uniform()) + rng.uniform(-14, 14), y)
             for y in range(-20, h + 40, 24)]
    pts_r = [(FW - 40 + 24 * np.sin(y / 55.0) + rng.uniform(-14, 14), y)
             for y in range(h + 40, -20, -24)]
    d.polygon(pts_l + pts_r, fill=255)
    for _ in range(h // 120):                                  # torn holes
        x, y, r = rng.uniform(80, FW - 80), rng.uniform(0, h), rng.uniform(10, 24)
        d.polygon([(x + np.cos(k) * r * rng.uniform(0.6, 1.2), y + np.sin(k) * r * rng.uniform(0.6, 1.2))
                   for k in np.linspace(0, TAU, 9, endpoint=False)], fill=0)
    plate = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    plate.putalpha(mask)
    img.alpha_composite(plate)
    sc = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d2 = ImageDraw.Draw(sc)
    for _ in range(h // 90):                                   # scorch marks
        x, y, r = rng.uniform(80, FW - 80), rng.uniform(0, h), rng.uniform(14, 34)
        d2.ellipse([x - r, y - r * 0.7, x + r, y + r * 0.7], fill=(10, 8, 8, 120))
    img.alpha_composite(sc.filter(ImageFilter.GaussianBlur(6)))
    return img


class Scroll:
    """A tall backdrop scrolling down at ``speed`` px/s: ``frame(t)`` crops the field, ``y(y0, t)``
    is the screen y of a ground point that was at ``y0`` at t = 0."""

    def __init__(self, make, seed, dur, speed=30.0, **kw):
        self.speed = speed
        self.h = int(FH + speed * dur + 8)
        self.img = make(seed, self.h, **kw)

    def frame(self, t):
        top = self.h - FH - int(round(self.speed * t))
        return self.img.crop((0, top, FW, top + FH))

    def y(self, y0, t):
        return y0 + int(round(self.speed * t))

    def still(self):
        return self.frame(0.0)


def darkest(bg, n=96):
    """The darkest n x n window of a backdrop (the readability check's ground)."""
    g = np.array(bg.convert("L")).astype(np.float64)
    ii = g.cumsum(0).cumsum(1)
    s = ii[n:, n:] - ii[:-n, n:] - ii[n:, :-n] + ii[:-n, :-n]
    y, x = np.unravel_index(np.argmin(s), s.shape)
    return bg.crop((x, y, x + n, y + n))


def readability(items, bg, scales=(1, 2), n=None):
    """Each sprite over the darkest window of its level's backdrop, at 1x and 2x, with the
    measured gold share."""
    n = n or max(64, max(max(sp.size) for sp, _ in items) + 24)
    ground = darkest(bg, n)
    cells, labels = [], []
    for sc in scales:
        for sp, lab in items:
            c = ground.copy()
            sprite.paste_center(c, sp, n / 2, n / 2)
            cells.append(c.resize((n * sc, n * sc), Image.NEAREST))
            labels.append(f"{lab} {sc}X")
    w = sum(c.width for c in cells) + 8 * len(cells)
    h = max(c.height for c in cells) + 12
    img = Image.new("RGBA", (w, h), raster.SHEET_BG)
    x = 0
    for c, lab in zip(cells, labels):
        img.alpha_composite(c, (x, 0))
        raster.draw_text(img, x, c.height + 2, lab, raster.LABEL_DIM)
        x += c.width + 8
    return img


def gold_note(sp):
    return f"GOLD {100 * gold_share(sp):.0f} %"


# --------------------------------------------------------------------------- the player and shots

def ship(img, px, py, t, shots=True, stop=None, rear=False):
    sprite.paste_center(img, e3.player_sprite(), px, py)
    if shots:
        for k in range(4):
            y = py - 40 - ((t * 420 + k * 105) % 420)
            if y > -10 and (stop is None or y > stop):
                e3.bolt(img, px - 10, y)
                e3.bolt(img, px + 10, y)
    if rear:
        for k in range(3):
            y = py + 30 + ((t * 380 + k * 120) % 330)
            if y < FH + 10:
                e3.bolt(img, px, y)


def follow(targets, dur, x0=240.0, vmax=220.0, lag=0.35):
    """The ship's x over time: it chases ``targets(t)`` (an x or None) at most ``vmax`` px/s.
    Returns a function t -> x."""
    xs, x = [], x0
    for t in times(dur + 1):
        tx = targets(t)
        if tx is not None:
            dx = (tx - x) / max(lag, 1e-3)
            x += float(np.clip(dx, -vmax, vmax)) / FPS
        xs.append(x)
    return lambda t: xs[min(len(xs) - 1, int(t * FPS))]


class Shots:
    """Straight enemy shots: (t0, x, y, vx, vy, r) drawn as the readability rules' orbs."""

    def __init__(self):
        self.list = []

    def fire(self, t0, x, y, tx, ty, speed=160, r=4):
        d = np.hypot(tx - x, ty - y) or 1
        self.list.append((t0, x, y, (tx - x) / d * speed, (ty - y) / d * speed, r))

    def ring(self, t0, x, y, n, speed, r=4, phase=0.0):
        for a in np.linspace(0, TAU, n, endpoint=False) + phase:
            self.fire(t0, x, y, x + np.cos(a), y + np.sin(a), speed, r)

    def fan(self, t0, x, y, aim, n, spread, speed, r=4):
        for a in np.linspace(-spread / 2, spread / 2, n) + aim:
            self.fire(t0, x, y, x + np.cos(a), y + np.sin(a), speed, r)

    def draw(self, img, t):
        for t0, x, y, vx, vy, r in self.list:
            if t >= t0:
                bx, by = x + vx * (t - t0), y + vy * (t - t0)
                if -20 < bx < FW + 20 and -20 < by < FH + 20:
                    e3.orb(img, bx, by, r)


def side_warning(img, t, side, ys, text="SIDE!"):
    if int(t * 8) % 2:
        return
    d = ImageDraw.Draw(img)
    x0 = 8 if side < 0 else FW - 8
    for y in ys:
        d.polygon([(x0, y - 14), (x0, y + 14), (x0 + 18 * (-side if side > 0 else 1), y)],
                  outline=(255, 70, 50, 255), fill=(90, 10, 10, 200))
    raster.draw_text(img, (x0 + 22) if side < 0 else (x0 - 22 - raster.text_width(text)),
                     int(np.mean(ys)) - 4, text, (255, 90, 70))


def rear_warning(img, t, xs, text="REAR!"):
    e6.edge_warning(img, t, xs, text)


# --------------------------------------------------------------------------- the homing diamond

def _diamond_hi(v, k=6):
    """The homing diamond, nose to the right, drawn at k x for a smooth downsample."""
    W, H = 22 * k, 14 * k
    im = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    cx, cy = 13 * k, 7 * k
    if v == "a":                                   # broad red-gold diamond, gold inner, hot core
        nose, tail, half = 7.5, 5.5, 4.6
        outer = [(cx + nose * k, cy), (cx, cy - half * k), (cx - tail * k, cy), (cx, cy + half * k)]
        d.polygon([(x + (x - cx) * 0.22, y + (y - cy) * 0.3) for x, y in outer], fill=(46, 6, 8, 255))
        d.polygon(outer, fill=(226, 40, 36, 255))
        inner = [(cx + (x - cx) * 0.55 + 0.6 * k, cy + (y - cy) * 0.55) for x, y in outer]
        d.polygon(inner, fill=(255, 196, 64, 255))
        d.ellipse([cx + 0.2 * k, cy - 1.1 * k, cx + 2.4 * k, cy + 1.1 * k], fill=(255, 244, 214, 255))
        d.line([(cx - tail * k, cy), (cx - (tail + 6.5) * k, cy)], fill=(255, 222, 150, 210), width=int(1.4 * k))
    else:                                          # slim gold kite, red core, a dotted exhaust
        nose, tail, half = 9.0, 4.5, 3.6
        outer = [(cx + nose * k, cy), (cx + 1.0 * k, cy - half * k), (cx - tail * k, cy),
                 (cx + 1.0 * k, cy + half * k)]
        d.polygon([(x + (x - cx) * 0.2, y + (y - cy) * 0.36) for x, y in outer], fill=(46, 22, 4, 255))
        d.polygon(outer, fill=(255, 190, 56, 255))
        inner = [(cx + (x - cx) * 0.5 + 0.8 * k, cy + (y - cy) * 0.5) for x, y in outer]
        d.polygon(inner, fill=(236, 36, 40, 255))
        d.ellipse([cx + 5.6 * k, cy - 0.8 * k, cx + 7.4 * k, cy + 0.8 * k], fill=(255, 240, 220, 255))
        for j in range(3):
            x = cx - (tail + 2.2 + 2.6 * j) * k
            r = (1.0 - 0.22 * j) * k
            d.ellipse([x - r, cy - r, x + r, cy + r], fill=(255, 222, 150, 220 - 50 * j))
    return im


@lru_cache(maxsize=None)
def diamond(v, k32, hit=False):
    """The diamond sprite at heading bucket ``k32`` (32 headings, 0 = right)."""
    hi = _diamond_hi(v).rotate(-k32 * 360 / 32, resample=Image.BICUBIC, expand=True)
    sp = hi.resize((max(1, hi.width // 6), max(1, hi.height // 6)), Image.LANCZOS)
    a = np.array(sp)
    a[..., 3] = np.where(a[..., 3] > 90, 255, 0)
    sp = Image.fromarray(a, "RGBA")
    return tint(sp, (255, 255, 255), 0.6) if hit else sp


def diamond_at(v, h, hit=False):
    return diamond(v, int(round((h % TAU) / (TAU / 32))) % 32, hit)


class Homing:
    """Homing diamonds: launched at t0 from (x, y) along h0, they turn toward ``target(t)``
    at ``turn`` deg/s and ``speed`` px/s for ``life`` s, then fizzle; ``kill`` shoots one down."""

    def __init__(self, v, target, speed=110.0, turn=80.0, life=4.0):
        self.v, self.target, self.speed, self.turn, self.life = v, target, speed, turn, life
        self.list = []

    def launch(self, t0, x, y, h0, kill=None):
        pos, h, path = np.array((x, y), dtype=np.float64), h0, []
        for k in range(int(self.life * FPS)):
            t = t0 + k / FPS
            if kill is not None and t >= kill:
                break
            tx, ty = self.target(t)
            want = np.arctan2(ty - pos[1], tx - pos[0])
            dh = (want - h + np.pi) % TAU - np.pi
            lim = np.radians(self.turn) / FPS
            h += float(np.clip(dh, -lim, lim))
            pos = pos + np.array((np.cos(h), np.sin(h))) * self.speed / FPS
            path.append((t, pos.copy(), h))
        self.list.append(dict(t0=t0, path=path, kill=kill, end=path[-1][0] if path else t0))

    def draw(self, img, t):
        for m in self.list:
            pts = [p for p in m["path"] if p[0] <= t]
            if not pts:
                continue
            _, pos, h = pts[-1]
            if m["kill"] is not None and t >= m["kill"]:
                img = spark(img, pos[0], pos[1], (t - m["kill"]) / 0.3, (255, 210, 140), 6, 9)
                continue
            if t > m["end"] + 1.0 / FPS:
                s = (t - m["end"]) / 0.4
                if s < 1:
                    d = ImageDraw.Draw(img)
                    r = 2 + 5 * s
                    d.ellipse([pos[0] - r, pos[1] - r, pos[0] + r, pos[1] + r],
                              outline=(200, 190, 170, int(200 * (1 - s))))
                continue
            hit = m["kill"] is not None and m["kill"] - 0.3 < t and int(t * 16) % 2
            sprite.paste_center(img, diamond_at(self.v, h, hit), pos[0], pos[1])
        return img


# --------------------------------------------------------------------------- the hex shield

@lru_cache(maxsize=None)
def _hex_lines(w, h, cell=5.0):
    im = Image.new("L", (w, h), 0)
    d = ImageDraw.Draw(im)
    dx, dy = cell * 1.732, cell * 1.5
    for j in range(-1, int(h / dy) + 2):
        for i in range(-1, int(w / dx) + 2):
            cx = i * dx + (dx / 2 if j % 2 else 0)
            cy = j * dy
            pts = [(cx + cell * np.cos(a), cy + cell * np.sin(a))
                   for a in np.linspace(np.pi / 6, TAU + np.pi / 6, 7)]
            d.line(pts, fill=255, width=1)
    return np.array(im) > 0


def shield(img, sp, x, y, mode="hug", alpha=0.4, hit=None, shatter=None, refill=None):
    """The hex shield over ``sp`` centred at (x, y): ``hug`` follows the hull 3 px out, ``bubble``
    is an ellipse round it. ``hit`` = local (dx, dy) flashes the hexes there; ``shatter`` 0..1
    throws the fragments; ``refill`` 0..1 shimmers it in from the edge."""
    pad = 7
    W, H = sp.width + 2 * pad, sp.height + 2 * pad
    if mode == "hug":
        m = Image.new("L", (W, H), 0)
        m.paste(sp.getchannel("A").point(lambda v: 255 if v else 0), (pad, pad))
        region = np.array(m.filter(ImageFilter.MaxFilter(7))) > 0
    else:
        yy, xx = np.mgrid[0:H, 0:W]
        region = ((xx - W / 2 + 0.5) / (sp.width / 2 + 4)) ** 2 + ((yy - H / 2 + 0.5) / (sp.height / 2 + 4)) ** 2 < 1
    er = np.array(Image.fromarray(region.astype(np.uint8) * 255).filter(ImageFilter.MinFilter(3))) > 0
    edge = region & ~er
    out = np.zeros((H, W, 4))
    gold = np.array((255, 206, 90))
    if shatter is not None:
        if shatter >= 1:
            return img
        ys, xs = np.nonzero(edge)
        d = ImageDraw.Draw(img)
        cx, cy = W / 2, H / 2
        for k in range(0, len(xs), max(1, len(xs) // 16)):
            ex, ey = xs[k], ys[k]
            a = np.arctan2(ey - cy, ex - cx)
            r = 3 + 16 * shatter
            sx, sy = x - W / 2 + ex + np.cos(a) * r, y - H / 2 + ey + np.sin(a) * r
            d.line([sx, sy, sx + np.cos(a + 1.2) * 2.5, sy + np.sin(a + 1.2) * 2.5],
                   fill=(255, 220, 120, int(255 * (1 - shatter))), width=1)
        return img
    lines = _hex_lines(W, H) & region
    k = alpha
    if refill is not None:
        dist = np.array(Image.fromarray(region.astype(np.uint8) * 255)
                        .filter(ImageFilter.MinFilter(int(1 + 2 * (1 + 8 * (1 - refill))) | 1))) > 0
        lines = lines & ~dist
        edge = edge if refill > 0.2 else edge & False
    out[lines] = np.concatenate([gold, [255 * k]])
    out[edge] = np.concatenate([gold, [255 * min(1, k * 1.7)]])
    if hit is not None:
        yy, xx = np.mgrid[0:H, 0:W]
        near = np.hypot(xx - (W / 2 + hit[0]), yy - (H / 2 + hit[1])) < 8
        out[(lines | edge) & near] = (255, 250, 220, 255)
    lay = Image.fromarray(out.astype(np.uint8), "RGBA")
    sprite.paste_center(img, lay, x, y)
    return img


# --------------------------------------------------------------------------- output

def out_path(unit_dir, name):
    d = DESIGN / unit_dir / "concept"
    d.mkdir(parents=True, exist_ok=True)
    return d / name


def save_unit(unit_dir, slug, v, sheet, frames, fps=FPS):
    png = out_path(unit_dir, f"{slug}-r60-{v}.png")
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, out_path(unit_dir, f"{slug}-r60-{v}.gif"), fps=fps)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)",
          flush=True)


def sheet(title, panels, notes, game_frame):
    return r5.build_sheet(title, panels, notes, game_frame)


def row(items, size, scale=2, labels=None, bg=None):
    """Sprites centred on ``size`` cells in one row (checker, or a flat colour ``bg``)."""
    cells = []
    for it in items:
        c = Image.new("RGBA", (size, size), (bg + (255,)) if bg else (0, 0, 0, 0))
        sprite.paste_center(c, it, size // 2, size // 2)
        cells.append(c)
    if bg:
        w = size * scale
        lab = 10 if labels else 0
        img = Image.new("RGBA", (len(cells) * (w + 6), w + lab + 6), raster.SHEET_BG)
        for i, c in enumerate(cells):
            img.alpha_composite(sprite.enlarge(c, scale), (i * (w + 6), 0))
            if labels:
                raster.draw_text(img, i * (w + 6), w + 2, labels[i], raster.LABEL_DIM)
        return img
    return grid(cells, len(cells), scale, labels=labels)


def player_cell(size=48):
    c = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sprite.paste_center(c, e3.player_sprite(), size // 2, size // 2)
    return c


def keyframes(frames, ts, labels, scale=0.5):
    """Frames of the GIF at times ``ts`` side by side, scaled down."""
    ims = [frames[min(len(frames) - 1, int(t * FPS))].resize((int(FW * scale), int(FH * scale)),
                                                             Image.LANCZOS) for t in ts]
    lab = 12
    img = Image.new("RGBA", (len(ims) * (ims[0].width + 6), ims[0].height + lab), raster.SHEET_BG)
    for i, (im, lb) in enumerate(zip(ims, labels)):
        img.alpha_composite(im.convert("RGBA"), (i * (im.width + 6), 0))
        raster.draw_text(img, i * (im.width + 6), im.height + 2, lb, raster.LABEL_DIM)
    return img


# ===================================================================== units

UNITS = {}


def unit(name):
    def deco(fn):
        UNITS[name] = fn
        return fn
    return deco


# ===================================================================== 1 Talon (L29)

def talon_model(v, leader=False, glow=1.0):
    """The chosen r04 Talon (em.talon) facing +Y in the belt livery. a: gold trim - the r04 gold
    chines and spine, gold wing-tip caps, the strong rim; b: gold panels - the outer wing panels
    and the tail fins in gold. The leader adds a broad gold stripe down the spine and brighter
    red running lights."""
    scene0, _ = em.talon(0.0, glow)

    def scene(p):
        d, m = scene0(rotate_z(p, np.pi))
        q = mirror_x(p)
        if v == "a":
            m = np.where((m == A_FACET) & (q[:, 0] > 0.8), A_GOLD, m)
        else:
            m = np.where((m == A_FACET) & ((q[:, 0] > 0.5) | (q[:, 1] < -0.45)), A_GOLD, m)
        if leader:
            m = np.where((m == A_HULL) & (q[:, 0] < 0.075) & (q[:, 1] > -0.62), A_GOLD, m)
        return d, m
    return scene, belt_mats("c8202a", glow, red=1.6 if leader else 1.0)


TAL_N = 46
talons = {(v, ld): Turning(lambda v=v, ld=ld: talon_model(v, ld), TAL_N, 16, factor=5, colors=32,
                           post=belt_rim(v == "a"))
          for v in "ab" for ld in (False, True)}
TAL_SCALE = TAL_N / 2.3
TAL_DUR = 9.0
LEAD_PATH = rig.catmull_rom([(60, -70), (90, 70), (170, 190), (270, 238), (370, 196), (430, 90),
                             (460, -80)], [0, 0.9, 1.9, 2.9, 3.9, 4.9, 6.0])
WING = [(0, 0), (-30, -28), (30, -28), (-60, -56), (60, -56)]       # (lateral, along) px
LINE_PATH = rig.catmull_rom([(560, 60), (420, 140), (240, 176), (60, 140), (-90, 50)],
                            [5.0, 5.9, 6.8, 7.7, 8.8])
LINE = [-81, -27, 27, 81]
TAL_HITS = {2: (1.75, 1.95, 2.2), 0: (2.75, 2.95, 3.25)}           # flash, shatter, kill
SCATTER = 3.25


def talon_state(i, t):
    """(x, y, heading, alive) of wing member i; members scatter after the leader's kill."""
    if i in TAL_HITS and t >= TAL_HITS[i][2]:
        x, y, h, _ = talon_state(i, TAL_HITS[i][2] - 1e-3)
        return x, y, h, False
    tt = min(t, SCATTER) if i != 0 else t
    x, y = LEAD_PATH(tt)
    h = rig.path_heading(LEAD_PATH, tt)
    lx, ly = WING[i]
    ox, oy = rig.local_to_screen(-lx / TAL_SCALE, ly / TAL_SCALE, h, TAL_SCALE)
    x, y = x + ox, y + oy
    if i != 0 and t > SCATTER:
        cx = LEAD_PATH(SCATTER)[0]
        side = 1.0 if x >= cx else -1.0
        u = t - SCATTER
        vx, vy = side * 260.0, -60.0
        x, y = x + vx * u, y + vy * u
        h = np.arctan2(vy, vx)
    return x, y, h, True


def line_state(j, t):
    x, y = LINE_PATH(t)
    h = rig.path_heading(LINE_PATH, t)
    ox, oy = rig.local_to_screen(LINE[j] / TAL_SCALE, 0, h, TAL_SCALE)
    return x + ox, y + oy, h


def talon_scene(v):
    bg = Scroll(hull_bg, 29, TAL_DUR, 30)
    shots = Shots()
    for i in range(5):
        for tf in (1.0 + 0.15 * i, 2.6 + 0.15 * i):
            x, y, h, alive = talon_state(i, tf)
            dead = i in TAL_HITS and tf >= TAL_HITS[i][2]
            if alive and not dead and y < 300 and not (i and tf > SCATTER):
                for s_ in (-1, 1):
                    ox, oy = rig.local_to_screen(s_ * 0.6, 0.45, h, TAL_SCALE)
                    shots.fire(tf, x + ox, y + oy, 240 + s_ * 6, 476, 160)
    for j in range(4):
        for tf in (6.3 + 0.2 * j, 7.9 + 0.2 * j):
            x, y, h = line_state(j, tf)
            if 0 < x < FW:
                for s_ in (-1, 1):
                    ox, oy = rig.local_to_screen(s_ * 0.6, 0.45, h, TAL_SCALE)
                    shots.fire(tf, x + ox, y + oy, x + (240 - x) * 0.6, 476, 160)

    def target(t):
        if t < TAL_HITS[2][2]:
            return talon_state(2, t)[0]
        if t < TAL_HITS[0][2]:
            return talon_state(0, t)[0]
        if 6.0 < t < 7.4:
            return line_state(1, t)[0]
        return 240 + 60 * np.sin(t)
    px_of = follow(target, TAL_DUR)
    frames = []
    for t in times(TAL_DUR):
        f = bg.frame(t)
        px, py = px_of(t), 476
        for i in range(5):
            x, y, h, alive = talon_state(i, t)
            if not alive:
                f = boom(f, x, y, (t - TAL_HITS[i][2]) / 0.6, "small")
                if (t - TAL_HITS[i][2]) < 0.9:                      # a spinning wing
                    u = t - TAL_HITS[i][2]
                    wx, wy = x + 40 * u, y - 20 * u + 30 * u * u
                    d = ImageDraw.Draw(f)
                    a = u * 14
                    d.line([wx - 6 * np.cos(a), wy - 6 * np.sin(a), wx + 6 * np.cos(a), wy + 6 * np.sin(a)],
                           fill=(220, 170, 60, 255), width=2)
                continue
            if not (-30 < x < FW + 30 and -30 < y < FH + 30):
                continue
            sp = talons[(v, i == 0)].get(h)
            hit_t = TAL_HITS.get(i)
            on = hit_t is not None and hit_t[1] < t < hit_t[2] and int(t * 16) % 2
            sprite.paste_center(f, flash(sp, on), x, y)
            if hit_t is None or t < hit_t[1]:
                hh = (0, 12) if hit_t and hit_t[0] - 0.12 < t < hit_t[0] + 0.12 else None
                if hh or i == 0 or (int(t * 3) + i) % 5 == 0:
                    f = shield(f, sp, x, y, "hug" if v == "a" else "bubble", 0.4, hit=hh)
            elif t < hit_t[1] + 0.2:
                f = shield(f, sp, x, y, "hug" if v == "a" else "bubble", shatter=(t - hit_t[1]) / 0.2)
            for tf in (1.0 + 0.15 * i, 2.6 + 0.15 * i):              # red glint tell
                if tf - 0.25 < t < tf and y < 300:
                    for s_ in (-1, 1):
                        ox, oy = rig.local_to_screen(s_ * 0.6, 0.45, h, TAL_SCALE)
                        f = light(f, x + ox, y + oy, 4, (255, 60, 50), 1.2)
        if 4.8 < t:
            for j in range(4):
                x, y, h = line_state(j, t)
                if -30 < x < FW + 30:
                    sp = talons[(v, False)].get(h)
                    hh = (0, 10) if j == 1 and 6.6 < t < 6.8 else None
                    sprite.paste_center(f, sp, x, y)
                    if hh:
                        f = shield(f, sp, x, y, "hug" if v == "a" else "bubble", 0.4, hit=hh)
        shots.draw(f, t)
        ship(f, px, py, t)
        if 3.25 < t < 4.6:
            caption(f, "LEADER DOWN - THE WING SCATTERS")
        frames.append(f)
    return frames, bg


def talon_sheet(v, frames, bg):
    W, L = talons[(v, False)], talons[(v, True)]
    heads = [W.frame(k) for k in range(16)]
    hold = Image.new("RGBA", (TAL_N + 14, TAL_N + 14), (0, 0, 0, 0))
    mode = "hug" if v == "a" else "bubble"
    states = []
    for kw in (dict(alpha=0.4), dict(alpha=0.4, hit=(0, 12)), dict(shatter=0.4), dict(refill=0.5)):
        c = hold.copy()
        sprite.paste_center(c, W.get(DOWN), c.width / 2, c.height / 2)
        states.append(shield(c, W.get(DOWN), c.width / 2, c.height / 2, mode, **kw))
    panels = [("SOURCE: THE LEADER, 260 PX", source_panel(lambda: talon_model(v, True))),
              ("WINGMAN: 16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("LEADER / WINGMAN / PLAYER (2X)",
               row([L.get(DOWN), W.get(DOWN), e3.player_sprite()], TAL_N + 4, 2,
                   ["LEADER", "WINGMAN", "PLAYER"])),
              ("HEX SHIELD (2X): HOLDS 40 %, A HIT, SHATTER, REFILL",
               row(states, TAL_N + 14, 2, ["HOLD", "HIT", "SHATTER", "REFILL"])),
              ("READABILITY: OVER THE DARKEST L29 GROUND",
               readability([(W.get(DOWN), gold_note(W.get(DOWN))), (L.get(DOWN), "LEADER")], bg.img)),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.9, 3.3, 3.9, 6.7],
                                              ["SHIELD HIT", "LEADER KILLED", "SCATTER", "LINE ABREAST"]))]
    if v == "a":
        notes = ["TALON A - GOLD TRIM (THE CHOSEN R04 AIRFRAME, Q11 A LIVERY, STRONG 2 PX GOLD RIM)",
                 "40X36 PX (46 PX CANVAS), 16 HEADINGS. GLOSS BLACK HEX-PANELLED HULL (THE L28 CRUISERS' BLACK),",
                 "THE R04 GOLD CHINES ALONG THE LEADING EDGES AND SPINE PLUS GOLD WING-TIP CAPS, RED RUNNING",
                 "LIGHTS, A 2 PX GOLD RIM ON THE SUN SIDE. LEADER: A BROAD GOLD STRIPE DOWN THE SPINE AND",
                 "BRIGHTER RED LIGHTS. THE HEX SHIELD HUGS THE HULL 3 PX OUT (40 %), HITS FLASH THE HEXES,",
                 "IT SHATTERS IN 0.2 S. A V-WING SWOOPS IN (PAIRS EVERY 1.6 S, A 0.25 S RED GLINT); THE",
                 "LEADER'S KILL SCATTERS THE WING SIDEWAYS (260 PX/S, NO FIRE); THEN A LINE ABREAST."]
    else:
        notes = ["TALON B - GOLD PANELS (THE CHOSEN R04 AIRFRAME, Q11 A LIVERY, 1 PX RIM)",
                 "40X36 PX (46 PX CANVAS), 16 HEADINGS. THE OUTER WING PANELS AND TAIL FINS GOLD, THE",
                 "BODY GLOSS BLACK: MORE GOLD, A BOLDER TWO-TONE READ AT SPEED, LESS 'STEALTH'. LEADER:",
                 "THE SAME GOLD SPINE STRIPE. THE HEX SHIELD IS A ROUND BUBBLE (40 %) INSTEAD OF A HUG.",
                 "SAME SCENE AS A."]
    return sheet(f"TALON {v.upper()} - V-WING, LEADER, HEX SHIELD (LEVEL 29, CERES HUB)", panels, notes,
                 frames[int(3.5 * FPS)])


@unit("talon")
def make_talon(variants):
    for v in variants:
        frames, bg = talon_scene(v)
        save_unit("air/talon", "talon", v, talon_sheet(v, frames, bg), frames)


# ===================================================================== 2 Dart Drone (L30, tiny)

def dart_model(v, glow=1.0):
    """Dart Drone (faces +Y), tiny expendable rammer. a: needle dart - a slim black needle, a
    gold nose cone, small swept gold-edged fins, one red light; b: chevron dart - a black
    arrowhead with gold leading edges and a red light in the notch."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            return union(
                (sd_capsule(p, (0, 0.5, 0.0), (0, -0.72, 0.0), 0.17, 0.15), A_HULL),
                (sd_capsule(p, (0, 0.45, 0.0), (0, 1.0, 0.0), 0.16, 0.03), A_GOLD),
                (sd_plate(q, [(0.1, -0.15), (0.66, -0.7), (0.6, -0.86), (0.1, -0.62)], 0.0, 0.04), A_GOLD),
                (sd_sphere(p, (0, 0.08, 0.15), 0.07), A_RED),
                (sd_cylinder_y(p, (0, -0.78, 0.0), 0.09, 0.06), A_ENGINE),
                k=0.02)
        wing = [(0.0, 1.0), (0.86, -0.55), (0.62, -0.78), (0.0, -0.28)]
        d, m = union(
            (sd_plate(q, wing, 0.0, 0.1, 0.0, taper=am._roof(0.8)), A_HULL),
            (sd_sphere(p, (0, -0.12, 0.1), 0.08), A_RED),
            (sd_cylinder_y(q, (0.3, -0.5, 0.0), 0.07, 0.05), A_ENGINE))
        lead = (q[:, 1] > 1.0 - 1.81 * q[:, 0] - 0.3) & (m == A_HULL)
        return d, np.where(lead, A_GOLD, m)
    return scene, belt_mats("c8202a", glow)


DART_N = 22
darts = {v: Turning(lambda v=v: dart_model(v), DART_N, 16, factor=6, colors=24, post=belt_rim(v == "a"))
         for v in "ab"}


class Darts:
    """Launched Darts: 0.4 s straight along the launch heading, then a 60 deg/s chase for 3 s,
    then straight on and out (170 px/s)."""

    def __init__(self, target, speed=170.0):
        self.target, self.speed, self.list = target, speed, []

    def launch(self, t0, x, y, h0, kill=None):
        pos, h, path = np.array((x, y), dtype=np.float64), h0, []
        for k in range(int(6.0 * FPS)):
            t = t0 + k / FPS
            if kill is not None and t >= kill:
                break
            age = t - t0
            if 0.4 <= age < 3.4:
                tx, ty = self.target(t)
                want = np.arctan2(ty - pos[1], tx - pos[0])
                dh = (want - h + np.pi) % TAU - np.pi
                h += float(np.clip(dh, -np.radians(60) / FPS, np.radians(60) / FPS))
            pos = pos + np.array((np.cos(h), np.sin(h))) * self.speed / FPS
            path.append((t, pos.copy(), h))
            if not (-30 < pos[0] < FW + 30 and -40 < pos[1] < FH + 30):
                break
        self.list.append(dict(path=path, kill=kill))

    def draw(self, img, t, v):
        for dd in self.list:
            pts = [p for p in dd["path"] if p[0] <= t]
            if not pts:
                continue
            _, pos, h = pts[-1]
            if dd["kill"] is not None and t >= dd["kill"]:
                img = boom(img, pos[0], pos[1], (t - dd["kill"]) / 0.45, "tiny", n=6)
                continue
            if t > pts[-1][0] + 0.1 and pts[-1] is dd["path"][-1]:
                continue
            sprite.paste_center(img, darts[v].get(h), pos[0], pos[1])
            img = light(img, pos[0] - np.cos(h) * 9, pos[1] - np.sin(h) * 9, 3, (255, 150, 90), 0.5)
        return img


# ===================================================================== 3 Gilded Gunship (L30)

GUN_BAYS = [(0.52, 0.12), (0.52, -0.22)]                            # (x, y) bay doors, mirrored


def gunship_model(v, lamps=0.0, bays=0.0, glow=1.0):
    """The chosen r04 Gilded Gunship (em.gilded_gunship) facing +Y in the belt livery, with
    twelve gold ring lamps round the dome (the ring's tell), belly bay doors on the flanks (the
    Darts) and white-gold engines (the x2 weak points). a: the r04 gilded chevron plate; b: a
    gold scale plate over the whole front third and gold pod tops."""
    scene0, _ = em.gilded_gunship(0.0, glow)

    def scene(p):
        d, m = scene0(rotate_z(p, np.pi))
        q = mirror_x(p)
        if v == "b":
            m = np.where((m == A_HULL) & (q[:, 1] > 0.2), A_GOLD, m)
            m = np.where((m == A_FACET) & (q[:, 0] > 0.64) & (p[:, 2] > 0.02), A_GOLD, m)
        items = [(d, m)]
        for a in np.linspace(0, TAU, 12, endpoint=False):
            items.append((sd_sphere(p, (0.33 * np.cos(a), -0.08 + 0.33 * np.sin(a), 0.21), 0.035), A_X))
        for bx, by in GUN_BAYS:
            items.append((sd_box(q, (bx, by, 0.2), (0.07, 0.11, 0.02)), A_X + 1))
        return union(*items)
    extra = [emissive(GOLD_F, 0.12 + 1.6 * lamps), Material((0.08, 0.06, 0.06), metal=0.6, spec=0.9,
                                                           emission=tuple(em.RED * 1.4 * bays))]
    mats = belt_mats("e8e4f0", glow, extra=extra)
    mats[A_ENGINE] = emissive(WHITE_GOLD_F, 1.7 * glow)
    if v == "b":
        mats[A_GOLD] = replace(mats[A_GOLD], pattern=em._plates(4.0, 0.08))
    return scene, mats


GUN_N = 104
GUN_SCALE = GUN_N / 2.3
gunships = {v: Turning(lambda v=v, **kw: gunship_model(v, **kw), GUN_N, 4, factor=4, colors=48,
                       post=belt_rim(v == "a")) for v in "ab"}
GS_DUR = 10.0
GS_RINGS = [2.4, 5.4, 8.4]
GS_STREAMS = [3.2]
GS_KILL = 8.0


def gs_pos(t):
    return 240 + 20 * np.sin(t * 0.9), -40 + 45 * t


def gs_sprite(v, t):
    lamp = max([smooth((t - (r - 0.4)) / 0.4) if r - 0.4 <= t < r + 0.1 else 0.0 for r in GS_RINGS])
    bay = max([1.0 if s - 0.2 <= t < s + 0.8 else 0.0 for s in GS_STREAMS])
    return gunships[v].get(DOWN, lamps=round(lamp * 4) / 4, bays=bay)


def gs_scene(v):
    bg = Scroll(space_bg, 30, GS_DUR, 30)
    shots = Shots()
    for r in GS_RINGS:
        if r < GS_KILL:
            x, y = gs_pos(r)
            shots.ring(r, x, y, 12, 110, phase=0.13)
    px_of = follow(lambda t: 240 + 26 * np.sin(t * 1.3) if t < 5.0 else 330, GS_DUR, 240, 160)
    py_of = lambda t: 482 - 100 * smooth((t - 5.0) / 1.2)
    ddrones = Darts(lambda t: (px_of(t), py_of(t)))
    kills = {1: 4.6, 2: 5.1}
    for s_ in GS_STREAMS:
        for j in range(3):
            tl = s_ + 0.25 * j
            x, y = gs_pos(tl)
            side = (-1, 1, -1)[j]
            ddrones.launch(tl, x + side * 0.52 * GUN_SCALE, y + 6, DOWN - side * (0.9 - 0.3 * j),
                           kills.get(j))
    # two micro-missiles from the ship's wing mounts curl round to the engines
    mis = []
    for j, (t0, t1) in enumerate(((5.6, 7.0), (5.9, 7.35))):
        gx, gy = gs_pos(t1)
        ex, ey = gx + (-1, 1)[j] * 0.18 * GUN_SCALE, gy - 0.84 * GUN_SCALE
        sx, sy = px_of(t0) + (-1, 1)[j] * 14, py_of(t0) - 6
        mis.append((t0, t1, rig.catmull_rom([(sx, sy), (sx + (-1, 1)[j] * 70, sy - 150),
                                             (gx + (-1, 1)[j] * 110, gy - 40),
                                             (gx + (-1, 1)[j] * 40, ey - 70), (ex, ey)],
                                            np.linspace(t0, t1, 5))))
    frames = []
    for t in times(GS_DUR):
        f = bg.frame(t)
        px, py = px_of(t), py_of(t)
        gx, gy = gs_pos(min(t, GS_KILL))
        sp = gs_sprite(v, t)
        if t < GS_KILL:
            hit = any(t1 - 0.05 < t < t1 + 0.12 for _, t1, _ in mis)
            sprite.paste_center(f, flash(sp, hit), gx, gy)
            for side in (-1, 1):
                f = light(f, gx + side * 0.18 * GUN_SCALE, gy - 0.9 * GUN_SCALE, 7, (255, 230, 170), 0.7)
            if 0.8 < t < 5.0 and int(t * 10) % 3 == 0:                # front shots on the plate
                f = spark(f, px + (-6 if int(t * 7) % 2 else 6), gy + 0.8 * GUN_SCALE, (t * 10 % 3) / 3,
                          (200, 190, 150))
                label(f, gx + 40, gy + 30, "X0.5", (210, 200, 160))
            for _, t1, _ in mis:
                if t1 <= t < t1 + 0.5:
                    label(f, gx + 30, gy - 64, "X2 ENGINES", (255, 240, 200))
            if t < 7.2:
                f = shield(f, sp, gx, gy, "hug" if v == "a" else "bubble", 0.4,
                           hit=(0, 40) if 1.0 < t < 1.2 else None)
            elif t < 7.4:
                f = shield(f, sp, gx, gy, "hug", shatter=(t - 7.2) / 0.2)
        else:
            u = t - GS_KILL
            if u < 1.6:                                          # the hull breaks in two
                cut = int(sp.height * 0.42)
                front, back = sp.crop((0, cut, sp.width, sp.height)), sp.crop((0, 0, sp.width, cut))
                a = fade(rot_sprite(front, -25 * u), max(0, 1 - u / 1.6))
                b = fade(rot_sprite(back, 32 * u), max(0, 1 - u / 1.6))
                sprite.paste_center(f, a, gx - 10 * u, gy + 20 + 26 * u)
                sprite.paste_center(f, b, gx + 12 * u, gy - 22 - 18 * u)
                d = ImageDraw.Draw(f)
                for k in range(5):                                # gold plates spin off
                    ang = k * 1.3 + u * 9
                    r = 20 + 60 * u
                    cx, cy = gx + np.cos(k * 1.25) * r, gy + np.sin(k * 1.25) * r
                    d.line([cx - 4 * np.cos(ang), cy - 4 * np.sin(ang), cx + 4 * np.cos(ang), cy + 4 * np.sin(ang)],
                           fill=(230, 178, 60, int(255 * max(0, 1 - u / 1.6))), width=2)
            f = boom(f, gx, gy, u / 0.9, "medium")
        if t < GS_KILL:
            for r in GS_RINGS:
                if 0 <= t - r < 0.15:
                    f = light(f, gx, gy - 4, 18, (255, 220, 160), 0.8)
        shots.draw(f, t)
        f = ddrones.draw(f, t, v)
        d = ImageDraw.Draw(f)
        for t0, t1, path in mis:
            if t0 <= t < t1:
                x, y = path(t)
                x2, y2 = path(t - 0.05)
                d.line([x2, y2, x, y], fill=(150, 210, 255, 255), width=3)
                d.point((x, y), fill=(255, 255, 255, 255))
        ship(f, px, py, t, stop=gy + 40 if t < GS_KILL else None)
        if 0.4 < t < 2.2:
            caption(f, "FRONT SHOTS X0.5 ON THE GILDED PLATE")
        elif 5.6 < t < 7.4:
            caption(f, "MISSILES CURL ROUND: ENGINES TAKE X2")
        frames.append(f)
    return frames, bg


def gs_sheet(v, frames, bg):
    G = gunships[v]
    base = G.get(DOWN)
    panels = [("SOURCE, 260 PX", source_panel(lambda: gunship_model(v, lamps=1.0))),
              ("STATES (1X): IDLE / RING TELL / BAYS OPEN",
               row([base, G.get(DOWN, lamps=1.0), G.get(DOWN, bays=1.0)], GUN_N, 1)),
              ("NEXT TO A DART, A TALON AND THE PLAYER (1X)",
               row([darts[v].get(DOWN), talons[(v, False)].get(DOWN), base, e3.player_sprite()], GUN_N, 1)),
              ("READABILITY: OVER THE DARKEST L30 SPACE",
               readability([(base, gold_note(base))], bg.img, (1,))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.3, 3.6, 6.8, 8.3],
                                              ["RING TELL", "DART STREAM", "ENGINE HITS", "BREAKS IN TWO"]))]
    if v == "a":
        notes = ["GILDED GUNSHIP A - GILDED CHEVRON (THE CHOSEN R04 HULL, Q11 A LIVERY, STRONG GOLD RIM)",
                 "88X100 PX (104 PX CANVAS), FIXED, FACES DOWN. THE R04 GOLD CHEVRON PLATE ACROSS THE NOSE (FRONT",
                 "SHOTS SPARK GREY-GOLD: X0.5), TWO WHITE-GOLD ENGINE GLOWS AT THE REAR (X2, THE WEAK POINT), TWELVE",
                 "GOLD LAMPS ROUND THE DOME LIGHT 0.4 S BEFORE THE RING OF 12, THE FLANK BAY DOORS GLOW RED AS A",
                 "STREAM OF 3 DARTS LEAVES. HEX SHIELD 12 HUGS THE HULL. 45 PX/S DOWN WITH A 20 PX SWAY. KILLED,",
                 "THE HULL BREAKS IN TWO BEHIND THE PLATE AND GOLD PLATES SPIN OFF."]
    else:
        notes = ["GILDED GUNSHIP B - GOLD SCALE PLATE (THE CHOSEN R04 HULL, Q11 A LIVERY, 1 PX RIM)",
                 "THE WHOLE FRONT THIRD IS GOLD SCALE ARMOUR AND THE POD TOPS ARE GOLD: THE ARMOURED ARC READS",
                 "AS ONE BIG GOLD FACE, THE BLACK REAR AS THE OPEN BACK. ROUND HEX BUBBLE. SAME SCENE AS A."]
    return sheet(f"GILDED GUNSHIP {v.upper()} - PLATE, ENGINES, RING, DART BAYS (LEVEL 30)", panels, notes,
                 frames[int(3.7 * FPS)])


@unit("gilded-gunship")
def make_gunship(variants):
    for v in variants:
        frames, bg = gs_scene(v)
        save_unit("air/gilded-gunship", "gilded-gunship", v, gs_sheet(v, frames, bg), frames)


DD_DUR = 8.0


def dart_scene(v):
    bg = Scroll(space_bg, 31, DD_DUR, 30)
    px_of = follow(lambda t: 240 + 120 * np.sin(t * 0.8), DD_DUR, 240, 200)
    tgt = lambda t: (px_of(t), 480)
    dd = Darts(tgt)
    kills = {(0, 2): 2.9, (1, 1): 5.6, (1, 4): 6.2}
    for j in range(6):                                            # a stream from the top right
        dd.launch(0.5 + 0.3 * j, 400 - 8 * j, -14, DOWN + 0.35, kills.get((0, j)))
    for j in range(6):                                            # a stream from the left side
        dd.launch(3.6 + 0.3 * j, -14, 120 + 6 * j, 0.45, kills.get((1, j)))
    gx, gy = 250, 60
    for j in range(3):                                            # a Gunship's bays
        side = (-1, 1, -1)[j]
        dd.launch(5.2 + 0.25 * j, gx + side * 0.52 * GUN_SCALE, gy + 6, DOWN - side * (0.9 - 0.3 * j))
    frames = []
    for t in times(DD_DUR):
        f = bg.frame(t)
        y_g = gy - 140 + 140 * smooth((t - 4.0) / 1.0)
        if t > 4.0:
            sprite.paste_center(f, gunships[v].get(DOWN, bays=1.0 if 5.0 < t < 6.2 else 0.0), gx, y_g)
        f = dd.draw(f, t, v)
        ship(f, px_of(t), 480, t)
        if t < 2.0:
            caption(f, "DART STREAM: LOW TURN RATE, SIDESTEP IT")
        frames.append(f)
    return frames, bg


def dart_sheet(v, frames, bg):
    D = darts[v]
    heads = [D.frame(k) for k in range(16)]
    base = D.get(DOWN)
    panels = [("SOURCE, 260 PX", source_panel(lambda: dart_model(v))),
              ("16 HEADINGS (2X)", grid(heads, 8, 2)),
              ("NEXT TO A SKITTER-SIZED TALON AND THE PLAYER (2X)",
               row([base, talons[(v, False)].get(DOWN), e3.player_sprite()], 48, 2, ["DART", "TALON", "PLAYER"])),
              ("READABILITY: OVER THE DARKEST L30 SPACE",
               readability([(base, gold_note(base))], bg.img, (1, 2, 4), 40)),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.6, 4.4, 5.8], ["TOP STREAM", "SIDE STREAM", "GUNSHIP BAYS"]))]
    if v == "a":
        notes = ["DART DRONE A - NEEDLE DART (Q11 A LIVERY, STRONG GOLD RIM)",
                 "20X20 PX (22 PX CANVAS), 16 HEADINGS. A SLIM GLOSS-BLACK NEEDLE, A GOLD NOSE CONE, SMALL SWEPT",
                 "GOLD FINS, ONE RED LIGHT, A HOT TAIL. NO SHIELD, NO LEADER. 0.4 S OUT OF THE BAY, A 60 DEG/S",
                 "CHASE FOR 3 S, THEN STRAIGHT ON AND OUT: A STREAM SWEEPS PAST RATHER THAN TRACKS. DEATH: A",
                 "TINY SPARK PUFF WITH GOLD FLECKS."]
    else:
        notes = ["DART DRONE B - CHEVRON DART (Q11 A LIVERY, 1 PX RIM)",
                 "A BLACK ARROWHEAD WITH GOLD LEADING EDGES AND A RED LIGHT IN THE NOTCH, TWIN TAIL GLOWS: A WIDER",
                 "READ AT 20 PX (A GOLD 'V' POINTING WHERE IT FLIES). SAME SCENE AS A."]
    return sheet(f"DART DRONE {v.upper()} - THE ASCENDANCY'S POPCORN (LEVEL 30)", panels, notes,
                 frames[int(4.6 * FPS)])


@unit("dart-drone")
def make_dart(variants):
    for v in variants:
        frames, bg = dart_scene(v)
        save_unit("air/dart-drone", "dart-drone", v, dart_sheet(v, frames, bg), frames)


# ===================================================================== 4 Hornet (L31)

def hornet_model(v, hatch=0.0, glow=1.0):
    """Hornet missile boat (faces +Y, hovers facing down). a: twin-pod boat - a slim black
    fuselage, gold canards and spine, two fat missile pods on short pylons with gold nose caps and
    two hatches each; b: delta hover-boat - a broad black delta with gold leading edges, four lift
    fans and the missile cells recessed in the wing roots. The hatches glow gold with ``hatch``."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            items = [
                (sd_plate(q, [(0.0, 0.95), (0.2, 0.7), (0.26, -0.2), (0.2, -0.75), (0.0, -0.85)], 0.0, 0.16,
                          0.0, taper=am._roof(1.6)), A_HULL),
                (sd_plate(q, [(0.2, 0.52), (0.58, 0.3), (0.56, 0.16), (0.22, 0.12)], 0.02, 0.04), A_GOLD),
                (sd_box(q, (0.45, -0.05, 0.02), (0.22, 0.09, 0.04), 0.02), A_GOLD),
                (sd_capsule(q, (0.72, -0.32, 0.04), (0.72, -0.68, 0.04), 0.2, 0.09), A_GOLD),
                (sd_capsule(q, (0.72, 0.5, 0.04), (0.72, -0.58, 0.04), 0.21, 0.19), A_HULL),
                (sd_capsule(q, (0.72, 0.48, 0.04), (0.72, 0.74, 0.04), 0.19, 0.06), A_GOLD),
                (sd_box(p, (0, 0.0, 0.15), (0.035, 0.6, 0.03)), A_GOLD),
                (sd_plate(p, [(0, 0.66), (0.1, 0.4), (0, 0.28), (-0.1, 0.4)], 0.16, 0.03), A_GLASS),
                (sd_sphere(q, (0.72, -0.62, 0.16), 0.05), A_RED),
                (sd_cylinder_y(q, (0.12, -0.86, 0.0), 0.08, 0.05), A_ENGINE),
            ]
            for hy in (0.25, -0.12):
                items.append((sd_box(q, (0.72, hy, 0.23), (0.1, 0.11, 0.025)), A_X))
            return union(*items, k=0.02)
        delta = [(0.0, 1.0), (0.95, -0.5), (0.82, -0.72), (0.0, -0.6)]
        items = [(sd_plate(q, delta, 0.0, 0.12, 0.0, taper=am._roof(0.9)), A_HULL)]
        d, m = union(*items)
        lead = (q[:, 1] > 1.0 - 1.58 * q[:, 0] - 0.3) & (m == A_HULL)
        m = np.where(lead, A_GOLD, m)
        d, m = subtract((d, m), np.minimum(sd_cylinder_z(q, (0.42, -0.3, 0.1), 0.12, 0.12),
                                           sd_cylinder_z(q, (0.22, 0.22, 0.1), 0.12, 0.12)))
        items = [(d, m),
                 (sd_cylinder_z(q, (0.42, -0.3, -0.02), 0.13, 0.03), A_FACET),
                 (sd_cylinder_z(q, (0.22, 0.22, -0.02), 0.13, 0.03), A_FACET),
                 (sd_box(q, (0.42, -0.3, 0.0), (0.12, 0.02, 0.04)), A_GUN),
                 (sd_box(q, (0.22, 0.22, 0.0), (0.02, 0.12, 0.04)), A_GUN),
                 (sd_plate(p, [(0, 0.7), (0.09, 0.45), (0, 0.32), (-0.09, 0.45)], 0.12, 0.03), A_GLASS),
                 (sd_sphere(q, (0.86, -0.55, 0.06), 0.05), A_RED),
                 (sd_cylinder_y(q, (0.1, -0.6, 0.0), 0.08, 0.05), A_ENGINE)]
        for hx_, hy in ((0.55, 0.05), (0.66, -0.12)):
            items.append((sd_box(q, (hx_, hy, 0.12), (0.06, 0.07, 0.02)), A_X))
        return union(*items)
    return scene, belt_mats("c8202a", glow, extra=[emissive(GOLD_F, 0.08 + 1.8 * hatch)])


HOR_N = 76
HOR_SCALE = HOR_N / 2.3
hornets = {v: Turning(lambda v=v, **kw: hornet_model(v, **kw), HOR_N, 4, factor=4, colors=40,
                      post=belt_rim(v == "a")) for v in "ab"}
HOR_DUR = 10.0
HOR = [dict(x=150, y=118, t0=0.2, salvos=[2.4, 7.4], kill=7.6, pod=0.72),
       dict(x=330, y=142, t0=0.8, salvos=[3.0, 8.0], kill=None, pod=0.72)]


def hornet_pos(h, t):
    y0 = -50 + 140 * (t - h["t0"])
    y = min(h["y"], y0) if t >= h["t0"] else -60
    x = h["x"] + 20 * np.sin((t - h["t0"]) * 0.6)
    return x, y


def pod_xy(v, h, side, t):
    x, y = hornet_pos(h, t)
    px = 0.72 if v == "a" else 0.6
    return x + side * px * HOR_SCALE, y + 0.2 * HOR_SCALE


def hornet_scene(v):
    bg = Scroll(space_bg, 32, HOR_DUR, 30)
    px_of = follow(lambda t: 250 + 110 * np.sin(t * 0.7 + 0.5), HOR_DUR, 240, 180)
    tgt = lambda t: (px_of(t), 478)
    ms = Homing(v, tgt)
    kills = {(0, 0, 1): 4.3, (0, 0, 2): 4.6, (1, 0, 0): 5.0}
    for i, h in enumerate(HOR):
        for si, s_ in enumerate(h["salvos"]):
            if h["kill"] and s_ >= h["kill"]:
                continue
            for j in range(4):
                side = (-1, 1)[j % 2]
                x, y = pod_xy(v, h, side, s_ + 0.2 * j)
                ms.launch(s_ + 0.2 * j, x, y + 6, DOWN, kills.get((i, si, j)))
    frames = []
    for t in times(HOR_DUR):
        f = bg.frame(t)
        for k, (rx, ry, r0) in enumerate(((60, 120, 0), (420, -60, 1), (200, -260, 2))):
            sprite.paste_center(f, ROCK_L[r0], rx, ry + 52 * t)
        for i, h in enumerate(HOR):
            x, y = hornet_pos(h, t)
            if h["kill"] and t >= h["kill"]:
                f = boom(f, x, y, (t - h["kill"]) / 0.9, "medium")
                for side in (-1, 1):
                    f = light(f, x + side * 0.7 * HOR_SCALE, y, 10, (255, 230, 170), 1.2 * max(0, 1 - (t - h["kill"]) / 0.3))
                continue
            hatch = max([smooth((t - (s_ - 0.5)) / 0.3) if s_ - 0.5 <= t < s_ + 0.7 else 0 for s_ in h["salvos"]])
            sp = hornets[v].get(DOWN, hatch=round(hatch * 4) / 4)
            on = h["kill"] and h["kill"] - 0.6 < t and int(t * 16) % 2
            sprite.paste_center(f, flash(sp, on), x, y)
            if h["kill"] and h["kill"] - 0.9 < t < h["kill"] - 0.6:
                f = shield(f, sp, x, y, "hug" if v == "a" else "bubble", 0.4, hit=(0, 20))
            elif h["kill"] and h["kill"] - 0.6 <= t < h["kill"] - 0.4:
                f = shield(f, sp, x, y, "hug", shatter=(t - h["kill"] + 0.6) / 0.2)
            elif not h["kill"] or t < h["kill"] - 0.9:
                f = shield(f, sp, x, y, "hug" if v == "a" else "bubble", 0.4)
        f = ms.draw(f, t)
        ship(f, px_of(t), 478, t)
        if 2.0 < t < 3.6:
            caption(f, "HATCHES GLOW GOLD: A SALVO OF 4 HOMING DIAMONDS")
        elif 4.2 < t < 5.4:
            caption(f, "SHOOTABLE: 2 HP, SPREAD CLEARS A SALVO")
        frames.append(f)
    return frames, bg


def hornet_sheet(v, frames, bg):
    H = hornets[v]
    base = H.get(DOWN)
    panels = [("SOURCE, 260 PX", source_panel(lambda: hornet_model(v, hatch=1.0))),
              ("IDLE / HATCH TELL (2X)", row([base, H.get(DOWN, hatch=1.0)], HOR_N, 2)),
              ("WITH A HOMING DIAMOND, A TALON, THE PLAYER (2X)",
               row([base, diamond_at(v, DOWN), talons[(v, False)].get(DOWN), e3.player_sprite()], HOR_N, 2)),
              ("READABILITY: OVER THE DARKEST L31 SPACE", readability([(base, gold_note(base))], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.2, 3.6, 4.4, 7.7],
                                              ["HATCH TELL", "SALVOS", "SHOT DOWN", "KILL, COOK-OFF"]))]
    if v == "a":
        notes = ["HORNET A - TWIN-POD BOAT (Q11 A LIVERY, STRONG GOLD RIM)",
                 "72X64 PX (76 PX CANVAS), FIXED, FACES DOWN. A SLIM BLACK FUSELAGE WITH A GOLD SPINE AND CANARDS,",
                 "TWO FAT MISSILE PODS ON SHORT PYLONS WITH GOLD NOSE CAPS; TWO HATCHES A POD GLOW GOLD 0.5 S",
                 "BEFORE A SALVO OF 4 (PODS ALTERNATING, 0.2 S APART: 110 PX/S, 80 DEG/S, 4 S, 2 HP). HEX SHIELD 8.",
                 "HOVERS 90-150 PX BELOW THE TOP WITH A 20 PX/S DRIFT. KILLED: A MEDIUM BLAST, THE PODS COOK OFF."]
    else:
        notes = ["HORNET B - DELTA HOVER-BOAT (Q11 A LIVERY, 1 PX RIM)",
                 "A BROAD BLACK DELTA WITH GOLD LEADING EDGES, FOUR GOLD-RIMMED LIFT FANS (IT HOVERS), THE",
                 "MISSILE CELLS RECESSED IN THE WING ROOTS (THE HATCH GLOW SITS NEARER THE HULL). READS AS A",
                 "FLYING WING, NOT A GUNBOAT. ROUND HEX BUBBLE. SAME SCENE AS A."]
    return sheet(f"HORNET {v.upper()} - MISSILE BOAT, HOMING DIAMOND SALVOS (LEVEL 31)", panels, notes,
                 frames[int(3.9 * FPS)])


@unit("hornet")
def make_hornet(variants):
    for v in variants:
        frames, bg = hornet_scene(v)
        save_unit("air/hornet", "hornet", v, hornet_sheet(v, frames, bg), frames)


# ===================================================================== 5 Buzzsaw Drone (L31)

def buzzsaw_model(v, glow=1.0):
    """The chosen r05 Buzzsaw (am.buzzsaw) in the belt livery. a: gunmetal blades with gold tips
    and the gold hub ring (the r05 look), strong rim; b: solid gold blades round a black hub."""
    scene0, _ = am.buzzsaw(glow)

    def scene(p):
        d, m = scene0(p)
        r = np.hypot(p[:, 0], p[:, 1])
        if v == "a":
            m = np.where((m == A_GUN) & (r > 0.78), A_GOLD, m)
        else:
            m = np.where(m == A_GUN, A_GOLD, np.where(m == A_GOLD, A_HULL, m))
        return d, m
    return scene, belt_mats("6a6e78", glow)


SAW_N = 42
saws = {v: Turning(lambda v=v: buzzsaw_model(v), SAW_N, 48, factor=6, colors=32, sym=6,
                   post=belt_rim(v == "a")) for v in "ab"}


def redden(sp, k=0.75):
    """The spin-up tell: the gold rim and gold parts flicker red."""
    a = np.array(sp).astype(np.float64)
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    gold = (a[..., 3] > 0) & (r > 110) & (g > 0.42 * r) & (b < 0.66 * g)
    a[gold, :3] = a[gold, :3] * (1 - k) + np.array(CRIMSON) * k
    return Image.fromarray(a.astype(np.uint8), "RGBA")


SAW_DUR = 10.0
SAW_ROCKS = [(110, 150, 0, 40), (370, 300, 1, 40)]       # x, y at t=0, sprite, radius (drift 24 px/s)


def saw_rocks(t):
    return [(x, y + 24 * t, k, r) for x, y, k, r in SAW_ROCKS]


def saw_sim(px_of, start, t_phase, kill=None):
    """One drone's states over time: chase 2.5 s (120 deg/s, 150 px/s), spin-up tell 0.6 s
    (40 px/s, bearing locked at its start), dash 440 px/s with up to 3 ricochets, slow to
    120 px/s over 0.5 s, chase again; after the second dash it leaves."""
    pos, h = np.array(start, dtype=np.float64), DOWN
    out, state, st, dashes, bounces, lock, spin = [], "chase", -t_phase, 0, 0, 0.0, 0.0
    sub = 4
    for k in range(int(SAW_DUR * FPS)):
        t = k / FPS
        for _ in range(sub):
            dt = 1.0 / FPS / sub
            st += dt
            tx, ty = px_of(t), 478
            if state == "chase":
                want = np.arctan2(ty - pos[1], tx - pos[0])
                dh = (want - h + np.pi) % TAU - np.pi
                h += float(np.clip(dh, -np.radians(120) * dt, np.radians(120) * dt))
                spd, rate = 150.0, 2.0
                if st >= 2.5:
                    state, st, lock = "tell", 0.0, float(np.arctan2(ty - pos[1], tx - pos[0]))
            elif state == "tell":
                spd, rate = 40.0, 2.0 + 4.0 * st / 0.6
                if st >= 0.6:
                    state, st, h, bounces = "dash", 0.0, lock, 0
            elif state == "dash":
                spd, rate = 440.0, 6.0
            elif state == "slow":
                spd, rate = 440 - 320 * min(1, st / 0.5), 4.0
                if st >= 0.5:
                    state, st = ("leave" if dashes >= 2 else "chase"), 0.0
            else:
                spd, rate = 200.0, 2.0
            vel = np.array((np.cos(h), np.sin(h))) * spd
            pos = pos + vel * dt
            spin += rate * TAU * dt
            if state == "dash":
                hit = False
                if pos[0] < 18 or pos[0] > FW - 18:
                    vel[0] *= -1
                    pos[0] = np.clip(pos[0], 18, FW - 18)
                    hit = True
                if pos[1] < 18 or pos[1] > FH - 18:
                    vel[1] *= -1
                    pos[1] = np.clip(pos[1], 18, FH - 18)
                    hit = True
                for rx, ry, _, rr in saw_rocks(t):
                    dd = pos - np.array((rx, ry))
                    dist = np.linalg.norm(dd)
                    if dist < rr + 16:
                        n = dd / dist
                        if vel @ n < 0:
                            vel = vel - 2 * (vel @ n) * n
                            pos = np.array((rx, ry)) + n * (rr + 16)
                            hit = True
                if hit:
                    h = float(np.arctan2(vel[1], vel[0]))
                    bounces += 1
                    out.append(("bounce", t, pos.copy()))
                    if bounces >= 3:
                        state, st, dashes = "slow", 0.0, dashes + 1
                elif st > 1.6:
                    state, st, dashes = "slow", 0.0, dashes + 1
        out.append((state, t, pos.copy(), h, spin, st, lock))
        if kill is not None and t >= kill:
            break
    return out


def saw_scene(v):
    bg = Scroll(space_bg, 33, SAW_DUR, 24)
    px_of = follow(lambda t: 240 + 150 * np.sin(t * 0.9 + 0.4), SAW_DUR, 240, 230, 0.25)
    sims = [saw_sim(px_of, (90, -20), 0.0), saw_sim(px_of, (400, -40), 1.4, kill=8.6)]
    frames = []
    trails = {0: [], 1: []}
    for k, t in enumerate(times(SAW_DUR)):
        f = bg.frame(t)
        for rx, ry, ri, _ in saw_rocks(t):
            sprite.paste_center(f, ROCK_L[ri], rx, ry)
        for i, sim in enumerate(sims):
            states = [s_ for s_ in sim if s_[0] != "bounce"]
            if k >= len(states):
                pos = states[-1][2]
                f = boom(f, pos[0], pos[1], (t - 8.6) / 0.5, "tiny", n=6)
                d = ImageDraw.Draw(f)
                u = t - 8.6
                for j in range(3):                                # the blades fly off as sparks
                    a = j * TAU / 3 + 0.4
                    cx, cy = pos[0] + np.cos(a) * 70 * u, pos[1] + np.sin(a) * 70 * u
                    if u < 1.0:
                        aa = u * 20 + j
                        d.line([cx - 4 * np.cos(aa), cy - 4 * np.sin(aa), cx + 4 * np.cos(aa), cy + 4 * np.sin(aa)],
                               fill=(230, 200, 140, int(255 * (1 - u))), width=2)
                continue
            state, _, pos, h, spin, st, lock = states[k]
            sp = saws[v].get(spin)
            if state == "tell" and int(t * 14) % 2:
                sp = redden(sp)
            trails[i] = (trails[i] + [(pos.copy(), spin)])[-5:]
            if state in ("dash", "slow"):
                for j, (q, sp_) in enumerate(trails[i][:-1]):
                    sprite.paste_center(f, fade(saws[v].get(sp_), 0.12 + 0.1 * j), q[0], q[1])
            if state == "tell":
                f = light(f, pos[0], pos[1], 14, (255, 60, 50), 0.35 + 0.3 * st / 0.6)
            sprite.paste_center(f, sp, pos[0], pos[1])
        for i, sim in enumerate(sims):
            for s_ in sim:
                if s_[0] == "bounce" and 0 <= t - s_[1] < 0.3:
                    f = spark(f, s_[2][0], s_[2][1], (t - s_[1]) / 0.3, (255, 230, 190), 6, 12)
        ship(f, px_of(t), 478, t, shots=t > 7.6)
        if any(s_[k][0] == "tell" for s_ in sims if k < len([x for x in s_ if x[0] != "bounce"])
               for s_ in [[x for x in s_ if x[0] != "bounce"]]):
            caption(f, "SPIN-UP 0.6 S: THE DASH LOCKS ITS BEARING NOW")
        frames.append(f)
    return frames, bg


def saw_sheet(v, frames, bg):
    S = saws[v]
    spin = [S.frame(k) for k in range(8)]
    base = S.frame(0)
    panels = [("SOURCE, 260 PX", source_panel(lambda: buzzsaw_model(v))),
              ("SPIN: 8 FRAMES COVER 60 DEG (2X)", grid(spin, 8, 2)),
              ("SPIN-UP TELL: RIM FLICKERS RED (2X)", row([base, redden(base)], SAW_N + 6, 2, ["", "TELL"])),
              ("NEXT TO A TALON AND THE PLAYER (2X)",
               row([base, talons[(v, False)].get(DOWN), e3.player_sprite()], 48, 2)),
              ("READABILITY: OVER THE DARKEST L31 SPACE", readability([(base, gold_note(base))], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.6, 3.4, 4.0, 8.8],
                                              ["TELL", "DASH", "RICOCHET", "KILLED"]))]
    if v == "a":
        notes = ["BUZZSAW DRONE A - GUNMETAL BLADES, GOLD TIPS (THE CHOSEN R05 DRONE, Q11 A LIVERY, STRONG RIM)",
                 "36X36 PX (42 PX CANVAS), RADIAL: SIX HOOKED GUNMETAL BLADES WITH GOLD TIPS ROUND A GOLD HUB RING",
                 "AND A RED EYE. CHASES 2.5 S (120 DEG/S), THEN THE 0.6 S SPIN-UP: IT SLOWS, SPINS 2 -> 6 TURNS/S AND",
                 "THE GOLD FLICKERS RED; THE DASH FOLLOWS THE BEARING AT THE TELL'S START (A MOVING SHIP IS MISSED),",
                 "RICOCHETS OFF THE EDGES AND THE BIG ROCKS UP TO 3 TIMES (AN AFTER-IMAGE TRAIL), SLOWS, CHASES AGAIN.",
                 "KILLED: THE BLADES FLY OFF AS THREE SPINNING SPARKS."]
    else:
        notes = ["BUZZSAW DRONE B - GOLD BLADES (THE CHOSEN R05 DRONE, Q11 A LIVERY, 1 PX RIM)",
                 "SIX SOLID GOLD BLADES ROUND A BLACK HUB WITH THE RED EYE: A SPINNING GOLD STAR, THE MOST GOLD",
                 "OF THE ROSTER; THE RED TELL FLICKER COVERS THE WHOLE DISC. SAME SCENE AS A."]
    return sheet(f"BUZZSAW DRONE {v.upper()} - SPIN-UP, DASH, RICOCHET (LEVEL 31)", panels, notes,
                 frames[int(3.5 * FPS)])


@unit("buzzsaw-drone")
def make_saw(variants):
    for v in variants:
        frames, bg = saw_scene(v)
        save_unit("air/buzzsaw-drone", "buzzsaw-drone", v, saw_sheet(v, frames, bg), frames)


# ===================================================================== 6 Rail Serpent (L30)

def rail_model(v, kind="car", lamp=0.0, glow=1.0):
    """Rail Serpent cars on the chosen r05 car (am.rail_car), facing +Y, in the belt livery.
    kind: engine (the r05 head with a white-gold reactor glow, the vital weak point), car,
    turret (a domed twin gun with a red lamp, the tell), tail (gold fins, the bonus).
    a: the r05 gold spine bar plus gold side skirts, strong rim; b: gold roof armour with a black
    centre stripe (couplers drawn as gold links in the scene)."""
    scene0, _ = am.rail_car(kind == "engine", glow)

    def scene(p):
        d, m = scene0(p)
        q = mirror_x(p)
        if v == "a":
            m = np.where((m == A_FACET) & (q[:, 0] > 0.5), A_GOLD, m)
        else:
            m = np.where((m == A_HULL) & (p[:, 2] > 0.18) & (q[:, 0] > 0.12), A_GOLD,
                         np.where(m == A_GOLD, A_HULL, m))
        items = [(d, m)]
        if kind == "engine":
            items.append((sd_cylinder_z(p, (0, -0.15, 0.24), 0.16, 0.04), A_X))
        elif kind == "turret":
            items += [(sd_sphere(p, (0, 0.0, 0.24), 0.24), A_FACET),
                      (sd_capsule(q, (0.07, 0.1, 0.38), (0.07, 0.55, 0.38), 0.035, 0.03), A_GUN),
                      (sd_sphere(p, (0, -0.1, 0.46), 0.06), A_X + 1)]
        elif kind == "tail":
            items.append((sd_plate(q, [(0.3, -0.3), (0.72, -0.72), (0.66, -0.86), (0.3, -0.6)], 0.1, 0.03),
                          A_GOLD))
        return union(*items)
    extra = [emissive(WHITE_GOLD_F, 1.8 * glow), emissive(np.array(CRIMSON) / 255.0, 0.15 + 1.8 * lamp)]
    return scene, belt_mats("c8202a", glow, extra=extra)


RAIL_SIZE = {"engine": 64, "car": 48, "turret": 48, "tail": 44}
rails = {(v, k): Turning(lambda v=v, k=k, **kw: rail_model(v, k, **kw), RAIL_SIZE[k], 32, factor=4,
                         colors=32, post=belt_rim(v == "a")) for v in "ab" for k in RAIL_SIZE}
RAIL_KIND = ["engine", "car", "car", "turret", "car", "car", "turret", "car", "car", "turret", "tail"]
RAIL_GAP = [44] + [38] * 8 + [36, 34]
RAIL_PATH = rig.catmull_rom([(-120, 150), (-40, 145), (120, 150), (280, 200), (390, 300), (340, 410),
                             (200, 400), (150, 290), (250, 190), (420, 160), (600, 150), (760, 150)],
                            [0, 0.5, 1.4, 2.4, 3.3, 4.1, 4.9, 5.7, 6.5, 7.5, 8.5, 9.4])
RAIL_T0 = 1.2                                                    # the engine crosses the left edge
RAIL_CUT = (5, 5.4)                                              # car index, time
RAIL_DUR = 10.5


def rail_chain(t):
    """Car positions and headings; cars not yet past the path's start queue up off the left edge."""
    pts, heads = rig.chain_at(RAIL_PATH, t - RAIL_T0 + 0.6, RAIL_GAP[:len(RAIL_KIND) - 1])
    pts, heads = list(pts), list(heads)
    while len(pts) < len(RAIL_KIND):
        g = RAIL_GAP[len(pts) - 1]
        pts.append(pts[-1] - np.array((g, 0.0)))
        heads.append(0.0)
    return pts, heads


def rail_scene(v):
    bg = Scroll(space_bg, 34, RAIL_DUR, 30)
    px_of = follow(lambda t: 240 + 60 * np.sin(t * 0.8) if t < 4.4 else 330 + 30 * np.sin(t), RAIL_DUR, 240, 200)
    shots = Shots()
    turret_fire = {}
    for ci, kind in enumerate(RAIL_KIND):
        if kind != "turret":
            continue
        n = ci // 3 - 1
        turret_fire[ci] = [t0 for t0 in np.arange(2.0 + 0.6 * n, RAIL_DUR, 2.4)]
    cut_i, cut_t = RAIL_CUT
    pts_c, heads_c = rail_chain(cut_t)
    for ci, ts in turret_fire.items():
        for t0 in ts:
            if ci >= cut_i and t0 >= cut_t:
                continue
            pts, _ = rail_chain(t0)
            x, y = pts[ci]
            if 0 < x < FW and 0 < y < FH:
                shots.fire(t0, x, y, px_of(t0), 478, 160)
    frames = []
    for t in times(RAIL_DUR):
        f = bg.frame(t)
        pts, heads = rail_chain(t)
        d = ImageDraw.Draw(f)
        order = list(range(len(RAIL_KIND) - 1, -1, -1))
        if v == "b":
            for ci in range(len(RAIL_KIND) - 1):
                if t >= cut_t and ci >= cut_i - 1:
                    continue
                d.line([tuple(pts[ci]), tuple(pts[ci + 1])], fill=(150, 112, 40, 255), width=3)
        for ci in order:
            kind = RAIL_KIND[ci]
            if t >= cut_t and ci >= cut_i:
                u = t - cut_t
                x0, y0 = pts_c[ci]
                hh = heads_c[ci]
                if ci == cut_i:
                    f = boom(f, x0, y0, u / 0.9, "medium")
                    continue
                travel = 170 * 0.4 * (u - u * u / 3.0) if u < 1.5 else 170 * 0.4 * 0.75
                x, y = x0 + np.cos(hh) * travel, y0 + np.sin(hh) * travel
                tb = 1.5 + 0.1 * (ci - cut_i - 1)
                if u >= tb:
                    f = boom(f, x, y, (u - tb) / 0.8, "medium" if kind != "car" else "small")
                    continue
                sp = rails[(v, kind)].get(hh + 0.15 * u * (1 if ci % 2 else -1), lamp=0.0)
                sprite.paste_center(f, tint(sp, (20, 18, 24), 0.35), x, y)
                if int(t * 20 + ci) % 3 == 0:
                    f = spark(f, x + 8 * np.sin(ci * 3 + t * 9), y + 6 * np.cos(ci + t * 7), 0.3, (255, 210, 150), 4, 6)
                continue
            x, y = pts[ci]
            if not (-40 < x < FW + 40 and -40 < y < FH + 40):
                continue
            lamp = 0.0
            if kind == "turret":
                lamp = 1.0 if any(t0 - 0.2 <= t < t0 for t0 in turret_fire[ci]) else 0.0
            sprite.paste_center(f, rails[(v, kind)].get(heads[ci], lamp=lamp), x, y)
            if kind == "engine":
                f = light(f, x, y, 8, (255, 236, 180), 0.5)
        if cut_t - 0.6 < t < cut_t:
            x, y = rail_chain(t)[0][cut_i]
            if int(t * 16) % 2:
                f = light(f, x, y, 10, (255, 255, 255), 0.6)
        shots.draw(f, t)
        ship(f, px_of(t), 478, t)
        if t < 1.6:
            side_warning(f, t, -1, [150])
        if 5.4 < t < 7.4:
            caption(f, "SPLIT: THE CARS BEHIND THE CUT LOSE POWER")
        frames.append(f)
    return frames, bg


def rail_sheet(v, frames, bg):
    R = {k: rails[(v, k)] for k in RAIL_SIZE}
    eng = [R["engine"].frame(k) for k in range(0, 32, 2)]
    parts = [R["engine"].get(DOWN), R["car"].get(DOWN), R["turret"].get(DOWN), R["turret"].get(DOWN, lamp=1.0),
             R["tail"].get(DOWN)]
    panels = [("SOURCE: THE ENGINE CAR, 260 PX", source_panel(lambda: rail_model(v, "engine"))),
              ("ENGINE CAR: 16 OF 32 HEADINGS (1X)", grid(eng, 8, 1)),
              ("ENGINE / CAR / TURRET / TELL / TAIL (2X)",
               row(parts, 64, 2, ["ENGINE", "CAR", "TURRET", "LAMP", "TAIL"])),
              ("READABILITY: OVER THE DARKEST L30 SPACE",
               readability([(parts[0], gold_note(parts[0])), (parts[1], gold_note(parts[1]))], bg.img, (1,))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.8, 5.5, 6.5, 7.6],
                                              ["TURRET CARS", "CUT", "REAR HALF DRIFTS", "FRONT RUNS ON"]))]
    if v == "a":
        notes = ["RAIL SERPENT A - GOLD SPINE AND SKIRTS (THE CHOSEN R05 CARS, Q11 A LIVERY, STRONG RIM)",
                 "ENGINE 64 PX, CARS 48 PX, 32 HEADINGS EACH. THE R05 BLACK CARS WITH THE GOLD SPINE BAR AND GOLD SIDE",
                 "SKIRTS; THE ENGINE'S WHITE-GOLD REACTOR GLOW IS THE VITAL WEAK POINT; EVERY THIRD CAR A DOMED",
                 "TURRET WHOSE RED LAMP LIGHTS 0.2 S BEFORE ITS AIMED SHOT; GOLD TAIL FINS (THE BONUS). ENTERS FROM",
                 "A SIDE (EDGE-WARNED). A CAR SHOT OUT CUTS THE TRAIN: THE CARS BEHIND LOSE POWER, DRIFT AT 40 %",
                 "SLOWING, DARK AND SPARKING, 1.5 S, THEN BLOW FROM THE CUT BACKWARDS; THE FRONT HALF RUNS ON."]
    else:
        notes = ["RAIL SERPENT B - GOLD ROOFS AND LINKS (THE CHOSEN R05 CARS, Q11 A LIVERY, 1 PX RIM)",
                 "GOLD ROOF ARMOUR WITH A BLACK CENTRE STRIPE ON EVERY CAR AND GOLD COUPLER LINKS BETWEEN THEM: THE",
                 "TRAIN READS AS ONE GOLD CHAIN, AND A CUT SHOWS AS A MISSING LINK. SAME SCENE AS A."]
    return sheet(f"RAIL SERPENT {v.upper()} - DRONE TRAIN AND THE MACHINE SPLIT (LEVEL 30)", panels, notes,
                 frames[int(6.2 * FPS)])


@unit("rail-serpent")
def make_rail(variants):
    for v in variants:
        frames, bg = rail_scene(v)
        save_unit("air/rail-serpent", "rail-serpent", v, rail_sheet(v, frames, bg), frames)


# ===================================================================== 7 Asteroid Mite (L29, Vrell, tiny)

PLUM = ("a020a8", "40004a", "c890d8")


def mite_model(v, phase=0.0, pose="cling", glow=1.0):
    """Asteroid Mite (Vrell swarm fodder: plum chitin, teal contact glow), faces +Y.
    a: tick - a round plated abdomen with a teal seam, a small head with bone mandibles, six short
    splayed legs; b: flea - a long flat segmented body with teal spots and two big folded hind
    legs (a leaper). Poses: cling (legs out, ``phase`` walks them), crouch (the tell: tucked,
    the glow flares), leap (legs trail back)."""
    reach = {"cling": 1.0, "crouch": 0.6, "leap": 0.8}[pose]
    back = 0.55 if pose == "leap" else 0.0

    def scene(p):
        q = mirror_x(p)
        side = np.where(p[:, 0] >= 0, 1.0, -1.0)
        items = []
        if v == "a":
            items += [(sd_ellipsoid(p, (0, -0.18, 0.12), (0.46, 0.52, 0.3)), V_SEAM),
                      (sd_ellipsoid(p, (0, 0.46, 0.08), (0.24, 0.2, 0.15)), V_DARK),
                      (sd_capsule(q, (0.08, 0.6, 0.06), (0.14, 0.86, 0.02), 0.05, 0.02), V_BONE),
                      (sd_sphere(q, (0.1, 0.55, 0.18), 0.05), V_EYE)]
            for k, (ly, ang) in enumerate(((0.3, 0.5), (0.05, 0.0), (-0.2, -0.5))):
                sw = 0.18 * np.sin(phase + k * 2.1) * (pose == "cling")
                ex = 0.45 + 0.42 * reach
                ey = ly + np.sin(ang) * 0.5 * reach + sw - back * (k + 1) * 0.3
                items.append((sd_capsule(q, (0.3, ly, 0.08), (ex, ey, 0.0), 0.06, 0.035), V_BODY))
        else:
            for k in range(4):
                y = 0.45 - 0.3 * k
                items.append((sd_ellipsoid(p, (0, y, 0.08), (0.28 - 0.03 * k, 0.2, 0.15)), V_BODY))
            items += [(sd_sphere(q, (0.1, 0.25 - 0.3 * k, 0.21), 0.05), V_GLOW) for k in range(3)]
            items += [(sd_ellipsoid(p, (0, 0.7, 0.06), (0.15, 0.14, 0.1)), V_DARK),
                      (sd_sphere(q, (0.07, 0.76, 0.14), 0.04), V_EYE)]
            kx = 0.42 + 0.2 * reach
            items += [(sd_capsule(q, (0.2, -0.05, 0.12), (kx, 0.22 - back, 0.22), 0.09, 0.07), V_BONE),
                      (sd_capsule(q, (kx, 0.22 - back, 0.22), (0.3 + 0.1 * reach, -0.8 - back * 0.6, 0.02),
                                  0.07, 0.03), V_BODY)]
            for ly in (0.55, 0.3):
                sw = 0.15 * np.sin(phase + ly * 6) * (pose == "cling")
                items.append((sd_capsule(q, (0.2, ly, 0.06), (0.25 + 0.35 * reach, ly + 0.25 + sw, 0.0),
                                         0.045, 0.03), V_BODY))
        return union(*items, k=0.04)
    flare = 2.0 if pose == "crouch" else 1.0
    return scene, vrell_mats(*PLUM, TEAL, TEAL, glow * flare)


MITE_N = 22
mites = {v: Turning(lambda v=v, **kw: mite_model(v, **kw), MITE_N, 16, factor=6, colors=24)
         for v in "ab"}
MITE_DUR = 8.5
MITE_ROCKS = [dict(x0=320, y0=-40, vx=-12, vy=95, spin=0.6, n=4, shot=None, near=None),
              dict(x0=110, y0=-120, vx=6, vy=95, spin=-0.5, n=3, shot=2.3, near=None)]


def mite_rock(r, t):
    return r["x0"] + r["vx"] * t, r["y0"] + r["vy"] * t, r["spin"] * t


def mite_scene(v):
    bg = Scroll(hull_bg, 41, MITE_DUR, 30)
    px_of = follow(lambda t: 110 if t < 2.5 else (300 if t < 4.0 else 190), MITE_DUR, 240, 230, 0.3)
    py = 478
    rock_sp = rock_sprite(17, 21)
    riders = []                                   # dicts: rock, angle, release time, tell end, dive vector
    for ri, r in enumerate(MITE_ROCKS):
        trig = None
        if r["shot"] is not None:
            trig = r["shot"] + 0.3                    # tumble 0.3 s first
        else:
            for t in times(MITE_DUR):
                x, y, _ = mite_rock(r, t)
                if np.hypot(px_of(t) - x, py - y) < 160:
                    trig = t
                    break
        r["near"] = trig
        for j in range(r["n"]):
            a0 = j * TAU / r["n"] + 0.4
            t_tell = trig + 0.15 * j
            t_go = t_tell + 0.4
            x, y, sp_ = mite_rock(r, t_tell if r["shot"] is None else r["shot"])
            ax = a0 + sp_
            sx, sy = x + np.cos(ax) * 22, y + np.sin(ax) * 22
            if r["shot"] is not None:
                sx, sy = sx + np.cos(ax) * 20, sy + np.sin(ax) * 20
            tx, ty = px_of(t_go), py
            dv = np.array((tx - sx, ty - sy))
            dv = dv / (np.linalg.norm(dv) + 1e-9)
            kill = {(0, 1): t_go + 0.55, (1, 2): t_go + 0.4}.get((ri, j))
            riders.append(dict(r=r, a0=a0, tell=t_tell, go=t_go, start=(sx, sy), dv=dv, kill=kill))
    frames = []
    for t in times(MITE_DUR):
        f = bg.frame(t)
        for r in MITE_ROCKS:
            x, y, sp_ = mite_rock(r, t)
            if r["shot"] is not None and t >= r["shot"]:
                f = boom(f, x, y, (t - r["shot"]) / 0.6, "small", sparks=(150, 128, 104))
                continue
            sprite.paste_center(f, rot_sprite(rock_sp, -np.degrees(sp_)), x, y)
        for m in riders:
            r = m["r"]
            x, y, sp_ = mite_rock(r, t)
            shot = r["shot"] is not None and t >= r["shot"]
            if t < m["tell"] and not shot:                       # clinging, crawling round the rim
                ax = m["a0"] + sp_ + 0.15 * np.sin(t * 2 + m["a0"])
                mx, my = x + np.cos(ax) * 22, y + np.sin(ax) * 22
                sp = mites[v].get(ax, phase=round((t * 8) % 4) * TAU / 4)
            elif t < m["go"]:                                    # tumble / the tell
                mx, my = m["start"]
                ax = np.arctan2(my - y, mx - x) if not shot else t * 9
                if shot and t < r["shot"] + 0.3:
                    u = (t - r["shot"]) / 0.3
                    x0, y0, s0 = mite_rock(r, r["shot"])
                    a_ = m["a0"] + s0
                    mx, my = x0 + np.cos(a_) * (22 + 20 * u), y0 + np.sin(a_) * (22 + 20 * u)
                    sp = mites[v].get(ax, pose="leap")
                else:
                    want = np.arctan2(py - my, px_of(t) - mx)
                    sp = mites[v].get(want, pose="crouch")
                    f = light(f, mx, my, 8, TEAL, 0.5)
            else:
                u = t - m["go"]
                if m["kill"] is not None and t >= m["kill"]:
                    kx, ky = np.array(m["start"]) + m["dv"] * 260 * (m["kill"] - m["go"])
                    f = boom(f, kx, ky, (t - m["kill"]) / 0.45, "tiny", sparks=TEAL, n=6)
                    continue
                mx, my = np.array(m["start"]) + m["dv"] * 260 * u
                if not (-20 < mx < FW + 20 and -20 < my < FH + 20):
                    continue
                sp = mites[v].get(np.arctan2(m["dv"][1], m["dv"][0]), pose="leap")
            sprite.paste_center(f, sp, mx, my)
        ship(f, px_of(t), py, t)
        if any(m["tell"] <= t < m["go"] for m in riders):
            caption(f, "WITHIN 160 PX: CROUCH, TEAL FLARE 0.4 S, THEN THE LEAP")
        frames.append(f)
    return frames, bg


def mite_sheet(v, frames, bg):
    M = mites[v]
    heads = [M.frame(k) for k in range(16)]
    poses = [M.get(DOWN, phase=k * TAU / 4) for k in range(4)] + [M.get(DOWN, pose="crouch"),
                                                                  M.get(DOWN, pose="leap")]
    base = M.get(DOWN)
    panels = [("SOURCE, 260 PX", source_panel(lambda: mite_model(v))),
              ("16 HEADINGS (2X)", grid(heads, 8, 2)),
              ("CRAWL x4 / CROUCH (TELL) / LEAP (3X)", grid(poses, 6, 3)),
              ("ON ITS ROCK, NEXT TO THE PLAYER (2X)",
               row([rock_sprite(17, 21), base, e3.player_sprite()], 48, 2, ["ROCK", "MITE", "PLAYER"])),
              ("READABILITY: OVER THE DARKEST L29 GROUND", readability([(base, "MITE")], bg.img, (1, 2, 4), 40)),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.0, 2.8, 4.1, 4.7], ["RIDING", "ROCK SHOT", "TELLS", "LEAPS"]))]
    if v == "a":
        notes = ["ASTEROID MITE A - TICK (VRELL SWARM FODDER: PLUM CHITIN, TEAL CONTACT GLOW)",
                 "20X20 PX (22 PX CANVAS), 16 HEADINGS, 4 CRAWL FRAMES, A CROUCH AND A LEAP POSE. A ROUND PLATED",
                 "ABDOMEN WITH A TEAL SEAM, A SMALL DARK HEAD WITH BONE MANDIBLES, SIX SHORT SPLAYED LEGS. RIDES A",
                 "SMALL ROCK (2-5 A ROCK), CLINGING ROUND THE RIM; WHEN THE SHIP COMES WITHIN 160 PX EACH RIDER IN",
                 "TURN (0.15 S APART) CROUCHES WITH A TEAL FLARE FOR 0.4 S AND LEAPS STRAIGHT AT THE SHIP (260 PX/S,",
                 "NO STEERING). A SHOT ROCK FREES THEM EARLY: THEY TUMBLE 0.3 S, THEN THE SAME TELL."]
    else:
        notes = ["ASTEROID MITE B - FLEA (VRELL SWARM FODDER: PLUM CHITIN, TEAL CONTACT GLOW)",
                 "A LONG FLAT SEGMENTED BODY WITH THREE TEAL SPOTS AND TWO BIG BONE-KNEED HIND LEGS FOLDED AT THE",
                 "SIDES: IT LOOKS LIKE A LEAPER, THE TELL TUCKS THE LEGS. SAME SCENE AS A."]
    return sheet(f"ASTEROID MITE {v.upper()} - ROCK RIDERS (LEVEL 29)", panels, notes, frames[int(4.3 * FPS)])


@unit("asteroid-mite")
def make_mite(variants):
    for v in variants:
        frames, bg = mite_scene(v)
        save_unit("space/asteroid-mite", "asteroid-mite", v, mite_sheet(v, frames, bg), frames)


# ===================================================================== 8 Minelayer (L30)

def minelayer_model(v, glow=1.0):
    """Minelayer (faces +Y), 88 wide x 64 long. a: hopper barge - a wide flat black hull with a gold
    edge band, two engine sponsons and a gold mine drum at the stern; b: catamaran - two slim black
    hulls with gold noses joined by a bridge deck, the gold mine rack between them at the rear."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            hull = [(0.0, 0.66), (0.55, 0.52), (0.92, 0.08), (0.88, -0.36), (0.6, -0.64), (0.0, -0.58)]
            d, m = sd_plate(q, hull, 0.0, 0.14, 0.0, taper=am._roof(0.7)), None
            m = np.where((q[:, 0] > 0.72) | (q[:, 1] > 0.5), A_GOLD, A_HULL)
            return union(
                (d, m),
                (sd_capsule(q, (0.66, 0.3, 0.06), (0.66, -0.52, 0.06), 0.13, 0.12), A_FACET),
                (sd_cylinder_y(q, (0.66, -0.6, 0.06), 0.09, 0.05), A_ENGINE),
                (sd_cylinder_z(p, (0, -0.28, 0.16), 0.3, 0.08), A_GOLD),
                (sd_cylinder_z(p, (0, -0.28, 0.24), 0.17, 0.04), A_GLASS),
                (sd_plate(p, [(0, 0.55), (0.12, 0.35), (0, 0.24), (-0.12, 0.35)], 0.15, 0.03), A_GLASS),
                (sd_sphere(q, (0.9, 0.08, 0.08), 0.05), A_RED),
                k=0.02)
        return union(
            (sd_capsule(q, (0.62, 0.42, 0.04), (0.62, -0.58, 0.04), 0.22, 0.18), A_HULL),
            (sd_capsule(q, (0.62, 0.25, 0.04), (0.62, 0.68, 0.04), 0.21, 0.05), A_GOLD),
            (sd_capsule(q, (0.62, -0.42, 0.04), (0.62, -0.6, 0.04), 0.19, 0.16), A_GOLD),
            (sd_box(p, (0, 0.12, 0.08), (0.5, 0.2, 0.06), 0.03), A_GOLD),
            (sd_box(p, (0, 0.12, 0.13), (0.36, 0.12, 0.03), 0.02), A_FACET),
            (sd_box(p, (0, -0.34, 0.06), (0.32, 0.17, 0.05), 0.02), A_GOLD),
            (sd_box(p, (0, -0.34, 0.12), (0.2, 0.08, 0.02)), A_GLASS),
            (sd_plate(p, [(0, 0.32), (0.1, 0.18), (0, 0.1), (-0.1, 0.18)], 0.15, 0.03), A_GLASS),
            (sd_cylinder_y(q, (0.62, -0.62, 0.04), 0.1, 0.05), A_ENGINE),
            (sd_sphere(q, (0.84, 0.0, 0.12), 0.05), A_RED),
            k=0.02)
    return scene, belt_mats("c8202a", glow)


def mine_model(v, lamp=0.15, glow=1.0):
    """A mine. a: a black ball with four gold spikes and a red lamp on top; b: a flat black disc
    with a gold rim, three short gold prongs and a red eye."""
    def scene(p):
        if v == "a":
            items = [(sd_sphere(p, (0, 0, 0.1), 0.48), A_HULL),
                     (sd_sphere(p, (0, 0, 0.5), 0.14), A_X)]
            for a in np.linspace(0, TAU, 4, endpoint=False) + np.pi / 4:
                items.append((sd_capsule(p, (0.3 * np.cos(a), 0.3 * np.sin(a), 0.2),
                                         (0.85 * np.cos(a), 0.85 * np.sin(a), 0.1), 0.1, 0.04), A_GOLD))
            return union(*items)
        ring = subtract((sd_cylinder_z(p, (0, 0, 0.1), 0.66, 0.1), A_GOLD), sd_cylinder_z(p, (0, 0, 0.1), 0.5, 0.2))
        items = [(sd_cylinder_z(p, (0, 0, 0.08), 0.52, 0.1), A_HULL), ring,
                 (sd_sphere(p, (0, 0, 0.2), 0.16), A_X)]
        for a in np.linspace(0, TAU, 3, endpoint=False):
            items.append((sd_capsule(p, (0.6 * np.cos(a), 0.6 * np.sin(a), 0.1),
                                     (0.92 * np.cos(a), 0.92 * np.sin(a), 0.1), 0.07, 0.04), A_GOLD))
        return union(*items)
    return scene, belt_mats("c8202a", glow, extra=[emissive(np.array(CRIMSON) / 255.0, lamp * 2.0)])


ML_N = 92
layers = {v: Turning(lambda v=v: minelayer_model(v), ML_N, 16, factor=4, colors=40, post=belt_rim(v == "a"))
          for v in "ab"}
mines = {v: rig.AngleSprites(lambda v=v, **kw: mine_model(v, **kw), 20, 4, factor=8, colors=20,
                             post=belt_rim(False)) for v in "ab"}
ML_DUR = 9.6
ML_SCROLL = 80.0
ML_ROW = 140.0
MINE_X = [12, 44, 76, 108] + [220, 252, 284, 316] + [372, 404, 436, 468]   # gap 112 between 108 and 220
ML_LAY0 = 3.4                                                              # strafe start
ML_SHOT = (9, 5.6)                                                         # mine index, time


def layer_pos(t):
    """Rear entry up the left lane, a turn into the lay row, the strafe to the right."""
    if t < ML_LAY0:
        y = FH + 50 - 230 * max(0.0, t - 1.4)
        return 46.0, max(ML_ROW, y), UP
    u = t - ML_LAY0
    if u < 0.4:
        a = UP + (np.pi / 2) * smooth(u / 0.4)
        return 46.0 + 30 * u, ML_ROW, a
    return 58.0 + 200 * (u - 0.4), ML_ROW, RIGHT


def mine_lay_t(x):
    return ML_LAY0 + 0.4 + max(0.0, (x - 58.0) / 200.0)


def ml_scene(v):
    bg = Scroll(space_bg, 42, ML_DUR, ML_SCROLL)
    px_of = follow(lambda t: 240 if t < 4.5 else (120 if t < 6.9 else 164), ML_DUR, 240, 140, 0.4)
    py = 478
    shots = Shots()
    burst = {}                                     # mine index -> (time, ring?)
    si, st = ML_SHOT
    burst[si] = (st, False)
    for j in (si - 1, si + 1):
        burst[j] = (st + 0.05, False)
    blink = {}
    for i, x in enumerate(MINE_X):                 # proximity: blink 0.6 s, then a ring of 8
        if i in burst:
            continue
        lt = mine_lay_t(x)
        for t in times(ML_DUR):
            y = ML_ROW + ML_SCROLL * (t - lt)
            if t > lt + 1.0 and np.hypot(px_of(t) - x, py - y) < 48:
                blink[i] = t
                burst[i] = (t + 0.6, True)
                shots.ring(t + 0.6, x, y + ML_SCROLL * 0.6, 8, 100)
                break
    frames = []
    for t in times(ML_DUR):
        f = bg.frame(t)
        for i, x in enumerate(MINE_X):
            lt = mine_lay_t(x)
            if t < lt:
                continue
            y = ML_ROW + ML_SCROLL * (t - lt)
            if i in burst and t >= burst[i][0]:
                f = boom(f, x, y, (t - burst[i][0]) / 0.5, "tiny", n=8)
                continue
            armed = t >= lt + 1.0
            lamp = 0.15
            if armed:
                lamp = 0.6 if int(t * 2) % 2 else 0.25
            if i in blink and t >= blink[i]:
                lamp = 1.0 if int(t * 12) % 2 else 0.1
            sp = mines[v].get(DOWN, lamp=lamp)
            if i == si and st - 0.25 < t < st:
                sp = flash(sp, True)
            sprite.paste_center(f, sp, x, y)
            if lamp >= 0.6:
                f = light(f, x, y - 1, 6, (255, 60, 50), 0.6)
        x, y, h = layer_pos(t)
        if x < FW + 60:
            sprite.paste_center(f, layers[v].get(h), x, y)
            for side in (-1, 1):
                ex, ey = rig.local_to_screen(side * 0.66, -0.66, h, ML_N / 2.3)
                f = light(f, x + ex, y + ey, 6, (255, 150, 90), 0.6)
        shots.draw(f, t)
        ship(f, px_of(t), py, t, shots=t > 4.8)
        if t < 1.6:
            rear_warning(f, t, [46])
        elif 4.0 < t < 5.4:
            caption(f, "A LINE OF 3 X 4 MINES WITH ONE 112 PX GAP")
        elif 5.6 < t < 6.8:
            caption(f, "ONE SHOT MINE SETS OFF ITS NEIGHBOURS")
        elif 7.0 < t < 8.6:
            caption(f, "TOO CLOSE: BLINK 0.6 S, THEN A RING OF 8")
        frames.append(f)
    return frames, bg


def ml_sheet(v, frames, bg):
    L = layers[v]
    heads = [L.frame(k) for k in range(0, 16, 2)]
    base = L.get(DOWN)
    ms = [mines[v].get(DOWN, lamp=k) for k in (0.15, 0.6, 1.0)]
    panels = [("SOURCE, 260 PX", source_panel(lambda: minelayer_model(v))),
              ("8 OF 16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("MINE: LAID / ARMED / BLINK (4X)", grid(ms, 3, 4)),
              ("NEXT TO A TALON AND THE PLAYER (1X)", row([base, talons[(v, False)].get(DOWN), e3.player_sprite()], ML_N, 1)),
              ("READABILITY: OVER THE DARKEST L30 SPACE",
               readability([(base, gold_note(base)), (ms[1], "MINE")], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.0, 4.6, 5.8, 7.9],
                                              ["REAR WARNING", "LAYING", "SHOT MINE", "BLINK AND RING"]))]
    if v == "a":
        notes = ["MINELAYER A - HOPPER BARGE (Q11 A LIVERY, STRONG GOLD RIM)",
                 "88X64 PX (92 PX CANVAS), 16 HEADINGS. A WIDE FLAT BLACK HULL WITH A GOLD EDGE BAND, TWO ENGINE",
                 "SPONSONS, THE GOLD MINE DRUM AT THE STERN (DRAWN ONLY). MINE A: A BLACK BALL WITH FOUR GOLD SPIKES",
                 "AND A RED LAMP: DIM WHEN LAID, A SLOW BLINK ONCE ARMED (1 S), A FAST BLINK FOR 0.6 S WHEN THE SHIP",
                 "IS WITHIN 48 PX, THEN A RING OF 8. COMES UP THE LEFT LANE FROM BEHIND (EDGE-WARNED), TURNS INTO",
                 "ITS ROW AND LAYS 3 CLUSTERS OF 4 (32 PX APART, 56 PX BETWEEN) WITH ONE 112 PX GAP; THE LINE SCROLLS",
                 "DOWN AND SLOPES. A SHOT MINE BURSTS AND SETS OFF THE MINES WITHIN 40 PX: A HOLE."]
    else:
        notes = ["MINELAYER B - CATAMARAN (Q11 A LIVERY, 1 PX RIM)",
                 "TWO SLIM BLACK HULLS WITH GOLD NOSES JOINED BY A BRIDGE DECK, THE GOLD MINE RACK BETWEEN THEM AT",
                 "THE REAR. MINE B: A FLAT BLACK DISC WITH A GOLD RIM, THREE GOLD PRONGS AND A RED EYE (FLATTER, A",
                 "BIGGER LAMP). SAME SCENE AS A."]
    return sheet(f"MINELAYER {v.upper()} - THE MINE LINE AND ITS GAP (LEVEL 30)", panels, notes,
                 frames[int(5.8 * FPS)])


@unit("minelayer")
def make_ml(variants):
    for v in variants:
        frames, bg = ml_scene(v)
        save_unit("space/minelayer", "minelayer", v, ml_sheet(v, frames, bg), frames)


# ===================================================================== 9 Shard Drone (L31)

def shard_model(v, glow=1.0):
    """Shard Drone (radial, 3-fold). a: tri-blade - three long gold shard blades round a black
    hub ring with a red light; b: tri-prism - a black triangular frame with a gold crystal prism
    at each corner and the red hub."""
    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, 0.1), 0.3, 0.1), A_HULL), (sd_sphere(p, (0, 0, 0.2), 0.11), A_RED)]
        for a in np.linspace(0, TAU, 3, endpoint=False):
            r = rotate_z(p, a)
            if v == "a":
                items.append((sd_plate(r, [(0.2, 0.14), (1.02, 0.0), (0.24, -0.12)], 0.08, 0.06, 0.0,
                                       taper=lambda x, y: np.clip(1.2 - 0.8 * x, 0.3, 1)), A_GOLD))
            else:
                items += [(sd_capsule(r, (0.25, 0.0, 0.1), (0.7, 0.0, 0.1), 0.07, 0.07), A_HULL),
                          (sd_ellipsoid(r, (0.84, 0.0, 0.12), (0.2, 0.13, 0.12)), A_GOLD)]
        if v == "b":
            for a in np.linspace(0, TAU, 3, endpoint=False) + np.pi / 3:
                r = rotate_z(p, a)
                items.append((sd_capsule(r, (0.42, -0.42, 0.08), (0.42, 0.42, 0.08), 0.05, 0.05), A_HULL))
        return union(*items, k=0.02)
    return scene, belt_mats("c8202a", glow)


SH_N = 40
shards = {v: Turning(lambda v=v: shard_model(v), SH_N, 24, factor=5, colors=28, sym=3, post=belt_rim(v == "a"))
          for v in "ab"}
SH_DUR = 9.0
SH_KILL = (1, 3.6)


def draw_link(img, a, b, v, t, state):
    """An energy link: state 'flicker' (the tell) or 'lit'. a: a crimson beam with a dark rim
    and a pale core; b: crimson-gold crackling lightning."""
    d = ImageDraw.Draw(img)
    (x0, y0), (x1, y1) = a, b
    if state == "flicker":
        if np.sin(t * 53 + x0) > 0.2:
            d.line([x0, y0, x1, y1], fill=(255, 80, 70, 150), width=1)
        return img
    if v == "a":
        d.line([x0, y0, x1, y1], fill=(40, 6, 10, 255), width=8)
        d.line([x0, y0, x1, y1], fill=CRIMSON + (255,), width=6)
        d.line([x0, y0, x1, y1], fill=(255, 200, 190, 255), width=2)
    else:
        rng = np.random.default_rng(int(t * FPS) + int(x0))
        n = 7
        pts = []
        for k in range(n + 1):
            u = k / n
            off = 0 if k in (0, n) else rng.uniform(-5, 5)
            nx, ny = -(y1 - y0), x1 - x0
            ln = np.hypot(nx, ny) or 1
            pts.append((x0 + (x1 - x0) * u + nx / ln * off, y0 + (y1 - y0) * u + ny / ln * off))
        d.line(pts, fill=(40, 6, 10, 255), width=6)
        d.line(pts, fill=CRIMSON + (255,), width=4)
        d.line(pts, fill=(255, 210, 110, 255), width=1)
    return img


def shard_line(t):
    base_y = -40 + 70 * t
    sway = 30 * np.sin(TAU * t / 5.0)
    return [(60 + 120 * k + sway, base_y + (0 if k % 2 else 4)) for k in range(4)]


def shard_scene(v):
    bg = Scroll(space_bg, 43, SH_DUR, 30)
    px_of = follow(lambda t: 240 if t < 3.0 else 182, SH_DUR, 240, 200)
    py = 478
    shots = Shots()
    for k in range(4):
        for t0 in np.arange(1.6 + 0.75 * k, SH_DUR, 3.0):
            if k == SH_KILL[0] and t0 >= SH_KILL[1]:
                continue
            x, y = shard_line(t0)[k]
            if 0 < y < FH:
                shots.fire(t0, x, y, px_of(t0), py, 150)
    lit_t = 0.6 + 0.8
    frames = []
    for t in times(SH_DUR):
        f = bg.frame(t)
        pos = shard_line(t)
        ki, kt = SH_KILL
        for k in range(3):
            if t >= kt and k in (ki - 1, ki):
                if t < kt + 0.25:
                    mx, my = (pos[k][0] + pos[k + 1][0]) / 2, (pos[k][1] + pos[k + 1][1]) / 2
                    f = spark(f, mx, my, (t - kt) / 0.25, (255, 120, 100), 6, 12)
                continue
            if t > 0.6:
                f = draw_link(f, pos[k], pos[k + 1], v, t, "flicker" if t < lit_t else "lit")
        for k, (x, y) in enumerate(pos):
            if k == ki and t >= kt:
                f = boom(f, x, y, (t - kt) / 0.6, "small")
                continue
            on = k == ki and kt - 0.3 < t < kt and int(t * 16) % 2
            sprite.paste_center(f, flash(shards[v].get(t * TAU), on), x, y)
        shots.draw(f, t)
        ship(f, px_of(t), py, t, stop=pos[1][1] + 10 if t < kt else None)
        if 0.6 < t < lit_t:
            caption(f, "THE LINKS FLICKER 0.8 S, THEN THEY HURT")
        elif kt < t < kt + 1.5:
            caption(f, "A KILLED DRONE BREAKS ITS LINKS")
        frames.append(f)
    return frames, bg


def shard_circle(v, t=0.6):
    """The circle formation: four drones on a 64 px radius, the closed square of links."""
    n = 180
    img = space_bg(44, n).crop((0, 0, n, n))
    img = img.crop((0, 0, n, n))
    pos = [(n / 2 + 64 * np.cos(a), n / 2 + 64 * np.sin(a)) for a in np.linspace(0, TAU, 4, endpoint=False) + t]
    for k in range(4):
        img = draw_link(img, pos[k], pos[(k + 1) % 4], v, 1.0, "lit")
    for x, y in pos:
        sprite.paste_center(img, shards[v].get(t * 3), x, y)
    return img


def shard_sheet(v, frames, bg):
    S = shards[v]
    spin = [S.frame(k) for k in range(8)]
    base = S.frame(0)
    panels = [("SOURCE, 260 PX", source_panel(lambda: shard_model(v))),
              ("SPIN: 8 FRAMES COVER 120 DEG (2X)", grid(spin, 8, 2)),
              ("CIRCLE OF 4 (1X)", shard_circle(v)),
              ("READABILITY: OVER THE DARKEST L31 SPACE", readability([(base, gold_note(base))], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.0, 2.6, 3.9, 6.6],
                                              ["FLICKER (TELL)", "LIT LINE", "DRONE KILLED", "THROUGH THE HOLE"]))]
    if v == "a":
        notes = ["SHARD DRONE A - TRI-BLADE, BEAM LINKS (Q11 A LIVERY, STRONG GOLD RIM)",
                 "40X40 PX, RADIAL (3-FOLD, SPINNING 360 DEG/S): THREE LONG GOLD SHARD BLADES ROUND A BLACK HUB WITH",
                 "A RED LIGHT. A LINE OF FOUR 120 PX APART SWAYS DOWN THE FIELD; 0.8 S AFTER THE GROUP IS IN, THE",
                 "LINKS FLICKER (THE TELL), THEN LIGHT: A CRIMSON BEAM WITH A DARK RIM AND A PALE CORE (8 PX DRAWN,",
                 "A 6 PX BAND). SHOTS PASS THROUGH LINKS; KILLING A DRONE DROPS ITS LINKS (THE MIDDLE ONE BOTH)."]
    else:
        notes = ["SHARD DRONE B - TRI-PRISM, LIGHTNING LINKS (Q11 A LIVERY, 1 PX RIM)",
                 "A BLACK TRIANGULAR FRAME WITH A GOLD CRYSTAL PRISM AT EACH CORNER AND THE RED HUB. THE LINKS ARE",
                 "CRACKLING CRIMSON LIGHTNING WITH A GOLD CORE, RE-DRAWN EVERY FRAME: MORE 'ENERGY', LESS 'LASER'",
                 "(THE BEAM LOOK STAYS THE LASERS' OWN). SAME SCENE AS A."]
    return sheet(f"SHARD DRONE {v.upper()} - LINKED GROUPS (LEVEL 31)", panels, notes, frames[int(3.2 * FPS)])


@unit("shard-drone")
def make_shard(variants):
    for v in variants:
        frames, bg = shard_scene(v)
        save_unit("space/shard-drone", "shard-drone", v, shard_sheet(v, frames, bg), frames)


# ===================================================================== 10 Void Leech (L33, Vrell)

LEECH = ("2c1838", "0c0612", "8c7898")


def leech_model(v, phase=0.0, bend=0.0, pulse=0.5, glow=1.0):
    """Void Leech (Vrell, black-violet), faces +Y. a: ringed leech - five tapering chitin rings,
    an oral sucker disc ringed with bone hooks, a violet power-draw sac on the back; b: ray leech -
    a flat undulating mantle, a hooked mouth, the sac and violet veins. ``bend`` curls it (latched),
    ``pulse`` drives the sac."""
    def scene(p):
        q = mirror_x(p)
        items = []
        if v == "a":
            for k in range(5):
                y = 0.45 - 0.28 * k
                x = 0.1 * np.sin(phase + k * 1.1) + bend * 0.12 * k * k / 4
                rr = 0.44 - 0.06 * k
                items.append((sd_ellipsoid(p, (x, y, 0.1), (rr, 0.24, rr * 0.7)), V_BODY if k % 2 else V_DARK))
            items += [(sd_cylinder_z(p, (0, 0.72, 0.06), 0.3, 0.05), V_DARK),
                      (sd_sphere(p, (0.0, 0.12, 0.3), 0.15), A_X)]
            for a in np.linspace(0, TAU, 6, endpoint=False):
                items.append((sd_capsule(p, (0.18 * np.cos(a), 0.72 + 0.18 * np.sin(a), 0.1),
                                         (0.26 * np.cos(a), 0.72 + 0.26 * np.sin(a), 0.06), 0.03, 0.015), V_BONE))
        else:
            wave = 0.06 * np.sin(phase + p[:, 1] * 4)
            pz = p.copy()
            pz[:, 2] = p[:, 2] - wave * np.abs(p[:, 0])
            pz[:, 0] = p[:, 0] - bend * 0.25 * (p[:, 1] - 0.6) ** 2 * 0.5
            items += [(sd_ellipsoid(pz, (0, -0.05, 0.05), (0.62, 0.72, 0.1)), V_SEAM),
                      (sd_ellipsoid(p, (0, 0.62, 0.08), (0.2, 0.16, 0.1)), V_DARK),
                      (sd_capsule(q, (0.08, 0.72, 0.08), (0.12, 0.92, 0.04), 0.04, 0.015), V_BONE),
                      (sd_sphere(p, (0.0, 0.0, 0.16), 0.17), A_X),
                      (sd_ellipsoid(pz, (0, -0.4, 0.12), (0.22, 0.3, 0.06)), V_SAC)]
        return union(*items, k=0.05)
    sac = emissive(VIOLET_F, (0.35 + 1.6 * pulse) * glow)
    return scene, vrell_mats(*LEECH, VIOLET, VIOLET, glow, extra=[sac])


LEECH_N = 32
leeches = {v: Turning(lambda v=v, **kw: leech_model(v, **kw), LEECH_N, 16, factor=5, colors=28)
           for v in "ab"}
LCH_DUR = 10.0
SPOTS = {"left": (-15, 6), "right": (15, 6), "tail": (0, 20)}
SHAKE = (5.4, 6.6)


def lch_px(t):
    x = 240 + 30 * np.sin(t * 0.5)
    if SHAKE[0] <= t < SHAKE[1]:
        x += 18 * np.sin(TAU * (t - SHAKE[0]) / 0.6)
    return x


def leech_sim(start, spot, t0, py=470):
    """A leech drifts in, chases within 220 px, latches at ``spot`` (or misses if spot is None),
    is thrown off at the shake's end, stunned 1 s, then flees."""
    pos, h = np.array(start, dtype=np.float64), np.arctan2(py - start[1], 240 - start[0])
    out, state = [], "drift"
    for t in times(LCH_DUR):
        if t < t0:
            out.append(("wait", pos.copy(), h))
            continue
        tx, ty = lch_px(t), py
        if state in ("drift", "chase"):
            if spot is not None:
                ox, oy = SPOTS[spot]
                tx, ty = tx + ox, ty + oy
            want = np.arctan2(ty - pos[1], tx - pos[0])
            if state == "drift" and np.hypot(tx - pos[0], ty - pos[1]) < 220:
                state = "chase"
            rate = 200 if (state == "chase" and spot) else 30
            dh = (want - h + np.pi) % TAU - np.pi
            h += float(np.clip(dh, -np.radians(rate) / FPS, np.radians(rate) / FPS))
            pos = pos + np.array((np.cos(h), np.sin(h))) * 150 / FPS
            if spot and np.hypot(tx - pos[0], ty - pos[1]) < 8:
                state = "latched"
        elif state == "latched":
            if t >= SHAKE[1]:
                state, ts = "stunned", t
                ox, oy = SPOTS[spot]
                pos = np.array((lch_px(t) + ox * 2.2, py + oy * 2.2))
            else:
                ox, oy = SPOTS[spot]
                pos = np.array((lch_px(t) + ox, py + oy))
                h = np.arctan2(-oy, -ox)
        elif state == "stunned":
            ox, oy = SPOTS[spot]
            pos = pos + np.array((ox, oy)) / (np.hypot(ox, oy) + 1e-9) * 40 / FPS
            h += 0.4
            if t - ts > 1.0:
                state = "flee"
        else:
            h = DOWN if pos[0] > 0 else LEFT
            pos = pos + np.array((np.cos(h), np.sin(h))) * 170 / FPS
        out.append((state, pos.copy(), h))
    return out


def leech_scene(v):
    bg = Scroll(rock_bg, 45, LCH_DUR, 0, growth=True)
    py = 470
    sims = [leech_sim((60, FH + 30), "left", 0.6), leech_sim((FW + 30, 400), "tail", 1.6),
            leech_sim((FW - 70, FH + 30), None, 2.4)]
    tal = talons[(v, False)].get(DOWN)
    frames = []
    for k, t in enumerate(times(LCH_DUR)):
        f = bg.frame(t)
        # the uneasy alliance: a Talon with a leech drifts down, shield gone, not firing
        tx, ty = 380 + 6 * np.sin(t), -30 + 60 * t
        if ty < FH + 30:
            sprite.paste_center(f, tint(tal, (60, 30, 90), 0.15), tx, ty)
            sprite.paste_center(f, leeches[v].get(UP + 0.3, bend=1.0, pulse=0.5 + 0.5 * np.sin(t * TAU)),
                                tx + 4, ty - 15)
            f = light(f, tx + 4, ty - 15, 7, VIOLET, 0.3 + 0.3 * np.sin(t * TAU))
        n_lat = 0
        for sim in sims:
            state, pos, h = sim[k]
            if state == "wait":
                continue
            if not (-30 < pos[0] < FW + 30 and -30 < pos[1] < FH + 40):
                continue
            pulse = 0.5 + 0.5 * np.sin(t * TAU) if state == "latched" else 0.3
            sp = leeches[v].get(h, phase=round((t * 6) % 4) * TAU / 4, bend=1.0 if state == "latched" else 0.0,
                                pulse=round(pulse * 4) / 4, glow=0.25 if state == "stunned" else 1.0)
            if state == "latched":
                n_lat += 1
                continue                                          # drawn over the ship
            sprite.paste_center(f, sp, pos[0], pos[1])
        ship(f, lch_px(t), py, t, shots=False)
        for sim in sims:
            state, pos, h = sim[k]
            if state == "latched":
                pulse = 0.5 + 0.5 * np.sin(t * TAU)
                sp = leeches[v].get(h, bend=1.0, pulse=round(pulse * 4) / 4)
                sprite.paste_center(f, sp, pos[0], pos[1])
                f = light(f, pos[0], pos[1], 7, VIOLET, 0.25 + 0.35 * pulse)
        if n_lat:
            label(f, lch_px(t) + 26, py - 4, f"GEN -{2 * n_lat} MW", (200, 160, 255))
        if t < 1.6:
            rear_warning(f, t, [60])
        if 1.4 < t < 3.0:
            side_warning(f, t, 1, [400])
        if SHAKE[0] <= t < SHAKE[1]:
            caption(f, "SHAKE IT OFF: FOUR REVERSALS IN 1.2 S")
        elif 0.2 < t < 1.6:
            caption(f, "PREY: A LEECHED TALON LOSES ITS SHIELD AND DRIFTS")
        frames.append(f)
    return frames, bg


def leech_sheet(v, frames, bg):
    L = leeches[v]
    heads = [L.frame(k) for k in range(16)]
    swim = [L.get(DOWN, phase=k * TAU / 4) for k in range(4)]
    states = [L.get(DOWN, bend=1.0, pulse=1.0), L.get(DOWN, bend=1.0, pulse=0.0), L.get(DOWN, glow=0.25)]
    base = L.get(DOWN)
    latched = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    sprite.paste_center(latched, e3.player_sprite(), 32, 30)
    for spot, (ox, oy) in SPOTS.items():
        sprite.paste_center(latched, L.get(np.arctan2(-oy, -ox), bend=1.0, pulse=1.0), 32 + ox, 30 + oy)
    panels = [("SOURCE, 260 PX", source_panel(lambda: leech_model(v, pulse=1.0))),
              ("16 HEADINGS (2X)", grid(heads, 8, 2)),
              ("SWIM x4 / LATCHED, SAC ON / OFF / STUNNED (2X)", grid(swim + states, 7, 2)),
              ("THREE LATCH SPOTS ON THE SHIP (2X)", grid([latched], 1, 2)),
              ("READABILITY: OVER THE DARKEST L33 GROUND", readability([(base, "LEECH")], bg.img, (1, 2, 4), 48)),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.2, 4.8, 6.0, 7.2], ["CHASE", "LATCHED: -4 MW", "SHAKE", "THROWN OFF"]))]
    if v == "a":
        notes = ["VOID LEECH A - RINGED LEECH (VRELL: BLACK-VIOLET CHITIN, VIOLET POWER-DRAW GLOW)",
                 "32X32 PX, 16 HEADINGS X 4 SWIM FRAMES, A LATCHED CURL, A STUNNED POSE. FIVE TAPERING BLACK-VIOLET",
                 "RINGS, AN ORAL SUCKER DISC RINGED WITH BONE HOOKS, A VIOLET SAC ON THE BACK THAT PULSES ONCE A",
                 "SECOND WHILE IT DRAINS (-2 MW EACH, 3 SPOTS: LEFT, RIGHT, TAIL). DRIFTS IN FROM BEHIND AND THE",
                 "SIDES (EDGE-WARNED), CHASES WITHIN 220 PX; THE LAMPREY'S SHAKE-OFF THROWS IT, STUNNED 1 S, THEN",
                 "IT FLEES. IN L33 A WAVE OF TALONS IS PREY: A LEECHED TALON DRIFTS DOWN, SHIELD GONE, SILENT."]
    else:
        notes = ["VOID LEECH B - RAY LEECH (VRELL: BLACK-VIOLET, VIOLET GLOW)",
                 "A FLAT UNDULATING MANTLE WITH A GLOWING SEAM, A HOOKED MOUTH, THE VIOLET SAC AND VEINS: BROADER",
                 "AND FLATTER ON THE HULL, MORE 'PARASITE', LESS 'WORM'. SAME SCENE AS A."]
    return sheet(f"VOID LEECH {v.upper()} - LATCH AND DRAIN (LEVEL 33)", panels, notes, frames[int(4.8 * FPS)])


@unit("void-leech")
def make_leech(variants):
    for v in variants:
        frames, bg = leech_scene(v)
        save_unit("space/void-leech", "void-leech", v, leech_sheet(v, frames, bg), frames)


# ===================================================================== 11 SAM Nest (L29)

def sam_base(v, lights="red", glow=1.0):
    """SAM Nest base (fixed). a: a round armoured base with a gold ring; b: a hexagonal base with
    gold edges. ``lights`` white = dormant (a friendly station), red = awake."""
    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        if v == "a":
            ring = subtract((sd_cylinder_z(p, (0, 0, 0.1), 0.8, 0.12), A_GOLD), sd_cylinder_z(p, (0, 0, 0.1), 0.66, 0.3))
            items = [(sd_cylinder_z(p, (0, 0, 0.05), 0.82, 0.1), A_HULL), ring]
        else:
            hexa = [(0.85 * np.cos(a), 0.85 * np.sin(a)) for a in np.linspace(0, TAU, 6, endpoint=False)]
            d = sd_plate(p, hexa, 0.08, 0.1, 0.0, taper=lambda x, y: np.clip(1.1 - 0.5 * np.hypot(x, y), 0.4, 1))
            items = [(d, np.where(r > 0.66, A_GOLD, A_HULL))]
        for a in np.linspace(0, TAU, 4, endpoint=False) + np.pi / 4:
            items.append((sd_sphere(p, (0.74 * np.cos(a), 0.74 * np.sin(a), 0.2), 0.06), A_X))
        return union(*items)
    col = np.array((1.0, 0.95, 0.85)) if lights == "white" else em.RED
    return scene, belt_mats("c8202a", glow, extra=[emissive(col, 1.8 * glow)])


def sam_rack(v, hatch=0.0, lock=0.0, lights="red", glow=1.0):
    """The launcher rack (pivot at the centre, aims +Y). a: twin tubes on a yoke with gold muzzle
    rims and a hatch over them; b: a square box launcher with four cells in a gold frame.
    ``hatch`` opens the hatch (the cells glow), ``lock`` lights the red lock light."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            items = [(sd_box(p, (0, -0.05, 0.3), (0.26, 0.24, 0.1), 0.03), A_FACET),
                     (sd_capsule(q, (0.17, -0.42, 0.44), (0.17, 0.5, 0.44), 0.14, 0.14), A_HULL),
                     (sd_capsule(q, (0.17, 0.48, 0.44), (0.17, 0.62, 0.44), 0.15, 0.15), A_GOLD),
                     (sd_box(p, (0, 0.66, 0.5 + 0.12 * hatch), (0.33, 0.05, 0.04)), A_GOLD),
                     (sd_sphere(q, (0.17, 0.66, 0.44), 0.09), A_X),
                     (sd_sphere(p, (0, -0.2, 0.56), 0.07), A_X + 1)]
        else:
            items = [(sd_box(p, (0, 0.05, 0.36), (0.38, 0.44, 0.16), 0.03), A_HULL),
                     (sd_box(p, (0, 0.05, 0.53), (0.38, 0.44, 0.02)), A_GOLD),
                     (sd_box(p, (0, 0.05, 0.54), (0.32, 0.38, 0.03)), A_HULL),
                     (sd_sphere(p, (0, -0.32, 0.6), 0.07), A_X + 1)]
            for cx in (-0.15, 0.15):
                for cy in (0.22, -0.08):
                    items.append((sd_box(p, (cx, cy, 0.56), (0.1, 0.11, 0.02)), A_X))
        return union(*items)
    lamp = np.array((1.0, 0.95, 0.85)) if lights == "white" else em.RED
    extra = [emissive(GOLD_F, 0.05 + 1.6 * hatch), emissive(lamp, 0.3 + 1.8 * lock)]
    return scene, belt_mats("c8202a", glow, extra=extra)


SAM_N = 48
sam_bases = {(v, li): rig.AngleSprites(lambda v=v, li=li: sam_base(v, li), SAM_N, 4, factor=5, colors=28,
                                       post=belt_rim(v == "a")) for v in "ab" for li in ("red", "white")}
sam_racks = {v: Turning(lambda v=v, **kw: sam_rack(v, **kw), SAM_N, 16, factor=5, colors=28,
                        post=belt_rim(v == "a")) for v in "ab"}
SAM_DUR = 9.5
SAMS = [dict(x=130, y0=110, wake=2.0, salvos=[4.0, 8.5]), dict(x=350, y0=40, wake=None, salvos=[1.2, 5.7])]


def sam_scene(v):
    bg = Scroll(hull_bg, 46, SAM_DUR, 30)
    px_of = follow(lambda t: 240 + 90 * np.sin(t * 0.8), SAM_DUR, 240, 200)
    py = 478
    ms = Homing(v, lambda t: (px_of(t), py))
    racks = []
    kills = {(1, 0, 0): 3.4, (0, 0, 1): 6.3}
    for i, n in enumerate(SAMS):                         # rack bearing over time (120 deg/s)
        h, hs = DOWN, []
        for t in times(SAM_DUR):
            y = bg.y(n["y0"], t)
            awake = n["wake"] is None or t >= n["wake"] + 1.0
            locked = any(s_ - 0.4 <= t < s_ + 0.3 for s_ in n["salvos"])
            if awake and not locked:
                want = np.arctan2(py - y, px_of(t) - n["x"])
                dh = (want - h + np.pi) % TAU - np.pi
                h += float(np.clip(dh, -np.radians(120) / FPS, np.radians(120) / FPS))
            hs.append(h)
        racks.append(hs)
        for si, s_ in enumerate(n["salvos"]):
            for j in range(2):
                tl = s_ + 0.25 * j
                hh = hs[min(len(hs) - 1, int(tl * FPS))]
                y = bg.y(n["y0"], tl)
                ms.launch(tl, n["x"] + np.cos(hh) * 16, y + np.sin(hh) * 16, hh, kills.get((i, si, j)))
    frames = []
    for k, t in enumerate(times(SAM_DUR)):
        f = bg.frame(t)
        for i, n in enumerate(SAMS):
            y = bg.y(n["y0"], t)
            dormant = n["wake"] is not None and t < n["wake"]
            waking = n["wake"] is not None and n["wake"] <= t < n["wake"] + 1.0
            li = "white" if dormant else "red"
            tell = any(s_ - 0.4 <= t < s_ for s_ in n["salvos"])
            hatch = 1.0 if any(s_ - 0.4 <= t < s_ + 0.4 for s_ in n["salvos"]) else 0.0
            lock = 1.0 if tell and int(t * 10) % 2 else 0.0
            if waking:
                lock = 1.0 if int(t * 6) % 2 else 0.3
            base = sam_bases[(v, li)].get(DOWN)
            rack = sam_racks[v].get(racks[i][k], hatch=hatch, lock=lock, lights=li)
            sh = sprite.shadow_of(rack, opacity=0.5, blur=1.0)
            lift = 2 + (3 * smooth((t - n["wake"]) / 1.0) if n["wake"] is not None and t >= n["wake"] else
                        (0 if dormant else 3))
            sprite.paste_center(f, base, n["x"], y)
            sprite.paste_center(f, sh, n["x"] + lift, y + lift)
            sprite.paste_center(f, rack, n["x"], y)
            if waking:
                label(f, n["x"] - 26, y + 30, "WAKES", (255, 120, 100))
        f = ms.draw(f, t)
        ship(f, px_of(t), py, t)
        if t < 2.0:
            caption(f, "DORMANT: WHITE LIGHTS, A FRIENDLY STATION")
        elif t < 3.0:
            caption(f, "THE BLOCK TURNS: RED LIGHTS, THE RACK LIFTS (1 S)")
        frames.append(f)
    return frames, bg


def sam_sheet(v, frames, bg):
    R = sam_racks[v]
    heads = [R.frame(k) for k in range(16)]

    def comp(li="red", **kw):
        c = Image.new("RGBA", (SAM_N, SAM_N), (0, 0, 0, 0))
        sprite.paste_center(c, sam_bases[(v, li)].get(DOWN), SAM_N / 2, SAM_N / 2)
        sprite.paste_center(c, R.get(DOWN, lights=li, **kw), SAM_N / 2, SAM_N / 2)
        return c
    states = [comp("white"), comp(), comp(hatch=1.0, lock=1.0)]
    panels = [("SOURCE: BASE AND RACK, 260 PX", source_panel(lambda: (
                  (lambda p: union(sam_base(v)[0](p), sam_rack(v, 1.0, 1.0)[0](p))), sam_rack(v, 1.0, 1.0)[1]))),
              ("RACK: 16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("DORMANT / AWAKE / TELL (HATCH, LOCK LIGHT) (3X)", grid(states, 3, 3)),
              ("READABILITY: OVER THE DARKEST L29 GROUND",
               readability([(states[1], gold_note(states[1])), (diamond_at(v, DOWN), "DIAMOND")], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.0, 2.6, 4.2, 6.0], ["DORMANT", "WAKING", "SALVO", "SHOT DOWN"]))]
    if v == "a":
        notes = ["SAM NEST A - TWIN TUBES ON A ROUND BASE (Q11 A LIVERY, STRONG GOLD RIM)",
                 "44X44 PX (48 PX CANVAS): A ROUND ARMOURED BASE WITH A GOLD RING AND FOUR RUNNING LIGHTS; THE RACK",
                 "(16 HEADINGS, 120 DEG/S) IS TWO MISSILE TUBES ON A YOKE WITH GOLD MUZZLE RIMS UNDER A GOLD HATCH.",
                 "TELL 0.4 S: THE RACK HOLDS, THE HATCH LIFTS (THE TUBES GLOW GOLD) AND THE RED LOCK LIGHT BLINKS;",
                 "THEN 2 HOMING DIAMONDS 0.25 S APART. DORMANT ON THE TURNING BLOCK: WHITE LIGHTS, NOT A TARGET;",
                 "AT ITS MOMENT THE LIGHTS GO RED AND THE RACK LIFTS FOR 1 S, THEN IT FIRES."]
    else:
        notes = ["SAM NEST B - FOUR-CELL BOX ON A HEX BASE (Q11 A LIVERY, 1 PX RIM)",
                 "A HEXAGONAL BASE WITH GOLD EDGES AND A SQUARE BOX LAUNCHER WITH FOUR CELLS IN A GOLD FRAME; THE",
                 "CELLS GLOW GOLD IN THE TELL. HEAVIER, MORE 'BUNKER'; THE B DIAMOND. SAME SCENE AS A."]
    return sheet(f"SAM NEST {v.upper()} - DORMANT, WAKING, SALVOS (LEVEL 29)", panels, notes, frames[int(4.3 * FPS)])


@unit("sam-nest")
def make_sam(variants):
    for v in variants:
        frames, bg = sam_scene(v)
        save_unit("ground/sam-nest", "sam-nest", v, sam_sheet(v, frames, bg), frames)


# ===================================================================== 12 Sentinel Tower (L32)

def sentinel_body(v, glow=1.0):
    """The tower seen from above (fixed). a: lighthouse - a gold-banded octagonal hardened collar
    and a round shaft; b: pylon - a square collar with four gold-edged buttress fins."""
    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        if v == "a":
            octa = [(0.95 * np.cos(a), 0.95 * np.sin(a)) for a in np.linspace(np.pi / 8, TAU + np.pi / 8, 8, endpoint=False)]
            d = sd_plate(p, octa, 0.06, 0.12, 0.0)
            return union((d, np.where(r > 0.8, A_GOLD, A_HULL)),
                         (sd_cylinder_z(p, (0, 0, 0.4), 0.5, 0.3), A_FACET),
                         (sd_cylinder_z(p, (0, 0, 0.66), 0.56, 0.04), A_GOLD))
        q = mirror_x(rotate_z(p, np.pi / 4))
        items = [(sd_box(p, (0, 0, 0.08), (0.66, 0.66, 0.1), 0.04), A_HULL),
                 (sd_box(p, (0, 0, 0.4), (0.42, 0.42, 0.3), 0.04), A_FACET)]
        for a in np.linspace(0, TAU, 4, endpoint=False) + np.pi / 4:
            r_ = rotate_z(p, a)
            items.append((sd_box(r_, (0.0, 0.78, 0.18), (0.07, 0.24, 0.16), 0.02), A_GOLD))
        return union(*items)
    return scene, belt_mats("c8202a", glow)


def sentinel_head(v, live=0.0, glow=1.0):
    """The emitter head (aims +Y): a: a black sphere with a gold equator ring and a lens snout;
    b: a flat disc with a gold rim and a long slit lens. The lens glows crimson with ``live``."""
    def scene(p):
        if v == "a":
            ring = subtract((sd_cylinder_z(p, (0, 0, 0.75), 0.58, 0.05), A_GOLD), sd_cylinder_z(p, (0, 0, 0.75), 0.48, 0.2))
            return union((sd_sphere(p, (0, 0, 0.7), 0.5), A_HULL), ring,
                         (sd_capsule(p, (0, 0.3, 0.85), (0, 0.72, 0.85), 0.17, 0.14), A_GUN),
                         (sd_sphere(p, (0, 0.76, 0.85), 0.12), A_X),
                         (sd_sphere(p, (0.18, -0.2, 1.1), 0.05), A_RED))
        rim = subtract((sd_cylinder_z(p, (0, 0, 0.8), 0.7, 0.07), A_GOLD), sd_cylinder_z(p, (0, 0, 0.8), 0.6, 0.2))
        return union((sd_cylinder_z(p, (0, 0, 0.78), 0.62, 0.1), A_HULL), rim,
                     (sd_box(p, (0, 0.3, 0.9), (0.07, 0.42, 0.04)), A_X),
                     (sd_sphere(p, (0.3, -0.3, 0.92), 0.05), A_RED))
    return scene, belt_mats("6a6e78", glow, extra=[emissive(np.array(CRIMSON) / 255.0, 0.25 + 1.8 * live)])


SEN_N = 84
sen_body = {v: rig.AngleSprites(lambda v=v: sentinel_body(v), SEN_N, 4, factor=4, colors=36, post=belt_rim(v == "a"))
            for v in "ab"}
sen_head = {v: Turning(lambda v=v, **kw: sentinel_head(v, **kw), SEN_N, 32, factor=4, colors=32,
                       post=belt_rim(v == "a")) for v in "ab"}
SEN_DUR = 14.0
SEN_X, SEN_Y0, SEN_SCROLL = 104, 210, 0.0
SWEEPS = [(0.5, "arc", 1), (5.0, "arc", -1), (9.5, "full", 1)]     # telegraph start, kind, direction
SEN_PY = 400


def sen_ship(t):
    if t < 9.5:
        return 210 + 70 * np.sin(t * 0.9), SEN_PY
    u = smooth((t - 9.5) / 1.0)
    return 210 + 70 * np.sin(9.5 * 0.9) * (1 - u) + 170 * u, SEN_PY + 40 * u


def sen_scene(v):
    bg = Scroll(tunnel_bg, 47, SEN_DUR, SEN_SCROLL, galleries=[(SEN_Y0 + 110, -1, 70)])
    plan = []
    for t0, kind, dr in SWEEPS:
        y = bg.y(SEN_Y0, t0)
        sx, sy = sen_ship(t0)
        b0 = np.arctan2(sy - y, sx - SEN_X)
        tel = 0.8 if kind == "arc" else 1.0
        span = np.radians(120) if kind == "arc" else TAU
        dur = 1.6 if kind == "arc" else 3.0
        start = b0 - dr * span / 2 if kind == "arc" else b0
        plan.append(dict(t0=t0, tel=tel, dur=dur, start=start, span=span, dr=dr, kind=kind))
    frames = []
    for t in times(SEN_DUR):
        f = bg.frame(t)
        y = bg.y(SEN_Y0, t)
        body = sen_body[v].get(DOWN)
        sprite.paste_center(f, sprite.shadow_of(body, opacity=0.5, blur=2.0), SEN_X + 16, y + 20)
        sprite.paste_center(f, body, SEN_X, y)
        aim, live = None, 0.0
        lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(lay)
        for s_ in plan:
            u = t - s_["t0"]
            if 0 <= u < s_["tel"]:
                k = 0.4 + 0.6 * u / s_["tel"]
                if s_["kind"] == "arc":
                    a0, a1 = sorted((np.degrees(s_["start"]), np.degrees(s_["start"] + s_["dr"] * s_["span"])))
                    d.pieslice([SEN_X - 220, y - 220, SEN_X + 220, y + 220], a0, a1, fill=(255, 48, 56, int(46 * k)),
                               outline=(255, 48, 56, int(220 * k)), width=2)
                else:
                    d.ellipse([SEN_X - 220, y - 220, SEN_X + 220, y + 220], outline=(255, 48, 56, int(230 * k)), width=2)
                    d.ellipse([SEN_X - 220, y - 220, SEN_X + 220, y + 220], fill=(255, 48, 56, int(26 * k)))
                    d.line([SEN_X, y, SEN_X + 220 * np.cos(s_["start"]), y + 220 * np.sin(s_["start"])],
                           fill=(255, 48, 56, int(255 * k)), width=3)
                aim = s_["start"]
                live = 0.3
            elif s_["tel"] <= u < s_["tel"] + s_["dur"]:
                w = (u - s_["tel"]) / s_["dur"]
                aim = s_["start"] + s_["dr"] * s_["span"] * w
                live = 1.0
        f.alpha_composite(lay)
        if aim is None:
            sx, sy = sen_ship(t)
            aim = np.arctan2(sy - y, sx - SEN_X)
        if live >= 1.0:
            ex, ey = SEN_X + 220 * np.cos(aim), y + 220 * np.sin(aim)
            d = ImageDraw.Draw(f)
            d.line([SEN_X, y, ex, ey], fill=(40, 6, 10, 255), width=8)
            d.line([SEN_X, y, ex, ey], fill=CRIMSON + (255,), width=6)
            d.line([SEN_X, y, ex, ey], fill=(255, 210, 200, 255), width=2)
        sprite.paste_center(f, sen_head[v].get(aim, live=live), SEN_X, y)
        if live >= 1.0:
            f = light(f, SEN_X + 18 * np.cos(aim), y + 18 * np.sin(aim), 10, CRIMSON, 0.9)
        sx, sy = sen_ship(t)
        ship(f, sx, sy, t, shots=False)
        if 9.5 <= t < 10.5:
            caption(f, "EVERY THIRD SWEEP A FULL TURN: LEAVE THE RING")
        elif 0.5 <= t < 1.3:
            caption(f, "ARC TELEGRAPH 0.8 S, ANY DIRECTION")
        frames.append(f)
    return frames, bg


def sen_sheet(v, frames, bg):
    B, H = sen_body[v], sen_head[v]
    heads = [H.frame(k) for k in range(0, 32, 2)]

    def comp(live=0.0, aim=DOWN):
        c = Image.new("RGBA", (SEN_N, SEN_N), (0, 0, 0, 0))
        sprite.paste_center(c, B.get(DOWN), SEN_N / 2, SEN_N / 2)
        sprite.paste_center(c, H.get(aim, live=live), SEN_N / 2, SEN_N / 2)
        return c
    stump = tint(B.get(DOWN), (30, 24, 22), 0.55)
    panels = [("SOURCE: TOWER AND HEAD, 260 PX", source_panel(lambda: (
                  (lambda p: union(sentinel_body(v)[0](p), sentinel_head(v, 1.0)[0](p))), sentinel_head(v, 1.0)[1]))),
              ("HEAD: 16 OF 32 HEADINGS (1X)", grid(heads, 8, 1)),
              ("IDLE / LIVE / STUMP (1X)", row([comp(), comp(1.0, 2.2), stump], SEN_N, 1)),
              ("READABILITY: OVER THE DARKEST L32 GROUND", readability([(comp(), gold_note(comp()))], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.0, 2.2, 10.0, 12.0],
                                              ["ARC TELEGRAPH", "ARC SWEEP", "FULL-TURN TELL", "FULL TURN"]))]
    if v == "a":
        notes = ["SENTINEL TOWER A - LIGHTHOUSE (Q11 A LIVERY, STRONG GOLD RIM)",
                 "80X80 PX FROM ABOVE: A GOLD-BANDED OCTAGONAL HARDENED COLLAR, A ROUND SHAFT, AND THE EMITTER HEAD",
                 "(32 HEADINGS): A BLACK SPHERE WITH A GOLD EQUATOR RING AND A LENS SNOUT THAT GLOWS CRIMSON WHILE",
                 "LIVE. A LONG SHADOW TO THE LOWER RIGHT GIVES ITS HEIGHT. ARC SWEEP: 120 DEG OVER 1.6 S ROUND THE",
                 "SHIP'S BEARING, THE ARC DRAWN CRIMSON FOR 0.8 S FIRST; EVERY THIRD A FULL TURN (3.0 S) AFTER 1.0 S",
                 "OF THE WHOLE 220 PX REACH RING AND THE START RAY. THE BEAM: 6 PX CRIMSON, A DARK RIM, A PALE CORE."]
    else:
        notes = ["SENTINEL TOWER B - PYLON (Q11 A LIVERY, 1 PX RIM)",
                 "A SQUARE COLLAR WITH FOUR GOLD-EDGED BUTTRESS FINS; THE HEAD IS A FLAT DISC WITH A GOLD RIM AND",
                 "A LONG CRIMSON SLIT LENS: THE SLIT POINTS THE WAY THE BEAM WILL GO. SAME SCENE AS A."]
    return sheet(f"SENTINEL TOWER {v.upper()} - ARC SWEEPS AND THE FULL TURN (LEVEL 32)", panels, notes,
                 frames[int(2.2 * FPS)])


@unit("sentinel-tower")
def make_sentinel(variants):
    for v in variants:
        frames, bg = sen_scene(v)
        save_unit("ground/sentinel-tower", "sentinel-tower", v, sen_sheet(v, frames, bg), frames)


# ===================================================================== 13 Rail Bunker (L33)

def bunker_model(aim=0.0, charge=0.0, glow=1.0):
    """The chosen r04 Rail Bunker (em.rail_bunker) in the belt livery; its three rail coils glow
    gold with ``charge``. ``aim`` is the barrel's screen heading."""
    scene0, _ = em.rail_bunker(aim - DOWN, glow)

    def scene(p):
        d, m = scene0(p)
        m = np.where((m == A_GOLD) & (p[:, 2] > 0.32), A_X, m)
        r = np.hypot(p[:, 0], p[:, 1])
        m = np.where((m == A_HULL) & (r > 0.6), A_GOLD, m)          # the Q11 a gold rim on the octagon
        return d, m
    coil = Material(em.GOLD[3], metal=0.9, shininess=120, spec=1.2,
                    emission=tuple(np.array((1.0, 0.75, 0.25)) * 1.8 * charge * glow))
    return scene, belt_mats("6a6e78", glow, extra=[coil])


RB_N = 140
_bunker_cache = {}


def bunker(aim, charge=0.0):
    k = int(round((aim % TAU) / (TAU / 32))) % 32
    c = round(charge * 3) / 3
    if (k, c) not in _bunker_cache:
        S = rig.AngleSprites(lambda: bunker_model(k * TAU / 32 + DOWN - DOWN, c), RB_N, 4, factor=3, colors=40,
                             post=belt_rim(False))
        _bunker_cache[(k, c)] = S.get(UP)
    return _bunker_cache[(k, c)]


RB_DUR = 10.0
BUNKERS = [dict(x=120, y0=110, tels=[0.8, 5.3]), dict(x=370, y0=250, tels=[2.7, 7.2])]
RB_SCROLL = 0.0


def rb_ship(t):
    return 240 + 120 * np.sin(t * 0.75 + 0.3), 470


def rail_line(img, x0, y0, ang, v, u, t):
    """The lock line from the muzzle through the ship to the edge: a: a solid thin red line
    brightening over the charge; b: a dashed red line with gold chevrons running out along it,
    faster as the charge builds, solid for the last 0.2 s."""
    L = 900
    x1, y1 = x0 + np.cos(ang) * L, y0 + np.sin(ang) * L
    d = ImageDraw.Draw(img)
    if v == "a" or u > 0.8:
        w = 1 if u < 0.5 else 2
        d.line([x0, y0, x1, y1], fill=(255, 48, 56, int(110 + 145 * u)), width=w)
        return img
    n = 60
    for i in range(0, n, 2):
        a, b = i / n * 600, (i + 1) / n * 600
        d.line([x0 + np.cos(ang) * a, y0 + np.sin(ang) * a, x0 + np.cos(ang) * b, y0 + np.sin(ang) * b],
               fill=(255, 48, 56, int(120 + 100 * u)), width=1)
    off = (t * (60 + 260 * u)) % 40
    for s in np.arange(off, 600, 40):
        cx, cy = x0 + np.cos(ang) * s, y0 + np.sin(ang) * s
        for sg in (-1, 1):
            d.line([cx, cy, cx - np.cos(ang + sg * 0.6) * 5, cy - np.sin(ang + sg * 0.6) * 5],
                   fill=(255, 200, 80, 230), width=1)
    return img


def rail_shot(img, x0, y0, ang, s):
    L = 900
    x1, y1 = x0 + np.cos(ang) * L, y0 + np.sin(ang) * L
    d = ImageDraw.Draw(img)
    k = 1.0 - s
    d.line([x0, y0, x1, y1], fill=(40, 6, 10, int(255 * k)), width=12)
    d.line([x0, y0, x1, y1], fill=CRIMSON + (int(255 * k),), width=10)
    d.line([x0, y0, x1, y1], fill=(255, 236, 220, int(255 * k)), width=4)
    return light(img, x0, y0, 14, (255, 220, 200), k)


def rb_scene(v):
    bg = Scroll(rock_bg, 48, RB_DUR, RB_SCROLL, growth=True)
    frames = []
    aims = []
    for n in BUNKERS:
        h, hs = DOWN, []
        locked = None
        for t in times(RB_DUR):
            y = bg.y(n["y0"], t)
            sx, sy = rb_ship(t)
            want = np.arctan2(sy - y, sx - n["x"])
            tel = [t0 for t0 in n["tels"] if t0 <= t < t0 + 1.2]
            if tel:
                if locked is None or locked[0] != tel[0]:
                    locked = (tel[0], want)
                h = locked[1]
            else:
                dh = (want - h + np.pi) % TAU - np.pi
                h += float(np.clip(dh, -np.radians(90) / FPS, np.radians(90) / FPS))
            hs.append(h)
        aims.append(hs)
    for k, t in enumerate(times(RB_DUR)):
        f = bg.frame(t)
        for i, n in enumerate(BUNKERS):
            y = bg.y(n["y0"], t)
            h = aims[i][k]
            charge = 0.0
            for t0 in n["tels"]:
                u = t - t0
                if 0 <= u < 1.0:
                    charge = u
                    mx, my = n["x"] + np.cos(h) * 50, y + np.sin(h) * 50
                    f = rail_line(f, mx, my, h, v, u, t)
            sprite.paste_center(f, bunker(h, charge), n["x"], y)
            for t0 in n["tels"]:
                u = t - t0
                if 1.0 <= u < 1.45:
                    f = rail_shot(f, n["x"] + np.cos(h) * 50, y + np.sin(h) * 50, h, min(1, (u - 1.0) / 0.45))
            if charge > 0:
                f = light(f, n["x"] + np.cos(h) * 24, y + np.sin(h) * 24, 10, (255, 190, 80), 0.4 + 0.6 * charge)
        sx, sy = rb_ship(t)
        ship(f, sx, sy, t, stop=None)
        if 3.8 < t < 5.2:                                  # bolts glance off the hardened bunker
            n = BUNKERS[1]
            if abs(sx - n["x"]) < 40:
                f = spark(f, n["x"] + 8 * np.sin(t * 20), bg.y(n["y0"], t) + 30, (t * 8) % 1, (170, 170, 170))
                label(f, n["x"] + 36, bg.y(n["y0"], t) + 24, "HARDENED", (200, 200, 200))
        if 0.8 < t < 1.8:
            caption(f, "LOCK: THE LINE IS FIXED; 1.0 S TO STEP OUT OF IT")
        frames.append(f)
    return frames, bg


def rb_sheet(v, frames, bg):
    aims = [bunker(k * TAU / 32) for k in range(0, 32, 4)]
    charge = [bunker(DOWN, c) for c in (0.0, 1 / 3, 2 / 3, 1.0)]
    panels = [("TURRET: 8 OF 32 HEADINGS (1X)", grid(aims, 8, 1)),
              ("CHARGE: THE COILS GLOW GOLD (1X)", grid(charge, 4, 1)),
              ("READABILITY: OVER THE DARKEST L33 GROUND", readability([(charge[0], gold_note(charge[0]))], bg.img, (1,))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.2, 1.75, 3.3, 3.75],
                                              ["LOCK LINE", "CHARGED", "SECOND BUNKER", "RAIL SHOT"]))]
    if v == "a":
        notes = ["RAIL BUNKER A - SOLID LOCK LINE (THE CHOSEN R04 BUNKER IN THE Q11 A LIVERY; ONLY THE RAIL LINE IS NEW)",
                 "72 PX OCTAGON, TURRET 32 HEADINGS (90 DEG/S). AT THE TELEGRAPH'S START THE LINE LOCKS FROM THE MUZZLE",
                 "THROUGH THE SHIP TO THE SCREEN EDGE: A THIN RED LINE (1 PX, THEN 2 PX) BRIGHTENING OVER 1.0 S WHILE THE",
                 "THREE COILS GLOW GOLD; THEN THE RAIL: 10 PX, CRIMSON WITH A DARK RIM AND A WHITE CORE, LIVE 0.2 S",
                 "AND FADING. A SIDESTEP OUT OF THE 8 PX BAND IS ENOUGH. BOLTS GLANCE OFF (HARDENED). Q11 A ADDS A GOLD",
                 "RIM ROUND THE OCTAGON (THE R04 BUNKER HAD ~10 % GOLD)."]
    else:
        notes = ["RAIL BUNKER B - DASHED LINE WITH RUNNING CHEVRONS (THE CHOSEN R04 BUNKER; ONLY THE RAIL LINE IS NEW)",
                 "THE LOCK LINE IS DASHED RED WITH GOLD CHEVRONS RUNNING OUT ALONG IT, FASTER AS THE CHARGE BUILDS;",
                 "IT GOES SOLID FOR THE LAST 0.2 S. MORE MOTION TO CATCH THE EYE, MORE CLUTTER. SAME SHOT AS A."]
    return sheet(f"RAIL BUNKER {v.upper()} - THE RAIL LINE (LEVEL 33)", panels, notes, frames[int(1.75 * FPS)])


@unit("rail-bunker")
def make_bunker(variants):
    for v in variants:
        frames, bg = rb_scene(v)
        save_unit("ground/rail-bunker", "rail-bunker", v, rb_sheet(v, frames, bg), frames)


# ===================================================================== 14 Strider (L32): the topple

STR_N = 120
STR_SCALE = STR_N / 2.3
STR_BIG = 220                                       # canvas for the falling frames (same px scale)


def strider_mats(glow=1.0, hatch=0.0):
    return belt_mats("c8202a", glow, extra=[emissive(GOLD_F, 0.05 + 1.6 * hatch)])


def strider_torso(hatch=0.0, glow=1.0):
    """The chosen r05 torso with the new shoulder pod (left shoulder, behind the cannon) whose
    hatch glows gold before a salvo."""
    s0, _ = am.strider_torso(glow)

    def scene(p):
        return union(s0(p), (sd_box(p, (-0.36, -0.28, 0.92), (0.14, 0.15, 0.09), 0.02), A_HULL),
                     (sd_box(p, (-0.36, -0.22, 1.02), (0.1, 0.07, 0.02)), A_X),
                     (sd_box(p, (-0.36, -0.4, 1.0), (0.14, 0.02, 0.02)), A_GOLD))
    return scene, strider_mats(glow, hatch)


def strider_legs(phase=0.0, glow=1.0):
    s0, _ = am.strider_legs(phase, glow)
    return s0, strider_mats(glow)


def strider_whole(phase=0.8, tilt=0.0, side=1, sink=0.0, split=0.0, glow=1.0):
    """Legs and torso (facing +Y) tipped over toward ``side`` (model x) by ``tilt`` radians about
    the foot line; ``sink`` lowers the body (b's buckled knee); ``split`` throws the torso off
    the legs (b's break-up)."""
    sl, _ = am.strider_legs(phase, glow)
    st, mats = strider_torso(0.0, glow)
    pivot = np.array((side * 0.5, 0.0, 0.0))

    def tip(p, ang):
        q = p - pivot
        return rotate_y(q, -side * ang) + pivot

    def scene(p):
        pl = tip(p, tilt)
        pl = pl + np.array((0, 0, sink))
        pt = tip(p - np.array((side * 0.5 * split, 0.1 * split, 0)), tilt + 0.25 * split)
        pt = pt + np.array((0, 0, sink))
        return union(sl(pl), st(pt))
    return scene, mats


str_legs = Turning(lambda **kw: strider_legs(**kw), STR_N, 16, factor=3, colors=40, post=belt_rim(True))
str_torso = Turning(lambda **kw: strider_torso(**kw), STR_N, 32, factor=3, colors=40, post=belt_rim(True))
str_fall = Turning(lambda **kw: strider_whole(**kw), STR_BIG, 16, factor=3, colors=48, post=belt_rim(True),
                   extent=2.3 * STR_BIG / STR_N)
STR_DUR = 10.0
STR_HITS = [3.0, 3.4, 3.8, 4.2]
STR_LEG = 4.6
STR_FALL = (5.2, 6.0)
STR_FANS = [1.4, 3.6]
STR_POD = 2.4
STR_SIDE = 1                                        # the lost leg's side (model x)
STR_REST = 1.2                                      # the hulk comes to rest on its side, propped (69 deg)


def fall_frame(v, u):
    """Topple frame at fall progress u (0..1). a: a rigid fall about the foot line, accelerating;
    b: the knee buckles (the body sinks and leans 25 deg) in the first third, then it pitches
    over and the torso breaks off the legs."""
    if v == "a":
        tilt = STR_REST * u * u
        return str_fall.get(DOWN, tilt=round(tilt, 2), side=STR_SIDE)
    if u < 0.35:
        w = u / 0.35
        return str_fall.get(DOWN, tilt=round(0.44 * w, 2), side=STR_SIDE, sink=round(-0.18 * w, 2))
    w = (u - 0.35) / 0.65
    return str_fall.get(DOWN, tilt=round(0.44 + (STR_REST - 0.44) * w * w, 2), side=STR_SIDE, sink=-0.18,
                        split=round(w, 2))


def str_pos(t, bg):
    tt = min(t, STR_LEG)
    return 250 + 24 * np.sin(tt * 0.7), bg.y(-20 + 40 * tt, t)


def str_scene(v):
    bg = Scroll(tunnel_bg, 49, STR_DUR, 0)
    px_of = follow(lambda t: 210 + 40 * np.sin(t * 0.8) if t < 3.0 else 205, STR_DUR, 240, 160)
    py = 478
    side_dx, side_dy = rig.local_to_screen(STR_SIDE, 0, DOWN, 1.0)      # unit vector to the fall side
    shots = Shots()
    for t0 in STR_FANS:
        x, y = str_pos(t0, bg)
        aim = np.arctan2(py - y, px_of(t0) - x)
        for sg in (-1, 1):
            ox, oy = rig.local_to_screen(sg * 0.56, 0.7, aim, STR_SCALE)
            shots.fan(t0, x + ox, y + oy, aim, 3, np.radians(24), 160, 5)
    ms = Homing(v, lambda t: (px_of(t), py))
    for j in range(2):
        x, y = str_pos(STR_POD + 0.25 * j, bg)
        ms.launch(STR_POD + 0.25 * j, x - 18, y - 14, UP + 0.6 * (j - 0.5), {1: 4.0}.get(j))
    # the Rail Bunker under the fall line (crushed on landing)
    x_leg, y_leg = str_pos(STR_LEG, bg)
    bunker_y0 = (-20 + 40 * STR_LEG) + side_dy * 80
    bunker_x = x_leg + side_dx * 80
    frames = []
    for t in times(STR_DUR):
        f = bg.frame(t)
        x, y = str_pos(t, bg)
        by = bg.y(bunker_y0, t)
        crushed = t >= STR_FALL[1]
        if not crushed:
            sprite.paste_center(f, bunker(DOWN + 0.3), bunker_x, by)
        if STR_LEG <= t:                                    # the fall line strip, 48 x 140 from the centre
            k = min(1.0, (t - STR_LEG) / 0.3)
            lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
            d = ImageDraw.Draw(lay)
            nx, ny = -side_dy, side_dx
            pts = [(x + nx * 24, y + ny * 24), (x + nx * 24 + side_dx * 140, y + ny * 24 + side_dy * 140),
                   (x - nx * 24 + side_dx * 140, y - ny * 24 + side_dy * 140), (x - nx * 24, y - ny * 24)]
            a_ = int((170 if t < STR_FALL[1] else 90) * k)
            d.polygon(pts, fill=(6, 4, 4, a_), outline=(200, 50, 40, a_) if t < STR_FALL[1] else None)
            f.alpha_composite(lay)
        if t < STR_LEG:
            ph = int(t * 3.2 * 4) % 8
            legs = str_legs.get(DOWN, phase=ph * TAU / 8)
            sprite.paste_center(f, sprite.shadow_of(legs, opacity=0.5, blur=1.5), x + 12, y + 16)
            aim = np.arctan2(py - y, px_of(t) - x)
            aim = float(np.clip(aim, DOWN - 1.0, DOWN + 1.0))
            hatch = 1.0 if STR_POD - 0.4 <= t < STR_POD + 0.4 else 0.0
            sprite.paste_center(f, legs, x, y)
            sprite.paste_center(f, str_torso.get(aim, hatch=hatch), x, y)
            kx, ky = rig.local_to_screen(STR_SIDE * 0.68, 0.1, DOWN, STR_SCALE)
            for th in STR_HITS:
                if 0 <= t - th < 0.2:
                    f = light(f, x + kx, y + ky, 9, (255, 40, 30), 1.4)
            for t0 in STR_FANS:
                if t0 - 0.3 <= t < t0:
                    for sg in (-1, 1):
                        ox, oy = rig.local_to_screen(sg * 0.56, 0.7, aim, STR_SCALE)
                        f = light(f, x + ox, y + oy, 5, (255, 200, 120), 1.0)
        elif t < STR_FALL[0]:                               # the stagger: sparks at the knee, a lurch
            w = (t - STR_LEG) / (STR_FALL[0] - STR_LEG)
            lx, ly = x + side_dx * 5 * w, y + side_dy * 5 * w
            sprite.paste_center(f, fall_frame(v, 0.0), lx, ly)
            kx, ky = rig.local_to_screen(STR_SIDE * 0.68, 0.1, DOWN, STR_SCALE)
            f = spark(f, x + kx, y + ky, (t * 6) % 1, (255, 200, 120), 7, 14)
        else:
            u = min(1.0, (t - STR_FALL[0]) / (STR_FALL[1] - STR_FALL[0]))
            sp = fall_frame(v, u)
            if crushed:
                sp = tint(sp, (24, 18, 16), min(0.55, (t - STR_FALL[1]) * 0.8))
            sprite.paste_center(f, sp, x + side_dx * 5, y + side_dy * 5)
        if crushed:
            u = t - STR_FALL[1]
            f = boom(f, bunker_x, by, u / 0.9, "medium")
            if u < 0.5:                                     # the ground shock: a dust ring
                d = ImageDraw.Draw(f)
                r = 20 + 120 * u
                cx, cy = x + side_dx * 60, y + side_dy * 60
                d.ellipse([cx - r, cy - r * 0.8, cx + r, cy + r * 0.8], outline=(180, 160, 130, int(220 * (1 - u / 0.5))), width=3)
            if u < 1.6:
                label(f, bunker_x - 30, by - 50, "CRUSHED +CREDITS", (255, 220, 140))
        shots.draw(f, t)
        f = ms.draw(f, t)
        ship(f, px_of(t), py, t, stop=y + 40 if t < STR_LEG else None)
        if STR_LEG <= t < STR_FALL[0] + 0.2:
            caption(f, "LEG DOWN: THE STAGGER MARKS THE FALL LINE (0.6 S)")
        elif STR_FALL[1] <= t < STR_FALL[1] + 1.5:
            caption(f, "THE TOPPLE CRUSHES THE GROUND UNITS UNDER THE LINE")
        frames.append(f)
    return frames, bg


def str_sheet(v, frames, bg):
    falls = [fall_frame(v, u) for u in (0.0, 0.2, 0.4, 0.6, 0.8, 1.0)]
    falls = [f.crop((0, 50, STR_BIG - 30, STR_BIG - 50)) for f in falls]
    wreck = tint(fall_frame(v, 1.0), (24, 18, 16), 0.55).crop((0, 50, STR_BIG - 30, STR_BIG - 50))
    whole = Image.new("RGBA", (STR_N, STR_N), (0, 0, 0, 0))
    sprite.paste_center(whole, str_legs.get(DOWN), STR_N / 2, STR_N / 2)
    sprite.paste_center(whole, str_torso.get(DOWN), STR_N / 2, STR_N / 2)
    hatch = [str_torso.get(DOWN), str_torso.get(DOWN, hatch=1.0)]
    panels = [("THE TOPPLE: 0 / 0.2 / 0.4 / 0.6 / 0.8 / 1.0 OF THE FALL (1X)", grid(falls + [wreck], 7, 1)),
              ("POD: IDLE / HATCH (1X)", grid(hatch, 2, 1)),
              ("READABILITY: DARKEST L32 GROUND", readability([(whole, gold_note(whole))], bg.img, (1,))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [3.0, 4.9, 5.6, 6.3],
                                              ["KNEE HITS GLOW RED", "STAGGER + FALL LINE", "FALLING", "CRUSH"]))]
    if v == "a":
        notes = ["STRIDER A - RIGID FALL (THE CHOSEN R05 WALKER IN THE Q11 A LIVERY; ONLY THE TOPPLE AND THE POD ARE NEW)",
                 "A HIT KNEE FLASHES RED. THE LEG LOST: THE STAGGER (0.6 S) SPARKS AT THE KNEE, THE HULL LURCHES 5 PX AND",
                 "THE FALL LINE IS MARKED ON THE GROUND: A DARK STRIP 48 X 140 PX ON THE LOST LEG'S SIDE. THEN IT FALLS",
                 "AS ONE PIECE ABOUT ITS FOOT LINE, ACCELERATING (0.8 S), LANDS WITH A DUST RING AND CRUSHES THE GROUND",
                 "UNITS UNDER THE STRIP (HERE A RAIL BUNKER, ITS CREDITS PAID); THE DARKENED HULK STAYS. NEW SHOULDER",
                 "POD BEHIND THE LEFT CANNON: ITS HATCH GLOWS GOLD 0.4 S BEFORE A SALVO OF 2 HOMING DIAMONDS."]
    else:
        notes = ["STRIDER B - BUCKLE AND CRUMPLE (THE CHOSEN R05 WALKER; ONLY THE TOPPLE AND THE POD ARE NEW)",
                 "THE LOST LEG'S KNEE BUCKLES FIRST (THE BODY SINKS AND LEANS 25 DEG IN THE FIRST THIRD), THEN IT",
                 "PITCHES OVER ONTO THE LINE AND THE TORSO BREAKS OFF THE LEGS: TWO PIECES IN THE WRECK. HEAVIER AND",
                 "MORE MECHANICAL, A LITTLE LESS CLEAR WHERE IT WILL LAND. SAME STAGGER, LINE AND CRUSH AS A."]
    return sheet(f"STRIDER {v.upper()} - STAGGER, FALL LINE, TOPPLE, CRUSH (LEVEL 32)", panels, notes,
                 frames[int(5.6 * FPS)])


@unit("strider")
def make_strider(variants):
    for v in variants:
        frames, bg = str_scene(v)
        save_unit("ground/strider", "strider", v, str_sheet(v, frames, bg), frames)


# ===================================================================== 15 Warden Tank in livery (L34)

def warden_mats(v, glow=1.0, tread=0.0):
    mats = belt_mats("e8e4f0", glow, red=1.3)
    return mats + [am.asc("warden-tank", glow, tread)[am.A_TREAD]]


def warden_hull(v, tread=0.0, glow=1.0):
    """The chosen r05 hull (am.warden_hull) in the belt livery. a: the r05 gold side stripes,
    red running lights, the strong rim; b: gold track guards and a gold engine deck as well."""
    s0, _ = am.warden_hull(tread, glow)

    def scene(p):
        d, m = s0(p)
        if v == "b":
            m = np.where((m == am.A_TREAD) & (p[:, 2] > 0.17), A_GOLD, m)
            m = np.where(m == A_FACET, A_GOLD, m)
        else:
            m = np.where((m == am.A_TREAD) & (np.abs(p[:, 0]) > 0.72), A_GOLD, m)   # gold track edges
        return d, m
    return scene, warden_mats(v, glow, tread)


def warden_turret(v, glow=1.0):
    s0, _ = am.warden_turret(glow)

    def scene(p):
        d, m = s0(p)
        if v == "b":
            r = np.hypot(p[:, 0], p[:, 1] + 0.05)
            m = np.where((m == A_HULL) & (r > 0.3), A_GOLD, m)
        return d, m
    return scene, warden_mats(v, glow)


WT_N = 76
wt_hull = {v: Turning(lambda v=v, **kw: warden_hull(v, **kw), WT_N, 16, factor=5, colors=36, post=belt_rim(v == "a"))
           for v in "ab"}
wt_tur = {v: Turning(lambda v=v: warden_turret(v), WT_N, 32, factor=5, colors=32, post=belt_rim(v == "a"))
          for v in "ab"}
WT_DUR = 9.0
WT_PATH = rig.catmull_rom([(-60, 80), (90, 110), (220, 170), (300, 250), (260, 340), (150, 400), (60, 470),
                           (-60, 520)], [0, 1.4, 2.8, 4.0, 5.2, 6.4, 7.6, 9.0])


def wt_scene(v):
    bg = Scroll(derelict_bg, 50, WT_DUR, 25)
    px_of = follow(lambda t: 250 + 110 * np.sin(t * 0.7), WT_DUR, 240, 180)
    py = 478
    shots = Shots()
    turret, h = [], DOWN
    fires = [1.6, 4.1, 6.6]
    for t in times(WT_DUR):
        x, y = WT_PATH(t)
        want = np.arctan2(py - y, px_of(t) - x)
        if not any(t0 - 0.3 <= t < t0 + 0.4 for t0 in fires):
            dh = (want - h + np.pi) % TAU - np.pi
            h += float(np.clip(dh, -np.radians(90) / FPS, np.radians(90) / FPS))
        turret.append(h)
    for t0 in fires:
        for j in range(3):
            tt = t0 + 0.15 * j
            x, y = WT_PATH(tt)
            a = turret[int(tt * FPS)]
            shots.fire(tt, x + np.cos(a) * 34, y + np.sin(a) * 34, x + np.cos(a) * 100, y + np.sin(a) * 100, 160, 5)
    frames = []
    for k, t in enumerate(times(WT_DUR)):
        f = bg.frame(t)
        x, y = WT_PATH(t)
        hh = rig.path_heading(WT_PATH, t)
        hull = wt_hull[v].get(hh, tread=0.5 if int(t * 4.4) % 2 else 0.0)
        sprite.paste_center(f, sprite.shadow_of(hull, opacity=0.55, blur=1.2), x + 8, y + 10)
        sprite.paste_center(f, hull, x, y)
        sprite.paste_center(f, wt_tur[v].get(turret[k]), x, y)
        for t0 in fires:
            for j in range(3):
                if 0 <= t - (t0 + 0.15 * j) < 0.08:
                    a = turret[k]
                    f = light(f, x + np.cos(a) * 36, y + np.sin(a) * 36, 7, (255, 220, 160), 1.0)
        shots.draw(f, t)
        ship(f, px_of(t), py, t)
        if 6.2 < t < 7.6:
            caption(f, "IT FIRES BACK UP THE SCREEN AFTER IT HAS PASSED")
        frames.append(f)
    return frames, bg


def wt_sheet(v, frames, bg):
    H, T = wt_hull[v], wt_tur[v]

    def comp(a=DOWN, dark=False):
        c = Image.new("RGBA", (WT_N, WT_N), (0, 0, 0, 0))
        sprite.paste_center(c, H.get(DOWN), WT_N / 2, WT_N / 2)
        sprite.paste_center(c, T.get(a), WT_N / 2, WT_N / 2)
        return tint(c, (26, 20, 18), 0.5) if dark else c
    hulls = [H.frame(k) for k in range(16)]
    panels = [("SOURCE: HULL AND TURRET, 260 PX", source_panel(lambda: (
                  (lambda p: union(warden_hull(v)[0](p), warden_turret(v)[0](p))), warden_mats(v)))),
              ("HULL: 16 HEADINGS (1X)", grid(hulls, 8, 1)),
              ("TANK / AIMING / WRECK (2X)", row([comp(), comp(DOWN + 1.2), comp(dark=True)], WT_N, 2)),
              ("READABILITY: OVER THE DARKEST L34 GROUND", readability([(comp(), gold_note(comp()))], bg.img, (1, 2))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.7, 4.2, 6.8], ["BURST", "TURRET TRACKS", "FIRING BACK UP"]))]
    if v == "a":
        notes = ["WARDEN TANK A - THE R05 LIVERY WITH RED RUNNING LIGHTS (THE CHOSEN R05 TANK, Q11 A, STRONG GOLD RIM)",
                 "76 PX, HULL 16 HEADINGS X 2 TREAD FRAMES, TURRET 32 HEADINGS. THE ACT 3 MODEL UNCHANGED, ITS LIVERY",
                 "BLACK AND GOLD: GLOSS BLACK HULL, THE R05 GOLD SIDE STRIPES, TURRET RING AND MUZZLE, GOLD TRACK EDGES, THE WHITE",
                 "ACCENT, BRIGHT RED RUNNING LIGHTS, A 2 PX GOLD RIM. ON L34'S DERELICT HULLS IT FIRES 3-SHOT BURSTS",
                 "(0.15 S APART, A 0.3 S STILL TURRET FIRST) IN ANY DIRECTION, UP THE SCREEN TOO. ITS WRECK IS",
                 "DARKENED BLACK AND GOLD."]
    else:
        notes = ["WARDEN TANK B - HEAVIER GOLD (THE CHOSEN R05 TANK, Q11 A, 1 PX RIM)",
                 "GOLD TRACK GUARDS, A GOLD ENGINE DECK AND A GOLD TURRET ROOF ON THE SAME MODEL: IT READS AS AN",
                 "ASCENDANCY TANK FROM ACROSS THE SCREEN, LESS AS THE ACT 3 TANK REPAINTED. SAME SCENE AS A."]
    return sheet(f"WARDEN TANK {v.upper()} - IN ASCENDANCY LIVERY ON THE DERELICTS (LEVEL 34)", panels, notes,
                 frames[int(6.8 * FPS)])


@unit("warden-tank")
def make_wt(variants):
    for v in variants:
        frames, bg = wt_scene(v)
        save_unit("ground/warden-tank", "warden-tank", v, wt_sheet(v, frames, bg), frames)


# ===================================================================== 16 The homing diamond

HD_DUR = 8.0


def hd_scene(v):
    bg = Scroll(space_bg, 51, HD_DUR, 30)
    px_of = follow(lambda t: 240 + 130 * np.sin(t * 0.9), HD_DUR, 240, 220)
    py = 478
    ms = Homing(v, lambda t: (px_of(t), py))
    kills = {1: 2.6, 4: 5.3}
    starts = [(0.3, 80, -10), (0.8, 400, -10), (1.6, 240, -10), (2.6, -10, 160), (3.4, FW + 10, 120), (4.4, 150, -10)]
    for i, (t0, x, y) in enumerate(starts):
        ms.launch(t0, x, y, np.arctan2(py - y, 240 - x), kills.get(i))
    frames = []
    for t in times(HD_DUR):
        f = bg.frame(t)
        f = ms.draw(f, t)
        ship(f, px_of(t), py, t)
        if t < 2.0:
            caption(f, "HOMING DIAMONDS: SHOOT THEM OR FLARE THEM")
        frames.append(f)
    return frames, bg


def hd_sheet(v, frames, bg):
    heads = [diamond(v, k) for k in range(32)]
    ref = Image.new("RGBA", (24, 24), (0, 0, 0, 0))
    small, med, ndl = ref.copy(), ref.copy(), ref.copy()
    e3.orb(small, 12, 12, 4)
    e3.orb(med, 12, 12, 5)
    e3.thorn(ndl, 12, 12, 0.0, 7, ring=(255, 255, 64))
    grounds = []
    for make, seed, kw, lab in ((space_bg, 3, {}, "L30 SPACE"), (rock_bg, 5, dict(growth=True), "L33 ROCK"),
                                (hull_bg, 7, {}, "L29 HULL"), (tunnel_bg, 9, {}, "L32 TUNNEL"),
                                (derelict_bg, 11, {}, "L34 DERELICT")):
        grounds.append((darkest(make(seed, **kw), 40), lab))
    cells = []
    for g, lab in grounds:
        c = g.copy()
        sprite.paste_center(c, diamond_at(v, DOWN + 0.4), 20, 20)
        cells.append(c)
    pops = []
    for s in (0.0, 0.33, 0.66, 1.0):
        c = Image.new("RGBA", (24, 24), (10, 10, 16, 255))
        c = spark(c, 12, 12, s, (255, 210, 140), 6, 9)
        pops.append(c)
    panels = [("32 HEADINGS (2X)", grid(heads, 16, 2)),
              ("4X: THE DIAMOND / SMALL ORB / MEDIUM ORB / NEEDLE", grid([diamond_at(v, DOWN), small, med, ndl], 4, 4)),
              ("OVER THE DARKEST GROUND OF FIVE BELT LEVELS (3X)",
               grid(cells, 5, 3, labels=[lab for _, lab in grounds])),
              ("SHOT DOWN: THE POP (3X)", grid(pops, 4, 3)),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.8, 2.8, 5.0], ["HOMING", "ONE SHOT DOWN", "FIZZLE AT 4 S"]))]
    if v == "a":
        notes = ["HOMING DIAMOND A - RED DIAMOND, GOLD HEART (THE ART DIRECTION'S ITEM, ROUND 59 C.7)",
                 "ABOUT 14X10 PX, 32 HEADINGS: A BROAD CRIMSON-RED DIAMOND WITH A DARK RIM, A GOLD INNER DIAMOND, A",
                 "WHITE-HOT CORE (THE HIT BOX, A MEDIUM BULLET'S) AND A SHORT THIN PALE-GOLD EXHAUST (NO SMOKE IN",
                 "VACUUM). EVERY ASCENDANCY HOMING MISSILE OF ACT 5 (HORNET, SAM NEST, STRIDER POD, SOVEREIGN",
                 "BATTERIES): ONE SHAPE FOR 'SHOOT THIS OR FLARE IT'. 110 PX/S, 80 DEG/S, 4 S, 2 HP; SHOT DOWN IT",
                 "POPS IN A SPARK, AT THE END OF ITS LIFE IT FIZZLES IN A FAINT RING. NO MAGENTA, NO NEEDLE YELLOW."]
    else:
        notes = ["HOMING DIAMOND B - GOLD KITE, RED CORE",
                 "ABOUT 15X8 PX: A SLIMMER GOLD KITE WITH A LONG NOSE, A RED CORE AND A WHITE TIP, A DOTTED EXHAUST:",
                 "READS MORE AS A MISSILE, A LITTLE CLOSER TO THE GOLD OF THE HULLS (A READABILITY RISK NEXT TO GOLD",
                 "UNITS). SAME SCENE AS A."]
    return sheet(f"HOMING DIAMOND {v.upper()} - THE ASCENDANCY'S SHOOTABLE MISSILE (ACT 5)", panels, notes,
                 frames[int(2.8 * FPS)])


@unit("homing-diamond")
def make_hd(variants):
    for v in variants:
        frames, bg = hd_scene(v)
        png = DESIGN / "concept" / f"homing-diamond-r60-{v}.png"
        hd_sheet(v, frames, bg).convert("RGB").save(png, optimize=True)
        size = rig.write_gif(frames, DESIGN / "concept" / f"homing-diamond-r60-{v}.gif", fps=FPS)
        print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB)", flush=True)


# ##INSERT-UNITS##


def main(args):
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    names = [a for a in args if a in UNITS] or list(UNITS)
    bad = [a for a in args if a not in UNITS and a not in ("a", "b")]
    if bad:
        sys.exit(f"unknown: {bad}; units: {', '.join(UNITS)}")
    for n in names:
        UNITS[n](variants)


if __name__ == "__main__":
    main(sys.argv[1:])
