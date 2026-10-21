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
  dust-colossus-vent_0..3    a phase-2 vent organ on a spine segment (48x48, the 40x40 vent; round 45: an organ,
                             not a turret): a fleshy chimney through a collar of armour scales, its top a
                             sphincter round the lime gill throat: 0 shut (rising/sinking), 1 up (lime dim), 2
                             venting (dilated, blazing, a gout of ichor), 3 destroyed (torn open; stays dead)
  dust-colossus-spine_0..191 the armoured back breaking the sand behind a risen edge mouth, toward the field's
                             centre: frame (segment * 3 + state) * 16 + heading; segments 0-3 at 120, 200, 280, 350
                             px from the mouth (the vents on 0-2, a sealed pore under each), states 0 rise 1/3, 1
                             rise 2/3 (sand heaped on the plates), 2 up; heading k = the spine's direction from the
                             mouth, k * 22.5 degrees clockwise from straight down; each frame centred on the
                             segment's centre. Round 46 (round 45 A.a): the four frames are stretches of ONE back
                             (dark shingled plates over the violet hide, a bone keel spike per plate, a low sand berm
                             on both flanks; one far key light), cut at 166/246/326 px on plate edges; segment 0
                             dives into the sand toward the mouth (from 60 px), segments 1-3 reach 12 px under the
                             one before (the game draws tail first), segment 3 tapers to the tip at 400
  dust-colossus-rear_0..3    round 45, the death throe (UNDER pose, bigger than the open mouth): the forebody
                             rearing out of the sand: 0 rising, 1 towering (the armour rings of the neck showing),
                             2 the scream (teeth white-hot), 3 a shudder (ichor bursting)
  dust-colossus-collapse_0..2  the forebody falling back into the sand (then death_4, death_5, the sinkhole)
  dust-colossus-geyser_0..7  a sand geyser bursting out of the ridge with lime ichor, by age (stepped alpha)
  dust-colossus-fissure_0..3 ground decal round the throe: the fissure ring (~118 px radius) cracking, open with
                             ichor light, widening, settled (stays with the sinkhole)
  dust-colossus-plume_0..2   the dust plume rolling out of the collapse: a soft alpha sheet (young, spreading,
                             thin) the game scales up and fades
  dust-colossus-debris_0..15 a big rock flung up by the throe (~40 px), shape * 4 + tumble step
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
  design/enemies/bosses/concept/dust-colossus-final-r46-a.png/.gif   the review (round 46: the spine as one
                             ridged back; r45's sheet showed the first spine, the vent organs and the throe, r44's
                             the first sets)

How the game draws it, back to front: the funnel at the breach point (the crater after the dive, scrolling with
the ground), the shut / mouth frames centred on the point, the gullet glow added over an open mouth; the vents on
the ridge's path; the bulge / marker on the ground before a breach; the death frames replace the mouth.

Run: python3 tools/art/dust_colossus.py [group ...] [--review]   (all groups ~20 min on 20 cores; renders cached in
the system temp dir, conceptgame-cache/dust-colossus/ (the cache key is the job, not the model: clear it after a
model edit); groups body, vent, ground, rocks, spine, throe; --review rebuilds the review from the written files)
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
from render import boss_models as bm  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_models import V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SEAM  # noqa: E402
from render.sdf import Material, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, union  # noqa: E402

SCRIPT = "dust_colossus.py"
BATCH = "M7 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r46"
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
W_PLATE, W_HIDE, W_RIM = 13, 14, 15                 # round 45 A.a: the spine's dark plates, violet hide, worn rims
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
    m.append(Material((0.29, 0.22, 0.31), metal=0.3, shininess=70, spec=0.75,               # W_PLATE: dark chitin
                      pattern=lambda p, n: 0.9 + 0.1 * np.sin(p[:, 0] * 41) * np.sin(p[:, 1] * 37)))
    m.append(Material((0.36, 0.2, 0.46), metal=0.1, shininess=40, spec=0.5))                 # W_HIDE: violet hide
    m.append(Material((0.55, 0.47, 0.44), metal=0.2, shininess=60, spec=0.6))                # W_RIM: worn plate edge
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
                seed=0, plates=False):
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
        if plates:                                  # the throe: the reared forebody's armour rings on the neck
            rq = np.sqrt(q[:, 0] ** 2 + q[:, 1] ** 2)
            for zk in (-1.3, -1.68, -2.06, -2.44, -2.82):
                rr = 0.835 + 0.08 * (-1.25 - zk) / 1.95       # the neck's radius there, the ring proud of it
                items.append((np.sqrt((rq - rr) ** 2 + ((q[:, 2] - zk) * 1.6) ** 2) / 1.6 - 0.075, V_BONE))
            qp = r36._polar(q, 6, offset=0.2)
            for zk in (-1.49, -1.87, -2.25, -2.63):
                items.append((sd_capsule(qp, (0.86, 0, zk + 0.1), (0.9, 0, zk - 0.1), 0.035, 0.03), V_GLOW))
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
    """A phase-2 vent organ on a spine segment (round 44 K.b: an organ of the Colossus, not a turret): a squat
    fleshy chimney broken up through a collar of six armour plates, violet veins up its sides, its top a puckered
    sphincter of five fleshy folds round the lime gill throat. 0 shut (rising or sinking: the folds closed, sand
    on it), 1 up (open, lime dim), 2 venting (dilated, lime blazing, a gout of ichor), 3 destroyed (torn open,
    dead tissue, ichor, two collar plates gone; stays dead)."""
    sink = 0.07 if state == 0 else 0.0
    dil = (0.0, 0.55, 1.0, 0.7)[state]                  # how far the folds are drawn back from the throat
    rng = np.random.default_rng(4300 + state)
    clumps = [(rng.uniform(-0.15, 0.15), rng.uniform(-0.15, 0.15), rng.uniform(0.1, 0.16), rng.uniform(0.035, 0.05))
              for _ in range(5)] if state == 0 else []

    def scene(p):
        q = p + np.array([0.0, 0.0, sink])
        rq = np.sqrt(q[:, 0] ** 2 + q[:, 1] ** 2)
        items = [(sd_capsule(q, (0, 0, -0.25), (0, 0, 0.08), 0.2, 0.14), V_BODY),          # the chimney
                 (np.sqrt((rq - 0.1 - 0.02 * dil) ** 2 + (q[:, 2] - 0.19) ** 2) - 0.045, V_SEAM),   # the lips
                 (sd_cylinder_z(q, (0, 0, 0.17), 0.075 + 0.02 * dil, 0.03), W_DEAD if state == 3 else V_EYE)]
        q6 = r36._polar(q, 6, offset=0.25)                                             # the plate collar
        collar = sd_ellipsoid(q6, (0.2, 0, 0.0), (0.075, 0.105, 0.045))           # overlapping scales
        if state == 3:                                                                 # two plates torn away
            ang = np.arctan2(q[:, 1], q[:, 0])
            collar = np.where(np.cos(ang - 1.0) > 0.75, 1.0, collar)
        items.append((collar, V_BONE))
        q5 = r36._polar(q, 5, offset=0.0)                                              # the sphincter's folds
        r0 = 0.015 + 0.06 * dil
        items.append((sd_capsule(q5, (r0, 0, 0.2), (0.13, 0.0, 0.17), 0.03, 0.035), V_BODY))
        q4 = r36._polar(q, 5, offset=0.6)                                              # veins up the sides
        items.append((sd_capsule(q4, (0.2, 0, -0.06), (0.13, 0.0, 0.13), 0.018, 0.014), V_GLOW))
        if state == 2:                                                                 # the gout
            for x, y, z, r in ((0.0, 0.0, 0.3, 0.06), (0.04, -0.03, 0.4, 0.045), (-0.05, 0.03, 0.37, 0.04)):
                items.append((sd_sphere(q, (x, y, z), r), W_ICHOR))
        if state == 3:
            for x, y, z, r in ((0.11, -0.06, 0.12, 0.035), (-0.1, 0.03, 0.1, 0.03), (0.02, 0.13, 0.07, 0.03)):
                items.append((sd_sphere(q, (x, y, z), r), W_ICHOR))
        for x, y, z, r in clumps:
            items.append((sd_ellipsoid(q, (x, y, z), (r, r, r * 0.6)), W_SAND))
        d, m = union(*items, k=0.025)
        if state == 3:                                          # the top torn off
            d = np.maximum(d, -sd_sphere(q, (0.03, 0.02, 0.27), 0.15))
        ground = p[:, 2]
        return np.maximum(d, -ground), m
    return scene


