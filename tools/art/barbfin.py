#!/usr/bin/env python3
"""Production art: the Barbfin, Act 4's small needle spitter (design/enemies/naval/barbfin; M8 part D batch, concept
round 53, step ART-D). From the chosen round-48 look **a** "bone lionfish" (tools/concept/enemies_r48.py ``barbfin``
a, role colours ``barbfin-a`` = bone / violet / violet, imported unchanged): a stubby bone body, five bone fin rays a
side with dark webbing, a long barbed snout tipped violet (the weak point, drawn only), violet eyes, a fan tail.

Headings as tools/art/README.md: heading k turns k x 11.25 degrees clockwise from straight down (`32 angles`: it
turns toward the ship while it holds), each its own render under the fixed key light. Size from the stat block:
36x28 on a 40 px canvas (hitbox 26x20), the concept's scale.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``barbfin``
once design/enemies/naval/barbfin/data.yaml exists, first_level 24):
  barbfin_0..95         40x40, normal: heading x 3 + frame: 0 and 1 the swim (the fan tail left / right, 6 fps
                        while it moves, slower in the hold), 2 the shot's tell (the snout barb and eyes at full
                        violet glow: shown for the 0.25 s before each needle)
  barbfin-glow_0..31    40x40, additive: per heading the violet eyes and snout barb alone (the model's emission, a 1 px
                        stepped halo), drawn ungraded over every frame so the fish keeps its violet points in the
                        dark water (as the Abyss Ray's gills; alone in the dark levels)
  barbfin-snout_0..3    16x16, additive: the violet sparkle at the snout tip with the tell and the shot (2 steps a
                        frame), centred on assets/pivots/barbfin.json ``snout`` (per heading, the needle's spawn)
  barbfin-shell_0..31   40x40, normal: the dead shell sinking, 8 headings (every 4th of the 32: heading // 4) x 4
                        steps (dimmed to the deep water's colour, shrinking, fading; 5 steps a second, drifting
                        down ~18 px as it plays); drawn over the under-water `small` burst (explosion-deep-small)
  barbfin-ink_0..7      48x48, normal with stepped translucency: the short ink-grey cloud that the death leaves (no
                        bubbles), 10 fps, centred a few px below the kill
  assets/pivots/barbfin.json   per heading the snout tip (the needle's spawn) in the frame's px
  design/enemies/naval/concept/barbfin-final-r53-a.png/.gif   the review sheet and GIF (a column of four from the
                        left and three from the right darting in, holding 90-200 px above the ship and turning to
                        it, violet needles every 2.4 s, two kills; Level 24's plain under the review grade)

The violet needle the Barbfin spits is tools/art/enemy_bullets.py ``needle-violet`` (run that first for the review).

Run: python3 tools/art/barbfin.py [--review]   (~40 s; --review only rebuilds the review files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import l24kit as kit
from artkit import DESIGN, TAU

import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look a's model)
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_rigs import local_to_screen, model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "barbfin.py"
BATCH = "M8 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r53"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
HEADINGS = 32
SIZE, EXTENT = 40, 2.3
SNOUT_TIP = (0.0, 1.14)                 # model units: the violet barb at the snout's end (+Y forward)
TELL_GLOW = 2.6
SNOUT, INK = 16, 48
VIOLET = np.array(e48.VIOLET, float)
DEEP = np.array((6, 30, 44), float)
SHELL_HEADINGS = 8


def turned(scene, mats, h):
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def _frame(job):
    k, phase, glow = job[:3]
    scene, mats = e48.barbfin("a", phase, glow)
    if len(job) > 3:                     # emission only: the glow set
        mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    scene, mats = turned(scene, mats, TAU * k / HEADINGS)
    return artkit.native(*artkit.render_hi(scene, mats, (SIZE, SIZE), EXTENT), crisp=0 if len(job) > 3 else 70)


def glow_frame(black):
    """Emission-only render -> additive: the violet eyes and barb, a 1 px stepped halo."""
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    halo = np.stack([np.array(Image.fromarray(rgb[..., c].astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.2)),
                              float) for c in range(3)], -1)
    out = np.maximum(rgb, np.floor(halo / 255 * 3 + 0.5) / 3 * 255 * 0.6 * (halo.max(-1, keepdims=True) > 12))
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def snout_pivot(k):
    dx, dy = local_to_screen(SNOUT_TIP[0], SNOUT_TIP[1], np.pi / 2 + TAU * k / HEADINGS, SIZE / EXTENT)
    return [round(SIZE / 2 + dx, 1), round(SIZE / 2 + dy, 1)]


def _light(w, field, col, levels=7):
    q = artkit.ordered_dither(np.clip(field, 0, 1), levels)
    rgb = np.asarray(col, float)[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def snout_frame(i):
    """The barb's violet sparkle: a short four-point star with a white core, swelling then fading."""
    w = SNOUT
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    s = [0.7, 1.0, 0.85, 0.55][i]
    star = np.exp(-np.abs(x) / 0.6) * np.exp(-np.abs(y) / (2.6 * s)) + np.exp(-np.abs(y) / 0.6) * np.exp(
        -np.abs(x) / (2.6 * s))
    core = np.exp(-(np.hypot(x, y) / (1.4 * s)) ** 2)
    f = np.clip(star * 0.7 + core, 0, 1) * [0.8, 1.0, 0.8, 0.5][i]
    col = np.clip(VIOLET * 0.75 + 70, 0, 255)
    return _light(w, f, col)


