#!/usr/bin/env python3
"""Production art: the Siege Spire's death (design/enemies/bosses/siege-spire, *Death*), redone after
round 34 (14.2b, the user: "Split the arms and let them fall down" / "break it into natural pieces,
detach the arms; it sits on land, so it collapses, it does not sink and drift"). Variant b of the
review (a, the 2D petal sectors burning down to embers, was rejected).

The free spire (phase 3, airborne at its station 76 px above the ash stump on the screen) breaks along
its natural joints and falls to the ground. Its pieces are cut from the production model of
tools/art/siege_spire.py (the free spire's heading 0, the frame it dies in, rendered again in 3D, so
each piece is lit as it turns under the same key light):
  - the six **arms** (the crown petals with their bone claws and crimson emitters) snap off at their
    joints with the crown (0.36 model units from the axis, past the crown's cap),
  - the **crown** (the crown's cap, the upper trunk and its upper spines) cracks into three chunks
    along jagged vertical seams between the arms, the core torn out (it bursts),
  - the **base** (the lower trunk with the torn root stubs) drops straight onto the stump and
    crumbles there into a dust plume.
The camera looks straight down, so a fall is a move away from it: every piece shrinks from the air's
scale to the ground's (the free spire's 1.2 -> 1.0), darkens a little and loses its sheen as it drops,
while gravity (altitude 1 - u^2) carries it down the screen toward the stump (58 px, the station's
height above the anchor, the game's convention for height). The arms fly apart along their own
directions, tumbling tip first; the crown chunks lean out and topple. Each piece has its drop shadow
on the ground (the game's flat 50 % silhouette, 85 % scale, offset 21, 30 px at the air's height),
which slides in under the piece and meets it at the impact. The arms land first, the crown chunks
next, both raising stone dust from the plaza; the last frame is the debris lying flat round the stump
in the thinning dust. No drift and no fade: the game's white flash at the chain's end takes it away.

Over the boss's 3 s death chain (`death_seconds`, 8 frames of 0.375 s) the game plays, centred where
the free spire was:
  siege-spire-death_0   intact: lime cracks of the core's light at the six arm joints and along
                        the crown's seams
  siege-spire-death_1   the arms sag at their cracked joints, the cracks blazing
  siege-spire-death_2   the arms break away; the crown splits, the core bursts
  siege-spire-death_3   falling apart: the arms flying out and tumbling, open space between pieces
  siege-spire-death_4   falling: smaller and lower, the shadows closing in
  siege-spire-death_5   the arms crash flat (dust); the crown chunks about to land
  siege-spire-death_6   the crown chunks crash, the base crumbles onto the stump (dust plumes)
  siege-spire-death_7   the debris lies flat round the stump, the dust thinning
All eight on one canvas larger than the free frame (the pieces fly out of the 244 px square), the
spire's centre at the canvas centre, so the game's centred draw needs no change; one 48-colour
palette, 1-bit alpha for the pieces, the shadows at 50 % and the dust in 4 translucency steps (no
dither); drawn as the free spire's frame is (not additive). The game's chained explosions go over it.

Outputs: assets/sprites/siege-spire-death_0..7 (with --assets); the review
design/enemies/bosses/siege-spire/concept/siege-spire-death-final-r36-b.png/.gif (the frames on a
plain ground, the frames over the Level 14 arena with the ash stump at 1x, and the death over the
arena: the free spire, then the chain).

Run: python3 tools/art/siege_spire_death.py [--assets]   (~25 min on 20 cores the first time; the
renders are cached in the system temp dir, conceptgame-cache/siege-spire-death/;
SPIRE_DEATH_FACTOR=1 renders a quick low-quality preview into its own cache files)
"""
import os
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image

import artkit
from artkit import DESIGN, SPRITES, TAU

import siege_spire as ss  # noqa: E402  (tools/art: the production model's scale, light and render)
from render import boss_models as bm  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_models import V_BODY, V_BONE  # noqa: E402
from render.enemy_rigs import model_rotation  # noqa: E402
from render.sdf import Material, sd_capsule, sd_sphere, union  # noqa: E402

SCRIPT = "siege_spire_death.py"
SOURCE = artkit.source_note(SCRIPT, "round 34 follow-up")
CONCEPT = DESIGN / "enemies" / "bosses" / "siege-spire" / "concept"
CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "siege-spire-death"
VARIANT = "b"
REV = "2"                               # the render cache's revision (the second pass)
FRAMES = 8
CHAIN = 3.0
FACTOR = int(os.environ.get("SPIRE_DEATH_FACTOR", ss.FACTOR))   # a lower factor: a quick preview
UNIT = 1 / (ss.S * ss.FREE_SCALE)       # model units per native px (the free frame's)
PPU = ss.S * ss.FREE_SCALE              # native px per model unit in the air
W, H = 380, 460                         # the render canvas (cropped afterwards, centre kept)
BAND = 64
WORKERS = 12
Z_TOP = 3.4

