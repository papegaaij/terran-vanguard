#!/usr/bin/env python3
"""Production art: the Drill Rig, Level 22's hardened Vrell drill rig on Europa's ice shelf
(design/enemies/ground/drill-rig; M8 part B batch, concept round 51, step ART-B). From the chosen
round-48 look **b** "rust auger flower" (tools/concept/enemies_r48.py ``rig_core``/``rig_pump`` b, the
role colours ``drill-rig-b`` = rust / lime / lime), with the user's round-48 note "b's augers are faint":
the three auger blades are rebuilt here thicker, taller and in their own dark iron-rust chitin with
bone cutting edges, so they read against the rust mound (the concept's thin seam-coloured capsules
vanished into it). The mound, studs, bore and pumps are the concept's models at production scale.

The rig is `fixed` (one heading, lit from the upper left); the collar spins decoratively. Sizes from the
stat block: core 120 px (hitbox 96), pump 32 px (hitbox 24).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims
``drill-rig`` once design/enemies/ground/drill-rig/data.yaml exists, first_level 22):
  drill-rig_0..7            128x128, normal: the core, the auger collar turned k x 15 degrees (the blades
                            repeat every 120 degrees, so 8 frames are one loop; 12 fps = 180 degrees/s,
                            the concept's speed); the bore's lime eye in the middle
  drill-rig-glow_0..7       128x128, additive, the same order: the lime bore and the mound's lime plate seams alone
                            with a soft lime bloom round the bore (drawn over the frame above the low-air layer)
  drill-rig-stump           128x128, normal: the stilled collar left after the core's death (remains):
                            the blades stopped, scorched and cracked, the bore dark, frost on the rim
  drill-rig-base            192x192, normal (ground decal, drawn under the core and the pumps, centred on
                            the core): the four ribbed feed hoses out to the pumps, the chitin root ring,
                            the Vrell teal growth on the churned ice round it
  drill-rig-crack           192x192, normal with stepped translucency (ground decal, replaces the base
                            when the core dies): the open bore hole, the radial cracks in the shelf, the
                            dark meltwater stain and the hoses' torn ends
  drill-rig-steam_0..7      128x160, stepped translucency, a loop at 8 fps: the steam plume rising off the
                            dead bore and drifting up-right, lit from the upper left with its own soft
                            shadow (drawn over the stump; the bore at (64, 112) of the frame)
  drill-rig-pump_0..3       36x36, normal: a ribbed rust bellows pump heaving (swell 0, 0.5, 1, 0.5;
                            6 fps loop), its lime tip lit
  drill-rig-pump-dead       36x36, normal: a destroyed pump, the bellows burst and slumped, scorched
  drill-rig-muzzle_0..5     160x160, additive, centred on the core: 0-3 the 0.5 s tell before a shard
                            ring (lime light gathering on the collar's rim with ten bright points at the
                            ring's ten directions, the first along straight down), 4-5 the release flash
                            (3 steps a frame); the ring's ten directions are k x 36 degrees
  assets/pivots/drill-rig.json   the core's centre in its frame, the four pumps' offsets (px, dx right,
                            dy up the screen, the stat block's convention: the data's part offsets must
                            equal these, the hoses of the base decal end there), the decals' and the
                            steam's anchors
  design/enemies/ground/concept/drill-rig-final-r51-a.png/.gif   the review sheet and GIF (on the shelf:
                            the collar spinning, the tell and a ring of ten shards, bombs taking the pumps
                            one by one, the core's death, the stump, crack and steam)

Run: python3 tools/art/drill_rig.py [--review]   (~1 min; --review only rebuilds the review files)
"""
import contextlib
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r36 as e36  # noqa: E402  (concept script, unchanged: folded(), sd_torus_z, lift())
import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look b's models and colours)
from render import raster  # noqa: E402
from render.enemy_models import V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SAC, V_SEAM  # noqa: E402
from render.sdf import Material, rotate_z, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, union  # noqa: E402

SCRIPT = "drill_rig.py"
BATCH = "M8 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r51"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "ground" / "concept"
HELD = ROOT / "build" / "art-held" / "sprites"
ROOT_NAME = "drill-rig"

