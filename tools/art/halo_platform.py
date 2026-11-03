#!/usr/bin/env python3
"""Production art: the Halo Platform, Level 41's held centrepiece (design/enemies/ground/halo-platform; M10 part G
batch, concept round 79, step ART-G1). The chosen r07 model (J8) in round 72's **A.6 a** "gold trim" livery
(tools/concept/enemies_r72.py ``halo_segment_v('a')``, ``halo_gun_v``, ``halo_rail_v``, ``halo_core_v``, imported
unchanged): gloss black, the r07 gold rails, mount rings and end caps gold, red running lights, the strong 2 px gold rim.

**QG-1 (round 79, pending the user's review): the Iron Sovereign's segment rule** instead of r07's 128 pre-rendered ring
frames per 60 degrees (about 2 atlas pages, over the one-page unit budget). Each of the ring's six segments is rendered
once **top-lit** (a point light ``LIGHT_H`` model units straight above the platform's centre, no fill, the chrome
environment without its side term), at the headings 0..45 degrees in ``n8`` equal steps so the ring's outer edge moves
at most ``EDGE_STEP`` = 1 px a step (r07's 128 frames moved it 0.9 px; the Sovereign's rings take 2 px); the game draws
a segment at any heading from that set by quarter turns and a mirror, exact on the pixel grid (every frame keeps the
platform's centre on a pixel corner). tools/art/iron_sovereign.py's rule, unchanged:

  heading h (degrees clockwise on the screen from straight down, any value): d = h mod 360, q = floor(d / 90),
  r = d - 90 q; r <= 45: frame k = round(r / 45 * n8), else frame k = round((90 - r) / 45 * n8) **mirrored** across
  the 45-degree line, (x, y) -> (-y, -x) about the centre; then q quarter turns clockwise, (x, y) -> (-y, x) each.
  A frame's pixels start at ``offset`` [x0, y0] px from the centre (y down); mirrored, its size (w, h) becomes (h, w)
  and its offset (-y0 - h, -x0 - w); each quarter turn makes it (-y0 - h, x0) with the size swapped.

The r07 segment is mirror-symmetric about its radial axis, so the mirror is exact. The sun's top-left light lives in two
**static overlays** over the ring (``-rim`` additive: the sheen and the elite rim's gold on the band's sunward edges, a
faint red on the far ones; ``-shade`` normal: the far side in shadow), drawn on top of the turning segments; the core and
the turrets are lit by the low Sun as every other unit (each frame its own render, nothing lit is turned).

Scale: r07's 47.8 px per model unit (110 / 2.3), the turrets' mounts at radius 96 px; screen angles as the simulation's
(RingArena: a mount at angle a sits at (-r sin a, r cos a) px from the centre, y down). Segment j is centred at
``angle + j * 60`` and carries turret j: even j a **gun turret**, odd j a **rail turret** (alternating, the stat block).

Outputs (build/art-held/sprites/ until the platform's data.yaml claims the root ``halo-platform`` (step G2; rerun
then and the files move to assets/sprites/); ``additive`` = premultiplied on black, GL_SRC_ALPHA, GL_ONE; every glow a
float field written in >= 32 undithered steps, no ordered dither, drawn 1x):
  halo-platform-segment_0..n8      normal, top-lit: one ring segment (the r07 block: a gloss-black hull between two
                         gold rails, the gold mount ring in its middle, gold end caps) at the headings 0..45 in n8 steps
                         (n8 = 85); trimmed, sizes and offsets in the pivots; one 48-colour palette. Shadow: the game
                         draws the segments (and the core, the turrets) once more tinted black at the pivots'
                         ``shadow`` offset and alpha first (no frames of their own). The wreck draws them tinted
                         (RGB x ``wreck_tint``) under ``-scorch``
  halo-platform-rim      240x240 additive, static, drawn 1x centred: the Sun's top-left sheen on the band, the 2 px
                         gold rim on its sunward outer and inner edges, a faint red on the far edges (after the
                         segments)
  halo-platform-shade    240x240 normal, stepped translucency (32 undithered steps), drawn 1x: the band's far
                         (bottom-right) side in shadow
  halo-platform-arc      (band - 6) x 4 additive: the live 120-degree arc's faint red band on the ring's hull (the
                         strip's x runs outward across the band from ``arc.from_radius``), tiled round the live sector
                         as quads; its first and last columns are the bright edges (the Sovereign's ``-arc-*``)
  halo-platform-lamp-gold, -lamp-red    9x9 additive: a mount's two running lights on the inner rail (radius and
                         the angles from the mount in the pivots): gold out of the arc, red inside it (the Sovereign's
                         look); dark while the platform sleeps and on a dead mount
  halo-platform-gun_0..63, -rail_0..63   36x36 normal, sunlit: the gun turret (r06's armoured head, twin barrels) and
                         the rail turret (one long rail, three gold coils) at 64 headings, k x 5.625 degrees clockwise
                         from down; drawn on the mount, centred on it
  halo-platform-gun-muzzle_0..3    12x12 additive: the gun's 0.3 s tell, the muzzles glowing (rising), one on each
                         barrel's muzzle (pivots ``muzzle``: two points per heading)
  halo-platform-rail-charge_0..3   16x16 additive: the rail's coils charging (rising), on the coils (pivots ``coils``)
  halo-platform-gun-wreck, -rail-wreck   36x36 normal: a dead mount's torn, burnt turret stump (it stays on the ring
                         and turns with it, unrotated like the Sovereign's)
  halo-platform-core_0..9    88x88 normal, sunlit, centred: the core (an octagonal armoured drum 76 px across, gold
                         trim and bolts, the reactor eye under its glass dome); 0 shut (the eye dark), 1..4 the eye
                         waking through the barrier's 0.5 s fade, 5 open (the eye lit red); 6..9 the 0.5 s flare (the
                         eye white-hot), played 6 -> 9 and back to 5
  halo-platform-core-wreck   88x88 normal: the burnt core, its glass gone, the eye blown out
  halo-platform-barrier_0..11  104x104 additive, centred: the red hex barrier (radius 46 over the 60x60 hitbox) at
                         about 25 % with a brighter rim, a slow shimmer crossing it in 12 frames of 20 steps (a 4 s
                         loop); the 0.5 s fade is the game's alpha
  halo-platform-barrier-hit_0..5   32x32 additive: a shot spent on the barrier, a red spark and the hexes round it lit
  halo-platform-eye-glow_0..3    48x48 additive, centred: the open eye's red light, a slow pulse (8 steps a frame)
  halo-platform-eye-flare_0..3   112x112 additive (PT2: was 72x72, its rays cut), centred: the 0.5 s flare before the
                                 open core's ring, white-red, rising
  halo-platform-cookoff_0..9   64x64 additive: a turret cooking off at the death (iron_sovereign.py's white-gold flash,
                         ring and sparks, in 64 steps), 30 fps; ``-cookoff-debris_0..9`` 64x64 normal: gloss-black and gold shards
  halo-platform-blowout_0..11  128x128 additive: the reactor eye blowing out (a white-red jet of light, a ring), 20 fps,
                         before the chain; then the shared ``explosion-vacuum-large`` and its debris with the flash
  halo-platform-scorch   240x240 normal, stepped translucency (32 steps): soot over the stopped ring and the core
  build/art-held/pivots/halo-platform.json: the rule, the ring (band, n8, frame sizes and offsets, lamps, arc), the
                         turrets (kinds per mount, muzzles and coils per heading), the core, the shadow, the wreck tint,
                         the overlays, the death's order and timings
  intel/halo-platform    30x30 sensor portrait via tools/art/intel.py (``intel_hi``: the ring at rest round the core)
  design/enemies/ground/halo-platform/concept/halo-platform-final-r79-a.png/.gif   review (the sets, the platform at rest
                         and wrecked over Level 41's citadel deck, the readability check, the atlas share) and GIF (the
                         held arena: the arc following the ship, gun bursts, a rail line, three mounts lost with their
                         eased speed-ups, the barrier fading, the eye's flare and ring, the death chain, the wreck)
  .../concept/halo-rotation-final-r79-a.png/.gif   QG-1's proof: the ring turning at 20 and 60 degrees a second at 1:1,
                         60 frames a second (consecutive frames, the joins), the rule against direct renders, the edge
                         step per frame; GIF at 50 fps, both speeds side by side, 1x

Run: python3 tools/art/halo_platform.py [ring] [turrets] [core] [fx] [intel] [--review] [--check]
     (no subject = all, about 3 minutes on 12 workers; renders cached in the system temp dir,
     conceptgame-cache/halo-platform/, by the model code's hash; ``ring`` also writes the overlays, the arc and the
     lamps; --review rebuilds only the review files from the written frames; --check lists every set with its place,
     frames, size, Source chunk, checker share and an additive set's glow steps, and prints the atlas share)
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
import l23kit
from artkit import DESIGN, ROOT, TAU

import enemies_r60 as e60  # noqa: E402  (concept: the elite rim's colours, gold_share)
import enemies_r72 as e72  # noqa: E402  (concept: round 72's Halo models, imported unchanged)
from render import raster, sdf, sprite  # noqa: E402
from render.enemy_models import A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.enemy_rigs import write_gif  # noqa: E402
from render.sdf import Material, sd_capsule, sd_cylinder_z, sd_ellipsoid, union  # noqa: E402

SCRIPT = "halo_platform.py"
BATCH = "M10 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r79"
artkit.REVIEW_ROUND = ROUND
ROOT_NAME = "halo-platform"
CONCEPT = DESIGN / "enemies" / "ground" / "halo-platform" / "concept"
WORKERS = 12
PXU = e72.HALO_PXU                          # 47.8 px per model unit (r07)
R = e72.HALO_R                              # 96 px: the turrets' mounts
RU = R / PXU
SEGMENTS = 6
EDGE_STEP = 1.0                             # px the outer edge moves from one segment frame to the next
LIGHT_H = 8.4                               # model units (400 px) straight above the centre: the top light
HULL_SPEC, GOLD_SPEC, SEG_EXPOSURE = 0.15, 0.5, 0.85     # the top light's highlight kept off the gloss black
SEG_COLOURS = 48
OVERLAY = 240
TUR = 36                                    # the turrets' cell
TUR_EXT = 2.3                               # model units across it (15.7 px a unit, round 72's turret sprites)
HEADINGS = 64
CORE = 88
CORE_PXU = 76 / 1.3                         # the octagon 76 px across its flats (the stat block)
CORE_FRAMES = 10
EYE_SHUT, EYE_OPEN, EYE_FLARE = 0.06, 0.64, 1.64          # the eye's emission (x 2.5 + the concept's base, see core_mats)
BARRIER_R, BARRIER_PAD, BARRIER_FRAMES, BARRIER_CELL = 46, 6, 12, 7.0
LAMP_DEG = 11.0                             # the two running lights each side of a mount, on the inner rail
SHADOW = {"offset": [6, 8], "alpha": 0.45}
WRECK_TINT = 0.5
GOLD = np.array((255, 196, 64), float)
GOLD_HI = np.array((255, 236, 170), float)
CRIMSON = np.array((255, 48, 56), float)
RED_LAMP = np.array((255, 60, 44), float)
BARRIER_RED = np.array((255, 70, 50), float)
WHITE = np.array((255, 248, 236), float)
KINDS = ("gun", "rail")
FPS = 20
write, load, where = bu.write, bu.load, bu.where


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def kind_of(j):
    return KINDS[j % 2]


# =========================================================================== geometry

def screen_xy(r, heading_deg):
    a = np.radians(heading_deg)
    return -r * np.sin(a), r * np.cos(a)


def grid(n, m=None):
    m = n if m is None else m
    yy, xx = np.mgrid[0:m, 0:n].astype(np.float64) + 0.5
    return xx - n / 2, yy - m / 2


def smooth_steps(values, levels=artkit.GLOW_LEVELS):
    return np.round(np.clip(values, 0, 1) * (levels - 1)) / (levels - 1)


def glow_img(f, col, white=0.0, levels=48):
    """A 0..1 field as an additive glow in ``levels`` undithered steps (premultiplied on black)."""
    f = smooth_steps(f, levels)
    rgb = np.asarray(col, float)[None, None] * f[..., None] + 255 * white * np.clip(f - 0.55, 0, 1)[..., None] / 0.45
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full(f.shape, 255.0)]).astype(np.uint8), "RGBA")
    return artkit.additive(img)


def dot_glow(n, col, core=1.2, halo=2.6, k=1.0, white=0.5):
    x, y = grid(n)
    r = np.hypot(x, y)
    f = (np.exp(-(r / core) ** 2) * 1.2 + np.exp(-(r / halo) ** 2) * 0.45) * k
    return glow_img(f, col, white=white)


# =========================================================================== ring segments (top-lit)

def _local(theta):
    """World (model, y up) -> the segment's local frame for a segment centred at model angle ``theta`` on radius RU:
    local +X toward the ring's centre, +Y along the tangent (r07's ``_sector_local``)."""
    c, s = np.cos(theta), np.sin(theta)

    def f(p):
        dx, dy = p[:, 0] - RU * c, p[:, 1] - RU * s
        q = p.copy()
        q[:, 0] = -(dx * c + dy * s)
        q[:, 1] = -dx * s + dy * c
        return q

    def fn(n):
        m = n.copy()
        m[:, 0] = -(n[:, 0] * c + n[:, 1] * s)
        m[:, 1] = -n[:, 0] * s + n[:, 1] * c
        return m
    return f, fn


def seg_mats(theta, tint=1.0):
    """Round 72's A.6 a segment materials, their patterns in the segment's own frame (the same at every heading), the
    top light's highlight kept off the gloss black."""
    _, mats = e72.halo_segment_v("a")
    loc, rot = _local(theta)
    out = []
    for i, m in enumerate(mats):
        pat = m.pattern
        if pat is not None:
            m = replace(m, pattern=(lambda p, n, f=pat: f(loc(p), rot(n))))
        if i in (A_HULL, A_FACET):
            m = replace(m, spec=HULL_SPEC)
        if i == A_GOLD:
            m = replace(m, spec=GOLD_SPEC)
        out.append(m)
    return out


