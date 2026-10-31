#!/usr/bin/env python3
"""Production art: the Vrell siphon pump, Level 23's secondary target on Thera Deep's farm racks
(design/enemies/ground/siphon-pump, promoted by part C's gap check D3; M8 part C batch, concept round 52, step
ART-C). Straight to production from the round-48 Thera Deep mood still (``europa-thera-deep-r48-a``: "purple chitin,
a teal mouth, teal roots spreading into the kelp", tools/concept/europa_r48.py ``thera_frame``) and the README's
"a teal pump tube with a glowing sump": a squat ribbed bellows of purple Vrell chitin, bone ridges, a flared neck
round a teal mouth with the glowing sump inside, three feeder hoses gripping the rack. `fixed` (one heading, lit
from the upper left); 40x40 (hitbox 32x32), a ground target on the sea floor (`layer: ground`).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims
``siphon-pump`` once design/enemies/ground/siphon-pump/data.yaml exists, first_level 23):
  siphon-pump_0..3          40x40, normal: the pump heaving (swell 0, 0.5, 1, 0.5; a 6 fps loop): the bellows
                            swelling, the neck rising, the sump's teal brightening at the top of the stroke
  siphon-pump-glow_0..3     40x40, additive, the same order: the sump's teal light and a soft bloom over the mouth
                            (drawn over the pump; the grade leaves glows bright)
  siphon-pump-dead          40x40, normal: the destroyed pump (remains): the bellows burst and slumped, the neck
                            broken off, the sump dark, scorched chitin
  siphon-pump-break_0..7    64x64, normal with stepped translucency: the break (with explosion-deep-small): the
                            teal sap gushing up out of the sump and spreading as a cloud, chitin chips flung, the
                            cloud thinning into the water (3 steps a frame)
  siphon-pump-roots         96x96, normal, stepped translucency (a ground decal under the pump, centred on it):
                            the feeder roots spreading into the rack's kelp, teal sap in their veins
  siphon-pump-roots-dead    96x96, the same roots withered grey-brown after the pump's death (replaces the decal)
  design/enemies/ground/concept/siphon-pump-final-r52-a.png/.gif   the review sheet and GIF (on Thera Deep's farm
                            floor under the review grade: a rack with two pumps heaving, shots taking one, the break)

Run: python3 tools/art/siphon_pump.py [--review]   (~30 s; --review only rebuilds the review files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import l23kit as kit
from artkit import DESIGN, TAU

import enemies_r36 as e36  # noqa: E402  (concept script, unchanged: sd_torus_z)
from render import raster  # noqa: E402
from render.sdf import Material, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

SCRIPT = "siphon_pump.py"
BATCH = "M8 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r52"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "ground" / "concept"
SIZE, EXTENT = 40, 2.0                  # 20 px a model unit
BREAK, DECAL = 64, 96
SWELL = (0.0, 0.5, 1.0, 0.5)
TEAL = np.array((0, 255, 154), float)
SAP = np.array((40, 220, 170), float)
HOSES = [0.35, 2.45, 4.35]              # the feeder hoses' directions (rad, screen x right, y down)


def mats(glow=1.0, dead=False):
    ribs = lambda p, n: 0.82 + 0.18 * np.cos(np.arctan2(p[:, 1], p[:, 0]) * 7) ** 2  # noqa: E731
    k = 0.45 if dead else 1.0
    return [Material((0.42 * k, 0.19 * k, 0.48 * k), metal=0.15, shininess=55, spec=0.7, pattern=ribs),  # 0 chitin
            Material((0.15 * k, 0.07 * k, 0.19 * k), metal=0.1, shininess=30, spec=0.4),                 # 1 dark plates
            Material((0.62 * k, 0.5 * k, 0.56 * k), metal=0.05, shininess=40, spec=0.5),                 # 2 bone ribs (mauve)
            Material((0.02, 0.06, 0.05), emission=tuple(np.array((0.0, 1.0, 0.6)) * (0.0 if dead else glow))),  # 3 sump
            Material((0.05, 0.12, 0.1), emission=tuple(np.array((0.0, 0.7, 0.45)) * (0.0 if dead else glow)))]  # 4 hose tips


def scene(swell=0.0, dead=False):
    """The pump (model units, 20 px each; z up): a ribbed bellows with seven bone ribs, a bone collar ring round
    the teal mouth over the sump, three chitin feeder hoses with teal-lit tips gripping the rack."""
    s = 1.0 + 0.1 * swell
    lift = 0.1 * swell

    def sc(p):
        items = []
        flat = 0.55 if dead else 1.0
        items.append((sd_ellipsoid(p, (0, 0, 0.24 * flat), (0.56 * s, 0.56 * s, (0.4 + lift) * flat)), 0))
        for i in range(7):                                        # bone ribs down the bellows
            a = i * TAU / 7 + 0.2
            c, sn = np.cos(a), np.sin(a)
            r0, r1 = 0.3 * s, 0.62 * s
            items.append((sd_capsule(p, (r0 * c, r0 * sn, (0.6 + lift) * flat), (r1 * c, r1 * sn, 0.1), 0.05, 0.075), 2))
        if not dead:
            items.append((sd_cylinder_z(p, (0, 0, 0.66 + lift), 0.25, 0.1), 1))          # the neck
            items.append((e36.sd_torus_z(p, (0, 0, 0.78 + lift), 0.24, 0.075), 2))       # the bone collar
            d, m = union(*items, k=0.05)
            d, m = subtract((d, m), sd_cylinder_z(p, (0, 0, 0.86 + lift), 0.19, 0.16))  # the mouth
            sump = sd_sphere(p, (0, 0, 0.6 + lift), 0.2)
            d2, m2 = union((d, m), (sump, 3))
        else:
            items.append((sd_cylinder_z(p, (0.06, -0.04, 0.42), 0.22, 0.06), 1))
            d2, m2 = union(*items, k=0.05)
            d2, m2 = subtract((d2, m2), sd_capsule(p, (-0.5, 0.3, 0.42), (0.4, -0.2, 0.44), 0.08))
        hoses = []
        for a in HOSES:
            c, sn = np.cos(a), -np.sin(a)                        # model y up = screen up
            hoses.append((sd_capsule(p, (0.42 * c, 0.42 * sn, 0.14), (0.86 * c, 0.86 * sn, 0.06), 0.12, 0.08), 1))
            hoses.append((sd_sphere(p, (0.9 * c, 0.9 * sn, 0.06), 0.085), 4))
        return union((d2, m2), *hoses, k=0.05)
    return sc


def _frame(job):
    kind, a = job
    if kind == "live":
        hi, f = artkit.render_hi(scene(SWELL[a]), mats(0.7 + 0.5 * SWELL[a]), (SIZE, SIZE), EXTENT)
        return artkit.native(hi, f)
    if kind == "glow":
        m = [Material((0, 0, 0), spec=0.0) for _ in range(5)]
        m[3] = mats(1.0 + 0.6 * SWELL[a])[3]
        m[4] = mats(1.0 + 0.6 * SWELL[a])[4]
        hi, f = artkit.render_hi(scene(SWELL[a]), m, (SIZE, SIZE), EXTENT, shadows=False)
        return glow_frame(hi, f, SWELL[a])
    hi, f = artkit.render_hi(scene(dead=True), mats(dead=True), (SIZE, SIZE), EXTENT)
    return artkit.native(hi, f)


def glow_frame(hi, factor, swell):
    from render import sprite
    lo = sprite.downsample(hi, factor)
    em = lo[..., :3] * lo[..., 3:4]
    lum = np.clip(em.max(axis=-1), 0, 1)
    img = Image.fromarray((lum * 255).astype(np.uint8))
    halo = np.asarray(img.filter(ImageFilter.GaussianBlur(3.0)), float) / 255
    core = np.asarray(img.filter(ImageFilter.GaussianBlur(0.6)), float) / 255
    k = np.clip(core * 0.8 + halo * 1.6, 0, 1) * (0.55 + 0.45 * swell)
    q = artkit.smooth_steps(k)
    rgb = TEAL[None, None] * q[..., None] + 120 * np.clip(core - 0.6, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full(k.shape, 255.0)]).astype(np.uint8)))


# --------------------------------------------------------------------------- 2D parts

def roots(dead=False, seed=2301):
    """Feeder roots from the hoses' ends, branching into the kelp (drawn at 4x, reduced)."""
    ss = 4
    w = DECAL * ss
    img = Image.new("RGBA", (w, w))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    c = w / 2
    body = (60, 40, 46) if dead else (70, 30, 80)
    vein = (96, 90, 70) if dead else (40, 220, 170)

    def grow(x, y, a, length, width, depth):
        pts = [(x, y)]
        for _ in range(int(length)):
            a += rng.normal(0, 0.18)
            x += np.cos(a) * 3 * ss
            y += np.sin(a) * 3 * ss
            pts.append((x, y))
            if np.hypot(x - c, y - c) > w / 2 - 4 * ss:
                break
        d.line(pts, fill=body + (255,), width=int(width * ss))
        d.line(pts, fill=vein + (255,), width=max(1, int(width * ss * 0.35)))
        if depth < 3:
            for _ in range(2):
                i = int(rng.integers(len(pts) // 3, max(len(pts) // 3 + 1, len(pts) - 1)))
                grow(*pts[i], a + rng.choice([-1, 1]) * rng.uniform(0.5, 1.0), length * 0.55, width * 0.65, depth + 1)

    for a in HOSES:
        grow(c + np.cos(a) * 18 * ss, c + np.sin(a) * 18 * ss, a, 9, 3.2, 0)
    for k in range(3):
        a = HOSES[k] + 1.05
        grow(c + np.cos(a) * 12 * ss, c + np.sin(a) * 12 * ss, a, 6, 2.2, 1)
    d.ellipse([c - 15 * ss, c - 15 * ss, c + 15 * ss, c + 15 * ss], fill=body + (200,))
    small = img.resize((DECAL, DECAL), Image.Resampling.BOX)
    a = np.array(small).astype(np.float64)
    al = a[..., 3] / 255
    al = np.where(al > 0.75, 1.0, np.where(al > 0.3, 0.66, np.where(al > 0.08, 0.33, 0.0)))
    a[..., :3] = a[..., :3] / np.maximum(a[..., 3:4] / 255, 1e-3)
    a[..., 3] = al * 255
    out = np.clip(a, 0, 255).astype(np.uint8)
    out[al == 0] = 0
    return Image.fromarray(out, "RGBA")


def break_frame(i):
    """The teal sap gushing out of the sump and spreading, chips flung (stepped translucency)."""
    w = BREAK
    u = i / 7
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    c = w / 2
    rng = np.random.default_rng(2311)
    field = np.zeros((w, w))
    for k in range(9):
        a = rng.uniform(0, TAU)
        reach = rng.uniform(4, 22) * (1 - (1 - u) ** 2)
        bx, by = c + np.cos(a) * reach, c + np.sin(a) * reach - 6 * u
        r = rng.uniform(5, 9) * (0.6 + 0.8 * u)
        field = np.maximum(field, np.exp(-((xx - bx) ** 2 + (yy - by) ** 2) / (2 * r * r)))
    alpha = field * (1 - u ** 1.3) * 0.95
    shade = 0.75 + 0.25 * np.clip((c - xx + c - yy) / w, -1, 1)
    rgb = SAP[None, None] * shade[..., None] * (1 - 0.35 * u) + 30 * np.clip(field - 0.8, 0, 1)[..., None]
    q = artkit.smooth_steps(alpha)
    out = np.dstack([np.clip(rgb, 0, 255), q * 255]).astype(np.uint8)
    for k in range(6):                                            # chitin chips
        a = k * TAU / 6 + 0.4
        d = 4 + 24 * (1 - (1 - u) ** 1.5)
        x, y = int(c + np.cos(a) * d), int(c + np.sin(a) * d - 4 * u)
        if u < 0.9 and 1 <= x < w - 2 and 1 <= y < w - 2:
            out[y:y + 2, x:x + 2] = (80, 36, 92, 255)
            out[y, x] = (150, 96, 160, 255)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


def intel_model():
    """(scene, mats) for the hangar intel portrait (the pump at the top of its stroke)."""
    return scene(1.0), mats(1.6)


def build():
    jobs = [("live", i) for i in range(4)] + [("glow", i) for i in range(4)] + [("dead", 0)]
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_frame, jobs))
    body = artkit.quantize_set(out[:4] + out[8:], 36)
    kit.write("siphon-pump", body[:4], SOURCE)
    kit.write("siphon-pump-glow", out[4:8], SOURCE)
    kit.write("siphon-pump-dead", body[4:], SOURCE, single=True)
    kit.write("siphon-pump-break", artkit.quantize_set([break_frame(i) for i in range(8)], 16), SOURCE)
    kit.write("siphon-pump-roots", artkit.quantize_set([roots()], 20), SOURCE, single=True)
    kit.write("siphon-pump-roots-dead", artkit.quantize_set([roots(dead=True)], 16), SOURCE, single=True)


