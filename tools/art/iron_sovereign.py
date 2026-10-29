#!/usr/bin/env python3
"""Production art: the Iron Sovereign, Act 5's boss (design/enemies/bosses/iron-sovereign; M9 part H batch, concept
round 68, step ART-H1). From the chosen round-60 look **A.17 b** "armoured links" (tools/concept/belt_r60.py
``segment_scene('b', ring)``, imported unchanged; ``base_scene`` and ``core_scene`` re-laid here, see below) in
**Q.11 b**, the belt look the user picked in round 60: gunmetal and charcoal hulls (belt_units.py ``gunmetal``: hull
5a606c, facets 464b55) with their gold, red running lights.

**A.23 top-lit** (round 60): the ring segments are rendered **once with the light fixed from the top** (a point light
straight above the station's centre, no fill light, an environment without a side), so a segment's look does not
depend on where it stands on the wheel: the game draws a segment at any heading from the 0-45 degree set by quarter
turns and a mirror (the rule below), exact on the pixel grid because every frame keeps the ring's centre on a pixel
corner. The sun's top-left direction is carried by two **static overlays** over the rings (``-rim`` additive, ``-shade``
normal), drawn on top of the turning wheel; the static parts (the base under the rings, the core, the battery mounts and
turrets, the bays) are lit by the belt's sun as every other unit (each frame its own render; nothing lit is turned).

Scale: 40 px per model unit, the station's centre at a pixel corner. Screen angles ("headings") are degrees
**clockwise on the screen from straight down**, the simulation's convention (RingArena: a mount at angle a sits at
(-r sin a, r cos a) px from the centre, y down). Segment j of a ring is centred at ``angle(ring) + j * 360 / segments``
(segment 0 under battery 0; the batteries stand on every second segment: outer 12 segments / 6 SAM, middle 8 / 4 rail,
inner 6 / 3 laser).

**The segment rule** (a heading h in degrees, any value): d = h mod 360, q = floor(d / 90), r = d - 90 q; if r <= 45
the frame is k = round(r / 45 * n8), else k = round((90 - r) / 45 * n8) **mirrored** across the 45-degree line, the
map (x, y) -> (-y, -x) about the centre; then turned q quarter turns clockwise on the screen, (x, y) -> (-y, x) each.
A frame's pixels start at ``offset`` [x0, y0] from the centre (px, y down); mirrored, its size (w, h) becomes (h, w)
and its offset (-y0 - h, -x0 - w); each quarter turn makes it (-y0 - h, x0) with the size swapped (a pixel cell to a
pixel cell, the centre a corner). ``seg_pose`` / ``place`` below are the reference; the review composes with them.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: l23kit's rule claims
``iron-sovereign`` once design/enemies/bosses/iron-sovereign/data.yaml exists; all held until H2 writes it):
  iron-sovereign-segment-<outer|middle|inner>_0..n8   normal, top-lit: one hull block of the ring (A.17 b: a gunmetal
                         block, a gold-edged cap plate, a gold stripe on its outer edge, a dark red lamp glass at each
                         end, unlit) at the headings 0..45 degrees in n8 equal steps (outer 87, middle 62, inner 40: the
                         outer edge moves <= 2 px a step); trimmed, offsets in the pivots; one 48-colour palette over the
                         three sets. A locked ring is drawn **tinted** (RGB x ``locked_tint``, 0.62) with no lamp glows
  iron-sovereign-rim     224x224, additive, static, **drawn 2x** (soft light: half resolution saves the page): the sun's top-left sheen over the three ring bands and a gold glint
                         on each band's sunward edge (drawn after the rings, centred on the station)
  iron-sovereign-shade   224x224, normal, stepped translucency, drawn 2x: the far (bottom-right) side of the bands in shadow
  iron-sovereign-base    456x456, normal, sunlit: the static under-structure (a charcoal disc with a 2 px gold rim on
                         its sunward silhouette, the catwalks between the rings with their light dots and struts, the
                         links' gold track under every ring's gaps, the core's collar with its two gold rings and the
                         four rim bays on the diagonals at 60 px); its lights unlit (the lamp sprites light them)
  iron-sovereign-lamp-red    9x9 additive: a segment's running light (two a segment, positions in the pivots: radius and
                         the angle from the segment's centre); none on a locked ring, none before the ring lights up
  iron-sovereign-lamp-white  5x5 additive: a catwalk light dot; iron-sovereign-lamp-gold  7x7 additive: a strut lamp
                         (positions per catwalk in the pivots: the intro lights them ring by ring, phase 4 puts them out)
  iron-sovereign-bay-<0..3>_0..3   24x24, normal, sunlit: a rim bay's doors, closed, opening, open (0.5 s tell); bay k at
                         45 + 90 k degrees (0 down-left, then up-left, up-right, down-right), 60 px from the centre
  iron-sovereign-bay-glow_0..1     32x32 additive: the bay's gold light (the doors opening, then full)
  iron-sovereign-<sam|rail|laser>-mount_0..2   40x40 / 48x48 / 44x44, normal, sunlit: the battery's collar in the
                         station's livery, its four lamps 0 dark (before the ring lights), 1 gold (out of the arc),
                         2 red (armed: in the fire arc)
  iron-sovereign-<sam|rail|laser>-wreck   the same size: the collar burnt out with the turret's torn stump (a dead
                         battery; it stays on its ring, smoking while the ring lives: ``-vent``)
  iron-sovereign-sam_0..63, -sam-open_0..63   40x40 (24 px per model unit): the SAM Nest's twin-tube rack (A.11 a) at 64 headings (k x 5.625
                         degrees clockwise from down), drawn on the mount; ``-open`` the salvo's tell (the gold hatch
                         raised, the tubes' mouths and the red lock light lit)
  iron-sovereign-rail_0..63   60x60 (26 px per unit): the Rail Bunker's twin-rail turret (A.13 a) at 64 headings
  iron-sovereign-rail-charge_0..3   16x16 additive: the gold coils' charge glow (rising), on the coils (pivots)
  iron-sovereign-laser_0..63  44x44: the Sentinel Tower's emitter head (A.12 a) at 64 headings
  iron-sovereign-laser-lens_0..3    16x16 additive: the crimson lens lit (the sweep's telegraph and beam), on the lens
  iron-sovereign-core_0..11   up to 112x112, normal, sunlit, centred: 0 shut (six petals, the crest's gold ray and
                         ring on each), 1..10 the petals sliding out and down (the 3 s opening, about 3.7 fps), 11 open
                         (the white-gold reactor); the implosion plays them backwards
  iron-sovereign-core-slits   additive, centred: the shut core's shutter slits glowing gold (the fan's 0.5 s tell)
  iron-sovereign-core-glow_0..3  96x96 additive, centred: the open reactor's light, 0-1 the idle pulse, 2-3 brightening
                         (the spiral's 0.6 s tell); the hex shield over it is the shared ``enemy-shield-medium``
  iron-sovereign-vent_0..7    32x32, normal, stepped translucency: grey gas venting from a wrecked mount in vacuum (it
                         spreads and thins, never rises), 8 fps, loop with random starts
  iron-sovereign-cookoff_0..9  64x64 additive: a dead battery cooking off at the death (a white-gold flash out of the
                         mount, a thin ring, sparks), 30 fps; with ``-cookoff-debris_0..9`` (64x64 normal: gunmetal and
                         gold collar shards flung outward), over the shared vacuum burst if the game wants more
  iron-sovereign-implosion_0..11  96x96 additive (drawn 1x or scaled up to taste): the core's white-gold collapse (0-5 drawing in) and blow (6-11),
                         15 fps, with the shared vacuum explosion drawn 2x and the screen flash
  iron-sovereign-arc-<outer|middle|inner>   (band width - 6) x 4 additive: the fire arc's faint red band across a
                         ring's hull (the strip's x runs outward across the band, from r0 + 3), tiled round the live
                         sector as a band of quads; its first and last column are the bright edges
  assets/pivots/iron-sovereign.json (held with the frames): every set's frame sizes and offsets, the rule above, the
                         lamps, the bays, the muzzles, coils and lenses per heading, the tints
  intel/boss-iron-sovereign   via tools/art/intel.py (``intel_mask`` here: the rings, the batteries and the core)
  design/enemies/bosses/iron-sovereign/concept/iron-sovereign-final-r68-a.png/.gif   review sheet (the station at rest,
                         the segment sets, the seams at 1:1 at several angles and the rule against direct renders, the
                         batteries, the core, the locked look, the death's frames, the overlays, readability crops, the
                         measured atlas share) and GIF (the intro, phase 1 and the arc, a lock, the core opening, the
                         death)

Run: python3 tools/art/iron_sovereign.py [rings] [base] [batteries] [core] [fx] [--review] [--check]
     (no subject = all, about 2 minutes on 8 workers; --review rebuilds only the review files from the written frames;
     --check lists every set with its folder, size and frame count and fails on a missing one or a missing Source
     chunk, and prints the atlas share)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import belt_units_e as bue
import belt_units_f as buf
from artkit import DESIGN, ROOT, TAU

import belt_r60 as b60  # noqa: E402  (concept script, imported unchanged: the round-60 station)
import enemies_r60 as e60  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.enemy_models import A_FACET, A_GOLD, A_GUN, A_HULL, A_RED, hx  # noqa: E402
from render.enemy_rigs import model_space_materials  # noqa: E402
from render.sdf import Material, sd_box, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "iron_sovereign.py"
BATCH = "M9 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r68"
ROOT_NAME = "iron-sovereign"
CONCEPT = DESIGN / "enemies" / "bosses" / "iron-sovereign" / "concept"
WORKERS = 8
PX = 40.0                                    # px per model unit (the round-60 station's scale)
CX, CY = 240, 160                            # the centre on the screen (the field 480x540)
# ring: (r0, r1, segments, mount radius, battery kind, batteries)
RINGS = {"outer": (180, 220, 12, 200, "sam", 6), "middle": (123, 157, 8, 140, "rail", 4),
         "inner": (70, 100, 6, 85, "laser", 3)}
SEG_EXPOSURE = 0.72
LIGHT_H = 10.0                               # model units: the top light straight above the centre (400 px)
LOCKED_TINT = 0.62
SEG_COLOURS = 48
SUN_FAR = sdf.norm(sdf.vec(-0.55, 0.6, 0.75)) * 400.0      # the belt's sun (belt_r60 SUN), far: one light for tiles
GOLD = np.array((255, 200, 90), float)
GOLD_HI = np.array((255, 236, 170), float)
CRIMSON = np.array((255, 48, 56), float)
RED_LAMP = np.array((255, 60, 44), float)
WHITE_GOLD = np.array((255, 244, 214), float)
A_LAMP, A_STRIP, A_BAY = 7, 8, 9
A_REACT, A_SLIT = 10, 11
BAY_R = 60                                   # px: the rim bays on the collar, clear of the opened petals (<= 54 px)
TURRET = {"sam": (40, 24.0), "rail": (60, 26.0), "laser": (44, 23.0)}       # canvas px, px per model unit
MOUNT = {"sam": 40, "rail": 48, "laser": 44}
HEADINGS = 64
MUZZLE = {"sam": 0.66, "rail": 1.06, "laser": 0.76}          # model units ahead of the pivot: tube mouths, rail tips, lens
COILS = 0.55                                 # model units: the rail turret's middle coil
CORE_FRAMES = 12
CORE_SLIDE = 0.3                             # model units the petals slide out when open (12 px: clear of the bays)

write, load, where = bu.write, bu.load, bu.where


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def n8(ring):
    """Steps over 0..45 degrees so the ring's outer edge moves <= 2 px a step."""
    r1 = RINGS[ring][1]
    return int(np.ceil(45.0 / np.degrees(2.0 / r1)))


