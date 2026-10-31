#!/usr/bin/env python3
"""Production art: Level 32's units and ground objects (M9 part E batch, concept round 65, step ART-E): the Sentinel
Tower (design/enemies/ground/sentinel-tower, round 60 A.12 **a** "lighthouse"), the Strider
(design/enemies/ground/strider, the chosen r05 walker with round 60's pod and A.14 **b** "buckle and crumple"), the
blast doors (Level 32's caches and the consortium store), the mining charge and its burst (the belt's hazard) and the
consortium repair crate (the refinery gate's armour patch).

The models are round 60's (tools/concept/enemies_r60.py ``sentinel_body``, ``sentinel_head``, ``strider_torso``,
``strider_legs``, ``strider_whole``; tools/concept/render/archetype_models.py ``strider_torso``; imported unchanged)
in **Q.11 b**: gunmetal and charcoal hulls (tools/art/belt_units.py ``gunmetal``) with their gold, red running lights
and the 1 px gold/red rim (belt_units.py ``rim``), gold about 20-30 % of the silhouette. The doors, the charge and the
crate are new (no concept: Level 32's README, *Blast doors, mining charges, topples*), the lit doors in the same
Ascendancy look, the sealed store's door and the crate in the consortium's worn grey and hazard yellow.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its
own render under the fixed key light (the left doors are their own renders of the mirrored layout).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: tools/art/l23kit.py's rule, an
enemy's root once ``design/enemies/<layer>/<slug>/data.yaml`` exists, a ground target's look once a level's data names
it; pivots likewise in assets/pivots or build/art-held/pivots):
  sentinel-tower             80x80: the fixed tower from above: the gold-banded octagonal hardened collar, the round
                             shaft, its red running lights (the emitter head is drawn over it)
  sentinel-tower-head_0..31  64x64, 32 headings: the emitter head (black-gunmetal sphere, gold equator ring, the lens
                             snout), the lens dark; drawn on the tower's centre turned to the beam (pivots ``lens[k]``)
  sentinel-tower-eye_0..3    24x24, additive: the lens's crimson glow (0 the telegraph's first glimmer .. 3 live), at
                             ``lens[k]``
  sentinel-tower-beam_0..3   32x10, additive: the live beam, a crimson strip with a white-hot core, tiled from the lens
                             along the beam (4 flicker steps); sentinel-tower-beam-tip_0..3 16x16 its end
  sentinel-tower-telegraph-100, -120, -160  additive: the arc telegraph's wedge (easy, medium, hard), 220 px radius,
                             the apex at the image's left middle, opening to the right (turned to the arc's centre)
  sentinel-tower-ring        448x448, additive: the full turn's telegraph, the 220 px reach ring (the start ray is
                             the beam strip drawn dim)
  sentinel-tower-stump       80x80: the scorched collar and the shaft's broken stump (the wreck that stays)
  sentinel-tower-topple_0..5 80x80: the death's head toppling off the shaft to the lower right, over the medium rung
  strider-legs_0..127        120x120, 16 headings x 8 walk frames (heading * 8 + frame): the legs and pelvis, the
                             knees gold (the two topple parts)
  strider_0..31              96x96, 32 headings: the torso with its twin cannons (the hull, vital), drawn on the
                             walker's centre over the legs
  strider-pod_0..63          24x24, 32 headings x 2 (aim * 2 + hatch: 0 shut, 1 open, the hatch glowing gold: the
                             0.4 s tell): the shoulder pod, at ``pod[k]`` (not drawn once destroyed)
  strider-knee_0..2          16x16, additive: a hit knee's red flash (swell, full, fade), at ``knees[heading*8+frame]``
  strider-fall_0..95         per fall direction (8: the walker's heading rounded to 45 degrees) x lost side (left,
                             right) x 6 steps ((dir * 2 + side) * 6 + step; steps 0-1 the stagger's buckle, 2-5 the
                             fall: the knee folds, it pitches over onto the line and the torso breaks off the legs),
                             at ``fall.anchor[dir * 2 + side]`` (the walker's centre in the frame)
  strider-wreck_0..15        the toppled wreck that stays (the fall's last step, burnt), same anchors
  strider-hulk_0..15         120x120, 16 headings: the burnt standing hulk after a hull kill (legs + torso, no pod)
  blast-door-right_0..3, blast-door-left_0..3   128x96: a lit Ascendancy blast door in a wall face (the door 42 px
                             beyond the lock: the lock's console at the image's centre, the ground target's place),
                             its frame's red running lights, the lock's amber lamp pulsing (4 steps)
  blast-door-right-open_0..5, -left-open_0..5   the lock blown, the leaves sliding apart, the cache bay's warm light
                             (the last frame stays)
  blast-door-sealed-right, -sealed-right-open_0..5   the consortium store's sealed door (worn grey, faded hazard
                             stripes and stamps, its lock dark), and its opening
  mining-charge_0..3         24x24: a miners' charge strapped to the rock, its amber lamp blinking (4 steps)
  mining-charge-blast_0..9   144x144, additive: the 64 px burst (white-hot flash, the shock ring at 64 px, sparks)
  repair-crate, repair-crate-open_0..3   40x40: the consortium repair crate on the refinery gate, latched with its
                             lock lamp; shot open (the lid off, the armour patch's glow)
  Review (round 65): design/enemies/ground/concept/sentinel-tower-final-r65-a.png/.gif, strider-final-r65-a.png/.gif,
  design/campaign/act-5-the-belt/level-32-deep-core/concept/ground-objects-final-r65-a.png/.gif

Run: python3 tools/art/belt_units_e.py [sentinel] [strider] [doors] [charge] [crate] [--review] [--check]
     (no subject = all, ~15 min on 20 cores, renders cached in the system temp dir, conceptgame-cache/belt-units-e/,
     by the model code's hash; --review rebuilds only the review files from the written frames; --check lists every
     set with its folder, size and frame count and fails on a missing one or a missing Source chunk)
"""
import hashlib
import json
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import belt_units_c as buc
import l23kit
from artkit import DESIGN, ROOT, TAU

import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged: the round-60 models)
from render import archetype_models as am  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_models import A_FACET, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.sdf import (Material, rotate_x, rotate_y, sd_box, sd_capsule, sd_cylinder_z, sd_sphere,  # noqa: E402
                        subtract, union)

SCRIPT = "belt_units_e.py"
BATCH = "M9 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r65"
artkit.REVIEW_ROUND = ROUND
FPS = 20
A_X = e60.A_X
L32 = DESIGN / "campaign" / "act-5-the-belt" / "level-32-deep-core"
CONCEPT = {"sentinel": DESIGN / "enemies" / "ground" / "concept", "strider": DESIGN / "enemies" / "ground" / "concept",
           "objects": L32 / "concept"}
BACKDROP = ROOT / "assets" / "backdrop" / "level-32"
write, load, where = bu.write, bu.load, bu.where
gunmetal, rim, turned, render = bu.gunmetal, bu.rim, bu.turned, bu.render
grid, field_img, screen_point = buc.grid, buc.field_img, buc.screen_point
CRIMSON = np.array(e60.CRIMSON, float)
AMBER = np.array((255, 170, 60), float)


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    """Pivots go with their sprite's folder: assets/pivots when the root is claimed, else build/art-held/pivots."""
    folder = artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def pivot_path(root):
    p = artkit.PIVOTS / f"{root}.json"
    return p if p.exists() else ROOT / "build" / "art-held" / "pivots" / f"{root}.json"


def pivots(root):
    return json.loads(pivot_path(root).read_text(encoding="utf-8"))


# =========================================================================== the Sentinel Tower (A.12 a)