# --------------------------------------------------------------------------- the spine (round 45 A.a)
#
# Round 45 A.a (the user: the round 44 spine read as a chain of pale bubbles): one continuous ridged back. The four
# segment frames are cut from ONE model of the back (the same plates, hide, berms and light in global spine
# coordinates), so laid at their distances on a straight line they join seamlessly; each frame reaches SPINE_OVERLAP
# px further toward the mouth than the cut in front of it, under the segment before it (the game draws tail first), and
# ends toward the tail at a plate's proud edge (the cuts sit on plate edges): where the curve or the 22.5 degree heading
# steps bend the back, the joint reads as one armour plate shingled over the next.

SPINE_DIST = (120, 200, 280, 350)        # px from the mouth's centre along the spine (the vents at the first three)
SPINE_CUT = (166, 246, 326)              # px: where segment i ends toward the tail (= where i + 1 shows from under it)
SPINE_FRONT = 60                         # px: segment 0's front (the back dives into the sand there, by the collar)
SPINE_TIP = 400                          # px: the tail's tip, sinking into the sand
SPINE_OVERLAP = 12                       # px: each segment 1-3 reaches this far under the one before it
SPINE_PITCH = 80 / 3                     # px: the plates' pitch (the cuts at 166, 246, 326 are plate edges)
SPINE_STATES = (0.78, 0.55, 0.3)         # how deep the back's axis sits, x its half height: 0, 1 rise, 2 up
SPINE_HEADINGS = 16
SPINE_KEY = np.array((-2.2, 2.6, 3.2)) * 12          # the render package's key light direction, 12 x as far


def spine_span(seg):
    """Segment ``seg``'s stretch of the back, px from the mouth: (front, tail end)."""
    front = SPINE_FRONT if seg == 0 else SPINE_CUT[seg - 1] - SPINE_OVERLAP
    return front, (SPINE_CUT[seg] if seg < 3 else SPINE_TIP + 8)


def spine_width(s):
    """The back's half width (model units) at s units from the mouth: full behind the neck, tapering to the tail,
    rounding off to the tip."""
    s = np.asarray(s, float)
    w = np.interp(s, [0.0, 2.2, 4.45], [0.42, 0.4, 0.31])
    tail = 4.45
    u = np.clip((s - tail) / (SPINE_TIP / S - tail), 0.0, 1.0)
    return w * np.sqrt(np.clip(1.0 - u * u, 0.0, 1.0)) + 0.012


def spine_canvas(seg):
    front, back = spine_span(seg)
    along = max(SPINE_DIST[seg] - front, back - SPINE_DIST[seg]) / S
    reach = math.hypot(along, float(spine_width(SPINE_DIST[seg] / S - along)) + 0.3)
    return 2 * int(math.ceil(reach * S)) + 8


def _ellipse(x, z, a, b):
    """Approximate distance to the ellipse with semi-axes a (across), b (up) (iq's ellipsoid bound, 2D)."""
    k0 = np.sqrt((x / a) ** 2 + (z / b) ** 2)
    k1 = np.sqrt((x / (a * a)) ** 2 + (z / (b * b)) ** 2)
    return k0 * (k0 - 1.0) / np.maximum(k1, 1e-6)


