#!/usr/bin/env python3
"""Concept round 48 - the Act 4 "Deep Water" boss and the reveal's Ascendancy cruiser, a/b each
(Act 4 design phase, step A-boss, 2026-10-21; pending the user's review).

Abyssal Maw (L28 act boss, the Vrell hatchery under Europa's ice, round 48 Q9). Spec:
design/enemies/bosses/abyssal-maw/README.md. Dark abyssal chitin plates over a deep-violet hide, a
bioluminescent teal line down each flank, Vrell weak points lime, and three grey unmarked Helix
implants with a hot-amber core (the Ascendancy rule for exposed cores); nothing gold (Q11 a).

  abyssal-maw-r48-a.{png,gif}   A "rising maw" (Q9 a, the roster's draft): hidden in the black
                                water; a shadow grows under the ship, a broad two-jawed head bursts
                                up through the play plane and snaps, sinks shedding Eel Swarms, and
                                fires bioluminescent fan walls from the deep between snaps; the
                                implants sit on its lower jaw.
  abyssal-maw-r48-b.{png,gif}   B "coiled guardian" (Q9 b, recommended): the scroll halts over the
                                hatchery vent; a ~1,750 px serpent lies coiled round the arena's
                                edges (31 segments, 32 angles), head at the top. Phase 1 gill-sac
                                rings and the throat fan; phase 2 fan walls with a gap and a coil
                                sweep; the one dive into the dark and the lunge up a shadow column;
                                phase 3 the Helix implants spark open.

Ascendancy cruiser (the L28 reveal, Q10 a): the faction's first black-and-gold hulls, invulnerable
`low-air` set pieces ~220x640 px, one on each flank with half the hull beyond the field's edge.

  ascendancy-cruiser-r48-a.{png,gif}  A "Regent wedge": a long faceted wedge, stepped spine and a
                                      bridge tower, one row of gun ports down each flank, a crimson
                                      accent, the gold sunburst emblem and the hull code on the bow.
  ascendancy-cruiser-r48-b.{png,gif}  B "Forked monitor": a forked ram bow, broad sponsons with three
                                      twin casemate guns a side, a row of gold crown fins on the
                                      spine, an ivory accent.
  Both GIFs: the two cruisers break up through the Europa ice beside the wing (cracks, slabs
  sheeting off, an open-water channel carved behind them), then the run: broadside bands (the gun
  ports glow, amber tracer lines, 7 shells), flak markers and point-defence bursts.

Each PNG is a sheet (the full sprite with callouts, parts and states, key frames, a frame from the
GIF, notes); each GIF is the play field 480x540 at 1x with the player for scale, at 15 fps. Models
live in this file; the frozen ``render`` package is only imported.
Run: python3 tools/concept/bosses_r48.py [maw-a] [maw-b] [cruiser-a] [cruiser-b]
     (no args = all four, roughly 10 minutes on 8 cores)
"""
import sys
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import bosses_r06 as b6  # noqa: E402
import enemies_r03 as e3  # noqa: E402
import enemies_r04 as e4  # noqa: E402  (the Ascendancy rim light)
import enemies_r05 as r5  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite, terrain  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import (A_ENGINE, A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL,  # noqa: E402
                                 A_RED, hx)
from render.sdf import (Material, mirror_x, rotate_x, sd_box, sd_capsule,  # noqa: E402
                        sd_cylinder_y, sd_cylinder_z, sd_ellipsoid, sd_plate, sd_polygon2,
                        sd_sphere, union)

FW, FH, TAU = 480, 540, 2 * np.pi
FPS = 15
b6.SUB = "CONCEPT ROUND 48 - 960X540"
OUT = ROOT / "design" / "enemies" / "bosses" / "concept"
MS = rig.ModelSpaceAngleSprites
DOWN, UP = np.pi / 2, -np.pi / 2
WATER_SLOW = 0.7                       # bullets x 0.7 under water (both sides)

TEAL = (0, 255, 154)
LIME = (168, 255, 42)
VIOLET = (154, 77, 255)
AMBER = (255, 122, 32)
GOLD_C = (255, 200, 60)
DEEP = np.array((3, 14, 22), dtype=np.float64)        # the abyss's water colour
TEAL_F, LIME_F, AMBER_F = (np.array(c) / 255.0 for c in (TEAL, LIME, AMBER))


def times(seconds):
    return [i / FPS for i in range(int(round(seconds * FPS)))]


def ease(x):
    x = float(np.clip(x, 0.0, 1.0))
    return x * x * (3 - 2 * x)


def win(t, a, b):
    """0 before a, ramps to 1 at b (eased)."""
    return ease((t - a) / (b - a))


def save(slug, variant, sheet, frames):
    OUT.mkdir(parents=True, exist_ok=True)
    png = OUT / f"{slug}-r48-{variant}.png"
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, OUT / f"{slug}-r48-{variant}.gif", fps=FPS)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)",
          flush=True)


# =========================================================================== rendering helpers

_JOBS = None


def _job(i):
    return _JOBS[i]()


def run_parallel(fns, procs=8):
    """Evaluate zero-argument closures in forked workers (closures need not pickle)."""
    global _JOBS
    import multiprocessing as mp
    if not fns:
        return []
    _JOBS = list(fns)
    with mp.get_context("fork").Pool(min(procs, len(_JOBS))) as pool:
        return pool.map(_job, range(len(_JOBS)))


def blackened(make):
    """The same model with every surface black: only the emission is left (its glow)."""
    def make2(**kw):
        scene, mats = make(**kw)
        return scene, [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None)
                       for m in mats]
    return make2


def glow_alpha(sp):
    a = np.array(sp).astype(np.float64)
    lum = a[..., :3].max(axis=-1)
    a[..., 3] = np.where(a[..., 3] > 0, np.clip((lum - 18) * 2.4, 0, 255), 0)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


class Static:
    """A model that is not turned (model +Y = screen up), rendered once per parameter set,
    with its glow-only twin for the dark arena."""

    def __init__(self, make, size, extent, factor=3, colors=48, post=None, twin=True):
        self.make, self.size, self.extent = make, size, extent
        self.factor, self.colors, self.post, self.twin = factor, colors, post, twin
        self.cache = {}

    def _render(self, make, glow, kw):
        scene, mats = make(**kw)
        w, h = (self.size, self.size) if isinstance(self.size, int) else self.size
        hi = sdf.render(scene, mats, (w * self.factor, h * self.factor), self.extent)
        sp = sprite.make_sprite(hi, self.factor, 24 if glow else self.colors, crisp=70)
        if glow:
            return glow_alpha(sp)
        return self.post(sp) if self.post else sp

    @staticmethod
    def key(kw):
        return tuple(sorted((a, b if isinstance(b, str) else round(float(b), 4))
                            for a, b in kw.items()))

    def jobs(self, kws):
        out = []
        for kw in kws:
            for g in ((False, True) if self.twin else (False,)):
                k = (self.key(kw), g)
                if k not in self.cache:
                    mk = blackened(self.make) if g else self.make
                    out.append((self, k, (lambda s=self, m=mk, gg=g, kk=dict(kw): s._render(m, gg, kk))))
        return out

    def get(self, **kw):
        k = (self.key(kw), False)
        if k not in self.cache:
            self.cache[k] = self._render(self.make, False, kw)
        return self.cache[k]

    def glow(self, **kw):
        k = (self.key(kw), True)
        if k not in self.cache:
            self.cache[k] = self._render(blackened(self.make), True, kw)
        return self.cache[k]


class Turned:
    """A heading-turned model (``MS``) and its glow-only twin."""

    def __init__(self, make, size, count, extent, factor=3, colors=32):
        self.full = MS(make, size, count, extent=extent, factor=factor, colors=colors)
        self.glo = MS(blackened(make), size, count, extent=extent, factor=factor, colors=20,
                      post=glow_alpha)

    def jobs(self, kws, headings):
        out = []
        for ms in (self.full, self.glo):
            for kw in kws:
                for h in headings:
                    k = ms.index(h)
                    key = (k, tuple(sorted((a, round(float(b), 4) if not isinstance(b, str) else b)
                                           for a, b in kw.items())))
                    if key not in ms.cache:
                        out.append((ms, key, (lambda m=ms, kk=k, w=dict(kw): m.frame(kk, **w))))
        return out

    def get(self, h, **kw):
        return self.full.get(h, **kw)

    def glow(self, h, **kw):
        return self.glo.get(h, **kw)


def prerender(job_lists, procs=8):
    jobs = [j for jl in job_lists for j in jl]
    seen, uniq = set(), []
    for owner, key, fn in jobs:
        if (id(owner), key) not in seen:
            seen.add((id(owner), key))
            uniq.append((owner, key, fn))
    res = run_parallel([fn for _, _, fn in uniq], procs)
    for (owner, key, _), sp in zip(uniq, res):
        owner.cache[key] = sp
    print(f"  rendered {len(uniq)} sprites", flush=True)


def lit(sp, k, glow=None, tint=0.2):
    """``sp`` dimmed to light level ``k`` and tinted toward the abyss; the glow-only twin is
    max-blended on top so the glows stay bright in the dark."""
    a = np.array(sp).astype(np.float64)
    rgb = a[..., :3] * (1 - tint) + DEEP * tint
    a[..., :3] = rgb * min(k, 1.2)
    if glow is not None:
        g = np.array(glow).astype(np.float64)
        ga = g[..., 3:4] / 255.0
        a[..., :3] = np.maximum(a[..., :3], g[..., :3] * ga)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def deep(sp, d):
    """A sprite sunk ``d`` (0 = on the play plane, 1 = gone in the deep): smaller, darker,
    fading into the water, softened."""
    if d <= 0.01:
        return sp
    k = 1.0 - 0.22 * d
    if k < 0.999:
        sp = sp.resize((max(1, int(sp.width * k)), max(1, int(sp.height * k))), Image.LANCZOS)
    a = np.array(sp).astype(np.float64)
    m = min(1.0, 0.25 + 0.8 * d)
    a[..., :3] = a[..., :3] * (1 - m) + DEEP * m
    a[..., 3] *= max(0.0, 1.0 - d ** 1.4)
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    if d > 0.25:
        img = img.filter(ImageFilter.GaussianBlur(1.2 * d))
    return img


def flash(sp, on=True, k=0.55):
    if not on:
        return sp
    a = np.array(sp).astype(np.float64)
    a[..., :3] = a[..., :3] * (1 - k) + 255 * k
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def cropped(sp, pad=3):
    return b6.cropped(sp, pad)


def scaled(sp, k):
    return sp.resize((max(1, int(sp.width * k)), max(1, int(sp.height * k))), Image.LANCZOS)


def cells(items, size, scale=2, labels=None, bg=None):
    """Sprites centred on equal cells (checker or a flat colour), one row."""
    w, h = (size, size) if isinstance(size, int) else size
    out = Image.new("RGBA", (len(items) * (w * scale + 6) - 6, h * scale + (12 if labels else 0)),
                    raster.SHEET_BG)
    for i, it in enumerate(items):
        c = e3.checker(w, h) if bg is None else Image.new("RGBA", (w, h), tuple(bg) + (255,))
        sprite.paste_center(c, it, w / 2, h / 2)
        out.alpha_composite(sprite.enlarge(c, scale), (i * (w * scale + 6), 0))
        if labels:
            raster.draw_text(out, i * (w * scale + 6), h * scale + 3, labels[i], raster.LABEL_DIM)
    return out


def key_frames(frames, picks, k=0.5):
    """A row of scaled GIF frames with their labels."""
    ims = [scaled(frames[min(len(frames) - 1, int(t * FPS))], k) for t, _ in picks]
    w, h = ims[0].size
    out = Image.new("RGBA", (len(ims) * (w + 8) - 8, h + 12), raster.SHEET_BG)
    for i, (im, (t, lab)) in enumerate(zip(ims, picks)):
        out.alpha_composite(im.convert("RGBA"), (i * (w + 8), 0))
        raster.draw_text(out, i * (w + 8), h + 3, f"{t:.1f}S {lab}", raster.LABEL_DIM)
    return out


# =========================================================================== shots and effects

class Shots:
    """Straight shots (t0, x, y, vx, vy, kind, r). ``water``: speeds x 0.7."""

    def __init__(self, water=True):
        self.list, self.k = [], (WATER_SLOW if water else 1.0)

    def fire(self, t0, x, y, tx, ty, speed, kind="teal", r=4):
        d = float(np.hypot(tx - x, ty - y)) or 1.0
        s = speed * self.k
        self.list.append((t0, x, y, (tx - x) / d * s, (ty - y) / d * s, kind, r))

    def dir(self, t0, x, y, ang, speed, kind="teal", r=4):
        self.fire(t0, x, y, x + np.cos(ang), y + np.sin(ang), speed, kind, r)

    def fan(self, t0, x, y, aim, n, spread, speed, kind="teal", r=4, skip=()):
        for i, a in enumerate(np.linspace(-spread / 2, spread / 2, n) + aim):
            if i not in skip:
                self.dir(t0, x, y, a, speed, kind, r)

    def ring(self, t0, x, y, n, speed, kind="teal", r=4, phase=0.0):
        for a in np.linspace(0, TAU, n, endpoint=False) + phase:
            self.dir(t0, x, y, a, speed, kind, r)

    def draw(self, img, t):
        d = ImageDraw.Draw(img)
        for t0, x, y, vx, vy, kind, r in self.list:
            if t < t0:
                continue
            bx, by = x + vx * (t - t0), y + vy * (t - t0)
            if not (-20 < bx < FW + 20 and -20 < by < FH + 20):
                continue
            if kind == "teal":
                e3.orb(img, bx, by, r, ring=TEAL)
            elif kind == "violet":
                e3.orb(img, bx, by, r, ring=VIOLET)
            elif kind == "bolt":                       # an implant's electric bolt (amber)
                ang = np.arctan2(vx, vy)
                e3.thorn(img, bx, by, ang, 7, ring=AMBER)
            elif kind == "shell":                      # Ascendancy shell: gold tracer
                n = np.hypot(vx, vy) or 1
                ux, uy = vx / n, vy / n
                d.line([(bx - ux * 16, by - uy * 16), (bx, by)], fill=(255, 150, 40, 200), width=3)
                e3.orb(img, bx, by, r, ring=GOLD_C)
            elif kind == "flak":
                e3.orb(img, bx, by, r, ring=(255, 70, 50))
            elif kind == "pd":
                d.ellipse([bx - r - 1, by - r - 1, bx + r + 1, by + r + 1], fill=(60, 20, 0, 255))
                d.ellipse([bx - r, by - r, bx + r, by + r], fill=GOLD_C + (255,))
                d.point((bx, by), fill=(255, 255, 255, 255))


