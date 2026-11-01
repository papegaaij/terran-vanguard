#!/usr/bin/env python3
"""Production art: the Ascendant, Level 42's act boss (M10 part H batch, concept round 80, step ART-H1): Vorne's
flagship bow-on over the halted arena (design/enemies/bosses/ascendant, round 72 **A.10 a** "the gilded wedge"), its
parts in their states, the tells, the eruption, the Vrell half, the hull's break along a **jagged fracture** (round 72
A.11, round 73's storyboard ``ascendant-phases-r73-a`` frame 8), the lifeboat and its escape; the intel silhouette
(through intel.py).

The models are round 72's (tools/concept/act6_r72.py, imported unchanged: ``asc_mats`` the elite livery with the Vrell
slots, ``WEDGE``/``LIGHTS_A``/``split_w`` the wedge, ``generator_model``, ``battery_model``, ``sac_model``,
``heart_model``, ``lifeboat_model`` a; the jagged break's ``ragged_line``, ``ragged_masks``, ``torn_half``, ``_grow`` and
``ASC_BREAK`` with round 73's seed 7301) drawn 1:1 in screen px (100 px a model unit). The hull is ``hull_a`` extended
here: its breaches sit where the simulation puts the side sacs, and it carries the bow's four gun ports, the two Dart
bays and the two shoulder racks the tells open. The cannon is ``cannon_model`` split into the fixed base and a turret
that aims at 64 headings (barrels 3 px shorter so the cell stays 66 px). The mirror panels are new (the concept turned
a flat crescent as an image): a sloped silver-gold crescent round its battery, rendered at each heading it can face.
Gold share 35-45 % with the elite rim (2 px gold on the edges facing the top-left sun, red on the far ones).

The parts sit where the simulation puts them (sim/src/test/java/vanguard/sim/Act6HSpecs.java, step H1a/H1b, the spec's
screen 480x540, y down): the piece's centre at rest (240, 125); the cannon (240, 235), the generators (110, 60) and
(370, 60), the batteries (150, 130), (185, 190), (330, 130), (295, 190), the sacs (200, 50), (280, 50), (240, 150), the
heart (240, 100), the lifeboat's breakout (240, 175), the Dart bays at (180, 85) and (300, 85). The pivots give every
place as an offset from the centre, so step H2's data and H4a's AscendantLooks read them from one file.

Headings as tools/art/README.md: heading k turns k x 360/64 degrees clockwise from straight down, every heading its own
render under the fixed key light. The hull, its overlays and the bays/racks share one key light fixed in screen space
(round 72's, at the hull's scale), so an overlay matches the hull under it. Glows are smooth float fields written
unquantised (no ordered dither, far more than 32 steps); additive sets are premultiplied on black (GL_SRC_ALPHA,
GL_ONE), drawn once. Nothing lit is rotated or mirrored as an image (the wreck's halves drift without turning). The
cannon's hex shield is the enemy shield's ``enemy-shield-medium`` set (act5_gear.py, bodies up to 84 px); the hull's
break chain uses ``explosion-vacuum-medium``/``-large`` (explosions.py) at the pivots' ``chain``; the bounced bullets,
the spores and the lance line are the game's.

Outputs (build/art-held/sprites/ until the boss's data.yaml claims the root ``ascendant`` (step H2); rerun then and the
files move to assets/sprites/; step H4a draws them):
  ascendant-hull                    440x276 normal, top-left at screen (20, -10): the phase-1 hull (one piece, the
                                    running lights on, ports, bays and racks shut; 48 colours shared with the bays and
                                    racks); phase 2 draws ``ascendant-keel_5`` over it
  ascendant-keel_0..5               160x276 normal at hull-frame (140, 0): the eruption over the hull (the keel splitting
                                    open, the plates peeled outward, the teal-black tissue swelling out of the keel and
                                    two breaches; only the pixels that change, the rest clear); 6 frames over the 3 s of
                                    erupting (0.5 s each), frame 5 = phase 2's look, kept to the break
  ascendant-keel-cracks_0..2        48x276 additive at hull-frame (196, 0): the eruption's 1 s shudder tell, cracks of
                                    teal light running down the keel (0 the first third, 1 the whole keel, 2 flaring),
                                    then held at 2 under the erupting frames, fading
  ascendant-bay-l_0..3, -bay-r_0..3 36x28 normal, centred on the pivots' ``bays``: the Dart bays' doors, shut (0) to open
                                    (3) over the 0.8 s tell, closing after the stream; ``-bay-light`` 40x32 additive gold
                                    from the open bay (strength = the doors' share)
  ascendant-rack-l_0..3, -rack-r_0..3  26x26 normal at the pivots' ``racks``: the shoulder racks' hatches opening on the
                                    missile salvo's 0.8 s tell (3 SAM diamonds showing); ``-rack-lock`` 16x16 additive
                                    red, the SAM Nest's lock light at the rack's ``lock`` point
  ascendant-port-flash_0..3         24x24 additive gold at each of the four ``ports``: the gold fan's tell (0.8 s, a
                                    flicker of frames 0..3, 2 steps a frame), and the grafted guns' muzzles in phase 2
                                    (at the dead generators)
  ascendant-lamp-red, -lamp-gold    16x16 additive: the running lights (``lamps.red``, 16, pairs ordered from the bow
                                    back) and the spine's gold lamps (``lamps.gold``, from the bow back): the arrival
                                    lights them over the 6 s slide-in, bow first
  ascendant-cannon-base_0..2        76x60 normal centred on the cannon: the fixed base and gold collar; 0 whole,
                                    1 damaged (below half: soot, the eye dimmed), 2 dead (the dome burst, no turret)
  ascendant-cannon_0..63            66x66 normal centred on the cannon, 64 headings: the turret (dome, twin barrels,
                                    red eye) drawn over the base while it lives; ``muzzle`` per heading in the pivots
  ascendant-muzzle_0..5             40x40 additive at the muzzle: the spinal lance's 0.8 s white-gold glow (frame =
                                    progress x 6), held at 5 under the told line
  ascendant-generator_0..3          56x56 normal: 0 lit, 1 damaged (the orb dim, soot), 2 dead (dark, sooted),
                                    3 grafted (dead with tissue tendrils in the mount: phase 2's grafted guns)
  ascendant-generator-glow          48x48 additive gold halo round a living orb (the game pulses it)
  ascendant-beam_0..3               24x32 additive gold strip tiling along y: the beam from each living generator to
                                    the cannon (``beams``), stretched over the line, 4 frames of the pulse running down
  ascendant-battery_0..2            52x52 normal: 0 whole, 1 damaged, 2 dead
  ascendant-panel_0..17             64x64 normal centred on the battery: the mirror panel (polished silver-gold
                                    crescent, gold tip caps) at headings ``panels.headings`` (left face 4..12, right
                                    face 52..60: 45 degrees to its side +-20); 40 colours
  ascendant-panel-glow_0..17        64x64 additive white: the plate's coverage with a soft 1 px halo, the mask the
                                    heat's colour is drawn through (MirrorLooks.heatColour: dull red, orange, white)
  ascendant-panel-cracks_0..17      64x64 normal white lines (alpha = coverage): the cracks, drawn tinted dark from
                                    70 % heat and in the heat's colour through a white-hot plate
  ascendant-panel-rim_0..17         64x64 additive red: the burst's 0.5 s tell, the plate's rim glowing red at strength 1
  ascendant-panel-down_0..17        64x64 normal: the shattered panel (3 s down): the gold caps and a jagged dark stub
  ascendant-panel-sweep             24x24 additive: the slow specular sweep's glint, moved along the plate's arc
                                    (pivots ``panels.frames``: the arc's centre, radius, tips) as MirrorLooks does
                                    (2.5 s tip to tip); also the 0.4 s silver shimmer when the panel grows back
  ascendant-sac_0..4                60x60 normal: 0 at rest, 1..3 swelling (the brood's 0.8 s tell), 4 split open (the
                                    brood leaving); ``-sac-dead`` the burst husk
  ascendant-sac-core, -heart-core   44x44 / 64x64 additive lime: the weak point's core (the game pulses it)
  ascendant-sac-heal, -heart-heal   60x60 / 68x68 additive teal: the healing pulse over the membrane (strength by the
                                    regrowth), ``-sac-flare`` 60x60 teal-white: the brood tell's flaring seams
  ascendant-heart_0..3              68x68 normal: 0 sealed under its membrane, 1..2 opening (two sacs dead: 0.4 s), 3 open
  ascendant-heart-burst_0..7        128x128 additive: K, the heart bursting lime-white with torn teal fibres (12 fps)
  ascendant-sac-burst_0..5          64x64 additive: a sac bursting (K, and a sac's death)
  ascendant-plate_0..7              16x16 normal: a gold-edged hull plate tumbling (the eruption's peeled plates and
                                    K's chain blowing plates outward; 12 fps)
  ascendant-shard_0..7              12x12 normal: a charred shard tumbling (the fracture's debris)
  ascendant-wreck-l, -wreck-r       normal, at ``wreck.<side>.offset`` in the hull frame: the broken hull's halves,
                                    torn apart along the authored jagged fracture (round 73's ragged line, seed 7301):
                                    charred edges, bent plate lips, torn flaps bent outward across the break, the
                                    sacs burst, darkened as the lights die (48 colours shared)
  ascendant-wreck-l-rim, -wreck-r-rim  additive: the fracture's hot rim (orange, glowing in burning runs), same frame
                                    as its half; the game fades it over the escape
  ascendant-vent-orange_0..5, -vent-teal_0..5  24x56 additive: gas venting from the fracture (orange from the plating,
                                    teal from the tissue), pointing up the frame; the game turns it to the vent's angle
                                    (``wreck.<side>.vents``: x, y, degrees, colour) and flickers the frames
  ascendant-fire_0..5               24x24 additive: the breaches burning (``wreck.<side>.fires``, 10 fps)
  ascendant-lifeboat_0..4           72x88 normal: the seed-pod with the half-swallowed gold capsule, nose up, leaning
                                    with the weave: -20, -10, 0, +10, +20 degrees (+ = moving right)
  ascendant-lifeboat-drive_0..3     32x32 additive teal-lime at ``lifeboat.drive`` per tilt frame: its drive flickering
  ascendant-lifeboat-wake_0..5      28x28 additive teal: a spore-wake puff by age (0.6 s), dropped with each spore pair
  ascendant-lifeboat-tear_0..7      112x112 additive: the breakout at K + 3 s, a lime flash and torn teal fibres
  ascendant-lifeboat-flash_0..5     96x96 additive gold-white: its flash toward the gate at the top edge (ESCAPED)
  pivots/ascendant.json             the centre, the hull's frame and anchor, every part's offset, the cannon's muzzle per
                                    heading, the panels' headings and arcs, the beams, lamps, ports, bays, racks, the
                                    eruption's timing, K's chain (t, dx, dy, size) and plates, the wreck's halves
                                    (offsets, fracture, vents, fires, drift), the lifeboat's drive and canopy per tilt
  assets/sprites/intel/boss-ascendant.png   40x40, the sensor-L2 silhouette (intel.py's ``BOSSES``, round 80): the
                                    bow-on wedge with its generators, batteries and cannon
Review (round 80): design/enemies/bosses/ascendant/concept/ascendant-final-r80-a.png/.gif (phase 1: the hull and parts,
the panels, the tells, the arrival) and ascendant-fx-final-r80-a.png/.gif (the eruption, phase 2, K, the wreck, the
lifeboat), on round 72's Level 42 stand-in (act6_r72.py ``space_l42``: Level 42's production backdrop is ART-H2's);
intel.py's intel-final-r80-a.png.

Run: python3 tools/art/ascendant.py [hull] [parts] [fx] [intel] [--review] [--check]
     (no subject = all, ~6 min on 20 cores the first time; renders are cached in build/art-cache/ascendant by the model
     code's hash; --review rebuilds only the review files; --check lists every set with its folder, size and frame
     count and fails on a missing one, a missing Source chunk, a checker over 5 % or an additive set under 32 glow
     steps, and prints the gold share and the atlas share)
"""
import hashlib
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU, sprite

import act6_r72 as a6  # noqa: E402  (concept: round 72's Ascendant, imported unchanged)
import enemies_r60 as u60  # noqa: E402  (concept: the elite rim's colours, gold_share)
from render import raster, sdf  # noqa: E402
from render.enemy_models import A_ENGINE, A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED  # noqa: E402
from render.enemy_rigs import local_to_screen, model_space_materials  # noqa: E402
from render.sdf import (Material, mirror_x, rotate_x, rotate_y, rotate_z, sd_box, sd_capsule,  # noqa: E402
                        sd_cylinder_z, sd_ellipsoid, sd_sphere, union)

