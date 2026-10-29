#!/usr/bin/env python3
"""Production art: Level 35's props (design/campaign/act-5-the-belt/level-35-iron-sovereign; M9 part H batch, concept
round 68, step ART-H2): the turned mass relay's line and flash (the belt's hazard, the README's *Layout*) in two
variants for **QH-2**, and the Ceres garrison lifeboat (the secret). The CDF beacon is Level 33's ``nav-buoy``
(tools/art/belt_units_f.py), reused unchanged.

**QH-2** (pending round 68; **a** is what the game uses until the pick):
  a (recommended)  a thin red line across the whole field from a glowing point on the right edge, brightening over the
                   1.2 s telegraph; at the flash a white-hot 16 px bar along the whole line for its 0.2 s, a short
                   full-screen flash, then a fading ion trail
  b                the same line; the flash a travelling slug crossing the field right -> left in the 0.2 s, the bar
                   lit behind it, then the same trail (reads as a shot, harder to read as "the whole row")

Outputs (build/art-held/sprites/ until the game claims them; ``--assets`` writes the relay into assets/sprites/ once
H4's ``RelayLooks`` names it in SpriteUse; the lifeboat follows tools/art/l23kit.py's rule: assets/ once Level 35's
data names ``ceres-lifeboat`` as a ground target's sprite):
  mass-relay-glow_0..3     32x32 additive: the relay's point of light on the right edge (the line's origin), 0 -> 3
                           brightening over the telegraph (drawn centred on x 476 of the field, the line's row)
  mass-relay-line_0..3     32x6 additive: the telegraph line's strip, tiled across the field (480 / 32 = 15 copies),
                           centred on the locked row; 0 -> 3 brightening (the game picks by the telegraph's progress)
  mass-relay-bolt_0..1     32x24 additive: the white-hot bar (16 px core, a crimson fringe), tiled across the field
                           for the 0.2 s live (two frames alternating at 20 fps)
  mass-relay-trail_0..5    32x24 additive: the ion trail after the bolt, fading over 0.6 s (10 fps), tiled
  mass-relay-slug_0..3     96x24 additive (variant b): the slug's head (white-hot, a crimson wake to its right),
                           crossing right -> left in 0.2 s (its nose at x = 480 - 2400 t); the bolt strip is drawn only
                           to the right of the nose, then the trail as a
  ceres-lifeboat_0..3      64x48: the Ceres garrison lifeboat lying in the station's clamps (Coalition white-grey,
                           a rescue-orange band, CDF blue stripe, portholes, the docking collar, the stores hatch), its
                           beacon blinking (1, 0.5, 0, 0: 4 fps)
  ceres-lifeboat-open_0..5 64x48: shot open (the trigger's 20 HP gone): the hatch blown, panels flung out in vacuum, the
                           stores' light inside (the crate is the pickup's own sprite), then the burnt hull that stays
  pivots/mass-relay.json   the strips' tiling, the timings (telegraph, live, trail), the flash's colour and alpha curve,
                           the glow's place, variant b's slug speed
Review: design/campaign/act-5-the-belt/level-35-iron-sovereign/concept/relay-flash-final-r68-a.png/.gif (variant a),
relay-flash-final-r68-b.png/.gif (variant b), lifeboat-final-r68-a.png (the boat in its clamps on the hull at 1x and
2x, the blink and the opening).

Run: python3 tools/art/l35_props.py [relay] [lifeboat] [--assets] [--review] [--check]   (about 20 s)
"""
import json
import sys

import numpy as np
from PIL import Image, ImageDraw

import artkit
import l23kit
from artkit import DESIGN, ROOT, TAU

import belt_units as bu  # noqa: E402  (tools/art: the renderer)
import belt_units_c as buc  # noqa: E402  (tools/art: additive field frames, scorch)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_x, sd_sphere, union  # noqa: E402