def ship(img, px, py, t, shots=True, stop=None, light=None):
    """The player's Stormhawk with its front bolts (x 0.7 under water)."""
    sprite.paste_center(img, e3.player_sprite(), px, py)
    if shots:
        for k in range(4):
            y = py - 40 - ((t * 300 + k * 90) % 420)
            if y > -10 and (stop is None or y > stop(px, y)):
                e3.bolt(img, px - 10, y)
                e3.bolt(img, px + 10, y)


def uw_burst(img, x, y, s, size=1.0, col=(255, 200, 120)):
    """Under-water kill: a flash, a pale pressure ring and debris, no fire ball."""
    if not 0 <= s <= 1:
        return img
    rr = (6 + 18 * s) * size
    img = raster.add_light(img, x, y, rr * 1.4, col, (1.0 - s) * 1.1)
    d = ImageDraw.Draw(img)
    if s < 0.35:
        r0 = rr * 0.5 * (1 - s)
        d.ellipse([x - r0, y - r0, x + r0, y + r0], fill=(255, 240, 210, int(255 * (1 - s / 0.35))))
    r1 = (10 + 50 * s) * size
    d.ellipse([x - r1, y - r1, x + r1, y + r1], outline=(170, 230, 230, int(170 * (1 - s))), width=1)
    for k in range(7):
        a = k * 0.9 + 0.4
        dd = rr * (0.4 + 1.1 * s)
        d.rectangle([x + np.cos(a) * dd - 1, y + np.sin(a) * dd - 1 + 8 * s,
                     x + np.cos(a) * dd + 1, y + np.sin(a) * dd + 1 + 8 * s],
                    fill=(110, 200, 90, int(255 * (1 - s))))
    return img


def silt(img, x, y, t, seed, n=8, spread=24, rise=1.0, col=(70, 82, 80)):
    """Silt streaming off plates (a tell, and the coil rising)."""
    if rise <= 0:
        return
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    for i in range(n):
        ox, oy = rng.uniform(-spread, spread, 2)
        ph = rng.uniform(0, 1)
        age = (t * 0.9 + ph) % 1.0
        r = 2 + 5 * age
        a = int(110 * rise * (1 - age))
        cx, cy = x + ox * (1 + 0.4 * age), y + oy * (1 + 0.4 * age) + 6 * age
        d.ellipse([cx - r, cy - r, cx + r, cy + r], fill=col + (a,))


def sparks(img, x, y, t, seed, n=7, r=16, col=(255, 230, 150)):
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed + int(t * FPS))
    for _ in range(n):
        a = rng.uniform(0, TAU)
        r0 = rng.uniform(2, r * 0.6)
        r1 = r0 + rng.uniform(3, 8)
        d.line([(x + np.cos(a) * r0, y + np.sin(a) * r0), (x + np.cos(a) * r1, y + np.sin(a) * r1)],
               fill=col + (255,), width=1)


def arc(img, x, y, t, seed, r=14):
    """An amber electric arc crawling over a housing (the burst's tell)."""
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed + int(t * FPS * 2))
    pts = [(x + np.cos(a) * r * rng.uniform(0.6, 1.1), y + np.sin(a) * r * rng.uniform(0.6, 1.1))
           for a in np.linspace(0, TAU * 0.8, 7) + rng.uniform(0, TAU)]
    d.line(pts, fill=(255, 170, 60, 255), width=2)
    d.line(pts, fill=(255, 245, 210, 255), width=1)


def caption(img, text, y=20):
    w = raster.text_width(text) + 12
    d = ImageDraw.Draw(img)
    d.rectangle([8, y, 8 + w, y + 14], fill=(10, 14, 22, 210), outline=(80, 110, 140, 255))
    raster.draw_text(img, 14, y + 4, text, (255, 210, 120))


def phase_label(img, text):
    w = raster.text_width(text) + 8
    d = ImageDraw.Draw(img)
    d.rectangle([4, FH - 16, 4 + w, FH - 4], fill=(0, 0, 0, 150))
    raster.draw_text(img, 8, FH - 13, text, (200, 210, 225))


def boss_bar(img, name, frac, alpha=1.0):
    if alpha <= 0:
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    d.rectangle([60, 3, 420, 16], fill=(8, 10, 16, 220), outline=(90, 100, 120, 255))
    raster.draw_text(lay, 66, 6, name, (230, 235, 245))
    x0, x1 = 66 + raster.text_width(name) + 8, 414
    d.rectangle([x0, 7, x1, 12], fill=(40, 20, 24, 255))
    d.rectangle([x0, 7, x0 + (x1 - x0) * max(0.0, frac), 12], fill=(230, 60, 60, 255))
    if alpha < 1:
        lay.putalpha(lay.getchannel("A").point(lambda v: int(v * alpha)))
    img.alpha_composite(lay)


# =========================================================================== Abyssal Maw: models

(M_PLATE, M_HIDE, M_LINE, M_THROAT, M_SAC, M_BONE, M_GREY, M_CORE, M_CABLE, M_EYE,
 M_FLARE) = range(11)
PLATE_C = (0.11, 0.13, 0.17)
HIDE_C = (0.26, 0.10, 0.34)


def _scorch(p, n):
    v = np.sin(p[:, 0] * 7.3 + np.sin(p[:, 1] * 5.1) * 2.0) * np.sin(p[:, 1] * 6.7 + 1.3)
    return 0.62 + 0.38 * np.clip(0.5 + v, 0, 1)


def maw_mats(glow=1.0, line=0.0, flare=0.0, throat=0.0, swell=0.0, core=0.0):
    """The Maw's material slots: plates, hide, the teal flank line (lime as a sweep's tell),
    the lime throat and sacs, bone teeth, the implants' grey housings and amber cores."""
    linec = TEAL_F * (1 - line) + LIME_F * line
    return [
        Material(PLATE_C, metal=0.35, shininess=110, spec=1.0, pattern=em._plates(3.0, 0.07)),
        Material(HIDE_C, metal=0.1, shininess=60, spec=0.7, pattern=em._ridges(18, 0.2)),
        Material((0.03, 0.03, 0.03), emission=tuple(linec * (1.5 + 0.6 * line) * glow)),
        Material((0.04, 0.04, 0.04), emission=tuple(LIME_F * (0.35 + 1.25 * throat) * glow)),
        Material((0.30, 0.46, 0.22), metal=0.1, shininess=90, spec=1.2,
                 emission=tuple(LIME_F * (0.45 + 0.9 * swell) * glow),
                 emission_pattern=em._veins(11, 0.3, 0.45)),
        Material((0.86, 0.81, 0.70), metal=0.2, shininess=70, spec=0.7),
        Material((0.47, 0.49, 0.51), metal=0.6, shininess=80, spec=0.9, pattern=_scorch),
        Material((0.05, 0.03, 0.02), emission=tuple(AMBER_F * (0.12 + 1.6 * core))),
        Material((0.07, 0.07, 0.08), metal=0.3, shininess=40, spec=0.4),
        Material((0.03, 0.03, 0.03), emission=tuple(TEAL_F * 1.3 * glow)),
        Material(PLATE_C, metal=0.35, shininess=110, spec=1.0,
                 emission=tuple(LIME_F * 1.3 * flare)),
    ]


# ---- B: the coiled guardian

SEG_S = 32.0                       # px per model unit for the coil (a segment is 64 px wide)


def coil_seg(w=1.0, line=0.0, flare=0.0):
    """One coil segment, forward (+Y) toward the head: violet hide, a shingled chitin plate
    whose front edge overlaps the next segment, a bone keel, a teal glow line on each flank."""
    plate = [(0.0, 1.05), (0.72, 0.62), (0.80, -0.40), (0.42, -0.86), (0.0, -0.95),
             (-0.42, -0.86), (-0.80, -0.40), (-0.72, 0.62)]
    plate = [(x * w, y) for x, y in plate]

    def scene(p):
        q = mirror_x(p)
        roof = lambda x, y: np.clip(1.0 - 0.5 * np.abs(x) / max(w, 0.1), 0.45, 1.0)
        d, m = union(
            (sd_ellipsoid(p, (0, -0.05, 0.0), (0.98 * w, 1.18, 0.34 * w)), M_HIDE),
            (sd_plate(p, plate, 0.30 * w, 0.13 * w, 0.04, taper=roof), M_FLARE if flare else M_PLATE),
            (sd_capsule(q, (0.83 * w, 0.95, 0.2 * w), (0.83 * w, -0.9, 0.2 * w), 0.075 * w + 0.02),
             M_LINE),
            (sd_capsule(p, (0, 0.62, 0.46 * w), (0, -0.55, 0.42 * w), 0.07 * w, 0.03), M_BONE),
            k=0.035,
        )
        return d, m
    return scene, maw_mats(1.0, line=line, flare=flare)


SEG = Turned(coil_seg, 80, 32, 80 / SEG_S, factor=3, colors=32)
SEG_W = (1.0, 0.82, 0.66, 0.5)    # the tail tapers in four steps

HEAD_S = 60.0                      # px per model unit for the head


def maw_head(open_=0.0, throat=0.3, up=0.0):
    """The head, snout toward the screen's bottom (or the top when ``up``): an eel skull of
    dark plates with bone brow ridges and teal eyes over an underslung lower jaw; opening, the
    skull's front lifts on its hinge and bares the lime throat in the toothed jaw bowl."""
    phi = np.radians(40.0 * open_)
    hinge = np.array([0.0, 0.25, 0.32])
    teeth = [(0.70 * np.cos(a), -0.42 + 0.98 * np.sin(a)) for a in np.linspace(np.pi * 1.06, np.pi * 1.94, 11)]

    def scene(p):
        if up:
            p = p * np.array([1.0, -1.0, 1.0])
        q = mirror_x(p)
        # the back of the head and the neck (fixed)
        items = [
            (sd_ellipsoid(p, (0, 0.55, 0.18), (1.0, 0.72, 0.5)), M_HIDE),
            (sd_capsule(p, (0, 0.7, 0.12), (0, 1.5, 0.05), 0.72, 0.62), M_HIDE),
            (sd_plate(p, [(0, 1.55), (0.6, 1.2), (0.62, 0.45), (0, 0.25), (-0.62, 0.45), (-0.6, 1.2)],
                      0.62, 0.1, 0.04), M_PLATE),
            (sd_capsule(q, (0.86, 1.3, 0.25), (0.92, 0.35, 0.3), 0.07), M_LINE),
        ]
        # the lower jaw: a bowl with the throat and a row of teeth on its rim
        jaw = sd_ellipsoid(p, (0, -0.42, -0.02), (0.84, 1.08, 0.3))
        bowl = sd_ellipsoid(p, (0, -0.42, 0.16), (0.68, 0.9, 0.24))
        items.append((np.maximum(jaw, -bowl), M_HIDE))
        items.append((sd_ellipsoid(p, (0, -0.25, 0.0), (0.5, 0.66, 0.1)), M_THROAT))
        for tx, ty in teeth:
            items.append((sd_capsule(p, (tx, ty, 0.12), (tx * 0.9, ty * 0.97 + 0.03, 0.32), 0.05, 0.015),
                          M_BONE))
        # the upper skull, lifted on its hinge by ``open_``
        s = rotate_x(p - hinge, phi) + hinge
        sq = mirror_x(s)
        items += [
            (sd_ellipsoid(s, (0, -0.12, 0.34), (0.88, 0.8, 0.34)), M_PLATE),
            (sd_ellipsoid(s, (0, -0.95, 0.3), (0.56, 0.62, 0.24)), M_PLATE),
            (sd_capsule(sq, (0.52, 0.32, 0.64), (0.4, -0.45, 0.62), 0.07, 0.04), M_BONE),
            (sd_sphere(sq, (0.66, -0.25, 0.56), 0.11), M_EYE),
            (sd_capsule(sq, (0.84, 0.3, 0.38), (0.5, -1.2, 0.34), 0.055, 0.04), M_LINE),
        ]
        return union(*items, k=0.03)
    return scene, maw_mats(1.0, throat=throat)


HEAD = Static(maw_head, (180, 190), 180 / HEAD_S, factor=3, colors=48)
HEAD_STATES = [dict(open_=0.0, throat=0.3, up=0.0), dict(open_=0.45, throat=1.0, up=0.0),
               dict(open_=1.0, throat=1.0, up=0.0), dict(open_=1.0, throat=1.0, up=1.0)]


