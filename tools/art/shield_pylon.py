#!/usr/bin/env python3
"""Production art: the Shield Pylon, Level 41's gate holder (design/enemies/ground/shield-pylon; M10 part G batch,
concept round 79, step ART-G1). Round 72's chosen **A.5 a** "crown" (tools/concept/enemies_r72.py ``pylon_base``,
``pylon_crown('a')``, imported unchanged) in Q9 a's elite livery: a gloss-black octagonal base with a heavy gold edge
and red corner lights, a short black mast ringed by a gold crown of six prongs, the emitter lens leaning out of the
crown toward its dome; the strong 2 px gold rim (enemies_r60 ``belt_rim(True)`` without its per-frame palette).

The dome's effects are part A's (tools/art/act6_gear.py ``dome``, round 73 F.3 accepted): this script **claims** them
for the pylon (``dome`` re-runs act6_gear's build unchanged, so the files come out byte-identical under act6_gear.py's
Source note, and moves them with the root) and lists them in its check and pivots.

Scale 26 px per model unit (round 72's sheet drew the pylon at 27.8: the base's corners now fit the stat block's 56x56
cell). Facings: the crown's facing k turns k x 22.5 degrees clockwise from straight down (the simulation's angles), each
facing its own render under the low top-left Sun; the base is fixed (one frame).

Outputs (build/art-held/sprites/ until the pylon's data.yaml claims the root ``shield-pylon`` (step G2; rerun then and
the files move to assets/sprites/); ``additive`` = premultiplied on black, GL_SRC_ALPHA, GL_ONE; every glow a float
field in >= 32 undithered steps, drawn 1x):
  shield-pylon           56x56 normal, centred: the fixed base (the black octagon, its gold edge, the red corner lights)
  shield-pylon-crown_0..15   56x56 normal, centred on the base: the mast and its crown at 16 facings, the emitter's lens
                         dark (asleep); drawn over the base
  shield-pylon-emitter_0..3  24x24 additive, centred on the lens (pivots ``lens`` per facing): the emitter lit, a slow
                         pulse (15 steps a frame); none while the pylon sleeps (its dome already lit), on from its wake;
                         the feed beam (act6_gear's ``shield-pylon-feed``) starts here
  shield-pylon-wreck     56x56 normal, centred: the burnt base with the mast's torn stump (the death's wreck)
  shield-pylon-topple_0..5   72x72 normal, centred on the pylon: the crown toppling off its mast toward the lower right
                         (0.4 s at 15 fps, drawn over the wreck from the death; frame 5, the crown lying broken beside
                         the base, stays); the medium vacuum explosion covers the first frames
  (claimed from act6_gear.py) shield-pylon-dome_0..11 (232x232 additive, the 110 px dome; scale radius / 110),
                         -dome-hit_0..5 (32x32), -dome-collapse_0..5 (the 0.4 s ring into the crown), -feed (32x12
                         strip, the emitter to the dome's rim), -sheen_0..3 (48x48, the covered units' sheen)
  build/art-held/pivots/shield-pylon.json: the facing rule, the lens per facing, the dome's radius and offset, the sets'
                         sizes and blends, the topple's timing
  intel/shield-pylon     30x30 sensor portrait via tools/art/intel.py (``intel_hi``: the pylon, crown facing down)
  design/enemies/ground/shield-pylon/concept/shield-pylon-final-r79-a.png/.gif   review (the sets, the readability check
                         over Level 41's darkest and brightest ice with the measured gold share, the elite pylon beside a
                         regular Warden Tank at 1:1, the atlas share) and GIF (a gate halt: two pylons asleep with their
                         domes lit, the wake, shots spent on the domes over two covered Warden Tanks, both pylons broken
                         in turn: the explosion, the crown's topple, the dome's collapse into it)

Run: python3 tools/art/shield_pylon.py [pylon] [dome] [intel] [--review] [--check]
     (no subject = all, about 30 s; --review rebuilds only the review files from the written frames; --check lists
     every set with its place, frames, size, Source chunk, checker share and an additive set's glow steps, and prints
     the atlas share)
"""
import json
import sys

import numpy as np
from PIL import Image

import artkit
import belt_units as bu
import halo_platform as hp
from artkit import DESIGN, ROOT, TAU

