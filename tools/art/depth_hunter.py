#!/usr/bin/env python3
"""Production art: the Depth Hunter, Act 4's unmarked grey sub drone (design/enemies/naval/depth-hunter; M8 part G
batch, concept round 57, step ART-G). From the chosen round-48 look **b** "flat glider" (tools/concept/enemies_r48.py
``depth_hunter`` b, imported unchanged: a broad flat lifting body in plain grey with panel lines, twin dorsal torpedo
tubes, a red sensor slit across the nose, two stern nozzles with an orange glow; enemies_r36's unmarked grey livery and
its neutral rim). Its torpedo is its own now (the round-48 sheet borrowed the Revenant Walker's missile): a slim grey
torpedo with a dark warhead band and a hot motor, and a pale wake.

Headings as tools/art/README.md: heading k turns k x 22.5 degrees clockwise from straight down (`16 angles`; it holds
facing up, heading 8). Size from the stat block: 72x40 on a 76 px canvas (hitbox 56x28), the concept's scale.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``depth-hunter``
once design/enemies/naval/depth-hunter/data.yaml exists, first_level 27):
  depth-hunter_0..31            76x76, normal: heading x 2 + frame: the nozzles' flicker (0 full, 1 dimmer; 8 fps
                                while it moves, 4 in the hold)
  depth-hunter-glow_0..31       76x76, additive, the same order: the red sensor slit and the two nozzles alone (the
                                model's emission, a 1 px stepped halo), drawn ungraded over the hull (in the vents'
                                light and the deep water the slit and nozzles stay bright)
  depth-hunter-torpedo_0..31    24x24, normal: heading x 2 + motor frame (the torpedo's own sprite: 20 px long, grey,
                                a dark warhead band, cruciform tail fins, the motor's orange); drawn at the torpedo's
                                heading (it homes, 16 headings)
  depth-hunter-torpedo-glow_0..31  24x24, additive, the same order: the motor's hot core and a short orange flare
  depth-hunter-wake_0..5        16x16, normal, stepped translucency: the pale wake a torpedo leaves (a puff every
                                ~10 px behind it, 12 fps, spreading and thinning; no bubbles)
  depth-hunter-launch_0..5      32x32, normal with an additive-looking hot core baked in, stepped translucency: the
                                launch's push of water at a tube (pivots ``tubes``), 15 fps
  depth-hunter-break_0..7       80x80, normal, stepped translucency: the `medium` hull break: grey hull plates and a
                                nozzle flung out of a dark burst cloud, sinking and thinning (10 fps; over the under-water
                                `medium` burst explosion-deep-medium); no wreck stays (gap check §5 G3)
  depth-hunter-oil_0..7         64x64, normal, stepped translucency: the dark oil bloom the break leaves (spreading,
                                its sheen fading, 6 fps), drawn under the break
  assets/pivots/depth-hunter.json   per heading the two tubes' muzzles (the torpedo's spawn, its facing) and the two
                                nozzles, in the frame's px
  design/enemies/naval/concept/depth-hunter-final-r57-a.png/.gif   the review sheet and GIF (Level 27's chimney garden
                                under the review's vent light: a column of three rises from the bottom edge out of the
                                deep, holds low facing up, torpedoes rise and home, one is shot down, one Hunter breaks)

Run: python3 tools/art/depth_hunter.py [--review]   (~1 min; --review only rebuilds the review files; backdrop_l27.py
first for the floor)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import l27kit as kit
from artkit import DESIGN, TAU

import enemies_r36 as e36  # noqa: E402  (concept script, unchanged: the unmarked grey livery, the neutral rim)
import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look b's model)
from render import raster  # noqa: E402
from render.enemy_rigs import local_to_screen, model_rotation, model_space_materials  # noqa: E402
from render.sdf import mirror_x, rotate_z, sd_box, sd_capsule, sd_cylinder_y, sd_plate, union  # noqa: E402

SCRIPT = "depth_hunter.py"
BATCH = "M8 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r57"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
HEADINGS = 16
SIZE, EXTENT = 76, 2.3
TUBES = [(-0.17, 0.66), (0.17, 0.66)]       # model units: the dorsal tubes' muzzles (+Y forward)
NOZZLES = [(-0.3, -0.84), (0.3, -0.84)]
T_SIZE, T_EXTENT = 24, 2.4
WAKE, LAUNCH, BREAK, OIL = 16, 32, 80, 64
UP = 8
THRUST = np.array((255, 150, 60), float)
GREY = np.array((124, 129, 139), float)


def turned(scene, mats, h):
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def _mats(thrust=1.0, black=False):
    scene, mats = e48.depth_hunter("b", 1.0)
    mats = list(mats)
    mats[4] = replace(mats[4], emission=tuple(np.array(mats[4].emission) * thrust))
    if black:
        mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    return scene, mats


def _frame(job):
    k, thrust, black = job
    scene, mats = _mats(thrust, black)
    scene, mats = turned(scene, mats, TAU * k / HEADINGS)
    img = artkit.native(*artkit.render_hi(scene, mats, (SIZE, SIZE), EXTENT), crisp=0 if black else 70)
    return img if black else e36.grey_rim(img)


def glow_frame(black):
    """Emission-only render -> additive: the slit and nozzles, a 1 px stepped halo."""
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    halo = np.stack([np.array(Image.fromarray(rgb[..., c].astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.4)),
                              float) for c in range(3)], -1)
    out = np.maximum(rgb, np.floor(halo / 255 * 3 + 0.5) / 3 * 255 * 0.7 * (halo.max(-1, keepdims=True) > 10))
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def pivot(pt, k, size=SIZE, extent=EXTENT):
    dx, dy = local_to_screen(pt[0], pt[1], np.pi / 2 + TAU * k / HEADINGS, size / extent)
    return [round(size / 2 + dx, 1), round(size / 2 + dy, 1)]


# --------------------------------------------------------------------------- the torpedo

def torpedo(motor=1.0):
    """A slim grey torpedo facing +Y: a rounded body, a dark warhead band behind the nose, four tail fins, the motor."""
    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_capsule(p, (0, 0.62, 0.0), (0, -0.62, 0.0), 0.2, 0.2), e48.A_HULL),
            (sd_cylinder_y(p, (0, 0.42, 0.0), 0.215, 0.06), e48.A_GLASS),
            (sd_plate(q, [(0.05, -0.42), (0.36, -0.7), (0.34, -0.8), (0.05, -0.74)], 0.0, 0.03), e48.A_FACET),
            (sd_box(p, (0, -0.6, 0.2), (0.025, 0.16, 0.12), 0.01), e48.A_FACET),
            (sd_cylinder_y(p, (0, -0.8, 0.0), 0.11, 0.06), e48.A_ENGINE),
            k=0.02)
    mats = e36.unmarked_mats(1.0)
    mats[4] = replace(mats[4], emission=tuple(np.array(mats[4].emission) * motor * 1.4))
    return scene, mats


def _torp(job):
    k, motor, black = job
    scene, mats = torpedo(motor)
    if black:
        mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    scene, mats = turned(scene, mats, TAU * k / HEADINGS)
    img = artkit.native(*artkit.render_hi(scene, mats, (T_SIZE, T_SIZE), T_EXTENT), crisp=0 if black else 60)
    return img if black else e36.grey_rim(img)


def torpedo_glow(black, k, motor):
    """The motor's core from the emission render, plus a short flare behind it along the heading."""
    g = np.array(glow_frame(black)).astype(np.float64)
    w = T_SIZE
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    ex, ey = pivot((0, -0.86), k, T_SIZE, T_EXTENT)
    a = TAU * k / HEADINGS                                      # heading 0 = down: the flare points up the screen
    fx, fy = np.sin(a) * 1.0, -np.cos(a) * 1.0
    along = (xx - ex) * fx + (yy - ey) * fy
    across = -(xx - ex) * fy + (yy - ey) * fx
    flare = np.exp(-(across / 1.1) ** 2) * np.clip(1 - along / (4 + 2 * motor), 0, 1) * (along > -1) * 0.8 * motor
    core = np.exp(-((xx - ex) ** 2 + (yy - ey) ** 2) / 2.2) * motor
    f = artkit.ordered_dither(np.clip(flare + core, 0, 1), 6)
    rgb = np.maximum(g[..., :3], THRUST[None, None] * f[..., None] + 90 * np.clip(core - 0.6, 0, 1)[..., None])
    alpha = np.where(rgb.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(rgb, 0, 255), alpha]).astype(np.uint8), "RGBA")


