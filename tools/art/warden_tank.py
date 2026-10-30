#!/usr/bin/env python3
"""Production art: the Warden Tank in its Act 3 unmarked grey livery, Level 19's tracked tank
(design/enemies/ground/warden-tank; M7 part F batch, step ART-F3, review round 42). Straight to
production from the chosen concept round 36 a (tools/concept/enemies_r36.py: the round-05 model of
render/archetype_models.warden_hull / warden_turret in plain grey, no gold, no accent, the red sensors
and the hex panelling kept, the neutral grey rim), on the frozen render package, at the concept's
scale (76 px sprites, 2.3 model units across: the hull ~57 px long, ~52 px wide).

What is new against the concept (pending the user's review):
* the turret's recoil frame: the barrel and its muzzle block slide 0.16 units (~5 px) back into the
  turret for the shot (the turret model is the concept's, re-built here with the barrel movable);
* the muzzle flash (additive, 32 aims x 2 ages), drawn at the muzzle point;
* the turret stub left on the hull when the turret part is shot off (the gun silenced): a torn,
  scorched ring with the breech hole;
* the wreck: the burnt grey hull with the engine deck blown open and the dead turret knocked askew,
  its barrel snapped short; no markings (it rides its belt on as scenery).
* The 0.3 s tell is the turret stopping, a motion, not a look: no frames of its own.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down
(16 hull headings: 4 = facing left, 8 = up, 12 = right; 32 turret aims likewise in 11.25 degree
steps); every heading is its own render under the fixed key light, the patterns turning with the
model. The turret is centred on the hull's centre (the tank's position); the muzzle sits `muzzle` px
along the aim from it (assets/pivots/warden-tank.json).

Outputs (assets/sprites/, or build/art-held/sprites/ (the pivots build/art-held/pivots/) while no data
claims the root `warden-tank`, vanguard.pipeline.SpriteUse: rerun once the tank's data.yaml exists):
  warden-tank_0..31             76x76 the hull, heading * 2 + tread frame (0, 1: the tread bands half
                                a link on)
  warden-tank-turret_0..63      76x76 the turret, aim * 2 + (0 at rest, 1 recoil: the barrel back)
  warden-tank-turret-stub       40x40 the torn turret ring left on the hull (the turret part destroyed)
  warden-tank-flash_0..63       32x32 additive: the muzzle flash, aim * 2 + age (0 the burst, 1 fading),
                                centred on the muzzle point
  warden-tank-wreck_0..15       76x76 the burnt wreck per hull heading (the turret askew, barrel snapped)
  design/enemies/ground/concept/warden-tank-final-r42-a.png/.gif   the review

How the game draws it, back to front: the hull (heading from its path, the tread frame from the
distance driven), the turret (its own aim; the recoil frame for ~0.1 s after each shot, the flash at
the muzzle on the shot), or the stub once the turret is gone; the wreck replaces both on death.

The Act 5 livery (``--livery asc``; M9 part G batch, step ART-G, review round 67): the same model and frames in
the chosen round-60 A.15 **b** (tools/concept/enemies_r60.py ``warden_hull("b")`` / ``warden_turret("b")``: heavier
gold, the track guards, the engine deck's facets and the turret roof's outer ring gold, the white accent, bright red
running lights) in Q.11 b's gunmetal and charcoal (tools/art/belt_units.py ``gunmetal``) with the belt's 1 px gold/red
rim (belt_units.py ``rim``); the wreck burnt black and gold, the stub the livery's ring. Written as
``warden-tank-asc``, ``-asc-turret``, ``-asc-turret-stub``, ``-asc-wreck`` (the same frame layout; the muzzle flash
is the grey tank's ``warden-tank-flash``, the pivots the same numbers in ``pivots/warden-tank-asc.json``), held in
build/art-held/ until the tank's data names the livery (``livery: {sprite: warden-tank-asc}``); review
design/enemies/ground/concept/warden-tank-asc-final-r67-a.png/.gif (on Level 34's Helix hull when its backdrop exists).
Round 67 **G.1 c** (step FIX4): the livery alone casts a faint ground shadow, the look of
design/campaign/act-5-the-belt/level-34-salvage-run/concept/tank-readability-r67-c.png (tools/concept/tank_readability_r67.py
``shadow``): each hull heading's and each wreck's silhouette offset 4, 5 px down-right (away from the top-left sun,
whatever the heading), blurred, 50 % black, written as ``warden-tank-asc-shadow_0..15`` and ``-asc-wreck-shadow_0..15``
(84x84, centred on the tank like the hull); the game draws them under the ground units (TankLooks), the grey tank has
none. ``--shadows`` writes them from the frames already written (no render).

Run: python3 tools/art/warden_tank.py [--livery asc] [--review | --shadows]   (~1 min on 20 cores; renders cached in the system
temp dir, conceptgame-cache/warden-tank/; --review rebuilds the review from the written files)
"""
import contextlib
import hashlib
import math
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r36 as r36  # noqa: E402  (concept round 36, imported unchanged: the grey livery)
from render import archetype_models as am  # noqa: E402
from render import raster, sdf  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials  # noqa: E402
from render.sdf import (Material, sd_box, sd_capsule, sd_cylinder_z, sd_plate, sd_sphere,  # noqa: E402
                        union)

