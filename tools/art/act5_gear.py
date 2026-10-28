#!/usr/bin/env python3
"""Production art: the Act 5 "The Belt" systems of M9 part A (step ART-A; final review in concept round 61):
the Tesla Coil Pod's arc and its wing pod, the Ascendancy's hex shield effects, the homing diamond (round 60
A.16 a) and the Swarm Tail's micro-missile with the warmer exhaust. The vacuum explosion set is
tools/art/explosions.py ``--vacuum``, the two hangar icons tools/art/icons.py ``--act5``.

Two looks round 60 did not show were a/b proposals of round 61 (the gap check m9a §7; the picks in ``ARC_VARIANT``,
``SHIELD_VARIANT``): the arc a, the shield a (round 61 F.2, the recommendation was b; production in round 64, ART-F):
  arc    a "blue-white" (recommended): a jagged white core in a blue-white glow with a violet fringe, the
           player's cool colours (readability rule 2: the player's fire is never gold);
         b "white-gold": the same shapes white-gold, matching the hex shield it breaks.
  shield a "hex lattice" (picked, written): a thin gold hex lattice over the round bubble with a brighter rim; a hit
           flashes the hexes where it lands. Round 61's sheet drew the lattice evenly at 40 %; the production
           frames (round 64) fade it toward the bubble's middle, 12 % over the unit to 38 % at the rim
           (``LATTICE_*``), so it stays thin over the Gilded Gunship (round 60 A.3's worry);
         b "gold film" (rejected in round 61): a smooth gold film, a bright rim and a soft top-left highlight,
           hexes only where a hit lands.
The round-60 picks (Talon, Hornet and Gunship b) all wear the shield as a round bubble, so the shield is a
circle per size tier, not the hull's outline.

Outputs (assets/sprites/; ``additive`` = premultiplied on black, blended GL_SRC_ALPHA, GL_ONE; ``normal`` =
straight alpha; frame times in game steps of 1/60 s; headings k x 360/n degrees clockwise from up):

  Tesla Coil Pod (design/player/weapons/tesla-coil-pod)
  tesla-coil-pod-arc_0..2           128x24 additive: the arc from the pod (the strip's left end, (0, 12)) to the
                                    target (the right end, (127, 12)): three jagged shapes, one per arc in turn;
                                    the game stretches the strip to the distance and turns it to the direction
                                    (length / 128 along x, y unscaled), drawn 0.08 s (5 steps)
  tesla-coil-pod-spark_0..5         72x72 additive, centred on the struck target: the flash and the spark burst
                                    (sparks out to about 32 px; scale it by the level's sparks radius / 32 if
                                    wanted), 2 steps a frame
  tesla-coil-pod-glow_0..1          12x12 additive: the terminal's light at the pod's tip while it arcs (the
                                    tip: the pod sprite's top centre), alternate frames each step
  pod-tesla-coil-<left|right>_0..4  the pod on each wing per banking frame (stormhawk.py's cut; offsets in
                                    pivots/pods.json): a dark pod with two copper coil rings, a white ceramic
                                    insulator and a glowing terminal sphere
  Enemy hex shield (design/enemies, Enemy shields; tiers by the unit's size: small <= 48 px (52 px bubble),
  medium <= 84 px (92 px), large <= 104 px (116 px); the Iron Sovereign's 80 px core: medium)
  enemy-shield-<tier>_0..3          D x D additive, centred on the unit: the shield holding, a 4-frame shimmer
                                    loop, 6 steps a frame; the game may scale its alpha by the pool's fill
  enemy-shield-hit_0..5             28x28 additive: the hexes lit where a hit lands, centred on the bubble's rim
                                    point nearest the hit (one size for every tier), 2 steps a frame
  enemy-shield-<tier>-break_0..5    (D + 40)^2 additive: emptied, the bubble flashes and shatters into a ring of
                                    gold fragments (0.2 s: 2 steps a frame)
  enemy-shield-<tier>-refill_0..7   D x D additive: the refill's shimmer in from the edge (0.4 s: 3 steps a
                                    frame), ending on the holding look
  Homing diamond (design/art-direction, round 60 A.16 a; every Act 5 homing missile)
  homing-diamond_0..15              18x18 normal: the crimson diamond with its dark rim, gold heart and white-hot
                                    core (the hit box), nose along the heading, centre (9, 9)
  homing-diamond-exhaust_0..31      28x28 additive: its short thin pale-gold exhaust, index heading x 2 +
                                    flicker (alternate every 3 steps), drawn under the diamond on its centre
  homing-diamond-pop_0..4           24x24 additive: shot down, a spark pop, 4 steps a frame
  homing-diamond-fizzle_0..4        24x24 additive: at the end of its life a faint ring, 5 steps a frame
  Swarm Tail (design/player/weapons/swarm-tail)
  swarm-tail-shot_0..31             24x24 normal: the micro-missile (weapon_fx.py's model) at 32 headings with a
                                    warmer, ivory-white plume (the wing pods' is cyan), centre (12, 12)
  The Swarm Tail has no mount of its own: rear guns fire from the hull's rear mount (pivots/ship.json).

Held sprites (as act4_gear.py): a frame set whose root the game data does not claim yet is written to
build/art-held/sprites/ instead: ``tesla-coil-pod-*`` and ``swarm-tail-*`` until their weapon data.yaml
exists (step A2), ``enemy-shield-*`` and ``homing-diamond-*`` until pipeline/.../SpriteUse.java names their
roots (step A3); rerun the part then. ``pod-*`` is always claimed.

Review sheets and GIFs of round 61: design/player/weapons/tesla-coil-pod/concept/tesla-arc-final-r61-<a|b>,
tesla-coil-pod-final-r61-a (the pod), design/enemies/concept/enemy-shield-final-r61-<a|b> (``shield --r61``: the
even lattice over round 60's stand-ins; b's files belong in concept/rejected/), homing-diamond-final-r61-a,
design/player/weapons/swarm-tail/concept/swarm-tail-final-r61-a. Round 64: design/enemies/concept/
enemy-shield-final-r64-a (the production shield over the production Talon and Gunship, 1x and 2x).

Run: python3 tools/art/act5_gear.py [arc] [pod] [shield] [diamond] [swarm] [--review] [--check] [--r61]
     (no part = all, ~1 min; --review only rebuilds the sheets; --check lists every written frame set
     with its size and frame count and fails on a missing Source chunk)
"""
import contextlib
import sys

import numpy as np
from PIL import Image, ImageDraw

import act3kit
import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r60 as e60  # noqa: E402  (concept scripts, imported unchanged)
import gear_r36 as g36  # noqa: E402
import vfx_r08 as v8  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.sdf import sd_box, sd_capsule, sd_sphere, union  # noqa: E402

