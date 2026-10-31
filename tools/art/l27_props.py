#!/usr/bin/env python3
"""Production art: Level 27's props and the heat plume (design/campaign/act-4-deep-water/level-27-hydrothermal-vents,
"Hazards", "Secrets and pickups"; M8 part G batch, concept round 57, step ART-G; straight to production, no concept of
their own: designed to the README, the round-48 mood still `europa-vents-r48-a` (tools/concept/europa_r48.py
``vents_still``: the plume as an amber disc with shimmer rings and rising silt) and Thera Deep's hauler look of Levels
24-26 (the *Meridian*'s dull orange hull, the amber trim; tools/art/l24_props.py), readability rule 7 (the light rim)
for the secret). The hull-code wreck is a backdrop piece (tools/art/backdrop_l27.py ``hull-wreck``).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse: the
trigger's ``sprite`` once Level 27's data names it; the plume's ``heat-plume*`` once its vents name `look: heat-plume`):
  marlin-hauler_0..2       64x64, the secret: the Thera Deep pressure hauler *Marlin* nose-down against a smoker's
                           stack (a 20 HP `ground` trigger, t~40, x 90; `sprite: marlin-hauler`, `size: [64, 64]`): her
                           nose buried toward the upper left, the stern raised, the dull orange hull and white band, the
                           cargo hold amidships sealed under a skin of Vrell resin (violet-brown, teal veins); 0 sealed,
                           1 hit (the resin cracked, a dent), 2 opened (the resin burst off, the hold dark: the hidden
                           crate rises from it); rule 7's light rim
  marlin-hauler-glow_0..7  64x64, additive, aligned: 0-3 her stern's tired amber running lamp (a slow blink until the
                           first hit), 4-7 the resin's teal veins glinting through the cracks and sparks (after it)
  heat-plume_0..3          80x80, the plume's mouth on the sea floor (the vents' `look: heat-plume`, the grate's place):
                           a crusted vent with a ring of amber bacterial mat, 0 cold (dark throat), 1-3 the tell's steps
                           (the throat glowing dull red, orange, white-hot amber); the 64 px disc is its mouth's ring
  heat-plume-glow_0..2     96x96, additive, centred: the tell's amber light over the mouth for tell steps 1-3
  heat-plume-silt_0..7     96x96, normal, stepped translucency: the silt lifting off the mouth during the tell (a ring of
                           grit rising and drifting inward, 6 fps loop)
  heat-plume-column_0..15  96x96, normal, stepped translucency: the eruption seen from above, 1.6 s at 10 fps: rings of
                           shimmering water rising out of the disc and silt streaming outward (in the flame's place;
                           drawn over the ground units with normal blending, as the ice geysers' steam)
  heat-plume-heat_0..15    96x96, additive, the same clock: the eruption's amber-white heat in the disc and the rings'
                           bright crests
  heat-plume-settle_0..5   96x96, normal, stepped translucency: the silt settling after a burst (8 fps)
  heat-plume-bend_0..5     32x32, normal, stepped translucency: the short water ripple where a bullet is bent
                           (PLUME_BEND), 15 fps
  design/campaign/act-4-deep-water/level-27-hydrothermal-vents/concept/l27-props-final-r57-a.png/.gif   the review sheet
                           and GIF (t=26-42 under the vent light's stand-in: the first plume row's tell and eruption,
                           shots bent off a burning disc, the *Marlin* shot open and its crate rising)

Run: python3 tools/art/l27_props.py [--review]   (~40 s; --review rebuilds only the review; run backdrop_l27.py first:
the review stands on its floor)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
import l24_props as p24
import l27kit as kit
from artkit import DESIGN, TAU

from render import raster  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

SCRIPT = "l27_props.py"
BATCH = "M8 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r57"
artkit.REVIEW_ROUND = ROUND
CONCEPT = kit.LEVEL_DIR / "concept"
HAULER = (64, 64)
AXIS = (-0.64, 0.77)                         # the hull's axis in model space (y up): the nose toward the upper left
AMBER = np.array((255, 170, 60), float)
TEAL = np.array((40, 255, 200), float)
HOT = np.array((255, 236, 180), float)
MOUTH, PLUME, BEND = 80, 96, 32
render, rimmed = p24.render, p24.rimmed


# =========================================================================== the Marlin

def mats(state):
    scorch = (lambda p, n: np.where(np.hypot(p[:, 0] - 2, p[:, 1] + 1) < 7, 0.4, 1.0)) if state else None
    veins = (lambda p, n: np.where(np.abs(np.sin(p[:, 0] * 1.3 + np.sin(p[:, 1] * 0.9) * 2.2)) < 0.16, 1.5, 1.0))
    return [Material((0.58, 0.34, 0.18), metal=0.4, shininess=40, spec=0.5, pattern=scorch),   # 0 the dull orange hull
            Material((0.84, 0.82, 0.76), metal=0.2, shininess=40, spec=0.5),                   # 1 the white band
            Material((0.62, 0.48, 0.16), metal=0.3, shininess=50, spec=0.6),                   # 2 amber trim, rails
            Material((0.34, 0.22, 0.32), metal=0.0, shininess=70, spec=0.9, pattern=veins),    # 3 the resin skin
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),                    # 4 the dark hold
            Material((0.3, 0.32, 0.34), metal=0.0, shininess=8, spec=0.1),                     # 5 silt
            Material((0.18, 0.18, 0.2), metal=0.5, shininess=30, spec=0.4),                    # 6 fittings, the screw
            Material((0.9, 0.7, 0.4), metal=0.1, shininess=120, spec=1.2)]                     # 7 the stern lamp's lens


def scene(state):
    ang = float(np.arctan2(AXIS[1], AXIS[0]))

    def at(u, z):                                       # a point on the hull's axis, the hull tilted nose-down
        return (AXIS[0] * u, AXIS[1] * u - 1.0, z)

    def sc(p):
        q = rotate_z(p - np.array([0.0, -1.0, 0.0]), ang)      # hull-local: x along (bow +), y across
        tilt = 0.22
        zc = 7.0 - tilt * q[:, 0]                                # the axis' height along the hull (nose low)
        qq = q.copy()
        qq[:, 2] = q[:, 2] - zc + 7.0
        items = [(sd_capsule(qq, (20.0, 0, 7.0), (-21.0, 0, 7.0), 8.0, 7.4), 0),
                 (sd_capsule(qq, (9.0, 0, 7.0), (6.5, 0, 7.0), 8.4, 8.4), 1),                  # the white band
                 (sd_box(qq, (-17.5, 0, 7.0), (1.0, 8.6, 8.6), 0.5), 2),                        # the stern ring
                 (sd_cylinder_z(qq, (-23.0, 0, 7.0), 3.0, 4.6), 6),                             # the screw's hub
                 (sd_box(qq, (-12.0, 5.5, 14.4), (1.8, 1.4, 1.2), 0.4), 6),
                 (sd_sphere(qq, (-12.0, 5.5, 16.0), 1.3), 7)]                                   # the stern lamp
        hold = sd_box(qq, (-3.0, 0, 13.6), (8.5, 6.0, 1.6), 1.0)
        if state == 2:
            items.append((subtract((hold, 3), sd_box(qq, (-3.0, 0, 15.0), (7.0, 4.6, 2.6), 0.6))[0], 2))
            items.append((sd_box(qq, (-3.0, 0, 12.4), (7.0, 4.6, 0.6), 0.3), 4))
            for k, (sx, sy) in enumerate(((-6, 13), (6, -12), (-14, -9))):                     # resin shards, flung
                items.append((sd_ellipsoid(q, (sx, sy, 1.4 + k * 0.3), (3.2, 2.0, 0.8)), 3))
        elif state == 1:
            skin = subtract((sd_box(qq, (-3.0, 0, 15.4), (8.0, 5.6, 0.7), 0.7), 3), sd_sphere(qq, (-1.0, 1.0, 16.6), 2.6))
            items.append((hold, 2))
            items.append(skin)
        else:
            items.append((hold, 2))
            items.append((sd_box(qq, (-3.0, 0, 15.4), (8.0, 5.6, 0.7), 0.7), 3))
        items.append((sd_ellipsoid(q, (16, 0, 0.8), (14.0, 13.0, 4.5)), 5))                    # silt piled at the nose
        items.append((sd_ellipsoid(q, (0, 11, 0.6), (18.0, 4.5, 2.4)), 5))
        return union(*items, k=0.6)
    return sc


def _px(u, v, z=0.0):
    """Hull-local (u along, v across) -> frame px (ignoring height)."""
    ax, ay = AXIS
    x, y = ax * u - ay * v, ay * u + ax * v - 1
    return HAULER[0] / 2 + x, HAULER[1] / 2 - y


def lamp_px():
    return _px(-12.0, 5.5)


def hold_px():
    return _px(-3.0, 0.0)


def frame(state):
    return rimmed(render(scene(state), mats(state), HAULER, z_top=40))


def glow(i):
    w, h = HAULER
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    lx, ly = lamp_px()
    r = np.hypot(xx - lx, yy - ly)
    on = [1.0, 0.85, 0.15, 0.0][i] if i < 4 else [0.3, 0.0, 0.0, 0.2][i - 4]
    f = (np.exp(-(r / 1.5) ** 2) + np.exp(-(r / 5.5) ** 2) * 0.45 + np.exp(-(r / 12.0) ** 2) * 0.15) * on
    rgb = AMBER[None, None] * artkit.smooth_steps(f)[..., None]
    if i >= 4:                                         # the resin's teal veins glinting through the cracks, sparks
        rng = np.random.default_rng(2790 + i)
        mx, my = hold_px()
        s = np.zeros((h, w))
        for _ in range(int(rng.integers(3, 6))):
            sx, sy = mx + rng.normal(0, 4), my + rng.normal(0, 3)
            s += np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / 1.2) * rng.uniform(0.5, 1.0)
        rgb = rgb + TEAL[None, None] * artkit.smooth_steps(s)[..., None] * 0.8
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the heat plume

def _grid(w):
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    return xx - w / 2, yy - w / 2


def mouth(step):
    """The vent: a crusted collar round a dark throat, the mat ring; the throat glowing by the tell's step."""
    w = MOUTH
    x, y = _grid(w)
    r = np.hypot(x, y)
    n = raster.fbm(w, w, 5, 2795, octaves=3, period=False)
    hgt = np.clip(1 - np.abs(r - 15) / (8 + 3 * n), 0, 1) * (4 + 2 * n)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1 + (gx + gy) * 0.8, 0.4, 1.5)
    collar = hgt > 0.4
    rgb = np.zeros((w, w, 3))
    crust = np.array((70, 58, 50.0)) * (0.8 + 0.4 * n[..., None])
    rgb = np.where(collar[..., None], crust * shade[..., None], rgb)
    throat = r < 11
    heat = [0.0, 0.35, 0.65, 1.0][step]
    tcol = np.array((14, 9, 8.0)) * (1 - heat) + (np.array((150, 40, 10.0)) * (1 - heat) + np.array((255, 170, 70.0))
                                                   * heat) * heat
    rgb = np.where(throat[..., None], tcol * np.clip(1.15 - r / 14, 0.4, 1)[..., None], rgb)
    mats = (n > 0.42) & (r > 20) & (r < 30 + 6 * n) & ~collar
    mat = np.array((210, 176, 118.0)) * (0.85 + 0.3 * n[..., None])
    rgb = np.where(mats[..., None], mat, rgb)
    rng = np.random.default_rng(2796)
    alpha = (collar | throat | mats).astype(float)
    for _ in range(12):                                           # a few tube worms in the mat
        a, rr = rng.uniform(0, TAU), rng.uniform(21, 30)
        px, py = int(w / 2 + np.cos(a) * rr), int(w / 2 + np.sin(a) * rr)
        rgb[py, px] = (222, 216, 204)
        rgb[py, px + 1] = (214, 44, 40)
        alpha[py, px] = alpha[py, px + 1] = 1
    a = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    a[alpha == 0] = 0
    return Image.fromarray(a, "RGBA")