SCRIPT = "warden_tank.py"
BATCH = "M7 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r42"
ROOT_NAME = "warden-tank"
CONCEPT = DESIGN / "enemies" / "ground" / "concept"
DATA = DESIGN / "enemies" / "ground" / "warden-tank" / "data.yaml"
HELD = ROOT / "build" / "art-held"
LIVERY = "grey"                            # "asc": the Act 5 Ascendancy livery (round 60 A.15 b, Q.11 b)


def set_livery(livery):
    """Switch the module to a livery before building (the workers fork with it)."""
    global LIVERY, ROOT_NAME, SOURCE, REVIEW_ROUND, BATCH
    LIVERY = livery
    if livery == "asc":
        ROOT_NAME, BATCH, REVIEW_ROUND = "warden-tank-asc", "M9 part G batch", "r67"
        SOURCE = artkit.source_note(SCRIPT, BATCH)
CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "warden-tank"

SIZE, STUB, FLASH = 76, 40, 32
EXTENT = 2.3                               # model units across the 76 px canvas (the concept's)
S = SIZE / EXTENT                          # px per model unit (33.0)
HEADINGS, TREADS, AIMS = 16, 2, 32
RECOIL = 0.16                              # model units the barrel slides back on the shot
MUZZLE = 1.13                              # the muzzle's tip, model units from the turret's centre
A_TREAD = 7                                # archetype_models.asc appends the tread material


def heading(k, n):
    """Screen angle (atan2, y down) of game heading k of n: 0 = down, clockwise."""
    return np.pi / 2 + TAU * k / n


# =========================================================================== models

def turret_scene(recoil=0.0, snapped=False):
    """render/archetype_models.warden_turret (pivot at the ring centre, barrel +Y) with the barrel and
    its muzzle block movable (``recoil`` model units back) or snapped off short (the wreck)."""
    hexa = [(np.cos(a) * 0.46, np.sin(a) * 0.46 - 0.05)
            for a in np.linspace(np.pi / 6, 2 * np.pi + np.pi / 6, 6, endpoint=False)]

    def scene(p):
        items = [(sd_plate(p, hexa, 0.4, 0.14, 0.0,
                           taper=lambda x, y: np.clip(1.1 - 1.2 * np.sqrt(x * x + y * y), 0.4, 1)), A_HULL),
                 (sd_cylinder_z(p, (0, -0.05, 0.3), 0.5, 0.03), A_GOLD),
                 (sd_sphere(p, (0.14, 0.12, 0.54), 0.05), A_RED),
                 (sd_box(p, (-0.14, -0.16, 0.54), (0.1, 0.08, 0.02), 0.0), A_GUN)]
        if snapped:                         # a short jagged stump of the barrel
            items += [(sd_box(p, (0, 0.42, 0.44), (0.08, 0.18, 0.06), 0.02), A_FACET),
                      (sd_box(p, (0.03, 0.6, 0.45), (0.05, 0.04, 0.05), 0.01), A_GUN)]
        else:
            items += [(sd_box(p, (0, 0.66 - recoil, 0.44), (0.08, 0.42, 0.06), 0.02), A_FACET),
                      (sd_box(p, (0, 1.06 - recoil, 0.44), (0.12, 0.06, 0.08), 0.0), A_GOLD)]
        return union(*items)
    return _gold_roof(scene)