def spine_scene(seg, state, heading):
    """Segment ``seg``'s stretch of the Colossus's back breaking the sand behind a risen mouth (round 45 A.a: one
    continuous ridged back): the dark violet hide as one body, shingled armour plates over it (each plate's tail edge
    stands proud over the next, a worn rim on it, a low bone keel spike along the crest), the hide showing along the
    flanks under the plates, a sand berm along both flanks, a sealed pore under each vent (segments 0-2); the sand heaped
    on the plates while it rises (state 0, 1). ``heading``: the spine's direction from the mouth toward the tail, k *
    22.5 degrees clockwise from straight down the screen (the ridge's heading rule)."""
    c0 = SPINE_DIST[seg] / S
    front, back = (v / S for v in spine_span(seg))
    sink = SPINE_STATES[state]
    th = TAU * heading / SPINE_HEADINGS
    dir_ = np.array([-math.sin(th), -math.cos(th)])          # world (y up): down the screen at heading 0
    perp = np.array([math.cos(th), -math.sin(th)])
    pitch = SPINE_PITCH / S
    edge0 = SPINE_CUT[0] / S
    rng = np.random.default_rng(5200 + seg * 10 + state)
    clumps = []
    for _ in range((26, 12, 0)[state]):
        s, v = rng.uniform(front + 0.1, back - 0.05), rng.uniform(-0.75, 0.75)
        w = float(spine_width(s))
        h = 0.72 * w
        top = h * math.sqrt(max(0.0, 1 - v * v)) - sink * h
        if top > 0.03:
            r = rng.uniform(0.06, 0.11) * (w / 0.47) ** 0.5
            clumps.append((v * w, s - c0, top + r * 0.15, r))
    berm_h = (0.07, 0.055, 0.045)[state]

    def scene(p):
        x = p[:, 0] * perp[0] + p[:, 1] * perp[1]
        y = p[:, 0] * dir_[0] + p[:, 1] * dir_[1]
        z = p[:, 2]
        s = c0 + y                                             # units from the mouth along the back
        w = spine_width(s)
        h = 0.72 * w
        dive = np.clip((s - 4.9) / 0.7, 0, 1) * 0.9 * h        # the tail dives into the sand, the front by the collar
        dive = dive + np.clip((82 - s * S) / 20, 0, 1) * 0.95 * h
        zz = z + sink * h + dive
        hide = np.maximum(_ellipse(x, zz, w, h), s - SPINE_TIP / S)   # capped at the tail's tip
        items = [(hide, W_HIDE)]
        # the plates: plate j spans [edge0 + (j - 1) pitch, edge0 + j pitch]; t runs 0 (tucked under the plate in
        # front) -> 1 (its proud tail edge); the edge bows toward the head on the flanks (scale-like shingles)
        bow = 0.09 * (x / w) ** 2
        sj = s + bow - edge0
        j = np.ceil(sj / pitch)
        t = 1.0 - (j * pitch - sj) / pitch
        wf = np.clip(w / 0.33, 0.15, 1.0)                    # the plates thin out toward the tail's tip
        e = (0.022 + 0.07 * t ** 1.5) * wf
        plate = _ellipse(x, zz, w + e, h + e)
        plate = np.maximum(plate, -(zz - 0.58 * h))                       # the top only: the flanks stay hide
        items.append((plate, W_PLATE))
        rim = np.maximum(plate - 0.006, (1.0 - t) * pitch - 0.035)             # the worn tail edge of each plate
        items.append((rim, W_RIM))
        # a low bone keel spike on each plate's crest, leaning back to its proud edge
        ky = j * pitch                                         # the plate's tail edge (in sj)
        kt = np.clip((sj - (ky - 0.75 * pitch)) / (0.75 * pitch), 0, 1)
        keel = np.sqrt(x ** 2 + (zz - (h + e) - kt * 0.07 * w / 0.47) ** 2) - (0.012 + 0.03 * kt) * (w / 0.47) * wf
        keel = np.maximum(keel, np.abs(sj - (ky - 0.38 * pitch)) - 0.38 * pitch)
        rq = np.sqrt(x ** 2 + y ** 2)
        if seg < 3:                                            # no keel spike through the vent's pore
            keel = np.maximum(keel, 0.2 - rq)
        items.append((keel, V_BONE))
        if seg < 3:                                            # the sealed pore the vent organ breaks out of
            items.append((np.sqrt((rq - 0.085) ** 2 + (zz - (h + 0.06)) ** 2) - 0.035, V_SEAM))
        for cx, cy, cz, r in clumps:
            items.append((sd_ellipsoid(np.stack([x, y, z], axis=1), (cx, cy, cz), (r, r * 1.2, r * 0.55)), W_SAND))
        d, m = union(*items, k=0.012)
        n = 0.016 * np.sin(p[:, 0] * 23 + 1.1) * np.sin(p[:, 1] * 19)
        ground = z - n
        bx = np.abs(x) - (w + 0.01)                            # a low sand berm hugging both flanks
        fade = np.clip(np.minimum((SPINE_TIP / S + 0.1 - s) / 0.5, (s * S - 62) / 20), 0, 1)   # closing at the ends
        berm = _ellipse(bx, z + 0.02, 0.06 + 0.015 * (2 - state), berm_h * fade + 0.02)
        sand_d = np.minimum(berm, np.maximum(ground - 0.035, hide - 0.04))
        hh = np.clip(0.5 + 0.5 * (sand_d - d) / 0.05, 0, 1)
        m = np.where(sand_d < d, W_SAND, m)
        d = sand_d * (1 - hh) + d * hh - 0.05 * hh * (1 - hh)
        return np.maximum(d / 1.15, -ground), m
    return scene


def spine_mask(seg, heading, size):
    """The native frame's pixels on segment ``seg``'s stretch of the back (the model is rendered whole and cut
    afterwards, so the cut has no lit face nor a sharpening halo): toward the mouth where the back has dived into the
    sand (segment 0) or under the segment before it, toward the tail on a plate's proud edge."""
    front, back = (v / S for v in spine_span(seg))
    th = TAU * heading / SPINE_HEADINGS
    c = (np.arange(size) + 0.5 - size / 2) / S
    gx, gy = np.meshgrid(c, -c)
    s = SPINE_DIST[seg] / S - gx * math.sin(th) - gy * math.cos(th)
    return (s >= front) & (s <= back)


# --------------------------------------------------------------------------- the throe (round 44 K.b)

THROE = 400                              # the reared forebody's render canvas (bigger than the open mouth)
REAR_STATES = [  # (scene kwargs, glow, weak): the forebody rearing out of the sand, screaming
    (dict(lift=0.75, open_=1.05, sand=("slide", 0.5)), 1.3, 1.2),
    (dict(lift=1.4, open_=1.2, tilt=((0.0, -1.0), 0.28)), 1.4, 1.3),
    (dict(lift=1.75, open_=1.3, tilt=((0.8, 0.6), 0.22), hot=True), 1.5, 1.5),
    (dict(lift=1.6, open_=1.15, tilt=((-0.9, 0.3), 0.32), ichor=10), 1.2, 1.1),
]
COLLAPSE_STATES = [  # falling back into the sand (then death_4, death_5 and the Level 21 sinkhole)
    (dict(lift=1.0, open_=0.85, tilt=((0.3, -1.0), 0.4), droop=0.4, ichor=12), 0.8, 0.7),
    (dict(lift=0.35, open_=0.55, tilt=((0.2, -1.0), 0.25), droop=0.8, ichor=10, sand=("pour", 0.4)), 0.45, 0.4),
    (dict(rise=0.85, open_=0.35, droop=1.0, ichor=8, sand=("pour", 0.7)), 0.25, 0.2),
]
DEBRIS = 48


def debris_scene(shape, spin):
    """A big rock flung up by the throe (half again the debris rock), some with lime ichor on them."""
    inner = rock_scene(shape, spin)
    k = 1.5
    spots = [(0.05, -0.04, 0.36, 0.04), (-0.06, 0.05, 0.33, 0.035)] if shape % 2 else []

    def scene(p):
        d, m = inner(p / k)
        d = d * k
        for x, y, z, r in spots:
            ds = sd_sphere(p, (x * k, y * k, z * k), r * k)
            m = np.where(ds < d, W_ICHOR, m)
            d = np.minimum(d, ds)
        return d, m
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

REV = {"vent": 2, "spine": 5}                         # round 45: the vent is an organ now (the cache keys change with it)


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
        return vent_scene(st), mats(weak=(0.15, 0.5, 1.4, 0.0)[st]), VENT
    if kind == "spine":
        _, seg, st, k = job
        return spine_scene(seg, st, k), mats(glow=0.55 + 0.15 * st, weak=0.2), spine_canvas(seg)
    if kind in ("rear", "collapse"):
        _, st = job
        kw, glow, weak = (REAR_STATES if kind == "rear" else COLLAPSE_STATES)[st]
        return (mouth_scene("under", seed=70 + st + (0 if kind == "rear" else 5), plates=True, **kw),
                mats(glow=max(glow, 0.05), weak=weak, hot=1.0 if kw.get("hot") else 0.0), THROE)
    if kind == "debris":
        _, shape, spin = job
        return debris_scene(shape, spin), mats(), DEBRIS
    if kind == "rock":
        _, shape, spin = job
        return rock_scene(shape, spin), mats(), ROCK
    raise ValueError(kind)