def gill_sac(swell=0.0, torn=0.0):
    """A gill sac: a translucent lime-teal bladder in a hide collar; torn = a ragged crater."""
    r = 0.62 * (1 + 0.22 * swell)

    def scene(p):
        items = [(sd_ellipsoid(p, (0, 0, -0.05), (0.95, 0.95, 0.28)), M_HIDE)]
        if torn:
            d, m = union(*items)
            d = np.maximum(d, -sd_sphere(p, (0, 0, 0.25), 0.62))
            shards = [(np.cos(a) * 0.6, np.sin(a) * 0.6) for a in (0.3, 1.7, 2.9, 4.4, 5.5)]
            d, m = union((d, m), (sd_ellipsoid(p, (0, 0, -0.1), (0.5, 0.5, 0.1)), M_CABLE),
                         *[(sd_capsule(p, (x, y, 0.05), (x * 1.3, y * 1.3, 0.3), 0.07, 0.02), M_BONE)
                           for x, y in shards])
            return d, m
        items.append((sd_ellipsoid(p, (0, 0, 0.18), (r, r, r * 0.8)), M_SAC))
        return union(*items, k=0.08)
    return scene, maw_mats(1.0, swell=swell)


SAC = Static(gill_sac, 44, 44 / 22.0, factor=4, colors=28)


def implant(state="shut"):
    """A Helix implant: a grey, unmarked hex housing bolted through the plates, cables sunk into
    the hide round it, casing cracked and scorched; shut, sparking, open (hot-amber core) or
    blown (a scorched crater)."""
    hexa = [(np.cos(a) * 0.72, np.sin(a) * 0.72) for a in np.linspace(0, TAU, 6, endpoint=False) + np.pi / 6]
    cover = [(np.cos(a) * 0.5, np.sin(a) * 0.5) for a in np.linspace(0, TAU, 6, endpoint=False) + np.pi / 6]

    def scene(p):
        items = [(sd_ellipsoid(p, (0, 0, -0.12), (1.1, 1.1, 0.3)), M_HIDE)]
        for a in np.linspace(0, TAU, 7, endpoint=False) + 0.3:
            items.append((sd_capsule(p, (np.cos(a) * 0.55, np.sin(a) * 0.55, 0.12),
                                     (np.cos(a) * 1.1, np.sin(a) * 1.1, -0.04), 0.075, 0.05), M_CABLE))
        if state == "blown":
            d, m = union(*items, k=0.03)
            d = np.maximum(d, -sd_sphere(p, (0, 0, 0.3), 0.66))
            d, m = union((d, m), (sd_ellipsoid(p, (0, 0, -0.12), (0.55, 0.55, 0.1)), M_CABLE),
                         (sd_box(p, (0.6, 0.3, 0.1), (0.18, 0.08, 0.06), 0.02), M_GREY),
                         (sd_box(p, (-0.5, -0.45, 0.08), (0.12, 0.2, 0.05), 0.02), M_GREY))
            return d, m
        items.append((sd_plate(p, hexa, 0.22, 0.13, 0.03), M_GREY))
        for a in np.linspace(0, TAU, 6, endpoint=False) + np.pi / 6:
            items.append((sd_sphere(p, (np.cos(a) * 0.62, np.sin(a) * 0.62, 0.36), 0.06), M_GREY))
        items.append((sd_cylinder_z(p, (0, 0, 0.3), 0.42, 0.08), M_CORE))
        if state == "shut":
            items.append((sd_plate(p, cover, 0.42, 0.06, 0.02), M_GREY))
        elif state == "spark":
            half = [(0.07, -0.5), (0.5, -0.25), (0.5, 0.25), (0.07, 0.5)]   # split, ajar
            items.append((sd_plate(p, half, 0.43, 0.05, 0.02), M_GREY))
            items.append((sd_plate(p, [(-x + 0.02, y) for x, y in half[::-1]], 0.47, 0.05, 0.02), M_GREY))
        else:                                            # open: the cover blown aside
            items.append((sd_plate(p - np.array([0.78, 0.42, 0.1]), cover, 0.42, 0.05, 0.02), M_GREY))
        return union(*items, k=0.03)
    core = {"shut": 0.0, "spark": 0.35, "open": 1.0, "blown": 0.0}[state]
    return scene, maw_mats(1.0, core=core)


IMPLANT = Static(implant, 48, 48 / 20.0, factor=4, colors=32)
IMPLANT_STATES = ("shut", "spark", "open", "blown")


# ---- A: the rising maw (a broad two-jawed head pointing up out of the dark)

MAWA_S = 58.0


def maw_a(open_=0.0, core=0.0):
    """Variant A's head seen from straight above as it rises: two broad jaw shells (upper at the
    top of the screen, lower at the bottom) hinged at their outer edges; open, they fold back and
    bare a ring of teeth round the lime throat. Two Helix implants on the lower jaw, teal eyes and
    flank lines on the upper, gill frills at the sides; the neck plunges into the dark below."""
    phi = np.radians(8 + 62 * open_)

    def scene(p):
        q = mirror_x(p)
        items = [
            (np.maximum(sd_ellipsoid(p, (0, 0, -0.85), (1.35, 1.4, 0.8)),
                        -sd_cylinder_z(p, (0, 0, 0.0), 0.8, 0.32)), M_HIDE),
            (sd_cylinder_z(p, (0, 0, -0.36), 0.66, 0.05), M_THROAT),
        ]
        for a in np.linspace(0, TAU, 14, endpoint=False):
            c, s = np.cos(a), np.sin(a)
            items.append((sd_capsule(p, (c * 0.86, s * 0.86, -0.3), (c * 0.6, s * 0.6, -0.12), 0.07, 0.02),
                          M_BONE))
        # gill frills at the sides
        items.append((sd_plate(q, [(1.1, 0.7), (1.75, 0.45), (1.9, -0.1), (1.7, -0.6), (1.1, -0.5)],
                               -0.35, 0.06, 0.03), M_HIDE))
        items.append((sd_capsule(q, (1.25, 0.55, -0.27), (1.8, -0.1, -0.29), 0.05), M_LINE))
        for sgn in (1, -1):                                # sgn 1: upper jaw (screen top)
            hinge = np.array([0.0, sgn * 1.12, -0.25])
            l = rotate_x(p - hinge, sgn * phi) + hinge if sgn > 0 else rotate_x(p - hinge, sgn * phi) + hinge
            lq = mirror_x(l)
            shell = sd_ellipsoid(l, (0, sgn * 0.52, -0.25), (1.18, 0.68, 0.34))
            inner = sd_ellipsoid(l, (0, sgn * 0.52, -0.42), (1.0, 0.55, 0.28))
            items.append((np.maximum(shell, -inner), M_PLATE))
            for x in np.linspace(-0.8, 0.8, 7):
                items.append((sd_capsule(l, (x, sgn * 0.02, -0.42), (x * 0.95, sgn * 0.12, -0.62), 0.05, 0.015),
                              M_BONE))
            if sgn > 0:
                items.append((sd_sphere(lq, (0.55, 0.62, 0.02), 0.09), M_EYE))
                items.append((sd_capsule(lq, (1.05, 0.25, -0.1), (0.7, 0.98, -0.05), 0.05), M_LINE))
                items.append((sd_capsule(l, (0, 0.15, 0.1), (0, 1.0, 0.0), 0.06, 0.03), M_BONE))
            else:
                for ix in (-0.5, 0.5):
                    items.append((sd_box(l, (ix, -0.6, 0.06), (0.2, 0.17, 0.1), 0.03), M_GREY))
                    items.append((sd_cylinder_z(l, (ix, -0.6, 0.15), 0.1, 0.04), M_CORE))
                    items.append((sd_capsule(l, (ix - 0.2, -0.45, 0.05), (ix - 0.42, -0.2, -0.02), 0.04), M_CABLE))
                    items.append((sd_capsule(l, (ix + 0.2, -0.75, 0.05), (ix + 0.38, -0.98, -0.05), 0.04), M_CABLE))
        return union(*items, k=0.03)
    return scene, maw_mats(1.0, throat=0.3 + 0.7 * open_, core=core)


MAWA = Static(maw_a, 230, 230 / MAWA_S, factor=3, colors=48)
MAWA_STATES = [dict(open_=0.0, core=0.0), dict(open_=0.5, core=0.0), dict(open_=1.0, core=0.0),
               dict(open_=1.0, core=1.0), dict(open_=0.0, core=1.0)]


def maw_parts(variant):
    if variant == "a":
        return [MAWA.jobs(MAWA_STATES), IMPLANT.jobs([dict(state=s) for s in IMPLANT_STATES])]
    heads = list(np.linspace(0, TAU, 32, endpoint=False))
    return [HEAD.jobs(HEAD_STATES),
            SAC.jobs([dict(swell=s, torn=0.0) for s in (0.0, 0.5, 1.0)] + [dict(swell=0.0, torn=1.0)]),
            IMPLANT.jobs([dict(state=s) for s in IMPLANT_STATES]),
            SEG.jobs([dict(w=w, line=0.0, flare=0.0) for w in SEG_W], heads),
            SEG.jobs([dict(w=1.0, line=1.0, flare=0.0), dict(w=1.0, line=0.0, flare=1.0)], heads)]


# =========================================================================== cruiser models

CR_S = 40.0                         # px per model unit (the hull is 16 x 5.5 units)
CR_SIZE = (240, 660)
CR_ACCENT = {"a": "b01828", "b": "ece4d0"}       # crimson / ivory secondary accent


def _rep(v, step, lo, hi):
    """Fold a coordinate into repeated cells of ``step`` between lo and hi (cell centre 0)."""
    c = np.clip(np.round((v - lo) / step), 0, np.floor((hi - lo) / step))
    return v - (lo + c * step)


PORTS = {   # gun ports along each flank: (x, y0, y1, step) in model units
    "a": (2.36, -6.0, 4.6, 0.62),
    "b": (2.62, -4.6, 1.6, 0.46),
}


def cruiser_a():
    """Regent wedge: a long faceted wedge with a stepped spine, a bridge tower aft, one row of gun
    ports down each flank, point-defence turrets either side of the spine, gold trim along every
    edge, a gold sunburst emblem on the bow deck, four engines at the stern."""
    hull = [(0.0, 8.0), (0.95, 6.5), (1.95, 4.0), (2.55, 1.0), (2.72, -2.6), (2.55, -6.3),
            (1.95, -7.7), (0.0, -7.85)]
    deck = [(0.0, 6.6), (0.75, 5.4), (1.5, 3.0), (1.9, 0.0), (1.95, -3.0), (1.8, -6.6), (0.0, -7.0)]
    px, py0, py1, pst = PORTS["a"]

    def scene(p):
        q = mirror_x(p)
        roof = lambda x, y: np.clip(1.0 - 0.16 * np.abs(x), 0.4, 1.0)
        d, m = union(
            (sd_plate(q, hull, 0.0, 0.42, 0.04, taper=roof), A_HULL),
            (sd_plate(q, deck, 0.45, 0.2, 0.04), A_FACET),
        )
        # stepped spine and the bridge tower
        d, m = union(
            (d, m),
            (sd_box(p, (0, 3.2, 0.78), (0.42, 1.6, 0.2), 0.04), A_HULL),
            (sd_box(p, (0, 0.2, 0.86), (0.55, 1.2, 0.26), 0.04), A_HULL),
            (sd_box(p, (0, -3.4, 0.95), (0.8, 1.5, 0.34), 0.05), A_FACET),
            (sd_box(p, (0, -3.9, 1.4), (0.55, 0.62, 0.2), 0.04), A_HULL),
            (sd_box(p, (0, -3.5, 1.52), (0.45, 0.07, 0.06), 0.0), A_GLASS),
            (sd_cylinder_y(q, (0.85, -7.85, 0.0), 0.32, 0.18), A_ENGINE),
            (sd_cylinder_y(q, (1.75, -7.55, 0.0), 0.26, 0.18), A_ENGINE),
            (sd_sphere(p, (0, 7.95, 0.1), 0.1), A_RED),
            (sd_sphere(q, (2.7, -2.4, 0.15), 0.08), A_RED),
        )
        # point-defence turrets either side of the spine (repeated)
        tq = q.copy()
        tq[:, 1] = _rep(q[:, 1], 2.2, -5.5, 4.5)
        tur = sd_cylinder_z(tq, (1.15, 0.0, 0.7), 0.24, 0.1)
        bar = sd_box(tq, (1.15, 0.28, 0.76), (0.12, 0.25, 0.04), 0.0)
        d, m = union((d, m), (tur, A_FACET), (bar, A_GUN))
        # gun ports: recessed slots down the flank (repeated)
        gq = q.copy()
        gq[:, 1] = _rep(q[:, 1], pst, py0, py1)
        port = sd_box(gq, (px, 0.0, 0.18), (0.16, 0.17, 0.13), 0.02)
        d = np.maximum(d, -port)
        m = np.where((np.abs(gq[:, 1]) < 0.19) & (np.abs(q[:, 0] - px) < 0.2) & (p[:, 2] < 0.34)
                     & (q[:, 1] > py0 - 0.3) & (q[:, 1] < py1 + 0.3), A_GLASS, m)
        # gold trim along the hull's and the deck's edges, cross bands, the accent stripe
        e_h = np.abs(sd_polygon2(q[:, 0], q[:, 1], hull))
        e_d = np.abs(sd_polygon2(q[:, 0], q[:, 1], deck))
        m = np.where((e_h < 0.09) & (m == A_HULL), A_GOLD, m)
        m = np.where((e_d < 0.07) & (m == A_FACET), A_GOLD, m)
        for yb in (4.6, -1.6, -5.8):
            m = np.where((np.abs(p[:, 1] - yb) < 0.05) & (m == A_FACET), A_GOLD, m)
        m = np.where((np.abs(q[:, 0] - 0.62) < 0.05) & (p[:, 1] > -2.0) & (p[:, 1] < 5.2)
                     & (m == A_FACET), A_GUN, m)
        # the sunburst emblem on the bow deck
        rr = np.hypot(p[:, 0], p[:, 1] - 5.6)
        ang = np.arctan2(p[:, 1] - 5.6, p[:, 0])
        emb = (rr < 0.52) & ((rr > 0.42) | ((np.cos(ang * 8) > 0.55) & (rr < 0.36) & (rr > 0.12))
                             | (rr < 0.09))
        m = np.where(emb & (p[:, 2] > 0.4), A_GOLD, m)
        return d, m
    return scene, cr_mats("a")