def stub_scene():
    """The turret ring torn open: a scorched collar, jagged shards standing round it, the breech hole."""
    rng = np.random.default_rng(1942)
    shards = [(a, rng.uniform(0.06, 0.16), rng.uniform(0.05, 0.09)) for a in
              np.linspace(0.3, TAU + 0.3, 7, endpoint=False) + rng.uniform(-0.25, 0.25, 7)]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, 0.26), 0.5, 0.04), A_GOLD),
                 (sd_cylinder_z(p, (0, 0, 0.31), 0.44, 0.06), A_HULL)]
        for a, h, w in shards:
            c = (0.38 * math.cos(a), 0.38 * math.sin(a), 0.36 + h / 2 + 0.04)
            items.append((sd_box(sdf.rotate_z(p - sdf.vec(*c), -a), (0, 0, 0), (0.05, w, h), 0.01), A_FACET))
        d, m = union(*items, k=0.02)
        d = np.maximum(d, -sd_cylinder_z(p, (0, 0, 0.4), 0.26, 0.2))      # the breech hole
        return union((d, m), (sd_cylinder_z(p, (0, 0, 0.2), 0.27, 0.02), A_GUN),       # the dark well
                     (sd_sphere(p, (0.06, -0.05, 0.2), 0.07), A_ENGINE),                 # embers in it
                     (sd_sphere(p, (-0.1, 0.08, 0.2), 0.045), A_ENGINE))
    return scene


def hull_scene(tread=0.0):
    scene = am.warden_hull(tread)[0]
    if LIVERY != "asc":
        return scene

    def gilded(p):                          # enemies_r60.warden_hull("b"): gold track guards, gold facets
        d, m = scene(p)
        m = np.where((m == A_TREAD) & (p[:, 2] > 0.17), A_GOLD, m)
        return d, np.where(m == A_FACET, A_GOLD, m)
    return gilded


def _gold_roof(scene):
    """enemies_r60.warden_turret("b"): the turret roof's outer ring gold."""
    if LIVERY != "asc":
        return scene

    def gilded(p):
        d, m = scene(p)
        r = np.hypot(p[:, 0], p[:, 1] + 0.05)
        return d, np.where((m == A_HULL) & (r > 0.3), A_GOLD, m)
    return gilded


def wreck_scene():
    """The hull with its engine deck blown open, the dead turret knocked askew on its ring."""
    hull, tur = hull_scene(0.0), turret_scene(snapped=True)

    def scene(p):
        d1, m1 = hull(p)
        d1 = np.maximum(d1, -sd_sphere(p, (0.16, -0.54, 0.34), 0.2))
        q = p - sdf.vec(0.1, -0.06, -0.05)
        q = sdf.rotate_x(sdf.rotate_z(q, 0.75), 0.16)
        d2, m2 = tur(q)
        d3, m3 = union((sd_capsule(p, (-0.3, 0.2, 0.28), (0.1, 0.4, 0.3), 0.03), A_GUN),
                       (sd_box(p, (-0.2, -0.3, 0.27), (0.12, 0.08, 0.02), 0.01), A_FACET))
        return union((d1, m1), (d2, m2), (d3, m3))
    return scene


def asc_mats(wreck=False, lit=False, tread=0.0):
    """The Act 5 livery: enemies_r60.warden_mats("b") (belt_mats with the white accent, red lights x1.3, the
    tread slot) in Q.11 b's gunmetal; the wreck burnt: the gunmetal near black, the gold scorched, lights out."""
    import belt_units as bu  # noqa: E402  (the Q.11 b kit)
    m = bu.gunmetal(bu.e60.warden_mats("b", 1.0, tread))
    if wreck:
        scorch = _scorch()
        m[A_HULL] = replace(m[A_HULL], albedo=(0.17, 0.17, 0.19), spec=0.35, pattern=scorch)
        m[A_FACET] = replace(m[A_FACET], albedo=(0.13, 0.13, 0.15), spec=0.3, pattern=scorch)
        m[A_GOLD] = replace(m[A_GOLD], albedo=tuple(c * 0.55 for c in m[A_GOLD].albedo), spec=0.35, pattern=scorch)
        m[A_GUN] = replace(m[A_GUN], albedo=(0.3, 0.29, 0.3), spec=0.3, pattern=scorch)
        m[A_RED] = Material((0.05, 0.04, 0.04))
        m[A_ENGINE] = Material((0.06, 0.04, 0.03), emission=(0.25, 0.08, 0.02))
        m[A_TREAD] = replace(m[A_TREAD], albedo=(0.08, 0.08, 0.09))
        if lit:
            m[A_HULL] = replace(m[A_HULL], albedo=(0.35, 0.37, 0.42))
            m[A_ENGINE] = Material((0.06, 0.04, 0.03), emission=(0.8, 0.26, 0.05))
    return m


