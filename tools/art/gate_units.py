#!/usr/bin/env python3
"""Production art: the units and objects of M11 part B, Levels 43 and 44 (step ART-B; final review in concept round
89): the Gate Warden (round 84 A.1 a "idol"), the Rift Skater (round 84 A.2 a "manta"), Level 43's glyph stones and the
Helix pod, Level 44's gold shard. The models are round 84's (tools/concept/enemies_r84.py ``warden_body``,
``shell_scene``, ``warden_mats``, ``skater_model``, imported unchanged) rendered through the production path (8x up to
64 px, 4x above, 1-bit alpha, one palette per unit); the objects are new models on the frozen render package in the
same stone (Vrell gate basalt, ``warden_mats``). The window shell's spark and shatter and the teleport fade and ring are
ART-A's (tools/art/act7_gear.py ``shell-*``, ``teleport-*``): the Warden and the Skater use those, nothing here
repeats them. Every glow is a smooth float light field written unquantised (no ordered dither, far more than 32 steps)
and fades to 0 inside its canvas (``--check`` fails on a lit border pixel of an additive set).

Outputs (assets/sprites/ once the data claims the root, else build/art-held/sprites/; ``additive`` = premultiplied on
black, blended GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha; steps = game steps of 1/60 s):

  Gate Warden (design/enemies/space/gate-warden; fixed, facing down; every frame centred on the unit's centre)
  gate-warden_0..5           120x132 normal: the body under the six-petal shell opening like an iris, open 0, 0.2, 0.4,
                             0.6, 0.8, 1.0 (frame 0 = closed: the pale gold-white egg with its gold seams; 0.15 s
                             iris: 1-2 steps a frame); the glyphs and the eye dim in the stone (their light is
                             ``-glyphs``)
  gate-warden-bare           120x132 normal: the body without its shell (broken by a breaker hit or the EMP; the
                             shatter itself is ART-A's ``shell-break``)
  gate-warden-regrow_0..7    120x132 normal: the shell growing back over the bare body in 0.4 s (3 steps a frame), its
                             petals rising from 0 to full opacity behind a bright silver-gold shimmer band climbing
                             over them; draw over ``-bare``, then ``gate-warden_0``
  gate-warden-sweep_0..11    120x132 additive: the closed shell's slow specular sweep, a warm white band crossing the
                             egg diagonally, one pass in 2.5 s (12.5 steps a frame), only on the closed frame
  gate-warden-glyphs         120x132 additive: the glyph band and the eye burning crimson at strength 1 with a soft
                             halo; the game scales it (closed ~0.18, open ~0.8, the leaving tell dimmed and flickered)
  gate-warden-eye_0..7       32x32 additive, centred on the eye (the sprite's centre, pupil offset 2.5 px toward the
                             aim by the game): the telegraph's charge over 0.8 s (6 steps a frame), a crimson glow
                             growing round a white-hot core; frame 7 held through the beam
  gate-warden-tilt_0..3      120x132 normal: the closed Warden tilted 4, 8, 12, 16 degrees, its top toward the top edge
                             (the leaving tell's last 2 s: it rises out through the top edge)
  gate-warden-tilt-glyphs_0..3  120x132 additive: the glyph light of each tilt frame (strength 1, dimmed and flickered by
                             the game in the tell)
  gate-warden-slabs_0..15    128x144 normal: the death: the body split into three stone slabs drifting apart and
                             falling (down the screen), turning, fading, each slab's glyphs going dark in turn (slab
                             1 at once, 2 by frame 4, 3 by frame 8); 0.8 s (3 steps a frame), played over the
                             ``medium`` vacuum burst; the shell is gone (``shell-break`` at the death)
  pivots/gate-warden.json    the eye and the shell's centre per frame
  Rift Skater (design/enemies/space/rift-skater; 16 angles, facing its glide)
  rift-skater_0..15          48x48 normal: the rust chitin manta, its thin pale lilac crystal rim and the violet core,
                             heading k = k x 22.5 degrees clockwise from moving down the screen
  rift-skater-glow           24x24 additive, centred on the unit: the violet core's soft light (drawn over the body)
  rift-skater-glint_0..5     24x24 additive, centred on the unit: the burst's tell, a violet-white four-point glint
                             growing and fading over the 0.25 s before the first shot (2-3 steps a frame)
  rift-skater-tatters_0..11  64x64 normal: the death, four chitin flakes with crystal edges splintering out, turning
                             and fading (0.6 s, 3 steps a frame)
  rift-skater-death_0..11    64x64 additive: the flakes' violet glow and a faint violet haze, decaying from the first
                             frame (never a flash); with ``-tatters``
  Level 43 objects (design/campaign/act-7-beyond-the-gate/level-43-the-tether-gate; vacuum: chips, no smoke)
  glyph-stone_0..2           48x48 normal: a squat block of the gate's basalt on the ring's surface, one crimson glyph
                             cut in its face (dimmer than a Warden's, never pulsing): intact, cracked (a split along
                             the glyph, the glyph dimmer), split (the halves parted, the glyph dark)
  glyph-stone-break_0..7     72x72 normal: destroyed: the two halves sliding apart along the glyph, stone chips flung
                             out in a ring, fading (0.4 s, 3 steps a frame)
  debris-helix-pod_0..15     32x32 normal: the Helix survey's beacon pod (the secret), charcoal with gold bands and
                             the gold Helix hex, its beacon lamp dead, tumbling (16 turns)
  debris-helix-pod-break_0..7  64x64 normal: shot open: shell chips and gold fragments flung out, a brief hot core
  Level 44 object (design/campaign/act-7-beyond-the-gate/level-44-crossing)
  debris-gold-shard_0..15    32x32 normal: a torn plate of the Ascendancy's gloss gold hull (Vorne's lifeboat), black
                             panel lines and a scorched torn edge, tumbling (16 turns, rocking)
  debris-gold-shard-break_0..7  64x64 normal: shot apart: gold fragments flung out, a brief hot core

The intel pictures (``gate-warden``, ``rift-skater``, 30x30) are tools/art/intel.py's, from ``warden_intel_hi`` and
``skater_intel_hi`` here.

Held sprites (tools/art/belt_units.py's rule): a root no data claims yet goes to build/art-held/sprites/ (and its pivot
file to build/art-held/pivots/): ``gate-warden-*`` and ``rift-skater-*`` until their enemy data.yaml exists (step B2),
``glyph-stone-*`` until a level's data names the sprite (B2's ground objects; SpriteUse must claim the root: step B4),
``debris-helix-pod-*`` and ``debris-gold-shard-*`` until a level's ``debris`` names the chunks ``helix-pod`` and
``gold-shard``. Rerun the script then.

Review sheets and GIFs (round 89): design/enemies/space/gate-warden/concept/gate-warden-final-r89-a,
design/enemies/space/rift-skater/concept/rift-skater-final-r89-a and the levels' concept/l43-props-final-r89-a and
l44-props-final-r89-a, on round 84's Level 43 and 44 stand-ins in the one Vrell palette, at 1x and 2x.

Run: python3 tools/art/gate_units.py [warden] [skater] [props] [--review] [--check]
     (no part = all, ~2 min; --review only rebuilds the sheets from the written frames; --check lists every set)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: one_palette, the stand-in stills)
import enemies_r84 as u84  # noqa: E402  (concept: the round 84 models, imported unchanged)
from render import raster  # noqa: E402
from render.sdf import Material, rotate_x, rotate_z, sd_box, sd_capsule, sd_plate, sd_sphere, union  # noqa: E402

SCRIPT = "gate_units.py"
BATCH = "M11 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r89"
artkit.REVIEW_ROUND = ROUND
L43 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-43-the-tether-gate" / "concept"
L44 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-44-crossing" / "concept"
CONCEPT = {"warden": DESIGN / "enemies" / "space" / "gate-warden" / "concept",
           "skater": DESIGN / "enemies" / "space" / "rift-skater" / "concept", "props": L43}
ADDITIVE = {"gate-warden-sweep", "gate-warden-glyphs", "gate-warden-tilt-glyphs", "gate-warden-eye", "rift-skater-glow", "rift-skater-glint",
            "rift-skater-death"}
CRIMSON = np.array((255, 48, 56), float)
VIOLET = np.array((154, 77, 255), float)
VIOLET_W = np.array((226, 206, 255), float)
GOLD_W = np.array((255, 240, 200), float)

WD = (120, 132)                      # the Warden's frames (its egg 74x82 at 38 px a unit, the open petals 110x124)
WD_S = u84.W_S
WD_EXT = WD[0] / WD_S
OPEN = (0.0, 0.2, 0.4, 0.6, 0.8, 1.0)
TILT = (4, 8, 12, 16)
SLAB = (128, 144)
SK = 48
SK_EXT = u84.SK_EXT


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    folder = artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    (other / f"{root}.json").unlink(missing_ok=True)


def smoothstep(a, b, x):
    t = np.clip((np.asarray(x, float) - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def additive_rgb(light):
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def rgba(rgb, alpha):
    a = np.clip(np.round(np.asarray(alpha, float) * 255), 0, 255)
    rgb = np.where(a[..., None] > 0, np.clip(np.round(rgb), 0, 255), 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def glow(light, x, y, r, col, k):
    h, w = light.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    light += (np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k)[..., None] * np.asarray(col, float)


def blur(a, sigma):
    if sigma <= 0:
        return a
    r = int(np.ceil(3 * sigma))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, ((r, r), (0, 0)) + ((0, 0),) * (a.ndim - 2))
    a = sum(k[i] * p[i:i + a.shape[0]] for i in range(2 * r + 1))
    p = np.pad(a, ((0, 0), (r, r)) + ((0, 0),) * (a.ndim - 2))
    return sum(k[i] * p[:, i:i + a.shape[1]] for i in range(2 * r + 1))


def edge_fade(light, margin=3):
    """A light field multiplied to 0 over its canvas's outer ``margin`` px (every glow fades out inside its canvas)."""
    h, w = light.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    d = np.minimum.reduce([xx, w - xx, yy, h - yy])
    return light * smoothstep(0.5, margin + 0.5, d)[..., None]


