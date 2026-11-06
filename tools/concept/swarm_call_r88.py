#!/usr/bin/env python3
"""Concept round 88 - the Vrell Swarm Call's drone, a/b proposals (QA-1 of m11a-gaps §7; step ART-A, M11 part A).

The special as its document describes it (design/player/specials/README.md, *Vrell Swarm Call (L46)*): six friendly
Vrell drones, **the Needler's body re-grown in the ship's colours**, 24 px, hunting for 10 s, each with a faint trail.
The question is only the shell's colour, so that the drones never read as a Vrell wave over Vrell space's one palette
(round 84 Q10 b: round 06's black-violet, violet, teal and pale mint):

  a "teal-black": the Needler's crab body (render/enemy_models.needler_b, the production Needler's model) with a
    teal-black shell, its plate seams, eye and gland glowing **white-cyan** where the Vrell's glow teal and pink; a
    faint white-cyan trail. (Recommended; the drafted look.)
  b "bone-white": the same body and glow with the shell paled to bone-white (the player's side never wears the
    Vrell's dark chitin).

Each variant's sheet: the source render, the 16 headings at play size (3x and 1x) beside the production Needler, and the
readability check: three drones hunting round two Needlers, the ship and Rook's stand-in, the Vrell's outlined orbs,
over Level 44's brightest and darkest window (the event plane, ``vrell-moods-r84-b``'s still) and Level 46's (the
graveyard), at 1x and 2x. The GIF: the 480x540 play field, 3 s over Level 44 then 3 s over Level 46, six drones
fanning out of the ship's bloom onto a row of Needlers.

Models on the frozen ``render`` package; the stills are act7_r84.py's ``one_palette`` over enemies_r84.py's level
stand-ins (imported unchanged). Every glow is a smooth light field added once (no dither), fading to 0 in its crop.
tools/art/act7_gear.py imports ``drone_model`` and ``DRONE`` from here for the production frames (variant a).

Outputs: design/player/specials/concept/swarm-call-drone-r88-{a,b}.{png,gif}
Run: python3 tools/concept/swarm_call_r88.py [a|b]   (no args = both, ~40 s)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import act7_r84 as a84  # noqa: E402  (one_palette: Q10 b)
import enemies_r60 as e60  # noqa: E402
import enemies_r84 as u84  # noqa: E402  (the L44/L46 stand-ins, the outlined Vrell orbs)
import gear_r36 as g36  # noqa: E402  (the production Stormhawk)
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.sdf import Material, rotate_z  # noqa: E402

OUT = ROOT / "design" / "player" / "specials" / "concept"
SUB = "CONCEPT ROUND 88 - QA-1"
TAU = 2 * np.pi
DRONE = 24                     # px (the document's)
EXTENT = 2.6                   # model units across the sprite (the needler's 2.3, room for the claws at any heading)
WHITE_CYAN = np.array((0.74, 1.0, 1.0))
GLOW_RGB = (190, 250, 255)
light = e60.light


def drone_mats(v, glow=1.0):
    """The needler_b material slots (body, dark, seam, glow, eye, bone, sac) re-grown in the ship's colours."""
    g = glow
    if v == "a":
        body, dark, bone = (0.09, 0.24, 0.25), (0.03, 0.1, 0.11), (0.42, 0.55, 0.56)
    else:
        body, dark, bone = (0.86, 0.84, 0.76), (0.52, 0.5, 0.46), (0.95, 0.93, 0.87)
    return [
        Material(body, metal=0.3, shininess=70, spec=0.9, pattern=em._plates(3.0, 0.07)),
        Material(dark, metal=0.35, shininess=60, spec=0.7, pattern=em._ridges(30, 0.3)),
        Material(body, metal=0.3, shininess=70, spec=0.9, pattern=em._plates(3.0, 0.07),
                 emission=tuple(WHITE_CYAN * 2.2 * g), emission_pattern=em._plate_gaps(3.0, 0.1)),
        Material((0.04, 0.04, 0.04), emission=tuple(WHITE_CYAN * 1.6 * g)),
        Material((0.04, 0.04, 0.04), emission=tuple(WHITE_CYAN * 1.8 * g)),
        Material(bone, metal=0.15, shininess=50, spec=0.6),
        Material(dark, metal=0.1, shininess=60, spec=0.9, emission=tuple(WHITE_CYAN * 0.9 * g),
                 emission_pattern=em._veins(9, 0.3, 0.35)),
    ]


