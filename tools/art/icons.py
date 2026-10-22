#!/usr/bin/env python3
"""Production art: the hangar's equipment icons (design/ui/hangar), one per shop item of the
catalogue (design/player: weapons, generators, shields, plating, engines, utility modules,
specials) and Rook's escort craft, in two sizes.

Outputs (assets/sprites/icons/, packed onto the shared sprite pages as ``icons/<name>``):
  <name>.png         16x16, the shop list's rows
  <name>-large.png   24x24, the selected item and the schematic's callouts
  design/ui/hangar/concept/hangar-final-r13-a.png   review sheet: the tactical map and every icon

<name> is the weapon's slug, or ``<kind>-<the item's name as a slug>`` for the other kinds
(``generator-mk-ii-arc``, ``utility-pickup-magnet``), ``escort-rook`` for the escort; the game
derives the same names (vanguard.game.hangar.ItemIcons).

Every icon is one emblem style: a bevelled dark steel badge, light from the top-left, with the item
on it, ray-marched at 8x through the sprite path (1-bit alpha, unsharp mask, 32 colours). The items
with a model are the model: the five wing pods (tools/concept/vfx_r08.py with the Stormhawk's
production pod materials), the Hornet's missile, the mortar shell, the mine, Rook's craft. Guns
without a model show their barrel block and their shots in the firing direction (front guns up,
rear guns down or sideways); the parts and specials have one symbol per kind in the kind's colour
(generator yellow core, shield cyan, plating steel, engine orange, utility green, specials orange).
Graded parts (Mk I-VI, Composite I-V) carry their grade as amber studs along the badge's foot.

The Act 3 items (M7 part A: Ion Beam, Rear Lance, Deflector Pod, Light Drone Bay, EMP Burst, Orbital
Lance, Shield Overcharge) had no icon chosen in round 36: they are an a/b proposal of concept round 37
(``act3_symbol``); the user chose a (P.3 = a, 2026-10-13), written to assets/ and shown on
design/ui/hangar/concept/act3-icons-r37-a.png, the rejected b on concept/rejected/act3-icons-r37-b.png.

``--rear-guard`` (M7 part G batch, step ICON-G, concept round 43): only the Rear-guard Drone Bay's
icon (``rear-guard-drone-bay.png``/``-large.png``, in the Light Drone Bay's style: the bay's drone model
of tools/art/light_drone.py ``drone_rear``, here the trailing pair with their guns turned back, each
firing a pulse bolt down the badge) and its review crop
design/player/weapons/rear-guard-drone-bay/concept/rear-guard-icon-final-r43-a.png (beside the Light
Drone Bay's icon); the other icons are left as they are. A full run renders it too, with the same
Source note.

``--act4`` (M8 part A batch, step ART-A, concept round 50): only the four Act 4 items' icons, the round-48
picks as emblems: ``harpoon-torpedoes`` (gear_r48's barbed harpoon, drawn 1.7x as stout, with its teal
line), ``depth-charges`` (the navy drum over the arcs of its pressure dome), ``special-sonar-pulse`` (the
sweep's dashed double ring round a contact dot, the specials' orange) and ``escort-bulwark`` (the turtle
drone); review sheet design/ui/hangar/concept/act4-icons-final-r50-a.png. A full run includes them too
once their data lists them (``escort-bulwark`` always, beside ``escort-rook``).

Run: python3 tools/art/icons.py [--review] [--variant a|b]   (~20 s; --review only rebuilds the sheets,
--variant only that proposal sheet); python3 tools/art/icons.py --rear-guard [--review]   (~3 s);
python3 tools/art/icons.py --act4 [--review]   (~5 s)
"""
import re
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image

import artkit
import stormhawk
from artkit import DESIGN, ROOT, TAU, sprite

import ships_r02  # noqa: E402  (concept scripts, imported unchanged)
import vfx_r08 as v8  # noqa: E402
from render import models, raster, sdf  # noqa: E402
from render.palette import B  # noqa: E402
from render.sdf import rotate_z, sd_box, sd_capsule, sd_sphere, union, vec  # noqa: E402

SCRIPT = "icons.py"
SOURCE = artkit.source_note(SCRIPT, "UI batch")
OUT = artkit.SPRITES / "icons"
CONCEPT = DESIGN / "ui" / "hangar" / "concept"
ROUND = "r13"
SIZES = {"": 16, "-large": 24}
COLOURS = 32
PLAYER = DESIGN / "player"

# Kinds of the catalogue (vanguard.content.campaign.Catalogue) with their data file and list.
PARTS = [("generator", "generator", "models"), ("shield", "shields", "models"), ("plating", "armor", "plating"),
         ("engine", "systems", "engines"), ("utility", "systems", "utility"), ("special", "specials", "specials")]
ROMAN = {"I": 1, "II": 2, "III": 3, "IV": 4, "V": 5, "VI": 6}

HULL, ACC, SHOTS = B["UTC HULL"], B["UTC ACCENTS"], B["PLAYER SHOTS"]
BADGE = v8.mat((34, 40, 82), metal=0.35, shininess=50, spec=0.6)
STEEL = v8.m_metal(HULL[3])
DARK = v8.m_metal(HULL[1])
GLOWS = {"shot": v8.m_glow(SHOTS[2], 1.5), "core": v8.m_glow((255, 236, 140), 1.5),
         "shield": v8.m_glow((0, 200, 255), 1.1), "flame": v8.m_glow(ACC[4], 1.5),
         "util": v8.m_glow((64, 255, 128), 1.2), "special": v8.m_glow(ACC[4], 1.4),
         "stud": v8.m_glow((255, 224, 74), 1.3), "vrell": v8.m_glow((0, 255, 154), 1.3)}
