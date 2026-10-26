#!/usr/bin/env python3
"""Production art: the Abyssal Maw, Act 4's boss (design/enemies/bosses/abyssal-maw; M8 part H batch, concept round 58,
step ART-H1). From the chosen round-48 look **b** "coiled guardian" (tools/concept/bosses_r48.py ``coil_seg``,
``maw_head``, ``gill_sac``, ``implant``, ``maw_mats``, imported unchanged): a serpent leviathan of dark shingled chitin
plates over a deep-violet hide, a bioluminescent teal line down each flank (lime as a sweep's tell), a long eel skull
with an underslung toothed jaw and a lime throat, four lime-teal gill sacs, and three grey unmarked Helix implants with
a hot-amber core (nothing gold, Q11 a). Every frame is its own render under the fixed key light; nothing lit is turned
or mirrored as an image.

Scales as the concept: the coil 32 px per model unit (a segment 64 px wide, ~76 long, 56 apart), the head 60 px per
unit. Headings as tools/art/README.md: heading k turns k x 11.25 degrees clockwise from straight down; a segment's
heading is the way **toward the head** (its plates shingle that way). From the simulation's ``segmentAngle`` a (radians,
0 to the right, y up, toward the tail): k = round((pi/2 - a) * 32 / (2 pi)) mod 32. The tail tapers: the last nine
segments (the right edge's run to the tail tip) in three steps of three (``TAPER_OF``).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: l23kit's rule claims
``abyssal-maw`` once design/enemies/bosses/abyssal-maw/data.yaml exists):
  abyssal-maw-segment_0..127        78x78, normal: heading x 4 + taper (0 full width, 1 x0.82, 2 x0.66, 3 x0.5); one
                                    32-colour palette
  abyssal-maw-segment-glow_0..127   78x78, additive, the same order: the teal flank lines alone (a 1 px stepped halo),
                                    drawn ungraded over the body (the frame in the dark, the glow-only twin)
  abyssal-maw-segment-tell_0..127   78x78, additive, the same order: the flank lines lime and brighter: a sweep's bight
                                    in its tell and swing, and a shed point's flare (with ``-shed``)
  abyssal-maw-head_0..6             200x212, normal (the model's centre at the frame's centre; the neck runs off the
                                    top edge into the coil): 0 rest (jaw shut, the throat dim), 1 the throat's glow (the
                                    jaw ajar: a fan's or a wall's tell), 2 open (the throat's window), 3 reared (phase
                                    3: the jaw clamped, the throat dark, the skull lifted toward the camera), 4 the dive
                                    (the snout pitched down into the dark), 5 the lunge (facing up the screen, jaws
                                    open), 6 dead (the jaw gaping, locked; the glows guttering)
  abyssal-maw-head-glow_0..6        200x212, additive, the same order: eyes, flank lines and the throat alone
  abyssal-maw-head-rim              200x212, additive, one frame: the teal orbs lighting along the jaw's rim (a fan
                                    wall's tell, over head frame 1 or 2)
  abyssal-maw-sac_0..3              44x44, normal: idle, swelling, full (the 0.6 s tell's peak), torn (destroyed)
  abyssal-maw-sac-glow_0..2         44x44, additive: idle, swelling, full (the torn sac has none)
  abyssal-maw-implant_0..4          48x48, normal: shut (a grey scab), spark a, spark b (the 1.0 s tell: the cover split
                                    and ajar, an amber flicker; alternate at ~12 fps), open (the hot-amber core, the
                                    cover blown aside), blown (the death: a scorched crater)
  abyssal-maw-implant-glow_0..2     48x48, additive: spark a, spark b, open (implant frames 1-3)
  abyssal-maw-arc_0..3              56x56, additive: the amber arc crawling over an open housing (the 0.4 s burst tell,
                                    ~16 fps) and the sparks of the opening tell
  abyssal-maw-shed_0..5             80x80, additive: a shed point's plate flaring lime (the 1.0 s tell; over the
                                    segment's ``-tell`` frame), ~6 fps, frame 5 the slip-out
  abyssal-maw-silt_0..7             48x48, normal, stepped translucency: silt streaming off the plates (a sweep's tell,
                                    the coil rising, the lunge column's floor lifting), 10 fps
  abyssal-maw-shadow-head           128x150, normal, stepped translucency, near black: the lunge's shadow tip (the
                                    head's outline) running up the column
  abyssal-maw-shadow-column         96x32, normal, stepped translucency: the column's shadow below the tip, tiled
                                    down to the bottom edge (seamless in y)
  abyssal-maw-blowout_0..7          96x96, normal, stepped translucency: an implant blowing out at the death (grey
                                    casing flung out of a dark burst), 12 fps
  abyssal-maw-blowout-glow_0..7     96x96, additive: its amber flash and electric arcs, the same timing
  abyssal-maw-ichor_0..7            80x80, normal, stepped translucency: a segment's sinking burst (silt cloud, lime
                                    ichor), 10 fps
  abyssal-maw-flare_0..3            192x192, additive: the vent's white-lime flare as the head sinks (drawn scaled up
                                    to ~1.6x over the throat; 4 fps rise, hold, fall)
  assets/pivots/abyssal-maw.json    the head's frame, its throat, eyes and sac offsets per head frame, the rim orbs;
                                    the segment frame, headings, tapers and the taper rule; the implants' sizes
  intel/boss-abyssal-maw            via tools/art/intel.py (``intel_mask`` here: the coil round the arena and the head)
  design/enemies/bosses/concept/abyssal-maw-final-r58-a.png/.gif   review sheet and GIF (the fight on Level 28's
                                    caldera under the hatchery light's stand-in: the rise, phase 1, phase 2 with a
                                    sweep and a shed, the dive and the lunge, phase 3's implants, the death)

Run: python3 tools/art/abyssal_maw.py [head|segment|parts|fx ...] [--review | --relocate]   (~7 min on 8 cores, the segments most of
it; groups rebuild only their sets; --review only rebuilds the review files; --relocate moves the held sets into
assets/ once the data claims them, without rendering; backdrop_l28.py first for the caldera)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import l28kit as kit
from artkit import DESIGN, TAU

import bosses_r48 as b48  # noqa: E402  (concept script, unchanged: the chosen look b's models)
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_x, rotate_z  # noqa: E402

SCRIPT = "abyssal_maw.py"
BATCH = "M8 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r58"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "bosses" / "concept"
ROOT_NAME = "abyssal-maw"

HEADINGS = 32
SEG, SEG_PX = 78, 32.0                       # canvas; px per model unit
TAPERS = (1.0, 0.82, 0.66, 0.5)
HEAD, HEAD_PX = (200, 212), 60.0
SAC, SAC_EXT = 44, 2.0
IMP, IMP_EXT = 48, 2.4
LIME = np.array((168, 255, 42), float)
TEAL = np.array((0, 255, 154), float)
AMBER = np.array((255, 122, 32), float)
SILT = np.array((92, 104, 98), float)
GREY = np.array((124, 128, 134), float)


def taper_of(i, n=31):
    """The taper step of segment ``i`` (0 next to the head) in a coil of ``n``: the last nine in three steps."""
    from_tail = n - 1 - i
    return 0 if from_tail >= 9 else 3 - from_tail // 3


# --------------------------------------------------------------------------- render jobs

def blackened(mats):
    return [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]


def head_model(name):
    """(scene, mats) of a head frame: the concept's maw_head with the pose's transform."""
    kw = {"rest": dict(open_=0.0, throat=0.3), "glow": dict(open_=0.72, throat=1.0),
          "open": dict(open_=1.0, throat=1.0), "rear": dict(open_=0.0, throat=0.0),
          "dive": dict(open_=0.0, throat=0.25), "lunge": dict(open_=1.0, throat=1.0, up=1.0),
          "death": dict(open_=1.18, throat=0.1)}[name]
    scene, mats = b48.maw_head(**kw)
    if name == "death":
        mats = b48.maw_mats(0.35, throat=0.1)
    if name == "rear":                          # the skull lifted toward the camera, snout up a little
        base = scene
        hinge = np.array([0.0, 1.0, 0.0])

        def scene(p, base=base):
            q = rotate_x(p - hinge, 0.22) + hinge
            q = q - np.array([0.0, 0.0, 0.35])
            return base(q)
        scene = artkit.perspective(scene, 5.0)
    elif name == "dive":                        # the snout pitched down into the dark
        base = scene
        hinge = np.array([0.0, 1.0, 0.0])

        def scene(p, base=base):
            return base(rotate_x(p - hinge, -0.5) + hinge)
    return scene, mats


HEAD_NAMES = ("rest", "glow", "open", "rear", "dive", "lunge", "death")


def _render(job):
    kind, a, b, black = job
    if kind == "seg":
        k, ti, line = a, b, (1.0 if black == "lime" else 0.0)
        scene, mats = b48.coil_seg(TAPERS[ti], line=line)
        rot = model_rotation(np.pi / 2 + TAU * k / HEADINGS)
        scene2 = (lambda p, s=scene, r=rot: s(rotate_z(p, r)))
        mats = model_space_materials(mats, rot)
        if black:
            mats = blackened(mats)
        return artkit.native(*artkit.render_hi(scene2, mats, (SEG, SEG), SEG / SEG_PX), crisp=0 if black else 70)
    if kind == "head":
        scene, mats = head_model(a)
        if black:
            mats = blackened(mats)
        return artkit.native(*artkit.render_hi(scene, mats, HEAD, HEAD[0] / HEAD_PX), crisp=0 if black else 70)
    if kind == "sac":
        scene, mats = b48.gill_sac(**a)
        if black:
            mats = blackened(mats)
        return artkit.native(*artkit.render_hi(scene, mats, (SAC, SAC), SAC_EXT), crisp=0 if black else 70)
    if kind == "imp":
        state, core = a, b
        scene, mats = b48.implant(state)
        if core is not None:
            mats = b48.maw_mats(1.0, core=core)
        if black:
            mats = blackened(mats)
        return artkit.native(*artkit.render_hi(scene, mats, (IMP, IMP), IMP_EXT), crisp=0 if black else 70)
    raise ValueError(kind)


def glow_frame(black, halo=1.4, k=0.7, gain=1.0):
    """Emission-only render -> additive: the glowing parts, a 1 px stepped halo."""
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255) * gain
    blur = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(halo))
    hb = np.array(blur, float)
    out = np.maximum(rgb, np.floor(hb / 255 * 3 + 0.5) / 3 * 255 * k * (hb.max(-1, keepdims=True) > 10))
    out[rgb.max(-1) < 14] = np.minimum(out[rgb.max(-1) < 14], hb[rgb.max(-1) < 14] * 1.6)
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


# --------------------------------------------------------------------------- 2-D effects

def _yx(w, h=None):
    h = h or w
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    return yy, xx


def _puff(density, rgb, levels=4):
    alpha = artkit.ordered_dither(np.clip(density, 0, 1), levels)
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def _add(rgb):
    rgb = np.clip(rgb, 0, 255)
    alpha = np.where(rgb.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def silt_frame(i, n=8):
    """Silt streaming off the plates: a soft grey-green plume rising and spreading, thinning (no bubbles)."""
    w = 48
    u = i / (n - 1)
    yy, xx = _yx(w)
    noise = raster.fbm(w, w, 6, 5801 + i, octaves=3, period=False)
    dens = np.zeros((w, w))
    rng = np.random.default_rng(5803)
    for j in range(5):
        ox, oy, r0 = rng.uniform(-8, 8), rng.uniform(-4, 8), rng.uniform(3, 6)
        cx, cy = w / 2 + ox * (1 + u), w / 2 + oy - 12 * u * (0.6 + 0.4 * j / 4)
        r = r0 + 9 * u
        dens += np.clip(1 - np.hypot(xx - cx, yy - cy) / (r * (0.75 + 0.5 * noise)), 0, 1) ** 0.9
    dens = np.clip(dens, 0, 1) * (1 - u) ** 1.2 * 0.85
    return _puff(dens, SILT[None, None] * (0.85 + 0.3 * noise[..., None]))


def ichor_frame(i, n=8):
    """A sinking segment's burst: a silt cloud swelling and settling, lime ichor flung out in blobs and threads."""
    w = 80
    u = i / (n - 1)
    yy, xx = _yx(w)
    noise = raster.fbm(w, w, 9, 5811, octaves=3, period=False)
    r = np.hypot(xx - w / 2, yy - w / 2 - 4 * u)
    rad = 8 + 26 * (1 - (1 - u) ** 2)
    cloud = np.clip(1 - r / (rad * (0.72 + 0.56 * noise)), 0, 1) ** 0.8 * (1 - u) ** 1.2 * 0.9
    img = _puff(cloud, np.array((58, 66, 60), float) * (0.8 + 0.4 * noise[..., None]))
    a = np.array(img)
    rng = np.random.default_rng(5813)
    for j in range(11):
        ang, v = rng.uniform(0, TAU), rng.uniform(14, 34)
        d = v * (1 - (1 - u) ** 2)
        px, py = w / 2 + np.cos(ang) * d, w / 2 + np.sin(ang) * d + 5 * u * u
        s = rng.uniform(1.6, 3.4) * (1 - 0.5 * u)
        if u > 0.85 and j % 2:
            continue
        m = np.hypot(xx - px, yy - py) < s
        tail = (np.abs((xx - px) * np.sin(ang) - (yy - py) * np.cos(ang)) < 0.8) & \
            (((xx - px) * np.cos(ang) + (yy - py) * np.sin(ang)) < 0) & \
            (np.hypot(xx - px, yy - py) < s + 6 * (1 - u))
        col = LIME * (1 - 0.45 * u) + np.array((30, 40, 10)) * u
        a[m | tail, :3] = np.clip(col, 0, 255)
        a[m | tail, 3] = 255
    return Image.fromarray(a, "RGBA")