CORE = (128, 128)
CORE_EXTENT = 2.12                      # model units across the 128 px frame: the base ring 0.95 -> 114 px across
PX = CORE[0] / CORE_EXTENT              # px per model unit
SPIN = 8                                # frames over the blades' 120 degrees
PUMP = (36, 36)
PUMP_EXTENT = 1.65
PUMP_SWELL = (0.0, 0.5, 1.0, 0.5)
DECAL = (192, 192)
STEAM = (128, 160)
STEAM_BORE = (64, 112)                  # the bore in the steam frame
MUZZLE = (160, 160)
# The pumps' offsets from the core's centre, px, dx right and dy up the screen (the stat block's convention).
PUMPS = [(-62, 58), (62, 58), (62, -58), (-62, -58)]
RING = 10
LIME = np.array(e48.LIME, dtype=np.float64)
TEAL = np.array(e48.TEAL, dtype=np.float64)
COLOURS, GLOW_COLOURS, PUMP_COLOURS = 44, 20, 24


# =========================================================================== claims (held sprites)

def claimed():
    """SpriteUse claims an enemy's sprites once its stat block exists (its first level's atlas)."""
    return any((DESIGN / "enemies").glob(f"*/{ROOT_NAME}/data.yaml"))


@contextlib.contextmanager
def sprite_folder():
    target = artkit.SPRITES if claimed() else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def load(name):
    for folder in (artkit.SPRITES, HELD):
        before = artkit.SPRITES
        artkit.SPRITES = folder
        try:
            frames = artkit.load_frames(name)
        finally:
            artkit.SPRITES = before
        if frames:
            return frames
    raise FileNotFoundError(name)


# =========================================================================== model

BLADE, EDGE = 7, 8                      # extra material slots: the augers' dark iron-rust chitin, their bone edges


def mats(glow=1.0, scorched=0.0):
    """The concept's drill-rig-b slots (rust body, lime glow), lifted as the concept did, plus the blades'."""
    m = e36.lift(e48.vmats("drill-rig-b", glow, "b"), 0.15)
    m += [Material((0.30, 0.11, 0.06), metal=0.45, shininess=120, spec=1.25),
          Material((0.93, 0.84, 0.68), metal=0.25, shininess=90, spec=1.0)]
    if scorched:
        k = 1 - 0.55 * scorched
        m = [replace(x, albedo=tuple(np.array(x.albedo) * k), emission=(0.0, 0.0, 0.0), spec=x.spec * k)
             for x in m]
        m[V_EYE] = replace(m[V_EYE], albedo=(0.05, 0.05, 0.06))
        m[V_GLOW] = replace(m[V_GLOW], albedo=(0.07, 0.07, 0.08))
    return m


def blade_items(f3, stopped=False):
    """One auger blade per sector of ``f3``: a thick curved blade sweeping from the bore out over the mound,
    rising toward the bore (dark iron-rust), with a bone cutting edge along its leading side."""
    items = []
    for k in range(5):
        a = -0.42 + 0.27 * k
        r0 = 0.27 + 0.13 * k
        a1, r1 = a + 0.3, r0 + 0.15
        z0, z1 = 0.5 - 0.055 * k, 0.44 - 0.055 * k
        rad = 0.115 - 0.014 * k
        p0 = (np.cos(a) * r0, np.sin(a) * r0, z0)
        p1 = (np.cos(a1) * r1, np.sin(a1) * r1, z1)
        items.append((sd_capsule(f3, p0, p1, rad, rad * 0.8), BLADE))
        lead = 0.07 if not stopped else 0.05
        q0 = (np.cos(a + lead) * r0, np.sin(a + lead) * r0, z0 + rad * 0.55)
        q1 = (np.cos(a1 + lead) * r1, np.sin(a1 + lead) * r1, z1 + rad * 0.5)
        items.append((sd_capsule(f3, q0, q1, rad * 0.42, rad * 0.32), EDGE))
    return items