# --------------------------------------------------------------------------- effects

def _puff(w, density, rgb):
    alpha = artkit.ordered_dither(np.clip(density, 0, 1), 4)
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def wake_frame(i, n=6):
    """A pale wake puff: a soft lens of disturbed water swelling and thinning (no bubbles)."""
    w = WAKE
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    noise = raster.fbm(w, w, 4, 2751 + i, octaves=2, period=False)
    r = np.hypot(xx - w / 2, yy - w / 2)
    rad = 2.5 + 5 * u
    dens = np.clip(1 - r / (rad * (0.8 + 0.4 * noise)), 0, 1) ** 0.8 * (1 - u) ** 1.1 * 0.8
    rgb = np.array((176, 206, 214), float) * (0.9 + 0.2 * noise[..., None])
    return _puff(w, dens, rgb)


def launch_frame(i, n=6):
    """The launch's push of water at the tube: a pale ring opening forward, a hot orange core in the first frames."""
    w = LAUNCH
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - w / 2, yy - w / 2)
    ring = np.exp(-((r - (3 + 11 * u ** 0.7)) / (1.2 + 1.5 * u)) ** 2) * (1 - u) * 0.9
    core = np.exp(-(r / (2 + 2 * u)) ** 2) * max(0.0, 1 - 2.5 * u)
    dens = np.clip(ring + core, 0, 1)
    rgb = np.array((190, 214, 220), float)[None, None] * (1 - core[..., None]) + THRUST[None, None] * core[..., None]
    return _puff(w, dens, rgb)


