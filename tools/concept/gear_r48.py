#!/usr/bin/env python3
"""Concept round 48 - the Act 4 "Deep Water" gear looks, a/b proposals (design phase, step m8-agear).

Each subject gets a PNG sheet (the source render, the sprites at 2-4x, key frames, the mechanic as a
diagram, a frame from the play field at 1x) and a GIF in the 480x540 play field under the round-48
under-water grade (enemies_r48.Water: the Stormhawk's headlight, marine snow, units lit by the light
map; bullets x 0.7). The targets are stand-ins (the production Needler and Driftjelly sprites, lit
like the Act 4 units): the new units are on A-units' sheets.

Outputs (no variant chosen yet; round 48 is open):
  design/player/weapons/concept/harpoon-torpedoes-r48-{a,b}.{png,gif}
      the Harpoon Torpedoes (front, anti-sub): 6x22 px torpedo, 250 -> 365 px/s on the screen,
      weak homing (70 deg cone, 90 deg/s), 20 px impact blast; L3 single, then L5 two parallel.
      a: "barbed harpoon": a dark gunmetal shaft with a steel spear head and swept barbs, a teal
         motor ring and the cable-thin line of light trailing behind (the harpoon's line); the
         impact a teal pressure ring with barb sparks and a dark cavity flash.
      b: "heavy torpedo": a white UTC torpedo with a cobalt nose band and a pump-jet shroud, a
         straight white-teal wake streak; the impact a white flash with a double ring.
  design/player/weapons/concept/depth-charges-r48-{a,b}.{png,gif}
      the Depth Charges (rear, anti-sub): rolled out of the stern at 147 px/s, slowing to a hover
      in 0.6 s, sinking with the scroll (shrinking, darker), bursting on the fuse (0.8 s at L3);
      burst radius 52 px (L3) and 64 px (L5, two charges splayed); swimmers rising from behind.
      a: "drum": a navy drum with yellow bands, a white fuse lamp blinking faster; the burst a
         pressure dome: white-teal flash disc, a dark cavity and a bright rim ring to the radius.
      b: "finned canister": a grey canister with a dome and cross fins seen from above, a cyan
         fuse ring that closes; the burst a sharp double shock ring refracting the floor.
  design/player/specials/concept/sonar-pulse-r48-{a,b}.{png,gif}
      the Sonar Pulse under water in the black of Level 26: the ring covers the field in 0.5 s,
      every unit is lit and drawn whole for 6 s (the last second blinks), then the dark returns.
      a: "flood": a soft teal band with two echoes that lights the water itself as it passes;
         units outlined in teal.
      b: "echo marks": a crisp dashed pale-cyan double ring; the water stays black, units are
         drawn whole inside sonar contact brackets that ping as the ring reaches them.
  design/player/wingmen/concept/bulwark-r48-{a,b}.{png,gif}
      the heavy drone "Bulwark" (escort slot): ~30 px, hitbox 14x14, wing slot (56, +20) with the
      water's drag, a slow cannon (1 shell/s), drawing 30 % of the aimed shots within 150 px.
      a: "turtle": a domed armour shell with a cobalt rim band, skirts, a heavy barrel and twin
         pump-jets; draws fire with a slow cyan lure halo.
      b: "riot barge": a slab hull behind a wide curved shield blade, the cannon through its slit,
         four corner thrusters; draws fire with a white strobe beacon.

Reuses the frozen render package, gear_r36 (the production Stormhawk, sprite helpers),
enemies_r48 (the under-water grade, lit sprites, under-water kills), enemies_r03 (enemy bullets),
production sprites from assets/. Deterministic.

Run: python3 tools/concept/gear_r48.py [harpoon-torpedoes] [depth-charges] [sonar-pulse] [bulwark]
     [a|b]   (no args = everything, a few minutes)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageChops, ImageDraw, ImageFilter

sys.path.insert(0, str(Path(__file__).resolve().parent))
import gear_r36 as g36  # noqa: E402  (production Stormhawk, asset(), flash_sprite)
import enemies_r48 as e48  # noqa: E402  (Water, lit, uw_burst; sets the round-48 subtitle)
import enemies_r05 as r5  # noqa: E402
import enemies_r03 as e3  # noqa: E402
import vfx_r08 as v8  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.sdf import (mirror_x, sd_box, sd_capsule, sd_cylinder_x, sd_cylinder_y,  # noqa: E402
                        sd_cylinder_z, sd_ellipsoid, sd_plate, sd_sphere, union)

r5.SUB = "CONCEPT ROUND 48 - 960X540"
FW, FH = 480, 540
SFPS = 12.5
TAU = 2 * np.pi
UP = -np.pi / 2
WHITE = (255, 255, 255)
HULL_C, ACC_C, PS = v8.HULL_C, v8.ACC_C, v8.PS
m_metal, m_glow = v8.m_metal, v8.m_glow
asset, draw_ship = g36.asset, g36.draw_ship
smooth = e48.smooth
D = ROOT / "design" / "player"
OUT = {"weapons": D / "weapons" / "concept", "specials": D / "specials" / "concept",
       "wingmen": D / "wingmen" / "concept"}
TEAL_W = (150, 255, 235)      # the harpoon's line and the player's under-water light (not Vrell's 00ff9a)
SONAR_A = (70, 230, 220)
SONAR_B = (170, 240, 255)


# =========================================================================== shared

def times(seconds):
    return [i / SFPS for i in range(int(round(seconds * SFPS)))]


def save(part, slug, v, sheet, frames):
    folder = OUT[part]
    folder.mkdir(parents=True, exist_ok=True)
    png = folder / f"{slug}-r48-{v}.png"
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(frames, folder / f"{slug}-r48-{v}.gif", fps=SFPS)
    print(f"wrote {png.relative_to(ROOT)} and .gif ({size / 1e6:.1f} MB, {len(frames)} frames)")


def render_up(make, w, h, extent, factor=8, colors=24, **kw):
    """A model facing up (+Y) rendered to a native sprite."""
    scene, mats = make(**kw)
    hi = sdf.render(scene, mats, (w * factor, h * factor), extent)
    return sprite.make_sprite(hi, factor, colors)


def source(make, extent, disp=200, **kw):
    scene, mats = make(**kw)
    hi = sdf.render(scene, mats, (disp, disp), extent)
    cell = e3.checker(disp, disp)
    cell.alpha_composite(sprite.to_image(hi))
    return cell


_GLOW = {}


def glow_of(sp):
    """The emissive pixels of a production sprite (saturated and bright, or near white): they stay
    bright in the dark like the round-48 units' glow."""
    k = id(sp)
    if k not in _GLOW:
        a = np.array(sp).astype(np.float64)
        mx, mn = a[..., :3].max(axis=-1), a[..., :3].min(axis=-1)
        m = (a[..., 3] > 0) & ((((mx - mn) > 110) & (mx > 170)) | (mn > 240))
        a[..., 3] = np.where(m, 255, 0)
        _GLOW[k] = (sp, Image.fromarray(a.astype(np.uint8), "RGBA"))
    return _GLOW[k][1]


def outline_img(sp, col, a=1.0):
    """A 1 px ring round a sprite's silhouette (2 px padding), for the sonar outline."""
    pad = 2
    al = Image.new("L", (sp.width + 2 * pad, sp.height + 2 * pad), 0)
    al.paste(sp.getchannel("A").point(lambda v: 255 if v > 0 else 0), (pad, pad))
    ring = ImageChops.subtract(al.filter(ImageFilter.MaxFilter(3)), al)
    out = Image.new("RGBA", al.size, tuple(col) + (0,))
    out.putalpha(ring.point(lambda v: int(v * a)))
    return out


def lit_put(img, water, sp, x, y, reveal=0.0, flash=False, alpha=1.0, outline=None, deep=0.0):
    """Paste a sprite lit by the water's light map (its glow stays bright); ``reveal`` lifts the
    light (the sonar), ``deep`` darkens it further (a sinking charge)."""
    k = max(water.light(x, y), reveal) * (1 - deep)
    k = round(k * 20) / 20
    tint = min(0.9, water.g["tint"] * (1.2 - min(1.0, k)) + deep * 0.5)
    im = e48.lit(sp, max(k, 0.0), tint, glow_of(sp))
    if flash:
        im = g36.flash_sprite(im, 0.6)
    if alpha < 1.0:
        im = e48.fade(im, alpha)
    if outline is not None:
        sprite.paste_center(img, outline_img(im, *outline), x, y)
    sprite.paste_center(img, im, x, y)


def layer():
    lay = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    return lay, ImageDraw.Draw(lay)


def ring(d, x, y, r, col, a, w=1):
    if r > 0.5:
        d.ellipse([x - r, y - r, x + r, y + r], outline=tuple(col) + (int(np.clip(a, 0, 255)),),
                  width=w)


def disc(d, x, y, r, col, a):
    if r > 0.3:
        d.ellipse([x - r, y - r, x + r, y + r], fill=tuple(col) + (int(np.clip(a, 0, 255)),))


def label(img, x, y, text, col=(230, 240, 250)):
    raster.draw_text(img, int(x), int(y), text, col)


def tag(img, text):
    """A small mock-up caption at the top left of the field (not a game element)."""
    w = raster.text_width(text) + 10
    lay, d = layer()
    d.rectangle([6, 6, 6 + w, 20], fill=(6, 12, 18, 190), outline=(60, 90, 110, 255))
    img.alpha_composite(lay)
    raster.draw_text(img, 11, 10, text, (200, 230, 240))


