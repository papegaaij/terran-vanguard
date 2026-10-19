#!/usr/bin/env python3
"""Production art: the Light Drone Bay (design/player/weapons/light-drone-bay, the drone of
design/player/wingmen; M7 part A batch, concept round 37), the look round 36 chose (G.2 = a): the
disc drone and the dock-ring bay of tools/concept/gear_r36.py (``drone_a``, ``bay_model("a")``,
imported unchanged).

Outputs (assets/):
  sprites/light-drone-bay-drone_0..15   14x14 disc drone facing 16 headings, k x 22.5 degrees
                                        clockwise from up (it faces the convergence point), each its
                                        own render with the key light fixed; 20 colours, one palette.
                                        The block flash and the rebuild fade are the game's tint/alpha
  sprites/light-drone-bay-shot_0..31    its shot (round 08's energy bolt, smaller: 3x8) at 32
                                        headings, k x 11.25 degrees clockwise from up; additive
  sprites/light-drone-bay-break_0..9    32x32, additive: the drone breaking (white flash, a blue-white
                                        spark ring, glowing chips thrown out), 2 steps a frame
  sprites/light-drone-bay-rebuild_0..5  24x24, additive: the bay rebuilding it (a cyan ring closing on
                                        the dock and a white spark as it is whole), 3 steps a frame
                                        (0.3 s, the data's fade_in), centred on the dock ring
  sprites/pod-drone-bay-<left|right>_0..4  the bay on each wing per banking frame, cut from a render
                                        of the Stormhawk with the bay (stormhawk.py's method)
  pivots/pods.json                      their offsets on the hull (entries added; the file stays
                                        tools/art/stormhawk.py's)
  design/player/weapons/light-drone-bay/concept/light-drone-final-r37-a.png/.gif   review

The drone's frames are claimed by the weapon's slug (vanguard.pipeline.SpriteUse) once its
data.yaml exists; until then they are written to build/art-held/sprites (act3kit).

Run: python3 tools/art/light_drone.py [--review]   (~40 s)
"""
import sys

import numpy as np
from PIL import Image

import act3kit
import artkit
from artkit import DESIGN, TAU, sdf, sprite

import vfx_r08 as v8  # noqa: E402  (concept scripts, imported unchanged)
import gear_r36 as g  # noqa: E402

SCRIPT = "light_drone.py"
SOURCE = artkit.source_note(SCRIPT, act3kit.BATCH)
ROOT_NAME = "light-drone-bay"
CONCEPT = DESIGN / "player" / "weapons" / "light-drone-bay" / "concept"
PS, WHITE = v8.PS, v8.WHITE
Canvas, gauss, solid = v8.Canvas, v8.gauss, v8.solid
DRONE, HEADINGS, EXTENT = 14, 16, 1.75
SHOT_HEADINGS = 32
BAY_SCALE = 0.5                       # bay model units -> Stormhawk model units (a 12x18 px bay)
ARC = (190, 235, 255)


def drone_frame(k):
    scene, mats = g.drone_a()
    theta = TAU * k / HEADINGS
    hi = sdf.render(lambda p: scene(sdf.rotate_z(p, -theta)), mats, (DRONE * 8, DRONE * 8), EXTENT)
    return artkit.native(hi, 8)


def drones():
    from concurrent.futures import ProcessPoolExecutor
    with ProcessPoolExecutor() as pool:
        frames = list(pool.map(drone_frame, range(HEADINGS)))
    return artkit.quantize_set(frames, 20)


def shots():
    out = [artkit.additive(v8.energy_bolt(6, 1.4, angle=TAU * k / SHOT_HEADINGS, glow=0.9, pad=3))
           for k in range(SHOT_HEADINGS)]
    return act3kit.quantize_bright(out, 16)


