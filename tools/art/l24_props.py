#!/usr/bin/env python3
"""Production art: Level 24's props (design/campaign/act-4-deep-water/level-24-abyssal-plain, "Ground targets",
"Secrets and pickups", "Layout"; M8 part D batch, concept round 53, step ART-D; straight to production from the
round-48 mood still `europa-abyssal-plain-r48-a` (tools/concept/europa_r48.py ``plain_still``: the grey beacon mast
head with its red-gold lamp, the warm lamps behind) and readability rule 7 (the light rim) for the secret).

The relay beacon and the grow-lamp pylon are rendered here and laid by tools/art/backdrop_l24.py as backdrop pieces
(``relay-beacon``, ``relay-beacon-glow``, ``lamp-pylon-*``, written into assets/backdrop/level-24/ by that script);
this script writes the sprites:

  meridian-wreck_0..2       80x48, the secret: the wrecked Thera Deep survey sub *Meridian* lying on its side on the
                            silt, nose to the upper right (a 20 HP `ground` trigger; the data's `sprite: meridian-wreck`,
                            `size: [80, 48]`): 0 lying silted (the city's orange hull gone dull, a white band, the dark
                            viewport, the sail, the dorsal hatch shut, a torn propeller shroud), 1 hit (scorched, a
                            dent over the hatch), 2 opened (the hatch blown off and lying beside the hull, the hold
                            dark and empty: the hidden crate rises from it as the pickup); rule 7's light rim
  meridian-wreck-glow_0..7  80x48, additive, aligned: 0-3 the sub's failing amber hull lamp (a tired, uneven blink,
                            until the first hit), 4-7 sparks flickering at the dent (after it), GroundGlow's halves
  scuttler-silt_0..7        32x32, normal, stepped translucency: the silt a Scuttler's stride kicks up on the sea floor
                            (a low grey-green cloud spreading and settling, no bubbles), played once per stride at the
                            foot (8 frames, 16 fps); claimed by the ``scuttler`` root
  design/campaign/act-4-deep-water/level-24-abyssal-plain/concept/l24-props-final-r53-a.png/.gif   the review sheet
                            and GIF (on the plain under the review grade of section 3: a beacon pulsing with its glow,
                            the wreck shot open and its crate rising, a Scuttler striding with its silt puffs)

Functions used by backdrop_l24.py: ``beacon_body()``, ``beacon_glow(k)`` (the 16-frame 2 s pulse), ``pylon_head()``.

Run: python3 tools/art/l24_props.py [wreck] [silt] [--review]   (~1 min; no subject = all; --review rebuilds only the
review from the written files; run backdrop_l24.py first: the review shows its beacon frames)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import l24kit as kit
from artkit import DESIGN, ROOT, TAU

import ground_targets as gt  # noqa: E402  (concept script, unchanged: the light rim)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

SCRIPT = "l24_props.py"
BATCH = "M8 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r53"
artkit.REVIEW_ROUND = ROUND
LEVEL = DESIGN / "campaign" / "act-4-deep-water" / "level-24-abyssal-plain"
CONCEPT = LEVEL / "concept"
BACKDROP = ROOT / "assets" / "backdrop" / "level-24"
WRECK = (80, 48)
SILT = 32
AMBER = np.array((255, 170, 50), float)
GOLD = np.array((255, 196, 64), float)
RED = np.array((220, 48, 36), float)
WARM = np.array((255, 226, 170), float)

# The beacon: a 128x192 canvas, the mast's foot at BEACON_BASE, the lamp at BEACON_LAMP (the mast leans toward the
# camera's top, as Level 23's probe mast: a tall thing seen from a little south of overhead).
BEACON = (128, 192)
BEACON_BASE = (64, 112)
BEACON_LAMP = (64, 50)
PULSE_FRAMES, PULSE_FPS = 16, 8.0                     # the 2 s pulse loop
PULSE = [1.0, 0.78, 0.55, 0.38, 0.26, 0.18, 0.13, 0.1] + [0.08] * 8
PYLON = (112, 144)
PYLON_BASE = (56, 88)
PYLON_LAMPS = [(-13, 0), (13, 0), (0, -13), (0, 13)]     # the four lamp pods round the head (model px)


def render(scene, mats, size, z_top=None, steps=150):
    hi, f = artkit.render_hi(scene, mats, size, float(size[0]), z_top=float(z_top or max(size)), steps=steps)
    return artkit.native(hi, f, crisp=60)


def rimmed(img):
    return Image.fromarray(np.clip(gt.light_rim(np.array(img).astype(np.float64)), 0, 255).astype(np.uint8), "RGBA")


def _model(canvas, px, py):
    """Canvas px -> model (x, y) (model y up, origin at the canvas centre)."""
    return px - canvas[0] / 2, canvas[1] / 2 - py


# =========================================================================== the relay beacon (a backdrop piece)

def beacon_mats():
    return [Material((0.46, 0.48, 0.52), metal=0.6, shininess=60, spec=0.7),     # 0 the grey mast, head
            Material((0.2, 0.21, 0.23), metal=0.5, shininess=30, spec=0.4),      # 1 dark fittings, cables
            Material((0.62, 0.6, 0.55), metal=0.1, shininess=15, spec=0.2),      # 2 the concrete collar
            Material((0.5, 0.08, 0.06), metal=0.2, shininess=120, spec=1.2),     # 3 the lamp's red glass
            Material((0.85, 0.62, 0.12), metal=0.3, shininess=50, spec=0.6)]     # 4 hazard bands (amber)


def beacon_scene():
    bx, by = _model(BEACON, *BEACON_BASE)
    lx, ly = _model(BEACON, *BEACON_LAMP)
    top = 46.0
    lean = (ly - by)                                      # the mast's lean toward the camera's top, per its height

    def along(u):
        return (bx, by + lean * u, top * u)

    def sc(p):
        items = [(sd_cylinder_z(p, (bx, by, 1.2), 9.0, 1.2), 2),
                 (sd_capsule(p, along(0.0), along(1.0), 3.0, 2.0), 0)]
        for u in (0.3, 0.62):                                       # hazard bands
            items.append((sd_capsule(p, along(u - 0.03), along(u + 0.03), 3.3, 3.1), 4))
        hx, hy, hz = along(1.0)
        items.append((sd_box(p, (hx, hy, hz), (5.2, 4.2, 2.8), 0.7), 0))
        items.append((sd_capsule(p, (hx - 12, hy, hz - 0.5), (hx + 12, hy, hz - 0.5), 1.0), 1))   # the cross-arm
        for s in (-1, 1):
            items.append((sd_box(p, (hx + s * 12, hy, hz - 0.5), (1.6, 1.6, 1.6), 0.4), 0))     # relay pods
        items.append((sd_sphere(p, (hx, hy + 0.6, hz + 3.0), 2.6), 3))                         # the lamp
        items.append((sd_capsule(p, (hx + 2.5, hy - 1, hz + 2), (hx + 3.5, hy - 1 + lean * 0.18, hz + 9), 0.45), 1))
        ax, ay, az = along(0.66)
        for k in range(3):                                          # guy cables to three anchors
            a = k * TAU / 3 + 0.9
            items.append((sd_capsule(p, (ax, ay, az), (bx + 26 * np.cos(a), by + 26 * np.sin(a), 0.6), 0.35), 1))
            items.append((sd_box(p, (bx + 26 * np.cos(a), by + 26 * np.sin(a), 0.8), (1.6, 1.6, 0.8), 0.3), 1))
        return union(*items, k=0.4)
    return sc


@lru_cache(maxsize=None)
def _beacon_render():
    return render(beacon_scene(), beacon_mats(), BEACON, z_top=60)


def beacon_body(level=0.0):
    """The beacon (normal, graded with the ground): the lens tinted by the pulse level (red at rest, gold at the
    flash); the light itself is the additive ``beacon_glow``."""
    img = _beacon_render().copy()
    a = np.array(img).astype(np.float64)
    yy, xx = np.mgrid[0:BEACON[1], 0:BEACON[0]].astype(np.float64) + 0.5
    lx, ly = BEACON_LAMP
    r = np.hypot(xx - lx, yy - (ly - 1.5))
    lens = (r < 2.6) & (a[..., 3] > 0)
    col = RED * (1 - level) + GOLD * level
    a[..., :3] = np.where(lens[..., None], col * (0.55 + 0.45 * level) + 30 * level, a[..., :3])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def beacon_glow(k):
    """Frame k of the pulse (additive, ungraded): the lamp's core and halo, and its light on the floor round the mast's
    foot (the lamp stands over the foot; the lean is the view's), gold at the flash, a dim red at rest."""
    w, h = BEACON
    level = PULSE[k % PULSE_FRAMES]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    lx, ly = BEACON_LAMP[0], BEACON_LAMP[1] - 1.5
    bx, by = BEACON_BASE
    r = np.hypot(xx - lx, yy - ly)
    core = np.exp(-(r / 1.6) ** 2)
    halo = np.exp(-(r / (4 + 7 * level)) ** 2) * (0.25 + 0.5 * level)
    rf = np.hypot((xx - bx) / 1.0, (yy - by) / 0.8)
    pool = np.exp(-(rf / (12 + 20 * level)) ** 2) * 0.32 * level ** 1.5
    col = RED * (1 - level) + GOLD * level
    v = np.clip(core * (0.55 + 0.45 * level) + halo + pool, 0, 1)
    q = artkit.ordered_dither(v, 9)
    rgb = col[None, None] * q[..., None] + 90 * np.clip(core * level - 0.6, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the grow-lamp pylon (a backdrop piece)

def pylon_mats():
    return [Material((0.44, 0.46, 0.48), metal=0.6, shininess=50, spec=0.6),     # 0 the mast, the ring
            Material((0.18, 0.19, 0.2), metal=0.5, shininess=30, spec=0.4),      # 1 cables, fittings
            Material((0.52, 0.5, 0.46), metal=0.1, shininess=15, spec=0.2),      # 2 the footing
            Material((0.1, 0.1, 0.1), emission=(1.25, 1.05, 0.7))]               # 3 the lamp pods (warm)


def pylon_scene():
    bx, by = _model(PYLON, *PYLON_BASE)
    top, lean = 34.0, 40.0

    def along(u):
        return (bx, by + lean * u, top * u)

    def sc(p):
        items = [(sd_cylinder_z(p, (bx, by, 1.0), 7.0, 1.0), 2),
                 (sd_capsule(p, along(0.0), along(1.0), 2.0, 1.4), 0)]
        hx, hy, hz = along(1.0)
        ring = np.abs(np.hypot(p[:, 0] - hx, p[:, 1] - hy) - 13.0)
        items.append((np.hypot(ring, p[:, 2] - hz) - 1.1, 0))
        for dx, dy in PYLON_LAMPS:
            items.append((sd_capsule(p, (hx, hy, hz), (hx + dx, hy + dy, hz), 0.7), 0))
            items.append((sd_ellipsoid(p, (hx + dx, hy + dy, hz - 1.0), (2.6, 2.6, 1.6)), 3))
        ax, ay, az = along(0.6)
        for k in range(3):
            a = k * TAU / 3 + 0.3
            items.append((sd_capsule(p, (ax, ay, az), (bx + 22 * np.cos(a), by + 22 * np.sin(a), 0.5), 0.35), 1))
        return union(*items, k=0.3)
    return sc


def pylon_lamps_px():
    """The lamp pods' canvas positions (for the baked pool's centres)."""
    bx, by = PYLON_BASE
    return [(bx + dx, by - 40 + -dy) for dx, dy in PYLON_LAMPS]


@lru_cache(maxsize=None)
def pylon_head():
    return render(pylon_scene(), pylon_mats(), PYLON, z_top=50)


# =========================================================================== the Meridian wreck

WRECK_AXIS = (np.cos(0.42), np.sin(0.42))            # the hull lies nose to the upper right (model y up)


def wreck_mats(state):
    ax, ay = WRECK_AXIS
    band = lambda p, n: 1.0  # noqa: E731
    silt = lambda p, n: np.where(n[:, 2] > 0.55, 0.78, 1.0)                                     # noqa: E731
    if state:
        scorch = lambda p, n: np.where(np.hypot(p[:, 0] - 1, p[:, 1] - 3) < 8, 0.4, 1.0) * band(p, n) * silt(p, n)  # noqa: E731,E501
    else:
        scorch = lambda p, n: band(p, n) * silt(p, n)                                           # noqa: E731
    return [Material((0.84, 0.42, 0.1), metal=0.25, shininess=40, spec=0.5, pattern=scorch),   # 0 the city's orange
            Material((0.46, 0.47, 0.46), metal=0.6, shininess=50, spec=0.6),                   # 1 steel: sail, shroud
            Material((0.05, 0.08, 0.1), metal=0.6, shininess=120, spec=1.2),                   # 2 the viewport
            Material((0.36, 0.42, 0.38), metal=0.0, shininess=8, spec=0.1),                    # 3 silt drifts
            Material((0.04, 0.04, 0.05), metal=0.0, shininess=5, spec=0.0),                    # 4 the dark hold
            Material((0.66, 0.64, 0.6), metal=0.3, shininess=40, spec=0.5),                    # 5 the hatch
            Material((0.86, 0.86, 0.84), metal=0.15, shininess=40, spec=0.5, pattern=silt)]    # 6 white band, sail


def wreck_scene(state):
    ax, ay = WRECK_AXIS
    nx, ny = -ay, ax

    def at(u, v, z):
        return (ax * u + nx * v, ay * u + ny * v - 1.0, z)

    def sc(p):
        hull = sd_capsule(p, at(-24, 0, 5.5), at(19, 0, 5.5), 8.0, 7.0)
        items = []
        if state == 2:
            hull = subtract((hull, 0), sd_ellipsoid(p, at(-2, 1, 13.0), (5.0, 3.6, 4.0)))[0]
            items.append((sd_ellipsoid(p, at(-2, 1, 9.0), (4.2, 3.0, 3.0)), 4))
            items.append((sd_cylinder_z(p, at(-6, 14, 1.0), 4.0, 0.9), 5))          # the hatch, blown off
        elif state == 1:
            hull = subtract((hull, 0), sd_sphere(p, at(-1, 3, 14.5), 3.2))[0]
            items.append((sd_cylinder_z(p, at(-2, 1, 12.6), 3.6, 0.9), 5))
        else:
            items.append((sd_cylinder_z(p, at(-2, 1, 12.6), 3.6, 0.9), 5))
        items.append((hull, 0))
        items.append((sd_capsule(p, at(11, 0, 5.5), at(13.5, 0, 5.5), 8.1, 7.9), 6))    # the white band
        items.append((sd_sphere(p, at(21, 0, 6.5), 5.2), 2))                          # the viewport nose
        items.append((sd_box(p, at(5, -1.0, 14.0), (5.0, 2.0, 3.6), 1.0), 6))         # the sail
        for u in (-17, -11):                                                         # portholes
            items.append((sd_sphere(p, at(u, -6.2, 8.0), 1.3), 2))
        items.append((sd_cylinder_z(p, at(-28, 0, 5.0), 5.0, 3.0), 1))                # the shroud, torn
        items.append((sd_box(p, at(-28, 7, 4.0), (1.4, 2.5, 0.6), 0.3), 1))
        items.append((sd_ellipsoid(p, at(-7, -11, 0.5), (20.0, 6.0, 3.6)), 3))         # silt banked on the low side
        items.append((sd_ellipsoid(p, at(14, -9, 0.5), (10.0, 5.0, 2.6)), 3))
        return union(*items, k=0.8)
    return sc


def wreck_frame(state):
    return rimmed(render(wreck_scene(state), wreck_mats(state), WRECK, z_top=36))


def wreck_points():
    ax, ay = WRECK_AXIS
    nx, ny = -ay, ax

    def px(u, v):
        x, y = ax * u + nx * v, ay * u + ny * v - 1.0
        return WRECK[0] / 2 + x, WRECK[1] / 2 - y
    return {"lamp": px(6, -1.5), "dent": px(-1, 3), "hold": px(-2, 1)}


def wreck_glow(i):
    w, h = WRECK
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    pts = wreck_points()
    if i < 4:                                   # the failing hull lamp on the sail: uneven, tired
        on = [0.9, 0.15, 0.55, 0.0][i]
        mx, my = pts["lamp"]
        r = np.hypot(xx - mx, yy - my - 4)
        f = (np.exp(-(r / 1.3) ** 2) + np.exp(-(r / 5.0) ** 2) * 0.45) * on
        rgb = AMBER[None, None] * artkit.ordered_dither(np.clip(f, 0, 1), 7)[..., None]
    else:                                       # sparks at the dent after the hit
        rng = np.random.default_rng(2440 + i)
        mx, my = pts["dent"]
        f = np.zeros((h, w))
        for _ in range(int(rng.integers(2, 5))):
            sx, sy = mx + rng.normal(0, 2.5), my + rng.normal(0, 2.0)
            f += np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / 0.9) * rng.uniform(0.6, 1.0)
        f += np.exp(-((xx - mx) ** 2 + (yy - my) ** 2) / 18) * (0.35 if i % 2 == 0 else 0.15)
        rgb = np.array((200, 230, 255.0))[None, None] * artkit.ordered_dither(np.clip(f, 0, 1), 7)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the Scuttler's silt puff

def silt_frame(i):
    """A low cloud of silt spreading from the foot and settling: lumpy, grey-green, stepped translucency."""
    n = 8
    s = SILT
    u = (i + 0.5) / n
    rng = np.random.default_rng(2450)
    yy, xx = np.mgrid[0:s, 0:s].astype(np.float64) + 0.5
    v = np.zeros((s, s))
    for _ in range(9):
        a, d0 = rng.uniform(0, TAU), rng.uniform(0.2, 1.0)
        rr = (2.5 + 9 * u ** 0.6) * d0
        cx, cy = s / 2 + np.cos(a) * rr, s / 2 + np.sin(a) * rr * 0.75
        rad = 2.2 + 3.6 * u ** 0.5 * rng.uniform(0.7, 1.2)
        v = np.maximum(v, np.clip(1 - np.hypot(xx - cx, yy - cy) / rad, 0, 1))
    tex = raster.fbm(s, s, 6, 2451 + i, octaves=2)
    dens = v * (0.7 + 0.5 * tex) * (1 - u) ** 1.2 * 1.4
    alpha = np.clip(dens, 0, 1)
    alpha = np.round(alpha * 3) / 3                                   # stepped: 1/3, 2/3, full
    alpha = np.where(alpha >= 1, 2 / 3, alpha)                        # never fully opaque: silt, not a solid
    shade = 0.85 + 0.3 * np.clip((s - xx - yy) / s, -0.5, 0.5)       # lit from the upper left
    rgb = np.array((112, 128, 114.0))[None, None] * shade[..., None] * (0.9 + 0.2 * tex)[..., None]
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


# =========================================================================== build

def _job(job):
    kind, *a = job
    if kind == "wreck":
        return wreck_frame(a[0])
    raise ValueError(kind)


def build(parts):
    if "wreck" in parts:
        with ProcessPoolExecutor() as ex:
            frames = list(ex.map(_job, [("wreck", s) for s in range(3)]))
        kit.write("meridian-wreck", artkit.quantize_set(frames, 40), SOURCE)
        kit.write("meridian-wreck-glow", [wreck_glow(k) for k in range(8)], SOURCE)
    if "silt" in parts:
        kit.write("scuttler-silt", [silt_frame(i) for i in range(8)], SOURCE)


# =========================================================================== review

def _backdrop_frames(name, n=None):
    if n is None:
        p = BACKDROP / f"{name}.png"
        return [Image.open(p).convert("RGBA")] if p.exists() else []
    out = []
    for k in range(n):
        p = BACKDROP / f"{name}_{k}.png"
        if p.exists():
            out.append(Image.open(p).convert("RGBA"))
    return out


def review():
    wreck, wglow = kit.load("meridian-wreck"), kit.load("meridian-wreck-glow")
    silt = kit.load("scuttler-silt")
    beacon = _backdrop_frames("relay-beacon", PULSE_FRAMES) or [beacon_body(PULSE[k]) for k in range(PULSE_FRAMES)]
    bglow = _backdrop_frames("relay-beacon-glow", PULSE_FRAMES) or [beacon_glow(k) for k in range(PULSE_FRAMES)]
    footing = _backdrop_frames("beacon-footing-1")
    pylon = _backdrop_frames("lamp-pylon-1")
    scut = artkit.load_frames("scuttler")
    crate = artkit.load_frames("pickup-crate")
    try:
        burst = kit.load("explosion-deep-small")
    except FileNotFoundError:
        burst = artkit.load_frames("explosion-small")
    rows = [("RELAY BEACON (BACKDROP PIECE, 16 FR AT 8 FPS: THE 2 S PULSE), FRAMES 0-5", beacon[:6], 1, False),
            ("RELAY BEACON GLOW (ADDITIVE, UNGRADED), FRAMES 0-7", bglow[:8], 1, True),
            ("MERIDIAN WRECK: LYING / HIT / OPENED", wreck, 3, False),
            ("WRECK GLOW (ADDITIVE): HULL LAMP 0-3, SPARKS 4-7", wglow, 2, True),
            ("SCUTTLER SILT PUFF (STEPPED)", silt, 3, False)]
    if pylon:
        rows.insert(2, ("GROW-LAMP PYLON (BACKDROP PIECE, ITS POOL BAKED)", pylon, 1, False))
    sheet = artkit.review_sheet("LEVEL 24 PROPS - FINAL (BEACON, PYLON, MERIDIAN WRECK, SILT)", rows, width=1000,
                                batch=BATCH)
    frames = []
    hit_t, open_t = 1.6, 2.6
    for fi in range(96):
        t = fi / 16
        img = kit.plate(480, 540, 0, 7000, section=3)
        glows = []
        # a beacon at the upper left, on its footing
        bx, by = 110, 150
        if footing:
            kit.paste_c(img, kit.grade(footing[0], 3), bx, by)
        k = int(t * PULSE_FPS) % PULSE_FRAMES
        bimg = kit.grade(beacon[k], 3)
        img.alpha_composite(bimg, (bx - BEACON_BASE[0], by - BEACON_BASE[1]))
        glows.append((bglow[k], (bx - BEACON_BASE[0] + BEACON[0] / 2, by - BEACON_BASE[1] + BEACON[1] / 2)))
        # the wreck at the right, shot open
        wx, wy = 340, 250
        st = 0 if t < hit_t else 1 if t < open_t else 2
        kit.paste_c(img, kit.grade(wreck[st], 3), wx, wy)
        if st < 2:
            glows.append((wglow[(int(t * 8) % 4) + (4 if st == 1 else 0)], (wx, wy)))
        for t0 in (hit_t, open_t):
            if t0 <= t < t0 + 0.4:
                glows.append((burst[min(len(burst) - 1, int((t - t0) * 30))], (wx, wy - 2)))
        if t >= open_t:
            hx, hy = wreck_points()["hold"]
            kit.paste_c(img, crate[int(t * 8) % len(crate)], wx - WRECK[0] / 2 + hx, wy - WRECK[1] / 2 + hy
                        - min(26, (t - open_t) * 40))
        # a Scuttler walking down with its puffs
        sx, sy = 200, 120 + t * 40
        f = int(t * 10) % 6
        kit.paste_c(img, kit.grade(scut[f], 3), sx, sy)
        for k2 in range(3):
            tp = (t * 10 - k2 * 3) % 9
            if 0 <= tp < 8:
                side = -1 if (int(t * 10 - k2 * 3) // 9) % 2 else 1
                kit.paste_c(img, kit.grade(silt[int(tp)], 3), sx + side * 12, sy - 6 - (tp / 10) * 40 + 40 * tp / 10)
        kit.paste_c(img, kit.ship(2), 240, 470)
        for g, p in glows:
            img = kit.add_c(img, g, *p)
        frames.append(img.convert("RGB"))
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2 ON THE PLAIN (REVIEW GRADE, SECTION 3): T=0.0, 1.8, 3.0, 5.2",
                     raster.LABEL)
    for i, fi in enumerate((0, 29, 48, 83)):
        big.alpha_composite(frames[fi].convert("RGBA").resize((240, 270), Image.Resampling.BOX),
                            (16 + i * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "l24-props", fps=16)


def main(argv):
    parts = [a for a in argv if a in ("wreck", "silt")] or ["wreck", "silt"]
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--review" not in argv:
        build(parts)
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