def crops(frames, pts, w, h):
    out = []
    for fi, x, y in pts:
        x0, y0 = int(round(x - w / 2)), int(round(y - h / 2))
        out.append(frames[min(fi, len(frames) - 1)].crop((x0, y0, x0 + w, y0 + h)))
    return out


def needler(t, ph=0.0):
    return asset(f"needler_{int(t * 10 + ph * 7) % 6}")


def jelly(t, ph=0.0):
    return asset(f"driftjelly_{int(t * 6 + ph * 5) % 4}")


VIOLET_K = (190, 110, 255)
JELLY_K = (120, 230, 255)


class Shots:
    """Aimed enemy bullets (orbs), slowed x 0.7 under water."""

    def __init__(self):
        self.list = []

    def fire(self, t0, x, y, tx, ty, speed=140, who="ship"):
        d = np.hypot(tx - x, ty - y) or 1
        s = speed * e48.WATER_SLOW
        self.list.append(dict(t0=t0, x=x, y=y, vx=(tx - x) / d * s, vy=(ty - y) / d * s,
                              who=who, gone=None))

    def pos(self, b, t):
        return b["x"] + b["vx"] * (t - b["t0"]), b["y"] + b["vy"] * (t - b["t0"])

    def draw(self, img, t):
        for b in self.list:
            if t < b["t0"] or (b["gone"] is not None and t >= b["gone"]):
                continue
            x, y = self.pos(b, t)
            if -10 < x < FW + 10 and -10 < y < FH + 10:
                e3.orb(img, x, y, 4)


# =========================================================================== 1 Harpoon Torpedoes

def _torp_mats(v, glow):
    if v == "a":
        return [m_metal((92, 102, 128)), m_metal(HULL_C[4]), m_metal(ACC_C[0]),
                m_glow(TEAL_W, 1.8 * glow), m_metal(HULL_C[2])]
    return [m_metal(HULL_C[4]), m_metal(HULL_C[1]), m_metal(ACC_C[0]),
            m_glow(PS[3], 1.8 * glow), m_metal(HULL_C[2])]


def harpoon_model(v, glow=1.0):
    if v == "a":
        def scene(p):
            q = mirror_x(p)
            return union(
                (sd_capsule(p, (0, -0.8, 0), (0, 0.42, 0), 0.15), 0),          # shaft
                (sd_capsule(p, (0, 0.38, 0), (0, 1.0, 0), 0.17, 0.02), 1),     # spear head
                (sd_plate(q, [(0.0, 0.66), (0.34, 0.28), (0.27, 0.25), (0.0, 0.5)], 0.0, 0.035), 1),
                (sd_cylinder_y(p, (0, 0.12, 0), 0.18, 0.05), 2),                 # cobalt bands
                (sd_cylinder_y(p, (0, -0.45, 0), 0.17, 0.03), 2),
                (sd_plate(q, [(0.0, -0.55), (0.3, -0.92), (0.25, -0.98), (0.0, -0.82)], 0.0, 0.03), 4),
                (sd_cylinder_y(p, (0, -0.9, 0), 0.11, 0.05), 3),                 # motor ring
                k=0.02)
    else:
        def scene(p):
            q = mirror_x(p)
            return union(
                (sd_capsule(p, (0, -0.72, 0), (0, 0.66, 0), 0.27, 0.24), 0),   # white body
                (sd_cylinder_y(p, (0, 0.5, 0), 0.275, 0.06), 2),                 # nose band
                (sd_sphere(p, (0, 0.78, 0), 0.17), 1),                           # sensor nose
                (sd_cylinder_y(p, (0, -0.84, 0), 0.31, 0.1), 1),                 # pump-jet shroud
                (sd_cylinder_y(p, (0, -0.95, 0), 0.2, 0.03), 3),
                (sd_box(q, (0.3, -0.6, 0), (0.08, 0.1, 0.03)), 4),
                k=0.02)
    return scene, _torp_mats(v, glow)


HARP = {v: rig.AngleSprites(lambda glow=1.0, v=v: harpoon_model(v, glow), 24, count=32,
                            extent=2.2, factor=8, colors=20) for v in "ab"}
H_LEVELS = {1: (1.0 / 0.8, (0,), 37.5, 30), 2: (1.0 / 0.9, (0,), 50, 45), 3: (1.0, (0,), 62, 62),
            4: (1.0, (0,), 80, 80), 5: (1.6, (-6, 6), 80, 100)}
H_T, H_SWITCH = 9.6, 5.0
H_CONE, H_TURN = np.radians(35), np.radians(90)
H_RANGE = 900


def h_ship(t):
    return 240 + 85 * np.sin(t * 0.6), 470


def h_level(t):
    return 3 if t < H_SWITCH else 5


def t_speed(age):
    return 250 + 115 * min(1.0, age / 0.6)


def tpos(tg, t):
    age = t - tg["spawn"]
    if tg["kind"] == "needler":
        return tg["x0"] + 34 * np.sin(age * 0.8 + tg["ph"]), min(tg["y0"], -20 + 140 * age)
    return tg["x0"] + 12 * np.sin(age * 1.1 + tg["ph"]), -20 + 30 * age


def angdiff(a, b):
    return (a - b + np.pi) % TAU - np.pi


def h_scene(v):
    water = e48.Water("edge", seed=31)
    rng = np.random.default_rng(48)
    tgs = [dict(kind="needler", x0=x, y0=y, ph=ph, spawn=s, hp=120, dead=None, fl=-1)
           for x, y, ph, s in ((150, 110, 0.0, -2), (330, 150, 1.5, -2), (240, 70, 3.0, -1))]
    tgs += [dict(kind="jelly", x0=x, y0=0, ph=ph, spawn=s, hp=60, dead=None, fl=-1)
            for x, ph, s in ((90, 0.3, -4), (390, 1.2, -2), (260, 2.2, 1.5))]
    shots = Shots()
    torps, hits, deaths, log = [], [], [], []
    next_fire, next_shot = 0.4, 0.8
    frames = []
    dt = 1 / SFPS
    sub = 4

    def alive(t):
        return [g for g in tgs if g["dead"] is None and g["spawn"] <= t and tpos(g, t)[1] > -10]

    def pick(x, y, h, t):
        best, bd = None, 1e9
        for g in alive(t):
            gx, gy = tpos(g, t)
            if abs(angdiff(np.arctan2(gy - y, gx - x), h)) <= H_CONE:
                dd = np.hypot(gx - x, gy - y)
                if dd < bd:
                    best, bd = g, dd
        return best

    for i in range(int(H_T * SFPS)):
        t = i * dt
        sx, sy = h_ship(t)
        lv = h_level(t)
        if t >= next_fire:
            iv, offs, dmg, _ = H_LEVELS[lv]
            for o in offs:
                x0, y0 = sx + o, sy - 22
                torps.append(dict(x=x0, y=y0, h=UP, t0=t, tg=pick(x0, y0, UP, t), hist=[(x0, y0)],
                                  run=0.0, dmg=dmg, gone=None))
            next_fire += iv
        if t >= next_shot:
            al = [g for g in alive(t) if g["kind"] == "needler" and tpos(g, t)[1] > 30]
            if al:
                g = al[int(rng.integers(len(al)))]
                gx, gy = tpos(g, t)
                shots.fire(t, gx, gy + 10, sx, sy)
            next_shot += 0.9
        for k in range(sub):                                  # torpedo flight
            tt = t + k * dt / sub
            for tp in torps:
                if tp["gone"] is not None:
                    continue
                if tp["tg"] is None or tp["tg"]["dead"] is not None:
                    tp["tg"] = pick(tp["x"], tp["y"], tp["h"], tt)
                if tp["tg"] is not None:
                    gx, gy = tpos(tp["tg"], tt)
                    want = np.arctan2(gy - tp["y"], gx - tp["x"])
                    tp["h"] += np.clip(angdiff(want, tp["h"]), -H_TURN * dt / sub, H_TURN * dt / sub)
                s = t_speed(tt - tp["t0"]) * dt / sub
                tp["x"] += np.cos(tp["h"]) * s
                tp["y"] += np.sin(tp["h"]) * s
                tp["run"] += s
                tp["hist"].append((tp["x"], tp["y"]))
                if tp["run"] > H_RANGE or tp["y"] < -30:
                    tp["gone"] = tt
                    continue
                for g in alive(tt):
                    gx, gy = tpos(g, tt)
                    if np.hypot(gx - tp["x"], gy - tp["y"]) < 14:
                        tp["gone"] = tt
                        hits.append((tt, tp["x"], tp["y"]))
                        for g2 in alive(tt):                  # direct hit + 20 px blast at half
                            g2x, g2y = tpos(g2, tt)
                            dd = np.hypot(g2x - tp["x"], g2y - tp["y"])
                            dmg = tp["dmg"] if g2 is g else (tp["dmg"] / 2 if dd < 20 + 12 else 0)
                            if dmg:
                                g2["hp"] -= dmg
                                g2["fl"] = tt
                                if g2["hp"] <= 0:
                                    g2["dead"] = tt
                                    deaths.append((tt, g2x, g2y, g2["kind"]))
                                    nx = float(rng.uniform(70, 410))
                                    tgs.append(dict(kind=g2["kind"], x0=nx,
                                                    y0=float(rng.uniform(70, 170)),
                                                    ph=float(rng.uniform(0, TAU)), spawn=tt + 1.3,
                                                    hp=120 if g2["kind"] == "needler" else 60,
                                                    dead=None, fl=-1))
                        break
        # ---- draw
        f = water.frame(t, sx, sy)
        for g in tgs:
            if g["dead"] is None and g["spawn"] <= t:
                gx, gy = tpos(g, t)
                sp = needler(t, g["ph"]) if g["kind"] == "needler" else jelly(t, g["ph"])
                lit_put(f, water, sp, gx, gy, flash=0 <= t - g["fl"] < 0.1)
        for td, x, y, kind in deaths:
            s = (t - td) / 0.5
            if 0 <= s <= 1:
                f = e48.uw_burst(f, x, y, s, 1.0 if kind == "needler" else 0.8,
                                 VIOLET_K if kind == "needler" else JELLY_K)
        lay, d = layer()
        for tp in torps:
            if tp["gone"] is not None and t > tp["gone"] + 0.35:
                continue
            fade = 1.0 if tp["gone"] is None else max(0.0, 1 - (t - tp["gone"]) / 0.35)
            h_trail(d, v, tp, t, fade)
        f.alpha_composite(lay)
        for tp in torps:
            if tp["gone"] is None:
                f = h_motor(f, v, tp)
                sprite.paste_center(f, HARP[v].get(tp["h"]), tp["x"], tp["y"])
                log.append((i, tp["x"], tp["y"]))
        for th, x, y in hits:
            s = (t - th) / 0.45
            if 0 <= s <= 1:
                f = h_hit(f, v, x, y, s)
        shots.draw(f, t)
        draw_ship(f, sx, sy, t)
        f = raster.add_light(f, sx, sy - 24, 5, TEAL_W, 0.6 if any(0 <= t - tp["t0"] < 0.1
                                                                    for tp in torps) else 0.0)
        tag(f, f"HARPOON TORPEDOES L{lv} - {H_LEVELS[lv][3]} DPS, FULL DAMAGE UNDER WATER")
        frames.append(f)
    return frames, hits, log


