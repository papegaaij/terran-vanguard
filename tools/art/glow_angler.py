#!/usr/bin/env python3
"""Production art: the Glow Angler, Level 26's lure ambusher (design/enemies/naval/glow-angler; M8 part F batch,
concept round 55, step ART-F). From the chosen round-48 look **a** "rust angler" (tools/concept/enemies_r48.py
``angler`` a, role colours ``glow-angler-a`` = rust / crimson / crimson, imported unchanged): a bulbous rust body, a
wide gape ringed with bone teeth, crimson eyes, a fan tail, a bone stalk arching over the mouth to a teal bead (Q6 a:
like a pickup at a glance, but Vrell teal on a stalk, never a pickup's gold).

Headings as tools/art/README.md: heading k turns k x 22.5 degrees clockwise from straight down (`16 angles`, toward
the ship in a lunge), each its own render under the fixed key light. The stat block's body is 40x32 (hitbox 30x24)
with a 36 px stalk and a 12 px lure: the model is drawn at the concept's scale (27.6 px a model unit) on an 80 px
canvas **with the body's centre on the canvas centre** (the hitbox's centre), so the stalk and the bead reach about
34 px ahead of it.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``glow-angler``
once design/enemies/naval/glow-angler/data.yaml exists, first_level 26):
  glow-angler_0..31        80x80, normal: heading x 2: 0 hanging (jaw shut), 1 the lunge and its ring (the gape wide);
                           the whole unit, stalk and bead included: what the headlight cone, a light pool or the Sonar
                           Pulse shows (in the dark the game's 15 % rim pool lights it faintly)
  glow-angler-glow_0..31   80x80, additive, aligned: the glow-only frame (the bead's teal and the crimson eyes, a 1 px
                           stepped halo; the body black): drawn ungraded over the unit everywhere
  glow-angler-stalk_0..15  80x80, additive, per heading: the stalk lit teal by its bead (brighter toward the bead): the
                           game fades it in while the bead is within 160 px of the ship (alpha (160 - d) / 40), and
                           draws it for 1 s at the death, fading (the stalk stays lit, then goes dark)
  glow-angler-lure_0..7    24x24, additive: the bead's pulse (a teal halo swelling and fading, 8 fps, 1 s a cycle),
                           drawn at the pivot file's ``lure`` of the heading
  glow-angler-muzzle_0..3  24x24, additive: the ring's flash at the gape (teal-white, 2 steps a frame), at ``mouth``
  glow-angler-death_0..7   48x48, additive: the lure blown out (a teal-white flash, the bead's shards flung out and
                           dimming; 12 fps), at ``lure``, over the under-water `small` burst (explosion-deep-small) at the
                           body's centre
  assets/pivots/glow-angler.json   per heading the bead's centre (``lure``), the gape (``mouth``: the ring's origin and
                           the muzzle's centre) and the eyes' midpoint (``eyes``) in the frame's px
  design/enemies/naval/concept/glow-angler-final-r55-a.png/.gif   the review sheet and GIF, in Level 26's dark (the
                           review's stand-in of the light map, tools/art/l26kit.py): anglers hanging in the black as a
                           bead and two eyes, the stalk fading in as the ship nears, a lunge and its ring, one shot (the
                           lure blown out, the stalk lit 1 s), a gold pickup beside them, the Sonar Pulse lighting all

Run: python3 tools/art/glow_angler.py [--review]   (~1 min; --review only rebuilds the review files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import l26kit as kit
from artkit import DESIGN, TAU

import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look a's model and colours)
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import Material, rotate_z, sd_capsule, sd_sphere, union  # noqa: E402

SCRIPT = "glow_angler.py"
BATCH = "M8 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r55"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
V = "a"
HEADINGS = 16
SIZE = 80
EXTENT = 2.9                                   # the concept's 64 px / 2.3 units, kept: 27.6 px a unit
SHIFT = 0.22                                   # the body's centre (concept model y -0.22) onto the canvas centre
JAWS = (0.0, 1.0)
LURE = (0.0, 1.0 + SHIFT, 0.42)                # the bead (model units, after the shift)
MOUTH = (0.0, 0.52 + SHIFT, 0.12)
EYES = (0.0, 0.12 + SHIFT, 0.34)
STALK = [(0.0, 0.05 + SHIFT, 0.42), (0.0, 0.55 + SHIFT, 0.62), (0.0, 0.95 + SHIFT, 0.42)]
TEAL = np.array(e48.TEAL, float)
SMALL, BLOW = 24, 48
COLOURS = 40


# =========================================================================== models

def turned(scene, mats, h):
    """A +Y-forward model turned to point ``h`` clockwise from straight down, its patterns with it."""
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def model(jaw=0.0, pulse=0.7):
    scene, mats = e48.angler(V, jaw, pulse)
    off = np.array([0.0, SHIFT, 0.0])
    return (lambda p: scene(p - off)), mats


def stalk_model():
    """The stalk alone (the concept's two capsules), emissive teal: lit by its bead."""
    def scene(p):
        return union((sd_capsule(p, STALK[0], STALK[1], 0.035, 0.03), 0),
                     (sd_capsule(p, STALK[1], STALK[2], 0.03, 0.022), 0),
                     (sd_sphere(p, LURE, 0.05), 0), k=0.02)
    return scene, [Material((0.0, 0.0, 0.0), emission=tuple(e48.TEAL_F * 0.7))]


def point(k, xyz):
    """A model point of heading k in the frame's px."""
    rot = model_rotation(np.pi / 2 + TAU * k / HEADINGS)
    p = rotate_z(np.array([list(xyz)]), -rot)[0]
    px = SIZE / EXTENT
    return [round(SIZE / 2 + p[0] * px, 1), round(SIZE / 2 - p[1] * px, 1)]


def _frame(job):
    kind, k, jaw = job
    h = TAU * k / HEADINGS
    if kind == "stalk":
        scene, mats = stalk_model()
    else:
        scene, mats = model(jaw)
        if kind == "glow":
            mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    scene, mats = turned(scene, mats, h)
    hi, f = artkit.render_hi(scene, mats, (SIZE, SIZE), EXTENT, shadows=kind == "body")
    return artkit.native(hi, f, crisp=70 if kind == "body" else 0)


def glow_frame(black):
    """The emission only, with a 1 px stepped halo (siren.py's treatment)."""
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    rgb = np.where(rgb.max(-1, keepdims=True) > 22, rgb, 0)
    halo = np.stack([np.array(Image.fromarray(rgb[..., c].astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.4)),
                              float) for c in range(3)], -1)
    out = np.maximum(rgb, np.floor(halo / 255 * 3 + 0.5) / 3 * 255 * 0.6 * (halo.max(-1, keepdims=True) > 12))
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def stalk_frame(black, k):
    """The stalk's emission brightening toward the bead, stepped, a faint 1 px halo."""
    a = np.array(black).astype(np.float64)
    m = (a[..., :3] * (a[..., 3:4] / 255)).max(-1) / 255
    yy, xx = np.mgrid[0:SIZE, 0:SIZE].astype(np.float64)
    lx, ly = point(k, LURE)
    bx, by = point(k, STALK[0])
    span = max(1.0, np.hypot(lx - bx, ly - by))
    near = np.clip(1 - np.hypot(xx - lx, yy - ly) / span, 0, 1)
    v = np.clip(m * 1.6, 0, 1) * (0.35 + 0.65 * near)
    halo = np.asarray(Image.fromarray((v * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.0)), float) / 255
    v = np.maximum(v, halo * 0.45)
    q = artkit.ordered_dither(np.clip(v, 0, 1), 6)
    rgb = (TEAL * 0.75 + 40)[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full(v.shape, 255.0)]).astype(np.uint8)))


