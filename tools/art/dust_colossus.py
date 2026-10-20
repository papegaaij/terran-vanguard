#!/usr/bin/env python3
"""Production art: the Dust Colossus, Act 3's boss in Level 21 (design/enemies/bosses/dust-colossus; M7 part H
batch, step ART-H2, review round 44). Straight to production from the chosen concept round 36 b "Sinkhole colony"
(tools/concept/bosses_r36.py: the fleshy collar, the lime throat in its two rings of bone teeth, the four bone
mandibles, the eight violet-tipped palps, the bone/violet/lime Vrell scheme; its helpers imported unchanged) on the
frozen render package.

What changed from the concept (to the README's spec, pending the user's review):
* drawn at 72 px per model unit (the concept 70): the collar ~115 px across, the teeth and palps' ring ~150 (the
  spec's "mouth ~150 px", hitbox a 120 px circle); the mandibles' open reach trimmed from the concept's 1.75 units to 1.5 so
  the open frames stay in the one-page budget (~230 px flung open);
* five poses, each its own render under the fixed key light: the four edge breaches lean 30 degrees into the
  field (FRONT leans down the screen, LEFT leans right, RIGHT leans left, REAR leans up) and stand out of the
  sand on the body's neck, which runs back under the sand toward its edge with violet vein ridges on its back;
  UNDER (phase 3) stands straight up and faces the camera, as the concept;
* the sand: where the body crosses the ground the cut is sand-coloured rubble; sand clumps slide off the shut
  mandibles in the rise and pour over the collar into the throat in the dive;
* a mild perspective (camera 8 units up, artkit.perspective) so what rises reads larger, the rearing death too;
* the bite: its tell lights the teeth white-hot as the jaws start to close; the snap clenches them past shut;
* the funnel (the ground collapsing round a breach), the crater it leaves, the tell's bulge, phase 3's marker,
  the death's bursts are 2D height fields lit like the Colossus ridge (tools/art/l20_props.py, imported).

Every model set shares one centre, the mouth's (the sim's breach point, the gullet's centre at full rise), and one
render canvas (the same key light); each set is then cropped by equal margins about that centre down to its
visible pixels. Pose index p: 0 FRONT, 1 LEFT, 2 RIGHT, 3 REAR, 4 UNDER (the sim's BreachArena order).

Outputs (assets/sprites/, or build/art-held/sprites/ (pivots build/art-held/pivots/) while no data claims the
root `dust-colossus`, vanguard.pipeline.SpriteUse: rerun once the boss's data.yaml exists):
  dust-colossus-shut_0..29   p * 6 + state, the mouth shut: 0 rise (a third out, sand sliding off), 1 rise (two
                             thirds), 2 risen shut (the end of the rise; the open's first frame), 3 the bite's snap
                             (clenched past shut), 4 dive (sinking, sand pouring over the collar), 5 dive (the
                             mandible tips last)
  dust-colossus-mouth_0..14  p * 3 + state, the mouth open: 0 half open, 1 open (the gullet lit: the weak spot),
                             2 the bite's tell (teeth white-hot, the jaws starting to close)
  dust-colossus-gullet_0..4  additive, per pose: the gullet's lime glow (the ring's 0.5 s glow; the game pulses
                             its alpha), centred like the mouth
  dust-colossus-death_0..5   the UNDER pose (the death is always phase 3's mouth): 0 rears up, mandibles flung
                             wide; 1, 2 shudders (tilted left / right, lime ichor bursting from the collar); 3-5
                             sinks, mandibles limp, sand pouring in (the collapsed crater is a Level 21 prop)
  dust-colossus-vent_0..3    a phase-2 spine vent (40x40): 0 rising out of the sand, 1 up (lime dim), 2 venting
                             (the burst's flash), 3 destroyed (the top torn off, ichor; stays dead)
  dust-colossus-bulge_0..3   the tell's sand bulge at a breach point, growing over the 2 s and cracking with
                             violet light
  dust-colossus-marker_0..3  phase 3's marker under the ship (90 px): a crack ring and a small bulge growing over
                             the 1.5 s, violet light in the cracks
  dust-colossus-funnel_0..5  the ground at a breach, centred on the mouth: 0 collapsing (the rise), 1-3 the
                             funnel with the sand sliding in (a 3-frame loop while the mouth is up), 4 refilling (the
                             dive), 5 the crater that stays and scrolls on with the ground
  dust-colossus-burst_0..7   the death's chained eruptions along the ridge: a dust plume with lime ichor flung out
                             (stepped translucency), by age
  dust-colossus-rock_0..15   a debris rock (20-28 px, `air`), shape * 4 + tumble step (4 shapes)
  assets/pivots/dust-colossus.json   per pose: the lean, the gullet's ellipse, the neck (where the body leaves
                             the sand: the ridge joins there), the collar's radius
  assets/sprites/intel/boss-dust-colossus.png   via tools/art/intel.py (the open UNDER mouth's outline)
  design/enemies/bosses/concept/dust-colossus-final-r44-a.png/.gif   the review

How the game draws it, back to front: the funnel at the breach point (the crater after the dive, scrolling with
the ground), the shut / mouth frames centred on the point, the gullet glow added over an open mouth; the vents on
the ridge's path; the bulge / marker on the ground before a breach; the death frames replace the mouth.

Run: python3 tools/art/dust_colossus.py [--review]   (~13 min on 20 cores; renders cached in the system temp dir,
conceptgame-cache/dust-colossus/; --review rebuilds the review from the written files)
"""
import contextlib
import hashlib
import math
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

import act3kit  # noqa: E402  (tools/art: quantize_bright keeps the lit gullet and teeth)
import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import backdrop_l15 as b15  # noqa: E402  (tools/art: canvases, ramps)
import l20_props as l20  # noqa: E402  (tools/art: the ridge's finishing, the ochre ramp, the ridge sprites)
import storm_units as su  # noqa: E402  (tools/art: noisy blobs, stepped translucency)
import bosses_r36 as r36  # noqa: E402  (concept round 36, imported unchanged: the polar fold, the teeth, the scheme)
import ui_r11  # noqa: E402  (concept: the edge warning, for the review GIF only)
from render import boss_models as bm  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_models import V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SEAM  # noqa: E402
from render.sdf import Material, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, union  # noqa: E402

SCRIPT = "dust_colossus.py"
BATCH = "M7 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r44"
ROOT_NAME = "dust-colossus"
CONCEPT = DESIGN / "enemies" / "bosses" / "concept"
DATA = DESIGN / "enemies" / "bosses" / "dust-colossus" / "data.yaml"
HELD = ROOT / "build" / "art-held"
CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "dust-colossus"

S = 72.0                                   # px per model unit
CANVAS = 336                               # every model set's render canvas (one key light), cropped after
CAMERA = 8.0                               # perspective: model units above the ground
POSES = ("front", "left", "right", "rear", "under")
LEAN = math.radians(30)
LEAN_DIR = {"front": (0.0, -1.0), "left": (1.0, 0.0), "right": (-1.0, 0.0), "rear": (0.0, 1.0), "under": None}
LIFT = {"front": 0.44, "left": 0.44, "right": 0.44, "rear": 0.44, "under": 0.14}
DEPTH = 1.25                               # how far along its axis the mouth sinks at rise 0
VENT, ROCK, BULGE, MARKER, FUNNEL, BURST, GLOW = 48, 32, 160, 96, 240, 64, 112
FUNNEL_R = 104.0