SEN_SIZE, SEN_EXT = 80, 2.3 * 80 / 84          # round 60's sheet scale (84 px for 2.3 units)
SEN_PX = SEN_SIZE / SEN_EXT
HEAD_SIZE = 64
HEAD_EXT = HEAD_SIZE / SEN_PX
LENS = (0.0, 0.8)                              # model units: the lens's front (+Y forward)
REACH = 220
ARCS = (100, 120, 160)


def _sen_body(stump=False):
    scene0, mats = e60.sentinel_body("a")
    mats = gunmetal(mats)
    mats[A_GOLD] = replace(mats[A_GOLD], albedo=(0.78, 0.6, 0.24))

    def scene(p):
        d, m = scene0(p)
        # the concept's top ring is a full gold disc (53 % gold with the band): a ring round the dark shaft cap instead
        m = np.where((m == A_GOLD) & (np.hypot(p[:, 0], p[:, 1]) < 0.4), A_FACET, m)
        if stump:                               # the shaft broken off low: the top ring gone, a jagged lip
            cut = p[:, 2] - (0.5 + 0.06 * np.sin(np.arctan2(p[:, 1], p[:, 0]) * 7))
            d = np.maximum(d, np.where(np.hypot(p[:, 0], p[:, 1]) < 0.62, cut, -1e9))
        for a in np.linspace(0, TAU, 8, endpoint=False) + TAU / 16:   # red running lights on the collar's corners
            dl = sd_sphere(p, (0.82 * np.cos(a), 0.82 * np.sin(a), 0.16), 0.06)
            m = np.where(dl < d, A_RED, m)
            d = np.minimum(d, dl)
        return d, m
    return scene, mats


def _sentinel_base(stump=False):
    return render(*turned(*_sen_body(stump), 0, 32), SEN_SIZE, SEN_EXT)


def _sen_head():
    scene, mats = e60.sentinel_head("a", live=0.0)
    mats = gunmetal(mats)
    mats[A_X] = e60.emissive(CRIMSON / 255.0, 0.1)
    return scene, mats


def _sentinel_head(k):
    return render(*turned(*_sen_head(), k, 32), HEAD_SIZE, HEAD_EXT)


def _sentinel_topple(i):
    """The head toppling off: tipping over (about its x axis) and sliding down-right off the shaft."""
    scene0, mats = _sen_head()
    u = i / 5

    def scene(p):
        q = p - np.array((0.42 * u, -0.34 * u, -0.45 * u * u))
        q = rotate_x(q - np.array((0, 0, 0.7)), 1.5 * u) + np.array((0, 0, 0.7))
        return scene0(q)
    return render(*turned(scene, mats, 5, 32), SEN_SIZE, SEN_EXT)


def eye(k):
    x, y = grid(24)
    r = np.hypot(x, y)
    f = (np.exp(-(r / (1.6 + 2.2 * k)) ** 2) * (0.6 + 0.5 * k) + np.exp(-(r / (5 + 3 * k)) ** 2) * 0.35 * k)
    return field_img(f, CRIMSON, white=0.7)


def beam_strip(i, w=32, h=10):
    rng = np.random.default_rng(3201 + i)
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    wob = 0.35 * np.sin(xx / w * TAU * 2 + i * 1.7) + rng.normal(0, 0.12, w)[None, :]
    dy = np.abs(yy + 0.5 - h / 2 - wob)
    f = np.exp(-(dy / 1.3) ** 2) * 1.0 + np.exp(-(dy / 3.4) ** 2) * 0.45
    f *= 0.9 + 0.1 * np.sin(xx / w * TAU * 3 + i)
    return field_img(f, CRIMSON, white=0.8)


def beam_tip(i):
    x, y = grid(16)
    k = (0.8, 1.0, 0.9, 0.7)[i]
    f = np.exp(-(np.hypot(x, y) / (2.2 + 0.4 * i)) ** 2) * k
    img = field_img(f, CRIMSON, white=0.7)
    d = ImageDraw.Draw(img)
    for a in np.linspace(0, TAU, 5, endpoint=False) + i * 0.7:
        d.line([8 + np.cos(a) * 3, 8 + np.sin(a) * 3, 8 + np.cos(a) * 7, 8 + np.sin(a) * 7], fill=(255, 150, 120, 255))
    return img


def wedge(arc):
    """The arc telegraph: a filled crimson wedge with a brighter rim, the apex at the left middle (Mantis convention)."""
    half = np.radians(arc / 2)
    h = 2 * int(np.ceil(REACH * np.sin(half))) + 4
    w = REACH + 4
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    x, y = xx - 1, yy - h / 2
    r, a = np.hypot(x, y), np.abs(np.arctan2(y, x))
    inside = (r <= REACH) & (a <= half)
    edge = inside & ((r > REACH - 2.2) | (a > half - 1.6 / np.maximum(r, 1)))
    f = np.where(inside, 0.16 + 0.06 * (r / REACH), 0) + np.where(edge, 0.85, 0)
    return field_img(artkit.smooth_steps(f), CRIMSON, white=0.95)


def ring():
    w = 2 * REACH + 8
    x, y = grid(w)
    r = np.hypot(x, y)
    f = np.exp(-((r - REACH) / 1.2) ** 2) * 0.95 + np.where(r < REACH, 0.07 + 0.05 * (r / REACH) ** 3, 0)
    return field_img(artkit.smooth_steps(f), CRIMSON, white=0.95)


def build_sentinel():
    jobs = [("sbase", False), ("sbase", True)] + [("shead", k) for k in range(32)] + [("stopple", i) for i in range(6)]
    res = run(jobs)
    bodies = artkit.quantize_set([rim(f) for f in res[:1]], 36)
    note_gold("sentinel-tower", bodies)
    save_set("sentinel-tower", bodies, single=True)
    stump = buc.scorch(rim(res[1]), 3207, 0.5)
    a = np.array(stump)
    d = ImageDraw.Draw(stump)
    d.ellipse([33, 33, 47, 47], fill=(26, 24, 26, 255), outline=(70, 66, 62, 255))
    for x, y in ((38, 36), (43, 41), (36, 43)):
        d.point((x, y), fill=(230, 130, 60, 255))
    a2 = np.array(stump)
    a2[a[..., 3] == 0] = 0
    save_set("sentinel-tower-stump", artkit.quantize_set([Image.fromarray(a2, "RGBA")], 28), single=True)
    heads = artkit.quantize_set([rim(f) for f in res[2:34]], 32)
    note_gold("sentinel-tower-head", heads)
    save_set("sentinel-tower-head", heads)
    top = [buc.scorch(rim(f), 3209 + i, 1.0 - 0.09 * i) for i, f in enumerate(res[34:])]
    save_set("sentinel-tower-topple", artkit.quantize_set(top, 32))
    save_set("sentinel-tower-eye", [eye(k) for k in (0.25, 0.5, 0.75, 1.0)])
    save_set("sentinel-tower-beam", [beam_strip(i) for i in range(4)])
    save_set("sentinel-tower-beam-tip", [beam_tip(i) for i in range(4)])
    for arc in ARCS:
        save_set(f"sentinel-tower-telegraph-{arc}", [wedge(arc)], single=True)
    save_set("sentinel-tower-ring", [ring()], single=True)
    write_pivots("sentinel-tower", {
        "size": [SEN_SIZE, SEN_SIZE], "head": [HEAD_SIZE, HEAD_SIZE], "headings": 32,
        "lens": [screen_point(LENS, k, 32, HEAD_SIZE, HEAD_EXT) for k in range(32)],
        "emitter": [28, 28], "collar": [72, 72], "reach": REACH,
        "note": "lens[k]: the lens's front in head frame k ([x, y] px from the head frame's top left; the head frame "
                "is centred on the tower's centre): the beam starts there and the eye glow is centred on it. The "
                "telegraph wedges' apex is their left middle (the tower's centre), the ring's centre is its centre"})