SCRIPT = "act5_gear.py"
BATCH = "M9 part A batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r61"
artkit.REVIEW_ROUND = ROUND
SPRITE_USE = ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java"
WEAPONS = DESIGN / "player" / "weapons"
CONCEPT = {"arc": WEAPONS / "tesla-coil-pod" / "concept", "pod": WEAPONS / "tesla-coil-pod" / "concept",
           "shield": DESIGN / "enemies" / "concept", "diamond": DESIGN / "enemies" / "concept",
           "swarm": WEAPONS / "swarm-tail" / "concept"}
ARC_VARIANT = "a"       # round 61 (recommended a: the player's fire stays cool)
SHIELD_VARIANT = "a"    # round 61 F.2 a, the hex lattice (b, the gold film, rejected); to production in round 64
SHIELD_ROUND = "r64"    # the production shield's review sheet (round 61's two: ``shield --r61``)
SS = 4
WHITE = np.array((255, 255, 255), float)
GOLD = np.array((255, 206, 90), float)
GOLD_W = np.array((255, 240, 200), float)
ARC_COL = {"a": dict(glow=np.array((140, 200, 255), float), fringe=np.array((150, 80, 255), float)),
           "b": dict(glow=np.array((255, 226, 150), float), fringe=np.array((255, 160, 50), float))}
ARC_LEN, ARC_H = 128, 24
SPARK = 72
TIERS = {"small": 52, "medium": 92, "large": 116}
BREAK_PAD = 20
HEX_CELL = 5.0
LATTICE_FALLOFF = 1.0   # round 64 (production a): the lattice fades toward the bubble's middle; 0 = round 61's sheets
LATTICE_CORE, LATTICE_RIM, LATTICE_W = 0.12, 0.38, 0.40   # its strength over the unit and at the rim; line width
DIAMOND, DIAMOND_HEADINGS = 18, 16
EXHAUST = 28
SWARM_HEADINGS = 32
PLUME = np.array((255, 228, 186), float)   # ivory-white: warmer than the pods' cyan, still no enemy hue
FPS = 30
FW, FH = 480, 540


# =========================================================================== small kit

def grid(w, h):
    """Supersampled pixel-centre coordinates in native pixels."""
    yy, xx = np.mgrid[0:h * SS, 0:w * SS].astype(np.float64)
    return (xx + 0.5) / SS, (yy + 0.5) / SS


def gauss(d, w):
    return np.exp(-(d / max(w, 1e-6)) ** 2)


def seg(x, y, x0, y0, x1, y1):
    px, py = x - x0, y - y0
    bx, by = x1 - x0, y1 - y0
    h = np.clip((px * bx + py * by) / max(bx * bx + by * by, 1e-9), 0, 1)
    return np.hypot(px - bx * h, py - by * h)


def poly_dist(x, y, pts):
    return np.min([seg(x, y, *a, *b) for a, b in zip(pts[:-1], pts[1:])], axis=0)


def light_image(light, w, h):
    """A supersampled light field (0..255 RGB) -> an additive frame (premultiplied, alpha where lit)."""
    lit = np.clip(light.reshape(h, SS, w, SS, 3).mean(axis=(1, 3)), 0, 255)
    alpha = np.where(lit.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([lit, alpha]).astype(np.uint8), "RGBA")


def add(light, col, amount):
    light += np.asarray(col, float)[None, None, :] * np.clip(amount, 0, None)[..., None]


def hex_edges(x, y, cell=HEX_CELL, ox=0.0, oy=0.0):
    """Distance (native px) to the nearest edge of a pointy-top hex lattice of ``cell`` circumradius."""
    q = np.stack([x - ox, y - oy], axis=-1)
    w, h = np.sqrt(3) * cell, 1.5 * cell
    best = np.full(x.shape, np.inf)
    centre = None
    row = np.floor(q[..., 1] / h)
    for dr in (-1, 0, 1):
        r = row + dr
        shift = np.where(r % 2 == 1, w / 2, 0.0)
        col = np.round((q[..., 0] - shift) / w)
        for dc in (-1, 0, 1):
            cx = (col + dc) * w + shift
            cy = r * h
            d = np.hypot(q[..., 0] - cx, q[..., 1] - cy)
            better = d < best
            best = np.where(better, d, best)
            centre = np.where(better[..., None], np.stack([cx, cy], -1), centre if centre is not None
                              else np.stack([cx, cy], -1))
    # distance to the hexagon's edge from inside: apothem minus the hex norm of the offset
    dx, dy = np.abs(q[..., 0] - centre[..., 0]), np.abs(q[..., 1] - centre[..., 1])
    norm = np.maximum(dx, dx / 2 + dy * np.sqrt(3) / 2)        # pointy-top hex: edge normals at 0, +-60 deg
    apothem = cell * np.sqrt(3) / 2
    return np.abs(apothem - norm), centre


def claimed(root):
    if root in ("tesla-coil-pod", "swarm-tail"):
        return act3kit.claimed(root)
    if root == "pod":
        return True
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


def load(root, name):
    with sprites_of(root):
        return artkit.load_frames(name)


