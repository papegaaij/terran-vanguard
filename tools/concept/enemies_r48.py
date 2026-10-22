#!/usr/bin/env python3
"""Concept round 48 - the new Act 4 "Deep Water" units (Europa, under the ice), a/b proposals.

Each subject gets a PNG sheet (source render, native sprites and states, the mechanic's key frames,
a frame from the play field at 1x) and a GIF of it in the 480x540 play field under the under-water
grade of its first level, with the player for scale. Models are built in this file on the frozen
``render`` package (sdf, enemy_rigs, enemy_models role colours), like enemies_r36.py.

The under-water grade (round 48 Q2 a, "lamp-lit to black", pending the user's review) darkens level
by level: L23 the lamp-lit Thera Deep farms (the chosen r07 kelp-farm floor, caustics), L24 the plain
at the lamps' edge (light from behind, the far water dark), L25 the trench (dark walls, red-gold
relay beacons), L26 black (the headlight cone only: outside it a unit shows only its glow, Q4 a),
L27 the vents (orange vent glow). Units on the play plane are lit by the same light map and tinted a
little toward the water colour; enemy bullets stay bright (readability) and are slowed x 0.7.

Outputs (design/enemies/...):
  naval/concept/eel-swarm-r48-{a,b}.{png,gif}     12-segment eel chain, head fires aimed shots and
                                                  fans, lateral stripe runs to the head as the tell;
                                                  a cut scatters the tail, the head kill unzips it
                                                  (a: rust ribbon eel, b: armoured lantern eel)
  naval/concept/abyss-ray-r48-{a,b}.{png,gif}     200 px manta: a shadow grows, it rises, sheds four
                                                  Barbfin escorts, three 9-way fans, glides off and
                                                  sinks (a: teal-black manta, b: bone skate)
  naval/concept/siren-r48-{a,b}.{png,gif}         trench-wall throat-sac nests, teal tell, sonar ring
                                                  that slows the ship 2 s (shimmer), then 3 s immune
                                                  (a: conch throat, b: frill bell)
  naval/concept/glow-angler-r48-{a,b}.{png,gif}   ambusher with a teal lure in the black water next
                                                  to a real salvage pickup; lunge, ring of 8
                                                  (a: rust angler, b: teal-black gulper)
  naval/concept/depth-hunter-r48-{a,b}.{png,gif}  unmarked grey sub drone (Q11 a), a column from the
                                                  rear, slow shootable homing torpedoes up the screen
                                                  (a: pump-jet cigar, b: flat glider)
  naval/concept/glassfin-r48-{a,b}.{png,gif}      tiny glass fry in ribbons, glint and ram, glass
                                                  shatter deaths (a: needle fry, b: leaf larva)
  naval/concept/barbfin-r48-{a,b}.{png,gif}       small needle spitter: darts in, holds, turns to the
                                                  ship (32 angles) (a: bone lionfish, b: plated box)
  ground/concept/drill-rig-r48-{a,b}.{png,gif}    L22 hardened Vrell drill rig on the ice shelf: spinning
                                                  collar, four pumps, a ring of 10 lime ice shards;
                                                  bombs take the pumps, then the core; a steaming crack
                                                  (a: bone bore tower, b: rust auger flower)

Run: python3 tools/concept/enemies_r48.py [eel-swarm] [abyss-ray] [siren] [glow-angler]
     [depth-hunter] [glassfin] [barbfin] [drill-rig] [a|b]   (no args = everything, ~20 minutes)
"""
import sys
from dataclasses import replace
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import enemies_r05b as r5b  # noqa: E402
import enemies_r06 as e6  # noqa: E402
import enemies_r36 as e36  # noqa: E402  (unmarked grey livery, folded(), sd_torus_z)
from render import archetype_models as am  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import (A_ENGINE, A_FACET, A_GLASS, A_GUN, A_HULL, A_RED,  # noqa: E402
                                 V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SAC, V_SEAM, hx)
from render.sdf import (Material, mirror_x, panel_lines, rotate_z, sd_box, sd_capsule,  # noqa: E402
                        sd_cylinder_y, sd_cylinder_z, sd_ellipsoid, sd_fin, sd_plate, sd_sphere,
                        subtract, union)

r5.SUB = "CONCEPT ROUND 48 - 960X540"
FW, FH, FPS, TAU = r5.FW, r5.FH, r5.FPS, 2 * np.pi
grid = r5.grid
source_panel = r5b.source_panel
folded, sd_torus_z = e36.folded, e36.sd_torus_z
DOWN, UP = np.pi / 2, -np.pi / 2

WATER = np.array((8, 74, 92), dtype=np.float64)       # r07's water colour
LIME = (168, 255, 42)
TEAL = (0, 255, 154)
VIOLET = (154, 77, 255)
CRIMSON = (255, 48, 56)
LIME_F, TEAL_F, VIOLET_F = (np.array(c) / 255.0 for c in (LIME, TEAL, VIOLET))
WATER_SLOW = 0.7                                       # bullets x 0.7 under water

em.ROLE_SCHEMES.update({
    "eel-swarm-a":   ("rust", "violet", "lime"),       # fast chain; violet = aimed; lime gills
    "eel-swarm-b":   ("teal-black", "violet", "lime"),
    "abyss-ray-a":   ("teal-black", "violet", "lime"), # carrier family; fans; lime gill slits
    "abyss-ray-b":   ("bone", "violet", "lime"),
    "siren-a":       ("slate", "teal", "teal"),        # rooted; teal throat sac (the spec's tell)
    "siren-b":       ("slate", "teal", "teal"),
    "glow-angler-a": ("rust", "crimson", "crimson"),   # diver: the lunge is a line of danger
    "glow-angler-b": ("teal-black", "crimson", "crimson"),
    "glassfin-a":    ("plum", "teal", "teal"),         # fodder, contact
    "glassfin-b":    ("plum", "teal", "teal"),
    "barbfin-a":     ("bone", "violet", "violet"),     # gunner (the Needler's role)
    "barbfin-b":     ("bone", "violet", "violet"),
    "drill-rig-a":   ("slate", "lime", "lime"),        # rooted ground structure, lime shards
    "drill-rig-b":   ("rust", "lime", "lime"),
})


def out(category, name):
    d = r5.DESIGN / category / "concept"
    d.mkdir(parents=True, exist_ok=True)
    return d / name


def save_unit(category, slug, variant, sheet, frames, fps=FPS):
    png = out(category, f"{slug}-r48-{variant}.png")
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, out(category, f"{slug}-r48-{variant}.gif"), fps=fps)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)")


def times(seconds):
    return [i / FPS for i in range(int(seconds * FPS))]


def smooth(x):
    x = np.clip(x, 0.0, 1.0)
    return x * x * (3 - 2 * x)


def vmats(unit, glow=1.0, style="a", extra=()):
    """Role-colour Vrell slots (V_BODY .. V_SAC) plus extra slots from index 7 on."""
    return am.vrell(unit, glow, style) + list(extra)


def emissive(col, k=1.0):
    return Material((0.03, 0.03, 0.03), emission=tuple(np.asarray(col, dtype=np.float64) * k))


# --------------------------------------------------------------------------- glow-only sprites

def _glow_alpha(sp):
    a = np.array(sp).astype(np.float64)
    lum = a[..., :3].max(axis=-1)
    a[..., 3] = np.where(a[..., 3] > 0, np.clip((lum - 18) * 2.4, 0, 255), 0)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def blackened(make):
    """The same model with every surface black: only the emission is left (the unit's glow)."""
    def make2(**kw):
        scene, mats = make(**kw)
        return scene, [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None)
                       for m in mats]
    return make2


class Lit:
    """A unit's angle set and its glow-only twin (for the dark levels)."""

    def __init__(self, make, size, count=16, factor=5, colors=32, post=None, extent=2.3):
        self.full = rig.ModelSpaceAngleSprites(make, size, count, extent=extent, factor=factor,
                                               colors=colors, post=post)
        self.glow = rig.ModelSpaceAngleSprites(blackened(make), size, count, extent=extent,
                                               factor=factor, colors=24, post=_glow_alpha)
        self.make, self.size, self.count, self.extent = make, size, count, extent

    def get(self, heading, **kw):
        return self.full.get(heading, **kw)

    def frame(self, k, **kw):
        return self.full.frame(k, **kw)

    def glow_get(self, heading, **kw):
        return self.glow.get(heading, **kw)

    @property
    def scale(self):
        return self.size / self.extent


def lit(sp, k, tint=0.0, glow=None, gain=1.0):
    """``sp`` dimmed to light level ``k`` and tinted toward the water; the glow-only twin is
    max-blended on top so the glows stay bright in the dark."""
    a = np.array(sp).astype(np.float64)
    rgb = a[..., :3] * (1 - tint) + WATER * tint
    a[..., :3] = rgb * min(k, 1.15)
    if glow is not None:
        g = np.array(glow).astype(np.float64)
        ga = g[..., 3:4] / 255.0
        a[..., :3] = np.maximum(a[..., :3], g[..., :3] * ga * gain)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def fade(sp, alpha):
    a = sp.copy()
    a.putalpha(a.getchannel("A").point(lambda v: int(v * alpha)))
    return a


def flash(sp, on):
    return e6.tint(sp, (255, 255, 255), 0.55) if on else sp


# --------------------------------------------------------------------------- water grade

FLOOR_STOPS = [(0.0, (4, 22, 30)), (0.35, (10, 50, 58)), (0.65, (22, 86, 88)),
               (0.9, (52, 124, 116)), (1.0, (96, 156, 138))]        # r07 kelp-forest floor ramp


def seafloor(seed, stops=FLOOR_STOPS, stones=18, coral=10):
    """A plain silt sea floor in the r07 colours (no domes or farms): fbm dunes with ripples,
    scattered stones and small Vrell coral tufts, posterized like the game's backdrops."""
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:FH, 0:FW]
    hgt = raster.fbm(FW, FH, 96, seed, octaves=4, period=False) * 0.7
    ridge = 1 - np.abs(2 * raster.fbm(FW, FH, 48, seed + 1, octaves=2, period=False) - 1)
    hgt += 0.3 * ridge ** 3
    warp = raster.fbm(FW, FH, 64, seed + 2, octaves=2, period=False)
    hgt += 0.025 * np.sin((xx * 0.3 + yy + warp * 60) / 5.0)
    for _ in range(stones):
        cx, cy, r = rng.uniform(0, FW), rng.uniform(0, FH), rng.uniform(3, 9)
        d = np.hypot(xx - cx, (yy - cy) * 1.2) / r
        hgt += np.sqrt(np.clip(1 - d * d, 0, 1)) * 0.1
    shade = e3._hillshade(hgt * 60, 0.6)
    t = (hgt - hgt.min()) / (np.ptp(hgt) + 1e-9)
    col = raster.ramp(stops, 0.1 + 0.8 * t) * shade[..., None]
    img = Image.fromarray(np.clip(col, 0, 255).astype(np.uint8), "RGB")
    img = img.filter(ImageFilter.GaussianBlur(1.2)).quantize(14, dither=Image.Dither.NONE)
    img = img.convert("RGBA")
    d = ImageDraw.Draw(img)
    for _ in range(coral):                                     # Vrell coral tufts
        cx, cy = rng.uniform(10, FW - 10), rng.uniform(10, FH - 10)
        hue = (120, 40, 140) if rng.uniform() < 0.6 else (30, 120, 110)
        for _ in range(int(rng.integers(5, 10))):
            a, rr = rng.uniform(0, TAU), rng.uniform(1, 7)
            x, y = cx + np.cos(a) * rr, cy + np.sin(a) * rr
            d.ellipse([x - 1.5, y - 1.5, x + 1.5, y + 1.5], fill=hue + (255,))
            d.point((x - 0.5, y - 0.5), fill=tuple(min(255, c + 70) for c in hue) + (255,))
    return img


@lru_cache(maxsize=None)
def farm_floor():
    """The chosen r07 kelp-forest floor (Thera Deep farms and domes), one still (slow: ~20 s)."""
    import scenes_r07 as s7
    return s7.EuropaR07("a").ground_image(0).convert("RGBA")


GRADES = {   # amb(y) = a0 + a1 * y / FH; cone = headlight gain; caustic; unit tint; snow brightness
    "lamp":   dict(level="L23 THERA DEEP FARMS (LAMP-LIT)", a0=1.0, a1=0.0, cone=0.18, caustic=0.3,
                   tint=0.08, snow=0.8, bg=0.72),
    "edge":   dict(level="L24 ABYSSAL PLAIN (LAMPS' EDGE)", a0=0.3, a1=0.6, cone=0.6, caustic=0.12,
                   tint=0.14, snow=0.6),
    "trench": dict(level="L25 SIREN TRENCH (DARK, BEACONS)", a0=0.3, a1=0.08, cone=0.85,
                   caustic=0.0, tint=0.18, snow=0.45),
    "black":  dict(level="L26 BLACKWATER (HEADLIGHT ONLY)", a0=0.05, a1=0.0, cone=1.05,
                   caustic=0.0, tint=0.22, snow=0.35, cw=(16, 0.24), halo=48),
    "vent":   dict(level="L27 HYDROTHERMAL VENTS (VENT GLOW)", a0=0.3, a1=0.06, cone=0.8,
                   caustic=0.0, tint=0.16, snow=0.4),
}


def wall_l(y):
    return 78 + 16 * np.sin(y / 70.0 + 1.0) + 7 * np.sin(y / 23.0)


def wall_r(y):
    return FW - (78 + 16 * np.sin(y / 55.0) + 7 * np.sin(y / 29.0 + 2.0))


