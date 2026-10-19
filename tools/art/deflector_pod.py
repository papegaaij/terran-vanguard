#!/usr/bin/env python3
"""Production art: the Deflector Pod (design/player/weapons/deflector-pod; M7 part A batch, concept
round 37). Round 36 showed no look for it, so this was an a/b proposal; the user chose a, "field band"
(round 37 P.1 = a, 2026-10-13): CHOSEN is written to assets/, the other variant only to its review
files in concept/rejected/.

  a "field band": a small emitter pod with a cyan dome on its outer side; the field a thin cyan
    band round the half-disc with a faint glow inside it (the shield ring's colours), a caught
    bullet lights a short arc of the band white with a few sparks.
  b "plate lattice": a pod with three emitter vanes; the field drawn as five thin hexagonal-cell
    plates along the half-disc with gaps between them, a caught bullet flashes the plate it hit and
    throws a small spark ring.

Outputs of the variant written (assets/; the other variant only in its review files):
  sprites/pod-deflector-<left|right>_0..4   the pod on each wing per banking frame, cut from a render
                                            of the Stormhawk with the pod (stormhawk.py's method)
  pivots/pods.json                          their offsets (entries added; the file stays stormhawk.py's)
  sprites/deflector-pod-field-<n>-<left|right>  per level n = 1..5 and 6 = overdrive (radius 22, 24,
                                            26, 28, 30, 32 px) the field's half-disc on that side,
                                            additive, (radius + 6) x (2 radius + 12) px; the ship's
                                            centre is the middle of the sprite's inner edge (the right
                                            edge for the left field, the left edge for the right one).
                                            Drawn while the pod holds a charge; the game raises its
                                            alpha for a moment as a charge returns
  sprites/deflector-pod-catch_0..95         24x24, additive: the catch flash, 16 headings x 6 frames
                                            (index heading x 6 + frame, heading k x 22.5 degrees
                                            clockwise from up = the direction from the ship's centre
                                            to the catch point, the sprite's centre on that point),
                                            2 steps a frame
  design/player/weapons/deflector-pod/concept/deflector-pod-r37-a.png/.gif   the chosen proposal
  design/player/weapons/deflector-pod/concept/rejected/deflector-pod-r37-b.png/.gif   the rejected one

Run: python3 tools/art/deflector_pod.py [--variant a|b] [--review]   (~40 s; the other variant's
review is rendered without touching assets/)
"""
import sys

import numpy as np
from PIL import Image

import act3kit
import artkit
from artkit import DESIGN, TAU, sprite

import vfx_r08 as v8  # noqa: E402  (concept scripts, imported unchanged)
import gear_r36 as g  # noqa: E402
from render.sdf import mirror_x, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "deflector_pod.py"
SOURCE = artkit.source_note(SCRIPT, act3kit.BATCH)
ROOT_NAME = "deflector-pod"
CONCEPT = DESIGN / "player" / "weapons" / "deflector-pod" / "concept"
PS, WHITE = v8.PS, v8.WHITE
Canvas, gauss, solid = v8.Canvas, v8.gauss, v8.solid
RADII = [22, 24, 26, 28, 30, 32]
HEADINGS, CATCH_FRAMES = 16, 6
FIELD = (90, 200, 255)
PAD = 6
CHOSEN = "a"   # the user's pick, round 37 P.1 (2026-10-13): "field band"


def pod_model(v):
    """(scene, materials) in the right wing pod's local units (origin at the mount, +x outwards)."""
    import stormhawk
    mats = list(stormhawk.POD_MATS) + [v8.m_glow((60, 210, 255), 1.6)]
    G, BODY, TIP, ACC, GLOW = range(5)

    def scene(p):
        if v == "a":                    # emitter dome: a short body, a cyan dome on its outer side
            return union(
                (sd_capsule(p, (0, -0.26, 0), (0, 0.12, 0), 0.085, 0.07), BODY),
                (sd_cylinder_z(p, (0.05, -0.04, 0.02), 0.075, 0.03), G),
                (sd_sphere(p, (0.06, -0.04, 0.05), 0.06), GLOW),
                (sd_box(p, (0, 0.18, 0.0), (0.03, 0.06, 0.02), 0.01), TIP),
                k=0.012)
        q = mirror_x(p)                 # vane emitter: a flat body with three splayed vanes
        vanes = [(sd_box(p, (0.0, 0.04, 0.03), (0.018, 0.2, 0.02)), TIP),
                 (sd_capsule(q, (0.0, 0.0, 0.03), (0.11, 0.14, 0.03), 0.018), TIP)]
        tips = [(sd_sphere(p, (0.0, 0.25, 0.03), 0.03), GLOW),
                (sd_sphere(q, (0.12, 0.155, 0.03), 0.028), GLOW)]
        return union(
            (sd_box(p, (0, -0.08, 0), (0.07, 0.22, 0.04), 0.03), BODY),
            (sd_box(p, (0, -0.08, 0.03), (0.075, 0.03, 0.02), 0.01), ACC),
            *vanes, *tips, k=0.01)
    return scene, mats


