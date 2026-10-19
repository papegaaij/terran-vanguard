#!/usr/bin/env python3
"""Concept round 36 - the Act 3 gear looks, a/b proposals (overnight design phase, step m7ag).

Each subject gets a PNG sheet (parts at 2-4x, key frames, a frame from the play field at 1x)
and a GIF in the 480x540 play field over Martian ground with the Stormhawk for scale (the
Shield Overcharge GIF adds the right HUD panel, 720x540).

Outputs (the chosen variants of round 36's review (CHOSEN: ion beam a, light drone a, EMP a,
overcharge b) in concept/, the unchosen ones in concept/rejected/):
  design/player/weapons/concept/ion-beam-r36-{a,b}.{png,gif}
      the Ion Beam per level (6 -> 14 px, OD 18), first-target rule on air units, burning
      through a tendril and going on past it, stopped by a rock island with the rock-scorch
      impact (the unit beyond stays safe), passing over a hardened ground target with a glance
      spark, running off the top edge with nothing in its path.
      a: the chosen round-08/09 beam family (segmented cyan column, round-09 contact flare),
         dust-tinted flare and dust kick at walls.
      b: "ion stream": a hard white core in a pale sheath, two helix strands and bright packets
         running up the column, a ring splash at the contact, slag beads at walls.
  design/player/weapons/concept/light-drone-r36-{a,b}.{png,gif}
      the Light Drone Bay: bay pod on each wing, the 14x14 drone orbiting at 36 px (150 deg/s,
      clockwise, a pair opposite each other), firing at the convergence point 140 px ahead,
      absorbing enemy bullets, breaking after 8 hits (L1) and rebuilt by the bay after 6 s.
      a: "disc" drone (round hull, blue band, lens, short barrel) and a dock-ring bay.
      b: "dart" drone (small delta with twin engine glows) and a slim launch-rail bay.
  design/player/specials/concept/emp-burst-r36-{a,b}.{png,gif}
      the EMP Burst: the ring covers the field in 0.4 s; machines (Ghost Drones, the grey
      Warden Tank) stunned 3 s with their running lights out, Vrell (Needlers) 1.5 s with their
      glow dimmed; stunned air units drift on their line, bullets in flight are not cleared.
      a: thin blue-white ring with crackling arcs along it (the spec's look).
      b: "arc front": a broken double ring with arcs forking from it into each unit it reaches
         and a short blue-white field flicker.
  design/player/specials/concept/shield-overcharge-r36-{a,b}.{png,gif}
      Shield Overcharge (Mk III: 45 -> 90 for 8 s, then the excess bleeds away in 0.5 s) with the
      HUD: the shield bar's second capacity and the special row's cooldown ring (fills clockwise
      in dim cyan over 45 s, the effect arc drains anticlockwise over 8 s, READY flash).
      a: gold-white flare and gold shimmer, the bar rescaled to 0-90 with the second half gold
         (the spec's look).
      b: white-hot double shell, the bar kept at 0-45 with the second layer drawn as white
         cores inside the cyan cells.

Reuses the frozen render package, vfx_r08/vfx_r09 (beam family, contact flare, heat glow,
Canvas fields), enemies_r36 (Mars ground, canyon, Tendril Anchor, Ghost Drone, grey Warden Tank)
and the production sprites in assets/ (Stormhawk, wing pods, Needler, Hive Node, explosions,
shield ring, HUD fonts, the HUD panel from the round-28 capture). Deterministic.

Run: python3 tools/concept/gear_r36.py [ion-beam] [light-drone] [emp-burst] [shield-overcharge]
     [--variant a|b]   (no args = everything, a few minutes)
"""
import json
import zlib
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import vfx_r08 as v8  # noqa: E402
import vfx_r09 as v9  # noqa: E402
import enemies_r36 as r36  # noqa: E402  (sets the round-36 sheet subtitle on enemies_r05)
import enemies_r05 as r5  # noqa: E402
import enemies_r03 as e3  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.sdf import (mirror_x, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid,  # noqa: E402
                        sd_plate, sd_sphere, union)

Canvas, gauss, solid = v8.Canvas, v8.gauss, v8.solid
PS, WHITE, TAU = v8.PS, v8.WHITE, 2 * np.pi
FW, FH = 480, 540
FPS = 20
SFPS = 12.5                 # scrolling scenes: the GIF stays under 8 MB (enemies_r36's rate)
ASSETS = ROOT / "assets"
OUT = {"weapons": ROOT / "design" / "player" / "weapons" / "concept",
       "specials": ROOT / "design" / "player" / "specials" / "concept"}
smooth = r36.smooth
CYAN = (120, 200, 255)


# =========================================================================== shared

_A = {}


def asset(name):
    """A production sprite from assets/sprites (RGBA, cached)."""
    if name not in _A:
        _A[name] = Image.open(ASSETS / "sprites" / f"{name}.png").convert("RGBA")
    return _A[name]


def frames_of(name, n):
    return [asset(f"{name}_{i}") for i in range(n)]


PIV = json.loads((ASSETS / "pivots" / "ship.json").read_text())["points"]
PODS = json.loads((ASSETS / "pivots" / "pods.json").read_text())["offsets"]


def ship_parts(bank=2):
    pts = {k: v[bank] for k, v in PIV.items()}
    return asset(f"ship_{bank}"), pts


def draw_ship(img, x, y, t=0.0, bank=2, pods=None, bays=None, shadow=True):
    """The production Stormhawk centred at (x, y): shadow, engine flames (additive), the hull,
    production wing pods (``pods`` = pod type) or a drone bay sprite per wing (``bays``)."""
    sp, pts = ship_parts(bank)
    ox, oy = x - sp.width / 2, y - sp.height / 2
    if shadow:
        sh = sprite.shadow_of(sp, opacity=0.6, blur=1.0, scale=0.85)
        sprite.paste_center(img, sh, x + 21, y + 30)
    fl = asset(f"engine-flame_{int(t * 20) % 3 + 3}")
    for k in ("engine-left", "engine-right"):
        ex, ey = pts[k]
        v8.additive(img, fl, ox + ex, oy + ey + fl.height / 2 - 2)
    sprite.paste(img, sp, ox, oy)
    if pods:
        for side in ("left", "right"):
            p = asset(f"pod-{pods}-{side}_{bank}")
            px, py = PODS[f"pod-{pods}-{side}"][bank]
            sprite.paste(img, p, ox + px, oy + py)
    if bays:
        for side, bay in zip(("left", "right"), bays):
            if bay is None:
                continue
            wx, wy = pts[f"wing-{side}"]
            sprite.paste_center(img, bay, ox + wx + (2 if side == "left" else -2), oy + wy + 1)
    return ox, oy


def explosion(img, x, y, s, kind="small"):
    """Production explosion (``explosion-<kind>``, 12 frames, additive) at progress s."""
    if not 0 <= s < 1:
        return
    fr = frames_of(f"explosion-{kind}", 12)
    v8.additive(img, fr[min(11, int(s * 12))], x, y)


def flash_sprite(sp, k=0.6):
    return v9.tint_sprite(sp, WHITE, k)


def dim(sp, k):
    a = np.array(sp).astype(np.float64)
    a[..., :3] *= k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def glow_line(img, pts, col, width=1, glow=4, alpha=1.0, core=WHITE):
    """Light line: a blurred halo in ``col`` and a thin core (arcs, sparks)."""
    if len(pts) < 2:
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    ImageDraw.Draw(lay).line(pts, fill=tuple(col) + (int(200 * alpha),), width=width + 3)
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(glow / 2)))
    d = ImageDraw.Draw(img)
    d.line(pts, fill=tuple(col) + (int(255 * alpha),), width=width + 1)
    d.line(pts, fill=tuple(core) + (int(255 * alpha),), width=width)


def jagged(p0, p1, rng, depth=4, rough=0.35):
    """Lightning polyline from p0 to p1 by midpoint displacement."""
    pts = [np.asarray(p0, float), np.asarray(p1, float)]
    for _ in range(depth):
        nxt = [pts[0]]
        for a, b in zip(pts[:-1], pts[1:]):
            v = b - a
            n = np.array((-v[1], v[0]))
            m = (a + b) / 2 + n * rng.normal(0, rough * 0.5)
            nxt += [m, b]
        pts = nxt
        rough *= 0.6
    return [tuple(p) for p in pts]


def label(img, x, y, text, color=raster.LABEL_DIM):
    raster.draw_text(img, int(x), int(y), text, color)


def enlarge(img, k):
    return sprite.enlarge(img, k)


def crop_at(img, x, y, w, h):
    x0, y0 = int(round(x - w / 2)), int(round(y - h / 2))
    return img.crop((x0, y0, x0 + w, y0 + h))


# the user's picks (round 36's review, 2026-10-13): the unchosen variants are written to concept/rejected/
CHOSEN = {"ion-beam": "a", "light-drone": "a", "emp-burst": "a", "shield-overcharge": "b"}


def save(part, slug, variant, sheet, frames, fps=FPS, colors=128):
    folder = OUT[part] if CHOSEN[slug] == variant else OUT[part] / "rejected"
    folder.mkdir(parents=True, exist_ok=True)
    png = folder / f"{slug}-r36-{variant}.png"
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, folder / f"{slug}-r36-{variant}.gif", fps=fps, colors=colors)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)")


def scroll_bg(make, speed, seconds, seed):
    """A tall ground strip for a scrolling scene and the crop for time t."""
    h = FH + int(speed * seconds) + 8
    bg = make(seed, h)
    top0 = h - FH

    def at(t):
        return bg.crop((0, int(round(top0 - speed * t)), FW, int(round(top0 - speed * t)) + FH))
    return bg, top0, at


class BMFont:
    """The game's bitmap HUD fonts (assets/fonts/*.fnt, BMFont text format), tinted."""

    def __init__(self, name):
        fnt = (ASSETS / "fonts" / f"{name}.fnt").read_text().splitlines()
        self.page = Image.open(ASSETS / "fonts" / f"{name}.png").convert("RGBA")
        self.chars = {}
        for line in fnt:
            if line.startswith("char "):
                kv = dict(f.split("=") for f in line.split()[1:])
                self.chars[int(kv["id"])] = {k: int(v) for k, v in kv.items() if k != "id"}
            elif line.startswith("common "):
                kv = dict(f.split("=") for f in line.split()[1:] if "=" in f)
                self.line = int(kv["lineHeight"])

    def width(self, text, spacing=0):
        return sum(self.chars.get(ord(c), self.chars[32])["xadvance"] + spacing for c in text)

    def draw(self, img, x, y, text, color, spacing=0):
        for c in text:
            g = self.chars.get(ord(c), self.chars[32])
            if g["width"]:
                gl = self.page.crop((g["x"], g["y"], g["x"] + g["width"], g["y"] + g["height"]))
                tint = Image.new("RGBA", gl.size, tuple(color[:3]) + (0,))
                tint.putalpha(gl.getchannel("A"))
                img.alpha_composite(tint, (int(x + g["xoffset"]), int(y + g["yoffset"])))
            x += g["xadvance"] + spacing
        return x


# =========================================================================== 1 Ion Beam

LEVEL_W = [("L1", 6), ("L2", 8), ("L3", 10), ("L4", 12), ("L5", 14), ("OD", 18)]
BEAM_W = 8                  # the GIF flies the beam at L2 (bought at the L18 visit)
DUST = (196, 160, 128)


def column_a(top, bottom, w, t, taper=6):
    """Variant a: the chosen round-08/09 column (v9.beam_segment) at the level's width."""
    return v9.beam_segment(top, bottom, width=w / 2, t=t, taper=taper)


