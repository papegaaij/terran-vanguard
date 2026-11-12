#!/usr/bin/env python3
"""Production art: the Choir Heart, Level 50's final boss (design/enemies/bosses/choir-heart; M11 part H batch, concept
round 95, step ART-H), round 84 A.7 b "the crown": a vast dome of near-black violet tissue banded with glowing rings and
fine curved spokes (the Choir Herald's ring markings), the Spore Bomber's sacs on its shoulders, a sealed boss over the
core marked with a whorl of eight spokes (the Whirl Seeds' spiral), the six membranes as pale violet mouths on its rim.
The hive-core kit, the closing-gate scene, the ending pages, the Choir's face frame and Level 50's briefing images are
step ART-H2's (tools/art/act7_core.py), not this file's.

The spec's geometry (the README's *Stat block* and *Arena*, screen px, y down): the piece's centre (240, 70); the organ
440x300 (an ellipse centred at (240, 50), its upper third beyond the top edge, its lowest point at y 200); the core and
the Heart's centre at (240, 150); the membranes at (90, 150), (150, 185), (210, 200), (270, 200), (330, 185), (390, 150).
The body is a signed-distance model rendered 1:1 at 3x under the fixed key light (top left), in strips on <= 6 workers;
its pulse is a smooth light layer: the body is drawn at the pulse's low and the ``-pulse`` layer (the high render minus
the low, unquantised) adds the rest with the pulse's alpha (<= 1 Hz in play, its peak below the bullets' cores). Every
glow and haze is a float field written unquantised (far more than 32 steps, no ordered dither) that fades to 0 inside
its canvas (``--check`` fails on a lit border pixel; the walls' tile strips wrap top to bottom, so only their sides are
checked). **No straight lines**: the rings, the spokes (swirled), the splits, the walls' ribs and face all bend;
``--check`` runs step FIXC-SPINE's row/column scan on every normal set (120 px; the 128 px wall strips 60 px).

Sprites (build/art-held/sprites/ until step H2's data claims the root ``choir-heart``, then assets/sprites/: belt_units'
rule; ``additive`` = premultiplied on black, GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha, 1-bit; offsets are px
from the piece's centre (240, 70), y down; pivots/choir-heart.json holds them all):

  The body (canvas 464x312, the piece's centre at (232, 176) in the canvas: screen x 8..472, y -106..206)
  choir-heart                     normal: phase 1's body, sealed (the membranes are parts drawn over it)
  choir-heart-closing_0..3        normal: the closing's contraction (the tissue clenching, the dome flattening, the rings
                                  standing out); 3 = phase 2's body
  choir-heart-opening_0..7        normal: the dome opening along its spokes from the core (the opening's 4 s: the first
                                  splits at 0, the eight flaps peeled back at 7); 7 = phase 3's body (the core in it)
  choir-heart-pulse_0..2          additive: the pulse's light at its peak over the sealed (0), contracted (1) and open
                                  (2) body; drawn with alpha = the pulse (0..1, in time with ``HEARTBEAT``)
  choir-heart-opening-light_0..7  additive: the light leaking out of the splits, frame by frame with the opening
  choir-heart-halo                480x400 additive at (0, 20), drawn under the body: the Heart's red-violet glow on the
                                  chamber round its rim (alpha 0.4 + 0.6 x the pulse; none after K)
  choir-heart-collapse_0..11      normal: K's chain (4 s, 3 fps): the open body greying and collapsing inward on the
                                  core, its rim breaking into chunks
  choir-heart-chain_0..9          128x128 additive: one lime-white burst of the chain (12 fps) at the pivots' ``chain``
                                  points and times (before and beside the built explosion-vacuum-large chain)
  choir-heart-debris_0..7         16x16 normal: a tissue chunk tumbling (the chain's debris rings, the vacuum rules)
  choir-heart-centre_0..5         96x96 additive at ``centre`` (0, 80): the Heart's line tell, the centre whitening
                                  (frame = the 0.8 s tell's progress, held at 5 under the drawn line)
  choir-heart-spark_0..3          24x24 additive: a shot sparking violet on a closed mouth or the body (no hit area)
  The membranes (parts m1..m6; 56x56, hitbox 40x40)
  choir-heart-membrane-<k>_0..12  normal, k = 1..6 (its song: 1 Herald's song, 2 Needler thorns, 3 spore drop, 4 seed
                                  scatter, 5 Choir spiral, 6 mote cloud): 0 closed, 1-4 irising open (the 0.8 s tell:
                                  open from its first frame, the window rule), 5 open; 6-11 the same below half its HP
                                  (torn, bruised); 12 dead (grey and shrivelled)
  choir-heart-membrane-<k>-singer_0..7   80x80 additive: the singer glowing in its unit's style while it sings (10 fps
                                  loop; alpha = the iris's progress): the Herald's ring light, the Needler's thorn glint,
                                  the Bomber's sac swelling, the seed's rattle, the spiral's lime pulse, the motes' shimmer
  choir-heart-membrane-death_0..7 96x96 additive: a membrane killed (violet-white burst and ring)
  choir-heart-membrane-tatters_0..7  64x64 normal: its petals' chips flung out
  The wall nodes (parts on the walls' inner faces at y 220 and 400; 48x48, hitbox 36x36)
  choir-heart-node-l_0..2, -node-r_0..2  normal: whole, damaged, dead (severed); its nerve strands reach into its own
                                  wall (l: toward -x, on the left wall; r: toward +x)
  choir-heart-node-glow_0..7      80x80 additive: the lime knot's pulse (8 fps loop)
  choir-heart-node-death_0..7     96x96 additive: the node cut (a lime snap); -node-tatters_0..7 64x64 normal
  The core (phase 3's part at ``centre`` (0, 80); 72x72, hitbox 56x56; the same model as the body's core)
  choir-heart-core_0..1           normal: whole, below half its HP (cracked, bleeding violet)
  choir-heart-core-glow_0..7      112x112 additive: its lime-white beat (frame = the 2.0 s heartbeat's phase)
  choir-heart-core-flare_0..5     112x112 additive: a storm attack's 0.8 s tell from the core (white-lime; may be tinted)
  The arena's walls (phase 2; terrain strips the game maps along ArenaWalls' polylines; 128x256, wrapping top to bottom)
  choir-heart-wall-l_0..7, -wall-r_0..7  normal: ribbed tissue, the inner face at canvas x 112 (l) / 15 (r) +- a 6 px
                                  organic wobble; 8 breathing frames (the 0.5 Hz breathe: frame = its phase x 8)
  choir-heart-wall-l-edge_0..7, -r-edge  additive: the lit inner edge (violet-white, the field's shape in the dark)
  choir-heart-wall-l-veins_0..7, -r-veins  additive: the veins' glow (a wall move's 1.0 s tell; the closing's veins)
  choir-heart-pit-l_0..5, -pit-r_0..5  40x48 normal on the inner face: 0 shut, 1-2 the lips swelling, 3-4 parting, 5 open
                                  (the pit brood's 0.8 s tell)
  choir-heart-spit_0..5           40x40 additive: the wall spit's tell at a vein point (lime: 0-3 building, 4-5 the shot)

Review (round 95, ``*-final-r95-a``): design/enemies/bosses/choir-heart/concept/choir-heart (sheet + GIF), over a
the hive-core arena (step ART-H2's tools/art/act7_core.py ``arena_bg``).
Run: nice -n 19 python3 tools/art/choir_heart.py [--review | --check]   (the 15 body renders take ~6 min on 6 workers,
     cached in the temp dir by the model code's hash)
"""
import hashlib
import json
import os
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image

import act7_citadel as cit
import artkit
import belt_units as bu
import jovkit
import l23kit
from artkit import DESIGN, ROOT, TAU
from jovkit import checker_share, rgba, smoothstep, wrap_score
from act7_reef import blob, blur3, chips_frames, grid, label, light_frame, lit_border

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: round 84's stand-in chamber, the darkness look, the Vrell bullets)
import act6_r72 as b6  # noqa: E402  (concept: the key light)
from render import raster, sdf, sprite  # noqa: E402
from render.sdf import Material, mirror_x, rotate_x, rotate_z, sd_capsule, sd_ellipsoid, sd_sphere, smin, union  # noqa: E402

SCRIPT = "choir_heart.py"
BATCH = "M11 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r95"
artkit.REVIEW_ROUND = ROUND
HEART_C = DESIGN / "enemies" / "bosses" / "choir-heart" / "concept"
HELD = ROOT / "build" / "art-held"
W, SCREEN = 480, 540

PPU = 100.0                                   # px per model unit: the Heart drawn 1:1
PIECE = (240, 70)                             # the piece's centre on the screen
BODY = (464, 312)                             # the body's canvas
BC = (232, 176)                               # the piece's centre in the canvas
CENTRE = (0, 80)                              # the Heart's centre = the core (screen (240, 150)), px from the piece
MEMB = [(-150, 80), (-90, 115), (-30, 130), (30, 130), (90, 115), (150, 80)]
NODE_Y = (220, 400)                           # the nodes' screen y on each wall's inner face
FACE = {"l": 112, "r": 15}                    # the wall strips' inner face (canvas x)
WALL = (128, 256)
HALO = (480, 400)
HALO_AT = (0, 20)

RV = np.array(a84.RV, float)                  # the Heart's red-violet pulse
LIME = np.array(a84.LIME, float)
VIOLET = np.array(a84.VIOLET, float)
VIOLET_W = np.array(a84.VIOLET_W, float)
LIME_W = np.array((226, 255, 196.0))
LIME_T = tuple(int(v) for v in a84.LIME)
SONGS = [("herald", "HERALD'S SONG", (190, 130, 255)), ("needler", "NEEDLER THORNS", (214, 228, 242)),
         ("spore", "SPORE DROP", (190, 255, 60)), ("seed", "SEED SCATTER", (0, 232, 170)),
         ("spiral", "CHOIR SPIRAL", (236, 220, 255)), ("motes", "MOTE CLOUD", (255, 110, 190))]

ADDITIVE_SUFFIX = ("-pulse", "-opening-light", "-halo", "-chain", "-centre", "-spark", "-singer", "-death", "-glow",
                   "-flare", "-edge", "-veins", "-spit")


def is_additive(name):
    return any(name.endswith(s) for s in ADDITIVE_SUFFIX)


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def arr(img):
    return np.array(img.convert("RGBA")).astype(np.float64)


def img_of_arr(a):
    return Image.fromarray(np.clip(np.round(a), 0, 255).astype(np.uint8), "RGBA")


def smooth01(x):
    x = np.clip(x, 0, 1)
    return x * x * (3 - 2 * x)


def sd_torus_z(p, c, R, r):
    q = np.hypot(p[:, 0] - c[0], p[:, 1] - c[1]) - R
    return np.hypot(q, p[:, 2] - c[2]) - r


def mx(ox):
    return ox / PPU


def my(oy):
    """A y offset from the piece's centre (px, down) -> model y (the core at 0, up)."""
    return (CENTRE[1] - oy) / PPU


# =========================================================================== the Heart's body (round 84 A.7 b)

