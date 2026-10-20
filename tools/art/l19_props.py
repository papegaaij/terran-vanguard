#!/usr/bin/env python3
"""Production art: Level 19's props (design/campaign/act-3-red-dust/level-19-foundry-of-tharsis, "Layout",
"Up-facing turret nests", "Ground targets", "Hazards" and "Secrets and pickups"; M7 part F batch, concept round
42, step ART-F4; straight to production, no concept: designed to the README, the Tharsis foundry mood still
`mars-tharsis-foundry-r36-a` (via tools/art/backdrop_l19.py's iron, concrete and glow colours), Mars light from
the upper left and readability rule 7 (the light rim) for the secret).

Headings as tools/art/README.md: heading k turns k x 360/n degrees clockwise from straight down (for 16: 4 = facing
left, 8 = up, 12 = right); every heading its own render under the fixed key light. Model units are pixels.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root, vanguard.pipeline.SpriteUse:
a ground target's `sprite` claims its name and every name starting with it and a hyphen; the vents, gantries,
office, ingots and rovers are claimed by the level's vents, `arc` nests, payroll safe and belts, as SpriteUse;
an unclaimed root stays held):
  furnace-vent_0..3         48x48, the furnace vent grate in its riveted iron frame with the hazard-striped lip:
                            0 shut (cold: the casting yard's, and between bursts), 1-3 the 1.2 s tell heating
                            (the bed under the bars dull red, orange, white-orange; the bars glowing on 3); the
                            burst draws frame 3 under its flame
  furnace-vent-glow_0..2    64x64, additive, centred on the grate: the tell's heat glow over it for tell steps 1-3
                            (0.4 s each), with the heat shimmer's broken rings
  furnace-vent-flame_0..7   64x64, additive: the 1.0 s flame column seen from above (8 fps): 0-1 the ignition
                            spreading, 2-5 the roaring column 44 px across (the README's hit width, white core,
                            turbulent orange edge; a loop if the game holds it), 6-7 collapsing to embers
  furnace-vent-smoke_0..5   64x64, stepped translucency: the dark smoke puff that rises off the grate after a burst
                            (6 frames at 8 fps, drawn over the grate on the ground pass)
  gantry                    480x48, one region: the steel truss gantry across the field (two chords, cross members,
                            diagonal lacing, a grating walkway; the end towers with hazard paint at x 0-28 and
                            452-480): the ore gantry (t~18), the conveyor gantry (t~60) and the yard gate's (t~115)
  gantry-cradle             64x64, one region: an up-facing nest's cradle, drawn under its Spine Turret at the same
                            centre: a grating deck flaring up the screen in a half disc with a hazard-striped lip, the
                            turret ring in its middle and two amber chevrons painted on the deck pointing up the line
                            (the README's "points back" cue: the arc faces up); two hangers down to the gantry's beam
                            below; the smelter-stack nests (t~80) sit on the same cradle
  payroll-safe_0..2         36x32, the foundry payroll safe (the secret, a 20 HP `ground` trigger): a squat dark steel
                            safe seen three-quarter from the front (its door faces up the screen, toward the light), its dial, handle and door lamp: 0 intact, 1 hit
                            (scorched, the dial knocked off), 2 opened (the door blown open on its hinge, the scrip
                            bundles inside); rule 7's light rim on every frame
  payroll-safe-glow_0..7    36x32, additive, aligned with the safe: the green door lamp (GroundGlow: 0-3 a slow blink
                            until the first hit, 4-7 a hurried flicker after it)
  shift-office              56x84, one region: the roofless shift office at the first hall's left wall (low walls,
                            a door gap, lockers, a desk with a lit terminal, a chair); the safe stands in its lower
                            left corner (office centre x 76, the safe at x 60, 22 px below the office's centre)
  ingot-stack-a, -b, -c     48x40, one region each: stacks of cast ingots on steel pallets by the outbound belt
                            (a: three crossed layers, b: two layers strapped, c: fresh from the moulds, the top layer
                            still glowing dull orange)
  ingot-pallet              40x32, one region: a pallet of ingots riding the outbound belt (fits the 48 px belt)
  crew-rover_0..15          48x48, 16 headings: the crews' six-wheeled pressurised rover (colony off-white, amber
                            bands, a dark windscreen, a roof beacon) leaving the tower (scenery, t~160)
  crew-rover-lights_0..15   64x64, additive, per heading, same centre: its two headlight beams and the amber beacon
  design/campaign/act-3-red-dust/level-19-foundry-of-tharsis/concept/l19-props-final-r42-a.png/.gif   the review
                            sheet and GIF (over tools/art/backdrop_l19.py's play field: the ore gantry's nests
                            passing the ship and firing back up, the safe shot open, a vent row's ripple, the south
                            gate's ingots and the crews' rovers)

Run: python3 tools/art/l19_props.py [vent] [gantry] [safe] [ingots] [rover] [--review]   (~1 min; no subject = all;
--review rebuilds only the review from the written files)
"""
import contextlib
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import ground_targets as gt  # noqa: E402  (concept script, imported unchanged: the light rim)
import siege_units  # noqa: E402  (tools/art: the levels' claimed ground-target roots)
import storm_units as su  # noqa: E402  (tools/art: noisy blobs, stepped translucency)
import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged: light canvases)
from render import raster  # noqa: E402
from render.enemy_rigs import model_space_materials, write_gif  # noqa: E402
from render.sdf import (Material, rotate_x, rotate_z, sd_box, sd_capsule, sd_cylinder_x, sd_cylinder_y,  # noqa: E402
                        sd_cylinder_z, sd_plate, sd_sphere, subtract, union)

SCRIPT = "l19_props.py"
BATCH = "M7 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r42"
LEVEL = DESIGN / "campaign" / "act-3-red-dust" / "level-19-foundry-of-tharsis"
CONCEPT = LEVEL / "concept"
HELD = ROOT / "build" / "art-held" / "sprites"

# the foundry's colours (backdrop_l19: STEEL, CONCRETE, GLOW, HOT, AMBER), as 0..1 albedos
IRON = (0.2, 0.2, 0.22)
IRON_D = (0.11, 0.11, 0.12)
STEEL = (0.42, 0.41, 0.42)
STEEL_L = (0.6, 0.58, 0.57)
CONCRETE = (0.5, 0.48, 0.45)
HAZARD = (0.86, 0.62, 0.12)
RUST = (0.46, 0.25, 0.14)
OFF_WHITE = (0.78, 0.75, 0.68)                      # the colony vehicles' (storm_units' rover)
AMBER_PAINT = (0.82, 0.48, 0.14)
GLOW = np.array((255, 132, 36), float)
HOT = np.array((255, 226, 150), float)


