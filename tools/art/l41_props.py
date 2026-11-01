#!/usr/bin/env python3
"""Production art: Level 41's ground objects (M10 part G batch, concept round 79, step ART-G1): Helix HQ's archive
cores with their armoured hatches and the uplink's teal ring (the `FILES` secondary), Varga's lab vault (the secret)
and the CDF beacon the fleet's bombers drop on the ice (an armour patch at t=20). New (no concept round: Level 41's
*Objective*, *Ground targets*, *Secrets and pickups* and *Concept art*), on the frozen render package, seen from above
under Callisto's low top-left Sun like the level's units. Helix's look is Level 34's (tools/art/belt_units_g.py: charcoal,
gold edges, the gold Helix hex), the CDF's the khaki, the white band and the blue mark; vacuum (Callisto): dust and
shards fly in a ring and fall, nothing burns or billows. The uplink's teal (70, 235, 220) is a cool cyan-teal, kept off
the Vrell's green-teal 00FF9A (pending the user's review in round 79).

Outputs (build/art-held/sprites/ until Level 41's data.yaml names the root as a ground target's or a drop's ``sprite``
(step G2; tools/art/l23kit.py's rule; rerun then and the files move to assets/sprites/); ``additive`` = premultiplied
on black, GL_SRC_ALPHA, GL_ONE; every glow a float field in >= 32 undithered steps, drawn 1x):
  archive-core_0..2      40x40 normal: an archive core's armoured hatch in its gold-edged collar on the courtyard deck,
                         the gold Helix hex on its lid, two red lock lamps: sealed, hit (dented, scorched, a lamp out),
                         broken open (the plate gone, the core's teal glass stack in its well); the trigger's look
  archive-core-break_0..7  56x56 normal: the hatch breaking (the plate's shards flung out in a ring, a pale dust puff),
                         2 steps a frame, centred
  archive-core-glow_0..7   64x64 additive, centred: the open core's teal light, a slow pulse (8 steps a frame); the
                         ground objects' ``-glow`` (drawn after the light pass while the core is live)
  archive-core-ring_0..31  96x96 additive, centred: the uplink's ring at the 40 px zone's edge: 0 the faint track (the
                         zone, shown while the core is live), k the track with k / 31 of the circle filled clockwise from
                         the top with a bright head (the 2.0 s timer's share; 31 full)
  archive-core-done_0..5   96x96 additive, centred: the file in: the full ring flaring and running out, 2 steps a frame
  lab-vault_0..2         36x36 normal: the Helix floor vault in Varga's old lab: a round steel door with eight bolts and
                         the gold Helix hex, sealed, hit (dented, scorched), blown open (the door thrown aside, the dark
                         vault with its crate's amber glint); the trigger's look
  cdf-beacon             48x48 normal: the CDF beacon planted on the ice: a khaki pod with the white band and the blue
                         mark on its cap, three landing legs, the strobe mast (lights dark)
  cdf-beacon-lights_0..1   48x48 additive: 0 the strobe's flash (white-green mast lamp, red and green leg lamps), 1 the
                         leg lamps only (the drop drones' convention: frame 0 for the flash)
  cdf-beacon-open_0..3   48x48 normal: the cap springing open as the patch lifts out (the patch is the pickup's sprite)
  cdf-beacon-fall_0..3   48x48 normal: the pod coming down, legs folded, turning slowly (4 frames a turn of 90 degrees,
                         its 4-fold look); the game scales it from 2.0 to 1.0 over the fall
  cdf-beacon-retro_0..3  48x48 additive: the retro burn round the falling pod (a flicker)
  cdf-beacon-land_0..5   64x64 normal, stepped translucency (32 steps): the ice dust kicked up in a ring as it lands,
                         2 steps a frame
  design/campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/l41-objects-final-r79-a.png/.gif   review (the
                         sets, the readability over Level 41's ice and deck, an upload at 1x: the hatch shot open, the
                         ring filling while the ship hovers, the file in; the beacon's drop; the vault)

Run: python3 tools/art/l41_props.py [core] [vault] [beacon] [--review] [--check]
     (no subject = all, about 30 s; --review rebuilds only the review files from the written frames; --check lists every
     set with its place, frames, size, Source chunk, checker share and an additive set's glow steps)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
import belt_units as bu
import halo_platform as hp
from artkit import DESIGN, ROOT, TAU

from render import raster, sdf  # noqa: E402
from render.enemy_rigs import write_gif  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "l41_props.py"
BATCH = "M10 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r79"
L41 = DESIGN / "campaign" / "act-6-jovian-storm" / "level-41-callisto-fortress"
CONCEPT = L41 / "concept"
PX = 20.0
TEAL = np.array((70, 235, 220), float)
AMBER = np.array((255, 170, 60), float)
CORE, VAULT, BEACON = 40, 36, 48
BEACON_PX = 26.0                              # the beacon a little larger than the props' 20 px a unit
RING, RING_R, RING_FRAMES = 96, 40, 32
FPS = 20


def save_set(name, frames, single=False):
    bu.write(name, frames, SOURCE, single=single)


load, where = bu.load, bu.where


def M(albedo, metal=0.3, shininess=40, spec=0.4, emission=(0, 0, 0), pattern=None):
    return Material(tuple(albedo), metal=metal, shininess=shininess, spec=spec, emission=tuple(emission),
                    pattern=pattern)


def ribs(n, k=0.84, axis=0):
    def f(p, nrm):
        return np.where(np.sin(p[:, axis] * n) > 0.3, 1.0, k)
    return f


def render(scene, mats, size):
    return bu.render(scene, mats, size, size / PX)


def soot(img, seed, k=0.55):
    """A hit machine: darker, mottled by soot (vacuum: no embers)."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    h, w = op.shape
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    a[..., :3] *= (k + 0.35 * n)[..., None]
    a[~op] = 0
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def outline(img, k=0.55):
    """A darker bottom-right edge (the far side from the Sun), so a prop reads on the deck."""
    a = np.array(img).astype(np.float64)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op
    a[br, :3] *= k
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def hex_ring(p, r, w, z0, z1):
    """A thin hexagon outline (the Helix hex) of circumradius r on a flat top between z0 and z1."""
    qx, qy = np.abs(p[:, 0]), np.abs(p[:, 1])
    d = np.maximum(qx * 0.866 + qy * 0.5, qy) - r * 0.866
    return np.maximum(np.abs(d) - w, np.maximum(z0 - p[:, 2], p[:, 2] - z1))