# =========================================================================== the Strider (r05 + round 60 pod, A.14 b)

STR_SIZE, STR_EXT = 120, 2.3                   # round 60's sheet scale (52 px per unit)
STR_PX = STR_SIZE / STR_EXT
TORSO_SIZE = 96
TORSO_EXT = TORSO_SIZE / STR_PX
POD = (-0.36, -0.28)                           # model units: the pod on the left shoulder
POD_SIZE = 24
POD_EXT = POD_SIZE / STR_PX
FALL_BIG = 240
FALL_EXT = FALL_BIG / STR_PX
FALL_DIRS = 8
# per step: tilt (radians about the foot line), sink, split (round 60 b's fall_frame, with a two-step stagger)
FALL_STEPS = [(0.12, -0.05, 0.0), (0.24, -0.1, 0.0), (0.44, -0.18, 0.0)] + [
    (0.44 + 0.76 * w * w, -0.18, w) for w in ((0.55 - 0.35) / 0.65, (0.8 - 0.35) / 0.65, 1.0)]


def _str_mats(hatch=0.0):
    mats = gunmetal(e60.strider_mats(1.0, hatch))
    mats[A_GOLD] = replace(mats[A_GOLD], albedo=(0.78, 0.6, 0.24))
    return mats


def _strider_legs(k, f):
    scene, _ = e60.strider_legs(phase=f * TAU / 8)
    return render(*turned(scene, _str_mats(), k, 16), STR_SIZE, STR_EXT)


def _torso_scene():
    return am.strider_torso(1.0)[0]


def _strider_torso(k):
    return render(*turned(_torso_scene(), _str_mats(), k, 32), TORSO_SIZE, TORSO_EXT)


def _pod_scene():
    """Round 60's pod (enemies_r60.strider_torso's three parts) around its own centre."""
    ox, oy = POD

    def scene(p):
        q = p + np.array((ox, oy, 0.0))
        return union((sd_box(q, (-0.36, -0.28, 0.92), (0.14, 0.15, 0.09), 0.02), A_HULL),
                     (sd_box(q, (-0.36, -0.22, 1.02), (0.1, 0.07, 0.02)), A_X),
                     (sd_box(q, (-0.36, -0.4, 1.0), (0.14, 0.02, 0.02)), A_GOLD))
    return scene


def _strider_pod(k, hatch):
    return render(*turned(_pod_scene(), _str_mats(hatch), k, 32), POD_SIZE, POD_EXT)


def _strider_fall(d, side, step):
    tilt, sink, split = FALL_STEPS[step]
    scene, _ = e60.strider_whole(phase=0.8, tilt=tilt, side=side, sink=sink, split=split)
    return render(*turned(scene, _str_mats(), 2 * d, 16), FALL_BIG, FALL_EXT)


def _strider_hulk(k):
    sl, _ = e60.strider_legs(phase=0.0)
    st = _torso_scene()
    return render(*turned(lambda p: union(sl(p), st(p)), _str_mats(), k, 16), STR_SIZE, STR_EXT)


def knee(i):
    x, y = grid(16)
    k = (0.6, 1.0, 0.45)[i]
    f = np.exp(-(np.hypot(x, y) / (2.2 + 0.8 * i)) ** 2) * k
    return field_img(f, (255, 40, 30), white=0.75)


def knee_points(k, f):
    ph = f * TAU / 8
    out = []
    for side in (-1, 1):                          # left (-x) first
        pha = ph + (0.0 if side > 0 else np.pi)
        fy = 0.55 * np.sin(pha)
        out.append(screen_point((side * 0.68, 0.1 + fy * 0.45), k, 16, STR_SIZE, STR_EXT))
    return out


def crop_fall(frames):
    """Per (dir, side) the union box of its steps; one canvas for all (the largest box), each set centred in it;
    returns the cropped frames and per (dir, side) the walker's centre in the canvas."""
    boxes = []
    for g in range(0, len(frames), 6):
        al = np.max([np.array(f)[..., 3] for f in frames[g:g + 6]], axis=0)
        ys, xs = np.nonzero(al)
        boxes.append((xs.min(), ys.min(), xs.max() + 1, ys.max() + 1))
    cw = max(b[2] - b[0] for b in boxes) + 4
    ch = max(b[3] - b[1] for b in boxes) + 4
    cw += cw % 2
    ch += ch % 2
    out, anchors = [], []
    for g, (x0, y0, x1, y1) in enumerate(boxes):
        ox = int((x0 + x1) / 2 - cw / 2)
        oy = int((y0 + y1) / 2 - ch / 2)
        anchors.append([float(FALL_BIG / 2 - ox), float(FALL_BIG / 2 - oy)])
        for f in frames[g * 6:g * 6 + 6]:
            canvas = Image.new("RGBA", (cw, ch), (0, 0, 0, 0))
            canvas.paste(f, (-ox, -oy))
            out.append(canvas)
    return out, anchors, (int(cw), int(ch))


def wreck_of(img, seed):
    return buc.scorch(img, seed, 0.5)


def build_strider():
    jobs = [("legs", k, f) for k in range(16) for f in range(8)] + [("torso", k) for k in range(32)]
    jobs += [("pod", k, h) for k in range(32) for h in (0.0, 1.0)]
    jobs += [("fall", d, side, s) for d in range(FALL_DIRS) for side in (-1, 1) for s in range(6)]
    jobs += [("hulk", k) for k in range(16)]
    res = run(jobs)
    i = 0
    legs = res[i:i + 128]
    i += 128
    torso = res[i:i + 32]
    i += 32
    pod = res[i:i + 64]
    i += 64
    fall = res[i:i + FALL_DIRS * 12]
    i += FALL_DIRS * 12
    hulk = res[i:i + 16]
    legs = artkit.quantize_set([rim(f) for f in legs], 40)
    note_gold("strider-legs", legs[::8])
    save_set("strider-legs", legs)
    torso = artkit.quantize_set([rim(f) for f in torso], 40)
    note_gold("strider", torso)
    save_set("strider", torso)
    save_set("strider-pod", artkit.quantize_set([rim(f) for f in pod], 24))
    save_set("strider-knee", [knee(j) for j in range(3)])
    fall, anchors, canvas = crop_fall([rim(f) for f in fall])
    fall = artkit.quantize_set(fall, 48)
    save_set("strider-fall", fall)
    wrecks = [wreck_of(fall[g * 6 + 5], 3221 + g) for g in range(FALL_DIRS * 2)]
    save_set("strider-wreck", artkit.quantize_set(wrecks, 32))
    save_set("strider-hulk", artkit.quantize_set([wreck_of(rim(f), 3251 + k) for k, f in enumerate(hulk)], 32))
    write_strider_pivots(anchors, canvas)


def write_strider_pivots(anchors, canvas):
    write_pivots("strider", {
        "size": [STR_SIZE, STR_SIZE], "torso": [TORSO_SIZE, TORSO_SIZE], "headings": 16, "torso_headings": 32,
        "pod": [screen_point(POD, k, 32, TORSO_SIZE, TORSO_EXT) for k in range(32)], "pod_size": [POD_SIZE, POD_SIZE],
        "muzzles": [[screen_point((s * 0.56, 0.7), k, 32, TORSO_SIZE, TORSO_EXT) for s in (-1, 1)] for k in range(32)],
        "knees": [knee_points(k, f) for k in range(16) for f in range(8)],
        "fall": {"size": list(canvas), "directions": FALL_DIRS, "steps": 6, "anchor": anchors,
                 "order": "(dir * 2 + side) * 6 + step, dir = heading 16 // 2 (the walker's heading rounded to 45 "
                          "degrees), side 0 = the left leg lost (falls to its left), 1 = the right"},
        "note": "pod[k]: the pod's centre in torso frame k (the torso frame centred on the walker); muzzles[k]: the "
                "twin cannons' muzzles (left, right) in torso frame k; knees[heading * 8 + frame]: the left and right "
                "knee in a legs frame; fall.anchor[dir * 2 + side]: the walker's centre in a fall / wreck frame; "
                "all [x, y] px from the frame's top left"})