# =========================================================================== held sprites

def _rooted(name, root):
    return name == root or name.startswith(root + "-")


# M7 part F, as vanguard.pipeline.SpriteUse: a payroll safe's surround, and a level's vents, up-facing nests and belts.
TRIGGER_SURROUNDS = {"payroll-safe": "shift-office"}
VENT_ROOT = "furnace-vent"
GANTRY_ROOT = "gantry"
FOUNDRY_ROOTS = ("ingot", "crew-rover")


def foundry_roots():
    """The roots the level data claims for the foundry's looks (SpriteUse, M7 part F): a ground target's surround,
    the gantry (and its cradles) under an up-facing nest (`arc`), the vents' looks, a belt level's ingots and rovers."""
    roots = set()
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        data = siege_units._yaml(f)
        for g in data.get("ground_targets") or []:
            if str(g.get("sprite")) in TRIGGER_SURROUNDS:
                roots.add(TRIGGER_SURROUNDS[str(g["sprite"])])
            if g.get("arc"):
                roots.add(GANTRY_ROOT)
        if data.get("vents"):
            roots.add(VENT_ROOT)
        if data.get("belts"):
            roots.update(FOUNDRY_ROOTS)
    return roots


def claimed(name):
    """Whether the data claims the sprite (SpriteUse): a level's ground target naming the root, or a foundry look."""
    return any(_rooted(name, r) for r in siege_units.level_sprite_roots() | foundry_roots())


@contextlib.contextmanager
def sprites_of(name):
    target = artkit.SPRITES if claimed(name) else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def _owned(stem, name):
    return stem == name or (stem.rsplit("_", 1)[0] == name and stem.rsplit("_", 1)[-1].isdigit())


def write(name, frames, single=False):
    for folder in (artkit.SPRITES, HELD):          # no stale copy in the other folder
        for old in folder.glob(f"{name}*.png"):
            if _owned(old.stem, name):
                old.unlink()
    with sprites_of(name) as where:
        artkit.write_frames(name, frames, SOURCE, single=single)
    print(f"  {name}: {len(frames)} frame(s) {frames[0].width}x{frames[0].height} -> {where.relative_to(ROOT)}")


def load(name):
    for folder in (artkit.SPRITES, HELD):
        single = folder / f"{name}.png"
        if single.exists():
            return [Image.open(single).convert("RGBA")]
        files = [f for f in folder.glob(f"{name}_*.png") if _owned(f.stem, name)]
        if files:
            files.sort(key=lambda p: int(p.stem.rsplit("_", 1)[1]))
            return [Image.open(f).convert("RGBA") for f in files]
    return []


def render(scene, mats, size, z_top=40.0, steps=150, crisp=70, **kw):
    hi, factor = artkit.render_hi(scene, mats, size, float(size[0]), z_top=z_top, steps=steps, **kw)
    return artkit.native(hi, factor, crisp=crisp)


def rimmed(img):
    return Image.fromarray(gt.light_rim(np.array(img).astype(np.float64)).astype(np.uint8), "RGBA")


def rust_pattern(seed=0.0, amount=0.18):
    def f(p, n):
        v = np.sin(p[:, 0] * 0.9 + seed) * np.sin(p[:, 1] * 0.7 - seed) + 0.5 * np.sin(p[:, 0] * 2.3 + p[:, 1] * 1.7)
        return 1.0 - amount * np.clip(v, 0, 1)
    return f


def hazard_stripes(width=3.0):
    def f(p, n):
        return np.where(((p[:, 0] + p[:, 1]) / (2 * width)) % 1 < 0.5, 0.14, 1.0)
    return f


def grating(pitch=2.0):
    def f(p, n):
        gx = np.abs((p[:, 0] / pitch) % 1 - 0.5)
        gy = np.abs((p[:, 1] / pitch) % 1 - 0.5)
        return np.where(np.minimum(gx, gy) < 0.16, 0.55, 1.0)
    return f


# =========================================================================== furnace vents

VENT = 48
VENT_GLOW = 64
FLAME_N = 8
SMOKE_N = 6
HEAT = (0.06, 0.38, 0.72, 1.0)                     # frame 0 shut .. 3 white-hot


def vent_mats(heat):
    bed = np.array((1.0, 0.36, 0.08)) * (0.3 + 2.4 * heat) if heat < 0.9 else np.array((2.6, 2.0, 1.1))
    bars = (0.0, 0.0, 0.0) if heat < 0.7 else tuple(np.array((1.0, 0.4, 0.1)) * (0.5 if heat < 0.9 else 1.1))
    return [
        Material(IRON, metal=0.55, shininess=60, spec=0.6, pattern=rust_pattern(0.4)),                 # 0 frame
        Material(IRON_D, metal=0.6, shininess=70, spec=0.7, emission=bars,
                 emission_pattern=lambda p, n: 0.7 + 0.3 * np.cos(p[:, 1] * 0.4)),                       # 1 bars
        Material((0.05, 0.03, 0.02), emission=tuple(bed),
                 emission_pattern=lambda p, n: 0.75 + 0.25 * np.sin(p[:, 0] * 0.8 + 1.7 * np.sin(p[:, 1] * 0.6))),  # 2 bed
        Material(STEEL_L, metal=0.6, shininess=100, spec=1.0),                                          # 3 bolts
        Material(HAZARD, metal=0.1, shininess=20, spec=0.2, pattern=hazard_stripes(2.5)),               # 4 lip paint
    ]


def vent_scene(p):
    half = 21.0
    frame = sd_box(p, (0, 0, 1.5), (half, half, 2.5), 1.5)
    frame = subtract((frame, 0), sd_box(p, (0, 0, 1.5), (half - 5, half - 5, 6.0), 0.5))
    lip = subtract((sd_box(p, (0, 0, 3.6), (half - 2.2, half - 2.2, 0.5), 0.3), 4),
                   sd_box(p, (0, 0, 3.6), (half - 4.6, half - 4.6, 2.0), 0.0))
    items = [frame, lip, (sd_box(p, (0, 0, -6.0), (half - 5, half - 5, 1.0)), 2)]
    for k in range(6):                                     # the bars, across
        y = -13.3 + k * 5.3
        items.append((sd_box(p, (0, y, -0.5), (half - 5, 1.1, 2.0), 0.4), 1))
    items.append((sd_box(p, (0, 0, -1.2), (1.0, half - 5, 1.4), 0.3), 1))   # the centre rail
    for sx in (-1, 1):
        for sy in (-1, 1):
            items.append((sd_sphere(p, (sx * (half - 2.5), sy * (half - 2.5), 4.0), 1.1), 3))
    return union(*items)


