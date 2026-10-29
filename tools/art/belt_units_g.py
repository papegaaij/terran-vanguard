#!/usr/bin/env python3
"""Production art: Level 34's ground objects (M9 part G batch, concept round 67, step ART-G): the salvage containers
(CDF and Helix), the Helix freighter's hold clamps, the Helix core's crate (shut in its clamps, rising, drifting:
QG-2 **a**, pending round 67), the three secrets' props (the frigate *Vigil*'s bridge shutter, the courier *Sable
Wren*'s cargo hatch, the tender *Old Marta*'s airlock). New (no concept round: the level README's *Layout*, *Ground
targets* and *Secrets*), on the frozen render package, seen from above under the top-left sun like the hulls of
tools/art/backdrop_l34.py. The CDF beacon that releases the armour patch is Level 33's ``nav-buoy`` (belt_units_f.py),
the mining charge Level 32's ``mining-charge`` (belt_units_e.py): reused, not redrawn.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: tools/art/l23kit.py's rule, a
ground target's ``sprite`` once a level's data names it; ``helix-core`` is the story pickup's look: SpriteUse needs
to claim it for Level 34, see m9-g4-notes.md):
  cargo-container-cdf_0..2     44x32: a CDF salvage container (khaki, the white CDF band and stencil, corner castings),
                               intact, damaged (dented, scorched), wrecked (doors blown, empty); the ``cargo-container``
                               look's variant (the secondary counts ``<look>-<variant>``)
  cargo-container-cdf-break_0..7    56x56: it bursts: the doors and panels flung out, a white gas puff, sparks (vacuum,
                               no smoke), 2 steps a frame
  cargo-container-helix_0..2   44x32: a Helix container: charcoal, gold edges, the gold Helix hex on its lid
  cargo-container-helix-break_0..7  56x56: the same burst in Helix colours
  hold-clamp-left_0..2         32x32: a hold clamp on its seat at the crate's left, its jaw gripping to the right (the
                               crate), its red lock lamp lit; hit (the lamp out, scorched); spent (the jaw sprung open)
  hold-clamp-right_0..2        32x32: the same at the crate's right, the jaw gripping to the left (its own render)
  hold-clamp-left-break_0..7, hold-clamp-right-break_0..7   48x48: the hydraulics blow: a white gas jet from the jaw,
                               sparks, the arm's pieces (vacuum)
  helix-core_0..1              64x48: the Helix crate (60x40) in the hold: charcoal, gold edges and handles, the Helix
                               emblem on its seal dimly lit (0, 1: the seal's slow glow; drawn while the clamps hold it)
  helix-core-rise_0..9         80x64: the rise (1.0 s, 10 fps): the crate lifting out of the hold, seen growing from
                               1.0 to 1.2 of its size, the seal's gold glow swelling
  helix-core-drift_0..7        80x64: drifting free, the seal pulsing slowly gold (8 frames, one pulse a second)
  helix-core-glow_0..15        96x96, additive: the seal's gold glow round the crate: 0..9 the rise (scaling up),
                               10..15 the drift's slow pulse (loop); drawn centred on the crate after it
  vigil-shutter_0..2           40x40: the *Vigil*'s bridge blast shutter (CDF grey slats in a frame, hazard edge, a red
                               lamp): sealed, buckled (hit), open (rolled up, the stores' amber light inside)
  sable-wren-hatch_0..2        40x40: the courier's cargo hatch (charcoal, gold rim, the white medical mark): sealed,
                               dented, blown off (the sealed white crates inside)
  old-marta-airlock_0..2       40x40: the tender's round airlock (rusty ochre, the hand wheel, a weak green lamp):
                               shut, dented, swung open (warm light inside)
  Review (round 67): design/campaign/act-5-the-belt/level-34-salvage-run/concept/salvage-objects-final-r67-a.png/.gif
  (the containers, the clamps and the crate's salvage, the secrets' props on Level 34's hulls)

Run: python3 tools/art/belt_units_g.py [containers] [clamps] [core] [secrets] [--review] [--check]
     (no subject = all, about a minute; renders cached in the system temp dir, conceptgame-cache/belt-units-g/, by the
     model code's hash; --review rebuilds only the review files from the written frames; --check lists every set with
     its folder, size and frame count and fails on a missing one or a missing Source chunk)
"""
import hashlib
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import l23kit
from artkit import DESIGN, ROOT, TAU

import enemies_r60 as e60  # noqa: E402  (concept script: the GIF writer)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

l23kit.claimed = bu.claimed

