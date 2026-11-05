#!/usr/bin/env python3
"""Concept round 84 - the new Act 7 "Beyond the Gate" units, a/b proposals (step ART-R-A, M11 design phase).

Each subject gets a PNG sheet (the source render, the sprites at play size with their states, the mechanic's
key frames, the readability check over its level's brightest and darkest band with the player for scale, a
frame from the play field at 1x) and a GIF of it in the 480x540 play field over its level's mood. Models are
built in this file on the frozen ``render`` package (sdf, enemy_rigs, enemy_models), with enemies_r60.py's and
enemies_r72.py's helpers. The looks are the enemies README's *Beyond the gate* rows (Q10 a, pending the user's
review in round 84): **gate stone** (basalt 3A3640 / 16141A / 8C8494, crimson glyphs FF3038, a pale gold-white
shell), **Vrell elite** (bone DCCFB4 / 5C4A5C / F4ECD8 lit gold-white F2DC9A, violet tells), **Silence-touched**
(ash grey-brown 6E645C / 2A2622 / B4AAA0, no glow, pale cold crystal B8C8D4); the Rift Skater keeps the rust
role colours (fast attackers) with the violet aimed-shot core; the spawning organ the spawners' teal glow.

Every glow, haze, ring and beam is smooth and drawn additively on the frame (gaussian falloffs, >= 32 steps via
``smooth_steps``, fading to 0 inside their crop); no ordered dither; the backdrops are unquantised. Vrell bullets
are the act's larger, brighter, outlined ones (a 2 px dark outline round the ring and the white core).

The level backdrops are mood stand-ins for the readability checks (ART-R-B owns the mood swatches): L43 the
gate's dark stone ring with cold teal-white glyphs under the violet and teal nebula, L44 the event plane (the
nebula inverted, pale violet-white with dark violet streaks, sliding along the drift), L45 the hive reef (dark
chitin plates, glowing pink veins, warm pits), L46 the graveyard (grey-brown, pale flat scenery hulks, one cold
white light from the top left, a starless patch), L47 the inner sphere (bone ribs, greyer and a step darker
than the Seraph, warm gold-white light).

Outputs (design/..., each <slug>-r84-{a,b}.{png,gif}):
  enemies/space/gate-warden        a "idol": a squat round basalt sentinel ringed by a glyph band, the shell a
                                   six-petal egg with gold seams / b "keystone": a tapered block like a piece of
                                   the gate ring with two glyph columns, the shell a seamless pearl egg; closed
                                   (shots spark off), the window (telegraph 0.8 s + beam 1.0 s), an aimed line,
                                   the windows in turn, the Tesla arc holding it open, the death (L43)
  enemies/space/rift-skater        a "manta": a rounded rust chitin manta with a thin crystal rim, the arrival
                                   ring shrinking onto the landing point / b "shard": a sharp crystal diamond
                                   with wide crystal wing panels, the ring growing out of the point; a stream
                                   riding the drift, the jumps (fade, ring, landing), the bursts of 3, kills (L44)
  enemies/space/husk               a "punctured": an ovoid hull punched open from inside, the plates curled out
                                   round round holes / b "split": a hull torn along its spine, plates peeled
                                   back, ribs across the gap; three hull shapes each; the quiet, the crack tell,
                                   the fan of 5 shards, the death ring of 8, the level's cue crack (L46)
  enemies/air/choir-seraph         a "blades": two pairs of long swept blade wings / b "halo": feathered wings and
                                   the Herald's halo ring over its back; the pair's arrival rings, the violet
                                   flare, the mirror and its two-arm spiral, the fold and jump, a kill (L47)
  campaign/act-7-beyond-the-gate/level-45-hive-reef   spawning-organ: a "teal bulb" (the Hive Node's teal-black
                                   spawner flesh, one seed pod on its crown, an iris on its front) / b "reef
                                   polyp" (the reef's pink flesh, a ring of six seed pods round a crown iris);
                                   the swell and the Whirl Seed cluster, the 120 px hold, the tear, the amber
                                   leaving blink (L45)

Run: python3 tools/concept/enemies_r84.py [unit ...] [a|b]   (no args = everything; the units render in this
process, so run them as separate processes to use more cores)
"""
import sys
import time
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import enemies_r60 as e60  # noqa: E402
import enemies_r72 as e72  # noqa: E402
import gear_r36 as g36  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import hx  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_ellipsoid, sd_plate, sd_polygon2,  # noqa: E402
                        sd_sphere, union)

FW, FH, FPS, TAU = r5.FW, r5.FH, r5.FPS, 2 * np.pi
DOWN, UP, RIGHT, LEFT = np.pi / 2, -np.pi / 2, 0.0, np.pi
DESIGN = ROOT / "design"
ROUND = "r84"
SUB = "CONCEPT ROUND 84 - 960X540"

times, smooth, light, fade, tint = e60.times, e60.smooth, e60.light, e60.fade, e60.tint
caption, spark, keyframes, grid = e60.caption, e60.spark, e60.keyframes, e60.grid
smooth_steps, ring_light, boom, Fire, bands_check = (e72.smooth_steps, e72.ring_light, e72.boom, e72.Fire,
                                                     e72.bands_check)
seg_dist2, rot_sprite = e72.seg_dist2, e72.rot_sprite

CRIMSON = (255, 48, 56)
VIOLET = (154, 77, 255)
VIOLET_W = (226, 206, 255)
TEAL = (0, 255, 154)
GOLD_WHITE = (242, 220, 154)
GW_F = np.array(GOLD_WHITE) / 255.0
CRYSTAL = (184, 200, 212)
AMBER = (255, 176, 40)
OUTLINE = (12, 8, 18)


# =========================================================================== small helpers

def vorb(img, x, y, r=4, col=VIOLET):
    """A Vrell bullet beyond the gate: larger, brighter, outlined (2 px dark outline, ring, white core)."""
    d = ImageDraw.Draw(img)
    d.ellipse([x - r - 2, y - r - 2, x + r + 2, y + r + 2], fill=OUTLINE + (255,))
    d.ellipse([x - r, y - r, x + r, y + r], fill=col + (255,))
    d.ellipse([x - r + 2, y - r + 2, x + r - 2, y + r - 2], fill=(255, 255, 255, 255))


def needle(img, x, y, ang, col=CRYSTAL, ln=8.0, w=3.0):
    """A crystal shard bullet (the fast needle, shard-shaped), dark outline, white spine."""
    c, s = np.cos(ang), np.sin(ang)

    def poly(L, W):
        return [(x + c * L, y + s * L), (x - s * W, y + c * W), (x - c * L * 0.7, y - s * L * 0.7),
                (x + s * W, y - c * W)]
    d = ImageDraw.Draw(img)
    d.polygon(poly(ln + 2, w + 2), fill=OUTLINE + (255,))
    d.polygon(poly(ln, w), fill=col + (255,))
    d.line([(x - c * ln * 0.5, y - s * ln * 0.5), (x + c * ln * 0.8, y + s * ln * 0.8)], fill=(255, 255, 255, 255))


class VShots:
    """Straight Vrell shots drawn by ``kind`` ('orb' or 'needle'), screen space."""

    def __init__(self, col=VIOLET, kind="orb", r=4):
        self.list, self.col, self.kind, self.r = [], col, kind, r

    def fire(self, t0, x, y, ang, speed):
        self.list.append((t0, x, y, np.cos(ang) * speed, np.sin(ang) * speed))

    def ring(self, t0, x, y, n, speed, phase=0.0):
        for a in np.linspace(0, TAU, n, endpoint=False) + phase:
            self.fire(t0, x, y, a, speed)

    def fan(self, t0, x, y, aim, n, step, speed):
        for k in range(n):
            self.fire(t0, x, y, aim + (k - (n - 1) / 2) * step, speed)

    def draw(self, img, t):
        for t0, x, y, vx, vy in self.list:
            if t >= t0:
                bx, by = x + vx * (t - t0), y + vy * (t - t0)
                if -20 < bx < FW + 20 and -20 < by < FH + 20:
                    if self.kind == "needle":
                        needle(img, bx, by, np.arctan2(vy, vx), self.col)
                    else:
                        vorb(img, bx, by, self.r, self.col)


def flakes(img, x, y, s, n, col, seed=0, r0=4, r1=34, size=2.0, fall=18.0):
    """Small diamond flakes flung out and fading (shell flakes, crystal splinters, feathers)."""
    if not 0 <= s < 1:
        return img
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(img)
    for _ in range(n):
        a, sp, sz = rng.uniform(0, TAU), rng.uniform(0.4, 1.0), size * rng.uniform(0.6, 1.2)
        r = r0 + (r1 - r0) * sp * np.sqrt(s)
        px, py = x + np.cos(a) * r, y + np.sin(a) * r + fall * s * s
        al = int(255 * (1 - s) ** 0.8)
        d.polygon([(px - sz, py), (px, py - sz * 0.6), (px + sz, py), (px, py + sz * 0.6)], fill=tuple(col) + (al,))
    return img