SCRIPT = "l35_props.py"
BATCH = "M9 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r68"
CONCEPT = DESIGN / "campaign" / "act-5-the-belt" / "level-35-iron-sovereign" / "concept"
BACKDROP = ROOT / "assets" / "backdrop" / "level-35"
W, SCREEN = 480, 540
CRIMSON = np.array((255, 40, 36.0))
WHITE_HOT = np.array((255, 244, 230.0))
TELEGRAPH, LIVE, TRAIL = 1.2, 0.2, 0.6
SLUG_SPEED = 2400.0                      # px/s: the 480 px field in 0.2 s
FLASH = {"colour": [255, 236, 222], "alpha": [[0.0, 0.55], [0.05, 0.4], [0.15, 0.0]]}   # [s after the flash, alpha]
GLOW_X = 476
ASSETS = "--assets" in sys.argv[1:]


def _write(name, frames, single=False):
    if name.startswith("mass-relay"):
        target = l23kit.ASSETS_SPRITES if ASSETS else l23kit.HELD
        target.mkdir(parents=True, exist_ok=True)
        other = l23kit.HELD if ASSETS else l23kit.ASSETS_SPRITES
        for old in other.glob(f"{name}_*.png"):
            old.unlink()
        before = artkit.SPRITES
        artkit.SPRITES = target
        try:
            artkit.write_frames(name, frames, SOURCE, single=single)
        finally:
            artkit.SPRITES = before
        return target
    l23kit.write(name, frames, SOURCE, single=single)
    return l23kit.ASSETS_SPRITES if l23kit.claimed(name) else l23kit.HELD


def load(name):
    return l23kit.load(name)


# =========================================================================== the relay (QH-2)

def _additive(rgb, alpha=None):
    a = np.clip(rgb, 0, 255)
    al = np.where(a.max(axis=-1) >= 1, 255, 0) if alpha is None else alpha
    return Image.fromarray(np.dstack([a, al]).astype(np.uint8), "RGBA")


def relay_glow(i):
    """The relay's point of light: a crimson star with a white core, brightening (k 0.3 .. 1)."""
    k = (0.3, 0.55, 0.8, 1.0)[i]
    x, y = buc.grid(32)
    r, a = np.hypot(x, y), np.arctan2(y, x)
    f = np.exp(-(r / (2.0 + 2.5 * k)) ** 2) * 1.2 * k + np.clip(np.cos(a * 2) ** 30, 0, 1) * np.exp(-(r / 12) ** 2) * k
    f += np.exp(-(r / 9) ** 2) * 0.35 * k
    return buc.field_img(f, CRIMSON, white=0.75)


def relay_line(i, w=32, h=6):
    """The telegraph line's strip: a 1 px crimson core with a faint halo row each side, brightening; a slow ripple."""
    k = (0.35, 0.55, 0.78, 1.0)[i]
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    dy = np.abs(yy - h / 2)
    f = (np.where(dy < 0.6, 1.0, 0.0) + np.where((dy >= 0.6) & (dy < 1.6), 0.35, 0.0)
         + np.where((dy >= 1.6) & (dy < 2.6), 0.12, 0.0)) * k * (0.9 + 0.1 * np.sin(xx / w * TAU * 2))
    rgb = CRIMSON[None, None] * f[..., None] + 255 * np.clip(f - 0.85, 0, 1)[..., None] * 1.5
    return _additive(rgb)