# =========================================================================== render jobs

def _warden_scene(part, open_=0.0, glow_=0.25, tilt=0.0):
    scene, mats = u84.warden_make("a", part, open_=open_, glow=glow_)
    if tilt:
        base, a = scene, np.radians(tilt)
        scene = lambda p: base(rotate_x(p, a))  # noqa: E731
    return scene, mats


def _emissive(mats):
    """The glyph band's and the eye's emission alone (every other surface black)."""
    out = bu.emission_only(mats)
    return [m if i in (u84.A_GLY, u84.A_EYE) else Material((0, 0, 0)) for i, m in enumerate(out)]


def _warden(kind, *args):
    if kind == "open":
        scene, mats = _warden_scene("all", open_=args[0])
    elif kind == "bare":
        scene, mats = _warden_scene("body")
    elif kind == "shell":
        scene, mats = _warden_scene("shell")
    elif kind == "glyphs":
        scene, mats = _warden_scene("body", glow_=1.0)
        mats = _emissive(mats)
    elif kind == "tilt":
        scene, mats = _warden_scene("all", tilt=args[0])
    elif kind == "tilt-glyphs":
        scene, mats = _warden_scene("body", glow_=1.0, tilt=args[0])
        mats = _emissive(mats)
    else:
        raise ValueError(kind)
    return bu.render(*bu.turned(scene, mats, 0, 16), WD, WD_EXT)