SCRIPT = "belt_units_g.py"
BATCH = "M9 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r67"
FPS = 20
L34 = DESIGN / "campaign" / "act-5-the-belt" / "level-34-salvage-run"
CONCEPT = L34 / "concept"
BACKDROP = ROOT / "assets" / "backdrop" / "level-34"
write, load, where = bu.write, bu.load, bu.where
PX = 20.0
GOLD = np.array((236, 180, 70), float)
AMBER = np.array((255, 170, 60), float)


def save_set(name, frames, single=False):
    write(name, frames, SOURCE, single=single)


def M(albedo, metal=0.3, shininess=40, spec=0.4, emission=(0, 0, 0), pattern=None):
    return Material(tuple(albedo), metal=metal, shininess=shininess, spec=spec, emission=tuple(emission),
                    pattern=pattern)


def ribs(n, k=0.82, axis=0):
    """Corrugation: a stripe multiplier along model x (axis 0) or y."""
    def f(p, nrm):
        return np.where(np.sin(p[:, axis] * n) > 0.3, 1.0, k)
    return f


def render(scene, mats, size, crisp=80):
    w, h = (size, size) if isinstance(size, int) else size
    return bu.render(scene, mats, (w, h), w / PX, crisp=crisp)


def outline(img, k=0.55):
    """A darker bottom-right edge (the far side from the sun), so the prop reads on a hull."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op
    a[br, :3] *= k
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# =========================================================================== containers

CONT = (44, 32)


def container_mats(kind, damage):
    dark = 1 - 0.35 * damage
    if kind == "cdf":
        body = M((0.46 * dark, 0.46 * dark, 0.32 * dark), 0.3, 40, 0.4, pattern=ribs(30))
        band = M((0.86 * dark, 0.86 * dark, 0.82 * dark), 0.2, 30, 0.3)
        trim = M((0.3, 0.31, 0.3), 0.6, 50, 0.6)
        mark = M((0.16, 0.3, 0.56), 0.2, 30, 0.3)
    else:
        body = M((0.2 * dark, 0.2 * dark, 0.22 * dark), 0.55, 90, 0.9, pattern=ribs(30, 0.85))
        band = M((0.86 * dark, 0.64 * dark, 0.24 * dark), 0.8, 90, 1.0)
        trim = M((0.82 * dark, 0.6 * dark, 0.2 * dark), 0.8, 90, 1.0)
        mark = M((0.95, 0.72, 0.26), 0.8, 90, 1.0, emission=(0.12, 0.08, 0.02))
    inside = M((0.04, 0.04, 0.05), 0, 5, 0)
    return [body, band, trim, mark, inside]


def container_scene(kind, state):
    """A container lying long side across the screen (2.0 x 1.3 units, 0.6 high): corrugated walls, the corner
    castings, a band across, the mark on the lid; ``state`` 1 dented, 2 burst open (the lid and doors gone)."""
    def scene(p):
        lid_z = 0.62 if state < 2 else 0.4
        items = [(sd_box(p, (0, 0, 0.3), (0.98, 0.62, 0.3 if state < 2 else 0.2), 0.03), 0)]
        if state < 2:
            items += [(sd_box(p, (0.42, 0, lid_z - 0.01), (0.12, 0.63, 0.02)), 1),
                      (sd_box(p, (-0.3, 0.05, lid_z), (0.24, 0.24, 0.015)), 3)]
        for sx in (-1, 1):
            for sy in (-1, 1):
                items.append((sd_box(p, (sx * 0.92, sy * 0.56, 0.32), (0.08, 0.08, 0.32)), 2))
        d, m = union(*items)
        if state == 1:                                      # a dent in the lid
            d = np.maximum(d, -sd_sphere(p, (0.15, -0.35, 0.92), 0.33))
        if state == 2:                                      # burst: the inside open
            d = np.maximum(d, -sd_box(p, (0, 0, 0.5), (0.86, 0.5, 0.4)))
            d2, m2 = union((sd_box(p, (0, 0, 0.06), (0.86, 0.5, 0.04)), 4),
                           (sd_box(p, (-0.95, 0.1, 0.2), (0.04, 0.4, 0.18), 0.01), 2))
            return union((d, m), (d2, m2))
        return d, m
    return scene


def _container(kind, state):
    img = render(container_scene(kind, state), container_mats(kind, 0.0 if state == 0 else 1.0), CONT)
    if state:
        import belt_units_c as buc  # noqa: E402
        img = buc.scorch(img, 3461 + state, 0.7 if state == 1 else 0.5)
    return outline(img)


def burst(base, i, n=8, size=56, palette=((196, 196, 180), (110, 112, 100)), gas=True, seed=3463):
    """An object bursting in vacuum: its wreck frame in the middle, panels flung out in a ring and spinning, a white
    gas puff (3 translucency steps, thinning), sparks; 2 steps a frame."""
    out = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    u = (i + 1) / n
    if base is not None:
        out.alpha_composite(base, ((size - base.width) // 2, (size - base.height) // 2))
    d = ImageDraw.Draw(out)
    rng = np.random.default_rng(seed)
    c = size / 2
    if gas and u < 0.8:
        for k, a in ((1.0, 150), (0.7, 100), (0.45, 60)):
            r = (6 + 18 * u) * k
            al = int(a * (1 - u / 0.8))
            d.ellipse([c - r, c - r * 0.85, c + r, c + r * 0.85], fill=(236, 240, 244, al))
    for k in range(9):
        a = k * TAU / 9 + rng.uniform(-0.3, 0.3)
        dist = (5 + 20 * u) * rng.uniform(0.7, 1.1)
        if u > 0.75 and k % 2:
            continue
        cx, cy = c + np.cos(a) * dist, c + np.sin(a) * dist
        s = rng.uniform(1.6, 3.2)
        spin = a + 4 * u
        pts = [(cx + np.cos(spin + j * TAU / 4) * s * (1.5 if j % 2 else 0.6), cy + np.sin(spin + j * TAU / 4) * s)
               for j in range(4)]
        d.polygon(pts, fill=palette[0] + (255,) if np.cos(a - 3.9) > 0 else palette[1] + (255,))
    if u < 0.6:
        for k in range(6):
            a = rng.uniform(0, TAU)
            r = (8 + 22 * u) * rng.uniform(0.6, 1.0)
            d.point((c + np.cos(a) * r, c + np.sin(a) * r), fill=(255, 220, 140, 255))
    return out


def build_containers():
    res = run([("container", k, s) for k in ("cdf", "helix") for s in range(3)])
    for i, k in enumerate(("cdf", "helix")):
        frames = artkit.quantize_set(res[i * 3:i * 3 + 3], 32)
        save_set(f"cargo-container-{k}", frames)
        pal = ((196, 196, 176), (104, 106, 86)) if k == "cdf" else ((226, 170, 70), (52, 52, 58))
        save_set(f"cargo-container-{k}-break",
                 artkit.quantize_set([burst(frames[2] if j < 6 else None, j, palette=pal, seed=3465 + i)
                                      for j in range(8)], 40))


# =========================================================================== the hold's clamps

CLAMP = 32


def clamp_mats(lamp):
    return [M((0.4, 0.42, 0.46), 0.6, 90, 0.9),                           # 0 gunmetal arm (lighter than the hold)
            M((0.86, 0.64, 0.24), 0.8, 90, 1.0),                          # 1 gold
            M((0.62, 0.62, 0.64), 0.7, 60, 0.7),                          # 2 steel: the hydraulic ram
            M((0.06, 0.02, 0.02), emission=(1.6 * lamp, 0.2 * lamp, 0.12 * lamp)),   # 3 the red lock lamp
            M((0.3, 0.3, 0.32), 0.3, 20, 0.2)]                            # 4 the seat


def clamp_scene(side, state):
    """A hold clamp seen from above: its seat block, the arm and the hydraulic ram reaching toward the crate (+x for
    the left clamp), the gold jaw gripping; ``state`` 2: the jaw sprung back and up, the ram broken."""
    s = 1 if side == "left" else -1

    def scene(p):
        q = p * np.array((s, 1.0, 1.0))
        jaw = 0.0 if state < 2 else -0.35
        items = [(sd_box(q, (-0.35, 0, 0.12), (0.42, 0.5, 0.12), 0.04), 4),
                 (sd_box(q, (-0.25, 0, 0.32), (0.32, 0.3, 0.1), 0.04), 0),
                 (sd_capsule(q, (-0.1, 0.12, 0.36), (0.42 + jaw, 0.12, 0.36), 0.08), 2),
                 (sd_box(q, (0.5 + jaw, 0, 0.36 + (0.12 if state == 2 else 0)), (0.1, 0.42, 0.1), 0.03), 1),
                 (sd_box(q, (0.62 + jaw, 0.34, 0.36), (0.12, 0.07, 0.07)), 1),
                 (sd_box(q, (0.62 + jaw, -0.34, 0.36), (0.12, 0.07, 0.07)), 1),
                 (sd_sphere(q, (-0.4, -0.18, 0.44), 0.08), 3)]
        return union(*items)
    return scene


def _clamp(side, state):
    img = render(clamp_scene(side, state), clamp_mats(1.0 if state == 0 else 0.0), CLAMP)
    if state:
        import belt_units_c as buc  # noqa: E402
        img = buc.scorch(img, 3471 + state, 0.75)
    return outline(img)


def build_clamps():
    res = run([("clamp", side, s) for side in ("left", "right") for s in range(3)])
    for i, side in enumerate(("left", "right")):
        frames = artkit.quantize_set(res[i * 3:i * 3 + 3], 28)
        save_set(f"hold-clamp-{side}", frames)
        save_set(f"hold-clamp-{side}-break",
                 artkit.quantize_set([burst(frames[2] if j < 6 else None, j, size=48,
                                            palette=((226, 170, 70), (60, 60, 66)), seed=3475 + i)
                                      for j in range(8)], 32))


# =========================================================================== the Helix core's crate

CRATE_W = 60 / PX


def crate_mats(seal):
    return [M((0.18, 0.18, 0.2), 0.55, 90, 0.9, pattern=ribs(18, 0.88, axis=1)),    # 0 charcoal
            M((0.86, 0.64, 0.24), 0.8, 90, 1.0),                                      # 1 gold edges, handles
            M((0.16, 0.12, 0.05), emission=tuple(GOLD / 255.0 * (0.25 + 1.5 * seal))),   # 2 the seal's emblem
            M((0.36, 0.36, 0.38), 0.7, 60, 0.7)]                                      # 3 steel latches


def crate_scene():
    """The Helix crate from above (3.0 x 2.0 units): a charcoal case with gold edge rails and handles, the hexagonal
    Helix emblem on its seal in the lid's middle, four steel latches."""
    def scene(p):
        items = [(sd_box(p, (0, 0, 0.4), (1.48, 0.98, 0.4), 0.06), 0)]
        for sy in (-1, 1):
            items.append((sd_box(p, (0, sy * 0.92, 0.82), (1.4, 0.05, 0.03)), 1))
        for sx in (-1, 1):
            items.append((sd_box(p, (sx * 1.42, 0, 0.82), (0.05, 0.92, 0.03)), 1))
            items.append((sd_capsule(p, (sx * 1.56, -0.3, 0.5), (sx * 1.56, 0.3, 0.5), 0.05), 1))
            for sy in (-1, 1):
                items.append((sd_box(p, (sx * 0.95, sy * 0.92, 0.84), (0.1, 0.06, 0.04)), 3))
        # the emblem: a raised hexagon ring with a lit core
        r = np.hypot(p[:, 0], p[:, 1])
        ang = np.arctan2(p[:, 1], p[:, 0])
        hexr = 0.36 * np.cos(np.pi / 6) / np.cos(((ang - np.pi / 6) % (np.pi / 3)) - np.pi / 6)
        ring = np.maximum(np.abs(r - hexr * 0.85) - 0.06, np.abs(p[:, 2] - 0.84) - 0.03)
        core = np.maximum(r - hexr * 0.5, np.abs(p[:, 2] - 0.83) - 0.03)
        d, m = union(*items)
        return union((d, m), (ring, 1), (core, 2))
    return scene


