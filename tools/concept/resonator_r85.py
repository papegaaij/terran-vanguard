#!/usr/bin/env python3
"""Concept round 85 - the Choir Resonator, Act 7's one new weapon, a/b proposals (step RES, M11 design phase).

The weapon as its draft document describes it (design/player/weapons/choir-resonator/README.md): a front gun made
from a Gate Warden's eye that sings a **wave front**, an arc of a ring that leaves the nose 32 px wide, widens as it
rises (L1 to 160 px at its 300 px reach, L5 to 240 px at 380 px) and passes through the hulls it meets, each one
taking the wave's damage and leaving it a step weaker (x 0.75) until its count of hulls (L1 3, L5 5) is spent.

Each variant gets a PNG sheet (the mount's source render, the mount on the production Stormhawk at 1x and 4x, one
wave at several distances for L1 and L5, the wave through a column of five hulls stepping down, the readability
check over Level 44's brightest and Level 43's darkest window (one Vrell palette, Q10 b) beside the Vrell bullets and
the player's other shots, key frames, a frame from the play field at 1x) and a GIF of the 480x540 play field over
Level 44's event plane: Rift Skaters (the chosen round-84 a "manta") in a column and a stream at L1, then a wide
formation and a column at L5, their bursts of three violet outlined orbs, the pings where the wave passes a hull.

The art rules (art-direction rule 2): the player's shots are blue / white / cyan and never share a hue with an
enemy bullet; the wave reads apart from the Vrell's outlined violet and teal orbs by its shape (a wide thin arc,
never an orb), its hue (ice blue and white, no green, no violet) and a thin dark rim either side of the bright
edge (the act's readability lesson over the event plane's light). Every glow is a smooth float light field added
once at 1x, fading to 0 inside its crop (the 2026-10-30 dither audit); no ordered dither.

  a "the bell": the mount a Gate Warden's eye (a faceted crystal, retuned from crimson to ice-white) held in a
    gunmetal collar with two claws on the Stormhawk's nose; the wave one bright crescent, thickest in the middle,
    with two fainter ripples trailing it (a struck bell's rings).
  b "the chord": the mount a tuning fork, two slim prongs either side of the nose with the eye's crystal set
    between them; the wave three thin parallel arcs (a chord), the middle one brightest, with a shimmer of nodes
    running along them.

Outputs: design/player/weapons/choir-resonator/concept/choir-resonator-r85-{a,b}.{png,gif}
Run: python3 tools/concept/resonator_r85.py [a|b]   (no args = both)
"""
import sys
import time
from pathlib import Path

import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import act7_r84 as a84  # noqa: E402  (one_palette: Q10 b's one Vrell palette)
import enemies_r03 as e3  # noqa: E402
import enemies_r05 as r5  # noqa: E402
import enemies_r60 as e60  # noqa: E402
import enemies_r72 as e72  # noqa: E402
import enemies_r84 as u84  # noqa: E402  (the L43/L44 backdrops, the Rift Skater, the Vrell bullets)
import gear_r36 as g36  # noqa: E402  (the production Stormhawk)
import vfx_r08 as v8  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import hx  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid,  # noqa: E402
                        sd_sphere, union)

FW, FH, FPS, TAU = r5.FW, r5.FH, r5.FPS, 2 * np.pi
DOWN = np.pi / 2
OUT = ROOT / "design" / "player" / "weapons" / "choir-resonator" / "concept"
SUB = "CONCEPT ROUND 85 - 960X540"
times, smooth, light, caption, keyframes, grid = e60.times, e60.smooth, e60.light, e60.caption, e60.keyframes, \
    e60.grid
VIOLET, TEAL = u84.VIOLET, u84.TEAL

# The document's numbers (L1 and L5; the scene flies these two).
LEVELS = {1: dict(rate=2.5, dmg=30.0, cap=3, speed=520.0, reach=300.0, width=160.0, w=2.0),
          5: dict(rate=3.0, dmg=53.5, cap=5, speed=520.0, reach=380.0, width=240.0, w=2.6)}
