#!/usr/bin/env python3
"""Production art: the Act 6 "Jovian Storm" systems of M10 part A (step ART-A; final review in concept round 73):
the Plasma Arc (round 72 A.7 a, "the wide Tesla"), the mirror's bounced bullet, the Reflex Shield's gold flash and
its bolt, Jupiter's lightning strike with its telegraph, and the Shield Pylon's dome effects. The shop icons are
tools/art/icons.py ``--act6``. Every glow here is a smooth float light field added once and written unquantised (far
more than 32 steps, no ordered dither: the 2026-10-30 dither audit, ``DitherGuardTest``).

Two looks the docs leave open are a/b proposals of round 73 (m10a-gaps §7; the picks in ``STRIKE_VARIANT``,
``BOLT_VARIANT``, the recommendation written until the user picks; the proposal sheets are
tools/concept/act6_r73.py's):
  strike a "jagged bolt" (QA-2, recommended, written): a jagged blue-white bolt filling the 40 px column top to bottom,
           a white core and two short side forks (the column's straight tell already marks the lane);
         b "light column": a straight full-height column of white light with a jagged core line, its edges fading
           over the 0.25 s.
  bolt   a "white-gold bullet" (QA-3, written): the enemy bullet's round shape in the shield's white-gold (a dark rim,
           a white core) with a short gold trail below it;
         b "gold needle": a thin gold needle with a white core, so a bolt never resembles an enemy bullet.

Outputs (assets/sprites/ unless held, see below; ``additive`` = premultiplied on black, blended GL_SRC_ALPHA, GL_ONE;
``normal`` = straight alpha; frame times in game steps of 1/60 s):

  Plasma Arc (design/player/weapons/plasma-arc; the Tesla Coil Pod's family, act5_gear.py ``arc`` a, drawn wider)
  plasma-arc-arc-<j>_0..2      160x48 additive, j = 0..5 the strike's place in the chain (0 the first strike from the
                               nose, 1.. each jump): the jagged bolt with two forks from the strip's left end (0, 24) to
                               its right end (159, 24), 1.7x the pod's width at j = 0 and 0.72x as wide each jump (1.70,
                               1.22, 0.88, 0.63, 0.46, 0.33); three shapes, one per discharge in turn. The game stretches
                               the strip to the distance (length / 160 along x, y unscaled) and turns it to the
                               direction, drawn 0.1 s (6 steps)
  plasma-arc-spark_0..5        64x64 additive, centred on each struck unit: a white flash and six short sparks, 0.2 s
                               (2 steps a frame)
  plasma-arc-glow_0..1         16x16 additive: the nose's emitter light while it discharges (centred 24 px above the
                               ship's centre, the nose), alternate frames each step
  Bounced bullet (design/enemies, Reflect; the Mirror Interceptor's)
  orb-bounced_0..15            27x27 normal: the standard magenta orb (enemy_bullets.py's ``orb`` body, a smooth halo)
                               with a short soft magenta wake behind it, 16 headings: k x 22.5 degrees clockwise from
                               straight down (``needle``'s order), the orb at the centre (13.5, 13.5)
  Reflex Shield (design/player/shields, Reflex Shield (Act 6))
  ship-reflex_0..3             60x60 additive: the shield-hit ring of ship_fx.py (``ship-shield``) in gold and white-gold,
                               drawn instead of the blue ring while the reflect window is open, 2 steps a frame
  ship-reflex-bolt_0..3        15x15 normal: the bolt (``BOLT_VARIANT`` a), the bullet's shape in white-gold, its core
                               pulsing (4 steps a frame), centre (7.5, 7.5); it flies straight up
  ship-reflex-bolt-trail       11x30 additive: its short gold trail, drawn under the bolt with its point (5.5, 5.5) on
                               the bolt's centre, the trail hanging below
  Lightning (design/world/jovian, Hazards & set pieces)
  lightning-tell_0..1          48x64 additive tile, repeated down the column (seamless top to bottom), centred on the
                               column's x: the 40 px column's faint edges and the thin blue-white line down its middle;
                               0 early, 1 the last 0.4 s (a brighter, wider core); the game brightens it over the 1.0 s
  lightning-strike_0..2        112x540 additive, centred on the column's x, top at the field's top: the strike
                               (``STRIKE_VARIANT`` a), three bolt shapes, 2 steps each through the 0.25 s, the last
                               0.1 s faded by the game; Level 12's sky flash (``weather-flash``) plays with it
  Shield Pylon dome (design/enemies/ground/shield-pylon, The dome)
  shield-pylon-dome_0..11      232x232 additive, centred on the dome (radius 110 px; scale by radius / 110 for easy 95
                               and hard 125): the gold hex lattice at about 20 % with a brighter rim, a slow shimmer
                               crossing it in 12 frames of 20 steps (a 4 s loop)
  shield-pylon-dome-hit_0..5   32x32 additive, on the dome over a covered unit where a shot is spent: a gold spark and
                               the hexes round it lit, 2 steps a frame
  shield-pylon-dome-collapse_0..5   additive, a frame's size fits its ring: the 0.4 s collapse, a gold ring shrinking
                               from the dome's radius to nothing (4 steps a frame); the game moves its centre from the
                               dome's centre to the crown's emitter
  shield-pylon-feed            32x12 additive strip: the solid thin gold feed beam, stretched from the crown's emitter
                               (the left end) to the dome's rim (the right end), y unscaled
  shield-pylon-sheen_0..3      48x48 additive: the covered sheen, a faint gold hex patch with a round soft edge, drawn
                               over a covered unit (scaled to its size / 48), a slow pulse, 15 steps a frame

Held sprites (as act5_gear.py): a frame set whose root the game data does not claim yet goes to
build/art-held/sprites/ instead: ``plasma-arc-*`` until its weapon data.yaml exists (step A2a), ``lightning-*`` until
pipeline/.../SpriteUse.java names the root (step A3b), ``shield-pylon-*`` until the pylon's enemy data.yaml exists (part
G); rerun the part then. ``orb-*`` and ``ship-*`` are claimed game-wide.

Review sheets and GIFs (round 73), each ``<subject>-final-r73-a``: design/player/weapons/plasma-arc/concept/plasma-arc,
design/enemies/concept/bounced-bullet, design/player/shields/concept/reflex-shield, design/world/jovian/concept/
lightning, design/enemies/ground/shield-pylon/concept/pylon-dome. The backgrounds are round 72's Level 36 and 38 mood
grades (tools/concept/act6_r72.py ``sky_l36``, ``sky_l38``, ``ground_l41``) at 1x.

Run: python3 tools/art/act6_gear.py [arc] [bounced] [reflex] [lightning] [dome] [--review] [--check]
     (no part = all, ~1 min; --review only rebuilds the sheets from the written frames; --check lists every set with
     its size, frame count and place and fails on a missing one or a missing Source chunk)
"""
import contextlib
import sys
from pathlib import Path

import numpy as np
from PIL import Image

import act3kit
import artkit
from artkit import DESIGN, ROOT, TAU, sprite

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act6_r72 as a6  # noqa: E402  (concept scripts, imported unchanged)
import gear_r36 as g36  # noqa: E402
import vfx_r08 as v8  # noqa: E402
import vfx_r09 as v9  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402