def seg_scene(theta):
    seg, _ = e72.halo_segment_v("a")
    loc, _ = _local(theta)
    return lambda p: seg(loc(p))


def _sym_env(r):
    """The chrome environment without its side (y) term: the same reflection at every heading (the Sovereign's)."""
    q = r.copy()
    q[:, 1] = 0.0
    return _ENV(q)


_ENV = sdf.env_color


def seg_window(h_deg, pad=5):
    """The render window for heading h: its centre on the pixel lattice (integer px from the platform's centre) and an
    even size that holds the block."""
    h = np.radians(h_deg)
    cx, cy = int(round(-R * np.sin(h))), int(round(R * np.cos(h)))
    th = np.linspace(-np.pi / SEGMENTS, np.pi / SEGMENTS, 25)[:, None] + h
    rr = np.linspace(R - 18, R + 18, 5)[None, :]
    xs, ys = -rr * np.sin(th) - cx, rr * np.cos(th) - cy
    half = int(np.ceil(max(np.abs(xs).max(), np.abs(ys).max()))) + pad
    return cx, cy, 2 * half


def seg_render(h_deg, pad=5):
    """A segment centred at screen heading ``h_deg``: (native unquantised frame, offset of its top left)."""
    cx, cy, w = seg_window(h_deg, pad)
    ext = w / PXU
    c = np.array((cx / PXU, -cy / PXU, 0.0))
    key = (np.array((0.0, 0.0, LIGHT_H)) - c) / (ext / 2.3)
    h = np.radians(h_deg)
    theta = np.arctan2(-np.cos(h), -np.sin(h))          # the segment's centre angle in the model (y up)
    sdf.env_color = _sym_env
    try:
        hi = sdf.render(seg_scene(theta), seg_mats(theta), (w * 4, w * 4), ext, center=(c[0], c[1]), key_pos=key,
                        fill=0.0, exposure=SEG_EXPOSURE)
    finally:
        sdf.env_color = _ENV
    img = artkit.native(hi, 4, 80)
    box = img.getchannel("A").getbbox()
    return img.crop(box), (cx - w // 2 + box[0], cy - w // 2 + box[1])


R_OUT = R + 13.6                            # the band's outer edge (the rails' outside: 0.28 units), measured in build
R_IN = R - 13.6


def n8():
    return int(np.ceil(45.0 / np.degrees(EDGE_STEP / R_OUT)))


def seg_headings():
    n = n8()
    return [45.0 * k / n for k in range(n + 1)]


def seg_pose(deg):
    """(frame index, mirrored, quarter turns) of a segment centred at heading ``deg``."""
    d = deg % 360.0
    q = int(d // 90) % 4
    r = d - 90 * q
    mirrored = r > 45
    k = int(round((90 - r if mirrored else r) / 45 * n8()))
    return k, mirrored, q


def place(img, offset, mirrored, q):
    """A frame turned by the rule: (image, offset from the centre)."""
    x0, y0 = offset
    if mirrored:
        x0, y0 = -y0 - img.height, -x0 - img.width
        img = img.transpose(Image.Transpose.TRANSVERSE)
    for _ in range(q):
        x0, y0 = -y0 - img.height, x0
        img = img.transpose(Image.Transpose.ROTATE_270)
    return img, (x0, y0)


# =========================================================================== turrets, wrecks (sunlit)

def elite_rim(img):
    """enemies_r60.belt_rim(True) without its per-frame palette: a 2 px gold rim on the edges facing the top-left sun,
    red on the far ones."""
    a = np.array(img.convert("RGBA"))
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
    out = a.astype(np.float64)
    out[tl, :3] = out[tl, :3] * 0.12 + e60.GOLD_HI * 0.88
    inner = op & ~tl & ~br & (np.roll(tl, 1, axis=1) | np.roll(tl, 1, axis=0))
    out[inner, :3] = out[inner, :3] * 0.55 + e60.GOLD_RIM * 0.45
    out[br, :3] = out[br, :3] * 0.45 + e60.RED_RIM * 0.55
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def turret_model(kind):
    return e72.halo_gun_v("a") if kind == "gun" else e72.halo_rail_v("a")


MUZZLES = {"gun": [(0.12, 1.02), (-0.12, 1.02)], "rail": [(0.0, 1.12)]}
COILS = (0.0, 0.65)


def to_px(lx, ly, k, n=HEADINGS, ppu=TUR / TUR_EXT):
    """A local turret point (x right, y forward, model units) at heading k: px from the mount's centre (y down)."""
    from render.enemy_rigs import local_to_screen
    dx, dy = local_to_screen(lx, ly, np.pi / 2 + TAU * k / n, ppu)
    return [round(float(dx), 2), round(float(dy), 2)]


def scorch_pattern(p, n):
    return 0.55 + 0.35 * np.sin(p[:, 0] * 23 + 2 * np.sin(p[:, 1] * 17)) * np.cos(p[:, 1] * 19)


def burnt(mats):
    out = list(mats)
    out[A_HULL] = replace(out[A_HULL], albedo=(0.07, 0.065, 0.07), spec=0.15, pattern=scorch_pattern)
    out[A_FACET] = replace(out[A_FACET], albedo=(0.09, 0.08, 0.08), spec=0.2, pattern=scorch_pattern)
    out[A_GOLD] = replace(out[A_GOLD], albedo=(0.36, 0.24, 0.08), spec=0.3)
    out[A_GUN] = Material((0.08, 0.07, 0.07), metal=0.2, shininess=20, spec=0.2, pattern=scorch_pattern)
    out[A_RED] = e60.emissive(np.array((0.2, 0.05, 0.03)), 0.05)
    return out


def wreck_model(kind):
    """The turret's head torn open at a jagged line, its barrels broken short, all scorched."""
    scene0, mats = turret_model(kind)

    def scene(p):
        d, m = scene0(p)
        ang = np.arctan2(p[:, 1], p[:, 0])
        cut = p[:, 2] - (0.3 + 0.07 * np.sin(ang * 5) + 0.04 * np.sin(ang * 9 + 1))
        d = np.maximum(d, cut)
        d = np.maximum(d, p[:, 1] - (0.42 + 0.1 * np.sin(p[:, 0] * 31)))
        hole = np.hypot(p[:, 0] + 0.04, p[:, 1] + 0.02) - 0.16
        d = np.maximum(d, -np.where(p[:, 2] > 0.2, hole, 1.0))
        stump = sd_cylinder_z(p, (0, 0, 0.12), 0.3, 0.1)
        d2 = np.minimum(d, stump)
        m = np.where(stump < d, A_GUN, m)
        return d2, m
    return scene, burnt(mats)


def _turret(kind, k):
    scene, mats = turret_model(kind)
    return elite_rim(bu.render(*bu.turned(scene, mats, k, HEADINGS), TUR, TUR_EXT))


def _wreck(kind):
    scene, mats = wreck_model(kind)
    return elite_rim(bu.render(*bu.turned(scene, mats, 6, HEADINGS), TUR, TUR_EXT))


# =========================================================================== the core (sunlit)

def core_mats(eye, wreck=False):
    """Round 72's core materials (A.6 a) with the eye's emission at ``eye`` (the concept's 1.6 + 2.5 x eye at 0 is
    0.64 here: open)."""
    _, mats = e72.halo_core_v("a", 0.0)
    mats = list(mats)
    mats[A_RED] = Material((0.05, 0.02, 0.02), emission=tuple(np.array((1.0, 0.25, 0.15)) * 2.5 * eye))
    col = np.array((1.0, 0.25, 0.15))
    if eye > 1.0:
        w = min(1.0, (eye - 1.0) / 0.64)
        col = np.array((1.0, 0.25 + 0.6 * w, 0.15 + 0.55 * w))
        mats[A_RED] = Material((0.05, 0.02, 0.02), emission=tuple(col * 2.5 * eye))
    # the reactor's light through the glass dome (the eye sits inside it): a hot spot over the eye, a faint wash
    mats[A_GLASS] = replace(mats[A_GLASS], emission=tuple(col * 1.6 * eye),
                            emission_pattern=lambda p, n: (np.exp(-(np.hypot(p[:, 0], p[:, 1]) / 0.13) ** 2)
                                                           + 0.12 * np.exp(-(np.hypot(p[:, 0], p[:, 1]) / 0.26) ** 2)))
    if wreck:
        mats = burnt(mats)
    return mats


def core_eye(i):
    if i <= 5:
        return EYE_SHUT + (EYE_OPEN - EYE_SHUT) * (i / 5) ** 1.3
    return EYE_OPEN + (EYE_FLARE - EYE_OPEN) * (i - 5) / 4


def core_scene(wreck=False):
    scene0, _ = e72.halo_core_v("a", 0.0)
    if not wreck:
        return scene0

    def scene(p):
        d, m = scene0(p)
        glass = m == A_GLASS
        d = np.where(glass, d + 0.08, d)                  # the glass dome blown away
        pit = np.hypot(p[:, 0], p[:, 1]) - 0.27
        d = np.maximum(d, -np.where(p[:, 2] > 0.24, pit, 1.0))
        m = np.where(glass, A_GUN, m)
        return d, m
    return scene


def _core(i, wreck=False):
    eye = 0.0 if wreck else core_eye(i)
    hi, factor = artkit.render_hi(core_scene(wreck), core_mats(eye, wreck), (CORE, CORE), CORE / CORE_PXU)
    return elite_rim(artkit.native(hi, factor, 80))


# =========================================================================== jobs (cached)

def _job(job):
    kind = job[0]
    if kind == "seg":
        return seg_render(job[1])
    if kind == "turret":
        return _turret(job[1], job[2])
    if kind == "wreck":
        return _wreck(job[1])
    if kind == "core":
        return _core(job[1])
    if kind == "core-wreck":
        return _core(0, wreck=True)
    if kind == "direct":
        return seg_render(job[1], pad=8)
    raise ValueError(job)


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "halo-platform"


def _model_key():
    text = Path(__file__).read_text(encoding="utf-8").split("# " + "=" * 75 + " effects")[0]
    return hashlib.sha1(text.encode()).hexdigest()[:12]


def run(jobs):
    """Render the jobs in parallel, cached by the model code's hash (a frame is (image) or (image, offset))."""
    key = _model_key()
    paths = [CACHE / f"{hashlib.sha1((key + repr(j)).encode()).hexdigest()}" for j in jobs]
    todo = [i for i, p in enumerate(paths) if not p.with_suffix(".png").exists()]
    if todo:
        CACHE.mkdir(parents=True, exist_ok=True)
        with ProcessPoolExecutor(WORKERS) as pool:
            for i, res in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img, off = res if isinstance(res, tuple) else (res, None)
                img.save(paths[i].with_suffix(".png"))
                if off is not None:
                    paths[i].with_suffix(".json").write_text(json.dumps([int(v) for v in off]))
    out = []
    for p in paths:
        img = Image.open(p.with_suffix(".png")).convert("RGBA")
        j = p.with_suffix(".json")
        out.append((img, tuple(json.loads(j.read_text()))) if j.exists() else img)
    return out


# =========================================================================== effects

def rim_overlay():
    """The Sun on the turning ring, static: a soft top-left sheen over the band, the elite rim's 2 px gold on the band's
    edges that face the Sun (the outer edge on the upper left, the inner edge on the lower right), a faint red on the
    far ones (additive; drawn 1x about the centre)."""
    x, y = grid(OVERLAY)
    r = np.hypot(x, y)
    ux, uy = x / np.maximum(r, 1), y / np.maximum(r, 1)
    sun = (-ux - uy) / np.sqrt(2)                       # +1 where the outward normal faces the top left
    band = np.clip(np.minimum(r - R_IN, R_OUT - r) + 0.5, 0, 1)
    across = np.clip((r - R_IN) / (R_OUT - R_IN), 0, 1)
    f = band * np.clip(sun, 0, 1) ** 1.6 * (0.06 + 0.08 * across)
    outer = np.exp(-((r - (R_OUT - 1.0)) / 0.9) ** 2)
    inner = np.exp(-((r - (R_IN + 1.0)) / 0.9) ** 2)
    gold = outer * np.clip(sun - 0.15, 0, 1) ** 1.1 + inner * np.clip(-sun - 0.15, 0, 1) ** 1.1
    red = outer * np.clip(-sun - 0.3, 0, 1) + inner * np.clip(sun - 0.3, 0, 1)
    rgb = (GOLD_HI[None, None] * smooth_steps(np.clip(f + 0.85 * gold, 0, 1), 48)[..., None]
           + e60.RED_RIM[None, None] * smooth_steps(np.clip(0.32 * red, 0, 1), 48)[..., None])
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full(r.shape, 255.0)]).astype(np.uint8), "RGBA")
    return artkit.additive(img)