def break_frame(i, n=8):
    """The hull break: a dark burst cloud and grey plates (and one nozzle) flung out, slowing and sinking."""
    w = BREAK
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    noise = raster.fbm(w, w, 10, 2761, octaves=3, period=False)
    r = np.hypot(xx - w / 2, yy - w / 2 - 3 * u)
    rad = 8 + 20 * (1 - (1 - u) ** 2)
    cloud = np.clip(1 - r / (rad * (0.75 + 0.5 * noise)), 0, 1) ** 0.8 * (1 - u) ** 1.3 * 0.85
    img = _puff(w, cloud, np.array((52, 56, 62), float) * (0.85 + 0.3 * noise[..., None]))
    a = np.array(img)
    rng = np.random.default_rng(2763)
    for j in range(9):
        ang, v = rng.uniform(0, TAU), rng.uniform(14, 30)
        d = v * (1 - (1 - u) ** 2)
        px, py = w / 2 + np.cos(ang) * d, w / 2 + np.sin(ang) * d + 6 * u * u
        s, rot = rng.uniform(2.0, 4.5) * (1 - 0.3 * u), rng.uniform(0, TAU) + u * rng.uniform(-3, 3)
        if u > 0.9 and j % 2:
            continue
        cx, cy = xx - px, yy - py
        lx = cx * np.cos(rot) + cy * np.sin(rot)
        ly = -cx * np.sin(rot) + cy * np.cos(rot)
        m = (np.abs(lx) < s) & (np.abs(ly) < s * 0.6)
        lit = np.where(lx + ly < 0, 1.15, 0.75)
        col = THRUST * 0.8 if j == 0 and u < 0.5 else GREY * (1 - 0.35 * u)
        a[m, :3] = np.clip(col * lit[m, None], 0, 255)
        a[m, 3] = 255
    return Image.fromarray(a, "RGBA")


def oil_frame(i, n=8):
    """The oil bloom: a dark brown-black slick spreading with a faint sheen ring, thinning."""
    w = OIL
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    noise = raster.fbm(w, w, 12, 2771, octaves=3, period=False)
    r = np.hypot(xx - w / 2, (yy - w / 2) * 1.1)
    rad = 6 + 22 * (1 - (1 - u) ** 2)
    edge = r / (rad * (0.8 + 0.4 * noise))
    dens = np.clip(1 - edge, 0, 1) ** 0.5 * (1 - 0.75 * u) * 0.9
    sheen = np.exp(-((edge - 0.8) / 0.12) ** 2) * (1 - u)
    rgb = np.array((26, 22, 18), float)[None, None] + np.array((60, 46, 70), float)[None, None] * sheen[..., None]
    return _puff(w, dens, rgb)


def intel_model():
    """(scene, mats) for the hangar intel portrait: the hunter nose down, its nozzles lit."""
    return turned(*_mats(1.0), 0.0)