NOSE_W = 32.0        # the wave's width as it leaves the nose
FALL = 0.75          # each hull it passes leaves it this much
SK_HP = 64.0         # a Rift Skater (32, the beam era's x 2)
SK_R = 15.0          # its hit radius for the wave

STYLE = {"a": dict(core=(255, 255, 255), glow=(150, 225, 255), fringe=(40, 140, 255), crystal=(0.85, 0.95, 1.0)),
         "b": dict(core=(245, 250, 255), glow=(170, 205, 255), fringe=(30, 90, 255), crystal=(0.8, 0.9, 1.0))}


# =========================================================================== the wave

def geom(lv):
    """The wave is an arc of a ring round a point r0 behind the nose: half-angle h, so that it is NOSE_W wide at
    the nose and lv['width'] wide at its reach."""
    sin_h = (lv["width"] - NOSE_W) / (2 * lv["reach"])
    return float(np.arcsin(sin_h)), NOSE_W / 2 / sin_h


def wave_life(d, lv):
    """Brightness over the wave's flight: up in its first 12 px, out over the last 20 % of its reach."""
    return float(smooth(d / 12.0 + 0.25) * (1 - smooth((d / lv["reach"] - 0.8) / 0.2)))


def draw_wave(img, nx, ny, d, lv, v, bright=1.0, t=0.0):
    """One wave front ``d`` px out from the nose (nx, ny), at ``bright`` (its steps through hulls), additive."""
    k = bright * wave_life(d, lv)
    if k <= 0.003:
        return img
    h, r0 = geom(lv)
    cx, cy, R = nx, ny + r0, r0 + d
    w = lv["w"]
    pad = int(10 * w + 14)
    half = R * np.sin(h)
    box = (max(0, int(cx - half - pad)), max(0, int(cy - R - pad)), min(img.width, int(cx + half + pad) + 1),
           min(img.height, int(cy - (R - 22) * np.cos(h) + pad) + 1))
    if box[2] <= box[0] or box[3] <= box[1]:
        return img
    a = np.array(img.crop(box)).astype(np.float64)
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]] + 0.5
    dx, dy = xx - cx, yy - cy
    rho = np.hypot(dx, dy)
    u = np.abs(np.arctan2(dx, -dy)) / h
    tap = (1 - smooth((u - 0.45) / 0.55)) * (dy < 0)
    s = rho - R
    st = STYLE[v]
    glow, fringe = np.array(st["glow"], float), np.array(st["fringe"], float)
    lf = np.zeros(a.shape[:2] + (3,))
    if v == "a":
        wv = w * (0.55 + 0.45 * np.cos(np.pi / 2 * np.clip(u, 0, 1)))          # a crescent: thickest in the middle
        rim = np.exp(-((np.abs(s) - 2.4 * wv) / (0.9 * wv)) ** 2)
        lf += np.exp(-(s / (0.55 * wv)) ** 2)[..., None] * 255 * 1.35
        lf += np.exp(-(s / (1.4 * wv)) ** 2)[..., None] * glow * 0.85
        lf += np.exp(-(s / (3.4 * wv)) ** 2)[..., None] * fringe * 0.3
        for off, kk in ((8.0, 0.42), (15.0, 0.2)):                               # the bell's trailing rings
            lf += np.exp(-((s + off) / (0.75 * w)) ** 2)[..., None] * glow * kk
    else:
        rim = np.zeros_like(s)
        node = 0.72 + 0.28 * np.cos(np.arctan2(dx, -dy) * 26 - t * 24)        # the chord's shimmer
        for off, kk in ((0.0, 1.0), (5.5, 0.6), (-5.5, 0.6)):                    # three thin arcs
            ss = s + off
            rim = np.maximum(rim, np.exp(-((np.abs(ss) - 1.7 * w) / (0.6 * w)) ** 2) * kk)
            lf += np.exp(-(ss / (0.42 * w)) ** 2)[..., None] * 255 * 1.2 * kk * node[..., None]
            lf += np.exp(-(ss / (1.1 * w)) ** 2)[..., None] * glow * 0.7 * kk
        lf += np.exp(-(s / (4.2 * w)) ** 2)[..., None] * fringe * 0.32
    dark = (rim * tap * k * 0.42)[..., None]
    a[..., :3] = a[..., :3] * (1 - dark) + np.array((10, 8, 30)) * dark
    a[..., :3] = np.clip(a[..., :3] + lf * (tap * k)[..., None], 0, 255)
    img.paste(Image.fromarray(a.astype(np.uint8), img.mode), box)
    return img