def _crate(scale, seal, size):
    w, h = size
    sc = crate_scene()
    scaled = (lambda p: (lambda r: (r[0] * scale, r[1]))(sc(p / scale)))
    return outline(bu.render(scaled, crate_mats(seal), (w, h), w / PX, crisp=80), 0.6)


def core_glow(k, size=96):
    """The seal's gold glow: a soft gold halo round the crate's outline (a rounded rectangle) and a bright core on the
    emblem; k 0..1 the strength (the rise grows it, the drift pulses it)."""
    yy, xx = np.mgrid[0:size, 0:size].astype(float) + 0.5
    x, y = xx - size / 2, yy - size / 2
    s = 0.85 + 0.35 * k
    rx, ry = 30 * s, 20 * s
    dx, dy = np.maximum(np.abs(x) - rx + 8, 0), np.maximum(np.abs(y) - ry + 8, 0)
    edge = np.hypot(dx, dy)
    halo = np.exp(-(np.maximum(edge - 8, 0) / (6 + 6 * k)) ** 2) * (np.abs(x) < rx + 30) * (edge > 4)
    core = np.exp(-(np.hypot(x, y) / (5 + 4 * k)) ** 2)
    f = np.clip(k * (0.55 * halo + 1.0 * core), 0, 1)
    f = artkit.ordered_dither(f, 6)
    rgb = np.array((255, 196, 80.0))[None, None] * f[..., None] + 120 * np.clip(f - 0.7, 0, 1)[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((size, size), 255.0)])
                                           .astype(np.uint8), "RGBA"))


