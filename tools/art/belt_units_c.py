#!/usr/bin/env python3
"""Production art: Level 30's units and their pieces (M9 part C batch, concept round 63, step ART-C): the Minelayer
and its line mine (design/enemies/space/minelayer, round 60 A.8 **a** "hopper barge"), the Gilded Gunship
(design/enemies/air/gilded-gunship, A.3 **b** "gold scale plate"), the Dart Drone (design/enemies/air/dart-drone,
A.4 **b** "chevron dart"), the Rail Serpent (design/enemies/air/rail-serpent, A.5 **b** "gold roofs and links"), the
belt's large rocks (Level 30's walls) and the lane's dead beacon buoy (Level 30's secret, drawn as a debris chunk).

The models are round 60's (tools/concept/enemies_r60.py ``minelayer_model``, ``mine_model``, ``gunship_model``,
``dart_model``, ``rail_model``; imported unchanged) in **Q.11 b**, the look the user picked in round 60: gunmetal and
charcoal hulls (tools/art/belt_units.py ``gunmetal``) with their gold, red running lights and the 1 px gold/red rim
(belt_units.py ``rim``). Gold is kept to about 20-30 % of the silhouette (round 62 flagged the Talon's 42-50 % as
heavy): the Gunship keeps b's gold scale front third but its pods are gunmetal; the Serpent keeps b's gold roof with
the black centre stripe but only on the roof's inner bands, its couplers gold (the "links": each car's front and rear
coupler stubs meet the next car's at the chain's spacing). The Minelayer and the Dart are as picked (about 20-25 %).
The mine speaks the homing diamond's "enemy ordnance" language (round 60 A.16 a): a crimson lamp with a gold ring and
a white-hot core when lit.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its
own render under the fixed key light (nothing lit is rotated afterwards). The Gunship is ``fixed`` (its README's
orientation: always faces down): its states are rendered facing down only.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: tools/art/l23kit.py's rule, an
enemy's root once ``design/enemies/<layer>/<slug>/data.yaml`` exists; ``debris-<chunk>`` once a level's ``debris``
names the chunk):
  minelayer_0..15              96x96, 16 headings: the hopper barge (88x64 on the canvas), engines lit, drum dark
  minelayer-laying_0..15       96x96: the same laying: the drum's hatch glowing red-gold
  minelayer-mine_0..7          24x24: the line mine (about 20 px across), lamp dim (unarmed, and the blink's off state); 8 turns of its
                               4-fold spikes over 90 degrees (frame k = k x 11.25 degrees clockwise)
  minelayer-mine-lit_0..7      24x24: the same with the lamp lit (armed: the once-a-second blink's on state, and the
                               0.6 s proximity blink's fast flashes)
  minelayer-mine-glow_0..2     24x24, additive: the lamp's red-gold halo over a lit frame (swell, full, fade)
  minelayer-mine-blast_0..7    96x96, additive: the proximity burst: a white-gold flash and the 40 px blast's ring
                               (its radius at the blast disc's edge in frame 3), sparks; vacuum: no smoke
  minelayer-mine-pop_0..5      40x40, additive: a chained mine's pop: a short flash and gold flecks (no ring)
  gilded-gunship_0..15         104x104, facing down: bays x 4 + lamps (bays 0 shut .. 3 open, the Darts' launch;
                               lamps 0 dark .. 3 lit, the ring's 0.4 s tell); frame 0 the idle hull
  gilded-gunship-engines_0..3  104x104, additive: the four engines' white-gold glow, flickering (drawn over the hull,
                               centred on it; the x2 weak point)
  gilded-gunship-plate-spark_0..3  24x24, additive: a shot glancing off the gold plate (grey-gold flash and sparks)
  gilded-gunship-bay-flash_0..3    20x20, additive: a bay's red-gold flash as a Dart leaves it
  gilded-gunship-halves_0..7   128x128: the death: the hull broken behind the plate, the halves drifting apart and
                               turning (over the medium vacuum burst)
  gilded-gunship-plates_0..7   24x24: a gold scale plate spinning off (8 turns)
  dart-drone_0..15             24x24, 16 headings: the chevron dart (20x20 on the canvas), its engine lit
  rail-serpent_0..31           64x64, 32 headings: the engine car, the white-gold reactor (the vital part), the prow
  rail-serpent-segment_0..31   48x48: a car, gold roof bands, gold coupler stubs front and rear
  rail-serpent-turret_0..31    48x48: a turret car (the domed twin gun), lamp dark
  rail-serpent-turret-tell_0..31  48x48: the same, the red lamp lit (the 0.2 s tell)
  rail-serpent-tail_0..31      48x48: the tail car, gold fins
  rail-serpent-<segment|turret|tail>-wreck_0..31  48x48: a cut-off car without power: lights dead, scorched (the
                               split's rear part drifting and slowing, then popping from the cut back)
  rail-serpent-sparks_0..7     32x32, additive: a dead car's sparking (a loop, 2 steps a frame): white-blue crackles
                               and gold flecks; vacuum: no smoke
  rail-serpent-snap_0..5       40x40, additive: the coupler snapping at the cut (a white flash, arcs, flecks)
  debris-belt-large-<a|b>_0..15  96x96 / 72x72: the belt's large rocks (Level 30's indestructible walls; about 88x80
                               and 66x60), 16 turns of the spin (frame k = k x 22.5 degrees clockwise)
  debris-lane-buoy_0..15       32x32: the dead beacon buoy (Level 30's secret, 20 HP like a small rock), dark, its
                               mast bent, tumbling (16 turns)
  debris-lane-buoy-break_0..7  56x56: the buoy shot open: its housing's panels flung out, the crate's light
  assets/pivots/minelayer.json, gilded-gunship.json, rail-serpent.json (written with their roots' frames; held with
  them)
  Review (round 63): design/enemies/space/concept/minelayer-final-r63-a.png/.gif, design/enemies/air/concept/
  gilded-gunship-final-r63-a, dart-drone-final-r63-a, rail-serpent-final-r63-a, design/campaign/act-5-the-belt/
  level-30-minefield/concept/belt-rocks-final-r63-a (the large rocks, the small ones of part B and the dead buoy)

Run: python3 tools/art/belt_units_c.py [layer] [gunship] [dart] [serpent] [rocks] [--review] [--check]
     (no subject = all, ~4 min; --review rebuilds only the review files from the written frames; --check lists
     every set with its folder, size and frame count and fails on a missing one or a missing Source chunk)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU

import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged: the round-60 models)
from render import raster  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED, hx  # noqa: E402
from render.enemy_rigs import local_to_screen  # noqa: E402
from render.sdf import (Material, mirror_x, rotate_y, rotate_z, sd_box, sd_capsule, sd_cylinder_y,  # noqa: E402
                        sd_cylinder_z, sd_sphere, union)

SCRIPT = "belt_units_c.py"
BATCH = "M9 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r63"
artkit.REVIEW_ROUND = ROUND
FPS = 20
A_X = e60.A_X
CONCEPT = {"layer": DESIGN / "enemies" / "space" / "concept", "gunship": DESIGN / "enemies" / "air" / "concept",
           "dart": DESIGN / "enemies" / "air" / "concept", "serpent": DESIGN / "enemies" / "air" / "concept",
           "rocks": DESIGN / "campaign" / "act-5-the-belt" / "level-30-minefield" / "concept"}
BACKDROP = ROOT / "assets" / "backdrop" / "level-30"
claimed, write, load, where = bu.claimed, bu.write, bu.load, bu.where
gunmetal, rim, turned, render, star = bu.gunmetal, bu.rim, bu.turned, bu.render, bu.star


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    """Pivots go with their sprite's folder: assets/pivots when the root is claimed, else build/art-held/pivots."""
    held = ROOT / "build" / "art-held" / "pivots"
    folder = artkit.PIVOTS if claimed(root) else held
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = held if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


