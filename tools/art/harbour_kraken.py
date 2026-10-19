#!/usr/bin/env python3
"""Production art: the Harbour Kraken, Act 2's mid-boss wrapped round Platform Tiamat in Level 11
(design/enemies/bosses/harbour-kraken; M5 part E batch, concept round 33). Straight to production
from its chosen concept (user decision E9 = a): the round-07 redo (tools/concept/kraken_r07.py: the
layout, the arm paths, the head's height map and its surfacing curve) on the round-06 models
(tools/concept/render/boss_models.kraken_mantle, kraken_arm, kraken_tip), imported unchanged.

How it is built (user decision E7 = a): the two slam arms are runtime chains of tapered segment
sprites at 32 headings; the four gripping arms are a pre-rendered overlay registered on the
platform (the platform itself is Level 11's backdrop piece); the two idle arms are `sub` shadows;
the head and mantle are pre-rendered surfacing, eye and beak frames. Water (E4 = c, see
tools/art/driftjelly.py): everything under the surface is drawn by the game's generic `sub` pass
from plain frames; what is above it, its foam and the surfacing steps are pre-rendered here.

Scale and registration: the concept's, 88.2 px per model unit for the head (the mantle with the
arm roots about 250 px long, 130 px wide) in the 480 px play field, the four lanes 120 px wide. All
positions in assets/pivots/harbour-kraken.json are px relative to the **platform's centre**, the
point the backdrop's Platform Tiamat piece is placed at (round 07: a 208 x 152 px deck at 100 px per
platform unit, render/boss_models.platform; the overlay assumes those deck edges, x -104..104,
y -76..76): the head's centre is 216 px below it (round 33: 64 px lower than the concept's, its
top below the deck's south edge, HEAD_DROP).

Outputs (assets/sprites/; heading k = k x 11.25 degrees clockwise from straight down, the direction
a segment points toward the arm's tip; every heading its own render under the fixed key light):
  harbour-kraken-sub           160x276, the whole plain head and mantle, eyes shut: drawn through the
                               `sub` pass at the head's point whenever any of it is under water
  harbour-kraken_0..4          160x276, the head surfaced (the water plane at its up level), the part
                               above the water: 0 eyes shut, 1 eyes half open, 2 eyes open, 3 open
                               with the beak glowing half, 4 open with the beak's full crimson glow
                               (the 0.5 s before a fan: 3 then 4); over harbour-kraken-sub
  harbour-kraken-collar_0..3   160x276, the foam collar round the surfaced head, a 4-frame loop
                               (8 steps a frame), drawn over the head frame
  harbour-kraken-surface_0..11 160x276, the surfacing's surface layer, 12 frames x 10 steps = 2 s,
                               even in time along the concept's curve: a swell rising over the crown,
                               the crown breaking first, then the eyes and the rest, the collar
                               growing, water streaming off (a wet sheen on what just came up);
                               played 11 -> 0 to dive (the layer flips at frame 6)
  harbour-kraken-arm_0..223    the slam arms' segments: 7 tapers (42, 37, 32, 27, 22, 18, 14 px,
                               square) x 32 headings, indexed heading * 7 + taper (taper 0 at the
                               root); the crimson dorsal seam emissive
  harbour-kraken-tip_0..63     the arm tip's curl, 2 sizes (34, 22 px) x 32 headings, indexed
                               heading * 2 + size
  harbour-kraken-foam_0..15    24x24, a foam clump where an arm breaks the surface or lies awash:
                               2 variants x 8 frames (burst, spread, break up, fade; 6 steps a frame),
                               indexed variant * 8 + frame, stepped translucency
  harbour-kraken-grip_0..3     256x156, the four gripping arms where they lie on the deck and climb
                               over its west, east and south edges (with the foam at the crossings and
                               their shadows on the deck), a 4-frame sway (12 steps a frame); drawn
                               over the platform piece, centred 22 px below its centre
  harbour-kraken-grip-sub_0..3 344x368, the same arms' under-water stretches from the head's roots to
                               the deck's edges, plain, the same sway; through the `sub` pass
  harbour-kraken-idle-left_0..3, harbour-kraken-idle-right_0..3
                               100x120, the two idle arms trailing deep below toward the lower
                               corners, squirming (4 frames, 12 steps a frame): dark shadows for the
                               `sub` pass at a stepped 2/3 opacity, rendered at 200x240 and halved,
                               drawn at 2x (unlit and blurred by the pass, so the scale does not show)
  harbour-kraken-shadow_0..1   102x235 (drawn at 2x, as the idle arms), the foreshadowing: the
                               whole animal as a deep shadow swimming crown first, arms streaming behind (2 frames, 15 steps a frame), for
                               the passing shape at t ~ 55 and the mantle under the platform in
                               section 4; drawn through the `sub` pass, unlit, so it may be turned
  harbour-kraken-sink_0..7     160x276, the death's surface layer: the head going down in a churning
                               foam ring, its eyes dead (8 frames x 12 steps); over the fading sub frame
  harbour-kraken-grip-slide_0..4 256x156, the death: the gripping arms sliding off the deck into the
                               water (5 frames x 12 steps; the grip-sub fading out with them)
  harbour-kraken-death_0..9    160x160, additive, centred 40 px below the head's centre: the `huge`
                               death's burst at the head: lime from the eyes, crimson from the beak and seam, spray glints (8 steps a frame);
                               the generic water-surface explosion and splash come from
                               tools/art/water_fx.py
  harbour-kraken-arm-death_0..7 48x48, additive: a severed slam arm's segment pop (crimson, droplets;
                               4 steps a frame, each segment 3 steps after the one rootward of it)
  pivots/harbour-kraken.json   the registration above, the eyes (weak spot) and the beak (fan
                               origin), the slam arms' roots and rest headings, the tapers and the
                               frame timings
  design/enemies/bosses/harbour-kraken/concept/harbour-kraken-final-r33-a.png/.gif

How the game draws it (game side, step E3c), back to front: the idle shadows and the grip's
under-water stretch and the plain head (whenever not fully surfaced) and every slam-arm segment whose
water height is below zero, all in the `sub` pass; the platform (backdrop); the grip overlay; the
head's surface layer (a surfacing/diving frame, or the up frame + collar); the slam arms' segments
above the water (tip first, root on top), a foam clump where the chain crosses the surface and along
it while it lies awash. The slam's churning telegraph and splash are round 33's a/b, not here.

Run: python3 tools/art/harbour_kraken.py [--review]   (~6 min on 20 cores; the head renders are
cached in the system temp dir, conceptgame-cache/; --review only rebuilds the review files)
"""
import json
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import driftjelly as dj  # noqa: E402  (tools/art: the water look)
import kraken_r07 as kr  # noqa: E402  (concept script, imported unchanged: layout, paths, height map)
import vfx_r08 as v8  # noqa: E402
import vrell_deaths as vd  # noqa: E402
import vrell_fx as fx  # noqa: E402
from render import boss_models as bm  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import sdf  # noqa: E402
from render.enemy_models import V_DARK  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials, write_gif  # noqa: E402
from render.sdf import mirror_x, rotate_z, sd_ellipsoid, union  # noqa: E402

# Round 33 (user, 2026-10-09): the head surfaces and sinks off the deck's south edge, in open water in
# front of the platform, not through it. The concept's layout (imported unchanged) is moved here: the
# head, its arm roots, the idle and slam arms' paths drop HEAD_DROP px; the gripping arms' under-water
# control points drop less toward the deck's edge (their crossings and deck parts stay), so they still
# reach from the roots up to the deck and hold on.
HEAD_DROP = 64.0
SIDE_DROP = (1.0, 1.0, 0.75, 0.4)                 # SIDE_L's four under-water points, root first
SOUTH_DROP = (1.0, 1.0, 0.8, 0.5)                 # SOUTH_L's
kr.HY += HEAD_DROP
kr.CROWN = (kr.CROWN[0], kr.CROWN[1] + HEAD_DROP)
kr.BEAK = (kr.BEAK[0], kr.BEAK[1] + HEAD_DROP)
kr.ROOTS = {k: kr._root(a) for k, a in zip(range(8), np.linspace(-1.6, 1.6, 8))}
kr.SIDE_L = [(x, y + HEAD_DROP * w) for (x, y), w in zip(kr.SIDE_L, SIDE_DROP)] + kr.SIDE_L[len(SIDE_DROP):]
kr.SOUTH_L = [(x, y + HEAD_DROP * w) for (x, y), w in zip(kr.SOUTH_L, SOUTH_DROP)] + kr.SOUTH_L[len(SOUTH_DROP):]
kr.IDLE_L = [(x, y + HEAD_DROP) for x, y in kr.IDLE_L]
kr.SLAM_REST_L = [(x, y + HEAD_DROP) for x, y in kr.SLAM_REST_L]
kr.SLAM_LANE_L = [(x, y + HEAD_DROP) for x, y in kr.SLAM_LANE_L]

SCRIPT = "harbour_kraken.py"
BATCH = "M5 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r33"
UNIT = DESIGN / "enemies" / "bosses" / "harbour-kraken"
CONCEPT = UNIT / "concept"
SLUG = "harbour-kraken"
STEP = 60
CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "harbour-kraken"

HEAD = (160, 276)
HPX = kr.H_W / kr.H_EXT                         # 88.2 px per model unit (the concept's)
H_EXT = HEAD[0] / HPX
H_CY = kr.H_CY
FACTOR = 4
UP_LEVEL = 0.04                                 # the head's water level when surfaced (concept)
DEEP_LEVEL = 1.3
SURFACE_FRAMES, SURFACE_STEPS = 12, 10
SINK_FRAMES, SINK_STEPS = 8, 12
COLLAR_FRAMES, COLLAR_STEPS = 4, 8
EYES = [("shut", 0.0), ("half", 0.0), ("open", 0.0), ("open", 0.5), ("open", 1.0)]
COLOURS = 48

HEADINGS = 32
TAPERS = [42, 37, 32, 27, 22, 18, 14]
TIPS = [34, 22]
ARM_EXTENT, TIP_EXTENT = 1.6, 1.3               # the concept's segment canvases (kraken_r07 / bosses_r06)
FOAM_SIZE, FOAM_FRAMES, FOAM_VARIANTS = (24, 24), 8, 2
SWAY_FRAMES, SWAY_STEPS = 4, 12

PLAT = np.array(kr.PLAT)                        # the platform's centre in the concept's field (240, 110)
HEAD_AT = np.array([kr.HX, kr.HY])              # the head's centre (240, 262)
GRIP = (256, 156)
GRIP_AT = np.array([0.0, 22.0])                 # its centre relative to the platform's centre
GRIP_SUB = (344, 368)                           # round 33: 64 px taller, down to the dropped roots
GRIP_SUB_AT = np.array([0.0, 184.0])            # its centre relative to the platform's centre
IDLE = (200, 240)                               # rendered, then halved: drawn at 2x
HALF = 2
SHADOW = (204, 470)
SLIDE_FRAMES, SLIDE_STEPS = 5, 12
DEATH_SIZE, DEATH_FRAMES, DEATH_STEPS = (160, 160), 10, 8
ARM_DEATH, ARM_DEATH_FRAMES = (48, 48), 8