def column_b(top, bottom, w, t, taper=6):
    """Variant b, "ion stream": a hard white core in a pale translucent sheath, two thin helix
    strands twisting round it and bright packets running up the column (no segment stripes)."""
    hw = w / 2
    h = max(int(bottom - top), 2)
    cv = Canvas(int(hw * 5 + 10), h)
    cx = cv.w / 2
    dx = np.abs(cv.x - cx)
    y = cv.y
    Y = y + top
    tp = np.clip(y / max(taper, 1), 0, 1) if taper else np.ones_like(y)
    k = 0.5 + 0.5 * tp
    cv.add(PS[1], gauss(dx, hw * 2.1 * k) * 0.5)
    cv.over(PS[3], solid(dx, hw * 0.9 * k) * 0.3)
    ph = (Y + t * 640) / 30.0 * TAU
    for s in (0.0, np.pi):
        sx = cx + np.sin(ph + s) * hw * 0.9 * k
        front = 0.55 + 0.45 * np.cos(ph + s)
        cv.add(PS[2], gauss(np.abs(cv.x - sx), 0.75) * (0.5 + 0.9 * front))
        cv.add(WHITE, gauss(np.abs(cv.x - sx), 0.4) * front * 0.5)
    pk = ((Y + t * 980) % 52) / 52.0
    cv.add(WHITE, gauss(pk - 0.5, 0.07) * solid(dx, hw * 0.75 * k) * 0.55)
    cv.over(WHITE, solid(dx, max(hw * 0.32, 0.8) * (0.7 + 0.8 * (1 - tp))))
    if taper:
        cv.add(WHITE, gauss(dx, hw * 1.1) * (1 - tp) * 1.2)
    return cv.image()


COLUMN = {"a": column_a, "b": column_b}
_FL = {}


def flares(v, scale):
    """Contact flare loop (6 frames, centre at (32, 22) of a 64x52 image)."""
    key = (v, scale)
    if key not in _FL:
        fr = v9.contact_frames(scale=scale) if v == "a" else \
            v9.contact_frames(n=6, seed=47, power=0.75, scale=scale * 0.85)
        if v == "b":                    # ring splash: two flattened rings spreading out
            out = []
            for i, f in enumerate(fr):
                cv = Canvas(f.width, f.height)
                for kk in range(2):
                    ph = (i / 6 + kk * 0.5) % 1.0
                    e = cv.ell(32, 22, (3 + ph * 15) * scale, (1.2 + ph * 4.5) * scale)
                    cv.add(PS[3], gauss(e - 1, 0.12) * (1 - ph) ** 1.2 * 1.1)
                g = f.copy()
                v8.additive(g, cv.image(), f.width / 2, f.height / 2)
                out.append(g)
            fr = out
        _FL[key] = fr
    return _FL[key]


def dusty(fr, k=0.45):
    """The flare tinted with dust for the rock-scorch impact (the white core stays)."""
    a = np.array(fr).astype(np.float64)
    lum = a[..., :3].mean(axis=-1, keepdims=True) / 255
    keep = np.clip((lum - 0.75) / 0.25, 0, 1)
    a[..., :3] = a[..., :3] * (1 - k * (1 - keep)) + np.array(DUST) * k * (1 - keep)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def glance(img, x, y, t, seed=0):
    """Hardened ground target under the beam: a faint glance spark at the crossing."""
    rng = np.random.default_rng(int(t * FPS) * 7 + seed)
    raster.add_light(img, x, y, 7, CYAN, 0.45)
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for _ in range(4):
        a = rng.uniform(0, TAU)
        r0, r1 = rng.uniform(4, 6), rng.uniform(8, 13)
        d.line([(x + np.cos(a) * r0, y + np.sin(a) * r0 * 0.6),
                (x + np.cos(a) * r1, y + np.sin(a) * r1 * 0.6)], fill=(200, 235, 255, 200))
    img.alpha_composite(lay)


def rock_island(bg, cx, cy, seed=5):
    """Paint a rock island (a small mesa: lit flat top, a layered cliff band, a cast shadow to
    the lower right) onto the canyon strip; returns its collision mask (the footprint polygon
    at scale 1.0, as M2 = a proposes)."""
    rng = np.random.default_rng(seed)
    n = 26
    ang = np.linspace(0, TAU, n, endpoint=False)
    rad = 1 + 0.12 * rng.normal(size=n)
    rad = np.convolve(np.r_[rad[-2:], rad, rad[:2]], np.ones(5) / 5, mode="valid")

    def poly(k, dx=0.0, dy=0.0):
        return [(cx + dx + np.cos(a) * 58 * r * k, cy + dy + np.sin(a) * 46 * r * k)
                for a, r in zip(ang, rad)]

    def fill(pts):
        m = Image.new("L", bg.size, 0)
        ImageDraw.Draw(m).polygon(pts, fill=255)
        return np.array(m) > 0
    mask = fill(poly(1.0))
    top = fill(poly(0.8, -4, -7))                  # the plateau, offset up-left (height)
    a = np.array(bg).astype(np.float64)
    sh = np.roll(np.roll(mask, 10, axis=0), 12, axis=1) & ~mask      # cast shadow
    a[sh, :3] *= 0.5
    yy, xx = np.mgrid[0:bg.height, 0:bg.width]
    noise = raster.fbm(bg.width, bg.height, 18, seed + 3, octaves=3, period=False)
    src = a[..., :3].copy()
    grey = src.mean(axis=-1, keepdims=True)
    strata = 0.8 + 0.14 * np.sin((yy * 1.0 + noise * 9) / 3.0) + 0.1 * noise
    d = np.hypot((xx - cx) / 58.0, (yy - cy) / 46.0) + 1e-6
    facing = ((xx - cx) / 58.0 * -0.6 + (yy - cy) / 46.0 * -0.8) / d     # towards the light
    cliff = (src * 0.55 + grey * 0.45) * (strata * (0.55 + 0.4 * facing))[..., None]
    plateau = src * (1.12 + 0.12 * noise)[..., None] + 8
    rgb = np.where(top[..., None], plateau, cliff)
    a[..., :3] = np.where(mask[..., None], rgb, a[..., :3])
    rim = top & ~np.roll(np.roll(top, 2, 0), 2, 1)
    a[rim, :3] = np.minimum(a[rim, :3] * 1.3 + 16, 255)
    lip = top & ~np.roll(np.roll(top, -2, 0), -2, 1)
    a[lip, :3] *= 0.7
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    ys, xs = np.nonzero(mask)
    box = (xs.min() - 14, ys.min() - 14, xs.max() + 16, ys.max() + 16)
    part = img.crop(box).convert("RGB").filter(ImageFilter.GaussianBlur(0.7)) \
        .quantize(14, dither=Image.Dither.NONE).convert("RGBA")
    m = Image.fromarray((mask[box[1]:box[3], box[0]:box[2]] * 255).astype(np.uint8), "L")
    img.paste(part, box[:2], m)
    return img, mask


def scorch_marks(img, mask, top, marks):
    """Scorch on the rock edge: darken rock pixels round each mark (world x, y, age 0..1);
    fresh marks keep a faint cyan glow. Clipped to the rock."""
    a = np.array(img).astype(np.float64)
    t0 = int(round(top))
    m = mask[t0:t0 + FH]
    for wx, wy, fresh in marks:
        x, y = int(round(wx)), int(round(wy - t0))
        x0, x1, y0, y1 = max(0, x - 7), min(FW, x + 8), max(0, y - 7), min(FH, y + 4)
        if x1 <= x0 or y1 <= y0:
            continue
        yy, xx = np.mgrid[y0:y1, x0:x1]
        g = np.exp(-(((xx - x) / 4.5) ** 2 + ((yy - y) / 3.0) ** 2)) * m[y0:y1, x0:x1]
        a[y0:y1, x0:x1, :3] *= (1 - 0.45 * g)[..., None]
        a[y0:y1, x0:x1, :3] += (0.5 * fresh * g ** 2)[..., None] * np.array((90, 170, 230))
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def wall_stop(mask, top, bx, hw, muzzle):
    """Screen y where the beam (centre bx, half-width hw) meets the rock, or None."""
    best = None
    for x in range(int(bx - hw), int(bx + hw) + 1):
        if not 0 <= x < mask.shape[1]:
            continue
        col = mask[int(top):int(top) + FH, x]
        rows = np.nonzero(col[:int(muzzle)])[0]
        if len(rows):
            best = rows.max() if best is None else max(best, rows.max())
    return best


def sprite_stop(sp, sx, sy, bx, hw):
    """Lowest opaque row (screen y) of a sprite centred at (sx, sy) inside the beam column."""
    left, top = int(round(sx - sp.width / 2)), int(round(sy - sp.height / 2))
    best = None
    for x in range(int(bx - hw), int(bx + hw) + 1):
        e = v9.bottom_edge(sp, x - left)
        if e is not None:
            best = top + e if best is None else max(best, top + e)
    return best


# timeline (s): two Ghost Drones, a tendril with a Needler beyond, the rock island with a
# Needler beyond, a Hive Node (hardened) and the open sky
B_T = 7.8
B_SPEED = 50.0
B_ON = [(0.45, 2.45), (2.75, 3.95), (4.45, 5.75), (6.15, 7.4)]
G_DIE = (1.35, 2.3)
TEND_CUT = 2.75 + 40 / 60        # 40 HP at L2 (60 per s)
N1_DIE = 3.75
ISL = (330, 190, 5.0)            # centre x, screen y at time t
TEN = (170, 2.75)                # screen y at time t
HIVE = (150, 330, 6.6)


def ship_x(t):
    keys = [(0, 200), (2.45, 200), (2.75, 222), (3.95, 222), (4.45, 330), (5.1, 312),
            (5.75, 346), (6.15, 150), (7.4, 150), (B_T, 160)]
    for (t0, x0), (t1, x1) in zip(keys[:-1], keys[1:]):
        if t0 <= t <= t1:
            return x0 + (x1 - x0) * smooth((t - t0) / max(t1 - t0, 1e-6))
    return keys[-1][1]


def beam_state(t):
    """(on, alpha, length factor): 0.05 s to full length, 0.08 s fade after release."""
    for t0, t1 in B_ON:
        if t0 <= t < t1:
            return True, 1.0, min(1.0, (t - t0) / 0.05)
        if t1 <= t < t1 + 0.08:
            return True, 1 - (t - t1) / 0.08, 1.0
    return False, 0.0, 0.0


