#!/usr/bin/env python3
"""Production art: the Act 7 "Beyond the Gate" systems of M11 part A (step ART-A; final review in concept round 88):
the Choir Resonator (round 85 A.1 a "the bell"), the Vrell Swarm Call's drones and bloom (round 88 QA-1, variant
``DRONE_VARIANT``), the window shell's hit spark and break, the ``teleport`` fade and arrival ring (round 84 A.2 a: the
ring **shrinks** onto the landing point) and a stand-in spore cloud (Level 45). The shop icons are tools/art/icons.py
``--act7``. Every glow here is a smooth float light field added once and written unquantised (far more than 32 steps,
no ordered dither: the 2026-10-30 dither audit, ``DitherGuardTest``), and fades to 0 inside its canvas (``--check``
fails on a lit border pixel of an additive set).

Round 88's QA-1 (m11a-gaps §7): the drone's shell, a "teal-black" (the drafted look, recommended, written here) or b
"bone-white"; the proposal sheets are tools/concept/swarm_call_r88.py's, whose ``drone_model`` this script renders.

Outputs (assets/sprites/ once the data claims the root, else held, see below; ``additive`` = premultiplied on black,
blended GL_SRC_ALPHA, GL_ONE; ``normal`` = straight alpha; frame times in game steps of 1/60 s):

  Choir Resonator (design/player/weapons/choir-resonator; the `wave` family)
  choir-resonator-wave_0..15       additive, the wave front by its width: frame i is the arc W_i = 32 x (260/32)^(i/15)
                                   px wide (32, 37, ... 260: 15 % a step), the arc of a ring of half-angle 14.3 deg (L3's;
                                   L1 12.3 to OD 16.6 bend within 2 px of it) round a point behind the nose, thickness
                                   2.0 (narrow) to 2.6 px (widest): a white crescent thickest in the middle and tapering
                                   to nothing at its ends, the ice-blue glow and deep-blue fringe, two faint rings
                                   trailing 8 and 15 px behind. Frame size (2 ceil((R + 28) sin h) + 4) x (28 + R -
                                   (R - 28) cos h + 2), R = W / 2 / sin h; **anchor: the arc's apex (the front's middle)
                                   at (width / 2, 28)**, put on the wave's front point. The game picks the frame whose
                                   width is nearest the wave's (32 + (width_L - 32) x d / reach_L) and scales it in x to
                                   it exactly (at most +-7.5 %), y unscaled
  choir-resonator-wave-rim_0..15   normal, the same frames' thin dark violet-black rim either side of the bright edge
                                   (10, 8, 30; alpha up to 42 %), same size and anchor, drawn first, under the light
                                   The brightness k scales the light's colour and the rim's alpha: k = smooth(d / 12 +
                                   0.25) x (1 - smooth((d / reach - 0.8) / 0.2)) x 0.75^(hulls passed), smooth the
                                   smoothstep (up in the first 12 px, out over the last fifth)
  choir-resonator-ring_0..7        56x56 additive, centred on a hull the wave passes: a thin ice-blue ring rings out from
                                   6 to 20 px with a white centre, 0.27 s (2 steps a frame); scaled by k like the wave
  choir-resonator-mount_0..4       16x24 normal, per banking frame of the ship (hard left .. hard right): the Warden's eye
                                   (an ice-white faceted crystal) in a gunmetal collar with two claws, rolled with the
                                   hull; **centred 4 px below the ship's ``front`` pivot** (pivots/ship.json, (24, 3)),
                                   the crystal ahead of the nose; drawn over the hull while the gun is fitted
  choir-resonator-glow_0..3        16x16 additive, centred on the ``front`` pivot (the nose): 0 the idle shimmer (always
                                   while fitted), 1..3 the bloom after each wave, 2 steps a frame
  Vrell Swarm Call (design/player/specials, Vrell Swarm Call (L46))
  vrell-swarm-call-drone_0..63     24x24 normal: the Needler's body re-grown (QA-1 ``DRONE_VARIANT``), 16 headings x 4
                                   claw frames, indexed heading * 4 + frame; heading k flies k x 22.5 degrees clockwise
                                   from straight down (``needle``'s order); frames 0-2 the claws opening, 3 the snap: one
                                   cycle per thorn (0.5 s: 8 steps a frame for 0-2, the snap on the thorn's launch)
  vrell-swarm-call-drone-glow      24x24 additive, centred on each drone: its soft white-cyan glow (it carries the drone
                                   over Level 46's dark), drawn under it
  vrell-swarm-call-trail           12x30 additive, its point (6, 0) on the drone's centre, the trail hanging below:
                                   the faint white-cyan trail, turned to point away from the heading
  vrell-swarm-call-wither_0..7     40x40 additive, centred on a drone as it withers (0.5 s, 4 steps a frame) while the
                                   game fades the drone's alpha 1 -> 0: its glow sinking and a few white-green motes
                                   drifting off; also the 0-HP "puff of spores", played twice as fast
  vrell-swarm-call-bloom_0..8      112x112 additive, centred 26 px behind the ship's centre (below it): the seed-pod
                                   bloom, six soft white-green petals of light unfolding and fading, 0.3 s (2 steps a
                                   frame), no hard edge
  Window shell (design/enemies, Window shields; the Gate Warden's)
  shell-spark_0..5                 28x28 additive, on a closed shell where a shot is spent: a pale gold glint, five short
                                   rays and four pale gold flakes flung out and falling, 0.2 s (2 steps a frame)
  shell-break_0..7                 72x72 additive, centred on the unit when a breaker hit or the EMP opens it: the shell
                                   shattering into fourteen gold-white flakes, 0.4 s (3 steps a frame)
  Teleport (design/enemies, the ``teleport`` movement; the Rift Skater's and the Choir Seraph's)
  teleport-fade_0..5               48x48 additive, on the unit while it fades out (0.2 s, 2 steps a frame; the game fades
                                   the unit's alpha): a violet-white shimmer, six glints drawn into it
  teleport-ring_0..9               56x56 additive, centred on the arrival point: the Rift Skater's ring (round 84 A.2 a),
                                   a thin violet circle with a violet-white core line **shrinking** from 22 to 3 px and
                                   brightening over its 0.5 s (3 steps a frame; easy's 0.7 s: 4 steps a frame + 2), never
                                   a flash; a ring that slides keeps playing at its moving centre
  teleport-ring-large_0..9         84x84 additive: the same for the Choir Seraph, 34 to 6 px (violet only, E.5 a)
  Spore cloud, stand-in (design/world/vrell-space, Hazards & set pieces; Level 45, part C)
  spore-cloud-veil                 176x176 normal, a cloud of radius 64 (scale by r / 64 for 60-100 px clouds): the
                                   palette's dark violet (30, 12, 44) as a smooth veil, alpha at most 25 % (64): drawn
                                   over the backdrop and the play-plane units under it (their tells above), under the
                                   bullets, the ship, Rook and the pickups
  spore-cloud_0..7                 176x176 additive, same scale: the cloud's dim violet glow and pale violet specks
                                   drifting round, a 4 s loop (30 steps a frame), drawn over the veil
  spore-cloud-burn_0..9            176x176 additive, same scale: the 1 s burn (6 steps a frame) instead of the cloud's
                                   frames: an ember edge eating inward from the rim, the cloud's glow left inside it; the
                                   game scales the veil by the front's radius (1 - t) meanwhile

Held sprites (as act6_gear.py): a root the game data does not claim yet is written to build/art-held/sprites/ instead
(SpriteUse fails the build on an unclaimed sprite): ``choir-resonator-*`` until its weapon data.yaml exists (A2b),
``vrell-swarm-call-*`` until specials/data.yaml lists the special (A2b), ``shell-*``, ``teleport-*`` and
``spore-cloud-*`` until pipeline/.../SpriteUse.java names the root (A3a/A3b); rerun the part then.

Review sheets and GIFs (round 88), each ``<subject>-final-r88-a``: design/player/weapons/choir-resonator/concept/
choir-resonator, design/player/specials/concept/swarm-call, design/enemies/concept/window-shell and teleport,
design/world/vrell-space/concept/spore-cloud. The backgrounds are round 84's Level 43-46 stand-ins in the one Vrell
palette (tools/concept/act7_r84.py ``one_palette`` over enemies_r84.py's stills), at 1x.

Run: python3 tools/art/act7_gear.py [resonator] [swarm] [shell] [teleport] [spore] [--review] [--check]
     (no part = all, ~1 min; --review only rebuilds the sheets from the written frames; --check lists every set with
     its size, frame count and place and fails on a missing one, a missing Source chunk or a lit additive border)
"""
import contextlib
import sys
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image

