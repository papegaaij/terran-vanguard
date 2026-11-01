#!/usr/bin/env python3
"""Production art: Level 42's craft on the play plane (M10 part H batch, concept round 80, step ART-H2): the **CDF
frigate pair** trading fire with the home fleet's cruiser A (design/campaign/act-6-jovian-storm/level-42-ascendant,
*Layout*: "a CDF frigate pair trading fire with cruiser A on the right flank (t~26-36, friendly marker)"; white, so a
gold-and-black hull is always the enemy's and a white one ours) and the **dead home-fleet tender** with its sealed
strongbox, the level's secret (*Secrets and pickups*: "a home-fleet supply tender the Vrell boarded and picked clean,
its strongbox still sealed on its spine"; Level 30's breakable-object rule: the housing takes 4 hits, then the crate).

New models on the frozen render package (no round-72 proposal; the round's rows are production art): the frigate is a
CDF warship in the fleet's white with blue bands (the Steadfast group's escorts: tools/art/outro_l14_images.py's heavy
transport is the carrier's kin, the Stormhawks' blue): a long white hull, a raised spine, the bridge forward, two twin
turrets on the spine turned to port (toward cruiser A on the left flank), grey radiator vanes aft, three engines; the
tender is an Ascendancy supply hull in gloss black with gold trim (the Regent cruisers' livery, Level 28): a boxy hull
with cargo pods along both sides, two torn off and one split open, the bridge forward, its plates blistered with
the Vrell's dull teal-black resin where they boarded (dead: nothing glows), the strongbox's gold cradle on its spine;
it drifts dead, turned 25 degrees, and never turns (one render). The strongbox is a gunmetal armoured case with gold
bands and the Ascendancy's gold seal, its seal lamp still breathing gold. Vacuum rules (design/world/belt#vacuum-rules):
the break flashes short and throws its shell chips out in a ring, no rising smoke. Every glow is a smooth float field
written unquantised (no ordered dither, far more than 32 steps), additive (premultiplied on black). The friendly
marker is the game's (FriendlyMarks: HUD-green brackets drawn in code), not baked into the art.

Outputs (build/art-held/sprites/ until the data claims the roots - tools/art/l23kit.py's rule; the frigates once
vanguard.pipeline.SpriteUse names ``cdf-frigate`` (step H4a), the tender once its tow look is in (``belt_units.py``'s
``TOW_LOOKS`` 42, step H4a); rerun then and the files move to assets/sprites/):
  cdf-frigate                   56x144: the frigate, bow up (40 colours)
  cdf-frigate-glow_0..7         56x144 additive, 4 fps: its three engines (blue-white, steady, a short plume) and
                                its white strobes (bow, stern, the radiator tips) flashing in turn (never red)
  cdf-frigate-muzzle_0..3       16x16 additive, 20 fps: a turret's flash (pale blue-white: never an enemy's amber)
  cdf-frigate-tracer            16x4 additive: one dash of its tracer, its head to the left (the game draws bursts of
                                dashes toward cruiser A; presentation only)
  pivots/cdf-frigate.json       the turrets' twin muzzles and the engines (px from the top left)
  debris-home-tender            24x24: the strongbox on the tender's spine (one frame: it never turns), its housing
                                the 20x20 hit box
  debris-home-tender-glow_0..7  24x24 additive, 4 fps: its seal lamp breathing gold
  debris-home-tender-break_0..7 56x56 normal, centred on the strongbox: the housing shot open: a white flash, the
                                gunmetal and gold shell chips flung out in a ring, a short grey dust ring, the crate's
                                amber light at the core fading (0.07 s a frame: Level 30's step)
  debris-home-tender-hull       96x136: the dead tender (one frame), drawn with its centre at ``HULL_OFFSET`` px from
                                the strongbox (y up), under it; it drifts on at the tow's place after the break
  pivots/debris-home-tender.json  the hull's offset from the strongbox
Review (round 80): design/campaign/act-6-jovian-storm/level-42-ascendant/concept/l42-props-final-r80-a.png/.gif on
Level 42's production backdrop (tools/art/backdrop_l42.py's tiles; run it first).

Run: python3 tools/art/l42_props.py [--review] [--check]
     (~1 min; --review rebuilds only the review files; --check lists every set with its folder, size and frame count
     and fails on a missing one, a missing Source chunk, a checker over 5 % or a glow under 32 levels)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU

from jovkit import additive_rgb, checker_share, glow, rgba, soft_box  # noqa: E402
from render import raster  # noqa: E402
from render.sdf import (Material, mirror_x, panel_lines, rotate_z, sd_box, sd_capsule, sd_cylinder_y,  # noqa: E402
                        sd_cylinder_z, sd_plate, sd_sphere, subtract, union)

SCRIPT = "l42_props.py"
BATCH = "M10 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r80"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "campaign" / "act-6-jovian-storm" / "level-42-ascendant" / "concept"
FRIGATE = "cdf-frigate"
TENDER = "debris-home-tender"
FR_SIZE, FR_PPU = (56, 144), 66.0                       # px per model unit (the hull 2.0 units long: 132 px)
TD_SIZE, TD_PPU, TD_TURN = (96, 136), 50.0, 0.44         # the tender turned 25 degrees (clockwise > 0 on the screen)
BOX, BOX_PPU = 24, 22.0
BREAK = 56
CRADLE = (0.0, 0.08)                                    # the strongbox's cradle on the tender's spine (model units)
TURRETS = [(0.0, 0.62, 0.1), (0.0, -0.22, 0.12)]       # the frigate's twin turrets (model x, y, z)
BARREL = 0.17                                           # their barrels' reach to port
ENGINES = [(-0.11, -1.02), (0.0, -1.02), (0.11, -1.02)]
STROBES = [(0.0, 0.97), (0.0, -0.96), (-0.41, -0.52), (0.41, -0.52)]
WHITE = np.array((255, 255, 255.0))
BLUE = np.array((120, 180, 255.0))
GOLD = np.array((255, 196, 64.0))
ORANGE = np.array((255, 176, 70.0))


def to_px(size, ppu, x, y, turn=0.0):
    """Model (x, y) -> sprite px (y down) for a scene sampled at rotate_z(p, -turn) (turned ``turn`` clockwise)."""
    c, s = np.cos(turn), np.sin(turn)
    sx, sy = c * x + s * y, -s * x + c * y
    return size[0] / 2 + sx * ppu, size[1] / 2 - sy * ppu


# =========================================================================== models

def frigate_model():
    """The CDF frigate, bow +Y: a white hull with blue bands, the spine and bridge, two twin turrets turned to port,
    grey radiator vanes aft, three dark engine bells."""
    mats = [Material((0.86, 0.88, 0.9), metal=0.25, shininess=60, spec=0.6, pattern=panel_lines(9.0, 0.05, 0.86)),
            Material((0.16, 0.40, 0.90), metal=0.3, shininess=60, spec=0.6),
            Material((0.24, 0.26, 0.3), metal=0.5, shininess=40, spec=0.4),
            Material((0.05, 0.08, 0.14), metal=0.6, shininess=90, spec=0.8),
            Material((0.6, 0.62, 0.66), metal=0.3, shininess=30, spec=0.3)]
    hull = [(0.0, 1.0), (0.1, 0.75), (0.17, 0.35), (0.19, -0.3), (0.21, -0.75), (0.16, -0.98), (-0.16, -0.98),
            (-0.21, -0.75), (-0.19, -0.3), (-0.17, 0.35), (-0.1, 0.75)]

    def scene(p):
        y = p[:, 1]
        items = [(sd_plate(p, hull, 0.0, 0.06, 0.015), 0),
                 (sd_box(p, (0, -0.05, 0.07), (0.07, 0.55, 0.035), 0.01), 0),
                 (sd_box(p, (0, 0.42, 0.11), (0.06, 0.08, 0.04), 0.01), 0),
                 (sd_box(mirror_x(p), (0.3, -0.52, 0.0), (0.11, 0.17, 0.008), 0.004), 4)]
        for tx, ty, tz in TURRETS:
            items.append((sd_cylinder_z(p, (tx, ty, tz), 0.055, 0.025), 2))
            for dy in (-0.016, 0.016):
                items.append((sd_capsule(p, (tx - 0.02, ty + dy, tz + 0.012), (tx - BARREL, ty + dy, tz + 0.012),
                                         0.011, 0.009), 2))
        for ex, ey in ENGINES:
            items.append((sd_cylinder_y(p, (ex, ey + 0.04, 0.02), 0.045, 0.05), 2))
        d, m = union(*items, k=0.01)
        m = np.where((m == 0) & ((np.abs(y - 0.18) < 0.035) | (np.abs(y + 0.62) < 0.025)), 1, m)
        m = np.where((m == 0) & (y > 0.47) & (y < 0.5) & (p[:, 2] > 0.12), 3, m)
        return d, m
    return scene, mats


def tender_model():
    """The home-fleet supply tender, bow +Y: gloss black with gold trim, cargo pods along both sides (two torn off,
    one split open), the bridge forward, the engine block aft, the strongbox's gold cradle on the spine, blisters of
    dull teal-black Vrell resin where they boarded, two breaches."""
    mats = [Material((0.06, 0.06, 0.07), metal=0.6, shininess=110, spec=1.0, pattern=panel_lines(7.0, 0.05, 0.7)),
            Material((0.85, 0.66, 0.28), metal=0.7, shininess=70, spec=0.8),
            Material((0.2, 0.2, 0.22), metal=0.5, shininess=40, spec=0.4),
            Material((0.07, 0.15, 0.13), metal=0.0, shininess=20, spec=0.25),
            Material((0.02, 0.02, 0.025), metal=0.0, shininess=10, spec=0.1)]
    hull = [(0.0, 1.0), (0.14, 0.92), (0.2, 0.6), (0.2, -0.8), (0.16, -1.0), (-0.16, -1.0), (-0.2, -0.8), (-0.2, 0.6),
            (-0.14, 0.92)]
    pods = [(-0.31, 0.45), (0.31, 0.45), (0.31, 0.1), (-0.31, -0.25), (0.31, -0.25), (-0.31, -0.6)]   # two torn off

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        items = [(sd_plate(p, hull, 0.0, 0.08, 0.02), 0),
                 (sd_box(p, (0, 0.74, 0.1), (0.1, 0.1, 0.05), 0.015), 0),
                 (sd_box(p, (0, -0.9, 0.02), (0.15, 0.1, 0.07), 0.015), 2),
                 (sd_box(p, (CRADLE[0], CRADLE[1], 0.09), (0.1, 0.12, 0.02), 0.01), 1)]
        for px_, py_ in pods:
            items.append((sd_box(p, (px_, py_, 0.0), (0.09, 0.15, 0.07), 0.012), 0))
        items.append((sd_box(p, (0.31, -0.6, 0.0), (0.03, 0.06, 0.04), 0.005), 2))    # a torn pod's stub
        d, m = union(*items, k=0.012)
        d, m = subtract((d, m), sd_box(p, (-0.31, -0.25, 0.06), (0.06, 0.11, 0.05), 0.005))   # a pod split open
        for bx, by in ((0.12, -0.42), (-0.1, 0.32)):                                   # the breaches
            d, m = subtract((d, m), sd_sphere(p, (bx, by, 0.1), 0.07))
        caps = np.zeros_like(x, dtype=bool)
        for px_, py_ in pods:
            caps |= (np.abs(x - px_) < 0.1) & (np.abs(np.abs(y - py_) - 0.14) < 0.025)
        m = np.where((m == 0) & caps, 1, m)
        m = np.where((m == 0) & (y > 0.9), 1, m)                                        # the gold bow cap
        m = np.where((m == 0) & (np.abs(np.abs(x) - 0.19) < 0.012) & (z > 0.05), 1, m)   # gold trim down the sides
        resin = (np.sin(x * 23 + y * 17) + np.sin(y * 31 - x * 9) + 0.6 * np.sin(x * 41 + y * 5)) > 1.15
        m = np.where((m == 0) & resin & (y < 0.55) & (y > -0.75), 3, m)
        for bx, by in ((0.12, -0.42), (-0.1, 0.32)):
            m = np.where(np.hypot(x - bx, y - by) < 0.075, 4, m)
        return d, m
    return scene, mats


def strongbox_model():
    """The strongbox: a gunmetal armoured case, gold bands, the gold seal on its lid, the seal lamp (dark: the glow)."""
    mats = [Material((0.24, 0.25, 0.28), metal=0.6, shininess=70, spec=0.7),
            Material((0.85, 0.66, 0.28), metal=0.7, shininess=70, spec=0.9),
            Material((0.1, 0.08, 0.05), metal=0.2, shininess=40, spec=0.4)]

    def scene(p):
        y = p[:, 1]
        box = artkit.chamfered_box(p, (0, 0, 0.0), (0.34, 0.4, 0.18), 0.06)
        seal = sd_cylinder_z(p, (0, -0.04, 0.18), 0.12, 0.02)
        lamp = sd_sphere(p, (0, 0.28, 0.17), 0.05)
        d, m = union((box, 0), (seal, 1), (lamp, 2), k=0.01)
        m = np.where((m == 0) & (np.abs(np.abs(y) - 0.26) < 0.035), 1, m)
        return d, m
    return scene, mats


def _render(job):
    kind = job
    if kind == "frigate":
        scene, mats = frigate_model()
        return bu.render(scene, mats, FR_SIZE, FR_SIZE[0] / FR_PPU, crisp=60)
    if kind == "tender":
        scene, mats = tender_model()
        return bu.render(lambda p: scene(rotate_z(p, -TD_TURN)), mats, TD_SIZE, TD_SIZE[0] / TD_PPU, crisp=60)
    scene, mats = strongbox_model()
    return bu.render(lambda p: scene(rotate_z(p, -TD_TURN)), mats, BOX, BOX / BOX_PPU, crisp=60)


def scorch(img, seed, k=0.72):
    """Dead in vacuum: darker, mottled by soot."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    a[..., :3] *= (k + 0.4 * n)[..., None]
    a[~op] = 0
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