def render_job(job):
    path = CACHE / f"{_job_key(job)}.png"
    if path.exists():
        return Image.open(path).convert("RGBA")
    scene, m, size = model_for(job)
    # the spine: a far key light (as a sun), so every segment of the one back is lit alike and the joints match
    kw = dict(key_pos=tuple(SPINE_KEY)) if job[0] == "spine" else {}
    hi, factor = artkit.render_hi(scene, m, (size, size), size / S, **kw)
    img = artkit.native(hi, factor)
    if job[0] == "spine":
        a = np.array(img)
        a[~spine_mask(job[1], job[3], size)] = 0
        img = Image.fromarray(a, "RGBA")
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


GEYSER, GEYSER_N = 144, 8
FISSURE, FISSURE_N, FISSURE_R = 280, 4, 118.0
PLUME, PLUME_N = 256, 3


def geyser_frame(i):
    """The throe's sand geyser bursting out of the ridge (round 44 K.b), seen from above, by age: a dense column
    of sand thrown up at the camera (its top lit), jets of sand flung out on rays, lime ichor spraying from it;
    the column falls back into a spreading ring of dust that thins out (stepped translucency)."""
    u = (i + 0.5) / GEYSER_N
    Sx = 3
    c = GEYSER / 2
    rng = np.random.default_rng(4600)
    rays = [(rng.uniform(0, TAU), rng.uniform(0.75, 1.15)) for _ in range(9)]
    items = []
    col = max(0.0, 1 - u / 0.62)                                # the column, falling back after ~60 % of its life
    if col > 0:
        rc = 24 + 22 * min(1.0, u / 0.3)
        items.append((c, c - 6 * col, rc, 0.98 * col ** 0.4))
        items.append((c - 0.3 * rc, c - 0.3 * rc - 4 * col, rc * 0.6, 0.95 * col ** 0.4))
    for a, sp in rays:                                          # the jets flung out on rays
        for k in range(3):
            dist = (22 + 46 * sp * min(1.0, u * 1.8)) * (0.45 + 0.27 * k)
            r = (13 - 3 * k) * (0.9 + 0.5 * u)
            items.append((c + math.cos(a) * dist, c + math.sin(a) * dist - 4 * (1 - u), r, 0.9 * (1 - u) ** 0.8))
    ring_r = 34 + 30 * u                                        # the dust falling back in a ring
    for k in range(14):
        a = k * TAU / 14 + 0.4 * math.sin(k * 2.3)
        items.append((c + math.cos(a) * ring_r, c + math.sin(a) * ring_r * 0.92, 12 + 9 * u,
                      0.9 * min(1.0, u / 0.25) * (1 - u) ** 0.7))
    alpha, light, noise = su.blobs(GEYSER, GEYSER, Sx, items, 4601 + i)
    colr = b15.ramp(FLOOR, np.clip(-0.05 + 1.15 * light + 0.12 * (noise - 0.5), 0, 1))
    colr = colr * (1 - 0.25 * (1 - u)) + np.array((236, 226, 196), float) * 0.25 * (1 - u) * light[..., None]
    if u < 0.8:                                                 # lime ichor sprayed on the rays
        yy, xx = np.mgrid[0:GEYSER * Sx, 0:GEYSER * Sx].astype(np.float64) / Sx
        drop = np.zeros_like(xx)
        for j in range(34):
            a = rays[j % len(rays)][0] + rng.uniform(-0.35, 0.35)
            sp = rng.uniform(22, 58)
            dr = 8 + sp * u * 1.5
            x, y = c + math.cos(a) * dr, c + math.sin(a) * dr - 8 * u
            size = rng.uniform(1.6, 3.0) * (1 - 0.45 * u)
            tail = np.hypot(xx - (x - math.cos(a) * 5 * u), yy - (y - math.sin(a) * 5 * u))
            drop = np.maximum(drop, np.clip(1.5 - np.hypot(xx - x, yy - y) / size, 0, 1))
            drop = np.maximum(drop, 0.6 * np.clip(1.2 - tail / (size * 0.8), 0, 1))
        drop = np.clip(drop, 0, 1)
        lime = LIME * 0.8 + np.array((255, 255, 210), float) * 0.2
        colr = colr * (1 - drop[..., None]) + lime * drop[..., None]
        alpha = np.maximum(alpha, drop)
    rgb, al = su.shrink(colr, alpha, Sx)
    return su.stepped(rgb, np.clip(al, 0, 1) * 0.95, steps=4)


