#!/usr/bin/env python3
"""Production art: the Level 30 backdrop, the shipping lane out of Ceres (design/campaign/act-5-the-belt/
level-30-minefield; M9 part C batch, concept round 63, step ART-C).

Space only (``ground: none``): no ground layer at all, the backdrop is space and its layers. Straight to production
from round 60's belt look (tools/concept/enemies_r60.py ``space_bg``, the L30 mood still of belt_r60.py: the sunlit
dust band, the beacon buoys) in **A.18 a**, the lighter belt grade the user picked: harsh sun from the top left, but
no near-black screen, so the gunmetal units and the mines' red-gold blink always have a lighter ground somewhere
near. Posterized per image (median cut), wide gradients ordered-dithered; nothing lit is mirrored.

The layers (``scroll_factors``): ``deep`` 0.12 (space: the lane's dust band and stars; Ceres falling behind, far
rocks, the convoy's running lights far ahead, the open belt at the end), ``low-air`` 1.35 (the lane's beacon buoys,
some shot out, the last beacon; slow-tumbling rocks, dimmed so they never read as the play plane's walls),
``high-air`` 2.2 (close rock grit and dust over everything, sparse and dark).

Tile sets (480 wide, wrapping):
  lane-space   deep 480x1024: space a step lighter than Act 4, the sunlit warm dust band of the lane running down
               the screen (left of centre, wandering), stars, a few far rock specks (all sections)
  lane-dust    high-air 480x960, transparent: sparse close grit (dark, lit on the top-left edge) and pale dust motes
Pieces (the layer named; ``frames`` loop at their fps):
  ceres-behind      deep 400x400: Ceres falling behind, its sunlit globe at the bottom right at the start
  far-rock-a, -b, -c  deep 120x100 / 84x72 / 56x48: distant lit rocks, hazed
  convoy-lights     deep 64x120: the convoy far ahead before it arrives: three dark hulls, their gold and red
                    running lights (section 3; the README names low-air, but "far ahead" reads on the deep layer)
  open-belt         deep 480x420: the open belt at the lane's end: a scatter of lit rocks in a brighter haze
  lane-buoy         low-air 40x64, 8 frames at 4 fps: a beacon buoy (float drum, orange bands, a short mast) with its
                    white strobe: one flash in 2 s (frame 0 the flash, frame 1 its afterglow)
  lane-buoy-dark    low-air 40x64: a buoy shot out: its mast snapped, scorched, dark
  lane-beacon       low-air 64x160, 8 frames at 4 fps: the lane's last beacon, a tall mast with a green strobe
  tumbling-rock     low-air 104x104, 16 frames at 2 fps: a big rock tumbling slowly past (an 8 s turn), dimmed
  drift-rock-a, -b  low-air 88x80 / 64x56: rocks drifting past below the play plane, dimmed

Review: design/campaign/act-5-the-belt/level-30-minefield/concept/backdrop-final-r63-a.png (the tile sets, the pieces,
eight screens with stand-ins for a mine line, a Gunship and the ship) and .gif (t 14-24 at half size: the buoys
strobing, Ceres falling behind). ``--proposal`` writes the ``backdrop:`` block, the sections' tiles, the debris block
(the belt rocks' chunks, the large rocks' and the dead buoy's places) to the level's concept/backdrop-proposal.yaml
(never into data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections. Re-run after
the data moves a section.
Run: python3 tools/art/backdrop_l30.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU

import belt_units as bu  # noqa: E402  (tools/art: the belt's rock model)
import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged)
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "backdrop_l30.py"
BATCH = "M9 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r63"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-30-minefield"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-30"
W, SCREEN = 480, 540
SPACE_H, DUST_H = 1024, 960
FACTORS = {"deep": 0.12, "low-air": 1.35, "high-air": 2.2}
WARM_ROCK = [(k, raster.hexrgb(c)) for k, c in e60.WARM_ROCK]

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
PROPOSED_SECTIONS = [
    {"name": "Lane Marker", "end": 30, "atmosphere": "clear", "tiles": ["lane-space", "lane-dust"]},
    {"name": "The Seeded Lane", "end": 88, "speed": 80, "ramp": 2, "atmosphere": "clear",
     "tiles": ["lane-space", "lane-dust"]},
    {"name": "Convoy Crossing", "end": 134, "speed": 80, "atmosphere": "clear", "tiles": ["lane-space", "lane-dust"]},
    {"name": "Lane End", "end": 178, "speed": 100, "ramp": 2, "atmosphere": "clear",
     "tiles": ["lane-space", "lane-dust"]},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speed(i):
    s = SECTIONS[i]
    return float(s["speed"]) if s.get("speed") is not None else BASE_SPEED


def _u(t, start, ramp):
    return min(max((t - start) / ramp, 0.0), 1.0) if ramp > 0 else (1.0 if t >= start else 0.0)


def _ease_out(u):
    return u - u ** 3 + u ** 4 / 2


def _travelled(i, a, b):
    """backdrop_l29.py's port of LevelScroll for a level without halts: a speed change eased over the ramp."""
    speed, start, end = _speed(i), STARTS[i], float(SECTIONS[i]["end"])
    px = (b - a) * speed
    ramp = float(SECTIONS[i].get("ramp", 0) or 0)
    if i > 0 and ramp > 0:
        ramp = min(ramp, end - start)
        px -= (speed - _speed(i - 1)) * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    return px