def field(v, r, side, glow=1.0):
    w, h = r + PAD, 2 * r + 2 * PAD
    cx = w if side == "left" else 0
    cv = Canvas(w, h)
    dx, dy = cv.x - cx, cv.y - h / 2
    d = np.hypot(dx, dy)
    ang = np.arctan2(np.abs(dx), -dy)                 # 0 at the top, pi at the bottom
    ends = np.clip(np.minimum(ang, np.pi - ang) / 0.5, 0, 1) ** 1.5
    if v == "a":
        cv.add(FIELD, gauss(d - r, 1.1) * 0.55 * ends * glow)
        cv.add(PS[1], gauss(d - r, 3.0) * 0.35 * ends * glow)
        cv.add(FIELD, np.clip((d - (r - 8)) / 8, 0, 1) * (d < r) * 0.10 * ends * glow)
    else:
        seg = (ang * 5 / np.pi) % 1.0
        plate = ((seg > 0.08) & (seg < 0.92)).astype(float)
        cells = (np.abs(((ang * r / 6) % 1.0) - 0.5) < 0.08).astype(float)
        band = (d > r - 4) & (d < r + 0.5)
        cv.add(FIELD, (gauss(d - r, 0.7) + gauss(d - (r - 4), 0.6) * 0.6) * plate * 0.6 * glow)
        cv.add(FIELD, cells * band * plate * 0.3 * glow)
        cv.add(PS[1], band * plate * 0.12 * glow)
    return artkit.additive(cv.image())


def catch(v, k, f):
    """Catch flash at heading k (from the ship's centre to the catch point), frame f of 6."""
    theta = TAU * k / HEADINGS
    t = f / (CATCH_FRAMES - 1)
    r = 27.0
    cv = Canvas(24, 24)
    # the field circle through the sprite centre, the ship's centre r px back along -theta
    ox, oy = 12 - np.sin(theta) * r, 12 + np.cos(theta) * r
    d = np.hypot(cv.x - ox, cv.y - oy)
    along = np.hypot(cv.x - 12, cv.y - 12)
    fade = (1 - t) ** 1.3
    if v == "a":
        span = 4 + 6 * t
        cv.add(WHITE, gauss(d - r, 0.8) * gauss(along, span) * 1.4 * fade)
        cv.add(FIELD, gauss(d - r, 2.2) * gauss(along, span * 1.4) * 0.8 * fade)
        cv.add(WHITE, gauss(along, 1.6) * 1.6 * (1 - t) ** 2)
        rng = np.random.default_rng(k * 11 + 5)
        for _ in range(4):
            a = theta + rng.uniform(-1.0, 1.0)
            r1 = 2 + 8 * t ** 0.7
            seg = cv.seg(12 + np.sin(a) * (r1 - 2), 12 - np.cos(a) * (r1 - 2), 12 + np.sin(a) * r1, 12 - np.cos(a) * r1)
            cv.add(PS[3], gauss(seg, 0.5) * 1.1 * fade)
    else:
        plate = gauss(np.abs(d - (r - 2)), 2.2) * (along < 9)
        cv.add(FIELD, plate * 0.9 * fade)
        cv.add(WHITE, (gauss(d - r, 0.6) + gauss(d - (r - 4), 0.6)) * (along < 9) * 1.1 * fade)
        cv.add(WHITE, gauss(np.abs(along - (2 + 8 * t)), 0.7) * 0.9 * fade)
    return artkit.additive(cv.image())


def bodies(v):
    return pod_model(v)


def build(v, write):
    fields = {(n, side): field(v, r, side) for n, r in enumerate(RADII, 1) for side in ("left", "right")}
    catches = [catch(v, k, f) for k in range(HEADINGS) for f in range(CATCH_FRAMES)]
    fields_q = act3kit.quantize_bright(list(fields.values()), 16)
    catches = act3kit.quantize_bright(catches, 24)
    frames, offsets = act3kit.render_pods("deflector_pod", "bodies", v, 1.0)
    if write:
        with act3kit.sprites_of(ROOT_NAME):
            for (n, side), img in zip(fields, fields_q):
                artkit.write_frames(f"deflector-pod-field-{n}-{side}", [img], SOURCE, single=True)
            artkit.write_frames("deflector-pod-catch", catches, SOURCE)
        act3kit.write_pods("deflector", frames, offsets, SOURCE)
        print(f"deflector pod {v}: {act3kit.where(ROOT_NAME)}; pods in assets/sprites")
    return dict(fields=dict(zip(fields, fields_q)), catches=catches, pods=frames, offsets=offsets)


# =========================================================================== review

def composite(bank, art):
    hull = artkit.load_frames("ship")[bank]
    img = Image.new("RGBA", hull.size, (0, 0, 0, 0))
    img.alpha_composite(hull)
    for side in ("left", "right"):
        img.alpha_composite(art["pods"][side][bank], tuple(art["offsets"][side][bank]))
    return img