CLOSED = np.array([(0.78, 0.0, 0.28), (0.62, 0.12, 0.62), (0.36, 0.2, 0.68), (0.2, 0.14, 0.52)])
OPENED = np.array([(0.78, 0.0, 0.2), (1.08, 0.06, 0.32), (1.34, 0.2, 0.26), (1.48, 0.42, 0.14)])
RADII = (0.19, 0.15, 0.1, 0.04)
GULLET_R = 0.48

OCHRE = l20.OCHRE
# the basin floor's sand (tools/art/backdrop_l21.py's arena tile, its median ~(153, 137, 103)): the ground pieces
# and the sand on the mouth are drawn in it so they sit on the floor
FLOOR = [(84, 72, 54), (118, 104, 78), (146, 131, 99), (164, 149, 114), (186, 172, 136), (210, 198, 164)]
LIME = np.array(l20.LIME, float)
VIOLET = np.array(raster.hexrgb("9a4dff"), float)
ROCKS = [(70, 44, 30), (104, 68, 44), (138, 94, 62), (170, 122, 84), (196, 150, 108)]

# extra material slots after boss_models.mats' nine (7 Vrell + crimson + dead tissue)
W_SAND, W_HOT, W_ICHOR, W_ROCK = 9, 10, 11, 12
W_DEAD = 8


# =========================================================================== models

def mats(glow=1.0, weak=0.75, hot=0.0):
    m = bm.mats("dust-colossus-b", glow, weak_k=weak)
    sand = np.array(FLOOR[3], float) / 255
    m.append(Material(tuple(sand), metal=0.0, shininess=6, spec=0.04,                       # W_SAND
                      pattern=lambda p, n: 0.93 + 0.07 * np.sin(p[:, 0] * 9) * np.sin(p[:, 1] * 11)))
    m.append(Material((0.95, 0.92, 0.84), metal=0.1, shininess=60, spec=0.6,                 # W_HOT: lit teeth
                      emission=(1.6 * hot, 1.5 * hot, 1.25 * hot)))
    m.append(Material((0.2, 0.35, 0.05), metal=0.0, shininess=40, spec=0.5,                  # W_ICHOR
                      emission=tuple(LIME / 255 * 1.1)))
    m.append(Material(tuple(np.array(ROCKS[2], float) / 255), metal=0.05, shininess=18, spec=0.25,   # W_ROCK
                      pattern=lambda p, n: 0.78 + 0.22 * np.sin(p[:, 0] * 33) * np.sin(p[:, 1] * 29 + p[:, 2] * 17)))
    return m


def rotation(d, angle):
    """Model -> world rotation tilting the model's +z toward the world direction d (x right, y up)."""
    if d is None or angle == 0:
        return np.eye(3)
    k = np.array([-d[1], d[0], 0.0]) / math.hypot(*d)
    K = np.array([[0, -k[2], k[1]], [k[2], 0, -k[0]], [-k[1], k[0], 0]])
    return np.eye(3) + math.sin(angle) * K + (1 - math.cos(angle)) * K @ K


def sand_blobs(kind, amount, seed):
    """Sand clumps in model space: ``slide`` on the shut mandibles' outer slopes (the rise), ``pour`` over the
    collar into the throat (the dive)."""
    rng = np.random.default_rng(seed)
    out = []
    if amount <= 0:
        return out
    n = int(round((12 if kind == "slide" else 8) * amount))
    for i in range(n):
        a = rng.uniform(0, TAU)
        if kind == "slide":
            r, z, rad = rng.uniform(0.5, 0.95), rng.uniform(0.3, 0.55), rng.uniform(0.1, 0.16)
        else:
            r = rng.uniform(0.55, 0.9)
            z, rad = rng.uniform(0.2, 0.35), rng.uniform(0.09, 0.15)
        out.append((r * math.cos(a), r * math.sin(a), z, rad))
    return out


def ichor_spots(count, seed):
    rng = np.random.default_rng(seed)
    out = []
    for _ in range(count):
        a = rng.uniform(0, TAU)
        out.append((0.62 * math.cos(a), 0.62 * math.sin(a), rng.uniform(0.18, 0.32), rng.uniform(0.05, 0.09)))
    return out


def mouth_scene(pose, rise=1.0, open_=0.0, sand=(None, 0.0), tilt=None, lift=0.0, droop=0.0, ichor=0, hot=False,
                seed=0):
    """Colossus b's mouth (bosses_r36.colossus_b re-laid) for a pose: the model tilted into the field, lifted,
    sunk along its own axis by the rise, cut by the ground (z = 0, the cut sand-coloured)."""
    R = rotation(LEAN_DIR[pose], LEAN)
    if tilt is not None:
        R = rotation(tilt[0], tilt[1]) @ R
    lift_v = np.array([0.0, 0.0, LIFT[pose] + lift])
    sink = np.array([0.0, 0.0, (1.0 - rise) * DEPTH])
    o = open_
    chain = CLOSED + o * (OPENED - CLOSED)
    chain[:, 2] -= droop * np.array([0.0, 0.12, 0.26, 0.38])
    teeth = W_HOT if hot else V_BONE
    blobs = sand_blobs(sand[0], sand[1], 300 + seed) if sand[0] else []
    spots = ichor_spots(ichor, 400 + seed)

    def scene(p):
        q = (p - lift_v) @ R + sink
        items = [
            (sd_ellipsoid(q, (0, 0, -0.55), (0.95, 0.95, 0.5)), V_DARK),
            (sd_capsule(q, (0, 0, -1.25), (0, 0, -3.2), 0.8, 0.88), V_BODY),              # the neck
            (np.sqrt((np.sqrt(q[:, 0] ** 2 + q[:, 1] ** 2) - 0.6) ** 2 + (q[:, 2] - 0.1) ** 2) - 0.2, V_BODY),
            (sd_cylinder_z(q, (0, 0, 0.02), GULLET_R, 0.05), V_EYE),
            r36._teeth(q, 12, 0.46, 0.14, 0.2, 0.06, teeth),
            r36._teeth(q, 12, 0.32, 0.06, 0.14, 0.045, teeth, offset=TAU / 24),
        ]
        qn = r36._polar(q, 5, offset=0.3)                                             # vein ridges on the neck
        items.append((sd_capsule(qn, (0.84, 0, -0.5), (0.9, 0.0, -2.6), 0.05, 0.06), V_GLOW))
        qm = r36._polar(q, 4, offset=np.pi / 4)
        items.append((sd_sphere(qm, (0.8, 0, 0.2), 0.22), V_SEAM))
        for i in range(3):
            items.append((sd_capsule(qm, chain[i], chain[i + 1], RADII[i], RADII[i + 1]), V_BONE))
        q8 = r36._polar(q, 8, offset=0.0)
        pr = 0.95 + 0.25 * max(o, 0.0)
        pz = 0.42 - 0.1 * max(o, 0.0) - 0.15 * droop
        items.append((sd_capsule(q8, (0.66, 0, 0.18), (pr, 0.0, pz), 0.06, 0.03), V_BODY))
        items.append((sd_sphere(q8, (pr + 0.02, 0.0, pz + 0.01), 0.055), V_GLOW))
        for x, y, z, r in spots:
            items.append((sd_sphere(q, (x, y, z), r), W_ICHOR))
        d, m = union(*items, k=0.04)
        sand_d = []
        if blobs:                                   # flattened clumps (ellipsoids half as high as wide)
            sand_d.append(np.min(np.stack([sd_ellipsoid(q, (x, y, z), (r, r, r * 0.55)) for x, y, z, r in blobs]),
                                 axis=0))
        if sand[0] == "pour":                       # a mound of sand pouring over the collar into the throat
            a = sand[1]
            sand_d.append(sd_ellipsoid(q, (0, 0, 0.1 + 0.1 * a), (0.3 + 0.4 * a, 0.3 + 0.4 * a, 0.16 + 0.08 * a)))
        ground = p[:, 2] - 0.02 * np.sin(p[:, 0] * 23) * np.sin(p[:, 1] * 19)
        sand_d.append(np.maximum(ground - 0.07, d - 0.14))     # sand piled against the body at the ground line
        ds = np.min(np.stack(sand_d), axis=0)
        h = np.clip(0.5 + 0.5 * (ds - d) / 0.06, 0, 1)
        m = np.where(ds < d, W_SAND, m)
        d = ds * (1 - h) + d * h - 0.06 * h * (1 - h)
        return np.maximum(d, -ground), m
    return artkit.perspective(scene, CAMERA)