def dent(p, x, y, k):
    z = np.zeros_like(p[:, 0])
    return p + np.stack([z, z, z + k * np.exp(-(((p[:, 0] - x) ** 2 + (p[:, 1] - y) ** 2) / 0.06))], 1)


# =========================================================================== the archive core

def core_scene(state):
    def scene(p):
        outer = sd_box(p, (0, 0, 0.14), (0.94, 0.94, 0.14), 0.04)
        inner = sd_box(p, (0, 0, 0.2), (0.76, 0.76, 0.3))
        items = [(np.maximum(outer, -inner) if state == 2 else outer, 0),
                 (np.maximum(sd_box(p, (0, 0, 0.29), (0.94, 0.94, 0.015)),
                             -sd_box(p, (0, 0, 0.29), (0.86, 0.86, 0.1))), 1)]          # the collar's gold edge
        if state < 2:
            q = dent(p, -0.25, 0.2, 0.12) if state == 1 else p
            items.append((sd_box(q, (0, 0, 0.34), (0.74, 0.74, 0.07), 0.03), 2))       # the hatch plate
            items.append((hex_ring(q, 0.36, 0.035, 0.38, 0.43), 1))                     # the Helix hex
            items.append((sd_cylinder_z(q, (0, 0, 0.42), 0.08, 0.02), 1))
            items += [(sd_box(q, (sx * 0.56, 0, 0.42), (0.05, 0.5, 0.015)), 3) for sx in (-1, 1)]   # hinge bars
        else:
            items.append((sd_box(p, (0, 0, 0.02), (0.76, 0.76, 0.02)), 4))              # the well's floor
            items.append((sd_cylinder_z(p, (0, 0, 0.18), 0.34, 0.16), 5))                # the core's glass stack
            items += [(sd_cylinder_z(p, (0, 0, 0.08 + 0.1 * j), 0.37, 0.012), 1) for j in range(3)]
            items.append((sd_box(p, (0.62, -0.7, 0.36), (0.22, 0.1, 0.05), 0.01), 2))   # a torn edge of the plate
            items.append((sd_box(p, (-0.66, 0.56, 0.34), (0.08, 0.2, 0.04), 0.01), 2))
        items += [(sd_sphere(p, (sx * 0.8, -0.8, 0.34), 0.06), 6 if (state == 0 or (state == 1 and sx < 0)) else 7)
                  for sx in (-1, 1)]
        return union(*items)
    return scene