def relay_bolt(i, w=32, h=24):
    """The bolt along the line: a white-hot 16 px core, a crimson fringe 4 px each side; a flicker along it."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    dy = np.abs(yy - h / 2 - 0.4 * np.sin(xx / w * TAU * 2 + i * 2.1))
    core = np.clip(8.0 - dy, 0, 1)
    fringe = np.clip(12.0 - dy, 0, 1) * (1 - core)
    lum = (0.88 + 0.12 * np.sin(xx / w * TAU * 3 + i * 1.3))[..., None]
    rgb = WHITE_HOT * core[..., None] * lum + CRIMSON * fringe[..., None] * 0.9
    rgb = np.where((dy > 6.5)[..., None] & (dy < 8)[..., None], rgb * 0.88 + CRIMSON * 0.12, rgb)
    return _additive(rgb)


def relay_trail(i, w=32, h=24, n=6):
    """The ion trail: the bar's place glowing crimson-violet, narrowing and breaking up as it fades."""
    u = (i + 1) / n
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    noise = raster.fbm(w, h, 8, 3581 + i, octaves=2, period=True)
    width = 7 * (1 - 0.6 * u)
    dy = np.abs(yy - h / 2 - (noise - 0.5) * 3 * u)
    f = np.exp(-(dy / width) ** 2) * (1 - u) ** 1.3 * (0.7 + 0.6 * noise)
    f = artkit.ordered_dither(np.clip(f, 0, 1), 6)
    rgb = np.array((230, 50, 90.0))[None, None] * f[..., None] + 255 * np.clip(f - 0.75, 0, 1)[..., None]
    return _additive(rgb)


