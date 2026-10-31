#!/usr/bin/env python3
"""Concept round 72 - the new Act 6 "Jovian Storm" units, a/b proposals (step ART-R-A, M10 design phase).

Each subject gets a PNG sheet (the source render, the sprites at play size with their states, the
mechanic's key frames, the readability check over its level's brightest and darkest band with the
player for scale, the measured gold and teal shares, a frame from the play field at 1x) and a GIF of it
in the 480x540 play field over its level's bands. Models are built in this file on the frozen ``render``
package (sdf, enemy_rigs, enemy_models, r06/r07_models), with enemies_r60.py's helpers.

The liveries are the gap analysis's Q9 a (pending the user's review in round 72; the enemies README owns
the rule): **regular** = Q11 b's gunmetal (hull 5a606c, facets 464b55) with gold trim, red running lights
and the 1 px gold rim (the Mirror Interceptor); **elite** = the L28 cruisers' gloss black with heavy gold
(35-45 % of the silhouette) and the strong 2 px gold rim (the Shield Pylon, the Halo Platform); **hybrid** =
the regular gunmetal split by Vrell tissue (dark teal-black flesh) with a teal 00FF9A glow in the seams
(the Chimera, the Harrow, the Graft Turret). Each sheet prints the measured gold and teal shares.

Every glow, haze and shadow is smooth (the 2026-10-30 dither audit): float light fields added once (1x),
glow layers kept at >= 32 steps (``smooth_steps``), no ordered dither, the backdrops unquantised. A
hybrid's seam glow is its own emission-only render, added over the sprite at the glow's strength (the HP
share, the healing pulse), as the production frames would layer it.

Outputs (design/..., each <slug>-r72-{a,b}.{png,gif}):
  enemies/air/chimera              a "seams": a Talon in gunmetal with thin glowing tissue cracks / b
                                   "overgrown": wide tissue bands and nodules at the wing roots; scattered
                                   hits let it heal (the pulse, the HP bar's teal edge), focus kills (L36)
  enemies/air/mirror-interceptor   a "crescent" mirror / b "facets" (five flat panels); the flank on its way
                                   in, the bounce (slow magenta bullets, one per 0.15 s), the heat / crack /
                                   broken / regrow states, the flank kill and the silver shards (L37)
  enemies/air/harrow               a "dart and sac" / b "fork": the sac cradled between two prongs; hunt,
                                   the 0.6 s lock tell, the straight ram, the death ring of 8 with the gap on
                                   the ship's bearing, a lightning kill (L38)
  enemies/ground/graft-turret      a "metal first" / b "tissue first" (roots across the deck); the turret, the
                                   exposed root with its pulse and shrinking ring, the regrowth, the stump (L37)
  enemies/ground/shield-pylon      a "crown" with a solid feed beam / b "obelisk" with flowing feed packets;
                                   the dome over a Rail Bunker and two guns, sparks on covered units, the
                                   collapse, the gate's halt and PYLONS n/m (L41)
  enemies/ground/halo-platform     the chosen r07 model (J8) in the elite livery: a "gold trim" / b "gold
                                   decks"; the live arc's band, a turret's lights gold -> red, the speed-ups,
                                   the red barrier dropping at the third loss (L41)
  enemies/concept/heartland-livery-r72-a.png   Q9's comparison: one airframe in the three liveries (and Q9
                                   b / c), the round's units side by side, over L36's cream and brown bands
                                   and L41's ice and citadel deck, gold and teal measured
  player/weapons/plasma-arc        a "wide Tesla": the arc family's jagged bolt drawn wider and hotter / b
                                   "braided": a two-strand plasma rope in cyan; the chain thinning per jump,
                                   L1 (3 targets) and L5 (6), the spark per struck unit (L36)

The Halo Platform's ring renders 128 frames per variant in parallel (cached under $CONCEPT_CACHE, default the
system temp dir, conceptgame-cache/); everything else renders in the main process.
Run: python3 tools/concept/enemies_r72.py [unit ...] [a|b]   (no args = everything)
"""
import os
import sys
import tempfile
import time
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from multiprocessing import get_context
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import enemies_r06 as e6  # noqa: E402
import enemies_r60 as e60  # noqa: E402
import gear_r36 as g36  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import r06_models as m6  # noqa: E402
from render import r07_models as m7  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import (A_ENGINE, A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED, hx)  # noqa: E402
from render.sdf import (Material, mirror_x, rotate_z, sd_box, sd_capsule, sd_cylinder_z,  # noqa: E402
                        sd_ellipsoid, sd_plate, sd_sphere, union)

r5.SUB = "CONCEPT ROUND 72 - 960X540"
FW, FH, FPS, TAU = r5.FW, r5.FH, r5.FPS, 2 * np.pi
DOWN, UP, RIGHT, LEFT = np.pi / 2, -np.pi / 2, 0.0, np.pi
DESIGN = ROOT / "design"
ROUND = "r72"
CACHE = Path(os.environ.get("CONCEPT_CACHE", tempfile.gettempdir())) / "conceptgame-cache"

grid, source_panel = e60.grid, e60.source_panel
times, smooth, light, fade, tint, flash = e60.times, e60.smooth, e60.light, e60.fade, e60.tint, e60.flash
caption, spark, follow, Shots, keyframes = e60.caption, e60.spark, e60.follow, e60.Shots, e60.keyframes


def boom(img, x, y, s, size="small", sparks=(255, 196, 64), n=10):
    """e60.boom, with the explosion's first frames (additive flashes, premultiplied on black) added as light
    instead of pasted (pasted, they show as a black square on a bright backdrop)."""
    if not 0 <= s < 1:
        return img
    fr = e60.EXPL[size][min(11, int(s * 12))]
    a = np.array(fr)
    if a[0, 0, 3] > 0:
        base = np.array(img).astype(np.int32)
        x0, y0 = int(round(x - fr.width / 2)), int(round(y - fr.height / 2))
        xa, ya = max(0, x0), max(0, y0)
        xb, yb = min(img.width, x0 + fr.width), min(img.height, y0 + fr.height)
        if xb > xa and yb > ya:
            g = a[ya - y0:yb - y0, xa - x0:xb - x0].astype(np.int32)
            base[ya:yb, xa:xb, :3] = np.minimum(255, base[ya:yb, xa:xb, :3] + g[..., :3] * g[..., 3:4] // 255)
            img = Image.fromarray(base.astype(np.uint8), "RGBA")
        return _boom_sparks(img, x, y, s, size, sparks, n)
    return e60.boom(img, x, y, s, size, sparks, n)


def _boom_sparks(img, x, y, s, size, sparks, n):
    if sparks:
        d = ImageDraw.Draw(img)
        rr = {"tiny": 14, "small": 26, "medium": 44, "large": 70}[size] * (0.3 + s)
        for k in range(n):
            a = k * TAU / n + 0.3 + 0.2 * np.sin(k * 7.1)
            px, py = x + np.cos(a) * rr, y + np.sin(a) * rr
            d.line([px, py, px - np.cos(a) * 3, py - np.sin(a) * 3], fill=tuple(sparks) + (int(255 * (1 - s)),),
                   width=1)
    return img
gold_share, Turning, emissive, side_warning = e60.gold_share, e60.Turning, e60.emissive, e60.side_warning

TEAL = (0, 255, 154)
TEAL_F = np.array(TEAL) / 255.0
FLESH = "163f3e"                       # the tissue's flesh: the Vrell spawner base 1E5C5A, a step darker
MAGENTA = (255, 60, 200)
GOLD = (255, 196, 64)
A_TISSUE = 7                           # extra material slots
A_MIRROR = 8
GLOW_LEVELS = 48                       # glow layers' steps (>= 32, undithered)


def smooth_steps(values, levels=GLOW_LEVELS):
    """0..1 values posterised to ``levels`` steps without dithering (tools/art/iron_sovereign.py's rule)."""
    return np.round(np.clip(values, 0, 1) * (levels - 1)) / (levels - 1)


# =========================================================================== liveries

def regular_mats(accent="c8202a", glow=1.0, red=1.0, extra=()):
    """Q11 b (Act 5's chosen look, Act 6's regular units): gunmetal hull, charcoal facets, gold, red lights."""
    mats = e60.belt_mats(accent, glow, red)
    mats[A_HULL] = replace(mats[A_HULL], albedo=hx("5a606c"), shininess=90)
    mats[A_FACET] = replace(mats[A_FACET], albedo=hx("464b55"))
    return mats + list(extra)


def elite_mats(accent="c8202a", glow=1.0, red=1.0, extra=()):
    """Q9 a's elite: the L28 cruisers' gloss black (e60.belt_mats) with its gold and bright red lights."""
    return e60.belt_mats(accent, glow, red) + list(extra)


def tissue_mat(tg=1.0, veins=False):
    """The hybrids' tissue: dark teal-black flesh with the teal glow (``tg`` its strength)."""
    return Material(hx(FLESH), metal=0.15, shininess=60, spec=0.7, emission=tuple(TEAL_F * 1.25 * tg),
                    emission_pattern=em._veins(16, 0.7, 0.55) if veins else None)


def mirror_mat():
    return Material((0.86, 0.84, 0.80), metal=1.0, shininess=320, spec=2.2)


def glow_only(mats, keep=(A_TISSUE,)):
    """The emission-only render of the slots in ``keep`` (every other slot black): a glow layer."""
    out = []
    for i, m in enumerate(mats):
        if i in keep:
            out.append(replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None))
        else:
            out.append(Material((0.0, 0.0, 0.0)))
    return out


REG_RIM = e60.belt_rim(False)          # 1 px gold rim on the sun side, red on the far side (Q11 b)
ELITE_RIM = e60.belt_rim(True)         # the strong 2 px gold rim (the elite: never dark-on-dark)


def teal_share(sp):
    """Share of the silhouette lit teal (the hybrids' seam glow; Q9 a asks 5-12 %)."""
    a = np.array(sp).astype(np.float64)
    op = a[..., 3] > 0
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    teal = op & (g > 95) & (g > 1.45 * r + 20) & (b > 0.35 * g) & (b < 1.02 * g)
    return teal.sum() / max(1, op.sum())


def shares(sp, teal=True):
    s = f"GOLD {100 * gold_share(sp):.0f} %"
    return s + (f"  TEAL {100 * teal_share(sp):.0f} %" if teal else "")