def ping(img, x, y, s, v, bright=1.0):
    """Where the wave passes a hull: a thin ring rings out from it (0 <= s < 1, 0.25 s)."""
    if not 0 <= s < 1:
        return img
    k = bright * (1 - s) ** 1.5
    img = e72.ring_light(img, x, y, 6 + 16 * s, STYLE[v]["glow"], 0.9 * k, 1.3)
    return light(img, x, y, 5, (255, 255, 255), 0.7 * k * (1 - s))


# =========================================================================== the mount

M_GUN, M_DARK, M_CRYS, M_GLOW = range(4)


def mount_model(v):
    """a: the eye (a faceted crystal sphere) in a collar with two claws; b: a tuning fork round the nose, the
    crystal set between its prongs. Model units: 1 = 8 px at 1x, +y forward (the nose)."""
    hull = v8.HULL_C
    cr = np.array(STYLE[v]["crystal"])
    mats = [v8.m_metal(hull[1]), v8.m_metal(hull[4]),
            Material(hx("bcd8f0"), metal=0.45, shininess=260, spec=2.4, emission=tuple(cr * 0.45),
                     pattern=lambda p, n: 0.75 + 0.25 * np.sign(np.sin(p[:, 0] * 31) * np.sin(p[:, 1] * 27))),
            Material((0.05, 0.06, 0.08), emission=tuple(cr * 1.9))]

    def scene(p):
        q = mirror_x(p)
        if v == "a":
            return union(
                (sd_box(p, (0, -0.55, 0.06), (0.42, 0.16, 0.16), 0.08), M_GUN),               # the collar
                (sd_box(p, (0, -0.55, 0.2), (0.3, 0.05, 0.04), 0.02), M_DARK),
                (sd_capsule(q, (0.3, -0.45, 0.12), (0.17, 0.42, 0.14), 0.07, 0.045), M_GUN),  # the claws
                (sd_sphere(p, (0, 0.44, 0.14), 0.27), M_CRYS),                                # the eye
                (sd_sphere(p, (0, 0.44, 0.3), 0.1), M_GLOW),
                k=0.03)
        return union(
            (sd_box(p, (0, -0.7, 0.05), (0.5, 0.14, 0.14), 0.07), M_GUN),                     # the yoke
            (sd_box(q, (0.36, 0.1, 0.08), (0.075, 0.8, 0.09), 0.04), M_GUN),                  # the prongs
            (sd_box(q, (0.29, 0.25, 0.14), (0.015, 0.62, 0.03)), M_GLOW),                     # their inner faces
            (sd_ellipsoid(p, (0, -0.3, 0.16), (0.15, 0.22, 0.12)), M_CRYS),                   # the crystal
            (sd_cylinder_z(p, (0, -0.7, 0.2), 0.08, 0.03), M_DARK),
            k=0.03)
    return scene, mats


MOUNT_PX = (16, 24)
_MOUNT = {}


def mount_sprite(v):
    if v not in _MOUNT:
        scene, mats = mount_model(v)
        hi = sdf.render(scene, mats, (MOUNT_PX[0] * 8, MOUNT_PX[1] * 8), MOUNT_PX[0] / 8.0)
        _MOUNT[v] = sprite.make_sprite(hi, 8, 24)
    return _MOUNT[v]