LIME = np.array(em.hx(em.GLOWS["lime"])) * 255
CROWN_PX = (HEAD[0] / 2, HEAD[1] / 2 + (-0.5 + H_CY) * HPX)   # the mantle's highest point
DEATH_AT = (0.0, 40.0)                          # the death burst's centre below the head's
CRIMSON = np.array(em.hx(em.GLOWS["crimson"])) * 255
DEEP_SHADE = np.array([4, 14, 30], float)


# =========================================================================== head

def head_model(eyes, beak):
    """The concept's mantle turned arms-down (kraken_r07._head_scene); ``eyes`` "half" adds a lid
    over the upper half of each open eye."""
    if eyes == "half":
        scene, mats = bm.kraken_mantle(1.0, beak)

        def lidded(p):
            q = mirror_x(p)
            return union(scene(p), (sd_ellipsoid(q, (0.47, 0.6, 0.33), (0.17, 0.12, 0.08)), V_DARK), k=0.02)
        base = lidded
    else:
        base, mats = bm.kraken_mantle(1.0 if eyes == "open" else 0.0, beak)
    return (lambda p: base(sdf.rotate_z(p, np.pi))), mats


def head_hi(key):
    """The 4x render of a head variant (cached outside the repo)."""
    eyes, beak = key
    CACHE.mkdir(parents=True, exist_ok=True)
    path = CACHE / f"head-{eyes}-{beak:.2f}.npy"
    if path.exists():
        return np.load(path)
    scene, mats = head_model(eyes, beak)
    hi = sdf.render(scene, mats, (HEAD[0] * FACTOR, HEAD[1] * FACTOR), H_EXT, center=(0.0, H_CY))
    np.save(path, hi)
    return hi


def head_z():
    """The height of the head's surface per 4x pixel (NaN off the head)."""
    CACHE.mkdir(parents=True, exist_ok=True)
    path = CACHE / "head-z.npy"
    if path.exists():
        return np.load(path)
    scene, _ = head_model("shut", 0.0)
    z = kr.heightmap(scene, (HEAD[0] * FACTOR, HEAD[1] * FACTOR), H_EXT, (0.0, H_CY))
    np.save(path, z)
    return z


def cut(hi, z, level):
    """The part of a 4x head render above the water plane ``level`` -> native sprite, and the
    native-size height map of what is visible."""
    keep = (hi[..., 3] > 0) & ~np.isnan(z) & (np.nan_to_num(z, nan=-9) > level)
    h = hi.copy()
    h[..., 3] = np.where(keep, hi[..., 3], 0)
    img = artkit.native(h, FACTOR, crisp=70)
    zz = np.nan_to_num(z, nan=-9).reshape(HEAD[1], FACTOR, HEAD[0], FACTOR).max(axis=(1, 3))
    return img, zz


def wet(img, zz, level, strength=1.0):
    """Water streaming off what just surfaced: a pale sheen and runnels on the parts close above
    the water plane, strongest at the waterline."""
    a = np.array(img).astype(np.float64)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    cx, cy = CROWN_PX
    th = np.arctan2(yy - cy, xx - cx)
    rr = np.hypot(yy - cy, xx - cx)
    wob = 3.0 * np.sin(rr * 0.09 + 1.7 * np.sin(th * 5)) + 2.0 * np.sin(rr * 0.23 + th * 3)
    riv = np.clip(np.sin(th * 23 + wob), 0, 1) ** 8 * (0.5 + 0.5 * np.sin(th * 7 + 1.3))
    near = np.exp(-np.clip(zz - level, 0, None) / 0.3) * (zz > level)
    k = np.clip(near * (0.12 + 0.3 * riv) * strength, 0, 0.42)[..., None] * (a[..., 3:4] > 0)
    a[..., :3] = a[..., :3] * (1 - k) + np.array([175, 215, 242]) * k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def surface_levels():
    """The water level of each surfacing frame: even in time along the concept's rise
    (kraken_r07.L_KEYS: 2.4 s from deep to up; the frames span it in SURFACE_FRAMES steps)."""
    t0, t1 = 2.4, 4.8
    return [kr.head_level(t0 + (t1 - t0) * (i + 1) / SURFACE_FRAMES) for i in range(SURFACE_FRAMES)]


def head_foam(img_mask, level_phase, seed, churn=0.0, gain=1.0):
    """The foam round the head's waterline (``img_mask``: what is above water), stepped."""
    f = dj.Field(*HEAD)
    if img_mask.sum() > 3:
        dj.edge_foam(f, img_mask, level_phase, seed, width=3.6 + 3 * churn, gain=gain, specks=70 + int(80 * churn),
                     reach=9 + 10 * churn, inner=2.0 + 2 * churn, centre=(HEAD[0] / 2, HEAD[1] / 2 + 10))
    return f.image()


def surface_frame(i, levels, hi, z):
    level = levels[i]
    w, h = HEAD
    f = dj.Field(w, h)
    cx, cy = CROWN_PX
    rise = np.clip((DEEP_LEVEL - level) / (DEEP_LEVEL - 0.6), 0, 1)
    if level > 0.56:                                          # the swell before the crown breaks
        dj.swell(f, cx, cy, 24 + 14 * rise, 0.3 + 0.45 * rise, aspect=1.5, crest=0.5, trough=0.22)
        rng = np.random.default_rng(3501 + i)                 # the first foam breaking on the swell
        for _ in range(int(4 + 22 * rise)):
            a, r = rng.uniform(0, TAU), (4 + 16 * rise) * np.sqrt(rng.uniform(0, 1))
            x, y = cx + np.cos(a) * r, cy + 6 + np.sin(a) * r * 1.5
            f.over(dj.FOAM, (np.hypot(f.x - x, f.y - y) <= rng.uniform(0.6, 1.6)) * rng.uniform(0.3, 0.8) * rise)
    out = f.image()
    img, zz = cut(hi, z, level)
    mask = np.array(img)[..., 3] > 0
    if mask.any():
        img = wet(img, zz, level, 1.0)
        out.alpha_composite(img)
        out.alpha_composite(head_foam(mask, 0.9 * i, 3510 + i, churn=0.4, gain=0.95))
    return out


def up_frame(key, z):
    img, zz = cut(head_hi(key), z, UP_LEVEL)
    return img


def collar_frame(j, mask):
    return head_foam(mask, TAU * j / COLLAR_FRAMES, 3520, churn=0.0, gain=0.9)


def sink_frame(i, z):
    """The death: the head goes down with its eyes dead in a churning foam ring."""
    t = (i + 1) / SINK_FRAMES
    level = UP_LEVEL + (DEEP_LEVEL - UP_LEVEL) * t ** 1.4
    img, zz = cut(head_hi(("shut", 0.0)), z, level)
    a = np.array(img).astype(np.float64)
    a[..., :3] *= 0.8 - 0.25 * t                              # the colour going out of it
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    out = Image.new("RGBA", HEAD)
    mask = a[..., 3] > 0
    out.alpha_composite(img)
    if mask.sum() > 3:
        out.alpha_composite(head_foam(mask, 1.4 * i, 3530 + i, churn=0.8 + 0.4 * t, gain=1.0))
    f = dj.Field(*HEAD)                                       # the churn closing over it
    rng = np.random.default_rng(3540 + i)
    for _ in range(70):
        x = HEAD[0] / 2 + rng.normal(0, 30 * (1.2 - 0.5 * t))
        y = CROWN_PX[1] + 30 + rng.normal(0, 55 * (1.2 - 0.5 * t))
        r = rng.uniform(0.8, 2.6) * (1.2 - 0.6 * t)
        f.over(dj.FOAM, (np.hypot(f.x - x, f.y - y) <= r) * rng.uniform(0.4, 1.0) * (1.1 - 0.7 * t))
    w, h = HEAD
    yy, xx = np.mgrid[0:h, 0:w]
    th = np.arctan2(yy - CROWN_PX[1] - 30, xx - w / 2)
    rad = np.hypot((xx - w / 2) / 1.0, (yy - CROWN_PX[1] - 30) / 1.7)
    edge = (34 + 34 * t) * (1 + 0.18 * np.sin(3 * th + 1.3 * i) + 0.12 * np.sin(5 * th - 0.9 * i + 1)
                            + 0.07 * np.sin(9 * th + 2.1 * i))
    blob = (rad < edge).astype(float)                         # the churned water spreading over it
    dj.edge_foam(f, blob, 1.7 * i, 3550 + i, width=3.0 + 2 * t, gain=0.9 * (1 - 0.45 * t), specks=60, reach=12,
                 inner=4.0, centre=(w / 2, CROWN_PX[1] + 30))
    out.alpha_composite(f.image())
    return out


# =========================================================================== arms

def turned(scene, mats, h):
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def render_segment(job):
    kind, k, n = job
    model = bm.kraken_arm() if kind == "arm" else bm.kraken_tip()
    scene, mats = turned(*model, TAU * k / HEADINGS)
    return artkit.native(*artkit.render_hi(scene, mats, (n, n), ARM_EXTENT if kind == "arm" else TIP_EXTENT))


def heading_index(screen_angle):
    """A concept path heading (screen angle from +x, y down) -> the game's heading index."""
    return int(round(((screen_angle - np.pi / 2) % TAU) / TAU * HEADINGS)) % HEADINGS


def taper_index(size):
    return int(np.argmin([abs(size - s) for s in TAPERS]))


def foam_clump(job):
    v, i = job
    w, h = FOAM_SIZE
    t = i / (FOAM_FRAMES - 1)
    f = dj.Field(w, h)
    rng = np.random.default_rng(3560 + v)
    for _ in range(14):
        a, r0 = rng.uniform(0, TAU), rng.uniform(0, 3)
        sp = rng.uniform(3, 8)
        r = r0 + sp * fx.ease_out(t, 2.0)
        x, y = w / 2 + np.cos(a) * r, h / 2 + np.sin(a) * r * 0.8
        rad = rng.uniform(0.9, 2.2) * (1.0 + 0.6 * t) * (1 - 0.55 * t)
        life = np.clip(1.1 - t * rng.uniform(0.8, 1.3), 0, 1)
        f.over(dj.FOAM * (0.8 + 0.2 * life) + dj.FOAM_SHADE * 0.2 * (1 - life),
               (np.hypot(f.x - x, f.y - y) <= rad) * (0.5 + 0.5 * life) * life)
    return f.image()


class Art:
    """The segment frames, for compositing the baked arms."""

    def __init__(self, arm, tip):
        self.arm, self.tip = arm, tip

    def segment(self, size, screen_angle, tip=False):
        k = heading_index(screen_angle)
        if tip:
            return self.tip[k * len(TIPS) + (0 if size > 26 else 1)]
        return self.arm[k * len(TAPERS) + taper_index(size)]


def sway_pts(pts, w, amp, seed, start=0):
    out = []
    for i, (x, y) in enumerate(pts):
        a = amp * min(1.0, max(0, i - start) / 2.0)
        out.append((x + a * np.sin(w + seed + 0.9 * i), y + a * 0.7 * np.cos(w + seed * 1.3 + 0.7 * i)))
    return out