def shade_overlay():
    """The far side of the band in shadow (normal blend, 32 undithered steps of near-black; drawn 1x)."""
    x, y = grid(OVERLAY)
    r = np.hypot(x, y)
    far = np.clip((x + y) / np.maximum(r, 1) / np.sqrt(2), 0, 1)
    band = np.clip(np.minimum(r - R_IN, R_OUT - r) + 0.5, 0, 1)
    alpha = smooth_steps(band * far ** 1.3, 32) * 0.4
    rgba = np.zeros((OVERLAY, OVERLAY, 4))
    rgba[..., :3] = (6, 6, 12)
    rgba[..., 3] = np.round(alpha * 255)
    rgba[rgba[..., 3] == 0] = 0
    return Image.fromarray(rgba.astype(np.uint8), "RGBA")


def arc_strip():
    w = int(R_OUT - R_IN - 6)
    f = np.full((4, w), 0.16)
    f[:, 0] = f[:, -1] = 0.85
    f[:, 1] = f[:, -2] = 0.4
    return glow_img(f, CRIMSON)


def hex_edges(x, y, cell):
    import act6_gear  # noqa: E402  (the dome's hex lattice, shared with the pylon)
    return act6_gear.hex_edges(x, y, cell)[0]


def barrier_frame(f, R0=BARRIER_R):
    """The red hex barrier (enemies_r72.barrier, the pylon dome's build): a thin lattice at about 25 % with a brighter
    rim, a shimmer band crossing it."""
    D = 2 * (R0 + BARRIER_PAD)
    ss = 3
    x, y = grid(D * ss)
    x, y = x / ss, y / ss
    r = np.hypot(x, y)
    d = hex_edges(x, y, BARRIER_CELL)
    band = (f / BARRIER_FRAMES) * 3 * R0 - 1.5 * R0
    shimmer = np.exp(-(((x + y) / np.sqrt(2) - band) / (0.3 * R0)) ** 2)
    lattice = np.exp(-(d / 0.7) ** 2) * (0.26 + 0.2 * shimmer)
    inside = np.clip(R0 - r + 0.5, 0, 1)
    amount = (0.05 + lattice) * inside + 0.9 * np.exp(-((r - R0) / 1.3) ** 2)
    amount = amount.reshape(D, ss, D, ss).mean(axis=(1, 3))
    return glow_img(np.clip(amount, 0, 1), BARRIER_RED, white=0.15)


def barrier_hit(f, n=6):
    x, y = grid(32)
    r = np.hypot(x, y)
    s = (f + 0.5) / n
    d = hex_edges(x + 5.0, y + 2.0, BARRIER_CELL)
    lattice = np.exp(-(d / 0.8) ** 2) * np.exp(-(r / (6 + 6 * s)) ** 2) * (1 - s) * 0.9
    spark = np.exp(-(r / (1.6 + 2.4 * s)) ** 2) * (1.3 * (1 - s) ** 1.4)
    rays = np.clip(np.cos(np.arctan2(y, x) * 4) ** 20, 0, 1) * np.exp(-(r / (4 + 6 * s)) ** 2) * (1 - s)
    return glow_img(np.clip(lattice + spark + 0.6 * rays, 0, 1), BARRIER_RED, white=0.6)


def eye_glow(f, n=48):
    x, y = grid(n)
    r = np.hypot(x, y)
    k = 0.8 + 0.2 * np.sin(TAU * f / 4)
    fld = (np.exp(-(r / 5) ** 2) * 0.8 + np.exp(-(r / 14) ** 2) * 0.35) * k
    return glow_img(fld, (255, 66, 40), white=0.3)


