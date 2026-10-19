#!/usr/bin/env python3
"""Concept round 36 - the new Act 3 "Red Dust" units (Mars), a/b proposals.

Each subject gets a PNG sheet (source render, native sprites and states, the mechanic's
key frames, a frame from the play field at 1x) and a GIF of it in the 480x540 play field over
Martian ground, with the player for scale. Models are built in this file on the frozen
``render`` package (sdf, enemy_rigs, enemy_models role colours); turning units use
``ModelSpaceAngleSprites`` so patterns turn with the body.

Outputs (design/enemies/...; the user's picks of round 36 (CHOSEN) stay in concept/, the unchosen
variants are written to concept/rejected/):
  ground/concept/burrower-r36-{a,b}.{png,gif}        sand burrower: furrow, bulge tell, breach,
                                                     ring of orbs, submerge (a: slate/lime maw
                                                     worm, b: bone/violet armoured clam-shell)
  ground/concept/tendril-anchor-r36-{a,b}.{png,gif}  anchor pairs on the canyon walls with the
                                                     damaging link (a: fleshy rope, b: crackling
                                                     filament); broken by killing an anchor or
                                                     burnt through by a beam (beam is a mock-up)
  air/concept/choir-herald-r36-{a,b}.{png,gif}       support flyer whose aura rings its escorts
                                                     (a: teal-black bell with sound-wave aura,
                                                     b: bone seraph with a halo crest, segmented
                                                     violet rings)
  air/concept/ghost-drone-r36-{a,b}.{png,gif}        unmarked grey human-built drone, strafes with
                                                     precise bursts (a: faceted dart, b: crescent);
                                                     a re-rendered with the U.5 fix: a 2x2 px red
                                                     eye and white wing-tip running lights, their
                                                     glow drawn additively (ghost_glow)
  ground/concept/warden-tank-r36-a.{png,gif}         the chosen r05-a Warden Tank model in
                                                     unmarked grey (no gold, no accent), on Mars
  air/concept/dust-kite-r36-{a,b}.{png,gif}          Mars popcorn glider: sine glide, one swoop
                                                     across the ship's lane, rams; stream, V-wing,
                                                     swarm; dust puff + teal spark death
                                                     (a: diamond kite with a whip tail,
                                                     b: crescent sail on bone fingers)

Run: python3 tools/concept/enemies_r36.py [burrower] [tendril-anchor] [choir-herald]
     [ghost-drone] [ghost-drone-a] [warden-tank] [dust-kite]   (no args = everything, a few minutes)
"""
import sys
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import enemies_r03 as e3  # noqa: E402
import enemies_r04 as e4  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import enemies_r05b as r5b  # noqa: E402
import enemies_r06 as e6  # noqa: E402
import vfx_r08 as v8  # noqa: E402  (light fields for the Ghost Drone's additive running lights)
from render import archetype_models as am  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import (A_ENGINE, A_FACET, A_GLASS, A_GUN, A_HULL, A_RED,  # noqa: E402
                                 RED, THRUST, V_BODY, V_BONE, V_DARK, V_EYE, V_GLOW, V_SAC,
                                 V_SEAM, hx)
from render.sdf import (Material, mirror_x, panel_lines, sd_box, sd_capsule,  # noqa: E402
                        sd_cylinder_z, sd_ellipsoid, sd_plate, sd_sphere, subtract, union)

r5.SUB = "CONCEPT ROUND 36 - 960X540"
FW, FH, FPS, TAU = r5.FW, r5.FH, r5.FPS, 2 * np.pi
times, grid, paste = r5.times, r5.grid, r5.paste
source_panel = r5b.source_panel
DOWN = np.pi / 2

em.ROLE_SCHEMES.update({
    "burrower-a":       ("slate", "lime", "lime"),       # rooted when up, ring = area denial
    "burrower-b":       ("bone", "violet", "violet"),    # gunner shell, ring of shots
    "tendril-anchor-a": ("slate", "crimson", "crimson"), # rooted; the link is a line of danger
    "tendril-anchor-b": ("slate", "crimson", "crimson"),
    "choir-herald-a":   ("teal-black", "teal", "teal"),  # support / carrier family
    "choir-herald-b":   ("bone", "violet", "violet"),
})

CRIMSON = (255, 48, 56)
LIME = (168, 255, 42)
TEAL = (0, 255, 154)
VIOLET = (154, 77, 255)


# the user's picks (round 36's review, 2026-10-13): the unchosen variants are written to concept/rejected/
CHOSEN = {"burrower": "a", "tendril-anchor": "a", "choir-herald": "b", "ghost-drone": "a",
          "warden-tank": "a", "dust-kite": "b"}


def save_unit(category, slug, variant, sheet, frames, fps=FPS):
    sub = "" if CHOSEN[slug] == variant else "rejected/"
    png = r5.out(category, f"{sub}{slug}-r36-{variant}.png")
    png.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, r5.out(category, f"{sub}{slug}-r36-{variant}.gif"), fps=fps)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)")


def smooth(x):
    x = np.clip(x, 0.0, 1.0)
    return x * x * (3 - 2 * x)


def sd_torus_z(p, c, big, small):
    q = p - np.asarray(c, dtype=np.float64)
    a = np.sqrt(q[:, 0] ** 2 + q[:, 1] ** 2) - big
    return np.sqrt(a * a + q[:, 2] ** 2) - small


def folded(p, n, offset=0.5):
    """``p`` folded into one of ``n`` sectors around Z (radial repetition)."""
    r = np.sqrt(p[:, 0] ** 2 + p[:, 1] ** 2)
    a = np.arctan2(p[:, 1], p[:, 0])
    sec = TAU / n
    af = (a + sec * offset) % sec - sec * offset
    return np.stack([r * np.cos(af), r * np.sin(af), p[:, 2]], axis=-1)


def lift(mats, k=0.3):
    """Lighter, less metallic chitin body/seam slots (same hue): slate reads near-black on the
    dark Martian ground otherwise."""
    for i in (V_BODY, V_SEAM):
        mats[i] = replace(mats[i], albedo=em._mix(mats[i].albedo, (1, 1, 1), k), metal=0.15)
    return mats


# --------------------------------------------------------------------------- unmarked livery

UG = [hx(c) for c in "15171b 2a2d33 434750 5d626c 7c818b a3a8b0".split()]
LIT_RIM, DARK_RIM = (190, 196, 204), (14, 15, 18)


def unmarked_mats(glow=1.0, hull_pattern=None):
    """Ascendancy material slots (A_HULL ... A_GUN) in plain grey: no gold, no accent colour.
    The red sensor lights and the engine glow stay (same hardware as the Ascendancy)."""
    g = glow
    return [
        Material(UG[4], metal=0.5, shininess=60, spec=0.6, pattern=hull_pattern),
        Material(UG[3], metal=0.55, shininess=90, spec=0.75),
        Material(UG[5], metal=0.5, shininess=70, spec=0.6),        # was gold: light plain grey
        Material((0.05, 0.02, 0.02), emission=tuple(RED * 1.25 * g)),
        Material((0.07, 0.04, 0.03), emission=tuple(THRUST * 1.2 * g)),
        Material(hx("1c2026"), metal=0.9, shininess=150, spec=1.3),
        Material(UG[2], metal=0.7, shininess=90, spec=0.8),
    ]


def grey_rim(sp):
    """``enemies_r04.rim`` in neutral greys: no gold/red faction rim on unmarked machines."""
    a = np.array(sp)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    left, up = ~pad[1:-1, :-2], ~pad[:-2, 1:-1]
    right, down = ~pad[1:-1, 2:], ~pad[2:, 1:-1]
    out = a.astype(np.float64)
    for mask, col, k in (((left | up) & op, LIT_RIM, 0.55),
                         ((right | down) & op & ~(left | up), DARK_RIM, 0.5)):
        out[mask, :3] = out[mask, :3] * (1 - k) + np.array(col) * k
    img = Image.fromarray(out.astype(np.uint8), "RGBA")
    return sprite.quantize(img, 32 if img.width <= 42 else 48)


# --------------------------------------------------------------------------- backgrounds

def mars_ground(seed, h=FH, craters=6, dunes=0.0):
    img = e3.regolith(FW, h, seed, ramp_name="MARS", craters=craters)
    if dunes:
        a = np.array(img).astype(np.float64)
        yy, xx = np.mgrid[0:h, 0:FW]
        warp = raster.fbm(FW, h, 64, seed + 7, octaves=3, period=False)
        rip = np.sin((xx * 0.45 + yy + warp * 70) / 6.5)
        a[..., :3] *= (1 + dunes * rip)[..., None]
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return img


def wall_l(y):
    return 74 + 14 * np.sin(y / 70.0 + 1.0) + 6 * np.sin(y / 23.0)


def wall_r(y):
    return FW - (74 + 14 * np.sin(y / 55.0) + 6 * np.sin(y / 29.0 + 2.0))


def canyon(seed, h):
    """Valles canyon floor between two layered rock walls (lit from the top-left: the left
    wall throws a shadow strip onto the floor, the right wall's face catches the light)."""
    img = mars_ground(seed, h, craters=4, dunes=0.03)
    a = np.array(img).astype(np.float64)
    yy = np.arange(h)[:, None]
    xx = np.arange(FW)[None, :]
    xl, xr = wall_l(yy), wall_r(yy)
    strata = 0.62 + 0.12 * np.sin((xx + yy * 0.3) / 6.0) + 0.05 * np.sin(yy / 3.0)
    left, right = xx < xl, xx > xr
    grey = a[..., :3].mean(axis=-1, keepdims=True)
    rock = (a[..., :3] * 0.6 + grey * 0.4) * strata[..., None] * 0.55
    a[..., :3] = np.where((left | right)[..., None], rock, a[..., :3])
    shade = np.clip((xx - xl) / 16.0, 0, 1) * 0.35 + 0.65
    a[..., :3] = np.where((~left & (xx - xl < 16))[..., None], a[..., :3] * shade[..., None],
                          a[..., :3])
    lit = (xx > xr) & (xx - xr < 4)
    a[lit, :3] = a[lit, :3] * 1.45
    edge = (xx < xl) & (xl - xx < 3)
    a[edge, :3] = a[edge, :3] * 1.6
    img = Image.fromarray(np.clip(a[..., :3], 0, 255).astype(np.uint8), "RGB")
    img = img.filter(ImageFilter.GaussianBlur(2.2)).quantize(12, dither=Image.Dither.NONE)
    return img.convert("RGBA")                    # posterized like the game's backdrops


# --------------------------------------------------------------------------- shared drawing

def draw_ship(img, px, py, shots=True, stop_y=None, t=0.0):
    e3.place(img, e3.player_sprite(), px, py, "air")
    if shots:
        for k in range(4):
            y = py - 40 - ((t * 420 + k * 105) % 420)
            if y > -10 and (stop_y is None or y > stop_y):
                e3.bolt(img, px - 10, y)
                e3.bolt(img, px + 10, y)


class Shots:
    """Straight enemy shots: (t0, x, y, vx, vy, radius)."""

    def __init__(self):
        self.list = []

    def fire(self, t0, x, y, tx, ty, speed, r=4):
        d = np.hypot(tx - x, ty - y) or 1
        self.list.append((t0, x, y, (tx - x) / d * speed, (ty - y) / d * speed, r))

    def ring(self, t0, x, y, n, speed, r=4, phase=0.0, r0=0.0):
        for a in np.linspace(0, TAU, n, endpoint=False) + phase:
            c, s = np.cos(a), np.sin(a)
            self.fire(t0, x + c * r0, y + s * r0, x + c * (r0 + 1), y + s * (r0 + 1), speed, r)

    def draw(self, img, t):
        for t0, x, y, vx, vy, r in self.list:
            if t >= t0:
                bx, by = x + vx * (t - t0), y + vy * (t - t0)
                if -20 < bx < FW + 20 and -20 < by < FH + 20:
                    e3.orb(img, bx, by, r)


def flash(sp, on):
    return e6.tint(sp, (255, 255, 255), 0.55) if on else sp


def crops(frames, spots, size, labels):
    out = []
    for (i, x, y) in spots:
        x0, y0 = int(x - size / 2), int(y - size / 2)
        out.append(frames[i].crop((x0, y0, x0 + size, y0 + size)))
    return out, labels


def bg_crop(bg, top):
    top = int(round(top))
    return bg.crop((0, top, FW, top + FH))


# ===================================================================== 1 Burrower (L15)