SCRIPT = "act6_gear.py"
BATCH = "M10 part A batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r73"
artkit.REVIEW_ROUND = ROUND
SPRITE_USE = ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java"
CONCEPT = {"arc": DESIGN / "player" / "weapons" / "plasma-arc" / "concept",
           "bounced": DESIGN / "enemies" / "concept",
           "reflex": DESIGN / "player" / "shields" / "concept",
           "lightning": DESIGN / "world" / "jovian" / "concept",
           "dome": DESIGN / "enemies" / "ground" / "shield-pylon" / "concept"}
STRIKE_VARIANT = "a"    # round 73 QA-2 (recommended a); b only on the proposal sheet
BOLT_VARIANT = "a"      # round 73 QA-3 (a written until the pick; b recommended if the renders show bolts as enemy fire)
SS = 4
FPS = 30
WHITE = np.array((255, 255, 255), float)

# the Plasma Arc: the Tesla Coil Pod's colours (act5_gear.ARC_COL a), drawn wider (round 72 A.7 a)
ARC_GLOW = np.array((140, 200, 255), float)
ARC_FRINGE = np.array((150, 80, 255), float)
ARC_LEN, ARC_H = 160, 48
ARC_W0, ARC_THIN, ARC_JUMPS = 1.7, 0.72, 6
ARC_SPARK = 64
ARC_GLOW_N = 16
# the bounced bullet: the standard orb (enemy_bullets.py) and a magenta wake
VRELL = v9.FAC["vrell"]
ORB_RADIUS = 4.5
BOUNCED, BOUNCED_HEADINGS, WAKE = 27, 16, 12.5
MAGENTA = np.array((255, 64, 255), float)
# the Reflex Shield
GOLD = np.array((255, 206, 90), float)
GOLD_W = np.array((255, 236, 180), float)
REFLEX_RING, REFLEX_RADIUS, REFLEX_CELLS, REFLEX_HIT = 60, 27.0, 5.5, (0.0, -0.85)
BOLT_PAL = {"ring": (255, 222, 120), "deep": (214, 150, 40), "hi": (255, 246, 214), "rim": (64, 40, 6),
            "core": (255, 255, 255)}
BOLT_N, TRAIL_W, TRAIL_H, TRAIL_AT = 15, 11, 30, 5.5
# lightning
STORM_GLOW = np.array((170, 200, 255), float)
STORM_FRINGE = np.array((110, 140, 255), float)
COLUMN = 40
TELL_W, TELL_H = 48, 64
STRIKE_W, STRIKE_H = 112, 540
# the pylon dome (round 72 A.5 a: enemies_r72.dome / beam_light / covered, variant a)
DOME_R, DOME_PAD, DOME_FRAMES, DOME_CELL = 110, 6, 12, 9.0
DOME_GOLD = np.array((255, 206, 90), float)
FEED_GOLD = np.array((255, 214, 120), float)
HIT_N, SHEEN_N = 32, 48


# =========================================================================== small kit

def grid(w, h, ss=SS):
    """Supersampled pixel-centre coordinates in native pixels."""
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64)
    return (xx + 0.5) / ss, (yy + 0.5) / ss


def gauss(d, w):
    return np.exp(-(d / np.maximum(w, 1e-6)) ** 2)


def poly_dist(x, y, pts):
    """Distance to a polyline (a running minimum over its segments: no stack of full-size arrays)."""
    best = np.full(x.shape, np.inf)
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        bx, by = x1 - x0, y1 - y0
        h = np.clip(((x - x0) * bx + (y - y0) * by) / max(bx * bx + by * by, 1e-9), 0, 1)
        np.minimum(best, np.hypot(x - x0 - bx * h, y - y0 - by * h), out=best)
    return best


def light_image(light, w, h, ss=SS):
    """A supersampled light field (0..255 RGB) -> an additive frame (premultiplied, alpha where lit), unquantised."""
    lit = np.clip(light.reshape(h, ss, w, ss, 3).mean(axis=(1, 3)), 0, 255)
    rgb = np.round(lit)
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def add(light, col, amount):
    light += np.asarray(col, float)[None, None, :] * np.clip(amount, 0, None)[..., None]


def hex_edges(x, y, cell, ox=0.0, oy=0.0):
    """Distance (native px) to the nearest edge of a pointy-top hex lattice of ``cell`` circumradius, and each
    point's cell centre (relative to the origin)."""
    qx, qy = x - ox, y - oy
    w, h = np.sqrt(3) * cell, 1.5 * cell
    best = np.full(x.shape, np.inf)
    cx_b = np.zeros(x.shape)
    cy_b = np.zeros(x.shape)
    row = np.floor(qy / h)
    for dr in (-1, 0, 1):
        r = row + dr
        shift = np.where(r % 2 == 1, w / 2, 0.0)
        col = np.round((qx - shift) / w)
        for dc in (-1, 0, 1):
            cx = (col + dc) * w + shift
            cy = r * h
            d = np.hypot(qx - cx, qy - cy)
            better = d < best
            best = np.where(better, d, best)
            cx_b = np.where(better, cx, cx_b)
            cy_b = np.where(better, cy, cy_b)
    dx, dy = np.abs(qx - cx_b), np.abs(qy - cy_b)
    norm = np.maximum(dx, dx / 2 + dy * np.sqrt(3) / 2)
    return np.abs(cell * np.sqrt(3) / 2 - norm), (cx_b, cy_b)


def claimed(root):
    if root == "plasma-arc":
        return act3kit.claimed(root)
    if root in ("orb", "ship"):
        return True
    if root == "shield-pylon":
        return (DESIGN / "enemies" / "ground" / "shield-pylon" / "data.yaml").exists()
    return f'"{root}"' in SPRITE_USE.read_text(encoding="utf-8")


@contextlib.contextmanager
def sprites_of(root):
    target = artkit.SPRITES if claimed(root) else act3kit.HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def where(root):
    return "assets/sprites" if claimed(root) else "build/art-held/sprites (not claimed yet)"


def write(root, name, frames, single=False):
    with sprites_of(root):
        artkit.write_frames(name, frames, SOURCE, single=single)


def load(root, name):
    with sprites_of(root):
        return artkit.load_frames(name)