def core_mats():
    return [M((0.2, 0.2, 0.22), 0.55, 90, 0.9),                             # 0 charcoal collar
            M((0.86, 0.64, 0.24), 0.8, 90, 1.0),                            # 1 gold
            M((0.24, 0.24, 0.27), 0.55, 90, 0.9, pattern=ribs(28, 0.86)),   # 2 the armoured plate
            M((0.12, 0.12, 0.14), 0.5, 60, 0.6),                            # 3 hinge bars
            M((0.03, 0.04, 0.05)),                                          # 4 the dark well
            M((0.1, 0.3, 0.3), 0.3, 160, 1.4, emission=tuple(TEAL / 255 * 0.55)),   # 5 the teal glass stack
            M((0.06, 0.02, 0.02), emission=(1.4, 0.2, 0.12)),               # 6 a lock lamp lit
            M((0.08, 0.05, 0.05), 0.2, 30, 0.3)]                            # 7 a lamp out


def _core(state):
    img = render(core_scene(state), core_mats(), CORE)
    if state:
        img = soot(img, 4101 + state, 0.8 if state == 1 else 0.85)
    return outline(img)


def core_glow(f, n=64):
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    k = 0.8 + 0.2 * np.sin(TAU * f / 8)
    fld = (np.exp(-(r / 7) ** 2) * 0.7 + np.exp(-(r / 18) ** 2) * 0.3) * k
    return hp.glow_img(fld, TEAL, white=0.25)


def ring_frame(k, n=RING):
    """The uplink ring: the faint track at the zone's edge, k / 31 of it filled clockwise from the top, a bright head."""
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    ang = (np.degrees(np.arctan2(x, -y)) % 360) / 360          # 0 at the top, clockwise
    share = k / (RING_FRAMES - 1)
    band = np.exp(-((r - RING_R) / 1.3) ** 2)
    fld = band * 0.2
    if k:
        on = np.clip((share - ang) * 360 / 2.0 + 0.5, 0, 1)    # 2 degrees of soft edge
        fld = fld + band * 0.75 * on
        head = np.exp(-(((ang - share + 0.5) % 1 - 0.5) * 360 / 4.0) ** 2) * np.exp(-((r - RING_R) / 2.4) ** 2)
        fld = fld + head * 0.6 * (k < RING_FRAMES - 1)
    return hp.glow_img(np.clip(fld, 0, 1), TEAL, white=0.4)


def done_frame(f, n=RING, frames=6):
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    s = f / (frames - 1)
    fld = (np.exp(-((r - RING_R - 6 * s) / (1.3 + 2.0 * s)) ** 2) * (1.2 - 0.9 * s)
           + np.exp(-(r / (6 + 14 * s)) ** 2) * 0.7 * (1 - s))
    return hp.glow_img(np.clip(fld, 0, 1.2), TEAL, white=0.8)