def burrower_a(rise=1.0, open_=0.0, glow=1.0):
    """Sand maw worm seen from above: a domed ringed head on a thick column, a crown of bone
    spikes at the sand line, a six-toothed maw over a lime throat and eight lime pores that
    fire the ring. Everything below the sand plane z = 0 is cut away (``rise`` 0..1)."""
    h = -0.5 + 0.72 * rise

    def scene(p):
        d, m = union(
            (sd_capsule(p, (0, 0, -1.6), (0, 0, h - 0.4), 0.6, 0.6), V_SEAM),
            (sd_ellipsoid(p, (0, 0, h), (0.7, 0.7, 0.42)), V_BODY),
            k=0.1,
        )
        f10 = folded(p, 10)
        d, m = union((d, m), (sd_capsule(f10, (0.52, 0, h - 0.06), (0.98, 0, h - 0.26),
                                         0.11, 0.02), V_BONE), k=0.05)
        orad = 0.2 + 0.12 * open_
        d, m = subtract((d, m), sd_cylinder_z(p, (0, 0, h + 0.45), orad, 0.3))
        f6 = folded(p, 6)
        d, m = union((d, m), (sd_capsule(f6, (orad + 0.24, 0, h + 0.3),
                                         (orad - 0.08 + 0.08 * open_, 0, h + 0.44), 0.1, 0.025),
                              V_BONE), k=0.02)
        d, m = union((d, m), (sd_sphere(p, (0, 0, h + 0.04), 0.2 + 0.08 * open_), V_GLOW))
        f8 = folded(p, 8, 0.0)
        d, m = union((d, m), (sd_sphere(f8, (0.5, 0, h + 0.27), 0.07), V_EYE), k=0.01)
        return np.maximum(d, -p[:, 2]), m
    return scene, lift(am.vrell("burrower-a", glow))


def burrower_b(rise=1.0, open_=0.0, glow=1.0):
    """Armoured sand clam: four ivory carapace plates over a violet gun core; the plates slide
    apart (``open_``) to bare the core and six ports; two shovel claws in front."""
    h = -0.5 + 0.72 * rise
    o = open_

    def scene(p):
        q = mirror_x(p)
        items = [(sd_ellipsoid(p, (0, 0, h - 0.05), (0.56, 0.7, 0.3)), V_DARK),
                 (sd_sphere(p, (0, 0.02, h + 0.04), 0.3), V_GLOW)]
        f6 = folded(p, 6, 0.0)
        items.append((sd_sphere(f6, (0.3, 0, h + 0.2), 0.065), V_EYE))
        for sy in (1, -1):
            c = (0.19 + 0.3 * o, sy * (0.21 + 0.24 * o), h + 0.14 + 0.06 * o)
            items.append((sd_ellipsoid(q, c, (0.4, 0.48, 0.2)), V_BODY))
            items.append((sd_capsule(q, (c[0] + 0.05, c[1] + sy * 0.3, c[2] + 0.2),
                                     (c[0] + 0.12, c[1] - sy * 0.2, c[2] + 0.2), 0.05, 0.03),
                          V_BONE))
        items.append((sd_capsule(q, (0.3, 0.6, h - 0.02), (0.16, 1.02, h - 0.1), 0.13, 0.04),
                      V_BONE))
        items.append((sd_capsule(q, (0.36, -0.55, h - 0.05), (0.62, -0.92, h - 0.2), 0.08,
                                 0.02), V_BONE))
        d, m = union(*items, k=0.03)
        return np.maximum(d, -p[:, 2]), m
    return scene, am.vrell("burrower-b", glow, style="b")


BUR_N = 68
burrower = {v: rig.ModelSpaceAngleSprites(
    (lambda rise=1.0, open_=0.0, f=f: f(rise, open_)), BUR_N, 4, factor=5, colors=32)
    for v, f in (("a", burrower_a), ("b", burrower_b))}
GLOWS_RGB = {"a": LIME, "b": VIOLET}


def bur_sprite(v, rise, open_):
    rise = round(float(np.clip(rise, 0, 1)) * 6) / 6
    open_ = round(float(np.clip(open_, 0, 1)) * 2) / 2
    if rise <= 0:
        return None
    return burrower[v].get(DOWN, rise=rise, open_=open_)


def sand_bump(img, x, y, amount, radius, seed=0, cracks=0.0, glow=None, t=0.0):
    """Shade a bump into the sand: a hill-shaded dome (lit from the top-left), radial cracks
    once it is high, grit trickling down its sides; ``glow`` lets light seep from the cracks."""
    if amount <= 0.01:
        return
    R = int(radius * 1.8) + 2
    x0, y0 = int(x) - R, int(y) - R
    box = (max(x0, 0), max(y0, 0), min(x0 + 2 * R, FW), min(y0 + 2 * R, FH))
    if box[2] <= box[0] or box[3] <= box[1]:
        return
    patch = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]]
    d2 = ((xx - x) ** 2 + (yy - y) ** 2) / radius ** 2
    hgt = amount * np.exp(-d2) * radius * 0.5
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 + 1.3 * (gx + gy), 0.45, 1.6)
    patch[..., :3] *= shade[..., None]
    fresh = amount * np.exp(-d2 * 1.5) * 0.35       # disturbed, lighter sand on the crown
    patch[..., :3] = patch[..., :3] * (1 - fresh[..., None]) + np.array((236, 168, 112)) * \
        fresh[..., None]
    img.paste(Image.fromarray(np.clip(patch, 0, 255).astype(np.uint8), "RGBA"), box[:2])
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    if cracks > 0:
        lay = Image.new("RGBA", img.size, (0, 0, 0, 0)) if glow else None
        ld = ImageDraw.Draw(lay) if glow else None
        for k in range(7):
            a = k * TAU / 7 + rng.uniform(-0.3, 0.3)
            r0, r1 = radius * 0.15, radius * (0.35 + 0.55 * cracks) * rng.uniform(0.7, 1.0)
            pts = []
            for u in np.linspace(0, 1, 5):
                rr = r0 + (r1 - r0) * u
                aa = a + 0.18 * np.sin(u * 7 + k)
                pts.append((x + np.cos(aa) * rr, y + np.sin(aa) * rr))
            d.line(pts, fill=(40, 16, 10, 255), width=2)
            if glow:
                ld.line(pts, fill=glow + (int(230 * cracks),), width=2)
        if glow:
            img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(1.6)))
            img.alpha_composite(lay)
    for k in range(int(10 * amount)):              # grit trickling outward
        a = rng.uniform(0, TAU)
        u = ((t * 1.4 + rng.uniform(0, 1)) % 1.0)
        rr = radius * (0.3 + 0.9 * u)
        gx_, gy_ = x + np.cos(a) * rr, y + np.sin(a) * rr
        d.point((gx_, gy_), fill=(240, 196, 150, int(220 * (1 - u))))


def crater(img, x, y, r, alpha):
    """Sand collar around a surfaced unit (or the fading pit it leaves)."""
    if alpha <= 0:
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    d.ellipse([x - r - 4, y - r - 4, x + r + 4, y + r + 4], fill=(36, 14, 8, int(150 * alpha)))
    d.arc([x - r - 5, y - r - 5, x + r + 5, y + r + 5], 200, 380,
          fill=(70, 30, 16, int(200 * alpha)), width=3)
    d.arc([x - r - 5, y - r - 5, x + r + 5, y + r + 5], 20, 200,
          fill=(236, 170, 112, int(190 * alpha)), width=3)
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(0.8)))


def spray(img, x, y, s, r=30, seed=0):
    """Breach: sand thrown out in a ring (0 <= s <= 1)."""
    if not 0 <= s <= 1:
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rr = r * (0.5 + 1.1 * s)
    d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=(214, 140, 86, int(110 * (1 - s))))
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(3)))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    for k in range(26):
        a = rng.uniform(0, TAU)
        dist = r * (0.4 + 1.3 * s) * rng.uniform(0.6, 1.1)
        px, py = x + np.cos(a) * dist, y + np.sin(a) * dist - 10 * np.sin(np.pi * s)
        sz = rng.uniform(1, 2.6) * (1.1 - s)
        d.ellipse([px - sz, py - sz, px + sz, py + sz], fill=(232, 180, 128, int(255 * (1 - s))))


# timing of one burrow cycle (s): travel underground, bulge (the tell), breach, up, sink
T_TRAVEL, T_BULGE, T_BREACH, T_UP, T_SINK = 1.1, 0.8, 0.25, 2.0, 0.35
BURROWERS = [  # first surfacing time, spots (always ahead of the ship), kill time
    (0.4, [(150, 330), (300, 280), (210, 380)], None),
    (1.6, [(340, 360), (120, 250)], 8.0),
    (3.1, [(250, 200), (380, 300)], None),
]


def bur_state(t, start, spots, kill):
    """(phase, x, y, phase progress) of a burrower; phases: none, travel, bulge, breach, up,
    sink, gone."""
    cycle = T_BULGE + T_BREACH + T_UP + T_SINK + T_TRAVEL
    if t < start - T_TRAVEL:
        return "none", spots[0][0], spots[0][1] - 90, 0.0
    if kill is not None and t >= kill:
        return "gone", 0, 0, 0
    u = t - start
    k = int(np.floor(u / cycle)) if u >= 0 else -1
    tc = u - k * cycle
    if k >= len(spots):
        return "none", 0, 0, 0
    x, y = spots[k] if k >= 0 else spots[0]
    if k < 0 or tc >= cycle - T_TRAVEL:            # travelling to the next spot
        k2 = k + 1
        if k2 >= len(spots):
            return "none", 0, 0, 0
        s = (tc - (cycle - T_TRAVEL)) / T_TRAVEL if k >= 0 else (u + T_TRAVEL) / T_TRAVEL
        ax, ay = (spots[k] if k >= 0 else (spots[0][0] - 40, spots[0][1] - 90))
        bx, by = spots[k2] if k >= 0 else spots[0]
        s = smooth(s)
        return "travel", ax + (bx - ax) * s, ay + (by - ay) * s, s
    for name, dur in (("bulge", T_BULGE), ("breach", T_BREACH), ("up", T_UP), ("sink", T_SINK)):
        if tc < dur:
            return name, x, y, tc / dur
        tc -= dur
    return "none", x, y, 0