DOME_C, DOME_R, DOME_Z = 1.0, (2.2, 1.5, 0.95), -0.25
RING_C = 1.3                                   # the rings' and spokes' centre (screen y 20)
RINGS = (0.40, 0.56, 0.71, 0.84, 0.94)
SWIRL = 0.22                                   # the spokes bend with the radius (no straight spoke)
SPLITS, SPLIT_SWIRL = 8, 0.9                   # the boss's whorl: the opening's eight splits
SACS = [(1.74, 0.92, 0.17), (1.5, 0.52, 0.14), (1.98, 1.3, 0.13)]
H_BODY, H_RIB, H_DARK, H_CORE, H_PALE, H_SAC, H_BOSS = range(7)
CORE_R, CORE_Z = 0.30, 0.14


def dome_z(x, y, zr=DOME_R[2]):
    e2 = (x / DOME_R[0]) ** 2 + ((y - DOME_C) / DOME_R[1]) ** 2
    return DOME_Z + zr * np.sqrt(np.clip(1 - e2, 0, 1))


def crown_coords(x, y):
    rho = np.hypot(x / 2.2, (y - RING_C) / 1.8)
    ang = np.arctan2(x, -(y - RING_C))
    return rho, ang


def crown_pattern(p, n):
    """The rings' and spokes' light (an emission multiplier): fine rings between the ridges, 28 spokes bending."""
    rho, ang = crown_coords(p[:, 0], p[:, 1])
    ring = np.exp(-(np.abs(np.sin(rho * np.pi * 15.0)) / 0.3) ** 2) * 0.55
    spoke = np.exp(-(np.abs(np.sin((ang + SWIRL * rho * 3.0) * 14)) / 0.22) ** 2) * smoothstep(0.22, 0.36, rho)
    return 0.18 + 0.82 * np.maximum(ring, spoke * 0.45)


def crown_groove(p, n):
    rho, ang = crown_coords(p[:, 0], p[:, 1])
    spoke = np.exp(-(np.abs(np.sin((ang + SWIRL * rho * 3.0) * 14)) / 0.22) ** 2) * smoothstep(0.22, 0.36, rho)
    return 1.0 - 0.35 * spoke


def whorl_pattern(p, n):
    """The sealed boss's eight swirling spokes (the opening's splits) and a soft centre."""
    r = np.hypot(p[:, 0], p[:, 1])
    th = np.arctan2(p[:, 0], -p[:, 1])
    s = np.exp(-(np.abs(np.sin((th - SPLIT_SWIRL * r) * SPLITS / 2)) / 0.18) ** 2)
    return 0.25 + 0.75 * s * smoothstep(0.04, 0.12, r)


def vein_pattern(scale, width, seed=0.0):
    def f(p, n):
        v = (np.sin(p[:, 0] * scale * 1.3 + seed) + np.sin(p[:, 1] * scale * 0.9 + 2 * seed)
             + np.sin((p[:, 0] - p[:, 1]) * scale * 0.7 + 1.0))
        return 0.35 + 0.65 * np.exp(-(v / width) ** 2)
    return f


def vein_pattern2(scale, width, seed=0.0):
    """A gentler vein pattern (an albedo multiplier on tissue)."""
    f0 = vein_pattern(scale, width, seed)
    return lambda p, n: 0.8 + 0.2 * f0(p, n)


def core_mats(k=1.0, cracked=False):
    em = np.array((0.78, 1.0, 0.62)) * (0.35 + 0.95 * k)
    if cracked:
        return Material((0.42, 0.55, 0.3), metal=0.0, shininess=50, spec=0.6, emission=tuple(em * 0.8),
                        emission_pattern=vein_pattern(30, 0.16, 1.7))
    return Material((0.5, 0.72, 0.36), metal=0.0, shininess=70, spec=0.9, emission=tuple(em),
                    emission_pattern=vein_pattern(26, 0.28))


def body_mats(pulse, o):
    rv = RV / 255.0
    return [Material((0.2, 0.09, 0.21), metal=0.12, shininess=70, spec=0.75, emission=tuple(rv * (0.26 + 0.62 * pulse))),
            Material((0.14, 0.06, 0.15), metal=0.05, shininess=35, spec=0.35, pattern=crown_groove,
                     emission=tuple(rv * (0.14 + 0.46 * pulse)), emission_pattern=crown_pattern),
            Material((0.04, 0.02, 0.05), metal=0.0, shininess=20, spec=0.2),
            core_mats(min(1.0, o * 1.4)),
            Material((0.42, 0.32, 0.5), metal=0.05, shininess=70, spec=0.6,
                     emission=tuple(np.array((0.3, 0.16, 0.4)) * (0.25 + 0.3 * pulse))),
            Material((0.13, 0.12, 0.08), metal=0.05, shininess=50, spec=0.5,
                     emission=tuple(LIME / 255 * (0.1 + 0.3 * pulse)), emission_pattern=vein_pattern(40, 0.25, 0.6)),
            Material((0.17, 0.07, 0.17), metal=0.15, shininess=80, spec=0.85,
                     emission=tuple(rv * (0.18 + 0.5 * pulse)), emission_pattern=whorl_pattern)]


def hole2d(x, y, o):
    """The opening's 2D shape round the core (model units; < 0 inside): a widening disc and eight splits running out
    along the boss's swirled spokes; the dome's flaps between them peel back."""
    r = np.hypot(x, y)
    th = np.arctan2(x, -y)
    R0 = 0.05 + 0.47 * o
    phi = th - SPLIT_SWIRL * r
    d = np.abs(((phi * SPLITS / TAU) % 1.0) - 0.5) * TAU / SPLITS          # angular distance to the nearest split
    d = np.pi / SPLITS - d
    L = R0 + 0.06 + 0.32 * np.sqrt(o)
    wdt = (0.012 + 0.06 * o) * np.clip(1 - (r - R0) / (L - R0), 0, 1)
    crack = np.maximum(r * d - wdt, r - L)
    disc = r - R0 * (1 + 0.08 * np.sin(th * 8 + 1.1))
    return np.minimum(disc, crack), r


def body_scene(c, o, pulse):
    """The crown: the dome, its ring ridges, the sealed boss over the core, the shoulder sacs and the membranes' collars;
    ``c`` the closing's contraction (0..1), ``o`` the opening (0..1)."""
    zr = DOME_R[2] * (1 - 0.13 * c)
    boss_z = 0.36 - 0.05 * c

    def scene(p):
        x, y = p[:, 0], p[:, 1]
        dome = sd_ellipsoid(p, (0.0, DOME_C, DOME_Z), (DOME_R[0], DOME_R[1], zr))
        boss = sd_ellipsoid(p, (0.0, 0.0, boss_z), (0.5, 0.43, 0.3))
        body = smin(dome, boss, 0.25)
        rho, ang = crown_coords(x, y)
        shell = sd_ellipsoid(p, (0.0, DOME_C, DOME_Z + 0.025 + 0.02 * c), (DOME_R[0], DOME_R[1], zr))
        wob = 0.004 * np.sin(ang * 7 + 1.3) + 0.003 * np.sin(ang * 11 + 0.4)
        rid = np.minimum.reduce([np.abs(rho - f - wob) * 2.0 - (0.02 + 0.008 * c) for f in RINGS])
        ridges = np.maximum(rid, shell)
        sacs = []
        for sx_, sy_, r in SACS:
            for sg in (-1, 1):
                z = float(dome_z(sx_, sy_, zr))
                sacs.append(sd_ellipsoid(p, (sg * sx_, sy_, z), (r * 1.25, r * 0.7, r * 0.5)))
                sacs.append(sd_ellipsoid(p, (sg * (sx_ - 0.13), sy_ - 0.1, z), (r * 0.6, r * 0.5, r * 0.4)))
        sacs = np.minimum.reduce(sacs)
        coll, mouth = [], []
        for ox, oy in MEMB:
            cx, cy = mx(ox), my(oy)
            coll.append(sd_torus_z(p, (cx, cy, 0.07), 0.27, 0.05))
            mouth.append(sd_ellipsoid(p, (cx, cy, 0.04), (0.25, 0.25, 0.05)))
        if o > 0:
            hd, r = hole2d(x, y, o)
            R0 = 0.05 + 0.47 * o
            lift = 0.15 * o * np.exp(-((r - R0) / 0.18) ** 2)
            body = np.maximum(body - lift, -hd)
            ridges = np.maximum(ridges, -hd)
            bowl = sd_ellipsoid(p, (0.0, 0.0, -0.05), (0.56, 0.45, 0.3))
            core = sd_sphere(p, (0.0, 0.0, CORE_Z), CORE_R)
            d, m = union((body, H_RIB), (ridges, H_BODY), (sacs, H_SAC), k=0.03)
            m = np.where((boss < dome) & (m == H_RIB), H_BOSS, m)
            d, m = union((d, m), (np.minimum.reduce(coll), H_PALE), k=0.05)
            d, m = union((d, m), (np.minimum.reduce(mouth), H_DARK), (bowl, H_DARK), (core, H_CORE), k=0.0)
            return d, m
        d, m = union((body, H_RIB), (ridges, H_BODY), (sacs, H_SAC), k=0.03)
        m = np.where((boss < dome + 0.02) & (m == H_RIB), H_BOSS, m)
        d, m = union((d, m), (np.minimum.reduce(coll), H_PALE), k=0.05)
        d, m = union((d, m), (np.minimum.reduce(mouth), H_DARK), k=0.0)
        return d, m
    return scene, body_mats(pulse, o)


FB = 3                                          # the body's render factor
STRIPS = 4
KEY = np.array(b6.KEY)


def _body_strip(c, o, pulse, i):
    scene, mats = body_scene(c, o, pulse)
    w, h = BODY
    E = w / PPU
    C = np.array((0.0, my(BODY[1] / 2 - BC[1])))            # the canvas centre in model units
    L = KEY * (E / 2.3) + np.array((C[0], C[1], 0.0))
    r0, r1 = h * i // STRIPS, h * (i + 1) // STRIPS
    cs = np.array((0.0, C[1] - ((r0 + r1) / 2 - h / 2) / PPU))
    kp = (L - np.array((cs[0], cs[1], 0.0))) / (E / 2.3)
    hi = sdf.render(scene, mats, (w * FB, (r1 - r0) * FB), E, center=tuple(cs), z_top=2.0, steps=120,
                    key_pos=tuple(kp))
    return sprite.downsample(hi, FB).astype(np.float32)


CLOSING = [(k + 1) / 4 for k in range(4)]
OPENING = [(k + 1) / 8 for k in range(8)]


def body_states():
    """(c, o, pulse) of every body render: sealed low/high, the closing, the contracted high, the opening, open high."""
    st = [(0.0, 0.0, 0.0), (0.0, 0.0, 1.0)] + [(c, 0.0, 0.0) for c in CLOSING] + [(1.0, 0.0, 1.0)]
    st += [(1.0 - o if o < 1 else 0.0, o, 0.0) for o in OPENING] + [(0.0, 1.0, 1.0)]
    return st


# =========================================================================== the parts: membranes, nodes, core, pits

def _part_render(scene, mats, size, factor=8, z_top=2.0):
    w, h = size
    hi = sdf.render(scene, mats, (w * factor, h * factor), w / PPU, z_top=z_top, steps=110, key_pos=tuple(KEY))
    return sprite.downsample(hi, factor).astype(np.float32)


IRIS = (0.0, 0.18, 0.42, 0.66, 0.86, 1.0)


