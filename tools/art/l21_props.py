#!/usr/bin/env python3
"""Production art: Level 21's props (design/campaign/act-3-red-dust/level-21-dust-colossus, "Layout", "Ground
targets" and "Secrets and pickups"; M7 part H, step ART-H1, review round 44; straight to production, no concept:
designed to the README and the approved Hellas mood still `mars-hellas-r36-a` (its ochre ramp), the landslides' red
sand of tools/art/backdrop_l21.py, Mars light from the upper left and readability rule 7 (the light rim) for the
secret).

Outputs:
  survey-crawler_0..2   64x56 sprite (assets/sprites/, or build/art-held/sprites/ while no data claims the root,
                        vanguard.pipeline.SpriteUse via l19_props.sprites_of; rerun once the level's ground target
                        names it): the Hellas survey crawler under its landslide on a ledge of the inner wall (the
                        secret, a 20 HP `ground` trigger, hard 28): 0 buried to its cab (the cab's roof and the folded
                        drill mast showing), 1 hit (the slide blown lower and scorched, the cargo bed showing),
                        2 cleared (the sand pushed aside, the cargo hatch open, the supply crate on the bed); a
                        colonial deep-survey crawler parked across the slope, off-white with the amber band, the
                        cab at the left; rule 7's light rim on every frame
  cdf-hauler-wreck      176x120 backdrop piece (assets/backdrop/level-21/; tools/art/backdrop_l21.py lists and
                        places it): a CDF heavy hauler's burnt wreck on the inner wall, slewed across the slope, its
                        container torn open, its crates spilled; the t~36 Spine Turret nest stands on the container's
                        roof at DECK (the spots, px from the piece's centre, x right, y up the screen)
  design/campaign/act-3-red-dust/level-21-dust-colossus/concept/l21-props-final-r44-a.png/.gif   the review sheet
                        and GIF: the crawler's three states at 3x and over the inner wall at t=44, the hauler's wreck
                        with its deck spots, and the ridge pass over the dune sea (t=64-72) drawn from Level 20's
                        `colossus-ridge` sprites (tools/art/l20_props.py, reused unchanged) along a stand-in path

Run: python3 tools/art/l21_props.py [crawler] [hauler] [--review]   (~1 min; no subject = all; --review rebuilds
only the review from the written files)
"""
import math
import sys

import numpy as np
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, write_gif

import backdrop_l21 as b21  # noqa: E402  (tools/art: the level's play field for the review, its timeline)
import ground_targets as gt  # noqa: E402  (concept script, imported unchanged: the light rim)
import l19_props as l19  # noqa: E402  (tools/art: the claimed/held rule, the render helper)
import l20_props as l20  # noqa: E402  (tools/art: the ridge's heading rule)
from render import raster  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, union  # noqa: E402

SCRIPT = "l21_props.py"
BATCH = "M7 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-21-dust-colossus"
CONCEPT = LEVEL / "concept"
REVIEW = CONCEPT / "l21-props-final-r44-a.png"


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, l19.HELD):          # no stale copy in the other folder
        for old in folder.glob(f"{name}*.png"):
            if l19._owned(old.stem, name):
                old.unlink()
    with l19.sprites_of(name) as where:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} frame(s) {frames[0].width}x{frames[0].height} -> {where.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, l19.HELD):
        files = sorted(folder.glob(f"{name}_*.png"), key=lambda p: int(p.stem.rsplit("_", 1)[1]))
        files = [f for f in files if l19._owned(f.stem, name)]
        if files:
            return [Image.open(f).convert("RGBA") for f in files]
    raise FileNotFoundError(name)


# =========================================================================== the survey crawler

CRAWLER = (64, 56)
CRAWLER_SCALE = 0.78                     # model units to px
WHITE = (0.82, 0.8, 0.74)
AMBER = (0.86, 0.52, 0.16)
REDSAND = (0.8, 0.52, 0.36)