def relay_slug(i, w=96, h=24):
    """Variant b: the slug's head (nose on the left, x 4), white-hot and 16 px tall, its crimson wake to the right."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float) + 0.5
    nose = 6.0
    ry = 8.5 - 0.8 * np.sin(i * 1.7)
    body = ((xx - nose) / 26) ** 2 + ((yy - h / 2) / ry) ** 2
    head = np.clip(1.4 - body, 0, 1) * (xx > nose - 2)
    wake = np.clip(1 - np.abs(yy - h / 2) / (6 + 0.05 * (xx - nose)), 0, 1) * np.clip((xx - nose) / 10, 0, 1) * \
        np.clip(1 - (xx - nose - 10) / (w - nose - 10), 0, 1)
    rgb = WHITE_HOT * np.clip(head * 1.4, 0, 1)[..., None] + CRIMSON * (wake * (1 - np.clip(head, 0, 1)))[..., None]
    return _additive(rgb)


def build_relay():
    sets = {"mass-relay-glow": [relay_glow(i) for i in range(4)],
            "mass-relay-line": [relay_line(i) for i in range(4)],
            "mass-relay-bolt": [relay_bolt(i) for i in range(2)],
            "mass-relay-trail": [relay_trail(i) for i in range(6)],
            "mass-relay-slug": [relay_slug(i) for i in range(4)]}
    for name, frames in sets.items():
        target = _write(name, frames)
    pivots = (ROOT / "build" / "art-held" / "pivots") if target == l23kit.HELD else artkit.PIVOTS
    pivots.mkdir(parents=True, exist_ok=True)
    data = {"source": SOURCE, "variant": "a",
            "line": {"strip": "mass-relay-line", "tile_w": 32, "rows": 6, "frames_by_progress": 4},
            "glow": {"sprite": "mass-relay-glow", "x": GLOW_X, "frames_by_progress": 4},
            "bolt": {"strip": "mass-relay-bolt", "tile_w": 32, "rows": 24, "band": 16, "fps": 20, "seconds": LIVE},
            "trail": {"strip": "mass-relay-trail", "tile_w": 32, "rows": 24, "fps": 10, "seconds": TRAIL},
            "flash": FLASH,
            "b": {"slug": "mass-relay-slug", "nose_x": 6, "speed": SLUG_SPEED, "fps": 20,
                  "rule": "the bolt strip only right of the nose (x > 480 - speed * s), then the trail"},
            "telegraph": TELEGRAPH}
    (pivots / "mass-relay.json").write_text(json.dumps(data, indent=1) + "\n", encoding="utf-8")
    print(f"relay: {len(sets)} sets in {target.relative_to(ROOT)}")


def _tile_strip(dst, strip, y, x0=0, x1=W, alpha=1.0):
    """Adds a strip tiled from x0 to x1, centred on row y (premultiplied, as the game blends it)."""
    a = np.array(dst).astype(np.float64)
    s = np.array(strip).astype(np.float64)[..., :3] * alpha
    h = s.shape[0]
    top = int(round(y - h / 2))
    for x in range(0, W, strip.width):
        lo, hi = max(x, int(x0)), min(x + strip.width, int(x1))
        if hi <= lo:
            continue
        ya, yb = max(0, top), min(a.shape[0], top + h)
        a[ya:yb, lo:hi, :3] += s[ya - top:yb - top, lo - x:hi - x]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def relay_overlay(img, t, row, variant="a", t0=59.8, flash_t=61.0):
    """The relay drawn over a screen at level time t (telegraph from t0, flash at flash_t), on screen row ``row``."""
    img = img.copy()
    if t < t0 or t > flash_t + LIVE + TRAIL:
        return img
    glow, line = load("mass-relay-glow"), load("mass-relay-line")
    if t < flash_t:
        u = (t - t0) / (flash_t - t0)
        k = min(3, int(u * 4))
        img = _tile_strip(img, line[k], row)
        return l23kit.add_c(img, glow[k], GLOW_X, row)
    s = t - flash_t
    bolt, trail = load("mass-relay-bolt"), load("mass-relay-trail")
    if s < LIVE:
        x0 = 0 if variant == "a" else max(0.0, W - SLUG_SPEED * s)
        if variant == "b":
            img = _tile_strip(img, line[3], row, 0, x0)
        img = _tile_strip(img, bolt[int(s * 20) % 2], row, x0, W)
        if variant == "b":
            img = l23kit.add_c(img, load("mass-relay-slug")[int(s * 20) % 4], x0 - 6 + 48, row)
        img = l23kit.add_c(img, glow[3], GLOW_X, row)
    else:
        k = min(5, int((s - LIVE) / TRAIL * 6))
        img = _tile_strip(img, trail[k], row)
    a = 0.0
    pts = FLASH["alpha"]
    if variant == "a":
        for (s0, a0), (s1, a1) in zip(pts, pts[1:]):
            if s0 <= s < s1:
                a = a0 + (a1 - a0) * (s - s0) / (s1 - s0)
    if a > 0:
        over = Image.new("RGBA", img.size, tuple(FLASH["colour"]) + (int(a * 255),))
        img.alpha_composite(over)
    return img


# =========================================================================== the Ceres lifeboat

BOAT = (64, 48)
BOAT_PX = 20.0


def boat_mats(beacon=1.0, inside=0.0):
    return [Material((0.8, 0.8, 0.78), metal=0.3, shininess=50, spec=0.5),           # 0 Coalition white-grey hull
            Material((0.86, 0.4, 0.1), metal=0.2, shininess=40, spec=0.4),            # 1 the rescue-orange band
            Material((0.3, 0.32, 0.36), metal=0.6, shininess=60, spec=0.6),           # 2 steel: collar, hatch frame
            Material((0.05, 0.07, 0.1), metal=0.0, shininess=120, spec=1.2),          # 3 the portholes
            Material((0.1, 0.06, 0.02), emission=(1.0 * (0.15 + 1.5 * beacon), 0.5 * (0.15 + 1.5 * beacon), 0.1)),
            Material((0.04, 0.04, 0.05), shininess=5, spec=0.0),                       # 5 the open interior
            Material((0.05, 0.04, 0.02), emission=(1.2 * inside, 0.8 * inside, 0.3 * inside)),   # 6 the stores' light
            Material((0.16, 0.28, 0.56), metal=0.3, shininess=40, spec=0.4)]          # 7 the CDF blue stripe


def boat_scene(broken=0.0):
    """The lifeboat lying across (+X along it, Z up): a pressure capsule with white caps, an orange band round its
    middle, a CDF blue stripe, a docking collar at its left end, four portholes along its top, the stores hatch and the
    beacon on a short mast. ``broken`` 0 -> 1: the hatch thrown off, the hull opened, the stores' light inside."""
    def scene(p):
        z = 0.62
        items = [(sd_capsule(p, (-1.05, 0, z), (1.05, 0, z), 0.56), 0),
                 (sd_cylinder_x(p, (0.0, 0, z), 0.585, 0.26), 1),
                 (sd_cylinder_x(p, (0.62, 0, z), 0.575, 0.05), 7),
                 (sd_cylinder_x(p, (-1.42, 0, z), 0.34, 0.08), 2),
                 (sd_cylinder_x(p, (-1.5, 0, z), 0.26, 0.04), 2)]
        for x in (-0.75, -0.45, 0.45, 0.75):
            items.append((sd_sphere(p, (x, -0.2, z + 0.47), 0.085), 3))
        if broken < 1:
            lift = broken * 0.9
            items.append((sd_box(p, (0.0, 0.12, z + 0.55 + lift), (0.2, 0.16, 0.04), 0.02), 2))
        if broken:
            items.append((sd_box(p, (0.0, 0.12, z + 0.5), (0.18 + 0.1 * broken, 0.15 + 0.06 * broken, 0.12), 0.02), 5))
            items.append((sd_box(p, (0.0, 0.12, z + 0.42), (0.1, 0.08, 0.06), 0.02), 6))
        items += [(sd_capsule(p, (0.92, 0.18, z + 0.4), (0.92, 0.18, z + 0.62), 0.025), 2),
                  (sd_sphere(p, (0.92, 0.18, z + 0.66), 0.065), 4)]
        return union(*items)
    return scene