def scroll(t):
    """LevelData.scrollAt: the ground layer's scroll at level time t, px (before the start at the first speed)."""
    s = 0.0
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]) or i == len(SECTIONS) - 1:
            return s + _travelled(i, STARTS[i], t)
        s += _travelled(i, STARTS[i], float(sec["end"]))
    raise AssertionError


def time_at(s):
    lo, hi = -120.0, END + 60
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll(mid) < s:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def t_for_layer_row(row, factor):
    """The placement time putting a piece's centre on layer row ``row`` (centre = scroll(t) x factor + 270)."""
    return round(time_at((row - SCREEN / 2) / factor), 3)


def t_centre_at(t, y_down, factor):
    """The placement time of a piece whose centre is at screen y (down) at level time t."""
    return t_for_layer_row(scroll(t) * factor + (SCREEN - y_down), factor)


# =========================================================================== shared

def periodic_noise(w, h, cell, seed, octaves=4):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=True)


def posterize(rgb, colours=64, alpha=None):
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    img = img.quantize(colours, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE).convert("RGBA")
    if alpha is not None:
        a = np.array(img)
        a[..., 3] = alpha
        a[alpha == 0] = 0
        img = Image.fromarray(a, "RGBA")
    return img


def hazed(img, k=0.5, tint=(62, 60, 70), haze=0.38):
    """A background body pushed back: darker, toward the dust's warm grey (never mistaken for the play plane)."""
    a = np.array(img).astype(np.float64)
    a[..., :3] = a[..., :3] * k * (1 - haze) + np.array(tint) * haze
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def render_model(scene, mats, size, extent, k=0, n=16, crisp=60):
    rot = model_rotation(np.pi / 2 + TAU * k / n)
    w, h = (size, size) if isinstance(size, int) else size
    hi, f = artkit.render_hi(lambda p: scene(rotate_z(p, rot)), model_space_materials(mats, rot), (w, h), extent)
    return artkit.native(hi, f, crisp)


# =========================================================================== tile sets