SCRIPT = "ascendant.py"
BATCH = "M10 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r80"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "bosses" / "ascendant" / "concept"
R = "ascendant"
CACHE = ROOT / "build" / "art-cache" / "ascendant"
WORKERS = 14
FW, FH = 480, 540
PPU = a6.PPU                                  # 100 px a model unit: the hull 1:1 in screen px
CENTRE = (240.0, 125.0)                       # the piece's centre at rest (Act6HSpecs X, Y)
HULL_BOX = (20, -10, 440, 276)                # x0, y0, w, h in screen px
KEEL_BOX = (160, -10, 160, 276)
CRACK_BOX = (216, -10, 48, 276)
CANNON = (240, 235)
GENS = [(110, 60), (370, 60)]
BATT = [(150, 130, -1), (185, 190, -1), (330, 130, 1), (295, 190, 1)]
SACS = [(200, 50), (280, 50), (240, 150)]
HEART = (240, 100)
LIFEBOAT = (240, 175)
BAYS = [(180, 85), (300, 85)]                 # Act6HSpecs' Dart bays: (-60, +40) and (60, +40) from the centre, y up
RACKS = [(140, 34), (340, 34)]                # the shoulder racks, inboard of the generators
PORTS = [(190, 204), (290, 204), (206, 224), (274, 224)]
BAY_HALF, RACK_HALF = (13, 9), (9, 9)
BAY_BOX, RACK_BOX = (36, 28), (26, 26)
TURRET_CELL, BASE_CELL = 66, (76, 60)
N = 64
PANEL_K = list(range(4, 13)) + list(range(52, 61))
PANEL_CELL = 64
PANEL_R, PANEL_HALF = 0.29, np.radians(64)
TILTS = (-20, -10, 0, 10, 20)
SPLITS = (0.12, 0.3, 0.5, 0.7, 0.86, 1.0)
GOLD = np.array((255, 196, 64), float)
GOLD_W = np.array((255, 236, 170), float)
WHITE = np.array((255, 255, 255), float)
RED = np.array((255, 48, 40), float)
TEAL = np.array((0, 255, 154), float)
LIME = np.array((190, 255, 60), float)
ORANGE = np.array((255, 120, 40), float)
SEED = 8001
L_WORLD = np.asarray(a6.KEY) * (4.4 / 2.3) + np.array((0.0, a6.sy(128), 0.0))   # one key light for every hull render


def sx(x):
    return a6.sx(x)


def sy(y):
    return a6.sy(y)


def off(x, y):
    """A screen point as an offset from the piece's centre (px, y down)."""
    return [round(float(x - CENTRE[0]), 2), round(float(y - CENTRE[1]), 2)]


def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def grid(w, h):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    return xx, yy


def blur(a, sigma):
    """A separable gaussian blur of a 2D float array (edges zero-padded)."""
    if sigma <= 0:
        return a
    r = int(np.ceil(sigma * 3))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, r)
    p = np.apply_along_axis(lambda v: np.convolve(v, k, mode="same"), 0, p)
    p = np.apply_along_axis(lambda v: np.convolve(v, k, mode="same"), 1, p)
    return p[r:-r, r:-r]


def additive_rgb(light):
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def gauss(xx, yy, x, y, r):
    return np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r))


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


def pivots_folder():
    return artkit.PIVOTS if bu.claimed(R) else ROOT / "build" / "art-held" / "pivots"


def write_pivots(data):
    folder = pivots_folder()
    folder.mkdir(parents=True, exist_ok=True)
    (folder / f"{R}.json").write_text(json.dumps({"source": SOURCE, **data}, indent=1) + "\n", encoding="utf-8")
    other = ROOT / "build" / "art-held" / "pivots" if folder == artkit.PIVOTS else artkit.PIVOTS
    if (other / f"{R}.json").exists():
        (other / f"{R}.json").unlink()
    print(f"pivots -> {(folder / f'{R}.json').relative_to(ROOT)}")


def load_pivots():
    for folder in (artkit.PIVOTS, ROOT / "build" / "art-held" / "pivots"):
        if (folder / f"{R}.json").exists():
            return json.loads((folder / f"{R}.json").read_text(encoding="utf-8"))
    raise FileNotFoundError(f"pivots/{R}.json")


def load(name):
    return l23kit.load(name)


# =========================================================================== models

def soot(p, n):
    """A blotchy darkening in model space (damaged and dead parts)."""
    x, y = p[:, 0], p[:, 1]
    v = np.sin(x * 23.1 + 1.3) * np.sin(y * 19.3 - 0.4) + 0.5 * np.sin(x * 41.0 + y * 37.0 + 2.1)
    return 0.5 + 0.5 * smoothstep(-0.7, 0.9, v)


def worn(mats, state):
    """``state`` 1 damaged (soot, glows halved), 2 dead (soot, darker, glows out)."""
    if state == 0:
        return mats
    out = []
    for m in mats:
        k = 0.85 if state == 1 else 0.6
        em = tuple(np.asarray(m.emission, float) * (0.4 if state == 1 else 0.0))
        out.append(replace(m, albedo=tuple(np.asarray(m.albedo, float) * k), spec=m.spec * (0.7 if state == 1 else 0.4),
                           emission=em, pattern=soot,
                           emission_pattern=m.emission_pattern if state == 1 else None))
    return out


def glow_mats(mats, slot, colour=(1.0, 1.0, 1.0)):
    """Every material black and unlit but ``slot``, which emits ``colour``: an emission mask of one paint."""
    out = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None, emission=(0.0, 0.0, 0.0),
                   emission_pattern=None) for m in mats]
    out[slot] = Material((0.0, 0.0, 0.0), emission=colour)
    return out


def surface_z(x):
    """The wedge's top at model x (sd_plate's roof taper)."""
    return 0.85 * np.clip(1.0 - 0.44 * np.abs(x), 0.1, 1.0)


def hull_scene(split=0.0, bay=0.0, rack=0.0, glow=1.0):
    """Round 72's ``hull_a`` (the gilded wedge: one smooth gloss-black wedge, gold ribs fanning down both faces, the
    gold spine, edge and crest, red running lights; at ``split`` the keel opened and the Vrell tissue swelling out of it
    and two breaches, the plates peeled outward), with the breaches at the simulation's side sacs, the bow's four gun
    ports, the two Dart bays (doors open by ``bay``) and the two shoulder racks (hatches open by ``rack``)."""
    mats = a6.asc_mats(glow)
    breaches = [(sx(x), sy(y)) for x, y in SACS[:2]]
    ports = [(sx(x), sy(y)) for x, y in PORTS if x > 240]
    bay_c = (sx(BAYS[1][0]), sy(BAYS[1][1]))
    rack_c = (sx(RACKS[1][0]), sy(RACKS[1][1]))
    bh = (BAY_HALF[0] / PPU, BAY_HALF[1] / PPU)
    rh = (RACK_HALF[0] / PPU, RACK_HALF[1] / PPU)

    def scene(p):
        q = mirror_x(p)
        X, Y = q[:, 0], q[:, 1]
        roof = lambda x, y: np.clip(1.0 - 0.44 * np.abs(x), 0.1, 1.0)  # noqa: E731
        d = sdf.sd_plate(q, a6.WEDGE, 0.0, 0.85, 0.03, taper=roof)
        recess = []
        for (cx, cy), (hx_, hy_), dz in ([(c, (0.05, 0.022), 0.04) for c in ports]
                                        + [(bay_c, bh, 0.07), (rack_c, rh, 0.06)]):
            zs = float(surface_z(cx))
            cut = sd_box(q, (cx, cy, zs), (hx_, hy_, dz))
            d = np.maximum(d, -cut)
            recess.append((cx, cy, hx_, hy_, zs))
        items = [(d, A_HULL)]
        for (cx, cy, hx_, hy_, zs) in recess:                          # gold frames round every opening
            ring = np.maximum(sd_box(q, (cx, cy, zs), (hx_ + 0.016, hy_ + 0.016, 0.012)),
                              -sd_box(q, (cx, cy, zs), (hx_, hy_, 0.05)))
            items.append((ring, A_GOLD))
        zb = float(surface_z(bay_c[0]))
        w = 0.5 * bh[0] * (1.0 - bay)
        if w > 0.002:                                                    # the bay's two leaves, sliding apart
            for s in (-1, 1):
                cxl = bay_c[0] + s * (bh[0] - w)
                items.append((sd_box(q, (cxl, bay_c[1], zb - 0.014), (w, bh[1] - 0.004, 0.01)), A_FACET))
        zr = float(surface_z(rack_c[0]))
        hgt = 0.5 * rh[1] * (1.0 - rack)
        for k in range(3):                                               # three SAM diamonds in the rack
            mx = rack_c[0] + (k - 1) * 0.055
            items.append((sd_ellipsoid(q, (mx, rack_c[1], zr - 0.035), (0.02, 0.045, 0.02)), A_GUN))
            items.append((sd_sphere(q, (mx, rack_c[1] - 0.04, zr - 0.03), 0.014), A_RED))
        if hgt > 0.002:                                                  # the hatch's leaves, sliding up and down
            for s in (-1, 1):
                cyl = rack_c[1] + s * (rh[1] - hgt)
                items.append((sd_box(q, (rack_c[0], cyl, zr - 0.012), (rh[0] - 0.004, hgt, 0.009)), A_FACET))
        crest = sd_cylinder_z(p, (0, sy(160), 0.85), 0.16, 0.04)
        items.append((crest, A_GOLD))
        for lx, ly in a6.LIGHTS_A:
            items.append((sd_sphere(q, (lx - 0.07, ly + 0.03, 0.85 * (1 - 0.44 * (lx - 0.07)) - 0.02), 0.03), A_RED))
        if split > 0:
            w = a6.split_w(Y, split)
            cut = np.where(Y > sy(250), np.abs(X) - w, 1.0)
            d0, m0 = union(*items)
            d0 = np.maximum(d0, -cut)
            for bx_, by_ in breaches:
                d0 = np.maximum(d0, -(np.hypot(p[:, 0] - bx_, p[:, 1] - by_) - 0.2 * split))
            items = [(d0, m0)]
            tis = np.full(len(p), 9.0)
            for k, yy in enumerate(np.linspace(sy(245), sy(-20), 11)):
                r = a6.split_w(yy, split)
                tis = sdf.smin(tis, sd_ellipsoid(p, (0.05 * np.sin(k * 2.3), yy, 0.55 + 0.25 * split),
                                                 (r * 1.2 + 0.04, 0.17, 0.2 * split + 0.08)), 0.1)
            for bx_, by_ in breaches:
                hz = 0.85 * (1 - 0.44 * abs(bx_))
                tis = sdf.smin(tis, sd_ellipsoid(p, (bx_, by_, hz), (0.2 * split, 0.18 * split, 0.14 * split)), 0.08)
            items.append((tis, a6.V_SAC))
            for k, yy in enumerate(np.linspace(sy(225), sy(5), 7)):          # plates peeled outward
                w0 = a6.split_w(yy, split)
                pc = rotate_y(q - np.array((w0 + 0.06, yy, 0.86 - 0.44 * w0)), -0.7 * split)
                items.append((sd_box(pc, (0, 0, 0), (0.08, 0.1, 0.014)), A_GOLD if k % 2 else A_FACET))
        d, m = union(*items)
        hullm = np.isin(m, (A_HULL, A_FACET))
        ang = np.arctan2(X, 3.4 - Y)
        rib = ((ang * 9.5) % 1.0) < 0.34
        m = np.where((m == A_HULL) & (np.floor(ang * 9.5) % 2 == 1) & (X > 0.16), A_FACET, m)
        m = np.where(np.isin(m, (A_HULL, A_FACET)) & rib & (X > 0.16), A_GOLD, m)
        edge = sdf.sd_polygon2(X, Y, a6.WEDGE) > -0.1
        m = np.where(hullm & edge, A_GOLD, m)
        m = np.where((m == A_HULL) & (X < 0.035), A_GOLD, m)
        band = (np.abs(Y - sy(100)) < 0.03) | (np.abs(Y - sy(200)) < 0.03)
        m = np.where((m == A_HULL) & band, A_FACET, m)
        for (cx, cy, hx_, hy_, zs) in recess:                          # the openings' floors in the dark
            inside = (np.abs(X - cx) < hx_) & (np.abs(Y - cy) < hy_) & (p[:, 2] < zs - 0.02)
            m = np.where(inside & np.isin(m, (A_HULL, A_FACET, A_GOLD)), A_GUN, m)
        return d, m
    return scene, mats


def cannon_base_scene(state):
    """``cannon_model``'s base and gold collar (fixed on the bow's tip); dead: the dome burst on it, no barrels."""
    def scene(p):
        base = sd_cylinder_z(p, (0, 0.0, 0.0), 0.24, 0.12)
        collar = np.maximum(sd_cylinder_z(p, (0, 0.0, 0.05), 0.27, 0.05), -sd_cylinder_z(p, (0, 0.0, 0.05), 0.22, 0.2))
        items = [(base, A_GUN), (collar, A_GOLD)]
        if state == 2:
            dome = sd_ellipsoid(p, (0.01, 0.01, 0.08), (0.19, 0.16, 0.09))
            hole = sd_ellipsoid(p, (0.03, -0.02, 0.16), (0.1, 0.08, 0.08))
            items.append((np.maximum(dome, -hole), A_HULL))
        return union(*items)
    return scene, worn(a6.asc_mats(), state)


def turret_scene():
    """``cannon_model``'s dome, twin barrels and red eye, nose +Y, about the base's centre (barrels 3 px shorter)."""
    def scene(p):
        q = mirror_x(p)
        dome = sd_ellipsoid(p, (0, -0.02, 0.1), (0.2, 0.17, 0.14))
        barrel = sd_box(q, (0.07, 0.165, 0.12), (0.035, 0.135, 0.04), 0.015)
        lens = sd_sphere(p, (0, 0.1, 0.2), 0.05)
        return union((dome, A_HULL), (barrel, A_GUN), (lens, A_RED))
    return scene, a6.asc_mats()