# =========================================================================== lights

def frigate_glow():
    w, h = FR_SIZE
    frames = []
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    for f in range(8):
        light = np.zeros((h, w, 3))
        for ex, ey in ENGINES:
            x, y = to_px(FR_SIZE, FR_PPU, ex, ey)
            glow(light, x, y, 1.6, (235, 245, 255), 1.4)
            glow(light, x, y + 1, 3.5, BLUE, 0.4)
            u = np.clip((yy - y) / 7.0, 0, 1)
            light += (np.exp(-((xx - x) / (1.0 + 1.5 * u)) ** 2) * (1 - u) ** 2 * (yy > y))[..., None] * BLUE * 0.35
        for j, (sx, sy) in enumerate(STROBES):
            v = {0: 1.0, 1: 0.35}.get((f - 2 * j) % 8, 0.0)
            if v:
                x, y = to_px(FR_SIZE, FR_PPU, sx, sy)
                glow(light, x, y, 1.0, WHITE, 2.2 * v)
                glow(light, x, y, 3.2, (200, 220, 255), 0.3 * v)
        frames.append(additive_rgb(light))
    return frames


def muzzle_frames():
    frames = []
    yy, xx = np.mgrid[0:16, 0:16].astype(np.float64) + 0.5
    for k in (0.6, 1.0, 0.5, 0.22):
        light = np.zeros((16, 16, 3))
        cx, cy = 8.3, 7.8                                   # off the pixel grid's centre: no 4-fold repeats
        glow(light, cx, cy, 1.4 * (0.7 + 0.3 * k), (240, 248, 255), 1.5 * k)
        glow(light, cx, cy, 3.6, BLUE, 0.5 * k)
        spike = np.exp(-((yy - cy) / 0.7) ** 2) * np.exp(-np.abs(xx - cx) / 2.4) * smoothstep_arr(16 - xx)
        light += (spike * 1.1 * k)[..., None] * np.array((220, 236, 255.0))
        frames.append(additive_rgb(light * soft_box(16, 16, 2)[..., None]))
    return frames