def space_tile(seed=3001):
    """The deep layer: space a step lighter than Act 4, the sunlit warm dust band of the lane winding down the
    screen, stars, far rock specks (wraps: everything periodic in the rows)."""
    h, w = SPACE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    p = TAU * yy / h
    centre = 190 + 70 * np.sin(p) + 30 * np.sin(2 * p + 1.3)
    band = np.exp(-((xx - centre) / 125) ** 2)
    neb = periodic_noise(w, h, 64, seed) * 0.6 + periodic_noise(w, h, 16, seed + 3) * 0.4
    dust = band * (0.6 + 0.65 * (neb - 0.5)) + 0.12 * periodic_noise(w, h, 128, seed + 5)
    rgb = np.zeros((h, w, 3)) + np.array((9, 9, 14.0))
    rgb += np.clip(dust, 0, 1)[..., None] * np.array((104, 84, 62.0))
    q = np.dstack([artkit.smooth_steps(rgb[..., c] / 120, 64) for c in range(3)]) * 120
    rng = np.random.default_rng(seed)
    for _ in range(460):
        x, y = rng.integers(0, w), rng.integers(0, h)
        v = rng.uniform(80, 255) * (0.6 if band[y, x] > 0.5 else 1.0)
        q[y, x] = (v, v * 0.97, v * 0.92)
    for _ in range(11):
        x, y, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(1.5, 4)
        d = np.hypot(xx - x, ((yy - y + h / 2) % h) - h / 2)
        m = d < r
        lit = np.clip(0.4 - ((xx - x) + (yy - y)) / (2 * r), 0.15, 1)
        q[m] = np.array((124, 106, 88.0)) * lit[m, None]
    return Image.fromarray(np.clip(q, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def dust_tile(seed=3002):
    """High-air: sparse close grit (dark lumps lit on their top-left edge) and pale dust motes, transparent; the
    rows wrap."""
    h, w = DUST_H, W
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    rng = np.random.default_rng(seed)
    for _ in range(16):
        x, y = rng.uniform(10, w - 10), rng.uniform(0, h)
        r = rng.uniform(1.5, 3.4)
        pts = [(x + np.cos(a) * r * rng.uniform(0.7, 1.2), y + np.sin(a) * r * rng.uniform(0.7, 1.2))
               for a in np.linspace(0, TAU, 6, endpoint=False)]
        for dy in (-h, 0, h):
            d = ImageDraw.Draw(img)
            d.polygon([(px, py + dy) for px, py in pts], fill=(46, 40, 36, 255))
            d.line([pts[3][0], pts[3][1] + dy, pts[4][0], pts[4][1] + dy], fill=(150, 128, 104, 255))
    d = ImageDraw.Draw(img)
    for _ in range(40):
        x, y = rng.uniform(0, w), rng.uniform(0, h)
        d.point((x, y), fill=(150, 140, 126, 120))
    return img


# =========================================================================== pieces

def ceres_behind(size=400, seed=3005):
    """Ceres's sunlit globe (a full disc inside the canvas: no opaque edge), cratered, lit from the top left, its
    night side dark."""
    import enemies_r03 as e3  # noqa: E402  (concept script: the regolith texture)
    yy, xx = np.mgrid[0:size, 0:size].astype(float)
    c, R = size / 2, size / 2 - 8
    d = np.hypot(xx - c, yy - c)
    tex = np.array(e3.regolith(size, size, seed, craters=40).convert("L")).astype(float) / 255
    nz = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    nx, ny = (xx - c) / R, (yy - c) / R
    lit = np.clip(-0.6 * nx - 0.55 * ny + 0.38 * nz, 0.03, 1.0)
    rgb = raster.ramp(WARM_ROCK, np.clip(0.35 + 0.6 * tex, 0, 1)) * (0.2 + 0.95 * lit[..., None])
    limb = np.abs(d - R) < 1.5
    rgb[limb] = rgb[limb] * 0.5 + np.array((210, 190, 160)) * 0.5 * lit[limb, None]
    return posterize(rgb, 40, np.where(d < R, 255, 0).astype(np.uint8))


def far_rock(w, h, seed, k=0.55):
    radius = min(w, h) / 2 / 20 * 0.86
    scene, mats = bu.rock_model(radius, seed)
    img = render_model(scene, mats, (w, h), w / 20.0, k=seed % 16)
    return hazed(img, k, haze=0.42)


def convoy_lights(w=64, h=120):
    """The convoy far ahead: three tiny dark hull slivers in a column, hazed, each with a gold bow light and two red
    running lights (1 px cores, stepped halos): lights, not craft (they must never read as the play plane's units)."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    spots = ((32, 22), (27, 60), (37, 98))
    for x, y in spots:
        d.ellipse([x - 2, y - 6, x + 2, y + 6], fill=(34, 32, 36, 255))
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    for x, y in spots:
        for lx, ly, col in ((x, y - 7, (255, 200, 90)), (x - 3, y + 4, (255, 60, 40)), (x + 3, y + 4, (255, 60, 40))):
            gd.ellipse([lx - 2.5, ly - 2.5, lx + 2.5, ly + 2.5], fill=col + (70,))
            gd.point((lx, ly), fill=col + (255,))
    img.alpha_composite(glow)
    return artkit.stepped_alpha(img, 4)


def open_belt(w=W, h=420, seed=3007):
    """The open belt beyond the lane's end: a brighter haze in the middle and a scatter of distant lit rocks."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    r = np.hypot((xx - w / 2) / (w * 0.45), (yy - h / 2) / (h * 0.42))
    haze = np.clip(1 - r, 0, 1) ** 1.6 * 0.55
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    a = np.zeros((h, w, 4))
    a[..., :3] = np.array((150, 124, 96.0))
    a[..., 3] = artkit.smooth_steps(haze) * 120
    img = Image.fromarray(a.astype(np.uint8), "RGBA")
    rng = np.random.default_rng(seed)
    for i in range(14):
        rw = int(rng.uniform(8, 26))
        rock = far_rock(rw + 4, rw + 4, 3100 + i, k=0.7)
        x, y = rng.uniform(30, w - 30), rng.uniform(30, h - 30)
        img.alpha_composite(rock, (int(x), int(y)))
    out = np.array(img)
    out[:2], out[-2:], out[:, :2], out[:, -2:] = 0, 0, 0, 0
    return Image.fromarray(out, "RGBA")


def buoy_model(snapped=False, mast=0.9):
    """A beacon buoy seen from above (+Y = up the screen: its mast drawn rising up the screen, the top-down sprites'
    convention for tall things): a white-grey float drum with orange hazard bands, a lattice mast with two small
    solar vanes, the strobe housing at the top. ``snapped``: the mast broken off short and bent over."""
    mats = [Material(np.array(raster.hexrgb("c8ccd0")) / 255.0, metal=0.4, shininess=60, spec=0.6),
            Material(np.array(raster.hexrgb("4a4e54")) / 255.0, metal=0.5, shininess=40, spec=0.5),
            Material(np.array(raster.hexrgb("e07828")) / 255.0, shininess=30, spec=0.3),
            Material((0.14, 0.14, 0.16), metal=0.6, shininess=120, spec=1.0),
            Material(np.array(raster.hexrgb("2c3c5c")) / 255.0, metal=0.3, shininess=90, spec=1.1)]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, -0.6, 0.12), 0.46, 0.16), 0),
                 (sd_cylinder_z(p, (0, -0.6, 0.22), 0.47, 0.04), 2),
                 (sd_cylinder_z(p, (0, -0.6, 0.06), 0.47, 0.03), 2)]
        if snapped:
            items += [(sd_capsule(p, (0, -0.5, 0.3), (0, 0.0, 0.3), 0.07, 0.06), 1),
                      (sd_capsule(p, (0, 0.0, 0.3), (0.34, 0.12, 0.26), 0.06, 0.05), 1)]
        else:
            items += [(sd_capsule(p, (0, -0.5, 0.3), (0, mast, 0.3), 0.07, 0.05), 1),
                      (sd_box(p, (-0.2, 0.25, 0.3), (0.14, 0.08, 0.02)), 4),
                      (sd_box(p, (0.2, 0.25, 0.3), (0.14, 0.08, 0.02)), 4),
                      (sd_sphere(p, (0, mast + 0.06, 0.32), 0.1), 3)]
        return union(*items)
    return scene, mats