def core_model(rot=0.0, glow=1.0, scorched=0.0):
    """The rust auger flower: a chitin mound on a dark base ring, bone studs round its rim, the lime bore and
    three augers turned by ``rot`` (radians, clockwise on the screen)."""
    def scene(p):
        r = rotate_z(p, -rot)
        items = [(sd_cylinder_z(p, (0, 0, 0.02), 0.95, 0.05), V_DARK),
                 (sd_ellipsoid(p, (0, 0, 0.0), (0.84, 0.84, 0.3)), V_BODY),
                 (sd_cylinder_z(p, (0, 0, 0.2), 0.3, 0.16), V_GLOW),
                 (sd_sphere(p, (0, 0, 0.18), 0.25), V_EYE)]
        items += blade_items(e36.folded(r, 3, 0.0), scorched > 0)
        f9 = e36.folded(p, 9, 0.0)
        items.append((sd_sphere(f9, (0.8, 0, 0.12), 0.075), V_BONE))
        d, m = union(*items, k=0.035)
        if scorched:                                      # cracks bitten into the stopped collar
            cut = sd_capsule(p, (-0.62, 0.15, 0.4), (0.1, 0.55, 0.4), 0.035, 0.02)
            cut2 = sd_capsule(p, (0.25, -0.65, 0.4), (0.6, 0.05, 0.4), 0.03, 0.02)
            d = np.maximum(d, -np.minimum(cut, cut2))
        return d, m
    return scene, mats(glow, scorched)


def pump_model(swell=0.0, dead=False):
    """A ribbed rust bellows pump (the concept's b) on its root; ``dead``: burst and slumped."""
    def scene(p):
        f5 = e36.folded(p, 5, 0.0)
        if dead:
            body = sd_ellipsoid(p, (0.04, -0.02, 0.02), (0.72, 0.66, 0.2))
            body = np.maximum(body, -sd_sphere(p, (0.0, 0.0, 0.3), 0.36))     # the burst crater
            return union((body, V_BODY),
                         (sd_capsule(f5, (0.25, 0, 0.18), (0.72, 0, 0.06), 0.07, 0.04), V_SEAM),
                         (sd_sphere(p, (0, 0, -0.02), 0.2), V_DARK), k=0.05)
        return union((sd_ellipsoid(p, (0, 0, 0.12), (0.7, 0.7, 0.4 + 0.1 * swell)), V_BODY),
                     (sd_capsule(f5, (0.15, 0, 0.52 + 0.08 * swell), (0.72, 0, 0.14), 0.085, 0.05), V_SEAM),
                     (sd_sphere(p, (0, 0, 0.55 + 0.1 * swell), 0.15), V_GLOW), k=0.05)
    return scene, mats(1.0, 0.85 if dead else 0.0)


def intel_model(scale=0.84):
    """(scene, mats) for the hangar intel portrait: the core (collar at rest) with the four pumps on their hoses,
    scaled by ``scale`` to fit the 30 px card."""
    core, m = core_model(0.0)
    pump, _ = pump_model(0.6)
    offs = [(dx / PX, dy / PX) for dx, dy in PUMPS]

    def scene(p):
        q = p / scale
        d, mm = core(q)
        for ox, oy in offs:
            sp = (PUMP[0] / PUMP_EXTENT) / PX                  # the pump's model units in the core's
            dp, mp = pump((q - np.array((ox, oy, 0.0))) / sp)
            dp = dp * sp
            hose = sd_capsule(q, (ox * 0.45, oy * 0.45, 0.08), (ox * 0.9, oy * 0.9, 0.08), 0.1, 0.09)
            closer = dp < d
            mm = np.where(closer, mp, mm)
            d = np.minimum(d, dp)
            mm = np.where(hose < d, V_SEAM, mm)
            d = np.minimum(d, hose)
        return d * scale, mm
    return scene, m