TOP = bm.SPIRE_HEIGHT
HEAD = model_rotation(np.pi / 2)        # the free frame 0's model rotation
PETALS = [0.3 + k * TAU / 6 for k in range(6)]   # the crown's arms (model space)
R_OPEN = 0.58 + 0.1                     # the petals' reach (the maw open)
R_JOINT = 0.36                          # an arm snaps off here (model units from the axis), past the crown's cap
SEAMS = [PETALS[0] + TAU / 12 + j * TAU / 3 for j in range(3)]   # the crown's seams, between arms
Z_CUT = 1.25                            # the crown breaks off the base here (model units up)
CORE = (0.0, 0.0, TOP + 0.1)
LAND = (0.0, 58.0)                      # px a piece moves on the screen from the air to the ground
GROUND_SCALE = 1.0 / ss.FREE_SCALE      # the ground's scale relative to the air's
SHADOW_OFF = np.array([21.0, 30.0])     # the game's air shadow offset (Shadows.AIR_DX, AIR_DY)
SHADOW_SCALE = 0.85                     # Shadows.SCALE
SHADOW_ALPHA = 0.5                      # Shadows.OPACITY
NM = 10                                 # materials per piece (the spire's 9 + the cut)
LIME = np.array([200, 255, 90], float)
HOT = np.array([245, 255, 210], float)
DUST = np.array([204, 194, 178], float)  # the plaza's pale stone dust
SEED = 1478


def frame_time(k):
    return (k + 0.5) * CHAIN / FRAMES


# =========================================================================== the pieces' motion

def rot_axis(axis, ang):
    """3x3 rotation by ``ang`` about the unit ``axis`` (Rodrigues)."""
    x, y, z = np.asarray(axis, float) / np.linalg.norm(axis)
    c, s, C = np.cos(ang), np.sin(ang), 1 - np.cos(ang)
    return np.array([[c + x * x * C, x * y * C - z * s, x * z * C + y * s],
                     [y * x * C + z * s, c + y * y * C, y * z * C - x * s],
                     [z * x * C - y * s, z * y * C + x * s, c + z * z * C]])


def world_angle(a):
    """A model-space angle on the screen (the free frame's rotation; y up)."""
    return a + HEAD


def unit_dir(a):
    return np.array([np.cos(a), np.sin(a), 0.0]), np.array([-np.sin(a), np.cos(a), 0.0])


_rng = np.random.default_rng(SEED)
ARM_PLAN = []      # per arm: (t0, fall, pitch peak, roll, yaw, spread)
for _i in range(6):
    ARM_PLAN.append((0.55 + _rng.uniform(0, 0.12), 1.15, np.radians(_rng.uniform(62, 88)),
                     np.radians(_rng.uniform(60, 115)) * _rng.choice([-1, 1]),
                     np.radians(_rng.uniform(25, 55)) * _rng.choice([-1, 1]), _rng.uniform(0.5, 0.62)))
CROWN_PLAN = []    # per crown chunk: (t0, fall, tilt, yaw, spread)
for _j in range(3):
    CROWN_PLAN.append((0.8 + 0.06 * _j, 1.3, np.radians(_rng.uniform(30, 38)),
                       np.radians(_rng.uniform(8, 20)) * _rng.choice([-1, 1]), _rng.uniform(0.45, 0.55)))
BASE_PLAN = (0.85, 1.0)


def fall(t, t0, dur):
    """(altitude 1 -> 0 under gravity, the fall's share of time u)."""
    u = float(np.clip((t - t0) / dur, 0, 1))
    return 1 - u * u, u


def placed(pivot, R, out, alt):
    """(T, s): a piece ``out`` (a world vector) from its rest place at altitude ``alt``."""
    s = 1 + (GROUND_SCALE - 1) * (1 - alt)
    T = out + np.array([LAND[0] / PPU, -LAND[1] / PPU, -0.6]) * (1 - alt)
    return T, s