def strobe(img, x, y, k, col=(255, 255, 255), r=10):
    """A strobe's flash baked onto a piece: a white core and a stepped halo (normal blending, translucency steps)."""
    if k <= 0:
        return img
    w, h = img.size
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    d = np.hypot(xx - x, yy - y)
    halo = np.clip(np.exp(-(d / (r * 0.55)) ** 2) * k, 0, 1)
    a = np.array(img).astype(np.float64)
    al = a[..., 3] / 255
    add_a = np.floor(halo * 3 + 0.5) / 3
    out_a = np.maximum(al, add_a)
    mix = np.where(out_a > 0, add_a / np.maximum(out_a, 1e-6), 0)[..., None]
    a[..., :3] = a[..., :3] * (1 - mix) + np.array(col) * mix
    core = d < 2.2 * k + 0.6
    a[core, :3] = (255, 255, 255)
    a[..., 3] = np.maximum(out_a, core) * 255
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


BUOY = (40, 64)
BEACON = (64, 160)


def _render_job(job):
    kind = job[0]
    if kind == "buoy":
        scene, mats = buoy_model(snapped=job[1])
        return render_model(scene, mats, BUOY, BUOY[0] / 18.0, k=8)
    if kind == "beacon":
        scene, mats = buoy_model(mast=3.4)
        mats[2] = Material(np.array(raster.hexrgb("d8d8d0")) / 255.0, shininess=30, spec=0.3)

        def tall(p, s=scene):
            q = p.copy()
            q[:, 1] = q[:, 1] + 1.75
            return s(q / 1.15)[0] * 1.15, s(q / 1.15)[1]
        return render_model(tall, mats, BEACON, BEACON[0] / 22.0, k=8)
    if kind == "rock":
        name, k = job[1], job[2]
        w, h, seed = ROCK_PIECES[name]
        radius = min(w, h) / 2 / 20 * 0.86
        scene, mats = bu.rock_model(radius, seed)
        return render_model(scene, mats, (w, h), w / 20.0, k=k, crisp=70)
    raise ValueError(kind)


