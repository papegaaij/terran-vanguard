#!/usr/bin/env python3
"""Production art: Level 22's props (design/campaign/act-4-deep-water/level-22-ice-shelf, "Hazards", "Secrets
and pickups" and "Layout" section 3; M8 part B batch, concept round 51, step ART-B; straight to production, no
concept of their own: designed to the README, the round-48 mood still `europa-ice-shelf-r48-a`
(tools/concept/europa_r48.py: its ice ramp ICE, the geyser's glowing crack and steam), Europa's hard sunlight
from the upper left, Jupiter's warm light from the right and readability rule 7 (the light rim) for the secret).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse):
  ice-geyser_0..3           48x48, the ice geyser's crack in the shelf (the vents' `look: ice-geyser`): 0 dormant (a
                            dark fissure with a pale refrozen rim and frost), 1-3 the 1.2 s tell (the crack darkening,
                            then glowing lime-white: dull, bright, white-hot); the burst draws frame 3 under its steam.
                            The README's furnace-vent model: frames as `furnace-vent_0..3`
  ice-geyser-glow_0..2      64x64, additive, centred on the crack: the tell's lime-white light over it for tell steps
                            1-3 (0.4 s each) with the vapour shimmer's broken rings (as `furnace-vent-glow`)
  ice-geyser-steam_0..7     64x64, NORMAL blending (stepped translucency, not additive): the 0.8 s steam column seen
                            from above (10 fps): 0-1 the burst spreading out of the crack, 2-5 the billowing column
                            40 px across (the README's hit width; white, lit from the upper left, its own shadow to
                            the lower right), 6-7 collapsing (in `furnace-vent-flame`'s place; the game draws it with
                            normal blending over the ground units)
  ice-geyser-residue_0..5   64x64, stepped translucency: the mist drifting off the crack after a burst and settling
                            as frost (8 fps; in `furnace-vent-smoke`'s place)
  crevasse-lip_0..2         64x48, the secret's crevasse lip (a 20 HP `ground` trigger, the core-sample cache under
                            it): 0 the intact overhang of snow-ice over the dark crevasse, the cache's orange case and
                            its marker light glimpsed in the shadow; 1 hit (cracked, a chunk gone); 2 fallen (the lip
                            broken into the crevasse, rubble, the cache gone: it pops out as the hidden crate); rule
                            7's light rim on every frame; the crevasse itself is the backdrop's (`crevasse` piece)
  crevasse-lip-glow_0..7    64x48, additive, aligned with the lip: the cache's marker light (0-3 a slow blink until
                            the first hit, 4-7 a hurried flicker after it), as the other trigger looks' glows
  ice-crawler_0..5          48x48, the Conamara ice-crawler (the drop's `sprite: ice-crawler`: a tracked survey
                            vehicle, the supply drone's role): index heading x 3 + track frame, heading 0 driving up
                            the screen, 1 driving down; 3 track frames (the treads' links moving, 12 fps at its 40
                            px/s); red polar cab, white body, the empty cargo bed behind the cab (the armour patch
                            is drawn in it at the bed's pivot, assets/pivots/ice-crawler.json)
  ice-crawler-lights_0..3   64x64, additive, same centre: index heading x 2 + (0 the amber roof beacon's flash with
                            the headlights, 1 the headlights alone); the headlights' beams point the way it drives
  station_layer()           (no file: the station's structures for tools/art/backdrop_l22.py's `conamara-station`
                            piece): Conamara Station drawn flat from above: the main research dome (insulated white
                            panels, a sodium-lit ring of windows) on the ice-breaker road at x 240, the north dome
                            with its shaft hatch, the linking tubes, the lab modules, the antenna mast, the landing pad
                            (its ring, H and edge lights: ``pad_marks``); ``lamps_on`` bakes the sodium lamps' pools
                            into the ice (STATION_LAMPS), ``road_strip`` is the road the run-in's tile set carries
  design/campaign/act-4-deep-water/level-22-ice-shelf/concept/l22-props-final-r51-a.png/.gif   the review sheet and
                            GIF (on the shelf: a geyser row's ripple, the lip shot open, the crawler driving up and
                            leaving, the station from tools/art/backdrop_l22.py)

Run: python3 tools/art/l22_props.py [geyser] [lip] [crawler] [station] [--review]   (~1 min; no subject = all;
--review rebuilds only the review from the written files)
"""
import contextlib
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import europa_r48 as eu  # noqa: E402  (concept script, unchanged: Europa's ice ramp)
from render import raster, sdf  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "l22_props.py"
BATCH = "M8 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r51"
artkit.REVIEW_ROUND = ROUND
LEVEL = DESIGN / "campaign" / "act-4-deep-water" / "level-22-ice-shelf"
CONCEPT = LEVEL / "concept"
HELD = ROOT / "build" / "art-held" / "sprites"
BACKDROP = ROOT / "assets" / "backdrop" / "level-22"

LIME = np.array((168, 255, 42), float)
WHITE_LIME = np.array((236, 255, 196), float)
ICE = [np.array(c, float) for c in eu.ICE]          # dark -> light
SODIUM = np.array((255, 168, 56), float)
VENT, VENT_GLOW = 48, 64
STEAM_N, RESIDUE_N = 8, 6
LIP = (64, 48)
CRAWLER, CRAWLER_LIGHTS = 48, 64
STATION = (480, 300)
ROAD = (64, 960)
ROAD_X = 240                                        # the road's centre in the play field (section 3)
SS = 4


