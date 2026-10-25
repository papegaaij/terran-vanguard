#!/usr/bin/env python3
"""Production art: the relay beacon as Level 27's secondary target (design/enemies/ground/relay-beacon, gap check m8g
§1.4; design/campaign/act-4-deep-water/level-27-hydrothermal-vents "Ground targets"; M8 part G batch, concept round 57,
step ART-G). The same human-built mast as Levels 24-26's scenery beacons (tools/art/l24_props.py ``beacon_scene``: the
grey mast with its amber hazard bands, the head with its cross-arm and relay pods, the red-gold lamp, three guy cables;
its materials and the 4 s pulse imported unchanged), here as a ground unit of its own: the scene is rebuilt at the
sprite's canvas (the mast leaning toward the camera's top as the pieces do), plus its broken state.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``relay-beacon``
once design/enemies/ground/relay-beacon/data.yaml exists, first_level 27):
  relay-beacon_0..15        72x104, normal: the mast on the ridge, its lamp lens tinted by the pulse (dim red at rest,
                            gold at the flash; l24_props.PULSE, 16 frames at 4 fps = the README's pulse every 4 s); the
                            sprite's centre is the unit's (the mast's middle): the foot 22 px below it, the lamp 38 px
                            above (assets/pivots/relay-beacon.json); data size [72, 104], hitbox proposed [36, 80]
  relay-beacon-glow_0..15   72x104, additive, the same clock: the lamp's core and halo and its light on the rock round
                            the foot (the pieces' glow at this canvas), drawn ungraded over the mast
  relay-beacon-dead         72x104, normal: the destroyed beacon (the dark frame): the mast snapped at its lower band,
                            the head and cross-arm lying across the rock, the lens dark, one guy cable whipped loose,
                            scorch round the break
  relay-beacon-break_0..7   64x64, additive, centred on the lamp (pivot ``lamp``): the break's flash, the lamp's
                            red-gold burst and sparks scattering and dying (12 fps; with explosion-deep-small)
  assets/pivots/relay-beacon.json   the lamp and the foot in the frame's px (for the break, the light pool, the dead
                            frame's place)
  design/enemies/ground/concept/relay-beacon-final-r57-a.png/.gif   the review sheet and GIF (on Level 27's ridge under
                            the review's vent light, section 4: the four masts pulsing, two shot down, their breaks and
                            dark frames)

Run: python3 tools/art/relay_beacon.py [--review]   (~30 s; --review only rebuilds the review; backdrop_l27.py first
for the ridge's floor)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
import l24_props as p24
import l27kit as kit
from artkit import DESIGN, TAU

from render import raster, sdf  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "relay_beacon.py"
BATCH = "M8 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r57"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "ground" / "concept"
SIZE = (72, 104)
FOOT = (36, 74)                      # the mast's foot on the rock (px in the frame)
LAMP = (36, 14)                      # the lamp (px)
BREAK = 64
FPS = 4
PULSE = p24.PULSE
RED, GOLD = p24.RED, p24.GOLD


def _model(px, py):
    return px - SIZE[0] / 2, SIZE[1] / 2 - py


def mats(dead=False):
    m = p24.beacon_mats()
    if dead:
        scorch = (lambda p, n: np.where(np.hypot(p[:, 0] - _model(*FOOT)[0] - 3, p[:, 1] - _model(*FOOT)[1] - 4)
                                        < 13, 0.35, 1.0))
        m[0] = Material((0.46, 0.48, 0.52), metal=0.6, shininess=60, spec=0.7, pattern=scorch)
        m[2] = Material((0.62, 0.6, 0.55), metal=0.1, shininess=15, spec=0.2, pattern=scorch)
        m[3] = Material((0.08, 0.05, 0.05), metal=0.2, shininess=60, spec=0.6)     # the lens dark
    return m


def scene(dead=False):
    """l24_props.beacon_scene at this canvas; ``dead``: snapped at the lower band, the head lying on the rock."""
    bx, by = _model(*FOOT)
    lx, ly = _model(*LAMP)
    top = 46.0
    lean = ly - by

    def along(u):
        return (bx, by + lean * u, top * u)

    def sc(p):
        items = [(sd_cylinder_z(p, (bx, by, 1.2), 9.0, 1.2), 2)]
        if not dead:
            items.append((sd_capsule(p, along(0.0), along(1.0), 3.0, 2.0), 0))
            for u in (0.3, 0.62):
                items.append((sd_capsule(p, along(u - 0.03), along(u + 0.03), 3.3, 3.1), 4))
            hx, hy, hz = along(1.0)
            items.append((sd_box(p, (hx, hy, hz), (5.2, 4.2, 2.8), 0.7), 0))
            items.append((sd_capsule(p, (hx - 12, hy, hz - 0.5), (hx + 12, hy, hz - 0.5), 1.0), 1))
            for s in (-1, 1):
                items.append((sd_box(p, (hx + s * 12, hy, hz - 0.5), (1.6, 1.6, 1.6), 0.4), 0))
            items.append((sd_sphere(p, (hx, hy + 0.6, hz + 3.0), 2.6), 3))
            items.append((sd_capsule(p, (hx + 2.5, hy - 1, hz + 2), (hx + 3.5, hy - 1 + lean * 0.18, hz + 9), 0.45), 1))
            ax, ay, az = along(0.66)
            anchors = range(3)
        else:
            sx, sy, sz = along(0.3)
            items.append((sd_capsule(p, along(0.0), (sx + 0.6, sy, sz), 3.0, 2.6), 0))   # the stump, snapped
            items.append((sd_capsule(p, along(0.27), (sx + 0.6, sy, sz), 3.3, 3.1), 4))
            # the upper mast fallen across the rock toward the lower right, the head at its end
            f0 = np.array((bx + 3, by - 2, 2.4))
            d = np.array((0.55, -0.83, 0.0))
            f1 = f0 + d * 21
            items.append((sd_capsule(p, tuple(f0), tuple(f1), 2.4, 1.9), 0))
            items.append((sd_capsule(p, tuple(f0 + d * 9), tuple(f0 + d * 11), 2.7, 2.6), 4))
            hx, hy, hz = f1 + d * 3
            items.append((sd_box(p, (hx, hy, 2.6), (4.6, 4.0, 2.4), 0.7), 0))
            items.append((sd_capsule(p, (hx - 9, hy - 7, 1.4), (hx + 7, hy + 9, 1.0), 1.0), 1))   # the arm, askew
            items.append((sd_sphere(p, (hx + 3, hy - 2, 4.4), 2.4), 3))
            ax, ay, az = sx, sy, sz
            anchors = (0, 1)
        for k in anchors:
            a = k * TAU / 3 + 0.9
            items.append((sd_capsule(p, (ax, ay, az), (bx + 26 * np.cos(a), by + 26 * np.sin(a), 0.6), 0.35), 1))
            items.append((sd_box(p, (bx + 26 * np.cos(a), by + 26 * np.sin(a), 0.8), (1.6, 1.6, 0.8), 0.3), 1))
        if dead:                                              # the third anchor's cable whipped loose on the rock
            a = 2 * TAU / 3 + 0.9
            e = (bx + 26 * np.cos(a), by + 26 * np.sin(a), 0.6)
            items.append((sd_box(p, (e[0], e[1], 0.8), (1.6, 1.6, 0.8), 0.3), 1))
            items.append((sd_capsule(p, e, (e[0] - 10, e[1] + 9, 0.5), 0.35), 1))
        return union(*items, k=0.4)
    return sc


def _render(dead):
    return p24.rimmed(p24.render(scene(dead), mats(dead), SIZE, z_top=60))


def body(base, level):
    """The lens tinted by the pulse level (l24_props.beacon_body's rule at this canvas)."""
    a = np.array(base).astype(np.float64)
    yy, xx = np.mgrid[0:SIZE[1], 0:SIZE[0]].astype(np.float64) + 0.5
    r = np.hypot(xx - LAMP[0], yy - (LAMP[1] - 1.5))
    lens = (r < 2.6) & (a[..., 3] > 0)
    col = RED * (1 - level) + GOLD * level
    a[..., :3] = np.where(lens[..., None], col * (0.55 + 0.45 * level) + 30 * level, a[..., :3])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def glow(k):
    """l24_props.beacon_glow at this canvas: the lamp's core and halo, its light round the foot."""
    w, h = SIZE
    level = PULSE[k % len(PULSE)]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - LAMP[0], yy - (LAMP[1] - 1.5))
    core = np.exp(-(r / 1.6) ** 2)
    halo = np.exp(-(r / (4 + 7 * level)) ** 2) * (0.25 + 0.5 * level)
    rf = np.hypot(xx - FOOT[0], (yy - FOOT[1]) / 0.8)
    pool = np.exp(-(rf / (12 + 16 * level)) ** 2) * 0.3 * level ** 1.5
    col = RED * (1 - level) + GOLD * level
    v = np.clip(core * (0.55 + 0.45 * level) + halo + pool, 0, 1)
    q = artkit.ordered_dither(v, 9)
    rgb = col[None, None] * q[..., None] + 90 * np.clip(core * level - 0.6, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def break_frame(i, n=8):
    """The lamp's burst: a red-gold flash (frames 0-2), sparks flying out and slowing in the water, dying."""
    w = BREAK
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    r = np.hypot(x, y)
    flash = np.exp(-(r / (3 + 16 * u)) ** 2) * max(0.0, 1 - 2.2 * u) * 1.2
    ring = np.exp(-((r - (6 + 22 * u ** 0.6)) / 1.6) ** 2) * max(0.0, 0.6 - 0.8 * u)
    f = flash + ring
    rng = np.random.default_rng(2741)
    sparks = np.zeros((w, w))
    for _ in range(14):
        a, v = rng.uniform(0, TAU), rng.uniform(14, 28)
        d = v * (1 - (1 - u) ** 2)
        px, py = w / 2 + np.cos(a) * d, w / 2 + np.sin(a) * d
        k = max(0.0, 1 - u * rng.uniform(1.0, 1.4))
        sparks = np.maximum(sparks, np.exp(-((xx - px) ** 2 + (yy - py) ** 2) / 0.9) * k)
    f = np.clip(f + sparks, 0, 1)
    q = artkit.ordered_dither(f, 8)
    col = RED * u + GOLD * (1 - u)
    rgb = col[None, None] * q[..., None] + 120 * np.clip(flash - 0.7, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def intel_hi():
    """The hangar intel portrait's 8x render (an RGBA array, intel.py's HI): the lit mast at the portrait's scale."""
    n = 30 * 8
    hi = sdf.render(scene(False), mats(False), (n, n), 86.0, center=(0.0, 4.0), z_top=60.0)
    return hi


def _job(job):
    return _render(job == "dead")


def build():
    with ProcessPoolExecutor() as ex:
        live, dead = ex.map(_job, ["live", "dead"])
    frames = artkit.quantize_set([body(live, PULSE[k]) for k in range(len(PULSE))] + [dead], 32)
    kit.write("relay-beacon", frames[:-1], SOURCE)
    kit.write("relay-beacon-dead", [frames[-1]], SOURCE, single=True)
    kit.write("relay-beacon-glow", [glow(k) for k in range(len(PULSE))], SOURCE)
    kit.write("relay-beacon-break", [break_frame(i) for i in range(8)], SOURCE)
    artkit.write_pivots("relay-beacon", SOURCE, {"frame": list(SIZE), "lamp": list(LAMP), "foot": list(FOOT)})


# =========================================================================== review

DUR, GIF_FPS = 8.0, 10
KILLS = {1: 3.2, 2: 5.6}             # beacon index -> kill time in the GIF


def review():
    bf, gl, dead, brk = (kit.load(n) for n in ("relay-beacon", "relay-beacon-glow", "relay-beacon-dead",
                                               "relay-beacon-break"))
    rows = [("THE 4 S PULSE, EVERY 2ND FRAME (THE LENS RED AT REST, GOLD AT THE FLASH)", bf[::2], 2, False),
            ("ITS LIGHT (ADDITIVE), EVERY 2ND FRAME", gl[::2], 2, True),
            ("DESTROYED: THE DARK FRAME", dead, 2, False),
            ("THE BREAK (ADDITIVE, 12 FPS, ON THE LAMP)", brk, 2, True),
            ("IN LEVEL 27'S VENT LIGHT (SECTION 4, 0.45 BLUE): REST, FLASH, DEAD", [
                kit.lit_sprite(bf[8], 100), kit.add_c(kit.lit_sprite(bf[0], 100), gl[0], 36, 52), kit.lit_sprite(dead[0], 100)],
             2, False)]
    sheet = artkit.review_sheet("RELAY BEACON - FINAL (L24-26'S MAST AS LEVEL 27'S TARGET)", rows, width=1000,
                                batch=BATCH)
    t0 = 103.0
    spots = [(70, 330), (400, 210), (240, 120), (100, 420)]
    frames = []
    for fi in range(int(DUR * GIF_FPS)):
        t = fi / GIF_FPS
        ground = kit.floor_plate(t=t0)
        solids, glows = [], []
        for i, (x, y) in enumerate(spots):
            kt = KILLS.get(i)
            k = int(t * FPS) % 16
            if kt is not None and t >= kt:
                solids.append((dead[0], x, y))
                s = int((t - kt) * 12)
                if s < len(brk):
                    glows.append((brk[s], x + LAMP[0] - SIZE[0] / 2, y + LAMP[1] - SIZE[1] / 2, 1.0))
                continue
            solids.append((bf[k], x, y))
            glows.append((gl[k], x, y, 1.0))
        img = kit.frame(ground, t0 + t * 0.0, solids=solids, glows=glows, ship_xy=(240 + 60 * np.sin(t * 0.6), 470),
                        plumes=False)
        raster.draw_text(img, 8, 8, "RELAY BEACONS - T={:.1f}".format(t), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * GIF_FPS)] for t in (1.0, 3.4, 6.5)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE RIDGE'S MASTS PULSING (T=1), ONE BREAKING (T=3.4), TWO DARK "
                     "(T=6.5); 1X CROP OF A MAST", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    big.alpha_composite(frames[10].crop((10, 240, 250, 510)).convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((240, 270), Image.Resampling.BOX) for f in frames], CONCEPT, "relay-beacon",
                       fps=GIF_FPS)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
