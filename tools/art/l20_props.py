#!/usr/bin/env python3
"""Production art: Level 20's props (design/campaign/act-3-red-dust/level-20-retreat-from-hellas, "Layout" and
"Secrets and pickups"; M7 part G, step ART-G1, review round 43; straight to production, no concept: designed to
the README and the approved Hellas mood still `mars-hellas-r36-a` (its ochre ramp, the ridge's lime-cracked
crest), Mars light from the upper left and readability rule 7 (the light rim) for the secret).

The militia crawlers and the rim guns are backdrop pieces (tools/art/backdrop_l20.py: scenery that never moves);
this script makes the sprites the game draws itself.

Headings as tools/art/README.md: heading k turns k x 360/16 degrees clockwise from straight down (4 = facing
left, 8 = up, 12 = right); every heading its own render under the fixed key light.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse;
rerun once the data claims it; the ridge is claimed by a level's `basin-rumble` ambience layer, step GB):
  colossus-ridge_0..15        96x96, per heading: one length of the travelling ridge (the Dust Colossus's body
                              under the sand, shared with Level 21, m7g-gaps.md 1.8): a sand hump ~56 px across,
                              its crest cracked with dim lime lines, sand sliding off its flanks; constant along its
                              axis with its ends faded (1-bit dither), so copies laid every 16 px along a path
                              join; no translucent shadow (the shaded flank is opaque sand)
  colossus-ridge-head_0..15   112x112, per heading: the leading end: the hump rising higher, sand heaving in a bow
                              wave ahead, cracks fanning forward
  colossus-ridge-tail_0..15   96x96, per heading: the trailing end: the hump sinking back into a furrow
  colossus-ridge-glow_0..15   96x96, additive, per heading (aligned with the length): the crest cracks' lime glow
                              (the game pulses its alpha; drawn over each length)
  colossus-ridge-dust_0..5    48x48, stepped translucency: a sand slide's puff off a flank (6 frames at 10 fps)
  emergency-cache_0..2        36x32, the Hellas Outpost emergency cache (the secret, a 20 HP `ground` trigger): a
                              militia shelter hatch in a concrete collar, half-buried by a sand drift, its yellow
                              strobe on a short post: 0 intact, 1 hit (scorched, the lens cracked), 2 opened (the
                              lid blown aside, a supply crate in the shaft); rule 7's light rim on every frame
  emergency-cache-glow_0..7   36x32, additive, aligned with the cache: the yellow strobe (GroundGlow: 0-3 a slow
                              flash until the first hit, 4-7 a hurried double flash after it)
  design/campaign/act-3-red-dust/level-20-retreat-from-hellas/concept/l20-props-final-r43-a.png/.gif   the review
                              sheet and GIF (over tools/art/backdrop_l20.py's play field: the ridge crossing the floor
                              near the top edge at t=12-20, the cache states and strobe, the fallback briefing image)

How the game draws the ridge (GB, Level 21's boss): along the body's path (its history under the sand), the tail at
the end, lengths every 16 px with the heading of the path there, the head at the front, back to front; the glow over
each length with a pulsing alpha; a dust puff now and then at a flank. On the ground layer (it scrolls with it).

Run: python3 tools/art/l20_props.py [ridge] [cache] [--review]   (~1 min; no subject = all; --review rebuilds
only the review from the written files)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, write_gif

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, reduce, quantize)
import backdrop_l15 as b15  # noqa: E402  (tools/art: posterizing, dithered alpha)
import backdrop_l20 as b20  # noqa: E402  (tools/art: the level's play field for the review)
import ground_targets as gt  # noqa: E402  (concept script, imported unchanged: the light rim)
import l19_props as l19  # noqa: E402  (tools/art: the claimed/held rule, the render helper)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the ochre ramp, lime)
import storm_units as su  # noqa: E402  (tools/art: noisy blobs, stepped translucency)
import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged: light canvases)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_cylinder_z, union  # noqa: E402

SCRIPT = "l20_props.py"
BATCH = "M7 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-20-retreat-from-hellas"
CONCEPT = LEVEL / "concept"
REVIEW = CONCEPT / "l20-props-final-r43-a.png"
BRIEFING = ROOT / "assets" / "ui" / "briefing" / "level-20-hellas-fallback.png"
HEADINGS = 16
OCHRE = m36.OCHRE
LIME = np.array(m36.LIME, float)


# =========================================================================== held sprites

RIDGE_ROOT = "colossus-ridge"
RIDGE_LAYER = "basin-rumble"     # vanguard.pipeline.SpriteUse: a level with this ambience layer claims the ridge


def ridge_claimed():
    """Whether a level's data claims the ridge (SpriteUse, M7 part G): its music has the basin-rumble ambience layer."""
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        music = l19.siege_units._yaml(f).get("music") or {}
        if any(str(layer.get("ambience")) == RIDGE_LAYER for layer in music.get("ambience_layers") or []):
            return True
    return False


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, l19.HELD):          # no stale copy in the other folder
        for old in folder.glob(f"{name}*.png"):
            if l19._owned(old.stem, name):
                old.unlink()
    if l19.siege_units._rooted(name, RIDGE_ROOT) and ridge_claimed():
        artkit.SPRITES.mkdir(parents=True, exist_ok=True)
        artkit.write_frames(name, frames, SOURCE, single=single)
        where = artkit.SPRITES
    else:
        with l19.sprites_of(name) as where:
            artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} frame(s) {frames[0].width}x{frames[0].height} -> {where.relative_to(ROOT)}")


