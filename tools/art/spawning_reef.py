#!/usr/bin/env python3
"""Production art: the Vrell spawning reef, Level 24's secondary target on the abyssal plain
(design/enemies/ground/spawning-reef, promoted by part D's gap check D1; M8 part D batch, concept round 53, step
ART-D). Straight to production from the round-48 abyssal plain mood still (``europa-abyssal-plain-r48-a``: "lumpy
plum and bone coral, a teal iris at the crown", tools/concept/europa_r48.py ``reef``) and the level's "a coral tower
with a teal iris that releases 4 Glassfin every 3.5 s". Two looks for round 53 (``--look``):

  a  "coral knot" (the mood still's reef): a lumpy mound of plum coral bulbs, bone-white finger coral breaking out of
     its flanks, teal polyp buds, a raised collar at the crown round a six-petal iris over the teal throat.
  b  "bone chimneys": a plum dome ringed by seven bone tube chimneys of different heights with violet lips and a teal
     glow deep in their mouths; the iris at the dome's crown.

The frame layout is the Hive Node's (tools/art/hive_node.py), so the game's periodic-spawner look (EnemyLooks: 6 iris
states x 4 pulse frames, the iris opening over the spawn's 0.5 s telegraph, the creep withering after the death)
draws it without new code. `fixed` (no headings), lit from the upper left; 80x80 (proposed hitbox 60x60), a ground
target on the sea floor (`layer: ground`, `tier: large`).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims
``spawning-reef`` once design/enemies/ground/spawning-reef/data.yaml exists, first_level 24):
  spawning-reef_0..23          80x80, normal: 6 iris states x 4 pulse frames, indexed iris * 4 + pulse (iris 0 shut,
                               1-4 opening, 5 open; the pulse a breathing loop of the bulbs, swell 0, 0.4, 1, 0.6)
  spawning-reef-glow_0..23     80x80, additive, the same order: the throat, the polyps (and b's chimney mouths) alone,
                               the throat dim while shut (30 %), a soft teal halo over the iris growing as it opens
  spawning-reef-creep_0..8     160x160, normal with stepped translucency, a ground decal under the reef: the reef bed
                               (coral rubble, teal-veined roots in the silt); 0 alive, 1-8 its wither over 2 s after
                               the death (the veins go out, the rubble greys and crumbles)
  spawning-reef-death_0..15    128x128, additive: the `large` coral burst's light (a teal-white flash from the iris,
                               the throat's teal bleeding into the water, motes; no fire, no bubbles)
  spawning-reef-tatters_0..15  128x128, normal: the coral bulbs and bone fingers broken off and tumbling slowly out,
                               settling; a stepped silt-and-coral dust cloud under them
  spawning-reef-stump          80x80, normal (single): the razed reef: the mound slumped and torn open, the iris
                               shut for good, broken finger stubs, greyed coral, no glow
  design/enemies/ground/concept/spawning-reef-final-r53-<look>.png/.gif   the review sheet and GIF (on the plain under
                               the review grade: two reefs scrolling at 80 px/s, the iris opening and four fry fanning
                               out every 3.5 s, one reef shot and razed)

Run: python3 tools/art/spawning_reef.py [--look a|b] [--review]   (~1 min a look; --review only rebuilds the review
files from what is written). The default look is a; run b first and a last so a's frames stand in assets/ until the
user's pick.
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import creeper  # noqa: E402  (tools/art: the 1 px light rim, the multi-material death pieces)
import l24kit as kit
from artkit import DESIGN, TAU

import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged)
import vrell_deaths as vd  # noqa: E402  (tools/art: the death pieces' pose and the motes)
import vrell_fx as fx  # noqa: E402  (tools/art: easing, timing)
from render import raster  # noqa: E402
from render.sdf import (Material, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract,  # noqa: E402
                        union)

import enemies_r36 as e36  # noqa: E402  (concept script, unchanged: sd_torus_z)

SCRIPT = "spawning_reef.py"
BATCH = "M8 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r53"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "ground" / "concept"
SLUG = "spawning-reef"

SIZE = (80, 80)
EXTENT = 2.5                                    # model units across the sprite (32 px a unit)
IRIS_STATES = 6
PULSES = (0.0, 0.4, 1.0, 0.6)
COLOURS, GLOW_COLOURS = 40, 16
THROAT_SHUT = 0.3
HALO = 0.6
TEAL = np.array((0, 255, 154), float)
CREEP = 160
CREEP_FRAMES = 9
DEATH = 128
DEATH_FRAMES = 16
DEATH_EXTENT = EXTENT * DEATH / SIZE[0]
PX = SIZE[0] / EXTENT

CORAL, DARK, BONE, THROAT, POLYP = range(5)

LOOK = "a"


def bumps(p, n):
    return 0.84 + 0.16 * np.cos(p[:, 0] * 23 + 1.3) * np.cos(p[:, 1] * 21 + 0.4) * np.cos(p[:, 2] * 19)


def mats(iris=1.0, glow=1.0, tone=1.0, look=None):
    look = look or LOOK
    throat = (THROAT_SHUT + (1 - THROAT_SHUT) * iris) * glow
    t = tone
    if look == "a":
        coral, dark, bone = (0.46, 0.21, 0.5), (0.17, 0.07, 0.21), (0.78, 0.71, 0.6)
    else:
        coral, dark, bone = (0.4, 0.17, 0.46), (0.3, 0.12, 0.52), (0.82, 0.77, 0.66)
    return [Material(tuple(np.array(coral) * t), metal=0.05, shininess=35, spec=0.45, pattern=bumps),   # 0 coral
            Material(tuple(np.array(dark) * t), metal=0.1, shininess=30, spec=0.4),                     # 1 petals / lips
            Material(tuple(np.array(bone) * t), metal=0.02, shininess=50, spec=0.55),                    # 2 bone coral
            Material((0.02, 0.07, 0.06), emission=tuple(np.array((0.0, 1.0, 0.62)) * throat)),           # 3 throat
            Material((0.04, 0.12, 0.1), emission=tuple(np.array((0.1, 0.9, 0.6)) * 0.8 * glow))]         # 4 polyps


def glow_mats(m):
    black = Material((0.0, 0.0, 0.0), metal=0.0, shininess=1.0, spec=0.0)
    return [replace(x, albedo=(0.0, 0.0, 0.0), metal=0.0, spec=0.0, pattern=None) if i in (THROAT, POLYP) else black
            for i, x in enumerate(m)]


def iris_of(i):
    return i / (IRIS_STATES - 1)


_RNG_A = np.random.default_rng(2401)
LUMPS_A = [(a, r, z, s) for a, r, z, s in zip(
    np.linspace(0, TAU, 9, endpoint=False) + _RNG_A.uniform(-0.25, 0.25, 9), _RNG_A.uniform(0.52, 0.66, 9),
    _RNG_A.uniform(0.02, 0.14, 9), _RNG_A.uniform(0.25, 0.34, 9))]
UPPER_A = [(a, r, z, s) for a, r, z, s in zip(
    np.linspace(0.3, TAU + 0.3, 5, endpoint=False) + _RNG_A.uniform(-0.2, 0.2, 5), _RNG_A.uniform(0.28, 0.36, 5),
    _RNG_A.uniform(0.3, 0.4, 5), _RNG_A.uniform(0.2, 0.26, 5))]
FINGERS_A = [(a, _RNG_A.uniform(0.85, 1.08), _RNG_A.uniform(0.3, 0.55), _RNG_A.uniform(-0.5, 0.5))
             for a in (0.5, 1.4, 2.5, 3.3, 4.2, 5.0, 5.8)]
POLYPS_A = [(a, _RNG_A.uniform(0.42, 0.7)) for a in np.linspace(0.15, TAU + 0.15, 11, endpoint=False)]

_RNG_B = np.random.default_rng(2402)
TUBES_B = [(a, _RNG_B.uniform(0.56, 0.7), _RNG_B.uniform(0.38, 0.78), _RNG_B.uniform(0.13, 0.19))
           for a in np.linspace(0.2, TAU + 0.2, 7, endpoint=False) + _RNG_B.uniform(-0.15, 0.15, 7)]


def _polar(a, r, z):
    return (r * np.cos(a), -r * np.sin(a), z)


def _iris(items, p, iris, z0, crown=0.26):
    """The crown: a raised collar round a crater whose six petals retract as the iris opens, over the teal
    throat (Hive Node's mechanism)."""
    d, m = union(*items, k=0.09)
    d, m = union((d, m), (e36.sd_torus_z(p, (0, 0, z0 + 0.06), crown, 0.085), CORAL), k=0.06)
    open_r = 0.07 + 0.17 * iris
    d, m = subtract((d, m), sd_cylinder_z(p, (0, 0, z0 + 0.1), open_r, 0.24))
    petals = []
    for j in range(6):
        ang = j * TAU / 6 + 0.25
        c, sn = np.cos(ang), np.sin(ang)
        r0 = 0.08 + 0.17 * iris
        petals.append((sd_capsule(p, (c * (r0 + 0.13), sn * (r0 + 0.13), z0 + 0.08),
                                  (c * r0, sn * r0, z0 + 0.12), 0.065, 0.025), DARK))
    d, m = union((d, m), *petals, k=0.02)
    return union((d, m), (sd_sphere(p, (0, 0, z0 - 0.12), 0.12 + 0.1 * iris), THROAT), k=0.0)


def scene_a(iris=0.0, pulse=0.0):
    s = 1.0 + 0.05 * pulse

    def sc(p):
        items = [(sd_ellipsoid(p, (0, 0, 0.0), (0.72, 0.68, 0.4)), CORAL)]
        for a, r, z, rad in LUMPS_A:
            items.append((sd_sphere(p, _polar(a, r, z), rad * s), CORAL))
        for a, r, z, rad in UPPER_A:
            items.append((sd_sphere(p, _polar(a, r, z), rad * s), CORAL))
        d, m = _iris(items, p, iris, 0.5)
        fingers = []
        for a, reach, h, bend in FINGERS_A:
            x0, y0, _ = _polar(a, 0.55, 0.2)
            x1, y1, _ = _polar(a, reach, 0.2 + h)
            fingers.append((sd_capsule(p, (x0, y0, 0.2), (x1, y1, 0.2 + h), 0.075, 0.035), BONE))
            x2, y2, _ = _polar(a + bend * 0.5, reach * 0.95 + 0.08, 0)
            fingers.append((sd_capsule(p, ((x0 + x1) / 2, (y0 + y1) / 2, 0.2 + h / 2), (x2, y2, 0.25 + h * 0.8),
                                       0.05, 0.025), BONE))
        d, m = union((d, m), *fingers, k=0.03)
        buds = [(sd_sphere(p, _polar(a, r, 0.32 - 0.25 * (r - 0.42)), 0.05), POLYP) for a, r in POLYPS_A]
        return union((d, m), *buds, k=0.015)
    return sc


def scene_b(iris=0.0, pulse=0.0):
    s = 1.0 + 0.05 * pulse

    def sc(p):
        items = [(sd_ellipsoid(p, (0, 0, 0.02), (0.56 * s, 0.52 * s, 0.44)), CORAL)]
        for k in range(5):                                        # an uneven plum foot, no round disc
            a = k * TAU / 5 + 0.6
            items.append((sd_sphere(p, _polar(a, 0.5, -0.04), 0.22 * s), CORAL))
        d, m = _iris(items, p, iris, 0.46, crown=0.22)
        tubes, holes, mouths = [], [], []
        for a, r, h, rad in TUBES_B:
            base = np.array(_polar(a, r * 0.75, 0.0))
            top = np.array(_polar(a, r + 0.14 * h, h))
            axis = (top - base) / np.linalg.norm(top - base)
            rr = rad * (1.0 + 0.04 * pulse)
            tubes.append((sd_capsule(p, tuple(base), tuple(top), rr * 1.1, rr), BONE))
            tubes.append((e36.sd_torus_z(p, tuple(top + axis * 0.02), rr * 0.86, 0.028), DARK))
            holes.append(sd_capsule(p, tuple(top + axis * 0.2), tuple(top - axis * 0.3), rr * 0.52, rr * 0.52))
            mouths.append((sd_sphere(p, tuple(top - axis * 0.3), rr * 0.5), POLYP))
        d, m = union((d, m), *tubes, k=0.05)
        for hole in holes:
            d, m = subtract((d, m), hole)
        return union((d, m), *mouths, k=0.0)
    return sc


def scene(iris=0.0, pulse=0.0, look=None):
    return (scene_a if (look or LOOK) == "a" else scene_b)(iris, pulse)


def halo(img, iris):
    """The open iris's soft teal light added to a straight-alpha glow render."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    rgb = a[..., :3] * a[..., 3:4] / 255
    h, w = rgb.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    d2 = (xx + 0.5 - w / 2) ** 2 + (yy + 0.5 - h / 2) ** 2
    sigma = 5 + 9 * iris
    rgb += TEAL * HALO * (0.15 + 0.85 * iris) * np.exp(-d2 / (2 * sigma * sigma))[..., None]
    rgb = np.clip(rgb, 0, 255)
    return Image.fromarray(np.dstack([rgb, np.full((h, w), 255.0)]).astype(np.uint8), "RGBA")


def render_unit(i, p, kind, look):
    global LOOK
    LOOK = look
    iris = iris_of(i)
    m = mats(iris)
    if kind == "glow":
        m = glow_mats(mats(iris, 1.3))
    img = artkit.native(*artkit.render_hi(scene(iris, PULSES[p]), m, SIZE, EXTENT, shadows=kind != "glow"))
    if kind == "glow":
        return artkit.additive(halo(img, iris))
    return creeper.light_rim(img, 0.6)


# =========================================================================== creep (the reef bed)

def creep_frame(f, look):
    """The reef bed under the tower (drawn at 4x, reduced): coral rubble and teal-veined roots in the silt;
    frame 0 alive, 1-8 withering (veins out, rubble greys and crumbles)."""
    ss = 4
    w = CREEP * ss
    u = 0.0 if f == 0 else f / (CREEP_FRAMES - 1)
    rng = np.random.default_rng(2410 if look == "a" else 2411)
    img = Image.new("RGBA", (w, w))
    d = ImageDraw.Draw(img)
    c = w / 2

    def mix(a, b, t):
        return tuple(int(round(x + (y - x) * t)) for x, y in zip(a, b))
    body = mix((70, 32, 82), (58, 52, 50), u)
    vein = mix((30, 210, 150), (70, 70, 62), min(1.0, u * 1.6))
    bone = mix((176, 160, 140), (110, 104, 96), u)

    def grow(x, y, a, length, width, depth):
        pts = [(x, y)]
        for _ in range(int(length)):
            a += rng.normal(0, 0.2)
            x += np.cos(a) * 3 * ss
            y += np.sin(a) * 3 * ss
            pts.append((x, y))
            if np.hypot(x - c, y - c) > w / 2 - 6 * ss:
                break
        keep = max(2, int(len(pts) * (1 - 0.45 * u)))
        d.line(pts[:keep], fill=body + (255,), width=int(width * ss))
        if u < 0.95:
            d.line(pts[:keep], fill=vein + (255,), width=max(1, int(width * ss * 0.35)))
        if depth < 2:
            for _ in range(2):
                i = int(rng.integers(len(pts) // 3, max(len(pts) // 3 + 1, len(pts) - 1)))
                grow(*pts[i], a + rng.choice([-1, 1]) * rng.uniform(0.5, 1.0), length * 0.55, width * 0.65, depth + 1)

    for k in range(7):
        a = k * TAU / 7 + rng.uniform(-0.2, 0.2)
        grow(c + np.cos(a) * 30 * ss, c + np.sin(a) * 30 * ss, a, 14, 3.4, 0)
    for _ in range(46):                                           # coral rubble round the foot
        a, r = rng.uniform(0, TAU), rng.uniform(26, 62)
        s = rng.uniform(2.0, 4.5) * (1 - 0.35 * u) * ss
        x, y = c + np.cos(a) * r * ss, c + np.sin(a) * r * ss
        col = bone if rng.uniform() < 0.35 else body
        lit = mix(col, (255, 255, 255), 0.18)
        d.ellipse([x - s, y - s, x + s, y + s], fill=col + (255,))
        d.ellipse([x - s * 0.8, y - s * 0.8, x - s * 0.1, y - s * 0.1], fill=lit + (255,))
    d.ellipse([c - 24 * ss, c - 24 * ss, c + 24 * ss, c + 24 * ss], fill=body + (110,))
    small = img.resize((CREEP, CREEP), Image.Resampling.BOX)
    a = np.array(small).astype(np.float64)
    al = a[..., 3] / 255
    al = np.where(al > 0.75, 1.0, np.where(al > 0.3, 0.66, np.where(al > 0.08, 0.33, 0.0)))
    al *= 1.0 - 0.3 * u
    a[..., :3] = a[..., :3] / np.maximum(a[..., 3:4] / 255, 1e-3)
    a[..., 3] = al * 255
    out = np.clip(a, 0, 255).astype(np.uint8)
    out[al == 0] = 0
    return Image.fromarray(out, "RGBA")


# =========================================================================== death

def death_glow(i, look):
    """The coral burst's light: a teal-white flash from the iris, the throat's teal bleeding out into the water
    and thinning, slow motes drifting up (under water: no fire)."""
    w = DEATH
    t = fx.t_of(i, DEATH_FRAMES)
    rng = np.random.default_rng(2421)
    c = w / 2
    core = 0.42 * PX
    cv = v8.Canvas(w, w)
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 6.0, 0, 1)
    tex = v8._cart_noise(w, 2422, cell=w * v8.SS // 6)
    churn = v8._shift(tex, t * w * 0.12, -t * w * 0.2)
    grow = fx.ease_out(t, 2.0)
    dens = v8.gauss(d, core * (1.2 + 1.6 * grow))
    haze = np.clip(dens * (0.5 + 0.9 * churn) - 0.25 - 0.55 * t, 0, 1)
    env = np.clip((i + 1) / 2.0, 0.4, 1) * (1 - t) ** 0.9
    cv.add((8, 70, 60), haze * 1.2 * env * fade)
    cv.add(TEAL, np.clip(haze - 0.45, 0, 1) * 0.5 * env * fade)
    flash = max(0.0, 1 - i / 4.5)
    cv.add((230, 255, 245), v8.gauss(d, core * (0.6 + 0.5 * t)) * 1.5 * flash)
    cv.add(TEAL, v8.gauss(d, core * (1.1 + 0.8 * t)) * 0.9 * flash)
    vd.motes(cv, rng, c, t, 18, (14, 40), (0.5, 1.0), TEAL, (225, 255, 245), fade)
    return artkit.additive(cv.image())


def _turn(q, ang):
    co, s = np.cos(-ang), np.sin(-ang)
    r = q.copy()
    r[:, 0], r[:, 1] = co * q[:, 0] - s * q[:, 1], s * q[:, 0] + co * q[:, 1]
    return r


def death_pieces(look):
    """Coral bulbs (a) or chimney sections (b) and bone fingers broken off, tumbling slowly out and settling; the
    mound slumping where it stood."""
    rng = np.random.default_rng(2431 if look == "a" else 2432)
    pieces = []
    if look == "a":
        for a, r, z, rad in LUMPS_A + UPPER_A:
            c0 = _polar(a, r, z)

            def lump(q, c0=c0, rad=rad):
                return sd_sphere(q, c0, rad * 0.85)
            pieces.append(dict(parts=[(lump, CORAL)], c0=c0, dir=vd.outward(rng, c0, 0.3),
                               reach=rng.uniform(0.15, 0.45), ease=2.2, axis=rng.uniform(0, TAU),
                               tumble=rng.uniform(1, 3) * rng.choice([-1, 1]), spin=rng.uniform(-1.5, 1.5),
                               rise=0.15, shrink_at=0.6, least=0.3))
        for a, reach, h, _ in FINGERS_A:
            x0, y0, _ = _polar(a, 0.55, 0.2)
            x1, y1, _ = _polar(a, reach, 0.2 + h)
            c0 = ((x0 + x1) / 2, (y0 + y1) / 2, 0.2 + h / 2)

            def finger(q, x0=x0, y0=y0, x1=x1, y1=y1, h=h):
                return sd_capsule(q, (x0, y0, 0.2), (x1, y1, 0.2 + h), 0.07, 0.035)
            pieces.append(dict(parts=[(finger, BONE)], c0=c0, dir=vd.outward(rng, c0, 0.4),
                               reach=rng.uniform(0.5, 0.9), ease=2.6, axis=rng.uniform(0, TAU),
                               tumble=rng.uniform(3, 5) * rng.choice([-1, 1]), spin=rng.uniform(-3, 3),
                               rise=0.3, shrink_at=0.65))
    else:
        for a, r, h, rad in TUBES_B:
            base = np.array(_polar(a, r * 0.75, 0.0))
            top = np.array(_polar(a, r + 0.14 * h, h))
            axis = (top - base) / np.linalg.norm(top - base)
            c0 = tuple((base + top) / 2)

            def tube(q, base=tuple(base), top=tuple(top), axis=axis, rad=rad):
                t = np.array(top)
                return subtract((sd_capsule(q, base, top, rad * 1.1, rad), 0),
                                sd_capsule(q, tuple(t + axis * 0.2), tuple(t - axis * 0.3), rad * 0.52, rad * 0.52))[0]
            pieces.append(dict(parts=[(tube, BONE)], c0=c0, dir=vd.outward(rng, c0, 0.3),
                               reach=rng.uniform(0.35, 0.75), ease=2.4, axis=rng.uniform(0, TAU),
                               tumble=rng.uniform(1.5, 3) * rng.choice([-1, 1]), spin=rng.uniform(-2, 2),
                               rise=0.25, shrink_at=0.65))
        for k in range(6):
            a = k * TAU / 6 + 0.3
            c0 = _polar(a, 0.32, 0.25)

            def shard(q, c0=c0):
                return sd_ellipsoid(q, c0, (0.2, 0.16, 0.14))
            pieces.append(dict(parts=[(shard, CORAL)], c0=c0, dir=vd.outward(rng, c0, 0.3),
                               reach=rng.uniform(0.2, 0.5), ease=2.2, axis=rng.uniform(0, TAU),
                               tumble=rng.uniform(1, 3) * rng.choice([-1, 1]), spin=rng.uniform(-1.5, 1.5),
                               rise=0.15, shrink_at=0.6, least=0.3))
    for j in range(6):                                            # the iris petals
        ang = j * TAU / 6 + 0.25
        co, s = np.cos(ang), np.sin(ang)
        c0 = (co * 0.22, s * 0.22, 0.56)

        def petal(q, co=co, s=s):
            return sd_capsule(q, (co * 0.3, s * 0.3, 0.56), (co * 0.17, s * 0.17, 0.6), 0.06, 0.025)
        pieces.append(dict(parts=[(petal, DARK)], c0=c0, dir=vd.outward(rng, c0, 0.4),
                           reach=rng.uniform(0.4, 0.8), ease=2.6, axis=rng.uniform(0, TAU),
                           tumble=rng.uniform(4, 7) * rng.choice([-1, 1]), spin=rng.uniform(-4, 4),
                           rise=0.4, shrink_at=0.55))
    return pieces


def dust(i):
    """The silt-and-coral dust cloud under the tatters (stepped translucency, slow: water)."""
    w = DEATH
    t = fx.t_of(i, DEATH_FRAMES)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    c = w / 2
    rng = np.random.default_rng(2441)
    field = np.zeros((w, w))
    for _ in range(12):
        a = rng.uniform(0, TAU)
        reach = rng.uniform(6, 34) * fx.ease_out(t, 2.2)
        bx, by = c + np.cos(a) * reach, c + np.sin(a) * reach - 5 * t
        r = rng.uniform(8, 14) * (0.5 + 0.9 * t)
        field = np.maximum(field, np.exp(-((xx - bx) ** 2 + (yy - by) ** 2) / (2 * r * r)))
    alpha = np.clip(field * 1.1 - 0.15, 0, 1) * (0.2 + 0.8 * min(1.0, i / 2.0)) * (1 - t) ** 1.2 * 0.9
    shade = 0.8 + 0.2 * np.clip((c - xx + c - yy) / w, -1, 1)
    rgb = np.array((96, 92, 104), float)[None, None] * shade[..., None]
    q = artkit.ordered_dither(np.clip(alpha, 0, 1), 4)
    out = np.dstack([np.clip(rgb, 0, 255), q * 255]).astype(np.uint8)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


def death_tatters(i, look):
    global LOOK
    LOOK = look
    scene_, m = creeper.pieces_scene(death_pieces(look), mats(0.0, 0.2, 0.85), fx.t_of(i, DEATH_FRAMES),
                                     lambda p: p)
    img = artkit.native(*artkit.render_hi(scene_, m, (DEATH, DEATH), DEATH_EXTENT))
    base = dust(i)
    base.alpha_composite(img)
    return base


def render_stump(look):
    """The razed reef: the mound slumped flat and torn open, the iris shut for good, greyed coral."""
    global LOOK
    LOOK = look

    def sc(p):
        q = p.copy()
        q[:, 2] = p[:, 2] * 1.9 + 0.05                           # slumped to half its height
        d, m = scene(0.0, 0.0)(q)
        d = d / 1.9
        d, m = subtract((d, m), sd_capsule(p, (-0.35, 0.25, 0.3), (0.4, -0.2, 0.32), 0.17))   # torn open
        return d, m
    m = mats(0.0, 0.0, 0.62)
    img = artkit.native(*artkit.render_hi(sc, m, SIZE, EXTENT))
    return creeper.light_rim(img, 0.4)


# =========================================================================== build

def _job(job):
    name, args = job
    return {"unit": render_unit, "creep": creep_frame, "death": death_glow, "tatters": death_tatters,
            "stump": render_stump}[name](*args)


def build(look):
    cycle = [(i, p) for i in range(IRIS_STATES) for p in range(len(PULSES))]
    with ProcessPoolExecutor() as pool:
        def run(name, arglist):
            return list(pool.map(_job, [(name, a) for a in arglist]))
        sets = [(SLUG, run("unit", [(i, p, "unit", look) for i, p in cycle]), COLOURS, False),
                (SLUG + "-glow", run("unit", [(i, p, "glow", look) for i, p in cycle]), GLOW_COLOURS, False),
                (SLUG + "-creep", run("creep", [(f, look) for f in range(CREEP_FRAMES)]), 24, False),
                (SLUG + "-death", run("death", [(i, look) for i in range(DEATH_FRAMES)]), 32, False),
                (SLUG + "-tatters", run("tatters", [(i, look) for i in range(DEATH_FRAMES)]), 36, False),
                (SLUG + "-stump", run("stump", [(look,)]), 24, True)]
    for name, frames, colours, single in sets:
        kit.write(name, artkit.quantize_set(frames, colours), SOURCE, single=single)


# =========================================================================== review

PERIOD, TELEGRAPH, HOLD, CLOSE = 3.5, 0.5, 0.4, 0.25
SCROLL = 80.0
FRY_SPEED, FRY_ARC = 160.0, 120.0


def iris_at(t):
    """The iris (0 shut .. 1 open) t s into a 3.5 s spawn cycle that releases at TELEGRAPH."""
    if t < TELEGRAPH:
        return t / TELEGRAPH
    if t < TELEGRAPH + HOLD:
        return 1.0
    return max(0.0, 1 - (t - TELEGRAPH - HOLD) / CLOSE)


def review(look):
    reef, glow = kit.load(SLUG), kit.load(SLUG + "-glow")
    creep, death, tat, stump = (kit.load(SLUG + "-creep"), kit.load(SLUG + "-death"), kit.load(SLUG + "-tatters"),
                                kit.load(SLUG + "-stump"))
    fry = kit.load("glassfin")
    try:
        burst = kit.load("explosion-deep-large")
    except FileNotFoundError:
        burst = artkit.load_frames("explosion-large")
    name = {"a": "CORAL KNOT", "b": "BONE CHIMNEYS"}[look]
    rows = [("IRIS STATES (SHUT .. OPEN), PULSE 0", reef[0::4], 2, False),
            ("BREATHING LOOP, IRIS SHUT AND OPEN", reef[0:4] + reef[20:24], 2, False),
            ("GLOW (ADDITIVE), IRIS STATES", glow[0::4], 2, True),
            ("REEF BED DECAL: ALIVE, WITHER", creep[0::2], 1, False),
            ("CORAL BURST LIGHT (ADDITIVE)", death[0::2], 1, True),
            ("TATTERS AND DUST", tat[0::2], 1, False),
            ("RAZED STUMP", stump, 2, False)]
    sheet = artkit.review_sheet(f"SPAWNING REEF - FINAL, LOOK {look.upper()} ({name})", rows, width=1000,
                                batch=BATCH)
    fps, dur = 15, 8.0
    w, h = 480, 400
    reefs = [(140, 60.0, 0.0, None), (340, -120.0, 1.7, 4.6)]      # x, y at t=0, cycle phase, razed at
    frames = []
    for fi in range(int(dur * fps)):
        t = fi / fps
        scroll = t * SCROLL
        img = kit.plate(w, h, 0, int(4000 - scroll), section=2)
        glows = []
        fish = []
        for x, y0, ph, kill in reefs:
            y = y0 + scroll
            if kill is not None and t >= kill:
                k = min(CREEP_FRAMES - 1, 1 + int((t - kill) * 4))
                kit.paste_c(img, creep[k], x, y)
                kit.paste_c(img, stump[0], x, y)
                s = int((t - kill) * 15)
                if s // 4 < len(tat):
                    kit.paste_c(img, tat[s // 4], x, y)
                if s // 4 < len(death):
                    glows.append((death[s // 4], x, y, 1.0))
                if s // 3 < len(burst):
                    glows.append((burst[s // 3], x, y, 1.0))
            else:
                kit.paste_c(img, creep[0], x, y)
                cyc = (t + ph) % PERIOD
                iris = iris_at(cyc)
                state = int(round(iris * (IRIS_STATES - 1)))
                pulse = int(t * 4 * (3 if cyc < TELEGRAPH else 1)) % 4
                kit.paste_c(img, reef[state * 4 + pulse], x, y)
                glows.append((glow[state * 4 + pulse], x, y, 1.0))
            # fry released at each cycle's TELEGRAPH, fanning out toward the ship (bottom) over 120 degrees
            for n in range(4):
                rel = TELEGRAPH - ph + n * PERIOD
                age = t - rel
                ry = y0 + rel * SCROLL                              # the reef's row at the release
                if (kill is not None and rel >= kill) or not 10 < ry < h - 10:
                    continue
                if 0 <= age < 2.4:
                    for j in range(4):
                        ang = np.radians(90 - FRY_ARC / 2 + FRY_ARC * j / 3)
                        dx, dy = np.cos(ang), np.sin(ang)
                        fish.append((x + dx * FRY_SPEED * age, ry + dy * FRY_SPEED * age, dx, dy, age))
        for fx_, fy, dx, dy, age in fish:
            hd = kit.heading_index(dx, dy, 16)
            kit.paste_c(img, fry[hd * 2 + (1 if age < 0.15 else 0)], fx_, fy)
        if 3.6 < t < 4.6:                                          # shots walking onto the second reef
            d = ImageDraw.Draw(img)
            for j in range(4):
                yy = h - ((t - 3.6) * 700 + j * 50) % 220
                d.rectangle([338, yy - 6, 342, yy + 6], fill=(200, 250, 255, 255))
        kit.paste_c(img, kit.ship(), 240, h - 40)
        for g, x, y, k in glows:
            img = kit.add_c(img, g, x, y, k)
        frames.append(img.convert("RGB"))
    big = Image.new("RGBA", (sheet.width, sheet.height + 250), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "ON THE PLAIN AT 1X (REVIEW GRADE, SECTION 2): TELEGRAPH, RELEASE, "
                     "A REEF SHOT AND RAZED", raster.LABEL)
    for i, fi in enumerate((6, 20, 72, 96)):
        big.alpha_composite(frames[fi].convert("RGBA").resize((228, 190)), (16 + i * 244, sheet.height + 18))
    artkit.save_review(big, frames, CONCEPT, SLUG, fps=fps, variant=look)


def main(argv):
    global LOOK
    look = argv[argv.index("--look") + 1] if "--look" in argv else "a"
    LOOK = look
    if "--review" not in argv:
        build(look)
    review(look)


if __name__ == "__main__":
    main(sys.argv[1:])