def ion_scene(v):
    bg, top0, _ = scroll_bg(r36.canyon, B_SPEED, B_T, 61)
    sy_at = lambda y, t0: y - B_SPEED * t0          # screen y at t=0 for one at y at time t0
    isl_y0 = top0 + sy_at(ISL[1], ISL[2])
    bg, mask = rock_island(bg, ISL[0], isl_y0)
    ten_w = top0 + sy_at(TEN[0], TEN[1])            # world y of the tendril pair
    hive_w = top0 + sy_at(HIVE[1], HIVE[2])
    gsp = r36.ghost["a"]
    needler = asset("needler_0")
    hive = frames_of("hive-node", 24)
    col = COLUMN[v]
    scorch = []                                     # (world x, world y, amount)
    frames, info = [], []
    py = 470
    for i in range(int(B_T * SFPS)):
        t = i / SFPS
        top = top0 - B_SPEED * t
        f = bg.crop((0, int(round(top)), FW, int(round(top)) + FH))
        if scorch:                                  # scorch marks stay on the rock
            f = scorch_marks(f, mask, top, [(x, y, max(0.0, 1 - (t - t0) / 0.8))
                                            for x, y, t0 in scorch])
        px = ship_x(t)
        on, alpha, grow = beam_state(t)
        muzzle = py - 22
        hw = BEAM_W / 2
        # ---- ground: hive node (hardened, never stops the beam)
        hy = hive_w - top
        hv = hive[int(t * 10) % 24]
        r5.paste(f, hv, HIVE[0], hy, "ground")
        # ---- tendril pair (anchors on the walls)
        a, b = r36.anchor_pts(ten_w)
        ay = a[1] - top
        ta, tb = (a[0] + r36.ANC_TIP, ay), (b[0] - r36.ANC_TIP, ay)
        u_cut = (222 - ta[0]) / (tb[0] - ta[0])
        if -60 < ay < FH + 60:
            if t >= TEND_CUT:
                s = smooth((t - TEND_CUT) / 0.7)
                for p0, p1, lnr in ((ta, tb, u_cut), (tb, ta, 1 - u_cut)):
                    ln = lnr * (1.0 - 0.8 * s)
                    end = (p0[0] + (p1[0] - p0[0]) * ln, p0[1] + (p1[1] - p0[1]) * ln + 6 * s)
                    pts, us = r36.tendril_path(p0, end, t, 3 + 8 * np.sin(np.pi * min(1, s * 1.4)),
                                               n=24, seed=1.7)
                    r36.draw_tendril(f, "a", pts, us, t, frame_seed=i + 5)
            else:
                pts, us = r36.tendril_path(ta, tb, t, 3.0, seed=1.7)
                heat = None
                if on and B_ON[1][0] <= t < TEND_CUT and alpha > 0.99:
                    heat = ((px - ta[0]) / (tb[0] - ta[0]), (t - B_ON[1][0]) / (TEND_CUT - B_ON[1][0]))
                r36.draw_tendril(f, "a", pts, us, t, heat=heat, frame_seed=i)
            for (x, y), head in ((a, 0.0), (b, np.pi)):
                sp = r36.anchors["a"].get(head, pulse=round((0.5 + 0.5 * np.sin(t * 5)) * 2) / 2)
                r5.paste(f, sp, x, y - top, "ground")
        # ---- air units: [sprite, x, y, alive-from, dies-at, name]
        units = []
        for k, (gx, gy, die) in enumerate(((200, 300, G_DIE[0]), (196, 228, G_DIE[1]))):
            yy = gy + 6 * np.sin(t * 1.7 + k)
            if t < die + 0.6:
                units.append(dict(sp=gsp.get(np.pi / 2, glow=1.0), x=gx + 4 * np.sin(t * 2 + k),
                                  y=yy, die=die, kind="small"))
        n1y = 40 + 25 * t if t < 2.4 else 100 + 4 * np.sin(t * 3)
        units.append(dict(sp=needler, x=222 + 5 * np.sin(t * 2.2), y=n1y, die=N1_DIE,
                          kind="small"))
        units.append(dict(sp=needler, x=ISL[0] - 4 + 30 * np.sin(t * 0.9), y=52 + 6 * np.sin(t * 2),
                          die=None, kind="small"))
        # ---- beam: first target nearest the ship
        stop, hit = None, None
        if on:
            ws = wall_stop(mask, top, px, hw, muzzle)
            if ws is not None:
                stop, hit = ws, "wall"
            for u in units:
                if u["die"] is not None and t >= u["die"]:
                    continue
                e = sprite_stop(u["sp"], u["x"], u["y"], px, hw)
                if e is not None and e < muzzle and (stop is None or e > stop):
                    stop, hit = e, u
        heat_t = 0.0
        for k, (t0, t1) in enumerate(B_ON):
            if t0 <= t < t1 + 0.1:
                heat_t = t - t0
        for u in units:
            sp = u["sp"]
            if u["die"] is not None and t >= u["die"]:
                explosion(f, u["x"], u["y"], (t - u["die"]) / 0.6)
                continue
            if hit is u:
                left, topy = u["x"] - sp.width / 2, u["y"] - sp.height / 2
                sp = v9.heat_glow(sp, px - left, stop - topy - v9.PEN, min(1.0, heat_t / 0.9))
                sp = v9.tint_sprite(sp, CYAN, 0.12 + 0.06 * np.sin(t * 40))   # steady heat tint
            r5.paste(f, sp, u["x"], u["y"], "air")
        imp = None
        if on:
            end = (stop - (v9.PEN if hit is not None and hit != "wall" else 0)) \
                if stop is not None else -4
            end = muzzle - (muzzle - end) * grow
            seg = col(end, muzzle, BEAM_W, t, taper=6 if stop is not None and grow >= 1 else 0)
            if alpha < 1:
                seg = v8.alpha_mul(seg, alpha)
            sprite.paste(f, seg, px - seg.width / 2, end)
            # crossings below the stop point
            if TEND_CUT > t >= B_ON[1][0] and -60 < ay < muzzle and (stop is None or ay > stop):
                fl = flares(v, 0.55)[i % 6]
                v8.additive(f, fl, px, ay + (fl.height / 2 - 22))
                imp = imp or ("tendril", px, ay)
            hx_ = hive_w - top
            if abs(px - HIVE[0]) < 26 and -40 < hx_ < muzzle and (stop is None or hx_ > stop):
                for gy in (hx_ - 8, hx_ + 6):
                    glance(f, px, gy, t, seed=int(gy))
                imp = imp or ("glance", px, hx_)
            if stop is not None and grow >= 1 and alpha > 0.5:
                if hit == "wall":
                    fl = dusty(flares(v, 0.8)[i % 6])
                    v8.additive(f, fl, px, stop + (fl.height / 2 - 22))
                    rock_kick(f, v, px, stop, t, i)
                    if i % 2 == 0:
                        scorch.append((px, stop + top - 2, t))
                    imp = ("wall", px, stop)
                else:
                    sz = hit["sp"].width
                    fl = flares(v, 0.55 if sz < 50 else 0.8)[i % 6]
                    v8.additive(f, fl, px, stop - v9.PEN + (fl.height / 2 - 22))
                    imp = ("air", px, stop - v9.PEN)
                    f = v9.shimmer(f, px, stop - v9.PEN, t)
            elif stop is None and grow >= 1:
                imp = imp or ("open", px, 20)
        draw_ship(f, px, py, t, pods="micro-missile")
        lab = "ION BEAM L2 (8 PX)" + ("  A: ROUND-08/09 FAMILY" if v == "a" else "  B: ION STREAM")
        label(f, 8, FH - 14, lab, (150, 200, 240))
        frames.append(f)
        info.append(imp)
    return frames, info


def rock_kick(img, v, x, y, t, i):
    """Rock-scorch extras: a: dust thrown back and sideways; b: slag beads dripping down."""
    rng = np.random.default_rng(i * 13 + (1 if v == "a" else 2))
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    if v == "a":
        for k in range(10):
            ph = ((t * 1.6 + k * 0.1) % 1.0)
            ang = np.radians(20 + 140 * ((k * 0.37) % 1))
            r = 6 + ph * 26
            cx, cy = x + np.cos(ang) * r * 1.2, y + np.sin(ang) * r * 0.7 + ph * 10
            a = int(150 * (1 - ph))
            rr = 2 + ph * 4
            d.ellipse([cx - rr, cy - rr, cx + rr, cy + rr], fill=DUST + (a // 2,))
        img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(0.8)))
        raster.add_light(img, x, y + 1, 12, (190, 220, 255), 0.5)
    else:
        for k in range(5):
            ph = ((t * 2.2 + k * 0.21) % 1.0)
            cx = x + (k - 2) * 3 + rng.uniform(-1, 1)
            cy = y + 3 + ph * ph * 30
            a = int(255 * (1 - ph))
            d.ellipse([cx - 1.2, cy - 1.2, cx + 1.2, cy + 1.2], fill=(220, 245, 255, a))
        img.alpha_composite(lay)
        raster.add_light(img, x, y + 2, 9, (170, 230, 255), 0.7)


def ion_sheet(v, frames, info):
    col = COLUMN[v]
    # widths per level over Martian ground
    strip = r36.mars_ground(8, 200).crop((0, 0, 330, 200))
    for k, (name, w) in enumerate(LEVEL_W):
        x = 30 + k * 54
        seg = col(30, 190, w, 0.3, taper=6)
        sprite.paste(strip, seg, x - seg.width / 2, 30)
        fl = flares(v, 0.55)[k % 6]
        v8.additive(strip, fl, x, 30 + (fl.height / 2 - 22))
        label(strip, x - 8, 6, name, WHITE)
        label(strip, x - 9, 16, f"{w}PX", (170, 200, 230))
    panels = [("WIDTH PER LEVEL, 1X: L1 6 PX -> L5 14 PX, OVERDRIVE 18 PX (THEN 2X)",
               enlarge(strip, 2))]

    def pick(kind, n=0):
        hits = [k for k, im in enumerate(info) if im and im[0] == kind]
        return hits[min(n, len(hits) - 1)] if hits else 0
    crops = []
    for kind, n, lab, s, k in (("air", 4, "AIR UNIT: FLARE + HEAT", 90, 3),
                               ("tendril", 4, "TENDRIL: BURNS, BEAM GOES ON", 90, 3),
                               ("wall", 10, "ROCK ISLAND: ROCK-SCORCH", 90, 3),
                               ("glance", 6, "HARDENED: GLANCE SPARK", 90, 3)):
        fi = pick(kind, n)
        _, x, y = info[fi]
        crops.append((f"{lab} ({k}X, T {fi / SFPS:.1f} S)", enlarge(crop_at(frames[fi], x, y, s, s), k)))
    panels += crops
    # whole-field frames: tendril cut with the needler beyond, island with the needler safe
    t_ten = int((TEND_CUT + 0.4) * SFPS)
    t_isl = int(5.2 * SFPS)
    panels.append((f"T {t_ten / SFPS:.1f} S: TENDRIL CUT, BEAM ON",
                   frames[t_ten].crop((60, 0, 420, 540)).resize((240, 360), Image.LANCZOS)))
    panels.append((f"T {t_isl / SFPS:.1f} S: ISLAND, NEEDLER SAFE (1X)",
                   frames[t_isl].crop((60, 0, 420, 540)).resize((240, 360), Image.LANCZOS)))
    notes = {
        "a": ["ION BEAM A - THE CHOSEN ROUND-08 BEAM FAMILY AND ROUND-09 CONTACT FLARE, PER LEVEL WIDTH",
              "SEGMENTED CYAN COLUMN WITH A WHITE CORE; BORES 4 PX INTO THE FIRST TARGET; STEADY HEAT TINT ON IT.",
              "WALL / ROCK ISLAND: THE FLARE TINTED WITH DUST, DUST THROWN BACK AND SIDEWAYS, A SCORCH TRAIL ON",
              "THE ROCK EDGE (STAYS WHILE IT SCROLLS). TENDRIL: WHITE-HOT SPOT WHERE IT CROSSES, A SMALL FLARE,",
              "THE BEAM GOES ON TO ITS FIRST TARGET; 40 HP PART IN 0.67 S AT L2. HARDENED GROUND: FAINT GLANCE",
              "SPARK, THE BEAM RUNS ON. NOTHING IN THE PATH: OFF THE TOP EDGE, NO CAP. ON 0.05 S, OFF FADE 0.08 S.",
              "ALL BEAM LIGHT STAYS CYAN / WHITE (PLAYER HUES): NO ORANGE HEAT, RESERVED FOR ENEMY BULLETS."],
        "b": ["ION BEAM B - ION STREAM: A HARD WHITE CORE IN A PALE SHEATH, HELIX STRANDS AND PACKETS",
              "TWO THIN STRANDS TWIST ROUND THE CORE (30 PX PITCH) AND BRIGHT PACKETS RUN UP AT ABOUT 1000 PX/S:",
              "READS AS A STREAM OF IONS RATHER THAN A LASER, AND STAYS DIFFERENT FROM THE LANCE LASER'S BOLTS.",
              "CONTACT: A LIGHTER FLARE WITH TWO FLAT RINGS SPREADING ALONG THE SURFACE. WALL: SLAG BEADS",
              "DRIP DOWN FROM THE CONTACT POINT, SCORCH TRAIL ON THE ROCK. TENDRIL, HARDENED GROUND, OPEN SKY",
              "AND TIMING AS A. ALL LIGHT CYAN / WHITE."]}[v]
    gi = int(5.0 * SFPS)
    return r5.build_sheet(f"ION BEAM {v.upper()} - CONTINUOUS BEAM, FIRST TARGET, WALLS AND TENDRILS",
                          panels, notes, frames[gi])


def make_ion(variants):
    for v in variants:
        frames, info = ion_scene(v)
        save("weapons", "ion-beam", v, ion_sheet(v, frames, info), frames, fps=SFPS)


# =========================================================================== HUD panel mock