SHUT_STATES = [  # (rise, open, sand, hot)
    dict(rise=0.36, open_=0.0, sand=("slide", 1.0)),
    dict(rise=0.7, open_=0.0, sand=("slide", 0.6)),
    dict(rise=1.0, open_=0.0, sand=("slide", 0.15)),
    dict(rise=1.0, open_=-0.1),
    dict(rise=0.62, open_=0.0, sand=("pour", 0.6)),
    dict(rise=0.3, open_=0.0, sand=("pour", 1.0)),
]
MOUTH_STATES = [dict(open_=0.5), dict(open_=1.0), dict(open_=0.78, hot=True)]
DEATH_STATES = [  # (scene kwargs, glow, weak)
    (dict(lift=0.32, open_=1.12), 1.3, 1.2),
    (dict(open_=0.9, tilt=((1.0, 0.0), 0.16), ichor=6), 1.0, 1.0),
    (dict(open_=0.7, tilt=((-0.7, -0.7), 0.16), ichor=11), 0.8, 0.7),
    (dict(rise=0.82, open_=0.45, droop=0.6, ichor=11, sand=("pour", 0.3)), 0.45, 0.35),
    (dict(rise=0.62, open_=0.3, droop=0.9, ichor=6, sand=("pour", 0.55)), 0.2, 0.1),
    (dict(rise=0.42, open_=0.22, droop=1.0, sand=("pour", 0.8)), 0.0, 0.0),
]


def vent_scene(state):
    """A spine vent: a chitin chimney with three bone spines, the lime vent on top, violet veins at its base."""
    sink = 0.2 if state == 0 else 0.0

    def scene(p):
        q = p + np.array([0.0, 0.0, sink])
        items = [(sd_capsule(q, (0, 0, -0.3), (0, 0, 0.1), 0.19, 0.12), V_BODY),
                 (np.sqrt((np.sqrt(q[:, 0] ** 2 + q[:, 1] ** 2) - 0.095) ** 2 + (q[:, 2] - 0.2) ** 2) - 0.04, V_BODY),
                 (sd_cylinder_z(q, (0, 0, 0.19), 0.085, 0.025), W_DEAD if state == 3 else V_EYE)]
        q3 = r36._polar(q, 3, offset=0.5)
        items.append((sd_capsule(q3, (0.15, 0, -0.02), (0.25, 0.0, 0.2), 0.05, 0.015), V_BONE))
        q6 = r36._polar(q, 6, offset=0.0)
        items.append((sd_capsule(q6, (0.18, 0, -0.01), (0.3, 0.0, -0.02), 0.022, 0.018), V_GLOW))
        if state == 3:
            for x, y, z, r in ((0.08, -0.05, 0.12, 0.035), (-0.09, 0.02, 0.1, 0.03), (0.0, 0.1, 0.08, 0.028)):
                items.append((sd_sphere(q, (x, y, z), r), W_ICHOR))
        d, m = union(*items, k=0.03)
        if state == 3:                                          # the top torn off
            d = np.maximum(d, -sd_sphere(q, (0.05, 0.03, 0.3), 0.17))
        ground = p[:, 2] - 0.012 * np.sin(p[:, 0] * 61) * np.sin(p[:, 1] * 53)
        cap = -ground > d
        return np.maximum(d, -ground), np.where(cap, W_SAND, m)
    return scene


def rock_scene(shape, spin):
    rng = np.random.default_rng(4400 + shape)
    size = (0.85, 1.0, 1.1, 1.18)[shape]
    blobs = [(rng.uniform(-0.06, 0.06), rng.uniform(-0.06, 0.06), rng.uniform(-0.03, 0.03), rng.uniform(0.06, 0.1))
             for _ in range(5)]
    axis = np.array([1.0, 0.6 - 0.3 * shape, 0.4])
    axis /= np.linalg.norm(axis)
    ang = spin * TAU / 4 / 2 + shape
    K = np.array([[0, -axis[2], axis[1]], [axis[2], 0, -axis[0]], [-axis[1], axis[0], 0]])
    R = np.eye(3) + math.sin(ang) * K + (1 - math.cos(ang)) * K @ K

    def scene(p):
        q = (p - np.array([0.0, 0.0, 0.3])) @ R / size
        items = [(sd_sphere(q, (x, y, z), r), W_ROCK) for x, y, z, r in blobs]
        items.append((sd_sphere(q, (0.04, 0.05, 0.06), 0.05), W_SAND))
        d, m = union(*items, k=0.05)
        return d * size, m
    return scene


# =========================================================================== render jobs

REV = {}


def _job_key(job):
    return hashlib.sha1((repr(job) + f"/{REV.get(job[0], 1)}/{S}/{CANVAS}").encode()).hexdigest()[:20]


def model_for(job):
    kind = job[0]
    if kind == "shut":
        _, p, st = job
        kw = dict(SHUT_STATES[st])
        return mouth_scene(POSES[p], seed=p * 10 + st, **kw), mats(weak=0.25), CANVAS
    if kind == "mouth":
        _, p, st = job
        kw = dict(MOUTH_STATES[st])
        weak = (0.6, 1.0, 1.0)[st]
        return (mouth_scene(POSES[p], seed=p * 10 + st, **kw), mats(weak=weak, hot=1.0 if kw.get("hot") else 0.0),
                CANVAS)
    if kind == "death":
        _, st = job
        kw, glow, weak = DEATH_STATES[st]
        return mouth_scene("under", seed=90 + st, **kw), mats(glow=max(glow, 0.05), weak=weak), CANVAS
    if kind == "vent":
        _, st = job
        return vent_scene(st), mats(weak=(0.2, 0.45, 1.3, 0.0)[st]), VENT
    if kind == "rock":
        _, shape, spin = job
        return rock_scene(shape, spin), mats(), ROCK
    raise ValueError(kind)