def h_trail(d, v, tp, t, fade):
    pts = tp["hist"][::-1]
    if v == "a":                       # the cable: 1 px line of light, slack waves toward its end
        length, acc = 150.0, 0.0
        for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
            seg = np.hypot(x1 - x0, y1 - y0)
            if seg < 1e-6:
                continue
            l0, acc = acc, acc + seg
            if l0 > length:
                break
            nx, ny = -(y1 - y0) / seg, (x1 - x0) / seg
            o0 = 1.8 * (l0 / length) * np.sin(l0 * 0.08 - t * 9)
            o1 = 1.8 * (acc / length) * np.sin(acc * 0.08 - t * 9)
            a = 235 * (1 - l0 / length) * fade
            p0, p1 = (x0 + nx * o0, y0 + ny * o0), (x1 + nx * o1, y1 + ny * o1)
            d.line([p0, p1], fill=(60, 160, 170, int(a * 0.35)), width=3)
            d.line([p0, p1], fill=TEAL_W + (int(a),), width=1)
    else:                              # the wake: a straight white-teal streak
        length, acc = 80.0, 0.0
        for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
            l0, acc = acc, acc + np.hypot(x1 - x0, y1 - y0)
            if l0 > length:
                break
            a = (1 - l0 / length) * fade
            d.line([(x0, y0), (x1, y1)], fill=(170, 235, 235, int(110 * a)), width=4)
            d.line([(x0, y0), (x1, y1)], fill=(235, 255, 255, int(230 * a)), width=1)


def h_motor(f, v, tp):
    bx, by = tp["x"] - np.cos(tp["h"]) * 11, tp["y"] - np.sin(tp["h"]) * 11
    return raster.add_light(f, bx, by, 3.5 if v == "a" else 4.5, TEAL_W if v == "a" else PS[3],
                            0.7)


def h_hit(f, v, x, y, s):
    lay, d = layer()
    if v == "a":
        if s < 0.25:
            disc(d, x, y, 6, (4, 18, 24), 210 * (1 - s / 0.25))       # the dark cavity: the thud
        ring(d, x, y, 4 + 16 * smooth(s / 0.6), TEAL_W, 230 * (1 - s), 2 if s < 0.3 else 1)
        for k in range(5):                                           # barb sparks
            a = k * TAU / 5 + 0.5
            r0 = 3 + 14 * s
            d.line([(x + np.cos(a) * r0, y + np.sin(a) * r0),
                    (x + np.cos(a) * (r0 + 4), y + np.sin(a) * (r0 + 4))],
                   fill=WHITE + (int(255 * (1 - s)),), width=1)
        f.alpha_composite(lay)
        return raster.add_light(f, x, y, 4 + 8 * (1 - s), TEAL_W, 1.3 * (1 - s))
    if s < 0.22:
        disc(d, x, y, 7 * (1 - s), WHITE, 255 * (1 - s / 0.22))
    ring(d, x, y, 20 * np.sqrt(s), WHITE, 255 * (1 - s), 2 if s < 0.25 else 1)
    ring(d, x, y, 13 * np.sqrt(s), PS[2], 220 * (1 - s), 1)
    f.alpha_composite(lay)
    return raster.add_light(f, x, y, 6 + 10 * (1 - s), (210, 245, 255), 1.5 * (1 - s))


def h_levels_panel(v):
    """L1..L5 and overdrive: the volley ahead of the ship's nose, at 1x on a water crop."""
    water = e48.Water("edge", seed=31)
    base = water.frame(0.0, 240, 470)
    cells = []
    for lv in (1, 2, 3, 4, 5, "OD"):
        key = 5 if lv == "OD" else lv
        iv, offs, dmg, dps = H_LEVELS[key]
        c = base.crop((200, 300, 280, 500)).copy()
        lay, d = layer()
        for o in offs:
            for k in range(2):
                y = 150 - 22 - (k + 0.4) * t_speed(0.6) * iv * 0.3
                tp = dict(x=40 + o, y=y, h=UP, hist=[(40 + o, y + 60 - j) for j in range(60)][::-1])
                h_trail(d, v, tp, 0.0, 1.0)
        c.alpha_composite(lay.crop((0, 0, 80, 200)))
        for o in offs:
            for k in range(2):
                y = 150 - 22 - (k + 0.4) * t_speed(0.6) * iv * 0.3
                sprite.paste_center(c, HARP[v].get(UP), 40 + o, y)
        draw_ship(c, 40, 170, 0.0)
        cells.append(c)
    labs = ["L1 30", "L2 45", "L3 62", "L4 80", "L5 100", "OD 128"]
    return r5.grid(cells, 6, 1, labels=labs)


def h_homing_panel(v):
    """A torpedo fired straight up turning at 90 deg/s onto a target 30 deg off its axis."""
    water = e48.Water("edge", seed=31)
    c = water.frame(0.0, 120, 290).crop((0, 0, 240, 300))
    lay, d = layer()
    sx, sy = 120, 280
    for a in (UP - H_CONE, UP + H_CONE):
        d.line([(sx, sy - 24), (sx + np.cos(a) * 300, sy - 24 + np.sin(a) * 300)],
               fill=(200, 220, 230, 120), width=1)
    gx, gy = 190, 80
    x, y, h, pts, t = sx, sy - 24, UP, [], 0.0
    while y > gy + 6 and t < 2:
        want = np.arctan2(gy - y, gx - x)
        h += np.clip(angdiff(want, h), -H_TURN / 50, H_TURN / 50)
        s = t_speed(t) / 50
        x, y, t = x + np.cos(h) * s, y + np.sin(h) * s, t + 0.02
        pts.append((x, y, h))
    tp = dict(hist=[(p[0], p[1]) for p in pts], x=pts[-1][0], y=pts[-1][1], h=pts[-1][2])
    h_trail(d, v, tp, 0.0, 1.0)
    c.alpha_composite(lay.crop((0, 0, 240, 300)))
    sprite.paste_center(c, needler(0.0), gx, gy)
    for k in range(0, len(pts), 12):
        sprite.paste_center(c, HARP[v].get(pts[k][2]), pts[k][0], pts[k][1])
    draw_ship(c, sx, sy, 0.0)
    label(c, 8, 8, "70 DEG CONE")
    label(c, 8, 20, "TURN 90 DEG/S")
    return sprite.enlarge(c, 1)


