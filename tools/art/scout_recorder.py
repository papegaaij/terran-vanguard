#!/usr/bin/env python3
"""Production art: Level 40's secret (M10 part F batch, concept round 78, step ART-F2): Sentry Two's flight recorder
and the dead CDF scout it tumbles with (design/campaign/act-6-jovian-storm/level-40-honour-guard, *Secrets and
pickups*: "the flight-recorder pod of a CDF scout lost at Callisto in 2184, still blinking, tumbling with its dead
scout"; Level 30's breakable-object rule: its housing takes 4 hits, then the hidden crate falls out).

New models on the frozen render package (no round-72 proposal): the scout is the CDF's Stormhawk airframe
(render.models ``ship_c_model``, the fleet's grey and blue of tools/art/jovian_units.py's ``fleet-stormhawk``) dead
a year in vacuum: its engines and canopy dark, the right wing torn off at mid-span, the hull scorched; the recorder is
a hazard-orange crash-survivable pod (a white reflective band, dark end caps, a torn cable stub) with its beacon
still blinking white. Vacuum rules (design/world/belt#vacuum-rules): the break flashes short and throws its shell
chips out in a ring, no rising smoke. Headings as tools/art/README.md: tumble frame k turns k x 22.5 degrees
clockwise, every frame its own render under the fixed key light. The beacon is a smooth float glow written
unquantised (no ordered dither, far more than 32 steps), additive (premultiplied on black).

One sprite root, ``debris-sentry-recorder``, so one claim covers the set (tools/art/belt_units.py ``claimed``: a
level's tow look, the pipeline's ``SpriteUse.TOW_LOOKS``; step F4 adds Level 40's). Outputs (build/art-held/sprites/
until then; rerun once the look is in and the files move to assets/sprites/):
  debris-sentry-recorder_0..15        24x24: the recorder tumbling (one turn in 16 frames; Level 30's buoy turns in
                                      2.4 s), its housing the 20x20 hit box
  debris-sentry-recorder-glow_0..15   24x24 additive: its beacon's light at each tumble frame's lamp (white core,
                                      pale amber halo; never red: the sniper's colour), drawn over the same frame at
                                      the blink's strength (on 0.16 s once a second)
  debris-sentry-recorder-break_0..7   56x56 normal, centred on the recorder: the housing shot open: a white flash,
                                      the orange shell chips flung out in a ring, a short grey dust ring, the crate's
                                      amber light at the core fading (0.07 s a frame: Level 30's step)
  debris-sentry-recorder-scout_0..15  48x48: the dead scout tumbling slowly (one turn in 16 frames; drawn at
                                      (-16, +22) px from the recorder, y up, under it), drifting on after the break
Review (round 78): design/campaign/act-6-jovian-storm/level-40-honour-guard/concept/
scout-recorder-final-r78-a.png/.gif on Level 40's production backdrop (tools/art/backdrop_l40.py's tiles at t~50).

Run: python3 tools/art/scout_recorder.py [--review] [--check]
     (~30 s; --review rebuilds only the review files; --check lists every set with its folder, size and frame count
     and fails on a missing one, a missing Source chunk, a checker over 5 % or a beacon under 32 levels)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU

from jovkit import additive_rgb, checker_share, glow, rgba  # noqa: E402
from render import models, raster  # noqa: E402
from render.enemy_rigs import local_to_screen  # noqa: E402
from render.sdf import Material, rotate_y, sd_capsule, sd_cylinder_y, sd_sphere, union  # noqa: E402

SCRIPT = "scout_recorder.py"
BATCH = "M10 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r78"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "campaign" / "act-6-jovian-storm" / "level-40-honour-guard" / "concept"
ROOT_NAME = "debris-sentry-recorder"
REC, REC_EXT = 24, 1.2
SCOUT, SCOUT_EXT = 48, 2.5
BREAK = 56
SCOUT_OFFSET = (-16, 22)                       # px from the recorder to the scout's centre (y up)
LAMP = (0.0, 0.16, 0.2)                        # the beacon on the recorder's top (model units, +Y its long axis)
ORANGE = np.array((240, 112, 28.0))
FLEET_PAL = {"hull": (0.62, 0.64, 0.68), "hull_dark": (0.3, 0.32, 0.36), "accent": (0.16, 0.40, 0.90),
             "accent2": (0.92, 0.92, 0.94), "engine": (0.55, 0.8, 1.0)}


def save_set(name, frames):
    bu.write(name, frames, SOURCE)


# =========================================================================== models

def recorder_model():
    """The flight recorder, its long axis +Y: a hazard-orange capsule with a white reflective band, dark end caps, a
    torn cable stub out of its tail and the beacon lamp on its top (dark: the blink is the glow set)."""
    mats = [Material((0.94, 0.42, 0.1), metal=0.2, shininess=50, spec=0.5),
            Material((0.92, 0.92, 0.9), metal=0.2, shininess=70, spec=0.8),
            Material((0.16, 0.16, 0.18), metal=0.6, shininess=40, spec=0.4),
            Material((0.6, 0.6, 0.58), metal=0.4, shininess=30, spec=0.3)]

    def scene(p):
        y = p[:, 1]
        body = sd_capsule(p, (0, -0.2, 0.0), (0, 0.2, 0.0), 0.17, 0.17)
        caps = sd_cylinder_y(p, (0, 0.0, 0.0), 0.175, 0.3)
        cable = sd_capsule(p, (0, -0.34, 0.0), (0.06, -0.5, -0.02), 0.025, 0.02)
        lamp = sd_sphere(p, LAMP, 0.05)
        d, m = union((body, 0), (cable, 3), (lamp, 1), k=0.01)
        m = np.where((m == 0) & (np.abs(y) < 0.045), 1, m)
        m = np.where((m == 0) & (np.abs(y) > 0.3) & (caps < 0.02), 2, m)
        return d, m
    return scene, mats


def scout_model():
    """The dead scout: the CDF Stormhawk airframe in the fleet's grey and blue, engines and canopy dark, the right
    wing torn off at mid-span (a jagged cut), listing 20 degrees."""
    scene, mats = models.ship_c_model(bank=0.0, palette=FLEET_PAL)
    mats = list(mats)
    mats[models.ENGINE] = Material((0.08, 0.08, 0.09))
    mats[models.GLASS] = Material((0.05, 0.06, 0.08), metal=0.6, shininess=80, spec=0.5)

    def dead(p):
        p = rotate_y(p, 0.35)
        d, m = scene(p)
        x, y = p[:, 0], p[:, 1]
        cut = 0.46 + 0.05 * np.sin(y * 31.0) + 0.03 * np.sin(y * 71.0)      # the torn edge
        d = np.maximum(d, x - cut)
        return d, m
    return dead, mats


def _recorder(k):
    scene, mats = recorder_model()
    return bu.render(*bu.turned(scene, mats, k, 16), REC, REC_EXT, crisp=60)


def _scout(k):
    scene, mats = scout_model()
    return bu.render(*bu.turned(scene, mats, k, 16), SCOUT, SCOUT_EXT, crisp=60)


JOBS = {"recorder": _recorder, "scout": _scout}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


def scorch(img, seed, k=0.6):
    """A year dead in vacuum: darker, mottled by soot, a few scorch streaks (no embers: nothing burns)."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    a[..., :3] *= (k + 0.4 * n)[..., None]
    a[~op] = 0
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