def cruiser_b():
    """Forked monitor: a forked ram bow, broad sponsons with three twin-gun casemates a side,
    a row of gold crown fins down the spine, a tiered bridge aft, an ivory accent."""
    hull = [(0.0, 5.7), (0.5, 6.3), (0.75, 8.0), (1.45, 7.9), (2.0, 5.2), (2.2, 3.2), (2.8, 2.2),
            (2.9, -4.8), (2.55, -5.8), (2.1, -6.4), (1.7, -7.9), (0.0, -7.7)]
    deck = [(0.0, 4.6), (1.2, 4.2), (1.6, 2.2), (1.7, -6.0), (0.0, -6.4)]
    px, py0, py1, pst = PORTS["b"]

    def scene(p):
        q = mirror_x(p)
        roof = lambda x, y: np.clip(1.0 - 0.14 * np.abs(x), 0.4, 1.0)
        d, m = union(
            (sd_plate(q, hull, 0.0, 0.4, 0.05, taper=roof), A_HULL),
            (sd_plate(q, deck, 0.42, 0.22, 0.05), A_FACET),
            (sd_box(q, (2.45, -1.3, 0.38), (0.36, 3.2, 0.16), 0.05), A_FACET),
        )
        # three twin casemate guns per sponson, turned outboard
        for y in (1.0, -1.3, -3.6):
            d, m = union(
                (d, m),
                (sd_cylinder_z(q, (2.2, y, 0.62), 0.42, 0.14), A_HULL),
                (sd_box(q, (2.75, y + 0.13, 0.66), (0.42, 0.07, 0.06), 0.0), A_GUN),
                (sd_box(q, (2.75, y - 0.13, 0.66), (0.42, 0.07, 0.06), 0.0), A_GUN),
            )
        # tiered bridge and the crown fins (gold blades down the spine)
        d, m = union(
            (d, m),
            (sd_box(p, (0, -4.6, 0.86), (0.95, 1.0, 0.25), 0.05), A_FACET),
            (sd_box(p, (0, -4.9, 1.2), (0.6, 0.55, 0.18), 0.04), A_HULL),
            (sd_box(p, (0, -4.5, 1.3), (0.5, 0.06, 0.05), 0.0), A_GLASS),
            (sd_cylinder_y(q, (0.7, -7.75, 0.0), 0.3, 0.18), A_ENGINE),
            (sd_cylinder_y(q, (1.55, -7.75, 0.0), 0.28, 0.18), A_ENGINE),
            (sd_sphere(q, (1.1, 7.95, 0.1), 0.09), A_RED),
            (sd_sphere(q, (2.88, -4.6, 0.15), 0.08), A_RED),
        )
        fq = p.copy()
        fq[:, 1] = _rep(p[:, 1], 0.9, -2.8, 3.6)
        fin = sd_box(fq, (0, 0, 0.84), (0.36, 0.09, 0.2), 0.02)
        d, m = union((d, m), (fin, A_GOLD))
        # point defence: small turrets on the deck edge (repeated)
        tq = q.copy()
        tq[:, 1] = _rep(q[:, 1], 2.4, -5.4, 4.2)
        d, m = union((d, m), (sd_cylinder_z(tq, (1.35, 0.0, 0.66), 0.2, 0.08), A_FACET),
                     (sd_box(tq, (1.35, 0.25, 0.72), (0.1, 0.22, 0.035), 0.0), A_GUN))
        # gun ports in the sponson's flank
        gq = q.copy()
        gq[:, 1] = _rep(q[:, 1], pst, py0, py1)
        m = np.where((np.abs(gq[:, 1]) < 0.15) & (np.abs(q[:, 0] - px) < 0.11) & (p[:, 2] > 0.4)
                     & (q[:, 1] > py0 - 0.3) & (q[:, 1] < py1 + 0.3) & (m == A_FACET), A_GLASS, m)
        e_h = np.abs(sd_polygon2(q[:, 0], q[:, 1], hull))
        e_d = np.abs(sd_polygon2(q[:, 0], q[:, 1], deck))
        m = np.where((e_h < 0.09) & (m == A_HULL), A_GOLD, m)
        m = np.where((e_d < 0.07) & (m == A_FACET), A_GOLD, m)
        m = np.where((np.abs(q[:, 0] - 0.95) < 0.07) & (p[:, 1] > -3.0) & (p[:, 1] < 4.0)
                     & (m == A_FACET), A_GUN, m)
        rr = np.hypot(p[:, 0], p[:, 1] - 3.6)
        ang = np.arctan2(p[:, 1] - 3.6, p[:, 0])
        emb = (rr < 0.5) & ((rr > 0.4) | ((np.cos(ang * 8) > 0.55) & (rr < 0.34) & (rr > 0.12)) | (rr < 0.08))
        m = np.where(emb & (p[:, 2] > 0.4) & (m != A_GOLD), A_GOLD, m)
        return d, m
    return scene, cr_mats("b")


def cr_mats(v, ports=0.0):
    mats = em.asc_mats(1.0)
    acc = hx(CR_ACCENT[v])
    mats[A_GUN] = Material(acc, metal=0.6, shininess=100, spec=1.0)
    mats[A_HULL] = replace(mats[A_HULL], albedo=hx("1a1622"))
    mats[A_FACET] = replace(mats[A_FACET], albedo=hx("26202e"))
    mats[A_GLASS] = Material((0.06, 0.03, 0.02), emission=tuple(AMBER_F * (0.18 + 1.2 * ports)))
    return mats


def hull_code(sp, v):
    """The Helix hull code stencilled in gold down both sides of the bow (placeholder text:
    the wording is Level 27's)."""
    code = "HX-427"
    tw = raster.text_width(code)
    lay = Image.new("RGBA", (tw + 2, 9), (0, 0, 0, 0))
    raster.draw_text(lay, 1, 1, code, (255, 200, 70))
    cx, cy = CR_SIZE[0] / 2, CR_SIZE[1] / 2
    y = {"a": 4.2, "b": 3.0}[v]
    x = {"a": 1.25, "b": 1.55}[v]
    for sgn in (1, -1):
        r = lay.rotate(-90 if sgn > 0 else 90, expand=True)
        a = np.array(sp)
        sprite.paste_center(sp, r, cx + sgn * x * CR_S, cy - y * CR_S)
        b = np.array(sp)
        b[..., 3] = a[..., 3]          # only on the hull
        sp = Image.fromarray(b, "RGBA")
    return sp


def cruiser_post(v):
    return lambda sp: e4.rim(hull_code(sp, v))


CRUISER = {v: Static(fn, CR_SIZE, CR_SIZE[0] / CR_S, factor=2, colors=64, post=cruiser_post(v), twin=False)
           for v, fn in (("a", cruiser_a), ("b", cruiser_b))}


def port_xy(v, side):
    """Screen offsets of a cruiser's inner-flank gun ports (side +1: the ports at +x)."""
    px, y0, y1, st = PORTS[v]
    xo = px
    ys = np.arange(y0, y1 + 1e-6, st)
    return [(side * xo * CR_S, -y * CR_S) for y in ys]


# =========================================================================== part preview

def preview():
    """A quick contact sheet of every model state (for iterating, written to argv[2]);
    argv[3] = "cr" adds the cruisers."""
    for v in "ab":
        prerender(maw_parts(v))
    cr = len(sys.argv) > 3
    if cr:
        prerender([CRUISER[v].jobs([{}]) for v in "ab"])
    rows = [
        cells([HEAD.get(**s) for s in HEAD_STATES], (180, 190), 1, bg=(30, 40, 50)),
        cells([SAC.get(swell=s, torn=0.0) for s in (0.0, 0.5, 1.0)] + [SAC.get(swell=0.0, torn=1.0)]
              + [IMPLANT.get(state=s) for s in IMPLANT_STATES], 48, 2, bg=(30, 40, 50)),
        cells([SEG.get(h, w=w, line=0.0, flare=0.0) for h in (0, 0.6, DOWN, 2.5) for w in (1.0, 0.5)]
              + [SEG.get(0.0, w=1.0, line=1.0, flare=0.0), SEG.get(0.0, w=1.0, line=0.0, flare=1.0)],
              80, 1, bg=(30, 40, 50)),
        cells([MAWA.get(**s) for s in MAWA_STATES], 230, 1, bg=(30, 40, 50)),
    ]
    if cr:
        rows.append(cells([CRUISER["a"].get(), CRUISER["b"].get()], CR_SIZE, 1, bg=(200, 210, 220)))
    W = max(r.width for r in rows)
    H = sum(r.height + 8 for r in rows)
    img = Image.new("RGBA", (W, H), raster.SHEET_BG)
    y = 0
    for r in rows:
        img.alpha_composite(r, (0, y))
        y += r.height + 8
    img.convert("RGB").save(sys.argv[2])
    print("preview", sys.argv[2])




def sprite_cache(path, load=True):
    """Keep the rendered sprites between runs (``BR48_CACHE``: a pickle in a scratch dir; delete
    it after changing a model)."""
    import pickle
    owners = {"head": HEAD, "sac": SAC, "implant": IMPLANT, "mawa": MAWA, "seg": SEG.full,
              "segg": SEG.glo, "cra": CRUISER["a"], "crb": CRUISER["b"]}
    p = Path(path)
    if load:
        if p.exists():
            data = pickle.loads(p.read_bytes())
            for k, o in owners.items():
                o.cache.update(data.get(k, {}))
        return
    p.write_bytes(pickle.dumps({k: o.cache for k, o in owners.items()}))


# =========================================================================== the vent arena

class Vent:
    """L28's arena floor: the hatchery vent's caldera at (240, 330) lighting the arena from
    below, Vrell egg clusters glowing round it (round 48 Q2 a: L28 lit by the vent and the
    hatchery's bioluminescence), marine snow. ``frame(t, px, py, dark)``: ``dark`` 1 = the
    dive's near-darkness (the vent gutters, only the headlight and the glows)."""

    VX, VY, VR = 240, 330, 70

    def __init__(self, seed=48):
        rng = np.random.default_rng(seed)
        yy, xx = np.mgrid[0:FH, 0:FW].astype(np.float64)
        self.yy, self.xx = yy, xx
        r = np.hypot(xx - self.VX, (yy - self.VY) * 1.1)
        self.r = r
        hgt = raster.fbm(FW, FH, 80, seed, octaves=4, period=False) * 0.55
        ridge = 1 - np.abs(2 * raster.fbm(FW, FH, 46, seed + 1, octaves=3, period=False) - 1)
        hgt += 0.3 * ridge ** 3
        hgt += 0.55 * np.exp(-((r - self.VR) / 15) ** 2) - 0.45 * np.clip(1 - r / self.VR, 0, 1)
        for _ in range(9):                                       # mineral chimneys round the rim
            a, rr = rng.uniform(0, TAU), rng.uniform(self.VR * 1.2, self.VR * 2.3)
            cx, cy = self.VX + np.cos(a) * rr, self.VY + np.sin(a) * rr * 0.9
            d = np.hypot(xx - cx, yy - cy) / rng.uniform(6, 12)
            hgt += np.sqrt(np.clip(1 - d * d, 0, 1)) * 0.35
        shade = e3._hillshade(hgt * 70, 0.6)
        t = (hgt - hgt.min()) / (np.ptp(hgt) + 1e-9)
        stops = [(0.0, (5, 12, 16)), (0.4, (16, 30, 34)), (0.7, (32, 50, 52)), (1.0, (66, 84, 80))]
        col = raster.ramp(stops, t) * shade[..., None]
        crust = np.exp(-((r - self.VR * 0.75) / 26) ** 2)[..., None]
        col = col * (1 - 0.45 * crust) + np.array((96, 52, 30)) * 0.45 * crust
        img = Image.fromarray(np.clip(col, 0, 255).astype(np.uint8), "RGB")
        img = img.filter(ImageFilter.GaussianBlur(1.0)).quantize(18, dither=Image.Dither.NONE)
        self.base = np.array(img.convert("RGB")).astype(np.float64)
        # the hatchery: egg clusters (dark translucent shells, glow cores added per frame)
        d = ImageDraw.Draw(img := img.convert("RGBA"))
        self.eggs = []
        for _ in range(17):
            a = rng.uniform(0, TAU)
            rr = rng.uniform(self.VR * 1.25, self.VR * 3.4)
            cx, cy = self.VX + np.cos(a) * rr, self.VY + np.sin(a) * rr * 0.95
            if not (30 < cx < FW - 30 and 60 < cy < FH - 30):
                continue
            hue = LIME if rng.uniform() < 0.55 else TEAL
            for _ in range(int(rng.integers(3, 7))):
                ex, ey = cx + rng.normal(0, 7), cy + rng.normal(0, 6)
                er = rng.uniform(2.5, 4.5)
                d.ellipse([ex - er, ey - er, ex + er, ey + er], fill=(22, 40, 34, 255),
                          outline=(60, 90, 70, 255))
                self.eggs.append((ex, ey, er, hue, rng.uniform(0, TAU)))
        self.base = np.array(img.convert("RGB")).astype(np.float64)
        self.vent_g = np.exp(-(r ** 2) / (2 * (self.VR * 1.7) ** 2))
        self.throat = np.exp(-(r ** 2) / (2 * (self.VR * 0.32) ** 2))
        glo = np.zeros((FH, FW, 3))
        eg = np.zeros((FH, FW))
        for ex, ey, er, hue, ph in self.eggs:
            x0, x1, y0, y1 = int(ex - 14), int(ex + 15), int(ey - 14), int(ey + 15)
            sl = (slice(max(0, y0), min(FH, y1)), slice(max(0, x0), min(FW, x1)))
            g = np.exp(-((xx[sl] - ex) ** 2 + (yy[sl] - ey) ** 2) / (2 * (er * 0.9) ** 2))
            glo[sl] += g[..., None] * np.array(hue) * 0.38
            eg[sl] += np.exp(-((xx[sl] - ex) ** 2 + (yy[sl] - ey) ** 2) / (2 * 12.0 ** 2)) * 0.12
        self.egg_glow, self.egg_light = glo, eg
        self.snow = [(rng.uniform(0, FW), rng.uniform(0, FH), rng.uniform(5, 18), rng.uniform(0, TAU),
                      rng.uniform(0.4, 1.0)) for _ in range(80)]
        self.lm = np.ones((FH, FW))

    def light(self, x, y):
        x, y = float(np.clip(x, 0, FW - 1)), float(np.clip(y, 0, FH - 1))
        return round(float(self.lm[int(y), int(x)]) * 20) / 20

    def frame(self, t, px, py, dark=0.0):
        xx, yy = self.xx, self.yy
        vk = 1.0 - 0.88 * dark
        pulse = 0.85 + 0.15 * np.sin(t * 2.1)
        dx, dy = xx - px, yy - py
        halo = np.exp(-(dx * dx + dy * dy) / (2 * 60 ** 2))
        ahead = np.maximum(0, -dy)
        cone = np.exp(-(dx / (24 + 0.45 * ahead)) ** 2) * (dy < 8) * np.exp(-ahead / 360)
        beam = np.maximum(halo * 0.7, cone)
        amb = 0.24 * (1 - dark) + 0.05 * dark
        lm = amb + 0.95 * self.vent_g * vk * pulse + self.egg_light * (1 - 0.4 * dark) \
            + beam * (0.15 + 0.85 * dark)
        self.lm = np.clip(lm, 0, 1.25)
        k = np.round(self.lm * 10) / 10
        rgb = self.base * k[..., None]
        rgb += (self.throat * vk * pulse)[..., None] * np.array((255, 130, 50)) * 1.1
        rgb += (self.vent_g * vk * pulse * 0.16)[..., None] * np.array((200, 80, 20))
        eg = 0.7 + 0.3 * np.sin(t * 1.7)
        rgb += self.egg_glow * eg * (1 - 0.35 * dark)
        rgb += (0.12 * cone * (0.3 + 0.7 * dark))[..., None] * np.array((150, 200, 210))
        img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
        d = ImageDraw.Draw(img)
        for x0, y0, v, ph, b in self.snow:
            y = (y0 + t * v) % FH
            x = (x0 + 5 * np.sin(t * 0.7 + ph)) % FW
            lv = min(1.0, self.lm[int(y), int(x)])
            c = int(50 + 150 * b * lv * 0.6)
            d.point((x, y), fill=(c, min(255, c + 25), min(255, c + 30), 255))
        return img


