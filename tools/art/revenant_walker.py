#!/usr/bin/env python3
"""Production art: the Revenant Walker, Act 3's mid-boss in Level 19 (design/enemies/bosses/
revenant-walker; M7 part F batch, step ART-F1, review round 42). Straight to production from the
chosen concept round 36 a "Strider heavy" (tools/concept/bosses_r36.py: the reverse-knee legs, the
torso's layout, the materials, the missile, imported unchanged) on the frozen render package.

What changed from the concept (to the README's spec, pending the user's review):
* drawn smaller, 72 px per model unit (the concept 92): the hull with its pods ~140 px across, the
  walking legs ~190 px, the spec's "drawn ~170x190";
* the round reactor grille became the two **reactor vents** (the weak spot x2) side by side on the
  back, shut by dark slats; open they glow hot amber, overheated (phase 3) white-orange;
* a **front plate** (the `armoured` part) wraps the prow outside the hull outline, and a short chin
  cannon pokes out under it;
* the shoulder pods are separate sprites that turn to their aim (16 headings) over a mount block on
  the hull; a destroyed pod leaves a torn stub;
* the flamer is the concept's turret, 32 aim headings (the game turns it: rest against the heading,
  the phase-2 swivel, the phase-3 sweep).
* the topple and wreck frames (280 px) are rendered at 2x instead of the quality bar's 4x (render
  time: 56 frames at 4x took longer than the step had; pending the user's review).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down
(8 body headings: 2 = facing left, 4 = up, 6 = right); every heading is its own render under the fixed
key light. Every body set shares one centre, the walker's position; assets/pivots/revenant-walker.json
gives the attachment points per body heading in px from it (x right, y down).

Outputs (assets/sprites/, or build/art-held/sprites/ (pivots build/art-held/pivots/) while no data
claims the root `revenant-walker`, vanguard.pipeline.SpriteUse: rerun once the boss's data.yaml exists):
  revenant-walker-legs_0..63    248x248 the legs, heading * 8 + walk phase (8-frame cycle), under the hull
  revenant-walker_0..7          152x152 the hull per heading: torso, front plate, chin cannon, pod mounts,
                                vents shut, flamer ring, the riveted serial plate
  revenant-walker-plate_0..7    152x152 the front plate alone, aligned with the hull (the glance flash)
  revenant-walker-vents_0..23   56x56 the vents, heading * 3 + (0 shut, 1 open amber, 2 overheated), centred
                                at `vents[k]`
  revenant-walker-pod_0..31     80x80 a shoulder pod, aim * 2 + hatch (0 shut, 1 open: cells lit red, the
                                0.4 s tell), 16 aim headings; centred at `pods[k]` = [left, right]
  revenant-walker-pod-stub_0..7 48x48 a destroyed pod's torn stub, per body heading, at `pods[k]`
  revenant-walker-flamer_0..31  84x84 the tail flamer turret, 32 aim headings (the nozzle's direction),
                                centred at `flamer[k]`; its nozzle at `nozzle` (px from the turret's
                                centre at aim 0, turned with the aim)
  revenant-walker-pilot_0..3    16x16 additive: the pilot light, idle, 2 steps, the blue-white flare (tell)
  revenant-walker-topple_0..47  280x280 the death, heading * 6 + step (2 s: the legs buckle, the hull
                                topples forward along the heading and rolls onto its flank)
  revenant-walker-wreck_0..7    280x280 the wreck that stays (scenery), = the topple's end, burnt, the
                                reactor blown, the serial plate ground to bare metal (hint 2)
  revenant-walker-missile_0..15 24x24 the homing missile (the concept's), 16 headings
  revenant-walker-smoke_0..5    16x16 the missile trail's grey smoke puff by age (stepped translucency)
  revenant-walker-flame_0..7    32x32 additive: a flame-cone puff by age (white-yellow core -> orange ->
                                red -> dark smoke tint)
  design/enemies/bosses/concept/revenant-walker-final-r42-a.png/.gif   the review

How the game draws it, back to front: the legs (walk phase from the distance walked), the hull, the
vents state over it, the pods (or stubs) at `pods[k]`, the flamer at `flamer[k]` with the pilot at its
nozzle; the topple frames replace legs + hull + parts on death, the wreck stays on the ground layer.

Run: python3 tools/art/revenant_walker.py [--review]   (~10 min on 20 cores; renders cached in the
system temp dir, conceptgame-cache/revenant-walker/; --review rebuilds the review from the written files)
"""
import contextlib
import hashlib
import json
import math
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import act3kit  # noqa: E402  (tools/art: quantize_bright keeps the small lit cells)
import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import bosses_r36 as r36  # noqa: E402  (concept round 36, imported unchanged: models, materials)
from bosses_r36 import (W_CELL, W_DARK, W_GLASS, W_HULL, W_LIGHT, W_PILOT, W_PLATE,  # noqa: E402
                        W_SCORCH, W_SENSOR, W_VENT)