# =========================================================================== blast doors, the charge, the crate

DOOR = (128, 96)
DOOR_PX = 20.0
DOOR_EXT = DOOR[0] / DOOR_PX
FACE_X = 42 / DOOR_PX                           # the wall face: 42 px beyond the lock (the lock 40 px inside the field)
M_FRAME, M_LEAF, M_DECK, M_LOCK, M_LAMP, M_INSIDE, M_STRIPE = 7, 8, 9, 10, 11, 12, 13


def door_mats(style, lamp=1.0, inside=0.0, broken=False):
    mats = gunmetal(e60.belt_mats("c8202a"))
    mats[A_GOLD] = replace(mats[A_GOLD], albedo=(0.78, 0.6, 0.24))
    worn = style == "sealed"
    grey = (0.26, 0.26, 0.25) if worn else (0.3, 0.32, 0.36)
    mats[A_RED] = (Material((0.05, 0.02, 0.02), emission=(1.6, 0.18, 0.12)) if not worn
                   else Material((0.25, 0.08, 0.06)))
    extra = [Material(grey, metal=0.5, shininess=60, spec=0.6),                          # frame
             Material((0.36, 0.38, 0.42) if not worn else (0.5, 0.48, 0.42), metal=0.55, shininess=70,
                      spec=0.7, pattern=_leaf_pattern),                                   # leaves
             Material((0.24, 0.25, 0.27) if not worn else (0.3, 0.3, 0.28), metal=0.4, shininess=40, spec=0.4,
                      pattern=_deck_pattern),                                             # deck plates
             Material((0.2, 0.2, 0.22) if not broken else (0.08, 0.07, 0.07), metal=0.6, shininess=60),   # console
             (e60.emissive(AMBER / 255.0, 0.2 + 1.6 * lamp) if not worn and not broken
              else Material((0.12, 0.1, 0.08))),                                          # lock lamp
             Material((0.05, 0.04, 0.03), emission=(1.0 * inside, 0.6 * inside, 0.25 * inside)),   # the bay inside
             Material((0.78, 0.6, 0.16) if worn else (0.78, 0.6, 0.24), metal=0.3, shininess=40)]  # stripes
    return mats + extra


def _leaf_pattern(p, n):
    return 1.0 - 0.18 * (np.abs(((p[:, 1] + 10) % 0.5) - 0.25) < 0.02)


def _deck_pattern(p, n):
    return 1.0 - 0.25 * ((np.abs(((p[:, 0] + 10) % 0.7) - 0.35) < 0.025) | (np.abs(((p[:, 1] + 10) % 0.7) - 0.35) < 0.025))


def door_scene(side, style, open_=0.0, broken=False):
    """A door pair in the wall face beyond the lock (right wall: +x), seen from above: the frame round it, two leaves
    meeting at y 0 (sliding apart along the wall as ``open_`` goes 0 -> 1), the deck plate from the lock to the door,
    the lock's console at the origin."""
    sx = 1.0 if side == "right" else -1.0
    fx = FACE_X
    half_h = 2.0
    worn = style == "sealed"

    def scene(p):
        q = p.copy()
        q[:, 0] *= sx
        items = [(sd_box(q, (fx + 0.55, 0, 0.18), (0.62, half_h + 0.35, 0.18), 0.03), M_FRAME),
                 (sd_box(q, (fx - 1.05, 0, 0.03), (1.05, 1.25, 0.03), 0.01), M_DECK),
                 (sd_box(q, (0, 0, 0.24), (0.42, 0.42, 0.24), 0.05), M_LOCK),
                 (sd_box(q, (0.12, 0, 0.5), (0.13, 0.13, 0.03), 0.02), M_LAMP)]
        if broken:
            items[2] = (sd_box(q, (0, 0, 0.12), (0.38, 0.4, 0.12), 0.05), M_LOCK)
            items[3] = (sd_box(q, (-0.1, 0.1, 0.26), (0.12, 0.12, 0.03), 0.02), M_LAMP)
        gap = open_ * half_h
        leaf_h = (half_h - gap) / 2
        if leaf_h > 0.02:
            for sy in (-1, 1):
                cy = sy * (gap + leaf_h)
                items.append((sd_box(q, (fx + 0.32, cy, 0.4), (0.32, leaf_h - 0.02, 0.06), 0.02), M_LEAF))
                if not worn:
                    items.append((sd_box(q, (fx + 0.32, cy - sy * (leaf_h - 0.22), 0.47), (0.26, 0.05, 0.02)),
                                  A_GOLD))
        items.append((sd_box(q, (fx + 0.5, 0, 0.3), (0.42, half_h - 0.02, 0.02)), M_INSIDE))
        for sy in (-1, 1):                         # the frame's jambs: gold trim and red running lights (lit doors)
            items.append((sd_box(q, (fx + 0.32, sy * (half_h + 0.18), 0.4), (0.4, 0.12, 0.05), 0.02),
                          M_STRIPE if worn else A_GOLD))
            for lx in (fx - 0.05, fx + 0.75):
                items.append((sd_sphere(q, (lx, sy * (half_h + 0.24), 0.48), 0.07), A_RED))
            for k in range(3):                     # deck edge lights toward the lock
                items.append((sd_sphere(q, (fx - 0.4 - 0.6 * k, sy * 1.2, 0.07), 0.05), A_RED))
        if worn:                                   # the consortium's hazard stripes across the leaves, a stamp
            for k in range(-3, 4):
                y = k * 0.55
                if abs(y) > gap + 0.1:
                    items.append((sd_box(rotate_xy(q, (fx + 0.32, y), 0.6), (fx + 0.32, y, 0.47), (0.24, 0.07, 0.01)),
                                  M_STRIPE))
        return union(*items)
    return scene


def rotate_xy(q, c, a):
    out = q.copy()
    x, y = q[:, 0] - c[0], q[:, 1] - c[1]
    out[:, 0] = c[0] + x * np.cos(a) - y * np.sin(a)
    out[:, 1] = c[1] + x * np.sin(a) + y * np.cos(a)
    return out


def _door(side, style, open_, lamp, inside, broken):
    return render(door_scene(side, style, open_, broken), door_mats(style, lamp, inside, broken), DOOR, DOOR_EXT)