def h_sheet(v, frames, hits, log):
    sp = [HARP[v].frame(k) for k in (24, 26, 28, 0, 4, 8, 12, 16)]
    labs = ["UP", "", "", "RIGHT", "", "DOWN", "", "LEFT"]
    th, hx, hy = hits[min(2, len(hits) - 1)]
    fi0 = int(round(th * SFPS))
    imp = crops(frames, [(fi0 + k, hx, hy) for k in range(0, 6)], 48, 48)
    fly = [(fi, x, y) for fi, x, y in log if 120 < y < 330][:1]
    trail = crops(frames, [(fly[0][0], fly[0][1], fly[0][2] + 40)], 80, 120) if fly else []
    panels = [("SOURCE (TOP VIEW)", source(lambda: harpoon_model(v), 2.4)),
              ("6X22 IN A 24X24 CELL, 8 OF 32 HEADINGS (4X)", r5.grid(sp, 8, 4, labels=labs)),
              ("PER LEVEL (1X): SHOTS AND DPS", h_levels_panel(v)),
              ("WEAK HOMING (1X)", h_homing_panel(v)),
              ("IMPACT, 0.08 S STEPS, 20 PX BLAST (3X)", r5.grid(imp, 6, 3))]
    if trail:
        panels.append(("IN FLIGHT (2X)", r5.grid(trail, 1, 2)))
    name = {"a": "BARBED HARPOON", "b": "HEAVY TORPEDO"}[v]
    notes = {
        "a": ["HARPOON TORPEDOES A - BARBED HARPOON",
              "A DARK GUNMETAL SHAFT WITH A STEEL SPEAR HEAD AND TWO SWEPT BARBS, COBALT BANDS, SMALL TAIL FINS AND",
              "A TEAL MOTOR RING; BEHIND IT THE HARPOON'S LINE: A CABLE-THIN LINE OF LIGHT (150 PX) THAT SLACKS INTO",
              "A SLOW WAVE TOWARD ITS END. IMPACT: A DARK CAVITY FLASH (THE THUD), A TEAL PRESSURE RING TO THE",
              "20 PX BLAST RADIUS AND FIVE BARB SPARKS."],
        "b": ["HARPOON TORPEDOES B - HEAVY TORPEDO",
              "A WHITE UTC TORPEDO WITH A COBALT NOSE BAND, A DARK SENSOR NOSE AND A PUMP-JET SHROUD WITH A CYAN",
              "GLOW; BEHIND IT A STRAIGHT WHITE-TEAL WAKE STREAK (80 PX), NO BUBBLES. IMPACT: A WHITE FLASH AND A",
              "DOUBLE RING (WHITE AT THE 20 PX BLAST RADIUS, CYAN INSIDE IT).",
              ""]}[v]
    notes += ["BOTH: FRONT SLOT, ANTI-SUB: FULL DAMAGE UNDER WATER (Q1 B). 250 -> 365 PX/S ON THE SCREEN OVER 0.6 S",
              "(360 -> 520 X 0.7), RANGE 900 PX UNDER WATER. WEAK HOMING: THE NEAREST TARGET IN A 70 DEG CONE AT",
              "THE SHOT, TURNING AT MOST 90 DEG/S, A NEW PICK WHEN IT DIES. THE GIF: L3 (ONE TORPEDO A SECOND, 62)",
              f"UNTIL {H_SWITCH:.0f} S, THEN L5 (TWO PARALLEL, 6 PX APART); STAND-IN TARGETS (NEEDLERS 120 HP,",
              "DRIFTJELLIES 60 HP) UNDER THE L24 GRADE. THE TAG AT THE TOP IS A MOCK-UP CAPTION, NOT A GAME ELEMENT."]
    gi = fly[0][0] if fly else int(3 * SFPS)
    return r5.build_sheet(f"HARPOON TORPEDOES {v.upper()} - {name} (FRONT, ANTI-SUB, L22)", panels,
                          notes, frames[gi])


def make_harpoon(variants):
    for v in variants:
        frames, hits, log = h_scene(v)
        print("harpoon hits:", len(hits))
        save("weapons", "harpoon-torpedoes", v, h_sheet(v, frames, hits, log), frames)


# =========================================================================== 2 Depth Charges

def charge_model(v):
    if v == "a":
        mats = [m_metal(HULL_C[1]), m_metal(HULL_C[3]), m_metal(ACC_C[5]), m_glow(PS[3], 0.5)]

        def scene(p):
            return union(
                (sd_cylinder_x(p, (0, 0, 0), 0.4, 0.5), 0),                       # navy drum
                (sd_cylinder_x(p, (0.53, 0, 0), 0.36, 0.05), 1),
                (sd_cylinder_x(p, (-0.53, 0, 0), 0.36, 0.05), 1),
                (sd_cylinder_x(p, (0.26, 0, 0), 0.415, 0.05), 2),                 # yellow bands
                (sd_cylinder_x(p, (-0.26, 0, 0), 0.415, 0.05), 2),
                (sd_sphere(p, (0.0, 0.0, 0.38), 0.09), 3),                        # fuse lamp
                k=0.02)
    else:
        mats = [m_metal(HULL_C[3]), m_metal(HULL_C[4]), m_metal(HULL_C[1]), m_glow(PS[2], 0.6),
                m_metal(ACC_C[0])]

        def scene(p):
            return union(
                (sd_cylinder_z(p, (0, 0, 0), 0.36, 0.3), 0),                      # canister
                (sd_sphere(p, (0, 0, 0.2), 0.3), 1),                              # dome
                (sd_box(p, (0, 0, -0.12), (0.62, 0.05, 0.14), 0.02), 2),          # cross fins
                (sd_box(p, (0, 0, -0.12), (0.05, 0.62, 0.14), 0.02), 2),
                (sd_cylinder_z(p, (0, 0, 0.06), 0.375, 0.04), 4),                 # cobalt band
                (sd_sphere(p, (0, 0, 0.47), 0.07), 3),
                k=0.02)
    return scene, mats


CHG = {v: rig.AngleSprites(lambda v=v: charge_model(v), 16, count=16,
                           extent=1.7 if v == "a" else 1.6, factor=8, colors=16) for v in "ab"}
DC_LEVELS = {1: (1.0, 1, 0.9, 25, 40), 2: (1 / 1.2, 1, 0.9, 31.7, 46), 3: (1 / 1.3, 1, 0.8, 40, 52),
             4: (1 / 1.4, 1, 0.8, 47, 58), 5: (1 / 0.8, 2, 0.7, 50, 64)}
DC_V0, DC_STOP, DC_SINK = 147.0, 0.6, 30.0
DC_T, DC_SWITCH = 10.0, 5.2


def dc_ship(t):
    return 240 + 70 * np.sin(t * 0.5), 300 + 8 * np.sin(t * 0.9)


def dc_level(t):
    return 3 if t < DC_SWITCH else 5


def dc_pos(c, t):
    tau = t - c["t0"]
    s = min(tau, DC_STOP)
    run = DC_V0 * (s - s * s / (2 * DC_STOP))
    sink = DC_SINK * max(0.0, tau - DC_STOP)
    return c["x0"] + np.cos(c["dir"]) * run, c["y0"] + np.sin(c["dir"]) * run + sink


def dc_scene(v):
    water = e48.Water("edge", seed=57)
    rng = np.random.default_rng(7)
    charges, bursts, deaths, warn = [], [], [], []
    swim = []
    for k, ts in enumerate((0.2, 0.9, 1.7, 2.6, 3.3, 4.1, 5.0, 5.6, 6.4, 7.0, 7.6, 8.3)):
        ta = ts + 2.7                                              # when it reaches the charges
        x = dc_ship(ta)[0] + float(rng.uniform(-34, 34))
        swim.append(dict(ts=ts, x0=x, x=x, ph=float(rng.uniform(0, TAU)), hp=40, dead=None, fl=-1))
        warn.append((ts, x))
    next_fire = 0.5
    frames, log = [], []
    for i in range(int(DC_T * SFPS)):
        t = i / SFPS
        sx, sy = dc_ship(t)
        lv = dc_level(t)
        iv, n, fuse, dmg, rad = DC_LEVELS[lv]
        if t >= next_fire:
            dirs = (np.pi / 2,) if n == 1 else (np.pi / 2 - np.radians(18), np.pi / 2 + np.radians(18))
            offs = (0,) if n == 1 else (-8, 8)
            for dr, o in zip(dirs, offs):
                charges.append(dict(t0=t, x0=sx + o, y0=sy + 24, dir=dr, fuse=fuse, dmg=dmg, rad=rad,
                                    spin0=float(rng.uniform(0, TAU)), burst=None))
            next_fire += iv

        for s in swim:                                             # they steer in behind the ship
            if s["ts"] <= t and s["dead"] is None:
                s["x"] += (sx - s["x"]) * min(1.0, 0.9 / SFPS)

        def spos(s, tt):
            age = tt - s["ts"]
            return s["x"] + 8 * np.sin(age * 1.4 + s["ph"]), FH + 24 - 62 * age

        for c in charges:                                          # fuses
            if c["burst"] is None and t - c["t0"] >= c["fuse"]:
                c["burst"] = c["t0"] + c["fuse"]
                bx, by = dc_pos(c, c["burst"])
                bursts.append((c["burst"], bx, by, c["rad"]))
                log.append((i, bx, by, c["rad"]))
                for s in swim:
                    if s["dead"] is None and s["ts"] <= t:
                        x, y = spos(s, t)
                        if np.hypot(x - bx, y - by) <= c["rad"] + 10:
                            s["hp"] -= c["dmg"]
                            s["fl"] = t
                            if s["hp"] <= 0:
                                s["dead"] = t
                                deaths.append((t, x, y))
        # ---- draw
        f = water.frame(t, sx, sy)
        for s in swim:
            if s["dead"] is None and s["ts"] <= t:
                x, y = spos(s, t)
                if y > -30:
                    lit_put(f, water, jelly(t, s["ph"]), x, y, flash=0 <= t - s["fl"] < 0.1)
        for td, x, y in deaths:
            f = e48.uw_burst(f, x, y, (t - td) / 0.5, 0.8, JELLY_K)
        for c in charges:
            if c["burst"] is not None:
                continue
            tau = t - c["t0"]
            x, y = dc_pos(c, t)
            spin = c["spin0"] + (5.0 * min(tau, DC_STOP) - 2.5 * min(tau, DC_STOP) ** 2 / DC_STOP
                                 + 0.6 * max(0, tau - DC_STOP))
            sp = CHG[v].get(spin)
            sink = smooth((tau - 0.2) / (c["fuse"] - 0.2))
            sc = 1 - 0.22 * sink
            sp = sp.resize((max(4, round(sp.width * sc)), max(4, round(sp.height * sc))),
                           Image.BILINEAR)
            lit_put(f, water, sp, x, y, deep=0.25 * sink)
            f = dc_fuse(f, v, x, y, tau / c["fuse"], sc)
        for tb, x, y, rad in bursts:
            s = (t - tb) / 0.55
            if 0 <= s <= 1:
                f = dc_burst(f, v, x, y, rad, s)
        draw_ship(f, sx, sy, t)
        for ts, x in warn:
            if 0 <= t - ts < 0.9:
                e48.edge_warning(f, t, [x])
        tag(f, f"DEPTH CHARGES L{lv} - FUSE {fuse:.1f} S, BURST {rad} PX")
        frames.append(f)
    return frames, log