ROCK_PIECES = {"tumbling-rock": (104, 104, 3011), "drift-rock-a": (88, 80, 3013), "drift-rock-b": (64, 56, 3017)}


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    save("lane-space", space_tile())
    save("lane-dust", dust_tile())
    save("ceres-behind", ceres_behind())
    for name, (w, h, seed) in {"far-rock-a": (120, 100, 3021), "far-rock-b": (84, 72, 3023),
                               "far-rock-c": (56, 48, 3029)}.items():
        save(name, far_rock(w, h, seed))
    save("convoy-lights", convoy_lights())
    save("open-belt", open_belt())
    jobs = [("buoy", False), ("buoy", True), ("beacon",)] + [("rock", "tumbling-rock", k) for k in range(16)] + [
        ("rock", "drift-rock-a", 3), ("rock", "drift-rock-b", 11)]
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(_render_job, jobs, chunksize=1))
    buoy, dark, beacon = res[0], res[1], res[2]
    top = _top_light(buoy)
    frames = [strobe(buoy, *top, k) for k in (1.0, 0.45, 0, 0, 0, 0, 0, 0)]
    for i, f in enumerate(artkit.quantize_set(frames, 24)):
        save(f"lane-buoy_{i}", f)
    save("lane-buoy-dark", artkit.quantize_set([_scorched(dark)], 20)[0])
    btop = _top_light(beacon)
    frames = [strobe(beacon, *btop, k, (90, 255, 120), 14) for k in (1.0, 0.45, 0, 0, 0, 0, 0, 0)]
    for i, f in enumerate(artkit.quantize_set(frames, 24)):
        save(f"lane-beacon_{i}", f)
    tumble = artkit.quantize_set([hazed(f) for f in res[3:19]], 24)
    for i, f in enumerate(tumble):
        save(f"tumbling-rock_{i}", f)
    save("drift-rock-a", artkit.quantize_set([hazed(res[19])], 20)[0])
    save("drift-rock-b", artkit.quantize_set([hazed(res[20])], 20)[0])
    return pieces_spec(), placements()


def _top_light(img):
    """The strobe housing: the topmost opaque pixels' centre."""
    a = np.array(img)[..., 3] > 0
    ys, xs = np.nonzero(a)
    y0 = ys.min()
    sel = ys <= y0 + 2
    return float(xs[sel].mean()) + 0.5, float(y0) + 2.5


def _scorched(img):
    a = np.array(img).astype(np.float64)
    a[..., :3] *= 0.55
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def pieces_spec():
    return {
        "ceres-behind": {"layer": "deep", "size": [400, 400]},
        "far-rock-a": {"layer": "deep", "size": [120, 100]},
        "far-rock-b": {"layer": "deep", "size": [84, 72]},
        "far-rock-c": {"layer": "deep", "size": [56, 48]},
        "convoy-lights": {"layer": "deep", "size": [64, 120]},
        "open-belt": {"layer": "deep", "size": [W, 420]},
        "lane-buoy": {"layer": "low-air", "size": list(BUOY), "frames": 8, "fps": 4},
        "lane-buoy-dark": {"layer": "low-air", "size": list(BUOY)},
        "lane-beacon": {"layer": "low-air", "size": list(BEACON), "frames": 8, "fps": 4},
        "tumbling-rock": {"layer": "low-air", "size": [104, 104], "frames": 16, "fps": 2},
        "drift-rock-a": {"layer": "low-air", "size": [88, 80]},
        "drift-rock-b": {"layer": "low-air", "size": [64, 56]},
    }


# =========================================================================== the places

BUOY_X = (44, 436)                 # the lane's left and right thirds, beside the play field's edges
BUOY_SPACING = 600                 # low-air px between two buoys on one side
LAST_BEACON_X = 404