# =========================================================================== review

def review():
    pump, glow, dead = kit.load("siphon-pump"), kit.load("siphon-pump-glow"), kit.load("siphon-pump-dead")
    brk, rts, rtd = kit.load("siphon-pump-break"), kit.load("siphon-pump-roots"), kit.load("siphon-pump-roots-dead")
    try:
        burst = kit.load("explosion-deep-small")
    except FileNotFoundError:
        burst = artkit.load_frames("explosion-small")
    rows = [("HEAVE LOOP", pump, 4, False), ("SUMP GLOW (ADDITIVE)", glow, 4, True), ("DEAD", dead, 4, False),
            ("BREAK (NORMAL, STEPPED)", brk, 2, False), ("ROOTS DECAL, LIVE AND DEAD", rts + rtd, 1, False)]
    sheet = artkit.review_sheet("SIPHON PUMP - FINAL (THERA DEEP'S VRELL PUMPS)", rows, width=1000, batch=BATCH)
    frames = []
    pumps = [(150, 200, None), (330, 300, 2.2)]
    for fi in range(60):
        t = fi / 15
        img = kit.plate(480, 420, 60, 1300)
        glows = []
        for x, y, kill in pumps:
            if kill is not None and t >= kill:
                kit.paste_c(img, rtd[0], x, y)
                kit.paste_c(img, dead[0], x, y)
                s = int((t - kill) * 10)
                if s < 8:
                    kit.paste_c(img, brk[s], x, y - 4)
                if s < len(burst):
                    glows.append((burst[s], (x, y)))
                continue
            kit.paste_c(img, rts[0], x, y)
            k = int(t * 6) % 4
            kit.paste_c(img, pump[k], x, y)
            glows.append((glow[k], (x, y)))
        if 1.2 < t < 2.3:                                     # a few shots walking onto the second pump
            for j in range(3):
                yy = 420 - ((t - 1.2) * 600 + j * 60) % 140 - 140 + 40
                glows.append((None, (330, yy)))
        for g, p in glows:
            if g is None:
                d = ImageDraw.Draw(img)
                d.rectangle([p[0] - 1, p[1] - 6, p[0] + 1, p[1] + 6], fill=(200, 250, 255, 255))
            else:
                img = kit.add_c(img, g, *p)
        frames.append(img.convert("RGB"))
    big = Image.new("RGBA", (sheet.width, sheet.height + 230), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "ON THE FARM FLOOR AT 1X (REVIEW GRADE): HEAVING, THEN THE BREAK", raster.LABEL)
    for i, fi in enumerate((10, 36, 42, 50)):
        big.alpha_composite(frames[fi].convert("RGBA").crop((90, 120, 390, 330)).resize((225, 158)),
                            (16 + i * 240, sheet.height + 18))
    artkit.save_review(big, frames, CONCEPT, "siphon-pump", fps=15)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