MATS = [BADGE, STEEL, DARK] + list(GLOWS.values())
M_BADGE, M_STEEL, M_DARK = 0, 1, 2
M = {name: 3 + i for i, name in enumerate(GLOWS)}


def slug(text):
    return re.sub(r"[^a-z0-9]+", "-", text.lower()).strip("-")


def catalogue():
    """(icon name, label, symbol key, grade) of every shop item, in the catalogue's order."""
    items = []
    for data in sorted((PLAYER / "weapons").glob("*/data.yaml")):
        weapon = yaml.safe_load(data.read_text(encoding="utf-8"))
        items.append((data.parent.name, weapon["name"], data.parent.name, 0))
    for kind, folder, key in PARTS:
        for part in yaml.safe_load((PLAYER / folder / "data.yaml").read_text(encoding="utf-8"))[key]:
            name = part["name"]
            grade = re.search(r"\b(?:Mk|Composite) (VI|IV|V|I{1,3})\b", name)
            symbol = f"{kind}-{slug(name)}" if kind in ("utility", "special") else kind
            items.append((f"{kind}-{slug(name)}", name, symbol, ROMAN[grade.group(1)] if grade else 0))
    items.append(("escort-rook", "Rook (escort)", "escort-rook", 0))
    items.append(("escort-bulwark", "Bulwark (escort)", "escort-bulwark", 0))
    return items


# --------------------------------------------------------------------------- shapes (badge units)
# The badge spans -1..1; a scene returns (distance, material) in badge units, z up from the face.

def badge(p):
    return artkit.chamfered_box(p, (0, 0, -0.14), (0.97, 0.97, 0.14), 0.16)


def bolt(p, a, b, r=0.1):
    return sd_capsule(p, (*a, 0.12), (*b, 0.12), r)


def fan(p, base, angles, start, end, r=0.1):
    d = np.full(len(p), np.inf)
    for a in angles:
        direction = np.array([np.sin(np.radians(a)), np.cos(np.radians(a))])
        d = np.minimum(d, bolt(p, base + direction * start, base + direction * end, r))
    return d


def block(p, centre, half):
    return sd_box(p, (*centre, 0.08), (*half, 0.1), 0.04)


def ring(p, centre, radius, tube, z=0.1):
    q = p - vec(*centre, z)
    return np.sqrt((sdf.length(q[:, :2]) - radius) ** 2 + q[:, 2] ** 2) - tube


def arc(p, centre, radius, tube, half_angle):
    """A ring segment of ``half_angle`` either side of straight up."""
    q = p - vec(*centre, 0.1)
    angle = np.abs(np.arctan2(q[:, 0], q[:, 1]))
    on = sdf.length(np.stack([sdf.length(q[:, :2]) - radius, q[:, 2]], axis=-1)) - tube
    ends = [sdf.length(q - vec(s * radius * np.sin(half_angle), radius * np.cos(half_angle), 0)) - tube
            for s in (-1, 1)]
    return np.where(angle <= half_angle, on, np.minimum(*ends))


def plate(p, poly, z=0.08, half=0.1, round_=0.04):
    return sdf.sd_plate(p, poly, z, half, round_)


def guns(kind):
    """The guns without a model: barrel block and shots in the firing direction."""
    def scene(p):
        if kind == "pulse-cannon":
            shots = np.minimum(bolt(p, (-0.28, -0.2), (-0.28, 0.65)), bolt(p, (0.28, -0.2), (0.28, 0.65)))
            gun = block(p, (0, -0.62), (0.5, 0.18))
        elif kind == "scatter-vulcan":
            shots = fan(p, np.array([0.0, -0.55]), (-32, 0, 32), 0.35, 1.25, 0.09)
            gun = block(p, (0, -0.62), (0.3, 0.2))
        elif kind == "lance-laser":
            shots = bolt(p, (0, -0.4), (0, 0.82), 0.12)
            gun = np.minimum(block(p, (0, -0.62), (0.42, 0.16)), block(p, (0, -0.4), (0.16, 0.14)))
        elif kind == "fan-blaster":
            shots = fan(p, np.array([0.0, 0.55]), (148, 180, 212), 0.35, 1.25, 0.09)
            gun = block(p, (0, 0.62), (0.3, 0.2))
        elif kind == "side-splitter":
            shots = np.minimum(bolt(p, (-0.32, 0), (-0.85, 0)), bolt(p, (0.32, 0), (0.85, 0)))
            gun = block(p, (0, 0), (0.2, 0.36))
        else:  # tail-gun
            shots = np.minimum(bolt(p, (0, 0.2), (0, -0.25)), bolt(p, (0, -0.45), (0, -0.85)))
            gun = np.minimum(block(p, (0, 0.62), (0.4, 0.18)), block(p, (0, 0.38), (0.12, 0.12)))
        return union((shots, M["shot"]), (gun, M_STEEL))
    return scene