def dc_fuse(f, v, x, y, p, sc):
    """The fuse tell: a: a white lamp blinking faster; b: a cyan ring that closes."""
    if v == "a":
        period = 0.32 - 0.24 * p
        if (p * 0.9 / period) % 1.0 < 0.5:
            return raster.add_light(f, x, y, 3.0, (210, 250, 255), 1.2)
        return f
    lay, d = layer()
    r = 8 * sc
    d.arc([x - r, y - r, x + r, y + r], -90, -90 + 360 * min(1.0, p), fill=PS[2] + (230,), width=1)
    f.alpha_composite(lay)
    return f


def refract(f, x, y, r, amp, band=10):
    """Displace the frame radially round a ring of radius r (the shock front's refraction)."""
    R = int(r + band + 4)
    x0, y0 = int(x) - R, int(y) - R
    box = (max(0, x0), max(0, y0), min(FW, x0 + 2 * R), min(FH, y0 + 2 * R))
    if box[2] <= box[0] or box[3] <= box[1]:
        return f
    a = np.array(f.crop(box))
    yy, xx = np.mgrid[box[1]:box[3], box[0]:box[2]].astype(np.float64)
    dx, dy = xx - x, yy - y
    dd = np.hypot(dx, dy) + 1e-6
    w = np.exp(-((dd - r) / band) ** 2) * amp
    sxx = np.clip(np.round(xx - dx / dd * w).astype(int) - box[0], 0, a.shape[1] - 1)
    syy = np.clip(np.round(yy - dy / dd * w).astype(int) - box[1], 0, a.shape[0] - 1)
    out = a[syy, sxx]
    f.paste(Image.fromarray(out, "RGBA"), box[:2])
    return f


def dc_burst(f, v, x, y, R, s):
    lay, d = layer()
    if v == "a":                       # pressure dome: flash disc, dark cavity, bright rim ring
        if s < 0.2:
            disc(d, x, y, R * 0.55 * smooth(s / 0.2), (200, 255, 245), 210 * (1 - s / 0.2))
        if 0.12 < s < 0.6:
            disc(d, x, y, R * 0.3 * (1 - (s - 0.12) / 0.48), (4, 16, 22), 170)
        rr = R * smooth(min(1.0, s / 0.35))
        ring(d, x, y, rr, (170, 255, 240), 240 * (1 - s), 3 if s < 0.25 else 2)
        ring(d, x, y, rr - 4, (90, 200, 200), 120 * (1 - s), 1)
        if s > 0.35:
            ring(d, x, y, R, (120, 180, 170), 70 * (1 - s), 1)
        f.alpha_composite(lay)
        if s < 0.3:
            f = raster.add_light(f, x, y, R * 0.6, (160, 240, 230), 1.1 * (1 - s / 0.3))
        return f
    r1 = R * min(1.0, s / 0.3) ** 0.6     # shock ring: refraction, double ring, a white core
    if s < 0.6:
        f = refract(f, x, y, r1, 4 * (1 - s / 0.6))
    if s < 0.12:
        disc(d, x, y, 5, WHITE, 255)
    ring(d, x, y, r1, WHITE, 255 * (1 - s), 2 if s < 0.3 else 1)
    ring(d, x, y, r1 * 0.82, PS[2], 200 * (1 - s), 1)
    f.alpha_composite(lay)
    if s < 0.2:
        f = raster.add_light(f, x, y, 9, (220, 245, 255), 1.4 * (1 - s / 0.2))
    return f


def dc_diagram(v):
    """Roll-out, hover, sink and burst behind the stern, with the radius per level (1x)."""
    water = e48.Water("edge", seed=57)
    c = water.frame(0.0, 120, 60).crop((60, 0, 300, 300)).copy()
    sx, sy = 120, 60
    lay, d = layer()
    ch = dict(t0=0.0, x0=sx, y0=sy + 24, dir=np.pi / 2)
    for tau in np.arange(0, 0.9, 0.02):
        x, y = dc_pos(ch, tau)
        d.point((x, y), fill=(220, 230, 240, 200))
    for rad, lv in ((40, "L1"), (52, "L3"), (64, "L5")):
        bx, by = dc_pos(ch, 0.8)
        ring(d, bx, by, rad, (170, 255, 240) if lv == "L3" else (110, 150, 160), 200, 1)
    c.alpha_composite(lay.crop((0, 0, 240, 300)))
    for tau, lab, lx, ly in ((0.0, "0 S", -34, -3), (0.3, "0.3 S", -40, -3), (0.6, "0.6 HOVER", 12, -9),
                             (0.8, "0.8 BURST", 12, 4)):
        x, y = dc_pos(ch, tau)
        sink = smooth((tau - 0.2) / 0.6)
        sp = CHG[v].get(1.0 + tau * 3)
        sc = 1 - 0.22 * sink
        sp = sp.resize((max(4, round(sp.width * sc)), max(4, round(sp.height * sc))), Image.BILINEAR)
        sprite.paste_center(c, sp, x, y)
        label(c, x + lx, y + ly, lab)
    draw_ship(c, sx, sy, 0.0)
    label(c, 8, 200, "RADIUS 40 / 52 / 64 PX")
    label(c, 8, 212, "(L1 / L3 / L5)")
    return c


def dc_sheet(v, frames, log):
    spins = [CHG[v].frame(k) for k in range(0, 16, 2)]
    sink = []
    for p in (0.0, 0.33, 0.66, 1.0):
        sp = CHG[v].frame(0)
        sc = 1 - 0.22 * p
        sp = sp.resize((max(4, round(sp.width * sc)), max(4, round(sp.height * sc))), Image.BILINEAR)
        cell = Image.new("RGBA", (20, 20), (14, 52, 62, 255))
        sprite.paste_center(cell, e48.lit(sp, 1 - 0.4 * p, 0.1 + 0.5 * p * 0.4), 10, 10)
        sink.append(cell)
    l3 = [b for b in log if b[3] == 52]
    l5 = [b for b in log if b[3] == 64]
    strips = []
    for sel in (l3[2:3], l5[1:2]):
        if sel:
            fi, x, y, rad = sel[0]
            strips.append(r5.grid(crops(frames, [(fi + k, x, y) for k in range(6)], 140, 140), 6, 1))
    panels = [("SOURCE (TOP VIEW)", source(lambda: charge_model(v), 1.9)),
              ("16X16, SPINNING AS IT ROLLS OUT (8 OF 16, 4X)", r5.grid(spins, 8, 4)),
              ("SINKING: SMALLER AND DARKER (4X)", r5.grid(sink, 4, 4, labels=["0", "", "", "FUSE"])),
              ("ROLL-OUT, HOVER, SINK, BURST (1X)", dc_diagram(v))]
    if strips:
        panels.append(("BURST L3, 52 PX, 0.08 S STEPS (1X)", strips[0]))
    if len(strips) > 1:
        panels.append(("BURST L5, 64 PX (1X)", strips[1]))
    name = {"a": "DRUM", "b": "FINNED CANISTER"}[v]
    notes = {
        "a": ["DEPTH CHARGES A - DRUM",
              "A NAVY DRUM WITH LIGHT END CAPS AND TWO YELLOW BANDS, TUMBLING AS IT ROLLS OUT; THE FUSE IS A WHITE LAMP",
              "ON TOP THAT BLINKS FASTER AS IT RUNS DOWN. BURST: A PRESSURE DOME - A WHITE-TEAL FLASH DISC, A DARK",
              "CAVITY THAT COLLAPSES, A BRIGHT RIM RING THAT REACHES THE RADIUS IN 0.19 S AND A FAINT SILT RING."],
        "b": ["DEPTH CHARGES B - FINNED CANISTER",
              "A GREY CANISTER SEEN FROM ABOVE: A PALE DOME, A COBALT BAND AND FOUR CROSS FINS THAT SHOW ITS SPIN;",
              "THE FUSE IS A CYAN RING THAT CLOSES ROUND IT. BURST: A SHARP DOUBLE SHOCK RING (WHITE AT THE RADIUS,",
              "CYAN INSIDE) THAT REFRACTS THE FLOOR AS IT PASSES, AND A SHORT WHITE CORE; NO FIRE BALL."]}[v]
    notes += ["BOTH: REAR SLOT, ANTI-SUB, AREA. A CHARGE LEAVES THE STERN AT 147 PX/S (140 X 1.5 RANGE X 0.7 SPEED),",
              "SLOWS TO A HOVER IN 0.6 S (~44 PX BEHIND), SINKS WITH THE SCROLL (SMALLER, DARKER) AND BURSTS ON ITS FUSE,",
              "NEVER ON CONTACT; THE BURST HITS EVERY UNIT INSIDE THE RADIUS ONCE AND NEVER THE SHIP OR ROOK.",
              f"THE GIF: L3 (52 PX, 1.3/S, FUSE 0.8 S) UNTIL {DC_SWITCH:.0f} S, THEN L5 (TWO CHARGES SPLAYED 18 DEG, 64 PX,",
              "FUSE 0.7 S); STAND-IN DRIFTJELLIES (40 HP) RISE FROM BEHIND WITH THE REAR! WARNING. L24 GRADE."]
    gi = l3[3][0] + 2 if len(l3) > 3 else int(4 * SFPS)
    return r5.build_sheet(f"DEPTH CHARGES {v.upper()} - {name} (REAR, ANTI-SUB, L22)", panels, notes,
                          frames[gi])


def make_charges(variants):
    for v in variants:
        frames, log = dc_scene(v)
        print("depth-charge bursts:", len(log))
        save("weapons", "depth-charges", v, dc_sheet(v, frames, log), frames)