def crawler_mats(scorch=0.0):
    soot = (lambda p, n: np.where(np.hypot(p[:, 0] - 6, p[:, 1] + 2) < 12, 1 - 0.55 * scorch, 1.0)) if scorch else None
    sand_pat = (lambda p, n: (0.94 + 0.12 * (n[:, 2] > 0.8)) * np.where(
        np.hypot(p[:, 0] - 6, p[:, 1] + 1) < 13, 1 - 0.5 * scorch, 1.0))
    return [
        Material(WHITE, metal=0.25, shininess=50, spec=0.5, pattern=soot),                         # 0 hull
        Material(AMBER, metal=0.3, shininess=50, spec=0.5),                                         # 1 the band
        Material((0.16, 0.16, 0.17), metal=0.3, shininess=20, spec=0.2),                            # 2 tracks
        Material((0.12, 0.2, 0.26), metal=0.6, shininess=90, spec=1.0),                             # 3 cab glass
        Material((0.5, 0.5, 0.52), metal=0.7, shininess=80, spec=0.8),                              # 4 the mast
        Material(REDSAND, metal=0.0, shininess=8, spec=0.06, pattern=sand_pat),                     # 5 the slide
        Material((0.4, 0.46, 0.3), metal=0.2, shininess=30, spec=0.3,
                 pattern=lambda p, n: np.where(np.abs(p[:, 0] - 7) < 1.2, 2.1, 1.0)),               # 6 the crate
        Material((0.05, 0.05, 0.06), metal=0.0, shininess=4, spec=0.05),                            # 7 the open hold
    ]


def heap(p, c, ax, ay, az, seed):
    """A lumpy sand mound as a height field (a gaussian hump with a few gentle waves), cut at the ground: a safe
    distance bound for the ray march (its slopes stay below 1)."""
    x, y = p[:, 0] - c[0], p[:, 1] - c[1]
    top = az * np.exp(-(x / ax) ** 2 - (y / ay) ** 2) * (1 + 0.08 * np.sin(x * 0.45 + seed) * np.sin(y * 0.5 - seed))
    return np.maximum((p[:, 2] - (top - 1.0)) * 0.55, -p[:, 2])      # sunk 1 px: no ground plane round it


def crawler_scene(state):
    """The crawler (about 50 x 22 px) parked across the slope, the cab at the left (x < -12), the cargo bed and the
    folded drill mast behind it; state 0 the slide over all but the cab's roof, 1 blown lower, 2 cleared."""
    def scene(p):
        p = p / CRAWLER_SCALE
        q = rotate_z(p, -0.12)
        items = []
        for y in (-8.5, 8.5):
            items.append((sd_box(q, (2.0, y, 3.0), (23.0, 3.6, 3.0), 1.6), 2))
        items.append((sd_box(q, (3.0, 0, 7.0), (21.0, 9.0, 2.6), 1.0), 0))                       # chassis
        items.append((sd_box(q, (-16.0, 0, 11.0), (6.0, 8.6, 4.4), 1.6), 0))                     # cab
        items.append((sd_box(q, (-20.4, 0, 12.0), (1.6, 7.6, 2.0), 0.6), 3))                     # windscreen
        items.append((sd_box(q, (-16.0, 0, 13.0), (6.3, 8.9, 0.7), 0.3), 1))                     # the amber band
        if state < 2:
            items.append((sd_box(q, (8.0, 0, 11.0), (13.0, 8.0, 2.0), 0.8), 0))                  # the hold's lid
        else:
            items.append((sd_box(q, (8.0, 0, 9.4), (12.0, 7.0, 0.6), 0.3), 7))                   # the open hold
            items.append((sd_box(q, (7.0, 0.5, 11.8), (6.0, 5.0, 2.8), 0.5), 6))                 # the crate
            items.append((sd_box(q, (8.0, 9.6, 14.0), (13.0, 0.8, 4.0), 0.4), 0))                # the lid swung up
        items.append((sd_capsule(q, (-8.0, -3.0, 14.0), (20.0, -3.0, 14.5), 1.4), 4))           # the folded mast
        items.append((sd_cylinder_z(q, (21.0, -3.0, 14.5), 2.6, 1.0), 4))                       # its drill head
        if state == 0:
            items.append((heap(p, (10.0, 1.0, 0.0), 14.0, 13.0, 23.0, 1.3), 5))
            items.append((heap(p, (-6.0, 12.0, 0.0), 9.0, 7.0, 13.0, 2.1), 5))
        elif state == 1:
            items.append((heap(p, (12.0, 5.0, 0.0), 13.0, 11.0, 15.0, 1.3), 5))
            items.append((heap(p, (-4.0, 14.0, 0.0), 9.0, 6.0, 8.0, 2.1), 5))
        else:
            items.append((heap(p, (14.0, 16.0, 0.0), 12.0, 4.5, 5.0, 1.7), 5))
            items.append((heap(p, (16.0, -15.0, 0.0), 10.0, 4.0, 4.0, 2.6), 5))
            items.append((heap(p, (-24.0, -12.0, 0.0), 5.0, 4.0, 3.0, 0.4), 5))
        d, m = union(*items)
        return d * CRAWLER_SCALE, m
    return scene