def eye_flare(f, n=112):
    x, y = grid(n)
    r, a = np.hypot(x, y), np.arctan2(y, x)
    s = (f + 1) / 4
    fld = (np.exp(-(r / (4 + 5 * s)) ** 2) * 1.3 + np.exp(-(r / (12 + 12 * s)) ** 2) * 0.5 * s
           + np.clip(np.cos(a * 4 + 0.4) ** 30, 0, 1) * np.exp(-(r / (10 + 16 * s)) ** 2) * 0.5 * s) * (0.5 + 0.5 * s)
    return glow_img(np.clip(fld, 0, 1.2), (255, 120, 90), white=0.95)


def muzzle_glow(f, n=12):
    return dot_glow(n, (255, 150, 60), core=0.8 + 0.25 * f, halo=1.8 + 0.4 * f, k=0.45 + 0.2 * f)


def coil_glow(f, n=16):
    return dot_glow(n, (255, 206, 90), core=1.2 + 0.3 * f, halo=2.6 + 0.6 * f, k=0.4 + 0.2 * f, white=0.4)


def cookoff_frame(i, n=64, frames=10):
    """A turret cooking off: a white-gold flash out of the mount, a thin ring running out, sparks (iron_sovereign.py's
    cook-off, the same read, its field written in 64 undithered steps)."""
    x, y = grid(n)
    r, a = np.hypot(x, y), np.arctan2(y, x)
    s = i / (frames - 1)
    flash = np.exp(-(r / (5 + 9 * s)) ** 2) * (1.3 if i < 2 else 1.1 * (1 - s) ** 1.5)
    ring = np.exp(-((r - (6 + 24 * s)) / 1.4) ** 2) * 0.7 * (1 - s)
    rays = np.clip(np.cos(a * 7 + 0.6) ** 24, 0, 1) * np.exp(-(r / (8 + 14 * s)) ** 2) * (1 - s) * 0.9
    rng = np.random.default_rng(71)
    sp = np.zeros((n, n))
    for _ in range(14):
        aa = rng.uniform(0, TAU)
        dd = (5 + rng.uniform(10, 26) * s)
        sp += np.exp(-(((x - np.cos(aa) * dd) ** 2 + (y - np.sin(aa) * dd) ** 2) / 0.9)) * (1 - s) * 1.2
    return glow_img(flash + ring + rays + sp, (255, 214, 130), white=0.9, levels=64)


def cookoff_frames():
    return [cookoff_frame(i) for i in range(10)]


def debris_frame(i, n=64, frames=10):
    """Gloss-black and gold shards flung outward (lit top-left), normal blending (iron_sovereign.py's, recoloured)."""
    s = i / (frames - 1)
    img = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(4173)
    for j in range(11):
        aa = rng.uniform(0, TAU)
        dist = 4 + rng.uniform(12, 26) * s ** 0.8
        spin = rng.uniform(-6, 6) * s
        size = rng.uniform(1.6, 3.4) * (1 - 0.3 * s)
        gold = j % 3 == 0
        cx, cy = n / 2 + np.cos(aa) * dist, n / 2 + np.sin(aa) * dist
        pts = [(cx + np.cos(spin + t) * size * (1.0 if t < 3 else 0.6), cy + np.sin(spin + t) * size)
               for t in (0.3, 2.2, 3.9, 5.2)]
        base = np.array((222, 160, 50) if gold else (34, 30, 40)) * (1 - 0.35 * s)
        hi = np.array((255, 226, 140) if gold else (120, 110, 130)) * (1 - 0.35 * s)
        d.polygon(pts, fill=tuple(int(v) for v in base) + (255,))
        d.point([(round(pts[0][0]), round(pts[0][1]))], fill=tuple(int(v) for v in hi) + (255,))
    return img


def blowout_frame(i, n=128, frames=12):
    """The reactor eye blowing out: a white-red core of light, a jet ring running out, rays (vacuum: no smoke)."""
    x, y = grid(n)
    r, a = np.hypot(x, y), np.arctan2(y, x)
    s = i / (frames - 1)
    core = np.exp(-(r / (6 + 22 * s)) ** 2) * (1.4 if i < 3 else 1.3 * (1 - s) ** 1.2)
    ring = np.exp(-((r - (8 + 50 * s)) / (1.6 + 2 * s)) ** 2) * 0.8 * (1 - s) ** 0.8
    rays = np.clip(np.cos(a * 6 + 0.3) ** 16, 0, 1) * np.exp(-(r / (10 + 40 * s)) ** 2) * (1 - s) * 0.8
    rgb_red = glow_img(np.clip(core * 0.6 + ring + rays, 0, 1.3), (255, 90, 60), white=0.9)
    return rgb_red


def scorch_overlay():
    """Soot over the stopped ring and the core: blotches along the band and round the core (normal blend, 32 steps)."""
    x, y = grid(OVERLAY)
    r = np.hypot(x, y)
    n = raster.fbm(OVERLAY, OVERLAY, 18, 4179, octaves=4, period=False)
    band = np.clip(np.minimum(r - R_IN + 3, R_OUT + 3 - r) / 4, 0, 1)
    core = np.clip((44 - r) / 6, 0, 1)
    alpha = np.clip((n - 0.35) * 2.2, 0, 1) * np.maximum(band, core) * 0.75
    alpha = smooth_steps(alpha / 0.75, 32) * 0.75
    rgba = np.zeros((OVERLAY, OVERLAY, 4))
    rgba[..., :3] = (10, 8, 8)
    rgba[..., 3] = np.round(alpha * 255)
    rgba[rgba[..., 3] == 0] = 0
    return Image.fromarray(rgba.astype(np.uint8), "RGBA")


# =========================================================================== build

def pivot_path():
    return (artkit.PIVOTS if bu.claimed(ROOT_NAME) else ROOT / "build" / "art-held" / "pivots") / f"{ROOT_NAME}.json"


def load_pivots():
    for folder in (artkit.PIVOTS, ROOT / "build" / "art-held" / "pivots"):
        if (folder / f"{ROOT_NAME}.json").exists():
            return json.loads((folder / f"{ROOT_NAME}.json").read_text(encoding="utf-8"))
    return {}


def update_pivots(section):
    data = load_pivots()
    data.pop("source", None)
    data.update(section)
    data["centre"] = {"note": "the platform's centre (the core's) on a pixel corner; every offset is px from it (y "
                              "down) unless a set says it is centred; the core stops at (240, 190) in the 480x540 "
                              "field in Level 41's halt"}
    folder = artkit.PIVOTS if bu.claimed(ROOT_NAME) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{ROOT_NAME}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n",
                                              encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{ROOT_NAME}.json").exists():
        (other / f"{ROOT_NAME}.json").unlink()


def measure_band():
    """The band's inner and outer radius from a direct render at heading 0 (the rails' outsides), px."""
    global R_IN, R_OUT
    img, off = run([("seg", 0.0)])[0]
    a = np.array(img)[..., 3] > 0
    ys, xs = np.nonzero(a)
    px, py = xs + off[0] + 0.5, ys + off[1] + 0.5
    rr = np.hypot(px, py)
    near = np.abs(px) < 30
    R_IN, R_OUT = float(np.floor(rr[near].min() * 2) / 2), float(np.ceil(rr[near].max() * 2) / 2)
    return R_IN, R_OUT


def build_ring():
    measure_band()
    heads = seg_headings()
    res = run([("seg", h) for h in heads])
    frames = artkit.quantize_set([img for img, _ in res], SEG_COLOURS)
    save_set(f"{ROOT_NAME}-segment", frames)
    save_set(f"{ROOT_NAME}-rim", [rim_overlay()], single=True)
    save_set(f"{ROOT_NAME}-shade", [shade_overlay()], single=True)
    save_set(f"{ROOT_NAME}-arc", [arc_strip()], single=True)
    save_set(f"{ROOT_NAME}-lamp-gold", [dot_glow(9, (255, 200, 90), core=1.1, halo=2.4)], single=True)
    save_set(f"{ROOT_NAME}-lamp-red", [dot_glow(9, RED_LAMP, core=1.1, halo=2.4)], single=True)
    update_pivots({
        "segment_rule": (
            "heading h (deg, cw from down): d = h mod 360, q = floor(d / 90), r = d - 90 q; r <= 45: frame round(r / 45 "
            "* n8), else frame round((90 - r) / 45 * n8) mirrored by (x, y) -> (-y, -x); then q quarter turns cw by "
            "(x, y) -> (-y, x); mirrored offset (-y0 - h, -x0 - w), turned offset (-y0 - h, x0), the size swapped each "
            "time (tools/art/iron_sovereign.py's rule, QG-1)"),
        "rings": {"ring": {
            "sprite": f"{ROOT_NAME}-segment", "band": [R_IN, R_OUT], "mount_radius": R, "segments": SEGMENTS,
            "batteries": SEGMENTS, "battery_kinds": [kind_of(j) for j in range(SEGMENTS)], "n8": n8(),
            "edge_step_px": EDGE_STEP, "headings_deg": [round(h, 4) for h in heads],
            "frames": [{"size": list(f.size), "offset": list(r[1])} for f, r in zip(frames, res)],
            "segment_of": "segment j at angle + j * 60 degrees carries mount j: even j a gun turret, odd j a rail "
                          "turret",
            "lamps": {"radius": round(R_IN + 2.0, 2), "angles_deg": [-LAMP_DEG, LAMP_DEG],
                      "gold": f"{ROOT_NAME}-lamp-gold", "red": f"{ROOT_NAME}-lamp-red",
                      "note": "two per mount, the angles from the mount's: gold out of the arc, red in it, none "
                              "asleep or on a dead mount"},
            "arc": {"sprite": f"{ROOT_NAME}-arc", "from_radius": R_IN + 3, "to_radius": R_OUT - 3,
                    "degrees": 120}}},
        "overlays": {"rim": {"sprite": f"{ROOT_NAME}-rim", "blend": "additive", "scale": 1},
                     "shade": {"sprite": f"{ROOT_NAME}-shade", "blend": "normal", "scale": 1},
                     "scorch": {"sprite": f"{ROOT_NAME}-scorch", "blend": "normal", "scale": 1, "when": "wreck"},
                     "order": "segments, shade, rim, arc band, lamps (all centred on the platform)"},
        "shadow": dict(SHADOW, note="the segments, the core and the turrets drawn once more tinted (0, 0, 0, alpha) "
                                    "at this offset before them (the low Sun's long shadow, top left to bottom right)"),
        "wreck_tint": WRECK_TINT})
    print(f"ring: band {R_IN}..{R_OUT} px, n8 {n8()}, {len(frames)} frames")