def mount_source(v, disp=200):
    scene, mats = mount_model(v)
    hi = sdf.render(scene, mats, (disp, int(disp * MOUNT_PX[1] / MOUNT_PX[0])), MOUNT_PX[0] / 8.0)
    cell = e3.checker(disp, int(disp * MOUNT_PX[1] / MOUNT_PX[0]))
    cell.alpha_composite(sprite.to_image(hi))
    return cell


MOUNT_AT = (24, 7)        # the mount's centre on the 48x48 ship sprite (the nose tip is at 24, 3)


def ship(img, x, y, v, t=0.0, fire=0.0):
    """The production Stormhawk with the mount on its nose; ``fire`` (0..1) the muzzle's bloom."""
    ox, oy = g36.draw_ship(img, x, y, t)
    sprite.paste_center(img, mount_sprite(v), ox + MOUNT_AT[0], oy + MOUNT_AT[1])
    nx, ny = ox + 24, oy + (3 if v == "a" else 6)
    img = light(img, nx, ny, 3.5, STYLE[v]["glow"], 0.35 + 0.9 * fire)
    return light(img, nx, ny, 1.6, (255, 255, 255), 0.5 * fire)


def nose_of(px, py):
    return px, py - 22.0


# =========================================================================== the scene

DUR = 10.0
SWITCH = 5.0
PLANE_SPEED = 10.0     # the event plane slid slowly under the scene (the deep layer at altitude; keeps the GIF small)


def stack(t0, x, n, vy=42.0, gap=46.0):
    return [dict(t0=t0, x=x, y=-30 - gap * i, vx=0.0, vy=vy) for i in range(n)]


def stream(t0, x, y, vx, vy, n, dt):
    return [dict(t0=t0 + dt * i, x=x, y=y, vx=vx, vy=vy) for i in range(n)]


def formation(t0, y, xs, vy=34.0):
    return [dict(t0=t0, x=x, y=y - 18 * abs(x - 240) / 100, vx=0.0, vy=vy) for x in xs]


UNITS = (stack(0.2, 236, 4) + stream(1.6, -30, 110, 74, 30, 5, 0.5)
         + formation(SWITCH - 0.4, -30, (90, 150, 210, 270, 330, 390)) + stack(SWITCH + 0.6, 300, 5, vy=40))


def ship_x(t):
    if t < SWITCH:
        return 236 if t < 3.2 else 236 - 40 * smooth((t - 3.2) / 0.8)
    return 196 + 104 * smooth((t - SWITCH) / 1.4) - 40 * smooth((t - 8.2) / 1.2)