import act3kit
import artkit
from artkit import DESIGN, ROOT, TAU, sprite

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept scripts, imported unchanged)
import enemies_r84 as u84  # noqa: E402
import gear_r36 as g36  # noqa: E402
import resonator_r85 as r85  # noqa: E402
import swarm_call_r88 as s88  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.sdf import rotate_y  # noqa: E402

SCRIPT = "act7_gear.py"
BATCH = "M11 part A batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r88"
artkit.REVIEW_ROUND = ROUND
SPRITE_USE = ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java"
CONCEPT = {"resonator": DESIGN / "player" / "weapons" / "choir-resonator" / "concept",
           "swarm": DESIGN / "player" / "specials" / "concept",
           "shell": DESIGN / "enemies" / "concept",
           "teleport": DESIGN / "enemies" / "concept",
           "spore": DESIGN / "world" / "vrell-space" / "concept"}
ADDITIVE = {"choir-resonator-wave", "choir-resonator-ring", "choir-resonator-glow", "vrell-swarm-call-drone-glow",
            "vrell-swarm-call-trail", "vrell-swarm-call-wither", "vrell-swarm-call-bloom", "shell-spark", "shell-break",
            "teleport-fade", "teleport-ring", "teleport-ring-large", "spore-cloud", "spore-cloud-burn"}
DRONE_VARIANT = "a"     # round 88 QA-1 (recommended a, written until the user's pick)
SS = 4
FPS = 30

# the Choir Resonator (round 85 A.1 a: resonator_r85.STYLE["a"])
WAVE_N, WAVE_MIN, WAVE_MAX, WAVE_PAD = 16, 32.0, 260.0, 28
WAVE_H = float(np.arcsin(168 / 680))          # L3's half-angle (width 200 at reach 340)
STYLE = r85.STYLE["a"]
GLOW = np.array(STYLE["glow"], float)
FRINGE = np.array(STYLE["fringe"], float)
RIM_RGB = (10, 8, 30)
LEVELS = {1: (160, 300), 2: (180, 320), 3: (200, 340), 4: (220, 360), 5: (240, 380), 6: (260, 400)}  # 6 = OD
RING_N, RING_FR = 56, 8
MOUNT = (16, 24)
BANKS = [-30, -15, 0, 15, 30]
NOSE_GLOW = 16
# the Swarm Call
DRONE, DRONE_HEADINGS, CLAW = 24, 16, [0.0, -2.0, -3.5, 2.8]
WHITE_CYAN = np.array(s88.GLOW_RGB, float)
WHITE_GREEN = np.array((220, 255, 230), float)
GREEN = np.array((120, 255, 170), float)
# the shell
PALE_GOLD = np.array((250, 232, 170), float)
GOLD_WHITE = np.array((248, 236, 196), float)
# teleport
VIOLET = np.array(u84.VIOLET, float)
VIOLET_W = np.array(u84.VIOLET_W, float)
# spores
SPORE_R, SPORE_N = 64, 176
EMBER = np.array((255, 140, 50), float)
EMBER_W = np.array((255, 220, 150), float)


# =========================================================================== small kit

def grid(w, h, ss=SS):
    """Supersampled pixel-centre coordinates in native pixels."""
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64)
    return (xx + 0.5) / ss, (yy + 0.5) / ss


def gauss(d, w):
    return np.exp(-(d / np.maximum(w, 1e-6)) ** 2)


def blob(x, y, cx, cy, r):
    """e60.light's falloff: exp(-2 d^2 / r^2)."""
    return np.exp(-2.0 * ((x - cx) ** 2 + (y - cy) ** 2) / (r * r))


def smooth(x):
    x = np.clip(x, 0.0, 1.0)
    return x * x * (3 - 2 * x)


def light_image(light, w, h, ss=SS):
    """A supersampled light field (0..255 RGB) -> an additive frame (premultiplied, alpha where lit), unquantised."""
    lit = np.clip(light.reshape(h, ss, w, ss, 3).mean(axis=(1, 3)), 0, 255)
    rgb = np.round(lit)
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def alpha_image(alpha, rgb, w, h, ss=SS):
    """A supersampled 0..1 coverage field -> a normal-alpha frame of one colour, smooth alpha."""
    a = np.round(np.clip(alpha.reshape(h, ss, w, ss).mean(axis=(1, 3)), 0, 1) * 255)
    out = np.zeros((h, w, 4), np.uint8)
    out[..., :3] = np.where(a[..., None] > 0, np.array(rgb, np.uint8), 0)
    out[..., 3] = a
    return Image.fromarray(out, "RGBA")


