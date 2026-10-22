#!/usr/bin/env python3
"""Concept round 48 - Europa (Act 4 "Deep Water"): mood stills per level, the a/b looks the Act 4
level drafts ask for, the ice breakout and the hydro-kit Stormhawk (Act 4 design phase, step
A-europa, 2026-10-21; everything pending the user's review in round 48).

Outputs (design/world/europa/concept/), every PNG a review sheet with the play field at 1x:
  europa-ice-shelf-r48-a.png      L22: the shelf in hard sunlight, an open lead mirroring Jupiter,
                                  a Vrell bore site with its steaming cracks, an ice geyser
  europa-descent-r48-a.png        L23 sections 1-2: the drilled shaft, the waterline crossing
                                  (dry ice below, the water grade above)
  europa-thera-deep-r48-a.png     L23 section 3: the chosen r07 kelp farms, now lamp-lit (grow-lamp
                                  pools, the water dark between them), Vrell siphon pumps
  europa-abyssal-plain-r48-a.png  L24: the silt plain at the lamps' edge, spawning reefs, a relay
                                  beacon, an Abyss Ray's shadow below the plane
  europa-siren-trench-r48-a.png   L25: trench walls with nests, a cave notch, beacons, a sonar ring
  europa-vents-r48-a.png          L27: black smokers, amber mats, a heat plume, the Hunter wreck
  europa-hatchery-r48-a.png       L28: the brood galleries (egg racks), the vent's glow ahead
  europa-breakout-r48-a.png       L28 sections 6-8: the climb, the ice ceiling shattering, the
                                  night ice under Jupiter (three frames at 1x)
  underwater-grade-r48-{a,b}.png/.gif  Q2: (a) lamp-lit to black, level by level; (b) dark
                                  everywhere from L23 (headlight and glow only); L24 at 1x and the
                                  L23-L28 ladder
  darkness-r48-{a,b}.png/.gif     L26 (Q4 a, a look only): (a) the spec's hard headlight cone (70
                                  degrees, 260 px), glow-only units with a 15 % rim; (b) a soft lamp
                                  pool round the ship and a long faint beam with back-scatter
  hydro-kit-r48-{a,b}.png         the Stormhawk's free hydro-kit: (a) sealed refit (pump-jet
                                  shrouds, ballast saddles, twin headlights); (b) pontoon kit
                                  (wing pontoons with lamps, a sonar blister, ducted engines)

Units on the stills are the production sprites of the returning units (assets/sprites) and the
chosen round-06 Nautilus and Driftjelly, for scale; the new Act 4 units are on their own round-48
sheets (enemies_r48.py) and stand-ins here are drawn as simple shapes (the Glow Angler's lure).
The light model: a level's albedo is lit by an ambient map, point lights and the ship's headlight
(posterized light steps), the water adds haze and in-scatter, emissive glows stay bright and bloom.
Rules: design/art-direction/README.md, design/world/europa/README.md (under water, darkness).
Run: python3 tools/concept/europa_r48.py [ice] [descent] [thera] [plain] [trench] [vents]
     [hatchery] [breakout] [grade] [darkness] [kit] [--no-gif]     (no subject = everything)
"""
import subprocess
import sys
import tempfile
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import mars_r36 as m36  # noqa: E402  (field sheet, height-field helpers; imported unchanged)
import parallax_r02 as r02  # noqa: E402
from parallax_r02 import H, W  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.palette import B  # noqa: E402

OUT = ROOT / "design" / "world" / "europa" / "concept"
ASSETS = ROOT / "assets" / "sprites"
SUB = "CONCEPT ROUND 48 - EUROPA"
TAU = 2 * np.pi
FPS, SECONDS = 20, 4
YY, XX = np.mgrid[0:H, 0:W].astype(np.float64)

WATER = np.array((8, 74, 92), dtype=np.float64)       # r07's water colour
DEEP = np.array((3, 14, 24), dtype=np.float64)
TEAL, LIME, VIOLET = (0, 255, 154), (168, 255, 42), (154, 77, 255)
AMBER, GOLD, BEACON = (255, 150, 60), (255, 196, 64), (220, 48, 36)
WARM = (1.0, 0.82, 0.55)                               # sodium / grow-lamp light
COOL = (0.55, 0.8, 0.95)                               # ambient light in the water
CONE_COL = np.array((150, 200, 210), dtype=np.float64)


# =========================================================================== small helpers

@lru_cache(maxsize=None)
def spr(name, k=0):
    p = ASSETS / f"{name}_{k}.png"
    if not p.exists():
        p = ASSETS / f"{name}.png"
    return Image.open(p).convert("RGBA")


def nfield(cell, seed, octaves=4, w=W, h=H):
    return m36.nfield(w, h, cell, seed, octaves=octaves)


def gauss2(x, y, r, xx=XX, yy=YY):
    return np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (2.0 * r * r))


def blank(w=W, h=H):
    return Image.new("RGBA", (w, h), (0, 0, 0, 0))


def to_img(rgb, alpha=None):
    return raster.to_rgba_image(np.clip(rgb, 0, 255), alpha)


def soft_blobs(seed, centres, col, max_a=170, cell=14, blur=3):
    """Vapour / steam: noise inside soft discs (x, y, r), returned as an RGBA layer."""
    n = nfield(cell, seed, 3)
    a = np.zeros((H, W))
    for x, y, r in centres:
        d = np.hypot(XX - x, YY - y) / r
        a = np.maximum(a, np.clip(1 - d, 0, 1) ** 0.8 * (0.55 + 0.6 * n))
    a = np.clip(a, 0, 1)
    a = np.round(a * 5) / 5 * max_a
    img = to_img(np.ones((H, W, 3)) * np.array(col), a)
    return img.filter(ImageFilter.GaussianBlur(blur))