def _render(args):
    kind, a = args
    if kind == "core":
        scene, m = core_model(a)
        return artkit.native(*artkit.render_hi(scene, m, CORE, CORE_EXTENT))
    if kind == "glow":
        scene, m = core_model(a)
        dark = [replace(x, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for x in m]
        img = artkit.native(*artkit.render_hi(scene, dark, CORE, CORE_EXTENT), crisp=0)
        return img
    if kind == "stump":
        scene, m = core_model(a, glow=0.0, scorched=1.0)
        return artkit.native(*artkit.render_hi(scene, m, CORE, CORE_EXTENT))
    if kind == "pump":
        scene, m = pump_model(a)
        return artkit.native(*artkit.render_hi(scene, m, PUMP, PUMP_EXTENT))
    if kind == "pump-dead":
        scene, m = pump_model(dead=True)
        return artkit.native(*artkit.render_hi(scene, m, PUMP, PUMP_EXTENT))
    if kind == "hoses":
        return render_hoses()
    raise ValueError(kind)


def spin(k):
    return k * (TAU / 3) / SPIN


# =========================================================================== glow (additive)

def glow_frame(img):
    """A blackened render -> the additive glow: the emissive parts, a soft lime bloom round the bore."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    h, w = rgb.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    d2 = (xx + 0.5 - w / 2) ** 2 + (yy + 0.5 - h / 2) ** 2
    rgb += LIME * 0.42 * np.exp(-d2 / (2 * 11.0 ** 2))[..., None]
    rgb[rgb.max(axis=-1) < 10] = 0
    rgb = np.clip(rgb, 0, 255)
    return artkit.additive(Image.fromarray(np.dstack([rgb, np.full((h, w), 255.0)]).astype(np.uint8), "RGBA"))


# =========================================================================== decals

SS = 4                                   # supersampling of the drawn decals


def pump_xy(i, size=DECAL):
    dx, dy = PUMPS[i]
    return size[0] / 2 + dx, size[1] / 2 - dy


def render_hoses():
    """The four feed hoses and the root ring as one lit render (192 px, the core's scale)."""
    ext = CORE_EXTENT * DECAL[0] / CORE[0]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, 0.0), 0.97, 0.05), V_DARK)]
        for dx, dy in PUMPS:
            ex, ey = dx / PX, dy / PX
            mx, my = ex * 0.55, ey * 0.55
            items.append((sd_capsule(p, (ex * 0.4, ey * 0.4, 0.06), (mx, my, 0.13), 0.1, 0.1), V_SEAM))
            items.append((sd_capsule(p, (mx, my, 0.13), (ex * 0.9, ey * 0.9, 0.08), 0.1, 0.085), V_SEAM))
            items.append((sd_cylinder_z(p, (ex, ey, 0.0), 0.22, 0.05), V_DARK))
        return union(*items, k=0.06)
    m = mats(0.55)
    hi, f = artkit.render_hi(scene, m, DECAL, ext)
    return artkit.native(hi, f)


def growth_layer(seed, reach=(62, 92), n=420, dead=0.0):
    """Teal Vrell growth on the ice round the rig: lumpy blobs with lit tops and teal veins (a 1-bit decal)."""
    W, H = DECAL[0] * SS, DECAL[1] * SS
    img = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    c = DECAL[0] / 2
    base = np.array((14, 44, 46)) * (1 - dead) + np.array((52, 52, 56)) * dead
    lit = np.array((30, 92, 84)) * (1 - dead) + np.array((90, 92, 96)) * dead
    blobs = []
    for _ in range(n):
        a = rng.uniform(0, TAU)
        r = rng.uniform(*reach) * (0.85 + 0.15 * np.sin(3 * a + 1.0))
        s = rng.uniform(1.3, 4.2) * (1.2 - (r - reach[0]) / (reach[1] - reach[0]) * 0.6)
        blobs.append((c + np.cos(a) * r, c + np.sin(a) * r, s, rng.uniform(0.8, 1.15)))
    for x, y, s, k in blobs:
        col = tuple(int(v) for v in np.clip(base * k, 0, 255))
        d.ellipse([(x - s) * SS, (y - s) * SS, (x + s) * SS, (y + s) * SS], fill=col + (255,))
    for x, y, s, k in blobs:
        col = tuple(int(v) for v in np.clip(lit * k, 0, 255))
        d.ellipse([(x - s * 0.8) * SS, (y - s * 0.8) * SS, (x + s * 0.1) * SS, (y + s * 0.1) * SS], fill=col + (255,))
    if dead < 1:
        for j in range(14):                                   # teal veins running out over the ice
            a0 = j * TAU / 14 + rng.uniform(-0.15, 0.15)
            pts = []
            for u in np.linspace(0, 1, 14):
                a = a0 + 0.35 * np.sin(u * 3 + j)
                r = reach[0] - 6 + (reach[1] + 4 - reach[0]) * u
                pts.append(((c + np.cos(a) * r) * SS, (c + np.sin(a) * r) * SS))
            d.line(pts, fill=(0, 196, 132, 255), width=SS)
    return img


def frost_ring(seed, r0=56, r1=94, tone=(226, 236, 246), dark=(120, 148, 176), levels=4, max_a=0.75):
    """Churned, frosted ice round the bore (stepped translucency): broken ice chips lit from the upper left."""
    W, H = DECAL[0], DECAL[1]
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    c = W / 2
    rr = np.hypot(xx + 0.5 - c, yy + 0.5 - c)
    n = raster.fbm(W, H, 9, seed, octaves=3, period=False)
    band = np.clip(1 - np.abs(rr - (r0 + r1) / 2) / ((r1 - r0) / 2), 0, 1)
    a = np.clip(band * (0.35 + 0.9 * n) - 0.2, 0, 1)
    a = artkit.ordered_dither(a * max_a / 0.8, levels) * 0.8
    gy, gx = np.gradient(n)
    shade = np.clip(1 + (gx + gy) * -14, 0.6, 1.25)
    col = np.array(tone) * shade[..., None] * (n[..., None] > 0.45) + np.array(dark) * (n[..., None] <= 0.45)
    return Image.fromarray(np.dstack([np.clip(col, 0, 255), a * 255]).astype(np.uint8), "RGBA")