load = l19.load


# =========================================================================== the travelling ridge

SEG, HEAD, DUST, DUST_N = 96, 112, 48, 6
HALF_W, RIDGE_H = 22.0, 11.0          # the hump's across-sigma (px) and height
SPACING = 16                          # px between lengths along a path (the game's)


def heading_vec(k):
    th = 2 * math.pi * k / HEADINGS
    return np.array([-math.sin(th), math.cos(th)])     # screen x right, y down; k=0 down, 4 left, 8 up


def local(size, k, f=2):
    """Supersampled canvas coordinates as (along, across) for heading k (along > 0 forward)."""
    yy, xx = b15.piece_canvas((size, size), f)
    fx, fy = heading_vec(k)
    px, py = xx - size / 2, yy - size / 2
    return px * fx + py * fy, px * fy - py * fx, f


def crack_lines(a, c, seed, reach=1.0):
    """The crest's lime cracks in the length's own frame: a wandering line along the crest, short branches."""
    main = np.abs(c - 2.6 * np.sin(a / 5.3 + seed) - 1.2 * np.sin(a / 2.1 + 2 * seed)) < 0.85
    branch = (np.abs(np.sin(a / 7.0 + seed) * 9 - c) < 0.7) & (np.abs(c) < 7 * reach)
    return (main | branch).astype(np.float64)