def steam(seed, x, y, length=70, angle=-0.6, size=24, col=(246, 250, 255), max_a=235):
    """A drifting steam plume: lumps along a drift line, shaded from the top left (volume), with
    a soft cast shadow, so white vapour reads on white ice."""
    rng = np.random.default_rng(seed)
    n = nfield(10, seed, 3)
    a = np.zeros((H, W))
    for k in range(7):
        f = k / 6
        cx = x + np.cos(angle) * length * f + rng.uniform(-6, 6)
        cy = y + np.sin(angle) * length * f + rng.uniform(-6, 6)
        r = size * (1.0 - 0.45 * f) * rng.uniform(0.8, 1.1)
        d = np.hypot(XX - cx, YY - cy) / r
        a = np.maximum(a, np.clip(1 - d ** 2, 0, 1) * (0.7 + 0.5 * n) * (1 - 0.5 * f))
    a = np.clip(a, 0, 1)
    blur = np.array(Image.fromarray((a * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(5)),
                    dtype=np.float64) / 255
    gy, gx = np.gradient(blur)
    shade = np.clip(1 + (gx + gy) * 70, 0.5, 1.12)
    rgb = np.array(col)[None, None, :] * shade[..., None] * np.array((0.92, 0.95, 1.0))
    alpha = np.round(a * 6) / 6 * max_a
    img = to_img(rgb, alpha).filter(ImageFilter.GaussianBlur(1.2))
    return shadowed(img, 16, 22, 0.6)


def shadowed(layer, dx=14, dy=20, k=0.35):
    """A vapour layer with its own soft shadow (so white steam reads on white ice)."""
    a = np.array(layer).astype(np.float64)
    sh = np.zeros_like(a)
    sh[..., 3] = np.roll(np.roll(a[..., 3], dy, 0), dx, 1) * k
    sh[..., :3] = (20, 40, 70)
    out = Image.fromarray(sh.astype(np.uint8), "RGBA").filter(ImageFilter.GaussianBlur(4))
    out.alpha_composite(layer)
    return out


def glow_mask(sp):
    a = np.array(sp).astype(np.float64)
    lum = 0.3 * a[..., 0] + 0.59 * a[..., 1] + 0.11 * a[..., 2]
    mx, mn = a[..., :3].max(-1), a[..., :3].min(-1)
    sat = (mx - mn) / np.maximum(mx, 1)
    return (a[..., 3] > 128) & ((lum > 205) | ((sat > 0.55) & (mx > 190)))


def crop_map(L, x0, y0, w, h):
    ys = np.clip(np.arange(y0, y0 + h), 0, H - 1)
    xs = np.clip(np.arange(x0, x0 + w), 0, W - 1)
    return L[np.ix_(ys, xs)]


def lit_sprite(sp, Lc, rim=0.0, tint=0.12, keep_glow=True):
    """``sp`` lit per pixel by the light crop ``Lc`` (h, w, 3), tinted toward the water; glow pixels
    keep their colour; ``rim`` is the floor brightness of the body in the dark."""
    a = np.array(sp).astype(np.float64)
    rgb = a[..., :3] * (1 - tint) + WATER * tint
    k = np.maximum(Lc, rim)
    out = rgb * np.minimum(k, 1.15)
    if keep_glow:
        g = glow_mask(sp)
        out[g] = a[g, :3]
    a[..., :3] = out
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def glow_only(sp):
    a = np.array(sp).astype(np.float64)
    g = glow_mask(sp)
    a[~g] = 0
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def add_rgb(img, layer, k=1.0):
    """Additive blend of an RGBA layer (alpha-weighted) onto an RGBA image."""
    a = np.array(img).astype(np.float64)
    b = np.array(layer).astype(np.float64)
    a[..., :3] += b[..., :3] * (b[..., 3:4] / 255.0) * k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def bloom(img, radius=3.0, k=0.9):
    return add_rgb(blank(img.width, img.height), img.filter(ImageFilter.GaussianBlur(radius)), k)


def caustics(t, scale=1.0):
    c1 = np.sin(XX * 0.085 * scale + t * 1.6 + 2.2 * np.sin(YY * 0.045 + t * 0.9))
    c2 = np.sin(YY * 0.075 * scale - t * 1.3 + 2.0 * np.sin(XX * 0.05 - t * 0.7))
    return np.clip(c1 * c2, 0, 1) ** 3


# headlights (the ship flies up the screen)

def cone_soft(px, py):
    """The default headlight (as enemies_r48): a halo and a beam widening ahead."""
    dx, dy = XX - px, YY - py
    halo = np.exp(-(dx * dx + dy * dy) / (2 * 60 ** 2))
    ahead = np.maximum(0, -dy)
    cone = np.exp(-(dx / (26 + 0.5 * ahead)) ** 2) * (dy < 8) * np.exp(-ahead / 380)
    return np.maximum(halo * 0.8, cone), cone


def cone_hard(px, py, half=35.0, reach=260.0):
    """Darkness look A: the spec's cone, 70 degrees and 260 px, a crisp edge, a small halo."""
    dx, dy = XX - px, YY - py
    r = np.hypot(dx, dy)
    ang = np.degrees(np.abs(np.arctan2(dx, -dy)))
    inside = sdf.smoothstep(half + 3, half - 3, ang) * sdf.smoothstep(reach, reach - 40, r)
    inside *= sdf.smoothstep(-4, 14, -dy)
    halo = np.exp(-(r / 46.0) ** 2)
    beam = np.maximum(inside * (1.0 - 0.35 * r / reach), halo)
    return beam, inside * 0.6


def cone_pool(px, py):
    """Darkness look B: a soft lamp pool round the ship (about 150 px), a long narrow faint beam."""
    dx, dy = XX - px, YY - py
    r = np.hypot(dx, dy)
    pool = np.exp(-(r / 86.0) ** 2) * 1.0
    ahead = np.maximum(0, -dy)
    beam = np.exp(-(dx / (10 + 0.16 * ahead)) ** 2) * (dy < 0) * np.exp(-ahead / 600) * 0.6
    return np.maximum(pool, beam), beam * 2.2 + pool * 0.3


CONES = {"soft": cone_soft, "hard": cone_hard, "pool": cone_pool}


# =========================================================================== the still model

class Still:
    """One play-field still: albedo (h, w, 3, 0..255), the water mask, the ambient map and its
    colour, point lights, emissive glows, layers and units (production sprites)."""

    def __init__(self, albedo, water=True):
        self.albedo = albedo
        self.water = np.ones((H, W)) if water is True else (np.zeros((H, W)) if water is False
                                                            else water)
        self.amb = np.ones((H, W))
        self.ambcol = COOL
        self.lights = []           # (x, y, r, (r, g, b) 0..1, k)
        self.emit = blank()        # ground glows (lamps, irises, mouths): always bright
        self.ground_units = []     # (sprite, x, y) on the sea floor (lit like the floor)
        self.sub = None            # RGBA: things below the plane (the Ray's shadow)
        self.low = blank()         # low-air: vapour, spores, bubbles
        self.units = []            # (sprite, x, y) on the play plane
        self.pickups = []          # (sprite, x, y): always warm gold
        self.over = blank()        # enemy bullets, rings, markers: always bright
        self.high = blank()        # high-air
        self.ship = (240, 450)
        self.rook = None
        self.rook_lamp = False
        self.caustic = 0.0
        self.cone_k = 0.9
        self.snow = 0
        self.seed = 1
        self.lit_ground = None     # an already lit ground (r07's), multiplied by the light map


GRADE_A = dict(amb=None, lights=1.0, caustic=None, cone="soft", cone_k=None, rim=0.0)
GRADE_B = dict(amb=0.06, lights=0.6, caustic=0.0, cone="soft", cone_k=1.05, rim=0.0)
DARK_A = dict(amb=None, lights=1.0, caustic=0.0, cone="hard", cone_k=1.0, rim=0.15)
DARK_B = dict(amb=None, lights=1.0, caustic=0.0, cone="pool", cone_k=1.0, rim=0.15)


def light_map(st, look, t=0.0):
    amb = st.amb if look["amb"] is None else np.ones((H, W)) * look["amb"]
    L = amb[..., None] * np.array(st.ambcol)
    for x, y, r, col, k in st.lights:
        L = L + gauss2(x, y, r)[..., None] * np.array(col) * k * look["lights"]
    if st.rook is not None and st.rook_lamp:
        L = L + gauss2(st.rook[0], st.rook[1] - 6, 30)[..., None] * np.array(WARM) * 0.9
    beam = np.zeros((H, W))
    if st.ship is not None and st.water.max() > 0:
        cone, beam = CONES[look["cone"]](*st.ship)
        ck = st.cone_k if look["cone_k"] is None else look["cone_k"]
        L = L + (cone * ck * st.water)[..., None] * np.array((0.85, 0.95, 1.0))
        beam = beam * ck * st.water
    return L, amb, beam


def render(st, look=GRADE_A, t=0.0):
    """Returns (frame, layers, light map)."""
    L, amb, beam = light_map(st, look, t)
    Lq = np.round(L * 12) / 12                                   # posterized light steps
    caus = st.caustic if look["caustic"] is None else look["caustic"]
    if st.lit_ground is not None:
        base = np.array(st.lit_ground).astype(np.float64)[..., :3]
    else:
        base = st.albedo
    k = Lq
    if caus:
        k = k * (1 + caus * caustics(t)[..., None] * np.clip(amb, 0, 1)[..., None] * st.water[..., None])
    g = base * k
    wm = st.water[..., None]
    lum = np.clip(Lq.mean(-1), 0, 1)[..., None]
    g = g * (1 - 0.2 * wm) + WATER * 0.2 * wm * lum                 # haze between floor and plane
    g = g + (beam * 0.10)[..., None] * CONE_COL                       # the beam in the water
    ground = to_img(g)
    for sp, x, y in st.ground_units:
        w, h = sp.size
        x0, y0 = int(round(x - w / 2)), int(round(y - h / 2))
        sprite.paste_center(ground, lit_sprite(sp, crop_map(Lq, x0, y0, w, h), 0.0,
                                               0.12 * float(st.water.mean())), x, y)
    ground.alpha_composite(st.emit)
    ground = add_rgb(ground, bloom(st.emit, 4, 0.8))
    frame = ground.copy()
    if st.sub is not None:
        frame.alpha_composite(st.sub)
    # marine snow, lit by the light map
    low = st.low.copy()
    if st.snow:
        rng = np.random.default_rng(st.seed)
        d = ImageDraw.Draw(low)
        for _ in range(st.snow):
            x0, y0, v, b = rng.uniform(0, W), rng.uniform(0, H), rng.uniform(6, 22), rng.uniform(0.4, 1)
            y = (y0 + t * v) % H
            x = (x0 + 4 * np.sin(t * 0.7 + x0)) % W
            if st.water[int(y), int(x)] < 0.5:
                continue
            lv = min(1.0, float(Lq[int(y), int(x)].mean()))
            if lv < 0.1:
                continue
            c = int(50 + 170 * b * lv)
            d.point((x, y), fill=(c, min(255, c + 20), min(255, c + 26), 255))
    frame.alpha_composite(low)
    air = blank()
    glows = blank()
    rim = look["rim"]
    for sp, x, y in st.units:
        w, h = sp.size
        x0, y0 = int(round(x - w / 2)), int(round(y - h / 2))
        Lc = np.ones((h, w, 3)) if look.get("units_full") else crop_map(Lq, x0, y0, w, h)
        sprite.paste_center(air, lit_sprite(sp, Lc, rim, 0.16 * float(st.water.mean())), x, y)
        sprite.paste_center(glows, glow_only(sp), x, y)
    for sp, x, y in st.pickups:
        sprite.paste_center(air, sp, x, y)
        sprite.paste_center(glows, sp, x, y)
    if st.rook is not None:
        sprite.paste_center(air, spr("rook", 2), *st.rook)
    if st.ship is not None:
        sp = st.ship_sprite if hasattr(st, "ship_sprite") else spr("ship", 2)
        sprite.paste_center(air, sp, *st.ship)
    air.alpha_composite(st.over)
    sprite_glow = bloom(glows, 3.0, 0.9 if rim else 0.5)
    frame.alpha_composite(air)
    frame = add_rgb(frame, sprite_glow)
    frame = add_rgb(frame, bloom(st.over, 2.5, 0.5))
    frame.alpha_composite(st.high)
    lmap = to_img(np.clip(Lq / 1.2, 0, 1) * 255)
    layers = {"ground": ground, "light": lmap, "sub": st.sub if st.sub is not None else blank(),
              "low-air": low, "air": air, "high-air": st.high}
    return frame, layers, Lq


# =========================================================================== shared drawing

def bullets(img, pts, r=5, ring=None):
    for x, y in pts:
        e3.orb(img, x, y, r, ring=ring)


def bolts(img, x, y, n=5, step=70, phase=0.0, slow=True):
    for k in range(n):
        yy = y - 30 - ((k * step + phase) % (n * step))
        if yy > -10:
            e3.bolt(img, x - 9, yy)
            e3.bolt(img, x + 9, yy)


def ring(img, x, y, r, col, width=4, alpha=200):
    d = ImageDraw.Draw(img)
    for k in range(width):
        a = int(alpha * (1 - abs(k - width / 2) / (width / 2 + 1)))
        d.ellipse([x - r - k, y - r - k, x + r + k, y + r + k], outline=col + (a,))


def lamp(img, x, y, col, r=2.0, halo=7, k=0.9):
    d = ImageDraw.Draw(img)
    d.ellipse([x - r, y - r, x + r, y + r], fill=col + (255,))
    d.ellipse([x - r * 0.4, y - r * 0.4, x + r * 0.4, y + r * 0.4], fill=(255, 250, 230, 255))
    lay = blank(img.width, img.height)
    ImageDraw.Draw(lay).ellipse([x - halo, y - halo, x + halo, y + halo], fill=col + (int(110 * k),))
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(halo / 2.2)))


def beacon(img, x, y, on=True):
    """A human relay beacon seen from above: a grey mast head with a red-gold lamp."""
    d = ImageDraw.Draw(img)
    d.rectangle([x - 4, y - 4, x + 4, y + 4], fill=(58, 60, 66, 255), outline=(30, 30, 34, 255))
    d.line([x - 9, y, x + 9, y], fill=(70, 72, 78, 255), width=1)
    lamp(img, x, y, GOLD if on else BEACON, 2.2, 10 if on else 6, 1.0 if on else 0.6)


