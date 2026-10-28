#!/usr/bin/env python3
"""Concept round 63 - the Act 5 title card's still, redone (step ART-T, 2026-10-28; round 62 F.9 "redo": the user
rejected the perspective-warped Level 29 ground as "almost like a rendering glitch"). Three entirely different
ideas, models and painted layers only (no tile ground in perspective), each pending the user's review in round 63.

Outputs (design/campaign/act-5-the-belt/concept/):
  act-5-still-r63-a.png   960x540, "the hub turns": Ceres Hub's towers and docking arms standing on Ceres's strongly
                          curved night-side horizon, seen from space; one tall block's windows turning from white to
                          red from the top down, the Helix block in charcoal and gold beside it; the hub's lights
                          sprawling over the dark crust, the dock canyon's lamps running toward us; the CDF transport
                          small in front, engines lit, on its approach; the belt's sunlit dust band and the sun's
                          glare top left over the warm, lit limb on the left
  act-5-still-r63-b.png   960x540, "intercepted": a Stormhawk's dark cockpit, the centre display amber with Vorne's
                          broadcast (the Ascendancy crest, interference bands, INTERCEPTED - ALL CHANNELS), its amber
                          light on the console and the canopy frame; teal radar and nav displays either side; beyond
                          the canopy the belt: warm sunlit rocks, the dust band, the sun's glare top left behind the
                          frame, Ceres a small pale disc with a speck of lights
  act-5-still-r63-c.png   960x540, "the gold wing": the belt's rocks close up with the sun low on the left behind a
                          huge foreground rock (rim-lit), a big sunlit rock top right, a drift of small rocks hazed in
                          the dust; a wing of four Talons (Q.11 b gunmetal with gold panels, the leader's gold spine)
                          in echelon gleaming past below the lettering, gold glints and red running lights
  act-5-still-r63-sheet.png   the contact sheet: each still at half size and the title card over it (as the game draws
                          it: darkened to 55 %, the letterbox, the chrome `ACT V / THE BELT`)

Every still is one 96-colour palette with 4x4 ordered dither (act_stills.py's ``ui_dither``), as Acts 1-4's. Models:
the Talon is round 60's (enemies_r60.talon_model "b") in Q.11 b gunmetal (tools/art/belt_units.py ``gunmetal``,
``rim``), the rocks belt_units.py's ``rock_model``, the transport the CDF heavy transport of Act 3's departure
(tools/art/outro_l14_images.py ``transport_scene``); the hub and the cockpit are modelled here from the render
package's SDF primitives. Nothing in assets/ is written (the current still stays until the user picks).

Run: python3 tools/concept/act5_still_r63.py [a] [b] [c]   (no letter = all three and the sheet; ~5 min on 16 cores)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "art"))
import act_stills as acts  # noqa: E402  (tools/art: title_card, ui_dither, the card's numbers; imported unchanged)
import artkit  # noqa: E402
import belt_units as bu  # noqa: E402  (tools/art: gunmetal, rim, rock_model, star)
import enemies_r60 as e60  # noqa: E402  (the round-60 Talon)
import outro_l14_images as o14  # noqa: E402  (the CDF heavy transport)
from render import raster, sdf  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_cylinder_x,  # noqa: E402
                        sd_cylinder_z, sd_ellipsoid, sd_sphere, union)

W, H = acts.W, acts.H
CONCEPT = acts.DESIGN / "campaign" / "act-5-the-belt" / "concept"
ROUND = "r63"
NAME = "act-5-the-belt"
PPU = 100.0                                    # px per render unit (the renderer's lights are tuned for ~2-3 units)
YY, XX = np.mgrid[0:H, 0:W].astype(np.float64)
DEG = np.pi / 180


# =========================================================================== render kit

def rx(a):
    c, s = np.cos(a), np.sin(a)
    return np.array([[1, 0, 0], [0, c, -s], [0, s, c]])


def ry(a):
    c, s = np.cos(a), np.sin(a)
    return np.array([[c, 0, s], [0, 1, 0], [-s, 0, c]])


def rz(a):
    c, s = np.cos(a), np.sin(a)
    return np.array([[c, -s, 0], [s, c, 0], [0, 0, 1]])


def posed(scene, mats, rot, s, shift=(0.0, 0.0, 0.0)):
    """A model turned by ``rot`` (screen = rot @ model), scaled by ``s`` (screen units per model unit) and its origin
    moved to ``shift`` (screen units from the image centre); the materials' patterns follow it (in model space)."""
    sh = np.asarray(shift, float)

    def sc(p):
        d, m = scene(((p - sh) @ rot) / s)
        return d * s, m

    def wrap(fn):
        return None if fn is None else (lambda p, n, f=fn: f(((p - sh) @ rot) / s, n @ rot))
    return sc, [replace(m, pattern=wrap(m.pattern), emission_pattern=wrap(m.emission_pattern)) for m in mats]


def strip(builder, args, i, n):
    """Rows i of n of a model's w x h native render (PPU px per unit, centred; split so the processes share the big
    renders), 1-bit alpha as the sprites."""
    scene, mats, (w, h, factor, crisp), kw = builder(*args)
    kw.setdefault("z_top", 4.0)
    r0, r1 = h * i // n, h * (i + 1) // n
    yc = (h / 2 - (r0 + r1) / 2) / PPU
    hi = sdf.render(scene, mats, (w * factor, (r1 - r0) * factor), w / PPU, center=(0.0, yc), **kw)
    return artkit.native(hi, factor, crisp)


