#!/usr/bin/env python3
"""Production art: the Spiral Nautilus, Level 25's shell roller (design/enemies/naval/spiral-nautilus; M8 part E batch,
concept round 54, step ART-E). Round 06's chosen concept **a** to production (tools/concept/render/r06_models.py
``nautilus_shell`` and ``nautilus_crown``, role colours ``spiral-nautilus`` = bone / violet / violet, imported
unchanged): a tiger-striped ivory shell with growth ribs and a faint violet aperture lip that rolls closed, and the
hooded tentacle crown it uncoils toward the ship. The crown's two eyes are re-lit lime-white (the stat block's weak
point "violet crown and eyes, glowing lime-white"); nothing else changes.

Headings as tools/art/README.md: heading k turns k x 22.5 degrees clockwise from straight down (`16 angles`, the crown
toward the ship), each its own render under the fixed key light. Sizes from the stat block: the shell 56x56 (hitbox
40x40); the crown on a 56 px canvas, drawn at the pivot file's offset from the shell's centre (so the uncoiled unit
spans about 64 px along its heading).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims
``spiral-nautilus`` once design/enemies/naval/spiral-nautilus/data.yaml exists, first_level 25):
  spiral-nautilus_0..23        56x56, normal: the closed shell rolling (the 24-frame spin, model-space stripes; the
                               game steps it by the distance rolled)
  spiral-nautilus-lip_0..23    56x56, additive: the closed shell's faint violet aperture lip per spin frame (its glow
                               in the dark, ungraded)
  spiral-nautilus-open_0..15   56x56, normal: the shell held still with its aperture toward heading k (shown from the
                               uncoil's first frame to its last)
  spiral-nautilus-crown_0..95  56x56, normal: heading x 6: 0-2 the crown emerging (the 0.6 s opening, 0.2 s a step;
                               played backwards to close), 3 and 4 the open sway (the tentacles' wave, 5 fps while it
                               fires), 5 the tell (eyes and tips at full glow: the 0.4 s before the spiral)
  spiral-nautilus-glow_0..47   56x56, additive, aligned with the crown: heading x 3: open, tell, emerging (eyes,
                               tentacle tips and the mantle's rim), drawn ungraded over the crown
  spiral-nautilus-dark_0..15   56x56, normal: the dead crown per heading (glow out, dimmed: "the crown goes dark")
  spiral-nautilus-halves_0..15 64x64, normal: the death: the shell cracked into two halves drifting apart and sinking
                               (8 steps at 10 fps) x 2 crack axes (frames 0-7 across, 8-15 along), dimming to the deep
                               water; drawn over the under-water `medium` burst
  spiral-nautilus-fleck_0..5   32x32, additive: the glance-off off the closed shell (a ring of lime-grey flecks
                               bursting out, 20 fps), at the hit point
  assets/pivots/spiral-nautilus.json   per heading the crown's centre offset from the shell's centre for the emerging
                               steps and the open crown, and the spiral's spawn (the mantle's mouth)
  design/enemies/naval/concept/spiral-nautilus-final-r54-a.png/.gif   the review sheet and GIF (a pair orbiting the ship
                               in opposite directions on Level 25's trench, spiralling in, uncoiling 2 s apart and firing
                               3-arm spirals, glance flecks off a closed shell, one cracked open while uncoiled)

Run: python3 tools/art/spiral_nautilus.py [--review]   (~2 min; --review only rebuilds the review files; run
backdrop_l25.py first: the review stands on its trench)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import l24kit as kit
from artkit import DESIGN, TAU

from render import enemy_models as em  # noqa: E402
from render import r06_models as m6  # noqa: E402  (round 06's chosen model, unchanged)
from render import raster  # noqa: E402
from render.enemy_rigs import local_to_screen, model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_z, union  # noqa: E402

SCRIPT = "spiral_nautilus.py"
BATCH = "M8 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r54"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
N = 56
EXTENT = 2.3
SPIN, HEADINGS = 24, 16
CROWN_STEPS = ((0.25, 0.0, 0.8, 14.0), (0.6, 0.5, 0.9, 19.0), (0.9, 1.0, 1.0, 23.5),
               (1.0, 0.0, 1.0, 23.5), (1.0, 2.1, 1.0, 23.5), (1.05, 1.0, 2.2, 23.5))   # spread, phase, glow, offset
MOUTH = (0.0, 0.42)                         # crown model units: where the spiral leaves (the tentacles' root)
HALF, FLECK = 64, 32
EYE_LIME = np.array((0.78, 1.0, 0.62))
DEEP = np.array((6, 30, 44), float)


def h_angle(k, n=HEADINGS):
    return np.pi / 2 + TAU * k / n


def turned(scene, mats, ang):
    rot = model_rotation(ang)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def crown_model(spread, phase, glow):
    scene, mats = m6.nautilus_crown(spread, phase, glow)
    mats[em.V_EYE] = replace(mats[em.V_EYE], emission=tuple(EYE_LIME * 1.4 * glow))
    return scene, mats


def shell_model(glow=1.0, half=0):
    scene, mats = m6.nautilus_shell(glow)
    if half:
        axis = 0 if abs(half) == 1 else 1
        sign = 1 if half > 0 else -1

        def cut(p, scene=scene):
            d, m = scene(p)
            return np.maximum(d, -sign * p[:, axis] + 0.03), m
        return cut, mats
    return scene, mats


def blacked(mats):
    return [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]


def _frame(job):
    kind = job[0]
    if kind == "spin":
        scene, mats = shell_model(1.0)
        scene, mats = turned(scene, mats, TAU * job[1] / SPIN)
        size = (N, N)
    elif kind == "lip":
        scene, mats = shell_model(1.6)
        scene, mats = turned(scene, blacked(mats), TAU * job[1] / SPIN)
        size = (N, N)
    elif kind == "open":
        scene, mats = shell_model(1.6)
        scene, mats = turned(scene, mats, h_angle(job[1]))
        size = (N, N)
    elif kind == "crown":
        spread, phase, glow, _ = CROWN_STEPS[job[2]]
        scene, mats = crown_model(spread, phase, glow)
        scene, mats = turned(scene, mats, h_angle(job[1]))
        size = (N, N)
    elif kind == "crown-glow":
        spread, phase, glow, _ = CROWN_STEPS[job[2]]
        scene, mats = crown_model(spread, phase, glow)
        scene, mats = turned(scene, blacked(mats), h_angle(job[1]))
        size = (N, N)
    elif kind == "dark":
        scene, mats = crown_model(0.7, 0.0, 0.0)
        scene, mats = turned(scene, mats, h_angle(job[1]))
        size = (N, N)
    else:                                                    # "half": (half id, axis rotation)
        scene, mats = shell_model(0.0, job[1])
        scene, mats = turned(scene, mats, job[2])
        size = (HALF, HALF)
    crisp = 0 if kind in ("lip", "crown-glow") else 70
    return artkit.native(*artkit.render_hi(scene, mats, size, EXTENT * size[0] / N), crisp=crisp)


def glow_frame(black, blur=1.2):
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    halo = np.stack([np.array(Image.fromarray(rgb[..., c].astype(np.uint8)).filter(ImageFilter.GaussianBlur(blur)),
                              float) for c in range(3)], -1)
    out = np.maximum(rgb, np.floor(halo / 255 * 3 + 0.5) / 3 * 255 * 0.6 * (halo.max(-1, keepdims=True) > 12))
    out = np.where(out.max(-1, keepdims=True) < 10, 0, out)
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def dimmed(img, k=0.55):
    a = np.array(img).astype(np.float64)
    lum = a[..., :3].mean(-1, keepdims=True)
    a[..., :3] = (a[..., :3] * 0.5 + lum * 0.5) * k + DEEP * 0.2
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def halves_frames(parts):
    """8 steps per crack axis: the two halves (each its own render) drifting apart along the crack's normal, tilting
    a little, sinking (shrinking, dimming toward the deep water, stepped fade)."""
    out = []
    for axis in range(2):
        a_img, b_img = parts[axis * 2], parts[axis * 2 + 1]
        nx, ny = ((1, 0), (0, 1))[axis]
        for s in range(8):
            u = s / 7
            cell = Image.new("RGBA", (HALF, HALF))
            for img, sg in ((a_img, 1), (b_img, -1)):
                sc = 1 - 0.38 * u
                n = max(4, int(round(HALF * sc)))
                im = dimmed(img, 0.85 - 0.45 * u).rotate(sg * 14 * u, Image.Resampling.BICUBIC).resize(
                    (n, n), Image.Resampling.BOX)
                off = 2 + 9 * u
                x = (HALF - n) / 2 + sg * nx * off
                y = (HALF - n) / 2 + sg * ny * off + 3 * u
                cell.alpha_composite(im, (int(round(x)), int(round(y))))
            al = np.array(cell)
            alpha = al[..., 3] / 255.0
            keep = 1 - 0.65 * u
            al[..., 3] = np.where(alpha >= 0.5, 255 if keep > 0.8 else (170 if keep > 0.5 else 85), 0)
            al[al[..., 3] == 0] = 0
            out.append(Image.fromarray(al, "RGBA"))
    return out


def fleck_frame(i, n=6):
    """A ring of lime-grey flecks bursting off the shell: short streaks flying out and fading."""
    w = FLECK
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    u = (i + 0.5) / n
    f = np.zeros((w, w))
    rng = np.random.default_rng(2621)
    for k in range(9):
        a = TAU * k / 9 + rng.uniform(-0.2, 0.2)
        r = 3 + 11 * u * rng.uniform(0.8, 1.2)
        px, py = np.cos(a) * r, np.sin(a) * r
        along = (x - px) * np.cos(a) + (y - py) * np.sin(a)
        across = -(x - px) * np.sin(a) + (y - py) * np.cos(a)
        f = np.maximum(f, np.clip(1 - np.abs(across) / 0.8, 0, 1) * np.clip(1 - np.abs(along) / (1 + 2 * (1 - u)), 0, 1))
    f += 0.8 * np.exp(-(np.hypot(x, y) / (2.2 * (1 - u) + 0.3)) ** 2) * (1 - u)
    f *= (1 - u) ** 0.6
    q = artkit.ordered_dither(np.clip(f, 0, 1), 5)
    col = np.array((196, 226, 150), float)
    rgb = col[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def offsets():
    out = []
    for k in range(HEADINGS):
        a = h_angle(k)
        out.append([[round(np.cos(a) * st[3], 1), round(np.sin(a) * st[3], 1)] for st in CROWN_STEPS[:3]])
    return out


def mouth_pivot(k):
    a = h_angle(k)
    dx, dy = local_to_screen(MOUTH[0], MOUTH[1], a, N / EXTENT)
    off = CROWN_STEPS[2][3]
    return [round(np.cos(a) * off + dx, 1), round(np.sin(a) * off + dy, 1)]


def intel_model():
    """(scene, mats) for the hangar intel portrait: the uncoiled unit nose down, the shell behind and the crown open
    with its eyes lit, scaled to fit the card."""
    s_scene, s_mats = shell_model(1.4)
    c_scene, c_mats = crown_model(1.0, 0.5, 1.4)
    k = 0.7
    off = CROWN_STEPS[2][3] / (N / EXTENT)

    def scene(p):
        q = p / k
        a = q.copy()
        a[:, 1] += 0.45
        b = q.copy()
        b[:, 1] -= off - 0.45
        d, m = union(s_scene(a), c_scene(b), k=0.02)
        return d * k, m
    mats = list(s_mats)
    mats[em.V_EYE] = c_mats[em.V_EYE]
    mats[em.V_GLOW] = c_mats[em.V_GLOW]
    mats[em.V_SAC] = c_mats[em.V_SAC]
    return turned(scene, mats, np.pi / 2)


def build():
    jobs = ([("spin", k) for k in range(SPIN)] + [("lip", k) for k in range(SPIN)]
            + [("open", k) for k in range(HEADINGS)]
            + [("crown", k, s) for k in range(HEADINGS) for s in range(6)]
            + [("crown-glow", k, s) for k in range(HEADINGS) for s in (3, 5, 1)]
            + [("dark", k) for k in range(HEADINGS)]
            + [("half", h, 0.6) for h in (1, -1, 2, -2)])
    with ProcessPoolExecutor() as ex:
        fr = list(ex.map(_frame, jobs))
    i = 0

    def take(n):
        nonlocal i
        out = fr[i:i + n]
        i += n
        return out
    spin, lip, opn, crown, cglow, dark, parts = (take(SPIN), take(SPIN), take(HEADINGS), take(HEADINGS * 6),
                                                 take(HEADINGS * 3), take(HEADINGS), take(4))
    kit.write("spiral-nautilus", artkit.quantize_set(spin, 32), SOURCE)
    kit.write("spiral-nautilus-lip", artkit.quantize_set([glow_frame(g, 1.0) for g in lip], 8), SOURCE)
    kit.write("spiral-nautilus-open", artkit.quantize_set(opn, 32), SOURCE)
    kit.write("spiral-nautilus-crown", artkit.quantize_set(crown, 32), SOURCE)
    kit.write("spiral-nautilus-glow", artkit.quantize_set([glow_frame(g) for g in cglow], 12), SOURCE)
    kit.write("spiral-nautilus-dark", artkit.quantize_set([dimmed(d, 0.6) for d in dark], 16), SOURCE)
    kit.write("spiral-nautilus-halves", artkit.quantize_set(halves_frames(parts), 24), SOURCE)
    kit.write("spiral-nautilus-fleck", [fleck_frame(k) for k in range(6)], SOURCE)
    artkit.write_pivots("spiral-nautilus", SOURCE, {
        "frame": [N, N], "headings": HEADINGS, "spin": SPIN,
        "crown_offset": offsets(),
        "mouth": [mouth_pivot(k) for k in range(HEADINGS)]})


# =========================================================================== review

DUR, FPS = 10.0, 12
T0 = 62.5                                   # Level 25's clock: the first pair (section 3)
SHIP_Y = 400.0
OPEN = ((3.0, 5.2), (8.2, 10.4))            # closed 3 s, then 0.6 opening + 1.6 open; the second 2 s later


def ship_x(t):
    return 240 + 40 * np.sin(t * 0.5)


class Naut:
    def __init__(self, sgn, offset, kill=None):
        self.sgn, self.offset, self.kill = sgn, offset, kill

    def phase(self, t):
        """'closed', 'opening' (step), 'open', 'closing' (step) and the cycle time."""
        c = (t - self.offset) % 5.2 if t >= self.offset else -1
        if c < 0 or c < 3.0:
            return "closed", 0
        c -= 3.0
        if c < 0.6:
            return "opening", int(c / 0.2)
        if c < 2.2:
            return "open", 0
        return "closed", 0

    def pos(self, t, centre):
        if t < self.offset:
            r, a = 260, self.sgn * 0.0
        c = max(0.0, (t - self.offset) % 5.2)
        r = 220 - 130 * min(1.0, c / 3.0) if c < 3.0 else 90 + 20 * max(0.0, c - 4.6)
        a = self.sgn * (0.9 * t) + (0 if self.sgn > 0 else np.pi)
        return centre + np.array((np.cos(a) * r, np.sin(a) * r * 0.8))


def review():
    import backdrop_l25 as b25                              # noqa: E402  (tools/art: the trench)
    spin, lip, opn, crown, cglow, dark, halves, fleck = (kit.load(n) for n in (
        "spiral-nautilus", "spiral-nautilus-lip", "spiral-nautilus-open", "spiral-nautilus-crown",
        "spiral-nautilus-glow", "spiral-nautilus-dark", "spiral-nautilus-halves", "spiral-nautilus-fleck"))
    orb = artkit.load_frames("orb")
    burst = artkit.load_frames("explosion-deep-medium") or artkit.load_frames("explosion-deep-small")
    offs = offsets()
    mouths = [mouth_pivot(k) for k in range(HEADINGS)]
    rows = [("CLOSED SHELL ROLLING, EVERY 2ND OF 24", spin[::2], 2, False),
            ("APERTURE LIP GLOW (ADDITIVE), EVERY 4TH", lip[::4], 2, True),
            ("OPEN SHELL, APERTURE TO HEADING 0-15", opn, 1, False),
            ("CROWN HEADING 0: EMERGING X 3, SWAY X 2, TELL", crown[0:6], 2, False),
            ("CROWN HEADING 4 (LEFT): THE SAME", crown[24:30], 2, False),
            ("CROWN OPEN AT EVERY HEADING (SWAY A)", crown[3::6], 1, False),
            ("CROWN GLOW (ADDITIVE): OPEN, TELL, EMERGING; HEADINGS 0, 4", cglow[0:3] + cglow[12:15], 2, True),
            ("DEAD CROWN, EVERY 2ND HEADING", dark[::2], 1, False),
            ("DEATH: THE SHELL'S HALVES SINKING (2 CRACK AXES)", halves, 1, False),
            ("GLANCE FLECKS (ADDITIVE)", fleck, 2, True)]
    sheet = artkit.review_sheet("SPIRAL NAUTILUS - FINAL (ROUND 06 CONCEPT A)", rows, width=1000, batch=BATCH)
    pieces = b25.placed_pieces()
    pair = [Naut(1, 0.0), Naut(-1, 2.0, kill=8.0)]
    shots = []                                               # (t0, x, y, vx, vy)
    for n_ in pair:
        for t in np.arange(0, DUR, 0.1):
            ph, _ = n_.phase(t)
            if ph == "open" and (n_.kill is None or t < n_.kill):
                c = (t - n_.offset) % 5.2 - 3.6
                if c >= 0.0:
                    shots.append((t, n_, 3))
    frames = []
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        lt = T0 + 3.0 + t
        img = b25.composite(lt, pieces, ship=False).copy()
        sx, sy = ship_x(t), SHIP_Y
        centre = np.array((sx, sy))
        glows = []
        dist = 0.0
        for j, n_ in enumerate(pair):
            p = n_.pos(t, centre)
            aim = kit.heading_index(sx - p[0], sy - p[1], n=HEADINGS)
            if n_.kill is not None and t >= n_.kill:
                s = t - n_.kill
                st = int(s * 10)
                if st < 8:
                    kit.paste_c(img, kit.grade(halves[st], 4), p[0], p[1] + 12 * s)
                    kit.paste_c(img, kit.grade(dark[aim], 4), p[0] + offs[aim][2][0], p[1] + offs[aim][2][1] + 10 * s)
                if burst and int(s * 20) < len(burst):
                    glows.append((burst[int(s * 20)], p))
                continue
            ph, step = n_.phase(t)
            if ph == "closed":
                dist = t * 150 * n_.sgn
                k = int(dist / (N * 0.4)) % SPIN
                kit.paste_c(img, kit.grade(spin[k], 4), *p)
                glows.append((lip[k], p))
                if int(t * 3 + j) % 4 == 0 and 2.0 < t < 9.0:
                    glows.append((fleck[int(t * 20) % 6], p + np.array((0, 14))))
            else:
                kit.paste_c(img, kit.grade(opn[aim], 4), *p)
                if ph == "opening":
                    s = min(2, step)
                    o = offs[aim][s]
                    kit.paste_c(img, kit.grade(crown[aim * 6 + s], 4), p[0] + o[0], p[1] + o[1])
                    glows.append((cglow[aim * 3 + 2], p + np.array(o)))
                else:
                    c = (t - n_.offset) % 5.2 - 3.6
                    s = 5 if c < 0.0 else 3 + int(t * 5) % 2
                    o = offs[aim][2]
                    kit.paste_c(img, kit.grade(crown[aim * 6 + s], 4), p[0] + o[0], p[1] + o[1])
                    glows.append((cglow[aim * 3 + (1 if s == 5 else 0)], p + np.array(o)))
        for g, p in glows:
            img = kit.add_c(img, g, *p)
        for t0, n_, arms in shots:                           # the spiral: 3 arms rotating 90 deg/s, 70 px/s
            if t0 > t:
                continue
            p0 = n_.pos(t0, np.array((ship_x(t0), SHIP_Y)))
            for a in range(arms):
                ang = n_.sgn * np.radians(90) * t0 + TAU * a / arms
                q = p0 + (t - t0) * 70 * np.array((np.cos(ang), np.sin(ang)))
                if -10 < q[0] < 490 and -10 < q[1] < 550 and orb:
                    kit.paste_c(img, orb[0], *q)
        img.alpha_composite(kit.ship(2), (int(sx - 24), int(sy - 24)))
        raster.draw_text(img, 8, 8, "SPIRAL NAUTILUS - L25 T={:.1f}".format(lt), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * FPS)] for t in (1.5, 4.4, 8.3)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: SPIRALLING IN CLOSED (T+1.5), ONE UNCOILED AND FIRING (T+4.4), "
                                            "CRACKED OPEN (T+8.3); 2X CROP; LEVEL 25, REVIEW GRADE 0.14", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    q = pair[0].pos(4.4, np.array((ship_x(4.4), SHIP_Y)))
    cx, cy = int(np.clip(q[0] - 90, 0, 300)), int(np.clip(q[1] - 101, 0, 338))
    crop = frames[int(4.4 * FPS)].crop((cx, cy, cx + 180, cy + 202)).resize((240, 270), Image.Resampling.NEAREST)
    big.alpha_composite(crop.convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "spiral-nautilus",
                       fps=FPS)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