SLAB_CELL = 72


def _slab_scene(k, spin, glow_):
    """Slab k of 3 (a 120 degree sector of the body round its centre) turned by ``spin`` about its own middle."""
    body, mats = _warden_scene("body", glow_=glow_)
    a0 = -np.pi + k * TAU / 3 + 0.4
    mid = a0 + TAU / 6
    c = np.array((np.cos(mid) * 0.32, np.sin(mid) * 0.32, 0.0))

    def scene(p):
        q = rotate_z(p - c, spin) + c
        d, m = body(q)
        ang = np.arctan2(q[:, 1], q[:, 0])
        rel = (ang - a0) % TAU
        dist_edge = np.minimum(rel, TAU / 3 - rel)
        r = np.hypot(q[:, 0], q[:, 1])
        sector = np.where(rel < TAU / 3, -dist_edge * np.maximum(r, 0.05), (np.minimum(np.abs(rel - TAU / 3), np.abs(TAU - rel))) * np.maximum(r, 0.05))
        return np.maximum(d, sector - 0.012), m
    return scene, mats, c


def _slab(k, spin, glow_):
    scene, mats, c = _slab_scene(k, spin, glow_)
    shifted = lambda p: scene(p + c)  # noqa: E731  (render the slab centred in its cell)
    return bu.render(*bu.turned(shifted, mats, 0, 16), SLAB_CELL, SLAB_CELL / WD_S)


def _skater(k):
    scene, mats = u84.skater_model("a")
    return bu.render(*bu.turned(scene, mats, k, 16), SK, SK_EXT)


# --- objects

def stone_mats(glyph=0.35):
    mats = u84.warden_mats(glyph)
    return mats


def stone_scene(split=0.0, crack=False):
    """The glyph stone: a squat rounded block of basalt (34x30 px top face, 0.45 model units high at 38 px a unit) with
    one glyph cut across its face (a crimson groove: a long stroke with a short cross stroke and a dot); ``split``
    parts the two halves along the glyph's stroke (model units), ``crack`` opens a dark crack along it."""
    def half(p, side):
        q = p.copy()
        q[:, 0] -= side * split
        d = sd_box(q, (0, 0, 0.1), (0.46, 0.42, 0.2), 0.09)
        if side:
            d = np.maximum(d, -side * q[:, 0])
        groove = np.minimum.reduce([sd_box(q, (0.0, 0.0, 0.3), (0.045, 0.3, 0.06), 0.01),
                                    sd_box(q, (0.0, 0.1, 0.3), (0.2, 0.04, 0.06), 0.01),
                                    sd_sphere(q, (0.12, -0.2, 0.3), 0.06)])
        d2 = np.maximum(d, -groove)
        m = np.where(groove < 0.025, u84.A_GLY, u84.A_ST)
        m = np.where((m == u84.A_ST) & (q[:, 2] < 0.0), u84.A_STD, m)
        return d2, m

    def scene(p):
        if split > 0 or crack:
            return union(half(p, -1), half(p, 1))
        return half(p, 0)
    return scene


def _stone(state):
    if state == 0:
        scene, glyph = stone_scene(), 0.35
    elif state == 1:
        scene, glyph = stone_scene(crack=True, split=0.02), 0.18
    else:
        scene, glyph = stone_scene(split=0.09), 0.0
    return bu.render(*bu.turned(scene, stone_mats(glyph), 0, 16), 48, 48 / WD_S)


def _stone_half(side, spin):
    base = stone_scene(split=0.0001)

    def scene(p):
        q = rotate_z(p, spin)
        d, m = base(q)
        return np.maximum(d, side * q[:, 0] + 0.0), m
    return bu.render(*bu.turned(scene, stone_mats(0.0), 0, 16), 40, 40 / WD_S)


PX = 20.0                                 # the debris props' scale (Level 36's pod)
HELIX = [Material((0.16, 0.16, 0.17), metal=0.4, shininess=60, spec=0.55),
         Material((0.9, 0.68, 0.24), metal=0.85, shininess=110, spec=1.1),
         Material((0.07, 0.07, 0.08), metal=0.4, shininess=30, spec=0.3),
         Material((0.22, 0.12, 0.04), metal=0.3, shininess=80, spec=0.8)]


def helix_pod_scene():
    """The Helix survey's beacon pod, long axis +Y: a charcoal capsule, gold bands at both ends and its middle, a gold
    hex plate on its back, two short stub fins, the dead amber beacon lamp at its nose."""
    def scene(p):
        y = p[:, 1]
        body = sd_capsule(p, (0, -0.38, 0.0), (0, 0.36, 0.0), 0.3, 0.27)
        fins = sd_box(p, (0, -0.5, 0.0), (0.42, 0.07, 0.03), 0.02)
        hexp = sd_plate(p, [(0.17 * np.cos(a), 0.17 * np.sin(a)) for a in np.arange(6) * TAU / 6], 0.27, 0.02, 0.01)
        lamp = sd_sphere(p, (0, 0.62, 0.02), 0.08)
        d, m = union((body, 0), (fins, 2), k=0.04)
        d, m = union((d, m), (hexp, 1), (lamp, 3), k=0.0)
        band = (np.abs(y) < 0.05) | (np.abs(np.abs(y + 0.01) - 0.5) < 0.05)
        m = np.where((m == 0) & band, 1, m)
        return d, m
    return scene


GOLD_SHARD = [Material((0.95, 0.72, 0.26), metal=0.9, shininess=140, spec=1.3),
              Material((0.05, 0.05, 0.06), metal=0.6, shininess=120, spec=1.0),
              Material((0.16, 0.1, 0.06), metal=0.2, shininess=20, spec=0.2)]