def build_turrets():
    jobs = [("turret", kind, k) for kind in KINDS for k in range(HEADINGS)]
    res = run(jobs)
    for i, kind in enumerate(KINDS):
        save_set(f"{ROOT_NAME}-{kind}", artkit.quantize_set(res[i * HEADINGS:(i + 1) * HEADINGS], 32))
    wrecks = run([("wreck", kind) for kind in KINDS])
    for kind, img in zip(KINDS, artkit.quantize_set(wrecks, 32)):
        save_set(f"{ROOT_NAME}-{kind}-wreck", [img], single=True)
    save_set(f"{ROOT_NAME}-gun-muzzle", [muzzle_glow(f) for f in range(4)])
    save_set(f"{ROOT_NAME}-rail-charge", [coil_glow(f) for f in range(4)])
    update_pivots({"turrets": {
        kind: {"sprite": f"{ROOT_NAME}-{kind}", "size": TUR, "headings": HEADINGS,
               "heading_rule": "k = round(aim * 64 / 360) mod 64, the aim in degrees cw from down",
               "wreck": f"{ROOT_NAME}-{kind}-wreck",
               "muzzle": [[to_px(x, y, k) for x, y in MUZZLES[kind]] for k in range(HEADINGS)],
               **({"tell": f"{ROOT_NAME}-gun-muzzle", "tell_note": "0.3 s, frames 0..3 rising, on every muzzle"}
                  if kind == "gun" else
                  {"coils": [to_px(*COILS, k) for k in range(HEADINGS)], "charge": f"{ROOT_NAME}-rail-charge",
                   "charge_note": "the line's 1 s telegraph, frames 0..3 rising, on the coils"})}
        for kind in KINDS}})


def build_core():
    frames = run([("core", i) for i in range(CORE_FRAMES)])
    wreck = run([("core-wreck",)])[0]
    q = artkit.quantize_set(frames + [wreck], 40)
    save_set(f"{ROOT_NAME}-core", q[:CORE_FRAMES])
    save_set(f"{ROOT_NAME}-core-wreck", [q[-1]], single=True)
    save_set(f"{ROOT_NAME}-barrier", [barrier_frame(f) for f in range(BARRIER_FRAMES)])
    save_set(f"{ROOT_NAME}-barrier-hit", [barrier_hit(f) for f in range(6)])
    save_set(f"{ROOT_NAME}-eye-glow", [eye_glow(f) for f in range(4)])
    save_set(f"{ROOT_NAME}-eye-flare", [eye_flare(f) for f in range(4)])
    update_pivots({"core": {
        "sprite": f"{ROOT_NAME}-core", "size": CORE, "centred": True, "hitbox": [60, 60],
        "frames": "0 shut (the eye dark: the barrier stands), 1..4 the eye waking through the barrier's 0.5 s fade, "
                  "5 open, 6..9 the 0.5 s flare (6 -> 9, then back to 5)",
        "wreck": f"{ROOT_NAME}-core-wreck",
        "barrier": {"sprite": f"{ROOT_NAME}-barrier", "radius": BARRIER_R, "frames": BARRIER_FRAMES,
                    "steps_per_frame": 20, "blend": "additive", "fade_seconds": 0.5,
                    "hit": f"{ROOT_NAME}-barrier-hit"},
        "eye_glow": {"sprite": f"{ROOT_NAME}-eye-glow", "steps_per_frame": 8, "when": "open"},
        "eye_flare": {"sprite": f"{ROOT_NAME}-eye-flare", "seconds": 0.5, "when": "the open core's tell"}}})


def build_fx():
    save_set(f"{ROOT_NAME}-cookoff", cookoff_frames())
    save_set(f"{ROOT_NAME}-cookoff-debris", [debris_frame(i) for i in range(10)])
    save_set(f"{ROOT_NAME}-blowout", [blowout_frame(i) for i in range(12)])
    save_set(f"{ROOT_NAME}-scorch", [scorch_overlay()], single=True)
    update_pivots({"death": {
        "order": "the eye's blowout (-blowout at 20 fps, centred) -> the standing turrets cook off round the ring 0.15 s "
                 "apart (-cookoff at 30 fps with -cookoff-debris, each on its mount) -> the shared "
                 "explosion-vacuum-large and explosion-vacuum-debris-large on the centre with a gold-white flash; the "
                 "ring stops; the wreck: segments x wreck_tint, -scorch, -core-wreck, every mount's -wreck",
        "chain_seconds": 0.15, "blowout_fps": 20, "cookoff_fps": 30}})


def intel_hi():
    """The platform at rest for the hangar intel's 30 px portrait (an 8x render, 240x240): the r07 ring round the core
    in the elite livery under the default sun, the gloss-black hull lifted 3x so the ring reads as a ring at 30 px (the
    turrets, a few pixels there, left out)."""
    ring, mats = e72.halo_ring_v("a", RU)
    mats = list(mats)
    mats[A_HULL] = replace(mats[A_HULL], albedo=tuple(np.array(mats[A_HULL].albedo) * 3.0))
    core, _ = e72.halo_core_v("a", 0.0)

    def scene(p):
        return union(ring(p), bu.scaled(core, CORE_PXU / PXU)(p))
    return sdf.render(scene, mats, (240, 240), 2 * (R_OUT + 6) / PXU * 0.92)


def build_intel():
    import intel  # noqa: E402  (registers halo-platform; imported late: it imports this module)
    intel.build(["halo-platform"])


# =========================================================================== review helpers

_FRAMES = {}


def L(name):
    if name not in _FRAMES:
        _FRAMES[name] = load(name)
    return _FRAMES[name]


def add_at(img, glow, xy, k=1.0):
    """``glow`` added onto ``img`` at ``xy`` (top left) as the game blends it, at strength k."""
    x, y = int(round(xy[0])), int(round(xy[1]))
    x0, y0 = max(0, x), max(0, y)
    x1, y1 = min(img.width, x + glow.width), min(img.height, y + glow.height)
    if x1 <= x0 or y1 <= y0:
        return img
    b = np.array(img.crop((x0, y0, x1, y1))).astype(np.float64)
    g = np.array(glow.crop((x0 - x, y0 - y, x1 - x, y1 - y))).astype(np.float64)
    b[..., :3] = np.minimum(255, b[..., :3] + g[..., :3] * g[..., 3:4] / 255 * k)
    img.paste(Image.fromarray(b.astype(np.uint8), img.mode), (x0, y0))
    return img


def add_c(img, glow, cx, cy, k=1.0):
    return add_at(img, glow, (cx - glow.width / 2, cy - glow.height / 2), k)


def tinted(img, rgb_k=None, black_alpha=None):
    a = np.array(img).astype(np.float64)
    if black_alpha is not None:
        a[..., :3] = 0
        a[..., 3] *= black_alpha
    elif rgb_k is not None:
        a[..., :3] *= rgb_k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def paste_c(dst, img, cx, cy):
    dst.alpha_composite(img, (int(round(cx - img.width / 2)), int(round(cy - img.height / 2))))


def ring_layer(angle, size, centre, dead=(), tint=None, shadow=True, segs=None):
    """The segments placed by the rule at ring angle ``angle`` (+ their shadow) on a transparent layer."""
    piv = load_pivots()["rings"]["ring"]
    segs = segs or L(f"{ROOT_NAME}-segment")
    lay = Image.new("RGBA", size, (0, 0, 0, 0))
    placed = []
    for j in range(SEGMENTS):
        k, mir, q = seg_pose(angle + j * 60.0)
        img, (x0, y0) = place(segs[k], piv["frames"][k]["offset"], mir, q)
        if tint is not None:
            img = tinted(img, tint)
        placed.append((img, (centre[0] + x0, centre[1] + y0)))
    if shadow:
        for img, (x, y) in placed:
            lay.alpha_composite(tinted(img, black_alpha=SHADOW["alpha"]), (x + SHADOW["offset"][0],
                                                                             y + SHADOW["offset"][1]))
    for img, xy in placed:
        lay.alpha_composite(img, xy)
    return lay


def arc_band(img, cx, cy, bearing_deg, k=1.0):
    """The arc strip tiled round the live 120 degrees as the game would (a band of thin quads)."""
    strip = L(f"{ROOT_NAME}-arc")[0]
    a = np.array(strip).astype(np.float64)[..., :3] * (np.array(strip)[..., 3:4] / 255)
    prof = a[0]                                         # across the band
    r0 = R_IN + 3
    box = (int(cx - R_OUT - 2), int(cy - R_OUT - 2), int(cx + R_OUT + 3), int(cy + R_OUT + 3))
    b = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    x, y = xx - cx, yy - cy
    r = np.hypot(x, y)
    head = np.degrees(np.arctan2(-x, y))                 # cw from down
    off = np.abs((head - bearing_deg + 180) % 360 - 180)
    i = np.clip((r - r0).astype(int), 0, len(prof) - 1)
    on = (off <= 60) & (r >= r0) & (r < r0 + len(prof))
    edge = (off > 58.5) & on
    add = prof[i] * on[..., None] * k
    add[edge] = np.maximum(add[edge], prof[0][None] * k)
    b[..., :3] = np.clip(b[..., :3] + add, 0, 255)
    img.paste(Image.fromarray(b.astype(np.uint8), "RGBA"), box)
    return img


def heading_to(px, py, sx, sy):
    """Screen degrees cw from down of the vector (px, py) -> (sx, sy)."""
    return float(np.degrees(np.arctan2(-(sx - px), sy - py)))


def tur_index(deg):
    return int(round(deg * HEADINGS / 360.0)) % HEADINGS


