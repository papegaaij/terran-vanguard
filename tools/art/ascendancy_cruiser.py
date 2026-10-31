#!/usr/bin/env python3
"""Production art: the Ascendancy cruiser, the Level 28 reveal's set piece (design/enemies/bosses/abyssal-maw, *The
cruisers*; M8 part H batch, concept round 58, step ART-H1). The faction's first black-and-gold hulls, from the chosen
round-48 look **b** "Forked monitor" (tools/concept/bosses_r48.py ``cruiser_b`` and ``cr_mats``, imported unchanged): a
forked ram bow, broad sponsons with three twin casemate guns a side and a row of gun ports between them, gold crown fins
down the spine, a tiered bridge aft, black hex-panelled armour with gold trim, an ivory accent, the gold emblem on the
bow deck, the 1 px gold / red rim light of the Ascendancy rule (enemies_r04 ``rim``). The Helix hull code is Level 27's
``HDX-417`` (accepted in round 57; the round-48 mockup's placeholder was ``HX-427``), stencilled in gold down both
sides of the bow.

The hull is symmetric and both cruisers fly bow up, so one render serves both flanks (nothing is mirrored): the left
hull's centre sits 116 px left of its inner edge (x 110 -> centre x -6), the right's 116 px right of its edge (x 370
-> 486), the rest beyond the field's edge; it is 640 px long, longer than the field.

Outputs (assets/sprites/, or build/art-held/sprites/ until vanguard.pipeline.SpriteUse names ``ascendancy-cruiser``:
step H5 adds the claim with the level's ``cruisers:`` block):
  ascendancy-cruiser                240x660, normal: the hull (one 48-colour palette), bow up
  ascendancy-cruiser-glow_0..1      240x660, additive: 0 the running glow (the engines, the red running lights, the
                                    gun ports' dim amber), 1 the broadside's tell (every port hot amber; the field shows
                                    only the inner flank's row), cross-fade 0 -> 1 over the tell's first 0.5 s
  ascendancy-cruiser-shadow         240x660, normal, stepped translucency: the hull's shadow on the ice (drawn under it
                                    offset (+12, +16) once risen; low-air)
  ascendancy-cruiser-ice_0..5       240x660, normal, stepped translucency: the ice crust sheeting off the hull as it
                                    rises (0 crusted and steaming, 5 the last flakes; over the rise's 4 s, ~1.5 fps);
                                    step I2 (round 58 F.3): the crust is Level 28's night ice, magnified by 1/0.6 so
                                    that at the rise's scale it is the ground's own ice lifting
  ascendancy-cruiser-hole           224x476, normal, stepped translucency: step I2: the hole the hull breaks in the
                                    night ice (its footprint at the rise's 0.6, black water, the crust's plates afloat,
                                    a white rim, cracks running out); the game draws it on the ground's scroll where
                                    the hull broke through, the hull moving off it to the wing's pace
  ascendancy-cruiser-muzzle_0..3    48x32, additive: a gun port's blast as a broadside's shells leave (symmetric
                                    across x, so it serves both flanks), 20 fps
  ascendancy-cruiser-pd_0..2        16x16, additive: a point-defence turret's flash, 20 fps
  ascendancy-cruiser-tracer         16x4, additive: one dash of the tell's amber tracer lines (the game draws rows
                                    of dashes across the 70 px band)
  ascendancy-cruiser-flak-marker_0..3  48x48, additive: the flak marker (45 px ring, the boss lob rule): a red-gold
                                    ring with four ticks closing over its 1.0 s, ~4 fps
  ascendancy-cruiser-flak_0..7      64x64, normal, stepped translucency, a hot core baked in: the airburst, 16 fps
  ascendancy-cruiser-spark_0..3     24x24, additive: shots sparking off the armour (gold-white), 20 fps
  assets/pivots/ascendancy-cruiser.json  the frame, the half-width to the inner edge, the gun ports (14 a flank), the
                                    casemates' muzzles (3 a flank), the point-defence turrets (5 a side), the bow code
  design/enemies/bosses/concept/ascendancy-cruiser-final-r58-a.png/.gif   review sheet and GIF (Level 28's breakout:
                                    the cruisers rising through the split ice on the night ice under Jupiter, then the
                                    run: broadside tells and volleys, flak, point defence)

Run: python3 tools/art/ascendancy_cruiser.py [hull|fx] [--review | --relocate]   (~4-9 min, the hull's three 4x renders;
``fx`` only the effects, the shadow and the ice from the written hull; --review only rebuilds the review files;
--relocate moves the held sets into assets/ once SpriteUse claims them, without rendering; backdrop_l28.py first for
the night ice)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import l23kit
import l28kit as kit
from artkit import DESIGN, TAU

import bosses_r48 as b48  # noqa: E402  (concept script, unchanged: the chosen look b's hull)
import enemies_r04 as e4  # noqa: E402  (the Ascendancy rim light)
from render import raster  # noqa: E402

SCRIPT = "ascendancy_cruiser.py"
BATCH = "M8 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r58"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "bosses" / "concept"
ROOT_NAME = "ascendancy-cruiser"
HULL_CODE = "HDX-417"                      # Level 27's wreck (tools/art/backdrop_l27.py), accepted in round 57
SIZE = (240, 660)
PX = 40.0                                  # px per model unit
HALF_W = 116                               # the hull's half-width (model x 2.9) to the inner edge
GOLD = (255, 200, 70)
AMBER = np.array((255, 140, 40), float)

# --------------------------------------------------------------------------- held until SpriteUse names the root

_plain = l23kit.claimed


def claimed(name):
    if l23kit._rooted(name, ROOT_NAME):
        return f'"{ROOT_NAME}"' in l23kit.SPRITE_USE.read_text(encoding="utf-8")
    return _plain(name)


l23kit.claimed = claimed                   # this process only (as l28kit.install)


# --------------------------------------------------------------------------- the hull

def to_px(x, y):
    return [round(SIZE[0] / 2 + x * PX, 1), round(SIZE[1] / 2 - y * PX, 1)]


def _render(mode):
    scene, mats = b48.cruiser_b()
    if mode != "hull":
        mats = b48.cr_mats("b", ports=1.0 if mode == "hot" else 0.0)
        mats = b48.blackened(lambda: (scene, mats))()[1]
    return artkit.native(*artkit.render_hi(scene, mats, SIZE, SIZE[0] / PX), crisp=0 if mode != "hull" else 70)


def hull_code(sp):
    """``HDX-417`` stencilled in gold down both sides of the bow (reading bow to stern on the right, stern to bow on
    the left, as painted on a hull), only on the hull's pixels."""
    tw = raster.text_width(HULL_CODE)
    lay = Image.new("RGBA", (tw + 2, 9), (0, 0, 0, 0))
    raster.draw_text(lay, 1, 1, HULL_CODE, GOLD)
    a0 = np.array(sp)
    out = sp.copy()
    for sgn in (1, -1):
        r = lay.rotate(-90 if sgn > 0 else 90, expand=True)
        x, y = to_px(sgn * 1.55, 3.0)
        out.alpha_composite(r, (int(round(x - r.width / 2)), int(round(y - r.height / 2))))
    b = np.array(out)
    b[..., 3] = a0[..., 3]
    b[a0[..., 3] == 0] = 0
    return Image.fromarray(b, "RGBA")