def singer_parts(k, p, s):
    """The singer organ of membrane k (its unit's echo), at scale s."""
    kind = SONGS[k][0]
    if kind == "herald":
        orbs = [sd_sphere(p, (0.115 * s * np.cos(a), 0.115 * s * np.sin(a), 0.035), 0.024 * s)
                for a in np.linspace(0, TAU, 6, endpoint=False) + 0.3]
        return np.minimum.reduce([sd_sphere(p, (0, 0, 0.02), 0.075 * s), sd_torus_z(p, (0, 0, 0.03), 0.115 * s, 0.012 * s)]
                                 + orbs)
    if kind == "needler":
        sp = [sd_capsule(p, (0, 0, 0.02), (0.13 * s * np.cos(a), 0.13 * s * np.sin(a), 0.07), 0.026 * s, 0.006)
              for a in np.linspace(0, TAU, 5, endpoint=False) + 0.5]
        return np.minimum.reduce(sp + [sd_sphere(p, (0, 0, 0.03), 0.05 * s)])
    if kind == "spore":
        nod = [sd_sphere(p, (0.1 * s * np.cos(a), 0.09 * s * np.sin(a), 0.03), 0.03 * s)
               for a in np.linspace(0, TAU, 4, endpoint=False) + 0.8]
        return np.minimum.reduce([sd_ellipsoid(p, (0, 0, 0.02), (0.11 * s, 0.095 * s, 0.075 * s))] + nod)
    if kind == "seed":
        bl = []
        for a in np.linspace(0, TAU, 5, endpoint=False):
            q = rotate_z(p, -a)
            bl.append(sd_ellipsoid(q, (0.03 * s, 0.07 * s, 0.02), (0.035 * s, 0.075 * s, 0.018)))
        return np.minimum.reduce(bl + [sd_sphere(p, (0, 0, 0.035), 0.042 * s)])
    if kind == "spiral":
        pts = []
        for arm in range(3):
            for j in range(5):
                a = arm * TAU / 3 + j * 0.55
                rr = (0.03 + 0.024 * j) * s
                pts.append(sd_sphere(p, (rr * np.cos(a), rr * np.sin(a), 0.035 - 0.004 * j), (0.03 - 0.004 * j) * s))
        return np.minimum.reduce(pts + [sd_sphere(p, (0, 0, 0.04), 0.04 * s)])
    rng = np.random.default_rng(9500 + k)
    pts = [sd_sphere(p, (rng.uniform(-0.09, 0.09) * s, rng.uniform(-0.09, 0.09) * s, 0.03), 0.032 * s) for _ in range(7)]
    return np.minimum.reduce(pts)


M_BASE, M_PETAL, M_ORGAN, M_LIP = range(4)


def membrane_scene(k, open_, dead=False):
    """A membrane: a pale violet collar mouth whose six petals iris open over its singer organ."""
    nseed = 9510 + k

    def scene(p):
        petals = []
        for j in range(6):
            a = j * TAU / 6 + 0.2 + 0.05 * k
            q = rotate_z(p, -a)
            if dead:
                cy_, tilt, rad = 0.075, 0.75, (0.075, 0.12, 0.028)
            else:
                cy_ = 0.1 + 0.08 * open_
                tilt = 0.55 * (1 - open_) - 0.95 * open_
                rad = (0.1 - 0.02 * open_, 0.155 - 0.045 * open_, 0.033)
            q = rotate_x(q - np.array((0.0, cy_, 0.07 - 0.03 * open_)), tilt)
            petals.append(sd_ellipsoid(q, (0.0, 0.0, 0.0), rad))
        pet = np.minimum.reduce(petals)
        if dead:
            pet = pet + 0.006 * np.sin(p[:, 0] * 90 + nseed) * np.sin(p[:, 1] * 80)
        base = sd_ellipsoid(p, (0, 0, -0.02), (0.25, 0.25, 0.05))
        lip = sd_torus_z(p, (0, 0, 0.0), 0.225, 0.04)
        organ = singer_parts(k, p, 0.7 if dead else 0.85 + 0.15 * open_)
        return union((base, M_BASE), (lip, M_LIP), (organ, M_ORGAN), (pet, M_PETAL), k=0.008)
    col = np.array(SONGS[k][2]) / 255.0
    if dead:
        mats = [Material((0.1, 0.09, 0.11), shininess=20, spec=0.2),
                Material((0.38, 0.35, 0.4), shininess=25, spec=0.25, pattern=vein_pattern2(60, 0.5)),
                Material((0.16, 0.15, 0.16), shininess=20, spec=0.2),
                Material((0.3, 0.28, 0.32), shininess=25, spec=0.3)]
    else:
        mats = [Material((0.05, 0.02, 0.06), shininess=30, spec=0.3),
                Material((0.62, 0.5, 0.74), metal=0.05, shininess=80, spec=0.6, pattern=vein_pattern2(55, 0.6, 0.3),
                         emission=(0.13, 0.07, 0.18)),
                Material(tuple(col * 0.4), metal=0.1, shininess=80, spec=0.7,
                         emission=tuple(col * (0.1 + 0.7 * open_))),
                Material((0.5, 0.38, 0.6), metal=0.05, shininess=80, spec=0.7, emission=(0.08, 0.04, 0.1))]
    return scene, mats


def _membrane(k, open_, dead):
    scene, mats = membrane_scene(k, open_, dead)
    return _part_render(scene, mats, (56, 56))


N_STRAND, N_BULB = range(2)


def node_scene(side, dead=False):
    """A wall node: a lime knot of nerve on the wall's inner face, its strands curving back into its wall."""
    sg = -1.0 if side == "l" else 1.0
    rng = np.random.default_rng(9530)
    strands = [(a, rng.uniform(0.2, 0.25), rng.uniform(-0.5, 0.5)) for a in np.linspace(-1.0, 1.0, 5)]

    def scene(p):
        st = []
        for j, (a, L, bend) in enumerate(strands):
            ang = np.pi / 2 * (1 - sg) + a                   # pointing into the wall (l: -x)
            pts = []
            for t in (0.0, 0.35, 0.7, 1.0):
                aa = ang + bend * np.sin(t * np.pi)
                pts.append((np.cos(aa) * L * t, np.sin(aa) * L * t, 0.07 - 0.09 * t))
            for q0, q1, k in zip(pts[:-1], pts[1:], range(3)):
                if dead and j % 2 == 0 and k == 0:
                    continue
                st.append(sd_capsule(p, q0, q1, 0.032 - 0.006 * k, 0.026 - 0.006 * k))
        strands_d = np.minimum.reduce(st)
        bulb = sd_sphere(p, (0, 0, 0.08), 0.11)
        bulb = smin(bulb, sd_sphere(p, (0.04 * sg, 0.05, 0.09), 0.06), 0.03)
        bulb = smin(bulb, sd_sphere(p, (-0.03 * sg, -0.06, 0.08), 0.05), 0.03)
        if dead:
            bulb = np.maximum(bulb, -sd_sphere(p, (0.0, 0.0, 0.2), 0.11))
        return union((strands_d, N_STRAND), (bulb, N_BULB), k=0.03)
    if dead:
        mats = [Material((0.2, 0.22, 0.14), shininess=25, spec=0.3), Material((0.16, 0.17, 0.11), shininess=20, spec=0.2)]
    else:
        mats = [Material((0.24, 0.42, 0.1), metal=0.05, shininess=60, spec=0.7, emission=(0.1, 0.22, 0.03),
                         emission_pattern=vein_pattern(70, 0.4)),
                Material((0.55, 0.78, 0.28), metal=0.0, shininess=70, spec=0.8, emission=(0.5, 0.72, 0.18),
                         emission_pattern=vein_pattern(60, 0.5, 2.0))]
    return scene, mats


def _node(side, dead):
    scene, mats = node_scene(side, dead)
    return _part_render(scene, mats, (48, 48))


def core_scene(cracked=False):
    """The core alone (the body's model at o = 1: the same sphere and material) over a dark socket ring."""
    def scene(p):
        core = sd_sphere(p, (0.0, 0.0, CORE_Z), CORE_R)
        sock = sd_torus_z(p, (0, 0, 0.02), 0.32, 0.035)
        return union((sock, 0), (core, 1), k=0.02)
    return scene, [Material((0.09, 0.04, 0.1), shininess=40, spec=0.5), core_mats(1.0, cracked)]


def _core(cracked):
    scene, mats = core_scene(cracked)
    return _part_render(scene, mats, (72, 72), factor=4)


PIT = (40, 48)
PIT_STATES = [(0.0, 0.0), (0.45, 0.0), (0.9, 0.0), (1.0, 0.45), (1.0, 0.8), (1.0, 1.0)]   # (swell, part)


def pit_scene(side, swell, part):
    """A wall pit: a fleshy pod grown out of the wall's face, its lip ring swelling and parting on a dark brood hole."""
    sg = -1.0 if side == "l" else 1.0
    s = 1.0 + 0.12 * swell
    k = np.array((1.0, 0.5, 1.0))

    def scene(p):
        root = sd_ellipsoid(p, (0.1 * sg, 0.0, 0.0), (0.12, 0.2 * s, 0.07))
        pod = sd_ellipsoid(p, (0.0, 0.0, 0.03), (0.14 * s, 0.2 * s, 0.09 * s))
        body = smin(root, pod, 0.05)
        R = 0.03 + 0.055 * part
        lip = sd_torus_z(p * k, (0.0, 0.0, 0.1 * s), R, 0.022 + 0.006 * swell) * 0.9
        hole = sd_ellipsoid(p, (0.0, 0.0, 0.11 * s), (0.012 + R, 2 * (0.012 + R), 0.07))
        body = np.maximum(body, -hole)
        dark = sd_ellipsoid(p, (0.0, 0.0, 0.05), (0.01 + 0.9 * R, 2 * (0.01 + 0.9 * R), 0.02))
        items = [(body, 0), (lip, 1), (dark, 3)]
        if part > 0.3:
            items.append((np.minimum.reduce([sd_sphere(p, (0.02 * j, 0.05 * j, 0.04), 0.014) for j in (-1, 0, 1)]), 2))
        return union(*items, k=0.012)
    mats = [Material((0.16, 0.07, 0.18), metal=0.1, shininess=50, spec=0.5, emission=tuple(RV / 255 * 0.12),
                     emission_pattern=vein_pattern(60, 0.35)),
            Material((0.4, 0.28, 0.48), metal=0.05, shininess=60, spec=0.5, emission=(0.1, 0.04, 0.1)),
            Material((0.2, 0.3, 0.1), emission=tuple(LIME / 255 * 1.2)),
            Material((0.02, 0.01, 0.03), spec=0.1)]
    return scene, mats


def _pit(side, i):
    scene, mats = pit_scene(side, *PIT_STATES[i])
    return _part_render(scene, mats, PIT)


# =========================================================================== render jobs (cached, <= 6 workers)

def jobs():
    js = [("body", c, o, pl, i) for (c, o, pl) in body_states() for i in range(STRIPS)]
    js += [("memb", k, op, False) for k in range(6) for op in IRIS] + [("memb", k, 0.0, True) for k in range(6)]
    js += [("node", s, d) for s in "lr" for d in (False, True)]
    js += [("core", cr) for cr in (False, True)]
    js += [("pit", s, i) for s in "lr" for i in range(len(PIT_STATES))]
    return js