def generator_scene(state):
    """``generator_model``: lit (core 1.0), damaged (0.35, soot), dead (0, soot), grafted (dead, tissue in the mount)."""
    core = (1.0, 0.35, 0.0, 0.0)[state]
    scene0, mats = a6.generator_model(core)
    mats = worn(mats, min(state, 2)) if state else mats
    if state == 3:
        mats = list(mats)
        mats[a6.V_SEAM] = a6.asc_mats()[a6.V_SEAM]

        def scene(p):
            d, m = scene0(p)
            tend = 9.0
            for k, a in enumerate((0.5, 2.6, 4.4)):
                c, s = np.cos(a), np.sin(a)
                a0 = (c * 0.34, s * 0.34, 0.0)
                a1 = (c * 0.2 + 0.05 * s, s * 0.2 - 0.05 * c, 0.1)
                a2 = (c * 0.08, s * 0.08, 0.12)
                tend = np.minimum(tend, sd_capsule(p, a0, a1, 0.045, 0.035))
                tend = np.minimum(tend, sd_capsule(p, a1, a2, 0.035, 0.025))
            return union((d, m), (tend, a6.V_SEAM), k=0.02)
        return scene, mats
    return scene0, mats


def battery_scene(state):
    scene, mats = a6.battery_model()
    return scene, worn(mats, state)


CRACK = 17                                    # an 18th material slot: the panel's cracks


def panel_scene(kind):
    """A mirror panel round its battery, nose +Y = the way it faces: a crescent band of polished silver-gold sloping
    outward (so each heading catches the top-left light its own way), its two ends capped in gold. ``down``: the plate
    shattered to a jagged dark stub between the caps; ``cracks``: the plate with its cracks in slot ``CRACK``."""
    mats = a6.asc_mats() + [Material((0.9, 0.9, 0.9))]
    mats[A_GLASS] = Material((1.0, 0.93, 0.74), metal=0.55, shininess=160, spec=1.5)
    rng = np.random.default_rng(SEED + 1)
    cracks = [(rng.uniform(-0.9, 0.9) * PANEL_HALF, rng.uniform(0, TAU)) for _ in range(5)]

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        r = np.hypot(x, y)
        th = np.arctan2(x, y)
        rr = r - PANEL_R
        radial = np.abs(rr) - 0.045
        ang = (np.abs(th) - PANEL_HALF) * r
        top = 0.15 - 0.2 * rr
        caps = np.minimum(*[sd_box(rotate_z(p - np.array((PANEL_R * np.sin(s * PANEL_HALF),
                                                            PANEL_R * np.cos(s * PANEL_HALF), 0.09)), s * PANEL_HALF),
                                   (0, 0, 0), (0.05, 0.022, 0.06), 0.01) for s in (-1, 1)])
        if kind == "down":
            jag = 0.07 + 0.025 * np.abs(np.sin(th * 23.0)) + 0.015 * np.sin(th * 57.0 + 1.0)
            stub = np.maximum.reduce([radial, z - jag, 0.04 - z, ang]) / 1.3
            return union((stub, A_GUN), (caps, A_GOLD))
        plate = np.maximum.reduce([radial, z - top, 0.05 - z, ang]) / 1.3
        d, m = union((plate, A_GLASS), (caps, A_GOLD))
        if kind == "cracks":
            hit = np.zeros(len(p), bool)
            for t0, ph in cracks:
                tc = t0 + 0.06 * np.sin(rr * 90 + ph) + 0.03 * np.sin(rr * 230 + 2 * ph)
                hit |= np.abs(th - tc) * r < 0.0055
            m = np.where((m == A_GLASS) & hit, CRACK, m)
        return d, m
    return scene, mats


def sac_scene(swell, state=0):
    """``sac_model`` at ``swell``; state 1 split open (a jagged slit, the teal glow inside); 2 dead (a deflated husk)."""
    if state == 0:
        return a6.sac_model(swell)
    mats = a6.asc_mats()

    def scene(p):
        if state == 2:
            husk = sd_ellipsoid(p, (0, 0, 0.0), (0.2, 0.18, 0.07))
            hole = sd_ellipsoid(p, (0.02, -0.01, 0.07), (0.09, 0.07, 0.05))
            return union((np.maximum(husk, -hole), a6.V_DARK))
        s = swell
        body = sd_ellipsoid(p, (0, 0, 0.05), (0.19 * s, 0.18 * s, 0.15 * s))
        veins = sd_ellipsoid(p, (0, 0, 0.04), (0.22 * s, 0.21 * s, 0.1))
        slit_x = 0.025 * np.sin(p[:, 1] * 40.0) + 0.012 * np.sin(p[:, 1] * 97.0)
        slit = np.maximum(np.abs(p[:, 0] - slit_x) - 0.035, np.abs(p[:, 1]) - 0.17 * s)
        inner = sd_ellipsoid(p, (0, 0, 0.0), (0.12 * s, 0.14 * s, 0.1 * s))
        return union((np.maximum(veins, -slit), a6.V_DARK), (np.maximum(body, -slit), a6.V_SAC), (inner, a6.V_GLOW))
    return scene, mats


def lifeboat_scene(tilt):
    """``lifeboat_model`` a (nose up), leaning ``tilt`` degrees toward its weave (+ = right) with a matching roll."""
    scene0, mats = a6.lifeboat_model("a")
    a = np.radians(tilt)
    roll = -0.9 * a
    return (lambda p: scene0(rotate_y(rotate_z(p, -a), roll))), model_space_materials(mats, -a)


def plate_scene(kind, f, n=8):
    """A tumbling hull plate (gold-edged black) or a charred shard, frame ``f`` of a tumble of ``n``."""
    mats = a6.asc_mats()
    t = TAU * f / n

    def scene(p):
        q = rotate_x(rotate_z(p, 0.6 + t * 0.5), t)
        if kind == "plate":
            core = sd_box(q, (0, 0, 0), (0.05, 0.035, 0.008))
            rim = np.maximum(sd_box(q, (0, 0, 0), (0.062, 0.047, 0.01)), -sd_box(q, (0, 0, 0), (0.05, 0.035, 0.05)))
            return union((core, A_HULL), (rim, A_GOLD))
        poly = [(-0.04, -0.03), (0.035, -0.04), (0.045, 0.01), (0.0, 0.045), (-0.03, 0.02)]
        return union((sdf.sd_plate(q, poly, 0.0, 0.007, 0.002), A_FACET))
    return scene, worn(mats, 2) if kind == "shard" else mats


MODELS = {"cannon-base": cannon_base_scene, "turret": turret_scene, "generator": generator_scene,
          "battery": battery_scene, "panel": panel_scene, "sac": sac_scene, "heart": a6.heart_model,
          "lifeboat": lifeboat_scene, "plate": plate_scene}


# =========================================================================== jobs (cached by the model code's hash)

def _strip(args, box, factor, i, n):
    """Rows i of n of a hull render over the screen box (x0, y0, w, h), lit by the fixed ``L_WORLD``."""
    x0, y0, w, h = box
    scene, mats = hull_scene(*args)
    E = w / PPU
    r0, r1 = h * i // n, h * (i + 1) // n
    cs = np.array((sx(x0 + w / 2), sy(y0 + (r0 + r1) / 2)))
    kp = (L_WORLD - np.array((cs[0], cs[1], 0.0))) / (E / 2.3)
    return sdf.render(scene, mats, (w * factor, (r1 - r0) * factor), E, center=tuple(cs), z_top=2.0, steps=130,
                      key_pos=tuple(kp))


def _part(name, args, size, k, n):
    scene, mats = MODELS[name](*args)
    if k is not None:
        scene, mats = bu.turned(scene, mats, k, n)
    w, h = size
    return bu.render(scene, mats, (w, h), w / PPU)


def _mask(name, args, size, k, n, slot):
    """The coverage (0..1 floats) of one paint seen from above, occlusion included."""
    scene, mats = MODELS[name](*args)
    if k is not None:
        scene, mats = bu.turned(scene, mats, k, n)
    w, h = size
    hi, f = artkit.render_hi(scene, glow_mats(mats, slot), (w, h), w / PPU, shadows=False)
    m = hi[..., 0] * (hi[..., 3] > 0.5)
    return m.reshape(h, f, w, f).mean(axis=(1, 3))


JOBS = {"strip": _strip, "part": _part, "mask": _mask}


def _job(job):
    return JOBS[job[0]](*job[1:])


def _model_key():
    text = open(__file__, encoding="utf-8").read().split("# " + "=" * 75 + " jobs")[0]
    text += open(a6.__file__, encoding="utf-8").read()
    return hashlib.sha1(text.encode()).hexdigest()[:12]


def run(jobs):
    """Render the jobs in parallel, cached by the model code's hash (a strip or mask is a float array, a part an image)."""
    key = _model_key()
    paths = [CACHE / hashlib.sha1((key + repr(j)).encode()).hexdigest() for j in jobs]
    todo = [i for i, p in enumerate(paths)
            if not (p.with_suffix(".npy").exists() or p.with_suffix(".png").exists())]
    if todo:
        CACHE.mkdir(parents=True, exist_ok=True)
        with ProcessPoolExecutor(WORKERS) as pool:
            for i, res in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                if isinstance(res, np.ndarray):
                    np.save(paths[i].with_suffix(".npy"), res.astype(np.float32))
                else:
                    res.save(paths[i].with_suffix(".png"))
    out = []
    for p in paths:
        if p.with_suffix(".npy").exists():
            out.append(np.load(p.with_suffix(".npy")).astype(np.float64))
        else:
            out.append(Image.open(p.with_suffix(".png")).convert("RGBA"))
    return out