CAPTURE = ROOT / "design" / "player" / "wingmen" / "concept" / "rook-ingame-capture-r28-a.png"
HUD_X = 720                       # the right panel of the 960x540 capture
BOX_BG = (12, 27, 40)
ROW_BG = (14, 36, 42)
NAME_GREEN = (64, 255, 128)
HUD_WHITE = (224, 230, 255)
PIP_LIT, PIP_DARK = (232, 196, 52), (46, 46, 26)
_F = {}


def font(name):
    if name not in _F:
        _F[name] = BMFont(name)
    return _F[name]


def hud_panel():
    """The right HUD panel (240x540) of the round-28 Level 02 capture, as the base to paint
    the new rows into (absolute capture x minus HUD_X)."""
    return Image.open(CAPTURE).convert("RGBA").crop((HUD_X, 0, 960, FH))


def fill(img, box, col):
    ImageDraw.Draw(img).rectangle([box[0] - HUD_X, box[1], box[2] - HUD_X, box[3]], fill=col)


def text(img, x, y, s, col, name="label-8x12"):
    return font(name).draw(img, x - HUD_X, y, s, col) + HUD_X


def weapon_row(img, r, slot, name, pips, pip_cols):
    """Weapons box row r (0 = F): slot letter, weapon name in green, five pips."""
    y = 189 + 14 * r
    fill(img, (740, y, 944, y + 12), BOX_BG)
    text(img, 742, y, slot, HUD_WHITE)
    text(img, 758, y, name, NAME_GREEN)
    for k in range(5):
        x0 = 888 + 10 * k
        fill(img, (x0, y + 4, x0 + 7, y + 10), pip_cols[k] if k < len(pip_cols) else PIP_DARK)


# =========================================================================== 2 Light drone

HULL_C, ACC_C = v8.HULL_C, v8.ACC_C


def _drone_mats(lit):
    return [v8.m_metal(HULL_C[4]), v8.m_metal(HULL_C[1]), v8.m_metal(ACC_C[1]),
            v8.m_glow(ACC_C[2], 1.8 * lit), v8.m_metal(HULL_C[2])]


def drone_a(lit=1.0):
    """Disc: a round light-grey hull with a blue rim band, a dark dome with a cyan lens and a
    short barrel at the front."""
    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_ellipsoid(p, (0, 0, 0.0), (0.6, 0.6, 0.18)), 0),
            (sd_cylinder_z(p, (0, 0, 0.0), 0.64, 0.05), 2),
            (sd_sphere(p, (0, -0.04, 0.1), 0.3), 1),
            (sd_sphere(p, (0, 0.14, 0.27), 0.11), 3),
            (sd_capsule(p, (0, 0.3, 0.06), (0, 0.8, 0.06), 0.075), 4),
            (sd_box(q, (0.36, -0.36, 0.12), (0.08, 0.08, 0.05), 0.03), 1),
            k=0.02)
    return scene, _drone_mats(lit)


def drone_b(lit=1.0):
    """Dart: a small delta with a raised spine, blue leading-edge stripes, a cyan sensor and
    two engine nacelles glowing at the back."""
    hull = [(0.0, 0.82), (0.66, -0.46), (0.3, -0.36), (0.0, -0.5)]

    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_plate(q, hull, 0.0, 0.08, 0.02), 0),
            (sd_plate(q, [(0.05, 0.62), (0.52, -0.3), (0.44, -0.33), (0.02, 0.5)], 0.07, 0.02), 2),
            (sd_ellipsoid(p, (0, 0.05, 0.08), (0.13, 0.42, 0.11)), 1),
            (sd_sphere(p, (0, 0.38, 0.13), 0.075), 3),
            (sd_capsule(q, (0.24, -0.12, 0.03), (0.24, -0.5, 0.03), 0.08), 4),
            (sd_sphere(q, (0.24, -0.56, 0.03), 0.065), 3),
            k=0.02)
    return scene, _drone_mats(lit)


DRONE_N = 14
DRONES = {"a": rig.AngleSprites(drone_a, DRONE_N, 16, extent=1.75, factor=8, colors=20),
          "b": rig.AngleSprites(drone_b, DRONE_N, 16, extent=1.75, factor=8, colors=20)}


def bay_model(v):
    def scene(p):
        q = mirror_x(p)
        if v == "a":            # dock-ring bay: the drone sits on a lit pad at the front
            return union(
                (sd_box(p, (0, -0.12, 0), (0.28, 0.6, 0.14), 0.12), 0),
                (sd_cylinder_z(p, (0, 0.28, 0.12), 0.26, 0.05), 1),
                (sd_cylinder_z(p, (0, 0.28, 0.16), 0.15, 0.03), 3),
                (sd_box(p, (0, -0.56, 0.08), (0.16, 0.09, 0.06), 0.02), 2),
                k=0.02)
        return union(           # launch-rail bay: a slim rail with a glowing slot and fins
            (sd_box(p, (0, -0.05, 0), (0.17, 0.72, 0.1), 0.06), 0),
            (sd_box(q, (0.09, 0.12, 0.1), (0.035, 0.52, 0.03)), 1),
            (sd_box(p, (0, 0.12, 0.1), (0.03, 0.48, 0.02)), 3),
            (sd_box(q, (0.22, -0.52, 0.0), (0.07, 0.15, 0.05), 0.02), 2),
            k=0.02)
    return scene, _drone_mats(1.0)


_BAY = {}


def bay_sprite(v):
    if v not in _BAY:
        scene, mats = bay_model(v)
        hi = sdf.render(scene, mats, (12 * 8, 18 * 8), 1.3)
        _BAY[v] = sprite.make_sprite(hi, 8, 20)
    return _BAY[v]


ORBIT_R, ORBIT_W = 36.0, np.radians(150)
CONV = 140.0
D_HITS, D_REBUILD = 8, 6.0          # L1
D_T = 15.5


def ld_ship(t):
    return 240 + 46 * np.sin(t * 0.55), 440 - 10 * np.sin(t * 0.9)


def ld_scene(v):
    still = r36.mars_ground(83, craters=8, dunes=0.02)    # still ground: the GIF stays small
    at = lambda t: still.copy()
    rng = np.random.default_rng(5)
    needler = asset("needler_0")
    bay = bay_sprite(v)
    drones = {side: dict(a0=np.pi if side == "left" else 0.0, hits=0, broke=None,
                         flash=-1.0, rebuilt=0.0) for side in ("left", "right")}
    # needlers: (x0, entry time), hover with a sway; respawn 1.2 s after a kill
    foes = [dict(x=x, te=te, hp=34, dead=None, k=k) for k, (x, te) in
            enumerate(((150, 0.0), (330, 0.1), (240, 0.3), (390, 1.0)))]
    bullets, bolts, sparks = [], [], []
    frames, log = [], []
    dt = 1 / SFPS
    for i in range(int(D_T * SFPS)):
        t = i / SFPS
        f = at(t)
        sx, sy = ld_ship(t)
        cpt = (sx, sy - CONV)
        # ---- foes
        live = []
        for fo in foes:
            if fo["dead"] is not None and t - fo["dead"] > 1.2:
                fo.update(x=float(rng.uniform(80, 400)), te=t, hp=34, dead=None)
            if fo["dead"] is None:
                ty = min(1.0, (t - fo["te"]) / 1.2)
                fo["px"] = fo["x"] + 26 * np.sin(t * 0.8 + fo["k"])
                fo["py"] = -30 + (110 + 20 * (fo["k"] % 2) + 30 * fo["k"] % 3) * smooth(ty)
                live.append(fo)
                period = 0.95
                ph = (t - fo["te"] - 0.5 - fo["k"] * 0.17) % period
                if t - fo["te"] > 0.5 and ph < dt:
                    ang = np.arctan2(sy - fo["py"], sx - fo["px"])
                    for spread in np.radians(np.linspace(-18, 18, 5)):
                        bullets.append([fo["px"], fo["py"], np.cos(ang + spread) * 165,
                                        np.sin(ang + spread) * 165])
        # ---- drones: positions
        pos = {}
        for side, d in drones.items():
            ang = d["a0"] + ORBIT_W * t
            ox, oy = sx + np.cos(ang) * ORBIT_R, sy + np.sin(ang) * ORBIT_R
            bx = sx + (-16 if side == "left" else 16)
            by = sy + 2
            if d["broke"] is not None:
                since = t - d["broke"] - D_REBUILD
                if since < 0:
                    pos[side] = None
                    continue
                k = smooth(min(1.0, max(0.0, (since - 0.3) / 0.35)))
                pos[side] = (bx + (ox - bx) * k, by + (oy - by) * k,
                             min(1.0, since / 0.3), since)
                if since > 0.65:
                    d.update(broke=None, hits=0, rebuilt=t)
            else:
                pos[side] = (ox, oy, 1.0, None)
        # ---- bullets: move, absorb on drones, shield on the ship
        keep = []
        for b in bullets:
            b[0] += b[2] * dt
            b[1] += b[3] * dt
            gone = not (-20 < b[0] < FW + 20 and -20 < b[1] < FH + 20)
            for side, p in pos.items():
                if gone or p is None or p[2] < 1.0:
                    continue
                if abs(b[0] - p[0]) < 4 + 4 and abs(b[1] - p[1]) < 4 + 4:
                    d = drones[side]
                    d["hits"] += 1
                    d["flash"] = t
                    sparks.append((t, p[0], p[1], "block"))
                    gone = True
                    if d["hits"] >= D_HITS:
                        d["broke"] = t
                        sparks.append((t, p[0], p[1], "break"))
                    log.append((round(t, 2), side, d["hits"]))
            if not gone and np.hypot(b[0] - sx, b[1] - sy) < 15:
                sparks.append((t, sx, sy, "shield"))
                gone = True
            if not gone:
                keep.append(b)
        bullets = keep
        # ---- player fire: Pulse Cannon (front) and the drones at the convergence point
        if i % 2 == 0:
            for ox_ in (-5, 5):
                bolts.append([sx + ox_, sy - 26, 0.0, -760.0, "pulse"])
        for side, p in pos.items():
            if p is None or p[2] < 1.0 or p[3] is not None:
                continue
            if (i + (0 if side == "left" else 1)) % 2 == 0:       # 6 per second each
                ang = np.arctan2(cpt[1] - p[1], cpt[0] - p[0])
                bolts.append([p[0], p[1], np.cos(ang) * 900, np.sin(ang) * 900, "drone"])
        keep = []
        for b in bolts:
            b[0] += b[2] * dt
            b[1] += b[3] * dt
            hit = None
            for fo in live:
                if abs(b[0] - fo["px"]) < 13 and abs(b[1] - fo["py"]) < 14:
                    hit = fo
                    break
            if hit is not None:
                hit["hp"] -= 1.0 if b[4] == "pulse" else 0.6
                hit["flash"] = t
                sparks.append((t, b[0], hit["py"] + 10, "impact"))
                if hit["hp"] <= 0 and hit["dead"] is None:
                    hit["dead"] = t
                    sparks.append((t, hit["px"], hit["py"], "kill"))
            elif b[1] > -30:
                keep.append(b)
        bolts = keep
        # ---- draw
        for fo in live:
            if fo["dead"] is None:
                sp = needler
                if t - fo.get("flash", -9) < 0.08:
                    sp = flash_sprite(sp, 0.5)
                r5.paste(f, sp, fo["px"], fo["py"], "air")
        for b in bullets:
            e3.orb(f, b[0], b[1], 4)
        for b in bolts:
            if b[4] == "pulse":
                v8.additive(f, asset("pulse-bolt"), b[0], b[1])
            else:
                v8.additive(f, drone_bolt(np.arctan2(b[2], -b[3])), b[0], b[1])
        draw_ship(f, sx, sy, t, bays=(bay, bay.transpose(Image.FLIP_LEFT_RIGHT)))
        for side, p in pos.items():
            if p is None:
                continue
            ang = np.arctan2(cpt[1] - p[1], cpt[0] - p[0])
            sp = DRONES[v].get(ang)
            if p[2] < 1.0:                          # rebuild: fades in at its bay
                sp = v8.alpha_mul(flash_sprite(sp, 0.7 * (1 - p[2])), p[2])
            elif t - drones[side]["flash"] < 0.1:
                sp = flash_sprite(sp, 0.65)
            sh = sprite.shadow_of(sp, opacity=0.5, blur=0.8, scale=0.85)
            sprite.paste_center(f, sh, p[0] + 21, p[1] + 30)
            sprite.paste_center(f, sp, p[0], p[1])
        for ts, x, y, kind in sparks:
            age = t - ts
            if kind == "block" and age < 0.16:
                v8.additive(f, asset(f"pulse-impact_{min(3, int(age * 25))}"), x, y)
            elif kind == "break" and age < 0.8:
                explosion(f, x, y, age / 0.8, "small")
            elif kind == "kill" and age < 0.8:
                explosion(f, x, y, age / 0.8, "small")
            elif kind == "impact" and age < 0.1:
                v8.additive(f, asset(f"pulse-impact_{min(3, int(age * 40))}"), x, y)
            elif kind == "shield" and age < 0.27:
                v8.additive(f, asset(f"ship-shield_{min(3, int(age * 15))}"), x, y)
        sparks = [s for s in sparks if t - s[0] < 1.0]
        lab = "LIGHT DRONE BAY L1 X2  " + ("A: DISC + DOCK RING" if v == "a" else "B: DART + LAUNCH RAIL")
        label(f, 8, FH - 14, lab, (150, 200, 240))
        hl = [drones[s]["hits"] if drones[s]["broke"] is None else -1 for s in ("left", "right")]
        frames.append(f)
        log.append(("state", round(t, 2), hl))
    return frames, log