FUNCS = {"body": _body_strip, "memb": _membrane, "node": _node, "core": _core, "pit": _pit}


def _job(job):
    return FUNCS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "choir-heart"


def _model_hash(kind):
    """The body's renders hang on the body's model code; the parts' on both sections (they share its materials)."""
    text = Path(__file__).read_text(encoding="utf-8")
    a = text.index("# =========================================================================== the Heart's body")
    b = text.index("# =========================================================================== the parts")
    c = text.index("# =========================================================================== render jobs")
    return hashlib.sha1(text[a:b if kind == "body" else c].encode()).hexdigest()[:12]


def run():
    js = jobs()
    CACHE.mkdir(parents=True, exist_ok=True)
    paths = [CACHE / f"{hashlib.sha1((_model_hash(j[0]) + repr(j)).encode()).hexdigest()[:20]}.npy" for j in js]
    todo = [i for i, p in enumerate(paths) if not p.exists()]
    if todo:
        print(f"rendering {len(todo)} of {len(js)} jobs on {min(6, os.cpu_count() or 1)} workers")
        with ProcessPoolExecutor(max_workers=min(6, os.cpu_count() or 1)) as pool:
            for i, res in zip(todo, pool.map(_job, [js[i] for i in todo], chunksize=1)):
                np.save(paths[i], res)
    return {j: np.load(p) for j, p in zip(js, paths)}


def to_sprite(fl, sharp=70):
    return sprite.sharpen(sprite.to_image(np.asarray(fl, np.float64), 0.5), sharp)


# =========================================================================== image-space work

def bodies(res):
    """{state: float RGBA} of the assembled body renders."""
    out = {}
    for st in body_states():
        out[st] = np.concatenate([res[("body", *st, i)] for i in range(STRIPS)], axis=0).astype(np.float64)
    return out


def pulse_layer(lo, hi):
    """The pulse's light at its peak: the high render minus the low (emission only, unquantised), on the body."""
    d = np.clip(hi[..., :3] - lo[..., :3], 0, None) * 255.0 * (lo[..., 3:4] >= 0.5)
    return light_frame(d, 4)


def canvas_model():
    x, y = grid(*BODY)
    return (x - BC[0]) / PPU, my(y - BC[1])


def opening_light(o):
    """The splits' light (lime-white deep in them, violet-white at the flaps' lips) as the dome opens."""
    X, Y = canvas_model()
    hd, r = hole2d(X, Y, o)
    inside = smooth01(-hd / 0.012)
    rim = np.exp(-(np.abs(hd) / 0.028) ** 2)
    keep = smooth01((r - CORE_R - 0.02) / 0.05)                   # never over the core itself
    k = np.sin(np.pi * min(1.0, 0.25 + o * 0.9)) ** 0.6
    light = (inside * keep * 0.8 * k)[..., None] * LIME_W + (rim * keep * 0.45 * k)[..., None] * VIOLET_W
    light += blur3(light, 3.0) * 0.7
    return light_frame(light, 6)


def halo():
    w, h = HALO
    x, y = grid(w, h)
    sx_, sy_ = x - w / 2 + PIECE[0] + HALO_AT[0], y - h / 2 + PIECE[1] + HALO_AT[1]
    e = np.hypot((sx_ - 240) / 220.0, (sy_ - 50) / 150.0)
    out = np.clip(e - 1, 0, None) * 170
    light = np.exp(-(out / 34.0) ** 2) * 0.3 + np.exp(-(out / 90.0) ** 2) * 0.08
    light = light[..., None] * RV
    return light_frame(light, 28)


def sample(a, sx_, sy_):
    """Bilinear sample of a float image at fractional coordinates (outside: transparent)."""
    h, w = a.shape[:2]
    x0, y0 = np.floor(sx_).astype(int), np.floor(sy_).astype(int)
    fx, fy = sx_ - x0, sy_ - y0
    out = np.zeros(sx_.shape + (a.shape[2],))
    for dx, dy, wgt in ((0, 0, (1 - fx) * (1 - fy)), (1, 0, fx * (1 - fy)), (0, 1, (1 - fx) * fy), (1, 1, fx * fy)):
        xi, yi = x0 + dx, y0 + dy
        ok = (xi >= 0) & (xi < w) & (yi >= 0) & (yi < h)
        v = np.zeros(out.shape)
        v[ok] = a[yi[ok], xi[ok]]
        out += v * wgt[..., None]
    return out


def collapse_frames(open_img, n=12):
    """K: the open body greying and collapsing inward on the core, its rim breaking into chunks (1-bit)."""
    a = arr(open_img)
    w, h = BODY
    x, y = grid(w, h)
    cx, cy = BC[0] + CENTRE[0], BC[1] + CENTRE[1]
    nz = raster.fbm(w, h, 24, 9540, octaves=4, period=False)[:h, :w]
    n2 = raster.fbm(w, h, 12, 9541, octaves=3, period=False)[:h, :w]
    ex = np.hypot((x - BC[0]) / 220.0, (y - (BC[1] - 20)) / 150.0)
    out = []
    for f in range(n):
        u = (f + 1) / n
        k = 1 + 0.42 * u ** 1.3 * (0.6 + 0.8 * nz)
        s = sample(a, cx + (x - cx) * k, cy + (y - cy) * k)
        rgb, al = s[..., :3], s[..., 3] / 255.0
        lum = rgb @ np.array((0.3, 0.59, 0.11))
        grey = lum[..., None] * np.array((0.62, 0.58, 0.64))
        rgb = rgb * (1 - 0.8 * u) + (grey + 14) * 0.8 * u
        rgb *= 1 - 0.22 * u * (0.7 + 0.6 * n2[..., None])
        rgb = np.minimum(rgb, 220 - 120 * u)
        keep = (ex * k) < (1.2 - 0.38 * u ** 1.2 + 0.22 * (n2 - 0.5))
        out.append(rgba(rgb, ((al >= 0.5) & keep).astype(float)))
    return out


CHAIN = []                                        # (x, y, t): px from the piece's centre, s from K


def chain_points():
    rng = np.random.default_rng(9545)
    pts = [(CENTRE[0], CENTRE[1], 0.0)]
    for j in range(13):
        t = 0.3 * (j + 1)
        a = rng.uniform(0, TAU)
        r = 30 + 150 * (t / 4.0) ** 0.8
        x = CENTRE[0] + np.sin(a) * r * 1.3
        yy = CENTRE[1] - np.cos(a) * r * 0.75
        yy = min(yy, 125.0)
        pts.append((int(round(x)), int(round(yy)), round(t, 2)))
    return pts


def chain_frames(n=10, w=128):
    x, y = grid(w, w)
    c = w / 2
    rr = np.hypot(x - c, y - c)
    out = []
    for f in range(n):
        u = (f + 0.5) / n
        k = (1 - u) ** 1.3
        light = blob(x, y, c, c, 6 + 22 * u)[..., None] * LIME_W * 0.95 * k
        light += blob(x, y, c, c, 14 + 34 * u)[..., None] * LIME * 0.35 * k
        ring = np.exp(-((rr - (10 + 44 * u)) / (3 + 4 * u)) ** 2) * (1 - u) ** 1.6
        light += ring[..., None] * VIOLET_W * 0.5
        out.append(light_frame(light * 1.3, 8))
    return out


def debris_frames(n=8, w=16):
    x, y = grid(w, w)
    rng = np.random.default_rng(9547)
    lobes = rng.uniform(0.75, 1.0, 5)
    out = []
    for f in range(n):
        a0 = f / n * TAU
        th = np.arctan2(y - w / 2, x - w / 2)
        rad = 5.2 * (1 + 0.18 * np.sin(th * 3 + a0) + 0.1 * np.cos(th * 5 - a0 * 2)) * lobes[f % 5]
        rr = np.hypot(x - w / 2, (y - w / 2) * (1.0 + 0.35 * np.cos(a0) ** 2))
        on = rr < rad
        sh = np.clip(0.55 + 0.6 * (-(x - w / 2) - (y - w / 2)) / w, 0.25, 1.2)
        rgb = np.array((52, 26, 60.0)) * sh[..., None]
        rgb += np.exp(-((rr - rad + 1.2) / 0.9) ** 2)[..., None] * np.array((60, 30, 50.0)) * sh[..., None]
        out.append(rgba(rgb, on.astype(float)))
    return out


def centre_frames(n=6, w=96):
    x, y = grid(w, w)
    out = []
    for f in range(n):
        u = (f + 1) / n
        light = blob(x, y, w / 2, w / 2, 8 + 14 * u)[..., None] * VIOLET_W * 0.55 * u
        light += blob(x, y, w / 2, w / 2, 3 + 5 * u)[..., None] * np.array((255, 252, 255.0)) * 0.85 * u
        out.append(light_frame(light, 8))
    return out


def spark_frames(n=4, w=24):
    x, y = grid(w, w)
    out = []
    for f in range(n):
        k = (1.0, 0.75, 0.45, 0.2)[f]
        r = 2.0 + 1.6 * f
        light = blob(x, y, w / 2, w / 2, r)[..., None] * VIOLET_W * 0.9 * k
        th = np.arctan2(y - w / 2, x - w / 2)
        star = np.exp(-(np.abs(np.sin(th * 2 + 0.4)) / 0.16) ** 2) * blob(x, y, w / 2, w / 2, r * 2.2)
        light += star[..., None] * VIOLET * 0.7 * k
        out.append(light_frame(light, 3))
    return out


def singer_frames(k, n=8, w=80):
    """The singer's style while it sings (8 frames, 10 fps loop): fading to 0 inside its canvas."""
    kind, _, col = SONGS[k]
    col = np.asarray(col, float)
    x, y = grid(w, w)
    c = w / 2
    rr, th = np.hypot(x - c, y - c), np.arctan2(y - c, x - c)
    out = []
    for f in range(n):
        ph = f / n * TAU
        light = blob(x, y, c, c, 10)[..., None] * col * 0.45
        if kind == "herald":
            ring = np.exp(-((rr - 12.5) / 2.2) ** 2) * (0.6 + 0.4 * np.cos(th * 6 - ph))
            light += ring[..., None] * col * 0.75 + blur3(ring[..., None] * col, 3) * 0.6
        elif kind == "needler":
            for j in range(5):
                a = j * TAU / 5 + 0.5
                gx, gy = c + np.cos(a) * 12, c + np.sin(a) * 12
                tw = max(0.0, np.sin(ph + j * 1.3)) ** 2
                g = blob(x, y, gx, gy, 2.2) + (np.exp(-((x - gx) / 7) ** 2 - ((y - gy) / 0.9) ** 2)
                                              + np.exp(-((x - gx) / 0.9) ** 2 - ((y - gy) / 7) ** 2)) * 0.5
                light += (g * tw)[..., None] * np.array((235, 248, 255.0)) * 0.9
        elif kind == "spore":
            sw = 0.5 - 0.5 * np.cos(ph)
            light += blob(x, y, c, c, 9 + 6 * sw)[..., None] * col * (0.35 + 0.45 * sw)
        elif kind == "seed":
            jx, jy = np.cos(ph * 3) * 1.2, np.sin(ph * 2) * 1.2
            arm = np.exp(-(np.abs(np.sin((th - ph * 0.5) * 2.5 - rr * 0.18)) / 0.3) ** 2) * blob(x, y, c + jx, c + jy, 16)
            light += arm[..., None] * np.array((235, 80, 200.0)) * 0.55 + blob(x, y, c + jx, c + jy, 5)[..., None] * col * 0.7
        elif kind == "spiral":
            pr = 6 + 16 * (f / n)
            ring = np.exp(-((rr - pr) / 2.5) ** 2) * (1 - f / n)
            light += ring[..., None] * LIME * 0.7 + blob(x, y, c, c, 6)[..., None] * col * 0.5
        else:
            rng = np.random.default_rng(9560)
            for j in range(9):
                a, r0, sp = rng.uniform(0, TAU), rng.uniform(5, 15), rng.uniform(0.6, 1.4)
                gx, gy = c + np.cos(a + ph * 0.3 * sp) * r0, c + np.sin(a + ph * 0.3 * sp) * r0
                tw = 0.35 + 0.65 * max(0.0, np.sin(ph * sp + j)) ** 2
                light += blob(x, y, gx, gy, 2.0)[..., None] * col * 0.8 * tw
        out.append(light_frame(light * 1.7, 10))
    return out