def placements():
    """Every placed piece: (piece, t, x, note). The buoys every 600 low-air px down both sides (staggered by half),
    dark ones in section 2; the last beacon's top edge leaving the bottom edge at the level's end."""
    f = FACTORS["low-air"]
    out = []
    beacon_t = t_centre_at(END, SCREEN + BEACON[1] / 2, f)   # its top row on the bottom edge at the end
    first = scroll(0) * f + SCREEN + 40                # the first buoys just above the top edge at the start
    last = scroll(beacon_t) * f + SCREEN / 2 - 300     # the buoys stop short of the last beacon
    row, n = first - BUOY_SPACING, 0
    s2 = (STARTS[1], STARTS[2])
    while row < last:
        for side, x in enumerate(BUOY_X):
            r = row + side * BUOY_SPACING / 2
            t = t_for_layer_row(r, f)
            if t > beacon_t - 1.0:
                continue
            dark = s2[0] <= t < s2[1] and (n + side) % 3 == 1
            out.append(("lane-buoy-dark" if dark else "lane-buoy", t, x,
                        "a shot-out buoy (section 2)" if dark else ""))
        row += BUOY_SPACING
        n += 1
    out.append(("lane-beacon", beacon_t, LAST_BEACON_X, "the lane's last beacon, its top row leaving the bottom "
                                                         f"edge at the end (t={END:g})"))
    out += [("ceres-behind", t_centre_at(0.0, SCREEN + 30, FACTORS["deep"]), 330, "Ceres falling behind"),
            ("far-rock-a", 12.0, 90, "a far rock"), ("far-rock-c", 46.0, 400, "a far rock"),
            ("far-rock-b", 70.0, 140, "a far rock"), ("convoy-lights", t_centre_at(88.0, 70, FACTORS["deep"]), 300,
                                                      "the convoy far ahead before it arrives (t=96-110)"),
            ("far-rock-c", 120.0, 80, "a far rock"), ("far-rock-a", 140.0, 380, "a far rock"),
            ("open-belt", t_centre_at(END, 170, FACTORS["deep"]), 240, "the open belt ahead at the lane's end")]
    out += [("tumbling-rock", 9.0, 400, "a big rock tumbling past below the play plane"),
            ("drift-rock-a", 26.0, 70, ""), ("drift-rock-b", 52.0, 410, ""),
            ("tumbling-rock", 76.0, 70, ""), ("drift-rock-a", 104.0, 420, ""),
            ("drift-rock-b", 126.0, 60, ""), ("tumbling-rock", 146.0, 420, "")]
    return [{"name": p, "t": round(float(t), 3), "x": x, "note": note} for p, t, x, note in out]


# Level 30's debris (Level 03's rules, the belt's chunks; tools/art/belt_units.py and belt_units_c.py). The large
# rocks are walls: at x 60, 240 or 420, out of the gaps' middles (x 164 and 316) when a line is laid; README: one
# at a time in section 2 (max_large 1), two in sections 3-4.
LARGE = [(36, 60), (50, 420), (66, 240), (80, 60), (92, 420), (100, 60), (112, 240), (120, 420), (128, 60),
         (140, 420), (150, 240), (160, 60), (168, 420)]
SMALL_X = (100, 380, 200, 300, 140, 340, 60, 420)


def debris_lines():
    lines = ["debris:",
             "  # Proposal (ART-C): the belt's rocks; small ones break (6 HP, part B's), large ones are walls (contact",
             "  # `large` 20 per the belt's hazards: a large unit's). The dead beacon buoy (the secret, 20 HP like a",
             "  # small rock, its crate on break) as a chunk of its own. C2 re-checks the large rocks against the",
             "  # authored gaps (never inside a line's gap when it is laid).",
             "  chunks:",
             "    belt-large-a: {size: [88, 80], damage: 20}",
             "    belt-large-b: {size: [66, 60], damage: 20}",
             "    belt-a: {size: [40, 36], hp: 6}",
             "    belt-b: {size: [36, 32], hp: 6}",
             "    belt-c: {size: [32, 28], hp: 6}",
             "    lane-buoy: {size: [28, 28], hp: 20}",
             "  clearance: 120",
             "  max_large: 2",
             "  placed:"]
    rows = []
    for i, (t, x) in enumerate(LARGE):
        rows.append((t, x, "belt-large-a" if i % 2 == 0 else "belt-large-b", [5 if x < 240 else -5, -20]))
    t, i = 8.0, 0
    while t < END - 6:
        if not (38 <= t <= 42):                       # the quiet window
            rows.append((round(t, 1), SMALL_X[i % len(SMALL_X)], "belt-" + "abc"[i % 3],
                         [(-1) ** i * (8 + 4 * (i % 3)), -(30 + 10 * (i % 4))]))
        t += 3.4 if t < 30 else 2.6
        i += 1
    rows.append((118, 60, "lane-buoy", [4, -12]))
    rows.sort(key=lambda r: r[0])
    for t, x, chunk, drift in rows:
        note = "  # the dead beacon buoy (secret)" if chunk == "lane-buoy" else ""
        lines.append(f"    - {{t: {t:g}, x: {x}, chunk: {chunk}, drift: [{drift[0]}, {drift[1]}]}}{note}")
    lines.append("  easy: {leave_out_large: 3}")
    return lines


