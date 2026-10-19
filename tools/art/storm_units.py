#!/usr/bin/env python3
"""Production art: Level 18's units and its secret (M7 part E batch, concept round 41, step ART-E):
the Ghost Drone (design/enemies/air/ghost-drone, the chosen round-36 look a "facet dart" with the U.5
lamps: tools/concept/enemies_r36.ghost_a, ghost_glow and the lamp offsets imported unchanged), the Dust
Devil (design/enemies/ground/dust-devil, the chosen round-06 concept: tools/concept/render/r06_models.
dust_devil imported unchanged, its vanes parting with ``core``) and the militia rover wreck of the
level's secret (design/campaign/act-3-red-dust/level-18-dust-storm: straight to production, no concept;
designed to the README, Mars light and readability rule 7, in the militia's off-white and amber of
Level 17's flak pits).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down (for 16:
4 = facing left, 8 = up, 12 = right); every heading its own render under the fixed key light.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.
SpriteUse: an enemy's root once its data.yaml exists, the rover's once a level's ground target names it):
  ghost-drone_0..15         40x40, 16 headings: the grey facet dart, a neutral grey rim, one 28-colour
                            palette over the set; the 2x2 red eye and the white wing-tip pixels set after
                            the palette step (U.5)
  ghost-drone-glow_0..15    48x48, additive, per heading, same centre: the eye's red bloom and the two
                            white running-light halos (enemies_r36.ghost_glow); what shows through the dust
  ghost-drone-stunned_0..15 40x40: the body with its lamps and engine dark (the EMP's lights-out)
  ghost-drone-glint_0..5    24x24, additive: the 0.3 s red glint tell before the burst (3 steps a frame),
                            centred on the eye: assets/pivots/ghost-drone.json `eye[k]` / `tips[k]`, [dx, dy]
                            px from the sprite's centre, y down
  ghost-drone-death_0..7    48x48, additive: the mechanical burst's sparks, white-amber (no ichor)
  ghost-drone-tatters_0..11 48x48, solid: a black smoke puff (4 translucency steps) and grey fragments
                            tumbling down into the dust
  dust-devil_0..7           72x72, closed: the spin, 5-fold, frame i turns the vanes i x 9 degrees clockwise
                            (8 frames cover 72 degrees)
  dust-devil-rise_0..23     72x72: the 0.4 s rise, the vanes parting, 3 steps x 8 spin frames (step * 8 + spin)
  dust-devil-open_0..7      72x72: open, the lime core exposed, spin frames; one 40-colour palette over the
                            closed, rise and open sets
  dust-devil-core_0..5      40x40, additive: the lime core's glow, a pulse loop (also its faint glow through
                            the dust outside the radius, drawn at low alpha)
  dust-devil-swirl_0..11    152x152, stepped translucency (<= 55 %): the ochre grit swirl, three arms, a
                            12-frame loop turning clockwise by 120 degrees
  dust-devil-swirl-tight_0..11 120x120: the swirl drawn in and brightened (the rise tell, the open window)
  dust-devil-death_0..9     96x96, additive: the lime flash, a ring and teal sparks
  dust-devil-collapse_0..7  72x72: the funnel collapsing, the vanes sagging flat and going dark
  dust-devil-tatters_0..13  160x160, stepped translucency: the grit cloud billowing out and settling
  rover-wreck_0..2          64x48: the six-wheeled militia rover half buried in a drift (intact, hit, destroyed
                            with its cargo pod blown open)
  rover-wreck-beacon_0..7   64x48, additive, aligned with the rover: the red distress beacon flashing (a 1 s loop
                            at 8 fps)
  rover-wreck-break_0..7    80x80: the cargo pod popping, a dust burst and amber panels flying
  design/enemies/air/concept/ghost-drone-final-r41-a.png/.gif, design/enemies/ground/concept/
  dust-devil-final-r41-a.png/.gif, design/campaign/act-3-red-dust/level-18-dust-storm/concept/
  l18-props-final-r41-a.png/.gif   review sheets and GIFs (the GIFs over the Level 18 storm plain with the
                            chosen radius look b, MA.2: ground gone into the veil outside 170 px, air units
                            as dark silhouettes keeping their glow, bullets always drawn)

Run: python3 tools/art/storm_units.py [ghost] [devil] [rover] [--review]   (~2 min; no subject = all)
"""
import contextlib
import json
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import burrower  # noqa: E402  (tools/art: the Mars ground stand-in, paste_into)
import siege_units  # noqa: E402  (tools/art: the levels' claimed ground-target roots)
import vrell_deaths as vd  # noqa: E402  (tools/art: the motes)
import vrell_fx as fx  # noqa: E402  (tools/art: easing, timing)
import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged: light canvases)
import enemies_r03 as e3  # noqa: E402  (concept script: place() with the air shadow, bolts)
import enemies_r36 as u36  # noqa: E402  (concept round 36: the chosen ghost_a, its lamps and glow)
import mars_r36 as m36  # noqa: E402  (concept round 36: the storm look b's veil colours)
from render import r06_models as m6  # noqa: E402  (the chosen round-06 Dust Devil)
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import (Material, rotate_z, sd_box, sd_capsule, sd_cylinder_y, sd_ellipsoid,  # noqa: E402
                        sd_sphere, subtract, union)

SCRIPT = "storm_units.py"
BATCH = "M7 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r41"
HELD = ROOT / "build" / "art-held" / "sprites"
AIR = DESIGN / "enemies" / "air" / "concept"
GROUND = DESIGN / "enemies" / "ground" / "concept"
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-18-dust-storm" / "concept"
STEP = 60                                         # simulation steps a second

LIME = np.array((168, 255, 42), float)
TEAL = np.array((0, 255, 154), float)
WHITE = (255, 255, 255)
AMBER = (255, 176, 70)


# =========================================================================== held sprites

def _rooted(name, root):
    return name == root or name.startswith(root + "-")


def claimed(name):
    """Whether the data claims the sprite (SpriteUse): the two enemies once their data.yaml exists, the rover's
    look once a level's ground target names it."""
    for slug in ("ghost-drone", "dust-devil"):
        if _rooted(name, slug):
            return any((DESIGN / "enemies").glob(f"*/{slug}/data.yaml"))
    return any(_rooted(name, r) for r in siege_units.level_sprite_roots())


@contextlib.contextmanager
def sprites_of(name):
    target = artkit.SPRITES if claimed(name) else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def _owned(stem, name):
    return stem == name or (stem.rsplit("_", 1)[0] == name and stem.rsplit("_", 1)[-1].isdigit())


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, HELD):          # no stale copy in the other folder
        for old in folder.glob(f"{name}*.png"):
            if _owned(old.stem, name):
                old.unlink()
    with sprites_of(name) as where:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} frame(s) {frames[0].width}x{frames[0].height} -> {where.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, HELD):
        single = folder / f"{name}.png"
        if single.exists():
            return [Image.open(single).convert("RGBA")]
        files = [f for f in folder.glob(f"{name}_*.png") if _owned(f.stem, name)]
        if files:
            files.sort(key=lambda p: int(p.stem.rsplit("_", 1)[1]))
            return [Image.open(f).convert("RGBA") for f in files]
    return []


def stepped(rgb, alpha, steps=4, floor=0.1):
    """Float RGB + 0..1 alpha -> RGBA with the alpha cut to ``steps`` translucency levels (below ``floor``
    clear), as the 90s translucency tables (art direction)."""
    a = np.clip(alpha, 0, 1)
    q = np.where(a < floor, 0, np.ceil(a * steps - 0.5) / steps)
    out = np.dstack([np.clip(rgb, 0, 255), q * 255]).astype(np.uint8)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