def _fissure_cracks(seed, n, r0, r1, size, f, k, width):
    """Smooth radial cracks from radius r0 out to r1 (grown to ``k`` of their length), a 0..1 mask."""
    rng = np.random.default_rng(seed)
    c = size / 2
    img = Image.new("L", (size * f, size * f), 0)
    d = ImageDraw.Draw(img)
    for j in range(n):
        a = j * TAU / n + rng.uniform(-0.2, 0.2)
        r, pts = r0 + rng.uniform(-6, 6), []
        end = r0 + (r1 - r0) * rng.uniform(0.6, 1.0) * k
        while r < end:
            pts.append(((c + math.cos(a) * r) * f, (c + math.sin(a) * r) * f))
            r += 5
            a += rng.uniform(-0.06, 0.06)
        if len(pts) > 1:
            d.line(pts, fill=255, width=max(1, int(round(width * f))))
            if rng.uniform() < 0.5 and len(pts) > 3:             # a fork
                bx, by = pts[len(pts) // 2]
                b = a + rng.choice((-0.5, 0.5))
                d.line([(bx, by), (bx + math.cos(b) * 14 * f * k, by + math.sin(b) * 14 * f * k)], fill=255,
                       width=max(1, int(round(width * 0.7 * f))))
    return np.array(img, float) / 255


def fissure_frame(i):
    """The fissure ring cracking open round the throe (a ground decal, centred on the mouth): a fissure at ~118
    px (just outside the Level 21 sinkhole's rim) tearing round, its lip heaved up outside and the crust slumping
    inside, radial cracks running into it, lime ichor light down in the fresh gap, dark once it has settled."""
    b = (i + 1) / FISSURE_N
    f = 2
    c = FISSURE / 2
    yy, xx = b15.piece_canvas((FISSURE, FISSURE), f)
    r = np.hypot(xx - c, yy - c)
    ang = np.arctan2(yy - c, xx - c)
    n = raster.fbm(FISSURE * f, FISSURE * f, 12, 4701, octaves=3, period=False)
    rr = FISSURE_R + 5 * np.sin(ang * 5 + 0.6) + 3 * np.sin(ang * 11 + 2.0) + 4 * (n - 0.5)
    spread = (np.sin(ang * 3 + 0.9) * 0.5 + np.sin(ang * 7 + 0.2) * 0.5) > 1.0 - 2.6 * b
    width = (0.8 + 3.2 * b) * (0.7 + 0.6 * n)
    ring = np.clip(1 - np.abs(r - rr) / width, 0, 1) * spread
    radial = _fissure_cracks(4702, 14, 64, FISSURE_R + 4, FISSURE, f, min(1.0, 0.35 + 0.75 * b), 0.8 + 0.7 * b)
    outer = _fissure_cracks(4703, 10, FISSURE_R + 2, FISSURE_R + 22, FISSURE, f, min(1.0, b * 1.3), 0.9)
    crack = np.clip(ring + radial + outer, 0, 1)
    heave = 5.0 * b * np.exp(-((r - rr - 8) / 8) ** 2) * (0.7 + 0.6 * n) * (spread | (b > 0.6))
    slump = -4.0 * b * np.clip((rr - r) / 26, 0, 1) * (r > 60)
    hgt = heave + slump - 4.0 * crack + 0.8 * (n - 0.5)
    band = np.exp(-((r - rr) / (10 + 8 * b)) ** 2)
    dil = crack.copy()
    for _ in range(3):
        dil = np.maximum.reduce([dil, np.roll(dil, f, 0), np.roll(dil, -f, 0), np.roll(dil, f, 1), np.roll(dil, -f, 1)])
    cover = np.clip((np.maximum(band * (0.6 + 0.6 * b), dil) + 0.3 * (n - 0.5) - 0.35) * 4, 0, 1)
    cover *= (r > 56)
    t = 0.5 + 0.14 * heave / 5 - 0.16 * np.clip(-slump / 4, 0, 1) + 0.1 * (n - 0.5)
    alb = b15.ramp(FLOOR, np.clip(t, 0, 1))
    alb = alb * (1 - 0.9 * crack[..., None]) + np.array((34, 24, 22), float) * 0.9 * crack[..., None]
    glow = (0.6, 1.3, 1.0, 0.15)[i]
    deep = np.clip(1 - np.abs(r - rr) / np.maximum(width * 0.6, 0.5), 0, 1) * spread
    em = (crack * 0.5 + deep)[..., None] * (LIME / 255 * glow)
    img = np.array(l20.finish_flat(hgt, alb, cover, f, emission=em, colours=32)).astype(np.float64)
    # the ichor light down in the fresh gap, laid over the lit relief (the relief's recede would grey it)
    dn = deep.reshape(FISSURE, f, FISSURE, f).mean(axis=(1, 3))
    k = (np.clip((dn - 0.2) / 0.4, 0, 1) * min(1.0, glow) * 0.9 * (img[..., 3] > 0))[..., None]
    hot = LIME * (0.75 + 0.25 * np.clip(dn, 0, 1))[..., None] + np.array((255, 255, 210), float) * 0.15
    img[..., :3] = img[..., :3] * (1 - k) + np.clip(hot, 0, 255) * k
    return Image.fromarray(img.astype(np.uint8), "RGBA")


def plume_frame(i):
    """The dust plume rolling out of the collapse: a large soft cloud sheet (smooth alpha, no steps: the game
    scales it up and fades it), billows lit from the key light; 0 dense and young, 1 spreading, 2 thin and wide."""
    k = i / (PLUME_N - 1)
    Sx = 2
    c = PLUME / 2
    rng = np.random.default_rng(4800)
    items = []
    R = 52 + 46 * k
    items.append((c, c, R * 0.85, (0.92 - 0.45 * k) * (1 - 0.6 * k)))
    for j in range(16):
        a = j * TAU / 16 + rng.uniform(-0.2, 0.2)
        d = R * rng.uniform(0.55, 0.95)
        items.append((c + math.cos(a) * d, c + math.sin(a) * d * 0.94, R * rng.uniform(0.32, 0.46),
                      (0.85 - 0.35 * k) * rng.uniform(0.75, 1.0)))
    alpha, light, noise = su.blobs(PLUME, PLUME, Sx, items, 4801 + i)
    yy, xx = np.mgrid[0:PLUME * Sx, 0:PLUME * Sx].astype(np.float64) / Sx
    soft = raster.fbm(PLUME * Sx, PLUME * Sx, 22 * Sx, 4810 + i, octaves=4, period=False)
    fall = np.clip(1.25 - np.hypot(xx - c, yy - c) / (R * 1.35), 0, 1) ** 0.8
    alpha = np.clip(alpha * (0.75 + 0.6 * soft) * fall * 1.25, 0, 1) * (0.92 - 0.25 * k)
    colr = b15.ramp(FLOOR, np.clip(0.42 + 0.6 * light + 0.25 * (soft - 0.5), 0, 1))
    rgb, al = su.shrink(colr, alpha, Sx)
    img = np.dstack([np.clip(rgb, 0, 255), np.clip(al, 0, 1) * 255]).astype(np.uint8)
    img[img[..., 3] < 3] = 0
    return Image.fromarray(img, "RGBA")


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
        "spine": {"distances": list(SPINE_DIST), "states": len(SPINE_STATES), "headings": SPINE_HEADINGS,
                  "note": "dust-colossus-spine frame (segment * states + state) * headings + heading; segment i "
                          "centred distances[i] px from the mouth's centre toward the field's centre; heading k "
                          "turns k * 22.5 degrees clockwise from straight down (the ridge's rule)"},
        "fissure_radius": FISSURE_R,
    }


SET_NAMES = ["dust-colossus-shut", "dust-colossus-mouth", "dust-colossus-gullet", "dust-colossus-death",
             "dust-colossus-vent", "dust-colossus-bulge", "dust-colossus-marker", "dust-colossus-funnel",
             "dust-colossus-burst", "dust-colossus-rock", "dust-colossus-spine", "dust-colossus-rear",
             "dust-colossus-collapse", "dust-colossus-geyser", "dust-colossus-fissure", "dust-colossus-plume",
             "dust-colossus-debris"]

# build groups: python3 tools/art/dust_colossus.py [group ...] renders and writes only those (the default all)
GROUPS = {
    "body": ["dust-colossus-shut", "dust-colossus-mouth", "dust-colossus-gullet", "dust-colossus-death"],
    "vent": ["dust-colossus-vent"],
    "ground": ["dust-colossus-bulge", "dust-colossus-marker", "dust-colossus-funnel"],
    "rocks": ["dust-colossus-burst", "dust-colossus-rock"],
    "spine": ["dust-colossus-spine"],
    "throe": ["dust-colossus-rear", "dust-colossus-collapse", "dust-colossus-geyser", "dust-colossus-fissure",
              "dust-colossus-plume", "dust-colossus-debris"],
}


def jobs(groups=None):
    want = set(GROUPS) if not groups else set(groups)
    js = []
    if "body" in want:
        js += [("shut", p, st) for p in range(5) for st in range(len(SHUT_STATES))]
        js += [("mouth", p, st) for p in range(5) for st in range(len(MOUTH_STATES))]
        js += [("death", st) for st in range(len(DEATH_STATES))]
    if "vent" in want:
        js += [("vent", st) for st in range(4)]
    if "rocks" in want:
        js += [("rock", sh, sp) for sh in range(4) for sp in range(4)]
    if "throe" in want:                                       # the big renders first: they bound the wall time
        js = [("rear", st) for st in range(len(REAR_STATES))] + [("collapse", st) for st in range(len(COLLAPSE_STATES))] + js
        js += [("debris", sh, sp) for sh in range(4) for sp in range(4)]
    if "spine" in want:
        js += [("spine", seg, st, k) for seg in range(len(SPINE_DIST)) for st in range(len(SPINE_STATES))
               for k in range(SPINE_HEADINGS)]
    return js