def build():
    jobs = [(k, th, False) for k in range(HEADINGS) for th in (1.0, 0.62)]
    gjobs = [(k, th, True) for k in range(HEADINGS) for th in (1.0, 0.62)]
    tjobs = [(k, m, False) for k in range(HEADINGS) for m in (1.0, 0.7)]
    tgjobs = [(k, m, True) for k in range(HEADINGS) for m in (1.0, 0.7)]
    with ProcessPoolExecutor() as ex:
        body = list(ex.map(_frame, jobs + gjobs))
        torp = list(ex.map(_torp, tjobs + tgjobs))
    n = len(jobs)
    kit.write("depth-hunter", artkit.quantize_set(body[:n], 32), SOURCE)
    kit.write("depth-hunter-glow", artkit.quantize_set([glow_frame(g) for g in body[n:]], 16), SOURCE)
    kit.write("depth-hunter-torpedo", artkit.quantize_set(torp[:n], 24), SOURCE)
    kit.write("depth-hunter-torpedo-glow",
              [torpedo_glow(g, j // 2, (1.0, 0.7)[j % 2]) for j, g in enumerate(torp[n:])], SOURCE)
    kit.write("depth-hunter-wake", [wake_frame(i) for i in range(6)], SOURCE)
    kit.write("depth-hunter-launch", artkit.quantize_set([launch_frame(i) for i in range(6)], 16), SOURCE)
    kit.write("depth-hunter-break", artkit.quantize_set([break_frame(i) for i in range(8)], 24), SOURCE)
    kit.write("depth-hunter-oil", artkit.quantize_set([oil_frame(i) for i in range(8)], 12), SOURCE)
    artkit.write_pivots("depth-hunter", SOURCE, {
        "frame": [SIZE, SIZE], "headings": HEADINGS,
        "tubes": [[pivot(p, k) for p in TUBES] for k in range(HEADINGS)],
        "nozzles": [[pivot(p, k) for p in NOZZLES] for k in range(HEADINGS)]})


# =========================================================================== review

DUR, FPS = 10.0, 20
SHIP_Y = 150.0
T0 = 62.0                                   # the chimney garden


def ship_x(t):
    return 240 + 80 * np.sin(t * 0.5)


class Hunter:
    """Rises from below the bottom edge at 80 px/s to its hold, holds facing up with a 12 px/s drift, fires every 3.5 s
    (the first 1.0 s after it stops)."""

    def __init__(self, x, hold_y, t0, kill=None):
        self.x, self.hold, self.t0, self.kill = x, hold_y, t0, kill
        self.arrive = t0 + (560 - hold_y) / 80

    def pos(self, t):
        if t < self.arrive:
            return np.array((self.x, 560 - 80 * (t - self.t0))), True
        dt = t - self.arrive
        return np.array((self.x + 12 * np.sin(dt * 0.6) * 1.6, self.hold)), False

    def depth(self, t):
        return float(np.clip((t - self.t0) / 1.2, 0, 1))

    def shots(self):
        ts, out = self.arrive + 1.0, []
        while ts < DUR and (self.kill is None or ts < self.kill):
            out.append(ts)
            ts += 3.5
        return out


class Torpedo:
    def __init__(self, t0, p, kill=None):
        self.t0, self.p0, self.kill = t0, np.array(p, float), kill
        self.path = []
        p, hd, v = self.p0.copy(), np.pi, 63.0             # heading angle: 0 = down (screen), pi = up
        for i in range(int((DUR - t0) * 40) + 1):
            t = t0 + i / 40
            tx = ship_x(t) - p[0]
            ty = SHIP_Y - p[1]
            want = np.arctan2(-tx, ty)
            d = (want - hd + np.pi) % TAU - np.pi
            hd += np.clip(d, -np.radians(70) / 40, np.radians(70) / 40)
            p = p + np.array((-np.sin(hd), np.cos(hd))) * v / 40
            self.path.append((t, p.copy(), hd))

    def at(self, t):
        if t < self.t0 or (self.kill is not None and t >= self.kill) or t >= self.t0 + 5.0:
            return None
        i = min(len(self.path) - 1, int((t - self.t0) * 40))
        return self.path[i]


def review():
    hb, hg, tb, tg, wk, ln, br, oil = (kit.load(n) for n in (
        "depth-hunter", "depth-hunter-glow", "depth-hunter-torpedo", "depth-hunter-torpedo-glow", "depth-hunter-wake",
        "depth-hunter-launch", "depth-hunter-break", "depth-hunter-oil"))
    burst = artkit.load_frames("explosion-deep-medium")
    sburst = artkit.load_frames("explosion-deep-small")
    rows = [("HEADINGS 0-15, NOZZLES FULL", hb[0::2], 1, False),
            ("HOLDING, FACING UP: NOZZLE FLICKER; THE GLOW SET; IN THE VENT LIGHT", [
                hb[UP * 2], hb[UP * 2 + 1], hg[UP * 2], hg[UP * 2 + 1], kit.lit_sprite(hb[UP * 2], 62),
                kit.add_c(kit.lit_sprite(hb[UP * 2], 62), hg[UP * 2], 38, 38)], 2, False),
            ("TORPEDO, 16 HEADINGS (2X)", tb[0::2], 2, False),
            ("TORPEDO MOTOR GLOW (ADDITIVE), EVERY 2ND HEADING", tg[0::4], 3, True),
            ("WAKE PUFF (STEPPED)", wk, 4, False),
            ("LAUNCH AT THE TUBE", ln, 2, False),
            ("HULL BREAK (OVER EXPLOSION-DEEP-MEDIUM)", br, 1, False),
            ("OIL BLOOM", oil, 1, False)]
    sheet = artkit.review_sheet("DEPTH HUNTER - FINAL (ROUND 48 LOOK B, FLAT GLIDER, UNMARKED GREY)", rows, width=1000,
                                batch=BATCH)
    piv = [[pivot(p, k) for p in TUBES] for k in range(HEADINGS)]
    hunters = [Hunter(150, 430, 0.4), Hunter(250, 400, 0.9, kill=7.4), Hunter(350, 445, 1.4)]
    torps = []
    for j, h in enumerate(hunters):
        for n, ts in enumerate(h.shots()):
            p, _ = h.pos(ts)
            mx, my = piv[UP][n % 2]
            torps.append(Torpedo(ts, (p[0] + mx - SIZE / 2, p[1] + my - SIZE / 2),
                                 kill=5.6 if (j, n) == (0, 0) else None))
    frames = []
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        ground = kit.floor_plate(t=T0)
        solids, glows, over = [], [], []
        for h in hunters:
            if t < h.t0:
                continue
            p, moving = h.pos(t)
            if h.kill is not None and t >= h.kill:
                s = t - h.kill
                if s < 1.3:
                    solids.append((oil[min(7, int(s * 6))], p[0], p[1] + 4))
                    if int(s * 10) < len(br):
                        solids.append((br[int(s * 10)], p[0], p[1]))
                if int(s * 20) < len(burst):
                    glows.append((burst[int(s * 20)], p[0], p[1], 1.0))
                continue
            f = int(t * (8 if moving else 4)) % 2
            img = hb[UP * 2 + f]
            dep = h.depth(t)
            if dep < 1:
                a = np.array(img).astype(np.float64)
                a[..., :3] = a[..., :3] * (0.35 + 0.65 * dep) + np.array((6, 30, 44)) * (1 - dep) * 0.6
                img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
            solids.append((img, p[0], p[1]))
            glows.append((hg[UP * 2 + f], p[0], p[1], dep))
            for n, ts in enumerate(h.shots()):
                if 0 <= t - ts < 0.4:
                    mx, my = piv[UP][n % 2]
                    over.append((ln[min(5, int((t - ts) * 15))], p[0] + mx - SIZE / 2, p[1] + my - SIZE / 2))
        for tp in torps:
            for back in np.arange(0.1, 0.55, 0.15):            # the wake behind it
                st = tp.at(t - back)
                if st is not None and tp.at(t) is not None:
                    solids.append((wk[min(5, int(back * 12))], st[1][0], st[1][1]))
            st = tp.at(t)
            if st is None:
                if tp.kill is not None and 0 <= t - tp.kill < 0.5:
                    q = tp.path[int((tp.kill - tp.t0) * 40)][1]
                    glows.append((sburst[min(len(sburst) - 1, int((t - tp.kill) * 20))], q[0], q[1], 1.0))
                continue
            _, q, hd = st
            k = kit.heading_index(-np.sin(hd), np.cos(hd))
            m = int(t * 12) % 2
            over.append((tb[k * 2 + m], q[0], q[1]))
            glows.append((tg[k * 2 + m], q[0], q[1], 1.0))
        img = kit.frame(ground, T0 + t, solids=solids, glows=glows, over=over, ship_xy=(ship_x(t), SHIP_Y))
        if t < 1.6 and int(t * 6) % 2 == 0:
            raster.draw_text(img, 200, 524, "BOTTOM", (255, 80, 60))
        raster.draw_text(img, 8, 8, "DEPTH HUNTER B - T={:.1f}".format(t), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * FPS)] for t in (2.0, 5.0, 7.6)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE COLUMN RISING OUT OF THE DEEP (T=2), TORPEDOES HOMING (T=5), "
                     "A HUNTER BREAKING (T=7.6); 1X CROP OF THE HOLD", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    big.alpha_composite(frames[int(5.0 * FPS)].crop((100, 250, 340, 520)).convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames[::2]], CONCEPT, "depth-hunter",
                       fps=FPS // 2)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