def _boat(beacon, broken):
    return bu.render(boat_scene(broken), boat_mats(beacon, broken), BOAT, BOAT[0] / BOAT_PX, crisp=70)


def boat_burst(img, i, n=6):
    """The lifeboat shot open in vacuum: panels flung outward, a short grey ring (2D over the broken render)."""
    out = img.copy()
    u = (i + 1) / n
    d = ImageDraw.Draw(out)
    rng = np.random.default_rng(3583)
    cx, cy = BOAT[0] / 2, BOAT[1] / 2
    if u < 0.9:
        rr = 6 + 18 * u
        d.ellipse([cx - rr, cy - rr * 0.8, cx + rr, cy + rr * 0.8], outline=(170, 160, 150, int(170 * (1 - u))), width=2)
    for k in range(9):
        a = k * TAU / 9 + rng.uniform(-0.3, 0.3)
        dist = (6 + 18 * u) * rng.uniform(0.7, 1.1)
        px, py = cx + np.cos(a) * dist, cy + np.sin(a) * dist * 0.8
        if u > 0.85 and k % 2:
            continue
        s = rng.uniform(1.6, 3.0)
        spin = a + 3 * u
        pts = [(px + np.cos(spin + j * TAU / 4) * s * (1.4 if j % 2 else 0.6), py + np.sin(spin + j * TAU / 4) * s)
               for j in range(4)]
        col = (220, 110, 40, 255) if k % 3 == 0 else (206, 204, 198, 255) if np.cos(a - 3.9) > 0 else (110, 110, 106, 255)
        d.polygon(pts, fill=col)
    return out


def build_lifeboat():
    from concurrent.futures import ProcessPoolExecutor
    jobs = [(b, 0.0) for b in (1.0, 0.5, 0.0)] + [(0.0, u) for u in (0.35, 0.7, 1.0)]
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(_boat_job, jobs))
    shut = artkit.quantize_set([res[0], res[1], res[2], res[2]], 32)      # a loyalist boat: no Ascendancy rim
    t1 = _write("ceres-lifeboat", shut)
    broken = list(res[3:])
    opened = [boat_burst(broken[0], 0), boat_burst(broken[1], 1), boat_burst(broken[2], 2), boat_burst(broken[2], 3),
              buc.scorch(boat_burst(broken[2], 4), 3585, 0.7), buc.scorch(broken[2], 3587, 0.6)]
    _write("ceres-lifeboat-open", artkit.quantize_set(opened, 32))
    print(f"lifeboat: 2 sets in {t1.relative_to(ROOT)}")