def blobs(w, h, S, items, seed):
    """Sum of noisy round blobs on an S-times canvas: items = [(cx, cy, r, a), ...] in native px; returns
    (alpha 0..1, light 0..1 toward the upper-left key light)."""
    yy, xx = np.mgrid[0:h * S, 0:w * S].astype(np.float64) / S
    noise = raster.fbm(w * S, h * S, 5 * S, seed, octaves=3, period=False)
    alpha = np.zeros_like(xx)
    light = np.zeros_like(xx)
    lv = np.array([-0.55, -0.6, 0.58])
    lv /= np.linalg.norm(lv)
    for cx, cy, r, a in items:
        nx, ny = (xx - cx) / r, (yy - cy) / r
        rr = nx ** 2 + ny ** 2
        edge = 1 - rr + (noise - 0.5) * 1.1
        cov = np.where(edge > 0, np.clip(edge * 1.8, 0, 1), 0) * a
        nz = np.sqrt(np.clip(1 - rr, 0, 1))
        lt = np.clip(nx * lv[0] + ny * lv[1] + nz * lv[2], 0, 1)
        light = np.where(cov > alpha, lt, light)
        alpha = alpha + cov * (1 - alpha)
    return alpha, light, noise


def shrink(rgb, alpha, S):
    """An S-times float canvas back to native size (box filter)."""
    h, w = alpha.shape[0] // S, alpha.shape[1] // S
    a = alpha.reshape(h, S, w, S).mean(axis=(1, 3))
    c = (rgb * alpha[..., None]).reshape(h, S, w, S, 3).mean(axis=(1, 3)) / np.maximum(a[..., None], 1e-6)
    return c, a


# =========================================================================== the Ghost Drone

GD = 40
GD_EXTENT = 2.3                                    # the concept's (ModelSpaceAngleSprites default)
GD_HEADINGS = 16
GD_COLOURS = 28
GD_GLOW = 48
GLINT = 24
GD_DEATH, GD_DEATH_N = 48, 8
GD_TATTERS, GD_TATTERS_N = 48, 12
GD_SCALE = GD / GD_EXTENT                          # px per model unit


def gd_heading(k):
    """The concept's screen heading (atan2, y down) of game heading k: 0 = down, clockwise."""
    return np.pi / 2 + TAU * k / GD_HEADINGS


def gd_model(k, glow=1.0):
    scene0, mats = u36.ghost_a(glow)
    rot = model_rotation(gd_heading(k))
    return (lambda p: scene0(rotate_z(p, rot))), model_space_materials(mats, rot)


def grey_rim(img):
    """enemies_r36.grey_rim without its per-frame palette (the set gets one palette)."""
    a = np.array(img)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    left, up = ~pad[1:-1, :-2], ~pad[:-2, 1:-1]
    right, down = ~pad[1:-1, 2:], ~pad[2:, 1:-1]
    out = a.astype(np.float64)
    for mask, col, k in (((left | up) & op, u36.LIT_RIM, 0.55),
                         ((right | down) & op & ~(left | up), u36.DARK_RIM, 0.5)):
        out[mask, :3] = out[mask, :3] * (1 - k) + np.array(col) * k
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def lamp_points(k):
    """(eye, tips) in px from the sprite's centre at heading k (y down)."""
    h = gd_heading(k)
    eye = rig.local_to_screen(*u36.GD_EYE, h, GD_SCALE)
    tips = [rig.local_to_screen(*t, h, GD_SCALE) for t in u36.GD_TIPS]
    return eye, tips


def set_lamps(img, k, lit=True):
    """enemies_r36.LampSprites' lamps: a 2x2 eye and the opaque pixel nearest each wing tip."""
    a = np.array(img)
    c = GD / 2
    (ex, ey), tips = lamp_points(k)
    x0, y0 = int(np.floor(c + ex - 0.5)), int(np.floor(c + ey - 0.5))
    a[y0:y0 + 2, x0:x0 + 2, :3] = (255, 64, 48) if lit else (60, 22, 20)
    a[y0:y0 + 2, x0:x0 + 2, 3] = 255
    for tx, ty in tips:
        ys, xs = np.nonzero(a[..., 3] > 0)
        i = int(np.argmin((xs + 0.5 - c - tx) ** 2 + (ys + 0.5 - c - ty) ** 2))
        a[ys[i], xs[i], :3] = (240, 244, 255) if lit else (70, 72, 78)
    return Image.fromarray(a, "RGBA")


def gd_body(job):
    k, glow = job
    return grey_rim(artkit.native(*artkit.render_hi(*gd_model(k, glow), (GD, GD), GD_EXTENT)))


def gd_glow(k):
    return artkit.additive(u36.ghost_glow(gd_heading(k), 1.0, GD_GLOW))


def gd_glint(i):
    """The red glint: a hot core with four short spikes, swelling and fading over 6 frames."""
    amp = (0.35, 0.7, 1.0, 1.0, 0.7, 0.35)[i]
    c = GLINT / 2
    cv = v8.Canvas(GLINT, GLINT)
    d = cv.dist(c, c)
    cv.add((255, 40, 30), v8.gauss(d, 4.0) * 0.6 * amp)
    cv.add((255, 150, 120), v8.gauss(d, 1.3) * 1.2 * amp)
    for ang in (0, np.pi / 2, np.pi, 3 * np.pi / 2):
        r = 3 + 7 * amp
        seg = cv.seg(c, c, c + np.cos(ang + 0.785) * r, c + np.sin(ang + 0.785) * r)
        cv.add((255, 90, 70), v8.gauss(seg, 0.5) * 1.0 * amp)
    return artkit.additive(cv.image())


def gd_death(i):
    """The mechanical burst's light: a white-amber flash, eight sparks flying out and fading, hot motes."""
    w = GD_DEATH
    t = fx.t_of(i, GD_DEATH_N)
    c = w / 2
    cv = v8.Canvas(w, w)
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 3.0, 0, 1)
    cv.add(WHITE, v8.gauss(d, 2.0 + 3 * t) * 1.4 * np.clip(1 - t * 2.6, 0, 1))
    cv.add(AMBER, v8.gauss(d, 5 + 6 * t) * 0.55 * (1 - t) ** 2 * fade)
    rng = np.random.default_rng(4101)
    for j in range(8):
        a = rng.uniform(0, TAU)
        reach = rng.uniform(12, 21)
        r1 = 3 + reach * fx.ease_out(t, 2.2)
        r0 = max(1.0, r1 - 5 * (1 - t))
        seg = cv.seg(c + np.cos(a) * r0, c + np.sin(a) * r0, c + np.cos(a) * r1, c + np.sin(a) * r1)
        cv.add((255, 220, 150), v8.gauss(seg, 0.55) * 1.3 * np.clip(1 - t * 1.4, 0, 1) * fade)
    vd.motes(cv, np.random.default_rng(4102), c, t, 10, (8, 20), (0.45, 0.8), AMBER, WHITE, fade,
             delay=0.05, gain=1.1)
    return artkit.additive(cv.image())


SMOKE = np.array([(14, 14, 16), (34, 33, 34), (58, 56, 56), (86, 84, 82)], float)