def shell_frames(frames):
    """The dead shell: the swim frame (glow out) dimmed toward the deep water, shrinking and fading in 4 steps."""
    out = []
    for hk in range(SHELL_HEADINGS):
        base = frames[hk]
        for s in range(4):
            u = s / 3
            a = np.array(base).astype(np.float64)
            lum = a[..., :3].mean(-1, keepdims=True)
            rgb = (a[..., :3] * 0.35 + lum * 0.25) * (1 - 0.35 * u) + DEEP * (0.25 + 0.45 * u)
            a[..., :3] = rgb
            img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
            sc = 1 - 0.4 * u
            n = max(4, int(round(SIZE * sc)))
            small = img.resize((n, n), Image.Resampling.BOX)
            cell = Image.new("RGBA", (SIZE, SIZE))
            cell.alpha_composite(small, ((SIZE - n) // 2, (SIZE - n) // 2))
            al = np.array(cell)
            alpha = al[..., 3] / 255.0
            keep = (1 - 0.6 * u)
            al[..., 3] = np.where(alpha >= 0.5, 255 if keep > 0.85 else (170 if keep > 0.5 else 85), 0)
            al[al[..., 3] == 0] = 0
            out.append(Image.fromarray(al, "RGBA"))
    return out


def ink_frame(i):
    """An ink-grey cloud swelling and thinning out (stepped translucency, a lumpy edge, no bubbles)."""
    w, n = INK, 8
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    noise = raster.fbm(w, w, 10, 2407, octaves=3, period=False)
    r = np.hypot(xx - w / 2, (yy - w / 2 - 2 * u) * 1.1)
    rad = 6 + 15 * (1 - (1 - u) ** 2)
    dens = np.clip(1 - r / (rad * (0.75 + 0.5 * noise)), 0, 1) ** 0.7
    dens *= (1 - u) ** 1.2 * 0.85
    alpha = artkit.smooth_steps(dens)
    rgb = np.array((64, 70, 80), float) * (0.85 + 0.3 * noise[..., None])
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def intel_model():
    """(scene, mats) for the hangar intel portrait: one Barbfin nose down, the barb lit."""
    return turned(*e48.barbfin("a", 0.25, 1.6), 0.0)


def build():
    jobs = [(k, ph, g) for k in range(HEADINGS) for ph, g in ((0.0, 1.0), (0.5, 1.0), (0.0, TELL_GLOW))]
    shell_jobs = [(k * (HEADINGS // SHELL_HEADINGS), 0.25, 0.0) for k in range(SHELL_HEADINGS)]
    glow_jobs = [(k, 0.0, 1.0, "black") for k in range(HEADINGS)]
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(_frame, jobs + shell_jobs + glow_jobs))
    body, shells = frames[:len(jobs)], frames[len(jobs):len(jobs) + len(shell_jobs)]
    glows = frames[len(jobs) + len(shell_jobs):]
    kit.write("barbfin-glow", artkit.quantize_set([glow_frame(g) for g in glows], 12), SOURCE)
    kit.write("barbfin", artkit.quantize_set(body, 32), SOURCE)
    kit.write("barbfin-snout", [snout_frame(i) for i in range(4)], SOURCE)
    kit.write("barbfin-shell", artkit.quantize_set(shell_frames(shells), 16), SOURCE)
    kit.write("barbfin-ink", artkit.quantize_set([ink_frame(i) for i in range(8)], 8), SOURCE)
    artkit.write_pivots("barbfin", SOURCE, {"frame": [SIZE, SIZE], "headings": HEADINGS,
                                            "snout": [snout_pivot(k) for k in range(HEADINGS)]})


# =========================================================================== review

DUR, FPS = 10.0, 20
SHIP_Y = 470.0


def ship_x(t):
    return 240 + 70 * np.sin(t * 0.55)


class Holder:
    """Enter on a curve, hold 90-200 px above the ship (6 px/s drift), turn to the ship, a needle every 2.4 s (the
    first 0.9 s after it stops), leave to the nearer side after 9 s (or die)."""

    def __init__(self, pts, t0, dur, kill=None, first=0.9, leave=9.0):
        n = len(pts)
        self.path = rig.catmull_rom(pts, [t0 + dur * i / (n - 1) for i in range(n)])
        self.t0, self.dur, self.kill, self.first = t0, dur, kill, first
        self.hold = np.array(pts[-1], float)
        self.leave = t0 + dur + leave

    def pos(self, t):
        if t < self.t0 + self.dur:
            p = np.array(self.path(t))
            p2 = np.array(self.path(t + 0.05))
            return p, kit.heading_index(*(p2 - p), n=HEADINGS), True
        dt = t - self.t0 - self.dur
        p = self.hold + np.array((6 * np.sin(dt * 0.9), 4 * np.sin(dt * 0.7)))
        if t > self.leave:
            side = -1 if self.hold[0] < 240 else 1
            u = t - self.leave
            p = p + np.array((side * 120 * u, -20 * u))
            return p, kit.heading_index(side, -0.15, n=HEADINGS), True
        return p, kit.heading_index(ship_x(t) - p[0], SHIP_Y - p[1], n=HEADINGS), False

    def shots(self):
        ts, out = self.t0 + self.dur + self.first, []
        while ts < min(self.leave, DUR) and (self.kill is None or ts < self.kill):
            out.append(ts)
            ts += 2.4
        return out


def holders():
    hs = []
    for i in range(4):
        hold = (90 + 80 * i, 290 - 18 * (i % 2))
        hs.append(Holder([(-30, 70 + 30 * i), (50 + 40 * i, 140 + 25 * i), (hold[0] - 18, hold[1] - 30), hold],
                         0.2 + 0.45 * i, 1.7, kill=5.0 if i == 1 else None))
    for i in range(3):
        hold = (380 - 90 * i, 360 + 14 * (i % 2))
        hs.append(Holder([(510, 120 + 30 * i), (420 - 40 * i, 200 + 30 * i), (hold[0] + 20, hold[1] - 30), hold],
                         3.0 + 0.4 * i, 1.6, kill=7.6 if i == 0 else None))
    return hs


def review():
    bf, sn, shell, ink, bg = (kit.load(n) for n in ("barbfin", "barbfin-snout", "barbfin-shell", "barbfin-ink",
                                                     "barbfin-glow"))
    pivots = [snout_pivot(k) for k in range(HEADINGS)]
    needle = artkit.load_frames("needle-violet")
    burst, cavity = artkit.load_frames("explosion-deep-small"), artkit.load_frames("explosion-deep-cavity-small")
    rows = [("HEADINGS 0-10, SWIM FRAME 0", bf[0:33:3], 2, False),
            ("HEADINGS 11-21", bf[33:66:3], 2, False),
            ("HEADINGS 22-31", bf[66:96:3], 2, False),
            ("SWIM FRAME 1 (TAIL THE OTHER WAY), EVERY 3RD HEADING", bf[1::9], 2, False),
            ("THE SHOT'S TELL (SNOUT AND EYES LIT), EVERY 3RD HEADING", bf[2::9], 2, False),
            ("EYE AND BARB GLOW (ADDITIVE), EVERY 3RD HEADING", bg[0::3], 2, True),
            ("SNOUT SPARKLE (ADDITIVE)", sn, 4, True),
            ("DEAD SHELL SINKING (8 HEADINGS X 4 STEPS)", shell[:16], 2, False),
            ("INK CLOUD (STEPPED; ON THE PLAIN'S WATER IN THE GAME)", ink, 2, False),
            ("VIOLET NEEDLE (ENEMY_BULLETS.PY), 16 HEADINGS", needle, 2, False)]
    sheet = artkit.review_sheet("BARBFIN - FINAL (ROUND 48 LOOK A, BONE LIONFISH)", rows, width=1000, batch=BATCH)
    hs = holders()
    shots = []
    for h in hs:
        for ts in h.shots():
            p, k, _ = h.pos(ts)
            sx, sy = pivots[k]
            o = p + np.array((sx - SIZE / 2, sy - SIZE / 2))
            d = np.array((ship_x(ts) - o[0], SHIP_Y - o[1]))
            shots.append((ts, o, d / np.linalg.norm(d) * 150 * 0.7))
    frames = []
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        img = kit.plate(480, 540, 0, 700, section=2)
        glow = []
        for i, h in enumerate(hs):
            if t < h.t0:
                continue
            p, k, moving = h.pos(t)
            if h.kill is not None and t >= h.kill:
                s = t - h.kill
                step = int(s * 20)
                if step < len(cavity):
                    kit.paste_c(img, cavity[step], *p)
                if s < 0.8:
                    kit.paste_c(img, ink[min(7, int(s * 10))], p[0], p[1] + 4)
                    kit.paste_c(img, shell[(k // 4) * 4 + min(3, int(s * 5))], p[0], p[1] + 22 * s)
                if step < len(burst):
                    glow.append((burst[step], p))
                continue
            tell = any(0 <= ts - t < 0.25 for ts in h.shots())
            frame = 2 if tell else int(t * (6 if moving else 3) + i) % 2
            kit.paste_c(img, kit.lit(bf[k * 3 + frame], 2), *p)
            glow.append((bg[k], p))
            sx, sy = pivots[k]
            if tell or any(0 <= t - ts < 0.1 for ts in h.shots()):
                glow.append((sn[min(3, int((t * 20) % 4))], p + np.array((sx - SIZE / 2, sy - SIZE / 2))))
        img.alpha_composite(kit.ship(2), (int(ship_x(t) - 24), int(SHIP_Y - 24)))
        for ts, o, v in shots:
            if ts <= t:
                q = o + v * (t - ts)
                if -10 < q[1] < 550:
                    kit.paste_c(img, needle[kit.heading_index(*v)], *q)
        for g, p in glow:
            img = kit.add_c(img, g, *p)
        raster.draw_text(img, 8, 8, "BARBFIN A - T={:.1f}".format(t), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * FPS)] for t in (2.3, 4.0, 5.2)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE COLUMN DARTING IN (T=2.3), HOLDING AND SPITTING (T=4.0), A "
                                            "KILL (T=5.2); 2X CROP; LEVEL 24 REVIEW GRADE (SECTION 2)", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    crop = frames[int(4.0 * FPS)].crop((60, 190, 240, 392)).resize((240, 270), Image.Resampling.NEAREST)
    big.alpha_composite(crop.convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames[::2]], CONCEPT, "barbfin",
                       fps=FPS // 2)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