def _field(w, fn):
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    return fn(xx - w / 2, yy - w / 2)


def _out(v, col, w, white=None):
    q = artkit.smooth_steps(v)
    rgb = np.asarray(col, float)[None, None] * q[..., None]
    if white is not None:
        rgb = rgb + 255 * np.clip(white, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def lure_frame(i, n=8):
    """The bead's pulse: a teal halo swelling (r 5 -> 9) and fading back, a white-teal core."""
    s = 0.5 - 0.5 * np.cos(TAU * i / n)
    r = 5.0 + 4.0 * s
    k = 0.45 + 0.55 * s

    def f(x, y):
        d = np.hypot(x, y)
        return np.clip(1 - d / r, 0, 1) ** 1.4 * k, np.exp(-(d / 1.6) ** 2) * (0.3 + 0.4 * s)
    v, core = _field(SMALL, f)
    return _out(v, TEAL * 0.9 + np.array((20, 0, 20)), SMALL, core)


def muzzle_frame(i):
    """The ring's flash at the gape: a teal-white burst, a thin ring opening, fading."""
    k = [1.0, 0.75, 0.45, 0.2][i]

    def f(x, y):
        d = np.hypot(x, y)
        core = np.exp(-(d / (2.0 + 0.8 * i)) ** 2) * k
        ring = np.clip(1 - np.abs(d - (4 + 2.4 * i)) / 1.2, 0, 1) * k * 0.8
        return np.clip(core + ring, 0, 1), np.clip(core - 0.55, 0, 1)
    v, white = _field(SMALL, f)
    return _out(v, TEAL, SMALL, white)


def death_frame(i, n=8):
    """The lure blown out: a teal-white flash, eight shards of the bead flung out and dimming."""
    u = i / (n - 1)
    rng = np.random.default_rng(2655)
    angs = rng.uniform(0, TAU, 9)
    spd = rng.uniform(0.7, 1.0, 9)

    def f(x, y):
        d = np.hypot(x, y)
        flash = np.exp(-(d / (3 + 9 * u)) ** 2) * (1 - u) ** 1.5
        v = flash
        for a, s in zip(angs, spd):
            rr = (4 + 18 * s) * (1 - (1 - u) ** 2)
            sx, sy = np.cos(a) * rr, np.sin(a) * rr
            v = v + np.exp(-((x - sx) ** 2 + (y - sy) ** 2) / (1.1 + 0.6 * u)) * (1 - u) ** 0.8
        return np.clip(v, 0, 1), np.clip(flash - 0.5, 0, 1) * 1.6
    v, white = _field(BLOW, f)
    return _out(v, TEAL, BLOW, white)


def intel_model():
    """(scene, mats) for the hangar intel portrait: the angler nose down, the gape half open, the lure lit (the
    concept's centring, so the stalk and bead fit the portrait's frame)."""
    return turned(*e48.angler(V, 0.5, 1.0), 0.0)


# =========================================================================== build

def build():
    jobs = [("body", k, j) for k in range(HEADINGS) for j in JAWS]
    glows = [("glow", k, j) for k in range(HEADINGS) for j in JAWS]
    stalks = [("stalk", k, 0.0) for k in range(HEADINGS)]
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_frame, jobs + glows + stalks))
    n = len(jobs)
    kit.write("glow-angler", artkit.quantize_set(out[:n], COLOURS), SOURCE)
    kit.write("glow-angler-glow", artkit.quantize_set([glow_frame(g) for g in out[n:2 * n]], 16), SOURCE)
    kit.write("glow-angler-stalk", [stalk_frame(s, k) for k, s in enumerate(out[2 * n:])], SOURCE)
    kit.write("glow-angler-lure", [lure_frame(i) for i in range(8)], SOURCE)
    kit.write("glow-angler-muzzle", [muzzle_frame(i) for i in range(4)], SOURCE)
    kit.write("glow-angler-death", [death_frame(i) for i in range(8)], SOURCE)
    artkit.write_pivots("glow-angler", SOURCE, {
        "frame": [SIZE, SIZE], "headings": "k x 22.5 degrees clockwise from straight down; frame k x 2 + jaw",
        "lure": [point(k, LURE) for k in range(HEADINGS)],
        "mouth": [point(k, MOUTH) for k in range(HEADINGS)],
        "eyes": [point(k, EYES) for k in range(HEADINGS)],
        "stalk_reveal": {"within": 160, "fade": 40}})