def pivot_path(root):
    p = artkit.PIVOTS / f"{root}.json"
    return p if p.exists() else ROOT / "build" / "art-held" / "pivots" / f"{root}.json"


def screen_point(local, k, n, size, extent):
    """A model point (x right, y forward) in a frame's px at heading k of n."""
    dx, dy = local_to_screen(local[0], local[1], np.pi / 2 + TAU * k / n, size / extent)
    return [round(size / 2 + dx, 1), round(size / 2 + dy, 1)]


def scorch(img, seed, k=0.55):
    """A dead machine: darker, mottled by a soot pattern, a few ember pixels."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    a[..., :3] *= (k + 0.35 * n)[..., None]
    rng = np.random.default_rng(seed)
    ys, xs = np.nonzero(op)
    for i in rng.choice(len(xs), size=min(3, len(xs)), replace=False):
        a[ys[i], xs[i], :3] = (230, 130, 60)
    a[~op] = 0
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def dim(img, grey=0.45):
    """Power lost: the colours drained toward grey (the gold dulled)."""
    a = np.array(img).astype(np.float64)
    lum = a[..., :3] @ np.array((0.3, 0.55, 0.15))
    a[..., :3] = a[..., :3] * (1 - grey) + lum[..., None] * grey
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


# =========================================================================== models (round 60's, Q.11 b)

ML_SIZE, ML_EXT = 96, 2.0                    # the barge 88 x 64 px
MINE_SIZE, MINE_EXT = 24, 2.1                # the mine about 20 px across its spikes
GS_SIZE, GS_EXT = 104, 2.3                   # the Gunship about 89 x 86 px (the r04 hull at the concept's scale)
DART_SIZE, DART_EXT = 24, 2.0                # the dart about 20 x 20 px
RS_EXT = 1.7 * 48 / 48                       # model units across a 48 px car canvas (cars about 37 x 35 px)
RS_SIZE = {"engine": 64, "car": 48, "turret": 48, "tail": 48}
HALF_SIZE = 128
DRUM = (0.0, -0.28)                          # the Minelayer's mine drum (model units, +Y forward)
GS_DOME = (0.0, -0.08)                       # the ring's centre (the dome, its twelve lamps round it)
GS_PLATE = (0.0, 0.62)                       # the gold plate's centre (the front arc's sparks)
TURRET_MUZZLE = (0.0, 0.58)
LAMP_GOLD = np.array((1.0, 0.55, 0.25))


def layer_model(laying=0.0):
    scene, mats = e60.minelayer_model("a")
    mats = gunmetal(mats)
    glass = np.array(e60.CRIMSON) / 255.0 * 0.6 + e60.GOLD_F * 0.4
    mats[A_GLASS] = Material((0.12, 0.05, 0.05), metal=0.8, shininess=140, spec=1.2,
                             emission=tuple(glass * (0.1 + 1.9 * laying)))
    return scene, mats


def mine_model(lamp):
    """Round 60's mine a (a ball with four gold spikes) with its lamp enlarged so the blink reads at 1x."""
    scene0, mats = e60.mine_model("a", lamp=0.0)

    def scene(p):
        d, m = scene0(p)
        dl = sd_sphere(p, (0, 0, 0.5), 0.2)
        return np.minimum(d, dl), np.where(dl <= d, A_X, m)
    mats = gunmetal(mats)
    col = np.array(e60.CRIMSON) / 255.0 * 0.75 + e60.GOLD_F * 0.25
    mats[A_X] = Material((0.08, 0.02, 0.02), emission=tuple(col * (0.18 + 2.6 * lamp) + (0.9 * lamp) ** 2 * np.array((1.0, 0.85, 0.5))))
    return scene, mats


def gunship_model(lamps=0.0, bays=0.0):
    """Round 60's A.3 b (the r04 hull, a gold scale plate over the front third) with its pods in gunmetal (gold
    about 28 %, b's full gold pods about 42 %)."""
    scene0, mats = e60.gunship_model("b", lamps=lamps, bays=bays)
    plain, _ = e60.em.gilded_gunship(0.0, 1.0)

    def scene(p):
        d, m = scene0(p)
        _, m0 = plain(rotate_z(p, np.pi))
        q = mirror_x(p)
        pod_top = (m == A_GOLD) & (m0 == A_FACET) & (q[:, 0] > 0.64)
        m = np.where(pod_top, A_FACET, m)
        for a in np.linspace(0, TAU, 12, endpoint=False):       # the ring lamps a little larger: the tell reads
            dl = sd_sphere(p, (0.33 * np.cos(a), -0.08 + 0.33 * np.sin(a), 0.21), 0.05)
            m = np.where(dl <= d, A_X, m)
            d = np.minimum(d, dl)
        return d, m
    return scene, gunmetal(mats)


def dart_model():
    """Round 60's A.4 b with its gold leading edges a little narrower (gold about 25-30 % at 20 px with the rim)."""
    scene0, mats = e60.dart_model("b")

    def scene(p):
        d, m = scene0(p)
        q = mirror_x(p)
        narrow = (m == A_GOLD) & ~(q[:, 1] > 1.0 - 1.81 * q[:, 0] - 0.17)
        return d, np.where(narrow, A_HULL, m)
    return scene, gunmetal(mats)


def rail_model(kind, lamp=0.0, dead=False):
    """Round 60's A.5 b car in gunmetal: the gold roof only on its inner bands beside the black centre stripe, the
    couplers gold (stubs front and rear meeting the next car's: the gold links)."""
    concept_kind = "engine" if kind == "engine" else kind
    scene0, mats = e60.rail_model("b", concept_kind, lamp=lamp, glow=0.0 if dead else 1.0)

    def scene(p):
        d, m = scene0(p)
        q = mirror_x(p)
        band = 0.3 if kind == "turret" else 0.22             # the turret's dome covers part of its roof
        m = np.where((m == A_GOLD) & (q[:, 0] > band), A_HULL, m)
        items = [(d, m), (sd_capsule(p, (0, -0.58, 0.08), (0, -0.82, 0.08), 0.068, 0.068), A_GOLD)]   # couplers
        if kind != "engine":
            items.append((sd_capsule(p, (0, 0.6, 0.08), (0, 0.76, 0.08), 0.06, 0.05), A_GOLD))
        return union(*items)
    mats = gunmetal(mats)
    if dead:
        mats[A_RED] = Material((0.08, 0.05, 0.05))
        mats = mats[:A_X] + [Material((0.1, 0.09, 0.08)), Material((0.08, 0.05, 0.05))]
    return scene, mats


# =========================================================================== jobs (one render each)