from render import raster, sdf  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_cylinder_y, sd_cylinder_z,  # noqa: E402
                        sd_ellipsoid, sd_plate, sd_sphere, union)

SCRIPT = "revenant_walker.py"
BATCH = "M7 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r42"
ROOT_NAME = "revenant-walker"
CONCEPT = DESIGN / "enemies" / "bosses" / "concept"
DATA = DESIGN / "enemies" / "bosses" / "revenant-walker" / "data.yaml"
HELD = ROOT / "build" / "art-held"
CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "revenant-walker"

S = 72.0                                   # px per model unit
HEADINGS = 8
WALK = 8
POD_AIMS = 16
FLAMER_AIMS = 32
TOPPLE = 6
LEGS, HULL, VENTS, POD, STUB, FLAMER, BIG = 248, 152, 56, 80, 48, 84, 280

PODS = r36.A_PODS                          # (0.74, 0.06): pod centre, mirrored (+x = the walker's right)
FLAMER_AT = r36.A_FLAMER                   # (0, -0.78)
PLATE_AT = r36.A_PLATE                     # the serial plate on the front deck
VENTS_AT = (0.0, -0.40)
VENT_X, VENT_HALF = 0.125, (0.095, 0.15)
CHIN = (0.0, 1.02)
NOZZLE = (0.0, 0.54)                       # in the flamer's own frame
FRONT_PLATE = [(0.0, 0.76), (0.0, 0.94), (0.5, 0.8), (0.67, 0.2), (0.57, 0.12), (0.44, 0.64)]


def heading(k, n=HEADINGS):
    """Screen angle (atan2, y down) of game heading k: 0 = down, clockwise."""
    return np.pi / 2 + TAU * k / n


def screen(local, k, n=HEADINGS):
    x, y = rig.local_to_screen(local[0], local[1], heading(k, n), S)
    return [round(float(x), 1), round(float(y), 1)]


# =========================================================================== models

def mats(vent=0.15, cells=0.0, pilot=0.3, wreck=False, overheat=False):
    m = r36.walker_mats(vent=vent, cells=cells, pilot=pilot, wreck=wreck, plate_c=PLATE_AT)
    if overheat:
        m[W_VENT] = Material((0.1, 0.08, 0.06), emission=(1.6, 1.15, 0.7),
                             emission_pattern=lambda p, n: 0.55 + 0.45 * (np.sin(p[:, 0] * 60) > 0))
    return m


def legs_scene(phase=0.0, fold=0.0):
    """walker_a_legs with the death's buckle: the walk pose at ``phase`` blended toward the
    concept's folded wreck pose by ``fold``."""
    def lerp(a, b):
        return tuple(x + (y - x) * fold for x, y in zip(a, b))

    def scene(p):
        items = [(sd_box(p, (0, 0, 0.8 - 0.38 * fold), (0.4, 0.3, 0.16), 0.06), W_DARK)]
        for side in (1, -1):
            ph = phase + (0.0 if side > 0 else np.pi)
            fy = 0.6 * np.sin(ph)
            lift = 0.2 * max(0.0, np.cos(ph))
            w_hip, w_knee = (side * 0.46, -0.05, 0.8), (side * 1.14, -0.6 + 0.3 * fy, 0.62 + lift)
            w_ankle, w_toe = (side * 1.02, fy - 0.12, 0.26 + lift), 0.06 + lift
            fy2 = 0.2 * side
            hip = lerp(w_hip, (side * 0.42, 0.0, 0.42))
            knee = lerp(w_knee, (side * 1.0, -0.35 + fy2, 0.2))
            ankle = lerp(w_ankle, (side * 1.15, 0.35 + fy2, 0.12))
            toe_z = w_toe + (0.06 - w_toe) * fold
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