def vent_frame(state):
    return render(vent_scene, vent_mats(HEAT[state]), (VENT, VENT), z_top=20.0, steps=160)


def vent_glow(i):
    """Tell step i+1: the heat over the grate (a soft square glow) and the shimmer's broken rings."""
    level = (0.45, 0.75, 1.0)[i]
    w = VENT_GLOW
    cv = v8.Canvas(w, w)
    c = w / 2
    yy, xx = cv.y, cv.x
    sq = np.maximum(np.abs(xx - c), np.abs(yy - c))
    col = GLOW * (1 - level) + HOT * level * 0.9 + GLOW * level * 0.1
    cv.add(tuple(col), np.clip(1 - sq / 18.0, 0, 1) ** 1.2 * 0.7 * level)
    cv.add(tuple(GLOW), v8.gauss(np.hypot(xx - c, yy - c), 22.0) * 0.35 * level)
    ang = np.arctan2(yy - c, xx - c)
    for k, r in enumerate((20.0 + 4 * i, 26.0 + 3 * i)):    # the shimmer: broken rings rising off the heat
        broken = (np.sin(ang * 5 + k * 1.7 + i * 2.1) > 0.15)
        cv.add(tuple(HOT * 0.7), v8.gauss(np.abs(np.hypot(xx - c, yy - c) - r), 0.8) * broken * 0.35 * level)
    return artkit.additive(cv.image())


def vent_flame(i):
    """The flame column from above: a turbulent disc with a white core; radius by frame."""
    w = VENT_GLOW
    S = 4
    radius = (11.0, 19.0, 22.0, 22.5, 22.0, 22.5, 17.0, 9.0)[i]
    power = (0.75, 0.95, 1.0, 1.0, 1.0, 1.0, 0.7, 0.35)[i]
    yy, xx = np.mgrid[0:w * S, 0:w * S].astype(np.float64) / S + 0.5 / S
    c = w / 2
    d = np.hypot(xx - c, yy - c)
    ang = np.arctan2(yy - c, xx - c)
    seed = 1901 + (2 + (i - 2) % 4 if 2 <= i <= 5 else i)
    turb = raster.fbm(w * S, w * S, 6 * S, seed, octaves=3, period=False)
    curl = raster.fbm(w * S, w * S, 3 * S, seed + 50, octaves=2, period=False)
    tongues = 0.5 + 0.5 * np.sin(ang * 9 + 6 * (turb - 0.5) + i * 1.3)
    edge = radius * (0.86 + 0.22 * tongues) + 5 * (turb - 0.5)
    body = np.clip((edge - d) / 6.0, 0, 1)
    core = np.clip(1 - d / (radius * 0.55), 0, 1) ** 0.8
    heat = np.clip(body * (0.5 + 0.35 * curl) + core * 0.75, 0, 1.2) * power
    if i >= 6:
        heat *= 0.6 + 0.4 * (curl > 0.45)
    ramp = [(0.0, (0, 0, 0)), (0.18, (120, 22, 6)), (0.42, (236, 92, 20)), (0.7, (255, 176, 60)),
            (0.92, (255, 236, 170)), (1.2, (255, 255, 236))]
    rgb = raster.ramp(ramp, np.clip(heat, 0, 1.2))
    steps = np.ceil(np.clip(heat, 0, 1) * 6) / 6                # stepped light, 6 levels
    rgb = rgb * (steps / np.maximum(np.clip(heat, 1e-6, 1), 1e-6))[..., None].clip(0, 1.4)
    rgb = rgb.reshape(w, S, w, S, 3).mean(axis=(1, 3))
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255)]).astype(np.uint8),
                                           "RGBA"))


SMOKE = np.array([(18, 16, 16), (40, 36, 34), (66, 60, 56), (96, 88, 82)], float)


def vent_smoke(i):
    w = VENT_GLOW
    S = 2
    t = (i + 0.5) / SMOKE_N
    rng = np.random.default_rng(1931)
    items = [(w / 2, w / 2, 9 + 12 * t ** 0.6, 0.85 * (1 - t) ** 1.1)]
    for k in range(5):
        a = rng.uniform(0, TAU)
        dist = 4 + rng.uniform(4, 12) * t
        items.append((w / 2 + math.cos(a) * dist - 3 * t, w / 2 + math.sin(a) * dist - 5 * t, 6 + 9 * t ** 0.6,
                      0.7 * (1 - t)))
    alpha, light, _ = su.blobs(w, w, S, items, 1932)
    rgb = SMOKE[np.clip(0.3 + light * 3.4, 0, 3).astype(int)]
    rgb, alpha = su.shrink(rgb, alpha, S)
    return su.stepped(rgb, alpha * 0.85, 4, 0.14)


# =========================================================================== gantry and cradle

GANTRY = (480, 48)
CRADLE = 64


def gantry_mats():
    return [
        Material(STEEL, metal=0.55, shininess=70, spec=0.7, pattern=rust_pattern(1.1, 0.12)),     # 0 chords, members
        Material(IRON, metal=0.4, shininess=40, spec=0.5, pattern=grating(2.0)),                 # 1 walkway grating
        Material(CONCRETE, metal=0.0, shininess=10, spec=0.1, pattern=rust_pattern(2.3, 0.1)),   # 2 tower footings
        Material(HAZARD, metal=0.1, shininess=20, spec=0.2, pattern=hazard_stripes(3.0)),        # 3 hazard paint
        Material(STEEL_L, metal=0.6, shininess=100, spec=1.0),                                   # 4 rails, bolts
        Material((0.25, 0.04, 0.03), emission=(1.5, 0.15, 0.08)),                                # 5 warning lamps
    ]