def grip_poses(j, slide=0.0):
    """The four gripping arms at sway frame j (loop phase), as the concept wraps them (kraken_r07
    arm_poses: under water from the roots to the deck's edge, then on the deck); ``slide`` 0..1
    pulls the deck parts back over the edge into the water (the death)."""
    w = TAU * j / SWAY_FRAMES
    root_e = kr.ROOT_H - DEEP_LEVEL
    poses = []
    for key, ctrl, ri, seed, out_dir in (("side-l", kr.SIDE_L, 7, 0.0, (-1, 0)), ("south-l", kr.SOUTH_L, 6, 1.7, (0, 1)),
                                         ("side-r", kr.mirror(kr.SIDE_L), 0, 3.1, (1, 0)),
                                         ("south-r", kr.mirror(kr.SOUTH_L), 1, 4.4, (0, 1))):
        under = sway_pts(ctrl[:4], w, 7, seed)
        deck = sway_pts(ctrl[4:], w, 2.5, seed + 2, start=1)
        if slide > 0:
            s = fx.ease_out(slide, 1.6)
            reach = 70 * s
            deck = [(x + out_dir[0] * reach * (0.6 + 0.4 * k / len(deck)), y + out_dir[1] * reach * (0.6 + 0.4 * k / len(deck)))
                    for k, (x, y) in enumerate(deck)]
            under = [(x + out_dir[0] * reach * 0.3, y + out_dir[1] * reach * 0.3 + 10 * s) for x, y in under]
        path = [kr.ROOTS[ri]] + under + deck
        ap = kr.sample_arm(path, kr.wrapped_e(root_e), key, 40, 14, edge_index=5)
        if slide > 0:                                         # what left the deck is in the water now
            ap.es = [e if on_deck(x, y) else min(e, -0.05) for (x, y), e in zip(ap.pts, ap.es)]
            ap.tip_e = ap.tip_e if on_deck(*ap.tip) else -0.05
        poses.append(ap)
    return poses


def on_deck(x, y):
    return kr.P_W + 2 <= x <= kr.P_E - 2 and kr.P_N + 2 <= y <= kr.P_S - 2


def items_of(ap):
    items = [(ap.pts[j], ap.heads[j], ap.sizes[j], ap.es[j], False) for j in range(len(ap.pts))]
    items.append((ap.tip, ap.tip_h, ap.sizes[-1] if ap.sizes else 16, ap.tip_e, True))
    return items


def compose_arms(art, poses, size, origin, above):
    """The arms' segments above (``above``) or below the water, tip first and root on top, on a
    canvas of ``size`` whose top left is ``origin`` in the concept's field; deck segments cast a
    soft shadow on the deck."""
    img = Image.new("RGBA", size)
    shadow = Image.new("L", size, 0)
    sd = ImageDraw.Draw(shadow)
    for ap in poses:
        for (x, y), hd, sz, e, tip in reversed(items_of(ap)):
            if (e > 0) != above:
                continue
            fr = art.segment(sz, hd, tip)
            sprite.paste_center(img, fr, x - origin[0], y - origin[1])
            if above and on_deck(x, y):
                r = sz * 0.32
                sd.ellipse([x - origin[0] + 3 - r, y - origin[1] + 4 - r, x - origin[0] + 3 + r, y - origin[1] + 4 + r],
                           fill=150)
    if above:
        sh = np.asarray(shadow.filter(ImageFilter.GaussianBlur(2.0)), float) / 255
        sh = np.floor(sh * 3 + 0.5) / 3 * 0.55
        layer = dj.stepped(np.zeros(sh.shape + (3,)) + np.array([6, 8, 22]), sh, 4)
        base = Image.new("RGBA", size)
        base.alpha_composite(layer)
        base.alpha_composite(img)
        img = base
    return img


def crossings(poses):
    """Where the arms break the surface: (x, y, size) per sign change of the water height."""
    out = []
    for ap in poses:
        it = items_of(ap)
        for a, b in zip(it, it[1:]):
            if a[3] * b[3] < 0:
                f = a[3] / (a[3] - b[3])
                out.append((a[0][0] + (b[0][0] - a[0][0]) * f, a[0][1] + (b[0][1] - a[0][1]) * f, a[2]))
    return out


def grip_frame(art, j, slide=0.0):
    origin = PLAT + GRIP_AT - np.array(GRIP) / 2
    poses = grip_poses(j, slide)
    img = compose_arms(art, poses, GRIP, origin, True)
    f = dj.Field(*GRIP)
    rng = np.random.default_rng(3570 + j)
    for x, y, sz in crossings(poses):
        cx, cy = x - origin[0], y - origin[1]
        for _ in range(9 + int(14 * slide)):
            a, r = rng.uniform(0, TAU), sz * 0.45 * np.sqrt(rng.uniform(0.2, 1.0)) + 2
            px, py = cx + np.cos(a + TAU * j / SWAY_FRAMES * 0.3) * r, cy + np.sin(a) * r
            f.over(dj.FOAM, (np.hypot(f.x - px, f.y - py) <= rng.uniform(0.9, 2.2)) * rng.uniform(0.55, 1.0))
    img.alpha_composite(f.image())
    return img


def grip_sub_frame(art, j):
    origin = PLAT + GRIP_SUB_AT - np.array(GRIP_SUB) / 2
    return compose_arms(art, grip_poses(j), GRIP_SUB, origin, False)


def idle_frame(art, j, side):
    """An idle arm trailing deep below, squirming: a dark shadow for the `sub` pass."""
    w = TAU * j / SWAY_FRAMES
    ctrl = kr.IDLE_L if side == "left" else kr.mirror(kr.IDLE_L)
    ri = 5 if side == "left" else 2
    path = [kr.ROOTS[ri]] + sway_pts(ctrl, w, 12, 0.5 if side == "left" else 2.5)
    ap = kr.sample_arm(path, lambda u, ue: -1.0, side, 36, 12)
    cx = (kr.ROOTS[ri][0] + ctrl[-1][0]) / 2
    origin = np.array([cx - IDLE[0] / 2, kr.ROOTS[ri][1] - 28])
    return halved(shadowed(compose_arms(art, [ap], IDLE, origin, False)))