def plate_items(p, q):
    items = [(sd_plate(q, FRONT_PLATE, 1.24, 0.11, round_=0.035), W_HULL),
             (sd_plate(q, [(0.0, 0.84), (0.42, 0.73), (0.55, 0.3), (0.5, 0.26), (0.38, 0.68), (0.0, 0.79)],
                       1.355, 0.012, round_=0.006), W_LIGHT)]
    for x, y in ((0.0, 0.88), (0.3, 0.8), (0.55, 0.52), (0.62, 0.26)):
        items.append((sd_sphere(q, (x, y, 1.36), 0.022), W_DARK))
    return items


def vent_items(p, q, state):
    """The two vent grilles in a dark housing; shut = slats over them."""
    items = [(sd_box(p, (VENTS_AT[0], VENTS_AT[1], 1.34), (0.27, 0.2, 0.06), 0.03), W_DARK),
             (sd_box(q, (VENT_X, VENTS_AT[1], 1.37), (*VENT_HALF, 0.04), 0.01), W_VENT)]
    if state == 0:
        for k in range(5):
            y = VENTS_AT[1] - 0.12 + 0.06 * k
            items.append((sd_box(q, (VENT_X, y, 1.415), (VENT_HALF[0] + 0.01, 0.024, 0.012), 0.008), W_LIGHT))
    return items


def torso_scene(vents=0, pods=True, wreck=False, plate_only=False, vents_only=False):
    """walker_a_torso re-laid for the spec: two vents, the front plate, a chin cannon, the pods
    left to their own sprites (``pods`` = stubs instead of mounts when False)."""
    body = [(0.0, 0.76), (0.44, 0.64), (0.58, 0.12), (0.52, -0.46), (0.3, -0.7), (0.0, -0.72)]

    def scene(p):
        q = mirror_x(p)
        if plate_only:
            return union(*plate_items(p, q), k=0.01)
        if vents_only:
            return union(*vent_items(p, q, vents), k=0.01)
        items = [
            (sd_plate(q, body, 1.15, 0.22, round_=0.05), W_HULL),
            (sd_plate(q, [(0.0, 0.5), (0.3, 0.42), (0.36, 0.05), (0.0, 0.0)], 1.38, 0.03, round_=0.01), W_LIGHT),
            (sd_box(p, (0, 0.67, 1.3), (0.26, 0.05, 0.05), 0.02), W_GLASS),
            (sd_sphere(q, (0.12, 0.7, 1.33), 0.035), W_SENSOR),
            (sd_box(p, (0, 0.12, 1.42), (0.06, 0.26, 0.04), 0.02), W_DARK),           # spine
            (sd_capsule(q, (0.36, -0.2, 1.36), (0.36, -0.62, 1.36), 0.1), W_LIGHT),  # fuel tanks
            (sd_box(q, (0.36, -0.41, 1.46), (0.11, 0.02, 0.02), 0.0), W_DARK),
            (sd_cylinder_z(p, (*FLAMER_AT, 1.3), 0.18, 0.06), W_DARK),
            (sd_capsule(q, (0.05, 0.7, 1.1), (0.05, CHIN[1], 1.1), 0.035), W_DARK),  # chin cannon
            (sd_box(p, (0, 0.72, 1.1), (0.12, 0.08, 0.06), 0.02), W_HULL),
            (sd_box(p, (PLATE_AT[0], PLATE_AT[1], 1.42), (0.13, 0.065, 0.014), 0.004), W_PLATE),
        ]
        items += plate_items(p, q) + vent_items(p, q, 0 if wreck else vents)
        for dx in (-0.115, 0.115):
            for dy in (-0.05, 0.05):
                items.append((sd_sphere(p, (PLATE_AT[0] + dx, PLATE_AT[1] + dy, 1.432), 0.013), W_LIGHT))
        for side in (1, -1):
            px, py = side * PODS[0], PODS[1]
            items.append((sd_box(p, (side * 0.56, py, 1.2), (0.1, 0.12, 0.08), 0.03), W_DARK))
            if pods:
                items.append((sd_cylinder_z(p, (px, py, 1.12), 0.17, 0.06), W_DARK))   # pod turntable
            else:
                items += stub_items(p - sdf.vec(px, py, 0))
        d, m = union(*items, k=0.015)
        if wreck:                                    # blown reactor: a crater through the vents
            d = np.maximum(d, -sd_sphere(p, (VENTS_AT[0], VENTS_AT[1], 1.55), 0.3))
        return d, m
    return scene