def _layer(k, laying):
    return render(*turned(*layer_model(laying), k, 16), ML_SIZE, ML_EXT)


def _mine(k, lamp):
    return render(*turned(*mine_model(lamp), k, 32), MINE_SIZE, MINE_EXT, crisp=60)


def _gunship(lamps, bays):
    return render(*turned(*gunship_model(lamps, bays), 0, 16), GS_SIZE, GS_EXT)


def _gunship_glow():
    """The engines alone, lit (an emission mask) for the additive engine glow."""
    scene, mats = gunship_model()
    mats = bu.emission_only(mats)
    mats[A_ENGINE] = Material((0, 0, 0), emission=(1.0, 0.9, 0.62))
    return render(*turned(scene, mats, 0, 16), GS_SIZE, GS_EXT, crisp=0)


def _halves(i):
    """The hull broken behind the plate (model y 0.2): the front half drifting forward and turning one way, the rear
    back and the other way."""
    u = i / 7
    scene0, mats = gunship_model()
    cut = 0.22

    def scene(p):
        pf = rotate_z(p - np.array((0.1 * u, 0.55 * u, 0.0)), 0.9 * u)
        pr = rotate_z(p - np.array((-0.08 * u, -0.45 * u, 0.0)), -0.7 * u)
        df, mf = scene0(pf)
        dr, mr = scene0(pr)
        df = np.maximum(df, cut - pf[:, 1] + 0.02 * u)
        dr = np.maximum(dr, pr[:, 1] - cut)
        return np.minimum(df, dr), np.where(df < dr, mf, mr)
    return render(*turned(scene, mats, 0, 16), HALF_SIZE, GS_EXT * HALF_SIZE / GS_SIZE)


def _plate(i):
    mats = gunmetal(e60.belt_mats("e8e4f0"))
    mats[A_GOLD] = replace(mats[A_GOLD], pattern=e60.em._plates(4.0, 0.08))

    def scene(p):
        q = rotate_y(rotate_z(p, TAU * i / 8), 0.5 + 0.9 * np.sin(TAU * i / 8))
        return union((sd_box(q, (0, 0, 0), (0.42, 0.28, 0.04), 0.03), A_GOLD),
                     (sd_box(q, (0, -0.2, 0.02), (0.36, 0.05, 0.03), 0.01), A_FACET))
    return render(scene, mats, 24, 24 / 20.0)


def _dart(k):
    return render(*turned(*dart_model(), k, 16), DART_SIZE, DART_EXT, crisp=60)


def _rail(kind, k, lamp=0.0, dead=False):
    size = RS_SIZE[kind]
    return render(*turned(*rail_model(kind, lamp, dead), k, 32), size, RS_EXT * size / 48)


def _rock(name, k):
    size, radius, seed = ROCKS[name]
    scene, mats = bu.rock_model(radius, seed)
    return render(*turned(scene, mats, k, 16), size, size / bu.PX, crisp=70)


def buoy_model(lit=0.0):
    """A lane beacon buoy (+Y = its mast's top): a white-grey float drum with orange hazard bands, a short lattice mast
    and a strobe lamp; dead, its mast bent and the lamp dark."""
    mats = [Material(hx("b8bcc0"), metal=0.4, shininess=60, spec=0.6),
            Material(hx("4a4e54"), metal=0.5, shininess=40, spec=0.5),
            Material(hx("e07828"), shininess=30, spec=0.3),
            Material((0.1, 0.1, 0.1), emission=(1.0 * lit, 1.0 * lit, 1.0 * lit))]

    def scene(p):
        return union((sd_cylinder_z(p, (0, -0.2, 0.1), 0.42, 0.18), 0),
                     (sd_cylinder_z(p, (0, -0.2, 0.2), 0.43, 0.04), 2),
                     (sd_capsule(p, (0, -0.1, 0.25), (0.12, 0.62, 0.25), 0.07, 0.05), 1),
                     (sd_capsule(p, (0.12, 0.62, 0.25), (0.34, 0.78, 0.25), 0.05, 0.04), 1),
                     (sd_sphere(p, (0.36, 0.8, 0.28), 0.1), 3),
                     (sd_box(p, (-0.36, -0.2, 0.12), (0.08, 0.2, 0.12), 0.02), 1))
    return scene, mats


def _buoy(k):
    scene, mats = buoy_model()
    return render(*turned(scene, mats, k, 16), 32, 32 / 22.0, crisp=60)


JOBS = {"layer": _layer, "mine": _mine, "gunship": _gunship, "gsglow": _gunship_glow, "halves": _halves,
        "plate": _plate, "dart": _dart, "rail": _rail, "rock": _rock, "buoy": _buoy}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


# =========================================================================== additive effects

def field_img(fld, col, white=0.6, levels=6):
    """An additive frame from a 0..1 field: the colour, going white above ``white``."""
    f = artkit.smooth_steps(fld)
    rgb = np.asarray(col, float)[None, None] * f[..., None] + 255 * np.clip((f - white) / (1 - white), 0, 1)[
        ..., None] * 0.8
    h, w = f.shape
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(
        np.uint8), "RGBA"))


def grid(w):
    yy, xx = np.mgrid[0:w, 0:w].astype(float) + 0.5
    return xx - w / 2, yy - w / 2


def mine_glow(k):
    x, y = grid(24)
    r = np.hypot(x, y)
    f = (np.exp(-(r / (3.0 + 4 * k)) ** 2) * 0.9 + np.exp(-((r - 5.5 * k) / 1.4) ** 2) * 0.45) * k
    return field_img(f, (255, 60, 50), white=0.7)


def mine_blast(i, n=8):
    """The proximity burst: flash, the 40 px ring (frame 3 at r 40), the disc lit inside it, sparks."""
    w = 96
    x, y = grid(w)
    r = np.hypot(x, y)
    u = (i + 0.5) / n
    rr = 40 * min(1.0, (i + 1) / 4)
    flash = np.exp(-(r / (6 + 10 * u)) ** 2) * max(0.0, 1.2 - 1.6 * u)
    ring = np.exp(-((r - rr) / (2.2 + 2 * u)) ** 2) * (1.0 if i < 4 else max(0.0, 1 - (i - 3) / 4.5))
    disc = (r < rr) * 0.22 * max(0.0, 1 - u * 1.4)
    img = field_img(flash + 0.85 * ring + disc, (255, 120, 50), white=0.75)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(301)
    for _ in range(12):
        a = rng.uniform(0, TAU)
        r0, r1 = 6 + 34 * u, 10 + 38 * u
        if i < 6:
            d.line([w / 2 + np.cos(a) * r0, w / 2 + np.sin(a) * r0, w / 2 + np.cos(a) * r1, w / 2 + np.sin(a) * r1],
                   fill=(255, 210, 110, 255))
    return img


def mine_pop(i, n=6):
    w = 40
    x, y = grid(w)
    r = np.hypot(x, y)
    u = (i + 0.5) / n
    img = field_img(np.exp(-(r / (3 + 6 * u)) ** 2) * (1.15 - u), (255, 110, 50), white=0.7)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(311)
    for _ in range(7):
        a = rng.uniform(0, TAU)
        dist = (4 + 14 * u) * rng.uniform(0.8, 1.1)
        if i < 5:
            cx, cy = w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist
            d.rectangle([cx - 0.5, cy - 0.5, cx + 0.5, cy + 0.5], fill=(255, 200, 90, 255))
    return img