def put(img, arena, owner, x, y, sp=None, gl=None, depth=0.0, flash_on=False, floor=0.0, **kw):
    """Paste a part lit by the arena's light map (its glow stays bright), sunk ``depth``;
    ``floor``: the least light (a boss stays readable away from the vent)."""
    sp = sp if sp is not None else owner.get(**kw)
    gl = gl if gl is not None else owner.glow(**kw)
    im = lit(sp, max(floor, arena.light(x, y)), gl)
    if flash_on:
        im = flash(im)
    sprite.paste_center(img, deep(im, depth), x, y)


# =========================================================================== B: the coil

COIL_CTRL = [(240, 42), (205, 32), (150, 46), (105, 56), (62, 50), (30, 76), (18, 135), (18, 250),
             (18, 370), (18, 478), (32, 520), (90, 537), (240, 538), (390, 537), (448, 520),
             (462, 478), (462, 370), (462, 250), (462, 135), (452, 74), (424, 40), (388, 24)]
N_SEG = 31


def _polyline():
    P = np.array(COIL_CTRL, dtype=np.float64)
    tt = np.concatenate([[0], np.cumsum(np.linalg.norm(np.diff(P, axis=0), axis=1))])
    path = rig.catmull_rom(P, tt)
    us = np.linspace(0, tt[-1] - 1e-6, 4000)
    pts = np.array([path(u) for u in us])
    s = np.concatenate([[0], np.cumsum(np.linalg.norm(np.diff(pts, axis=0), axis=1))])
    return pts, s


_PTS, _S = _polyline()
SEG_SP = (_S[-1] - 30.0) / (N_SEG - 1)          # ~56 px apart


def coil_at(s):
    """Position, heading toward the head and inward normal at arc length ``s``."""
    i = int(np.clip(np.searchsorted(_S, s), 1, len(_S) - 2))
    a, b = _PTS[i - 1], _PTS[i + 1]
    tx, ty = (b - a) / (np.linalg.norm(b - a) + 1e-9)
    return _PTS[i], float(np.arctan2(-ty, -tx)), np.array((ty, -tx))


def seg_w(i):
    return SEG_W[0] if i < 22 else SEG_W[min(3, 1 + (i - 22) // 3)]


HEAD_XY = (240, 95)
SAC_OFF = [(-62, -15), (62, -15), (-50, 33), (50, 33)]
THROAT = (240, 135)
IMPLANT_XY = [(110, 60), (40, 300), (440, 300)]

# the GIF's timeline (seconds), compressed from the spec's ~130 s fight
T_P1, T_P2, T_DIVE, T_SH, T_LUNGE, T_RET, T_P3, T_END = 2.5, 6.0, 10.0, 12.2, 14.0, 15.4, 16.4, 20.0
SAC_RINGS = [(2.6, 0), (3.5, 1), (4.4, 3), (5.3, 1), (6.2, 3), (8.0, 0)]     # (swell start, sac)
SAC_KILL = (2, 4.7, 5.4)                       # sac 2 hit from 4.7, bursts at 5.4
JAW = [(3.6, 4.2, 5.9, "fan"), (6.0, 6.8, 7.4, "wall"), (8.2, 9.0, 9.6, "wall")]   # glow, fire, shut
SWEEP = (6.6, "left")                          # tell start; the swing 1.2 s later
SHED = (11, 5.0)                               # segment, flare start (the eel slips out 1.0 s later)
IMPL = [(0, 16.6, 17.6, 19.4), (1, 19.0, 20.0, 22.0)]   # implant, tell, open, shut
SHIP_B = rig.catmull_rom(
    [(240, 475), (225, 470), (262, 462), (195, 472), (190, 476), (225, 470), (160, 465), (150, 465),
     (290, 468), (320, 470), (300, 475), (270, 470), (262, 470), (262, 470), (355, 470), (360, 468),
     (300, 470), (240, 470), (205, 468), (230, 472), (240, 470)],
    [0, 2.5, 3.4, 4.3, 5.2, 6.0, 6.8, 7.4, 8.1, 9.2, 10.2, 11.5, 12.2, 13.2, 13.9, 15.0, 16.4, 17.5,
     18.5, 19.3, 20.0])


def coil_layout(t):
    """Every segment's (x, y, heading, w, depth, line, flare) at time t (tail first)."""
    out = []
    dive = win(t, T_DIVE, T_DIVE + 1.4)
    back = win(t, T_RET, T_RET + 1.0)
    sweep_t = t - SWEEP[0] - 1.2
    swing = 0.0
    if sweep_t > 0:
        swing = ease(sweep_t / 0.5) if sweep_t < 1.3 else 1.0 - ease((sweep_t - 1.3) / 0.8)
    sy = float(np.clip(SHIP_B(SWEEP[0])[1], 160, 420))
    tell = SWEEP[0] <= t < SWEEP[0] + 1.2 + 2.1
    for i in range(N_SEG - 1, -1, -1):
        s = 30.0 + i * SEG_SP
        (x, y), h, n = coil_at(s)
        u = i / (N_SEG - 1)
        depth = 1.0 - win(t, 0.2 + 1.2 * (1 - u), 1.2 + 1.2 * (1 - u))
        line = flare = 0.0
        if x < 60 and 100 < y < 500:                    # the left edge's bight
            b = max(0.0, np.cos(min(np.pi / 2, abs(y - sy) / 140 * np.pi / 2))) ** 2
            if b > 0.05:
                x, y = x + n[0] * 110 * b * swing, y + n[1] * 110 * b * swing
                line = 1.0 if (tell and b > 0.3) else 0.0
        if i == SHED[0] and SHED[1] <= t < SHED[1] + 1.0:
            flare = 1.0
        if t >= T_DIVE:                                 # off the edges into the dark ...
            x, y = x - n[0] * 70 * dive, y - n[1] * 70 * dive
            depth = max(depth, dive)
            if t >= T_RET:                              # ... and back round the edges
                x, y = x + n[0] * 70 * back, y + n[1] * 70 * back
                depth = 1.0 - back
        out.append((x, y, h, seg_w(i), depth, line, flare))
    return out


def with_fillers(segs):
    """Fill the gaps a sweep's bight opens between neighbouring segments (a drawn half-step)."""
    segs = list(segs)
    for k in range(1, len(segs) - 1):                  # headings follow the (displaced) chain
        p, n = segs[k - 1], segs[k + 1]
        segs[k] = segs[k][:2] + (float(np.arctan2(n[1] - p[1], n[0] - p[0])),) + segs[k][3:]
    out = []
    for a, b in zip(segs, segs[1:] + [None]):
        out.append(a)
        if b is not None:
            dd = float(np.hypot(b[0] - a[0], b[1] - a[1]))
            if 58 < dd < 200:
                hm = float(np.arctan2(np.sin(a[2]) + np.sin(b[2]), np.cos(a[2]) + np.cos(b[2])))
                out.append(((a[0] + b[0]) / 2, (a[1] + b[1]) / 2, hm, min(a[3], b[3]), max(a[4], b[4]),
                            max(a[5], b[5]), 0.0))
    return out


def column_layout(t, cx):
    """The lunge: the head races up the column, the body streams up behind it and stays."""
    if not T_LUNGE <= t < T_RET + 1.0:
        return None, []
    u = min(1.0, (t - T_LUNGE) / 0.6)
    hy = 640 - 1100 * u
    sink = win(t, T_RET, T_RET + 1.0)
    segs = []
    for i in range(N_SEG - 1, -1, -1):
        y = hy + 70 + SEG_SP * i
        if -40 < y < FH + 40:
            segs.append((cx, y, UP, seg_w(i), sink, 0.0, 0.0))
    return (cx, hy, sink), segs


def draw_eel(img, arena, t, t0, x0, y0, n=8):
    """A shed Eel Swarm (8 segments) swimming across on a snake path (2-D stand-in)."""
    if t < t0:
        return
    d = ImageDraw.Draw(img)
    for j in range(n - 1, -1, -1):
        tt = t - t0 - j * 0.14
        if tt < 0:
            continue
        x = x0 + 70 * tt
        y = y0 + 26 * np.sin(2.4 * tt) - 10 * tt
        if x > FW + 20:
            continue
        k = max(0.35, arena.light(x, y))
        r = 7 if j == 0 else 6 - j * 0.35
        body = tuple(int(c * k) for c in (150, 64, 40))
        d.ellipse([x - r, y - r * 0.75, x + r, y + r * 0.75], fill=body + (255,), outline=(20, 8, 6, 255))
        d.point((x + 1, y - 2), fill=LIME + (255,))
        d.point((x + 1, y + 2), fill=LIME + (255,))


def maw_b_fight():
    arena = Vent()
    shots = Shots(water=True)
    path = SHIP_B
    for ts, sac in SAC_RINGS:
        if sac == SAC_KILL[0] and ts > SAC_KILL[2]:
            continue
        sx, sy = HEAD_XY[0] + SAC_OFF[sac][0], HEAD_XY[1] + SAC_OFF[sac][1]
        shots.ring(ts + 0.6, sx, sy, 10, 100, "teal", 4, phase=ts)
    for tg, tf, tc, kind in JAW:
        px, py = path(tf)
        aim = np.arctan2(py - THROAT[1], px - THROAT[0])
        if kind == "fan":
            shots.fan(tf, *THROAT, aim, 5, np.radians(40), 130, "violet", 6)
        else:
            arrive = tf + (470 - THROAT[1]) / (110 * WATER_SLOW)
            gx = path(arrive)[0]
            ga = np.arctan2(470 - THROAT[1], gx - THROAT[0])
            angs = np.linspace(-np.radians(75), np.radians(75), 17) + DOWN
            gap = int(np.argmin(np.abs(angs - ga)))
            shots.fan(tf, *THROAT, DOWN, 17, np.radians(150), 110, "teal", 4, skip=(gap,))
    for imp, tt, to, tc in IMPL:
        ix, iy = IMPLANT_XY[imp]
        for b in (to + 0.6, to + 1.6):
            if b < min(tc, T_END):
                px, py = path(b)
                for j in range(3):
                    shots.fire(b + j * 0.12, ix, iy, px, py, 140, "bolt")
    col_x = float(np.clip(path(T_SH)[0], 95, 385))
    frames = []
    for t in times(T_END):
        px, py = path(t)
        dark = win(t, T_DIVE + 0.2, T_DIVE + 1.4) * (1 - win(t, T_RET, T_RET + 1.0))
        img = arena.frame(t, px, py, dark)
        # the lunge's shadow column
        if T_SH <= t < T_LUNGE + 0.3:
            k = win(t, T_SH, T_LUNGE)
            top = FH - FH * k
            lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
            dl = ImageDraw.Draw(lay)
            for yy in range(int(top), FH, 3):
                a = int(235 * min(1.0, (yy - top) / 90 + 0.4))
                dl.rectangle([col_x - 45, yy, col_x + 45, yy + 2], fill=(0, 2, 4, a))
            img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(3)))
            for j in range(4):
                silt(img, col_x, top + 30 + j * 110, t + j * 0.3, 5 + j, n=9, spread=40, rise=min(1.0, 2 * k),
                     col=(120, 150, 140))
        # Eel Swarm shed from the left coil
        (ex, ey), _, _ = coil_at(30.0 + SHED[0] * SEG_SP)
        draw_eel(img, arena, t, SHED[1] + 1.0, ex + 30, ey)
        # the coil (tail first, so the plates shingle toward the head)
        segs = coil_layout(t)
        head, col = column_layout(t, col_x)
        fl_k = 0.6 * (1 - dark) + 0.12
        for x, y, h, w, dp, line, fl in with_fillers(segs) + col:
            if dp >= 0.98:
                continue
            put(img, arena, SEG, x, y, sp=SEG.get(h, w=w, line=line, flare=fl),
                gl=SEG.glow(h, w=w, line=line, flare=fl), depth=dp, floor=fl_k)
            if line and SWEEP[0] <= t < SWEEP[0] + 1.2:
                silt(img, x, y, t, int(y), n=5, spread=20)
            if 0.05 < dp < 0.9 and t < T_P1:
                silt(img, x, y, t, int(x + y), n=4, spread=22, rise=1 - dp)
        # the head and its gill sacs
        hd = 1.0 - win(t, 1.8, 2.5)
        hx_, hy_ = HEAD_XY
        if t >= T_DIVE:
            hd = max(hd, win(t, T_DIVE, T_DIVE + 1.4))
            hy_ -= 35 * win(t, T_DIVE, T_DIVE + 1.4)
            if t >= T_RET:
                hd = 1.0 - win(t, T_RET + 0.2, T_RET + 1.0)
                hy_ = HEAD_XY[1]
        state = HEAD_STATES[0]
        for tg, tf, tc, kind in JAW:
            if tg <= t < tf:
                state = HEAD_STATES[1]
            elif tf <= t < tc:
                state = HEAD_STATES[2]
        if hd < 0.98:
            for i, (ox, oy) in enumerate(SAC_OFF):
                torn = i == SAC_KILL[0] and t >= SAC_KILL[2]
                sw = 0.0
                for ts, sac in SAC_RINGS:
                    if sac == i and ts <= t < ts + 0.6:
                        sw = (t - ts) / 0.6
                kw = dict(swell=0.0, torn=1.0) if torn else dict(swell=round(sw * 2) / 2, torn=0.0)
                hit = i == SAC_KILL[0] and SAC_KILL[1] <= t < SAC_KILL[2] and int(t * FPS) % 3 == 0
                put(img, arena, SAC, hx_ + ox, hy_ + oy, depth=hd, flash_on=hit, floor=fl_k, **kw)
            put(img, arena, HEAD, hx_, hy_, depth=hd, floor=fl_k, **state)
        if SAC_KILL[2] <= t < SAC_KILL[2] + 0.7:
            img = uw_burst(img, HEAD_XY[0] + SAC_OFF[2][0], HEAD_XY[1] + SAC_OFF[2][1],
                           (t - SAC_KILL[2]) / 0.7, 1.2)
        if head is not None and head[1] > -110:
            put(img, arena, HEAD, head[0], head[1], depth=head[2], floor=fl_k, **HEAD_STATES[3])
            if T_LUNGE <= t < T_LUNGE + 0.6:                 # the rush
                d = ImageDraw.Draw(img)
                for k in range(6):
                    xx = head[0] + (k - 2.5) * 16
                    d.line([(xx, head[1] + 90), (xx, min(FH, head[1] + 260))], fill=(150, 220, 210, 90))
        # phase 3: the implants
        if t >= T_P3 - 0.4:
            for imp, (ix, iy) in enumerate(IMPLANT_XY):
                st, pre = "shut", None
                for j, tt, to, tc in IMPL:
                    if j == imp and tt <= t < to:
                        st, pre = "spark", tt
                    elif j == imp and to <= t < tc:
                        st = "open"
                emerge = win(t, T_P3 - 0.4, T_P3 + 0.2)
                put(img, arena, IMPLANT, ix, iy, depth=1 - emerge, floor=fl_k, state=st)
                if st == "spark":
                    sparks(img, ix, iy, t, imp * 7, n=8, r=20)
                    img = raster.add_light(img, ix, iy, 14, AMBER, 0.5 + 0.4 * np.sin(t * 40))
                if st == "open":
                    img = raster.add_light(img, ix, iy, 18, AMBER, 0.55)
                    for j, tt, to, tc in IMPL:
                        if j == imp:
                            for b in (to + 0.6, to + 1.6):
                                if b - 0.4 <= t < b:
                                    arc(img, ix, iy, t, imp)
        shots.draw(img, t)
        # the throat's glow (tells)
        for tg, tf, tc, kind in JAW:
            if tg <= t < tf and hd < 0.5:
                img = raster.add_light(img, THROAT[0], THROAT[1], 26, LIME, 0.5 + 0.5 * (t - tg) / (tf - tg))
        for ts, sac in SAC_RINGS:
            if ts <= t < ts + 0.6 and not (sac == SAC_KILL[0] and t >= SAC_KILL[2]) and hd < 0.5:
                img = raster.add_light(img, HEAD_XY[0] + SAC_OFF[sac][0], HEAD_XY[1] + SAC_OFF[sac][1],
                                       18, LIME, 0.3 + 0.5 * (t - ts) / 0.6)
        ship(img, px, py, t, stop=lambda x, y: 150 if abs(x - 240) < 60 and t < T_DIVE else -10)
        # HUD: bar, captions, phase label
        frac = 1.0
        for (a, b, f0, f1) in ((T_P1, T_P2, 1.0, 0.70), (T_P2, T_DIVE, 0.70, 0.50), (T_DIVE, T_RET, 0.50, 0.46),
                               (T_RET, T_P3, 0.46, 0.30), (T_P3, T_END, 0.30, 0.17)):
            if t >= a:
                frac = f0 + (f1 - f0) * min(1.0, (t - a) / (b - a))
        boss_bar(img, "ABYSSAL MAW", frac * win(t, 1.8, 2.5) if t < T_P1 else frac, alpha=win(t, 1.5, 1.8))
        if 0.2 <= t < 2.4:
            caption(img, "[THE CHOIR SINGS]")
        if 16.7 <= t < 18.6:
            caption(img, "VARGA: THAT'S NOT VRELL. THAT'S MACHINERY IN ITS SPINE.")
        if 18.6 <= t < T_END:
            caption(img, "OKAFOR: THEN HIT THE MACHINERY, LANCER.")
        lab = ("INTRO - THE COIL RISES ROUND THE EDGES, THE HEAD LAST" if t < T_P1 else
               "PHASE 1 - COILED: GILL RINGS, THROAT FAN, A SAC SHOT OFF" if t < T_P2 else
               "PHASE 2 - FAN WALLS WITH A GAP, A COIL SWEEP (LEFT)" if t < T_DIVE else
               "THE DIVE - INTO THE DARK, THE SHADOW COLUMN" if t < T_LUNGE else
               "THE LUNGE - UP THE COLUMN, THE BODY A WALL" if t < T_RET else
               "BACK ROUND THE EDGES, THE LIGHT RETURNS" if t < T_P3 else
               "PHASE 3 - HELIX IMPLANTS SPARK OPEN (HOT AMBER)")
        phase_label(img, lab)
        frames.append(img.convert("RGB"))
    return frames