def generator(p):
    return union((ring(p, (0, 0), 0.52, 0.15), M_STEEL), (sd_sphere(p, (0, 0, 0.08), 0.3), M["core"]),
                 *[(block(p, (0.82 * s, 0), (0.12, 0.1)), M_DARK) for s in (-1, 1)])


def shield(p):
    outer = [(0, 0.85), (0.7, 0.55), (0.6, -0.25), (0, -0.85), (-0.6, -0.25), (-0.7, 0.55)]
    inner = [(x * 0.7, y * 0.7) for x, y in outer]
    return union((plate(p, outer, 0.04, 0.08), M_STEEL), (plate(p, inner, 0.12, 0.08), M["shield"]))


def plating(p):
    d = plate(p, [(-0.7, 0.7), (0.7, 0.7), (0.7, -0.7), (-0.7, -0.7)], 0.06, 0.1, 0.08)
    ribs = np.min([block(p, (0, y), (0.48, 0.05)) for y in (-0.25, 0.0, 0.25)], axis=0)
    rivets = np.min([sd_sphere(p, (0.52 * sx, 0.52 * sy, 0.16), 0.09) for sx in (-1, 1) for sy in (-1, 1)], axis=0)
    return union((d, M_STEEL), (np.maximum(ribs, -(p[:, 2] - 0.22)), M_DARK), (rivets, M_STEEL))


def engine(p):
    flame = np.minimum(sd_sphere(p, (0, 0.1, 0.06), 0.24), bolt(p, (0, 0.0), (0, -0.85), 0.14))
    return union((ring(p, (0, 0.1), 0.45, 0.14), M_STEEL), (flame, M["flame"]))


def utility(name):
    def scene(p):
        if name == "sensor-suite":
            return union((sd_sphere(p, (0, -0.55, 0.1), 0.16), M["util"]),
                         (arc(p, (0, -0.55), 0.55, 0.09, np.radians(48)), M["util"]),
                         (arc(p, (0, -0.55), 1.0, 0.09, np.radians(40)), M["util"]),
                         (block(p, (0, -0.8), (0.3, 0.08)), M_STEEL))
        if name == "pickup-magnet":
            legs = np.minimum(bolt(p, (-0.42, 0.0), (-0.42, 0.55), 0.17), bolt(p, (0.42, 0.0), (0.42, 0.55), 0.17))
            bow = np.where(p[:, 1] < 0.0, ring(p, (0, 0), 0.42, 0.17), np.inf)
            tips = np.minimum(bolt(p, (-0.42, 0.55), (-0.42, 0.78), 0.17), bolt(p, (0.42, 0.55), (0.42, 0.78), 0.17))
            return union((np.minimum(legs, bow), M_STEEL), (tips, M["util"]))
        if name == "salvage-scanner":
            return union((ring(p, (-0.15, 0.18), 0.45, 0.12), M_STEEL), (sd_sphere(p, (-0.15, 0.18, -0.05), 0.36), M["util"]),
                         (bolt(p, (0.2, -0.18), (0.68, -0.68), 0.13), M_STEEL))
        if name == "targeting-computer":
            ticks = np.min([bolt(p, (0.45 * dx, 0.45 * dy), (0.9 * dx, 0.9 * dy), 0.08)
                            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))], axis=0)
            return union((ring(p, (0, 0), 0.6, 0.08), M["util"]), (ticks, M["util"]),
                         (sd_sphere(p, (0, 0, 0.1), 0.13), M["util"]))
        if name == "evasive-thrusters":
            d = np.full(len(p), np.inf)
            for s in (-1, 1):
                for x in (0.25, 0.65):
                    d = np.minimum(d, np.minimum(bolt(p, (s * x, 0), (s * (x - 0.3), 0.4), 0.1),
                                                 bolt(p, (s * x, 0), (s * (x - 0.3), -0.4), 0.1)))
            return union((d, M["util"]), (block(p, (0, 0), (0.1, 0.5)), M_STEEL))
        if name == "auto-repair-nanites":
            cross = np.minimum(block(p, (0, 0), (0.18, 0.55)), block(p, (0, 0), (0.55, 0.18)))
            return union((ring(p, (0, 0), 0.78, 0.1), M_STEEL), (cross, M["util"]))
        if name == "pressure-hull":
            hexagon = [(0.8 * np.cos(a), 0.8 * np.sin(a)) for a in np.radians(np.arange(30, 390, 60))]
            return union((plate(p, hexagon, 0.06, 0.1, 0.06), M_STEEL), (sd_sphere(p, (0, 0, 0.12), 0.26), M["util"]))
        # ascendancy-iff-spoofer: a Vrell eye
        lens = np.maximum(sd_sphere(p, (0, -0.55, -0.6), 1.05), sd_sphere(p, (0, 0.55, -0.6), 1.05))
        return union((lens, M_DARK), (sd_sphere(p, (0, 0, 0.2), 0.24), M["vrell"]))
    return scene