def weathered(img, seed, k=0.2):
    """The consortium store's door: dust, grime streaks, faded paint (no gloss)."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    n = raster.fbm(w, h, 8, seed, octaves=3, period=False)
    lum = a[..., :3] @ np.array((0.3, 0.55, 0.15))
    a[..., :3] = a[..., :3] * (1 - k) + lum[..., None] * k
    a[..., :3] *= (0.86 + 0.24 * n)[..., None]
    a[..., :3] += np.array((10, 7, 3)) * (n > 0.6)[..., None]
    a[~op] = 0
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


CHARGE = 24
CHARGE_EXT = CHARGE / 20.0


def charge_scene():
    def scene(p):
        return union((sd_box(p, (0, 0, 0.04), (0.42, 0.34, 0.04), 0.02), 9),
                     (sd_capsule(p, (-0.26, 0, 0.2), (0.26, 0, 0.2), 0.17), 8),
                     (sd_box(p, (-0.12, 0, 0.2), (0.04, 0.2, 0.2)), 10),
                     (sd_box(p, (0.12, 0, 0.2), (0.04, 0.2, 0.2)), 10),
                     (sd_box(p, (0.0, -0.3, 0.12), (0.5, 0.04, 0.05)), 10),
                     (sd_sphere(p, (0.0, 0.06, 0.39), 0.07), 11))
    return scene


def _charge(lamp):
    mats = gunmetal(e60.belt_mats("c8202a"))
    mats += [Material((0.2, 0.2, 0.2)), Material((0.86, 0.62, 0.12), metal=0.2, shininess=40, spec=0.5),
             Material((0.32, 0.3, 0.26), shininess=20), Material((0.08, 0.08, 0.08), metal=0.3),
             e60.emissive(AMBER / 255.0, 0.15 + 2.0 * lamp)]
    return render(charge_scene(), mats, CHARGE, CHARGE_EXT)


def blast(i, n=10):
    """The 64 px burst: a white-hot flash, the shock ring running out to 64 px, sparks and hot chips."""
    w = 144
    x, y = grid(w)
    r = np.hypot(x, y)
    u = i / (n - 1)
    rr = 14 + 50 * min(1, u * 1.6)
    core = np.exp(-(r / (10 + 26 * u)) ** 2) * max(0, 1.2 - 1.3 * u)
    shock = np.exp(-((r - rr) / (2.0 + 3 * u)) ** 2) * max(0, 1 - u) * 0.9
    f = np.clip(core + shock, 0, 1.2)
    img = field_img(f, (255, 150, 60), white=0.55)
    rng = np.random.default_rng(3261)
    d = ImageDraw.Draw(img)
    for _ in range(26):
        a, s = rng.uniform(0, TAU), rng.uniform(0.6, 1.1)
        r0, r1 = 8 + 60 * u * s, 14 + 66 * u * s
        if r1 < 70 and u < 0.9:
            col = (255, int(200 - 120 * u), int(120 - 100 * u), 255)
            d.line([72 + np.cos(a) * r0, 72 + np.sin(a) * r0, 72 + np.cos(a) * r1, 72 + np.sin(a) * r1], fill=col)
    return img


CRATE = 40
CRATE_EXT = CRATE / 20.0


def crate_scene(open_=0.0):
    def scene(p):
        items = [(sd_box(p, (0, 0, 0.26), (0.66, 0.5, 0.26), 0.04), 8),
                 (sd_box(p, (0, 0, 0.53), (0.12, 0.34, 0.01)), 10),
                 (sd_box(p, (0, 0, 0.53), (0.34, 0.12, 0.01)), 10),
                 (sd_box(p, (0.5, 0.42, 0.5), (0.08, 0.05, 0.04)), 9),
                 (sd_sphere(p, (-0.52, 0.38, 0.55), 0.06), 11)]
        if open_ > 0:
            items = items[:1] + [(sd_box(p, (0, 0, 0.5), (0.58, 0.42, 0.02)), 12),
                                 (sd_box(p, (0.8 * open_ + 0.2, -0.3 * open_, 0.55 + 0.2 * open_),
                                         (0.6, 0.46, 0.03), 0.02), 13)]
        return union(*items)
    return scene


def _crate(open_, glow):
    mats = gunmetal(e60.belt_mats("c8202a"))
    mats += [Material((0.2, 0.2, 0.2)), Material((0.62, 0.64, 0.6), metal=0.3, shininess=50, spec=0.5),
             Material((0.3, 0.3, 0.3), metal=0.5), Material((0.2, 0.62, 0.32), shininess=30),
             e60.emissive(AMBER / 255.0, 1.4 if open_ == 0 else 0.1),
             Material((0.05, 0.05, 0.05), emission=(0.25 * glow, 1.0 * glow, 0.45 * glow)),
             Material((0.6, 0.62, 0.58), metal=0.3, shininess=50)]
    return render(crate_scene(open_), mats, CRATE, CRATE_EXT)


def build_doors():
    jobs = []
    for side in ("right", "left"):
        jobs += [("door", side, "lit", 0.0, lamp, 0.0, False) for lamp in (0.35, 0.7, 1.0, 0.7)]
        jobs += [("door", side, "lit", u, 0.0, 0.4 + 0.6 * u, True) for u in (0.0, 0.15, 0.35, 0.6, 0.85, 1.0)]
    jobs += [("door", "right", "sealed", 0.0, 0.0, 0.0, False)]
    jobs += [("door", "right", "sealed", u, 0.0, 0.3 + 0.5 * u, True) for u in (0.0, 0.15, 0.35, 0.6, 0.85, 1.0)]
    res = run(jobs)
    for j, side in enumerate(("right", "left")):
        frames = artkit.quantize_set([rim(f) for f in res[j * 10:j * 10 + 10]], 40)
        save_set(f"blast-door-{side}", frames[:4])
        save_set(f"blast-door-{side}-open", frames[4:])
    sealed = artkit.quantize_set([weathered(f, 3271 + i) for i, f in enumerate(res[20:27])], 36)
    save_set("blast-door-sealed-right", sealed[:1], single=True)
    save_set("blast-door-sealed-right-open", sealed[1:])


def build_charge():
    res = run([("charge", lamp) for lamp in (0.1, 0.5, 1.0, 0.5)])
    save_set("mining-charge", artkit.quantize_set(res, 24))
    save_set("mining-charge-blast", [blast(i) for i in range(10)])


def build_crate():
    res = run([("crate", 0.0, 0.0)] + [("crate", u, g) for u, g in ((0.3, 0.6), (0.6, 1.0), (1.0, 0.8), (1.0, 0.5))])
    frames = artkit.quantize_set(res, 28)
    save_set("repair-crate", frames[:1], single=True)
    save_set("repair-crate-open", frames[1:])


# =========================================================================== jobs

JOBS = {"sbase": _sentinel_base, "shead": _sentinel_head, "stopple": _sentinel_topple, "legs": _strider_legs,
        "torso": _strider_torso, "pod": _strider_pod, "fall": _strider_fall, "hulk": _strider_hulk, "door": _door,
        "charge": _charge, "crate": _crate}


def _job(job):
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "belt-units-e"


def _model_key():
    """The renders depend on the code above the review section (and the imported concept models)."""
    text = Path(__file__).read_text(encoding="utf-8").split("# " + "=" * 75 + " review")[0]
    return hashlib.sha1(text.encode()).hexdigest()[:12]


def run(jobs):
    """Renders the jobs in parallel, cached in the system temp dir by the model code's hash and the job."""
    key = _model_key()
    paths = [CACHE / f"{hashlib.sha1((key + repr(j)).encode()).hexdigest()}.png" for j in jobs]
    todo = [i for i, p in enumerate(paths) if not p.exists()]
    if todo:
        CACHE.mkdir(parents=True, exist_ok=True)
        with ProcessPoolExecutor() as pool:
            for i, img in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img.save(paths[i])
    return [Image.open(p).convert("RGBA") for p in paths]


GOLD = {}


def note_gold(name, frames):
    shares = [e60.gold_share(f) for f in frames]
    GOLD[name] = (min(shares), max(shares))
    print(f"gold {name}: {100 * min(shares):.0f}-{100 * max(shares):.0f} %")


BUILDERS = {"sentinel": build_sentinel, "strider": build_strider, "doors": build_doors, "charge": build_charge,
            "crate": build_crate}


# =========================================================================== review