def crawler_frame(state):
    mats = crawler_mats(scorch=0.0 if state == 0 else 1.0 if state == 1 else 0.5)
    img = l19.render(crawler_scene(state), mats, CRAWLER, z_top=34.0, steps=170, crisp=60)
    return Image.fromarray(gt.light_rim(np.array(img).astype(np.float64)).astype(np.uint8), "RGBA")


def crawler_build():
    write("survey-crawler", artkit.quantize_set([crawler_frame(s) for s in range(3)], 36))


# =========================================================================== the CDF hauler's wreck

HAULER = (176, 120)
HEADING = 0.32                           # the wreck's slew across the slope (rad, anticlockwise from +x)
DECK_LOCAL = ((-40.0, 0.0), (-14.0, 0.0), (12.0, 0.0))   # the turret spots on the container roof (model x, y)
DRAB = (0.46, 0.47, 0.38)


def deck_spots():
    """The turret spots in the piece: px from its centre, x right, y up the screen (the model's axes)."""
    c, s = math.cos(HEADING), math.sin(HEADING)
    return [(round(c * x - s * y, 1), round(s * x + c * y, 1)) for x, y in DECK_LOCAL]


def hauler_mats():
    soot = lambda p, n: np.where(np.hypot(p[:, 0] - 30, p[:, 1] + 4) < 22, 0.35, 1.0) * \
        np.where(np.hypot(p[:, 0] + 30, p[:, 1] - 6) < 14, 0.55, 1.0)   # noqa: E731
    panel = lambda p, n: (0.85 + 0.15 * (np.abs(np.sin(p[:, 0] * 0.6)) > 0.2)) * soot(p, n)   # noqa: E731
    return [
        Material(tuple(c * 0.75 for c in DRAB), metal=0.35, shininess=40, spec=0.4, pattern=soot),      # 0 cab, chassis
        Material(DRAB, metal=0.4, shininess=40, spec=0.4, pattern=panel),                               # 1 container
        Material((0.12, 0.12, 0.12), metal=0.1, shininess=10, spec=0.1),                                # 2 wheels
        Material((0.05, 0.05, 0.06), metal=0.0, shininess=4, spec=0.05),                                # 3 the torn hold
        Material((0.5, 0.42, 0.22), metal=0.2, shininess=30, spec=0.3),                                 # 4 crates
        Material((0.68, 0.38, 0.24), metal=0.0, shininess=8, spec=0.06),                                # 5 drifted red sand
    ]


def hauler_scene():
    """A CDF heavy hauler (about 130 x 34 px): the armoured cab at +x, a long cargo container behind it on an eight-
    wheeled chassis, the container's side torn open toward the rim with crates spilled out, burnt; slewed across the
    slope, red sand drifted against its uphill (lower) side."""
    def scene(p):
        q = rotate_z(p, HEADING)
        items = [(sd_box(q, (-12.0, 0, 4.0), (60.0, 15.0, 3.0), 1.0), 0)]
        for x in (-58.0, -38.0, 10.0, 30.0, 52.0):
            for y in (-16.0, 16.0):
                items.append((sd_cylinder_z(q - np.array([x, y, 0.0]), (0, 0, 5.0), 5.2, 3.0) - 0.6, 2))
        container = sd_box(q, (-14.0, 0, 16.0), (44.0, 14.0, 9.0), 1.2)
        tear = sd_box(q, (-6.0, -14.0, 15.0), (20.0, 5.0, 8.0), 2.0)
        items.append((np.maximum(container, -tear), 1))
        items.append((sd_box(q, (-6.0, -8.0, 9.0), (18.0, 5.0, 1.0), 0.5), 3))
        items.append((sd_box(q, (44.0, 0, 13.0), (12.0, 14.0, 7.0), 2.5), 0))
        items.append((sd_box(q, (54.0, 0, 15.0), (2.0, 11.0, 3.0), 0.8), 3))
        for i, (x, y, a) in enumerate(((-4.0, -24.0, 0.3), (8.0, -28.0, -0.5), (-16.0, -27.0, 0.9), (2.0, -36.0, 0.1))):
            items.append((sd_box(rotate_z(q - np.array([x, y, 0.0]), a), (0, 0, 3.0), (4.0, 3.0, 3.0), 0.4), 4))
        for x, y, r in ((-24.0, -21.0, 5.0), (30.0, -19.0, 4.0)):
            items.append((sd_cylinder_z(q, (x, y, 0.0), r, 1.0) - 1.5, 5))
        drift = np.maximum((np.abs(q[:, 1] + 20.0) - 6.0) * 0.6 + np.maximum(q[:, 2] - 2.5, 0) * 0.8,
                           np.abs(q[:, 0] + 10.0) - 66.0)
        items.append((np.maximum(drift, -p[:, 2]), 5))
        return union(*items)
    return scene