def halved(img):
    """An unlit deep shadow at half size (the game draws it at 2x through the blurring `sub` pass)."""
    a = np.array(img.resize((img.width // HALF, img.height // HALF), Image.BOX)).astype(np.float64)
    a[..., 3] = np.round(a[..., 3] / 85) * 85                 # stepped: 1/3, 2/3
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def doubled(img):
    return img.resize((img.width * HALF, img.height * HALF), Image.NEAREST)


def shadowed(img, k=0.82, alpha=2 / 3):
    """A deep shadow: the shapes darkened toward the deep water, at a stepped translucency."""
    a = np.array(img).astype(np.float64)
    a[..., :3] = a[..., :3] * (1 - k) + DEEP_SHADE * k
    a[..., 3] = np.where(a[..., 3] > 0, 255 * alpha, 0)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def shadow_frame(art, sub_head, j):
    """The foreshadowing silhouette: the mantle crown first (up the sprite) with the eight arms
    streaming behind it, undulating."""
    w, h = SHADOW
    img = Image.new("RGBA", SHADOW)
    hx, hy = w / 2, 150
    poses = []
    for i, a in enumerate(np.linspace(-1.6, 1.6, 8)):
        rx, ry = kr._root(a)
        rx, ry = rx - kr.HX + hx, ry - kr.HY + hy
        spread = np.sin(a) * 0.5
        ctrl = [(rx, ry)]
        for k in range(1, 6):
            d = k * 40
            ctrl.append((rx - spread * d * 0.9 + 6 * np.sin(TAU * j / 2 + k * 1.1 + i), ry + d))
        poses.append(kr.sample_arm(ctrl, lambda u, ue: -1.0, f"s{i}", 32, 10))
    img.alpha_composite(compose_arms(art, poses, SHADOW, (0, 0), False))
    head = sub_head
    img.alpha_composite(head, (int(hx - head.width / 2), int(hy - head.height / 2)))
    return halved(shadowed(img, 0.86, 0.6))


# =========================================================================== deaths

def death_glow(i):
    """The `huge` death at the head: lime bursting from the eyes, crimson from the beak and the
    mantle's seam, spray glints flung out, a crimson haze; additive."""
    w, h = DEATH_SIZE
    t = fx.t_of(i, DEATH_FRAMES)
    c = w / 2
    cv = v8.Canvas(w, h)
    fade = np.clip((w / 2 - cv.dist(c, c)) / 6.0, 0, 1)
    ey = c + (0.52 + H_CY) * HPX - DEATH_AT[1]
    for ex in (c - 0.47 * HPX, c + 0.47 * HPX):
        d = cv.dist(ex, ey)
        cv.add((236, 255, 210), v8.gauss(d, 4 + 8 * t) * 1.3 * np.clip(1 - t * 2.4, 0, 1))
        cv.add(tuple(LIME), v8.gauss(d, 10 + 18 * fx.ease_out(t)) * 0.5 * (1 - t) ** 1.6 * fade)
    beak = cv.dist(c, c + (1.04 + H_CY) * HPX - DEATH_AT[1])
    cv.add((255, 220, 200), v8.gauss(beak, 3 + 6 * t) * 1.2 * np.clip(1 - t * 2.6, 0, 1))
    cv.add(tuple(CRIMSON), v8.gauss(beak, 12 + 22 * fx.ease_out(t)) * 0.55 * (1 - t) ** 1.4 * fade)
    seam = cv.seg(c, c + (-1.25 + H_CY) * HPX - DEATH_AT[1], c, c + (0.15 + H_CY) * HPX - DEATH_AT[1])
    cv.add(tuple(CRIMSON), v8.gauss(seam, 3 + 6 * t) * 0.6 * np.clip(1 - t * 1.6, 0, 1) * fade)
    tex = v8._cart_noise(w, 3581, cell=w * v8.SS // 6)
    haze = np.clip(v8.gauss(cv.dist(c, c), 30 + 50 * fx.ease_out(t, 2.2)) * (0.5 + 0.8 * tex) - 0.3 - 0.5 * t, 0, 1)
    cv.add((90, 20, 16), haze * 0.9 * (1 - t) * fade)
    vd.motes(cv, np.random.default_rng(3582), c, t, 60, (30, 90), (0.6, 1.3), (190, 225, 255), (240, 250, 255), fade,
             delay=0.15, gain=1.0)
    return artkit.additive(cv.image())


def arm_death(i):
    w, h = ARM_DEATH
    t = fx.t_of(i, ARM_DEATH_FRAMES)
    c = w / 2
    cv = v8.Canvas(w, h)
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 3.0, 0, 1)
    cv.add((255, 210, 190), v8.gauss(d, 2.5 + 4 * t) * 1.3 * np.clip(1 - t * 2.6, 0, 1))
    cv.add(tuple(CRIMSON), v8.gauss(d, 6 + 9 * fx.ease_out(t)) * 0.6 * (1 - t) ** 1.5 * fade)
    vd.motes(cv, np.random.default_rng(3590), c, t, 14, (8, 20), (0.5, 0.9), tuple(CRIMSON), (255, 225, 210), fade)
    return artkit.additive(cv.image())


# =========================================================================== build

def _render(job):
    kind = job[0]
    if kind in ("arm", "tip"):
        return render_segment(job)
    if kind == "head":
        return head_hi(job[1])
    if kind == "z":
        return head_z()
    if kind == "foam":
        return foam_clump(job[1])
    if kind == "death":
        return death_glow(job[1])
    if kind == "arm-death":
        return arm_death(job[1])
    raise ValueError(kind)


def _compose(job):
    kind, i = job
    z = head_z()
    if kind == "surface":
        return surface_frame(i, surface_levels(), head_hi(("shut", 0.0)), z)
    if kind == "sink":
        return sink_frame(i, z)
    if kind == "up":
        return up_frame(EYES[i], z)
    raise ValueError(kind)


def build():
    heads = [(e, b) for e, b in EYES]
    jobs = ([("z",)] + [("head", k) for k in heads]
            + [("arm", k, n) for k in range(HEADINGS) for n in TAPERS]
            + [("tip", k, n) for k in range(HEADINGS) for n in TIPS]
            + [("foam", (v, i)) for v in range(FOAM_VARIANTS) for i in range(FOAM_FRAMES)]
            + [("death", i) for i in range(DEATH_FRAMES)] + [("arm-death", i) for i in range(ARM_DEATH_FRAMES)])
    with ProcessPoolExecutor() as pool:
        out = list(pool.map(_render, jobs))
        at = 1 + len(heads)
        n_arm, n_tip = HEADINGS * len(TAPERS), HEADINGS * len(TIPS)
        arm, tip = out[at:at + n_arm], out[at + n_arm:at + n_arm + n_tip]
        at += n_arm + n_tip
        foam = out[at:at + FOAM_VARIANTS * FOAM_FRAMES]
        at += FOAM_VARIANTS * FOAM_FRAMES
        death, arm_d = out[at:at + DEATH_FRAMES], out[at + DEATH_FRAMES:]
        comp = list(pool.map(_compose, [("up", i) for i in range(len(EYES))]
                             + [("surface", i) for i in range(SURFACE_FRAMES)]
                             + [("sink", i) for i in range(SINK_FRAMES)]))
    z = head_z()
    sub = artkit.native(head_hi(("shut", 0.0)), FACTOR, crisp=70)
    up, surface, sink = comp[:len(EYES)], comp[len(EYES):len(EYES) + SURFACE_FRAMES], comp[len(EYES) + SURFACE_FRAMES:]
    up_mask = np.array(up[0])[..., 3] > 0
    collar = [collar_frame(j, up_mask) for j in range(COLLAR_FRAMES)]
    # one palette over the head, the arms and the baked arms, so the parts match
    seg = artkit.quantize_set(arm + tip, COLOURS)
    art = Art(seg[:len(arm)], seg[len(arm):])
    grip = [grip_frame(art, j) for j in range(SWAY_FRAMES)]
    grip_sub = [grip_sub_frame(art, j) for j in range(SWAY_FRAMES)]
    slide = [grip_frame(art, (j * 2) % SWAY_FRAMES, (j + 1) / (SLIDE_FRAMES + 1)) for j in range(SLIDE_FRAMES)]
    idle_l = [idle_frame(art, j, "left") for j in range(SWAY_FRAMES)]
    idle_r = [idle_frame(art, j, "right") for j in range(SWAY_FRAMES)]
    body = artkit.quantize_set([sub] + up + surface + sink + grip + grip_sub + slide, COLOURS)
    at = 0

    def take(seq, n):
        nonlocal at
        part = seq[at:at + n]
        at += n
        return part
    sets = [(f"{SLUG}-sub", take(body, 1)), (SLUG, take(body, len(EYES))),
            (f"{SLUG}-surface", take(body, SURFACE_FRAMES)), (f"{SLUG}-sink", take(body, SINK_FRAMES)),
            (f"{SLUG}-grip", take(body, SWAY_FRAMES)), (f"{SLUG}-grip-sub", take(body, SWAY_FRAMES)),
            (f"{SLUG}-grip-slide", take(body, SLIDE_FRAMES)),
            (f"{SLUG}-collar", artkit.quantize_set(collar, 8)),
            (f"{SLUG}-arm", art.arm), (f"{SLUG}-tip", art.tip),
            (f"{SLUG}-foam", artkit.quantize_set(foam, 8)),
            (f"{SLUG}-idle-left", artkit.quantize_set(idle_l, 16)), (f"{SLUG}-idle-right", artkit.quantize_set(idle_r, 16)),
            (f"{SLUG}-death", artkit.quantize_set(death, 32)), (f"{SLUG}-arm-death", artkit.quantize_set(arm_d, 16))]
    shadow = [shadow_frame(art, sub, j) for j in range(2)]
    sets.append((f"{SLUG}-shadow", artkit.quantize_set(shadow, 16)))
    for name, frames in sets:
        artkit.write_frames(name, frames, SOURCE, single=name.endswith("-sub") and name.count("-") == 2)
        print(f"{name}: {len(frames)} frames, {frames[0].width}x{frames[0].height}")
    write_pivots()


def rel(xy):
    """A point of the concept's field relative to the platform's centre, rounded."""
    return [round(float(xy[0] - PLAT[0]), 1), round(float(xy[1] - PLAT[1]), 1)]


def write_pivots():
    head = HEAD_AT
    slam = {}
    for side, ri, rest in (("left", 4, kr.SLAM_REST_L), ("right", 3, kr.mirror(kr.SLAM_REST_L))):
        x, y = kr.ROOTS[ri]
        dx, dy = rest[0][0] - x, rest[0][1] - y
        slam[side] = {"root": rel((x, y)), "rest_heading": heading_index(np.arctan2(dy, dx)),
                      "rest_path": [rel(p) for p in rest], "lanes": [1, 2] if side == "left" else [3, 4]}
    artkit.write_pivots(SLUG, SOURCE, {
        "origin": "px relative to Platform Tiamat's centre (the backdrop piece; deck edges x -104..104, y -76..76)",
        "head": rel(head), "head_size": list(HEAD),
        "eyes": [rel(head + np.array([s * 0.47 * HPX, (0.52 + H_CY) * HPX])) for s in (-1, 1)],
        "eye_radius": round(0.16 * HPX, 1),
        "beak": rel(head + np.array([0.0, (1.04 + H_CY) * HPX])),
        "crown": rel(head + np.array([0.0, (-0.5 + H_CY) * HPX])),
        "death": rel(head + np.array(DEATH_AT)),
        "grip": [float(v) for v in GRIP_AT], "grip_size": list(GRIP), "grip_sub": [float(v) for v in GRIP_SUB_AT],
        "grip_sub_size": list(GRIP_SUB),
        "idle_left": rel(idle_centre("left")), "idle_right": rel(idle_centre("right")), "idle_size": [v // HALF for v in IDLE], "shadow_size": [v // HALF for v in SHADOW],
        "shadow_scale": HALF,
        "slam_arms": slam,
        "headings": HEADINGS, "tapers": TAPERS, "tips": TIPS,
        "arm_index": "heading * tapers + taper (taper 0 = 42 px at the root); tip: heading * 2 + (0: 34 px, 1: 22 px)",
        "segment_spacing": "0.5 x the segment's size (the concept's), sizes 42 at the root to 14 at the tip",
        "steps": {"surface": SURFACE_STEPS, "sink": SINK_STEPS, "collar": COLLAR_STEPS, "sway": SWAY_STEPS,
                  "slide": SLIDE_STEPS, "death": DEATH_STEPS, "arm_death": 4, "foam": 6, "shadow": 15},
        "surface_flip_frame": SURFACE_FRAMES // 2,
        "up_frames": ["eyes shut", "eyes half open", "eyes open", "beak glow half", "beak glow full"]})


def idle_centre(side):
    ctrl = kr.IDLE_L if side == "left" else kr.mirror(kr.IDLE_L)
    ri = 5 if side == "left" else 2
    cx = (kr.ROOTS[ri][0] + ctrl[-1][0]) / 2
    return np.array([cx, kr.ROOTS[ri][1] - 28 + IDLE[1] / 2])


# =========================================================================== review

def put(cell, img, x, y):
    cell.alpha_composite(img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))
    return cell


def add(cell, img, x, y):
    return artkit.add_light(cell, img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))


def platform_stand_in():
    """The review's stand-in for Level 11's Platform Tiamat (the concept's platform model,
    render/boss_models.platform at 100 px per unit, as kraken_r07 drew it)."""
    import bosses_r06 as b6
    return b6.static_sprite(bm.platform, (230, 172), 2.3, factor=2, colors=40)


def load():
    names = ["sub", "", "surface", "sink", "grip", "grip-sub", "grip-slide", "collar", "arm", "tip", "foam",
             "idle-left", "idle-right", "death", "arm-death", "shadow"]
    return {n or "up": artkit.load_frames(f"{SLUG}-{n}" if n else SLUG) for n in names}


def head_state(t):
    """(kind, frame) of the head at concept time t: deep, surfacing/diving frame, or up."""
    L = kr.head_level(t)
    if L >= DEEP_LEVEL - 0.01:
        return "deep", 0
    if L <= UP_LEVEL + 0.01:
        return "up", 0
    levels = surface_levels()
    i = int(np.argmin([abs(L - lv) for lv in levels]))
    return "swap", i


def draw_scene(art, plat, t, sea, death=None):
    """The fight as the game would draw it, at concept time t (kraken_r07's choreography)."""
    cell = sea.copy()
    sub = Image.new("RGBA", cell.size)
    tm = t % kr.T
    j = int(t * STEP / SWAY_STEPS) % SWAY_FRAMES
    a = Art(art["arm"], art["tip"])
    for side in ("left", "right"):
        put(sub, doubled(art[f"idle-{side}"][j]), *(PLAT + np.array(rel(idle_centre(side)))))
    gs = art["grip-sub"][j]
    if death is not None:
        gs = fade(gs, 1 - min(death / 1.0, 1))
    put(sub, gs, *(PLAT + GRIP_SUB_AT))
    kind, fi = head_state(t) if death is None else ("dead", 0)
    if death is None or death < 1.6:
        put(sub, art["sub"][0] if death is None else fade(art["sub"][0], 1 - death / 1.6), *HEAD_AT)
    poses = [p for p in kr.arm_poses(t) if p.key.startswith("slam")] if death is None else []
    for ap in poses:
        for (x, y), hd, sz, e, tip in reversed(items_of(ap)):
            if e <= 0:
                put(sub, a.segment(sz, hd, tip), x, y)
    cell.alpha_composite(dj.sub_pass(sub, t, k=0.5, blur=0.9, sway=0.8))
    put(cell, plat, *PLAT)
    if death is None:
        put(cell, art["grip"][j], *(PLAT + GRIP_AT))
    else:
        k = min(int(death * STEP / SLIDE_STEPS), SLIDE_FRAMES - 1)
        put(cell, art["grip-slide"][k], *(PLAT + GRIP_AT))
    if kind == "swap":
        put(cell, art["surface"][fi], *HEAD_AT)
    elif kind == "up":
        eyes = kr.EYES_OPEN[0] <= tm < kr.EYES_OPEN[1]
        beak = 0
        for tf in kr.FANS:
            if tf - 0.5 <= tm < tf:
                beak = 1 if tm < tf - 0.25 else 2
        fr = 0 if not eyes else (1 if tm < kr.EYES_OPEN[0] + 0.15 else 2 + beak)
        put(cell, art["up"][fr], *HEAD_AT)
        put(cell, art["collar"][int(t * STEP / COLLAR_STEPS) % COLLAR_FRAMES], *HEAD_AT)
    elif kind == "dead":
        k = int(death * STEP / SINK_STEPS)
        if k < SINK_FRAMES:
            put(cell, art["sink"][k], *HEAD_AT)
    for ap in poses:
        items = items_of(ap)
        for (x, y), hd, sz, e, tip in reversed(items):
            if e > 0:
                put(cell, a.segment(sz, hd, tip), x, y)
        for n, (p, q) in enumerate(zip(items, items[1:])):
            if p[3] * q[3] < 0 or (abs(p[3]) < 0.06 and n % 2 == 0):
                put(cell, art["foam"][(n % 2) * FOAM_FRAMES + int(t * 10 + n) % FOAM_FRAMES], *p[0])
    if death is not None:
        k = int(death * STEP / DEATH_STEPS)
        if k < DEATH_FRAMES:
            cell = add(cell, art["death"][k], HEAD_AT[0] + DEATH_AT[0], HEAD_AT[1] + DEATH_AT[1])
    return cell


def fade(img, k):
    a = np.array(img).astype(np.float64)
    a[..., 3] *= max(0.0, k)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    art = load()
    plat = platform_stand_in()
    sea = dj.ocean_plate(kr.FW, kr.FH, seed=121)

    def on_sea(img, size, layers=()):
        cell = dj.ocean_plate(*size, seed=121, x0=0, y0=0)
        for im, kind in layers:
            if kind == "sub":
                put(cell, dj.sub_pass(im, 0.0, k=0.5, blur=0.9), size[0] / 2, size[1] / 2)
            else:
                put(cell, im, size[0] / 2, size[1] / 2)
        if img is not None:
            put(cell, img, size[0] / 2, size[1] / 2)
        return cell
    surf_drawn = [on_sea(s, HEAD, [(art["sub"][0], "sub")]) for s in art["surface"]]
    up_drawn = [on_sea(None, HEAD, [(art["sub"][0], "sub"), (u, ""), (art["collar"][0], "")]) for u in art["up"]]
    sink_drawn = [on_sea(None, HEAD, [(fade(art["sub"][0], 1 - (i + 1) / SINK_FRAMES), "sub"), (s, "")])
                  for i, s in enumerate(art["sink"])]
    taper_row = [art["arm"][t] for t in range(len(TAPERS))] + [art["tip"][0], art["tip"][1]]
    plat_cell = dj.ocean_plate(320, 300, seed=121)
    put(plat_cell, dj.sub_pass(art["grip-sub"][0], 0.0, k=0.5, blur=0.9), 160, 70 + GRIP_SUB_AT[1])
    put(plat_cell, plat, 160, 70)
    put(plat_cell, art["grip"][0], 160, 70 + GRIP_AT[1])
    scene_stills = [draw_scene(art, plat, t, sea).resize((240, 270), Image.BOX) for t in (1.6, 3.6, 5.4, 6.0)]
    sheet = artkit.review_sheet("HARBOUR KRAKEN - FINAL SPRITES (PROPOSAL)", [
        ("IN PLAY (0.5X) ON THE SEA AND PLATFORM STAND-INS: SLAM, CROWN BREAKING, EYES OPEN, BEAK GLOW", scene_stills, 1,
         False),
        ("HEAD FOR THE SUB PASS / SURFACED: SHUT, HALF, OPEN, BEAK HALF, BEAK FULL / COLLAR", art["sub"] + art["up"]
         + art["collar"][:1], 1, False),
        ("SURFACING, SURFACE LAYER, 12 FRAMES X 10 STEPS (DIVING: REVERSED)", art["surface"][:7], 1, False),
        ("SURFACING AS DRAWN OVER THE SUB-PASS HEAD (FRAMES 0, 2, 4 ... 11)", surf_drawn[::2] + surf_drawn[-1:], 1, False),
        ("SURFACED AS DRAWN: SUB HEAD + UP FRAME + COLLAR", up_drawn, 1, False),
        ("SLAM-ARM SEGMENTS: 7 TAPERS AND 2 TIPS AT HEADING 0", taper_row, 2, False),
        ("SEGMENT 42 PX, EVERY 2ND HEADING", [art["arm"][k * len(TAPERS)] for k in range(0, HEADINGS, 2)], 1, False),
        ("FOAM CLUMPS, 2 VARIANTS X 8 FRAMES", art["foam"], 2, False),
        ("GRIP OVERLAY (4 SWAY FRAMES) / AS DRAWN ON THE PLATFORM STAND-IN WITH THE SUB-PASS STRETCH",
         art["grip"][:2] + [plat_cell], 1, False),
        ("GRIP UNDER-WATER STRETCH (PLAIN, FOR THE SUB PASS) / IDLE SHADOWS", art["grip-sub"][:1] + art["idle-left"][:2]
         + art["idle-right"][:2], 1, False),
        ("FORESHADOW SHADOW, 2 FRAMES (HALF SIZE, DRAWN AT 2X)", art["shadow"], 2, False),
        ("DEATH: HEAD SINKING IN A CHURNING RING, AS DRAWN", sink_drawn[::2], 1, False),
        ("DEATH: GRIP SLIDING OFF, 5 FRAMES X 12 STEPS", art["grip-slide"], 1, False),
        ("DEATH BURST (ADDITIVE) / SEVERED ARM SEGMENT POP (ADDITIVE)", art["death"][::2] + art["arm-death"], 1, True)],
        width=1500, batch=BATCH)
    gif = []
    for f in range(int(kr.T * 15)):
        gif.append(draw_scene(art, plat, f / 15, sea))
    for f in range(int(2.6 * 15)):
        gif.append(draw_scene(art, plat, kr.T, sea, death=f / 15))
    artkit.save_review(sheet, gif, CONCEPT, SLUG, fps=15)


# =========================================================================== round 40
# The L11 rework (playtest 2026-10-18, pt2-b.md proposal 3a): four one-lane arms, each rooted in a
# fleshy socket breaking the surface in its lane, and the whole animal 124 px up the screen. The
# round-33 registration is kept: the head stays 216 px below the platform's centre, so the head, the
# grip and its under-water stretch, the surfacing, the death and the segment frames are reused as
# they are; the platform itself moves up (the halt puts its centre at y -46 instead of 78). New: the
# four root sockets (intact, hit, raised = rearing with the glowing tell, cut = a severed stump
# oozing, sinking at the death), the tell's additive glow and the four arms' under-water reach from
# the mantle's rim to their sockets. Proposals for round 40 (written beside the chosen set).

R40_TAG = "r40-a"
R40_ROUND = "r40"
R40_BATCH = "round 40 batch"
R40_SOURCE = artkit.source_note(SCRIPT, "round 40 batch, L11 rework proposal a")
R40_ANCHOR = -46.0                              # the platform's centre at the halt (screen y; round 33: 78)
LANES = (1, 2, 3, 4)
# The sockets (the 40 x 40 root targets' centres), px relative to the platform's centre: y 256 is
# screen 210 at the halt. Lanes 1 and 4 sit on their lane centres (x 60 / 420); lanes 2 and 3 sit
# 26 px outward of theirs (x 154 / 326) so their boxes clear the mantle's flanks and the eyes.
SOCKETS = {1: (-180.0, 256.0), 2: (-86.0, 256.0), 3: (86.0, 256.0), 4: (180.0, 256.0)}
LANE_X = {1: -180.0, 2: -60.0, 3: 60.0, 4: 180.0}
LANE_TOP = 290.0                                # where an arm reaches its lane's centre line (screen 244)
MANTLE_ROOT = {1: 5, 2: 4, 3: 3, 4: 2}          # the concept's root ring index an arm leaves the mantle at
ROOT_SIZE = (72, 96)
ROOT_PX = TAPERS[0] / ARM_EXTENT                # 26.25 px per unit: the 42 px segment's scale
ROOT_EXT = ROOT_SIZE[0] / ROOT_PX
ROOT_FACTOR = 4
ROOT_TARGET = (40, 40)
INTACT_FRAMES, RAISED_FRAMES, CUT_FRAMES, RSINK_FRAMES, GLOW_FRAMES = 4, 6, 6, 5, 4
RAISED_APEX = [0.55, 0.8, 1.02, 1.2, 1.2, 1.16]
RAISED_LASER = [0.2, 0.45, 0.75, 1.0, 1.0, 0.72]
R40_STEPS = {"root": 12, "raised": 9, "raised_hold": 8, "cut": 8, "ooze": 10, "root_sink": 12, "glow": 6, "reach": 12}
GLOW_SIZE = (64, 64)
CAMERA = 7.0                                    # the mild perspective that makes the rearing read
REST_SIGN = {1: -1, 2: 1, 3: -1, 4: 1}
OOZE = np.array([74, 10, 18], float)
OOZE_HI = np.array([150, 28, 34], float)


def lane_side(lane):
    """+1 for the left sockets (their arm comes in from the head, on the right), -1 for the right."""
    return 1.0 if lane <= 2 else -1.0


def socket_ctrl(lane, apex, sway=0.0, cut=False):
    """The arm's base as 3-D control points (units; x right, y down the screen, z up): up out of the
    water inside its socket's collar, arching (the apex near the socket's centre) and on down into
    its lane at a shallow angle; a cut arm ends just past the arch."""
    sx = lane_side(lane)
    ox = sx * 0.28 if lane in (2, 3) else 0.0            # lanes 2/3 head inward to the lane's centre
    lift = max(0.0, apex - 0.5)                          # rearing: the arm's run lifts off the water
    pts = [(0.0, -0.12, -0.5), (0.0, -0.02, apex * 0.85), (0.02 * sx + sway * 0.03, 0.22, apex)]
    if cut:
        return pts + [(ox * 0.25, 0.36, apex * 1.2 + 0.1)]
    return pts + [(ox * 0.5 + sway * 0.08, 0.55, apex * 0.55 + 0.6 * lift), (ox + sway * 0.2, 0.88, -0.2 + 0.9 * lift),
                  (ox * 1.4 + sway * 0.35, 1.2, -0.9 + 1.0 * lift)]


def root_model(lane, apex, laser=0.0, sway=0.0, cut=False, glowing=False):
    """(scene, mats) of a root socket: a dark webbed collar ringed with pale suckers where the arm
    breaks the surface, the mottled arm rearing out of it and running on down its lane, suckers
    along its flanks and the crimson dorsal seam on its back (an emitter while raised); a cut arm
    ends in a raw crimson wound."""
    ctrl = np.array(socket_ctrl(lane, apex, sway, cut))
    pts = kr.catmull(ctrl, per=6)
    n = len(pts)
    u = np.arange(n) / (n - 1)
    r_end = 0.42 if cut else 0.3
    rad = 0.5 + (r_end - 0.5) * u ** 1.1
    model = pts * np.array([1.0, -1.0, 1.0])              # model y up the screen
    tang = np.gradient(model[:, :2], axis=0)
    tang /= np.maximum(np.linalg.norm(tang, axis=1, keepdims=True), 1e-9)
    side = np.stack([-tang[:, 1], tang[:, 0]], axis=1)
    seam = bm.V_LASER if glowing else bm.V_SEAM
    ring_r, ring_w = 0.6, 0.15
    ring = [(ring_r * np.cos(a), ring_r * 0.92 * np.sin(a) + 0.05) for a in np.linspace(0, TAU, 12, endpoint=False)]

    def scene(p):
        body = [(sdf.sd_capsule(p, tuple(model[i]), tuple(model[i + 1]), rad[i], rad[i + 1]), bm.V_BODY)
                for i in range(n - 1)]
        d, m = union(*body)
        q = np.stack([np.hypot(p[:, 0], (p[:, 1] - 0.05) / 0.92) - ring_r, p[:, 2] + 0.02], axis=-1)
        collar = np.sqrt(np.sum(q * q, axis=-1)) - ring_w
        web = sd_ellipsoid(p, (0.0, 0.05, -0.12), (ring_r + 0.1, ring_r * 0.92 + 0.1, 0.16))
        d, m = union((d, m), (np.minimum(collar, web), V_DARK), k=0.08)
        extras = [(sdf.sd_sphere(p, (x, y, 0.1), 0.085), bm.V_BONE) for x, y in ring]
        for i in range(int(0.3 * n), n - 2, 2):
            for s in (-1, 1):
                c = model[i, :2] + side[i] * s * rad[i] * 0.62
                extras.append((sdf.sd_sphere(p, (c[0], c[1], model[i, 2] + 0.72 * rad[i]), 0.075), bm.V_BONE))
        top = list(range(int(0.25 * n), n if cut else n - 1))
        for a, b in zip(top, top[1:]):
            extras.append((sdf.sd_capsule(p, (model[a, 0], model[a, 1], model[a, 2] + rad[a]),
                                          (model[b, 0], model[b, 1], model[b, 2] + rad[b]), 0.085), seam))
        if cut:
            e, t = model[-1], model[-1] - model[-2]
            t = t / np.linalg.norm(t)
            extras.append((sdf.sd_sphere(p, tuple(e + t * 0.06), r_end * 0.72), bm.V_LASER))
        return union((d, m), *extras, k=0.025)
    return artkit.perspective(scene, CAMERA), bm.mats("harbour-kraken", 1.0, laser=laser, body_pattern=bm.mottled())


def root_render(job):
    """One socket render at 4x and its height map (the ray-marcher's grid), cached outside the repo."""
    lane, apex, laser, sway, cut, glowing = job
    CACHE.mkdir(parents=True, exist_ok=True)
    path = CACHE / f"r40-root-{lane}-{apex:.3f}-{laser:.2f}-{sway:.2f}-{int(cut)}-{int(glowing)}.npz"
    if path.exists():
        f = np.load(path)
        return f["hi"], f["z"]
    scene, mats = root_model(lane, apex, laser, sway, cut, glowing)
    size = (ROOT_SIZE[0] * ROOT_FACTOR, ROOT_SIZE[1] * ROOT_FACTOR)
    hi = sdf.render(scene, mats, size, ROOT_EXT)
    z = kr.heightmap(scene, size, ROOT_EXT, (0.0, 0.0))
    np.savez_compressed(path, hi=hi, z=z)
    return hi, z


def root_cut(hi, z, level=0.0, dim=1.0):
    """What of a socket render is above the water plane -> native sprite and its 0/1 mask."""
    keep = (hi[..., 3] > 0) & (np.nan_to_num(z, nan=-9) > level)
    h = hi.copy()
    h[..., 3] = np.where(keep, hi[..., 3], 0)
    if dim != 1.0:
        h[..., :3] *= dim
    img = artkit.native(h, ROOT_FACTOR, crisp=70)
    return img, (np.array(img)[..., 3] > 0).astype(float)


def water_shadow(mask, dx, dy, alpha):
    """A raised knot's soft shadow on the water, stepped."""
    sh = Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.2))
    a = np.roll(np.roll(np.asarray(sh, float) / 255, int(round(dy)), 0), int(round(dx)), 1)
    a = np.floor(a * 3 + 0.5) / 3 * alpha * (mask < 0.5)
    out = Image.new("RGBA", ROOT_SIZE)
    out.alpha_composite(dj.stepped(np.zeros(a.shape + (3,)) + np.array([4, 10, 24]), a, 4))
    return out