def gold_shard_scene(rock):
    """A torn plate of the Ascendancy's gold hull: an irregular bent plate, two black panel lines across it, its torn
    edge scorched; ``rock`` tips it about its long axis (the tumble)."""
    poly = [(-0.55, 0.62), (-0.1, 0.72), (0.28, 0.52), (0.6, 0.6), (0.5, 0.1), (0.62, -0.28), (0.3, -0.42),
            (0.12, -0.7), (-0.3, -0.5), (-0.6, -0.56), (-0.48, -0.1), (-0.64, 0.24)]

    def scene(p):
        q = rotate_x(p, rock)
        z = 0.1 * q[:, 0] ** 2 - 0.05
        qq = q.copy()
        qq[:, 2] = q[:, 2] - z
        d = sd_plate(qq, poly, 0.0, 0.05, 0.015)
        e = u84.sd_polygon2(q[:, 0], q[:, 1], poly)
        m = np.where(e > -0.08, 2, 0)
        m = np.where((m == 0) & ((np.abs(q[:, 1] - 0.18) < 0.03) | (np.abs(q[:, 1] + 0.24) < 0.03)), 1, m)
        return d, m
    return scene


def _pod(k):
    return bu.render(*bu.turned(helix_pod_scene(), HELIX, k, 16), 32, 32 / PX)


def _shard(k):
    scene = gold_shard_scene(0.5 * np.sin(k * TAU / 16 * 2))
    return bu.render(*bu.turned(scene, GOLD_SHARD, k, 16), 32, 32 / PX)


JOBS = {"warden": _warden, "slab": _slab, "skater": _skater, "stone": _stone, "half": _stone_half, "pod": _pod,
        "shard": _shard}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


# =========================================================================== Gate Warden

def shell_alpha_mask(shell):
    return np.array(shell)[..., 3] > 0


def regrow_frames(shell, n=8):
    """The closed shell rising from nothing in 0.4 s behind a silver-gold shimmer band climbing over it."""
    a = np.array(shell).astype(np.float64)
    h, w = a.shape[:2]
    on = a[..., 3] > 0
    ys = np.where(on.any(axis=1))[0]
    top, bot = ys.min(), ys.max()
    yy = np.mgrid[0:h, 0:w][0].astype(np.float64)
    frames = []
    for f in range(n):
        u = (f + 1) / n
        front = bot - (bot - top + 12) * u                    # the band climbs from the bottom to over the top
        band = np.exp(-((yy - front) / 4.0) ** 2)
        below = smoothstep(front - 10, front + 14, yy)        # the shell grown in below the band
        alpha = on * np.clip(0.15 + 0.85 * below, 0, 1) * (0.35 + 0.65 * u)
        alpha = np.maximum(alpha, on * band * 0.9)
        rgb = a[..., :3] + band[..., None] * (np.array((255, 246, 214.0)) - a[..., :3]) * 0.75
        frames.append(rgba(rgb, np.where(on, alpha, 0)))
    return frames


def sweep_frames(shell, n=12):
    a = np.array(shell)
    on = (a[..., 3] > 0).astype(np.float64)
    h, w = on.shape
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    ys, xs = np.where(on > 0)
    u = ((xx - xs.min()) / (xs.max() - xs.min()) + (yy - ys.min()) / (ys.max() - ys.min())) / 2
    frames = []
    for f in range(n):
        ph = f / n * 1.5 - 0.25
        k = np.exp(-((u - ph) / 0.08) ** 2) * 0.42
        lit = blur(on, 0.6)
        light = (k * lit)[..., None] * np.array((255, 246, 222.0))
        frames.append(additive_rgb(edge_fade(light)))
    return frames


def glyph_light(em):
    """The emissive pixels of the glyph band and the eye as light, with a soft crimson halo (strength 1)."""
    a = np.array(em).astype(np.float64)
    core = a[..., :3] * (a[..., 3:4] > 0)
    halo = blur(core.max(axis=-1), 3.0)
    light = core * 1.1 + halo[..., None] * CRIMSON / 255.0 * 0.9
    return additive_rgb(edge_fade(light, 4))


def eye_frames(n=8, size=32):
    frames = []
    for f in range(n):
        u = (f + 1) / n
        light = np.zeros((size, size, 3))
        c = size / 2
        glow(light, c, c, 3.0 + 4.0 * u, CRIMSON, 0.35 + 0.55 * u)
        glow(light, c, c, 1.6 + 0.6 * u, (255, 226, 210), 0.3 + 0.8 * u ** 1.5)
        frames.append(additive_rgb(edge_fade(light, 4)))
    return frames


SLAB_SPIN = (0.5, -0.7, 0.35)
SLAB_DARK = (0, 4, 8)                 # the frame by which each slab's glyphs are dark


def slab_jobs(n=16):
    jobs = []
    for f in range(n):
        s = f / (n - 1)
        for k in range(3):
            g = 0.9 * (1 - smoothstep(SLAB_DARK[k] - 3, SLAB_DARK[k], f)) if SLAB_DARK[k] else 0.0
            jobs.append(("slab", k, SLAB_SPIN[k] * s, g))
    return jobs