def hauler_build():
    img = b21.sdf_piece(hauler_scene(), hauler_mats(), HAULER, z_top=40, f=4, shadow=(4, 5), colours=32)
    b21.OUT.mkdir(parents=True, exist_ok=True)
    artkit.save_png(img, b21.OUT / "cdf-hauler-wreck.png", SOURCE)
    print(f"  cdf-hauler-wreck: {img.size[0]}x{img.size[1]} -> {(b21.OUT / 'cdf-hauler-wreck.png').relative_to(ROOT)}"
          f"; deck spots {deck_spots()}")


# =========================================================================== review

def arr(img):
    return np.array(img.convert("RGBA")).astype(np.float64)


def add_over(dst, src, x, y):
    h, w = src.shape[:2]
    y0, y1 = max(0, y), min(dst.shape[0], y + h)
    x0, x1 = max(0, x), min(dst.shape[1], x + w)
    if y1 > y0 and x1 > x0:
        dst[y0:y1, x0:x1] = np.clip(dst[y0:y1, x0:x1] + src[y0 - y:y1 - y, x0 - x:x1 - x, :3], 0, 255)


RIDGE_T = (64.0, 72.0)


def head_at(t):
    """The ridge head's ground (x, position) at t: from the right edge to the left across the upper half of the
    field (the README's ridge pass), a gentle S; a review stand-in for the game's `ridge` key (Q7f)."""
    u = (t - RIDGE_T[0]) / (RIDGE_T[1] - RIDGE_T[0])
    x = 540 - 640 * u + 26 * math.sin(u * 7)
    pos = b21.scroll_at(t) + b21.SCREEN - 150 - 40 * math.sin(u * 3.2)
    return x, pos


def ridge_parts(t, length=700):
    pts = [head_at(t - s * 0.01) for s in range(0, 1200) if t - s * 0.01 >= RIDGE_T[0] - 2]
    out, run = [pts[0]], 0.0
    for a, b in zip(pts, pts[1:]):
        run += math.hypot(b[0] - a[0], b[1] - a[1])
        if run >= l20.SPACING:
            out.append(b)
            run = 0.0
        if len(out) * l20.SPACING >= length:
            break
    parts = []
    for i, (x, pos) in enumerate(out):
        j, k = max(i - 1, 0), min(i + 1, len(out) - 1)
        dx, dpos = out[j][0] - out[k][0], out[j][1] - out[k][1]
        kind = "head" if i == 0 else "tail" if i == len(out) - 1 else "seg"
        parts.append((x, pos, kind, l20.nearest_heading(dx, -dpos)))
    return parts[::-1]


def ridge_frame(t, placed, sets):
    fr = b21.frame_at(t, placed).astype(np.float64)
    if not RIDGE_T[0] <= t <= RIDGE_T[1] + 1.5:
        return fr.astype(np.uint8)
    view = b21.scroll_at(t)
    pulse = 0.55 + 0.45 * math.sin(t * 5.0)
    for x, pos, kind, k in ridge_parts(t):
        img = sets[{"seg": "colossus-ridge", "head": "colossus-ridge-head", "tail": "colossus-ridge-tail"}[kind]][k]
        y = b21.SCREEN - (pos - view)
        b21.over(fr, img, int(round(x - img.shape[1] / 2)), int(round(y - img.shape[0] / 2)))
        if kind == "seg":
            g = sets["colossus-ridge-glow"][k] * pulse
            add_over(fr, g, int(round(x - g.shape[1] / 2)), int(round(y - g.shape[0] / 2)))
    return np.clip(fr, 0, 255).astype(np.uint8)


