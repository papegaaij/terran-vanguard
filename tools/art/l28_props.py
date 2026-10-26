#!/usr/bin/env python3
"""Production art: Level 28's props and the breakout's ice (design/campaign/act-4-deep-water/level-28-abyssal-maw,
"Secrets and pickups", "Layout" sections 6-7; the boss spec's *The reveal*; M8 part H batch, concept round 58, step
ART-H2; straight to production, no concept of their own: designed to the README, the survey sub and buoy of Level 25
(tools/art/l25_props.py, its buoy model imported unchanged), the *Marlin*'s resin skin of Level 27
(tools/art/l27_props.py) and readability rule 7 (the light rim) for the secret). The egg racks, the collapsed rack over
*Pelagic-3* and the bore's lip are backdrop pieces (tools/art/backdrop_l28.py).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse: the
trigger's ``sprite`` and the drop's ``sprite`` once Level 28's data names them; the breakout's ``ice-slab*`` and
``ice-shatter*`` once SpriteUse names them, step H5):
  recon-sub_0..2         64x64, the secret: the UTC recon sub *Pelagic-3* nose-down under the collapsed egg rack (a 20
                         HP `ground` trigger, hard 28, t~58, x 90; `sprite: recon-sub`, `size: [64, 64]`): the slate-grey
                         Coalition hull with its pale band and the sail's hull number, the bow buried toward the lower
                         left, two of the rack's bones fallen across her, the dorsal hatch sealed under Vrell resin
                         (violet-brown, teal veins); 0 sealed, 1 hit (the resin cracked), 2 opened (the resin burst off,
                         the hatch dark: the crate rises from it); rule 7's light rim
  recon-sub-glow_0..7    64x64, additive, aligned: 0-3 her emergency beacon's faint red blink on the sail (until the
                         first hit), 4-7 the resin's teal veins glinting through the cracks and sparks (after it)
  service-buoy_0..2      40x40, the antechamber's armour patch carrier (`drops:` `sprite: service-buoy`, t=88): Level
                         25's buoy model in Thera Deep's dull orange with a white band; 0 shut, 1 the hatch swinging
                         open, 2 open and empty
  service-buoy-glow_0..3 40x40, additive: its strobe's blink (Level 25's, 4 fps)
  ice-slab_<s*8+f>       96x96, s 0-3 four slabs of the shattered ceiling, f 0-7 a tumble (the slab turning over about its
                         long axis: the lit top face, the edge, the dark wet underside), normal blending; the game spins
                         and scales them as they fly out of the field (step H5's breakout)
  ice-shatter_0..9       320x320, additive: the ceiling's white burst at the breakout (a flash, the ring of light
                         running out along radial cracks, fading), 20 fps, centred on the ship's x at the breakout line
  design/campaign/act-4-deep-water/level-28-abyssal-maw/concept/l28-props-final-r58-a.png/.gif   the review sheet and
                         GIF (on Level 28's production backdrop under the hatchery light's stand-in: *Pelagic-3* shot open
                         and her crate rising, the buoy releasing its patch, the breakout: the burst, the slabs tumbling
                         out, the night ice)

Run: python3 tools/art/l28_props.py [--review]   (~1 min; --review rebuilds only the review; run backdrop_l28.py
first: the review stands on its floor)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import l24_props as p24
import l25_props as p25
import l28kit as kit
from artkit import TAU

from render import raster  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

kit.install()     # l25_props / l24_props imported l24kit: this level's held-sprite rule again

SCRIPT = "l28_props.py"
BATCH = "M8 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r58"
artkit.REVIEW_ROUND = ROUND
CONCEPT = kit.LEVEL_DIR / "concept"
SUB = (64, 64)
AXIS = (-0.70, -0.71)                       # the hull's axis in model space (y up): the bow toward the lower left
RED = np.array((255, 60, 40), float)
TEAL = np.array((40, 255, 200), float)
SLAB, SHATTER = 96, 320
render, rimmed = p24.render, p24.rimmed


# =========================================================================== Pelagic-3

def mats(state):
    scorch = (lambda p, n: np.where(np.hypot(p[:, 0] - 3, p[:, 1] - 2) < 7, 0.4, 1.0)) if state else None
    veins = (lambda p, n: np.where(np.abs(np.sin(p[:, 0] * 1.3 + np.sin(p[:, 1] * 0.9) * 2.2)) < 0.16, 1.5, 1.0))
    return [Material((0.50, 0.54, 0.58), metal=0.45, shininess=40, spec=0.5, pattern=scorch),  # 0 the slate hull
            Material((0.80, 0.80, 0.76), metal=0.2, shininess=40, spec=0.5),                   # 1 the pale band, number
            Material((0.30, 0.32, 0.36), metal=0.6, shininess=50, spec=0.6),                   # 2 sail, planes, fittings
            Material((0.34, 0.22, 0.32), metal=0.0, shininess=70, spec=0.9, pattern=veins),    # 3 the resin skin
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),                    # 4 the dark hatch
            Material((0.30, 0.24, 0.28), metal=0.0, shininess=8, spec=0.1),                    # 5 resin silt
            Material((0.62, 0.56, 0.58), metal=0.0, shininess=30, spec=0.4),                   # 6 the rack's bones
            Material((0.9, 0.5, 0.4), metal=0.1, shininess=120, spec=1.2)]                     # 7 the beacon's lens


def scene(state):
    ang = float(np.arctan2(AXIS[1], AXIS[0]))

    def sc(p):
        q = rotate_z(p - np.array([0.0, 1.0, 0.0]), ang)        # hull-local: x along (bow +), y across
        zc = 7.0 - 0.24 * q[:, 0]                                # the bow sinks
        qq = q.copy()
        qq[:, 2] = q[:, 2] - zc + 7.0
        items = [(sd_capsule(qq, (19.0, 0, 7.0), (-20.0, 0, 7.0), 7.4, 6.6), 0),
                 (sd_capsule(qq, (-2.0, 0, 7.0), (-4.5, 0, 7.0), 7.7, 7.7), 1),                  # the pale band
                 (sd_box(qq, (6.0, 0, 13.4), (4.6, 1.6, 3.0), 0.9), 2),                          # the sail
                 (sd_box(qq, (6.5, 0, 16.2), (2.0, 1.75, 0.5), 0.3), 1),                         # its hull number
                 (sd_box(qq, (6.0, 0, 13.4), (0.6, 6.0, 0.6), 0.3), 2),                          # sail planes
                 (sd_box(qq, (-20.0, 0, 7.0), (1.2, 7.5, 0.7), 0.4), 2),                         # the stern's cross
                 (sd_box(qq, (-20.0, 0, 7.0), (1.2, 0.7, 7.0), 0.4), 2),
                 (sd_sphere(qq, (8.0, 1.0, 17.0), 1.1), 7)]                                     # the beacon on the sail
        hatch = sd_box(qq, (-9.0, 0, 13.0), (4.0, 3.4, 1.4), 0.9)
        if state == 2:
            items.append((subtract((hatch, 2), sd_box(qq, (-9.0, 0, 14.4), (3.0, 2.5, 2.4), 0.5))[0], 2))
            items.append((sd_box(qq, (-9.0, 0, 12.2), (3.0, 2.5, 0.5), 0.3), 4))
            for k, (sx, sy) in enumerate(((-15, 12), (-2, -13), (-22, -6))):                    # resin shards, flung
                items.append((sd_ellipsoid(q, (sx, sy, 1.4 + k * 0.3), (3.0, 2.0, 0.8)), 3))
        elif state == 1:
            skin = subtract((sd_ellipsoid(qq, (-9.0, 0, 14.2), (6.5, 5.0, 2.2)), 3),
                            sd_sphere(qq, (-8.0, 1.0, 16.0), 2.4))
            items.append((hatch, 2))
            items.append(skin)
        else:
            items.append((hatch, 2))
            items.append((sd_ellipsoid(qq, (-9.0, 0, 14.2), (6.5, 5.0, 2.2)), 3))
        items.append((sd_capsule(p, (-26.0, 16.0, 17.0), (22.0, -8.0, 9.0), 2.2, 1.6), 6))     # the rack's bones across
        items.append((sd_capsule(p, (-14.0, -24.0, 4.0), (16.0, 22.0, 15.0), 1.8, 1.4), 6))
        items.append((sd_ellipsoid(q, (18, 0, 0.8), (13.0, 12.0, 4.0)), 5))                    # resin silt at the bow
        items.append((sd_ellipsoid(q, (-2, -11, 0.6), (16.0, 4.5, 2.2)), 5))
        return union(*items, k=0.6)
    return sc


def _px(u, v):
    """Hull-local (u along, v across) -> frame px (ignoring height)."""
    ax, ay = AXIS
    x, y = ax * u - ay * v, ay * u + ax * v + 1
    return SUB[0] / 2 + x, SUB[1] / 2 - y


def beacon_px():
    return _px(8.0, 1.0)


def hatch_px():
    return _px(-9.0, 0.0)


def sub_frame(state):
    return rimmed(render(scene(state), mats(state), SUB, z_top=40))


def sub_glow(i):
    w, h = SUB
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    if i < 4:                                            # the emergency beacon's faint red blink, one a cycle
        on = [0.8, 0.25, 0.0, 0.0][i]
        mx, my = beacon_px()
        r = np.hypot(xx - mx, yy - my + 3)
        f = (np.exp(-(r / 1.1) ** 2) + np.exp(-(r / 4.0) ** 2) * 0.35) * on
        rgb = RED[None, None] * artkit.ordered_dither(np.clip(f, 0, 1), 7)[..., None]
    else:                                                # the resin's teal glints and sparks after the hit
        rng = np.random.default_rng(2850 + i)
        mx, my = hatch_px()
        s = np.zeros((h, w))
        for _ in range(int(rng.integers(3, 6))):
            sx, sy = mx + rng.normal(0, 4), my + rng.normal(0, 3)
            s += np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / 1.2) * rng.uniform(0.5, 1.0)
        rgb = TEAL[None, None] * artkit.ordered_dither(np.clip(s, 0, 1), 6)[..., None] * 0.8
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== the service buoy

def buoy_mats():
    return [Material((0.62, 0.34, 0.14), metal=0.35, shininess=45, spec=0.55),   # 0 Thera Deep's dull orange float
            Material((0.86, 0.86, 0.84), metal=0.15, shininess=40, spec=0.5),    # 1 the white band
            Material((0.4, 0.42, 0.44), metal=0.6, shininess=50, spec=0.6),      # 2 steel: mast, reflector
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),      # 3 the empty hatch
            Material((0.55, 0.42, 0.30), metal=0.3, shininess=40, spec=0.5),     # 4 the hatch door
            Material((0.9, 0.86, 0.7), metal=0.2, shininess=120, spec=1.2)]      # 5 the strobe's lens


def buoy_frame(state):
    return rimmed(render(p25.buoy_scene(state), buoy_mats(), (p25.BUOY, p25.BUOY), z_top=40))


# =========================================================================== the breakout's ice

def slab_shape(s):
    """An irregular slab outline (unit polygon, long axis along x) and its crack lines."""
    rng = np.random.default_rng(2870 + s)
    n = int(rng.integers(6, 9))
    angs = np.sort(rng.uniform(0, TAU, n))
    rx, ry = rng.uniform(0.85, 1.0), rng.uniform(0.45, 0.65)
    pts = [(math.cos(a) * rx * rng.uniform(0.75, 1.0), math.sin(a) * ry * rng.uniform(0.75, 1.0)) for a in angs]
    cracks = []
    for _ in range(int(rng.integers(1, 3))):
        a = rng.uniform(0, TAU)
        cracks.append(((rng.uniform(-0.3, 0.3), rng.uniform(-0.2, 0.2)), (math.cos(a) * 0.9, math.sin(a) * 0.5)))
    return pts, cracks, float(rng.uniform(0.85, 1.0))


def slab(s, f, n=8):
    """Slab s at tumble frame f: turning about its long axis, the top face foreshortened as cos(phase), the
    underside (dark, wet, teal-tinted) showing past 90 degrees, the edge band between."""
    ss = 3
    size = SLAB * ss
    pts, cracks, scale = slab_shape(s)
    phase = f / n * TAU
    c = math.cos(phase)
    top = c >= 0
    R = size * 0.42 * scale
    k = max(0.12, abs(c))
    edge = abs(math.sin(phase)) * 0.12
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)

    def poly(dy, sy, col):
        return [(size / 2 + x * R, size / 2 + y * R * sy + dy) for x, y in pts], col

    face = (214, 230, 244) if top else (70, 104, 120)
    side = (140, 170, 196)
    sgn = 1 if math.sin(phase) >= 0 else -1
    dy = sgn * edge * R
    p_side, _ = poly(dy, k, None)
    d.polygon(p_side, fill=side + (255,))
    p_face, _ = poly(0, k, None)
    d.polygon(p_face, fill=face + (255,))
    for (x0, y0), (x1, y1) in cracks:
        d.line([(size / 2 + x0 * R, size / 2 + y0 * R * k), (size / 2 + x1 * R, size / 2 + y1 * R * k)],
               fill=(120, 150, 176, 255) if top else (30, 60, 72, 255), width=ss)
    rim = [(x, y) for x, y in p_face]
    d.line(rim + [rim[0]], fill=(244, 250, 255, 255) if top else (110, 150, 160, 255), width=ss)
    small = img.resize((SLAB, SLAB), Image.BOX)
    a = np.array(small).astype(float)
    alpha = (a[..., 3] > 110).astype(float)
    rgb = a[..., :3] / np.maximum(a[..., 3:4] / 255, 1e-3)
    n2 = raster.fbm(SLAB, SLAB, 4, 2880 + s, octaves=2, period=False)
    rgb = rgb * (0.92 + 0.16 * n2[..., None])
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def shatter(i, n=10):
    """The ceiling's burst: a flash, a ring of light running out, radial cracks lit along it, fading (additive)."""
    w = SHATTER
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - w / 2, yy - w / 2)
    th = np.arctan2(yy - w / 2, xx - w / 2)
    u = i / (n - 1)
    ring_r = 20 + 140 * u ** 0.7
    fade = (1 - u) ** 1.4
    rng = np.random.default_rng(2890)
    rays = np.zeros((w, w))
    for _ in range(18):
        a = rng.uniform(0, TAU)
        width = rng.uniform(0.012, 0.03)
        dth = np.abs((th - a + math.pi) % TAU - math.pi)
        rays = np.maximum(rays, np.clip(1 - dth / width, 0, 1) * (r < ring_r * rng.uniform(0.8, 1.05)))
    flash = np.exp(-(r / (30 + 60 * u)) ** 2) * (1.0 if i == 0 else 0.7 * (1 - u) ** 2)
    ring = np.exp(-((r - ring_r) / (8 + 10 * u)) ** 2) * 0.8 * fade
    f = np.clip(flash + ring + rays * 0.6 * fade, 0, 1)
    v = artkit.ordered_dither(f, 10)
    rgb = np.array((190, 226, 255.0))[None, None] * v[..., None] + np.array((60, 30, 0.0)) * np.clip(f - 0.8, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


# =========================================================================== build

def _job(job):
    kind, a = job
    if kind == "sub":
        return sub_frame(a)
    if kind == "buoy":
        return buoy_frame(a)
    return slab(*a)


def build():
    jobs = [("sub", s) for s in range(3)] + [("buoy", s) for s in range(3)] + \
        [("slab", (s, f)) for s in range(4) for f in range(8)]
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(_job, jobs))
    kit.write("recon-sub", artkit.quantize_set(frames[0:3], 40), SOURCE)
    kit.write("recon-sub-glow", [sub_glow(k) for k in range(8)], SOURCE)
    kit.write("service-buoy", artkit.quantize_set(frames[3:6], 32), SOURCE)
    kit.write("service-buoy-glow", [p25.buoy_glow(k) for k in range(4)], SOURCE)
    kit.write("ice-slab", artkit.quantize_set(frames[6:], 24), SOURCE)
    kit.write("ice-shatter", [shatter(i) for i in range(10)], SOURCE)