def drone_model(v, heading=0.0, anim=0.0, glow=1.0):
    """The Needler's body (needler_b, nose down at heading 0) turned ``heading`` radians clockwise on the screen."""
    scene, _ = em.needler_b(anim=anim, glow=glow)
    return (lambda p: scene(rotate_z(p, -heading))), drone_mats(v, glow)


_CACHE = {}


def drone_sprite(v, k, anim=0.0):
    """Heading k of 16 (k x 22.5 degrees clockwise from straight down) at play size."""
    key = (v, k, anim)
    if key not in _CACHE:
        scene, mats = drone_model(v, TAU * k / 16, anim)
        _CACHE[key] = sprite.make_sprite(sdf.render(scene, mats, (DRONE * 8, DRONE * 8), EXTENT), 8, 32)
    return _CACHE[key]


def heading_of(dx, dy):
    """Screen direction (y down) -> the nearest of 16 headings clockwise from straight down."""
    return int(round(np.arctan2(-dx, dy) / TAU * 16)) % 16


def trail(img, x, y, dx, dy, k=1.0):
    """The faint white-cyan trail: a short smooth streak behind the drone, and its soft body glow (additive)."""
    n = np.hypot(dx, dy) or 1.0
    img = light(img, x, y, 7.0, GLOW_RGB, 0.26 * k)
    for i in range(1, 7):
        img = light(img, x - dx / n * (6 + 2.6 * i), y - dy / n * (6 + 2.6 * i), 2.2 - 0.18 * i,
                    GLOW_RGB, 0.32 * k * (1 - i / 7.5))
    return img


def needler():
    return Image.open(ROOT / "assets" / "sprites" / "needler_0.png").convert("RGBA")


def stills():
    """(Level 44's event plane, Level 46's graveyard) in the one Vrell palette (vrell-moods-r84-b's stills)."""
    return (a84.one_palette(u84.EventPlane(4401, 1.0).frame(0.0)).convert("RGBA"),
            a84.one_palette(u84.graveyard_bg(4601)).convert("RGBA"))