_DB = {}


def drone_bolt(angle):
    """The drone's shot: the pulse family, smaller (3x8)."""
    k = int(round(np.degrees(angle) / 3)) * 3
    if k not in _DB:
        _DB[k] = v8.energy_bolt(6, 1.4, angle=np.radians(k), glow=0.9, pad=3)
    return _DB[k]


def ld_hud_rows(states):
    """Weapons box rows L and R of the HUD (from the capture) for (left, right) states:
    ('hits', left_of_8) or ('rebuild', fraction)."""
    out = []
    for st in states:
        p = hud_panel()
        weapon_row(p, 0, "F", "PULSE CANNON", 4, [PIP_LIT] * 4)
        for r, (slot, s) in zip((2, 3), (("L", st[0]), ("R", st[1]))):
            if s[0] == "hits":
                n = int(np.ceil(5 * s[1] / D_HITS))
                cols = [(110, 220, 255)] * n
            else:
                n = int(np.floor(5 * s[1]))
                cols = [(40, 90, 120)] * n
            weapon_row(p, r, slot, "LIGHT DRONE", n, cols)
        weapon_row(p, 1, "R", "-", 0, [])
        out.append(p.crop((10, 180, 232, 266)))
    return out


def ld_sheet(v, frames, log):
    states = [x for x in log if x[0] == "state"]
    hits = [x for x in log if x[0] != "state"]
    sprites = [DRONES[v].frame(k) for k in (12, 13, 14, 15, 0, 4, 8)]
    sprites += [flash_sprite(DRONES[v].frame(12), 0.65)]
    labs = ["UP", "", "", "", "RIGHT", "DOWN", "LEFT", "BLOCK"]
    panels = [("DRONE 14X14 (4X): AIMS AT THE CONVERGENCE POINT, 16 HEADINGS",
               r5.grid(sprites, 8, scale=4, labels=labs))]
    bay = bay_sprite(v)
    ship = Image.new("RGBA", (96, 80), (0, 0, 0, 0))
    draw_ship(ship, 48, 40, 0.0, bays=(bay, bay.transpose(Image.FLIP_LEFT_RIGHT)), shadow=False)
    panels.append(("BAY 12X18 (4X) AND ON THE WINGS (3X)",
                   r5.grid([bay], 1, scale=4)))
    panels.append(("", e3.checker(288, 240).copy()))
    panels[-1][1].alpha_composite(enlarge(ship, 3))
    # orbit and convergence diagram at 2x
    g = r36.mars_ground(83, 260).crop((140, 0, 340, 260)).convert("RGBA")
    sx, sy = 100, 200
    d = ImageDraw.Draw(g)
    for k in range(48):
        a = k / 48 * TAU
        d.point((sx + np.cos(a) * ORBIT_R, sy + np.sin(a) * ORBIT_R), fill=(170, 220, 255, 255))
    cx, cy = sx, sy - CONV
    for a in (np.pi, 0.0, np.radians(225), np.radians(300)):
        px_, py_ = sx + np.cos(a) * ORBIT_R, sy + np.sin(a) * ORBIT_R
        d.line([(px_, py_), (cx + (cx - px_) * 0.25, cy + (cy - py_) * 0.25)],
               fill=(90, 170, 230, 160))
    d.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], outline=(255, 255, 255, 255))
    draw_ship(g, sx, sy, 0.0, bays=(bay, bay.transpose(Image.FLIP_LEFT_RIGHT)))
    for a in (np.pi, 0.0, np.radians(225), np.radians(300)):
        px_, py_ = sx + np.cos(a) * ORBIT_R, sy + np.sin(a) * ORBIT_R
        sprite.paste_center(g, DRONES[v].get(np.arctan2(cy - py_, cx - px_)), px_, py_)
    label(g, cx + 6, cy - 3, "140 PX", WHITE)
    label(g, sx + 40, sy + 30, "R 36", WHITE)
    panels.append(("ORBIT 36 PX, 150 DEG/S CLOCKWISE; SHOTS MEET 140 PX AHEAD (2X)", enlarge(g, 2)))
    # block / break / rebuild crops
    brk = [h for h in hits if h[2] == D_HITS]
    crops = []
    if brk:
        tb = brk[0][0]
        side = brk[0][1]
        for lab, tt in (("BLOCKS A BULLET", tb - 0.6), ("8TH HIT: BREAKS", tb + 0.08),
                        ("REBUILT AT THE BAY", tb + D_REBUILD + 0.16),
                        ("BACK ON ITS ORBIT", tb + D_REBUILD + 0.9)):
            fi = min(len(frames) - 1, int(round(tt * SFPS)))
            sx_, sy_ = ld_ship(fi / SFPS)
            crops.append((lab, frames[fi].crop((int(sx_ - 60), int(sy_ - 60), int(sx_ + 60),
                                                int(sy_ + 60)))))
        panels.append((f"{side.upper()} DRONE (3X): " + " / ".join(c[0] for c in crops),
                       r5.grid([c[1] for c in crops], 4, scale=2)))
    rows = ld_hud_rows([(("hits", 8), ("hits", 8)), (("hits", 3), ("hits", 7)),
                        (("rebuild", 0.4), ("hits", 6)), (("rebuild", 0.9), ("hits", 5))])
    panels.append(("HUD WEAPONS BOX (2X): HITS LEFT AS FIVE PIPS; REBUILD REFILLS THEM DIM",
                   r5.grid(rows, 2, scale=2, pad=6)))
    nb = len(brk)
    notes = {
        "a": ["LIGHT DRONE A - DISC DRONE AND DOCK-RING BAY",
              "A ROUND LIGHT-GREY HULL WITH THE UTC BLUE RIM BAND, A DARK DOME WITH A CYAN LENS AND A SHORT BARREL",
              "THAT TURNS TO THE CONVERGENCE POINT; THE BAY IS A ROUNDED POD WITH A LIT DOCKING PAD AT ITS NOSE."],
        "b": ["LIGHT DRONE B - DART DRONE AND LAUNCH-RAIL BAY",
              "A SMALL DELTA WITH BLUE LEADING-EDGE STRIPES, A CYAN SENSOR AND TWO GLOWING NACELLES: IT READS",
              "AS A MINIATURE STORMHAWK; THE BAY IS A SLIM RAIL WITH A LIT SLOT IT LAUNCHES FROM."]}[v]
    notes += ["BOTH: 14X14 SPRITE, 8X8 HIT BOX, AIR LAYER, UNDER THE SHIP; ORBIT 36 PX AT 150 DEG/S CLOCKWISE, THE LEFT",
              "BAY'S DRONE STARTS AT 9 O'CLOCK, THE RIGHT ONE'S AT 3; 6 SHOTS/S EACH (PULSE FAMILY, 3X8, 900 PX/S)",
              "AIMED AT THE POINT 140 PX AHEAD. A BULLET THAT TOUCHES IT IS ABSORBED (WHITE FLASH + PULSE IMPACT);",
              f"AFTER 8 HITS (L1) IT BREAKS (EXPLOSION-SMALL) AND THE BAY REBUILDS IT IN 6 S: FADES IN AT THE BAY",
              "OVER 0.3 S, THEN FLIES OUT TO ITS ORBIT. THE GIF: 5-WAY NEEDLER FANS AIMED AT THE SHIP, NEVER AT",
              f"THE DRONES; {nb} DRONE(S) BROKE IN {D_T:.1f} S. HUD: HITS LEFT AS FIVE PIPS IN PLACE OF THE LEVEL PIPS",
              "(PENDING: WHERE THE BAY'S LEVEL SHOWS THEN)."]
    gi = int(3.0 * SFPS)
    return r5.build_sheet(f"LIGHT DRONE {v.upper()} - ORBIT, CONVERGENCE, BLOCK, BREAK, REBUILD",
                          [p for p in panels if p[0] or True], notes, frames[gi])


def make_drone(variants):
    for v in variants:
        frames, log = ld_scene(v)
        brk = [h for h in log if h[0] != "state" and h[2] == D_HITS]
        print("drone breaks:", brk)
        save("weapons", "light-drone", v, ld_sheet(v, frames, log), frames, fps=SFPS)


# =========================================================================== 3 EMP Burst

E_T = 6.6
E_AT = 1.6                       # the burst
E_GROW = 0.4                     # the ring covers the field
STUN = {"machine": 3.0, "vrell": 1.5}
ARC = (150, 210, 255)


def _grey_tank(model, glow):
    scene, mats = model
    grey = r36.unmarked_mats(glow, hull_pattern=r36.em._hex(9))
    grey.append(r36.replace(mats[7], albedo=r36.UG[1]))
    return scene, grey


TANK_HULL = rig.ModelSpaceAngleSprites(
    lambda tread=0.0, glow=1.0: _grey_tank(r36.am.warden_hull(tread), glow), 76, 16, factor=5,
    colors=32, post=r36.grey_rim)
TANK_TURRET = rig.ModelSpaceAngleSprites(
    lambda glow=1.0: _grey_tank(r36.am.warden_turret(), glow), 76, 32, factor=5, colors=24,
    post=r36.grey_rim)


def crackle(sp, k, v, seed=0):
    """Stun overlay frame k (4-frame loop): electric arcs over the silhouette, masked near it."""
    rng = np.random.default_rng(seed * 31 + k * 7 + (0 if v == "a" else 99))
    pad = 6
    w, h = sp.width + 2 * pad, sp.height + 2 * pad
    lay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    al = np.array(sp.getchannel("A")) > 100
    ys, xs = np.nonzero(al)
    if not len(xs):
        return lay
    n = 2 + sp.width // 18
    for _ in range(n):
        i, j = rng.integers(0, len(xs), 2)
        p0, p1 = (xs[i] + pad, ys[i] + pad), (xs[j] + pad, ys[j] + pad)
        pts = jagged(p0, p1, rng, depth=3, rough=0.5)
        glow_line(lay, pts, ARC, width=1, glow=3, alpha=0.95)
    if v == "b":                     # sparks orbiting the unit: a small lightning cage
        cx, cy = w / 2, h / 2
        for m in range(3):
            a = (k / 4 + m / 3) * TAU
            r = sp.width * 0.55
            q0 = (cx + np.cos(a) * r, cy + np.sin(a) * r * 0.8)
            q1 = (cx + np.cos(a + 0.6) * r, cy + np.sin(a + 0.6) * r * 0.8)
            glow_line(lay, jagged(q0, q1, rng, depth=2, rough=0.4), (190, 235, 255), glow=2)
    return lay