def base_decal(hoses):
    img = frost_ring(5101)
    g = growth_layer(5102).resize(DECAL, Image.Resampling.BOX)
    g = Image.fromarray(np.where(np.array(g)[..., 3:4] >= 128, np.array(g), 0).astype(np.uint8), "RGBA")
    img.alpha_composite(g)
    img.alpha_composite(hoses)
    return img


def crack_decal(hoses):
    """The dead rig's ground: the open bore, radial cracks, the meltwater stain, the hoses' torn ends."""
    W, H = DECAL[0] * SS, DECAL[1] * SS
    c = W / 2
    rng = np.random.default_rng(5111)
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    rr = np.hypot(xx - c, yy - c) / SS
    n = raster.fbm(W, H, 40, 5112, octaves=3, period=False)
    stain = np.clip((74 - rr) / 30 + (n - 0.5) * 0.9, 0, 1)
    rgb = np.zeros((H, W, 3)) + np.array((40, 66, 92))
    alpha = artkit.ordered_dither(np.array(Image.fromarray((stain * 255).astype(np.uint8)).resize(
        DECAL, Image.Resampling.BOX), dtype=np.float64) / 255 * 0.7, 4)
    img = Image.fromarray(np.dstack([np.zeros(DECAL[::-1] + (3,)) + np.array((44, 70, 98)),
                                     alpha * 255]).astype(np.uint8), "RGBA")
    big = Image.new("RGBA", (W, H), (0, 0, 0, 0))
    d = ImageDraw.Draw(big)
    for k in range(9):                                       # radial cracks, branching
        a = k * TAU / 9 + rng.uniform(-0.2, 0.2)
        x, y, r = c + np.cos(a) * 30 * SS, c + np.sin(a) * 30 * SS, 30.0
        pts = [(x, y)]
        while r < rng.uniform(70, 92):
            a += rng.uniform(-0.35, 0.35)
            x, y = x + np.cos(a) * 6 * SS, y + np.sin(a) * 6 * SS
            r = np.hypot(x - c, y - c) / SS
            pts.append((x, y))
        w = int(2.6 * SS)
        d.line(pts, fill=(18, 30, 46, 255), width=w)
        d.line([(px + SS, py + SS) for px, py in pts], fill=(214, 230, 244, 255), width=SS)
    for rad, col in ((36, (176, 196, 214)), (31, (24, 40, 58)), (24, (8, 14, 22))):   # rim, wall, the hole
        d.ellipse([c - rad * SS, c - rad * SS, c + rad * SS, c + rad * SS], fill=col + (255,))
    d.ellipse([c - 22 * SS, c - 20 * SS, c + 8 * SS, c + 2 * SS], fill=(14, 24, 36, 255))
    small = big.resize(DECAL, Image.Resampling.BOX)
    small = Image.fromarray(np.where(np.array(small)[..., 3:4] >= 128, np.array(small), 0).astype(np.uint8), "RGBA")
    img.alpha_composite(small)
    torn = np.array(hoses)                                   # the hoses' outer halves stay, torn at the root ring
    yy2, xx2 = np.mgrid[0:DECAL[1], 0:DECAL[0]]
    keep = np.hypot(xx2 + 0.5 - DECAL[0] / 2, yy2 + 0.5 - DECAL[1] / 2) > 64
    torn[~keep] = 0
    t = Image.fromarray(torn, "RGBA")
    ta = np.array(t).astype(np.float64)
    ta[..., :3] *= 0.6                                       # scorched
    img.alpha_composite(Image.fromarray(ta.astype(np.uint8), "RGBA"))
    return img