def blowout_frame(i, n=8):
    """An implant blowing out: a dark burst cloud, grey casing plates and a cable end flung out, slowing."""
    w = 96
    u = i / (n - 1)
    yy, xx = _yx(w)
    noise = raster.fbm(w, w, 10, 5821, octaves=3, period=False)
    r = np.hypot(xx - w / 2, yy - w / 2)
    rad = 9 + 26 * (1 - (1 - u) ** 2)
    cloud = np.clip(1 - r / (rad * (0.72 + 0.56 * noise)), 0, 1) ** 0.8 * (1 - u) ** 1.25 * 0.85
    img = _puff(cloud, np.array((64, 62, 66), float) * (0.85 + 0.3 * noise[..., None]))
    a = np.array(img)
    rng = np.random.default_rng(5823)
    for j in range(14):
        ang, v = rng.uniform(0, TAU), rng.uniform(18, 42)
        d = v * (1 - (1 - u) ** 2)
        px, py = w / 2 + np.cos(ang) * d, w / 2 + np.sin(ang) * d + 4 * u * u
        s, rot = rng.uniform(3.0, 6.2) * (1 - 0.25 * u), rng.uniform(0, TAU) + u * rng.uniform(-4, 4)
        if u > 0.9 and j % 3:
            continue
        cx, cy = xx - px, yy - py
        lx = cx * np.cos(rot) + cy * np.sin(rot)
        ly = -cx * np.sin(rot) + cy * np.cos(rot)
        m = (np.abs(lx) < s) & (np.abs(ly) < s * 0.55)
        lit = np.where(lx + ly < 0, 1.15, 0.72)
        hot = AMBER * 1.0 if j < 3 and u < 0.35 else GREY * (1 - 0.4 * u)
        a[m, :3] = np.clip(hot * lit[m, None], 0, 255)
        a[m, 3] = 255
    return Image.fromarray(a, "RGBA")