def break_frame(i, n=56, frames=8):
    """The hatch plate breaking: charcoal and gold shards flung out in a ring, a pale dust puff (vacuum)."""
    s = i / (frames - 1)
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    puff = np.exp(-((r - (4 + 18 * s)) / (3 + 5 * s)) ** 2) * 0.55 * (1 - s) ** 1.2
    alpha = hp.smooth_steps(np.clip(puff, 0, 1), 32)
    rgba = np.zeros((n, n, 4))
    rgba[..., :3] = (196, 204, 214)
    rgba[..., 3] = np.round(alpha * 255)
    rgba[rgba[..., 3] == 0] = 0
    img = Image.fromarray(rgba.astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(4141)
    for j in range(10):
        aa = rng.uniform(0, TAU)
        dist = 3 + rng.uniform(10, 22) * s ** 0.8
        spin = rng.uniform(-5, 5) * s
        size = rng.uniform(1.8, 3.6) * (1 - 0.25 * s)
        cx, cy = n / 2 + np.cos(aa) * dist, n / 2 + np.sin(aa) * dist
        pts = [(cx + np.cos(spin + t) * size, cy + np.sin(spin + t) * size * 0.7) for t in (0.2, 2.0, 3.6, 5.0)]
        base = np.array((214, 160, 60) if j % 4 == 0 else (60, 60, 68)) * (1 - 0.3 * s)
        d.polygon(pts, fill=tuple(int(v) for v in base) + (255,))
    return img


# =========================================================================== the lab vault

def vault_scene(state):
    def scene(p):
        items = [(sd_box(p, (0, 0, 0.08), (0.86, 0.86, 0.08), 0.06), 0)]               # the floor frame
        if state < 2:
            q = dent(p, 0.2, -0.15, 0.1) if state == 1 else p
            items.append((sd_cylinder_z(q, (0, 0, 0.22), 0.7, 0.07), 1))                # the round door
            items.append((sd_cylinder_z(q, (0, 0, 0.3), 0.46, 0.02), 2))
            items.append((hex_ring(q, 0.26, 0.035, 0.3, 0.35), 3))                      # the Helix hex
            items += [(sd_cylinder_z(q, (np.cos(a) * 0.6, np.sin(a) * 0.6, 0.3), 0.06, 0.03), 2)
                      for a in np.arange(8) * TAU / 8 + TAU / 16]                        # the bolts
        else:
            d0, m0 = union(*items)
            d0 = np.maximum(d0, -sd_cylinder_z(p, (0, 0, 0.1), 0.62, 0.3))              # the vault open
            items = [(d0, m0), (sd_cylinder_z(p, (0, 0, 0.0), 0.62, 0.02), 4),
                     (sd_box(p, (0.12, -0.08, 0.08), (0.24, 0.18, 0.08), 0.02), 5),     # the crate inside
                     (sd_cylinder_z(p - np.array((0.0, 0.0, 0.0)), (0.72, 0.62, 0.2), 0.4, 0.06), 1)]   # the door
        return union(*items)
    return scene


def vault_mats():
    return [M((0.3, 0.3, 0.32), 0.4, 50, 0.5), M((0.58, 0.6, 0.63), 0.7, 80, 0.8, pattern=ribs(40, 0.92, 1)),
            M((0.42, 0.43, 0.46), 0.7, 80, 0.8), M((0.86, 0.64, 0.24), 0.8, 90, 1.0),
            M((0.08, 0.06, 0.04), emission=tuple(AMBER / 255 * 0.35)),
            M((0.5, 0.4, 0.24), 0.2, 30, 0.3, emission=tuple(AMBER / 255 * 0.3))]


def _vault(state):
    img = render(vault_scene(state), vault_mats(), VAULT)
    if state:
        img = soot(img, 4151 + state, 0.78 if state == 1 else 0.85)
    return outline(img)


# =========================================================================== the CDF beacon

def beacon_scene(lid=0.0, legs=1.0, spin=0.0):
    """The CDF beacon: a khaki pod (radius 0.42) with the white band, its cap with the blue mark, three legs (folded
    at ``legs`` 0), the strobe mast; ``lid`` 0..1 the cap hinged open about its upper-left edge."""
    def scene(p):
        p = sdf.rotate_z(p, spin)
        items = [(sd_cylinder_z(p, (0, 0, 0.32), 0.42, 0.24), 0),
                 (sd_cylinder_z(p, (0, 0, 0.42), 0.43, 0.05), 1)]
        hinge = np.array((-0.3, 0.3, 0.6))
        a = lid * np.radians(140)
        q = p - hinge
        axis_rot = sdf.rotate_z(q, -np.pi / 4)
        axis_rot = sdf.rotate_x(axis_rot, a)
        q = sdf.rotate_z(axis_rot, np.pi / 4) + hinge
        rq = np.hypot(q[:, 0], q[:, 1])
        band = np.maximum(np.abs(rq - 0.3) - 0.045, np.abs(q[:, 2] - 0.645) - 0.01)          # the CDF's white band
        items += [(sd_cylinder_z(q, (0, 0, 0.6), 0.4, 0.04), 2), (sd_cylinder_z(q, (0, 0, 0.645), 0.16, 0.01), 3),
                  (band, 1),
                  (sd_capsule(q, (0.0, 0.0, 0.62), (0.0, 0.0, 0.9), 0.04), 4), (sd_sphere(q, (0, 0, 0.93), 0.06), 5)]
        if lid > 0:
            items.append((sd_cylinder_z(p, (0, 0, 0.5), 0.3, 0.06), 6))                 # the patch in its bay
        for j in range(3):
            t = j * TAU / 3 + np.pi / 6
            reach = 0.42 + 0.4 * legs
            items.append((sd_capsule(p, (np.cos(t) * 0.38, np.sin(t) * 0.38, 0.3),
                                     (np.cos(t) * reach, np.sin(t) * reach, 0.05 + 0.2 * (1 - legs)), 0.05), 4))
            items.append((sd_sphere(p, (np.cos(t) * 0.4, np.sin(t) * 0.4, 0.5), 0.045), 7))
        return union(*items)
    return scene


def beacon_mats():
    return [M((0.46, 0.46, 0.32), 0.3, 40, 0.4, pattern=ribs(26, 0.86, 1)), M((0.88, 0.88, 0.84), 0.2, 30, 0.3),
            M((0.4, 0.4, 0.28), 0.3, 40, 0.4), M((0.16, 0.3, 0.56), 0.2, 30, 0.3), M((0.32, 0.33, 0.34), 0.6, 50, 0.6),
            M((0.2, 0.22, 0.2), 0.3, 60, 0.6), M((0.84, 0.86, 0.8), 0.2, 40, 0.4), M((0.12, 0.1, 0.1), 0.3, 40, 0.4)]


def _beacon(kind, i):
    if kind == "planted":
        scene = beacon_scene()
    elif kind == "open":
        scene = beacon_scene(lid=(i + 1) / 4)
    else:
        scene = beacon_scene(legs=0.0, spin=i * TAU / 16)
    return outline(bu.render(scene, beacon_mats(), BEACON, BEACON / BEACON_PX))


def beacon_lights(f, n=BEACON):
    x, y = hp.grid(n)
    pts = [(np.cos(j * TAU / 3 + np.pi / 6) * 0.4 * BEACON_PX, -np.sin(j * TAU / 3 + np.pi / 6) * 0.4 * BEACON_PX)
           for j in range(3)]
    rgb = np.zeros((n, n, 3))
    for j, (px, py) in enumerate(pts):
        col = np.array((255, 60, 44)) if j == 0 else np.array((80, 255, 120)) if j == 1 else np.array((255, 236, 200))
        rgb += col * hp.smooth_steps(np.exp(-((x - px) ** 2 + (y - py) ** 2) / 1.6) * 0.9
                                     + np.exp(-((x - px) ** 2 + (y - py) ** 2) / 9.0) * 0.25, 48)[..., None]
    if f == 0:
        r = np.hypot(x, y + 0.2 * PX * 0)
        fld = np.exp(-(r / 2.0) ** 2) * 1.2 + np.exp(-(r / 8.0) ** 2) * 0.5
        rgb += np.array((220, 255, 230)) * hp.smooth_steps(np.clip(fld, 0, 1), 48)[..., None]
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((n, n), 255.0)]).astype(np.uint8), "RGBA")
    return artkit.additive(img)