def maw_b_sheet(frames):
    # the full view: the head end of the arena at 2x with an implant open
    arena = Vent()
    full = Image.new("RGBA", (FW, 200), (0, 0, 0, 0))
    big = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    arena.frame(0.0, 240, 470, 0.0)
    arena.lm[:] = 1.0
    for x, y, h, w, dp, line, fl in coil_layout(3.0)[-9:]:
        put(big, arena, SEG, x, y, sp=SEG.get(h, w=w, line=0.0, flare=0.0), gl=SEG.glow(h, w=w, line=0.0, flare=0.0))
    put(big, arena, IMPLANT, *IMPLANT_XY[0], state="open")
    for i, (ox, oy) in enumerate(SAC_OFF):
        put(big, arena, SAC, HEAD_XY[0] + ox, HEAD_XY[1] + oy, swell=0.5 if i == 1 else 0.0, torn=0.0)
    put(big, arena, HEAD, *HEAD_XY, **HEAD_STATES[2])
    full = sprite.enlarge(big.crop((60, 0, 420, 190)), 1)
    full = full.resize((full.width * 2 // 1, full.height * 2 // 1), Image.NEAREST).crop((0, 0, 720, 360))
    full = full.resize((540, 270), Image.NEAREST)
    k = 540 / 360
    P = lambda x, y: (int((x - 60) * k), int(y * k))
    callouts = [
        (P(240, 128), "LIME THROAT - WEAK SPOT X2 WHILE THE JAW IS OPEN (THE FANS AND THE WALLS)"),
        (P(302, 80), "GILL SACS (4, 150 HP EACH) - SWELL 0.6 S, EXHALE A RING OF 10"),
        (P(225, 70), "TEAL EYES, BONE BROW RIDGES; THE SKULL X0.5"),
        (P(110, 60), "NECK IMPLANT (PHASE 3) - GREY UNMARKED HOUSING, HOT-AMBER CORE OPEN 4 S"),
        (P(150, 46), "COIL - SHINGLED DARK CHITIN OVER VIOLET HIDE, ARMOURED, A WALL"),
        (P(175, 40), "TEAL FLANK LINE - THE COIL READS AS A GLOWING FRAME; LIME = A SWEEP'S TELL"),
    ]
    heads = cells([HEAD.get(**s) for s in HEAD_STATES], (180, 190), 1, bg=(14, 26, 32),
                  labels=["SHUT", "GLOW (TELL)", "JAW OPEN", "LUNGE (UP)"])
    segs = cells([SEG.get(h, w=1.0, line=0.0, flare=0.0) for h in np.linspace(0, TAU, 8, endpoint=False)],
                 80, 1, bg=(14, 26, 32))
    states = cells([SEG.get(0.0, w=w, line=0.0, flare=0.0) for w in SEG_W]
                   + [SEG.get(0.0, w=1.0, line=1.0, flare=0.0), SEG.get(0.0, w=1.0, line=0.0, flare=1.0)],
                   80, 1, bg=(14, 26, 32), labels=["W 1.0", "0.82", "0.66", "0.5", "SWEEP", "SHED"])
    sacs = cells([SAC.get(swell=s, torn=0.0) for s in (0.0, 0.5, 1.0)] + [SAC.get(swell=0.0, torn=1.0)],
                 44, 2, bg=(14, 26, 32), labels=["IDLE", "SWELL", "FULL", "TORN"])
    imps = cells([IMPLANT.get(state=s) for s in IMPLANT_STATES], 48, 2, bg=(14, 26, 32),
                 labels=["SHUT", "SPARK", "OPEN", "BLOWN"])
    glows = cells([HEAD.glow(**HEAD_STATES[2]), SEG.glow(0.0, w=1.0, line=0.0, flare=0.0),
                   SAC.glow(swell=1.0, torn=0.0), IMPLANT.glow(state="open")], (180, 190), 1, bg=(0, 0, 0))
    kf = key_frames(frames, [(2.0, "RISING"), (4.4, "PH1"), (8.7, "SWEEP"), (13.4, "SHADOW"),
                             (14.3, "LUNGE"), (17.9, "PH3")], 0.4)
    panels = [("HEAD (FIXED, 180X190): SHUT / THROAT GLOW / OPEN / LUNGE", heads),
              ("COIL SEGMENT - 8 OF 32 ANGLES (64 PX WIDE, 56 APART)", segs),
              ("SEGMENT TAPER AND TELLS", states), ("GILL SAC", sacs), ("HELIX IMPLANT", imps),
              ("GLOW-ONLY TWINS (WHAT THE DIVE'S DARK LEAVES)", glows),
              ("KEY FRAMES FROM THE GIF (0.4X)", kf)]
    notes = ["ABYSSAL MAW B - THE COILED GUARDIAN (ROUND 48 Q9 B, RECOMMENDED; PENDING THE USER'S REVIEW)",
             "A 1,750 PX SERPENT COILED ROUND THE HATCHERY VENT: 31 SEGMENTS ALONG THE ARENA'S EDGES (HALF BEYOND THE",
             "EDGE), THE HEAD AT THE TOP. THE COIL IS ALWAYS THERE: THE DANGER IS THE FRAME MOVING (SWEEPS, THE ONE DIVE).",
             "VRELL ROLE COLOURS: DARK CHITIN PLATES OVER DEEP-VIOLET HIDE, TEAL FLANK LINE; WEAK POINTS LIME (THROAT,",
             "SACS); THE HELIX IMPLANTS GREY AND UNMARKED WITH A HOT-AMBER CORE (THE ASCENDANCY RULE). NOTHING GOLD (Q11 A).",
             "ARENA: THE VENT'S CALDERA LIGHTS IT FROM BELOW, EGG CLUSTERS GLOW (Q2 A). BULLETS DRAWN BRIGHT, X0.7 SPEED.",
             "THE GIF COMPRESSES THE 130 S FIGHT TO 20 S; THE EEL SWARM IS A 2-D STAND-IN (ITS OWN ART IS STEP A-UNITS')."]
    return b6.boss_sheet("ABYSSAL MAW B - COILED GUARDIAN (ACT 4 BOSS, L28)", full, callouts, panels,
                         frames[int(8.9 * FPS)], notes, "THE HEAD END AT 1.5X: HEAD, SACS, NECK IMPLANT, COIL")


# =========================================================================== A: the rising maw

A_BREACH = [(0.6, 2.6, 3.1, 3.35, 4.0, 5.0), (8.4, 10.4, 10.9, 11.15, 11.6, 12.4)]   # shadow, rise, open, snap, sink, gone
A_WALLS = [(4.9, 5.7), (6.7, 7.5)]                 # glow from the deep, fire
A_END = 12.8
SHIP_A = rig.catmull_rom(
    [(240, 470), (240, 470), (236, 450), (330, 452), (340, 460), (300, 470), (250, 466), (225, 470),
     (300, 462), (310, 450), (300, 450), (200, 456), (190, 466), (220, 470)],
    [0, 0.5, 1.6, 2.4, 3.6, 4.6, 5.6, 6.6, 7.6, 8.3, 9.6, 10.3, 11.6, 12.8])


def maw_a_fight():
    arena = Vent(seed=52)
    shots = Shots(water=True)
    path = SHIP_A
    deep_xy = (240, 120)
    for tg, tf in A_WALLS:
        arrive = tf + (470 - deep_xy[1]) / (110 * WATER_SLOW)
        gx = path(arrive)[0]
        ga = np.arctan2(470 - deep_xy[1], gx - deep_xy[0])
        angs = np.linspace(-np.radians(75), np.radians(75), 17) + DOWN
        gap = int(np.argmin(np.abs(angs - ga)))
        shots.fan(tf, *deep_xy, DOWN, 17, np.radians(150), 110, "teal", 4, skip=(gap,))
    spots = [path(b[0]) for b in A_BREACH]
    frames = []
    for t in times(A_END):
        px, py = path(t)
        img = arena.frame(t, px, py, 0.35)
        for (ts, tr, to, tsn, tk, tg), (bx, by) in zip(A_BREACH, spots):
            by = min(by, 440)
            if ts <= t < tr:                                # the shadow grows under the ship
                k = win(t, ts, tr)
                lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
                dl = ImageDraw.Draw(lay)
                for j in range(5):
                    r = (20 + 70 * k) * (1 - j * 0.14)
                    dl.ellipse([bx - r, by - r * 0.85, bx + r, by + r * 0.85], fill=(0, 2, 4, int(60 + 45 * k)))
                img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(4)))
                for j in range(3):
                    silt(img, bx, by, t + j * 0.33, 3 + j, n=8, spread=25 + 45 * k, rise=min(1.0, 1.5 * k),
                         col=(120, 150, 140))
            if tr <= t < tg:
                if t < to:
                    dp, op = 1 - win(t, tr, to), win(t, tr, to)
                elif t < tsn:
                    dp, op = 0.0, 1 - win(t, to, tsn)
                else:
                    dp, op = win(t, tk, tg), 0.0
                core = 1.0 if ts > 5 else 0.0
                st = min(MAWA_STATES[:3] if not core else [MAWA_STATES[4], MAWA_STATES[3]],
                         key=lambda s: abs(s["open_"] - op))
                put(img, arena, MAWA, bx, by, depth=dp, floor=0.55, **st)
                if core and t < tk:
                    for ix in (-29, 29):
                        sparks(img, bx + ix, by + 35, t, int(ix), n=4, r=12)
                if tsn - 0.05 <= t < tsn + 0.5:              # the snap's shock ring
                    s = (t - tsn + 0.05) / 0.55
                    d = ImageDraw.Draw(img)
                    r = 60 + 90 * s
                    d.ellipse([bx - r, by - r, bx + r, by + r], outline=(200, 240, 230, int(200 * (1 - s))), width=3)
            if ts < 5 and tk <= t:
                for sgn in (-1, 1):                          # Eel Swarms peel off its sides
                    tt = t - tk - 0.2
                    if tt > 0:
                        draw_eel_dir(img, arena, tt, bx + sgn * 70, by - 10, sgn)
        for tg, tf in A_WALLS:                               # the maw in the deep, the throat glowing
            if tg - 0.6 <= t < tf + 0.8:
                k = win(t, tg - 0.6, tg) * (1 - win(t, tf + 0.3, tf + 0.8))
                sp = MAWA.get(**MAWA_STATES[2])
                gl = MAWA.glow(**MAWA_STATES[2])
                im = deep(lit(sp, 0.3, gl), 0.72)
                im.putalpha(im.getchannel("A").point(lambda v: int(v * k)))
                sprite.paste_center(img, im, *deep_xy)
                img = raster.add_light(img, deep_xy[0], deep_xy[1], 30, LIME, 0.6 * k * win(t, tg, tf))
        shots.draw(img, t)
        ship(img, px, py, t)
        boss_bar(img, "ABYSSAL MAW", 1.0 - 0.25 * t / A_END)
        if 5.0 <= t < 7.6:
            caption(img, "VARGA: IT'S BELOW US. WATCH THE WATER UNDER YOU.")
        lab = ("THE SHADOW TELL - IT GROWS UNDER THE SHIP" if t < 2.6 else
               "THE JAW-SNAP - UP THROUGH THE PLAY PLANE" if t < 4.0 else
               "IT SINKS, EEL SWARMS PEEL OFF ITS SIDES" if t < 5.0 else
               "FAN WALLS FROM THE DEEP BETWEEN SNAPS" if t < 8.4 else
               "SECOND SNAP - HELIX IMPLANTS ON ITS LOWER JAW")
        phase_label(img, lab)
        frames.append(img.convert("RGB"))
    return frames