def plate_spark(i):
    x, y = grid(24)
    k = (1.0, 0.8, 0.45, 0.2)[i]
    f = np.exp(-(np.hypot(x, y) / (2 + i)) ** 2) * k
    img = field_img(f, (210, 190, 140), white=0.65)
    d = ImageDraw.Draw(img)
    for a in (-2.3, -1.6, -0.9, -0.4) if i < 3 else ():
        r0, r1 = 3 + 3 * i, 6 + 4 * i
        d.line([12 + np.cos(a) * r0, 12 + np.sin(a) * r0, 12 + np.cos(a) * r1, 12 + np.sin(a) * r1],
               fill=(255, 225, 150, 255))
    return img


def bay_flash(i):
    x, y = grid(20)
    k = (0.7, 1.0, 0.6, 0.25)[i]
    return field_img(np.exp(-(np.hypot(x, y * 0.8) / (2.5 + i)) ** 2) * k, (255, 110, 40), white=0.7)


def engine_glow(mask, f):
    """The engines' emission mask spread into a soft white-gold glow (4 flicker steps)."""
    a = np.array(mask).astype(np.float64)
    m = (a[..., :3].max(axis=-1) > 40).astype(float)
    k = (0.85, 1.0, 0.9, 0.75)[f]
    blur = np.array(Image.fromarray((m * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3.0 + 0.4 * f))) / 255.0
    return field_img(np.clip(blur * 2.2 * k, 0, 1), (255, 220, 150), white=0.75)


def rail_sparks(i):
    """A dead car's sparking: a few white-blue crackles and gold flecks, new each frame (seeded)."""
    w = 32
    img = Image.new("RGBA", (w, w), (0, 0, 0, 255))
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(400 + i)
    for _ in range(2):
        x, y = rng.uniform(8, 24, 2)
        pts = [(x, y)]
        for _ in range(3):
            x, y = x + rng.uniform(-5, 5), y + rng.uniform(-5, 5)
            pts.append((x, y))
        d.line(pts, fill=(170, 210, 255, 255))
        d.point(pts[0], fill=(255, 255, 255, 255))
    for _ in range(4):
        x, y = rng.uniform(4, 28, 2)
        d.point((x, y), fill=(255, 200, 100, 255))
    return artkit.additive(img)


def rail_snap(i, n=6):
    w = 40
    x, y = grid(w)
    u = (i + 0.5) / n
    img = field_img(np.exp(-(np.hypot(x, y) / (3 + 7 * u)) ** 2) * (1.2 - u), (180, 210, 255), white=0.6)
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(500 + i)
    if i < 4:
        for s in (-1, 1):
            px, py = w / 2, w / 2
            for _ in range(4):
                nx, ny = px + s * rng.uniform(2, 5), py + rng.uniform(-4, 4)
                d.line([px, py, nx, ny], fill=(200, 225, 255, 255))
                px, py = nx, ny
    for _ in range(6):
        a = rng.uniform(0, TAU)
        r = 4 + 14 * u
        d.point((w / 2 + np.cos(a) * r, w / 2 + np.sin(a) * r), fill=(255, 200, 90, 255))
    return img


# =========================================================================== builders

GOLD = {}


def note_gold(name, frames):
    shares = [e60.gold_share(f) for f in frames]
    GOLD[name] = (min(shares), max(shares))
    print(f"gold {name}: {100 * min(shares):.0f}-{100 * max(shares):.0f} %")


def build_layer():
    jobs = [("layer", k, lay) for lay in (0.0, 1.0) for k in range(16)]
    jobs += [("mine", k, lamp) for lamp in (0.0, 1.0) for k in range(8)]
    res = run(jobs)
    body = artkit.quantize_set([rim(f) for f in res[:32]], 40)
    note_gold("minelayer", body[:16])
    save_set("minelayer", body[:16])
    save_set("minelayer-laying", body[16:])
    mines = artkit.quantize_set([rim(f) for f in res[32:]], 20)
    save_set("minelayer-mine", mines[:8])
    save_set("minelayer-mine-lit", mines[8:])
    save_set("minelayer-mine-glow", [mine_glow(k) for k in (0.6, 1.0, 0.55)])
    save_set("minelayer-mine-blast", [mine_blast(i) for i in range(8)])
    save_set("minelayer-mine-pop", [mine_pop(i) for i in range(6)])
    write_pivots("minelayer", {"size": [ML_SIZE, ML_SIZE], "headings": 16,
                               "drum": [screen_point(DRUM, k, 16, ML_SIZE, ML_EXT) for k in range(16)],
                               "note": "drum: per heading the mine drum's centre (where a laid mine appears), [x, y] "
                                       "px from the frame's top left"})


def blobs(mask, level=40):
    """The centres of the lit blobs of an emission mask (the engines), [x, y] px, left to right."""
    a = np.array(mask).astype(np.float64)
    on = a[..., :3].max(axis=-1) > level
    seen = np.zeros_like(on)
    out = []
    for y0, x0 in zip(*np.nonzero(on)):
        if seen[y0, x0]:
            continue
        stack, pts = [(y0, x0)], []
        seen[y0, x0] = True
        while stack:
            y, x = stack.pop()
            pts.append((y, x))
            for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                yy, xx = y + dy, x + dx
                if 0 <= yy < on.shape[0] and 0 <= xx < on.shape[1] and on[yy, xx] and not seen[yy, xx]:
                    seen[yy, xx] = True
                    stack.append((yy, xx))
        if len(pts) >= 3:
            ys, xs = np.array(pts).T
            out.append([round(float(xs.mean()) + 0.5, 1), round(float(ys.mean()) + 0.5, 1)])
    return sorted(out)


def build_gunship():
    steps = (0.0, 0.33, 0.67, 1.0)
    jobs = [("gunship", lamp, bay) for bay in steps for lamp in steps] + [("gsglow",)]
    jobs += [("halves", i) for i in range(8)] + [("plate", i) for i in range(8)]
    res = run(jobs)
    body = artkit.quantize_set([rim(f) for f in res[:16]], 48)
    note_gold("gilded-gunship", body[:1])
    save_set("gilded-gunship", body)
    save_set("gilded-gunship-engines", [engine_glow(res[16], f) for f in range(4)])
    save_set("gilded-gunship-plate-spark", [plate_spark(i) for i in range(4)])
    save_set("gilded-gunship-bay-flash", [bay_flash(i) for i in range(4)])
    save_set("gilded-gunship-halves", artkit.quantize_set([rim(f) for f in res[17:25]], 40))
    save_set("gilded-gunship-plates", artkit.quantize_set(res[25:33], 16))
    bays = [screen_point((s * bx, by), 0, 16, GS_SIZE, GS_EXT) for bx, by in e60.GUN_BAYS for s in (1, -1)]
    write_pivots("gilded-gunship", {
        "size": [GS_SIZE, GS_SIZE], "headings": 1,
        "engines": blobs(res[16]),
        "bays": bays, "dome": screen_point(GS_DOME, 0, 16, GS_SIZE, GS_EXT),
        "plate": screen_point(GS_PLATE, 0, 16, GS_SIZE, GS_EXT),
        "frames": "bays * 4 + lamps (0..3 each)",
        "note": "facing down (fixed): engines the four white-gold engines (the x2 arc), bays the belly doors the "
                "Darts leave by (the bay flash's centres), dome the ring's centre, plate the gold plate's centre; "
                "[x, y] px from the frame's top left"})