def windows(img, w=200, h=120):
    """The brightest and darkest w x h window of a still (by mean luminance)."""
    a = np.array(img.convert("RGB")).astype(float) @ np.array([0.3, 0.59, 0.11])
    best = []
    for y in range(0, img.height - h, 20):
        for x in range(0, img.width - w, 20):
            best.append((a[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return [(img.crop((x, y, x + w, y + h)), m) for m, x, y in (best[-1], best[0])]


def readability(crop, v):
    """Three drones hunting round two Needlers, Vrell orbs, the ship's nose at the foot."""
    c = crop.copy()
    for x, y in ((60, 34), (138, 30)):
        sprite.paste_center(c, needler(), x, y)
    u84.vorb(c, 100, 60, 4, u84.VIOLET)
    u84.vorb(c, 30, 74, 4, u84.TEAL)
    for x, y, dx, dy in ((36, 52, 1.0, -0.6), (84, 18, -1.0, 0.3), (164, 58, -0.4, -1.0)):
        c = trail(c, x, y, dx, dy)
        sprite.paste_center(c, drone_sprite(v, heading_of(dx, dy)), x, y)
    g36.draw_ship(c, 100, 112)
    return c


def scene_frame(v, bgs, t):
    """The GIF: six drones out of the bloom onto three Needlers at the top, over L44 (t < 3) then L46."""
    bg = bgs[0] if t < 3.0 else bgs[1]
    tt = t % 3.0
    f = bg.copy()
    px, py = 240 + 50 * np.sin(tt * 1.3), 450
    targets = [(150 + 30 * np.sin(tt + i), 140 + 16 * np.cos(tt * 0.8 + i)) for i in range(3)]
    for x, y in targets:
        sprite.paste_center(f, needler(), x, y)
    if tt < 0.3:
        f = light(f, px, py + 26, 10 + 40 * tt, (220, 255, 230), 0.9 * (1 - tt / 0.3))
    for i in range(6):
        tx, ty = targets[i % 3]
        tx += 250 * (i % 2)
        a = i * TAU / 6 + tt * 3.0
        ox, oy = tx + np.cos(a) * 46, ty + 50 + np.sin(a) * 30
        u = min(1.0, tt / 0.9)
        x = px + (ox - px) * e60.smooth(u)
        y = py + (oy - py) * e60.smooth(u)
        dx, dy = (ox - px) * (1 - u) - np.sin(a) * 46 * u, (oy - py) * (1 - u) + np.cos(a) * 30 * u
        f = trail(f, x, y, dx, dy)
        sprite.paste_center(f, drone_sprite(v, heading_of(dx, dy), [0.0, -2.0, -3.5, 2.8][int(t * 8) % 4]), x, y)
    g36.draw_ship(f, px, py, t)
    raster.draw_text(f, 10, 524, f"{'L44 EVENT PLANE' if t < 3 else 'L46 GRAVEYARD'} - DRONES {v.upper()} BESIDE NEEDLERS",
                     raster.LABEL)
    return f


def make(v):
    bgs = stills()
    w = 1000
    rows = []
    hi = sdf.render(*drone_model(v), (200, 200), EXTENT)
    src = sprite.to_image(hi)
    heads = [drone_sprite(v, k) for k in range(16)]
    strip = Image.new("RGBA", (8 * 120, 152), (12, 12, 22, 255))
    for k, s in enumerate(heads):
        strip.alpha_composite(sprite.enlarge(s, 3), ((k % 8) * 120 + 2, (k // 8) * 76 + 2))
    rows.append(("SOURCE RENDER; THE NEEDLER (36 PX, PRODUCTION) AND THE DRONE (24 PX) AT 1X AND 3X", None))
    top = Image.new("RGBA", (w - 32, 210), (12, 12, 22, 255))
    top.alpha_composite(src, (0, 4))
    n = needler()
    top.alpha_composite(n, (230, 20))
    top.alpha_composite(sprite.enlarge(n, 3), (280, 20))
    top.alpha_composite(heads[0], (420, 26))
    top.alpha_composite(sprite.enlarge(heads[0], 3), (460, 20))
    top.alpha_composite(sprite.enlarge(heads[3], 3), (550, 20))
    raster.draw_text(top, 230, 140, "NEEDLER (VRELL)", raster.LABEL_DIM)
    raster.draw_text(top, 420, 140, f"SWARM CALL DRONE {v.upper()}", raster.LABEL_DIM)
    rows[-1] = (rows[-1][0], top)
    rows.append(("16 HEADINGS, 3X (CLOCKWISE FROM DOWN)", strip))
    for (lv, still) in (("LEVEL 44 (EVENT PLANE)", bgs[0]), ("LEVEL 46 (GRAVEYARD)", bgs[1])):
        (bright, mb), (dark, md) = windows(still)
        panel = Image.new("RGBA", (w - 32, 250), (12, 12, 22, 255))
        rb, rd = readability(bright, v), readability(dark, v)
        panel.alpha_composite(rb, (0, 0))
        panel.alpha_composite(rd, (0, 128))
        panel.alpha_composite(sprite.enlarge(rb, 2).crop((0, 0, 400, 240)), (210, 0))
        panel.alpha_composite(sprite.enlarge(rd, 2).crop((0, 0, 340, 240)), (620, 0))
        rows.append((f"{lv}: BRIGHTEST (MEAN {mb:.0f}) AND DARKEST (MEAN {md:.0f}) WINDOW AT 1X; BRIGHT AND DARK AT 2X",
                     panel))
    hgt = 60 + sum(r.height + 30 for _, r in rows) + 10
    sheet = raster.sheet(w, hgt, f"VRELL SWARM CALL DRONE {v.upper()} - "
                         + ("TEAL-BLACK SHELL, WHITE-CYAN GLOW" if v == "a" else "BONE-WHITE SHELL, WHITE-CYAN GLOW"), SUB)
    y = 40
    for label, img in rows:
        raster.draw_text(sheet, 16, y, label, raster.LABEL)
        sheet.alpha_composite(img, (16, y + 14))
        y += img.height + 30
    OUT.mkdir(parents=True, exist_ok=True)
    png = OUT / f"swarm-call-drone-r88-{v}.png"
    sheet.convert("RGB").save(png, optimize=True)
    frames = [scene_frame(v, bgs, i / 15) for i in range(90)]
    size = rig.write_gif(frames, OUT / f"swarm-call-drone-r88-{v}.gif", fps=15, colors=128)
    print(f"{png.relative_to(ROOT)} + gif ({size / 1e6:.1f} MB)")


if __name__ == "__main__":
    for variant in [a for a in sys.argv[1:] if a in ("a", "b")] or ["a", "b"]:
        make(variant)