def review(v, art):
    ground = g.r36.mars_ground(41, craters=7, dunes=0.03)
    level = 1
    r = RADII[level - 1]
    fl = {side: art["fields"][(level, side)] for side in ("left", "right")}
    hull = composite(2, art)
    fps = 30
    charges = {"left": 3, "right": 3}
    back = {"left": 0.0, "right": 0.0}
    bright = {"left": -9.0, "right": -9.0}
    rng = np.random.default_rng(31)
    bullets, flashes, frames = [], [], []
    sx, sy = 240, 380
    for i in range(fps * 6):
        t = i / fps
        img = ground.copy()
        if i % 4 == 0:                           # a stream from the upper left, sometimes the right
            src = (60, 60) if (i // 40) % 3 != 2 else (420, 80)
            a = np.arctan2(sy - src[1], sx - src[0]) + rng.uniform(-0.12, 0.12)
            bullets.append([src[0], src[1], np.cos(a) * 190, np.sin(a) * 190])
        for side in charges:                     # one charge back every 0.67 s (L1)
            if charges[side] < 3:
                back[side] += 1 / fps
                if back[side] >= 0.67:
                    back[side] = 0.0
                    charges[side] += 1
                    bright[side] = t
            else:
                back[side] = 0.0
        keep = []
        for b in bullets:
            b[0] += b[2] / fps
            b[1] += b[3] / fps
            dx, dy = b[0] - sx, b[1] - sy
            side = "left" if dx < 0 else "right"
            if np.hypot(dx, dy) <= r and charges[side] > 0:
                charges[side] -= 1
                k = int(round((np.arctan2(dx, -dy) % TAU) / TAU * HEADINGS)) % HEADINGS
                flashes.append((i, b[0], b[1], k))
                continue
            if np.hypot(dx, dy) < 10:
                continue
            keep.append(b)
        bullets = keep
        sprite.paste_center(img, sprite.shadow_of(hull, opacity=0.6, blur=1.0, scale=0.85), sx + 21, sy + 30)
        sprite.paste_center(img, hull, sx, sy)
        for side in ("left", "right"):
            if charges[side] > 0:
                k = 1.0 + (1.2 if t - bright[side] < 0.15 else 0.0)
                f = fl[side]
                lay = f
                if k > 1:
                    a = np.array(f).astype(np.float64)
                    a[..., :3] = np.clip(a[..., :3] * k, 0, 255)
                    lay = Image.fromarray(a.astype(np.uint8), "RGBA")
                x0 = sx - f.width if side == "left" else sx
                img = artkit.add_light(img, lay, (int(x0), int(sy - f.height / 2)))
        for b in bullets:
            g.e3.orb(img, b[0], b[1], 4)
        for i0, x, y, k in flashes:
            age = (i - i0)
            if age < CATCH_FRAMES:
                img = artkit.add_light(img, art["catches"][k * CATCH_FRAMES + age], (int(x - 12), int(y - 12)))
        g.label(img, 8, 540 - 14, f"DEFLECTOR POD L1 X2 ({v.upper()}): CHARGES L {charges['left']} R {charges['right']}",
                (150, 200, 240))
        frames.append(img)
    fields = [art["fields"][(n, "left")] for n in range(1, 7)] + [art["fields"][(n, "right")] for n in (1, 6)]
    rows = [("POD-DEFLECTOR ON EVERY BANK FRAME (HULL + POD SPRITES AT THEIR PIVOTS)",
             [composite(b, art) for b in range(5)], 4, False),
            ("DEFLECTOR-POD-FIELD-1..6-LEFT, -1/-6-RIGHT (RADIUS 22..32 PX), ADDITIVE", fields, 3, True),
            ("DEFLECTOR-POD-CATCH: HEADING 0 (UP), 6 FRAMES, 2 STEPS", art["catches"][:6], 4, True),
            ("DEFLECTOR-POD-CATCH: FRAME 1 AT THE 16 HEADINGS", art["catches"][1::6], 4, True),
            ("IN FLIGHT (1X): THE LEFT FIELD DRAINED BY A STREAM, CHARGES RETURNING",
             [f.crop((150, 290, 330, 470)) for f in frames[20:140:20]], 1, False)]
    title = {"a": "A: FIELD BAND", "b": "B: PLATE LATTICE"}[v]
    sheet = artkit.review_sheet(f"DEFLECTOR POD - PROPOSAL {title} (ROUND 37)", rows, width=1400, batch=act3kit.BATCH)
    act3kit.save_proposal(sheet, [f.crop((90, 200, 390, 500)).resize((450, 450), Image.NEAREST) for f in frames],
                          CONCEPT, "deflector-pod", v, fps=fps, chosen=CHOSEN)


if __name__ == "__main__":
    artkit.REVIEW_ROUND = act3kit.ROUND
    args = sys.argv[1:]
    variants = [args[args.index("--variant") + 1]] if "--variant" in args else ["a", "b"]
    for v in variants:
        review(v, build(v, write=(v == CHOSEN and "--review" not in args)))