class Sim:
    def __init__(self, v):
        self.v = v
        self.units = [dict(u, hp=SK_HP, dead=None, hit=-9.0, fired=False) for u in UNITS]
        self.waves, self.pings, self.next = [], [], 0.25
        self.bullets = u84.VShots(VIOLET)

    def pos(self, u, t):
        dt = t - u["t0"]
        return u["x"] + u["vx"] * dt, u["y"] + u["vy"] * dt

    def step(self, t, px, py):
        lvn = 1 if t < SWITCH else 5
        lv = LEVELS[lvn]
        fired = False
        if t >= self.next:
            nx, ny = nose_of(px, py)
            self.waves.append(dict(t0=t, nx=nx, ny=ny, lv=lv, n=0, bright=1.0, dmg=lv["dmg"], seen=set(), prev=0.0))
            self.next += 1.0 / lv["rate"]
            fired = True
        live = {}
        for i, u in enumerate(self.units):
            if t >= u["t0"] and u["dead"] is None:
                x, y = self.pos(u, t)
                if -40 < x < FW + 40 and y < FH + 40:
                    live[i] = (x, y)
                    if not u["fired"] and t - u["t0"] > 2.2 and 40 < y < 300 and 20 < x < FW - 20:
                        u["fired"] = True
                        ang = np.arctan2(py - y, px - x)
                        for k in range(3):
                            self.bullets.fire(t + 0.12 * k, x, y + 10, ang, 170)
        for wv in self.waves:
            lv = wv["lv"]
            d = lv["speed"] * (t - wv["t0"])
            if wv["n"] >= lv["cap"] or d > lv["reach"]:
                wv["done"] = True
                continue
            h, r0 = geom(lv)
            cx, cy = wv["nx"], wv["ny"] + r0
            hits = []
            for i, (x, y) in live.items():
                if i in wv["seen"]:
                    continue
                rho = np.hypot(x - cx, y - cy)
                phi = abs(np.arctan2(x - cx, -(y - cy)))
                if r0 + wv["prev"] - SK_R <= rho <= r0 + d + SK_R and phi <= h + SK_R / max(rho, 1):
                    hits.append((rho, i))
            for _, i in sorted(hits):
                if wv["n"] >= lv["cap"]:
                    break
                u = self.units[i]
                wv["seen"].add(i)
                u["hp"] -= wv["dmg"] * FALL ** wv["n"]
                u["hit"] = t
                self.pings.append((t, live[i][0], live[i][1], wv["bright"]))
                wv["n"] += 1
                wv["bright"] = FALL ** wv["n"]
                if u["hp"] <= 0:
                    u["dead"], u["at"] = t, live[i]
            wv["prev"] = d
        return live, fired, lvn


def scene(v):
    plane = u84.EventPlane(4401, DUR, speed=PLANE_SPEED)
    sim = Sim(v)
    S = u84.skaters["a"]
    frames, last_fire, py = [], -9.0, 470.0
    kills = 0
    for t in times(DUR):
        f = a84.one_palette(plane.frame(t))
        px = ship_x(t)
        live, fired, lvn = sim.step(t, px, py)
        if fired:
            last_fire = t
        for i, (x, y) in live.items():
            u = sim.units[i]
            hd = np.arctan2(u["vy"], u["vx"]) if (u["vx"] or u["vy"]) else DOWN
            sp = S.get(hd)
            sprite.paste_center(f, e60.flash(sp, t - u["hit"] < 0.08), x, y)
        for u in sim.units:
            if u["dead"] is not None and 0 <= t - u["dead"] < 0.8:
                f = e72.boom(f, u["at"][0], u["at"][1], (t - u["dead"]) / 0.6, "small")
                f = u84.shard_flakes(f, u["at"][0], u["at"][1], (t - u["dead"]) / 0.8, seed=u["t0"])
        for wv in sim.waves:
            if wv.get("done") and wv["lv"]["speed"] * (t - wv["t0"]) > wv["lv"]["reach"] + 1:
                continue
            d = wv["lv"]["speed"] * (t - wv["t0"])
            if d <= wv["lv"]["reach"] and wv["n"] <= wv["lv"]["cap"]:
                k = wv["bright"] if wv["n"] < wv["lv"]["cap"] else wv["bright"] * max(0.0, 1 - (t - wv.get("spent", t)) / 0.1)
                if wv["n"] >= wv["lv"]["cap"]:
                    wv.setdefault("spent", t)
                f = draw_wave(f, wv["nx"], wv["ny"], d, wv["lv"], v, k, t)
        for t0, x, y, b in sim.pings:
            f = ping(f, x, y, (t - t0) / 0.25, v, b)
        sim.bullets.draw(f, t)
        f = ship(f, px, py, v, t, max(0.0, 1 - (t - last_fire) / 0.12))
        lv = LEVELS[lvn]
        caption(f, f"CHOIR RESONATOR L{lvn}: {lv['rate']:g} WAVES/S, {lv['dmg']:g} DAMAGE, THROUGH {lv['cap']} HULLS "
                   f"(X0.75 EACH)")
        frames.append(f)
    kills = sum(u["dead"] is not None for u in sim.units)
    print(f"resonator {v}: waves {len(sim.waves)}, pings {len(sim.pings)}, kills {kills}/{len(sim.units)}", flush=True)
    return frames, plane