def slab_frames(res, n=16):
    frames = []
    for f in range(n):
        s = f / (n - 1)
        img = Image.new("RGBA", SLAB, (0, 0, 0, 0))
        for k in range(3):
            a0 = -np.pi + k * TAU / 3 + 0.4
            mid = a0 + TAU / 6
            piece = res[f * 3 + k]
            x = SLAB[0] / 2 + np.cos(mid) * (0.32 * WD_S + 22 * s)
            y = SLAB[1] / 2 - 12 + np.sin(mid) * (0.32 * WD_S + 14 * s) + 44 * s * s
            fade = (1 - smoothstep(0.45, 1.0, s))
            arr = np.array(piece).astype(np.float64)
            arr[..., :3] *= 1 - 0.35 * s
            arr[..., 3] *= fade
            pi = rgba(arr[..., :3], arr[..., 3] / 255)
            img.alpha_composite(pi, (int(round(x - SLAB_CELL / 2)), int(round(y - SLAB_CELL / 2))))
        frames.append(img)
    return frames


def build_warden():
    jobs = ([("warden", "open", o) for o in OPEN] + [("warden", "bare"), ("warden", "shell"), ("warden", "glyphs")]
            + [("warden", "tilt", t) for t in TILT] + [("warden", "tilt-glyphs", t) for t in TILT] + slab_jobs())
    res = run(jobs)
    opened, bare, shell, em = res[:6], res[6], res[7], res[8]
    tilts, tilt_em, slabs = res[9:13], res[13:17], res[17:]
    solid = artkit.quantize_set(opened + [bare] + tilts, 48)
    save_set("gate-warden", solid[:6])
    save_set("gate-warden-bare", [solid[6]], single=True)
    save_set("gate-warden-tilt", solid[7:])
    save_set("gate-warden-tilt-glyphs", [glyph_light(e) for e in tilt_em])
    save_set("gate-warden-regrow", regrow_frames(shell))
    save_set("gate-warden-sweep", sweep_frames(shell))
    save_set("gate-warden-glyphs", [glyph_light(em)], single=True)
    save_set("gate-warden-eye", eye_frames())
    save_set("gate-warden-slabs", artkit.quantize_set(slab_frames(slabs), 40))
    c = [WD[0] / 2, WD[1] / 2]
    write_pivots("gate-warden", {"note": "px from each frame's top left; the eye is the laser-line's emitter and the "
                                         "telegraph eye's centre (the game turns the pupil 2.5 px toward the aim)",
                                 "gate-warden": [{"eye": c, "centre": c} for _ in OPEN],
                                 "gate-warden-tilt": [{"eye": [c[0], round(c[1] - 0.55 * WD_S * np.sin(np.radians(t)), 1)],
                                                       "centre": c} for t in TILT]})


# =========================================================================== Rift Skater

def skater_glow(size=24):
    light = np.zeros((size, size, 3))
    glow(light, size / 2, size / 2, 3.2, VIOLET, 0.55)
    glow(light, size / 2, size / 2, 1.4, VIOLET_W, 0.35)
    return additive_rgb(edge_fade(light, 4))


def glint_frames(n=6, size=24):
    frames = []
    yy, xx = np.mgrid[0:size, 0:size].astype(np.float64) + 0.5
    c = size / 2
    for f in range(n):
        u = (f + 0.5) / n
        k = np.sin(np.pi * min(u * 1.25, 1.0)) if u < 0.8 else np.sin(np.pi * 1.0) + (1 - u) * 2.5 * 0.4
        k = max(k, 0.0)
        dx, dy = np.abs(xx - c), np.abs(yy - c)
        ray = (np.exp(-(dy / 0.7) ** 2) * np.exp(-dx / (3.5 + 3 * u)) + np.exp(-(dx / 0.7) ** 2) * np.exp(-dy / (3.5 + 3 * u)))
        light = ray[..., None] * (VIOLET_W * 0.6 + 100) * 1.2 * k
        glow(light, c, c, 2.4, VIOLET, 0.7 * k)
        glow(light, c, c, 1.0, (255, 255, 255), 0.6 * k)
        frames.append(additive_rgb(edge_fade(light, 4)))
    return frames


FLAKES = [(0.6, 1.0, 0.9), (2.2, 0.8, -1.2), (3.7, 1.1, 0.7), (5.2, 0.9, -0.8)]  # angle, speed, spin of each flake


def flake_pts(f, n, w, k):
    u = f / (n - 1)
    a, sp, spin = FLAKES[k]
    r = 5 + 24 * sp * np.sqrt(u)
    cx, cy = w / 2 + np.cos(a) * r, w / 2 + np.sin(a) * r
    s = a + spin * u * 6
    size = 7.0 - 2.0 * u
    pts = [(cx + np.cos(s) * size, cy + np.sin(s) * size), (cx + np.cos(s + 2.5) * size * 0.5, cy + np.sin(s + 2.5) * size * 0.5),
           (cx + np.cos(s + 3.8) * size * 0.45, cy + np.sin(s + 3.8) * size * 0.45)]
    return pts, (cx, cy), s, u


def tatters_frames(n=12, w=64):
    frames = []
    for f in range(n):
        hi = Image.new("RGBA", (w * 4, w * 4), (0, 0, 0, 0))
        d = ImageDraw.Draw(hi)
        for k in range(4):
            pts, _, s, u = flake_pts(f, n, w, k)
            lit = 0.55 + 0.45 * np.cos(s - 2.3)
            body = tuple(int(c) for c in np.array((60, 18, 10)) + np.array((150, 64, 36)) * lit)
            d.polygon([(x * 4, y * 4) for x, y in pts], fill=body + (255,), outline=(214, 198, 242, 255), width=3)
        arr = np.array(artkit.native(np.array(hi).astype(np.float64) / 255.0, 4)).astype(np.float64)
        fade = 1 - smoothstep(0.35, 1.0, f / (n - 1))
        frames.append(rgba(arr[..., :3], arr[..., 3] / 255 * fade))
    return frames