def burrower_scene(v):
    bg = mars_ground(52, craters=3, dunes=0.06)
    shots = Shots()
    cycle = T_BULGE + T_BREACH + T_UP + T_SINK + T_TRAVEL
    for start, spots, kill in BURROWERS:
        for k, (x, y) in enumerate(spots):
            tf = start + k * cycle + T_BULGE + T_BREACH + 0.35
            if kill is None or tf < kill:
                shots.ring(tf, x, y, 10, 105, 4, phase=0.3 * k, r0=24)
    glow = GLOWS_RGB[v]
    pits = []                                       # (t_end_of_sink, x, y)
    frames = []
    for t in times(10.0):
        f = bg.copy()
        for (te, x, y) in pits:
            if t > te:
                crater(f, x, y, 18, max(0.0, 1 - (t - te) / 2.5) * 0.7)
        later = []
        for i, (start, spots, kill) in enumerate(BURROWERS):
            ph, x, y, s = bur_state(t, start, spots, kill)
            seed = i * 7 + int(x)
            if ph == "travel":
                for j in range(6, -1, -1):          # a low ridge with a trail behind it
                    st = bur_state(t - 0.07 * j, start, spots, kill)
                    if st[0] == "travel":
                        sand_bump(f, st[1], st[2], 0.85 * (1 - j / 7), 10, seed + j, t=t)
            elif ph == "bulge":
                sand_bump(f, x, y, 0.3 + 0.9 * s, 16 + 8 * s, seed, cracks=max(0, s - 0.35) / 0.65,
                          glow=glow if v == "b" else None, t=t)
            elif ph in ("breach", "up", "sink"):
                rise = {"breach": s, "up": 1.0, "sink": 1 - s}[ph]
                open_ = 1.0 if ph == "up" and 0.1 < s < 0.6 else (0.5 if ph == "up" else 0.0)
                crater(f, x, y, 20, 1.0)
                if ph == "sink" and s > 0.75 and not any(p[1:] == (x, y) for p in pits):
                    pits.append((t, x, y))
                sp = bur_sprite(v, rise, open_)
                hit = kill is not None and kill - 0.7 < t < kill and int(t * 14) % 2
                if sp is not None:
                    later.append((flash(sp, hit), x, y, ph == "breach", s, seed))
            if kill is not None and kill <= t < kill + 0.6:
                kx, ky = spots[int((kill - start) // cycle)]
                later.append(("boom", kx, ky, (t - kill) / 0.6, 0, 0))
        for sp, x, y, breach, s, seed in later:
            if isinstance(sp, str):
                e6.explosion(f, x, y, breach, 1.3)
                continue
            paste(f, sp, x, y, "ground")
            if breach:
                spray(f, x, y, s, seed=seed)
        shots.draw(f, t)
        r5.draw_player(f, t)
        frames.append(f)
    return frames


def burrower_sheet(v, frames):
    rises = [1 / 3, 2 / 3, 1.0]
    states = [bur_sprite(v, r, 0.0) for r in rises] + [bur_sprite(v, 1.0, 0.5),
                                                      bur_sprite(v, 1.0, 1.0)]
    labs = ["RISE 1/3", "RISE 2/3", "UP, SHUT", "UP, HALF", "UP, OPEN"]
    start, spots, _ = BURROWERS[0]
    x, y = spots[0]
    fi = lambda tt: int(round(tt * FPS))
    fx, fy = bur_state(start + 3.95, *BURROWERS[0])[1:3]      # between two surfacings
    seq = [(fi(start + 3.95), fx, fy), (fi(start + 0.25), x, y), (fi(start + 0.65), x, y),
           (fi(start + 0.9), x, y), (fi(start + 1.45), x, y), (fi(start + 1.9), x, y),
           (fi(start + 3.2), x, y), (fi(start + 3.9), x, y)]
    seq_l = ["FURROW", "BULGE 30%", "BULGE 90%", "BREACH", "UP: FIRE", "RING OUT", "SINK",
             "PIT FADES"]
    ims, _ = crops(frames, seq, 84, seq_l)
    panels = [("SOURCE, 260 PX (UP, MAW OPEN)",
               source_panel(lambda: (burrower_a if v == "a" else burrower_b)(1.0, 1.0))),
              ("STATES (2X): CUT AT THE SAND LINE", grid(states, 5, 2, labels=labs)),
              ("THE TELL, CROPS FROM THE GIF (2X)", grid(ims, 4, 2, labels=seq_l))]
    if v == "a":
        notes = ["BURROWER A - SAND MAW (SLATE CHITIN, LIME GLOW: THE RING DENIES AN AREA)",
                 "68 PX, RADIAL. A RINGED DOME HEAD ON A THICK COLUMN, CROWN OF BONE SPIKES AT THE",
                 "SAND LINE, SIX-TOOTHED MAW OVER A LIME THROAT, EIGHT LIME PORES FIRE THE RING.",
                 "BURIED: ONLY A SMALL FURROW RUNS UNDER THE SAND (NOTHING HITS IT). TELL: THE SAND",
                 "BULGES FOR 0.8 S, CRACKS SPLIT IT, GRIT TRICKLES; THEN IT BREACHES IN A SPRAY,",
                 "FIRES A RING OF 10 ORBS, STAYS UP 2 S (HITTABLE) AND SINKS, LEAVING A FADING PIT.",
                 "IT ALWAYS SURFACES AHEAD OF THE SHIP. THE MIDDLE ONE IS KILLED WHILE UP."]
    else:
        notes = ["BURROWER B - SAND CLAM (BONE CARAPACE, VIOLET GLOW: SHOTS FROM A GUN CORE)",
                 "68 PX, FACES THE SHIP. FOUR IVORY CARAPACE PLATES OVER A VIOLET CORE; THE PLATES",
                 "SLIDE APART TO BARE THE CORE AND SIX PORTS, WHICH FIRE THE RING; TWO SHOVEL CLAWS",
                 "IN FRONT. SAME CYCLE AS A, BUT THE TELL ALSO GLOWS: VIOLET LIGHT SEEPS FROM THE",
                 "CRACKS OF THE BULGE, SO IT READS IN THE DUST STORM AND ON BUSY SAND.",
                 "BURIED: FURROW ONLY, NOTHING HITS IT. UP 2 S, THEN SINKS. MIDDLE ONE KILLED."]
    return r5.build_sheet(f"BURROWER {v.upper()} - SURFACES ONLY AFTER THE SAND BULGES", panels,
                          notes, frames[int(2.75 * FPS)])


def make_burrower():
    for v in ("a", "b"):
        frames = burrower_scene(v)
        save_unit("ground", "burrower", v, burrower_sheet(v, frames), frames)


# ===================================================================== 2 Tendril Anchor (L16)

def anchor_a(pulse=0.0, glow=1.0):
    """Polyp clamp: a squat slate body gripping the wall with three bone roots, a fleshy
    crimson-seamed cone that pays out the tendril from a glowing lip (weak point)."""
    s = 1.0 + 0.06 * pulse

    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_ellipsoid(p, (0, -0.15, 0.2), (0.55 * s, 0.5 * s, 0.36)), V_BODY),
            (sd_capsule(q, (0.3, -0.3, 0.15), (0.78, -0.86, -0.05), 0.13, 0.03), V_BONE),
            (sd_capsule(p, (0, -0.5, 0.15), (0, -1.06, -0.05), 0.13, 0.03), V_BONE),
            (sd_capsule(p, (0, 0.05, 0.3), (0, 0.74, 0.36), 0.4 * s, 0.17), V_SEAM),
            (sd_ellipsoid(q, (0.3, -0.12, 0.4), (0.17, 0.2, 0.12)), V_SAC),
            (sd_torus_z(p, (0, 0.8, 0.38), 0.12, 0.05), V_BONE),
            (sd_sphere(p, (0, 0.8, 0.36), 0.12 + 0.02 * pulse), V_EYE),
            k=0.06,
        )
    return scene, lift(am.vrell("tendril-anchor-a", glow))


def anchor_b(pulse=0.0, glow=1.0):
    """Spine pylon: a bone tripod clamped to the wall, a curved armoured horn leaning over the
    canyon with glowing vanes, a crimson node at its tip that holds the filament."""
    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_ellipsoid(p, (0, -0.32, 0.12), (0.48, 0.4, 0.22)), V_DARK),
            (sd_capsule(q, (0.2, -0.35, 0.1), (0.82, -0.78, -0.05), 0.11, 0.02), V_BONE),
            (sd_capsule(p, (0, -0.5, 0.1), (0, -1.06, -0.05), 0.11, 0.02), V_BONE),
            (sd_capsule(p, (0, -0.25, 0.25), (0, 0.22, 0.6), 0.26, 0.18), V_BODY),
            (sd_capsule(p, (0, 0.22, 0.6), (0, 0.64, 0.8), 0.18, 0.1), V_BODY),
            (sd_plate(q, [(0.1, -0.1), (0.56, 0.16), (0.5, 0.36), (0.1, 0.3)], 0.42, 0.03),
             V_SEAM),
            (sd_capsule(q, (0.2, 0.0, 0.5), (0.34, 0.12, 0.62), 0.05, 0.015), V_BONE),
            (sd_sphere(p, (0, 0.72, 0.84), 0.15 + 0.03 * pulse), V_EYE),
            k=0.05,
        )
    return scene, lift(am.vrell("tendril-anchor-b", glow))


ANC_N = 48
ANC_TIP = 0.82 * ANC_N / 2.3                  # px from the centre to the tendril's root
anchors = {v: rig.ModelSpaceAngleSprites((lambda pulse=0.0, f=f: f(pulse)), ANC_N, 16,
                                         factor=6, colors=28)
           for v, f in (("a", anchor_a), ("b", anchor_b))}

SCROLL = 34.0
T_LEN = 9.2
TFPS = 12.5                     # the scrolling canyon GIF runs at 12.5 fps to stay under 8 MB
BG_H = FH + int(SCROLL * T_LEN) + 4


def tendril_path(p0, p1, t, amp, n=40, seed=0):
    p0, p1 = np.asarray(p0, dtype=np.float64), np.asarray(p1, dtype=np.float64)
    v = p1 - p0
    ln = np.hypot(*v) or 1
    nrm = np.array((-v[1], v[0])) / ln
    us = np.linspace(0, 1, n)
    off = amp * np.sin(np.pi * us) * np.sin(us * 9 - t * 4 + seed)
    return [tuple(p0 + v * u + nrm * o) for u, o in zip(us, off)], us


def draw_tendril(img, v, pts, us, t, heat=None, frame_seed=0, alpha=1.0):
    """``heat`` = (u, amount): where a beam burns it, glowing white-hot there."""
    if len(pts) < 2 or alpha <= 0.02:
        return
    glow = Image.new("RGBA", img.size, (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    lines = Image.new("RGBA", img.size, (0, 0, 0, 0))   # composited, so alpha blends
    d = ImageDraw.Draw(lines)
    a8 = int(255 * alpha)
    if v == "a":
        gd.line(pts, fill=CRIMSON + (int(150 * alpha),), width=12)
        img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(3.5)))
        d.line(pts, fill=(40, 10, 18, a8), width=11)
        d.line(pts, fill=(140, 112, 132, a8), width=8)
        arr = np.array(pts)
        seg = np.diff(arr, axis=0)
        nrm = np.stack([-seg[:, 1], seg[:, 0]], axis=1)
        nrm /= np.linalg.norm(nrm, axis=1, keepdims=True) + 1e-9
        for ph in (0.0, np.pi):                     # two twisted strands
            tw = np.sin(np.linspace(0, 1, len(seg)) * 34 + ph) * 2.5
            strand = arr[:-1] + nrm * tw[:, None]
            d.line([tuple(s) for s in strand], fill=(68, 44, 62, a8), width=2)
        pulse = 0.5 + 0.5 * np.sin(np.asarray(us) * 18 - t * 9)
        for i in range(len(pts) - 1):              # crimson vein, pulses travel along it
            k = 0.35 + 0.65 * pulse[i]
            col = tuple(int(c * k + 40 * (1 - k)) for c in CRIMSON)
            d.line([pts[i], pts[i + 1]], fill=col + (a8,), width=3)
    else:
        rng = np.random.default_rng(frame_seed)
        arr = np.array(pts)
        seg = arr[-1] - arr[0]
        ln = np.hypot(*seg) or 1
        nrm = np.array((-seg[1], seg[0])) / ln
        strands = []
        for k in range(3):
            j = rng.normal(0, 3.2, len(arr))
            j[0] = j[-1] = 0
            j = np.convolve(j, [0.25, 0.5, 0.25], mode="same")
            strands.append([tuple(p + nrm * o) for p, o in zip(arr, j)])
        for s in strands:
            gd.line(s, fill=CRIMSON + (int(150 * alpha),), width=7)
        img.alpha_composite(glow.filter(ImageFilter.GaussianBlur(3.5)))
        for s in strands:
            d.line(s, fill=CRIMSON + (a8,), width=2)
        d.line(strands[0], fill=(255, 196, 196, a8), width=1)
    img.alpha_composite(lines)
    d = ImageDraw.Draw(img)
    if heat is not None:
        u, amt = heat
        i = int(np.clip(u, 0, 1) * (len(pts) - 1))
        x, y = pts[i]
        raster.add_light(img, x, y, 10 + 14 * amt, (255, 170, 120), 0.4 + 0.8 * amt)
        d.ellipse([x - 3 - 2 * amt, y - 3 - 2 * amt, x + 3 + 2 * amt, y + 3 + 2 * amt],
                  fill=(255, 236, 200, 255))


def anchor_pts(wy):
    xl, xr = wall_l(wy), wall_r(wy)
    a = (xl + 2, wy)
    b = (xr - 2, wy)
    return a, b


def ion_beam_mock(img, x, y_top, y_ship, t):
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    d.line([(x, y_ship - 18), (x, y_top)], fill=(80, 170, 255, 150), width=11)
    img.alpha_composite(lay.filter(ImageFilter.GaussianBlur(3)))
    d = ImageDraw.Draw(img)
    w = 4 + int(t * 30) % 2
    d.line([(x, y_ship - 18), (x, y_top)], fill=(120, 200, 255, 255), width=w)
    d.line([(x, y_ship - 18), (x, y_top)], fill=(235, 250, 255, 255), width=2)
    raster.add_light(img, x, y_top, 12, (160, 220, 255), 1.0)