def spine_index(seg, state, heading):
    """dust-colossus-spine's frame: (segment * 3 + state) * 16 + heading."""
    return (seg * len(SPINE_STATES) + state) * SPINE_HEADINGS + heading


def build(groups=None):
    want = set(GROUPS) if not groups else set(groups)
    unknown = want - set(GROUPS)
    if unknown:
        raise SystemExit(f"unknown group(s) {sorted(unknown)}; groups: {sorted(GROUPS)}")
    js = jobs(want)
    with ProcessPoolExecutor() as pool:
        out = dict(zip(js, pool.map(render_job, js)))
        flat = {}
        if "ground" in want:
            flat["bulge"] = list(pool.map(bulge_frame, range(4)))
            flat["marker"] = list(pool.map(marker_frame, range(4)))
            flat["funnel"] = list(pool.map(funnel_frame, range(len(FUNNEL_STATES))))
        if "rocks" in want:
            flat["burst"] = list(pool.map(burst_frame, range(8)))
        if "throe" in want:
            flat["geyser"] = list(pool.map(geyser_frame, range(GEYSER_N)))
            flat["fissure"] = list(pool.map(fissure_frame, range(FISSURE_N)))
            flat["plume"] = list(pool.map(plume_frame, range(PLUME_N)))

    def of(kind):
        return [out[j] for j in js if j[0] == kind]
    sets = {}
    if "body" in want:
        shut, mouth, death = centred_crop(of("shut")), centred_crop(of("mouth")), centred_crop(of("death"))
        body = act3kit.quantize_bright(shut + mouth + death, 56)      # one palette: no flicker between the sets
        shut, mouth, death = body[:len(shut)], body[len(shut):len(shut) + len(mouth)], body[len(shut) + len(mouth):]
        sets["dust-colossus-shut"] = shut
        sets["dust-colossus-mouth"] = mouth
        sets["dust-colossus-gullet"] = [gullet_glow(p) for p in range(5)]
        sets["dust-colossus-death"] = death
    if "vent" in want:
        sets["dust-colossus-vent"] = act3kit.quantize_bright(of("vent"), 28)
    if "ground" in want:
        sets["dust-colossus-bulge"] = artkit.quantize_set(centred_crop(flat["bulge"]), 32)
        sets["dust-colossus-marker"] = artkit.quantize_set(centred_crop(flat["marker"]), 24)
        sets["dust-colossus-funnel"] = artkit.quantize_set(centred_crop(flat["funnel"]), 40)
    if "rocks" in want:
        sets["dust-colossus-burst"] = artkit.quantize_set(flat["burst"], 20)
        sets["dust-colossus-rock"] = artkit.quantize_set(centred_crop(of("rock")), 16)
    if "spine" in want:
        frames = [None] * (len(SPINE_DIST) * len(SPINE_STATES) * SPINE_HEADINGS)
        for seg in range(len(SPINE_DIST)):
            for k in range(SPINE_HEADINGS):                  # cropped per heading about the segment's centre
                group = centred_crop([out[("spine", seg, st, k)] for st in range(len(SPINE_STATES))])
                for st, f in enumerate(group):
                    frames[spine_index(seg, st, k)] = f
        sets["dust-colossus-spine"] = act3kit.quantize_bright(frames, 40)
    if "throe" in want:
        throe = centred_crop(of("rear") + of("collapse"))
        throe = act3kit.quantize_bright(throe, 56)
        sets["dust-colossus-rear"] = throe[:len(REAR_STATES)]
        sets["dust-colossus-collapse"] = throe[len(REAR_STATES):]
        sets["dust-colossus-geyser"] = artkit.quantize_set(flat["geyser"], 24)
        sets["dust-colossus-fissure"] = artkit.quantize_set(centred_crop(flat["fissure"]), 32)
        sets["dust-colossus-plume"] = flat["plume"]
        sets["dust-colossus-debris"] = artkit.quantize_set(centred_crop(of("debris")), 20)
    for name, frames in sets.items():
        write(name, frames)
    with target():
        artkit.write_pivots(ROOT_NAME, SOURCE, pivots())
    if "body" in want:
        import intel                                             # tools/art/intel.py: the sensor silhouette
        intel.build(["boss-dust-colossus"])
    budget()


def budget():
    """The pixel budget of every written set (one 2048 x 2048 atlas page is 4.19 M px)."""
    total = pad = 0
    for name in SET_NAMES:
        frames = load(name)
        px = sum(f.width * f.height for f in frames)
        pd = sum((f.width + 4) * (f.height + 4) for f in frames)
        total, pad = total + px, pad + pd
        print(f"    {name}: {len(frames)} fr, {px / 1e6:.3f} M px")
    print(f"  {total / 1e6:.2f} M px ({pad / 1e6:.2f} M with the packer's padding; one page is 4.19 M)")


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


def heading_of(dx, dy):
    """The ridge's heading rule on the screen (x right, y down): k * 22.5 degrees clockwise from straight down."""
    return int(round(math.atan2(-dx, dy) / (TAU / 16))) % 16


def spine_rise_state(seg, tr):
    """A segment's frame tr s into the mouth's 1.5 s rise (each 0.12 s behind the one before it), None while
    still under the sand."""
    u = tr - 0.12 * seg
    return None if u < 0 else min(2, int(u / 0.5))


def spine_dive_state(seg, td):
    """A segment's frame td s into the mouth's 1.5 s dive: 1, then 0, then gone (None), each 0.12 s behind."""
    u = td - 0.12 * seg
    return 2 if u < 0 else (1 if u < 0.5 else (0 if u < 1.0 else None))


def spine_draw(img, s, mouth, states, vents=None):
    """The spine behind a risen mouth toward the field's centre: segment i at SPINE_DIST[i] px in its frame
    states[i] (None: under the sand), tail first; the vent organs over segments 0-2 (vents[j] its frame)."""
    mx, my = mouth
    dx, dy = FW / 2 - mx, 270 - my
    L = math.hypot(dx, dy)
    k = heading_of(dx, dy)
    for seg in reversed(range(len(SPINE_DIST))):
        if states[seg] is not None:
            d = SPINE_DIST[seg]
            paste_c(img, s["dust-colossus-spine"][spine_index(seg, states[seg], k)], mx + dx / L * d, my + dy / L * d)
    for j, st in enumerate(vents or []):
        if st is not None and states[j] is not None:
            d = SPINE_DIST[j]
            paste_c(img, s["dust-colossus-vent"][st], mx + dx / L * d, my + dy / L * d)


THROE_SEQ = [(0.3, "rear", 0), (0.65, "rear", 1), (1.1, "rear", 2), (1.35, "rear", 3), (1.6, "rear", 2),
             (1.85, "rear", 3), (2.2, "collapse", 0), (2.55, "collapse", 1), (2.9, "collapse", 2), (3.25, "death", 4),
             (3.6, "death", 5)]