def break_frames(n=10):
    rng = np.random.default_rng(1543)
    chips = [(rng.uniform(0, TAU), rng.uniform(5, 13), rng.uniform(0.6, 1.0)) for _ in range(7)]
    sparks = [(rng.uniform(0, TAU), rng.uniform(8, 15)) for _ in range(12)]
    out = []
    for i in range(n):
        t = i / (n - 1)
        cv = Canvas(32, 32)
        d = cv.dist(16, 16)
        cv.add(WHITE, gauss(d, 2.5 + 3 * t) * 1.6 * (1 - t) ** 2)
        cv.add(PS[2], gauss(d, 6 + 4 * t) * 0.6 * (1 - t) ** 1.5)
        r = 3 + 11 * (1 - (1 - t) ** 2)
        cv.add(ARC, gauss(np.abs(d - r), 0.8) * 0.9 * (1 - t) ** 1.2)
        for a, reach in sparks:
            r1 = 3 + reach * (1 - (1 - t) ** 2.4)
            r0 = max(2.0, r1 - 3.5)
            seg = cv.seg(16 + np.cos(a) * r0, 16 + np.sin(a) * r0, 16 + np.cos(a) * r1, 16 + np.sin(a) * r1)
            cv.add(WHITE if reach > 12 else PS[3], gauss(seg, 0.5) * 1.1 * (1 - t) ** 0.8)
        for a, reach, s in chips:
            r1 = 2 + reach * (1 - (1 - t) ** 1.8)
            cv.add(PS[3], solid(cv.dist(16 + np.cos(a) * r1, 16 + np.sin(a) * r1 + 4 * t * t), 0.9 * s)
                   * (1 - t) * 1.2)
        out.append(artkit.additive(cv.image()))
    return act3kit.quantize_bright(out, 24)


def rebuild_frames(n=6):
    out = []
    for i in range(n):
        t = i / (n - 1)
        cv = Canvas(24, 24)
        d = cv.dist(12, 12)
        r = 11 - 8 * t
        cv.add(PS[3], gauss(np.abs(d - r), 0.7) * 0.9 * (0.5 + 0.5 * t))
        cv.add(PS[2], gauss(d, 4) * 0.35 * t)
        cv.add(PS[1], gauss(np.abs(d - r), 2.2) * 0.4)
        if i == n - 1:
            cv.add(WHITE, gauss(d, 2.2) * 1.4)
            cv.add(WHITE, gauss(np.abs(cv.y - 12), 0.5) * gauss(np.abs(cv.x - 12), 7) * 0.9)
        out.append(artkit.additive(cv.image()))
    return act3kit.quantize_bright(out, 16)


def bay(_):
    return g.bay_model("a")


def build():
    with act3kit.sprites_of(ROOT_NAME):
        artkit.write_frames("light-drone-bay-drone", drones(), SOURCE)
        artkit.write_frames("light-drone-bay-shot", shots(), SOURCE)
        artkit.write_frames("light-drone-bay-break", break_frames(), SOURCE)
        artkit.write_frames("light-drone-bay-rebuild", rebuild_frames(), SOURCE)
    frames, offsets = act3kit.render_pods("light_drone", "bay", None, BAY_SCALE)
    act3kit.write_pods("drone-bay", frames, offsets, SOURCE)
    print(f"light drone: {act3kit.where(ROOT_NAME)}; pods in assets/sprites")


# =========================================================================== review