def glow_frame(black, halo=1.6):
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    blur = np.array(Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(halo)),
                    float)
    out = np.maximum(rgb, np.floor(blur / 255 * 3 + 0.5) / 3 * 255 * 0.8 * (blur.max(-1, keepdims=True) > 10))
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def shadow(hull):
    a = np.array(hull)[..., 3].astype(np.float64) / 255
    m = np.array(Image.fromarray((a * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3)), float) / 255
    alpha = artkit.smooth_steps(m * 0.5)
    out = np.zeros(a.shape + (4,), np.uint8)
    out[..., :3] = (6, 10, 18)
    out[..., 3] = (alpha * 255).astype(np.uint8)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


RISE_SCALE = 0.6                          # the game's CruiserLooks.RISE_SCALE: the hull's size as it breaks the ice


@lru_cache(maxsize=1)
def night_crust():
    """Level 28's night ice (assets/backdrop/level-28/night-ice.png, backdrop_l28.py's ``night-ice``) at the frame's
    size, magnified by 1 / RISE_SCALE so that the crust, drawn at the rise's scale, shows the ice at the ground's own
    pixel size: the crust is the ice the hull lifts (step I2, round 58 F.3)."""
    tile = np.array(Image.open(kit.BACKDROP / "night-ice.png").convert("RGB")).astype(np.float64)
    w, h = SIZE
    cw, ch = int(round(w * RISE_SCALE)), int(round(h * RISE_SCALE))
    x0 = (tile.shape[1] - cw) // 2                                    # the tile's middle: Jupiter's shine half way
    crop = Image.fromarray(tile[:ch, x0:x0 + cw].astype(np.uint8))
    return np.array(crop.resize((w, h), Image.Resampling.NEAREST)).astype(np.float64)