def gd_tatters(i):
    """Black smoke swelling and thinning, grey hull fragments tumbling out and falling (y down)."""
    w = GD_TATTERS
    S = 4
    t = (i + 0.5) / GD_TATTERS_N
    rng = np.random.default_rng(4111)
    items = []
    for k in range(5):
        ox, oy = rng.normal(0, 1.8, 2)
        items.append((w / 2 + ox * (1 + 2 * t), w / 2 + oy * (1 + 2 * t) + 5 * t, 3 + 9 * t ** 0.6 + rng.uniform(0, 2),
                      0.85 * (1 - t) ** 1.1))
    alpha, light, _ = blobs(w, w, S, items, 4112)
    rgb = SMOKE[np.clip(light * 3.99, 0, 3).astype(int)]
    rgb, alpha = shrink(rgb, alpha, S)
    img = stepped(rgb, alpha, 4, 0.12)
    d = ImageDraw.Draw(img)
    for k in range(5):                                           # grey fragments, falling as they fly
        ang = rng.uniform(0, TAU)
        dist = 3 + rng.uniform(9, 17) * fx.ease_out(t, 2.0)
        cx, cy = w / 2 + np.cos(ang) * dist, w / 2 + np.sin(ang) * dist + 14 * t ** 2
        r, rot = rng.uniform(1.6, 2.8) * (1 - 0.3 * t), ang + t * rng.uniform(6, 12)
        pts = [(cx + np.cos(rot + j * 2.3) * r, cy + np.sin(rot + j * 2.3) * r * 0.7) for j in range(3)]
        if t < 0.92:
            d.polygon(pts, fill=(92, 96, 104, 255))
            d.point(pts[0], fill=(178, 184, 192, 255))
    return img


def ghost_build():
    jobs = [(k, 1.0) for k in range(GD_HEADINGS)] + [(k, 0.0) for k in range(GD_HEADINGS)]
    with ProcessPoolExecutor() as pool:
        bodies = list(pool.map(gd_body, jobs))
        glows = list(pool.map(gd_glow, range(GD_HEADINGS)))
        glints = list(pool.map(gd_glint, range(6)))
        deaths = list(pool.map(gd_death, range(GD_DEATH_N)))
        tatters = list(pool.map(gd_tatters, range(GD_TATTERS_N)))
    pal = artkit.quantize_set(bodies, GD_COLOURS)
    lit = [set_lamps(f, k, True) for k, f in enumerate(pal[:GD_HEADINGS])]
    dark = [set_lamps(f, k, False) for k, f in enumerate(pal[GD_HEADINGS:])]
    write("ghost-drone", lit)
    write("ghost-drone-glow", artkit.quantize_set(glows, 24))
    write("ghost-drone-stunned", dark)
    write("ghost-drone-glint", artkit.quantize_set(glints, 16))
    write("ghost-drone-death", artkit.quantize_set(deaths, 24))
    write("ghost-drone-tatters", artkit.quantize_set(tatters, 16))
    pts = [lamp_points(k) for k in range(GD_HEADINGS)]
    artkit.write_pivots("ghost-drone", SOURCE, {
        "note": "per heading k (0 = down, clockwise): the sensor eye's and the two wing-tip lights' offsets in px "
                "from the sprite's centre, y down (the glint is centred on the eye)",
        "eye": [[round(e[0], 1), round(e[1], 1)] for e, _ in pts],
        "tips": [[[round(x, 1), round(y, 1)] for x, y in tips] for _, tips in pts]})




# =========================================================================== the Dust Devil

DD = 72
DD_EXTENT = 2.3                                    # the concept's (ModelSpaceAngleSprites default)
DD_SPIN = 8                                        # 8 frames cover 72 degrees (5-fold)
DD_RISE = (0.3, 0.55, 0.8)                         # the vanes parting over the 0.4 s rise
DD_COLOURS = 40
CORE, CORE_N = 40, 6
SWIRL, SWIRL_TIGHT, SWIRL_N = 152, 120, 12
DD_DEATH, DD_DEATH_N = 96, 10
COLLAPSE_N = 8
DD_TATTERS, DD_TATTERS_N = 160, 14
GRIT = np.array([(92, 50, 30), (140, 86, 52), (186, 128, 80), (222, 172, 118), (242, 208, 160)], float)


def dd_model(i, core=0.0, glow=1.0, sag=0.0):
    """The concept's dust_devil at spin frame i (i x 9 degrees clockwise; the concept's 40-step angle set with
    5-fold symmetry), ``sag`` 0..1 flattening and shrinking it for the collapse."""
    scene0, mats = m6.dust_devil(0.0, core, glow)
    rot = model_rotation(i * TAU / 40)
    kz, kr = 1 - 0.8 * sag, 1 - 0.18 * sag

    def scene(p, s=scene0, r=rot):
        q = rotate_z(p, r).copy()
        q[:, 2] /= kz
        q[:, :2] /= kr
        d, m = s(q)
        return d * min(kz, kr), m
    return scene, model_space_materials(mats, rot)


def dd_body(job):
    i, core, glow, sag = job
    return artkit.native(*artkit.render_hi(*dd_model(i, core, glow, sag), (DD, DD), DD_EXTENT))


def dd_core(i):
    """The lime core's glow: a hot centre and a soft halo, a slow pulse loop."""
    amp = 0.82 + 0.18 * np.cos(TAU * i / CORE_N)
    c = CORE / 2
    cv = v8.Canvas(CORE, CORE)
    d = cv.dist(c, c)
    cv.add(tuple(LIME), v8.gauss(d, 9.0) * 0.45 * amp)
    cv.add((220, 255, 160), v8.gauss(d, 3.2) * 0.9 * amp)
    return artkit.additive(cv.image())


def dd_swirl(i, tight=False):
    """The grit swirl: three spiral arms of grit specks over a faint spiral haze, one arm's specks repeated
    on the other two so a turn of 120 degrees over the 12 frames loops; lit toward the upper left, the
    translucency cut to 4 steps (at most 55 %)."""
    size = SWIRL_TIGHT if tight else SWIRL
    r_out = size / 2 - 4
    S = 3
    n = size * S
    yy, xx = (np.mgrid[0:n, 0:n].astype(np.float64) + 0.5) / S - size / 2
    rr = np.hypot(xx, yy) / r_out
    th = np.arctan2(yy, xx)
    phase = TAU / 3 * i / SWIRL_N                       # clockwise on screen (y down)
    wind = 4.2 if tight else 3.4
    arm = (0.5 + 0.5 * np.cos(3 * (th - phase) - wind * TAU / 3 * rr)) ** 1.2
    falloff = np.clip((1 - rr) * 2.4, 0, 1) * np.clip((rr - 0.12) * 6, 0, 1)
    haze = (0.26 + 0.42 * arm) * falloff * (0.95 if tight else 0.8)
    rng = np.random.default_rng(4201 if tight else 4202)
    specks = np.zeros_like(xx)
    count = 120 if tight else 110
    for _ in range(count):
        u = rng.uniform(0.22, 1.0) ** 0.8
        a0 = wind * TAU / 3 * u / 3 + rng.uniform(-0.22, 0.22)
        s = (0.7 + 1.3 * u) * (0.85 if tight else 1.0)
        amp = rng.uniform(0.5, 1.0) * (1.15 - u)
        for k in range(3):
            a = a0 + phase + k * TAU / 3
            px, py = np.cos(a) * r_out * u, np.sin(a) * r_out * u
            specks += amp * np.exp(-((xx - px) ** 2 + (yy - py) ** 2) / s ** 2)
    alpha = np.clip(haze + 0.75 * specks, 0, 1) * (0.55 if not tight else 0.6)
    lit = np.clip(0.5 - 0.055 * (xx + yy) / (size / 2) + 0.6 * specks, 0, 1)
    if tight:
        lit = np.clip(lit + 0.25, 0, 1)
    rgb = GRIT[np.clip(lit * 4.99, 0, 4).astype(int)]
    rgb, alpha = shrink(rgb, alpha, S)
    return stepped(rgb, alpha / 0.6 * 0.55, 4, 0.08)