def special(name):
    def scene(p):
        if name == "airstrike":
            pts = [(5, 0), (7, 4), (10, 6), (10, 7), (6, 6), (6, 9), (8, 10), (2, 10), (4, 9), (4, 6), (0, 7), (0, 6), (3, 4)]
            jet = plate(p, [((x - 5) / 6, (5 - y) / 6) for x, y in pts], 0.08, 0.1, 0.03)
            return union((jet, M_STEEL), (sd_sphere(p, (0, -0.7, 0.1), 0.12), M["special"]))
        if name == "smart-bomb":
            rays = np.min([bolt(p, (0.55 * np.cos(a), 0.55 * np.sin(a)), (0.9 * np.cos(a), 0.9 * np.sin(a)), 0.08)
                           for a in np.radians(np.arange(45, 405, 90))], axis=0)
            body = sd_sphere(p, (0, 0, 0.0), 0.48)
            band = np.maximum(body, np.abs(p[:, 1]) - 0.09)
            return union((body, M_DARK), (band - 0.02, M["special"]), (rays, M["special"]))  # the band stands proud
        # decoy-flares
        flares = np.min([np.minimum(sd_sphere(p, (x, y, 0.12), 0.17), bolt(p, (x, y), (x - 0.18, y - 0.55), 0.07))
                         for x, y in ((-0.45, 0.5), (0.1, 0.65), (0.6, 0.35))], axis=0)
        return union((flares, M["special"]), (block(p, (0, -0.7), (0.45, 0.12)), M_STEEL))
    return scene


def studs(p, grade):
    """The grade's amber studs along the badge's foot."""
    pitch = 0.27
    xs = (np.arange(grade) - (grade - 1) / 2) * pitch
    return np.min([sd_sphere(p, (x, -0.8, 0.0), 0.1) for x in xs], axis=0)


# --------------------------------------------------------------------------- models on the badge

def model_symbol(scene, mats, span, angle=-45.0, centre=(0.0, 0.0, 0.0)):
    """A model (in its own units, ``span`` of them filling the badge's symbol area of 1.5) turned
    by ``angle`` degrees about z before it is lit. Returns (scene in badge units, materials)."""
    k = 1.5 / span
    offset = len(MATS)
    rad = np.radians(angle)

    def s(p):
        q = rotate_z(p, rad) / k + vec(*centre)
        q[:, 2] -= 0.12 / k
        d, m = scene(q)
        return d * k, m + offset
    return s, mats


def pod(kind, span=0.55, angle=-45.0):
    """A wing pod of the Stormhawk's (the concept's model at its mount, with the production pod
    materials), centred on the badge."""
    return model_symbol(v8._pod_scene(kind), stormhawk.POD_MATS, span, angle, tuple(v8.POD_AT))


MODELS = {
    "autocannon-pod": lambda: pod("autocannon"), "micro-missile-pod": lambda: pod("micro-missile"),
    "bomb-rack": lambda: pod("bomb-rack"), "swivel-gun": lambda: pod("swivel", 0.42, 0.0),
    "torpedo-pod": lambda: pod("torpedo"),
    "hornet-launcher": lambda: model_symbol(*v8.missile_model(), 1.45),
    "hammer-mortar": lambda: model_symbol(*v8.shell_model(), 1.6, 0.0),
    "proximity-mines": lambda: model_symbol(*v8.mine_model(), 1.7, 0.0),
    "escort-rook": lambda: model_symbol(*models.ship_c_model(palette=ships_r02.ROOK_SCHEMES["a"][1]), 1.6, 0.0),
}
GUNS = ("pulse-cannon", "scatter-vulcan", "lance-laser", "fan-blaster", "side-splitter", "tail-gun")


# --------------------------------------------------------------------------- the Act 3 items (M7 part A)
# Round 36 chose no icons for them, so they were an a/b proposal of concept round 37 (``--variant b``
# renders b's sheet only); the user chose a (P.3, 2026-10-13), the one written to assets/.

def _with(model, extra_scene):
    """A model on the badge plus badge-unit shapes: (scene, extra materials)."""
    inner, mats = model

    def scene(p):
        return union(inner(p), *extra_scene(p))
    return scene, mats


def _deflector_pod(v):
    import deflector_pod
    scene, mats = deflector_pod.pod_model(v)
    return model_symbol(scene, mats, 0.62, 0.0, (0.0, -0.06, 0.0))


def _drone():
    import gear_r36
    scene, mats = gear_r36.drone_a()
    return model_symbol(scene, mats, 1.6, 0.0)


def _rear_guard_pair():
    """The Rear-guard Drone Bay: two of its drones (light_drone.drone_rear, gun at the model's +y)
    trailing side by side with their guns turned back (down the badge), each firing a pulse bolt."""
    import light_drone
    scene, mats = light_drone.drone_rear()
    one, _ = model_symbol(scene, mats, 2.6, 180.0)
    spots = ((-0.44, 0.36), (0.44, 0.36))

    def pair(p):
        d, m = one(p - vec(*spots[0], 0.0))
        for x, y in spots[1:]:
            d2, m2 = one(p - vec(x, y, 0.0))
            m = np.where(d2 < d, m2, m)
            d = np.minimum(d, d2)
        shots = np.min([bolt(p, (x, y - 0.5), (x, y - 1.06), 0.075) for x, y in spots], axis=0)
        return union((d, m), (shots, M["shot"]))
    return pair, mats