def seg_headings(ring):
    n = n8(ring)
    return [45.0 * k / n for k in range(n + 1)]


# =========================================================================== materials

def station_mats(lamps=0.12, strip=0.08, bay=0.0, react=0.4, slit=0.0):
    """Q.11 b over the round-60 station's slots: gunmetal hull, charcoal facets, a darker gun slot; the lights unlit
    (their glow is a sprite of its own) unless asked."""
    m = b60.station_mats(lamps=lamps, strip=strip, bay=bay)
    m[A_HULL] = replace(m[A_HULL], albedo=hx("5a606c"), shininess=90, spec=0.4)
    m[A_FACET] = replace(m[A_FACET], albedo=hx("464b55"), spec=0.5)
    m[A_GUN] = replace(m[A_GUN], albedo=hx("2c3038"))
    m[A_GOLD] = replace(m[A_GOLD], spec=0.6)
    m += [e60.emissive(np.array((1.0, 0.86, 0.5)), 0.25 + 0.85 * react),
          e60.emissive(np.array((1.0, 0.76, 0.28)), 0.04 + 1.9 * slit)]
    return m


def _sym_env(r):
    """The concept's chrome environment without its side (y) term: the same reflection at every heading."""
    q = r.copy()
    q[:, 1] = 0.0
    return _ENV(q)


_ENV = sdf.env_color


# =========================================================================== ring segments (top-lit)

def seg_window(ring, h_deg):
    """The render window for heading h: its centre on the pixel lattice (integer px from the station's centre) and an
    even size that holds the block."""
    r0, r1, n = RINGS[ring][:3]
    h = np.radians(h_deg)
    mid = (r0 + r1) / 2
    cx, cy = int(round(-mid * np.sin(h))), int(round(mid * np.cos(h)))
    th = np.linspace(-np.pi / n, np.pi / n, 25)[:, None] + h
    rr = np.linspace(r0, r1, 5)[None, :]
    xs, ys = -rr * np.sin(th) - cx, rr * np.cos(th) - cy
    half = int(np.ceil(max(np.abs(xs).max(), np.abs(ys).max()))) + 5
    return cx, cy, 2 * half