def stub_items(p):
    return [(sd_box(p, (0, -0.04, 1.18), (0.14, 0.2, 0.1), 0.05), W_SCORCH),
            (sd_capsule(p, (-0.04, 0.08, 1.22), (0.12, 0.22, 1.3), 0.03), W_DARK),
            (sd_capsule(p, (0.05, -0.12, 1.2), (-0.1, -0.24, 1.3), 0.025), W_DARK)]


def pod_scene(hatch=0):
    """The concept's shoulder box around its own centre; the launcher lip faces +y (the aim)."""
    def scene(p):
        items = [(sd_box(p, (0, 0, 1.26), (0.24, 0.38, 0.18), 0.04), W_HULL),
                 (sd_box(p, (0, 0.42, 1.24), (0.2, 0.05, 0.12), 0.02), W_DARK),
                 (sd_box(p, (0, -0.3, 1.45), (0.17, 0.04, 0.02), 0.01), W_LIGHT)]
        for cx in (-0.1, 0.1):
            for cy in (-0.16, 0.04, 0.24):
                if hatch:
                    items += [(sd_box(p, (cx, cy, 1.41), (0.065, 0.075, 0.03), 0.01), W_SENSOR),
                              (sd_box(p, (cx + np.sign(cx) * 0.09, cy, 1.5), (0.012, 0.075, 0.05), 0.005), W_LIGHT)]
                else:
                    items.append((sd_box(p, (cx, cy, 1.43), (0.07, 0.08, 0.02), 0.01), W_CELL))
        return union(*items, k=0.015)
    return scene


def flamer_scene():
    """The concept's flamer turret (nozzle along +y) with its pilot light."""
    return r36.walker_a_flamer()


def topple_scene(u, wreck=False):
    """Legs buckling (fold) and the hull pitching forward along the heading, rolling onto its left
    flank (serial-plate side up) and dropping onto them; u = 0 .. 1."""
    legs = legs_scene(0.0, min(1.0, u * 1.4))
    torso = torso_scene(pods=False, wreck=wreck)
    pitch, roll, fwd, drop = 0.42 * u, -0.3 * u, 0.32 * u, 0.5 * u

    def scene(p):
        q = p - sdf.vec(0, fwd, -drop)
        q = q - sdf.vec(0, 0.3, 0.8)                 # pivot at the hips' front
        q = sdf.rotate_x(q, -pitch)
        q = sdf.rotate_y(q, roll)
        q = q + sdf.vec(0, 0.3, 0.8)
        return union(legs(p), torso(q))
    return scene


# =========================================================================== render jobs

def _job_key(job):
    return hashlib.sha1(repr(job).encode()).hexdigest()[:20]


def model_for(job):
    kind = job[0]
    if kind == "legs":
        _, k, ph = job
        return legs_scene(TAU * ph / WALK), mats(), heading(k), LEGS
    if kind == "hull":
        _, k = job
        return torso_scene(0, True), mats(), heading(k), HULL
    if kind == "plate":
        _, k = job
        return torso_scene(plate_only=True), mats(), heading(k), HULL
    if kind == "vents":
        _, k, st = job
        return (torso_scene(st, vents_only=True), mats(vent=(0.15, 1.0, 1.0)[st], overheat=st == 2),
                heading(k), VENTS)
    if kind == "pod":
        _, a, hatch = job
        return pod_scene(hatch), mats(cells=1.0 if hatch else 0.05), heading(a, POD_AIMS), POD
    if kind == "stub":
        _, k = job
        return (lambda p: union(*stub_items(p))), mats(), heading(k), STUB
    if kind == "flamer":
        _, a = job
        return flamer_scene(), mats(pilot=0.3), heading(a, FLAMER_AIMS), FLAMER
    if kind == "topple":
        _, k, st = job
        u = st / (TOPPLE - 1)
        return topple_scene(u), mats(vent=0.15 + 0.85 * u), heading(k), BIG
    if kind == "wreck":
        _, k = job
        return topple_scene(1.0, wreck=True), mats(vent=0.0, wreck=True), heading(k), BIG
    if kind == "missile":
        _, k = job
        scene, m = r36.missile_model()
        return scene, m, heading(k, 16), 24
    raise ValueError(kind)


def offset_for(job):
    """Where the sprite's centre sits in model units relative to the scene origin (the hull's
    vents crop and the pods/stub/flamer are rendered around their own centres)."""
    if job[0] == "vents":
        return VENTS_AT
    return (0.0, 0.0)


