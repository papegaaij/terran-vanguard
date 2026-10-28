#!/usr/bin/env python3
"""Production art: Level 29's units and their pieces (M9 part B batch, concept round 62, step ART-B): the Talon
(design/enemies/air/talon, round 60 A.1 **b** "gold panels"), the SAM Nest (design/enemies/ground/sam-nest, A.11
**a** "twin tubes on a round base"), the Asteroid Mite (design/enemies/space/asteroid-mite, A.7 **a** "tick") and
the belt's small rocks it rides, the wavering Ceres Wing Stormhawk (Level 29, A.22 **a** "Ceres Wing ochre"), the
clamp gun with its docking clamps (Level 29's Dock 9) and Rook's Ember launching from the prison tender's hold.

The models are round 60's (tools/concept/enemies_r60.py ``talon_model``, ``sam_base``, ``sam_rack``,
``mite_model``; tools/concept/belt_r60.py ``stormhawk_model``; imported unchanged) in **Q.11 b**, the look the user
picked in round 60: the Ascendancy's hulls in gunmetal and charcoal (belt_r60.py ``look_make('b')``: hull 5a606c,
facets 464b55) with their gold, red running lights and a 1 px gold rim on the edges facing the top-left sun, red
on the far edges (belt_r60.py's Q.11 b frame, ``belt_rim(False)`` without its own palette step: one palette per
frame set here). The Vrell mite keeps its plum chitin and teal glow; the wavering craft is a CDF airframe. The
clamp gun and the clamps are new (no concept: Level 29's README, *Ground targets*), in the same Q.11 b look.

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down, every heading its
own render under the fixed key light (nothing lit is rotated afterwards).

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: tools/art/l23kit.py's rule, an
enemy's root once ``design/enemies/<layer>/<slug>/data.yaml`` exists; ``debris-<chunk>`` once a level's ``debris``
names the chunk; ``rook-*`` always):
  talon_0..15                48x48, 16 headings: the gunmetal Talon, outer wing panels and tail fins gold (36 colours)
  talon-leader_0..15         48x48: the squad leader, a broad gold stripe down the spine, brighter red lights
  talon-glint_0..2           12x12, additive: the 0.25 s red glint on each twin muzzle (swell, full, fade; on
                             assets/pivots/talon.json ``muzzles[k]``: two [x, y] per heading, px from the top left)
  talon-wing_0..7            24x24: the spinning wing of the death (8 turns of 45 degrees clockwise), over the small
                             explosion with gold sparks
  sam-nest-base              48x48: the fixed round base, gold ring, its four lights red (awake)
  sam-nest-base-dormant      48x48: the same with its lights white (a friendly station's turret until the turn)
  sam-nest_0..15             48x48, 16 headings: the twin-tube rack on its yoke, hatch shut, drawn over the base
  sam-nest-open_0..15        48x48: the tell, the gold hatch raised, the tubes' mouths lit and the red lock light on
  sam-nest-dormant           48x48: the rack folded low facing down, lights white (dormant: drawn with the base's
                             dormant frame)
  sam-nest-wake_0..3         48x48: the 1 s wake tell, the rack lifting and its light going white -> red (facing down;
                             then the turning set takes over)
  sam-nest-wreck             48x48: the scorched base with the rack's stump (the wreck that stays)
  asteroid-mite_0..95        24x24, 16 headings x 6 poses (heading * 6 + pose): 0-3 the crawl loop (8 fps), 4 the
                             crouch (the 0.4 s tell, the back glow flared teal), 5 the leap
  debris-belt-<a|b|c>_0..15  44x44 / 40x40 / 36x36: the belt's small rocks (warm grey-brown, cratered, lit hard from
                             the top left), 16 turns of the chunk's spin (frame k = k x 22.5 degrees clockwise)
  debris-belt-<a|b|c>-break_0..7  56x56: the rock shot apart in vacuum: chips flung out in a ring, a short grey dust
                             ring in 3 translucency steps, no smoke (2 steps a frame)
  wavering-stormhawk_0..79   40x40, 16 headings x 5 banks (heading * 5 + bank; -30, -15, 0, +15, +30 degrees of roll)
                             through the Stormhawk's camera: the CDF airframe grey with sand-ochre panels, the
                             Ascendancy crest over the roundels, the IFF light dark
  wavering-stormhawk-iff_0..79  the same with the white IFF light lit (the 1 Hz blink alternates the two sets)
  wavering-stormhawk-hit_0..79  the same with the IFF light red (the warning flash on a hit)
  clamp-gun_0..15            40x40, 16 headings: the gunmetal turret, gold collar, one barrel, red lamp
  clamp-gun-glint_0..2       16x16, additive: the 0.3 s red muzzle glint (on pivots/clamp-gun.json ``muzzle[k]``)
  clamp-gun-clamp-<left|right>  96x56: the docking clamp under the gun (the gun's centre on the sprite's centre),
                             charcoal and gold, its jaw gripping the tender's flank (the left clamps' jaw points
                             right, toward the tender; the right clamps' left; each rendered under the fixed light)
  clamp-gun-clamp-<left|right>-open_0..5  96x56: the gun destroyed, the clamp springs open (the jaw's fingers swing
                             apart, the arm draws back 10 px), 3 steps a frame; the last frame stays
  clamp-gun-gas_0..5         48x48, additive: the burst of gas and sparks at the jaw as it opens (vacuum: a white jet
                             that thins away, no smoke), centred on the jaw (pivots/clamp-gun.json ``jaw``)
  rook-launch_0..7           42x42: Rook's Ember rising from the tender's open hold (seen growing from 0.55 to the
                             full scale, from the hold's shadow into the sun), 4 steps a frame; then rook_2
  assets/pivots/talon.json, assets/pivots/clamp-gun.json (written with their roots' frames; held with them)
  Review (round 62): design/enemies/air/concept/talon-final-r62-a.png/.gif, design/enemies/ground/concept/
  sam-nest-final-r62-a, design/enemies/space/concept/asteroid-mite-final-r62-a (with the rocks),
  design/campaign/act-5-the-belt/concept/wavering-stormhawk-final-r62-a, design/campaign/act-5-the-belt/
  level-29-betrayal-at-ceres/concept/clamp-gun-final-r62-a (the clamps, the gas and the Ember's launch)

Run: python3 tools/art/belt_units.py [talon] [sam] [mite] [rocks] [hawk] [clamp] [launch] [--review] [--check]
     (no subject = all, ~3 min; --review rebuilds only the review files from the written frames; --check lists
     every set with its folder, size and frame count and fails on a missing one or a missing Source chunk)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw

import artkit
import l23kit
from artkit import DESIGN, ROOT, TAU, sprite

import belt_r60 as b60  # noqa: E402  (concept scripts, imported unchanged: the round-60 models)
import enemies_r60 as e60  # noqa: E402
import ships_r02  # noqa: E402
from render import models, raster  # noqa: E402
from render.enemy_models import A_FACET, A_GOLD, A_GUN, A_HULL, A_RED, hx  # noqa: E402
from render.enemy_rigs import local_to_screen, model_rotation, model_space_materials  # noqa: E402
from render.sdf import (Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, sd_sphere,  # noqa: E402
                        subtract, union)

SCRIPT = "belt_units.py"
BATCH = "M9 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r62"
artkit.REVIEW_ROUND = ROUND
FPS = 20
PX = 20.0                                   # px per model unit (the round-60 sheets' play scale)
GOLD_RIM = np.array((255, 206, 84), float)
RED_RIM = np.array((230, 50, 40), float)
TEAL = np.array((0, 255, 154), float)
CONCEPT = {"talon": DESIGN / "enemies" / "air" / "concept", "sam": DESIGN / "enemies" / "ground" / "concept",
           "mite": DESIGN / "enemies" / "space" / "concept",
           "hawk": DESIGN / "campaign" / "act-5-the-belt" / "concept",
           "clamp": DESIGN / "campaign" / "act-5-the-belt" / "level-29-betrayal-at-ceres" / "concept"}
BACKDROP = ROOT / "assets" / "backdrop" / "level-29"

# --------------------------------------------------------------------------- held sprites (+ the debris chunks)

_plain_claimed = l23kit.claimed


def _debris_chunks():
    chunks = set()
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        deb = (l23kit._yaml(f) or {}).get("debris") or {}
        if isinstance(deb, dict):
            chunks |= set((deb.get("chunks") or {}).keys())
    return chunks


# A level's tows drawn in another look than the lifeboat, by level number (vanguard.pipeline.SpriteUse.TOW_LOOKS, the
# game's TowLooks.LOOKS): Level 30's dead beacon buoy (M9 part C, step C4); Level 31's claim rock (M9 part D, step D2).
TOW_LOOKS = {30: "debris-lane-buoy", 31: "debris-claim-rock"}


def _tow_looks():
    looks = set()
    for number, look in TOW_LOOKS.items():
        for f in (DESIGN / "campaign").glob(f"*/level-{number:02d}-*/data.yaml"):
            if (l23kit._yaml(f) or {}).get("tows"):
                looks.add(look)
    return looks


def claimed(name):
    """l23kit's rule, plus a debris chunk's sprites (``debris-<chunk>``) once a level's ``debris`` names the chunk
    (vanguard.pipeline.SpriteUse claims ``debris-`` + every chunk key of a level's debris) or a level's ``tows`` are
    drawn in it (``TOW_LOOKS``)."""
    if name.startswith("debris-"):
        return any(l23kit._rooted(name, "debris-" + c) for c in _debris_chunks()) or any(
            l23kit._rooted(name, look) for look in _tow_looks())
    return _plain_claimed(name)


l23kit.claimed = claimed                    # l23kit's write/load/where decide by its module's ``claimed``
write, load, where = l23kit.write, l23kit.load, l23kit.where


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def write_pivots(root, data):
    """Pivots go with their sprite's folder: assets/pivots when the root is claimed, else build/art-held/pivots."""
    folder = artkit.PIVOTS if claimed(root) else ROOT / "build" / "art-held" / "pivots"
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{root}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{root}.json").exists():
        (other / f"{root}.json").unlink()


# =========================================================================== render kit

def gunmetal(mats):
    """Q.11 b: belt_r60.look_make('b')'s gunmetal hull and charcoal facets over the unit's materials."""
    mats = list(mats)
    mats[A_HULL] = replace(mats[A_HULL], albedo=hx("5a606c"), shininess=90)
    mats[A_FACET] = replace(mats[A_FACET], albedo=hx("464b55"))
    return mats


def rim(img):
    """Q.11 b's rim (belt_r60's frame: enemies_r60.belt_rim(False) without its palette step): gold on the edge pixels
    facing the top-left sun, red on the far ones."""
    a = np.array(img.convert("RGBA"))
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
    out = a.astype(np.float64)
    out[tl, :3] = out[tl, :3] * 0.3 + GOLD_RIM * 0.7
    out[br, :3] = out[br, :3] * 0.45 + RED_RIM * 0.55
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def turned(scene, mats, k, n):
    """A +Y-forward model at heading k of n (0 = down the screen, clockwise), its patterns turning with it."""
    rot = model_rotation(np.pi / 2 + TAU * k / n)
    return (lambda p, s=scene: s(rotate_z(p, rot))), model_space_materials(mats, rot)


def render(scene, mats, size, extent, crisp=80, camera=None):
    if camera:
        scene = artkit.perspective(scene, camera)
    w, h = (size, size) if isinstance(size, int) else size
    hi, factor = artkit.render_hi(scene, mats, (w, h), extent)
    return artkit.native(hi, factor, crisp)


def scaled(scene, s):
    return lambda p: (lambda r: (r[0] * s, r[1]))(scene(p / s))


def emission_only(mats):
    return [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]


def star(w, k, col, arm=2.6):
    """A small additive four-point star with a white core (glints)."""
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    s = max(0.3, k)
    f = (np.exp(-np.abs(x) / 0.6) * np.exp(-np.abs(y) / (arm * s)) + np.exp(-np.abs(y) / 0.6) * np.exp(
        -np.abs(x) / (arm * s))) * 0.75 + np.exp(-(np.hypot(x, y) / (1.3 * s)) ** 2)
    f = artkit.ordered_dither(np.clip(f * k, 0, 1), 6)
    rgb = (np.asarray(col, float) * 0.8 + 50)[None, None] * f[..., None] + 205 * np.clip(f - 0.6, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


# =========================================================================== jobs (one render each)

TAL_SIZE, TAL_EXT = 48, 48 / PX
SAM_SIZE, SAM_EXT = 48, 2.3
MITE_SIZE, MITE_EXT = 24, 24 / (e60.MITE_N / 2.3)
HAWK_SIZE, HAWK_EXT, HAWK_CAM = b60.SH_SIZE, b60.SH_EXT, b60.SH_CAM
BANKS = (-30, -15, 0, 15, 30)
ROCKS = {"a": (44, 0.92, 11), "b": (40, 0.82, 23), "c": (36, 0.74, 37)}      # canvas, radius (units), seed
CLAMP_GUN, CLAMP_EXT = 40, 40 / PX
CLAMP_W, CLAMP_H = 96, 56
GUN_MUZZLE = (0.0, 0.98)                     # model units: the clamp gun's barrel tip (+Y forward)
EMBER = ships_r02.ROOK_SCHEMES["a"][1]
LAUNCH = 42


def _talon(k, leader):
    scene, mats = e60.talon_model("b", leader)
    return render(*turned(scene, gunmetal(mats), k, 16), TAL_SIZE, TAL_EXT)


def _talon_guns(k):
    """The guns alone, lit (an emission mask): where the twin muzzles are."""
    scene, mats = e60.talon_model("b", False)
    mats = emission_only(mats)
    mats[A_GUN] = Material((0, 0, 0), emission=(1.0, 0.0, 1.0))
    return render(*turned(scene, mats, k, 16), TAL_SIZE, TAL_EXT, crisp=0)


def _wing_scene():
    scene, mats = e60.talon_model("b", False)
    centre = WING_CENTRE

    def clipped(p):
        q = p + np.array((centre[0], centre[1], 0.0))
        d, m = scene(q)
        return np.maximum(np.maximum(d, 0.3 - q[:, 0]), -0.55 - q[:, 1]), m
    return clipped, gunmetal(mats)


def _talon_wing(i):
    scene, mats = _wing_scene()
    return render(*turned(scene, mats, i, 8), 24, 24 / PX)


def _wing_centre():
    """The centroid of the Talon's right wing (x > 0.3, ahead of the tail fins) in model units, from the SDF on the
    wing plate's plane z = -0.02."""
    scene, _ = e60.talon_model("b", False)
    ys, xs = np.mgrid[-0.55:1.2:176j, 0.3:1.2:91j]
    p = np.stack([xs.ravel(), ys.ravel(), np.full(xs.size, -0.02)], -1)
    d, _ = scene(p)
    inside = d < 0
    return float(p[inside, 0].mean()), float(p[inside, 1].mean())


WING_CENTRE = _wing_centre()


def _sam_base(lights, glow=1.0):
    """The concept's base with its four lights enlarged so their colour reads at 48 px (white dormant, red awake)."""
    scene0, mats = e60.sam_base("a", lights, glow)

    def scene(p):
        d, m = scene0(p)
        for a in np.linspace(0, TAU, 4, endpoint=False) + np.pi / 4:
            dl = sd_sphere(p, (0.73 * np.cos(a), 0.73 * np.sin(a), 0.22), 0.11)
            m = np.where(dl < d, e60.A_X, m)
            d = np.minimum(d, dl)
        return d, m
    return render(scene, gunmetal(mats), SAM_SIZE, SAM_EXT)


def _sam_rack(k, hatch, lock, lamp, lift=1.0):
    """The rack at heading k; ``lamp`` mixes the lock light from white (0) to red (1); ``lift`` scales it (folded
    low = smaller from above)."""
    scene, mats = e60.sam_rack("a", hatch, lock, "red")
    col = np.array((1.0, 0.95, 0.85)) * (1 - lamp) + np.asarray(e60.em.RED) * lamp
    mats = gunmetal(mats)
    mats[-1] = e60.emissive(col, 0.3 + 1.8 * max(lock, 0.35 * (1 - lamp) + 0.25 * lamp))
    if lift != 1.0:
        scene = scaled(scene, lift)
    return render(*turned(scene, mats, k, 16), SAM_SIZE, SAM_EXT)


def _mite(k, pose):
    if pose < 4:
        scene, mats = e60.mite_model("a", phase=pose * TAU / 4, pose="cling")
    else:
        scene, mats = e60.mite_model("a", pose="crouch" if pose == 4 else "leap")
    return render(*turned(scene, mats, k, 16), MITE_SIZE, MITE_EXT, crisp=60)


def rock_model(radius, seed):
    """A lumpy belt rock: a sphere pushed out by a few broad seeded bumps and two flat facets, three craters cut in,
    flattened a little; dark brown-grey regolith speckled by a fine seeded pattern (the belt's rock, lit hard)."""
    rng = np.random.default_rng(seed)
    bumps = [(v / np.linalg.norm(v), rng.uniform(0.14, 0.3), rng.uniform(0.12, 0.28))
             for v in rng.normal(size=(9, 3))]
    cuts = [(v / np.linalg.norm(v), rng.uniform(0.7, 0.84)) for v in rng.normal(size=(4, 3))]
    craters = [(v / np.linalg.norm(v), rng.uniform(0.2, 0.32)) for v in rng.normal(size=(3, 3))]
    ph = rng.uniform(0, TAU, 3)

    def speckle(p, n):
        cell = np.floor(p * 11.0)
        h = np.sin(cell @ np.array((12.9898, 78.233, 37.719)) + ph[0]) * 43758.5453
        h = h - np.floor(h)
        g = np.sin(p[:, 0] * 5 + ph[1]) * np.sin(p[:, 1] * 4 + ph[2]) * np.sin(p[:, 2] * 6)
        return 0.8 + 0.16 * h + 0.12 * g
    mats = [Material((0.44, 0.38, 0.32), shininess=10, spec=0.12, pattern=speckle),
            Material((0.27, 0.23, 0.2), shininess=8, spec=0.08)]

    def scene(p):
        q = p * np.array((1.0, 1.08, 1.4))
        r = np.linalg.norm(q, axis=-1)
        n = q / np.maximum(r, 1e-6)[:, None]
        disp = sum(a * np.exp(-(1 - n @ v) / w) for v, a, w in bumps)
        d = (r - radius * (0.8 + disp)) / 1.6
        for v, k in cuts:
            d = np.maximum(d, p @ v - radius * k)
        m = np.zeros(len(p), dtype=int)
        for v, cr in craters:
            c = v * radius * 1.02
            c[2] = abs(c[2]) * 0.7 + 0.25 * radius
            dc = np.linalg.norm(p - c, axis=-1) - cr * radius
            m = np.where((dc < 0.05) & (d < 0.06), 1, m)
            d = np.maximum(d, -dc)
        return d, m
    return scene, mats


def _rock(name, k):
    size, radius, seed = ROCKS[name]
    scene, mats = rock_model(radius, seed)
    return render(*turned(scene, mats, k, 16), size, size / PX, crisp=70)


def _hawk(k, bank, iff):
    scene, mats = b60.stormhawk_model("a", np.pi / 2 + TAU * k / 16, bank, iff)
    return render(scene, mats, HAWK_SIZE, HAWK_EXT, camera=HAWK_CAM)


def clamp_gun_model():
    def scene(p):
        return union((sd_cylinder_z(p, (0, 0, 0.05), 0.62, 0.1), A_HULL),
                     subtract((sd_cylinder_z(p, (0, 0, 0.12), 0.6, 0.06), A_GOLD),
                              sd_cylinder_z(p, (0, 0, 0.12), 0.5, 0.2)),
                     (sd_ellipsoid(p, (0, -0.04, 0.24), (0.44, 0.5, 0.26)), A_FACET),
                     (sd_box(p, (0, 0.34, 0.34), (0.16, 0.14, 0.08), 0.03), A_HULL),
                     (sd_capsule(p, (0, 0.3, 0.36), (0, 0.9, 0.36), 0.085, 0.07), A_GUN),
                     (sd_capsule(p, (0, 0.86, 0.36), (0, 0.98, 0.36), 0.1, 0.1), A_GOLD),
                     (sd_sphere(p, (0, -0.36, 0.44), 0.07), A_RED))
    return scene, gunmetal(e60.belt_mats("3a3e46"))


def _clamp_gun(k):
    return render(*turned(*clamp_gun_model(), k, 16), CLAMP_GUN, CLAMP_EXT)


def clamp_model(side, u):
    """The docking clamp (gun centre at the origin): a charcoal arm from the dock (outside) to a two-fingered jaw
    gripping the tender's flank (inside), gold hinge plates, hazard-free Helix work. ``side`` +1 = the left clamps
    (the jaw toward +x), -1 the right ones; ``u`` 0 closed .. 1 sprung open (the fingers apart, the arm back)."""
    back = 0.5 * u
    ang = 0.75 * u

    def scene(p):
        q = p.copy()
        q[:, 0] = q[:, 0] * side + back
        jaw = q - np.array((1.6, 0.0, 0.0))
        items = [(sd_box(q, (-0.4, 0.0, 0.16), (1.85, 0.34, 0.16), 0.06), A_FACET),
                 (sd_box(q, (-0.7, 0.0, 0.34), (1.3, 0.2, 0.04), 0.02), A_HULL),
                 (sd_box(q, (0.0, 0.0, 0.2), (0.72, 0.64, 0.2), 0.08), A_HULL),
                 subtract((sd_box(q, (0.0, 0.0, 0.41), (0.7, 0.62, 0.03), 0.02), A_GOLD),
                          sd_box(q, (0.0, 0.0, 0.41), (0.56, 0.48, 0.1))),
                 (sd_box(q, (1.15, 0.0, 0.2), (0.3, 0.3, 0.14), 0.05), A_FACET),
                 (sd_cylinder_z(jaw, (0, 0, 0.22), 0.3, 0.22), A_GOLD)]
        for s in (-1, 1):
            a = s * ang
            c, sn = np.cos(a), np.sin(a)
            f = jaw.copy()
            f[:, 0], f[:, 1] = jaw[:, 0] * c + jaw[:, 1] * sn, -jaw[:, 0] * sn + jaw[:, 1] * c
            items.append((sd_capsule(f, (0.1, s * 0.34, 0.2), (0.75, s * 0.5, 0.2), 0.14, 0.11), A_FACET))
            items.append((sd_capsule(f, (0.72, s * 0.5, 0.2), (0.9, s * 0.3, 0.2), 0.11, 0.08), A_GOLD))
        items.append((sd_sphere(q, (-1.9, 0.2, 0.36), 0.07), A_RED))
        return union(*items)
    return scene, gunmetal(e60.belt_mats("3a3e46"))


def _clamp(side, u):
    return render(*clamp_model(side, u), (CLAMP_W, CLAMP_H), CLAMP_W / PX)


def _launch(i):
    s = 0.55 + 0.45 * float(e60.smooth(i / 7))
    scene, mats = models.ship_c_model(bank=0.0, palette=EMBER)
    return render(scene, mats, LAUNCH, 2.3 / s, camera=4.0)


JOBS = {"talon": _talon, "guns": _talon_guns, "wing": _talon_wing, "base": _sam_base, "rack": _sam_rack,
        "mite": _mite, "rock": _rock, "hawk": _hawk, "gun": _clamp_gun, "clamp": _clamp, "launch": _launch}


def _job(job):
    return JOBS[job[0]](*job[1:])


def run(jobs):
    with ProcessPoolExecutor() as pool:
        return list(pool.map(_job, jobs, chunksize=1))


# =========================================================================== builders

def build_talon():
    res = run([("talon", k, ld) for ld in (False, True) for k in range(16)] + [("guns", k) for k in range(16)]
              + [("wing", i) for i in range(8)])
    body = artkit.quantize_set([rim(f) for f in res[:32]], 36)
    save_set("talon", body[:16])
    save_set("talon-leader", body[16:])
    muzzles = []
    for g in res[32:48]:
        a = np.array(g).astype(float)
        mag = (a[..., 0] > 60) & (a[..., 2] > 60) & (a[..., 3] > 0)
        ys, xs = np.nonzero(mag)
        if len(xs) == 0:
            muzzles.append([[24, 24], [24, 24]])
            continue
        cx = xs.mean()
        pts = []
        for side in (xs < cx, xs >= cx):
            sx, sy = xs[side], ys[side]
            pts.append([round(float(sx.mean()) + 0.5, 1), round(float(sy.mean()) + 0.5, 1)])
        muzzles.append(pts)
    save_set("talon-glint", [star(12, k, (255, 70, 50)) for k in (0.6, 1.0, 0.55)])
    save_set("talon-wing", artkit.quantize_set(res[48:], 24))
    write_pivots("talon", {"size": [TAL_SIZE, TAL_SIZE], "headings": 16,
                           "muzzles": muzzles, "note": "per heading the two gun muzzles (the pair's spawn and the "
                                                       "glint's centre), [x, y] px from the frame's top left"})


def build_sam():
    jobs = [("base", "red"), ("base", "white"), ("base", "red", 0.0)]
    jobs += [("rack", k, 0.0, 0.0, 1.0) for k in range(16)] + [("rack", k, 1.0, 1.0, 1.0) for k in range(16)]
    jobs += [("rack", 0, 0.0, 0.0, 0.0, 0.86)] + [("rack", 0, 0.0, 0.3 * i, i / 3, 0.86 + 0.14 * i / 3)
                                                  for i in range(4)]
    res = run(jobs)
    bases = artkit.quantize_set([rim(f) for f in res[:2]] + [wreck(res[2])], 28)
    save_set("sam-nest-base", bases[:1], single=True)
    save_set("sam-nest-base-dormant", bases[1:2], single=True)
    save_set("sam-nest-wreck", bases[2:], single=True)
    racks = artkit.quantize_set([rim(f) for f in res[3:]], 32)
    save_set("sam-nest", racks[:16])
    save_set("sam-nest-open", racks[16:32])
    save_set("sam-nest-dormant", racks[32:33], single=True)
    save_set("sam-nest-wake", racks[33:])


def wreck(base):
    """The base with its lights dead, scorched and pitted, the rack's torn stump in the middle."""
    a = np.array(base).astype(float)
    op = a[..., 3] > 0
    yy, xx = np.mgrid[0:SAM_SIZE, 0:SAM_SIZE].astype(float)
    noise = raster.fbm(SAM_SIZE, SAM_SIZE, 6, 2917, octaves=3, period=False)
    r = np.hypot(xx - 24, yy - 24)
    scorch = np.clip(1.1 - r / 20 + (noise - 0.5) * 0.8, 0, 1)
    a[..., :3] *= (1 - 0.7 * scorch[..., None]) * 0.8
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    d.polygon([(19, 21), (27, 19), (30, 26), (25, 30), (18, 28)], fill=(38, 40, 46, 255))
    d.line([(20, 22), (26, 20)], fill=(150, 140, 120, 255))
    for x, y in ((27, 23), (21, 27), (24, 21)):
        d.point((x, y), fill=(230, 150, 70, 255))
    out = np.array(img)
    out[~op] = 0
    return rim(Image.fromarray(out, "RGBA"))


def build_mite():
    res = run([("mite", k, pose) for k in range(16) for pose in range(6)])
    save_set("asteroid-mite", artkit.quantize_set(res, 28))


def rock_break(frames, f, n=8):
    """The rock shot apart: chips in its own colours flung out in a ring, a short grey dust ring (3 steps)."""
    w = 56
    img = Image.new("RGBA", (w, w))
    src = np.array(frames[0])
    cols = src[src[..., 3] > 0][:, :3]
    cols = cols[np.argsort(cols.sum(1))]
    u = (f + 1) / n
    rng = np.random.default_rng(71)
    dust = Image.new("RGBA", (w, w))
    dd = ImageDraw.Draw(dust)
    rr = 6 + 18 * u
    level = [170, 110, 60, 0][min(3, int(u * 4))]
    if level:
        dd.ellipse([w / 2 - rr, w / 2 - rr, w / 2 + rr, w / 2 + rr], outline=(150, 138, 122, level), width=3)
    img.alpha_composite(dust)
    d = ImageDraw.Draw(img)
    for k in range(12):
        a = k * TAU / 12 + rng.uniform(-0.2, 0.2)
        dist = (5 + 20 * u) * rng.uniform(0.75, 1.1)
        cx, cy = w / 2 + np.cos(a) * dist, w / 2 + np.sin(a) * dist
        s = rng.uniform(2.0, 4.2) * (1 - 0.4 * u)
        spin = a + 3 * u * (1 if k % 2 else -1)
        pts = [(cx + np.cos(spin + j * TAU / 4) * s * (1 + 0.3 * (j % 2)), cy + np.sin(spin + j * TAU / 4) * s)
               for j in range(4)]
        lit = cols[int(len(cols) * (0.85 if np.cos(a - 3.9) > 0 else 0.3))]
        d.polygon(pts, fill=tuple(int(c) for c in lit) + (255,))
    return img


def build_rocks():
    res = run([("rock", name, k) for name in ROCKS for k in range(16)])
    for i, name in enumerate(ROCKS):
        frames = artkit.quantize_set(res[16 * i:16 * (i + 1)], 24)
        save_set(f"debris-belt-{name}", frames)
        save_set(f"debris-belt-{name}-break", artkit.quantize_set([rock_break(frames, f) for f in range(8)], 24))


def build_hawk():
    res = run([("hawk", k, b, iff) for iff in ("off", "white", "red") for k in range(16) for b in BANKS])
    frames = artkit.quantize_set(res, 40)
    for j, name in enumerate(("wavering-stormhawk", "wavering-stormhawk-iff", "wavering-stormhawk-hit")):
        save_set(name, frames[80 * j:80 * (j + 1)])


def gas_frame(f, n=6):
    """The clamp's release: a white jet of gas to both sides of the jaw thinning away, a few sparks (additive)."""
    w = 48
    yy, xx = np.mgrid[0:w, 0:w].astype(float) + 0.5
    x, y = xx - w / 2, yy - w / 2
    u = (f + 0.5) / n
    jet = np.exp(-(y / (3 + 10 * u)) ** 2) * np.exp(-np.abs(x) / (4 + 10 * u)) * (1 - u) ** 1.2
    plume = np.exp(-((np.hypot(x, y) - 14 * u) / (3 + 4 * u)) ** 2) * (1 - u) * 0.5
    fld = artkit.ordered_dither(np.clip(jet + plume, 0, 1), 6)
    rgb = np.array((220, 232, 240.0))[None, None] * fld[..., None]
    img = Image.fromarray(np.dstack([rgb, np.full((w, w), 255.0)]).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(5)
    for k in range(7):
        a = rng.uniform(0, TAU)
        r0, r1 = 4 + 18 * u, 7 + 20 * u
        if f < 4:
            d.line([w / 2 + np.cos(a) * r0, w / 2 + np.sin(a) * r0, w / 2 + np.cos(a) * r1, w / 2 + np.sin(a) * r1],
                   fill=(255, 200, 120, 255))
    return artkit.additive(img)


def build_clamp():
    res = run([("gun", k) for k in range(16)] + [("clamp", s, u) for s in (1, -1) for u in
                                                  (0.0, 0.18, 0.38, 0.6, 0.82, 1.0)])
    save_set("clamp-gun", artkit.quantize_set([rim(f) for f in res[:16]], 32))
    save_set("clamp-gun-glint", [star(16, k, (255, 60, 40), 3.2) for k in (0.6, 1.0, 0.6)])
    clamps = artkit.quantize_set([rim(f) for f in res[16:]], 32)
    for j, side in enumerate(("left", "right")):
        save_set(f"clamp-gun-clamp-{side}", clamps[6 * j:6 * j + 1], single=True)
        save_set(f"clamp-gun-clamp-{side}-open", clamps[6 * j:6 * (j + 1)])
    save_set("clamp-gun-gas", [gas_frame(f) for f in range(6)])
    muzzle = []
    for k in range(16):
        dx, dy = local_to_screen(GUN_MUZZLE[0], GUN_MUZZLE[1], np.pi / 2 + TAU * k / 16, PX)
        muzzle.append([round(CLAMP_GUN / 2 + dx, 1), round(CLAMP_GUN / 2 + dy, 1)])
    write_pivots("clamp-gun", {"size": [CLAMP_GUN, CLAMP_GUN], "headings": 16, "muzzle": muzzle,
                               "clamp_size": [CLAMP_W, CLAMP_H],
                               "jaw": {"left": [CLAMP_W / 2 + 1.6 * PX, CLAMP_H / 2],
                                       "right": [CLAMP_W / 2 - 1.6 * PX, CLAMP_H / 2]},
                               "note": "muzzle: per heading the barrel tip in the gun's frame; jaw: the gas burst's "
                                       "centre in the clamp's frame (the gun's centre is the clamp's centre)"})


def build_launch():
    res = run([("launch", i) for i in range(8)])
    out = []
    for i, f in enumerate(res):
        k = 0.45 + 0.55 * float(e60.smooth(i / 6))
        a = np.array(f).astype(float)
        a[..., :3] *= k
        out.append(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"))
    save_set("rook-launch", artkit.quantize_set(out, 32))


BUILDERS = {"talon": build_talon, "sam": build_sam, "mite": build_mite, "rocks": build_rocks, "hawk": build_hawk,
            "clamp": build_clamp, "launch": build_launch}


# =========================================================================== review

def plate(w=480, h=540, seed=291):
    """The review ground: Level 29's production canyon when backdrop_l29.py has written it, else round 60's hull."""
    tile = BACKDROP / "dock-canyon.png"
    if tile.exists():
        src = Image.open(tile).convert("RGBA")
        return src.crop((0, src.height - h, w, src.height)) if src.height >= h else src.resize((w, h))
    return e60.hull_bg(seed, h).crop((0, 0, w, h)).convert("RGBA")


def gif_frames(draw, seconds, base):
    return [draw(base.copy(), i / FPS) for i in range(int(seconds * FPS))]


def paste(dst, img, x, y):
    return l23kit.paste_c(dst, img, x, y)


def add(dst, img, x, y, k=1.0):
    return l23kit.add_c(dst, img, x, y, k)


def ship_sprite():
    return artkit.load_frames("ship")[2]


def caption(img, text):
    e60.caption(img, text)
    return img


def save_review(kind, subject, sheet, gif):
    png, gif_path = artkit.review_paths(CONCEPT[kind], subject)
    sheet.convert("RGB").save(png, optimize=True)
    size = e60.rig.write_gif(gif, gif_path, fps=FPS, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif_path.name} ({size / 1e6:.1f} MB)")


def review_talon():
    t, lead, glint, wing = load("talon"), load("talon-leader"), load("talon-glint"), load("talon-wing")
    shield = load("enemy-shield-small")
    expl = artkit.load_frames("explosion-small")
    gold = [e60.gold_share(f) for f in t[:16]]
    sheet = artkit.review_sheet("TALON (FINAL R62): ROUND 60 A.1 B IN GUNMETAL (Q.11 B)", [
        (f"TALON, 16 HEADINGS (GOLD SHARE {100 * min(gold):.0f}-{100 * max(gold):.0f} %)", t, 2, False),
        ("TALON LEADER (THE GOLD SPINE STRIPE)", lead, 2, False),
        ("GLINT (ADDITIVE) | WING DEBRIS", glint + wing, 3, False),
        ("ON LEVEL 29'S CANYON (1X): HEADINGS 0, 4, 8, 12 AND THE SHIELD", [over_plate(
            [t[0], lead[0], t[4], t[8], t[12]], shield[0])], 2, False)], batch=BATCH)

    def draw(f, s):
        for i in range(5):
            x, y, h, alive = e60.talon_state(i, s)
            if not alive:
                ks = s - e60.TAL_HITS[i][2]
                if ks < 0.6:
                    f = paste(f, expl[min(11, int(ks * 20))], x, y)
                    f = paste(f, wing[int(ks * 16) % 8], x + 18 * ks * 30 / 20, y - 10 * ks)
                continue
            k = l23kit.heading_index(np.cos(h), np.sin(h))
            f = paste(f, (lead if i == 0 else t)[k], x, y)
            if s < e60.TAL_HITS.get(i, (99,))[0]:
                f = add(f, shield[int(s * 10) % 4], x, y, 0.8)
            if 1.0 < s % 1.6 < 1.25 and y < 300:
                pts = json.loads(pivot_path("talon").read_text())["muzzles"][k]
                for px_, py_ in pts:
                    f = add(f, glint[min(2, int((s % 1.6 - 1.0) * 12))], x - 24 + px_, y - 24 + py_)
        for j in range(4):
            if 5.0 < s < 8.8:
                x, y, h = e60.line_state(j, s)
                f = paste(f, t[l23kit.heading_index(np.cos(h), np.sin(h))], x, y)
        f = paste(f, ship_sprite(), 240, 480)
        return caption(f, "V-WING: THE LEADER'S KILL SCATTERS THE WING" if s < 5 else "LINE ABREAST, NO LEADER")
    save_review("talon", "talon", sheet, gif_frames(draw, 9.0, plate()))


def over_plate(frames, shield=None):
    img = plate(len(frames) * 56 + 8, 64)
    for i, fr in enumerate(frames):
        img = paste(img, fr, 32 + 56 * i, 32)
        if shield is not None and i == 0:
            img = add(img, shield, 32, 32, 0.8)
    return img


def pivot_path(root):
    p = artkit.PIVOTS / f"{root}.json"
    return p if p.exists() else ROOT / "build" / "art-held" / "pivots" / f"{root}.json"


def review_sam():
    base, dorm_b, wreck_ = load("sam-nest-base")[0], load("sam-nest-base-dormant")[0], load("sam-nest-wreck")[0]
    rack, opened, dorm, wake = load("sam-nest"), load("sam-nest-open"), load("sam-nest-dormant")[0], load(
        "sam-nest-wake")
    diamond, exhaust = load("homing-diamond"), load("homing-diamond-exhaust")
    combo = []
    for r in rack[::4]:
        c = base.copy()
        c.alpha_composite(r)
        combo.append(c)
    sheet = artkit.review_sheet("SAM NEST (FINAL R62): ROUND 60 A.11 A IN GUNMETAL (Q.11 B)", [
        ("BASE AWAKE | DORMANT | WRECK", [base, dorm_b, wreck_], 3, False),
        ("RACK, 16 HEADINGS (HATCH SHUT)", rack, 2, False),
        ("RACK OPEN: THE 0.4 S TELL (HATCH UP, LOCK LIGHT)", opened, 2, False),
        ("DORMANT RACK | THE 1 S WAKE (WHITE -> RED, LIFTING)", [dorm] + wake, 3, False),
        ("ASSEMBLED (BASE + RACK), HEADINGS 0, 4, 8, 12", combo, 3, False)], batch=BATCH)
    nests = [(130, 90, 2.0), (350, 40, None)]

    def draw(f, s):
        sx = 240 + 120 * np.sin(s * 0.7)
        for x, y0, wake_t in nests:
            y = y0 + 30 * s
            dormant = wake_t is not None and s < wake_t
            f = paste(f, dorm_b if dormant else base, x, y)
            if dormant:
                f = paste(f, dorm, x, y)
                continue
            if wake_t is not None and s < wake_t + 1.0:
                f = paste(f, wake[min(3, int((s - wake_t) * 4))], x, y)
                continue
            k = l23kit.heading_index(sx - x, 480 - y)
            tell = (s - (wake_t or 0) - 1.2) % 4.5 < 0.4
            f = paste(f, (opened if tell else rack)[k], x, y)
            launch = (s - (wake_t or 0) - 1.6) % 4.5
            for m in range(2):
                u = launch - 0.25 * m
                if 0 < u < 3.0:
                    a = np.arctan2(480 - y, sx - x)
                    mx, my = x + np.cos(a) * 110 * u, y + np.sin(a) * 110 * u
                    hk = l23kit.heading_index(np.cos(a), np.sin(a))
                    f = add(f, exhaust[(hk * 2 + int(s * 10) % 2) % 32], mx, my)
                    f = paste(f, diamond[hk], mx, my)
        f = paste(f, ship_sprite(), sx, 480)
        return caption(f, "DORMANT, WHITE-LIT; AT ITS MOMENT IT WAKES RED" if s < 3.2 else "SALVOS OF 2 DIAMONDS")
    save_review("sam", "sam-nest", sheet, gif_frames(draw, 9.0, plate()))


def review_mite():
    mite = load("asteroid-mite")
    rocks = {n: load(f"debris-belt-{n}") for n in ROCKS}
    breaks = {n: load(f"debris-belt-{n}-break") for n in ROCKS}
    sheet = artkit.review_sheet("ASTEROID MITE AND THE BELT ROCKS (FINAL R62): ROUND 60 A.7 A", [
        ("MITE: CRAWL 0-3, CROUCH (TELL), LEAP, HEADING 0", mite[:6], 4, False),
        ("MITE, CRAWL FRAME 0 AT THE 16 HEADINGS", mite[::6], 3, False),
        ("ROCK BELT-A, 16 TURNS", rocks["a"], 2, False),
        ("ROCKS BELT-B AND BELT-C, EVERY 4TH TURN", rocks["b"][::4] + rocks["c"][::4], 2, False),
        ("ROCK BELT-A SHOT APART (8 FR)", breaks["a"], 2, False)], batch=BATCH)
    plan = [("a", 330, -40, -12, 95, 0.6, 4, None), ("b", 120, -140, 6, 95, -0.5, 3, 2.6)]
    sx_of = lambda s: 240 + 90 * np.sin(s * 0.9)                                  # noqa: E731
    trig = {}                                    # rock -> the moment its riders start the tell (near or shot)
    for name, x0, y0, vx, vy, spin, n, shot in plan:
        for i in range(int(7.5 * FPS)):
            s = i / FPS
            if shot is not None and s >= shot:
                trig[name] = shot + 0.3
                break
            if np.hypot(sx_of(s) - (x0 + vx * s), 480 - (y0 + vy * s)) < 160:
                trig[name] = s
                break

    def draw(f, s):
        sx = sx_of(s)
        for name, x0, y0, vx, vy, spin, n, shot in plan:
            gone = shot is not None and s >= shot
            if gone:
                x, y, a = x0 + vx * shot, y0 + vy * shot, spin * shot
                if s - shot < 0.8:
                    f = paste(f, breaks[name][min(7, int((s - shot) * 10))], x, y)
            else:
                x, y, a = x0 + vx * s, y0 + vy * s, spin * s
                f = paste(f, rocks[name][int(round(a / (TAU / 16))) % 16], x, y)
            t0 = trig.get(name, 99.0)
            for j in range(n):
                ang = j * TAU / n + 0.4 + a
                mx, my = x + np.cos(ang) * 22, y + np.sin(ang) * 22
                tell = t0 + 0.15 * j
                if s < tell:                                     # clinging, crawling, facing out from the rock
                    k = l23kit.heading_index(np.cos(ang), np.sin(ang))
                    f = paste(f, mite[k * 6 + int(s * 8) % 4], mx, my)
                    continue
                if s < tell + 0.4:                               # the crouch: the tell
                    k = l23kit.heading_index(sx - mx, 480 - my)
                    f = paste(f, mite[k * 6 + 4], mx, my)
                    continue
                ax, ay = x0 + vx * (tell + 0.4), y0 + vy * (tell + 0.4)
                if gone:
                    ax, ay = x0 + vx * shot, y0 + vy * shot
                ang0 = j * TAU / n + 0.4 + spin * min(tell + 0.4, shot if gone else 99)
                mx, my = ax + np.cos(ang0) * 22, ay + np.sin(ang0) * 22
                d = np.array((sx_of(tell + 0.4) - mx, 480 - my))
                d /= np.linalg.norm(d) + 1e-9
                u = s - tell - 0.4
                f = paste(f, mite[l23kit.heading_index(*d) * 6 + 5], mx + d[0] * 260 * u, my + d[1] * 260 * u)
        f = paste(f, ship_sprite(), sx, 480)
        return caption(f, "RIDERS CLING AND CRAWL; WITHIN 160 PX THEY CROUCH AND LEAP")
    save_review("mite", "asteroid-mite", sheet, gif_frames(draw, 7.5, plate()))


def review_hawk():
    off, iff, hit = load("wavering-stormhawk"), load("wavering-stormhawk-iff"), load("wavering-stormhawk-hit")
    talon = load("talon")
    sheet = artkit.review_sheet("WAVERING STORMHAWK (FINAL R62): ROUND 60 A.22 A, CERES WING OCHRE", [
        ("HEADING 0 AT THE 5 BANKS: IFF DARK | WHITE | RED", off[:5] + iff[:5] + hit[:5], 3, False),
        ("LEVEL, 16 HEADINGS (IFF DARK)", off[2::5], 2, False),
        ("LINE-UP AT 1X: LANCER, ROOK, A WAVERING CRAFT, A TALON", [over_plate(
            [ship_sprite(), artkit.load_frames("rook")[2], off[2], talon[0]])], 2, False)], batch=BATCH)

    def draw(f, s):
        for i in range(2):
            x, y, h, bank = b60.hawk_state(i, s)
            k = l23kit.heading_index(np.cos(h), np.sin(h))
            b = int(np.argmin([abs(bank - v) for v in BANKS]))
            red = i == 0 and 4.4 <= s < 4.6
            lit = (s % 1.0) < 0.5
            f = paste(f, (hit if red else iff if lit else off)[k * 5 + b], x, y)
        f = paste(f, ship_sprite(), 240, 480)
        return caption(f, "IFF BLINKS WHITE, HOLDS FIRE, WAGGLES, PEELS OFF; A HIT FLASHES RED")
    save_review("hawk", "wavering-stormhawk", sheet, gif_frames(draw, 10.0, plate()))


def review_clamp():
    gun, glint, gas = load("clamp-gun"), load("clamp-gun-glint"), load("clamp-gun-gas")
    cl = {s: load(f"clamp-gun-clamp-{s}")[0] for s in ("left", "right")}
    op = {s: load(f"clamp-gun-clamp-{s}-open") for s in ("left", "right")}
    launch, rook = load("rook-launch"), artkit.load_frames("rook")
    expl = artkit.load_frames("explosion-medium")
    sheet = artkit.review_sheet("CLAMP GUN, DOCKING CLAMPS, THE EMBER'S LAUNCH (FINAL R62)", [
        ("CLAMP GUN, 16 HEADINGS | GLINT", gun + glint, 2, False),
        ("CLAMP LEFT: CLOSED .. SPRUNG OPEN (6 FR)", op["left"], 2, False),
        ("CLAMP RIGHT: CLOSED .. SPRUNG OPEN", op["right"], 2, False),
        ("GAS BURST (ADDITIVE, 6 FR)", gas, 2, True),
        ("ROOK'S EMBER LAUNCHING (8 FR), THEN ROOK_2", launch + [rook[2]], 3, False)], batch=BATCH)
    guns = [(150, 170, "left", 2.0), (330, 170, "right", 3.2), (150, 330, "left", 4.4), (330, 330, "right", 5.6)]
    tender = Image.new("RGBA", (120, 230))
    ImageDraw.Draw(tender).rounded_rectangle([4, 4, 115, 225], 30, fill=(70, 74, 82, 255), outline=(200, 160, 70, 255))
    tender_path = BACKDROP / "prison-tender.png"
    if tender_path.exists():
        tender = Image.open(tender_path).convert("RGBA")

    def draw(f, s):
        f = paste(f, tender, 240, 250)
        for x, y, side, kill in guns:
            if s < kill:
                f = paste(f, cl[side], x, y)
                k = l23kit.heading_index(240 - x, 480 - y)
                f = paste(f, gun[k], x, y)
                if (s % 2.0) > 1.7:
                    m = json.loads(pivot_path("clamp-gun").read_text())["muzzle"][k]
                    f = add(f, glint[1], x - 20 + m[0], y - 20 + m[1])
            else:
                u = s - kill
                f = paste(f, op[side][min(5, int(u * 20))], x, y)
                jx = x + (32 if side == "left" else -32)
                if u < 0.6:
                    f = add(f, gas[min(5, int(u * 10))], jx, y)
                if u < 0.6:
                    f = paste(f, expl[min(11, int(u * 20))], x, y)
        if s > 5.6:
            u = s - 5.6
            if u < 1.6:
                f = paste(f, launch[min(7, int(u * 5))], 240, 250 - 20 * u)
            else:
                f = paste(f, rook[2], 240 + (300 - 240) * min(1, u - 1.6), 218 + (440 - 218) * min(1, u - 1.6))
        f = paste(f, ship_sprite(), 240, 480)
        return caption(f, "THE FOUR CLAMP GUNS FALL; THE CLAMPS SPRING; THE EMBER LAUNCHES")
    save_review("clamp", "clamp-gun", sheet, gif_frames(draw, 8.0, plate()))


REVIEWS = {"talon": review_talon, "sam": review_sam, "mite": review_mite, "rocks": None, "hawk": review_hawk,
           "clamp": review_clamp, "launch": None}


# =========================================================================== check

SETS = {"talon": [("talon", 16), ("talon-leader", 16), ("talon-glint", 3), ("talon-wing", 8)],
        "sam": [("sam-nest-base", 1), ("sam-nest-base-dormant", 1), ("sam-nest", 16), ("sam-nest-open", 16),
                ("sam-nest-dormant", 1), ("sam-nest-wake", 4), ("sam-nest-wreck", 1)],
        "mite": [("asteroid-mite", 96)],
        "rocks": [(f"debris-belt-{n}{s}", c) for n in ROCKS for s, c in (("", 16), ("-break", 8))],
        "hawk": [("wavering-stormhawk", 80), ("wavering-stormhawk-iff", 80), ("wavering-stormhawk-hit", 80)],
        "clamp": [("clamp-gun", 16), ("clamp-gun-glint", 3), ("clamp-gun-clamp-left", 1),
                  ("clamp-gun-clamp-right", 1), ("clamp-gun-clamp-left-open", 6), ("clamp-gun-clamp-right-open", 6),
                  ("clamp-gun-gas", 6)],
        "launch": [("rook-launch", 8)]}


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
        if s == "rocks" and "mite" not in subjects:
            review_mite()
        elif s == "launch" and "clamp" not in subjects:
            review_clamp()
        elif REVIEWS.get(s):
            REVIEWS[s]()