def smoothstep_arr(x, a=0.0, b=3.0):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def tracer():
    yy, xx = np.mgrid[0:4, 0:16].astype(np.float64) + 0.5
    core = np.exp(-((yy - 1.85) / 0.65) ** 2)            # off the rows' centre: no mirrored rows
    along = np.exp(-np.clip(xx - 2.5, 0, None) / 5.0) * smoothstep_arr(xx, 0.0, 2.5)
    light = (core * along)[..., None] * np.array((235, 245, 255.0)) * 1.1
    light += (np.exp(-((yy - 1.85) / 1.3) ** 2) * along * 0.4)[..., None] * BLUE
    return additive_rgb(light)


def seal_glow():
    frames = []
    lx, ly = to_px((BOX, BOX), BOX_PPU, 0.0, 0.28, TD_TURN)
    for f in range(8):
        k = 0.55 + 0.45 * (0.5 + 0.5 * np.cos(f * TAU / 8))
        light = np.zeros((BOX, BOX, 3))
        glow(light, lx, ly, 0.9, (255, 236, 190), 1.8 * k)
        glow(light, lx, ly, 2.8, GOLD, 0.28 * k)
        frames.append(additive_rgb(light))
    return frames


def break_frames(n=8, w=BREAK, seed=4281):
    """The housing shot open in vacuum: a white flash (two frames), gunmetal and gold shell chips flung straight out in
    a ring (no gravity, no smoke), a thin grey dust ring expanding and fading, the crate's amber light at the core."""
    rng = np.random.default_rng(seed)
    chips = [(TAU * i / 12 + rng.uniform(-0.2, 0.2), rng.uniform(55, 95), rng.uniform(1.6, 3.2), rng.uniform(0, TAU),
              i % 3 == 0) for i in range(12)]
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    c = w / 2
    frames = []
    for f in range(n):
        u = (f + 0.5) / n
        dist = np.hypot(xx - c, yy - c)
        rr = 4 + 22 * u ** 0.6
        ring = np.exp(-((dist - rr) / (1.6 + 2.5 * u)) ** 2) * 0.55 * (1 - u) ** 1.2
        flash = np.exp(-(dist / (3 + 5 * u)) ** 2) * max(0.0, 1 - 2.6 * u)
        core = np.exp(-(dist / 2.2) ** 2) * max(0.0, 0.9 - 0.9 * u)
        rgb = (np.array((120, 116, 112.0)) * ring[..., None] + np.array((255, 250, 236.0)) * flash[..., None]
               + ORANGE * core[..., None])
        alpha = np.clip(ring + flash + core, 0, 1)
        img = rgba(rgb / np.maximum(alpha, 1e-6)[..., None], alpha)
        d = ImageDraw.Draw(img)
        for ang, speed, size, spin, gold in chips:
            dist_c = speed * 0.28 * u
            x, y = c + np.cos(ang) * dist_c, c + np.sin(ang) * dist_c
            a = spin + u * 9
            pts = [(x + np.cos(a + k * 2.1) * size, y + np.sin(a + k * 2.1) * size * 0.7) for k in range(3)]
            lit = 0.55 + 0.45 * np.cos(a + 0.8)
            base = np.array((216, 168, 72.0)) if gold else np.array((96, 98, 108.0))
            col = tuple(int(v) for v in np.clip(base * (0.45 + 0.6 * lit), 0, 255))
            if u < 0.94:
                d.polygon(pts, fill=col + (255,))
        frames.append(img)
    return frames