def _boat_job(job):
    return _boat(*job)


# =========================================================================== review, check

def _plate(w, h, kind="space", y0=0):
    name = {"space": "orbit-space", "hull": "sovereign-hull"}[kind]
    f = BACKDROP / f"{name}.png"
    if f.exists():
        src = Image.open(f).convert("RGBA")
        out = Image.new("RGBA", (w, h))
        for y in range(-y0, h, src.height):
            for x in range(0, w, src.width):
                out.alpha_composite(src, (x, y)) if y >= 0 else out.paste(src.crop((0, -y, src.width, src.height)),
                                                                          (x, 0))
        return out
    return Image.new("RGBA", (w, h), (12, 12, 18, 255))


def _ship(img, x, y):
    return l23kit.paste_c(img, artkit.load_frames("ship")[2], x, y)


def review_relay():
    for variant in ("a", "b"):
        times = [59.8, 60.4, 60.9, 61.0, 61.05, 61.12, 61.3, 61.6]
        labels = ["TELEGRAPH 0 S", "0.6 S", "1.1 S", "FLASH +0", "+0.05", "+0.12", "TRAIL +0.3", "TRAIL +0.6"]
        sw, sh = 240, 270
        sheet = raster.sheet(16 + 4 * (sw + 12), 70 + 2 * (sh + 26) + 120,
                             f"MASS RELAY FLASH (FINAL R68), VARIANT {variant.upper()}",
                             f"PRODUCTION ART, {BATCH.upper()} - R68 (QH-2)")
        desc = ("A: THE LINE BRIGHTENS, THE WHOLE ROW FLASHES WHITE-HOT WITH A SHORT SCREEN FLASH, AN ION TRAIL"
                if variant == "a" else "B: THE SAME LINE; THE FLASH A SLUG CROSSING RIGHT TO LEFT IN 0.2 S, THEN THE TRAIL")
        raster.draw_text(sheet, 16, 38, desc + " (HALF SIZE, THE SHIP ON THE LOCKED ROW)", raster.LABEL)
        base = _plate(W, SCREEN, "hull")
        for i, (t, lab) in enumerate(zip(times, labels)):
            scr = _ship(base.copy(), 240 + (40 if t > 61 else 0), 440)
            scr = relay_overlay(scr, t, 440, variant)
            x, y = 16 + (i % 4) * (sw + 12), 56 + (i // 4) * (sh + 26)
            sheet.alpha_composite(scr.resize((sw, sh), Image.BOX), (x, y))
            raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
        y = 56 + 2 * (sh + 26)
        raster.draw_text(sheet, 16, y, "THE STRIPS AT 2X: LINE 0-3, BOLT, TRAIL 0-5" + (", SLUG" if variant == "b" else ""),
                         raster.LABEL)
        x = 16
        sets = [("mass-relay-line", 4), ("mass-relay-bolt", 2), ("mass-relay-trail", 6)] + \
            ([("mass-relay-slug", 1)] if variant == "b" else []) + [("mass-relay-glow", 4)]
        for name, n in sets:
            for fr in load(name)[:n]:
                big = fr.resize((fr.width * 2, fr.height * 2), Image.NEAREST)
                cell = Image.new("RGBA", (big.width + 4, big.height + 4), (10, 12, 26, 255))
                cell = l23kit.add_c(cell, big, cell.width / 2, cell.height / 2)
                if x + cell.width > sheet.width - 16:
                    break
                sheet.alpha_composite(cell, (x, y + 18))
                x += cell.width + 6
        artkit.REVIEW_ROUND = ROUND
        png, gif = artkit.review_paths(CONCEPT, "relay-flash", variant)
        sheet.convert("RGB").save(png, optimize=True)
        frames = []
        for k in range(48):
            t = 59.6 + k * 0.05
            scr = _ship(base.copy(), 240 + 60 * np.sin(k * 0.08), 440)
            frames.append(relay_overlay(scr, t, 440, variant).resize((sw, sh), Image.BOX))
        size = bu.e60.rig.write_gif(frames, gif, fps=20, colors=128)
        print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def review_lifeboat():
    shut, opened = load("ceres-lifeboat"), load("ceres-lifeboat-open")
    clamps = BACKDROP / "lifeboat-clamps.png"
    sheet = raster.sheet(16 + 10 * 140, 70 + 2 * 130 + 40, "CERES LIFEBOAT (FINAL R68): THE SECRET ON THE HULL",
                         f"PRODUCTION ART, {BATCH.upper()} - R68")
    raster.draw_text(sheet, 16, 38, "IN THE STATION'S CLAMPS ON THE HULL TILE: THE BEACON'S BLINK (4 FRAMES), SHOT OPEN "
                                    "(6 FRAMES); 1X ABOVE, 2X BELOW", raster.LABEL)
    tile = _plate(480, 960, "hull")
    for zoom, y in ((1, 60), (2, 60 + 130)):
        for i, fr in enumerate(list(shut) + list(opened)):
            cell = tile.crop((150, 400, 150 + 120, 400 + 100))
            if clamps.exists():
                cell = l23kit.paste_c(cell, Image.open(clamps).convert("RGBA"), 60, 50)
            cell = l23kit.paste_c(cell, fr, 60, 50)
            if zoom == 2:
                cell = cell.crop((30, 25, 90, 75)).resize((120, 100), Image.NEAREST)
            sheet.alpha_composite(cell, (16 + i * 140, y))
    artkit.REVIEW_ROUND = ROUND
    png, _ = artkit.review_paths(CONCEPT, "lifeboat")
    sheet.convert("RGB").save(png, optimize=True)
    print(f"review: {png.relative_to(ROOT)}")


SETS = [("mass-relay-glow", 4, (32, 32)), ("mass-relay-line", 4, (32, 6)), ("mass-relay-bolt", 2, (32, 24)),
        ("mass-relay-trail", 6, (32, 24)), ("mass-relay-slug", 4, (96, 24)), ("ceres-lifeboat", 4, BOAT),
        ("ceres-lifeboat-open", 6, BOAT)]


def check():
    problems = []
    for name, n, size in SETS:
        try:
            frames = load(name)
        except FileNotFoundError:
            problems.append(f"{name}: missing")
            continue
        if len(frames) != n:
            problems.append(f"{name}: {len(frames)} frames, not {n}")
        for f in frames:
            if f.size != size:
                problems.append(f"{name}: {f.size} vs {size}")
        folder = l23kit.ASSETS_SPRITES if (l23kit.ASSETS_SPRITES / f"{name}_0.png").exists() else l23kit.HELD
        for k in range(n):
            if Image.open(folder / f"{name}_{k}.png").text.get(artkit.SOURCE_KEY) != SOURCE:
                problems.append(f"{name}_{k}: no Source chunk")
        print(f"{name}: {n} frames {size[0]}x{size[1]} in {folder.relative_to(ROOT)}")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"{len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--check" in sys.argv[1:]:
        sys.exit(0 if check() else 1)
    subjects = args or ["relay", "lifeboat"]
    if "--review" not in sys.argv[1:]:
        if "relay" in subjects:
            build_relay()
        if "lifeboat" in subjects:
            build_lifeboat()
    if "relay" in subjects:
        review_relay()
    if "lifeboat" in subjects:
        review_lifeboat()