_CR = {}


def stunned_look(sp, kind, k, v, key):
    """A stunned unit: glow dimmed (Vrell) or dark (machines: lights out comes from the model),
    a cool desaturation for b, and the crackle overlay on top."""
    ck = (key, k, v)
    if ck not in _CR:
        base = dim(sp, 0.72) if kind == "vrell" else sp
        if v == "b":
            base = v9.tint_sprite(base, (110, 130, 160), 0.22)
        out = Image.new("RGBA", (sp.width + 12, sp.height + 12), (0, 0, 0, 0))
        out.alpha_composite(base, (6, 6))
        out.alpha_composite(crackle(sp, k, v, seed=zlib.crc32(repr(key).encode()) % 1000))
        _CR[ck] = out
    return _CR[ck]


def emp_ring(v, r, p, cx, cy, i, contacts):
    """The ring at radius r (progress p of the 0.4 s spread), as a field-sized layer."""
    cv = Canvas(FW, FH, ss=2)
    d = cv.dist(cx, cy)
    fade = 1 - 0.35 * p
    if v == "a":
        cv.add(PS[1], gauss(d - r, 9) * 0.7 * fade)
        cv.add(PS[2], gauss(d - r, 3.5) * 0.9 * fade)
        cv.add(WHITE, gauss(d - r, 1.3) * 1.2 * fade)
        cv.add(PS[1], (d < r) * 0.10 * (1 - p))
    else:
        ang = np.arctan2(cv.y - cy, cv.x - cx)
        dash = (np.sin(ang * 26 + i * 1.7) > -0.3).astype(float)
        dash2 = (np.sin(ang * 19 - i * 2.3 + 1) > -0.1).astype(float)
        cv.add(PS[2], gauss(d - r, 2.2) * dash * 1.0 * fade)
        cv.add(WHITE, gauss(d - r, 1.0) * dash * 1.0 * fade)
        cv.add(PS[3], gauss(d - (r - 9), 1.4) * dash2 * 0.8 * fade)
        cv.add(PS[1], gauss(d - r, 14) * 0.35 * fade)
    lay = cv.image()
    rng = np.random.default_rng(i * 5 + (0 if v == "a" else 50))
    if v == "a":                     # arcs crackling along the ring
        for _ in range(14):
            a0 = rng.uniform(0, TAU)
            a1 = a0 + rng.uniform(0.12, 0.3) * 120 / max(r, 120)
            p0 = (cx + np.cos(a0) * r, cy + np.sin(a0) * r)
            p1 = (cx + np.cos(a1) * r, cy + np.sin(a1) * r)
            glow_line(lay, jagged(p0, p1, rng, depth=3, rough=0.6), ARC, glow=3, alpha=fade)
    else:                            # forks from the front into each unit it just reached
        for ux, uy in contacts:
            a = np.arctan2(uy - cy, ux - cx)
            for da in (-0.09, 0.09):
                q = (cx + np.cos(a + da) * r, cy + np.sin(a + da) * r)
                glow_line(lay, jagged(q, (ux, uy), rng, depth=3, rough=0.5), (190, 235, 255),
                          glow=3)
    return lay


class Unit:
    def __init__(self, kind, path, sprite_fn, fire=None, hp=None, layer="air", name=""):
        self.kind, self.path, self.sprite_fn, self.fire = kind, path, sprite_fn, fire
        self.tau, self.off = 0.0, np.zeros(2)
        self.stun_from, self.stun_to, self.vel = None, None, np.zeros(2)
        self.hp, self.dead, self.layer, self.name = hp, None, layer, name
        self.flash = -9.0

    def pos(self):
        return np.asarray(self.path(self.tau), float) + self.off

    def stunned(self, t):
        return self.stun_from is not None and self.stun_from <= t < self.stun_to


def emp_scene(v):
    still = r36.mars_ground(41, craters=7, dunes=0.03)
    road = Image.new("RGBA", still.size, (0, 0, 0, 0))     # the tank's graded road
    ImageDraw.Draw(road).rectangle([333, 0, 367, FH], fill=(176, 110, 70, 110))
    still.alpha_composite(road.filter(ImageFilter.GaussianBlur(2.5)))
    gh = r36.ghost["a"]
    needler = asset("needler_0")
    units = []
    for k in range(3):
        units.append(Unit("machine", (lambda tau, k=k: (-60 + 95 * tau + k * 70,
                                                         150 + 34 * k + 10 * np.sin(tau * 2.4))),
                          lambda u, t, lit: gh.get(0.0, glow=lit), fire=(0.9 + 0.25 * k, 1.3),
                          name="ghost"))
    for k, (dx, dy) in enumerate(((0, 0), (-40, -26), (40, -26), (-80, -52), (80, -52))):
        units.append(Unit("vrell", (lambda tau, dx=dx, dy=dy: (240 + dx + 20 * np.sin(tau * 1.1),
                                                                40 + dy + 34 * tau)),
                          lambda u, t, lit: needler, fire=(0.5 + 0.2 * k, 1.5),
                          hp=14 if k == 0 else 99, name="needler"))
    units.append(Unit("machine", lambda tau: (350, 250 + 16 * tau), None, fire=(0.7, 1.6),
                      layer="ground", name="tank"))
    shots, bolts, frames, info = [], [], [], []
    dt = 1 / FPS
    px, py = 240.0, 468.0
    for i in range(int(E_T * FPS)):
        t = i / FPS
        sx = px + 8 * np.sin(t * 1.3)
        f = still.copy()
        ring_r = None
        if E_AT <= t < E_AT + E_GROW + 0.15:
            p = min(1.0, (t - E_AT) / E_GROW)
            ring_r = p * np.hypot(max(sx, FW - sx), py) * 1.02
        contacts = []
        for u in units:
            if u.dead is not None:
                continue
            x, y = u.pos()
            if ring_r is not None and u.stun_from is None and np.hypot(x - sx, y - py) <= ring_r:
                u.stun_from, u.stun_to = t, t + STUN[u.kind]
                if u.layer == "air":
                    u.vel = (np.asarray(u.path(u.tau + 0.05)) - np.asarray(u.path(u.tau))) / 0.05
                contacts.append((x, y))
            if u.stunned(t):
                u.off += u.vel * dt
            else:
                prev = u.tau
                u.tau += dt
                if u.fire and u.dead is None:
                    t0, per = u.fire
                    if np.floor((u.tau - t0) / per) > np.floor((prev - t0) / per):
                        ang = np.arctan2(py - y, sx - x)
                        n = 3 if u.name == "ghost" else 1
                        for m in range(n):
                            shots.append([x + np.cos(ang) * 14, y + np.sin(ang) * 14,
                                          np.cos(ang) * 230, np.sin(ang) * 230, -m * 0.07,
                                          "gold" if u.name != "needler" else "orb"])
        # shots fly on through the burst (not cleared)
        keep = []
        for s_ in shots:
            if s_[4] < 0:
                s_[4] += dt
                keep.append(s_)
                continue
            s_[0] += s_[2] * dt
            s_[1] += s_[3] * dt
            if -20 < s_[0] < FW + 20 and -20 < s_[1] < FH + 20 and np.hypot(s_[0] - sx, s_[1] - py) > 14:
                keep.append(s_)
        shots = keep
        # the player's Pulse Cannon on the lead Needler
        if i % 3 == 0:
            for ox in (-5, 5):
                bolts.append([sx + ox, py - 26])
        keep = []
        for b in bolts:
            b[1] -= 760 * dt
            hit = None
            for u in units:
                if u.name == "needler" and u.dead is None:
                    ux, uy = u.pos()
                    if abs(b[0] - ux) < 13 and abs(b[1] - uy) < 14:
                        hit = u
            if hit is not None:
                hit.hp -= 1
                hit.flash = t
                if hit.hp <= 0:
                    hit.dead = t
            elif b[1] > -30:
                keep.append(b)
        bolts = keep
        # draw: ground, air, shots, ship, ring
        for u in units:
            x, y = u.pos()
            st = u.stunned(t)
            k = int(t * 15) % 4
            if u.name == "tank":
                lit = 0.0 if st else 1.0
                hull = TANK_HULL.get(np.pi / 2, tread=0.0 if st else
                                     (0.5 if round((u.tau * 2.2) % 1.0, 1) >= 0.5 else 0.0),
                                     glow=lit)
                aim = np.arctan2(py - y, sx - x) if not st else getattr(u, "aim", np.pi / 2)
                if not st:
                    u.aim = aim
                tur = TANK_TURRET.get(aim, glow=lit)
                r5.paste(f, hull, x, y, "ground")
                sprite.paste_center(f, tur, x, y)
                if st:
                    sprite.paste_center(f, crackle(hull, k, v, seed=3), x, y)
                continue
        for u in units:
            if u.name == "tank":
                continue
            x, y = u.pos()
            if u.dead is not None:
                explosion(f, x, y, (t - u.dead) / 0.6)
                continue
            st = u.stunned(t)
            k = int(t * 15) % 4
            sp = u.sprite_fn(u, t, 0.0 if (st and u.kind == "machine") else 1.0)
            if t - u.flash < 0.06:
                sp = flash_sprite(sp, 0.5)
            if st:
                e3.place(f, sp, x, y, "air")
                sprite.paste_center(f, stunned_look(sp, u.kind, k, v, (u.name, t - u.flash < 0.06)),
                                    x, y)
            else:
                e3.place(f, sp, x, y, "air")
        for s_ in shots:
            if s_[4] >= 0:
                e3.orb(f, s_[0], s_[1], 4, ring=None if s_[5] == "orb" else e3.GOLD[3])
        for b in bolts:
            v8.additive(f, asset("pulse-bolt"), b[0], b[1])
        draw_ship(f, sx, py, t)
        if ring_r is not None:
            if v == "b" and t - E_AT < 0.1:          # short blue-white field flicker
                tint = Image.new("RGBA", f.size, (170, 210, 255, 46))
                f.alpha_composite(tint)
            ringl = emp_ring(v, ring_r, min(1.0, (t - E_AT) / E_GROW), sx, py, i, contacts)
            v8.additive(f, ringl, FW / 2, FH / 2)
            if t - E_AT < 0.12:
                raster.add_light(f, sx, py, 40, (180, 220, 255), 1.0 - (t - E_AT) / 0.12)
        lab = "EMP BURST  " + ("A: RING WITH CRACKLING ARCS" if v == "a" else "B: ARC FRONT")
        label(f, 8, FH - 14, lab, (150, 200, 240))
        if E_AT <= t:
            mach = [u for u in units if u.kind == "machine" and u.stunned(t)]
            vr = [u for u in units if u.kind == "vrell" and u.stunned(t) and u.dead is None]
            label(f, 8, FH - 28, f"STUNNED: MACHINES {len(mach)}  VRELL {len(vr)}", (190, 220, 255))
        frames.append(f)
        info.append(t)
    return frames, units