def ooze(lane, j, k=1.0):
    """The severed stump's ichor: a dark crimson slick spreading from the wound down the water,
    glints on it, drops beading off; a 4-frame loop (``k`` < 1: still spreading)."""
    w, h = ROOT_SIZE
    f = dj.Field(w, h)
    sx = lane_side(lane)
    cx, cy = w / 2 + (0.3 * sx if lane in (2, 3) else 0) * ROOT_PX * 0.25, h / 2 + 14
    ph = TAU * j / CUT_FRAMES
    th = np.arctan2(f.y - cy, f.x - cx)
    rr = np.hypot((f.x - cx) / 1.0, (f.y - cy) / (1.35 + 0.15 * np.sin(ph)))
    edge = (16 * k) * (1 + 0.16 * np.sin(3 * th + ph) + 0.1 * np.sin(5 * th - 2 * ph + 1) + 0.06 * np.sin(8 * th + ph))
    film = np.clip((edge - rr) / 2.0, 0, 1)
    f.over(OOZE, film * 0.9)
    glint = np.clip(np.sin(f.x * 0.9 + 1.7 * np.sin(f.y * 0.4 + ph)) * np.sin(f.y * 0.7 - ph), 0, 1) ** 6
    f.over(OOZE_HI, film * glint * 0.9)
    rng = np.random.default_rng(4010 + lane)
    for _ in range(7):
        a, r0 = rng.uniform(0.2, np.pi - 0.2), rng.uniform(6, 16) * k
        age = (j / CUT_FRAMES + rng.uniform(0, 1)) % 1.0
        x, y = cx + np.cos(a) * r0 * 0.6, cy + np.sin(a) * (r0 + 6 * age)
        f.over(OOZE_HI * (1 - age) + OOZE * age, (np.hypot(f.x - x, f.y - y) <= 1.1 - 0.4 * age) * (1 - 0.6 * age))
    return f.image()