def _light(w, f, col):
    q = artkit.smooth_steps(f)
    rgb = np.asarray(col, float)[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def tell_glow(step):
    x, y = _grid(PLUME)
    r = np.hypot(x, y)
    k = [0.35, 0.65, 1.0][step]
    f = (np.exp(-(r / 8) ** 2) * 0.9 + np.exp(-(r / 24) ** 2) * 0.35) * k
    return _light(PLUME, f, AMBER * (0.8 + 0.2 * k))


def _puff(w, dens, rgb):
    rgb = np.broadcast_to(np.asarray(rgb, float), (w, w, 3))
    alpha = artkit.smooth_steps(dens)
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


SILT = np.array((112, 100, 88), float)


def silt_tell(i, n=8):
    """The silt lifting: grit motes on a ring round the mouth, rising (drawn inward and brighter) and drifting."""
    w = PLUME
    x, y = _grid(w)
    rng = np.random.default_rng(2801)
    dens = np.zeros((w, w))
    for k in range(40):
        a0, r0, sp = rng.uniform(0, TAU), rng.uniform(16, 34), rng.uniform(0.5, 1.0)
        u = (i / n * sp + rng.uniform(0, 1)) % 1.0
        rr = r0 * (1 - 0.45 * u)
        a = a0 + 0.6 * u
        px, py = np.cos(a) * rr, np.sin(a) * rr - 4 * u
        s = 1.0 + 1.4 * u
        dens = np.maximum(dens, np.exp(-((x - px) ** 2 + (y - py) ** 2) / s) * (0.4 + 0.5 * np.sin(u * np.pi)))
    return _puff(w, dens * 0.9, SILT * 1.15)


def column(i, n=16):
    """The eruption from above: shimmering rings rising out of the disc (pale water, expanding, thinning), the silt
    streaming outward in radial plumes, the disc's centre a churning pale core."""
    w = PLUME
    x, y = _grid(w)
    r = np.hypot(x, y)
    th = np.arctan2(y, x)
    u = i / (n - 1)
    env = min(1.0, u / 0.12) * min(1.0, (1 - u) / 0.2 + 0.15)
    rings = np.zeros((w, w))
    for k in range(3):
        ph = ((u * 2.6 + k / 3) % 1.0)
        rr = 6 + 30 * ph
        rings = np.maximum(rings, np.exp(-((r - rr) / (1.6 + 1.2 * ph)) ** 2) * (1 - ph) * 0.8)
    streams = (0.5 + 0.5 * np.cos(th * 9 + 3 * np.sin(th * 4) + u * 2)) ** 3
    silt = streams * np.exp(-((r - (18 + 18 * u)) / (8 + 6 * u)) ** 2) * 0.75
    core = np.exp(-(r / (12 + 6 * np.sin(u * np.pi))) ** 2) * 0.55
    n_ = raster.fbm(w, w, 6, 2811 + i, octaves=2, period=False)
    water = np.clip((rings + core) * (0.8 + 0.4 * n_), 0, 1) * env
    sd = np.clip(silt * (0.7 + 0.6 * n_), 0, 1) * env
    dens = np.clip(water * 0.75 + sd, 0, 0.92)
    k = sd / np.maximum(sd + water, 1e-6)
    rgb = (np.array((200, 222, 226), float)[None, None] * (1 - k[..., None]) + SILT[None, None] * k[..., None])
    return _puff(w, dens, rgb)


def heat(i, n=16):
    w = PLUME
    x, y = _grid(w)
    r = np.hypot(x, y)
    u = i / (n - 1)
    env = min(1.0, u / 0.1) * min(1.0, (1 - u) / 0.25 + 0.1)
    core = np.exp(-(r / (10 + 10 * u)) ** 2) * (1.0 - 0.4 * u)
    crest = np.zeros((w, w))
    for k in range(3):
        ph = ((u * 2.6 + k / 3) % 1.0)
        crest = np.maximum(crest, np.exp(-((r - (6 + 30 * ph)) / 1.2) ** 2) * (1 - ph) * 0.45)
    f = np.clip((core + crest) * env, 0, 1)
    q = artkit.smooth_steps(f)
    rgb = AMBER[None, None] * q[..., None] + (HOT - AMBER)[None, None] * np.clip(core * env - 0.55, 0, 1)[..., None] * 2
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def settle(i, n=6):
    w = PLUME
    x, y = _grid(w)
    r = np.hypot(x, y)
    u = i / (n - 1)
    n_ = raster.fbm(w, w, 8, 2821, octaves=3, period=False)
    dens = np.exp(-((r - (34 + 6 * u)) / (9 + 4 * u)) ** 2) * (0.6 + 0.5 * n_) * (1 - u) * 0.8
    return _puff(w, dens, SILT * (0.9 + 0.2 * n_[..., None]))


def bend(i, n=6):
    w = BEND
    x, y = _grid(w)
    r = np.hypot(x, y)
    u = i / (n - 1)
    ring = np.exp(-((r - (2 + 11 * u)) / (1.0 + 0.8 * u)) ** 2) * (1 - u)
    return _puff(w, ring * 0.9, np.array((210, 228, 232), float)[None, None] * np.ones((w, w, 1)))


def build():
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(frame, range(3)))
    kit.write("marlin-hauler", artkit.quantize_set(frames, 40), SOURCE)
    kit.write("marlin-hauler-glow", [glow(i) for i in range(8)], SOURCE)
    kit.write("heat-plume", artkit.quantize_set([mouth(s) for s in range(4)], 32), SOURCE)
    kit.write("heat-plume-glow", [tell_glow(s) for s in range(3)], SOURCE)
    kit.write("heat-plume-silt", artkit.quantize_set([silt_tell(i) for i in range(8)], 8), SOURCE)
    kit.write("heat-plume-column", artkit.quantize_set([column(i) for i in range(16)], 16), SOURCE)
    kit.write("heat-plume-heat", [heat(i) for i in range(16)], SOURCE)
    kit.write("heat-plume-settle", artkit.quantize_set([settle(i) for i in range(6)], 8), SOURCE)
    kit.write("heat-plume-bend", [bend(i) for i in range(6)], SOURCE)