def render_job(job):
    path = CACHE / f"{_job_key(job)}.png"
    if path.exists():
        return Image.open(path).convert("RGBA")
    scene, m, size = model_for(job)
    hi, factor = artkit.render_hi(scene, m, (size, size), size / S)
    img = artkit.native(hi, factor)
    CACHE.mkdir(parents=True, exist_ok=True)
    img.save(path)
    return img


def centred_crop(frames, pad=1):
    """The set cropped by the same margins on both sides (x and y apart) down to its visible pixels: the
    centre stays the centre."""
    w, h = frames[0].size
    mx, my = w // 2, h // 2
    for f in frames:
        a = np.array(f)[..., 3] > 0
        ys, xs = np.nonzero(a)
        if len(xs) == 0:
            continue
        mx = min(mx, xs.min(), w - 1 - xs.max())
        my = min(my, ys.min(), h - 1 - ys.max())
    mx, my = max(0, mx - pad), max(0, my - pad)
    return [f.crop((mx, my, w - mx, h - my)) for f in frames]


# =========================================================================== 2D pieces (lit like the ridge)

def _cracks(seed, n, reach, size, f, k, cx=None, cy=None, width=1.0):
    """Random-walk cracks out from the centre (the concept's Breach.cracks), grown to ``k`` of their length,
    as a 0..1 mask on the f-times canvas."""
    rng = np.random.default_rng(seed)
    cx = size / 2 if cx is None else cx
    cy = size / 2 if cy is None else cy
    img = Image.new("L", (size * f, size * f), 0)
    d = ImageDraw.Draw(img)
    for j in range(n):
        a = j * TAU / n + rng.uniform(-0.25, 0.25)
        pts, r, aa = [(0.0, 0.0)], 4.0, a
        while r < reach:
            r += rng.uniform(4, 8)
            aa += rng.uniform(-0.35, 0.35)
            pts.append((math.cos(aa) * r, math.sin(aa) * r))
        m = max(2, int(len(pts) * k))
        xy = [((cx + x) * f, (cy + y) * f) for x, y in pts[:m]]
        d.line(xy, fill=255, width=max(1, int(round(width * f))))
    return np.array(img, float) / 255


def bulge_frame(i):
    b = (i + 1) / 4
    f = 2
    yy, xx = b15.piece_canvas((BULGE, BULGE), f)
    r = np.hypot(xx - BULGE / 2, yy - BULGE / 2)
    n = raster.fbm(BULGE * f, BULGE * f, 12, 4401, octaves=3, period=False)
    dome = np.exp(-(r / (34 + 12 * b)) ** 2)
    hgt = 28 * b * dome + 2.0 * b * np.exp(-((r - 46) / 7) ** 2) * (n + 0.3) + 1.0 * (n - 0.5)
    k = np.clip((b - 0.3) / 0.7, 0, 1)
    crack = _cracks(4402, 9, 52, BULGE, f, k) * (dome > 0.12)
    hgt -= 2.5 * crack * b
    cover = np.clip((dome * (0.5 + 0.5 * b) + 0.2 * (n - 0.5) - 0.16) * 5, 0, 1)
    alb = b15.ramp(FLOOR, np.clip(0.58 + 0.16 * dome * b + 0.14 * (n - 0.5), 0, 1))
    alb = alb * (1 - 0.75 * crack[..., None]) + np.array((128, 78, 190), float) * 0.75 * crack[..., None]
    em = crack[..., None] * (VIOLET / 255 * (0.5 + 0.9 * k))
    return l20.finish_flat(hgt, alb, cover, f, emission=em, colours=32)


def marker_frame(i):
    b = (i + 1) / 4
    f = 2
    c = MARKER / 2
    yy, xx = b15.piece_canvas((MARKER, MARKER), f)
    r = np.hypot(xx - c, yy - c)
    ang = np.arctan2(yy - c, xx - c)
    n = raster.fbm(MARKER * f, MARKER * f, 8, 4411, octaves=3, period=False)
    rr = 40 + 1.8 * np.sin(ang * 7 + 1.0) + 2.5 * (n - 0.5)
    ring = (np.abs(r - rr) < 0.9) & (np.sin(ang * 5 + 0.7) > 1.2 - 2.2 * b)
    radial = _cracks(4412, 7, 40 * (0.4 + 0.6 * b), MARKER, f, 1.0) * b
    crack = np.clip(ring + radial, 0, 1)
    mound = np.exp(-(r / 24) ** 2)
    hgt = 8 * b * mound + 1.4 * np.exp(-((r - 43) / 3) ** 2) * b - 2.0 * crack + 0.8 * (n - 0.5)
    band = np.clip(1 - np.abs(r - 41) / 7, 0, 1)
    cover = np.clip(np.maximum(mound * (0.4 + 0.6 * b) * 1.3, band * b * 1.4) + 0.25 * (n - 0.5) - 0.2, 0, 1) * 3
    alb = b15.ramp(FLOOR, np.clip(0.6 + 0.12 * mound * b + 0.12 * (n - 0.5), 0, 1))
    alb = alb * (1 - 0.75 * crack[..., None]) + np.array((128, 78, 190), float) * 0.75 * crack[..., None]
    em = crack[..., None] * (VIOLET / 255 * (0.7 + 0.8 * b))
    return l20.finish_flat(hgt, alb, np.clip(cover, 0, 1), f, emission=em, colours=24)


FUNNEL_STATES = [  # depth, rim, spiral phase (None = none), veins, mound, cracks, cover radius, size
    (18, 3.0, None, 0.5, 0.0, 1.0, 1.06, 1.0),
    (44, 5.0, 0.0, 1.0, 0.0, 0.0, 1.07, 1.0),
    (44, 5.0, TAU / 3, 1.0, 0.0, 0.0, 1.07, 1.0),
    (44, 5.0, 2 * TAU / 3, 1.0, 0.0, 0.0, 1.07, 1.0),
    (22, 4.0, None, 0.3, 10.0, 0.0, 1.05, 1.0),
    (12, 4.0, None, 0.0, 3.0, 0.0, 1.06, 0.72),
]


def _veins(seed, size, f, reach_lo, reach_hi, n=7):
    """Violet-lit vein ridges under the sand, wandering out from round the funnel."""
    rng = np.random.default_rng(seed)
    img = Image.new("L", (size * f, size * f), 0)
    d = ImageDraw.Draw(img)
    c = size / 2
    for j in range(n):
        a = j * TAU / n + rng.uniform(-0.3, 0.3)
        r = reach_lo
        pts = []
        while r < reach_hi:
            pts.append(((c + math.cos(a) * r) * f, (c + math.sin(a) * r) * f))
            r += 5
            a += rng.uniform(-0.12, 0.12)
        d.line(pts, fill=255, width=int(1.5 * f))
    return np.array(img, float) / 255