def arm_pose(i, t):
    """(pivot, R, T, s, alt) of arm ``i`` at ``t``, in rest-world coords (screen x right, y up)."""
    t0, dur, peak, roll, yaw, spread = ARM_PLAN[i]
    d, tan = unit_dir(world_angle(PETALS[i]))
    pivot = np.array([0.47 * d[0], 0.47 * d[1], TOP + 0.2])
    alt, u = fall(t, t0, dur)
    if t < 0.45:                                           # cracking: in place
        pitch, r, y, out = 0.0, 0.0, 0.0, 0.0
    elif u <= 0:                                           # sagging at the cracked joint
        pitch, r, y, out = np.radians(8), 0.0, 0.0, 0.03
    elif u < 1:                                            # flung out along its own direction, tumbling
        pitch = np.radians(8) + (peak - np.radians(8)) * u ** 1.2
        r, y, out = roll * u, yaw * u, 0.03 + spread * u ** 0.7
    else:                                                  # slammed flat on the ground
        pitch, r, y, out = np.radians(30), roll, yaw * 1.1, 0.03 + spread
    # the tip goes down (a turn about the tangent), rolling about its own axis, turning (yaw)
    R = rot_axis((0, 0, 1), y) @ rot_axis(tan, pitch) @ rot_axis(d * np.cos(0.5) + [0, 0, np.sin(0.5)], r)
    T, s = placed(pivot, R, out * d, alt)
    return pivot, R, T, s, alt


def crown_pose(j, t):
    t0, dur, tilt, yaw, spread = CROWN_PLAN[j]
    d, tan = unit_dir(world_angle(SEAMS[j] + TAU / 6))
    pivot = np.array([0.3 * d[0], 0.3 * d[1], 1.9])
    alt, u = fall(t, t0, dur)
    if t < 0.75:
        ang, y, out = 0.0, 0.0, 0.0
    elif u < 1:                                            # split, leaning out, toppling as it falls
        ang, y, out = tilt * u ** 1.3, yaw * u, 0.06 + spread * u ** 0.5
    else:                                                  # crashed on its side
        ang, y, out = tilt + np.radians(6), yaw, 0.06 + spread
    R = rot_axis((0, 0, 1), y) @ rot_axis(tan, ang)        # the top falls outward
    T, s = placed(pivot, R, out * d, alt)
    return pivot, R, T, s, alt


def base_pose(t):
    t0, dur = BASE_PLAN
    alt, u = fall(t, t0, dur)
    pivot = np.array([0.0, 0.0, 0.6])
    T, s = placed(pivot, np.eye(3), np.zeros(3), alt)
    return pivot, np.eye(3), T, s, alt


def screen_of(pose, point):
    """Screen px (from the frame's centre, y down) of the rest-world ``point`` of a posed piece."""
    pivot, R, T, s, _ = pose
    w = T + s * (pivot + R @ (np.asarray(point, float) - pivot))
    return np.array([w[0] * PPU, -w[1] * PPU])


def landings():
    """[(time, screen px, size)] of every impact: the arms, the crown chunks, the base."""
    out = []
    for i in range(6):
        t0, dur, *_ = ARM_PLAN[i]
        out.append((t0 + dur, screen_of(arm_pose(i, t0 + dur + 1e-3), arm_pose(i, 0)[0]), "arm"))
    for j in range(3):
        t0, dur, *_ = CROWN_PLAN[j]
        p = crown_pose(j, t0 + dur + 1e-3)
        out.append((t0 + dur, screen_of(p, p[0]), "crown"))
    t0, dur = BASE_PLAN
    out.append((t0 + dur, screen_of(base_pose(t0 + dur), (0, 0, 0.6)), "base"))
    return out


# =========================================================================== the pieces' models

def veins(p):
    """A sparse network of veins through the tissue (model space), 0..1."""
    a = np.sin(6.3 * p[:, 0] + 2.1 * np.sin(4.7 * p[:, 1] + 1.3 * p[:, 2]))
    b = np.sin(5.1 * p[:, 1] + 1.7 * np.sin(3.9 * p[:, 2] + 2.0 * p[:, 0]))
    c = np.sin(4.4 * p[:, 2] + 1.9 * np.sin(5.3 * p[:, 0] - 1.1 * p[:, 1]))
    return np.exp(-((a + b) ** 2) * 9) + np.exp(-((b - c) ** 2) * 9) * 0.7


def cut_material(lime, vein, dark):
    """The broken faces: the creature's interior, a dull violet-grey cross-section lit like the rest,
    faint violet veins fading as it dies (lime while the core still lives)."""
    base = np.array([0.055, 0.045, 0.07]) + np.array([0.78, 1.0, 0.35]) * lime
    return Material(tuple(np.array([0.40, 0.35, 0.45]) * dark), shininess=10, spec=0.08 * dark,
                    emission=tuple(base),
                    emission_pattern=lambda p, n: 1.0 + 5.0 * vein * veins(p))