# =========================================================================== review

DUR, FPS = 10.0, 12
SHIP_PATH = [(0, (240, 486)), (2.5, (226, 420)), (4.0, (250, 400)), (6.0, (300, 410)), (10.0, (250, 440))]
SONAR_T = 7.2


def ship_at(t):
    ts = [p[0] for p in SHIP_PATH]
    return (float(np.interp(t, ts, [p[1][0] for p in SHIP_PATH])),
            float(np.interp(t, ts, [p[1][1] for p in SHIP_PATH])))


class Angler:
    def __init__(self, x, y, drift=20.0, kill=None, trigger=130.0):
        self.p0 = np.array((x, y), float)
        self.drift, self.kill, self.trigger = drift, kill, trigger
        self.state, self.ts, self.pos, self.k = "hang", 0.0, self.p0.copy(), 0
        self.fired, self.away = None, None

    def step(self, t, dt, ship):
        s = np.array(ship)
        if self.state == "hang":
            self.pos = self.pos + np.array((5 * np.cos(t * 0.8) * dt, self.drift * dt))
            d = s - self.pos
            if self.trigger and np.hypot(*d) < self.trigger:
                self.state, self.ts, self.start = "lunge", t, self.pos.copy()
                self.target = self.pos + d / np.linalg.norm(d) * max(0.0, np.linalg.norm(d) - 60)
                self.k = kit.heading_index(*d)
        elif self.state == "lunge":
            u = min(1.0, (t - self.ts) / 0.6)
            self.pos = self.start + (self.target - self.start) * (u * u * (3 - 2 * u))
            if u >= 1.0:
                self.state, self.ts, self.fired = "retreat", t, t
                d = self.pos - s
                self.away = d / max(1e-6, np.linalg.norm(d))
        elif self.state == "retreat":
            self.pos = self.pos + self.away * 100 * dt
            if t - self.ts > 1.5:
                self.state, self.trigger = "hang", 0.0
        return self.pos


