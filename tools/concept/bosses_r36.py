#!/usr/bin/env python3
"""Concept round 36 - the two Act 3 bosses, a/b each (overnight design phase, 2026-10-12).

Revenant Walker (L19 mid-boss, Tharsis foundry): an unmarked human war machine - neutral grey,
no trim, no insignia - with homing missile launchers at the front and a rear-facing flamer that
fires once the ship has got past it. Its weak point (the reactor) sits on its back, so the fight
asks for front guns while it approaches and rear guns once it is below the ship. Each variant
includes the wreck (hint 2): the serial plate is gone, and a close-up render shows how.

  revenant-walker-r36-a.{png,gif}  A "Strider heavy": a reverse-knee biped (8-frame walk cycle),
                                   two shoulder missile boxes (2x3 cells) that die separately,
                                   a tail flamer turret between two fuel tanks, reactor grille
                                   on the back. Wreck: plate ground blank (grinder swirls).
  revenant-walker-r36-b.{png,gif}  B "Siege crawler": a low four-legged hull with a dozer prow,
                                   two vertical-launch blocks (2x4 cells; missiles pop up, then
                                   turn and home), a rear flamer barbette with twin nozzles, and
                                   radiator fins over the reactor. Wreck: plate cut off with a
                                   torch (scorched bare rectangle, sheared rivet stubs).

Dust Colossus (L21 act boss, Hellas basin): a colony-sized Vrell organism under the sand. It
breaches at the edges of the slowly scrolling arena (sides, rear, front in turn); each breach
exposes a mouth (weak point, lime) that fires rings and sucks in debris and the ship. The tell
is a bulge in the sand (with the edge warning 3 s ahead for sides and rear).

  dust-colossus-r36-a.{png,gif}    A "Rearing maw": a segmented worm that arches out of a
                                   cracking sand dome; its head opens three jaw plates around
                                   rings of teeth and a lime throat. Slate chitin, teal seams.
  dust-colossus-r36-b.{png,gif}    B "Sinkhole colony": the organism stays under the sand; the
                                   ground collapses into a funnel, four bone mandibles unfold
                                   from the rim around a lime throat; violet-lit colony ridges
                                   run under the whole basin. Bone chitin, violet veins.

The chosen variants (CHOSEN: walker a, colossus b, round 36's review) are written to
design/enemies/bosses/concept/, the unchosen ones to concept/rejected/.
Each PNG is a sheet (full sprite with callouts, parts, states, a frame from the GIF, notes);
each GIF is the boss in action in the 480x540 play field at 1x with the player for scale.
Models live in this file (the frozen render package is only imported).
Run: python3 tools/concept/bosses_r36.py [walker-a] [walker-b] [colossus-a] [colossus-b]
     (no args = all four, about 10-15 min)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import bosses_r06 as b6  # noqa: E402
import enemies_r03 as e3  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import ui_r11  # noqa: E402
from render import boss_models as bm  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SAC, V_SEAM  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_cylinder_x,  # noqa: E402
                        sd_cylinder_y, sd_cylinder_z, sd_ellipsoid, sd_plate, sd_sphere, union)

FW, FH, FPS, TAU = 480, 540, 20, 2 * np.pi
b6.SUB = "CONCEPT ROUND 36 - 960X540"
OUT = ROOT / "design" / "enemies" / "bosses" / "concept"
MS = rig.ModelSpaceAngleSprites
SHOTS = e3.SHOTS
DOWN = np.pi / 2
ease = b6.ease
times = b6.times

# muted Mars ground ramp (the palette's MARS ramp pulled toward ochre-grey so bullets and the
# grey machines read on it)
MARS = [raster.hexrgb(c) for c in "2a140e 4e2616 74402a 98603c b8845a d6aa80".split()]
SAND = [raster.hexrgb(c) for c in "2e170e 54301c 7c4a2c a26a42 c08c5e dcb488".split()]

em.ROLE_SCHEMES.update({
    "dust-colossus-a": ("slate", "teal", "lime"),
    "dust-colossus-b": ("bone", "violet", "lime"),
})


# =========================================================================== shared helpers

def sstep(a, b, x):
    return ease((x - a) / (b - a))


def with_alpha(sp, k):
    a = sp.copy()
    a.putalpha(a.getchannel("A").point(lambda v: int(v * max(0.0, min(1.0, k)))))
    return a


def scaled(sp, k):
    if abs(k - 1.0) < 1e-3:
        return sp
    return sp.resize((max(1, int(sp.width * k)), max(1, int(sp.height * k))), Image.LANCZOS)


# the user's picks (round 36's review, 2026-10-13): the unchosen variants are written to concept/rejected/
CHOSEN = {"revenant-walker": "a", "dust-colossus": "b"}


def save(slug, variant, sheet, frames):
    folder = OUT if CHOSEN[slug] == variant else OUT / "rejected"
    folder.mkdir(parents=True, exist_ok=True)
    png = folder / f"{slug}-r36-{variant}.png"
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, folder / f"{slug}-r36-{variant}.gif", fps=FPS)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)")


def player_draw(img, x, y, t, front=True, rear=False):
    e3.place(img, e3.player_sprite(), x, y, "air")
    c = raster.hexrgb("40ff80")
    d = ImageDraw.Draw(img)
    for k in range(4):
        if front:
            yy = y - 40 - ((t * 420 + k * 105) % 420)
            if yy > -10:
                e3.bolt(img, x - 10, yy)
                e3.bolt(img, x + 10, yy)
        if rear:          # rear gun: green-white bolts down (player shot palette)
            yy = y + 30 + ((t * 380 + k * 95) % 380)
            if yy < FH + 10:
                d.rectangle([x - 2, yy - 7, x + 2, yy + 7], fill=c + (220,))
                d.rectangle([x - 1, yy - 5, x + 1, yy + 5], fill=(240, 255, 240, 255))


def smoke_puffs(img, x, y, t, seed, rate=10, life=2.0, spread=10, drift=(10, -18), size=(5, 14),
                tone=(70, 64, 62)):
    """Grey smoke rising from a point (wreck, torn pods)."""
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rng = np.random.default_rng(seed)
    offs = rng.uniform(-spread, spread, (64, 2))
    for i in range(int(life * rate)):
        age = (t * rate - i) % (life * rate) / rate
        j = (int(t * rate) - i) % 64
        k = age / life
        px = x + offs[j, 0] + drift[0] * age
        py = y + offs[j, 1] * 0.5 + drift[1] * age
        r = size[0] + (size[1] - size[0]) * k
        a = int(150 * (1 - k))
        c = tuple(int(v * (1.1 - 0.3 * k)) for v in tone)
        d.ellipse([px - r, py - r, px + r, py + r], fill=c + (a,))
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(2)))


def flame_cone(img, x, y, ang, reach, t, width=0.32, seed=4):
    """Flamer burst: a cone of flame puffs from a nozzle at (x, y) along ``ang`` (screen rad)."""
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rng = np.random.default_rng(seed)
    jit = rng.uniform(-1, 1, (90, 2))
    n = 46
    for i in range(n):
        s = ((i / n) + t * 2.4) % 1.0               # puff travels out along the cone
        j = (i + int(t * 40)) % 90
        a = ang + width * jit[j, 0] * s
        r = reach * s
        px, py = x + np.cos(a) * r, y + np.sin(a) * r
        rad = 4 + 18 * s
        stops = [(0.0, (255, 250, 220)), (0.25, (255, 220, 110)), (0.5, (255, 140, 40)),
                 (0.75, (200, 60, 24)), (1.0, (70, 40, 34))]
        c = raster.ramp(stops, np.array([s]))[0]
        alpha = int(235 * (1 - s ** 2.2))
        d.ellipse([px - rad, py - rad, px + rad, py + rad], fill=tuple(int(v) for v in c) + (alpha,))
    lay = lay.filter(ImageFilter.GaussianBlur(1.6))
    img.alpha_composite(lay)
    raster.add_light(img, x, y, 26, (255, 170, 80), 0.9)


def ground_heightfield(w, h, seed, dunes=True):
    """Mars ground height: fBm plus (for the basin) wind-driven dune bands and fine ripples."""
    hgt = raster.fbm(w, h, 56, seed, octaves=6, period=False) * 0.7
    if dunes:
        yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
        warp = raster.fbm(w, h, 90, seed + 1, octaves=3, period=False) * 60
        u = (xx * 0.55 + yy * 0.84 + warp)
        crest = np.abs(((u / 74) % 1.0) - 0.62)
        dune = np.where(((u / 74) % 1.0) < 0.62, ((u / 74) % 1.0) / 0.62, (1 - (u / 74) % 1.0) / 0.38)
        hgt = hgt + 0.55 * dune ** 1.5 + 0.02 * np.sin(u * 0.9 + warp * 0.2) + 0.0 * crest
    return hgt


def hillshade(hgt, strength):
    gy, gx = np.gradient(hgt)
    return np.clip(1.0 + (gx + gy) * -strength, 0.45, 1.6)


# =========================================================================== Revenant Walker

W_HULL, W_DARK, W_LIGHT, W_GLASS, W_SENSOR, W_VENT, W_CELL, W_PLATE, W_PILOT, W_SCORCH, W_RUST = range(11)
GREY = (0.47, 0.49, 0.51)
GREY_D = (0.16, 0.17, 0.19)
GREY_L = (0.68, 0.69, 0.70)
VENT = np.array((1.0, 0.45, 0.12))       # reactor glow: hot amber-orange (the weak point)


def _scorch(seed=0.0):
    def f(p, n):
        v = (np.sin(p[:, 0] * 5.1 + seed) * np.sin(p[:, 1] * 4.3 - seed)
             + 0.5 * np.sin(p[:, 0] * 9 + p[:, 1] * 7 + seed))
        return np.clip(0.55 + 0.35 * v, 0.25, 1.0)
    return f


def _grinder(cx=0.0, cy=0.0):
    """Bare metal ground smooth with an angle grinder: overlapping fine swirl arcs (two offset
    grids of arc centres, so the marks work wherever the plate ends up)."""
    def f(p, n):
        out = np.full(len(p), 1.0)
        for g, ox, oy, ph in ((0.11, 0.0, 0.0, 0.0), (0.13, 0.05, 0.035, 1.9)):
            x, y = p[:, 0] - cx + ox, p[:, 1] - cy + oy
            lx, ly = (x / g) % 1.0 - 0.5, (y / g) % 1.0 - 0.5
            r = np.sqrt(lx * lx + ly * ly) * g
            arc = np.cos(r * 300 + ph)
            band = np.clip(1 - r / (0.6 * g), 0, 1)
            out = out + 0.16 * band * arc
        return np.clip(out, 0.72, 1.3)
    return f


def _torched():
    """Bare metal where a plate was cut away: straight scrape lines across the rectangle."""
    def f(p, n):
        s = np.abs(np.sin(p[:, 0] * 140 + p[:, 1] * 30)) ** 6
        return 1.05 - 0.3 * s
    return f


def walker_mats(vent=1.0, cells=0.0, pilot=0.3, plate="ground", wreck=False, plate_c=(0, 0)):
    hull_pat = sdf.panel_lines(scale=5.5, width=0.05, depth=0.8)
    grey = GREY
    if wreck:
        grey = (0.34, 0.34, 0.35)
        base = _scorch(1.3)
        hull_pat = (lambda p, n, a=hull_pat, b=base: a(p, n) * b(p, n))
    plate_pat = _grinder(*plate_c) if plate == "ground" else _torched()
    return [
        Material(grey, metal=0.55, shininess=70, spec=0.8, pattern=hull_pat),
        Material(GREY_D, metal=0.7, shininess=90, spec=0.9),
        Material(GREY_L, metal=0.6, shininess=110, spec=1.0),
        Material((0.06, 0.07, 0.09), metal=0.9, shininess=160, spec=1.5),
        Material((0.05, 0.02, 0.02), emission=tuple(np.array((1.0, 0.18, 0.12)) * 1.4)),
        Material((0.05, 0.03, 0.02), emission=tuple(VENT * 1.15 * vent),
                 emission_pattern=lambda p, n: 0.3 + 0.7 * (np.sin(p[:, 0] * 60) > 0)),
        Material((0.07, 0.07, 0.08), metal=0.5, shininess=60, spec=0.6,
                 emission=tuple(np.array((1.0, 0.2, 0.14)) * 0.75 * cells),
                 emission_pattern=lambda p, n: 0.35 + 0.65 * np.clip(1.4 - 9 * np.abs(((p[:, 1] * 6.5) % 1.0) - 0.5), 0, 1)),
        Material((0.86, 0.86, 0.84), metal=0.5, shininess=200, spec=1.6, pattern=plate_pat),
        Material((0.08, 0.04, 0.02), emission=tuple(np.array((1.0, 0.6, 0.25)) * 1.4 * pilot)),
        Material((0.09, 0.08, 0.08), metal=0.1, shininess=20, spec=0.2, pattern=_scorch(0.4)),
        Material((0.36, 0.22, 0.14), metal=0.3, shininess=40, spec=0.4, pattern=_scorch(2.0)),
    ]


# ---- variant A: reverse-knee biped

A_PLATE = (-0.24, 0.36)          # serial plate on the front deck (model units)
A_FLAMER = (0.0, -0.78)          # flamer turret pivot
A_PODS = (0.74, 0.06)             # missile box centre (mirrored)
A_REACTOR = (0.0, -0.4)


def walker_a_legs(phase=0.0, pose="walk"):
    def scene(p):
        items = [(sd_box(p, (0, 0, 0.8), (0.4, 0.3, 0.16), 0.06), W_DARK)]
        for side in (1, -1):
            ph = phase + (0.0 if side > 0 else np.pi)
            if pose == "walk":
                fy = 0.6 * np.sin(ph)
                lift = 0.2 * max(0.0, np.cos(ph))
                hip = (side * 0.46, -0.05, 0.8)
                knee = (side * 1.14, -0.6 + 0.3 * fy, 0.62 + lift)
                ankle = (side * 1.02, fy - 0.12, 0.26 + lift)
                toe_z = 0.06 + lift
            else:                                   # wreck: legs folded flat, splayed
                fy = 0.2 * side
                hip = (side * 0.42, 0.0, 0.42)
                knee = (side * 1.0, -0.35 + fy, 0.2)
                ankle = (side * 1.15, 0.35 + fy, 0.12)
                toe_z = 0.06
            items += [(sd_capsule(p, hip, knee, 0.2, 0.15), W_HULL),
                      (sd_capsule(p, knee, ankle, 0.12, 0.09), W_DARK),
                      (sd_capsule(p, (hip[0] * 0.9, hip[1] - 0.12, hip[2] - 0.05),
                                  (ankle[0] * 0.98, ankle[1] - 0.2, ankle[2] + 0.12), 0.045), W_LIGHT),
                      (sd_sphere(p, knee, 0.15), W_LIGHT),
                      (sd_ellipsoid(p, (ankle[0], ankle[1] + 0.05, toe_z + 0.02), (0.18, 0.2, 0.08)), W_HULL)]
            for dx in (-0.16, 0.0, 0.16):
                items.append((sd_capsule(p, (ankle[0] + dx * 0.5, ankle[1] + 0.12, toe_z + 0.03),
                                         (ankle[0] + dx * 1.6, ankle[1] + 0.48, toe_z),
                                         0.07, 0.03), W_DARK if dx else W_HULL))
            items.append((sd_capsule(p, (ankle[0], ankle[1] - 0.05, toe_z + 0.03),
                                     (ankle[0], ankle[1] - 0.3, toe_z), 0.06, 0.03), W_DARK))
        return union(*items, k=0.02)
    return scene


def walker_a_torso(pods="LR", wreck=False, tilt=0.0):
    body = [(0.0, 0.76), (0.44, 0.64), (0.58, 0.12), (0.52, -0.46), (0.3, -0.7), (0.0, -0.72)]

    def scene(p):
        if tilt:
            p = sdf.rotate_y(p, tilt)
        q = mirror_x(p)
        items = [
            (sd_plate(q, body, 1.15, 0.22, round_=0.05), W_HULL),
            (sd_plate(q, [(0.0, 0.5), (0.3, 0.42), (0.36, 0.05), (0.0, 0.0)], 1.38, 0.03, round_=0.01),
             W_LIGHT),
            (sd_box(p, (0, 0.67, 1.3), (0.26, 0.05, 0.05), 0.02), W_GLASS),
            (sd_sphere(q, (0.12, 0.7, 1.33), 0.035), W_SENSOR),
            (sd_box(p, (0, -0.05, 1.42), (0.06, 0.42, 0.04), 0.02), W_DARK),
            # reactor housing and grille (weak point), fuel tanks, flamer ring
            (sd_cylinder_z(p, (*A_REACTOR, 1.36), 0.25, 0.06), W_DARK),
            (sd_cylinder_z(p, (*A_REACTOR, 1.4), 0.19, 0.05), W_VENT if not wreck else W_SCORCH),
            (sd_capsule(q, (0.34, -0.22, 1.36), (0.34, -0.64, 1.36), 0.11), W_LIGHT),
            (sd_box(q, (0.34, -0.43, 1.47), (0.12, 0.02, 0.02), 0.0), W_DARK),
            (sd_cylinder_z(p, (*A_FLAMER, 1.3), 0.18, 0.06), W_DARK),
            # the serial plate (riveted, bare metal) on the front deck
            (sd_box(p, (A_PLATE[0], A_PLATE[1], 1.42), (0.13, 0.065, 0.014), 0.004), W_PLATE),
        ]
        for dx in (-0.115, 0.115):
            for dy in (-0.05, 0.05):
                items.append((sd_sphere(p, (A_PLATE[0] + dx, A_PLATE[1] + dy, 1.432), 0.013), W_LIGHT))
        for side, key in ((1, "R"), (-1, "L")):
            px, py = side * A_PODS[0], A_PODS[1]
            items.append((sd_box(p, (side * 0.56, py, 1.2), (0.1, 0.12, 0.08), 0.03), W_DARK))
            if key in pods:
                items += [(sd_box(p, (px, py, 1.26), (0.24, 0.38, 0.18), 0.04), W_HULL),
                          (sd_box(p, (px, py + 0.42, 1.24), (0.2, 0.05, 0.12), 0.02), W_DARK)]
                for cx in (-0.1, 0.1):
                    for cy in (-0.22, 0.0, 0.22):
                        items.append((sd_box(p, (px + cx, py + cy, 1.43), (0.07, 0.08, 0.02), 0.01),
                                      W_CELL))
            else:                                   # torn stub
                items += [(sd_box(p, (side * 0.74, py - 0.04, 1.18), (0.14, 0.22, 0.1), 0.05), W_SCORCH),
                          (sd_capsule(p, (side * 0.7, py + 0.1, 1.22), (side * 0.9, py + 0.24, 1.3),
                                      0.03), W_DARK)]
        d, m = union(*items, k=0.015)
        if wreck:                                    # blown reactor: a crater in the deck
            cut = sd_sphere(p, (*A_REACTOR, 1.55), 0.32)
            d = np.maximum(d, -cut)
        return d, m
    return scene


def walker_a_flamer(pilot=0.3):
    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_cylinder_z(p, (0, 0, 1.42), 0.16, 0.07), W_HULL),
            (sd_box(p, (0, 0.2, 1.46), (0.07, 0.24, 0.05), 0.02), W_DARK),
            (sd_cylinder_y(p, (0, 0.46, 1.46), 0.06, 0.05), W_LIGHT),
            (sd_sphere(p, (0, 0.52, 1.46), 0.04), W_PILOT),
            (sd_capsule(q, (0.12, -0.06, 1.46), (0.12, 0.16, 1.46), 0.035), W_LIGHT),
            k=0.01)
    return scene


def walker_model(scene_fn, **mk):
    def make(**kw):
        return scene_fn(**kw), walker_mats(**mk)
    return make


# ---- variant B: four-legged siege crawler

B_PLATE = (0.3, 0.52)
B_FLAMER = (0.0, -0.4)
B_VLS = (0.32, 0.08)             # VLS block centre (mirrored)
B_REACTOR = (0.0, -0.83)


def walker_b_legs(phase=0.0, pose="walk"):
    corners = [(0.62, 0.58), (-0.62, 0.58), (0.62, -0.62), (-0.62, -0.62)]
    offs = [0.0, np.pi, np.pi, 0.0]          # diagonal pairs move together

    def scene(p):
        items = []
        for (hx, hy), o in zip(corners, offs):
            sx, sy = np.sign(hx), np.sign(hy)
            ph = phase + o
            if pose == "walk":
                fy = 0.3 * np.sin(ph)
                lift = 0.16 * max(0.0, np.cos(ph))
                knee = (hx + sx * 0.38, hy + sy * 0.28 + 0.5 * fy, 0.95 + lift)
                foot = (hx + sx * 0.72, hy + sy * 0.5 + fy, 0.06 + lift)
            else:
                knee = (hx + sx * 0.5, hy + sy * 0.2, 0.35)
                foot = (hx + sx * 0.95, hy + sy * 0.55, 0.08)
            items += [(sd_sphere(p, (hx, hy, 0.62), 0.17), W_DARK),
                      (sd_capsule(p, (hx, hy, 0.62), knee, 0.15, 0.12), W_HULL),
                      (sd_sphere(p, knee, 0.12), W_LIGHT),
                      (sd_capsule(p, knee, foot, 0.1, 0.06), W_DARK),
                      (sd_capsule(p, (hx + sx * 0.1, hy, 0.66), (knee[0] - sx * 0.04, knee[1], knee[2] - 0.08),
                                  0.04), W_LIGHT),
                      (sd_cylinder_z(p, (foot[0], foot[1], foot[2]), 0.12, 0.04), W_HULL)]
        return union(*items, k=0.02)
    return scene


def walker_b_torso(vls="LR", wreck=False, tilt=0.0):
    hull = [(0.0, 1.05), (0.44, 0.92), (0.66, 0.6), (0.68, -0.72), (0.5, -1.02), (0.0, -1.06)]
    prow = [(0.0, 1.32), (0.5, 1.12), (0.74, 0.86), (0.6, 0.9), (0.3, 1.02), (0.0, 1.1)]

    def scene(p):
        if tilt:
            p = sdf.rotate_y(p, tilt)
        q = mirror_x(p)
        items = [
            (sd_plate(q, hull, 0.86, 0.24, round_=0.06), W_HULL),
            (sd_plate(q, prow, 0.66, 0.1, round_=0.03), W_DARK),
            (sd_box(p, (0, 0.98, 1.06), (0.3, 0.04, 0.04), 0.02), W_GLASS),
            (sd_sphere(q, (0.18, 1.0, 1.08), 0.03), W_SENSOR),
            (sd_box(p, (0, 0.08, 1.12), (0.07, 0.62, 0.04), 0.02), W_DARK),
            # reactor at the rear under radiator fins (weak point), flamer barbette ring
            (sd_box(p, (*B_REACTOR, 1.06), (0.44, 0.19, 0.08), 0.03), W_VENT if not wreck else W_SCORCH),
            (sd_box(p, (*B_FLAMER, 1.08), (0.24, 0.2, 0.04), 0.03), W_DARK),
            # serial plate on the right front deck
            (sd_box(p, (B_PLATE[0], B_PLATE[1], 1.12), (0.13, 0.065, 0.014), 0.004),
             W_PLATE if not wreck else W_SCORCH),
        ]
        for fx in (-0.27, -0.09, 0.09, 0.27):               # radiator fins over the reactor
            items.append((sd_box(p, (fx, B_REACTOR[1], 1.15), (0.018, 0.16, 0.03), 0.0), W_DARK))
        if wreck:      # torched off: bare scraped rectangle and four sheared rivet stubs
            items.append((sd_box(p, (B_PLATE[0], B_PLATE[1], 1.13), (0.1, 0.045, 0.01), 0.0), W_PLATE))
        for dx in (-0.115, 0.115):
            for dy in (-0.05, 0.05):
                items.append((sd_cylinder_z(p, (B_PLATE[0] + dx, B_PLATE[1] + dy, 1.135), 0.013,
                                            0.012 if not wreck else 0.01), W_LIGHT))
        for side, key in ((1, "R"), (-1, "L")):
            vx, vy = side * B_VLS[0], B_VLS[1]
            if key in vls:
                items.append((sd_box(p, (vx, vy, 1.12), (0.2, 0.42, 0.07), 0.03), W_HULL))
                for cx in (-0.09, 0.09):
                    for cy in (-0.3, -0.1, 0.1, 0.3):
                        items.append((sd_box(p, (vx + cx, vy + cy, 1.2), (0.065, 0.075, 0.02), 0.01),
                                      W_CELL))
            else:
                items.append((sd_box(p, (vx, vy, 1.12), (0.2, 0.42, 0.04), 0.04), W_SCORCH))
        d, m = union(*items, k=0.015)
        if wreck:
            d = np.maximum(d, -sd_sphere(p, (*B_REACTOR, 1.2), 0.3))
        return d, m
    return scene


def walker_b_flamer(pilot=0.3):
    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_cylinder_z(p, (0, 0, 1.16), 0.22, 0.07), W_HULL),
            (sd_box(p, (0, -0.1, 1.24), (0.14, 0.1, 0.05), 0.03), W_DARK),
            (sd_capsule(q, (0.08, 0.05, 1.24), (0.08, 0.5, 1.24), 0.05), W_DARK),
            (sd_cylinder_y(q, (0.08, 0.52, 1.24), 0.055, 0.04), W_LIGHT),
            (sd_sphere(q, (0.08, 0.57, 1.24), 0.035), W_PILOT),
            (sd_capsule(p, (0, -0.2, 1.3), (0, 0.15, 1.3), 0.06), W_LIGHT),
            k=0.01)
    return scene


def missile_model():
    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_capsule(p, (0, -0.3, 0), (0, 0.24, 0), 0.11, 0.1), W_LIGHT),
            (sd_sphere(p, (0, 0.3, 0), 0.085), W_SENSOR),
            (sd_plate(q, [(0.0, -0.14), (0.22, -0.32), (0.22, -0.4), (0.0, -0.34)], 0.0, 0.02), W_DARK),
            (sd_sphere(p, (0, -0.36, 0), 0.07), W_PILOT),
            k=0.01)
    return scene, walker_mats(cells=1.0, pilot=1.0)


def wreck_maker(legs_fn, torso_fn, tilt, drop, mats):
    """The wreck: legs folded flat, the torso dropped onto them and tilted, pods gone."""
    def make():
        legs, torso = legs_fn(0.0, "wreck"), torso_fn("", True, tilt)
        return (lambda p: union(legs(p), torso(p + sdf.vec(0, 0, drop)))), mats
    return make


MISSILE = MS(missile_model, 24, 16, extent=0.95, factor=4, colors=20)


class Walker:
    """Sprites and geometry of one walker variant."""

    def __init__(self, v):
        self.v = v
        if v == "a":
            self.S, self.N = 92.0, 230
            self.legs = MS(lambda phase=0.0, pose="walk": (walker_a_legs(phase, pose), walker_mats()),
                           self.N, 16, extent=self.N / self.S, factor=2, colors=40)
            self.torso = MS(lambda pods="LR", cells=0.0, vent=1.0:
                            (walker_a_torso(pods), walker_mats(vent=vent, cells=cells, plate_c=A_PLATE)),
                            self.N, 16, extent=self.N / self.S, factor=2, colors=48)
            self.flamer = MS(lambda pilot=0.3: (walker_a_flamer(), walker_mats(pilot=pilot)),
                             64, 32, extent=64 / self.S, factor=4, colors=24)
            self.parts = (A_PODS, A_FLAMER, A_REACTOR, A_PLATE)
            self.wreck_make = wreck_maker(walker_a_legs, walker_a_torso, 0.1, 0.42,
                                          walker_mats(vent=0.0, wreck=True, plate_c=A_PLATE))
            self.stride = 0.96
        else:
            self.S, self.N = 88.0, 260
            self.legs = MS(lambda phase=0.0, pose="walk": (walker_b_legs(phase, pose), walker_mats()),
                           self.N, 16, extent=self.N / self.S, factor=2, colors=40)
            self.torso = MS(lambda pods="LR", cells=0.0, vent=1.0:
                            (walker_b_torso(pods), walker_mats(vent=vent, cells=cells, plate="torch")),
                            self.N, 16, extent=self.N / self.S, factor=2, colors=48)
            self.flamer = MS(lambda pilot=0.3: (walker_b_flamer(), walker_mats(pilot=pilot)),
                             80, 32, extent=80 / self.S, factor=4, colors=24)
            self.parts = (B_VLS, B_FLAMER, B_REACTOR, B_PLATE)
            self.wreck_make = wreck_maker(walker_b_legs, walker_b_torso, -0.08, 0.5,
                                          walker_mats(vent=0.0, wreck=True, plate="torch"))
            self.stride = 0.6
        self.wreck = MS(self.wreck_make, self.N + 40, 16, extent=(self.N + 40) / self.S,
                        factor=2, colors=48)

    def screen(self, cx, cy, local, h=DOWN):
        ox, oy = rig.local_to_screen(local[0], local[1], h, self.S)
        return cx + ox, cy + oy

    def pod_screen(self, cx, cy, side):
        pods = self.parts[0]
        return self.screen(cx, cy, (side * pods[0], pods[1]))


def walker_ground(h=FH * 2, seed=19):
    """Tharsis foundry yard: dusty regolith, steel deck slabs, rail tracks, a conveyor and
    furnace grates (dim, muted orange per the ground-layer rule)."""
    hgt = ground_heightfield(FW, h, seed, dunes=False)
    t = (hgt - hgt.min()) / np.ptp(hgt)
    stops = [(0.0, MARS[0]), (0.3, MARS[1]), (0.55, MARS[2]), (0.8, MARS[3]), (1.0, MARS[4])]
    rgb = raster.ramp(stops, t) * hillshade(hgt * 30, 1.0)[..., None]
    img = raster.to_rgba_image(np.clip(rgb, 0, 255))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    # deck of steel slabs across the middle of the yard
    for y in range(0, h, 44):
        for x in range(96, 392, 74):
            c = int(rng.uniform(78, 100))
            d.rectangle([x, y, x + 72, y + 42], fill=(c, c - 6, c - 12, 255), outline=(48, 40, 36, 255))
            if rng.random() < 0.3:
                d.line([x + 6, y + 6, x + 66, y + 6], fill=(c + 14, c + 8, c + 2, 255))
            if rng.random() < 0.08:              # furnace grate
                gx, gy = x + 14, y + 10
                d.rectangle([gx, gy, gx + 44, gy + 22], fill=(40, 24, 18, 255))
                for k in range(gx + 3, gx + 44, 6):
                    d.line([k, gy + 3, k, gy + 19], fill=(150, 72, 30, 255), width=2)
    # rail tracks either side and a conveyor at the far left
    for rx in (58, 424):
        for y in range(0, h, 9):
            d.rectangle([rx - 12, y, rx + 12, y + 3], fill=(52, 40, 34, 255))
        d.line([rx - 8, 0, rx - 8, h], fill=(150, 146, 140, 255), width=2)
        d.line([rx + 8, 0, rx + 8, h], fill=(150, 146, 140, 255), width=2)
    d.rectangle([8, 0, 34, h], fill=(34, 32, 34, 255))
    for y in range(0, h, 6):
        d.line([10, y, 32, y], fill=(58, 56, 58, 255))
    d.line([8, 0, 8, h], fill=(120, 116, 110, 255), width=2)
    d.line([34, 0, 34, h], fill=(120, 116, 110, 255), width=2)
    arr = np.array(img).astype(np.float64)
    dust = raster.fbm(FW, h, 30, seed + 5, octaves=4, period=False)
    k = np.clip((dust - 0.45) * 2.2, 0, 0.6)[..., None]
    arr[..., :3] = arr[..., :3] * (1 - k) + rgb * k
    img = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")
    return b6.terrain.recede(img, amount=0.15, darken=0.8)


def homing_missiles(launches, player, T):
    """Simulate homing missiles: launches = [(t0, x, y, heading, popup)]; returns a list of
    tracks [(t, x, y, h, k)] per missile (k = draw scale for the pop-up)."""
    tracks = []
    dt = 1 / FPS
    for t0, x, y, h, popup in launches:
        tr = []
        t, sp, life = t0, 60.0, 3.2
        while t < min(T, t0 + life):
            px, py = player(t)
            age = t - t0
            k = 1.0
            if popup and age < 0.35:
                k = 0.45 + 0.55 * age / 0.35
                sp = 30
            else:
                want = np.arctan2(py - y, px - x)
                turn = 2.4 if age < 2.0 else 0.6
                h = h + float(np.clip(b6.angdiff(want, h), -turn * dt, turn * dt))
                sp = min(185.0, sp + 260 * dt)
            x, y = x + np.cos(h) * sp * dt, y + np.sin(h) * sp * dt
            tr.append((t, x, y, h, k))
            if np.hypot(px - x, py - y) < 16 or not (-30 < x < FW + 30 and -30 < y < FH + 30):
                break
            t += dt
        tracks.append(tr)
    return tracks


def draw_missiles(img, tracks, t, booms_done):
    for i, tr in enumerate(tracks):
        if not tr or t < tr[0][0]:
            continue
        idx = int(round((t - tr[0][0]) * FPS))
        if idx >= len(tr):
            continue
        # smoke trail
        lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(lay)
        for j in range(max(0, idx - 14), idx):
            _, x, y, _, _ = tr[j]
            a = (j - idx + 14) / 14
            r = 2 + 4 * (1 - a)
            d.ellipse([x - r, y - r, x + r, y + r], fill=(150, 146, 150, int(120 * a)))
        img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(1.2)))
        _, x, y, h, k = tr[idx]
        sp = MISSILE.get(h)
        e3.place(img, scaled(sp, k), x, y, "air")
        raster.add_light(img, x - np.cos(h) * 9, y - np.sin(h) * 9, 7, (255, 170, 80), 0.8)


def walker_fight(v):
    W = Walker(v)
    T = 12.0
    rng = np.random.default_rng(7 if v == "a" else 8)
    bg = walker_ground()
    BH = bg.height
    # walker centre path, kill times, flamer bursts
    X0 = 175

    def wpos(t):
        if t < 2.6:
            y = -150 + (190 + 150) * ease(t / 2.6)
        elif t < 3.6:
            y = 190
        elif t < 6.6:
            y = 190 + (400 - 190) * ease((t - 3.6) / 3.0)
        else:
            y = 400
        return X0 + 8 * np.sin(t * 0.6), y

    def walking(t):
        return t < 2.6 or 3.6 <= t < 6.6

    def player(t):
        if t < 3.4:
            return 360 + 40 * np.sin(t * 1.6), 470
        if t < 5.6:                                  # up the right side, clear of the walker
            s = ease((t - 3.4) / 2.2)
            return 380 + 22 * s + 40 * np.sin(3.4 * 1.6) * (1 - s), 470 + (220 - 470) * s
        if t < 6.2:
            s = ease((t - 5.6) / 0.6)
            return 402 + (250 - 402) * s, 220 + (205 - 220) * s
        if t < 9.6:
            return 250 + 70 * np.sin((t - 6.2) * 1.3), 205 + 10 * np.sin(t * 2.1)
        s = ease((t - 9.6) / 2.0)
        x0 = 250 + 70 * np.sin(3.4 * 1.3)
        return x0 + (380 - x0) * s, 205 + 220 * s

    pod_die = {1: 3.2, -1: 4.5}
    reactor_die = 9.2
    flame = [(5.7, 7.0), (7.6, 8.9)]
    # the scroll holds during the fight (boss lock) and eases in slowly past the wreck (M15 a)
    scroll_speed = lambda t: 0.0 if t < reactor_die + 0.6 else 16 * ease((t - reactor_die - 0.6) / 1.2)

    # scroll offset (integrated)
    offs = [0.0]
    for _ in times(T):
        offs.append(offs[-1] + scroll_speed(_) / FPS)

    def scroll(t):
        i = int(round(t * FPS))
        return offs[min(len(offs) - 1, i - i % 4)]          # ground steps at 5 Hz (GIF size)

    # walk phase integrated from the body's speed over the ground
    phases, ph = [], 0.0
    prev = wpos(0)[1]
    for t in times(T):
        y = wpos(t)[1]
        ph += TAU * abs(y - prev) / (2 * W.stride * W.S) if walking(t) else 0.0
        prev = y
        phases.append(ph)

    # missile salvos: pods open cells 0.4 s before
    salvos = [1.5, 2.9, 4.1] if v == "a" else [1.6, 3.0, 4.2]
    launches = []
    for ts in salvos:
        cx, cy = wpos(ts)
        for side in (1, -1):
            if ts >= pod_die[side]:
                continue
            px, py = W.pod_screen(cx, cy, side)
            for k in range(3):
                if v == "a":
                    launches.append((ts + 0.12 * k, px + side * 6, py + 10 * (k - 1),
                                     DOWN - side * (0.9 + 0.25 * k), False))
                else:
                    launches.append((ts + 0.15 * k, px, py + 22 * (k - 1), DOWN - side * 1.3, True))
    tracks = homing_missiles(launches, player, T)
    booms = b6.Booms()
    for tr in tracks:
        if tr:
            _, x, y, _, _ = tr[-1]
            booms.add(tr[-1][0], x, y, 26, seed=int(x) % 17)
    for side, td in pod_die.items():
        cx, cy = wpos(td)
        x, y = W.pod_screen(cx, cy, side)
        booms.add(td, x, y, 70, seed=60 + side)
    cx, cy = wpos(reactor_die)
    rx, ry = W.screen(cx, cy, W.parts[2])
    for k, (dx, dy, tt) in enumerate(((0, 0, 0), (-50, 30, 0.25), (46, -20, 0.4), (0, 40, 0.6), (-20, -40, 0.75))):
        booms.add(reactor_die + tt, rx + dx, ry + dy, 96 if k == 0 else 66, seed=80 + k, ring=k == 0)

    footprints = []                     # (ground x, ground y)
    planted = [False, False]
    frames = []
    for i, t in enumerate(times(T)):
        y0 = BH - FH - scroll(t)
        f = bg.crop((0, int(y0), FW, int(y0) + FH))
        d = ImageDraw.Draw(f)
        cx, cy = wpos(t)
        ph = phases[i]
        k8 = int(round(ph / (TAU / 8))) % 8
        phase = k8 * TAU / 8
        alive = t < reactor_die + 0.3
        # footprints when a foot plants
        if alive and walking(t):
            feet = ((1, 0.0), (-1, np.pi)) if v == "a" else ((1, 0.0), (-1, np.pi))
            for j, (side, o) in enumerate(feet):
                st = np.cos(phase + o) < 0
                if st and not planted[j]:
                    fx = cx + (-side) * (0.74 if v == "a" else 1.3) * W.S
                    fy = cy - (0.48 * np.sin(phase + o) + (0.15 if v == "a" else 0.2)) * W.S
                    footprints.append((fx, fy + y0))
                planted[j] = st
        for fx, gy in footprints:
            sy = gy - y0
            if -20 < sy < FH + 20:
                d.ellipse([fx - 11, sy - 14, fx + 11, sy + 14], fill=(40, 22, 16, 255))
                d.ellipse([fx - 9, sy - 12, fx + 9, sy + 8], fill=(66, 38, 24, 255))
        if alive:
            pods = "".join(k for s, k in ((1, "R"), (-1, "L")) if t < pod_die[s])
            cells = 1.0 if any(ts - 0.4 <= t < ts + 0.45 for ts in salvos) else 0.0
            legs = W.legs.get(DOWN, phase=phase)
            torso = W.torso.get(DOWN, pods=pods, cells=cells, vent=1.0)
            e3.place(f, legs, cx, cy, "ground")
            sh = sprite.shadow_of(torso, opacity=0.5, blur=1.4)
            sprite.paste_center(f, sh, cx + 16, cy + 22)
            sprite.paste_center(f, torso, cx, cy)
            # flamer: swings toward the ship once it is behind (above) the walker
            fx, fy = W.screen(cx, cy, W.parts[1])
            px, py = player(t)
            behind = py < cy - 40
            aim = np.arctan2(py - fy, px - fx) if behind else -np.pi / 2
            aim = -np.pi / 2 + float(np.clip(b6.angdiff(aim, -np.pi / 2), -1.1, 1.1))
            firing = any(a <= t < b for a, b in flame)
            tele = any(a - 0.5 <= t < a for a, b in flame)
            pilot = 1.0 if (firing or (tele and int(t * 12) % 2 == 0)) else 0.3
            fl = W.flamer.get(aim, pilot=pilot)
            sprite.paste_center(f, sprite.shadow_of(fl, 0.5, 1.2), fx + 16, fy + 22)
            sprite.paste_center(f, fl, fx, fy)
            for side in (1, -1):
                if pod_die[side] <= t < pod_die[side] + 2.5:
                    x, y = W.pod_screen(cx, cy, side)
                    smoke_puffs(f, x, y, t, seed=3 + side, rate=8, life=1.4, size=(4, 10))
            if firing:
                nl = 0.62 if v == "a" else 0.6
                nx, ny = fx + np.cos(aim) * nl * W.S, fy + np.sin(aim) * nl * W.S
                flame_cone(f, nx, ny, aim, 190, t, seed=5)
            if t >= reactor_die - 0.3:
                raster.add_light(f, *W.screen(cx, cy, W.parts[2]), 30, (255, 200, 120), 1.2)
        if t >= reactor_die + 0.3:
            cy = cy + scroll(t) - scroll(reactor_die)
            rx, ry = W.screen(cx, cy, W.parts[2])
            wr = W.wreck.get(DOWN + 0.39)
            e3.place(f, wr, cx, cy - 6, "ground")
            smoke_puffs(f, rx, ry, t, seed=11, rate=10, life=2.4, spread=14, size=(6, 18))
            # the bare plate catches the light once as the wreck scrolls by
            plx, ply = W.screen(cx, cy - 6, W.parts[3], DOWN + 0.39)
            g = np.exp(-((t - 10.6) / 0.25) ** 2)
            if g > 0.05:
                raster.add_light(f, plx, ply, 10, (255, 255, 240), 1.4 * g)
                dd = ImageDraw.Draw(f)
                L = 10 * g
                dd.line([plx - L, ply, plx + L, ply], fill=(255, 255, 240, int(255 * g)))
                dd.line([plx, ply - L, plx, ply + L], fill=(255, 255, 240, int(255 * g)))
        draw_missiles(f, tracks, t, booms)
        booms.draw(f, t)
        px, py = player(t)
        player_draw(f, px, py, t, front=t < 9.6 and py > cy, rear=py < cy - 40 and t < reactor_die + 0.2)
        frames.append(f)
        # ground moves with the scroll; the walker centre is in screen space
    return W, frames, wpos, salvos, flame, pod_die, reactor_die


def plate_closeup(W, size=(320, 160), extent=0.62):
    """High-resolution render of the wreck around the serial plate (for the sheet; the L20
    briefing image is drawn from the same idea)."""
    scene, mats = W.wreck_make()
    plate = W.parts[3]
    drop, tilt = (0.42, 0.1) if W.v == "a" else (0.5, -0.08)
    # where the plate ends up on the dropped, tilted torso: the scene evaluates the torso at
    # rotate_y(p + drop, tilt), so invert that for the plate's model position
    z = 1.42 if W.v == "a" else 1.135
    q = sdf.rotate_y(np.array([[plate[0], plate[1], z]]), -tilt)[0]
    # locate the plate material in a small probe render and centre on it
    probe = [Material((0, 0, 0), emission=(1, 1, 1) if i == W_PLATE else (0, 0, 0)) for i in range(len(mats))]
    pr = sdf.render(scene, probe, (120, 120), 1.2, center=(q[0], q[1]), shadows=False)
    ys, xs = np.nonzero(pr[..., 0] > 0.5)
    if len(xs):
        q = (q[0] + (xs.mean() + 0.5 - 60) * 0.01, q[1] - (ys.mean() + 0.5 - 60) * 0.01)
    hi = sdf.render(scene, mats, (size[0] * 2, size[1] * 2), extent, center=(q[0], q[1]))
    return sprite.make_sprite(hi, 2, 64, crisp=60)


def walker_sheet(v, W, frames, salvos, pod_die, reactor_die):
    S = W.S
    N = W.N
    full = Image.new("RGBA", (N + 40, N + 40), (0, 0, 0, 0))
    c = (N + 40) / 2
    sprite.paste_center(full, W.legs.get(DOWN, phase=0.0), c, c)
    sprite.paste_center(full, W.torso.get(DOWN, pods="LR", cells=1.0), c, c)
    fx, fy = W.screen(c, c, W.parts[1])
    sprite.paste_center(full, W.flamer.get(-np.pi / 2, pilot=1.0), fx, fy)
    pt = lambda loc: tuple(int(v_) for v_ in W.screen(c, c, loc))
    pods, flm, rea, plate = W.parts
    if v == "a":
        callouts = [
            (pt((pods[0], pods[1])), "MISSILE BOX (2X3 CELLS), ONE PER SHOULDER; CELLS LIGHT WHEN A SALVO IS READY; EACH DIES SEPARATELY"),
            (pt(flm), "TAIL FLAMER TURRET (32 HEADINGS, +-60 DEG ABOUT THE REAR); PILOT LIGHT BLINKS 0.5 S BEFORE A BURST"),
            (pt(rea), "REACTOR GRILLE = WEAK POINT (HOT AMBER), ON THE BACK: ONLY REAR GUNS REACH IT ONCE YOU ARE PAST"),
            (pt((0.34, -0.43)), "FUEL TANKS EITHER SIDE OF THE FLAMER"),
            (pt(plate), "SERIAL PLATE (BARE, RIVETED); HINT 2 ON THE WRECK"),
            (pt((0.0, 0.67)), "SENSOR VISOR, TWO RED SENSOR LIGHTS; NO COCKPIT, NO INSIGNIA, NO TRIM"),
            (pt((0.84, -0.6)), "REVERSE-KNEE LEGS, THREE-TOED FEET (8-FRAME WALK)"),
        ]
    else:
        callouts = [
            (pt((pods[0], pods[1])), "VERTICAL-LAUNCH BLOCK (2X4 CELLS) EACH SIDE OF THE SPINE; MISSILES POP UP, THEN TURN AND HOME"),
            (pt(flm), "REAR FLAMER BARBETTE, TWIN NOZZLES (32 HEADINGS); PILOT LIGHTS BLINK 0.5 S BEFORE A BURST"),
            (pt(rea), "REACTOR UNDER RADIATOR FINS = WEAK POINT (HOT AMBER), AT THE TAIL"),
            (pt(plate), "SERIAL PLATE (BARE, RIVETED); HINT 2 ON THE WRECK"),
            (pt((0.0, 1.2)), "DOZER PROW AND SENSOR SLIT; NO COCKPIT, NO INSIGNIA, NO TRIM"),
            (pt((1.2, -1.0)), "FOUR SPIDER LEGS, DIAGONAL PAIRS STEP TOGETHER (8 FRAMES)"),
        ]
    # walk cycle
    legs = r5.grid([b6.cropped(W.legs.get(DOWN, phase=k * TAU / 8)) for k in range(8)], 8, 1,
                   labels=[f"{k + 1}/8" for k in range(8)])
    # states
    states = []
    for lab, pods_, cells, firing in (("SALVO READY", "LR", 1.0, False), ("ONE BOX DOWN", "L", 0.0, False),
                                      ("BOXES DOWN, FLAMER", "", 0.0, True)):
        im = Image.new("RGBA", (N + 20, N + 60), (40, 26, 20, 255))
        cc = ((N + 20) / 2, (N + 60) / 2 + 20)
        sprite.paste_center(im, W.legs.get(DOWN, phase=TAU / 8 * 2), *cc)
        sprite.paste_center(im, W.torso.get(DOWN, pods=pods_, cells=cells), *cc)
        fx, fy = W.screen(*cc, W.parts[1])
        aim = -np.pi / 2 - 0.4
        sprite.paste_center(im, W.flamer.get(aim, pilot=1.0 if firing else 0.3), fx, fy)
        if firing:
            nl = 0.62 if v == "a" else 0.6
            flame_cone(im, fx + np.cos(aim) * nl * S, fy + np.sin(aim) * nl * S, aim, 120, 0.3)
        states.append((lab, im))
    wr = W.wreck.get(DOWN + 0.39)
    im = Image.new("RGBA", (wr.width + 10, N + 60), (40, 26, 20, 255))
    sprite.paste_center(im, wr, im.width / 2, im.height / 2)
    states.append(("WRECK", im))
    st = Image.new("RGBA", (sum(s_.width + 8 for _, s_ in states), N + 74), raster.SHEET_BG)
    x = 0
    for lab, s_ in states:
        raster.draw_text(st, x, 0, lab, raster.LABEL)
        st.alpha_composite(s_, (x, 12))
        x += s_.width + 8
    flam = r5.grid([W.flamer.get(-np.pi / 2 + k * TAU / 32, pilot=0.3) for k in (-6, -4, -2, 0, 2, 4, 6)]
                   + [MISSILE.get(k * TAU / 8) for k in range(4)], 11, 2,
                   labels=["-68", "-45", "-23", "REAR", "+23", "+45", "+68", "MISSILE", "", "", ""])
    close = plate_closeup(W)
    close = sprite.enlarge(close, 1)
    how = "GROUND BLANK WITH AN ANGLE GRINDER" if v == "a" else "CUT OFF WITH A TORCH, RIVETS SHEARED"
    panels = [("WALK CYCLE, 8 FRAMES AT 1X (16 FACINGS IN THE GAME)", legs),
              ("STATES (1X)", st),
              ("FLAMER 7 OF 32 HEADINGS, MISSILE 4 OF 16 (2X)", flam),
              (f"WRECK: SERIAL PLATE {how} (CLOSE RENDER, ~5X)", close)]
    if v == "a":
        notes = ["REVENANT WALKER A - 'STRIDER HEAVY' - ACT 3 MID-BOSS, L19 THARSIS FOUNDRY (UNMARKED: NEUTRAL GREY, NO TRIM)",
                 "A REVERSE-KNEE BIPED ABOUT 230 PX ACROSS, STRIDING DOWN THE FOUNDRY YARD (FEET PLANT, FOOTPRINTS STAY ON THE GROUND",
                 "LAYER). WHILE IT IS AHEAD OF THE SHIP ITS TWO SHOULDER BOXES FIRE SALVOS OF HOMING MISSILES (RED NOSE LIGHT, SMOKE",
                 "TRAIL) - FRONT GUNS KILL THE BOXES. ONCE IT HAS WALKED PAST, THE TAIL FLAMER SWINGS UP AT THE SHIP AND BURNS A CONE",
                 "(0.5 S PILOT-LIGHT TELL); THE REACTOR GRILLE ON ITS BACK IS THE WEAK POINT FOR REAR GUNS. IT DIES IN A CHAIN OF",
                 "BLASTS AND FOLDS ONTO ITS LEGS; THE SCROLL EASES PAST THE WRECK AND THE BLANK PLATE CATCHES THE LIGHT (HINT 2)."]
    else:
        notes = ["REVENANT WALKER B - 'SIEGE CRAWLER' - ACT 3 MID-BOSS, L19 THARSIS FOUNDRY (UNMARKED: NEUTRAL GREY, NO TRIM)",
                 "A LOW FOUR-LEGGED HULL ABOUT 260 PX ACROSS WITH A DOZER PROW, CRAWLING DOWN THE YARD (DIAGONAL LEG PAIRS, FOOTPRINTS).",
                 "TWO VERTICAL-LAUNCH BLOCKS POP MISSILES STRAIGHT UP (DRAWN GROWING) THAT THEN TURN AND HOME - FRONT GUNS KILL",
                 "THE BLOCKS. ONCE PAST, THE TWIN-NOZZLE BARBETTE ON THE REAR DECK TURNS ON THE SHIP AND BURNS A CONE (0.5 S TELL);",
                 "THE REACTOR UNDER THE RADIATOR FINS AT THE TAIL IS THE WEAK POINT FOR REAR GUNS. THE WRECK SLUMPS FLAT; WHERE THE",
                 "SERIAL PLATE WAS IS A SCORCHED BARE RECTANGLE WITH FOUR SHEARED RIVETS - SOMEONE CUT IT OFF ON PURPOSE (HINT 2)."]
    title = "REVENANT WALKER " + v.upper() + " - ACT 3 MID-BOSS (UNMARKED)"
    return b6.boss_sheet(title, full, callouts, panels, frames[int(7.9 * FPS)], notes,
                         f"FULL SPRITE 1X ({N + 40}X{N + 40}), PARTS ATTACHED")


def walker(v):
    W, frames, wpos, salvos, flame, pod_die, reactor_die = walker_fight(v)
    sheet = walker_sheet(v, W, frames, salvos, pod_die, reactor_die)
    return sheet, frames


# =========================================================================== Dust Colossus

C_S = 70.0                       # px per model unit (both variants)
_PRE = None


def _pre_job(i):
    ms, k, kw = _PRE[i]
    return ms.frame(k, **kw)


def prerender(jobs, procs=8):
    """Render many (sprite set, heading, kwargs) frames in parallel (fork) into the caches."""
    global _PRE
    import multiprocessing as mp
    todo = []
    for ms, h, kw in jobs:
        k = ms.index(h)
        key = (k, tuple(sorted((a, round(float(b), 4) if not isinstance(b, str) else b)
                               for a, b in kw.items())))
        if key not in ms.cache and all(key != kk for _, _, _, kk in todo):
            todo.append((ms, k, kw, key))
    if not todo:
        return
    _PRE = [(ms, k, kw) for ms, k, kw, _ in todo]
    with mp.get_context("fork").Pool(min(procs, len(_PRE))) as pool:
        res = pool.map(_pre_job, range(len(_PRE)))
    for (ms, k, kw, key), sp in zip(todo, res):
        ms.cache[key] = sp


def _frame3(T):
    """Orthonormal frame (x, y, z) for a mouth whose axis is T (in the model's YZ plane)."""
    ez = np.asarray(T, dtype=np.float64) / np.linalg.norm(T)
    ex = np.array([1.0, 0.0, 0.0])
    ey = np.cross(ez, ex)
    return ex, ey, ez


def _to_local(p, origin, frame):
    q = p - np.asarray(origin, dtype=np.float64)
    ex, ey, ez = frame
    return np.stack([q @ ex, q @ ey, q @ ez], axis=-1)


def _polar(l, n, offset=0.0):
    """Fold local points into one of n sectors around the local z axis (sector axis = +x)."""
    a = np.arctan2(l[:, 1], l[:, 0]) - offset
    step = TAU / n
    a = a - np.round(a / step) * step
    r = np.sqrt(l[:, 0] ** 2 + l[:, 1] ** 2)
    out = l.copy()
    out[:, 0], out[:, 1] = r * np.cos(a), r * np.sin(a)
    return out


def _teeth(l, n, radius, depth, length, rad, mat, offset=0.0):
    q = _polar(l, n, offset)
    return sd_capsule(q, (radius, 0, depth), (radius - length, 0, depth - length * 0.25), rad, rad * 0.2), mat


def _mouth_items(l, open_, petals=3):
    """Throat (weak point), two rows of teeth and the jaw petals, in mouth-local coordinates."""
    items = [
        (np.sqrt(np.maximum((np.sqrt(l[:, 0] ** 2 + l[:, 1] ** 2) - 0.5) ** 2 + l[:, 2] ** 2, 0)) - 0.13, V_DARK),
        (sd_cylinder_z(l, (0, 0, -0.16), 0.44, 0.05), V_EYE),
        _teeth(l, 11, 0.44, 0.02, 0.2, 0.06, V_BONE),
        _teeth(l, 11, 0.3, -0.06, 0.14, 0.045, V_BONE, offset=TAU / 22),
    ]
    q = _polar(l, petals, offset=np.pi / 2)
    ang = np.radians(118 + (30 - 118) * open_)
    for w, ln, r0 in ((0.0, 0.9, 0.24), (0.17, 0.78, 0.17), (-0.17, 0.78, 0.17)):
        hinge = np.array([0.5, w, 0.0])
        tip = hinge + ln * np.array([np.cos(ang), w * 0.4, np.sin(ang)])
        items.append((sd_capsule(q, hinge, tip, r0, 0.05), V_BODY if w == 0 else V_SEAM))
    tip = np.array([0.5, 0, 0]) + 0.98 * np.array([np.cos(ang), 0, np.sin(ang)])
    items.append((sd_capsule(q, tip - 0.12 * np.array([np.cos(ang), 0, np.sin(ang)]), tip, 0.07, 0.02), V_BONE))
    return items


# ---- variant A: the rearing maw (a segmented worm arching out of the sand)

A_CTRL = np.array([(-2.0, -0.7), (-1.0, 0.25), (0.08, 1.33), (0.24, 1.92)])   # (y, z) Bezier
A_NSEG = 13


def _bezier(u):
    P = A_CTRL
    a = (1 - u) ** 3
    b = 3 * (1 - u) ** 2 * u
    c = 3 * (1 - u) * u ** 2
    d = u ** 3
    return a * P[0] + b * P[1] + c * P[2] + d * P[3]


def _bezier_d(u):
    P = A_CTRL
    return (3 * (1 - u) ** 2 * (P[1] - P[0]) + 6 * (1 - u) * u * (P[2] - P[1]) + 3 * u ** 2 * (P[3] - P[2]))


def worm_rise_shift(rise):
    return (rise - 1.0) * 2.3


def worm_exit(rise):
    """Model y where the body leaves the sand (z = 0) at this rise."""
    sh = worm_rise_shift(rise)
    us = np.linspace(0, 1, 400)
    z = np.array([_bezier(u)[1] for u in us]) + sh
    k = int(np.argmax(z > 0))
    return float(_bezier(us[k])[0])


def worm_mouth_y(rise=1.0):
    return float(_bezier(1.0)[0])


def colossus_a(rise=1.0, open_=0.0, glow=1.0):
    sh = worm_rise_shift(rise)
    us = np.linspace(0, 1, A_NSEG)
    pts = [np.array([0.0, *_bezier(u)]) + np.array([0, 0, sh]) for u in us]
    radii = [0.42 + 0.2 * u for u in us]
    E = pts[-1]
    T = np.array([0.0, *_bezier_d(1.0)])
    fr = _frame3(T)
    mouth_c = E + fr[2] * 0.12

    def scene(p):
        items = []
        for i in range(A_NSEG - 1):
            a, b = pts[i], pts[i + 1]
            items.append((sd_capsule(p, a, b, radii[i] * (1.0 if i % 2 == 0 else 0.93),
                                     radii[i + 1] * (1.0 if i % 2 == 0 else 0.93)),
                          V_BODY if i % 2 == 0 else V_DARK))
            t = b - a
            nrm = np.array([0.0, -t[2], t[1]])
            nrm /= np.linalg.norm(nrm) + 1e-9
            if nrm[2] < 0:
                nrm = -nrm
            base = (a + b) / 2 + nrm * radii[i] * 0.8
            items.append((sd_capsule(p, base, base + nrm * 0.24 - t * 0.25, 0.08, 0.02), V_BONE))
            for sx in (1, -1):
                sb = (a + b) / 2 + np.array([sx * radii[i] * 0.85, 0, 0]) + nrm * 0.15
                items.append((sd_sphere(p, sb, 0.06), V_GLOW))
        items.append((sd_sphere(p, E - fr[2] * 0.3, 0.64), V_BODY))
        d, m = union(*items, k=0.04)
        lm = _to_local(p, mouth_c, fr)
        cav = sd_capsule(lm, (0, 0, -0.1), (0, 0, 1.2), 0.44)        # the mouth cavity
        d = np.maximum(d, -cav)
        d, m = union((d, m), *_mouth_items(lm, open_), k=0.02)
        return np.maximum(d, -p[:, 2]), m           # nothing below the sand
    return scene, bm.mats("dust-colossus-a", glow, weak_k=0.35 + 0.4 * open_)


# ---- variant B: the sinkhole colony (four mandibles round a throat in a sand funnel)

def colossus_b(rise=1.0, open_=0.0, glow=1.0):
    sh = (rise - 1.0) * 0.9
    o = open_
    closed = np.array([(0.78, 0.0, 0.28), (0.62, 0.14, 0.62), (0.32, 0.24, 0.66), (0.12, 0.12, 0.5)])
    opened = np.array([(0.78, 0.0, 0.2), (1.18, 0.06, 0.34), (1.55, 0.24, 0.28), (1.72, 0.5, 0.14)])
    chain = closed * (1 - o) + opened * o
    radii = (0.19, 0.15, 0.1, 0.04)

    def scene(p):
        p = p - np.array([0, 0, sh])
        l = p.copy()
        items = [
            (sd_ellipsoid(p, (0, 0, -0.55), (0.95, 0.95, 0.5)), V_DARK),
            (np.sqrt((np.sqrt(p[:, 0] ** 2 + p[:, 1] ** 2) - 0.6) ** 2 + (p[:, 2] - 0.1) ** 2) - 0.2, V_BODY),
            (sd_cylinder_z(p, (0, 0, 0.02), 0.48, 0.05), V_EYE),
            _teeth(l, 12, 0.46, 0.14, 0.2, 0.06, V_BONE),
            _teeth(l, 12, 0.32, 0.06, 0.14, 0.045, V_BONE, offset=TAU / 24),
        ]
        q = _polar(l, 4, offset=np.pi / 4)
        items.append((sd_sphere(q, (0.8, 0, 0.2), 0.22), V_SEAM))
        for i in range(3):
            items.append((sd_capsule(q, chain[i], chain[i + 1], radii[i], radii[i + 1]), V_BONE))
        q8 = _polar(l, 8, offset=0.0)
        items.append((sd_capsule(q8, (0.66, 0, 0.18), (0.95 + 0.25 * o, 0.0, 0.42 - 0.1 * o), 0.06, 0.03), V_BODY))
        items.append((sd_sphere(q8, (0.97 + 0.25 * o, 0.0, 0.43 - 0.1 * o), 0.055), V_GLOW))
        d, m = union(*items, k=0.04)
        return np.maximum(d, -(p[:, 2] + sh)), m
    return scene, bm.mats("dust-colossus-b", glow, weak_k=0.35 + 0.4 * o)


A_SPRITES = MS(lambda rise=1.0, open_=0.0: colossus_a(rise, open_), 300, 4, extent=300 / C_S,
               factor=2, colors=56)
B_SPRITES = MS(lambda rise=1.0, open_=0.0: colossus_b(rise, open_), 300, 4, extent=300 / C_S,
               factor=2, colors=56)


def rock_sprites():
    out = []
    for i in range(5):
        rng = np.random.default_rng(40 + i)
        blobs = [(rng.uniform(-0.2, 0.2), rng.uniform(-0.2, 0.2), rng.uniform(0.18, 0.32)) for _ in range(4)]

        def make(blobs=blobs):
            def scene(p):
                return union(*[(sd_sphere(p, (x, y, 0.0), r), 0) for x, y, r in blobs], k=0.12)
            return scene, [Material(tuple(c / 255 for c in SAND[2]), metal=0.05, shininess=20, spec=0.3,
                                    pattern=lambda p, n: 0.8 + 0.2 * np.sin(p[:, 0] * 30) * np.sin(p[:, 1] * 27))]
        size = int(10 + 3 * i)
        out.append(b6.static_sprite(make, size, 1.3, factor=4, colors=16))
    return out


def basin_ground(seed=23, h=FH + 140):
    hgt = ground_heightfield(FW, h, seed, dunes=True)
    rng = np.random.default_rng(seed)
    t = (hgt - hgt.min()) / np.ptp(hgt)
    stops = [(0.0, SAND[1]), (0.35, SAND[2]), (0.65, SAND[3]), (0.9, SAND[4]), (1.0, SAND[5])]
    alb = raster.ramp(stops, t)
    alb *= (0.94 + 0.08 * raster.fbm(FW, h, 40, seed + 4, octaves=2, period=False))[..., None]
    img = Image.fromarray(np.clip(alb, 0, 255).astype(np.uint8), "RGB")
    d = ImageDraw.Draw(img)
    for _ in range(70):                       # scattered dark rocks
        x, y, r = rng.uniform(0, FW), rng.uniform(0, h), rng.uniform(1.5, 4.5)
        d.ellipse([x - r, y - r * 0.8, x + r, y + r * 0.8], fill=tuple(int(c * 0.55) for c in SAND[2]))
        d.point((x - r * 0.4, y - r * 0.4), fill=tuple(SAND[4]))
    return np.array(img).astype(np.float64), hgt


class Breach:
    """One surfacing: where, from which edge, and its timing."""

    def __init__(self, side, x, y, t_warn, seed):
        self.side, self.x, self.y, self.seed = side, x, y, seed
        self.heading = {"l": 0.0, "r": np.pi, "b": -np.pi / 2, "t": np.pi / 2}[side]
        self.tw = t_warn                    # edge warning starts (3 s ahead)
        self.tb = t_warn + 3.0              # breach
        self.to = self.tb + 0.8             # mouth opens
        self.tc = self.to + 3.0             # mouth closes
        self.td = self.tc + 0.5             # dive starts
        self.te = self.td + 0.7             # under again
        rng = np.random.default_rng(seed)
        self.cracks = []
        for k in range(9):
            a = k * TAU / 9 + rng.uniform(-0.25, 0.25)
            pts, r, aa = [(0.0, 0.0)], 6.0, a
            while r < 64:
                r += rng.uniform(6, 11)
                aa += rng.uniform(-0.35, 0.35)
                pts.append((np.cos(aa) * r, np.sin(aa) * r))
            self.cracks.append(pts)
        self.parts = [(rng.uniform(0, TAU), rng.uniform(70, 230), rng.uniform(0.5, 1.3), rng.uniform(1.5, 3.5))
                      for _ in range(150)]
        self.dust = [(rng.uniform(0, TAU), rng.uniform(10, 50), rng.uniform(18, 40)) for _ in range(14)]

    def rise(self, t):
        if t < self.tb:
            return 0.0
        if t < self.to:
            return ease((t - self.tb) / (self.to - self.tb))
        if t < self.td:
            return 1.0
        return 1.0 - ease((t - self.td) / (self.te - self.td))

    def opening(self, t):
        if self.to <= t < self.tc:
            return ease((t - self.to) / 0.45)
        if self.tc <= t < self.td:
            return 1.0 - ease((t - self.tc) / 0.4)
        return 0.0

    def bulge(self, t):
        """0..1 bulge of the sand before the breach (the tell; it grows over the last 2 s)."""
        if t < self.tb - 2.0 or t >= self.tb + 0.3:
            return 0.0
        if t < self.tb:
            return ease((t - (self.tb - 2.0)) / 2.0)
        return 1.0 - (t - self.tb) / 0.3

    def active(self, t):
        return self.tw <= t < self.te + 2.0

    def to_screen(self, lx, ly):
        ox, oy = rig.local_to_screen(lx, ly, self.heading, C_S)
        return self.x + ox, self.y + oy


def displacement(v, br, t, yy, xx):
    """Height displacement of the sand for one breach (bulge, body furrow, crater or funnel)."""
    D = np.zeros_like(yy)
    if not br.active(t):
        return D
    b = br.bulge(t)
    dx, dy = xx - br.x, yy - br.y
    r = np.sqrt(dx * dx + dy * dy)
    # the body under the sand: a furrow from the edge toward the breach point, growing during the tell
    k = sstep(br.tw, br.tb, t) * (1.0 - sstep(br.td, br.te + 1.5, t))
    if k > 0:
        if v == "a":
            ex, ey = br.to_screen(0, -3.6)
            fx, fy = br.to_screen(0, worm_exit(1.0))
            reach = sstep(br.tw, br.tb - 0.5, t)
            fx, fy = ex + (fx - ex) * reach, ey + (fy - ey) * reach
            lines = [((ex, ey), (fx, fy), 34.0, 1.0)]
        else:
            rng = np.random.default_rng(br.seed)
            lines = []
            for j in range(5):
                a = br.heading + np.pi + rng.uniform(-1.1, 1.1)
                L = rng.uniform(160, 300)
                lines.append(((br.x + np.cos(a) * L, br.y + np.sin(a) * L), (br.x, br.y), 13.0, 0.9))
        for (x0, y0), (x1, y1), w, amp in lines:
            sx, sy = x1 - x0, y1 - y0
            L2 = sx * sx + sy * sy + 1e-9
            u = np.clip(((xx - x0) * sx + (yy - y0) * sy) / L2, 0, 1)
            dd = np.hypot(xx - (x0 + sx * u), yy - (y0 + sy * u))
            wob = 1.0 + 0.25 * np.sin(u * 9 + t * 3)
            D += amp * k * np.exp(-(dd / (w * wob)) ** 2)
    if v == "a":
        D += 2.1 * b * np.exp(-(r / 62) ** 2)
        if t >= br.tb:              # crater rim around where the body comes out
            c = sstep(br.tb, br.tb + 0.4, t) * (1.0 - sstep(br.te, br.te + 2.0, t))
            ex, ey = br.to_screen(0, worm_exit(max(0.35, br.rise(t))))
            re = np.hypot(xx - ex, yy - ey)
            D += c * (0.55 * np.exp(-((re - 46) / 16) ** 2) - 0.5 * np.exp(-(re / 34) ** 2))
    else:
        D += 1.6 * b * np.exp(-(r / 70) ** 2) - 0.4 * b * np.exp(-(r / 25) ** 2)
        if t >= br.tb - 0.2:
            c = sstep(br.tb - 0.2, br.tb + 0.5, t) * (1.0 - sstep(br.te, br.te + 2.0, t))
            R = 130.0
            fun = -2.2 * np.clip(1 - r / R, 0, 1) ** 1.3 + 0.45 * np.exp(-((r - R) / 16) ** 2)
            # sand sliding in: spiral ridges in the funnel wall
            ang = np.arctan2(dy, dx)
            fun += 0.08 * np.sin(ang * 7 + r * 0.12 - t * 5) * np.clip(1 - r / R, 0, 1)
            D += c * fun
    return D


def vein_pulses(img, br, t, glow):
    """Variant B's tell: violet light pulses running along the swelling veins to the breach."""
    k = sstep(br.tw, br.tw + 0.8, t) * (1.0 - sstep(br.tb, br.tb + 0.6, t))
    if k <= 0:
        return
    rng = np.random.default_rng(br.seed)
    for j in range(5):
        a = br.heading + np.pi + rng.uniform(-1.1, 1.1)
        L = rng.uniform(160, 300)
        for m in range(4):
            u = ((t * 0.45 + m * 0.25 + j * 0.13) % 1.0)
            x = br.x + np.cos(a) * L * (1 - u)
            y = br.y + np.sin(a) * L * (1 - u)
            raster.add_light(img, x, y, 11, glow, 1.5 * k * np.sin(np.pi * u))


def draw_cracks(img, br, t, glow):
    b = br.bulge(t)
    k = np.clip((b - 0.35) / 0.65, 0, 1)
    if k <= 0:
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for pts in br.cracks:
        n = max(2, int(len(pts) * k))
        xy = [(br.x + x, br.y + y) for x, y in pts[:n]]
        d.line(xy, fill=(30, 14, 10, int(230 * k)), width=2)
        d.line(xy, fill=glow + (int(90 * k),), width=1)
    img.alpha_composite(lay)


def draw_eruption(img, br, t, mouth_xy):
    """Sand thrown out at the breach, then sand streaming off; a dust cloud (<= 40 % opacity)."""
    s = t - br.tb
    if not (-0.05 <= s < 1.6):
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for a, sp, life, size in br.parts:
        if s > life:
            continue
        g = s / life
        r = sp * life * (1 - (1 - g) ** 2) * 0.7
        x, y = br.x + np.cos(a) * r, br.y + np.sin(a) * r
        c = SAND[4] if (a * 7) % 1 > 0.4 else SAND[3]
        al = int(255 * (1 - g ** 2))
        z = size * (1.3 - 0.6 * g)
        d.rectangle([x - z / 2, y - z / 2, x + z / 2, y + z / 2], fill=tuple(c) + (al,))
    dust = Image.new("RGBA", img.size, (0, 0, 0, 0))
    dd = ImageDraw.Draw(dust)
    for a, r0, rr in br.dust:
        g = min(1.0, s / 1.6)
        r = r0 + 70 * g
        x, y = br.x + np.cos(a) * r, br.y + np.sin(a) * r
        R = rr * (0.6 + 1.2 * g)
        dd.ellipse([x - R, y - R, x + R, y + R], fill=tuple(SAND[4]) + (int(100 * (1 - g)),))
    img.alpha_composite(dust.filter(ImageFilter.GaussianBlur(8)))
    img.alpha_composite(lay)


class Suction:
    """Debris rocks and sand streaks spiralling into an open mouth."""

    def __init__(self, seed):
        rng = np.random.default_rng(seed)
        self.rocks = [(rng.uniform(0, TAU), rng.uniform(140, 300), rng.uniform(0, 1.6), int(rng.integers(0, 5)))
                      for _ in range(20)]
        self.streaks = [(rng.uniform(0, TAU), rng.uniform(60, 260), rng.uniform(0, 1)) for _ in range(46)]

    def draw(self, img, cx, cy, s, rocks, strength=1.0):
        """``s`` = seconds since the pull started."""
        lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(lay)
        for a0, r0, ph in self.streaks:
            u = (s * 0.9 + ph) % 1.0
            r = r0 * (1 - u) ** 1.3 + 20
            a = a0 + 1.6 * u
            x, y = cx + np.cos(a) * r, cy + np.sin(a) * r
            x2, y2 = cx + np.cos(a - 0.12) * (r + 16), cy + np.sin(a - 0.12) * (r + 16)
            al = int(110 * strength * np.sin(np.pi * u))
            d.line([x2, y2, x, y], fill=tuple(SAND[5]) + (al,), width=1)
        img.alpha_composite(lay)
        for a0, r0, t0, k in self.rocks:
            if s < t0:
                continue
            u = min(1.0, (s - t0) / 2.2)
            r = r0 * (1 - u ** 1.7)
            if r < 24:
                continue
            a = a0 + 2.2 * u ** 1.5
            x, y = cx + np.cos(a) * r, cy + np.sin(a) * r
            e3.place(img, rocks[k], x, y, "ground")


def colossus_fight(v):
    T = 12.0
    alb, hgt0 = basin_ground()
    GH = hgt0.shape[0]
    ms = A_SPRITES if v == "a" else B_SPRITES
    glow = raster.hexrgb("00ff9a") if v == "a" else raster.hexrgb("9a4dff")
    if v == "a":
        brs = [Breach("l", 92, 250, 0.0, 3), Breach("b", 280, 470, 8.0, 4)]
    else:
        brs = [Breach("l", 96, 250, 0.0, 3), Breach("b", 270, 462, 8.0, 4)]
    rocks = rock_sprites()
    suck = Suction(9)
    rises = (0.3, 0.55, 0.8, 1.0)
    opens = (0.5, 1.0)
    jobs = []
    for br in brs:
        h = br.heading if v == "a" else 0.0
        jobs += [(ms, h, dict(rise=r, open_=0.0)) for r in rises]
        jobs += [(ms, h, dict(rise=1.0, open_=o)) for o in opens]
    prerender(jobs)
    yy, xx = np.mgrid[0:FH, 0:FW].astype(np.float64)
    scroll = lambda t: 8.0 * (int(round(t * FPS)) // 10 * 10) / FPS   # steps at 2 Hz (GIF size)

    def mouth_xy(br):
        if v == "a":
            return br.to_screen(0, worm_mouth_y())
        return br.x, br.y

    def player(t):
        b0 = brs[0]
        if t < 3.0:
            return 250 + 60 * np.sin(t * 1.4), 470 - 50 * ease(t / 3.0)
        if t < 4.4:
            s = ease((t - 3.0) / 1.4)
            return 250 + 60 * np.sin(3 * 1.4) * (1 - s) + 60 * s, 420 - 130 * s
        if t < 7.6:                                 # side guns at the mouth; pulled toward it
            pull = sstep(b0.to + 0.2, b0.tc, t) * 46 * (1 - sstep(b0.tc, b0.tc + 0.6, t))
            return 310 - pull + 14 * np.sin(t * 2.2), 290 + 26 * np.sin(t * 1.1)
        s = ease((t - 7.6) / 2.2)
        return 316 + (230 - 316) * s, 300 + (250 - 300) * s

    rings = []
    for br in brs:
        for tr in (br.to + 0.35, br.to + 1.35, br.to + 2.35):
            rings.append((tr, br))
    frames = []
    for i, t in enumerate(times(T)):
        y0 = int(GH - FH - scroll(t))
        H = hgt0[y0:y0 + FH].copy()
        tg = (i - i % 2) / FPS                 # the sand's shape updates at 10 Hz (GIF size)
        for br in brs:
            H += displacement(v, br, tg, yy, xx)
        shade = np.round(hillshade(H * 30, 1.0) * 20) / 20
        if v == "b":                        # the funnel's depth: darker toward its throat
            for br in brs:
                c = sstep(br.tb - 0.2, br.tb + 0.5, tg) * (1.0 - sstep(br.te, br.te + 2.0, tg)) if br.active(tg) else 0
                if c > 0:
                    r = np.hypot(xx - br.x, yy - br.y)
                    shade = shade * (1 - 0.45 * c * np.clip(1 - r / 130, 0, 1) ** 0.8)
        rgb = alb[y0:y0 + FH] * shade[..., None]
        f = raster.to_rgba_image(np.clip(rgb, 0, 255))
        f = b6.terrain.recede(f, amount=0.12, darken=0.85)
        px, py = player(t)
        for br in brs:
            if not br.active(t):
                continue
            draw_cracks(f, br, t, glow)
            if v == "b":
                vein_pulses(f, br, t, glow)
            rise, op = br.rise(t), br.opening(t)
            mx, my = mouth_xy(br)
            if 0 < rise:
                h = br.heading if v == "a" else 0.0
                r_ = min(rises, key=lambda z: abs(z - rise)) if rise < 0.999 else 1.0
                o_ = 0.0 if op < 0.25 else (0.5 if op < 0.75 else 1.0)
                sp = ms.get(h, rise=r_, open_=o_ if r_ == 1.0 else 0.0)
                cx, cy = br.x, br.y
                sh = sprite.shadow_of(sp, opacity=0.5, blur=2.0)
                sprite.paste_center(f, sh, cx + 10 * r_, cy + 14 * r_)
                sprite.paste_center(f, sp, cx, cy)
                if o_ > 0:
                    raster.add_light(f, mx, my, 40, raster.hexrgb("a8ff2a"), 0.6 * o_)
            if br.to + 0.2 <= t < br.tc + 0.4:
                s = t - (br.to + 0.2)
                k = min(1.0, s / 0.4) * (1 - sstep(br.tc, br.tc + 0.4, t))
                suck.draw(f, mx, my, s, rocks, k)
            draw_eruption(f, br, t, (mx, my))
            if br.tw <= t < br.tb and br.side != "t":
                f.alpha_composite(ui_r11.variant_a(br.side, t - br.tw))
        for tr, br in rings:
            if tr <= t < tr + 3.5:
                mx, my = mouth_xy(br)
                R = 26 + 115 * (t - tr)
                for j in range(14):
                    a = j * TAU / 14 + (0.11 if int(tr) % 2 else 0)
                    e3.orb(f, mx + np.cos(a) * R, my + np.sin(a) * R, 5)
        # player: side guns while level with the left mouth, rear guns over the rear breach
        player_draw(f, px, py, t, front=t < 4.0 or t > 7.6, rear=t > 9.8)
        b0 = brs[0]
        if b0.to <= t < b0.tc:
            d = ImageDraw.Draw(f)
            c = raster.hexrgb("40ff80")
            for k in range(4):
                x = px - 30 - ((t * 400 + k * 80) % 320)
                if x > mouth_xy(b0)[0] - 20:
                    d.rectangle([x - 7, py - 2, x + 7, py + 2], fill=c + (220,))
                    d.rectangle([x - 5, py - 1, x + 5, py + 1], fill=(240, 255, 240, 255))
        frames.append(f)
    return frames, brs, rises, opens


def colossus_sheet(v, frames, brs, rises, opens):
    ms = A_SPRITES if v == "a" else B_SPRITES
    h_full = -np.pi / 2 if v == "a" else 0.0
    if v == "a":
        prerender([(ms, -np.pi / 2, dict(rise=1.0, open_=1.0)), (ms, np.pi, dict(rise=1.0, open_=1.0)),
                   (ms, np.pi / 2, dict(rise=1.0, open_=1.0)), (ms, 0.0, dict(rise=1.0, open_=1.0))])
    full_sp = ms.get(h_full, rise=1.0, open_=1.0)
    full = Image.new("RGBA", (300, 300), (0, 0, 0, 0))
    sprite.paste_center(full, full_sp, 150, 150)
    if v == "a":
        to = lambda lx, ly: tuple(int(c) for c in np.add((150, 150), rig.local_to_screen(lx, ly, h_full, C_S)))
        callouts = [
            (to(0, worm_mouth_y()), "THROAT = WEAK POINT (LIME), LIT ONLY WHILE THE JAWS ARE OPEN; FIRES THE RINGS"),
            (to(0.62, worm_mouth_y() + 0.5), "THREE JAW PLATES ROUND TWO ROWS OF BONE TEETH (SHUT = A POINTED BEAK)"),
            (to(0.0, -0.6), "SEGMENTED SLATE BODY ARCHING OUT OF THE SAND; BONE SPINES, TEAL SEAM LIGHTS"),
            (to(0.0, worm_exit(1.0)), "WHERE IT LEAVES THE SAND: CRATER RIM, SAND STREAMING OFF; THE REST RUNS ON UNDER THE BASIN"),
        ]
    else:
        to = lambda lx, ly: (int(150 + lx * C_S), int(150 - ly * C_S))
        callouts = [
            (to(0, 0), "THROAT = WEAK POINT (LIME) IN A RING OF TEETH; FIRES THE RINGS, PULLS DEBRIS IN"),
            (to(1.15, 1.15), "FOUR BONE MANDIBLES: CROSSED OVER THE THROAT WHEN SHUT, FLUNG OPEN ACROSS THE SAND"),
            (to(-0.7, 0.3), "EIGHT PALPS WITH VIOLET TIPS ROUND THE COLLAR"),
            (to(0.0, -0.62), "FLESHY COLLAR AT THE BOTTOM OF A SAND FUNNEL (THE FUNNEL IS THE GROUND, NOT THE SPRITE)"),
        ]
    # breach sequence crops round the left breach
    b0 = brs[0]
    seq_t = [(1.4, "TELL: BULGE + EDGE WARNING"), (2.7, "CRACKS"), (b0.tb + 0.35, "BREACH"),
             (b0.to + 0.5, "JAWS OPEN, RING"), (b0.to + 1.9, "SUCTION"), (b0.td + 0.35, "DIVE")]
    crops = []
    for t, lab in seq_t:
        fr = frames[min(len(frames) - 1, int(t * FPS))]
        x0 = 0
        y0 = int(b0.y - 140)
        crops.append((lab, fr.crop((x0, y0, x0 + 300, y0 + 280))))
    seq = Image.new("RGBA", (len(crops) * 206, 210), raster.SHEET_BG)
    for i, (lab, c) in enumerate(crops):
        seq.alpha_composite(c.resize((200, 187), Image.LANCZOS), (i * 206, 12))
        raster.draw_text(seq, i * 206, 0, lab, raster.LABEL)
    # states at 1x
    h0 = b0.heading if v == "a" else 0.0
    st = [b6.cropped(ms.get(h0, rise=r, open_=0.0)) for r in rises] + \
         [b6.cropped(ms.get(h0, rise=1.0, open_=o)) for o in opens]
    labs = [f"RISE {r:.2f}" for r in rises] + [f"OPEN {o:.1f}" for o in opens]
    states = Image.new("RGBA", (sum(s.width + 10 for s in st), max(s.height for s in st) + 14), raster.SHEET_BG)
    x = 0
    for s, lab in zip(st, labs):
        cell = e3.checker(s.width, s.height)
        cell.alpha_composite(s)
        states.alpha_composite(cell, (x, 12))
        raster.draw_text(states, x, 0, lab, raster.LABEL_DIM)
        x += s.width + 10
    panels = [("BREACH SEQUENCE, LEFT EDGE (FRAMES FROM THE GIF, 0.67X)", seq), ("SPRITE STATES (1X)", states)]
    if v == "a":
        sides = [scaled(b6.cropped(ms.get(h, rise=1.0, open_=1.0)), 0.5) for h in (0.0, np.pi, -np.pi / 2, np.pi / 2)]
        sd_img = Image.new("RGBA", (sum(s.width + 10 for s in sides), max(s.height for s in sides) + 14), raster.SHEET_BG)
        x = 0
        for s, lab in zip(sides, ("FROM THE LEFT", "FROM THE RIGHT", "FROM THE REAR", "FROM THE FRONT")):
            sd_img.alpha_composite(s, (x, 12))
            raster.draw_text(sd_img, x, 0, lab, raster.LABEL_DIM)
            x += s.width + 10
        panels.append(("FOUR BREACH SIDES, OPEN (0.5X; EACH SIDE IS ITS OWN RENDER, LIGHT STAYS TOP-LEFT)", sd_img))
        notes = ["DUST COLOSSUS A - 'REARING MAW' - ACT 3 BOSS, L21 HELLAS BASIN (SLATE CHITIN, TEAL SEAMS, LIME WEAK POINT)",
                 "ONE COLONY-SIZED WORM UNDER A SLOWLY SCROLLING DUNE FIELD. THE TELL: A FURROW RUNS IN FROM THE EDGE UNDER THE SAND",
                 "AND A DOME BULGES AND CRACKS (TEAL LIGHT IN THE CRACKS) WHILE THE EDGE WARNING SHOWS, 3 S AHEAD. THEN IT BREACHES",
                 "(SAND THROWN OUT, A DUST CLOUD, SAND STREAMING OFF), ARCHES ITS HEAD INTO THE FIELD AND OPENS THREE JAW PLATES: THE",
                 "LIME THROAT IS THE WEAK POINT, FIRES 14-ORB RINGS AND SUCKS ROCKS AND SAND STREAKS (AND THE SHIP) TOWARD IT. IT",
                 "SHUTS, SINKS BACK, AND THE NEXT FURROW HEADS FOR ANOTHER EDGE: SIDES AND REAR TEST SIDE AND REAR WEAPONS."]
    else:
        notes = ["DUST COLOSSUS B - 'SINKHOLE COLONY' - ACT 3 BOSS, L21 HELLAS BASIN (BONE MANDIBLES, VIOLET VEINS, LIME WEAK POINT)",
                 "THE ORGANISM NEVER SHOWS ITS BODY: IT IS THE BASIN. THE TELL: VEIN RIDGES SWELL UNDER THE SAND AND CONVERGE ON A",
                 "SPOT NEAR AN EDGE, WHICH BULGES AND CRACKS (VIOLET LIGHT) UNDER THE EDGE WARNING, 3 S AHEAD. THE GROUND THEN",
                 "COLLAPSES INTO A FUNNEL (SAND SLIDING IN, SPIRAL RIDGES), FOUR BONE MANDIBLES UNFOLD FROM THE RIM AND THE LIME THROAT",
                 "FIRES 14-ORB RINGS WHILE THE FUNNEL SUCKS ROCKS, SAND AND THE SHIP IN. THE MANDIBLES CROSS SHUT, THE THROAT SINKS AND",
                 "THE SAND FILLS IN; THE NEXT VEINS RUN TO ANOTHER EDGE. SAME BREACH POINTS, PULL AND RINGS AS A; A DIFFERENT CREATURE."]
    b1 = brs[0]
    title = "DUST COLOSSUS " + v.upper() + " - ACT 3 BOSS (VRELL)"
    return b6.boss_sheet(title, full, callouts, panels, frames[int((b1.to + 1.9) * FPS)], notes,
                         "FULL SPRITE 1X (300X300), OPEN" + (", RISING FROM THE REAR" if v == "a" else ""))


def colossus(v):
    frames, brs, rises, opens = colossus_fight(v)
    return colossus_sheet(v, frames, brs, rises, opens), frames


# =========================================================================== main

JOBS = {
    "walker-a": lambda: save("revenant-walker", "a", *walker("a")),
    "walker-b": lambda: save("revenant-walker", "b", *walker("b")),
    "colossus-a": lambda: save("dust-colossus", "a", *colossus("a")),
    "colossus-b": lambda: save("dust-colossus", "b", *colossus("b")),
}


def main(args):
    for key in (args or list(JOBS)):
        JOBS[key]()


if __name__ == "__main__":
    main(sys.argv[1:])