def arm_model(i):
    """Arm ``i`` (model space): the petal, its claw and its emitter, cut at the joint."""
    a = PETALS[i]
    c, s = np.cos(a), np.sin(a)
    r1 = R_OPEN

    def scene(p):
        d, m = union((sd_capsule(p, (0.18 * c, 0.18 * s, TOP), (r1 * c, r1 * s, TOP + 0.28), 0.17, 0.1), V_BODY),
                     (sd_capsule(p, (r1 * c, r1 * s, TOP + 0.28), ((r1 + 0.14) * c, (r1 + 0.14) * s, TOP + 0.58),
                                 0.09, 0.02), V_BONE), k=0.04)
        d, m = union((d, m), (sd_sphere(p, ((r1 + 0.04) * c, (r1 + 0.04) * s, TOP + 0.36), 0.075), bm.V_LASER))
        keep = R_JOINT - (p[:, 0] * c + p[:, 1] * s)
        m = np.where(keep > d, NM - 1, m)
        return np.maximum(d, keep), m
    return scene


def arm_region(i, p):
    """The space arm ``i`` takes (its petal, claw and emitter and their blends), out from the joint
    (past the crown's cap, so the cap stays whole on the body)."""
    a = PETALS[i]
    c, s = np.cos(a), np.sin(a)
    r1 = R_OPEN
    d = np.minimum.reduce([
        sd_capsule(p, (0.18 * c, 0.18 * s, TOP), (r1 * c, r1 * s, TOP + 0.28), 0.22, 0.15),
        sd_capsule(p, (r1 * c, r1 * s, TOP + 0.28), ((r1 + 0.14) * c, (r1 + 0.14) * s, TOP + 0.58), 0.14, 0.07),
        sd_sphere(p, ((r1 + 0.04) * c, (r1 + 0.04) * s, TOP + 0.36), 0.12)])
    return np.maximum(d, R_JOINT - (p[:, 0] * c + p[:, 1] * s))


def _cut_z(p):
    """The crown/base break: ~Z_CUT, jagged (<= 0 above it)."""
    return Z_CUT - p[:, 2] + 0.12 * np.sin(9 * p[:, 0] + 1.3) * np.sin(8 * p[:, 1] + 0.4)


def body_model(part, cored, glow):
    """The spire without its arms (model space): ``part`` "crown-<j>" (the 120 degree sector between
    two seams above the break, jagged with height; the core torn out once it has burst) or "base"."""
    full, _ = bm.spire(rooted=False, maw=1.0, laser=0.0, torn=0.0, glow=glow)

    def scene(p):
        d, m = full(p)
        keep = -np.min([arm_region(i, p) for i in range(6)], axis=0)    # not an arm
        if part == "base":
            keep = np.maximum(keep, -_cut_z(p))
        else:
            j = int(part.split("-")[1])
            r = np.sqrt(p[:, 0] ** 2 + p[:, 1] ** 2)
            phi = np.arctan2(p[:, 1], p[:, 0]) + 0.09 * np.sin(6.5 * p[:, 2] + 2 * j) + 0.05 * np.sin(11 * r)
            da = np.abs((phi - (SEAMS[j] + TAU / 6) + np.pi) % TAU - np.pi)
            wedge = r * np.sin(np.clip(da - TAU / 6, -np.pi / 2, np.pi / 2))
            keep = np.maximum.reduce([keep, wedge, _cut_z(p)])
            if cored:
                keep = np.maximum(keep, 0.24 - np.sqrt(((p - sdf.vec(*CORE)) ** 2).sum(axis=1)))
        m = np.where(keep > d, NM - 1, m)
        return np.maximum(d, keep), m
    return scene


def piece_mats(glow, laser, lime, alt, vein):
    """The spire's materials for a piece at altitude ``alt``: darker and duller nearer the ground."""
    dark = 0.8 + 0.2 * alt
    sheen = 0.2 + 0.8 * alt
    m = bm.mats("siege-spire", glow, laser=laser, body_pattern=bm.radial_ribs(16, 0.1))
    m = [replace(x, albedo=tuple(np.array(x.albedo) * dark), spec=x.spec * sheen, metal=x.metal * sheen)
         for x in m]
    m = m + [cut_material(lime, vein, dark)]
    assert len(m) == NM
    return m