def paste(rgb, img, cx, cy):
    """Composite a native RGBA image centred at (cx, cy) over the float canvas."""
    a = np.array(img).astype(np.float64)
    h, w = a.shape[:2]
    x0, y0 = int(round(cx - w / 2)), int(round(cy - h / 2))
    xs, ys = max(0, -x0), max(0, -y0)
    xe, ye = min(w, W - x0), min(h, H - y0)
    if xe <= xs or ye <= ys:
        return rgb
    al = a[ys:ye, xs:xe, 3:4] / 255
    tgt = rgb[y0 + ys:y0 + ye, x0 + xs:x0 + xe]
    rgb[y0 + ys:y0 + ye, x0 + xs:x0 + xe] = tgt * (1 - al) + a[ys:ye, xs:xe, :3] * al
    return rgb


def mask_of(img, cx, cy):
    a = np.array(img)[..., 3] > 0
    out = np.zeros((H, W), bool)
    h, w = a.shape
    x0, y0 = int(round(cx - w / 2)), int(round(cy - h / 2))
    xs, ys, xe, ye = max(0, -x0), max(0, -y0), min(w, W - x0), min(h, H - y0)
    out[y0 + ys:y0 + ye, x0 + xs:x0 + xe] = a[ys:ye, xs:xe]
    return out


def glow(rgb, x, y, r, col, k=1.0):
    d = np.hypot(XX - x, YY - y)
    return rgb + (np.exp(-(d / r) ** 2) * k)[..., None] * np.asarray(col, float)


def hash2(a, b, seed=0.0):
    h = np.sin(a * 127.1 + b * 311.7 + seed * 74.7) * 43758.5453
    return h - np.floor(h)


def space(seed, sun, band_y, band_tilt, band_k=1.0, stars=170, sun_r=1.0):
    """Black space in the belt grade (round 60 A.18 a, the lighter one): the belt's warm sunlit dust band across the
    still, stars, the sun's soft glare."""
    rgb = np.zeros((H, W, 3)) + np.array((4, 4, 9.0))
    n = raster.fbm(W, H, 40, seed, octaves=4, period=False)
    fine = raster.fbm(W, H, 9, seed + 1, octaves=3, period=False)
    centre = band_y + (XX - W / 2) * band_tilt + 14 * np.sin(XX / 150 + seed)
    band = np.exp(-((YY - centre) / 74) ** 2) * (0.45 + 0.55 * n) * (0.8 + 0.3 * fine)
    core = np.exp(-((YY - centre - 6) / 22) ** 2) * (0.4 + 0.6 * n) * 0.5
    rgb += (band * band_k)[..., None] * np.array((118, 94, 68.0))
    rgb += (core * band_k)[..., None] * np.array((150, 126, 96.0))
    rng = np.random.default_rng(seed + 2)
    for _ in range(stars):
        x, y = rng.integers(0, W), rng.integers(0, H)
        b = rng.uniform(50, 230) * (1 - 0.6 * min(1.0, band[y, x]))
        rgb[y, x] += (b, b * 0.97, b * 0.9)
    sx, sy = sun
    d = np.hypot(XX - sx, YY - sy)
    rgb += (np.exp(-(d / (210 * sun_r)) ** 2) * 0.55 + np.exp(-(d / (60 * sun_r)) ** 2) * 1.1)[..., None] * \
        np.array((255, 232, 196.0))
    rgb += (np.exp(-(d / 520) ** 2) * 0.18)[..., None] * np.array((170, 130, 90.0))
    return rgb