def angler(lure_open=1.0, seed=3):
    """A Glow Angler stand-in (its sheet is enemies_r48's): a dark rust body, a teal lure on a
    stalk, two crimson eye-spots; 40 px."""
    s = 4
    img = Image.new("RGBA", (40 * s, 48 * s), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.ellipse([8 * s, 18 * s, 32 * s, 44 * s], fill=(92, 46, 34, 255), outline=(40, 20, 16, 255),
              width=s)
    d.polygon([(8 * s, 30 * s), (1 * s, 22 * s), (6 * s, 36 * s)], fill=(70, 34, 26, 255))
    d.polygon([(32 * s, 30 * s), (39 * s, 22 * s), (34 * s, 36 * s)], fill=(70, 34, 26, 255))
    d.polygon([(14 * s, 42 * s), (20 * s, 47 * s), (26 * s, 42 * s)], fill=(70, 34, 26, 255))
    d.arc([12 * s, 20 * s, 28 * s, 30 * s], 200, 340, fill=(214, 200, 180, 255), width=s)
    for ex in (15, 25):
        d.ellipse([(ex - 1.6) * s, 23 * s, (ex + 1.6) * s, 26.2 * s], fill=(255, 48, 56, 255))
    d.line([(20 * s, 20 * s), (21 * s, 12 * s), (20 * s, 7 * s)], fill=(120, 70, 56, 255), width=s)
    d.ellipse([16.5 * s, 2.5 * s, 23.5 * s, 9.5 * s], fill=(0, 255, 154, 255))
    d.ellipse([18.5 * s, 4.5 * s, 21.5 * s, 7.5 * s], fill=(220, 255, 240, 255))
    return img.resize((40, 48), Image.LANCZOS)


@lru_cache(maxsize=None)
def nautilus():
    import enemies_r06 as e6
    return e6.nau_shell.get(0.0)


@lru_cache(maxsize=None)
def jelly():
    return spr("driftjelly", 0)


@lru_cache(maxsize=None)
def silt_albedo(seed, stops, relief=0.3, ripples=0.02, strength=26, shadow=0.008):
    """A shaded silt floor (cached: callers must not change the arrays in place)."""
    stops = list(stops)
    n = nfield(96, seed, 5)
    ridge = 1 - np.abs(2 * nfield(52, seed + 1, 2) - 1)
    hgt = 0.42 + relief * n + 0.18 * ridge ** 4
    hgt += ripples * np.sin((XX * 0.35 + YY + nfield(64, seed + 2, 2) * 60) / 5.0)
    return m36.shade_rgb(hgt, stops, strength=strength, shadow=shadow), hgt


def kelp_tufts(img, rng, n, box, col=(30, 84, 62)):
    d = ImageDraw.Draw(img)
    x0, y0, x1, y1 = box
    for _ in range(n):
        cx, cy = rng.uniform(x0, x1), rng.uniform(y0, y1)
        for _ in range(int(rng.integers(4, 8))):
            a = rng.uniform(0, TAU)
            ln = rng.uniform(6, 16)
            d.line([cx, cy, cx + np.cos(a) * ln, cy + np.sin(a) * ln],
                   fill=tuple(int(c * rng.uniform(0.7, 1.2)) for c in col) + (255,), width=2)


# =========================================================================== L22 ice shelf

ICE = [(30, 48, 74), (66, 92, 124), (110, 140, 172), (160, 184, 208), (200, 216, 232),
       (226, 236, 246)]
JUP = [(0.0, (70, 40, 26)), (0.25, (150, 96, 60)), (0.45, (214, 176, 132)), (0.6, (236, 214, 180)),
       (0.75, (178, 118, 74)), (1.0, (120, 70, 44))]


def jupiter_tex(cx=360, cy=140, r=300):
    n = nfield(30, 487, 3)
    v = 0.5 + 0.5 * np.sin((YY - cy) / 15.0 + 2.2 * n + 0.6 * np.sin(XX / 40.0))
    col = raster.ramp(JUP, v)
    disc = np.hypot(XX - cx, YY - cy) < r
    spot = np.exp(-(((XX - cx - 70) / 34) ** 2 + ((YY - cy - 60) / 18) ** 2))
    col = col * (1 - 0.4 * spot[..., None]) + np.array((200, 80, 50)) * 0.4 * spot[..., None]
    return np.where(disc[..., None], col, np.array((6, 10, 20.0)))


def ice_albedo(seed=481, night=False):
    n = nfield(110, seed, 5)
    r1 = 1 - np.abs(2 * nfield(70, seed + 1, 3) - 1)
    big = nfield(220, seed + 3, 3)
    hgt = 0.5 + 0.3 * (big - 0.5) + 0.08 * (n - 0.5) + 0.16 * r1 ** 12
    f = nfield(38, seed + 2, 3)
    crack = np.exp(-((f - 0.5) / 0.006) ** 2) * (n > 0.45)
    hgt -= 0.03 * crack
    col = m36.shade_rgb(hgt, ICE, strength=34, shadow=0.004)
    col = col * (1 - 0.3 * crack)[..., None] + np.array((60, 120, 170)) * 0.3 * crack[..., None]
    xl = 330 + 50 * np.sin(YY / 110) + 20 * np.sin(YY / 37) - (YY - 270) * 0.25
    n2 = nfield(16, seed + 6, 3)
    wl = (10 + 9 * (0.5 + 0.5 * np.sin(YY / 60 + 1))) * np.clip((330 - YY) / 80, 0, 1) + 10 * (n2 - 0.5)
    dist = np.abs(XX - xl)
    lead = dist < wl
    rim = (dist < wl + 2) & ~lead
    jup = jupiter_tex()
    glint = np.exp(-((XX - xl + wl * 0.5) / 2.5) ** 2) * (0.5 + 0.5 * nfield(20, seed + 5, 2))
    water = jup * 0.16 + np.array((3, 9, 18)) + glint[..., None] * 40
    skin = (nfield(30, seed + 7, 3) > 0.58)
    water = np.where(skin[..., None], np.array((52, 70, 92.0)) + water * 0.3, water)
    col = np.where(lead[..., None], water, col)
    col = np.where(rim[..., None], np.array((196, 214, 230.0)), col)
    shine = (XX / W)[..., None] * 0.10                               # Jupiter-shine from the right
    col = col * (1 - shine) + np.array((255, 196, 150)) * shine
    if night:
        col = col * np.array((0.34, 0.36, 0.48)) + np.array((40, 22, 10)) * (XX / W)[..., None]
    return col, lead


def bore_site(st, x, y, vapour=True):
    """A Vrell bore site stand-in (the rig itself is drill-rig-r48's): the bore, a bone collar,
    teal growth and steaming radial cracks."""
    d = ImageDraw.Draw(st.emit)
    rng = np.random.default_rng(int(x * 7 + y))
    for _ in range(140):
        a, rr = rng.uniform(0, TAU), rng.uniform(34, 62)
        px, py = x + np.cos(a) * rr, y + np.sin(a) * rr * 0.9
        c = TEAL if rng.uniform() < 0.6 else (40, 140, 120)
        d.ellipse([px - 1.5, py - 1.5, px + 1.5, py + 1.5], fill=c + (int(rng.uniform(110, 230)),))
    for k in range(6):
        a = k * TAU / 6 + 0.3
        d.line([x + np.cos(a) * 34, y + np.sin(a) * 34, x + np.cos(a) * 90, y + np.sin(a) * 84],
               fill=(150, 230, 255, 120), width=2)
    d.ellipse([x - 36, y - 36, x + 36, y + 36], fill=(196, 184, 164, 255), outline=(90, 80, 70, 255),
              width=2)
    for k in range(8):
        a = k * TAU / 8
        d.ellipse([x + np.cos(a) * 30 - 4, y + np.sin(a) * 30 - 4, x + np.cos(a) * 30 + 4,
                   y + np.sin(a) * 30 + 4], fill=(120, 108, 96, 255))
    d.ellipse([x - 22, y - 22, x + 22, y + 22], fill=(10, 16, 26, 255))
    d.ellipse([x - 12, y - 12, x + 12, y + 12], fill=(0, 120, 90, 255))
    d.ellipse([x - 5, y - 5, x + 5, y + 5], fill=LIME + (255,))
    if vapour:
        st.low.alpha_composite(steam(int(x + y), x + 14, y - 4, 100, -0.5, 28))


def geyser(st, x, y, burst=True):
    d = ImageDraw.Draw(st.emit)
    for k in range(-3, 4):
        d.line([x + k * 9 - 4, y + k * 5, x + k * 9 + 5, y + k * 5 + 4], fill=(170, 240, 255, 230),
               width=2)
    if burst:
        st.low.alpha_composite(steam(int(x * 3 + y), x, y, 110, -1.2, 36))
        hd = ImageDraw.Draw(st.high)
        rng = np.random.default_rng(int(x))
        for _ in range(40):
            a, rr = rng.uniform(0, TAU), rng.uniform(10, 60)
            px, py = x + np.cos(a) * rr, y - 20 + np.sin(a) * rr * 0.8
            hd.point((px, py), fill=(255, 255, 255, int(rng.uniform(120, 255))))


def ice_shelf():
    col, lead = ice_albedo()
    st = Still(col, water=False)
    st.amb = np.ones((H, W))
    st.ambcol = (1.0, 1.0, 1.0)
    st.ship, st.rook = (236, 452), (290, 490)
    bore_site(st, 128, 150)
    for a in (0.4, 1.9, 3.3, 4.7):
        st.ground_units.append((spr("spine-turret", 4), 128 + np.cos(a) * 82, 150 + np.sin(a) * 74))
    geyser(st, 300, 300)
    geyser(st, 120, 380, burst=False)
    for i, (x, y) in enumerate(((180, 250), (210, 228), (150, 228), (240, 206), (120, 206))):
        st.units.append((spr("skitter", 0), x, y))
    st.units.append((spr("needler", 0), 400, 120))
    bullets(st.over, [(392, 160), (380, 196), (368, 232)])
    bolts(st.over, 236, 452)
    hd = ImageDraw.Draw(st.high)
    rng = np.random.default_rng(5)
    for _ in range(90):
        x, y = rng.uniform(0, W), rng.uniform(0, H)
        hd.point((x, y), fill=(255, 255, 255, int(rng.uniform(40, 140))))
    st.high.alpha_composite(m36.s6.wisp_tex(540, [90, 45], 489, (236, 244, 252), thresh=0.72,
                                            max_a=60, stretch=4))
    frame, layers, _ = render(st)
    layers = {"ground": layers["ground"], "low-air": layers["low-air"], "air": layers["air"],
              "high-air": layers["high-air"]}
    notes = {"ground": "X1.0 FISSURED ICE, RIDGES, AN OPEN LEAD MIRRORING JUPITER",
             "low-air": "X1.3 STEAM FROM THE BORE AND THE GEYSER",
             "air": "PLAY PLANE; SKITTERS, A NEEDLER FOR SCALE",
             "high-air": "X2 ICE CRYSTALS, VAPOUR WISPS"}
    text = ["L22 ICE SHELF, THE RIG FIELD: A WHITE-BLUE PLAIN OF PRESSURE RIDGES AND FISSURES IN HARD "
            "SUNLIGHT FROM THE TOP LEFT; JUPITER'S ORANGE-BROWN LIGHT WARMS THE RIGHT SIDE AND ITS "
            "BANDED DISC SHOWS IN THE BLACK WATER OF AN OPEN LEAD (THE DEEP LAYER, SCENERY).",
            "A VRELL BORE SITE (STAND-IN: THE RIG ITSELF IS ON THE DRILL-RIG SHEETS): BONE COLLAR, "
            "TEAL GROWTH, STEAMING RADIAL CRACKS, A RING OF SPINE TURRETS. AN ICE GEYSER BURSTS "
            "FROM A GLOWING CRACK (RIGHT), A SECOND CRACK GLOWS BEFORE ITS BURST (LEFT).",
            "MOTION BUDGET: THE STEAM AND THE GEYSERS MOVE, THE ICE STAYS STILL."]
    return frame, layers, notes, text


# =========================================================================== L23 the descent

def wall_l(y, w=80):
    return w + 10 * np.sin(y / 60.0) + 5 * np.sin(y / 17.0)


def wall_r(y, w=80):
    return W - (w + 10 * np.sin(y / 70.0 + 2) + 5 * np.sin(y / 21.0 + 1))


def shaft_albedo(seed=231, glow_far=0.12):
    """The drilled shaft seen from above: ice walls at the field's edges, the shaft receding to
    a dark centre with spiral drill grooves in perspective."""
    xl, xr = wall_l(YY), wall_r(YY)
    n = nfield(40, seed, 3)
    rr = np.hypot((XX - 240) / 170, (YY - 250) / 300)
    ang = np.arctan2(YY - 250, XX - 240)
    grooves = 0.5 + 0.5 * np.sin(ang * 14 + np.log(rr + 0.05) * 18)
    depth = np.clip(rr, 0, 1) ** 1.6
    face = np.array((70, 104, 134.0)) * (0.25 + 0.75 * depth)[..., None] * (0.8 + 0.25 * grooves)[..., None]
    face += np.array((255, 170, 90)) * glow_far * np.exp(-(rr / 0.18) ** 2)[..., None]
    ice = raster.ramp([(0, (90, 124, 156)), (0.5, (160, 190, 214)), (1, (220, 232, 244))],
                      0.35 + 0.5 * n + 0.15 * np.sin((YY + XX * 0.6) / 8.0))
    edge_l = np.clip((xl - XX) / 14, 0, 1)
    edge_r = np.clip((XX - xr) / 14, 0, 1)
    col = face
    col = np.where((XX < xl)[..., None], ice * (0.55 + 0.45 * edge_l)[..., None], col)
    col = np.where((XX > xr)[..., None], ice * (0.45 + 0.4 * edge_r)[..., None], col)
    lip = ((XX < xl) & (xl - XX < 3)) | ((XX > xr) & (XX - xr < 3))
    col = np.where(lip[..., None], np.array((236, 244, 252.0)), col)
    return col


def collar_strips(img, ys, col=TEAL):
    d = ImageDraw.Draw(img)
    for y in ys:
        for side in (0, 1):
            x = wall_l(y) if side == 0 else wall_r(y)
            sx = -1 if side == 0 else 1
            d.line([x + sx * 4, y, x + sx * 40, y - 22], fill=col + (200,), width=2)


def descent():
    yw = 236
    st = Still(shaft_albedo(), water=(YY < yw).astype(np.float64))
    above = YY < yw
    st.amb = np.where(above, 0.42, 0.5 + 0.35 * (YY - yw) / (H - yw))
    st.ambcol = (0.75, 0.88, 1.0)
    st.lights = [(240, 520, 160, (0.9, 0.9, 1.0), 0.5)]
    st.caustic = 0.45
    st.cone_k = 0.8
    st.snow = 60
    st.ship, st.rook = (236, 300), (292, 344)
    collar_strips(st.emit, (60, 150, 330, 470))
    d = ImageDraw.Draw(st.emit)
    rng = np.random.default_rng(9)
    for x in np.arange(wall_l(yw), wall_r(yw), 3):                    # the waterline
        y = yw + 2 * np.sin(x / 9.0)
        d.line([x, y - 1, x + 2, y + 1], fill=(220, 246, 255, int(rng.uniform(140, 230))), width=2)
    low = ImageDraw.Draw(st.low)
    for _ in range(40):                                               # bubbles at the waterline
        x, y, r = rng.uniform(wall_l(yw), wall_r(yw)), yw - rng.uniform(2, 50), rng.uniform(1, 3)
        low.ellipse([x - r, y - r, x + r, y + r], outline=(200, 240, 255, 180))
    st.ground_units += [(spr("spine-turret", 6), wall_l(420) - 14, 420),
                        (spr("reef-spitter", 24), wall_r(110) + 14, 110)]
    st.units += [(jelly(), 190, 92), (spr("lamprey", 0), 330, 168), (spr("skitter", 0), 160, 420),
                 (spr("skitter", 0), 330, 400)]
    bullets(st.over, [(116, 418), (140, 400)], ring=(255, 96, 40))
    bolts(st.over, 236, 300)
    frame, layers, _ = render(st)
    layers = {"ground": layers["ground"], "light": layers["light"], "low-air": layers["low-air"],
              "air": layers["air"]}
    notes = {"ground": "X1.0 ICE WALLS + THE SHAFT RECEDING, DRILL GROOVES",
             "light": "LIGHT MAP: DAYLIGHT BEHIND, WATER AHEAD, HEADLIGHT",
             "low-air": "BUBBLES AND FOAM AT THE WATERLINE",
             "air": "PLAY PLANE; STOCK SHIP (KIT: HYDRO-KIT SHEETS)"}
    text = ["L23 THROUGH THE ICE, T=25: THE SHIP CROSSES THE WATERLINE. BELOW IT (BEHIND THE SHIP) THE "
            "DRY SHAFT: ICE WALLS LIT FROM THE MOUTH BEHIND, SURFACE UNITS. ABOVE IT (AHEAD) THE WATER: "
            "THE GRADE TURNS TEAL AND DARKER, CAUSTICS RIPPLE ON THE WALLS, MARINE SNOW, THE HEADLIGHT "
            "BEAM SHOWS IN THE WATER; THE FIRST DRIFTJELLY AND LAMPREY WAIT THERE.",
            "THE SHAFT IS DRAWN RECEDING TO A DARK CENTRE WITH THE SPIRAL GROOVES OF THE DRILL AND A "
            "FAINT WARM GLOW FAR BELOW (THERA DEEP); THE VRELL COLLARS' TEAL STRIPS RUN UP THE WALLS. "
            "THE WALL FOOT IS THE COLLISION LINE (CANYON RULE B)."]
    return frame, layers, notes, text


# =========================================================================== L23 Thera Deep

FARM_LAMPS = [(110, 250, 70), (240, 466, 66), (276, 76, 46), (372, 420, 50), (360, 316, 40),
              (306, 474, 40), (60, 60, 40)]


@lru_cache(maxsize=None)
def r07_layers(f=24):
    import scenes_r07 as s7
    sc = s7.EuropaR07("a")
    return sc.layer_images(f)


def thera_frame(look=GRADE_A):
    L7 = r07_layers()
    ground = L7["ground"].copy()
    for k in ("shadows", "kelp"):
        ground.alpha_composite(L7[k])
    st = Still(np.zeros((H, W, 3)))
    st.lit_ground = ground
    st.amb = np.full((H, W), 0.58)
    st.ambcol = (0.8, 0.95, 1.0)
    st.lights = [(x, y, r, WARM, 0.75) for x, y, r in FARM_LAMPS]
    st.cone_k = 0.35
    st.caustic = 0.0
    st.ship = None                         # r07's own layers carry the ship, its shots, the nautili
    for x, y in ((150, 222), (210, 492)):                         # Vrell siphon pumps on the racks
        d = ImageDraw.Draw(st.emit)
        d.ellipse([x - 9, y - 9, x + 9, y + 9], fill=(70, 34, 80, 255), outline=(30, 14, 40, 255))
        d.ellipse([x - 4, y - 4, x + 4, y + 4], fill=TEAL + (255,))
        for k in range(5):
            a = k * TAU / 5
            d.line([x, y, x + np.cos(a) * 22, y + np.sin(a) * 18], fill=(40, 180, 140, 200), width=2)
    for x, y, r in FARM_LAMPS[:3]:                                  # grow-lamp arrays
        for i in range(-2, 3):
            lamp(st.emit, x + i * 14, y - r * 0.55, (255, 220, 160), 1.6, 5, 0.7)
    frame, layers, _ = render(st, look)
    dim = 1.0 if look["amb"] is None else 0.35
    for k in ("sub", "low-air", "air", "high-air"):
        lay = L7[k]
        if dim < 1 and k != "air":
            a = np.array(lay).astype(np.float64)
            a[..., :3] *= dim
            lay = Image.fromarray(a.astype(np.uint8), "RGBA")
        frame.alpha_composite(lay)
    return frame, L7, layers


def thera_deep():
    frame, L7, layers = thera_frame()
    layers = {"ground": layers["ground"], "light": layers["light"], "sub": L7["sub"],
              "low-air": L7["low-air"], "air": L7["air"], "high-air": L7["high-air"]}
    notes = {"ground": "THE CHOSEN R07 FLOOR AND KELP, LIT BY THE LAMPS ONLY",
             "light": "LIGHT MAP: 58 % AMBIENT + WARM GROW-LAMP POOLS",
             "sub": "R07: SILT, FISH SCHOOLS, NAUTILI", "low-air": "R07: RISING BUBBLES",
             "air": "R07: PLAY PLANE", "high-air": "R07: SHAFTS (HERE: LAMP BEAMS)"}
    text = ["L23 THERA DEEP, THE FARMS (Q2 A, LAMP-LIT): THE CHOSEN ROUND-07 KELP FOREST AND FARM GRIDS "
            "ARE KEPT, BUT THE LIGHT IS NOW THE CITY'S: WARM GROW-LAMP ARRAYS OVER THE RACKS AND THE "
            "DOMES' LIGHTS MAKE POOLS, THE WATER BETWEEN THEM FALLS TO ABOUT 60 %. THE CAUSTICS ARE THE "
            "LAMP ARRAYS' (NO SUN UNDER 20 KM OF ICE).",
            "TWO VRELL SIPHON PUMPS (THE LEVEL'S SECONDARY) SIT ON THE RACKS: PURPLE CHITIN, A TEAL "
            "MOUTH, TEAL ROOTS SPREADING INTO THE KELP. UNITS ARE R07'S (NAUTILI, JELLIES) FOR SCALE."]
    return frame, layers, notes, text


# =========================================================================== L24 abyssal plain

PLAIN = ((8, 20, 26), (20, 40, 46), (38, 62, 66), (64, 88, 88), (98, 118, 112), (132, 146, 134))


def reef(st, x, y, r=26, seed=0):
    """A spawning reef from above: lumpy coral tower, plum and bone, a teal iris at the crown."""
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(st.emit)
    lay = blank()
    dl = ImageDraw.Draw(lay)
    for _ in range(26):
        a, rr = rng.uniform(0, TAU), rng.uniform(0, r)
        px, py, s = x + np.cos(a) * rr, y + np.sin(a) * rr, rng.uniform(6, 13)
        c = (96, 52, 104) if rng.uniform() < 0.6 else (176, 160, 140)
        sh = 0.6 + 0.4 * (1 - rr / r)
        dl.ellipse([px - s, py - s, px + s, py + s], fill=tuple(int(v * sh) for v in c) + (255,),
                   outline=(30, 16, 34, 255))
    st.reefs.append(lay)
    d.ellipse([x - 9, y - 9, x + 9, y + 9], fill=(30, 10, 40, 255))
    for k in range(6):
        a = k * TAU / 6
        d.pieslice([x - 8, y - 8, x + 8, y + 8], np.degrees(a) + 10, np.degrees(a) + 40,
                   fill=TEAL + (230,))
    d.ellipse([x - 3, y - 3, x + 3, y + 3], fill=(220, 255, 240, 255))
    st.lights.append((x, y, 34, (0.2, 1.0, 0.7), 0.35))


def ray_shadow(x, y, s=1.0, alpha=0.55):
    """An Abyss Ray far below the plane: a dark manta shape, blurred, two faint lime gill rows."""
    lay = blank()
    d = ImageDraw.Draw(lay)
    pts = [(x, y - 70 * s), (x + 40 * s, y - 30 * s), (x + 110 * s, y + 6 * s), (x + 50 * s, y + 26 * s),
           (x + 10 * s, y + 60 * s), (x, y + 120 * s), (x - 10 * s, y + 60 * s), (x - 50 * s, y + 26 * s),
           (x - 110 * s, y + 6 * s), (x - 40 * s, y - 30 * s)]
    d.polygon(pts, fill=(2, 10, 16, int(255 * alpha)))
    lay = lay.filter(ImageFilter.GaussianBlur(5))
    d = ImageDraw.Draw(lay)
    for k in range(4):
        for sx in (-1, 1):
            gx, gy = x + sx * (14 + k * 5), y + 8 + k * 6
            d.ellipse([gx - 1.5, gy - 1.5, gx + 1.5, gy + 1.5], fill=LIME + (110,))
    return lay


def plain_still(t=0.0, still=True):
    col, _ = silt_albedo(241, PLAIN, relief=0.32)
    st = Still(col)
    st.reefs = []
    st.amb = 0.14 + 0.62 * np.clip((YY - 120) / (H - 120), 0, 1) ** 1.4
    st.ambcol = (0.75, 0.9, 1.0)
    st.lights = [(140, 560, 120, WARM, 0.55), (380, 600, 140, WARM, 0.45)]   # the last lamps behind
    st.caustic = 0.12
    st.snow = 80
    rng = np.random.default_rng(31)
    kelp = blank()
    kelp_tufts(kelp, rng, 14, (20, 300, 460, 530))
    kelp_tufts(kelp, rng, 5, (20, 40, 460, 260), col=(24, 60, 48))
    reef(st, 112, 330, 30, 1)
    reef(st, 372, 214, 26, 2)
    for lay in st.reefs:
        kelp.alpha_composite(lay)
    base = Image.fromarray(np.clip(col, 0, 255).astype(np.uint8)).convert("RGBA")
    base.alpha_composite(kelp)
    st.albedo = np.array(base).astype(np.float64)[..., :3]
    pulse = (t * 0.5) % 1.0 < 0.15
    beacon(st.emit, 64, 74, pulse)
    st.lights.append((64, 74, 26 if pulse else 14, (1.0, 0.6, 0.25), 0.8 if pulse else 0.4))
    st.sub = ray_shadow(300 - 18 * t, 230 + 10 * t, 1.0, 0.8)
    st.ground_units += [(spr("scuttler", 8), 262, 400), (spr("scuttler", 8), 196, 470),
                        (spr("reef-spitter", 0), 150, 362)]
    sway = 30 * np.sin(t * TAU / SECONDS)
    st.ship, st.rook = (240 + sway, 452), (292 + sway, 492)
    for i in range(3):
        st.units.append((spr("lamprey", 0), 120 + 110 * i + 20 * np.sin(t * 1.6 + i), 150 + 28 * i))
    st.units.append((jelly(), 390, 360 - 6 * np.sin(t * 2)))
    bullets(st.over, [(360 - 14 * t, 380 + 20 * t), (330 - 14 * t, 404 + 20 * t)])
    bolts(st.over, st.ship[0], st.ship[1], phase=t * 210)
    return st


def plain():
    st = plain_still()
    frame, layers, _ = render(st)
    layers = {"ground": layers["ground"], "light": layers["light"], "sub": layers["sub"],
              "air": layers["air"]}
    notes = {"ground": "X1.0 SILT PLAIN, KELP, SPAWNING REEFS, A BEACON",
             "light": "LIGHT MAP: THE LAMPS BEHIND, DARK AHEAD",
             "sub": "X1.2 AN ABYSS RAY'S SHADOW BELOW THE PLANE",
             "air": "PLAY PLANE; LAMPREYS, A JELLY FOR SCALE"}
    text = ["L24 ABYSSAL PLAIN, SECTIONS 2-3: THERA DEEP'S LAST LAMPS ARE BEHIND THE SHIP (THE BOTTOM "
            "EDGE STILL WARM), THE WATER AHEAD FALLS TO ABOUT 15 %: THE HEADLIGHT, THE REEFS' TEAL "
            "IRISES AND THE BEACON'S RED-GOLD PULSE (EVERY 2 S) ARE THE LIGHT THERE.",
            "TWO SPAWNING REEFS (PLUM AND BONE CORAL, A TEAL IRIS AT THE CROWN), SCUTTLERS ON THE "
            "FLOOR, THE FIRST HUMAN RELAY BEACON (GREY MAST HEAD, NO VRELL GROWTH ROUND IT). AN ABYSS "
            "RAY'S SHADOW SLIDES UNDER THE PLANE BEFORE IT RISES (ITS SHEET: ABYSS-RAY-R48)."]
    return frame, layers, notes, text


# =========================================================================== L25 the trench

def trench_l(y):
    return 92 + 14 * np.sin(y / 70.0 + 1.0) + 6 * np.sin(y / 23.0)


def trench_r(y):
    x = W - (100 + 14 * np.sin(y / 55.0) + 6 * np.sin(y / 29.0 + 2.0))
    notch = (np.abs(y - 200) < 45) * 70 * np.clip((45 - np.abs(y - 200)) / 10, 0, 1)
    return x + notch


def trench_albedo(seed=251):
    col, _ = silt_albedo(seed, PLAIN, relief=0.2)
    col = col * 0.4
    xl, xr = trench_l(YY), trench_r(YY)
    n = nfield(30, seed + 3, 3)
    strata = 0.6 + 0.16 * np.sin((XX + YY * 0.4) / 5.0) + 0.08 * np.sin(YY / 2.6) + 0.2 * n
    rock = np.array((96, 124, 140.0)) * strata[..., None]
    growth = (nfield(24, seed + 4, 3) > 0.6)
    rock = np.where(growth[..., None], np.array((120, 70, 128.0)) * strata[..., None], rock)
    left, right = XX < xl, XX > xr
    col = np.where((left | right)[..., None], rock, col)
    lit_edge = ((XX > xr) & (XX - xr < 4)) | ((XX < xl) & (xl - XX < 3))
    col = np.where(lit_edge[..., None], col * 1.8, col)
    sh = np.clip((XX - xl) / 22.0, 0, 1) * 0.45 + 0.55
    col = np.where((~left & (XX - xl < 22))[..., None], col * sh[..., None], col)
    return col


def sac(img, x, y, side, tell=0.0):
    d = ImageDraw.Draw(img)
    d.ellipse([x - 11, y - 9, x + 11, y + 9], fill=(60, 66, 74, 255), outline=(24, 26, 30, 255))
    c = tuple(int(v * (0.6 + 0.4 * tell)) for v in TEAL)
    d.ellipse([x - 6 + side * 3, y - 5, x + 6 + side * 3, y + 5], fill=c + (255,))
    d.ellipse([x - 2 + side * 3, y - 2, x + 2 + side * 3, y + 2], fill=(220, 255, 240, 255))


def trench_still():
    st = Still(trench_albedo())
    st.amb = np.full((H, W), 0.2)
    st.ambcol = (0.55, 0.75, 1.0)
    st.snow = 70
    st.cone_k = 1.0
    for y in (90, 300, 480):                                          # beacons on the left wall
        x = trench_l(y) - 10
        on = y == 300
        beacon(st.emit, x, y, on)
        st.lights.append((x, y, 30 if on else 16, (1.0, 0.55, 0.25), 0.9 if on else 0.45))
    for y, side in ((150, -1), (400, -1)):
        x = trench_l(y) + 6
        sac(st.emit, x, y, 1, 1.0 if y == 150 else 0.4)
        st.lights.append((x, y, 26, (0.2, 1.0, 0.7), 0.5))
    for y in (330, 470):
        x = trench_r(y) - 6
        sac(st.emit, x, y, -1, 0.5)
        st.lights.append((x, y, 22, (0.2, 1.0, 0.7), 0.4))
    cx, cy = trench_r(200) - 30, 200                                  # the cave notch's own sac
    sac(st.emit, cx + 50, cy, -1, 0.8)
    ring(st.over, trench_l(150) + 6, 150, 96, TEAL, 6, 170)
    st.ship, st.rook = (232, 450), (284, 488)
    try:
        st.units += [(nautilus(), 300, 240), (nautilus(), 196, 210)]
    except Exception:  # noqa: BLE001  (the r06 model is optional here)
        pass
    st.units += [(jelly(), 330, 360)]
    for k in range(10):
        a = k * 0.5
        bullets(st.over, [(300 + np.cos(a) * (30 + 9 * k), 240 + np.sin(a) * (30 + 9 * k))], r=4,
                ring=(255, 48, 140))
    bolts(st.over, 232, 450)
    return st


def trench():
    frame, layers, _ = render(trench_still())
    layers = {"ground": layers["ground"], "light": layers["light"], "air": layers["air"]}
    notes = {"ground": "X1.0 ICE-ROCK WALLS, VRELL GROWTH, A CAVE NOTCH, NESTS",
             "light": "LIGHT MAP: 14 % BLUE, HEADLIGHT, SACS, BEACONS",
             "air": "PLAY PLANE; R06 NAUTILI, A JELLY"}
    text = ["L25 SIREN TRENCH, SECTION 3: DARK BUT NOT BLACK (ABOUT 14 % BLUE AMBIENT): THE HEADLIGHT "
            "CONE, THE SIREN NESTS' TEAL SACS AND THE BEACONS' RED-GOLD PULSE (ABOUT EVERY 4 S) LIGHT "
            "THE WALLS. A SAC THAT HAS JUST SUNG (TOP LEFT) SENDS ITS TEAL RING ACROSS THE TRENCH.",
            "THE WALLS ARE ICE-ROCK STRATA WITH PURPLE VRELL GROWTH; THE FOOT IS THE COLLISION LINE. A "
            "CAVE NOTCH (RIGHT, 70 X 90 PX) HOLDS A NEST AT ITS BACK; WAVES SWIM OUT OF IT. THE NAUTILUS "
            "IS ROUND 06'S CHOSEN MODEL, FOR SCALE."]
    return frame, layers, notes, text


# =========================================================================== L27 vents

BASALT = ((10, 10, 14), (24, 22, 26), (40, 36, 38), (62, 54, 52), (90, 78, 70), (120, 104, 90))
VENTS = [(120, 140, 16), (330, 96, 13), (380, 300, 18), (90, 420, 12), (250, 250, 10)]


def chimney(img, x, y, r):
    d = ImageDraw.Draw(img)
    for k in range(6, 0, -1):
        rr = r * (0.4 + 0.6 * k / 6) + 6
        c = int(16 + 9 * (6 - k))
        d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=(c, c - 3, c - 5, 255))
    d.ellipse([x - r * 0.35, y - r * 0.35, x + r * 0.35, y + r * 0.35], fill=(255, 150, 60, 255))
    d.ellipse([x - r * 0.16, y - r * 0.16, x + r * 0.16, y + r * 0.16], fill=(255, 236, 170, 255))


