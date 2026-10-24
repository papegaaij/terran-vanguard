#!/usr/bin/env python3
"""Production art: Rook's loss at the end of Level 26 (design/campaign/act-4-deep-water/level-26-blackwater, *Rook's
loss*; design/player/wingmen, *Rook's loss and absence*; M8 part F batch, concept round 55, step ART-F; straight to
production, no concept of its own: designed to the README, round 48's Q7 a and the gap check m8f §1.5, §5 F6/F7).

Three thin torpedo trails rise from the bottom edge (white-orange against the black, the Depth Hunters themselves never
drawn), converge on Rook's lamp: three flashes; the lamp flickers and his craft spins down off the bottom edge in a long
spiral, 2.5 s, going dark. Harmless to the player: drawn only (no hit boxes). The craft is act4_gear.py's hydro-kit
refit of Rook's Ember (``kit_model("c")``, imported unchanged), seen through the Stormhawk's camera as ``rook-hydro``.

Outputs (assets/sprites/; ``rook-*`` is always claimed):
  rook-spin_0..15         42x42, normal: Rook's hydro craft tumbling (yaw k x 22.5 degrees clockwise, the roll swinging
                          +-35 degrees twice a turn), the hull scorched and holed, the lamp dark: the spin-down plays it
                          at 12 fps (a turn in 1.33 s) while the game moves him down and off the bottom edge in 2.5 s
                          and dims him toward the black (going dark)
  rook-spin-glow_0..15    42x42, additive, aligned: the failing lamp's light and a fire-red glow from the torn engine
                          per spin frame; the game flickers it (on, off, a stutter) and lets it die over the spin
  rook-trail_0..7         16x64, additive: a torpedo's trail as a vertical strip the game repeats every 64 px from the
                          torpedo's head down to the bottom edge, along the trail's line (x the strip's centre): a thin
                          white-orange core in a turbulent orange-red wake, 8 frames of the wake's churn at 12 fps;
                          faded by the game toward the tail
  rook-torpedo_0..3       16x16, additive: the torpedo's motor at the trail's head (white core, orange flare, flicker)
  rook-flash_0..9         64x64, additive: a hit's flash under water (a white core, an orange fireball, a pale pressure
                          ring opening), 20 fps; three on Rook 0.1 s apart at the loss
  assets/pivots/rook-spin.json   per spin frame the lamp's centre (the 56 px light pool's centre while it lasts)
  design/campaign/act-4-deep-water/level-26-blackwater/concept/rook-loss-final-r55-a.png/.gif   the review sheet and GIF
                          (the black water of section 6 in the dark look's stand-in: Rook's lamp beside the ship, the
                          trails rising in their lanes, the flashes, the spin-down, the dark)

Run: python3 tools/art/rook_loss.py [--review]   (~1 min; --review only rebuilds the review files; run backdrop_l26.py
first: the review stands on its black water)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import act4_gear
import artkit
import l26kit as kit
import rook
import stormhawk
from artkit import DESIGN, TAU

from render import raster, sprite  # noqa: E402
from render.enemy_rigs import model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "rook_loss.py"
BATCH = "M8 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r55"
artkit.REVIEW_ROUND = ROUND
LEVEL = DESIGN / "campaign" / "act-4-deep-water" / "level-26-blackwater"
CONCEPT = LEVEL / "concept"
SIZE = rook.SIZE
SPIN = 16
LAMP_MODEL = (0.12, 0.58, 0.045)              # act4_gear._kit_parts: Rook's lamp (mirrored: one each side)
ENGINE_MODEL = (0.15, -0.72, 0.0)
TRAIL_W, TRAIL_H = 16, 64
ORANGE = np.array((255, 140, 50), float)
WHITE_HOT = np.array((255, 236, 200), float)
FIRE = np.array((255, 70, 30), float)
LAMP_COL = np.array((255, 214, 150), float)


def _spin_angles(k):
    yaw = TAU * k / SPIN
    roll = np.radians(35) * np.sin(2 * yaw)
    return yaw, roll


def _scorched(mats, n0):
    """The hull scorched and the lamp off: albedo darkened, a burn pattern; emissions out but the nav lights dim."""
    out = []
    for i, m in enumerate(mats):
        burn = (lambda p, n: np.where(np.sin(p[:, 0] * 9 + p[:, 1] * 5) + np.cos(p[:, 1] * 11) > 0.6, 0.35, 1.0))
        alb = tuple(float(c) * 0.62 for c in m.albedo)
        out.append(replace(m, albedo=alb, emission=(0.0, 0.0, 0.0), spec=m.spec * 0.5,
                           pattern=burn if m.pattern is None else m.pattern))
    return out


def _frame(job):
    kind, k = job
    yaw, roll = _spin_angles(k)
    scene, mats = act4_gear.kit_model("c", roll)
    n0 = len(mats)
    if kind == "body":
        mats = _scorched(mats, n0)
    else:
        mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    rot = -yaw
    sc = (lambda p, s=scene: s(rotate_z(p, rot)))
    mats = model_space_materials(mats, rot)
    hi, f = artkit.render_hi(artkit.perspective(sc, stormhawk.CAMERA), mats, (SIZE, SIZE), stormhawk.EXTENT,
                             shadows=kind == "body")
    if kind == "body":
        return artkit.native(hi, f)
    lo = sprite.downsample(hi, f)
    return np.clip(lo[..., :3] * lo[..., 3:4], 0, 1)


def lamp_point(k):
    """The lamp's centre (both lamps' midpoint) of spin frame k in the frame's px (approximate: the camera's warp is
    small at the lamp's height)."""
    yaw, _ = _spin_angles(k)
    p = rotate_z(np.array([[0.0, LAMP_MODEL[1], LAMP_MODEL[2]]]), yaw)[0]
    px = SIZE / stormhawk.EXTENT
    return [round(SIZE / 2 + p[0] * px, 1), round(SIZE / 2 - p[1] * px, 1)]