def build_dart():
    res = run([("dart", k) for k in range(16)])
    frames = artkit.quantize_set([rim(f) for f in res], 24)
    note_gold("dart-drone", frames)
    save_set("dart-drone", frames)


RAIL_SETS = [("rail-serpent", "engine", 0.0, False), ("rail-serpent-segment", "car", 0.0, False),
             ("rail-serpent-turret", "turret", 0.0, False), ("rail-serpent-turret-tell", "turret", 1.0, False),
             ("rail-serpent-tail", "tail", 0.0, False), ("rail-serpent-segment-wreck", "car", 0.0, True),
             ("rail-serpent-turret-wreck", "turret", 0.0, True), ("rail-serpent-tail-wreck", "tail", 0.0, True)]


def build_serpent():
    res = run([("rail", kind, k, lamp, dead) for _, kind, lamp, dead in RAIL_SETS for k in range(32)])
    live = artkit.quantize_set([rim(f) for f in res[:5 * 32]], 40)
    for j, (name, *_rest) in enumerate(RAIL_SETS[:5]):
        frames = live[32 * j:32 * (j + 1)]
        if j < 3:
            note_gold(name, frames)
        save_set(name, frames)
    dead = artkit.quantize_set([dim(scorch(rim(f), 9000 + i, 0.45)) for i, f in enumerate(res[5 * 32:])], 28)
    for j, (name, *_rest) in enumerate(RAIL_SETS[5:]):
        save_set(name, dead[32 * j:32 * (j + 1)])
    save_set("rail-serpent-sparks", [rail_sparks(i) for i in range(8)])
    save_set("rail-serpent-snap", [rail_snap(i) for i in range(6)])
    write_pivots("rail-serpent", {
        "headings": 32, "sizes": RS_SIZE,
        "muzzle": [screen_point(TURRET_MUZZLE, k, 32, 48, RS_EXT) for k in range(32)],
        "note": "muzzle: per heading the turret car's twin-gun muzzle (the aimed shot's spawn), [x, y] px from the "
                "48 px frame's top left; cars about 37 x 35 px with gold coupler stubs reaching 21 px ahead and 23 px "
                "behind the centre (they meet at the chain's spacing of about 40 px)"})


ROCKS = {"large-a": (96, 2.15, 61), "large-b": (72, 1.58, 67)}      # canvas, radius (units at 20 px), seed


def buoy_break(frames, f, n=8):
    """The dead buoy shot open: its panels flung out, a short grey dust ring, the crate's amber light at the core."""
    img = bu.rock_break(frames, f, n)
    u = (f + 1) / n
    if f < 5:
        d = ImageDraw.Draw(img)
        c = 28
        k = int(255 * (1 - u))
        d.ellipse([c - 3, c - 3, c + 3, c + 3], fill=(255, 190, 90, 255) if k > 120 else (200, 140, 70, 255))
    return img


def build_rocks():
    res = run([("rock", name, k) for name in ROCKS for k in range(16)] + [("buoy", k) for k in range(16)])
    for i, name in enumerate(ROCKS):
        save_set(f"debris-belt-{name}", artkit.quantize_set(res[16 * i:16 * (i + 1)], 28))
    buoy = artkit.quantize_set([scorch(f, 7100 + i, 0.75) for i, f in enumerate(res[32:])], 24)
    save_set("debris-lane-buoy", buoy)
    save_set("debris-lane-buoy-break", artkit.quantize_set([buoy_break(buoy, f) for f in range(8)], 24))


BUILDERS = {"layer": build_layer, "gunship": build_gunship, "dart": build_dart, "serpent": build_serpent,
            "rocks": build_rocks}


# =========================================================================== review

def plate(w=480, h=540, seed=301):
    """The review ground: Level 30's production space when backdrop_l30.py has written it, else round 60's."""
    tile = BACKDROP / "lane-space.png"
    if tile.exists():
        src = Image.open(tile).convert("RGBA")
        return src.crop((0, 0, w, h)) if src.height >= h else src.resize((w, h))
    return e60.space_bg(seed, h).crop((0, 0, w, h)).convert("RGBA")


def paste(dst, img, x, y):
    return l23kit.paste_c(dst, img, x, y)


def add(dst, img, x, y, k=1.0):
    return l23kit.add_c(dst, img, x, y, k)


def ship_sprite():
    return artkit.load_frames("ship")[2]


def caption(img, text):
    e60.caption(img, text)
    return img


def gif_frames(draw, seconds, base):
    return [draw(base.copy(), i / FPS) for i in range(int(seconds * FPS))]


def save_review(kind, subject, sheet, gif):
    png, gif_path = artkit.review_paths(CONCEPT[kind], subject)
    sheet.convert("RGB").save(png, optimize=True)
    size = e60.rig.write_gif(gif, gif_path, fps=FPS, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif_path.name} ({size / 1e6:.1f} MB)")


def over_plate(frames, glows=()):
    """Frames side by side on the level's space; ``glows``: (additive frame, index of the frame it lies on)."""
    w = sum(f.width for f in frames) + 12 * len(frames) + 8
    h = max(f.height for f in frames) + 16
    img = plate(w, h)
    x, centres = 8, []
    for f in frames:
        img = paste(img, f, x + f.width / 2, h / 2)
        centres.append(x + f.width / 2)
        x += f.width + 12
    for g, i in glows:
        img = add(img, g, centres[i], h / 2)
    return img


def share(name):
    lo, hi = GOLD.get(name, (None, None))
    if lo is None:
        fr = load(name)
        vals = [e60.gold_share(f) for f in fr[:16]]
        lo, hi = min(vals), max(vals)
    return f"{100 * lo:.0f}" if round(lo * 100) == round(hi * 100) else f"{100 * lo:.0f}-{100 * hi:.0f}"


def burst_set(rung):
    """A vacuum kill burst (explosions.py: the additive burst and its normal-blended debris)."""
    return artkit.load_frames(f"explosion-vacuum-{rung}"), artkit.load_frames(f"explosion-vacuum-debris-{rung}")


def boom(f, sets, i, x, y):
    adds, debris = sets
    if i < len(debris):
        f = paste(f, debris[i], x, y)
    if i < len(adds):
        f = add(f, adds[i], x, y)
    return f


def orbs():
    return artkit.load_frames("orb")