# =========================================================================== 3 Sonar Pulse

SP_T, SP_CALL, SP_DUR, SP_COVER = 9.6, 1.6, 6.0, 0.5
SP_UNITS = [("needler", 110, 120, 0.0), ("needler", 350, 90, 1.3), ("needler", 250, 210, 2.1),
            ("needler", 400, 300, 3.3), ("jelly", 70, 260, 0.4), ("jelly", 180, 330, 1.7),
            ("jelly", 310, 380, 2.9), ("needler", 60, 400, 0.9)]


def sp_ship(t):
    return 240 + 50 * np.sin(t * 0.55), 470


def sp_upos(u, t):
    kind, x, y, ph = u
    if kind == "needler":
        return x + 18 * np.sin(t * 0.7 + ph), y
    return x + 8 * np.sin(t * 1.0 + ph), y + 12 * np.sin(t * 0.4 + ph)


def sp_state(t):
    """(radius of the ring, its centre, the window's strength 0..1, blink-off now)."""
    if t < SP_CALL:
        return None
    ox, oy = sp_ship(SP_CALL)
    far = max(np.hypot(cx - ox, cy - oy) for cx in (0, FW) for cy in (0, FH))
    r = far * (t - SP_CALL) / SP_COVER
    left = SP_CALL + SP_DUR - t
    blink = 0 < left < 1.0 and int(t * 8) % 2 == 1
    on = 1.0 if left > 0 else 0.0
    return r, (ox, oy), on, blink, left


def sp_scene(v):
    water = e48.Water("black", seed=73)
    amb0 = water.amb.copy()
    xx, yy = water.xx, water.yy
    frames, pings = [], {}
    for t in times(SP_T):
        px, py = sp_ship(t)
        st = sp_state(t)
        if v == "a" and st is not None:                         # the pulse lights the water itself
            r, (ox, oy), on, blink, left = st
            dd = np.hypot(xx - ox, yy - oy)
            inside = np.clip((r - dd) / 30.0, 0, 1)
            env = np.clip(left / 1.5, 0, 1)
            water.amb = amb0 + 0.34 * inside * env
        else:
            water.amb = amb0
        f = water.frame(t, px, py)
        if v == "a" and st is not None and st[4] > 0:
            r, (ox, oy), on, blink, left = st
            tint = Image.new("RGBA", (FW, FH), SONAR_A + (0,))
            dd = np.hypot(xx - ox, yy - oy)
            a = np.clip((r - dd) / 30.0, 0, 1) * 34 * np.clip(left / 1.5, 0, 1)
            tint.putalpha(Image.fromarray(a.astype(np.uint8), "L"))
            f.alpha_composite(tint)
        lay, d = layer()
        for k, u in enumerate(SP_UNITS):
            x, y = sp_upos(u, t)
            sp = needler(t, u[3]) if u[0] == "needler" else jelly(t, u[3])
            reveal, outline = 0.0, None
            if st is not None and st[2] > 0:
                r, (ox, oy), on, blink, left = st
                if r >= np.hypot(x - ox, y - oy):
                    pings.setdefault(k, t)
                    reveal = 1.0
                    if v == "a" and not blink:
                        outline = (SONAR_A, 1.0)
                    if v == "b":
                        sp_brackets(d, x, y, sp, t - pings[k], blink)
            lit_put(f, water, sp, x, y, reveal=reveal, outline=outline)
        f.alpha_composite(lay)
        if st is not None and st[0] < 1400:
            f = sp_ring(f, v, st)
        draw_ship(f, px, py, t)
        if st is None:
            tag(f, "LEVEL 26, THE BLACK: HEADLIGHT ONLY")
        elif st[4] > 0:
            tag(f, f"SONAR PULSE: {max(0.0, st[4]):.1f} S - LIT, FULL DAMAGE")
        else:
            tag(f, "SONAR OVER: THE DARK RETURNS")
        frames.append(f)
    return frames