def engine_point(k):
    yaw, _ = _spin_angles(k)
    p = rotate_z(np.array([[0.0, ENGINE_MODEL[1], 0.0]]), yaw)[0]
    px = SIZE / stormhawk.EXTENT
    return SIZE / 2 + p[0] * px, SIZE / 2 - p[1] * px


def spin_glow(em, k):
    """The failing lamp's light (a warm core and a stepped halo at the lamp, the render's lamp emission where it shows)
    and the torn engine's red fire glow."""
    yy, xx = np.mgrid[0:SIZE, 0:SIZE].astype(np.float64) + 0.5
    lum = em.max(-1)
    lx, ly = lamp_point(k)
    r = np.hypot(xx - lx, yy - ly)
    lamp = np.maximum(np.clip(lum * 6.0, 0, 1) * (r < 6), np.exp(-(r / 1.4) ** 2))
    halo = np.exp(-(r / 5.0) ** 2) * 0.55
    ex, ey = engine_point(k)
    fire = np.exp(-((xx - ex) ** 2 + (yy - ey) ** 2) / 10.0) * 0.8
    v_l = np.clip(lamp + halo, 0, 1)
    rgb = LAMP_COL[None, None] * artkit.ordered_dither(v_l, 6)[..., None] + \
        FIRE[None, None] * artkit.ordered_dither(np.clip(fire, 0, 1), 5)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((SIZE, SIZE), 255.0)])
                                           .astype(np.uint8)))


def _out(v, rgb, w, h):
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def trail_frame(i, n=8):
    """The trail strip: a thin hot core, a turbulent orange wake round it churning along (periodic in rows and frames)."""
    w, h = TRAIL_W, TRAIL_H
    ss = 4
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64) / ss + 0.125
    ph = TAU * i / n
    wob = 0.9 * np.sin(TAU * yy / h * 2 + ph) + 0.5 * np.sin(TAU * yy / h * 5 - ph * 2)
    d = np.abs(xx - w / 2 - wob * 0.4)
    core = np.clip(1 - d / 0.9, 0, 1)
    churn = 0.55 + 0.45 * np.sin(TAU * yy / h * 3 + ph + 2.0 * np.sin(xx * 0.7)) * np.cos(TAU * yy / h * 4 - ph)
    wake = np.clip(1 - np.abs(xx - w / 2 - wob) / (3.6 + 1.2 * churn), 0, 1) * (0.35 + 0.35 * churn)
    core = core.reshape(h, ss, w, ss).mean(axis=(1, 3))
    wake = wake.reshape(h, ss, w, ss).mean(axis=(1, 3))
    q = artkit.ordered_dither(np.clip(wake, 0, 1), 6)
    rgb = ORANGE[None, None] * q[..., None] * 0.85 + WHITE_HOT[None, None] * np.clip(core * 1.2, 0, 1)[..., None]
    return _out(None, rgb, w, h)