def tendril_scene(v):
    bg = canyon(61, BG_H)
    top0 = BG_H - FH
    wy1 = top0 + 110                               # pair 1: broken by killing the left anchor
    wy2 = top0 - 150                               # pair 2: burnt through by the beam mock
    wy3 = top0 - 300                               # pair 3: enters late, still intact
    K1 = 3.7                                       # left anchor of pair 1 dies
    B_ON, B_CUT, B_OFF = 6.3, 8.0, 8.6             # beam on, tendril parts, beam off
    xk = wall_l(wy1) + 28                          # ship x that hits the left anchor's cone
    frames = []
    for t in [i / TFPS for i in range(int(T_LEN * TFPS))]:
        top = top0 - SCROLL * t
        f = bg_crop(bg, top)
        # player route: over to the left anchor, shoot it, back to the middle, beam
        if t < 1.6:
            px = 240 + (xk - 240) * smooth(t / 1.6)
        elif t < 4.2:
            px = xk + 2 * np.sin(t * 3)
        elif t < 6.0:
            px = xk + (240 - xk) * smooth((t - 4.2) / 1.8)
        else:
            px = 240 + 6 * np.sin(t * 1.3)
        py = 470
        hits = []
        for wy, kill, cut, seed in ((wy1, K1, None, 0.0), (wy2, None, (B_ON, B_CUT), 1.7),
                                    (wy3, None, None, 3.1)):
            a, b = anchor_pts(wy)
            ay, by_ = a[1] - top, b[1] - top
            if ay < -60 or ay > FH + 60:
                continue
            pulse = 0.5 + 0.5 * np.sin(t * 5 + seed)
            ta = (a[0] + ANC_TIP, ay)
            tb = (b[0] - ANC_TIP, by_)
            if kill is not None and t >= kill:     # free end whips back into the right anchor
                s = smooth((t - kill) / 0.7)
                ln = 1.0 - 0.82 * s
                pts, us = tendril_path(tb, (tb[0] + (ta[0] - tb[0]) * ln, ta[1] + 8 * s), t,
                                       4 + 14 * np.sin(np.pi * min(1, s * 1.4)), seed=seed)
                draw_tendril(f, v, pts, us, t, frame_seed=int(t * FPS) + 11,
                             alpha=1.0 if v == "a" else max(0.0, 1 - s * 1.2))
            elif cut is not None and t >= cut[1]:  # cut in two by the beam: both halves recoil
                s = smooth((t - cut[1]) / 0.7)
                u_cut = (px - ta[0]) / (tb[0] - ta[0])
                for p0, p1, lnr in ((ta, tb, u_cut), (tb, ta, 1 - u_cut)):
                    ln = lnr * (1.0 - 0.8 * s)
                    end = (p0[0] + (p1[0] - p0[0]) * ln, p0[1] + (p1[1] - p0[1]) * ln + 6 * s)
                    pts, us = tendril_path(p0, end, t, 3 + 8 * np.sin(np.pi * min(1, s * 1.4)),
                                           n=24, seed=seed)
                    draw_tendril(f, v, pts, us, t, frame_seed=int(t * FPS) + 5,
                                 alpha=1.0 if v == "a" else max(0.0, 1 - s * 1.2))
            else:
                pts, us = tendril_path(ta, tb, t, 3.0, seed=seed)
                heat = None
                if cut is not None and t >= cut[0] and ay < py:
                    u_cut = (px - ta[0]) / (tb[0] - ta[0])
                    heat = (u_cut, (t - cut[0]) / (cut[1] - cut[0]))
                    hits.append(ay)
                draw_tendril(f, v, pts, us, t, heat=heat, frame_seed=int(t * FPS))
            for (x, y), head, dead in ((a, 0.0, kill is not None and t >= kill),
                                       (b, np.pi, False)):
                sy = y - top
                if dead:
                    if t < kill + 0.6:
                        e6.explosion(f, x, sy, (t - kill) / 0.6, 1.1)
                    continue
                sp = anchors[v].get(head, pulse=round(pulse * 2) / 2)
                hit = kill is not None and head == 0.0 and kill - 1.4 < t < kill \
                    and int(t * 14) % 2
                paste(f, flash(sp, hit), x, sy, "ground")
        beam = B_ON <= t < B_OFF
        if beam:
            y_top = max(hits) if hits and t < B_CUT else -5
            ion_beam_mock(f, px, y_top, py, t)
        stop = (wy1 - top) if t < K1 and 1.0 < t else None
        draw_ship(f, px, py, shots=not beam and t < 6.0, stop_y=stop, t=t)
        if beam:
            raster.draw_text(f, int(px) + 14, py - 70, "BEAM (MOCK-UP)", (150, 210, 255))
        frames.append(f)
    return frames


def tendril_sheet(v, frames):
    heads = [anchors[v].frame(k, pulse=0.5) for k in range(0, 16, 2)]
    pulses = [anchors[v].get(0.0, pulse=p) for p in (0.0, 0.5, 1.0)]
    canvas = Image.new("RGBA", (300, 70), (24, 14, 12, 255))
    pts, us = tendril_path((10, 35), (290, 35), 0.5, 3.0)
    draw_tendril(canvas, v, pts, us, 0.5, frame_seed=3)
    canvas2 = Image.new("RGBA", (300, 70), (24, 14, 12, 255))
    pts, us = tendril_path((10, 35), (290, 35), 0.5, 3.0)
    draw_tendril(canvas2, v, pts, us, 0.5, heat=(0.55, 0.9), frame_seed=4)
    panels = [("SOURCE, 260 PX", source_panel(lambda: (anchor_a if v == "a" else anchor_b)(0.5),
                                              heading=0.0)),
              ("8 OF 16 HEADINGS (2X)", grid(heads, 4, 2)),
              ("PULSE 3 FRAMES (2X)", grid(pulses, 3, 2)),
              ("THE LINK, 1X", canvas), ("BURNING (BEAM AT 90%), 1X", canvas2)]
    if v == "a":
        notes = ["TENDRIL ANCHOR A - POLYP CLAMP AND FLESH ROPE (SLATE, CRIMSON: A LINE OF DANGER)",
                 "48 PX, CLAMPED TO THE CANYON WALL BY THREE BONE ROOTS. A FLESHY CONE PAYS OUT A",
                 "TWISTED SINEW ROPE WITH A CRIMSON VEIN; PULSES RUN ALONG IT AND IT SWAYS SLOWLY.",
                 "KILL AN ANCHOR: THE ROPE WHIPS BACK INTO THE OTHER ONE AND HANGS LIMP (HARMLESS).",
                 "A BEAM BURNS IT WHITE-HOT WHERE IT CROSSES AND CUTS IT; BOTH HALVES RECOIL.",
                 "THE GLOWING LIP AT THE CONE'S TIP IS THE WEAK POINT. BLUE BEAM = MOCK-UP ONLY."]
    else:
        notes = ["TENDRIL ANCHOR B - SPINE PYLON AND CRACKLING FILAMENT (SLATE, CRIMSON)",
                 "48 PX. A BONE TRIPOD ON THE WALL, A CURVED HORN LEANING OVER THE CANYON WITH",
                 "GLOWING VANES, A CRIMSON NODE AT THE TIP (WEAK POINT). THE LINK IS THREE JAGGED",
                 "CRIMSON STRANDS RE-DRAWN EVERY FRAME, LIKE A LASER (THE MANTIS SWEEP'S HUE).",
                 "KILL AN ANCHOR: THE FILAMENT SNAPS BACK AND FADES. A BEAM HEATS IT AND CUTS IT.",
                 "READS AS A HARD LINE; LESS ORGANIC THAN A. BLUE BEAM = MOCK-UP ONLY."]
    return r5.build_sheet(f"TENDRIL ANCHOR {v.upper()} - A DAMAGING LINK ACROSS THE CANYON",
                          panels, notes, frames[int(2.6 * TFPS)])


def make_tendril():
    for v in ("a", "b"):
        frames = tendril_scene(v)
        save_unit("ground", "tendril-anchor", v, tendril_sheet(v, frames), frames, TFPS)


# ===================================================================== 3 Choir Herald (L17)

def herald_a(beat=0.0, glow=1.0, crest=1.0):
    """Cantor bell: a teal-black manta-like body, broad membrane wings, a bone crown on the
    back around a glowing teal resonance sac (the crest, weak point) with a fan of bone pipes
    behind it, two trailing tails."""
    wing = [(0.3, 0.38), (1.08, -0.02), (1.02, -0.36), (0.7, -0.3), (0.34, -0.48)]

    def scene(p):
        q = mirror_x(p)
        wq = em._hinge(q, (0.32, 0, 0.0), 0.22 * beat, axis="y")
        d, m = union(
            (sd_ellipsoid(p, (0, 0.05, 0), (0.42, 0.74, 0.26)), V_BODY),
            (sd_plate(wq, wing, 0.0, 0.06, 0.02,
                      taper=lambda x, y: np.clip(1.3 - 0.8 * x, 0.3, 1)), V_DARK),
            (sd_ellipsoid(p, (0, 0.7, 0.02), (0.24, 0.24, 0.17)), V_BODY),
            k=0.08,
        )
        items = [(d, m),
                 (sd_sphere(q, (0.12, 0.8, 0.12), 0.05), V_GLOW),
                 (sd_ellipsoid(wq, (0.62, -0.12, 0.05), (0.18, 0.1, 0.04)), V_SAC),
                 (sd_torus_z(p, (0, -0.02, 0.26), 0.25, 0.06), V_BONE),
                 (sd_sphere(p, (0, -0.02, 0.2), 0.21 + 0.02 * crest), V_EYE),
                 (sd_capsule(q, (0.12, -0.62, 0), (0.22, -1.1, -0.05), 0.08, 0.02), V_SEAM)]
        for ang in np.linspace(-0.9, 0.9, 5):
            r0, r1 = 0.28, 0.52 + 0.12 * np.cos(ang)
            items.append((sd_capsule(p, (np.sin(ang) * r0, -0.02 - np.cos(ang) * r0, 0.28),
                                     (np.sin(ang) * r1, -0.02 - np.cos(ang) * r1, 0.36),
                                     0.055, 0.03), V_BONE))
        return union(*items, k=0.03)
    mats = am.vrell("choir-herald-a", glow)
    mats[V_EYE] = replace(mats[V_EYE], emission=tuple(np.array(mats[V_EYE].emission) * crest))
    return scene, mats


def herald_b(beat=0.0, glow=1.0, crest=1.0):
    """Seraph: a slim ivory body with two pairs of veined wings and a floating halo of bone
    above its back, six violet nodes on the halo (the crest, weak point)."""
    fore = [(0.12, 0.3), (0.5, 0.6), (0.92, 0.66), (1.06, 0.42), (0.74, 0.1), (0.14, 0.0)]
    hind = [(0.12, -0.04), (0.62, -0.16), (0.86, -0.46), (0.6, -0.8), (0.28, -0.62),
            (0.12, -0.36)]

    def scene(p):
        q = mirror_x(p)
        fq = em._hinge(q, (0.15, 0, 0.0), 0.3 * beat, axis="y")
        hq = em._hinge(q, (0.15, 0, 0.0), -0.2 * beat, axis="y")
        d, m = union(
            (sd_capsule(p, (0, 0.66, 0.05), (0, -0.85, 0.0), 0.2, 0.08), V_BODY),
            (sd_ellipsoid(p, (0, 0.15, 0.05), (0.27, 0.42, 0.2)), V_BODY),
            (sd_plate(fq, fore, 0.02, 0.045, 0.08,
                      taper=lambda x, y: np.clip(1.3 - 0.7 * x, 0.35, 1)), V_SAC),
            (sd_plate(hq, hind, 0.0, 0.04, 0.08,
                      taper=lambda x, y: np.clip(1.3 - 0.7 * x, 0.35, 1)), V_DARK),
            k=0.07,
        )
        items = [(d, m),
                 (sd_sphere(p, (0, 0.76, 0.08), 0.16), V_BODY),
                 (sd_sphere(q, (0.09, 0.86, 0.16), 0.045), V_GLOW),
                 (sd_torus_z(p, (0, 0.08, 0.5), 0.42, 0.05), V_BONE)]
        for ang in np.linspace(0, TAU, 3, endpoint=False) + np.pi / 2:
            items.append((sd_capsule(p, (0, 0.08, 0.2), (np.cos(ang) * 0.38,
                                                          0.08 + np.sin(ang) * 0.38, 0.48),
                                     0.035, 0.03), V_BONE))
        for ang in np.linspace(0, TAU, 6, endpoint=False):
            items.append((sd_sphere(p, (np.cos(ang) * 0.42, 0.08 + np.sin(ang) * 0.42, 0.52),
                                    0.08 + 0.01 * crest), V_EYE))
        return union(*items, k=0.02)
    mats = am.vrell("choir-herald-b", glow)
    mats[V_EYE] = replace(mats[V_EYE], emission=tuple(np.array(mats[V_EYE].emission) * crest))
    return scene, mats