def dd_death(i):
    """The medium Vrell death light: a white-lime flash, a lime ring running out, teal sparks."""
    w = DD_DEATH
    t = fx.t_of(i, DD_DEATH_N)
    c = w / 2
    cv = v8.Canvas(w, w)
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 4.0, 0, 1)
    cv.add((235, 255, 200), v8.gauss(d, 4 + 6 * t) * 1.3 * np.clip(1 - t * 2.4, 0, 1))
    cv.add(tuple(LIME), v8.gauss(d, 10 + 10 * t) * 0.5 * (1 - t) ** 2 * fade)
    ring = np.abs(d - (6 + 34 * fx.ease_out(t, 2.0)))
    cv.add(tuple(LIME), v8.gauss(ring, 1.2 + 2 * t) * 0.8 * (1 - t) ** 1.5 * fade)
    vd.motes(cv, np.random.default_rng(4211), c, t, 16, (20, 42), (0.5, 0.9), tuple(TEAL), (220, 255, 240), fade,
             delay=0.05, gain=1.2)
    return artkit.additive(cv.image())


def dd_tatters(i):
    """The grit cloud: ochre puffs billowing out round the fallen funnel, lit from the upper left, then
    thinning and settling (4 translucency steps), grit specks thrown out."""
    w = DD_TATTERS
    S = 2
    t = (i + 0.5) / DD_TATTERS_N
    rng = np.random.default_rng(4221)
    items = [(w / 2, w / 2, 14 + 26 * t ** 0.7, 0.8 * (1 - t) ** 1.3)]
    for k in range(9):
        a = rng.uniform(0, TAU)
        dist = (10 + rng.uniform(18, 40) * fx.ease_out(t, 2.0))
        items.append((w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist * 0.9, 8 + 16 * t ** 0.6 + rng.uniform(0, 5),
                      0.75 * (1 - t) ** 1.2))
    alpha, light, _ = blobs(w, w, S, items, 4222)
    rgb = GRIT[np.clip(0.4 + light * 3.6, 0, 4).astype(int)]
    rgb, alpha = shrink(rgb, alpha, S)
    img = stepped(rgb, alpha, 4, 0.12)
    d = ImageDraw.Draw(img)
    for k in range(24):
        a = rng.uniform(0, TAU)
        dist = rng.uniform(30, 74) * fx.ease_out(t, 1.6)
        if t < 0.8:
            x, y = w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist
            d.point((x, y), fill=tuple(int(v) for v in GRIT[3 if k % 3 else 4]) + (255,))
    return img


def devil_build():
    closed = [(i, 0.0, 1.0, 0.0) for i in range(DD_SPIN)]
    rise = [(i, c, 1.0, 0.0) for c in DD_RISE for i in range(DD_SPIN)]
    opened = [(i, 1.0, 1.0, 0.0) for i in range(DD_SPIN)]
    collapse = [(2 * j % DD_SPIN, 0.0, 1 - j / (COLLAPSE_N - 1), j / (COLLAPSE_N - 1)) for j in range(COLLAPSE_N)]
    with ProcessPoolExecutor() as pool:
        bodies = list(pool.map(dd_body, closed + rise + opened + collapse))
        cores = list(pool.map(dd_core, range(CORE_N)))
        swirl = list(pool.map(dd_swirl, range(SWIRL_N)))
        tight = list(pool.map(dd_swirl, range(SWIRL_N), [True] * SWIRL_N))
        deaths = list(pool.map(dd_death, range(DD_DEATH_N)))
        tatters = list(pool.map(dd_tatters, range(DD_TATTERS_N)))
    n1, n2, n3 = DD_SPIN, DD_SPIN + len(rise), 2 * DD_SPIN + len(rise)
    pal = artkit.quantize_set(bodies[:n3], DD_COLOURS)
    write("dust-devil", pal[:n1])
    write("dust-devil-rise", pal[n1:n2])
    write("dust-devil-open", pal[n2:n3])
    darken = []
    for j, f in enumerate(bodies[n3:]):                          # the dying funnel goes dark
        a = np.array(f).astype(np.float64)
        a[..., :3] *= 1 - 0.45 * j / (COLLAPSE_N - 1)
        darken.append(Image.fromarray(a.astype(np.uint8), "RGBA"))
    write("dust-devil-collapse", artkit.quantize_set(darken, 32))
    write("dust-devil-core", artkit.quantize_set(cores, 16))
    write("dust-devil-swirl", artkit.quantize_set(swirl, 16))
    write("dust-devil-swirl-tight", artkit.quantize_set(tight, 16))
    write("dust-devil-death", artkit.quantize_set(deaths, 32))
    write("dust-devil-tatters", artkit.quantize_set(tatters, 16))


# =========================================================================== the militia rover wreck

ROVER = (64, 48)
ROVER_TURN = np.radians(22)
ROVER_EXTENT = 64.0                                # model units are pixels
BEACON_N = 8
BREAK, BREAK_N = 80, 8
OFF_WHITE = (0.78, 0.75, 0.68)
AMBER_PAINT = (0.82, 0.48, 0.14)
DUST_RUST = (0.56, 0.31, 0.19)


def rover_mats(beacon=0.25):
    dusty = lambda p, n: 0.9 + 0.1 * np.sin(p[:, 0] * 0.45 + 1.3 * np.sin(p[:, 1] * 0.6))  # noqa: E731
    mats = [
        Material(OFF_WHITE, metal=0.2, shininess=40, spec=0.4, pattern=dusty),               # 0 body
        Material(AMBER_PAINT, metal=0.25, shininess=40, spec=0.45,
                 pattern=lambda p, n: np.where((np.abs(p[:, 0] + 40) % 3.2) < 0.8, 0.72, 1.0)),  # 1 cargo pod, ribbed
        Material((0.11, 0.11, 0.12), metal=0.1, shininess=10, spec=0.1),                       # 2 tyres
        Material((0.1, 0.14, 0.18), metal=0.8, shininess=120, spec=1.1),                       # 3 cab glass
        Material(DUST_RUST, metal=0.0, shininess=4, spec=0.05,
                 pattern=lambda p, n: 0.94 + 0.06 * np.sin(p[:, 0] * 0.8) * np.sin(p[:, 1] * 1.1)),  # 4 the drift
        Material((0.25, 0.04, 0.03), emission=(1.6 * beacon, 0.12 * beacon, 0.08 * beacon)),   # 5 beacon lens
        Material((0.4, 0.41, 0.43), metal=0.7, shininess=80, spec=0.8),                        # 6 steel
        Material((0.08, 0.07, 0.07), metal=0.0, shininess=4, spec=0.05),                       # 7 the pod's dark inside
    ]
    return model_space_materials(mats, ROVER_TURN)


BEACON_AT = (8.0, -4.0, 13.4)