# =========================================================================== the beacon and the break

def lamp_xy(k):
    """The beacon's centre on tumble frame k (px from the frame's top left): bu.turned's heading k of 16."""
    dx, dy = local_to_screen(LAMP[0], LAMP[1], np.pi / 2 + TAU * k / 16, REC / REC_EXT)
    return REC / 2 + dx, REC / 2 + dy


def beacon_frames():
    frames = []
    for k in range(16):
        light = np.zeros((REC, REC, 3))
        x, y = lamp_xy(k)
        glow(light, x, y, 0.9, (255, 250, 236), 2.2)
        glow(light, x, y, 3.0, (255, 214, 160), 0.24)
        frames.append(additive_rgb(light))
    return frames


def break_frames(n=8, w=BREAK, seed=4081):
    """The housing shot open in vacuum: a white flash (two frames), orange shell chips flung straight out in a ring
    (no gravity, no smoke), a thin grey dust ring expanding and fading, the crate's amber light at the core fading."""
    rng = np.random.default_rng(seed)
    chips = [(TAU * i / 12 + rng.uniform(-0.2, 0.2), rng.uniform(55, 95), rng.uniform(1.6, 3.2), rng.uniform(0, TAU),
              i % 4 == 0) for i in range(12)]
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
               + np.array((255, 176, 70.0)) * core[..., None])
        alpha = np.clip(ring + flash + core, 0, 1)
        img = rgba(rgb / np.maximum(alpha, 1e-6)[..., None], alpha)
        d = ImageDraw.Draw(img)
        for ang, speed, size, spin, white in chips:
            dist_c = speed * 0.28 * u
            x, y = c + np.cos(ang) * dist_c, c + np.sin(ang) * dist_c
            a = spin + u * 9
            pts = [(x + np.cos(a + k * 2.1) * size, y + np.sin(a + k * 2.1) * size * 0.7) for k in range(3)]
            lit = 0.55 + 0.45 * np.cos(a + 0.8)
            base = np.array((236, 236, 230.0)) if white else ORANGE
            col = tuple(int(v) for v in np.clip(base * (0.45 + 0.6 * lit), 0, 255))
            if u < 0.94:
                d.polygon(pts, fill=col + (255,))
        frames.append(img)
    return frames


# =========================================================================== build