import enemies_r60 as e60  # noqa: E402  (concept: gold_share)
import enemies_r72 as e72  # noqa: E402  (concept: round 72's pylon model, imported unchanged)
from render import raster, sdf, sprite  # noqa: E402
from render.enemy_models import A_FACET, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.enemy_rigs import local_to_screen, write_gif  # noqa: E402
from render.sdf import Material, union  # noqa: E402

SCRIPT = "shield_pylon.py"
BATCH = "M10 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r79"
ROOT_NAME = "shield-pylon"
CONCEPT = DESIGN / "enemies" / "ground" / "shield-pylon" / "concept"
PXU = 26.0
CELL = 56
EXT = CELL / PXU
TOPPLE = 72
FACINGS = 16
LENS = e72.LENS_LOCAL["a"]                    # (0, 0.34): the lens, model units ahead of the mast
DOME_R, DOME_D = e72.DOME_R, e72.DOME_D       # 110 px, 140 px from the pylon along the facing
EMIT = 24
FPS = 20
A_LENS = e72.A_LENS


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


load, where = bu.load, bu.where


def face_deg(k):
    return 360.0 * k / FACINGS


def lens_px(k):
    dx, dy = local_to_screen(LENS[0], LENS[1], np.pi / 2 + TAU * k / FACINGS, PXU)
    return [round(float(dx), 2), round(float(dy), 2)]


# =========================================================================== models

def base_model(wreck=False):
    scene, mats = e72.pylon_base()
    if not wreck:
        return scene, mats

    def burnt_scene(p):
        d, m = scene(p)
        stump = np.maximum(sdf.sd_cylinder_z(p, (0, 0, 0.2), 0.24, 0.14),
                           p[:, 2] - (0.26 + 0.06 * np.sin(np.arctan2(p[:, 1], p[:, 0]) * 5)))
        m = np.where(stump < d, A_GUN, m)
        return np.minimum(d, stump), m
    return burnt_scene, hp.burnt(mats)


def crown_model(lens=0.0, scorched=False):
    """The concept's crown (a) with the lens's emission at ``lens`` (0: dark, the emitter asleep)."""
    scene, mats = e72.pylon_crown("a", lens=0.0)
    mats = list(mats)
    mats[A_LENS] = Material((0.22, 0.16, 0.06), metal=0.6, shininess=120, spec=1.0,
                            emission=tuple(np.array((1.0, 0.86, 0.5)) * (0.05 + 1.6 * lens)))
    if scorched:
        mats = hp.burnt(mats)
        mats[A_LENS] = Material((0.05, 0.04, 0.03), metal=0.2, shininess=20, spec=0.2)
    return scene, mats


def _base(wreck=False):
    scene, mats = base_model(wreck)
    return hp.elite_rim(bu.render(scene, mats, CELL, EXT))


def _crown(k):
    scene, mats = crown_model()
    return hp.elite_rim(bu.render(*bu.turned(scene, mats, k, FACINGS), CELL, EXT))


TOPPLE_STEPS = [(14, 0.04), (32, 0.1), (55, 0.2), (76, 0.32), (88, 0.42), (90, 0.46)]   # tilt (deg), slide (units)
TOPPLE_DIR = 315.0                            # the fall: toward the lower right (deg cw from down)


def topple_scene(i):
    """The crown tipped over its mast's lower-right edge: the crown model (facing 0) pitched by the step's tilt about
    a hinge at the mast's foot, slid outward, then turned toward ``TOPPLE_DIR``; the last frame lies broken (its prongs
    clipped by the ground)."""
    tilt, slide = TOPPLE_STEPS[i]
    scene, mats = crown_model(scorched=i >= 3)
    a = np.radians(tilt)
    hinge = np.array((0.0, -0.22, 0.12))

    def tipped(p):
        q = p - hinge - np.array((0.0, -slide, 0.0))
        q = sdf.rotate_x(q, -a)
        q = q + hinge
        d, m = scene(q)
        return np.maximum(d, -p[:, 2]), m                 # never below the deck
    rot = bu.model_rotation(np.pi / 2 + np.radians(TOPPLE_DIR))
    return (lambda p: tipped(sdf.rotate_z(p, rot))), bu.model_space_materials(mats, rot)


def _topple(i):
    scene, mats = topple_scene(i)
    return hp.elite_rim(bu.render(scene, mats, TOPPLE, TOPPLE / PXU))