def rover_scene(state=0, lens_only=False):
    """A six-wheeled militia rover (x forward), cab and roof beacon at the front, the amber cargo pod behind,
    half buried: a drift of rust dust over its left rear wheels with a wind tail behind it. State 2: the pod's
    lid blown off (lying beside it), the pod open and dark."""
    def scene(p):
        q = rotate_z(p, ROVER_TURN)
        lens = (sd_sphere(q, BEACON_AT, 1.6), 5)
        if lens_only:
            return lens[0], np.full(lens[0].shape, 5)
        parts = [
            (sd_box(q, (0, 0, 6.0), (19, 8.6, 3.2), 1.2), 0),
            (sd_box(q, (12.5, 0, 10.0), (6.0, 8.0, 2.4), 1.0), 0),
            (sd_box(q, (17.2, 0, 10.4), (1.6, 7.0, 1.6), 0.5), 3),
            (sd_box(q, (-7.0, 0, 11.4), (9.5, 7.8, 3.0), 0.7), 1),
            (sd_capsule(q, (10, 5.4, 12.6), (10, 5.4, 17), 0.45), 6),
            lens,
        ]
        for x in (-12.0, 0.0, 12.0):
            for y in (-10.4, 10.4):
                parts.append((sd_cylinder_y(q, (x, y, 4.2), 4.2, 2.0), 2))
        body = union(*parts)
        if state == 2:
            body = subtract(body, sd_box(q, (-7.0, 0, 14.0), (8.2, 6.6, 2.4)))
            inside = (sd_box(q, (-7.0, 0, 10.0), (8.2, 6.6, 0.4)), 7)
            lid = (sd_box(rotate_z(q - np.array([-6.0, -13.5, 0.8]), -0.3), (0, 0, 0), (7.0, 5.6, 0.6), 0.3), 1)
            body = union(body, inside, lid)
        drift = (sd_ellipsoid(q, (-5, 9.5, -1.5), (19, 8.5, 9.5)), 4)
        tail = (sd_ellipsoid(q, (-23, 4, -3.5), (13, 9, 6.5)), 4)
        return union(body, drift, tail, k=1.5)
    return scene


def rover_frame(state):
    img = artkit.native(*artkit.render_hi(rover_scene(state), rover_mats(), ROVER, ROVER_EXTENT, z_top=40.0, steps=140),
                        crisp=60)
    if state >= 1:                                                # scorch marks, a scratch of bare metal
        a = np.array(img).astype(np.float64)
        h, w = a.shape[:2]
        yy, xx = np.mgrid[0:h, 0:w]
        n = raster.fbm(w, h, 4, 4301 + state, octaves=2, period=False)
        spots = [(36, 20, 6), (27, 27, 4)] + ([(22, 22, 7), (40, 28, 5)] if state == 2 else [])
        for cx, cy, r in spots:
            m = np.clip(1 - np.hypot(xx - cx, yy - cy) / r + (n - 0.5) * 0.8, 0, 1)
            a[..., :3] *= 1 - 0.7 * m[..., None]
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
    return img


def beacon_centre():
    hi, f = artkit.render_hi(rover_scene(0, lens_only=True), rover_mats(), ROVER, ROVER_EXTENT, z_top=40.0)
    a = hi[..., 3]
    ys, xs = np.nonzero(a > 0.5)
    return (xs.mean() + 0.5) / f, (ys.mean() + 0.5) / f


def rover_beacon(i, centre):
    """The distress beacon's flash, 8 frames a second: a hard red flash on frames 0-1, then dark."""
    amp = (1.0, 0.75, 0.25, 0.08, 0.0, 0.0, 0.0, 0.0)[i]
    w, h = ROVER
    cv = v8.Canvas(w, h)
    d = cv.dist(*centre)
    cv.add((255, 40, 30), v8.gauss(d, 6.0) * 0.5 * amp)
    cv.add((255, 140, 110), v8.gauss(d, 1.5) * 1.3 * amp)
    for ang in (0.0, np.pi / 2, np.pi, 1.5 * np.pi):
        seg = cv.seg(*centre, centre[0] + np.cos(ang) * 9 * amp, centre[1] + np.sin(ang) * 9 * amp)
        cv.add((255, 70, 50), v8.gauss(seg, 0.5) * 0.9 * amp)
    return artkit.additive(cv.image())


def rover_break(i):
    """The cargo pod popping: a dust burst (4 translucency steps) and amber panels flying, falling back."""
    w = BREAK
    S = 2
    t = (i + 0.5) / BREAK_N
    rng = np.random.default_rng(4311)
    items = [(w / 2, w / 2, 6 + 22 * t ** 0.6, 0.85 * (1 - t) ** 1.2)]
    for k in range(6):
        a = rng.uniform(0, TAU)
        dist = 4 + rng.uniform(10, 24) * fx.ease_out(t, 2.0)
        items.append((w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist, 5 + 10 * t ** 0.6, 0.7 * (1 - t)))
    alpha, light, _ = blobs(w, w, S, items, 4312)
    rgb = GRIT[np.clip(0.4 + light * 3.6, 0, 4).astype(int)]
    rgb, alpha = shrink(rgb, alpha, S)
    img = stepped(rgb, alpha, 4, 0.12)
    d = ImageDraw.Draw(img)
    for k in range(6):
        a = rng.uniform(0, TAU)
        dist = 4 + rng.uniform(16, 30) * fx.ease_out(t, 1.8)
        cx, cy = w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist
        r, rot = rng.uniform(2.0, 3.6), a + t * rng.uniform(5, 10)
        pts = [(cx + np.cos(rot + j * TAU / 4) * r, cy + np.sin(rot + j * TAU / 4) * r * 0.55) for j in range(4)]
        if t < 0.9:
            d.polygon(pts, fill=(206, 120, 40, 255))
            d.line(pts[:2], fill=(244, 190, 110, 255))
    return img


def rover_build():
    centre = beacon_centre()
    with ProcessPoolExecutor() as pool:
        bodies = list(pool.map(rover_frame, range(3)))
        beacon = [rover_beacon(i, centre) for i in range(BEACON_N)]
        brk = list(pool.map(rover_break, range(BREAK_N)))
    write("rover-wreck", artkit.quantize_set(bodies, 40))
    write("rover-wreck-beacon", artkit.quantize_set(beacon, 16))
    write("rover-wreck-break", artkit.quantize_set(brk, 24))




# =========================================================================== intel models (tools/art/intel.py)

def ghost_intel_model():
    """(scene, mats) for the hangar intel portrait: nose down (heading 0), lit."""
    return gd_model(0)


def devil_intel_model():
    """(scene, mats) for the hangar intel portrait: in its rise, the vanes parting on the lime core; the
    vanes' and the body's slots lightened (enemies_r36.lift) so the teal-black funnel reads on the dark plate."""
    scene, mats = dd_model(2, 0.8)
    return scene, u36.lift(list(mats), 0.4)

# =========================================================================== review

FW, FH = 480, 540


def storm_ground(name, t, speed=120.0):
    """The play field's ground at t: a tile set of tools/art/backdrop_l18.py scrolling down (float RGB), or
    the Level 15 ground stand-in before the backdrop exists."""
    import backdrop_l18 as b18
    path = b18.OUT / f"{name}.png"
    if path.exists():
        img = np.array(Image.open(path).convert("RGB")).astype(np.float64)
        rows = (np.arange(FH) - int(speed * t)) % img.shape[0]
        return img[rows]
    return np.array(burrower.mars_plate(FW, FH, 151, "dunes").convert("RGB")).astype(np.float64)


def storm_look():
    """backdrop_l18.StormLook (the chosen radius look b from the level's textures), or None before them."""
    import backdrop_l18 as b18
    return b18.StormLook() if (b18.OUT / "storm-veil.png").exists() else None


def glow_on(img, glow, x, y, k=1.0):
    if k < 1.0:
        a = np.array(glow).astype(np.float64)
        a[..., :3] *= k
        glow = Image.fromarray(a.astype(np.uint8), "RGBA")
    return artkit.add_light(img, glow, (int(round(x - glow.width / 2)), int(round(y - glow.height / 2))))


def paste(img, sp, x, y):
    img.alpha_composite(sp, (int(round(x - sp.width / 2)), int(round(y - sp.height / 2))))


def faded(sp, k):
    a = np.array(sp).astype(np.float64)
    a[..., 3] *= k
    a[..., 3] = np.where(a[..., 3] >= 128, 255, 0)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def heading_of(vx, vy, n=16):
    """Game heading index of a velocity (0 = down, clockwise as seen on the screen)."""
    ang = math.atan2(vy, vx) - math.pi / 2
    return int(round((ang % TAU) / (TAU / n))) % n