def proposal_text(pieces, placed):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after C2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 30's shipping lane (tools/art/backdrop_l30.py, step ART-C, from {src}).",
             "  # Space only (ground: none): the deep layer is the base (space, the lane's dust band); low-air holds the",
             "  # beacon buoys (animated: one strobe flash in 2 s), the last beacon and the dimmed rocks; high-air the",
             "  # close grit. At most two animated pieces on the screen at once: lane-buoy with tumbling-rock or",
             "  # lane-beacon (never a tumbling rock near the end).",
             "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far, ground unused",
             "  ramp: 2",
             "  haze_colour: 09090e",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:",
             f"    lane-space: {{layer: deep, height: {SPACE_H}}}   # space, the lane's sunlit dust band, stars",
             f"    lane-dust: {{layer: high-air, height: {DUST_H}}}   # close grit and dust motes (transparent)",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}}}")
    lines.append("sections:   # ART-C's reading of the README's Layout (speeds, 2 s eases); C2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - " + "{" + ", ".join(f"{k}: {v}" for k, v in s.items()) + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); low-air "
                 f"{int(round(scroll(END) * FACTORS['low-air']))}")
    lines += debris_lines()
    return "\n".join(lines) + "\n"


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["debris"]


# =========================================================================== composite, review, check

def piece_images(name, spec):
    n = spec.get("frames")
    if n:
        return [Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)]
    return [Image.open(OUT / f"{name}.png").convert("RGBA")]


_CACHE = {}


def images(name, spec):
    if name not in _CACHE:
        _CACHE[name] = piece_images(name, spec)
    return _CACHE[name]