# =========================================================================== review

FPS = 12
SEGMENTS = [(25.5, 6.0), (41.0, 3.5)]   # (level t, seconds): the first plume row; the Marlin
SHOTS = (27.5, 29.2)                    # level t: the ship fires up through the first row's burning left disc
MARLIN_FIRE, OPEN = 42.2, 42.9          # level t


def review():
    import backdrop_l27 as b27                                  # noqa: E402  (the floor, the pieces)
    body, gl = kit.load("marlin-hauler"), kit.load("marlin-hauler-glow")
    mo, tg, sl, col, ht, se, bd = (kit.load(n) for n in ("heat-plume", "heat-plume-glow", "heat-plume-silt",
                                                          "heat-plume-column", "heat-plume-heat", "heat-plume-settle",
                                                          "heat-plume-bend"))
    crate = artkit.load_frames("pickup-crate")
    bolt = artkit.load_frames("pulse-bolt")
    rows = [("THE MARLIN: SEALED, HIT, OPENED (2X)", body, 2, False),
            ("HER STERN LAMP (0-3), THE RESIN'S TEAL AND SPARKS AFTER THE HIT (4-7), ADDITIVE", gl, 2, True),
            ("PLUME MOUTH: COLD, TELL STEPS 1-3", mo, 2, False),
            ("THE TELL'S LIGHT (ADDITIVE)", tg, 1, True),
            ("THE TELL'S SILT LIFTING", sl, 1, False),
            ("THE ERUPTION FROM ABOVE: RINGS AND STREAMING SILT, EVERY 2ND OF 16", col[::2], 1, False),
            ("ITS HEAT (ADDITIVE), EVERY 2ND", ht[::2], 1, True),
            ("SETTLING; THE BEND'S RIPPLE (2X)", se + [f.resize((64, 64), Image.NEAREST) for f in bd], 1, False)]
    sheet = artkit.review_sheet("LEVEL 27 PROPS - THE MARLIN AND THE HEAT PLUME (FINAL)", rows, width=1000, batch=BATCH)
    pieces = b27.placed_pieces()
    frames = []
    halo = l26_halo()
    mt, mx = b27.MARLIN
    shots = []
    clock = [(t0 + k / FPS) for t0, d in SEGMENTS for k in range(int(d * FPS))]
    for fi, lt in enumerate(clock):
        t = lt
        if lt < 40:
            ship = (128.0 if lt > 26.8 else 240 - 112 * (lt - 25.5) / 1.3, 444.0)
        else:
            ship = (110.0, 444.0)
        img, pglows = b27.ground_view(lt, pieces)
        base = kit.scroll(lt)
        solids, glows, over = [], list(pglows), []
        burning = []
        for row_t, xs in kit.PLUME_ROWS:
            y = base + kit.SCREEN - kit.target_centre(row_t, kit.PLUME_WIDTH)
            if not -60 < y < kit.SCREEN + 60:
                continue
            for i, x in enumerate(xs):
                ph, u = kit.plume_phase(lt, row_t, i)
                k = 0 if ph == "cold" else (1 + min(2, int(u * 3)) if ph == "tell" else 3)
                solids.append((mo[k], x, y))
                if ph == "tell":
                    glows.append((tg[min(2, int(u * 3))], x, y, 1.0))
                    over.append((sl[int(lt * 6) % 8], x, y))
                elif ph == "burn":
                    over.append((col[min(15, int(u * 16))], x, y))
                    glows.append((ht[min(15, int(u * 16))], x, y, 1.0))
                    burning.append((x, y))
        y = base + kit.SCREEN - kit.target_centre(mt, HAULER[1])
        st = 0 if t < MARLIN_FIRE + 0.3 else (1 if t < OPEN else 2)
        solids.append((body[st], mx, y))
        glows.append((gl[int(lt * 4) % 4] if st == 0 else gl[4 + int(lt * 8) % 4], mx, y, 1.0))
        if t >= OPEN:
            u = min(1.0, (t - OPEN) / 0.8)
            hx, hy = hold_px()
            cy = y + hy - HAULER[1] / 2 - 18 * u
            glows.append((halo, mx + hx - HAULER[0] / 2, cy, 1.0))
            over.append((crate[int(t * 10) % len(crate)], mx + hx - HAULER[0] / 2, cy))
        fire = SHOTS[0] <= t < SHOTS[1] or MARLIN_FIRE <= t < OPEN
        if fire and fi % 2 == 0:
            for dx in (-9, 9):
                shots.append({"p": np.array((ship[0] + dx, ship[1] - 26.0)), "v": np.array((0.0, -520 * 0.7)),
                              "bent": False, "bt": None})
        for s in shots:
            s["p"] = s["p"] + s["v"] / FPS
            if not s["bent"]:
                for bx, by in burning:
                    d = s["p"] - np.array((bx, by))
                    if np.hypot(*d) < kit.PLUME_WIDTH / 2:
                        side = 1.0 if d[0] >= 0 else -1.0
                        a = np.radians(25) * side
                        v = s["v"]
                        s["v"] = np.array((v[0] * np.cos(a) - v[1] * np.sin(a), v[0] * np.sin(a) + v[1] * np.cos(a)))
                        s["bent"], s["bt"], s["bp"] = True, t, s["p"].copy()
                        break
        shots = [s for s in shots if -20 < s["p"][1] < 560]
        for s in shots:
            glows.append((bolt[0], s["p"][0], s["p"][1], 1.0))
            if s["bt"] is not None and t - s["bt"] < 0.4:
                over.append((bd[min(5, int((t - s["bt"]) * 15))], s["bp"][0], s["bp"][1]))
        pools = []
        for (tt, x, kind) in b27.SMOKERS:
            row, mxx = b27.pool_of(kind, tt, x)
            yy = base + kit.SCREEN - 1 - row
            if -100 < yy < kit.SCREEN + 100:
                pools.append((mxx, yy, 100, 1.0))
        out = kit.frame(img, lt, solids=solids, glows=glows, ship_xy=ship, pools=pools, over=over)
        raster.draw_text(out, 8, 8, "L27 PLUMES AND THE MARLIN  T={:.1f}".format(lt), raster.LABEL)
        frames.append(out.convert("RGB"))
    def frame_at(lt):
        return frames[min(range(len(clock)), key=lambda i: abs(clock[i] - lt))]
    stills = [(26.9, "THE FIRST ROW'S TELL"), (28.0, "ERUPTING, SHOTS BENT"), (41.6, "THE MARLIN BY HER STACK"),
              (43.8, "THE MARLIN OPENED")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 540), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2 UNDER THE VENT LIGHT'S STAND-IN (T=25.5-31.5, 41-44.5)", raster.LABEL)
    for i, (tt, cap) in enumerate(stills):
        big.alpha_composite(frame_at(tt).convert("RGBA").resize((240, 270), Image.Resampling.BOX),
                            (16 + i * 248, sheet.height + 14))
        raster.draw_text(big, 16 + i * 248, sheet.height + 288, cap, raster.LABEL_DIM)
    y1 = sheet.height + 304
    raster.draw_text(big, 16, y1, "1:1 CROPS: A PLUME ERUPTING AND BENDING SHOTS / THE MARLIN OPENED, ITS CRATE", raster.LABEL)
    big.alpha_composite(frame_at(28.1).crop((0, 120, 480, 330)).convert("RGBA"), (16, y1 + 14))
    big.alpha_composite(frame_at(43.8).crop((0, 260, 300, 470)).convert("RGBA"), (16 + 488, y1 + 14))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "l27-props", fps=FPS)


def l26_halo():
    import l26kit                                               # noqa: E402  (the pickups' gold halo)
    return l26kit.gold_halo()


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