def plate(w=480, h=540, tile="tunnel-floor"):
    """The review ground: Level 32's production floor when backdrop_l32.py has written it, else round 60's tunnel."""
    src_path = BACKDROP / f"{tile}.png"
    src = Image.open(src_path).convert("RGBA") if src_path.exists() else e60.tunnel_bg(321, 540).convert("RGBA")
    out = Image.new("RGBA", (w, h))
    for y in range(0, h, src.height):
        for x in range(0, w, src.width):
            out.alpha_composite(src.crop((0, 0, min(src.width, w - x), min(src.height, h - y))), (x, y))
    return out


paste, add, ship_sprite = buc.paste, buc.add, buc.ship_sprite


def caption(img, text):
    e60.caption(img, text)
    return img


def gif_frames(draw, seconds, base):
    return [draw(base.copy(), i / FPS) for i in range(int(seconds * FPS))]


def save_review(kind, subject, sheet, gif=None):
    png, gif_path = artkit.review_paths(CONCEPT[kind], subject)
    sheet.convert("RGB").save(png, optimize=True)
    msg = f"review: {png.relative_to(ROOT)}"
    if gif:
        size = e60.rig.write_gif(gif, gif_path, fps=FPS, colors=128)
        msg += f", {gif_path.name} ({size / 1e6:.1f} MB)"
    print(msg)


def share(name, frames):
    vals = [e60.gold_share(f) for f in frames]
    lo, hi = min(vals), max(vals)
    return f"{100 * lo:.0f}" if round(lo * 100) == round(hi * 100) else f"{100 * lo:.0f}-{100 * hi:.0f}"


def explosion(rung):
    try:
        return artkit.load_frames(f"explosion-{rung}")
    except Exception:
        return []


def boom(f, frames, i, x, y):
    return add(f, frames[i], x, y) if 0 <= i < len(frames) else f


def strip_beam(f, x, y, ang, length, frame, k=1.0):
    """The beam as the game tiles it: the strip turned along the beam (unlit effect), from the lens."""
    strip = load("sentinel-tower-beam")[frame]
    n = int(np.ceil(length / strip.width))
    row = Image.new("RGBA", (n * strip.width, strip.height))
    for j in range(n):
        row.paste(strip, (j * strip.width, 0))
    row = row.crop((0, 0, int(length), strip.height))
    rot = row.rotate(-np.degrees(ang), resample=Image.BILINEAR, expand=True)
    cx, cy = x + np.cos(ang) * length / 2, y + np.sin(ang) * length / 2
    return add(f, rot, cx, cy, k)


def sheet_of(title, sub, rows, notes, width=1000):
    hgt = 70 + sum(r[1].height + 26 for r in rows) + 16 * len(notes) + 10
    sh = raster.sheet(width, hgt, title, sub)
    y = 40
    for label, img in rows:
        raster.draw_text(sh, 16, y, label, raster.LABEL)
        sh.alpha_composite(img.convert("RGBA"), (16, y + 14))
        y += img.height + 26
    for line in notes:
        raster.draw_text(sh, 16, y, line, raster.LABEL_DIM)
        y += 16
    return sh


def row_of(frames, w=968, gap=6, scale=1, bg=None):
    frames = [f.resize((f.width * scale, f.height * scale), Image.NEAREST) if scale != 1 else f for f in frames]
    h = max(f.height for f in frames) + 8
    img = bg.crop((0, 0, w, h)) if bg is not None else plate(w, h)
    x = 4
    for f in frames:
        if x + f.width > w:
            break
        img.alpha_composite(f, (x, 4))
        x += f.width + gap
    return img


def review_sentinel():
    base = load("sentinel-tower")[0]
    heads = load("sentinel-tower-head")
    eyes = load("sentinel-tower-eye")
    pv = pivots("sentinel-tower")
    lens = pv["lens"]
    ring_img = load("sentinel-tower-ring")[0]
    wedge_img = load("sentinel-tower-telegraph-120")[0]
    med = explosion("medium")
    tx, ty0 = 320, 120
    sweeps = [(0.6, "arc", 1), (5.1, "arc", -1), (9.6, "full", 1)]

    def ship_at(t):
        if t < 9.6:
            return 200 + 70 * np.sin(t * 0.9), 430
        u = min(1, (t - 9.6) / 1.0)
        return 200 + 70 * np.sin(9.6 * 0.9) * (1 - u) - 120 * u, 430 + 60 * u

    def draw(f, t):
        ty = ty0 + 22 * t
        sx, sy = ship_at(t)
        bearing = np.arctan2(sy - ty, sx - tx)
        aim, live, tel, kind = bearing, 0.0, None, None
        for t0, kd, dr in sweeps:
            telw = 0.8 if kd == "arc" else 1.0
            dur = 1.6 if kd == "arc" else 3.0
            b0 = np.arctan2(ship_at(t0)[1] - (ty0 + 22 * t0), ship_at(t0)[0] - tx)
            start = b0 - dr * np.radians(60) if kd == "arc" else b0
            span = np.radians(120) if kd == "arc" else TAU
            if t0 <= t < t0 + telw:
                tel, kind, aim, live = (b0, (t - t0) / telw), kd, start, 0.4 * (t - t0) / telw
            elif t0 + telw <= t < t0 + telw + dur:
                aim, live = start + dr * span * (t - t0 - telw) / dur, 1.0
        dead = t >= 13.4
        if not dead:
            paste(f, base, tx, ty)
        else:
            paste(f, load("sentinel-tower-stump")[0], tx, ty)
            k = int((t - 13.4) * FPS / 2)
            if k < 6:
                paste(f, load("sentinel-tower-topple")[k], tx, ty)
            f = boom(f, med, int((t - 13.4) * FPS), tx, ty)
        if tel is not None:
            if kind == "arc":
                w_ = wedge_img.rotate(-np.degrees(tel[0]), resample=Image.BILINEAR, expand=True)
                f = add(f, w_, tx, ty, 0.5 + 0.5 * tel[1])
            else:
                f = add(f, ring_img, tx, ty, 0.5 + 0.5 * tel[1])
                f = strip_beam(f, tx, ty, aim, REACH, 0, 0.35)
        k = int(round(((aim - np.pi / 2) % TAU) / TAU * 32)) % 32       # heading k: clockwise from straight down
        if not dead:
            lx, ly = lens[k][0] - HEAD_SIZE / 2 + tx, lens[k][1] - HEAD_SIZE / 2 + ty
            if live >= 1.0:
                f = strip_beam(f, lx, ly, aim, REACH - 16, int(t * FPS) % 4)
                f = add(f, load("sentinel-tower-beam-tip")[int(t * FPS) % 4], tx + np.cos(aim) * REACH,
                        ty + np.sin(aim) * REACH)
            paste(f, heads[k], tx, ty)
            if live > 0:
                f = add(f, eyes[min(3, int(live * 3.99))], lx, ly)
        paste(f, ship_sprite(), sx, sy)
        if 0.6 <= t < 1.4:
            caption(f, "ARC TELEGRAPH 0.8 S, CENTRED ON THE SHIP'S BEARING")
        elif 9.6 <= t < 10.6:
            caption(f, "EVERY THIRD SWEEP A FULL TURN: THE REACH RING, LEAVE IT")
        elif t >= 13.4:
            caption(f, "EMITTER DOWN: THE HEAD TOPPLES OFF, THE STUMP STAYS")
        return f

    gif = gif_frames(draw, 15.0, plate())
    heads_row = row_of([heads[k] for k in range(0, 32, 2)], gap=2)
    bigs = row_of([base, load("sentinel-tower-stump")[0]] + load("sentinel-tower-topple") + eyes
                  + load("sentinel-tower-beam") + load("sentinel-tower-beam-tip"), scale=1)
    comp = Image.new("RGBA", (80, 80))
    comp.alpha_composite(base)
    comp.alpha_composite(heads[0], (8, 8))
    zoom = row_of([comp, comp.resize((240, 240), Image.NEAREST)], gap=12)
    rows = [("THE TOWER, STUMP, TOPPLE (6), EYE GLOW (4), BEAM STRIP (4), BEAM TIP (4) (1X, OVER THE TUNNEL FLOOR)", bigs),
            ("THE EMITTER HEAD, EVERY SECOND OF ITS 32 HEADINGS (1X)", heads_row),
            ("THE TOWER WITH ITS HEAD (1X, 3X)", zoom),
            ("KEY FRAMES (0.48X): ARC TELEGRAPH, ARC SWEEP, THE FULL TURN'S RING, THE DEATH",
             row_of([g.resize((230, 259), Image.BOX) for g in (gif[16], gif[40], gif[204], gif[276])], gap=4))]
    notes = [f"SENTINEL TOWER (A.12 A, LIGHTHOUSE) IN Q.11 B: GUNMETAL HULL, GOLD COLLAR BAND AND EQUATOR RING "
             f"(GOLD {share('sentinel-tower', [base])} % OF THE TOWER, {share('sentinel-tower-head', heads)} % OF THE HEAD),",
             "RED RUNNING LIGHTS ON THE COLLAR'S CORNERS, THE 1 PX GOLD/RED RIM. THE HEAD TURNS TO THE BEAM (32 HEADINGS); THE",
             "LENS GLOW, BEAM, TIP, WEDGES (100/120/160) AND THE 220 PX REACH RING ARE ADDITIVE EFFECTS THE GAME TURNS."]
    sh = sheet_of("SENTINEL TOWER - FINAL (ROUND 65)", f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}", rows, notes)
    save_review("sentinel", "sentinel-tower", sh, gif)