def render_job(job):
    path = CACHE / f"{_job_key(job)}.png"
    if path.exists():
        return Image.open(path).convert("RGBA")
    scene0, m, h, size = model_for(job)
    ox, oy = offset_for(job)
    rot = model_rotation(h)
    scene = (lambda p, s=scene0, r=rot: s(sdf.rotate_z(p, r) + sdf.vec(ox, oy, 0)))
    scale = 25.3 if job[0] == "missile" else S
    if job[0] in ("topple", "wreck"):        # 2x: the 280 px death frames (time; pending review)
        factor = 2
        hi = sdf.render(scene, model_space_materials(m, rot), (size * factor, size * factor), size / scale)
    else:
        hi, factor = artkit.render_hi(scene, model_space_materials(m, rot), (size, size), size / scale)
    img = artkit.native(hi, factor)
    CACHE.mkdir(parents=True, exist_ok=True)
    img.save(path)
    return img


# =========================================================================== 2D effects

def smoke_frames():
    out = []
    rng = np.random.default_rng(7)
    blobs = [(rng.uniform(-2, 2), rng.uniform(-2, 2), rng.uniform(2.5, 4.5)) for _ in range(5)]
    for i in range(6):
        u = i / 5
        n = 64
        yy, xx = np.mgrid[0:n, 0:n] / 4 - 8
        a = np.zeros((n, n))
        for bx, by, br in blobs:
            r = br * (0.7 + 0.8 * u)
            a = np.maximum(a, np.clip(1 - np.hypot(xx - bx * (0.5 + u), yy - by * (0.5 + u)) / r, 0, 1))
        a = a ** 0.6 * (1 - 0.75 * u)
        a = a.reshape(16, 4, 16, 4).mean(axis=(1, 3))
        a = np.floor(a * 4 + 0.35) / 4
        g = 150 - 50 * u
        shade = g + 30 * (np.mgrid[0:16, 0:16][0] < 8)
        rgb = np.dstack([shade * 1.0, shade * 0.98, shade * 0.95])
        out.append(Image.fromarray(np.dstack([rgb, a * 255 * 0.85]).astype(np.uint8), "RGBA"))
    return artkit.quantize_set(out, 12)


FLAME_RAMP = [(0.0, (255, 252, 220)), (0.25, (255, 214, 90)), (0.5, (255, 130, 30)),
              (0.75, (200, 50, 20)), (1.0, (70, 30, 24))]


def flame_frames():
    out = []
    n = 32 * 4
    yy, xx = (np.mgrid[0:n, 0:n] + 0.5) / 4 - 16
    noise = raster.fbm(n, n, 12, 3, octaves=3, period=True)
    for i in range(8):
        u = i / 7
        r = 5 + 9 * u
        d = np.hypot(xx, yy) / r + (noise - 0.5) * 0.6
        a = np.clip(1 - d, 0, 1) ** (0.8 + 0.6 * u)
        col = np.array(raster.ramp(FLAME_RAMP, np.clip(u * 0.9 + (1 - a) * 0.35, 0, 1)), float)
        rgb = col * (a[..., None] * (1.15 - 0.7 * u))
        img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").resize((32, 32), Image.BOX)
        out.append(artkit.additive(img.convert("RGBA")))
    return artkit.quantize_set(out, 32)


def pilot_frames():
    out = []
    for i, (col, k, r) in enumerate((((255, 150, 60), 0.6, 2.5), ((255, 200, 140), 0.85, 3.5),
                                     ((220, 230, 255), 1.0, 5.0), ((190, 215, 255), 1.0, 7.0))):
        n = 64
        yy, xx = (np.mgrid[0:n, 0:n] + 0.5) / 4 - 8
        a = np.clip(1 - np.hypot(xx, yy) / r, 0, 1) ** 1.5 * k
        core = np.clip(1 - np.hypot(xx, yy) / (r * 0.35), 0, 1)
        rgb = np.array(col, float) * a[..., None] + 255 * core[..., None] * 0.6
        img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").resize((16, 16), Image.BOX)
        out.append(artkit.additive(img.convert("RGBA")))
    return out


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