def funnel_frame(i):
    depth, rim, phase, veins, mound, cracks, reach, size = FUNNEL_STATES[i]
    f = 2
    c = FUNNEL / 2
    R = FUNNEL_R * size
    yy, xx = b15.piece_canvas((FUNNEL, FUNNEL), f)
    r = np.hypot(xx - c, yy - c)
    ang = np.arctan2(yy - c, xx - c)
    n = raster.fbm(FUNNEL * f, FUNNEL * f, 14, 4421, octaves=3, period=False)
    bowl = np.clip(1 - r / R, 0, 1)
    hgt = -depth * bowl ** 1.3 + rim * np.exp(-((r - R) / 12) ** 2) * (0.7 + 0.6 * n) + 1.2 * (n - 0.5)
    if phase is not None:                                      # sand sliding in: spiral ridges in the wall
        hgt += 2.4 * np.sin(ang * 7 + r * 0.12 - phase) * bowl * np.clip(r / 30, 0, 1)
    if mound:
        hgt += mound * np.exp(-(r / 34) ** 2) * (0.8 + 0.4 * n)
    vein = _veins(4422, FUNNEL, f, R * 0.45, R * 1.08) * veins if veins else np.zeros_like(r)
    hgt += 1.2 * vein
    crack = _cracks(4423, 10, R * 0.9, FUNNEL, f, 1.0) * (r < R) if cracks else np.zeros_like(r)
    hgt -= 2.0 * crack
    deep = bowl ** 0.8 * min(1.0, depth / 30)
    t = 0.56 - 0.42 * deep + 0.1 * (n - 0.5) - (0.06 if i == 5 else 0.0)
    alb = b15.ramp(FLOOR, np.clip(t, 0, 1))
    alb = alb * (1 - 0.35 * vein[..., None]) + np.array((70, 40, 90), float) * 0.35 * vein[..., None]
    alb = alb * (1 - 0.6 * crack[..., None]) + np.array((40, 24, 40), float) * 0.6 * crack[..., None]
    em = vein[..., None] * (VIOLET / 255 * 0.6) + crack[..., None] * (VIOLET / 255 * 0.6)
    cover = np.clip((reach - r / R) * 6 + 0.8 * (n - 0.5), 0, 1)
    return l20.finish_flat(hgt, alb, cover, f, emission=em, colours=32)


def burst_frame(i):
    """A death eruption: a dust plume rising and spreading, lime ichor flung out early (stepped alpha)."""
    u = (i + 1) / 8
    Sx = 4
    c = BURST / 2
    items = [(c + 9 * u * dx, c + 2 - 10 * u * dy, (6 + 15 * u) * rr, (1 - u) ** 0.7 * 0.95)
             for dx, dy, rr in ((0, 0, 1.0), (-1.1, 0.4, 0.75), (1.0, 0.7, 0.8), (0.2, 1.4, 0.7), (-0.5, -0.8, 0.6))]
    alpha, light, _ = su.blobs(BURST, BURST, Sx, items, 4501 + i)
    col = b15.ramp(FLOOR[1:], np.clip(0.3 + 0.6 * light, 0, 1))
    if u < 0.75:                                               # ichor droplets on rays out of the plume
        rng = np.random.default_rng(4510)
        yy, xx = np.mgrid[0:BURST * Sx, 0:BURST * Sx].astype(np.float64) / Sx
        drop = np.zeros_like(xx)
        for _ in range(14):
            a = rng.uniform(0, TAU)
            sp = rng.uniform(16, 30)
            dr = 4 + sp * u * 1.4
            x, y = c + math.cos(a) * dr, c + math.sin(a) * dr - 6 * u
            drop = np.maximum(drop, np.clip(1.6 - np.hypot(xx - x, yy - y) / (1.3 * (1 - 0.5 * u)), 0, 1))
        drop = np.clip(drop, 0, 1)
        lime = LIME * 0.8 + np.array((255, 255, 220), float) * 0.2
        col = col * (1 - drop[..., None]) + lime * drop[..., None]
        alpha = np.maximum(alpha, drop)
    rgb, a = su.shrink(col, alpha, Sx)
    return su.stepped(rgb, np.clip(a, 0, 1) * 0.9, steps=4)


def gullet_ellipse(pose):
    """The gullet's ellipse on the screen: (semi-axis across the lean, semi-axis along it, the lean (x, y down))."""
    z = LIFT[pose] + 0.02 * math.cos(LEAN if LEAN_DIR[pose] else 0)
    mag = CAMERA / (CAMERA - z)
    rad = GULLET_R * S * mag
    d = LEAN_DIR[pose]
    if d is None:
        return rad, rad, (0.0, 0.0)
    return rad, rad * math.cos(LEAN), (d[0], -d[1])


def gullet_glow(p):
    """The gullet's lime glow (additive): a bright core in the ellipse, a soft halo past its rim."""
    across, along, (dx, dy) = gullet_ellipse(POSES[p])
    f = 4
    yy, xx = (np.mgrid[0:GLOW * f, 0:GLOW * f] + 0.5) / f - GLOW / 2
    if dx == 0 and dy == 0:
        rr = np.hypot(xx, yy) / across
    else:
        a = (xx * dx + yy * dy) / along
        b = (-xx * dy + yy * dx) / across
        rr = np.hypot(a, b)
    core = np.clip(1.05 - rr, 0, 1) ** 0.7
    halo = np.clip(1 - (rr - 0.8) / 0.6, 0, 1) ** 2 * 0.55
    light = np.clip(core * 0.95 + halo, 0, 1)
    rgb = LIME[None, None] * light[..., None] + np.array((230, 255, 200), float) * (core > 0.75)[..., None] * 0.5
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), light * 255]).astype(np.uint8), "RGBA")
    return artkit.additive(img.resize((GLOW, GLOW), Image.BOX))


# =========================================================================== output

def claimed():
    return DATA.exists()


@contextlib.contextmanager
def target():
    sprites, pivots = (artkit.SPRITES, artkit.PIVOTS) if claimed() else (HELD / "sprites", HELD / "pivots")
    sprites.mkdir(parents=True, exist_ok=True)
    pivots.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES, artkit.PIVOTS
    artkit.SPRITES, artkit.PIVOTS = sprites, pivots
    try:
        yield sprites
    finally:
        artkit.SPRITES, artkit.PIVOTS = before


def _owned(stem, name):
    return stem == name or (stem.rsplit("_", 1)[0] == name and stem.rsplit("_", 1)[-1].isdigit())