def frame_state(k):
    """The pieces of frame ``k``: [(kind, index, pose, glow, laser, lime, cored, vein)]."""
    t = frame_time(k)
    glow = 1.25 if k < 2 else 0.25
    lime = (0.6, 1.2, 1.0)[k] if k < 3 else 0.0
    laser = 0.3 if k == 0 else 0.0
    vein = (1.0, 1.0, 1.0, 0.85, 0.7, 0.55, 0.4, 0.3)[k]
    arm_lime = lime * (0.25 if k == 2 else 1.0)            # the torn ends cool fast off the core
    out = [("arm", i, arm_pose(i, t), glow, laser, arm_lime, False, vein) for i in range(6)]
    crown_lime = lime * (0.4 if k == 2 else 1.0)          # the split faces: a glow, not a blaze
    out += [("crown", j, crown_pose(j, t), glow, laser, crown_lime, k >= 2, vein) for j in range(3)]
    if t < sum(BASE_PLAN):                                 # it crumbles on the stump
        out.append(("base", 0, base_pose(t), glow, laser, lime, False, vein))
    return out


def _wrap(fn, inv, rot_n):
    if fn is None:
        return None
    return lambda p, n, f=fn: f(inv(p), rot_n(n))


def scene_of(state):
    """(scene, materials, bounds) of a frame: every piece moved by its pose, its materials evaluated in
    its own model space (piece i uses materials i*NM .. i*NM+NM-1)."""
    items, mats, bounds = [], [], []
    for n, (kind, idx, (pivot, R, T, s, alt), glow, laser, lime, cored, vein) in enumerate(state):
        if kind == "arm":
            base = arm_model(idx)
            d, _ = unit_dir(world_angle(PETALS[idx]))
            centre, radius = np.array([0.5 * d[0], 0.5 * d[1], TOP + 0.25]), 0.45
        elif kind == "crown":
            base = body_model(f"crown-{idx}", cored, glow)
            d, _ = unit_dir(world_angle(SEAMS[idx] + TAU / 6))
            centre, radius = np.array([0.3 * d[0], 0.3 * d[1], 1.95]), 1.15
        else:
            base = body_model("base", False, glow)
            centre, radius = np.array([0.0, 0.0, 0.6]), 1.2

        def inv(p, R=R, T=T, s=s, pivot=pivot):
            x = pivot + ((p - T) / s - pivot) @ R              # rest-world (R^T applied to rows)
            return sdf.rotate_z(x, HEAD)                       # model space

        def rot_n(nn, R=R):
            return sdf.rotate_z(nn @ R, HEAD)

        def piece(p, base=base, inv=inv, s=s, off=n * NM):
            d, mm = base(inv(p))
            return d * s, mm + off

        wc = T + s * (pivot + R @ (centre - pivot))
        items.append((piece, wc, radius * s))
        bounds.append((wc, radius * s))
        for mat in piece_mats(glow, laser, lime, alt, vein):
            mats.append(replace(mat, pattern=_wrap(mat.pattern, inv, rot_n),
                                emission_pattern=_wrap(mat.emission_pattern, inv, rot_n)))

    def scene(p):
        d = m = None
        for piece, wc, rad in items:
            bound = np.sqrt(((p - wc) ** 2).sum(axis=1)) - rad
            g = bm.bounded(p, bound, 0.1, piece)
            d, m = g if d is None else union((d, m), g)
        return d, m
    return scene, mats, bounds


# =========================================================================== render

def _band(task):
    k, r0, rows = task
    scene, mats, _ = scene_of(frame_state(k))
    extent = UNIT * W
    by = -((r0 + rows / 2) - H / 2) * UNIT
    light = sdf.KEY_POS * (ss.b6.FREE_EXT / 2.3)
    key = (light - sdf.vec(0.0, by, 0.0)) / (extent / 2.3)
    return sdf.render(scene, mats, (W * FACTOR, rows * FACTOR), extent, center=(0.0, by), z_top=Z_TOP,
                      key_pos=tuple(key))


def _ids(k):
    """The piece each native pixel of frame ``k`` shows (-1: none) and its depth, by marching the
    frame's scene."""
    scene, _, _ = scene_of(frame_state(k))
    w, h = W, H
    px = UNIT
    xs = (np.arange(w) + 0.5 - w / 2) * px
    ys = -(np.arange(h) + 0.5 - h / 2) * px
    gx, gy = np.meshgrid(xs, ys)
    o = np.stack([gx.ravel(), gy.ravel(), np.full(w * h, Z_TOP)], axis=-1)
    t = np.zeros(w * h)
    hit = np.zeros(w * h, bool)
    active = np.arange(w * h)
    for _ in range(110):
        if active.size == 0:
            break
        p = o[active].copy()
        p[:, 2] -= t[active]
        d, _ = scene(p)
        t[active] += d * 0.8
        h_ = d < px * 0.25
        hit[active[h_]] = True
        active = active[~(h_ | (t[active] > 2 * Z_TOP))]
    ids = np.full(w * h, -1)
    idx = np.nonzero(hit)[0]
    p = o[idx].copy()
    p[:, 2] -= t[idx]
    _, m = scene(p)
    ids[idx] = m // NM
    depth = np.where(hit, t, 1e3)
    return np.stack([ids.reshape(h, w), depth.reshape(h, w)])