THROE_SECONDS = 4.5


def throe_frame(a):
    """The body's frame a s into the throe ((set, index)), None after the collapse."""
    for end, name, i in THROE_SEQ:
        if a < end:
            return name, i
    return None


def scaled(sp, k):
    return sp.resize((max(1, int(sp.width * k)), max(1, int(sp.height * k))), Image.BILINEAR)


def faded(sp, k):
    a = np.array(sp).copy()
    a[..., 3] = (a[..., 3] * max(0.0, min(1.0, k))).astype(np.uint8)
    return Image.fromarray(a, "RGBA")


def review_gif(s, until=None):
    """Round 45: a rear breach in phase 2 (the spine and its vent organs rising with the mouth, venting, one
    destroyed, sinking with the dive), a following front breach (a slanted spine), then a breach under the ship
    and the 4.5 s death throe (rearing, the geysers along the ridge, the fissure ring, the debris, the collapse,
    the plume, the flash, the sinkhole)."""
    field = Field()
    shipf = load_game("ship")
    shipf = shipf[len(shipf) // 2]
    dust = l20.load("colossus-ridge-dust")
    sink = ROOT / "assets" / "backdrop" / "level-21" / "colossus-sinkhole.png"
    sinkhole = Image.open(sink).convert("RGBA") if sink.exists() else None
    P1, P2, P3 = (240, 515), (130, 30), (300, 300)
    T = until or 23.0
    rng = np.random.default_rng(45)
    rocks = [(rng.uniform(0.7, 3.0), rng.uniform(30, 450), rng.uniform(40, 500), int(rng.integers(16)))
             for _ in range(18)]
    kill = 17.0
    death_ridge = ((60, 560), (P3[0] - 40, P3[1] + 50))
    frames = []
    for fi in range(int(T * FPS)):
        t = fi / FPS
        img = field.frame(t)
        label = ""
        # ------------------------------------------------ the rear breach (phase 2)
        if t < 3.0:
            img = ridge_draw(img, s, (FW / 2, 250), (P1[0], P1[1] - 60), min(1.0, t / 2.6))
            label = "PHASE 2: THE RIDGE COMES IN, THE BULGE"
        if 1.0 <= t < 3.0:
            paste_c(img, s["dust-colossus-bulge"][min(3, int((t - 1.0) / 0.5))], *P1)
        if 3.0 <= t < 9.5:
            fu = 0 if t < 3.75 else (4 if t >= 8.0 else 1 + int(t * 6) % 3)
            paste_c(img, s["dust-colossus-funnel"][fu], *P1)
        if t >= 9.5:
            paste_c(img, s["dust-colossus-funnel"][5], P1[0], P1[1] + ground_step(t) - ground_step(9.5))
        if 3.0 <= t < 9.5:
            if t < 8.0:
                states = [spine_rise_state(i, t - 3.0) for i in range(4)]
            else:
                states = [spine_dive_state(i, t - 8.0) for i in range(4)]
            vents = []
            for j in range(3):
                if states[j] is None:
                    vents.append(None)
                elif j == 1 and t >= 6.0:
                    vents.append(3)
                elif states[j] < 2 or t < 4.5 or t >= 8.0:
                    vents.append(0)
                else:
                    vents.append(2 if (t - 4.8 - 0.3 * j) % 1.2 < 0.25 else 1)
            spine_draw(img, s, P1, states, vents)
            if 6.0 <= t < 6.5:
                dx, dy = FW / 2 - P1[0], 270 - P1[1]
                L = math.hypot(dx, dy)
                paste_c(img, s["dust-colossus-burst"][min(7, int((t - 6.0) / 0.0625))], P1[0] + dx / L * 200,
                        P1[1] + dy / L * 200 - 8)
        p = 3
        if 3.0 <= t < 4.5:
            paste_c(img, s["dust-colossus-shut"][p * 6 + min(2, int((t - 3.0) / 0.5))], *P1)
            label = "THE RISE: THE SPINE BREAKS THE SAND BEHIND THE MOUTH"
        elif 4.5 <= t < 7.0:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + (0 if t < 4.8 else 1)], *P1)
            label = "OPEN: THE VENT ORGANS ON THE SPINE VENT" if t < 6.0 else "VENT 2 DESTROYED (SCARRED, STAYS DEAD)"
        elif 7.0 <= t < 7.8:
            paste_c(img, s["dust-colossus-mouth"][p * 3 + 2], *P1)
            label = "THE BITE'S TELL"
        elif 7.8 <= t < 8.0:
            paste_c(img, s["dust-colossus-shut"][p * 6 + 3], *P1)
        elif 8.0 <= t < 9.5:
            paste_c(img, s["dust-colossus-shut"][p * 6 + (4 if t < 8.75 else 5)], *P1)
            label = "THE DIVE: THE SPINE SINKS, HEAD FIRST"
        # ------------------------------------------------ the front breach, following (a slanted spine)
        if 9.5 <= t < 15.5:
            fu = 0 if t < 10.25 else (4 if t >= 14.0 else 1 + int(t * 6) % 3)
            paste_c(img, s["dust-colossus-funnel"][fu], *P2)
            if t < 14.0:
                states = [spine_rise_state(i, t - 9.5) for i in range(4)]
            else:
                states = [spine_dive_state(i, t - 14.0) for i in range(4)]
            vents = [None if states[j] is None else (3 if j == 1 else (0 if states[j] < 2 or t < 11.0 or t >= 14.0
                     else (2 if (t - 11.3 - 0.3 * j) % 1.2 < 0.25 else 1))) for j in range(3)]
            spine_draw(img, s, P2, states, vents)
            p = 0
            if t < 11.0:
                paste_c(img, s["dust-colossus-shut"][p * 6 + min(2, int((t - 9.5) / 0.5))], *P2)
            elif t < 14.0:
                paste_c(img, s["dust-colossus-mouth"][p * 3 + (0 if t < 11.3 else 1)], *P2)
            else:
                paste_c(img, s["dust-colossus-shut"][p * 6 + (4 if t < 14.75 else 5)], *P2)
            label = "A FOLLOWING FRONT BREACH: THE SPINE AT A SLANT (16 HEADINGS)"
        # ------------------------------------------------ under the ship, the kill, the throe
        a = t - kill
        if 15.5 <= t < kill:
            paste_c(img, s["dust-colossus-funnel"][1 + int(t * 6) % 3], *P3)
            img = ridge_draw(img, s, death_ridge[0], death_ridge[1], 1.0)
            paste_c(img, s["dust-colossus-mouth"][4 * 3 + 1], *P3)
            label = "PHASE 3: OPEN UNDER THE SHIP"
        if a >= 0:
            if a >= 3.3 and sinkhole is not None:
                paste_c(img, faded(sinkhole, (a - 3.3) / 0.6), *P3)
            if a < 3.6:
                paste_c(img, s["dust-colossus-funnel"][1 + int(t * 6) % 3 if a < 2.0 else 4], *P3)
            if a >= 0.9:
                paste_c(img, s["dust-colossus-fissure"][0 if a < 1.2 else (1 if a < 1.5 else (2 if a < 1.9 else 3))], *P3)
            if a < 2.2:
                img = ridge_draw(img, s, death_ridge[0], death_ridge[1], 1.0, glow=1.0)
            (ax, ay), (bx, by) = death_ridge
            n_g = 9
            for j in range(n_g):
                t0 = 0.5 + 1.5 * j / (n_g - 1)
                if t0 <= a < t0 + 0.56:
                    u = j / (n_g - 1)
                    paste_c(img, s["dust-colossus-geyser"][min(7, int((a - t0) / 0.07))],
                            ax + (bx - ax) * u, ay + (by - ay) * u - 6)
            fr = throe_frame(a)
            if fr is not None:
                name, i = fr
                paste_c(img, s[f"dust-colossus-{name}"][i], *P3)
            for t0, x0, y0, k in rocks:
                u = (a - t0) / 0.9
                if 0 <= u < 1:
                    paste_c(img, scaled(s["dust-colossus-debris"][(k // 4) * 4 + int(a * 10) % 4], 1.6 - 0.6 * u),
                            x0, y0 - 40 * (1 - u))
                elif 1 <= u < 1.4:
                    paste_c(img, dust[min(len(dust) - 1, int((u - 1) / 0.4 * len(dust)))], x0, y0)
            if 1.9 <= a < THROE_SECONDS:
                pa = a - 1.9
                pk = 0 if pa < 0.8 else (1 if pa < 1.6 else 2)
                fade = 1.0 if pa < 1.3 else max(0.0, 1 - (pa - 1.3) / 1.3)
                paste_c(img, faded(scaled(s["dust-colossus-plume"][pk], 1.0 + 0.6 * pa / 2.6), fade), *P3)
            if a < 0.9:
                label = "THE THROE: THE FOREBODY REARS OUT OF THE SAND, SCREAMING"
            elif a < 1.9:
                label = "THE RIDGE BURSTS TAIL TO HEAD, THE FISSURE RING, DEBRIS"
            elif a < 3.6:
                label = "THE COLLAPSE INTO THE SINKHOLE, THE DUST PLUME"
            else:
                label = "THE FLASH, THE CREDIT SHOWER; THE SINKHOLE STAYS"
        sx, sy = ship_path(t)
        paste_c(img, shipf, sx, sy)
        if 4.2 <= a < 4.65:
            k = 1.0 - (a - 4.2) / 0.45
            img.alpha_composite(Image.new("RGBA", img.size, (255, 250, 235, int(210 * k))))
        raster.draw_text(img, 8, FH - 14, f"DUST COLOSSUS FINAL {REVIEW_ROUND.upper()} - {t:4.1f} S  {label}", raster.LABEL)
        frames.append(img)
    return frames


def ground_step(t):
    """The review field's scroll (px) at t: 20 px/s in 0.5 s steps."""
    return 10 * int(t * 2)


def ship_path(t):
    keys = [(0, (250, 300)), (3, (230, 330)), (7, (300, 300)), (9.5, (260, 330)), (12, (200, 330)),
            (15.5, (300, 420)), (17, (300, 430)), (20, (330, 470)), (23, (300, 470))]
    for (t0, a), (t1, b) in zip(keys, keys[1:]):
        if t < t1:
            u = (t - t0) / (t1 - t0)
            u = u * u * (3 - 2 * u)
            return a[0] + (b[0] - a[0]) * u, a[1] + (b[1] - a[1]) * u
    return keys[-1][1]


def load_game(name):
    return artkit.load_frames(name)


def spine_scene_review(s, mouth, pose, states, vents):
    """The back as the game lays it behind an edge mouth (risen shut, or still rising), on the arena floor."""
    img = Field().frame(0.0)
    spine_draw(img, s, mouth, states, vents)
    rise = 2 if states[0] == 2 else 1
    paste_c(img, s["dust-colossus-shut"][pose * 6 + rise], *mouth)
    return img.resize((288, 324), Image.LANCZOS)


def review():
    """Round 46 (round 45 A.a): the spine re-rendered as one continuous ridged back."""
    artkit.REVIEW_ROUND = REVIEW_ROUND
    s = {name: load(name) for name in SET_NAMES}
    for name in ("colossus-ridge", "colossus-ridge-head", "colossus-ridge-tail", "colossus-ridge-glow"):
        s[name] = l20.load(name)
    gif = review_gif(s, until=15.5)
    sp = s["dust-colossus-spine"]
    laid = [spine_scene_review(s, (240, 515), 3, [2, 2, 2, 2], [1, 1, 1]),
            spine_scene_review(s, (130, 30), 0, [2, 2, 2, 2], [2, 3, 1]),
            spine_scene_review(s, (25, 330), 1, [2, 2, 2, 2], None),
            spine_scene_review(s, (240, 515), 3, [2, 1, 0, None], [0, None, None]),
            spine_scene_review(s, (455, 120), 2, [1, 0, 0, None], None)]
    rows = [
        ("THE BACK AS THE GAME LAYS IT (SEGMENTS 0-3 AT 120/200/280/350 PX): REAR, FRONT AT A SLANT (VENTING, ONE "
         "DESTROYED), LEFT; RISING (REAR, RIGHT)", laid, 1, False),
        ("SPINE SEGMENT 0 UP (STATE 2), HEADINGS 0-7 (0 = THE SPINE RUNS DOWN THE SCREEN, CLOCKWISE)",
         [sp[spine_index(0, 2, k)] for k in range(8)], 1, False),
        ("SPINE SEGMENT 0 UP, HEADINGS 8-15", [sp[spine_index(0, 2, k)] for k in range(8, 16)], 1, False),
        ("SPINE AT HEADING 0: SEGMENTS 0-3 (EACH ONE STRETCH OF THE ONE BACK), EACH RISE 1/3, RISE 2/3, UP",
         [sp[spine_index(g, st, 0)] for g in range(4) for st in range(3)], 1, False),
        ("SPINE AT HEADING 3 (A SLANT): SEGMENTS 0-3 UP", [sp[spine_index(g, 2, 3)] for g in range(4)], 1, False),
        ("VENT ORGAN ON A SPINE SEGMENT: SHUT (RISING/SINKING), UP, VENTING, DESTROYED", s["dust-colossus-vent"], 2,
         False),
        ("FRAMES FROM THE GIF: THE RISE, VENTING, VENT 2 DESTROYED, THE DIVE, THE SLANTED FRONT BREACH",
         [gif[int(t * FPS)].resize((216, 243), Image.LANCZOS) for t in (3.9, 5.2, 6.5, 8.6, 12.5)], 1, False),
    ]
    sheet = artkit.review_sheet("DUST COLOSSUS - THE SPINE AS ONE CONTINUOUS RIDGED BACK (ROUND 46)", rows,
                                width=1500, batch=BATCH)
    artkit.save_review(sheet, gif, CONCEPT, "dust-colossus", fps=FPS)


PIV = pivots()


if __name__ == "__main__":
    if "--review" not in sys.argv[1:]:
        build([a for a in sys.argv[1:] if not a.startswith("--")] or None)
    review()
