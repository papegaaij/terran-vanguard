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

``--rear`` (M7 part G batch, step ART-G2, concept round 43): the Rear-guard Drone Bay
(design/player/weapons/rear-guard-drone-bay; the drone of design/player/wingmen#rear-guard-drone,
"the light drone's 14x14 body with a rear-facing gun") instead:
  sprites/rear-guard-drone-bay-drone_0..15  14x14, the light drone's disc with a twin gun on a raised
                                        housing and blue fins on the other side, 16 headings with the
                                        light drone's convention: frame k has its GUN k x 22.5 degrees
                                        clockwise from up, so the trail station firing straight back
                                        draws frame 8 (a swing in a turn may pick 7 or 9); 20 colours
  sprites/pod-rear-guard-bay-<left|right>_0..4  the bay with its dock ring at the BACK of the pod
                                        (the drone launches backwards) and a blue nose cap, cut as above;
                                        its entries in pivots/pods.json
  design/player/weapons/rear-guard-drone-bay/concept/rear-guard-drone-final-r43-a.png/.gif   review
The shot is the pulse bolt (the data's ``vfx: pulse``, drawn smaller by the game) and the break and
rebuild are the light drone's frames (``light-drone-bay-break``, ``-rebuild``), so --rear writes no
shot or effect frames. Run: python3 tools/art/light_drone.py --rear [--review]   (~30 s)
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


# =========================================================================== --rear: the Rear-guard Drone Bay

REAR_ROOT = "rear-guard-drone-bay"
REAR_POD = "rear-guard-bay"
REAR_CONCEPT = DESIGN / "player" / "weapons" / "rear-guard-drone-bay" / "concept"
REAR_BATCH, REAR_ROUND = "M7 part G batch", "r43"
REAR_SOURCE = artkit.source_note(SCRIPT, REAR_BATCH)


def drone_rear(lit=1.0):
    """The light drone's disc (``gear_r36.drone_a``'s hull, rim band and dome) with a twin gun on a
    raised housing at +y (its gun heading) and two blue fins at -y; the lens looks along the guns."""
    from render.sdf import mirror_x, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere, union

    def scene(p):
        q = mirror_x(p)
        return union(
            (sd_ellipsoid(p, (0, 0, 0.0), (0.6, 0.6, 0.18)), 0),
            (sd_cylinder_z(p, (0, 0, 0.0), 0.64, 0.05), 2),
            (sd_sphere(p, (0, -0.1, 0.1), 0.27), 1),
            (sd_sphere(p, (0, 0.06, 0.25), 0.1), 3),
            (sd_box(p, (0, 0.34, 0.1), (0.24, 0.13, 0.07), 0.04), 1),
            (sd_capsule(q, (0.13, 0.3, 0.13), (0.13, 0.84, 0.13), 0.065), 4),
            (sd_sphere(q, (0.13, 0.84, 0.13), 0.05), 3),
            (sd_box(q, (0.3, -0.5, 0.06), (0.07, 0.17, 0.04), 0.03), 2),
            k=0.02)
    return scene, g._drone_mats(lit)


def rear_bay(_):
    """The bay: bay ``a``'s body turned round (the dock ring and its lit pad at the back, the
    drone launches backwards) with a blue nose cap and a dark launch slot behind the ring."""
    from render.sdf import sd_box, sd_cylinder_z, sd_ellipsoid, union

    def scene(p):
        return union(
            (sd_box(p, (0, 0.1, 0), (0.28, 0.6, 0.14), 0.12), 0),
            (sd_ellipsoid(p, (0, 0.6, 0.04), (0.22, 0.2, 0.12)), 2),
            (sd_cylinder_z(p, (0, -0.3, 0.12), 0.26, 0.05), 1),
            (sd_cylinder_z(p, (0, -0.3, 0.16), 0.15, 0.03), 3),
            (sd_box(p, (0, -0.62, 0.06), (0.12, 0.06, 0.05), 0.02), 1),
            k=0.02)
    return scene, g._drone_mats(1.0)


def rear_frame(k):
    scene, mats = drone_rear()
    theta = TAU * k / HEADINGS
    hi = sdf.render(lambda p: scene(sdf.rotate_z(p, -theta)), mats, (DRONE * 8, DRONE * 8), EXTENT)
    return artkit.native(hi, 8)


def rear_drones():
    from concurrent.futures import ProcessPoolExecutor
    with ProcessPoolExecutor() as pool:
        frames = list(pool.map(rear_frame, range(HEADINGS)))
    return artkit.quantize_set(frames, 20)


def build_rear():
    with act3kit.sprites_of(REAR_ROOT):
        artkit.write_frames(f"{REAR_ROOT}-drone", rear_drones(), REAR_SOURCE)
    frames, offsets = act3kit.render_pods("light_drone", "rear_bay", None, BAY_SCALE)
    act3kit.write_pods(REAR_POD, frames, offsets, REAR_SOURCE)
    print(f"rear-guard drone: {act3kit.where(REAR_ROOT)}; pods in assets/sprites")


def review_rear():
    import json
    with act3kit.sprites_of(REAR_ROOT):
        drone = artkit.load_frames(f"{REAR_ROOT}-drone")
    with act3kit.sprites_of("light-drone-bay"):
        light = artkit.load_frames("light-drone-bay-drone")
        brk = artkit.load_frames("light-drone-bay-break")
        reb = artkit.load_frames("light-drone-bay-rebuild")
    bolt0 = artkit.load_frames("pulse-bolt")[0]
    bolt = bolt0.resize((max(1, round(bolt0.width * 0.45)), round(bolt0.height * 0.45)), Image.LANCZOS)
    bolt = bolt.transpose(Image.FLIP_TOP_BOTTOM)
    orb = artkit.load_frames("orb")[0]
    bays = [act3kit.pod_composite(b, REAR_POD) for b in range(5)]
    pods = json.loads((artkit.PIVOTS / "pods.json").read_text(encoding="utf-8"))["offsets"]
    pod_r = artkit.load_frames(f"pod-{REAR_POD}-right")[2]
    ox, oy = pods[f"pod-{REAR_POD}-right"][2]
    # the dock ring's centre on the hull (the pod's back third), from the ship's centre
    dock = (ox + pod_r.width / 2 - 24, oy + pod_r.height * 0.78 - 24)
    ground = g.r36.mars_ground(97, craters=8, dunes=0.02)
    fps, dt = 30, 1 / 30
    behind, side, lag = 40.0, 12.0, 0.15
    broke_at, rebuild = 3.2, 1.6                   # the review's break and a shortened rebuild
    frames, bolts, shots_in = [], [], []
    pos = None
    hits = {"left": [], "right": []}
    rng = np.random.default_rng(4320)
    for i in range(fps * 6):
        t = i / fps
        sx, sy = 240 + 70 * np.sin(t * 1.1), 330
        vx = 70 * 1.1 * np.cos(t * 1.1)
        bank = 2 if abs(vx) < 25 else ((1 if vx < -55 else 0) if vx < 0 else (3 if vx < 55 else 4))
        target = {"left": (sx - side, sy + behind), "right": (sx + side, sy + behind)}
        if pos is None:
            pos = dict(target)
        a = 1 - np.exp(-dt / lag)
        pos = {s: (p[0] + (target[s][0] - p[0]) * a, p[1] + (target[s][1] - p[1]) * a) for s, p in pos.items()}
        right_gone = broke_at <= t < broke_at + rebuild + 0.3
        img = ground.copy()
        # bullets from behind (orange orbs, rising), some blocked by the drones
        if i % 9 == 0 and t < 5.0:
            shots_in.append([pos["right" if (i // 9) % 3 else "left"][0] + rng.uniform(-3, 3), 560.0])
        for b in shots_in:
            b[1] -= 260 * dt
        live = []
        for b in shots_in:
            hit = None
            for s, p in pos.items():
                if s == "right" and right_gone:
                    continue
                if abs(b[0] - p[0]) < 5 and abs(b[1] - p[1]) < 5:
                    hit = s
            if hit:
                hits[hit].append(t)
            elif b[1] > -10:
                live.append(b)
        shots_in[:] = live
        for b in shots_in:
            sprite.paste_center(img, orb, b[0], b[1])
        if i % 5 == 0:
            for s, p in pos.items():
                if not (s == "right" and right_gone):
                    bolts.append([p[0], p[1] + 6])
        for b in bolts:
            b[1] += 900 * dt
        bolts[:] = [b for b in bolts if b[1] < 560]
        for b in bolts:
            img = artkit.add_light(img, bolt, (int(b[0] - bolt.width / 2), int(b[1] - bolt.height / 2)))
        hull = bays[bank]
        sprite.paste_center(img, sprite.shadow_of(hull, opacity=0.6, blur=1.0, scale=0.85), sx + 21, sy + 30)
        sprite.paste_center(img, hull, sx, sy)
        for s, p in pos.items():
            sp = drone[8]
            if s == "right" and right_gone:
                since = t - broke_at - rebuild
                if since >= 0:
                    k = min(len(reb) - 1, int(since / 0.05))
                    img = artkit.add_light(img, reb[k], (int(sx + dock[0] - 12), int(sy + dock[1] - 12)))
                continue
            fade = 1.0
            if s == "right" and broke_at + rebuild + 0.3 <= t < broke_at + rebuild + 0.6:
                fade = (t - broke_at - rebuild - 0.3) / 0.3
            if any(0 <= t - h < 0.1 for h in hits[s]):
                sp = g.flash_sprite(sp)
            if fade < 1:
                a = np.array(sp)
                a[..., 3] = (a[..., 3] * fade).astype(np.uint8)
                sp = Image.fromarray(a, "RGBA")
            sprite.paste_center(img, sprite.shadow_of(sp, opacity=0.5 * fade, blur=0.8, scale=0.85),
                                p[0] + 21, p[1] + 30)
            sprite.paste_center(img, sp, p[0], p[1])
        age = t - broke_at
        if 0 <= age < len(brk) * 2 / 60:
            p = pos["right"]
            img = artkit.add_light(img, brk[int(age * 30)], (int(p[0] - 16), int(p[1] - 16)))
        g.label(img, 8, 540 - 14, "REAR-GUARD DRONE BAY X2: TRAIL 40 PX, LAG 0.15 S, FIRE BACK, BLOCK, BREAK, REBUILD",
                (150, 200, 240))
        frames.append(img)
    pair = [light[0], drone[0], light[8], drone[8]]
    rows = [("REAR-GUARD-DRONE-BAY-DRONE: 16 GUN HEADINGS FROM UP, CLOCKWISE (TRAIL FIRES WITH FRAME 8)", drone, 4, False),
            ("LIGHT DRONE VS REAR-GUARD DRONE (FRAMES 0 AND 8)", pair, 6, False),
            ("POD-REAR-GUARD-BAY ON EVERY BANK FRAME (HULL + POD SPRITES AT THEIR PIVOTS)", bays, 4, False),
            ("IN FLIGHT (1X)", [frames[k].crop((140, 230, 340, 430)) for k in (15, 60, 97, 140, 175)], 1, False)]
    sheet = artkit.review_sheet("REAR-GUARD DRONE BAY - FINAL SPRITES", rows, width=1400, batch=REAR_BATCH)
    artkit.save_review(sheet, [f.crop((80, 150, 400, 540)) for f in frames], REAR_CONCEPT, "rear-guard-drone", fps=fps)


if __name__ == "__main__":
    if "--rear" in sys.argv[1:]:
        artkit.REVIEW_ROUND = REAR_ROUND
        if "--review" not in sys.argv[1:]:
            build_rear()
        review_rear()
        sys.exit(0)
    artkit.REVIEW_ROUND = act3kit.ROUND
    if "--review" not in sys.argv[1:]:
        build()
    review()