def pivots():
    return {
        "note": "per body heading k (0 = down, clockwise, 45 degree steps): px from the walker's centre "
                "(x right, y down); every body set (legs, hull, plate, topple, wreck) is centred on it",
        "scale": S,
        "pods": [[screen((-PODS[0], PODS[1]), k), screen((PODS[0], PODS[1]), k)] for k in range(HEADINGS)],
        "pod_order": "left, right (the walker's own)",
        "vents": [screen(VENTS_AT, k) for k in range(HEADINGS)],
        "vent_spots": [[screen((-VENT_X, VENTS_AT[1]), k), screen((VENT_X, VENTS_AT[1]), k)]
                       for k in range(HEADINGS)],
        "flamer": [screen(FLAMER_AT, k) for k in range(HEADINGS)],
        "nozzle": [round(NOZZLE[1] * S, 1), "px from the flamer's centre along its aim"],
        "chin": [screen(CHIN, k) for k in range(HEADINGS)],
        "plate_centre": [screen((0.0, 0.8), k) for k in range(HEADINGS)],
        "pod_launcher": [round(0.47 * S, 1), "px from a pod's centre along its aim (the missiles' start)"],
    }


def jobs():
    js = [("legs", k, ph) for k in range(HEADINGS) for ph in range(WALK)]
    js += [("hull", k) for k in range(HEADINGS)] + [("plate", k) for k in range(HEADINGS)]
    js += [("vents", k, st) for k in range(HEADINGS) for st in range(3)]
    js += [("pod", a, h) for a in range(POD_AIMS) for h in range(2)]
    js += [("stub", k) for k in range(HEADINGS)] + [("flamer", a) for a in range(FLAMER_AIMS)]
    js += [("missile", k) for k in range(16)]
    js += [("topple", k, st) for k in range(HEADINGS) for st in range(TOPPLE)]
    js += [("wreck", k) for k in range(HEADINGS)]
    return js


def build():
    js = jobs()
    big = [j for j in js if j[0] in ("legs", "topple", "wreck")]
    rest = [j for j in js if j not in big]
    with ProcessPoolExecutor() as pool:
        out = dict(zip(rest + big, pool.map(render_job, rest + big)))

    def of(kind):
        return [out[j] for j in js if j[0] == kind]
    hull = artkit.quantize_set(of("hull"), 48)
    plate = artkit.quantize_set(of("plate"), 24)
    sets = {
        "revenant-walker-legs": artkit.quantize_set(of("legs"), 40),
        "revenant-walker": hull,
        "revenant-walker-plate": plate,
        "revenant-walker-vents": act3kit.quantize_bright(of("vents"), 32),
        "revenant-walker-pod": act3kit.quantize_bright(of("pod"), 32),
        "revenant-walker-pod-stub": artkit.quantize_set(of("stub"), 20),
        "revenant-walker-flamer": artkit.quantize_set(of("flamer"), 24),
        "revenant-walker-pilot": pilot_frames(),
        "revenant-walker-topple": artkit.quantize_set(of("topple"), 48),
        "revenant-walker-wreck": artkit.quantize_set(of("wreck"), 40),
        "revenant-walker-missile": artkit.quantize_set(of("missile"), 20),
        "revenant-walker-smoke": smoke_frames(),
        "revenant-walker-flame": flame_frames(),
    }
    for name, frames in sets.items():
        write(name, frames)
    with target():
        artkit.write_pivots(ROOT_NAME, SOURCE, pivots())


# =========================================================================== review

FW, FH = 480, 540


def paste_c(img, sp, x, y):
    sprite.paste(img, sp, x - sp.width / 2, y - sp.height / 2)


def glow_c(img, g, x, y):
    return artkit.add_light(img, g, (int(round(x - g.width / 2)), int(round(y - g.height / 2))))


def walker_draw(img, s, x, y, k, phase, vents=0, aims=(0, 0), hatch=(0, 0), pods=(True, True), faim=None,
                pilot=0):
    pv = PIV
    paste_c(img, s["revenant-walker-legs"][k * WALK + phase % WALK], x, y)
    paste_c(img, s["revenant-walker"][k], x, y)
    if vents:
        vx, vy = pv["vents"][k]
        paste_c(img, s["revenant-walker-vents"][k * 3 + vents], x + vx, y + vy)
    for side in range(2):
        px, py = pv["pods"][k][side]
        if pods[side]:
            paste_c(img, s["revenant-walker-pod"][aims[side] * 2 + hatch[side]], x + px, y + py)
        else:
            paste_c(img, s["revenant-walker-pod-stub"][k], x + px, y + py)
    fa = (k * 4 + 16) % 32 if faim is None else faim
    fx, fy = pv["flamer"][k]
    paste_c(img, s["revenant-walker-flamer"][fa], x + fx, y + fy)
    ang = heading(fa, FLAMER_AIMS)
    nx, ny = x + fx + NOZZLE[1] * S * math.cos(ang), y + fy + NOZZLE[1] * S * math.sin(ang)
    return glow_c(img, s["revenant-walker-pilot"][pilot], nx, ny), (nx, ny, ang)