def burst_frames(n, w, col_in, col_out, seed):
    x, y = grid(w, w)
    c = w / 2
    rr = np.hypot(x - c, y - c)
    out = []
    for f in range(n):
        u = (f + 0.5) / n
        k = (1 - u) ** 1.4
        light = blob(x, y, c, c, 5 + 16 * u)[..., None] * col_in * 0.9 * k
        light += blob(x, y, c, c, 12 + 22 * u)[..., None] * col_out * 0.4 * k
        light += np.exp(-((rr - (8 + 30 * u)) / 3.0) ** 2)[..., None] * col_out * 0.45 * (1 - u) ** 1.8
        out.append(light_frame(light, 8))
    return out


def glow_frames(n, w, col, r0, r1, k0=0.35, k1=0.75, core_col=None):
    x, y = grid(w, w)
    out = []
    for f in range(n):
        s = 0.5 - 0.5 * np.cos(f / n * TAU)
        light = blob(x, y, w / 2, w / 2, r0 + (r1 - r0) * s)[..., None] * np.asarray(col, float) * (k0 + (k1 - k0) * s)
        if core_col is not None:
            light += blob(x, y, w / 2, w / 2, r0 * 0.45)[..., None] * np.asarray(core_col, float) * (0.3 + 0.3 * s)
        out.append(light_frame(light, 8))
    return out


def flare_frames(n=6, w=112):
    x, y = grid(w, w)
    out = []
    for f in range(n):
        u = (f + 1) / n
        light = blob(x, y, w / 2, w / 2, 20 + 16 * u)[..., None] * LIME * 0.45 * u
        light += blob(x, y, w / 2, w / 2, 12 + 10 * u)[..., None] * LIME_W * 0.7 * u
        out.append(light_frame(light, 10))
    return out


def spit_frames(n=6, w=40):
    x, y = grid(w, w)
    out = []
    for f in range(n):
        k = (0.2, 0.4, 0.65, 0.9, 1.0, 0.5)[f]
        r = (3, 4, 5, 6, 8, 6)[f]
        light = blob(x, y, w / 2, w / 2, r)[..., None] * LIME * 0.7 * k
        light += blob(x, y, w / 2, w / 2, r * 0.45)[..., None] * LIME_W * 0.7 * k
        out.append(light_frame(light, 5))
    return out


def bruise(img, seed, k=1.0):
    """A part below half its HP: dark bruising, a few torn notches out of its edge, red-violet in the tears."""
    a = arr(img)
    h, w = a.shape[:2]
    n = raster.fbm(max(w, 8), max(h, 8), 8, seed, octaves=3, period=False)[:h, :w]
    on = a[..., 3] > 0
    dark = np.clip((n - 0.4) * 2.2, 0, 1) * k
    a[..., :3] *= (1 - 0.55 * dark)[..., None]
    tear = np.exp(-(np.abs(n - 0.62) / 0.02) ** 2) * k
    a[..., :3] = a[..., :3] * (1 - tear[..., None]) + tear[..., None] * np.array((120, 30, 70.0))
    x, y = grid(w, h)
    rr = np.hypot(x - w / 2, y - h / 2) / (min(w, h) / 2)
    notch = (rr > 0.72) & (n > 0.66)
    a[..., 3] = np.where(notch, 0, a[..., 3])
    a[..., :3] *= (a[..., 3:4] > 0)
    return img_of_arr(a)


# =========================================================================== the walls

def _wall_fields(side, f):
    """Height, depth and vein fields of a wall strip (left geometry; the right one mirrored before shading)."""
    w, h = WALL
    x, y = grid(w, h)
    ph = f / 8 * TAU
    n1 = raster.fbm(w, h, 32, 9570, octaves=4)
    n2 = raster.fbm(w, h, 16, 9571, octaves=3)
    n3 = raster.fbm(w, h, 64, 9572, octaves=3)
    yy = y * TAU / h
    wob = 3.0 * np.sin(yy * 2 + 0.7) + 2.0 * np.sin(yy * 5 + 2.0) + 5.0 * (n1 - 0.5)
    wob += 1.6 * np.sin(ph) * (0.6 + 0.4 * np.sin(yy * 3 + 1.0))          # the lip's breath
    d = FACE["l"] + wob - x                                                # depth into the tissue (> 0 inside)
    q = yy * 9 + 1.6 * np.sin(x / 31.0 + 0.8 + 2.0 * n1) + 3.2 * (n2 - 0.5) + 0.25 * np.sin(ph)
    rib = 0.5 + 0.5 * np.cos(q)
    hgt = 12 * smooth01(d / 18) + 9.0 * rib * (1 + 0.18 * np.sin(ph)) * smoothstep(3, 22, d) + 2.5 * (n2 - 0.5)
    vein = np.exp(-(np.abs(n3 - 0.5) / 0.03) ** 2) * smoothstep(5, 14, d) * smoothstep(0, 18, x)
    if side == "r":
        d, hgt, vein, rib = (np.ascontiguousarray(a[:, ::-1]) for a in (d, hgt, vein, rib))
    return d, hgt, vein, rib


def wall_frames(side):
    """The ribbed tissue strip (normal), its lit inner edge and its veins' glow (additive), 8 breathing frames."""
    w, h = WALL
    tis, edge, veins = [], [], []
    L = np.array((-0.55, -0.6, 0.75))
    L /= np.linalg.norm(L)
    for f in range(8):
        d, hgt, vein, rib = _wall_fields(side, f)
        gy = (np.roll(hgt, -1, 0) - np.roll(hgt, 1, 0)) / 2
        gx = np.gradient(hgt, axis=1)
        nrm = np.dstack([-gx, -gy, np.ones_like(hgt) * 1.1])
        nrm /= np.linalg.norm(nrm, axis=-1, keepdims=True)
        lam = np.clip(nrm @ L, 0, 1)
        v = np.clip(0.12 + 0.75 * lam * (0.55 + 0.45 * rib) + 0.04 * hgt / 12, 0, 1.2)
        rgb = np.interp(v, (0, 0.35, 0.7, 1.2), (0, 1, 2, 3))
        ramp = np.array(((5, 3, 8), (22, 11, 28), (46, 26, 58), (88, 58, 104.0)))
        i0 = np.clip(np.floor(rgb).astype(int), 0, 2)
        t = (rgb - i0)[..., None]
        col = ramp[i0] * (1 - t) + ramp[i0 + 1] * t
        col += vein[..., None] * np.array((48, 14, 34.0))
        tis.append(rgba(col, (d > 0).astype(float)))
        e = np.exp(-((d - 1.5) / 2.6) ** 2) * 0.5 + np.exp(-((d - 4) / 8.0) ** 2) * 0.15 * (d > 0)
        e *= smoothstep(-8, 0, d)
        el = e[..., None] * (VIOLET_W * 0.65 + VIOLET * 0.35)
        edge.append(light_frame_sides(el, 6))
        vl = vein[..., None] * np.array((235, 90, 175.0)) * 0.5
        vl += blur3(vl, 3.0) * 0.9
        veins.append(light_frame_sides(vl * (d > -1)[..., None], 6))
    return tis, edge, veins


def light_frame_sides(light, margin):
    """An additive tile strip: faded to 0 over its left and right margins only (it wraps top to bottom)."""
    h, w = light.shape[:2]
    x, _ = grid(w, h)
    dd = np.minimum(x, w - x)
    return jovkit.additive_rgb(light * smoothstep(0.5, margin + 0.5, dd)[..., None])


# =========================================================================== build

def build(res):
    bd = bodies(res)
    sealed_lo, sealed_hi = bd[(0.0, 0.0, 0.0)], bd[(0.0, 0.0, 1.0)]
    clos = [bd[(c, 0.0, 0.0)] for c in CLOSING]
    con_hi = bd[(1.0, 0.0, 1.0)]
    opn = [bd[(1.0 - o if o < 1 else 0.0, o, 0.0)] for o in OPENING]
    open_hi = bd[(0.0, 1.0, 1.0)]
    allb = artkit.quantize_set([to_sprite(b) for b in [sealed_lo] + clos + opn], 192)
    save_set("choir-heart", [allb[0]], single=True)
    save_set("choir-heart-closing", allb[1:5])
    save_set("choir-heart-opening", allb[5:13])
    save_set("choir-heart-pulse", [pulse_layer(sealed_lo, sealed_hi), pulse_layer(clos[-1], con_hi),
                                   pulse_layer(opn[-1], open_hi)])
    save_set("choir-heart-opening-light", [opening_light(o) for o in OPENING])
    save_set("choir-heart-halo", [halo()], single=True)
    save_set("choir-heart-collapse", collapse_frames(allb[12]))
    save_set("choir-heart-chain", chain_frames())
    save_set("choir-heart-debris", debris_frames())
    save_set("choir-heart-centre", centre_frames())
    save_set("choir-heart-spark", spark_frames())
    for k in range(6):
        whole = [to_sprite(res[("memb", k, op, False)], 60) for op in IRIS]
        dmg = [bruise(im, 9580 + k * 7 + j) for j, im in enumerate(whole)]
        dead = to_sprite(res[("memb", k, 0.0, True)], 50)
        save_set(f"choir-heart-membrane-{k + 1}", artkit.quantize_set(whole + dmg + [dead], 96))
        save_set(f"choir-heart-membrane-{k + 1}-singer", singer_frames(k))
    save_set("choir-heart-membrane-death", burst_frames(8, 96, VIOLET_W, VIOLET, 9590))
    save_set("choir-heart-membrane-tatters", chips_frames(8, 64, [(150, 120, 180), (110, 80, 140), (70, 40, 90)], 9591,
                                                          count=14, size=(2.4, 4.0)))
    for s in "lr":
        whole = to_sprite(res[("node", s, False)], 60)
        save_set(f"choir-heart-node-{s}", [whole, bruise(whole, 9600 if s == "l" else 9601, 0.8),
                                           to_sprite(res[("node", s, True)], 50)])
        save_set(f"choir-heart-pit-{s}", [to_sprite(res[("pit", s, i)], 60) for i in range(len(PIT_STATES))])
    save_set("choir-heart-node-glow", glow_frames(8, 80, LIME, 13, 20, 0.4, 0.8, core_col=LIME_W))
    save_set("choir-heart-node-death", burst_frames(8, 96, LIME_W, LIME, 9602))
    save_set("choir-heart-node-tatters", chips_frames(8, 64, [(120, 170, 50), (70, 110, 30), (40, 60, 24)], 9603,
                                                      count=14, size=(2.4, 4.0)))
    core = to_sprite(res[("core", False)], 60)
    save_set("choir-heart-core", [core, to_sprite(res[("core", True)], 60)])
    save_set("choir-heart-core-glow", glow_frames(8, 112, LIME_W, 18, 26, 0.25, 0.6, core_col=(255, 255, 240)))
    save_set("choir-heart-core-flare", flare_frames())
    save_set("choir-heart-spit", spit_frames())
    for s in "lr":
        tis, edge, veins = wall_frames(s)
        save_set(f"choir-heart-wall-{s}", tis)
        save_set(f"choir-heart-wall-{s}-edge", edge)
        save_set(f"choir-heart-wall-{s}-veins", veins)
    write_pivots()