HER_N = 80
herald = {v: rig.ModelSpaceAngleSprites((lambda beat=0.0, crest=1.0, f=f: f(beat, 1.0, crest)),
                                        HER_N, 16, factor=5, colors=36)
          for v, f in (("a", herald_a), ("b", herald_b))}
ND = e4.R04["needler-a"]


def needler(anim):
    return e3.render(ND[4], ND[3], round(float(np.clip(anim, -1, 1)), 1))[1]


AURA_R = 90
ESC_N, ESC_R = 6, 58
H_DIE = 7.6
TARGET = 0                                         # escort shot through its ring first


def herald_pos(t):
    y = -60 + 230 * smooth(t / 2.2)
    return 240 + 50 * np.sin(t * 0.55), y


def escort_pos(i, t):
    hx_, hy = herald_pos(min(t, H_DIE))
    a = i * TAU / ESC_N + t * 0.8
    x, y = hx_ + np.cos(a) * ESC_R, hy + np.sin(a) * ESC_R
    if t > H_DIE:                                   # formation breaks: scatter away
        s = t - H_DIE
        x += np.cos(a) * 40 * s
        y += 70 * s * s
    return x, y


def ring_frac(t):
    """Absorb left on the targeted escort (1 = full)."""
    return float(np.clip(1 - (t - 2.8) / 1.8, 0, 1))


def draw_aura_ring(img, v, x, y, frac, alpha, hit, r=21):
    if alpha <= 0:
        return
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    col = TEAL if v == "a" else VIOLET
    hot = (220, 255, 240) if v == "a" else (226, 206, 255)
    c = hot if hit else col
    box = [x - r, y - r, x + r, y + r]
    if v == "a":
        d.ellipse(box, fill=col + (int(38 * alpha),))
        d.ellipse(box, outline=c + (int(90 * alpha),), width=1)
        if frac > 0:
            d.arc(box, -90, -90 + 360 * frac, fill=c + (int(255 * alpha),), width=2)
    else:
        n = int(np.ceil(frac * 6 - 1e-6))
        for k in range(n):
            a0 = -90 + k * 60 + 6
            d.arc(box, a0, a0 + 48, fill=c + (int(255 * alpha),), width=3)
    img.alpha_composite(lay)


def herald_scene(v):
    bg = mars_ground(83, craters=7)
    shots = Shots()
    for t0 in np.arange(2.6, 7.4, 1.3):            # the escorts are Needlers: aimed shots
        for i in (1, 3, 5):
            x, y = escort_pos(i, t0)
            shots.fire(t0 + i * 0.05, x, y, *r5.player_at(t0), 150, 4)
    deaths = {TARGET: 5.3, 2: 8.5, 4: 9.1}
    frames = []
    for t in times(10.0):
        f = bg.copy()
        hx_, hy = herald_pos(min(t, H_DIE))
        tgt = None
        if 2.6 <= t < deaths[TARGET]:
            tgt = escort_pos(TARGET, t)
        elif 5.6 <= t < H_DIE:
            tgt = (hx_, hy)
        elif H_DIE + 0.3 <= t < deaths[2]:
            tgt = escort_pos(2, t)
        elif deaths[2] <= t < deaths[4]:
            tgt = escort_pos(4, t)
        base = r5.player_at(t)[0]
        px = base if tgt is None else tgt[0]
        py = 488
        # aura reach
        alive = t < H_DIE
        ring_alpha = 1.0 if alive else max(0.0, 1 - (t - H_DIE) / 0.4)
        if alive and hy > 0:
            lay = Image.new("RGBA", f.size, (0, 0, 0, 0))
            d = ImageDraw.Draw(lay)
            if v == "a":                             # sound waves rolling out to the reach
                for k in range(3):
                    u = (t * 0.8 + k / 3) % 1.0
                    r = 16 + (AURA_R - 16) * u
                    d.ellipse([hx_ - r, hy - r, hx_ + r, hy + r], outline=TEAL + (int(120 * (1 - u)),),
                              width=2)
            else:                                    # dotted reach circle and threads
                for k in range(36):
                    a = k * TAU / 36 + t * 0.4
                    xx, yy = hx_ + np.cos(a) * AURA_R, hy + np.sin(a) * AURA_R
                    d.ellipse([xx - 1.5, yy - 1.5, xx + 1.5, yy + 1.5], fill=VIOLET + (150,))
                for i in range(ESC_N):
                    if i == TARGET and t > 4.6:
                        continue
                    ex, ey = escort_pos(i, t)
                    e3.dashed(lay, hx_, hy, ex, ey, VIOLET, dash=4)
                lay.putalpha(lay.getchannel("A").point(lambda a: int(a * 0.55)))
            f.alpha_composite(lay)
        draws = []
        for i in range(ESC_N):
            if i in deaths and t >= deaths[i]:
                if t < deaths[i] + 0.6:
                    ex, ey = escort_pos(i, deaths[i])
                    e6.explosion(f, ex, ey, (t - deaths[i]) / 0.6)
                continue
            ex, ey = escort_pos(i, t)
            sp = needler(np.sin(t * 9 + i))
            frac = ring_frac(t) if i == TARGET else 1.0
            hit = tgt is not None and i in (TARGET, 2, 4) and abs(tgt[0] - ex) < 1 \
                and int(t * 14) % 2
            draws.append((sp, ex, ey, frac, hit, i))
        for sp, ex, ey, frac, hit, i in draws:
            e3.place(f, flash(sp, hit and frac <= 0), ex, ey, "air")
            if frac > 0 or (i == TARGET and t < 4.8):
                pop = i == TARGET and 4.6 <= t < 4.8
                draw_aura_ring(f, v, ex, ey, frac, ring_alpha * (0.4 if pop else 1.0),
                               hit or pop)
        if alive:
            beat = np.sin(t * 3.0)
            crest_hit = 5.6 <= t < H_DIE and int(t * 14) % 2
            sp = herald[v].get(DOWN, beat=round(beat * 2) / 2, crest=1.0)
            e3.place(f, flash(sp, crest_hit), hx_, hy, "air")
            glow = TEAL if v == "a" else VIOLET
            raster.add_light(f, hx_, hy - (2 if v == "a" else 8), 16, glow,
                             0.5 + 0.3 * np.sin(t * 6))
        elif t < H_DIE + 0.9:
            s = (t - H_DIE) / 0.9
            e6.explosion(f, hx_, hy, s, 2.0)
            if v == "a":
                r = 20 + 120 * s
                d = ImageDraw.Draw(f)
                d.ellipse([hx_ - r, hy - r, hx_ + r, hy + r], outline=TEAL + (int(200 * (1 - s)),),
                          width=2)
        shots.draw(f, t)
        stop = tgt[1] + 10 if tgt is not None else None
        draw_ship(f, px, py, stop_y=stop, t=t)
        frames.append(f)
    return frames


def herald_sheet(v, frames):
    heads = [herald[v].frame(k, beat=0.0, crest=1.0) for k in range(0, 16, 4)]
    beats = [herald[v].get(DOWN, beat=b, crest=1.0) for b in (-1.0, -0.5, 0.0, 0.5, 1.0)]
    rings = []
    for frac, hit in ((1.0, False), (0.66, True), (0.33, False), (0.0, False)):
        c = Image.new("RGBA", (56, 56), (40, 22, 16, 255))
        e3.place(c, needler(0.0), 28, 28, None)
        if frac > 0:
            draw_aura_ring(c, v, 28, 28, frac, 1.0, hit)
        rings.append(c)
    panels = [("SOURCE, 260 PX", source_panel(lambda: (herald_a if v == "a" else herald_b)())),
              ("WING BEAT 5 FRAMES (1X)", grid(beats, 5, 1)),
              ("4 OF 16 HEADINGS (1X)", grid(heads, 4, 1)),
              ("ESCORT RING: FULL / HIT / 1/3 / GONE (2X)",
               grid(rings, 4, 2, labels=["FULL", "HIT", "1/3", "STRIPPED"]))]
    if v == "a":
        notes = ["CHOIR HERALD A - CANTOR BELL (TEAL-BLACK CHITIN, TEAL GLOW: SUPPORT / CARRIER)",
                 "80 PX. A MANTA-LIKE BELL WITH BROAD MEMBRANE WINGS; ON ITS BACK A BONE CROWN AROUND",
                 "A GLOWING TEAL RESONANCE SAC (THE CREST, WEAK POINT) WITH A FAN OF BONE PIPES.",
                 "AURA: SOUND WAVES ROLL OUT TO THE 90 PX REACH; EVERY ESCORT INSIDE WEARS A TEAL",
                 "BUBBLE WHOSE BRIGHT ARC SHOWS THE ABSORB LEFT. SHOOTING AN ESCORT DRAINS ITS ARC;",
                 "AT ZERO THE BUBBLE POPS AND IT TAKES DAMAGE. KILL THE HERALD: ALL BUBBLES FADE.",
                 "ESCORTS HERE ARE NEEDLERS (CIRCLE FORMATION) FOR SCALE."]
    else:
        notes = ["CHOIR HERALD B - SERAPH (BONE CHITIN, VIOLET GLOW)",
                 "80 PX. A SLIM IVORY BODY WITH TWO PAIRS OF VEINED WINGS AND A BONE HALO FLOATING",
                 "ABOVE ITS BACK; SIX VIOLET NODES ON THE HALO ARE THE CREST (WEAK POINT).",
                 "AURA: A SLOWLY TURNING DOTTED CIRCLE MARKS THE 90 PX REACH, FAINT THREADS RUN TO",
                 "EACH PROTECTED ESCORT. THE ESCORT'S RING HAS SIX SEGMENTS THAT DROP OFF ONE BY ONE",
                 "AS IT ABSORBS (EASY TO COUNT). KILL THE HERALD: THREADS AND RINGS VANISH.",
                 "ESCORTS HERE ARE NEEDLERS (CIRCLE FORMATION) FOR SCALE."]
    return r5.build_sheet(f"CHOIR HERALD {v.upper()} - AURA SHIELDS ITS ESCORTS", panels, notes,
                          frames[int(3.6 * FPS)])


def make_herald():
    for v in ("a", "b"):
        frames = herald_scene(v)
        save_unit("air", "choir-herald", v, herald_sheet(v, frames), frames)


# ===================================================================== 4 Ghost Drone (L18)

GD_EYE = (0.0, 0.6)                    # model x/y of the Ghost Drone a's sensor eye
GD_TIPS = ((-0.95, -0.48), (0.95, -0.48))   # ... and of its two wing-tip running lights
A_LAMP = 7                             # the extra material slot: the white running lights


def ghost_a(glow=1.0):
    """Facet dart: a flat faceted arrowhead (stealth-like planes), a dark sensor spine with a
    single red eye, two ventral guns, a slit engine. Plain grey, no markings.
    U.5 (round 36's review): the eye is a 0.08-unit lens (about 3 px at 40 px, at least 2x2 px
    of emissive red) and white running lights sit on both wing tips; their glow is the additive
    layer ``ghost_glow`` drawn over the sprite, so the silhouette reads inside the dust storm."""
    hull = [(0.0, 1.02), (0.98, -0.5), (0.62, -0.42), (0.4, -0.72), (0.0, -0.52)]

    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_plate(q, hull, 0.0, 0.17, 0.0, taper=am._roof(0.95)), A_HULL),
            (sd_plate(q, [(0.0, 0.72), (0.16, 0.3), (0.16, -0.22), (0.0, -0.32)], 0.13, 0.07),
             A_FACET),
            (sd_box(q, (0.3, 0.05, 0.13), (0.12, 0.022, 0.02)), A_GLASS),
            (sd_ellipsoid(p, (0, GD_EYE[1], 0.19), (0.085, 0.08, 0.06)), A_RED),
            (sd_sphere(q, (-GD_TIPS[0][0], GD_TIPS[0][1], 0.04), 0.055), A_LAMP),
            (sd_box(q, (0.24, 0.42, -0.04), (0.03, 0.24, 0.03), 0.01), A_GUN),
            (sd_box(q, (0.17, -0.6, 0.04), (0.09, 0.05, 0.03)), A_ENGINE),
        )
    mats = unmarked_mats(glow, hull_pattern=panel_lines(6.0, 0.05, 0.78))
    mats.append(Material((0.12, 0.12, 0.12), emission=(1.3 * glow, 1.3 * glow, 1.3 * glow)))
    return scene, mats