def review_strider():
    legs = load("strider-legs")
    torso = load("strider")
    pods = load("strider-pod")
    fall = load("strider-fall")
    wreck = load("strider-wreck")
    knees = load("strider-knee")
    pv = pivots("strider")
    sam_base = load("sam-nest-base")[0]
    sam = load("sam-nest")
    med = explosion("medium")
    large = explosion("large")
    T_LEG, DIR, SIDE = 5.0, 0, 1                 # heading 0 (down), the right leg lost: it falls to its right
    x0, y0 = 220, 80

    def pos(t):
        tt = min(t, T_LEG)
        return x0, y0 + 30 * tt

    fx, fy = pos(T_LEG)
    dx = -1.0                                     # heading 0 faces down: its right is the screen's left
    sam_xy = (fx + dx * 90, fy + 20)

    def ship_at(t):
        return 300 + 60 * np.sin(t * 0.8), 470

    def draw(f, t):
        x, y = pos(t)
        crushed = t >= T_LEG + 1.4
        if not crushed:
            paste(f, sam_base, *sam_xy)
            paste(f, sam[int(((np.arctan2(470 - sam_xy[1], ship_at(t)[0] - sam_xy[0]) - np.pi / 2) % TAU)
                             / TAU * 16 + 0.5) % 16], *sam_xy)
        else:
            paste(f, load("sam-nest-wreck")[0], *sam_xy)
            f = boom(f, med, int((t - T_LEG - 1.4) * FPS), *sam_xy)
        if t < T_LEG:
            ph = int(t * 3.2 * 2.5) % 8
            paste(f, legs[ph], x, y)
            sx, sy = ship_at(t)
            aim = np.arctan2(sy - y, sx - x)
            k = int(round(((aim - np.pi / 2) % TAU) / TAU * 32)) % 32
            paste(f, torso[k], x, y)
            hatch = 1 if 2.0 <= t < 2.4 else 0
            px, py = pv["pod"][k]
            paste(f, pods[k * 2 + hatch], x + px - TORSO_SIZE / 2, y + py - TORSO_SIZE / 2)
            for th in (3.6, 4.0, 4.4, 4.8):
                j = int((t - th) * FPS)
                if 0 <= j < 3:
                    kx, ky = pv["knees"][ph][SIDE]
                    f = add(f, knees[j], x + kx - STR_SIZE / 2, y + ky - STR_SIZE / 2)
            if 1.2 <= t < 1.5 or 3.2 <= t < 3.5:
                for mx, my in pv["muzzles"][k]:
                    f = add(f, knees[1], x + mx - TORSO_SIZE / 2, y + my - TORSO_SIZE / 2, 0.8)
        else:
            u = t - T_LEG
            step = min(5, int(u / 0.6 * 2)) if u < 0.6 else min(5, 2 + int((u - 0.6) / 0.8 * 4))
            g = DIR * 2 + SIDE
            ax, ay = pv["fall"]["anchor"][g]
            fr = wreck[g] if crushed else fall[g * 6 + step]
            if u < 1.4:                             # the fall line: a dark strip 48 x 140 from the centre
                lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
                d = ImageDraw.Draw(lay)
                d.polygon([(fx, fy - 24), (fx + dx * 140, fy - 24), (fx + dx * 140, fy + 24), (fx, fy + 24)],
                          fill=(6, 4, 4, 150), outline=(220, 50, 40, 200))
                f.alpha_composite(lay)
            l23kit._paste_clip(f, fr, int(round(fx - ax)), int(round(fy - ay)))
            if crushed:
                f = boom(f, large, int((t - T_LEG - 1.4) * FPS), fx + dx * 50, fy)
        paste(f, ship_sprite(), *ship_at(t))
        if T_LEG <= t < T_LEG + 0.6:
            caption(f, "LEG DOWN: THE STAGGER (0.6 S) MARKS THE FALL LINE")
        elif T_LEG + 0.6 <= t < T_LEG + 1.4:
            caption(f, "THE KNEE BUCKLES, IT PITCHES OVER (0.8 S)")
        elif t >= T_LEG + 1.4:
            caption(f, "THE CRUSH: THE SAM NEST UNDER THE LINE IS PAID")
        elif 2.0 <= t < 2.4:
            caption(f, "THE POD'S HATCH GLOWS GOLD 0.4 S BEFORE A SALVO")
        return f

    gif = gif_frames(draw, 9.5, plate())
    whole = []
    for k in range(0, 16, 2):
        c = legs[k * 8].copy()
        tk = k * 2
        c.alpha_composite(torso[tk], (12, 12))
        px, py = pv["pod"][tk]
        c.alpha_composite(pods[tk * 2], (int(12 + px - POD_SIZE / 2), int(12 + py - POD_SIZE / 2)))
        whole.append(c)
    rows = [("THE WALKER: LEGS + TORSO + POD AT 8 OF ITS 16 HEADINGS (1X, OVER THE TUNNEL FLOOR)", row_of(whole, gap=0)),
            ("THE WALK CYCLE, HEADING 0 (8 FRAMES) AND HEADING 4", row_of(legs[:8] + legs[32:36], gap=0)),
            ("THE TORSO, EVERY 4TH OF 32; THE POD SHUT / OPEN", row_of(torso[::4] + pods[:2] + pods[16:18], gap=2)),
            ("THE FALL TO THE RIGHT, HEADING 0: STAGGER 2 + FALL 4, THE WRECK; THE HULK", row_of(
                fall[1 * 6:1 * 6 + 6] + [wreck[1]], gap=2)),
            ("KEY FRAMES (0.48X): WALKING AND FIRING, THE STAGGER AND THE LINE, FALLING, THE CRUSH",
             row_of([g.resize((230, 259), Image.BOX) for g in (gif[30], gif[106], gif[124], gif[150])], gap=4))]
    notes = [f"STRIDER (THE CHOSEN R05 WALKER, ROUND 60'S POD AND A.14 B) IN Q.11 B: GUNMETAL, GOLD KNEES, CLAWS AND "
             f"SHOULDERS (GOLD {share('strider', torso)} % OF THE TORSO, {share('strider-legs', legs[::8])} % OF THE LEGS),",
             "RED LIGHTS, THE 1 PX GOLD/RED RIM. LEGS 16 HEADINGS X 8 FRAMES, TORSO 32, THE POD A PART OF ITS OWN (NOT",
             "DRAWN WHEN DESTROYED). THE FALL: 8 DIRECTIONS X 2 SIDES X 6 STEPS, THE KNEE BUCKLES, THE TORSO BREAKS OFF."]
    sh = sheet_of("STRIDER - FINAL (ROUND 65)", f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}", rows, notes)
    save_review("strider", "strider", sh, gif)