RISE = 10
DRIFT = 8


def build_core():
    jobs = [("crate", 1.0, s, (64, 48)) for s in (0.15, 0.35)]
    jobs += [("crate", round(1.0 + 0.2 * (i / (RISE - 1)) ** 0.8, 4), round(0.35 + 0.65 * i / (RISE - 1), 4), (80, 64))
             for i in range(RISE)]
    jobs += [("crate", 1.2, round(0.7 + 0.3 * float(np.sin(TAU * i / DRIFT)) ** 2, 4), (80, 64)) for i in range(DRIFT)]
    res = run(jobs)
    save_set("helix-core", artkit.quantize_set(res[:2], 32))
    save_set("helix-core-rise", artkit.quantize_set(res[2:2 + RISE], 40))
    save_set("helix-core-drift", artkit.quantize_set(res[2 + RISE:], 40))
    glows = [core_glow((i + 1) / RISE) for i in range(RISE)] + [core_glow(0.75 + 0.25 * np.sin(TAU * i / 6) ** 2)
                                                                for i in range(6)]
    save_set("helix-core-glow", glows)


# =========================================================================== the secrets' props

SECRET = 40


def shutter_scene(state):
    """The Vigil's bridge blast shutter from above, its face toward the screen's bottom: a grey frame, horizontal slats
    (state 1: buckled), state 2: rolled up into the head box, the dark stores behind with their amber light."""
    def scene(p):
        items = [(sd_box(p, (0, 0.1, 0.25), (0.92, 0.72, 0.25), 0.03), 0),           # the frame block
                 (sd_box(p, (0, 0.62, 0.55), (0.86, 0.18, 0.12), 0.03), 1)]           # the head box
        if state < 2:
            for k in range(5):
                y = 0.36 - k * 0.2
                bend = 0.12 * np.exp(-((p[:, 0] - 0.1) / 0.3) ** 2) if state == 1 and k in (2, 3) else 0.0
                z = np.zeros_like(p[:, 0])
                items.append((sd_box(p - np.stack([z, z, z - bend], 1),
                                     (0, y, 0.5), (0.72, 0.08, 0.04), 0.01), 1))
        else:
            items.append((sd_box(p, (0, -0.1, 0.2), (0.72, 0.5, 0.05)), 4))            # the stores, lit amber
            items.append((sd_box(p, (-0.36, -0.15, 0.3), (0.2, 0.18, 0.08)), 5))
            items.append((sd_box(p, (0.3, -0.2, 0.3), (0.18, 0.2, 0.08)), 5))
        items += [(sd_box(p, (sx * 0.84, -0.66, 0.52), (0.08, 0.08, 0.04)), 2) for sx in (-1, 1)]
        items.append((sd_sphere(p, (0.78, 0.66, 0.7), 0.07), 3))
        return union(*items)
    return scene