def vents_still(t=0.0):
    col, _ = silt_albedo(271, BASALT, relief=0.3, ripples=0.01)
    st = Still(col)
    st.amb = np.full((H, W), 0.1)
    st.ambcol = (0.5, 0.7, 1.0)
    st.snow = 60
    mats = nfield(22, 273, 3)
    em = np.zeros((H, W))
    for x, y, r in VENTS:
        em = np.maximum(em, np.exp(-(np.hypot(XX - x, YY - y) / (r * 3.0)) ** 2) * (mats > 0.52))
        st.lights.append((x, y, r * 5, (1.0, 0.55, 0.2), 0.85))
    mat_rgb = np.ones((H, W, 3)) * np.array((255, 140, 40.0))
    st.emit.alpha_composite(to_img(mat_rgb, np.round(em * 4) / 4 * 90))
    st.lights.append((72, 340, 44, (1.0, 0.6, 0.3), 0.6))
    rng = np.random.default_rng(4)
    d = ImageDraw.Draw(st.emit)
    for x, y, r in VENTS:
        chimney(st.emit, x, y, r)
        for _ in range(16):                                           # tube worms
            a, rr = rng.uniform(0, TAU), rng.uniform(r + 8, r + 22)
            px, py = x + np.cos(a) * rr, y + np.sin(a) * rr
            d.ellipse([px - 1.5, py - 1.5, px + 1.5, py + 1.5], fill=(236, 230, 220, 255))
            d.point((px, py), fill=(220, 40, 40, 255))
    # the Hunter wreck (hint 3): a grey hull, scorched, nose-down in the silt
    wl = blank()
    dw = ImageDraw.Draw(wl)
    dw.rounded_rectangle([60, 300, 84, 380], 11, fill=(112, 116, 120, 255), outline=(40, 42, 46, 255))
    dw.polygon([(60, 370), (50, 392), (72, 384), (94, 392), (84, 370)], fill=(90, 94, 98, 255))
    dw.ellipse([62, 318, 82, 346], fill=(40, 30, 26, 220))
    dw.line([66, 352, 78, 352], fill=(220, 200, 120, 255), width=2)
    st.wreck = wl
    base = Image.fromarray(np.clip(col, 0, 255).astype(np.uint8)).convert("RGBA")
    base.alpha_composite(wl.rotate(14, center=(72, 340)))
    st.albedo = np.array(base).astype(np.float64)[..., :3]
    # a heat plume erupting: an amber disc, shimmer rings, rising silt
    px, py, pr = 380, 300, 54
    ph = (t / SECONDS) % 1.0
    pl = blank()
    dp = ImageDraw.Draw(pl)
    dp.ellipse([px - pr, py - pr, px + pr, py + pr], fill=(255, 150, 60, 70))
    pl = pl.filter(ImageFilter.GaussianBlur(10))
    for k in range(3):
        ring(pl, px, py, pr * (0.4 + 0.25 * k + 0.2 * ph), (255, 200, 120), 2, 120 - 30 * k)
    st.low.alpha_composite(pl)
    st.low.alpha_composite(soft_blobs(275, [(px, py, 40), (px - 10, py - 26, 30)], (120, 100, 90), 130))
    st.ship, st.rook = (210, 462), None
    st.units += [(spr("lamprey", 0), 200, 180), (spr("lamprey", 0), 260, 150), (jelly(), 120, 260)]
    bullets(st.over, [(200, 214), (200, 248)], ring=(255, 96, 40))
    bolts(st.over, 210, 462)
    return st