def review_objects():
    dr, dl = load("blast-door-right"), load("blast-door-left")
    dro, dlo = load("blast-door-right-open"), load("blast-door-left-open")
    sealed, sealed_o = load("blast-door-sealed-right"), load("blast-door-sealed-right-open")
    charge, blast_f = load("mining-charge"), load("mining-charge-blast")
    crate, crate_o = load("repair-crate"), load("repair-crate-open")
    talon = load("talon")
    sam_base, sam = load("sam-nest-base")[0], load("sam-nest")

    def draw(f, t):
        # the right wall at x 470 (the rock a dark band), a lit door, three charges by a SAM pair, the crate
        d = ImageDraw.Draw(f)
        d.rectangle([470, 0, 480, 540], fill=(30, 26, 24, 255))
        d.rectangle([0, 0, 10, 540], fill=(30, 26, 24, 255))
        opened = t >= 2.0
        k = int(t * 6) % 4
        door = dr[k] if not opened else dro[min(5, int((t - 2.0) * FPS / 3))]
        paste(f, door, 428, 120)
        paste(f, sealed[0] if t < 4.0 else sealed_o[min(5, int((t - 4.0) * FPS / 3))], 428, 270)
        paste(f, dl[k] if t < 5.5 else dlo[min(5, int((t - 5.5) * FPS / 3))], 52, 200)
        boomed = t >= 6.5
        for j, (cx, cy) in enumerate(((180, 400), (240, 430), (300, 400))):
            if not boomed or j != 1:
                paste(f, charge[(k + j) % 4], cx, cy)
        for sx in (200, 280):
            if not boomed:
                paste(f, sam_base, sx, 470)
                paste(f, sam[8], sx, 470)
            else:
                paste(f, load("sam-nest-wreck")[0], sx, 470)
        if not boomed:
            paste(f, talon[0], 240, 360 + 10 * t)
        i = int((t - 6.5) * FPS)
        if 0 <= i < 10:
            f = add(f, blast_f[i], 240, 430)
        paste(f, crate[0] if t < 3.0 else crate_o[min(3, int((t - 3.0) * FPS / 4))], 120, 80)
        if t < 2.0:
            caption(f, "A LIT BLAST DOOR: THE LOCK 42 PX FROM THE WALL FACE")
        elif t < 4.0:
            caption(f, "THE LOCK BLOWN: THE LEAVES SLIDE APART")
        elif t < 6.5:
            caption(f, "THE CONSORTIUM STORE: DARK LOCK, FADED STRIPES")
        else:
            caption(f, "A CHARGE'S 64 PX BURST: A TALON AND THE SAM PAIR")
        return f

    gif = gif_frames(draw, 8.5, plate())
    rows = [("LIT BLAST DOOR RIGHT: LAMP 4 STEPS, OPENING 6 (1X)", row_of(dr + dro, gap=4)),
            ("LIT BLAST DOOR LEFT, OPENING; THE SEALED STORE AND ITS OPENING", row_of(dl[:1] + dlo[::2] + sealed + sealed_o[::2], gap=4)),
            ("MINING CHARGE (4), ITS BURST (EVERY 2ND OF 10); THE REPAIR CRATE AND ITS OPENING",
             row_of(charge + blast_f[::2] + crate + crate_o, gap=4)),
            ("KEY FRAMES (0.48X)", row_of([g.resize((230, 259), Image.BOX) for g in (gif[10], gif[60], gif[100],
                                                                                     gif[136])], gap=4))]
    notes = ["THE DOORS ARE GROUND TARGETS ON THE LOCK (THE IMAGE'S CENTRE): THE DOOR PAIR SITS IN THE WALL FACE 42 PX BEYOND,",
             "SO THE LOCK STAYS 40 PX INSIDE THE FIELD (QE-1 A). LIT: ASCENDANCY FRAME, GOLD TRIM, RED RUNNING LIGHTS, AMBER LOCK;",
             "SEALED: THE CONSORTIUM'S WORN GREY, HAZARD STRIPES, NO LIGHTS. THE CHARGE: THE MINERS' YELLOW CANISTER, AMBER LAMP."]
    sh = sheet_of("LEVEL 32 GROUND OBJECTS - FINAL (ROUND 65)", f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}",
                  rows, notes)
    save_review("objects", "ground-objects", sh, gif)


REVIEWS = {"sentinel": review_sentinel, "strider": review_strider, "doors": review_objects, "charge": None,
           "crate": None}


# =========================================================================== check

SETS = {"sentinel": [("sentinel-tower", 1), ("sentinel-tower-head", 32), ("sentinel-tower-eye", 4),
                     ("sentinel-tower-beam", 4), ("sentinel-tower-beam-tip", 4), ("sentinel-tower-telegraph-100", 1),
                     ("sentinel-tower-telegraph-120", 1), ("sentinel-tower-telegraph-160", 1),
                     ("sentinel-tower-ring", 1), ("sentinel-tower-stump", 1), ("sentinel-tower-topple", 6)],
        "strider": [("strider-legs", 128), ("strider", 32), ("strider-pod", 64), ("strider-knee", 3),
                    ("strider-fall", 96), ("strider-wreck", 16), ("strider-hulk", 16)],
        "doors": [("blast-door-right", 4), ("blast-door-left", 4), ("blast-door-right-open", 6),
                  ("blast-door-left-open", 6), ("blast-door-sealed-right", 1), ("blast-door-sealed-right-open", 6)],
        "charge": [("mining-charge", 4), ("mining-charge-blast", 10)],
        "crate": [("repair-crate", 1), ("repair-crate-open", 4)]}


def check():
    bad = 0
    for subject, sets in SETS.items():
        for name, n in sets:
            try:
                frames = load(name)
                folder = where(name)
                paths = [p for d in (l23kit.ASSETS_SPRITES, l23kit.HELD) for p in d.glob(f"{name}*.png")
                         if p.stem == name or (p.stem.rsplit("_", 1)[0] == name and p.stem.rsplit("_", 1)[1].isdigit())]
                ok = len(frames) == n and all(Image.open(p).text.get(artkit.SOURCE_KEY) == SOURCE for p in paths)
            except FileNotFoundError:
                frames, folder, ok = [], "-", False
            size = frames[0].size if frames else None
            print(f"{'ok ' if ok else 'BAD'} {name}: {len(frames)}/{n} {size} in {folder}")
            bad += not ok
    for root in ("sentinel-tower", "strider"):
        ok = pivot_path(root).exists()
        print(f"{'ok ' if ok else 'BAD'} pivots/{root}.json in {pivot_path(root).parent.relative_to(ROOT)}")
        bad += not ok
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    subjects = [a for a in args if not a.startswith("--")] or list(BUILDERS)
    unknown = [s for s in subjects if s not in BUILDERS]
    if unknown:
        sys.exit(f"unknown: {unknown}; subjects: {', '.join(BUILDERS)}")
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    for s in subjects:
        if REVIEWS.get(s):
            REVIEWS[s]()