def emitter_glow(f, n=EMIT):
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    k = 0.85 + 0.15 * np.sin(TAU * f / 4)
    fld = (np.exp(-(r / 2.2) ** 2) * 1.1 + np.exp(-(r / 6.5) ** 2) * 0.4) * k
    return hp.glow_img(fld, (255, 214, 120), white=0.6, levels=128)


# =========================================================================== build

def pivot_path():
    return (artkit.PIVOTS if bu.claimed(ROOT_NAME) else ROOT / "build" / "art-held" / "pivots") / f"{ROOT_NAME}.json"


def load_pivots():
    for folder in (artkit.PIVOTS, ROOT / "build" / "art-held" / "pivots"):
        if (folder / f"{ROOT_NAME}.json").exists():
            return json.loads((folder / f"{ROOT_NAME}.json").read_text(encoding="utf-8"))
    return {}


def write_pivots(data):
    folder = artkit.PIVOTS if bu.claimed(ROOT_NAME) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{ROOT_NAME}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n",
                                              encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{ROOT_NAME}.json").exists():
        (other / f"{ROOT_NAME}.json").unlink()


def build_pylon():
    from concurrent.futures import ProcessPoolExecutor
    with ProcessPoolExecutor(hp.WORKERS) as pool:
        crowns = list(pool.map(_crown, range(FACINGS)))
        topple = list(pool.map(_topple, range(len(TOPPLE_STEPS))))
        base, wreck = pool.map(_base, (False, True))
    q = artkit.quantize_set([base] + crowns, 40)
    save_set(ROOT_NAME, [q[0]], single=True)
    save_set(f"{ROOT_NAME}-crown", q[1:])
    qw = artkit.quantize_set([wreck] + topple, 32)
    save_set(f"{ROOT_NAME}-wreck", [qw[0]], single=True)
    save_set(f"{ROOT_NAME}-topple", qw[1:])
    save_set(f"{ROOT_NAME}-emitter", [emitter_glow(f) for f in range(4)])
    write_pivots({
        "size": CELL, "centred": True, "hitbox": [40, 40],
        "facing_rule": "k = round(facing * 16 / 360) mod 16, the facing in degrees cw from down: the crown's "
                       "emitter turns toward its dome",
        "crown": {"sprite": f"{ROOT_NAME}-crown", "frames": FACINGS, "over": ROOT_NAME},
        "lens": [lens_px(k) for k in range(FACINGS)],
        "lens_note": "px from the pylon's centre (y down) per facing: the emitter's glow and the feed beam's start",
        "emitter": {"sprite": f"{ROOT_NAME}-emitter", "blend": "additive", "steps_per_frame": 15,
                    "when": "awake (from the gate's halt); dark while the pylon sleeps, its dome lit"},
        "dome": {"sprite": f"{ROOT_NAME}-dome", "radius": DOME_R, "offset": DOME_D, "easy_radius": 95,
                 "hard_radius": 125, "scale": "radius / 110", "frames": 12, "steps_per_frame": 20,
                 "hit": f"{ROOT_NAME}-dome-hit", "collapse": f"{ROOT_NAME}-dome-collapse",
                 "feed": f"{ROOT_NAME}-feed", "sheen": f"{ROOT_NAME}-sheen",
                 "note": "act6_gear.py's frames (part A), claimed here; the dome's centre DOME offset px from the "
                         "pylon along its facing, the feed from the lens to the dome's rim"},
        "wreck": {"sprite": f"{ROOT_NAME}-wreck", "note": "the base burnt with the mast's stump"},
        "topple": {"sprite": f"{ROOT_NAME}-topple", "size": TOPPLE, "centred": True, "fps": 15,
                   "direction_deg": TOPPLE_DIR,
                   "note": "drawn over the wreck from the death, frame 5 stays; with the medium vacuum explosion "
                           "and the dome's 0.4 s collapse into the crown"}})


def build_dome():
    import act6_gear  # noqa: E402  (part A's dome, claimed for the pylon)
    act6_gear.build_dome(act6_gear.dome_set())