def death_light_frames(n=12, w=64):
    frames = []
    for f in range(n):
        light = np.zeros((w, w, 3))
        decay = (1 - f / n) ** 1.6
        glow(light, w / 2, w / 2, 7 + 10 * f / n, VIOLET, 0.35 * decay)
        for k in range(4):
            _, (cx, cy), _, u = flake_pts(f, n, w, k)
            glow(light, cx, cy, 3.0, VIOLET, 0.6 * decay)
            glow(light, cx, cy, 1.2, VIOLET_W, 0.4 * decay)
        frames.append(additive_rgb(edge_fade(light, 5)))
    return frames


def build_skater():
    res = run([("skater", k) for k in range(16)])
    save_set("rift-skater", artkit.quantize_set(res, 32))
    save_set("rift-skater-glow", [skater_glow()], single=True)
    save_set("rift-skater-glint", glint_frames())
    save_set("rift-skater-tatters", artkit.quantize_set(tatters_frames(), 24))
    save_set("rift-skater-death", death_light_frames())


# =========================================================================== objects

def chips(img, n, w, u, seed, cols, speed=(30, 70), size=(1.6, 3.4), fall=8.0):
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(img)
    for _ in range(n):
        ang, sp, sz, spin = rng.uniform(0, TAU), rng.uniform(*speed), rng.uniform(*size), rng.uniform(0, TAU)
        col = np.asarray(cols[rng.integers(len(cols))], float)
        dist = sp * 0.4 * (1 - (1 - u) ** 2)
        x, y = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + fall * u * u
        a = spin + u * 6
        pts = [(x + np.cos(a + k * 2.1) * sz, y + np.sin(a + k * 2.1) * sz * 0.75) for k in range(3)]
        lit = 0.55 + 0.45 * np.cos(a + 0.8)
        al = int(255 * (1 - smoothstep(0.55, 1.0, u)))
        if al > 0:
            d.polygon(pts, fill=tuple(int(c) for c in col * lit) + (al,))
    return img


def stone_break(halves, n=8, w=72):
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        img = Image.new("RGBA", (w, w), (0, 0, 0, 0))
        fade = 1 - smoothstep(0.45, 1.0, u)
        for side, half in zip((-1, 1), halves[f]):
            arr = np.array(half).astype(np.float64)
            arr[..., 3] *= fade
            x = w / 2 + side * (2 + 9 * u) - half.width / 2
            y = w / 2 + 4 * u * u - half.height / 2
            img.alpha_composite(rgba(arr[..., :3], arr[..., 3] / 255), (int(round(x)), int(round(y))))
        frames.append(chips(img, 12, w, u, 4301 + f * 0, [(110, 100, 120), (70, 64, 78), (150, 140, 156)]))
    return frames


def debris_break(cols, seed, n=8, w=64, hot=(255, 210, 140)):
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
        core = np.exp(-((xx - w / 2) ** 2 + (yy - w / 2) ** 2) / (3 + 5 * u) ** 2) * max(0.0, 1 - 3.0 * u)
        img = rgba(np.ones((w, w, 3)) * np.array(hot, float), np.clip(core * 0.9, 0, 1))
        frames.append(chips(img, 12, w, u, seed, cols, speed=(40, 90)))
    return frames


def build_props():
    res = run([("stone", s) for s in range(3)] + [("half", side, 0.5 * u) for u in np.linspace(0.06, 1, 8)
                                                  for side in (-1, 1)]
              + [("pod", k) for k in range(16)] + [("shard", k) for k in range(16)])
    stones, halves, pods, shards = res[:3], res[3:19], res[19:35], res[35:51]
    save_set("glyph-stone", artkit.quantize_set(stones, 32))
    save_set("glyph-stone-break", artkit.quantize_set(stone_break([halves[2 * i:2 * i + 2] for i in range(8)]), 32))
    save_set("debris-helix-pod", artkit.quantize_set(pods, 32))
    save_set("debris-helix-pod-break", debris_break([(60, 60, 64), (210, 160, 60), (40, 40, 44)], 4311))
    save_set("debris-gold-shard", artkit.quantize_set(shards, 32))
    save_set("debris-gold-shard-break", debris_break([(230, 176, 64), (120, 86, 30), (20, 20, 22)], 4411))


BUILDERS = {"warden": build_warden, "skater": build_skater, "props": build_props}


# =========================================================================== intel (tools/art/intel.py)

def warden_intel_hi():
    """The Gate Warden for the hangar intel's 30 px portrait (an 8x render, 240x240): the shell parting (0.2) over
    the body, its glyphs and eye lit."""
    from render import sdf  # noqa: E402
    scene, mats = bu.turned(*_warden_scene("all", open_=0.2, glow_=1.0), 0, 16)
    return sdf.render(scene, mats, (240, 240), 92 / WD_S)


def skater_intel_hi():
    """The Rift Skater nose down (heading 0) for the intel portrait (an 8x render, 240x240)."""
    from render import sdf  # noqa: E402
    scene, mats = bu.turned(*u84.skater_model("a"), 0, 16)
    return sdf.render(scene, mats, (240, 240), SK_EXT * 0.9)


# =========================================================================== review

def stand_in(level, w=480, h=540):
    """Round 84's level stand-in in the one Vrell palette (as ART-A's reviews)."""
    if level == 43:
        img = u84.gate_bg(4301, h)
    else:
        img = u84.EventPlane(4401, 1.0).frame(0.0)
    return a84.one_palette(img.convert("RGB")).convert("RGBA").crop((0, 0, w, h))


def paste(dst, img, x, y):
    return l23kit.paste_c(dst, img, x, y)


def addc(dst, img, x, y, k=1.0):
    return l23kit.add_c(dst, img, x, y, k)


def label(img, x, y, text):
    raster.draw_text(img, x, y, text.upper(), raster.LABEL)