def ridge_field(kind, k):
    """(height, albedo, cover, crack) of a ridge piece at heading k on its supersampled canvas."""
    size = HEAD if kind == "head" else SEG
    a, c, f = local(size, k)
    n = raster.fbm(size * f, size * f, 12, 2201 + k, octaves=3, period=False)
    wide = raster.fbm(size * f, size * f, 24, 2251 + k, octaves=2, period=False)
    c = c + 3 * (wide - 0.5)                                               # ragged flanks
    across = np.exp(-(c / HALF_W) ** 2)
    if kind == "seg":
        along = np.ones_like(a)
        cover_along = np.clip(1 - (np.abs(a) - 12) / 30, 0, 1)
    elif kind == "head":
        along = np.where(a < 0, 1.0, np.exp(-(a / 22) ** 2)) * (1 + 0.3 * np.exp(-((a - 2) / 14) ** 2))
        cover_along = np.clip(1 - (-a - 12) / 30, 0, 1) * np.clip(1 - (a - 40) / 10, 0, 1)
    else:
        along = np.where(a > 0, 1.0, np.exp(-(a / 26) ** 2))
        cover_along = np.clip(1 - (a - 12) / 30, 0, 1) * np.clip(1 - (-a - 44) / 2, 0, 1)
    hgt = RIDGE_H * across * along
    slide = np.exp(-((np.abs(c) - 20) / 6) ** 2) * (0.6 + 0.8 * (n - 0.5))     # sand sliding off the flanks
    hgt += 1.4 * slide * along + 1.0 * (n - 0.5)
    if kind == "head":                                                       # the bow wave of heaving sand ahead
        r = np.hypot(a - 6, c * 0.85)
        bow = np.exp(-((r - 22) / 8) ** 2) * (a > 2) * np.clip(1 - np.abs(c) / 40, 0, 1)
        hgt += 3.5 * bow
    elif kind == "tail":                                                     # the furrow it leaves
        hgt -= 2.5 * np.exp(-(c / 9) ** 2) * np.clip(-a / 30, 0, 1) * (a < 0)
    reach = 1.0 if kind == "seg" else 0.6
    crack = crack_lines(a, c, 0.0, reach) * (across * along > 0.55)
    if kind == "head":
        ang = np.arctan2(c, np.maximum(a - 6, 1e-3))
        fan = (np.abs(np.sin(ang * 5 + 0.4)) < 0.08) & (a > 6) & (a < 34) & (np.abs(c) < 20)
        crack = np.maximum(crack, fan * 1.0)
    shape = across * along
    cover = np.clip((shape + 0.35 * slide * along + 0.18 * (n - 0.5) - 0.12) * 4, 0, 1) * cover_along
    if kind == "head":
        cover = np.maximum(cover, np.clip(bow * 3 - 0.4, 0, 1) * cover_along)
    t = np.clip(0.56 + 0.18 * shape + 0.14 * (n - 0.5), 0, 1)
    alb = b15.ramp(OCHRE[1:], t)
    alb = alb * (1 - 0.55 * crack[..., None]) + np.array((60, 70, 30), float) * 0.55 * crack[..., None]
    return hgt, alb, cover, crack, f


def finish_flat(hgt, alb, cover, f, emission=None, colours=32):
    """backdrop_l19.finish_flat: lit with no fall-off gradient (copies along a path must match), receding, the
    cover dithered to 1-bit alpha."""
    em = np.zeros_like(alb) if emission is None else emission
    a = k8.relief(hgt, alb, em, np.ones(hgt.shape), f, wrap=False, ambient=0.44, key=0.7, ao=0.11, gradient=0.0)
    cv = np.clip(cover, 0, 1)
    cv[:f], cv[-f:], cv[:, :f], cv[:, -f:] = 0, 0, 0, 0
    a[..., 3] = cv
    nat = k8.reduce(a, f)
    nat[..., 3] = np.clip((nat[..., 3] - 0.5) * 2.5 + 0.5, 0, 1)
    rgb = b15.recede(nat[..., :3] * 255, 0.16)
    img = np.array(b15.posterize_rgb(rgb, colours, b15.dither_alpha(nat[..., 3], 2)))
    img[img[..., 3] == 0] = 0
    return Image.fromarray(img, "RGBA")


def ridge_piece(job):
    kind, k = job
    hgt, alb, cover, crack, f = ridge_field(kind, k)
    em = crack[..., None] * (LIME / 255 * 0.35)
    return finish_flat(hgt, alb, cover, f, emission=em, colours=32)


def ridge_glow(k):
    """The crest cracks' lime glow over a length (additive): the lines bright, a soft halo."""
    _, _, cover, crack, f = ridge_field("seg", k)
    m = crack * (cover > 0.5)
    h = m.copy()
    for _ in range(6):
        h = (h + np.roll(h, 1, 0) + np.roll(h, -1, 0) + np.roll(h, 1, 1) + np.roll(h, -1, 1)) / 5
    light = np.clip(m * 0.9 + h * 1.6, 0, 1)
    light = light.reshape(SEG, f, SEG, f).mean(axis=(1, 3))
    rgb = LIME * 0.75 + np.array((255, 255, 210), float) * 0.25 * (light > 0.6)[..., None]
    img = Image.fromarray(np.dstack([rgb, light * 255]).clip(0, 255).astype(np.uint8), "RGBA")
    return artkit.additive(img)