def write(name, frames):
    for folder in (artkit.SPRITES, HELD / "sprites"):
        for old in folder.glob(f"{name}*.png"):
            if _owned(old.stem, name):
                old.unlink()
    with target() as where:
        artkit.write_frames(name, frames, SOURCE)
    print(f"  {name}: {len(frames)} x {frames[0].width}x{frames[0].height} -> {where.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, HELD / "sprites"):
        files = [f for f in folder.glob(f"{name}_*.png") if _owned(f.stem, name)]
        if files:
            files.sort(key=lambda p: int(p.stem.rsplit("_", 1)[1]))
            return [Image.open(f).convert("RGBA") for f in files]
    return []


def neck(pose):
    """Where the neck's axis leaves the sand at full rise, px from the mouth's centre (x right, y down)."""
    d = LEAN_DIR[pose]
    if d is None:
        return [0.0, 0.0]
    t = -LIFT[pose] / math.cos(LEAN)
    off = t * math.sin(LEAN) * S
    return [round(off * d[0], 1), round(-off * d[1], 1)]


def pivots():
    return {
        "note": "per pose (0 front, 1 left, 2 right, 3 rear, 4 under): px from the mouth's centre (x right, y down); "
                "every mouth set (shut, mouth, gullet, death) and the funnel are centred on it",
        "scale": S,
        "poses": list(POSES),
        "lean": [[float(gullet_ellipse(p)[2][0]), float(gullet_ellipse(p)[2][1])] for p in POSES],
        "gullet": [[round(gullet_ellipse(p)[0], 1), round(gullet_ellipse(p)[1], 1)] for p in POSES],
        "gullet_note": "the gullet's semi-axes: across the lean, along it",
        "neck": [neck(p) for p in POSES],
        "neck_note": "where the body leaves the sand behind a risen mouth: the ridge's head joins there",
        "collar_radius": round(0.8 * S, 1),
        "funnel_radius": FUNNEL_R,
    }


SET_NAMES = ["dust-colossus-shut", "dust-colossus-mouth", "dust-colossus-gullet", "dust-colossus-death",
             "dust-colossus-vent", "dust-colossus-bulge", "dust-colossus-marker", "dust-colossus-funnel",
             "dust-colossus-burst", "dust-colossus-rock"]


def jobs():
    js = [("shut", p, st) for p in range(5) for st in range(len(SHUT_STATES))]
    js += [("mouth", p, st) for p in range(5) for st in range(len(MOUTH_STATES))]
    js += [("death", st) for st in range(len(DEATH_STATES))]
    js += [("vent", st) for st in range(4)]
    js += [("rock", sh, sp) for sh in range(4) for sp in range(4)]
    return js


def build():
    js = jobs()
    with ProcessPoolExecutor() as pool:
        out = dict(zip(js, pool.map(render_job, js)))
        bulges = list(pool.map(bulge_frame, range(4)))
        markers = list(pool.map(marker_frame, range(4)))
        funnels = list(pool.map(funnel_frame, range(len(FUNNEL_STATES))))
        bursts = list(pool.map(burst_frame, range(8)))

    def of(kind):
        return [out[j] for j in js if j[0] == kind]
    shut, mouth, death = centred_crop(of("shut")), centred_crop(of("mouth")), centred_crop(of("death"))
    body = act3kit.quantize_bright(shut + mouth + death, 56)      # one palette: no flicker between the sets
    shut, mouth, death = body[:len(shut)], body[len(shut):len(shut) + len(mouth)], body[len(shut) + len(mouth):]
    sets = {
        "dust-colossus-shut": shut,
        "dust-colossus-mouth": mouth,
        "dust-colossus-gullet": [gullet_glow(p) for p in range(5)],
        "dust-colossus-death": death,
        "dust-colossus-vent": act3kit.quantize_bright(of("vent"), 28),
        "dust-colossus-bulge": artkit.quantize_set(centred_crop(bulges), 32),
        "dust-colossus-marker": artkit.quantize_set(centred_crop(markers), 24),
        "dust-colossus-funnel": artkit.quantize_set(centred_crop(funnels), 40),
        "dust-colossus-burst": artkit.quantize_set(bursts, 20),
        "dust-colossus-rock": artkit.quantize_set(centred_crop(of("rock")), 16),
    }
    for name, frames in sets.items():
        write(name, frames)
    with target():
        artkit.write_pivots(ROOT_NAME, SOURCE, pivots())
    import intel                                                 # tools/art/intel.py: the sensor silhouette
    intel.build(["boss-dust-colossus"])
    px = sum(f.width * f.height for frames in sets.values() for f in frames)
    pad = sum((f.width + 4) * (f.height + 4) for frames in sets.values() for f in frames)
    print(f"  {px / 1e6:.2f} M px ({pad / 1e6:.2f} M with the packer's padding; one page is 4.19 M)")


def intel_mask():
    """The open UNDER mouth as the game draws it, the outline only (tools/art/intel.py's silhouette)."""
    frames = load("dust-colossus-mouth")
    return np.array(frames[4 * 3 + 1])[..., 3] > 127


# =========================================================================== review

FW, FH, FPS = 480, 540, 12


def paste_c(img, sp, x, y):
    sprite.paste(img, sp, x - sp.width / 2, y - sp.height / 2)


def glow_c(img, g, x, y, k=1.0):
    if k < 1.0:
        a = np.array(g).astype(np.float64)
        a[..., :3] *= k
        g = Image.fromarray(a.astype(np.uint8), "RGBA")
    return artkit.add_light(img, g, (int(round(x - g.width / 2)), int(round(y - g.height / 2))))


class Field:
    """The review's play field: Level 21's arena tile (tools/art/backdrop_l21.py's basin floor; Level 20's dunes
    if it is missing) scrolling at the arena's 20 px/s, in 0.5 s steps (the GIF's size)."""

    def __init__(self):
        tile = ROOT / "assets" / "backdrop" / "level-21" / "basin-floor.png"
        if not tile.exists():
            tile = ROOT / "assets" / "backdrop" / "level-20" / "basin-dunes.png"
        tile = np.array(Image.open(tile).convert("RGB"))
        self.strip = np.concatenate([tile] * 4)

    def frame(self, t):
        h = self.strip.shape[0]
        y0 = int(h - FH - ground_step(t)) % (h - FH)
        return Image.fromarray(self.strip[y0:y0 + FH]).convert("RGBA")


def ridge_draw(img, s, a, b, u, glow=0.5):
    """The colossus ridge (tools/art/l20_props.py's sprites) from a toward b, its head at fraction u."""
    (ax, ay), (bx, by) = a, b
    hx, hy = ax + (bx - ax) * u, ay + (by - ay) * u
    L = math.hypot(hx - ax, hy - ay)
    if L < 8:
        return img
    dx, dy = (hx - ax) / L, (hy - ay) / L
    th = math.atan2(-dx, dy)
    k = int(round(th / (TAU / 16))) % 16
    n = int(L // 16)
    paste_c(img, s["colossus-ridge-tail"][k], ax + dx * 8, ay + dy * 8)
    for i in range(1, n):
        x, y = ax + dx * 16 * i, ay + dy * 16 * i
        paste_c(img, s["colossus-ridge"][k], x, y)
    for i in range(1, n):
        x, y = ax + dx * 16 * i, ay + dy * 16 * i
        img = glow_c(img, s["colossus-ridge-glow"][k], x, y, glow)
    paste_c(img, s["colossus-ridge-head"][k], hx, hy)
    return img


def ring(img, orb, cx, cy, t0, t, n, speed, offset=0.0):
    if t < t0:
        return
    r = 30 + speed * (t - t0)
    for j in range(n):
        a = offset + j * TAU / n
        x, y = cx + math.cos(a) * r, cy + math.sin(a) * r
        if -10 < x < FW + 10 and -10 < y < FH + 10:
            paste_c(img, orb[int(t * 8) % len(orb)], x, y)


def review_gif(s):
    """A left breach (phase 1), a rear breach with its vents (phase 2), a breach under the ship (phase 3) and the
    death, compressed (the open phase shortened)."""
    field = Field()
    orb = load_game("orb")
    ship = load_game("ship")
    shipf = ship[len(ship) // 2]
    frames = []
    T = 27.5
    P1, P2 = (30, 260), (250, 515)
    rng = np.random.default_rng(44)
    rocks = []                                         # phase 2's debris: (spawn t, x, y)
    for j, t0 in enumerate(np.arange(15.2, 18.0, 0.6)):
        rocks.append((t0, rng.uniform(40, 440), -10.0, j % 16))
    vents = [(P2[0] - 3 * j, P2[1] - d) for j, d in enumerate((120, 200, 280))]
    death_ridge = ((300, 330), (40, 570))
    for fi in range(int(T * FPS)):
        t = fi / FPS
        img = field.frame(t)
        label = ""
        # ---------------------------------------------------------------- phase 1: the left breach
        if t < 3.0:
            img = ridge_draw(img, s, (170, 600), (44, 300), t / 3.0)
        if 1.0 <= t < 3.0:
            paste_c(img, s["dust-colossus-bulge"][min(3, int((t - 1.0) / 0.5))], *P1)
        if 3.0 <= t < 11.7:
            fu = 0 if t < 3.75 else (4 if t >= 10.2 else 1 + int(t * 6) % 3)
            paste_c(img, s["dust-colossus-funnel"][fu], *P1)
        if t >= 11.7:                                   # the crater stays and scrolls on with the ground
            paste_c(img, s["dust-colossus-funnel"][5], P1[0], P1[1] + ground_step(t) - ground_step(11.7))
        p = 1
        if 3.0 <= t < 4.5:
            paste_c(img, s["dust-colossus-shut"][p * 6 + min(2, int((t - 3.0) / 0.5))], *P1)
            label = "PHASE 1: LEFT BREACH, THE RISE"
        elif 4.5 <= t < 9.2:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + (0 if t < 4.8 else 1)], *P1)
            for tr in (5.6, 8.1):
                if tr - 0.5 <= t < tr:
                    img = glow_c(img, s["dust-colossus-gullet"][p], *P1)
            label = "PHASE 1: OPEN, RINGS OF 12, THE SUCTION"
        elif 9.2 <= t < 10.0:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + 2], *P1)
            label = "THE BITE'S TELL"
        elif 10.0 <= t < 10.2:
            paste_c(img, s["dust-colossus-shut"][p * 6 + 3], *P1)
            label = "THE SNAP"
        elif 10.2 <= t < 11.7:
            paste_c(img, s["dust-colossus-shut"][p * 6 + (4 if t < 10.95 else 5)], *P1)
            label = "THE DIVE"
        if 4.8 <= t < 9.2:
            img = suction(img, P1, t - 4.8)
        for tr in (5.6, 8.1):
            ring(img, orb, *P1, tr, t, 12, 100, 0.13 * tr)
        if t < 3.0:
            img.alpha_composite(ui_r11.variant_a("l", t))
            label = label or "PHASE 1: THE RIDGE, THE EDGE WARNING, THE BULGE"
        # ---------------------------------------------------------------- phase 2: the rear breach, the vents
        if 10.5 <= t < 13.5:
            img = ridge_draw(img, s, (470, 230), (P2[0] + 10, P2[1] - 40), (t - 10.5) / 3.0)
            img.alpha_composite(ui_r11.variant_a("b", t - 10.5))
            label = "PHASE 2: THE RIDGE TO THE REAR, THE WARNING"
        if 11.5 <= t < 13.5:
            paste_c(img, s["dust-colossus-bulge"][min(3, int((t - 11.5) / 0.5))], *P2)
        if 13.5 <= t < 20.5:
            fu = 0 if t < 14.25 else (4 if t >= 19.0 else 1 + int(t * 6) % 3)
            paste_c(img, s["dust-colossus-funnel"][fu], *P2)
        if t >= 20.5:
            paste_c(img, s["dust-colossus-funnel"][5], P2[0], P2[1] + ground_step(t) - ground_step(20.5))
        if 13.5 <= t < 19.0:
            for j, (vx, vy) in enumerate(vents):
                dead = j == 1 and t >= 16.4
                st = 0 if t < 15.0 else (3 if dead else (2 if (t - 15.4 - 0.3 * j) % 1.5 < 0.25 else 1))
                paste_c(img, s["dust-colossus-vent"][st], vx, vy)
                if j == 1 and 16.4 <= t < 16.9:
                    paste_c(img, s["dust-colossus-burst"][min(7, int((t - 16.4) / 0.0625))], vx, vy - 8)
        p = 3
        if 13.5 <= t < 15.0:
            paste_c(img, s["dust-colossus-shut"][p * 6 + min(2, int((t - 13.5) / 0.5))], *P2)
            label = "PHASE 2: REAR BREACH, THE SPINE VENTS RISE"
        elif 15.0 <= t < 18.0:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + (0 if t < 15.3 else 1)], *P2)
            if 15.6 <= t < 16.1:
                img = glow_c(img, s["dust-colossus-gullet"][p], *P2)
            label = "PHASE 2: RINGS OF 14, VENT BURSTS, DEBRIS"
        elif 18.0 <= t < 18.8:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + 2], *P2)
            label = "THE BITE'S TELL"
        elif 18.8 <= t < 19.0:
            paste_c(img, s["dust-colossus-shut"][p * 6 + 3], *P2)
        elif 19.0 <= t < 20.5:
            paste_c(img, s["dust-colossus-shut"][p * 6 + (4 if t < 19.75 else 5)], *P2)
            label = "THE DIVE"
        if 15.3 <= t < 18.0:
            img = suction(img, P2, t - 15.3)
            for t0, x0, y0, k in rocks:
                if t0 <= t:
                    u = t - t0
                    dx, dy = P2[0] - x0, P2[1] - y0
                    L = math.hypot(dx, dy)
                    dist = 60 * u + 70 * u * u
                    if dist < L - 50:
                        paste_c(img, s["dust-colossus-rock"][(k // 4) * 4 + int(t * 8) % 4],
                                x0 + dx / L * dist, y0 + dy / L * dist)
        ring(img, orb, *P2, 16.1, t, 14, 110, 0.4)
        # ---------------------------------------------------------------- phase 3: under the ship, the death
        M3 = death_ridge[0]
        if 20.5 <= t < 22.0:
            paste_c(img, s["dust-colossus-marker"][min(3, int((t - 20.5) / 0.375))], *M3)
            label = "PHASE 3: THE MARKER UNDER THE SHIP"
        if 22.0 <= t < 27.5:
            fu = 0 if t < 22.4 else (4 if t >= 25.8 else 1 + int(t * 6) % 3)
            paste_c(img, s["dust-colossus-funnel"][fu], *M3)
        p = 4
        if 22.0 <= t < 22.5:
            paste_c(img, s["dust-colossus-shut"][p * 6 + (1 if t < 22.25 else 2)], *M3)
            label = "PHASE 3: THE ERUPTION"
        elif 22.5 <= t < 24.0:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + (0 if t < 22.7 else 1)], *M3)
            for arm in range(2):
                for k in range(8):
                    t0 = 22.7 + k * 0.25
                    if t0 <= t:
                        a = arm * math.pi + 1.4 * (t0 - 22.7)
                        r = 30 + 120 * (t - t0)
                        paste_c(img, orb[k % len(orb)], M3[0] + math.cos(a) * r, M3[1] + math.sin(a) * r)
            label = "PHASE 3: OPEN IN THE FIELD, THE TWO-ARM SPIRAL"
        elif 24.0 <= t < 27.0:
            seq = [(24.5, 0), (24.9, 1), (25.3, 2), (25.6, 1), (25.9, 2), (26.3, 3), (26.7, 4), (27.0, 5)]
            st = next(v for e, v in seq if t < e)
            img = ridge_draw(img, s, death_ridge[1], (M3[0] - 50, M3[1] + 50), 1.0, glow=1.0)
            paste_c(img, s["dust-colossus-death"][st], *M3)
            (ax, ay), (bx, by) = death_ridge
            for j in range(10):
                t0 = 24.2 + j * 0.27
                if t0 <= t < t0 + 0.55:
                    u = 0.15 + 0.085 * j
                    paste_c(img, s["dust-colossus-burst"][min(7, int((t - t0) / 0.07))],
                            ax + (bx - ax) * u, ay + (by - ay) * u - 10)
            label = "THE DEATH: REARS, SHUDDERS, THE RIDGE SPLITS, SINKS"
        elif t >= 27.0:
            label = "THE FLASH; THE CRATER SETTLES"
        # ---------------------------------------------------------------- the ship
        sx, sy = ship_path(t)
        paste_c(img, shipf, sx, sy)
        if 26.95 <= t < 27.4:                           # the act boss's flash at the chain's end
            k = 1.0 - (t - 26.95) / 0.45
            white = Image.new("RGBA", img.size, (255, 250, 235, int(200 * k)))
            img.alpha_composite(white)
        raster.draw_text(img, 8, FH - 14, f"DUST COLOSSUS FINAL R44 - {t:4.1f} S  {label}", raster.LABEL)
        frames.append(img)
    return frames