def vents():
    st = vents_still()
    frame, layers, _ = render(st)
    layers = {"ground": layers["ground"], "light": layers["light"], "low-air": layers["low-air"],
              "air": layers["air"]}
    notes = {"ground": "X1.0 BASALT SILT, BLACK SMOKERS, AMBER MATS, THE WRECK",
             "light": "LIGHT MAP: 10 % BLUE + THE VENTS' AMBER",
             "low-air": "A HEAT PLUME ERUPTING, RISING SILT",
             "air": "PLAY PLANE; LAMPREYS, A JELLY"}
    text = ["L27 HYDROTHERMAL VENTS, THE CHIMNEY GARDEN: LIT BY THE VENTS THEMSELVES. BLACK SMOKER "
            "CHIMNEYS (DARK MINERAL CONES WITH HOT MOUTHS), AMBER BACTERIAL MATS ROUND THEM, WHITE TUBE "
            "WORMS WITH RED PLUMES; BETWEEN THE VENTS THE DIM BLUE OF LEVELS 25-26.",
            "A HEAT PLUME ERUPTS (RIGHT): AN AMBER DISC WITH SHIMMER RINGS, THE AREA THAT BURNS AND "
            "BENDS SHOTS. THE GREY DEPTH HUNTER WRECK (LEFT) LIES NOSE-DOWN, SCORCHED, A PALE HULL CODE "
            "SHOWING THROUGH THE PAINT (HINT 3). NO ROOK: THE BULWARK FLIES HIS SLOT (NOT DRAWN)."]
    return frame, layers, notes, text


# =========================================================================== L28 hatchery

FLESH = ((14, 6, 16), (30, 14, 34), (50, 24, 54), (74, 38, 74), (100, 56, 92), (130, 84, 112))