class Shots:
    """Aimed enemy bullets (the orb, always drawn) and the ship's bolts."""

    def __init__(self):
        self.list = []

    def fire(self, t, x, y, tx, ty, v=210.0):
        d = math.hypot(tx - x, ty - y) or 1.0
        self.list.append((t, x, y, (tx - x) / d * v, (ty - y) / d * v))

    def draw(self, img, t, orb):
        for t0, x, y, vx, vy in self.list:
            if t0 <= t < t0 + 3:
                xx, yy = x + vx * (t - t0), y + vy * (t - t0)
                if -10 < xx < FW + 10 and -10 < yy < FH + 10:
                    paste(img, orb, xx, yy)


def ship_at(t):
    return 240 + 50 * math.sin(t * 0.8), 450


def ship_and_bolts(img, t):
    ship = artkit.load_frames("ship")[2]
    px, py = ship_at(t)
    for k in range(4):
        yb = py - 40 - ((t * 420 + k * 105) % 420)
        if yb > -10:
            e3.bolt(img, px - 10, yb)
            e3.bolt(img, px + 10, yb)
    paste(img, ship, px, py)


# --------------------------------------------------------------------------- the Ghost Drone's

def drone_paths():
    """(path, t0, t1, kill) per drone: a line abreast of three dropping into their lanes and strafing out,
    the middle one shot down; then a pincer 2 + 2 from the sides at their lanes."""
    out = []
    for i, (x, side) in enumerate(((170, -1), (240, -1), (310, 1))):
        lane = 130 + 40 * i
        t0, t_lane = 0.3, 0.3 + (lane + 30) / 180
        pts = [(x, -30), (x, lane - 40), (x + side * 30, lane), (x + side * 180, lane), (x + side * 420, lane)]
        times = [t0, t_lane - 0.25, t_lane + 0.15, t_lane + 1.2, t_lane + 2.8]
        out.append((rig.catmull_rom(pts, times), t0, times[-1], 3.4 if i == 1 else None))
    for i in range(4):
        side = -1 if i < 2 else 1
        lane = 150 + 46 * (i % 2)
        x0 = -30 if side < 0 else FW + 30
        pts = [(x0, lane), (x0 - side * 200, lane), (x0 - side * 540, lane)]
        out.append((rig.catmull_rom(pts, [4.6 + 0.2 * (i % 2), 4.6 + 0.2 * (i % 2) + 1.4, 4.6 + 0.2 * (i % 2) + 3.6]),
                    4.6 + 0.2 * (i % 2), 4.6 + 0.2 * (i % 2) + 3.6, 7.0 if i == 3 else None))
    return out


def drone_events(paths, glint_steps=18):
    """Per drone the burst: the glint starts when it first passes within 60 px of the ship's x, three shots
    0.15 s apart after 0.3 s, each aimed at the ship."""
    shots = Shots()
    glints = {}
    for i, (path, t0, t1, kill) in enumerate(paths):
        for t in np.arange(t0 + 0.4, t1, 1 / STEP):
            x, y = path(t)
            if kill and t >= kill:
                break
            if 0 < x < FW and abs(x - ship_at(t)[0]) < 60:
                glints[i] = t
                for k in range(3):
                    tf = t + glint_steps / STEP + 0.15 * k
                    sx, sy = path(tf)
                    shots.fire(tf, sx, sy, *ship_at(tf))
                break
    return shots, glints