def torpedo_frame(i):
    w = 16
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - w / 2, (yy - w / 2) * 0.8)
    k = [1.0, 0.8, 0.95, 0.7][i]
    core = np.exp(-(r / 1.3) ** 2)
    flare = np.exp(-(r / (3.5 + 0.8 * k)) ** 2) * 0.8 * k
    v = artkit.ordered_dither(np.clip(flare, 0, 1), 6)
    rgb = ORANGE[None, None] * v[..., None] + WHITE_HOT[None, None] * np.clip(core * 1.3, 0, 1)[..., None]
    return _out(None, rgb, w, w)


def flash_frame(i, n=10):
    w = 64
    u = i / (n - 1)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - w / 2, yy - w / 2)
    noise = np.random.default_rng(2741 + i).normal(0, 1, (w, w))
    core = np.exp(-(r / (3 + 6 * u)) ** 2) * (1 - u) ** 1.2
    ball = np.exp(-(r / (6 + 12 * u)) ** 2) * (1 - u) ** 0.8 * (0.85 + 0.1 * noise)
    ring = np.clip(1 - np.abs(r - (6 + 24 * u)) / 1.5, 0, 1) * (1 - u) * 0.6
    vb = artkit.ordered_dither(np.clip(ball, 0, 1), 7)
    rgb = (ORANGE[None, None] * vb[..., None] + WHITE_HOT[None, None] * np.clip(core * 1.4, 0, 1)[..., None]
           + np.array((150, 190, 210.0))[None, None] * artkit.ordered_dither(ring, 4)[..., None])
    return _out(None, rgb, w, w)


def build():
    jobs = [("body", k) for k in range(SPIN)] + [("glow", k) for k in range(SPIN)]
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_frame, jobs))
    kit.write("rook-spin", artkit.quantize_set(out[:SPIN], rook.HULL_COLOURS), SOURCE)
    kit.write("rook-spin-glow", [spin_glow(em, k) for k, em in enumerate(out[SPIN:])], SOURCE)
    kit.write("rook-trail", [trail_frame(i) for i in range(8)], SOURCE)
    kit.write("rook-torpedo", [torpedo_frame(i) for i in range(4)], SOURCE)
    kit.write("rook-flash", [flash_frame(i) for i in range(10)], SOURCE)
    artkit.write_pivots("rook-spin", SOURCE, {"frame": [SIZE, SIZE], "lamp": [lamp_point(k) for k in range(SPIN)],
                                              "fps": 12, "spin_seconds": 2.5})


# =========================================================================== review

FPS, T0, DUR = 12, 155.6, 6.0
T_TRAILS, T_LOSS, SPIN_S = 157.0, 157.5, 2.5
SHIP_X, SHIP_Y = 230.0, 444.0


def rook_xy(lt):
    return SHIP_X + 58 + 6 * np.sin(lt * 1.3), SHIP_Y + 22