def mats(wreck=False, lit=False, tread=0.0):
    """The concept's grey livery (enemies_r36.grey_tank): unmarked_mats with the hex panelling and the
    tread slot in dark grey; the wreck burnt: darker, scorched, every light out."""
    if LIVERY == "asc":
        return asc_mats(wreck, lit, tread)
    _, base = am.warden_hull(tread)              # the tread bands' phase lives in the material
    m = r36.unmarked_mats(1.0, hull_pattern=r36.em._hex(9))
    m.append(replace(base[A_TREAD], albedo=r36.UG[1]))
    if wreck:
        scorch = _scorch()
        hexp = r36.em._hex(9)
        m[A_HULL] = replace(m[A_HULL], albedo=(0.4, 0.4, 0.41), spec=0.35,
                            pattern=lambda p, n: hexp(p, n) * scorch(p, n))
        for i in (A_FACET, A_GOLD, A_GUN):
            m[i] = replace(m[i], albedo=tuple(c * 0.8 for c in m[i].albedo), spec=0.35, pattern=scorch)
        m[A_RED] = Material((0.05, 0.04, 0.04))
        m[A_ENGINE] = Material((0.06, 0.04, 0.03), emission=(0.25, 0.08, 0.02))   # embers in the vents
        m[A_TREAD] = replace(m[A_TREAD], albedo=(0.09, 0.09, 0.1))
        if lit:                              # the stub: the hull's own grey round a torn, still glowing hole
            m[A_HULL] = replace(m[A_HULL], albedo=r36.UG[4])
            m[A_ENGINE] = Material((0.06, 0.04, 0.03), emission=(0.8, 0.26, 0.05))
    return m


def _scorch():
    def f(p, n):
        v = (np.sin(p[:, 0] * 7.1 + 0.4) * np.sin(p[:, 1] * 6.3 - 1.1)
             + 0.5 * np.sin(p[:, 0] * 13 + p[:, 1] * 9 + 2.0))
        return np.clip(0.7 + 0.3 * v, 0.4, 1.0)
    return f


# =========================================================================== render jobs

REV = {"hull": 2}                         # the tread frame's material phase


def _job_key(job):
    return hashlib.sha1((repr(job) + f"/{REV.get(job[0], 1)}" + ("" if LIVERY == "grey" else f"/{LIVERY}/2"))
                        .encode()).hexdigest()[:20]


def model_for(job):
    kind = job[0]
    if kind == "hull":
        _, k, tr = job
        return hull_scene(0.5 * tr), mats(tread=0.5 * tr), heading(k, HEADINGS), SIZE
    if kind == "turret":
        _, a, rc = job
        return turret_scene(RECOIL * rc), mats(), heading(a, AIMS), SIZE
    if kind == "stub":
        return stub_scene(), mats(wreck=True, lit=True), heading(0, 1), STUB
    if kind == "wreck":
        _, k = job
        return wreck_scene(), mats(wreck=True), heading(k, HEADINGS), SIZE
    raise ValueError(kind)


def render_job(job):
    if isinstance(job[-1], str) and job[-1] in ("grey", "asc"):     # the livery rides along (forkserver workers)
        if job[-1] != LIVERY:
            set_livery(job[-1])
        job = job[:-1]
    path = CACHE / f"{_job_key(job)}.png"
    if path.exists():
        return Image.open(path).convert("RGBA")
    scene0, m, h, size = model_for(job)
    rot = model_rotation(h)
    scene = (lambda p, s=scene0, r=rot: s(sdf.rotate_z(p, r)))
    hi, factor = artkit.render_hi(scene, model_space_materials(m, rot), (size, size), size / S)
    if LIVERY == "asc":
        import belt_units as bu  # noqa: E402
        img = bu.rim(artkit.native(hi, factor))
    else:
        img = grey_rim(artkit.native(hi, factor))
    CACHE.mkdir(parents=True, exist_ok=True)
    img.save(path)
    return img


def grey_rim(sp):
    """enemies_r36.grey_rim without its per-sprite palette cut (the set is cut to one palette)."""
    a = np.array(sp)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    left, up = ~pad[1:-1, :-2], ~pad[:-2, 1:-1]
    right, down = ~pad[1:-1, 2:], ~pad[2:, 1:-1]
    out = a.astype(np.float64)
    for mask, col, k in (((left | up) & op, r36.LIT_RIM, 0.55),
                         ((right | down) & op & ~(left | up), r36.DARK_RIM, 0.5)):
        out[mask, :3] = out[mask, :3] * (1 - k) + np.array(col) * k
    return Image.fromarray(out.astype(np.uint8), "RGBA")


# =========================================================================== 2D effects

FLASH_RAMP = [(0.0, (255, 255, 236)), (0.3, (255, 226, 120)), (0.6, (255, 150, 40)), (1.0, (150, 50, 10))]