def gantry_scene(p):
    x0 = 240.0 - 14.0                                     # the towers' centres at +-226
    items = []
    for sx in (-1, 1):                                    # the end towers: concrete footing, steel column, cap
        cx = sx * x0
        items += [(sd_box(p, (cx, 0, 4.0), (13.0, 22.0, 4.0), 1.0), 2),
                  (sd_box(p, (cx, 0, 13.0), (9.0, 16.0, 6.0), 1.0), 0),
                  (sd_box(p, (cx, 0, 19.6), (10.5, 17.5, 0.8), 0.4), 3),
                  (sd_sphere(p, (cx - sx * 5.0, 12.0, 20.6), 1.3), 5)]
    span = x0 - 9.0
    for y in (-14.0, 14.0):                               # the two chords (box beams)
        items.append((sd_box(p, (0, y, 20.0), (span, 2.2, 2.0), 0.6), 0))
        items.append((sd_box(p, (0, y + (2.6 if y > 0 else -2.6), 22.4), (span, 0.5, 0.5), 0.2), 4))   # the rails
    bay = 32.0
    n = int(2 * span // bay)
    start = -n * bay / 2
    for k in range(n + 1):                                # cross members and the lacing
        x = start + k * bay
        items.append((sd_box(p, (x, 0, 19.5), (1.6, 13.0, 1.4), 0.4), 0))
        if k < n:
            a, b = (x, -13.0 if k % 2 else 13.0, 19.0), (x + bay, 13.0 if k % 2 else -13.0, 19.0)
            items.append((sd_capsule(p, a, b, 0.9), 0))
    items.append((sd_box(p, (0, 0, 21.4), (span, 4.0, 0.4)), 1))       # the walkway grating along the middle
    return union(*items)


def gantry_frame():
    return render(gantry_scene, gantry_mats(), GANTRY, z_top=40.0, steps=170)


def cradle_mats():
    return gantry_mats() + [Material((0.07, 0.07, 0.08), metal=0.6, shininess=80, spec=0.8),       # 6 turret ring
                            Material((0.95, 0.66, 0.16), metal=0.0, shininess=12, spec=0.1)]       # 7 chevron paint


def cradle_scene(p):
    """The deck: a half disc flaring up the screen (+y) on a short box, z 6..8; the lip striped; chevrons."""
    r = 29.0
    disc = sd_cylinder_z(p, (0, 0, 7.0), r, 1.2)
    disc = np.maximum(disc, -(p[:, 1] + 0.0))             # keep the upper half
    box = sd_box(p, (0, -9.0, 7.0), (r, 9.0, 1.2), 0.6)
    deck = np.minimum(disc, box)
    lip_d = np.maximum(sd_cylinder_z(p, (0, 0, 8.6), r, 0.5), -(sd_cylinder_z(p, (0, 0, 8.6), r - 3.0, 3.0)))
    lip_d = np.maximum(lip_d, -(p[:, 1] + 0.5))
    items = [(deck, 1), (lip_d, 3),
             (sd_cylinder_z(p, (0, 0, 8.4), 17.0, 0.6), 6),                # the turret ring
             (sd_cylinder_z(p, (0, 0, 8.9), 14.5, 0.4), 4)]
    for x in (-r + 1.5, r - 1.5):                          # the railing posts on the box's sides
        for y in (-16.0, -4.0):
            items.append((sd_cylinder_z(p, (x, y, 10.0), 0.8, 2.6), 4))
    for x in (-12.0, 12.0):                                # the hangers down to the beam below
        items.append((sd_box(p, (x, -26.0, 5.0), (2.0, 6.5, 1.2), 0.4), 0))
    for k, y in enumerate((20.5, 25.5)):                   # two chevrons pointing up the line
        poly = [(-9.0, y - 4.5), (0.0, y), (9.0, y - 4.5), (9.0, y - 2.3), (0.0, y + 2.2), (-9.0, y - 2.3)]
        items.append((sd_plate(p, poly, 8.5, 0.35), 7))
    return union(*items)


def cradle_frame():
    return render(cradle_scene, cradle_mats(), (CRADLE, CRADLE), z_top=24.0, steps=160)


# =========================================================================== the payroll safe and the office

SAFE = (36, 32)
SAFE_TILT = 0.85                                         # rad: three-quarter view, the door face toward the bottom
SAFE_SCALE = 0.7                                         # the model (24 x 16 x 18 units) to px
SAFE_GLOW_N = 8
DIAL_GREEN = (0.1, 0.4, 0.16)


def safe_mats(lamp=0.25, scorched=False):
    shell = rust_pattern(0.7, 0.1)
    if scorched:
        base = shell
        shell = (lambda p, n, b=base: b(p, n) * np.where(np.hypot(p[:, 0] - 2, p[:, 1] - 5) < 5.0, 0.45, 1.0))
    return [
        Material((0.38, 0.46, 0.42), metal=0.35, shininess=70, spec=0.7, pattern=shell),       # 0 the shell, green-grey
        Material((0.3, 0.37, 0.34), metal=0.4, shininess=80, spec=0.8),                        # 1 the door, darker
        Material((0.86, 0.7, 0.36), metal=0.6, shininess=140, spec=1.3),                       # 2 brass: dial, handle
        Material((0.05, 0.2, 0.08), emission=(0.2 * lamp, 1.5 * lamp, 0.4 * lamp)),             # 3 the door lamp
        Material((0.04, 0.04, 0.04), metal=0.0, shininess=4, spec=0.05),                       # 4 the dark inside
        Material((0.62, 0.7, 0.56), metal=0.0, shininess=8, spec=0.1,
                 pattern=lambda p, n: np.where((p[:, 0] * 1.4) % 1 < 0.2, 0.75, 1.0)),          # 5 scrip bundles
        Material((0.3, 0.29, 0.28), metal=0.2, shininess=20, spec=0.2),                        # 6 the plinth
    ]


LAMP_AT = (6.0, -9.3, 13.6)                              # on the door's top right, in the safe's own frame


def safe_scene(state=0):
    """A squat safe (x right, y away from the viewer, z up) 24 x 16 x 18 units (x 0.7 px) on a low plinth, its door on the front
    (-y) face; tilted toward the viewer so the door shows. State 2: the door swung open on its left hinge."""
    def scene(p):
        d, m = model(p / SAFE_SCALE)
        return d * SAFE_SCALE, m

    def model(p):
        q = rotate_x(p, SAFE_TILT)
        q[:, 1] = -q[:, 1]                         # the door (-y in the safe's frame) faces up the screen, lit
        lamp = (sd_sphere(q, LAMP_AT, 1.4), 3)
        body = sd_box(q, (0, 0, 9.5), (12.0, 8.0, 9.0), 1.4)
        items = [(sd_box(q, (0, 0, 0.6), (13.0, 9.0, 0.6), 0.3), 6)]
        if state < 2:
            items.append((body, 0))
            items.append((sd_box(q, (0, -8.3, 9.0), (9.5, 0.6, 7.2), 0.5), 1))
            if state == 0:
                items.append((sd_cylinder_y(q, (-2.5, -9.2, 10.0), 3.6, 0.7), 2))      # the dial
            items.append((sd_box(q, (5.0, -9.6, 7.6), (1.2, 1.0, 3.2), 0.5), 2))        # the handle
            items.append(lamp)
        else:
            shell = subtract((body, 0), sd_box(q, (0, -6.0, 9.0), (9.5, 4.0, 7.2)))
            items.append(shell)
            items.append((sd_box(q, (0, 0.4, 9.0), (9.4, 0.4, 7.0)), 4))
            for k, (x, z) in enumerate(((-4.5, 4.4), (1.0, 4.4), (5.6, 4.4), (-2.0, 9.4), (3.5, 9.4))):
                items.append((sd_box(q, (x, -2.8, z), (2.4, 2.4, 1.6), 0.4), 5))
            hinge = q - np.array([-9.8, -8.6, 0.0])                                 # the door, swung out 110 deg
            door = rotate_z(hinge, -1.9) + np.array([9.5, 0.0, 0.0])
            items.append((sd_box(door, (0, 0, 9.0), (9.5, 0.6, 7.2), 0.5), 1))
        return union(*items)
    return scene


def safe_frame(state):
    mats = safe_mats(0.25, scorched=state >= 1)
    return rimmed(render(safe_scene(state), mats, SAFE, z_top=40.0, steps=170, crisp=60))


def lamp_centre():
    """The lens's visible pixels in a probe render of the whole safe (only the lens lit)."""
    probe = [Material((0, 0, 0), emission=(1, 1, 1) if i == 3 else (0, 0, 0)) for i in range(len(safe_mats()))]
    hi, f = artkit.render_hi(safe_scene(0), probe, SAFE, float(SAFE[0]), z_top=40.0, shadows=False)
    ys, xs = np.nonzero(hi[..., 0] > 0.5)
    return (xs.mean() + 0.5) / f, (ys.mean() + 0.5) / f


def safe_glow(i, centre):
    level = (1.0, 0.85, 0.0, 0.0, 1.0, 0.0, 0.8, 0.0)[i]
    w, h = SAFE
    cv = v8.Canvas(w, h)
    d = cv.dist(*centre)
    cv.add((60, 255, 110), v8.gauss(d, 4.0) * 0.55 * level)
    cv.add((190, 255, 200), v8.gauss(d, 1.2) * 1.2 * level)
    return artkit.additive(cv.image())


OFFICE = (56, 84)


def office_mats():
    return [
        Material(CONCRETE, metal=0.0, shininess=10, spec=0.1, pattern=rust_pattern(0.2, 0.1)),        # 0 walls
        Material((0.36, 0.35, 0.33), metal=0.0, shininess=8, spec=0.1,
                 pattern=lambda p, n: np.where((np.abs(p[:, 0]) % 6 < 0.4) | (np.abs(p[:, 1]) % 6 < 0.4), 0.8, 1.0)),  # 1 tiles
        Material((0.46, 0.44, 0.4), metal=0.4, shininess=50, spec=0.5,
                 pattern=lambda p, n: np.where((p[:, 0] % 5) < 0.5, 0.7, 1.0)),                         # 2 lockers
        Material((0.36, 0.26, 0.18), metal=0.1, shininess=30, spec=0.3),                               # 3 the desk
        Material((0.04, 0.08, 0.1), emission=(0.1, 0.55, 0.7)),                                         # 4 terminal screen
        Material((0.15, 0.15, 0.16), metal=0.3, shininess=40, spec=0.4),                               # 5 the chair
        Material(HAZARD, metal=0.1, shininess=20, spec=0.2),                                           # 6 the door's sill
    ]


def office_scene(p):
    w, h, t = 26.0, 40.0, 2.0
    walls = sd_box(p, (0, 0, 3.0), (w, h, 3.0), 0.5)
    walls = np.maximum(walls, -sd_box(p, (0, 0, 3.0), (w - t, h - t, 5.0)))
    walls = np.maximum(walls, -sd_box(p, (w - 1.0, 6.0, 3.0), (3.0, 7.0, 5.0)))            # the door gap, east
    items = [(walls, 0), (sd_box(p, (0, 0, 0.3), (w - t, h - t, 0.3)), 1),
             (sd_box(p, (w - 1.0, 6.0, 0.4), (1.6, 7.0, 0.4)), 6)]
    for k in range(4):                                                                      # lockers on the north wall
        items.append((sd_box(p, (-15.0 + 7.0 * k, h - 5.2, 5.5), (3.2, 2.8, 5.0), 0.3), 2))
    items.append((sd_box(p, (8.0, 14.0, 3.4), (10.0, 5.0, 0.8), 0.3), 3))                   # the desk
    items.append((sd_box(p, (5.0, 15.5, 5.2), (4.0, 2.4, 1.0), 0.3), 4))                    # its terminal
    items.append((sd_cylinder_z(p, (7.0, 6.5, 2.2), 3.0, 0.8), 5))                          # the chair
    return union(*items)


def office_frame():
    return render(office_scene, office_mats(), OFFICE, z_top=20.0, steps=150)


# =========================================================================== ingots

INGOTS = (48, 40)
PALLET = (40, 32)


def ingot_mats(warm=0.0):
    return [
        Material((0.62, 0.62, 0.63), metal=0.75, shininess=120, spec=1.1,
                 pattern=lambda p, n: 0.92 + 0.08 * np.sin(p[:, 0] * 1.7 + p[:, 1] * 0.9)),           # 0 ingots
        Material((0.3, 0.3, 0.32), metal=0.4, shininess=40, spec=0.4, pattern=rust_pattern(0.9, 0.2)),  # 1 pallet
        Material(HAZARD, metal=0.2, shininess=40, spec=0.4),                                            # 2 strapping
        Material((0.5, 0.35, 0.28), metal=0.6, shininess=80, spec=0.7, emission=(1.2 * warm, 0.4 * warm, 0.1 * warm),
                 emission_pattern=lambda p, n: 0.6 + 0.4 * np.sin(p[:, 0] * 0.9)),                       # 3 warm ingots
    ]


def ingot(p, c, along_x, mat):
    """One cast ingot, a tapered bar 10 x 3.6 x 2.4 (the box's top narrower: two boxes)."""
    x, y, z = c
    if along_x:
        return union((sd_box(p, (x, y, z), (5.0, 1.8, 0.9), 0.4), mat), (sd_box(p, (x, y, z + 1.0), (4.5, 1.3, 0.5), 0.3), mat))
    return union((sd_box(p, (x, y, z), (1.8, 5.0, 0.9), 0.4), mat), (sd_box(p, (x, y, z + 1.0), (1.3, 4.5, 0.5), 0.3), mat))


def ingot_layers(p, cx, cy, layers, warm_top=False, strap=False):
    items = [(sd_box(p, (cx, cy, 1.0), (11.5, 11.5, 1.0), 0.3), 1)]
    for L in range(layers):
        z = 3.0 + L * 2.6
        mat = 3 if warm_top and L == layers - 1 else 0
        for k in range(3):
            off = -7.6 + 7.6 * k
            items.append(ingot(p, (cx, cy + off, z), True, mat) if L % 2 == 0 else ingot(p, (cx + off, cy, z), False, mat))
    if strap:
        top = 3.0 + layers * 2.6
        items.append((sd_box(p, (cx, cy, top / 2 + 0.6), (12.0, 0.6, top / 2 + 0.4), 0.2), 2))
    return union(*items)


def ingot_scene(variant):
    def scene(p):
        if variant == 0:
            return union(ingot_layers(p, -10.0, 3.0, 3), ingot_layers(p, 12.0, -4.0, 2))
        if variant == 1:
            return union(ingot_layers(p, -10.0, -3.0, 2, strap=True), ingot_layers(p, 12.0, 4.0, 2, strap=True))
        return union(ingot_layers(p, -10.0, 0.0, 2, warm_top=True), ingot_layers(p, 12.0, 0.0, 1, warm_top=True))
    return scene


def ingot_frame(variant):
    return render(ingot_scene(variant), ingot_mats(0.9 if variant == 2 else 0.0), INGOTS, z_top=30.0)


def pallet_frame():
    return render(lambda p: ingot_layers(p, 0.0, 0.0, 2, strap=True), ingot_mats(), PALLET, z_top=30.0)


# =========================================================================== the crews' rovers

ROVER = 48
ROVER_LIGHTS = 64
ROVER_HEADINGS = 16
LAMPS = ((16.0, 5.2, 7.0), (16.0, -5.2, 7.0))           # headlights in the rover's frame (x forward)
BEACON = (2.0, 0.0, 13.2)


def heading(k, n=ROVER_HEADINGS):
    """Screen angle (y down) of game heading k: 0 = down, clockwise."""
    return math.pi / 2 + TAU * k / n


def rover_mats(k):
    mats = [
        Material(OFF_WHITE, metal=0.2, shininess=40, spec=0.4,
                 pattern=lambda p, n: np.where(np.abs(np.abs(p[:, 1]) - 6.0) < 1.1, 0.86, 1.0)),      # 0 body
        Material(AMBER_PAINT, metal=0.25, shininess=40, spec=0.45),                                     # 1 amber bands
        Material((0.11, 0.11, 0.12), metal=0.1, shininess=10, spec=0.1),                                # 2 tyres
        Material((0.08, 0.12, 0.16), metal=0.8, shininess=120, spec=1.1),                               # 3 glass
        Material((0.4, 0.41, 0.43), metal=0.7, shininess=80, spec=0.8),                                 # 4 steel
        Material((0.3, 0.16, 0.04), emission=(1.2, 0.6, 0.12)),                                         # 5 beacon lens
        Material((0.6, 0.6, 0.55), emission=(0.8, 0.8, 0.7)),                                           # 6 headlamps
    ]
    return model_space_materials(mats, -heading(k))


def rover_scene(k):
    a = -heading(k)

    def scene(p):
        q = rotate_z(p, a)
        items = [(sd_box(q, (0, 0, 6.0), (15.0, 7.6, 3.0), 1.4), 0),
                 (sd_box(q, (9.5, 0, 10.0), (5.5, 7.0, 2.2), 1.2), 0),                 # the cab
                 (sd_box(q, (14.2, 0, 10.0), (1.2, 6.2, 1.4), 0.5), 3),                # the windscreen
                 (sd_cylinder_x(q, (-4.5, 0, 10.0), 5.6, 8.5), 0),                     # the pressurised module
                 (sd_box(q, (-4.5, 0, 10.0), (1.2, 7.4, 5.0), 0.4), 1),                # an amber band round it
                 (sd_box(q, (-11.5, 0, 10.0), (0.9, 7.0, 4.6), 0.4), 1),
                 (sd_cylinder_z(q, BEACON, 1.3, 1.0), 5),
                 (sd_box(q, (16.0, 0, 5.2), (0.8, 7.0, 0.9), 0.3), 4)]                 # the bumper
        for lx, ly, lz in LAMPS:
            items.append((sd_sphere(q, (lx, ly, lz - 1.6), 1.2), 6))
        for x in (-10.0, 0.0, 10.0):
            for y in (-8.2, 8.2):
                items.append((sd_cylinder_y(q, (x, y, 3.6), 3.6, 1.6), 2))
        return union(*items)
    return scene


def rover_frame(k):
    return render(rover_scene(k), rover_mats(k), (ROVER, ROVER), z_top=30.0, steps=150)


def rover_lights(k):
    """The two headlight beams (wedges forward along the heading) and the beacon, centred on the rover."""
    w = ROVER_LIGHTS
    cv = v8.Canvas(w, w)
    a = heading(k)
    d = np.array([math.cos(a), math.sin(a)])
    n = np.array([-d[1], d[0]])
    c = np.array([w / 2, w / 2])
    yy, xx = cv.y, cv.x
    for lx, ly, _ in LAMPS:
        src = c + d * lx + n * ly
        rx, ry = xx - src[0], yy - src[1]
        along = rx * d[0] + ry * d[1]
        across = rx * n[0] + ry * n[1]
        cone = (along > 0) * np.clip(1 - along / 15.0, 0, 1) * v8.gauss(across, 1.2 + along * 0.28)
        cv.add((255, 240, 200), cone * 0.55)
        cv.add((255, 250, 230), v8.gauss(np.hypot(rx, ry), 1.1) * 1.1)
    b = c + d * BEACON[0]
    cv.add((255, 170, 50), v8.gauss(np.hypot(xx - b[0], yy - b[1]), 2.4) * 0.8)
    return artkit.additive(cv.image())


# =========================================================================== build

def _job(job):
    kind, i = job
    if kind == "vent":
        return vent_frame(i)
    if kind == "vent-glow":
        return vent_glow(i)
    if kind == "flame":
        return vent_flame(i)
    if kind == "smoke":
        return vent_smoke(i)
    if kind == "gantry":
        return gantry_frame()
    if kind == "cradle":
        return cradle_frame()
    if kind == "safe":
        return safe_frame(i)
    if kind == "office":
        return office_frame()
    if kind == "ingots":
        return ingot_frame(i)
    if kind == "pallet":
        return pallet_frame()
    if kind == "rover":
        return rover_frame(i)
    if kind == "rover-lights":
        return rover_lights(i)
    raise ValueError(kind)


SUBJECTS = {
    "vent": [("vent", i) for i in range(4)] + [("vent-glow", i) for i in range(3)]
    + [("flame", i) for i in range(FLAME_N)] + [("smoke", i) for i in range(SMOKE_N)],
    "gantry": [("gantry", 0), ("cradle", 0)],
    "safe": [("safe", i) for i in range(3)] + [("office", 0)],
    "ingots": [("ingots", i) for i in range(3)] + [("pallet", 0)],
    "rover": [("rover", k) for k in range(ROVER_HEADINGS)] + [("rover-lights", k) for k in range(ROVER_HEADINGS)],
}


def build(subjects):
    jobs = [j for s in subjects for j in SUBJECTS[s]]
    with ProcessPoolExecutor() as pool:
        out = dict(zip(jobs, pool.map(_job, jobs)))

    def of(kind):
        return [out[j] for j in jobs if j[0] == kind]
    if "vent" in subjects:
        write("furnace-vent", artkit.quantize_set(of("vent"), 40))
        write("furnace-vent-glow", artkit.quantize_set(of("vent-glow"), 24))
        write("furnace-vent-flame", artkit.quantize_set(of("flame"), 32))
        write("furnace-vent-smoke", artkit.quantize_set(of("smoke"), 12))
    if "gantry" in subjects:
        write("gantry", artkit.quantize_set(of("gantry"), 40), single=True)
        write("gantry-cradle", artkit.quantize_set(of("cradle"), 40), single=True)
    if "safe" in subjects:
        centre = lamp_centre()
        write("payroll-safe", artkit.quantize_set(of("safe"), 40))
        write("payroll-safe-glow", artkit.quantize_set([safe_glow(i, centre) for i in range(SAFE_GLOW_N)], 12))
        write("shift-office", artkit.quantize_set(of("office"), 40), single=True)
    if "ingots" in subjects:
        sets = artkit.quantize_set(of("ingots") + of("pallet"), 40)
        for name, img in zip(("ingot-stack-a", "ingot-stack-b", "ingot-stack-c", "ingot-pallet"), sets):
            write(name, [img], single=True)
    if "rover" in subjects:
        write("crew-rover", artkit.quantize_set(of("rover"), 40))
        write("crew-rover-lights", artkit.quantize_set(of("rover-lights"), 16))


# =========================================================================== review

FW, FH = 480, 540
ZOOM = 0.75


def paste(img, sp, x, y):
    img.alpha_composite(sp, (int(round(x - sp.width / 2)), int(round(y - sp.height / 2))))


def light(img, glow, x, y):
    return artkit.add_light(img, glow, (int(round(x - glow.width / 2)), int(round(y - glow.height / 2))))


def aim_index(x0, y0, x1, y1, n=32):
    ang = math.atan2(y1 - y0, x1 - x0) - math.pi / 2
    return int(round((ang % TAU) / (TAU / n))) % n


class Field:
    """The play field at t from backdrop_l19 (ground, belts, pieces, smoke), and where a prop placed at
    (x, t_c) stands on the screen at t (its centre at mid-screen at t_c)."""

    def __init__(self):
        import backdrop_l19 as b19
        self.b = b19
        self.placed = [p for _, p in b19.placements()]

    def frame(self, t):
        return Image.fromarray(self.b.frame_at(t, self.placed), "RGB").convert("RGBA")

    def y(self, t_c, t):
        return self.b.MID - self.b.scroll_at(t_c) + self.b.scroll_at(t)


def turret(k):
    return Image.open(artkit.SPRITES / f"spine-turret_{k}.png").convert("RGBA")


SHIP = (240, 430)
ORB = None


def ship_img():
    return Image.open(artkit.SPRITES / "ship_2.png").convert("RGBA")


def clip_gantry(f, s, t):
    """The ore gantry at t~18: three up-facing nests on cradles at x 120, 240, 360; silent while above the ship,
    aiming up at it and firing once passed."""
    img = f.frame(t)
    gy = f.y(18.0, t)
    paste(img, s["gantry"][0], 240, gy)
    shots = []
    for x in (120, 240, 360):
        cy = gy - 26
        paste(img, s["gantry-cradle"][0], x, cy)
        passed = cy > SHIP[1] + 10
        k = aim_index(x, cy, *SHIP) if passed else 16
        paste(img, turret(k), x, cy)
        if passed:
            a = math.atan2(SHIP[1] - cy, SHIP[0] - x)
            for j in range(3):
                u = ((t * 2.5 + j / 3 + x / 97) % 1.0)
                dist = 20 + u * math.hypot(SHIP[0] - x, SHIP[1] - cy)
                shots.append((x + math.cos(a) * dist, cy + math.sin(a) * dist))
    paste(img, ship_img(), *SHIP)
    d = ImageDraw.Draw(img)
    for sx, sy in shots:
        d.ellipse([sx - 2.5, sy - 2.5, sx + 2.5, sy + 2.5], fill=(255, 90, 210, 255), outline=(255, 230, 250, 255))
    return img


def clip_safe(f, s, t):
    """The shift office at t~52 (x 76) with the payroll safe (x 60): the lamp blinking, then shot open."""
    img = f.frame(t)
    oy = f.y(52.0, t)
    paste(img, s["shift-office"][0], 76, oy)
    hit_at, open_at = 51.0, 51.6
    state = 0 if t < hit_at else 1 if t < open_at else 2
    paste(img, s["payroll-safe"][state], 60, oy + 22)
    if state < 2:
        step = int(t * 8) % 4 + (4 if state else 0)
        img = light(img, s["payroll-safe-glow"][step], 60, oy + 22)
    paste(img, ship_img(), 90, SHIP[1])
    if 50.2 < t < open_at:
        d = ImageDraw.Draw(img)
        for j in range(4):
            by = SHIP[1] - 20 - ((t * 600 + j * 30) % (SHIP[1] - oy - 10))
            d.rectangle([65 - 2, by, 65 + 1, by + 8], fill=(150, 230, 255, 255))
    return img


VENT_XS = (70, 150, 330, 410)


def clip_vents(f, s, t):
    """A vent row at t~70 (x 70, 150, 330, 410 between the stacks, the belt at 240): the ripple from the left, a grate
    0.4 s after the other, every 3.5 s: 1.2 s tell (3 glow steps), 1.0 s flame (8 frames), smoke."""
    img = f.frame(t)
    vy = f.y(70.0, t)
    for i, x in enumerate(VENT_XS):
        u = (t - 67.0 - 0.4 * i) % 3.5
        if u < 1.2:
            state, glow, flame = 1 + int(u / 0.4), int(u / 0.4), None
        elif u < 2.2:
            state, glow, flame = 3, None, min(FLAME_N - 1, int((u - 1.2) * 8))
        else:
            state, glow, flame = 0, None, None
        paste(img, s["furnace-vent"][state], x, vy)
        if glow is not None:
            img = light(img, s["furnace-vent-glow"][glow], x, vy)
        if flame is not None:
            img = light(img, s["furnace-vent-flame"][flame], x, vy)
        if 2.2 <= u < 2.2 + SMOKE_N / 8:
            paste(img, s["furnace-vent-smoke"][int((u - 2.2) * 8)], x, vy - 4)
    paste(img, ship_img(), 240, SHIP[1])
    return img


def clip_south(f, s, t):
    """The south gate's run-out at t~160: ingot stacks by the outbound belt at 240, pallets riding it up at 40 px/s,
    the crews' rovers driving up from the tower toward the gate."""
    img = f.frame(t)
    for name, x, tc in (("ingot-stack-a", 176, 158.0), ("ingot-stack-b", 306, 159.5), ("ingot-stack-c", 172, 161.5),
                        ("ingot-stack-a", 312, 162.5)):
        paste(img, s[name][0], x, f.y(tc, t))
    for tc in (157.0, 158.6, 160.2, 161.8):
        y = f.y(tc, t) - 40.0 * (t - 156.0)
        paste(img, s["ingot-pallet"][0], 240, y)
    for j, (x0, tc) in enumerate(((84, 159.0), (120, 160.0), (150, 161.0))):
        y = f.y(tc, t) - 70.0 * (t - 156.0)
        x = x0 + 6 * math.sin((t - 156.0) * 0.6 + j)
        k = 8 if abs(math.cos((t - 156.0) * 0.6 + j)) < 0.5 else (7 if math.cos((t - 156.0) * 0.6 + j) > 0 else 9)
        paste(img, s["crew-rover"][k], x, y)
        img = light(img, s["crew-rover-lights"][k], x, y)
    paste(img, ship_img(), 300, SHIP[1])
    return img


CLIPS = [(clip_gantry, 15.0, 4.6, "ORE GANTRY T~18: THE NESTS FIRE AFTER THEY PASS"),
         (clip_safe, 49.6, 3.0, "SHIFT OFFICE T~52: THE PAYROLL SAFE"),
         (clip_vents, 67.4, 4.4, "FURNACE ROW: A VENT ROW'S RIPPLE"),
         (clip_south, 156.0, 4.0, "SOUTH GATE: INGOTS AND THE CREWS' ROVERS")]


def gif_frames(s):
    f = Field()
    out = []
    for clip, t0, dur, label in CLIPS:
        for t in np.arange(t0, t0 + dur, 0.1):
            img = clip(f, s, float(t))
            img = img.resize((int(FW * ZOOM), int(FH * ZOOM)), Image.BOX)
            raster.draw_text(img, 6, 6, label, (255, 255, 255, 255), shadow=(0, 0, 0, 255))
            out.append(img.convert("RGB"))
    return out, f


def review():
    names = ["furnace-vent", "furnace-vent-glow", "furnace-vent-flame", "furnace-vent-smoke", "gantry",
             "gantry-cradle", "payroll-safe", "payroll-safe-glow", "shift-office", "ingot-stack-a", "ingot-stack-b",
             "ingot-stack-c", "ingot-pallet", "crew-rover", "crew-rover-lights"]
    s = {n: load(n) for n in names}
    missing = [n for n, v in s.items() if not v]
    if missing:
        raise SystemExit(f"missing sprites (run the build first): {missing}")
    artkit.REVIEW_ROUND = REVIEW_ROUND
    ingots = s["ingot-stack-a"] + s["ingot-stack-b"] + s["ingot-stack-c"] + s["ingot-pallet"]
    rows = [
        ("FURNACE VENT: SHUT, TELL 1-3", s["furnace-vent"], 3, False),
        ("VENT TELL GLOW 1-3 (ADDITIVE)", s["furnace-vent-glow"], 2, True),
        ("VENT FLAME COLUMN, 1.0 S AT 8 FPS (ADDITIVE, 44 PX ACROSS)", s["furnace-vent-flame"], 2, True),
        ("VENT SMOKE AFTER THE BURST", s["furnace-vent-smoke"], 2, False),
        ("GANTRY (THE ORE, CONVEYOR AND YARD GATE GANTRIES)", s["gantry"], 2, False),
        ("GANTRY CRADLE (UP-FACING NEST: THE DECK AND CHEVRONS POINT UP THE LINE)", s["gantry-cradle"], 3, False),
        ("PAYROLL SAFE: INTACT, HIT, OPENED", s["payroll-safe"], 4, False),
        ("SAFE LAMP (ADDITIVE): SLOW BLINK 0-3, AFTER A HIT 4-7", s["payroll-safe-glow"], 3, True),
        ("SHIFT OFFICE", s["shift-office"], 3, False),
        ("INGOT STACKS A B C, PALLET", ingots, 3, False),
        ("CREW ROVER, 16 HEADINGS", s["crew-rover"], 2, False),
        ("CREW ROVER LIGHTS (ADDITIVE), HEADINGS 0-7", s["crew-rover-lights"][:8], 1, True),
    ]
    sheet = artkit.review_sheet("LEVEL 19 PROPS - FINAL SPRITES", rows, width=1500, batch=BATCH)
    gif, f = gif_frames(s)
    picks = [12, 40, 52, 66, 74, 80, 116, 150]
    sw, sh = gif[0].width, gif[0].height
    strip = Image.new("RGBA", (sheet.width, sh + 40), raster.SHEET_BG)
    raster.draw_text(strip, 16, 6, "FRAMES FROM THE GIF (0.75X, OVER TOOLS/ART/BACKDROP_L19.PY'S PLAY FIELD)", raster.LABEL)
    x = 16
    for i in picks:
        if i < len(gif) and x + sw // 2 <= sheet.width - 16:
            fr = gif[i].resize((sw // 2 * 2 // 2, sh // 2 * 2 // 2), Image.BOX)
            strip.paste(fr.resize((sw * 2 // 3, sh * 2 // 3), Image.BOX), (x, 22))
            x += sw * 2 // 3 + 8
    full = Image.new("RGBA", (sheet.width, sheet.height + strip.height), raster.SHEET_BG)
    full.alpha_composite(sheet.convert("RGBA"), (0, 0))
    full.alpha_composite(strip, (0, sheet.height))
    artkit.save_review(full, gif, CONCEPT, "l19-props", fps=10)


def main(args):
    flags = [a for a in args if a.startswith("--")]
    subjects = [a for a in args if not a.startswith("--")] or list(SUBJECTS)
    if "--review" not in flags:
        build(subjects)
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