def boost(sp, mask_fn, col, k):
    """``sp`` with the pixels ``mask_fn`` picks (0..1) brightened toward ``col`` by ``k`` (a glow's strength)."""
    if k == 0:
        return sp
    a = np.array(sp).astype(np.float64)
    m = mask_fn(a) * (a[..., 3] > 0)
    a[..., :3] = np.clip(a[..., :3] + smooth_steps(np.clip(m * abs(k), 0, 1))[..., None] * np.array(col) * np.sign(k),
                         0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def sweep(sp, ph, col=(255, 248, 226), k=0.45, w=0.07):
    """A specular band sweeping diagonally over a sprite's silhouette (``ph`` 0..1 across it)."""
    a = np.array(sp).astype(np.float64)
    h, wd = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:wd]
    u = (xx / wd + yy / h) / 2
    f = np.exp(-((u - (ph * 1.4 - 0.2)) / w) ** 2) * k * (a[..., 3] > 0)
    a[..., :3] = np.clip(a[..., :3] + smooth_steps(f)[..., None] * np.array(col), 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def outline(sp, col=(16, 12, 18), k=0.85):
    """A 1 px dark outline round a sprite's silhouette (the dark edge on a pale ground)."""
    a = np.array(sp)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    ring = (pad[1:-1, :-2] | pad[1:-1, 2:] | pad[:-2, 1:-1] | pad[2:, 1:-1]) & ~op
    a[ring] = tuple(col) + (int(255 * k),)
    return Image.fromarray(a, "RGBA")


def sd_torus(p, c, R, r):
    """A torus in the XY plane round ``c``."""
    return np.hypot(np.hypot(p[:, 0] - c[0], p[:, 1] - c[1]) - R, p[:, 2] - c[2]) - r


class Fixed:
    """A non-turning model facing down (count 4, so heading DOWN picks the facing-down frame)."""

    def __init__(self, make, size, extent, factor=4, colors=40, post=None):
        self.s = rig.ModelSpaceAngleSprites(make, size, 4, extent=extent, factor=factor, colors=colors, post=post)

    def get(self, **kw):
        return self.s.get(DOWN, **kw)


def source_panel(make, extent, disp=260):
    scene, mats = make()
    r = rig.model_rotation(DOWN)
    hi = sdf.render(lambda p: scene(sdf.rotate_z(p, r)), rig.model_space_materials(mats, r), (disp, disp), extent)
    cell = e3.checker(disp, disp)
    cell.alpha_composite(sprite.to_image(hi))
    return cell


def ship(img, px, py):
    sprite.paste_center(img, e3.player_sprite(), px, py)


def move(cur, tgt, vmax, lag=0.25):
    """One frame of the ship easing toward a target (x, y), at most ``vmax`` px/s."""
    dx, dy = tgt[0] - cur[0], tgt[1] - cur[1]
    vx, vy = dx / lag, dy / lag
    v = np.hypot(vx, vy)
    if v > vmax:
        vx, vy = vx * vmax / v, vy * vmax / v
    return [cur[0] + vx / FPS, cur[1] + vy / FPS]


def path_at(keys, t):
    """Linear interpolation of [(t, x, y)] keys."""
    ts = [k[0] for k in keys]
    if t <= ts[0]:
        return keys[0][1:]
    for (t0, x0, y0), (t1, x1, y1) in zip(keys, keys[1:]):
        if t <= t1:
            u = smooth((t - t0) / max(1e-6, t1 - t0))
            return (x0 + (x1 - x0) * u, y0 + (y1 - y0) * u)
    return keys[-1][1:]


def save(subdir, slug, v, sheet_img, frames=None):
    d = DESIGN / subdir / "concept"
    d.mkdir(parents=True, exist_ok=True)
    png = d / f"{slug}-{ROUND}-{v}.png"
    sheet_img.convert("RGB").save(png, optimize=True)
    msg = f"wrote {png.relative_to(ROOT)}"
    if frames:
        size = rig.write_gif(frames, d / f"{slug}-{ROUND}-{v}.gif", fps=FPS)
        msg += f" and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)"
    print(msg, flush=True)


def sheet(title, panels, notes, game_frame, width=1300):
    r5.SUB = SUB
    return r5.build_sheet(title, panels, notes, game_frame, width)


def row(items, size, scale=2, labels=None, bg=None):
    return e60.row(items, size, scale, labels, bg)


UNITS = {}


def unit(name):
    def deco(fn):
        UNITS[name] = fn
        return fn
    return deco


# =========================================================================== Vrell space moods (smooth, 1x)

def _arr(rgb):
    return Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def _ramp(t, stops):
    return raster.ramp([(k, c if isinstance(c, tuple) else raster.hexrgb(c)) for k, c in stops], np.clip(t, 0, 1))


def _fbm(w, h, cell, seed, octaves=5):
    return raster.fbm(w, h, cell, seed, octaves=octaves, period=False)


def _stars(w, h, seed, density, k=0.8):
    return np.array(raster.starfield(w, h, seed, density=density)).astype(np.float64)[..., :3] * k


def gate_bg(seed, h=FH):
    """Level 43: deep space under the violet and teal nebula, the Tether Gate's dark stone ring crossing the field
    (a step darker and less saturated than the Wardens) with faint cold teal-white glyphs that never pulse, the
    fleet's white running lights far behind."""
    w = FW
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    n1, n2 = _fbm(w, h, 128, seed + 1), _fbm(w, h, 96, seed + 2)
    rgb = np.zeros((h, w, 3)) + (6, 5, 12)
    rgb += np.clip((n1 - 0.42) * 1.9, 0, 1)[..., None] * np.array((74, 28, 100))
    rgb += np.clip((n2 - 0.55) * 2.2, 0, 1)[..., None] * np.array((8, 70, 78))
    rgb += _stars(w, h, seed + 3, 0.0016)
    cx, cy, R, half = -760.0, h * 0.55, 1060.0, 72.0
    dr = np.hypot(xx - cx, yy - cy) - R
    band = np.clip(half - np.abs(dr) + 0.5, 0, 1)
    tex = _fbm(w, h, 18, seed + 4, 4)
    u = dr / half                                        # -1 inner edge .. 1 outer edge
    bevel = np.clip(1 - np.abs(u) ** 6, 0, 1)
    shade = 0.55 + 0.35 * (-u) + 0.25 * (tex - 0.5)      # the inner (left) face catches the light
    stone = _ramp(np.clip(0.22 + 0.4 * shade * bevel, 0, 1), [(0, "0c0b0f"), (0.4, "1d1b22"), (0.75, "2c2932"),
                                                             (1, "48444e")])
    ang = np.arctan2(yy - cy, xx - cx)
    for rr in (-40.0, 38.0):                              # two glyph rows along the ring
        cell = ang * R / 13.0
        ci = np.floor(cell)
        hsh = (np.sin(ci * 12.9898 + rr) * 43758.5453) % 1.0
        fu = cell - ci
        mark = (np.abs(dr - rr) < 2.6) & (np.abs(fu - 0.5) < 0.2) & (hsh > 0.3)
        mark |= (np.abs(dr - rr) < 0.9) & (hsh > 0.75)
        stone = np.where(mark[..., None], stone * 0.6 + np.array((120, 196, 190)) * 0.26, stone)
    seams = (np.abs(((ang * R) % 90.0) - 45) > 43.8) & (band > 0)
    stone = np.where(seams[..., None], stone * 0.55, stone)
    rgb = rgb * (1 - band[..., None]) + stone * band[..., None]
    shadow = np.clip(1 - (dr - half) / 26, 0, 1) * (dr > half)
    rgb *= (1 - 0.5 * shadow)[..., None]
    img = _arr(rgb)
    rng = np.random.default_rng(seed + 5)
    for _ in range(int(h / 140)):                         # the fleet far behind: white running lights
        x, y = rng.uniform(300, 470), rng.uniform(0, h)
        light(img, x, y, 1.6, (235, 240, 255), 0.9)
    return img


EV_DRIFT = np.radians(25)                                # the scroll turned 25 degrees: the current flows down-right
EV_SPEED = 60.0


class EventPlane:
    """Level 44: the event plane, the nebula inverted (pale violet-white, dark violet streaks along the flow) in
    two layers that slide along the drift at their own parallax (deep 0.5, near 1.0)."""

    def __init__(self, seed, dur, drift=EV_DRIFT, speed=EV_SPEED):
        self.v = np.array((np.sin(drift), np.cos(drift))) * speed
        self.drift = drift
        self.deep = self._layer(seed, dur, 0.5, deep=True)
        self.near = self._layer(seed + 10, dur, 1.0, deep=False)
        self.img = self.deep[0]

    def _layer(self, seed, dur, f, deep):
        mx, my = abs(self.v[0]) * dur * f, self.v[1] * dur * f
        w, h = int(FW + mx + 12), int(FH + my + 12)
        big = int(np.hypot(w, h)) + 8
        streak = Image.fromarray((np.clip(_fbm(big, big // 6, 24, seed, 4), 0, 1) * 255).astype(np.uint8), "L")
        streak = streak.resize((big, big), Image.BICUBIC).rotate(np.degrees(self.drift), Image.BICUBIC)
        x0, y0 = (big - w) // 2, (big - h) // 2
        s = np.array(streak.crop((x0, y0, x0 + w, y0 + h))).astype(np.float64) / 255.0
        if deep:
            n = _fbm(w, h, 150, seed + 1)
            v = np.clip(0.25 + 0.75 * n, 0, 1)
            rgb = _ramp(v, [(0, (88, 60, 128)), (0.45, (150, 128, 192)), (0.8, (196, 184, 224)), (1, (214, 204, 236))])
            dark = np.clip((s - 0.52) * 2.4, 0, 1)
            rgb = rgb * (1 - 0.55 * dark[..., None]) + np.array((44, 24, 74)) * 0.55 * dark[..., None]
            img = _arr(rgb)
        else:
            al = np.clip((s - 0.62) * 2.5, 0, 1) * 0.4
            a = np.zeros((h, w, 4))
            a[..., :3] = (40, 20, 70)
            a[..., 3] = al * 255
            img = Image.fromarray(a.astype(np.uint8), "RGBA")
        return img, mx, my, f

    def frame(self, t):
        out = None
        for img, mx, my, f in (self.deep, self.near):
            ox, oy = self.v[0] * t * f, self.v[1] * t * f
            x = int(round((mx + 6) - ox)) if self.v[0] >= 0 else int(round(6 + ox))
            y = int(round((my + 6) - oy))
            c = img.crop((x, y, x + FW, y + FH))
            if out is None:
                out = c.copy()
            else:
                out.alpha_composite(c)
        return out


def reef_bg(seed, h=FH):
    """Level 45: the hive reef, dark chitin-brown plates with a pink tinge, pink veins glowing in the plate gaps
    (slow, never bullet-sized dots), the pits' warm glow, a pink-violet spore haze; a step darker than the units."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    n = int(w * h / 4200)
    pts = np.c_[rng.uniform(-20, w + 20, n), rng.uniform(-20, h + 20, n)]
    d1, d2 = np.full((h, w), 1e9), np.full((h, w), 1e9)
    idx = np.zeros((h, w), dtype=int)
    for i, (px, py) in enumerate(pts):
        d = np.hypot(xx - px, yy - py)
        closer = d < d1
        d2 = np.where(closer, d1, np.minimum(d2, d))
        idx = np.where(closer, i, idx)
        d1 = np.where(closer, d, d1)
    e = (d2 - d1) / 2
    g = np.clip(e / 14.0, 0, 1)
    tone = rng.uniform(0.75, 1.1, n)[idx]
    tex = _fbm(w, h, 28, seed + 2, 2)
    hgt = np.sqrt(1 - (1 - g) ** 2) * (0.9 + 0.1 * tex)
    gy, gx = np.gradient(hgt * 20)
    shade = np.clip(0.75 + (-gx - gy) * 0.9, 0.35, 1.3)
    col = _ramp(np.clip(0.15 + 0.6 * hgt * tone, 0, 1), [(0, "100709"), (0.4, "2c161a"), (0.75, "4e2c30"),
                                                         (1, "6e464a")]) * shade[..., None]
    vein = np.exp(-(e / 1.6) ** 2) * (0.55 + 0.45 * _fbm(w, h, 40, seed + 3, 3))
    col += smooth_steps(np.clip(vein, 0, 1))[..., None] * np.array((150, 46, 96)) * 0.75
    for _ in range(int(h / 160)):                         # the pits: dark holes with a warm glow
        cx, cy, r = rng.uniform(40, w - 40), rng.uniform(0, h), rng.uniform(12, 18)
        dd = np.hypot(xx - cx, yy - cy)
        col *= (1 - 0.8 * np.clip(1 - dd / r, 0, 1) ** 0.5)[..., None]
        col += (np.exp(-(dd / (r * 1.3)) ** 2) * 0.5)[..., None] * np.array((255, 120, 90))
    haze = np.clip((_fbm(w, h, 110, seed + 4) - 0.5) * 1.6, 0, 1) * 0.22
    col = col * (1 - haze[..., None]) + np.array((150, 90, 170)) * haze[..., None]
    return _arr(col)


def graveyard_bg(seed, h=FH):
    """Level 46: the dead fleet, desaturated grey-brown, the scenery hulks paler and flatter than a Husk, one cold
    white light from the top left, sparse stars and a starless patch."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rgb = np.zeros((h, w, 3)) + (12, 11, 10)
    rgb += np.clip((_fbm(w, h, 140, seed + 1) - 0.45) * 1.2, 0, 1)[..., None] * np.array((34, 30, 28))
    st = _stars(w, h, seed + 2, 0.0012, 0.6)
    patch = np.clip((np.hypot(xx - 360, (yy % 900) - 300) - 70) / 30, 0, 1)
    rgb += st * patch[..., None]
    tex = _fbm(w, h, 14, seed + 3, 4)
    for _ in range(int(h / 230) + 1):                     # scenery hulks: pale, flat, low contrast
        cx, cy = rng.uniform(-40, w + 40), rng.uniform(0, h)
        a, L, W = rng.uniform(0, np.pi), rng.uniform(150, 250), rng.uniform(28, 46)
        c, s = np.cos(a), np.sin(a)
        u, v = ((xx - cx) * c + (yy - cy) * s) / L, (-(xx - cx) * s + (yy - cy) * c) / W
        r = np.abs(u) ** 2.5 + np.abs(v) ** 2 + 0.35 * (_fbm(w, h, 20, seed + 9, 3) - 0.5)
        on = np.clip((1 - r) * 8, 0, 1)
        plate = (np.abs(((u * 6) % 1.0) - 0.5) > 0.46) | (np.abs(((v * 3) % 1.0) - 0.5) > 0.45)
        lit = np.clip(0.62 - 0.25 * (u * c + v * s) + 0.15 * (tex - 0.5), 0.3, 0.9)
        hull = _ramp(lit, [(0, "58534e"), (0.5, "6e6862"), (1, "8a847c")]) * (1 - 0.08 * plate)[..., None]
        rgb = rgb * (1 - on[..., None]) + hull * on[..., None]
    cold = np.clip(1 - (xx / w * 0.8 + 0.3 + 0.25 * np.sin(yy / 260.0)), 0, 1) ** 2
    rgb += cold[..., None] * np.array((40, 46, 54))
    return _arr(rgb)


def sphere_bg(seed, h=FH):
    """Level 47: the inner sphere, a vault of pale bone ribs (greyer and a step darker than the Seraph) curving
    across the field, lit warm gold-white from the core ahead."""
    w = FW
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    warp = (_fbm(w, h, 160, seed, 3) - 0.5) * 24
    arc = yy + warp - 70 * ((xx - 240) / 240) ** 2
    ph = (arc / 70.0) % 1.0
    rib = np.exp(-((ph - 0.5) / 0.16) ** 2)                    # a rounded rib, lit on its upper flank
    lit = np.exp(-((ph - 0.4) / 0.08) ** 2)
    groove = np.exp(-(np.minimum(ph, 1 - ph) / 0.07) ** 2)
    tex = _fbm(w, h, 40, seed + 2, 2)
    v = np.clip(0.42 + 0.3 * rib + 0.18 * lit - 0.3 * groove + 0.08 * (tex - 0.5), 0, 1)
    rgb = _ramp(v, [(0, "4a463e"), (0.35, "7c776a"), (0.7, "a8a290"), (1, "c4bca6")])
    glow = smooth_steps(np.clip((_fbm(w, h, 180, seed + 3, 3) - 0.4) * 1.6, 0, 1))
    rgb += glow[..., None] * np.array((60, 50, 22))
    return _arr(rgb)


class Scroll(e60.Scroll):
    pass


# ===================================================================== 1 Gate Warden (L43)

W_S = 38.0                           # px per model unit
W_N = (120, 124)
W_EXT = W_N[0] / W_S
A_ST, A_STD, A_STE, A_GLY, A_EYE, A_SH, A_SEAM = range(7)
SH_R = (0.98, 1.08, 0.86)            # the closed shell: an ovoid 74 x 82 px round the body


def glyph_pat(cell=0.11, seed=1.0):
    """Gate glyphs: one short stroke, cross or dot per cell (model space)."""
    def f(p, n):
        gx, gy = p[:, 0] / cell, p[:, 1] / cell
        ix, iy = np.floor(gx), np.floor(gy)
        fx, fy = gx - ix - 0.5, gy - iy - 0.5
        h = (np.sin(ix * 12.9898 + iy * 78.233 + seed) * 43758.5453) % 1.0
        hz = (np.abs(fy) < 0.14) & (np.abs(fx) < 0.36)
        vt = (np.abs(fx) < 0.14) & (np.abs(fy) < 0.36)
        dot = np.hypot(fx, fy) < 0.18
        m = np.where(h < 0.3, hz, np.where(h < 0.6, vt, np.where(h < 0.85, hz | vt, dot)))
        return 0.15 + 0.85 * m
    return f


def warden_mats(glow=1.0):
    cr = np.array(CRIMSON) / 255.0
    return [Material(hx("3a3640"), metal=0.05, shininess=24, spec=0.3, pattern=em._plates(8, 0.06, 0.75)),
            Material(hx("16141a"), metal=0.05, shininess=18, spec=0.2),
            Material(hx("8c8494"), metal=0.1, shininess=40, spec=0.5),
            Material(hx("1e1418"), shininess=20, spec=0.2, emission=tuple(cr * 1.8 * glow),
                     emission_pattern=glyph_pat()),
            Material((0.06, 0.02, 0.02), shininess=140, spec=1.3, emission=tuple(cr * (0.35 + 1.2 * glow))),
            Material(hx("efe3c2"), metal=0.3, shininess=200, spec=1.4),
            Material(hx("b8902e"), metal=0.7, shininess=120, spec=1.0)]


def warden_body(v):
    """a "idol": a squat round basalt sentinel, a glyph band round its waist set with pale studs, a low crown,
    the single eye on top; b "keystone": a tapered stone block like a piece of the gate ring, two glyph columns
    down its face, a lintel, shoulder stones, the eye in the middle."""
    def scene(p):
        if v == "a":
            items = [(sd_ellipsoid(p, (0, 0, 0.04), (0.74, 0.8, 0.36)), A_ST),
                     (sd_torus(p, (0, 0, 0.26), 0.62, 0.1), A_GLY),
                     (sd_ellipsoid(p, (0, 0, 0.34), (0.44, 0.48, 0.28)), A_ST),
                     (sd_torus(p, (0, 0, 0.58), 0.18, 0.05), A_STD),
                     (sd_sphere(p, (0, 0, 0.55), 0.15), A_EYE)]
            for k in range(8):
                a = k * TAU / 8 + TAU / 16
                items.append((sd_box(p, (0.64 * np.cos(a), 0.66 * np.sin(a), 0.3), (0.07, 0.07, 0.07), 0.02), A_STE))
            d, m = union(*items, k=0.02)
            m = np.where((m == A_ST) & (p[:, 2] < 0.12), A_STD, m)
            return d, m
        q = mirror_x(p)
        key = [(0.0, -0.72), (0.6, -0.72), (0.42, 0.7), (0.0, 0.7)]
        col = [(0.26, -0.56), (0.36, -0.56), (0.3, 0.56), (0.22, 0.56)]
        d, m = union((sd_plate(q, key, 0.1, 0.22, 0.05), A_ST),
                     (sd_plate(q, col, 0.33, 0.035, 0.0), A_GLY),
                     (sd_box(q, (0.68, -0.4, 0.04), (0.09, 0.2, 0.14), 0.03), A_STD),
                     (sd_box(p, (0, -0.64, 0.33), (0.5, 0.06, 0.05), 0.02), A_STE),
                     (sd_torus(p, (0, 0.02, 0.36), 0.17, 0.05), A_STD),
                     (sd_sphere(p, (0, 0.02, 0.33), 0.15), A_EYE), k=0.01)
        m = np.where((m == A_ST) & (np.abs(q[:, 0] - 0.5 + 0.12 * q[:, 1]) < 0.04), A_STD, m)
        return d, m
    return scene


def shell_scene(v, open_=0.0):
    """The six-petal shell. Closed it is a smooth pale gold-white ovoid round the body (a: gold seams between the
    petals; b: seamless pearl); opening, each petal hinges outward at its base, 68 degrees fully open."""
    th = np.radians(68) * open_
    beta = np.radians(30) - np.radians(0.6 if v == "b" else 0.0)
    ct, st, sb, cb = np.cos(th), np.sin(th), np.sin(beta), np.cos(beta)

    def petal(p, ak):
        c, s = np.cos(ak), np.sin(ak)
        lx, ly, lz = p[:, 0] * c + p[:, 1] * s, -p[:, 0] * s + p[:, 1] * c, p[:, 2]
        rb = 1 / np.sqrt((c / SH_R[0]) ** 2 + (s / SH_R[1]) ** 2)
        qx = (lx - rb) * ct - lz * st + rb
        qz = (lx - rb) * st + lz * ct
        P = np.stack([qx * c - ly * s, qx * s + ly * c, qz], axis=-1)
        d = np.abs(sd_ellipsoid(P, (0, 0, 0), SH_R)) - 0.045
        wedge = np.maximum(-qx * sb + ly * cb, -qx * sb - ly * cb)
        d = np.maximum(np.maximum(d, wedge), -qz)
        m = np.where(wedge > -0.035, A_SEAM, A_SH) if v == "a" else np.full(len(p), A_SH)
        return d, m

    def scene(p):
        return union(*[petal(p, k * TAU / 6 + TAU / 4) for k in range(6)])
    return scene


def warden_make(v, part, open_=0.0, glow=0.6):
    body, shell = warden_body(v), shell_scene(v, open_)
    if part == "body":
        return body, warden_mats(glow)
    if part == "shell":
        return shell, warden_mats(glow)
    return (lambda p: union(body(p), shell(p))), warden_mats(glow)


OPEN_STEPS = (0.0, 0.34, 0.67, 1.0)
w_body = {v: Fixed(lambda v=v, glow=0.6: warden_make(v, "body", glow=glow), W_N, W_EXT, 4, 44) for v in "ab"}
w_shell = {v: Fixed(lambda v=v, open_=0.0: warden_make(v, "shell", open_=open_), W_N, W_EXT, 4, 40) for v in "ab"}


def glyph_mask(a):
    r, g = a[..., 0], a[..., 1]
    return np.clip((r - 1.7 * g - 30) / 80, 0, 1)


def warden_sprite(v, open_=0.0, glow=0.6, shell_alpha=1.0, regrow=None, sweep_ph=None):
    """The body (its glyphs brightened by ``glow`` 0..1.4) under the shell at ``open_``; ``shell_alpha`` < 1 with
    ``regrow`` (0..1) the shell growing back behind the regrowth shimmer."""
    body = boost(w_body[v].get(), glyph_mask, (255, 120, 90), (glow - 0.6) * 1.1)
    sp = body.copy()
    if shell_alpha > 0:
        o = min(OPEN_STEPS, key=lambda s: abs(s - open_))
        sh = w_shell[v].get(open_=o)
        if sweep_ph is not None and o == 0.0:
            sh = sweep(sh, sweep_ph)
        if regrow is not None:
            sh = sweep(sh, 1 - regrow, col=(255, 236, 190), k=0.9, w=0.09)
        sp.alpha_composite(fade(sh, shell_alpha))
    return sp


def eye_glow(img, x, y, aim, k):
    """The eye burning crimson, its pupil turned toward ``aim`` (radians)."""
    if k <= 0:
        return img
    ex, ey = x + np.cos(aim) * 2.5, y + np.sin(aim) * 2.5
    img = light(img, ex, ey, 6, CRIMSON, 0.55 * k)
    return light(img, ex, ey, 2.2, (255, 220, 200), 0.9 * k)


def laser(img, x0, y0, ang, mode, k=1.0):
    """A Warden's ``laser-line`` from its eye: ``tele`` the thin crimson line brightening, ``beam`` the 10 px live
    band (crimson with a white-hot core); additive and smooth."""
    if k <= 0:
        return img
    a = np.array(img).astype(np.float64)
    yy, xx = np.mgrid[0:FH, 0:FW] + 0.5
    dx, dy = np.cos(ang), np.sin(ang)
    s = (xx - x0) * dx + (yy - y0) * dy
    d = np.abs(-(xx - x0) * dy + (yy - y0) * dx)
    on = np.clip((s - 6) / 8, 0, 1)
    if mode == "tele":
        f = (np.exp(-(d / 0.75) ** 2) * (0.35 + 0.65 * k) + np.exp(-(d / 3) ** 2) * 0.12 * k) * on
        a[..., :3] += smooth_steps(np.clip(f, 0, 1))[..., None] * np.array(CRIMSON)
    else:
        band = np.exp(-(d / 5.2) ** 2) * 1.1 + np.exp(-(d / 11) ** 2) * 0.3
        core = np.exp(-(d / 2.0) ** 2)
        a[..., :3] += (smooth_steps(np.clip(band * k * on / 1.4, 0, 1)) * 1.4)[..., None] * np.array(CRIMSON)
        a[..., :3] += smooth_steps(np.clip(core * k * on, 0, 1))[..., None] * np.array((255, 236, 220))
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def slabs(img, sp, x, y, s, seed=0):
    """The body splitting into three slabs that fall and fade (presentation)."""
    if not 0 <= s < 1:
        return img
    a = np.array(sp)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    ang = np.arctan2(yy - h / 2, xx - w / 2)
    for k in range(3):
        a0 = -np.pi + k * TAU / 3 + 0.4
        m = ((ang - a0) % TAU) < TAU / 3
        piece = a.copy()
        piece[~m, 3] = 0
        mid = a0 + TAU / 6
        im = rot_sprite(Image.fromarray(piece, "RGBA"), (k - 1) * 40 * s)
        px, py = x + np.cos(mid) * 26 * s, y + np.sin(mid) * 18 * s + 60 * s * s
        sprite.paste_center(img, fade(tint(im, (30, 28, 34), 0.35 * s), (1 - s) ** 1.2), px, py)
    return img


WD_DUR = 10.6
WARDENS = [dict(x=84, y=140, aim="down", tele=[2.0, 6.0]),
           dict(x=240, y=150, aim="aimed", tele=[3.3, 7.3], kill=7.65),
           dict(x=396, y=140, aim="down", tele=[4.6, 8.6], kill=9.25)]
WD_SHIP = [(0.0, 240, 470), (1.9, 240, 470), (3.25, 200, 460), (3.6, 262, 440), (5.2, 262, 440), (7.25, 290, 460),
           (7.45, 232, 440), (7.75, 232, 440), (8.2, 372, 300), (9.4, 372, 300), (10.6, 320, 430)]
WD_FIRE = [(0.0, 2.0), (3.5, 5.2), (7.35, 7.7), (8.25, 9.3)]
TESLA = (8.2, 9.25)


def wd_scene(v):
    bg = Scroll(gate_bg, 43, WD_DUR, 30.0)
    st = [dict(d, hp=1.0, aim_ang=DOWN, dead=None, tesla=-9.0) for d in WARDENS]
    fire = Fire(rate=10)
    hits, frames = [], []
    sp_ = [240.0, 470.0]
    for t in times(WD_DUR):
        f = bg.frame(t)
        sp_ = list(path_at(WD_SHIP, t))
        px, py = sp_
        for w in st:
            w["cy"] = min(w["y"], w["y"] - 100 + 90 * t)
            w["window"] = None
            for t0 in w["tele"]:
                if t0 <= t < t0 + 1.8:
                    w["window"] = t - t0
                if abs(t - t0) < 0.5 / FPS and w["aim"] == "aimed":
                    w["aim_ang"] = np.arctan2(py - w["cy"], px - w["x"])
            if TESLA[0] <= t < TESLA[1] and w is st[2]:
                if int((t - TESLA[0]) * FPS) % 5 == 0:
                    w["tesla"] = t
            if w.get("kill") and t >= w["kill"] and w["dead"] is None:
                w["dead"] = t
            w["open"] = w["dead"] is None and (w["window"] is not None or t - w["tesla"] < 3.0)

        def hit(bx, by, t=t):
            for w in st:
                if w["dead"] is None and abs(bx - w["x"]) < 28 and abs(by - w["cy"]) < 30:
                    hits.append((t, bx, by + 6, w["open"]))
                    return True
            return False
        fire.step(t, px, py, hit, on=any(a <= t < b for a, b in WD_FIRE))
        # draw
        for w in st:
            x, y = w["x"], w["cy"]
            if w["dead"] is None:
                win = w["window"]
                w["o"] = float(np.clip(w.get("o", 0.0) + (1 if w["open"] else -1) / (0.15 * FPS), 0, 1))
                o = w["o"]
                glow = 0.6
                if win is not None:
                    glow = 0.7 + 0.6 * min(1.0, win / 0.8)
                elif w["open"]:
                    glow = 0.9
                sp = warden_sprite(v, o, glow, sweep_ph=((t * 0.35 + x / 480) % 1.0))
                sprite.paste_center(f, sprite.shadow_of(sp, 0.35, 1.5, 0.85), x + 9, y + 14)
                sprite.paste_center(f, sp, x, y)
                aim = DOWN if w["aim"] == "down" else w["aim_ang"]
                if o > 0.5:
                    f = eye_glow(f, x, y, aim, glow)
                if win is not None and win < 0.8:
                    f = laser(f, x, y, aim, "tele", win / 0.8)
                elif win is not None and win < 1.8:
                    f = laser(f, x, y, aim, "beam", 1.0 if win < 1.7 else (1.8 - win) / 0.1)
            else:
                u = (t - w["dead"])
                if u < 0.5:
                    f = boom(f, x, y, u / 0.5, "medium", sparks=(170, 160, 180))
                f = flakes(f, x, y, u / 0.8, 22, (248, 236, 196), seed=3, r1=48, size=2.4)
                if u < 0.35:
                    sp = warden_sprite(v, 1.0, 1.0 - 2.4 * u, shell_alpha=0.0)
                    sprite.paste_center(f, sp, x, y)
                else:
                    f = slabs(f, warden_sprite(v, 1.0, -0.6, shell_alpha=0.0), x, y, (u - 0.35) / 1.2)
        for t0, bx, by, opened in hits:
            if 0 <= t - t0 < 0.2:
                if opened:
                    f = spark(f, bx, by, (t - t0) / 0.2, (255, 200, 170), 5, 7)
                else:
                    f = flakes(f, bx, by, (t - t0) / 0.2, 4, (250, 232, 170), seed=int(t0 * 97), r0=1, r1=10,
                               size=1.4, fall=4)
        if TESLA[0] <= t < TESLA[1] and st[2]["dead"] is None and (t - st[2]["tesla"]) < 0.1:
            rng = np.random.default_rng(int(t * 100))
            pts = g36.jagged((px, py - 18), (st[2]["x"], st[2]["cy"] + 8), rng, depth=5, rough=0.22)
            d = ImageDraw.Draw(f)
            d.line(pts, fill=(150, 190, 255, 200), width=3)
            d.line(pts, fill=(255, 255, 255, 255), width=1)
            f = light(f, st[2]["x"], st[2]["cy"] + 8, 7, (170, 200, 255), 0.7)
        fire.draw(f)
        ship(f, px, py)
        if t < 2.0:
            caption(f, "CLOSED: EVERY HIT SPARKS OFF THE SHELL, NO DAMAGE")
        elif t < 3.3:
            caption(f, "THE WINDOW: ITS LINE TELEGRAPHS 0.8 S; OPEN UNTIL THE BEAM ENDS")
        elif t < 5.2:
            caption(f, "AN AIMED LINE: STAND OFF IT AND SHOOT THE OPEN WARDEN")
        elif t < 7.3:
            caption(f, "THE LINE ABREAST OPENS IN TURN, 1.3 S APART")
        elif t < 8.2:
            caption(f, "ONE WINDOW AND A LITTLE MORE KILLS IT")
        elif t < 9.25:
            caption(f, "THE TESLA ARC (170 PX) HOLDS THE SHELL OPEN 3 S A HIT")
        else:
            caption(f, "DEATH: THE SHELL SHATTERS, THE GLYPHS DIE, THREE SLABS FALL")
        frames.append(f)
    return frames, bg


def wd_sheet(v, frames, bg):
    closed = warden_sprite(v, 0.0, 0.6, sweep_ph=0.45)
    states = [closed, warden_sprite(v, 0.34, 0.7), warden_sprite(v, 1.0, 0.8), warden_sprite(v, 1.0, 1.3),
              warden_sprite(v, 1.0, 0.8, shell_alpha=0.0), warden_sprite(v, 0.0, 0.6, 0.55, regrow=0.5)]
    panels = [("SOURCE: 260 PX, HALF OPEN", source_panel(lambda: warden_make(v, "both", open_=0.6, glow=1.0), W_EXT)),
              ("STATES (2X): CLOSED / OPENING / OPEN / TELEGRAPH / BROKEN / REGROW 0.4 S",
               row(states + [e60.player_cell(56)], 126, 2,
                   ["CLOSED", "OPENING", "OPEN", "TELEGRAPH", "BROKEN", "REGROW", "PLAYER"])),
              ("READABILITY: LEVEL 43, THE GATE RING AND DEEP SPACE",
               bands_check([(closed, "CLOSED"), (states[3], "OPEN")], bg.img, scales=(1,), names=("BRIGHTEST", "DARKEST"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.0, 2.5, 4.4, 8.6, 9.6],
                                              ["CLOSED", "TELEGRAPH", "AIMED BEAM", "TESLA HOLD", "DEATH"], 0.4))]
    if v == "a":
        notes = ["GATE WARDEN A - IDOL: A SQUAT ROUND BASALT SENTINEL IN A SIX-PETAL EGG WITH GOLD SEAMS",
                 "76X84 PX (120X124 CANVAS), FIXED, FACING DOWN. GATE STONE 3A3640 / 16141A / 8C8494: A ROUND BODY, A WAIST",
                 "BAND OF CRIMSON GLYPHS SET WITH PALE STUDS, A LOW CROWN, THE SINGLE EYE ON TOP (ITS PUPIL TURNS TO THE AIM).",
                 "CLOSED: A SMOOTH PALE GOLD-WHITE OVOID IN SIX PETALS JOINED BY THIN GOLD SEAMS, A SLOW SPECULAR SWEEP; HITS",
                 "THROW PALE GOLD FLAKES. OPEN (0.15 S): THE PETALS HINGE BACK LIKE AN IRIS, THE GLYPHS AND THE EYE BURN.",
                 "THE LINE: A THIN CRIMSON TELEGRAPH BRIGHTENING 0.8 S, THEN THE 10 PX BEAM 1.0 S. THE GATE RING BEHIND IS",
                 "DARKER AND COLDER (TEAL-WHITE GLYPHS THAT NEVER PULSE). SEAMS SHOW WHERE THE SHELL WILL OPEN."]
    else:
        notes = ["GATE WARDEN B - KEYSTONE: A TAPERED STONE BLOCK LIKE A PIECE OF THE GATE IN A SEAMLESS PEARL EGG",
                 "THE SAME SIZE, SHELL AND TIMING. THE BODY IS A WEDGE OF THE RING ITSELF: TWO GLYPH COLUMNS DOWN ITS FACE, A",
                 "LINTEL, SHOULDER STONES, THE EYE IN THE MIDDLE; IT READS AS 'THE GATE STANDING UP'. CLOSED, THE EGG IS ONE",
                 "SMOOTH PEARL WITHOUT SEAMS (A PURER 'IMMUNE' READ, BUT NOTHING HINTS WHERE IT OPENS UNTIL IT DOES).",
                 "SAME SCENE AS A."]
    return sheet(f"GATE WARDEN {v.upper()} - THE WINDOW SHIELD (LEVEL 43, THE GATE APPROACH)", panels, notes,
                 frames[int(4.4 * FPS)])


@unit("gate-warden")
def make_warden(variants):
    for v in variants:
        frames, bg = wd_scene(v)
        save("enemies/space/gate-warden", "gate-warden", v, wd_sheet(v, frames, bg), frames)


# ===================================================================== 2 Rift Skater (L44)

SK_N = 48
SK_EXT = SK_N / 20.0
S_RUST, S_RDARK, S_CRYS, S_CORE, S_BONE = range(5)


def skater_model(v):
    """a "manta": a rounded rust chitin manta, broad curved wings swept to points, a thin crystal rim, the violet
    core; b "shard": a sharp diamond with a raised keel and wide crystal wing panels (more crystal than chitin)."""
    rust, dark, bone = (hx(c) for c in em.CHITIN_BASES["rust"])
    vi = np.array(VIOLET) / 255.0
    mats = [Material(rust, metal=0.3, shininess=90, spec=0.9, pattern=em._ridges(16, 0.18)),
            Material(dark, metal=0.3, shininess=70, spec=0.7),
            Material(hx("d6c6f2"), metal=0.45, shininess=280, spec=2.2, emission=tuple(vi * 0.18)),
            Material((0.05, 0.03, 0.08), emission=tuple(vi * 1.7)),
            Material(bone, metal=0.2, shininess=70, spec=0.7)]

    def scene(p):
        q = mirror_x(p)
        if v == "a":
            half = [(0.0, 0.92), (0.22, 0.58), (0.98, -0.12), (0.92, -0.3), (0.46, -0.16), (0.16, -0.58), (0.0, -0.48)]
            d = sd_plate(q, half, 0.0, 0.1, 0.02, taper=lambda x, y: np.clip(1 - 0.8 * np.abs(x), 0.25, 1))
            e = sd_polygon2(q[:, 0], q[:, 1], half)
            m = np.where(e > -0.09, S_CRYS, S_RUST)
            m = np.where((m == S_RUST) & (q[:, 0] < 0.04), S_RDARK, m)
            return union((d, m), (sd_ellipsoid(p, (0, 0.06, 0.07), (0.14, 0.24, 0.08)), S_CORE), k=0.02)
        half = [(0.0, 1.02), (0.12, 0.6), (1.02, -0.4), (0.86, -0.44), (0.4, -0.22), (0.12, -0.84), (0.0, -0.66)]
        d = sd_plate(q, half, 0.0, 0.08, 0.01, taper=lambda x, y: np.clip(1 - 0.7 * np.abs(x), 0.3, 1))
        e = sd_polygon2(q[:, 0], q[:, 1], half)
        m = np.where((e > -0.07) | (q[:, 0] > 0.4), S_CRYS, S_RUST)
        m = np.where((m == S_CRYS) & (np.abs(q[:, 0] - 0.4) < 0.03), S_RDARK, m)
        dia = [(0.0, 0.3), (0.1, 0.06), (0.0, -0.2)]
        return union((d, m), (sd_box(p, (0, 0.1, 0.06), (0.05, 0.62, 0.05), 0.03), S_RDARK),
                     (sd_plate(q, dia, 0.1, 0.05, 0.01), S_CORE), k=0.01)
    return scene, mats


skaters = {v: e60.Turning(lambda v=v: skater_model(v), SK_N, 16, factor=5, colors=32, extent=SK_EXT) for v in "ab"}


def arrival_ring(img, x, y, u, v):
    """The arrival marker over its 0.5 s: a shrinks onto the point, b grows out of it; violet-white, no flash."""
    if not 0 <= u <= 1:
        return img
    r = 22 - 19 * u if v == "a" else 3 + 17 * u
    k = 0.5 + 0.5 * u
    img = ring_light(img, x, y, r, VIOLET, 0.9 * k, 1.5)
    return ring_light(img, x, y, r, VIOLET_W, 0.6 * k, 0.6)


def shard_flakes(img, x, y, s, seed=0):
    """Four violet chitin flakes splintering out (the death)."""
    if not 0 <= s < 1:
        return img
    d = ImageDraw.Draw(img)
    for k in range(4):
        a = k * TAU / 4 + 0.6 + seed
        r = 6 + 26 * np.sqrt(s)
        cx, cy = x + np.cos(a) * r, y + np.sin(a) * r
        sp = a + s * 6
        pts = [(cx + np.cos(sp) * 5, cy + np.sin(sp) * 5), (cx + np.cos(sp + 2.4) * 2.5, cy + np.sin(sp + 2.4) * 2.5),
               (cx + np.cos(sp + 3.9) * 2.5, cy + np.sin(sp + 3.9) * 2.5)]
        al = int(255 * (1 - s))
        d.polygon(pts, fill=(176, 120, 255, al), outline=(60, 20, 90, al))
    return img


SK_DUR = 10.0


def sk_scene(v):
    plane = EventPlane(44, SK_DUR)
    fv = plane.v
    rng = np.random.default_rng(7)
    sk = []
    for i in range(5):
        sk.append(dict(i=i, t0=0.4 + 0.5 * i, x=120.0 + 12 * i, y=-20.0, vx=fv[0] * 0.6, vy=fv[1] + 12, phase="wait",
                       next=0.4 + 0.5 * i + 1.2, jumps=0, hp=16.0, died=None, land=None, dest=None, side=1))
    shots = VShots(VIOLET, "orb", 4)
    fire = Fire(rate=10)
    frames, hits = [], []
    sx, sy = 240.0, 470.0
    for t in times(SK_DUR):
        f = plane.frame(t)
        for s in sk:
            if s["phase"] == "wait" and t >= s["t0"]:
                s["phase"] = "glide"
            if s["phase"] in ("glide", "fade"):
                s["x"] += s["vx"] / FPS
                s["y"] += s["vy"] / FPS
            if s["phase"] == "glide" and t >= s["next"]:
                if s["jumps"] < 3:
                    s["phase"], s["f0"], s["dest"] = "fade", t, None
                    best = None
                    for side in (s["side"], -s["side"]):
                        for deg in (rng.uniform(30, 45), 52.0, 60.0):
                            for L in (rng.uniform(110, 130), 100.0, 90.0):
                                a = np.radians(deg) * side
                                nx = s["x"] + np.sin(a) * L + fv[0] * 0.7
                                ny = s["y"] + np.cos(a) * L + fv[1] * 0.7
                                ok = (np.hypot(nx - sx, ny - sy) >= 150 and 24 <= nx <= FW - 24
                                      and ny + fv[1] * 0.5 < FH - 80 - 70)
                                if ok and best is None:
                                    best = (nx, ny, side)
                    if best is None:                      # no legal landing left: it glides out downwind
                        s["phase"] = "out"
                    else:
                        nx, ny, s["side"] = best
                        s["dest"] = [nx, ny]
                else:
                    s["phase"] = "out"
            if s["phase"] == "fade" and t >= s["f0"] + 0.2 and s["dest"] is not None:
                s["phase"], s["g0"] = "gone", t
            if s["phase"] == "gone":
                s["dest"][0] += fv[0] / FPS
                s["dest"][1] += fv[1] / FPS
                if t >= s["g0"] + 0.5:
                    s["phase"], s["x"], s["y"], s["land"] = "glide", s["dest"][0], s["dest"][1], t
                    s["jumps"] += 1
                    s["next"] = t + 1.0
                    s["vx"], s["vy"] = fv[0], fv[1]
                    for k in range(3):
                        tt = t + 0.25 + 0.15 * k
                        s.setdefault("burst", []).append(tt)
            if s["phase"] == "out":
                s["x"] += fv[0] * 1.4 / FPS
                s["y"] += fv[1] * 1.4 / FPS
            for tt in list(s.get("burst", [])):
                if tt <= t and s["phase"] in ("glide",) and s["died"] is None:
                    shots.fire(t, s["x"], s["y"] + 8, np.arctan2(sy - s["y"], sx - s["x"]), 160)
                    s["burst"].remove(tt)
        # the ship: under the next ring, else under the nearest live Skater
        rings = [s for s in sk if s["phase"] == "gone" and s["i"] in (0, 3)]   # the ship covers two of the five
        live = [s for s in sk if s["phase"] in ("glide", "fade") and s["y"] > 20]
        landed = [s for s in sk if s["land"] is not None and s["phase"] == "glide" and t - s["land"] < 0.5
                  and s["i"] in (0, 3)]
        if rings:
            tx = min(rings, key=lambda s: s["g0"])["dest"][0]
        elif landed:
            tx = landed[0]["x"]
        else:
            tx = 300 if t < 1.5 else sx
        sx, sy = move((sx, sy), (float(np.clip(tx, 40, 440)), 470), 230, 0.3)

        def hit(bx, by, t=t):
            for s in sk:
                if s["phase"] in ("glide", "fade") and s["died"] is None and abs(bx - s["x"]) < 13 and abs(by - s["y"]) < 11:
                    s["hp"] -= 6
                    hits.append((t, bx, by))
                    if s["hp"] <= 0:
                        s["phase"], s["died"] = "dead", t
                    return True
            return False
        cover = bool(rings) or bool(landed)
        fire.step(t, sx, sy, hit, on=cover)
        for s in sk:
            if s["phase"] in ("glide", "fade", "out"):
                hd = np.arctan2(s["vy"], s["vx"])
                sp = skaters[v].get(hd)
                if s["phase"] == "fade":
                    u = (t - s["f0"]) / 0.2
                    sp = fade(sp, 1 - u)
                    f = light(f, s["x"], s["y"], 8, VIOLET, 0.4 * (1 - u))
                sprite.paste_center(f, sp, s["x"], s["y"])
                if s["land"] is not None and 0.18 <= t - s["land"] < 0.25 + 0.3 and s["died"] is None:
                    f = light(f, s["x"], s["y"] + 6, 3.5, VIOLET_W, 0.8)
            elif s["phase"] == "gone":
                f = arrival_ring(f, s["dest"][0], s["dest"][1], (t - s["g0"]) / 0.5, v)
            elif s["phase"] == "dead":
                u = (t - s["died"]) / 0.6
                if u < 0.4:
                    f = light(f, s["x"], s["y"], 10, VIOLET, 0.6 * (1 - u / 0.4))
                f = shard_flakes(f, s["x"], s["y"], u, seed=s["t0"])
        for t0, bx, by in hits:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, (255, 230, 200), 4, 6)
        fire.draw(f)
        shots.draw(f, t)
        ship(f, sx, sy)
        if any(s["phase"] == "gone" for s in sk):
            caption(f, "A JUMP: GONE 0.5 S WHILE ITS RING MARKS THE LANDING")
        elif any(s["land"] is not None and 0 <= t - s["land"] < 0.7 and s["died"] is None for s in sk):
            caption(f, "IT LANDS AND FIRES A BURST OF 3 (0.25 S): COVER THE RING")
        elif any(s["died"] is not None and t - s["died"] < 0.8 for s in sk):
            caption(f, "KILLED: FOUR VIOLET FLAKES")
        else:
            caption(f, "A STREAM RIDES THE DRIFT; EVERYTHING GLIDES WITH THE CURRENT")
        frames.append(f)
    print("rift-skater", v, [(s["jumps"], s["died"] and round(s["died"], 2)) for s in sk], flush=True)
    return frames, plane


def sk_sheet(v, frames, plane):
    S = skaters[v]
    heads = [S.frame(k) for k in range(16)]
    main = S.get(DOWN + np.radians(25))
    ring_cells = []
    for u in (0.0, 0.5, 1.0):
        c = Image.new("RGBA", (SK_N, SK_N), (30, 20, 50, 255))
        arrival_ring(c, SK_N / 2, SK_N / 2, u, v)
        ring_cells.append(c)
    dead = Image.new("RGBA", (SK_N, SK_N), (30, 20, 50, 255))
    shard_flakes(dead, SK_N / 2, SK_N / 2, 0.3)
    shot = Image.new("RGBA", (SK_N, SK_N), (0, 0, 0, 0))
    vorb(shot, SK_N / 2, SK_N / 2, 4, VIOLET)
    panels = [("SOURCE: 260 PX", source_panel(lambda: skater_model(v), SK_EXT)),
              ("16 HEADINGS (2X)", grid(heads, 8, 2)),
              ("THE JUMP (2X): GLIDE / FADING / RING 0 / 0.25 / 0.5 S / DEATH / ITS SHOT",
               row([main, fade(main, 0.5)] + ring_cells + [dead, shot, e60.player_cell(48)], 52, 2,
                   ["GLIDE", "FADE", "RING 0", "0.25 S", "LAND", "DEATH", "SHOT", "PLAYER"])),
              ("READABILITY: LEVEL 44, THE EVENT PLANE", bands_check([(main, "SKATER")], plane.img, names=("PALEST", "DARKEST"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.5, 2.4, 3.2, 5.2], ["STREAM", "RINGS", "LANDING BURST", "KILLS"]))]
    if v == "a":
        notes = ["RIFT SKATER A - MANTA: A ROUNDED RUST CHITIN MANTA WITH A THIN CRYSTAL RIM; THE RING SHRINKS ONTO THE POINT",
                 "40X32 PX (48 CANVAS), 16 HEADINGS, FACING ITS GLIDE. RUST CHITIN (THE FAST ATTACKERS' ROLE COLOUR), BROAD CURVED",
                 "WINGS SWEPT TO POINTS, A PALE LILAC CRYSTAL EDGE THAT CATCHES THE PLANE'S LIGHT, THE VIOLET CORE (AIMED SHOTS).",
                 "THE JUMP: FADE 0.2 S, GONE 0.5 S WHILE A THIN VIOLET-WHITE RING SHRINKS FROM 44 PX ONTO THE LANDING POINT (THE",
                 "'SHRINKS' OF ITS LOOK ROW), THEN A MUZZLE GLINT AND A BURST OF 3 OUTLINED VIOLET SHOTS. NEVER A FLASH."]
    else:
        notes = ["RIFT SKATER B - SHARD: A SHARP CRYSTAL DIAMOND, WIDE CRYSTAL WING PANELS; THE RING GROWS OUT OF THE POINT",
                 "THE SAME SIZE AND TIMING. A NARROW RUST BODY WITH A DARK KEEL AND A DIAMOND CORE, THE WINGS MOSTLY PALE CRYSTAL",
                 "PANELS: MORE 'SHARD OF THE RIFT', PALER OVER THE PALE PLANE (THE RUST KEEL AND THE OUTLINE CARRY IT). THE RING",
                 "GROWS FROM THE POINT TO THE UNIT'S SIZE (THE STAT BLOCK'S 'GROWS'): IT SHOWS THE SIZE OF WHAT ARRIVES.",
                 "SAME SCENE AS A."]
    return sheet(f"RIFT SKATER {v.upper()} - PHASE JUMPS ON THE DRIFT (LEVEL 44, CROSSING)", panels, notes,
                 frames[int(2.4 * FPS)])


@unit("rift-skater")
def make_skater(variants):
    for v in variants:
        frames, plane = sk_scene(v)
        save("enemies/space/rift-skater", "rift-skater", v, sk_sheet(v, frames, plane), frames)


# ===================================================================== 3 Husk (L46)

HU_N = 112
HU_S = 40.0
HU_EXT = HU_N / HU_S
H_HULL, H_DARK, H_EDGE, H_HOLLOW, H_CRYS = range(5)
HR = (0.82, 1.08, 0.5)


def hull_plates(cell=0.32, seed=2.0):
    """Irregular brick-laid hull plates with dark seams (a multiplier)."""
    def f(p, n):
        y = p[:, 1] / cell * 0.8
        iy = np.floor(y)
        x = p[:, 0] / cell + 0.5 * (iy % 2)
        ix = np.floor(x)
        fx, fy = x - ix, y - iy
        h = (np.sin(ix * 12.9898 + iy * 78.233 + seed) * 43758.5453) % 1.0
        seam = np.minimum(np.minimum(fx, 1 - fx), np.minimum(fy, 1 - fy)) < 0.05
        return np.where(seam, 0.5, 0.78 + 0.27 * h) * (0.85 + 0.15 * n[:, 2])
    return f


def bent_plate(p, c, ang, size, tilt):
    """A plate at ``c`` lying along the bearing ``ang``, its outer edge lifted by ``tilt`` (a plate bent out)."""
    dx, dy, dz = p[:, 0] - c[0], p[:, 1] - c[1], p[:, 2] - c[2]
    ca, sa, ct, st = np.cos(ang), np.sin(ang), np.cos(tilt), np.sin(tilt)
    u, w = dx * ca + dy * sa, -dx * sa + dy * ca
    q = np.stack([u * ct + dz * st, w, -u * st + dz * ct], axis=-1)
    return sd_box(q, (0, 0, 0), size, 0.012)


def _surf_z(x, y, lift=0.0):
    return HR[2] * np.sqrt(max(0.0, 1 - (x / HR[0]) ** 2 - (y / HR[1]) ** 2)) + lift


HU_HOLES = {"a": [[((0.22, 0.18), 0.4)],
                  [((-0.3, -0.42), 0.3), ((0.34, 0.5), 0.26)],
                  [((0.0, -0.1), 0.3), ((-0.38, 0.55), 0.2), ((0.42, -0.6), 0.18)]],
            "b": [[(-0.02, -0.85, 0.04, 0.85)],
                  [(0.08, -0.8, -0.06, 0.55), ((0.5, 0.45), 0.2)],
                  [(-0.2, -0.6, 0.2, 0.7)]]}


def husk_model(v, shape=0, crack=0.0):
    """a "punctured": an ovoid hull of dull ash plates punched open from inside, the plates round each hole curled
    outward, pale crystal along the cracks running from the holes; b "split": the hull torn along its spine, the
    plates on both lips peeled back, the ribs bridging the dark gap, crystal along both lips. ``crack`` 1 = the
    cracked state (the crystal split wider and longer)."""
    rng = np.random.default_rng(11 + shape)
    holes = [h for h in HU_HOLES[v][shape] if len(h) == 2]
    splits = [h for h in HU_HOLES[v][shape] if len(h) == 4]
    lips, crys, ribs = [], [], []
    cw = 0.045 + 0.025 * crack
    cl = 0.36 + 0.2 * crack

    def crystal(x0, y0, a):
        b = a + rng.uniform(-0.35, 0.35)
        x1, y1 = x0 + np.cos(b) * cl, y0 + np.sin(b) * cl
        crys.append(((x0, y0, _surf_z(x0, y0, 0.015)), (x1, y1, _surf_z(x1, y1, 0.015))))
        xm, ym, c2 = (x0 + x1) / 2, (y0 + y1) / 2, b + rng.choice((-0.8, 0.8))
        x2, y2 = xm + np.cos(c2) * cl * 0.45, ym + np.sin(c2) * cl * 0.45
        crys.append(((xm, ym, _surf_z(xm, ym, 0.015)), (x2, y2, _surf_z(x2, y2, 0.015))))
    for (cx, cy), r in holes:
        for j in range(7):
            a = j * TAU / 7 + rng.uniform(-0.25, 0.25)
            px, py = cx + np.cos(a) * (r + 0.07), cy + np.sin(a) * (r + 0.07)
            lips.append(((px, py, _surf_z(px, py, 0.03)), a, (0.1, 0.07 * rng.uniform(0.8, 1.2), 0.018), 0.7))
        for j in range(3):
            a = rng.uniform(0, TAU)
            crystal(cx + np.cos(a) * (r + 0.12), cy + np.sin(a) * (r + 0.12), a)
    for x0, y0, x1, y1 in splits:
        for k in range(6):
            u = (k + 0.5) / 6
            sx, sy = x0 + (x1 - x0) * u, y0 + (y1 - y0) * u
            for side in (-1, 1):
                px = sx + side * (0.24 + 0.03 * rng.uniform(-1, 1))
                lips.append(((px, sy, _surf_z(px, sy, 0.03)), 0.0 if side > 0 else np.pi,
                             (0.1, 0.1 * rng.uniform(0.8, 1.1), 0.018), 0.8))
            if k % 2 == 1:
                ribs.append(((sx - 0.24, sy, 0.3), (sx + 0.24, sy, 0.3)))
        for side in (-1, 1):
            for k in range(2):
                u0 = 0.2 + 0.45 * k + 0.1 * rng.uniform(-1, 1)
                crystal(x0 + (x1 - x0) * u0 + side * 0.36, y0 + (y1 - y0) * u0,
                        (0.0 if side > 0 else np.pi) + rng.uniform(-0.6, 0.6))
    glow = 0.35 + 0.65 * crack

    def scene(p):
        ell = sdf.smin(sd_ellipsoid(p, (0, 0, 0), HR), sd_ellipsoid(p, (0.14, -0.46, -0.04), (0.6, 0.58, 0.42)), 0.12)
        d = np.abs(ell) - 0.05
        for (cx, cy), r in holes:
            d = np.maximum(d, -sd_sphere(p, (cx, cy, _surf_z(cx, cy)), r * (1 + 0.15 * crack)))
        for x0, y0, x1, y1 in splits:
            d = np.maximum(d, -sd_capsule(p, (x0, y0, 0.55), (x1, y1, 0.55), 0.15 + 0.05 * crack))
        m = np.where(ell < 0, H_HOLLOW, H_HULL)
        items = [(d, m)]
        for c, a, size, tilt in lips:
            items.append((bent_plate(p, c, a, size, tilt), H_EDGE))
        for a, b in ribs:
            items.append((sd_capsule(p, a, b, 0.045), H_DARK))
        for a, b in crys:
            items.append((sd_capsule(p, a, b, cw, cw * 0.4), H_CRYS))
        d, m = union(*items, k=0.01)
        m = np.where((m == H_HULL) & (p[:, 2] < 0.12), H_DARK, m)
        return d, m
    mats = [Material(hx("6e645c"), metal=0.1, shininess=14, spec=0.15, pattern=hull_plates()),
            Material(hx("2a2622"), metal=0.05, shininess=10, spec=0.1),
            Material(hx("8a8078"), metal=0.1, shininess=20, spec=0.25),
            Material((0.015, 0.013, 0.012)),
            Material(hx("b8c8d4"), metal=0.3, shininess=220, spec=1.6,
                     emission=tuple(np.array((0.72, 0.82, 0.9)) * 0.45 * glow))]
    return scene, mats


husks = {(v, s): e60.Turning(lambda v=v, s=s, crack=0.0: husk_model(v, s, crack), HU_N, 32, factor=3, colors=36,
                             extent=HU_EXT) for v in "ab" for s in range(3)}


def crystal_mask(a):
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    return np.clip((b - r - 6) / 20, 0, 1) * np.clip((b - 110) / 60, 0, 1)


def husk_sprite(v, shape, hd, crack=0.0, flare=0.0):
    sp = husks[(v, shape)].get(hd, crack=crack)
    return boost(sp, crystal_mask, (200, 230, 255), flare)


HU_DUR = 10.0
HUSKS = [dict(x=130, y=-50, vx=8, vy=36, h0=0.3, w=14, shape=0),
         dict(x=FW + 60, y=290, vx=-34, vy=4, h0=2.0, w=-10, shape=1),
         dict(x=360, y=-70, vx=-4, vy=30, h0=4.0, w=22, shape=2)]
HU_HP = 150.0
CUE = 7.0                              # the level's cue: every Husk on the screen cracks, 0.6 s apart


def hu_scene(v):
    bg = Scroll(graveyard_bg, 46, HU_DUR, 14.0)
    st = [dict(d, hp=HU_HP, crack=None, fanned=False, died=None) for d in HUSKS]
    needles = VShots(CRYSTAL, "needle")
    orbs = VShots(CRYSTAL, "orb", 4)
    fire = Fire(rate=10)
    frames, hits = [], []
    keys = [(0.0, 240, 470), (2.6, 240, 470), (2.95, 160, 420), (4.9, 160, 420), (5.4, 200, 430), (6.3, 200, 430),
            (6.8, 150, 460), (7.4, 150, 460), (7.9, 90, 470), (8.6, 90, 470), (9.2, 170, 470), (10.0, 200, 470)]
    for t in times(HU_DUR):
        f = bg.frame(t)
        sx, sy = path_at(keys, t)
        for i, h in enumerate(st):
            h["cx"], h["cy"] = h["x"] + h["vx"] * t, h["y"] + h["vy"] * t
            h["hd"] = h["h0"] + np.radians(h["w"]) * t
            cue = CUE + 0.6 * (i - 1) if i > 0 else None
            if cue is not None and h["crack"] is None and t >= cue and h["died"] is None:
                h["crack"] = t
            if h["crack"] is not None and not h["fanned"] and t >= h["crack"] + 0.5:
                h["fanned"] = True
                needles.fan(t, h["cx"], h["cy"], np.arctan2(sy - h["cy"], sx - h["cx"]), 5, np.radians(15), 220)

        def hit(bx, by, t=t):
            for h in st:
                if h["died"] is None and np.hypot(bx - h["cx"], (by - h["cy"]) * 1.2) < 34:
                    h["hp"] -= 6.5
                    hits.append((t, bx, by))
                    if h["hp"] <= HU_HP / 2 and h["crack"] is None:
                        h["crack"] = t
                    if h["hp"] <= 0:
                        h["died"] = t
                        orbs.ring(t, h["cx"], h["cy"], 8, 100, phase=np.radians(22.5))
                    return True
            return False
        fire.step(t, sx, sy, hit, on=(3.0 <= t < 4.0) or (5.6 <= t < 6.3))
        for h in st:
            x, y = h["cx"], h["cy"]
            if h["died"] is None:
                crack, flare = 0.0, 0.0
                if h["crack"] is not None:
                    u = t - h["crack"]
                    crack = 1.0 if u >= 0.25 else 0.0
                    flare = 1.2 * np.sin(np.pi * min(1.0, u / 0.6)) if u < 0.6 else 0.0
                sp = husk_sprite(v, h["shape"], h["hd"], crack, flare)
                sprite.paste_center(f, sprite.shadow_of(sp, 0.3, 1.5, 0.85), x + 10, y + 16)
                sprite.paste_center(f, sp, x, y)
                if flare > 0:
                    f = light(f, x, y, 16, (190, 220, 255), 0.3 * flare)
            else:
                u = t - h["died"]
                if u < 1.0:
                    sp = husk_sprite(v, h["shape"], h["hd"], 1.0)
                    f = slabs(f, sp, x, y, u / 1.0, seed=1)
                f = flakes(f, x, y, u / 1.0, 18, (200, 222, 240), seed=5, r1=46, size=2.0)
                if u < 0.4:
                    f = boom(f, x, y, u / 0.4, "small", sparks=None)
        for t0, bx, by in hits:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, (230, 222, 210), 4, 6)
        fire.draw(f)
        needles.draw(f, t)
        orbs.draw(f, t)
        ship(f, sx, sy)
        cracking = [h for h in st if h["crack"] is not None and 0 <= t - h["crack"] < 0.5]
        if t < 3.0:
            caption(f, "THE QUIET: HUSKS DRIFT; NOTHING FIRES UNLESS THE PLAYER FIRES FIRST")
        elif cracking and t < CUE:
            caption(f, "HALF ITS HP: THE CRACK TELL (0.5 S), THE CRYSTAL FLARES...")
        elif t < 5.7:
            caption(f, "...A FAN OF 5 SHARDS CENTRED ON THE SHIP")
        elif t < CUE - 0.2:
            caption(f, "DEATH: IT FOLDS IN; A SLOW RING OF 8 WHEREVER IT DIES")
        else:
            caption(f, "THE LEVEL'S CUE: EVERY HUSK ON THE SCREEN CRACKS, 0.6 S APART")
        frames.append(f)
    print("husk", v, [(h["crack"] and round(h["crack"], 2), h["died"] and round(h["died"], 2)) for h in st],
          flush=True)
    return frames, bg


def hu_sheet(v, frames, bg):
    shapes = [husk_sprite(v, s, DOWN) for s in range(3)]
    tell = husk_sprite(v, 0, DOWN, 0.0, 1.0)
    cracked = husk_sprite(v, 0, DOWN, 1.0)
    angles = [husk_sprite(v, 0, DOWN + k * TAU / 8) for k in range(8)]
    shard = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    needle(shard, 14, 14, -0.6)
    orbc = Image.new("RGBA", (28, 28), (0, 0, 0, 0))
    vorb(orbc, 14, 14, 4, CRYSTAL)
    panels = [("SOURCE: 260 PX", source_panel(lambda: husk_model(v, 0), HU_EXT)),
              ("THREE HULL SHAPES / CRACK TELL / CRACKED (1X)",
               row(shapes + [tell, cracked, e60.player_cell(56)], HU_N, 1,
                   ["SHAPE 1", "SHAPE 2", "SHAPE 3", "TELL", "CRACKED", "PLAYER"])),
              ("8 OF 32 ANGLES (1X); ITS SHARD AND ORB (2X)",
               row(angles, HU_N, 1)),
              ("", row([shard, orbc], 28, 2, ["SHARD", "ORB"])),
              ("READABILITY: LEVEL 46, THE COLD LIGHT AND THE DARK",
               bands_check([(shapes[0], "WHOLE"), (cracked, "CRACKED")], bg.img, scales=(1,),
                           names=("BRIGHTEST", "DARKEST"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.0, 4.6, 5.6, 6.6, 7.7],
                                              ["THE QUIET", "CRACK TELL", "THE FAN", "DEATH RING", "THE CUE"], 0.4))]
    if v == "a":
        notes = ["HUSK A - PUNCTURED: AN OVOID HULL PUNCHED OPEN FROM INSIDE, ITS PLATES CURLED OUT ROUND ROUND HOLES",
                 "88X72 PX (112 CANVAS), 32 ANGLES, TURNING SLOWLY. SILENCE-TOUCHED: DULL ASH GREY-BROWN PLATES (6E645C / 2A2622,",
                 "PALER LIP EDGES), NO GLOW AT ALL; ONE TO THREE HOLES WITH THE PLATES BENT OUTWARD ROUND THEM, AN EMPTY DARK",
                 "HOLLOW INSIDE; PALE COLD CRYSTAL (B8C8D4) RUNNING FROM THE HOLES ALONG THE CRACKS, FAINTLY LIT. THE CRACK TELL:",
                 "THE CRYSTAL FLARES ONCE AND SPLITS WIDER (0.5 S), THEN THE FAN. THE DEATH: SLABS AND SPLINTERS, THE ORB RING.",
                 "THE SCENERY HULKS BEHIND ARE PALER AND FLATTER, SO THE LIVING THREAT READS DARKER AND HARDER."]
    else:
        notes = ["HUSK B - SPLIT: A HULL TORN OPEN ALONG ITS SPINE, PLATES PEELED BACK, RIBS ACROSS THE DARK GAP",
                 "THE SAME SIZE, COLOURS AND TIMING. ONE LONG TEAR DOWN ITS BACK (STRAIGHT, CROOKED, OR WITH A SIDE HOLE), THE",
                 "PLATES ON BOTH LIPS BENT OUTWARD, DARK RIBS BRIDGING THE EMPTY INSIDE, CRYSTAL GROWING ALONG BOTH LIPS. IT",
                 "READS MORE 'OPENED LIKE A CARCASS' (SOMETHING CLIMBED OUT); THE DARK SLOT IS VISIBLE FROM EVERY ANGLE.",
                 "SAME SCENE AS A."]
    return sheet(f"HUSK {v.upper()} - THE QUIET, THE CRACK, THE SHARDS (LEVEL 46, GRAVEYARD OF THE CHOIR)", panels,
                 notes, frames[int(4.5 * FPS)])


@unit("husk")
def make_husk(variants):
    for v in variants:
        frames, bg = hu_scene(v)
        save("enemies/space/husk", "husk", v, hu_sheet(v, frames, bg), frames)


# ===================================================================== 4 Choir Seraph (L47)

SE_N = 88
SE_S = 34.0
SE_EXT = SE_N / SE_S
E_BONE, E_DARK, E_BRIGHT, E_EDGE, E_HEART = range(5)


def _rot_about(q, pivot, ang):
    """Points ``q`` rotated by ``ang`` about ``pivot`` (x, y): the shape rotated by -ang."""
    c, s = np.cos(ang), np.sin(ang)
    x, y = q[:, 0] - pivot[0], q[:, 1] - pivot[1]
    out = q.copy()
    out[:, 0] = x * c - y * s + pivot[0]
    out[:, 1] = x * s + y * c + pivot[1]
    return out


def _leaf(root, ang, L, w):
    c, s = np.cos(ang), np.sin(ang)
    rx, ry = root
    return [(rx, ry), (rx + c * L * 0.35 - s * w, ry + s * L * 0.35 + c * w), (rx + c * L, ry + s * L),
            (rx + c * L * 0.35 + s * w, ry + s * L * 0.35 - c * w)]


def seraph_model(v, fold=0.0, glow=1.0):
    """a "blades": a slim ivory body, two pairs of long swept-back blade wings with gold-white light along their
    leading edges, the gold-white heart; b "halo": broader feathered wings (three and two long feathers, bone
    and ivory in turn) and the Herald's halo ring floating over its back. ``fold`` 1 = the wings closed back
    along the body (the jump's tell)."""
    mats = [Material(hx("dccfb4"), metal=0.2, shininess=90, spec=0.9, pattern=em._ridges(16, 0.15)),
            Material(hx("5c4a5c"), metal=0.2, shininess=60, spec=0.6),
            Material(hx("f4ecd8"), metal=0.25, shininess=120, spec=1.1),
            Material(hx("f4ecd8"), metal=0.2, shininess=120, spec=1.0, emission=tuple(GW_F * 1.0 * glow)),
            Material((0.05, 0.05, 0.04), emission=tuple(GW_F * 1.7 * glow))]
    fp, hp = (0.12, 0.24), (0.12, -0.08)
    fa, ha = np.radians(58) * fold, np.radians(34) * fold

    def scene(p):
        q = mirror_x(p)
        body = union((sd_capsule(p, (0, 0.6, 0.1), (0, -0.78, 0.02), 0.13, 0.05), E_BONE),
                     (sd_ellipsoid(p, (0, 0.1, 0.08), (0.2, 0.34, 0.16)), E_BONE),
                     (sd_sphere(p, (0, 0.64, 0.14), 0.12), E_BRIGHT), k=0.06)
        d, m = body
        m = np.where((m == E_BONE) & (np.sin(p[:, 1] * 30) > 0.75) & (p[:, 1] < -0.22), E_DARK, m)
        items = [(d, m), (sd_sphere(p, (0, 0.13, 0.22), 0.075), E_HEART)]
        qf, qh = _rot_about(q, fp, fa), _rot_about(q, hp, ha)
        if v == "a":
            fore = [(0.1, 0.34), (0.55, 0.34), (1.1, -0.12), (1.02, -0.24), (0.48, 0.08), (0.1, 0.1)]
            hind = [(0.1, 0.0), (0.45, -0.16), (0.86, -0.82), (0.74, -0.88), (0.32, -0.46), (0.1, -0.3)]
            for qq, poly, le, z in ((qf, fore, [(0.1, 0.34, 0.55, 0.34), (0.55, 0.34, 1.1, -0.12)], 0.1),
                                    (qh, hind, [(0.1, 0.0, 0.45, -0.16), (0.45, -0.16, 0.86, -0.82)], 0.04)):
                wd = sd_plate(qq, poly, z, 0.04, 0.015, taper=lambda x, y: np.clip(1.2 - 0.6 * x, 0.4, 1))
                wm = np.where(seg_dist2(qq, le) < 0.05, E_EDGE, E_BONE)
                items.append((wd, wm))
        else:
            fore = [(0.1, 0.34), (0.5, 0.42), (1.08, 0.02), (1.0, -0.12), (0.86, -0.04), (0.8, -0.2), (0.66, -0.08),
                    (0.58, -0.24), (0.44, -0.08), (0.1, 0.08)]
            hind = [(0.1, 0.0), (0.42, -0.08), (0.84, -0.6), (0.7, -0.66), (0.62, -0.5), (0.5, -0.62), (0.4, -0.44),
                    (0.28, -0.5), (0.1, -0.3)]
            for qq, poly, piv, z in ((qf, fore, fp, 0.1), (qh, hind, hp, 0.04)):
                wd = sd_plate(qq, poly, z, 0.04, 0.012, taper=lambda x, y: np.clip(1.2 - 0.6 * x, 0.4, 1))
                band = np.floor(np.arctan2(qq[:, 1] - piv[1], qq[:, 0] - piv[0]) / np.radians(13)) % 2
                wm = np.where(band > 0, E_BRIGHT, E_BONE)
                wm = np.where(seg_dist2(qq, [(poly[0][0], poly[0][1], poly[1][0], poly[1][1]),
                                             (poly[1][0], poly[1][1], poly[2][0], poly[2][1])]) < 0.05, E_EDGE, wm)
                items.append((wd, wm))
            items.append((sd_torus(p, (0, 0.1, 0.52), 0.42, 0.04), E_EDGE))
            for a in np.linspace(0, TAU, 3, endpoint=False) + np.pi / 2:
                items.append((sd_capsule(p, (0, 0.1, 0.22), (np.cos(a) * 0.4, 0.1 + np.sin(a) * 0.4, 0.5), 0.03),
                              E_BRIGHT))
        return union(*items, k=0.02)
    return scene, mats


FOLD_STEPS = (0.0, 0.5, 1.0)
seraphs = {v: Fixed(lambda v=v, fold=0.0: seraph_model(v, fold), SE_N, SE_EXT, 4, 44) for v in "ab"}
ROOTS = [(-0.15 * SE_S, 0.18 * SE_S), (0.15 * SE_S, 0.18 * SE_S)]       # the fore wing roots on the screen (facing down)


def seraph_sprite(v, fold=0.0):
    return seraphs[v].get(fold=min(FOLD_STEPS, key=lambda s: abs(s - fold)))


def seraph_ring(img, x, y, u, R=34):
    """The arrival ring: violet with a violet-white core line, shrinking over its time; never gold, never a flash."""
    if not 0 <= u <= 1:
        return img
    r = R - (R - 6) * u
    img = ring_light(img, x, y, r, VIOLET, 0.6 + 0.5 * u, 1.7)
    return ring_light(img, x, y, r, VIOLET_W, 0.5 + 0.4 * u, 0.6)


def feathers_of_light(img, x, y, s, seed=0):
    """The death: the wings scatter into gold-white feathers of light that drift and fade."""
    if not 0 <= s < 1:
        return img
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(img)
    for _ in range(14):
        a = rng.uniform(np.pi * 0.9, np.pi * 2.1)
        r = rng.uniform(10, 46) * np.sqrt(s)
        cx, cy = x + np.cos(a) * r, y + np.sin(a) * r * 0.7 + 30 * s * s
        L = 5 * (1 - s) + 2
        ang = a + np.pi / 2 + s * rng.uniform(-2, 2)
        al = int(230 * (1 - s))
        d.line([(cx - np.cos(ang) * L, cy - np.sin(ang) * L), (cx + np.cos(ang) * L, cy + np.sin(ang) * L)],
               fill=GOLD_WHITE + (al,), width=2)
        img = light(img, cx, cy, 3, GOLD_WHITE, 0.25 * (1 - s))
    return img


SE_DUR = 10.5
SE_KEYS = [(0.0, 170, 470), (4.5, 170, 470), (5.6, 240, 470), (6.6, 240, 470), (7.4, 300, 470), (8.2, 300, 470),
           (9.0, 200, 470), (10.5, 230, 470)]
SE_CYCLES = [dict(ring=(0.5, 1.0), rows=(110, 210)), dict(ring=(4.5, 0.5), rows=(150, 250)),
             dict(ring=(8.0, 0.5), rows=(120, 230))]


def se_scene(v):
    bg = Scroll(sphere_bg, 47, SE_DUR, 6.0)
    shots = VShots(VIOLET, "orb", 4)
    fire = Fire(rate=10)
    frames, hits = [], []
    pair = [dict(hp=90.0, died=None, th0=0.0), dict(hp=90.0, died=None, th0=np.pi / 2)]
    nextshot = [0.0, 0.0]
    for t in times(SE_DUR):
        f = bg.frame(t)
        sx, sy = path_at(SE_KEYS, t)
        mx = FW - sx
        state = None
        for ci, c in enumerate(SE_CYCLES):
            r0, rl = c["ring"]
            if r0 <= t < r0 + rl + 3.3:
                state = (ci, c, t - r0, rl)
        for i, s in enumerate(pair):
            if s["died"] is not None:
                u = t - s["died"]
                f = feathers_of_light(f, s["x"], s["y"], u / 1.0, seed=3 + i)
                if u < 0.45:
                    f = boom(f, s["x"], s["y"], u / 0.45, "small", sparks=GOLD_WHITE)
                    f = light(f, s["x"], s["y"], 18, GOLD_WHITE, 0.5 * (1 - u / 0.45))
                continue
            if state is None:
                continue
            ci, c, u, rl = state
            y = c["rows"][i]
            if u < rl:                                         # the arrival ring at its point
                f = seraph_ring(f, mx, y, u / rl, 34 if ci == 0 else 26)
                s["x"], s["y"] = mx, y
                continue
            m = u - rl
            x = mx
            s["x"], s["y"] = x, y
            if m < 2.5:                                        # mirroring, firing its spiral
                sp = seraph_sprite(v, 0.0)
                if m >= 0.3 and t >= nextshot[i]:
                    th = s["th0"] + ci * 0.7 + np.radians(120) * m
                    for arm in (0, np.pi):
                        shots.fire(t, x, y + 6, th + arm, 120)
                    nextshot[i] = t + 1.0 / 3.0
            elif m < 2.8:
                sp = seraph_sprite(v, (m - 2.5) / 0.3)
            elif m < 3.0:
                sp = fade(seraph_sprite(v, 1.0), 1 - (m - 2.8) / 0.2)
            else:
                continue
            sprite.paste_center(f, sprite.shadow_of(sp, 0.3, 1.5, 0.85), x + 10, y + 18)
            sprite.paste_center(f, sp, x, y)
            if m < 0.35:                                       # the violet flare at the wing roots: the spiral's tell
                k = np.sin(np.pi * m / 0.35)
                for rx, ry in ROOTS:
                    f = light(f, x + rx, y + ry, 6, VIOLET, 0.9 * k)
            f = light(f, x, y + 4, 5, GOLD_WHITE, 0.25)

        def hit(bx, by, t=t):
            for s in pair:
                if s["died"] is None and state is not None and state[2] >= state[3] and state[2] - state[3] < 2.8 \
                        and abs(bx - s["x"]) < 24 and abs(by - s["y"]) < 26:
                    s["hp"] -= 6
                    hits.append((t, bx, by))
                    if s["hp"] <= 0:
                        s["died"] = t
                    return True
            return False
        fire.step(t, sx, sy, hit, on=t > 1.6)
        for t0, bx, by in hits:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, (255, 240, 200), 4, 6)
        fire.draw(f)
        shots.draw(f, t)
        ship(f, sx, sy)
        if state and state[2] < state[3]:
            caption(f, "A PAIR ARRIVES: VIOLET RINGS MARK WHERE (1.0 S, LATER 0.5 S)")
        elif any(s["died"] is not None and t - s["died"] < 1.2 for s in pair):
            caption(f, "KILLED: ITS WINGS SCATTER INTO FEATHERS OF LIGHT")
        elif state and 2.5 <= state[2] - state[3] < 3.0:
            caption(f, "THE FOLD (0.3 S) TELLS THE JUMP: FINISH IT NOW")
        elif state and abs(sx - 240) < 16:
            caption(f, "ONLY THE MIDDLE LINES IT UP, WHERE THE SPIRAL IS DENSEST")
        elif state:
            caption(f, "IT MIRRORS THE SHIP ACROSS THE CENTRE, FIRING A TWO-ARM SPIRAL")
        else:
            caption(f, "THE INNER SPHERE")
        frames.append(f)
    print("seraph", v, [s["died"] and round(s["died"], 2) for s in pair], flush=True)
    return frames, bg


def se_sheet(v, frames, bg):
    main = seraph_sprite(v, 0.0)
    states = [main, seraph_sprite(v, 0.5), seraph_sprite(v, 1.0), fade(seraph_sprite(v, 1.0), 0.5)]
    flare = Image.new("RGBA", (SE_N, SE_N), (0, 0, 0, 0))
    flare.alpha_composite(main)
    for rx, ry in ROOTS:
        flare = light(flare, SE_N / 2 + rx, SE_N / 2 + ry, 6, VIOLET, 0.9)
    ringc = Image.new("RGBA", (SE_N, SE_N), (40, 34, 30, 255))
    seraph_ring(ringc, SE_N / 2, SE_N / 2, 0.4)
    shot = Image.new("RGBA", (SE_N, SE_N), (0, 0, 0, 0))
    vorb(shot, SE_N / 2, SE_N / 2, 4, VIOLET)
    panels = [("SOURCE: 260 PX", source_panel(lambda: seraph_model(v), SE_EXT)),
              ("STATES (2X): MIRRORING / FLARE / FOLDING / FOLDED / FADING / RING / SHOT",
               row(states[:1] + [flare] + states[1:] + [ringc, shot, e60.player_cell(56)], 92, 2,
                   ["MIRROR", "FLARE", "FOLD", "FOLDED", "FADE", "RING", "SHOT", "PLAYER"])),
              ("READABILITY: LEVEL 47, THE BONE SHELL", bands_check([(main, "SERAPH")], bg.img, names=("BRIGHTEST", "DARKEST"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.0, 3.0, 4.2, 6.3, 7.0],
                                              ["ARRIVAL", "MIRROR + SPIRAL", "FOLD", "THE MIDDLE", "KILLED"], 0.4))]
    if v == "a":
        notes = ["CHOIR SERAPH A - BLADES: A SLIM IVORY SERAPH WITH TWO PAIRS OF LONG SWEPT BLADE WINGS",
                 "72X76 PX (88 CANVAS), FIXED, FACING DOWN. VRELL ELITE: BONE AND IVORY CHITIN (DCCFB4 / 5C4A5C / F4ECD8), GOLD-",
                 "WHITE LIGHT ALONG THE WINGS' LEADING EDGES AND IN ITS HEART (F2DC9A), NEVER THE PLAYER'S WHITE. THE SPIRAL'S",
                 "TELL: A VIOLET FLARE AT THE WING ROOTS; THE JUMP'S TELL: THE WINGS FOLD BACK ALONG THE BODY (0.3 S), THEN IT",
                 "FADES (0.2 S). ARRIVAL RINGS VIOLET WITH A VIOLET-WHITE CORE (THE WORLD DOC'S 'NEVER GOLD' OVER THE BONE SHELL).",
                 "A SLEEK 'ANGEL OF BLADES': THE HERALD'S FAMILY BY COLOUR, NOT BY SHAPE."]
    else:
        notes = ["CHOIR SERAPH B - HALO: FEATHERED WINGS AND THE CHOIR HERALD'S HALO RING OVER ITS BACK",
                 "THE SAME SIZE, COLOURS AND TIMING. EACH WING A FAN OF LONG FEATHERS (BONE AND IVORY IN TURN, GOLD-WHITE EDGES),",
                 "A GLOWING GOLD-WHITE HALO ON THREE SPOKES OVER THE BACK: PLAINLY THE HERALD'S SERAPH FAMILY (ROUND 36 B), RICHER",
                 "AND MORE 'CHOIR', BUT BUSIER IN THE SAME 72 PX AND MORE GOLD ON THE SCREEN. SAME SCENE AS A."]
    return sheet(f"CHOIR SERAPH {v.upper()} - TELEPORT AND MIRROR (LEVEL 47, THE SERAPH GAUNTLET)", panels, notes,
                 frames[int(3.0 * FPS)])


@unit("choir-seraph")
def make_seraph(variants):
    for v in variants:
        frames, bg = se_scene(v)
        save("enemies/air/choir-seraph", "choir-seraph", v, se_sheet(v, frames, bg), frames)


# ===================================================================== 5 The spawning organ (L45)

OR_N = 96
OR_S = 40.0
OR_EXT = OR_N / OR_S
O_FLESH, O_DARK, O_VEIN, O_POD, O_GLOW, O_HOLE = range(6)
OR_COL = {"a": ("1e5c5a", "06201e", "8ab8a8"), "b": ("9a4462", "36121e", "e0b0c0")}


def _root_veins():
    """One glowing vein down the middle of each root (a multiplier on the emission)."""
    def f(p, n):
        return np.clip(1.0 - np.abs(n[:, 2] - 0.96) * 14, 0, 1)
    return f


def organ_model(v, swell=0.0, iris=0.0, opened=0.0, torn=0.0):
    """a "teal bulb": the Hive Node's teal-black spawner flesh, a round sac on seven roots, one seed pod on its
    crown, an iris on its front; b "reef polyp": the reef's own pink flesh, a taller ribbed sac on nine roots, a
    ring of six seed pods round an iris on its crown. ``swell`` the pods' 0.5 s tell, ``opened`` the pods torn
    open by the release, ``iris`` the Skitter mouth, ``torn`` the dead husk left on the plates."""
    flesh, dark, pod = (hx(c) for c in OR_COL[v])
    t_ = np.array(TEAL) / 255.0 * (1 - torn)
    mats = [Material(flesh, metal=0.15, shininess=70, spec=0.8, emission=tuple(t_ * 0.55),
                     emission_pattern=em._veins(7, 0.4, 0.18), pattern=em._ridges(12, 0.12) if v == "b" else None),
            Material(dark, metal=0.1, shininess=40, spec=0.4),
            Material(em._mix(flesh, dark, 0.55), metal=0.1, shininess=50, spec=0.6, emission=tuple(t_ * 0.6),
                     emission_pattern=_root_veins()),
            Material(pod, metal=0.2, shininess=90, spec=1.0, pattern=em._ridges(14, 0.2),
                     emission=tuple(t_ * 0.9 * swell)),
            Material((0.04, 0.04, 0.04), emission=tuple(t_ * 1.7)),
            Material((0.02, 0.01, 0.02))]
    rng = np.random.default_rng(5 if v == "a" else 6)
    nroot = 7 if v == "a" else 9
    roots = []
    for k in range(nroot):
        a = k * TAU / nroot + rng.uniform(-0.2, 0.2)
        L = rng.uniform(0.74, 0.9)
        b, c = a + rng.uniform(0.15, 0.3), a + rng.uniform(0.3, 0.55)
        roots.append(((np.cos(a) * 0.3, np.sin(a) * 0.3, 0.1), (np.cos(b) * 0.58, np.sin(b) * 0.58, 0.06),
                      (np.cos(c) * L, np.sin(c) * L, 0.02)))
    sw = 1 + 0.28 * swell

    def scene(p):
        rw = 1.0 if v == "a" else 0.8
        items = [(sd_ellipsoid(p, (0, 0, -0.02), (0.84, 0.84, 0.08)), O_DARK)]
        for a, b, c in roots:
            items += [(sd_capsule(p, a, b, 0.13 * rw, 0.1 * rw), O_VEIN), (sd_capsule(p, b, c, 0.1 * rw, 0.08 * rw), O_VEIN)]
        zs = 1 - 0.7 * torn
        if v == "a":
            sac = sd_ellipsoid(p, (0, 0.06, 0.3 * zs), (0.62, 0.66, 0.4 * zs))
            items.append((sac, O_FLESH))
            if torn < 0.5:
                pd = sd_ellipsoid(p, (0, -0.1, 0.66), (0.25 * sw, 0.25 * sw, 0.2 * sw))
                if opened > 0:
                    slot = np.minimum(np.abs(p[:, 0]) - 0.05 * opened, np.abs(p[:, 1] + 0.1) - 0.05 * opened)
                    pd = np.maximum(pd, -slot)
                    items.append((sd_sphere(p, (0, -0.1, 0.6), 0.14), O_GLOW))
                items.append((pd, O_POD))
                items.append((sd_torus(p, (0, 0.4, 0.6), 0.16, 0.05), O_DARK))
                items.append((sd_sphere(p, (0, 0.4, 0.5), 0.13), O_GLOW if iris > 0.05 else O_FLESH))
                d, m = union(*items, k=0.04)
                if iris > 0.05:
                    d = np.maximum(d, -sd_sphere(p, (0, 0.4, 0.68), 0.15 * iris))
                return d, m
        else:
            sac = sd_ellipsoid(p, (0, 0, 0.34 * zs), (0.56, 0.56, 0.52 * zs))
            items.append((sac, O_FLESH))
            if torn < 0.5:
                for k in range(6):
                    a = k * TAU / 6
                    c = (np.cos(a) * 0.3, np.sin(a) * 0.3, 0.8)
                    pd = sd_sphere(p, c, 0.11 * sw)
                    if opened > 0:
                        pd = np.maximum(pd, -sd_sphere(p, (c[0] * 1.15, c[1] * 1.15, 0.88), 0.09 * opened))
                    items.append((pd, O_POD))
                items.append((sd_torus(p, (0, 0, 0.86), 0.13, 0.045), O_DARK))
                items.append((sd_sphere(p, (0, 0, 0.78), 0.11), O_GLOW if iris > 0.05 else O_FLESH))
                d, m = union(*items, k=0.04)
                if iris > 0.05:
                    d = np.maximum(d, -sd_sphere(p, (0, 0, 0.92), 0.12 * iris))
                return d, m
        d, m = union(*items, k=0.04)
        for k in range(4):                                    # the torn sac: ragged holes, nothing inside
            a = k * TAU / 4 + 0.5
            d = np.maximum(d, -sd_sphere(p, (np.cos(a) * 0.25, np.sin(a) * 0.25, 0.35), 0.2))
        return d, np.where(m == O_FLESH, O_DARK, m)
    return scene, mats


organs = {v: Fixed(lambda v=v, swell=0.0, iris=0.0, opened=0.0, torn=0.0: organ_model(v, swell, iris, opened, torn),
                   OR_N, OR_EXT, 4, 44) for v in "ab"}


def teal_mask(a):
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    return np.clip((g - r - 40) / 60, 0, 1)


def organ_sprite(v, swell=0.0, iris=0.0, opened=0.0, torn=0.0, pulse=0.0):
    q = lambda x: round(float(x) * 4) / 4
    sp = organs[v].get(swell=q(swell), iris=q(iris), opened=q(opened), torn=float(torn > 0.5))
    return boost(sp, teal_mask, TEAL, 0.25 * pulse + 0.6 * swell) if torn < 0.5 else sp


WHIRL = [Image.open(ROOT / "assets" / "sprites" / f"whirl-seed_{i}.png").convert("RGBA") for i in range(8)]


class Seeds:
    """A Whirl Seed cluster: spiral-out 1.5 s (1 turn/s, +90 px/s), the release point riding the reef down,
    then on along its outward angle turned downward, off the side edges."""

    def __init__(self):
        self.list = []

    def release(self, t0, x, y, n=6, scroll=100.0):
        for k in range(n):
            self.list.append(dict(t0=t0, x0=x, y0=y, a0=k * TAU / n, scroll=scroll, dead=None))

    def pos(self, s, t):
        u = t - s["t0"]
        if u < 1.5:
            a = s["a0"] + TAU * u
            r = 90 * u
            return s["x0"] + np.cos(a) * r, s["y0"] + s["scroll"] * u + np.sin(a) * r
        a = s["a0"] + TAU * 1.5
        x, y = s["x0"] + np.cos(a) * 135, s["y0"] + s["scroll"] * 1.5 + np.sin(a) * 135
        dx, dy = np.cos(a) * 0.6, np.sin(a) * 0.6 + 0.8
        n = np.hypot(dx, dy)
        x, y = x + dx / n * 140 * (u - 1.5), y + dy / n * 140 * (u - 1.5)
        x = abs(x) if x < 0 else (2 * FW - x if x > FW else x)
        return x, y

    def draw(self, img, t):
        for s in self.list:
            if t >= s["t0"] and s["dead"] is None:
                x, y = self.pos(s, t)
                sprite.paste_center(img, WHIRL[int(t * FPS) % 8], x, y)
            elif s["dead"] is not None and t - s["dead"] < 0.3:
                img = boom(img, s["dx"], s["dy"], (t - s["dead"]) / 0.3, "tiny", sparks=TEAL)
        return img


OR_DUR = 6.4
OR_SCROLL = 100.0
ORGANS = [dict(x=140, y0=-20, hp=183.0), dict(x=330, y0=-100, hp=183.0)]
OR_KEYS = [(0.0, 330, 470), (0.8, 330, 470), (2.0, 330, 205), (2.9, 330, 300), (3.3, 230, 420), (3.75, 140, 470),
           (6.4, 160, 470)]


def or_scene(v):
    bg = Scroll(reef_bg, 45, OR_DUR, OR_SCROLL)
    seeds = Seeds()
    fire = Fire(rate=10)
    st = [dict(d, died=None, rel=[], held=False) for d in ORGANS]
    frames, hits = [], []
    for t in times(OR_DUR):
        f = bg.frame(t)
        sx, sy = path_at(OR_KEYS, t)
        if 2.0 <= t < 2.9:
            sy = st[1]["y0"] + OR_SCROLL * t + 105
        for o in st:
            o["cy"] = o["y0"] + OR_SCROLL * t
            top = -o["y0"] / OR_SCROLL                        # its centre crosses the top edge
            o["due"] = top + 1.5 + 3.0 * len(o["rel"])
            near = np.hypot(sx - o["x"], sy - o["cy"]) < 120
            o["held"] = near and o["died"] is None and t >= o["due"] - 0.5
            if o["died"] is None and not near and t >= o["due"] and o["cy"] < FH:
                o["rel"].append(t)
                if len(o["rel"]) % 2 == 1:
                    seeds.release(t, o["x"], o["cy"] - 24)

        def hit(bx, by, t=t):
            for o in st:
                if o["died"] is None and abs(bx - o["x"]) < 30 and abs(by - o["cy"]) < 30:
                    o["hp"] -= 11.0
                    hits.append((t, bx, by))
                    if o["hp"] <= 0:
                        o["died"] = t
                    return True
            for s in seeds.list:
                if s["dead"] is None and t >= s["t0"]:
                    x, y = seeds.pos(s, t)
                    if abs(bx - x) < 10 and abs(by - y) < 10:
                        s["dead"], s["dx"], s["dy"] = t, x, y
                        return True
            return False
        fire.step(t, sx, sy, hit, on=(1.8 <= t < 3.0) or (3.2 <= t < 4.6))
        for o in st:
            x, y = o["x"], o["cy"]
            if y < -60 or y > FH + 60:
                continue
            if o["died"] is None:
                swell = iris = opened = 0.0
                if not o["held"] and o["due"] - 0.5 <= t < o["due"]:
                    u = (t - (o["due"] - 0.5)) / 0.5
                    if len(o["rel"]) % 2 == 0:
                        swell = u
                    else:
                        iris = u
                if o["rel"] and t - o["rel"][-1] < 0.6 and len(o["rel"]) % 2 == 1:
                    opened = 1.0 - (t - o["rel"][-1]) / 0.6
                pulse = 0.5 + 0.5 * np.sin(t * TAU * 0.6)
                sp = organ_sprite(v, swell, iris, opened, 0.0, pulse)
                sprite.paste_center(f, sp, x, y)
                if swell > 0:
                    f = light(f, x, y - 18, 10 + 8 * swell, TEAL, 0.35 * swell)
                if y > FH * 2 / 3 and int(t * 4) % 2 == 0:            # the amber leaving blink, bottom third
                    d = ImageDraw.Draw(f)
                    for sxn in (-1, 1):
                        for syn in (-1, 1):
                            cx, cy = x + sxn * 40, y + syn * 40
                            d.line([(cx, cy), (cx - sxn * 10, cy)], fill=AMBER + (255,), width=2)
                            d.line([(cx, cy), (cx, cy - syn * 10)], fill=AMBER + (255,), width=2)
            else:
                u = t - o["died"]
                sprite.paste_center(f, organ_sprite(v, torn=1.0), x, y)
                if u < 0.25:
                    f = light(f, x, y, 22, (255, 220, 230), 0.7 * (1 - u / 0.25))
                if u < 0.9:
                    f = ring_light(f, x, y, 10 + 46 * u, (120, 255, 200) if v == "a" else (255, 130, 190),
                                   0.8 * (1 - u / 0.9), 3.0)
                f = flecks(f, x, y, u / 0.8, seed=4)
        f = seeds.draw(f, t)
        for t0, bx, by in hits:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, (255, 230, 200), 4, 6)
        fire.draw(f)
        ship(f, sx, sy)
        swelling = any(o["died"] is None and o["due"] - 0.5 <= t < o["due"] and not o["held"] for o in st)
        if any(o["held"] for o in st):
            caption(f, "THE SHIP WITHIN 120 PX: IT HOLDS ITS RELEASE")
        elif any(o["died"] is not None and t - o["died"] < 0.9 for o in st):
            caption(f, "TORN: A FLASH, A RING OF ICHOR; THE HUSK STAYS ON THE PLATES")
        elif any(o["died"] is None and o["cy"] > FH * 2 / 3 for o in st):
            caption(f, "THE BOTTOM THIRD: IT BLINKS AMBER (AN ORGAN THAT LEAVES FAILS THE LEVEL)")
        elif swelling:
            caption(f, "THE SEED POD SWELLS 0.5 S...")
        elif any(o["rel"] and t - o["rel"][0] < 1.5 for o in st):
            caption(f, "...AND A WHIRL SEED CLUSTER OF 6 SPIRALS OUT")
        else:
            caption(f, "A SPAWNING ORGAN ON THE REEF (GROUND, NOT HARDENED)")
        frames.append(f)
    print("organ", v, [(o["died"] and round(o["died"], 2), [round(r, 2) for r in o["rel"]]) for o in st], flush=True)
    return frames, bg


def flecks(img, x, y, s, seed=0):
    return e72.flecks(img, x, y, s, n=10, col=TEAL, seed=seed)


def or_sheet(v, frames, bg):
    idle = organ_sprite(v, pulse=0.5)
    states = [idle, organ_sprite(v, swell=0.5, pulse=0.5), organ_sprite(v, swell=1.0, pulse=0.5),
              organ_sprite(v, opened=1.0, pulse=0.5), organ_sprite(v, iris=1.0, pulse=0.5), organ_sprite(v, torn=1.0)]
    seed = Image.new("RGBA", (OR_N, OR_N), (0, 0, 0, 0))
    sprite.paste_center(seed, WHIRL[0], OR_N / 2, OR_N / 2)
    panels = [("SOURCE: 260 PX", source_panel(lambda: organ_model(v, 0.5), OR_EXT)),
              ("STATES (2X): WHOLE / SWELL 0.25 S / 0.5 S / RELEASED / IRIS / TORN; A WHIRL SEED",
               row(states + [seed, e60.player_cell(56)], 100, 2,
                   ["WHOLE", "SWELL", "SWOLLEN", "RELEASED", "IRIS", "TORN", "SEED", "PLAYER"])),
              ("READABILITY: LEVEL 45, THE REEF", bands_check([(idle, "WHOLE"), (states[5], "TORN")], bg.img, scales=(1,),
                                                              names=("BRIGHTEST", "DARKEST"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.35, 1.9, 2.5, 3.1, 4.0],
                                              ["SWELL", "CLUSTER", "HELD", "TORN", "AMBER"], 0.4))]
    if v == "a":
        notes = ["SPAWNING ORGAN A - TEAL BULB: THE HIVE NODE'S TEAL-BLACK SPAWNER FLESH, ONE SEED POD ON ITS CROWN",
                 "80X80 PX (96 CANVAS), GROUND. A ROUND SAC ON SEVEN THICK ROOTS RUNNING INTO THE PLATES, TEAL VEINS (THE SPAWNERS'",
                 "GLOW: 'IT MAKES THINGS'), ONE BONE-GREEN SEED POD ON ITS CROWN THAT SWELLS AND GLOWS 0.5 S, THEN TEARS OPEN",
                 "IN A CROSS AS THE WHIRL SEEDS SPIRAL OUT; AN IRIS ON ITS FRONT FOR THE SKITTER PAIR (THE HIVE NODE'S 0.5 S",
                 "TELL). TORN: THE SAC COLLAPSES, HOLED, THE VEINS DARK. TEAL-BLACK OVER THE PINK REEF: THE STRONGEST CONTRAST,",
                 "AND THE HIVE NODE'S FAMILY AT A GLANCE."]
    else:
        notes = ["SPAWNING ORGAN B - REEF POLYP: THE REEF'S OWN PINK FLESH, A RING OF SIX SEED PODS ROUND A CROWN IRIS",
                 "THE SAME SIZE AND TIMING. A TALLER RIBBED SAC ON NINE THINNER ROOTS, GROWN FROM THE REEF ITSELF (ITS PINK, A",
                 "STEP BRIGHTER THAN THE PLATES), THE SAME TEAL VEINS; THE SIX PODS SWELL TOGETHER AND BURST, THE IRIS IN THE",
                 "MIDDLE OPENS FOR THE SKITTERS. IT BELONGS TO THE REEF MORE, BUT READS LESS AGAINST IT (THE TEAL AND ITS",
                 "SHAPE CARRY IT). SAME SCENE AS A."]
    return sheet(f"SPAWNING ORGAN {v.upper()} - LEVEL 45'S TARGETS (HIVE REEF)", panels, notes, frames[int(1.9 * FPS)])


@unit("spawning-organ")
def make_organ(variants):
    for v in variants:
        frames, bg = or_scene(v)
        save("campaign/act-7-beyond-the-gate/level-45-hive-reef", "spawning-organ", v, or_sheet(v, frames, bg), frames)


def main(args):
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    names = [a for a in args if a in UNITS] or list(UNITS)
    bad = [a for a in args if a not in UNITS and a not in ("a", "b")]
    if bad:
        sys.exit(f"unknown: {bad}; units: {', '.join(UNITS)}")
    for n in names:
        t0 = time.time()
        UNITS[n](variants)
        print(f"{n}: {time.time() - t0:.0f} s", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