def region_jobs(args, box, factor=4, n=None):
    n = n or max(1, box[3] // 14)
    return [("strip", tuple(args), tuple(box), factor, i, n) for i in range(n)]


def regions(specs):
    """Hull renders: specs = [(args, box)] -> native unquantised RGBA images (1-bit alpha, unsharp mask)."""
    jobs, spans = [], []
    for args, box in specs:
        js = region_jobs(args, box)
        spans.append((len(jobs), len(jobs) + len(js)))
        jobs += js
    res = run(jobs)
    return [artkit.native(np.concatenate(res[a:b], axis=0), 4, crisp=70) for a, b in spans]


def to_px(lx, ly, k, cell, n=N):
    dx, dy = local_to_screen(lx, ly, np.pi / 2 + TAU * k / n, PPU)
    return [round(float(cell / 2 + dx), 2), round(float(cell / 2 + dy), 2)]


# =========================================================================== the hull

def elite_rim(img, keep_top=0):
    """enemies_r60.belt_rim(True) without its palette step: a 2 px gold rim on the edges facing the top-left sun
    (bright edge, softer inner row), red on the far ones; rows above ``keep_top`` (the cut at the frame's top) left."""
    a = np.array(img.convert("RGBA"))
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
    tl[:keep_top] = False
    br[:keep_top] = False
    out = a.astype(np.float64)
    out[tl, :3] = out[tl, :3] * 0.12 + u60.GOLD_HI * 0.88
    inner = op & ~tl & ~br & (np.roll(tl, 1, axis=1) | np.roll(tl, 1, axis=0))
    out[inner, :3] = out[inner, :3] * 0.55 + u60.GOLD_RIM * 0.45
    out[br, :3] = out[br, :3] * 0.45 + u60.RED_RIM * 0.55
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def shade_faces(img, box):
    """``act6_r72.shade_faces`` in absolute screen px: the left face lifted with a soft blue-grey sheen and a long
    specular streak as the top-left sun would light it, the right face a step darker (gold and lights untouched)."""
    a = np.array(img).astype(np.float64)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    X, Y = xx + box[0], yy + box[1]
    dark = (a[..., :3].max(-1) < 70) & (a[..., 3] > 0)
    left = X < 240
    k = np.where(left, 1.55 + 0.25 * np.exp(-((X - 150 - (Y - 60) * 0.55) / 26) ** 2), 0.8)
    lift = np.where(left, 1.0, 0.0)[..., None] * np.array((10, 12, 18.0))
    a[..., :3] = np.where(dark[..., None], a[..., :3] * k[..., None] + lift, a[..., :3])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def sub_box(box, inner):
    """``inner`` (x0, y0, w, h, screen px) as a crop box of a render over ``box``."""
    return (inner[0] - box[0], inner[1] - box[1], inner[0] - box[0] + inner[2], inner[1] - box[1] + inner[3])


def bay_box(i):
    x, y = BAYS[i]
    return (x - BAY_BOX[0] // 2, y - BAY_BOX[1] // 2, BAY_BOX[0], BAY_BOX[1])


def rack_box(i):
    x, y = RACKS[i]
    return (x - RACK_BOX[0] // 2, y - RACK_BOX[1] // 2, RACK_BOX[0], RACK_BOX[1])


def opening_mask(box, centre, half):
    """The pixels of an opening inside its gold frame (the part a door overlay draws)."""
    xx, yy = grid(box[2], box[3])
    return (np.abs(xx + box[0] - centre[0]) < half[0] + 0.5) & (np.abs(yy + box[1] - centre[1]) < half[1] + 0.5)


def build_hull():
    states = (0.0, 0.34, 0.67, 1.0)
    specs = [((0.0, 0.0, 0.0), HULL_BOX)]
    specs += [((0.0, s, 0.0), bay_box(i)) for i in range(2) for s in states]
    specs += [((0.0, 0.0, s), rack_box(i)) for i in range(2) for s in states]
    specs += [((0.0, 0.0, 0.0), KEEL_BOX)] + [((s, 0.0, 0.0), KEEL_BOX) for s in SPLITS]
    res = regions(specs)
    hull = elite_rim(shade_faces(res[0], HULL_BOX), keep_top=11)
    overlays = []
    for j, (args, box) in enumerate(specs[1:17]):
        i = (j // 4) % 2
        is_bay = j < 8
        centre, half = (BAYS[i], BAY_HALF) if is_bay else (RACKS[i], RACK_HALF)
        img = np.array(shade_faces(res[1 + j], box))
        img[~opening_mask(box, centre, half)] = 0
        overlays.append(Image.fromarray(img, "RGBA"))
    q = artkit.quantize_set([hull] + overlays, 48)
    save_set(f"{R}-hull", [q[0]], single=True)
    for i, side in enumerate("lr"):
        save_set(f"{R}-bay-{side}", q[1 + 4 * i:5 + 4 * i])
        save_set(f"{R}-rack-{side}", q[9 + 4 * i:13 + 4 * i])
    base = np.array(shade_faces(res[17], KEEL_BOX)).astype(np.int32)
    keel = []
    for j, s in enumerate(SPLITS):
        a = np.array(shade_faces(res[18 + j], KEEL_BOX)).astype(np.int32)
        diff = (np.abs(a - base).max(axis=-1) > 10) | ((a[..., 3] > 0) != (base[..., 3] > 0))
        m = np.array(Image.fromarray(diff.astype(np.uint8) * 255).filter(ImageFilter.MaxFilter(3))) > 0
        a[~m] = 0
        a[a[..., 3] == 0] = 0
        keel.append(Image.fromarray(a.astype(np.uint8), "RGBA"))
    save_set(f"{R}-keel", artkit.quantize_set(keel, 48))
    save_set(f"{R}-keel-cracks", keel_cracks())
    save_set(f"{R}-lamp-red", [lamp(RED, np.array((255, 150, 120.0)))], single=True)
    save_set(f"{R}-lamp-gold", [lamp(GOLD, GOLD_W)], single=True)
    save_set(f"{R}-port-flash", [port_flash(k) for k in (1.0, 0.7, 0.9, 0.55)])
    save_set(f"{R}-bay-light", [bay_light()], single=True)
    save_set(f"{R}-rack-lock", [lamp(RED, WHITE, r=2.2, halo=5.0)], single=True)
    write_pivots(pivot_data())


def keel_crack_line(seed=SEED + 7):
    pts = [(241, -12), (237, 40), (243, 100), (238, 160), (242, 215), (240, 262)]
    return a6.ragged_line(pts, seed, amp=3.0, step=3.0)


def keel_cracks():
    """The shudder's tell: a ragged crack down the keel with side branches, teal light with a soft halo; frame 0 the
    bow's third, 1 the whole keel, 2 flaring wider."""
    x0, y0, w, h = CRACK_BOX
    line = keel_crack_line()
    rng = np.random.default_rng(SEED + 8)
    branches = []
    for _ in range(9):
        j = int(rng.integers(5, len(line) - 5))
        bx, by = line[j]
        side = rng.choice((-1, 1))
        ln = rng.uniform(6, 16)
        branches.append(a6.ragged_line([(bx, by), (bx + side * ln, by + rng.uniform(-8, 8))], int(rng.integers(1_000_000)),
                                       amp=1.2, step=2.0))
    out = []
    for f, (reach, k, sig) in enumerate(((0.33, 0.7, 1.2), (1.0, 0.9, 1.4), (1.0, 1.0, 2.4))):
        lim = 262 - reach * 274
        m = Image.new("L", (w * 4, h * 4), 0)
        d = ImageDraw.Draw(m)
        for seg in [line] + branches:
            pts = [((x - x0) * 4, (y - y0) * 4) for x, y in seg if y >= lim]
            if len(pts) > 1:
                d.line(pts, fill=255, width=4 if seg is line else 3)
        core = np.array(m).astype(np.float64).reshape(h, 4, w, 4).mean(axis=(1, 3)) / 255
        light = core[..., None] * (TEAL * 0.6 + WHITE * 0.4) * k + blur(core, sig)[..., None] * TEAL * 1.6 * k
        out.append(additive_rgb(light))
    return out


def lamp(col, core, w=16, r=1.6, halo=4.2):
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    g0, g1 = gauss(xx, yy, c, c + 0.1, r), gauss(xx, yy, c, c + 0.1, halo)
    return additive_rgb(g0[..., None] * core + g1[..., None] * col * 0.75)


def port_flash(k, w=24):
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    dx, dy = xx - c, yy - c - 0.1
    star = np.exp(-(dx / 1.3) ** 2) * np.exp(-(np.abs(dy) / 7.0)) + np.exp(-(dy / 1.3) ** 2) * np.exp(-np.abs(dx) / 7.0)
    light = gauss(xx, yy, c, c + 0.1, 2.0)[..., None] * GOLD_W + gauss(xx, yy, c, c + 0.1, 6.0)[..., None] * GOLD * 0.6
    light += star[..., None] * GOLD * 0.5
    return additive_rgb(light * k)


def bay_light(w=40, h=32):
    xx, yy = grid(w, h)
    cx, cy = w / 2 - 0.2, h / 2 + 0.1
    inside = np.exp(-(((xx - cx) / (BAY_HALF[0] + 1)) ** 8 + ((yy - cy) / (BAY_HALF[1] + 1)) ** 8))
    light = inside[..., None] * GOLD * 0.55 + blur(inside, 3.0)[..., None] * GOLD * 0.5
    light += gauss(xx, yy, cx, cy, 4.0)[..., None] * GOLD_W * 0.5
    return additive_rgb(light)


# =========================================================================== the parts

def part_jobs():
    jobs = [("part", "cannon-base", (s,), BASE_CELL, None, N) for s in range(3)]
    jobs += [("part", "turret", (), (TURRET_CELL, TURRET_CELL), k, N) for k in range(N)]
    jobs += [("part", "generator", (s,), (56, 56), None, N) for s in range(4)]
    jobs += [("part", "battery", (s,), (52, 52), None, N) for s in range(3)]
    for kind in ("whole", "down"):
        jobs += [("part", "panel", (kind,), (PANEL_CELL, PANEL_CELL), k, N) for k in PANEL_K]
    jobs += [("mask", "panel", ("whole",), (PANEL_CELL, PANEL_CELL), k, N, A_GLASS) for k in PANEL_K]
    jobs += [("mask", "panel", ("cracks",), (PANEL_CELL, PANEL_CELL), k, N, CRACK) for k in PANEL_K]
    jobs += [("part", "sac", (s, 0), (60, 60), None, N) for s in (1.0, 1.08, 1.16, 1.24)]
    jobs += [("part", "sac", (1.24, 1), (60, 60), None, N), ("part", "sac", (1.0, 2), (60, 60), None, N)]
    jobs += [("part", "heart", (o,), (68, 68), None, N) for o in (0.0, 0.35, 0.7, 1.0)]
    jobs += [("part", "lifeboat", (t,), (72, 88), None, N) for t in TILTS]
    jobs += [("part", "plate", ("plate", f), (16, 16), None, N) for f in range(8)]
    jobs += [("part", "plate", ("shard", f), (12, 12), None, N) for f in range(8)]
    return jobs


def build_parts():
    res = run(part_jobs())
    i = 0

    def take(n):
        nonlocal i
        i += n
        return res[i - n:i]
    nk = len(PANEL_K)
    save_set(f"{R}-cannon-base", artkit.quantize_set([elite_rim(f) for f in take(3)], 40))
    save_set(f"{R}-cannon", artkit.quantize_set([elite_rim(f) for f in take(N)], 40))
    save_set(f"{R}-generator", artkit.quantize_set([elite_rim(f) for f in take(4)], 40))
    save_set(f"{R}-battery", artkit.quantize_set([elite_rim(f) for f in take(3)], 36))
    save_set(f"{R}-panel", artkit.quantize_set(take(nk), 40))
    save_set(f"{R}-panel-down", artkit.quantize_set(take(nk), 24))
    plate_m, crack_m = take(nk), take(nk)
    save_set(f"{R}-panel-glow", [additive_rgb((m[..., None] * 0.85 + blur(m, 1.0)[..., None] * 0.55) * WHITE)
                                 for m in plate_m])
    save_set(f"{R}-panel-cracks", [cracks_img(m) for m in crack_m])
    save_set(f"{R}-panel-rim", [rim_glow(m) for m in plate_m])
    save_set(f"{R}-panel-sweep", [sweep_glint()], single=True)
    sacs = take(6)
    save_set(f"{R}-sac", artkit.quantize_set(sacs[:5], 40))
    save_set(f"{R}-sac-dead", artkit.quantize_set([sacs[5]], 24), single=True)
    save_set(f"{R}-heart", artkit.quantize_set(take(4), 44))
    save_set(f"{R}-lifeboat", artkit.quantize_set(take(len(TILTS)), 48))
    save_set(f"{R}-plate", artkit.quantize_set(take(8), 24))
    save_set(f"{R}-shard", artkit.quantize_set(take(8), 16))
    save_set(f"{R}-sac-core", [core_glow(44, 6.5)], single=True)
    save_set(f"{R}-heart-core", [core_glow(64, 10.0)], single=True)
    save_set(f"{R}-sac-heal", [membrane_glow(sacs[0], TEAL, 1.0)], single=True)
    save_set(f"{R}-sac-flare", [membrane_glow(sacs[3], TEAL * 0.6 + WHITE * 0.4, 1.4)], single=True)
    hearts = load(f"{R}-heart")
    save_set(f"{R}-heart-heal", [membrane_glow(hearts[0], TEAL, 1.0)], single=True)
    save_set(f"{R}-generator-glow", [generator_glow()], single=True)
    save_set(f"{R}-beam", [beam(f) for f in range(4)])
    save_set(f"{R}-muzzle", [muzzle(i) for i in range(6)])
    write_pivots(pivot_data())


def cracks_img(m):
    a = np.clip(m * 1.4, 0, 1)
    out = np.zeros(m.shape + (4,), np.uint8)
    out[..., :3] = 255
    out[..., 3] = np.round(a * 255).astype(np.uint8)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


def rim_glow(m):
    """The plate's outline glowing red: its edge (the coverage minus its erosion) with a soft halo."""
    solid = m > 0.5
    pad = np.pad(solid, 1)
    inner = solid & pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:]
    edge = (solid & ~inner).astype(np.float64)
    light = edge[..., None] * np.array((255, 110, 80.0)) * 0.8 + blur(edge, 1.6)[..., None] * RED * 1.5
    light += m[..., None] * RED * 0.18
    return additive_rgb(light)


def sweep_glint(w=24):
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    light = gauss(xx, yy, c, c + 0.1, 2.0)[..., None] * WHITE * 0.9
    light += gauss(xx, yy, c, c + 0.1, 5.5)[..., None] * np.array((255, 236, 190.0)) * 0.55
    return additive_rgb(light)


def core_glow(w, r):
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    light = gauss(xx, yy, c, c + 0.1, r * 0.45)[..., None] * (LIME * 0.6 + WHITE * 0.4)
    light += gauss(xx, yy, c, c + 0.1, r)[..., None] * LIME * 0.8
    return additive_rgb(light)


def membrane_glow(frame, col, k):
    """A healing pulse (or the brood's flare): teal over the membrane, strongest at its rim, with a soft halo."""
    a = np.array(frame)[..., 3] > 0
    m = a.astype(np.float64)
    pad = np.pad(a, 2)
    inner = a & pad[:-4, 2:-2] & pad[4:, 2:-2] & pad[2:-2, :-4] & pad[2:-2, 4:]
    edge = blur((a & ~inner).astype(np.float64), 0.9)
    halo = np.clip(blur(m, 3.0) - m, 0, 1)
    light = (edge * 0.55 + m * 0.06 + halo * 0.7)[..., None] * col * k
    return additive_rgb(light)


def generator_glow(w=48):
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    d = np.hypot(xx - c, yy - c - 0.1)
    light = np.exp(-((d - 13) / 4.0) ** 2)[..., None] * GOLD * 0.55 + gauss(xx, yy, c, c + 0.1, 9)[..., None] * GOLD_W * 0.5
    return additive_rgb(light)


def beam(f, w=24, h=32):
    """The generator's gold beam, tiling along y: a white-gold core, a gold halo, a pulse running down."""
    xx, yy = grid(w, h)
    c = w / 2
    pulse = 0.82 + 0.18 * np.sin(TAU * (yy / h - f / 4))
    light = (np.exp(-((xx - c) / 1.3) ** 2)[..., None] * GOLD_W * 0.9
             + np.exp(-((xx - c) / 4.5) ** 2)[..., None] * GOLD * 0.55) * pulse[..., None]
    return additive_rgb(light)


def muzzle(i, w=40):
    p = (i + 1) / 6
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    dx, dy = xx - c, yy - c - 0.1
    light = gauss(xx, yy, c, c + 0.1, 1.5 + 3.0 * p)[..., None] * WHITE * (0.4 + 0.6 * p)
    light += gauss(xx, yy, c, c + 0.1, 5 + 9 * p)[..., None] * GOLD * (0.25 + 0.55 * p)
    star = (np.exp(-(dx / 1.2) ** 2) * np.exp(-np.abs(dy) / (3 + 8 * p))
            + np.exp(-(dy / 1.2) ** 2) * np.exp(-np.abs(dx) / (3 + 8 * p)))
    light += star[..., None] * GOLD_W * 0.5 * p
    return additive_rgb(light)


# =========================================================================== effects and the break

def build_fx():
    save_set(f"{R}-heart-burst", [burst(f, 8, 128, 54, SEED + 20) for f in range(8)])
    save_set(f"{R}-sac-burst", [burst(f, 6, 64, 26, SEED + 21) for f in range(6)])
    save_set(f"{R}-vent-orange", [vent(f, ORANGE, np.array((255, 200, 120.0)), SEED + 30) for f in range(6)])
    save_set(f"{R}-vent-teal", [vent(f, TEAL, np.array((170, 255, 220.0)), SEED + 31) for f in range(6)])
    save_set(f"{R}-fire", [fire(f) for f in range(6)])
    save_set(f"{R}-lifeboat-drive", [drive(k) for k in (1.0, 0.82, 0.94, 0.76)])
    save_set(f"{R}-lifeboat-wake", [wake_puff(a) for a in range(6)])
    save_set(f"{R}-lifeboat-tear", [burst(f, 8, 112, 46, SEED + 40, fibres=22) for f in range(8)])
    save_set(f"{R}-lifeboat-flash", [gate_flash(f) for f in range(6)])
    build_wreck()
    write_pivots(pivot_data())


def burst(f, n, w, reach, seed, fibres=16):
    """A Vrell burst: a lime-white flash swelling and fading, a thin teal ring running out, torn teal fibres flung."""
    t = (f + 0.5) / n
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    d = np.hypot(xx - c, yy - c - 0.1)
    ang = np.arctan2(yy - c, xx - c)
    k = np.exp(-3.2 * t) * min(1.0, 3 * t + 0.3)
    light = gauss(xx, yy, c, c + 0.1, reach * (0.18 + 0.3 * t))[..., None] * (WHITE * 0.5 + LIME * 0.5) * 1.3 * k
    light += gauss(xx, yy, c, c + 0.1, reach * (0.35 + 0.4 * t))[..., None] * LIME * 0.7 * k
    ring = np.exp(-((d - reach * (0.25 + 0.75 * t)) / (1.6 + 2.5 * t)) ** 2) * (1 - t) ** 1.2
    light += ring[..., None] * TEAL * 0.9
    rng = np.random.default_rng(seed)
    for _ in range(fibres):
        a0 = rng.uniform(0, TAU)
        r0 = reach * (0.2 + 0.8 * t) * rng.uniform(0.6, 1.05)
        ln = reach * rng.uniform(0.12, 0.28)
        da = (ang - a0 + np.pi) % TAU - np.pi
        along = np.exp(-((d - r0) / ln) ** 2)
        across = np.exp(-(da * d / 1.1) ** 2)
        light += (along * across)[..., None] * TEAL * 1.1 * (1 - t) ** 0.8
    return additive_rgb(light)


def vent(f, col, core, seed, w=24, h=56):
    """Gas venting up the frame from its bottom centre: widening, fading puffs, turbulent per frame."""
    xx, yy = grid(w, h)
    rng = np.random.default_rng(seed + f)
    light = np.zeros((h, w, 3))
    for j in range(16):
        t = j / 15
        x = w / 2 + rng.normal(0, 0.6 + 2.2 * t)
        y = h - 4 - t * (h - 10)
        r = 1.8 + 6.5 * t
        k = (1 - t) ** 1.5 * rng.uniform(0.7, 1.0)
        light += gauss(xx, yy, x, y, r)[..., None] * (col * 0.6 * k)
    light += gauss(xx, yy, w / 2, h - 4, 2.2)[..., None] * core * 0.8
    return additive_rgb(light)


def fire(f, w=24):
    xx, yy = grid(w, w)
    rng = np.random.default_rng(SEED + 50 + f)
    c = w / 2
    light = gauss(xx, yy, c, c, 2.4 + rng.uniform(-0.3, 0.3))[..., None] * np.array((255, 230, 150.0)) * 0.9
    for _ in range(5):
        x, y = c + rng.normal(0, 2.2), c + rng.normal(0, 2.2)
        light += gauss(xx, yy, x, y, rng.uniform(2.5, 4.5))[..., None] * ORANGE * rng.uniform(0.35, 0.6)
    return additive_rgb(light)


def drive(k, w=32):
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    light = gauss(xx, yy, c, c + 0.1, 2.4)[..., None] * (WHITE * 0.5 + LIME * 0.5)
    light += np.exp(-(((xx - c) / 4.0) ** 2 + ((yy - c - 3) / 8.0) ** 2))[..., None] * TEAL * 0.7
    return additive_rgb(light * k)


def wake_puff(age, w=28):
    t = (age + 0.5) / 6
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    light = gauss(xx, yy, c, c + 0.1, 3 + 7 * t)[..., None] * TEAL * 0.6 * (1 - t) ** 1.3
    return additive_rgb(light)


def gate_flash(f, w=96):
    t = (f + 0.5) / 6
    k = np.exp(-((t - 0.25) / 0.22) ** 2)
    xx, yy = grid(w, w)
    c = w / 2 - 0.2
    dx, dy = xx - c, yy - c - 0.1
    star = (np.exp(-(dx / 1.6) ** 2) * np.exp(-np.abs(dy) / 18.0) + np.exp(-(dy / 1.6) ** 2) * np.exp(-np.abs(dx) / 18.0))
    light = gauss(xx, yy, c, c + 0.1, 4.0)[..., None] * WHITE + gauss(xx, yy, c, c + 0.1, 14.0)[..., None] * GOLD * 0.7
    light += star[..., None] * GOLD_W * 0.8
    return additive_rgb(light * k * 1.2)


def phase2_look():
    """The phase-2 hull at rest, in its frame: the hull, the keel's last frame, the sacs dead, the heart's cavity torn
    (K), as the wreck's halves start from."""
    hull = load(f"{R}-hull")[0].copy()
    hull.alpha_composite(load(f"{R}-keel")[5], (KEEL_BOX[0] - HULL_BOX[0], 0))
    dead = load(f"{R}-sac-dead")[0]
    for x, y in SACS:
        sprite.paste_center(hull, dead, x - HULL_BOX[0], y - HULL_BOX[1])
    heart = load(f"{R}-heart")[3]
    a = np.array(heart).astype(np.float64)
    a[..., :3] *= 0.45
    sprite.paste_center(hull, Image.fromarray(a.astype(np.uint8), "RGBA"), HEART[0] - HULL_BOX[0], HEART[1] - HULL_BOX[1])
    sprite.paste_center(hull, dead, LIFEBOAT[0] - HULL_BOX[0], LIFEBOAT[1] - HULL_BOX[1] - 4)
    return hull


def fracture():
    """Round 73's authored break (ASC_BREAK, seed 7301) in the hull frame's px."""
    pts = [(x - HULL_BOX[0], y - HULL_BOX[1]) for x, y in a6.ASC_BREAK]
    pts = [(pts[0][0], -4)] + pts[1:-1] + [(pts[-1][0], HULL_BOX[3] + 4)]
    return a6.ragged_line(pts, 7301, amp=5.0)


def build_wreck():
    """The halves: the phase-2 hull darkened as its lights die, split along the jagged fracture by round 73's
    ``torn_half`` (charred edge, lit lips, torn flaps bent outward across the break; its hot glow kept out of the body
    and drawn as the additive rim), each cropped to its pixels."""
    look = phase2_look()
    a = np.array(look).astype(np.float64)
    a[..., :3] *= 0.62
    look = Image.fromarray(a.astype(np.uint8), "RGBA")
    line = fracture()
    lm, rm = a6.ragged_masks(look.size, line)
    halves, rims, offs = [], [], []
    for keep, side, seed in ((lm, -1, 7301), (rm, 1, 7351)):
        half = a6.torn_half(look, keep, line, seed, side, hot=(0, 0, 0))
        arr = np.array(half)
        ys, xs = np.nonzero(arr[..., 3] > 0)
        x0, y0, x1, y1 = xs.min(), ys.min(), xs.max() + 1, ys.max() + 1
        x0, x1 = max(0, x0 - 2), min(look.width, x1 + 2)
        halves.append(half.crop((x0, y0, x1, y1)))
        rims.append(hot_rim(arr, keep, seed).crop((x0, y0, x1, y1)))
        offs.append((int(x0), int(y0)))
    q = artkit.quantize_set(halves, 48)
    for i, side in enumerate("lr"):
        save_set(f"{R}-wreck-{side}", [q[i]], single=True)
        save_set(f"{R}-wreck-{side}-rim", [rims[i]], single=True)
    CACHE.mkdir(parents=True, exist_ok=True)
    (CACHE / "wreck-offsets.json").write_text(json.dumps(offs))


def hot_rim(arr, keep, seed, band=6):
    """``torn_half``'s hot glow as light: the very edge glowing orange in burning runs, a soft halo round it."""
    h, w = keep.shape
    dist = a6._grow(~keep, band)
    solid = arr[..., 3] > 40
    edge = solid & (dist <= band)
    burn = raster.fbm(w, h, 8, seed + 11, octaves=3, period=False)
    glow = edge & (dist <= 2)
    g = np.zeros((h, w))
    g[glow] = (smoothstep(0.45, 0.75, burn) * (1.0 - (dist - 1) / 2.0))[glow]
    light = g[..., None] * np.array((255, 170, 90.0)) * 0.9 + blur(g, 1.8)[..., None] * ORANGE * 1.4
    return additive_rgb(light)


# =========================================================================== pivots

def wreck_points(offs):
    """The fracture on each half (half-frame px), and the vents and fires along it."""
    line = fracture()
    rng = np.random.default_rng(SEED + 60)
    out = {}
    for i, (side, sgn) in enumerate((("left", 1), ("right", -1))):
        ox, oy = offs[i]
        pts = [[round(float(x - ox + (-1.5 if sgn > 0 else 1.5)), 1), round(float(y - oy), 1)] for x, y in line[::4]]
        along = [p for p in pts if 70 < p[1] + oy < 270]
        vents = []
        for j, p in enumerate(rng.choice(len(along), 6, replace=False)):
            x, y = along[p]
            deg = float(np.degrees(np.arctan2(sgn * rng.uniform(0.6, 1.0), -rng.uniform(-0.9, 0.6))))
            vents.append([x, y, round(deg, 1), "teal" if j % 3 == 0 else "orange"])
        fires = []
        for x, y in (SACS[i], (150 + 180 * i, 190), (90 + 300 * i, 120)):
            fires.append([round(float(x - HULL_BOX[0] - ox), 1), round(float(y - HULL_BOX[1] - oy), 1)])
        out[side] = {"offset": [ox, oy], "fracture": pts, "vents": vents, "fires": fires,
                     "drift": [-250 if sgn > 0 else 250, 140]}
    return out


def chain_schedule(seed=SEED + 70):
    """K's 3 s chain down the keel: (t s, dx, dy from the centre, size), large at the start and the end."""
    rng = np.random.default_rng(seed)
    out = []
    for j in range(12):
        t = round(j * 0.25 + rng.uniform(0, 0.12), 2)
        y = 245 - j * 22 + rng.uniform(-6, 6)
        x = 240 + rng.uniform(-30, 30) * (0.5 + y / 300)
        size = "large" if j in (0, 5, 11) else "medium"
        out.append({"t": t, "dx": round(float(x - CENTRE[0]), 1), "dy": round(float(y - CENTRE[1]), 1), "size": size})
    return out


def pivot_data():
    tc = TURRET_CELL
    panels = []
    for i, k in enumerate(PANEL_K):
        tips = [to_px(s * PANEL_R * np.sin(PANEL_HALF), PANEL_R * np.cos(PANEL_HALF), k, PANEL_CELL) for s in (-1, 1)]
        facing = (360 * k / N + 180) % 360 - 180
        panels.append({"heading": k, "facing": round(facing, 3), "mid": to_px(0, PANEL_R, k, PANEL_CELL),
                       "tips": tips, "radius": round(PANEL_R * PPU, 1)})
    lamps = []
    for lx, ly in a6.LIGHTS_A:
        x, y = (lx - 0.07) * PPU, 150 - (ly + 0.03) * PPU
        lamps += [off(240 - x, y), off(240 + x, y)]
    offs = json.loads((CACHE / "wreck-offsets.json").read_text()) if (CACHE / "wreck-offsets.json").exists() else None
    tilt_pts = {"drive": [], "canopy": []}
    for t in TILTS:
        a = np.radians(t)
        for key, (lx, ly) in (("drive", (0.0, -0.37)), ("canopy", (0.04, 0.17))):
            rx, ry = lx * np.cos(a) + ly * np.sin(a), -lx * np.sin(a) + ly * np.cos(a)
            tilt_pts[key].append([round(36 + rx * PPU, 2), round(44 - ry * PPU, 2)])
    data = {
        "screen": "the spec's 480x540 field, y down; every place an offset [dx, dy] px from the piece's centre",
        "centre": list(CENTRE),
        "hull": {"frame": [HULL_BOX[2], HULL_BOX[3]], "anchor": [CENTRE[0] - HULL_BOX[0], CENTRE[1] - HULL_BOX[1]],
                 "note": "the frame px drawn on the piece's centre"},
        "keel": {"offset": [KEEL_BOX[0] - HULL_BOX[0], 0], "frames": len(SPLITS), "seconds": 3.0, "tell": 1.0,
                 "cracks_offset": [CRACK_BOX[0] - HULL_BOX[0], 0]},
        "parts": {"cannon": off(*CANNON), "gen-l": off(*GENS[0]), "gen-r": off(*GENS[1]),
                  "bat-1": off(*BATT[0][:2]), "bat-2": off(*BATT[1][:2]), "bat-3": off(*BATT[2][:2]),
                  "bat-4": off(*BATT[3][:2]), "sac-1": off(*SACS[0]), "sac-2": off(*SACS[1]), "sac-3": off(*SACS[2]),
                  "heart": off(*HEART), "lifeboat": off(*LIFEBOAT)},
        "cannon": {"headings": N, "cell": tc, "muzzle": [to_px(0, 0.30, k, tc) for k in range(N)],
                   "base_states": ["whole", "damaged", "dead"]},
        "panels": {"headings": PANEL_K, "left": list(range(9)), "right": list(range(9, 18)),
                   "frames": panels, "sweep_seconds": 2.5, "states": ["whole", "heating", "cracked", "down"]},
        "generator_states": ["lit", "damaged", "dead", "grafted"],
        "battery_states": ["whole", "damaged", "dead"],
        "beams": [{"from": off(*g), "to": off(*CANNON)} for g in GENS],
        "lamps": {"red": lamps, "gold": [off(240, y) for y in (212, 130, 104, 78, 52, 26, 2)],
                  "slide_in": 6.0},
        "ports": [off(*p) for p in PORTS],
        "bays": [off(*b) for b in BAYS],
        "racks": [off(*r) for r in RACKS],
        "rack_lock": [13, 4],
        "sac_frames": ["rest", "swell", "swell", "swell", "split"],
        "heart_frames": ["sealed", "opening", "opening", "open"],
        "chain": chain_schedule(),
        "break": {"flash": 2.5, "breakout": 3.0, "plates": 18},
        "lifeboat": {"tilts": list(TILTS), "drive": tilt_pts["drive"], "canopy": tilt_pts["canopy"],
                     "wake_seconds": 0.6},
    }
    if offs:
        data["wreck"] = {"seconds": 14.0, "frame_origin": "the hull frame's top left",
                         **wreck_points(offs)}
    return data


# =========================================================================== intel

def phase1_layer(dy=0, dead=(), panels=None, base_state=0, heading=0, lamps=True, glows=True):
    """The whole phase-1 boss at its arena place on a 480x540 layer (normal sprites), and its additive lights."""
    lay = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    light = Image.new("RGBA", (FW, FH), (0, 0, 0, 255))
    lay.alpha_composite(load(f"{R}-hull")[0], (HULL_BOX[0], HULL_BOX[1] + dy))
    gens = load(f"{R}-generator")
    for i, (x, y) in enumerate(GENS):
        sprite.paste_center(lay, gens[2 if f"gen{i}" in dead else 0], x, y + dy)
    bats, pan = load(f"{R}-battery"), load(f"{R}-panel")
    for i, (x, y, s) in enumerate(BATT):
        sprite.paste_center(lay, bats[2 if f"bat{i}" in dead else 0], x, y + dy)
        if f"bat{i}" not in dead:
            j = (panels or {}).get(i, 4 + (0 if s < 0 else 9))
            sprite.paste_center(lay, pan[j], x, y + dy)
    sprite.paste_center(lay, load(f"{R}-cannon-base")[base_state], CANNON[0], CANNON[1] + dy)
    if base_state < 2:
        sprite.paste_center(lay, load(f"{R}-cannon")[heading], CANNON[0], CANNON[1] + dy)
    return lay


def intel_mask():
    """The bow-on wedge with its generators, batteries, panels and cannon, as phase 1 shows it (intel.py's
    ``BOSSES``); the hull's top rows off the screen left out."""
    return np.array(phase1_layer())[..., 3] > 127


def build_intel():
    import intel  # noqa: E402  (registers boss-ascendant; imported late: it imports this module)
    intel.build(["boss-ascendant"])


def review_intel():
    import intel  # noqa: E402
    intel.review(["boss-ascendant"])


# =========================================================================== review

_BG = {}


def bg():
    if "l42" not in _BG:
        _BG["l42"] = a6.l42()
    return _BG["l42"].copy()


def addc(dst, img, x, y, k=1.0):
    """Add an additive frame centred at (x, y), scaled by k (the game's colour)."""
    if k <= 0:
        return dst
    a = np.array(img).astype(np.float64)
    a[..., :3] *= k
    layer = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return artkit.add_light(dst, layer, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))


def addtl(dst, img, x, y, k=1.0):
    return addc(dst, img, x + img.width / 2, y + img.height / 2, k)


def tinted_glow(img, col, k=1.0):
    a = np.array(img).astype(np.float64)
    a[..., :3] = a[..., :3] / 255 * np.asarray(col, float) * k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def heat_colour(h):
    """MirrorLooks' heat ladder as the review shows it: dull red, orange, white."""
    stops = [(0.0, (90, 20, 10)), (0.5, (255, 110, 30)), (1.0, (255, 250, 230))]
    for (a, ca), (b, cb) in zip(stops, stops[1:]):
        if h <= b:
            t = (h - a) / (b - a)
            return tuple(np.array(ca) * (1 - t) + np.array(cb) * t)
    return stops[-1][1]


def beam_line(img, x0, y0, x1, y1, f, k=1.0):
    """The generator's beam drawn as the game would: the strip tiled along the line (no rotation of a lit sprite; the
    review draws the strip's profile along the line)."""
    strip = np.array(load(f"{R}-beam")[f]).astype(np.float64)[..., :3]
    prof = strip[:, :, :].mean(axis=0)
    xx, yy = grid(FW, FH)
    dx, dy = x1 - x0, y1 - y0
    L = np.hypot(dx, dy)
    t = ((xx - x0) * dx + (yy - y0) * dy) / L ** 2
    dist = ((xx - x0) * dy - (yy - y0) * dx) / L
    inside = (t > 0) & (t < 1) & (np.abs(dist) < 12)
    idx = np.clip(np.round(dist + 12).astype(int), 0, 23)
    pulse = 0.82 + 0.18 * np.sin(TAU * (t * L / 32 - f / 4))
    light = prof[idx] * (inside * pulse * k)[..., None]
    a = np.array(img).astype(np.float64)
    a[..., :3] = np.minimum(255, a[..., :3] + light)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def ship(img, x, y):
    sprite.paste_center(img, l23kit.load("ship")[2], x, y)
    return img


def lamp_lights(img, dy=0, upto=1.0):
    piv = load_pivots()
    red, gold = load(f"{R}-lamp-red")[0], load(f"{R}-lamp-gold")[0]
    n = len(piv["lamps"]["red"])
    for j, (ox, oy) in enumerate(piv["lamps"]["red"]):
        if j / n < upto:
            img = addc(img, red, CENTRE[0] + ox, CENTRE[1] + oy + dy, 0.8)
    n = len(piv["lamps"]["gold"])
    for j, (ox, oy) in enumerate(piv["lamps"]["gold"]):
        if j / n < upto:
            img = addc(img, gold, CENTRE[0] + ox, CENTRE[1] + oy + dy, 0.7)
    return img


def heading_to(x0, y0, x1, y1):
    """The 64-heading index facing from (x0, y0) toward (x1, y1) (0 = down, clockwise)."""
    a = np.arctan2(y1 - y0, x1 - x0) - np.pi / 2
    return int(round(a / TAU * N)) % N


def panel_index(i, tx, ty):
    """The panel frame of battery i turned toward (tx, ty) within its +-20 degrees."""
    x, y, s = BATT[i]
    want = (np.degrees(np.arctan2(ty - y, tx - x)) - 90)
    base = 45 if s < 0 else -45
    turn = np.clip(((want - base + 180) % 360) - 180, -20, 20)
    k = int(round((base + turn) / 360 * N)) % N
    return PANEL_K.index(min(PANEL_K, key=lambda kk: min(abs(kk - k), N - abs(kk - k))))


def phase1_scene(t, ship_xy, heat=None, rim=None, down=(), dead=(), lance=0.0, ports=0.0, bays=0, racks=0,
                 sweep_t=0.0, dy=0, lamps_up=1.0, shield=False):
    img = bg()
    piv = load_pivots()
    panels = {i: panel_index(i, *ship_xy) for i in range(4)}
    hd = heading_to(CANNON[0], CANNON[1] + dy, *ship_xy)
    lay = phase1_layer(dy, dead=dead, panels=panels, heading=hd)
    downs = load(f"{R}-panel-down")
    for i in down:
        x, y, s = BATT[i]
        sprite.paste_center(lay, load(f"{R}-battery")[0], x, y + dy)
        sprite.paste_center(lay, downs[panels[i]], x, y + dy)
    if bays:
        for i, side in enumerate("lr"):
            sprite.paste_center(lay, load(f"{R}-bay-{side}")[bays], BAYS[i][0], BAYS[i][1] + dy)
    if racks:
        for i, side in enumerate("lr"):
            if f"gen{i}" not in dead:
                sprite.paste_center(lay, load(f"{R}-rack-{side}")[racks], RACKS[i][0], RACKS[i][1] + dy)
    img.alpha_composite(lay)
    for i, g in enumerate(GENS):
        if f"gen{i}" not in dead:
            img = beam_line(img, g[0], g[1] + dy, CANNON[0], CANNON[1] + dy, int(t * 8) % 4, 0.8 * lamps_up)
    img = lamp_lights(img, dy, lamps_up)
    for i, (x, y) in enumerate(GENS):
        if f"gen{i}" not in dead:
            img = addc(img, load(f"{R}-generator-glow")[0], x, y + dy, 0.8 + 0.2 * np.sin(t * 5))
    for i, (x, y, s) in enumerate(BATT):
        if i in down or f"bat{i}" in dead:
            continue
        j = panels[i]
        if heat and i in heat:
            img = addc(img, tinted_glow(load(f"{R}-panel-glow")[j], heat_colour(heat[i]), 0.55 + 0.45 * heat[i]),
                       x, y + dy)
            if heat[i] > 0.7:
                cr = np.array(load(f"{R}-panel-cracks")[j]).astype(np.float64)
                cr[..., :3] = 40
                cr[..., 3] *= min(1, (heat[i] - 0.7) / 0.3)
                sprite.paste_center(img, Image.fromarray(cr.astype(np.uint8), "RGBA"), x, y + dy)
        if rim and i in rim:
            img = addc(img, load(f"{R}-panel-rim")[j], x, y + dy, rim[i])
        fr = piv["panels"]["frames"][j]
        cx, cy = PANEL_CELL / 2, PANEL_CELL / 2
        a0 = np.arctan2(fr["tips"][0][1] - cy, fr["tips"][0][0] - cx)
        a1 = np.arctan2(fr["tips"][1][1] - cy, fr["tips"][1][0] - cx)
        da = (a1 - a0 + np.pi) % TAU - np.pi
        u = ((sweep_t + i * 0.6) / 2.5) % 1.0
        ang = a0 + da * u
        img = addc(img, load(f"{R}-panel-sweep")[0], x + np.cos(ang) * fr["radius"], y + dy + np.sin(ang) * fr["radius"],
                   0.45)
    if lance > 0:
        mz = piv["cannon"]["muzzle"][hd]
        img = addc(img, load(f"{R}-muzzle")[min(5, int(lance * 6))], CANNON[0] - TURRET_CELL / 2 + mz[0],
                   CANNON[1] - TURRET_CELL / 2 + mz[1] + dy)
    if ports > 0:
        for x, y in PORTS:
            img = addc(img, load(f"{R}-port-flash")[int(t * 10) % 4], x, y + dy, ports)
    if bays:
        for x, y in BAYS:
            img = addc(img, load(f"{R}-bay-light")[0], x, y + dy, bays / 3)
    if racks:
        for i, (x, y) in enumerate(RACKS):
            if f"gen{i}" not in dead:
                img = addc(img, load(f"{R}-rack-lock")[0], x - RACK_BOX[0] / 2 + 13, y - RACK_BOX[1] / 2 + 4 + dy,
                           racks / 3)
    if shield:
        sprite.paste_center(img, load("enemy-shield-medium")[int(t * 8) % 4], CANNON[0], CANNON[1] + dy)
    ship(img, *ship_xy)
    return img


def gold_shares():
    return float(u60.gold_share(phase1_layer()))


def label(img, x, y, s, col=raster.LABEL):
    raster.draw_text(img, x, y, s, col)


def cell_row(sheet, x, y, frames, zoom, glow=False, gap=4, maxw=None):
    for f in frames:
        c = artkit.on_background(f, zoom, glow)
        if maxw and x + c.width > maxw:
            break
        sheet.alpha_composite(c, (x, y))
        x += c.width + gap
    return x


def review_phase1():
    W = 1000
    sheet = raster.sheet(W, 1980, "ASCENDANT (FINAL R80): PHASE 1, THE GILDED WEDGE",
                         f"PRODUCTION ART, {BATCH.upper()} - R80")
    gs = gold_shares()
    y = 36
    label(sheet, 16, y, f"PHASE 1 OVER THE LEVEL 42 STAND-IN (1X): GOLD {gs * 100:.0f} % OF THE SILHOUETTE; "
                        "THE LEFT FACE'S PANELS HEATING, THE RIGHT ONE'S RIM TOLD")
    s1 = phase1_scene(0.4, (360, 470), heat={0: 0.45, 1: 0.85}, rim={2: 1.0}, sweep_t=0.8, shield=True)
    s2 = phase1_scene(1.3, (130, 470), down=(2,), dead=("gen0",), lance=0.9, racks=3, bays=3, sweep_t=1.6)
    sheet.alpha_composite(s1, (16, y + 14))
    sheet.alpha_composite(s2, (16 + 480 + 8, y + 14))
    y += 14 + 540 + 4
    label(sheet, 16, y, "LEFT: THE SHIP RIGHT, THE CANNON'S SHIELD, BEAMS, LAMPS, PANEL SWEEP. RIGHT: GEN-L DEAD (ITS "
                        "BEAM AND RACK GONE), A PANEL DOWN, THE LANCE'S MUZZLE GLOW, BAYS AND RACK OPEN", raster.LABEL_DIM)
    y += 20
    rows = [("CANNON BASE: WHOLE, DAMAGED, DEAD (2X)", load(f"{R}-cannon-base"), 2, False),
            ("CANNON TURRET, EVERY 4TH OF 64 HEADINGS (2X)", load(f"{R}-cannon")[::4], 2, False),
            ("GENERATOR: LIT, DAMAGED, DEAD, GRAFTED; GLOW (2X)", load(f"{R}-generator") + load(f"{R}-generator-glow"),
             2, False),
            ("BATTERY: WHOLE, DAMAGED, DEAD; BEAM STRIP (2X)", load(f"{R}-battery") + load(f"{R}-beam"), 2, False),
            ("PANELS, LEFT FACE HEADINGS 4..12 (2X)", load(f"{R}-panel")[:9], 2, False),
            ("PANELS, RIGHT FACE HEADINGS 52..60 (2X)", load(f"{R}-panel")[9:], 2, False)]
    for text, frames, zoom, glow in rows:
        label(sheet, 16, y, text)
        cell_row(sheet, 16, y + 12, frames, zoom, glow, maxw=W - 16)
        y += 12 + max(f.height for f in frames) * zoom + 10
    label(sheet, 16, y, "PANEL HEADING 8: WHOLE, HEAT (RED, ORANGE, WHITE), CRACKS AT 70 %+, RED RIM TELL, DOWN, "
                        "SWEEP GLINT (2X)")
    j = 4
    base = load(f"{R}-panel")[j]
    cells = [artkit.on_background(base, 2)]
    for h in (0.25, 0.55, 0.95):
        c = artkit.on_background(base, 2)
        c = artkit.add_light(c, sprite.enlarge(tinted_glow(load(f"{R}-panel-glow")[j], heat_colour(h), 0.6 + 0.4 * h), 2))
        if h > 0.7:
            cr = sprite.enlarge(load(f"{R}-panel-cracks")[j], 2)
            c.alpha_composite(cr)
        cells.append(c)
    c = artkit.add_light(artkit.on_background(base, 2), sprite.enlarge(load(f"{R}-panel-rim")[j], 2))
    cells += [c, artkit.on_background(load(f"{R}-panel-down")[j], 2),
              artkit.on_background(load(f"{R}-panel-sweep")[0], 2, glow=True)]
    x = 16
    for c in cells:
        sheet.alpha_composite(c, (x, y + 12))
        x += c.width + 4
    y += 12 + 128 + 10
    label(sheet, 16, y, "TELLS: MUZZLE 0..5, PORT FLASH, BAY-L 0..3, BAY LIGHT, RACK-L 0..3, RACK LOCK, LAMPS (2X)")
    tells = [(f, True) for f in load(f"{R}-muzzle")] + [(f, True) for f in load(f"{R}-port-flash")]
    tells += [(f, False) for f in load(f"{R}-bay-l")] + [(load(f"{R}-bay-light")[0], True)]
    tells += [(f, False) for f in load(f"{R}-rack-l")] + [(load(f"{R}-rack-lock")[0], True)]
    tells += [(load(f"{R}-lamp-red")[0], True), (load(f"{R}-lamp-gold")[0], True)]
    x = 16
    for f, g in tells:
        c = artkit.on_background(f, 2, g)
        if x + c.width > W - 16:
            y += 92
            x = 16
        sheet.alpha_composite(c, (x, y + 12))
        x += c.width + 4
    y += 12 + 82 + 8
    label(sheet, 16, y, f"THE HULL FRAME 440X276 (0.5X) AND ITS OVERLAYS; ATLAS {atlas_share() * 100:.1f} % OF ONE "
                        "2048 PAGE FOR THE WHOLE BOSS (2 PX PADDING)")
    hull = load(f"{R}-hull")[0]
    sheet.alpha_composite(artkit.on_background(hull.resize((220, 138), Image.LANCZOS), 1), (16, y + 12))
    sheet = sheet.crop((0, 0, W, y + 12 + 138 + 12))
    frames = []
    for f in range(70):
        t = f / 10
        if t < 3.0:                                       # the slide-in: lights from the bow back
            u = smoothstep(0, 1, t / 3.0)
            frames.append(phase1_scene(t, (240, 470), dy=-int(280 * (1 - u)), lamps_up=u, sweep_t=t).convert("RGB"))
        else:
            tx = 240 + 150 * np.sin((t - 3) * 0.9)
            lance = max(0.0, min(1.0, (t - 4.2) / 0.8)) if t < 5.4 else 0.0
            heat = {0: min(1.0, max(0, (t - 3.0) / 3.0)), 1: min(1.0, max(0, (t - 4.0) / 3.0))}
            rim = {3: 1.0} if 5.6 < t < 6.1 else None
            frames.append(phase1_scene(t, (tx, 470), heat=heat, rim=rim, lance=lance, ports=1.0 if 6.2 < t else 0.0,
                                       bays=min(3, max(0, int((t - 5.8) * 6))), sweep_t=t).convert("RGB"))
    artkit.save_review(sheet, frames, CONCEPT, R, fps=10)


def phase2_layer(erupt=5, sacs=(0, 0, 0), heart=0, dy=0, dead_sacs=()):
    lay = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    lay.alpha_composite(load(f"{R}-hull")[0], (HULL_BOX[0], HULL_BOX[1] + dy))
    gens = load(f"{R}-generator")
    for x, y in GENS:
        sprite.paste_center(lay, gens[3 if erupt is not None and erupt >= 5 else 2], x, y + dy)
    bats = load(f"{R}-battery")
    for x, y, s in BATT:
        sprite.paste_center(lay, bats[2], x, y + dy)
    sprite.paste_center(lay, load(f"{R}-cannon-base")[2], CANNON[0], CANNON[1] + dy)
    if erupt is not None and erupt >= 0:
        lay.alpha_composite(load(f"{R}-keel")[erupt], (KEEL_BOX[0], KEEL_BOX[1] + dy))
    if erupt is not None and erupt >= 3:
        sf = load(f"{R}-sac")
        for i, (x, y) in enumerate(SACS):
            if i in dead_sacs:
                sprite.paste_center(lay, load(f"{R}-sac-dead")[0], x, y + dy)
            else:
                sprite.paste_center(lay, sf[sacs[i]], x, y + dy)
        sprite.paste_center(lay, load(f"{R}-heart")[heart], HEART[0], HEART[1] + dy)
    return lay


def phase2_scene(t, ship_xy, erupt=5, sacs=(0, 0, 0), heart=0, dead_sacs=(), heal=(), cracks=None, shake=0,
                 heart_heal=0.0, flare=None):
    img = bg()
    lay = phase2_layer(erupt, sacs, heart, 0, dead_sacs)
    if shake:
        lay = lay.transform(lay.size, Image.AFFINE, (1, 0, shake, 0, 1, -shake // 2))
    img.alpha_composite(lay)
    if cracks is not None:
        img = addtl(img, load(f"{R}-keel-cracks")[cracks[0]], CRACK_BOX[0], CRACK_BOX[1], cracks[1])
    if erupt is not None and erupt >= 3:
        for i, (x, y) in enumerate(SACS):
            if i in dead_sacs:
                continue
            img = addc(img, load(f"{R}-sac-core")[0], x, y, 0.7 + 0.3 * np.sin(t * 6 + i))
            if i in heal:
                img = addc(img, load(f"{R}-sac-heal")[0], x, y, 0.5 + 0.5 * np.sin(t * 9))
            if flare is not None and flare == i:
                img = addc(img, load(f"{R}-sac-flare")[0], x, y, 1.0)
        img = addc(img, load(f"{R}-heart-core")[0], *HEART, (0.4 if heart == 0 else 1.0) * (0.75 + 0.25 * np.sin(t * 4)))
        if heart_heal:
            img = addc(img, load(f"{R}-heart-heal")[0], *HEART, heart_heal)
    ship(img, *ship_xy)
    return img


def wreck_scene(t, ship_xy, boat_xy=None, tilt=2, rim_k=1.0, flash=None):
    """The wreck's halves drifting apart along the fracture (t seconds into the escape), vents, fires, shards."""
    img = bg()
    piv = load_pivots()["wreck"]
    u = smoothstep(0, 1, t / 14.0) * 0.8 + 0.2 * (t / 14.0)
    rng = np.random.default_rng(SEED + 90)
    for side in ("left", "right"):
        w = piv[side]
        dx, dy = w["drift"][0] * u, w["drift"][1] * u
        ox, oy = HULL_BOX[0] + w["offset"][0] + dx, HULL_BOX[1] + w["offset"][1] + dy
        half = load(f"{R}-wreck-{side[0]}")[0]
        sprite.paste(img, half, ox, oy)
        img = addtl(img, load(f"{R}-wreck-{side[0]}-rim")[0], ox, oy, rim_k * (1 - 0.5 * t / 14))
        for j, (vx, vy, deg, col) in enumerate(w["vents"]):
            fr = load(f"{R}-vent-{col}")[(int(t * 12) + j) % 6]
            fr = fr.rotate(-deg, resample=Image.BICUBIC, expand=True)
            ang = np.radians(deg)
            cx = ox + vx + np.sin(ang) * 22
            cy = oy + vy - np.cos(ang) * 22
            img = addc(img, fr, cx, cy, 0.8 * (1 - 0.6 * t / 14))
        for j, (fx, fy) in enumerate(w["fires"]):
            img = addc(img, load(f"{R}-fire")[(int(t * 10) + j * 2) % 6], ox + fx, oy + fy, 0.9)
        for j in range(5):
            line = w["fracture"]
            px, py = line[int(rng.integers(len(line) // 4, len(line)))]
            sh = load(f"{R}-shard")[(int(t * 12) + j) % 8]
            sgn = 1 if side == "left" else -1
            sprite.paste_center(img, sh, ox + px + sgn * (8 + 30 * u) * rng.uniform(0.5, 1.2),
                                oy + py + rng.uniform(-10, 10) + 20 * u)
    if boat_xy:
        b = load(f"{R}-lifeboat")[tilt]
        sprite.paste_center(img, b, *boat_xy)
        lp = load_pivots()["lifeboat"]
        dxp, dyp = lp["drive"][tilt]
        img = addc(img, load(f"{R}-lifeboat-drive")[int(t * 12) % 4], boat_xy[0] - 36 + dxp, boat_xy[1] - 44 + dyp)
        for j in range(4):
            age = j
            img = addc(img, load(f"{R}-lifeboat-wake")[age], boat_xy[0] + (-8 if j % 2 else 8),
                       boat_xy[1] + 50 + j * 18, 0.9)
    if flash is not None:
        img = addc(img, load(f"{R}-lifeboat-flash")[flash], 240, 30, 1.0)
    ship(img, *ship_xy)
    return img


def k_scene(t, ship_xy):
    """K: the heart bursting, the sacs with it, the chain down the keel with plates blown outward, the flash."""
    img = bg()
    img.alpha_composite(phase2_layer(5, heart=3, dead_sacs=()))
    if t < 0.67:
        img = addc(img, load(f"{R}-heart-burst")[min(7, int(t * 12))], *HEART)
        for x, y in SACS:
            img = addc(img, load(f"{R}-sac-burst")[min(5, int(t * 12))], x, y)
    for ev in load_pivots()["chain"]:
        dt = t - ev["t"]
        if 0 <= dt < 14 / 15:
            name = f"explosion-vacuum-{ev['size']}"
            fr = load(name)
            img = addc(img, fr[min(len(fr) - 1, int(dt * 15))], CENTRE[0] + ev["dx"], CENTRE[1] + ev["dy"])
            rng = np.random.default_rng(int(ev["t"] * 100))
            for j in range(2):
                a = rng.uniform(0, TAU)
                r = 20 + 90 * dt
                sprite.paste_center(img, load(f"{R}-plate")[(int(dt * 12) + j) % 8], CENTRE[0] + ev["dx"] + np.cos(a) * r,
                                    CENTRE[1] + ev["dy"] + np.sin(a) * r)
    if 2.5 <= t < 3.0:
        a = np.array(img).astype(np.float64)
        k = 1 - (t - 2.5) / 0.5
        a[..., :3] = a[..., :3] * (1 - 0.6 * k) + 255 * 0.6 * k
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
    ship(img, *ship_xy)
    return img


def review_fx():
    W = 1000
    sheet = raster.sheet(W, 3200, "ASCENDANT (FINAL R80): THE ERUPTION, PHASE 2, THE BREAK, THE LIFEBOAT",
                         f"PRODUCTION ART, {BATCH.upper()} - R80")
    y = 36
    label(sheet, 16, y, "THE ERUPTION (0.5X): THE SHUDDER'S CRACKS (1 S), THEN KEEL FRAMES 0..5 OVER 3 S; PHASE 2 AT REST")
    x = 16
    for f in range(7):
        if f == 0:
            sc = phase2_scene(0, (240, 470), erupt=None, cracks=(1, 1.0))
        else:
            sc = phase2_scene(f * 0.5, (240, 470), erupt=f - 1, cracks=(2, max(0.0, 1 - f / 6)),
                              sacs=(min(3, f - 3),) * 3 if f >= 4 else (0, 0, 0))
        sm = sc.crop((0, 0, FW, 300)).resize((240 - 4 + 4, 150), Image.LANCZOS)
        sheet.alpha_composite(sm, (x, y + 12))
        x += 240 + 4
        if x + 240 > W:
            x = 16
            y += 150 + 6
    y += 150 + 18
    label(sheet, 16, y, "PHASE 2 (1X): LEFT THE HEART SEALED, SAC-2 HEALING, SAC-1 SWELLING (BROOD TELL); RIGHT TWO SACS "
                        "DEAD, THE HEART OPEN AND HEALING")
    a = phase2_scene(0.3, (360, 470), sacs=(3, 0, 0), heal=(1,), flare=0)
    b = phase2_scene(0.9, (150, 470), sacs=(0, 0, 0), heart=3, dead_sacs=(0, 1), heart_heal=0.8)
    sheet.alpha_composite(a.crop((0, 0, FW, 330)), (16, y + 12))
    sheet.alpha_composite(b.crop((0, 0, FW, 330)), (16 + 488, y + 12))
    y += 12 + 330 + 10
    rows = [("SACS: REST, SWELL 1-3, SPLIT, DEAD; CORE, HEAL, FLARE (2X)",
             load(f"{R}-sac") + load(f"{R}-sac-dead") + load(f"{R}-sac-core") + load(f"{R}-sac-heal")
             + load(f"{R}-sac-flare"), 2),
            ("HEART: SEALED, OPENING, OPEN; CORE, HEAL (2X)", load(f"{R}-heart") + load(f"{R}-heart-core")
             + load(f"{R}-heart-heal"), 2),
            ("K: HEART BURST 0..7 (1X)", load(f"{R}-heart-burst"), 1),
            ("SAC BURST, PLATES, SHARDS, FIRE (2X)", load(f"{R}-sac-burst") + load(f"{R}-plate") + load(f"{R}-shard")
             + load(f"{R}-fire"), 2)]
    for text, frames, zoom in rows:
        label(sheet, 16, y, text)
        x = 16
        hmax = 0
        for f in frames:
            c = artkit.on_background(f, zoom, glow=is_additive(f))
            if x + c.width > W - 16:
                y += hmax + 4
                x = 16
                hmax = 0
            sheet.alpha_composite(c, (x, y + 12))
            x += c.width + 4
            hmax = max(hmax, c.height)
        y += 12 + hmax + 10
    label(sheet, 16, y, "THE WRECK'S HALVES ON THE JAGGED FRACTURE (1X, APART) AND THEIR HOT RIMS; VENTS ORANGE/TEAL (2X)")
    x = 16
    for side in "lr":
        h = load(f"{R}-wreck-{side}")[0]
        c = artkit.checker(h.width, h.height)
        c.alpha_composite(h)
        c = artkit.add_light(c, load(f"{R}-wreck-{side}-rim")[0])
        sheet.alpha_composite(c, (x, y + 12))
        x += h.width + 16
    vx = x
    vy = y + 12
    for f in load(f"{R}-vent-orange")[:3] + load(f"{R}-vent-teal")[:3]:
        c = artkit.on_background(f, 2, True)
        sheet.alpha_composite(c, (vx, vy))
        vx += c.width + 4
    y += 12 + 280 + 10
    label(sheet, 16, y, "K AND THE ESCAPE (1X): THE CHAIN AT 1.2 S; THE HALVES PARTING AT 4 S WITH THE LIFEBOAT; "
                        "AT 10 S; ESCAPED (THE FLASH)")
    x = 16
    for sc in (k_scene(1.2, (240, 470)), wreck_scene(4.0, (260, 470), (220, 150), 1),
               wreck_scene(10.0, (230, 470), (270, 60), 3), wreck_scene(13.9, (240, 470), None, 2, flash=1)):
        sheet.alpha_composite(sc.resize((240, 270), Image.LANCZOS), (x, y + 12))
        x += 244
    y += 12 + 270 + 10
    label(sheet, 16, y, "LIFEBOAT: TILTS -20..+20, DRIVE, WAKE BY AGE (2X); TEAR 0..7, GATE FLASH (1X)")
    x = 16
    for f in load(f"{R}-lifeboat"):
        c = artkit.on_background(f, 2)
        sheet.alpha_composite(c, (x, y + 12))
        x += c.width + 4
    for f in load(f"{R}-lifeboat-drive") + load(f"{R}-lifeboat-wake"):
        c = artkit.on_background(f, 2, True)
        sheet.alpha_composite(c, (x, y + 12))
        x += c.width + 4
    y += 12 + 176 + 6
    x = 16
    for f in load(f"{R}-lifeboat-tear") + load(f"{R}-lifeboat-flash")[:2]:
        c = artkit.on_background(f, 1, True)
        if x + c.width > W - 16:
            break
        sheet.alpha_composite(c, (x, y))
        x += c.width + 4
    y += 112 + 10
    sheet = sheet.crop((0, 0, W, y))
    frames = []
    for f in range(40):                                    # the shudder and the eruption, 4 s
        t = f / 10
        if t < 1.0:
            frames.append(phase2_scene(t, (240, 470), erupt=None, cracks=(0 if t < 0.4 else 1, 0.6 + 0.4 * t),
                                       shake=(f % 2) * 2 - 1).convert("RGB"))
        else:
            e = min(5, int((t - 1.0) / 0.5))
            frames.append(phase2_scene(t, (240, 470), erupt=e, cracks=(2, max(0.0, 1 - (t - 1) / 3)),
                                       sacs=(0, 0, 0), shake=(f % 2) * 2 - 1 if t < 2 else 0).convert("RGB"))
    for f in range(20):                                    # phase 2: a sac healing, a brood swelling
        t = f / 10
        frames.append(phase2_scene(t, (200 + 8 * f, 470), sacs=(min(3, f // 5), 0, 0), heal=(1,),
                                   flare=0 if f > 8 else None).convert("RGB"))
    for f in range(36):                                    # K: the burst, the chain, the flash at 2.5 s
        frames.append(k_scene(f / 12, (240, 470)).convert("RGB"))
    rng = np.random.default_rng(SEED + 95)
    wx = 240.0
    for f in range(70):                                    # the escape, 14 s at 5x speed (the last second slower)
        t = f * 0.2
        y_b = 175 - (175 + 40) * smoothstep(0, 1, t / 14) ** 1.3
        wx = 240 + 60 * np.sin(t * 0.9)
        tilt = int(np.clip(np.round(np.cos(t * 0.9) * 2), -2, 2)) + 2
        frames.append(wreck_scene(t, (240 + 40 * np.sin(t * 0.5), 470), (wx, y_b) if y_b > -40 else None, tilt,
                                  flash=None).convert("RGB"))
    for f in range(6):
        frames.append(wreck_scene(14.0, (240, 470), None, 2, flash=f).convert("RGB"))
    artkit.save_review(sheet, frames, CONCEPT, f"{R}-fx", fps=10)


def is_additive(f):
    """An additive frame is premultiplied on black: alpha is 255 wherever it adds light, and its darkest lit pixels
    are near black (a normal sprite's are not)."""
    a = np.array(f)
    lit = a[..., 3] > 0
    if not lit.any():
        return False
    return a[lit][:, :3].max(axis=1).min() < 8


# =========================================================================== check

SETS = ([(f"{R}-hull", 1, (440, 276), False, True), (f"{R}-keel", 6, (160, 276), False, False),
         (f"{R}-keel-cracks", 3, (48, 276), True, False)]
        + [(f"{R}-bay-{s}", 4, BAY_BOX, False, False) for s in "lr"]
        + [(f"{R}-rack-{s}", 4, RACK_BOX, False, False) for s in "lr"]
        + [(f"{R}-bay-light", 1, (40, 32), True, True), (f"{R}-rack-lock", 1, (16, 16), True, True),
           (f"{R}-port-flash", 4, (24, 24), True, False), (f"{R}-lamp-red", 1, (16, 16), True, True),
           (f"{R}-lamp-gold", 1, (16, 16), True, True),
           (f"{R}-cannon-base", 3, BASE_CELL, False, False), (f"{R}-cannon", N, (TURRET_CELL, TURRET_CELL), False, False),
           (f"{R}-muzzle", 6, (40, 40), True, False), (f"{R}-generator", 4, (56, 56), False, False),
           (f"{R}-generator-glow", 1, (48, 48), True, True), (f"{R}-beam", 4, (24, 32), True, False),
           (f"{R}-battery", 3, (52, 52), False, False)]
        + [(f"{R}-panel{s}", len(PANEL_K), (PANEL_CELL, PANEL_CELL), add, False)
           for s, add in (("", False), ("-glow", True), ("-cracks", False), ("-rim", True), ("-down", False))]
        + [(f"{R}-panel-sweep", 1, (24, 24), True, True),
           (f"{R}-sac", 5, (60, 60), False, False), (f"{R}-sac-dead", 1, (60, 60), False, True),
           (f"{R}-sac-core", 1, (44, 44), True, True), (f"{R}-sac-heal", 1, (60, 60), True, True),
           (f"{R}-sac-flare", 1, (60, 60), True, True), (f"{R}-heart", 4, (68, 68), False, False),
           (f"{R}-heart-core", 1, (64, 64), True, True), (f"{R}-heart-heal", 1, (68, 68), True, True),
           (f"{R}-heart-burst", 8, (128, 128), True, False), (f"{R}-sac-burst", 6, (64, 64), True, False),
           (f"{R}-plate", 8, (16, 16), False, False), (f"{R}-shard", 8, (12, 12), False, False),
           (f"{R}-wreck-l", 1, None, False, True), (f"{R}-wreck-r", 1, None, False, True),
           (f"{R}-wreck-l-rim", 1, None, True, True), (f"{R}-wreck-r-rim", 1, None, True, True),
           (f"{R}-vent-orange", 6, (24, 56), True, False), (f"{R}-vent-teal", 6, (24, 56), True, False),
           (f"{R}-fire", 6, (24, 24), True, False),
           (f"{R}-lifeboat", len(TILTS), (72, 88), False, False), (f"{R}-lifeboat-drive", 4, (32, 32), True, False),
           (f"{R}-lifeboat-wake", 6, (28, 28), True, False), (f"{R}-lifeboat-tear", 8, (112, 112), True, False),
           (f"{R}-lifeboat-flash", 6, (96, 96), True, False)])


def checker_share(img):
    """DitherGuardTest's measure: visible pixels that are a 1 px checker cell -> (visible, checker)."""
    a = np.array(img.convert("RGBA")).astype(np.int64)
    c = (a[..., 3] << 24) | (a[..., 0] << 16) | (a[..., 1] << 8) | a[..., 2]
    vis = a[1:-1, 1:-1, 3] > 0
    mid = c[1:-1, 1:-1]
    left, right, up, down = c[1:-1, :-2], c[1:-1, 2:], c[:-2, 1:-1], c[2:, 1:-1]
    chk = vis & (left == right) & (up == down) & (left != mid) & (up != mid)
    return int(vis.sum()), int(chk.sum())


def glow_steps(frames):
    vals = np.concatenate([np.array(f)[..., :3].max(axis=-1).ravel() for f in frames])
    return len(np.unique(vals[vals > 0]))


def atlas_area(frames):
    """Page area the packer takes for a set (2 px padding, no whitespace strip)."""
    return sum((f.width + 2) * (f.height + 2) for f in frames)


def atlas_share():
    total = 0
    for name, *_ in SETS:
        try:
            total += atlas_area(load(name))
        except FileNotFoundError:
            pass
    return total / 2048 ** 2


def check():
    bad = 0
    total = 0
    for name, n, size, add, single in SETS:
        where = l23kit.where(name)
        try:
            frames = load(name)
        except FileNotFoundError:
            frames = []
        folder = ROOT / where
        files = [f for f in folder.glob(f"{name}*.png") if (f.stem == name if single else (
            f.stem.rsplit("_", 1)[0] == name and f.stem.rsplit("_", 1)[-1].isdigit()))]
        src_ok = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        vis = chk = 0
        for f in files:
            v, c = checker_share(Image.open(f))
            vis, chk = vis + v, chk + c
        share = chk / max(vis, 1)
        steps = glow_steps(frames) if add and frames else None
        ok = (len(frames) == n and len(files) == n and (size is None or all(fr.size == tuple(size) for fr in frames))
              and src_ok and share < 0.05 and (steps is None or steps >= 32))
        total += atlas_area(frames)
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)} frames {frames[0].size if frames else None}, "
              f"checker {share * 100:.1f} %" + (f", {steps} glow steps" if steps is not None else ""))
    try:
        piv = load_pivots()
        ok = (len(piv["cannon"]["muzzle"]) == N and len(piv["panels"]["frames"]) == len(PANEL_K)
              and "wreck" in piv and len(piv["lifeboat"]["drive"]) == len(TILTS))
    except (FileNotFoundError, KeyError):
        ok = False
    bad += not ok
    print(f"{'ok ' if ok else 'BAD'} {pivots_folder().relative_to(ROOT)}/{R}.json")
    path = artkit.SPRITES / "intel" / "boss-ascendant.png"
    ok = path.exists() and Image.open(path).text.get(artkit.SOURCE_KEY, "").startswith("tools/art/intel.py")
    bad += not ok
    print(f"{'ok ' if ok else 'BAD'} {path.relative_to(ROOT)}")
    gs = gold_shares()
    ok = 0.35 <= gs <= 0.45
    bad += not ok
    print(f"{'ok ' if ok else 'BAD'} gold share of the phase-1 silhouette: {gs * 100:.1f} % (35-45 %)")
    print(f"atlas: {total:,} px = {100 * total / 2048 ** 2:.1f} % of one 2048x2048 page (2 px padding)")
    return bad == 0


BUILDERS = {"hull": build_hull, "parts": build_parts, "fx": build_fx, "intel": build_intel}
REVIEWS = {"hull": lambda: None, "parts": review_phase1, "fx": review_fx, "intel": review_intel}

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    for s in subjects:
        REVIEWS[s]()