def egg_rack(st, x, y0, y1, seed):
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(st.emit)
    lay = blank()
    dl = ImageDraw.Draw(lay)
    bend = lambda y: x + 18 * np.sin(y / 90.0 + seed)                                  # noqa: E731
    spine = [(bend(y), y) for y in np.arange(y0, y1, 6)]
    dl.line(spine, fill=(40, 22, 44, 255), width=12)
    dl.line(spine, fill=(70, 40, 70, 255), width=4)
    for y in np.arange(y0 + 12, y1 - 8, 24):
        for sx in (-1, 1):
            if rng.uniform() < 0.15:
                continue
            px = bend(y) + sx * 16 + rng.uniform(-2, 2)
            py = y + rng.uniform(-3, 3)
            rx, ry = rng.uniform(8, 11), rng.uniform(7, 10)
            tone_ = rng.uniform(0.75, 1.0)
            dl.ellipse([px - rx, py - ry, px + rx, py + ry], fill=(int(200 * tone_), int(186 * tone_),
                       int(170 * tone_), 255), outline=(60, 36, 54, 255))
            dl.arc([px - rx + 2, py - ry + 2, px + rx - 2, py + ry - 2], 200, 300, fill=(240, 232, 220, 255))
            c = TEAL if rng.uniform() < 0.6 else VIOLET
            d.ellipse([px - 2.5, py - 2, px + 2.5, py + 2], fill=c + (int(rng.uniform(90, 200)),))
    st.racks.append(lay)
    for y in np.arange(y0 + 30, y1, 70):
        st.lights.append((bend(y), y, 52, (0.35, 0.95, 0.85), 0.8))


def hatchery_still():
    col, _ = silt_albedo(281, FLESH, relief=0.34)
    st = Still(col)
    st.racks = []
    st.amb = 0.08 + 0.4 * np.clip(1 - YY / 220, 0, 1) ** 2
    st.ambcol = (1.0, 0.45, 0.25)                                    # the vent's red-amber ahead
    st.snow = 50
    for i, x in enumerate((70, 176, 316, 420)):
        egg_rack(st, x, 40 + 30 * (i % 2), 520 - 20 * (i % 2), 10 + i)
    base = Image.fromarray(np.clip(col, 0, 255).astype(np.uint8)).convert("RGBA")
    for lay in st.racks:
        base.alpha_composite(lay)
    st.albedo = np.array(base).astype(np.float64)[..., :3]
    rng = np.random.default_rng(6)
    d = ImageDraw.Draw(st.low)
    for _ in range(120):                                              # drifting spores
        x, y = rng.uniform(0, W), rng.uniform(0, H)
        c = TEAL if rng.uniform() < 0.5 else (200, 255, 230)
        d.point((x, y), fill=c + (int(rng.uniform(80, 200)),))
    st.ground_units += [(spr("spine-turret", 4), 176, 66), (spr("spine-turret", 4), 316, 500)]
    st.ship, st.rook = (246, 452), None
    st.units += [(jelly(), 246, 200), (spr("lamprey", 0), 120, 260), (spr("lamprey", 0), 372, 300)]
    bullets(st.over, [(246, 236), (226, 250), (266, 250)], ring=(255, 48, 140))
    bolts(st.over, 246, 452)
    return st


def hatchery():
    frame, layers, _ = render(hatchery_still())
    layers = {"ground": layers["ground"], "light": layers["light"], "low-air": layers["low-air"],
              "air": layers["air"]}
    notes = {"ground": "X1.0 FLESH FLOOR, EGG RACKS (PALE PODS, GLOWING CORES)",
             "light": "LIGHT MAP: THE VENT'S RED-AMBER AHEAD, RACK GLOW",
             "low-air": "DRIFTING SPORES", "air": "PLAY PLANE; A JELLY, LAMPREYS"}
    text = ["L28 THE HATCHERY, THE BROOD GALLERIES: THE VRELL NURSERY'S OWN BIOLUMINESCENCE. LONG "
            "GALLERIES OF EGG RACKS (ROWS OF PALE PODS WITH TEAL AND VIOLET CORES) ON A DARK FLESH-AND-"
            "RESIN FLOOR; THE VENT'S RED-AMBER GLOW RISES AHEAD (THE TOP OF THE FIELD), BRIGHTER WITH "
            "EVERY SECTION UNTIL THE ARENA'S LIME-WHITE THROAT.",
            "MOTION: DRIFTING SPORES; THE PODS' CORES PULSE. TURRET NESTS SIT ON THE RACKS' ENDS."]
    return frame, layers, notes, text


# =========================================================================== L26 blackwater

def black_still(t=0.0):
    col, _ = silt_albedo(261, PLAIN, relief=0.2)
    st = Still(col * 0.5)
    st.amb = np.full((H, W), 0.06)
    st.ambcol = (0.5, 0.7, 1.0)
    st.snow = 120
    st.cone_k = 1.05
    st.seed = 26
    sway = 26 * np.sin(t * TAU / SECONDS)
    st.ship = (236 + sway, 456)
    st.rook, st.rook_lamp = (300 + sway * 0.8, 500), True
    a1 = angler()
    bob = 3 * np.sin(t * 3)
    st.units += [(a1, 150 + 8 * np.sin(t * 1.5), 160 + bob), (a1, 360, 300 + bob),
                 (jelly(), 300 - 10 * np.sin(t * 1.2), 120), (jelly(), 210, 330 + 6 * np.sin(t * 2)),
                 (spr("lamprey", 0), 96, 330), (spr("lamprey", 0), 410, 170)]
    st.pickups.append((spr("pickup-salvage-medium", int(t * 8) % 8), 404, 262))
    lamp(st.over, 404, 262, GOLD, 0.0, 18, 0.9)
    bullets(st.over, [(296 - 12 * t, 160 + 18 * t), (316 - 12 * t, 176 + 18 * t)], ring=(255, 96, 40))
    bolts(st.over, st.ship[0], st.ship[1], phase=t * 210)
    return st


# =========================================================================== sheets

def field_sheet(title, frame, layers, notes, text, label=None):
    m36.SUB = SUB
    img = m36.field_sheet(title, frame, layers, notes, text)
    if label:
        d = ImageDraw.Draw(img)
        d.rectangle([516, 36, 1124, 46], fill=raster.SHEET_BG if hasattr(raster, "SHEET_BG")
                    else (20, 22, 30, 255))
        raster.draw_text(img, 516, 38, label, raster.LABEL_DIM)
    return img


def save(img, name):
    OUT.mkdir(parents=True, exist_ok=True)
    p = OUT / name
    img.convert("RGB").save(p, optimize=True)
    print("wrote", p.relative_to(ROOT))


def save_gif(frames, name, colors=128, max_mb=6.0):
    p = OUT / name
    with tempfile.TemporaryDirectory() as tmp:
        for i, f in enumerate(frames):
            f.convert("RGB").save(Path(tmp) / f"f{i:03d}.png")
        while True:
            cmd = ["ffmpeg", "-v", "error", "-y", "-framerate", str(FPS), "-i",
                   str(Path(tmp) / "f%03d.png"), "-vf",
                   f"split[a][b];[a]palettegen=max_colors={colors}:stats_mode=full[p];"
                   "[b][p]paletteuse=dither=none:diff_mode=rectangle", "-loop", "0", str(p)]
            subprocess.run(cmd, check=True)
            if p.stat().st_size <= max_mb * 1e6 or colors <= 48:
                break
            colors -= 16
    print("wrote", p.relative_to(ROOT), f"{p.stat().st_size / 1e6:.1f} MB, {colors} colours")


STILLS = {
    "ice": ("europa-ice-shelf", "EUROPA - L22 ICE SHELF (R48): THE RIG FIELD", ice_shelf),
    "descent": ("europa-descent", "EUROPA - L23 THE DESCENT (R48): THE WATERLINE", descent),
    "thera": ("europa-thera-deep", "EUROPA - L23 THERA DEEP (R48): THE LAMP-LIT FARMS", thera_deep),
    "plain": ("europa-abyssal-plain", "EUROPA - L24 ABYSSAL PLAIN (R48): THE LAMPS' EDGE", plain),
    "trench": ("europa-siren-trench", "EUROPA - L25 SIREN TRENCH (R48): NESTS AND BEACONS", trench),
    "vents": ("europa-vents", "EUROPA - L27 HYDROTHERMAL VENTS (R48): THE CHIMNEY GARDEN", vents),
    "hatchery": ("europa-hatchery", "EUROPA - L28 THE HATCHERY (R48): THE BROOD GALLERIES",
                 hatchery),
}


# =========================================================================== L28 breakout

def ice_ceiling(alpha_top=170, y_end=280, seed=88, cracks=1.0):
    """The underside of the ice sheet (high-air): pale, translucent toward the top, bright cracks."""
    n = nfield(50, seed, 4)
    a = np.clip((y_end - YY) / y_end, 0, 1) ** 1.3 * alpha_top * (0.7 + 0.5 * n)
    col = raster.ramp([(0, (90, 140, 170)), (1, (210, 236, 248))], n)
    img = to_img(col, np.round(a / 20) * 20)
    crack_layer = blank()
    d = ImageDraw.Draw(crack_layer)
    rng = np.random.default_rng(seed)
    for _ in range(int(9 * cracks)):
        x, y = rng.uniform(40, 440), rng.uniform(0, y_end * 0.7)
        for _ in range(int(rng.integers(3, 7))):
            a2 = rng.uniform(0, TAU)
            ln = rng.uniform(14, 50)
            x2, y2 = x + np.cos(a2) * ln, y + np.sin(a2) * ln * 0.7
            d.line([x, y, x2, y2], fill=(250, 255, 255, 200), width=1)
            x, y = x2, y2
    img.alpha_composite(crack_layer.filter(ImageFilter.GaussianBlur(2.5)))
    img.alpha_composite(crack_layer)
    return img