# =========================================================================== the sheet

PLATE = (12, 12, 22, 255)


def mount_panel(v):
    cells = []
    for scale in (1, 4):
        c = Image.new("RGBA", (48 * scale + 16, 48 * scale + 16 + 4 * scale), PLATE)
        s = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        ship(s, 32, 32, v, 0.0, 0.0)
        c.alpha_composite(s.resize((64 * scale, 64 * scale), Image.NEAREST), (8 - 8 * scale, 8 - 4 * scale))
        cells.append(c)
    bare = Image.new("RGBA", (48 * 4 + 16, 48 * 4 + 32), PLATE)
    s = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    g36.draw_ship(s, 32, 32, 0.0)
    bare.alpha_composite(s.resize((256, 256), Image.NEAREST), (-24, -16))
    src = mount_source(v, 150)
    out = Image.new("RGBA", (cells[0].width + cells[1].width + bare.width + src.width + 48, max(cells[1].height,
                                                                                              src.height) + 14),
                    raster.SHEET_BG)
    x = 0
    for im, lab in ((cells[0], "1X"), (cells[1], "4X, ARMED"), (bare, "4X, THE BARE NOSE"), (src, "SOURCE")):
        out.alpha_composite(im, (x, 14))
        raster.draw_text(out, x + 2, 0, lab, raster.LABEL_DIM)
        x += im.width + 16
    return out


def reach_panel(v):
    img = Image.new("RGBA", (500, 420), PLATE)
    for (lvn, nx, ds) in ((1, 125, (30, 110, 190, 270)), (5, 375, (30, 140, 250, 350))):
        lv = LEVELS[lvn]
        for d in ds:
            img = draw_wave(img, nx, 408, d, lv, v, 1.0, 0.3)
        raster.draw_text(img, nx - 40, 4, f"L{lvn}: {lv['width']:g} PX AT {lv['reach']:g}", raster.LABEL_DIM)
    return img


def pierce_panel(v):
    lv = LEVELS[5]
    img = Image.new("RGBA", (260, 360), PLATE)
    nx, ny = 130, 350
    ys = [300, 245, 190, 135, 80]
    S = u84.skaters["a"]
    for y in ys:
        sprite.paste_center(img, S.get(DOWN), nx, y)
    for k, y in enumerate(ys):
        img = ping(img, nx, y, 0.15, v, FALL ** k)
        if k < 4:                                   # after the fifth hull (L5's count) the wave is spent
            img = draw_wave(img, nx, ny, (ny - y) + 12, lv, v, FALL ** (k + 1), 0.3 + k)
        raster.draw_text(img, 4, y - 26, f"{FALL ** (k + 1):.2f}" if k < 4 else "SPENT", raster.LABEL_DIM)
    return img


def readability_panel(v):
    ev = a84.one_palette(u84.EventPlane(4401, 1.0).frame(0.0))
    gate = a84.one_palette(u84.gate_bg(4301))
    out = []
    for bg, mode in ((ev, "bright"), (gate, "dark")):
        g = e72.window(bg, 150, 150, mode).copy()
        g = draw_wave(g, 75, 150, 70, LEVELS[5], v, 1.0, 0.4)
        u84.vorb(g, 30, 30, 4, VIOLET)
        u84.vorb(g, 52, 18, 4, TEAL)
        u84.vorb(g, 120, 26, 4, (236, 220, 255))
        e3.bolt(g, 128, 110)
        g = e72.draw_strike(g, (16, 128), (40, 92), "tesla", 3, 0.8)
        ship(g, 75, 128, v, 0.0, 0.6)
        out.append(g.resize((300, 300), Image.NEAREST))
    rd = Image.new("RGBA", (612, 300), raster.SHEET_BG)
    rd.alpha_composite(out[0], (0, 0))
    rd.alpha_composite(out[1], (312, 0))
    return rd