def write_pivots():
    data = {
        "piece": list(PIECE), "note": "offsets in px from the piece's centre (screen (240, 70)), y down; additive sets "
                                      "are premultiplied on black (GL_SRC_ALPHA, GL_ONE)",
        "body_canvas": list(BODY), "body_centre": list(BC),
        "bodies": {"phase-1": "choir-heart", "closing": "choir-heart-closing_0..3 (the closing's 4 s; 3 = phase 2)",
                   "phase-2": "choir-heart-closing_3", "opening": "choir-heart-opening_0..7 (the opening's 4 s)",
                   "phase-3": "choir-heart-opening_7", "death": "choir-heart-collapse_0..11 (K .. K + 4 s, 3 fps)"},
        "pulse": {"set": "choir-heart-pulse", "frames": {"sealed": 0, "contracted": 1, "open": 2},
                  "alpha": "the pulse 0..1 (smooth, <= 1 Hz, its peak on HEARTBEAT); 0 from K"},
        "halo": {"set": "choir-heart-halo", "at": list(HALO_AT), "under_body": True, "alpha": "0.4 + 0.6 x the pulse"},
        "centre": list(CENTRE),
        "centre_note": "the Heart's centre = the core's place (the README's core row, screen (240, 150)): the heartbeat "
                       "ring, the line and the spiral start here; the README's Movement row gives the piece's centre "
                       "(240, 70)",
        "membranes": {f"m{k + 1}": {"at": list(MEMB[k]), "song": SONGS[k][1].lower(),
                                    "set": f"choir-heart-membrane-{k + 1}",
                                    "singer": f"choir-heart-membrane-{k + 1}-singer"} for k in range(6)},
        "membrane_frames": {"closed": 0, "iris": [1, 2, 3, 4], "open": 5, "damaged_offset": 6, "dead": 12,
                            "iris_note": "the 0.8 s tell: frames 1-4 over its first 0.5 s, 5 held while it sings, "
                                         "the iris back 4..0 over 0.3 s after its last bullet; below half HP add 6"},
        "membrane_size": [56, 56], "node_size": [48, 48], "core_size": [72, 72],
        "nodes": {"y": list(NODE_Y), "left": "choir-heart-node-l", "right": "choir-heart-node-r",
                  "frames": {"whole": 0, "damaged": 1, "dead": 2},
                  "note": "the node's centre on its half-wall's inner face (ArenaWalls), its strands into the wall"},
        "core": {"at": list(CENTRE), "frames": {"whole": 0, "damaged": 1},
                 "glow_note": "choir-heart-core-glow frame = the 2.0 s heartbeat's phase x 8 (peak at 4)"},
        "walls": {"tile": list(WALL), "face_x": FACE, "frames": 8, "fps_note": "frame = the 0.5 Hz breathe's phase x 8",
                  "wrap": "vertical (256 px)", "sets": {"l": ["choir-heart-wall-l", "-edge", "-veins"],
                                                        "r": ["choir-heart-wall-r", "-edge", "-veins"]},
                  "note": "the strip's face column on the polyline; the tissue reaches 112 px outward (the wall's "
                          "deepest: the hard 300 px corridor + 12 px breathe = 102 px)"},
        "pits": {"size": list(PIT), "frames": {"shut": 0, "swell": [1, 2], "part": [3, 4], "open": 5},
                 "note": "centred 8 px inside the wall's face (l: x - 8 ... the pod's mouth toward the corridor)"},
        "chain": [{"at": [x, y], "t": t} for x, y, t in chain_points()],
        "chain_note": "a choir-heart-chain burst (10 frames, 12 fps) at each point at its time from K; debris rings of "
                      "choir-heart-debris with the bursts; the act's flash at K + 3.5 s is the built one",
        "spark": "choir-heart-spark (4 frames, 16 fps) where a shot meets a closed mouth or the body",
        "centre_tell": "choir-heart-centre frame = the line's 0.8 s tell's progress x 6, held at 5 under the line",
    }
    folder = artkit.PIVOTS if bu.claimed("choir-heart") else HELD / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / "choir-heart.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = HELD / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / "choir-heart.json").exists():
        (other / "choir-heart.json").unlink()


# =========================================================================== review

def S(name):
    return l23kit.load(name)


def chamber(pulse=0.6):
    return __import__("act7_core").arena_bg(pulse)            # ART-H2's hive-core arena (was a84.chamber_bg)


def put_body(img, body, dy=0):
    l23kit.paste_c(img, body, PIECE[0], PIECE[1] - BC[1] + BODY[1] / 2 + dy)
    return img


def add_body(img, layer, k=1.0, dy=0):
    return l23kit.add_c(img, layer, PIECE[0], PIECE[1] - BC[1] + BODY[1] / 2 + dy, k)


def heart(img, phase=1, pulse=0.5, membs=None, body=None, pulse_i=None, dy=0, singers=None, halo_k=None):
    img = l23kit.add_c(img, S("choir-heart-halo")[0], PIECE[0] + HALO_AT[0], PIECE[1] + HALO_AT[1] + dy,
                       0.4 + 0.6 * pulse if halo_k is None else halo_k)
    if body is None:
        body = {1: S("choir-heart")[0], 2: S("choir-heart-closing")[3], 3: S("choir-heart-opening")[7]}[phase]
        pulse_i = {1: 0, 2: 1, 3: 2}[phase]
    put_body(img, body, dy)
    if pulse_i is not None and pulse > 0:
        img = add_body(img, S("choir-heart-pulse")[pulse_i], pulse, dy)
    membs = membs if membs is not None else ([0] * 6 if phase == 1 else [12] * 6)
    for k, fr in enumerate(membs):
        if fr is None:
            continue
        x, y = PIECE[0] + MEMB[k][0], PIECE[1] + MEMB[k][1] + dy
        l23kit.paste_c(img, S(f"choir-heart-membrane-{k + 1}")[fr], x, y)
        if singers and singers.get(k) is not None:
            f, a = singers[k]
            img = l23kit.add_c(img, S(f"choir-heart-membrane-{k + 1}-singer")[f], x, y, a)
    return img


def intel_mask():
    """The Heart in phase 1 as the arena shows it first (the sealed body, its six membranes shut; the halo, an additive
    glow, left out), the outline only: the sensor silhouette of tools/art/intel.py's ``BOSSES`` (the user's Act 7
    playthrough, 2026-11-12: Level 50's intel had none)."""
    down = 160                                                  # the body's top on the layer (it rises off the screen)
    layer = Image.new("RGBA", (480, 440), (0, 0, 0, 0))
    put_body(layer, S("choir-heart")[0], down)
    for k, (dx, dy) in enumerate(MEMB):
        l23kit.paste_c(layer, S(f"choir-heart-membrane-{k + 1}")[0], PIECE[0] + dx, PIECE[1] + dy + down)
    return np.array(layer)[..., 3] > 127


def walls(img, xl, xr, f=0, veins=0.0, edge=1.0):
    for s, xf in (("l", xl), ("r", xr)):
        tile, ed, vn = S(f"choir-heart-wall-{s}")[f], S(f"choir-heart-wall-{s}-edge")[f], S(f"choir-heart-wall-{s}-veins")[f]
        x0 = xf - FACE[s]
        for ty in range(0, SCREEN, WALL[1]):
            l23kit.paste_c(img, tile, x0 + WALL[0] / 2, ty + WALL[1] / 2)
    return img


def walls_light(img, xl, xr, f=0, veins=0.0, edge=1.0):
    for s, xf in (("l", xl), ("r", xr)):
        x0 = xf - FACE[s]
        for ty in range(0, SCREEN, WALL[1]):
            img = l23kit.add_c(img, S(f"choir-heart-wall-{s}-edge")[f], x0 + WALL[0] / 2, ty + WALL[1] / 2, edge)
            if veins > 0:
                img = l23kit.add_c(img, S(f"choir-heart-wall-{s}-veins")[f], x0 + WALL[0] / 2, ty + WALL[1] / 2, veins)
    return img


def nodes(img, xl, xr, frames=(0, 0, 0, 0), glow=0):
    for j, (s, y) in enumerate((("l", NODE_Y[0]), ("l", NODE_Y[1]), ("r", NODE_Y[0]), ("r", NODE_Y[1]))):
        x = xl - 6 if s == "l" else xr + 6
        l23kit.paste_c(img, S(f"choir-heart-node-{s}")[frames[j]], x, y)
    return img


def nodes_light(img, xl, xr, frames=(0, 0, 0, 0), glow=0):
    for j, (s, y) in enumerate((("l", NODE_Y[0]), ("l", NODE_Y[1]), ("r", NODE_Y[0]), ("r", NODE_Y[1]))):
        if frames[j] < 2:
            x = xl - 6 if s == "l" else xr + 6
            img = l23kit.add_c(img, S("choir-heart-node-glow")[glow % 8], x, y)
    return img


def dark(img, xl, xr, ship_x, cut=0, extra=()):
    pools = [("pool", 240, 70, 230), ("cone", ship_x, 470, 16, 300), ("pool", ship_x + 50, 492, 40)]
    for j, (s, y) in enumerate((("l", NODE_Y[0]), ("l", NODE_Y[1]), ("r", NODE_Y[0]), ("r", NODE_Y[1]))):
        pools.append(("pool", xl - 6 if s == "l" else xr + 6, y, 46))
    pools += list(extra)
    return a84.darkness(img, pools, ambient=0.08 + 0.23 * cut)


def ship(img, x, y=470):
    l23kit.paste_c(img, l23kit.ship(), x, y)
    return img


def bullets(img, pts, col=a84.VIOLET):
    for x, y in pts:
        a84.vorb(img, x, y, 4, col)
    return img


def ring_pts(cx, cy, n, r, ph=0.0):
    return [(cx + np.cos(a) * r, cy + np.sin(a) * r) for a in np.linspace(0, TAU, n, endpoint=False) + ph]