def slab(rng, size):
    pts = []
    for k in range(int(rng.integers(4, 7))):
        a = k * TAU / 6 + rng.uniform(-0.3, 0.3)
        r = size * rng.uniform(0.6, 1.0)
        pts.append((size * 1.2 + np.cos(a) * r, size * 1.2 + np.sin(a) * r))
    im = Image.new("RGBA", (int(size * 2.4), int(size * 2.4)), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    tone = rng.uniform(0.8, 1.0)
    d.polygon(pts, fill=(int(196 * tone), int(222 * tone), int(240 * tone), 255),
              outline=(70, 100, 130, 255))
    d.line(pts[:3], fill=(250, 255, 255, 255), width=2)
    return im


def glassfin_flee(img, rng, n, box):
    d = ImageDraw.Draw(img)
    x0, y0, x1, y1 = box
    for _ in range(n):
        x, y = rng.uniform(x0, x1), rng.uniform(y0, y1)
        d.line([x, y, x, y + 6], fill=(200, 240, 250, 230), width=2)
        d.point((x, y - 1), fill=TEAL + (255,))


def breakout_frames():
    rng = np.random.default_rng(48)
    # 1. the climb
    st = Still(shaft_albedo(glow_far=0.0))
    st.amb = 0.18 + 0.7 * np.clip(1 - YY / 320, 0, 1) ** 2
    st.ambcol = (0.75, 0.9, 1.0)
    st.caustic = 0.4
    st.snow = 60
    st.ship = (240, 420)
    glassfin_flee(st.over, rng, 16, (150, 120, 330, 330))
    st.high = ice_ceiling()
    f1, _, _ = render(st)
    # 2. the burst
    st2 = Still(shaft_albedo(glow_far=0.0))
    st2.amb = 0.4 + 0.9 * gauss2(240, 120, 170)
    st2.ambcol = (0.9, 0.97, 1.0)
    st2.caustic = 0.5
    st2.ship = (240, 250)
    st2.high = ice_ceiling(120, 200, cracks=0.4)
    f2, _, _ = render(st2)
    flash = to_img(np.ones((H, W, 3)) * 255, gauss2(240, 130, 120) * 230)
    f2.alpha_composite(flash)
    f2.alpha_composite(soft_blobs(91, [(240, 150, 120), (180, 210, 60), (300, 200, 70)],
                                  (236, 246, 252), 190, cell=10, blur=4))
    for _ in range(18):
        a, r = rng.uniform(0, TAU), rng.uniform(70, 280)
        x, y = 240 + np.cos(a) * r, 150 + np.sin(a) * r * 0.85
        sp = slab(rng, rng.uniform(12, 34)).rotate(rng.uniform(0, 360), expand=True)
        if r > 150:
            sp = sp.filter(ImageFilter.GaussianBlur(1.2))
        sprite.paste_center(f2, sp, x, y)
    ring(f2, 240, 150, 150, (220, 245, 255), 5, 180)
    sprite.paste_center(f2, spr("ship", 2), 240, 250)
    sp = soft_blobs(92, [(240, 300, 18), (240, 330, 14)], (230, 246, 255), 200, cell=8, blur=2)
    f2.alpha_composite(sp)
    # 3. the night ice under Jupiter
    col, _ = ice_albedo(seed=611, night=True)
    hole = np.hypot((XX - 240) / 110, (YY - 520) / 90) < 1 + 0.12 * (nfield(12, 612, 2) - 0.5)
    col = np.where(hole[..., None], jupiter_tex(300, 500, 220) * 0.22 + np.array((4, 10, 18)), col)
    st3 = Still(col, water=False)
    st3.amb = np.ones((H, W))
    st3.ambcol = (1, 1, 1)
    st3.ship = (240, 330)
    for x, y in ((100, 80), (396, 130)):                           # other drill holes, carriers
        ImageDraw.Draw(st3.emit).ellipse([x - 30, y - 22, x + 30, y + 22], fill=(6, 12, 20, 255))
        car = Image.new("RGBA", (56, 64), (0, 0, 0, 0))
        dc = ImageDraw.Draw(car)
        dc.polygon([(28, 2), (46, 18), (52, 44), (40, 62), (16, 62), (4, 44), (10, 18)],
                   fill=(46, 26, 52, 255), outline=(16, 8, 20, 255))
        for k in range(4):
            dc.line([(14 + k * 9, 20), (12 + k * 10, 54)], fill=(70, 40, 76, 255), width=2)
        for k in range(3):
            dc.ellipse([20 + k * 6, 36, 24 + k * 6, 40], fill=TEAL + (255,))
        st3.units.append((car, x, y))
        st3.low.alpha_composite(steam(int(x), x + 10, y + 20, 60, -0.3, 18, (200, 214, 232), 170))
    for _ in range(14):                                              # slabs round the hole
        a = rng.uniform(np.pi * 1.05, np.pi * 1.95)
        r = rng.uniform(100, 170)
        sp = slab(rng, rng.uniform(8, 18)).rotate(rng.uniform(0, 360), expand=True)
        st3.units.append((sp, 240 + np.cos(a) * r * 1.2, 520 + np.sin(a) * r * 0.9))
    st3.low.alpha_composite(steam(613, 200, 520, 120, -0.9, 34, (210, 224, 240), 170))
    bolts(st3.over, 240, 330)
    f3, _, _ = render(st3)
    return f1, f2, f3


def breakout_sheet():
    frames = breakout_frames()
    w = 16 + 3 * 496
    img = raster.sheet(w, 760, "EUROPA - L28 THE BREAKOUT (R48): UP THE SHAFT, THROUGH THE ICE", SUB)
    caps = ["1. THE CLIMB (T=105-115): LIGHT GROWS FROM ABOVE, THE ICE CEILING CRACKS",
            "2. THE BREAKOUT (T=115): WHITE BURST, SLABS TUMBLE OUT, THE GRADE LIFTS",
            "3. THE NIGHT ICE (T=118): JUPITER-LIT, VRELL CARRIERS RISING FROM OTHER HOLES"]
    for i, (f, c) in enumerate(zip(frames, caps)):
        x = 16 + i * 496
        raster.draw_text(img, x, 38, c, raster.ACCENT if i == 1 else raster.LABEL_DIM)
        img.alpha_composite(f.convert("RGBA"), (x, 50))
    text = ["EUROPA'S SIGNATURE SET PIECE, LEVEL 28 SECTIONS 6-8, THREE FRAMES AT 1X. THE CLIMB REVERSES "
            "LEVEL 23'S SHAFT: THE WATER GRADE STAYS, THE LIGHT COMES FROM THE ICE CEILING (HIGH-AIR) "
            "WHOSE CRACKS BRIGHTEN; FLEEING GLASSFIN STREAK UPWARD (STAND-INS) AND FIRE NOTHING.",
            "THE BURST: A WHITE FLASH AT THE BREACH, A PRESSURE RING, ICE SLABS TUMBLING OUT OF THE FIELD "
            "(THE FAR ONES BLURRED), SPRAY; THE UNDER-WATER GRADE LIFTS OVER 1 S AS THE MEDIUM SWITCHES TO "
            "AIR. AFTER: LEVEL 22'S ICE AT NIGHT, LIT ORANGE-BROWN BY JUPITER, THE BREACH BEHIND THE SHIP "
            "MIRRORING JUPITER, VRELL CARRIERS RISING FROM OTHER DRILL HOLES. THE ASCENDANCY CRUISERS' "
            "RISE IS ON THE CRUISER SHEETS (ASCENDANCY-CRUISER-R48)."]
    y = 600
    for line in text:
        for part in m36.wrap(line, 240):
            raster.draw_text(img, 16, y, part, raster.LABEL)
            y += 11
        y += 6
    return img


# =========================================================================== Q2: the grade a/b

LADDER = ["L23 THERA DEEP", "L24 PLAIN", "L25 TRENCH", "L26 BLACKWATER", "L27 VENTS", "L28 HATCHERY"]


def ladder(v):
    look = GRADE_A if v == "a" else GRADE_B
    out = {}
    out[LADDER[0]] = thera_frame(look)[0]
    out[LADDER[1]] = render(plain_still(), look)[0]
    out[LADDER[2]] = render(trench_still(), look)[0]
    out[LADDER[3]] = render(black_still(), DARK_A if v == "a" else GRADE_B)[0]
    out[LADDER[4]] = render(vents_still(), look)[0]
    out[LADDER[5]] = render(hatchery_still(), look)[0]
    return out


GRADE_TEXT = {
    "a": ["Q2 A, LAMP-LIT TO BLACK (RECOMMENDED): THE LIGHT UNDER 20 KM OF ICE IS THE CITY'S AND THE "
          "VRELL'S. L23 THE FARMS IN GROW-LAMP POOLS (THE CHOSEN R07 LOOK), L24 THE PLAIN AT THE LAMPS' "
          "EDGE (WARM BEHIND, DARK AHEAD), L25 THE TRENCH AT ABOUT 14 % WITH BEACONS AND SACS, L26 BLACK "
          "(THE HEADLIGHT AND GLOWS ONLY), L27 THE VENTS' AMBER, L28 THE HATCHERY'S TEAL AND THE VENT'S "
          "RED. EVERY LEVEL A DIFFERENT LIGHT, DARKENING TOWARD L26.",
          "THE PLAY FIELD AT 1X: L24 AT THE LAMPS' EDGE. ENEMY BULLETS STAY BRIGHT EVERYWHERE; UNITS "
          "TAKE THE LIGHT MAP, THEIR GLOWS STAY LIT."],
    "b": ["Q2 B, DARK EVERYWHERE FROM L23: AMBIENT 6 % IN EVERY UNDER-WATER LEVEL, NO CAUSTICS; ONLY THE "
          "HEADLIGHT, THE LAMPS' SMALL POOLS AND THE VRELL GLOWS LIGHT THE FIELD. THE FARMS AND THE "
          "PLAIN LOSE THEIR COLOUR, THE SIX LEVELS READ ALIKE AND L26'S DARKNESS IS NO LONGER A "
          "BEAT OF ITS OWN (THE COST NAMED IN THE ROUND).",
          "THE PLAY FIELD AT 1X: THE SAME L24 MOMENT AS A. ENEMY BULLETS STAY BRIGHT; UNITS OUTSIDE THE "
          "CONE SHOW THEIR GLOW ONLY."],
}


def grade_sheet(v):
    look = GRADE_A if v == "a" else GRADE_B
    frame, _, _ = render(plain_still(), look)
    lad = ladder(v)
    notes = {k: "" for k in lad}
    name = "LAMP-LIT TO BLACK" if v == "a" else "DARK EVERYWHERE"
    return field_sheet(f"EUROPA - UNDER-WATER GRADE {v.upper()} (R48): {name}", frame, lad, notes,
                       GRADE_TEXT[v], label="THE LADDER L23-L28 IN THIS GRADE (0.4X)")


def grade_gif(v):
    look = GRADE_A if v == "a" else GRADE_B
    return [render(plain_still(i / FPS), look, i / FPS)[0] for i in range(FPS * SECONDS)]


# =========================================================================== L26 darkness a/b

DARK_TEXT = {
    "a": ["L26 DARKNESS A, THE SPEC'S CONE: AMBIENT 6 %; THE HEADLIGHT IS A CRISP 70-DEGREE CONE OF 260 "
          "PX WITH A SMALL HALO ROUND THE HULL. INSIDE IT UNITS ARE DRAWN WHOLE; OUTSIDE ONLY THEIR GLOW "
          "(TEAL, VIOLET, EYES) AND A 15 % RIM. ENEMY BULLETS ARE ALWAYS BRIGHT. PICKUPS GLOW WARM GOLD; "
          "THE GLOW ANGLER'S LURE IS A TEAL BEAD (ITS STALK SHOWS WHEN LIT). ROOK CARRIES A 56 PX LAMP.",
          "EVERYTHING IS HITTABLE (Q4 A, A LOOK ONLY). THE SONAR PULSE DRAWS EVERY UNIT WHOLE FOR 6 S."],
    "b": ["L26 DARKNESS B, A SOFT LAMP POOL: AMBIENT 6 %; INSTEAD OF A HARD CONE THE SHIP CARRIES A ROUND "
          "POOL OF LIGHT (ABOUT 150 PX, FADING) AND A LONG, NARROW, FAINT BEAM AHEAD THAT LIGHTS THE "
          "MARINE SNOW (BACK-SCATTER). UNITS FADE IN GRADUALLY AS THEY NEAR THE SHIP; FROM AFAR ONLY "
          "THEIR GLOW AND THE 15 % RIM. BULLETS, PICKUPS, THE LURE AND ROOK'S LAMP AS IN A.",
          "SAME RULES AS A (EVERYTHING HITTABLE, THE SONAR PULSE DRAWS EVERY UNIT WHOLE); ONLY THE SHAPE OF "
          "THE LIGHT DIFFERS: A SHOWS FURTHER AHEAD, B SHOWS MORE TO THE SIDES AND BEHIND."],
}


def darkness_sheet(v):
    look = DARK_A if v == "a" else DARK_B
    st = black_still()
    frame, layers, _ = render(st, look)
    sonar_look = dict(look, units_full=True)
    sonar, _, _ = render(st, sonar_look)
    ring(sonar, st.ship[0], st.ship[1], 300, (120, 255, 220), 3, 150)
    img = raster.sheet(1140, 760, f"EUROPA - L26 DARKNESS {v.upper()} (R48): "
                       + ("THE HARD CONE" if v == "a" else "THE SOFT LAMP POOL"), SUB)
    raster.draw_text(img, 16, 38, "PLAY FIELD 480X540 AT 1X", raster.LABEL_DIM)
    img.alpha_composite(frame, (16, 50))
    x0 = 516
    cx, cy = 300, 214
    crop = frame.crop((cx, cy, cx + 140, cy + 130)).resize((280, 260), Image.NEAREST)
    raster.draw_text(img, x0, 38, "THE LURE (TEAL) NEXT TO A PICKUP (GOLD), 2X", raster.ACCENT)
    img.alpha_composite(crop, (x0, 50))
    rx, ry = int(st.rook[0]) - 60, int(st.rook[1]) - 70
    crop = frame.crop((rx, ry, rx + 130, ry + 110)).resize((260, 220), Image.NEAREST)
    raster.draw_text(img, x0 + 300, 38, "ROOK'S LAMP AND THE SHIP, 2X", raster.ACCENT)
    img.alpha_composite(crop, (x0 + 300, 50))
    raster.draw_text(img, x0, 330, "SONAR PULSE ON (0.4X)", raster.ACCENT)
    img.alpha_composite(sonar.resize((192, 216), Image.BOX), (x0, 342))
    raster.draw_text(img, x0 + 204, 330, "LIGHT MAP (0.4X)", raster.ACCENT)
    img.alpha_composite(layers["light"].resize((192, 216), Image.BOX), (x0 + 204, 342))
    raster.draw_text(img, x0 + 408, 330, "GLOW ONLY (AIR, 0.4X)", raster.ACCENT)
    cell = r02.checker(192, 216)
    cell.alpha_composite(layers["air"].resize((192, 216), Image.BOX))
    img.alpha_composite(cell, (x0 + 408, 342))
    y = 600
    for line in DARK_TEXT[v]:
        for part in m36.wrap(line, 102):
            raster.draw_text(img, x0, y, part, raster.LABEL)
            y += 11
        y += 5
    return img


def darkness_gif(v):
    look = DARK_A if v == "a" else DARK_B
    return [render(black_still(i / FPS), look, i / FPS)[0] for i in range(FPS * SECONDS)]


# =========================================================================== the hydro-kit

KIT_SIZE, KIT_FACTOR = 48, 8
KIT_BANKS = [-30, -15, 0, 15, 30]


def kit_model(v, bank):
    from render import models
    from render.sdf import (Material, mirror_x, rotate_y, sd_box, sd_capsule, sd_cylinder_y,
                            sd_ellipsoid, subtract, union)
    base, mats = models.ship_a_model(bank=bank, palette=B.ship_colors())
    n0 = len(mats)
    KIT, HAZ, LAMPM, NAVR, NAVG, SONAR = range(n0, n0 + 6)
    mats = mats + [
        Material((0.17, 0.19, 0.23), metal=0.45, shininess=30, spec=0.4),
        Material((0.95, 0.62, 0.12), metal=0.25, shininess=40, spec=0.5),
        Material((0.1, 0.1, 0.1), emission=(2.4, 2.25, 1.8)),
        Material((0.1, 0.05, 0.05), emission=(2.2, 0.25, 0.2)),
        Material((0.05, 0.1, 0.05), emission=(0.25, 2.0, 0.7)),
        Material((0.32, 0.62, 0.7), metal=0.8, shininess=120, spec=1.2),
    ]

    def scene(p):
        d, m = base(p)
        pr = rotate_y(p, bank)
        q = mirror_x(pr)
        nav = np.where(pr[:, 0] > 0, NAVG, NAVR).astype(np.int32)
        if v == "a":
            shroud = subtract((sd_cylinder_y(q, (0.2, -0.9, -0.01), 0.115, 0.09), KIT),
                              sd_cylinder_y(q, (0.2, -0.9, -0.01), 0.078, 0.2))
            parts = [shroud,
                     (sd_capsule(q, (0.42, -0.36, 0.05), (0.42, 0.02, 0.05), 0.055, 0.05), HAZ),
                     (sd_box(q, (0.17, 0.05, 0.075), (0.035, 0.2, 0.02), 0.01), KIT),
                     (sd_ellipsoid(q, (0.13, 0.66, 0.05), (0.03, 0.04, 0.03)), LAMPM),
                     (sd_ellipsoid(q, (0.95, 0.36, 0.0), (0.022, 0.03, 0.022)), nav)]
            d, m = union((d, m), *parts, k=0.008)
            band = (np.abs(q[:, 1] + 0.17) < 0.03) & (np.abs(q[:, 0] - 0.42) < 0.08)
            m = np.where(band & (m == HAZ), KIT, m)
        else:
            duct = subtract((sd_cylinder_y(q, (0.2, -0.86, -0.01), 0.13, 0.12), KIT),
                            sd_cylinder_y(q, (0.2, -0.86, -0.01), 0.1, 0.3))
            parts = [(sd_capsule(q, (0.6, -0.44, 0.07), (0.6, 0.34, 0.07), 0.06, 0.06), KIT),
                     (sd_ellipsoid(q, (0.6, 0.42, 0.07), (0.035, 0.04, 0.035)), LAMPM),
                     (sd_box(q, (0.6, -0.05, 0.09), (0.05, 0.03, 0.04), 0.01), HAZ),
                     (sd_ellipsoid(pr, (0.0, -0.3, 0.11), (0.08, 0.13, 0.06)), SONAR),
                     duct,
                     (sd_ellipsoid(q, (0.6, -0.5, 0.07), (0.025, 0.03, 0.025)), nav)]
            d, m = union((d, m), *parts, k=0.008)
        return d, m

    return scene, mats


def _kit_render(args):
    v, bank, size, factor = args
    import artkit
    scene, mats = kit_model(v, np.radians(bank))
    if size == KIT_SIZE:
        scene = artkit.perspective(scene, 4.0)
    return sdf.render(scene, mats, (size * factor, size * factor), 2.3)


def kit_sprites(v):
    sys.path.insert(0, str(ROOT / "tools" / "art"))
    from concurrent.futures import ProcessPoolExecutor
    jobs = [(v, b, KIT_SIZE, KIT_FACTOR) for b in KIT_BANKS] + [(v, 0, 64, 4)]
    with ProcessPoolExecutor() as pool:
        his = list(pool.map(_kit_render, jobs))
    frames = [sprite.make_sprite(h, KIT_FACTOR, 32) for h in his[:5]]
    src = sprite.to_image(his[5], 0.5) if hasattr(sprite, "to_image") else None
    return frames, src


def wake(img, x, y, v, t=0.0):
    """Under water the engines leave a pump wake instead of a flame."""
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(int(t * 10) + 3)
    for sx in (-5, 5):
        d.line([x + sx, y + 20, x + sx, y + 34], fill=(170, 220, 240, 90), width=2 if v == "a" else 4)
        for k in range(12):
            yy = y + 20 + k * 2.6 + rng.uniform(0, 2)
            spread = (0.5 if v == "a" else 1.6) * k * 0.5
            xx = x + sx + rng.uniform(-spread, spread)
            d.point((xx, yy), fill=(220, 244, 255, int(230 - 16 * k)))


KIT_TEXT = {
    "a": ["HYDRO-KIT A, SEALED REFIT: A FIELD REFIT THAT KEEPS THE STORMHAWK'S SHAPE. PUMP-JET SHROUDS "
          "ROUND BOTH ENGINES, ORANGE BALLAST SADDLES ON THE WING ROOTS, DARK SEAL PLATES OVER THE "
          "INTAKES, TWIN HEADLIGHTS UNDER THE CANARDS, RED AND GREEN NAV LIGHTS AT THE WINGTIPS.",
          "UNDER WATER THE ENGINE FLAME IS REPLACED BY A TIGHT PUMP WAKE (TWO JETS OF FINE BUBBLES). FREE "
          "AND AUTOMATIC FROM LEVEL 23 (NO SLOT, NO POWER); ROOK'S VARIANT-C STORMHAWK GETS THE SAME KIT "
          "PARTS. THE WEAPON PODS STILL FIT ON THE WINGS (THE SADDLES SIT INBOARD)."],
    "b": ["HYDRO-KIT B, PONTOON KIT: AN EXTERNAL KIT THAT CHANGES THE SILHOUETTE. A SLIM PONTOON ON EACH "
          "WING WITH A LAMP IN ITS NOSE AND AN ORANGE CLAMP, A SONAR BLISTER BEHIND THE CANOPY, WIDE "
          "DUCTED SHROUDS ON THE ENGINES, NAV LIGHTS AT THE PONTOONS' TAILS.",
          "UNDER WATER THE ENGINES LEAVE A WIDER, SWIRLING DUCT WAKE. READS AS 'A SUB' AT A GLANCE, BUT "
          "THE PONTOONS SIT WHERE THE WEAPON PODS GO, SO THE PODS WOULD NEED NEW OFFSETS (COST)."],
}


def kit_sheet(v):
    frames, src = kit_sprites(v)
    name = "SEALED REFIT" if v == "a" else "PONTOON KIT"
    img = raster.sheet(1140, 668, f"STORMHAWK HYDRO-KIT {v.upper()} (R48): {name}", SUB)
    raster.draw_text(img, 16, 38, "SOURCE RENDER (4X)", raster.LABEL_DIM)
    cell = r02.checker(256, 256)
    big = src.resize((256, 256), Image.LANCZOS)
    cell.alpha_composite(big)
    img.alpha_composite(cell, (16, 50))
    raster.draw_text(img, 290, 38, "STOCK (TOP) AND KIT (BOTTOM), BANKING FRAMES 48 PX AT 2X",
                     raster.LABEL_DIM)
    for i in range(5):
        c1 = r02.checker(96, 96)
        c1.alpha_composite(spr("ship", i).resize((96, 96), Image.NEAREST))
        img.alpha_composite(c1, (290 + i * 104, 50))
        c2 = r02.checker(96, 96)
        c2.alpha_composite(frames[i].resize((96, 96), Image.NEAREST))
        img.alpha_composite(c2, (290 + i * 104, 156))
    # in game: lamp-lit plain and the dark
    st = plain_still()
    st.ship_sprite = frames[2]
    st.rook = None
    f1, _, _ = render(st)
    wake(f1, st.ship[0], st.ship[1], v)
    st2 = black_still()
    st2.ship_sprite = frames[2]
    f2, _, _ = render(st2, DARK_A)
    wake(f2, st2.ship[0], st2.ship[1], v)
    for i, (f, s, lab) in enumerate(((f1, st, "IN L24 (LAMPS' EDGE), 1X"),
                                     (f2, st2, "IN L26 (DARKNESS A), 1X"))):
        x, y = int(s.ship[0]) - 130, int(s.ship[1]) - 200
        crop = f.crop((x, y, x + 260, y + 260))
        raster.draw_text(img, 16 + i * 276, 352, lab, raster.ACCENT)
        img.alpha_composite(crop, (16 + i * 276, 364))
    x = 16 + 2 * 276
    raster.draw_text(img, x, 352, "KIT FRAME AT 4X WITH ITS WAKE", raster.ACCENT)
    z = Image.new("RGBA", (64, 72), (8, 30, 40, 255))
    sprite.paste_center(z, frames[2], 32, 26)
    wake(z, 32, 26, v)
    img.alpha_composite(z.resize((256, 288), Image.NEAREST), (x, 364))
    y = 262
    for line in KIT_TEXT[v]:
        for part in m36.wrap(line, 130):
            raster.draw_text(img, 290, y, part, raster.LABEL)
            y += 11
        y += 4
    return img


# =========================================================================== main

def main():
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    gifs = "--no-gif" not in sys.argv
    everything = list(STILLS) + ["breakout", "grade", "darkness", "kit"]
    for what in args or everything:
        if what in STILLS:
            fname, title, fn = STILLS[what]
            frame, layers, notes, text = fn()
            save(field_sheet(title, frame, layers, notes, text), f"{fname}-r48-a.png")
        elif what == "breakout":
            save(breakout_sheet(), "europa-breakout-r48-a.png")
        elif what == "grade":
            for v in "ab":
                save(grade_sheet(v), f"underwater-grade-r48-{v}.png")
                if gifs:
                    save_gif(grade_gif(v), f"underwater-grade-r48-{v}.gif")
        elif what == "darkness":
            for v in "ab":
                save(darkness_sheet(v), f"darkness-r48-{v}.png")
                if gifs:
                    save_gif(darkness_gif(v), f"darkness-r48-{v}.gif")
        elif what == "kit":
            for v in "ab":
                save(kit_sheet(v), f"hydro-kit-r48-{v}.png")


if __name__ == "__main__":
    main()
