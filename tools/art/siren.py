#!/usr/bin/env python3
"""Production art: the Siren, Level 25's singing wall nest (design/enemies/naval/siren; M8 part E batch, concept
round 54, step ART-E). From the chosen round-48 look **b** "frill bell" (tools/concept/enemies_r48.py ``siren`` b,
role colours ``siren-b`` = slate / teal / teal, imported unchanged): a slate dome grown into the wall's foot, five
membrane frills on bone rays fanned round a teal throat sac; on the tell the frills spread and the sac swells; the
ring is b's rippled double wavefront.

The nest is `fixed` and faces the open water: facing 0 sits on the LEFT wall and faces right, facing 1 on the RIGHT
wall and faces left; each facing is its own render under the fixed key light (nothing lit mirrored). Size from the
stat block: 64x56 (hitbox 48x40); the dome's back is the sprite's wall side: drawn with its centre about 6 px off the
foot line into the water (the dome then sits in the rock, the sac over the foot band).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``siren`` once
design/enemies/naval/siren/data.yaml exists, first_level 25):
  siren_0..11          64x56, normal: facing x 6 (facing 0 frames 0-5, facing 1 frames 6-11): 0, 1 the idle breath
                       (the sac's slow pulse, 2 fps), 2-4 the tell (0.8 s: the frills spreading, the sac swelling and
                       brightening; 0.27 s a step), 5 the song (frills flared, the sac at full glow: the 0.15 s the ring
                       leaves), then back to idle
  siren-glow_0..11     64x56, additive: per frame the sac's and the frills' teal emission (a 1 px stepped halo), drawn
                       ungraded over the nest (so it reads in the dark; alone in Level 26)
  siren-death_0..7     64x56, normal: facing x 4: the sac collapsing, the frills folding, the glow dying (12 fps), over
                       the under-water `medium` burst
  siren-dead_0..1      64x56, normal: per facing the dark husk left on the wall (the cave mouth goes dark)
  siren-plume_0..7     64x64, normal, stepped translucency: the pale plume the collapsing sac releases (10 fps), drawn at
                       the sac, drifting up ~16 px as it plays
  siren-ring_0..7      64x24, additive: the ring's band as a strip the game wraps round the circle (u along the arc,
                       tiling every 64 px; row 7 the wavefront at the ring's radius, rows 0-6 outside it, 8-23 inside:
                       b's rippled double wavefront, the second front 8 px behind the first), 8 frames of the ripple's
                       travel at 12 fps; the game fades it over the last quarter of its 230 px and leaves out the
                       segments rock stops (E5)
  siren-shimmer_0..7   64x64, additive: the slowed hull's teal sonar shimmer (rippling outlines expanding round the
                       ship, 12 fps loop) for the ship and Rook while the slow lasts
  assets/pivots/siren.json   per facing the sac's centre (the ring's origin) in the frame's px
  design/enemies/naval/concept/siren-final-r54-a.png/.gif   the review sheet and GIF (Level 25's walls: nests on both
                       walls telling and singing, a ring slowing the ship, a later ring passing it immune, one nest shot)

Run: python3 tools/art/siren.py [--review]   (~1 min; --review only rebuilds the review files; run backdrop_l25.py
first: the review stands on its walls)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import l24kit as kit
from artkit import DESIGN, TAU

import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look b's model)
from render import raster  # noqa: E402
from render.enemy_rigs import local_to_screen, model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "siren.py"
BATCH = "M8 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r54"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
SIZE = (64, 56)
EXTENT = 2.3
FACINGS = (0.0, np.pi)                      # facing 0: on the left wall, facing right; 1: on the right wall
STEPS = (0.0, 0.14, 0.35, 0.68, 1.0, 1.4)   # idle, idle breath, tell x 3, the song
DEATH = ((-0.7, 0.55), (-1.5, 0.28), (-2.3, 0.1), (-3.0, 0.0))
SAC = (0.0, 0.18)                           # model units: the sac's centre (+Y forward)
TEAL = np.array(e48.TEAL, float)
RING_W, RING_H, RING_FRONT = 64, 24, 7
PLUME, SHIMMER = 64, 64


def model(tell, glow=1.0):
    scene, mats = e48.siren("b", tell, glow)
    k = max(0.0, 0.35 + 1.25 * tell) * glow
    mats[-1] = replace(mats[-1], emission=tuple(np.array(e48.TEAL_F) * k))
    return scene, mats


def turned(scene, mats, h):
    rot = model_rotation(h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def _frame(job):
    face, tell, glow = job[:3]
    scene, mats = model(tell, glow)
    if len(job) > 3:
        mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    scene, mats = turned(scene, mats, FACINGS[face])
    return artkit.native(*artkit.render_hi(scene, mats, SIZE, EXTENT), crisp=0 if len(job) > 3 else 70)


def glow_frame(black):
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    halo = np.stack([np.array(Image.fromarray(rgb[..., c].astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.4)),
                              float) for c in range(3)], -1)
    out = np.maximum(rgb, np.floor(halo / 255 * 3 + 0.5) / 3 * 255 * 0.6 * (halo.max(-1, keepdims=True) > 12))
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def sac_pivot(face):
    dx, dy = local_to_screen(SAC[0], SAC[1], FACINGS[face], SIZE[0] / EXTENT)
    return [round(SIZE[0] / 2 + dx, 1), round(SIZE[1] / 2 + dy, 1)]


def plume_frame(i, n=8):
    """A pale plume: the sac's fluid billowing up and thinning (stepped translucency, lumpy edge, no bubbles)."""
    w = PLUME
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    noise = raster.fbm(w, w, 10, 2611, octaves=3, period=False)
    r = np.hypot(xx - w / 2, (yy - w / 2 + 6 * u) * 1.15)
    rad = 6 + 20 * (1 - (1 - u) ** 2)
    dens = np.clip(1 - r / (rad * (0.7 + 0.6 * noise)), 0, 1) ** 0.7 * (1 - u) ** 1.1 * 0.9
    alpha = artkit.smooth_steps(dens)
    rgb = np.array((176, 214, 206), float) * (0.85 + 0.3 * noise[..., None])
    out = np.dstack([np.clip(rgb, 0, 255), alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


def ring_frame(i, n=8):
    """The band strip: two rippled fronts (the first bright, the second 8 px behind and dimmer) with a soft halo."""
    w, h = RING_W, RING_H
    ss = 4
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64) / ss + 0.125
    ph = TAU * i / n
    f = np.zeros(yy.shape)
    for k, (off, amp) in enumerate(((0.0, 1.0), (8.0, 0.55))):
        line = RING_FRONT + off + 2.6 * np.sin(TAU * xx / w * 2 + ph + k * 1.3)
        d = np.abs(yy - line)
        f = np.maximum(f, amp * (np.clip(1 - d / 1.1, 0, 1) + 0.38 * np.clip(1 - d / 3.6, 0, 1)))
    f = f.reshape(h, ss, w, ss).mean(axis=(1, 3))
    q = artkit.ordered_dither(np.clip(f, 0, 1), 6)
    col = np.clip(TEAL * 0.55 + np.array((80, 40, 60)), 0, 255)
    rgb = col[None, None] * q[..., None]
    rgb = np.where((f > 0.85)[..., None], np.array((190, 255, 228.0)) * q[..., None], rgb)
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def shimmer_frame(i, n=8):
    """Three rippling outlines expanding from the hull (r 14 -> 30), fading as they grow; the slowed ship's tell."""
    w = SHIMMER
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, (yy - w / 2) / 0.9
    r = np.hypot(x, y)
    th = np.arctan2(y, x)
    f = np.zeros((w, w))
    for k in range(3):
        u = ((i / n) + k / 3) % 1.0
        rr = 14 + 16 * u + 1.2 * np.sin(th * 7 + TAU * i / n * 2 + k)
        f = np.maximum(f, (1 - u) * np.clip(1 - np.abs(r - rr) / 1.0, 0, 1))
    q = artkit.ordered_dither(np.clip(f, 0, 1), 4)
    rgb = np.array((140, 255, 210), float)[None, None] * q[..., None] * 0.85
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def intel_model():
    """(scene, mats) for the hangar intel portrait: the nest facing down the screen, the sac mid-tell."""
    return turned(*model(0.7), np.pi / 2)


def build():
    jobs = [(f, t, 1.0) for f in range(2) for t in STEPS]
    death = [(f, t, g) for f in range(2) for t, g in DEATH]
    glows = [(f, t, 1.0, "black") for f in range(2) for t in STEPS]
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(_frame, jobs + death + glows))
    body, dead, gl = frames[:12], frames[12:20], frames[20:]
    kit.write("siren", artkit.quantize_set(body, 32), SOURCE)
    kit.write("siren-glow", artkit.quantize_set([glow_frame(g) for g in gl], 12), SOURCE)
    dq = artkit.quantize_set(dead, 24)
    kit.write("siren-death", dq, SOURCE)
    kit.write("siren-dead", [dq[3], dq[7]], SOURCE)
    kit.write("siren-plume", artkit.quantize_set([plume_frame(i) for i in range(8)], 8), SOURCE)
    kit.write("siren-ring", [ring_frame(i) for i in range(8)], SOURCE)
    kit.write("siren-shimmer", [shimmer_frame(i) for i in range(8)], SOURCE)
    artkit.write_pivots("siren", SOURCE, {"frame": list(SIZE), "facings": ["left wall, facing right",
                                                                           "right wall, facing left"],
                                          "sac": [sac_pivot(f) for f in range(2)],
                                          "ring": {"strip": [RING_W, RING_H], "front_row": RING_FRONT}})