def ground_step(t):
    """The review field's scroll (px) at t: 20 px/s in 0.5 s steps."""
    return 10 * int(t * 2)


def ship_path(t):
    keys = [(0, (250, 470)), (3, (230, 380)), (5, (150, 300)), (8.5, (175, 290)), (9.6, (230, 300)),
            (12, (260, 380)), (15, (250, 340)), (18, (240, 330)), (20.5, (300, 330)), (21.4, (380, 230)),
            (24, (390, 200)), (27.5, (300, 420))]
    for (t0, a), (t1, b) in zip(keys, keys[1:]):
        if t < t1:
            u = (t - t0) / (t1 - t0)
            u = u * u * (3 - 2 * u)
            return a[0] + (b[0] - a[0]) * u, a[1] + (b[1] - a[1]) * u
    return keys[-1][1]


def suction(img, c, s):
    """Sand streaks spiralling into an open mouth (the review's stand-in for the game's suction effect)."""
    rng = np.random.default_rng(9)
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    k = min(1.0, s / 0.4)
    for _ in range(40):
        a0, r0, ph = rng.uniform(0, TAU), rng.uniform(70, 300), rng.uniform(0, 1)
        u = (s * 0.8 + ph) % 1.0
        r = r0 * (1 - u) ** 1.3 + 40
        a = a0 + 1.4 * u
        x, y = c[0] + math.cos(a) * r, c[1] + math.sin(a) * r
        x2, y2 = c[0] + math.cos(a - 0.1) * (r + 14), c[1] + math.sin(a - 0.1) * (r + 14)
        d.line([x2, y2, x, y], fill=tuple(OCHRE[5]) + (int(120 * k * math.sin(math.pi * u)),), width=1)
    img.alpha_composite(lay)
    return img