def review_layer():
    lay, laying = load("minelayer"), load("minelayer-laying")
    mine, lit, glow = load("minelayer-mine"), load("minelayer-mine-lit"), load("minelayer-mine-glow")
    blast, pop = load("minelayer-mine-blast"), load("minelayer-mine-pop")
    expl = burst_set("medium")
    orb = orbs()
    bolt = star(12, 1.0, (120, 220, 255))
    sheet = artkit.review_sheet("MINELAYER AND ITS LINE MINE (FINAL R63): ROUND 60 A.8 A IN GUNMETAL (Q.11 B)", [
        (f"MINELAYER, 16 HEADINGS (GOLD {share('minelayer')} %)", lay, 1, False),
        ("LAYING: THE DRUM'S HATCH LIT (EVERY 4TH HEADING)", laying[::4], 1, False),
        ("MINE DIM (UNARMED / BLINK OFF), 8 TURNS | LIT", mine + lit, 3, False),
        ("LAMP GLOW (ADDITIVE) | CHAINED POP (6 FR)", glow + pop, 3, True),
        ("PROXIMITY BLAST: FLASH, THE 40 PX RING (8 FR)", blast, 1, True),
        ("ON LEVEL 30'S SPACE (1X): HEADINGS 0, 4, 12; MINES DIM, LIT, LIT + GLOW", [over_plate(
            [lay[0], lay[4], lay[12], mine[0], lit[0], lit[0]], [(glow[1], 5)])], 2, False)], batch=BATCH)
    xs = [12, 44, 76, 108, 220, 252, 284, 316, 372, 404, 436, 468]     # the gap of 112 between 108 and 220
    lay_t0, strafe = 2.2, 150.0
    scroll = 80.0
    row = 140.0
    shot_t, shot_i = 5.9, 9                       # a shot sets off the right cluster (mines 8-11)
    prox_t, prox_i = 7.2, 2                       # the ship drifts left: mine 2 blinks and bursts

    def lay_t(x):
        return lay_t0 + 0.5 + max(0.0, (x - 46) / strafe)

    def layer_xy(s):
        if s < lay_t0:
            return 46.0, max(row, 600 - 230 * max(0.0, s - 0.4)), 8
        u = s - lay_t0
        if u < 0.5:
            return 46.0 + 20 * u, row, int(round(8 + 4 * u / 0.5)) % 16     # up (8) turning to the right (12)
        return 56 + strafe * (u - 0.5), row, 12

    def ship_x(s):
        return 300 - 40 * np.sin(s * 0.6) if s < 6.4 else max(60.0, 300 - 40 * np.sin(6.4 * 0.6) - 150 * (s - 6.4))

    def draw(f, s):
        sx = ship_x(s)
        x, y, k = layer_xy(s)
        if s < 9.0:
            f = paste(f, (laying if lay_t(0) < s < lay_t(480) + 0.3 else lay)[k], x, y)
        for i, mx in enumerate(xs):
            t0 = lay_t(mx)
            if s < t0:
                continue
            my = row + scroll * (s - t0)
            ct = shot_t + 0.08 * abs(i - shot_i) if 8 <= i <= 11 else None
            pt = prox_t if i == prox_i else None
            if ct is not None and s >= ct:
                if s - ct < 0.3:
                    f = add(f, (blast if i == shot_i else pop)[min(5, int((s - ct) * 20))], mx, my)
                    if i == shot_i:
                        f = boom(f, expl, int((s - ct) * 20), mx, my)
                continue
            if pt is not None and s >= pt + 0.6:
                u = s - pt - 0.6
                if u < 0.4:
                    f = add(f, blast[min(7, int(u * 20))], mx, my)
                for j in range(8):
                    a = j * TAU / 8 + 0.2
                    f = add(f, orb[int(s * 10) % len(orb)], mx + np.cos(a) * (10 + 100 * u), my + np.sin(a) * (10 + 100 * u))
                continue
            spin = int(s * 4 + i) % 8
            armed = s - t0 > 1.0
            on = armed and ((s - t0) % 1.0 < 0.18)
            if pt is not None and pt <= s:
                on = int((s - pt) * 12) % 2 == 0
            f = paste(f, (lit if on else mine)[spin], mx, my)
            if on:
                f = add(f, glow[1], mx, my)
        if shot_t - 0.5 < s < shot_t:                    # the player's shot at the cluster
            my = row + scroll * (s - lay_t(xs[shot_i]))
            u = (s - (shot_t - 0.5)) / 0.5
            f = add(f, bolt, xs[shot_i], 470 + (my - 470) * u)
        f = paste(f, ship_sprite(), sx, 470)
        cap = ("REAR ENTRY UP THE LEFT LANE" if s < lay_t0 else "THE LINE: 3 CLUSTERS OF 4, ONE 112 PX GAP"
               if s < shot_t else "A SHOT MINE SETS OFF ITS CLUSTER" if s < prox_t else
               "TOO CLOSE: THE FAST BLINK, THE BLAST AND ITS RING")
        return caption(f, cap)
    save_review("layer", "minelayer", sheet, gif_frames(draw, 9.0, plate()))


def review_gunship():
    gs = load("gilded-gunship")
    eng, spark, bayf = load("gilded-gunship-engines"), load("gilded-gunship-plate-spark"), load(
        "gilded-gunship-bay-flash")
    halves, plates, dart = load("gilded-gunship-halves"), load("gilded-gunship-plates"), load("dart-drone")
    shield = load("enemy-shield-medium")
    expl = burst_set("medium")
    orb = orbs()
    piv = json.loads(pivot_path("gilded-gunship").read_text())
    sheet = artkit.review_sheet("GILDED GUNSHIP (FINAL R63): ROUND 60 A.3 B IN GUNMETAL (Q.11 B)", [
        (f"FACING DOWN (FIXED): LAMPS 0-3 (THE RING'S TELL), BAYS SHUT (GOLD {share('gilded-gunship')} %)",
         gs[:4], 1, False),
        ("BAYS OPENING 0-3 (THE DART STREAM), LAMPS DARK", gs[0::4], 1, False),
        ("ENGINE GLOW (ADDITIVE, 4 FR) | PLATE SPARK | BAY FLASH", eng[:1] + spark + bayf, 1, True),
        ("DEATH: THE HALVES (8 FR) | A GOLD PLATE SPINNING (8 FR)", halves[::2] + plates, 1, False),
        ("ON LEVEL 30'S SPACE (1X): IDLE, TELL, BAYS OPEN, THE ENGINE GLOW; THE SHIELD OVER THE FIRST",
         [over_plate([gs[0], gs[3], gs[12]], [(eng[1], 0), (eng[1], 1), (eng[1], 2), (shield[0], 0)])], 1, False)],
        batch=BATCH)
    rings = [2.0, 5.0, 8.0]
    streams = [3.2]
    kill = 8.6

    def gpos(s):
        return 240 + 20 * np.sin(s * 0.9), -30 + 45 * s

    def draw(f, s):
        x, y = gpos(min(s, kill))
        if s < kill:
            lamp = max([int(np.clip((s - (r - 0.4)) / 0.4 * 4, 0, 3)) if r - 0.4 <= s < r else 0 for r in rings])
            bay = max([3 if st - 0.1 <= s < st + 0.9 else 0 for st in streams])
            f = paste(f, gs[bay * 4 + lamp], x, y)
            f = add(f, eng[int(s * 12) % 4], x, y)
            if s < 6.0:
                f = add(f, shield[int(s * 10) % len(shield)], x, y, 0.7)
            if 6.0 < s < 7.6 and int(s * 10) % 3 == 0:
                px_, py_ = piv["plate"]
                f = add(f, spark[int(s * 20) % 4], x - 52 + px_, y - 52 + py_)
            for st in streams:
                if st <= s < st + 0.75:
                    for j, (bx, by) in enumerate(piv["bays"]):
                        if int((s - st) / 0.25) == j % 3:
                            f = add(f, bayf[int((s - st) * 16) % 4], x - 52 + bx, y - 52 + by)
        else:
            u = s - kill
            if u < 0.7:
                f = boom(f, expl, int(u * 20), x, y)
            f = paste(f, halves[min(7, int(u * 10))], x, y + 10 * u)
            for j in range(3):
                a = -1.2 + j * 1.2
                f = paste(f, plates[int(u * 16 + j) % 8], x + np.cos(a) * 70 * u, y + 20 + np.sin(a) * 50 * u)
        for r in rings:
            if r <= s < r + 3 and r < kill:
                cx, cy = gpos(r)
                for j in range(12):
                    a = j * TAU * 11 / 12 / 11 + np.pi / 2
                    u = s - r
                    f = add(f, orb[int(s * 10) % len(orb)], cx + np.cos(a) * (12 + 110 * u), cy + np.sin(a) * (12 + 110 * u))
        for st in streams:
            for j in range(3):
                t0 = st + 0.25 * j
                if t0 <= s < t0 + 3.0 and t0 < kill:
                    cx, cy = gpos(t0)
                    a = np.pi / 2 + (j - 1) * 0.5
                    u = s - t0
                    if u > 0.4:
                        a += 0.6 * (j - 1) * -1 * min(1.0, (u - 0.4))
                    k = l23kit.heading_index(np.cos(a), np.sin(a))
                    f = paste(f, dart[k], cx + np.cos(a) * 170 * u, cy + 20 + np.sin(a) * 170 * u)
        f = paste(f, ship_sprite(), 240 + 60 * np.sin(s * 0.5), 470)
        cap = ("THE GOLD LAMPS TELL THE RING" if s < 3.2 else "BAYS OPEN: A STREAM OF DARTS" if s < 6.0
               else "FRONT SHOTS GLANCE OFF THE PLATE" if s < kill else "BROKEN BEHIND THE PLATE")
        return caption(f, cap)
    save_review("gunship", "gilded-gunship", sheet, gif_frames(draw, 10.0, plate()))