# =========================================================================== build

HULL_OFFSET = None


def hull_offset():
    """The hull's centre from the strongbox's (px, y up): the cradle sits on the turned spine."""
    x, y = to_px(TD_SIZE, TD_PPU, CRADLE[0], CRADLE[1], TD_TURN)
    return [int(round(TD_SIZE[0] / 2 - x)), int(round(y - TD_SIZE[1] / 2))]


def write_pivots(root, data):
    """Pivots go with their sprite's folder: assets/pivots when the root is claimed, else build/art-held/pivots."""
    folder = artkit.PIVOTS if bu.claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def build():
    with ProcessPoolExecutor() as pool:
        fr, td, box = pool.map(_render, ["frigate", "tender", "box"])
    bu.write(FRIGATE, artkit.quantize_set([fr], 40), SOURCE, single=True)
    bu.write(FRIGATE + "-glow", frigate_glow(), SOURCE)
    bu.write(FRIGATE + "-muzzle", muzzle_frames(), SOURCE)
    bu.write(FRIGATE + "-tracer", [tracer()], SOURCE, single=True)
    muzzles = []
    for tx, ty, tz in TURRETS:
        for dy in (-0.016, 0.016):
            muzzles.append([round(v, 1) for v in to_px(FR_SIZE, FR_PPU, tx - BARREL, ty + dy)])
    write_pivots(FRIGATE, {"frame": list(FR_SIZE), "muzzles": muzzles,
                           "engines": [[round(v, 1) for v in to_px(FR_SIZE, FR_PPU, ex, ey)] for ex, ey in ENGINES]})
    bu.write(TENDER, artkit.quantize_set([scorch(box, 4291, 0.9)], 32), SOURCE)
    bu.write(TENDER + "-glow", seal_glow(), SOURCE)
    bu.write(TENDER + "-break", break_frames(), SOURCE)
    bu.write(TENDER + "-hull", artkit.quantize_set([scorch(td, 4292, 0.86)], 40), SOURCE, single=True)
    write_pivots(TENDER, {"hull_offset": hull_offset(), "note": "the hull's centre from the strongbox's, px, y up"})