def _path(kind, k):
    return CACHE / f"{kind}-{VARIANT}{REV}-{k}-x{FACTOR}.npy"


def render_frames():
    """The 3D frames (native RGBA, W x H) and their piece maps, cached."""
    CACHE.mkdir(parents=True, exist_ok=True)
    tasks, his = [], {}
    for k in range(FRAMES):
        if _path("frame", k).exists():
            his[k] = np.load(_path("frame", k))
            continue
        _, _, bounds = scene_of(frame_state(k))
        for r0 in range(0, H, BAND):
            rows = min(BAND, H - r0)
            y0, y1 = (H / 2 - r0 - rows) * UNIT, (H / 2 - r0) * UNIT     # the band's world y range
            if any(c[1] + r > y0 and c[1] - r < y1 for c, r in bounds):
                tasks.append((k, r0, rows))
    ids_needed = [k for k in range(FRAMES) if not _path("ids", k).exists()]
    if tasks or ids_needed:
        print(f"rendering {len(tasks)} bands, {len(ids_needed)} piece maps")
        with ProcessPoolExecutor(max_workers=WORKERS) as pool:
            bands = list(pool.map(_band, tasks, chunksize=1))
            idmaps = list(pool.map(_ids, ids_needed))
        for k in {t[0] for t in tasks}:
            hi = np.zeros((H * FACTOR, W * FACTOR, 4))
            for (kk, r0, rows), band in zip(tasks, bands):
                if kk == k:
                    hi[r0 * FACTOR:(r0 + rows) * FACTOR] = band
            np.save(_path("frame", k), hi)
            his[k] = hi
        for k, ids in zip(ids_needed, idmaps):
            np.save(_path("ids", k), ids)
    natives = [np.array(artkit.native(his[k], FACTOR)).astype(np.float64) for k in range(FRAMES)]
    ids = [np.load(_path("ids", k)) for k in range(FRAMES)]
    return natives, ids


# =========================================================================== 2D: shadows, cracks, burst, dust

def shadows(img, pieces, k):
    """Each piece's drop shadow on the ground, under the pieces: its silhouette (as the frame shows
    it) at 85 % -> 100 % scale and offset 21, 30 px -> 0 as it falls, so it meets the piece at the
    impact; one flat 50 % layer (overlapping shadows do not stack)."""
    ids = pieces[0].astype(int)
    h, w = ids.shape
    state = frame_state(k)
    shade = np.zeros((h, w), bool)
    for n, (_, _, pose, *_) in enumerate(state):
        alt = pose[4]
        ys, xs = np.nonzero(ids == n)
        if len(xs) == 0 or alt <= 0.02:
            continue
        sc = SHADOW_SCALE + (1 - SHADOW_SCALE) * (1 - alt)
        cx, cy = xs.mean(), ys.mean()
        dx, dy = SHADOW_OFF * alt
        tx = np.round(cx + (xs - cx) * sc + dx).astype(int)
        ty = np.round(cy + (ys - cy) * sc + dy).astype(int)
        ok = (tx >= 0) & (tx < w) & (ty >= 0) & (ty < h)
        shade[ty[ok], tx[ok]] = True
    # close the pinholes a scaled-down scatter leaves
    shade = shade | (np.roll(shade, 1, 0) & np.roll(shade, -1, 0)) | (np.roll(shade, 1, 1) & np.roll(shade, -1, 1))
    under = shade & (img[..., 3] == 0)
    img[under, :3] = 0
    img[under, 3] = round(255 * SHADOW_ALPHA)