def ice_frame(hull, i, n=6):
    """The ice crust over the hull: the night ice it lifts (``night_crust``) split by dark cracks into plates, breaking
    up into floes that slide off outward and fall away (frame 0 whole and steaming: at the rise's scale the ice of the
    ground round it; frame n-1 a few flakes at the edges)."""
    w, h = SIZE
    u = i / (n - 1)
    hm = np.array(hull)[..., 3] > 0
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    noise = raster.fbm(w, h, 26, 6101, octaves=4, period=False)
    cells = raster.fbm(w, h, 14, 6103, octaves=2, period=False)
    # floes: Voronoi-ish cells from the cell noise; each leaves when the break front (bow to stern, edges first) passes
    rng = np.random.default_rng(6105)
    seeds = np.column_stack([rng.uniform(0, w, 150), rng.uniform(0, h, 150)])
    d2 = (xx[..., None] - seeds[:, 0]) ** 2 + (yy[..., None] - seeds[:, 1]) ** 2
    idx = np.argmin(d2, -1)
    srt = np.sort(d2, -1)
    edge = np.sqrt(srt[..., 1]) - np.sqrt(srt[..., 0]) < 1.6
    leave = (0.0 + 0.7 * (1 - np.abs(seeds[:, 0] - w / 2) / (w / 2)) * 0.6
             + 0.4 * seeds[:, 1] / h + rng.uniform(-0.12, 0.12, len(seeds)))
    gone = leave[idx] < u * 1.25
    out = np.zeros((h, w, 4), np.float64)
    ice = night_crust() * (0.94 + 0.12 * noise[..., None]) * (1 + 0.06 * (cells[..., None] - 0.5))
    crack = edge | (np.abs(cells - 0.5) < 0.012)
    col = np.where(crack[..., None], ice * 0.42 + np.array((4, 8, 14.0)), ice)
    keep = hm & ~gone
    # sliding floes: the ones that are leaving now drift outward (drawn shifted, thinning)
    out[keep, :3] = col[keep]
    out[keep, 3] = 255 * np.where(crack[keep], 0.9, 1.0)
    moving = hm & gone & (leave[idx] > u * 1.25 - 0.25)
    if moving.any():
        ys, xs = np.nonzero(moving)
        sgn = np.where(xs < w / 2, -1, 1)
        age = (u * 1.25 - leave[idx[ys, xs]]) / 0.25
        nx = np.clip((xs + sgn * 26 * age).astype(int), 0, w - 1)
        ny = np.clip((ys + 10 * age).astype(int), 0, h - 1)
        out[ny, nx, :3] = col[ys, xs] * 0.92
        out[ny, nx, 3] = 255 * np.clip(1 - age, 0, 1) * 0.9
    # mist off the crust (frames 0-2)
    if u < 0.5:
        mist = np.clip(noise - 0.45, 0, 1) * 2 * (1 - 2 * u) * hm
        mist = np.array(Image.fromarray((mist * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(4)), float) / 255
        m = (mist > 0.05) & (out[..., 3] < 1)
        out[m, :3] = (226, 236, 246)
        out[m, 3] = mist[m] * 255 * 0.8
    alpha = artkit.ordered_dither(out[..., 3] / 255, 4)
    res = np.dstack([np.clip(out[..., :3], 0, 255), alpha * 255]).astype(np.uint8)
    res[alpha == 0] = 0
    return Image.fromarray(res, "RGBA")


HOLE_MARGIN = 40


def hole(hull):
    """The hole the hull breaks in the night ice as it rises (step I2, round 58 F.3): its footprint at the rise's
    scale, ragged and a few px wider, black water with the crust's plates afloat in it, a broken white rim, cracks
    running out into the ice round it (dark, translucent over the ground). Drawn by the game on the ground's scroll at
    the place the hull broke through, from the rise's start (hidden under the crusted hull until it moves on)."""
    hw, hh = int(round(SIZE[0] * RISE_SCALE)), int(round(SIZE[1] * RISE_SCALE))
    foot = np.array(hull.resize((hw, hh), Image.Resampling.BILINEAR))[..., 3] > 96
    w, h = hw + 2 * HOLE_MARGIN, hh + 2 * HOLE_MARGIN
    mask = np.zeros((h, w), np.uint8)
    mask[HOLE_MARGIN:HOLE_MARGIN + hh, HOLE_MARGIN:HOLE_MARGIN + hw] = foot * 255
    dist = np.array(Image.fromarray(mask).filter(ImageFilter.GaussianBlur(5)), float) / 255
    n = raster.fbm(w, h, 9, 6161, octaves=4, period=False)
    m = raster.fbm(w, h, 30, 6163, octaves=3, period=False)
    water = dist + 0.35 * (n - 0.5) + 0.2 * (m - 0.5) > 0.32
    rim = water & ~(np.array(Image.fromarray((water * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(5))) > 0)
    yy, xx = _yx(w, h)
    cy, cx = h / 2, w / 2
    th = np.arctan2((yy - cy) / (hh / 2 + 10), (xx - cx) / (hw / 2 + 10))
    r = np.hypot((yy - cy) / (hh / 2 + 10), (xx - cx) / (hw / 2 + 10))
    cracks = ~water & (np.abs(np.sin(th * 11 + 3 * n)) < 0.05) & (r < 1.0 + 0.35 * m) & (dist > 0.0)
    cracks |= ~water & (np.abs(np.sin(th * 5 + 5 * m)) < 0.03) & (r < 1.15 + 0.3 * n)
    plates = water & ~rim & (raster.fbm(w, h, 16, 6165, octaves=2, period=False) > 0.62)
    crust = np.array(Image.fromarray(night_crust().astype(np.uint8)).resize((w, h), Image.Resampling.NEAREST), float)
    rgb = np.zeros((h, w, 3))
    rgb[water] = (6, 10, 18)
    rgb[plates] = crust[plates] * 0.8
    rgb[rim] = np.array((214, 226, 238.0)) * np.array((0.38, 0.44, 0.62)) * 1.3
    rgb[cracks] = (8, 12, 22)
    alpha = np.where(water, 1.0, np.where(cracks, 0.6, 0.0))
    alpha = artkit.ordered_dither(alpha, 4)
    res = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    res[alpha == 0] = 0
    return Image.fromarray(res, "RGBA")


# --------------------------------------------------------------------------- effects

def _yx(w, h):
    return np.mgrid[0:h, 0:w].astype(np.float64) + 0.5


def _add(rgb):
    rgb = np.clip(rgb, 0, 255)
    alpha = np.where(rgb.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def muzzle_frame(i, n=4):
    w, h = 48, 32
    u = i / (n - 1)
    yy, xx = _yx(w, h)
    dx, dy = (xx - w / 2), (yy - h / 2)
    f = np.exp(-(dx / (9 + 10 * u)) ** 2 - (dy / (4 + 2 * u)) ** 2) * (1 - 0.7 * u) * 1.3
    f += np.exp(-(np.hypot(dx, dy) / (4 + 5 * u)) ** 2) * (1 - u)
    f = artkit.ordered_dither(np.clip(f, 0, 1), 6)
    white = np.clip(f - 0.7, 0, 1)[..., None] / 0.3
    return _add((AMBER * (1 - white) + 255 * white) * f[..., None])


def pd_frame(i, n=3):
    w = 16
    yy, xx = _yx(w, w)
    r = np.hypot(xx - w / 2, yy - w / 2)
    k = (1.0, 0.65, 0.3)[i]
    f = artkit.ordered_dither(np.clip(np.exp(-(r / (2.2 + i)) ** 2) * 1.3 * k, 0, 1), 5)
    white = np.clip(f - 0.7, 0, 1)[..., None] / 0.3
    return _add((np.array((255, 210, 120.0)) * (1 - white) + 255 * white) * f[..., None])


def tracer():
    w, h = 16, 4
    yy, xx = _yx(w, h)
    f = np.exp(-((yy - h / 2) / 0.9) ** 2) * np.clip(1 - np.abs(xx - w / 2) / (w / 2), 0, 1) ** 0.4
    return _add(np.array((255, 170, 60.0)) * artkit.ordered_dither(f, 5)[..., None])


def marker_frame(i, n=4):
    """The flak marker: a red-gold 45 px ring, four ticks closing inward over its 1.0 s, a centre pip."""
    w = 48
    k = 4
    img = Image.new("RGB", (w * k, w * k), (0, 0, 0))
    d = ImageDraw.Draw(img)
    c = w * k / 2
    r = 22.5 * k
    ring = (255, 90, 50) if i % 2 == 0 else (255, 150, 60)
    d.ellipse([c - r, c - r, c + r, c + r], outline=ring, width=int(1.5 * k))
    tick = (20 - 12 * i / (n - 1)) * k
    for a in (0, TAU / 4, TAU / 2, 3 * TAU / 4):
        ca, sa = np.cos(a + 0.785), np.sin(a + 0.785)
        d.line([(c + ca * (r - tick), c + sa * (r - tick)), (c + ca * (r + 2 * k), c + sa * (r + 2 * k))],
               fill=GOLD, width=int(1.5 * k))
    d.ellipse([c - 2 * k, c - 2 * k, c + 2 * k, c + 2 * k], fill=(255, 220, 140))
    a = np.array(img.resize((w, w), Image.Resampling.BOX), float) * (0.7 + 0.1 * i)
    return _add(a)


def flak_frame(i, n=8):
    """The airburst: a white-orange flash, then a dark grey puff with ragged edges, thinning (stepped)."""
    w = 64
    u = i / (n - 1)
    yy, xx = _yx(w, w)
    noise = raster.fbm(w, w, 9, 6121, octaves=3, period=False)
    r = np.hypot(xx - w / 2, yy - w / 2)
    rad = 9 + 22 * (1 - (1 - u) ** 2)
    dens = np.clip(1 - r / (rad * (0.75 + 0.5 * noise)), 0, 1) ** 0.5 * (1 - u) ** 0.9 * 1.3
    core = np.exp(-(r / (6 + 10 * u)) ** 2) * max(0.0, 1 - 2.0 * u) * 1.2
    rgb = np.array((96, 90, 86.0)) * (0.8 + 0.4 * noise[..., None])
    rgb = rgb * (1 - core[..., None]) + np.array((255, 200, 120.0)) * core[..., None]
    alpha = artkit.ordered_dither(np.clip(np.maximum(dens * 0.9, core * 1.5), 0, 1), 4)
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def spark_frame(i, n=4):
    w = 24
    k = 4
    img = Image.new("RGB", (w * k, w * k), (0, 0, 0))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(6131)
    c = w * k / 2
    for j in range(7):
        a = rng.uniform(0, TAU)
        r0 = (1.5 + 7 * i / (n - 1)) * k
        r1 = r0 + rng.uniform(2, 5) * k * (1 - 0.5 * i / (n - 1))
        col = (255, 245, 200) if j % 2 else GOLD
        d.line([(c + np.cos(a) * r0, c + np.sin(a) * r0), (c + np.cos(a) * r1, c + np.sin(a) * r1)], fill=col,
               width=k)
    if i == 0:
        d.ellipse([c - 3 * k, c - 3 * k, c + 3 * k, c + 3 * k], fill=(255, 250, 230))
    return _add(np.array(img.resize((w, w), Image.Resampling.BOX), float))


# --------------------------------------------------------------------------- pivots

def pivots():
    px, y0, y1, st = b48.PORTS["b"]
    ports = np.arange(y0, y1 + 1e-6, st)
    return {
        "frame": list(SIZE), "half_width": HALF_W, "scale_px_per_unit": PX,
        "placement": "left hull centre = inner edge - half_width (x 110 -> -6), right = edge + half_width "
                     "(x 370 -> 486); bow up",
        "ports": {"right": [to_px(px, y) for y in ports], "left": [to_px(-px, y) for y in ports]},
        "casemates": {"right": [to_px(3.17, y + s) for y in (1.0, -1.3, -3.6) for s in (0.13, -0.13)],
                      "left": [to_px(-3.17, y + s) for y in (1.0, -1.3, -3.6) for s in (0.13, -0.13)]},
        "turrets": {"right": [to_px(1.35, -5.4 + 2.4 * c) for c in range(5)],
                    "left": [to_px(-1.35, -5.4 + 2.4 * c) for c in range(5)]},
        "bow_code": [to_px(1.55, 3.0), to_px(-1.55, 3.0)],
        "emblem": to_px(0, 3.6),
    }


GROUPS = ("hull", "fx")


def build(groups=GROUPS):
    if "hull" in groups:
        with ProcessPoolExecutor(3) as ex:
            hull, cold, hot = ex.map(_render, ("hull", "cold", "hot"))
        hull = artkit.quantize_set([e4.rim(hull_code(hull))], 48)[0]
        kit.write(ROOT_NAME, [hull], SOURCE, single=True)
        kit.write(f"{ROOT_NAME}-glow", artkit.quantize_set([glow_frame(cold), glow_frame(hot)], 24), SOURCE)
    if "fx" in groups:
        hull = kit.load(ROOT_NAME)[0]
        kit.write(f"{ROOT_NAME}-shadow", [shadow(hull)], SOURCE, single=True)
        kit.write(f"{ROOT_NAME}-ice", artkit.quantize_set([ice_frame(hull, i) for i in range(6)], 24), SOURCE)
        kit.write(f"{ROOT_NAME}-hole", artkit.quantize_set([hole(hull)], 16), SOURCE, single=True)
        kit.write(f"{ROOT_NAME}-muzzle", [muzzle_frame(i) for i in range(4)], SOURCE)
        kit.write(f"{ROOT_NAME}-pd", [pd_frame(i) for i in range(3)], SOURCE)
        kit.write(f"{ROOT_NAME}-tracer", [tracer()], SOURCE, single=True)
        kit.write(f"{ROOT_NAME}-flak-marker", [marker_frame(i) for i in range(4)], SOURCE)
        kit.write(f"{ROOT_NAME}-flak", artkit.quantize_set([flak_frame(i) for i in range(8)], 20), SOURCE)
        kit.write(f"{ROOT_NAME}-spark", [spark_frame(i) for i in range(4)], SOURCE)
    artkit.write_pivots(ROOT_NAME, SOURCE, pivots())


# =========================================================================== review

FPS = 15
T0, T1 = 117.0, 133.0
RISE = (118.0, 122.0)
CY = 270
CX = {"L": 110 - HALF_W, "R": 370 + HALF_W}
BROADSIDES = [(123.0, "L", None), (126.0, "R", None), (129.0, "L", 300), (129.0, "R", 420), (132.0, "R", None)]
FLAK = [124.4, 128.4, 131.6]
PD = [(123.6, "L"), (124.9, "R"), (126.1, "L"), (127.4, "R"), (128.6, "L"), (129.9, "R"), (131.1, "L")]


CATCH = 0.5                                # the game's CruiserLooks.CATCH: the share of the rise to the wing's pace


def rise_drop():
    """How far above its running place a hull breaks the ice (the game's CruiserLooks.riseDrop): the ground's speed at
    the rise times half the catch-up, so that it starts at the ice's own speed and slows to the wing's pace by then."""
    return (kit.scroll(RISE[0] + 0.5) - kit.scroll(RISE[0] - 0.5)) * CATCH * (RISE[1] - RISE[0]) / 2


def ship_xy(t):
    return 240 + 70 * np.sin((t - T0) * 0.55), 444 - 40 * np.sin((t - T0) * 0.31) ** 2


def review():
    import backdrop_l28 as b28                                       # noqa: E402  (the night ice, ART-H2's)
    hull = kit.load(ROOT_NAME)[0]
    glow = kit.load(f"{ROOT_NAME}-glow")
    shd = kit.load(f"{ROOT_NAME}-shadow")[0]
    ice = kit.load(f"{ROOT_NAME}-ice")
    hole_img = kit.load(f"{ROOT_NAME}-hole")[0]
    muz, pd, trc = kit.load(f"{ROOT_NAME}-muzzle"), kit.load(f"{ROOT_NAME}-pd"), kit.load(f"{ROOT_NAME}-tracer")[0]
    mk, flak, spk = kit.load(f"{ROOT_NAME}-flak-marker"), kit.load(f"{ROOT_NAME}-flak"), kit.load(f"{ROOT_NAME}-spark")
    piv = pivots()
    rows = [
        ("HULL (1X), THE RUNNING GLOW, THE BROADSIDE'S TELL, THE SHADOW", [hull, glow[0], glow[1], shd], 1, False),
        ("THE ICE CRUST SHEETING OFF AS IT RISES; THE HOLE IT BREAKS IN THE ICE", ice + [hole_img], 1, False),
        ("PORT BLAST, POINT DEFENCE, TRACER DASH, SPARKS (ADDITIVE, 3X)", muz + pd + [trc] + spk, 3, True),
        ("FLAK MARKER (ADDITIVE) AND AIRBURST", mk + flak, 2, False),
    ]
    sheet = artkit.review_sheet("ASCENDANCY CRUISER - FINAL (ROUND 48 LOOK B, FORKED MONITOR; HULL CODE HDX-417)",
                                rows, width=1300, batch=BATCH)
    gp, hp = b28.placed_pieces("ground"), b28.placed_pieces("high-air")
    shots = b48.Shots(water=False)
    rng = np.random.default_rng(6141)
    tells = []
    for tb, side, by in BROADSIDES:
        by = ship_xy(tb)[1] if by is None else by
        tells.append((tb, side, by))
        x0 = 110 if side == "L" else 370
        dx = 1 if side == "L" else -1
        for j in range(7):
            yy = by - 30 + 60 * j / 6 + rng.uniform(-3, 3)
            shots.fire(tb + 1.2 + j * 0.05, x0, yy, x0 + dx * 1000, yy, 260, "shell", 4)
    markers = []
    for tf in FLAK:
        px, py = ship_xy(tf + 0.6)
        for j, (ox, oy) in enumerate(((-40, -50), (45, -20))):
            markers.append((tf, px + ox, py + oy))
            shots.ring(tf + 1.0, px + ox, py + oy, 8, 120, "flak", 3, phase=0.3 * j)
    pd_shots = []
    for tp, side in PD:
        px, py = ship_xy(tp + 0.4)
        tx, ty = piv["turrets"]["right" if side == "L" else "left"][1 + int(tp) % 3]
        sx = CX[side] - SIZE[0] / 2 + tx
        sy = CY - SIZE[1] / 2 + ty
        pd_shots.append((tp, sx, sy))
        for j in range(3):
            shots.fire(tp + j * 0.1, sx, sy, px, py, 170, "pd", 2.5)
    frames = []
    for fi in range(int((T1 - T0) * FPS)):
        t = T0 + fi / FPS
        img, glows, over = b28.ground_view(t, gp, hp)
        solids, adds, top = [], [], []
        rise = float(np.clip((t - RISE[0]) / (RISE[1] - RISE[0]), 0, 1))     # linear, as the game's riseShare
        cy = CY - rise_drop() * max(0.0, 1 - rise / CATCH) ** 2              # anchored on the ice at the rise's start
        hole_y = CY - rise_drop() + (kit.scroll(t) - kit.scroll(RISE[0]))   # the hole on the ground's scroll
        for side in "LR":
            cx = CX[side]
            if t < RISE[0]:
                continue
            if hole_y - hole_img.height / 2 < kit.SCREEN:
                solids.append((hole_img, cx, hole_y))
            k = 0.6 + 0.4 * rise
            dim = 0.35 + 0.65 * rise
            h = hull.resize((int(hull.width * k), int(hull.height * k)), Image.Resampling.NEAREST)
            a = np.array(h).astype(np.float64)
            a[..., :3] = a[..., :3] * dim + np.array((10, 30, 46)) * (1 - dim) * 0.6
            h = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
            if rise > 0.3:
                s = shd.resize(h.size, Image.Resampling.NEAREST)
                solids.append((s, cx + 12 * rise, cy + 16 * rise))
            solids.append((h, cx, cy))
            if rise < 1:
                f = min(5, int(rise * 6))
                solids.append((ice[f].resize(h.size, Image.Resampling.NEAREST), cx, cy))
            g = glow[0]
            hot = 0.0
            for tb, s2, by in tells:
                if s2 == side and tb <= t < tb + 1.4:
                    hot = b48.win(t, tb, tb + 0.5) * (1 - b48.win(t, tb + 1.2, tb + 1.4))
            if rise >= 1:
                adds.append((g, cx, CY, 1.0 - hot))
                if hot > 0:
                    adds.append((glow[1], cx, CY, hot))
            else:
                adds.append((g.resize(h.size, Image.Resampling.NEAREST), cx, cy, rise))
            for tb, s2, by in tells:
                if s2 == side and tb + 1.2 <= t < tb + 1.45:
                    f = min(3, int((t - tb - 1.2) * 16))
                    for ox, oy in piv["ports"]["right" if side == "L" else "left"]:
                        y = CY - SIZE[1] / 2 + oy
                        if abs(y - by) < 36:
                            adds.append((muz[f], cx - SIZE[0] / 2 + ox + (12 if side == "L" else -12), y, 1.0))
        for tb, side, by in tells:
            if tb <= t < tb + 1.2:
                g = 0.6 + 0.4 * np.sin((t - tb) * 18)
                for yy in (by - 24, by, by + 24):
                    for x in range(116, 366, 20):
                        adds.append((trc, x + (int(t * 60) % 20) * (1 if side == "L" else -1), yy, g))
        for tf, mx, my in markers:
            if tf <= t < tf + 1.0:
                adds.append((mk[min(3, int((t - tf) * 4))], mx, my, 1.0))
            if tf + 1.0 <= t < tf + 1.5:
                top.append((flak[min(7, int((t - tf - 1.0) * 16))], mx, my))
        for tp, sx, sy in pd_shots:
            if tp <= t < tp + 0.35:
                adds.append((pd[min(2, int(((t - tp) % 0.1) * 30))], sx, sy, 1.0))
        px, py = ship_xy(t)
        if t > 125 and int(t * 4) % 5 == 0:                     # Rook's/stray shots glancing off a hull
            adds.append((spk[int(t * 20) % 4], 104, 160 + 40 * np.sin(t), 1.0))
        out = kit.frame(img, t, solids=solids, glows=glows + adds, ship_xy=(px, py), over=over + top)
        shots.draw(out, t)
        if 120.0 <= t < 123.5:
            b48.caption(out, "OKAFOR: THOSE ARE NOT VRELL SHIPS. GOD HELP US. THOSE ARE OURS.")
        raster.draw_text(out, 8, 40, "ASCENDANCY CRUISER FINAL - T={:.1f}".format(t), raster.LABEL)
        frames.append(out.convert("RGB"))
    picks = [(118.6, "RISING THROUGH THE ICE"), (121.0, "THE ICE SHEETING OFF"), (123.8, "BROADSIDE TELL"),
             (124.4, "VOLLEY, FLAK"), (129.6, "TWO BANDS"), (131.0, "FLAK, POINT DEFENCE")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 600), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    y0 = sheet.height
    raster.draw_text(big, 16, y0, "AT 1/2 ON LEVEL 28'S NIGHT ICE (THE BREAKOUT, THE RISE, THE RUN); 1:1 CROP OF THE "
                     "BOW AND ITS CODE", raster.LABEL)
    for i, (tt, cap) in enumerate(picks):
        x = 16 + (i % 4) * 258
        y = y0 + 14 + (i // 4) * 290
        big.alpha_composite(frames[int((tt - T0) * FPS)].convert("RGBA").resize((240, 270), Image.Resampling.BOX),
                            (x, y))
        raster.draw_text(big, x, y + 274, cap, raster.LABEL_DIM)
    crop = hull.crop((0, 0, 240, 280))
    plate = Image.new("RGBA", crop.size, (150, 166, 182, 255))
    plate.alpha_composite(crop)
    big.alpha_composite(plate, (16 + 2 * 258 + 8, y0 + 14 + 290))
    # every 2nd frame: the scrolling ice changes every pixel every frame (the GIF's size)
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames[::2]], CONCEPT, ROOT_NAME,
                       fps=FPS / 2)


SETS = ("", "-glow", "-shadow", "-ice", "-hole", "-muzzle", "-pd", "-tracer", "-flak-marker", "-flak", "-spark")
SINGLE = ("", "-shadow", "-tracer", "-hole")


def relocate():
    """Re-write every written set through the claim rule without rendering: once SpriteUse names the root, the held
    frames move from build/art-held/sprites/ into assets/sprites/ (the files are unchanged)."""
    for s in SETS:
        kit.write(ROOT_NAME + s, kit.load(ROOT_NAME + s), SOURCE, single=s in SINGLE)


def main(argv):
    if "--relocate" in argv:
        relocate()
        return
    groups = [a for a in argv if not a.startswith("--")]
    unknown = set(groups) - set(GROUPS)
    if unknown:
        sys.exit(f"unknown groups: {', '.join(sorted(unknown))} (of {', '.join(GROUPS)})")
    if "--review" not in argv:
        build(groups or GROUPS)
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