def shutter_mats(state):
    return [M((0.5, 0.52, 0.54), 0.5, 50, 0.5),
            M((0.66, 0.67, 0.68), 0.5, 50, 0.6, pattern=ribs(30, 0.86, axis=1)),
            M((0.8, 0.6, 0.12), 0.2, 30, 0.3),                                         # hazard corners
            M((0.06, 0.02, 0.02), emission=(1.4, 0.18, 0.1) if state < 2 else (0.1, 0.02, 0.02)),
            M((0.08, 0.06, 0.04), emission=tuple(AMBER / 255.0 * 0.5)),
            M((0.4, 0.34, 0.24), 0.2, 30, 0.3, emission=tuple(AMBER / 255.0 * 0.35))]


def hatch_scene(state):
    """The Sable Wren's cargo hatch from above: a charcoal hatch in a gold rim with the white medical cross; state 2
    blown off: the sealed white crates below."""
    def scene(p):
        items = [(sd_box(p, (0, 0, 0.2), (0.86, 0.86, 0.2), 0.05), 1)]
        if state < 2:
            dent = 0.1 * np.exp(-(((p[:, 0] + 0.2) ** 2 + (p[:, 1] - 0.15) ** 2) / 0.06)) if state == 1 else 0.0
            z = np.zeros_like(p[:, 0])
            items.append((sd_box(p + np.stack([z, z, z + dent], 1),
                                 (0, 0, 0.46), (0.7, 0.7, 0.06), 0.03), 0))
            items += [(sd_box(p, (0, 0, 0.53), (0.32, 0.08, 0.01)), 2), (sd_box(p, (0, 0, 0.53), (0.08, 0.32, 0.01)), 2)]
        else:
            d, m = union(*items)
            d = np.maximum(d, -sd_box(p, (0, 0, 0.4), (0.7, 0.7, 0.3)))          # the hatch gone: the hold open
            inner = [(sd_box(p, (0, 0, 0.1), (0.7, 0.7, 0.02)), 3)]
            inner += [(sd_box(p, (sx * 0.34, sy * 0.3, 0.2), (0.26, 0.22, 0.1), 0.02), 2)
                      for sx in (-1, 1) for sy in (-1, 1)]
            return union((d, m), *inner)
        return union(*items)
    return scene