def flash_frames():
    """A muzzle flash per aim: a hot core on the muzzle and a forward cone of three tongues, plus a
    small side puff; age 1 is smaller and dimmer. 4x then box-filtered, 5 light levels."""
    out = []
    n = FLASH * 4
    yy, xx = (np.mgrid[0:n, 0:n] + 0.5) / 4 - FLASH / 2
    for a in range(AIMS):
        ang = heading(a, AIMS)
        c, s = math.cos(ang), math.sin(ang)
        fwd, side = xx * c + yy * s, -xx * s + yy * c
        for age in range(2):
            k = (1.0, 0.55)[age]
            core = np.clip(1 - np.hypot(fwd, side) / (3.2 * k), 0, 1)
            lobe = np.zeros_like(core)
            for off, length, width in ((0.0, 14.0, 3.4), (0.55, 9.0, 2.2), (-0.55, 9.0, 2.2)):
                f2 = fwd * math.cos(off) + side * math.sin(off)
                s2 = -fwd * math.sin(off) + side * math.cos(off)
                t = np.clip(f2 / (length * k), 0, 1)
                w = width * k * (1 - t) + 0.3
                lobe = np.maximum(lobe, np.clip(1 - np.abs(s2) / w, 0, 1) * (f2 > -0.5) * (1 - t) ** 0.7)
            v = np.clip(np.maximum(core * 1.3, lobe), 0, 1) ** 0.6 * (1.0, 0.75)[age]
            col = np.array(raster.ramp(FLASH_RAMP, np.clip(1 - v, 0, 1)), float)
            rgb = col * v[..., None]
            img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").resize((FLASH, FLASH), Image.BOX)
            q = np.array(img).astype(np.float64)
            q = np.floor(q / 255 * 5 + 0.5) / 5 * 255
            out.append(artkit.additive(Image.fromarray(q.astype(np.uint8), "RGB").convert("RGBA")))
    return artkit.quantize_set(out, 24)


# =========================================================================== output

def claimed():
    if LIVERY == "asc":                    # the livery's frames once the tank's data names them
        return DATA.exists() and ROOT_NAME in DATA.read_text(encoding="utf-8")
    return DATA.exists()


@contextlib.contextmanager
def target():
    sprites, pivots = (artkit.SPRITES, artkit.PIVOTS) if claimed() else (HELD / "sprites", HELD / "pivots")
    sprites.mkdir(parents=True, exist_ok=True)
    pivots.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES, artkit.PIVOTS
    artkit.SPRITES, artkit.PIVOTS = sprites, pivots
    try:
        yield sprites
    finally:
        artkit.SPRITES, artkit.PIVOTS = before


def _owned(stem, name):
    return stem == name or (stem.rsplit("_", 1)[0] == name and stem.rsplit("_", 1)[-1].isdigit())


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, HELD / "sprites"):
        for old in folder.glob(f"{name}*.png"):
            if _owned(old.stem, name):
                old.unlink()
    with target() as where:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} x {frames[0].width}x{frames[0].height} -> {where.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, HELD / "sprites"):
        if (folder / f"{name}.png").exists():
            return [Image.open(folder / f"{name}.png").convert("RGBA")]
        files = [f for f in folder.glob(f"{name}_*.png") if _owned(f.stem, name)]
        if files:
            files.sort(key=lambda p: int(p.stem.rsplit("_", 1)[1]))
            return [Image.open(f).convert("RGBA") for f in files]
    return []


def pivots():
    return {
        "note": "the turret, its stub and the wreck are centred on the hull's centre (the tank's position); "
                "the muzzle flash is centred `muzzle` px along the turret's aim from it (aim k: k x 11.25 "
                "degrees clockwise from straight down), `muzzle_recoil` in the recoil frame",
        "scale": round(S, 3),
        "muzzle": round(MUZZLE * S, 1),
        "muzzle_recoil": round((MUZZLE - RECOIL) * S, 1),
    }


def jobs():
    js = [("hull", k, tr) for k in range(HEADINGS) for tr in range(TREADS)]
    js += [("turret", a, rc) for a in range(AIMS) for rc in range(2)]
    js += [("stub",)] + [("wreck", k) for k in range(HEADINGS)]
    return js