def review():
    import backdrop_l26 as b26                              # noqa: E402  (the black water)
    spin, sglow, trail, torp, flash = (kit.load(n) for n in ("rook-spin", "rook-spin-glow", "rook-trail",
                                                            "rook-torpedo", "rook-flash"))
    piv = json.loads((artkit.PIVOTS / "rook-spin.json").read_text(encoding="utf-8"))
    hydro = artkit.load_frames("rook-hydro")
    rows = [("ROOK-SPIN: THE TUMBLE, 16 FRAMES (12 FPS), SCORCHED, LAMP DARK", spin, 2, False),
            ("ROOK-SPIN-GLOW: THE FAILING LAMP AND THE TORN ENGINE'S FIRE (ADDITIVE)", sglow, 2, True),
            ("ROOK-TRAIL STRIP (16X64, REPEATED ALONG THE TRAIL) / ROOK-TORPEDO", trail + torp, 2, True),
            ("ROOK-FLASH: A HIT UNDER WATER (20 FPS)", flash, 1, True)]
    sheet = artkit.review_sheet("ROOK'S LOSS - FINAL (LEVEL 26, Q7 A)", rows, width=1000, batch=BATCH)
    pieces = b26.placed_pieces()
    frames = []
    trails = None
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        lt = T0 + t
        rx, ry = rook_xy(min(lt, T_LOSS))
        adds, solids, lamps = [], [], []
        rook_drawn = None
        if lt < T_LOSS:
            rook_drawn = (rx, ry)
        else:
            s = (lt - T_LOSS) / SPIN_S
            if s <= 1.0:
                u = s * s
                k = int((lt - T_LOSS) * 12) % SPIN
                sx = rx + 40 * np.sin(s * 5.0) * (1 - s * 0.3)
                sy = ry + u * 220
                fade = 1 - s
                body = spin[k].copy()
                a = np.array(body).astype(np.float64)
                a[..., :3] *= 0.4 + 0.6 * fade
                solids.append((Image.fromarray(a.astype(np.uint8), "RGBA"), sx, sy, 0))
                on = [1, 1, 0, 1, 0, 0, 1, 0, 0, 0][int((lt - T_LOSS) * 6) % 10] * fade
                adds.append((sglow[k], sx, sy, 0.3 + 0.7 * on))
                if on > 0.05:
                    lx, ly = piv["lamp"][k]
                    lamps.append((sx + lx - SIZE / 2, sy + ly - SIZE / 2, kit.ROOK_LAMP * (0.4 + 0.6 * fade),
                                  0.9 * on, kit.LAMP))
        if lt >= T_TRAILS and lt < T_LOSS + 0.1:
            if trails is None:                               # lanes: Rook's x and x +- 48 (none within 40 px of the ship)
                xs = [rx - 48, rx, rx + 48]
                xs = [x if abs(x - SHIP_X) >= 40 else rx + (rx - x) + (96 if x < rx else -96) for x in xs]
                trails = [(x, 540.0 + 10) for x in xs]
            u = np.clip((lt - T_TRAILS) / (T_LOSS - T_TRAILS), 0, 1)
            for x0, y0 in trails:
                hx, hy = x0 + (rx - x0) * u ** 2, y0 + (ry - y0) * u
                d = np.hypot(hx - x0, hy - y0)
                ang = np.arctan2(hx - x0, -(hy - y0))
                strip = trail[int(lt * 12) % 8]
                L = int(d)
                if L > 4:
                    rep = Image.new("RGBA", (TRAIL_W, L), (0, 0, 0, 255))
                    for oy in range(0, L, TRAIL_H):
                        rep.paste(strip, (0, oy))
                    a = np.array(rep).astype(np.float64)
                    a[..., :3] *= np.linspace(1.0, 0.25, L)[:, None, None]
                    rep = Image.fromarray(a.astype(np.uint8), "RGBA").rotate(-np.degrees(ang), expand=True,
                                                                              resample=Image.Resampling.NEAREST)
                    adds.append((rep, (hx + x0) / 2, (hy + y0) / 2, 1.0))
                adds.append((torp[int(lt * 12) % 4], hx, hy, 1.0))
        for j in range(3):
            ft = lt - (T_LOSS + 0.1 * j)
            if 0 <= ft < 0.5:
                adds.append((flash[min(9, int(ft * 20))], rx + (j - 1) * 8, ry + (j % 2) * 6, 1.0))
        img = b26.composite(lt, pieces, real=t, ship=(SHIP_X, SHIP_Y), rook=rook_drawn, solids=solids,
                            glows=adds, lamps=lamps)
        raster.draw_text(img, 8, 8, "L26 ROOK'S LOSS  T={:.1f}".format(lt), raster.LABEL)
        if T_TRAILS <= lt < T_TRAILS + 2.5:
            raster.draw_text(img, 8, 520, "VARGA: CONTACTS BELOW!", (255, 200, 120))
        frames.append(img.convert("RGB"))
    stills = [(1.0, "ROOK'S LAMP BESIDE THE SHIP"), (1.7, "THE TRAILS RISING"), (2.0, "THE FLASHES"),
              (3.0, "THE SPIN-DOWN"), (5.5, "THE DARK")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 250 + 240), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 2/5 IN THE DARK LOOK'S STAND-IN (SECTION 6, AMBIENT 6 %; HYDRO-KIT ROOK "
                                            "BEFORE THE LOSS)", raster.LABEL)
    for i, (tt, cap) in enumerate(stills):
        f = frames[int(tt * FPS)]
        big.alpha_composite(f.convert("RGBA").resize((192, 216), Image.Resampling.BOX), (16 + i * 196, sheet.height + 14))
        raster.draw_text(big, 16 + i * 196, sheet.height + 234, cap[:30], raster.LABEL_DIM)
    y1 = sheet.height + 250
    raster.draw_text(big, 16, y1, "1:1 CROPS ROUND ROOK: THE TRAILS CONVERGING, THE FLASHES, THE SPIN-DOWN WITH THE LAMP "
                                  "FAILING", raster.LABEL)
    for i, tt in enumerate((1.8, 2.0, 2.6, 3.3)):
        f = frames[int(tt * FPS)]
        cx, cy = rook_xy(T_LOSS)
        c = f.crop((int(cx) - 100, int(cy) - 130, int(cx) + 100, int(cy) + 76))
        big.alpha_composite(c.convert("RGBA"), (16 + i * 208, y1 + 14))
    del hydro
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "rook-loss",
                       fps=FPS)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