def intel_hi():
    """The pylon with its crown facing down for the hangar intel's 30 px portrait (an 8x render, 240x240)."""
    b_scene, _ = e72.pylon_base()
    c_scene, c_mats = crown_model(lens=0.6)
    c_scene, c_mats = bu.turned(c_scene, c_mats, 0, FACINGS)
    return sdf.render(lambda p: union(b_scene(p), c_scene(p)), c_mats, (240, 240), EXT * 1.04)


def build_intel():
    import intel  # noqa: E402  (registers shield-pylon; imported late: it imports this module)
    intel.build(["shield-pylon"])


# =========================================================================== review

def L(name):
    return hp.L(name)


def pylon(img, x, y, k, awake=True, f=0):
    sp = L(ROOT_NAME)[0]
    hp.paste_c(img, hp.tinted(sp, black_alpha=0.4), x + 3, y + 4)
    hp.paste_c(img, sp, x, y)
    hp.paste_c(img, L(f"{ROOT_NAME}-crown")[k], x, y)
    if awake:
        lx, ly = load_pivots()["lens"][k]
        img = hp.add_c(img, L(f"{ROOT_NAME}-emitter")[(f // 15) % 4], x + lx, y + ly)
    return img


def dome_at(img, x, y, k, f, show=1.0):
    import act6_gear as g6  # noqa: E402
    a = np.radians(face_deg(k))
    dx, dy = -np.sin(a), np.cos(a)
    cx, cy = x + dx * DOME_D, y + dy * DOME_D
    img = hp.add_c(img, L(f"{ROOT_NAME}-dome")[(f // 20) % 12], cx, cy, show)
    lx, ly = load_pivots()["lens"][k]
    img = g6.draw_strip(img, L(f"{ROOT_NAME}-feed")[0], x + lx, y + ly, cx - dx * DOME_R, cy - dy * DOME_R)
    return img, (cx, cy)


def heading_k(deg, n):
    return int(round(deg * n / 360.0)) % n


def warden(img, x, y, aim, covered=False, f=0):
    body = artkit.load_frames("warden-tank-asc")[0] if (artkit.SPRITES / "warden-tank-asc_0.png").exists() else \
        bu.load("warden-tank-asc")[0]
    try:
        tur = bu.load("warden-tank-asc-turret")[heading_k(aim, 32) * 2]
    except FileNotFoundError:
        tur = None
    hp.paste_c(img, body, x, y)
    if tur is not None:
        hp.paste_c(img, tur, x, y)
    if covered:
        img = hp.add_c(img, L(f"{ROOT_NAME}-sheen")[(f // 15) % 4], x, y, 1.0)
    return img


def gate_frames():
    """A gate halt on Level 41's ice: two pylons asleep, their domes lit; the wake at 1.0 s; shots spent on the domes
    over the Warden pair; the pylons broken at KILLS (the explosion, the topple, the collapse)."""
    import act6_gear as g6  # noqa: E402
    bg = hp.ice(540, 4111)
    pylons = [dict(x=60, y=90, k=heading_k(330, FACINGS)), dict(x=420, y=90, k=heading_k(30, FACINGS))]
    for p in pylons:
        a = np.radians(face_deg(p["k"]))
        p["dome"] = (p["x"] - np.sin(a) * DOME_D, p["y"] + np.cos(a) * DOME_D)
    tanks = [pylons[0]["dome"], pylons[1]["dome"]]
    kills = [4.2, 6.8]
    wake = 1.0
    ship = hp.ship_img()
    frames = []
    hits = []
    for i in range(int(9.0 * FPS)):
        t = i / FPS
        f = int(t * 60)
        img = bg.copy()
        sx = 240 + 70 * np.sin(t * 0.7)
        alive = [t < k for k in kills]
        for j, (tx, ty) in enumerate(tanks):
            img = warden(img, tx, ty, hp.heading_to(tx, ty, sx, 470), covered=alive[j], f=f)
        for j, p in enumerate(pylons):
            if alive[j]:
                img = pylon(img, p["x"], p["y"], p["k"], awake=t >= wake, f=f)
                img, _ = dome_at(img, p["x"], p["y"], p["k"], f)
            else:
                u = t - kills[j]
                w = L(f"{ROOT_NAME}-wreck")[0]
                hp.paste_c(img, w, p["x"], p["y"])
                hp.paste_c(img, L(f"{ROOT_NAME}-topple")[min(5, int(u * 15))], p["x"], p["y"])
                n = int(u * 30)
                e = hp.explosion("explosion-vacuum-medium", n)
                if e is not None:
                    img = hp.add_c(img, e, p["x"], p["y"])
                k6 = int(u * 15)
                if k6 < 6:
                    uu = (k6 + 0.5) / 6
                    cx, cy = p["dome"]
                    img = g6.paste_light(img, L(f"{ROOT_NAME}-dome-collapse")[k6], cx + (p["x"] - cx) * uu,
                                         cy + (p["y"] - cy) * uu)
        # shots: the ship's fire spent on the domes over the tanks (a spark each 0.15 s), then on the pylons
        for j, (tx, ty) in enumerate(tanks):
            if alive[j] and 1.4 < t and int(t / 0.15) != int((t - 1 / FPS) / 0.15) and t < kills[j] - 1.2:
                hits.append((t, tx + 6 * np.sin(t * 7), ty - 14))
        for t0, hx, hy in hits:
            n = int((t - t0) * 30)
            if 0 <= n < 6:
                img = g6.paste_light(img, L(f"{ROOT_NAME}-dome-hit")[n], hx, hy)
        hp.paste_c(img, ship, sx, 470)
        raster.draw_text(img, 8, 8, "PYLONS %d / 2" % sum(not a for a in alive), raster.LABEL)
        frames.append(img)
    return frames


def gold_share(img):
    return e60.gold_share(img)


def readability():
    """The pylon over Level 41's darkest and brightest ice windows (1x and 2x) with the gold share."""
    bg = hp.ice(540, 4111)
    cells = []
    sp = Image.new("RGBA", (CELL, CELL), (0, 0, 0, 0))
    sp.alpha_composite(L(ROOT_NAME)[0])
    sp.alpha_composite(L(f"{ROOT_NAME}-crown")[3])
    for mode in ("dark", "bright"):
        w = e72.window(bg, 80, 80, mode).copy()
        hp.paste_c(w, sp, 40, 40)
        cells += [w, w.resize((160, 160), Image.NEAREST)]
    return cells, gold_share(sp)


def livery_split():
    """1:1 crop: the elite pylon beside a regular Warden Tank (the Act 5 gunmetal) on the ice, and 3x."""
    c = hp.ice(140, 4112).crop((0, 0, 160, 100))
    c = pylon(c, 48, 50, 4)
    c = warden(c, 116, 52, 200)
    return [c, c.resize((480, 300), Image.NEAREST)]


def review():
    artkit.REVIEW_ROUND = ROUND
    gif = gate_frames()
    read, share = readability()
    total = sum(hp.atlas_area(load(n)) for n, *_ in sets() + dome_sets())
    rows = [
        ("SHIELD-PYLON (THE BASE) AND -CROWN_0..7 OVER IT (THE FACINGS, THE EMITTER DARK), 2X",
         [L(ROOT_NAME)[0]] + [Image.alpha_composite(L(ROOT_NAME)[0], c) for c in L(f"{ROOT_NAME}-crown")[:8]], 2,
         False),
        ("-CROWN_8..15 OVER THE BASE, 2X", [Image.alpha_composite(L(ROOT_NAME)[0], c)
                                            for c in L(f"{ROOT_NAME}-crown")[8:]], 2, False),
        ("-EMITTER_0..3 (ADDITIVE, 3X), THE LENS LIT ON FACINGS 0, 4, 8, 12 (2X)",
         [sprite.enlarge(e, 3) for e in L(f"{ROOT_NAME}-emitter")], 1, True),
        ("THE PYLON AWAKE AT FACINGS 0, 4, 8, 12 (2X, THE EMITTER'S GLOW ON THE LENS)", [awake_cell(k) for k in
                                                                                          (0, 4, 8, 12)], 2, False),
        ("-WRECK AND -TOPPLE_0..5 (THE CROWN FALLING TOWARD THE LOWER RIGHT; FRAME 5 STAYS), 2X",
         [L(f"{ROOT_NAME}-wreck")[0]] + [topple_cell(i) for i in range(6)], 2, False),
        (f"READABILITY OVER LEVEL 41'S DARKEST AND BRIGHTEST ICE (1X, 2X), GOLD {100 * share:.0f} % OF THE "
         f"SILHOUETTE; THE LIVERY SPLIT: THE ELITE PYLON BESIDE A REGULAR WARDEN TANK (1:1, 3X)", read
         + livery_split(), 1, False),
        ("THE GATE HALT (1X, CROPPED): ASLEEP (THE DOMES LIT, THE EMITTERS DARK), AWAKE AND SHOTS SPENT ON THE DOMES",
         [gif[k].crop((0, 0, 480, 400)) for k in (8, 50)], 1, False),
        ("THE FIRST PYLON BROKEN (THE TOPPLE, THE COLLAPSE INTO THE CROWN), THE GATE OPEN",
         [gif[k].crop((0, 0, 480, 400)) for k in (int(4.4 * FPS), int(8.5 * FPS))], 1, False)]
    sheet = artkit.review_sheet("SHIELD PYLON - FINAL FRAMES (ROUND 72 A.5 A, THE CROWN)", rows, width=1500,
                                batch=BATCH)
    sheet = hp.with_notes(sheet, [
        f"ATLAS: {total:,} PX = {100 * total / 2048 ** 2:.0f} % OF ONE 2048X2048 PAGE (2 PX PADDING), THE CLAIMED DOME "
        "SETS INCLUDED (THE DOME ALONE IS 12 FRAMES OF 232X232).",
        "THE DOME, ITS HIT, COLLAPSE, FEED AND SHEEN ARE ACT6_GEAR.PY'S (ROUND 73 F.3), CLAIMED BY THE PYLON'S ROOT."])
    png, gifp = CONCEPT / f"{ROOT_NAME}-final-{ROUND}-a.png", CONCEPT / f"{ROOT_NAME}-final-{ROUND}-a.gif"
    sheet.convert("RGB").save(png, optimize=True)
    size = write_gif([g.crop((0, 0, 480, 420)) for g in gif], gifp, fps=FPS, colors=128, max_mb=8.0)
    print(f"review: {png.relative_to(ROOT)}, {gifp.name} ({size / 1e6:.1f} MB)")


def awake_cell(k):
    c = Image.new("RGBA", (CELL, CELL), (20, 24, 32, 255))
    return pylon(c, CELL / 2, CELL / 2, k, awake=True)


def topple_cell(i):
    c = Image.new("RGBA", (TOPPLE, TOPPLE), (0, 0, 0, 0))
    hp.paste_c(c, L(f"{ROOT_NAME}-wreck")[0], TOPPLE / 2, TOPPLE / 2)
    c.alpha_composite(L(f"{ROOT_NAME}-topple")[i])
    return c


# =========================================================================== sets, check, main

def sets():
    return [(ROOT_NAME, 1, (CELL, CELL), False), (f"{ROOT_NAME}-crown", FACINGS, (CELL, CELL), False),
            (f"{ROOT_NAME}-emitter", 4, (EMIT, EMIT), True), (f"{ROOT_NAME}-wreck", 1, (CELL, CELL), False),
            (f"{ROOT_NAME}-topple", len(TOPPLE_STEPS), (TOPPLE, TOPPLE), False)]


def dome_sets():
    return [(f"{ROOT_NAME}-dome", 12, (232, 232), True), (f"{ROOT_NAME}-dome-hit", 6, (32, 32), True),
            (f"{ROOT_NAME}-dome-collapse", 6, None, True), (f"{ROOT_NAME}-feed", 1, (32, 12), True),
            (f"{ROOT_NAME}-sheen", 4, (48, 48), True)]


def extra_checks():
    piv = load_pivots()
    out = [(len(piv.get("lens", [])) == FACINGS and "dome" in piv, str(pivot_path().relative_to(ROOT)))]
    path = artkit.SPRITES / "intel" / f"{ROOT_NAME}.png"
    out.append((path.exists() and Image.open(path).text.get(artkit.SOURCE_KEY, "").startswith("tools/art/"),
                str(path.relative_to(ROOT))))
    return out


def check():
    import act6_gear  # noqa: E402
    ok = hp.check(sets(), ROOT_NAME, SOURCE, extra=extra_checks)
    print("the claimed dome sets (act6_gear.py's Source note):")
    ok2 = hp.check(dome_sets(), ROOT_NAME + " dome", act6_gear.SOURCE)
    return ok and ok2


BUILDERS = {"pylon": build_pylon, "dome": build_dome, "intel": build_intel}

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    review()