def socket_frame(hi_z, lane, phase, seed, shadow=0.0, oozing=None, level=0.0, dim=1.0, churn=0.0):
    hi, z = hi_z
    img, mask = root_cut(hi, z, level, dim)
    out = Image.new("RGBA", ROOT_SIZE)
    if oozing is not None:
        out.alpha_composite(ooze(lane, *oozing))
    if shadow > 0:
        out.alpha_composite(water_shadow(mask, 2 + 3 * shadow, 3 + 4 * shadow, 0.5))
    out.alpha_composite(img)
    if mask.sum() > 3:
        f = dj.Field(*ROOT_SIZE)
        dj.edge_foam(f, mask, phase, seed, width=2.0 + 2 * churn, gain=0.9, specks=20 + int(30 * churn),
                     reach=6 + 6 * churn, inner=1.0 + churn)
        out.alpha_composite(f.image())
    return out


def tinted(img, k=0.3, tint=(255, 224, 209)):
    """The hit flash (as the game's PART_FLASH: 0.3 toward a pale flesh tint)."""
    a = np.array(img).astype(np.float64)
    on = a[..., 3:4] > 0
    a[..., :3] = np.where(on, a[..., :3] * (1 - k) + np.array(tint) * k, a[..., :3])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def root_glow(i):
    """The raised tell's glow round the socket (additive, centred on it): a crimson halo pulsing,
    a hot core over the knot."""
    w, h = GLOW_SIZE
    k = [0.7, 1.0, 0.82, 0.58][i]
    cv = v8.Canvas(w, h)
    c = w / 2
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 5.0, 0, 1)
    cv.add(tuple(CRIMSON), v8.gauss(d, 12 + 4 * k) * 0.55 * k * fade)
    cv.add((255, 170, 150), v8.gauss(d, 4 + 2 * k) * 0.45 * k)
    tex = v8._cart_noise(w, 4020 + i, cell=w * v8.SS // 5)
    cv.add((120, 18, 16), np.clip(v8.gauss(d, 20) * (0.4 + 0.8 * tex) - 0.25, 0, 1) * 0.6 * k * fade)
    return artkit.additive(cv.image())


def root_jobs():
    """Every socket render the round-40 set needs, keyed (lane, state, frame)."""
    jobs = {}
    for lane in LANES:
        for j in range(INTACT_FRAMES):
            jobs[(lane, "intact", j)] = (lane, round(0.39 + 0.03 * np.sin(TAU * j / INTACT_FRAMES), 3), 0.0,
                                         round(float(np.sin(TAU * j / INTACT_FRAMES)) * 0.4, 2), False, False)
        for j in range(RAISED_FRAMES):
            jobs[(lane, "raised", j)] = (lane, RAISED_APEX[j], RAISED_LASER[j], 0.0, False, True)
        for j, (apex, laser) in enumerate([(0.95, 0.35), (0.6, 0.25)] + [(0.3 + 0.02 * np.cos(TAU * i / 4), 0.15)
                                                                       for i in range(CUT_FRAMES - 2)]):
            jobs[(lane, "cut", j)] = (lane, round(float(apex), 3), laser, 0.0, True, False)
    return jobs


def build_r40():
    jobs = root_jobs()
    keys = list(jobs)
    with ProcessPoolExecutor() as pool:
        renders = dict(zip(keys, pool.map(root_render, [jobs[k] for k in keys])))
    sets = {"intact": [], "raised": [], "hit": [], "cut": [], "sink": []}
    for lane in LANES:
        seed = 4100 + 10 * lane
        for j in range(INTACT_FRAMES):
            sets["intact"].append(socket_frame(renders[(lane, "intact", j)], lane, TAU * j / INTACT_FRAMES, seed))
        for j in range(RAISED_FRAMES):
            sets["raised"].append(socket_frame(renders[(lane, "raised", j)], lane, 0.7 * j, seed + 1,
                                               shadow=RAISED_APEX[j] - 0.4, churn=0.5))
        sets["hit"].append(tinted(sets["intact"][-INTACT_FRAMES]))
        for j in range(CUT_FRAMES):
            spread = (0.45, 0.75)[j] if j < 2 else 1.0
            sets["cut"].append(socket_frame(renders[(lane, "cut", j)], lane, 0.9 * j, seed + 2,
                                            oozing=(max(0, j - 2), spread), churn=0.6 if j < 2 else 0.2))
        for j in range(RSINK_FRAMES):
            t = (j + 1) / RSINK_FRAMES
            sets["sink"].append(socket_frame(renders[(lane, "intact", 0)], lane, 1.1 * j, seed + 3,
                                             level=0.12 + 0.62 * t ** 1.2, dim=0.85 - 0.25 * t, churn=0.5 + 0.5 * t))
    body = artkit.quantize_set(sets["intact"] + sets["raised"] + sets["cut"] + sets["sink"], COLOURS)
    at = 0
    out = {"hit": artkit.quantize_set(sets["hit"], 32)}      # its own palette: the pale tint keeps its shades
    for name in ("intact", "raised", "cut", "sink"):
        out[name] = body[at:at + len(sets[name])]
        at += len(sets[name])
    art = Art(artkit.load_frames(f"{SLUG}-arm"), artkit.load_frames(f"{SLUG}-tip"))
    reach_frames, centre, size = reach_set(art)
    names = [(f"{SLUG}-root-{R40_TAG}", out["intact"]), (f"{SLUG}-root-raised-{R40_TAG}", out["raised"]),
             (f"{SLUG}-root-hit-{R40_TAG}", out["hit"]), (f"{SLUG}-root-cut-{R40_TAG}", out["cut"]),
             (f"{SLUG}-root-sink-{R40_TAG}", out["sink"]),
             (f"{SLUG}-root-glow-{R40_TAG}", artkit.quantize_set([root_glow(i) for i in range(GLOW_FRAMES)], 24)),
             (f"{SLUG}-reach-{R40_TAG}", artkit.quantize_set(reach_frames, COLOURS))]
    for name, frames in names:
        artkit.write_frames(name, frames, R40_SOURCE)
        print(f"{name}: {len(frames)} frames, {frames[0].width}x{frames[0].height}")
    write_r40_pivots(centre, size)


# --------------------------------------------------------------------------- the reach

def reach_paths():
    """Each arm's under-water stretch from the mantle's root ring to where its socket's incoming
    leg starts, in the concept's field (relative positions + PLAT)."""
    out = {}
    for lane in LANES:
        sx = lane_side(lane)
        iy = 0.45 if lane in (1, 4) else 0.8
        sock = np.array(SOCKETS[lane])
        leg = sock + np.array([sx * 0.95, iy * 0.75]) * ROOT_PX     # the incoming leg's start
        root = np.array(kr.ROOTS[MANTLE_ROOT[lane]]) - PLAT
        if lane in (1, 4):
            mid = [root + np.array([-sx * 36, 0.0]), (root + leg) / 2 + np.array([0.0, 12.0])]
        else:
            mid = [root + np.array([-sx * 10, -16.0])]
        end = leg + (sock - leg) * 0.35
        out[lane] = [tuple(PLAT + p) for p in [root] + mid + [leg, end]]
    return out


def reach_set(art):
    """The four under-water reaches, plain, for the `sub` pass: a 4-frame sway (none at the ends)."""
    paths = reach_paths()
    allp = np.array([p for ps in paths.values() for p in ps])
    lo, hi = allp.min(axis=0) - 30, allp.max(axis=0) + 30
    size = (int(np.ceil(hi[0] - lo[0])) // 2 * 2, int(np.ceil(hi[1] - lo[1])) // 2 * 2)
    centre = lo + np.array(size) / 2
    origin = centre - np.array(size) / 2
    frames = []
    for j in range(SWAY_FRAMES):
        w = TAU * j / SWAY_FRAMES
        poses = []
        for lane, ps in paths.items():
            sw = []
            for i, (x, y) in enumerate(ps):
                env = np.sin(np.pi * i / (len(ps) - 1))
                sw.append((x + 5 * env * np.sin(w + lane), y + 3.5 * env * np.cos(w + 1.3 * lane)))
            poses.append(kr.sample_arm(sw, lambda u, ue: -1.0 + 0.8 * u, f"reach{lane}", 42, 38))
        frames.append(compose_arms(art, poses, size, origin, False))
    return frames, centre, size


# --------------------------------------------------------------------------- pivots

def rest_path(lane):
    """An arm at rest beyond its socket: a curl under the water below it (px relative to the platform)."""
    x, y = SOCKETS[lane]
    s = REST_SIGN[lane]
    ox = (LANE_X[lane] - x) * 0.5
    return [[round(x + ox * 0.6, 1), y + 30], [round(x + ox, 1), y + 50], [round(x + ox + 10 * s, 1), y + 66],
            [round(x + ox + 2 * s, 1), y + 78], [round(x + ox - 12 * s, 1), y + 70]]


def write_r40_pivots(reach_centre, reach_size):
    """Round 40's registration, added to assets/pivots/harbour-kraken.json under ``r40_a`` (every
    round-33 key stays as it is, so today's game keeps drawing the chosen fight)."""
    path = artkit.PIVOTS / f"{SLUG}.json"
    data = json.loads(path.read_text(encoding="utf-8"))
    head = np.array(data["head"])
    halt = lambda p: [round(240 + p[0], 1), round(R40_ANCHOR + p[1], 1)]   # noqa: E731
    roots = []
    for lane in LANES:
        mr = np.array(kr.ROOTS[MANTLE_ROOT[lane]]) - PLAT
        roots.append({"lane": lane, "socket": list(SOCKETS[lane]), "socket_at_halt": halt(SOCKETS[lane]),
                      "mantle_root": [round(float(v), 1) for v in mr], "lane_x": LANE_X[lane], "lane_top": LANE_TOP,
                      "rest_path": rest_path(lane)})
    data["r40_a"] = {
        "note": "round 40 proposal a (L11 rework 3a, pending the user's pick): four one-lane arms rooted in sockets; "
                "the head, grip, surfacing, death and segment frames and every round-33 key above are reused unchanged; "
                "the idle arms and the two slam arms' rest curls are not drawn (8 arms: 4 grip + 4 lane arms)",
        "anchor": R40_ANCHOR,
        "anchor_note": "the platform's centre at the halt, screen y (round 33: 78): everything moves 124 px up",
        "at_halt": {"head": halt(head), "eyes": [halt(e) for e in data["eyes"]], "eye_radius": data["eye_radius"],
                    "beak": halt(data["beak"]), "deck_y": [R40_ANCHOR - 76, R40_ANCHOR + 76]},
        "roots": roots,
        "root_size": list(ROOT_SIZE), "root_target": list(ROOT_TARGET),
        "root_note": "a socket sprite is centred on its socket; the slam chain starts at the socket and runs to "
                     "(lane_x, lane_top), then down the lane (the round-33 lane leg); its first segments lie under "
                     "the socket sprite, which is drawn over the arm's segments",
        "root_frames": {"root": INTACT_FRAMES, "root-raised": RAISED_FRAMES, "root-hit": 1, "root-cut": CUT_FRAMES,
                        "root-sink": RSINK_FRAMES},
        "root_index": "(lane - 1) * frames + frame",
        "root_states": {
            "root": "intact, a 4-frame loop",
            "root-hit": "intact frame 0 at the hit flash (0.3 toward the pale flesh tint; or the flash shader on 'root')",
            "root-raised": "the tell: frames 0-3 rear up once, then 4-5 loop until the arm rises; with root-glow",
            "root-cut": "frames 0-1 the stump recoiling (play once, with the arm's segment pops), then 2-5 the ooze loop",
            "root-sink": "the death: the knot going under (play once), with the head's sink"},
        "root_glow": "additive, centred on the socket, a 4-frame pulse while raised (glow steps)",
        "root_glow_size": list(GLOW_SIZE),
        "reach": [round(float(v - p), 1) for v, p in zip(reach_centre, PLAT)], "reach_size": list(reach_size),
        "reach_note": "the four arms' under-water stretches from the mantle's roots to the sockets, plain: through "
                      "the `sub` pass with the head, a sway loop (reach steps)",
        "steps": R40_STEPS,
        "sprites": {k: f"{SLUG}-{k}-{R40_TAG}" for k in ("root", "root-raised", "root-hit", "root-cut", "root-sink",
                                                          "root-glow", "reach")}}
    path.write_text(json.dumps(data, indent=1) + "\n", encoding="utf-8")
    print(f"pivots: {path.relative_to(ROOT)} (r40_a)")


# --------------------------------------------------------------------------- review

def r40_load():
    names = ("root", "root-raised", "root-hit", "root-cut", "root-sink", "root-glow", "reach")
    out = {n: artkit.load_frames(f"{SLUG}-{n}-{R40_TAG}") for n in names}
    out.update(load())
    return out


def by_lane(frames, lane, n):
    return frames[(lane - 1) * n:lane * n]


def scr(rel_xy):
    """A position relative to the platform -> the play field at the halt (round 40)."""
    return 240.0 + rel_xy[0], R40_ANCHOR + rel_xy[1]


def lane_arm(lane, share, whip_amp, phase):
    """A slam arm's path from its socket into its lane and down to the tip (screen px)."""
    sx, sy = scr(SOCKETS[lane])
    lx, ly = scr((LANE_X[lane], LANE_TOP))
    pts = [(sx, sy)]
    for i in range(13):
        u = i / 12
        x = lx + whip_amp * min(1.0, u * 1.5) * np.sin(TAU * (1.25 * u - 1.5 * share) + phase)
        pts.append((x, ly + (PlayFieldH - 24 - ly) * u))
    return pts


PlayFieldH = 540


def r40_state(t):
    """The review's choreography (s): lane 2 telegraphs, slams, is shot and cut; the head surfaces,
    opens its eyes and glows for a fan; lane 4 telegraphs and slams; the death."""
    s = {"head": ("deep", 0), "roots": {k: ("root", int(t * STEP / R40_STEPS["root"]) % INTACT_FRAMES) for k in LANES},
         "arms": [], "glow": [], "marks": [], "pops": [], "death": None}
    for lane, t0 in ((2, 1.0), (4, 8.8)):
        tel, rise, awash, sink = t0, t0 + 1.4, t0 + 1.9, t0 + 3.4 if lane == 2 else t0 + 2.8
        if tel <= t < rise:
            k = int((t - tel) * STEP / R40_STEPS["raised"])
            fr = k if k < 4 else 4 + int((t - tel) * STEP / R40_STEPS["raised_hold"]) % 2
            s["roots"][lane] = ("root-raised", fr)
            s["glow"].append((lane, int(t * STEP / R40_STEPS["glow"]) % GLOW_FRAMES))
            s["marks"].append(lane)
            s["arms"].append((lane, "under", (t - tel) / 1.4))
        elif rise <= t < awash:
            s["roots"][lane] = ("root-raised", 4 + int(t * STEP / R40_STEPS["raised_hold"]) % 2)
            s["arms"].append((lane, "rise", (t - rise) / 0.5))
            s["marks"].append(lane)
        elif awash <= t < sink:
            s["arms"].append((lane, "awash", (t - awash)))
            if lane == 2 and int((t - awash) * 15) % 5 == 0:
                s["roots"][lane] = ("root-hit", 0)
        elif lane == 4 and sink <= t < sink + 0.5:
            s["arms"].append((lane, "sink", (t - sink) / 0.5))
    cut_t = 4.4
    if t >= cut_t:
        k = int((t - cut_t) * STEP / R40_STEPS["cut"])
        s["roots"][2] = ("root-cut", k if k < 2 else 2 + int((t - cut_t) * STEP / R40_STEPS["ooze"]) % 4)
        if t < cut_t + 1.2:
            s["pops"].append((2, t - cut_t))
    if 5.0 <= t < 7.0:
        s["head"] = ("swap", min(SURFACE_FRAMES - 1, int((t - 5.0) * STEP / SURFACE_STEPS)))
    elif 7.0 <= t < 12.1:
        tm = t - 7.0
        fr = 1 if tm < 0.15 else 2
        if 0.8 <= tm < 1.6:
            fr = 3 if tm < 1.2 else 4
        s["head"] = ("up", fr)
    if t >= 12.1:
        d = t - 12.1
        s["death"] = d
        s["head"] = ("dead", min(SINK_FRAMES - 1, int(d * STEP / SINK_STEPS)))
        k = min(RSINK_FRAMES - 1, int(d * STEP / R40_STEPS["root_sink"]))
        for lane in LANES:
            s["roots"][lane] = ("root-sink", k) if lane != 2 else s["roots"][2]
        s["arms"] = []
    return s


def draw_r40(art, plat, sea, t, boxes=False):
    s = r40_state(t)
    cell = sea.copy()
    sub = Image.new("RGBA", cell.size)
    a = Art(art["arm"], art["tip"])
    j = int(t * STEP / SWAY_STEPS) % SWAY_FRAMES
    death = s["death"]
    hx, hy = scr(rel(HEAD_AT))
    rd = json.loads((artkit.PIVOTS / f"{SLUG}.json").read_text(encoding="utf-8"))["r40_a"]
    gs = art["grip-sub"][j] if death is None else fade(art["grip-sub"][j], 1 - min(death, 1))
    put(sub, gs, *scr(GRIP_SUB_AT))
    rs = art["reach"][int(t * STEP / R40_STEPS["reach"]) % SWAY_FRAMES]
    put(sub, rs if death is None else fade(rs, 1 - min(death, 1)), *scr(rd["reach"]))
    if death is None or death < 1.6:
        put(sub, art["sub"][0] if death is None else fade(art["sub"][0], 1 - death / 1.6), hx, hy)
    chains = []
    for lane, phase, k in s["arms"]:
        if phase == "under":
            pts = lane_arm(lane, 0.0, 6, 0.0)
            rest = [scr(p) for p in [SOCKETS[lane]] + rd["roots"][lane - 1]["rest_path"]]
            pts = [tuple(np.array(rest[min(i * len(rest) // len(pts), len(rest) - 1)]) * (1 - k) + np.array(p) * k)
                   for i, p in enumerate(pts)]
            e_of = lambda u, ue: -0.4      # noqa: E731
        elif phase == "rise":
            pts = lane_arm(lane, k, 22, 0.0)
            e_of = (lambda kk: (lambda u, ue: 0.3 if u <= kk else -0.3))(k)
        elif phase == "awash":
            pts = lane_arm(lane, 1.0, 6 + 16 * np.exp(-k * 2.5), 0.0)
            e_of = lambda u, ue: 0.03      # noqa: E731
        else:
            pts = lane_arm(lane, 1.0, 6, 0.0)
            e_of = (lambda kk: (lambda u, ue: 0.03 if u < 1 - kk else -0.3))(k)
        ap = kr.sample_arm(pts, e_of, f"lane{lane}", 40, 14)
        chains.append(ap)
        for (x, y), hd, sz, e, tip in reversed(items_of(ap)):
            if e <= 0:
                put(sub, a.segment(sz, hd, tip), x, y)
    for lane in LANES:                                       # the rest curls, under the water
        if s["roots"][lane][0] == "root-cut" or any(c[0] == lane for c in s["arms"]) or death is not None:
            continue
        rest = [scr(p) for p in [SOCKETS[lane]] + rd["roots"][lane - 1]["rest_path"]]
        ap = kr.sample_arm(rest, lambda u, ue: -0.6, f"rest{lane}", 40, 22)
        for (x, y), hd, sz, e, tip in reversed(items_of(ap)):
            put(sub, a.segment(sz, hd, tip), x, y)
    cell.alpha_composite(dj.sub_pass(sub, t, k=0.5, blur=0.9, sway=0.8))
    for lane in s["marks"]:                                  # stand-in for round 33's lane marks
        if int(t * 10) % 2 == 0:
            lx = scr((LANE_X[lane], 0))[0]
            d = ImageDraw.Draw(cell, "RGBA")
            for y in range(int(scr((0, LANE_TOP))[1]), 516, 22):
                for ex in (lx - 56, lx + 52):
                    d.rectangle([ex, y, ex + 3, y + 11], fill=(230, 60, 50, 150))
    for x in (60, 180, 420):                                 # stand-ins: the cargo ships at the halt
        d = ImageDraw.Draw(cell, "RGBA")
        d.rounded_rectangle([x - 12, 422, x + 12, 478], 8, fill=(88, 96, 104, 255), outline=(40, 44, 50, 255))
    put(cell, plat, 240, R40_ANCHOR)
    if death is None:
        put(cell, art["grip"][j], *scr(GRIP_AT))
    else:
        put(cell, art["grip-slide"][min(int(death * STEP / SLIDE_STEPS), SLIDE_FRAMES - 1)], *scr(GRIP_AT))
    kind, fi = s["head"]
    if kind == "swap":
        put(cell, art["surface"][fi], hx, hy)
    elif kind == "up":
        put(cell, art["up"][fi], hx, hy)
        put(cell, art["collar"][int(t * STEP / COLLAR_STEPS) % COLLAR_FRAMES], hx, hy)
    elif kind == "dead":
        put(cell, art["sink"][fi], hx, hy)
    for ap in chains:
        items = items_of(ap)
        for (x, y), hd, sz, e, tip in reversed(items):
            if e > 0:
                put(cell, a.segment(sz, hd, tip), x, y)
        for n, (p, q) in enumerate(zip(items, items[1:])):
            if p[3] * q[3] < 0 or (abs(p[3]) < 0.06 and n % 2 == 0):
                put(cell, art["foam"][(n % 2) * FOAM_FRAMES + int(t * 10 + n) % FOAM_FRAMES], *p[0])
    n_of = {"root": INTACT_FRAMES, "root-raised": RAISED_FRAMES, "root-hit": 1, "root-cut": CUT_FRAMES,
            "root-sink": RSINK_FRAMES}
    for lane in LANES:
        name, fr = s["roots"][lane]
        if death is not None and death > (0.8 if name == "root-cut" else RSINK_FRAMES * R40_STEPS["root_sink"] / STEP):
            continue
        put(cell, by_lane(art[name], lane, n_of[name])[fr], *scr(SOCKETS[lane]))
    for lane, g in s["glow"]:
        cell = add(cell, art["root-glow"][g], *scr(SOCKETS[lane]))
    for lane, dt in s["pops"]:                               # the severed arm's segment pops
        pts = lane_arm(lane, 1.0, 6, 0.0)
        ap = kr.sample_arm(pts, lambda u, ue: 0.03, "pop", 40, 14)
        for n, (x, y) in enumerate(ap.pts):
            k = int(dt * STEP / 4) - n // 2
            if 0 <= k < ARM_DEATH_FRAMES:
                cell = add(cell, art["arm-death"][k], x, y)
    if death is not None:
        k = int(death * STEP / DEATH_STEPS)
        if k < DEATH_FRAMES:
            cell = add(cell, art["death"][k], hx + DEATH_AT[0], hy + DEATH_AT[1])
    if boxes:
        d = ImageDraw.Draw(cell, "RGBA")
        for lane in LANES:
            x, y = scr(SOCKETS[lane])
            d.rectangle([x - 20, y - 20, x + 20, y + 20], outline=(255, 230, 60, 255))
        for e in rd["at_halt"]["eyes"]:
            r = rd["at_halt"]["eye_radius"]
            d.ellipse([e[0] - r, e[1] - r, e[0] + r, e[1] + r], outline=(140, 255, 120, 255))
        bx, by = rd["at_halt"]["beak"]
        d.line([bx - 6, by, bx + 6, by], fill=(255, 90, 80, 255))
        d.line([bx, by - 6, bx, by + 6], fill=(255, 90, 80, 255))
        for lx in (120, 240, 360):
            for y in range(0, 540, 8):
                d.line([lx, y, lx, y + 3], fill=(255, 255, 255, 70))
    return cell


def review_r40():
    artkit.REVIEW_ROUND = R40_ROUND
    art = r40_load()
    plat = platform_stand_in()
    sea = dj.ocean_plate(480, 540, seed=121)

    def on_sea(img, size=ROOT_SIZE):
        cell = dj.ocean_plate(*size, seed=121)
        put(cell, img, size[0] / 2, size[1] / 2)
        return cell
    states = []
    for name, n in (("root", INTACT_FRAMES), ("root-raised", RAISED_FRAMES), ("root-cut", CUT_FRAMES),
                    ("root-sink", RSINK_FRAMES)):
        states.append((name, [on_sea(f) for f in by_lane(art[name], 2, n)]))
    lanes_row = [on_sea(by_lane(art["root"], k, INTACT_FRAMES)[0]) for k in LANES] + \
                [on_sea(art["root-hit"][k - 1]) for k in LANES]
    stills = [draw_r40(art, plat, sea, t).resize((240, 270), Image.BOX) for t in (1.9, 3.4, 6.0, 8.3, 10.4)]
    layout = draw_r40(art, plat, sea, 8.3, boxes=True)
    reach_cell = dj.ocean_plate(art["reach"][0].width, art["reach"][0].height, seed=121)
    reach_cell.alpha_composite(dj.sub_pass(art["reach"][0], 0.0, k=0.5, blur=0.9))
    sheet = artkit.review_sheet("HARBOUR KRAKEN - ROUND 40 PROPOSAL A: FOUR ROOTED ARMS, 124 PX UP", [
        ("THE HALT AT 1X (PLATFORM CENTRE Y -46): ROOT TARGETS 40X40 (YELLOW), EYES (GREEN), BEAK (RED), LANES; "
         "SHIPS AND LANE MARKS ARE STAND-INS", [layout], 1, False),
        ("IN PLAY (0.5X): LANE 2 RAISED (TELL) / SLAM AWASH, ROOT SHOT / CUT, HEAD SURFACING / BEAK GLOW / LANE 4 RISING",
         stills, 1, False),
        ("SOCKETS, INTACT FRAME 0, LANES 1-4 / HIT FLASH, LANES 1-4 (ON THE SEA)", lanes_row, 2, False),
        ("LANE 2 INTACT LOOP (4 X 12 STEPS)", states[0][1], 2, False),
        ("LANE 2 RAISED: REARING 0-3 (9 STEPS), HOLD 4-5 (8 STEPS) - THE TELL, WITH THE GLOW", states[1][1], 2, False),
        ("RAISED GLOW (ADDITIVE, 4 X 6 STEPS)", art["root-glow"], 2, True),
        ("LANE 2 CUT: STUMP RECOIL 0-1, THEN THE OOZE LOOP 2-5", states[2][1], 2, False),
        ("LANE 2 SINK AT THE DEATH (5 X 12 STEPS)", states[3][1], 2, False),
        ("THE FOUR ARMS' UNDER-WATER REACH (SUB PASS STAND-IN), 4 SWAY FRAMES", [reach_cell] + art["reach"][1:2], 1,
         False)], width=1500, batch=R40_BATCH)
    gif = [draw_r40(art, plat, sea, f / 15) for f in range(int(14.5 * 15))]
    png = CONCEPT / f"{SLUG}-{R40_TAG}.png"
    gif_path = CONCEPT / f"{SLUG}-{R40_TAG}.gif"
    sheet.convert("RGB").save(png, optimize=True)
    write_gif(gif, gif_path, fps=15, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif_path.relative_to(ROOT)}")


def r40_main(argv):
    if "--review" not in argv:
        build_r40()
    review_r40()


if __name__ == "__main__":
    artkit.REVIEW_ROUND = REVIEW_ROUND
    if "--r40" in sys.argv[1:]:
        r40_main(sys.argv[1:])
    else:
        if "--review" not in sys.argv[1:]:
            build()
        review()