def finish(rgb):
    """One 96-colour palette with 4x4 ordered dither, as act_stills.py's ``ui_dither``, but the median cut weighs the
    few saturated accent pixels (red windows and beacons, gold glints, the amber display) so they keep a colour."""
    b = np.tile(artkit.BAYER4, (H // 4 + 1, W // 4 + 1))[:H, :W]
    px = np.clip(rgb + ((b - 0.5) * 10.0)[..., None], 0, 255).astype(np.uint8)
    flat = px.reshape(-1, 3).astype(int)
    accent = flat[(flat.max(1) - flat.min(1) > 80) & (flat.max(1) > 110)]
    sample = np.concatenate([flat, np.repeat(accent, 24, axis=0)]).astype(np.uint8)
    palette = Image.fromarray(sample.reshape(-1, 1, 3), "RGB").quantize(
        colors=acts.STILL_COLOURS, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    out = Image.fromarray(px, "RGB").quantize(palette=palette, dither=Image.Dither.NONE)
    return out.convert("RGB").convert("RGBA")


# =========================================================================== a: the hub turns

GREY_HULL, HELIX_HULL, GOLD, WIN_WHITE, WIN_RED, WIN_GOLD, LAMP, BEACON, ROCK, ARM, SHIP = range(11)
# the flanking blocks: cx, depth, half w, half d, height, kind (px, z up from the crust)
HUB_BLOCKS = [(-196, 10, 22, 22, 40, "grey"), (-148, -6, 28, 24, 92, "helix"), (-98, 24, 18, 18, 34, "grey"),
              (96, 22, 20, 18, 46, "grey"), (150, -4, 30, 26, 112, "red"), (204, 16, 20, 18, 36, "grey")]
RED_FROM = 0.38                                # the turned block's windows are red above this share of its height
RING = (112.0, 96.0, 6.0)                      # the docking ring: height, radius, tube radius


def windows(p, n):
    """Strip windows on the vertical faces: rows of small panes, a seeded share of them lit."""
    side = np.abs(n[:, 2]) < 0.5
    along = np.where(np.abs(n[:, 0]) > np.abs(n[:, 1]), p[:, 1], p[:, 0])
    ang = np.arctan2(p[:, 1], p[:, 0]) * 30                                    # round faces: along the circumference
    along = np.where(np.hypot(p[:, 0], p[:, 1]) < 60, ang, along)
    u, v = along / 4.0, p[:, 2] / 7.0
    fu, fv = u - np.floor(u), v - np.floor(v)
    cell = (fu > 0.18) & (fu < 0.82) & (fv > 0.42) & (fv < 0.72)
    lit = hash2(np.floor(u / 3), np.floor(v), np.floor(p[:, 0] / 40)) > 0.3
    return (side & cell & lit & (p[:, 2] > 5)).astype(float)


def hub_model():
    """Ceres Hub's core on its crust shelf (px, z up): the central tower (a broad drum, the shaft, the crown and the
    spire), the docking ring round the shaft on four spokes with two ships moored, the flanking blocks (the Helix block
    charcoal with gold bands, the Coalition blocks grey, one tall block whose windows turn red from the top down),
    radiator fins at the left end, lamps and red beacons."""
    mats = [Material((0.6, 0.62, 0.66), metal=0.35, shininess=50, spec=0.5, pattern=sdf.panel_lines(0.09, 0.05, 0.8)),
            Material((0.2, 0.21, 0.24), metal=0.6, shininess=90, spec=0.8),
            Material((0.85, 0.62, 0.22), metal=0.9, shininess=120, spec=1.2),
            Material((0.6, 0.62, 0.66), metal=0.35, shininess=50, spec=0.5, emission=(1.3, 1.26, 1.14),
                     emission_pattern=windows),
            Material((0.6, 0.62, 0.66), metal=0.35, shininess=50, spec=0.5, emission=(2.0, 0.24, 0.16),
                     emission_pattern=windows),
            Material((0.2, 0.21, 0.24), metal=0.6, shininess=90, spec=0.8, emission=(1.45, 1.08, 0.5),
                     emission_pattern=windows),
            Material((0.2, 0.2, 0.2), emission=(1.6, 1.6, 1.5)),
            Material((0.2, 0.05, 0.05), emission=(2.2, 0.25, 0.2)),
            Material((0.4, 0.35, 0.3), shininess=10, spec=0.1),
            Material((0.42, 0.44, 0.48), metal=0.5, shininess=40, spec=0.5),
            Material((0.72, 0.74, 0.78), metal=0.3, shininess=50, spec=0.6, pattern=sdf.panel_lines(0.12, 0.06, 0.8))]
    rh, rr, rt = RING

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        rad = np.hypot(x, y)
        items = [(sd_ellipsoid(p, (0, 0, -18), (300, 90, 28)), ROCK),                  # the crust shelf
                 (sd_cylinder_z(p, (0, 0, 22), 50, 22), WIN_WHITE),                   # the tower's drum
                 (sd_cylinder_z(p, (0, 0, 96), 24, 56), WIN_WHITE),                   # the shaft
                 (sd_cylinder_z(p, (0, 0, 160), 36, 10), GREY_HULL),                  # the crown
                 (sd_ellipsoid(p, (0, 0, 172), (30, 30, 10)), GREY_HULL),
                 (sd_capsule(p, (0, 0, 176), (0, 0, 216), 3.5, 1.5), ARM),             # the spire
                 (np.hypot(rad - rr, z - rh) - rt, ARM)]                               # the docking ring
        for a in np.arange(4) * np.pi / 2 + np.pi / 4:
            c, s_ = np.cos(a), np.sin(a)
            q = np.stack([x * c + y * s_, -x * s_ + y * c, z], -1)
            items.append((sd_box(q, (60, 0, rh), (36, 2.5, 2.5), 1), ARM))
        for a, ln in ((0.35, 38), (np.pi + 0.5, 30)):                                 # two ships moored outside
            c, s_ = np.cos(a), np.sin(a)
            q = np.stack([x * c + y * s_, -x * s_ + y * c, z], -1)
            items.append((sd_capsule(q, (rr + 16, -ln / 2, rh), (rr + 16, ln / 2, rh), 7, 5), SHIP))
            items.append((sd_box(q, (rr + 6, 0, rh), (6, 2, 2), 0.5), ARM))
        for cx, cy, hw, hd, h, kind in HUB_BLOCKS:
            items.append((sd_box(p, (cx, cy, h / 2), (hw, hd, h / 2), 2.5),
                          HELIX_HULL if kind == "helix" else GREY_HULL))
        items.append((sd_box(p, (150, -4, 118), (18, 16, 6), 2), GREY_HULL))           # the turned block's crown
        for k in range(4):                                                             # radiator fins, left end
            items.append((sd_box(p, (-236 - 9 * k, 0, 26), (1.2, 26, 24), 0.5), SHIP))
        d, m = union(*items)
        for cx, cy, hw, hd, h, kind in HUB_BLOCKS:
            inside = (np.abs(x - cx) < hw + 3) & (np.abs(y - cy) < hd + 3) & (z < h + 1)
            if kind == "helix":
                band = ((z / 23) % 1) < 0.13
                m = np.where(inside & (m == HELIX_HULL), np.where(band, GOLD, WIN_GOLD), m)
            elif kind == "red":
                m = np.where(inside & (m == GREY_HULL), np.where(z > h * RED_FROM, WIN_RED, WIN_WHITE), m)
            else:
                m = np.where(inside & (m == GREY_HULL), WIN_WHITE, m)
        lamps = [(0, 0, 218, 3.5, BEACON), (150, -4, 128, 3.0, BEACON), (-148, -6, 96, 2.6, BEACON),
                 (rr + 2, 0, rh + 7, 2.4, LAMP), (-rr - 2, 0, rh + 7, 2.4, LAMP), (0, -rr - 2, rh + 7, 2.4, LAMP),
                 (0, rr + 2, rh + 7, 2.4, LAMP), (204, 16, 40, 2.4, BEACON)]
        for lx, ly, lz, r, mat in lamps:
            dl = sd_sphere(p, (lx, ly, lz), r)
            m = np.where(dl < d, mat, m)
            d = np.minimum(d, dl)
        return d, m
    return scene, mats


def hub_job():
    scene, mats = hub_model()
    rot = rx(-72 * DEG) @ rz(-10 * DEG)
    sc, mm = posed(scene, mats, rot, 0.85 / PPU, shift=(0.0, -0.9, 0.0))
    return sc, mm, (640, 300, 3, 60), dict(key_pos=(-2.8, 1.4, 0.2), ambient=0.12, fill=0.16, exposure=1.05,
                                          steps=170)


TRANSPORT_ROT = rx(-62 * DEG) @ rz(22 * DEG)     # nose to the right and away, toward the hub
TRANSPORT_S = 170 / 270                          # px per model unit (the hull ~170 px long)
TRANSPORT_AT = (236, 402)


def transport_job():
    scene, mats = o14.transport_scene()                                     # nose to +x, top +z
    sc, mm = posed(scene, mats, TRANSPORT_ROT, TRANSPORT_S / PPU)
    return sc, mm, (180, 130, 4, 70), dict(key_pos=(-2.4, 2.0, 1.2), ambient=0.18, fill=0.25)


CERES = (560.0, 1530.0, 1100.0)                 # the dwarf planet's centre and radius (px): a strong curve
HUB_AT = (790, 372)                             # the hub sprite's centre (its crust shelf under the horizon)


def still_a(parts):
    rgb = space(6301, (-30, -20), 120, -0.12)
    rgb = paste(rgb, parts["hub"], *HUB_AT)
    # Ceres: lit from the left and a little behind, warm grey; the night side to the right with the hub's lights
    cx, cy, r = CERES
    dx, dy = (XX - cx) / r, -(YY - cy) / r
    inside = dx * dx + dy * dy < 1
    nz = np.sqrt(np.clip(1 - dx * dx - dy * dy, 0, 1))
    sun = np.array((-0.92, 0.05, -0.12))
    sun /= np.linalg.norm(sun)
    lam = dx * sun[0] + dy * sun[1] + nz * sun[2]
    light = np.clip(lam * 3.4 + 0.08, 0, 1) ** 1.2
    tex = raster.fbm(W, H, 26, 6311, octaves=5, period=False)
    fine = raster.fbm(W, H, 5, 6312, octaves=2, period=False)
    alb = 0.55 + 0.35 * tex + 0.18 * fine
    # craters, foreshortened toward the limb
    rng = np.random.default_rng(6313)
    for _ in range(46):
        x0 = rng.uniform(100, 960)
        hy = cy - np.sqrt(max(r * r - (x0 - cx) ** 2, 0))
        y0 = hy + rng.uniform(4, 150) ** 1.0
        rad = rng.uniform(5, 26) * (0.4 + (y0 - hy) / 160)
        fz = np.clip((y0 - hy) / 120, 0.18, 1)
        dd = np.hypot((XX - x0) / rad, (YY - y0) / (rad * fz))
        alb = alb - 0.22 * np.exp(-(dd / 0.75) ** 4) * (dd < 1.2) + 0.2 * np.exp(-((dd - 1.0) / 0.14) ** 2)
    ground = np.array((150, 136, 120.0)) * alb[..., None] * light[..., None]
    ground += np.array((10, 11, 16.0)) * (1 - light[..., None])                        # the night side, faint
    limb = np.exp(-((1 - np.sqrt(dx * dx + dy * dy)) * r / 1.6) ** 2)
    ground += (limb * np.clip(lam * 4 + 0.3, 0, 1))[..., None] * np.array((120, 104, 84.0))
    rgb = np.where(inside[..., None], ground, rgb)
    # the hub's sprawl on the night side: lamps foreshortened over the crust, a few gone red under the turned block
    hy = cy - np.sqrt(np.clip(r * r - (XX - cx) ** 2, 0, None))
    lights = np.zeros((H, W, 3))
    for i in range(240):
        x0 = rng.normal(790, 90)
        if not 560 < x0 < 960:
            continue
        top = cy - np.sqrt(r * r - (x0 - cx) ** 2)
        y0 = top + 2 + rng.exponential(18)
        red = abs(x0 - 880) < 40 and rng.uniform() < 0.6
        col = (255, 70, 50) if red else ((255, 236, 200) if rng.uniform() < 0.7 else (200, 220, 255))
        xi, yi = int(x0), int(y0)
        if 0 <= xi < W and 0 <= yi < H:
            lights[yi, xi] = np.maximum(lights[yi, xi], np.array(col) * rng.uniform(0.6, 1.0))
    # the dock canyon's lamps, two rows running from the hub's foot toward us
    for k in range(26):
        t = k / 25
        y0 = 462 + 70 * t ** 1.6
        for side in (-1, 1):
            x0 = 770 - 90 * t + side * (4 + 30 * t ** 1.4)
            lights[int(y0), int(x0)] = (230, 240, 255)
    rgb = rgb + lights
    for i in range(3):                                                             # the glow of the sprawl
        rgb = glow(rgb, 740 + 50 * i, 470 + 4 * i, 60, (34, 30, 26), 0.6)
    rgb = glow(rgb, 916, 372, 44, (70, 8, 6), 0.7)                                 # the turned block's red
    # the transport on its approach, engines toward us
    tr = parts["transport"]
    tx, ty = TRANSPORT_AT
    rgb = paste(rgb, tr, tx, ty)
    for q in ((-128, 8, 9), (-128, -8, 9), (-123, 42, 7), (-123, -42, 7)):          # its four bells (nose +x)
        e = TRANSPORT_ROT @ np.array(q, float) * TRANSPORT_S
        rgb = glow(rgb, tx + e[0], ty - e[1], 6, (255, 200, 140), 1.0)
    e = TRANSPORT_ROT @ np.array((-140, 0, 8.0)) * TRANSPORT_S
    rgb = glow(rgb, tx + e[0], ty - e[1], 30, (120, 80, 50), 0.6)
    return finish(rgb)


# =========================================================================== b: intercepted

CON_BODY, CON_DARK, CON_SCREEN, CON_BUTTON, CON_FRAME, CON_TEAL = range(6)
SCREENS = {"centre": (0, -150, 132, 58), "left": (-318, -186, 88, 50), "right": (318, -186, 88, 50)}  # X, Y, half w, h


def cockpit_model():
    """The Stormhawk's cockpit in screen units (px, origin at the still's centre, Y up, Z toward the camera): the
    console's rounded bulk across the bottom, three display bezels facing us with their buttons, the glare shield
    over the centre display, the canopy frame's two side bows and the top bar."""
    mats = [Material((0.13, 0.135, 0.155), metal=0.35, shininess=40, spec=0.4,
                     pattern=sdf.panel_lines(0.05, 0.03, 0.7)),
            Material((0.09, 0.095, 0.11), metal=0.5, shininess=60, spec=0.6),
            Material((0.01, 0.01, 0.01), metal=0.0, shininess=200, spec=1.5),
            Material((0.1, 0.1, 0.1), emission=(0.5, 0.42, 0.25)),
            Material((0.17, 0.18, 0.21), metal=0.6, shininess=80, spec=0.9),
            Material((0.1, 0.1, 0.1), emission=(0.15, 0.6, 0.55))]

    def surf_z(x, y):
        return -40 + 130 * np.sqrt(np.clip(1 - (x / 640) ** 2 - ((y + 430) / 330) ** 2, 0, 1))

    def scene(p):
        x, y = p[:, 0], p[:, 1]
        items = [(sd_ellipsoid(p, (0, -430, -40), (640, 330, 130)), CON_BODY)]
        for name, (sx, sy, hw, hh) in SCREENS.items():
            zc = float(surf_z(sx, sy)) + 6
            bez = sd_box(p, (sx, sy, zc), (hw + 14, hh + 14, 14), 5)
            cut = sd_box(p, (sx, sy, zc + 14), (hw, hh, 8), 1)
            bez = np.maximum(bez, -cut)
            items.append((bez, CON_DARK))
            items.append((sd_box(p, (sx, sy, zc + 4), (hw, hh, 2), 0), CON_SCREEN))
            for k in range(5):                                                       # the bezel buttons
                bx = sx - hw + hw * 2 * (k + 0.5) / 5
                items.append((sd_box(p, (bx, sy + hh + 8, zc + 14), (7, 3, 2), 1), CON_BUTTON))
                items.append((sd_box(p, (bx, sy - hh - 8, zc + 14), (7, 3, 2), 1), CON_BUTTON))
        items.append((sd_box(p, (0, -82, 70), (170, 7, 34), 5), CON_DARK))            # the glare shield
        q = mirror_x(p)
        items.append((sd_capsule(q, (500, -150, 40), (372, 300, 40), 22, 16), CON_FRAME))   # the canopy bows
        items.append((sd_capsule(q, (470, -160, 64), (430, -60, 64), 9, 9), CON_FRAME))
        items.append((sd_box(p, (0, 292, 40), (420, 22, 16), 6), CON_FRAME))
        items.append((sd_cylinder_x(p, (0, -100, 60), 3, 150), CON_TEAL))              # a teal strip under the shield
        d, m = union(*items)
        teal = (np.abs(y + 100) < 4) & (np.abs(x) < 150) & (m == CON_TEAL)
        m = np.where((m == CON_TEAL) & ~teal, CON_DARK, m)
        return d, m
    return scene, mats


def cockpit_job():
    scene, mats = cockpit_model()
    sc, mm = posed(scene, mats, np.eye(3), 1 / PPU)
    return sc, mm, (W, H, 2, 50), dict(steps=150, key_pos=(-2.2, 1.6, 1.6), ambient=0.1, fill=0.15)


def rock_job(radius_px, seed, key=(-2.2, 2.6, 3.2), spin=0.0, exposure=1.15):
    scene0, mats = bu.rock_model(1.0, seed)
    amp = 0.02 + 0.05 * min(1.0, radius_px / 150)

    def scene(p):
        """belt_units's rock with relief for the big ones: three octaves of 3D value noise pushing the surface."""
        d, m = scene0(p)
        rel = noise3(p * 3.0, seed) + 0.5 * noise3(p * 6.5, seed + 1) + 0.25 * noise3(p * 14.0, seed + 2)
        return (d + amp * rel) / (1 + amp * 14), m
    rot = rx(spin * 0.7) @ rz(spin)
    sc, mm = posed(scene, mats, rot, radius_px / 1.1 / PPU)
    size = int(radius_px * 2.6) // 2 * 2
    return sc, mm, (size, size, 2 if size > 160 else 3, 60), dict(key_pos=key, exposure=exposure, ambient=0.16,
                                                                    z_top=6.0)


def noise3(p, seed):
    """3D value noise in -1..1 (a hashed lattice, smoothstep-blended): the rocks' relief."""
    i = np.floor(p)
    f = p - i
    u = f * f * (3 - 2 * f)
    out = 0.0
    for dx in (0, 1):
        for dy in (0, 1):
            for dz in (0, 1):
                h = np.sin((i[:, 0] + dx) * 127.1 + (i[:, 1] + dy) * 311.7 + (i[:, 2] + dz) * 74.7 + seed * 19.3)
                h = (h * 43758.5453) % 1.0
                wx = u[:, 0] if dx else 1 - u[:, 0]
                wy = u[:, 1] if dy else 1 - u[:, 1]
                wz = u[:, 2] if dz else 1 - u[:, 2]
                out = out + h * wx * wy * wz
    return out * 2 - 1


def amber_screen(w, h, seed):
    """The intercepted broadcast on the centre display: Vorne's production briefing portrait (already amber, the enemy
    channel's) on the right, the Ascendancy crest (a rising sun over a ringed planet) on the left, rolling
    interference bands, scanlines and the enemy channel's labels."""
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    amber = np.array((255, 172, 44.0))
    v = 0.07 + 0.05 * raster.fbm(w, h, 6, seed, octaves=2, period=False)
    cx, cy = w * 0.25, h * 0.56
    d = np.hypot(xx - cx, yy - cy)
    ang = np.arctan2(yy - cy, xx - cx)
    sun = (d < 15) & (yy < cy)
    rays = (d > 18) & (d < 32) & (yy < cy - 2) & (np.cos(ang * 13) > 0.5)
    planet = np.hypot(xx - cx, yy - cy - 8) < 10
    ring = np.abs(np.hypot((xx - cx) / 30, (yy - cy - 8) / 6) - 1) < 0.11
    v = np.where(sun | rays | ring, 0.95, v)
    v = np.where(planet & ~ring, 0.55, v)
    rgb = amber[None, None] * v[..., None]
    port = Image.open(acts.ROOT / "assets" / "sprites" / "portraits" / "briefing-vorne-neutral.png").convert("RGB")
    ph = h - 22
    port = np.array(port.resize((ph, ph), Image.LANCZOS)).astype(np.float64)
    x0 = w - ph - 10
    rgb[14:14 + ph, x0:x0 + ph] = port * 1.15
    band = 0.3 * np.clip(np.sin(yy / 6.0 + 1.3), 0, 1) ** 8 + 0.18 * np.clip(np.sin(yy / 19.0), 0, 1) ** 10
    rgb = rgb + band[..., None] * np.array((255, 190, 90.0)) * 0.6
    rgb = rgb * np.where(yy % 2 < 1, 1.0, 0.7)[..., None]
    img = raster.to_rgba_image(np.clip(rgb, 0, 255))
    raster.draw_text(img, 6, 4, "INTERCEPTED", (255, 206, 100, 255))
    raster.draw_text(img, 6, h - 11, "ALL CHANNELS", (255, 190, 80, 255))
    return np.array(img)[..., :3].astype(np.float64)


def teal_screen(w, h, kind):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    teal = np.array((40, 230, 200.0))
    v = np.full((h, w), 0.05)
    if kind == "radar":
        cx, cy = w / 2, h - 4
        d = np.hypot(xx - cx, yy - cy)
        for r in (24, 48, 72, 96):
            v = np.maximum(v, 0.35 * (np.abs(d - r) < 0.7))
        ang = np.arctan2(yy - cy, xx - cx)
        v = np.maximum(v, 0.6 * np.exp(-((ang + 2.0) / 0.12) ** 2) * (d < 98))
        for bx, by in ((60, 40), (120, 30), (98, 66), (140, 70), (34, 72)):
            v = np.maximum(v, 1.0 * (np.hypot(xx - bx, yy - by) < 1.6))
    else:
        v = np.maximum(v, 0.25 * ((xx % 22 < 0.8) | (yy % 22 < 0.8)))
        v = np.maximum(v, 0.9 * (np.hypot(xx - w * 0.62, yy - h * 0.42) < 4))
    v = v * np.where(yy % 2 < 1, 1.0, 0.75)
    rgb = teal[None, None] * v[..., None]
    img = raster.to_rgba_image(np.clip(rgb, 0, 255))
    raster.draw_text(img, 6, 5, "TAC" if kind == "radar" else "NAV - CERES", (90, 240, 210, 255))
    if kind != "radar":
        raster.draw_text(img, 6, h - 12, "2.1 MKM", (90, 240, 210, 255))
    return np.array(img)[..., :3].astype(np.float64)


def screen_box(name):
    sx, sy, hw, hh = SCREENS[name]
    x0, y0 = int(W / 2 + sx - hw), int(H / 2 - sy - hh)
    return x0, y0, int(2 * hw), int(2 * hh)


def still_b(parts):
    rgb = space(6321, (40, 30), 150, -0.1, band_k=1.05)
    # Ceres far ahead: a pale disc lit from the upper left, a speck of the hub's lights on its night side
    cx, cy, r = 806, 128, 30
    d = np.hypot(XX - cx, YY - cy)
    lam = np.clip(((cx - XX) * 0.7 + (cy - YY) * 0.6) / r + 0.35, 0, 1)
    tex = 0.8 + 0.25 * raster.fbm(W, H, 4, 6322, octaves=2, period=False)
    rgb = np.where((d < r)[..., None], (np.array((206, 192, 172.0)) * tex[..., None] * lam[..., None]
                                        + np.array((6, 6, 9.0))), rgb)
    for k, (dx, dy) in enumerate(((10, 8), (12, 10), (9, 12), (13, 7))):
        rgb[cy + dy, cx + dx] = (255, 240, 210) if k else (255, 70, 50)
    rgb = paste(rgb, parts["rock1"], 186, 278)
    rgb = paste(rgb, parts["rock2"], 668, 238)
    rgb = paste(rgb, parts["rock3"], 470, 96)
    rgb = paste(rgb, parts["rock4"], 900, 330)
    # the cockpit over it
    cock = np.array(parts["cockpit"]).astype(np.float64)
    al = cock[..., 3:4] / 255
    body = cock[..., :3]
    # the amber display's light on the cockpit, falling off from the centre display
    sx, sy, sw, sh = screen_box("centre")
    dd = np.hypot(np.maximum(np.abs(XX - (sx + sw / 2)) - sw / 2, 0),
                  np.maximum(np.abs(YY - (sy + sh / 2)) - sh / 2, 0))
    spill = np.exp(-dd / 90) * 0.5 + np.exp(-dd / 10) * 0.35
    body = body + (spill * al[..., 0])[..., None] * np.array((92, 52, 12.0)) * (
        0.25 + body.mean(-1, keepdims=True) / 60)
    rgb = rgb * (1 - al) + body * al
    # the displays
    for name in SCREENS:
        x0, y0, w, h = screen_box(name)
        content = (amber_screen(w, h, 6323) if name == "centre"
                   else teal_screen(w, h, "radar" if name == "left" else "nav"))
        rgb[y0:y0 + h, x0:x0 + w] = content
    rgb = glow(rgb, sx + sw / 2, sy + sh / 2, 170, (44, 24, 4), 0.5)
    return finish(rgb)


# =========================================================================== c: the gold wing

SUN_C = (150, 404)                                  # the low sun, just clear of the foreground rock's edge
FRONT_AT = (-10, 566)                               # the foreground rock's centre (radius 240 px)
# x, y, size px (the span), leader: the wing recedes to the left, the leader nearest
TALONS = [(318, 378, 84, False), (452, 394, 110, False), (618, 410, 142, False), (812, 400, 174, True)]
TAL_YAW = [-118, -117, -116, -115]                  # nose to the right and toward us
TAL_ROLL = [22, 23, 24, 25]                         # banked, the tops turned to us


def talon_rot(yaw, roll):
    return rx(-35 * DEG) @ rz(yaw * DEG) @ ry(roll * DEG)


def talon_job(size, leader, yaw, roll):
    scene, mats = e60.talon_model("b", leader)
    rot = talon_rot(yaw, roll)
    sc, mm = posed(scene, bu.gunmetal(mats), rot, size / 2.1 / PPU)
    return sc, mm, (size + 8, size + 8, 4, 80), dict(key_pos=(-3.4, -0.2, 1.4), exposure=0.98)


def gold_glint(img):
    """The brightest gold pixel of a Talon render (where the low sun catches a gold panel), px from its top left."""
    a = np.array(img).astype(np.float64)
    score = (a[..., 0] + a[..., 1] - 1.6 * a[..., 2]) * (a[..., 3] > 0)
    y, x = np.unravel_index(np.argmax(score), score.shape)
    return x, y


def still_c(parts):
    rgb = space(6331, SUN_C, 330, -0.24, band_k=1.25, stars=120, sun_r=0.5)
    haze = np.array((120, 98, 74.0))
    for key, x, y, k in (("f1", 880, 300, 0.5), ("f2", 380, 104, 0.62), ("f3", 560, 250, 0.66), ("f4", 250, 276, 0.55),
                         ("f5", 724, 214, 0.6), ("f6", 640, 84, 0.68), ("f7", 120, 160, 0.6)):
        im = np.array(parts[key]).astype(np.float64)
        im[..., :3] = im[..., :3] * (1 - k) + haze * k
        rgb = paste(rgb, Image.fromarray(im.astype(np.uint8), "RGBA"), x, y)
    rgb = paste(rgb, parts["big"], 858, 118)
    for i, (x, y, size, leader) in enumerate(TALONS):
        rot = talon_rot(TAL_YAW[i], TAL_ROLL[i])
        nose = rot @ np.array((0, 1.0, 0))
        back = -np.array((nose[0], -nose[1])) / np.hypot(nose[0], nose[1])
        for ex in (-0.12, 0.12):                                                   # the twin engines' short trails
            e = rot @ np.array((ex, -0.74, 0)) * size / 2.1
            px, py = x + e[0], y - e[1]
            for t in np.linspace(0, 1, 12):
                rgb = glow(rgb, px + back[0] * 0.5 * size * t, py + back[1] * 0.5 * size * t, 1.6 + 2.5 * t,
                           (255, 110, 50), 0.45 * (1 - t))
        rgb = paste(rgb, parts[f"t{i}"], x, y)
        for ex in (-0.12, 0.12):
            e = rot @ np.array((ex, -0.74, 0)) * size / 2.1
            rgb = glow(rgb, x + e[0], y - e[1], 2.5 + size / 80, (255, 170, 100), 1.2)
    rgb = paste(rgb, parts["front"], *FRONT_AT)
    # the low sun just clear of the foreground rock: its glare spills over the rock's edge
    m = mask_of(parts["front"], *FRONT_AT)
    pad = np.pad(m, 2)
    edge = m & ~(pad[:-4, 2:-2] & pad[2:-2, 4:] & pad[4:, 2:-2] & pad[2:-2, :-4])
    near = np.exp(-(np.hypot(XX - SUN_C[0], YY - SUN_C[1]) / 110) ** 2)
    rgb += (edge * near * 1.2)[..., None] * np.array((255, 226, 180.0))
    d = np.hypot(XX - SUN_C[0], YY - SUN_C[1])
    rgb += (np.exp(-(d / 16) ** 2) * 1.2 + np.exp(-(d / 60) ** 2) * 0.3)[..., None] * np.array((255, 236, 205.0))
    # gold glints where the sun catches the Talons' gold panels
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8)).convert("RGBA")
    for i, (x, y, size, leader) in enumerate(TALONS):
        t = parts[f"t{i}"]
        gx, gy = gold_glint(t)
        w = 11 + size // 14 // 2 * 2
        img = artkit.add_light(img, bu.star(w, 1.0, (255, 214, 120)),
                               (int(x - t.width / 2 + gx - w / 2), int(y - t.height / 2 + gy - w / 2)))
    return finish(np.array(img)[..., :3].astype(np.float64))


# =========================================================================== jobs, sheet

STRIPS = {"hub": 12, "cockpit": 14, "big": 4, "front": 8}


def _run(job):
    name, fn, args, i, n = job
    return name, i, strip(fn, args, i, n)


def jobs_for(v):
    if v == "a":
        return [("hub", hub_job, ()), ("transport", transport_job, ())]
    if v == "b":
        return ([("cockpit", cockpit_job, ())]
                + [("rock1", rock_job, (64, 501, (-2.2, 2.6, 3.2), 0.4)), ("rock2", rock_job, (34, 502)),
                   ("rock3", rock_job, (14, 503)), ("rock4", rock_job, (22, 504, (-2.2, 2.6, 3.2), 1.0))])
    out = [("big", rock_job, (150, 511, (-3.4, -1.2, 0.9), 0.6, 1.25)),
           ("front", rock_job, (240, 512, (1.8, 1.2, -2.2), 0.2, 1.6))]
    for i, (key, r, seed) in enumerate((("f1", 30, 513), ("f2", 18, 514), ("f3", 12, 515), ("f4", 22, 516),
                                        ("f5", 16, 517), ("f6", 10, 518), ("f7", 14, 519))):
        out.append((key, rock_job, (r, seed, (-3.4, -0.4, 1.0), 0.3 * i)))
    for i, (x, y, size, leader) in enumerate(TALONS):
        out.append((f"t{i}", talon_job, (size, leader, TAL_YAW[i], TAL_ROLL[i])))
    return out


def build(variants):
    jobs = [(name, fn, args, i, STRIPS.get(name, 1)) for v in variants for name, fn, args in jobs_for(v)
            for i in range(STRIPS.get(name, 1))]
    parts = {}
    with ProcessPoolExecutor(max_workers=16) as ex:
        for name, i, img in ex.map(_run, jobs):
            parts.setdefault(name, {})[i] = img
    res = {}
    for name, got in parts.items():
        rows = [np.array(got[i]) for i in sorted(got)]
        res[name] = Image.fromarray(np.concatenate(rows, axis=0), "RGBA")
    res.update({f"t{i}": bu.rim(res[f"t{i}"]) for i in range(len(TALONS)) if f"t{i}" in res})
    stills = {}
    if "a" in variants:
        stills["a"] = still_a(res)
    if "b" in variants:
        stills["b"] = still_b(res)
    if "c" in variants:
        stills["c"] = still_c(res)
    CONCEPT.mkdir(parents=True, exist_ok=True)
    for v, img in stills.items():
        path = CONCEPT / f"act-5-still-{ROUND}-{v}.png"
        img.convert("RGB").save(path, optimize=True)
        print(f"{path.relative_to(acts.ROOT)}: {artkit.colour_count([img])} colours")
    return stills


WHAT = {"a": "A: THE HUB TURNS - CERES HUB ON THE NIGHT-SIDE HORIZON, A BLOCK GOING RED, THE TRANSPORT ON APPROACH",
        "b": "B: INTERCEPTED - VORNE'S BROADCAST AMBER IN A DARK COCKPIT, THE BELT BEYOND THE CANOPY",
        "c": "C: THE GOLD WING - A TALON WING GLEAMING PAST IN THE ROCKS, THE SUN LOW BEHIND A ROCK"}


def sheet():
    stills = {v: Image.open(CONCEPT / f"act-5-still-{ROUND}-{v}.png").convert("RGBA") for v in "abc"}
    hw, hh = W // 2, H // 2
    out = raster.sheet(1000, 40 + 3 * (hh + 30) + 10, "ACT 5 TITLE-CARD STILL (R63)",
                       "F.9 REDO - LEFT: THE STILL, RIGHT: THE TITLE CARD OVER IT")
    y = 38
    for v, img in stills.items():
        raster.draw_text(out, 16, y, WHAT[v], raster.LABEL)
        out.alpha_composite(img.resize((hw, hh), Image.LANCZOS), (16, y + 14))
        out.alpha_composite(acts.title_card(img, NAME).resize((hw, hh), Image.LANCZOS), (16 + hw + 8, y + 14))
        y += hh + 30
    path = CONCEPT / f"act-5-still-{ROUND}-sheet.png"
    out.convert("RGB").save(path, optimize=True)
    print(f"sheet: {path.relative_to(acts.ROOT)}")


if __name__ == "__main__":
    vs = [a for a in sys.argv[1:] if a in ("a", "b", "c")] or ["a", "b", "c"]
    build(vs)
    if all((CONCEPT / f"act-5-still-{ROUND}-{v}.png").exists() for v in "abc"):
        sheet()