def emp_sheet(v, frames, units):
    pick = lambda tt: frames[min(len(frames) - 1, int(round(tt * FPS)))]
    ring = [pick(E_AT + dtt).resize((240, 270), Image.LANCZOS)
            for dtt in (0.05, 0.15, 0.25, 0.35)]
    panels = [("THE BURST: +0.05 / 0.15 / 0.25 / 0.35 S (HALF SIZE)", r5.grid(ring, 4, scale=1))]
    gh = r36.ghost["a"]
    rows = []
    g_on, g_off = gh.get(0.0, glow=1.0), gh.get(0.0, glow=0.0)
    nd = asset("needler_0")
    for sp, kind, key in ((g_off, "machine", "ghost-s"), (nd, "vrell", "needler-s")):
        rows.append([sp] + [stunned_look(sp, kind, k, v, key) for k in range(4)])
    cell = max(max(s.width, s.height) for r in rows for s in r)
    sheet_cells = []
    for r_, base in zip(rows, (g_on, nd)):
        for s in [base] + r_[1:]:
            c = Image.new("RGBA", (cell, cell), (0, 0, 0, 0))
            sprite.paste_center(c, s, cell / 2, cell / 2)
            sheet_cells.append(c)
    labs = ["GHOST DRONE", "STUN 1", "2", "3", "4", "NEEDLER", "STUN 1", "2", "3", "4"]
    panels.append(("STUN OVERLAY, 4-FRAME LOOP (3X): MACHINE LIGHTS OUT / VRELL GLOW DIMMED",
                   r5.grid(sheet_cells, 5, scale=3, labels=labs)))
    hull_on = TANK_HULL.get(np.pi / 2, tread=0.0, glow=1.0)
    hull_off = TANK_HULL.get(np.pi / 2, tread=0.0, glow=0.0)
    t_on = Image.new("RGBA", (88, 88), (0, 0, 0, 0))
    sprite.paste_center(t_on, hull_on, 44, 44)
    sprite.paste_center(t_on, TANK_TURRET.get(np.pi / 2, glow=1.0), 44, 44)
    t_off = Image.new("RGBA", (88, 88), (0, 0, 0, 0))
    sprite.paste_center(t_off, hull_off, 44, 44)
    sprite.paste_center(t_off, TANK_TURRET.get(np.pi / 2, glow=0.0), 44, 44)
    sprite.paste_center(t_off, crackle(hull_off, 1, v, seed=3), 44, 44)
    panels.append(("WARDEN TANK (GREY, MACHINE) 2X: RUNNING / STUNNED",
                   r5.grid([t_on, t_off], 2, scale=2)))
    # stun timeline
    tl = Image.new("RGBA", (420, 120), raster.SHEET_BG)
    d = ImageDraw.Draw(tl)
    x0, sc = 90, 80
    for k in range(6):
        d.line([(x0 + k * sc / 2, 10), (x0 + k * sc / 2, 104)], fill=(40, 46, 60))
        label(tl, x0 + k * sc / 2 - 6, 108, f"{k * 0.5:.1f}", raster.LABEL_DIM)
    for j, (name, dur, col) in enumerate((("MACHINES", 3.0, (120, 190, 255)),
                                          ("VRELL", 1.5, (110, 230, 170)),
                                          ("BOSSES", 0.0, (160, 160, 170)))):
        y = 20 + j * 28
        label(tl, 6, y + 4, name, WHITE)
        if dur:
            d.rectangle([x0, y, x0 + dur * sc, y + 14], fill=col + (255,))
            label(tl, int(x0 + dur * sc + 6), y + 4, f"{dur:.1f} S", col)
        else:
            label(tl, x0 + 4, y + 4, "NEVER STUNNED (SHIELDS, AURAS STRIPPED)", col)
    panels.append(("STUN LENGTHS FROM THE RING'S CONTACT", tl))
    notes = {
        "a": ["EMP BURST A - THIN BLUE-WHITE RING WITH CRACKLING ARCS (THE SPEC'S LOOK)",
              "A WHITE CORE IN A CYAN-BLUE HALO, 14 SHORT ARCS FLICKERING ALONG IT, A FAINT BLUE WASH INSIDE;",
              "UNLIKE THE SMART BOMB (WHITE FLASH, CLEAN RING, BULLETS POPPING) IT LEAVES THE BULLETS ALONE."],
        "b": ["EMP BURST B - ARC FRONT: A BROKEN DOUBLE RING THAT THROWS FORKS INTO EVERY UNIT IT REACHES",
              "A DASHED CYAN/WHITE RING WITH A SECOND PALE RING 9 PX INSIDE, A 0.1 S BLUE-WHITE FIELD FLICKER AT",
              "THE BURST, TWO FORKS INTO EACH UNIT AT CONTACT; STUNNED UNITS GET A COOL GREY CAST AND A SPARK CAGE."]}[v]
    notes += ["BOTH: THE RING COVERS THE FIELD IN 0.4 S FROM THE SHIP; A UNIT IS STUNNED WHEN IT REACHES ITS CENTRE.",
              "MACHINES (GHOST DRONES, THE GREY WARDEN TANK) 3 S WITH RUNNING LIGHTS AND ENGINES OUT; VRELL",
              "(NEEDLERS) 1.5 S WITH THEIR GLOW DIMMED. A STUNNED AIR UNIT DRIFTS ON ITS LINE, THE TANK STOPS",
              "(TRACKS AND TURRET FROZEN); NOBODY FIRES; BULLETS ALREADY IN FLIGHT FLY ON. THE LEAD NEEDLER",
              "IS SHOT DOWN DURING ITS STUN. THE OVERLAY IS 4 FRAMES OF ARCS OVER THE SILHOUETTE (ADDITIVE)."]
    return r5.build_sheet(f"EMP BURST {v.upper()} - STUNS MACHINES 3 S, VRELL 1.5 S",
                          panels, notes, pick(E_AT + 0.5))


def make_emp(variants):
    for v in variants:
        frames, units = emp_scene(v)
        save("specials", "emp-burst", v, emp_sheet(v, frames, units), frames)


# =========================================================================== 4 Shield Overcharge

O_T, OFPS = 12.0, 15
O_AT, O_LEN, O_CD = 2.0, 8.0, 45.0
CAP, REGEN, DELAY = 45.0, 4.0, 1.5
HIT_DMG = 6.0
HITS = [0.5, 0.9, 1.2, 1.6, 2.8, 3.1, 3.4, 3.7, 4.0, 4.3, 6.5, 7.0, 10.8, 11.3]
DENIED_AT = 6.0
GOLD_C = (255, 206, 72)
OC = {"a": dict(col=GOLD_C, core=(255, 248, 220), num=GOLD_C, cell=(255, 190, 40)),
      "b": dict(col=(200, 240, 255), core=WHITE, num=(210, 245, 255), cell=(235, 250, 255))}


def shield_sim(t_end, dt):
    """Shield value and capacity over time with the overcharge (Mk III numbers)."""
    s, last, out = CAP, -9.0, []
    hits = list(HITS)
    t = 0.0
    while t <= t_end + 1e-9:
        cap = 2 * CAP if O_AT <= t < O_AT + O_LEN else CAP
        if abs(t - O_AT) < dt / 2:
            s = 2 * CAP
        while hits and hits[0] <= t:
            hits.pop(0)
            s = max(0.0, s - HIT_DMG)
            last = t
        if t - last >= DELAY and s < cap:
            s = min(cap, s + REGEN * dt)
        if s > cap:                                  # the excess bleeds away in 0.5 s
            s = max(cap, s - CAP / 0.5 * dt)
        out.append((round(t, 4), s, cap))
        t += dt
    return out