def review_dart():
    dart = load("dart-drone")
    talon = load("talon")
    expl = burst_set("tiny")
    sheet = artkit.review_sheet("DART DRONE (FINAL R63): ROUND 60 A.4 B IN GUNMETAL (Q.11 B)", [
        (f"DART, 16 HEADINGS (GOLD {share('dart-drone')} %)", dart, 3, False),
        ("ON LEVEL 30'S SPACE (1X): THE LANCER, A DART, A TALON", [over_plate([ship_sprite(), dart[0], talon[0]])],
         3, False)], batch=BATCH)
    launches = [(0.4 + 0.25 * j + 2.2 * w, 120 + 240 * w, -0.5 + 0.5 * j) for w in range(2) for j in range(3)]

    def ship_x(s):
        return 240 + 120 * np.sin(s * 0.8)

    paths = []
    for t0, x0, spread in launches:
        pos, h, path = np.array((x0, 60.0)), np.pi / 2 + spread, []
        for i in range(int(5.0 * FPS)):
            age = i / FPS
            if 0.4 <= age < 3.4:
                want = np.arctan2(470 - pos[1], ship_x(t0 + age) - pos[0])
                dh = (want - h + np.pi) % TAU - np.pi
                h += float(np.clip(dh, -np.radians(60) / FPS, np.radians(60) / FPS))
            pos = pos + np.array((np.cos(h), np.sin(h))) * 170 / FPS
            path.append((pos.copy(), h))
        paths.append((t0, path))
    kills = {1: 1.9, 4: 4.0}

    def draw(f, s):
        for n, (t0, path) in enumerate(paths):
            i = int((s - t0) * FPS)
            if i < 0 or i >= len(path):
                continue
            kt = kills.get(n)
            pos, h = path[min(i, len(path) - 1)]
            if kt is not None and s >= kt:
                pos, h = path[int((kt - t0) * FPS)]
                if s - kt < 0.6:
                    f = boom(f, expl, int((s - kt) * 20), *pos)
                continue
            f = paste(f, dart[l23kit.heading_index(np.cos(h), np.sin(h))], *pos)
        f = paste(f, ship_sprite(), ship_x(s), 470)
        return caption(f, "OUT OF THE BAY STRAIGHT, A LAZY CHASE, THEN STRAIGHT ON")
    save_review("dart", "dart-drone", sheet, gif_frames(draw, 6.5, plate()))