def act3_symbol(symbol, v):
    """(scene, extra materials) of an Act 3 item's emblem in variant v, or None."""
    if symbol == "ion-beam":
        def scene(p):
            if v == "a":   # the gun block and a thick beam with a white core and a contact star
                beam = bolt(p, (0, -0.42), (0, 0.6), 0.2)
                star = np.minimum(bolt(p, (-0.38, 0.66), (0.38, 0.66), 0.06), sd_sphere(p, (0, 0.66, 0.14), 0.17))
                return union((beam, M["shot"]), (star, M["core"]), (block(p, (0, -0.64), (0.46, 0.16)), M_STEEL))
            beam = bolt(p, (0, -0.2), (0, 0.85), 0.15)  # b: an emitter ring with the beam out of it
            return union((ring(p, (0, -0.42), 0.32, 0.1), M_STEEL), (sd_sphere(p, (0, -0.42, 0.08), 0.16), M["shot"]),
                         (beam, M["shot"]))
        return scene, []
    if symbol == "rear-lance":
        def scene(p):
            if v == "a":   # the Lance Laser's emblem turned round: block at the top, the lance downwards
                shots = bolt(p, (0, 0.4), (0, -0.82), 0.12)
                gun = np.minimum(block(p, (0, 0.62), (0.42, 0.16)), block(p, (0, 0.4), (0.16, 0.14)))
                return union((shots, M["shot"]), (gun, M_STEEL))
            shots = np.minimum(bolt(p, (0, 0.1), (0, -0.85), 0.11), bolt(p, (-0.42, 0.3), (-0.42, -0.4), 0.06))
            shots = np.minimum(shots, bolt(p, (0.42, 0.3), (0.42, -0.4), 0.06))  # b: one lance piercing two marks
            return union((shots, M["shot"]), (block(p, (0, 0.6), (0.3, 0.2)), M_STEEL))
        return scene, []
    if symbol == "deflector-pod":
        if v == "a":       # the pod with its field's arc beside it
            return _with(_deflector_pod("a"), lambda p: [(arc(rotate_z(p, -np.pi / 2), (0.0, -0.1), 0.62, 0.07,
                                                              np.radians(62)), M["shield"])])

        def scene(p):      # b: the field's arc catching a bolt
            hit = np.minimum(sd_sphere(p, (0.22, 0.0, 0.12), 0.14), bolt(p, (0.25, 0.0), (0.8, 0.45), 0.07))
            return union((arc(rotate_z(p, -np.pi / 2), (0.0, 0.25), 0.75, 0.1, np.radians(58)), M["shield"]),
                         (hit, M["shot"]), (block(p, (-0.62, 0), (0.14, 0.42)), M_STEEL))
        return scene, []
    if symbol == "light-drone-bay":
        if v == "a":       # the disc drone itself
            return _drone()
        return _with(model_symbol(*__import__("gear_r36").drone_a(), 2.6, 0.0),
                     lambda p: [(arc(p, (0, 0), 0.72, 0.06, np.radians(150)), M["util"])])
    if symbol == "special-emp-burst":
        def scene(p):
            zig = [(-0.5, 0.55), (0.1, 0.1), (-0.15, -0.05), (0.45, -0.6)]
            bolt_ = np.min([bolt(p, a, b, 0.09) for a, b in zip(zig[:-1], zig[1:])], axis=0)
            if v == "a":   # a ring with a lightning bolt across it
                return union((ring(p, (0, 0), 0.72, 0.08), M["special"]), (bolt_, M["special"]))
            arcs_ = np.minimum(arc(p, (0, -0.5), 0.6, 0.07, np.radians(45)), arc(p, (0, -0.5), 1.05, 0.07, np.radians(38)))
            return union((sd_sphere(p, (0, -0.5, 0.06), 0.2), M["special"]), (arcs_, M["special"]))
        return scene, []
    if symbol == "special-orbital-lance":
        def scene(p):
            target = ring(p, (0, -0.55), 0.3, 0.07)
            if v == "a":   # a column striking a target ring
                return union((bolt(p, (0, 0.85), (0, -0.45), 0.17), M["special"]), (target, M["special"]),
                             (block(p, (0, 0.82), (0.36, 0.08)), M_STEEL))
            sat = np.minimum(block(p, (0, 0.62), (0.13, 0.13)), block(p, (0, 0.62), (0.62, 0.06)))
            return union((sat, M_STEEL), (bolt(p, (0, 0.45), (0, -0.4), 0.1), M["special"]), (target, M["special"]))
        return scene, []
    if symbol == "special-shield-overcharge":
        outer = [(0, 0.85), (0.7, 0.55), (0.6, -0.25), (0, -0.85), (-0.6, -0.25), (-0.7, 0.55)]

        def scene(p):
            if v == "a":   # two shield shells, one inside the other
                inner = [(x * 0.62, y * 0.62) for x, y in outer]
                shells = np.maximum(plate(p, outer, 0.06, 0.08), -plate(p, [(x * 0.82, y * 0.82) for x, y in outer], 0.06, 0.2))
                return union((shells, M["special"]), (plate(p, inner, 0.08, 0.08), M["special"]))
            up = np.minimum(bolt(p, (0, -0.35), (0, 0.4), 0.1), np.minimum(bolt(p, (0, 0.45), (-0.28, 0.15), 0.09),
                                                                            bolt(p, (0, 0.45), (0.28, 0.15), 0.09)))
            return union((plate(p, outer, 0.04, 0.08), M_STEEL), (up, M["special"]))
        return scene, []
    return None


# --------------------------------------------------------------------------- the Act 4 items (M8 part A)
# The round-48 picks as emblems (concept round 50): the harpoon and the drum of tools/concept/gear_r48.py
# (variant a), the Bulwark's turtle (a), and the Sonar Pulse's dashed double ring (G3 b) in the specials'
# orange; ``--act4`` builds only these.