def cracks(img, pieces, k):
    """Lime cracks where two pieces meet (the arm joints and the crown's seams), not where one only
    passes in front of another."""
    a, depth = pieces[0].astype(int), pieces[1]
    edge = np.zeros(a.shape, bool)
    for dy, dx in ((0, 1), (1, 0)):
        b = np.roll(a, (-dy, -dx), axis=(0, 1))
        near = np.abs(np.roll(depth, (-dy, -dx), axis=(0, 1)) - depth) < 4 * UNIT
        e = (a >= 0) & (b >= 0) & (a != b) & near
        edge |= e | np.roll(e, (dy, dx), axis=(0, 1))
    if k == 0:                                             # the first cracks: broken lines
        rng = np.random.default_rng(SEED + 7)
        noise = raster.fbm(a.shape[1], a.shape[0], 6, SEED + 9, octaves=2, period=False)
        edge &= (noise > 0.45) | (rng.random(a.shape) < 0.1)
        thin = edge
    else:                                                  # blazing: a halo round the line
        thin = edge
        edge = edge | np.roll(edge, 1, 0) | np.roll(edge, -1, 0) | np.roll(edge, 1, 1) | np.roll(edge, -1, 1)
    on = edge & (img[..., 3] == 255)
    img[on, :3] = img[on, :3] * 0.25 + LIME * 0.75
    core = thin & (img[..., 3] == 255) & (k > 0)
    img[core, :3] = HOT


def burst(img):
    """The core bursting (frame 2): a ragged lime-white blast at the centre, rays out along the seams."""
    h, w = img.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    cx, cy = w / 2, h / 2 - 2
    ang = np.arctan2(yy - cy, xx - cx)
    r = np.hypot(xx - cx, yy - cy)
    rays = 0.0
    for a in SEAMS + PETALS:
        wa = -world_angle(a)                               # screen y down
        rays = np.maximum(rays, np.clip(1 - np.abs((ang - wa + np.pi) % TAU - np.pi) / 0.16, 0, 1))
    noise = raster.fbm(w, h, 5, SEED + 3, octaves=2, period=False)
    reach = 20 + 26 * rays + 8 * (noise - 0.5)
    inside = r < reach
    hot = r < reach * 0.55
    img[inside, :3] = LIME
    img[hot, :3] = HOT
    img[inside, 3] = 255


def dust_plan():
    """(t0, x0, y0, vx, vy, r0, r1, life, peak) per plume, px from the frame's centre (y down): two
    where each arm lands, three where each crown chunk lands, five big ones where the base
    crumbles on the stump."""
    rng = np.random.default_rng(SEED + 21)
    plan = []
    for tl, (x, y), kind in landings():
        n, (r0, r1), sp, life, peak = {"arm": (2, (10, 28), 20, 1.0, 0.7),
                                       "crown": (3, (14, 40), 30, 0.9, 0.7),
                                       "base": (5, (20, 56), 40, 0.95, 0.75)}[kind]
        for q in range(n):
            a = q * TAU / n + rng.uniform(-0.6, 0.6)
            plan.append((tl + rng.uniform(0, 0.05), x + np.cos(a) * r0 * 0.6, y + np.sin(a) * r0 * 0.5,
                         np.cos(a) * sp, np.sin(a) * sp * 0.75 - 4, r0, r1 * rng.uniform(0.9, 1.1), life, peak))
    return plan


def dust(img, t):
    """The impact dust at ``t``: a few large soft plumes of pale stone dust, billowing out on the
    impact and thinning away (4 translucency steps, no dither, high key: no dark speckle)."""
    h, w = img.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    noise = raster.fbm(w, h, 12, SEED + 11, octaves=3, period=False)
    light = np.array([-0.5, -0.6, 0.62]) / np.linalg.norm([-0.5, -0.6, 0.62])
    rgb = np.zeros((h, w, 3))
    alpha = np.zeros((h, w))
    for t0, x0, y0, vx, vy, r0, r1, life, peak in dust_plan():
        age = t - t0
        if age < 0 or age >= life:
            continue
        u = age / life
        ease = 1 - (1 - u) ** 3                           # thrown out fast, slowing
        r = r0 + (r1 - r0) * ease
        cx = w / 2 + x0 + vx * life * ease / 3
        cy = h / 2 + y0 + vy * life * ease / 3
        nx, ny = (xx - cx) / r, (yy - cy) / r
        rr = nx ** 2 + ny ** 2
        edge = 1 - rr + (noise - 0.5) * 0.8
        nz = np.sqrt(np.clip(1 - rr, 0, 1))
        lit = np.clip(nx * light[0] + ny * light[1] + nz * light[2], 0, 1)
        a = np.clip(edge * 1.4, 0, 1) * peak * min(1, age / 0.06) * (1 - u) ** 1.3
        col = DUST[None, None, :] * (0.9 + 0.14 * lit[..., None])
        rgb = rgb * (1 - a[..., None]) + col * a[..., None]
        alpha = alpha + a * (1 - alpha)
    q = np.floor(np.clip(alpha, 0, 1) * 4 + 0.35) / 4
    over = q > 0
    base_a = img[..., 3] / 255
    out_a = q + base_a * (1 - q)
    img[over, :3] = (rgb[over] * q[over, None] + img[over, :3] * (base_a[over, None] * (1 - q[over, None]))) \
        / np.maximum(out_a[over, None], 1e-6)
    img[..., 3] = np.round(out_a * 255)