def hatch_mats(state):
    return [M((0.2, 0.2, 0.22), 0.55, 90, 0.9), M((0.84, 0.62, 0.22), 0.8, 90, 1.0),
            M((0.86, 0.9, 0.9), 0.1, 40, 0.4, emission=(0.05, 0.08, 0.08)), M((0.03, 0.03, 0.04))]


def airlock_scene(state):
    """Old Marta's airlock from above: a round rusty door in a square collar, the hand wheel, a small green lamp;
    state 2: the door swung aside, warm light inside."""
    def scene(p):
        items = [(sd_box(p, (0, 0, 0.18), (0.82, 0.82, 0.18), 0.06), 1)]
        if state < 2:
            items += [(sd_cylinder_z(p, (0, 0, 0.36), 0.62, 0.06 + 0.04 * (state == 1)), 0),
                      (sd_cylinder_z(p, (0, 0, 0.46), 0.26, 0.03), 2)]
            items += [(sd_capsule(p, (0, 0, 0.5), (np.cos(a) * 0.3, np.sin(a) * 0.3, 0.5), 0.035), 2)
                      for a in (0.3, 0.3 + TAU / 3, 0.3 + 2 * TAU / 3)]
        else:
            items += [(sd_cylinder_z(p, (0, 0, 0.2), 0.56, 0.03), 4),
                      (sd_cylinder_z(p, (0.9, -0.3, 0.42), 0.6, 0.05), 0)]
        items.append((sd_sphere(p, (-0.66, 0.66, 0.4), 0.07), 3))
        return union(*items)
    return scene


def airlock_mats(state):
    return [M((0.56, 0.4, 0.22), 0.3, 30, 0.3), M((0.4, 0.34, 0.26), 0.3, 30, 0.3), M((0.3, 0.3, 0.3), 0.6, 50, 0.6),
            M((0.02, 0.06, 0.02), emission=(0.1, 0.9, 0.3) if state < 2 else (0.02, 0.1, 0.04)),
            M((0.12, 0.08, 0.04), emission=tuple(AMBER / 255.0 * 0.55))]


SECRETS = {"vigil-shutter": (shutter_scene, shutter_mats), "sable-wren-hatch": (hatch_scene, hatch_mats),
           "old-marta-airlock": (airlock_scene, airlock_mats)}


def _secret(name, state):
    scene, mats = SECRETS[name]
    img = render(scene(state), mats(state), SECRET)
    if state:
        import belt_units_c as buc  # noqa: E402
        img = buc.scorch(img, 3481 + state + len(name), 0.8 if state == 1 else 0.7)
    return outline(img)


def build_secrets():
    names = list(SECRETS)
    res = run([("secret", n, s) for n in names for s in range(3)])
    for i, n in enumerate(names):
        save_set(n, artkit.quantize_set(res[i * 3:i * 3 + 3], 32))


# =========================================================================== jobs

JOBS = {"container": _container, "clamp": _clamp, "crate": _crate, "secret": _secret}


def _job(job):
    return JOBS[job[0]](*job[1:])


CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "belt-units-g"


def _model_key():
    text = Path(__file__).read_text(encoding="utf-8").split("# " + "=" * 75 + " review")[0]
    return hashlib.sha1(text.encode()).hexdigest()[:12]


def run(jobs):
    key = _model_key()
    paths = [CACHE / f"{hashlib.sha1((key + repr(j)).encode()).hexdigest()}.png" for j in jobs]
    todo = [i for i, p in enumerate(paths) if not p.exists()]
    if todo:
        CACHE.mkdir(parents=True, exist_ok=True)
        with ProcessPoolExecutor() as pool:
            for i, img in zip(todo, pool.map(_job, [jobs[i] for i in todo], chunksize=1)):
                img.save(paths[i])
    return [Image.open(p).convert("RGBA") for p in paths]


BUILDERS = {"containers": build_containers, "clamps": build_clamps, "core": build_core, "secrets": build_secrets}