def sp_ring(f, v, st):
    r, (ox, oy), on, blink, left = st
    if v == "a":
        R = int(r + 10)
        x0, y0 = max(0, int(ox) - R), max(0, int(oy) - R)
        x1, y1 = min(FW, int(ox) + R), min(FH, int(oy) + R)
        if x1 <= x0 or y1 <= y0:
            return f
        a = np.array(f.crop((x0, y0, x1, y1))).astype(np.float64)
        yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64)
        dd = np.hypot(xx - ox, yy - oy)
        band = np.exp(-((dd - r) / 4.0) ** 2) + 0.35 * np.exp(np.minimum(0, dd - r) / 26.0) * (dd < r)
        for e in (70, 140):                                       # two faint echoes
            band += 0.25 * np.exp(-((dd - (r - e)) / 3.0) ** 2) * (r - e > 0)
        a[..., :3] += band[..., None] * np.array(SONAR_A) * 0.9
        f.paste(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"), (x0, y0))
        return f
    lay, d = layer()
    for rr, al in ((r, 240), (r - 7, 130)):
        if rr <= 0:
            continue
        for k in range(36):
            a0 = k * 10
            d.arc([ox - rr, oy - rr, ox + rr, oy + rr], a0, a0 + 6, fill=SONAR_B + (al,), width=1)
    f.alpha_composite(lay)
    return f


def sp_brackets(d, x, y, sp, since, blink):
    """Sonar contact brackets: a bright ping as the ring reaches the unit, then steady marks."""
    if blink:
        return
    w, h = sp.width / 2 + 3, sp.height / 2 + 3
    grow = max(0.0, 1 - since / 0.3) * 8
    w, h = w + grow, h + grow
    a = 255 if since < 0.4 else 170
    c = SONAR_B + (a,)
    L = 5
    for sxn in (-1, 1):
        for syn in (-1, 1):
            cx, cy = x + sxn * w, y + syn * h
            d.line([(cx, cy), (cx - sxn * L, cy)], fill=c, width=1)
            d.line([(cx, cy), (cx, cy - syn * L)], fill=c, width=1)


def sp_hud(v):
    """The special row mock-ups: ready, active (the 6 s arc drains), last second, NO WATER."""
    cells = []
    col = SONAR_A if v == "a" else SONAR_B
    for state in ("ready", "active", "blink", "nowater"):
        c = Image.new("RGBA", (150, 30), (10, 16, 24, 255))
        d = ImageDraw.Draw(c)
        d.rectangle([0, 0, 149, 29], outline=(60, 80, 100, 255))
        grey = state == "nowater"
        ic = (90, 100, 110) if grey else col
        for rr in (4, 7, 10):                                     # the icon: three arcs
            d.arc([15 - rr, 15 - rr, 15 + rr, 15 + rr], -60, 60, fill=ic + (255,), width=1)
        d.ellipse([13, 13, 17, 17], fill=ic + (255,))
        raster.draw_text(c, 32, 5, "SONAR", (120, 130, 140) if grey else (220, 235, 245))
        if state == "ready":
            raster.draw_text(c, 32, 17, "X3  READY", (220, 235, 245))
        elif state == "nowater":
            raster.draw_text(c, 32, 17, "NO WATER", (150, 150, 150))
        else:
            p = 0.62 if state == "active" else 0.12
            d.arc([118, 4, 140, 26], -90, -90 + 360 * p, fill=col + (255,), width=3)
            raster.draw_text(c, 32, 17, "X2  FULL DMG", col if state == "active" or True else col)
        cells.append(c)
    return r5.grid(cells, 2, 2, labels=["READY", "ACTIVE 3.7 S LEFT", "LAST SECOND", "DRY LEVEL"])


def sp_above(v):
    """Above water: a submerged Driftjelly (the sub layer) revealed and hittable for 6 s."""
    cells = []
    for on in (False, True):
        c = Image.new("RGBA", (64, 64), (20, 70, 96, 255))
        sp = asset("driftjelly-sub_0")
        if on:
            if v == "a":
                sprite.paste_center(c, outline_img(sp, SONAR_A), 32, 32)
            else:
                lay = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
                sp_brackets(ImageDraw.Draw(lay), 32, 32, sp, 1.0, False)
                c.alpha_composite(lay)
        sprite.paste_center(c, sp, 32, 32)
        cells.append(c)
    return r5.grid(cells, 2, 2, labels=["SUB LAYER", "REVEALED"])


def sp_sheet(v, frames):
    fi = [int((SP_CALL + k) * SFPS) for k in (-0.5, 0.16, 0.32, 0.48, 2.0, 5.5)]
    strip = [fr.resize((160, 180), Image.LANCZOS) for fr in (frames[min(i, len(frames) - 1)]
                                                             for i in fi)]
    labs = ["BEFORE", "+0.16 S", "+0.32 S", "+0.48 S", "+2 S", "+5.5 S BLINK"]
    k0 = int((SP_CALL + 2.0) * SFPS)
    kd = int((SP_CALL - 0.4) * SFPS)
    ud, ul = sp_upos(SP_UNITS[7], kd / SFPS), sp_upos(SP_UNITS[7], k0 / SFPS)
    dark = crops(frames, [(kd, ud[0], ud[1])], 72, 72)
    lit_ = crops(frames, [(k0, ul[0], ul[1])], 72, 72)
    panels = [("THE PULSE AND THE 6 S WINDOW (1/3 SIZE)", r5.grid(strip, 3, 1, labels=labs)),
              ("A UNIT IN THE BLACK, THEN LIT (3X)", r5.grid(dark + lit_, 2, 3)),
              ("ABOVE WATER: THE SUB LAYER (2X)", sp_above(v)),
              ("HUD SPECIAL ROW (MOCK-UP, 2X)", sp_hud(v))]
    name = {"a": "FLOOD", "b": "ECHO MARKS"}[v]
    notes = {
        "a": ["SONAR PULSE A - FLOOD",
              "A SOFT TEAL BAND WITH A BRIGHT LEADING EDGE AND TWO FAINT ECHOES SWEEPS OUT FROM THE SHIP; THE WATER IT",
              "PASSES IS LIT (A TEAL CAST ON THE FLOOR) AND EVERY UNIT IS DRAWN WHOLE WITH A 1 PX TEAL OUTLINE. THE",
              "LIGHT FADES OVER THE LAST 1.5 S, THE OUTLINES BLINK IN THE LAST SECOND."],
        "b": ["SONAR PULSE B - ECHO MARKS",
              "A CRISP DASHED PALE-CYAN DOUBLE RING (A SONAR DISPLAY'S SWEEP); THE WATER STAYS BLACK. EACH UNIT THE RING",
              "REACHES IS DRAWN WHOLE INSIDE FOUR CONTACT BRACKETS THAT PING (SNAP IN FROM 8 PX) AND THEN STAY; THE",
              "BRACKETS BLINK IN THE LAST SECOND. A LAMP, NOT A LOCK: NOTHING ELSE CHANGES ON THE UNITS."]}[v]
    notes += ["BOTH: SPECIAL, 4 CHARGES (250 CR), WATER ONLY. THE RING COVERS THE FIELD IN 0.5 S; FOR 6 S EVERY UNIT IS LIT",
              "AND DRAWN WHOLE AND EVERY WEAPON DOES FULL DAMAGE (THE X 0.7 IS OFF); A SECOND CALL RESTARTS THE 6 S.",
              "THE GIF: LEVEL 26'S BLACK (HEADLIGHT ONLY, Q4 A), STAND-IN NEEDLERS AND DRIFTJELLIES; THE TAG AT THE TOP IS",
              "A MOCK-UP CAPTION. COLOUR: KEPT TOWARD THE PLAYER'S CYAN, AWAY FROM THE VRELL / SIREN TEAL (00FF9A)."]
    return r5.build_sheet(f"SONAR PULSE {v.upper()} - {name} (SPECIAL, L22)", panels, notes, frames[k0])


def make_sonar(variants):
    for v in variants:
        frames = sp_scene(v)
        save("specials", "sonar-pulse", v, sp_sheet(v, frames), frames)


# =========================================================================== 4 Bulwark

def _bw_mats(strobe=0.0):
    return [m_metal(HULL_C[3]), m_metal(HULL_C[1]), m_metal(ACC_C[0]), m_glow(ACC_C[2], 1.6),
            m_metal(HULL_C[4]), m_metal((58, 62, 78)), m_glow(WHITE, 0.3 + 1.6 * strobe),
            m_metal(ACC_C[5])]


def bulwark_model(v, strobe=0.0):
    if v == "a":
        def scene(p):
            q = mirror_x(p)
            return union(
                (sd_ellipsoid(p, (0, -0.05, 0), (0.66, 0.72, 0.3)), 0),           # armour shell
                (sd_box(p, (0, -0.05, 0.2), (0.3, 0.34, 0.1), 0.08), 4),          # raised plate
                (sd_cylinder_z(p, (0, -0.05, -0.02), 0.7, 0.06), 2),              # rim band
                (sd_box(q, (0.66, -0.08, 0), (0.1, 0.42, 0.12), 0.05), 1),        # skirts
                (sd_sphere(p, (0, 0.42, 0.18), 0.2), 1),                          # mantlet
                (sd_capsule(p, (0, 0.45, 0.14), (0, 0.9, 0.14), 0.13), 5),        # heavy barrel
                (sd_cylinder_y(p, (0, 0.88, 0.14), 0.17, 0.07), 1),               # muzzle brake
                (sd_cylinder_y(q, (0.38, -0.78, 0.0), 0.16, 0.12), 1),            # pump-jets
                (sd_cylinder_y(q, (0.38, -0.91, 0.0), 0.11, 0.02), 3),
                (sd_sphere(p, (0, 0.12, 0.33), 0.07), 3),                         # sensor eye
                k=0.02)
    else:
        blade = [(-0.86, 0.5), (-0.62, 0.74), (0.0, 0.86), (0.62, 0.74), (0.86, 0.5),
                 (0.74, 0.42), (0.0, 0.66), (-0.74, 0.42)]

        def scene(p):
            q = mirror_x(p)
            return union(
                (sd_box(p, (0, -0.12, 0), (0.48, 0.58, 0.18), 0.12), 0),         # slab hull
                (sd_plate(p, blade, 0.1, 0.12, 0.03), 4),                         # shield blade
                (sd_box(q, (0.62, 0.5, 0.22), (0.12, 0.04, 0.02)), 7),            # hazard marks
                (sd_capsule(p, (0, 0.4, 0.16), (0, 0.98, 0.16), 0.12), 5),        # cannon in the slit
                (sd_cylinder_z(q, (0.55, -0.58, 0), 0.17, 0.12), 1),              # corner thrusters
                (sd_cylinder_z(q, (0.55, -0.58, 0.13), 0.09, 0.02), 3),
                (sd_cylinder_z(q, (0.56, 0.12, 0), 0.15, 0.1), 1),
                (sd_cylinder_z(q, (0.56, 0.12, 0.11), 0.08, 0.02), 3),
                (sd_box(p, (0, -0.3, 0.2), (0.22, 0.2, 0.06), 0.04), 2),          # cobalt deck
                (sd_sphere(p, (0, -0.3, 0.3), 0.1), 6),                           # strobe beacon
                k=0.02)
    return scene, _bw_mats(strobe)


_BW = {}


def bw_sprite(v, strobe=0.0):
    key = (v, strobe)
    if key not in _BW:
        _BW[key] = render_up(lambda: bulwark_model(v, strobe), 36, 36, 2.05, factor=8, colors=28)
    return _BW[key]


def damaged(sp, seed=3):
    """Scorched plates and a few sparks (the armour under half)."""
    rng = np.random.default_rng(seed)
    a = np.array(sp).astype(np.float64)
    n = raster.fbm(sp.width, sp.height, 5, seed, octaves=2, period=False)
    a[..., :3] *= np.where(n > 0.58, 0.42, 1.0)[..., None]
    ys, xs = np.nonzero(a[..., 3] > 0)
    for k in rng.choice(len(xs), 4, replace=False):
        a[ys[k], xs[k], :3] = (230, 250, 255)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


BW_T = 10.0
BW_SLOT = (56, 20)


def bw_ship(t):
    return 200 + 80 * np.sin(t * 0.55), 440 - 12 * np.sin(t * 0.9)


def bw_scene(v):
    water = e48.Water("edge", seed=91)
    rng = np.random.default_rng(22)
    nd = [dict(x0=x, y0=y, ph=ph, hp=42, dead=None, spawn=-1.0, fl=-1)
          for x, y, ph in ((120, 120, 0.0), (300, 90, 1.7), (380, 170, 3.1))]
    shots = Shots()
    shells, hits, deaths, flashes, log = [], [], [], [], []
    bx, by = bw_ship(0)[0] + BW_SLOT[0], bw_ship(0)[1] + BW_SLOT[1]
    next_gun, next_shot = 0.5, 0.4
    redirect = 0
    frames = []
    dt = 1 / SFPS

    def npos(n, t):
        age = t - n["spawn"]
        return n["x0"] + 30 * np.sin(t * 0.7 + n["ph"]), min(n["y0"], -20 + 150 * age)

    for i in range(int(BW_T * SFPS)):
        t = i * dt
        sx, sy = bw_ship(t)
        tx, ty = sx + BW_SLOT[0], sy + BW_SLOT[1]
        bx += (tx - bx) * min(1.0, dt * 2.6)                     # the water's drag: it lags
        by += (ty - by) * min(1.0, dt * 2.6)
        live = [n for n in nd if n["dead"] is None and n["spawn"] <= t]
        if t >= next_shot and live:
            n = live[int(rng.integers(len(live)))]
            x, y = npos(n, t)
            near = np.hypot(bx - sx, by - sy) <= 150
            to_bw = near and rng.uniform() < 0.3
            redirect += to_bw
            ax, ay = bx, by
            if to_bw:                                            # aimed with lead at the drone
                for _ in range(3):
                    lead = np.hypot(ax - x, ay - y - 10) / (140 * e48.WATER_SLOW)
                    qx, qy = bw_ship(t + lead)
                    ax, ay = qx + BW_SLOT[0], qy + BW_SLOT[1]
            shots.fire(t, x, y + 10, ax if to_bw else sx, ay if to_bw else sy,
                       who="bw" if to_bw else "ship")
            log.append((i, "shot", to_bw))
            next_shot += 0.45
        if t >= next_gun:
            shells.append(dict(t0=t, x=bx, y=by - 17, gone=None))
            flashes.append(t)
            next_gun += 1.0
        for b in shots.list:                                     # bullets on the Bulwark
            if b["gone"] is None and b["who"] == "bw" and b["t0"] <= t:
                x, y = shots.pos(b, t)
                if np.hypot(x - bx, y - by) < 12:
                    b["gone"] = t
                    hits.append(t)
        for s in shells:
            if s["gone"] is not None:
                continue
            s["y"] -= 420 * dt
            for n in live:
                x, y = npos(n, t)
                if abs(x - s["x"]) < 13 and abs(y - s["y"]) < 16:
                    s["gone"] = t
                    n["hp"] -= 14
                    n["fl"] = t
                    if n["hp"] <= 0:
                        n["dead"] = t
                        deaths.append((t, x, y))
                        nd.append(dict(x0=float(rng.uniform(80, 400)), y0=float(rng.uniform(80, 180)),
                                       ph=float(rng.uniform(0, TAU)), hp=42, dead=None,
                                       spawn=t + 1.2, fl=-1))
                    break
            if s["y"] < -10:
                s["gone"] = t
        # ---- draw
        f = water.frame(t, sx, sy)
        for n in nd:
            if n["dead"] is None and n["spawn"] <= t:
                x, y = npos(n, t)
                lit_put(f, water, needler(t, n["ph"]), x, y, flash=0 <= t - n["fl"] < 0.1)
        for td, x, y in deaths:
            f = e48.uw_burst(f, x, y, (t - td) / 0.5, 1.0, VIOLET_K)
        lay, d = layer()
        for s in shells:
            if s["gone"] is None:
                bw_shell(d, s["x"], s["y"])
        f.alpha_composite(lay)
        strobe = 1.0 if v == "b" and (t % 0.8) < 0.1 else 0.0
        hit = any(0 <= t - h < 0.1 for h in hits)
        if v == "a":
            f = bw_lure(f, bx, by, t)
        sh = sprite.shadow_of(bw_sprite(v), opacity=0.45, blur=1.2, scale=0.85)
        sprite.paste_center(f, sh, bx + 14, by + 20)
        lit_put(f, water, bw_sprite(v, strobe), bx, by, flash=hit)
        f = bw_jets(f, v, bx, by, t)
        if strobe:
            f = raster.add_light(f, bx, by + 4, 10, WHITE, 0.9)
        if any(0 <= t - fl < 0.1 for fl in flashes):
            f = raster.add_light(f, bx, by - 18, 5, PS[3], 1.1)
        for h in hits:
            if 0 <= t - h < 0.25:
                f = bw_spark(f, bx, by - 6, (t - h) / 0.25, int(h * 100))
        shots.draw(f, t)
        draw_ship(f, sx, sy, t)
        tag(f, f"BULWARK - WING SLOT, DRAWS FIRE ({redirect} SHOTS ON IT)")
        frames.append(f)
    return frames, hits, log


def bw_shell(d, x, y):
    d.line([(x, y + 4), (x, y + 16)], fill=(170, 230, 240, 110), width=3)    # straight wake
    d.rectangle([x - 1.5, y - 4, x + 1.5, y + 4], fill=PS[2] + (255,))
    d.rectangle([x - 0.5, y - 3, x + 0.5, y + 3], fill=WHITE + (255,))


def bw_lure(f, x, y, t):
    """Variant a's draws-fire tell: a slow cyan halo ring breathing out every second."""
    s = t % 1.0
    lay, d = layer()
    ring(d, x, y, 15 + 8 * s, ACC_C[2], 120 * (1 - s), 1)
    f.alpha_composite(lay)
    return f


def bw_jets(f, v, x, y, t):
    k = 0.55 + 0.15 * np.sin(t * 23)
    if v == "a":
        for o in (-5, 5):
            f = raster.add_light(f, x + o, y + 13, 3, ACC_C[2], k)
        return f
    for ox, oy in ((-8, 9), (8, 9), (-8, -2), (8, -2)):
        f = raster.add_light(f, x + ox, y + oy, 2.2, ACC_C[2], k * 0.8)
    return f


def bw_spark(f, x, y, s, seed):
    rng = np.random.default_rng(seed)
    lay, d = layer()
    for _ in range(5):
        a = rng.uniform(-np.pi, 0)
        r = 3 + 9 * s
        d.point((x + np.cos(a) * r, y + np.sin(a) * r), fill=(230, 250, 255, int(255 * (1 - s))))
    f.alpha_composite(lay)
    return raster.add_light(f, x, y, 4, (200, 240, 255), 1.0 * (1 - s))


def bw_formation(v):
    water = e48.Water("edge", seed=91)
    c = water.frame(0.0, 110, 200).crop((0, 40, 260, 300)).copy()
    sx, sy = 110, 160
    bx, by = sx + BW_SLOT[0], sy + BW_SLOT[1]
    lay, d = layer()
    ring(d, sx, sy, 150, (200, 220, 230), 110, 1)
    d.line([(sx, sy), (bx, sy), (bx, by)], fill=(230, 240, 250, 140), width=1)
    for k, (ex, ey) in enumerate(((60, 10), (140, 0), (220, 30))):
        tx, ty = (bx, by) if k == 1 else (sx, sy)
        d.line([(ex, ey), (tx, ty)], fill=(255, 64, 255, 120) if k != 1 else (255, 230, 120, 170),
               width=1)
    c.alpha_composite(lay.crop((0, 0, 260, 260)))
    sprite.paste_center(c, bw_sprite(v), bx, by)
    draw_ship(c, sx, sy, 0.0)
    label(c, sx + 4, sy - 12, "56")
    label(c, bx + 4, sy + 6, "+20")
    label(c, 6, 248, "150 PX: 30 % OF AIMED SHOTS GO TO IT")
    return c


def bw_sheet(v, frames, hits):
    sp = bw_sprite(v)
    states = [sp, g36.flash_sprite(sp, 0.6), damaged(sp)]
    if v == "b":
        states.append(bw_sprite(v, 1.0))
    labs = ["INTACT", "HIT", "UNDER HALF"] + (["STROBE"] if v == "b" else [])
    scale = Image.new("RGBA", (150, 56), (0, 0, 0, 0))
    sprite.paste_center(scale, asset("ship_2"), 28, 28)
    sprite.paste_center(scale, asset("rook_2"), 80, 28)
    sprite.paste_center(scale, sp, 128, 28)
    lost = []
    for s in (0.0, 0.2, 0.4, 0.6, 0.8):
        c = Image.new("RGBA", (64, 64), (14, 52, 62, 255))
        if s < 0.1:
            sprite.paste_center(c, damaged(sp), 32, 32)
        elif s < 0.3:
            sprite.paste_center(c, g36.flash_sprite(damaged(sp), 0.5), 32, 32)
        c = e48.uw_burst(c.convert("RGBA"), 32, 32, s, 1.3, (200, 240, 255))
        lost.append(c)
    hf = int(round(hits[0] * SFPS)) if hits else int(4 * SFPS)
    panels = [("SOURCE (TOP VIEW)", source(lambda: bulwark_model(v), 2.6)),
              ("36X36, HIT BOX 14X14 (4X)", r5.grid(states, len(states), 4, labels=labs)),
              ("STORMHAWK, ROOK, BULWARK (2X)", sprite.enlarge(scale, 2)),
              ("WING SLOT AND DRAWING FIRE (1X)", bw_formation(v)),
              ("DESTROYED: OUT FOR THE LEVEL (2X)", r5.grid(lost, 5, 2))]
    name = {"a": "TURTLE", "b": "RIOT BARGE"}[v]
    notes = {
        "a": ["BULWARK A - TURTLE",
              "A DOMED BLUE-GREY ARMOUR SHELL WITH A PALE RAISED CENTRE PLATE, A COBALT RIM BAND, DARK SIDE SKIRTS, A HEAVY",
              "BARREL WITH A MUZZLE BRAKE ON A BALL MANTLET, A CYAN SENSOR EYE AND TWIN PUMP-JETS. DRAWS FIRE WITH A",
              "SLOW CYAN LURE HALO THAT BREATHES OUT ONCE A SECOND (THE TELL THAT IT IS THE DECOY)."],
        "b": ["BULWARK B - RIOT BARGE",
              "A BLUE-GREY SLAB HULL BEHIND A WIDE CURVED SHIELD BLADE WITH YELLOW HAZARD MARKS, THE CANNON THROUGH ITS",
              "SLIT, FOUR CORNER THRUSTERS AND A COBALT DECK; DRAWS FIRE WITH A WHITE STROBE BEACON ON THE DECK THAT",
              "FLASHES EVERY 0.8 S."]}[v]
    notes += ["BOTH: ESCORT SLOT (THE HEAVY DRONE, WAS \"WARDEN\"), ARMOUR 120, NO SHIELD, 200 PX/S, HIT BOX 14X14; WING",
              "SLOT (56, +20) WITH THE HYDRO-KIT'S DRAG (IT LAGS THE SHIP); A SLOW CANNON: 1 SHELL/S, 14 DAMAGE, ANTI-SUB",
              "UNDER WATER (A CYAN SLUG WITH A STRAIGHT WAKE). WITHIN 150 PX OF THE SHIP 30 % OF THE AIMED SHOTS GO TO",
              "IT. DESTROYED: AN UNDER-WATER BURST, OUT FOR THE REST OF THE LEVEL. GIF: L24 GRADE, STAND-IN NEEDLERS;",
              "THE PLAYER'S GUNS ARE OFF FOR CLARITY; THE TAG AT THE TOP IS A MOCK-UP CAPTION."]
    return r5.build_sheet(f"BULWARK {v.upper()} - {name} (HEAVY DRONE, ESCORT, L22)", panels, notes,
                          frames[min(hf + 3, len(frames) - 1)])


def make_bulwark(variants):
    for v in variants:
        frames, hits, log = bw_scene(v)
        print("bulwark hits:", len(hits), "of", len(log), "shots")
        save("wingmen", "bulwark", v, bw_sheet(v, frames, hits), frames)


# =========================================================================== main

PARTS = {"harpoon-torpedoes": make_harpoon, "depth-charges": make_charges,
         "sonar-pulse": make_sonar, "bulwark": make_bulwark}


def main(args):
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    names = [a for a in args if a in PARTS] or list(PARTS)
    for n in names:
        PARTS[n](variants)


if __name__ == "__main__":
    main(sys.argv[1:])