def ridge_dust(i):
    """A sand slide's puff off the ridge's flank: noisy ochre blobs growing and thinning (stepped alpha)."""
    S = 4
    u = (i + 1) / DUST_N
    items = [(24 + 7 * u * dx, 26 - 5 * u * dy, (7 + 13 * u) * r, (1 - u) ** 0.8 * 0.85)
             for dx, dy, r in ((0, 0, 1.0), (-1.2, 0.6, 0.7), (1.1, 1.0, 0.75), (0.3, 1.6, 0.6))]
    alpha, light, _ = su.blobs(DUST, DUST, S, items, 2231 + i)
    col = b15.ramp(OCHRE[2:], np.clip(0.35 + 0.55 * light, 0, 1))
    rgb, a = su.shrink(col, alpha, S)
    return su.stepped(rgb, a * 0.8, steps=4)


def ridge_build():
    jobs = [(kind, k) for kind in ("seg", "head", "tail") for k in range(HEADINGS)]
    with ProcessPoolExecutor(max_workers=12) as pool:
        imgs = list(pool.map(ridge_piece, jobs))
        glows = list(pool.map(ridge_glow, range(HEADINGS)))
    segs, heads, tails = imgs[:HEADINGS], imgs[HEADINGS:2 * HEADINGS], imgs[2 * HEADINGS:]
    write("colossus-ridge", artkit.quantize_set(segs, 40))
    write("colossus-ridge-head", artkit.quantize_set(heads, 40))
    write("colossus-ridge-tail", artkit.quantize_set(tails, 40))
    write("colossus-ridge-glow", glows)
    write("colossus-ridge-dust", artkit.quantize_set([ridge_dust(i) for i in range(DUST_N)], 16))


# =========================================================================== the emergency cache

CACHE = (36, 32)                           # the level data's size (step G2)
CACHE_GLOW_N = 8
HAZARD = (0.86, 0.66, 0.12)
LAMP_AT = (9.5, 7.5, 6.0)                  # the strobe's lens on its post (px; y up the screen)


def cache_mats(lens=0.3, scorched=False, broken=False):
    soot = (lambda p, n: np.where(np.hypot(p[:, 0] + 2, p[:, 1] - 1) < 9, 0.4, 1.0)) if scorched else None
    stripe = lambda p, n: np.where(((p[:, 0] + p[:, 1]) / 5.0) % 1 < 0.5, 0.16, 1.0)   # noqa: E731
    lamp_e = (0.0, 0.0, 0.0) if broken else (1.6 * lens, 1.2 * lens, 0.2 * lens)
    return [
        Material((0.58, 0.55, 0.5), metal=0.0, shininess=10, spec=0.1, pattern=soot),               # 0 collar
        Material((0.46, 0.47, 0.44), metal=0.6, shininess=80, spec=0.8, pattern=soot),               # 1 the lid
        Material(HAZARD, metal=0.1, shininess=20, spec=0.2, pattern=stripe),                         # 2 hazard ring
        Material((0.3, 0.24, 0.06), emission=lamp_e),                                                # 3 the lens
        Material((0.2, 0.2, 0.2), metal=0.5, shininess=60, spec=0.6),                                # 4 the post
        Material((0.03, 0.03, 0.03), metal=0.0, shininess=4, spec=0.05),                             # 5 the shaft
        Material((0.36, 0.42, 0.3), metal=0.2, shininess=30, spec=0.3,
                 pattern=lambda p, n: np.where(np.abs(p[:, 1] + 2) < 1.2, 2.2, 1.0)),                # 6 the crate
        Material((0.78, 0.64, 0.44), metal=0.0, shininess=8, spec=0.08),                             # 7 drift sand
    ]