# =========================================================================== review

def hull_plate(w, h, name="freighter-stern", y0=600):
    """The review ground: a Level 34 hull piece (backdrop_l34.py) from row y0, its middle 60 % tiled across, when
    written, else charcoal."""
    f = BACKDROP / f"{name}.png"
    out = Image.new("RGBA", (w, h), (14, 13, 18, 255))
    if f.exists():
        src = Image.open(f).convert("RGBA")
        if src.width >= w:
            out.alpha_composite(src.crop((src.width // 2 - w // 2, y0, src.width // 2 + w // 2, y0 + h)))
        else:
            a, b = int(src.width * 0.2), int(src.width * 0.8)
            tile = src.crop((a, y0, b, y0 + h))
            for x in range(0, w, tile.width):
                out.alpha_composite(tile, (x, 0))
    return out


def row_of(frames, bg, gap=8, scale=1):
    frames = [f.resize((f.width * scale, f.height * scale), Image.NEAREST) if scale != 1 else f for f in frames]
    h = max(f.height for f in frames) + 8
    img = bg.crop((0, 0, bg.width, h))
    x = 4
    for f in frames:
        if x + f.width > img.width:
            break
        img.alpha_composite(f, (x, 4))
        x += f.width + gap
    return img


def review():
    artkit.REVIEW_ROUND = ROUND
    s = {n: load(n) for n, _ in [x for v in SETS.values() for x in v]}
    cdf_bg = hull_plate(968, 140, "cdf-frigate-a", 600) if (BACKDROP / "cdf-frigate-a.png").exists() else None
    hx_bg = hull_plate(968, 140, "freighter-stern", 640)
    cdf_bg = cdf_bg or hx_bg
    rows = [("CDF CONTAINER: INTACT, DAMAGED, WRECKED; BURST (8, ADD THE SMALL EXPLOSION) (1X)",
             row_of(s["cargo-container-cdf"] + s["cargo-container-cdf-break"], cdf_bg)),
            ("HELIX CONTAINER: THE SAME", row_of(s["cargo-container-helix"] + s["cargo-container-helix-break"], hx_bg)),
            ("CONTAINERS AT 2X", row_of(s["cargo-container-cdf"] + s["cargo-container-helix"], hx_bg, scale=2)),
            ("HOLD CLAMPS LEFT / RIGHT: LOCKED, HIT, SPRUNG; THE HYDRAULICS BLOW (1X)",
             row_of(s["hold-clamp-left"] + s["hold-clamp-right"] + s["hold-clamp-left-break"], hx_bg)),
            ("THE HELIX CORE: IN THE HOLD (2), THE RISE (10, 1 S), THE DRIFT (8) (1X)",
             row_of(s["helix-core"] + s["helix-core-rise"][::2] + s["helix-core-drift"][::2], hx_bg)),
            ("THE SEAL'S GLOW (ADDITIVE): THE RISE 0-9, THE DRIFT 10-15 (EVERY 2ND)",
             row_of(s["helix-core-glow"][::2], Image.new("RGBA", (968, 140), (10, 10, 14, 255)))),
            ("THE SECRETS: VIGIL SHUTTER, SABLE WREN HATCH, OLD MARTA AIRLOCK: SHUT, HIT, OPEN (1X, 2X)",
             row_of(s["vigil-shutter"] + s["sable-wren-hatch"] + s["old-marta-airlock"]
                    + [f.resize((80, 80), Image.NEAREST) for f in (s["vigil-shutter"][0], s["sable-wren-hatch"][0],
                                                                    s["old-marta-airlock"][0])], cdf_bg))]
    notes = ["THE CONTAINERS ARE THE CARGO-CONTAINER LOOK'S VARIANTS (-CDF, -HELIX): 12 HP, A SALVAGE PICKUP OF 50 EACH.",
             "THE CLAMPS GRIP THE CRATE FROM BOTH SIDES (15 HP); WITH THE FOURTH DOWN THE CRATE RISES ON ITS SEAL'S GOLD",
             "GLOW (1 S, ITS SIZE 1.0 -> 1.2), THEN DRIFTS WITH A SLOW GOLD PULSE UNTIL FLOWN THROUGH (QG-2 A; NO TUG).",
             "THE CDF BEACON IS LEVEL 33'S NAV BUOY, THE CHARGES LEVEL 32'S MINING CHARGE (REUSED)."]
    hgt = 70 + sum(r[1].height + 26 for r in rows) + 16 * len(notes) + 10
    sheet = raster.sheet(1000, hgt, "LEVEL 34 SALVAGE OBJECTS (FINAL R67)", f"PRODUCTION ART, {BATCH.upper()} - R67")
    y = 40
    for label, img in rows:
        raster.draw_text(sheet, 16, y, label, raster.LABEL)
        sheet.alpha_composite(img.convert("RGBA"), (16, y + 14))
        y += img.height + 26
    for line in notes:
        raster.draw_text(sheet, 16, y, line, raster.LABEL_DIM)
        y += 16
    png, gif_path = artkit.review_paths(CONCEPT, "salvage-objects")
    sheet.convert("RGB").save(png, optimize=True)
    gif = review_gif(s)
    size = e60.rig.write_gif(gif, gif_path, fps=FPS, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif_path.name} ({size / 1e6:.1f} MB)")


def review_gif(s):
    """The hold at 1x, scrolling at 110 px/s: the four clamps shot one by one (the burst, the sprung frame), the crate
    rising on its glow, drifting, the ship flying through it; beside it a CDF container shot open."""
    bg = hull_plate(480, 1400, "freighter-stern", 0)
    small = bu.load("explosion-small") if True else []
    frames = []
    T = 5.0
    cx, cy0 = 240, 780                               # the hold's centre on the piece (backdrop_l34.HOLD)
    clamps = [("left", -46, -30, 0.6), ("right", 46, -30, 0.9), ("left", -46, 30, 1.2), ("right", 46, 30, 1.5)]
    for f in range(int(T * FPS)):
        t = f / FPS
        off = int(500 - 540 + 110 * t * 0.35)
        img = bg.crop((0, max(0, cy0 - 330 - int(40 * t)), 480, max(0, cy0 - 330 - int(40 * t)) + 540))
        ox, oy = cx, 330 + int(40 * t)
        rise_t = 1.8
        if t < rise_t + 0.05:
            img = bu.paste(img, s["helix-core"][int(t * 2) % 2], ox, oy)
        for side, dx, dy, tk in clamps:
            if t < tk:
                img = bu.paste(img, s[f"hold-clamp-{side}"][0], ox + dx, oy + dy)
            else:
                img = bu.paste(img, s[f"hold-clamp-{side}"][2], ox + dx, oy + dy)
                j = int((t - tk) * 10)
                if j < 8:
                    img = bu.add(img, s[f"hold-clamp-{side}-break"][j], ox + dx, oy + dy)
                k = int((t - tk) * 20)
                if small and k < len(small):
                    img = bu.add(img, small[k], ox + dx, oy + dy)
        if t >= rise_t:
            u = t - rise_t
            if u < 1.0:
                i = min(RISE - 1, int(u * RISE))
                img = bu.paste(img, s["helix-core-rise"][i], ox, oy - 30 * u)
                img = bu.add(img, s["helix-core-glow"][i], ox, oy - 30 * u)
            else:
                i = int((u - 1.0) * DRIFT) % DRIFT
                yy = oy - 30 - 20 * (u - 1.0)
                if u < 2.6:
                    img = bu.paste(img, s["helix-core-drift"][i], ox, yy)
                    img = bu.add(img, s["helix-core-glow"][10 + int((u - 1.0) * 6) % 6], ox, yy)
        ship = artkit.load_frames("ship")[2]
        sy = 470 - max(0, t - 3.2) * 120
        img = bu.paste(img, ship, 240 + 60 * np.sin(t), sy)
        bx, by = 400, 120 + 40 * t
        if t < 1.0:
            img = bu.paste(img, s["cargo-container-cdf"][0], bx, by)
        else:
            img = bu.paste(img, s["cargo-container-cdf"][2], bx, by)
            j = int((t - 1.0) * 10)
            if j < 8:
                img = bu.paste(img, s["cargo-container-cdf-break"][j], bx, by)
        raster.draw_text(img, 8, 540 - 14, f"SALVAGE OBJECTS FINAL R67 - {t:4.1f} S", raster.LABEL)
        frames.append(img)
        del off
    return frames


# =========================================================================== check

SETS = {"containers": [("cargo-container-cdf", 3), ("cargo-container-cdf-break", 8), ("cargo-container-helix", 3),
                       ("cargo-container-helix-break", 8)],
        "clamps": [("hold-clamp-left", 3), ("hold-clamp-right", 3), ("hold-clamp-left-break", 8),
                   ("hold-clamp-right-break", 8)],
        "core": [("helix-core", 2), ("helix-core-rise", RISE), ("helix-core-drift", DRIFT), ("helix-core-glow", 16)],
        "secrets": [(n, 3) for n in SECRETS]}


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
    review()