def _jagged(rng, x0, y0, x1, y1, n=7, amp=4.0):
    pts = [(x0, y0)]
    for j in range(1, n):
        f = j / n
        nx, ny = -(y1 - y0), (x1 - x0)
        ln = np.hypot(nx, ny) or 1
        o = rng.uniform(-amp, amp)
        pts.append((x0 + (x1 - x0) * f + nx / ln * o, y0 + (y1 - y0) * f + ny / ln * o))
    pts.append((x1, y1))
    return pts


def _arc_layer(w, segs, seed, width=2):
    """Additive electric arcs: amber strokes with a white-hot core, drawn at 4x and reduced."""
    k = 4
    img = Image.new("RGB", (w * k, w * k), (0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    for (x0, y0, x1, y1) in segs:
        pts = [(x * k, y * k) for x, y in _jagged(rng, x0, y0, x1, y1)]
        d.line(pts, fill=(200, 80, 16), width=(width + 2) * k)
        d.line(pts, fill=(255, 170, 60), width=width * k)
        d.line(pts, fill=(255, 245, 210), width=max(1, k))
    return np.array(img.resize((w, w), Image.Resampling.BOX), float)


def arc_frame(i, n=4):
    """An amber arc crawling over a housing: three jagged arcs round a ring, re-struck every frame."""
    w = 56
    rng = np.random.default_rng(5831 + i)
    segs = []
    for j in range(3):
        a0 = rng.uniform(0, TAU)
        a1 = a0 + rng.uniform(0.9, 1.8)
        r0, r1 = rng.uniform(13, 20), rng.uniform(13, 20)
        segs.append((w / 2 + np.cos(a0) * r0, w / 2 + np.sin(a0) * r0, w / 2 + np.cos(a1) * r1, w / 2 + np.sin(a1) * r1))
    segs.append((w / 2 + rng.uniform(-6, 6), w / 2 + rng.uniform(-6, 6), w / 2 + rng.uniform(-22, 22),
                 w / 2 + rng.uniform(-22, 22)))
    rgb = _arc_layer(w, segs, 5833 + i, width=1)
    yy, xx = _yx(w)
    glow = np.exp(-(np.hypot(xx - w / 2, yy - w / 2) / 14) ** 2) * (0.35 + 0.15 * (i % 2))
    rgb = rgb + AMBER[None, None] * artkit.ordered_dither(glow, 5)[..., None] * 0.55
    return _add(rgb)


def blowout_glow_frame(i, n=8):
    """The blow-out's light: a white-amber flash shrinking, arcs lashing out and dying."""
    w = 96
    u = i / (n - 1)
    yy, xx = _yx(w)
    r = np.hypot(xx - w / 2, yy - w / 2)
    core = np.exp(-(r / (6 + 16 * u)) ** 2) * max(0.0, 1 - 1.3 * u)
    halo = np.exp(-(r / (16 + 22 * u)) ** 2) * (1 - u) * 0.55
    rgb = np.array((255, 236, 200))[None, None] * artkit.ordered_dither(np.clip(core, 0, 1), 6)[..., None] + \
        AMBER[None, None] * artkit.ordered_dither(np.clip(halo, 0, 1), 5)[..., None]
    if u < 0.75:
        rng = np.random.default_rng(5841 + i)
        segs = []
        for j in range(5 - int(u * 5)):
            a = rng.uniform(0, TAU)
            r1 = 18 + 24 * rng.uniform(0.5, 1.0) * (0.6 + u)
            segs.append((w / 2, w / 2, w / 2 + np.cos(a) * r1, w / 2 + np.sin(a) * r1))
        rgb = rgb + _arc_layer(w, segs, 5843 + i) * (1 - u)
    return _add(rgb)


def shed_frame(i, n=6):
    """A shed point's plate flaring lime: a bloom swelling over 1.0 s (frames 0-4), the slip-out's burst (5)."""
    w = 80
    yy, xx = _yx(w)
    r = np.hypot((xx - w / 2) / 1.0, (yy - w / 2) / 1.15)
    if i < n - 1:
        k = 0.35 + 0.65 * i / (n - 2)
        f = np.exp(-(r / (14 + 6 * k)) ** 2) * k + np.exp(-((r - 22 - 4 * k) / 3) ** 2) * 0.25 * k
    else:
        f = np.exp(-(r / 30) ** 2) * 0.7 + np.exp(-((r - 32) / 3.5) ** 2) * 0.5
    f = artkit.ordered_dither(np.clip(f, 0, 1), 6)
    rgb = LIME[None, None] * f[..., None] + 90 * np.clip(f - 0.75, 0, 1)[..., None] * 4
    return _add(rgb)


def flare_frame(i, n=4):
    """The vent's white-lime flare: a core and soft rays, rising (0-1), held (2), falling (3)."""
    w = 192
    k = (0.55, 1.0, 0.85, 0.4)[i]
    yy, xx = _yx(w)
    dx, dy = xx - w / 2, yy - w / 2
    r = np.hypot(dx, dy)
    ang = np.arctan2(dy, dx)
    rays = (0.55 + 0.45 * np.cos(ang * 9 + i * 0.4)) * (0.6 + 0.4 * np.cos(ang * 5 - 1.1))
    f = np.exp(-(r / 22) ** 2) * 1.2 + np.exp(-(r / (52 + 10 * k)) ** 2) * 0.55 + \
        np.exp(-(r / 88) ** 2) * rays * 0.35
    f = artkit.ordered_dither(np.clip(f * k, 0, 1), 8)
    white = np.clip(f - 0.6, 0, 1)[..., None] / 0.4
    rgb = (np.array((200, 255, 140))[None, None] * (1 - white) + 255 * white) * f[..., None]
    return _add(rgb)


def shadow_head(lunge):
    """The lunge's shadow tip: the lunge head's outline, softened and spread, near black in three steps."""
    a = np.array(lunge)[..., 3].astype(np.float64) / 255
    big = Image.fromarray((a * 255).astype(np.uint8)).resize((128, 136), Image.Resampling.BOX)
    pad = Image.new("L", (128, 150), 0)
    pad.paste(big, (0, 0))
    m = np.array(pad.filter(ImageFilter.GaussianBlur(5)), float) / 255
    yy, xx = _yx(128, 150)
    m = np.maximum(m, np.clip(1 - np.abs(xx - 64) / 46, 0, 1) * np.clip((yy - 96) / 50, 0, 1) * 0.85)
    dens = np.clip(m * 1.25, 0, 0.92)
    return _puff(dens, np.array((2, 6, 10), float)[None, None] * np.ones((150, 128, 1)), levels=4)


def shadow_column():
    """The column's shadow below the tip: 90 px wide, its edges soft in three steps, seamless in y."""
    yy, xx = _yx(96, 32)
    dens = np.clip(1 - np.abs(xx - 48) / 47, 0, 1) ** 0.6 * 0.86
    return _puff(dens, np.array((2, 6, 10), float)[None, None] * np.ones((32, 96, 1)), levels=4)


# --------------------------------------------------------------------------- pivots

def head_px(x, y):
    """A head model point (units, +Y up) in the head frame's px."""
    return [round(HEAD[0] / 2 + x * HEAD_PX, 1), round(HEAD[1] / 2 - y * HEAD_PX, 1)]


TEETH = [(0.70 * np.cos(a), -0.42 + 0.98 * np.sin(a)) for a in np.linspace(np.pi * 1.06, np.pi * 1.94, 11)]


def rim_frame():
    """Teal orbs lighting on the jaw's rim (the fan wall's tell): one at each tooth of the lower jaw."""
    w, h = HEAD
    yy, xx = _yx(w, h)
    f = np.zeros((h, w))
    for tx, ty in TEETH:
        x, y = head_px(tx, ty)
        r = np.hypot(xx - x, yy - y)
        f += np.exp(-(r / 2.6) ** 2) * 1.2 + np.exp(-(r / 6.0) ** 2) * 0.35
    f = artkit.ordered_dither(np.clip(f, 0, 1), 6)
    rgb = TEAL[None, None] * f[..., None] + 200 * np.clip(f - 0.8, 0, 1)[..., None] * 5
    return _add(rgb)


# --------------------------------------------------------------------------- build

GROUPS = ("head", "segment", "parts", "fx")


def _map(jobs):
    with ProcessPoolExecutor() as ex:
        return list(ex.map(_render, jobs, chunksize=2))


def build(groups=GROUPS):
    if "segment" in groups:
        segs = _map([("seg", k, ti, mode) for mode in (False, "teal", "lime") for k in range(HEADINGS)
                     for ti in range(4)])
        n = HEADINGS * 4
        kit.write(f"{ROOT_NAME}-segment", artkit.quantize_set(segs[:n], 32), SOURCE)
        kit.write(f"{ROOT_NAME}-segment-glow", artkit.quantize_set([glow_frame(g) for g in segs[n:2 * n]], 16),
                  SOURCE)
        kit.write(f"{ROOT_NAME}-segment-tell",
                  artkit.quantize_set([glow_frame(g, gain=1.15) for g in segs[2 * n:]], 16), SOURCE)
    if "head" in groups:
        heads = _map([("head", n, None, black) for black in (False, True) for n in HEAD_NAMES])
        nn = len(HEAD_NAMES)
        kit.write(f"{ROOT_NAME}-head", artkit.quantize_set(heads[:nn], 48), SOURCE)
        kit.write(f"{ROOT_NAME}-head-glow", artkit.quantize_set([glow_frame(g) for g in heads[nn:]], 24), SOURCE)
        kit.write(f"{ROOT_NAME}-head-rim", [rim_frame()], SOURCE, single=True)
        kit.write(f"{ROOT_NAME}-shadow-head", [shadow_head(heads[HEAD_NAMES.index("lunge")])], SOURCE, single=True)
    if "parts" in groups:
        sac_kw = [dict(swell=0.0), dict(swell=0.5), dict(swell=1.0), dict(swell=0.0, torn=1.0)]
        imp_st = [("shut", None), ("spark", None), ("spark", 0.75), ("open", None), ("blown", None)]
        res = _map([("sac", kw, None, False) for kw in sac_kw] + [("sac", kw, None, True) for kw in sac_kw[:3]]
                   + [("imp", st, c, False) for st, c in imp_st] + [("imp", st, c, True) for st, c in imp_st[1:4]])
        sacs, imps = res[:7], res[7:]
        kit.write(f"{ROOT_NAME}-sac", artkit.quantize_set(sacs[:4], 28), SOURCE)
        kit.write(f"{ROOT_NAME}-sac-glow", artkit.quantize_set([glow_frame(g, halo=1.2) for g in sacs[4:]], 16),
                  SOURCE)
        kit.write(f"{ROOT_NAME}-implant", artkit.quantize_set(imps[:5], 32), SOURCE)
        kit.write(f"{ROOT_NAME}-implant-glow",
                  artkit.quantize_set([glow_frame(g, halo=1.6, k=0.8) for g in imps[5:]], 16), SOURCE)
    if "fx" in groups:
        kit.write(f"{ROOT_NAME}-arc", [arc_frame(i) for i in range(4)], SOURCE)
        kit.write(f"{ROOT_NAME}-shed", [shed_frame(i) for i in range(6)], SOURCE)
        kit.write(f"{ROOT_NAME}-silt", artkit.quantize_set([silt_frame(i) for i in range(8)], 12), SOURCE)
        kit.write(f"{ROOT_NAME}-shadow-column", [shadow_column()], SOURCE, single=True)
        kit.write(f"{ROOT_NAME}-blowout", artkit.quantize_set([blowout_frame(i) for i in range(8)], 24), SOURCE)
        kit.write(f"{ROOT_NAME}-blowout-glow", [blowout_glow_frame(i) for i in range(8)], SOURCE)
        kit.write(f"{ROOT_NAME}-ichor", artkit.quantize_set([ichor_frame(i) for i in range(8)], 20), SOURCE)
        kit.write(f"{ROOT_NAME}-flare", [flare_frame(i) for i in range(4)], SOURCE)
    write_pivots()
    report_area()


def write_pivots():
    # the lime throat's centre as the open frames show it (40 px below the head's centre, the data's throat offset)
    throat = {"rest": (0, -0.67), "glow": (0, -0.67), "open": (0, -0.67), "rear": (0, -0.67), "dive": (0, -0.5),
              "lunge": (0, 0.67), "death": (0, -0.67)}
    eyes = [(-0.66, -0.25), (0.66, -0.25)]
    artkit.write_pivots(ROOT_NAME, SOURCE, {
        "head": {"frame": list(HEAD), "frames": list(HEAD_NAMES), "scale_px_per_unit": HEAD_PX,
                 "throat": [head_px(*throat[n]) for n in HEAD_NAMES],
                 "eyes": [head_px(*e) for e in eyes],
                 "rim_orbs": [head_px(x, y) for x, y in TEETH],
                 "note": "the model's centre is the frame's centre; the sacs and the neck implant are separate "
                         "sprites at the data's offsets from the head (the concept's: sacs (-62, -15), (62, -15), "
                         "(-50, 33), (50, 33) px, y down)"},
        "segment": {"frame": [SEG, SEG], "headings": HEADINGS, "tapers": list(TAPERS),
                    "index": "heading * 4 + taper; heading k = the way toward the head, k x 11.25 deg clockwise "
                             "from straight down",
                    "from_sim_angle": "k = round((pi/2 - segmentAngle) * 32 / (2 pi)) mod 32",
                    "taper_rule": "the last nine segments from the tail in three steps of three: tail tip taper 3",
                    "taper_of_31": [taper_of(i) for i in range(31)]},
        "sac": {"frame": [SAC, SAC]}, "implant": {"frame": [IMP, IMP]},
    })


def report_area():
    """The Maw's frames' atlas area (with the packer's 2 px padding) against one 2048 page."""
    total, n = 0, 0
    for folder in (kit.ASSETS_SPRITES, kit.HELD):
        for p in folder.glob(f"{ROOT_NAME}*.png"):
            w, h = Image.open(p).size
            total += (w + 2) * (h + 2)
            n += 1
    print(f"{ROOT_NAME}: {n} frames, {total} px = {total / 2048 ** 2:.2f} of an atlas page")


# --------------------------------------------------------------------------- the intel silhouette

def intel_mask():
    """The Maw as the arena shows it after its rise (phase 1): the coil round the edges and the head at the top, the
    production frames placed on the concept's path; an alpha mask (480x540) for tools/art/intel.py."""
    segs, heads, sacs = kit.load(f"{ROOT_NAME}-segment"), kit.load(f"{ROOT_NAME}-head"), kit.load(f"{ROOT_NAME}-sac")
    layer = Image.new("RGBA", (480, 540), (0, 0, 0, 0))
    for x, y, h, w, *_ in b48.coil_layout(b48.T_P1 + 1.0):
        kit.paste_c(layer, segs[seg_index(h, TAPERS.index(w))], x, y)
    for ox, oy in b48.SAC_OFF:
        kit.paste_c(layer, sacs[0], b48.HEAD_XY[0] + ox, b48.HEAD_XY[1] + oy)
    kit.paste_c(layer, heads[0], *b48.HEAD_XY)
    return np.array(layer)[..., 3] > 127


def seg_index(h_screen, ti):
    """The segment frame of the concept layout's heading (radians, screen, toward the head)."""
    k = kit.heading_index(np.cos(h_screen), np.sin(h_screen), HEADINGS)
    return k * 4 + ti


# =========================================================================== review

FPS = 15
T_DEATH = b48.T_END                      # the concept's 20 s compressed fight, then the death chain
T_STOP = T_DEATH + 5.2
ARENA_LT = kit.ARENA_T + 0.6             # the level clock while the arena is halted


def _arena_plate():
    import backdrop_l28 as b28                                          # noqa: E402  (the caldera, ART-H2's)
    img, glows, over = b28.ground_view(ARENA_LT, b28.placed_pieces("ground"), b28.placed_pieces("high-air"))
    base = kit.scroll(ARENA_LT)
    pools = []
    for s, x, r, col, note in b28.light_rows():
        y = base - 1 - s
        if -r < y < kit.SCREEN + r:
            pools.append((x, y, r, 1.0, kit._rgb(col)))
    return img, glows, pools


def sunk(img, d):
    """A part ``d`` below the play plane (the sub layer's stand-in): smaller, darker toward the water, fading."""
    if d <= 0.01:
        return img
    return b48.deep(img, d)


class Look:
    def __init__(self):
        L = kit.load
        self.seg, self.seg_g, self.seg_t = L(f"{ROOT_NAME}-segment"), L(f"{ROOT_NAME}-segment-glow"), \
            L(f"{ROOT_NAME}-segment-tell")
        self.head, self.head_g, self.rim = L(f"{ROOT_NAME}-head"), L(f"{ROOT_NAME}-head-glow"), \
            L(f"{ROOT_NAME}-head-rim")[0]
        self.sac, self.sac_g = L(f"{ROOT_NAME}-sac"), L(f"{ROOT_NAME}-sac-glow")
        self.imp, self.imp_g, self.arc = L(f"{ROOT_NAME}-implant"), L(f"{ROOT_NAME}-implant-glow"), \
            L(f"{ROOT_NAME}-arc")
        self.shed, self.silt = L(f"{ROOT_NAME}-shed"), L(f"{ROOT_NAME}-silt")
        self.sh_head, self.sh_col = L(f"{ROOT_NAME}-shadow-head")[0], L(f"{ROOT_NAME}-shadow-column")[0]
        self.blow, self.blow_g = L(f"{ROOT_NAME}-blowout"), L(f"{ROOT_NAME}-blowout-glow")
        self.ichor, self.flare = L(f"{ROOT_NAME}-ichor"), L(f"{ROOT_NAME}-flare")


def lit(img, t=ARENA_LT):
    """A sprite under the flat light of t's section (l28kit.lit_sprite's rule)."""
    amb, tint, air = kit.look_at(t)
    a = np.array(img.convert("RGBA")).astype(np.float64)
    a[..., :3] *= np.round(np.clip(amb * tint, 0, 1.4) * 12) / 12 * (1 - air) + air
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def _dim(img, k):
    a = np.array(img).astype(np.float64)
    a[..., :3] *= k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def fight_frames(lk):
    """The concept's compressed fight (its timeline, layout and shots, tools/concept/bosses_r48.py) redrawn with the
    production frames on Level 28's caldera, then the death chain."""
    ground, ground_glows, pools = _arena_plate()
    shots = b48.Shots(water=True)
    path = b48.SHIP_B
    for ts, sac in b48.SAC_RINGS:
        if sac == b48.SAC_KILL[0] and ts > b48.SAC_KILL[2]:
            continue
        sx, sy = b48.HEAD_XY[0] + b48.SAC_OFF[sac][0], b48.HEAD_XY[1] + b48.SAC_OFF[sac][1]
        shots.ring(ts + 0.6, sx, sy, 10, 100, "teal", 4, phase=ts)
    for tg, tf, tc, kind in b48.JAW:
        px, py = path(tf)
        if kind == "fan":
            shots.fan(tf, *b48.THROAT, np.arctan2(py - b48.THROAT[1], px - b48.THROAT[0]), 5, np.radians(40), 130,
                      "violet", 6)
        else:
            gx = path(tf + (470 - b48.THROAT[1]) / 77)[0]
            ga = np.arctan2(470 - b48.THROAT[1], gx - b48.THROAT[0])
            angs = np.linspace(-np.radians(75), np.radians(75), 17) + np.pi / 2
            shots.fan(tf, *b48.THROAT, np.pi / 2, 17, np.radians(150), 110, "teal", 4,
                      skip=(int(np.argmin(np.abs(angs - ga))),))
    for imp, tt, to, tc in b48.IMPL:
        ix, iy = b48.IMPLANT_XY[imp]
        for b in (to + 0.6, to + 1.6):
            if b < min(tc, b48.T_END):
                px, py = path(b)
                for j in range(3):
                    shots.fire(b + j * 0.12, ix, iy, px, py, 140, "bolt")
    col_x = float(np.clip(path(b48.T_SH)[0], 95, 385))
    frames = []
    for fi in range(int(T_STOP * FPS)):
        t = fi / FPS
        px, py = path(min(t, b48.T_END - 0.01))
        dark = b48.win(t, b48.T_DIVE + 0.2, b48.T_DIVE + 1.4) * (1 - b48.win(t, b48.T_RET, b48.T_RET + 1.0))
        death = max(0.0, t - T_DEATH)
        solids, glows, over = [], [], []
        # the lunge's shadow column
        if b48.T_SH <= t < b48.T_LUNGE + 0.3:
            k = b48.win(t, b48.T_SH, b48.T_LUNGE)
            top = kit.SCREEN - (kit.SCREEN + 60) * k
            for y in range(int(top) + 120, kit.SCREEN + 32, 32):
                solids.append((lk.sh_col, col_x, y))
            solids.append((lk.sh_head, col_x, top + 75))
            for j in range(4):
                solids.append((lk.silt[int(t * 10 + j * 3) % 8], col_x + (j - 1.5) * 18, top + 160 + j * 90))
        # the coil, tail first
        segs = b48.coil_layout(min(t, b48.T_END))
        head, col = b48.column_layout(t, col_x)
        allsegs = b48.with_fillers(segs) + col
        for idx, (x, y, h, w, dp, line, fl) in enumerate(allsegs):
            if death > 0:                                         # unwinding and sinking, tail to head
                order = idx / max(1, len(allsegs) - 1)
                ds = b48.win(death, 0.6 + order * 2.6, 1.3 + order * 2.6)
                x += 4 * np.sin(death * 14 + idx) * (1 - ds) * (death < 1.0)
                dp = max(dp, ds)
                if 0.05 < ds < 0.6:
                    over.append((lk.ichor[min(7, int((ds - 0.05) * 14))], x, y))
            if dp >= 0.98:
                continue
            fr = seg_index(h, TAPERS.index(w))
            solids.append((sunk(lk.seg[fr], dp), x, y))
            g = lk.seg_t[fr] if line else lk.seg_g[fr]
            glows.append((sunk(g, dp) if dp > 0.01 else g, x, y, 1.0 - dp))
            if line and b48.SWEEP[0] <= t < b48.SWEEP[0] + 1.2:
                solids.append((lk.silt[int(t * 10 + idx) % 8], x + 6, y - 4))
            if 0.05 < dp < 0.9 and t < b48.T_P1:
                solids.append((lk.silt[int(t * 10 + idx * 3) % 8], x, y - 8))
            if fl:
                glows.append((lk.seg_t[fr], x, y, 1.0))
                glows.append((lk.shed[min(4, int((t - b48.SHED[1]) * 5))], x, y, 1.0))
        # the head and its sacs
        hd = 1.0 - b48.win(t, 1.8, 2.5)
        hx, hy = b48.HEAD_XY
        if t >= b48.T_DIVE:
            hd = max(hd, b48.win(t, b48.T_DIVE, b48.T_DIVE + 1.4))
            hy -= 35 * b48.win(t, b48.T_DIVE, b48.T_DIVE + 1.4)
            if t >= b48.T_RET:
                hd = 1.0 - b48.win(t, b48.T_RET + 0.2, b48.T_RET + 1.0)
                hy = b48.HEAD_XY[1]
        hf = 0
        rim = False
        for tg, tf, tc, kind in b48.JAW:
            if tg <= t < tf:
                hf, rim = 1, kind == "wall"
            elif tf <= t < tc:
                hf = 2
        if b48.T_DIVE <= t < b48.T_RET:
            hf = 4
        if t >= b48.T_P3 - 0.4:
            hf = 3
        if death > 0:
            hf = 6
            hd = b48.win(death, 3.6, 4.6)
            hy = b48.HEAD_XY[1] + 40 * hd
        if hd < 0.98:
            for i, (ox, oy) in enumerate(b48.SAC_OFF):
                torn = i == b48.SAC_KILL[0] and t >= b48.SAC_KILL[2]
                sw = 0
                for ts, sac in b48.SAC_RINGS:
                    if sac == i and ts <= t < ts + 0.6:
                        sw = 1 if t - ts < 0.35 else 2
                sp = lk.sac[3] if torn else lk.sac[sw]
                if i == b48.SAC_KILL[0] and b48.SAC_KILL[1] <= t < b48.SAC_KILL[2] and int(t * FPS) % 3 == 0:
                    sp = b48.flash(sp)
                solids.append((sunk(sp, hd), hx + ox, hy + oy))
                if not torn:
                    glows.append((lk.sac_g[sw], hx + ox, hy + oy, 1 - hd))
            solids.append((sunk(lk.head[hf], hd), hx, hy))
            glows.append((lk.head_g[hf], hx, hy, (1 - hd) * (0.5 if death > 0 else 1.0)))
            if rim:
                glows.append((lk.rim, hx, hy, 1 - hd))
        if head is not None and head[1] > -110:
            solids.append((sunk(lk.head[5], head[2]), head[0], head[1]))
            glows.append((lk.head_g[5], head[0], head[1], 1 - head[2]))
        # phase 3: the implants
        if t >= b48.T_P3 - 0.4:
            for imp, (ix, iy) in enumerate(b48.IMPLANT_XY):
                st = 0
                arcing = False
                for j, tt, to, tc in b48.IMPL:
                    if j == imp and tt <= t < to:
                        st = 1 + int(t * 12) % 2
                    elif j == imp and to <= t < tc:
                        st = 3
                        arcing = any(b - 0.4 <= t < b for b in (to + 0.6, to + 1.6))
                if death > 0:
                    st = 4 if death >= 0.4 * imp else 0
                emerge = b48.win(t, b48.T_P3 - 0.4, b48.T_P3 + 0.2)
                solids.append((sunk(lk.imp[st], 1 - emerge), ix, iy))
                if 1 <= st <= 3:
                    glows.append((lk.imp_g[st - 1], ix, iy, 1.0))
                if st in (1, 2) or arcing:
                    glows.append((lk.arc[int(t * 16) % 4], ix, iy, 1.0))
                if death > 0 and 0 <= death - 0.4 * imp < 0.66:
                    f = int((death - 0.4 * imp) * 12)
                    over.append((lk.blow[f], ix, iy))
                    glows.append((lk.blow_g[f], ix, iy, 1.0))
        if death > 3.4:
            f = min(3, int((death - 3.4) * 2.5))
            fl = lk.flare[f].resize((307, 307), Image.Resampling.NEAREST)
            glows.append((fl, *kit.THROAT, 1.0))
        # the frame: lit, the dark of the dive, the glows ungraded
        gk = 1.0 - 0.75 * dark
        base = kit.frame(ground, ARENA_LT, solids=[], glows=[], ship_xy=None, pools=pools)
        if dark > 0:
            base = _dim(base, 1 - 0.7 * dark)
        lit_solids = kit.frame(Image.new("RGBA", base.size, (0, 0, 0, 0)), ARENA_LT, solids=solids, glows=[],
                               ship_xy=(px, py), pools=pools)
        base.alpha_composite(_dim(lit_solids, 1 - 0.4 * dark) if dark > 0 else lit_solids)
        for g, x, y, k in ground_glows:
            base = kit.add_c(base, g, x, y, k * gk * (1 + (1.2 if death > 3.4 else 0) * b48.win(death, 3.4, 3.9)))
        for g, x, y, k in glows:
            if k > 0.01:
                base = kit.add_c(base, g, x, y, k)
        for img, x, y in over:
            kit.paste_c(base, img, x, y)
        shots.draw(base, t)
        frac = 1.0
        for (a, b, f0, f1) in ((b48.T_P1, b48.T_P2, 1.0, 0.70), (b48.T_P2, b48.T_DIVE, 0.70, 0.50),
                               (b48.T_DIVE, b48.T_RET, 0.50, 0.46), (b48.T_RET, b48.T_P3, 0.46, 0.30),
                               (b48.T_P3, b48.T_END, 0.30, 0.0)):
            if t >= a:
                frac = f0 + (f1 - f0) * min(1.0, (t - a) / (b - a))
        if death < 0.4:
            b48.boss_bar(base, "ABYSSAL MAW", frac * b48.win(t, 1.8, 2.5) if t < b48.T_P1 else frac,
                         alpha=b48.win(t, 1.5, 1.8))
        lab = ("INTRO - THE COIL RISES ROUND THE EDGES, THE HEAD LAST" if t < b48.T_P1 else
               "PHASE 1 - GILL RINGS, THROAT FAN, A SAC SHOT OFF" if t < b48.T_P2 else
               "PHASE 2 - FAN WALLS, A SWEEP (LEFT), A SHED (LEFT)" if t < b48.T_DIVE else
               "THE DIVE - INTO THE DARK, THE SHADOW COLUMN" if t < b48.T_LUNGE else
               "THE LUNGE - UP THE COLUMN, THE BODY A WALL" if t < b48.T_RET else
               "BACK ROUND THE EDGES" if t < b48.T_P3 else
               "PHASE 3 - REARED, THE HELIX IMPLANTS SPARK OPEN" if t < T_DEATH else
               "DEATH - IMPLANTS BLOW, THE COIL SINKS, THE VENT FLARES")
        b48.phase_label(base, lab)
        raster.draw_text(base, 8, 26, "ABYSSAL MAW FINAL - T={:.1f}".format(t), raster.LABEL)
        frames.append(base.convert("RGB"))
    return frames


def review():
    lk = Look()
    up = HEADINGS // 2
    rows = [
        ("HEAD 0-3: REST, THROAT GLOW, OPEN, REARED (PHASE 3)", lk.head[:4], 1, False),
        ("HEAD 4-6: DIVE, LUNGE, DEAD; THE LUNGE'S SHADOW TIP", lk.head[4:] + [lk.sh_head], 1, False),
        ("HEAD GLOW 0-3 (ADDITIVE)", lk.head_g[:4], 1, True),
        ("HEAD GLOW 4-6 AND THE JAW RIM'S ORBS (WALL TELL)", lk.head_g[4:] + [lk.rim], 1, True),
        ("HEAD UNDER THE ARENA'S LIGHT (STAND-IN) WITH ITS GLOW, 0-5", [
            kit.add_c(lit(h), g, h.width / 2, h.height / 2) for h, g in
            zip(lk.head[:6], lk.head_g[:6])], 1, False),
        ("SEGMENT, 32 HEADINGS (FULL WIDTH), EVERY 2ND", lk.seg[0::8], 1, False),
        ("SEGMENT TAPERS AT HEADING 0 AND 8 (THE TAIL'S LAST NINE)", lk.seg[0:4] + lk.seg[32:36], 2, False),
        ("SEGMENT GLOW (TEAL) AND TELL (LIME: SWEEP, SHED) AT HEADING 8", [lk.seg_g[32], lk.seg_t[32], lk.seg_g[34],
                                                                         lk.seg_t[34]], 2, True),
        ("GILL SAC: IDLE, SWELL, FULL, TORN; GLOW", lk.sac + lk.sac_g, 2, False),
        ("HELIX IMPLANT: SHUT, SPARK A/B, OPEN, BLOWN; GLOW; ARC", lk.imp + lk.imp_g + lk.arc, 2, False),
        ("SHED FLARE (LIME, ADDITIVE)", lk.shed, 1, True),
        ("SILT; LUNGE SHADOW TIP AND COLUMN", lk.silt + [lk.sh_head, lk.sh_col], 1, False),
        ("IMPLANT BLOW-OUT AND ITS LIGHT", lk.blow + lk.blow_g[0::2], 1, False),
        ("SINKING SEGMENT'S ICHOR BURST", lk.ichor, 1, False),
        ("THE VENT'S FLARE (ADDITIVE)", lk.flare, 1, True),
    ]
    del up
    sheet = artkit.review_sheet("ABYSSAL MAW - FINAL (ROUND 48 LOOK B, THE COILED GUARDIAN)", rows, width=1300,
                                batch=BATCH)
    frames = fight_frames(lk)
    picks = [(2.2, "RISING"), (4.6, "PH1 RINGS, FAN"), (8.4, "PH2 SWEEP, WALL"), (13.4, "THE SHADOW"),
             (14.15, "THE LUNGE"), (18.2, "PH3 IMPLANTS"), (T_DEATH + 0.9, "BLOW-OUT"), (T_DEATH + 3.9, "SINKING")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 600), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    y0 = sheet.height
    raster.draw_text(big, 16, y0, "AT 1/2 ON LEVEL 28'S CALDERA UNDER THE HATCHERY LIGHT'S STAND-IN (THE DIVE'S DARK A "
                     "PLAIN DIM; THE GAME'S IS DARKLIGHT); THE CONCEPT'S 130 S FIGHT IN 20 S", raster.LABEL)
    for i, (tt, cap) in enumerate(picks):
        x = 16 + (i % 4) * 318
        y = y0 + 14 + (i // 4) * 290
        big.alpha_composite(frames[int(tt * FPS)].convert("RGBA").resize((240, 270), Image.Resampling.BOX), (x, y))
        raster.draw_text(big, x, y + 274, cap, raster.LABEL_DIM)
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, ROOT_NAME,
                       fps=FPS)


SETS = ("segment", "segment-glow", "segment-tell", "head", "head-glow", "head-rim", "shadow-head", "sac", "sac-glow",
        "implant", "implant-glow", "arc", "shed", "silt", "shadow-column", "blowout", "blowout-glow", "ichor", "flare")
SINGLE = ("head-rim", "shadow-head", "shadow-column")


def relocate():
    """Re-write every written set through l28kit's rule without rendering: once the boss's data.yaml claims the root,
    the held frames move from build/art-held/sprites/ into assets/sprites/ (the files are unchanged)."""
    for s in SETS:
        name = f"{ROOT_NAME}-{s}"
        kit.write(name, kit.load(name), SOURCE, single=s in SINGLE)


def main(argv):
    if "--relocate" in argv:
        relocate()
        return
    groups = [a for a in argv if not a.startswith("--")]
    unknown = set(groups) - set(GROUPS)
    if unknown:
        sys.exit(f"unknown groups: {', '.join(sorted(unknown))} (of {', '.join(GROUPS)})")
    if "--review" not in argv:
        build(groups or GROUPS)
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