def steam_frames(n=8):
    """The plume off the dead bore: puffs born at the bore, rising (growing) and drifting up-right, a loop."""
    W, H = STEAM
    frames = []
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    noise = raster.fbm(W, H, 12, 5121, octaves=3, period=False)
    for f in range(n):
        a = np.zeros((H, W))
        for k in range(6):
            u = (f / n + k / 6) % 1.0
            cx = STEAM_BORE[0] + 26 * u + 4 * np.sin(k * 2.1)
            cy = STEAM_BORE[1] - 92 * u
            r = 9 + 22 * u
            dd = np.hypot(xx - cx, yy - cy) / r
            a = np.maximum(a, np.clip(1 - dd ** 2, 0, 1) * (0.7 + 0.5 * noise) * (1 - 0.75 * u) * min(1, u * 6 + 0.3))
        a = np.clip(a, 0, 1)
        blur = np.array(Image.fromarray((a * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3)),
                        dtype=np.float64) / 255
        gy, gx = np.gradient(blur)
        shade = np.clip(1 + (gx + gy) * 40, 0.62, 1.1)
        rgb = np.array((240, 246, 252)) * shade[..., None]
        alpha = artkit.ordered_dither(blur * 0.82, 5)
        img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8), "RGBA")
        frames.append(img)
    return frames


def muzzle_frames():
    """The shard ring's tell and release (additive): lime light on the collar's rim, ten points brightening."""
    W, H = MUZZLE
    yy, xx = np.mgrid[0:H, 0:W].astype(np.float64)
    c = W / 2
    rr = np.hypot(xx + 0.5 - c, yy + 0.5 - c)
    ang = np.arctan2(yy + 0.5 - c, xx + 0.5 - c)             # 0 = right; straight down = +pi/2
    frames = []
    for f in range(6):
        if f < 4:
            k = (f + 1) / 4
            rim = np.exp(-((rr - 50) / (3 + 3 * k)) ** 2) * 0.55 * k
            pts = np.zeros_like(rr)
            for j in range(RING):
                a = np.pi / 2 + j * TAU / RING
                px, py = c + np.cos(a) * 54, c + np.sin(a) * 54
                pts += np.exp(-((xx + 0.5 - px) ** 2 + (yy + 0.5 - py) ** 2) / (2 * (1.2 + 1.6 * k) ** 2))
            v = rim + pts * (0.5 + 0.6 * k) + np.exp(-(rr / 18) ** 2) * 0.35 * k
        else:
            k = 1.0 if f == 4 else 0.45
            ring_r = 56 if f == 4 else 66
            rays = np.zeros_like(rr)
            for j in range(RING):
                a = np.pi / 2 + j * TAU / RING
                da = np.angle(np.exp(1j * (ang - a)))
                rays += np.exp(-(da / 0.06) ** 2) * np.clip(1 - np.abs(rr - ring_r) / 16, 0, 1)
            v = (np.exp(-((rr - ring_r) / 5) ** 2) * 0.7 + rays * 0.9 + np.exp(-(rr / 30) ** 2) * 0.45) * k
        v = np.clip(v, 0, 1.2)
        v = artkit.ordered_dither(np.clip(v, 0, 1), 9)
        white = np.clip(v - 0.65, 0, 1) / 0.35
        rgb = LIME[None, None, :] * v[..., None] * (1 - white[..., None] * 0.6) + 255 * white[..., None] * 0.6
        frames.append(artkit.additive(Image.fromarray(
            np.dstack([np.clip(rgb, 0, 255), np.full((H, W), 255.0)]).astype(np.uint8), "RGBA")))
    return frames


# =========================================================================== build