def seg_job(job):
    """(ring, k) -> (native unquantised frame, offset of its top left from the centre)."""
    ring, k = job
    h_deg = seg_headings(ring)[k]
    cx, cy, w = seg_window(ring, h_deg)
    ext = w / PX
    c = np.array((cx / PX, -cy / PX, 0.0))
    key = (np.array((0.0, 0.0, LIGHT_H)) - c) / (ext / 2.3)
    h = np.radians(h_deg)
    phi = np.arctan2(-np.cos(h), -np.sin(h))           # the segment's direction in the model (y up)
    base = b60.segment_scene("b", ring)
    scene = lambda p: base(sdf.rotate_z(p, phi))  # noqa: E731
    sdf.env_color = _sym_env
    try:
        hi = sdf.render(scene, model_space_materials(station_mats(), phi), (w * 4, w * 4), ext, center=(c[0], c[1]),
                        key_pos=key, fill=0.0, exposure=SEG_EXPOSURE)
    finally:
        sdf.env_color = _ENV
    img = artkit.native(hi, 4, 80)
    box = img.getchannel("A").getbbox()
    return img.crop(box), (cx - w // 2 + box[0], cy - w // 2 + box[1])


def seg_pose(ring, deg):
    """(frame index, mirrored, quarter turns) of a segment centred at heading ``deg``."""
    d = deg % 360.0
    q = int(d // 90) % 4
    r = d - 90 * q
    mirrored = r > 45
    k = int(round((90 - r if mirrored else r) / 45 * n8(ring)))
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


def lamp_points(ring):
    """The segment's two lamps: (radius px, angle from the segment's centre in degrees) (segment_scene b's lamps)."""
    r0, r1, n = RINGS[ring][:3]
    rl = (r0 + r1) / 2 / PX - 0.02
    s = TAU / n
    dth = (s / 2 * rl - 0.075 - 0.14) / rl
    return rl * PX, float(np.degrees(dth))


# =========================================================================== base (static, sunlit)

def base_scene():
    """belt_r60's under-structure (variant b) re-laid: the bays at 60 px (clear of the opened petals), the lights'
    material slots kept."""
    _round_slab = b60._round_slab

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        r = np.hypot(x, y)
        th = np.arctan2(y, x)
        items = [(_round_slab(r - 222 / PX, z, -0.22, 0.12, 0.04), A_FACET)]
        for g0, g1 in ((100, 123), (157, 180)):
            gm = (g0 + g1) / 2 / PX
            items.append((_round_slab(np.abs(r - gm) - 0.09, z, -0.07, 0.03, 0.01), A_GUN))
            k = 24 if g0 > 120 else 16
            st = (th + np.pi / k) % (TAU / k) - np.pi / k
            strip = np.hypot(np.abs(st) * r, r - gm) - 0.035
            items.append((np.sqrt(np.maximum(strip, 0) ** 2 + (z + 0.02) ** 2) - 0.02, A_STRIP))
            ss = (th + np.pi / 6) % (TAU / 6) - np.pi / 6
            strut = np.maximum(np.abs(ss) * r - 0.07, np.maximum(g0 / PX - r, r - g1 / PX))
            items.append((_round_slab(strut, z, -0.04, 0.04, 0.01), A_HULL))
            items.append((np.sqrt(np.maximum(np.hypot(np.abs(ss) * r, r - gm) - 0.05, 0) ** 2 + (z - 0.02) ** 2)
                          - 0.025, A_LAMP))
        collar = np.maximum(r - 68 / PX, 40 / PX - r)
        items.append((_round_slab(collar, z, 0.02, 0.12, 0.05), A_HULL))
        items.append((_round_slab(np.abs(r - 66 / PX) - 0.05, z, 0.12, 0.05, 0.02), A_GOLD))
        items.append((_round_slab(np.abs(r - 43 / PX) - 0.04, z, 0.12, 0.05, 0.02), A_GOLD))
        for k in range(4):
            a = bay_model_angle(k)
            q = sdf.rotate_z(p - np.array((BAY_R / PX * np.cos(a), BAY_R / PX * np.sin(a), 0.0)), a)
            items.append((sd_box(q, (0, 0, 0.12), (0.17, 0.12, 0.05), 0.02), A_GUN))
            items.append((sd_box(q, (0, 0, 0.16), (0.12, 0.08, 0.03), 0.01), A_BAY))
        for rr in (85, 140, 200):                              # the links' track, under the gaps
            items.append((_round_slab(np.abs(r - rr / PX) - 0.06, z, -0.06, 0.04, 0.01), A_GOLD))
        return union(*items)
    return scene


def bay_heading(k):
    return 45.0 + 90.0 * k


def bay_model_angle(k):
    """Screen heading (cw from down) -> the model's angle (ccw from +x, y up): theta = -90 - heading."""
    return np.radians(-90.0 - bay_heading(k))


def screen_xy(r, heading_deg):
    a = np.radians(heading_deg)
    return -r * np.sin(a), r * np.cos(a)


BASE_SIZE, BASE_TILES = 456, 6


def sun_render(scene, mats, cx_px, cy_px, w, h, factor, **kw):
    """A window (w x h px) centred cx, cy px from the station's centre (y down), lit by the far sun (so tiles share
    one light)."""
    ext = w / PX
    c = np.array((cx_px / PX, -cy_px / PX, 0.0))
    key = (SUN_FAR - c) / (ext / 2.3)
    return sdf.render(scene, mats, (w * factor, h * factor), ext, center=(c[0], c[1]), key_pos=key, **kw)


def base_tile(job):
    i, j = job
    t = BASE_SIZE // BASE_TILES
    cx = -BASE_SIZE / 2 + (i + 0.5) * t
    cy = -BASE_SIZE / 2 + (j + 0.5) * t
    hi = sun_render(base_scene(), station_mats(), cx, cy, t, t, 4, steps=110)
    return sprite.downsample(hi, 4)


def silhouette_rim(img, width=2):
    """A gold rim on the edges facing the top-left sun, red on the far ones (Q.11 b's frame, belt_units ``rim``) at
    ``width`` px: the station's own outline."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    pad = np.pad(op, width)
    h, w = op.shape
    tl = np.zeros_like(op)
    br = np.zeros_like(op)
    for d in range(1, width + 1):
        tl |= ~pad[width:width + h, width - d:width - d + w] | ~pad[width - d:width - d + h, width:width + w]
    br |= ~pad[width:width + h, width + 1:width + 1 + w] | ~pad[width + 1:width + 1 + h, width:width + w]
    tl &= op
    br &= op & ~tl
    a[tl, :3] = a[tl, :3] * 0.3 + bu.GOLD_RIM * 0.7
    a[br, :3] = a[br, :3] * 0.45 + bu.RED_RIM * 0.55
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def base_lamps():
    """Catwalk light dots (white) and strut lamps (gold): screen px from the centre, per catwalk."""
    out = {}
    for name, (g0, g1), k in (("inner", (100, 123), 16), ("outer", (157, 180), 24)):
        gm = (g0 + g1) / 2
        strips = [screen_xy(gm, -90.0 - 360.0 * m / k) for m in range(k)]
        struts = [screen_xy(gm, -90.0 - 60.0 * m) for m in range(6)]
        out[name] = {"white": [[round(x, 2), round(y, 2)] for x, y in strips],
                     "gold": [[round(x, 2), round(y, 2)] for x, y in struts]}
    return out


# =========================================================================== bays

BAY = 24


def bay_scene(k, u):
    """Bay k's housing with two door leaves sliding apart across it (``u`` 0 shut .. 1 open), the floor glowing."""
    a = bay_model_angle(k)
    bx, by = BAY_R / PX * np.cos(a), BAY_R / PX * np.sin(a)

    def scene(p):
        q = sdf.rotate_z(p - np.array((bx, by, 0.0)), a)
        items = [(sd_box(q, (0, 0, 0.12), (0.17, 0.12, 0.05), 0.02), A_GUN),
                 (sd_box(q, (0, 0, 0.1), (0.12, 0.08, 0.03), 0.01), A_BAY)]
        gap = 0.12 * u
        for s in (-1, 1):
            items.append((sd_box(q, (0, s * (0.04 + gap), 0.165), (0.115, 0.038, 0.012), 0.004), A_FACET))
        return union(*items)
    return scene


def bay_job(job):
    k, u = job
    x, y = screen_xy(BAY_R, bay_heading(k))
    cx, cy = int(round(x)), int(round(y))
    hi = sun_render(bay_scene(k, u), station_mats(bay=u), cx, cy, BAY, BAY, 8)
    return artkit.native(hi, 8, 80), (cx - BAY // 2, cy - BAY // 2)


# =========================================================================== batteries (sunlit, 64 headings)

def mount_scene(kind, wreck=False):
    """belt_r60's collar (a gunmetal drum, a gold ring, four lamps on the diagonals); the wreck burnt with the turret's
    torn stump in it."""
    scene0, _ = b60.collar_model("gold")

    def scene(p):
        d, m = scene0(p)
        if wreck:
            ang = np.arctan2(p[:, 1], p[:, 0])
            stump = np.maximum(sd_cylinder_z(p, (0, 0, 0.3), 0.36, 0.14),
                               p[:, 2] - (0.4 + 0.08 * np.sin(ang * 5) + 0.05 * np.sin(ang * 9 + 1)))
            d2 = stump
            m = np.where(d2 < d, A_GUN, m)
            d = np.minimum(d, d2)
            hole = np.hypot(p[:, 0] + 0.05, p[:, 1] - 0.03) - 0.2
            d = np.maximum(d, -np.where(p[:, 2] > 0.36, hole, 1.0))
        return d, m
    return scene


def mount_mats(lamp, wreck=False):
    mats = bu.gunmetal(e60.belt_mats("2c3038"))
    col = {"dark": np.array((0.3, 0.06, 0.04)), "gold": np.array((1.0, 0.76, 0.3)), "red": e60.em.RED}[lamp]
    k = 0.15 if lamp == "dark" else 1.9
    mats = mats + [e60.emissive(col, k)]
    mats[A_RED] = e60.emissive(e60.em.RED, 0.2)
    if wreck:
        scorch = lambda p, n: 0.55 + 0.35 * np.sin(p[:, 0] * 23 + 2 * np.sin(p[:, 1] * 17)) * np.cos(p[:, 1] * 19)  # noqa: E731
        mats[A_HULL] = replace(mats[A_HULL], albedo=(0.16, 0.15, 0.15), spec=0.15, pattern=scorch)
        mats[A_GOLD] = replace(mats[A_GOLD], albedo=(0.36, 0.25, 0.1), spec=0.3)
        mats[A_GUN] = Material((0.08, 0.07, 0.07), metal=0.2, shininess=20, spec=0.2, pattern=scorch)
        mats[7] = e60.emissive(np.array((0.2, 0.05, 0.03)), 0.1)
    return mats


def mount_job(job):
    kind, state = job
    n = MOUNT[kind]
    ext = n / ((n / 2 - 1) / 0.95)
    wreck = state == "wreck"
    img = bu.render(mount_scene(kind, wreck), mount_mats("dark" if wreck else state, wreck), n, ext)
    return bu.rim(img)


def turret_model(kind, state):
    if kind == "sam":
        hatch = 1.0 if state == "open" else 0.0
        scene, mats = e60.sam_rack("a", hatch, hatch, "red")
        mats = bu.gunmetal(mats)
        mats[-1] = e60.emissive(e60.em.RED, 0.3 + 1.8 * hatch)
        return scene, mats
    if kind == "rail":
        return buf._rb_turret_scene(), buf._rb_mats(0.0)
    return bue._sen_head()


def turret_job(job):
    kind, state, k = job
    size, ppu = TURRET[kind]
    scene, mats = turret_model(kind, state)
    return bu.rim(bu.render(*bu.turned(scene, mats, k, HEADINGS), size, size / ppu))


def ahead(kind, length_units, k):
    """A point ``length_units`` ahead of a turret's pivot at heading k: px from the battery's centre (y down)."""
    ppu = TURRET[kind][1]
    return list(map(lambda v: round(v, 2), screen_xy(length_units * ppu, 360.0 * k / HEADINGS)))


# =========================================================================== the core (static, sunlit)

def core_scene(u=0.0):
    """belt_r60's core (six shutter petals with the crest's gold ray and ring over the reactor) with the petals
    sliding out CORE_SLIDE units and down when open."""
    def scene(p):
        items = [(sd_sphere(p, (0, 0, -0.15), 0.62), A_REACT),
                 (sd_cylinder_z(p, (0, 0, -0.12), 0.98, 0.06), A_SLIT)]
        for k in range(6):
            a = k * TAU / 6 + np.pi / 6
            off = np.array((np.cos(a), np.sin(a), 0.0)) * CORE_SLIDE * u - np.array((0, 0, 0.22 * u))
            q = p - off
            r = np.hypot(q[:, 0], q[:, 1])
            th = np.arctan2(q[:, 1], q[:, 0])
            dth = (th - a + np.pi) % TAU - np.pi
            dome = sdf.sd_ellipsoid(q, (0, 0, -0.05), (1.0, 1.0, 0.5))
            wedge = (np.abs(dth) - np.pi / 6) * r + 0.035 + 0.02 * u
            d = np.maximum(np.maximum(dome, wedge), -q[:, 2] - 0.06)
            ray = (np.abs(dth) * r < 0.05) & (r > 0.25)
            ring = np.abs(r - 0.66) < 0.035
            m = np.where(ray | ring, A_GOLD, np.where(r > 0.9, A_FACET, A_HULL))
            items.append((d, m))
        return union(*items)
    return scene


CORE_WIN = 112


def core_u(i):
    return i / (CORE_FRAMES - 1)


def core_job(job):
    kind, i = job
    u = core_u(i)
    if kind == "frame":
        react = 0.4 + 0.5 * u
        hi = sun_render(core_scene(u), station_mats(react=react), 0, 0, CORE_WIN, CORE_WIN, 4)
        return bu.rim(artkit.native(hi, 4, 80))
    mats = station_mats(slit=1.0)                              # the slits alone, emission only
    mats = [Material((0, 0, 0)) if j != A_SLIT else replace(m, albedo=(0, 0, 0)) for j, m in enumerate(mats)]
    hi = sun_render(core_scene(0.0), mats, 0, 0, CORE_WIN, CORE_WIN, 4, shadows=False, ambient=0.0)
    return artkit.native(hi, 4, 0)


# =========================================================================== procedural effects

def grid(n):
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float64) + 0.5
    return xx - n / 2, yy - n / 2


def glow_img(f, col, white=0.0, levels=8):
    """A 0..1 field as an additive glow (premultiplied, ordered-dithered steps)."""
    f = artkit.ordered_dither(np.clip(f, 0, 1), levels)
    rgb = np.asarray(col, float)[None, None] * f[..., None] + 255 * white * np.clip(f - 0.55, 0, 1)[..., None] / 0.45
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full(f.shape, 255.0)]).astype(np.uint8), "RGBA")
    return artkit.additive(img)


def dot_glow(n, col, core=1.2, halo=2.6, k=1.0):
    x, y = grid(n)
    r = np.hypot(x, y)
    f = (np.exp(-(r / core) ** 2) * 1.2 + np.exp(-(r / halo) ** 2) * 0.45) * k
    return glow_img(f, col, white=0.5, levels=6)


OVERLAY = 224                                # the overlays' size: half the station's, drawn 2x (soft light only)


def rim_overlay():
    """The sun on the turning rings, static: a soft top-left sheen over each band and a gold glint on the band's
    sunward outer edge (additive; half resolution, drawn 2x about the centre)."""
    n = OVERLAY
    x, y = (2 * v for v in grid(n))
    r = np.hypot(x, y)
    sunward = np.clip((-x - y) / np.maximum(r, 1) / np.sqrt(2), 0, 1)   # 1 toward the top left
    f = np.zeros((n, n))
    for ring, (r0, r1, *_rest) in RINGS.items():
        band = (r >= r0) & (r <= r1)
        across = np.clip((r - r0) / (r1 - r0), 0, 1)
        f += band * sunward ** 1.6 * (0.10 + 0.14 * across)
        edge = (r >= r1 - 2.6) & (r <= r1 - 0.4)
        f += edge * np.clip(sunward - 0.25, 0, 1) ** 1.2 * 0.5
    return glow_img(f, GOLD_HI, levels=10)


def shade_overlay():
    """The far side of the bands in shadow (normal blend, stepped translucency of near-black; half resolution, drawn
    2x)."""
    n = OVERLAY
    x, y = (2 * v for v in grid(n))
    r = np.hypot(x, y)
    farward = np.clip((x + y) / np.maximum(r, 1) / np.sqrt(2), 0, 1)
    alpha = np.zeros((n, n))
    for ring, (r0, r1, *_rest) in RINGS.items():
        band = (r >= r0) & (r <= r1)
        alpha = np.maximum(alpha, band * farward ** 1.3 * 0.42)
    alpha = artkit.ordered_dither(alpha / 0.42, 5) * 0.42
    rgba = np.zeros((n, n, 4))
    rgba[..., :3] = (6, 6, 12)
    rgba[..., 3] = np.round(alpha * 255)
    rgba[rgba[..., 3] == 0] = 0
    return Image.fromarray(rgba.astype(np.uint8), "RGBA")


