#!/usr/bin/env python3
"""Production art: the Glassfin, Act 4's tiny school popcorn (design/enemies/naval/glassfin; M8 part C batch, concept
round 52, step ART-C). From the chosen round-48 look **a** "needle fry" (tools/concept/enemies_r48.py ``glassfin`` a,
role colours ``glassfin-a`` = plum / teal / teal, imported unchanged): a thin needle of clear Vrell glass, a glowing
teal spine (the contact hue), two dark eye dots, a fan tail.

Headings as tools/art/README.md: heading k turns k x 22.5 degrees clockwise from straight down (`16 angles`), each
its own render under the fixed key light. Size from the stat block: 16x16 (hitbox 10x10).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``glassfin``
once design/enemies/naval/glassfin/data.yaml exists, first_level 23):
  glassfin_0..31          16x16, normal: heading x 2 + (0 swimming, 1 the glint: the glass flashing teal-white,
                          the ram's 0.3 s tell)
  glassfin-glint_0..3     24x24, additive, centred on the fry: the tell's sparkle (a four-point star turning and
                          swelling, 0.3 s: 5 steps a frame, then the dive)
  glassfin-death_0..7     32x32, additive: the `tiny` glass shatter's grey-teal flash (3 steps a frame)
  glassfin-shards_0..7    32x32, normal with stepped translucency: four glass slivers flung slowly out, turning
                          and fading (no bubbles), drawn over the flash, 3 steps a frame
  design/enemies/naval/concept/glassfin-final-r52-a.png/.gif   the review sheet and GIF (two ribbons of 14 swaying
                          over Thera Deep's farms under the review grade, fry glinting and diving down the ship's
                          lane at 230 px/s, shatters)

The dark levels' spine-only look (L26: "in the dark only the spine shows") comes with that level's part.

Run: python3 tools/art/glassfin.py [--review]   (~30 s; --review only rebuilds the review files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
import l23kit as kit
from artkit import DESIGN, TAU

import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look a's model)
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "glassfin.py"
BATCH = "M8 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r52"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
HEADINGS = 16
SIZE, EXTENT = 16, 2.1
GLINT, DEATH = 24, 32
GLASS = np.array((176, 236, 232), float)
TEAL = np.array(e48.TEAL, float)


def turned(scene, mats, h):
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def _frame(job):
    k, glint = job
    scene, mats = turned(*e48.glassfin("a", glint), TAU * k / HEADINGS)
    return artkit.native(*artkit.render_hi(scene, mats, (SIZE, SIZE), EXTENT), crisp=60)


def _light(w, field, col):
    """An analytic light field (0..1) of colour ``col`` -> an additive sprite, stepped to a few levels."""
    q = artkit.ordered_dither(np.clip(field, 0, 1), 7)
    rgb = np.asarray(col, float)[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def glint_frame(i):
    w = GLINT
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    c = w / 2
    x, y = xx - c, yy - c
    a = 0.3 * i
    s = [0.6, 1.0, 1.2, 0.8][i]
    xr, yr = x * np.cos(a) - y * np.sin(a), x * np.sin(a) + y * np.cos(a)
    star = np.exp(-np.abs(xr) / 0.7) * np.exp(-np.abs(yr) / (3.5 * s)) + np.exp(-np.abs(yr) / 0.7) * np.exp(
        -np.abs(xr) / (3.5 * s))
    core = np.exp(-(np.hypot(x, y) / (1.3 * s)) ** 2)
    f = np.clip(star * 0.9 + core, 0, 1) * [0.7, 1.0, 1.0, 0.75][i]
    return _light(w, f, np.clip(GLASS * 0.5 + TEAL * 0.5 + 40, 0, 255))


def death_frame(i):
    w = DEATH
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - w / 2, yy - w / 2)
    u = i / 7
    f = np.exp(-(r / (2.5 + 7 * u)) ** 2) * (1 - u) ** 1.4 * 1.2 + np.exp(-((r - 3 - 9 * u) / 1.4) ** 2) * 0.35 * (1 - u)
    col = GLASS * (1 - u) + np.array((90, 170, 165)) * u
    return _light(w, f, col)


SHARDS = [(0.6, 1.0, 0.0), (2.2, 0.85, 1.1), (3.7, 0.95, 2.3), (5.2, 0.8, 0.7)]


def shards_frame(i):
    """Four slivers of glass flung out slowly, turning, fading out (stepped translucency)."""
    w, ss = DEATH, 4
    n = w * ss
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float64) / ss
    u = i / 7
    acc = np.zeros((n, n))
    shade = np.zeros((n, n))
    for a, reach, spin in SHARDS:
        d = 2 + 11 * reach * (1 - (1 - u) ** 1.7)
        cx, cy = w / 2 + np.cos(a) * d, w / 2 + np.sin(a) * d
        t = spin + 2.4 * u * (1 if spin > 1 else -1)
        ux, uy = np.cos(t), np.sin(t)
        px, py = xx - cx, yy - cy
        along, across = px * ux + py * uy, -px * uy + py * ux
        m = (np.abs(along) < 2.6) & (np.abs(across) < 0.75 * (1 - np.abs(along) / 3.2))
        acc = np.maximum(acc, m.astype(float))
        shade = np.where(m, 0.75 + 0.25 * np.sign(across + 0.1), shade)
    a = acc.reshape(w, ss, w, ss).mean(axis=(1, 3))
    sh = shade.reshape(w, ss, w, ss).max(axis=(1, 3))
    alpha = np.floor(np.clip(a * 1.6, 0, 1) * (1 - u ** 1.5) * 3 + 0.5) / 3
    rgb = GLASS[None, None] * sh[..., None]
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def intel_model():
    """(scene, mats) for the hangar intel portrait: three fry of the school, nose down, the middle one glinting."""
    from render import sdf
    fry, mats = turned(*e48.glassfin("a", 0.5), 0.0)
    parts = [((-0.55, 0.45), 0.62), ((0.5, 0.3), 0.62), ((0.0, -0.35), 0.75)]

    def scene(p):
        out = []
        for (x, y), k in parts:
            d, m = fry((p - np.array([x, y, 0.0])) / k)
            out.append((d * k, m))
        return sdf.union(*out)
    return scene, mats


def build():
    jobs = [(k, g) for k in range(HEADINGS) for g in (0.0, 1.0)]
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(_frame, jobs))
    kit.write("glassfin", artkit.quantize_set(frames, 20), SOURCE)
    kit.write("glassfin-glint", [glint_frame(i) for i in range(4)], SOURCE)
    kit.write("glassfin-death", [death_frame(i) for i in range(8)], SOURCE)
    kit.write("glassfin-shards", artkit.quantize_set([shards_frame(i) for i in range(8)], 8), SOURCE)


# =========================================================================== review

R1 = rig.catmull_rom([(100, -40), (190, 90), (320, 190), (300, 280), (130, 340), (160, 470), (380, 600)],
                     [0, 1.0, 2.0, 2.8, 3.9, 5.0, 6.4])
R2 = rig.catmull_rom([(420, -40), (330, 90), (170, 190), (200, 290), (390, 360), (330, 480), (90, 610)],
                     [0, 1.0, 2.0, 2.8, 3.9, 5.0, 6.4])
SCHOOLS = [(R1, 0.0, 14), (R2, 3.4, 14)]
RAMS = {(0, 4): 3.0, (0, 9): 3.6, (1, 6): 6.6}          # (school, fry): the glint's start
KILLS = {(0, 1): 2.0, (0, 12): 3.3, (1, 2): 5.2, (1, 11): 6.1, (1, 13): 7.0}
DUR, FPS = 10.0, 20
SHIP = (240.0, 488.0)


def fry_pos(school, i, t):
    path, t0, n = SCHOOLS[school]
    u = t - t0 - 0.13 * i
    x, y = path(u)
    h = rig.path_heading(path, u)
    sw = 14 * np.sin(TAU * 1.4 * u + i * 0.5)
    return np.array([x - np.sin(h) * sw * 0.6, y + np.cos(h) * sw * 0.6]), h, u


def review():
    gf, gl, dth, sh = kit.load("glassfin"), kit.load("glassfin-glint"), kit.load("glassfin-death"), kit.load(
        "glassfin-shards")
    rows = [("16 HEADINGS, SWIMMING", gf[0::2], 3, False),
            ("16 HEADINGS, THE GLINT (RAM TELL)", gf[1::2], 3, False),
            ("GLINT SPARKLE (ADDITIVE)", gl, 3, True),
            ("SHATTER FLASH (ADDITIVE)", dth, 2, True),
            ("SHARDS (NORMAL, STEPPED)", sh, 2, False)]
    sheet = artkit.review_sheet("GLASSFIN - FINAL (ROUND 48 LOOK A, NEEDLE FRY)", rows, width=1000, batch=BATCH)
    frames = []
    dives = {}
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        img = kit.plate(480, 540, 0, 900)
        glow = []
        for sc, (path, t0, n) in enumerate(SCHOOLS):
            for i in range(n):
                key = (sc, i)
                p, h, u = fry_pos(sc, i, t)
                if u < 0 or u > 6.4:
                    continue
                if key in RAMS and t >= RAMS[key]:
                    if key not in dives:
                        dives[key] = p.copy()
                    t_d = t - RAMS[key] - 0.3 - 0.5
                    if t_d > 0:               # 0.3 s glint, 0.5 s on, then straight down the ship's lane
                        start = dives[key]
                        aim = np.array([SHIP[0] - start[0], 1.0])
                        dx = aim[0] * min(1, t_d / 0.4)
                        p = start + np.array([dx, 230 * 0.7 * t_d])
                        k = kit.heading_index(aim[0] / 0.4 * 0.2, 230 * 0.7)
                    else:
                        p = dives[key] + np.array([0, 40 * (t - RAMS[key])])
                        k = kit.heading_index(np.cos(h), np.sin(h))
                    glinting = t - RAMS[key] < 0.3
                    kit.paste_c(img, gf[k * 2 + int(glinting)], *p)
                    if glinting:
                        glow.append((gl[min(3, int((t - RAMS[key]) / 0.075))], p))
                    continue
                if key in KILLS and t >= KILLS[key]:
                    s = int((t - KILLS[key]) * 20)
                    if s < 8:
                        glow.append((dth[s], p))
                        kit.paste_c(img, sh[s], *p)
                    continue
                k = kit.heading_index(np.cos(h), np.sin(h))
                kit.paste_c(img, gf[k * 2], *p)
        kit.paste_c(img, kit.ship(2), *SHIP)
        for g, p in glow:
            img = kit.add_c(img, g, *p)
        raster.draw_text(img, 8, 8, "GLASSFIN A - T={:.1f}".format(t), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * FPS)] for t in (2.4, 3.3, 4.2)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE RIBBON SWAYING (T=2.4), A GLINT (T=3.3), THE RAM DOWN THE "
                                            "SHIP'S LANE (T=4.2); REVIEW GRADE", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    crop = frames[int(3.35 * FPS)].crop((120, 120, 360, 390)).resize((240, 270), Image.Resampling.NEAREST)
    big.alpha_composite(crop.convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames[::2]], CONCEPT, "glassfin",
                       fps=FPS // 2)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