def cache_scene(state=0):
    """A square concrete collar (20 px) round a round steel lid (r 6) with a hazard-striped ring, a sand drift
    over its lower-left corner, the strobe on a short post at its upper-right corner. State 2: the lid blown off
    to the right, the dark shaft and the top of a supply crate in it."""
    def scene(p):
        items = []
        collar = sd_box(p, (0, 0, 2.0), (10.0, 10.0, 2.0), 1.0)
        hole = sd_cylinder_z(p, (0, 0, 2.0), 6.6, 3.0)
        items.append((np.maximum(collar, -hole), 0))
        if state < 2:
            items.append((sd_cylinder_z(p, (0, 0, 3.4), 7.2, 0.6), 2))
            items.append((sd_cylinder_z(p, (0, 0, 4.0), 5.8, 0.8) - 0.2, 1))
            items.append((sd_box(p, (0, 0, 4.9), (3.4, 0.9, 0.4), 0.3), 4))                    # the wheel bar
        else:
            items.append((sd_cylinder_z(p, (0, 0, -2.0), 6.4, 3.0), 5))
            items.append((sd_box(p, (-0.8, -0.8, 0.6), (3.8, 3.4, 1.4), 0.4), 6))
            lid = p - np.array([10.5, -8.0, 0.0])
            items.append((sd_cylinder_z(lid, (0, 0, 1.0), 5.8, 0.8) - 0.2, 1))
        items.append((sd_cylinder_z(p, (LAMP_AT[0], LAMP_AT[1], 3.0), 1.2, 3.0), 4))
        items.append((sd_cylinder_z(p, LAMP_AT, 2.1, 1.3) - 0.3, 3))
        drift = (np.hypot((p[:, 0] + 7.5) / 1.2, p[:, 1] + 7) - 7.5) * 0.8 + np.maximum(p[:, 2] - 1.0, 0) * 0.4 \
            + np.maximum(-p[:, 2], 0)
        drift = np.maximum(drift, p[:, 2] - (5.0 - 0.4 * np.hypot(p[:, 0] + 7.5, p[:, 1] + 7)))
        items.append((drift, 7))
        d, m = union(*items)
        return d, m
    return scene


def cache_frame(state):
    mats = cache_mats(0.3, scorched=state >= 1, broken=state >= 1)
    img = l19.render(cache_scene(state), mats, CACHE, z_top=30.0, steps=170, crisp=60)
    return Image.fromarray(gt.light_rim(np.array(img).astype(np.float64)).astype(np.uint8), "RGBA")


def lamp_centre():
    """The lens's visible pixels in a probe render of the whole cache (only the lens lit)."""
    probe = [Material((0, 0, 0), emission=(1, 1, 1) if i == 3 else (0, 0, 0)) for i in range(len(cache_mats()))]
    hi, f = artkit.render_hi(cache_scene(0), probe, CACHE, float(CACHE[0]), z_top=30.0, shadows=False)
    ys, xs = np.nonzero(hi[..., 0] > 0.5)
    return (xs.mean() + 0.5) / f, (ys.mean() + 0.5) / f


def cache_glow(i, centre):
    level = (1.0, 0.45, 0.0, 0.0, 1.0, 0.0, 0.9, 0.0)[i]
    cv = v8.Canvas(*CACHE)
    d = cv.dist(*centre)
    cv.add((255, 196, 40), v8.gauss(d, 5.0) * 0.6 * level)
    cv.add((255, 246, 190), v8.gauss(d, 1.4) * 1.3 * level)
    return artkit.additive(cv.image())


def cache_build():
    frames = artkit.quantize_set([cache_frame(s) for s in range(3)], 32)
    write("emergency-cache", frames)
    c = lamp_centre()
    write("emergency-cache-glow", [cache_glow(i, c) for i in range(CACHE_GLOW_N)])


# =========================================================================== review