def retro(f, n=BEACON):
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    rng = np.random.default_rng(4177 + f)
    k = 0.85 + 0.15 * rng.random()
    ring = np.exp(-((r - 12.5) / 2.6) ** 2) * 0.5 * k
    jets = sum(np.exp(-((x - np.cos(a) * 12) ** 2 + (y - np.sin(a) * 12) ** 2) / (3.0 + rng.random()))
               for a in np.arange(3) * TAU / 3 + np.pi / 2 + f * 0.2) * 0.9 * k
    return hp.glow_img(np.clip(ring + jets, 0, 1.2), (255, 170, 80), white=0.7)


def land_frame(i, n=64, frames=6):
    """Ice dust kicked up in a ring as the pod lands (vacuum: it flies out and falls, no billow)."""
    s = (i + 0.5) / frames
    x, y = hp.grid(n)
    r = np.hypot(x, y)
    fld = np.exp(-((r - (8 + 20 * s)) / (2.5 + 4 * s)) ** 2) * 0.7 * (1 - s) ** 1.1
    n_ = raster.fbm(n, n, 8, 4180 + i, octaves=3, period=False)
    fld *= 0.7 + 0.6 * n_
    alpha = hp.smooth_steps(np.clip(fld, 0, 1), 32)
    rgba = np.zeros((n, n, 4))
    rgba[..., :3] = (176, 188, 204)
    rgba[..., 3] = np.round(alpha * 255)
    rgba[rgba[..., 3] == 0] = 0
    return Image.fromarray(rgba.astype(np.uint8), "RGBA")