def load_game(name):
    return artkit.load_frames(name)


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    s = {name: load(name) for name in SET_NAMES}
    for name in ("colossus-ridge", "colossus-ridge-head", "colossus-ridge-tail", "colossus-ridge-glow"):
        s[name] = l20.load(name)
    gif = review_gif(s)
    shut, mouth = s["dust-colossus-shut"], s["dust-colossus-mouth"]
    intel_pic = Image.open(artkit.SPRITES / "intel" / "boss-dust-colossus.png").convert("RGBA")
    rows = [
        ("OPEN (STATE 1) PER POSE: FRONT, LEFT, RIGHT, REAR, UNDER", [mouth[p * 3 + 1] for p in range(5)], 1, False),
        ("HALF OPEN AND THE BITE'S TELL (TEETH LIT): FRONT, LEFT; UNDER",
         [mouth[0], mouth[2], mouth[3], mouth[5], mouth[12], mouth[14]], 1, False),
        ("SHUT, LEFT POSE: RISE 1/3, RISE 2/3, RISEN, SNAP, DIVE, DIVE (SAND POURING IN)", shut[6:12], 1, False),
        ("SHUT, UNDER POSE: THE SAME SIX", shut[24:30], 1, False),
        ("RISEN SHUT PER POSE (FRONT, LEFT, RIGHT, REAR, UNDER)", [shut[p * 6 + 2] for p in range(5)], 1, False),
        ("DEATH (UNDER): REARS, SHUDDER L, SHUDDER R (ICHOR), SINKING x3", s["dust-colossus-death"], 1, False),
        ("GULLET GLOW PER POSE (ADDITIVE)", s["dust-colossus-gullet"], 1, True),
        ("FUNNEL: COLLAPSING, SLIDING LOOP x3, REFILLING, THE CRATER THAT STAYS", s["dust-colossus-funnel"], 1, False),
        ("BULGE (THE TELL, 2 S) AND PHASE 3'S MARKER (1.5 S)", s["dust-colossus-bulge"] + s["dust-colossus-marker"], 1,
         False),
        ("SPINE VENT: RISING, UP, VENTING, DESTROYED; DEBRIS ROCKS (4 SHAPES x 4 TUMBLE STEPS)",
         s["dust-colossus-vent"] + s["dust-colossus-rock"], 2, False),
        ("DEATH BURSTS ALONG THE RIDGE (DUST, LIME ICHOR) BY AGE; INTEL SILHOUETTE (SENSOR L2)",
         s["dust-colossus-burst"] + [intel_pic], 2, False),
        ("FRAMES FROM THE GIF: LEFT BREACH OPEN, THE TELL, REAR WITH VENTS, THE MARKER, PHASE 3 OPEN, THE DEATH",
         [gif[int(t * FPS)].resize((216, 243), Image.LANCZOS) for t in (6.0, 9.5, 16.0, 21.5, 23.5, 25.0)], 1, False),
    ]
    sheet = artkit.review_sheet("DUST COLOSSUS - FINAL SPRITES (L21 ACT 3 BOSS)", rows, width=1500, batch=BATCH)
    artkit.save_review(sheet, gif, CONCEPT, "dust-colossus", fps=FPS)


PIV = pivots()


if __name__ == "__main__":
    if "--review" not in sys.argv[1:]:
        build()
    review()