def review_serpent():
    sets = {name: load(name) for name, *_ in RAIL_SETS}
    sparks, snap = load("rail-serpent-sparks"), load("rail-serpent-snap")
    piv = json.loads(pivot_path("rail-serpent").read_text())
    expl_m, expl_s = burst_set("medium"), burst_set("small")
    orb = orbs()
    sheet = artkit.review_sheet("RAIL SERPENT (FINAL R63): ROUND 60 A.5 B IN GUNMETAL (Q.11 B)", [
        (f"ENGINE, EVERY 2ND OF 32 HEADINGS (GOLD {share('rail-serpent')} %)", sets["rail-serpent"][::2], 1, False),
        (f"CAR (GOLD {share('rail-serpent-segment')} %)", sets["rail-serpent-segment"][::2], 1, False),
        (f"TURRET CAR (GOLD {share('rail-serpent-turret')} %)", sets["rail-serpent-turret"][::2], 1, False),
        ("TURRET CAR, THE TELL | TAIL", sets["rail-serpent-turret-tell"][::4] + sets["rail-serpent-tail"][::4], 1,
         False),
        ("WRECKS (POWER LOST): CAR, TURRET CAR, TAIL", sets["rail-serpent-segment-wreck"][::4]
         + sets["rail-serpent-turret-wreck"][::8] + sets["rail-serpent-tail-wreck"][::8], 1, False),
        ("SPARKS (LOOP, 8 FR) | COUPLER SNAP (6 FR)", sparks + snap, 2, True)], batch=BATCH)
    kinds = ["rail-serpent"] + ["rail-serpent-turret" if i % 3 == 2 else "rail-serpent-segment" for i in range(9)] + [
        "rail-serpent-tail"]
    path = e60.rig.catmull_rom([(-120, 140), (-40, 140), (120, 150), (300, 210), (400, 320), (330, 420), (190, 400),
                                (150, 290), (260, 190), (430, 160), (600, 150), (780, 150)],
                               [0, 0.5, 1.4, 2.5, 3.4, 4.2, 5.0, 5.8, 6.6, 7.6, 8.6, 9.6])
    gap = [44] + [40] * 10
    cut_i, cut_t = 6, 5.0
    fire = {i: [1.4 + 0.6 * (i // 3) + 2.4 * m for m in range(4)] for i in range(len(kinds)) if i % 3 == 2}

    def chain(t):
        pts, heads = e60.rig.chain_at(path, t, gap[:len(kinds) - 1])
        pts, heads = list(pts), list(heads)
        while len(pts) < len(kinds):
            pts.append(pts[-1] - np.array((gap[len(pts) - 1], 0.0)))
            heads.append(0.0)
        return pts, heads

    pts_c, heads_c = chain(cut_t)

    def draw(f, s):
        pts, heads = chain(s)
        for i in range(len(kinds) - 1, -1, -1):
            name = kinds[i]
            if s >= cut_t and i >= cut_i:
                u = s - cut_t
                x0, y0 = pts_c[i]
                h = heads_c[i]
                if i == cut_i:
                    if u < 0.7:
                        f = boom(f, expl_s, int(u * 20), x0, y0)
                    continue
                travel = 170 * 0.4 * (u - u * u / 3.0) if u < 1.5 else 170 * 0.4 * 0.75
                x, y = x0 + np.cos(h) * travel, y0 + np.sin(h) * travel
                pop = 1.5 + 0.1 * (i - cut_i - 1)
                if u >= pop:
                    if u - pop < 0.7:
                        f = boom(f, expl_m, int((u - pop) * 20), x, y)
                    continue
                k = l23kit.heading_index(np.cos(h), np.sin(h), 32)
                f = paste(f, sets[name + "-wreck" if name != "rail-serpent" else name][k], x, y)
                f = add(f, sparks[int(s * 10 + i) % 8], x, y)
                if i == cut_i + 1 and u < 0.3:
                    f = add(f, snap[min(5, int(u * 20))], x - np.cos(h) * -20, y - np.sin(h) * -20)
                continue
            x, y = pts[i]
            if not (-40 < x < 520 and -40 < y < 580):
                continue
            k = l23kit.heading_index(np.cos(heads[i]), np.sin(heads[i]), 32)
            tell = name == "rail-serpent-turret" and any(t0 - 0.2 <= s < t0 for t0 in fire.get(i, ()))
            f = paste(f, sets[name + "-tell" if tell else name][k], x, y)
        for i, ts in fire.items():
            for t0 in ts:
                if t0 <= s < t0 + 2.5 and not (i >= cut_i and t0 >= cut_t):
                    pts0, heads0 = chain(t0)
                    k = l23kit.heading_index(np.cos(heads0[i]), np.sin(heads0[i]), 32)
                    mx, my = piv["muzzle"][k]
                    x0, y0 = pts0[i][0] - 24 + mx, pts0[i][1] - 24 + my
                    d = np.array((240 - x0, 470 - y0))
                    d /= np.linalg.norm(d)
                    u = s - t0
                    f = add(f, orb[int(s * 10) % len(orb)], x0 + d[0] * 160 * u, y0 + d[1] * 160 * u)
        f = paste(f, ship_sprite(), 240, 470)
        cap = "THE TRAIN: TURRET CARS TELL RED AND FIRE" if s < cut_t else "CUT: THE REAR CARS DRIFT, SPARK AND POP"
        return caption(f, cap)
    save_review("serpent", "rail-serpent", sheet, gif_frames(draw, 9.5, plate()))


def review_rocks():
    large = {n: load(f"debris-belt-{n}") for n in ROCKS}
    small = {n: load(f"debris-belt-{n}") for n in ("a", "b", "c")}
    buoy, brk = load("debris-lane-buoy"), load("debris-lane-buoy-break")
    sheet = artkit.review_sheet("THE BELT'S LARGE ROCKS AND THE DEAD BUOY (FINAL R63), LEVEL 30", [
        ("LARGE ROCK A (INDESTRUCTIBLE WALL), EVERY 2ND OF 16 TURNS", large["large-a"][::2], 1, False),
        ("LARGE ROCK B, EVERY 2ND TURN", large["large-b"][::2], 1, False),
        ("THE DEAD BEACON BUOY (SECRET), 16 TURNS", buoy, 2, False),
        ("THE BUOY SHOT OPEN (8 FR)", brk, 2, False),
        ("ON LEVEL 30'S SPACE (1X): LARGE A, LARGE B, PART B'S SMALL A-C, THE BUOY", [over_plate(
            [large["large-a"][0], large["large-b"][0], small["a"][0], small["b"][0], small["c"][0], buoy[0]])], 1,
         False)], batch=BATCH)
    items = [("large-a", 120, -60, 8, 85, 0.25), ("large-b", 380, -200, -10, 85, -0.3), ("a", 260, -20, 20, 120, 0.9),
             ("c", 60, -260, 30, 120, -1.1)]
    buoy_shot = 4.2

    def draw(f, s):
        for name, x0, y0, vx, vy, spin in items:
            fr = large[name] if name in large else small[name]
            k = int(round(spin * s / (TAU / 16))) % 16
            f = paste(f, fr[k], x0 + vx * s, y0 + vy * s)
        bx, by = 70 + 6 * min(s, buoy_shot), -30 + 95 * min(s, buoy_shot)
        if s < buoy_shot:
            f = paste(f, buoy[int(s * 2) % 16], bx, by)
        elif s - buoy_shot < 0.8:
            f = paste(f, brk[min(7, int((s - buoy_shot) * 10))], bx, by)
        f = paste(f, ship_sprite(), 240 + 80 * np.sin(s * 0.7), 470)
        return caption(f, "LARGE ROCKS ARE WALLS; THE DEAD BUOY BREAKS OPEN")
    save_review("rocks", "belt-rocks", sheet, gif_frames(draw, 7.0, plate()))


REVIEWS = {"layer": review_layer, "gunship": review_gunship, "dart": review_dart, "serpent": review_serpent,
           "rocks": review_rocks}


# =========================================================================== check

SETS = {"layer": [("minelayer", 16), ("minelayer-laying", 16), ("minelayer-mine", 8), ("minelayer-mine-lit", 8),
                  ("minelayer-mine-glow", 3), ("minelayer-mine-blast", 8), ("minelayer-mine-pop", 6)],
        "gunship": [("gilded-gunship", 16), ("gilded-gunship-engines", 4), ("gilded-gunship-plate-spark", 4),
                    ("gilded-gunship-bay-flash", 4), ("gilded-gunship-halves", 8), ("gilded-gunship-plates", 8)],
        "dart": [("dart-drone", 16)],
        "serpent": [(name, 32) for name, *_ in RAIL_SETS] + [("rail-serpent-sparks", 8), ("rail-serpent-snap", 6)],
        "rocks": [(f"debris-belt-{n}", 16) for n in ROCKS] + [("debris-lane-buoy", 16),
                                                               ("debris-lane-buoy-break", 8)]}


def check():
    bad = 0
    for subject, sets in SETS.items():
        for name, n in sets:
            try:
                frames = load(name)
                folder = where(name)
                paths = [p for d in (l23kit.ASSETS_SPRITES, l23kit.HELD) for p in d.glob(f"{name}*.png")
                         if p.stem == name or (p.stem.rsplit("_", 1)[0] == name and p.stem.rsplit("_", 1)[1].isdigit())]
                ok = len(frames) == n and all(Image.open(p).text.get(artkit.SOURCE_KEY) == SOURCE for p in paths)
            except FileNotFoundError:
                frames, folder, ok = [], "-", False
            size = frames[0].size if frames else None
            print(f"{'ok ' if ok else 'BAD'} {name}: {len(frames)}/{n} {size} in {folder}")
            bad += not ok
    sys.exit(1 if bad else 0)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        check()
    subjects = [a for a in args if not a.startswith("--")] or list(BUILDERS)
    unknown = [s for s in subjects if s not in BUILDERS]
    if unknown:
        sys.exit(f"unknown: {unknown}; subjects: {', '.join(BUILDERS)}")
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    for s in subjects:
        REVIEWS[s]()