def nearest_heading(dx, dy):
    """The heading index (16) of a screen direction (dx, dy), y down."""
    th = math.atan2(-dx, dy)                       # heading_vec's inverse
    return int(round(th / (2 * math.pi) * HEADINGS)) % HEADINGS


def head_ground(t):
    """The ridge head's ground position at t (x, strip row): from t=12 it crosses the field from the left near
    the top edge, keeping ahead of the strike (up the strip at ~124 px/s, across at 70), a slow S; the body
    follows its own track under the sand. A review stand-in: the game's path is GB's."""
    u = t - 12.0
    x = -40 + 70 * u + 30 * math.sin(u / 1.3)
    row = b20.ORIGIN + b20.scroll_at(12.0) + 470 + 124 * u - 60 * math.sin(u / 1.6)
    return x, row


def ridge_path(t, length=900):
    """The body's lengths from the tail to the head: (x, row, kind, heading) at SPACING px along the track."""
    pts = [head_ground(t - s * 0.02) for s in range(0, 3000)]
    out, run = [pts[0]], 0.0
    for a, b in zip(pts, pts[1:]):
        run += math.hypot(b[0] - a[0], b[1] - a[1])
        if run >= SPACING:
            out.append(b)
            run = 0.0
        if len(out) * SPACING >= length:
            break
    parts = []
    for i, (x, row) in enumerate(out):
        j = max(i - 1, 0)
        k = min(i + 1, len(out) - 1)
        dx, drow = out[j][0] - out[k][0], out[j][1] - out[k][1]   # forward = toward the head (earlier index)
        kind = "head" if i == 0 else "tail" if i == len(out) - 1 else "seg"
        parts.append((x, row, kind, nearest_heading(dx, -drow)))
    return parts[::-1]


def arr(img):
    return np.array(img.convert("RGBA")).astype(np.float64)


def add_over(dst, src, x, y):
    """Additive blend of a premultiplied glow."""
    h, w = src.shape[:2]
    y0, y1 = max(0, y), min(dst.shape[0], y + h)
    x0, x1 = max(0, x), min(dst.shape[1], x + w)
    if y1 > y0 and x1 > x0:
        dst[y0:y1, x0:x1] = np.clip(dst[y0:y1, x0:x1] + src[y0 - y:y1 - y, x0 - x:x1 - x, :3], 0, 255)