class Water:
    """A level's under-water backdrop and its light map. ``frame(t, px, py)`` returns the lit
    backdrop with caustics, the headlight's beam in the water and marine snow; ``light(x, y)``
    is the light level a unit at (x, y) gets in the frame last built."""

    def __init__(self, grade, seed=11, base=None, walls=False, vents=(), beacons=()):
        self.g = GRADES[grade]
        self.grade = grade
        self.base = base if base is not None else seafloor(seed)
        self.walls, self.vents, self.beacons = walls, list(vents), list(beacons)
        if walls:
            self.base = self._walls(self.base)
        for vx, vy, vr in self.vents:
            self._chimney(self.base, vx, vy, vr)
        self.yy, self.xx = np.mgrid[0:FH, 0:FW].astype(np.float64)
        self.amb = self.g["a0"] + self.g["a1"] * self.yy / FH
        for vx, vy, vr in self.vents:
            self.amb = self.amb + 0.75 * np.exp(-((self.xx - vx) ** 2 + (self.yy - vy) ** 2)
                                                / (2 * (vr * 2.6) ** 2))
        rng = np.random.default_rng(seed + 5)
        self.snow = [(rng.uniform(0, FW), rng.uniform(0, FH), rng.uniform(6, 22),
                      rng.uniform(0, TAU), rng.uniform(0.4, 1.0)) for _ in range(90)]
        self.px, self.py = 240, 488

    def _walls(self, img):
        a = np.array(img).astype(np.float64)
        yy = np.arange(FH)[:, None]
        xx = np.arange(FW)[None, :]
        xl, xr = wall_l(yy), wall_r(yy)
        strata = 0.6 + 0.14 * np.sin((xx + yy * 0.4) / 5.0) + 0.06 * np.sin(yy / 2.6)
        rock = np.array((84, 104, 112)) * strata[..., None]
        left, right = xx < xl, xx > xr
        a[..., :3] = np.where((left | right)[..., None], rock, a[..., :3] * 0.7)
        lit_edge = ((xx > xr) & (xx - xr < 4)) | ((xx < xl) & (xl - xx < 3))
        a[lit_edge, :3] *= 1.7
        sh = np.clip((xx - xl) / 18.0, 0, 1) * 0.4 + 0.6
        a[..., :3] = np.where((~left & (xx - xl < 18))[..., None], a[..., :3] * sh[..., None],
                              a[..., :3])
        im = Image.fromarray(np.clip(a[..., :3], 0, 255).astype(np.uint8), "RGB")
        return im.filter(ImageFilter.GaussianBlur(1.0)).quantize(16, dither=Image.Dither.NONE) \
            .convert("RGBA")

    def _chimney(self, img, x, y, r):
        """A hydrothermal chimney seen from above: a dark mineral cone with a hot glowing mouth."""
        lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
        d = ImageDraw.Draw(lay)
        for k in range(6, 0, -1):
            rr = r * (0.35 + 0.65 * k / 6)
            c = int(18 + 10 * (6 - k))
            d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=(c, c - 4, c - 6, 255))
        d.ellipse([x - r * 0.3, y - r * 0.3, x + r * 0.3, y + r * 0.3], fill=(255, 150, 60, 255))
        d.ellipse([x - r * 0.15, y - r * 0.15, x + r * 0.15, y + r * 0.15],
                  fill=(255, 236, 170, 255))
        img.alpha_composite(lay)

    def light(self, x, y):
        x, y = float(np.clip(x, 0, FW - 1)), float(np.clip(y, 0, FH - 1))
        return float(self.lm[int(y), int(x)])

    def frame(self, t, px, py):
        self.px, self.py = px, py
        xx, yy = self.xx, self.yy
        dx, dy = xx - px, yy - py
        hr = self.g.get("halo", 70)
        halo = np.exp(-(dx * dx + dy * dy) / (2 * hr ** 2))
        ahead = np.maximum(0, -dy)
        c0, c1 = self.g.get("cw", (26, 0.5))
        cone = np.exp(-(dx / (c0 + c1 * ahead)) ** 2) * (dy < 8) * np.exp(-ahead / 380)
        beam = np.maximum(halo * 0.8, cone)
        self.lm = np.clip(self.amb + self.g["cone"] * beam, 0, 1.25)
        a = np.array(self.base).astype(np.float64)
        k = self.lm * self.g.get("bg", 1.0)                       # the floor sits a bit darker
        if self.g["caustic"]:
            c1 = np.sin(xx * 0.085 + t * 1.6 + 2.2 * np.sin(yy * 0.045 + t * 0.9))
            c2 = np.sin(yy * 0.075 - t * 1.3 + 2.0 * np.sin(xx * 0.05 - t * 0.7))
            caus = np.clip(c1 * c2, 0, 1) ** 3
            k = k * (1 + self.g["caustic"] * caus * np.clip(self.amb, 0, 1))
        k = np.round(k * 10) / 10                                # posterized light steps
        haze = np.clip(0.45 * (1 - np.clip(self.lm, 0, 1)), 0, 0.45)[..., None] * 0.35
        rgb = a[..., :3] * k[..., None] * (1 - haze) + WATER * haze * 0.6
        rgb += (0.10 * self.g["cone"] * cone)[..., None] * np.array((150, 200, 210))   # the beam
        for vx, vy, vr in self.vents:                            # hot water above the mouths
            pulse = 0.75 + 0.25 * np.sin(t * 2.3 + vx)
            h = np.exp(-((xx - vx) ** 2 + (yy - vy + 4) ** 2) / (2 * (vr * 1.4) ** 2)) * pulse
            rgb += h[..., None] * np.array((120, 50, 10))
        img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        d = ImageDraw.Draw(img)
        for bx, by, ph in self.beacons:                          # human relay beacons, red-gold
            on = (t * 1.25 + ph) % 1.0
            col = (255, 196, 64) if on < 0.12 else (200, 40, 30)
            d.rectangle([bx - 2, by - 2, bx + 2, by + 2], fill=(30, 30, 34, 255))
            d.point((bx, by), fill=col + (255,))
            if on < 0.12:
                img = raster.add_light(img, bx, by, 9, (255, 170, 60), 0.9)
                d = ImageDraw.Draw(img)
        d = ImageDraw.Draw(img)
        sb = self.g["snow"]
        for x0, y0, v, ph, b in self.snow:                       # marine snow, drifting down
            y = (y0 + t * v) % FH
            x = (x0 + 5 * np.sin(t * 0.7 + ph)) % FW
            lv = min(1.0, self.lm[int(y), int(x)])
            c = int(60 + 150 * b * lv * sb)
            d.point((x, y), fill=(c, min(255, c + 25), min(255, c + 30), 255))
        return img

    def put(self, img, lit_set, heading, x, y, flash_on=False, alpha=1.0, glow_gain=1.0, **kw):
        """Paste a unit lit by the frame's light map (its glow stays bright)."""
        k = self.light(x, y)
        k = round(k * 20) / 20
        sp = lit_set.get(heading, **kw)
        g = lit_set.glow_get(heading, **kw) if isinstance(lit_set, Lit) else None
        tint = self.g["tint"] * (1.2 - min(1.0, k))
        im = lit(sp, max(k, 0.0), tint, g, glow_gain)
        if flash_on:
            im = flash(im, True)
        if alpha < 1.0:
            im = fade(im, alpha)
        sprite.paste_center(img, im, x, y)


# --------------------------------------------------------------------------- shots and effects

class Shots:
    """Straight enemy shots: (t0, x, y, vx, vy, kind). Speeds are base speeds: x 0.7 here."""

    def __init__(self):
        self.list = []

    def fire(self, t0, x, y, tx, ty, speed, kind="orb"):
        d = np.hypot(tx - x, ty - y) or 1
        s = speed * WATER_SLOW
        self.list.append((t0, x, y, (tx - x) / d * s, (ty - y) / d * s, kind))

    def fan(self, t0, x, y, aim, n, spread, speed, kind="orb"):
        for a in np.linspace(-spread / 2, spread / 2, n) + aim:
            self.fire(t0, x, y, x + np.cos(a), y + np.sin(a), speed, kind)

    def ring(self, t0, x, y, n, speed, kind="orb", phase=0.0):
        for a in np.linspace(0, TAU, n, endpoint=False) + phase:
            self.fire(t0, x, y, x + np.cos(a), y + np.sin(a), speed, kind)

    def draw(self, img, t, until=None):
        for t0, x, y, vx, vy, kind in self.list:
            if t < t0 or (until is not None and t > until):
                continue
            bx, by = x + vx * (t - t0), y + vy * (t - t0)
            if -20 < bx < FW + 20 and -20 < by < FH + 20:
                if kind == "orb":
                    e3.orb(img, bx, by, 4)
                elif kind == "needle":
                    e3.thorn(img, bx, by, np.arctan2(vx, vy), 7)
                elif kind == "shard":
                    e3.thorn(img, bx, by, np.arctan2(vx, vy), 6, ring=LIME)


def ship(img, px, py, t, shots=True, rear=False, stop_y=None):
    sprite.paste_center(img, e3.player_sprite(), px, py)
    if shots:
        for k in range(4):
            y = py - 40 - ((t * 300 + k * 90) % 420)              # x 0.7: slower bolts too
            if y > -10 and (stop_y is None or y > stop_y):
                e3.bolt(img, px - 10, y)
                e3.bolt(img, px + 10, y)
    if rear:
        for k in range(3):
            y = py + 30 + ((t * 300 + k * 110) % 330)
            if y < FH + 10:
                e3.bolt(img, px, y)


def uw_burst(img, x, y, s, size=1.0, col=(255, 170, 90), ring=True):
    """Under-water kill: a short flash, a pale pressure ring and debris, no fire ball."""
    if not 0 <= s <= 1:
        return img
    rr = (6 + 16 * s) * size
    img = raster.add_light(img, x, y, rr * 1.4, col, (1.0 - s) * 1.1)
    d = ImageDraw.Draw(img)
    if s < 0.35:
        r0 = rr * 0.5 * (1 - s)
        d.ellipse([x - r0, y - r0, x + r0, y + r0], fill=(255, 240, 210, int(255 * (1 - s / 0.35))))
    if ring:
        r1 = (10 + 46 * s) * size
        d.ellipse([x - r1, y - r1, x + r1, y + r1], outline=(170, 230, 230, int(170 * (1 - s))),
                  width=1)
    for k in range(6):
        a = k * 1.05 + 0.4
        dd = rr * (0.4 + 1.1 * s)
        d.rectangle([x + np.cos(a) * dd - 1, y + np.sin(a) * dd - 1 + 8 * s,
                     x + np.cos(a) * dd + 1, y + np.sin(a) * dd + 1 + 8 * s],
                    fill=(90, 70, 80, int(255 * (1 - s))))
    return img


def label(img, x, y, text, col=(230, 240, 250)):
    raster.draw_text(img, int(x), int(y), text, col)


def caption(img, text, t, t0, t1):
    """A radio caption at the bottom of the field (as the in-level radio lines)."""
    if not t0 <= t < t1:
        return
    w = raster.text_width(text) + 12
    d = ImageDraw.Draw(img)
    d.rectangle([8, 8, 8 + w, 22], fill=(10, 14, 22, 200), outline=(80, 110, 140, 255))
    raster.draw_text(img, 14, 12, text, (255, 210, 120))


def edge_warning(img, t, xs, label_="REAR!"):
    e6.edge_warning(img, t, xs, label_)