# =========================================================================== build

def _job(job):
    kind = job[0]
    return {"core": _core, "vault": _vault}[kind](job[1]) if kind in ("core", "vault") else _beacon(*job)


def run(jobs):
    with ProcessPoolExecutor(8) as pool:
        return list(pool.map(_job, jobs))


def build_core():
    save_set("archive-core", artkit.quantize_set(run([("core", s) for s in range(3)]), 40))
    save_set("archive-core-break", artkit.quantize_set([break_frame(i) for i in range(8)], 48))
    save_set("archive-core-glow", [core_glow(f) for f in range(8)])
    save_set("archive-core-ring", [ring_frame(k) for k in range(RING_FRAMES)])
    save_set("archive-core-done", [done_frame(f) for f in range(6)])


def build_vault():
    save_set("lab-vault", artkit.quantize_set(run([("vault", s) for s in range(3)]), 40))


def build_beacon():
    frames = run([("planted", 0)] + [("open", i) for i in range(4)] + [("fall", i) for i in range(4)])
    q = artkit.quantize_set(frames, 40)
    save_set("cdf-beacon", [q[0]], single=True)
    save_set("cdf-beacon-open", q[1:5])
    save_set("cdf-beacon-fall", q[5:9])
    save_set("cdf-beacon-lights", [beacon_lights(f) for f in range(2)])
    save_set("cdf-beacon-retro", [retro(f) for f in range(4)])
    save_set("cdf-beacon-land", [land_frame(i) for i in range(6)])


# =========================================================================== review

def L(name):
    return hp.L(name)


