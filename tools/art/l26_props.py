#!/usr/bin/env python3
"""Production art: Level 26's prop, the farm barge (design/campaign/act-4-deep-water/level-26-blackwater, "Ground
targets", "Secrets and pickups"; M8 part F batch, concept round 55, step ART-F; straight to production, no concept of
its own: designed to the README, round 48's darkness look a and Thera Deep's farm look of Level 23 (grow trays, sodium
lamps), readability rule 7 (the light rim) for the secret). The armour patch's carrier is Level 25's survey buoy
(tools/art/l25_props.py, its `look: survey-buoy`), reused unchanged; the lamp posts are backdrop pieces
(tools/art/backdrop_l26.py).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse: a
ground target's ``sprite`` once Level 26's data names it):
  farm-barge_0..2         80x48, the secret: a drowned Thera Deep farm barge on the sea floor in the dark's left side (a
                          20 HP `ground` trigger, t~84, x 60; the data's `sprite: farm-barge`, `size: [80, 48]`): a
                          flat hull lying a little askew, rows of empty grow trays on its deck, the cargo pod on its
                          stern, a short mast with the one working sodium lamp, silt drifted against its port side; 0
                          lying, 1 hit (scorched, the pod's lid dented), 2 opened (the pod's lid blown off beside it,
                          the hold dark: the hidden crate rises from it as the pickup); rule 7's light rim
  farm-barge-glow_0..7    80x48, additive, aligned: 0-3 the lamp burning (a warm core and halo, a slow sodium flicker;
                          "the only warm light" in the Blackwater), 4-7 after the first hit: the lamp stuttering and
                          sparks at the dent; drawn ungraded (the floor's warm pool round it is the game's static light)
  design/campaign/act-4-deep-water/level-26-blackwater/concept/l26-props-final-r55-a.png/.gif   the review sheet and GIF
                          (in the dark look's stand-in at t~84: the barge's lamp seen from afar among the anglers' teal
                          lures, the ship's cone finding the hull, the barge shot open and its crate rising)

Run: python3 tools/art/l26_props.py [--review]   (~30 s; --review rebuilds only the review; run backdrop_l26.py and
glow_angler.py first: the review stands on the black water and shows the lures)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
import l24_props as p24
import l26kit as kit
from artkit import DESIGN

from render import raster  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, subtract, union  # noqa: E402

SCRIPT = "l26_props.py"
BATCH = "M8 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r55"
artkit.REVIEW_ROUND = ROUND
LEVEL = DESIGN / "campaign" / "act-4-deep-water" / "level-26-blackwater"
CONCEPT = LEVEL / "concept"
BARGE = (80, 48)
AXIS = (np.cos(0.22), np.sin(0.22))           # the hull's long axis (bow to the upper right, a little askew)
SODIUM = np.array((255, 200, 120), float)
render, rimmed = p24.render, p24.rimmed


def mats(state):
    scorch = (lambda p, n: np.where(np.hypot(p[:, 0] + 18, p[:, 1] - 1) < 9, 0.35, 1.0)) if state else None
    return [Material((0.36, 0.42, 0.44), metal=0.5, shininess=40, spec=0.5, pattern=scorch),   # 0 the grey-green hull
            Material((0.62, 0.48, 0.16), metal=0.3, shininess=40, spec=0.5),                    # 1 Thera Deep amber trim
            Material((0.24, 0.26, 0.24), metal=0.2, shininess=10, spec=0.2),                    # 2 the grow trays
            Material((0.3, 0.36, 0.34), metal=0.0, shininess=8, spec=0.1),                      # 3 silt
            Material((0.03, 0.03, 0.04), metal=0.0, shininess=5, spec=0.0),                     # 4 the dark hold
            Material((0.56, 0.56, 0.52), metal=0.4, shininess=40, spec=0.5, pattern=scorch),    # 5 the pod and its lid
            Material((0.95, 0.82, 0.55), metal=0.1, shininess=120, spec=1.2),                   # 6 the lamp's lens
            Material((0.18, 0.18, 0.2), metal=0.5, shininess=30, spec=0.4)]                     # 7 dark fittings, mast


HULL = (29.0, 11.0, 4.0)                      # the hull's half extents (px): along, across, up
POD_U, LAMP_UV = -18.0, (19.0, -6.0)


def scene(state):
    ang = float(np.arctan2(AXIS[1], AXIS[0]))

    def sc(p):
        q = rotate_z(p - np.array([0.0, -1.0, 0.0]), ang)   # hull-local: x along (bow +), y across (port +)
        items = [(sd_box(q, (0, 0, HULL[2]), HULL, 2.5), 0),
                 (sd_box(q, (0, 0, 6.2), (HULL[0] + 0.4, HULL[1] + 0.4, 0.9), 0.6), 1),          # the amber gunwale
                 (sd_box(q, (2, 0, 8.4), (HULL[0] - 3, HULL[1] - 2.5, 0.5), 0.3), 7)]           # the deck
        for k in range(5):                                                                 # grow trays in rows
            items.append((sd_box(q, (-6 + k * 6.5, 0, 9.4), (2.0, HULL[1] - 4, 0.8), 0.3), 2))
        pod = sd_cylinder_z(q, (POD_U, 0, 12.0), 6.5, 4.0)
        if state == 2:
            pod = subtract((pod, 5), sd_cylinder_z(q, (POD_U, 0, 15.0), 5.0, 3.0))[0]
            items.append((sd_cylinder_z(q, (POD_U, 0, 13.0), 5.0, 1.0), 4))
            items.append((sd_box(q, (POD_U - 6, 17, 2.5), (5.5, 5.5, 0.8), 0.8), 5))     # the lid, blown off
        elif state == 1:
            items.append((subtract((sd_cylinder_z(q, (POD_U, 0, 16.4), 6.8, 0.6), 5),
                                   sd_sphere(q, (POD_U + 2, 1, 17.6), 2.8))[0], 5))
        else:
            items.append((sd_cylinder_z(q, (POD_U, 0, 16.4), 6.8, 0.6), 5))
        items.append((pod, 5))
        mu, mv = LAMP_UV
        items.append((sd_capsule(q, (mu, mv, 8.0), (mu, mv, 20.0), 0.9), 7))               # the lamp mast
        items.append((sd_box(q, (mu + 1.5, mv, 21.0), (3.0, 2.2, 1.0), 0.5), 7))           # the hood
        items.append((sd_sphere(q, (mu + 1.5, mv, 19.6), 1.8), 6))                         # the lens
        items.append((sd_ellipsoid(q, (0, 15, 1.0), (30.0, 6.0, 4.0)), 3))                 # silt against port
        items.append((sd_ellipsoid(q, (-30, 3, 0.8), (7.0, 10.0, 3.0)), 3))
        return union(*items, k=0.6)
    return sc


def _px(u, v):
    ax, ay = AXIS
    x, y = ax * u - ay * v, ay * u + ax * v - 1
    return BARGE[0] / 2 + x, BARGE[1] / 2 - y


def lamp_px():
    return _px(LAMP_UV[0] + 1.5, LAMP_UV[1])


def dent_px():
    return _px(POD_U + 2, 1)


def frame(state):
    return rimmed(render(scene(state), mats(state), BARGE, z_top=40))


def glow(i):
    w, h = BARGE
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    lx, ly = lamp_px()
    r = np.hypot(xx - lx, yy - ly)
    if i < 4:
        on = [1.0, 0.94, 1.0, 0.9][i]
    else:
        on = [0.85, 0.2, 0.7, 0.0][i - 4]
    f = (np.exp(-(r / 1.6) ** 2) + np.exp(-(r / 6.0) ** 2) * 0.5 + np.exp(-(r / 14.0) ** 2) * 0.18) * on
    rgb = SODIUM[None, None] * artkit.ordered_dither(np.clip(f, 0, 1), 8)[..., None]
    if i >= 4:                                         # sparks at the dent
        rng = np.random.default_rng(2760 + i)
        mx, my = dent_px()
        s = np.zeros((h, w))
        for _ in range(int(rng.integers(2, 5))):
            sx, sy = mx + rng.normal(0, 2.4), my + rng.normal(0, 2.0)
            s += np.exp(-((xx - sx) ** 2 + (yy - sy) ** 2) / 0.9) * rng.uniform(0.6, 1.0)
        rgb = rgb + np.array((200, 230, 255.0))[None, None] * artkit.ordered_dither(np.clip(s, 0, 1), 6)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


def build():
    with ProcessPoolExecutor() as ex:
        frames = list(ex.map(frame, range(3)))
    kit.write("farm-barge", artkit.quantize_set(frames, 40), SOURCE)
    kit.write("farm-barge-glow", [glow(i) for i in range(8)], SOURCE)


# =========================================================================== review

FPS, T0, DUR = 12, 84.0, 5.0
BARGE_T, BARGE_X = 84.0, 60.0
HIT, OPEN = 2.6, 3.0


def review():
    import backdrop_l26 as b26                                  # noqa: E402  (the black water and the scarp)
    body, gl = kit.load("farm-barge"), kit.load("farm-barge-glow")
    crate = artkit.load_frames("pickup-crate")
    abody, aglow, alure = kit.load("glow-angler"), kit.load("glow-angler-glow"), kit.load("glow-angler-lure")
    piv = json.loads((artkit.PIVOTS / "glow-angler.json").read_text(encoding="utf-8"))
    bolt = artkit.load_frames("pulse-bolt")
    rows = [("FARM BARGE: LYING, HIT, OPENED (2X)", body, 2, False),
            ("ITS LAMP (0-3) AND, AFTER THE HIT, THE STUTTER AND SPARKS (4-7), ADDITIVE", gl, 2, True)]
    sheet = artkit.review_sheet("LEVEL 26 PROPS - THE FARM BARGE (FINAL)", rows, width=1000, batch=BATCH)
    pieces = b26.placed_pieces()
    lures = [(170, 60), (300, 150), (120, 230), (210, 330)]
    frames = []
    bolts = []
    halo = kit.gold_halo()
    lx, ly = lamp_px()
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        lt = T0 + t
        y = b26.scroll(lt) - b26.scroll(BARGE_T) - BARGE[1] / 2 + 4
        ship_x = 240 - 140 * np.clip((t - 0.3) / 1.7, 0, 1)
        ship = (ship_x, 444.0)
        st = 0 if t < HIT else (1 if t < OPEN else 2)
        solids = [(body[st], BARGE_X, y, 64)]
        g = gl[int(lt * 4) % 4] if t < HIT else gl[4 + int(lt * 8) % 4]
        glows = [(g, BARGE_X, y, 1.0)]
        lamps = [(BARGE_X + lx - BARGE[0] / 2, y + ly - BARGE[1] / 2 + 8, 80, 0.8, kit.LAMP)]
        for i, (ax, ay) in enumerate(lures):
            yy = ay + 16 * t
            solids.append((abody[0], ax, yy, 40))
            glows += [(aglow[0], ax, yy, 1.0), (alure[int(lt * 8 + i) % 8],
                                                ax + piv["lure"][0][0] - 40, yy + piv["lure"][0][1] - 40, 1.0)]
        over = []
        if t >= OPEN:
            u = min(1.0, (t - OPEN) / 0.8)
            cy = y - 4 - 18 * u
            glows.append((halo, BARGE_X - 18, cy, 1.0))
            over.append((crate[int(t * 10) % len(crate)], BARGE_X - 18, cy))
        if 1.9 < t < OPEN and fi % 3 == 0:
            bolts += [[ship[0] - 9, ship[1] - 26], [ship[0] + 9, ship[1] - 26]]
        for b in bolts:
            b[1] -= 520 / FPS
        bolts = [b for b in bolts if b[1] > -20]
        for b in bolts:
            glows.append((bolt[0], b[0], b[1], 1.0))
        img = b26.composite(lt, pieces, real=t, ship=ship, rook=(ship[0] + 60, 470.0), solids=solids, glows=glows,
                            over=over, lamps=lamps)
        raster.draw_text(img, 8, 8, "L26 THE FARM BARGE  T={:.1f}".format(lt), raster.LABEL)
        frames.append(img.convert("RGB"))
    stills = [(0.2, "ITS LAMP FROM THE FAR SIDE"), (1.8, "IN THE CONE"), (2.7, "HIT"), (3.6, "OPENED, THE CRATE")]
    big = Image.new("RGBA", (sheet.width, sheet.height + 520), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2 IN THE DARK LOOK'S STAND-IN (T=84-89, THE SCARP ON THE RIGHT; "
                                            "ANGLERS' TEAL LURES FOR CONTRAST)", raster.LABEL)
    for i, (tt, cap) in enumerate(stills):
        big.alpha_composite(frames[int(tt * FPS)].convert("RGBA").resize((240, 270), Image.Resampling.BOX),
                            (16 + i * 248, sheet.height + 14))
        raster.draw_text(big, 16 + i * 248, sheet.height + 288, cap, raster.LABEL_DIM)
    y1 = sheet.height + 304
    raster.draw_text(big, 16, y1, "1:1 CROPS: THE WARM LAMP BESIDE A TEAL LURE / THE OPENED BARGE AND ITS CRATE",
                     raster.LABEL)
    for i, tt in enumerate((0.7, 3.6)):
        f = frames[int(tt * FPS)]
        yb = int(b26.scroll(T0 + tt) - b26.scroll(BARGE_T) - BARGE[1] / 2 + 4)
        c = f.crop((0, max(0, yb - 120), 300, max(0, yb - 120) + 200))
        big.alpha_composite(c.convert("RGBA"), (16 + i * 316, y1 + 14))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "l26-props",
                       fps=FPS)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