def draw_eel_dir(img, arena, tt, x0, y0, sgn, n=8):
    d = ImageDraw.Draw(img)
    for j in range(n - 1, -1, -1):
        u = tt - j * 0.14
        if u < 0:
            continue
        x = x0 + sgn * 80 * u
        y = y0 + 24 * np.sin(2.6 * u) - 18 * u
        if not -20 < x < FW + 20:
            continue
        k = max(0.35, arena.light(x, y))
        r = 7 if j == 0 else 6 - j * 0.35
        body = tuple(int(c * k) for c in (150, 64, 40))
        d.ellipse([x - r, y - r * 0.75, x + r, y + r * 0.75], fill=body + (255,), outline=(20, 8, 6, 255))
        d.point((x, y - 2), fill=LIME + (255,))
        d.point((x, y + 2), fill=LIME + (255,))


def maw_a_sheet(frames):
    full = Image.new("RGBA", (230, 230), (0, 0, 0, 0))
    full.alpha_composite(MAWA.get(**MAWA_STATES[3]))
    full = full.resize((460, 460), Image.NEAREST)
    callouts = [
        ((230, 230), "LIME THROAT - THE WEAK SPOT WHILE THE JAWS ARE OPEN"),
        ((230, 110), "UPPER JAW SHELL - DARK CHITIN, TEAL EYES AND FLANK LINES"),
        ((172, 352), "LOWER JAW (FOLDED BACK) - ITS TWO HELIX IMPLANTS (GREY, HOT-AMBER CORE) FACE UP WHEN IT SNAPS SHUT: SEE SNAPPED"),
        ((410, 230), "GILL FRILLS - VIOLET HIDE, TEAL EDGE"),
        ((300, 160), "A RING OF BONE TEETH ROUND THE GULLET"),
    ]
    rise = cells([deep(MAWA.get(**MAWA_STATES[2]), d) for d in (0.8, 0.5, 0.2, 0.0)], 230, 1,
                 bg=(10, 22, 28), labels=["DEEP", "RISING", "", "THE PLAY PLANE"])
    opens = cells([MAWA.get(**s) for s in MAWA_STATES[:3]], 230, 1, bg=(10, 22, 28),
                  labels=["SNAPPED", "OPENING", "OPEN"])
    imps = cells([IMPLANT.get(state=s) for s in IMPLANT_STATES], 48, 2, bg=(10, 22, 28),
                 labels=["SHUT", "SPARK", "OPEN", "BLOWN"])
    kf = key_frames(frames, [(2.0, "SHADOW"), (3.0, "BREACH"), (3.3, "SNAP"), (4.6, "EELS"), (7.9, "WALL"),
                             (10.8, "IMPLANTS")], 0.4)
    panels = [("RISE STATES (THE SAME SPRITE SUNK)", rise), ("JAW STATES", opens),
              ("HELIX IMPLANT (SHARED WITH B)", imps), ("KEY FRAMES FROM THE GIF (0.4X)", kf)]
    notes = ["ABYSSAL MAW A - THE RISING MAW (ROUND 48 Q9 A, THE ROSTER'S DRAFT; PENDING THE USER'S REVIEW)",
             "HIDDEN IN THE BLACK WATER MOST OF THE FIGHT: A SHADOW GROWS UNDER THE SHIP (2 S), A BROAD TWO-JAWED HEAD",
             "BURSTS UP THROUGH THE PLAY PLANE AND SNAPS (HEAVY), SINKS SHEDDING EEL SWARMS, AND FIRES BIOLUMINESCENT",
             "FAN WALLS FROM THE DEEP BETWEEN SNAPS. ITS HELIX IMPLANTS ARE SEEN ONLY IN THE SNAPS.",
             "WHY B IS RECOMMENDED: A REPEATS THE DUST COLOSSUS'S BREACH UNDER THE SHIP ONE ACT EARLIER (ITS PHASE 3).",
             "SAME ARENA, COLOURS AND IMPLANT AS B; THE EEL SWARMS ARE 2-D STAND-INS."]
    return b6.boss_sheet("ABYSSAL MAW A - RISING MAW (ACT 4 BOSS, L28)", full, callouts, panels,
                         frames[int(3.05 * FPS)], notes, "THE HEAD AT 2X (JAWS OPEN, IMPLANTS LIT)")


# =========================================================================== the cruisers

def ice_field(h, seed=61):
    """Europa's surface from above: pale blue-white ice, chaos blocks and rust-brown lineae,
    pushed back for readability. ``h`` tall (it scrolls)."""
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:FW].astype(np.float64)
    hgt = raster.fbm(FW, h, 70, seed, octaves=5, period=False) * 0.6
    cr = 1 - np.abs(2 * raster.fbm(FW, h, 40, seed + 1, octaves=3, period=False) - 1)
    hgt += 0.35 * cr ** 6
    shade = e3._hillshade(hgt * 40, 0.35)
    t = (hgt - hgt.min()) / (np.ptp(hgt) + 1e-9)
    stops = [(0.0, (118, 136, 150)), (0.4, (150, 168, 180)), (0.75, (176, 190, 200)), (1.0, (200, 210, 218))]
    col = raster.ramp(stops, t) * shade[..., None]
    crack = np.clip((cr - 0.93) * 14, 0, 1)[..., None]
    col = col * (1 - 0.5 * crack) + np.array((90, 110, 130)) * 0.5 * crack
    for _ in range(4):                                  # lineae: rust bands with a pale ridge
        x0, a, ph, f = rng.uniform(40, FW - 40), rng.uniform(30, 90), rng.uniform(0, TAU), rng.uniform(0.004, 0.009)
        cx = x0 + a * np.sin(yy * f + ph) + 0.25 * (yy - h / 2) * rng.uniform(-0.6, 0.6)
        dd = np.abs(xx - cx)
        band = np.exp(-(dd / 7) ** 2)[..., None]
        ridge = np.exp(-(dd / 1.6) ** 2)[..., None]
        col = col * (1 - 0.55 * band) + np.array((150, 96, 66)) * 0.55 * band
        col = col * (1 - 0.5 * ridge) + np.array((230, 220, 210)) * 0.5 * ridge
    img = Image.fromarray(np.clip(col, 0, 255).astype(np.uint8), "RGB")
    img = img.filter(ImageFilter.GaussianBlur(0.8)).quantize(20, dither=Image.Dither.NONE).convert("RGBA")
    return terrain.recede(img, amount=0.3, darken=0.66)


C_RISE = (0.6, 4.4)                     # the cruisers break up through the ice
C_END = 13.0
SCROLL = 32.0                           # px/s: the wing flies up, the ice scrolls down
BROADSIDES = [(4.8, [("L", None)]), (7.6, [("R", None)]), (10.2, [("L", 300), ("R", 470)])]
FLAK = [6.2, 9.4, 11.8]
PD = [(5.2, "L"), (6.4, "R"), (7.7, "L"), (8.9, "R"), (10.0, "L"), (11.4, "R"), (12.4, "L")]
SHIP_C = rig.catmull_rom(
    [(240, 470), (240, 460), (236, 450), (250, 440), (252, 372), (246, 360), (240, 360), (236, 362),
     (246, 458), (250, 456), (236, 410), (240, 404), (250, 420), (240, 430)],
    [0, 1.5, 3.0, 4.8, 5.6, 6.6, 7.4, 7.9, 8.6, 9.6, 10.6, 11.4, 12.2, 13.0])
CX = {"L": 0, "R": FW}                  # hull centre x: half of each hull beyond the edge
C_CY = 300