def aim_index(x0, y0, x1, y1, n):
    a = math.atan2(y1 - y0, x1 - x0) - math.pi / 2
    return int(round(a / (TAU / n))) % n


def ground():
    g = r36.walker_ground(FH * 2)
    return g


def review_gif(s):
    bg = ground()
    frames = []
    fps = 20
    T = 14.0
    smoke, flames, missiles = [], [], []
    ship = None
    for f in range(int(T * fps)):
        t = f / fps
        img = bg.crop((0, 200, FW, 200 + FH)).copy()
        # the pass: down the centre lane from the top, turn at the bottom (1.5 s), walk back up
        if t < 6.0:
            x, y, k = 240, -60 + 45 * t, 0
        elif t < 7.5:
            x, y, k = 240, 210, int((t - 6.0) / 1.5 * 4 + 0.5) % 8
        else:
            x, y, k = 240, 210 - 30 * (t - 7.5), 4
        dist = 45 * t
        phase = int(dist / (0.96 * S) * WALK) % WALK
        sx, sy = 240 + 60 * math.sin(t * 0.9), (470 if t < 4.0 else 470 - (t - 4.0) * 60 if t < 6.0 else 350)
        sx = sx if t < 4.0 else 160 + 30 * math.sin(t)
        aims = tuple(aim_index(x + PIV["pods"][k][i][0], y + PIV["pods"][k][i][1], sx, sy, POD_AIMS)
                     for i in range(2))
        hatch = (1 if 1.6 <= t % 3.0 < 2.0 else 0, 1 if 0.1 <= t % 3.0 < 0.5 and t > 1 else 0)
        burn = 8.6 <= t < 10.1
        tell = 8.0 <= t < 8.6
        vents = 1 if 10.1 <= t < 12.1 else 0
        dead = t >= 11.0
        if not dead:
            img, (nx, ny, ang) = walker_draw(img, s, x, y, k, phase, vents=vents, aims=aims, hatch=hatch,
                                            pods=(True, t < 5.0), pilot=3 if tell or burn else (1 if t % 1 < 0.5 else 0))
        else:
            if t < 13.0:                         # the 2 s topple, then the wreck stays
                st = min(TOPPLE - 1, int((t - 11.0) / 0.4))
                paste_c(img, s["revenant-walker-topple"][k * TOPPLE + st], x, y)
            else:
                paste_c(img, s["revenant-walker-wreck"][k], x, y)
        # missiles: one per hatch end
        for side, at in ((0, 2.0), (1, 0.5)):
            if 1 < t and abs((t % 3.0) - at) < 1e-6 + 0.5 / fps and t < 10 and (side == 0 or t < 5.0):
                px, py = PIV["pods"][k][side]
                missiles.append([x + px, y + py, heading(aims[side], POD_AIMS), t])
        for m in missiles:
            if t - m[3] > 4.0:
                continue
            want = math.atan2(sy - m[1], sx - m[0])
            da = (want - m[2] + math.pi) % TAU - math.pi
            m[2] += max(-math.radians(90) / fps, min(math.radians(90) / fps, da))
            m[0] += 110 / fps * math.cos(m[2])
            m[1] += 110 / fps * math.sin(m[2])
            smoke.append([m[0] - 8 * math.cos(m[2]), m[1] - 8 * math.sin(m[2]), t])
        smoke[:] = [p for p in smoke if t - p[2] < 0.6]
        for p in smoke:
            paste_c(img, s["revenant-walker-smoke"][min(5, int((t - p[2]) / 0.1))], p[0], p[1])
        for m in missiles:
            if t - m[3] <= 4.0:
                kk = int(round((m[2] - math.pi / 2) / (TAU / 16))) % 16
                paste_c(img, s["revenant-walker-missile"][kk], m[0], m[1])
        if burn and not dead:
            for j in range(10):
                flames.append([nx, ny, ang + math.radians(np.random.default_rng(f * 10 + j).uniform(-22, 22)), t])
        flames[:] = [p for p in flames if t - p[3] < 0.4]
        for p in flames:
            age = t - p[3]
            d = 18 + age * 330
            img = glow_c(img, s["revenant-walker-flame"][min(7, int(age / 0.05))],
                         p[0] + d * math.cos(p[2]), p[1] + d * math.sin(p[2]))
        artkit_ship(img, sx, sy)
        raster.draw_text(img, 8, FH - 14, f"REVENANT WALKER FINAL R42 - {t:4.1f} S", raster.LABEL)
        frames.append(img)
    return frames