def ghost_frames(dur=8.6, fps=15):
    body, glow = load("ghost-drone"), load("ghost-drone-glow")
    glint, death, tatters = load("ghost-drone-glint"), load("ghost-drone-death"), load("ghost-drone-tatters")
    small = artkit.load_frames("explosion-small")
    orb = artkit.load_frames("orb")[0]
    eye = json.loads((artkit.PIVOTS / "ghost-drone.json").read_text(encoding="utf-8"))["eye"]
    look = storm_look()
    paths = drone_paths()
    shots, glints = drone_events(paths)
    out = []
    for f in range(int(dur * fps)):
        t = f / fps
        r = 170 if t >= 2.0 else 170 + 400 * (1 - t / 2.0)
        ground = storm_ground("storm-plain", 0.0)            # still: a scrolling ground would not fit 8 MB
        px, py = ship_at(t)
        if look:
            ground, v = look.ground(ground, t, px, py, r)
        else:
            v = np.ones((FH, FW))
        img = Image.fromarray(np.clip(ground, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        lights = []
        for i, (path, t0, t1, kill) in enumerate(paths):
            if not t0 <= t < t1:
                continue
            if kill is not None and t >= kill:
                x, y = path(kill)
                age = int((t - kill) * STEP)
                fr = fx.timed(tatters, 3, age)
                if fr is not None:
                    paste(img, fr, x, y + 0.2 * age)
                for frames, steps, delay in ((small, 2, 0), (death, 3, 2)):
                    g = fx.timed(frames, steps, age - delay)
                    if g is not None:
                        lights.append((g, x, y, 1.0))
                continue
            x, y = path(t)
            if not (-30 < x < FW + 30 and -30 < y < FH + 30):
                continue
            (xa, ya), (xb, yb) = path(t - 0.02), path(t + 0.02)
            k = heading_of(xb - xa, yb - ya)
            vu = float(v[int(np.clip(y, 0, FH - 1)), int(np.clip(x, 0, FW - 1))])
            sp = body[k]
            if kill is not None and kill - 0.5 < t and int(t * 14) % 2:
                sp = u36.flash(sp, True)
            sh = sprite.shadow_of(sp, opacity=0.5 * vu, blur=1.0, scale=0.85)
            paste(img, sh, x + 8, y + 12)
            if look:
                img = look.unit(img, sp, glow[k], x, y, vu)
            else:
                paste(img, sp, x, y)
                img = glow_on(img, glow[k], x, y)
            g0 = glints.get(i)
            if g0 is not None:
                age = int((t - g0) * STEP)
                g = fx.timed(glint, 3, age)
                if g is not None:
                    lights.append((g, x + eye[k][0], y + eye[k][1], 1.0))
        for g, x, y, k in lights:                       # the bursts and the tell show through the dust
            img = glow_on(img, g, x, y, k)
        shots.draw(img, t, orb)
        ship_and_bolts(img, t)
        raster.draw_text(img, 8, FH - 14, f"GHOST DRONE FINAL R41 - STORM LOOK B, R {int(r)} - {t:4.2f} S", raster.LABEL)
        out.append(img)
    return out


def ghost_review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    body, glow, dark = load("ghost-drone"), load("ghost-drone-glow"), load("ghost-drone-stunned")
    glint, death, tatters = load("ghost-drone-glint"), load("ghost-drone-death"), load("ghost-drone-tatters")
    lit = []
    for k in range(GD_HEADINGS):
        c = Image.new("RGBA", (GD_GLOW, GD_GLOW), artkit.PLATE)
        paste(c, body[k], GD_GLOW / 2, GD_GLOW / 2)
        lit.append(glow_on(c, glow[k], GD_GLOW / 2, GD_GLOW / 2))
    look = storm_look()
    veil = []
    for k in range(0, GD_HEADINGS, 2):
        for vu in (1.0, 0.0):
            c = Image.fromarray(np.clip(look.veil_at(0.0)[200:248, 200 + 6 * k:248 + 6 * k] if look else
                                        np.full((48, 48, 3), (176, 118, 74)), 0, 255).astype(np.uint8), "RGB").convert("RGBA")
            if look:
                c = look.unit(c, body[k], glow[k], 24, 24, vu)
            veil.append(c)
    rows = [
        ("16 HEADINGS WITH THE ADDITIVE LIGHTS, 0 = DOWN, CLOCKWISE", lit, 3, False),
        ("THE BODY ALONE (2X2 RED EYE, WHITE TIP PIXELS SET AFTER THE PALETTE)", body, 2, False),
        ("THE GLOW FRAMES (ADDITIVE, 48X48 PER HEADING)", glow, 2, True),
        ("EMP STUN: LAMPS AND ENGINE DARK", dark, 2, False),
        ("THE 0.3 S GLINT TELL (ADDITIVE, 3 STEPS A FRAME), AT THE EYE (PIVOTS/GHOST-DRONE.JSON)", glint, 4, True),
        ("IN THE DUST (LOOK B): INSIDE / OUTSIDE THE RADIUS, HEADINGS 0-14 STEP 2", veil, 3, False),
        ("DEATH: SPARKS (ADDITIVE, 3 STEPS A FRAME)", death, 3, True),
        ("DEATH: BLACK SMOKE AND GREY FRAGMENTS FALLING (SOLID)", tatters, 3, False),
    ]
    sheet = artkit.review_sheet("GHOST DRONE - FINAL SPRITES (FACET DART, U.5 LAMPS)", rows, width=1500, batch=BATCH)
    artkit.save_review(sheet, ghost_frames(), AIR, "ghost-drone", fps=15)


# --------------------------------------------------------------------------- the Dust Devil's

CYCLE, CLOSED, RISE_S, OPEN_S = 2.6, 1.4, 0.4, 0.8


def devil_state(t, offset):
    """(phase, u): 'closed', 'rise' (u 0..1), 'open' (u 0..1) of the 2.6 s cycle."""
    c = (t + offset) % CYCLE
    if c < CLOSED:
        return "closed", c / CLOSED
    if c < CLOSED + RISE_S:
        return "rise", (c - CLOSED) / RISE_S
    return "open", (c - CLOSED - RISE_S) / OPEN_S


def devil_frames(dur=7.8, fps=15):
    s = {n: load(n) for n in ("dust-devil", "dust-devil-rise", "dust-devil-open", "dust-devil-core",
                              "dust-devil-swirl", "dust-devil-swirl-tight", "dust-devil-death",
                              "dust-devil-collapse", "dust-devil-tatters")}
    medium = artkit.load_frames("explosion-medium")
    orb = artkit.load_frames("orb")[0]
    pick = artkit.load_frames("pickup-salvage-small")
    devils = [  # (centre path in screen px at the ground's scroll, cycle offset, kill t)
        (rig.catmull_rom([(140, -80), (120, 120), (170, 260), (130, 420), (160, 640)], [0, 2, 4, 6, 8.5]), 0.0, 6.3),
        (rig.catmull_rom([(340, -40), (370, 140), (320, 300), (360, 470), (330, 680)], [0, 2.2, 4.4, 6.6, 9]), 0.8, None),
    ]
    rng = np.random.default_rng(4401)
    loose = [dict(x=rng.uniform(80, 400), y=rng.uniform(60, 380), held=None, ang=rng.uniform(0, TAU)) for _ in range(6)]
    shots = Shots()
    for d, (path, off, kill) in enumerate(devils):
        for t in np.arange(0, dur, 1 / STEP):
            if kill is not None and t >= kill:
                break
            phase, u = devil_state(t, off)
            if phase == "open":
                age = int(round(u * OPEN_S * STEP))
                if age % 10 == 0:                                # a bullet per arm every 0.16 s
                    x, y = path(t)
                    for arm in range(2):
                        a = math.pi * t + arm * math.pi             # two arms rotating 180 degrees a second
                        shots.fire(t, x, y, x + math.cos(a) * 100, y + math.sin(a) * 100, 110.0)
    out = []
    for f in range(int(dur * fps)):
        t = f / fps
        ground = storm_ground("dry-lake", t, 0.0)
        img = Image.fromarray(np.clip(ground, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        lights = []
        centres = []
        for d, (path, off, kill) in enumerate(devils):
            x, y = path(t)
            if kill is not None and t >= kill:
                age = t - kill
                x, y = path(kill)
                fr = s["dust-devil-collapse"][min(int(age * 10), 7)] if age < 0.8 else None
                if fr is not None:
                    paste(img, fr, x, y)
                tt = int(age * 10)
                if tt < len(s["dust-devil-tatters"]):
                    paste(img, s["dust-devil-tatters"][tt], x, y)
                for frames, rate in ((medium, 16), (s["dust-devil-death"], 14)):
                    i = int(age * rate)
                    if i < len(frames):
                        lights.append((frames[i], x, y))
                continue
            centres.append((d, x, y))
            phase, u = devil_state(t, off)
            spin_fps = 20 if phase == "closed" else 30
            spin = int(t * spin_fps + d * 3) % DD_SPIN
            if phase == "closed":
                sw, body = s["dust-devil-swirl"][int(t * 12) % SWIRL_N], s["dust-devil"][spin]
            elif phase == "rise":
                step = min(int(u * 3), 2)
                sw, body = s["dust-devil-swirl-tight"][int(t * 16) % SWIRL_N], s["dust-devil-rise"][step * DD_SPIN + spin]
            else:
                sw, body = s["dust-devil-swirl-tight"][int(t * 16) % SWIRL_N], s["dust-devil-open"][spin]
            paste(img, sw, x, y)
            if phase == "open":                                  # lifted to low-air: its shadow on the ground
                paste(img, sprite.shadow_of(body, opacity=0.45, blur=1.2), x + 5, y + 7)
            hit = kill is not None and kill - 1.0 < t and phase == "open" and int(t * 14) % 2
            paste(img, u36.flash(body, True) if hit else body, x, y)
            if phase == "open":
                lights.append((s["dust-devil-core"][int(t * 10) % CORE_N], x, y))
        dt = 1 / fps
        for p in loose:                                          # pickups drawn in, circling, released at death
            if p["held"] is not None:
                hd = [c for c in centres if c[0] == p["held"]]
                if not hd:
                    p["held"] = None
                    p["y"] += 0
                else:
                    _, cx, cy = hd[0]
                    p["ang"] += 3.0 * dt
                    p["x"], p["y"] = cx + math.cos(p["ang"]) * 30, cy + math.sin(p["ang"]) * 26
            else:
                for d, cx, cy in centres:
                    dx, dy = cx - p["x"], cy - p["y"]
                    dist = math.hypot(dx, dy)
                    if dist < 34:
                        p["held"], p["ang"] = d, math.atan2(-dy, -dx)
                    elif dist < 170:
                        p["x"] += dx / dist * 90 * dt
                        p["y"] += dy / dist * 90 * dt
            paste(img, pick[int(t * 10) % len(pick)], p["x"], p["y"])
        for g, x, y in lights:
            img = glow_on(img, g, x, y)
        shots.draw(img, t, orb)
        ship_and_bolts(img, t)
        raster.draw_text(img, 8, FH - 14, f"DUST DEVIL FINAL R41 - THE EYE'S DRY LAKE (CLEAR) - {t:4.2f} S", raster.LABEL)
        out.append(img)
    return out


def devil_review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    s = {n: load(n) for n in ("dust-devil", "dust-devil-rise", "dust-devil-open", "dust-devil-core",
                              "dust-devil-swirl", "dust-devil-swirl-tight", "dust-devil-death",
                              "dust-devil-collapse", "dust-devil-tatters")}
    together = []
    for phase, frames, sw in (("closed", s["dust-devil"], s["dust-devil-swirl"]),
                              ("rise", s["dust-devil-rise"][16:], s["dust-devil-swirl-tight"]),
                              ("open", s["dust-devil-open"], s["dust-devil-swirl-tight"])):
        for j in (0, 4):
            c = Image.fromarray(storm_ground("dry-lake", 0.0, 0.0)[100:252, 100:252].astype(np.uint8), "RGB").convert("RGBA")
            paste(c, sw[j], 76, 76)
            paste(c, frames[j], 76, 76)
            if phase == "open":
                c = glow_on(c, s["dust-devil-core"][j % CORE_N], 76, 76)
            together.append(c)
    look = storm_look()
    dust = []
    if look:
        for phase in ("closed", "open"):
            for vu in (1.0, 0.0):
                g = storm_ground("storm-plain", 0.0)[100:252, 100:252]
                veil = look.veil_at(0.0)[100:252, 100:252]
                c = Image.fromarray(np.clip(g * vu + veil * (1 - vu), 0, 255).astype(np.uint8), "RGB").convert("RGBA")
                if vu > 0:
                    paste(c, (s["dust-devil-swirl"] if phase == "closed" else s["dust-devil-swirl-tight"])[0], 76, 76)
                    paste(c, (s["dust-devil"] if phase == "closed" else s["dust-devil-open"])[0], 76, 76)
                if phase == "open":
                    c = glow_on(c, s["dust-devil-core"][0], 76, 76, 1.0 if vu > 0 else 0.35)
                dust.append(c)
    rows = [
        ("CLOSED: SPIN, 8 FRAMES = 72 DEGREES (5-FOLD), FRAME I = I X 9 DEGREES CLOCKWISE", s["dust-devil"], 2, False),
        ("RISE (0.4 S): 3 STEPS X 8 SPIN FRAMES, STEP 0", s["dust-devil-rise"][:8], 2, False),
        ("RISE STEPS 1 AND 2", s["dust-devil-rise"][8:24:2], 2, False),
        ("OPEN: THE LIME CORE EXPOSED (LOW-AIR)", s["dust-devil-open"], 2, False),
        ("CORE GLOW (ADDITIVE, PULSE LOOP)", s["dust-devil-core"], 3, True),
        ("TOGETHER ON THE EYE'S DRY LAKE: CLOSED / RISE / OPEN WITH THEIR SWIRLS", together, 1, False),
        ("GRIT SWIRL, 12-FRAME LOOP (EVERY 2ND)", s["dust-devil-swirl"][::2], 1, False),
        ("THE TIGHT SWIRL (RISE TELL, OPEN)", s["dust-devil-swirl-tight"][::2], 1, False),
        ("IN THE STORM (LOOK B): CLOSED IN / OUT OF THE RADIUS, OPEN IN / OUT (THE CORE'S FAINT GLOW)", dust, 1, False),
        ("COLLAPSE (SOLID)", s["dust-devil-collapse"], 2, False),
        ("DEATH LIGHT (ADDITIVE)", s["dust-devil-death"], 1, True),
        ("GRIT CLOUD (EVERY 2ND)", s["dust-devil-tatters"][::2], 1, False),
    ]
    sheet = artkit.review_sheet("DUST DEVIL - FINAL SPRITES (VORTEX ORGANISM)", rows, width=1500, batch=BATCH)
    artkit.save_review(sheet, devil_frames(), GROUND, "dust-devil", fps=15)


# --------------------------------------------------------------------------- the rover's

def rover_frames(dur=4.6, fps=20):
    rover, beacon, brk = load("rover-wreck"), load("rover-wreck-beacon"), load("rover-wreck-break")
    crate = artkit.load_frames("pickup-crate")
    small = artkit.load_frames("explosion-small")
    look = storm_look()
    out = []
    hit, dead = 2.6, 3.2
    for f in range(int(dur * fps)):
        t = f / fps
        ground = storm_ground("storm-plain", t)
        px, py = 110 + 30 * math.sin(t), 440
        x, y = 60, 40 + 120 * t
        g = ground.copy()
        img = Image.fromarray(np.clip(g, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        state = 0 if t < hit else (1 if t < dead else 2)
        paste(img, rover[state], x, y)
        if state < 2:
            img = glow_on(img, beacon[int(t * 8) % BEACON_N], x, y)
        if t >= dead:
            age = t - dead
            i = int(age * 12)
            if i < BREAK_N:
                paste(img, brk[i], x, y)
            if age > 0.25:
                paste(img, crate[int(t * 10) % len(crate)], x + 20, y - 8)
        ground2 = np.array(img.convert("RGB")).astype(np.float64)
        if look:
            ground2, _ = look.ground(ground2, t, px, py, 170)
        img = Image.fromarray(np.clip(ground2, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        if hit - 0.1 <= t < dead + 0.4:
            i = int((t - hit + 0.1) * 20)
            if i < len(small):
                img = glow_on(img, small[i], x, y)
        ship = artkit.load_frames("ship")[2]
        for k in range(4):
            yb = py - 40 - ((t * 420 + k * 105) % 420)
            if yb > y - 10:
                e3.bolt(img, px - 10, yb)
        paste(img, ship, px, py)
        raster.draw_text(img, 8, FH - 14, f"L18 SECRET FINAL R41 - THE ROVER AT THE RADIUS'S EDGE - {t:4.2f} S", raster.LABEL)
        out.append(img)
    return out


def rover_review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    rover, beacon, brk = load("rover-wreck"), load("rover-wreck-beacon"), load("rover-wreck-break")
    flashing = [glow_on(Image.alpha_composite(artkit.checker(64, 48), rover[0]), beacon[i], 32, 24)
                for i in range(BEACON_N)]
    look = storm_look()
    dust = []
    if look:
        for vu in (1.0, 0.5, 0.0):
            g = storm_ground("storm-plain", 0.0)[200:248, 200:264]
            img = Image.fromarray(g.astype(np.uint8), "RGB").convert("RGBA")
            paste(img, rover[0], 32, 24)
            img = glow_on(img, beacon[0], 32, 24)
            a = np.array(img.convert("RGB")).astype(np.float64)
            veil = look.veil_at(0.0)[200:248, 200:264]
            dust.append(Image.fromarray(np.clip(a * vu + veil * (1 - vu), 0, 255).astype(np.uint8), "RGB").convert("RGBA"))
    rows = [
        ("THE MILITIA ROVER WRECK: INTACT, HIT, DESTROYED (POD BLOWN OPEN)", rover, 4, False),
        ("THE DISTRESS BEACON (ADDITIVE, 8 FPS LOOP) ON THE INTACT ROVER", flashing, 3, False),
        ("IN THE STORM: INSIDE, AT THE EDGE, OUTSIDE THE RADIUS (THE BEACON GOES WITH THE GROUND)", dust, 4, False),
        ("THE POD POPPING (SOLID, 12 FPS)", brk, 2, False),
    ]
    sheet = artkit.review_sheet("LEVEL 18 SECRET - MILITIA ROVER WRECK (FINAL)", rows, width=1300, batch=BATCH)
    artkit.save_review(sheet, rover_frames(), LEVEL, "l18-props", fps=20)


if __name__ == "__main__":
    artkit.REVIEW_ROUND = REVIEW_ROUND
    args = [a for a in sys.argv[1:] if not a.startswith("--")] or ["ghost", "devil", "rover"]
    if "--review" not in sys.argv[1:]:
        for name, fn in (("ghost", ghost_build), ("devil", devil_build), ("rover", rover_build)):
            if name in args:
                fn()
    for name, fn in (("ghost", ghost_review), ("devil", devil_review), ("rover", rover_review)):
        if name in args:
            fn()