def platform(img, cx, cy, angle, bearing=None, dead=(), barrier=1.0, core_frame=0, lamps=True, aim=None,
             barrier_f=0, wreck=False, tells=()):
    """The platform composed as the game draws it, onto ``img`` (RGBA) with the core at (cx, cy)."""
    core = L(f"{ROOT_NAME}-core-wreck")[0] if wreck else L(f"{ROOT_NAME}-core")[core_frame]
    sh = tinted(core, black_alpha=SHADOW["alpha"])
    paste_c(img, sh, cx + SHADOW["offset"][0], cy + SHADOW["offset"][1])
    paste_c(img, core, cx, cy)
    lay = ring_layer(angle, img.size, (cx, cy), tint=WRECK_TINT if wreck else None)
    img.alpha_composite(lay)
    paste_c(img, L(f"{ROOT_NAME}-shade")[0], cx, cy)
    img = add_c(img, L(f"{ROOT_NAME}-rim")[0], cx, cy)
    if wreck:
        paste_c(img, L(f"{ROOT_NAME}-scorch")[0], cx, cy)
    if bearing is not None and not wreck:
        img = arc_band(img, cx, cy, bearing)
    for j in range(SEGMENTS):
        a = angle + j * 60.0
        mx, my = screen_xy(R, a)
        x, y = cx + mx, cy + my
        kind = kind_of(j)
        if wreck or j in dead:
            w = L(f"{ROOT_NAME}-{kind}-wreck")[0]
            paste_c(img, tinted(w, black_alpha=SHADOW["alpha"]), x + 3, y + 4)
            paste_c(img, w, x, y)
            continue
        live = bearing is not None and abs((a - bearing + 180) % 360 - 180) <= 60
        if lamps:
            lamp = L(f"{ROOT_NAME}-lamp-red" if live else f"{ROOT_NAME}-lamp-gold")[0]
            for dl in (-LAMP_DEG, LAMP_DEG):
                lx, ly = screen_xy(R_IN + 2.0, a + dl)
                img = add_c(img, lamp, cx + lx, cy + ly)
        k = tur_index(aim[j] if aim is not None else a)
        t = L(f"{ROOT_NAME}-{kind}")[k]
        paste_c(img, tinted(t, black_alpha=SHADOW["alpha"]), x + 3, y + 4)
        paste_c(img, t, x, y)
        for tj, frame in tells:
            if tj == j:
                piv = load_pivots()["turrets"][kind]
                if kind == "gun":
                    for mx_, my_ in piv["muzzle"][k]:
                        img = add_c(img, L(f"{ROOT_NAME}-gun-muzzle")[frame], x + mx_, y + my_)
                else:
                    c = piv["coils"][k]
                    img = add_c(img, L(f"{ROOT_NAME}-rail-charge")[frame], x + c[0], y + c[1])
    if barrier > 0 and not wreck:
        img = add_c(img, L(f"{ROOT_NAME}-barrier")[barrier_f % BARRIER_FRAMES], cx, cy, barrier)
    if core_frame >= 5 and not wreck:
        img = add_c(img, L(f"{ROOT_NAME}-eye-glow")[barrier_f % 4], cx, cy, 0.8)
    return img


_BG = {}


def deck(h=540, seed=4119):
    """Level 41's citadel deck under the platform (round 72's concept plate: dark ice, the black deck)."""
    key = (h, seed)
    if key not in _BG:
        bg = e72.ice_bg(seed, h, deck=(14, 466))
        _BG[key] = (bg if isinstance(bg, Image.Image) else Image.fromarray(bg)).convert("RGBA")
    return _BG[key].copy()


def ice(h=540, seed=4101):
    import act6_r72 as a6  # noqa: E402
    key = ("ice", h, seed)
    if key not in _BG:
        _BG[key] = a6.to_img(a6.ground_l41(seed)[:h]).convert("RGBA")
    return _BG[key].copy()


def ship_img():
    import act6_r72 as a6  # noqa: E402
    return a6.Prod.get("ship", 2)


def explosion(name, i):
    fs = artkit.load_frames(name)
    return fs[i] if 0 <= i < len(fs) else None


# =========================================================================== review: the held arena

class Fight:
    """The review GIF's script: the ship weaving under the platform, mounts lost at KILLS (each speed-up +8 deg/s
    eased over 0.8 s), the barrier fading at the third, the eye's flares and rings, the death at DEATH."""
    DUR = 15.0
    KILLS = {1: 2.6, 2: 4.6, 4: 6.6}                     # mount -> its fall (the third opens the core)
    DEATH = 11.2
    BASE, UP = 20.0, 8.0

    def __init__(self):
        dt = 1 / 600
        self.ts = np.arange(0, self.DUR + 1, dt)
        w = np.array([self.speed(t) for t in self.ts])
        self.ang = np.concatenate([[0.0], np.cumsum(w[:-1]) * dt])

    def speed(self, t):
        if t >= self.DEATH:
            return 0.0
        u = sum(min(1.0, max(0.0, (t - k) / 0.8)) for k in self.KILLS.values())
        return self.BASE + self.UP * u

    def angle(self, t):
        return float(np.interp(t, self.ts, self.ang))