def strip(frames, bg, cell, scale=2, add=None, under=None):
    """A row of frames over a crop of ``bg`` at ``scale``; ``add``: additive frames over each, ``under``: a frame
    under each (centred)."""
    out = Image.new("RGBA", (len(frames) * (cell[0] * scale + 6), cell[1] * scale), (0, 0, 0, 0))
    for i, fr in enumerate(frames):
        c = bg.crop((0, 0) + cell).copy()
        if under is not None:
            c = paste(c, under, cell[0] / 2, cell[1] / 2)
        c = paste(c, fr, cell[0] / 2, cell[1] / 2)
        if add is not None:
            c = addc(c, add[i] if isinstance(add, list) else add, cell[0] / 2, cell[1] / 2)
        out.alpha_composite(c.resize((cell[0] * scale, cell[1] * scale), Image.NEAREST), (i * (cell[0] * scale + 6), 0))
    return out


def sheet_of(title, rows, width=1000):
    hgt = 40 + sum(r.height + 30 for _, r in rows)
    sheet = raster.sheet(width, hgt, title, f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 40
    for text, r in rows:
        label(sheet, 12, y, text)
        if r.width > width - 24:
            r = r.resize((width - 24, int(r.height * (width - 24) / r.width)), Image.NEAREST)
        sheet.alpha_composite(r, (12, y + 12))
        y += r.height + 30
    return sheet.crop((0, 0, width, y))


def ImageEnhance_scale(img, k):
    a = np.array(img).astype(np.float64)
    a[..., :3] *= k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def review_warden():
    L = l23kit.load
    bg = stand_in(43)
    dark = bg.crop((300, 0, 480, 200)).resize((480, 540))
    wd, bare, rg, sw, gl, eye, tilt, slabs = (L("gate-warden"), L("gate-warden-bare")[0], L("gate-warden-regrow"),
                                              L("gate-warden-sweep"), L("gate-warden-glyphs")[0], L("gate-warden-eye"),
                                              L("gate-warden-tilt"), L("gate-warden-slabs"))
    levels = [0.18 + 0.62 * o for o in OPEN]
    rows = [("THE IRIS: CLOSED TO OPEN (0.15 S), THE GLYPH LIGHT AT THE GAME'S LEVEL", Image.new("RGBA", (1, 1)))]
    r = Image.new("RGBA", (6 * (WD[0] * 2 + 6), WD[1] * 2), (0, 0, 0, 0))
    for i, fr in enumerate(wd):
        c = paste(bg.crop((0, 0) + WD).copy(), fr, WD[0] / 2, WD[1] / 2)
        c = addc(c, gl, WD[0] / 2, WD[1] / 2, levels[i])
        r.alpha_composite(c.resize((WD[0] * 2, WD[1] * 2), Image.NEAREST), (i * (WD[0] * 2 + 6), 0))
    rows[0] = (rows[0][0], r)
    rows.append(("THE CLOSED SHELL'S SWEEP (2.5 S), EVERY OTHER FRAME", strip(wd[:1] * 6, bg, WD, add=sw[::2])))
    rows.append(("BROKEN: BARE, THEN THE SHELL REGROWING (0.4 S) BEHIND THE SHIMMER", strip(rg[::2] + [wd[0]], bg, WD, under=bare)))
    rows.append(("THE TELEGRAPH EYE (0.8 S) OVER THE OPEN WARDEN", strip([wd[5]] * 8, bg, WD, add=eye)))
    tg = [ImageEnhance_scale(g, 0.3) for g in L("gate-warden-tilt-glyphs")]
    rows.append(("THE LEAVING TELL: TILT 4-16 DEGREES, THE GLYPHS DIMMED", strip(tilt, bg, WD, add=tg)))
    rows.append(("THE DEATH: THREE SLABS, THE GLYPHS GOING DARK IN TURN (EVERY OTHER FRAME)",
                 strip(slabs[::2], bg.resize((480, 540)), SLAB, scale=1)))
    rows.append(("OVER THE LEVEL'S DARK BAND: CLOSED, OPEN WITH THE EYE (1X)", strip([wd[0], wd[5]], dark, WD, scale=1,
                                                                                      add=[ImageEnhance_scale(gl, 0.18), gl])))
    sheet = sheet_of("GATE WARDEN (FINAL R89): A 'IDOL'", rows)
    gif = []
    for f in range(48):
        img = bg.crop((96, 120, 384, 340)).copy()
        t = f / 12
        ph = t % 4.0
        o = 0 if ph < 1.6 else min(5, int((ph - 1.6) / 0.15 * 5)) if ph < 3.4 else max(0, 5 - int((ph - 3.4) / 0.15 * 5))
        img = paste(img, wd[o], 144, 110)
        img = addc(img, gl, 144, 110, 0.18 + 0.62 * o / 5)
        if o == 0:
            img = addc(img, sw[f % 12], 144, 110)
        if 1.6 <= ph < 2.4:
            img = addc(img, eye[min(7, int((ph - 1.6) / 0.1))], 144, 110)
        gif.append(img.resize((576, 440), Image.NEAREST))
    artkit.save_review(sheet, gif, CONCEPT["warden"], "gate-warden", fps=12)
    print("review: gate-warden-final-r89-a")


def review_skater():
    L = l23kit.load
    bg = stand_in(44)
    sk, gl, gn, ta, de = L("rift-skater"), L("rift-skater-glow")[0], L("rift-skater-glint"), L("rift-skater-tatters"), L("rift-skater-death")
    try:
        ring = L("teleport-ring")
    except FileNotFoundError:
        ring = None
    rows = [("16 HEADINGS (2X) WITH THE CORE GLOW", strip(sk, bg, (48, 48), add=[gl] * 16))]
    rows.append(("THE BURST'S GLINT (0.25 S)", strip([sk[0]] * 6, bg, (48, 48), add=gn)))
    rows.append(("THE DEATH: FOUR FLAKES, THEIR VIOLET LIGHT (EVERY OTHER FRAME)",
                 strip(ta[::2], bg, (64, 64), add=de[::2])))
    if ring:
        rows.append(("ART-A'S SHRINKING RING, THEN THE LANDING (THE SKATER'S MATCH)", strip(ring[::2] + [sk[0]], bg, (56, 56), add=ring[::2] + [gl])))
    sheet = sheet_of("RIFT SKATER (FINAL R89): A 'MANTA'", rows)
    gif = []
    for f in range(40):
        img = bg.crop((120, 160, 360, 360)).copy()
        k = f % 16
        x, y = 60 + f * 3.0, 40 + f * 2.2
        img = paste(img, sk[(k // 4 + 1) % 16], x, y)
        img = addc(img, gl, x, y)
        if f >= 30:
            img = addc(img, gn[min(5, f - 30)], 200, 150)
        img = paste(img, ta[min(11, f // 3)], 70, 140)
        img = addc(img, de[min(11, f // 3)], 70, 140)
        gif.append(img.resize((480, 400), Image.NEAREST))
    artkit.save_review(sheet, gif, CONCEPT["skater"], "rift-skater", fps=12)
    print("review: rift-skater-final-r89-a")


def review_props():
    L = l23kit.load
    b43, b44 = stand_in(43), stand_in(44)
    st, sb, pod, pb, sh, shb = (L("glyph-stone"), L("glyph-stone-break"), L("debris-helix-pod"),
                                L("debris-helix-pod-break"), L("debris-gold-shard"), L("debris-gold-shard-break"))
    rows43 = [("GLYPH STONE: INTACT, CRACKED, SPLIT (2X)", strip(st, b43, (48, 48))),
              ("GLYPH STONE DESTROYED (0.4 S)", strip(sb, b43, (72, 72))),
              ("THE HELIX POD TUMBLING (EVERY OTHER TURN)", strip(pod[::2], b43, (32, 32))),
              ("THE HELIX POD SHOT OPEN", strip(pb, b43, (64, 64)))]
    sheet = sheet_of("LEVEL 43 OBJECTS (FINAL R89): GLYPH STONE, HELIX POD", rows43)
    gif = [paste(paste(b43.crop((100, 100, 300, 240)).copy(), pod[f % 16], 60, 70), sb[min(7, f // 2)] if f < 16 else st[0], 140, 70).resize((400, 280), Image.NEAREST) for f in range(32)]
    artkit.save_review(sheet, gif, L43, "l43-props", fps=10)
    rows44 = [("THE GOLD SHARD TUMBLING (2X)", strip(sh, b44, (32, 32))),
              ("THE GOLD SHARD SHOT APART", strip(shb, b44, (64, 64)))]
    sheet = sheet_of("LEVEL 44 OBJECT (FINAL R89): THE GOLD SHARD", rows44)
    gif = [paste(b44.crop((100, 100, 300, 240)).copy(), sh[f % 16] if f < 24 else shb[min(7, f - 24)], 100, 70).resize((400, 280), Image.NEAREST) for f in range(32)]
    artkit.save_review(sheet, gif, L44, "l44-props", fps=10)
    print("review: l43-props-final-r89-a, l44-props-final-r89-a")


REVIEWS = {"warden": review_warden, "skater": review_skater, "props": review_props}

SETS = {"warden": [("gate-warden", 6, WD), ("gate-warden-bare", 1, WD), ("gate-warden-regrow", 8, WD),
                   ("gate-warden-sweep", 12, WD), ("gate-warden-glyphs", 1, WD), ("gate-warden-eye", 8, (32, 32)),
                   ("gate-warden-tilt", 4, WD), ("gate-warden-tilt-glyphs", 4, WD), ("gate-warden-slabs", 16, SLAB)],
        "skater": [("rift-skater", 16, (SK, SK)), ("rift-skater-glow", 1, (24, 24)), ("rift-skater-glint", 6, (24, 24)),
                   ("rift-skater-tatters", 12, (64, 64)), ("rift-skater-death", 12, (64, 64))],
        "props": [("glyph-stone", 3, (48, 48)), ("glyph-stone-break", 8, (72, 72)), ("debris-helix-pod", 16, (32, 32)),
                  ("debris-helix-pod-break", 8, (64, 64)), ("debris-gold-shard", 16, (32, 32)),
                  ("debris-gold-shard-break", 8, (64, 64))]}


def checker_share(img):
    import jovkit  # noqa: E402
    return jovkit.checker_share(img)


def check():
    bad = 0
    for subject, sets in SETS.items():
        for name, n, size in sets:
            try:
                frames = l23kit.load(name)
            except FileNotFoundError:
                frames = []
            where = bu.where(name)
            files = [f for f in (ROOT / where).glob(f"{name}*.png")
                     if f.stem == name or (f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit())]
            src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
            vis = chk = 0
            for f in files:
                v, c = checker_share(Image.open(f))
                vis, chk = vis + v, chk + c
            share = chk / max(vis, 1)
            lit_edge = False
            if name in ADDITIVE:
                for fr in frames:
                    a = np.array(fr)[..., :3].max(axis=-1)
                    lit_edge |= bool(max(a[0].max(), a[-1].max(), a[:, 0].max(), a[:, -1].max()) > 0)
            ok = len(frames) == n and all(fr.size == size for fr in frames) and src_ok and share < 0.05 and not lit_edge
            bad += not ok
            print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
                  f", checker {share * 100:.1f} %{', LIT EDGE' if lit_edge else ''}")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    for s in subjects:
        if "--review" not in args:
            BUILDERS[s]()
        REVIEWS[s]()