M8_TEAL = v8.m_glow((150, 255, 235), 1.4)      # gear_r48.TEAL_W: the harpoon's line, the charge's ring


def _harpoon():
    """Harpoon Torpedoes: the barbed harpoon (drawn 1.7x as stout, so it reads at 16 px) diagonally
    across the badge, its line of light trailing in a slack wave."""
    import gear_r48
    scene, mats = gear_r48.harpoon_model("a")

    def stout(p):
        q = p.copy()
        q[:, 0] /= 1.7
        q[:, 2] /= 1.7
        d, m = scene(q)
        return d * 1.0, m
    inner, extra = model_symbol(stout, mats, 2.0, -45.0, (0.0, -0.12, 0.0))
    teal = len(MATS) + len(extra)
    lead = np.array([np.sin(np.radians(45)), np.cos(np.radians(45))])   # the nose's direction on the badge
    side = np.array([lead[1], -lead[0]])

    def symbol(p):
        start = -lead * 0.7
        pts = [start - lead * 0.11 * k + side * 0.1 * np.sin(k * 1.6) * (k / 4) for k in range(5)]
        line = np.min([bolt(p, tuple(a), tuple(b), 0.075) for a, b in zip(pts[:-1], pts[1:])], axis=0)
        return union(inner(p), (line, teal))
    return symbol, extra + [M8_TEAL]


def _depth_charge():
    """Depth Charges: the navy drum with its yellow bands and lamp over the arc of its pressure dome."""
    import gear_r48
    scene, mats = gear_r48.charge_model("a")
    inner, extra = model_symbol(scene, mats, 1.3, -25.0, (0.0, -0.1, 0.0))
    teal = len(MATS) + len(extra)

    def symbol(p):
        dome = np.minimum(arc(rotate_z(p, np.pi), (0, 0.12), 0.78, 0.075, np.radians(62)),
                          arc(rotate_z(p, np.pi), (0, 0.12), 0.52, 0.06, np.radians(48)))
        return union(inner(p), (dome, teal))
    return symbol, extra + [M8_TEAL]


def _sonar(p):
    """Sonar Pulse: the sweep's dashed double ring round a contact dot (the chosen echo marks)."""
    q = p - vec(0, 0, 0.1)
    angle = np.arctan2(q[:, 1], q[:, 0]) + np.pi / 4
    rr = sdf.length(q[:, :2])
    rings = np.full(len(p), np.inf)
    for radius, dashes, tube in ((0.46, 4, 0.08), (0.84, 8, 0.085)):
        gap = np.abs((angle * dashes / TAU) % 1.0 - 0.5) > 0.3
        rings = np.minimum(rings, np.where(gap, np.inf, np.sqrt((rr - radius) ** 2 + q[:, 2] ** 2) - tube))
    return union((rings, M["special"]), (sd_sphere(p, (0, 0, 0.12), 0.19), M["special"]))


def _bulwark():
    """The Bulwark: the turtle drone itself, nose up."""
    import gear_r48
    return model_symbol(*gear_r48.bulwark_model("a"), 1.8, 0.0, (0.0, 0.06, 0.0))


M8_BATCH = {"harpoon-torpedoes": _harpoon, "depth-charges": _depth_charge,
            "special-sonar-pulse": lambda: (_sonar, []), "escort-bulwark": _bulwark}
M8_SOURCE = artkit.source_note(SCRIPT, "M8 part A batch")
M8_REVIEW = CONCEPT / "act4-icons-final-r50-a.png"

G_BATCH = {"rear-guard-drone-bay": _rear_guard_pair}   # M7 part G (step ICON-G, round 43)
G_SOURCE = artkit.source_note(SCRIPT, "M7 part G batch")
G_CONCEPT = PLAYER / "weapons" / "rear-guard-drone-bay" / "concept"
ACT3 = ("ion-beam", "rear-lance", "deflector-pod", "light-drone-bay", "special-emp-burst",
        "special-orbital-lance", "special-shield-overcharge")
VARIANT = "a"
CHOSEN = "a"   # the user's pick, round 37 P.3 (2026-10-13); the other sheet goes to concept/rejected/
SYMBOLS = {"generator": generator, "shield": shield, "plating": plating, "engine": engine}


def emblem(symbol, grade):
    """(scene in badge units, materials) of one icon."""
    mats = list(MATS)
    act3 = act3_symbol(symbol, VARIANT) if symbol in ACT3 else None
    if act3 is not None:
        inner, extra = act3
        mats += extra
    elif symbol in M8_BATCH:
        inner, extra = M8_BATCH[symbol]()
        mats += extra
    elif symbol in G_BATCH:
        inner, extra = G_BATCH[symbol]()
        mats += extra
    elif symbol in MODELS:
        inner, extra = MODELS[symbol]()
        mats += extra
    elif symbol in GUNS:
        inner = guns(symbol)
    elif symbol in SYMBOLS:
        inner = SYMBOLS[symbol]
    elif symbol.startswith("utility-"):
        inner = utility(symbol.removeprefix("utility-"))
    else:
        inner = special(symbol.removeprefix("special-"))

    def scene(p):
        q = p
        if grade:
            q = p.copy()
            q[:, :2] = (q[:, :2] - vec(0, 0.14)[:2]) / 0.84
        d, m = inner(q)
        if grade:
            d = d * 0.84
        items = [(badge(p), M_BADGE), (d, m)]
        if grade:
            items.append((studs(p, grade), M["stud"]))
        return union(*items)
    return scene, mats