def crawler_over(t, placed, frame_img):
    fr = b21.frame_at(t, placed).astype(np.float64)
    view = b21.scroll_at(t)
    pos = b21.scroll_at(b21.SURVEY[0]) + b21.MID
    a = arr(frame_img)
    b21.over(fr, a, int(round(b21.SURVEY[1] - a.shape[1] / 2)), int(round(b21.SCREEN - (pos - view) - a.shape[0] / 2)))
    return np.clip(fr, 0, 255).astype(np.uint8)


def review():
    sets = {n: [arr(f) for f in load(n)] for n in ("colossus-ridge", "colossus-ridge-head", "colossus-ridge-tail",
                                                    "colossus-ridge-glow")}
    placed = [p for _, p in b21.placements()]
    crawler = load("survey-crawler")
    sheet = raster.sheet(1280, 720, "LEVEL 21 DUST COLOSSUS - PROPS (FINAL R44)", "PRODUCTION ART, M7 PART H - R44")
    raster.draw_text(sheet, 16, 38, "THE SURVEY CRAWLER 0-2 AT 3X (BURIED, HIT, CLEARED) AND OVER THE INNER WALL AT T=44 "
                     "(0.5X: 0 AND 2)", raster.LABEL)
    for j, im in enumerate(crawler):
        sheet.alpha_composite(im.resize((CRAWLER[0] * 3, CRAWLER[1] * 3), Image.NEAREST), (16 + j * 200, 56))
    for j, s in enumerate((0, 2)):
        fr = Image.fromarray(crawler_over(44.0, placed, crawler[s]), "RGB").resize((240, 270), Image.BOX)
        sheet.paste(fr, (620 + j * 250, 56))
        raster.draw_text(sheet, 620 + j * 250, 330, f"T=44 STATE {s}", raster.LABEL_DIM)
    y = 360
    raster.draw_text(sheet, 16, y, "THE CDF HAULER'S WRECK AT 1X (A BACKDROP PIECE) WITH THE TURRET NEST'S DECK SPOTS", raster.LABEL)
    hauler = Image.open(b21.OUT / "cdf-hauler-wreck.png").convert("RGBA")
    plain = hauler.copy()
    marked = hauler.copy()
    d = ImageDraw.Draw(marked)
    for dx, dy in deck_spots():
        cx, cy = HAULER[0] / 2 + dx, HAULER[1] / 2 - dy
        d.ellipse((cx - 3, cy - 3, cx + 3, cy + 3), outline=(255, 60, 200, 255))
    bg = Image.new("RGBA", (HAULER[0] * 2 + 30, HAULER[1]), (150, 120, 84, 255))
    bg.alpha_composite(plain, (0, 0))
    bg.alpha_composite(marked, (HAULER[0] + 30, 0))
    sheet.alpha_composite(bg, (16, y + 16))
    raster.draw_text(sheet, 16, y + 16 + HAULER[1] + 4, f"DECK SPOTS (PX FROM CENTRE, Y UP): {deck_spots()}",
                     raster.LABEL_DIM)
    raster.draw_text(sheet, 420, y + 160, "THE RIDGE PASS OVER THE DUNE SEA (T=65, 68, 71; 0.5X): LEVEL 20'S COLOSSUS-RIDGE",
                     raster.LABEL)
    raster.draw_text(sheet, 420, y + 172, "SPRITES ALONG A STAND-IN PATH (THE GAME'S RIDGE KEY, Q7F)", raster.LABEL_DIM)
    for j, t in enumerate((65.0, 68.0, 71.0)):
        fr = Image.fromarray(ridge_frame(t, placed, sets), "RGB").resize((120, 135), Image.BOX)
        sheet.paste(fr, (420 + j * 130, y + 190))
    CONCEPT.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(REVIEW, optimize=True)
    gif = [Image.fromarray(ridge_frame(float(t), placed, sets), "RGB").resize((240, 270), Image.BOX)
           for t in np.arange(63.0, 74.0, 0.1)]
    gif += [Image.fromarray(crawler_over(44.0, placed, crawler[s]), "RGB").resize((240, 270), Image.BOX)
            for s in (0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2) for _ in range(2)]
    write_gif(gif, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif")


if __name__ == "__main__":
    subjects = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--review" not in sys.argv[1:]:
        if not subjects or "crawler" in subjects:
            crawler_build()
        if not subjects or "hauler" in subjects:
            hauler_build()
    if "--build-only" not in sys.argv[1:]:
        review()
