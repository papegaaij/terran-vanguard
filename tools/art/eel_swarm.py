#!/usr/bin/env python3
"""Production art: the Eel Swarm, Level 23's head-fired eel chain (design/enemies/naval/eel-swarm; M8 part C batch,
concept round 52, step ART-C). From the chosen round-48 look **a** "ribbon eel" (tools/concept/enemies_r48.py
``eel_head``/``eel_segment`` a, the role colours ``eel-swarm-a`` = rust / violet / lime, imported unchanged): rust
chitin, a violet lateral stripe (the tell), lime gill frills on the head (the weak point), a bone dorsal ribbon.

Headings as tools/art/README.md: heading k turns k x 22.5 degrees clockwise from straight down (the stat block's
`16 angles`), every heading its own render under the fixed key light (upper left). Sizes from the stat block: head
48x40 (hitbox 36x28) on a 48 canvas, segment 36x24 on 40, tail 32x24 on 36; the chain's spacing (the concept's):
head->segment 1 30 px, segment->segment 24, last segment->tail 22.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``eel-swarm``
once design/enemies/naval/eel-swarm/data.yaml exists, first_level 23):
  eel-swarm_0..47           48x48, normal: the head, index heading x 3 + frame: 0 rest, 1 the tell (the stripe lit
                            violet, jaws parting), 2 the shot (jaws wide, stripe and eyes at full glow); the game
                            shows 1 for the 0.3 s tell before each shot and 2 for 6 steps at the shot
  eel-swarm-segment_0..31   40x40, normal: one body eel, heading x 2 + (0 rest, 1 stripe lit); the tell runs from
                            the tail to the head over the 0.3 s before a shot (segment i of n lit while
                            |1 - i / n - u| < 0.2, u = 0 at the tail .. 1 at the head, the concept's wave)
  eel-swarm-tail_0..31      36x36, normal: the tail eel (smaller, a broader fan), heading x 2 + (rest, lit)
  eel-swarm-stripe_0..47    48x48, additive: the stripe's violet bloom for the tell, heading x 3 + part (0 head,
                            1 segment, 2 tail), centred on the part's centre; drawn over a lit part (frame 1 of its
                            set) while the wave passes it, so the tell reads under the grade
  eel-swarm-muzzle_0..3     24x24, additive: the violet pulse at the jaws (the shot's light, 2 steps a frame),
                            centred on the head's mouth: assets/pivots/eel-swarm.json ``mouth`` per heading
  eel-swarm-husk_0..63      40x40, normal: a dead body eel sinking (the chained death and the scattered trail's
                            end): heading x 4 + step (0 just killed, scorched and dark; 1-3 sinking: smaller, faded
                            toward the deep water's teal-black), 4 steps a frame; the tail uses these too
  eel-swarm-head-husk_0..63 48x48, normal: the head's husk likewise (jaws slack, the gills dark)
  assets/pivots/eel-swarm.json   per heading the mouth (the shot's spawn and the muzzle's centre) in the head
                            frame's px and the gills' centre (the weak point), plus the chain's spacing
  design/enemies/naval/concept/eel-swarm-final-r52-a.png/.gif   the review sheet and GIF (a 12-segment snake over
                            Thera Deep's farms under the review grade: the stripe tell running to the head, the
                            aimed shots and a 3-way fan, a cut at segment 8 and its rear part scattering for 3 s,
                            the head kill unzipping the rest head to tail 0.07 s apart, the husks sinking)

Deaths use the game's under-water burst ladder (explosions.py ``explosion-deep-*``: `small` per segment, `medium`
at the head) under the husks. In the dark levels (L26) the eel is "a dark snake lit by its stripe": its glow-only
frames come with that level's part (not here).

Run: python3 tools/art/eel_swarm.py [--review]   (~1 min; --review only rebuilds the review files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image

import artkit
import l23kit as kit
from artkit import DESIGN, TAU

import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look a's models and colours)
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "eel_swarm.py"
BATCH = "M8 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r52"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
V = "a"
HEADINGS = 16
HEAD, SEG, TAIL, MUZZLE = 48, 40, 36, 24
HEAD_EXTENT, SEG_EXTENT = 2.05, 1.8               # the concept's model units across its 48 / 40 px canvases
TAIL_EXTENT = 1.8 * TAIL / 34                     # the concept's tail at 34 px, kept at its scale on 36
HEAD_FRAMES = [(0.0, 0.0), (1.6, 0.35), (1.8, 1.0)]   # (stripe, jaw): rest, tell, shot
LIT = 1.6                                         # the stripe's glow in the tell (the concept's 1.0, brighter: it must read)
HUSK_STEPS = [(1.0, 0.0), (0.9, 0.3), (0.8, 0.55), (0.7, 0.78)]   # (scale, fade toward the deep)
DEEP = np.array((4, 20, 26), float)
GAPS = (30, 24, 22)
VIOLET = np.array(e48.VIOLET, float)
COLOURS = 40
MOUTH_Y = 1.12                                    # model y of the jaws' tips (the shot's spawn)
GILLS_Y = 0.02


# =========================================================================== models

def turned(scene, mats, h):
    """A +Y-forward model turned to point ``h`` clockwise from straight down, its patterns with it."""
    rot = model_rotation(np.pi / 2 + h)
    return (lambda p: scene(rotate_z(p, rot))), model_space_materials(mats, rot)


def dead_mats(mats):
    """The husk: every glow out, the chitin scorched dark."""
    out = []
    for m in mats:
        alb = tuple(float(c) * 0.45 for c in m.albedo)
        out.append(replace(m, albedo=alb, emission=(0.0, 0.0, 0.0), spec=m.spec * 0.4))
    return out


def model(kind, stripe=0.0, jaw=0.0):
    if kind == "head":
        return e48.eel_head(V, stripe, jaw)
    return e48.eel_segment(V, stripe, tail=kind == "tail")


def _frame(job):
    kind, k, a, b = job
    h = TAU * k / HEADINGS
    size, extent = {"head": (HEAD, HEAD_EXTENT), "segment": (SEG, SEG_EXTENT), "tail": (TAIL, TAIL_EXTENT),
                    "head-husk": (HEAD, HEAD_EXTENT), "husk": (SEG, SEG_EXTENT)}[kind]
    if kind.endswith("husk"):
        scale, _ = HUSK_STEPS[a]
        scene, mats = model("head" if kind == "head-husk" else "segment", 0.0, 0.15)
        mats = dead_mats(mats)
        extent = extent / scale
    else:
        scene, mats = model(kind, a, b)
    scene, mats = turned(scene, mats, h)
    img = artkit.native(*artkit.render_hi(scene, mats, (size, size), extent))
    if kind.endswith("husk"):
        _, fade = HUSK_STEPS[a]
        arr = np.array(img).astype(np.float64)
        arr[..., :3] = arr[..., :3] * (1 - fade) + DEEP * fade
        img = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA")
    return img


def _stripe(job):
    """The stripe alone (every other surface black, no emission but the stripe's), bloomed: the tell's overlay."""
    from render import sprite
    _, k, part = job
    kind = ("head", "segment", "tail")[part]
    scene, mats = model(kind, 1.8, 0.35)
    keep = e48.E_STRIPE                                   # the stripe slot
    dark = [m if i == keep else replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None,
                                         emission=(0.0, 0.0, 0.0)) for i, m in enumerate(mats)]
    extent = {"head": HEAD_EXTENT, "segment": SEG_EXTENT, "tail": TAIL_EXTENT}[kind]
    size = {"head": HEAD, "segment": SEG, "tail": TAIL}[kind]
    scene, dark = turned(scene, dark, TAU * k / HEADINGS)
    hi, f = artkit.render_hi(scene, dark, (size, size), extent, shadows=False)
    lo = sprite.downsample(hi, f)
    em = np.clip((lo[..., :3] * lo[..., 3:4]).max(axis=-1), 0, 1)
    pad = (HEAD - size) // 2
    em = np.pad(em, ((pad, HEAD - size - pad), (pad, HEAD - size - pad)))
    img = Image.fromarray((em * 255).astype(np.uint8))
    from PIL import ImageFilter
    halo = np.asarray(img.filter(ImageFilter.GaussianBlur(2.0)), float) / 255
    core = np.asarray(img.filter(ImageFilter.GaussianBlur(0.5)), float) / 255
    v = np.clip(core * 1.1 + halo * 1.5, 0, 1)
    q = artkit.ordered_dither(v, 7)
    rgb = VIOLET[None, None] * q[..., None] + 90 * np.clip(core - 0.6, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full(v.shape, 255.0)]).astype(np.uint8)))


def mouth(k, y=MOUTH_Y):
    """The jaws' tip of heading k in the head frame's px."""
    rot = model_rotation(np.pi / 2 + TAU * k / HEADINGS)
    p = rotate_z(np.array([[0.0, y, 0.2]]), -rot)[0]
    px = HEAD / HEAD_EXTENT
    return [round(HEAD / 2 + p[0] * px, 1), round(HEAD / 2 - p[1] * px, 1)]


def muzzle_frame(i):
    """The violet pulse at the jaws: a white-violet core, a soft violet bloom and four short rays, fading."""
    w = MUZZLE
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    c = w / 2
    r = np.hypot(xx - c, yy - c)
    k = [1.0, 0.8, 0.5, 0.25][i]
    core = np.exp(-(r / (1.6 + 0.5 * i)) ** 2)
    bloom = np.exp(-(r / (4.5 + 1.6 * i)) ** 2) * 0.7
    ang = np.arctan2(yy - c, xx - c)
    rays = np.clip(np.cos(ang * 4 + 0.4) ** 16, 0, 1) * np.exp(-r / (5 + 2 * i)) * 0.8
    v = np.clip((core + bloom + rays) * k, 0, 1)
    col = VIOLET[None, None] * np.clip(v * 1.4, 0, 1)[..., None] + 255 * np.clip(core * k - 0.5, 0, 1)[..., None]
    q = artkit.ordered_dither(np.clip(col.max(axis=-1) / 255, 0, 1), 8)
    col = col / np.maximum(col.max(axis=-1, keepdims=True), 1) * q[..., None] * 255
    return artkit.additive(Image.fromarray(np.dstack([np.clip(col, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def intel_model():
    """(scene, mats) for the hangar intel portrait: the head nose down, the stripe lit, two body eels trailing up
    behind it up to the right (the chain), as intel.coilwyrm_model."""
    from render import sdf
    head, mats = turned(*e48.eel_head(V, 0.8, 0.6), 0.35)
    seg1, _ = turned(*e48.eel_segment(V, 0.8), 0.6)
    seg2, _ = turned(*e48.eel_segment(V, 0.8), 0.75)
    parts = [(head, (-0.3, -0.42), 0.74), (seg1, (0.22, 0.3), 0.58), (seg2, (0.62, 0.78), 0.5)]

    def scene(p):
        out = []
        for m, (x, y), k in parts:
            d, mm = m((p - np.array([x, y, 0.0])) / k)
            out.append((d * k, mm))
        return sdf.union(*out[::-1])
    return scene, mats


# =========================================================================== build

def build():
    heads = [("head", k, s, j) for k in range(HEADINGS) for s, j in HEAD_FRAMES]
    segs = [("segment", k, s, 0.0) for k in range(HEADINGS) for s in (0.0, LIT)]
    tails = [("tail", k, s, 0.0) for k in range(HEADINGS) for s in (0.0, LIT)]
    husks = [("husk", k, i, 0) for k in range(HEADINGS) for i in range(len(HUSK_STEPS))]
    hhusks = [("head-husk", k, i, 0) for k in range(HEADINGS) for i in range(len(HUSK_STEPS))]
    jobs = heads + segs + tails + husks + hhusks
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_frame, jobs, chunksize=4))
        stripes = list(ex.map(_stripe, [("stripe", k, part) for k in range(HEADINGS) for part in range(3)]))
    n1, n2, n3 = len(heads), len(heads) + len(segs), len(heads) + len(segs) + len(tails)
    live = artkit.quantize_set(out[:n3], COLOURS)
    dead = artkit.quantize_set(out[n3:], 28)
    kit.write("eel-swarm", live[:n1], SOURCE)
    kit.write("eel-swarm-segment", live[n1:n2], SOURCE)
    kit.write("eel-swarm-tail", live[n2:n3], SOURCE)
    kit.write("eel-swarm-husk", dead[:len(husks)], SOURCE)
    kit.write("eel-swarm-head-husk", dead[len(husks):], SOURCE)
    kit.write("eel-swarm-muzzle", [muzzle_frame(i) for i in range(4)], SOURCE)
    kit.write("eel-swarm-stripe", stripes, SOURCE)
    artkit.write_pivots("eel-swarm", SOURCE, {
        "size": [HEAD, HEAD],
        "mouth": [mouth(k) for k in range(HEADINGS)],
        "gills": [mouth(k, GILLS_Y) for k in range(HEADINGS)],
        "spacing": {"head_to_segment": GAPS[0], "segment": GAPS[1], "segment_to_tail": GAPS[2]},
        "note": "per heading (k x 22.5 degrees clockwise from down) the jaws' tip (the shot's spawn, the muzzle's "
                "centre) and the gills (the weak point) in the head frame's px; the chain's spacing in px along the "
                "path history (the concept's)",
    })


# =========================================================================== review

N = 12
PATH = rig.catmull_rom([(150, -90), (160, 110), (320, 210), (370, 330), (230, 390), (110, 300), (80, 120),
                        (150, -160)], [0.0, 1.9, 3.6, 4.8, 6.2, 7.6, 9.0, 11.0])
SHOTS = [1.4, 3.2, 5.0, 6.8]
FANS = [3.2]
CUT = (8, 3.9)
HEAD_KILL = 6.9
DUR = 9.6
FPS = 20
SPACING = [GAPS[0]] + [GAPS[1]] * (N - 1) + [GAPS[2]]


def route(t):
    base = PATH(t)
    h = rig.path_heading(PATH, t)
    off = 36 * np.sin(TAU * 0.8 * t)
    return np.array([base[0] - np.sin(h) * off * 0.5, base[1] + np.cos(h) * off * 0.5])


def stripe(i, t):
    for ts in SHOTS:
        u = 1 - (ts - t) / 0.3
        if -0.05 <= u <= 1.05 and abs(1 - i / (N + 1) - u) < 0.2:
            return 1
    return 0


def review():
    hd, sg, tl = kit.load("eel-swarm"), kit.load("eel-swarm-segment"), kit.load("eel-swarm-tail")
    hk, hhk, mz = kit.load("eel-swarm-husk"), kit.load("eel-swarm-head-husk"), kit.load("eel-swarm-muzzle")
    stp = kit.load("eel-swarm-stripe")
    orb = artkit.load_frames("orb")
    try:
        burst_s, burst_m = kit.load("explosion-deep-small"), kit.load("explosion-deep-medium")
    except FileNotFoundError:
        burst_s, burst_m = artkit.load_frames("explosion-small"), artkit.load_frames("explosion-medium")
    rows = [("HEAD: 16 HEADINGS (REST)", hd[0::3], 1, False),
            ("HEAD FRAMES REST / TELL / SHOT (HEADINGS 0, 4, 10)", [hd[k * 3 + j] for k in (0, 4, 10) for j in range(3)],
             2, False),
            ("SEGMENT: 16 HEADINGS (REST)", sg[0::2], 1, False),
            ("SEGMENT AND TAIL: REST / STRIPE LIT (HEADINGS 0, 6)", [sg[0], sg[1], sg[12], sg[13], tl[0], tl[1], tl[12],
                                                                      tl[13]], 2, False),
            ("TAIL: 16 HEADINGS (REST)", tl[0::2], 1, False),
            ("MUZZLE (ADDITIVE)", mz, 3, True),
            ("STRIPE BLOOM (ADDITIVE): HEAD, SEGMENT, TAIL (HEADINGS 0, 6)", stp[0:3] + stp[18:21], 2, True),
            ("HUSKS SINKING: SEGMENT, HEAD (HEADINGS 2, 9)", hk[8:12] + hk[36:40] + hhk[8:12], 2, False)]
    sheet = artkit.review_sheet("EEL SWARM - FINAL (ROUND 48 LOOK A, RIBBON EEL)", rows, width=1000, batch=BATCH)
    # the chain over the farms, at 1x
    frames = []
    cut_part = None
    shots = []
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        img = kit.plate(480, 540, 0, 300)
        pts, heads = rig.chain_at(route, t, SPACING)
        pts = [np.asarray(p) for p in pts]
        heads = [float(h) for h in heads]
        alive = list(range(len(pts)))
        glow = []
        if t >= CUT[1] and cut_part is None:
            cut_part = [(pts[i].copy(), heads[i]) for i in range(CUT[0] + 1, len(pts))]
        # the scattered rear part: keeps its heading at 70 px/s for 3 s, then dies from the cut backwards
        if cut_part is not None:
            u = t - CUT[1]
            for j, (p0, h0) in enumerate(cut_part):
                d = np.array([np.cos(h0), np.sin(h0)])
                p = p0 + d * 70 * u
                k = kit.heading_index(d[0], d[1])
                if u < 3.0 + 0.07 * j:
                    fr = tl[k * 2] if j == len(cut_part) - 1 else sg[k * 2]
                    kit.paste_c(img, fr, *p)
                else:
                    s = min(3, int((u - 3.0 - 0.07 * j) / 0.25))
                    kit.paste_c(img, hk[k * 4 + s], *p)
                    if u - 3.0 - 0.07 * j < 0.4:
                        glow.append((burst_s[min(len(burst_s) - 1, int((u - 3.0 - 0.07 * j) * 30))], p))
        keep = CUT[0] + 1 if cut_part is not None else len(pts)
        for i in reversed(range(min(keep, len(pts)))):
            p, h = pts[i], heads[i]
            k = kit.heading_index(np.cos(h), np.sin(h))
            if t >= HEAD_KILL:
                dt = t - HEAD_KILL - 0.07 * i
                if dt >= 0:
                    s = min(3, int(dt / 0.25))
                    kit.paste_c(img, (hhk if i == 0 else hk)[k * 4 + s], *p)
                    if dt < 0.45:
                        b = burst_m if i == 0 else burst_s
                        glow.append((b[min(len(b) - 1, int(dt * 30))], p))
                    continue
            if i == 0:
                st = 2 if any(0 <= t - s < 0.1 for s in SHOTS) else 1 if any(0 <= s - t < 0.3 for s in SHOTS) else 0
                kit.paste_c(img, hd[k * 3 + st], *p)
                if st:
                    glow.append((stp[k * 3], p))
                if st == 2:
                    m = mouth(k)
                    glow.append((mz[int((t % 0.1) * 40) % 4], p - HEAD / 2 + np.array(m)))
            elif i == len(pts) - 1:
                kit.paste_c(img, tl[k * 2 + stripe(i, t)], *p)
                if stripe(i, t):
                    glow.append((stp[k * 3 + 2], p))
            else:
                kit.paste_c(img, sg[k * 2 + stripe(i, t)], *p)
                if stripe(i, t):
                    glow.append((stp[k * 3 + 1], p))
        if t < HEAD_KILL:
            for s in SHOTS:
                if abs(t - s) < 0.5 / FPS:
                    hp = pts[0]
                    aim = np.array([240.0, 488.0]) - hp
                    aim /= np.linalg.norm(aim)
                    shots.append((hp.copy(), aim * 130 * 0.7, t))
                    if s in FANS:
                        for a in (-0.35, 0.35):
                            c, sn = np.cos(a), np.sin(a)
                            shots.append((hp.copy(), np.array([aim[0] * c - aim[1] * sn, aim[0] * sn + aim[1] * c])
                                          * 120 * 0.7, t))
        for p0, v, t0 in shots:
            p = p0 + v * (t - t0)
            if -10 < p[1] < 550:
                kit.paste_c(img, orb[int(t * 10) % 4], *p)
        kit.paste_c(img, kit.ship(2), 240, 488)
        for g, p in glow:
            img = kit.add_c(img, g, *p)
        raster.draw_text(img, 8, 8, "EEL SWARM A - T={:.1f}".format(t), raster.LABEL)
        frames.append(img.convert("RGB"))
    # three frames of the GIF on the sheet
    stills = [frames[int(t * FPS)] for t in (3.1, 5.4, 7.3)]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE TELL BEFORE THE FAN (T=3.1), THE CUT TAIL SCATTERING (T=5.4), "
                                            "THE HEAD KILL UNZIPPING (T=7.3); REVIEW GRADE", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames[::2]], CONCEPT, "eel-swarm",
                       fps=FPS // 2)


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