def render(job):
    global VARIANT
    name, symbol, grade, size, *variant = job
    VARIANT = variant[0] if variant else "a"
    scene, mats = emblem(symbol, grade)
    half = size / 2

    def px(p):
        d, m = scene(p / half)
        return d * half, m
    hi = sdf.render(px, mats, (size * 8, size * 8), float(size), z_top=size * 0.5)
    img = artkit.native(hi, 8)
    return artkit.quantize_set([img], COLOURS)[0]


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()
    jobs = [(name + suffix, symbol, grade, size) for name, _, symbol, grade in catalogue()
            for suffix, size in SIZES.items()]
    with ProcessPoolExecutor() as pool:
        for job, img in zip(jobs, pool.map(render, jobs)):
            artkit.save_png(img, OUT / f"{job[0]}.png", G_SOURCE if job[1] in G_BATCH
                            else M8_SOURCE if job[1] in M8_BATCH else SOURCE)
    print(f"{len(jobs)} icons ({len(jobs) // len(SIZES)} items x {len(SIZES)} sizes)")


# --------------------------------------------------------------------------- review

def review():
    items = catalogue()
    hangar_map = Image.open(ROOT / "assets" / "ui" / "hangar-map.png").convert("RGBA")
    cols, cell = 6, 200
    rows = -(-len(items) // cols)
    sheet = raster.sheet(16 + max(960, cols * cell) + 16, 60 + 540 + 30 + rows * 70 + 20,
                         "HANGAR (FINAL R13): TACTICAL MAP AND EQUIPMENT ICONS", f"PRODUCTION ART, UI BATCH - {ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, f"TACTICAL MAP 960X540 AT 1X ({artkit.colour_count([hangar_map])} COLOURS), "
                                    "BEHIND THE HANGAR'S GLASS PANELS", raster.LABEL)
    sheet.alpha_composite(hangar_map, (16, 52))
    y0 = 52 + 540 + 18
    raster.draw_text(sheet, 16, y0, f"{len(items)} ICONS: 16X16 (SHOP ROWS) AT 1X AND 3X, 24X24 (SELECTED, CALLOUTS) AT 1X AND 2X",
                     raster.LABEL)
    for i, (name, label, _, _) in enumerate(items):
        x, y = 16 + (i % cols) * cell, y0 + 16 + (i // cols) * 70
        small = Image.open(OUT / f"{name}.png").convert("RGBA")
        large = Image.open(OUT / f"{name}-large.png").convert("RGBA")
        plate_ = Image.new("RGBA", (cell - 8, 62), (6, 8, 26, 255))
        plate_.alpha_composite(small, (2, 2))
        plate_.alpha_composite(sprite.enlarge(small, 3), (22, 2))
        plate_.alpha_composite(large, (74, 2))
        plate_.alpha_composite(sprite.enlarge(large, 2), (102, 2))
        sheet.alpha_composite(plate_, (x, y))
        raster.draw_text(sheet, x + 2, y + 52, label.upper().replace('"', "")[:30], raster.LABEL_DIM)
    path = CONCEPT / f"hangar-final-{ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


def act3_review(v):
    """The round-37 proposal sheet of the Act 3 items' icons in variant v (CHOSEN is the one in assets/)."""
    labels = {name: label for name, label, _, _ in catalogue()}
    jobs = [(name + suffix, name, 0, size, v) for name in ACT3 for suffix, size in SIZES.items()]
    with ProcessPoolExecutor() as pool:
        icons = dict(zip([j[0] for j in jobs], pool.map(render, jobs)))
    cell = 230
    sheet = raster.sheet(16 + 4 * cell + 16, 60 + 2 * 96 + 20, f"ACT 3 EQUIPMENT ICONS - PROPOSAL {v.upper()} (ROUND 37)",
                         "PRODUCTION ART, M7 PART A BATCH - R37")
    raster.draw_text(sheet, 16, 38, "16X16 (SHOP ROWS, HUD SPECIAL ROW) AT 1X AND 3X, 24X24 (SELECTED, CALLOUTS) AT 1X AND 2X",
                     raster.LABEL)
    for i, name in enumerate(ACT3):
        x, y = 16 + (i % 4) * cell, 56 + (i // 4) * 96
        small, large = icons[name], icons[name + "-large"]
        plate_ = Image.new("RGBA", (cell - 8, 80), (6, 8, 26, 255))
        plate_.alpha_composite(small, (2, 2))
        plate_.alpha_composite(sprite.enlarge(small, 3), (22, 2))
        plate_.alpha_composite(large, (74, 2))
        plate_.alpha_composite(sprite.enlarge(large, 2), (102, 2))
        sheet.alpha_composite(plate_, (x, y))
        raster.draw_text(sheet, x + 2, y + 66, labels.get(name, name).upper()[:30], raster.LABEL_DIM)
    folder = CONCEPT if v == CHOSEN else CONCEPT / "rejected"
    folder.mkdir(parents=True, exist_ok=True)
    path = folder / f"act3-icons-r37-{v}.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


def build_g():
    """The M7 part G batch only: writes its icons, leaves every other icon as it is."""
    OUT.mkdir(parents=True, exist_ok=True)
    jobs = [(name + suffix, name, 0, size) for name in G_BATCH for suffix, size in SIZES.items()]
    with ProcessPoolExecutor() as pool:
        for job, img in zip(jobs, pool.map(render, jobs)):
            artkit.save_png(img, OUT / f"{job[0]}.png", G_SOURCE)
    print(f"{len(jobs)} icons (M7 part G batch)")


def review_g():
    """Review crop: the Rear-guard Drone Bay's icon beside the Light Drone Bay's, at 1x and enlarged."""
    labels = {name: label for name, label, _, _ in catalogue()}
    names = ("rear-guard-drone-bay", "light-drone-bay")
    cell = 350
    sheet = raster.sheet(16 + len(names) * cell + 16, 60 + 112 + 12, "REAR-GUARD BAY ICON (FINAL R43)",
                         "PRODUCTION ART, M7 PART G BATCH - R43")
    raster.draw_text(sheet, 16, 38, "16X16 AT 1X AND 4X, 24X24 AT 1X AND 3X; THE LIGHT DRONE BAY'S FOR THE STYLE", raster.LABEL)
    for i, name in enumerate(names):
        x, y = 16 + i * cell, 56
        small = Image.open(OUT / f"{name}.png").convert("RGBA")
        large = Image.open(OUT / f"{name}-large.png").convert("RGBA")
        plate_ = Image.new("RGBA", (cell - 8, 104), (6, 8, 26, 255))
        plate_.alpha_composite(small, (2, 2))
        plate_.alpha_composite(sprite.enlarge(small, 4), (22, 2))
        plate_.alpha_composite(large, (92, 2))
        plate_.alpha_composite(sprite.enlarge(large, 3), (120, 2))
        sheet.alpha_composite(plate_, (x, y))
        raster.draw_text(sheet, x + 2, y + 92, labels.get(name, name).upper()[:34], raster.LABEL_DIM)
    path = G_CONCEPT / "rear-guard-icon-final-r43-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


def build_m8():
    """The M8 part A batch only: writes its four icons, leaves every other icon as it is."""
    OUT.mkdir(parents=True, exist_ok=True)
    jobs = [(name + suffix, name, 0, size) for name in M8_BATCH for suffix, size in SIZES.items()]
    with ProcessPoolExecutor() as pool:
        for job, img in zip(jobs, pool.map(render, jobs)):
            artkit.save_png(img, OUT / f"{job[0]}.png", M8_SOURCE)
    print(f"{len(jobs)} icons (M8 part A batch)")


def review_m8():
    """Review sheet: the four Act 4 icons at 1x and enlarged, beside the icons they sit next to."""
    labels = {"harpoon-torpedoes": "HARPOON TORPEDOES", "depth-charges": "DEPTH CHARGES",
              "special-sonar-pulse": "SONAR PULSE", "escort-bulwark": "BULWARK (ESCORT)",
              "torpedo-pod": "TORPEDO POD (FOR STYLE)", "proximity-mines": "PROXIMITY MINES (FOR STYLE)",
              "special-emp-burst": "EMP BURST (FOR STYLE)", "escort-rook": "ROOK (FOR STYLE)"}
    names = list(M8_BATCH) + ["torpedo-pod", "proximity-mines", "special-emp-burst", "escort-rook"]
    cell = 260
    sheet = raster.sheet(16 + 4 * cell + 16, 60 + 2 * 120 + 12, "ACT 4 EQUIPMENT ICONS (FINAL R50)",
                         "PRODUCTION ART, M8 PART A BATCH - R50")
    raster.draw_text(sheet, 16, 38, "16X16 (SHOP ROWS, HUD SPECIAL ROW) AT 1X AND 4X, 24X24 (SELECTED, CALLOUTS) AT 1X AND 3X;"
                                    " BOTTOM ROW: EXISTING ICONS FOR THE STYLE", raster.LABEL)
    for i, name in enumerate(names):
        x, y = 16 + (i % 4) * cell, 56 + (i // 4) * 120
        small = Image.open(OUT / f"{name}.png").convert("RGBA")
        large = Image.open(OUT / f"{name}-large.png").convert("RGBA")
        plate_ = Image.new("RGBA", (cell - 8, 104), (6, 8, 26, 255))
        plate_.alpha_composite(small, (2, 2))
        plate_.alpha_composite(sprite.enlarge(small, 4), (22, 2))
        plate_.alpha_composite(large, (92, 2))
        plate_.alpha_composite(sprite.enlarge(large, 3), (120, 2))
        sheet.alpha_composite(plate_, (x, y))
        raster.draw_text(sheet, x + 2, y + 108, labels[name], raster.LABEL_DIM)
    M8_REVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(M8_REVIEW, optimize=True)
    print(f"review: {M8_REVIEW.relative_to(ROOT)}")


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--act4" in args:
        if "--review" not in args:
            build_m8()
        review_m8()
        sys.exit()
    if "--rear-guard" in args:
        if "--review" not in args:
            build_g()
        review_g()
        sys.exit()
    if "--variant" in args:
        act3_review(args[args.index("--variant") + 1])
        sys.exit()
    if "--review" not in args:
        build()
    review()
    for v in ("a", "b"):
        act3_review(v)