def artkit_ship(img, x, y):
    shipf = artkit.load_frames("ship")
    sp = shipf[len(shipf) // 2]
    paste_c(img, sp, x, y)


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    s = {name: load(name) for name in SET_NAMES}
    a = [walker_draw(Image.new("RGBA", (260, 260), (0, 0, 0, 0)), s, 130, 130, k, 0, aims=(k * 2, k * 2))[0]
         for k in range(HEADINGS)]
    gif = review_gif(s)
    legs, top, wreck = s["revenant-walker-legs"], s["revenant-walker-topple"], s["revenant-walker-wreck"]
    rows = [
        ("ASSEMBLED, HEADINGS 0-3 (LEGS + HULL + PODS AIMED ALONG + FLAMER AT REST)", a[:4], 1, False),
        ("ASSEMBLED, HEADINGS 4-7", a[4:], 1, False),
        ("WALK CYCLE, HEADING 0, PHASES 0-3", legs[:4], 1, False),
        ("WALK CYCLE, HEADING 6 (FACING RIGHT), PHASES 0-7 EVERY 2ND", legs[6 * WALK:7 * WALK:2], 1, False),
        ("HULL PER HEADING (FRONT PLATE, VENTS SHUT)", s["revenant-walker"], 1, False),
        ("FRONT PLATE OVERLAY, VENTS SHUT / OPEN / OVERHEATED (HEADINGS 0, 4)",
         s["revenant-walker-plate"][:2] + [s["revenant-walker-vents"][i] for i in (0, 1, 2, 12, 13, 14)], 1, False),
        ("POD: AIMS 0-7, HATCH SHUT / OPEN (CELLS LIT, THE TELL)", s["revenant-walker-pod"][:16], 1, False),
        ("POD STUBS PER HEADING, FLAMER AIMS (EVERY 4TH)", s["revenant-walker-pod-stub"] + s["revenant-walker-flamer"][::4], 1, False),
        ("PILOT LIGHT (ADDITIVE): IDLE, 2 STEPS, FLARE", s["revenant-walker-pilot"], 4, True),
        ("TOPPLE, HEADING 0, STEPS 0 2 4 5", [top[i] for i in (0, 2, 4, 5)], 1, False),
        ("WRECK, HEADINGS 0 2 4 6 (REACTOR BLOWN, FLANK UP, THE SERIAL PLATE GROUND BLANK)", wreck[::2], 1, False),
        ("MISSILE (16 HEADINGS), SMOKE PUFFS BY AGE", s["revenant-walker-missile"] + s["revenant-walker-smoke"], 2, False),
        ("FLAME-CONE PUFFS BY AGE (ADDITIVE)", s["revenant-walker-flame"], 2, True),
        ("FRAMES FROM THE GIF: A PASS, THE TELL, THE BURN, THE TOPPLE, THE WRECK",
         [gif[i].resize((240, 270), Image.LANCZOS) for i in (60, 165, 185, 236, 270)], 1, False),
    ]
    sheet = artkit.review_sheet("REVENANT WALKER - FINAL SPRITES (L19 MID-BOSS)", rows, width=1500, batch=BATCH)
    artkit.save_review(sheet, gif, CONCEPT, "revenant-walker", fps=20)


SET_NAMES = ["revenant-walker-legs", "revenant-walker", "revenant-walker-plate", "revenant-walker-vents",
             "revenant-walker-pod", "revenant-walker-pod-stub", "revenant-walker-flamer",
             "revenant-walker-pilot", "revenant-walker-topple", "revenant-walker-wreck",
             "revenant-walker-missile", "revenant-walker-smoke", "revenant-walker-flame"]
PIV = pivots()


if __name__ == "__main__":
    if "--review" not in sys.argv[1:]:
        build()
    review()