# =========================================================================== held sprites

def _yaml(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


def _rooted(name, root):
    return name == root or name.startswith(root + "-")


def level_roots():
    """The roots the levels' data claims for these props (SpriteUse): a ground target's `sprite`, a drop's `sprite`,
    a vents block's `look` (the look's root needs SpriteUse to claim it: step B4)."""
    roots = set()
    use = (ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java").read_text(
        encoding="utf-8")
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        data = _yaml(f)
        for g in data.get("ground_targets") or []:
            if g.get("sprite"):
                roots.add(str(g["sprite"]))
        for d in data.get("drops") or []:
            if isinstance(d, dict) and d.get("sprite"):
                roots.add(str(d["sprite"]))
        vents = data.get("vents")
        if isinstance(vents, dict) and vents.get("look") == "ice-geyser" and '"ice-geyser"' in use:
            roots.add("ice-geyser")
    return roots


def claimed(name):
    return any(_rooted(name, r) for r in level_roots())


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


def write(name, frames, single=False):
    with sprites_of(name) as target:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"{name}: {len(frames)} frames -> {target.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, HELD):
        before = artkit.SPRITES
        artkit.SPRITES = folder
        try:
            frames = artkit.load_frames(name)
        finally:
            artkit.SPRITES = before
        if frames:
            return frames
    raise FileNotFoundError(name)


# =========================================================================== shared drawing

def grid(w, h, s=SS):
    yy, xx = np.mgrid[0:h * s, 0:w * s].astype(np.float64)
    return (xx + 0.5) / s, (yy + 0.5) / s


def down(rgba_hi, s=SS, one_bit=True):
    """A supersampled float RGBA (0..255, straight alpha) -> the native image (coverage-weighted box)."""
    h, w = rgba_hi.shape[0] // s, rgba_hi.shape[1] // s
    a = rgba_hi[..., 3:4] / 255
    pre = (rgba_hi[..., :3] * a).reshape(h, s, w, s, 3).mean(axis=(1, 3))
    cov = a.reshape(h, s, w, s, 1).mean(axis=(1, 3))
    rgb = pre / np.maximum(cov, 1e-6)
    alpha = (cov[..., 0] >= 0.5) * 255.0 if one_bit else cov[..., 0] * 255
    out = np.dstack([np.clip(rgb, 0, 255), alpha])
    out[alpha == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def crack_path(seed=2261, length=34.0, cx=24.0, cy=24.0, angle=-0.45):
    """The geyser's fissure: a jagged polyline across the 48 px frame (a few side branches)."""
    rng = np.random.default_rng(seed)
    n = 9
    pts = []
    for i in range(n):
        u = i / (n - 1) - 0.5
        off = rng.uniform(-2.2, 2.2) if 0 < i < n - 1 else 0.0
        pts.append((cx + np.cos(angle) * length * u - np.sin(angle) * off,
                    cy + np.sin(angle) * length * u + np.cos(angle) * off))
    branches = []
    for i in (2, 5, 6):
        a = angle + rng.choice((-1, 1)) * rng.uniform(0.9, 1.4)
        x, y = pts[i]
        branches.append([(x, y), (x + np.cos(a) * 6, y + np.sin(a) * 6),
                         (x + np.cos(a + 0.4) * 9, y + np.sin(a + 0.4) * 9)])
    return pts, branches


def seg_dist(xx, yy, pts):
    d = np.full(xx.shape, 1e9)
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        dx, dy = x1 - x0, y1 - y0
        t = np.clip(((xx - x0) * dx + (yy - y0) * dy) / (dx * dx + dy * dy), 0, 1)
        d = np.minimum(d, np.hypot(xx - x0 - t * dx, yy - y0 - t * dy))
    return d


# =========================================================================== ice geyser

HEAT = (0.0, 0.35, 0.7, 1.0)


def geyser_frame(state):
    """The crack at tell ``state`` (0 dormant): a pale frost patch round it, the refrozen rim, the dark fissure,
    from state 1 a lime-white glow rising in the fissure."""
    xx, yy = grid(VENT, VENT)
    pts, branches = crack_path()
    d = seg_dist(xx, yy, pts)
    db = np.min([seg_dist(xx, yy, b) for b in branches], axis=0)
    heat = HEAT[state]
    n = raster.fbm(VENT * SS, VENT * SS, 10 * SS, 2262, octaves=3, period=False)
    frost = np.clip(1 - np.minimum(d, db + 3) / (11 + 5 * n), 0, 1)
    out = np.zeros(xx.shape + (4,))
    # frost patch: refrozen white ice, broken edge (opaque, it sits on the shelf)
    patch = frost > 0.5 + 0.2 * (n - 0.5)
    lit = 1 + np.clip(((xx - 24) * -0.6 + (yy - 24) * -0.8) / 30, -0.15, 0.15)
    out[patch, :3] = (ICE[4] * (1 - 0.35 * heat * frost[..., None]) * lit[..., None])[patch]
    out[patch, 3] = 255
    width = 1.6 + 1.4 * heat
    fiss = (d < width) | (db < 0.9 + 0.5 * heat)
    rim = ((d < width + 1.3) | (db < 1.9 + 0.5 * heat)) & ~fiss
    out[rim, :3] = (236, 244, 252)
    out[rim, 3] = 255
    dark = np.array((14, 26, 44.0))
    glow = np.clip(1 - np.minimum(d, db) / width, 0, 1) * heat
    hot = WHITE_LIME if heat >= 1 else LIME
    col = dark[None, None, :] * (1 - glow[..., None]) + (LIME * 0.7 + hot * 0.3 * heat)[None, None, :] * glow[..., None]
    col = np.where((glow > 0.75)[..., None] & (heat >= 0.7), WHITE_LIME, col)
    out[fiss, :3] = col[fiss]
    out[fiss, 3] = 255
    return down(out)


def geyser_glow(i):
    level = (0.45, 0.75, 1.0)[i]
    w = VENT_GLOW
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    pts, branches = crack_path(cx=w / 2, cy=w / 2)
    d = seg_dist(xx, yy, pts)
    v = np.exp(-(d / (3 + 3 * level)) ** 2) * 0.85 * level
    v += np.exp(-(np.hypot(xx - w / 2, yy - w / 2) / 14) ** 2) * 0.25 * level
    r = np.hypot(xx - w / 2, yy - w / 2)
    ang = np.arctan2(yy - w / 2, xx - w / 2)
    for k, rr in enumerate((12 + 8 * level, 20 + 8 * level)):            # the shimmer's broken rings
        broken = (np.sin(ang * 5 + k * 1.7 + i) > 0.1)
        v += np.exp(-((r - rr) / 0.9) ** 2) * broken * 0.25 * level
    v = artkit.ordered_dither(np.clip(v, 0, 1), 9)
    col = LIME * v[..., None] * 0.85 + WHITE_LIME * np.clip(v - 0.6, 0, 1)[..., None] * 0.6
    return artkit.additive(Image.fromarray(np.dstack([np.clip(col, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def puff_layer(w, h, lumps, seed, shade_k=38, max_a=0.92, levels=6, shadow=(5, 7, 0.32)):
    """Lumps of steam (x, y, r, density) -> stepped-translucency white vapour lit from the upper left, with a soft
    cast shadow to the lower right (so white steam reads on white ice)."""
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    a = np.zeros((h, w))
    for x, y, r, k in lumps:
        dd = np.hypot(xx - x, yy - y) / max(r, 0.5)
        a = np.maximum(a, np.clip(1 - dd ** 2, 0, 1) * k * (0.75 + 0.45 * n))
    a = np.clip(a, 0, 1)
    blur = np.array(Image.fromarray((a * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.6)),
                    dtype=np.float64) / 255
    gy, gx = np.gradient(blur)
    shade = np.clip(1 - (gx + gy) * shade_k, 0.62, 1.12)
    rgb = np.array((244, 248, 252.0)) * shade[..., None] * np.array((0.94, 0.97, 1.0))
    alpha = artkit.smooth_steps(blur * max_a)
    sdx, sdy, sk = shadow
    sh = np.roll(np.roll(blur, sdy, axis=0), sdx, axis=1)
    if sdy > 0:
        sh[:sdy] = 0
    if sdx > 0:
        sh[:, :sdx] = 0
    sha = artkit.smooth_steps(sh * sk)      # step DITH8: smooth steps, the vapour composited over its shadow (was a
    a_out = alpha + sha * (1 - alpha)        # 6- and 4-step ordered dither, the shadow only where the vapour was 0)
    rgb = (rgb * alpha[..., None] + np.array((40, 66, 96.0)) * (sha * (1 - alpha))[..., None]) \
        / np.maximum(a_out, 1e-6)[..., None]
    out = np.dstack([np.clip(rgb, 0, 255), artkit.smooth_steps(a_out) * 255])
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def geyser_steam(i):
    """The steam column from above, frame i of 8 over 0.8 s: the burst out of the crack, the column, the collapse."""
    w = VENT_GLOW
    c = w / 2
    radius = (11.0, 18.0, 22.0, 23.0, 22.5, 23.0, 18.0, 11.0)[i]
    dens = (0.9, 1.0, 1.0, 1.0, 1.0, 1.0, 0.8, 0.5)[i]
    rng = np.random.default_rng(2270 + i % 4)
    lumps = [(c, c, radius * 0.85, dens * 1.1)]
    for k in range(9):
        a = k * TAU / 9 + rng.uniform(-0.25, 0.25) + i * 0.35
        rr = radius * rng.uniform(0.45, 0.7)
        lumps.append((c + np.cos(a) * rr, c + np.sin(a) * rr, radius * rng.uniform(0.38, 0.5), dens * 0.9))
    pts, _ = crack_path(cx=c, cy=c)
    if i < 2:                                                       # the burst follows the crack first
        for x, y in pts[1:-1]:
            lumps.append((x, y, 4 + 3 * i, 1.0))
    return puff_layer(w, w, lumps, 2280 + i, max_a=1.0)


def geyser_residue(i):
    w = VENT_GLOW
    c = w / 2
    t = (i + 0.5) / RESIDUE_N
    rng = np.random.default_rng(2290)
    lumps = []
    for k in range(7):
        a = rng.uniform(0, TAU)
        rr = 6 + 14 * t
        lumps.append((c + np.cos(a) * rr + 8 * t, c + np.sin(a) * rr - 10 * t, 7 + 6 * t, 0.7 * (1 - t) ** 1.2))
    return puff_layer(w, w, lumps, 2291, max_a=0.7, levels=4, shadow=(4, 6, 0.18 * (1 - t)))


def build_geyser():
    frames = artkit.quantize_set([geyser_frame(s) for s in range(4)], 48)
    write("ice-geyser", frames)
    write("ice-geyser-glow", [geyser_glow(i) for i in range(3)])
    write("ice-geyser-steam", [geyser_steam(i) for i in range(STEAM_N)])
    write("ice-geyser-residue", [geyser_residue(i) for i in range(RESIDUE_N)])


# =========================================================================== crevasse lip (the secret)

def lip_frame(state):
    """Top-down: the crevasse (a dark gash across the frame, blue walls lit on the far side; the backdrop's
    `crevasse` piece continues it), a snow bridge (the lip) spanning it with its shadow cast into the gash, the
    cache's orange case in the shadow beside it; state 1 cracked and bitten, 2 fallen (stubs at both rims, broken
    blocks deep in the gash, the cache gone)."""
    w, h = LIP
    xx, yy = grid(w, h)
    n = raster.fbm(w * SS, h * SS, 6 * SS, 2301, octaves=3, period=False)
    out = np.zeros(xx.shape + (4,))
    top = 13 + 1.5 * np.sin(xx / 9) + 2 * (n - 0.5)                  # the gash's rims (the backdrop's crevasse
    bot = 35 + 1.5 * np.sin(xx / 8 + 2) + 2 * (n - 0.5)              # continues it, centred on the frame's row 24)
    gash = (yy > top) & (yy < bot)
    u = np.clip((yy - top) / np.maximum(bot - top, 1), 0, 1)
    wall = np.clip(1 - u / 0.35, 0, 1)                                 # the far (upper) wall catches the light
    deep = np.array((8, 16, 30.0))
    col = deep * (1 - wall[..., None]) + ICE[2] * wall[..., None]
    col = np.where((u > 0.8)[..., None], ICE[1] * 0.7, col)            # the near wall's dim lip
    out[gash, :3] = col[gash]
    out[gash, 3] = 255
    rim = (np.abs(yy - top) < 1.2) | (np.abs(yy - bot) < 1.2)
    out[rim, :3] = ICE[5]
    out[rim, 3] = 255
    if state < 2:                                                      # the cache in the shadow, right of the bridge
        case = (np.abs(xx - 43) < 6) & (np.abs(yy - 24) < 4)
        out[case, :3] = (190, 86, 24) if state == 0 else (160, 72, 22)
        band = case & (np.abs(xx - 43) % 4.5 < 0.9)
        out[band, :3] = (214, 210, 200)
    bx = 24 + 2 * np.sin(yy / 5)                                       # the snow bridge across the gash
    bridge = (np.abs(xx - bx) < 11 + 2 * n) & (yy > top - 3) & (yy < bot + 3)
    if state == 1:
        bridge &= ~(np.hypot(xx - 28, yy - 24) < 6)                    # a bite shot out of it
    if state < 2:
        shadow = (np.abs(xx - bx - 6) < 11) & gash & ~bridge          # cast to the lower right into the gash
        out[shadow, :3] = out[shadow, :3] * 0.55
        lit = np.clip(1.1 - (xx - bx + 11) / 22 * 0.3, 0.75, 1.1)
        out[bridge, :3] = (ICE[5][None, None, :] * lit[..., None])[bridge]
        out[bridge, 3] = 255
    else:                                                              # fallen: stubs and blocks in the gash
        stub = (np.abs(xx - bx) < 11) & (((yy > top - 3) & (yy < top + 3 + 2 * n)) | ((yy < bot + 3) & (yy > bot - 3 - 2 * n)))
        out[stub, :3] = ICE[4]
        out[stub, 3] = 255
        rng = np.random.default_rng(2302)
        for _ in range(7):
            x, y, r = rng.uniform(14, 36), rng.uniform(18, 30), rng.uniform(1.8, 3.4)
            m = (np.abs(xx - x) + np.abs(yy - y) * 1.3) < r * 1.4
            out[m, :3] = ICE[3] * 0.75
    if state >= 1:
        for pts in ([(17, 12), (22, 19), (20, 27)], [(32, 34), (28, 25)]):
            d = seg_dist(xx, yy, pts)
            m = (d < 0.7) & (out[..., 3] > 0) & bridge
            out[m, :3] = ICE[1]
    return light_rim(down(out))


def light_rim(img, col=(250, 252, 255)):
    a = np.array(img)
    solid = a[..., 3] > 0
    pad = np.pad(solid, 1)
    edge = solid & ~(pad[:-2, 1:-1] & pad[1:-1, :-2])                   # exposed to the upper left
    a[edge, :3] = np.clip(a[edge, :3].astype(int) // 2 + np.array(col) // 2, 0, 255)
    return Image.fromarray(a, "RGBA")


def lip_glow(i):
    """The cache's marker light: a slow blink (0-3), then a hurried flicker (4-7)."""
    w, h = LIP
    on = (1.0, 0.6, 0.0, 0.0, 1.0, 0.3, 0.9, 0.0)[i]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    d = np.hypot(xx - 40, yy - 23)
    v = (np.exp(-(d / 1.3) ** 2) + np.exp(-(d / 5) ** 2) * 0.45) * on
    v = artkit.smooth_steps(v)
    col = np.array((255, 120, 40.0)) * v[..., None] + 255 * np.clip(v - 0.8, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(col, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def build_lip():
    write("crevasse-lip", artkit.quantize_set([lip_frame(s) for s in range(3)], 28))
    write("crevasse-lip-glow", [lip_glow(i) for i in range(8)])


# =========================================================================== the Conamara ice-crawler

RED = (0.7, 0.13, 0.08)
WHITE = (0.84, 0.86, 0.88)
TREAD = (0.1, 0.1, 0.11)
GLASS = (0.06, 0.1, 0.14)
STEEL = (0.45, 0.46, 0.48)
AMBER = (1.0, 0.55, 0.12)


def crawler_mats(phase):
    links = lambda p, n: np.where(((p[:, 1] * 1.4 + phase / 3.0) % 1.0) < 0.5, 0.55, 1.15)  # noqa: E731
    return [Material(RED, metal=0.2, shininess=60, spec=0.6),                      # 0 cab
            Material(WHITE, metal=0.15, shininess=40, spec=0.45),                  # 1 body, bed
            Material(TREAD, metal=0.3, shininess=30, spec=0.3, pattern=links),     # 2 tracks
            Material(GLASS, metal=0.6, shininess=120, spec=1.2),                   # 3 windscreen
            Material(STEEL, metal=0.6, shininess=70, spec=0.7),                    # 4 rails, mast
            Material((0.1, 0.1, 0.1), emission=(1.2, 0.66, 0.15)),                # 5 beacon
            Material((0.1, 0.1, 0.1), emission=(1.3, 1.25, 1.05))]                 # 6 headlights


def crawler_scene(heading):
    """The crawler (model units = px), nose toward +y before ``heading`` turns it (0 = up the screen, 1 = down)."""
    flip = 1.0 if heading == 0 else -1.0

    def scene(p):
        q = p.copy()
        q[:, 1] *= flip
        q[:, 0] *= flip
        items = []
        for s in (-1, 1):
            items.append((sd_box(q, (s * 9.5, 0, 2.5), (4.0, 18.0, 2.6), 1.6), 2))
        items.append((sd_box(q, (0, 1, 5.5), (8.0, 15.5, 2.0), 1.0), 1))             # chassis
        items.append((sd_box(q, (0, 9.5, 9.5), (8.5, 6.5, 4.0), 2.0), 0))           # the red cab, forward
        items.append((sd_box(q, (0, 15.0, 9.8), (6.5, 1.0, 2.2), 0.8), 3))         # windscreen
        items.append((sd_box(q, (0, -7.5, 7.0), (8.0, 8.0, 0.8), 0.5), 1))          # the cargo bed floor
        for s in (-1, 1):
            items.append((sd_box(q, (s * 8.2, -7.5, 8.6), (0.7, 8.0, 1.6), 0.3), 4))   # bed rails
        items.append((sd_box(q, (0, -15.6, 8.6), (8.0, 0.6, 1.6), 0.3), 4))
        items.append((sd_cylinder_z(q, (-4.0, 7.0, 14.5), 1.6, 1.0), 5))            # the amber beacon
        items.append((sd_capsule(q, (5.0, 6.0, 13.0), (5.0, 6.0, 19.0), 0.5), 4))   # antenna
        for s in (-1, 1):
            items.append((sd_sphere(q, (s * 5.5, 16.0, 8.0), 1.3), 6))              # headlights
        return union(*items, k=0.4)
    return scene


def crawler_frame(job):
    heading, phase = job
    hi, f = artkit.render_hi(crawler_scene(heading), crawler_mats(phase), (CRAWLER, CRAWLER), float(CRAWLER),
                             z_top=40.0, steps=140)
    return artkit.native(hi, f)


def crawler_lights(heading, flash):
    w = CRAWLER_LIGHTS
    c = w / 2
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    sign = -1 if heading == 0 else 1                     # beams up the screen (heading 0) or down
    v = np.zeros((w, w))
    for s in (-1, 1):
        hx, hy = c + s * 5.5 * (1 if heading == 0 else -1), c + sign * 16
        ahead = (yy - hy) * sign
        spread = np.abs(xx - hx) - ahead * 0.28
        cone = np.clip(ahead / 14, 0, 1) * np.clip(1 - ahead / 30, 0, 1) * np.clip(1 - spread / 4, 0, 1)
        v += cone * 0.5 + np.exp(-((xx - hx) ** 2 + (yy - hy) ** 2) / 2.5) * 0.9
    col = np.array((255, 246, 214.0)) * np.clip(v, 0, 1)[..., None]
    if flash:
        bx, by = c - 4 * (1 if heading == 0 else -1), c - 7 * (1 if heading == 0 else -1)
        b = np.exp(-((xx - bx) ** 2 + (yy - by) ** 2) / 3.0) + np.exp(-((xx - bx) ** 2 + (yy - by) ** 2) / 40) * 0.5
        col += np.array((255, 150, 40.0)) * np.clip(b, 0, 1)[..., None]
    q = artkit.ordered_dither(np.clip(col.max(axis=-1) / 255, 0, 1), 9)
    col = col / np.maximum(col.max(axis=-1, keepdims=True), 1) * q[..., None] * 255
    return artkit.additive(Image.fromarray(np.dstack([np.clip(col, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def build_crawler():
    jobs = [(h, k) for h in range(2) for k in range(3)]
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(crawler_frame, jobs))
    write("ice-crawler", artkit.quantize_set(frames, 32))
    write("ice-crawler-lights", [crawler_lights(h, f) for h in range(2) for f in (True, False)])
    artkit.write_pivots("ice-crawler", SOURCE, {
        "size": [CRAWLER, CRAWLER],
        "bed": [[24, 31], [24, 17]],
        "headings": ["up the screen", "down the screen"],
        "note": "per heading the cargo bed's centre (px from the frame's top left), where the armour patch rides",
    })


# =========================================================================== the station (backdrop pieces)

def station_mats():
    panel = lambda p, n: 0.96 + 0.04 * ((np.floor(np.arctan2(p[:, 1], p[:, 0]) * 6) + np.floor(np.hypot(p[:, 0], p[:, 1]) / 11)) % 2)  # noqa: E731
    return [Material((0.84, 0.86, 0.88), metal=0.0, shininess=20, spec=0.18, pattern=panel),  # 0 dome panels
            Material((0.62, 0.64, 0.66), metal=0.3, shininess=40, spec=0.4),                 # 1 modules, tubes
            Material((0.7, 0.14, 0.08), metal=0.2, shininess=50, spec=0.5),                  # 2 red trim
            Material((0.1, 0.1, 0.1), emission=(1.25, 0.72, 0.24)),                          # 3 sodium windows
            Material((0.32, 0.33, 0.36), metal=0.4, shininess=50, spec=0.5),                 # 4 pad, steel
            Material((0.42, 0.44, 0.46), metal=0.6, shininess=80, spec=0.8),                 # 5 mast
            Material((0.12, 0.14, 0.16), metal=0.3, shininess=40, spec=0.4)]                 # 6 hatch, dark


MAIN = (240.0, 150.0, 92.0)                 # the main dome's centre (x, y from the piece's top left) and radius
NORTH = (400.0, 74.0, 50.0)
PAD = (86.0, 196.0, 46.0)


def station_scene(p):
    """The station in px units, y up = -(piece y)."""
    def at(x, y):
        return (x - STATION[0] / 2, STATION[1] / 2 - y)
    items = []
    mx, my = at(*MAIN[:2])
    nx, ny = at(*NORTH[:2])
    px_, py_ = at(*PAD[:2])
    dome = np.maximum(sd_sphere(p, (mx, my, -MAIN[2] * 0.62), MAIN[2]), -p[:, 2])
    items.append((dome, 0))
    items.append((np.maximum(sd_sphere(p, (nx, ny, -NORTH[2] * 0.55), NORTH[2]), -p[:, 2]), 0))
    items.append((sd_cylinder_z(p, (mx, my, 1.0), MAIN[2] * 0.8 + 6, 1.5), 2))              # ring footing
    items.append((sd_cylinder_z(p, (nx, ny, 1.0), NORTH[2] * 0.84 + 5, 1.5), 2))
    items.append((sd_cylinder_z(p, (nx + 8, ny + 6, NORTH[2] * 0.55), 9, 1.2), 6))         # the shaft hatch on top
    tube = lambda a, b, r=6.0: sd_capsule(p, (a[0], a[1], r * 0.8), (b[0], b[1], r * 0.8), r)  # noqa: E731
    items.append((tube((mx + 60, my + 30), (nx - 36, ny - 14)), 1))
    for (x, y, w, h) in ((372, 196, 46, 26), (392, 246, 30, 22), (110, 82, 34, 24), (172, 262, 40, 20)):
        cx, cy = at(x, y)
        items.append((sd_box(p, (cx, cy, 7.0), (w / 2, h / 2, 7.0), 3.0), 1))
        items.append((sd_box(p, (cx, cy, 14.2), (w / 2 - 3, 2.0, 0.6), 0.4), 3))           # lit roof light strip
    items.append((tube((mx + 60, my - 30), at(372, 196)), 1))
    items.append((tube((mx - 60, my + 30), at(110, 82)), 1))
    items.append((tube((mx - 30, my - 60), at(172, 262), 5.0), 1))
    items.append((sd_cylinder_z(p, (px_, py_, 1.0), PAD[2], 1.0), 4))                       # landing pad
    items.append((tube((px_ + 40, py_ + 10), (mx - 66, my - 20), 4.0), 1))
    mast = at(452, 236)
    items.append((sd_capsule(p, (mast[0], mast[1], 0), (mast[0], mast[1], 40), 2.0), 5))
    items.append((sd_sphere(p, (mast[0], mast[1], 41), 3.0), 3))
    for k in range(16):                                                                        # sodium window ring
        a = k * TAU / 16
        items.append((sd_box(p, (mx + np.cos(a) * MAIN[2] * 0.72, my + np.sin(a) * MAIN[2] * 0.72, 5.0),
                             (2.0, 2.0, 4.0), 0.8), 3))
    return union(*items, k=1.5)


STATION_LAMPS = [(262, 18), (218, 282), (122, 112), (356, 150), (58, 252), (150, 34), (322, 268), (440, 178)]


def station_layer():
    """The station's structures alone (transparent round them), lit from the upper left: the backdrop
    (tools/art/backdrop_l22.py) lays them over the run-in's ice at exact rows with the lamps' pools."""
    w, h = STATION
    f = 2
    hi = sdf.render(station_scene, station_mats(), (w * f, h * f), float(w), z_top=120.0, steps=170,
                    key_pos=(-2.2, 2.4, 3.0), ambient=0.3)
    return artkit.native(hi, f, crisp=60)


def packed_ice(w, h, seed):
    """Packed, ploughed ice round the station (flatter and greyer than the shelf), lit from the upper left."""
    n = raster.fbm(w, h, 26, seed, octaves=4, period=False)
    gy, gx = np.gradient(n)
    shade = np.clip(1 - (gx + gy) * 30, 0.8, 1.12)
    col = (ICE[3] * 0.6 + ICE[4] * 0.4)[None, None, :] * shade[..., None]
    ruts = (np.abs(np.sin((np.arange(w)[None, :] + 0.3 * np.arange(h)[:, None]) / 3.5)) > 0.97) * (n > 0.5)
    col[ruts] *= 0.9
    return Image.fromarray(np.dstack([np.clip(col, 0, 255), np.full((h, w), 255)]).astype(np.uint8), "RGBA")


def lamps_on(img, spots, r=26):
    """Sodium lamps on the ice: the warm pool baked into the ground (posterized steps), the lamp head."""
    a = np.array(img).astype(np.float64)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    for x, y in spots:
        d = np.hypot(xx - x, (yy - y) * 1.1)
        pool = np.clip(1 - d / r, 0, 1) ** 1.4
        pool = np.floor(pool * 4) / 4
        a[..., :3] = a[..., :3] * (1 - 0.45 * pool[..., None]) + SODIUM * 0.62 * pool[..., None] + \
            a[..., :3] * pool[..., None] * np.array((0.15, 0.0, -0.2))
    out = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(out)
    for x, y in spots:
        d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=(60, 58, 56, 255))
        d.ellipse([x - 1, y - 1, x + 1, y + 1], fill=(255, 226, 150, 255))
    return out if img is None else img.paste(out) or img


def pad_marks(img):
    """The landing pad's painted ring and H, its edge lights."""
    d = ImageDraw.Draw(img)
    x, y, r = PAD
    d.ellipse([x - r + 5, y - r + 5, x + r - 5, y + r - 5], outline=(214, 160, 40, 255), width=2)
    for dx in (-10, 10):
        d.rectangle([x + dx - 2, y - 14, x + dx + 1, y + 14], fill=(226, 228, 230, 255))
    d.rectangle([x - 10, y - 2, x + 10, y + 1], fill=(226, 228, 230, 255))
    for k in range(8):
        a = k * TAU / 8
        lx, ly = x + np.cos(a) * (r - 1), y + np.sin(a) * (r - 1)
        d.rectangle([lx - 1, ly - 1, lx, ly], fill=(255, 220, 140, 255))


def road_strip(w, h, seed, lamp_every=240, lamp_at=60):
    """The ice-breaker road: ploughed lane with berms either side, ruts, marker poles, a sodium lamp every
    ``lamp_every`` px (wrapping when h is a multiple of it)."""
    n = raster.fbm(w, h, 8, seed, octaves=3, period=False)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    c = w / 2
    dx = np.abs(xx - c)
    out = np.zeros((h, w, 4))
    lane = dx < 16
    berm = (dx >= 16) & (dx < 22 + 3 * n)
    out[lane, :3] = (ICE[3] * 0.85 + np.array((10, 8, 4.0)))
    ruts = lane & ((np.abs(dx - 8) < 1.2) | (np.abs(dx - 8) < 2.0) & (n > 0.55))
    out[ruts, :3] = ICE[2]
    lit = np.where(xx < c, 1.08, 0.86)                                  # the berms' sunlit and shaded faces
    out[berm, :3] = (ICE[5][None, None, :] * lit[..., None] * (0.92 + 0.1 * n[..., None]))[berm]
    out[lane | berm, 3] = 255
    img = Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    for y in range(20, h, 60):                                          # marker poles: orange tips
        for x in (c - 20, c + 20):
            d.rectangle([x - 1, y - 1, x, y + 1], fill=(230, 110, 30, 255))
    spots = [(c + 20, y) for y in range(lamp_at, h, lamp_every)]
    lamps_on(img, spots, r=24)
    return img


def posterize(img, colours):
    a = np.array(img)
    alpha = a[..., 3]
    q = Image.fromarray(np.ascontiguousarray(a[..., :3]), "RGB").quantize(colours, dither=Image.Dither.NONE).convert("RGB")
    out = np.dstack([np.array(q), alpha])
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def build_station():
    """The station's pieces are written by tools/art/backdrop_l22.py (exact rows over the run-in's ice); this
    renders the structures once to check them."""
    station_layer()
    print("station: structures render (the piece itself: tools/art/backdrop_l22.py)")


# =========================================================================== review

FPS = 20


def plate(w, h):
    try:
        import backdrop_l22
        return backdrop_l22.review_plate(w, h)
    except Exception:  # noqa: BLE001
        n = raster.fbm(w, h, 60, 2399, octaves=5, period=False)
        gy, gx = np.gradient(n)
        shade = np.clip(1 + (gx + gy) * -40, 0.7, 1.15)
        col = raster.ramp([(0.0, (110, 140, 172)), (0.5, (184, 204, 224)), (1.0, (232, 240, 250))], n)
        return Image.fromarray(np.clip(col * shade[..., None], 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def review():
    gey, glow, steam, res = (load(n) for n in ("ice-geyser", "ice-geyser-glow", "ice-geyser-steam",
                                                "ice-geyser-residue"))
    lip, lglow = load("crevasse-lip"), load("crevasse-lip-glow")
    crawler, lights = load("ice-crawler"), load("ice-crawler-lights")
    station = Image.open(BACKDROP / "conamara-station.png").convert("RGBA") if (BACKDROP / "conamara-station.png").exists() \
        else station_layer()
    road = road_strip(64, 540, 2313)
    W, H = 480, 540
    base = plate(W, H)
    row_y = 150
    xs = (120, 240, 360)
    period, tell, burn, ripple = 4.0, 1.2, 0.8, 0.4
    frames = []
    for i in range(int(8.0 * FPS)):
        t = i / FPS
        f = base.copy()
        for j, x in enumerate(xs):                       # a row of three, rippling from the left
            u = (t - 0.6 - j * ripple) % period
            if u < tell:
                fr, g = gey[1 + min(2, int(u / 0.4))], glow[min(2, int(u / 0.4))]
            else:
                fr, g = gey[3] if u < tell + burn else gey[0], None
            sprite.paste_center(f, fr, x, row_y)
            if g is not None:
                f = artkit.add_light(f, g, (x - 32, row_y - 32))
            if tell <= u < tell + burn:
                f.alpha_composite(steam[min(STEAM_N - 1, int((u - tell) / burn * STEAM_N))], (x - 32, row_y - 32))
            elif tell + burn <= u < tell + burn + RESIDUE_N / 8:
                f.alpha_composite(res[int((u - tell - burn) * 8)], (x - 32, row_y - 32))
        # the lip: intact, hit at 3 s, fallen at 4.5 s
        st = 0 if t < 3 else 1 if t < 4.5 else 2
        sprite.paste_center(f, lip[st], 420, 300)
        if st < 2:
            k = int(t * 4) % 4 if st == 0 else 4 + int(t * 12) % 4
            f = artkit.add_light(f, lglow[k], (420 - LIP[0] // 2, 300 - LIP[1] // 2))
        # the crawler drives up from the bottom edge, stops, then drives back down
        if t < 4:
            cy, hd = 560 - 50 * t, 0
        elif t < 5:
            cy, hd = 360, 0
        else:
            cy, hd = 360 + 60 * (t - 5), 1
        k = int(t * 12) % 3 if not (4 <= t < 5) else 0
        sp = crawler[hd * 3 + k]
        sprite.paste_center(f, sprite.shadow_of(sp, opacity=0.4, blur=1.0), 244 + 3, cy + 4)
        sprite.paste_center(f, sp, 244, cy)
        f = artkit.add_light(f, lights[hd * 2 + (0 if int(t * 1.25) % 4 == 0 else 1)], (244 - 32, int(cy) - 32))
        frames.append(f.convert("RGB"))
    rows = [("ICE GEYSER: DORMANT, TELL 1-3", gey, 3, False),
            ("TELL GLOW (ADDITIVE)", glow, 2, True),
            ("STEAM COLUMN 0.8 S (NORMAL BLEND)", steam, 1, False),
            ("RESIDUE MIST", res, 1, False),
            ("CREVASSE LIP: INTACT, HIT, FALLEN (2X)", lip, 2, False),
            ("THE CACHE'S MARKER LIGHT (ADDITIVE)", lglow, 1, True),
            ("ICE-CRAWLER: UP X3, DOWN X3 TRACK FRAMES (2X)", crawler, 2, False),
            ("ITS LIGHTS (ADDITIVE)", lights, 1, True)]
    sheet = artkit.review_sheet("LEVEL 22 PROPS - FINAL FRAMES", rows, width=1500, batch=BATCH)
    big = Image.new("RGBA", (1500, sheet.height + 570), tuple(artkit.SHEET_BG[:3]) + (255,))
    big.alpha_composite(sheet)
    y0 = sheet.height + 4
    raster.draw_text(big, 16, y0, "A FRAME FROM THE GIF | CONAMARA STATION (THE BACKDROP'S PIECE) | THE ROAD (64 PX)",
                     raster.LABEL)
    big.alpha_composite(frames[int(1.9 * FPS)].convert("RGBA"), (16, y0 + 16))
    st = plate(480, 300)
    st.alpha_composite(station)
    big.alpha_composite(st, (16 + 490, y0 + 16))
    big.alpha_composite(road, (16 + 980, y0 + 16))
    artkit.save_review(big, frames, CONCEPT, "l22-props", fps=FPS)


SUBJECTS = {"geyser": build_geyser, "lip": build_lip, "crawler": build_crawler, "station": build_station}


def main(args):
    names = [a for a in args if not a.startswith("--")]
    if "--review" not in args:
        for name in names or SUBJECTS:
            SUBJECTS[name]()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