def build():
    js = jobs()
    with ProcessPoolExecutor() as pool:
        out = dict(zip(js, pool.map(render_job, [j + (LIVERY,) for j in js])))

    def of(kind):
        return [out[j] for j in js if j[0] == kind]
    r = ROOT_NAME
    write(r, artkit.quantize_set(of("hull"), 36 if LIVERY == "asc" else 32))
    write(f"{r}-turret", artkit.quantize_set(of("turret"), 28 if LIVERY == "asc" else 24))
    write(f"{r}-turret-stub", artkit.quantize_set(of("stub"), 16), single=True)
    if LIVERY != "asc":                     # the livery draws the grey tank's flash (the same colours)
        write("warden-tank-flash", flash_frames())
    write(f"{r}-wreck", artkit.quantize_set(of("wreck"), 32))
    with target():
        artkit.write_pivots(ROOT_NAME, SOURCE, pivots())
    if LIVERY == "asc":
        write_shadows()


def shadow(frame, dx=4, dy=5, k=0.5):
    """G.1 c's ground shadow (tank_readability_r67.py ``shadow``): the silhouette offset away from the top-left sun,
    blurred, k black, on a canvas 8 px larger, centred like the frame."""
    a = frame.getchannel("A").point(lambda p: 255 if p > 40 else 0)
    pad = Image.new("L", (frame.width + 8, frame.height + 8), 0)
    pad.paste(a, (4 + dx, 4 + dy))
    pad = pad.filter(ImageFilter.GaussianBlur(1.6)).point(lambda p: int(p * k))
    out = Image.new("RGBA", pad.size, (0, 0, 0, 0))
    out.putalpha(pad)
    return out


def write_shadows():
    """The livery's shadows from its written hull (tread frame 0 of each heading) and wreck frames."""
    hull, wreck = load(ROOT_NAME), load(f"{ROOT_NAME}-wreck")
    if len(hull) != HEADINGS * TREADS or len(wreck) != HEADINGS:
        sys.exit(f"{ROOT_NAME}: {len(hull)} hull and {len(wreck)} wreck frames, build them first")
    write(f"{ROOT_NAME}-shadow", [shadow(hull[k * TREADS]) for k in range(HEADINGS)])
    write(f"{ROOT_NAME}-wreck-shadow", [shadow(f) for f in wreck])


def intel_model(livery="grey"):
    """(scene, mats) for the hangar intel portrait: the hull driving down (heading 0) with the turret
    aimed down along it, at rest; the grey livery (``asc``: the Act 5 livery)."""
    if livery != LIVERY:
        set_livery(livery)
    hull, tur = hull_scene(0.0), turret_scene()
    rot = model_rotation(heading(0, HEADINGS))
    return (lambda p: union(hull(sdf.rotate_z(p, rot)), tur(sdf.rotate_z(p, rot)))), \
        model_space_materials(mats(), rot)


# =========================================================================== review

FW, FH, FPS = 480, 540, 20
BELT_X, BELT_W, BELT_SPEED = 240, 84, 40.0


def paste_c(img, sp, x, y):
    sprite.paste(img, sp, x - sp.width / 2, y - sp.height / 2)


def glow_c(img, g, x, y):
    return artkit.add_light(img, g, (int(round(x - g.width / 2)), int(round(y - g.height / 2))))


def ground():
    """Mars regolith (the concept's) with a foundry conveyor down the middle: rails and a plate belt."""
    bg = r36.mars_ground(1942, craters=5)
    d = ImageDraw.Draw(bg)
    x0, x1 = BELT_X - BELT_W // 2, BELT_X + BELT_W // 2
    d.rectangle([x0 - 10, 0, x1 + 10, FH], fill=(58, 52, 50, 255))
    for x in (x0 - 9, x1 + 3):
        d.rectangle([x, 0, x + 6, FH], fill=(92, 88, 86, 255))
        d.line([(x, 0), (x, FH)], fill=(132, 128, 124, 255))
    return bg


def belt(img, t):
    d = ImageDraw.Draw(img)
    x0, x1 = BELT_X - BELT_W // 2, BELT_X + BELT_W // 2
    off = (t * BELT_SPEED) % 18
    d.rectangle([x0, 0, x1, FH], fill=(44, 44, 47, 255))
    y = -18 + off
    while y < FH:
        d.line([(x0, int(y)), (x1, int(y))], fill=(26, 26, 28, 255), width=2)
        d.line([(x0, int(y) + 2), (x1, int(y) + 2)], fill=(70, 70, 74, 255))
        y += 18
    return img


def aim_of(angle):
    return int(round((angle - np.pi / 2) / (TAU / AIMS))) % AIMS


def turn_toward(cur, want, rate):
    da = (want - cur + math.pi) % TAU - math.pi
    return cur + max(-rate, min(rate, da))


def ship_at(t):
    """Holds low, slides right, then climbs past the convoy up the screen's right lane."""
    if t < 3.0:
        return 200 + 50 * math.sin(t * 1.2), 470
    if t < 7.0:
        u = (t - 3.0) / 4.0
        return 200 + 50 * math.sin(3.6) + (340 - 200 - 50 * math.sin(3.6)) * u, 470 - 360 * u
    return 340 - 20 * math.sin((t - 7.0) * 1.5), 110