def cell(frames, size, scale=2, add=None, bg=(14, 8, 20), step=6):
    w, h = size
    out = Image.new("RGBA", (len(frames) * (w * scale + step), h * scale), (0, 0, 0, 0))
    for i, fr in enumerate(frames):
        c = Image.new("RGBA", (w, h), bg + (255,))
        if fr is not None:
            c = l23kit.paste_c(c, fr, w / 2, h / 2)
        if add is not None and (add[i] if isinstance(add, list) else add) is not None:
            c = l23kit.add_c(c, add[i] if isinstance(add, list) else add, w / 2, h / 2)
        out.alpha_composite(c.resize((w * scale, h * scale), Image.NEAREST), (i * (w * scale + step), 0))
    return out


def row_of(images, gap=8):
    w = sum(i.width for i in images) + gap * (len(images) - 1)
    out = Image.new("RGBA", (w, max(i.height for i in images)), (0, 0, 0, 0))
    x = 0
    for i in images:
        out.alpha_composite(i, (x, 0))
        x += i.width + gap
    return out


def sheet_of(title, rows, width=1000):
    hgt = 40 + sum(r.height + 30 for _, r in rows)
    sheet = raster.sheet(width, hgt, title, f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 40
    for text, r in rows:
        label(sheet, 12, y, text)
        if r.width > width - 24:
            r = r.resize((width - 24, int(r.height * (width - 24) / r.width)), Image.LANCZOS)
        sheet.alpha_composite(r, (12, y + 12))
        y += r.height + 30
    return sheet.crop((0, 0, width, y))


def review():
    top = lambda im, h=270: im.crop((0, 0, W, h))   # noqa: E731
    p_lo = top(heart(chamber(0.0), 1, 0.0))
    p_hi = top(heart(chamber(1.0), 1, 1.0))
    m = [5, 0, 0, 0, 0, 0]
    sing = heart(chamber(0.6), 1, 0.6, membs=m, singers={0: (3, 1.0)})
    sing = bullets(sing, ring_pts(90, 150 + 70, 16, 60, 0.1) + ring_pts(90, 150 + 70, 16, 110, 0.3))
    half = heart(chamber(0.6), 1, 0.6, membs=[12, 12, 5, 0, 11, 12], singers={2: (4, 1.0), 4: (2, 1.0)})
    xl, xr = 80, 400
    p2 = heart(chamber(0.8), 2, 0.8)
    p2 = walls(p2, xl + 4, xr - 4, 2)
    p2 = nodes(p2, xl + 4, xr - 4, (2, 0, 0, 0))
    p2 = bullets(p2, [(140, 330), (330, 360), (250, 280)])
    p2 = ship(p2, 210)
    p2 = dark(p2, xl + 4, xr - 4, 210, cut=1)
    p2 = walls_light(p2, xl + 4, xr - 4, 2, veins=0.0)
    p2 = nodes_light(p2, xl + 4, xr - 4, (2, 0, 0, 0), 3)
    p2 = add_body(p2, S("choir-heart-pulse")[1], 0.35)
    p2 = l23kit.add_c(p2, S("choir-heart-centre")[5], PIECE[0] + CENTRE[0], PIECE[1] + CENTRE[1])
    p2v = heart(chamber(0.5), 2, 0.5)
    p2v = walls(p2v, xl, xr, 5)
    p2v = nodes(p2v, xl, xr)
    p2v = dark(p2v, xl, xr, 260)
    p2v = walls_light(p2v, xl, xr, 5, veins=1.0)
    p2v = nodes_light(p2v, xl, xr, glow=6)
    p3 = heart(chamber(0.9), 3, 0.9)
    p3 = l23kit.paste_c(p3, S("choir-heart-core")[0], PIECE[0] + CENTRE[0], PIECE[1] + CENTRE[1])
    p3 = l23kit.add_c(p3, S("choir-heart-core-glow")[4], PIECE[0] + CENTRE[0], PIECE[1] + CENTRE[1])
    p3 = bullets(p3, ring_pts(240, 150, 12, 70) + ring_pts(240, 150, 12, 120, 0.2))
    rows = [("PHASE 1 AT 1X (THE FIELD'S TOP 270 PX OVER THE HIVE-CORE ARENA): SEALED, THE PULSE AT ITS LOW AND ITS PEAK "
             "(THE -PULSE LAYER, SMOOTH)", row_of([p_lo, p_hi])),
            ("PHASE 1: THE HERALD'S MEMBRANE OPEN AND SINGING (ITS RING LIGHT, TWO RINGS OF 16); FROM THE HALF: THREE DEAD, "
             "TWO SINGING (SPORE SAC, SPIRAL PULSE), ONE DAMAGED", row_of([top(sing, 320), top(half, 320)])),
            ("PHASE 2 AT 1X: THE WALLS AT THE 320 PX CORRIDOR, DARK (8 % + A QUARTER BACK PER NODE CUT; HERE ONE CUT), THE "
             "LIT EDGES, THE NODES' GLOW, THE CENTRE WHITENING FOR THE LINE; RIGHT: A WALL MOVE'S TELL (THE VEINS), AMBIENT 8 %",
             row_of([p2, p2v]))]
    clo = [S("choir-heart")[0]] + S("choir-heart-closing")
    rows.append(("THE CLOSING (0.5X): SEALED, THEN THE CONTRACTION 0-3 (3 = PHASE 2'S BODY)",
                 row_of([b.resize((232, 156), Image.LANCZOS) for b in clo], 6)))
    opn = []
    for k in range(8):
        c = chamber(0.7).crop((8, 0, 472, 312))
        c = l23kit.paste_c(c, S("choir-heart-opening")[k], BODY[0] / 2, BODY[1] / 2 - 106 + 106)
        c = l23kit.add_c(c, S("choir-heart-opening-light")[k], BODY[0] / 2, BODY[1] / 2)
        opn.append(c.crop((112, 100, 352, 280)))
    rows.append(("THE OPENING ALONG THE SPOKES (1X CROPS ROUND THE CORE, WITH ITS LIGHT): 0-7, 7 = PHASE 3'S BODY",
                 row_of(opn[:4], 6)))
    rows.append(("", row_of(opn[4:], 6)))
    p3c = p3.crop((0, 0, W, 270))
    col = []
    for k in (0, 3, 6, 9, 11):
        c = chamber(0.0)
        c = l23kit.paste_c(c, S("choir-heart-collapse")[k], PIECE[0], PIECE[1] - BC[1] + BODY[1] / 2)
        t = k / 3.0
        for (x, y, tc) in chain_points():
            fr = int((t - tc) * 12)
            if 0 <= fr < 10:
                c = l23kit.add_c(c, S("choir-heart-chain")[fr], PIECE[0] + x, PIECE[1] + y)
        col.append(c.crop((0, 0, W, 240)).resize((240, 120), Image.LANCZOS))
    rows.append(("PHASE 3 AT 1X: THE CORE IN THE OPENED DOME, ITS BEAT, THE STORM", p3c))
    rows.append(("K: THE COLLAPSE (0.5X; FRAMES 0, 3, 6, 9, 11 OVER 4 S) WITH THE CHAIN'S BURSTS", row_of(col, 6)))
    for k in range(6):
        ms = S(f"choir-heart-membrane-{k + 1}")
        sg = S(f"choir-heart-membrane-{k + 1}-singer")
        add = [None, None, None, None, None, sg[0], None, None, None, None, None, sg[0], None]
        rows.append((f"M{k + 1} {SONGS[k][1]} (1X): CLOSED, IRIS 1-4, OPEN (+ SINGER), DAMAGED 6-11, DEAD" if k == 0 else f"M{k + 1} {SONGS[k][1]}",
                     cell(ms, (56, 56), 1, add=add, step=4)))
    grid_ = [cell([S(f"choir-heart-membrane-{k + 1}")[5]] * 8, (80, 80), 1, add=S(f"choir-heart-membrane-{k + 1}-singer"),
                  step=2) for k in range(6)]
    g_img = Image.new("RGBA", (grid_[0].width, 6 * 84), (0, 0, 0, 0))
    for k, gi in enumerate(grid_):
        g_img.alpha_composite(gi, (0, k * 84))
    rows.append(("THE SINGERS' LOOPS OVER THEIR OPEN MOUTHS (1X, 8 FRAMES, 10 FPS): M1-M6 TOP TO BOTTOM", g_img))
    nd = row_of([cell(S("choir-heart-node-l"), (48, 48), 2, add=[S("choir-heart-node-glow")[4], None, None]),
                 cell(S("choir-heart-node-r"), (48, 48), 2, add=[S("choir-heart-node-glow")[4], None, None]),
                 cell([None] * 8, (80, 80), 1, add=S("choir-heart-node-glow"), step=2)], 16)
    rows.append(("THE NODES (2X): LEFT AND RIGHT, WHOLE (WITH ITS GLOW), DAMAGED, DEAD; THE GLOW'S LOOP (1X)", nd))
    rows.append(("THE NODE CUT AND A MEMBRANE'S DEATH (1X): BURSTS AND TATTERS",
                 row_of([cell([None] * 8, (96, 96), 1, add=S("choir-heart-node-death"), step=2),
                         cell(S("choir-heart-node-tatters")[::2], (64, 64), 1, step=2)], 12)))
    rows.append(("", row_of([cell([None] * 8, (96, 96), 1, add=S("choir-heart-membrane-death"), step=2),
                             cell(S("choir-heart-membrane-tatters")[::2], (64, 64), 1, step=2)], 12)))
    core = S("choir-heart-core")
    rows.append(("THE CORE (1X): WHOLE, DAMAGED; ITS BEAT (8); A STORM ATTACK'S FLARE (6); THE CENTRE WHITENING (6)",
                 row_of([cell(core, (72, 72), 1, step=2),
                         cell([core[0]] * 8, (112, 112), 1, add=S("choir-heart-core-glow"), step=2)], 10)))
    rows.append(("", row_of([cell([core[0]] * 6, (112, 112), 1, add=S("choir-heart-core-flare"), step=2),
                             cell([None] * 6, (96, 96), 1, add=S("choir-heart-centre"), step=2)], 10)))
    wl = []
    for s in "lr":
        for f in (0, 2, 4, 6):
            c = Image.new("RGBA", WALL, (14, 8, 20, 255))
            c = l23kit.paste_c(c, S(f"choir-heart-wall-{s}")[f], WALL[0] / 2, WALL[1] / 2)
            c = l23kit.add_c(c, S(f"choir-heart-wall-{s}-edge")[f], WALL[0] / 2, WALL[1] / 2)
            if f == 6:
                c = l23kit.add_c(c, S(f"choir-heart-wall-{s}-veins")[f], WALL[0] / 2, WALL[1] / 2)
            wl.append(c)
    rows.append(("THE WALL STRIPS (1X, 128X256, WRAPPING): LEFT AND RIGHT, BREATHING FRAMES 0/2/4/6 WITH THE LIT EDGE; 6 WITH "
                 "THE VEINS' TELL", row_of(wl, 4)))
    pits = row_of([cell(S("choir-heart-pit-l"), PIT, 2), cell(S("choir-heart-pit-r"), PIT, 2)], 16)
    rows.append(("THE PITS (2X): SHUT, SWELLING 1-2, PARTING 3-4, OPEN 5; LEFT AND RIGHT", pits))
    rows.append(("THE WALL SPIT'S TELL (6), A SPARK ON THE BODY (4), THE CHAIN'S BURST (10), THE DEBRIS (8) (1X)",
                 row_of([cell([None] * 6, (40, 40), 1, add=S("choir-heart-spit"), step=2),
                         cell([None] * 4, (24, 24), 1, add=S("choir-heart-spark"), step=2),
                         cell(S("choir-heart-debris"), (16, 16), 2, step=2)], 14)))
    rows.append(("", cell([None] * 10, (128, 128), 1, add=S("choir-heart-chain"), step=2)))
    sheet = sheet_of("CHOIR HEART (FINAL R95)", rows)
    artkit.save_review(sheet, gif_frames()[::2], HEART_C, "choir-heart", fps=5)


def gif_frames():
    """A scripted pass at 1x (written every second frame at 5 fps): the arrival, phase 1's songs (one, then two at once), the closing, the arena in
    the dark (a wall move's tell, the nodes cut, the light coming back), the opening, the storm, K's collapse."""
    out = []
    xl0, xr0 = 80, 400
    for f in range(150):
        t = f / 10.0
        pulse = 0.5 - 0.5 * np.cos(TAU * t / 3.0)          # the heartbeat every 3 s (peak on the beat)
        sx = 240 + 60 * np.sin(t * 0.6)
        if f < 20:                                           # the arrival (6 s compressed into 2)
            u = smooth01(f / 19)
            img = heart(chamber(0.3 * u), 1, 0.3 * u, dy=-150 * (1 - u))
        elif f < 62:
            g = f - 20
            membs = [0] * 6
            sing = {}
            order = [0, 3, 2, 4, 1, 5]
            if g < 30:
                k = order[g // 10]
                lg = g % 10
                fr = [1, 2, 3, 4][lg] if lg < 4 else 5 if lg < 9 else 3
                membs[k] = fr
                sing[k] = (g % 8, min(1.0, lg / 4))
                img = heart(chamber(pulse), 1, pulse, membs=membs, singers=sing)
                if 4 <= lg < 9 and k == 0:
                    img = bullets(img, ring_pts(90, 150, 16, 20 + 18 * (lg - 4)))
                if 4 <= lg < 9 and k == 2:
                    img = bullets(img, [(210 + 40 * j - 80, 200 + 30 * (lg - 4)) for j in range(5)], LIME_T)
            else:
                membs = [12, 12, 5, 0, 5, 12]
                if f % 12 < 2:
                    membs[3] = 6
                img = heart(chamber(pulse), 1, pulse, membs=membs, singers={2: (g % 8, 1.0), 4: (g % 8, 1.0)})
                img = bullets(img, ring_pts(330, 185, 3, 20 + 6 * (g - 30), 0.3 * g))
        elif f < 74:                                         # the closing (4 s compressed)
            g = f - 62
            k = min(3, g // 3)
            img = heart(chamber(0.6), body=S("choir-heart-closing")[k], pulse=0.6, pulse_i=1 if k == 3 else None,
                        membs=[12] * 6)
            u = smooth01(g / 11)
            xl, xr = xl0 * u, W - (W - xr0) * u
            img = walls(img, xl, xr, g % 8)
            img = ship(img, sx)
            img = a84.darkness(img, [("pool", 240, 70, 260), ("cone", sx, 470, 16, 300)], ambient=1.0 - 0.92 * u)
            img = walls_light(img, xl, xr, g % 8, veins=1.0 - u)
        elif f < 116:                                        # phase 2
            g = f - 74
            cut = 0 if g < 12 else 1 if g < 24 else 2 if g < 33 else 3 if g < 38 else 4
            br = 12 * np.sin(TAU * 0.5 * t)
            fr = int((t * 0.5 % 1.0) * 8)
            xl = [xl0 + br, 0, 0, 0, 0][0] if cut < 4 else xl0 * max(0, 1 - (g - 38) / 4)
            xr = xr0 - br if cut < 4 else W - (W - xr0) * max(0, 1 - (g - 38) / 4)
            nf = [2 if cut > 0 else 0, 2 if cut > 2 else 0, 2 if cut > 1 else 0, 2 if cut > 3 else 0]
            img = heart(chamber(pulse), 2, pulse)
            img = walls(img, xl, xr, fr)
            img = nodes(img, xl, xr, nf)
            if 5 <= g < 12:
                img = bullets(img, [(xl + 20, 300 + 8 * (g - 5)), (xr - 20, 320 + 8 * (g - 5))], LIME_T)
            img = ship(img, sx)
            img = dark(img, xl, xr, sx, cut=cut)
            img = walls_light(img, xl, xr, fr, veins=1.0 if 14 <= g < 22 else 0.0)
            img = nodes_light(img, xl, xr, nf, g)
            if 2 <= g < 5:
                img = l23kit.add_c(img, S("choir-heart-spit")[min(5, (g - 2) * 2)], xl + 20, 300)
                img = l23kit.add_c(img, S("choir-heart-spit")[min(5, (g - 2) * 2)], xr - 20, 320)
            if 24 <= g < 32:
                img = l23kit.add_c(img, S("choir-heart-centre")[min(5, g - 24)], PIECE[0], PIECE[1] + CENTRE[1])
            for j, (s, y) in enumerate((("l", NODE_Y[0]), ("l", NODE_Y[1]), ("r", NODE_Y[0]), ("r", NODE_Y[1]))):
                tc = {0: 12, 2: 24, 1: 33, 3: 38}[j]
                if 0 <= g - tc < 8:
                    img = l23kit.add_c(img, S("choir-heart-node-death")[g - tc], xl - 6 if s == "l" else xr + 6, y)
        elif f < 126:                                        # the opening (4 s compressed)
            g = f - 116
            k = min(7, g)
            img = heart(chamber(0.8), body=S("choir-heart-opening")[k], pulse=0.0, membs=[12] * 6)
            img = add_body(img, S("choir-heart-opening-light")[k])
            img = ship(img, sx)
        elif f < 138:                                        # phase 3
            g = f - 126
            bp = 0.5 - 0.5 * np.cos(TAU * t / 2.0)
            img = heart(chamber(bp), 3, bp)
            img = l23kit.paste_c(img, S("choir-heart-core")[0 if g < 6 else 1], PIECE[0], PIECE[1] + CENTRE[1])
            img = l23kit.add_c(img, S("choir-heart-core-glow")[int(t / 2.0 % 1 * 8)], PIECE[0], PIECE[1] + CENTRE[1])
            img = bullets(img, ring_pts(240, 150, 12, 30 + 14 * (g % 8), 0.2 * g))
            img = ship(img, sx)
        else:                                                # K
            g = f - 138
            img = chamber(0.0)
            img = put_body(img, S("choir-heart-collapse")[min(11, g)])
            tk = g / 3.0
            for (x, y, tc) in chain_points():
                fr = int((tk - tc) * 12)
                if 0 <= fr < 10:
                    img = l23kit.add_c(img, S("choir-heart-chain")[fr], PIECE[0] + x, PIECE[1] + y)
            img = ship(img, sx)
        out.append(img)
    return out


# =========================================================================== check

def sets():
    out = [("choir-heart", 1, BODY), ("choir-heart-closing", 4, BODY), ("choir-heart-opening", 8, BODY),
           ("choir-heart-pulse", 3, BODY), ("choir-heart-opening-light", 8, BODY), ("choir-heart-halo", 1, HALO),
           ("choir-heart-collapse", 12, BODY), ("choir-heart-chain", 10, (128, 128)), ("choir-heart-debris", 8, (16, 16)),
           ("choir-heart-centre", 6, (96, 96)), ("choir-heart-spark", 4, (24, 24))]
    for k in range(6):
        out += [(f"choir-heart-membrane-{k + 1}", 13, (56, 56)), (f"choir-heart-membrane-{k + 1}-singer", 8, (80, 80))]
    out += [("choir-heart-membrane-death", 8, (96, 96)), ("choir-heart-membrane-tatters", 8, (64, 64))]
    for s in "lr":
        out += [(f"choir-heart-node-{s}", 3, (48, 48)), (f"choir-heart-pit-{s}", 6, PIT),
                (f"choir-heart-wall-{s}", 8, WALL), (f"choir-heart-wall-{s}-edge", 8, WALL),
                (f"choir-heart-wall-{s}-veins", 8, WALL)]
    out += [("choir-heart-node-glow", 8, (80, 80)), ("choir-heart-node-death", 8, (96, 96)),
            ("choir-heart-node-tatters", 8, (64, 64)), ("choir-heart-core", 2, (72, 72)),
            ("choir-heart-core-glow", 8, (112, 112)), ("choir-heart-core-flare", 6, (112, 112)),
            ("choir-heart-spit", 6, (40, 40))]
    return out


def side_lit(img):
    a = np.array(img.convert("RGBA"))
    return int((np.concatenate([a[:, 0], a[:, -1]])[:, :3].max(axis=-1) > 0).sum())


def check():
    bad = 0
    worst = (0, "")
    for name, n, size in sets():
        try:
            frames = l23kit.load(name)
        except FileNotFoundError:
            frames = []
        where = bu.where(name)
        files = [f for f in (ROOT / where).glob(f"{name}*.png")
                 if f.stem == name or (f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit())]
        src = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        tile = name.startswith("choir-heart-wall-")
        msg = []
        ok = len(frames) == n and all(fr.size == size for fr in frames) and src and share < 0.05
        if is_additive(name):
            lit = sum(side_lit(fr) if tile else lit_border(fr) for fr in frames)
            ok &= lit == 0
            msg.append(f"{lit} lit border px")
            levels = max(len(np.unique(np.array(fr)[..., :3].max(axis=-1))) for fr in frames)
            ok &= levels >= 32
            msg.append(f"{levels} light levels")
        else:
            lim = 60 if tile else 120
            run = 0
            for fr in frames:
                rs, _, cs, _ = cit.straight_scan(fr, wrap=tile)
                run = max(run, rs, cs)
            ok &= run < lim
            msg.append(f"longest straight run {run} px (limit {lim})")
            worst = max(worst, (run, name))
        if tile and not is_additive(name):
            ws = max(wrap_score(np.array(fr)) for fr in frames)
            ok &= ws <= 1.5
            msg.append(f"wrap {ws:.2f}")
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)}/{n} {frames[0].size if frames else None}, "
              f"checker {share * 100:.1f} %, {', '.join(msg)}")
    piv = (artkit.PIVOTS if bu.claimed("choir-heart") else HELD / "pivots") / "choir-heart.json"
    ok = piv.exists() and json.loads(piv.read_text())["source"] == SOURCE
    bad += not ok
    print(f"{'ok ' if ok else 'BAD'} {piv.relative_to(ROOT)}")
    peak = pulse_peak()
    print(f"the pulse's peak (sealed body + full pulse layer): luminance {peak:.0f} (bullet cores 255)")
    print(f"straight-line scan: longest {worst[0]} px ({worst[1]})")
    return bad == 0


def pulse_peak():
    b, p = arr(l23kit.load("choir-heart")[0]), arr(l23kit.load("choir-heart-pulse")[0])
    rgb = np.clip(b[..., :3] + p[..., :3], 0, 255)
    lum = rgb @ np.array((0.299, 0.587, 0.114))
    return float(np.percentile(lum[b[..., 3] > 0], 99.9))


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        build(run())
    review()