def upload_frames():
    """An upload at 1x on Level 41's courtyard: the hatch shot open, the ship hovering in the zone, the ring filling
    (2.0 s in total, paused while it drifts out), the file in."""
    bg = hp.deck(300, 4141).crop((90, 0, 390, 300))
    cx, cy = 150, 150
    ship = hp.ship_img()
    frames = []
    open_t, done_t = 0.8, None
    held = 0.0
    for i in range(int(6.0 * FPS)):
        t = i / FPS
        img = bg.copy()
        sx = cx + (60 - 60 * min(1, max(0, (t - 1.0) / 0.8)))
        sy = cy + 70 - 70 * min(1, max(0, (t - 1.0) / 0.8))
        if 2.6 < t < 3.2:
            sx += 55 * np.sin((t - 2.6) / 0.6 * np.pi)            # drifts out: the ring pauses
        inside = np.hypot(sx - cx, sy - cy) <= RING_R
        if t >= open_t and done_t is None and inside:
            held += 1 / FPS
            if held >= 2.0:
                done_t = t
        if t < open_t:
            hp.paste_c(img, L("archive-core")[0 if t < 0.5 else 1], cx, cy)
        else:
            hp.paste_c(img, L("archive-core")[2], cx, cy)
            n = int((t - open_t) * 40)
            if n < 8:
                hp.paste_c(img, L("archive-core-break")[n], cx, cy)
            img = hp.add_c(img, L("archive-core-glow")[(i // 2) % 8], cx, cy)
            if done_t is None:
                img = hp.add_c(img, L("archive-core-ring")[min(RING_FRAMES - 1, int(held / 2.0 * (RING_FRAMES - 1)))],
                               cx, cy)
            else:
                k = int((t - done_t) * 30)
                if k < 6:
                    img = hp.add_c(img, L("archive-core-done")[k], cx, cy)
        hp.paste_c(img, ship, sx, sy)
        raster.draw_text(img, 6, 6, f"FILES {0 if done_t is None else 1} / 3", raster.LABEL)
        frames.append(img)
    return frames


def beacon_frames():
    bg = hp.ice(300, 4142).crop((90, 0, 390, 300))
    cx, cy = 150, 150
    frames = []
    for i in range(int(4.0 * FPS)):
        t = i / FPS
        img = bg.copy()
        if t < 1.4:
            s = 2.0 - 1.0 * (t / 1.4)
            pod = L("cdf-beacon-fall")[i % 4]
            pod = pod.resize((int(pod.width * s), int(pod.height * s)), Image.BILINEAR)
            sh = hp.tinted(L("cdf-beacon-fall")[i % 4], black_alpha=0.35)
            hp.paste_c(img, sh, cx + 30 * (s - 1) + 4, cy + 30 * (s - 1) + 5)
            hp.paste_c(img, pod, cx, cy)
            img = hp.add_c(img, L("cdf-beacon-retro")[i % 4].resize(pod.size, Image.BILINEAR), cx, cy)
        else:
            u = t - 1.4
            if u < 0.6:
                hp.paste_c(img, L("cdf-beacon-land")[min(5, int(u * 10))], cx, cy)
            if t < 3.0:
                hp.paste_c(img, L("cdf-beacon")[0], cx, cy)
            else:
                hp.paste_c(img, L("cdf-beacon-open")[min(3, int((t - 3.0) * 10))], cx, cy)
            img = hp.add_c(img, L("cdf-beacon-lights")[0 if (t % 0.8) < 0.1 else 1], cx, cy)
        frames.append(img)
    return frames


def review():
    artkit.REVIEW_ROUND = ROUND
    up = upload_frames()
    bc = beacon_frames()
    ice = hp.ice(300, 4143)
    deck = hp.deck(300, 4144)

    def on(bg_, img, mode):
        import enemies_r72 as e72  # noqa: E402
        w = e72.window(bg_, 64, 64, mode).copy()
        hp.paste_c(w, img, 32, 32)
        return w.resize((128, 128), Image.NEAREST)
    read = [on(deck, L("archive-core")[0], "dark"), on(deck, L("archive-core")[2], "bright"),
            on(ice, L("lab-vault")[0], "dark"), on(ice, L("lab-vault")[0], "bright"),
            on(ice, L("cdf-beacon")[0], "dark"), on(ice, L("cdf-beacon")[0], "bright")]
    rows = [("ARCHIVE-CORE_0..2 (SEALED, HIT, OPEN), LAB-VAULT_0..2, CDF-BEACON, -OPEN_0..3, -FALL_0..3, 2X",
             L("archive-core") + L("lab-vault") + L("cdf-beacon") + L("cdf-beacon-open") + L("cdf-beacon-fall"), 2,
             False),
            ("ARCHIVE-CORE-BREAK_0..7 (2X), CDF-BEACON-LAND_0..5 (1X)", [f.resize((112, 112), Image.NEAREST) for f in
                                                                       L("archive-core-break")] + L("cdf-beacon-land"),
             1, False),
            ("ARCHIVE-CORE-RING (EVERY 4TH OF 32: THE TRACK, THEN FILLING), -DONE_0..5 (ADDITIVE, 1X)",
             L("archive-core-ring")[::4] + L("archive-core-ring")[-1:] + L("archive-core-done"), 1, True),
            ("ARCHIVE-CORE-GLOW_0..7, CDF-BEACON-LIGHTS_0..1, -RETRO_0..3 (ADDITIVE, 1X)", L("archive-core-glow")
             + L("cdf-beacon-lights") + L("cdf-beacon-retro"), 1, True),
            ("READABILITY (2X): THE HATCH ON THE DARKEST DECK, THE OPEN CORE ON THE BRIGHTEST, THE VAULT AND THE BEACON "
             "ON THE DARKEST AND BRIGHTEST ICE", read, 1, False),
            ("AN UPLOAD (1X): THE HATCH SHOT OPEN, THE RING FILLING WHILE THE SHIP HOVERS (PAUSED AS IT DRIFTS OUT), "
             "THE FILE IN", [up[k] for k in (10, 40, 58, 80, 104)], 1, False),
            ("THE BEACON'S DROP (1X): THE RETRO BURN, THE LANDING, PLANTED (THE STROBE), THE CAP OPEN",
             [bc[k] for k in (8, 30, 40, 70)], 1, False)]
    sheet = artkit.review_sheet("LEVEL 41 - ARCHIVE CORES, THE LAB VAULT, THE CDF BEACON (FINAL)", rows, width=1600,
                                batch=BATCH)
    png = CONCEPT / f"l41-objects-final-{ROUND}-a.png"
    gif = CONCEPT / f"l41-objects-final-{ROUND}-a.gif"
    CONCEPT.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(png, optimize=True)
    frames = []
    for a, b in zip(up, bc + [bc[-1]] * (len(up) - len(bc))):
        f = Image.new("RGBA", (606, 300), raster.SHEET_BG)
        f.alpha_composite(a, (0, 0))
        f.alpha_composite(b, (306, 0))
        frames.append(f)
    size = write_gif(frames, gif, fps=FPS, colors=128, max_mb=6.0)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


# =========================================================================== sets, check, main

def sets():
    return [("archive-core", 3, (CORE, CORE), False), ("archive-core-break", 8, (56, 56), False),
            ("archive-core-glow", 8, (64, 64), True), ("archive-core-ring", RING_FRAMES, (RING, RING), True),
            ("archive-core-done", 6, (RING, RING), True), ("lab-vault", 3, (VAULT, VAULT), False),
            ("cdf-beacon", 1, (BEACON, BEACON), False), ("cdf-beacon-lights", 2, (BEACON, BEACON), True),
            ("cdf-beacon-open", 4, (BEACON, BEACON), False), ("cdf-beacon-fall", 4, (BEACON, BEACON), False),
            ("cdf-beacon-retro", 4, (BEACON, BEACON), True), ("cdf-beacon-land", 6, (64, 64), False)]


BUILDERS = {"core": build_core, "vault": build_vault, "beacon": build_beacon}

if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if hp.check(sets(), "level 41 objects", SOURCE) else 1)
    subjects = [a for a in args if a in BUILDERS] or list(BUILDERS)
    if "--review" not in args:
        for s in subjects:
            BUILDERS[s]()
    review()