def cruiser_fight(v):
    ice = ice_field(int(FH + SCROLL * C_END + 20))
    hull = CRUISER[v].get()
    shots = Shots(water=False)
    rng = np.random.default_rng(7)
    path = SHIP_C
    tells = []
    for tb, bands in BROADSIDES:
        for side, by in bands:
            by = path(tb)[1] if by is None else by
            tells.append((tb, side, by))
            x0 = 110 if side == "L" else 370
            dirx = 1 if side == "L" else -1
            for j in range(7):
                yy = by - 28 + 56 * j / 6 + rng.uniform(-3, 3)
                shots.fire(tb + 1.2 + j * 0.05, x0 - dirx * rng.uniform(0, 40), yy,
                           x0 + dirx * 1000, yy, 260, "shell", 5)
    markers = []
    for tf in FLAK:
        px, py = path(tf + 0.6)
        for j, (ox, oy) in enumerate(((-40, -50), (45, -20))):
            mx, my = px + ox, py + oy
            markers.append((tf, mx, my))
            shots.ring(tf + 1.0, mx, my, 8, 120, "flak", 3, phase=0.3 * j)
    for tp, side in PD:
        px, py = path(tp + 0.4)
        sx, sy = (100 if side == "L" else 380), C_CY + (-120 if tp % 2 < 1 else 60)
        for j in range(3):
            shots.fire(tp + j * 0.1, sx, sy, px, py, 170, "pd", 2.5)
    # ice slabs sheeting off the hulls
    slabs = [(rng.uniform(-90, 90), rng.uniform(-300, 300), rng.uniform(6, 16), rng.uniform(0.5, 1.4),
              rng.uniform(0, TAU)) for _ in range(46)]
    frames = []
    hull_l = hull
    for t in times(C_END):
        off = int(SCROLL * (C_END - np.floor(t * 5) / 5))      # the ice steps at 5 Hz (GIF size)
        img = ice.crop((0, off, FW, off + FH)).copy()
        rise = win(t, C_RISE[0] + 1.2, C_RISE[1])
        crack = win(t, C_RISE[0], C_RISE[0] + 1.6)
        d = ImageDraw.Draw(img)
        for side in "LR":
            cx = CX[side]
            # the channel the hull carves: open water from the bow down, jagged floe edges
            if crack > 0:
                water = Image.new("RGBA", img.size, (0, 0, 0, 0))
                dw = ImageDraw.Draw(water)
                if rise > 0:
                    for y in range(0, FH, 4):
                        wy = y + off                                  # the floe edge, fixed to the ice
                        jag = 8 * np.sin(wy * 0.21) + 5 * np.sin(wy * 0.57 + 1.0)
                        top = C_CY - 330 + 10
                        if y > top - 30 * rise:
                            hw = (120 + jag) * min(1.0, rise * 1.2)
                            dw.rectangle([cx - hw, y, cx + hw, y + 3], fill=(14, 34, 50, 255))
                for k in range(9):                                    # cracks spreading along the hull
                    a = k * 0.7 + (0 if side == "L" else 1.3)
                    y0 = C_CY - 300 + k * 70
                    ln = 30 + 70 * crack
                    dw.line([(cx + (115 if side == "L" else -115) * 0.9, y0),
                             (cx + (115 + ln * abs(np.cos(a))) * (1 if side == "L" else -1), y0 + ln * np.sin(a) * 0.6)],
                            fill=(30, 50, 72, int(240 * crack * (1 - win(t, C_RISE[1], C_RISE[1] + 1.5)))), width=3)
                img.alpha_composite(water)
            if rise > 0:
                sp = hull_l
                k = 0.86 + 0.14 * rise
                sp2 = scaled(sp, k)
                a = np.array(sp2).astype(np.float64)
                m = 0.7 * (1 - rise)
                a[..., :3] = a[..., :3] * (1 - m) + np.array((20, 50, 70)) * m
                a[..., 3] *= min(1.0, 0.3 + rise)
                im = Image.fromarray(a.astype(np.uint8), "RGBA")
                sh = sprite.shadow_of(im, opacity=0.5 * rise, blur=2.0)
                sprite.paste_center(img, sh, cx + 9 * rise, C_CY + 13 * rise)
                sprite.paste_center(img, im, cx, C_CY)
                # the gun ports in the band of a broadside's tell glow hot amber
                for tb, s2, by in tells:
                    if s2 == side and tb <= t < tb + 1.4:
                        g = win(t, tb, tb + 0.5) * (1 - win(t, tb + 1.2, tb + 1.4))
                        for ox, oy in port_xy(v, 1 if side == "L" else -1):
                            yy = C_CY + oy
                            if abs(yy - by) < 60:
                                img = raster.add_light(img, cx + ox, yy, 7, AMBER, 1.3 * g)
                # slabs sheeting off
                if t < C_RISE[1] + 1.5:
                    d = ImageDraw.Draw(img)
                    for sx, sy, sz, sp_, ph in slabs:
                        age = t - C_RISE[0] - 1.0 - sp_ * 0.4
                        if 0 < age < 2.4:
                            out = (1 if side == "L" else -1) * (abs(sx) * 0.6 + 40 * age)
                            x = cx + out + np.sign(out) * 30
                            y = C_CY + sy + 20 * age
                            c = int(235 - 50 * age / 2.4)
                            al = int(255 * (1 - age / 2.4))
                            pts = [(x + np.cos(ph + q) * sz * (0.6 + 0.4 * (q % 2)), y + np.sin(ph + q) * sz * 0.7)
                                   for q in range(5)]
                            d.polygon(pts, fill=(c, c + 5 if c < 250 else 255, 255, al), outline=(140, 170, 190, al))
                    if C_RISE[0] + 1.0 < t < C_RISE[1] + 1.2:            # mist
                        mk = 1 - win(t, C_RISE[1], C_RISE[1] + 1.2)
                        lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
                        dl = ImageDraw.Draw(lay)
                        for j in range(10):
                            yy = C_CY - 280 + j * 60 + 10 * np.sin(t + j)
                            xx = cx + (1 if side == "L" else -1) * (120 + 12 * np.sin(j * 1.7 + t))
                            dl.ellipse([xx - 34, yy - 22, xx + 34, yy + 22], fill=(230, 240, 250, int(90 * mk)))
                        img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(8)))
        # broadside tells: tracer lines across the band
        for tb, side, by in tells:
            if tb <= t < tb + 1.2:
                g = 0.5 + 0.5 * np.sin((t - tb) * 18)
                lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
                dl = ImageDraw.Draw(lay)
                dl.rectangle([110, by - 35, 370, by + 35], fill=(255, 120, 30, int(26 + 20 * g)))
                for yy in (by - 22, by, by + 22):
                    for x in range(110, 370, 14):
                        dl.line([(x, yy), (x + 7, yy)], fill=(255, 170, 60, int(120 + 100 * g)), width=1)
                img.alpha_composite(lay)
        for tf, mx, my in markers:                   # flak markers (the boss lob rule)
            if tf <= t < tf + 1.0:
                k = (t - tf) / 1.0
                d = ImageDraw.Draw(img)
                r = 22.5
                for a in np.linspace(0, TAU, 12, endpoint=False) + t * 3:
                    d.arc([mx - r, my - r, mx + r, my + r], np.degrees(a), np.degrees(a) + 15,
                          fill=(255, 90, 50, 255), width=2)
                d.ellipse([mx - 3, my - 3, mx + 3, my + 3], fill=(255, 200, 60, int(255 * k)))
            if tf + 1.0 <= t < tf + 1.4:
                img = raster.add_light(img, mx, my, 16, (255, 170, 80), 1.2 * (1 - (t - tf - 1.0) / 0.4))
        shots.draw(img, t)
        px, py = path(t)
        e3.place(img, e3.player_sprite(), px, py, "air")
        for kk in range(4):
            y = py - 40 - ((t * 420 + kk * 105) % 420)
            if y > -10:
                e3.bolt(img, px - 10, y)
                e3.bolt(img, px + 10, y)
        if 3.0 <= t < 5.6:
            caption(img, "OKAFOR: ...THOSE ARE OURS.", y=8)
        lab = ("BREAKOUT - THE CRUISERS RISE THROUGH THE ICE" if t < C_RISE[1] else
               "THE RUN - BROADSIDE BANDS, FLAK, POINT DEFENCE" if t < 10.2 else
               "TWO BANDS AT ONCE, ONE FREE BAND BETWEEN")
        phase_label(img, lab)
        frames.append(img.convert("RGB"))
    return frames


def cruiser_sheet(v, frames):
    hull = CRUISER[v].get()
    full = hull
    names = {"a": "REGENT WEDGE", "b": "FORKED MONITOR"}
    cx, cy = CR_SIZE[0] / 2, CR_SIZE[1] / 2
    P = lambda x, y: (int(cx + x * CR_S), int(cy - y * CR_S))
    if v == "a":
        callouts = [(P(0, 5.6), "GOLD SUNBURST EMBLEM - THE FACTION'S MARK, OPENLY"),
                    (P(1.25, 4.2), "HELIX HULL CODE IN GOLD DOWN THE BOW (L27'S CODE, HINT 3 PAID OFF; TEXT A PLACEHOLDER)"),
                    (P(2.36, 1.5), "GUN PORTS - ONE ROW DOWN EACH FLANK; GLOW HOT AMBER IN A BROADSIDE'S TELL"),
                    (P(1.15, -1.1), "POINT-DEFENCE TURRETS EITHER SIDE OF THE SPINE (CRIMSON BARRELS)"),
                    (P(0, -3.6), "BRIDGE TOWER, AMBER VISION SLIT"),
                    (P(2.6, -5.0), "GOLD TRIM ON EVERY EDGE; 1 PX GOLD / RED RIM LIGHT"),
                    (P(0.85, -7.8), "FOUR ENGINES (ORANGE-RED THRUST)")]
    else:
        callouts = [(P(1.1, 7.6), "FORKED RAM BOW"),
                    (P(1.55, 3.0), "HELIX HULL CODE IN GOLD (TEXT A PLACEHOLDER)"),
                    (P(0, 3.6), "GOLD SUNBURST EMBLEM"),
                    (P(0, 1.2), "GOLD CROWN FINS DOWN THE SPINE"),
                    (P(2.2, -1.3), "SPONSONS: THREE TWIN CASEMATE GUNS A SIDE, GUN PORTS BETWEEN THEM"),
                    (P(1.35, -3.0), "POINT-DEFENCE TURRETS (IVORY BARRELS)"),
                    (P(0, -4.6), "TIERED BRIDGE")]
    bow = sprite.enlarge(hull.crop((0, 0, CR_SIZE[0], 260)), 2)
    port_ims = []
    for g in (0.0, 1.0):
        im = hull.crop((CR_SIZE[0] - 90, 200, CR_SIZE[0], 420)).copy()
        if g:
            for ox, oy in port_xy(v, 1):
                im = raster.add_light(im, ox + cx - (CR_SIZE[0] - 90), oy + cy - 200, 7, AMBER, 1.3)
        port_ims.append(sprite.enlarge(im, 2))
    ports = cells(port_ims, (180, 440), 1, bg=(160, 176, 190), labels=["PORTS COLD", "BROADSIDE TELL"])
    kf = key_frames(frames, [(1.6, "CRACKS"), (3.2, "RISING"), (5.4, "TELL"), (6.6, "BROADSIDE"),
                             (7.0, "FLAK"), (11.0, "TWO BANDS")], 0.4)
    panels = [("THE BOW AT 2X", bow), ("THE FLANK AT 2X", ports), ("KEY FRAMES FROM THE GIF (0.4X)", kf)]
    acc = {"a": "CRIMSON", "b": "IVORY"}[v]
    notes = [f"ASCENDANCY CRUISER {v.upper()} - {names[v]} (THE L28 REVEAL, ROUND 48 Q10 A; PENDING THE USER'S REVIEW)",
             "THE FACTION'S FIRST BLACK-AND-GOLD HULLS: BLACK HEX-PANELLED ARMOUR, GOLD TRIM, A " + acc + " SECONDARY ACCENT,",
             "THE 1 PX GOLD / RED RIM LIGHT (THE ROUND 04 ASCENDANCY RULE). 220X640 PX, LOW-AIR, INVULNERABLE, NO BAR:",
             "ONE ON EACH FLANK WITH HALF THE HULL BEYOND THE FIELD'S EDGE (INNER EDGE X 110 / 370).",
             "GIF: THEY BREAK UP THROUGH THE ICE BESIDE THE WING (CRACKS, SLABS SHEETING OFF, MIST), CARVE AN OPEN-WATER",
             "CHANNEL AS THEY KEEP PACE, THEN THE RUN: BROADSIDE BANDS (PORTS GLOW, TRACER LINES 1.2 S, 7 SHELLS),",
             "FLAK MARKERS (1 S, RING OF 8) AND POINT-DEFENCE BURSTS. ABOVE WATER: NO X0.7 ON THEIR SHOTS."]
    return b6.boss_sheet(f"ASCENDANCY CRUISER {v.upper()} - {names[v]} (L28 REVEAL SET PIECE)", full, callouts,
                         panels, frames[int(6.7 * FPS)], notes, "THE HULL AT 1X (BOTH FLANKS; IN GAME HALF SHOWS)")


# =========================================================================== main

JOBS = ("maw-a", "maw-b", "cruiser-a", "cruiser-b")


def main(args):
    import os
    todo = [a for a in args if a in JOBS] or list(JOBS)
    cache = os.environ.get("BR48_CACHE")
    if cache:
        sprite_cache(cache, load=True)
    jl = []
    if "maw-a" in todo:
        jl += maw_parts("a")
    if "maw-b" in todo:
        jl += maw_parts("b")
    for v in "ab":
        if f"cruiser-{v}" in todo:
            jl.append(CRUISER[v].jobs([{}]))
    prerender(jl)
    if cache:
        sprite_cache(cache, load=False)
    if "maw-a" in todo:
        fr = maw_a_fight()
        save("abyssal-maw", "a", maw_a_sheet(fr), fr)
    if "maw-b" in todo:
        fr = maw_b_fight()
        save("abyssal-maw", "b", maw_b_sheet(fr), fr)
    for v in "ab":
        if f"cruiser-{v}" in todo:
            fr = cruiser_fight(v)
            save("ascendancy-cruiser", v, cruiser_sheet(v, fr), fr)


if __name__ == "__main__":
    if len(sys.argv) > 2 and sys.argv[1] == "preview":
        preview()
    else:
        main(sys.argv[1:])