def save(kind, sheet, gif_frames, subject, variant="a", fps=FPS):
    png, gif = artkit.review_paths(CONCEPT[kind], subject, variant)
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(gif_frames, gif, fps=fps, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def belt_plate(w, h, seed=3):
    """A belt space crop (round 60's lighter grade) as the review ground."""
    return e60.space_bg(seed, max(h, 300)).crop((0, 0, w, h)).convert("RGBA")


def paste_light(img, frame, cx, cy):
    return artkit.add_light(img, frame, (int(round(cx - frame.width / 2)), int(round(cy - frame.height / 2))))


# =========================================================================== the Tesla arc

def jagged(rng, p0, p1, depth=5, rough=0.24):
    return g36.jagged(p0, p1, rng, depth=depth, rough=rough)


def arc_frame(v, k):
    rng = np.random.default_rng(6100 + 17 * k)
    x, y = grid(ARC_LEN, ARC_H)
    light = np.zeros(x.shape + (3,))
    c = ARC_COL[v]
    pts = [(px, float(np.clip(py, 4, ARC_H - 4))) for px, py in jagged(rng, (0.0, 12.0), (ARC_LEN, 12.0))]
    d = poly_dist(x, y, pts)
    add(light, c["fringe"], gauss(d, 3.4) * 0.38)
    add(light, c["glow"], gauss(d, 1.5) * 0.85)
    add(light, WHITE, gauss(d, 0.55) * 1.3)
    for _ in range(3):                                   # short forks off the main line
        i = int(rng.integers(4, len(pts) - 6))
        bx, by = pts[i]
        ang = rng.uniform(0.5, 1.0) * rng.choice((-1, 1))
        length = rng.uniform(9, 18)
        end = (bx + np.cos(ang) * length, float(np.clip(by + np.sin(ang) * length, 3, ARC_H - 3)))
        fork = jagged(rng, (bx, by), end, depth=3, rough=0.3)
        fd = poly_dist(x, y, fork)
        add(light, c["fringe"], gauss(fd, 2.0) * 0.25)
        add(light, c["glow"], gauss(fd, 0.9) * 0.6)
        add(light, WHITE, gauss(fd, 0.4) * 0.6)
    return light_image(light, ARC_LEN, ARC_H)


def spark_frame(v, f, n=6):
    t = f / (n - 1)
    rng = np.random.default_rng(6200)
    x, y = grid(SPARK, SPARK)
    cx = cy = SPARK / 2
    r = np.hypot(x - cx, y - cy)
    light = np.zeros(x.shape + (3,))
    c = ARC_COL[v]
    add(light, WHITE, gauss(r, 2.5 + 3 * t) * 1.6 * (1 - t) ** 2)
    add(light, c["glow"], gauss(r, 7 + 8 * t) * 0.55 * (1 - t) ** 1.5)
    add(light, c["fringe"], gauss(r - 30 * t ** 0.6, 1.6) * 0.30 * (1 - t))   # the sparks' reach
    for j in range(8):
        a = TAU * j / 8 + rng.uniform(-0.3, 0.3)
        r0 = 3 + 16 * t ** 0.7
        r1 = min(33.0, r0 + rng.uniform(8, 15) * (1 - 0.4 * t))
        p0 = (cx + np.cos(a) * r0, cy + np.sin(a) * r0)
        p1 = (cx + np.cos(a) * r1, cy + np.sin(a) * r1)
        sp = jagged(np.random.default_rng(6300 + j * 5 + f), p0, p1, depth=3, rough=0.35)
        d = poly_dist(x, y, sp)
        fade = (1 - t) ** 1.2
        add(light, c["glow"], gauss(d, 1.1) * 0.7 * fade)
        add(light, WHITE, gauss(d, 0.45) * 0.9 * fade)
    return light_image(light, SPARK, SPARK)


def glow_frame(v, f):
    x, y = grid(12, 12)
    r = np.hypot(x - 6, y - 6)
    light = np.zeros(x.shape + (3,))
    c = ARC_COL[v]
    k = 1.0 if f == 0 else 0.75
    add(light, c["glow"], gauss(r, 3.2) * 0.8 * k)
    add(light, WHITE, gauss(r, 1.2) * 1.2 * k)
    return light_image(light, 12, 12)


def arc_set(v):
    arcs = act3kit.quantize_bright([arc_frame(v, k) for k in range(3)], 24)
    sparks = act3kit.quantize_bright([spark_frame(v, f) for f in range(6)], 32)
    glows = act3kit.quantize_bright([glow_frame(v, f) for f in range(2)], 12)
    return dict(arcs=arcs, sparks=sparks, glows=glows)


def build_arc(v):
    art = arc_set(v)
    with sprites_of("tesla-coil-pod"):
        artkit.write_frames("tesla-coil-pod-arc", art["arcs"], SOURCE)
        artkit.write_frames("tesla-coil-pod-spark", art["sparks"], SOURCE)
        artkit.write_frames("tesla-coil-pod-glow", art["glows"], SOURCE)
    print(f"tesla arc {v}: {where('tesla-coil-pod')}")


def draw_arc(img, strip, x0, y0, x1, y1):
    """The arc strip stretched from (x0, y0) to (x1, y1), as the game draws it."""
    length = max(4, int(round(np.hypot(x1 - x0, y1 - y0))))
    s = strip.resize((length, strip.height), Image.BILINEAR)
    ang = np.degrees(np.arctan2(-(y1 - y0), x1 - x0))
    s = s.rotate(ang, resample=Image.BILINEAR, expand=True)
    return paste_light(img, s, (x0 + x1) / 2, (y0 + y1) / 2)


# =========================================================================== the hex shield

def shield_light(v, D, frame=0, front=None, flash=0.0, falloff=None):
    """The holding shield of diameter D (frame of the shimmer loop); ``front`` 0..1 shows only the part
    outside a front moving in from the rim (the refill); ``flash`` brightens it all (the break); ``falloff`` 1 = the
    production lattice (fading toward the centre), 0 = round 61's even one."""
    falloff = LATTICE_FALLOFF if falloff is None else falloff
    x, y = grid(D, D)
    c = D / 2
    R = D / 2 - 2
    r = np.hypot(x - c, y - c)
    inside = np.clip((R - r) * 2 + 0.5, 0, 1)
    light = np.zeros(x.shape + (3,))
    phase = frame / 4
    band = 0.75 + 0.25 * np.cos(TAU * (((x + y) / (2.2 * D)) - phase))      # a slow shimmer across
    shown = np.ones(x.shape)
    edge_k = 1.0
    if front is not None:
        rf = R * (1 - front)
        shown = np.clip((r - rf) / 1.5 + 0.5, 0, 1)
        add(light, GOLD_W, gauss(r - rf, 1.6) * 0.9 * (1 - front) ** 0.5 * (front < 1))
        edge_k = min(1.0, 0.3 + front * 1.5)
    k = 1 + flash
    rim = gauss(r - R, 0.9)
    add(light, GOLD, rim * 0.72 * band * edge_k * k)
    if v == "a":
        d, _ = hex_edges(x, y, ox=c, oy=c)
        lattice = gauss(d, LATTICE_W if falloff else 0.45) * inside
        # the bubble seen face on: the lattice faint over the unit, strongest toward the rim (round 61's sheets: flat)
        weight = (1 - falloff) * 0.40 + falloff * (LATTICE_CORE + (LATTICE_RIM - LATTICE_CORE) * np.clip(r / R, 0, 1) ** 2)
        add(light, GOLD, lattice * weight * band * shown * k)
    else:
        film = (r / R) ** 4 * inside
        add(light, GOLD, film * 0.20 * band * shown * k)
        hx, hy = c - R * 0.42, c - R * 0.42                               # the top-left highlight
        hl = gauss(np.hypot(x - hx, y - hy), R * 0.22)
        add(light, GOLD_W, hl * 0.16 * inside * (0.8 + 0.2 * np.cos(TAU * phase)) * shown * k)
    if flash:
        add(light, GOLD_W, inside * 0.12 * flash)
    return light_image(light, D, D)


def hit_frame(f, n=6):
    t = f / (n - 1)
    W = 28
    x, y = grid(W, W)
    r = np.hypot(x - W / 2, y - W / 2)
    d, centre = hex_edges(x, y, ox=W / 2, oy=W / 2)
    cell_r = np.hypot(centre[..., 0], centre[..., 1])          # centres are relative to the origin
    reach = 3 + 9 * t ** 0.6
    lit = np.clip((reach - cell_r) / 2.0 + 0.5, 0, 1) * (1 - t) ** 1.3
    light = np.zeros(x.shape + (3,))
    add(light, GOLD_W, gauss(d, 0.5) * lit * 1.2)
    add(light, GOLD, gauss(d, 1.2) * lit * 0.35)
    add(light, WHITE, gauss(r, 2.0) * (1 - t) ** 2.5 * 1.2)
    return light_image(light, W, W)


def break_frame(v, D, f, n=6):
    t = f / (n - 1)
    W = D + 2 * BREAK_PAD
    x, y = grid(W, W)
    c = W / 2
    R = D / 2 - 2
    r = np.hypot(x - c, y - c)
    light = np.zeros(x.shape + (3,))
    if f == 0:
        base = np.array(shield_light(v, D, flash=1.2)).astype(np.float64)
        img = Image.new("RGBA", (W, W), (0, 0, 0, 0))
        img.paste(Image.fromarray(base.astype(np.uint8), "RGBA"), (BREAK_PAD, BREAK_PAD))
        return img
    add(light, GOLD, gauss(r - (R + 14 * t), 1.4 + 2 * t) * 0.5 * (1 - t) ** 1.5)
    rng = np.random.default_rng(6400 + D)
    count = max(12, int(D * 0.45))
    for j in range(count):
        a = TAU * j / count + rng.uniform(-0.12, 0.12)
        sp = rng.uniform(0.7, 1.15)
        rr = R + (BREAK_PAD - 3) * sp * (1 - (1 - t) ** 1.6)
        fx, fy = c + np.cos(a) * rr, c + np.sin(a) * rr
        spin = a + 1.2 + rng.uniform(-1, 1) + 4 * t * rng.choice((-1, 1))
        half = rng.uniform(1.2, 2.2)
        dd = seg(x, y, fx - np.cos(spin) * half, fy - np.sin(spin) * half, fx + np.cos(spin) * half,
                 fy + np.sin(spin) * half)
        fade = (1 - t) ** 1.1
        add(light, GOLD_W, gauss(dd, 0.5) * 1.1 * fade)
        add(light, GOLD, gauss(dd, 1.2) * 0.35 * fade)
    return light_image(light, W, W)


def shield_set(v):
    out = {}
    for tier, D in TIERS.items():
        idle = act3kit.quantize_bright([shield_light(v, D, frame=f) for f in range(4)], 24)
        brk = act3kit.quantize_bright([break_frame(v, D, f) for f in range(6)], 24)
        refill = act3kit.quantize_bright([shield_light(v, D, frame=0, front=min(1.0, (f + 1) / 7)) if f < 7
                                          else shield_light(v, D, frame=0) for f in range(8)], 24)
        out[tier] = dict(idle=idle, brk=brk, refill=refill)
    out["hit"] = act3kit.quantize_bright([hit_frame(f) for f in range(6)], 16)
    return out


def build_shield(v):
    art = shield_set(v)
    with sprites_of("enemy-shield"):
        for tier in TIERS:
            artkit.write_frames(f"enemy-shield-{tier}", art[tier]["idle"], SOURCE)
            artkit.write_frames(f"enemy-shield-{tier}-break", art[tier]["brk"], SOURCE)
            artkit.write_frames(f"enemy-shield-{tier}-refill", art[tier]["refill"], SOURCE)
        artkit.write_frames("enemy-shield-hit", art["hit"], SOURCE)
    print(f"enemy shield {v}: {where('enemy-shield')}")


# =========================================================================== the homing diamond

def diamond_hi(k=8):
    """Round 60 A.16 a's diamond (tools/concept/enemies_r60.py ``_diamond_hi``'s shapes, without the exhaust
    line, which is its own sprite here), nose up, centred on the widest point, drawn at k x."""
    W = DIAMOND * k
    im = Image.new("RGBA", (W, W), (0, 0, 0, 0))
    d = ImageDraw.Draw(im)
    c = W / 2
    nose, tail, half = 7.5, 5.5, 4.6
    outer = [(c, c - nose * k), (c + half * k, c), (c, c + tail * k), (c - half * k, c)]
    d.polygon([(c + (px - c) * 1.3, c + (py - c) * 1.22) for px, py in outer], fill=(46, 6, 8, 255))
    d.polygon(outer, fill=(226, 40, 36, 255))
    inner = [(c + (px - c) * 0.55, c + (py - c) * 0.55 - 0.6 * k) for px, py in outer]
    d.polygon(inner, fill=(255, 196, 64, 255))
    d.ellipse([c - 1.1 * k, c - 2.4 * k, c + 1.1 * k, c - 0.2 * k], fill=(255, 244, 214, 255))
    d.polygon([(c - half * 0.5 * k, c - nose * 0.5 * k), (c, c - nose * k), (c - 0.8 * k, c - nose * 0.45 * k)],
              fill=(255, 120, 96, 255))                                # a lit top-left facet
    return im


def diamond_frame(k):
    hi = diamond_hi().rotate(-k * 360 / DIAMOND_HEADINGS, resample=Image.BICUBIC)
    sp = hi.resize((DIAMOND, DIAMOND), Image.LANCZOS)
    a = np.array(sp)
    a[..., 3] = np.where(a[..., 3] > 90, 255, 0)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


def exhaust_frame(k, f):
    h = TAU * k / DIAMOND_HEADINGS
    x, y = grid(EXHAUST, EXHAUST)
    c = EXHAUST / 2
    dx, dy = np.sin(h), -np.cos(h)
    length = 7.0 if f == 0 else 5.5
    x0, y0 = c - dx * 5.0, c - dy * 5.0
    d = seg(x, y, x0, y0, x0 - dx * length, y0 - dy * length)
    along = np.clip(((x0 - x) * dx + (y0 - y) * dy) / length, 0, 1)
    light = np.zeros(x.shape + (3,))
    add(light, (255, 222, 150), gauss(d, 0.75) * (1 - along) ** 1.2 * 1.1)
    add(light, (255, 160, 80), gauss(d, 1.6) * (1 - along) * 0.25)
    return light_image(light, EXHAUST, EXHAUST)


def pop_frame(f, n=5):
    t = f / (n - 1)
    x, y = grid(24, 24)
    r = np.hypot(x - 12, y - 12)
    light = np.zeros(x.shape + (3,))
    add(light, WHITE, gauss(r, 2.2 + 2 * t) * 1.4 * (1 - t) ** 2)
    add(light, (255, 210, 140), gauss(r, 4 + 3 * t) * 0.5 * (1 - t) ** 1.5)
    for j in range(6):
        a = TAU * j / 6 + 0.4
        r0, r1 = 2 + 6 * t, 4 + 8 * t
        d = seg(x, y, 12 + np.cos(a) * r0, 12 + np.sin(a) * r0, 12 + np.cos(a) * r1, 12 + np.sin(a) * r1)
        add(light, (255, 222, 150), gauss(d, 0.5) * (1 - t) * 1.1)
    return light_image(light, 24, 24)


def fizzle_frame(f, n=5):
    t = f / (n - 1)
    x, y = grid(24, 24)
    r = np.hypot(x - 12, y - 12)
    light = np.zeros(x.shape + (3,))
    add(light, (200, 190, 170), gauss(r - (2 + 7 * t), 0.7) * 0.8 * (1 - t))
    add(light, (255, 222, 150), gauss(r, 1.5) * 0.6 * (1 - t) ** 3)
    return light_image(light, 24, 24)


def diamond_set():
    bodies = artkit.quantize_set([diamond_frame(k) for k in range(DIAMOND_HEADINGS)], 16)
    ex = act3kit.quantize_bright([exhaust_frame(k, f) for k in range(DIAMOND_HEADINGS) for f in range(2)], 12)
    pops = act3kit.quantize_bright([pop_frame(f) for f in range(5)], 16)
    fizz = act3kit.quantize_bright([fizzle_frame(f) for f in range(5)], 12)
    return dict(bodies=bodies, exhaust=ex, pops=pops, fizzles=fizz)


def build_diamond():
    art = diamond_set()
    with sprites_of("homing-diamond"):
        artkit.write_frames("homing-diamond", art["bodies"], SOURCE)
        artkit.write_frames("homing-diamond-exhaust", art["exhaust"], SOURCE)
        artkit.write_frames("homing-diamond-pop", art["pops"], SOURCE)
        artkit.write_frames("homing-diamond-fizzle", art["fizzles"], SOURCE)
    print(f"homing diamond: {where('homing-diamond')}")


# =========================================================================== the Swarm Tail

def swarm_missile(heading):
    """weapon_fx.py ``missile`` with the plume in ivory-white instead of the player shots' cyan."""
    body = v8._obj(v8.missile_model, 10, 1.9, heading=-heading)
    size = 24
    cv = v8.Canvas(size, size)
    cx = cy = size / 2
    dx, dy = np.sin(heading), -np.cos(heading)
    tail_x, tail_y = cx - dx * 4, cy - dy * 4
    length = 7
    d = cv.seg(tail_x, tail_y, tail_x - dx * length, tail_y - dy * length)
    along = np.clip(((tail_x - cv.x) * dx + (tail_y - cv.y) * dy) / length, 0, 1)
    cv.add(PLUME, v8.gauss(d, 1.6 * (1 - 0.5 * along)) * (1 - along) * 1.2)
    cv.add(v8.WHITE, v8.gauss(d, 0.6) * (1 - along) ** 2)
    out = cv.image()
    out.alpha_composite(body, ((size - body.width) // 2, (size - body.height) // 2))
    return out


def build_swarm():
    frames = artkit.quantize_set([swarm_missile(TAU * k / SWARM_HEADINGS) for k in range(SWARM_HEADINGS)], 32)
    with sprites_of("swarm-tail"):
        artkit.write_frames("swarm-tail-shot", frames, SOURCE)
    print(f"swarm tail: {where('swarm-tail')}")
    return frames


# =========================================================================== the Tesla pod

def tesla_pod_model(v):
    """(scene, materials) in the right wing pod's local units (origin at the mount, +y forward, z up)."""
    import stormhawk
    copper = v8.mat((214, 120, 60), metal=0.7, shininess=60, spec=0.9)
    ceramic = v8.mat((226, 230, 240), metal=0.05, shininess=70, spec=0.8)
    glow = v8.m_glow(tuple(int(c) for c in ARC_COL[v]["glow"]), 1.8)
    mats = list(stormhawk.POD_MATS) + [copper, ceramic, glow]
    G, BODY, TIP, ACC, COPPER, CERAMIC, GLOW = range(7)

    def ring(p, yc, radius=0.088, tube=0.026):
        q = np.sqrt(p[:, 0] ** 2 + p[:, 2] ** 2) - radius
        return np.sqrt(q ** 2 + (p[:, 1] - yc) ** 2) - tube

    def scene(p):
        return union(
            (sd_capsule(p, (0, -0.25, 0), (0, 0.04, 0), 0.08, 0.072), BODY),
            (sd_box(p, (0, -0.2, -0.02), (0.03, 0.05, 0.03), 0.01), G),
            (ring(p, -0.13), COPPER), (ring(p, -0.04), COPPER),
            (sd_capsule(p, (0, 0.04, 0), (0, 0.13, 0), 0.042, 0.032), CERAMIC),
            (sd_sphere(p, (0, 0.18, 0.0), 0.058), GLOW),
            k=0.01)
    return scene, mats


def pod_set(v):
    return act3kit.render_pods("act5_gear", "tesla_pod_model", v, 1.0)


def build_pod(v):
    frames, offsets = pod_set(v)
    act3kit.write_pods("tesla-coil", frames, offsets, SOURCE)
    print(f"tesla pod {v}: assets/sprites (pod-tesla-coil-*), pivots/pods.json")
    return frames, offsets


def pod_composite(bank, frames, offsets):
    hull = artkit.load_frames("ship")[bank]
    img = Image.new("RGBA", hull.size, (0, 0, 0, 0))
    img.alpha_composite(hull)
    for side in ("left", "right"):
        img.alpha_composite(frames[side][bank], tuple(offsets[side][bank]))
    return img


def pod_tips(offsets, frames, bank=2):
    """The terminal's tip (the pod sprite's top centre) of each side, relative to the hull's top-left."""
    return {side: (offsets[side][bank][0] + frames[side][bank].width / 2, offsets[side][bank][1] + 1)
            for side in ("left", "right")}


# =========================================================================== review scenes

def stand_ins():
    return dict(small=e60.talons[("b", False)].get(e60.DOWN), medium=e60.hornets["b"].get(e60.DOWN),
                large=e60.gunships["b"].get(e60.DOWN))


def arc_review(v, art, pods=None):
    frames, offsets = pods if pods else pod_set(v)
    hull = pod_composite(2, frames, offsets)
    tips = pod_tips(offsets, frames)
    shield = shield_set(SHIELD_VARIANT)
    units = stand_ins()
    W, H = 320, 300
    plate = belt_plate(W, H, 7)
    sx, sy = 160, 230
    targets = [dict(x=90, y=110, vx=14, sp=units["small"], shielded=True, hp=6.0, sh=2.0, broke=None),
               dict(x=225, y=125, vx=-10, sp=units["small"], shielded=False, hp=6.0, sh=0.0, broke=None),
               dict(x=160, y=60, vx=0, sp=units["medium"], shielded=True, hp=99.0, sh=8.0, broke=None)]
    gif, arcs_on, sparks = [], [], []
    nxt = {"left": 0.0, "right": 0.12}
    for i in range(FPS * 4):
        t = i / FPS
        img = plate.copy()
        for tg in targets:
            tg["x"] += tg["vx"] / FPS
        for side in ("left", "right"):
            if t >= nxt[side]:
                nxt[side] += 0.25
                ox, oy = sx - hull.width / 2 + tips[side][0], sy - hull.height / 2 + tips[side][1]
                live = [tg for tg in targets if tg["hp"] > 0 and np.hypot(tg["x"] - ox, tg["y"] - oy) <= 190]
                if not live:
                    continue
                pick = sorted(live, key=lambda tg: (not (tg["shielded"] and tg["sh"] > 0),
                                                    np.hypot(tg["x"] - ox, tg["y"] - oy)))[0]
                arcs_on.append((i, ox, oy, pick, len(arcs_on) % 3))
                sparks.append((i, pick))
                if pick["sh"] > 0:
                    pick["sh"] = 0.0
                    pick["broke"] = i
                pick["hp"] -= 3
        for tg in targets:
            if tg["hp"] > 0:
                sprite.paste_center(img, tg["sp"], tg["x"], tg["y"])
                tier = "small" if tg["sp"].width <= 48 else "medium"
                if tg["shielded"] and tg["sh"] > 0:
                    img = paste_light(img, shield[tier]["idle"][(i // 3) % 4], tg["x"], tg["y"])
                elif tg["broke"] is not None and i - tg["broke"] < 6:
                    img = paste_light(img, shield[tier]["brk"][i - tg["broke"]], tg["x"], tg["y"])
            elif tg.get("dead") is None:
                tg["dead"] = i
            if tg.get("dead") is not None and i - tg["dead"] < 12:
                img = paste_light(img, e60.EXPL["small"][i - tg["dead"]], tg["x"], tg["y"])
        sprite.paste_center(img, sprite.shadow_of(hull, opacity=0.5, blur=1.0, scale=0.85), sx + 18, sy + 26)
        sprite.paste_center(img, hull, sx, sy)
        for i0, ox, oy, tg, shape in arcs_on:
            if 0 <= i - i0 < 3:
                img = draw_arc(img, art["arcs"][(shape + (i - i0)) % 3], ox, oy, tg["x"], tg["y"])
                img = paste_light(img, art["glows"][(i - i0) % 2], ox, oy)
        for i0, tg in sparks:
            if 0 <= i - i0 < 6:
                img = paste_light(img, art["sparks"][i - i0], tg["x"], tg["y"])
        gif.append(img)
    big = [g.resize((W * 2 // 1, H * 2 // 1), Image.NEAREST) for g in gif[::1]]
    name = {"a": "A BLUE-WHITE, VIOLET FRINGE", "b": "B WHITE-GOLD"}[v]
    rows = [("TESLA-COIL-POD-ARC_0..2: 128X24 STRIP, POD AT THE LEFT END, TARGET AT THE RIGHT (ADDITIVE)",
             art["arcs"], 3, True),
            ("TESLA-COIL-POD-SPARK_0..5: 72X72 BURST AT THE TARGET, 2 STEPS A FRAME", art["sparks"], 2, True),
            ("TESLA-COIL-POD-GLOW_0..1 AT THE TERMINAL; THE ARC STRETCHED TO 60 / 140 / 190 PX",
             art["glows"] + [arc_stretch(art["arcs"][0], n) for n in (60, 140, 190)], 2, True),
            ("IN FLIGHT (1X, STAND-INS: ROUND 60'S TALON B AND HORNET B, BLACK-GOLD CONCEPT LIVERY): THE SHIELDED "
             "UNITS PICKED FIRST", [gif[k].crop((20, 20, 300, 280)) for k in (8, 30, 60, 100)], 1, False)]
    sheet = artkit.review_sheet(f"TESLA COIL POD ARC - PROPOSAL {name} (ROUND 61)", rows, width=1300, batch=BATCH)
    save("arc", sheet, big, "tesla-arc", v)


def arc_stretch(strip, n):
    s = strip.resize((n, strip.height), Image.BILINEAR)
    return s


def pod_review(v, frames, offsets):
    rows = [("POD-TESLA-COIL ON EVERY BANK FRAME (HULL + POD SPRITES AT THEIR PIVOTS)",
             [pod_composite(b, frames, offsets) for b in range(5)], 4, False),
            ("POD-TESLA-COIL-LEFT / -RIGHT, LEVEL FRAME", [frames["left"][2], frames["right"][2]], 6, False),
            ("BESIDE THE MICRO-MISSILE AND DEFLECTOR PODS (LEVEL FRAME)",
             [act3kit.pod_composite(2, "micro-missile"), act3kit.pod_composite(2, "deflector"),
              pod_composite(2, frames, offsets)], 4, False)]
    sheet = artkit.review_sheet("TESLA COIL POD - THE WING POD (FINAL R61)", rows, width=1100, batch=BATCH)
    gif = []
    plate = belt_plate(120, 120, 11)
    glows = arc_set(v)["glows"]
    tips = pod_tips(offsets, frames)
    for i in range(40):
        bank = [2, 1, 0, 0, 1, 2, 3, 4, 4, 3][(i // 4) % 10]
        hull = pod_composite(bank, frames, offsets)
        img = plate.copy()
        sprite.paste_center(img, hull, 60, 60)
        if i % 8 < 3:
            for side in ("left", "right"):
                img = paste_light(img, glows[i % 2], 60 - hull.width / 2 + tips[side][0],
                                  60 - hull.height / 2 + tips[side][1])
        gif.append(sprite.enlarge(img, 3))
    save("pod", sheet, gif, "tesla-coil-pod", "a", fps=15)


def shield_units():
    """The round 64 sheet's units: the production Talon, Hornet (held until its data claims it; round 60's b until its
    frames exist) and Gunship, flying down; round 61's sheets used round 60's three stand-ins."""
    if SHIELD_ROUND == ROUND:
        return stand_ins()
    out = stand_ins()
    for tier, root in (("small", "talon"), ("medium", "hornet"), ("large", "gilded-gunship")):
        for folder in (artkit.SPRITES, act3kit.HELD):
            path = folder / f"{root}_8.png"
            if path.exists():
                out[tier] = Image.open(path).convert("RGBA")
                break
    return out


def shield_review(v, art):
    units = shield_units()
    before = artkit.REVIEW_ROUND
    artkit.REVIEW_ROUND = SHIELD_ROUND
    W, H = 420, 220
    plate = belt_plate(W, H, 5)
    spots = [("small", 70, 110), ("medium", 200, 110), ("large", 340, 110)]
    # timeline per unit: hits every 0.2 s from t=0.3 to t=1.4, emptied at 1.4, refill from 3.4 (2 s quiet)
    gif = []
    for i in range(int(FPS * 4.6)):
        t = i / FPS
        img = plate.copy()
        for tier, x, y in spots:
            sp = units[tier]
            sprite.paste_center(img, sp, x, y)
            D = TIERS[tier]
            k = int(t * 60)
            if t < 1.4:
                img = paste_light(img, art[tier]["idle"][(k // 6) % 4], x, y)
                if t >= 0.3 and ((t - 0.3) % 0.2) < 0.2 / 2:
                    hf = int(((t - 0.3) % 0.2) * 60 / 2)
                    ang = 2.6 + 0.4 * np.sin(t * 7)
                    hx, hy = x + np.cos(ang) * (D / 2 - 2), y - abs(np.sin(ang)) * (D / 2 - 2)
                    img = paste_light(img, art["hit"][min(hf, 5)], hx, hy)
            elif t < 1.4 + 0.2:
                img = paste_light(img, art[tier]["brk"][min(5, int((t - 1.4) * 30))], x, y)
            elif t >= 3.4:
                f = int((t - 3.4) * 20)
                img = paste_light(img, art[tier]["refill"][f] if f < 8 else art[tier]["idle"][(k // 6) % 4], x, y)
        raster.draw_text(img, 6, H - 12, "HITS, EMPTIED, 2 S QUIET, THE REFILL", raster.LABEL_DIM)
        gif.append(img)
    big = [g.resize((W * 2, H * 2), Image.NEAREST) for g in gif]
    name = {"a": "A HEX LATTICE", "b": "B GOLD FILM, HEXES WHERE A HIT LANDS"}[v]
    title = f"ENEMY HEX SHIELD - PROPOSAL {name} (ROUND 61)"
    over = "OVER THE STAND-INS (1X; ROUND 60'S TALON B, HORNET B, GILDED GUNSHIP B IN THE CONCEPT LIVERY)"
    if SHIELD_ROUND != ROUND:
        title = f"ENEMY HEX SHIELD - FINAL {name}, FADING TOWARD THE MIDDLE (ROUND 64)"
        over = "OVER THE PRODUCTION TALON (SHIELD 12), HORNET AND GILDED GUNSHIP (1X)"
    rows = []
    for tier, D in TIERS.items():
        z = 2 if D <= 52 else 1
        rows.append((f"ENEMY-SHIELD-{tier.upper()}_0..3 ({D} PX): HOLDING, SHIMMER LOOP", art[tier]["idle"], z, True))
        rows.append((f"ENEMY-SHIELD-{tier.upper()}-REFILL_0..7", art[tier]["refill"], z, True))
        rows.append((f"ENEMY-SHIELD-{tier.upper()}-BREAK_0..5 (0.2 S)", art[tier]["brk"], z, True))
    rows.append(("ENEMY-SHIELD-HIT_0..5 (28X28, ON THE RIM AT THE HIT)", art["hit"], 3, True))
    rows.append((over, [gif[k] for k in (10, 44, int(FPS * 3.6))], 1, False))
    rows.append(("THE SAME AT 2X: HOLDING WHILE HIT, THE REFILL", [gif[k].resize((W * 2, H * 2), Image.NEAREST)
                                                              for k in (10, int(FPS * 3.6))], 1, False))
    sheet = artkit.review_sheet(title, rows, width=1720, batch=BATCH)
    save("shield", sheet, big, "enemy-shield", v)
    artkit.REVIEW_ROUND = before


def diamond_review(art):
    W, H = 300, 300
    plate = belt_plate(W, H, 51)
    sx = lambda t: 150 + 80 * np.sin(t * 0.9)  # noqa: E731
    sy = 250
    ship = artkit.load_frames("ship")[2]
    starts = [(0.2, 40, -10, None), (0.6, 260, -10, 2.2), (1.2, 150, -10, None), (1.8, -10, 120, None)]
    paths = []
    for t0, x, y, kill in starts:
        pos, h, path = np.array((x, y), float), np.arctan2(sy - y, sx(t0) - x), []
        for k in range(int(3.0 * FPS)):
            t = t0 + k / FPS
            if kill is not None and t >= kill:
                break
            want = np.arctan2(sy - pos[1], sx(t) - pos[0])
            dh = (want - h + np.pi) % TAU - np.pi
            h += float(np.clip(dh, -np.radians(80) / FPS, np.radians(80) / FPS))
            pos = pos + np.array((np.cos(h), np.sin(h))) * 110 / FPS
            path.append((t, pos.copy(), h))
        paths.append(dict(path=path, kill=kill, end=path[-1][0]))
    gif = []
    for i in range(int(FPS * 5)):
        t = i / FPS
        img = plate.copy()
        sprite.paste_center(img, ship, sx(t), sy)
        for m in paths:
            pts = [p for p in m["path"] if p[0] <= t]
            if not pts:
                continue
            _, pos, h = pts[-1]
            if m["kill"] is not None and t >= m["kill"]:
                f = int((t - m["kill"]) * 15)
                if f < 5:
                    img = paste_light(img, art["pops"][f], *pos)
                continue
            if t > m["end"]:
                f = int((t - m["end"]) * 12)
                if f < 5:
                    img = paste_light(img, art["fizzles"][f], *pos)
                continue
            k = int(round(((h + np.pi / 2) % TAU) / (TAU / DIAMOND_HEADINGS))) % DIAMOND_HEADINGS
            img = paste_light(img, art["exhaust"][k * 2 + (i // 2) % 2], *pos)
            sprite.paste_center(img, art["bodies"][k], pos[0], pos[1])
        gif.append(img)
    big = [g.resize((W * 2, H * 2), Image.NEAREST) for g in gif]
    cells = []
    for k in range(DIAMOND_HEADINGS):
        c = Image.new("RGBA", (EXHAUST, EXHAUST), artkit.PLATE)
        c = artkit.add_light(c, art["exhaust"][k * 2])
        c.alpha_composite(art["bodies"][k], ((EXHAUST - DIAMOND) // 2, (EXHAUST - DIAMOND) // 2))
        cells.append(c)
    rows = [("HOMING-DIAMOND_0..15: 18X18, 16 HEADINGS CLOCKWISE FROM UP (NORMAL)", art["bodies"], 4, False),
            ("WITH HOMING-DIAMOND-EXHAUST (HEADING X 2, FLICKER 0) ON THE DARK PLATE", cells, 3, False),
            ("HOMING-DIAMOND-POP_0..4 (SHOT DOWN) / -FIZZLE_0..4 (END OF LIFE)", art["pops"] + art["fizzles"], 4, True),
            ("IN FLIGHT (1X): FOUR DIAMONDS HOMING, ONE SHOT DOWN, ONE FIZZLING",
             [gif[k].crop((0, 0, 300, 300)) for k in (30, 70, 110)], 1, False)]
    sheet = artkit.review_sheet("HOMING DIAMOND (ROUND 60 A.16 A) - FINAL FRAMES (R61)", rows, width=1300, batch=BATCH)
    save("diamond", sheet, big, "homing-diamond", "a")


def swarm_review(frames):
    pods = artkit.load_frames("micro-missile-pod-shot")
    W, H = 320, 300
    plate = belt_plate(W, H, 9)
    ship = artkit.load_frames("ship")[2]
    units = stand_ins()
    sx, sy = 160, 110
    targets = [(70, 250), (260, 210), (230, 40)]
    gif, missiles = [], []
    for i in range(int(FPS * 4)):
        t = i / FPS
        img = plate.copy()
        if i % 10 == 0:                                           # a pair backwards +-25 degrees (L3)
            for s in (-1, 1):
                missiles.append(dict(pos=np.array((sx, sy + 14.0)), h=np.pi + s * np.radians(25), v=260.0,
                                     age=0.0, tgt=targets[(i // 10 + (s > 0)) % 2]))
        for tx, ty in targets:
            sprite.paste_center(img, units["small"], tx, ty)
        sprite.paste_center(img, ship, sx, sy)
        keep = []
        for m in missiles:
            m["age"] += 1 / FPS
            m["v"] = min(480.0, m["v"] + 220 / 0.3 / FPS)
            hx = np.arctan2(m["tgt"][0] - m["pos"][0], -(m["tgt"][1] - m["pos"][1]))
            dh = (hx - m["h"] + np.pi) % TAU - np.pi
            m["h"] += float(np.clip(dh, -np.radians(240) / FPS, np.radians(240) / FPS))
            m["pos"] = m["pos"] + np.array((np.sin(m["h"]), -np.cos(m["h"]))) * m["v"] / FPS
            if np.hypot(*(m["pos"] - np.array(m["tgt"]))) < 10 or m["age"] > 1.6:
                continue
            keep.append(m)
            k = int(round((m["h"] % TAU) / (TAU / SWARM_HEADINGS))) % SWARM_HEADINGS
            sprite.paste_center(img, frames[k], *m["pos"])
        missiles = keep
        gif.append(img)
    big = [g.resize((W * 2, H * 2), Image.NEAREST) for g in gif]
    rows = [("SWARM-TAIL-SHOT_0..31 (24X24, 32 HEADINGS), IVORY-WHITE PLUME", frames, 2, False),
            ("BESIDE THE MICRO-MISSILE POD'S (CYAN PLUME), HEADINGS 0, 8, 16, 24",
             [frames[k] for k in (0, 8, 16, 24)] + [pods[k] for k in (0, 8, 16, 24)], 4, False),
            ("IN FLIGHT (1X): L3 PAIRS LAUNCHED BACKWARDS, CURVING ONTO THE STAND-INS BEHIND THE SHIP",
             [gif[k] for k in (12, 40, 80)], 1, False)]
    sheet = artkit.review_sheet("SWARM TAIL - FINAL FRAMES (R61)", rows, width=1300, batch=BATCH)
    save("swarm", sheet, big, "swarm-tail", "a")


# =========================================================================== check

def check():
    """Every written set exists with its frame count, carries the Source chunk; prints sizes."""
    want = {"tesla-coil-pod": [("tesla-coil-pod-arc", 3), ("tesla-coil-pod-spark", 6), ("tesla-coil-pod-glow", 2)],
            "swarm-tail": [("swarm-tail-shot", 32)],
            "pod": [("pod-tesla-coil-left", 5), ("pod-tesla-coil-right", 5)],
            "enemy-shield": [(f"enemy-shield-{t}{s}", n) for t in TIERS
                             for s, n in (("", 4), ("-break", 6), ("-refill", 8))] + [("enemy-shield-hit", 6)],
            "homing-diamond": [("homing-diamond", 16), ("homing-diamond-exhaust", 32), ("homing-diamond-pop", 5),
                               ("homing-diamond-fizzle", 5)]}
    bad = 0
    for root, sets in want.items():
        with sprites_of(root) as folder:
            for name, n in sets:
                files = sorted(folder.glob(f"{name}_*.png"))
                files = [f for f in files if f.stem.rsplit("_", 1)[0] == name]
                ok = len(files) == n and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
                size = Image.open(files[0]).size if files else None
                print(f"{'ok ' if ok else 'BAD'} {name}: {len(files)}/{n} frames {size} in {where(root)}")
                bad += not ok
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    parts = [a for a in args if not a.startswith("--")] or ["arc", "pod", "shield", "diamond", "swarm"]
    write = "--review" not in args
    pods = {}
    if "pod" in parts and write:
        pods[ARC_VARIANT] = build_pod(ARC_VARIANT)
    if "arc" in parts:
        for v in ("a", "b"):
            if write and v == ARC_VARIANT:
                build_arc(v)
            arc_review(v, arc_set(v), pods.get(v))
    if "pod" in parts:
        if write:
            frames, offsets = pods[ARC_VARIANT]
        else:
            frames = {s: artkit.load_frames(f"pod-tesla-coil-{s}") for s in ("left", "right")}
            import json
            data = json.loads((artkit.PIVOTS / "pods.json").read_text(encoding="utf-8"))
            offsets = {s: data["offsets"][f"pod-tesla-coil-{s}"] for s in ("left", "right")}
        pod_review(ARC_VARIANT, frames, offsets)
    if "shield" in parts and "--r61" in args:            # round 61's two proposal sheets, as they were made
        LATTICE_FALLOFF, SHIELD_ROUND = 0.0, ROUND
        for v in ("a", "b"):
            shield_review(v, shield_set(v))
    elif "shield" in parts:
        art = shield_set(SHIELD_VARIANT)
        if write:
            build_shield(SHIELD_VARIANT)
        shield_review(SHIELD_VARIANT, art)
    if "diamond" in parts:
        if write:
            build_diamond()
        diamond_review(diamond_set())
    if "swarm" in parts:
        frames = build_swarm() if write else load("swarm-tail", "swarm-tail-shot")
        swarm_review(frames)