SETS = [(FRIGATE, 1, FR_SIZE), (FRIGATE + "-glow", 8, FR_SIZE), (FRIGATE + "-muzzle", 4, (16, 16)),
        (FRIGATE + "-tracer", 1, (16, 4)), (TENDER, 1, (BOX, BOX)), (TENDER + "-glow", 8, (BOX, BOX)),
        (TENDER + "-break", 8, (BREAK, BREAK)), (TENDER + "-hull", 1, TD_SIZE)]


# =========================================================================== review

def plate(t):
    import backdrop_l42 as bd  # noqa: E402  (the production backdrop)
    return bd.compose(t, units=False)


def review():
    fr, frg, mz, tr = (bu.load(n) for n in (FRIGATE, FRIGATE + "-glow", FRIGATE + "-muzzle", FRIGATE + "-tracer"))
    box, boxg, br, hull = (bu.load(n) for n in (TENDER, TENDER + "-glow", TENDER + "-break", TENDER + "-hull"))
    off = hull_offset()
    piv = json.loads((ROOT / bu.where(FRIGATE)).parent.joinpath("pivots", f"{FRIGATE}.json").read_text())
    bg = plate(30.0)
    sheet = raster.sheet(1000, 700, "LEVEL 42: THE CDF FRIGATES AND THE DEAD TENDER (FINAL R80)",
                         f"PRODUCTION ART, {BATCH.upper()} - R80")
    raster.draw_text(sheet, 16, 38, "THE FRIGATE (2X, 1X, ITS GLOW FRAMES 0-7 AT 1X, THE MUZZLE 4X, THE TRACER 4X); THE "
                                    "TENDER AND ITS STRONGBOX (2X), THE SEAL LAMP (3X), THE BREAK (2X): ON LEVEL 42'S "
                                    "SPACE", raster.LABEL)
    cell = bg.crop((300, 40, 300 + 64, 40 + 152)).copy()
    cell = l23kit.add_c(l23kit.paste_c(cell, fr[0], 32, 76), frg[0], 32, 76)
    sheet.alpha_composite(cell.resize((128, 304), Image.NEAREST), (16, 56))
    for k in range(8):
        c = bg.crop((40 + k * 58, 200, 40 + k * 58 + 56, 344)).copy()
        c = l23kit.add_c(l23kit.paste_c(c, fr[0], 28, 72), frg[k], 28, 72)
        sheet.alpha_composite(c, (156 + k * 60, 56))
    for k in range(4):
        c = Image.new("RGBA", (16, 16), (3, 4, 9, 255))
        c = l23kit.add_c(c, mz[k], 8, 8)
        sheet.alpha_composite(c.resize((64, 64), Image.NEAREST), (156 + k * 70, 216))
    c = Image.new("RGBA", (16, 4), (3, 4, 9, 255))
    c = l23kit.add_c(c, tr[0], 8, 2)
    sheet.alpha_composite(c.resize((128, 32), Image.NEAREST), (440, 232))
    raster.draw_text(sheet, 156, 290, f"MUZZLES {piv['muzzles']}", raster.LABEL_DIM)
    tb = bg.crop((20, 300, 20 + 110, 300 + 150)).copy()
    hx, hy = 55, 75
    tb = l23kit.paste_c(tb, hull[0], hx, hy)
    tb = l23kit.paste_c(tb, box[0], hx - off[0], hy + off[1])
    tb = l23kit.add_c(tb, boxg[0], hx - off[0], hy + off[1])
    sheet.alpha_composite(tb.resize((220, 300), Image.NEAREST), (16, 380))
    for k in range(8):
        c = bg.crop((200 + k * 26, 300, 200 + k * 26 + 24, 324)).copy()
        c = l23kit.add_c(l23kit.paste_c(c, box[0], 12, 12), boxg[k], 12, 12)
        sheet.alpha_composite(c.resize((72, 72), Image.NEAREST), (250 + k * 78, 330))
    for f in range(8):
        c = bg.crop((f * 50, 380, f * 50 + 60, 440)).copy()
        c = l23kit.paste_c(c, br[f], 30, 30)
        sheet.alpha_composite(c.resize((90, 90), Image.NEAREST), (250 + f * 92, 430))
    raster.draw_text(sheet, 250, 540, "AT 1X ON THE BACKDROP (T=30 AND T=44): THE PAIR FIRING TO PORT, THE TENDER",
                     raster.LABEL_DIM)
    one = plate(30.0).crop((280, 120, 480, 400))
    for (x, y) in ((166, 180), (138, 50)):
        one = l23kit.add_c(l23kit.paste_c(one, fr[0], x, y), frg[2], x, y)
        mx, my = piv["muzzles"][0]
        one = l23kit.add_c(one, mz[1], x - FR_SIZE[0] / 2 + mx, y - FR_SIZE[1] / 2 + my)
        for j in range(3):
            one = l23kit.add_c(one, tr[0], x - FR_SIZE[0] / 2 + mx - 20 - 22 * j, y - FR_SIZE[1] / 2 + my)
    sheet.alpha_composite(one.resize((100, 140), Image.BOX), (250, 556))
    two = plate(44.0).crop((240, 0, 440, 280))
    two = l23kit.paste_c(two, hull[0], 90 + off[0], 140 - off[1])
    two = l23kit.paste_c(two, box[0], 90, 140)
    two = l23kit.add_c(two, boxg[0], 90, 140)
    sheet.alpha_composite(two.resize((100, 140), Image.BOX), (360, 556))
    frames = []
    shot = 4.6
    for i in range(70):
        s = i / 10
        img = plate(42.0 + s)
        x, y = 330 - 3 * s, -20 + 80 * s
        img = l23kit.paste_c(img, hull[0], x + off[0], y - off[1])
        if s < shot:
            img = l23kit.paste_c(img, box[0], x, y)
            img = l23kit.add_c(img, boxg[int(s * 4) % 8], x, y)
        else:
            f = int((s - shot) / 0.07)
            if f < 8:
                img = l23kit.paste_c(img, br[f], x, y)
        for k, (fx, fy) in enumerate(((446, 300), (418, 170))):          # the frigates' burst in the same clip
            img = l23kit.add_c(l23kit.paste_c(img, fr[0], fx, fy), frg[int(s * 4 + 3 * k) % 8], fx, fy)
            ph = (s + 0.8 * k) % 1.6
            mx, my = piv["muzzles"][2 * k]
            ox, oy = fx - FR_SIZE[0] / 2 + mx, fy - FR_SIZE[1] / 2 + my
            if ph < 0.15:
                img = l23kit.add_c(img, mz[min(3, int(ph / 0.05))], ox, oy)
            for j in range(3):
                d = (ph - 0.1 * j) * 600
                if 0 < d < 480:
                    img = l23kit.add_c(img, tr[0], ox - d, oy)
        img = l23kit.paste_c(img, artkit.load_frames("ship")[2], 200, 470)
        frames.append(img)
    artkit.REVIEW_ROUND = ROUND                                         # an imported generator may have set its own
    artkit.save_review(sheet, frames, CONCEPT, "l42-props", fps=10)


# =========================================================================== check

def glow_levels(frames):
    return max(len(np.unique(np.array(f.convert("RGB")).max(axis=-1))) for f in frames) if frames else 0


def check():
    bad = 0
    for name, n, size in SETS:
        where = bu.where(name)
        try:
            frames = bu.load(name)
        except FileNotFoundError:
            frames = []
        folder = ROOT / where
        files = ([folder / f"{name}.png"] if n == 1 and (folder / f"{name}.png").exists() else
                 [f for f in sorted(folder.glob(f"{name}_*.png")) if f.stem.rsplit("_", 1)[0] == name])
        src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        additive = name.endswith(("-glow", "-muzzle", "-tracer"))
        levels = glow_levels(frames) if additive else 99
        ok = len(frames) == n and all(fr.size == tuple(size) for fr in frames) and src_ok and share < 0.05 and (
            levels >= 32)
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
              f", checker {share * 100:.1f} %" + (f", {levels} levels" if additive else ""))
    area = sum(n * (s[0] + 2) * (s[1] + 2) for _, n, s in SETS)
    print(f"atlas: {area / 2048 ** 2 * 100:.1f} % of one 2048 page (every frame, padded)")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        build()
    review()