def add(light, col, amount):
    light += np.asarray(col, float)[None, None, :] * np.clip(amount, 0, None)[..., None]


def diamond(x, y, cx, cy, size, ang):
    """Soft coverage (0..1) of a small diamond flake turned by ``ang``."""
    c, s = np.cos(ang), np.sin(ang)
    u, v = (x - cx) * c + (y - cy) * s, -(x - cx) * s + (y - cy) * c
    d = np.abs(u) / size + np.abs(v) / (0.6 * size)
    return np.clip((1.0 - d) * size * 2.0 + 0.5, 0, 1)


def claimed(root):
    if root in ("choir-resonator", "vrell-swarm-call"):
        return act3kit.claimed(root)
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


def save(kind, sheet, gif_frames, subject, fps=FPS):
    png, gif = artkit.review_paths(CONCEPT[kind], subject)
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(gif_frames, gif, fps=fps, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def put_light(img, frame, cx, cy, k=1.0, sx=1.0):
    """An additive frame centred at (cx, cy), its colour scaled by k and its width by sx, as the game blends it."""
    if k <= 0.003:
        return img
    if abs(sx - 1.0) > 1e-3:
        frame = frame.resize((max(1, int(round(frame.width * sx))), frame.height), Image.BILINEAR)
    a = np.array(frame).astype(float)
    a[..., :3] *= k
    return artkit.add_light(img, Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"),
                            (int(round(cx - frame.width / 2)), int(round(cy - frame.height / 2))))


def put_alpha(img, frame, x0, y0, k=1.0):
    """A normal-alpha frame at its top-left (x0, y0), its alpha scaled by k."""
    a = np.array(frame).astype(float)
    a[..., 3] *= k
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    layer.paste(Image.fromarray(a.astype(np.uint8), "RGBA"), (int(round(x0)), int(round(y0))))
    img.alpha_composite(layer)
    return img


def still(level):
    """A level's stand-in still in the one Vrell palette (Q10 b), 480x540."""
    rgb = {"l43": lambda: u84.gate_bg(4301), "l44": lambda: u84.EventPlane(4401, 1.0).frame(0.0),
           "l45": lambda: u84.reef_bg(4501), "l46": lambda: u84.graveyard_bg(4601)}[level]()
    return a84.one_palette(rgb).convert("RGBA")


def needler():
    return Image.open(artkit.SPRITES / "needler_0.png").convert("RGBA")


def tile(frames, zoom=1, glow=True, gap=4, bg=artkit.PLATE, cols=None):
    """Frames side by side (cols per row) at a zoom, glows added onto the plate."""
    cols = cols or len(frames)
    cw, ch = max(f.width for f in frames) * zoom, max(f.height for f in frames) * zoom
    rows = -(-len(frames) // cols)
    out = Image.new("RGBA", (cols * (cw + gap), rows * (ch + gap)), bg)
    for i, f in enumerate(frames):
        cell = artkit.on_background(f, zoom, glow)
        out.alpha_composite(cell, ((i % cols) * (cw + gap) + (cw - cell.width) // 2,
                                   (i // cols) * (ch + gap) + (ch - cell.height) // 2))
    return out


def sheet_of(title, rows, width=1000):
    """rows = [(label, image)]; each image cropped to the sheet's width."""
    h = 40 + sum(img.height + 28 for _, img in rows) + 10
    img = raster.sheet(width, h, title, f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    y = 38
    for label, part in rows:
        raster.draw_text(img, 16, y, label, raster.LABEL)
        img.alpha_composite(part.crop((0, 0, min(part.width, width - 32), part.height)), (16, y + 14))
        y += part.height + 28
    return img


# =========================================================================== the Choir Resonator

def wave_width(i):
    return WAVE_MIN * (WAVE_MAX / WAVE_MIN) ** (i / (WAVE_N - 1))


def wave_size(i):
    W = wave_width(i)
    R = W / 2 / np.sin(WAVE_H)
    cw = 2 * int(np.ceil((R + WAVE_PAD) * np.sin(WAVE_H))) + 4
    ch = int(np.ceil(WAVE_PAD + R - (R - WAVE_PAD) * np.cos(WAVE_H))) + 2
    return cw, ch, R


def wave_frame(i):
    """(light, rim) of wave frame i: resonator_r85.draw_wave's variant a as a supersampled field, k = 1."""
    W = wave_width(i)
    cw, ch, R = wave_size(i)
    w = 2.0 + 0.6 * (W - WAVE_MIN) / (WAVE_MAX - WAVE_MIN)
    x, y = grid(cw, ch)
    dx, dy = x - cw / 2, y - (WAVE_PAD + R)
    s = np.hypot(dx, dy) - R
    u = np.abs(np.arctan2(dx, -dy)) / WAVE_H
    tap = (1 - smooth((u - 0.45) / 0.55)) * (dy < 0)
    wv = w * (0.55 + 0.45 * np.cos(np.pi / 2 * np.clip(u, 0, 1)))
    lf = np.zeros(x.shape + (3,))
    add(lf, (255, 255, 255), gauss(s, 0.55 * wv) * 1.35 * tap)
    add(lf, GLOW, gauss(s, 1.4 * wv) * 0.85 * tap)
    add(lf, FRINGE, gauss(s, 3.4 * wv) * 0.3 * tap)
    for off, kk in ((8.0, 0.42), (15.0, 0.2)):
        add(lf, GLOW, gauss(s + off, 0.75 * w) * kk * tap)
    rim = gauss(np.abs(s) - 2.4 * wv, 0.9 * wv) * tap * 0.42
    return light_image(lf, cw, ch), alpha_image(rim, RIM_RGB, cw, ch)


def ring_frame(f):
    s = f / RING_FR
    x, y = grid(RING_N, RING_N)
    c = RING_N / 2
    d = np.hypot(x - c, y - c)
    k = (1 - s) ** 1.5
    lf = np.zeros(x.shape + (3,))
    add(lf, GLOW, gauss(d - (6 + 16 * s), 1.3) * 0.9 * k)
    add(lf, (255, 255, 255), blob(x, y, c, c, 5.0) * 0.7 * k * (1 - s))
    return light_image(lf, RING_N, RING_N)


def nose_glow(f):
    fire = (0.0, 1.0, 0.66, 0.33)[f]
    x, y = grid(NOSE_GLOW, NOSE_GLOW)
    c = NOSE_GLOW / 2
    lf = np.zeros(x.shape + (3,))
    add(lf, GLOW, blob(x, y, c, c, 3.5) * (0.35 + 0.9 * fire))
    add(lf, (255, 255, 255), blob(x, y, c, c, 1.6) * 0.5 * fire)
    return light_image(lf, NOSE_GLOW, NOSE_GLOW)


def render_mount(bank):
    scene, mats = r85.mount_model("a")
    hi, factor = artkit.render_hi(lambda p: scene(rotate_y(p, np.radians(bank))), mats, MOUNT, MOUNT[0] / 8.0)
    return artkit.native(hi, factor)


def build_resonator():
    waves = [wave_frame(i) for i in range(WAVE_N)]
    with ProcessPoolExecutor() as pool:
        mounts = artkit.quantize_set(list(pool.map(render_mount, BANKS)), 24)
    write("choir-resonator", "choir-resonator-wave", [w[0] for w in waves])
    write("choir-resonator", "choir-resonator-wave-rim", [w[1] for w in waves])
    write("choir-resonator", "choir-resonator-ring", [ring_frame(f) for f in range(RING_FR)])
    write("choir-resonator", "choir-resonator-mount", mounts)
    write("choir-resonator", "choir-resonator-glow", [nose_glow(f) for f in range(4)])
    print(f"choir resonator: {where('choir-resonator')}")


def load_resonator():
    return {k: load("choir-resonator", f"choir-resonator-{k}") for k in ("wave", "wave-rim", "ring", "mount", "glow")}


def wave_k(d, reach):
    return float(smooth(d / 12.0 + 0.25) * (1 - smooth((d / reach - 0.8) / 0.2)))


def draw_wave(img, art, nx, ny, d, level, bright=1.0):
    """One wave of ``level`` d px out from the nose (nx, ny), as the game will draw it."""
    width_l, reach = LEVELS[level]
    k = bright * wave_k(d, reach)
    if k <= 0.003 or d > reach:
        return img
    W = WAVE_MIN + (width_l - WAVE_MIN) * d / reach
    i = int(np.argmin([abs(np.log(W / wave_width(j))) for j in range(WAVE_N)]))
    sx = W / wave_width(i)
    rim, lit = art["wave-rim"][i], art["wave"][i]
    fy = ny - d
    rim_s = rim.resize((max(1, int(round(rim.width * sx))), rim.height), Image.BILINEAR)
    img = put_alpha(img, rim_s, nx - rim_s.width / 2, fy - WAVE_PAD, k)
    return put_light(img, lit, nx, fy - WAVE_PAD + lit.height / 2, k, sx)


def ship_with_mount(img, art, x, y, t=0.0, fire=0.0):
    g36.draw_ship(img, x, y, t)
    ox, oy = x - 24, y - 24
    img.alpha_composite(art["mount"][2], (int(ox + 24 - MOUNT[0] / 2), int(oy + 3 + 4 - MOUNT[1] / 2)))
    f = 0 if fire <= 0 else min(3, 1 + int((1 - fire) * 3))
    return put_light(img, art["glow"][f], ox + 24, oy + 3)


def resonator_frame(art, bg, t, level):
    """The play field crop: the ship at the foot firing ``level`` waves up through a column of hulls (Needlers as
    stand-ins), rings on the hulls passed, each pass a step dimmer."""
    f = bg.copy()
    rate = 2.5 if level < 4 else 3.0
    px, py = 160 + 28 * np.sin(t * 0.9), 360
    nx, ny = px, py - 21
    hulls = [(160 + 10 * np.sin(t + j), 250 - 48 * j) for j in range(4)] + [(70, 120), (250, 130)]
    for hx, hy in hulls:
        sprite.paste_center(f, needler(), hx, hy)
    reach = LEVELS[level][1]
    cap = {1: 3, 2: 3, 3: 4, 4: 4, 5: 5, 6: 6}[level]
    n = int(t * rate)
    for j in range(max(0, n - 3), n + 1):
        t0 = j / rate
        d = (t - t0) * 520
        if not 0 <= d <= reach:
            continue
        fx = 160 + 28 * np.sin(t0 * 0.9)
        width = WAVE_MIN + (LEVELS[level][0] - WAVE_MIN) * d / reach
        passed = 0
        for hx, hy in sorted(hulls, key=lambda h: -h[1]):
            dh = ny - hy
            if dh < d and abs(hx - fx) < (WAVE_MIN + (LEVELS[level][0] - WAVE_MIN) * dh / reach) / 2 + 12:
                if passed < cap:
                    s = (d - dh) / 520 / 0.27
                    if s < 1:
                        f = put_light(f, art["ring"][min(RING_FR - 1, int(s * RING_FR))], hx, hy, 0.75 ** passed)
                passed += 1
        if passed < cap and width > 0:
            f = draw_wave(f, art, fx, ny, d, level, 0.75 ** passed)
    since = (t * rate) % 1 / rate
    return ship_with_mount(f, art, px, py, t, 1 - since / 0.1 if since < 0.1 else 0.0)


def resonator_review(art):
    l44 = still("l44")
    bright = l44.crop((60, 0, 380, 400))
    keys = []
    for d, lv in ((40, 1), (150, 1), (290, 1), (60, 5), (200, 5), (370, 5)):
        c = Image.new("RGBA", (300, 420), artkit.PLATE)
        c = draw_wave(c, art, 150, 410, d, lv)
        keys.append(c.crop((0, 0, 300, 420)).resize((150, 210), Image.BILINEAR))
    keyrow = Image.new("RGBA", (6 * 156, 210), artkit.PLATE)
    for i, c in enumerate(keys):
        keyrow.alpha_composite(c, (i * 156, 0))
    gif = [resonator_frame(art, bright, i / FPS, 1 if i < 75 else 5) for i in range(150)]
    field = Image.new("RGBA", (2 * 324, 400), artkit.PLATE)
    field.alpha_composite(gif[40], (0, 0))
    field.alpha_composite(gif[118], (324, 0))
    ship = Image.new("RGBA", (5 * 60, 60), artkit.PLATE)
    for b in range(5):
        cell = Image.new("RGBA", (56, 60), artkit.PLATE)
        hull = Image.open(artkit.SPRITES / f"ship_{b}.png").convert("RGBA")
        cell.alpha_composite(hull, (4, 8))
        cell.alpha_composite(art["mount"][b], (4 + 24 - 8, 8 + 7 - 12))
        cell = put_light(cell, art["glow"][0], 4 + 24, 8 + 3)
        ship.alpha_composite(cell, (b * 60, 0))
    rows = [("WAVE FRAMES 0-15 (32 TO 260 PX WIDE, 15 % A STEP), LIGHT OVER THE RIM, 1X",
             tile([put_light(put_alpha(Image.new("RGBA", (w.width, w.height), artkit.PLATE), r, 0, 0), w,
                             w.width / 2, w.height / 2) for w, r in zip(art["wave"], art["wave-rim"])][:9], 1, False)),
            ("", tile([put_light(put_alpha(Image.new("RGBA", (w.width, w.height), artkit.PLATE), r, 0, 0), w,
                                 w.width / 2, w.height / 2) for w, r in zip(art["wave"], art["wave-rim"])][9:], 1, False,
                      cols=4)),
            ("ONE WAVE AT DISTANCES: L1 AT 40 / 150 / 290 PX, L5 AT 60 / 200 / 370 PX (FADING OVER THE LAST FIFTH), 1/2X",
             keyrow),
            ("THE MOUNT PER BANKING FRAME ON THE PRODUCTION HULL WITH THE IDLE NOSE GLOW, 1X; MOUNT 4X; NOSE GLOW 4X;"
             " HULL RING 2X", ship),
            ("", tile(art["mount"], 4, False)),
            ("", tile(art["glow"] + [Image.new("RGBA", (4, 4), artkit.PLATE)], 4)),
            ("", tile(art["ring"], 2)),
            ("1X OVER LEVEL 44'S EVENT PLANE (ONE VRELL PALETTE): L1 THROUGH A COLUMN OF HULLS (NEEDLERS AS STAND-INS),"
             " THEN L5", field)]
    sheet = sheet_of("CHOIR RESONATOR - FINAL FRAMES (ROUND 85 A.1 A, THE BELL)", rows)
    save("resonator", sheet, gif, "choir-resonator")


# =========================================================================== the Vrell Swarm Call

def render_drone(heading, k):
    scene, mats = s88.drone_model(DRONE_VARIANT, heading, CLAW[k])
    return artkit.native(*artkit.render_hi(scene, mats, (DRONE, DRONE), s88.EXTENT))


def drone_glow():
    x, y = grid(DRONE, DRONE)
    lf = np.zeros(x.shape + (3,))
    add(lf, WHITE_CYAN, blob(x, y, DRONE / 2, DRONE / 2, 7.0) * 0.3)
    return light_image(lf, DRONE, DRONE)


def drone_trail():
    w, h = 12, 30
    x, y = grid(w, h)
    lf = np.zeros(x.shape + (3,))
    for i in range(1, 7):
        add(lf, WHITE_CYAN, blob(x, y, w / 2, 6 + 2.6 * i, 2.2 - 0.18 * i) * 0.32 * (1 - i / 7.5))
    return light_image(lf, w, h)


def wither_frame(f, n=8):
    s = (f + 0.5) / n
    N = 40
    x, y = grid(N, N)
    c = N / 2
    lf = np.zeros(x.shape + (3,))
    add(lf, WHITE_CYAN, blob(x, y, c, c, 7.0 + 3 * s) * 0.4 * (1 - s) ** 1.4)
    rng = np.random.default_rng(46)
    for _ in range(9):
        a, sp = rng.uniform(0, TAU), rng.uniform(0.5, 1.0)
        r = 3 + 11 * sp * np.sqrt(s)
        add(lf, WHITE_GREEN, blob(x, y, c + np.cos(a) * r, c + np.sin(a) * r - 3 * s, 1.6) * 0.75 * (1 - s) ** 0.9)
    return light_image(lf, N, N)


def bloom_frame(f, n=9):
    t = (f + 0.5) / n
    N = 112
    x, y = grid(N, N)
    c = N / 2
    dx, dy = x - c, y - c
    rho, ang = np.hypot(dx, dy), np.arctan2(dy, dx)
    env = smooth(t * 4) * (1 - t) ** 0.8
    lf = np.zeros(x.shape + (3,))
    add(lf, WHITE_GREEN, blob(x, y, c, c, 10 + 10 * t) * 0.9 * env)
    rc, ln, wd = 6 + 20 * smooth(t * 1.2), 7 + 6 * t, 0.38
    for j in range(6):
        a = j * TAU / 6 + 0.26
        da = np.angle(np.exp(1j * (ang - a)))
        petal = gauss(rho - rc, ln) * gauss(da, wd) * smooth(rho / 6)
        add(lf, GREEN, petal * 0.7 * env)
        add(lf, WHITE_GREEN, petal ** 2 * 0.5 * env)
    return light_image(lf, N, N)


def build_swarm():
    jobs = [(TAU * h / DRONE_HEADINGS, k) for h in range(DRONE_HEADINGS) for k in range(len(CLAW))]
    with ProcessPoolExecutor() as pool:
        drones = artkit.quantize_set(list(pool.map(render_drone, *zip(*jobs))), 32)
    write("vrell-swarm-call", "vrell-swarm-call-drone", drones)
    write("vrell-swarm-call", "vrell-swarm-call-drone-glow", [drone_glow()], single=True)
    write("vrell-swarm-call", "vrell-swarm-call-trail", [drone_trail()], single=True)
    write("vrell-swarm-call", "vrell-swarm-call-wither", [wither_frame(f) for f in range(8)])
    write("vrell-swarm-call", "vrell-swarm-call-bloom", [bloom_frame(f) for f in range(9)])
    print(f"swarm call: {where('vrell-swarm-call')}")


def load_swarm():
    return {k: load("vrell-swarm-call", f"vrell-swarm-call-{k}")
            for k in ("drone", "drone-glow", "trail", "wither", "bloom")}


def draw_drone(img, art, x, y, dx, dy, k=0, alpha=1.0):
    h = s88.heading_of(dx, dy)
    img = put_light(img, art["drone-glow"][0], x, y, alpha)
    tr = art["trail"][0]
    ang = np.degrees(np.arctan2(-dx, -dy))                     # the trail hangs opposite the heading
    big = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    big.paste(tr, (32 - 6, 32))
    img = put_light(img, big.rotate(ang, resample=Image.BILINEAR), x, y, alpha)
    sp = art["drone"][h * len(CLAW) + k]
    return put_alpha(img, sp, x - DRONE / 2, y - DRONE / 2, alpha)


def swarm_frame(art, bgs, t):
    """3 s over Level 46 then 3 s over Level 44: the bloom, six drones fanning out onto three Needlers, orbiting and
    spitting thorns; the last second the wither."""
    f = (bgs[1] if t < 3.0 else bgs[0]).copy()
    tt = t % 3.0
    px, py = 160 + 30 * np.sin(tt * 1.2), 330
    targets = [(70 + 90 * i + 12 * np.sin(tt + i), 110 + 14 * np.cos(tt * 0.8 + i)) for i in range(3)]
    for x, y in targets:
        sprite.paste_center(f, needler(), x, y)
    if tt < 0.3:
        f = put_light(f, art["bloom"][min(8, int(tt / 0.3 * 9))], px, py + 26)
    for i in range(6):
        tx, ty = targets[i % 3]
        a = i * TAU / 6 + tt * 2.4 + (i // 3) * np.pi
        ox, oy = tx + np.cos(a) * 40, ty + 34 + np.sin(a) * 22
        u = s88.e60.smooth(min(1.0, max(0.0, tt - 0.15) / 0.8))
        x, y = px + (ox - px) * u, py + 20 + (oy - py - 20) * u
        dx = (ox - px) * (1 - u) - np.sin(a) * 40 * u
        dy = (oy - py) * (1 - u) + np.cos(a) * 22 * u
        alpha = 1.0 if tt < 2.5 else max(0.0, 1 - (tt - 2.5) / 0.5)
        if tt >= 2.5:
            f = put_light(f, art["wither"][min(7, int((tt - 2.5) / 0.5 * 8))], x, y)
        if tt > 0.15:
            spit = (tt * 2 + i / 6) % 1
            f = draw_drone(f, art, x, y, dx, dy, 3 if spit < 0.12 else int(spit * 3) % 3, alpha)
            if tt > 1.0 and alpha > 0.5:
                th = spit * 60
                n = np.hypot(tx - x, ty - y) or 1
                f = s88.light(f, x + (tx - x) / n * th, y + (ty - y) / n * th, 1.6, (200, 245, 255), 0.9)
    g36.draw_ship(f, px, py, t)
    return f


def swarm_review(art):
    bgs = (still("l44").crop((80, 60, 400, 420)), still("l46").crop((80, 60, 400, 420)))
    gif = [swarm_frame(art, bgs, i / FPS) for i in range(180)]
    heads = tile([art["drone"][h * len(CLAW)] for h in range(DRONE_HEADINGS)], 3, False, cols=8)
    claw = tile(art["drone"][:len(CLAW)] + art["drone"][4 * len(CLAW):5 * len(CLAW)], 4, False)
    misc = tile(art["drone-glow"] + art["trail"], 3)
    field = Image.new("RGBA", (3 * 324, 360), artkit.PLATE)
    for j, i in enumerate((8, 60, 140)):
        field.alpha_composite(gif[i], (j * 324, 0))
    rows = [("16 HEADINGS (CLOCKWISE FROM DOWN), CLAWS OPEN, 3X", heads),
            ("THE CLAW CYCLE (OPENING 0-2, THE SNAP 3) AT HEADINGS 0 AND 4, 4X", claw),
            ("THE DRONE'S GLOW AND TRAIL, 3X", misc),
            ("THE WITHER (0.5 S), 3X", tile(art["wither"], 3)),
            ("THE BLOOM (0.3 S), 1X", tile(art["bloom"], 1)),
            ("1X OVER LEVEL 46, THEN 44: THE BLOOM, THE DRONES ORBITING NEEDLERS AND SPITTING THORNS", field)]
    sheet = sheet_of(f"VRELL SWARM CALL - FINAL FRAMES (QA-1 {DRONE_VARIANT.upper()})", rows)
    save("swarm", sheet, gif, "swarm-call")


# =========================================================================== the window shell

def spark_frame(f, n=6):
    s = (f + 0.5) / n
    N = 28
    x, y = grid(N, N)
    c = N / 2
    lf = np.zeros(x.shape + (3,))
    add(lf, (255, 246, 214), blob(x, y, c, c, 4.0) * 0.9 * (1 - s) ** 1.5)
    for k in range(5):
        a = k * TAU / 5 + 1.1
        r0, r1 = 7 * (0.3 + s) * 0.4, 7 * (0.3 + s)
        px0, py0, px1, py1 = c + np.cos(a) * r0, c + np.sin(a) * r0, c + np.cos(a) * r1, c + np.sin(a) * r1
        bx, by = px1 - px0, py1 - py0
        hh = np.clip(((x - px0) * bx + (y - py0) * by) / (bx * bx + by * by), 0, 1)
        add(lf, PALE_GOLD, gauss(np.hypot(x - px0 - bx * hh, y - py0 - by * hh), 0.6) * 0.8 * (1 - s))
    rng = np.random.default_rng(43)
    for _ in range(4):
        a, sp = rng.uniform(0, TAU), rng.uniform(0.5, 1.0)
        r = 1 + 9 * sp * np.sqrt(s)
        cov = diamond(x, y, c + np.cos(a) * r, c + np.sin(a) * r + 4 * s * s, 1.4, a + 4 * s)
        add(lf, PALE_GOLD, cov * 0.95 * (1 - s) ** 0.8)
    return light_image(lf, N, N)


def break_frame(f, n=8):
    s = (f + 0.5) / n
    N = 72
    x, y = grid(N, N)
    c = N / 2
    lf = np.zeros(x.shape + (3,))
    add(lf, GOLD_WHITE, blob(x, y, c, c, 14 * (0.5 + s)) * 0.45 * (1 - s) ** 2)
    rng = np.random.default_rng(47)
    for _ in range(14):
        a, sp, sz = rng.uniform(0, TAU), rng.uniform(0.4, 1.0), 2.2 * rng.uniform(0.7, 1.2)
        r = 6 + 22 * sp * np.sqrt(s)
        cov = diamond(x, y, c + np.cos(a) * r, c + np.sin(a) * r + 8 * s * s, sz, a + 5 * s * sp)
        add(lf, GOLD_WHITE, cov * 0.9 * (1 - s) ** 0.8)
    return light_image(lf, N, N)


def build_shell():
    write("shell", "shell-spark", [spark_frame(f) for f in range(6)])
    write("shell", "shell-break", [break_frame(f) for f in range(8)])
    print(f"shell: {where('shell')}")


BOLT = None


def shell_review():
    global BOLT
    BOLT = Image.open(artkit.SPRITES / "pulse-bolt.png").convert("RGBA")
    sparks, breaks = load("shell", "shell-spark"), load("shell", "shell-break")
    bg = still("l43").crop((0, 0, 320, 300))
    warden = u84.warden_sprite("a", 0.0)
    gif = []
    for i in range(72):
        t = i / FPS
        f = bg.copy()
        for j, (wx, wy) in enumerate(((80, 110), (240, 110))):
            sprite.paste_center(f, warden, wx, wy)
            for k in range(4):
                if j == 1 and t > 1.2:
                    continue
                tau = ((t + k * 0.07 + j * 0.11) % 0.28)
                hx, hy = wx - 14 + 9 * k, wy + 30
                if tau < 0.15:
                    f = put_light(f, BOLT, hx, 300 - (300 - hy) * tau / 0.15)
                elif tau < 0.25:
                    f = put_light(f, sparks[min(5, int((tau - 0.15) / 0.1 * 6))], hx, hy)
        if 1.2 <= t < 1.6:
            f = put_light(f, breaks[min(7, int((t - 1.2) / 0.4 * 8))], 240, 110)
        gif.append(f)
    field = Image.new("RGBA", (2 * 324, 300), artkit.PLATE)
    field.alpha_composite(gif[20], (0, 0))
    field.alpha_composite(gif[40], (324, 0))
    rows = [("THE SPARK ON A CLOSED SHELL (0.2 S), 4X", tile(sparks, 4)),
            ("THE BREAK (A BREAKER HIT OR THE EMP OPENS IT; 0.4 S), 2X", tile(breaks, 2, cols=8)),
            ("1X OVER LEVEL 43'S GATE (ONE VRELL PALETTE): PULSE BOLTS SPENT ON TWO CLOSED SHELLS (ROUND 84'S WARDEN A AS A"
             " STAND-IN), THEN ONE BROKEN", field)]
    save("shell", sheet_of("WINDOW SHELL - FINAL EFFECTS (ENEMIES, WINDOW SHIELDS)", rows), gif, "window-shell")


# =========================================================================== teleport

def ring_tp(f, large=False, n=10):
    u = (f + 0.5) / n
    N = 84 if large else 56
    x, y = grid(N, N)
    d = np.hypot(x - N / 2, y - N / 2)
    lf = np.zeros(x.shape + (3,))
    if large:
        r = 34 - 28 * u
        add(lf, VIOLET, gauss(d - r, 1.7) * (0.6 + 0.5 * u))
        add(lf, VIOLET_W, gauss(d - r, 0.6) * (0.5 + 0.4 * u))
    else:
        r, k = 22 - 19 * u, 0.5 + 0.5 * u
        add(lf, VIOLET, gauss(d - r, 1.5) * 0.9 * k)
        add(lf, VIOLET_W, gauss(d - r, 0.6) * 0.6 * k)
    return light_image(lf, N, N)


def fade_frame(f, n=6):
    s = (f + 0.5) / n
    N = 48
    x, y = grid(N, N)
    c = N / 2
    lf = np.zeros(x.shape + (3,))
    add(lf, VIOLET, blob(x, y, c, c, 14 - 8 * s) * 0.45 * np.sin(np.pi * min(1.0, s * 1.2)))
    add(lf, VIOLET_W, blob(x, y, c, c, 4.0) * 0.4 * s)
    for k in range(6):
        a = k * TAU / 6 + 0.4 + 1.5 * s
        r = 17 * (1 - s) + 2
        add(lf, VIOLET_W, blob(x, y, c + np.cos(a) * r, c + np.sin(a) * r, 2.0) * 0.8 * np.sin(np.pi * s))
    return light_image(lf, N, N)


def build_teleport():
    write("teleport", "teleport-fade", [fade_frame(f) for f in range(6)])
    write("teleport", "teleport-ring", [ring_tp(f) for f in range(10)])
    write("teleport", "teleport-ring-large", [ring_tp(f, True) for f in range(10)])
    print(f"teleport: {where('teleport')}")


def teleport_review():
    fades, rings, large = load("teleport", "teleport-fade"), load("teleport", "teleport-ring"), \
        load("teleport", "teleport-ring-large")
    bg = still("l44").crop((80, 40, 400, 340))
    unit = needler()
    gif = []
    hops = [(80, 80, 200, 150), (200, 150, 120, 230), (240, 70, 140, 120)]
    for i in range(54):
        t = i / FPS
        f = bg.copy()
        for j, (x0, y0, x1, y1) in enumerate(hops):
            u = (t - 0.2 * j) % 1.8
            if u < 0.2:
                f = put_alpha(f, unit, x0 - 18, y0 - 18, 1 - u / 0.2)
                f = put_light(f, fades[min(5, int(u / 0.2 * 6))], x0, y0)
            elif u < 0.7:
                f = put_light(f, (large if j == 2 else rings)[min(9, int((u - 0.2) / 0.5 * 10))], x1, y1)
            elif u < 1.6:
                f = put_alpha(f, unit, x1 - 18, y1 - 18)
            else:
                f = put_alpha(f, unit, x0 - 18, y0 - 18, (u - 1.6) / 0.2)
        gif.append(f)
    field = Image.new("RGBA", (3 * 324, 300), artkit.PLATE)
    for j, i in enumerate((4, 12, 20)):
        field.alpha_composite(gif[i], (j * 324, 0))
    rows = [("THE FADE OUT (0.2 S), 3X", tile(fades, 3)),
            ("THE RIFT SKATER'S ARRIVAL RING, SHRINKING ONTO THE POINT (0.5 S, ROUND 84 A.2 A), 2X", tile(rings, 2)),
            ("THE CHOIR SERAPH'S, LARGER (VIOLET ONLY, E.5 A), 1X", tile(large, 1)),
            ("1X OVER LEVEL 44'S EVENT PLANE: THREE JUMPS (A NEEDLER AS THE UNIT'S STAND-IN), THE RIGHT ONE THE LARGE RING",
             field)]
    save("teleport", sheet_of("TELEPORT - FINAL EFFECTS (ENEMIES, MOVEMENT TELEPORT)", rows), gif, "teleport")


# =========================================================================== the spore cloud (stand-in)

def _cloud_fields(phase):
    x, y = grid(SPORE_N, SPORE_N)
    c = SPORE_N / 2
    dx, dy = x - c, y - c
    rho, ang = np.hypot(dx, dy), np.arctan2(dy, dx)
    wob = 1 + 0.08 * np.sin(3 * ang + phase) + 0.05 * np.sin(5 * ang - 2 * phase + 1.3)
    env = np.exp(-(rho / (SPORE_R * 0.86 * wob)) ** 4) * smooth((SPORE_N / 2 - 3 - rho) / 22)   # 0 before the rim
    tex = 0.8 + 0.1 * np.sin(dx / 9 + phase) * np.sin(dy / 11 - phase) + 0.1 * np.sin((dx + dy) / 7 + 2 * phase)
    return x, y, rho, ang, env, tex


def cloud_frame(f, n=8, front=None):
    """The cloud's glow and specks; with ``front`` (a radius) only what lies inside it, and the ember edge there."""
    phase = TAU * f / n
    x, y, rho, ang, env, tex = _cloud_fields(phase)
    c = SPORE_N / 2
    inside = 1.0 if front is None else smooth((front - rho) / 6 + 0.5)
    lf = np.zeros(x.shape + (3,))
    add(lf, VIOLET, env * tex * 0.32 * inside)
    rng = np.random.default_rng(45)
    for _ in range(46):
        a0, r0, sp = rng.uniform(0, TAU), SPORE_R * np.sqrt(rng.uniform(0, 0.8)), rng.choice((-1, 1))
        a = a0 + sp * phase / 6
        px, py = c + np.cos(a) * r0, c + np.sin(a) * r0
        tw = 0.6 + 0.4 * np.sin(phase + a0 * 3)
        add(lf, VIOLET_W, blob(x, y, px, py, 1.4) * 0.3 * tw * inside)
    if front is not None and front > 0:
        edge = gauss(rho - front * (1 + 0.06 * np.sin(7 * ang + 2.0) + 0.04 * np.sin(11 * ang)), 2.5)
        add(lf, EMBER, edge * 0.8 * env ** 0.3)
        add(lf, EMBER_W, edge ** 3 * 0.5 * env ** 0.3)
    return light_image(lf, SPORE_N, SPORE_N)


def veil():
    _, _, _, _, env, tex = _cloud_fields(0.0)
    return alpha_image(env * (0.85 + 0.15 * (tex - 0.8) / 0.2) * 0.25, (30, 12, 44), SPORE_N, SPORE_N)


def build_spore():
    write("spore-cloud", "spore-cloud-veil", [veil()], single=True)
    write("spore-cloud", "spore-cloud", [cloud_frame(f) for f in range(8)])
    write("spore-cloud", "spore-cloud-burn", [cloud_frame(f % 8, front=SPORE_R * 1.05 * (1 - (f + 0.5) / 10))
                                              for f in range(10)])
    print(f"spore cloud: {where('spore-cloud')}")


def spore_review():
    veil_, clouds, burns = load("spore-cloud", "spore-cloud-veil")[0], load("spore-cloud", "spore-cloud"), \
        load("spore-cloud", "spore-cloud-burn")
    bg = still("l45").crop((60, 60, 380, 400))
    gif = []
    for i in range(90):
        t = i / FPS
        f = bg.copy()
        for j, (cx, cy, sc) in enumerate(((110, 120 + 20 * t, 1.25), (240, 230 + 20 * t, 1.0))):
            if j == 1 and t > 2.0:
                continue
            v = veil_.resize((int(SPORE_N * sc), int(SPORE_N * sc)), Image.BILINEAR)
            burning = j == 1 and t >= 1.0
            k = 1.0 if not burning else max(0.0, 1 - (t - 1.0))
            vv = v.resize((max(1, int(v.width * k)), max(1, int(v.height * k))), Image.BILINEAR)
            f = put_alpha(f, vv, cx - vv.width / 2, cy - vv.height / 2)
            fr = burns[min(9, int((t - 1.0) * 10))] if burning else clouds[int(t / 0.5) % 8]
            f = put_light(f, fr.resize((int(SPORE_N * sc), int(SPORE_N * sc)), Image.BILINEAR), cx, cy)
        g36.draw_ship(f, 120, 150 + 30 * t)
        gif.append(f)
    field = Image.new("RGBA", (3 * 324, 340), artkit.PLATE)
    for j, i in enumerate((10, 45, 70)):
        field.alpha_composite(gif[i], (j * 324, 0))
    rows = [("THE VEIL (NORMAL ALPHA, AT MOST 25 %) OVER A CHECKER, 1X", tile([veil_], 1, False)),
            ("THE CLOUD'S 8 FRAMES (A 4 S LOOP), ADDITIVE, 1X", tile(clouds[:5], 1)),
            ("", tile(clouds[5:], 1)),
            ("THE BURN (1 S, AN EMBER EDGE EATING INWARD), 1X", tile(burns[:5], 1)),
            ("", tile(burns[5:], 1)),
            ("1X OVER LEVEL 45'S REEF (ONE VRELL PALETTE): THE SHIP INSIDE A CLOUD, A SECOND CLOUD BURNT AWAY", field)]
    save("spore", sheet_of("SPORE CLOUD - STAND-IN (LEVEL 45)", rows), gif,
         "spore-cloud")


# =========================================================================== check and main

def wanted():
    return {"choir-resonator": [("choir-resonator-wave", WAVE_N), ("choir-resonator-wave-rim", WAVE_N),
                                ("choir-resonator-ring", RING_FR), ("choir-resonator-mount", 5),
                                ("choir-resonator-glow", 4)],
            "vrell-swarm-call": [("vrell-swarm-call-drone", DRONE_HEADINGS * len(CLAW)),
                                 ("vrell-swarm-call-drone-glow", 1), ("vrell-swarm-call-trail", 1),
                                 ("vrell-swarm-call-wither", 8), ("vrell-swarm-call-bloom", 9)],
            "shell": [("shell-spark", 6), ("shell-break", 8)],
            "teleport": [("teleport-fade", 6), ("teleport-ring", 10), ("teleport-ring-large", 10)],
            "spore-cloud": [("spore-cloud-veil", 1), ("spore-cloud", 8), ("spore-cloud-burn", 10)]}


def lit_border(path):
    a = np.array(Image.open(path).convert("RGBA"))
    edge = np.concatenate([a[0], a[-1], a[:, 0], a[:, -1]])
    return int((edge[:, 3] > 0).sum())


def check():
    """Every written set exists with its frame count, carries the Source chunk and (additive or veil) fades to 0
    inside its canvas; prints sizes and places."""
    bad = 0
    for root, sets in wanted().items():
        with sprites_of(root) as folder:
            for name, n in sets:
                files = [f for f in folder.glob(f"{name}*.png")
                         if f.stem == name or f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[1].isdigit()]
                ok = len(files) == n and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
                border = sum(lit_border(f) for f in files) if name in ADDITIVE or name.endswith(("-veil", "-rim")) else 0
                ok = ok and border == 0
                size = Image.open(sorted(files)[0]).size if files else None
                print(f"{'ok ' if ok else 'BAD'} {name}: {len(files)}/{n} frames {size} in {where(root)}"
                      + (f", {border} lit border px" if border else ""))
                bad += not ok
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    parts = [a for a in args if not a.startswith("--")] or ["resonator", "swarm", "shell", "teleport", "spore"]
    build = "--review" not in args
    if "resonator" in parts:
        if build:
            build_resonator()
        resonator_review(load_resonator())
    if "swarm" in parts:
        if build:
            build_swarm()
        swarm_review(load_swarm())
    if "shell" in parts:
        if build:
            build_shell()
        shell_review()
    if "teleport" in parts:
        if build:
            build_teleport()
        teleport_review()
    if "spore" in parts:
        if build:
            build_spore()
        spore_review()