def ghost_glow(heading, glow=1.0, size=48):
    """The additive light layer of Ghost Drone a at the sprite's (quantized) heading: a red bloom
    round the sensor eye and a white point with a small halo on each wing tip (vfx_r08 light
    fields, drawn with ``v8.additive`` like the Vrell glows). Centred like the sprite."""
    k = ghost["a"].index(heading)
    h = k * TAU / GD_N_HEADINGS
    scale = GD_N / 2.3
    cv = v8.Canvas(size, size)
    ex, ey = rig.local_to_screen(*GD_EYE, h, scale)
    d = cv.dist(size / 2 + ex, size / 2 + ey)
    cv.add((255, 40, 30), v8.gauss(d, 3.2) * 0.55 * glow)
    cv.add((255, 120, 90), v8.gauss(d, 1.2) * 0.9 * glow)
    for tip in GD_TIPS:
        tx, ty = rig.local_to_screen(*tip, h, scale)
        d = cv.dist(size / 2 + tx, size / 2 + ty)
        cv.add((200, 220, 255), v8.gauss(d, 2.4) * 0.4 * glow)
        cv.add((255, 255, 255), v8.gauss(d, 0.9) * 1.0 * glow)
    return cv.image()


def ghost_with_glow(base, heading, glow=1.0, size=48):
    """The sprite (or its hit-flash) with the additive light layer on top, on a ``size`` canvas."""
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    sprite.paste_center(img, base, size // 2, size // 2)
    v8.additive(img, ghost_glow(heading, glow, size), size / 2, size / 2)
    return img


def ghost_b(glow=1.0):
    """Crescent: a forward-swept crescent wing round a long sensor pod, gun pods at the wing
    tips, two canted tail fins. Plain grey, no markings."""
    wing = [(0.1, 0.18), (0.9, 0.62), (1.04, 0.5), (0.98, 0.3), (0.12, -0.36)]

    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_ellipsoid(p, (0, 0.04, 0.08), (0.2, 0.78, 0.18)), A_FACET),
            (sd_plate(q, wing, 0.0, 0.08, 0.0, taper=am._roof(0.6)), A_HULL),
            (sd_capsule(q, (0.96, 0.78, 0.02), (0.96, 0.25, 0.02), 0.07, 0.06), A_GUN),
            (sd_plate(q, [(0.06, -0.42), (0.36, -0.86), (0.26, -0.92), (0.05, -0.72)], 0.12,
                      0.03), A_HULL),
            (sd_ellipsoid(p, (0, 0.4, 0.2), (0.08, 0.2, 0.05)), A_GLASS),
            (sd_sphere(p, (0, 0.74, 0.16), 0.06), A_RED),
            (sd_box(p, (0, -0.78, 0.08), (0.1, 0.05, 0.06)), A_ENGINE),
        )
    return scene, unmarked_mats(glow, hull_pattern=panel_lines(7.0, 0.05, 0.78))


class LampSprites(rig.ModelSpaceAngleSprites):
    """Ghost Drone a's angle set with its lamps set as solid pixels after the palette step (the
    quantizer would otherwise merge a few emissive pixels into the grey): a 2x2 red eye and one
    white pixel on each wing tip, both dimmed with ``glow`` (the EMP's lights-out)."""

    def frame(self, k, **kw):
        key = (k, tuple(sorted(kw.items())))
        if key not in self.lit:
            sp = super().frame(k, **kw).copy()
            a = np.array(sp)
            g = float(kw.get("glow", 1.0))
            h = k * TAU / self.count
            scale = self.size[0] / self.extent
            c = self.size[0] / 2
            ex, ey = rig.local_to_screen(*GD_EYE, h, scale)
            x0, y0 = int(np.floor(c + ex - 0.5)), int(np.floor(c + ey - 0.5))
            eye = np.array((255, 64, 48)) * g + np.array((60, 22, 20)) * (1 - g)
            a[y0:y0 + 2, x0:x0 + 2, :3] = eye.astype(np.uint8)
            a[y0:y0 + 2, x0:x0 + 2, 3] = 255
            lamp = np.array((240, 244, 255)) * g + np.array((70, 72, 78)) * (1 - g)
            for tip in GD_TIPS:
                tx, ty = rig.local_to_screen(*tip, h, scale)
                ys, xs = np.nonzero(a[..., 3] > 0)              # the opaque pixel nearest the tip
                i = int(np.argmin((xs + 0.5 - c - tx) ** 2 + (ys + 0.5 - c - ty) ** 2))
                a[ys[i], xs[i], :3] = lamp.astype(np.uint8)
            self.lit[key] = Image.fromarray(a, "RGBA")
        return self.lit[key]


GD_N, GD_N_HEADINGS = 40, 16
ghost = {"a": LampSprites(ghost_a, GD_N, GD_N_HEADINGS, factor=6, colors=28, post=grey_rim),
         "b": rig.ModelSpaceAngleSprites(ghost_b, GD_N, GD_N_HEADINGS, factor=6, colors=28,
                                         post=grey_rim)}
ghost["a"].lit = {}


def _line_path(i):
    x = 120 + i * 80
    return rig.catmull_rom([(x, -40), (x, 90), (x + 30, 150), (x + 160, 175), (x + 420, 190)],
                           [0.3, 1.2, 1.7, 2.5, 4.0])


def _pincer_path(i):
    side = -1 if i < 2 else 1
    k = i % 2
    x0 = -40 if side < 0 else FW + 40
    return rig.catmull_rom([(x0, 60 + 50 * k), (240 - side * 120, 130 + 50 * k),
                            (240 + side * 30, 260 + 40 * k), (240 + side * 200, 420 + 30 * k),
                            (240 + side * 320, 470)],
                           [4.6, 5.6, 6.6, 7.6, 8.6])


DRONES = [(_line_path(i), 0.3, 4.0) for i in range(4)] + \
         [(_pincer_path(i), 4.6, 8.6) for i in range(4)]
GD_KILLS = {1: 3.1, 6: 7.2}


def ghost_scene(v):
    bg = mars_ground(97, craters=9)
    haze = raster.fbm(FW, FH * 2, 90, 5, octaves=3, period=False)
    shots = Shots()
    for i, (path, t0, t1) in enumerate(DRONES):
        for tb in np.arange(t0 + (1.3 if i < 4 else 1.0) + 0.15 * (i % 4), t1 - 0.4, 1.6):
            if i in GD_KILLS and tb > GD_KILLS[i]:
                continue
            x, y = path(tb)
            if not (0 < x < FW and 0 < y < FH - 60):
                continue
            px, py = r5.player_at(tb)
            for k in range(3):                       # precise 3-round burst, one aim point
                shots.fire(tb + k * 0.07, x, y, px, py, 260, 3)
    frames = []
    for t in times(9.0):
        f = bg.copy()
        a = np.array(f).astype(np.float64)          # drifting dust haze (storm plains)
        off = int(t * 30) % FH
        hz = haze[off:off + FH]
        a[..., :3] = a[..., :3] * (1 - 0.18 * hz[..., None]) + np.array((230, 150, 100)) * \
            0.18 * hz[..., None]
        f = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
        for i, (path, t0, t1) in enumerate(DRONES):
            if not t0 <= t < t1:
                continue
            if i in GD_KILLS and t >= GD_KILLS[i]:
                if t < GD_KILLS[i] + 0.6:
                    e6.explosion(f, *path(GD_KILLS[i]), (t - GD_KILLS[i]) / 0.6)
                continue
            x, y = path(t)
            h = rig.path_heading(path, t)
            hit = i in GD_KILLS and GD_KILLS[i] - 0.5 < t and int(t * 14) % 2
            e3.place(f, flash(ghost[v].get(h), hit), x, y, "air")
            if v == "a":                             # U.5: the additive eye and running lights
                v8.additive(f, ghost_glow(h), x, y)
        shots.draw(f, t)
        r5.draw_player(f, t)
        frames.append(f)
    return frames


def ghost_sheet(v, frames):
    heads = [ghost[v].frame(k) for k in range(16)]
    if v == "a":                                         # U.5: with the additive lights
        heads = [ghost_with_glow(sp, k * TAU / GD_N_HEADINGS) for k, sp in enumerate(heads)]
    asc = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    sprite.paste_center(asc, e4.render(e4.R04["talon-a"][4], 36)[1], 24, 24)
    me = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    sprite.paste_center(me, ghost[v].get(DOWN) if v == "b" else
                        ghost_with_glow(ghost[v].get(DOWN), DOWN), 24, 24)
    ply = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
    sprite.paste_center(ply, e3.player_sprite(), 24, 24)
    panels = [("SOURCE, 260 PX", source_panel(lambda: (ghost_a if v == "a" else ghost_b)())),
              ("16 HEADINGS (2X)" + (", WITH THE ADDITIVE LIGHTS" if v == "a" else ""),
               grid(heads, 8, 2)),
              ("NEXT TO A TALON (ASCENDANCY) AND THE PLAYER (2X)",
               grid([asc, me, ply], 3, 2, labels=["TALON", "GHOST", "PLAYER"]))]
    if v == "a":
        notes = ["GHOST DRONE A - FACET DART (UNMARKED: PLAIN GREY, NO TRIM, NEUTRAL GREY RIM)",
                 "40 PX. A FLAT FACETED ARROWHEAD OF STEALTH-LIKE PLANES, A DARK SENSOR SPINE WITH",
                 "ONE RED EYE, TWO VENTRAL GUNS, A SLIT ENGINE. NO VRELL BIOLOGY, NO GOLD, NO",
                 "FACTION ACCENT: CLEARLY HUMAN-MADE, OWNED BY NOBODY. ONLY THE RED SENSOR AND THE",
                 "ENGINE GLOW MATCH ASCENDANCY HARDWARE (THE HINT). STRAFE: LINE ABREAST FROM THE TOP",
                 "THEN A PINCER FROM BOTH SIDES; EACH FIRES PRECISE 3-ROUND BURSTS AT ONE AIM POINT.",
                 "U.5 FIX (CHOSEN): A 2X2 PX EMISSIVE RED EYE AND A WHITE RUNNING LIGHT ON EACH WING",
                 "TIP, WITH A RED BLOOM AND WHITE HALOS DRAWN ADDITIVELY (LIKE THE VRELL GLOWS), SO",
                 "THE SHAPE READS IN THE DUST STORM FROM THE SPRITE ITSELF."]
    else:
        notes = ["GHOST DRONE B - CRESCENT (UNMARKED: PLAIN GREY, NO TRIM, NEUTRAL GREY RIM)",
                 "40 PX. A FORWARD-SWEPT CRESCENT WING AROUND A LONG SENSOR POD, GUN PODS AT THE",
                 "WING TIPS, TWO CANTED TAIL FINS, ONE RED SENSOR EYE. READS MORE LIKE A UTC DESIGN",
                 "GONE WRONG THAN A (WHICH IS COLDER AND MORE ALIEN IN SHAPE). SAME STRAFE AND",
                 "PRECISE 3-ROUND BURSTS; LINE ABREAST, THEN A PINCER FROM BOTH SIDES."]
    return r5.build_sheet(f"GHOST DRONE {v.upper()} - GREY, UNMARKED, HUMAN-BUILT", panels, notes,
                          frames[int(5.9 * FPS)])


def make_ghost(variants=("a", "b")):
    for v in variants:
        frames = ghost_scene(v)
        save_unit("air", "ghost-drone", v, ghost_sheet(v, frames), frames)


# ===================================================================== 5 Warden Tank, unmarked (L19)

def grey_tank(model):
    scene, mats = model
    grey = unmarked_mats(1.0, hull_pattern=em._hex(9))
    grey.append(replace(mats[7], albedo=UG[1]))
    return scene, grey


g_hull = rig.ModelSpaceAngleSprites(lambda tread=0.0: grey_tank(am.warden_hull(tread)), 76, 16,
                                    factor=5, colors=32, post=grey_rim)
g_turret = rig.ModelSpaceAngleSprites(lambda: grey_tank(am.warden_turret()), 76, 32, factor=5,
                                      colors=24, post=grey_rim)