def tile_rows(name, height, offset):
    if name not in _CACHE:
        _CACHE[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[name]
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[height - 1 - (rows % height)]


def blit(img, p, pieces, t, layer):
    spec = pieces[p["piece"]]
    if spec["layer"] != layer:
        return img
    ims = images(p["piece"], spec)
    frame = ims[int(t * spec.get("fps", 0)) % len(ims)] if spec.get("frames") else ims[0]
    centre = scroll(p["t"]) * FACTORS[layer] + SCREEN / 2
    y_down = SCREEN - (centre - scroll(t) * FACTORS[layer])
    if y_down - frame.height / 2 > SCREEN or y_down + frame.height / 2 < 0:
        return img
    return bu.paste(img, frame, p["x"], y_down)


def compose(t, pieces, placed, debris=None, units=True):
    """The screen at level time t (480x540): the deep tile and pieces, low-air pieces, stand-ins (the play plane: the
    ship, a mine line, the debris), high-air grit."""
    g = scroll(t)
    img = Image.fromarray(tile_rows("lane-space", SPACE_H, g * FACTORS["deep"]), "RGBA")
    for p in placed:
        img = blit(img, p, pieces, t, "deep")
    for p in placed:
        img = blit(img, p, pieces, t, "low-air")
    if units:
        img = stand_ins(img, t, debris)
    dust = Image.fromarray(tile_rows("lane-dust", DUST_H, g * FACTORS["high-air"]), "RGBA")
    img.alpha_composite(dust)
    return img


def _held_or_assets(name):
    try:
        return bu.load(name)
    except FileNotFoundError:
        return None


def stand_ins(img, t, debris):
    """The play plane for the review: the debris chunks in their places (moving with the scroll and their drift), a
    mine line laid at the top, the ship."""
    if debris:
        for row in debris["placed"]:
            u = t - row["t"]
            if u < 0:
                continue
            fr = _held_or_assets(f"debris-{row['chunk']}")
            if not fr:
                continue
            speed = scroll(t) - scroll(row["t"])
            x = row["x"] + row["drift"][0] * u
            y = -20 + speed - row["drift"][1] * u
            if -60 < y < SCREEN + 60:
                img = bu.paste(img, fr[int(u * 2) % len(fr)], x, y)
    mine = _held_or_assets("minelayer-mine")
    if mine and 30 < t < 134:
        for k, x in enumerate((12, 44, 76, 108, 220, 252, 284, 316, 372, 404, 436, 468)):
            img = bu.paste(img, mine[k % len(mine)], x, 150 + 0.27 * x)
    img = bu.paste(img, artkit.load_frames("ship")[2], 240, 470)
    return img


SHOTS = [(0.5, "LANE MARKER: CERES FALLING BEHIND"), (14.0, "THE BUOYS DOWN BOTH SIDES"),
         (36.0, "THE SEEDED LANE: A LARGE ROCK"), (60.0, "SHOT-OUT BUOYS, A MINE LINE"),
         (93.0, "THE CONVOY'S LIGHTS FAR AHEAD"), (118.0, "THE DEAD BUOY (SECRET)"),
         (150.0, "LANE END"), (END - 0.5, "THE LAST BEACON, THE OPEN BELT")]


def review():
    pieces, placed, debris = load_proposal()
    shots = [(compose(t, pieces, placed, debris), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    piece_names = ["lane-buoy", "lane-buoy-dark", "lane-beacon", "tumbling-rock", "drift-rock-a", "drift-rock-b",
                   "far-rock-a", "far-rock-b", "far-rock-c", "convoy-lights", "ceres-behind"]
    pieces_h = 210
    tile_h = SPACE_H // 4
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + pieces_h + tile_h + 70,
                         "LEVEL 30 BACKDROP (FINAL R63): THE SHIPPING LANE (SPACE ONLY)",
                         f"PRODUCTION ART, {BATCH.upper()} - R63")
    raster.draw_text(sheet, 16, 38, "EIGHT SCREENS AT HALF SIZE WITH STAND-INS (THE ROCKS AND THE DEAD BUOY IN THEIR "
                                    "PROPOSED PLACES, A MINE LINE, THE SHIP)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 10
    raster.draw_text(sheet, 16, y, "THE PIECES AT 1X ON THE LANE'S SPACE (THE BUOY'S FLASH, ITS AFTERGLOW; THE "
                                   "BEACON'S GREEN FLASH; THE ROCKS DIMMED)", raster.LABEL)
    x = 16
    space = Image.open(OUT / "lane-space.png").convert("RGBA")
    for name in piece_names:
        ims = images(name, pieces[name])
        for fr in ims[:2] if pieces[name].get("frames") and "rock" not in name else ims[:1]:
            fr = fr if fr.height <= 190 else fr.resize((fr.width * 190 // fr.height, 190), Image.BOX)
            if x + fr.width > sheet.width - 16:
                break
            cell = space.crop((0, 0, fr.width + 8, fr.height + 8))
            cell.alpha_composite(fr, (4, 4))
            sheet.alpha_composite(cell, (x, y + 16))
            x += fr.width + 14
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (QUARTER SIZE): LANE-SPACE; LANE-DUST (ON A GREY PLATE)",
                     raster.LABEL)
    x = 16
    for name in ("lane-space", "lane-dust"):
        tl = Image.open(OUT / f"{name}.png").convert("RGBA")
        small = tl.resize((tl.width // 4, tl.height // 4), Image.BOX) if name == "lane-space" else tl.resize(
            (tl.width // 2, tl.height // 4), Image.NEAREST)
        pl = Image.new("RGBA", small.size, (60, 64, 76, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl, (x, y + 16))
        x += small.width + 12
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    frames = [compose(14.0 + i / 10, pieces, placed, debris).resize((sw, sh), Image.BOX) for i in range(100)]
    size = e60.rig.write_gif(frames, gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def wrap_score(a):
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def check():
    """The space tile wraps (BackdropSeamsTest's measure) and is opaque, sizes match the proposal, Source chunks, the
    deep pieces' edges transparent (BackdropAssetsTest's edge rule: the deep layer is the base here), every placement
    on the screen, at most two animated piece names on the screen at once (BackdropCheck's motion budget)."""
    problems = []
    pieces, placed, _ = load_proposal()
    a = np.array(Image.open(OUT / "lane-space.png").convert("RGBA"))
    if wrap_score(a) > 1.5:
        problems.append(f"lane-space: wrap score {wrap_score(a):.2f}")
    if (a[..., 3] < 255).any():
        problems.append("lane-space: not opaque")
    for name, spec in pieces.items():
        for im in piece_images(name, spec):
            if list(im.size) != list(spec["size"]):
                problems.append(f"{name}: {im.size} vs {spec['size']}")
                break
    for f in OUT.glob("*.png"):
        if Image.open(f).text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
    for name, spec in pieces.items():
        if spec["layer"] == "deep":
            for im in piece_images(name, spec):
                al = np.array(im)[..., 3] > 0
                if al[0].any() or al[-1].any() or al[:, 0].any() or al[:, -1].any():
                    problems.append(f"{name}: an opaque edge on the deep (base) layer")
    seen = set()
    worst = (0, 0.0, ())
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = set()
        for i, p in enumerate(placed):
            spec = pieces[p["piece"]]
            f = FACTORS[spec["layer"]]
            y = SCREEN - (scroll(p["t"]) * f + SCREEN / 2 - scroll(t) * f)
            if y + spec["size"][1] / 2 > 0 and y - spec["size"][1] / 2 < SCREEN:
                seen.add(i)
                if spec.get("frames"):
                    animated.add(p["piece"])
        if len(animated) > worst[0]:
            worst = (len(animated), t, tuple(sorted(animated)))
    if worst[0] > 2:
        problems.append(f"t={worst[1]:.1f}: {worst[0]} animated pieces on the screen: {worst[2]}")
    for i, p in enumerate(placed):
        if i not in seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"{len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}; {len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        pc, pl = build_all()
        write_proposal(pc, pl)
    if "--proposal" not in args:
        review()