def fight_frames():
    fight = Fight()
    bg = deck(400)
    cx, cy = 240, 190
    ship = ship_img()
    frames = []
    bullets = []                                   # (t0, x0, y0, vx, vy)
    lines = []                                     # (t0, j, angle_deg at lock, x, y)
    last_line = -9.0
    flares = [8.0, 10.0]
    bar = fight.KILLS[4]
    for i in range(int(fight.DUR * FPS)):
        t = i / FPS
        img = bg.copy()
        sx = 240 + 130 * np.sin(t * 0.55 + 0.3)
        sy = 352.0
        ang = fight.angle(t)
        bearing = heading_to(cx, cy, sx, sy)
        dead = {j for j, k in fight.KILLS.items() if t >= k}
        wreck = t >= fight.DEATH + 0.9
        if t >= fight.DEATH:
            dead = set(range(SEGMENTS))
        aim, tells = [], []
        for j in range(SEGMENTS):
            a = ang + j * 60.0
            mx, my = screen_xy(R, a)
            aim.append(heading_to(cx + mx, cy + my, sx, sy))
            live = abs((a - bearing + 180) % 360 - 180) <= 60
            if j in dead or not live or t < 0.6:
                continue
            if kind_of(j) == "gun":
                ph = (t - 0.8 * j) % 2.4
                if 2.1 <= ph < 2.4:
                    tells.append((j, min(3, int((ph - 2.1) / 0.075))))
                for q_ in range(3):
                    if abs(ph - 0.12 * q_) < 0.5 / FPS and t > 1.0:
                        dx, dy = sx - (cx + mx), sy - (cy + my)
                        d = np.hypot(dx, dy)
                        bullets.append((t, cx + mx + dx / d * 16, cy + my + dy / d * 16, dx / d * 160, dy / d * 160))
            elif t - last_line >= 2.2 and t > 1.5 and np.hypot(sx - cx - mx, sy - cy - my) > 100 and t < fight.DEATH - 1:
                lead = 12.0
                lines.append((t, j, heading_to(cx + mx, cy + my, sx + lead, sy)))
                last_line = t
        core_frame = 0
        if t >= bar:
            core_frame = min(5, int((t - bar) / 0.1) + 1)
        for f in flares:
            if f <= t < f + 0.5:
                core_frame = 6 + min(3, int((t - f) / 0.125))
        barrier = 1.0 if t < bar else max(0.0, 1 - (t - bar) / 0.5)
        img = platform(img, cx, cy, ang, None if t >= fight.DEATH else bearing, dead=dead, barrier=barrier,
                       core_frame=core_frame, aim=aim, barrier_f=i // 2, wreck=wreck, tells=tells)
        for f in flares:
            if f <= t < f + 0.5:
                img = add_c(img, L(f"{ROOT_NAME}-eye-flare")[min(3, int((t - f) / 0.125))], cx, cy)
            if f + 0.5 <= t < fight.DEATH:
                r_ = (t - f - 0.5) * 100
                for q_ in range(10):
                    a_ = TAU * q_ / 10 + 0.2
                    bx, by = cx + np.cos(a_) * (40 + r_), cy + np.sin(a_) * (40 + r_)
                    paste_c(img, artkit.load_frames("orb")[0], bx, by)
        # the rail lines: a 1 s red telegraph locked at its start, then the 0.2 s live band
        for t0, j, a_lock, in [(l_[0], l_[1], l_[2]) for l_ in lines]:
            if j in dead or t >= fight.DEATH:
                continue
            u = t - t0
            a = ang + j * 60.0
            mx, my = screen_xy(R, a)
            x0, y0 = cx + mx, cy + my
            if 0 <= u < 1.2:
                piv = load_pivots()["turrets"]["rail"]
                k = tur_index(a_lock)
                c = piv["coils"][k]
                img = add_c(img, L(f"{ROOT_NAME}-rail-charge")[min(3, int(u / 0.25))], x0 + c[0], y0 + c[1])
                img = laser_line(img, x0, y0, a_lock, live=u >= 1.0, k=0.6 + 0.4 * (int(t * 10) % 2))
        for b in bullets:
            u = t - b[0]
            if 0 <= u < 3:
                paste_c(img, artkit.load_frames("orb")[0], b[1] + b[3] * u, b[2] + b[4] * u)
        # mounts lost: the medium vacuum explosion on the mount
        for j, k_ in fight.KILLS.items():
            n = int((t - k_) * 30)
            e = explosion("explosion-vacuum-medium", n) if n >= 0 else None
            if e is not None:
                mx, my = screen_xy(R, ang + j * 60.0)
                img = add_c(img, e, cx + mx, cy + my)
        # the death: the blowout, the chain round the ring, the large explosion
        if t >= fight.DEATH:
            u = t - fight.DEATH
            e = L(f"{ROOT_NAME}-blowout")[int(u * 20)] if u * 20 < 12 else None
            if e is not None:
                img = add_c(img, e, cx, cy)
            alive = [j for j in range(SEGMENTS) if j not in fight.KILLS]
            for n_, j in enumerate(alive):
                uu = u - 0.3 - 0.15 * n_
                if 0 <= uu * 30 < 10:
                    mx, my = screen_xy(R, fight.angle(fight.DEATH) + j * 60.0)
                    img = add_c(img, L(f"{ROOT_NAME}-cookoff")[int(uu * 30)], cx + mx, cy + my)
                    paste_c(img, L(f"{ROOT_NAME}-cookoff-debris")[int(uu * 30)], cx + mx, cy + my)
            ul = u - 0.3 - 0.15 * len(alive)
            e = explosion("explosion-vacuum-large", int(ul * 30)) if ul >= 0 else None
            if e is not None:
                img = add_c(img, e, cx, cy)
            d_ = explosion("explosion-vacuum-debris-large", int(ul * 30)) if ul >= 0 else None
            if d_ is not None:
                paste_c(img, d_, cx, cy)
            if 0 <= ul < 0.2:
                fl = np.array(img).astype(np.float64)
                fl[..., :3] = np.clip(fl[..., :3] + np.array((255, 236, 170)) * 0.5 * (1 - ul / 0.2), 0, 255)
                img = Image.fromarray(fl.astype(np.uint8), "RGBA")
        paste_c(img, ship, sx, sy)
        frames.append(img)
    return frames


def laser_line(img, x0, y0, ang_deg, live=False, k=1.0):
    """The rail turret's laser-line (the Rail Bunker's): a thin red telegraph, then the 8 px live band (review only)."""
    a = np.radians(ang_deg)
    dx, dy = -np.sin(a), np.cos(a)
    arr = np.array(img).astype(np.float64)
    yy, xx = np.mgrid[0:img.height, 0:img.width] + 0.5
    h = np.clip((xx - x0) * dx + (yy - y0) * dy, 0, None)
    d = np.hypot(xx - x0 - dx * h, yy - y0 - dy * h)
    if live:
        f, col = np.clip(4.5 - d, 0, 1) + np.exp(-(d / 7) ** 2) * 0.5, np.array((255, 230, 220))
    else:
        f, col = np.exp(-(d / 0.8) ** 2) * 0.9 + np.exp(-(d / 2.5) ** 2) * 0.25, np.array((255, 50, 40))
    f = smooth_steps(np.clip(f * k, 0, 1), 48)[..., None] * (h > 14)[..., None]
    arr[..., :3] = np.clip(arr[..., :3] + f * col, 0, 255)
    return Image.fromarray(arr.astype(np.uint8), "RGBA")


def gold_share(img):
    return e60.gold_share(img)


def atlas_area(frames):
    return sum((f.width + 2) * (f.height + 2) for f in frames)


def review():
    gif = fight_frames()
    segs = L(f"{ROOT_NAME}-segment")
    piv = load_pivots()
    at_rest = platform(deck(300).crop((0, 0, 300, 300)), 150, 150, 7.0, bearing=0.0, barrier_f=0)
    open_c = platform(deck(300).crop((0, 0, 300, 300)), 150, 150, 31.0, bearing=40.0, dead={1, 2, 4}, barrier=0.0,
                      core_frame=5, barrier_f=3)
    wrecked = platform(deck(300).crop((0, 0, 300, 300)), 150, 150, 31.0, wreck=True)
    ring_only = ring_layer(7.0, (300, 300), (150, 150), shadow=False)
    total = sum(atlas_area(load(n)) for n, *_ in sets())
    rows = [
        ("THE PLATFORM 1X OVER LEVEL 41'S CITADEL DECK: AT REST (BARRIER UP, ARC TOWARD THE SHIP BELOW), THREE MOUNTS "
         "LOST (THE CORE OPEN, ITS EYE LIT), THE WRECK", [at_rest, open_c, wrecked], 1, False),
        (f"HALO-PLATFORM-SEGMENT_0..{n8()}: TOP-LIT, 0..45 DEG IN {n8()} STEPS (EVERY 12TH SHOWN; QG-1'S SEGMENT RULE)",
         segs[::12], 2, False),
        ("HALO-PLATFORM-GUN / -RAIL: 64 HEADINGS (EVERY 8TH), THE WRECKS", L(f"{ROOT_NAME}-gun")[::8]
         + L(f"{ROOT_NAME}-rail")[::8] + L(f"{ROOT_NAME}-gun-wreck") + L(f"{ROOT_NAME}-rail-wreck"), 2, False),
        ("HALO-PLATFORM-CORE_0..9 (SHUT, WAKING, OPEN, THE FLARE), -CORE-WRECK", L(f"{ROOT_NAME}-core")
         + L(f"{ROOT_NAME}-core-wreck"), 1, False),
        ("THE BARRIER (EVERY 3RD OF 12), ITS HIT, THE EYE'S GLOW AND FLARE, THE GUN'S MUZZLE TELL, THE RAIL'S CHARGE, "
         "THE LAMPS (ADDITIVE)", L(f"{ROOT_NAME}-barrier")[::3] + L(f"{ROOT_NAME}-barrier-hit")[::2]
         + L(f"{ROOT_NAME}-eye-glow")[:2] + L(f"{ROOT_NAME}-eye-flare") + L(f"{ROOT_NAME}-gun-muzzle")
         + L(f"{ROOT_NAME}-rail-charge") + L(f"{ROOT_NAME}-lamp-gold") + L(f"{ROOT_NAME}-lamp-red"), 1, True),
        ("THE DEATH: -BLOWOUT (EVERY 2ND), -COOKOFF (EVERY 2ND, ADDITIVE)", L(f"{ROOT_NAME}-blowout")[::2]
         + L(f"{ROOT_NAME}-cookoff")[::2], 1, True),
        ("-COOKOFF-DEBRIS (EVERY 2ND), THE OVERLAYS: -SHADE (NORMAL), -SCORCH (NORMAL)",
         L(f"{ROOT_NAME}-cookoff-debris")[::2] + L(f"{ROOT_NAME}-shade") + L(f"{ROOT_NAME}-scorch"), 1, False),
        ("THE OVERLAYS: -RIM (ADDITIVE, STATIC), THE ARC STRIP 4X", L(f"{ROOT_NAME}-rim")
         + [sprite.enlarge(L(f"{ROOT_NAME}-arc")[0], 4)], 1, True),
        ("KEY FRAMES OF THE GIF (1X, CROPPED): THE ARC AND A GUN BURST, A MOUNT LOST, THE BARRIER FADING",
         [gif[k].crop((40, 20, 440, 380)) for k in (int(1.6 * FPS), int(2.75 * FPS), int(6.85 * FPS))], 1, False),
        ("KEY FRAMES: THE OPEN CORE'S RING AND A RAIL LINE, THE CHAIN, THE WRECK",
         [gif[k].crop((40, 20, 440, 380)) for k in (int(8.9 * FPS), int(11.75 * FPS), int(14.0 * FPS))], 1, False)]
    artkit.REVIEW_ROUND = ROUND
    sheet = artkit.review_sheet("HALO PLATFORM - FINAL FRAMES (ROUND 72 A.6 A, QG-1 SEGMENT RULE)", rows, width=1500,
                                batch=BATCH)
    sheet = with_notes(sheet, [
        f"GOLD {100 * gold_share(ring_only):.0f} % OF THE RING'S PIXELS (THE ELITE RULE: 35-45 % OF THE SILHOUETTE WITH "
        f"THE MOUNTS). BAND {piv['rings']['ring']['band']} PX, MOUNTS AT {R} PX, THE CORE 76 PX ACROSS.",
        f"ATLAS: {total:,} PX = {100 * total / 2048 ** 2:.0f} % OF ONE 2048X2048 PAGE (2 PX PADDING): ONE PAGE "
        f"(R07'S 128 RING FRAMES WERE ABOUT 2 PAGES)."])
    png, gifp = review_paths(ROOT_NAME)
    sheet.convert("RGB").save(png, optimize=True)
    size = write_gif([g.crop((0, 0, 480, 400)) for g in gif], gifp, fps=FPS, colors=128, max_mb=8.0)
    print(f"review: {png.relative_to(ROOT)}, {gifp.name} ({size / 1e6:.1f} MB)")
    rotation_review()


# =========================================================================== review: QG-1's rotation proof

def direct(deg):
    """A segment rendered straight at ``deg`` (any heading), quantised to the written palette, with its offset."""
    img, off = run([("direct", float(deg))])[0]
    img = artkit.quantize_set(L(f"{ROOT_NAME}-segment") + [img], SEG_COLOURS)[-1]
    return img, off


def rule_rows():
    """Direct renders at headings off the set against the rule's turned frame: shape pixels and the mean colour
    difference where both are opaque."""
    piv = load_pivots()["rings"]["ring"]
    rows = []
    for deg in (101.3, 197.0, 286.4, 333.7):
        ref, off = direct(deg)
        k, mir, q = seg_pose(deg)
        img, (x0, y0) = place(L(f"{ROOT_NAME}-segment")[k], piv["frames"][k]["offset"], mir, q)
        lay_a = Image.new("RGBA", (300, 300), (0, 0, 0, 0))
        lay_b = Image.new("RGBA", (300, 300), (0, 0, 0, 0))
        lay_a.alpha_composite(ref, (150 + off[0], 150 + off[1]))
        lay_b.alpha_composite(img, (150 + x0, 150 + y0))
        a, b = np.array(lay_a).astype(int), np.array(lay_b).astype(int)
        shape = int(((a[..., 3] > 0) != (b[..., 3] > 0)).sum())
        both = (a[..., 3] > 0) & (b[..., 3] > 0)
        diff = float(np.abs(a[both, :3] - b[both, :3]).mean()) if both.any() else 0.0
        box = lay_a.getchannel("A").getbbox()
        rows.append((deg, k, mir, q, shape, int((a[..., 3] > 0).sum()), diff, lay_a.crop(box), lay_b.crop(box)))
    return rows


def edge_steps(speed, fps=60, seconds=3.0):
    """How far the ring's outer edge jumps from one displayed frame to the next at ``speed`` deg/s (the frame's own
    heading vs the true one), px: (mean step, max step, max lag)."""
    steps, lags = [], []
    prev = None
    for i in range(int(seconds * fps)):
        true = speed * i / fps
        k, mir, q = seg_pose(true)
        shown = 90 * q + (90 - 45 * k / n8() if mir else 45 * k / n8())
        lags.append(abs((shown - true + 180) % 360 - 180) * np.pi / 180 * R_OUT)
        if prev is not None:
            steps.append(abs((shown - prev + 180) % 360 - 180) * np.pi / 180 * R_OUT)
        prev = shown
    return float(np.mean(steps)), float(np.max(steps)), float(np.max(lags))


def review_paths(subject):
    """``<subject>-final-r79-a`` in the part's concept folder (another module may have moved artkit.REVIEW_ROUND)."""
    artkit.REVIEW_ROUND = ROUND
    return CONCEPT / f"{subject}-final-{ROUND}-a.png", CONCEPT / f"{subject}-final-{ROUND}-a.gif"


def with_notes(sheet, lines):
    """The sheet with text lines below it."""
    out = Image.new("RGBA", (sheet.width, sheet.height + 14 * len(lines) + 8), raster.SHEET_BG)
    out.alpha_composite(sheet, (0, 0))
    for i, line in enumerate(lines):
        raster.draw_text(out, 16, sheet.height + 14 * i, line, raster.LABEL_DIM)
    return out


def rotation_review():
    plate = deck(260).crop((60, 0, 320, 260))
    rows, notes = [], []
    for speed in (20.0, 60.0):
        cells = []
        for i in range(10):
            ang = 3.0 + speed * i / 60.0
            img = plate.copy()
            img.alpha_composite(ring_layer(ang, img.size, (130, 130)))
            paste_c(img, L(f"{ROOT_NAME}-shade")[0], 130, 130)
            img = add_c(img, L(f"{ROOT_NAME}-rim")[0], 130, 130)
            cells.append(img.crop((10, 130, 130, 250)))
        m, mx, lag = edge_steps(speed)
        notes.append(f"{speed:.0f} DEG/S: THE OUTER EDGE (R {R_OUT:.0f}) TRULY MOVES {speed / 60 * np.pi / 180 * R_OUT:.2f} "
                     f"PX A FRAME; THE SHOWN FRAMES STEP {m:.2f} PX ON AVERAGE, AT MOST {mx:.2f} PX, AND TRAIL THE TRUE "
                     f"HEADING BY AT MOST {lag:.2f} PX (N8 = {n8()}: ONE FRAME STEP MOVES THE EDGE <= {EDGE_STEP:.0f} PX)")
        rows.append((f"{speed:.0f} DEG/S AT 60 FPS, 1:1: TEN CONSECUTIVE FRAMES OF THE LOWER LEFT QUARTER", cells, 1,
                     False))
        rows.append((f"{speed:.0f} DEG/S: THE FIRST FIVE OF THEM 3X (A JOIN AND A MOUNT RING)",
                     [c.crop((30, 30, 110, 110)).resize((240, 240), Image.NEAREST) for c in cells[:5]], 1, False))
    rr = rule_rows()
    cells = []
    for deg, k, mir, q, shape, area, diff, a, b in rr:
        cell = deck(a.height + 8, 4121).crop((100, 0, 100 + a.width * 2 + 14, a.height + 8))
        cell.alpha_composite(a, (4, 4))
        cell.alpha_composite(b, (a.width + 10, 4))
        cells.append(cell)
        notes.append(f"{deg:.1f} DEG: THE RULE DRAWS FRAME {k}{' MIRRORED' if mir else ''}, {q} QUARTER TURNS: "
                     f"{shape} OF {area} PX DIFFER IN SHAPE ({100 * shape / area:.1f} %), MEAN COLOUR DIFFERENCE "
                     f"{diff:.1f} OF 255 WHERE BOTH ARE OPAQUE")
    rows.append(("THE RULE AGAINST DIRECT RENDERS AT HEADINGS OFF THE SET, 1X (EACH PAIR: THE DIRECT RENDER LEFT, THE "
                 "RULE'S TURNED AND MIRRORED FRAME RIGHT)", cells, 1, False))
    artkit.REVIEW_ROUND = ROUND
    sheet = artkit.review_sheet("HALO PLATFORM - QG-1: THE SEGMENT RULE TURNING AT 20 AND 60 DEG/S (1:1)", rows,
                                width=1500, batch=BATCH)
    sheet = with_notes(sheet, notes)
    png, gifp = review_paths("halo-rotation")
    sheet.convert("RGB").save(png, optimize=True)
    frames = []
    for i in range(int(4.0 * 50)):
        img = deck(260, 4120).crop((0, 0, 480, 260))
        for cx, speed in ((120, 20.0), (360, 60.0)):
            ang = speed * i / 50.0
            img.alpha_composite(ring_layer(ang, img.size, (cx, 130)))
            paste_c(img, L(f"{ROOT_NAME}-core")[0], cx, 130)
            paste_c(img, L(f"{ROOT_NAME}-shade")[0], cx, 130)
            img = add_c(img, L(f"{ROOT_NAME}-rim")[0], cx, 130)
            for j in range(SEGMENTS):
                mx, my = screen_xy(R, ang + j * 60.0)
                paste_c(img, L(f"{ROOT_NAME}-{kind_of(j)}")[0], cx + mx, 130 + my)
            raster.draw_text(img, cx - 40, 248, f"{speed:.0f} DEG/S", raster.LABEL)
        frames.append(img)
    size = write_gif(frames, gifp, fps=50, colors=128, max_mb=8.0)
    print(f"review: {png.relative_to(ROOT)}, {gifp.name} ({size / 1e6:.1f} MB)")


# =========================================================================== sets, check, main

def sets():
    """(name, frames, size or None, additive)."""
    out = [(f"{ROOT_NAME}-segment", n8() + 1, None, False), (f"{ROOT_NAME}-rim", 1, (OVERLAY, OVERLAY), True),
           (f"{ROOT_NAME}-shade", 1, (OVERLAY, OVERLAY), False), (f"{ROOT_NAME}-arc", 1, None, True),
           (f"{ROOT_NAME}-lamp-gold", 1, (9, 9), True), (f"{ROOT_NAME}-lamp-red", 1, (9, 9), True)]
    for kind in KINDS:
        out += [(f"{ROOT_NAME}-{kind}", HEADINGS, (TUR, TUR), False), (f"{ROOT_NAME}-{kind}-wreck", 1, (TUR, TUR), False)]
    out += [(f"{ROOT_NAME}-gun-muzzle", 4, (12, 12), True), (f"{ROOT_NAME}-rail-charge", 4, (16, 16), True),
            (f"{ROOT_NAME}-core", CORE_FRAMES, (CORE, CORE), False), (f"{ROOT_NAME}-core-wreck", 1, (CORE, CORE), False),
            (f"{ROOT_NAME}-barrier", BARRIER_FRAMES, (2 * (BARRIER_R + BARRIER_PAD),) * 2, True),
            (f"{ROOT_NAME}-barrier-hit", 6, (32, 32), True), (f"{ROOT_NAME}-eye-glow", 4, (48, 48), True),
            (f"{ROOT_NAME}-eye-flare", 4, (112, 112), True), (f"{ROOT_NAME}-cookoff", 10, (64, 64), True),
            (f"{ROOT_NAME}-cookoff-debris", 10, (64, 64), False), (f"{ROOT_NAME}-blowout", 12, (128, 128), True),
            (f"{ROOT_NAME}-scorch", 1, (OVERLAY, OVERLAY), False)]
    return out


def checker_share(img):
    a = np.array(img.convert("RGBA")).astype(np.int64)
    c = (a[..., 3] << 24) | (a[..., 0] << 16) | (a[..., 1] << 8) | a[..., 2]
    vis = a[1:-1, 1:-1, 3] > 0
    mid = c[1:-1, 1:-1]
    l_, r_, u_, d_ = c[1:-1, :-2], c[1:-1, 2:], c[:-2, 1:-1], c[2:, 1:-1]
    chk = vis & (l_ == r_) & (u_ == d_) & (l_ != mid) & (u_ != mid)
    return int(vis.sum()), int(chk.sum())


def glow_steps(frames):
    vals = np.concatenate([np.array(f)[..., :3].max(axis=-1).ravel() for f in frames])
    return len(np.unique(vals[vals > 0]))


def check(sets_=None, root=ROOT_NAME, source=SOURCE, extra=None):
    bad, total = 0, 0
    for name, n, size, add in sets_ or sets():
        place_ = l23kit.where(name)
        try:
            frames = load(name)
        except FileNotFoundError:
            frames = []
        folder = ROOT / place_
        files = [f for f in folder.glob(f"{name}*.png") if f.stem == name or (
            f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit())]
        src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == source for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        steps = glow_steps(frames) if add and frames else None
        small = bool(frames) and (frames[0].width * frames[0].height < 400 or min(frames[0].size) < 8)
        ok = (len(frames) == n and (size is None or all(fr.size == size for fr in frames)) and src_ok
              and share < 0.05 and (steps is None or small or steps >= 32))
        total += atlas_area(frames)
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {place_}/{name}: {len(frames)}/{n} frames {frames[0].size if frames else None}, "
              f"checker {share * 100:.1f} %" + (f", {steps} glow steps" + (" (a small light: exempt)" if small else "")
                                                 if steps is not None else ""))
    for line in (extra or (lambda: []))():
        bad += not line[0]
        print(("ok  " if line[0] else "BAD ") + line[1])
    print(f"{root} atlas: {total:,} px = {100 * total / 2048 ** 2:.1f} % of one 2048x2048 page (2 px padding)")
    return bad == 0


def extra_checks():
    piv = load_pivots()
    out = [(bool(piv.get("rings")) and len(piv["rings"]["ring"]["frames"]) == n8() + 1 and "turrets" in piv
            and "core" in piv and "death" in piv, f"{pivot_path().relative_to(ROOT)}")]
    path = artkit.SPRITES / "intel" / f"{ROOT_NAME}.png"
    out.append((path.exists() and Image.open(path).text.get(artkit.SOURCE_KEY, "").startswith("tools/art/"),
                str(path.relative_to(ROOT))))
    return out


BUILDERS = {"ring": build_ring, "turrets": build_turrets, "core": build_core, "fx": build_fx, "intel": build_intel}

if __name__ == "__main__":
    args = sys.argv[1:]
    piv0 = load_pivots().get("rings", {}).get("ring", {})
    if piv0.get("band"):
        R_IN, R_OUT = piv0["band"]
    if "--check" in args:
        sys.exit(0 if check(extra=extra_checks) else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    review()