# =========================================================================== review

FPS = 12
SEGMENTS = [(58.8, 3.6), (87.4, 2.4), (113.6, 4.6)]     # (level t, seconds): Pelagic-3; the buoy; the breakout
SUB_FIRE, SUB_OPEN = 59.8, 60.6
BUOY_OPEN = 88.4


def review():
    import backdrop_l28 as b28                                  # noqa: E402  (the floor, the pieces)
    sub, sg = kit.load("recon-sub"), kit.load("recon-sub-glow")
    buoy, bg = kit.load("service-buoy"), kit.load("service-buoy-glow")
    slabs, burst = kit.load("ice-slab"), kit.load("ice-shatter")
    crate = artkit.load_frames("pickup-crate")
    patch = artkit.load_frames("pickup-armour-patch") or crate
    bolt = artkit.load_frames("pulse-bolt")
    rows = [("PELAGIC-3: SEALED, HIT, OPENED (2X)", sub, 2, False),
            ("HER BEACON (0-3), THE RESIN'S TEAL AND SPARKS AFTER THE HIT (4-7), ADDITIVE", sg, 2, True),
            ("THE SERVICE BUOY: SHUT, OPENING, EMPTY; ITS STROBE (2X)", buoy + bg, 2, False),
            ("ICE SLABS 1-2: THE TUMBLE (8 FRAMES EACH)", slabs[:16], 1, False),
            ("ICE SLABS 3-4", slabs[16:], 1, False),
            ("THE CEILING'S BURST (ADDITIVE, 1/2), EVERY FRAME", [f.resize((160, 160), Image.BOX) for f in burst], 1,
             True)]
    sheet = artkit.review_sheet("LEVEL 28 PROPS (FINAL)", rows, width=1000,
                                batch=BATCH)
    g, hgh = b28.placed_pieces("ground"), b28.placed_pieces("high-air")
    st, sx = (kit.targets_of("recon-sub") or [(58.0, 90.0)])[0]
    halo = l26_halo()
    rng = np.random.default_rng(2899)
    flights = [(rng.uniform(0, TAU), rng.uniform(140, 260), int(rng.integers(0, 4)), rng.uniform(-6, 6),
                rng.uniform(0.7, 1.3)) for _ in range(9)]
    frames, shots = [], []
    clock = [(t0 + k / FPS) for t0, d in SEGMENTS for k in range(int(d * FPS))]
    for fi, t in enumerate(clock):
        ship = (sx + 6, 444.0) if t < 80 else (240.0, 444.0)
        img, glows, over = b28.ground_view(t, g, hgh)
        base = kit.scroll(t)
        solids, adds = [], []
        if t < 80:
            y = base + kit.SCREEN - kit.target_centre(st, SUB[1])
            state = 0 if t < SUB_FIRE + 0.3 else (1 if t < SUB_OPEN else 2)
            solids.append((sub[state], sx, y))
            glows.append((sg[int(t * 4) % 4] if state == 0 else sg[4 + int(t * 8) % 4], sx, y, 1.0))
            if t >= SUB_OPEN:
                u = min(1.0, (t - SUB_OPEN) / 0.8)
                hx, hy = hatch_px()
                cy = y + hy - SUB[1] / 2 - 18 * u
                glows.append((halo, sx + hx - SUB[0] / 2, cy, 1.0))
                over.append((crate[int(t * 10) % len(crate)], sx + hx - SUB[0] / 2, cy))
            if SUB_FIRE <= t < SUB_OPEN and fi % 2 == 0:
                for dx in (-9, 9):
                    shots.append([ship[0] + dx, ship[1] - 26.0])
        elif t < 100:
            bx, by = kit.BUOY[1], kit.SCREEN - kit.BUOY[2]
            state = 0 if t < BUOY_OPEN else (1 if t < BUOY_OPEN + 0.4 else 2)
            over.append((buoy[state], bx, by))
            glows.append((bg[int(t * 4) % 4], bx, by, 1.0))
            if t >= BUOY_OPEN:
                u = min(1.0, (t - BUOY_OPEN) / 1.0)
                glows.append((halo, bx + 6, by - 4 - 26 * u, 1.0))
                over.append((patch[int(t * 10) % len(patch)], bx + 6, by - 4 - 26 * u))
        else:
            dt = t - kit.BREAK_T
            if 0 <= dt < 10 / 20:
                adds.append((burst[min(9, int(dt * 20))], 240.0, 444.0 - 40, 1.0))
            if dt >= 0:
                for a, v, s, spin, grow in flights:
                    d = v * dt
                    x, y = 240 + math.cos(a) * (30 + d), 404 + math.sin(a) * (30 + d) * 0.8 - 20 * dt
                    if -60 < x < 540 and -60 < y < 600:
                        k = 8 * s + int(dt * 10 * grow + spin) % 8
                        im = slabs[k].resize((int(96 * (0.6 + 0.4 * grow + 0.3 * dt)),) * 2, Image.BOX)
                        over.append((im.rotate(spin * 30 * dt + a * 57, expand=False), x, y))
        for s in shots:
            s[1] -= 520 * 0.7 / FPS
        shots = [s for s in shots if s[1] > -20]
        for s in shots:
            glows.append((bolt[0], s[0], s[1], 1.0))
        pools = []
        for srow, x, r, col, note in b28.light_rows():
            yy = base - 1 - srow
            if -r < yy < kit.SCREEN + r:
                pools.append((x, yy, r, 1.0, kit._rgb(col)))
        out = kit.frame(img, t, solids=solids, glows=glows, ship_xy=ship, pools=pools, over=over, adds=adds)
        raster.draw_text(out, 8, 8, "L28 PROPS  T={:.1f}".format(t), raster.LABEL)
        frames.append(out.convert("RGB"))

    def frame_at(lt):
        return frames[min(range(len(clock)), key=lambda i: abs(clock[i] - lt))]
    stills = [(59.4, "PELAGIC-3 UNDER THE RACK"), (61.8, "OPENED, HER CRATE"), (89.4, "THE BUOY'S PATCH"),
              (115.3, "THE BREAKOUT")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 540), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2 ON LEVEL 28'S BACKDROP UNDER THE HATCHERY LIGHT'S STAND-IN", raster.LABEL)
    for i, (tt, cap) in enumerate(stills):
        big.alpha_composite(frame_at(tt).convert("RGBA").resize((240, 270), Image.Resampling.BOX),
                            (16 + i * 248, sheet.height + 14))
        raster.draw_text(big, 16 + i * 248, sheet.height + 288, cap, raster.LABEL_DIM)
    y1 = sheet.height + 304
    raster.draw_text(big, 16, y1, "1:1 CROPS: PELAGIC-3 UNDER THE RACK / OPENED / THE BURST AND THE SLABS", raster.LABEL)
    big.alpha_composite(frame_at(59.4).crop((0, 40, 300, 250)).convert("RGBA"), (16, y1 + 14))
    big.alpha_composite(frame_at(115.4).crop((90, 230, 390, 440)).convert("RGBA"), (16 + 308, y1 + 14))
    big.alpha_composite(frame_at(61.8).crop((0, 180, 300, 390)).convert("RGBA"), (16 + 616, y1 + 14))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "l28-props", fps=FPS)


def l26_halo():
    import l26kit                                               # noqa: E402  (the pickups' gold halo)
    return l26kit.gold_halo()


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