def warden_scene():
    bg = mars_ground(77, craters=8)
    d = ImageDraw.Draw(bg)
    path = r5.TANK_PATH
    road = Image.new("RGBA", bg.size, (0, 0, 0, 0))
    rd = ImageDraw.Draw(road)
    pts = [tuple(float(c) for c in path(tt)) for tt in np.linspace(0, 11.5, 400)]
    rd.line(pts, fill=(176, 110, 70, 120), width=34)       # graded dirt road
    bg.alpha_composite(road.filter(ImageFilter.GaussianBlur(2.5)))
    for tt in np.linspace(0, 11.5, 900):
        x, y = path(tt)
        h = rig.path_heading(path, tt)
        for side in (-1, 1):
            ox, oy = rig.local_to_screen(side * 0.62, 0, h, 76 / 2.3)
            d.ellipse([x + ox - 2, y + oy - 2, x + ox + 2, y + oy + 2], fill=(52, 24, 14, 255))
    shots = Shots()
    for t0 in np.arange(1.6, 11.0, 1.4):
        x, y = path(t0)
        px, py = r5.player_at(t0)
        ta = np.arctan2(py - y, px - x)
        shots.fire(t0, x + np.cos(ta) * 38, y + np.sin(ta) * 38, px, py, 190, 5)
    frames = []
    for t in times(11.5):
        f = bg.copy()
        x, y = path(t)
        h = rig.path_heading(path, t)
        tr = round((t * 2.2) % 1.0, 1) >= 0.5
        paste(f, g_hull.get(h, tread=0.5 if tr else 0.0), x, y, "ground")
        px, py = r5.player_at(t)
        paste(f, g_turret.get(np.arctan2(py - y, px - x)), x, y)
        shots.draw(f, t)
        r5.draw_player(f, t)
        frames.append(f)
    return frames


def warden_sheet(frames):
    hulls = [g_hull.frame(k, tread=0.0) for k in range(16)]
    turs = [g_turret.frame(k) for k in range(32)]
    old = r5._tank_still()
    new = Image.new("RGBA", (76, 76), (0, 0, 0, 0))
    sprite.paste_center(new, g_hull.get(DOWN), 38, 38)
    sprite.paste_center(new, g_turret.get(DOWN), 38, 38)
    panels = [("SOURCE: HULL, 260 PX",
               source_panel(lambda: grey_tank(am.warden_hull()))),
              ("HULL: 16 HEADINGS (1X)", grid(hulls, 8, 1)),
              ("TURRET: 32 HEADINGS (1X)", grid(turs, 16, 1)),
              ("R05-A ASCENDANCY VS R36 UNMARKED (2X)",
               grid([old, new], 2, 2, labels=["R05-A (ACT 5+)", "R36 (ACT 3)"]))]
    notes = ["WARDEN TANK - UNMARKED GREY LIVERY FOR ACT 3 (SAME MODEL AS THE CHOSEN R05-A)",
             "76 PX. GEOMETRY, HEADINGS AND RIG UNCHANGED: HULL 16 HEADINGS WITH 2 TREAD FRAMES,",
             "TURRET 32 HEADINGS TRACKING THE PLAYER. COLOURS ONLY: BLACK HULL -> MID GREY, GOLD",
             "TRIM -> PLAIN LIGHT GREY, WHITE ACCENT -> GUNMETAL, GOLD/RED RIM -> NEUTRAL GREY RIM.",
             "RED SENSOR LIGHTS AND THE HEX PANELLING STAY: THE SAME MACHINE, REPAINTED (THE HINT).",
             "SHOTS ARE PLAIN ENEMY ORBS, NOT GOLD SHELLS. RENDERED IN MODEL SPACE (ROUND 08 RULE)."]
    return r5.build_sheet("WARDEN TANK - UNMARKED (ACT 3, LEVEL 19)", panels, notes, frames[100])


def make_warden():
    frames = warden_scene()
    save_unit("ground", "warden-tank", "a", warden_sheet(frames), frames)


# ===================================================================== 6 Dust Kite (L15, popcorn)

em.ROLE_SCHEMES.update({
    "dust-kite-a": ("rust", "teal", "teal"),       # fast attacker / diver, rams (contact)
    "dust-kite-b": ("rust", "teal", "teal"),
})
OCHRE = (0.80, 0.58, 0.35)
DUST = (196, 122, 80)                               # haze colour of the entry shimmer


def _mottle(base=None):
    """Ochre dust mottling in model space (patchy, not a regular grid)."""
    def f(p, n):
        v = (np.sin(p[:, 0] * 31 + 1.3) * np.sin(p[:, 1] * 27 + 0.4)
             + np.sin((p[:, 0] + p[:, 1]) * 17 + 2.1) * 0.6)
        out = 0.86 + 0.1 * v
        return out * base(p, n) if base else out
    return f


def kite_mats(v, glow=1.0):
    """Rust chitin with an ochre dust bloom on the membrane; the spine glows teal."""
    mats = am.vrell(f"dust-kite-{v}", glow, style=v)
    mid = mats[V_BODY].albedo
    ribs = em._ridges(26, 0.22, axis=0) if v == "a" else em._ridges(14, 0.18, axis=1)
    dark = mats[V_DARK].albedo                      # a deeper rust than the ground it hides on
    mats[V_BODY] = Material(em._mix(em._mix(mid, dark, 0.45), OCHRE, 0.12), metal=0.1,
                            shininess=40, spec=0.45, pattern=_mottle(ribs))
    mats[V_BONE] = replace(mats[V_BONE], albedo=em._mix(mats[V_BONE].albedo, OCHRE, 0.35))
    return mats


KITE_LIT, KITE_DARK = (236, 186, 124), (34, 10, 4)


def kite_rim(sp):
    """Ochre lit edge (top-left) and a dark rust outline elsewhere, so the 24 px glider stays
    readable over rust ground (it is camouflaged only while it fades in)."""
    a = np.array(sp)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    left, up = ~pad[1:-1, :-2], ~pad[:-2, 1:-1]
    right, down = ~pad[1:-1, 2:], ~pad[2:, 1:-1]
    out = a.astype(np.float64)
    for mask, col, k in (((left | up) & op, KITE_LIT, 0.4),
                         ((right | down) & op & ~(left | up), KITE_DARK, 0.7)):
        out[mask, :3] = out[mask, :3] * (1 - k) + np.array(col) * k
    return sprite.quantize(Image.fromarray(out.astype(np.uint8), "RGBA"), 24)


def _flex(flex):
    a = 0.26 * np.sin(flex * np.pi / 2)              # 4-frame membrane flex cycle
    return a, 0.06 * np.cos(flex * np.pi / 2)


def kite_a(flex=0, glow=1.0):
    """Diamond kite: a classic kite membrane (short nose, long tail end) on a bone cross strut,
    a teal-seamed spine from the blunt head to a whip tail with two little vanes."""
    ang, sag = _flex(flex)
    sail = [(0.0, 0.98), (0.46, 0.66), (0.94, 0.32), (0.5, -0.2), (0.0, -0.84)]

    def scene(p):
        q = mirror_x(p)
        w = em._hinge(q, (0.06, 0.0, 0.0), ang)
        w[:, 2] -= sag * w[:, 0] ** 2               # the membrane billows between the struts
        tail_x = 0.07 * np.sin(flex * np.pi / 2)
        d, m = union(
            (sd_plate(w, sail, 0.0, 0.03, 0.01,
                      taper=lambda x, y: np.clip(1.1 - 0.7 * x, 0.35, 1)), V_BODY),
            (sd_capsule(w, (0.04, 0.36, 0.03), (0.92, 0.32, 0.0), 0.045, 0.018), V_BONE),
            (sd_capsule(p, (0, -0.8, 0.02), (0, 0.78, 0.05), 0.08, 0.11), V_SEAM),
            k=0.05,
        )
        return union(
            (d, m),
            (sd_sphere(p, (0, 0.84, 0.07), 0.12), V_DARK),
            (sd_sphere(q, (0.06, 0.92, 0.13), 0.04), V_EYE),
            (sd_capsule(p, (0, -0.8, 0.02), (tail_x, -0.96, 0.0), 0.045, 0.03), V_DARK),
            (sd_capsule(p, (tail_x, -0.96, 0.0), (-tail_x * 0.5, -1.12, 0.0), 0.03, 0.015),
             V_DARK),
            (sd_plate(q - np.array([tail_x, 0, 0]),
                      [(0.0, -0.9), (0.16, -0.98), (0.0, -1.04)], 0.0, 0.025), V_BONE),
            k=0.03,
        )
    return scene, kite_mats("a", glow)


def kite_b(flex=0, glow=1.0):
    """Crescent sail: a broad swept membrane on three bone finger struts a side with a
    scalloped trailing edge, a slim segmented body whose plate gaps glow teal along the spine,
    three teal spine nodes, a short forked tail."""
    ang, sag = _flex(flex)
    sail = [(0.0, 0.42), (0.42, 0.4), (0.8, 0.2), (1.02, -0.14), (0.86, -0.17), (0.74, -0.36),
            (0.58, -0.26), (0.44, -0.46), (0.3, -0.3), (0.13, -0.44), (0.0, -0.3)]
    tips = [(1.0, -0.13), (0.73, -0.34), (0.43, -0.44)]

    def scene(p):
        q = mirror_x(p)
        w = em._hinge(q, (0.08, 0.0, 0.0), ang)
        w[:, 2] -= sag * w[:, 0] ** 2
        items = [(sd_plate(w, sail, 0.0, 0.028, 0.01,
                           taper=lambda x, y: np.clip(1.1 - 0.6 * x, 0.4, 1)), V_BODY),
                 (sd_capsule(w, (0.06, 0.38, 0.03), (0.52, 0.36, 0.03), 0.045, 0.035), V_BONE),
                 (sd_capsule(w, (0.52, 0.36, 0.03), (1.0, -0.12, 0.0), 0.035, 0.016), V_BONE)]
        for tx, ty in tips[1:]:
            items.append((sd_capsule(w, (0.08, 0.22, 0.03), (tx, ty, 0.0), 0.028, 0.012),
                          V_BONE))
        items.append((sd_ellipsoid(p, (0, 0.08, 0.04), (0.13, 0.62, 0.11)), V_SEAM))
        d, m = union(*items, k=0.04)
        fork = 0.05 * np.sin(flex * np.pi / 2)
        return union(
            (d, m),
            (sd_ellipsoid(p, (0, 0.68, 0.06), (0.1, 0.12, 0.08)), V_DARK),
            (sd_sphere(q, (0.05, 0.75, 0.11), 0.03), V_EYE),
            (sd_sphere(p, (0, 0.36, 0.14), 0.05), V_GLOW),
            (sd_sphere(p, (0, 0.08, 0.15), 0.055), V_GLOW),
            (sd_sphere(p, (0, -0.2, 0.13), 0.045), V_GLOW),
            (sd_capsule(q, (0.0, -0.46, 0.02), (0.13 + fork, -0.8, 0.0), 0.035, 0.012), V_DARK),
            k=0.025,
        )
    return scene, kite_mats("b", glow)


DK_N = 24
kite = {v: rig.ModelSpaceAngleSprites(f, DK_N, 16, factor=8, colors=24, post=kite_rim)
        for v, f in (("a", kite_a), ("b", kite_b))}

DK_SWOOP_ROW = 488 - 180                            # swoop starts 180 px above the ship's row
DK_GLIDE, DK_SWOOP, DK_AMP, DK_PERIOD = 130.0, 200.0, 50.0, 1.6
DK_FADE = 0.3


DK_WAY = [(0.0, 240), (1.4, 240), (2.2, 160), (3.4, 160), (4.1, 300), (5.4, 300), (6.0, 220),
          (7.6, 220), (8.4, 320), (10.5, 320)]


def dk_player(t):
    """The ship slides between holds (a player reading the swoops), so some kites hit."""
    for (t0, x0), (t1, x1) in zip(DK_WAY, DK_WAY[1:]):
        if t <= t1:
            return x0 + (x1 - x0) * float(smooth((t - t0) / (t1 - t0))), 488
    return DK_WAY[-1][1], 488


class KiteTrack:
    """One kite: sine glide down the screen, then one swoop across the ship's lane on a
    shallow curve (aimed at the ship's x when it starts, no homing), then straight on."""

    def __init__(self, x0, y0, t0, phase, side):
        self.t0, self.side = t0, side
        self.x0, self.y0, self.phase = x0, y0, phase
        u = (DK_SWOOP_ROW - y0) / DK_GLIDE
        self.ts = t0 + u
        p0 = np.array(self.glide(self.ts))
        ax = dk_player(self.ts)[0]
        aim = np.array((ax, 488.0))
        c = p0 + np.array((side * 70.0, 0.45 * (aim[1] - p0[1])))
        us = np.linspace(0, 1, 240)[:, None]
        pts = (1 - us) ** 2 * p0 + 2 * (1 - us) * us * c + us ** 2 * aim
        seg = np.linalg.norm(np.diff(pts, axis=0), axis=1)
        self.pts, self.cum = pts, np.concatenate([[0.0], np.cumsum(seg)])
        tan = pts[-1] - pts[-2]
        self.exit_dir = tan / np.linalg.norm(tan)

    def glide(self, t):
        u = t - self.t0
        return (self.x0 + DK_AMP * np.sin(TAU * u / DK_PERIOD + self.phase),
                self.y0 + DK_GLIDE * u)

    def __call__(self, t):
        if t < self.ts:
            return self.glide(t)
        s = DK_SWOOP * (t - self.ts)
        if s < self.cum[-1]:
            return (float(np.interp(s, self.cum, self.pts[:, 0])),
                    float(np.interp(s, self.cum, self.pts[:, 1])))
        e = self.pts[-1] + self.exit_dir * (s - self.cum[-1])
        return float(e[0]), float(e[1])