def build():
    res = run([("recorder", k) for k in range(16)] + [("scout", k) for k in range(16)])
    rec = artkit.quantize_set([scorch(f, 4071 + i, 0.8) for i, f in enumerate(res[:16])], 32)
    scout = artkit.quantize_set([scorch(f, 4091 + i, 0.62) for i, f in enumerate(res[16:])], 40)
    save_set(ROOT_NAME, rec)
    save_set(ROOT_NAME + "-glow", beacon_frames())
    save_set(ROOT_NAME + "-break", break_frames())
    save_set(ROOT_NAME + "-scout", scout)


SETS = [(ROOT_NAME, 16, (REC, REC)), (ROOT_NAME + "-glow", 16, (REC, REC)), (ROOT_NAME + "-break", 8, (BREAK, BREAK)),
        (ROOT_NAME + "-scout", 16, (SCOUT, SCOUT))]


# =========================================================================== review

def plate(t):
    import backdrop_l40 as bd  # noqa: E402  (the production backdrop's tiles)
    return bd.layer_tiles(t)


def review():
    rec, gl, br, sc = (bu.load(n) for n, _, _ in SETS)
    bg = plate(50.0)
    sheet = raster.sheet(1000, 640, "SENTRY TWO'S RECORDER (FINAL R78): LEVEL 40'S SECRET",
                         f"PRODUCTION ART, {BATCH.upper()} - R78")
    raster.draw_text(sheet, 16, 38, "THE RECORDER TUMBLING (16 FRAMES, 3X), EACH WITH ITS BEACON ADDED; THE DEAD SCOUT "
                                    "(16 FRAMES, 2X); THE BREAK (8 FRAMES, 2X): ON LEVEL 40'S SPACE AT T=50", raster.LABEL)
    for k in range(16):
        cell = bg.crop((k * 26, 300, k * 26 + 28, 328)).copy()
        cell = l23kit.paste_c(cell, rec[k], 14, 14)
        cell = l23kit.add_c(cell, gl[k], 14, 14)
        sheet.alpha_composite(cell.resize((84, 84), Image.NEAREST), (16 + (k % 8) * 90, 54 + (k // 8) * 90))
    for k in range(16):
        cell = bg.crop((k * 28, 100, k * 28 + 52, 152)).copy()
        cell = l23kit.paste_c(cell, sc[k], 26, 26)
        sheet.alpha_composite(cell.resize((104, 104), Image.NEAREST), (16 + (k % 8) * 110, 240 + (k // 8) * 110))
    for f in range(8):
        cell = bg.crop((f * 50, 380, f * 50 + 60, 440)).copy()
        cell = l23kit.paste_c(cell, br[f], 30, 30)
        sheet.alpha_composite(cell.resize((112, 112), Image.NEAREST), (16 + f * 120, 466))
    raster.draw_text(sheet, 760, 54, "AT 1X ON THE BACKDROP:", raster.LABEL_DIM)
    one = bg.crop((0, 0, 200, 120)).copy()
    one = l23kit.paste_c(one, sc[3], 70 + SCOUT_OFFSET[0], 60 - SCOUT_OFFSET[1])
    one = l23kit.paste_c(one, rec[5], 70, 60)
    one = l23kit.add_c(one, gl[5], 70, 60)
    one = l23kit.paste_c(one, artkit.load_frames("ship")[2], 160, 90)
    sheet.alpha_composite(one, (760, 70))
    frames = []
    shot = 4.0
    for i in range(70):
        s = i / 10
        img = plate(50.0 + s)
        x, y = 70 + 3 * s, -20 + 90 * s
        img = l23kit.paste_c(img, sc[int(s * 16 / 8.0) % 16], x + SCOUT_OFFSET[0], y - SCOUT_OFFSET[1])
        if s < shot:
            k = int(s * 16 / 2.4) % 16
            img = l23kit.paste_c(img, rec[k], x, y)
            if s % 1.0 < 0.16:
                img = l23kit.add_c(img, gl[k], x, y)
        else:
            f = int((s - shot) / 0.07)
            if f < 8:
                img = l23kit.paste_c(img, br[f], x, y)
        img = l23kit.paste_c(img, artkit.load_frames("ship")[2], 150, 470)
        frames.append(img.crop((0, 0, 300, 540)))
    artkit.save_review(sheet, frames, CONCEPT, "scout-recorder", fps=10)


# =========================================================================== check

def check():
    bad = 0
    for name, n, size in SETS:
        where = bu.where(name)
        try:
            frames = bu.load(name)
        except FileNotFoundError:
            frames = []
        folder = ROOT / where
        files = [f for f in sorted(folder.glob(f"{name}_*.png")) if f.stem.rsplit("_", 1)[0] == name]
        src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        levels = 99
        if name.endswith("-glow"):
            levels = max(len(np.unique(np.array(f.convert("RGB")).max(axis=-1))) for f in frames) if frames else 0
        ok = len(frames) == n and all(fr.size == size for fr in frames) and src_ok and share < 0.05 and levels >= 32
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}"
              f", checker {share * 100:.1f} %" + (f", {levels} levels" if name.endswith("-glow") else ""))
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        build()
    review()