def save(kind, sheet, gif_frames, subject, variant="a", fps=FPS):
    png, gif = artkit.review_paths(CONCEPT[kind], subject, variant)
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(gif_frames, gif, fps=fps, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def paste_light(img, frame, cx, cy):
    return artkit.add_light(img, frame, (int(round(cx - frame.width / 2)), int(round(cy - frame.height / 2))))


def draw_strip(img, strip, x0, y0, x1, y1):
    """A strip stretched from (x0, y0) to (x1, y1), as the game draws it (length along x, y unscaled)."""
    length = max(4, int(round(np.hypot(x1 - x0, y1 - y0))))
    s = strip.resize((length, strip.height), Image.BILINEAR)
    ang = np.degrees(np.arctan2(-(y1 - y0), x1 - x0))
    s = s.rotate(ang, resample=Image.BILINEAR, expand=True)
    return paste_light(img, s, (x0 + x1) / 2, (y0 + y1) / 2)


def plate(kind, w=a6.FW, h=a6.FH, seed=None):
    """A review ground at 1x: round 72's Level 36 (``l36``) or 38 (``l38``) grade, or Level 41's ice (``l41``)."""
    rgb = {"l36": a6.sky_l36, "l38": a6.sky_l38, "l41": a6.ground_l41}[kind]()
    return a6.to_img(rgb[:h, :w]).convert("RGBA")


def prod(name, i=0):
    return a6.Prod.get(name, i)


# =========================================================================== the Plasma Arc

def arc_width(j):
    return ARC_W0 * ARC_THIN ** j


def arc_frame(j, k):
    """Strike j of the chain, shape k: round 72's draw_strike (a) in a 160 px strip."""
    rng = np.random.default_rng(7300 + 17 * k)
    w = arc_width(j)
    x, y = grid(ARC_LEN, ARC_H)
    light = np.zeros(x.shape + (3,))
    c = ARC_H / 2
    main = [(px, float(np.clip(py, c - 11, c + 11)))
            for px, py in g36.jagged((0.0, c), (float(ARC_LEN), c), rng, depth=5, rough=0.2)]
    paths = [(main, 1.0)]
    for _ in range(2):                                    # two short forks off the main line
        i = int(rng.integers(4, len(main) - 6))
        bx, by = main[i]
        ang = rng.choice((-1, 1)) * rng.uniform(0.5, 1.0)
        ln = rng.uniform(0.12, 0.22) * ARC_LEN
        end = (bx + np.cos(ang) * ln, float(np.clip(by + np.sin(ang) * ln, 5, ARC_H - 5)))
        paths.append((g36.jagged((bx, by), end, rng, depth=3, rough=0.3), 0.5))
    for pts, kk in paths:
        d = poly_dist(x, y, pts)
        wk = w * (0.7 if kk < 1 else 1.0)
        add(light, ARC_FRINGE, gauss(d, 3.4 * wk) * 0.26 * kk)
        add(light, ARC_GLOW, gauss(d, 1.5 * wk) * 0.9 * kk)
        add(light, WHITE, gauss(d, 0.6 * wk) * 1.4 * kk)
    return light_image(light, ARC_LEN, ARC_H)


def short_bolt(light, x, y, p0, p1, seed, w, k):
    """A tiny jagged spark (the Tesla family at width ``w``) into ``light``."""
    pts = g36.jagged(p0, p1, np.random.default_rng(seed), depth=3, rough=0.3)
    d = poly_dist(x, y, pts)
    add(light, ARC_FRINGE, gauss(d, 3.4 * w) * 0.26 * k)
    add(light, ARC_GLOW, gauss(d, 1.5 * w) * 0.9 * k)
    add(light, WHITE, gauss(d, 0.6 * w) * 1.4 * k)


def arc_spark(f, n=6):
    """round 72's strike_burst: a white flash, the glow, six short sparks flying out (s = 0..1 over 0.2 s)."""
    s = f / n
    x, y = grid(ARC_SPARK, ARC_SPARK)
    c = ARC_SPARK / 2
    r = np.hypot(x - c, y - c)
    light = np.zeros(x.shape + (3,))
    add(light, WHITE, gauss(r, 5 + 6 * s) * 1.4 * (1 - s) ** 2)
    add(light, ARC_GLOW, gauss(r, 10 + 10 * s) * 0.6 * (1 - s))
    rng = np.random.default_rng(7400)
    for j in range(6):
        a = TAU * j / 6 + rng.uniform(-0.3, 0.3)
        r0, r1 = 3 + 10 * s, 9 + 16 * s
        short_bolt(light, x, y, (c + np.cos(a) * r0, c + np.sin(a) * r0), (c + np.cos(a) * r1, c + np.sin(a) * r1),
                   7410 + j, 0.45, (1 - s) * 0.8)
    return light_image(light, ARC_SPARK, ARC_SPARK)


def arc_glow(f):
    x, y = grid(ARC_GLOW_N, ARC_GLOW_N)
    r = np.hypot(x - ARC_GLOW_N / 2, y - ARC_GLOW_N / 2)
    light = np.zeros(x.shape + (3,))
    k = 1.0 if f == 0 else 0.75
    add(light, ARC_FRINGE, gauss(r, 5.0) * 0.25 * k)
    add(light, ARC_GLOW, gauss(r, 3.4) * 0.85 * k)
    add(light, WHITE, gauss(r, 1.4) * 1.3 * k)
    return light_image(light, ARC_GLOW_N, ARC_GLOW_N)


def arc_set():
    return dict(arcs=[[arc_frame(j, k) for k in range(3)] for j in range(ARC_JUMPS)],
                sparks=[arc_spark(f) for f in range(6)], glows=[arc_glow(f) for f in range(2)])


def build_arc(art):
    for j in range(ARC_JUMPS):
        write("plasma-arc", f"plasma-arc-arc-{j}", art["arcs"][j])
    write("plasma-arc", "plasma-arc-spark", art["sparks"])
    write("plasma-arc", "plasma-arc-glow", art["glows"])
    print(f"plasma arc: {where('plasma-arc')}")


def load_arc():
    return dict(arcs=[load("plasma-arc", f"plasma-arc-arc-{j}") for j in range(ARC_JUMPS)],
                sparks=load("plasma-arc", "plasma-arc-spark"), glows=load("plasma-arc", "plasma-arc-glow"))


def talon(heading):
    """The production Talon (16 headings, frame 0 nose down) as the act's stand-in."""
    k = int(round((heading % TAU) / TAU * 16)) % 16
    return prod("talon", k)


def arc_review(art, bounced):
    """The chain at L5 over Level 36's bands: eight Talon stand-ins (the Chimera is part B's), the strips
    stretched per jump, the sparks, a kill's explosion on the cream band, bounced orbs drifting through."""
    W, H = a6.FW, 400
    bg = plate("l36", W, H)
    boom = artkit.load_frames("explosion-small")
    py, nose_dy = 350, 24
    units = [dict(x=110 + 40 * (j % 4) + 18 * (j // 4), y=150 + 46 * (j // 4) + 8 * (j % 2),
                  hp=50.0 if j == 5 else 900.0, dead=None) for j in range(8)]
    # bounced bullets drifting through the chain: x, y, heading (radians clockwise from straight down)
    orbs = [(330, 120, 0.9), (360, 170, 0.6), (90, 230, -0.5), (300, 250, 0.2)]
    strikes, sparks, gif = [], [], []
    nxt = 0.2
    for i in range(FPS * 3):
        t = i / FPS
        img = bg.copy()
        px = 220 + 40 * np.sin(t * 1.3)
        nose = (px, py - nose_dy)
        for u in units:
            u["x"] += 18 / FPS
        if t >= nxt:
            nxt += 0.2                                       # 5 discharges a second (L5)
            live = [u for u in units if u["dead"] is None]
            cone = [u for u in live if np.hypot(u["x"] - nose[0], u["y"] - nose[1]) <= 240
                    and abs(np.arctan2(u["x"] - nose[0], -(u["y"] - nose[1]))) <= np.radians(45)]
            if cone:
                cur = min(cone, key=lambda u: np.hypot(u["x"] - nose[0], u["y"] - nose[1]))
                chain, src, dmg, seen = [], nose, 24.0, {id(cur)}
                for c in range(6):
                    chain.append((src, cur, c))
                    cur["hp"] -= dmg
                    sparks.append((i, cur))
                    if cur["hp"] <= 0 and cur["dead"] is None:
                        cur["dead"] = i
                    dmg *= 0.75
                    src = (cur["x"], cur["y"])
                    near = [u for u in live if id(u) not in seen and np.hypot(u["x"] - src[0], u["y"] - src[1]) <= 120]
                    if not near:
                        break
                    cur = min(near, key=lambda u: np.hypot(u["x"] - src[0], u["y"] - src[1]))
                    seen.add(id(cur))
                strikes.append((i, [(s, (u["x"], u["y"]), c) for s, u, c in chain], len(strikes) % 3))
        for u in units:
            if u["dead"] is None:
                sprite.paste_center(img, talon(0.0), u["x"], u["y"])
            elif i - u["dead"] < len(boom):
                img = paste_light(img, boom[i - u["dead"]], u["x"], u["y"])
        for x0, y0, hd in orbs:
            speed = 110 / FPS
            ox, oy = x0 - np.sin(hd) * speed * i, y0 + np.cos(hd) * speed * i
            k = int(round(hd / TAU * BOUNCED_HEADINGS)) % BOUNCED_HEADINGS
            sprite.paste_center(img, bounced[k], ox, oy)
        sprite.paste_center(img, prod("ship", 2), px, py)
        firing = False
        for i0, chain, shape in strikes:
            if 0 <= i - i0 < 3:                               # 0.1 s
                firing = True
                for (sx, sy), (tx, ty), c in chain:
                    img = draw_strip(img, art["arcs"][c][(shape + (i - i0)) % 3], sx, sy, tx, ty)
        for i0, u in sparks:
            if 0 <= (i - i0) * 2 < 6:
                img = paste_light(img, art["sparks"][(i - i0) * 2], u["x"], u["y"])
        if firing:
            img = paste_light(img, art["glows"][i % 2], *nose)
        gif.append(img)
    rows = [("PLASMA-ARC-ARC-<J>_0: 160X48 STRIPS, J = 0 (THE FIRST STRIKE, 1.7X THE POD'S WIDTH) .. 5 (0.72X A JUMP); "
             "SOURCE AT THE LEFT END (ADDITIVE)", [art["arcs"][j][0] for j in range(ARC_JUMPS)], 1, True),
            ("PLASMA-ARC-ARC-0_0..2: THE THREE SHAPES OF THE FIRST STRIKE (2X)", art["arcs"][0], 2, True),
            ("PLASMA-ARC-SPARK_0..5: 64X64 ON EVERY STRUCK UNIT, 2 STEPS A FRAME (2X); PLASMA-ARC-GLOW_0..1 AT THE NOSE",
             art["sparks"] + art["glows"], 2, True),
            ("BESIDE THE TESLA COIL POD'S ARC (ACT5_GEAR.PY, 128X24), 2X: ONE FAMILY",
             [load_tesla(), art["arcs"][0][0], art["arcs"][2][0]], 2, True),
            ("IN FLIGHT AT 1X OVER LEVEL 36'S BANDS (ROUND 72'S GRADE): L5, SIX TARGETS, REACH 240 PX, JUMPS OF 120; "
             "TALON STAND-INS; BOUNCED MAGENTA ORBS DRIFTING THROUGH; A KILL'S EXPLOSION ON THE CREAM",
             [gif[k].crop((60, 60, 420, 390)) for k in (7, 13, 37, 62)], 1, False)]
    sheet = artkit.review_sheet("PLASMA ARC - FINAL SPRITES (ROUND 72 A.7 A, THE WIDE TESLA)", rows, width=1500,
                                batch=BATCH)
    save("arc", sheet, gif, "plasma-arc")


def load_tesla():
    return artkit.load_frames("tesla-coil-pod-arc")[0]


# =========================================================================== the bounced bullet

def orb_body(pal, phase):
    """The standard orb's body (vfx_r09.b_orb) with its soft halo kept smooth."""
    return v9.b_orb(pal, ORB_RADIUS, phase)


def bounced_frame(k):
    """Heading k (x 22.5 degrees clockwise from straight down): the orb with its magenta wake behind it."""
    a = k * TAU / BOUNCED_HEADINGS
    vx, vy = -np.sin(a), np.cos(a)                      # its flight on the screen (0 = down)
    x, y = grid(BOUNCED, BOUNCED)
    c = BOUNCED / 2
    px, py = x - c, y - c
    s = -(px * vx + py * vy)                             # distance behind the centre
    perp = np.abs(px * vy - py * vx)
    along = np.clip(s / WAKE, 0, 1)
    wake = gauss(perp, 2.0 * (1 - 0.5 * along)) * (s > 0) * (1 - along) ** 1.1 * 0.9
    alpha = wake.reshape(BOUNCED, SS, BOUNCED, SS).mean(axis=(1, 3))
    img = np.zeros((BOUNCED, BOUNCED, 4))
    img[..., :3] = MAGENTA
    img[..., 3] = np.clip(alpha, 0, 1) * 255
    under = Image.fromarray(np.round(img).astype(np.uint8), "RGBA")
    under.alpha_composite(orb_body(VRELL, 0.0), (int(c - 7.5), int(c - 7.5)))
    a8 = np.array(under)
    a8[a8[..., 3] == 0] = 0
    return Image.fromarray(a8, "RGBA")


def bounced_set():
    return [bounced_frame(k) for k in range(BOUNCED_HEADINGS)]


def bounced_review(frames):
    orbs = artkit.load_frames("orb")
    gif = []
    ground = {"l36": plate("l36", 240, 240), "l38": plate("l38", 240, 240)}
    rng = np.random.default_rng(73)
    shots = [(rng.uniform(10, 230), rng.uniform(-40, 200), rng.uniform(-0.9, 0.9), j % 2) for j in range(12)]
    for i in range(48):
        cells = []
        for key in ("l36", "l38"):
            img = ground[key].copy()
            for j, (x, y, a, bounced) in enumerate(shots):
                speed = (110 if bounced else 160) / FPS
                px, py = x - np.sin(a) * speed * i, (y + np.cos(a) * speed * i + 40) % 280 - 40
                if bounced:
                    k = int(round(a / TAU * BOUNCED_HEADINGS)) % BOUNCED_HEADINGS
                    sprite.paste_center(img, frames[k], px, py)
                else:
                    sprite.paste_center(img, orbs[(i // 4 + j) % 4], px, py)
            cells.append(img)
        row = Image.new("RGBA", (488, 240), (10, 12, 26, 255))
        row.alpha_composite(cells[0], (0, 0))
        row.alpha_composite(cells[1], (248, 0))
        gif.append(sprite.enlarge(row, 2))
    rows = [("ORB-BOUNCED_0..15: 27X27, 16 HEADINGS CLOCKWISE FROM DOWN (NORMAL BLEND), 3X", frames, 3, False),
            ("BESIDE THE STANDARD ORB (ORB_0..3, 160 PX/S) AT 3X: THE SAME MAGENTA BODY, THE WAKE MARKS A TURNED SHOT",
             orbs + frames[:1] + frames[4:5] + frames[8:9], 3, False),
            ("1X OVER ROUND 72'S LEVEL 36 AND 38 GRADES: BOUNCED (110 PX/S, WAKE) AMONG STANDARD ORBS",
             [gif[k].resize((488, 240), Image.NEAREST) for k in (0, 24)], 1, False)]
    sheet = artkit.review_sheet("BOUNCED BULLET - FINAL SPRITES (THE MIRROR'S REFLECT, ROUND 72 Q1 C)", rows,
                                width=1300, batch=BATCH)
    save("bounced", sheet, gif, "bounced-bullet")


# =========================================================================== the Reflex Shield

def reflex_ring():
    """ship_fx.ring_frames in gold: the hex cells round a hit at the front and a ripple over the bubble."""
    out = []
    for i in range(4):
        t = i / 3
        cv = v8.Canvas(REFLEX_RING, REFLEX_RING)
        c = REFLEX_RING / 2
        e = cv.ell(c, c, REFLEX_RADIUS, REFLEX_RADIUS)
        u, v = (cv.x - c) / REFLEX_RADIUS, (cv.y - c) / REFLEX_RADIUS
        hx = u * REFLEX_CELLS
        hy = v * REFLEX_CELLS * 1.1547
        q = np.abs(((hx + 0.5 * (np.floor(hy) % 2)) % 1.0) - 0.5)
        r = np.abs((hy % 1.0) - 0.5)
        lines = np.clip(1 - np.minimum(q, r) * 9, 0, 1)
        hd = np.hypot(u - REFLEX_HIT[0], v - REFLEX_HIT[1])
        ripple = v8.gauss(hd - t * 1.8, 0.22) * (1 - t)
        local = v8.gauss(hd, 0.5) * (1 - t) ** 1.5
        shell = np.clip(1 - np.abs(e - 1) * 9, 0, 1) * (e < 1.08)
        inside = (e < 1).astype(float)
        amount = (shell * (0.25 + 1.2 * local + ripple)
                  + inside * lines * (0.9 * local + 0.7 * ripple)) * (1.0 - 0.35 * t)
        cv.add(GOLD / 255, amount * 0.9)
        cv.add(GOLD_W / 255, v8.gauss(hd, 0.3) * (1 - t) ** 2 * 1.2 * shell)
        cv.add(v8.WHITE, v8.gauss(hd, 0.16) * (1 - t) ** 2 * 1.2 * shell)
        out.append(artkit.additive(cv.image()))
    return out


def bolt_body(v, f):
    """The bolt, variant a: the orb's shape in white-gold; b: a thin gold needle (normal blend, 15 x 15 / 7 x 24)."""
    if v == "a":
        return orb_body(BOLT_PAL, TAU * f / 4)
    x, y = grid(7, 24)
    d = np.hypot(np.maximum(np.abs(y - 12) - 7.0, 0), x - 3.5)
    body = np.clip(2.3 - d, 0, 1)                         # with its 1 px dark rim (the bullets' outline rule)
    fill = np.clip(1.4 - d, 0, 1)
    core = np.clip(0.8 - d, 0, 1) * (0.8 + 0.2 * np.cos(TAU * f / 4))
    rgb = np.zeros(x.shape + (3,))
    rgb[:] = np.array(BOLT_PAL["rim"], float)
    rgb = rgb * (1 - fill[..., None]) + GOLD * fill[..., None]
    rgb = rgb * (1 - core[..., None]) + WHITE * core[..., None]
    a = body.reshape(24, SS, 7, SS).mean(axis=(1, 3))
    c = (rgb * body[..., None]).reshape(24, SS, 7, SS, 3).mean(axis=(1, 3)) / np.maximum(a[..., None], 1e-6)
    return Image.fromarray(np.dstack([np.round(np.clip(c, 0, 255)), np.round(a * 255)]).astype(np.uint8), "RGBA")


def bolt_trail(v):
    """The short gold trail under the bolt (additive), its point (5.5, 5.5) on the bolt's centre."""
    x, y = grid(TRAIL_W, TRAIL_H)
    s = np.clip((y - TRAIL_AT) / (TRAIL_H - TRAIL_AT - 1), 0, 1) * (y > TRAIL_AT - 2)
    width = (1.8 if v == "a" else 0.9) * (1 - 0.6 * s)
    light = np.zeros(x.shape + (3,))
    fade = (1 - s) ** 1.6 * (y > TRAIL_AT - 2)
    add(light, GOLD, gauss(np.abs(x - TRAIL_W / 2), width * 1.8) * 0.45 * fade)
    add(light, GOLD_W, gauss(np.abs(x - TRAIL_W / 2), width * 0.7) * 0.7 * fade)
    return light_image(light, TRAIL_W, TRAIL_H)


def reflex_set(v=BOLT_VARIANT):
    return dict(ring=reflex_ring(), bolt=[bolt_body(v, f) for f in range(4)], trail=bolt_trail(v))


def build_reflex(art):
    write("ship", "ship-reflex", art["ring"])
    write("ship", "ship-reflex-bolt", art["bolt"])
    write("ship", "ship-reflex-bolt-trail", [art["trail"]], single=True)
    print("reflex shield: assets/sprites")


def reflex_scene(art, bg, frames=72, window=True, seed=37):
    """The ship under a stream of orbs: each hit flashes gold (window open) or blue, and a bolt leaves straight up."""
    orbs = artkit.load_frames("orb")
    blue = artkit.load_frames("ship-shield")
    W, H = bg.size
    sx, sy = W / 2, H - 70
    rng = np.random.default_rng(seed)
    shots = [(sx + rng.uniform(-14, 14), 60 - 34 * j, rng.uniform(-0.08, 0.08)) for j in range(9)]
    flashes, bolts, gif = [], [], []
    alive = [True] * len(shots)
    for i in range(frames):
        img = bg.copy()
        sprite.paste_center(img, prod("ship", 2), sx, sy)
        for j, (x0, y0, a) in enumerate(shots):
            if not alive[j]:
                continue
            ox, oy = x0 - np.sin(a) * 4.6 * i, y0 + np.cos(a) * 4.6 * i
            if oy >= sy - 24:
                alive[j] = False
                flashes.append(i)
                if window:
                    bolts.append((i, ox, sy - 26))
                continue
            sprite.paste_center(img, orbs[(i // 2 + j) % 4], ox, oy)
        for i0 in flashes:
            k = (i - i0) // 2
            if 0 <= k < 4:
                img = paste_light(img, (art["ring"] if window else blue)[k], sx, sy)
        for i0, bx, by in bolts:
            yb = by - 640 / FPS * (i - i0)
            if yb > -20:
                img = artkit.add_light(img, art["trail"], (int(round(bx - TRAIL_AT)), int(round(yb - TRAIL_AT))))
                body = art["bolt"][(i // 2) % 4]
                sprite.paste_center(img, body, bx, yb)
        gif.append(img)
    return gif


def reflex_review(art):
    bg = plate("l36", 200, 320)
    gold = reflex_scene(art, bg)
    blue = reflex_scene(art, bg, window=False)
    gif = []
    for g, b in zip(gold, blue):
        row = Image.new("RGBA", (408, 320), (10, 12, 26, 255))
        row.alpha_composite(g, (0, 0))
        row.alpha_composite(b, (208, 0))
        gif.append(sprite.enlarge(row, 2))
    orbs = artkit.load_frames("orb")
    bounced = artkit.load_frames("orb-bounced")
    rows = [("SHIP-REFLEX_0..3: 60X60 GOLD HEX FLASH WHILE THE WINDOW IS OPEN, BESIDE THE BLUE SHIP-SHIELD_0..3 "
             "(ADDITIVE, 2X)", art["ring"] + artkit.load_frames("ship-shield"), 2, True),
            ("SHIP-REFLEX-BOLT_0..3 (15X15, NORMAL, CORE PULSE) AND ITS TRAIL (11X30, ADDITIVE), 4X",
             art["bolt"], 4, False),
            ("THE TRAIL; THE BOLT BESIDE THE ENEMY ORB AND THE BOUNCED BULLET (4X)",
             [art["trail"]], 4, True),
            ("", [art["bolt"][0], orbs[0], bounced[0]], 4, False),
            ("1X OVER LEVEL 36: LEFT THE WINDOW OPEN (GOLD FLASH, A BOLT UP AT 640 PX/S PER HIT), RIGHT BELOW 75 %",
             [gif[k].resize((408, 320), Image.NEAREST) for k in (38, 41, 47, 56)], 1, False)]
    sheet = artkit.review_sheet("REFLEX SHIELD - FINAL SPRITES (BOLT: ROUND 73 QA-3 A, PENDING THE PICK)", rows,
                                width=1300, batch=BATCH)
    save("reflex", sheet, gif, "reflex-shield")


# =========================================================================== lightning

def tell_frame(f):
    """The telegraph tile: the column's faint edges and the thin line down its middle (f 1: the late, brighter one)."""
    x, y = grid(TELL_W, TELL_H)
    c = TELL_W / 2
    dx = np.abs(x - c)
    light = np.zeros(x.shape + (3,))
    k = 1.0 if f else 0.6
    add(light, STORM_GLOW, np.exp(-(dx / (COLUMN / 2)) ** 8) * (0.16 + 0.06 * f))
    add(light, STORM_GLOW, gauss(dx - COLUMN / 2 + 1, 1.2) * 0.22 * k)          # the column's edges
    add(light, STORM_GLOW, gauss(dx, 2.0 + 1.2 * f) * 0.45 * k)
    add(light, WHITE, gauss(dx, 0.6 + 0.4 * f) * (0.8 + 0.6 * f))
    return light_image(light, TELL_W, TELL_H)


def strike_path(rng, c, top=-6.0, bottom=STRIKE_H + 6.0):
    """A bolt down the column: a bounded random walk every 14-26 px, each leg broken by a small jag."""
    pts, yv, xv = [], top, c
    while yv < bottom:
        pts.append((xv, yv))
        yv += rng.uniform(14, 26)
        xv = float(np.clip(xv + rng.normal(0, 8) - 0.25 * (xv - c), c - 16, c + 16))
    pts.append((xv, bottom))
    out = [pts[0]]
    for a, b in zip(pts[:-1], pts[1:]):
        out += g36.jagged(a, b, rng, depth=2, rough=0.22)[1:]
    return out


def strike_frame(v, k, t=0.0):
    """Variant a: shape k of the jagged bolt with two forks; b: the column of light at time t (0..1 of 0.25 s)."""
    ss = 2
    rng = np.random.default_rng(7500 + 31 * k)
    x, y = grid(STRIKE_W, STRIKE_H, ss)
    c = STRIKE_W / 2
    dx = np.abs(x - c)
    light = np.zeros(x.shape + (3,))
    if v == "a":
        main = strike_path(rng, c)
        add(light, STORM_GLOW, np.exp(-(dx / (COLUMN / 2)) ** 8) * 0.10)        # the lane lit faintly
        paths = [(main, 1.0)]
        for half in (0, 1):                                                     # one fork in each half
            i = int(rng.integers(len(main) * (0.12 + 0.45 * half), len(main) * (0.4 + 0.45 * half)))
            bx, by = main[i]
            side = 1 if bx < c else -1
            if rng.uniform() < 0.3:
                side = -side
            ang = np.pi / 2 - side * rng.uniform(0.6, 1.0)                     # down and out
            ln = rng.uniform(30, 44)
            end = (bx + np.cos(ang) * ln, by + np.sin(ang) * ln)
            paths.append((g36.jagged((bx, by), end, rng, depth=4, rough=0.35), 0.75))
        for pts, kk in paths:
            d = poly_dist(x, y, pts)
            add(light, STORM_FRINGE, gauss(d, 9.0 * kk) * 0.30 * kk)
            add(light, STORM_GLOW, gauss(d, 3.2 * kk) * 0.85 * kk)
            add(light, WHITE, gauss(d, 1.1 * kk) * 1.6 * min(1.0, kk * 1.25))
    else:
        edge = (COLUMN / 2) * (1 - 0.65 * t)
        add(light, STORM_GLOW, np.exp(-(dx / edge) ** 6) * 0.55 * (1 - 0.8 * t))
        add(light, WHITE, np.exp(-(dx / (edge * 0.7)) ** 6) * 0.45 * (1 - t) ** 1.5)
        core = []
        yv = -6.0
        while yv < STRIKE_H + 6:
            core.append((c + rng.normal(0, 2.2), yv))
            yv += rng.uniform(8, 14)
        d = poly_dist(x, y, core)
        add(light, WHITE, gauss(d, 1.0) * 1.5 * (1 - 0.5 * t))
        add(light, STORM_GLOW, gauss(d, 3.0) * 0.5)
    return light_image(light, STRIKE_W, STRIKE_H, ss)


def lightning_set(v=STRIKE_VARIANT):
    strikes = [strike_frame(v, k) for k in range(3)] if v == "a" else \
        [strike_frame(v, k, k / 3) for k in range(4)]
    return dict(tells=[tell_frame(f) for f in range(2)], strikes=strikes)


def build_lightning(art):
    write("lightning", "lightning-tell", art["tells"])
    write("lightning", "lightning-strike", art["strikes"])
    print(f"lightning: {where('lightning')}")


def draw_tell(img, tile, x, k):
    """The tile repeated down the field at x, brightened by k (the game's alpha)."""
    t = np.array(tile).astype(np.float64)
    t[..., :3] *= k
    tile = Image.fromarray(np.round(t).astype(np.uint8), "RGBA")
    for y0 in range(0, img.height, tile.height):
        img = artkit.add_light(img, tile, (int(round(x - tile.width / 2)), y0))
    return img


def flash(img, k):
    """Level 12's sky flash stand-in: the scene lit blue-white for <= 0.1 s."""
    a = np.array(img).astype(np.float64)
    a[..., :3] = np.clip(a[..., :3] + k * np.array((70, 80, 110)), 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def strike_scene(v, art, bg, x=200, frames=None, ship_x=300):
    """The 1.0 s tell brightening, the 0.25 s strike (the flash), the column gone; a Talon in the column dies."""
    n = frames or int(FPS * 1.8)
    boom = artkit.load_frames("explosion-small")
    out = []
    t_strike = 1.0
    for i in range(n):
        t = i / FPS
        img = bg.copy()
        dead = t >= t_strike + 0.05
        if not dead:
            sprite.paste_center(img, talon(0.0), x + 6, 150 + 20 * t)
        elif (i - int((t_strike + 0.05) * FPS)) < len(boom):
            img = paste_light(img, boom[i - int((t_strike + 0.05) * FPS)], x + 6, 150 + 20 * t)
        sprite.paste_center(img, talon(0.0), x + 90, 120 + 20 * t)
        if t < t_strike:
            u = t / t_strike
            img = draw_tell(img, art["tells"][1 if u > 0.6 else 0], x, 0.4 + 0.6 * u)
        elif t < t_strike + 0.25:
            s = (t - t_strike) / 0.25
            if t - t_strike < 0.1:
                img = flash(img, 1 - (t - t_strike) / 0.1)
            if v == "a":
                frame = art["strikes"][int((t - t_strike) * 60 // 2) % len(art["strikes"])]
                k = 1.0 if s < 0.6 else (1 - s) / 0.4
            else:
                frame = art["strikes"][min(len(art["strikes"]) - 1, int(s * len(art["strikes"])))]
                k = 1.0
            f = np.array(frame).astype(np.float64)
            f[..., :3] *= k
            img = artkit.add_light(img, Image.fromarray(np.round(f).astype(np.uint8), "RGBA"),
                                   (int(round(x - frame.width / 2)), 0))
        sprite.paste_center(img, prod("ship", 2), ship_x, img.height - 60)
        out.append(img)
    return out


def lightning_review(art):
    gif36 = strike_scene("a", art, plate("l36", 360, a6.FH), x=150, ship_x=250)
    gif38 = strike_scene("a", art, plate("l38", 360, a6.FH), x=150, ship_x=250)
    gif = []
    for a, b in zip(gif38, gif36):
        row = Image.new("RGBA", (728, a6.FH), (10, 12, 26, 255))
        row.alpha_composite(a, (0, 0))
        row.alpha_composite(b, (368, 0))
        gif.append(row)
    half = [s.resize((s.width // 2, s.height // 2), Image.BILINEAR) for s in art["strikes"]]
    rows = [("LIGHTNING-TELL_0..1: 48X64 TILE (EARLY, LAST 0.4 S), REPEATED DOWN THE COLUMN (ADDITIVE, 3X)",
             art["tells"], 3, True),
            ("LIGHTNING-STRIKE_0..2: 112X540 (ROUND 73 QA-2 A, THE JAGGED BOLT), AT 0.5X", half, 1, True),
            ("1X OVER ROUND 72'S LEVEL 38 STORM: THE TELL AT 0.1 / 0.7 S, THE STRIKE AT 0.05 / 0.15 S (THE FLASH), "
             "THE TALON IN THE COLUMN DIES (60), THE ONE BESIDE IT DOES NOT",
             [gif38[k].crop((40, 40, 300, 400)) for k in (3, 21, 31, 34)], 1, False),
            ("1X OVER LEVEL 36'S BANDS (A FEW STRIKES ARE ALLOWED THERE)",
             [gif36[k].crop((40, 40, 300, 400)) for k in (21, 31, 34)], 1, False)]
    sheet = artkit.review_sheet("LIGHTNING STRIKE - FINAL SPRITES (ROUND 73 QA-2 A, PENDING THE PICK)", rows,
                                width=1300, batch=BATCH)
    save("lightning", sheet, gif, "lightning")


# =========================================================================== the pylon dome

def dome_frame(f, R=DOME_R):
    """enemies_r72.dome (a): a thin lattice at about 20 % with a brighter rim, a shimmer band crossing it."""
    D = 2 * (R + DOME_PAD)
    x, y = grid(D, D, 2)
    px, py = x - D / 2, y - D / 2
    r = np.hypot(px, py)
    inside = np.clip(R - r + 0.5, 0, 1)
    d, _ = hex_edges(px, py, DOME_CELL)
    hexl = gauss(d, 0.7)
    band = (f / DOME_FRAMES) * 3 * R - 1.5 * R
    shimmer = 0.6 + 0.8 * gauss((px * 0.8 + py * 0.6) - band, 28)
    amount = (0.05 + 0.22 * hexl * shimmer) * inside + 0.9 * gauss(r - R, 1.6)
    light = np.zeros(x.shape + (3,))
    add(light, DOME_GOLD, np.clip(amount, 0, 1) * 0.9)
    return light_image(light, D, D, 2)


def dome_hit(f, n=6):
    """The spent shot on the dome: a gold spark and the hexes round it lit, fading."""
    t = f / (n - 1)
    x, y = grid(HIT_N, HIT_N)
    c = HIT_N / 2
    r = np.hypot(x - c, y - c)
    d, (cx, cy) = hex_edges(x - c, y - c, 5.0)
    reach = 4 + 9 * t ** 0.6
    lit = np.clip((reach - np.hypot(cx, cy)) / 2.0 + 0.5, 0, 1) * (1 - t) ** 1.3
    light = np.zeros(x.shape + (3,))
    add(light, GOLD_W, gauss(d, 0.5) * lit * 1.1)
    add(light, GOLD, gauss(d, 1.2) * lit * 0.35)
    add(light, WHITE, gauss(r, 2.2) * (1 - t) ** 2.5 * 1.3)
    ang = np.arctan2(y - c, x - c)
    rays = np.abs(np.cos(2 * (ang + 0.4))) ** 24 * gauss(r, 3 + 7 * t ** 0.7) * (1 - t) ** 1.5
    add(light, GOLD_W, rays * 1.2)
    return light_image(light, HIT_N, HIT_N)


def dome_collapse(f, n=6, R=DOME_R):
    """A thin gold ring shrinking from the dome's radius to nothing (enemies_r72 ring_light, k 1 -> 0.5)."""
    u = (f + 0.5) / n
    rr = R * (1 - u)
    w = 1.4
    D = int(2 * np.ceil(rr + 4 * w + 2))
    x, y = grid(D, D, 2)
    r = np.hypot(x - D / 2, y - D / 2)
    light = np.zeros(x.shape + (3,))
    k = 1.0 * (1 - 0.5 * u)
    add(light, DOME_GOLD, gauss(r - rr, w) * k)
    add(light, GOLD_W, gauss(r - rr, w * 0.5) * k * 0.5)
    return light_image(light, D, D, 2)


def feed_strip():
    """The solid thin gold feed beam (enemies_r72.beam_light a), uniform along the strip."""
    x, y = grid(32, 12)
    d = np.abs(y - 6)
    light = np.zeros(x.shape + (3,))
    add(light, FEED_GOLD, gauss(d, 0.8) * 1.0 + gauss(d, 2.6) * 0.35)
    return light_image(light, 32, 12)


def sheen_frame(f):
    """enemies_r72.covered: a faint gold hex patch (cell 5) with a round soft edge, pulsing."""
    x, y = grid(SHEEN_N, SHEEN_N)
    c = SHEEN_N / 2
    r = np.hypot(x - c, y - c)
    d, _ = hex_edges(x - c, y - c, 5.0)
    hexl = gauss(d, 0.6)
    edge = np.clip((21 - r) / 6, 0, 1) ** 1.5
    k = (0.10 + 0.25 * hexl * (0.7 + 0.3 * np.sin(TAU * f / 4))) * edge
    light = np.zeros(x.shape + (3,))
    add(light, FEED_GOLD, k * 0.8)
    return light_image(light, SHEEN_N, SHEEN_N)


def dome_set():
    return dict(dome=[dome_frame(f) for f in range(DOME_FRAMES)], hit=[dome_hit(f) for f in range(6)],
                collapse=[dome_collapse(f) for f in range(6)], feed=feed_strip(),
                sheen=[sheen_frame(f) for f in range(4)])


def build_dome(art):
    write("shield-pylon", "shield-pylon-dome", art["dome"])
    write("shield-pylon", "shield-pylon-dome-hit", art["hit"])
    write("shield-pylon", "shield-pylon-dome-collapse", art["collapse"])
    write("shield-pylon", "shield-pylon-feed", [art["feed"]], single=True)
    write("shield-pylon", "shield-pylon-sheen", art["sheen"])
    print(f"pylon dome: {where('shield-pylon')}")


def load_dome():
    return dict(dome=load("shield-pylon", "shield-pylon-dome"), hit=load("shield-pylon", "shield-pylon-dome-hit"),
                collapse=load("shield-pylon", "shield-pylon-dome-collapse"),
                feed=load("shield-pylon", "shield-pylon-feed")[0], sheen=load("shield-pylon", "shield-pylon-sheen"))


def pylon_stand_in():
    """A stand-in mast (the pylon's production sprite is part G's): the elite black base, a gold crown ring."""
    img = Image.new("RGBA", (40, 40), (0, 0, 0, 0))
    from PIL import ImageDraw
    d = ImageDraw.Draw(img)
    d.ellipse([6, 6, 34, 34], fill=(18, 16, 22, 255), outline=(220, 170, 60, 255), width=2)
    d.ellipse([15, 15, 25, 25], fill=(60, 46, 20, 255), outline=(255, 214, 120, 255), width=1)
    return img


def dome_review(art):
    bg = plate("l41", a6.FW, 400)
    bunker = prod("rail-bunker", 0)
    turret = prod("rail-bunker-turret", 16)
    mast = pylon_stand_in()
    cx, cy = 250, 190
    face = np.radians(200)                                   # the crown's facing toward its dome
    mx, my = cx + np.cos(face) * 140, cy + np.sin(face) * 140
    rim = (cx + np.cos(face) * DOME_R, cy + np.sin(face) * DOME_R)
    covered = [(cx - 20, cy + 10), (cx + 50, cy - 40)]
    hits = [(10 + 7 * j, covered[j % 2][0] + (-1) ** j * 6, covered[j % 2][1] - 14) for j in range(8)]
    death = 80
    gif = []
    for i in range(FPS * 4):
        img = bg.copy()
        for k_, (ux, uy) in enumerate(covered):
            sprite.paste_center(img, bunker, ux, uy)
            sprite.paste_center(img, turret, ux, uy)
            if i < death:
                img = paste_light(img, art["sheen"][(i // 15) % 4], ux, uy)
        sprite.paste_center(img, mast, mx, my)
        if i < death:
            img = paste_light(img, art["dome"][(i // 20) % DOME_FRAMES], cx, cy)
            img = draw_strip(img, art["feed"], mx, my, *rim)
            for i0, hx, hy in hits:
                if 0 <= (i - i0) // 2 < 6:
                    img = paste_light(img, art["hit"][(i - i0) // 2], hx, hy)
        else:
            k = (i - death) // 4
            if k < 6:
                u = (k + 0.5) / 6
                img = paste_light(img, art["collapse"][k], cx + (mx - cx) * u, cy + (my - cy) * u)
            boom = artkit.load_frames("explosion-medium")
            if i - death < len(boom):
                img = paste_light(img, boom[i - death], mx, my)
        gif.append(img)
    rows = [("SHIELD-PYLON-DOME_0..11: 232X232 (RADIUS 110), THE SHIMMER'S 12 FRAMES; 0, 3, 6, 9 SHOWN (ADDITIVE, 1X)",
             art["dome"][::3], 1, True),
            ("SHIELD-PYLON-DOME-HIT_0..5 (32X32, 3X)", art["hit"], 3, True),
            ("SHIELD-PYLON-DOME-COLLAPSE_0..5 (0.4 S, 1X), THE FEED STRIP (32X12, 3X), THE SHEEN_0..3 (48X48, 2X)",
             art["collapse"] + [sprite.enlarge(art["feed"], 3)] + [sprite.enlarge(s, 2) for s in art["sheen"]], 1, True),
            ("1X OVER ROUND 72'S LEVEL 41 ICE: TWO COVERED RAIL BUNKERS (SHEEN), SHOTS SPENT ON THE DOME, THE FEED FROM "
             "A STAND-IN MAST; THE MAST FALLS, THE RING COLLAPSES INTO IT",
             [gif[k].crop((60, 30, 420, 370)) for k in (12, 50, death + 6, death + 16)], 1, False)]
    sheet = artkit.review_sheet("SHIELD PYLON DOME - FINAL EFFECTS (ROUND 72 A.5 A)", rows, width=1500, batch=BATCH)
    save("dome", sheet, gif, "pylon-dome")


# =========================================================================== check and main

def wanted():
    return {"plasma-arc": [(f"plasma-arc-arc-{j}", 3) for j in range(ARC_JUMPS)]
            + [("plasma-arc-spark", 6), ("plasma-arc-glow", 2)],
            "orb": [("orb-bounced", BOUNCED_HEADINGS)],
            "ship": [("ship-reflex", 4), ("ship-reflex-bolt", 4), ("ship-reflex-bolt-trail", 1)],
            "lightning": [("lightning-tell", 2), ("lightning-strike", 3)],
            "shield-pylon": [("shield-pylon-dome", DOME_FRAMES), ("shield-pylon-dome-hit", 6),
                             ("shield-pylon-dome-collapse", 6), ("shield-pylon-feed", 1), ("shield-pylon-sheen", 4)]}


def check():
    """Every written set exists with its frame count and carries the Source chunk; prints sizes and places."""
    bad = 0
    for root, sets in wanted().items():
        with sprites_of(root) as folder:
            for name, n in sets:
                files = [f for f in folder.glob(f"{name}*.png")
                         if f.stem == name or f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[1].isdigit()]
                ok = len(files) == n and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
                size = Image.open(sorted(files)[0]).size if files else None
                print(f"{'ok ' if ok else 'BAD'} {name}: {len(files)}/{n} frames {size} in {where(root)}")
                bad += not ok
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    parts = [a for a in args if not a.startswith("--")] or ["arc", "bounced", "reflex", "lightning", "dome"]
    build = "--review" not in args
    if "bounced" in parts:
        frames = bounced_set() if build else None
        if build:
            write("orb", "orb-bounced", frames)
            print("bounced bullet: assets/sprites")
        bounced_review(load("orb", "orb-bounced"))
    if "arc" in parts:
        if build:
            build_arc(arc_set())
        arc_review(load_arc(), load("orb", "orb-bounced"))
    if "reflex" in parts:
        if build:
            build_reflex(reflex_set())
        reflex_review(dict(ring=load("ship", "ship-reflex"), bolt=load("ship", "ship-reflex-bolt"),
                           trail=load("ship", "ship-reflex-bolt-trail")[0]))
    if "lightning" in parts:
        if build:
            build_lightning(lightning_set())
        lightning_review(dict(tells=load("lightning", "lightning-tell"), strikes=load("lightning", "lightning-strike")))
    if "dome" in parts:
        if build:
            build_dome(dome_set())
        dome_review(load_dome())