def _kite_tracks():
    tr = []
    for i in range(8):                              # a stream down the left: one path
        tr.append(KiteTrack(150, -14, 0.2 + 0.36 * i, 0.0, 1 if i % 2 == 0 else -1))
    for dx in (0, -24, 24, -48, 48, -72, 72):       # a V-wing of 7
        tr.append(KiteTrack(310 + dx, -14 - 0.75 * abs(dx), 3.2, 1.0, -1 if dx < 0 else 1))
    rng = np.random.default_rng(36)
    for k in range(8):                              # a loose swarm out of the haze
        tr.append(KiteTrack(rng.uniform(90, 390), -14 - rng.uniform(0, 50), 6.2 + 0.2 * k,
                            rng.uniform(0, TAU), 1 if k % 2 else -1))
    return tr


DK_SPARE = {1, 4, 6, 8, 9, 14, 18, 20}                     # these are not shot down: they swoop/ram


def kite_events(tracks, dur):
    """Per kite: (time, kind) of its end: 'shot' in the fire column, 'ram' on the ship."""
    ends = {}
    for t in times(dur):
        px, py = dk_player(t)
        for i, tr in enumerate(tracks):
            if i in ends or t < tr.t0:
                continue
            x, y = tr(t)
            if i not in DK_SPARE and abs(x - px) < 12 and 30 < y < py - 46:
                ends[i] = (t, "shot")
            elif abs(x - px) < 15 and abs(y - py) < 15:
                ends[i] = (t, "ram")
    return ends


def kite_fade(sp, u):
    """Entry shimmer: blended into the dust, fading to full contrast over 0.3 s."""
    if u >= 1:
        return sp
    return e6.tint(sp, DUST, 0.75 * (1 - u), alpha=0.25 + 0.75 * u)


def kite_death(img, x, y, s, seed=0, vel=(0.0, 0.0)):
    """Tiny death (0 <= s <= 1 over 0.5 s): a ragged puff of rust dust drifting with the
    kite's momentum, two membrane scraps, a short teal spark. No rings, no round flash."""
    if not 0 <= s <= 1:
        return
    rng = np.random.default_rng(seed)
    T = s * 0.5
    drift = np.array(vel) * 0.25 * (1 - np.exp(-T * 4)) / 4 * 4
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for k in range(4):                              # soft dust cloud: offset blobs
        ox, oy = rng.normal(0, 2.5, 2)
        r = 3 + 6 * s ** 0.6 + rng.uniform(0, 2)
        cx, cy = x + ox + drift[0] + ox * s * 2, y + oy + drift[1] + oy * s * 2
        col = (int(150 + 40 * rng.random()), int(80 + 30 * rng.random()), 52)
        d.ellipse([cx - r, cy - r * 0.8, cx + r, cy + r * 0.8],
                  fill=col + (int(120 * (1 - s) ** 1.3),))
    lay = lay.filter(ImageFilter.GaussianBlur(1.6))
    d = ImageDraw.Draw(lay)
    for k in range(11):                             # dust motes
        a = rng.uniform(0, TAU)
        sp_ = rng.uniform(25, 70)
        dist = sp_ * (1 - np.exp(-T * 5)) / 5
        mx = x + np.cos(a) * dist + drift[0]
        my = y + np.sin(a) * dist + drift[1] + 6 * T
        r = rng.choice([0.6, 1.0, 1.4])
        col = (214, 160, 100) if k % 3 == 0 else (176, 84, 48)
        d.rectangle([mx - r, my - r, mx + r, my + r], fill=col + (int(230 * (1 - s) ** 1.1),))
    for k in range(2):                              # membrane scraps tumbling off
        a = rng.uniform(0, TAU)
        dist = 30 * (1 - np.exp(-T * 4)) / 4 * 4
        cx, cy = x + np.cos(a) * dist + drift[0], y + np.sin(a) * dist + drift[1]
        r, rot = 2.5, a + T * 14
        pts = [(cx + np.cos(rot + j * 2.2) * r, cy + np.sin(rot + j * 2.2) * r * 0.6)
               for j in range(3)]
        d.polygon(pts, fill=(120, 44, 26, int(255 * (1 - s))))
    img.alpha_composite(lay)
    if s < 0.35:                                    # the teal spark: short streaks
        k = 1 - s / 0.35
        raster.add_light(img, x, y, 7, TEAL, 0.7 * k)
        d = ImageDraw.Draw(img)
        for j in range(3):
            a = rng.uniform(0, TAU)
            r0, r1 = 2 + 10 * s, 5 + 16 * s
            d.line([(x + np.cos(a) * r0, y + np.sin(a) * r0),
                    (x + np.cos(a) * r1, y + np.sin(a) * r1)],
                   fill=TEAL + (int(255 * k),), width=1)
        if s < 0.12:
            d.point([(x, y), (x + 1, y), (x, y + 1)], fill=(220, 255, 240, 255))


def dk_haze(f, haze, t):
    a = np.array(f).astype(np.float64)
    off = int(t * 24) % FH
    hz = haze[off:off + FH]
    a[..., :3] = a[..., :3] * (1 - 0.16 * hz[..., None]) + np.array((230, 150, 100)) * \
        0.16 * hz[..., None]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


DK_DUR = 10.5


def kite_scene(v):
    bg = mars_ground(151, craters=7, dunes=0.04)
    haze = raster.fbm(FW, FH * 2, 90, 15, octaves=3, period=False)
    tracks = _kite_tracks()
    ends = kite_events(tracks, DK_DUR)
    frames = []
    for t in times(DK_DUR):
        f = dk_haze(bg.copy(), haze, t)
        px, py = dk_player(t)
        rammed = False
        for i, tr in enumerate(tracks):
            if t < tr.t0:
                continue
            end = ends.get(i)
            if end and t >= end[0]:
                s = (t - end[0]) / 0.5
                x, y = tr(end[0])
                x1, y1 = tr(end[0] + 0.05)
                kite_death(f, x, y, s, seed=i, vel=((x1 - x) / 0.05, (y1 - y) / 0.05))
                rammed |= end[1] == "ram" and t - end[0] < 0.2
                continue
            x, y = tr(t)
            if not (-30 < x < FW + 30 and -30 < y < FH + 30):
                continue
            h = rig.path_heading(tr, t)
            sp = kite[v].get(h, flex=int(t * 7 + i) % 4)
            u = (t - tr.t0 - 14 / DK_GLIDE) / DK_FADE if y > -12 else 0.0
            hit = end is not None and end[1] == "shot" and end[0] - t < 0.1
            e3.place(f, kite_fade(flash(sp, hit), max(0.0, u)), x, y, "air")
        e3.place(f, flash(e3.player_sprite(), rammed and int(t * 20) % 2 == 0), px, py, "air")
        for k in range(4):
            yb = py - 40 - ((t * 420 + k * 105) % 420)
            if yb > -10:
                e3.bolt(f, px - 10, yb)
                e3.bolt(f, px + 10, yb)
        frames.append(f)
    return frames, ends


def _ground_cell(bg, x, y, n=40):
    return bg.crop((x - n // 2, y - n // 2, x + n // 2, y + n // 2)).copy()


def kite_sheet(v, frames, ends):
    heads = [kite[v].frame(k, flex=0) for k in range(16)]
    flexes = [kite[v].get(DOWN, flex=k) for k in range(4)]
    bg = mars_ground(151, craters=7, dunes=0.04)
    fades = []
    for k, u in enumerate((0.0, 0.33, 0.67, 1.0)):
        c = _ground_cell(bg, 120 + 30 * k, 200)
        e3.place(c, kite_fade(kite[v].get(DOWN, flex=k), u), 20, 20, "air")
        fades.append(c)
    deaths = []
    for k, s in enumerate((0.0, 0.1, 0.25, 0.45, 0.7, 0.9)):
        c = _ground_cell(bg, 300, 300, 48)
        if s == 0.0:
            e3.place(c, kite[v].get(DOWN, flex=0), 24, 24, "air")
        else:
            kite_death(c, 24, 24, s, seed=3, vel=(30.0, 130.0))
        deaths.append(c)
    trio = []
    for sp in (e6.skitter(0.0), kite[v].get(DOWN, flex=0), e3.player_sprite()):
        c = Image.new("RGBA", (48, 48), (0, 0, 0, 0))
        sprite.paste_center(c, sp, 24, 24)
        trio.append(c)
    panels = [("SOURCE, 260 PX", source_panel(lambda: (kite_a if v == "a" else kite_b)())),
              ("16 HEADINGS (2X)", grid(heads, 8, 2)),
              ("MEMBRANE FLEX, 4 FRAMES (4X)", grid(flexes, 4, 4)),
              ("ENTRY SHIMMER 0 / 0.1 / 0.2 / 0.3 S (2X)", grid(fades, 4, 2)),
              ("DEATH: RUST DUST PUFF + TEAL SPARK, 0.5 S (2X)", grid(deaths, 6, 2)),
              ("NEXT TO A SKITTER AND THE PLAYER (2X)",
               grid(trio, 3, 2, labels=["SKITTER", "DUST KITE", "PLAYER"]))]
    if v == "a":
        notes = ["DUST KITE A - DIAMOND KITE (RUST CHITIN DUSTED OCHRE, TEAL GLOW: CONTACT/RAM)",
                 "24 PX, 1 HP. A RHOMBUS MEMBRANE ON A BONE CROSS STRUT, A TEAL-SEAMED SPINE FROM",
                 "THE BLUNT HEAD TO A WHIP TAIL WITH TWO VANES: READS AS A KITE AT A GLANCE.",
                 "SINE GLIDE (50 PX, 1.6 S) DOWN AT 130 PX/S; 180 PX ABOVE THE SHIP'S ROW IT SWOOPS",
                 "ACROSS THE SHIP'S LANE AT 200 PX/S (AIMED ONCE, NO HOMING), SIDES ALTERNATING.",
                 "SHOWN: A STREAM, A V-WING OF 7, A LOOSE SWARM; FIRST 0.3 S BLENDED INTO THE DUST."]
    else:
        notes = ["DUST KITE B - CRESCENT SAIL (RUST CHITIN DUSTED OCHRE, TEAL GLOW: CONTACT/RAM)",
                 "24 PX, 1 HP. A BROAD SWEPT MEMBRANE ON THREE BONE FINGERS A SIDE WITH A SCALLOPED",
                 "TRAILING EDGE, A SLIM BODY WHOSE PLATE GAPS GLOW TEAL, THREE TEAL SPINE NODES,",
                 "A SHORT FORKED TAIL. WIDER AND FLATTER THAN A: MORE BAT-LIKE, LESS 'TOY KITE'.",
                 "SAME GLIDE AND SWOOP: SINE DOWN AT 130 PX/S, ONE SWOOP ACROSS THE LANE AT 200 PX/S.",
                 "SHOWN: A STREAM, A V-WING OF 7, A LOOSE SWARM; FIRST 0.3 S BLENDED INTO THE DUST."]
    return r5.build_sheet(f"DUST KITE {v.upper()} - MARS POPCORN, GLIDES AND RAMS", panels, notes,
                          frames[int(3.9 * FPS)])


def make_kite():
    for v in ("a", "b"):
        frames, ends = kite_scene(v)
        print("dust-kite", v, "ends:", {i: (round(t, 2), k) for i, (t, k) in sorted(ends.items())})
        save_unit("air", "dust-kite", v, kite_sheet(v, frames, ends), frames)


# --------------------------------------------------------------------------- main

UNITS = {"burrower": make_burrower, "tendril-anchor": make_tendril,
         "choir-herald": make_herald, "ghost-drone": make_ghost,
         "ghost-drone-a": lambda: make_ghost(("a",)), "warden-tank": make_warden,
         "dust-kite": make_kite}


def main(args):
    for name, fn in UNITS.items():
        if name in args or (not args and name != "ghost-drone-a"):
            fn()


if __name__ == "__main__":
    main(sys.argv[1:])