def recolor(sp, col, k=1.0):
    """An additive cyan sprite recoloured by its brightness (shield ring frames)."""
    a = np.array(sp).astype(np.float64)
    lum = a[..., :3].max(axis=-1, keepdims=True) / 255
    rgb = lum * np.array(col) * k + (lum ** 3) * 255 * 0.35
    a[..., :3] = np.clip(rgb, 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def oc_bubble(v, t, since, x, y, img, alpha=1.0):
    """The overcharged shield round the ship: a gold shimmer (a) or a white double shell (b)."""
    c = OC[v]
    base = asset("ship-shield_3")
    pulse = 0.75 + 0.25 * np.sin(t * 7)
    cv = Canvas(72, 72)
    d = cv.dist(36, 36)
    ang = np.arctan2(cv.y - 36, cv.x - 36)
    if v == "a":
        sh = recolor(base, c["col"], 1.25 * pulse * alpha)
        v8.additive(img, sh, x, y)
        hl = gauss(d - 28.5, 1.4) * np.clip(np.cos(ang - t * 3.0), 0, 1) ** 6
        cv.add(c["core"], hl * 1.2 * alpha)
        cv.add(c["col"], gauss(d - 28.5, 4) * 0.18 * pulse * alpha)
    else:
        sh = recolor(base, c["col"], 1.1 * alpha)
        v8.additive(img, sh, x, y)
        big = sh.resize((70, 70), Image.BILINEAR)
        v8.additive(img, v8.alpha_mul(big, 0.7 * pulse), x, y)
        cv.add(c["core"], gauss(d - 34, 0.9) * 0.55 * pulse * alpha)
        rng = np.random.default_rng(int(t * OFPS))
        for _ in range(2):                           # crackle running round the outer shell
            a0 = rng.uniform(0, TAU)
            lay = Image.new("RGBA", (72, 72), (0, 0, 0, 0))
            pts = [(36 + np.cos(a0 + u * 0.5) * 34, 36 + np.sin(a0 + u * 0.5) * 34)
                   for u in np.linspace(0, 1, 6)]
            pts = [(px_ + rng.normal(0, 0.8), py_ + rng.normal(0, 0.8)) for px_, py_ in pts]
            glow_line(lay, pts, (200, 240, 255), glow=2, alpha=0.8 * alpha)
            v8.additive(img, lay, x, y)
    v8.additive(img, cv.image(), x, y)


def oc_activation(v, s, x, y, img):
    """The activation flare, s = 0..1 over 0.35 s."""
    c = OC[v]
    cv = Canvas(110, 110)
    d = cv.dist(55, 55)
    r = 14 + 38 * s
    if v == "a":
        cv.add(c["col"], gauss(d - r, 3 + 3 * s) * 1.1 * (1 - s))
        cv.add(c["core"], gauss(d, 16 * (1 - s) + 2) * 1.3 * (1 - s) ** 2)
    else:
        for rr, k in ((r, 1.0), (r * 0.72, 0.7)):
            cv.add(c["col"], gauss(d - rr, 1.6) * 1.3 * k * (1 - s))
        cv.add(WHITE, gauss(d, 20 * (1 - s) + 2) * 1.5 * (1 - s) ** 2)
    v8.additive(img, cv.image(), x, y)


def oc_icon(v):
    ic = asset("icons/shield-mk-iii").copy()
    ic = v9.tint_sprite(ic, OC[v]["col"], 0.55)
    d = ImageDraw.Draw(ic)
    d.line([(12, 1), (12, 5)], fill=WHITE + (255,))
    d.line([(10, 3), (14, 3)], fill=WHITE + (255,))
    return ic


def ring_img(v, cool, effect, flash=0.0):
    """The special row's ring (24x24, radius 10.5 round the 16 px icon): cooldown fill
    clockwise from 12 in dim cyan (``cool`` 0..1, 1 = ready), the effect arc draining
    anticlockwise (``effect`` 0..1 left)."""
    cv = Canvas(24, 24)
    d = cv.dist(12, 12)
    ang = np.arctan2(cv.x - 12, -(cv.y - 12)) % TAU          # 0 at 12 o'clock, clockwise
    band = solid(np.abs(d - 10.5), 1.1)
    cv.over((28, 46, 58), band)
    ready = cool >= 1.0
    if ready:
        cv.over((110, 210, 235), band)
    else:
        cv.over((54, 128, 150), band * (ang < cool * TAU))
    if effect > 0:
        cv.over(OC[v]["col"], solid(np.abs(d - 10.5), 1.4) * (ang > (1 - effect) * TAU))
    if flash:
        cv.add(WHITE, gauss(np.abs(d - 10.5), 1.8) * flash * 1.5)
    img = cv.image()
    sprite.paste_center(img, oc_icon(v), 12, 12)
    return img


_HUD = {}


def hud_base():
    if "p" not in _HUD:
        p = hud_panel()
        cap = Image.open(CAPTURE).convert("RGBA")
        _HUD["p"] = p
        _HUD["lit"] = cap.crop((737, 91, 749, 100))
        _HUD["dark"] = cap.crop((906, 91, 918, 100))
        _HUD["numbg"] = cap.crop((902, 62, 924, 86))
    return _HUD["p"].copy()


def cell_img(col, base="lit"):
    """A shield bar cell from the capture's own cell, recoloured (the cyan scale -> col)."""
    a = np.array(_HUD[base]).astype(np.float64)
    k = a[..., 1:2] / 204.0
    a[..., :3] = np.clip(k * np.array(col), 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def hud_frame(v, s, cap, cool, effect, denied=0.0, flash=0.0, over=False):
    p = hud_base()
    c = OC[v]
    # shield number
    for x in (902, 924):
        p.alpha_composite(_HUD["numbg"], (x - HUD_X, 62))
    num = f"{int(round(s))}"
    w = font("body-10x20").width(num)
    text(p, 945 - w, 64, num, c["num"] if over else HUD_WHITE, name="body-10x20")
    # bar: 16 cells
    for k in range(16):
        x = 737 + 13 * k - HUD_X
        if v == "a":
            unit = cap / 16
            lit = s >= (k + 0.5) * unit
            col = (0, 204, 204) if (k + 0.5) * unit <= CAP else c["cell"]
            p.alpha_composite(cell_img(col) if lit else _HUD["dark"], (x, 91))
        else:
            unit = CAP / 16
            lit = min(s, CAP) >= (k + 0.5) * unit
            p.alpha_composite(cell_img((0, 204, 204)) if lit else _HUD["dark"], (x, 91))
            if s - CAP >= (k + 0.5) * unit:          # the second layer: a white core
                ImageDraw.Draw(p).rectangle([x + 3, 93, x + 8, 97], fill=c["cell"] + (255,))
    # special row
    bg = (90, 24, 34) if denied > 0 else ROW_BG
    fill(p, (737, 273, 943, 297), bg)
    text(p, 742, 277, "SPECIAL", HUD_WHITE)
    sprite.paste_center(p, ring_img(v, cool, effect, flash), 812 - HUD_X, 285)
    ready = cool >= 1.0
    text(p, 826, 277, "OVERCHARGE", (200, 255, 220) if ready else NAME_GREEN)
    # READY does not fit after the name (pending): the full lit ring and the bright name say it
    val = "" if ready else f"{int(np.ceil((1 - cool) * O_CD))}s"
    w = font("label-8x12").width(val)
    text(p, 945 - w, 277, val, (255, 120, 120) if denied > 0 else
         (c["col"] if effect > 0 else HUD_WHITE))
    return p


def oc_ship(t):
    return 240 + 34 * np.sin(t * 0.6), 452


def oc_scene(v):
    still = r36.mars_ground(29, craters=5, dunes=0.06)
    haze = Image.new("RGBA", still.size, (200, 140, 100, 40))        # a dusty storm plain
    still.alpha_composite(haze)
    gh = r36.ghost["b"]
    sim = shield_sim(O_T, 1 / OFPS)
    shooters = [lambda t, k=k: (120 + 120 * k + 30 * np.sin(t * 0.8 + k), 110 + 26 * (k % 2))
                for k in range(3)]
    shots = []
    rng = np.random.default_rng(3)
    for j, th in enumerate(HITS):                    # aimed so they land at the hit times
        sp_ = 240.0
        tx, ty = oc_ship(th)
        for _ in range(3):
            ts = th - np.hypot(tx - shooters[j % 3](th)[0], ty - shooters[j % 3](th)[1]) / sp_
        x0, y0 = shooters[j % 3](ts)
        shots.append((ts, th, x0, y0, tx, ty, True))
    for k in range(26):                              # misses for a busier field
        ts = rng.uniform(0, O_T)
        x0, y0 = shooters[k % 3](ts)
        tx, ty = oc_ship(ts)
        tx += rng.choice([-1, 1]) * rng.uniform(50, 110)
        dd = np.hypot(tx - x0, ty - y0)
        shots.append((ts, ts + dd / 240, x0, y0, tx, ty, False))
    frames = []
    for i, (t, s, cap) in enumerate(sim[:int(O_T * OFPS)]):
        f = still.copy()
        sx, sy = oc_ship(t)
        for k, fn in enumerate(shooters):
            x, y = fn(t)
            e3.place(f, gh.get(np.pi / 2, glow=1.0), x, y, "air")
        for ts, th, x0, y0, tx, ty, hit in shots:
            if ts <= t < th or (not hit and ts <= t < th + 0.6):
                u = (t - ts) / (th - ts)
                e3.orb(f, x0 + (tx - x0) * u, y0 + (ty - y0) * u, 4, ring=e3.GOLD[3])
        for k in range(3):                           # Pulse Cannon
            yb = sy - 30 - ((t * 760 + k * 180) % 540)
            for ox in (-5, 5):
                v8.additive(f, asset("pulse-bolt"), sx + ox, yb)
        draw_ship(f, sx, sy, t, pods="micro-missile")
        over = O_AT <= t < O_AT + O_LEN + 0.5
        if over:
            alpha = 1.0 if t < O_AT + O_LEN else 1 - (t - O_AT - O_LEN) / 0.5
            oc_bubble(v, t, t - O_AT, sx, sy, f, alpha)
        if 0 <= t - O_AT < 0.35:
            oc_activation(v, (t - O_AT) / 0.35, sx, sy, f)
        for th in HITS:
            age = t - th
            if 0 <= age < 0.27:
                fr = asset(f"ship-shield_{min(3, int(age * 15))}")
                if O_AT <= th < O_AT + O_LEN:
                    fr = recolor(fr, OC[v]["col"], 1.3)
                v8.additive(f, fr, sx, sy)
        lab = "SHIELD OVERCHARGE  " + ("A: GOLD" if v == "a" else "B: WHITE DOUBLE SHELL")
        label(f, 8, FH - 14, lab, (150, 200, 240))
        cool = 1.0 if t < O_AT else min(1.0, (t - O_AT) / O_CD)
        effect = max(0.0, 1 - (t - O_AT) / O_LEN) if t >= O_AT else 0.0
        den = 1.0 if 0 <= t - DENIED_AT < 0.2 else 0.0
        hud = hud_frame(v, s, cap, cool, effect, denied=den, over=s > CAP + 0.01 or effect > 0)
        out = Image.new("RGBA", (FW + 240, FH))
        out.paste(f, (0, 0))
        out.paste(hud, (FW, 0))
        if den:
            label(out, 8, FH - 28, "SPECIAL PRESSED WHILE COOLING: DENIED", (255, 140, 140))
        frames.append(out)
    return frames, sim


def oc_sheet(v, frames, sim):
    pick = lambda tt: frames[min(len(frames) - 1, int(round(tt * OFPS)))]

    def ship_crop(tt, k=3):
        sx, sy = oc_ship(tt)
        return pick(tt).crop((int(sx - 45), int(sy - 45), int(sx + 45), int(sy + 45)))
    act = [ship_crop(O_AT + dtt) for dtt in (0.0, 0.07, 0.14, 0.27, 0.6)]
    panels = [("ACTIVATION +0 / 0.07 / 0.14 / 0.27 / 0.6 S (3X)", r5.grid(act, 5, scale=3))]
    later = [ship_crop(tt) for tt in (3.1, 5.0, O_AT + O_LEN + 0.15, O_AT + O_LEN + 0.6)]
    panels.append(("A HIT WHILE OVERCHARGED / SHIMMER / END: EXCESS BLEEDS 0.5 S / NORMAL (3X)",
                   r5.grid(later, 4, scale=3)))
    rows = [ring_img(v, 1.0, 0.0), ring_img(v, 0.02, 0.97), ring_img(v, 0.11, 0.38),
            ring_img(v, 0.4, 0.0), ring_img(v, 1.0, 0.0, flash=1.0)]
    panels.append(("RING 24X24 (4X): READY / JUST FIRED / EFFECT 3 S LEFT / COOLING 27S / READY FLASH",
                   r5.grid(rows, 5, scale=4)))
    states = [("READY", hud_frame(v, 45, 45, 1.0, 0.0)),
              ("FIRED: 90 OF 90", hud_frame(v, 90, 90, 0.01, 0.99, over=True)),
              ("AFTER HITS: 60", hud_frame(v, 60, 90, 0.1, 0.55, over=True)),
              ("COOLING 27S", hud_frame(v, 45, 45, 0.4, 0.0)),
              ("DENIED", hud_frame(v, 45, 45, 0.4, 0.0, denied=1.0))]
    cells = []
    for _, p in states:
        c = Image.new("RGBA", (222, 74), raster.SHEET_BG)
        c.alpha_composite(p.crop((10, 60, 232, 102)), (0, 0))
        c.alpha_composite(p.crop((10, 270, 232, 300)), (0, 44))
        cells.append(c)
    panels.append(("HUD (2X): " + " / ".join(n for n, _ in states) + "; BAR " +
                   ("RESCALED TO 0-90, 2ND HALF GOLD" if v == "a" else "KEPT AT 0-45, 2ND LAYER WHITE"),
                   r5.grid(cells, 3, scale=2, pad=6)))
    panels.append(("THE GIF FRAME'S HUD PANEL (1X)", pick(5.0).crop((FW, 0, FW + 240, 320))))
    notes = {
        "a": ["SHIELD OVERCHARGE A - GOLD (THE SPEC'S LOOK)",
              "A GOLD-WHITE FLARE AT ACTIVATION, THEN THE SHIELD RING SHIMMERS GOLD WITH A HIGHLIGHT RUNNING",
              "ROUND IT; HITS RIPPLE GOLD. HUD: THE BAR SHOWS 0-90 WHILE DOUBLED, THE SECOND HALF IN GOLD CELLS,",
              "THE NUMBER IN GOLD. RISK: GOLD IS THE ASCENDANCY'S LIVERY HUE (READABILITY RULE: PLAYER = CYAN)."],
        "b": ["SHIELD OVERCHARGE B - WHITE-HOT DOUBLE SHELL",
              "TWO SHELLS SNAP OUT AT ACTIVATION; WHILE OVERCHARGED A WHITE INNER SHELL AND A FAINT OUTER ONE",
              "WITH CRACKLE RUNNING ROUND IT; HITS RIPPLE WHITE. HUD: THE BAR KEEPS ITS 0-45 SCALE AND THE",
              "SECOND LAYER SHOWS AS WHITE CORES INSIDE THE CYAN CELLS. STAYS IN THE PLAYER'S CYAN/WHITE HUES."]}[v]
    notes += ["BOTH: SHIELD MK III 45 -> 90 AT ONCE, CAPACITY DOUBLED FOR 8 S; HITS DRAIN IT AS USUAL, REGEN (4/S",
              "AFTER 1.5 S) REFILLS TOWARDS 90; AFTER 8 S THE EXCESS BLEEDS AWAY OVER 0.5 S. THE SPECIAL ROW:",
              "SPECIAL, THE ICON IN ITS RING, THE NAME, THE SECONDS LEFT. THE FULL NAME (SHIELD OVERCHARGE 27s)",
              "DOES NOT FIT THE ROW (210 PX): THE MOCK SHOWS OVERCHARGE, AND READY HAS NO ROOM AFTER IT, SO WHEN",
              "READY THE RING IS FULLY LIT AND THE NAME BRIGHT (PENDING). RING: DIM CYAN FILL CLOCKWISE FROM",
              "12 O'CLOCK OVER THE 45 S COOLDOWN, THE EFFECT ARC DRAINS ANTICLOCKWISE OVER 8 S, A WHITE FLASH",
              "AND A SOFT CHIME WHEN READY; A PRESS WHILE COOLING FLASHES THE ROW RED (DENIED, AT 6.0 S IN THE GIF)."]
    return r5.build_sheet(f"SHIELD OVERCHARGE {v.upper()} - DOUBLE CAPACITY 8 S, COOLDOWN RING",
                          panels, notes, pick(5.0).crop((0, 0, FW, FH)), width=1640)


def make_oc(variants):
    for v in variants:
        frames, sim = oc_scene(v)
        save("specials", "shield-overcharge", v, oc_sheet(v, frames, sim), frames, fps=OFPS)


# =========================================================================== main

SUBJECTS = {"ion-beam": make_ion, "light-drone": make_drone,
            "emp-burst": make_emp,
            "shield-overcharge": make_oc}


def main(args):
    variants = ["a", "b"]
    if "--variant" in args:
        k = args.index("--variant")
        variants = [args[k + 1]]
        args = args[:k] + args[k + 2:]
    for name, fn in SUBJECTS.items():
        if not args or name in args:
            fn(variants)


if __name__ == "__main__":
    main(sys.argv[1:])