L34_GROUND = ROOT / "assets" / "backdrop" / "level-34" / "helix-hulk-a.png"


def asc_ground():
    """The livery's review ground: Level 34's Helix hulk (its keel road under x 240) when its backdrop exists, a
    tall strip scrolled by the belt's 40 px/s; else charcoal plating."""
    if L34_GROUND.exists():
        hull = Image.open(L34_GROUND).convert("RGBA")
        import backdrop_l34 as l34  # noqa: E402  (the road's x on the hulk)
        road = l34.road_x("helix-hulk-a")
        strip = Image.new("RGBA", (FW, hull.height), (14, 13, 18, 255))
        strip.alpha_composite(hull, (int(BELT_X - road), 0))
        return strip
    strip = Image.new("RGBA", (FW, FH * 2), (60, 58, 62, 255))
    return strip


def ground_at(bg, t):
    if LIVERY != "asc":
        return belt(bg.copy(), t)
    off = int(t * BELT_SPEED)
    top = max(0, bg.height - FH - off)
    return bg.crop((0, top, FW, top + FH))


def review_gif(s):
    bg = ground() if LIVERY != "asc" else asc_ground()
    fps, T = FPS, 11.0
    tanks = [{"y0": -40 - 92 * i, "aim": np.pi / 2, "next": 0.8 + 0.7 * i, "shots": [], "turret": True,
              "dead": None} for i in range(3)]
    tanks[2]["turret_lost"] = 6.2
    tanks[1]["dies"] = 8.0
    bullets, booms, frames = [], [], []
    boom = artkit.load_frames("explosion-medium")
    orb = artkit.load_frames("orb-medium")
    for f in range(int(T * fps)):
        t = f / fps
        img = ground_at(bg, t)
        sx, sy = ship_at(t)
        for i, tk in enumerate(tanks):
            x, y = BELT_X, tk["y0"] + 80 * t           # 40 px/s drive + the 40 px/s belt
            if tk.get("dies") and t >= tk["dies"]:
                if tk["dead"] is None:
                    tk["dead"] = t
                    booms.append((x, y, t))
                paste_c(img, s["warden-tank-wreck"][0], x,                 # rides the belt on
                        tk["y0"] + 80 * tk["dead"] + BELT_SPEED * (t - tk["dead"]))
                continue
            tread = int(t * 80 / 9) % 2
            paste_c(img, s["warden-tank"][tread], x, y)
            if tk.get("turret_lost") and t >= tk["turret_lost"]:
                if tk["turret"]:
                    tk["turret"] = False
                    booms.append((x, y, t))
                paste_c(img, s["warden-tank-turret-stub"][0], x, y)
                continue
            want = math.atan2(sy - y, sx - x)
            # the burst cycle: 0.3 s still (the tell), 3 shots 0.15 s apart, every 2.5 s
            cyc = t - tk["next"]
            if not 0 <= cyc < 0.7:
                tk["aim"] = turn_toward(tk["aim"], want, math.radians(120) / fps)
            if cyc >= 0.8:
                tk["next"] += 2.5
                cyc -= 2.5
            a = aim_of(tk["aim"])
            shot = None
            for j in range(3):
                ts = 0.3 + 0.15 * j
                if 0 <= cyc - ts < 0.1 and y > -20:
                    shot = cyc - ts
                    if cyc - ts < 0.5 / fps:
                        ang = heading(a, AIMS)
                        bullets.append([x + MUZZLE * S * math.cos(ang), y + MUZZLE * S * math.sin(ang), ang, t])
            paste_c(img, s["warden-tank-turret"][a * 2 + (1 if shot is not None else 0)], x, y)
            if shot is not None:
                ang = heading(a, AIMS)
                mx, my = x + MUZZLE * S * math.cos(ang), y + MUZZLE * S * math.sin(ang)
                img = glow_c(img, s["warden-tank-flash"][a * 2 + (0 if shot < 0.05 else 1)], mx, my)
        for b in bullets:
            age = t - b[3]
            bx, by = b[0] + 160 * age * math.cos(b[2]), b[1] + 160 * age * math.sin(b[2])
            if -20 < bx < FW + 20 and -20 < by < FH + 20:
                paste_c(img, orb[f % len(orb)], bx, by)
        for bxx, byy, t0 in booms:
            k = int((t - t0) * 20)
            if 0 <= k < len(boom):
                img = glow_c(img, boom[k], bxx, byy)
        shipf = artkit.load_frames("ship")
        paste_c(img, shipf[len(shipf) // 2], sx, sy)
        raster.draw_text(img, 8, FH - 14, f"{ROOT_NAME.upper()} FINAL {REVIEW_ROUND.upper()} - {t:4.1f} S",
                         raster.LABEL)
        frames.append(img)
    return frames


def assembled(s, k, a, recoil=0, stub=False):
    img = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    img.alpha_composite(s["warden-tank"][k * TREADS])
    if stub:
        img.alpha_composite(s["warden-tank-turret-stub"][0], ((SIZE - STUB) // 2, (SIZE - STUB) // 2))
    else:
        img.alpha_composite(s["warden-tank-turret"][a * 2 + recoil])
    return img


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    s = {name: load(name if name == "warden-tank-flash" else name.replace("warden-tank", ROOT_NAME, 1))
         for name in SET_NAMES}
    gif = review_gif(s)
    hull, tur = s["warden-tank"], s["warden-tank-turret"]
    combos = [assembled(s, k, a, rc, st) for k, a, rc, st in
              ((0, 0, 0, False), (0, 16, 0, False), (4, 24, 1, False), (12, 6, 0, False), (8, 0, 1, False),
               (2, 2, 0, True))]
    rows = [
        ("HULL, 16 HEADINGS (0 = DOWN, CLOCKWISE), TREAD FRAME 0", hull[0::2], 1, False),
        ("HULL TREAD FRAMES 0/1, HEADINGS 0 2 4 6", [hull[k * 2 + tr] for k in (0, 2, 4, 6) for tr in (0, 1)], 2, False),
        ("TURRET, 32 AIMS AT REST (EVERY 2ND)", tur[0::4], 1, False),
        ("TURRET REST / RECOIL, AIMS 0 4 8 12 16", [tur[a * 2 + rc] for a in (0, 4, 8, 12, 16) for rc in (0, 1)], 1, False),
        ("ASSEMBLED: HULL + TURRET (AIM ANY WAY, RECOIL), LAST THE TURRET STUB (2X)", combos, 2, False),
        ("MUZZLE FLASH (ADDITIVE), AIMS 0 4 8 12 16 20 24 28, AGES 0/1", [s["warden-tank-flash"][a * 2 + g]
                                                                           for a in range(0, 32, 4) for g in (0, 1)], 2, True),
        ("TURRET STUB (THE TURRET SHOT OFF)", s["warden-tank-turret-stub"], 3, False),
        ("WRECK, 16 HEADINGS (BURNT, THE ENGINE DECK BLOWN, THE TURRET ASKEW, NO MARKINGS)", s["warden-tank-wreck"], 1, False),
        ("FRAMES FROM THE GIF: THE CONVOY ON ITS BELT, A BURST, FIRING BACK UP, THE STUB, THE WRECK",
         [gif[i].resize((240, 270), Image.LANCZOS) for i in (30, 54, 140, 175, 200)], 1, False),
    ]
    if LIVERY == "asc":
        rows[-1] = ("FRAMES FROM THE GIF: THE CONVOY ON A HELIX HULK'S KEEL (LEVEL 34), A BURST, FIRING BACK UP, "
                    "THE STUB, THE WRECK", rows[-1][1], 1, False)
        rows[-2] = ("WRECK, 16 HEADINGS (BURNT BLACK AND GOLD, THE ENGINE DECK BLOWN, THE TURRET ASKEW)",
                    rows[-2][1], 1, False)
        title = "WARDEN TANK IN ASCENDANCY LIVERY - FINAL SPRITES (A.15 B, Q.11 B GUNMETAL, L34)"
    else:
        title = "WARDEN TANK - FINAL SPRITES (ACT 3 UNMARKED GREY, L19)"
    artkit.REVIEW_ROUND = REVIEW_ROUND         # (importing backdrop_l34 for the livery's ground sets other rounds)
    sheet = artkit.review_sheet(title, rows, width=1400, batch=BATCH)
    artkit.save_review(sheet, gif, CONCEPT, ROOT_NAME, fps=FPS)


SET_NAMES = ["warden-tank", "warden-tank-turret", "warden-tank-turret-stub", "warden-tank-flash",
             "warden-tank-wreck"]


if __name__ == "__main__":
    if "--livery" in sys.argv[1:]:
        set_livery(sys.argv[sys.argv.index("--livery") + 1])
    if "--shadows" in sys.argv[1:]:
        write_shadows()
        sys.exit(0)
    if "--review" not in sys.argv[1:]:
        build()
    review()