def build():
    jobs = ([("core", spin(k)) for k in range(SPIN)] + [("glow", spin(k)) for k in range(SPIN)]
            + [("stump", spin(2)), ("pump-dead", 0.0), ("hoses", 0.0)]
            + [("pump", s) for s in PUMP_SWELL])
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_render, jobs))
    cores = artkit.quantize_set(out[:SPIN], COLOURS)
    glows = [glow_frame(g) for g in out[SPIN:2 * SPIN]]
    stump = artkit.quantize_set([out[2 * SPIN]], 32)[0]
    pump_dead = out[2 * SPIN + 1]
    hoses = out[2 * SPIN + 2]
    pumps = artkit.quantize_set(out[2 * SPIN + 3:] + [pump_dead], PUMP_COLOURS)
    pump_dead = pumps.pop()
    base = base_decal(hoses)
    crack = crack_decal(hoses)
    with sprite_folder() as folder:
        artkit.write_frames("drill-rig", cores, SOURCE)
        artkit.write_frames("drill-rig-glow", glows, SOURCE)
        artkit.write_frames("drill-rig-stump", [stump], SOURCE, single=True)
        artkit.write_frames("drill-rig-base", [base], SOURCE, single=True)
        artkit.write_frames("drill-rig-crack", [crack], SOURCE, single=True)
        artkit.write_frames("drill-rig-steam", steam_frames(), SOURCE)
        artkit.write_frames("drill-rig-pump", pumps, SOURCE)
        artkit.write_frames("drill-rig-pump-dead", [pump_dead], SOURCE, single=True)
        artkit.write_frames("drill-rig-muzzle", muzzle_frames(), SOURCE)
        print(f"drill-rig: frames -> {folder.relative_to(ROOT)}")
    artkit.write_pivots("drill-rig", SOURCE, {
        "core": {"size": list(CORE), "centre": [CORE[0] // 2, CORE[1] // 2]},
        "pumps": {"offsets": [list(p) for p in PUMPS], "convention": "px from the core's centre, dx right, dy up",
                  "size": list(PUMP)},
        "decals": {"size": list(DECAL), "centre": [DECAL[0] // 2, DECAL[1] // 2]},
        "steam": {"size": list(STEAM), "bore": list(STEAM_BORE)},
        "muzzle": {"size": list(MUZZLE), "centre": [MUZZLE[0] // 2, MUZZLE[1] // 2], "ring": RING,
                   "first_direction": "straight down, then every 36 degrees"},
        "spin": {"frames": SPIN, "fps": 12, "degrees_per_frame": 15},
    })


# =========================================================================== review

FPS = 20


def ice_plate(w, h, seed=2201):
    """A stand-in shelf plate for the review (Level 22's backdrop when it exists)."""
    try:
        import backdrop_l22
        return backdrop_l22.review_plate(w, h)
    except Exception:  # noqa: BLE001  (the backdrop may not exist yet)
        col, _ = ice_plate_np(w, h, seed)
        return Image.fromarray(np.clip(col, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def ice_plate_np(w, h, seed):
    n = raster.fbm(w, h, 70, seed, octaves=5, period=False)
    gy, gx = np.gradient(n)
    shade = np.clip(1 + (gx + gy) * -40, 0.7, 1.15)
    col = raster.ramp([(0.0, (110, 140, 172)), (0.5, (184, 204, 224)), (1.0, (232, 240, 250))], n)
    return col * shade[..., None], n


def lime_tint(img):
    a = np.array(img.convert("RGBA")).astype(np.float64)
    lum = a[..., :3].max(axis=-1, keepdims=True) / 255
    a[..., :3] = np.clip(LIME * lum * 0.9 + 255 * np.clip(lum - 0.9, 0, 1) * 4, 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def add(base, glow, x, y):
    return artkit.add_light(base, glow, (int(round(x - glow.width / 2)), int(round(y - glow.height / 2))))


def shadow(img, sp, x, y, dx=6, dy=8):
    sh = sprite.shadow_of(sp, opacity=0.5, blur=1.0)
    sprite.paste_center(img, sh, x + dx, y + dy)


def review():
    core, glow = load("drill-rig"), load("drill-rig-glow")
    stump, base, crack = load("drill-rig-stump")[0], load("drill-rig-base")[0], load("drill-rig-crack")[0]
    steam, pumps = load("drill-rig-steam"), load("drill-rig-pump")
    pump_dead, muzzle = load("drill-rig-pump-dead")[0], load("drill-rig-muzzle")
    boom = artkit.load_frames("explosion-large")
    small_boom = artkit.load_frames("explosion-medium")
    orb = [lime_tint(o) for o in artkit.load_frames("orb-medium")]   # the game's medium bullet, lime
    ship = artkit.load_frames("ship")[2]

    W, H = 480, 540
    plate = ice_plate(W, H)
    cx, cy = 240, 200
    pump_kill = [3.0, 3.8, 5.6, 6.4]
    core_kill = 8.2
    dur = 11.0

    def rig_layer(t):
        f = plate.copy()
        dead = t >= core_kill
        f.alpha_composite(crack if dead else base, (cx - DECAL[0] // 2, cy - DECAL[1] // 2))
        for i, (dx, dy) in enumerate(PUMPS):
            px, py = cx + dx, cy - dy
            if t < pump_kill[i]:
                sp = pumps[int(t * 6 + i) % 4]
                shadow(f, sp, px, py, 3, 4)
                sprite.paste_center(f, sp, px, py)
            else:
                sprite.paste_center(f, pump_dead, px, py)
        if dead:
            sprite.paste_center(f, stump, cx, cy)
        else:
            k = int(t * 12) % SPIN
            shadow(f, core[k], cx, cy)
            sprite.paste_center(f, core[k], cx, cy)
            f = add(f, glow[k], cx, cy)
        return f

    # ring times: every 3.5 s + 0.5 s per lost pump, the tell 0.5 s before
    rings, tr = [], 1.0
    while tr < core_kill:
        rings.append(tr)
        lost = sum(1 for k in pump_kill if k <= tr)
        tr += 3.5 + 0.5 * lost
    frames = []
    for i in range(int(dur * FPS)):
        t = i / FPS
        f = rig_layer(t)
        for i2, k in enumerate(pump_kill):                     # the pumps' bursts
            if k <= t < k + 0.7:
                fr = small_boom[min(len(small_boom) - 1, int((t - k) / 0.7 * len(small_boom)))]
                dx, dy = PUMPS[i2]
                f = add(f, fr, cx + dx, cy - dy)
        if t >= core_kill:
            u = (t - core_kill) / 1.2
            if u < 1:
                fr = boom[min(len(boom) - 1, int(u * len(boom)))]
                f = add(f, fr.resize((fr.width * 2, fr.height * 2), Image.Resampling.NEAREST), cx, cy)
            if t > core_kill + 0.6:
                sf = steam[int(t * 8) % len(steam)]
                f.alpha_composite(sf, (cx - STEAM_BORE[0], cy - STEAM_BORE[1]))
        for r0 in rings:                                      # the tell, the release, the shards
            if r0 - 0.5 <= t < r0:
                f = add(f, muzzle[min(3, int((t - (r0 - 0.5)) / 0.125))], cx, cy)
            elif r0 <= t < r0 + 0.1:
                f = add(f, muzzle[4 if t < r0 + 0.05 else 5], cx, cy)
            if r0 <= t < r0 + 3.5:
                for j in range(RING):
                    a = np.pi / 2 + j * TAU / RING
                    d = 56 + 110 * (t - r0)
                    x, y = cx + np.cos(a) * d, cy + np.sin(a) * d
                    if -12 < x < W + 12 and -12 < y < H + 12:
                        f = add(f, orb[int(t * 12) % len(orb)], x, y)
        px = 240 + 60 * np.sin(t * 0.7)
        sprite.paste_center(f, ship, px, 470)
        frames.append(f.convert("RGB"))

    still = rig_layer(2.2)
    still = add(still, muzzle[2], cx, cy)
    rows = [("CORE: THE AUGER COLLAR'S SPIN (8 FRAMES OVER 120 DEG, 12 FPS)", core, 1, False),
            ("CORE GLOW (ADDITIVE, SAME ORDER)", glow[:4], 1, True),
            ("STUMP (REMAINS), PUMP HEAVE X4 AND DEAD PUMP (2X)", [stump], 1, False),
            ("PUMPS (2X)", pumps + [pump_dead], 2, False),
            ("BASE AND CRACK DECALS (GROUND)", [base, crack], 1, False),
            ("STEAM PLUME LOOP (8 FPS)", steam[:6], 1, False),
            ("SHARD RING TELL 0-3 AND RELEASE 4-5 (ADDITIVE)", muzzle, 1, True)]
    sheet = artkit.review_sheet("DRILL RIG - FINAL FRAMES (ROUND 48 B, STRONGER AUGERS)", rows, width=1500,
                                batch=BATCH)
    big = Image.new("RGBA", (sheet.width, sheet.height + 560), tuple(artkit.SHEET_BG[:3]) + (255,))
    big.alpha_composite(sheet)
    raster.draw_text(big, 16, sheet.height + 4, "ON THE SHELF AT 1X: THE TELL BEFORE A RING (LEFT); FRAMES FROM THE GIF",
                     raster.LABEL)
    big.alpha_composite(still, (16, sheet.height + 18))
    big.alpha_composite(frames[int(6.0 * FPS)].convert("RGBA"), (16 + 490, sheet.height + 18))
    big.alpha_composite(frames[int(10.0 * FPS)].convert("RGBA"), (16 + 980, sheet.height + 18))
    artkit.save_review(big, frames, CONCEPT, "drill-rig", fps=FPS)


def main(args):
    if "--review" not in args:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