def add_glow(sp, layer, k, pulse=None):
    """``sp`` with its emission-only ``layer`` added at strength ``k`` (0..1.6); ``pulse`` (phase 0..1)
    runs a brighter band outward from the centre (the healing pulse)."""
    if layer is None or k <= 0:
        return sp
    a = np.array(sp).astype(np.float64)
    g = np.array(layer.resize(sp.size) if layer.size != sp.size else layer).astype(np.float64)[..., :3]
    kk = np.full(a.shape[:2], float(k))
    if pulse is not None:
        h, w = a.shape[:2]
        yy, xx = np.mgrid[0:h, 0:w]
        r = np.hypot(xx - w / 2 + 0.5, yy - h / 2 + 0.5) / (0.5 * max(w, h))
        kk = kk + 0.9 * np.exp(-((r - pulse * 1.2) / 0.16) ** 2)
    kk = smooth_steps(kk / 2.0) * 2.0
    a[..., :3] = np.clip(a[..., :3] + g * kk[..., None], 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


class Hybrid:
    """A turning hybrid: its sprite set and its seam glow set (emission only)."""

    def __init__(self, make, size, count=16, factor=5, colors=36):
        self.body = Turning(lambda **kw: make(tg=0.0, **kw), size, count, factor=factor, colors=colors, post=REG_RIM)
        self.glow = Turning(lambda **kw: make(only_glow=True, tg=1.0, **kw), size, count, factor=factor, colors=48)

    def get(self, h, k=0.45, pulse=None, **kw):
        return add_glow(self.body.get(h, **kw), self.glow.get(h, **kw), k, pulse)

    def frame(self, i, k=0.45, **kw):
        return add_glow(self.body.frame(i, **kw), self.glow.frame(i, **kw), k)


def seg_dist2(q, segs):
    """2D distance (x, y of model points ``q``) to the nearest of the segments [(x0, y0, x1, y1)]."""
    x, y = q[:, 0], q[:, 1]
    best = np.full(len(q), 9.0)
    for x0, y0, x1, y1 in segs:
        bx, by = x1 - x0, y1 - y0
        h = np.clip(((x - x0) * bx + (y - y0) * by) / (bx * bx + by * by + 1e-9), 0, 1)
        best = np.minimum(best, np.hypot(x - x0 - bx * h, y - y0 - by * h))
    return best


# =========================================================================== Jovian backdrops (smooth, 1x)

L36_STOPS = [(0.0, "3e2418"), (0.22, "6e3a22"), (0.42, "a45a30"), (0.6, "c79052"), (0.8, "dcc092"),
             (1.0, "f2e8d2")]
L38_STOPS = [(0.0, "10141c"), (0.3, "232a38"), (0.5, "3a3a52"), (0.7, "565c74"), (0.88, "7c869c"),
             (1.0, "aeb8cc")]
ICE_STOPS = [(0.0, "1e2834"), (0.3, "34404e"), (0.55, "4c5a6a"), (0.8, "6c7c8e"), (1.0, "96a6b8")]
DECK_STOPS = [(0.0, "7a7058"), (0.4, "b4a888"), (0.75, "d4caac"), (1.0, "ebe4cf")]


def _arr(rgb):
    return Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def _ramp(t, stops):
    return raster.ramp([(k, raster.hexrgb(c)) for k, c in stops], np.clip(t, 0, 1))


def bands_bg(seed, h=FH, period=300.0, stops=L36_STOPS, haze=(232, 226, 214), haze_k=0.1):
    """Level 36: Jupiter's storm bands below, cream / ochre / rust / brown, under a pale haze with the warm
    sun from the top left. The bands run across the screen, so the scroll carries a bright cream band and a
    dark brown one through every frame."""
    w = FW
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    warp = (raster.fbm(w, h, 96, seed, octaves=4, period=False) - 0.5) * 70
    curl = (raster.fbm(w, h, 32, seed + 7, octaves=3, period=False) - 0.5) * 22
    yb = yy + warp + curl + 18 * np.sin(xx / 70 + seed)
    v = 0.5 + 0.5 * np.sin(TAU * yb / period)
    v = 0.15 + 0.85 * v ** 1.15
    tex = raster.fbm(w, h, 20, seed + 3, octaves=4, period=False)
    v = np.clip(v + (tex - 0.5) * 0.22, 0, 1)
    rgb = _ramp(v, stops)
    sun = np.clip(1.0 - (xx / w + (yy % FH) / FH) * 0.35, 0.65, 1.0)
    rgb = rgb * (0.9 + 0.12 * sun[..., None])
    rgb = rgb * (1 - haze_k) + np.array(haze) * haze_k
    return _arr(rgb)


def storm_bg(seed, h=FH):
    """Level 38: the storm layer: slate and purple-grey cloud walls, one lightning-lit cloud flank."""
    w = FW
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    big = raster.fbm(w, h, 128, seed, octaves=5, period=False)
    walls = 0.5 + 0.5 * np.sin(TAU * (yy + (big - 0.5) * 160) / 360.0)
    tex = raster.fbm(w, h, 24, seed + 5, octaves=4, period=False)
    v = np.clip(0.18 + 0.55 * walls ** 2 + (big - 0.5) * 0.5 + (tex - 0.5) * 0.18, 0, 1)
    lit = np.exp(-((xx - 330) / 120) ** 2) * (0.5 + 0.5 * np.sin(TAU * yy / 360.0 + 1.0)) ** 3
    v = np.clip(v + 0.35 * lit, 0, 1)
    return _arr(_ramp(v, L38_STOPS))


def deck_bg(seed, h=FH, burnt=((200, 330),)):
    """Level 37: Aurelia's deck, pale gold-white plating a step paler than the units' gold, dome rims and rails
    over warm cream cloud at the sides; ``burnt`` (y ranges in the tall image) the deck after the turn:
    scorched brown-grey with fire-orange glows and teal growth."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    cloud = _ramp(0.45 + 0.5 * raster.fbm(w, h, 64, seed, octaves=4, period=False), L36_STOPS)
    tex = raster.fbm(w, h, 16, seed + 2, octaves=3, period=False)
    pv = 0.55 + 0.25 * (tex - 0.5) + 0.12 * (((yy // 48) + (xx // 64)) % 2)
    plate = _ramp(pv, DECK_STOPS)
    seams = ((yy % 48) < 1.5) | ((xx % 64) < 1.5)
    plate[seams] *= 0.72
    edge = 40 + 18 * np.sin(yy / 90 + seed)
    on = (xx > edge) & (xx < w - edge)
    rgb = np.where(on[..., None], plate, cloud)
    rail = (np.abs(xx - edge) < 3) | (np.abs(xx - (w - edge)) < 3)
    rgb[rail] = np.array((200, 164, 80)) * 0.9
    shadow = np.clip(1 - np.abs(xx - edge - 6) / 10, 0, 1) * (xx > edge)
    rgb *= (1 - 0.35 * shadow)[..., None]
    for _ in range(int(h / 260) + 1):                        # dome rims (rings on the deck)
        cx, cy, r = rng.uniform(120, 360), rng.uniform(0, h), rng.uniform(40, 70)
        d = np.hypot(xx - cx, yy - cy)
        ring = np.clip(1 - np.abs(d - r) / 4, 0, 1) * on
        rgb = rgb * (1 - 0.3 * ring[..., None]) + np.array((236, 214, 150)) * 0.3 * ring[..., None]
        rgb *= (1 - 0.18 * np.clip(1 - np.abs(d - r - 6) / 5, 0, 1) * on)[..., None]
    for y0, y1 in burnt:
        u = np.clip(np.minimum(yy - y0, y1 - yy) / 30, 0, 1) * on
        scorch = _ramp(0.15 + 0.35 * tex, [(0, "1c1612"), (0.5, "3c3028"), (1, "5c4c40")])
        rgb = rgb * (1 - u[..., None]) + scorch * u[..., None]
        growth = np.clip((raster.fbm(w, h, 28, seed + 9, octaves=3, period=False) - 0.62) * 6, 0, 1) * u
        rgb = rgb * (1 - 0.7 * growth[..., None]) + np.array((22, 92, 84)) * 0.7 * growth[..., None]
        for _ in range(4):
            fx, fy = rng.uniform(80, 400), rng.uniform(y0 + 20, y1 - 20)
            f = np.exp(-((xx - fx) ** 2 + (yy - fy) ** 2) / (2 * 22 ** 2)) * u
            rgb += f[..., None] * np.array((255, 120, 30)) * 0.55
    return _arr(rgb)


def ice_bg(seed, h=FH, deck=(110, 370)):
    """Level 41: dark blue-grey ice, cratered, the low Sun's long shadows; the black citadel deck down the
    middle (``deck`` x range) with red and gold lights."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    hgt = raster.fbm(w, h, 48, seed, octaves=5, period=False)
    for _ in range(int(h / 70)):
        cx, cy, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(8, 30)
        d = np.hypot(xx - cx, yy - cy) / r
        hgt -= 0.25 * np.clip(1 - d, 0, 1) ** 2
        hgt += 0.12 * np.clip(1 - np.abs(d - 1.05) / 0.25, 0, 1)
    gy, gx = np.gradient(hgt)
    shade = np.clip(0.55 + (-gx - gy) * 22, 0.1, 1.2)
    rgb = _ramp(np.clip(0.25 + 0.55 * shade * (0.6 + 0.4 * hgt), 0, 1), ICE_STOPS)
    x0, x1 = deck
    on = (xx > x0) & (xx < x1)
    tex = raster.fbm(w, h, 12, seed + 4, octaves=3, period=False)
    plate = _ramp(0.3 + 0.3 * tex + 0.15 * (((yy // 40) + (xx // 50)) % 2), [(0, "0e0f13"), (0.5, "1d1f26"),
                                                                             (1, "33353f")])
    plate[((yy % 40) < 1.2) | ((xx % 50) < 1.2)] *= 0.6
    rgb = np.where(on[..., None], plate, rgb)
    sh = np.clip(1 - (xx - x1) / 26, 0, 1) * (xx >= x1)              # the deck's long shadow on the ice
    rgb *= (1 - 0.45 * sh)[..., None]
    lip = (np.abs(xx - x0) < 2) | (np.abs(xx - x1) < 2)
    rgb[lip] = (176, 140, 60)
    img = _arr(rgb)
    for y in range(20, h, 80):
        for x in (x0 + 8, x1 - 8):
            light(img, x, y, 3.5, (255, 50, 40), 1.0)
        light(img, (x0 + x1) / 2, y + 40, 3, (255, 196, 90), 0.7)
    return img


def window(bg, w, h, mode="dark"):
    """The darkest (or brightest) w x h window of a backdrop."""
    g = np.array(bg.convert("L")).astype(np.float64)
    ii = np.pad(g.cumsum(0).cumsum(1), ((1, 0), (1, 0)))
    s = ii[h:, w:] - ii[:-h, w:] - ii[h:, :-w] + ii[:-h, :-w]
    idx = np.argmin(s) if mode == "dark" else np.argmax(s)
    y, x = np.unravel_index(idx, s.shape)
    return bg.crop((x, y, x + w, y + h))


def bands_check(items, bg, scales=(1, 2), names=("BRIGHTEST", "DARKEST")):
    """Each sprite beside the player over the brightest and the darkest window of its level's backdrop."""
    pl = e3.player_sprite()
    uw = max(sp.width for sp, _ in items)
    uh = max(sp.height for sp, _ in items)
    w, h = uw + pl.width + 28, max(uh, pl.height) + 16
    cells, labels = [], []
    for sc in scales:
        for mode, nm in zip(("bright", "dark"), names):
            ground = window(bg, w, h, mode)
            for sp, lab in items:
                c = ground.copy()
                sprite.paste_center(c, sp, 8 + uw / 2, h / 2)
                sprite.paste_center(c, pl, w - 8 - pl.width / 2, h / 2)
                cells.append(c.resize((w * sc, h * sc), Image.NEAREST))
                labels.append(f"{nm} {sc}X" + (f": {lab}" if sc > 1 else ""))
    width = max(sum(c.width + 8 for c in cells[:len(cells) // 2]), sum(c.width + 8 for c in cells[len(cells) // 2:]))
    rows = [cells[:len(cells) // 2], cells[len(cells) // 2:]]
    lrows = [labels[:len(labels) // 2], labels[len(labels) // 2:]]
    hgt = sum(max(c.height for c in r) + 14 for r in rows)
    img = Image.new("RGBA", (width, hgt), raster.SHEET_BG)
    y = 0
    for r, lr in zip(rows, lrows):
        x = 0
        for c, lab in zip(r, lr):
            img.alpha_composite(c, (x, y))
            raster.draw_text(img, x, y + c.height + 2, lab, raster.LABEL_DIM)
            x += c.width + 8
        y += max(c.height for c in r) + 14
    return img


class Scroll(e60.Scroll):
    """A tall backdrop scrolling down; ``speed`` may be a function of t (a halt) via ``offset``."""

    def __init__(self, make, seed, dur, speed=30.0, offset=None, **kw):
        self.speed = speed
        self.offset = offset or (lambda t: speed * t)
        self.h = int(FH + self.offset(dur) + 8)
        self.img = make(seed, self.h, **kw)

    def frame(self, t):
        top = self.h - FH - int(round(self.offset(t)))
        return self.img.crop((0, top, FW, top + FH))

    def y(self, y0, t):
        return y0 + int(round(self.offset(t)))


# =========================================================================== shared scene pieces

AIR_SCROLL = 6.0          # the deep cloud layer under an air scene (high altitude: slow; keeps the GIFs small)
SHOT_RED = (255, 84, 40)   # Ascendancy bullets: red-orange ring, dark rim, white core


class EShots(e60.Shots):
    """Straight enemy shots drawn in a ring colour (the Ascendancy's red, the Vrell's teal)."""

    def __init__(self, col=SHOT_RED):
        super().__init__()
        self.col = col

    def draw(self, img, t):
        for t0, x, y, vx, vy, r in self.list:
            if t >= t0:
                bx, by = x + vx * (t - t0), y + vy * (t - t0)
                if -20 < bx < FW + 20 and -20 < by < FH + 20:
                    e3.orb(img, bx, by, r, ring=self.col)


def ship(img, px, py, t, shots=False):
    sprite.paste_center(img, e3.player_sprite(), px, py)


class Fire:
    """The player's forward bolts (two guns 20 px apart, 10 a second, 420 px/s), simulated so a bolt stops
    where it hits: ``step(t, px, py, hit)`` advances them; ``hit(x, y)`` returns True when a bolt is spent
    there (it may record the hit). ``draw`` paints the live bolts."""

    def __init__(self, rate=10.0, speed=420.0, guns=(-10, 10)):
        self.rate, self.speed, self.guns = rate, speed, guns
        self.bolts, self.next = [], 0.0

    def step(self, t, px, py, hit, on=True):
        while on and t >= self.next:
            for g in self.guns:
                self.bolts.append([px + g, py - 26.0])
            self.next += 1.0 / self.rate
        if not on:
            self.next = max(self.next, t)
        keep = []
        for b in self.bolts:
            b[1] -= self.speed / FPS
            if b[1] < -20:
                continue
            if hit(b[0], b[1]):
                continue
            keep.append(b)
        self.bolts = keep

    def draw(self, img):
        for x, y in self.bolts:
            e3.bolt(img, x, y)


def orb(img, x, y, r=4, col=None):
    e3.orb(img, x, y, r, ring=col)


def hp_bar(img, x, y, share, teal_edge=False, w=26):
    """The Targeting computer's small HP bar over a unit (its regrowth edge teal)."""
    d = ImageDraw.Draw(img)
    x0, y0 = int(x - w / 2), int(y)
    d.rectangle([x0 - 1, y0 - 1, x0 + w + 1, y0 + 3], fill=(10, 12, 18, 220))
    fw = int(round(w * np.clip(share, 0, 1)))
    d.rectangle([x0, y0, x0 + fw, y0 + 2], fill=(230, 70, 50, 255))
    if teal_edge and 0 < fw < w:
        d.rectangle([x0 + fw - 2, y0, x0 + fw, y0 + 2], fill=TEAL + (255,))


def flecks(img, x, y, s, n=9, col=TEAL, seed=0):
    """Tissue flecks flung from a hybrid's death (presentation)."""
    if not 0 <= s < 1:
        return img
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(img)
    for k in range(n):
        a = rng.uniform(0, TAU)
        r = (10 + rng.uniform(0, 26)) * (0.2 + s)
        px, py = x + np.cos(a) * r, y + np.sin(a) * r + 14 * s * s
        al = int(255 * (1 - s))
        d.rectangle([px - 1, py - 1, px + 1, py], fill=(30, 110, 100, al))
        d.point((px, py), fill=col + (al,))
    return img


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


SUB = "CONCEPT ROUND 72 - 960X540"


def sheet(title, panels, notes, game_frame, width=1300):
    r5.SUB = SUB                       # importing enemies_r07 (the Halo ring's helpers) resets it
    return r5.build_sheet(title, panels, notes, game_frame, width)


def row(items, size, scale=2, labels=None, bg=None):
    return e60.row(items, size, scale, labels, bg)


UNITS = {}


def unit(name):
    def deco(fn):
        UNITS[name] = fn
        return fn
    return deco


# ===================================================================== 1 Chimera (L36)

CHI_SEAMS = {   # (x0, y0, x1, y1) in the mirrored model frame (x >= 0, nose +y)
    "a": [(0.17, 0.42, 0.24, -0.55), (0.26, 0.02, 0.84, 0.30), (0.52, 0.16, 0.44, -0.18),
          (0.0, 0.18, 0.15, 0.24), (0.0, -0.32, 0.22, -0.38), (0.22, -0.62, 0.40, -0.80)],
    "b": [(0.17, 0.46, 0.25, -0.62), (0.26, 0.0, 0.80, 0.26), (0.55, 0.15, 0.46, -0.16),
          (0.0, 0.16, 0.16, 0.22), (0.0, -0.30, 0.23, -0.36), (0.22, -0.60, 0.40, -0.80)],
}
CHI_NODES = [((0.22, 0.10, 0.04), (0.11, 0.16, 0.08)), ((0.24, -0.42, 0.03), (0.10, 0.14, 0.08)),
             ((0.0, -0.05, 0.11), (0.08, 0.2, 0.07)), ((0.62, 0.20, 0.0), (0.09, 0.07, 0.05))]


def chimera_model(v, only_glow=False, tg=1.0):
    """The Talon airframe (em.talon, +Y forward) in the production Talon's livery (Q11 b gunmetal, the outer
    wing panels and tail fins gold) split by tissue. a "seams": thin cracks (0.03 units) along the wing roots,
    across the wings and the spine, the airframe intact; b "overgrown": the seams twice as wide and wavering,
    tissue nodules bulging at the wing roots, the spine and the wing panels."""
    scene0, _ = em.talon(0.0, 1.0)

    def scene(p):
        d, m = scene0(rotate_z(p, np.pi))
        q = mirror_x(p)
        m = np.where((m == A_FACET) & ((q[:, 0] > 0.5) | (q[:, 1] < -0.45)), A_GOLD, m)
        sd = seg_dist2(q, CHI_SEAMS[v])
        if v == "a":
            w = 0.019
        else:
            w = 0.055 + 0.018 * np.sin(q[:, 1] * 23 + q[:, 0] * 11)
        metal = np.isin(m, [A_HULL, A_FACET, A_GOLD])
        m = np.where((sd < w) & metal, A_TISSUE, m)
        if v == "b":
            parts = [(sd_ellipsoid(q, c, r), A_TISSUE) for c, r in CHI_NODES]
            d, m = union((d, m), *parts, k=0.03)
        return d, m
    mats = regular_mats("c8202a", 1.0, extra=[tissue_mat(tg, veins=(v == "b"))])
    return scene, (glow_only(mats) if only_glow else mats)


CHI_N = 46
chimeras = {v: Hybrid(lambda v=v, **kw: chimera_model(v, **kw), CHI_N) for v in "ab"}
CHI_SCALE = CHI_N / 2.3
CHI_DUR = 10.0
CHI_HP = 40.0
CHI_PATH = rig.catmull_rom([(70, -60), (110, 90), (200, 200), (300, 236), (390, 190), (440, 80), (470, -70)],
                           [0, 1.1, 2.3, 3.5, 4.8, 6.1, 7.4])
CHI_WING = [(0, 0), (-30, -28), (30, -28), (-60, -56), (60, -56)]          # V of five, no leader
CHI_IDLE = 0.45                                                           # the seams' glow at full health


def chi_state(i, t):
    x, y = CHI_PATH(t)
    h = rig.path_heading(CHI_PATH, t)
    lx, ly = CHI_WING[i]
    ox, oy = rig.local_to_screen(-lx / CHI_SCALE, ly / CHI_SCALE, h, CHI_SCALE)
    return x + ox, y + oy, h


def chi_scene(v):
    bg = Scroll(bands_bg, 36, CHI_DUR, AIR_SCROLL)
    H = chimeras[v]
    hp = {i: CHI_HP for i in range(5)}
    last_hit = {i: -9.0 for i in range(5)}
    dead = {}
    # the ship: scattered fire across the wing first (it heals), then a held stream on one member
    plan = lambda t: (chi_state(2, t)[0] + (40 * np.sin(t * 7) if t < 3.4 else 0)) if t < 6.2 else 240 + 40 * np.sin(t)
    px_of = follow(plan, CHI_DUR, 230, 230, 0.3)
    py = 470
    fire = Fire()
    shots = EShots()
    for i in range(5):
        for tf in (1.4 + 0.15 * i, 3.0 + 0.15 * i, 4.6 + 0.15 * i):
            x, y, h = chi_state(i, tf)
            if 0 < y < 300:
                for s_ in (-1, 1):
                    ox, oy = rig.local_to_screen(s_ * 0.6, 0.45, h, CHI_SCALE)
                    a = np.arctan2(py - y, 240 - x) + s_ * np.radians(2)
                    shots.fire(tf, x + ox, y + oy, x + np.cos(a) * 100, y + np.sin(a) * 100, 160)
    frames, heal_log = [], []
    for t in times(CHI_DUR):
        f = bg.frame(t)
        px = px_of(t)
        pos = {i: chi_state(i, t) for i in range(5)}

        def hit(x, y, t=t):
            for i, (ux, uy, _) in pos.items():
                if i in dead or not (-10 < uy < FH):
                    continue
                if abs(x - ux) < 15 and abs(y - uy) < 13:
                    hp[i] -= 2.0                                   # a Pulse Cannon L1 bolt
                    last_hit[i] = t
                    f_hits.append((x, y))
                    if hp[i] <= 0:
                        dead[i] = t
                    return True
            return False
        f_hits = []
        fire.step(t, px, py, hit, on=(0.9 < t < 2.0) or (3.6 < t < 6.4))
        for i in range(5):                                         # regeneration: 20 %/s after 1.0 s
            if i not in dead and t - last_hit[i] > 1.0 and hp[i] < CHI_HP:
                hp[i] = min(CHI_HP, hp[i] + 0.2 * CHI_HP / FPS)
        for i in range(5):
            x, y, h = pos[i]
            if i in dead:
                s = (t - dead[i]) / 0.6
                f = boom(f, x, y, s, "small")
                f = flecks(f, x, y, (t - dead[i]) / 0.8, seed=i)
                continue
            if not (-30 < y < FH + 30):
                continue
            share = hp[i] / CHI_HP
            healing = share < 1 and t - last_hit[i] > 1.0
            since = t - last_hit[i]
            if since < 1.0:
                k = 0.08 + 0.1 * share                             # just hit: the seams go dark
                pulse = None
            elif healing:
                k = 0.3 + 0.75 * share
                pulse = (t * 2.0) % 1.0
            else:
                k, pulse = CHI_IDLE, None
            sp = H.get(h, k, pulse)
            on = since < 0.05
            sprite.paste_center(f, flash(sp, on, k=0.4), x, y)
            if share < 1:
                hp_bar(f, x, y - 26, share, teal_edge=healing)
            for tf in (1.4 + 0.15 * i, 3.0 + 0.15 * i, 4.6 + 0.15 * i):
                if tf - 0.25 < t < tf and 0 < y < 300:
                    for s_ in (-1, 1):
                        ox, oy = rig.local_to_screen(s_ * 0.6, 0.45, h, CHI_SCALE)
                        f = light(f, x + ox, y + oy, 4, (255, 60, 50), 1.2)
            heal_log.append((t, i, share, healing))
        for x, y in f_hits:
            f = spark(f, x, y, 0.2, (255, 230, 170), 4, 6)
        fire.draw(f)
        shots.draw(f, t)
        ship(f, px, py, t)
        if 2.1 < t < 3.5:
            caption(f, "SCATTERED HITS: A SECOND WITHOUT A HIT AND IT HEALS")
        elif 3.7 < t < 6.4:
            caption(f, "FOCUS ONE: A HELD STREAM GIVES IT NO SECOND")
        frames.append(f)
    print("chimera kills:", {i: round(t, 2) for i, t in dead.items()}, flush=True)
    return frames, bg, min(dead.values()) if dead else 6.6


def chi_sheet(v, frames, bg, kill=6.6):
    H = chimeras[v]
    heads = [H.frame(k) for k in range(16)]
    states = [H.get(DOWN, k) for k in (0.1, CHI_IDLE, 0.7, 1.05)] + [H.get(DOWN, 0.6, 0.35)]
    talon = e60.talons[("b", False)].get(DOWN)
    full = H.get(DOWN, CHI_IDLE)
    peak = H.get(DOWN, 1.05)
    panels = [("SOURCE: 260 PX", source_panel(lambda: chimera_model(v, tg=0.8))),
              ("16 HEADINGS (1X), SEAMS AT FULL HEALTH", grid(heads, 8, 1)),
              ("SEAMS BY HP (2X): JUST HIT / FULL / HEALING 50 % / HEALING 95 % / THE PULSE",
               row(states, CHI_N + 4, 2, ["HIT", "FULL", "HEAL", "NEAR", "PULSE"])),
              ("BESIDE THE ACT 5 TALON AND THE PLAYER (2X)", row([talon, full, e3.player_sprite()], CHI_N + 4, 2,
                                                                 ["TALON", "CHIMERA", "PLAYER"])),
              ("READABILITY: LEVEL 36'S BANDS", bands_check([(full, shares(full)), (peak, "HEALING")], bg.img)),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.8, 3.0, 5.2, kill + 0.15],
                                              ["HITS, DARK SEAMS", "HEALING PULSE", "HELD STREAM", "KILLED"]))]
    common = ("40X36 PX (46 PX CANVAS), 16 HEADINGS: THE TALON AIRFRAME IN THE PRODUCTION TALON'S GUNMETAL WITH GOLD "
              "OUTER PANELS AND TAIL FINS (Q9 A HYBRID).")
    if v == "a":
        notes = ["CHIMERA A - SEAMS: THIN GLOWING CRACKS, THE TALON SILHOUETTE INTACT",
                 common,
                 "THIN TEAL SEAMS ALONG THE WING ROOTS, ACROSS EACH WING AND THE SPINE, DARK TEAL FLESH IN THEM. FULL",
                 "HEALTH: A FAINT GLOW; JUST HIT: DARK SEAMS; HEALING (A SECOND WITHOUT A HIT, 20 %/S): A TEAL PULSE",
                 "TWICE A SECOND RUNS OUT FROM THE BODY AND THE SEAMS BRIGHTEN TOWARD FULL; THE HP BAR GROWS BACK WITH",
                 "A TEAL EDGE. THE TELL STAYS THE TALON'S RED GLINT. IT READS AS 'A TALON, CRACKED': THE RULE IS NEW,",
                 "THE SHAPE IS NOT. DEATH: GOLD SPARKS AND TEAL FLECKS."]
    else:
        notes = ["CHIMERA B - OVERGROWN: WIDE WAVERING TISSUE BANDS AND NODULES BULGING FROM THE SEAMS",
                 common,
                 "THE SAME SEAMS TWICE AS WIDE, VEINED, WITH TISSUE NODULES AT THE WING ROOTS, ON THE SPINE AND THE WING",
                 "PANELS: MORE TEAL AND A LUMPIER OUTLINE, SO IT READS AS A DIFFERENT UNIT FROM THE TALON AT A GLANCE (AT",
                 "THE COST OF THE TALON'S CLEAN DART). SAME GLOW STATES AND SCENE AS A."]
    return sheet(f"CHIMERA {v.upper()} - THE HYBRID TALON THAT HEALS (LEVEL 36, CLOUD DIVERS)", panels, notes,
                 frames[int(3.0 * FPS)])


@unit("chimera")
def make_chimera(variants):
    for v in variants:
        frames, bg, kill = chi_scene(v)
        save("enemies/air/chimera", "chimera", v, chi_sheet(v, frames, bg, kill), frames)


# ===================================================================== 2 Mirror Interceptor (L37)

A_TELL, A_FRAME = 9, 10
MI_CY, MI_R, MI_HW, MI_ARC = -0.33, 1.12, 0.065, np.radians(62)


def sd_cylinder_y(p, c, r, h):
    return sdf.sd_cylinder_y(p, c, r, h)


def mi_hull(v, tell=0.0, glow=1.0):
    """The short broad gunmetal hull (+Y forward): a wide wedge with gold edge trim and a gold chevron, two
    gun pods under the mirror's rim (their muzzles glow red in the 0.4 s tell), two engines at the rear, red
    wing-tip lights, and the mirror's bare grey frame (seen when the mirror is down). b: gold side panels."""
    body = [(0.0, 0.6), (0.42, 0.54), (0.8, 0.18), (0.94, -0.28), (0.64, -0.64), (0.3, -0.9), (0.0, -0.84)]

    def scene(p):
        q = mirror_x(p)
        roof = lambda x, y: np.clip(1.0 - 0.7 * np.abs(x), 0.35, 1.0)
        d, m = union((sd_plate(q, body, 0.0, 0.17, 0.02, taper=roof), A_HULL),
                     (sd_ellipsoid(p, (0, -0.05, 0.14), (0.26, 0.42, 0.14)), A_FACET),
                     (sd_plate(p, [(0, 0.3), (0.09, 0.08), (0, 0.0), (-0.09, 0.08)], 0.22, 0.03), A_GLASS),
                     k=0.03)
        frame_d = np.maximum(np.abs(np.hypot(p[:, 0], p[:, 1] - MI_CY) - MI_R) - 0.025,
                             (np.abs(np.arctan2(p[:, 0], p[:, 1] - MI_CY)) - MI_ARC) * MI_R)
        frame_d = np.maximum(frame_d, np.abs(p[:, 2] - 0.05) - 0.03)
        d, m = union((d, m),
                     (frame_d, A_FRAME),
                     (sd_capsule(q, (0.34, 0.5, 0.0), (0.34, 0.08, 0.0), 0.06, 0.05), A_FRAME),
                     (sd_capsule(q, (0.45, 0.32, -0.06), (0.45, 0.72, -0.06), 0.075, 0.06), A_GUN),
                     (sd_sphere(q, (0.45, 0.74, -0.06), 0.05), A_TELL),
                     (sd_cylinder_y(q, (0.3, -0.86, 0.0), 0.09, 0.05), A_ENGINE),
                     (sd_sphere(q, (0.9, -0.3, 0.02), 0.045), A_RED))
        edge = (q[:, 0] > 0.62 + 0.25 * (q[:, 1] + 0.28)) | ((q[:, 1] < -0.55) & (q[:, 0] > 0.38))
        m = np.where((m == A_HULL) & edge, A_GOLD, m)
        chev = np.abs(q[:, 1] - (0.3 - 0.9 * q[:, 0])) < 0.06
        m = np.where((m == A_HULL) & chev & (q[:, 0] < 0.5), A_GOLD, m)
        if v == "b":
            m = np.where((m == A_HULL) & (q[:, 0] > 0.45) & (q[:, 1] < 0.2) & (q[:, 1] > -0.5), A_GOLD, m)
        return d, m
    extra = [mirror_mat(), emissive((1.0, 0.18, 0.12), 0.15 + 2.2 * tell),
             Material(hx("8a8f98"), metal=0.6, shininess=60, spec=0.8)]
    return scene, regular_mats("c8202a", glow, extra=[Material((0, 0, 0))] + extra)


def mi_mirror(v):
    """The mirror alone. a "crescent": one curved polished plate 76 px across the nose, gold caps at the tips;
    b "facets": five flat mirror panels set round the same arc in gold frames."""
    def scene(p):
        rr = np.hypot(p[:, 0], p[:, 1] - MI_CY)
        phi = np.arctan2(p[:, 0], p[:, 1] - MI_CY)
        dz = np.abs(p[:, 2] - 0.12) - 0.1
        if v == "a":
            d = np.maximum(np.maximum(np.abs(rr - MI_R) - MI_HW, (np.abs(phi) - MI_ARC) * MI_R), dz) - 0.01
            m = np.where(np.abs(phi) > MI_ARC - 0.1, A_GOLD, A_MIRROR)
            return d, m
        parts = []
        for i in range(-2, 3):
            a = i * np.radians(24)
            c = np.array((np.sin(a) * MI_R, MI_CY + np.cos(a) * MI_R, 0.12))
            loc = rotate_z(p - c, -a)
            parts.append((sd_box(loc, (0, 0, 0), (0.215, 0.06, 0.1), 0.01), A_MIRROR))
        for i in range(-3, 3):
            a = (i + 0.5) * np.radians(24)
            c = np.array((np.sin(a) * MI_R, MI_CY + np.cos(a) * MI_R, 0.13))
            loc = rotate_z(p - c, -a)
            parts.append((sd_box(loc, (0, 0, 0), (0.035, 0.08, 0.11), 0.01), A_GOLD))
        return union(*parts)
    return scene, regular_mats("c8202a", extra=[Material((0, 0, 0)), mirror_mat()])


MI_N = 84
MI_SCALE = MI_N / 2.3
mi_hulls = {v: Turning(lambda v=v, **kw: mi_hull(v, **kw), MI_N, 16, factor=4, colors=48, post=REG_RIM)
            for v in "ab"}
mi_mirrors = {v: Turning(lambda v=v: mi_mirror(v), MI_N, 16, factor=4, colors=40) for v in "ab"}


def heat_col(h):
    stops = [(0.0, (0, 0, 0)), (0.33, (170, 40, 20)), (0.66, (255, 140, 40)), (1.0, (255, 244, 220))]
    return raster.ramp(stops, np.array([h]))[0]


def mirror_layer(v, hd, t=0.0, heat=0.0, regrow=None):
    """The mirror sprite at heading ``hd`` with its states: the slow specular sweep, the heat (dull red,
    orange, white; cracks from 70 %), the regrowth (revealed from one tip behind a silver shimmer)."""
    sp = mi_mirrors[v].get(hd)
    a = np.array(sp).astype(np.float64)
    op = a[..., 3] > 0
    h_, w_ = op.shape
    yy, xx = np.mgrid[0:h_, 0:w_].astype(np.float64)
    cx, cy = w_ / 2, h_ / 2
    along = ((xx - cx) * -np.sin(hd) + (yy - cy) * np.cos(hd)) / (MI_R * MI_SCALE)    # tip to tip, -1..1
    sweep = np.exp(-((along - (np.sin(t * 1.3) * 1.1)) / 0.16) ** 2)
    a[..., :3] += smooth_steps(sweep * 0.55)[..., None] * 255 * op[..., None]
    if heat > 0:
        col = heat_col(heat)
        k = smooth_steps(min(1.0, heat * 1.1) * 0.85)
        a[op, :3] = a[op, :3] * (1 - k) + col * k
        if heat >= 0.7:
            rng = np.random.default_rng(int(hd * 100) % 97)
            img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
            dd = ImageDraw.Draw(img)
            n = 2 + int((heat - 0.7) * 10)
            ox, oy = cx + MI_CY * MI_SCALE * np.cos(hd), cy + MI_CY * MI_SCALE * np.sin(hd)
            for j in range(n):
                s0 = -0.7 + 1.4 * j / max(1, n - 1)
                pts = []
                for u in np.linspace(0, 1, 5):
                    rad = MI_R * MI_SCALE * (1.0 - 0.07 + 0.14 * u)
                    ang = hd + s0 * MI_ARC + rng.normal(0, 0.05)
                    pts.append((ox + np.cos(ang) * rad, oy + np.sin(ang) * rad))
                dd.line(pts, fill=(60, 20, 12, 255), width=1)
            a2 = np.array(img).astype(np.float64)
            a2[~op] = a[~op]
            a = a2
    if regrow is not None:
        front = -1.15 + 2.3 * regrow
        a[..., 3] = np.where(along < front, a[..., 3], 0)
        band = np.exp(-((along - front) / 0.12) ** 2) * op
        a[..., :3] += smooth_steps(band * 0.8)[..., None] * 255
    a[..., :3] = np.clip(a[..., :3], 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def mi_sprite(v, hd, t=0.0, heat=0.0, down=False, regrow=None, tell=0.0):
    hull = mi_hulls[v].get(hd, tell=round(tell, 1))
    c = hull.copy()
    if not down:
        c.alpha_composite(mirror_layer(v, hd, t, heat, regrow))
    if tell > 0:
        for s_ in (-1, 1):
            ox, oy = rig.local_to_screen(s_ * 0.45, 0.8, hd, MI_SCALE)
            c = light(c, c.width / 2 + ox, c.height / 2 + oy, 6.0, (255, 60, 40), 2.2 * tell)
    return c


class Shards:
    """Silver mirror shards bursting and falling (presentation)."""

    def __init__(self):
        self.list = []

    def burst(self, t0, x, y, hd, n=14, seed=0):
        rng = np.random.default_rng(seed)
        for k in range(n):
            a = hd + rng.uniform(-1.1, 1.1)
            r = MI_R * MI_SCALE * rng.uniform(0.85, 1.05)
            sx, sy = x + np.cos(a) * r * 0.9, y + np.sin(a) * r * 0.9
            sp = rng.uniform(30, 90)
            self.list.append((t0, sx, sy, np.cos(a) * sp, np.sin(a) * sp, rng.uniform(0, TAU), rng.uniform(2, 4)))

    def draw(self, img, t):
        d = ImageDraw.Draw(img)
        for t0, x, y, vx, vy, a0, s in self.list:
            u = t - t0
            if not 0 <= u < 1.4:
                continue
            px, py = x + vx * u, y + vy * u + 90 * u * u
            a = a0 + u * 9
            al = int(255 * min(1, 1.6 - u))
            pts = [(px + np.cos(a + k * 2.1) * s, py + np.sin(a + k * 2.1) * s) for k in range(3)]
            bright = (235, 240, 250) if int(u * 12) % 2 else (150, 160, 175)
            d.polygon(pts, fill=bright + (al,))


MI_DUR = 12.8
MI_PATH = rig.catmull_rom([(FW + 50, 120), (420, 170), (370, 230)], [1.4, 2.6, 3.6])
MI_OUT = rig.catmull_rom([(370, 230), (430, 175), (FW + 60, 110)], [10.6, 11.6, 12.6])
MI_TELLS = [6.2, 8.6]                      # the pod tells (0.4 s), each followed by a burst of 3


def mi_scene(v):
    bg = Scroll(deck_bg, 37, MI_DUR, 15, burnt=((420, 640),))
    py = 470
    plan = lambda t: 415 if t < 3.7 else (330 if t < 6.0 else (290 + 40 * np.sin(t * 2) if t < 9.8 else 445))
    px_of = follow(plan, MI_DUR, 415, 200, 0.4)
    pyf = lambda t: py
    fire, shots, bounced, shards = Fire(rate=12), EShots(), EShots(MAGENTA), Shards()
    st = dict(hp=120.0, dead=None, last_bounce=-9.0, hull=0, mirror=0)
    hd = LEFT
    sparks = []
    burst_ts = [tl + 0.4 + 0.15 * j for tl in MI_TELLS for j in range(3)]
    frames = []
    for t in times(MI_DUR):
        f = bg.frame(t)
        px, pyy = px_of(t), pyf(t)
        if t < 3.6:
            x, y = MI_PATH(max(1.4, t))
            if t >= 1.4:
                hd = rig.path_heading(MI_PATH, max(1.45, t))
        elif t >= 10.6:
            x, y = MI_OUT(t)
            want = rig.path_heading(MI_OUT, max(10.65, t))
            dh = (want - hd + np.pi) % TAU - np.pi
            hd += float(np.clip(dh, -np.radians(180) / FPS, np.radians(180) / FPS))
        else:
            x, y = MI_PATH(3.6)
            want = np.arctan2(pyy - y, px - x)
            if not any(tl <= t < tl + 0.85 for tl in MI_TELLS):
                dh = (want - hd + np.pi) % TAU - np.pi
                hd += float(np.clip(dh, -np.radians(60) / FPS, np.radians(60) / FPS))
        live = t >= 1.4 and st["dead"] is None

        def hit(bx, by, t=t, x=x, y=y, hd=hd, live=live):
            if not live or np.hypot(bx - x, by - y) > 32:
                return False
            rel = np.arctan2(by - y, bx - x)
            off = abs((rel - hd + np.pi) % TAU - np.pi)
            if off <= np.radians(60):
                st["mirror"] += 1
                if t - st["last_bounce"] >= 0.15:
                    st["last_bounce"] = t
                    n = np.array((np.cos(hd), np.sin(hd)))
                    dv = np.array((0.0, -1.0))
                    r = dv - 2 * dv.dot(n) * n
                    bounced.fire(t, bx, by, bx + r[0] * 50, by + r[1] * 50, 110, 4)
                sparks.append((t, bx, by, (230, 235, 255)))
            else:
                st["hull"] += 1
                st["hp"] -= 3.0                                   # a mid-act gun's bolt
                sparks.append((t, bx, by, (255, 210, 140)))
                if st["hp"] <= 0:
                    st["dead"] = t
                    shards.burst(t, x, y, hd, seed=3)
            return True
        fire.step(t, px, pyy, hit, on=(1.6 < t < 5.9) or t > 10.2)
        if live:
            for tb in burst_ts:
                if tb <= t < tb + 1.0 / FPS:
                    for s_ in (-1, 1):
                        ox, oy = rig.local_to_screen(s_ * 0.45, 0.76, hd, MI_SCALE)
                        a = np.arctan2(pyy - y - oy, px - x - ox)
                        shots.fire(t, x + ox, y + oy, x + ox + np.cos(a) * 50, y + oy + np.sin(a) * 50, 160, 5)
        if st["dead"] is None and x < FW + 50:
            tell = 1.0 if any(tl <= t < tl + 0.4 for tl in MI_TELLS) else 0.0
            sp = mi_sprite(v, hd, t, tell=tell)
            sprite.paste_center(f, sprite.shadow_of(sp, 0.35, 1.5, 0.85), x + 10, y + 16)
            sprite.paste_center(f, sp, x, y)
            if st["hp"] < 120:
                hp_bar(f, x, y - 44, st["hp"] / 120.0)
        elif st["dead"] is not None:
            f = boom(f, x, y, (t - st["dead"]) / 0.7, "medium", sparks=(220, 225, 240))
        shards.draw(f, t)
        for t0, sx, sy, col in sparks:
            if 0 <= t - t0 < 0.15:
                f = spark(f, sx, sy, (t - t0) / 0.15, col, 4, 6)
        fire.draw(f)
        shots.draw(f, t)
        bounced.draw(f, t)
        ship(f, px, pyy, t)
        if t < 1.5:
            side_warning(f, t, 1, [130])
        if 1.6 < t < 3.4:
            caption(f, "ON ITS WAY IN IT SHOWS ITS FLANK: HIT IT NOW")
        elif 3.8 < t < 6.0:
            caption(f, "INTO THE MIRROR: SHOTS COME BACK SLOW AND MAGENTA")
        elif 6.1 < t < 10.4:
            caption(f, "STOP FIRING INTO IT: 7 S AT ITS STATION, POD BURSTS (0.4 S RED TELL)")
        elif 10.5 < t:
            caption(f, "IT LEAVES THE WAY IT CAME: THE FLANK AGAIN")
        frames.append(f)
    print(f"mirror interceptor {v}: hull hits {st['hull']}, mirror hits {st['mirror']}, killed at {st['dead']}",
          flush=True)
    return frames, bg, st


def mi_sheet(v, frames, bg, st):
    heads = [mi_sprite(v, k * TAU / 16 + 1e-3, 0.4) for k in range(16)]
    states = [mi_sprite(v, DOWN, 0.0), mi_sprite(v, DOWN, 1.2), mi_sprite(v, DOWN, 0, heat=0.4),
              mi_sprite(v, DOWN, 0, heat=0.78), mi_sprite(v, DOWN, 0, heat=1.0), mi_sprite(v, DOWN, down=True),
              mi_sprite(v, DOWN, 0, regrow=0.55), mi_sprite(v, DOWN, tell=1.0)]
    main = mi_sprite(v, DOWN, 0.4)
    hull_only = mi_hulls[v].get(DOWN)
    kill = st["dead"] or MI_DUR - 1
    panels = [("SOURCE: 260 PX", source_panel(lambda: ((lambda p: union(mi_hull(v)[0](p), mi_mirror(v)[0](p))),
                                                       mi_hull(v)[1]))),
              ("16 HEADINGS (1X)", grid(heads, 8, 1)),
              ("STATES (2X)", row(states, MI_N, 2, ["MIRROR", "SWEEP", "HEAT 40", "HEAT 78", "HEAT 100", "BROKEN",
                                                    "REGROW", "POD TELL"])),
              ("READABILITY: LEVEL 37 (THE DECK, THE BURNT DECK)",
               bands_check([(main, shares(main, False))], bg.img, names=("BRIGHT", "DARK"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [2.6, 4.6, 6.5, min(MI_DUR - 0.1, kill + 0.3)],
                                              ["FLANK ON ENTRY", "BOUNCES", "POD BURST", "FLANK KILL"]))]
    hull_gold = f"HULL ALONE {100 * gold_share(hull_only):.0f} % GOLD"
    if v == "a":
        notes = ["MIRROR INTERCEPTOR A - THE CRESCENT: ONE CURVED POLISHED PLATE ACROSS THE NOSE",
                 f"72X68 PX (84 PX CANVAS), 16 HEADINGS; REGULAR GUNMETAL (Q9 A): GOLD EDGE TRIM AND CHEVRON, RED LIGHTS, "
                 f"1 PX RIM ({shares(main, False)} WITH THE MIRROR, {hull_gold}).",
                 "THE MIRROR: A SILVER CRESCENT 76 PX ACROSS WITH GOLD TIP CAPS AND A SLOW SPECULAR SWEEP, SO THE 120 DEG",
                 "ARC READS AT A GLANCE; THE TIPS MARK WHERE THE OPEN FLANK STARTS. HEAT: DULL RED, ORANGE, WHITE,",
                 "CRACKS FROM 70 %; BROKEN: THE BARE GREY FRAME; REGROWING: A SILVER SHIMMER SWEEPS IN FROM ONE TIP.",
                 "BOUNCED SHOTS: SLOW MAGENTA SMALL BULLETS (110 PX/S, ONE PER 0.15 S, THE REST SPARK). TELL: 0.4 S RED",
                 "GLOW IN THE PODS UNDER THE RIM, THEN 3 AIMED SHOTS. DEATH: SILVER SHARDS FALL."]
    else:
        notes = ["MIRROR INTERCEPTOR B - THE FACETS: FIVE FLAT MIRROR PANELS IN GOLD FRAMES ROUND THE SAME ARC",
                 f"THE SAME HULL WITH GOLD SIDE PANELS ({shares(main, False)}, {hull_gold}). THE FACETED MIRROR FLASHES",
                 "PANEL BY PANEL AS IT TURNS (MORE SPARKLE, A HARDER 'MACHINE' LOOK) AND THE GOLD FRAMES COUNT THE ARC;",
                 "THE CRESCENT'S CLEAN EDGE IS LOST, SO THE ARC'S ENDS READ A LITTLE LESS SHARPLY. SAME STATES AND SCENE."]
    return sheet(f"MIRROR INTERCEPTOR {v.upper()} - THE MIRROR, THE BOUNCE, THE FLANK (LEVEL 37, GILDED CAGE)",
                 panels, notes, frames[int(4.6 * FPS)])


@unit("mirror-interceptor")
def make_mi(variants):
    for v in variants:
        frames, bg, st = mi_scene(v)
        save("enemies/air/mirror-interceptor", "mirror-interceptor", v, mi_sheet(v, frames, bg, st), frames)


# ===================================================================== 3 Harrow (L38)

def harrow_model(v, only_glow=False, tg=1.0):
    """34x34 px, +Y forward. a "dart and sac": a short gunmetal dart with gold leading edges, swept fins, and a
    swollen sac of dark teal tissue on its back behind the nose, its veins glowing; b "fork": two gunmetal
    prongs joined at the tail, gold-tipped, cradling a longer sac between them (the payload in plain view)."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            body = [(0.0, 1.0), (0.16, 0.45), (0.56, -0.52), (0.44, -0.66), (0.16, -0.44), (0.12, -0.82),
                    (0.0, -0.74)]
            roof = lambda x, y: np.clip(1.0 - 3.0 * np.abs(x), 0.3, 1.0)
            d, m = union((sd_plate(q, body, 0.0, 0.12, 0.0, taper=roof), A_HULL),
                         (sd_cylinder_y(q, (0.07, -0.78, 0.0), 0.06, 0.05), A_ENGINE))
            lead = seg_dist2(q, [(0.0, 1.0, 0.16, 0.45), (0.16, 0.45, 0.56, -0.52)]) < 0.075
            m = np.where((m == A_HULL) & lead, A_GOLD, m)
            m = np.where((m == A_HULL) & (q[:, 1] < -0.4) & (q[:, 0] > 0.2), A_FACET, m)
            return union((d, m), (sd_ellipsoid(p, (0, -0.08, 0.13), (0.27, 0.42, 0.21)), A_TISSUE), k=0.04)
        prong = [(0.2, 0.98), (0.36, 0.92), (0.48, -0.25), (0.5, -0.72), (0.18, -0.78), (0.26, -0.25)]
        d, m = union((sd_plate(q, prong, 0.0, 0.11, 0.0), A_HULL),
                     (sd_box(p, (0, -0.68, 0.0), (0.3, 0.08, 0.08), 0.03), A_FACET),
                     (sd_cylinder_y(p, (0.0, -0.76, 0.0), 0.08, 0.05), A_ENGINE))
        tips = (q[:, 1] > 0.6) | (seg_dist2(q, [(0.36, 0.92, 0.48, -0.25)]) < 0.06)
        m = np.where((m == A_HULL) & tips, A_GOLD, m)
        return union((d, m), (sd_ellipsoid(p, (0, 0.1, 0.05), (0.21, 0.6, 0.2)), A_TISSUE), k=0.03)
    mats = regular_mats("c8202a", 1.0, extra=[tissue_mat(tg, veins=True)])
    return scene, (glow_only(mats) if only_glow else mats)


HAR_N = 40
HAR_SCALE = HAR_N / 2.3
harrows = {v: Hybrid(lambda v=v, **kw: harrow_model(v, **kw), HAR_N, colors=32) for v in "ab"}
HAR_DUR = 10.5
HAR_HP = 16.0
STRIKE = (6.2, 205.0)                  # the lightning strike's time and column x (telegraphed 0.8 s ahead)
HARS = [dict(t0=1.0, x=-20, y=150, h=RIGHT, hunt=3.0, edge="left"),
        dict(t0=1.6, x=FW + 20, y=290, h=LEFT, hunt=2.0, edge="right"),
        dict(t0=4.3, x=205, y=-20, h=DOWN, hunt=4.0, edge="top"),
        dict(t0=5.6, x=FW + 20, y=120, h=LEFT, hunt=2.2, edge="right")]


def har_scene(v):
    bg = Scroll(storm_bg, 38, HAR_DUR, AIR_SCROLL)
    H = harrows[v]
    py = 470
    st = [dict(d, x=float(d["x"]), y=float(d["y"]), phase="wait", hp=HAR_HP, lock=None, died=None, ram=None)
          for d in HARS]

    def plan(t):
        if t < 2.6:
            return st[0]["x"] if st[0]["phase"] in ("hunt", "tell") else 200
        if t < 3.9:
            return 200
        if t < 4.9:
            return 330
        if t < 7.2:
            return 212
        return st[3]["x"] if st[3]["phase"] in ("hunt", "tell") else 300
    xs = {"px": 240.0}
    fire, ring = Fire(rate=12), EShots(TEAL)
    sparks, frames = [], []
    strike = {"x": STRIKE[1]}
    for t in times(HAR_DUR):
        f = bg.frame(t)
        px = xs["px"]
        if abs(t - (STRIKE[0] - 0.8)) < 0.5 / FPS and st[2]["phase"] == "hunt":
            strike["x"] = st[2]["x"] + np.cos(st[2]["h"]) * 110 * 0.8      # the column the third will cross
        tx = plan(t)
        xs["px"] += float(np.clip((tx - px) / 0.3, -240, 240)) / FPS
        px = xs["px"]
        for i, s in enumerate(st):
            if s["phase"] == "wait" and t >= s["t0"]:
                s["phase"] = "hunt"
            if s["phase"] == "hunt":
                want = np.arctan2(py - s["y"], px - s["x"])
                dh = (want - s["h"] + np.pi) % TAU - np.pi
                s["h"] += float(np.clip(dh, -np.radians(90) / FPS, np.radians(90) / FPS))
                sp = 110
                if t >= s["t0"] + s["hunt"]:
                    s["phase"], s["tell0"] = "tell", t
            elif s["phase"] == "tell":
                want = np.arctan2(py - s["y"], px - s["x"])
                dh = (want - s["h"] + np.pi) % TAU - np.pi
                s["h"] += float(np.clip(dh, -np.radians(240) / FPS, np.radians(240) / FPS))
                sp = 50
                if t >= s["tell0"] + 0.6:
                    s["phase"], s["lock"] = "ram", np.arctan2(py - s["y"], px - s["x"])
                    s["h"] = s["lock"]
            elif s["phase"] == "ram":
                sp = 300
            else:
                sp = 0
            if s["phase"] in ("hunt", "tell", "ram"):
                s["x"] += np.cos(s["h"]) * sp / FPS
                s["y"] += np.sin(s["h"]) * sp / FPS
                if not (-60 < s["x"] < FW + 60 and -60 < s["y"] < FH + 60):
                    s["phase"] = "gone"
                if t >= STRIKE[0] and t - STRIKE[0] < 1.0 / FPS and abs(s["x"] - strike["x"]) < 18:
                    s["phase"], s["died"], s["how"] = "dead", t, "LIGHTNING"

        def hit(bx, by, t=t):
            for s in st:
                if s["phase"] in ("hunt", "tell", "ram") and abs(bx - s["x"]) < 12 and abs(by - s["y"]) < 12:
                    s["hp"] -= 3.0
                    sparks.append((t, bx, by))
                    if s["hp"] <= 0:
                        s["phase"], s["died"], s["how"] = "dead", t, "SHOT"
                    return True
            return False
        fire.step(t, px, py, hit, on=(1.2 < t < 2.7) or (8.0 < t < 9.4))
        for s in st:
            if s["phase"] == "dead" and s["died"] == t:
                bearing = np.arctan2(py - s["y"], px - s["x"])
                ring.ring(t, s["x"], s["y"], 8, 90, 4, phase=bearing + np.pi / 8)
                s["bearing"] = bearing
        # draw
        if STRIKE[0] - 0.8 <= t < STRIKE[0]:
            if int(t * 14) % 2:
                d = ImageDraw.Draw(f)
                d.line([strike["x"], 0, strike["x"], FH], fill=(190, 215, 255, 150), width=1)
            f = light(f, strike["x"], 12, 10, (170, 200, 255), 0.6)
        if STRIKE[0] <= t < STRIKE[0] + 0.25:
            u = (t - STRIKE[0]) / 0.25
            a = np.array(f).astype(np.float64)
            a[..., :3] = a[..., :3] * (1 - 0.35 * (1 - u)) + np.array((200, 220, 255)) * 0.35 * (1 - u)
            f = Image.fromarray(a.astype(np.uint8), "RGBA")
            rng = np.random.default_rng(int(t * 100))
            pts = g36.jagged((strike["x"], -10), (strike["x"] + 10, FH + 10), rng, depth=6, rough=0.12)
            d = ImageDraw.Draw(f)
            d.line(pts, fill=(170, 200, 255, int(200 * (1 - u))), width=5)
            d.line(pts, fill=(255, 255, 255, int(255 * (1 - u))), width=2)
        for i, s in enumerate(st):
            if s["phase"] == "wait" and s["t0"] - 1.0 <= t < s["t0"]:
                if s["edge"] == "top":
                    pass
                else:
                    side_warning(f, t, -1 if s["edge"] == "left" else 1, [int(s["y"])])
            if s["phase"] in ("hunt", "tell", "ram"):
                if s["phase"] == "hunt":
                    k, pulse = 0.35 + 0.2 * np.sin(t * TAU * 1.0), None
                elif s["phase"] == "tell":
                    u = (t - s["tell0"]) / 0.6
                    k, pulse = 0.7 + 0.5 * u + 0.3 * np.sin(t * TAU * 6), None
                else:
                    k, pulse = 1.1, None
                sp = H.get(s["h"], k, pulse)
                sprite.paste_center(f, sprite.shadow_of(sp, 0.3, 1.5, 0.85), s["x"] + 8, s["y"] + 14)
                sprite.paste_center(f, sp, s["x"], s["y"])
                if s["phase"] == "tell":
                    f = light(f, s["x"], s["y"], 9, TEAL, 0.25 + 0.35 * u)
            elif s["phase"] == "dead":
                u = (t - s["died"]) / 0.5
                if u < 1:
                    f = light(f, s["x"], s["y"], 14 * (0.5 + u), TEAL, 1.2 * (1 - u))
                    f = boom(f, s["x"], s["y"], u, "tiny", sparks=None)
                f = flecks(f, s["x"], s["y"], (t - s["died"]) / 0.7, seed=7)
                if (t - s["died"]) < 1.0:
                    sh = rot_sprite(H.body.get(s["h"]), (t - s["died"]) * 400)
                    sprite.paste_center(f, fade(tint(sh, (40, 40, 44), 0.5), 1 - (t - s["died"])),
                                        s["x"] + 30 * (t - s["died"]), s["y"] + 50 * (t - s["died"]) ** 2)
        for t0, bx, by in sparks:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, (255, 230, 170), 4, 6)
        fire.draw(f)
        ring.draw(f, t)
        ship(f, px, py, t)
        tells = [s for s in st if s["phase"] == "tell"]
        rams = [s for s in st if s["phase"] == "ram"]
        if any(s["phase"] == "dead" and t - s["died"] < 1.6 and s.get("how") == "LIGHTNING" for s in st):
            caption(f, "LIGHTNING KILLS IT: IT PAYS, AND ITS RING OPENS")
        elif any(s["phase"] == "dead" and t - s["died"] < 1.4 for s in st):
            caption(f, "EVERY DEATH OPENS A SLOW RING OF 8; THE SHIP'S BEARING IS THE GAP")
        elif tells:
            caption(f, "THE LOCK TELL (0.6 S): IT SLOWS, THE SAC PULSES FAST")
        elif rams:
            caption(f, "THE RAM IS STRAIGHT: SIDESTEP THE LINE")
        frames.append(f)
    print("harrow", v, [(s.get("how"), s["died"] and round(s["died"], 2), s["phase"]) for s in st], flush=True)
    return frames, bg, st


def har_sheet(v, frames, bg, st):
    H = harrows[v]
    heads = [H.frame(k) for k in range(16)]
    states = [H.get(DOWN, 0.15), H.get(DOWN, 0.55), H.get(DOWN, 0.9), H.get(DOWN, 1.4)]
    ref = Image.new("RGBA", (HAR_N, HAR_N), (0, 0, 0, 0))
    orbc = ref.copy()
    e3.orb(orbc, HAR_N / 2, HAR_N / 2, 4, ring=TEAL)
    main = H.get(DOWN, 0.55)
    deaths = sorted([s["died"] for s in st if s["died"]])
    k1 = (deaths[0] + 0.5) if deaths else 2.8
    kl = STRIKE[0] + 0.1
    panels = [("SOURCE: 260 PX", source_panel(lambda: harrow_model(v, tg=0.8))),
              ("16 HEADINGS (1X), HUNTING", grid(heads, 8, 1)),
              ("THE SAC (2X): DIM / HUNTING / TELL / TELL PEAK; ITS RING BULLET",
               row(states + [orbc, e3.player_sprite()], HAR_N + 8, 2, ["DIM", "HUNT", "TELL", "PEAK", "BULLET",
                                                                       "PLAYER"])),
              ("READABILITY: LEVEL 38'S STORM", bands_check([(main, shares(main))], bg.img, names=("LIT FLANK", "DARKEST"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [k1, 4.0, 4.5, kl],
                                              ["SHOT AT RANGE", "LOCK TELL", "RAM MISSED", "LIGHTNING"]))]
    if v == "a":
        notes = ["HARROW A - DART AND SAC: A GUNMETAL DART WITH A SWOLLEN TISSUE SAC ON ITS BACK",
                 f"34X34 PX (40 PX CANVAS), 16 HEADINGS (Q9 A HYBRID; {shares(main)} HUNTING). GOLD LEADING EDGES, SWEPT",
                 "FINS, THE SAC DARK TEAL WITH GLOWING VEINS: A SLOW PULSE WHILE IT HUNTS (110 PX/S), A FAST BRIGHTENING ONE",
                 "IN THE 0.6 S LOCK TELL (IT SLOWS TO 50 PX/S AND SWINGS ITS NOSE ONTO THE SHIP), THEN A STRAIGHT RAM AT",
                 "300 PX/S. ANY DEATH: A TEAL FLASH, FLECKS, THE SHELL TUMBLES AWAY, AND A RING OF 8 SLOW TEAL BULLETS",
                 "(90 PX/S) TURNED SO THE SHIP'S BEARING FALLS BETWEEN TWO. LIGHTNING THAT CROSSES IT KILLS IT (J3)."]
    else:
        notes = ["HARROW B - FORK: TWO GOLD-TIPPED PRONGS CRADLING A LONGER SAC",
                 f"THE SAME SIZE AND STATES ({shares(main)} HUNTING): THE PAYLOAD SITS IN PLAIN VIEW BETWEEN THE PRONGS, SO",
                 "THE TEAL READS FROM EVERY HEADING AND SAYS 'IT BURSTS'; A STRONGER TEAL SHARE, A LESS DART-LIKE NOSE (THE",
                 "RAM'S LINE IS READ FROM THE PRONGS). SAME SCENE AS A."]
    return sheet(f"HARROW {v.upper()} - HUNT, LOCK, RAM, BURST (LEVEL 38, LIGHTNING BELT)", panels, notes,
                 frames[min(len(frames) - 1, int(k1 * FPS))])


def rot_sprite(sp, deg):
    return sp.rotate(deg, resample=Image.BICUBIC, expand=True)


@unit("harrow")
def make_harrow(variants):
    for v in variants:
        frames, bg, st = har_scene(v)
        save("enemies/air/harrow", "harrow", v, har_sheet(v, frames, bg, st), frames)


# ===================================================================== 4 Graft Turret (L37)

A_BONE = 9


def graft_base(v, only_glow=False, tg=1.0):
    """The fixed base: a gunmetal octagonal deck mount with a gold ring. a "metal first": three thin tissue
    cracks run out from under the turret; b "tissue first": a fleshy collar round the turret and six roots
    spreading across the deck beyond the mount (the graft is visible before anything is shot)."""
    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        a = np.arctan2(p[:, 1], p[:, 0])
        sec = TAU / 8
        af = (a + sec / 2) % sec - sec / 2
        op = np.stack([r * np.cos(af), r * np.sin(af), p[:, 2]], axis=-1)
        d, m = sd_box(op, (0, 0, 0.04), (0.86, 0.5, 0.06), 0.02), np.full(len(p), A_HULL)
        ring = np.abs(r - 0.72) < 0.06
        m = np.where(ring, A_GOLD, m)
        if v == "a":
            cracks = [(0.0, 0.0, 0.8 * np.cos(t_), 0.8 * np.sin(t_)) for t_ in (0.5, 2.6, 4.4)]
            m = np.where((seg_dist2(p, cracks) < 0.05) & (r > 0.45), A_TISSUE, m)
            return d, m
        parts = [(d, m)]
        for k in range(6):
            t_ = k * TAU / 6 + 0.3
            wob = 0.12 * np.sin(k * 2.3)
            a0 = (0.45 * np.cos(t_), 0.45 * np.sin(t_), 0.1)
            a1 = (1.12 * np.cos(t_ + wob), 1.12 * np.sin(t_ + wob), 0.06)
            parts.append((sd_capsule(p, a0, a1, 0.085, 0.03), A_TISSUE))
        tor = np.hypot(r - 0.52, p[:, 2] - 0.12) - 0.09
        parts.append((tor, A_TISSUE))
        return union(*parts, k=0.04)
    mats = regular_mats("c8202a", extra=[tissue_mat(tg, veins=True)])
    return scene, (glow_only(mats) if only_glow else mats)


def graft_head(v, regrown=False, only_glow=False, tg=1.0):
    """The turret (+Y = its barrel): an armoured dome with a gold ring and one barrel, gold muzzle, red sensor.
    a: thin tissue scars across the dome; b: a third of the dome overgrown. Regrown: more tissue than metal,
    the barrel bone-white and veined teal (a turret that came back reads as such)."""
    def scene(p):
        q = mirror_x(p)
        d, m = union((sd_ellipsoid(p, (0, -0.05, 0.3), (0.5, 0.52, 0.24)), A_HULL),
                     (sd_cylinder_z(p, (0, -0.05, 0.2), 0.56, 0.04), A_GOLD),
                     (sd_capsule(p, (0, 0.25, 0.34), (0, 0.98, 0.34), 0.085, 0.07), A_GUN),
                     (sd_capsule(p, (0, 0.92, 0.34), (0, 1.02, 0.34), 0.1, 0.1), A_GOLD),
                     (sd_sphere(p, (0, 0.18, 0.5), 0.07), A_RED), k=0.02)
        if regrown:
            over = (np.sin(p[:, 0] * 7 + 1) + np.sin(p[:, 1] * 9) > -0.6)
            m = np.where((m == A_HULL) & over, A_TISSUE, m)
            m = np.where(m == A_GUN, A_BONE, m)
            m = np.where((m == A_GOLD) & (q[:, 0] > 0.3), A_TISSUE, m)
            vein = np.abs(np.sin(p[:, 1] * 22 + p[:, 0] * 30)) < 0.25
            m = np.where((m == A_BONE) & vein, A_TISSUE, m)
            return union((d, m), (sd_ellipsoid(p, (0.22, -0.3, 0.3), (0.22, 0.2, 0.16)), A_TISSUE), k=0.06)
        if v == "a":
            scars = [(-0.42, -0.38, 0.1, 0.15), (0.1, 0.15, 0.46, 0.0), (-0.2, 0.3, -0.48, 0.1),
                     (0.05, -0.5, 0.38, -0.3), (-0.1, -0.1, -0.3, -0.5)]
            m = np.where((m == A_HULL) & (seg_dist2(p, scars) < 0.06), A_TISSUE, m)
            return d, m
        grow = p[:, 0] + 0.15 * np.sin(p[:, 1] * 9) > 0.18
        m = np.where((m == A_HULL) & grow, A_TISSUE, m)
        return union((d, m), (sd_ellipsoid(p, (0.32, -0.25, 0.32), (0.18, 0.2, 0.14)), A_TISSUE), k=0.05)
    mats = regular_mats("c8202a", extra=[tissue_mat(tg, veins=True),
                                         Material(hx("d8cfb4"), metal=0.1, shininess=50, spec=0.6)])
    return scene, (glow_only(mats) if only_glow else mats)


def graft_root(v, only_glow=False, tg=1.0):
    """The root: a knot of teal tissue under the turret (a bigger bulb with b)."""
    s = 1.0 if v == "a" else 1.25

    def scene(p):
        parts = [(sd_ellipsoid(p, (0, 0, 0.12), (0.32 * s, 0.3 * s, 0.2)), A_TISSUE)]
        for k in range(5):
            t_ = k * TAU / 5 + 0.4
            parts.append((sd_ellipsoid(p, (0.24 * s * np.cos(t_), 0.24 * s * np.sin(t_), 0.08),
                                       (0.14 * s, 0.12 * s, 0.1)), A_TISSUE))
        return union(*parts, k=0.08)
    mats = regular_mats("c8202a", extra=[tissue_mat(tg, veins=True)])
    return scene, (glow_only(mats) if only_glow else mats)


GT_N = 52
GT_SCALE = GT_N / 2.3
gt_base = {v: Hybrid(lambda v=v, **kw: graft_base(v, **kw), GT_N, 1, colors=36) for v in "ab"}
gt_head = {(v, rg): Hybrid(lambda v=v, rg=rg, **kw: graft_head(v, rg, **kw), GT_N, 32, colors=36)
           for v in "ab" for rg in (False, True)}
gt_root = {v: Hybrid(lambda v=v, **kw: graft_root(v, **kw), GT_N, 1, colors=32) for v in "ab"}


def gt_wreck(v):
    """The burst shell: the dome split in two halves thrown apart, scorched, the barrel fallen off beside it."""
    head = tint(gt_head[(v, False)].get(DOWN + 0.5, 0.2), (30, 24, 20), 0.55)
    w, h = head.size
    c = Image.new("RGBA", (GT_N + 12, GT_N + 12), (0, 0, 0, 0))
    lft, rgt = head.crop((0, 0, w // 2, h)), head.crop((w // 2, 0, w, h))
    c.alpha_composite(lft.rotate(-14, resample=Image.BICUBIC, expand=False), (0, 6))
    c.alpha_composite(rgt.rotate(16, resample=Image.BICUBIC, expand=False), (w // 2 + 12, 4))
    d = ImageDraw.Draw(c)
    d.line([c.width - 12, c.height - 8, c.width - 30, c.height - 18], fill=(70, 72, 78, 255), width=4)
    d.line([c.width - 12, c.height - 8, c.width - 30, c.height - 18], fill=(110, 112, 118, 255), width=1)
    return c


def gt_stump(v):
    base = tint(gt_base[v].get(DOWN, 0.0), (36, 28, 22), 0.6)
    d = ImageDraw.Draw(base)
    cx, cy = base.width / 2, base.height / 2
    rng = np.random.default_rng(5)
    for k in range(6):
        a = k * TAU / 6 + 0.2
        pts = [(cx, cy)]
        for j in range(1, 4):
            pts.append((cx + np.cos(a + rng.normal(0, 0.25)) * 6 * j, cy + np.sin(a + rng.normal(0, 0.25)) * 6 * j))
        d.line(pts, fill=(84, 74, 60, 255), width=1)
    d.ellipse([cx - 6, cy - 6, cx + 6, cy + 6], fill=(30, 24, 20, 255), outline=(70, 56, 44, 255))
    return base


def ring_light(img, x, y, r, col, k=1.0, w=1.1):
    """A thin smooth ring of light (additive, 1x)."""
    if r <= 0.3 or k <= 0:
        return img
    R = int(r + 4 * w + 2)
    box = (int(x - R), int(y - R), int(x + R + 1), int(y + R + 1))
    a = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    f = np.exp(-((np.hypot(xx - x, yy - y) - r) / w) ** 2) * k
    a[..., :3] += smooth_steps(np.clip(f, 0, 1.5) / 1.5)[..., None] * 1.5 * np.asarray(col, float)
    img.paste(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), img.mode), box)
    return img


GT_DUR = 11.0
GT_SPEED = 20.0
GT_WIN, GT_KNIT = 5.0, 0.8
GTS = [dict(x=140, y0=150, kill=None, root_kill=True), dict(x=330, y0=95, kill=None, root_kill=False),
       dict(x=250, y0=-30, kill=None, root_kill=False)]


def gt_scene(v):
    bg = Scroll(deck_bg, 371, GT_DUR, GT_SPEED, burnt=((250, 420),))
    py = 470

    def plan(t):
        if t < 2.3:
            return 140
        if t < 4.0:
            return 330
        return 230 + 50 * np.sin(t * 0.9)
    px_of = follow(plan, GT_DUR, 200, 220, 0.3)
    st = [dict(d, hp=24.0, root=16.0, state="turret", h=DOWN, t_state=0.0, paid=False) for d in GTS]
    fire, shots = Fire(rate=12), EShots()
    sparks, texts, frames = [], [], []
    for t in times(GT_DUR):
        f = bg.frame(t)
        px = px_of(t)
        pos = [(s["x"], s["y0"] + GT_SPEED * t) for s in st]
        for s, (x, y) in zip(st, pos):
            if s["state"] == "knit" and t - s["t_state"] >= GT_KNIT:
                s["state"], s["t_state"], s["hp"], s["regrown"] = "turret", t, 24.0, True
            if s["state"] == "root" and t - s["t_state"] >= GT_WIN:
                s["state"], s["t_state"] = "knit", t
            if s["state"] == "turret":
                want = np.arctan2(py - y, px - x)
                dh = (want - s["h"] + np.pi) % TAU - np.pi
                s["h"] += float(np.clip(dh, -np.radians(120) / FPS, np.radians(120) / FPS))
        fire_on = (0.9 < t < 2.3) or (2.7 < t < 3.7)

        def hit(bx, by, t=t):
            for s, (x, y) in zip(st, pos):
                if s["state"] == "turret" and abs(bx - x) < 16 and abs(by - y) < 16:
                    s["hp"] -= 3.0
                    sparks.append((t, bx, by, (255, 220, 160)))
                    if s["hp"] <= 0:
                        s["state"], s["t_state"] = "root", t
                        if not s.get("regrown"):
                            texts.append((t, x, y - 20, "+15"))
                    return True
                if s["state"] == "root" and abs(bx - x) < 10 and abs(by - y) < 10:
                    s["root"] -= 3.0
                    sparks.append((t, bx, by, TEAL))
                    if s["root"] <= 0:
                        s["state"], s["t_state"] = "stump", t
                        texts.append((t, x, y - 20, "+5"))
                    return True
            return False
        fire.step(t, px, py, hit, on=fire_on)
        for i, (s, (x, y)) in enumerate(zip(st, pos)):
            if s["state"] == "turret" and 0 < y < FH:
                age = t - (s["t_state"] if s.get("regrown") else -9)
                for tf in np.arange(0.8 + 0.7 * i, GT_DUR, 2.2):
                    if abs(t - tf) < 0.5 / FPS and age > 0.5 and y > 0:
                        for s_ in (-1, 1):
                            a = s["h"] + s_ * np.radians(2)
                            mx, my = x + np.cos(s["h"]) * 22, y + np.sin(s["h"]) * 22
                            shots.fire(t, mx, my, mx + np.cos(a) * 50, my + np.sin(a) * 50, 160, 4)
        # draw
        for i, (s, (x, y)) in enumerate(zip(st, pos)):
            if not -40 < y < FH + 40:
                continue
            u = t - s["t_state"]
            if s["state"] == "stump":
                sprite.paste_center(f, gt_stump(v), x, y)
                if u < 0.6:
                    f = light(f, x, y, 10, TEAL, 0.8 * (1 - u / 0.6))
                    f = flecks(f, x, y, u / 0.6, seed=11)
                continue
            sprite.paste_center(f, gt_base[v].get(DOWN, 0.6), x, y)
            if s["state"] == "turret":
                hd = gt_head[(v, bool(s.get("regrown")))]
                sp = hd.get(s["h"], 0.75 if s.get("regrown") else 0.6)
                sprite.paste_center(f, sprite.shadow_of(sp, 0.45, 1.2), x + 5, y + 6)
                sprite.paste_center(f, flash(sp, any(abs(t - st_) < 0.05 for st_, *_ in sparks[-4:])), x, y)
                for tf in np.arange(0.8 + 0.7 * i, GT_DUR, 2.2):
                    if tf - 0.25 < t < tf:
                        f = light(f, x + np.cos(s["h"]) * 23, y + np.sin(s["h"]) * 23, 4, (255, 60, 50), 1.3)
                if s.get("regrown") and u < 0.4:
                    f = light(f, x, y, 16, TEAL, 0.6 * (1 - u / 0.4))
            elif s["state"] == "root":
                hz = 1.0 + 3.0 * (u / GT_WIN)
                ph = (np.sin(TAU * hz * u) * 0.5 + 0.5)
                sprite.paste_center(f, gt_wreck(v), x, y)
                sprite.paste_center(f, gt_root[v].get(DOWN, 0.4 + 0.8 * ph), x, y)
                f = ring_light(f, x, y, 22 * (1 - u / GT_WIN), TEAL, 0.9)
                if u < 0.6:
                    f = boom(f, x, y, u / 0.6, "small")
            elif s["state"] == "knit":
                w = smooth(u / GT_KNIT)
                sprite.paste_center(f, fade(gt_wreck(v), 1 - w), x, y)
                sp = gt_head[(v, True)].get(s["h"], 1.2)
                sc = 0.6 + 0.4 * w
                sp = sp.resize((max(1, int(sp.width * sc)), max(1, int(sp.height * sc))), Image.BILINEAR)
                sprite.paste_center(f, fade(tint(sp, TEAL, 0.6 * (1 - w)), w), x, y)
                f = light(f, x, y, 14, TEAL, 0.7 * (1 - w))
        for t0, bx, by, col in sparks:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, col, 4, 6)
        for t0, x, y, txt in texts:
            if 0 <= t - t0 < 1.0:
                raster.draw_text(f, int(x - 6), int(y + GT_SPEED * (t - t0) - 18 * (t - t0)), txt, (255, 230, 140))
        fire.draw(f)
        shots.draw(f, t)
        ship(f, px, py, t)
        roots = [s for s in st if s["state"] == "root"]
        knits = [s for s in st if s["state"] == "knit" or (s["state"] == "turret" and s.get("regrown")
                                                           and t - s["t_state"] < 1.5)]
        if knits:
            caption(f, "THE WINDOW CLOSED: IT REGROWS (0.8 S), MORE TISSUE THAN METAL")
        elif any(s["state"] == "stump" and t - s["t_state"] < 1.5 for s in st):
            caption(f, "ROOT KILLED: GONE FOR GOOD (+5)")
        elif roots:
            caption(f, "TURRET DOWN: THE ROOT IS OPEN FOR 5 S (THE PULSE QUICKENS)")
        frames.append(f)
    print("graft", v, [(s["state"], round(s["t_state"], 2)) for s in st], flush=True)
    return frames, bg, st


def gt_sheet(v, frames, bg, st):
    head = gt_head[(v, False)]
    heads = [head.frame(k) for k in range(0, 32, 2)]

    def comp(state, k=0.45):
        c = Image.new("RGBA", (GT_N + 12, GT_N + 12), (0, 0, 0, 0))
        m = c.width / 2
        if state == "stump":
            sprite.paste_center(c, gt_stump(v), m, m)
            return c
        sprite.paste_center(c, gt_base[v].get(DOWN, 0.6), m, m)
        if state == "turret":
            sprite.paste_center(c, head.get(DOWN, 0.6), m, m)
        elif state == "regrown":
            sprite.paste_center(c, gt_head[(v, True)].get(DOWN, 0.75), m, m)
        elif state == "root":
            sprite.paste_center(c, gt_wreck(v), m, m)
            sprite.paste_center(c, gt_root[v].get(DOWN, k), m, m)
            c = ring_light(c, m, m, 14, TEAL, 0.9)
        elif state == "knit":
            sprite.paste_center(c, fade(gt_wreck(v), 0.5), m, m)
            sp = gt_head[(v, True)].get(DOWN, 1.2)
            sp = sp.resize((int(sp.width * 0.8), int(sp.height * 0.8)), Image.BILINEAR)
            sprite.paste_center(c, fade(tint(sp, TEAL, 0.3), 0.6), m, m)
        return c
    states = [comp("turret"), comp("root", 0.4), comp("root", 1.2), comp("knit"), comp("regrown"), comp("stump")]
    whole = comp("turret")
    reg = comp("regrown")
    kill_t = min([s["t_state"] for s in st if s["state"] == "stump"] or [2.0])
    back = max([s["t_state"] for s in st if s.get("regrown")] or [9.3])
    panels = [("SOURCE: TURRET ON ITS BASE, 260 PX",
               source_panel(lambda: ((lambda p: union(graft_base(v)[0](p), graft_head(v)[0](p))), graft_head(v)[1]))),
              ("TURRET: 16 OF ITS 32 HEADINGS (1X)", grid(heads, 8, 1)),
              ("CYCLE (2X): TURRET / ROOT DIM / ROOT PULSE / KNITTING / REGROWN / STUMP",
               row(states, GT_N + 12, 2, ["TURRET", "ROOT", "PULSE", "KNIT", "REGROWN", "STUMP"])),
              ("READABILITY: LEVEL 37'S DECK (BRIGHT PLATING, BURNT DECK)",
               bands_check([(whole, shares(whole)), (reg, "REGROWN " + shares(reg))], bg.img, names=("BRIGHT", "DARK"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.9, kill_t + 0.3, 6.0, min(GT_DUR - 0.1, back + 0.3)],
                                              ["ROOT OPEN", "ROOT KILLED", "WINDOW", "REGROWN"]))]
    if v == "a":
        notes = ["GRAFT TURRET A - METAL FIRST: AN ASCENDANCY DECK TURRET WITH TISSUE SCARS",
                 f"44X44 PX (52 PX CANVAS); TURRET 32 HEADINGS (120 DEG/S) ON A FIXED BASE (Q9 A HYBRID; {shares(whole)}).",
                 "GUNMETAL DOME, GOLD RING AND MUZZLE, RED SENSOR; THIN TEAL SCARS ON THE DOME AND THREE CRACKS IN THE",
                 "BASE. SHOT: A SMALL BURST, THE SHELL SPLITS, THE BARREL FALLS OFF AND THE ROOT (A TEAL KNOT) SHOWS",
                 "WITH A THIN TEAL RING SHRINKING OVER THE 5 S WINDOW AND A PULSE QUICKENING 1 -> 4 HZ. ROOT KILLED: A",
                 "FIBROUS CRUMBLE, A SCORCHED STUMP WITH WITHERED TENDRILS (+5). WINDOW CLOSED: TISSUE KNITS OVER THE",
                 "WRECK IN 0.8 S AND THE TURRET COMES BACK MOSTLY TISSUE, ITS BARREL BONE-WHITE AND VEINED; IT PAYS NOTHING."]
    else:
        notes = ["GRAFT TURRET B - TISSUE FIRST: THE TURRET SITS ON A FLESHY COLLAR WITH ROOTS ACROSS THE DECK",
                 f"THE SAME CYCLE ({shares(whole)}): A THIRD OF THE DOME OVERGROWN, A COLLAR AND SIX ROOTS REACHING PAST",
                 "THE MOUNT ONTO THE DECK, A BIGGER ROOT BULB. 'THIS ONE GROWS BACK' READS BEFORE THE FIRST SHOT, AT THE",
                 "COST OF A BUSIER DECK AND MORE TEAL NEAR THE GOLD PLATING. SAME SCENE AS A."]
    return sheet(f"GRAFT TURRET {v.upper()} - THE TURRET, THE ROOT, THE REGROWTH (LEVEL 37, AURELIA'S DECKS)", panels,
                 notes, frames[int(1.9 * FPS)])


@unit("graft-turret")
def make_graft(variants):
    for v in variants:
        frames, bg, st = gt_scene(v)
        save("enemies/ground/graft-turret", "graft-turret", v, gt_sheet(v, frames, bg, st), frames)


# ===================================================================== 5 Shield Pylon (L41)

A_LENS = 9


def pylon_base():
    """The fixed base (both variants): a gloss-black octagon with a heavy gold edge and red corner lights."""
    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        a = np.arctan2(p[:, 1], p[:, 0])
        sec = TAU / 8
        af = (a + sec / 2) % sec - sec / 2
        op = np.stack([r * np.cos(af), r * np.sin(af), p[:, 2]], axis=-1)
        d, m = union((sd_box(op, (0, 0, 0.06), (0.92, 0.5, 0.08), 0.02), A_HULL),
                     (sd_box(op, (0.86, 0, 0.16), (0.07, 0.36, 0.04), 0.01), A_GOLD),
                     (sd_sphere(op, (0.7, 0.29, 0.16), 0.05), A_RED))
        m = np.where((m == A_HULL) & (r > 0.66) & (r < 0.7), A_FACET, m)
        return d, m
    return scene, elite_mats("c8202a", red=1.3)


def pylon_crown(v, lens=1.0):
    """The mast and its emitter (+Y = toward its dome). a "crown": a short black mast ringed by a gold crown of
    six prongs, the emitter lens leaning out of the crown toward the dome; b "obelisk": a tall faceted black
    spire with four gold vanes down its edges and a gold emitter dish at its tip tilted toward the dome."""
    def scene(p):
        if v == "a":
            r = np.hypot(p[:, 0], p[:, 1])
            parts = [(sd_cylinder_z(p, (0, 0, 0.3), 0.26, 0.2), A_HULL),
                     (np.hypot(r - 0.44, p[:, 2] - 0.42) - 0.075, A_GOLD)]
            for k in range(6):
                t_ = k * TAU / 6 + np.pi / 6
                parts.append((sd_capsule(p, (0.44 * np.cos(t_), 0.44 * np.sin(t_), 0.42),
                                         (0.66 * np.cos(t_), 0.66 * np.sin(t_), 0.62), 0.06, 0.03), A_GOLD))
            parts += [(sd_capsule(p, (0, 0.0, 0.5), (0, 0.3, 0.66), 0.08, 0.06), A_GOLD),
                      (sd_sphere(p, (0, 0.34, 0.68), 0.13), A_LENS),
                      (sd_cylinder_z(p, (0, 0, 0.52), 0.2, 0.05), A_FACET)]
            return union(*parts, k=0.02)
        q = mirror_x(p)
        dia = [(0.0, 0.5), (0.5, 0.0), (0.0, -0.5), (-0.5, 0.0)]
        spire = sd_plate(p, dia, 0.45, 0.45, 0.0, taper=lambda x, y: np.clip(1.0 - 1.5 * (np.abs(x) + np.abs(y)),
                                                                                0.1, 1.0))
        vane = np.minimum(sd_box(rotate_z(p, np.pi / 4), (0, 0, 0.4), (0.62, 0.03, 0.3), 0.01),
                          sd_box(rotate_z(p, -np.pi / 4), (0, 0, 0.4), (0.62, 0.03, 0.3), 0.01))
        d, m = union((spire, A_HULL), (vane, A_GOLD),
                     (sd_ellipsoid(p, (0, 0.16, 0.86), (0.24, 0.2, 0.06)), A_GOLD),
                     (sd_sphere(p, (0, 0.2, 0.9), 0.09), A_LENS), k=0.01)
        m = np.where((m == A_HULL) & (q[:, 0] + np.abs(q[:, 1]) < 0.18), A_FACET, m)
        return d, m
    lens_m = emissive((1.0, 0.86, 0.5), 0.4 + 1.6 * lens)
    return scene, elite_mats("c8202a", red=1.3, extra=[Material((0, 0, 0)), Material((0, 0, 0)), lens_m])


PY_N = 64
PY_SCALE = PY_N / 2.3
py_base = rig.AngleSprites(lambda: pylon_base(), PY_N, 1, factor=4, colors=36, post=ELITE_RIM)
py_crown = {v: Turning(lambda v=v, **kw: pylon_crown(v, **kw), PY_N, 16, factor=4, colors=40, post=ELITE_RIM)
            for v in "ab"}
DOME_R, DOME_D = 110, 140
LENS_LOCAL = {"a": (0.0, 0.34), "b": (0.0, 0.2)}


def pylon_sprite(v, hd, lens=1.0):
    c = py_base.get(0.0).copy()
    c.alpha_composite(py_crown[v].get(hd, lens=lens))
    return c


def hex_dist(x, y, cell):
    """Distance to the nearest edge of a pointy-top hex lattice of circumradius ``cell``."""
    qf = (np.sqrt(3) / 3 * x - y / 3) / cell
    rf = (2 / 3 * y) / cell
    xf, zf = qf, rf
    yf = -xf - zf
    rx, ry, rz = np.round(xf), np.round(yf), np.round(zf)
    dx, dy, dz = np.abs(rx - xf), np.abs(ry - yf), np.abs(rz - zf)
    rx = np.where((dx > dy) & (dx > dz), -ry - rz, rx)
    rz = np.where(~((dx > dy) & (dx > dz)) & ~(dy > dz), -rx - ry, rz)
    cx = cell * np.sqrt(3) * (rx + rz / 2)
    cy = cell * 1.5 * rz
    lx, ly = x - cx, y - cy
    n = np.maximum(np.abs(lx), np.maximum(np.abs(0.5 * lx + 0.866 * ly), np.abs(0.5 * lx - 0.866 * ly)))
    return cell * np.sqrt(3) / 2 - n


def dome(img, cx, cy, v, t, k=1.0, R=DOME_R):
    """The gold hex dome (additive, smooth, drawn once): a thin lattice at about 20 % with a brighter rim and a
    slow shimmer running over it; b fades the lattice toward the middle (the production hex shield's falloff)."""
    if k <= 0:
        return img
    box = (int(cx - R - 4), int(cy - R - 4), int(cx + R + 5), int(cy + R + 5))
    a = np.array(img.crop(box).convert("RGBA")).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    x, y = xx - cx, yy - cy
    r = np.hypot(x, y)
    inside = np.clip(R - r + 0.5, 0, 1)
    hexl = np.exp(-(hex_dist(x, y, 9.0) / 0.7) ** 2)
    if v == "b":
        hexl = hexl * (0.35 + 0.65 * (r / R) ** 2)
    shimmer = 0.6 + 0.8 * np.exp(-(((x * 0.8 + y * 0.6) - ((t * 60) % (3 * R) - 1.5 * R)) / 28) ** 2)
    f = (0.05 + 0.22 * hexl * shimmer) * inside + 0.9 * np.exp(-((r - R) / 1.6) ** 2)
    f = smooth_steps(np.clip(f * k, 0, 1))[..., None] * np.asarray((255, 206, 90), float)[None, None, :] / 255.0
    a[..., :3] = np.clip(a[..., :3] + f * 255 * 0.9, 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), "RGBA"), box)
    return img


def beam_light(img, x0, y0, x1, y1, v, t, k=1.0):
    """The feed from the crown to its dome's rim: a a solid thin gold beam; b a faint line with packets
    flowing to the dome."""
    pad = 8
    box = (int(min(x0, x1) - pad), int(min(y0, y1) - pad), int(max(x0, x1) + pad + 1), int(max(y0, y1) + pad + 1))
    a = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    bx, by = x1 - x0, y1 - y0
    L2 = bx * bx + by * by
    h = np.clip(((xx - x0) * bx + (yy - y0) * by) / L2, 0, 1)
    d = np.hypot(xx - x0 - bx * h, yy - y0 - by * h)
    if v == "a":
        f = np.exp(-(d / 0.8) ** 2) * 1.0 + np.exp(-(d / 2.6) ** 2) * 0.35
    else:
        L = np.sqrt(L2)
        s = h * L
        pk = np.exp(-(((s - t * 120) % 18 - 9) / 2.2) ** 2)
        f = np.exp(-(d / 0.7) ** 2) * 0.35 + np.exp(-(d / 1.8) ** 2) * pk * 1.3
    f = smooth_steps(np.clip(f * k, 0, 1.4) / 1.4) * 1.4
    a[..., :3] = np.clip(a[..., :3] + f[..., None] * np.array((255, 214, 120)), 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), img.mode), box)
    return img


def deck_gun(aim):
    """A covered fortress gun: the Halo Platform's turret head (m6.halo_turret) in the elite livery on a small
    black base with a gold ring."""
    return py_gun.get(aim)


def gun_model():
    scene0, _ = m6.halo_turret()
    return scene0, elite_mats("c8202a", red=1.3)


py_gun = Turning(lambda: gun_model(), 40, 32, factor=5, colors=32, post=ELITE_RIM)


def gun_sprite(aim):
    c = Image.new("RGBA", (40, 40), (0, 0, 0, 0))
    d = ImageDraw.Draw(c)
    d.ellipse([6, 6, 34, 34], fill=(22, 20, 28, 255), outline=(200, 160, 60, 255), width=2)
    c.alpha_composite(py_gun.get(aim))
    return c


def covered(sp, t):
    """A covered unit: a faint gold hex sheen over its sprite."""
    a = np.array(sp).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    yy, xx = np.mgrid[0:h, 0:w] + 0.5
    hexl = np.exp(-(hex_dist(xx - w / 2, yy - h / 2, 5.0) / 0.6) ** 2)
    k = smooth_steps(0.12 + 0.25 * hexl * (0.7 + 0.3 * np.sin(t * 3)))
    a[op, :3] = np.clip(a[op, :3] * (1 - 0.15) + np.array((255, 214, 120)) * k[op, None], 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


PY_DUR = 12.0
PY_HALT0, PY_HALT1 = 1.6, 2.8          # the scroll eases to a halt
PY_OPEN = None                          # set in the scene: when the last pylon falls


def py_offset_fn(open_t):
    def speed(t):
        sp = 30.0
        if t >= PY_HALT0:
            sp *= 1 - smooth((t - PY_HALT0) / (PY_HALT1 - PY_HALT0))
        if open_t is not None and t >= open_t:
            sp = 30.0 * smooth((t - open_t) / 2.0)
        return sp
    ts = np.arange(0, PY_DUR + 1, 1 / 200)
    cum = np.concatenate([[0.0], np.cumsum([speed(t) for t in ts[:-1]]) / 200])
    return lambda t: float(np.interp(t, ts, cum))


PYLONS = [dict(x=92, y=150, face=np.radians(14)), dict(x=396, y=300, face=np.radians(198))]
COVER = [dict(kind="bunker", x=228, y=176), dict(kind="gun", x=214, y=262), dict(kind="gun", x=318, y=226)]
PY_KILLS = [7.0, 9.2]                   # the pylons' falls (the ship's fire is timed for them)


def py_scene(v):
    open_t = PY_KILLS[-1]
    off = py_offset_fn(open_t)
    halt = off(PY_KILLS[0] - 0.5)
    bg = Scroll(ice_bg, 41, PY_DUR, offset=off)
    py = 470

    def plan(t):
        if t < 5.2:
            return 228
        if t < 7.4:
            return PYLONS[0]["x"]
        if t < 9.6:
            return PYLONS[1]["x"]
        return 240
    px_of = follow(plan, PY_DUR, 240, 230, 0.3)
    fire, shots = Fire(rate=12), EShots()
    pyl = [dict(p, hits=0, dead=None) for p in PYLONS]
    cov = [dict(c, hits=0) for c in COVER]
    sparks, frames = [], []
    for t in times(PY_DUR):
        f = bg.frame(t)
        dy = off(t) - halt
        px = px_of(t)
        domes = []
        for i, p in enumerate(pyl):
            x, y = p["x"], p["y"] + dy
            cxd, cyd = x + np.cos(p["face"]) * DOME_D, y + np.sin(p["face"]) * DOME_D
            k = 1.0 if p["dead"] is None else 0.0
            domes.append((cxd, cyd, k, p))

        def is_cov(x, y):
            return any(k > 0 and np.hypot(x - cxd, y - cyd) < DOME_R for cxd, cyd, k, _ in domes)

        def hit(bx, by, t=t):
            for p in pyl:
                x, y = p["x"], p["y"] + dy
                if p["dead"] is None and abs(bx - x) < 20 and abs(by - y) < 20:
                    p["hits"] += 1
                    sparks.append((t, bx, by, (255, 220, 160)))
                    if t >= PY_KILLS[pyl.index(p)]:
                        p["dead"] = t
                    return True
            for c in cov:
                x, y = c["x"], c["y"] + dy
                rr = 34 if c["kind"] == "bunker" else 14
                if abs(bx - x) < rr and abs(by - y) < rr:
                    if is_cov(x, y):
                        sparks.append((t, bx, by, (255, 206, 90)))
                    else:
                        c["hits"] += 1
                        sparks.append((t, bx, by, (255, 230, 170)))
                    return True
            return False
        fire.step(t, px, py, hit, on=(3.0 < t < 4.8) or (5.6 < t < 7.4) or (7.8 < t < 9.6))
        for j, c in enumerate(cov):
            if c["kind"] == "gun":
                for tf in np.arange(2.0 + 0.9 * j, PY_DUR, 2.4):
                    if abs(t - tf) < 0.5 / FPS:
                        x, y = c["x"], c["y"] + dy
                        a = np.arctan2(py - y, px - x)
                        shots.fire(t, x + np.cos(a) * 14, y + np.sin(a) * 14, x + np.cos(a) * 60, y + np.sin(a) * 60,
                                   160, 5)
        # draw: the covered units, the domes, the pylons
        for c in cov:
            x, y = c["x"], c["y"] + dy
            if c["kind"] == "bunker":
                aim = np.arctan2(py - y, px - x)
                sp = e60.bunker(round(aim / (TAU / 8)) * (TAU / 8))        # covered: its aim in 8 steps
            else:
                sp = gun_sprite(np.arctan2(py - y, px - x))
            if is_cov(x, y):
                sp = covered(sp, t)
            sprite.paste_center(f, sprite.shadow_of(sp, 0.5, 1.4), x + 6, y + 8)
            sprite.paste_center(f, sp, x, y)
        for cxd, cyd, k, p in domes:
            x, y = p["x"], p["y"] + dy
            if p["dead"] is None:
                f = dome(f, cxd, cyd, v, t, 1.0)
                lx, ly = rig.local_to_screen(*LENS_LOCAL[v], p["face"], PY_SCALE)
                ex, ey = x + lx, y + ly
                rx, ry = cxd - np.cos(p["face"]) * DOME_R, cyd - np.sin(p["face"]) * DOME_R
                f = beam_light(f, ex, ey, rx, ry, v, t)
            else:
                u = (t - p["dead"]) / 0.4
                if u < 1:
                    mx, my = cxd + (x - cxd) * u, cyd + (y - cyd) * u
                    f = ring_light(f, mx, my, DOME_R * (1 - u), (255, 206, 90), 1.0 * (1 - 0.5 * u), 1.4)
        for p in pyl:
            x, y = p["x"], p["y"] + dy
            if p["dead"] is None:
                sp = pylon_sprite(v, p["face"], 0.7 + 0.3 * np.sin(t * 5))
                sprite.paste_center(f, sprite.shadow_of(sp, 0.5, 1.4), x + 8, y + 10)
                sprite.paste_center(f, sp, x, y)
                lx, ly = rig.local_to_screen(*LENS_LOCAL[v], p["face"], PY_SCALE)
                f = light(f, x + lx, y + ly, 5, (255, 214, 120), 0.8 + 0.3 * np.sin(t * 5))
            else:
                u = t - p["dead"]
                base = tint(py_base.get(0.0), (24, 18, 16), 0.6)
                sprite.paste_center(f, base, x, y)
                crown = tint(py_crown[v].get(p["face"] + 1.0), (30, 22, 18), 0.45)
                crown = crown.rotate(-min(1.0, u / 0.5) * 75, resample=Image.BICUBIC)
                sprite.paste_center(f, crown, x + 12 * min(1, u / 0.5), y + 8 * min(1, u / 0.5))
                f = boom(f, x, y, u / 0.8, "medium")
        for t0, bx, by, col in sparks:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, col, 5, 7)
        fire.draw(f)
        shots.draw(f, t)
        ship(f, px, py, t)
        n = sum(1 for p in pyl if p["dead"] is not None)
        if 2.4 < t < open_t + 1.2:
            txt = f"PYLONS {n}/2"
            w = raster.text_width(txt, 2) + 14
            d = ImageDraw.Draw(f)
            d.rectangle([FW - w - 10, 8, FW - 10, 28], fill=(10, 12, 20, 210), outline=(200, 160, 60, 255))
            raster.draw_text(f, FW - w - 3, 12, txt, (255, 214, 120), scale=2)
        if 3.0 < t < 5.0:
            caption(f, "COVERED: SHOTS SPEND ON THE DOME")
        elif 5.4 < t < PY_KILLS[0]:
            caption(f, "THE PYLON IS NOT COVERED: BREAK IT")
        elif PY_KILLS[0] <= t < PY_KILLS[0] + 1.4:
            caption(f, "PYLON DOWN: ITS DOME COLLAPSES INTO THE CROWN")
        elif open_t + 0.2 < t:
            caption(f, "THE GATE OPENS: THE SCROLL RESUMES")
        frames.append(f)
    print("pylon", v, [(p["hits"], p["dead"]) for p in pyl], flush=True)
    return frames, bg


def py_sheet(v, frames, bg):
    heads = [pylon_sprite(v, k * TAU / 16) for k in range(16)]
    main = pylon_sprite(v, DOWN)
    dead = Image.new("RGBA", (PY_N, PY_N), (0, 0, 0, 0))
    sprite.paste_center(dead, tint(py_base.get(0.0), (24, 18, 16), 0.6), PY_N / 2, PY_N / 2)
    cr = tint(py_crown[v].get(DOWN + 1.0), (30, 22, 18), 0.45).rotate(-75, resample=Image.BICUBIC)
    sprite.paste_center(dead, cr, PY_N / 2 + 12, PY_N / 2 + 8)
    demo = Image.new("RGBA", (300, 300), (16, 18, 24, 255))
    demo = window(bg.img, 300, 300, "dark").copy()
    gun = covered(gun_sprite(DOWN), 0.0)
    sprite.paste_center(demo, gun, 210, 190)
    demo = dome(demo, 190, 170, v, 1.0)
    demo = beam_light(demo, 64, 160 - 6, 190 - DOME_R + 1, 170, v, 1.0)
    sprite.paste_center(demo, pylon_sprite(v, 0.0), 40, 160)
    panels = [("SOURCE: 260 PX", source_panel(lambda: ((lambda p: union(pylon_base()[0](p), pylon_crown(v)[0](p))),
                                                       pylon_crown(v)[1]))),
              ("THE CROWN'S 16 FACINGS (1X)", grid(heads, 8, 1)),
              ("PYLON / WRECK / PLAYER (2X)", row([main, dead, e3.player_sprite()], PY_N, 2, ["PYLON", "WRECK", "PLAYER"])),
              ("THE DOME AND ITS FEED OVER THE DARKEST ICE (1X)", demo),
              ("READABILITY: LEVEL 41 (SUNLIT ICE, CITADEL DECK)",
               bands_check([(main, shares(main, False))], bg.img, names=("ICE", "DECK"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [4.0, 6.6, PY_KILLS[0] + 0.2, PY_KILLS[1] + 1.4],
                                              ["COVERED", "THE PYLON", "COLLAPSE", "GATE OPEN"]))]
    if v == "a":
        notes = ["SHIELD PYLON A - THE CROWN: A GOLD CROWN OF SIX PRONGS ON A SHORT BLACK MAST, A SOLID FEED BEAM",
                 f"56X56 PX (64 PX CANVAS), THE BASE FIXED, THE CROWN TURNED TO ITS DOME (16 FACINGS); ELITE LIVERY (Q9 A:",
                 f"GLOSS BLACK, HEAVY GOLD, RED CORNER LIGHTS, THE STRONG 2 PX GOLD RIM; {shares(main, False)}). THE EMITTER",
                 "LENS LEANS OUT OF THE CROWN TOWARD THE DOME; A SOLID THIN GOLD BEAM JOINS IT TO THE DOME'S RIM. THE DOME:",
                 "R 110 PX, 140 PX OUT, A THIN EVEN GOLD HEX LATTICE AT ABOUT 20 % WITH A BRIGHT RIM AND A SLOW SHIMMER",
                 "(SMOOTH, DRAWN ONCE). COVERED UNITS WEAR A FAINT GOLD HEX SHEEN; A SHOT ON ONE SPENDS IN A GOLD SPARK.",
                 "DEATH: THE CROWN TOPPLES, THE DOME COLLAPSES INTO IT AS A SHRINKING GOLD RING (0.4 S); PYLONS N/M COUNTS."]
    else:
        notes = ["SHIELD PYLON B - THE OBELISK: A TALL FACETED BLACK SPIRE WITH GOLD VANES, A FEED OF FLOWING PACKETS",
                 f"THE SAME BASE AND RULES ({shares(main, False)}): THE SPIRE READS TALLER (A MAST, NOT A TURRET), ITS GOLD",
                 "DISH AT THE TIP; THE FEED IS A FAINT LINE WITH BRIGHT PACKETS RUNNING TO THE DOME (WHO FEEDS WHICH DOME",
                 "READS AS A DIRECTION). ITS DOME'S LATTICE FADES TOWARD THE MIDDLE (THE PRODUCTION HEX SHIELD'S FALLOFF),",
                 "SO THE COVERED GUNS STAY CLEAR. SAME SCENE AS A."]
    return sheet(f"SHIELD PYLON {v.upper()} - THE DOME AND THE GATE (LEVEL 41, CALLISTO FORTRESS)", panels, notes,
                 frames[int(4.0 * FPS)])


@unit("shield-pylon")
def make_pylon(variants):
    for v in variants:
        frames, bg = py_scene(v)
        save("enemies/ground/shield-pylon", "shield-pylon", v, py_sheet(v, frames, bg), frames)


# ===================================================================== 6 Halo Platform (L41, J8)

HALO_PXU = 110 / 2.3                   # round 06/07's px per model unit
HALO_R = 96                            # the turrets' ring radius in px (r07)
RING_N, RING_STEPS, RING_FACTOR = 260, 128, 4
RING_STEP = m7.SEC / RING_STEPS


def halo_segment_v(v):
    """The r07 ring segment (m6.halo_segment, unchanged geometry) in the elite livery. a "gold trim": gloss black
    hull, the r07 gold edge rails, the turret mount rings and the end caps gold; b "gold decks": the segments'
    top decks gold between black rails, the mount rings black."""
    seg, _ = m6.halo_segment()

    def scene(p):
        d, m = seg(p)
        if v == "a":
            m = np.where((m == A_FACET) | (m == A_GUN), A_GOLD, m)
        else:
            m = np.where(m == A_GOLD, A_FACET, m)
            bent_x = p[:, 0] - 0.25 * p[:, 1] ** 2
            m = np.where((m == A_HULL) & (p[:, 2] > 0.18) & (np.abs(bent_x) < 0.17), A_GOLD, m)
        return d, m
    return scene, elite_mats("6a6e78")


def halo_ring_v(v, R):
    """``m7.halo_ring`` with the livery's segment: six segments, one 6-fold model."""
    seg, mats = halo_segment_v(v)

    def scene(p):
        k0, k1 = m7._nearest_sectors(p)
        return union(seg(m7._sector_local(p, R, k0)), seg(m7._sector_local(p, R, k1)), k=0.0)

    def local_pattern(fn):
        if fn is None:
            return None

        def f(p, n):
            k0, _ = m7._nearest_sectors(p)
            return fn(m7._sector_local(p, R, k0), m7._rot_normals(n, k0))
        return f
    mats = [replace(m, pattern=local_pattern(m.pattern), emission_pattern=local_pattern(m.emission_pattern))
            for m in mats]
    return scene, mats


def _halo_ring_worker(job):
    v, k = job
    path = CACHE / f"halo-ring-r72-{v}-{RING_STEPS}-{RING_FACTOR}-{k:03d}.png"
    if path.exists():
        return job, np.array(Image.open(path).convert("RGBA"))
    scene0, mats = halo_ring_v(v, HALO_R / HALO_PXU)
    rot = k * RING_STEP
    scene = lambda p: scene0(sdf.rotate_z(p, rot))  # noqa: E731
    hi = sdf.render(scene, rig.model_space_materials(mats, rot), (RING_N * RING_FACTOR, RING_N * RING_FACTOR),
                    RING_N / HALO_PXU)
    img = sprite.sharpen(sprite.to_image(sprite.downsample(hi, RING_FACTOR), 0.5), 90)
    img = rim_raw_strong(img)
    img.save(path)
    return job, np.array(img)


def rim_raw_strong(sp):
    """The elite rim (gold toward the top-left sun, 2 px; red on the far edges) without a palette step (the ring
    shares one palette over all its frames)."""
    a = np.array(sp)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
    inner = op & ~tl & ~br & (np.roll(tl, 1, axis=1) | np.roll(tl, 1, axis=0))
    out = a.astype(np.float64)
    out[tl, :3] = out[tl, :3] * 0.12 + e60.GOLD_HI * 0.88
    out[inner, :3] = out[inner, :3] * 0.55 + e60.GOLD_RIM * 0.45
    out[br, :3] = out[br, :3] * 0.45 + e60.RED_RIM * 0.55
    return Image.fromarray(out.astype(np.uint8), "RGBA")


_HALO_RING = {}


def halo_ring_frames(v):
    """The 128 ring frames of a variant, rendered in parallel and cut to one shared 48-colour palette."""
    if v in _HALO_RING:
        return _HALO_RING[v]
    import enemies_r07 as e7
    CACHE.mkdir(parents=True, exist_ok=True)
    t0 = time.time()
    workers = max(1, min(18, (os.cpu_count() or 4) - 2))
    with ProcessPoolExecutor(workers, mp_context=get_context("fork")) as pool:
        raw = dict(pool.map(_halo_ring_worker, [(v, k) for k in range(RING_STEPS)]))
    imgs = [Image.fromarray(raw[(v, k)], "RGBA") for k in range(RING_STEPS)]
    pal = e7.shared_palette([imgs[k] for k in range(0, RING_STEPS, RING_STEPS // 4)], 48)
    _HALO_RING[v] = [e7.quantize_to(im, pal) for im in imgs]
    print(f"halo ring {v}: {RING_STEPS} frames in {time.time() - t0:.0f} s ({workers} processes)", flush=True)
    return _HALO_RING[v]


def halo_ring_index(rot):
    k = int(round(((-rot) % m7.SEC) / RING_STEP)) % RING_STEPS
    return k, -k * RING_STEP


def halo_core_v(v, eye=0.0):
    scene0, _ = m6.halo_core()

    def scene(p):
        d, m = scene0(p)
        if v == "b":
            m = np.where(m == A_FACET, A_GOLD, m)
        return d, m
    mats = elite_mats("6a6e78")
    mats[A_RED] = Material((0.05, 0.02, 0.02), emission=tuple(np.array((1.0, 0.25, 0.15)) * (1.6 + 2.5 * eye)))
    return scene, mats


def halo_gun_v(v):
    scene0, _ = m6.halo_turret()

    def scene(p):
        d, m = scene0(p)
        if v == "b":
            m = np.where(m == A_FACET, A_GOLD, m)
        return d, m
    return scene, elite_mats("6a6e78")


def halo_rail_v(v):
    """The rail turret (new with the draft: three of the six mounts): the same armoured head with one long rail
    barrel ringed by three gold coils."""
    def scene(p):
        d, m = union((sd_ellipsoid(p, (0, -0.05, 0.3), (0.36, 0.4, 0.2)), A_FACET),
                     (sd_box(p, (0, -0.32, 0.28), (0.22, 0.12, 0.12), 0.03), A_HULL),
                     (sd_capsule(p, (0, 0.05, 0.38), (0, 1.08, 0.38), 0.075, 0.065), A_GUN),
                     (sd_sphere(p, (0, 0.12, 0.5), 0.07), A_RED), k=0.02)
        coils = [(sd_cylinder_y(p, (0, 0.45 + 0.2 * j, 0.38), 0.11, 0.03), A_GOLD) for j in range(3)]
        d, m = union((d, m), *coils)
        if v == "b":
            m = np.where(m == A_FACET, A_GOLD, m)
        return d, m
    return scene, elite_mats("6a6e78")


HALO_TUR_N = 36
halo_core_s = {(v, e): rig.ModelSpaceAngleSprites(lambda v=v, e=e: halo_core_v(v, e), 104, 1, factor=4, colors=40,
                                                  post=ELITE_RIM) for v in "ab" for e in (0.0, 1.0)}
halo_tur_s = {(v, kind): rig.ModelSpaceAngleSprites((lambda v=v: halo_gun_v(v)) if kind == "gun" else
                                                    (lambda v=v: halo_rail_v(v)), HALO_TUR_N, 64, factor=6,
                                                    colors=28, post=ELITE_RIM)
              for v in "ab" for kind in ("gun", "rail")}


def barrier(img, cx, cy, t, k=1.0, R=46):
    """The core's red hex barrier: a thin red lattice at about 25 % with a brighter rim, smooth, drawn once."""
    if k <= 0:
        return img
    box = (int(cx - R - 4), int(cy - R - 4), int(cx + R + 5), int(cy + R + 5))
    a = np.array(img.crop(box).convert("RGBA")).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    x, y = xx - cx, yy - cy
    c, s = np.cos(t * 0.3), np.sin(t * 0.3)
    r = np.hypot(x, y)
    hexl = np.exp(-(hex_dist(c * x - s * y, s * x + c * y, 7.0) / 0.7) ** 2)
    f = (0.06 + 0.28 * hexl) * np.clip(R - r + 0.5, 0, 1) + 0.9 * np.exp(-((r - R) / 1.4) ** 2)
    f = smooth_steps(np.clip(f * k, 0, 1))[..., None]
    a[..., :3] = np.clip(a[..., :3] + f * np.array((255, 70, 50)) * 0.95, 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), "RGBA"), box)
    return img


def arc_band(img, cx, cy, bearing, k=1.0, r0=75, r1=118, half=np.radians(60)):
    """The live arc drawn on the ring's hull: a faint red band over the 120 deg facing the ship (smooth)."""
    box = (int(cx - r1 - 2), int(cy - r1 - 2), int(cx + r1 + 3), int(cy + r1 + 3))
    a = np.array(img.crop(box).convert("RGBA")).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    x, y = xx - cx, yy - cy
    r = np.hypot(x, y)
    off = np.abs((np.arctan2(y, x) - bearing + np.pi) % TAU - np.pi)
    f = np.clip((half - off) / np.radians(8), 0, 1) * np.clip(np.minimum(r - r0, r1 - r) / 6, 0, 1)
    f = smooth_steps(f * 0.2 * k)[..., None]
    a[..., :3] = np.clip(a[..., :3] + f * np.array((255, 60, 40)), 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), "RGBA"), box)
    return img


def red_line(img, x0, y0, ang, live=False, k=1.0):
    """The rail turret's laser-line telegraph (thin, smooth) or its 8 px live band."""
    L = 700
    x1, y1 = x0 + np.cos(ang) * L, y0 + np.sin(ang) * L
    box = (0, 0, img.width, img.height)
    a = np.array(img).astype(np.float64)
    yy, xx = np.mgrid[0:img.height, 0:img.width] + 0.5
    bx, by = x1 - x0, y1 - y0
    h = np.clip(((xx - x0) * bx + (yy - y0) * by) / (bx * bx + by * by), 0, 1)
    d = np.hypot(xx - x0 - bx * h, yy - y0 - by * h)
    if live:
        f = np.clip(4.5 - d, 0, 1) * 1.0 + np.exp(-(d / 7) ** 2) * 0.5
        col = np.array((255, 230, 220))
    else:
        f = np.exp(-(d / 0.8) ** 2) * 0.9 + np.exp(-(d / 2.5) ** 2) * 0.25
        col = np.array((255, 50, 40))
    f = smooth_steps(np.clip(f * k, 0, 1))[..., None]
    a[..., :3] = np.clip(a[..., :3] + f * col, 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), "RGBA"), box)
    return img


HA_DUR = 11.0
HA_C = (240, 190)
HA_KILLS = {0: 2.4, 1: 4.4, 3: 6.2}     # mount -> its fall (the third opens the core)
HA_BASE, HA_UP = np.radians(20), np.radians(8)


def ha_speed(t):
    return HA_BASE + HA_UP * sum(smooth((t - k) / 0.8) for k in HA_KILLS.values())


_ha_ts = np.arange(0, HA_DUR + 1, 1 / 600)
_ha_ang = np.concatenate([[0.0], np.cumsum([ha_speed(t) for t in _ha_ts[:-1]]) / 600])


def ha_angle(t):
    return float(np.interp(t, _ha_ts, _ha_ang))


def ha_scene(v):
    ring = halo_ring_frames(v)
    bg = Scroll(lambda seed, h: ice_bg(seed, h, deck=(14, 466)), 411, HA_DUR, 0.0)
    cx, cy = HA_C
    py = 470
    px_of = follow(lambda t: 240 + 120 * np.sin(t * 0.55 + 0.3), HA_DUR, 270, 160, 0.4)
    fire, shots, ring_shots = Fire(rate=12), EShots(), EShots()
    sparks, frames = [], []
    dead = {}
    bar_drop = HA_KILLS[3]
    lines = []                                   # (t0, mount, angle)
    last_line = -9.0
    eye_flares = [8.0, 10.0]
    for eye in eye_flares:
        ring_shots.ring(eye + 0.5, cx, cy, 10, 100, 4, phase=0.2)
    for t in times(HA_DUR):
        f = bg.frame(t)
        px = px_of(t)
        rot = ha_angle(t)
        k, rshow = halo_ring_index(rot)
        bearing = np.arctan2(py - cy, px - cx)
        mounts = []
        for j in range(6):
            a = rshow + j * TAU / 6
            x, y = cx + np.cos(a) * HALO_R, cy + np.sin(a) * HALO_R
            off = abs((a - bearing + np.pi) % TAU - np.pi)
            mounts.append((j, a, x, y, off <= np.radians(60)))

        def hit(bx, by, t=t):
            for j, a, x, y, live in mounts:
                if j not in dead and abs(bx - x) < 15 and abs(by - y) < 15:
                    sparks.append((t, bx, by, (255, 220, 160)))
                    if j in HA_KILLS and t >= HA_KILLS[j]:
                        dead[j] = t
                    return True
            if np.hypot(bx - cx, by - cy) < 44:
                sparks.append((t, bx, by, (255, 90, 70) if t < bar_drop else (255, 230, 170)))
                return True
            return False
        fire.step(t, px, py, hit)
        for j, a, x, y, live in mounts:
            if j in dead or not live:
                continue
            if j % 2 == 0:
                for tf in np.arange(1.0 + 0.8 * j, HA_DUR, 2.4):
                    for q in range(3):
                        if abs(t - (tf + 0.12 * q)) < 0.5 / FPS:
                            ang = np.arctan2(py - y, px - x)
                            shots.fire(t, x + np.cos(ang) * 16, y + np.sin(ang) * 16, px, py, 160, 5)
            elif t - last_line >= 2.5 and t > 1.5 and abs(px - x) > 60:
                lines.append((t, j, np.arctan2(py - y, px - x + 30)))
                last_line = t
        # draw
        eye = 1.0 if any(e <= t < e + 0.5 for e in eye_flares) else 0.0
        core = halo_core_s[(v, eye)].get(0.0)
        sprite.paste_center(f, sprite.shadow_of(core, 0.5, 1.5), cx + 6, cy + 8)
        sprite.paste_center(f, core, cx, cy)
        sprite.paste_center(f, sprite.shadow_of(ring[k], 0.5, 1.5), cx + 8, cy + 10)
        sprite.paste_center(f, ring[k], cx, cy)
        f = arc_band(f, cx, cy, bearing)
        if t < bar_drop + 0.5:
            f = barrier(f, cx, cy, t, 1.0 if t < bar_drop else 1 - (t - bar_drop) / 0.5)
        for j, a, x, y, live in mounts:
            if j in dead:
                u = (t - dead[j]) / 0.7
                d = ImageDraw.Draw(f)
                d.ellipse([x - 9, y - 9, x + 9, y + 9], fill=(20, 14, 18, 255), outline=(90, 50, 24, 255))
                f = boom(f, x, y, u, "medium")
                continue
            kind = "gun" if j % 2 == 0 else "rail"
            aim = np.arctan2(py - y, px - x)
            sp = halo_tur_s[(v, kind)].get(aim)
            sprite.paste_center(f, sp, x, y)
            lx, ly = x - np.cos(a) * 12, y - np.sin(a) * 12
            f = light(f, lx, ly, 2.6, (255, 60, 40) if live else (255, 200, 90), 1.3)
            if kind == "gun":
                for tf in np.arange(1.0 + 0.8 * j, HA_DUR, 2.4):
                    if tf - 0.3 < t < tf and live:
                        f = light(f, x + np.cos(aim) * 16, y + np.sin(aim) * 16, 4, (255, 120, 60), 1.4)
        for t0, j, ang in lines:
            if j in dead:
                continue
            u = t - t0
            jj = [m_ for m_ in mounts if m_[0] == j][0]
            if 0 <= u < 1.0:
                f = red_line(f, jj[2], jj[3], ang, False, 0.6 + 0.4 * (int(t * 10) % 2))
            elif 1.0 <= u < 1.2:
                f = red_line(f, jj[2], jj[3], ang, True)
        if t >= bar_drop:
            for e in eye_flares:
                if e <= t < e + 0.5:
                    f = light(f, cx, cy - 4, 12, (255, 240, 220), 1.2)
            ring_shots.draw(f, t)
        for t0, bx, by, col in sparks:
            if 0 <= t - t0 < 0.15:
                f = spark(f, bx, by, (t - t0) / 0.15, col, 4, 6)
        fire.draw(f)
        shots.draw(f, t)
        ship(f, px, py, t)
        if bar_drop <= t < bar_drop + 1.6:
            caption(f, "THE THIRD MOUNT FALLS: THE BARRIER DROPS, THE CORE IS OPEN")
        elif any(d_ <= t < d_ + 1.2 for d_ in dead.values()):
            caption(f, "A MOUNT LOST: THE RING SPEEDS UP (+8 DEG/S, EASED)")
        elif t < 2.2:
            caption(f, "ONLY THE MOUNTS IN THE RED ARC FACING THE SHIP FIRE")
        frames.append(f)
    print("halo", v, {j: round(d_, 2) for j, d_ in dead.items()}, len(lines), "lines", flush=True)
    return frames, bg


def ha_sheet(v, frames, bg):
    ring = halo_ring_frames(v)
    still = bg.img

    def whole(t=0.0, bearing=DOWN, barrier_k=1.0, size=280):
        c = window(still, size, size, "dark").copy()
        m = size / 2
        sprite.paste_center(c, halo_core_s[(v, 0.0)].get(0.0), m, m)
        k, rshow = halo_ring_index(ha_angle(t))
        sprite.paste_center(c, ring[k], m, m)
        c = arc_band(c, m, m, bearing)
        if barrier_k:
            c = barrier(c, m, m, t, barrier_k)
        for j in range(6):
            a = rshow + j * TAU / 6
            x, y = m + np.cos(a) * HALO_R, m + np.sin(a) * HALO_R
            sprite.paste_center(c, halo_tur_s[(v, "gun" if j % 2 == 0 else "rail")].get(DOWN), x, y)
            off = abs((a - bearing + np.pi) % TAU - np.pi)
            c = light(c, x - np.cos(a) * 12, y - np.sin(a) * 12, 2.6, (255, 60, 40) if off <= np.radians(60)
                      else (255, 200, 90), 1.3)
        return c
    ringonly = ring[0]
    tur = [halo_tur_s[(v, kind)].get(a) for kind in ("gun", "rail") for a in (DOWN, DOWN + 1.0, UP)]
    panels = [("THE PLATFORM, BARRIER UP, ARC TOWARD THE SHIP (1X)", whole()),
              ("TURRETS: GUN / RAIL (2X)", row(tur, HALO_TUR_N + 4, 2)),
              ("CORE: HELD / EYE FLARE (1X)", row([halo_core_s[(v, 0.0)].get(0.0), halo_core_s[(v, 1.0)].get(0.0)],
                                                  110, 1)),
              ("READABILITY: LEVEL 41'S CITADEL DECK (1X)", bands_check([(ringonly, shares(ringonly, False))], still,
                                                                       scales=(1,), names=("DECK LIT", "DECK DARK"))),
              ("KEY FRAMES (0.5X)", keyframes(frames, [1.2, HA_KILLS[1] + 0.3, HA_KILLS[3] + 0.3, 8.3],
                                              ["THE ARC", "A MOUNT LOST", "BARRIER DROPS", "OPEN CORE RING"]))]
    if v == "a":
        notes = ["HALO PLATFORM A - GOLD TRIM: THE CHOSEN R07 RING IN GLOSS BLACK, ITS RAILS, MOUNT RINGS AND END CAPS GOLD",
                 f"J8: THE R07 MODEL KEPT (128 FRAMES PER 60 DEG, TURRETS AT 64 HEADINGS); ONLY THE LIVERY CHANGES TO THE",
                 f"ELITE (Q9 A): GLOSS BLACK, HEAVY GOLD, THE STRONG 2 PX GOLD RIM, RED LIGHTS ({shares(ringonly, False)} ON",
                 "THE RING). NEW WITH THE DRAFT: THREE OF THE SIX MOUNTS ARE RAIL TURRETS (ONE LONG BARREL, THREE GOLD",
                 "COILS); THE LIVE 120 DEG ARC IS A FAINT RED BAND ON THE RING AND A MOUNT'S LIGHT TURNS GOLD -> RED IN IT;",
                 "THE CORE'S RED HEX BARRIER (SMOOTH) DROPS WHEN THE THIRD MOUNT FALLS; THE EYE FLARES BEFORE ITS RING OF 10."]
    else:
        notes = ["HALO PLATFORM B - GOLD DECKS: THE SAME RING WITH GOLD TOP DECKS BETWEEN BLACK RAILS, A GOLD CORE DRUM",
                 f"MORE GOLD AND A BOLDER RING ({shares(ringonly, False)} ON THE RING): THE ROTATION READS FROM THE DECKS'",
                 "SEGMENT BREAKS; THE GOLD DECKS SIT NEARER THE FORTRESS'S GOLD LIGHTS AND THE DOMES' GOLD, SO THE RED ARC",
                 "BAND HAS LESS CONTRAST ON THEM. SAME SCENE AS A."]
    return sheet(f"HALO PLATFORM {v.upper()} - RE-RENDERED IN THE HEARTLAND LIVERY (LEVEL 41, J8)", panels, notes,
                 frames[int(1.2 * FPS)])


@unit("halo-platform")
def make_halo(variants):
    for v in variants:
        frames, bg = ha_scene(v)
        save("enemies/ground/halo-platform", "halo-platform", v, ha_sheet(v, frames, bg), frames)


# ===================================================================== 7 Q9's livery comparison

def talon_livery(kind):
    """The production Talon (r60 ``talon_model`` b: gold outer panels and tail fins) in a livery: ``regular``
    (Q11 b gunmetal), ``elite`` (gloss black with the leader's gold spine stripe),
    ``black`` (Q9 c: the regular trim on the L28 cruisers' gloss black)."""
    scene0, _ = e60.talon_model("b", leader=(kind == "elite"))
    if kind == "elite":
        return scene0, elite_mats("c8202a", red=1.6)
    return scene0, (regular_mats("c8202a") if kind == "regular" else elite_mats("c8202a"))


liv_talon = {k: Turning(lambda k=k: talon_livery(k), CHI_N, 16, factor=5, colors=36,
                        post=ELITE_RIM if k == "elite" else REG_RIM) for k in ("regular", "elite", "black")}


@unit("heartland-livery")
def make_livery(variants):
    subjects = [("TALON REGULAR", liv_talon["regular"].get(DOWN), False),
                ("TALON ELITE", liv_talon["elite"].get(DOWN), False),
                ("TALON HYBRID", chimeras["a"].get(DOWN, CHI_IDLE), True),
                ("TALON Q9 C BLACK", liv_talon["black"].get(DOWN), False),
                ("MIRROR INT.", mi_sprite("a", DOWN, 0.4), False),
                ("CHIMERA B", chimeras["b"].get(DOWN, CHI_IDLE), True),
                ("HARROW A", harrows["a"].get(DOWN, 0.55), True),
                ("PYLON A", pylon_sprite("a", DOWN), False),
                ("HALO RAIL", halo_tur_s[("a", "rail")].get(DOWN), False),
                ("PLAYER", e3.player_sprite(), False)]
    grounds = [("L36 CREAM", window(bands_bg(36, 1200), 92, 92, "bright")),
               ("L36 BROWN", window(bands_bg(36, 1200), 92, 92, "dark")),
               ("L41 ICE", window(ice_bg(41, 900, deck=(300, 470)), 92, 92, "bright")),
               ("L41 DECK", window(ice_bg(41, 900, deck=(20, 460)), 92, 92, "dark"))]
    cell, sc = 92, 1
    lab_w = 70
    img = Image.new("RGBA", (lab_w + len(subjects) * (cell * 2 + 6), 40 + len(grounds) * (cell * 2 + 6)),
                    raster.SHEET_BG)
    for i, (name, sp, hyb) in enumerate(subjects):
        x = lab_w + i * (cell * 2 + 6)
        raster.draw_text(img, x, 2, name, raster.LABEL)
        if name != "PLAYER":
            raster.draw_text(img, x, 14, f"GOLD {100 * gold_share(sp):.0f} %", raster.LABEL_DIM)
            if hyb:
                raster.draw_text(img, x, 24, f"TEAL {100 * teal_share(sp):.0f} %", raster.LABEL_DIM)
    for r, (gname, g) in enumerate(grounds):
        y = 40 + r * (cell * 2 + 6)
        raster.draw_text(img, 4, y + cell, gname, raster.LABEL_DIM)
        for i, (name, sp, _) in enumerate(subjects):
            c = g.copy()
            sprite.paste_center(c, sp, cell / 2, cell / 2)
            img.alpha_composite(c.resize((cell * 2, cell * 2), Image.NEAREST), (lab_w + i * (cell * 2 + 6), y))
    field = Scroll(bands_bg, 36, 1, AIR_SCROLL).frame(0.0)
    for (x, y, sp) in [(110, 120, liv_talon["regular"].get(DOWN)), (170, 150, liv_talon["regular"].get(DOWN)),
                       (300, 130, chimeras["a"].get(DOWN, CHI_IDLE)), (360, 160, chimeras["a"].get(DOWN, CHI_IDLE)),
                       (240, 250, liv_talon["elite"].get(DOWN)), (380, 300, mi_sprite("a", DOWN + 0.4, 0.4)),
                       (120, 330, harrows["a"].get(DOWN - 0.3, 0.55)), (240, 470, e3.player_sprite())]:
        sprite.paste_center(field, sp, x, y)
    notes = ["Q9 - THE HEARTLAND'S LIVERIES SIDE BY SIDE (ROUND 72; A IS THE DRAFT, PENDING THE USER'S REVIEW)",
             "A: REGULAR = ACT 5'S GUNMETAL (Q11 B) FOR EVERY RETURNING UNIT AND THE MIRROR INTERCEPTOR, NO RE-RENDER; ELITE =",
             "GLOSS BLACK WITH HEAVY GOLD (35-45 %) AND THE STRONG 2 PX GOLD RIM FOR THE ACES, THE ASCENDANT AND THE FORTRESS'S",
             "OWN EMPLACEMENTS (PYLON, HALO PLATFORM); HYBRID = THE GUNMETAL SPLIT BY DARK TEAL FLESH WITH A TEAL 00FF9A GLOW",
             "IN THE SEAMS (GOLD >= 15 %, TEAL 5-12 % AT FULL HEALTH). B: EVERYTHING GUNMETAL (THE FIRST COLUMN FOR THE ELITE",
             "TOO: THE ACES WOULD NOT STAND OUT). C: THE HEARTLAND ALL GLOSS BLACK (FOURTH COLUMN: DARK ON L36'S BROWN BAND AND",
             "L41'S BLACK DECK, AND A RE-RENDER OF EVERY RETURNING UNIT). EACH CELL 2X OVER ITS LEVEL'S BRIGHTEST AND DARKEST",
             "WINDOW; THE SHARES ARE MEASURED ON THE SPRITE (THE CHIMERA AT ITS FULL-HEALTH GLOW)."]
    s = sheet("Q9 - REGULAR / ELITE / HYBRID LIVERIES OVER JUPITER AND CALLISTO", [("THE LIVERIES (2X)", img)],
              notes, field, width=img.width + FW + 90)
    save("enemies", "heartland-livery", "a", s)


# ===================================================================== 8 Plasma Arc (the player's Act 6 front gun)

ARC_STYLE = {
    "tesla": dict(glow=(140, 200, 255), fringe=(150, 80, 255), w=1.0),     # the Tesla Coil Pod's (act5_gear.py a)
    "a": dict(glow=(140, 200, 255), fringe=(150, 80, 255), w=1.7),         # the same family, wider and hotter
    "b": dict(glow=(120, 236, 255), fringe=(105, 125, 255), w=1.5),        # the braid: cyan, an indigo fringe
}


def _polyline_dist(xx, yy, pts):
    best = np.full(xx.shape, 1e9)
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        bx, by = x1 - x0, y1 - y0
        h = np.clip(((xx - x0) * bx + (yy - y0) * by) / (bx * bx + by * by + 1e-9), 0, 1)
        best = np.minimum(best, np.hypot(xx - x0 - bx * h, yy - y0 - by * h))
    return best


def arc_paths(style, p0, p1, seed, w):
    """The strands of one strike: the jagged bolt with two short forks (a, tesla) or two strands braided round
    the line, pinched at both ends (b)."""
    rng = np.random.default_rng(seed)
    p0, p1 = np.asarray(p0, float), np.asarray(p1, float)
    L = max(1.0, float(np.hypot(*(p1 - p0))))
    if style != "b":
        main = g36.jagged(p0, p1, rng, depth=5, rough=0.2)
        out = [(main, 1.0)]
        for _ in range(2):
            i = int(rng.integers(3, max(4, len(main) - 4)))
            bx, by = main[i]
            dirv = (p1 - p0) / L
            ang = np.arctan2(dirv[1], dirv[0]) + rng.choice((-1, 1)) * rng.uniform(0.5, 1.0)
            ln = rng.uniform(0.12, 0.22) * L
            fork = g36.jagged((bx, by), (bx + np.cos(ang) * ln, by + np.sin(ang) * ln), rng, depth=3, rough=0.3)
            out.append((fork, 0.5))
        return out
    n = np.array((-(p1 - p0)[1], (p1 - p0)[0])) / L
    s = np.linspace(0, 1, max(8, int(L / 3)))
    env = np.sin(np.pi * s) ** 0.5
    ph = rng.uniform(0, TAU)
    out = []
    for sign in (1, -1):
        off = sign * 2.6 * w * np.sin(TAU * s * L / 24 + ph) * env + rng.normal(0, 0.35, len(s)) * env
        pts = [tuple(p0 + (p1 - p0) * si + n * oi) for si, oi in zip(s, off)]
        out.append((pts, 1.0))
    return out


def draw_strike(img, p0, p1, style, seed, w=None, k=1.0):
    """One strike drawn as a smooth additive light field (1x, float, added once)."""
    st = ARC_STYLE[style]
    w = st["w"] if w is None else w
    paths = arc_paths(style, p0, p1, seed, w)
    pad = int(6 * w + 10)
    xs = [x for pts, _ in paths for x, _ in pts]
    ys = [y for pts, _ in paths for _, y in pts]
    box = (max(0, int(min(xs)) - pad), max(0, int(min(ys)) - pad), min(img.width, int(max(xs)) + pad + 1),
           min(img.height, int(max(ys)) + pad + 1))
    if box[2] <= box[0] or box[3] <= box[1]:
        return img
    a = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    lightf = np.zeros(a.shape[:2] + (3,))
    for pts, kk in paths:
        d = _polyline_dist(xx, yy, pts)
        wk = w * (0.7 if kk < 1 else 1.0)
        lightf += np.exp(-(d / (3.4 * wk)) ** 2)[..., None] * np.array(st["fringe"]) * 0.26 * kk
        lightf += np.exp(-(d / (1.5 * wk)) ** 2)[..., None] * np.array(st["glow"]) * 0.9 * kk
        lightf += np.exp(-(d / (0.6 * wk)) ** 2)[..., None] * 255 * 1.4 * kk
    a[..., :3] = np.clip(a[..., :3] + lightf * k, 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), img.mode), box)
    return img


def strike_burst(img, x, y, s, style, seed=0, size=1.0):
    """The flash and spark burst on a struck unit (0 <= s < 1)."""
    if not 0 <= s < 1:
        return img
    st = ARC_STYLE[style]
    img = light(img, x, y, (5 + 6 * s) * size, (255, 255, 255), 1.4 * (1 - s) ** 2)
    img = light(img, x, y, (10 + 10 * s) * size, st["glow"], 0.6 * (1 - s))
    rng = np.random.default_rng(seed)
    for j in range(6):
        a = TAU * j / 6 + rng.uniform(-0.3, 0.3)
        r0, r1 = (3 + 10 * s) * size, (9 + 16 * s) * size
        img = draw_strike(img, (x + np.cos(a) * r0, y + np.sin(a) * r0), (x + np.cos(a) * r1, y + np.sin(a) * r1),
                          "tesla" if style == "a" else "b", seed + j, 0.45, (1 - s) * 0.8)
    return img


PA_LEVELS = {1: dict(chain=3, rate=4, dmg=7.5, reach=200, jump=90), 5: dict(chain=6, rate=5, dmg=24, reach=240, jump=120)}
PA_DUR = 10.0
PA_SWITCH = 5.0


def swarm_pos(j, t, t0, x0, vx, y0):
    u = t - t0
    ang = j * 2.4
    rad = 28 + 14 * (j % 3)
    cx = x0 + vx * u
    return (cx + np.cos(ang + u * 0.8) * rad * 1.3, y0 + np.sin(ang + u * 0.8) * rad * 0.7 + 10 * np.sin(u + j),
            (np.pi / 2 if vx == 0 else (0.0 if vx > 0 else np.pi)) + 0.3 * np.sin(u * 1.3 + j))


def pa_scene(v):
    bg = Scroll(bands_bg, 360, PA_DUR, AIR_SCROLL)
    H = chimeras["a"]
    py = 470
    waves = [dict(t0=0.0, x0=-90, vx=70, y0=300, n=8), dict(t0=PA_SWITCH - 0.6, x0=FW + 50, vx=-80, y0=290, n=8)]
    units = []
    for wi, wv in enumerate(waves):
        for j in range(wv["n"]):
            units.append(dict(w=wi, j=j, hp=CHI_HP, last=-9.0, dead=None))
    px_of = follow(lambda t: 200 + 60 * np.sin(t * 0.7) if t < PA_SWITCH else 280 - 60 * np.sin(t * 0.7), PA_DUR,
                   200, 160, 0.4)
    nxt, strikes, bursts, frames = 0.4, [], [], []
    for t in times(PA_DUR):
        f = bg.frame(t)
        px = px_of(t)
        lv = PA_LEVELS[1 if t < PA_SWITCH else 5]
        pos = {}
        for i, u in enumerate(units):
            wv = waves[u["w"]]
            if t >= wv["t0"] and u["dead"] is None:
                x, y, h = swarm_pos(u["j"], t, wv["t0"], wv["x0"], wv["vx"], wv["y0"])
                if -40 < x < FW + 40:
                    pos[i] = (x, y, h)
        if t >= nxt:
            nxt += 1.0 / lv["rate"]
            nose = (px, py - 24)
            cand = [(np.hypot(x - nose[0], y - nose[1]), i) for i, (x, y, _) in pos.items()
                    if np.hypot(x - nose[0], y - nose[1]) <= lv["reach"]
                    and abs(np.arctan2(x - nose[0], -(y - nose[1]))) <= np.radians(45)]
            if cand:
                _, i = min(cand)
                chain, src, dmg, seen = [], nose, lv["dmg"], {i}
                cur = i
                for c in range(lv["chain"]):
                    chain.append((src, pos[cur][:2], c))
                    u = units[cur]
                    u["hp"] -= dmg
                    u["last"] = t
                    bursts.append((t, pos[cur][0], pos[cur][1], cur))
                    if u["hp"] <= 0:
                        u["dead"] = t
                        u["at"] = pos[cur][:2]
                    dmg *= 0.75
                    src = pos[cur][:2]
                    near = [(np.hypot(x - src[0], y - src[1]), k_) for k_, (x, y, _) in pos.items()
                            if k_ not in seen and np.hypot(x - src[0], y - src[1]) <= lv["jump"]]
                    if not near:
                        break
                    _, cur = min(near)
                    seen.add(cur)
                strikes.append((t, chain, int(t * 100)))
        for u in units:
            if u["dead"] is None and t - u["last"] > 1.0 and u["hp"] < CHI_HP:
                u["hp"] = min(CHI_HP, u["hp"] + 0.2 * CHI_HP / FPS)
        for i, (x, y, h) in pos.items():
            u = units[i]
            share = u["hp"] / CHI_HP
            since = t - u["last"]
            k = 0.1 if since < 1.0 else (0.3 + 0.75 * share if share < 1 else CHI_IDLE)
            sprite.paste_center(f, H.get(h, k, (t * 2) % 1 if (since >= 1 and share < 1) else None), x, y)
        for u in units:
            if u["dead"] is not None:
                s = (t - u["dead"]) / 0.6
                f = boom(f, u["at"][0], u["at"][1], s, "small")
                f = flecks(f, u["at"][0], u["at"][1], (t - u["dead"]) / 0.8, seed=u["j"])
        for t0, chain, seed in strikes:
            if 0 <= t - t0 < 0.1:
                for src, dst, c in chain:
                    w = ARC_STYLE[v]["w"] * (0.72 ** c)
                    f = draw_strike(f, src, dst, v, seed + c, w, 1.0)
        for t0, x, y, i in bursts:
            if 0 <= t - t0 < 0.2:
                f = strike_burst(f, x, y, (t - t0) / 0.2, v, i)
        ship(f, px, py, t)
        f = light(f, px, py - 24, 4, ARC_STYLE[v]["glow"], 0.9 if any(0 <= t - t0 < 0.1 for t0, _, _ in strikes) else 0.3)
        caption(f, "PLASMA ARC L1: 3 TARGETS, REACH 200 PX, JUMPS OF 90" if t < PA_SWITCH else
                "PLASMA ARC L5: 6 TARGETS, REACH 240 PX, JUMPS OF 120")
        frames.append(f)
    best = max(strikes, key=lambda s: (len(s[1]), s[0] > PA_SWITCH))
    l1 = max([s for s in strikes if s[0] < PA_SWITCH], key=lambda s: len(s[1]))
    print("plasma arc", v, "strikes", len(strikes), "longest L1", len(l1[1]), "L5", len(best[1]),
          "kills", sum(u["dead"] is not None for u in units), flush=True)
    return frames, bg, l1[0], best[0]


def pa_sheet(v, frames, bg, t1, t5):
    plate = (12, 14, 22, 255)
    strip = Image.new("RGBA", (300, 150), plate)
    pts = [(14, 120), (90, 40), (170, 90), (240, 30), (286, 110), (230, 140)]
    for c, (p0, p1) in enumerate(zip(pts[:-1], pts[1:])):
        strip = draw_strike(strip, p0, p1, v, 70 + c, ARC_STYLE[v]["w"] * 0.72 ** c)
        strip = strike_burst(strip, p1[0], p1[1], 0.15, v, c)
    tes = Image.new("RGBA", (300, 60), plate)
    tes = draw_strike(tes, (14, 30), (286, 30), "tesla", 3)
    mine = Image.new("RGBA", (300, 60), plate)
    mine = draw_strike(mine, (14, 30), (286, 30), v, 3)
    comp = Image.new("RGBA", (300, 126), raster.SHEET_BG)
    comp.alpha_composite(tes, (0, 0))
    comp.alpha_composite(mine, (0, 66))
    raster.draw_text(comp, 4, 2, "TESLA COIL POD (ROUND 61 A)", raster.LABEL_DIM)
    raster.draw_text(comp, 4, 68, "PLASMA ARC " + v.upper(), raster.LABEL_DIM)
    bursts = []
    for s in (0.0, 0.3, 0.6, 0.9):
        c = Image.new("RGBA", (60, 60), plate)
        bursts.append(strike_burst(c, 30, 30, s, v, 4))
    checks = []
    H = chimeras["a"]
    for mode in ("bright", "dark"):
        g = window(bg.img, 150, 150, mode).copy()
        sprite.paste_center(g, H.get(DOWN, CHI_IDLE), 110, 40)
        sprite.paste_center(g, H.get(DOWN, CHI_IDLE), 40, 70)
        sprite.paste_center(g, e3.player_sprite(), 75, 128)
        g = draw_strike(g, (75, 106), (110, 40), v, 5)
        g = draw_strike(g, (110, 40), (40, 70), v, 6, ARC_STYLE[v]["w"] * 0.72)
        g = strike_burst(g, 110, 40, 0.1, v, 1)
        checks.append(g.resize((300, 300), Image.NEAREST))
    rd = Image.new("RGBA", (612, 312), raster.SHEET_BG)
    rd.alpha_composite(checks[0], (0, 0))
    rd.alpha_composite(checks[1], (312, 0))
    panels = [("A CHAIN OF FIVE JUMPS, EACH FORK THINNER (1X)", strip),
              ("THE FAMILY: THE POD'S ARC / THIS GUN (1X)", comp),
              ("THE SPARK BURST ON A STRUCK UNIT (2X)", grid(bursts, 4, 2)),
              ("READABILITY: L36'S CREAM AND BROWN BANDS (2X)", rd),
              ("KEY FRAMES (0.5X)", keyframes(frames, [t1, t1 + 0.05, t5, t5 + 0.05],
                                              ["L1 CHAIN", "L1 +1 FRAME", "L5 CHAIN", "L5 +1 FRAME"]))]
    if v == "a":
        notes = ["PLASMA ARC A - THE WIDE TESLA: THE ARC FAMILY'S JAGGED BOLT, DRAWN WIDER AND HOTTER",
                 "THE TESLA COIL POD'S LOOK (A JAGGED WHITE CORE IN A BLUE-WHITE GLOW, A VIOLET FRINGE, TWO SHORT FORKS) AT",
                 "1.7X ITS WIDTH WITH A HOTTER WHITE CORE ON THE FIRST STRIKE; EVERY JUMP IS DRAWN 0.72X AS WIDE AS THE ONE",
                 "BEFORE (THE DAMAGE FALLS 0.75X), SO A CHAIN READS AS A FORKING STROKE THAT THINS OUT. 0.1 S PER DISCHARGE,",
                 "A WHITE FLASH AND SIX SHORT SPARKS ON EVERY STRUCK UNIT. IT IS PLAINLY 'THE TESLA, BIGGER': ONE FAMILY,",
                 "ONE SOUND (THE TESLA ZAP PER JUMP AT A RISING PITCH)."]
    else:
        notes = ["PLASMA ARC B - THE BRAID: TWO PLASMA STRANDS TWISTING ROUND THE LINE, CYAN WITH AN INDIGO FRINGE",
                 "NOT JAGGED: TWO SMOOTH STRANDS BRAIDED ROUND EACH STRIKE, PINCHED AT BOTH ENDS (A 'ROPE' OF PLASMA), A",
                 "CYAN GLOW AND AN INDIGO FRINGE KEPT AWAY FROM THE MIRROR'S MAGENTA; EACH JUMP A THINNER BRAID. IT READS AS",
                 "ITS OWN WEAPON, NOT AS THE POD'S ARC, AND THE CHAIN'S PATH IS EASIER TO FOLLOW; IT LEAVES THE ARC FAMILY'S",
                 "LOOK. SAME SCENE AS A."]
    return sheet(f"PLASMA ARC {v.upper()} - THE CHAIN LOOK (ACT 6 FRONT GUN, OVER LEVEL 36)", panels, notes,
                 frames[min(len(frames) - 1, int(t5 * FPS))])


@unit("plasma-arc")
def make_arc(variants):
    for v in variants:
        frames, bg, t1, t5 = pa_scene(v)
        save("player/weapons/plasma-arc", "plasma-arc", v, pa_sheet(v, frames, bg, t1, t5), frames)


# ##INSERT-UNITS##


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