def review():
    body, glow, stalk, lure, muzzle, death = (kit.load(n) for n in (
        "glow-angler", "glow-angler-glow", "glow-angler-stalk", "glow-angler-lure", "glow-angler-muzzle",
        "glow-angler-death"))
    piv = __import__("json").loads((artkit.PIVOTS / "glow-angler.json").read_text(encoding="utf-8"))
    burst = artkit.load_frames("explosion-deep-small")
    orb = artkit.load_frames("orb")
    bolt = artkit.load_frames("pulse-bolt")
    pickup = artkit.load_frames("pickup-salvage-small")
    halo = kit.gold_halo()
    rows = [("HANGING, HEADINGS 0-7 (1X)", body[0:16:2], 1, False),
            ("HANGING, HEADINGS 8-15", body[16:32:2], 1, False),
            ("THE LUNGE (GAPE WIDE), EVEN HEADINGS", body[1:32:4], 1, False),
            ("GLOW ONLY: BEAD AND EYES (ADDITIVE, UNGRADED), HEADINGS 0-7", glow[0:16:2], 1, True),
            ("THE STALK LIT BY ITS BEAD (ADDITIVE; FADES IN WITHIN 160 PX, 1 S AT THE DEATH)", stalk[0:8], 1, True),
            ("THE BEAD'S PULSE (8 FPS) / THE RING'S FLASH AT THE GAPE", lure + muzzle, 3, True),
            ("THE LURE BLOWN OUT (12 FPS, OVER EXPLOSION-DEEP-SMALL)", death, 2, True)]
    sheet = artkit.review_sheet("GLOW ANGLER - FINAL (ROUND 48 LOOK A, RUST ANGLER)", rows, width=1000, batch=BATCH)

    def unit(img, a, pos, t, ship, sonar, kill_t=None):
        """Adds one angler to the frame's lists: solids, glows, adds."""
        k = a.k
        jaw = 1 if a.state == "lunge" or (a.fired is not None and t - a.fired < 0.25) else 0
        f = k * 2 + jaw
        lx, ly = pos[0] + piv["lure"][k][0] - SIZE / 2, pos[1] + piv["lure"][k][1] - SIZE / 2
        out = {"solid": [(body[f], pos[0], pos[1], 40)], "glow": [(glow[f], pos[0], pos[1], 1.0),
                                                                  (lure[int(t * 8) % 8], lx, ly, 1.0)], "add": []}
        d = np.hypot(lx - ship[0], ly - ship[1])
        a_st = 1.0 if sonar else float(np.clip((160 - d) / 40, 0, 1))
        if a_st > 0:
            out["add"].append((stalk[k], pos[0], pos[1], a_st))
        if a.fired is not None and t - a.fired < 0.34:
            mx, my = pos[0] + piv["mouth"][k][0] - SIZE / 2, pos[1] + piv["mouth"][k][1] - SIZE / 2
            out["add"].append((muzzle[min(3, int((t - a.fired) * 12))], mx, my, 1.0))
        return out

    pack = [Angler(236, 226, 22), Angler(110, 70, 18, trigger=0), Angler(392, 200, 16, kill=4.6, trigger=0),
            Angler(150, 300, 10, trigger=0)]
    pk = np.array((338.0, 90.0))
    shots, bolts, frames = [], [], []
    dt = 1 / FPS
    for fi in range(int(DUR * FPS)):
        t = fi * dt
        ship = ship_at(t)
        rook = (ship[0] + 62, ship[1] + 30)
        sonar = t >= SONAR_T
        solids, glows, adds, over = [], [], [], []
        for a in pack:
            pos = a.step(t, dt, ship)
            if a.state == "retreat" and abs(t - a.fired) < dt / 2:
                mx, my = pos[0] + piv["mouth"][a.k][0] - SIZE / 2, pos[1] + piv["mouth"][a.k][1] - SIZE / 2
                for j in range(8):
                    ang = TAU * j / 8 + 0.2
                    shots.append([mx, my, np.cos(ang) * 70, np.sin(ang) * 70])
            if a.kill is not None and t >= a.kill:
                s = t - a.kill
                lx, ly = pos[0] + piv["lure"][a.k][0] - SIZE / 2, pos[1] + piv["lure"][a.k][1] - SIZE / 2
                if int(s * 20) < len(burst):
                    adds.append((burst[int(s * 20)], pos[0], pos[1], 1.0))
                if int(s * 12) < len(death):
                    adds.append((death[int(s * 12)], lx, ly, 1.0))
                if s < 1.0:
                    adds.append((stalk[a.k], pos[0], pos[1], 1.0 - s))
                continue
            u = unit(None, a, pos, t, ship, sonar)
            if a.kill is not None and a.kill - 0.5 < t < a.kill and int(t * 14) % 2:
                u["solid"] = [(Image.fromarray(np.where(np.array(u["solid"][0][0])[..., 3:4] > 0,
                                                        np.array((255, 255, 255, 255), np.uint8), 0).astype(np.uint8),
                                               "RGBA"), pos[0], pos[1], 40)]
            solids += u["solid"]
            glows += u["glow"]
            adds += u["add"]
        pp = pk + np.array((0, 26 * t))
        adds.append((halo, pp[0], pp[1], 1.0))
        over.append((pickup[int(t * 10) % 8], pp[0], pp[1]))
        if fi % 3 == 0:
            bolts += [[ship[0] - 9, ship[1] - 26], [ship[0] + 9, ship[1] - 26]]
        for b in bolts:
            b[1] -= 520 * dt
        bolts = [b for b in bolts if b[1] > -20]
        for b in bolts:
            adds.append((bolt[0], b[0], b[1], 1.0))
        for s in shots:
            s[0] += s[2] * dt
            s[1] += s[3] * dt
        shots = [s for s in shots if -10 < s[0] < 490 and -10 < s[1] < 550]
        for s in shots:
            over.append((orb[int(t * 12) % len(orb)], s[0], s[1]))
        img, L = kit.frame(kit.black_floor(), solids, glows, ship, rook, t=t, sonar=sonar, adds=adds, over=over)
        if sonar:
            r = (t - SONAR_T) * 600
            if r < 700:
                a = np.array(img).astype(np.float64)
                yy, xx = np.mgrid[0:540, 0:480]
                band = np.abs(np.hypot(xx - ship[0], yy - ship[1]) - r) < 2
                a[band, :3] = np.minimum(255, a[band, :3] + np.array((60, 200, 170)))
                img = Image.fromarray(a.astype(np.uint8), "RGBA")
            raster.draw_text(img, 8, 24, "SONAR PULSE: ALL UNITS LIT 6 S", (120, 255, 220))
        raster.draw_text(img, 8, 8, "GLOW ANGLER - L26 DARK, AMBIENT 6 %  T+{:.1f}".format(t), raster.LABEL)
        frames.append(img.convert("RGB"))
    # stills: the dark at 1:1, crops at 2x
    stills = [(frames[int(1.0 * FPS)], "IN THE BLACK (T+1.0)"), (frames[int(3.9 * FPS)], "THE LUNGE AND RING (T+3.9)"),
              (frames[int(4.8 * FPS)], "SHOT: LURE OUT (T+4.8)"), (frames[int(8.0 * FPS)], "SONAR PULSE (T+8.0)")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 560), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    y0 = sheet.height
    raster.draw_text(big, 16, y0, "IN LEVEL 26'S DARK (REVIEW STAND-IN OF THE LIGHT MAP: AMBIENT 6 %, THE 70 DEG / 260 PX "
                                  "CONE, 15 % RIM, ROOK'S 56 PX LAMP), 1/2 SIZE", raster.LABEL)
    for i, (s, cap) in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, y0 + 14))
        raster.draw_text(big, 16 + i * 248, y0 + 288, cap, raster.LABEL_DIM)
    y1 = y0 + 304
    raster.draw_text(big, 16, y1, "1:1 CROPS AT 2X: FAR IN THE DARK (BEAD + EYES + RIM) / THE STALK WITHIN 160 PX / "
                                  "A GOLD PICKUP BESIDE A LURE", raster.LABEL)
    crops = [(frames[int(1.0 * FPS)], (60, 20)), (frames[int(2.2 * FPS)], (176, 214)),
             (frames[int(1.6 * FPS)], (280, 50))]
    for i, (f, (cx, cy)) in enumerate(crops):
        c = f.crop((cx, cy, cx + 120, cy + 110)).resize((240, 220), Image.Resampling.NEAREST)
        big.alpha_composite(c.convert("RGBA"), (16 + i * 248, y1 + 14))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "glow-angler",
                       fps=FPS)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