def thumbs(items, size, scale=2, labels=None, bg=None):
    """Sprites centred on ``size`` cells (checker or a colour) as a 1-row grid."""
    cells = []
    for it in items:
        c = Image.new("RGBA", (size, size), (0, 0, 0, 0))
        sprite.paste_center(c, it, size // 2, size // 2)
        cells.append(c)
    g = grid(cells, len(cells), scale, labels=labels)
    return g


def on_bg(items, size, bgcol, scale=2, labels=None):
    """Like ``thumbs`` on a flat water colour (for the dark / glow-only states)."""
    w = size * scale
    lab = 10 if labels else 0
    img = Image.new("RGBA", (len(items) * (w + 6), w + lab + 6), raster.SHEET_BG)
    for i, it in enumerate(items):
        c = Image.new("RGBA", (size, size), bgcol + (255,))
        sprite.paste_center(c, it, size // 2, size // 2)
        img.alpha_composite(sprite.enlarge(c, scale), (i * (w + 6), 0))
        if labels:
            raster.draw_text(img, i * (w + 6), w + 2, labels[i], raster.LABEL_DIM)
    return img


def player_cell(size=48):
    c = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sprite.paste_center(c, e3.player_sprite(), size // 2, size // 2)
    return c


def sheet(title, panels, notes, game_frame):
    return r5.build_sheet(title, panels, notes, game_frame)


# ===================================================================== 1 Eel Swarm (L23)

E_STRIPE = 7                                           # the lateral stripe slot (violet)


def _eel_mats(v, stripe, glow=1.0):
    return vmats(f"eel-swarm-{v}", glow, "a" if v == "a" else "b",
                 [emissive(VIOLET_F, (0.25 + 1.35 * stripe) * glow)])


def eel_segment(v, stripe=0.0, tail=False, glow=1.0):
    """One small eel of the swarm (faces +Y): a thick tapering body with a broad lateral stripe,
    small pectoral fins and a tail fan. a: rust ribbon eel with a bone dorsal ribbon; b: plated
    lantern eel with three photophores a side and a bone spine ridge."""
    L = 0.8 if not tail else 0.66

    def scene(p):
        q = mirror_x(p)
        items = [(sd_capsule(p, (0, L * 0.55, 0.02), (0, -L * 0.95, 0.0), 0.36, 0.12), V_BODY),
                 (sd_ellipsoid(p, (0, L * 0.72, 0.04), (0.3, 0.36, 0.24)), V_BODY),
                 (sd_plate(q, [(0.25, L * 0.45), (0.56, L * 0.2), (0.5, L * 0.05), (0.25, L * 0.18)],
                           0.0, 0.025), V_BONE)]
        if v == "a":
            items += [(sd_fin(p, [(L * 0.55, 0.26), (L * 0.1, 0.44), (-L * 0.9, 0.24),
                                  (-L, 0.08), (L * 0.5, 0.08)], 0.0, 0.035), V_BONE),
                      (sd_capsule(q, (0.24, L * 0.62, 0.17), (0.12, -L * 0.8, 0.06), 0.085, 0.05),
                       E_STRIPE)]
        else:
            for k in range(3):
                y = L * (0.5 - 0.55 * k)
                items.append((sd_sphere(q, (0.27 - 0.06 * k, y, 0.16), 0.1 - 0.012 * k), E_STRIPE))
            items.append((sd_capsule(p, (0, L * 0.85, 0.3), (0, -L * 0.75, 0.14), 0.07, 0.04),
                          V_BONE))
        fin = [(0.0, -L * 0.75), (0.4 if tail else 0.3, -L * 1.3), (0.12, -L * 1.42), (0.0, -L * 1.2)]
        items.append((sd_plate(q, fin, 0.0, 0.03), V_BONE))
        items.append((sd_sphere(q, (0.17, L * 0.98, 0.18), 0.065), V_GLOW))
        return union(*items, k=0.05)
    return scene, _eel_mats(v, stripe, glow)


def eel_head(v, stripe=0.0, jaw=0.0, glow=1.0):
    """The lead eel (vital): a broad flat skull with long hooked jaws, violet eyes, lime gill
    frills on both sides (the weak point), the stripe and a short body with a tail fan.
    a: slim ribbon head with a bone crest; b: blunt armoured bone skull with a violet maw."""
    def scene(p):
        q = mirror_x(p)
        j = 0.07 + 0.13 * jaw
        items = [(sd_ellipsoid(p, (0, 0.3, 0.04), (0.52, 0.66, 0.3)), V_BODY),
                 (sd_capsule(p, (0, -0.1, 0.02), (0, -0.95, 0.0), 0.36, 0.13), V_BODY),
                 (sd_capsule(q, (0.16, 0.6, 0.06), (j + 0.05, 1.08, 0.02), 0.12, 0.05), V_BONE),
                 (sd_sphere(q, (0.3, 0.55, 0.22), 0.1), V_GLOW),
                 (sd_capsule(q, (0.3, 0.2, 0.2), (0.14, -0.75, 0.06), 0.09, 0.05), E_STRIPE)]
        for k in range(4):                               # lime gill frills
            items.append((sd_ellipsoid(q, (0.5, 0.18 - 0.11 * k, 0.06), (0.1, 0.05, 0.05)), V_EYE))
        if v == "a":
            items.append((sd_fin(p, [(0.4, 0.34), (-0.1, 0.52), (-0.9, 0.26), (0.4, 0.1)], 0.0,
                                 0.04), V_BONE))
        else:
            items += [(sd_ellipsoid(p, (0, 0.42, 0.24), (0.42, 0.5, 0.16)), V_BONE),
                      (sd_sphere(p, (0, 0.86, 0.06), 0.13 + 0.06 * jaw), V_GLOW)]
        fin = [(0.0, -0.8), (0.36, -1.12), (0.12, -1.15), (0.0, -1.02)]
        items.append((sd_plate(q, fin, 0.0, 0.03), V_BONE))
        return union(*items, k=0.05)
    return scene, _eel_mats(v, stripe, glow)


EEL_HEAD, EEL_SEG, EEL_TAIL = 48, 40, 34
EEL_N = 12                                             # segments (the spec's default)
EEL_GAP = [30] + [24] * (EEL_N - 1) + [22]             # head->seg1, seg->seg, seg->tail (px)
eel = {}
for _v in "ab":
    eel[_v] = dict(
        head=Lit(lambda stripe=0.0, jaw=0.0, v=_v: eel_head(v, stripe, jaw), EEL_HEAD, 16,
                 extent=2.05),
        seg=Lit(lambda stripe=0.0, v=_v: eel_segment(v, stripe), EEL_SEG, 16, extent=1.8),
        tail=Lit(lambda stripe=0.0, v=_v: eel_segment(v, stripe, tail=True), EEL_TAIL, 16,
                 extent=1.8))


def eel_route(t):
    base = EEL_PATH(t)
    h = rig.path_heading(EEL_PATH, t)
    off = 36 * np.sin(TAU * 0.8 * t)                   # the spec's sine about the route
    return base[0] - np.sin(h) * off * 0.5, base[1] + np.cos(h) * off * 0.5


EEL_PATH = rig.catmull_rom([(150, -90), (160, 110), (320, 210), (370, 330), (230, 390),
                            (110, 300), (80, 120), (150, -160)],
                           [0.0, 1.9, 3.6, 4.8, 6.2, 7.6, 9.0, 11.0])
EEL_SHOTS = [1.4, 3.2, 5.0, 6.8]                       # aimed every 1.8 s
EEL_FANS = [3.2]                                       # the 3-way fan (every 5 s)
EEL_CUT = (8, 3.9)                                     # segment index 8 shot through at 3.9 s
EEL_HEAD_KILL = 6.9
EEL_DUR = 9.6


def eel_stripe(i, t):
    """The stripe runs from the tail to the head in the 0.3 s before each shot (the tell)."""
    n = EEL_N + 1
    for ts in EEL_SHOTS:
        u = 1 - (ts - t) / 0.3                          # 0 at the tail .. 1 at the head
        if -0.05 <= u <= 1.05:
            pos = 1 - i / n
            if abs(pos - u) < 0.2:
                return 1.0
    return 0.0


def eel_scene(v):
    water = Water("lamp", base=farm_floor())
    parts = eel[v]
    shots = Shots()
    for ts in EEL_SHOTS:
        if ts < EEL_HEAD_KILL:
            x, y = eel_route(ts)
            px, py = r5.player_at(ts)
            shots.fire(ts, x, y, px, py, 130, "orb")
    for ts in EEL_FANS:
        x, y = eel_route(ts + 0.05)
        px, py = r5.player_at(ts)
        shots.fan(ts + 0.05, x, y, np.arctan2(py - y, px - x), 3, np.radians(40), 120)
    frames, cut_state = [], None
    for t in times(EEL_DUR):
        px, py = r5.player_at(t)
        f = water.frame(t, px, py)
        tt = min(t, EEL_HEAD_KILL)
        pts, heads = rig.chain_at(eel_route, tt, EEL_GAP)
        if cut_state is None and t >= EEL_CUT[1]:
            cp, ch = rig.chain_at(eel_route, EEL_CUT[1], EEL_GAP)
            cut_state = [(np.array(p_), h_) for p_, h_ in zip(cp, ch)]
        idx = list(range(len(pts)))
        draws = []
        for i in reversed(idx):
            kind = "head" if i == 0 else ("tail" if i == EEL_N + 1 else "seg")
            x, y = pts[i]
            h = heads[i]
            if i == EEL_CUT[0] and t >= EEL_CUT[1]:
                s = (t - EEL_CUT[1]) / 0.5
                f = uw_burst(f, *cut_state[i][0], s, 0.7, VIOLET)
                continue
            if i > EEL_CUT[0] and t >= EEL_CUT[1]:          # the cut tail: drifts on, then dies
                p0, h0 = cut_state[i]
                dt = t - EEL_CUT[1]
                x = p0[0] + np.cos(h0) * 70 * dt
                y = p0[1] + np.sin(h0) * 70 * dt + 6 * np.sin(dt * 5 + i)
                h = h0 + 0.25 * np.sin(dt * 4 + i)
                die = EEL_CUT[1] + 3.0 + 0.07 * (i - EEL_CUT[0])
                if t >= die:
                    f = uw_burst(f, x, y, (t - die) / 0.45, 0.6, VIOLET, ring=False)
                    continue
                draws.append((parts[kind], h, x, y, 0.0, False, 1.0))
                continue
            if t >= EEL_HEAD_KILL:                          # unzips from the head, 0.07 s apart
                die = EEL_HEAD_KILL + 0.07 * i
                if t >= die:
                    s = (t - die) / 0.5
                    f = uw_burst(f, x, y + 10 * s, s, 1.6 if i == 0 else 0.7, VIOLET, ring=i == 0)
                    continue
            hit = (i == EEL_CUT[0] and EEL_CUT[1] - 0.45 < t < EEL_CUT[1]) or \
                (i == 0 and EEL_HEAD_KILL - 0.6 < t < EEL_HEAD_KILL)
            draws.append((parts[kind], h, x, y, eel_stripe(i, t), hit and int(t * 14) % 2, 1.0))
        for ps, h, x, y, st, hit, al in draws:
            kw = dict(stripe=st)
            if ps is parts["head"]:
                kw["jaw"] = 1.0 if any(0 <= ts - t < 0.25 for ts in EEL_SHOTS) else 0.0
            water.put(f, ps, h, x, y, hit, al, **kw)
        shots.draw(f, t)
        ship(f, px, py, t)
        frames.append(f)
    return frames


def eel_sheet(v, frames):
    P = eel[v]
    heads = [P["head"].frame(k) for k in range(16)]
    segs = [P["seg"].frame(k) for k in range(0, 16, 2)]
    states = [P["head"].get(DOWN), P["head"].get(DOWN, jaw=1.0), P["head"].get(DOWN, stripe=1.0),
              P["seg"].get(DOWN), P["seg"].get(DOWN, stripe=1.0), P["tail"].get(DOWN)]
    dark = [lit(P["head"].get(DOWN), 0.06, 0.2, P["head"].glow_get(DOWN)),
            lit(P["seg"].get(DOWN, stripe=1.0), 0.06, 0.2, P["seg"].glow_get(DOWN, stripe=1.0))]
    # a short straight chain at 1x
    chain = Image.new("RGBA", (360, 56), (0, 0, 0, 0))
    sprite.paste_center(chain, P["tail"].get(0.0), 18, 28)
    for i in range(EEL_N):
        st = eel_stripe(EEL_N - i, EEL_SHOTS[0] - 0.12)
        sprite.paste_center(chain, P["seg"].get(0.0, stripe=st), 40 + 24 * i, 28)
    sprite.paste_center(chain, P["head"].get(0.0), 40 + 24 * EEL_N + 8, 28)
    panels = [("SOURCE: HEAD, 260 PX", source_panel(lambda: eel_head(v))),
              ("SOURCE: ONE EEL OF THE BODY", source_panel(lambda: eel_segment(v, 1.0))),
              ("HEAD: 16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("BODY EEL: 8 OF 16 HEADINGS (1X)", grid(segs, 8, 1)),
              ("HEAD, JAW OPEN, STRIPE LIT; EEL, LIT; TAIL (2X)",
               thumbs(states, 48, 2, ["HEAD", "JAW", "STRIPE", "EEL", "LIT", "TAIL"])),
              ("IN THE DARK (L26): GLOW ONLY (2X)",
               on_bg(dark, 48, (2, 10, 14), 2, ["HEAD", "EEL"])),
              ("THE CHAIN, STRIPE RUNNING TO THE HEAD (1X)", chain)]
    name = {"a": "RIBBON EEL", "b": "LANTERN EEL"}[v]
    first = {"a": "EEL SWARM A - RIBBON EEL (RUST CHITIN, VIOLET STRIPE, LIME GILLS, BONE RIBBON FIN)",
             "b": "EEL SWARM B - LANTERN EEL (TEAL-BLACK PLATES, VIOLET PHOTOPHORES, BONE SKULL)"}[v]
    notes = [first,
             "A SWARM OF SMALL EELS SWIMMING AS ONE CHAIN: A 48 PX LEAD EEL (VITAL; JAWS, VIOLET EYES,",
             "LIME GILL CLUSTERS = THE WEAK POINT), 12 BODY EELS (40 PX CANVAS) AND A TAIL EEL, 16",
             "HEADINGS EACH, FOLLOWING THE HEAD'S PATH HISTORY. 0.3 S BEFORE EACH SHOT THE STRIPE RUNS",
             "FROM THE TAIL TO THE HEAD (THE TELL); THE HEAD OPENS ITS JAWS AND FIRES AIMED SHOTS",
             "AND A 3-WAY FAN (SLOWED X 0.7 UNDER WATER). GIF: A CUT AT EEL 8 - THE REAR PART",
             "DRIFTS ON 3 S AND DIES; THE HEAD KILL UNZIPS THE REST HEAD TO TAIL, 0.07 S APART.",
             "UNDER-WATER GRADE: L23, THE LAMP-LIT THERA DEEP FARMS (R07 FLOOR, MOVING CAUSTICS)."]
    if v == "b":
        notes[1] = "AS A, ARMOURED: PLATED BODY EELS WITH THREE VIOLET PHOTOPHORES A SIDE (THEY LIGHT"
        notes[2] = "IN A WAVE AS THE TELL), A BONE SKULL OVER THE LEAD EEL WITH A VIOLET MAW GLOW. 16"
    return sheet(f"EEL SWARM {v.upper()} - {name}, CHAIN OF EELS (LEVEL 23)", panels, notes,
                 frames[int(3.05 * FPS)])


def make_eel(variants):
    for v in variants:
        frames = eel_scene(v)
        save_unit("naval", "eel-swarm", v, eel_sheet(v, frames), frames)


# ===================================================================== 2 Barbfin (L24)

def barbfin(v, phase=0.0, glow=1.0):
    """Small Needler-role swimmer (faces +Y), a barbed snout tipped violet (the weak point).
    a: bone lionfish - a stubby body with five bone fin rays a side and webbing, a fan tail;
    b: plated box - a boxy bone-plated body with violet seams, short fins, a forked tail."""
    sw = 0.13 * np.sin(phase * TAU)

    def scene(p):
        q = mirror_x(p)
        items = [(sd_capsule(p, (0, 0.55, 0.04), (0, 1.12, 0.02), 0.08, 0.03), V_BONE),
                 (sd_sphere(p, (0, 1.1, 0.04), 0.07), V_GLOW),
                 (sd_sphere(q, (0.17, 0.48, 0.2), 0.07), V_GLOW)]
        for k in range(3):                                      # the snout's barbs
            y = 0.68 + 0.13 * k
            items.append((sd_capsule(q, (0.04, y, 0.04), (0.14, y - 0.09, 0.03), 0.025, 0.012),
                          V_BONE))
        tail = [(0.0, -0.62), (0.34 + sw, -1.05), (0.1 + sw, -1.12), (0.0, -0.95)]
        tailm = [(x - sw * 0.5, y) for x, y in tail]
        items.append((sd_plate(q, tailm, 0.0, 0.03), V_BONE))
        if v == "a":
            items.append((sd_ellipsoid(p, (0, 0.05, 0.06), (0.36, 0.68, 0.3)), V_BODY))
            for k in range(5):
                a0 = np.radians(18 + 26 * k)
                ln = 0.82 - 0.06 * abs(k - 2)
                x1, y1 = 0.2 + np.cos(a0) * ln, 0.15 - np.sin(a0) * ln * 0.9
                items.append((sd_capsule(q, (0.2, 0.15, 0.08), (x1, y1, 0.0), 0.04, 0.015), V_BONE))
            web = [(0.18, 0.3), (0.95, 0.05), (0.85, -0.45), (0.55, -0.68), (0.18, -0.2)]
            items.append((sd_plate(q, web, -0.02, 0.012), V_DARK))
            items.append((sd_capsule(p, (0, 0.4, 0.3), (0, -0.45, 0.26), 0.05, 0.03), V_SEAM))
        else:
            items += [(sd_box(p, (0, 0.02, 0.06), (0.36, 0.6, 0.24), 0.14), V_SEAM),
                      (sd_box(p, (0, 0.05, 0.22), (0.24, 0.42, 0.08), 0.06), V_BONE),
                      (sd_plate(q, [(0.3, 0.2), (0.7, -0.05), (0.62, -0.25), (0.32, -0.12)], 0.0,
                                0.025), V_BONE)]
        return union(*items, k=0.03)
    return scene, vmats(f"barbfin-{v}", glow, "a" if v == "a" else "b")


BARB_N = 40
barb = {v: Lit(lambda phase=0.0, v=v: barbfin(v, phase), BARB_N, 32, factor=5) for v in "ab"}


def barb_phase(t, i=0):
    return 0.5 if int(t * 6 + i) % 2 else 0.0


class Holder:
    """A Barbfin: an entry curve to its hold point, holds (turning to the ship, a needle every
    2.4 s, the first 0.9 s after it stops), then leaves toward the nearest side."""

    def __init__(self, pts, t0, dur, leave=None, kill=None, first=0.9, cadence=2.4):
        self.t0, self.dur = t0, dur
        n = len(pts)
        self.path = rig.catmull_rom(pts, [t0 + dur * k / (n - 1) for k in range(n)])
        self.hold = np.array(pts[-1], dtype=np.float64)
        self.leave, self.kill = leave, kill
        self.first, self.cadence = first, cadence

    def pos(self, t):
        if t < self.t0 + self.dur:
            return np.array(self.path(t)), rig.path_heading(self.path, t)
        dt = t - self.t0 - self.dur
        drift = np.array((6 * np.sin(dt * 0.9), 4 * np.sin(dt * 0.7)))
        if self.leave is not None and t > self.leave:
            side = -1 if self.hold[0] < FW / 2 else 1
            u = t - self.leave
            return self.hold + drift + np.array((side * 120 * u, -20 * u)), (0.0 if side > 0 else np.pi)
        return self.hold + drift, None

    def shots(self, end):
        out = []
        ts = self.t0 + self.dur + self.first
        while ts < end and (self.leave is None or ts < self.leave):
            if self.kill is None or ts < self.kill:
                out.append(ts)
            ts += self.cadence
        return out


def barb_group(group):
    hs = []
    if group == "column":
        for i in range(4):
            hold = (110 + 86 * i, 300 - 16 * (i % 2))
            pts = [(-30, 90 + 40 * i), (60 + 40 * i, 150 + 30 * i), (hold[0] - 20, hold[1] - 30),
                   hold]
            hs.append(Holder(pts, 0.2 + 0.45 * i, 1.7, leave=8.6 + 0.2 * i,
                             kill=4.6 if i == 1 else None))
        for i in range(3):
            hold = (360 - 95 * i, 390 + 14 * (i % 2))
            pts = [(FW + 30, 140 + 30 * i), (420 - 40 * i, 210 + 30 * i), (hold[0] + 25, hold[1] - 30),
                   hold]
            hs.append(Holder(pts, 3.3 + 0.4 * i, 1.6, kill=7.4 if i == 0 else None))
    return hs


def draw_barb(f, water, v, h, t, i, px, py, shots_t):
    p, head = h.pos(t)
    if h.kill is not None and t >= h.kill:
        s = (t - h.kill) / 0.8
        if s <= 1:                                         # sinking shell + an ink-grey cloud
            ink = Image.new("RGBA", f.size, (0, 0, 0, 0))
            d = ImageDraw.Draw(ink)
            r = 6 + 20 * s
            d.ellipse([p[0] - r, p[1] - r + 4, p[0] + r, p[1] + r + 4],
                      fill=(40, 46, 54, int(170 * (1 - s))))
            f.alpha_composite(ink.filter(ImageFilter.GaussianBlur(3)))
            shell = barb[v].get(DOWN + 0.6 * s)
            shell = fade(e6.dim(shell.resize((max(4, int(shell.width * (1 - 0.4 * s))),) * 2),
                                0.45), 1 - s)
            sprite.paste_center(f, shell, p[0], p[1] + 18 * s)
            f = uw_burst(f, p[0], p[1], s * 1.6, 0.7, VIOLET, ring=False)
        return f
    if head is None:
        head = np.arctan2(py - p[1], px - p[0])
    hit = h.kill is not None and h.kill - 0.4 < t < h.kill and int(t * 14) % 2
    water.put(f, barb[v], head, p[0], p[1], hit, phase=barb_phase(t, i))
    for ts in shots_t:
        if 0 <= t - ts < 0.1:
            f = raster.add_light(f, p[0], p[1], 10, VIOLET, 0.9)
    return f


def barb_scene(v):
    water = Water("edge", seed=24)
    hs = barb_group("column")
    shots = Shots()
    fired = []
    for h in hs:
        st = h.shots(10.0)
        fired.append(st)
        for ts in st:
            p, _ = h.pos(ts)
            px, py = r5.player_at(ts)
            shots.fire(ts, p[0], p[1], px, py, 150, "needle")
    frames = []
    for t in times(10.0):
        px, py = r5.player_at(t)
        f = water.frame(t, px, py)
        for i, h in enumerate(hs):
            if t >= h.t0:
                f = draw_barb(f, water, v, h, t, i, px, py, fired[i])
        shots.draw(f, t)
        ship(f, px, py, t)
        frames.append(f)
    return frames


def barb_sheet(v, frames):
    heads = [barb[v].frame(k) for k in range(0, 32, 2)]
    swim = [barb[v].get(DOWN, phase=0.0), barb[v].get(DOWN, phase=0.5)]
    dark = [lit(barb[v].get(DOWN), 0.06, 0.2, barb[v].glow_get(DOWN))]
    nd = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    sprite.paste_center(nd, e36.needler(0.0), 24, 24)
    panels = [("SOURCE, 260 PX", source_panel(lambda: barbfin(v))),
              ("16 OF 32 HEADINGS (2X)", grid(heads, 8, 2)),
              ("SWIM FRAMES; IN THE DARK (2X)",
               thumbs(swim + dark, 48, 2, ["TAIL L", "TAIL R", "DARK"])),
              ("NEXT TO A NEEDLER (AIR, ITS ROLE) AND THE PLAYER (2X)",
               thumbs([nd, barb[v].get(DOWN), player_cell()], 48, 2,
                      ["NEEDLER", "BARBFIN", "PLAYER"]))]
    if v == "a":
        notes = ["BARBFIN A - BONE LIONFISH (BONE CHITIN, VIOLET SNOUT BARB AND EYES)",
                 "36X28 PX (40 PX CANVAS), 32 HEADINGS X 2 TAIL FRAMES. A STUBBY BONE BODY WITH FIVE",
                 "BONE FIN RAYS A SIDE AND DARK WEBBING, A LONG BARBED SNOUT TIPPED VIOLET (THE WEAK",
                 "POINT, DRAWN ONLY), A FAN TAIL. THE NEEDLER'S ROLE UNDER WATER: DARTS IN ON A CURVE,",
                 "HOLDS 90-200 PX ABOVE THE SHIP'S LANE TURNING TO IT, SPITS AN AIMED NEEDLE EVERY",
                 "2.4 S (X 0.7), LEAVES TO THE NEAREST SIDE. DEATH: A SINKING SHELL AND AN INK-GREY",
                 "CLOUD. GIF: A COLUMN OF 4 FROM THE LEFT, 3 FROM THE RIGHT; L24'S PLAIN AT THE",
                 "LAMPS' EDGE (LIGHT FROM BEHIND, THE WATER AHEAD DARK, HEADLIGHT CONE)."]
    else:
        notes = ["BARBFIN B - PLATED BOX (BONE PLATES, VIOLET SEAMS AND SNOUT BARB)",
                 "36X28 PX (40 PX CANVAS), 32 HEADINGS X 2 TAIL FRAMES. A BOXY BONE-PLATED BODY WHOSE",
                 "PLATE GAPS GLOW VIOLET, SHORT FINS, A BARBED SNOUT TIPPED VIOLET, A FORKED TAIL. A",
                 "HEAVIER, MORE ARMOURED READ THAN A; THE SEAMS KEEP IT VISIBLE IN THE DARK LEVELS.",
                 "SAME DART-IN, HOLD, AIMED NEEDLE EVERY 2.4 S (X 0.7), LEAVE; SAME DEATH."]
    return sheet(f"BARBFIN {v.upper()} - SMALL NEEDLE SPITTER (LEVEL 24)", panels, notes,
                 frames[int(5.2 * FPS)])


def make_barb(variants):
    for v in variants:
        frames = barb_scene(v)
        save_unit("naval", "barbfin", v, barb_sheet(v, frames), frames)


# ===================================================================== 3 Abyss Ray (L24)

R_GILL = 7                                             # extra slot: lime gill slits


def abyss_ray(v, flap=0.0, glow=1.0):
    """A 200 px Vrell ray seen from above (faces +Y). ``flap`` -1..1 bends the wings up/down.
    a: manta - pointed wings, curled cephalic lobes, teal veins, a violet mouth, lime gill slits
    on the leading edges, a thin tail; b: bone skate - a rounded diamond disc, a ridge of bone
    tubercles down the back, violet spots, a long whip tail with a spine."""
    def wing_z(q):
        return 0.02 + flap * 0.22 * np.clip(q[:, 0] - 0.35, 0, None) ** 2

    def scene(p):
        q = mirror_x(p)
        items = []
        if v == "a":
            wing = [(0.0, 0.5), (0.3, 0.46), (0.62, 0.3), (0.95, 0.04), (1.13, -0.18 - 0.06 * flap),
                    (0.9, -0.2), (0.6, -0.3), (0.32, -0.5), (0.0, -0.56)]
            items += [(sd_plate(q, wing, wing_z(q), 0.1, 0.02, taper=am._roof(0.8)), V_SAC),
                      (sd_ellipsoid(p, (0, 0.02, 0.07), (0.27, 0.52, 0.13)), V_BODY),
                      (sd_capsule(q, (0.2, 0.5, 0.06), (0.13, 0.76, 0.05), 0.075, 0.045), V_BONE),
                      (sd_ellipsoid(p, (0, 0.53, 0.05), (0.12, 0.05, 0.05)), V_GLOW),
                      (sd_sphere(q, (0.23, 0.44, 0.11), 0.05), V_GLOW),
                      (sd_capsule(p, (0, -0.5, 0.04), (0, -1.12, 0.02), 0.035, 0.012), V_DARK)]
            for k in range(5):
                x = 0.34 + 0.1 * k
                items.append((sd_capsule(q, (x, 0.43 - 0.05 * k, 0.1), (x + 0.05, 0.37 - 0.05 * k,
                                                                         0.1), 0.022), R_GILL))
        else:
            wing = [(0.0, 0.72), (0.3, 0.55), (0.75, 0.2), (1.08, -0.02 - 0.05 * flap), (0.9, -0.22),
                    (0.5, -0.5), (0.18, -0.62), (0.0, -0.62)]
            items += [(sd_plate(q, wing, wing_z(q), 0.12, 0.05, taper=am._roof(0.75)), V_BODY),
                      (sd_ellipsoid(p, (0, 0.05, 0.1), (0.3, 0.6, 0.18)), V_SEAM),
                      (sd_sphere(q, (0.2, 0.42, 0.2), 0.055), V_GLOW),
                      (sd_ellipsoid(p, (0, 0.66, 0.04), (0.12, 0.05, 0.04)), V_GLOW),
                      (sd_capsule(p, (0, -0.55, 0.06), (0, -1.14, 0.02), 0.06, 0.014), V_BONE),
                      (sd_capsule(p, (0, -0.95, 0.06), (0.06, -1.02, 0.08), 0.02, 0.01), V_BONE)]
            for k in range(7):
                y = 0.32 - 0.13 * k
                items.append((sd_sphere(p, (0, y, 0.27 - 0.01 * k), 0.06 - 0.004 * k), V_BONE))
            for k in range(2):
                items.append((sd_sphere(q, (0.5 + 0.2 * k, 0.02 - 0.1 * k, 0.1), 0.05), V_GLOW))
            for k in range(5):
                x = 0.32 + 0.1 * k
                items.append((sd_capsule(q, (x, 0.52 - 0.08 * k, 0.1), (x + 0.05, 0.46 - 0.08 * k,
                                                                         0.1), 0.02), R_GILL))
        return union(*items, k=0.08)
    gill = Material((0.04, 0.04, 0.04), emission=tuple(np.array(LIME_F) * 1.4 * glow))
    return scene, vmats(f"abyss-ray-{v}", glow, "a", [gill])


RAY_N = 208
ray = {v: Lit(lambda flap=0.0, v=v: abyss_ray(v, flap), RAY_N, 16, factor=3, colors=40)
       for v in "ab"}
RAY_C = np.array((250.0, 250.0))
RAY_RISE = (1.5, 2.4)                                  # the shadow grows 0..1.5 s, rises to 2.4 s
RAY_FANS = [2.8, 4.0, 5.2]
RAY_GLIDE = 5.6
RAY_SINK = (8.3, 9.3)
RAY_DUR = 10.0


def ray_flap(t):
    return float(np.round(np.sin(TAU * t / 2.2)))     # -1 / 0 / 1


def ray_state(t):
    """(centre, heading) of the ray."""
    if t < RAY_GLIDE:
        return RAY_C + np.array((0.0, 6 * np.sin(t * 1.3))), DOWN
    u = t - RAY_GLIDE
    turn = smooth(u / 0.9)
    head = DOWN - turn * (DOWN - 0.4)
    return RAY_C + np.array((70 * u * turn, 6 * np.sin(t * 1.3) + 26 * u * turn)), head


def submerged(sp, k, depth):
    """A unit under the play plane: tinted to the water, darker, softened (depth 0..1)."""
    im = lit(sp, k * (1 - 0.65 * depth), 0.25 + 0.55 * depth)
    if depth > 0.05:
        im = im.filter(ImageFilter.GaussianBlur(0.4 + 1.4 * depth))
    return im


def ray_shadow(f, sp, x, y, a, scale, blur):
    sh = sprite.shadow_of(sp, opacity=a, blur=blur, scale=scale, color=(0, 6, 10))
    sprite.paste_center(f, sh, x, y)


def ray_scene(v):
    water = Water("edge", seed=31)
    R = ray[v]
    esc = []
    for k, ang in enumerate((-2.4, -0.75, 0.75, 2.4)):
        hold = RAY_C + np.array((np.cos(ang + DOWN) * 150, np.sin(ang + DOWN) * 110 + 40))
        hold = (float(np.clip(hold[0], 50, FW - 50)), float(hold[1]))
        esc.append(Holder([tuple(RAY_C + (0, 10)), (RAY_C[0] + (hold[0] - RAY_C[0]) * 0.5,
                                                    RAY_C[1] + 20), hold],
                          2.0 + 0.12 * k, 1.0, leave=None, kill=6.4 if k == 2 else None,
                          first=0.9 + 0.3 * k))
    shots = Shots()
    for tf in RAY_FANS:
        c, h = ray_state(tf)
        mx, my = c + np.array((np.cos(h), np.sin(h))) * 60
        px, py = r5.player_at(tf)
        shots.fan(tf, mx, my, np.arctan2(py - my, px - mx), 9, np.radians(110), 110)
    fired = []
    for e in esc:
        st = e.shots(RAY_DUR)
        fired.append(st)
        for ts in st:
            p, _ = e.pos(ts)
            px, py = r5.player_at(ts)
            shots.fire(ts, p[0], p[1], px, py, 150, "needle")
    frames = []
    for t in times(RAY_DUR):
        px, py = r5.player_at(t)
        f = water.frame(t, px, py)
        c, h = ray_state(t)
        sp = R.get(h, flap=ray_flap(t))
        k = max(0.05, water.light(*c))
        if t < RAY_RISE[0]:                                  # a dark shape grows in the depth
            u = smooth(t / RAY_RISE[0])
            ray_shadow(f, sp, c[0], c[1] + 6, 0.25 + 0.6 * u, 0.55 + 0.33 * u, 9 - 6 * u)
        elif t < RAY_RISE[1]:
            u = smooth((t - RAY_RISE[0]) / (RAY_RISE[1] - RAY_RISE[0]))
            ray_shadow(f, sp, c[0] + 10 * u, c[1] + 6 + 14 * u, 0.85 * (1 - u * 0.5), 0.88 + 0.12 * u,
                       3)
            im = submerged(sp, k, 1 - u)
            sc = 0.88 + 0.12 * u
            im = im.resize((int(im.width * sc), int(im.height * sc)), Image.BILINEAR)
            sprite.paste_center(f, fade(im, 0.4 + 0.6 * u), c[0], c[1])
        elif t < RAY_SINK[1]:
            u = smooth((t - RAY_SINK[0]) / (RAY_SINK[1] - RAY_SINK[0])) if t > RAY_SINK[0] else 0
            ray_shadow(f, sp, c[0] + 10 + 6 * u, c[1] + 20, 0.3 * (1 - u), 1.0, 3)
            if u <= 0:
                water.put(f, R, h, c[0], c[1], flap=ray_flap(t))
                for tf in RAY_FANS:
                    if 0 <= t - tf < 0.12:
                        mx, my = c + np.array((np.cos(h), np.sin(h))) * 60
                        f = raster.add_light(f, mx, my, 18, VIOLET, 1.0)
            else:
                im = submerged(sp, k, u)
                sc = 1 - 0.15 * u
                im = im.resize((int(im.width * sc), int(im.height * sc)), Image.BILINEAR)
                sprite.paste_center(f, fade(im, 1 - 0.7 * u), c[0], c[1])
        for i, e in enumerate(esc):
            if t >= e.t0:
                f = draw_barb(f, water, "a", e, t, i, px, py, fired[i])
        shots.draw(f, t)
        ship(f, px, py, t)
        frames.append(f)
    return frames


def ray_sheet(v, frames):
    R = ray[v]
    heads = [R.frame(k) for k in (4, 2, 0, 14)]
    flaps = [R.get(DOWN, flap=fl) for fl in (-1.0, 0.0, 1.0)]
    seq = []
    for t in (0.4, 1.0, 1.5, 1.95, 2.4):
        cell = Image.new("RGBA", (RAY_N, RAY_N), (6, 30, 38, 255))
        sp = R.get(DOWN)
        if t <= RAY_RISE[0]:
            u = smooth(t / RAY_RISE[0])
            sh = sprite.shadow_of(sp, 0.25 + 0.6 * u, 9 - 6 * u, 0.55 + 0.33 * u, (0, 6, 10))
            sprite.paste_center(cell, sh, RAY_N / 2, RAY_N / 2)
        else:
            u = smooth((t - RAY_RISE[0]) / 0.9)
            sh = sprite.shadow_of(sp, 0.85 * (1 - u * 0.5), 3, 0.88 + 0.12 * u, (0, 6, 10))
            sprite.paste_center(cell, sh, RAY_N / 2 + 10 * u, RAY_N / 2 + 6 + 14 * u)
            im = submerged(sp, 0.9, 1 - u)
            sc = 0.88 + 0.12 * u
            im = im.resize((int(im.width * sc), int(im.height * sc)), Image.BILINEAR)
            sprite.paste_center(cell, fade(im, 0.4 + 0.6 * u), RAY_N / 2, RAY_N / 2)
        seq.append(cell.resize((RAY_N // 2, RAY_N // 2), Image.BILINEAR))
    seqimg = Image.new("RGBA", (5 * (RAY_N // 2 + 6), RAY_N // 2 + 12), raster.SHEET_BG)
    for i, (c, t) in enumerate(zip(seq, (0.4, 1.0, 1.5, 1.95, 2.4))):
        seqimg.alpha_composite(c, (i * (RAY_N // 2 + 6), 0))
        raster.draw_text(seqimg, i * (RAY_N // 2 + 6), RAY_N // 2 + 2, f"{t:.2f} S", raster.LABEL_DIM)
    dark = lit(R.get(DOWN), 0.06, 0.2, R.glow_get(DOWN))
    size = Image.new("RGBA", (RAY_N, RAY_N), (0, 0, 0, 0))
    sprite.paste_center(size, R.get(DOWN), RAY_N / 2, RAY_N / 2)
    sprite.paste_center(size, barb["a"].get(DOWN), 30, 30)
    sprite.paste_center(size, e3.player_sprite(), RAY_N - 30, RAY_N - 30)
    panels = [("SOURCE, 260 PX", source_panel(lambda: abyss_ray(v))),
              ("4 OF 16 HEADINGS (0.5X)", grid([h_.resize((104, 104), Image.LANCZOS) for h_ in heads],
                                                4, 1)),
              ("WING FLAP -1 / 0 / +1 (0.5X)",
               grid([f_.resize((104, 104), Image.LANCZOS) for f_ in flaps], 3, 1)),
              ("THE RISE: SHADOW GROWS, THEN THE RAY SURFACES ONTO THE PLAY PLANE (0.5X)", seqimg),
              ("1X WITH A BARBFIN AND THE PLAYER", grid([size], 1, 1)),
              ("IN THE DARK (L26): GILLS ONLY (0.5X)",
               on_bg([dark.resize((104, 104), Image.LANCZOS)], 104, (2, 10, 14), 1))]
    if v == "a":
        notes = ["ABYSS RAY A - MANTA (TEAL-BLACK CHITIN, TEAL WING VEINS, VIOLET MOUTH, LIME GILLS)",
                 "200X150 PX (208 PX CANVAS), 16 HEADINGS X 3 FLAP FRAMES. POINTED MANTA WINGS WITH",
                 "GLOWING TEAL VEINS, CURLED BONE CEPHALIC LOBES, A VIOLET MOUTH (THE FAN), FIVE LIME",
                 "GILL SLITS ON EACH LEADING EDGE (THE WEAK POINT, DRAWN ONLY), A THIN TAIL.",
                 "RISE: A DARK SHAPE GROWS 1.5 S UNDER THE SHIP'S LANE (ANTI-SUB ONLY), THE RAY RISES",
                 "0.9 S (TINTED, SOFT -> CRISP; ON THE PLAY PLANE AT THE MIDPOINT), FOUR BARBFINS PEEL",
                 "OFF, THREE 9-WAY FANS OVER 110 DEG (X 0.7), THEN IT GLIDES OFF AND SINKS AWAY.",
                 "UNDER-WATER GRADE: L24, THE PLAIN AT THE LAMPS' EDGE (THE WATER DARKENS AHEAD)."]
    else:
        notes = ["ABYSS RAY B - BONE SKATE (BONE DISC, VIOLET SEAMS AND SPOTS, LIME GILLS)",
                 "200X150 PX (208 PX CANVAS), 16 HEADINGS X 3 FLAP FRAMES. A ROUNDED DIAMOND DISC IN",
                 "BONE PLATES, A RIDGE OF BONE TUBERCLES DOWN THE BACK, VIOLET EYE SPOTS ON THE WINGS",
                 "AND A VIOLET MOUTH, FIVE LIME GILL SLITS ON EACH LEADING EDGE, A LONG WHIP TAIL WITH",
                 "A SPINE. PALER: READS STRONGER ON THE DARK WATER THAN A, LESS ALIEN-ELEGANT.",
                 "SAME RISE, ESCORTS, THREE FANS, GLIDE AND SINK AS A."]
    return sheet(f"ABYSS RAY {v.upper()} - RISES FROM A SHADOW (LEVEL 24)", panels, notes,
                 frames[int(4.05 * FPS)])


def make_ray(variants):
    for v in variants:
        frames = ray_scene(v)
        save_unit("naval", "abyss-ray", v, ray_sheet(v, frames), frames)


# ===================================================================== 4 Siren (L25)

S_SAC = 7                                              # extra slot: the throat sac (tell-driven)


def siren(v, tell=0.0, glow=1.0):
    """A trench-wall nest (faces +Y, the open water); its teal throat sac brightens for the
    0.8 s tell. a: conch throat - a ribbed spiral shell lying in the cave mouth, fleshy lips round
    a swelling sac; b: frill bell - a slate dome with five membrane frills fanned round the sac,
    spreading on the tell."""
    def scene(p):
        q = mirror_x(p)
        sac_r = 0.26 + 0.07 * tell
        if v == "a":
            items = [(sd_capsule(p, (0.08, -0.85, 0.02), (0, 0.0, 0.08), 0.16, 0.5), V_BODY)]
            for k in range(7):                               # spiral knobs round the spire
                a = k * 2.2
                y = -0.7 + 0.11 * k
                rr = 0.18 + 0.045 * k
                items.append((sd_sphere(p, (np.cos(a) * rr, y, 0.08 + np.sin(a) * rr * 0.6 + 0.12),
                                        0.07 + 0.008 * k), V_BONE))
            items += [(sd_ellipsoid(p, (0, 0.3, 0.06), (0.5, 0.34, 0.22)), V_DARK),
                      (sd_torus_z(p, (0, 0.32, 0.12), 0.38, 0.09), V_SEAM),
                      (sd_sphere(p, (0, 0.32, 0.1 + 0.05 * tell), sac_r), S_SAC)]
            for k in range(5):                               # lip spines
                a = np.radians(-60 + 30 * k)
                items.append((sd_capsule(p, (np.sin(a) * 0.4, 0.32 + np.cos(a) * 0.3, 0.14),
                                         (np.sin(a) * 0.64, 0.34 + np.cos(a) * 0.52, 0.06),
                                         0.045, 0.015), V_BONE))
        else:
            items = [(sd_ellipsoid(p, (0, -0.35, 0.02), (0.6, 0.45, 0.3)), V_BODY),
                     (sd_sphere(p, (0, 0.18, 0.12 + 0.05 * tell), sac_r + 0.04), S_SAC)]
            spread = 1.0 + 0.25 * tell
            for k in range(5):
                a = np.radians((-72 + 36 * k) * spread)
                tip = (np.sin(a) * 0.98, 0.15 + np.cos(a) * 0.8)
                frill = [(np.sin(a) * 0.2, 0.15 + np.cos(a) * 0.2)] + \
                    [(np.sin(a + da) * 0.86, 0.15 + np.cos(a + da) * 0.74)
                     for da in np.linspace(-0.27, 0.27, 5)]
                items.append((sd_plate(p, frill, 0.06, 0.03), V_SAC))
                items.append((sd_capsule(p, (np.sin(a) * 0.25, 0.15 + np.cos(a) * 0.25, 0.1),
                                         (tip[0], tip[1], 0.08), 0.04, 0.02), V_BONE))
            items.append((sd_torus_z(p, (0, 0.18, 0.14), 0.32, 0.06), V_DARK))
        return union(*items, k=0.04)
    sac = Material((0.05, 0.1, 0.08), emission=tuple(np.array(TEAL_F) * (0.35 + 1.25 * tell) * glow))
    return scene, e36.lift(vmats(f"siren-{v}", glow, "a", [sac]), 0.2)


SIR_N = 64
sirens = {v: Lit(lambda tell=0.0, v=v: siren(v, tell), SIR_N, 2, factor=5, colors=32) for v in "ab"}
SIR_RING = (120.0, 230.0, 10.0)                        # px/s, max radius, band width
SIR_TELL = 0.8
SIR_SLOW, SIR_IMMUNE = 2.0, 3.0
SIR_DUR = 10.0


def _nest(y, side, fires, kill=None):
    x = wall_l(y) - 6 if side < 0 else wall_r(y) + 6
    return dict(x=x, y=y, h=0.0 if side < 0 else np.pi, fires=fires, kill=kill,
                mouth=(x + (22 if side < 0 else -22), y))


NESTS = [_nest(150, -1, [0.9, 5.4]), _nest(300, 1, [2.4, 6.9], kill=7.7),
         _nest(410, -1, [3.9, 8.4])]


def draw_ring(f, v, x, y, r, alpha, t):
    lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    w = SIR_RING[2]
    if v == "a":                                          # a smooth sound band, bright front
        for k in range(5):
            rr = r - k * w / 4
            if rr > 2:
                a = int(alpha * (230 if k == 0 else 110 - 18 * k))
                d.ellipse([x - rr, y - rr, x + rr, y + rr], outline=(170, 255, 220, a),
                          width=2 if k == 0 else 1)
    else:                                                 # a rippled double wavefront
        th = np.linspace(0, TAU, 180)
        for k, off in enumerate((0.0, -w * 0.8)):
            rr = r + off + 3 * np.sin(th * 9 + t * 8 + k)
            pts = list(zip(x + rr * np.cos(th), y + rr * np.sin(th)))
            d.line(pts + [pts[0]], fill=(150, 255, 210, int(alpha * (220 if k == 0 else 120))),
                   width=2)
    f.alpha_composite(lay.filter(ImageFilter.GaussianBlur(2.0)))
    f.alpha_composite(lay)


def shimmer(f, px, py, t, u):
    """The ship's slow tell: a teal sonar shimmer round the hull (rippling outline)."""
    lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for k in range(3):
        rr = 14 + ((t * 30 + k * 7) % 21)
        a = int(200 * u * (1 - (rr - 14) / 21))
        d.ellipse([px - rr, py - rr * 0.9, px + rr, py + rr * 0.9], outline=(140, 255, 210, a))
    f.alpha_composite(lay)


def siren_scene(v):
    beacons = [(wall_l(230) - 16, 230, 0.0), (wall_r(470) + 16, 470, 0.45),
               (wall_r(90) + 14, 90, 0.7)]
    water = Water("trench", seed=25, walls=True, beacons=beacons)
    S = sirens[v]
    frames = []
    phase, slow_until, immune_until = 0.0, -1.0, -1.0
    hits = set()
    events = []
    for t in times(SIR_DUR):
        dt = 1.0 / FPS
        sp = 0.6 if t < slow_until else 1.0
        phase += dt * 1.1 * sp
        px = 240 + 72 * np.sin(phase)
        py = 455 + 14 * np.sin(phase * 0.6)
        f = water.frame(t, px, py)
        for n in NESTS:                                    # cave mouths
            lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
            ImageDraw.Draw(lay).ellipse([n["x"] - 26, n["y"] - 24, n["x"] + 26, n["y"] + 24],
                                        fill=(2, 8, 10, 200))
            f.alpha_composite(lay.filter(ImageFilter.GaussianBlur(3)))
        for i, n in enumerate(NESTS):
            if n["kill"] is not None and t >= n["kill"]:
                s_ = (t - n["kill"]) / 0.9
                if s_ <= 1:
                    f = uw_burst(f, *n["mouth"], s_, 1.2, (160, 255, 220))
                    plume = Image.new("RGBA", f.size, (0, 0, 0, 0))
                    r = 8 + 26 * s_
                    ImageDraw.Draw(plume).ellipse([n["mouth"][0] - r, n["mouth"][1] - r - 20 * s_,
                                                   n["mouth"][0] + r, n["mouth"][1] + r - 20 * s_],
                                                  fill=(190, 220, 215, int(140 * (1 - s_))))
                    f.alpha_composite(plume.filter(ImageFilter.GaussianBlur(4)))
                continue
            tell = 0.0
            for tf in n["fires"]:
                if tf - SIR_TELL <= t < tf:
                    tell = 1.0 if int(t * 10) % 2 else 0.5
                elif tf <= t < tf + 0.15:
                    tell = 1.0
            hit = n["kill"] is not None and n["kill"] - 0.6 < t < n["kill"] and int(t * 14) % 2
            water.put(f, S, n["h"], n["x"], n["y"], hit, tell=tell)
            if tell:
                f = raster.add_light(f, *n["mouth"], 14, TEAL, 0.5 * tell)
        for i, n in enumerate(NESTS):
            for tf in n["fires"]:
                age = t - tf
                if n["kill"] is not None and tf >= n["kill"]:
                    continue
                r = age * SIR_RING[0]
                if 0 <= age and r <= SIR_RING[1]:
                    alpha = 1.0 - max(0.0, (r - SIR_RING[1] * 0.75) / (SIR_RING[1] * 0.25))
                    draw_ring(f, v, *n["mouth"], r, alpha, t)
                    d_ = np.hypot(px - n["mouth"][0], py - n["mouth"][1])
                    if abs(d_ - r) < SIR_RING[2] / 2 + 8 and (i, tf) not in hits:
                        hits.add((i, tf))
                        if t >= immune_until:
                            slow_until = t + SIR_SLOW
                            immune_until = slow_until + SIR_IMMUNE
                            events.append(("slow", t))
                        else:
                            events.append(("immune", t))
        ship(f, px, py, t)
        if t < slow_until:
            shimmer(f, px, py, t, 1.0)
            label(f, px + 22, py - 6, "SLOWED 60%", (150, 255, 210))
        elif t < immune_until:
            label(f, px + 22, py - 6, "IMMUNE", (150, 170, 180))
        caption(f, "VARGA: SIRENS. THE RINGS WON'T HURT YOU - THEY'LL PIN YOU.", t, 0.3, 3.0)
        frames.append(f)
    print("siren events:", events)
    return frames


def siren_sheet(v, frames):
    S = sirens[v]
    st = [S.get(0.0, tell=0.0), S.get(0.0, tell=0.5), S.get(0.0, tell=1.0), S.get(np.pi, tell=0.0)]
    dark = [lit(S.get(0.0, tell=k), 0.06, 0.2, S.glow_get(0.0, tell=k)) for k in (0.0, 1.0)]
    ringc = Image.new("RGBA", (300, 170), (6, 22, 28, 255))
    for k, r in enumerate((30, 60, 90)):
        draw_ring(ringc, v, 70 + k * 80 - r * 0 , 85, r * 0.75, 1.0, 0.3 * k)
    rp = Image.new("RGBA", (300, 170), (6, 22, 28, 255))
    draw_ring(rp, v, 40, 85, 120, 1.0, 0.0)
    sprite.paste_center(rp, sirens[v].get(0.0, tell=1.0), 20, 85)
    shim = Image.new("RGBA", (96, 96), (6, 22, 28, 255))
    sprite.paste_center(shim, e3.player_sprite(), 48, 48)
    shimmer(shim, 48, 48, 0.2, 1.0)
    panels = [("SOURCE, 260 PX (TELL LIT)", source_panel(lambda: siren(v, 1.0))),
              ("LEFT WALL: TELL 0 / 0.5 / 1; RIGHT WALL (2X)",
               thumbs(st, SIR_N, 2, ["IDLE", "TELL", "SING", "RIGHT"])),
              ("IN THE DARK (L26): GLOW ONLY (2X)",
               on_bg(dark, SIR_N, (2, 10, 14), 2, ["IDLE", "TELL"])),
              ("THE RING (1X): A BAND 10 PX WIDE, 120 PX/S TO 230 PX", rp),
              ("THE SHIP SLOWED: SONAR SHIMMER (1X)", shim)]
    if v == "a":
        notes = ["SIREN A - CONCH THROAT (SLATE SHELL, BONE RIBS AND LIP SPINES, TEAL THROAT SAC)",
                 "64X56 PX, FIXED (LEFT- AND RIGHT-WALL FACINGS), 3 TELL FRAMES. A RIBBED SPIRAL SHELL",
                 "LYING IN A CAVE MOUTH OF THE TRENCH WALL, ITS OPENING TO THE WATER: FLESHY LIPS AND",
                 "BONE SPINES ROUND A TEAL THROAT SAC THAT SWELLS AND FLICKERS FOR THE 0.8 S TELL.",
                 "RING (A): A SMOOTH SOUND BAND WITH A BRIGHT LEADING EDGE, NO DAMAGE; A SHIP IT PASSES",
                 "THROUGH IS SLOWED TO 60 % FOR 2 S (TEAL SHIMMER ROUND THE HULL), THEN IMMUNE FOR 3 S",
                 "(Q5 A). GIF: THREE NESTS, RINGS 1.5 S APART; ONE NEST SHOT (A PALE PLUME). L25:",
                 "THE DARK TRENCH, ROCK WALLS, RED-GOLD HUMAN RELAY BEACONS, THE HEADLIGHT CONE."]
    else:
        notes = ["SIREN B - FRILL BELL (SLATE DOME, FIVE MEMBRANE FRILLS ON BONE RAYS, TEAL SAC)",
                 "64X56 PX, FIXED, 3 TELL FRAMES. A SLATE DOME IN THE CAVE MOUTH WITH FIVE GLOWING",
                 "MEMBRANE FRILLS FANNED ROUND A TEAL SAC; ON THE TELL THE FRILLS SPREAD AND THE SAC",
                 "SWELLS. RING (B): A RIPPLED DOUBLE WAVEFRONT (READS MORE AS SOUND, LESS AS A",
                 "BULLET RING THAN A). SAME SLOW, SHIMMER AND IMMUNITY; SAME SCENE."]
    return sheet(f"SIREN {v.upper()} - TRENCH NEST, SONAR RING SLOWS (LEVEL 25)", panels, notes,
                 frames[int(4.3 * FPS)])


def make_siren(variants):
    for v in variants:
        frames = siren_scene(v)
        save_unit("naval", "siren", v, siren_sheet(v, frames), frames)


# ===================================================================== 5 Glow Angler (L26)

G_LURE = 7                                             # extra slot: the teal lure bead
LURE_TIP = {"a": (0.0, 1.0), "b": (0.0, 1.06)}        # model x/y of the bead


def angler(v, jaw=0.0, pulse=1.0, glow=1.0):
    """A small Vrell ambusher (faces +Y) with a teal lure bead on a bone stalk arching over its
    mouth (Q6 a: a near-miss pickup look, never a pickup's colour, the stalk visible when lit).
    a: rust angler - a bulbous body, a wide toothed gape, crimson eyes, fan tail;
    b: teal-black gulper - a slender body behind a huge pouch mouth, a longer stalk, whip tail."""
    def scene(p):
        q = mirror_x(p)
        bx, by = LURE_TIP[v]
        if v == "a":
            items = [(sd_ellipsoid(p, (0, -0.22, 0.08), (0.46, 0.6, 0.38)), V_BODY),
                     (sd_ellipsoid(p, (0, 0.28, 0.08 - 0.04 * jaw), (0.4, 0.2 + 0.1 * jaw, 0.2)),
                      V_DARK),
                     (sd_sphere(q, (0.24, 0.12, 0.34), 0.06), V_GLOW),
                     (sd_plate(q, [(0.38, -0.1), (0.7, -0.25), (0.62, -0.45), (0.36, -0.36)], 0.0,
                               0.03), V_BONE),
                     (sd_plate(q, [(0.0, -0.75), (0.34, -1.08), (0.1, -1.12), (0.0, -1.0)], 0.0,
                               0.03), V_BONE)]
            for k in range(6):
                a = np.radians(-70 + 28 * k)
                items.append((sd_capsule(p, (np.sin(a) * 0.36, 0.28 + np.cos(a) * 0.16, 0.14),
                                         (np.sin(a) * 0.3, 0.3 + np.cos(a) * 0.08, 0.1), 0.03,
                                         0.008), V_BONE))
            stalk = [(0, 0.05, 0.42), (0, 0.55, 0.62), (bx, by - 0.05, 0.42)]
        else:
            items = [(sd_capsule(p, (0, -0.2, 0.06), (0, -1.05, 0.02), 0.22, 0.05), V_BODY),
                     (sd_ellipsoid(p, (0, 0.2, 0.06), (0.52, 0.42, 0.16 + 0.06 * jaw)), V_SEAM),
                     (sd_ellipsoid(p, (0, 0.34, 0.12), (0.4, 0.22 + 0.08 * jaw, 0.08)), V_DARK),
                     (sd_sphere(q, (0.18, -0.12, 0.24), 0.055), V_GLOW),
                     (sd_plate(q, [(0.15, -0.3), (0.4, -0.5), (0.2, -0.55)], 0.0, 0.025), V_BONE)]
            stalk = [(0, -0.1, 0.3), (0, 0.5, 0.6), (bx, by - 0.05, 0.42)]
        items.append((sd_capsule(p, stalk[0], stalk[1], 0.035, 0.03), V_BONE))
        items.append((sd_capsule(p, stalk[1], stalk[2], 0.03, 0.022), V_BONE))
        items.append((sd_sphere(p, (bx, by, 0.42), 0.17), G_LURE))
        return union(*items, k=0.035)
    lure = Material((0.1, 0.2, 0.16), emission=tuple(np.array(TEAL_F) * (0.9 + 0.8 * pulse) * glow))
    return scene, vmats(f"glow-angler-{v}", glow, "a", [lure])


ANG_N = 64
anglers = {v: Lit(lambda jaw=0.0, pulse=1.0, v=v: angler(v, jaw, pulse), ANG_N, 16, factor=5)
           for v in "ab"}
ANG_PATH = rig.catmull_rom([(240, 488), (250, 440), (300, 384), (280, 405), (210, 420),
                            (170, 362), (190, 318)], [0, 2.0, 3.5, 5.0, 6.5, 8.0, 10.0])
PICKUP = [Image.open(ROOT / "assets" / "sprites" / f"pickup-salvage-small_{k}.png").convert("RGBA")
          for k in range(8)]


def lure_xy(v, x, y, heading):
    lx, ly = rig.local_to_screen(*LURE_TIP[v], heading, ANG_N / 2.3)
    return x + lx, y + ly


class Angler:
    def __init__(self, x, y, drift, kill=None, lunges=True):
        self.p0 = np.array((x, y), dtype=np.float64)
        self.drift, self.kill, self.lunges = drift, kill, lunges
        self.state, self.ts, self.pos, self.h = "hang", 0.0, self.p0.copy(), DOWN
        self.target = None

    def step(self, t, ship_xy, shots):
        sx, sy = ship_xy
        if self.state == "hang":
            self.pos = self.p0 + np.array((4 * np.sin(t * 0.8), self.drift * t))
            self.h = DOWN + 0.15 * np.sin(t * 0.6)
            if self.lunges and np.hypot(sx - self.pos[0], sy - self.pos[1]) < 130:
                self.state, self.ts = "lunge", t
                d = np.array((sx, sy)) - self.pos
                self.target = self.pos + d / np.linalg.norm(d) * max(0, np.linalg.norm(d) - 60)
                self.start = self.pos.copy()
                self.h = float(np.arctan2(d[1], d[0]))
        elif self.state == "lunge":
            u = min(1.0, (t - self.ts) / 0.6)
            self.pos = self.start + (self.target - self.start) * smooth(u)
            if u >= 1.0:
                self.state, self.ts = "retreat", t
                shots.ring(t, self.pos[0], self.pos[1], 8, 100, phase=0.2)
                self.fired = t
        elif self.state == "retreat":
            self.pos = self.pos - np.array((np.cos(self.h), np.sin(self.h))) * 100 / FPS
        return self.pos


def angler_scene(v):
    water = Water("black", seed=26)
    A = anglers[v]
    pack = [Angler(318, 190, 22, kill=6.3), Angler(120, 40, 24, kill=7.7, lunges=False),
            Angler(410, -30, 24, lunges=False), Angler(60, 250, 18, lunges=False)]
    shots = Shots()
    pk = np.array((182.0, 118.0))
    got = None
    frames = []
    for t in times(10.0):
        px, py = ANG_PATH(t)
        f = water.frame(t, px, py)
        pulse = 1.0 if int(t * 3) % 2 else 0.6
        if got is None:                                     # a real salvage pickup, for contrast
            pp = pk + np.array((0, 30 * t))
            if np.hypot(pp[0] - px, pp[1] - py) < 20:
                got = t
            else:
                k = water.light(*pp)
                sp = PICKUP[int(t * 10) % 8]
                f = raster.add_light(f, pp[0], pp[1], 11, (60, 200, 255), 0.55)
                sprite.paste_center(f, lit(sp, max(0.75, k)), pp[0], pp[1])
        elif t - got < 0.5:
            f = raster.add_light(f, px, py - 10, 16, (90, 220, 255), 1 - (t - got) / 0.5)
            label(f, px + 18, py - 26, "+SALVAGE", (120, 220, 255))
        for i, a in enumerate(pack):
            pos = a.step(t, (px, py), shots)
            if a.kill is not None and t >= a.kill:
                s_ = (t - a.kill) / 0.6
                if s_ <= 1:
                    f = uw_burst(f, *pos, s_, 0.8, (255, 120, 90))
                if t - a.kill < 1.0:                         # the stalk stays lit 1 s
                    lx, ly = lure_xy(v, *pos, a.h)
                    d = ImageDraw.Draw(f)
                    d.line([pos[0], pos[1], lx, ly], fill=(40, 160, 110, int(200 * (1 - (t - a.kill)))))
                continue
            jaw = 1.0 if a.state == "lunge" or (a.state == "retreat" and t - a.ts < 0.2) else 0.0
            hit = a.kill is not None and a.kill - 0.4 < t < a.kill and int(t * 14) % 2
            water.put(f, A, a.h, pos[0], pos[1], hit, jaw=jaw, pulse=pulse)
            lx, ly = lure_xy(v, *pos, a.h)
            f = raster.add_light(f, lx, ly, 9, TEAL, 0.55 * pulse)
        shots.draw(f, t)
        ship(f, px, py, t)
        caption(f, "VARGA: CAREFUL - THAT IS NOT A PICKUP.", t, 0.6, 3.6)
        frames.append(f)
    return frames


def angler_sheet(v, frames):
    A = anglers[v]
    heads = [A.frame(k) for k in range(0, 16, 2)]
    st = [A.get(DOWN), A.get(DOWN, jaw=1.0), A.get(DOWN, pulse=0.6)]
    dark = [lit(A.get(DOWN), 0.06, 0.2, A.glow_get(DOWN)),
            lit(A.get(DOWN), 0.45, 0.2, A.glow_get(DOWN))]
    pick = Image.new("RGBA", (ANG_N, ANG_N), (0, 0, 0, 0))
    pick = raster.add_light(pick, 32, 32, 11, (60, 200, 255), 0.55)
    sprite.paste_center(pick, PICKUP[0], 32, 32)
    lure = Image.new("RGBA", (ANG_N, ANG_N), (0, 0, 0, 0))
    sprite.paste_center(lure, A.glow_get(DOWN), 32, 16)
    cmp_ = on_bg([pick, dark[0], dark[1]], ANG_N, (2, 8, 12), 2,
                 ["REAL PICKUP", "LURE (DARK)", "IN THE CONE"])
    panels = [("SOURCE, 260 PX", source_panel(lambda: angler(v))),
              ("8 OF 16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("IDLE / LUNGE (JAW) / LURE DIM (2X)", thumbs(st, ANG_N, 2, ["IDLE", "LUNGE", "PULSE"])),
              ("A SALVAGE PICKUP VS THE LURE IN THE BLACK (2X)", cmp_),
              ("NEXT TO THE PLAYER (2X)", thumbs([A.get(DOWN), player_cell(64)], ANG_N, 2))]
    if v == "a":
        notes = ["GLOW ANGLER A - RUST ANGLER (RUST CHITIN, CRIMSON EYES, TEAL LURE)",
                 "40X32 PX BODY + 36 PX STALK + 12 PX LURE (64 PX CANVAS), 16 HEADINGS. A BULBOUS BODY,",
                 "A WIDE GAPE RINGED WITH BONE TEETH, A FAN TAIL; A BONE STALK ARCHES OVER THE MOUTH TO A",
                 "TEAL BEAD (Q6 A: LIKE A PICKUP AT A GLANCE BUT VRELL TEAL, NOT A PICKUP'S CYAN, AND",
                 "ON A STALK). IN THE BLACK ONLY THE LURE AND THE EYES SHOW (Q4 A); IN THE HEADLIGHT",
                 "THE BODY APPEARS. WITHIN 130 PX IT LUNGES 0.6 S, STOPS 60 PX SHORT, FIRES A RING OF 8",
                 "(X 0.7) AND BACKS OFF. DEATH BLOWS THE LURE OUT; THE STALK STAYS LIT 1 S.",
                 "L26 BLACKWATER: HEADLIGHT ONLY. A REAL SALVAGE PICKUP DRIFTS PAST FOR COMPARISON."]
    else:
        notes = ["GLOW ANGLER B - TEAL-BLACK GULPER (TEAL-BLACK CHITIN, CRIMSON EYE, TEAL LURE)",
                 "40X32 PX BODY + STALK + LURE (64 PX CANVAS), 16 HEADINGS. A SLENDER BODY BEHIND A",
                 "HUGE POUCH MOUTH WITH GLOWING SEAMS, A WHIP TAIL, THE LURE ON A LONGER STALK. NEARLY",
                 "INVISIBLE IN THE BLACK UNTIL LIT: MORE OF A SHOCK THAN A, HARDER TO READ.",
                 "SAME HANG, LUNGE, RING OF 8 AND DEATH; SAME SCENE."]
    return sheet(f"GLOW ANGLER {v.upper()} - LURE IN THE DARK (LEVEL 26)", panels, notes,
                 frames[int(3.9 * FPS)])


def make_angler(variants):
    for v in variants:
        frames = angler_scene(v)
        save_unit("naval", "glow-angler", v, angler_sheet(v, frames), frames)


# ===================================================================== 6 Depth Hunter (L27)

def depth_hunter(v, glow=1.0):
    """An unmarked grey sub drone (faces +Y), plain grey with no gold or marks (Q11 a); only the
    red sensor and the thruster glow match Ascendancy hardware. a: pump-jet cigar - a long round
    hull, a ducted shroud at the stern, cruciform fins, two torpedo tubes on the bow flanks;
    b: flat glider - a broad flat lifting body, twin dorsal tubes, a sensor slit, two nozzles."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            shroud = subtract((sd_cylinder_y(p, (0, -0.88, 0.04), 0.3, 0.13), A_FACET),
                              sd_cylinder_y(p, (0, -0.88, 0.04), 0.22, 0.2))
            return union(
                (sd_capsule(p, (0, 0.82, 0.04), (0, -0.72, 0.04), 0.27, 0.2), A_HULL),
                shroud,
                (sd_cylinder_y(p, (0, -0.86, 0.04), 0.12, 0.08), A_ENGINE),
                (sd_plate(q, [(0.1, -0.45), (0.5, -0.72), (0.48, -0.86), (0.1, -0.78)], 0.04, 0.025),
                 A_FACET),
                (sd_capsule(q, (0.27, 0.72, 0.0), (0.3, 0.12, 0.0), 0.07, 0.07), A_GUN),
                (sd_box(p, (0, 0.3, 0.27), (0.08, 0.3, 0.06), 0.03), A_GLASS),
                (sd_sphere(p, (0, 0.84, 0.27), 0.085), A_RED),
            )
        body = [(0.0, 1.0), (0.28, 0.72), (0.62, -0.2), (0.6, -0.62), (0.3, -0.78), (0.0, -0.72)]
        return union(
            (sd_plate(q, body, 0.0, 0.14, 0.04, taper=am._roof(0.9)), A_HULL),
            (sd_capsule(q, (0.17, 0.62, 0.17), (0.17, -0.22, 0.17), 0.075, 0.075), A_GUN),
            (sd_ellipsoid(p, (0, 0.1, 0.14), (0.12, 0.55, 0.1)), A_FACET),
            (sd_box(p, (0, 0.74, 0.16), (0.15, 0.035, 0.04)), A_RED),
            (sd_cylinder_y(q, (0.3, -0.76, 0.04), 0.08, 0.08), A_ENGINE),
            k=0.03)
    return scene, e36.unmarked_mats(glow, hull_pattern=panel_lines(6.0, 0.05, 0.78))


DH_N = 76
hunters = {v: Lit(lambda v=v: depth_hunter(v), DH_N, 16, factor=5, colors=28, post=e36.grey_rim)
           for v in "ab"}
MISSILE = [Image.open(ROOT / "assets" / "sprites" / f"revenant-walker-missile_{k}.png").convert("RGBA")
           for k in range(16)]
DH_VENTS = [(100, 150, 20), (398, 255, 24), (300, 470, 18)]
DH_HOLD = [(130, 432), (245, 398), (352, 446)]
DH_FIRE = [[3.0, 6.5], [4.2, 7.7], [5.4, 8.9]]
DH_KILL = {2: 8.1}
TORP_KILL = {(1, 4.2): 6.4}
DH_DUR = 10.5


def missile_sprite(h):
    k = int(round(((h - DOWN) % TAU) / (np.pi / 8))) % 16
    return MISSILE[k]


def dh_ship(t):
    if t < 0.6:
        return 240.0, 488.0
    u = smooth((t - 0.6) / 2.0)
    y = 488 - 270 * u + 14 * np.sin(t * 1.1) * u
    x = 240 + 90 * np.sin((t - 0.6) * 0.75) * u
    return x, y


def hunter_pos(i, t):
    hx, hy = DH_HOLD[i]
    t0 = 0.4 + 0.5 * i
    y = max(hy, FH + 50 - 80 * (t - t0)) if t >= t0 else FH + 60
    return hx + 12 * np.sin((t - t0) * 0.5), y


def torpedoes():
    """Homing torpedoes: launched up along the hunter's facing, 90 px/s x 0.7, 70 deg/s, 5 s."""
    out = []
    for i, fires in enumerate(DH_FIRE):
        for tf in fires:
            if i in DH_KILL and tf >= DH_KILL[i]:
                continue
            x, y = hunter_pos(i, tf)
            pos, h, tr = np.array((x, y - 30.0)), UP, []
            path = []
            kill = TORP_KILL.get((i, tf))
            for k in range(int(5.0 * FPS)):
                t = tf + k / FPS
                if kill is not None and t >= kill:
                    break
                sx, sy = dh_ship(t)
                want = np.arctan2(sy - pos[1], sx - pos[0])
                dh = (want - h + np.pi) % TAU - np.pi
                h += float(np.clip(dh, -np.radians(70) / FPS, np.radians(70) / FPS))
                pos = pos + np.array((np.cos(h), np.sin(h))) * 90 * WATER_SLOW / FPS
                path.append((t, pos.copy(), h))
            out.append(dict(path=path, kill=kill, end=path[-1][0] if path else tf))
    return out


def dh_scene(v):
    water = Water("vent", seed=27, vents=DH_VENTS)
    H = hunters[v]
    torps = torpedoes()
    frames = []
    for t in times(DH_DUR):
        px, py = dh_ship(t)
        f = water.frame(t, px, py)
        for i in range(3):
            x, y = hunter_pos(i, t)
            if y > FH + 40:
                continue
            if i in DH_KILL and t >= DH_KILL[i]:
                s_ = (t - DH_KILL[i]) / 0.9
                if s_ <= 1:
                    ink = Image.new("RGBA", f.size, (0, 0, 0, 0))
                    r = 10 + 30 * s_
                    ImageDraw.Draw(ink).ellipse([x - r, y - r * 0.8, x + r, y + r * 0.8],
                                                fill=(12, 12, 14, int(190 * (1 - s_))))
                    f.alpha_composite(ink.filter(ImageFilter.GaussianBlur(5)))   # oil bloom
                    f = uw_burst(f, x, y, s_, 1.4, (255, 190, 120))
                continue
            hit = i in DH_KILL and DH_KILL[i] - 0.7 < t < DH_KILL[i] and int(t * 14) % 2
            water.put(f, H, UP, x, y, hit)
            f = raster.add_light(f, x, y + 30, 8, (255, 130, 50), 0.6)     # thruster glow
            for tf in DH_FIRE[i]:
                if 0 <= t - tf < 0.15:
                    f = raster.add_light(f, x, y - 32, 12, (255, 220, 180), 0.9)
        d = ImageDraw.Draw(f)
        for tp in torps:
            pts = [(pt, pos, h) for pt, pos, h in tp["path"] if pt <= t]
            if not pts:
                continue
            if tp["kill"] is not None and t >= tp["kill"]:
                last = pts[-1][1]
                f = uw_burst(f, *last, (t - tp["kill"]) / 0.4, 0.7, (255, 200, 140))
                d = ImageDraw.Draw(f)
                continue
            if t > tp["end"] + 1.0 / FPS:
                last = pts[-1][1]
                f = uw_burst(f, *last, (t - tp["end"]) / 0.35, 0.4, (200, 200, 200), ring=False)
                d = ImageDraw.Draw(f)
                continue
            for pt, pos, h in pts[-30::3]:                         # a pale wake
                a = int(120 * (1 - (t - pt) / 1.5))
                if a > 0:
                    d.point((pos[0] - np.cos(h) * 10, pos[1] - np.sin(h) * 10),
                            fill=(170, 210, 220, a))
            _, pos, h = pts[-1]
            hit = tp["kill"] is not None and tp["kill"] - 0.5 < t and int(t * 14) % 2
            sprite.paste_center(f, flash(missile_sprite(h), hit), pos[0], pos[1])
        ship(f, px, py, t, rear=t > 3.0)
        if t < 2.2:
            edge_warning(f, t, [130, 245, 352])
        frames.append(f)
    return frames


def dh_sheet(v, frames):
    H = hunters[v]
    heads = [H.frame(k) for k in range(16)]
    glow = lit(H.get(UP), 0.06, 0.2, H.glow_get(UP))
    ghost = e36.ghost["a"].get(DOWN)
    panels = [("SOURCE, 260 PX", source_panel(lambda: depth_hunter(v))),
              ("16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("NEXT TO THE GHOST DRONE (ACT 3), THE TORPEDO AND THE PLAYER (2X)",
               thumbs([ghost, H.get(UP), missile_sprite(UP), player_cell(76)], DH_N, 2,
                      ["GHOST DRONE", "HUNTER", "TORPEDO", "PLAYER"])),
              ("IN THE DARK (2X)", on_bg([glow], DH_N, (2, 10, 14), 2))]
    if v == "a":
        notes = ["DEPTH HUNTER A - PUMP-JET CIGAR (UNMARKED PLAIN GREY, NO GOLD, NEUTRAL RIM)",
                 "72X40 PX (76 PX CANVAS), 16 HEADINGS. A LONG ROUND GREY HULL WITH PANEL LINES, A",
                 "DUCTED SHROUD AT THE STERN WITH THE THRUSTER GLOW, CRUCIFORM FINS, TWO TORPEDO TUBES",
                 "ON THE BOW FLANKS, A DARK SPINE WINDOW, ONE RED SENSOR AT THE NOSE. CLEARLY HUMAN,",
                 "OWNED BY NOBODY: THE ACT 3 GHOST DRONE'S LIVERY (Q11 A: NO BLACK AND GOLD BEFORE",
                 "L28). A COLUMN RISES FROM THE BOTTOM EDGE (REAR WARNING), HOLDS LOW AND FIRES SLOW",
                 "HOMING TORPEDOES UP THE SCREEN (90 PX/S X 0.7, 70 DEG/S, 5 S, SHOOTABLE: THE",
                 "REVENANT WALKER'S MISSILE SPRITE WITH A PALE WAKE). L27: THE VENTS' ORANGE GLOW."]
    else:
        notes = ["DEPTH HUNTER B - FLAT GLIDER (UNMARKED PLAIN GREY, NO GOLD, NEUTRAL RIM)",
                 "72X40 PX (76 PX CANVAS), 16 HEADINGS. A BROAD FLAT LIFTING BODY (A MILITARY UUV),",
                 "TWIN DORSAL TORPEDO TUBES, A RED SENSOR SLIT ACROSS THE NOSE, TWO STERN NOZZLES.",
                 "WIDER AND FLATTER THAN A: READS MORE LIKE A WEAPON PLATFORM, LESS LIKE A SUBMARINE.",
                 "SAME COLUMN FROM THE REAR, HOLD AND TORPEDOES AS A."]
    return sheet(f"DEPTH HUNTER {v.upper()} - GREY, UNMARKED, FROM BELOW (LEVEL 27)", panels, notes,
                 frames[int(6.0 * FPS)])


def make_hunter(variants):
    for v in variants:
        frames = dh_scene(v)
        save_unit("naval", "depth-hunter", v, dh_sheet(v, frames), frames)


# ===================================================================== 7 Glassfin (L23, tiny)

def glassfin(v, glint=0.0, glow=1.0):
    """A 16 px school fry of clear Vrell glass (faces +Y) with a teal spine (the contact hue).
    a: needle fry - a thin glass needle, two dark eye dots, a fan tail; b: leaf larva - a flat
    glass leaf with the spine down its middle and a tiny head."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            items = [(sd_capsule(p, (0, 0.82, 0.04), (0, -0.62, 0.02), 0.22, 0.07), V_BODY),
                     (sd_plate(q, [(0.0, -0.5), (0.36, -0.98), (0.12, -1.02), (0.0, -0.84)], 0.0,
                               0.03), V_BODY),
                     (sd_capsule(p, (0, 0.6, 0.2), (0, -0.55, 0.08), 0.07, 0.04), V_GLOW),
                     (sd_sphere(q, (0.15, 0.62, 0.12), 0.08), V_DARK)]
        else:
            leaf = [(0.0, 1.0), (0.3, 0.6), (0.44, 0.0), (0.3, -0.62), (0.0, -1.0)]
            items = [(sd_plate(q, leaf, 0.0, 0.1, 0.03, taper=am._roof(1.6)), V_BODY),
                     (sd_capsule(p, (0, 0.72, 0.1), (0, -0.82, 0.06), 0.06, 0.03), V_GLOW),
                     (sd_sphere(p, (0, 0.88, 0.08), 0.1), V_DARK)]
        return union(*items, k=0.03)
    mats = vmats(f"glassfin-{v}", glow)
    mats[V_BODY] = Material((0.62 + 0.3 * glint, 0.86, 0.88), metal=0.35, shininess=140, spec=1.5,
                            emission=tuple(np.array(TEAL_F) * 0.9 * glint * glow))
    return scene, mats


GF_N = 18
glass = {v: Lit(lambda glint=0.0, v=v: glassfin(v, glint), GF_N, 16, factor=6, colors=16)
         for v in "ab"}
GF_R1 = rig.catmull_rom([(100, -40), (190, 90), (320, 190), (300, 280), (130, 340), (160, 470),
                         (380, 600)], [0, 1.0, 2.0, 2.8, 3.9, 5.0, 6.4])
GF_R2 = rig.catmull_rom([(420, -40), (330, 90), (170, 190), (200, 290), (390, 360), (330, 480),
                         (90, 610)], [0, 1.0, 2.0, 2.8, 3.9, 5.0, 6.4])
GF_SCHOOLS = [(GF_R1, 0.0, 14), (GF_R2, 3.4, 14)]
GF_RAM = {(0, 4): True, (0, 9): False, (1, 6): False}  # (school, fry): reaches the ship?
GF_SPARE = {(0, 1), (0, 12), (1, 2), (1, 11), (1, 13)}
GF_DUR = 10.0


def shatter(img, x, y, s, vel=(0.0, 0.0)):
    """Glass shatter: a grey-teal flash and four slow shards (no bubbles)."""
    if not 0 <= s <= 1:
        return img
    img = raster.add_light(img, x, y, 8, (150, 220, 210), 0.9 * (1 - s))
    d = ImageDraw.Draw(img)
    for k in range(4):
        a = k * TAU / 4 + 0.6
        r = 3 + 14 * s
        cx = x + np.cos(a) * r + vel[0] * s * 0.3
        cy = y + np.sin(a) * r + vel[1] * s * 0.3
        d.line([cx, cy, cx + np.cos(a + 1.2) * 3, cy + np.sin(a + 1.2) * 3],
               fill=(200, 240, 240, int(255 * (1 - s))), width=1)
    return img


def fry_pos(school, i, t):
    path, t0, n = GF_SCHOOLS[school]
    u = t - t0 - 0.13 * i
    x, y = path(u)
    h = rig.path_heading(path, u)
    sw = 14 * np.sin(TAU * 1.4 * u + i * 0.5)
    return x - np.sin(h) * sw * 0.6, y + np.cos(h) * sw * 0.6, h, u


def glass_scene(v):
    water = Water("lamp", base=farm_floor())
    G = glass[v]
    dead, ram = {}, {}
    frames = []
    for t in times(GF_DUR):
        px, py = r5.player_at(t)
        f = water.frame(t, px, py)
        for sc, (path, t0, n) in enumerate(GF_SCHOOLS):
            for i in range(n):
                key = (sc, i)
                x, y, h, u = fry_pos(sc, i, t)
                if u < 0 or u > 6.4:
                    continue
                if key in dead:
                    td, dx_, dy_, vel = dead[key]
                    f = shatter(f, dx_, dy_, (t - td) / 0.5, vel)
                    continue
                glint = 0.0
                if key in GF_RAM:
                    tb = t0 + 0.13 * i + 3.0                    # breaks off after 3 s on screen
                    if t >= tb:
                        if key not in ram:
                            ram[key] = (np.array((x, y)), np.array((px, py)))
                        p0, aim = ram[key]
                        if t < tb + 0.3:                         # 0.3 s glint tell
                            x, y = p0
                            glint = 1.0 if int(t * 20) % 2 else 0.5
                        else:
                            if not GF_RAM[key]:
                                aim = aim + np.array((60.0, 0.0))
                            d = aim - p0
                            d = d / np.linalg.norm(d)
                            pos = p0 + d * 230 * (t - tb - 0.3)
                            x, y = pos
                            h = float(np.arctan2(d[1], d[0]))
                            if np.hypot(x - px, y - py) < 14:
                                dead[key] = (t, x, y, (0.0, 0.0))
                                f = raster.add_light(f, px, py, 26, (120, 200, 255), 0.9)
                                continue
                            if y > FH + 20:
                                continue
                if i % 3 == 1 and key not in GF_SPARE and key not in ram and abs(x - px) < 12 and \
                        60 < y < py - 40 and u > 0.6:
                    dead[key] = (t, x, y, (np.cos(h) * 170, np.sin(h) * 170))
                    continue
                water.put(f, G, h, x, y, glint=glint)
        ship(f, px, py, t)
        frames.append(f)
    return frames


def glass_sheet(v, frames):
    G = glass[v]
    heads = [G.frame(k) for k in range(16)]
    st = [G.get(DOWN), G.get(DOWN, glint=1.0), lit(G.get(DOWN), 0.06, 0.2, G.glow_get(DOWN))]
    sh = []
    for s_ in (0.1, 0.4, 0.7):
        c = Image.new("RGBA", (40, 40), (6, 30, 38, 255))
        c = shatter(c, 20, 20, s_)
        sh.append(c)
    sk = e36.e4.R04["skitter-a"]
    skit = e3.render(sk[4], sk[3])[1]
    panels = [("SOURCE, 260 PX", source_panel(lambda: glassfin(v))),
              ("16 HEADINGS (2X)", grid(heads, 8, 2)),
              ("IDLE / GLINT (THE RAM TELL) / IN THE DARK (4X)",
               thumbs(st, GF_N + 6, 4, ["IDLE", "GLINT", "DARK"])),
              ("GLASS SHATTER DEATH (2X)", grid(sh, 3, 2)),
              ("NEXT TO A SKITTER AND THE PLAYER (2X)",
               thumbs([skit, G.get(DOWN), player_cell()], 48, 2, ["SKITTER", "GLASSFIN", "PLAYER"]))]
    if v == "a":
        notes = ["GLASSFIN A - NEEDLE FRY (CLEAR VRELL GLASS, TEAL SPINE, DARK EYES)",
                 "16X16 PX (18 PX CANVAS), 16 HEADINGS, 1 HP, THE ACT'S TINY POPCORN. A THIN GLASS",
                 "NEEDLE WITH A FAN TAIL AND A GLOWING TEAL SPINE (THE CONTACT HUE). SCHOOLS STRING",
                 "OUT IN RIBBONS ALONG AN S-ROUTE (A 14 PX SWAY); AFTER 3 S A FEW GLINT TEAL FOR 0.3 S",
                 "AND RAM THE SHIP'S LANE AT 230 PX/S (AIMED ONCE, NO HOMING). DEATH: A GREY-TEAL",
                 "FLASH AND FOUR SLOW GLASS SHARDS, NO BUBBLES. IN THE DARK ONLY THE SPINE SHOWS.",
                 "GIF: TWO RIBBONS OF 14 OVER L23'S LAMP-LIT FARMS; ONE RAM HITS THE SHIELD."]
    else:
        notes = ["GLASSFIN B - LEAF LARVA (A FLAT CLEAR GLASS LEAF, TEAL SPINE, TINY HEAD)",
                 "16X16 PX, 16 HEADINGS, 1 HP. A FLAT TRANSPARENT LEAF (AN EEL LARVA) WITH THE TEAL",
                 "SPINE DOWN ITS MIDDLE: A BROADER SHAPE THAN A, SO IT READS BETTER AT 1X; LESS",
                 "OBVIOUSLY A FISH. SAME RIBBONS, GLINT, RAM AND SHATTER; SAME SCENE."]
    return sheet(f"GLASSFIN {v.upper()} - TINY GLASS SCHOOL (LEVEL 23)", panels, notes,
                 frames[int(4.4 * FPS)])


def make_glass(variants):
    for v in variants:
        frames = glass_scene(v)
        save_unit("naval", "glassfin", v, glass_sheet(v, frames), frames)


# ===================================================================== 8 Drill Rig (L22, ice surface)

def rig_core(v, rot=0.0, glow=1.0):
    """The hardened Vrell drill rig's core (120 px, fixed): a collar boring into Europa's ice round
    a lime bore (the vital core); ``rot`` turns the collar (decorative spin).
    a: bone bore tower - a slate base ring with eight bone teeth round the bore and radial ribs;
    b: rust auger flower - three curved rust auger blades round the bore on a chitin mound."""
    def scene(p):
        r = rotate_z(p, rot)
        items = [(sd_cylinder_z(p, (0, 0, 0.02), 0.95, 0.05), V_DARK),
                 (sd_cylinder_z(p, (0, 0, 0.2), 0.3, 0.14), V_GLOW),
                 (sd_sphere(p, (0, 0, 0.16), 0.26), V_EYE)]
        if v == "a":
            items += [(sd_torus_z(p, (0, 0, 0.16), 0.66, 0.2), V_BODY),
                      (sd_torus_z(p, (0, 0, 0.3), 0.42, 0.09), V_SEAM)]
            f8 = folded(r, 8)
            items.append((sd_capsule(f8, (0.4, 0, 0.36), (0.22, 0, 0.5), 0.09, 0.03), V_BONE))
            f6 = folded(p, 6, 0.0)
            items.append((sd_capsule(f6, (0.8, 0, 0.12), (1.04, 0, 0.04), 0.1, 0.05), V_BONE))
        else:
            items.append((sd_ellipsoid(p, (0, 0, 0.0), (0.82, 0.82, 0.3)), V_BODY))
            f3 = folded(r, 3, 0.0)
            for k in range(4):
                a = -0.35 + 0.28 * k
                rr = 0.32 + 0.14 * k
                items.append((sd_capsule(f3, (np.cos(a) * rr, np.sin(a) * rr, 0.42 - 0.06 * k),
                                         (np.cos(a + 0.28) * (rr + 0.14), np.sin(a + 0.28) * (rr + 0.14),
                                          0.36 - 0.06 * k), 0.08 - 0.012 * k), V_SEAM))
            f9 = folded(p, 9, 0.0)
            items.append((sd_sphere(f9, (0.78, 0, 0.12), 0.07), V_BONE))
        return union(*items, k=0.04)
    return scene, e36.lift(vmats(f"drill-rig-{v}", glow, "a" if v == "a" else "b"), 0.15)


def rig_pump(v, pulse=0.0, glow=1.0):
    """A pump (32 px, destroyable): a lime-veined sac (a) / a ribbed rust bellows (b) on a root."""
    def scene(p):
        if v == "a":
            return union((sd_ellipsoid(p, (0, 0, 0.1), (0.62 + 0.08 * pulse,) * 2 + (0.45,)), V_SAC),
                         (sd_torus_z(p, (0, 0, 0.05), 0.68, 0.12), V_BODY),
                         (sd_sphere(p, (0, 0, 0.5), 0.16), V_GLOW), k=0.05)
        f5 = folded(p, 5, 0.0)
        return union((sd_ellipsoid(p, (0, 0, 0.12), (0.7, 0.7, 0.4 + 0.08 * pulse)), V_BODY),
                     (sd_capsule(f5, (0.15, 0, 0.5), (0.7, 0, 0.14), 0.08, 0.05), V_SEAM),
                     (sd_sphere(p, (0, 0, 0.52), 0.14), V_GLOW), k=0.05)
    return scene, e36.lift(vmats(f"drill-rig-{v}", glow, "a" if v == "a" else "b"), 0.15)


RIG_N, PUMP_N = 124, 34
cores = {v: rig.ModelSpaceAngleSprites(lambda rot=0.0, v=v: rig_core(v, rot), RIG_N, 1, factor=4,
                                       colors=40) for v in "ab"}
pumps = {v: rig.ModelSpaceAngleSprites(lambda pulse=0.0, v=v: rig_pump(v, pulse), PUMP_N, 1,
                                       factor=5, colors=24) for v in "ab"}
RIG_C = (240.0, 205.0)
PUMPS = [(-66, -60), (66, -60), (66, 62), (-66, 62)]
PUMP_KILL = [3.1, 3.7, 5.9, 6.5]
CORE_KILL = 8.3
RIG_DUR = 10.5


def rig_rot(t, v):
    sym = 8 if v == "a" else 3
    stop = min(t, CORE_KILL)
    k = int(stop * 6) % 4
    return k * (TAU / sym) / 4


def ice_shelf(seed=22):
    rng = np.random.default_rng(seed)
    n = raster.fbm(FW, FH, 90, seed, octaves=5, period=False)
    shade = e3._hillshade(n * 40, 0.6)
    rgb = raster.ramp([(0.0, (70, 104, 134)), (0.45, (150, 186, 210)), (0.8, (205, 226, 240)),
                       (1.0, (236, 246, 252))], n) * shade[..., None]
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB")
    img = img.filter(ImageFilter.GaussianBlur(1.0)).quantize(12, dither=Image.Dither.NONE).convert("RGBA")
    d = ImageDraw.Draw(img)
    for _ in range(14):                                     # pressure cracks
        x, y = rng.uniform(0, FW), rng.uniform(0, FH)
        a = rng.uniform(0, TAU)
        pts = [(x, y)]
        for _ in range(int(rng.integers(6, 14))):
            a += rng.uniform(-0.6, 0.6)
            x, y = x + np.cos(a) * 9, y + np.sin(a) * 9
            pts.append((x, y))
        d.line(pts, fill=(40, 70, 100, 255), width=1)
        d.line([(px_ + 1, py_ + 1) for px_, py_ in pts], fill=(240, 250, 255, 160), width=1)
    return img


def frost_hose(img, a, b):
    d = ImageDraw.Draw(img)
    d.line([a, b], fill=(40, 30, 44, 255), width=9)
    d.line([a, b], fill=(110, 96, 112, 255), width=5)
    d.line([(a[0] - 1, a[1] - 1), (b[0] - 1, b[1] - 1)], fill=(170, 160, 175, 255), width=1)


def bomb_flight(img, sx, sy, tx, ty, u):
    """A dropped bomb: travels forward and shrinks as it falls to the ice (0 <= u < 1)."""
    x, y = sx + (tx - sx) * u, sy + (ty - sy) * u
    r = 4.5 - 2.5 * u
    d = ImageDraw.Draw(img)
    d.ellipse([x - r, y - r * 1.4, x + r, y + r * 1.4], fill=(60, 64, 72, 255), outline=(170, 176, 186, 255))


def steam(img, x, y, t0, t):
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for k in range(6):
        u = ((t - t0) * 0.7 + k / 6) % 1.0
        r = 10 + 30 * u
        a = int(150 * (1 - u) * min(1.0, (t - t0) / 0.6))
        d.ellipse([x - r + 14 * u, y - r - 40 * u, x + r + 14 * u, y + r - 40 * u],
                  fill=(236, 244, 250, a))
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(5)))


def rig_scene(v):
    bg = ice_shelf()
    cx, cy = RIG_C
    for dx, dy in PUMPS:
        frost_hose(bg, (cx, cy), (cx + dx, cy + dy))
    shots = Shots()
    t_ring, ts = 0.9, []
    while t_ring < CORE_KILL:
        ts.append(t_ring)
        alive = sum(1 for k in PUMP_KILL if k > t_ring)
        t_ring += 3.5 + 0.5 * (4 - alive)
    for tr in ts:
        shots.ring(tr, cx, cy, 10, 110 / WATER_SLOW, "shard", phase=tr)    # no water factor
    bombs = [(k - 0.55, *PUMPS[i]) for i, k in enumerate(PUMP_KILL)] + \
            [(CORE_KILL - 0.55, 0, 0), (CORE_KILL - 1.1, -14, 10)]
    frames = []
    for t in times(RIG_DUR):
        px, py = r5.player_at(t)
        f = bg.copy()
        if t >= CORE_KILL:
            d = ImageDraw.Draw(f)
            d.ellipse([cx - 48, cy - 48, cx + 48, cy + 48], fill=(26, 40, 58, 255))
            for k in range(7):
                a = k * TAU / 7 + 0.3
                d.line([cx + np.cos(a) * 40, cy + np.sin(a) * 40, cx + np.cos(a) * 92,
                        cy + np.sin(a) * 92], fill=(30, 56, 84, 255), width=2)
        else:
            e3.place(f, cores[v].get(0.0, rot=rig_rot(t, v)), cx, cy, "ground")
            for tr in ts:
                if tr - 0.5 <= t < tr:
                    f = raster.add_light(f, cx, cy, 30, LIME, 0.6 * (1 - (tr - t) / 0.5))
        for i, (dx, dy) in enumerate(PUMPS):
            if t < PUMP_KILL[i]:
                pl = 1.0 if int(t * 4 + i) % 2 else 0.0
                hit = PUMP_KILL[i] - 0.15 < t and int(t * 14) % 2
                e3.place(f, flash(pumps[v].get(0.0, pulse=pl), hit), cx + dx, cy + dy, "ground")
            else:
                d = ImageDraw.Draw(f)
                d.ellipse([cx + dx - 12, cy + dy - 12, cx + dx + 12, cy + dy + 12], fill=(30, 36, 44, 255))
                e6.explosion(f, cx + dx, cy + dy, (t - PUMP_KILL[i]) / 0.7, 1.2)
        for tb, dx, dy in bombs:
            if tb <= t < tb + 0.55:
                sx, sy = r5.player_at(tb)
                bomb_flight(f, sx, sy - 20, cx + dx, cy + dy, (t - tb) / 0.55)
        if t >= CORE_KILL:
            e6.explosion(f, cx, cy, (t - CORE_KILL) / 1.1, 3.2)
            if t > CORE_KILL + 0.5:
                steam(f, cx, cy, CORE_KILL + 0.5, t)
        shots.draw(f, t)
        stop = cy + 52 if abs(px - cx) < 52 and t < CORE_KILL else None
        if stop:                                             # hardened: the bolts glance off
            d = ImageDraw.Draw(f)
            for k in range(3):
                a = -np.pi / 2 + (k - 1) * 0.7 + 0.3 * np.sin(t * 17 + k)
                d.line([px - 10 + 20 * (k % 2), stop, px - 10 + 20 * (k % 2) + np.cos(a) * 6,
                        stop + np.sin(a) * -6], fill=(255, 250, 220, 255))
        ship(f, px, py, t, stop_y=stop)
        if 0.3 < t < 3.0:
            caption(f, "OKAFOR: DRILL RIGS. BULLETS WON'T BITE - BOMBS AND MISSILES.", t, 0.3, 3.0)
        frames.append(f)
    return frames


def rig_sheet(v, frames):
    rots = [cores[v].get(0.0, rot=k * (TAU / (8 if v == "a" else 3)) / 4) for k in range(4)]
    pm = [pumps[v].get(0.0, pulse=0.0), pumps[v].get(0.0, pulse=1.0)]
    full = Image.new("RGBA", (260, 260), (0, 0, 0, 0))
    for dx, dy in PUMPS:
        frost_hose(full, (130, 130), (130 + dx, 130 + dy))
    sprite.paste_center(full, rots[0], 130, 130)
    for dx, dy in PUMPS:
        sprite.paste_center(full, pm[0], 130 + dx, 130 + dy)
    sprite.paste_center(full, e3.player_sprite(), 236, 236)
    panels = [("SOURCE: CORE, 260 PX", source_panel(lambda: rig_core(v))),
              ("THE SET AT 1X: CORE, HOSES, 4 PUMPS (AND THE PLAYER)", grid([full], 1, 1)),
              ("COLLAR SPIN: 4 FRAMES (1X)", grid(rots, 4, 1)),
              ("PUMP: 2 PULSE FRAMES (2X)", grid(pm, 2, 2))]
    if v == "a":
        notes = ["DRILL RIG A - BONE BORE TOWER (SLATE CHITIN, BONE TEETH, LIME BORE) - LEVEL 22",
                 "CORE 120 PX (124 PX CANVAS, FIXED), PUMPS 32 PX. A SLATE BASE RING ON THE ICE WITH",
                 "BONE BUTTRESSES, A COLLAR OF EIGHT BONE TEETH THAT SPINS (4 FRAMES) ROUND A LIME",
                 "BORE (THE VITAL CORE); FOUR LIME-VEINED PUMP SACS ON FLESHY HOSES THAT PULSE.",
                 "HARDENED: THE PLAYER'S BOLTS GLANCE OFF (SPARKS); BOMBS AND MISSILES TAKE THE PUMPS",
                 "(EACH SLOWS THE SHARD RING BY 0.5 S) AND THE CORE. THE COLLAR FIRES A RING OF 10 LIME",
                 "ICE SHARDS (ABOVE WATER, NO WATER FACTOR). DEATH: A LARGE GROUND EXPLOSION, A DARK",
                 "BORE HOLE WITH CRACKS, A STEAM PLUME (SCENERY). ON CONAMARA'S ICE SHELF."]
    else:
        notes = ["DRILL RIG B - RUST AUGER FLOWER (RUST CHITIN, LIME BORE) - LEVEL 22",
                 "CORE 120 PX, PUMPS 32 PX. A RUST CHITIN MOUND WITH THREE CURVED AUGER BLADES",
                 "TURNING ROUND THE LIME BORE (A FLOWER THAT SCREWS ITSELF INTO THE ICE), BONE",
                 "STUDS ROUND ITS RIM; FOUR RIBBED RUST BELLOWS PUMPS THAT HEAVE. MORE ORGANIC-",
                 "MECHANICAL THAN A. SAME HARDENED RULE, SHARD RING, PUMPS AND DEATH; SAME SCENE."]
    return sheet(f"DRILL RIG {v.upper()} - HARDENED ICE-SHELF TARGET (LEVEL 22)", panels, notes,
                 frames[int(3.5 * FPS)])


def make_rig(variants):
    for v in variants:
        frames = rig_scene(v)
        save_unit("ground", "drill-rig", v, rig_sheet(v, frames), frames)


UNITS = {"eel-swarm": make_eel, "barbfin": make_barb, "abyss-ray": make_ray, "siren": make_siren, "glow-angler": make_angler, "depth-hunter": make_hunter, "glassfin": make_glass, "drill-rig": make_rig}


def main(args):
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    names = [a for a in args if a in UNITS] or list(UNITS)
    for n in names:
        UNITS[n](variants)


if __name__ == "__main__":
    main(sys.argv[1:])