# =========================================================================== frames

def frames():
    natives, ids = render_frames()
    out = []
    for k, img in enumerate(natives):
        img = img.copy()
        if k < 2:
            cracks(img, ids[k], k)
        if k == 2:
            burst(img)
        shadows(img, ids[k], k)
        dust(img, frame_time(k))
        out.append(img)
    # crop to the content, keeping the spire's centre at the canvas centre
    ys, xs = np.nonzero(np.max([f[..., 3] for f in out], axis=0) > 0)
    hw = int(np.ceil(max(W / 2 - xs.min(), xs.max() + 1 - W / 2))) + 2
    hh = int(np.ceil(max(H / 2 - ys.min(), ys.max() + 1 - H / 2))) + 2
    if hw > W // 2 or hh > H // 2:
        raise SystemExit(f"the pieces leave the render canvas ({2 * hw} x {2 * hh} > {W} x {H})")
    x0, y0 = W // 2 - hw, H // 2 - hh
    imgs = [Image.fromarray(np.clip(f[y0:y0 + 2 * hh, x0:x0 + 2 * hw], 0, 255).astype(np.uint8), "RGBA")
            for f in out]
    return artkit.quantize_set(imgs, 48)


def arena():
    """The Level 14 arena at the halt with the ash stump and the dead roots, as the game shows it after
    the lift (tools/art/backdrop_l14.py's composite), and the free spire's place on it (x, y down)."""
    import backdrop_l14 as l14
    bd, _ = l14.level_backdrop()
    img = l14.composite(bd, l14.ARENA - 1, view=l14.HALT_SCROLL, units=True, phase="stump").convert("RGBA")
    return img, (240, 120)


def review(fs):
    artkit.REVIEW_ROUND = "r36"
    fw, fh = fs[0].size
    field, (sx, sy) = arena()
    over = []
    for f in fs:                                           # each frame over the arena at 1x, cropped
        g = field.copy()
        g.alpha_composite(f, (sx - fw // 2, sy - fh // 2))
        over.append(g.crop((sx - fw // 2, max(0, sy - fh // 2), sx + fw // 2, sy + fh // 2)))
    rows = [("BREAKING AT THE JOINTS 0-3 (0-1.5 S)", fs[:4], 1, False),
            ("FALLING, CRASHING, SETTLING 4-7 (1.5-3 S)", fs[4:], 1, False),
            ("OVER THE ARENA AND THE ASH STUMP, 1X: 0-3", over[:4], 1, False),
            ("OVER THE ARENA AND THE ASH STUMP, 1X: 4-7", over[4:], 1, False)]
    sheet = artkit.review_sheet("SIEGE SPIRE - DEATH (PRODUCTION, REDO)", rows, width=4 * (fw + 6) + 40,
                                batch="round 34 follow-up")
    free = Image.open(SPRITES / "siege-spire-free_0.png").convert("RGBA")
    gif = []
    for _ in range(4):                                     # the free spire, alive, with its shadow
        g = field.copy()
        a = np.array(free)
        sh = np.zeros_like(a)
        sh[..., 3] = np.where(a[..., 3] > 127, round(255 * SHADOW_ALPHA), 0)
        shs = Image.fromarray(sh, "RGBA").resize((round(free.width * SHADOW_SCALE),) * 2, Image.NEAREST)
        g.alpha_composite(shs, (sx + 21 - shs.width // 2, sy + 30 - shs.height // 2))
        g.alpha_composite(free, (sx - free.width // 2, sy - free.height // 2))
        gif.append(g)
    for f in fs:
        for _ in range(3):
            g = field.copy()
            g.alpha_composite(f, (sx - fw // 2, sy - fh // 2))
            gif.append(g)
    for _ in range(4):
        gif.append(field.copy())
    gif = [g.crop((40, 0, 440, 400)) for g in gif]
    artkit.save_review(sheet, gif, CONCEPT, "siege-spire-death", fps=8, variant=VARIANT)


def main(args):
    fs = frames()
    print(f"frames: {len(fs)} x {fs[0].size[0]}x{fs[0].size[1]}")
    if "--assets" in args:
        artkit.write_frames("siege-spire-death", fs, SOURCE)
        print(f"assets: sprites/siege-spire-death_0..{len(fs) - 1}")
    review(fs)


if __name__ == "__main__":
    main(sys.argv[1:])