def ridge_frame(t, placed, sets):
    fr = b20.frame_at(t, placed).astype(np.float64)
    if not 12.0 <= t <= 22.0:
        return fr.astype(np.uint8)
    view = b20.bottom_row(t)
    pulse = 0.55 + 0.45 * math.sin(t * 5.0)
    parts = ridge_path(t)
    for x, row, kind, k in parts:
        img = sets[{"seg": "colossus-ridge", "head": "colossus-ridge-head", "tail": "colossus-ridge-tail"}[kind]][k]
        y = b20.SCREEN - (row - view)
        b20.over(fr, img, int(round(x - img.shape[1] / 2)), int(round(y - img.shape[0] / 2)))
        if kind == "seg":
            g = sets["colossus-ridge-glow"][k] * pulse
            add_over(fr, g, int(round(x - g.shape[1] / 2)), int(round(y - g.shape[0] / 2)))
    x, row, _, _ = parts[len(parts) // 2]
    d = sets["colossus-ridge-dust"][int(t * 10) % DUST_N]
    b20.over(fr, d, int(x + 18), int(b20.SCREEN - (row - view) - 10))
    return np.clip(fr, 0, 255).astype(np.uint8)


def review():
    sets = {n: [arr(f) for f in load(n)] for n in ("colossus-ridge", "colossus-ridge-head", "colossus-ridge-tail",
                                                    "colossus-ridge-glow", "colossus-ridge-dust")}
    placed = [p for _, p in b20.placements()]
    sheet = raster.sheet(1180, 930, "LEVEL 20 RETREAT FROM HELLAS - PROPS (FINAL R43)", "PRODUCTION ART, M7 PART G - R43")
    raster.draw_text(sheet, 16, 38, "THE TRAVELLING RIDGE OVER THE FLOOR (T=13, 16, 19; 0.5X) - ITS PIECES PER HEADING AT 1X", raster.LABEL)
    for i, t in enumerate((13.0, 16.0, 19.0)):
        fr = Image.fromarray(ridge_frame(t, placed, sets), "RGB").resize((240, 270), Image.BOX)
        sheet.paste(fr, (16 + i * 250, 54))
        raster.draw_text(sheet, 16 + i * 250, 54 + 274, f"T={t:g}", raster.LABEL_DIM)
    x0, y0 = 780, 54
    for row, name in enumerate(("colossus-ridge", "colossus-ridge-head", "colossus-ridge-tail")):
        for j, k in enumerate((0, 2, 4, 6, 8)):
            img = Image.fromarray(sets[name][k].astype(np.uint8), "RGBA").resize((72, 72), Image.BOX)
            sheet.alpha_composite(img, (x0 + j * 76, y0 + row * 88))
        raster.draw_text(sheet, x0, y0 + row * 88 + 74, name.upper() + " 0,2,4,6,8 (0.75X)", raster.LABEL_DIM)
    y = 360
    raster.draw_text(sheet, 16, y, "THE CRACK GLOW (ADDITIVE, ON DARK) AND THE SAND SLIDE 0-5 AT 1X", raster.LABEL)
    for j, k in enumerate((0, 4, 8, 12)):
        img = Image.fromarray(sets["colossus-ridge-glow"][k].astype(np.uint8), "RGBA").convert("RGB")
        sheet.paste(img, (16 + j * 100, y + 16))
    for j in range(DUST_N):
        img = Image.fromarray(sets["colossus-ridge-dust"][j].astype(np.uint8), "RGBA")
        sheet.alpha_composite(img, (430 + j * 52, y + 40))
    y = 480
    raster.draw_text(sheet, 16, y, "THE EMERGENCY CACHE 0-2 AT 3X (INTACT, HIT, OPENED) AND ITS STROBE 0-7 (ADDITIVE OVER FRAME 0)", raster.LABEL)
    cache = load("emergency-cache")
    glow = load("emergency-cache-glow")
    for j, im in enumerate(cache):
        sheet.alpha_composite(im.resize((CACHE[0] * 3, CACHE[1] * 3), Image.NEAREST), (16 + j * 130, y + 16))
    base = arr(cache[0])
    for j, g in enumerate(glow):
        fr = base.copy()
        bg = np.zeros((CACHE[1], CACHE[0], 3)) + np.array((150, 120, 84))
        a = fr[..., 3:4] / 255
        out = fr[..., :3] * a + bg * (1 - a)
        out = np.clip(out + arr(g)[..., :3], 0, 255).astype(np.uint8)
        sheet.paste(Image.fromarray(out, "RGB").resize((CACHE[0] * 2, CACHE[1] * 2), Image.NEAREST), (420 + j * 80, y + 36))
    y = 640
    raster.draw_text(sheet, 16, y, "OKAFOR'S BRIEFING IMAGE LEVEL-20-HELLAS-FALLBACK (672X240 AT 1X; TOOLS/ART/BRIEFING_IMAGES.PY)", raster.LABEL)
    if BRIEFING.exists():
        sheet.alpha_composite(Image.open(BRIEFING).convert("RGBA"), (16, y + 16))
    CONCEPT.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(REVIEW, optimize=True)
    gif = [Image.fromarray(ridge_frame(float(t), placed, sets), "RGB").resize((240, 270), Image.BOX)
           for t in np.arange(11.0, 23.0, 0.1)]
    write_gif(gif, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif")


if __name__ == "__main__":
    subjects = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--review" not in sys.argv[1:]:
        if not subjects or "ridge" in subjects:
            ridge_build()
        if not subjects or "cache" in subjects:
            cache_build()
    review()