def arc_strip(ring):
    r0, r1 = RINGS[ring][:2]
    w = int(r1 - r0 - 6)
    f = np.full((4, w), 0.16)
    f[:, 0] = f[:, -1] = 0.85
    f[:, 1] = f[:, -2] = 0.4
    return glow_img(f, CRIMSON, levels=8)


def vent_frame(i, n=32, frames=8):
    """Gas venting from a wrecked mount in vacuum: a pale jet that spreads and thins; stepped translucency."""
    x, y = grid(n)
    s = (i + 0.5) / frames
    rng = np.random.default_rng(611 + i)
    f = np.zeros((n, n))
    for j in range(5):
        a = -2.3 + 0.5 * j + 0.15 * np.sin(i + j)
        d = (2 + 11 * s) * (0.7 + 0.15 * j % 3)
        bx, by = np.cos(a) * d, np.sin(a) * d
        rad = 2.5 + 6 * s
        f += np.exp(-(((x - bx) ** 2 + (y - by) ** 2) / rad ** 2)) * (1 - s) ** 1.2
    f += rng.random((n, n)) * 0.08 * (1 - s)
    alpha = artkit.ordered_dither(np.clip(f * 0.8, 0, 0.75) / 0.75, 4) * 0.75
    rgba = np.zeros((n, n, 4))
    rgba[..., :3] = np.array((150, 156, 168)) * (0.75 + 0.25 * np.clip(f, 0, 1))[..., None]
    rgba[..., 3] = np.round(alpha * 255)
    rgba[rgba[..., 3] == 0] = 0
    return Image.fromarray(rgba.astype(np.uint8), "RGBA")


def cookoff_frame(i, n=64, frames=10):
    """A dead battery cooking off: a white-gold flash out of the mount, a thin ring running out, sparks."""
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
    return glow_img(flash + ring + rays + sp, (255, 214, 130), white=0.9, levels=8)