def make_sheet(v, frames):
    kf = [1.3, 2.35, SWITCH + 1.5, SWITCH + 2.6]
    panels = [("THE MOUNT ON THE STORMHAWK'S NOSE", mount_panel(v)),
              ("ONE WAVE AT FOUR DISTANCES, L1 / L5 (1X)", reach_panel(v)),
              ("THROUGH A COLUMN OF FIVE (L5, 1X)", pierce_panel(v)),
              ("READABILITY: L44'S BRIGHTEST / L43'S DARKEST WINDOW, VRELL ORBS, A BOLT, THE TESLA ARC (2X)",
               readability_panel(v)),
              ("KEY FRAMES (0.5X)", keyframes(frames, kf, ["L1 COLUMN", "L1 STREAM", "L5 FORMATION", "L5 COLUMN"]))]
    if v == "a":
        notes = ["CHOIR RESONATOR A - THE BELL: A WARDEN'S EYE IN A COLLAR, ONE BRIGHT CRESCENT WITH TWO TRAILING RINGS",
                 "THE MOUNT: THE GATE WARDEN'S EYE (A FACETED CRYSTAL, RETUNED FROM CRIMSON TO ICE-WHITE) HELD IN A GUNMETAL",
                 "COLLAR WITH TWO CLAWS AHEAD OF THE NOSE. THE WAVE: AN ARC OF A RING, 32 PX AT THE NOSE, WIDENING TO 160 (L1)",
                 "OR 240 PX (L5) AT ITS REACH; ONE WHITE CRESCENT THICKEST IN THE MIDDLE, ICE-BLUE GLOW, A THIN DARK RIM EITHER",
                 "SIDE, TWO FAINT RINGS TRAILING IT. EVERY HULL IT PASSES RINGS (A THIN RING) AND THE WAVE STEPS DOWN X0.75;",
                 "AT ITS COUNT OF HULLS IT IS SPENT. BLUE / WHITE ONLY (THE PLAYER'S SHOT HUES), NO VIOLET, NO TEAL."]
    else:
        notes = ["CHOIR RESONATOR B - THE CHORD: A TUNING FORK ON THE NOSE, THREE THIN PARALLEL ARCS",
                 "THE MOUNT: TWO SLIM PRONGS EITHER SIDE OF THE NOSE, THEIR INNER FACES LIT, THE EYE'S CRYSTAL SET BETWEEN",
                 "THEM. THE WAVE: THE SAME ARC OF A RING DRAWN AS THREE THIN ARCS 5.5 PX APART (THE MIDDLE BRIGHTEST), WITH",
                 "NODES OF LIGHT RUNNING ALONG THEM (THE CHORD'S SHIMMER) AND A WIDE DEEP-BLUE FRINGE. LIGHTER AND FINER",
                 "THAN A; EACH HULL STEPS IT DOWN X0.75 THE SAME WAY. SAME SCENE AS A."]
    return r5.build_sheet(f"CHOIR RESONATOR {v.upper()} - THE WAVE FRONT (ACT 7 FRONT GUN, OVER LEVEL 44)", panels,
                          notes, frames[int((SWITCH + 1.5) * FPS)], 1300)


def main(args):
    r5.SUB = SUB
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    OUT.mkdir(parents=True, exist_ok=True)
    for v in variants:
        t0 = time.time()
        frames, _ = scene(v)
        r5.SUB = SUB
        img = make_sheet(v, frames)
        png = OUT / f"choir-resonator-r85-{v}.png"
        img.convert("RGB").save(png, optimize=True)
        size = rig.write_gif(frames, OUT / f"choir-resonator-r85-{v}.gif", fps=FPS)
        print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames), "
              f"{time.time() - t0:.0f} s", flush=True)


if __name__ == "__main__":
    main(sys.argv[1:])