# =========================================================================== review

DUR, FPS = 9.0, 12
T0 = 35.6                                    # Level 25's clock: the walls of section 2
SHIP_Y = 444.0
RING_V, RING_R = 120.0, 230.0


def ship_x(t):
    return 240 + 60 * np.sin(t * 0.7)


def draw_ring(img, strip, cx, cy, r, alpha, clip):
    """The strip wrapped round the circle (row RING_FRONT at radius r), clipped where ``clip(x, y)`` is rock."""
    a = np.array(img).astype(np.float64)
    h, w = a.shape[:2]
    x0, x1 = int(max(0, cx - r - 20)), int(min(w, cx + r + 20))
    y0, y1 = int(max(0, cy - r - 20)), int(min(h, cy + r + 20))
    if x1 <= x0 or y1 <= y0:
        return img
    yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64)
    d = np.hypot(xx - cx, yy - cy)
    row = np.round(RING_FRONT - (d - r)).astype(int)
    th = np.arctan2(yy - cy, xx - cx)
    col = np.mod(np.round(th * max(r, 1)), RING_W).astype(int)
    ok = (row >= 0) & (row < RING_H) & clip(xx, yy)
    s = np.asarray(strip)[..., :3].astype(np.float64)
    add = np.where(ok[..., None], s[np.clip(row, 0, RING_H - 1), col], 0) * alpha
    a[y0:y1, x0:x1, :3] += add
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def review():
    import backdrop_l25 as b25                              # noqa: E402  (tools/art: the trench, its walls)
    body, glow, death, deadh, plume, ring, shim = (kit.load(n) for n in (
        "siren", "siren-glow", "siren-death", "siren-dead", "siren-plume", "siren-ring", "siren-shimmer"))
    burst = artkit.load_frames("explosion-deep-medium") or artkit.load_frames("explosion-deep-small")
    rows = [("LEFT WALL (FACING RIGHT): IDLE, BREATH, TELL X 3, SONG", body[0:6], 2, False),
            ("RIGHT WALL (FACING LEFT): THE SAME", body[6:12], 2, False),
            ("SAC AND FRILL GLOW (ADDITIVE, UNGRADED)", glow[0:6], 2, True),
            ("DEATH: THE SAC COLLAPSING (BOTH WALLS), THE DARK HUSK", death + deadh, 2, False),
            ("THE PALE PLUME (STEPPED)", plume, 2, False),
            ("RING STRIP (ADDITIVE; ROW 7 THE WAVEFRONT), 8 RIPPLE FRAMES", ring, 3, True),
            ("THE SLOWED HULL'S SHIMMER (ADDITIVE)", shim, 2, True)]
    sheet = artkit.review_sheet("SIREN - FINAL (ROUND 48 LOOK B, FRILL BELL)", rows, width=1000, batch=BATCH)
    pieces = b25.placed_pieces()
    sac = [np.array(sac_pivot(f)) - np.array(SIZE) / 2 for f in range(2)]
    # nests: (entry t on the level's clock, side, kill t (relative) or None); sing 1.2 s after entry
    nests = [(T0 + 0.2, 1, None), (T0 + 1.7, 0, None), (T0 + 4.4, 1, 6.9), (T0 + 5.6, 0, None)]

    def nest_pos(te, side, t):
        row = b25.scroll(te) + 28
        y = b25.scroll(t) - 1 - row
        fx = float(b25.foot(side, row))
        return np.array((fx + (6 if side == 0 else -6), y))

    def clip_for(t):
        base = b25.scroll(t)

        def clip(xx, yy):
            rows = base - 1 - yy
            xl = np.nan_to_num(b25.foot(0, rows), nan=0.0)
            xr = np.nan_to_num(b25.foot(1, rows), nan=480.0)
            return (xx > xl) & (xx < xr)
        return clip

    slow_until, immune_until, hits = -1.0, -1.0, set()
    frames = []
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        lt = T0 + t
        img = b25.composite(lt, pieces, ship=False)
        glows = []
        px, py = ship_x(t), SHIP_Y
        for i, (te, side, kill) in enumerate(nests):
            if lt < te - 0.6:
                continue
            p = nest_pos(te, side, lt)
            age = lt - te
            if kill is not None and t >= kill:
                s = t - kill
                if s < 0.34:
                    kit.paste_c(img, kit.grade(death[side * 4 + min(3, int(s * 12))], 4), *p)
                else:
                    kit.paste_c(img, kit.grade(deadh[side], 4), *p)
                if s < 0.8:
                    kit.paste_c(img, plume[min(7, int(s * 10))], p[0] + sac[side][0], p[1] + sac[side][1] - 20 * s)
                if burst and int(s * 20) < len(burst):
                    glows.append((burst[int(s * 20)], p + sac[side]))
                continue
            if 0.4 <= age < 1.2:
                k = 2 + min(2, int((age - 0.4) / 0.27))
            elif 1.2 <= age < 1.35:
                k = 5
            else:
                k = int(lt * 2) % 2
            kit.paste_c(img, kit.grade(body[side * 6 + k], 4), *p)
            glows.append((glow[side * 6 + k], p))
        img = img.copy()
        for g, p in glows:
            img = kit.add_c(img, g, *p)
        clip = clip_for(lt)
        for i, (te, side, kill) in enumerate(nests):
            tr = te + 1.2
            if lt < tr or (kill is not None and T0 + kill <= tr):
                continue
            r = (lt - tr) * RING_V
            if r > RING_R:
                continue
            o = nest_pos(te, side, lt) + sac[side]
            alpha = 1.0 - max(0.0, (r - RING_R * 0.75) / (RING_R * 0.25))
            img = draw_ring(img, ring[int(lt * 12) % 8], o[0], o[1], r, alpha, clip)
            dd = np.hypot(px - o[0], py - o[1])
            if abs(dd - r) < 5 + 8 and i not in hits and clip(np.array([px]), np.array([py]))[0]:
                hits.add(i)
                if t >= immune_until:
                    slow_until, immune_until = t + 2.0, t + 5.0
        img.alpha_composite(kit.ship(2), (int(px - 24), int(py - 24)))
        if t < slow_until:
            img = kit.add_c(img, shim[int(t * 12) % 8], px, py)
            raster.draw_text(img, int(px) + 26, int(py) - 4, "SLOWED", (150, 255, 210))
        elif t < immune_until:
            raster.draw_text(img, int(px) + 26, int(py) - 4, "IMMUNE", (150, 170, 180))
        raster.draw_text(img, 8, 8, "SIREN B - L25 T={:.1f}".format(lt), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [frames[int(t * FPS)] for t in (1.1, 2.4, 7.2)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: A TELL (T+1.1), A RING CROSSING THE TRENCH (T+2.4), A NEST SHOT "
                                            "(T+7.2); 2X CROP; LEVEL 25'S WALLS, REVIEW GRADE 0.14", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    crop = frames[int(1.1 * FPS)].crop((300, 0, 480, 202)).resize((240, 270), Image.Resampling.NEAREST)
    big.alpha_composite(crop.convert("RGBA"), (16 + 3 * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "siren", fps=FPS)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