def debris_frame(i, n=64, frames=10):
    """Collar shards flung outward (gunmetal and gold, lit top-left), normal blending."""
    s = i / (frames - 1)
    img = Image.new("RGBA", (n, n), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(73)
    for j in range(11):
        aa = rng.uniform(0, TAU)
        dist = 4 + rng.uniform(12, 26) * s ** 0.8
        spin = rng.uniform(-6, 6) * s
        size = rng.uniform(1.6, 3.4) * (1 - 0.3 * s)
        gold = j % 3 == 0
        cx, cy = n / 2 + np.cos(aa) * dist, n / 2 + np.sin(aa) * dist
        pts = [(cx + np.cos(spin + t) * size * (1.0 if t < 3 else 0.6), cy + np.sin(spin + t) * size)
               for t in (0.3, 2.2, 3.9, 5.2)]
        base = np.array((214, 160, 64) if gold else (92, 98, 110)) * (1 - 0.35 * s)
        d.polygon(pts, fill=tuple(int(v) for v in base) + (255,))
        d.point([(round(pts[0][0]), round(pts[0][1]))], fill=tuple(int(min(255, v * 1.6)) for v in base) + (255,))
    return img


def implosion_frame(i, n=96, frames=12):
    """The core's collapse (0-5: light drawing in, streaks inward) and blow (6-11: a white-gold burst and ring)."""
    x, y = grid(n)
    r, a = np.hypot(x, y) / 0.6, np.arctan2(y, x)
    if i < 6:
        s = i / 5
        rad = 46 * (1 - s) + 8
        f = np.exp(-((r - rad) / (5 + 3 * s)) ** 2) * (0.5 + 0.5 * s) + np.exp(-(r / (10 + 6 * s)) ** 2) * (0.4 + 0.9 * s)
        f += np.clip(np.cos(a * 9 + s * 2) ** 30, 0, 1) * np.exp(-(r / (rad + 6)) ** 2) * 0.6 * s
    else:
        s = (i - 6) / 5
        f = np.exp(-(r / (14 + 50 * s)) ** 2) * 1.5 * (1 - s) ** 1.2
        f += np.exp(-((r - (20 + 56 * s)) / 3.0) ** 2) * 0.9 * (1 - s)
        f += np.clip(np.cos(a * 11) ** 40, 0, 1) * np.exp(-(r / (30 + 40 * s)) ** 2) * (1 - s)
    return glow_img(f, (255, 226, 160), white=1.0, levels=10)


def core_glow(i, n=96):
    x, y = grid(n)
    r = np.hypot(x, y)
    k = (0.75, 0.9, 1.15, 1.4)[i]
    f = (np.exp(-(r / 20) ** 2) * 0.9 + np.exp(-(r / 36) ** 2) * 0.35) * k
    return glow_img(f, (255, 230, 170), white=0.6, levels=8)


def small_glow(i, col, n=16, frames=4):
    k = (0.45, 0.7, 0.95, 1.2)[i] if frames == 4 else (0.6, 1.1)[i]
    return dot_glow(n, col, core=1.6, halo=3.6 if n <= 16 else 7, k=k)


# =========================================================================== builders

def run(fn, jobs):
    with ProcessPoolExecutor(WORKERS) as pool:
        return list(pool.map(fn, jobs, chunksize=2))


def pivot_path():
    return (artkit.PIVOTS if bu.claimed(ROOT_NAME) else ROOT / "build" / "art-held" / "pivots") / f"{ROOT_NAME}.json"


def update_pivots(section):
    """Merge one subject's entry into the pivot file (held with the frames)."""
    path = pivot_path()
    data = {}
    for p in (path, artkit.PIVOTS / f"{ROOT_NAME}.json", ROOT / "build" / "art-held" / "pivots" / f"{ROOT_NAME}.json"):
        if p.exists():
            data = json.loads(p.read_text(encoding="utf-8"))
            data.pop("source", None)
            break
    data.update(section)
    data["centre"] = {"screen": [CX, CY], "note": "the station's centre on the 480x540 field, a pixel corner; every "
                      "offset below is px from it (y down) unless a set says it is centred"}
    bu.write_pivots(ROOT_NAME, data)


def build_rings():
    jobs = [(ring, k) for ring in RINGS for k in range(n8(ring) + 1)]
    res = run(seg_job, jobs)
    frames = artkit.quantize_set([img for img, _ in res], SEG_COLOURS)
    rings = {}
    i = 0
    for ring, (r0, r1, n, mount, kind, count) in RINGS.items():
        m = n8(ring) + 1
        save_set(f"{ROOT_NAME}-segment-{ring}", frames[i:i + m])
        rl, dth = lamp_points(ring)
        rings[ring] = {"band": [r0, r1], "mount_radius": mount, "segments": n, "batteries": count, "battery": kind,
                       "n8": m - 1, "headings_deg": [round(h, 4) for h in seg_headings(ring)],
                       "frames": [{"size": list(frames[i + j].size), "offset": list(res[i + j][1])} for j in range(m)],
                       "lamps": {"radius": round(rl, 2), "angles_deg": [round(-dth, 3), round(dth, 3)],
                                 "sprite": f"{ROOT_NAME}-lamp-red"},
                       "arc": {"sprite": f"{ROOT_NAME}-arc-{ring}", "from_radius": r0 + 3, "to_radius": r1 - 3}}
        i += m
    save_set(f"{ROOT_NAME}-rim", [rim_overlay()], single=True)
    save_set(f"{ROOT_NAME}-shade", [shade_overlay()], single=True)
    save_set(f"{ROOT_NAME}-lamp-red", [dot_glow(9, RED_LAMP, core=1.1, halo=2.4)], single=True)
    for ring in RINGS:
        save_set(f"{ROOT_NAME}-arc-{ring}", [arc_strip(ring)], single=True)
    update_pivots({"rings": rings, "segment_rule": (
        "heading h (deg, cw from down): d = h mod 360, q = floor(d / 90), r = d - 90 q; r <= 45: frame round(r / 45 * "
        "n8), else frame round((90 - r) / 45 * n8) mirrored by (x, y) -> (-y, -x); then q quarter turns cw by (x, y) -> "
        "(-y, x); mirrored offset (-y0 - h, -x0 - w), turned offset (-y0 - h, x0), the size swapped each time"),
        "segment_of": "segment j of a ring at angle(ring) + j * 360 / segments; battery b on segment b * segments / "
                      "batteries",
        "locked_tint": LOCKED_TINT,
        "overlays": {"rim": {"sprite": f"{ROOT_NAME}-rim", "blend": "additive", "centred": True, "scale": 2},
                     "shade": {"sprite": f"{ROOT_NAME}-shade", "blend": "normal", "centred": True, "scale": 2},
                     "order": "base, segments (tinted when locked), lamps, shade, rim, mounts and turrets, core"}})


def build_base():
    tiles = run(base_tile, [(i, j) for j in range(BASE_TILES) for i in range(BASE_TILES)])
    t = BASE_SIZE // BASE_TILES
    hi = np.zeros((BASE_SIZE, BASE_SIZE, 4))
    for n, tile in enumerate(tiles):
        i, j = n % BASE_TILES, n // BASE_TILES
        hi[j * t:(j + 1) * t, i * t:(i + 1) * t] = tile
    img = sprite.sharpen(sprite.to_image(hi, 0.5), 80)
    img = silhouette_rim(artkit.quantize_set([img], 64)[0])
    save_set(f"{ROOT_NAME}-base", [img], single=True)
    bays = run(bay_job, [(k, u) for k in range(4) for u in (0.0, 0.33, 0.67, 1.0)])
    bay_frames = artkit.quantize_set([b for b, _ in bays], 32)
    for k in range(4):
        save_set(f"{ROOT_NAME}-bay-{k}", bay_frames[4 * k:4 * k + 4])
    save_set(f"{ROOT_NAME}-bay-glow", [small_glow(i, GOLD, n=32, frames=2) for i in range(2)])
    save_set(f"{ROOT_NAME}-lamp-white", [dot_glow(5, (255, 246, 220), core=0.9, halo=1.6)], single=True)
    save_set(f"{ROOT_NAME}-lamp-gold", [dot_glow(7, GOLD, core=1.0, halo=2.0)], single=True)
    update_pivots({"base": {"sprite": f"{ROOT_NAME}-base", "offset": [-BASE_SIZE // 2, -BASE_SIZE // 2],
                            "lamps": base_lamps(),
                            "lamp_sprites": {"white": f"{ROOT_NAME}-lamp-white", "gold": f"{ROOT_NAME}-lamp-gold"},
                            "note": "lamps centred on their points; the inner catwalk (100-123 px) lights with the "
                                    "inner ring, the outer (157-180) with the middle ring"},
                   "bays": [{"sprite": f"{ROOT_NAME}-bay-{k}", "heading_deg": bay_heading(k), "radius": BAY_R,
                             "centre": [round(v, 2) for v in screen_xy(BAY_R, bay_heading(k))],
                             "offset": list(bays[4 * k][1]), "frames": "0 shut, 1-2 opening, 3 open",
                             "glow": f"{ROOT_NAME}-bay-glow"} for k in range(4)]})


KINDS = ("sam", "rail", "laser")


def build_batteries():
    mjobs = [(kind, st) for kind in KINDS for st in ("dark", "gold", "red", "wreck")]
    mounts = run(mount_job, mjobs)
    for i, kind in enumerate(KINDS):
        m = mounts[4 * i:4 * i + 4]
        q = artkit.quantize_set(m, 32)
        save_set(f"{ROOT_NAME}-{kind}-mount", q[:3])
        save_set(f"{ROOT_NAME}-{kind}-wreck", q[3:], single=True)
    tjobs = [(kind, st, k) for kind, st in (("sam", "shut"), ("sam", "open"), ("rail", "shut"), ("laser", "shut"))
             for k in range(HEADINGS)]
    tur = run(turret_job, tjobs)
    sam = artkit.quantize_set(tur[:2 * HEADINGS], 36)
    save_set(f"{ROOT_NAME}-sam", sam[:HEADINGS])
    save_set(f"{ROOT_NAME}-sam-open", sam[HEADINGS:])
    save_set(f"{ROOT_NAME}-rail", artkit.quantize_set(tur[2 * HEADINGS:3 * HEADINGS], 40))
    save_set(f"{ROOT_NAME}-laser", artkit.quantize_set(tur[3 * HEADINGS:], 36))
    save_set(f"{ROOT_NAME}-rail-charge", [small_glow(i, (255, 196, 80)) for i in range(4)])
    save_set(f"{ROOT_NAME}-laser-lens", [small_glow(i, CRIMSON) for i in range(4)])
    update_pivots({"batteries": {kind: {
        "mount": f"{ROOT_NAME}-{kind}-mount", "mount_size": MOUNT[kind], "mount_frames": "0 dark, 1 gold, 2 red (armed)",
        "wreck": f"{ROOT_NAME}-{kind}-wreck", "turret": f"{ROOT_NAME}-{kind}", "turret_size": TURRET[kind][0],
        "headings": HEADINGS, "heading_rule": "k = round(turretRadians * 64 / 2 pi) mod 64 (cw from down)",
        "muzzle": [ahead(kind, MUZZLE[kind], k) for k in range(HEADINGS)],
        **({"open": f"{ROOT_NAME}-sam-open"} if kind == "sam" else {}),
        **({"coils": [ahead(kind, COILS, k) for k in range(HEADINGS)], "charge": f"{ROOT_NAME}-rail-charge"}
           if kind == "rail" else {}),
        **({"lens": f"{ROOT_NAME}-laser-lens"} if kind == "laser" else {}),
        "note": "every sprite centred on the battery's place on its ring; muzzle/coils/lens: px from that centre per "
                "turret heading"} for kind in KINDS}})


def centred_trim(frames):
    """Crop frames to a common square about their centre (the core stays centred on the station)."""
    half = 0
    for f in frames:
        box = f.getchannel("A").getbbox()
        if box:
            c = f.width // 2
            half = max(half, c - box[0], box[2] - c, c - box[1], box[3] - c)
    c = frames[0].width // 2
    return [f.crop((c - half, c - half, c + half, c + half)) for f in frames]


def build_core():
    frames = run(core_job, [("frame", i) for i in range(CORE_FRAMES)])
    frames = centred_trim(artkit.quantize_set(frames, 48))
    save_set(f"{ROOT_NAME}-core", frames)
    slits = centred_trim([artkit.additive(core_job(("slits", 0)))])
    save_set(f"{ROOT_NAME}-core-slits", slits, single=True)
    save_set(f"{ROOT_NAME}-core-glow", [core_glow(i) for i in range(4)])
    update_pivots({"core": {"sprite": f"{ROOT_NAME}-core", "centred": True, "frames": CORE_FRAMES,
                            "opening": "frame round(u * 11) over the 3 s opening (u 0..1); 11 open; backwards for the "
                                       "implosion", "slits": f"{ROOT_NAME}-core-slits",
                            "glow": f"{ROOT_NAME}-core-glow", "shield": "enemy-shield-medium (shared)"}})


def build_fx():
    save_set(f"{ROOT_NAME}-vent", [vent_frame(i) for i in range(8)])
    save_set(f"{ROOT_NAME}-cookoff", [cookoff_frame(i) for i in range(10)])
    save_set(f"{ROOT_NAME}-cookoff-debris", [debris_frame(i) for i in range(10)])
    save_set(f"{ROOT_NAME}-implosion", [implosion_frame(i) for i in range(12)])
    update_pivots({"fx": {"vent": "centred on a wrecked mount, 8 fps loop", "cookoff": "centred on the battery, 30 fps "
                          "(RING_COOKOFF), the debris over it", "implosion": "centred on the core, 15 fps",
                          "death_drift": "after the cook-offs the segments drift outward (14-22 px/s, growing) and "
                          "turn slowly (5-11 deg/s, alternate signs) by the segment rule, tinted, fading over 3.5 s"}})


BUILDERS = {"rings": build_rings, "base": build_base, "batteries": build_batteries, "core": build_core,
            "fx": build_fx}


# =========================================================================== sets (check, atlas share)

def expected_sets():
    sets = [(f"{ROOT_NAME}-segment-{ring}", n8(ring) + 1) for ring in RINGS]
    sets += [(f"{ROOT_NAME}-{s}", 1) for s in ("rim", "shade", "lamp-red", "base", "lamp-white", "lamp-gold",
                                                 "core-slits")]
    sets += [(f"{ROOT_NAME}-arc-{ring}", 1) for ring in RINGS]
    sets += [(f"{ROOT_NAME}-bay-{k}", 4) for k in range(4)] + [(f"{ROOT_NAME}-bay-glow", 2)]
    for kind in KINDS:
        sets += [(f"{ROOT_NAME}-{kind}-mount", 3), (f"{ROOT_NAME}-{kind}-wreck", 1), (f"{ROOT_NAME}-{kind}", HEADINGS)]
    sets += [(f"{ROOT_NAME}-sam-open", HEADINGS), (f"{ROOT_NAME}-rail-charge", 4), (f"{ROOT_NAME}-laser-lens", 4)]
    sets += [(f"{ROOT_NAME}-core", CORE_FRAMES), (f"{ROOT_NAME}-core-glow", 4), (f"{ROOT_NAME}-vent", 8),
             (f"{ROOT_NAME}-cookoff", 10), (f"{ROOT_NAME}-cookoff-debris", 10), (f"{ROOT_NAME}-implosion", 12)]
    return sets


def atlas_share():
    """The frames' atlas area with the packer's 2 px padding against one 2048 page."""
    total, n = 0, 0
    for name, _ in expected_sets():
        for f in load(name):
            total += (f.width + 2) * (f.height + 2)
            n += 1
    return n, total, total / 2048 ** 2


def check():
    from PIL import Image as _I
    bad = 0
    for name, count in expected_sets():
        folder = ROOT / where(name)
        files = sorted(folder.glob(f"{name}.png")) or sorted(
            p for p in folder.glob(f"{name}_*.png") if p.stem.rsplit("_", 1)[1].isdigit()
            and p.stem.rsplit("_", 1)[0] == name)
        ok = len(files) == count and all(_I.open(p).info.get(artkit.SOURCE_KEY) for p in files)
        size = _I.open(files[0]).size if files else None
        print(f"{'ok ' if ok else 'BAD'} {where(name)}/{name}: {len(files)}/{count} frames, {size}")
        bad += not ok
    if not pivot_path().exists():
        print(f"BAD pivots missing: {pivot_path()}")
        bad += 1
    n, total, share = atlas_share()
    print(f"{ROOT_NAME}: {n} frames, {total} px = {share:.2f} of a 2048x2048 atlas page")
    return bad == 0


# =========================================================================== intel silhouette

_FRAMES = {}


def L(name):
    """A written frame set, cached (the review composes hundreds of frames)."""
    if name not in _FRAMES:
        _FRAMES[name] = load(name)
    return _FRAMES[name]


def add_at(img, glow, xy):
    """``glow`` added onto ``img`` at ``xy`` (top left) as the game blends it, on the covered crop only."""
    x, y = int(xy[0]), int(xy[1])
    x0, y0 = max(0, x), max(0, y)
    x1, y1 = min(img.width, x + glow.width), min(img.height, y + glow.height)
    if x1 <= x0 or y1 <= y0:
        return img
    b = np.array(img.crop((x0, y0, x1, y1))).astype(np.int32)
    g = np.array(glow.crop((x0 - x, y0 - y, x1 - x, y1 - y))).astype(np.int32)
    b[..., :3] = np.minimum(255, b[..., :3] + g[..., :3] * g[..., 3:4] // 255)
    img.paste(Image.fromarray(b.astype(np.uint8), img.mode), (x0, y0))
    return img

_PIVOTS = {}


def pivots():
    if "data" not in _PIVOTS:
        _PIVOTS["data"] = json.loads(pivot_path().read_text(encoding="utf-8"))
    return _PIVOTS["data"]


def station_layer(angles, size=(480, 540), centre=(CX, CY), locked=(), lit=("outer", "middle", "inner"),
                  base=True, overlays=True, drift=0.0, mounts=True, aim=None, armed=None, dead=(), segs=None):
    """The station composed as the game would draw it (for the review and the intel mask): ``angles`` ring -> degrees
    (cw from down), ``locked`` rings tinted without lamps, ``dead`` (ring, b) batteries drawn as wrecks, ``drift`` s
    after the death's crack (segments drifting out and turning)."""
    piv = pivots()
    lay = Image.new("RGBA", size, (0, 0, 0, 0))
    cx, cy = centre
    if base:
        b = L(f"{ROOT_NAME}-base")[0]
        lay.alpha_composite(b, (cx - BASE_SIZE // 2, cy - BASE_SIZE // 2))
        if lit:
            for name, ring in (("inner", "inner"), ("outer", "middle")):
                if ring in lit and not set(locked) >= set(RINGS):
                    for kind in ("white", "gold"):
                        g = L(f"{ROOT_NAME}-lamp-{kind}")[0]
                        for x, y in piv["base"]["lamps"][name][kind]:
                            lay = add_at(lay, g, (round(cx + x - g.width / 2), round(cy + y - g.height / 2)))
        for k in range(4):
            bay = piv["bays"][k]
            fr = L(f"{ROOT_NAME}-bay-{k}")[0]
            lay.alpha_composite(fr, (cx + bay["offset"][0], cy + bay["offset"][1]))
    segs = segs or {ring: L(f"{ROOT_NAME}-segment-{ring}") for ring in RINGS}
    lamp = L(f"{ROOT_NAME}-lamp-red")[0]
    for ring, (r0, r1, n, mount, kind, count) in RINGS.items():
        info = piv["rings"][ring]
        for j in range(n):
            deg = angles[ring] + j * 360.0 / n
            ox = oy = 0.0
            if drift > 0:
                spin = drift * (5 + 3 * (j % 3)) * (1 if j % 2 else -1)
                dr = drift * (14 + 4 * ((j * 7) % 5)) + 6 * drift * drift
                ox, oy = screen_xy(dr, deg)
                deg += spin
            k, mir, q = seg_pose(ring, deg)
            img, (x0, y0) = place(segs[ring][k], info["frames"][k]["offset"], mir, q)
            if ring in locked or drift > 0:
                a = np.array(img).astype(np.float64)
                a[..., :3] *= LOCKED_TINT
                img = Image.fromarray(a.astype(np.uint8), "RGBA")
            lay.alpha_composite(img, (int(cx + x0 + round(ox)), int(cy + y0 + round(oy))))
            if ring in lit and ring not in locked and drift == 0:
                for da in info["lamps"]["angles_deg"]:
                    x, y = screen_xy(info["lamps"]["radius"], deg + da)
                    lay = add_at(lay, lamp, (round(cx + x - lamp.width / 2), round(cy + y - lamp.height / 2)))
    if overlays and drift == 0:
        sh = sprite.enlarge(L(f"{ROOT_NAME}-shade")[0], 2)
        lay.alpha_composite(sh, (cx - sh.width // 2, cy - sh.height // 2))
        lay = add_at(lay, sprite.enlarge(L(f"{ROOT_NAME}-rim")[0], 2), (cx - OVERLAY, cy - OVERLAY))
    if mounts and drift == 0:
        for ring, (r0, r1, n, mount, kind, count) in RINGS.items():
            for b in range(count):
                deg = angles[ring] + b * 360.0 / count
                x, y = screen_xy(mount, deg)
                px, py = cx + x, cy + y
                if (ring, b) in dead or ring in locked:
                    sprite.paste_center(lay, L(f"{ROOT_NAME}-{kind}-wreck")[0], px, py)
                    continue
                state = 0 if ring not in lit else (2 if armed and armed(ring, deg) else 1)
                sprite.paste_center(lay, L(f"{ROOT_NAME}-{kind}-mount")[state], px, py)
                hk = 0 if aim is None else int(round(aim(px, py) * HEADINGS / 360.0)) % HEADINGS
                sprite.paste_center(lay, L(f"{ROOT_NAME}-{kind}")[hk], px, py)
    return lay


def intel_mask():
    """The station at rest (rings, batteries, core; the base disc left out so the three rings read); an alpha mask
    (480x480) for tools/art/intel.py."""
    lay = station_layer({"outer": 0.0, "middle": 0.0, "inner": 0.0}, size=(480, 480), centre=(240, 240), base=False,
                        overlays=False)
    core = L(f"{ROOT_NAME}-core")[0]
    sprite.paste_center(lay, core, 240, 240)
    return np.array(lay)[..., 3] > 127


# =========================================================================== review

FPS = 15


def heading_to(px, py, sx, sy):
    """Screen heading (cw from down, degrees) from (px, py) toward (sx, sy)."""
    return float(np.degrees(np.arctan2(-(sx - px), sy - py))) % 360.0


def plate():
    bd = ROOT / "assets" / "backdrop" / "level-35"
    cands = sorted(bd.glob("*.png")) if bd.exists() else []
    if cands:
        img = Image.open(cands[0]).convert("RGBA")
        if img.width >= 480 and img.height >= 540:
            return img.crop((0, 0, 480, 540)), "LEVEL 35 BACKDROP"
    return b60.ceres_bg(), "ROUND-60 CERES STAND-IN"


def arc_band(img, bearing, rings, arc=120.0, centre=(CX, CY)):
    """The fire arc on the hulls: each live ring's strip tiled round the sector (as the game's band of quads)."""
    w, h = img.size
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    x, y = xx - centre[0], yy - centre[1]
    r = np.hypot(x, y)
    head = np.degrees(np.arctan2(-x, y)) % 360
    near = np.abs((head - bearing + 180) % 360 - 180)
    out = img
    for ring in rings:
        r0, r1 = RINGS[ring][:2]
        strip = np.array(L(f"{ROOT_NAME}-arc-{ring}")[0]).astype(np.float64)
        col = np.clip((r - (r0 + 3)).astype(int), 0, strip.shape[1] - 1)
        sel = (r >= r0 + 3) & (r < r1 - 3) & (near <= arc / 2)
        endk = sel & (near > arc / 2 - 1.2 * 180 / np.pi / np.maximum(r, 1))
        layer = np.zeros((h, w, 4))
        layer[sel, :3] = strip[0, col[sel], :3]
        layer[endk, :3] = strip[0, 0, :3]
        layer[sel, 3] = 255
        out = artkit.add_light(out, Image.fromarray(layer.astype(np.uint8), "RGBA"))
    return out


def clamp_bearing(sx, sy):
    b = heading_to(CX, CY, sx, sy)
    b = (b + 180) % 360 - 180
    return float(np.clip(b, -60, 60)) % 360


def in_arc(deg, bearing, arc=120.0):
    return abs((deg - bearing + 180) % 360 - 180) <= arc / 2


def ship_img():
    return Image.open(artkit.SPRITES / "ship_2.png").convert("RGBA")


SHIP = None


def ship_xy(t):
    if t < 12.0:
        return 240 + 120 * np.sin(0.45 * t), 455.0
    return 240 + 90 * np.sin(0.6 * t), 450.0


class Timeline:
    """The review GIF: intro 0-3 s (lights ring by ring, spin-up), phase 1 3-9 s, the outer ring's lock 9-12 s, cut to
    the core opening 12-17 s, cut to the death 17-22 s."""
    DUR = 22.0
    SPEED = {"outer": 6.0, "middle": -10.0}
    KILLS = [9.0, 9.35, 9.7, 10.05, 10.4, 10.75]

    def __init__(self):
        dt = 1.0 / FPS
        self.ang = {r: [0.0] for r in RINGS}
        for i in range(1, int(self.DUR * FPS) + 2):
            t = i * dt
            for ring in RINGS:
                w = self.omega(ring, t)
                self.ang[ring].append(self.ang[ring][-1] + w * dt)

    def omega(self, ring, t):
        if t >= 12.0:
            return 0.0
        spin = np.clip(t / 3.0, 0, 1)
        if ring == "inner":
            c = (t - 3.0) % 8.0 if t >= 3.0 else -1
            return 30.0 if 0 <= c < 4.0 else 0.0
        if ring == "outer" and t >= self.KILLS[-1]:
            return self.SPEED["outer"] * max(0.0, 1 - (t - self.KILLS[-1]) / 2.0)
        fast = 1.25 if ring == "middle" and t >= self.KILLS[-1] else 1.0
        return self.SPEED[ring] * spin * fast

    def angles(self, t):
        i = min(int(round(t * FPS)), len(self.ang["outer"]) - 1)
        return {r: self.ang[r][i] for r in RINGS}


def review_frame(bg, tl, t, segs):
    f = bg.copy()
    ang = tl.angles(t)
    sx, sy = ship_xy(t)
    lit = tuple(r for r, t0 in (("outer", 0.3), ("middle", 1.0), ("inner", 1.7)) if t >= t0)
    if t >= 12.0:
        lit = ()
    locked = ("outer",) if t >= tl.KILLS[-1] else ()
    if t >= 12.0:
        locked = tuple(RINGS)
    dead = set()
    for b, tk in enumerate(tl.KILLS):
        if t >= tk:
            dead.add(("outer", b))
    bearing = clamp_bearing(sx, sy)
    armed = (lambda ring, deg: t >= 3.0 and in_arc(deg, bearing)) if t < 12.0 else None
    aim = lambda px, py: heading_to(px, py, sx, sy)  # noqa: E731
    drift = max(0.0, t - 18.4) if t >= 18.4 else 0.0
    lay = station_layer(ang, locked=locked, lit=lit, dead=dead, armed=armed, aim=aim, drift=drift, segs=segs,
                        base=t < 18.4 or True)
    if drift > 0:
        lay = e60.fade(lay, max(0.0, 1 - drift / 3.5))
    f.alpha_composite(lay)
    if 3.0 <= t < 12.0:
        f = arc_band(f, bearing, [r for r in RINGS if r not in locked], arc=120.0 * min(1.0, (t - 3.0) / 0.6))
    # the batteries' tells: a SAM's hatch, a rail's charge, the laser lens in the inner ring's hold
    if t < 12.0 and t >= 3.0:
        for ring, (r0, r1, n, mount, kind, count) in RINGS.items():
            for b in range(count):
                deg = ang[ring] + b * 360.0 / count
                if (ring, b) in dead or not in_arc(deg, bearing):
                    continue
                x, y = screen_xy(mount, deg)
                px, py = CX + x, CY + y
                hk = int(round(heading_to(px, py, sx, sy) * HEADINGS / 360.0)) % HEADINGS
                if kind == "sam" and int(t * 2) % 5 == 0:
                    sprite.paste_center(f, L(f"{ROOT_NAME}-sam-open")[hk], px, py)
                elif kind == "rail" and 5.0 <= t % 6 + 3 < 6.5:
                    piv = pivots()["batteries"]["rail"]["coils"][hk]
                    g = L(f"{ROOT_NAME}-rail-charge")[min(3, int((t % 1) * 4))]
                    f = add_at(f, g, (round(px + piv[0] - 8), round(py + piv[1] - 8)))
                elif kind == "laser" and tl.omega("inner", t) == 0.0:
                    piv = pivots()["batteries"]["laser"]["muzzle"][hk]
                    g = L(f"{ROOT_NAME}-laser-lens")[2 + int(t * 6) % 2]
                    f = add_at(f, g, (round(px + piv[0] - 8), round(py + piv[1] - 8)))
    # cook-offs of the outer ring's kills
    for b, tk in enumerate(tl.KILLS):
        s = t - tk
        if 0 <= s < 10 / 30:
            deg = ang["outer"] + b * 60.0
            x, y = screen_xy(200, deg)
            i = int(s * 30)
            sprite.paste_center(f, L(f"{ROOT_NAME}-cookoff-debris")[i], CX + x, CY + y)
            g = L(f"{ROOT_NAME}-cookoff")[i]
            f = add_at(f, g, (round(CX + x - 32), round(CY + y - 32)))
    # vents on the locked ring's wrecks
    if tl.KILLS[-1] <= t < 18.4:
        for b in range(6):
            deg = ang["outer"] + b * 60.0
            x, y = screen_xy(200, deg)
            v = L(f"{ROOT_NAME}-vent")[int(t * 8 + 3 * b) % 8]
            sprite.paste_center(f, v, CX + x + 4, CY + y - 4)
    # the core
    core = L(f"{ROOT_NAME}-core")
    u = 0.0
    if t >= 12.5:
        u = min(1.0, (t - 12.5) / 3.0)
    if t >= 20.2:
        u = max(0.0, 1 - (t - 20.2) / 0.5)
    if t < 20.8:
        ci = int(round(u * (CORE_FRAMES - 1)))
        sprite.paste_center(f, core[ci], CX, CY)
        if t < 12.0 and int(t * 2) % 6 == 5:
            sl = L(f"{ROOT_NAME}-core-slits")[0]
            f = add_at(f, sl, (CX - sl.width // 2, CY - sl.height // 2))
        if ci == CORE_FRAMES - 1 or t >= 20.2:
            gi = 2 + int(t * 8) % 2 if 16.6 <= t < 17.2 or t >= 20.0 else int(t * 2) % 2
            f = add_at(f, L(f"{ROOT_NAME}-core-glow")[gi], (CX - 48, CY - 48))
        if 15.5 <= t < 18.4:
            sh = L("enemy-shield-medium")[int(t * 10 / 6) % 4]
            f = add_at(f, sh, (CX - sh.width // 2, CY - sh.height // 2))
    # a bay opening (phase 4)
    if 16.0 <= t < 18.4:
        k = 0
        bay = pivots()["bays"][k]
        st = min(3, int((t - 16.0) / 0.5 * 3))
        f.alpha_composite(L(f"{ROOT_NAME}-bay-{k}")[st], (CX + bay["offset"][0], CY + bay["offset"][1]))
        g = L(f"{ROOT_NAME}-bay-glow")[0 if t < 16.5 else 1]
        f = add_at(f, g, (round(CX + bay["centre"][0] - 16), round(CY + bay["centre"][1] - 16)))
    # the death: cook-offs ring by ring 0.15 s apart, the implosion
    if 18.0 <= t < 21.0:
        order = [(ring, b) for ring in RINGS for b in range(RINGS[ring][5])]
        for n_, (ring, b) in enumerate(order):
            s = t - (18.0 + 0.15 * n_)
            if 0 <= s < 10 / 30:
                deg = ang[ring] + b * 360.0 / RINGS[ring][5]
                x, y = screen_xy(RINGS[ring][3] + max(0, t - 18.4) * 16, deg)
                i = int(s * 30)
                sprite.paste_center(f, L(f"{ROOT_NAME}-cookoff-debris")[i], CX + x, CY + y)
                f = add_at(f, L(f"{ROOT_NAME}-cookoff")[i], (round(CX + x - 32), round(CY + y - 32)))
    if 20.2 <= t < 21.0:
        i = min(11, int((t - 20.2) * 15))
        f = add_at(f, L(f"{ROOT_NAME}-implosion")[i], (CX - 48, CY - 48))
    if 20.6 <= t < 21.6:
        big = L("explosion-vacuum-large")[min(13, int((t - 20.6) * 14))]
        f = artkit.add_light(f, big.resize((big.width * 2, big.height * 2), Image.NEAREST), (CX - 96, CY - 96))
    sprite.paste_center(f, SHIP, sx, sy)
    if 20.6 <= t < 21.2:
        fl = Image.new("RGBA", f.size, (255, 250, 236, int(220 * (1 - (t - 20.6) / 0.6))))
        f.alpha_composite(fl)
    cap = [(0, 3, "INTRO: LIGHTS RING BY RING, SPIN-UP"), (3, 9, "PHASE 1: THE ARC FOLLOWS THE SHIP"),
           (9, 12, "THE OUTER RING LOCKS: GRIND, DARK, VENTING"), (12, 18, "CUT: THE CORE OPENS (HEX SHIELD, A BAY)"),
           (18, 22, "CUT: THE DEATH: COOK-OFFS, DRIFT, IMPLOSION")]
    for t0, t1, text in cap:
        if t0 <= t < t1:
            b60.cap_at(f, text)
    return f.convert("RGB")


def _gif_frame(t):
    return review_frame(_GIF["bg"], _GIF["tl"], t, _GIF["segs"])


_GIF = {}


def seam_crops(segs):
    """The joins at 1:1 and 4x at several angles of each ring, and the rule against direct renders."""
    cells, labels = [], []
    piv = pivots()
    for ring, angs in (("outer", (0.0, 7.3, 22.6, 44.8, 131.0)), ("middle", (3.1, 45.4, 200.0)),
                       ("inner", (0.0, 61.7, 300.2))):
        n = RINGS[ring][2]
        for a in angs:
            lay = Image.new("RGBA", (480, 480), (0, 0, 0, 0))
            for j in range(n):
                k, mir, q = seg_pose(ring, a + j * 360.0 / n)
                img, (x0, y0) = place(segs[ring][k], piv["rings"][ring]["frames"][k]["offset"], mir, q)
                lay.alpha_composite(img, (240 + x0, 240 + y0))
            seam = a + 180.0 / n                       # the join after segment 0
            x, y = screen_xy((RINGS[ring][0] + RINGS[ring][1]) / 2, seam)
            box = (int(240 + x - 24), int(240 + y - 24), int(240 + x + 24), int(240 + y + 24))
            crop = artkit.checker(48, 48)
            crop.alpha_composite(lay.crop(box))
            cell = Image.new("RGBA", (48 + 6 + 192, 192), raster.SHEET_BG)
            cell.alpha_composite(crop, (0, 0))
            cell.alpha_composite(sprite.enlarge(crop, 4), (54, 0))
            cells.append(cell)
            labels.append(f"{ring.upper()} AT {a:.1f}: JOIN 1X, 4X")
    return cells, labels


def rule_check(segs):
    """Direct top-lit renders at headings outside 0-45 against the rule's turned frame: the differing pixels."""
    piv = pivots()
    rows = []
    on_frame = lambda ring, q, k, mir: 90.0 * q + (90.0 - 45.0 * k / n8(ring) if mir else 45.0 * k / n8(ring))  # noqa: E731
    for ring, deg in (("outer", on_frame("outer", 1, 22, False)), ("middle", on_frame("middle", 2, 47, True)),
                      ("inner", on_frame("inner", 3, 27, True))):
        ref, off = direct_render(ring, deg)
        k, mir, q = seg_pose(ring, deg)
        img, (x0, y0) = place(segs[ring][k], piv["rings"][ring]["frames"][k]["offset"], mir, q)
        lay_a = Image.new("RGBA", (480, 480), (0, 0, 0, 0))
        lay_b = Image.new("RGBA", (480, 480), (0, 0, 0, 0))
        lay_a.alpha_composite(ref, (240 + off[0], 240 + off[1]))
        lay_b.alpha_composite(img, (240 + x0, 240 + y0))
        a, b = np.array(lay_a).astype(int), np.array(lay_b).astype(int)
        shape = int(((a[..., 3] > 0) != (b[..., 3] > 0)).sum())
        both = (a[..., 3] > 0) & (b[..., 3] > 0)
        diff = float(np.abs(a[both, :3] - b[both, :3]).mean()) if both.any() else 0.0
        box = lay_b.getchannel("A").getbbox()
        rows.append((ring, deg, k, mir, q, shape, diff, lay_a.crop(box), lay_b.crop(box)))
    return rows


def direct_render(ring, deg):
    """A segment rendered straight at ``deg`` (outside the set), quantised to the written palette."""
    r0, r1, n = RINGS[ring][:3]
    h = np.radians(deg)
    mid = (r0 + r1) / 2
    cx, cy = int(round(-mid * np.sin(h))), int(round(mid * np.cos(h)))
    w = seg_window(ring, deg % 90 if deg % 90 <= 45 else 90 - deg % 90)[2] + 8
    ext = w / PX
    c = np.array((cx / PX, -cy / PX, 0.0))
    key = (np.array((0.0, 0.0, LIGHT_H)) - c) / (ext / 2.3)
    phi = np.arctan2(-np.cos(h), -np.sin(h))
    base = b60.segment_scene("b", ring)
    scene = lambda p: base(sdf.rotate_z(p, phi))  # noqa: E731
    sdf.env_color = _sym_env
    try:
        hi = sdf.render(scene, model_space_materials(station_mats(), phi), (w * 4, w * 4), ext, center=(c[0], c[1]),
                        key_pos=key, fill=0.0, exposure=SEG_EXPOSURE)
    finally:
        sdf.env_color = _ENV
    img = artkit.native(hi, 4, 80)
    pal = L(f"{ROOT_NAME}-segment-{ring}")
    img = artkit.quantize_set(pal + [img], SEG_COLOURS)[-1]
    box = img.getchannel("A").getbbox()
    return img.crop(box), (cx - w // 2 + box[0], cy - w // 2 + box[1])


def strip_row(frames, zoom, glow=False, every=1, width=1240):
    cells = [artkit.on_background(f, zoom, glow) for f in frames[::every]]
    h = max(c.height for c in cells)
    img = Image.new("RGBA", (width, h), raster.SHEET_BG)
    x = 0
    for c in cells:
        if x + c.width > width:
            break
        img.alpha_composite(c, (x, 0))
        x += c.width + 4
    return img


def review():
    global SHIP
    SHIP = ship_img()
    bg, plate_name = plate()
    segs = {ring: L(f"{ROOT_NAME}-segment-{ring}") for ring in RINGS}
    tl = Timeline()
    W = 1300
    blocks = []

    def block(title, img):
        blocks.append((title, img))

    rest = bg.copy()
    rest.alpha_composite(station_layer({r: 0.0 for r in RINGS}, segs=segs, armed=lambda r, d: in_arc(d, 0.0),
                                       aim=lambda px, py: heading_to(px, py, 240, 455)))
    sprite.paste_center(rest, L(f"{ROOT_NAME}-core")[0], CX, CY)
    p1 = review_frame(bg, tl, 6.0, segs).convert("RGBA")
    top = Image.new("RGBA", (480 * 2 + 12, 540), raster.SHEET_BG)
    top.alpha_composite(rest, (0, 0))
    top.alpha_composite(p1, (492, 0))
    block(f"THE STATION AT REST AND IN PHASE 1 (1X, OVER THE {plate_name}): BASE, SEGMENTS BY THE RULE, LAMPS, SHADE "
          "AND RIM OVERLAYS, MOUNTS AND TURRETS, CORE", top)
    for ring in RINGS:
        block(f"SEGMENTS {ring.upper()} ({len(segs[ring])} FRAMES, 0-45 DEG, EVERY 8TH, 1X)",
              strip_row(segs[ring], 1, every=8))
    cells, labels = seam_crops(segs)
    block("SEAMS AT 1:1 AND 4X (THE JOIN AFTER SEGMENT 0, THE RING AT SEVERAL ANGLES, COMPOSED BY THE RULE)",
          b60.panel_grid(cells, 4, labels=labels))
    rows = rule_check(segs)
    rc = [artkit.on_background(x, 2) for row in rows for x in row[7:9]]
    rl = []
    for ring, deg, k, mir, q, shape, diff, _, _ in rows:
        rl += [f"{ring.upper()} {deg:.1f} DIRECT", f"RULE: F{k}{' MIRROR' if mir else ''} +{q}X90: {shape} PX "
                                                  f"SHAPE, {diff:.1f} MEAN"]
    block("THE RULE AGAINST DIRECT TOP-LIT RENDERS AT HEADINGS OUTSIDE 0-45 THAT FALL ON A FRAME (2X; SHAPE = PIXELS "
          "IN ONE ONLY, MEAN = COLOUR DIFFERENCE OVER BOTH, OF 255)",
          b60.panel_grid(rc, 6, labels=rl))
    bat = []
    blab = []
    for kind in KINDS:
        for i, s in enumerate(("DARK", "GOLD", "ARMED")):
            bat.append(artkit.on_background(L(f"{ROOT_NAME}-{kind}-mount")[i], 2))
            blab.append(f"{kind.upper()} MOUNT {s}")
        bat.append(artkit.on_background(L(f"{ROOT_NAME}-{kind}-wreck")[0], 2))
        blab.append(f"{kind.upper()} WRECK")
    block("BATTERY MOUNTS (2X): THE COLLAR'S LAMPS DARK, GOLD, RED IN THE ARC; THE WRECKS", b60.panel_grid(bat, 12,
                                                                                                       labels=blab))
    for name in ("sam", "sam-open", "rail", "laser"):
        fr = L(f"{ROOT_NAME}-{name}")
        mounted = []
        kind = name.split("-")[0]
        for k in range(0, HEADINGS, 4):
            m = L(f"{ROOT_NAME}-{kind}-mount")[1].copy()
            c = Image.new("RGBA", fr[k].size, (0, 0, 0, 0))
            sprite.paste_center(c, m, c.width / 2, c.height / 2)
            c.alpha_composite(fr[k])
            mounted.append(c)
        block(f"{name.upper()} TURRET ON ITS MOUNT, EVERY 4TH OF 64 HEADINGS (2X)", strip_row(mounted, 2))
    glows = [L(f"{ROOT_NAME}-rail-charge")[i] for i in range(4)] + [L(f"{ROOT_NAME}-laser-lens")[i]
                                                                        for i in range(4)]
    block("RAIL CHARGE, LASER LENS (ADDITIVE, 3X)", strip_row(glows, 3, glow=True))
    core = L(f"{ROOT_NAME}-core")
    block("THE CORE (1X): SHUT, OPENING (3 S), OPEN", strip_row(core, 1))
    cs = []
    shut = core[0].copy()
    cs.append(artkit.on_background(shut, 2))
    sl = artkit.add_light(Image.new("RGBA", shut.size, artkit.PLATE), shut)
    s2 = Image.new("RGBA", shut.size, artkit.PLATE)
    s2.alpha_composite(shut)
    sll = L(f"{ROOT_NAME}-core-slits")[0]
    s2 = add_at(s2, sll, ((shut.width - sll.width) // 2, (shut.height - sll.height) // 2))
    cs.append(sprite.enlarge(s2, 2))
    op = Image.new("RGBA", (128, 128), artkit.PLATE)
    sprite.paste_center(op, core[-1], 64, 64)
    op = add_at(op, L(f"{ROOT_NAME}-core-glow")[1], (16, 16))
    op2 = add_at(op, L("enemy-shield-medium")[0], (18, 18))
    cs += [sprite.enlarge(op, 2), sprite.enlarge(op2, 2)]
    del sl
    block("THE CORE (2X): SHUT, THE FAN'S TELL (SLITS), OPEN WITH ITS GLOW, UNDER THE SHARED HEX SHIELD (MEDIUM)",
          b60.panel_grid(cs, 4))
    fx = []
    for name, glow in (("cookoff", True), ("cookoff-debris", False), ("vent", False), ("implosion", True)):
        fx.append((name, strip_row(L(f"{ROOT_NAME}-{name}"), 1 if name == "implosion" else 2, glow=glow)))
    for name, img in fx:
        block(f"{name.upper()} ({'ADDITIVE' if name in ('cookoff', 'implosion') else 'NORMAL'})", img)
    bays = [artkit.on_background(L(f"{ROOT_NAME}-bay-{k}")[i], 3) for k in range(4) for i in range(4)]
    block("RIM BAYS (3X): EACH BAY'S DOORS SHUT, OPENING, OPEN", b60.panel_grid(bays, 16))
    # the locked look and readability crops
    lk = bg.copy()
    ang = {"outer": 17.0, "middle": 40.0, "inner": 0.0}
    lk.alpha_composite(station_layer(ang, segs=segs, locked=("outer",), dead={("outer", b) for b in range(6)},
                                     armed=lambda r, d: in_arc(d, 20.0), aim=lambda px, py: heading_to(px, py, 300, 450)))
    for b in range(6):
        x, y = screen_xy(200, ang["outer"] + b * 60)
        sprite.paste_center(lk, L(f"{ROOT_NAME}-vent")[(3 * b) % 8], CX + x + 4, CY + y - 4)
    lk = arc_band(lk, 20.0, ["middle", "inner"])
    crops = [lk.crop((40, 200, 240, 400)), lk.crop((260, 180, 460, 380)), p1.crop((140, 240, 340, 440))]
    block("THE LOCKED OUTER RING (TINTED, LAMPS OUT, WRECKS VENTING) AND THE ARC'S BAND, 2X CROPS; LAST: PHASE 1, "
          "AN ARMED BATTERY OVER THE BACKDROP", b60.panel_grid([sprite.enlarge(c, 2) for c in crops], 3))
    n, total, share = atlas_share()
    notes = [f"ATLAS: {n} FRAMES, {total / 1e6:.2f} MPX WITH 2 PX PADDING = {share:.2f} OF A 2048X2048 PAGE "
             f"(TARGET ABOUT 0.6; A.23 TOP-LIT)",
             f"SEGMENTS: OUTER {n8('outer') + 1}, MIDDLE {n8('middle') + 1}, INNER {n8('inner') + 1} FRAMES OVER 0-45 DEG "
             "(<= 2 PX A STEP AT THE RIM); THE REST BY QUARTER TURNS AND A MIRROR, EXACT ON THE PIXEL GRID",
             "A LOCKED RING IS ITS SEGMENTS TINTED X0.62 WITHOUT LAMPS; THE OVERLAYS (SHADE, RIM) STAY: THE SUN IS STATIC"]
    heights = [24 + img.height + 10 for _, img in blocks]
    sheet = raster.sheet(W, 40 + sum(heights) + 16 * len(notes) + 20, "IRON SOVEREIGN - PRODUCTION (A.17 B, Q.11 B, "
                         "A.23 TOP-LIT)", f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 38
    for (title, img), h in zip(blocks, heights):
        raster.draw_text(sheet, 16, y, title, raster.LABEL)
        sheet.alpha_composite(img.convert("RGBA"), (16, y + 14))
        y += h
    for line in notes:
        raster.draw_text(sheet, 16, y, line, raster.LABEL)
        y += 16
    _GIF.update(bg=bg, tl=tl, segs=segs)
    times = [i / FPS for i in range(int(Timeline.DUR * FPS))]
    gif = [_gif_frame(t) for t in times]
    artkit.REVIEW_ROUND = ROUND
    artkit.save_review(sheet, gif, CONCEPT, ROOT_NAME, fps=FPS)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if not a.startswith("--")] or list(BUILDERS)
    unknown = set(subjects) - set(BUILDERS)
    if unknown:
        sys.exit(f"unknown subjects: {', '.join(sorted(unknown))}")
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    review()
    n, total, share = atlas_share()
    print(f"{ROOT_NAME}: {n} frames, {total} px = {share:.2f} of a 2048x2048 atlas page")