def review():
    with act3kit.sprites_of(ROOT_NAME):
        drone = artkit.load_frames("light-drone-bay-drone")
        shot = artkit.load_frames("light-drone-bay-shot")
        brk = artkit.load_frames("light-drone-bay-break")
        reb = artkit.load_frames("light-drone-bay-rebuild")
    bays = [act3kit.pod_composite(b, "drone-bay") for b in range(5)]
    ground = g.r36.mars_ground(83, craters=8, dunes=0.02)
    fps = 30
    frames = []
    orbit, period, conv = 36.0, 2.4, 140.0
    broke_at, rebuild = 2.0, 2.0                    # the review's break and a shortened rebuild
    bolts = []
    for i in range(fps * 6):
        t = i / fps
        step = i * 2
        img = ground.copy()
        sx, sy = 240 + 40 * np.sin(t * 0.7), 430
        hull = bays[2 if abs(np.cos(t * 0.7)) < 0.5 else (1 if np.cos(t * 0.7) < 0 else 3)]
        cpt = (sx, sy - conv)
        pos = {}
        for side, a0 in (("left", np.pi), ("right", 0.0)):
            ang = a0 + TAU * t / period
            p = (sx + np.cos(ang) * orbit, sy + np.sin(ang) * orbit)
            if side == "right" and broke_at <= t < broke_at + rebuild + 0.3:
                pos[side] = None
                since = t - broke_at - rebuild
                if since >= 0:
                    k = int(since / 0.05)
                    img = artkit.add_light(img, reb[min(5, k)], (int(sx + 16 - 12), int(sy - 4 - 12)))
                continue
            pos[side] = p
        for b in bolts:
            b[0] += b[2] / fps
            b[1] += b[3] / fps
        bolts[:] = [b for b in bolts if b[1] > -20]
        for side, p in pos.items():
            if p is not None and i % 5 == (0 if side == "left" else 2):
                a = np.arctan2(cpt[0] - p[0], -(cpt[1] - p[1])) % TAU
                bolts.append([p[0], p[1], np.sin(a) * 900, -np.cos(a) * 900, a])
        for b in bolts:
            sp = shot[int(round(b[4] / TAU * SHOT_HEADINGS)) % SHOT_HEADINGS]
            img = artkit.add_light(img, sp, (int(b[0] - sp.width / 2), int(b[1] - sp.height / 2)))
        sh = sprite.shadow_of(hull, opacity=0.6, blur=1.0, scale=0.85)
        sprite.paste_center(img, sh, sx + 21, sy + 30)
        sprite.paste_center(img, hull, sx, sy)
        for side, p in pos.items():
            if p is None:
                continue
            a = np.arctan2(cpt[0] - p[0], -(cpt[1] - p[1])) % TAU
            sp = drone[int(round(a / TAU * HEADINGS)) % HEADINGS]
            sprite.paste_center(img, sprite.shadow_of(sp, opacity=0.5, blur=0.8, scale=0.85), p[0] + 21, p[1] + 30)
            sprite.paste_center(img, sp, p[0], p[1])
        age = t - broke_at
        if 0 <= age < 10 * 2 / 60:
            ang = TAU * broke_at / period
            bx, by = sx + np.cos(ang) * orbit, sy + np.sin(ang) * orbit
            img = artkit.add_light(img, brk[int(age * 30)], (int(bx - 16), int(by - 16)))
        g.label(img, 8, 540 - 14, "LIGHT DRONE BAY X2: ORBIT, FIRE AT THE CONVERGENCE POINT, BREAK, REBUILD",
                (150, 200, 240))
        frames.append(img)
    rows = [("LIGHT-DRONE-BAY-DRONE: 16 HEADINGS FROM UP, CLOCKWISE", drone, 4, False),
            ("LIGHT-DRONE-BAY-SHOT: 32 HEADINGS (ADDITIVE)", shot[:16], 4, True),
            ("LIGHT-DRONE-BAY-BREAK, 2 STEPS A FRAME", brk, 3, True),
            ("LIGHT-DRONE-BAY-REBUILD AT THE DOCK, 3 STEPS A FRAME", reb, 4, True),
            ("POD-DRONE-BAY ON EVERY BANK FRAME (HULL + POD SPRITES AT THEIR PIVOTS)", bays, 4, False),
            ("IN FLIGHT (1X)", [frames[k].crop((120, 300, 360, 520)) for k in (10, 50, 62, 120, 160)], 1, False)]
    sheet = artkit.review_sheet("LIGHT DRONE BAY - FINAL SPRITES (ROUND 36 G.2 = A)", rows, width=1400,
                                batch=act3kit.BATCH)
    artkit.REVIEW_ROUND = act3kit.ROUND
    artkit.save_review(sheet, [f.crop((60, 135, 420, 540)) for f in frames], CONCEPT, "light-drone", fps=fps)


if __name__ == "__main__":
    artkit.REVIEW_ROUND = act3kit.ROUND
    if "--review" not in sys.argv[1:]:
        build()
    review()
