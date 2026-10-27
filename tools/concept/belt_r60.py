#!/usr/bin/env python3
"""Concept round 60 - the Act 5 "The Belt" world proposals (step ART-P-world, 2026-10-27; everything
pending the user's review in round 60).

Outputs (each PNG a review sheet; GIFs are the 480x540 play field at 1x, 15 fps, with the player):
  design/enemies/bosses/iron-sovereign/concept/iron-sovereign-r60-{a,b}.{png,gif}
      The Iron Sovereign (Q7 a, the top-down rings) in the Q11 a look: three rings turning at their own
      speeds round the armoured core, batteries live only in the fire arc (the red band, lights gold ->
      red), a lock (the ring grinds to a halt and goes dark), the core opening under its hex shield
      (spiral, Dart stream), the death chain (cook-offs ring by ring, the rings cracking into drifting
      segments, the core's implosion and blow). The rings are drawn from identical hull segments
      (outer 12, middle 8, inner 6) at 1-degree headings with one fixed sun, as the game would place
      them; the sheet measures the segment sprites and estimates the one-page atlas budget.
      a "continuous wheel": the segments butt into a smooth ring (seams hidden in the panel joints),
        gold teeth on every rim, red running lights on the ribs.
      b "armoured links": every segment a separate armour block with a dark gap and a gold-edged
        cap plate, the turning read by the gaps (seams by design), red lights at the block ends.
  design/world/belt/concept/belt-moods-r60-{a,b}.png
      The per-level mood ladder L29-L35 (lit Ceres Hub -> the shipping lane -> the rock field -> the
      dark tunnels and the refinery -> the leech field -> the graveyard -> Ceres orbit), each still
      with its luminance and its darkest window. a: the Q11 a grade (a step lighter than Act 4, the
      sunlit dust band, warm sunlit rock); b: the harsh grade (deep shadows, as Act 4's darkness).
  design/world/belt/concept/belt-readability-r60-{a,b,c}.png
      The Q11 readability frame: Ascendancy units (Talon leader, Hornet, SAM Nest; enemies_r60.py's
      models) in look a (gloss black, gold trim >= 20 %, strong gold rim), b (gunmetal hulls with gold)
      or c (black silhouettes, gold accents only, red lights) over the darkest window of every level
      in both grades, with the measured gold share, and a play-field frame.
  design/allies/mining-tug/concept/mining-tug-r60-{a,b}.{png,gif}
      The Bellwether, 160x260, three sections (bow, cargo, engines) intact, damaged (< 50 %: venting,
      flickering lights) and lost (dark, scorched); the GIF: station and sway over the rock field,
      hooked Hornet missiles hitting a section, a section lost. a "ore frame": yellow-grey hazard
      girders with white habitat cans lashed on, three drive bells; b "pressure hull": an enclosed
      older hull, sections told by colour bands.
  design/story/characters/vorne/concept/vorne-portraits-r60-{a,b}.png
      Vorne's production portraits in the chosen r04 style (portraits.py's style-B pipeline, imported
      unchanged): radio 72 px and briefing 144 px, neutral / smug / rage (a cold rage: bared,
      clenched teeth, the screen tearing with static), one palette over the three. a: the plain amber
      enemy screen as the cast's; b: his office (the gilded Ascendancy crest behind his head).
  design/story/characters/concept/act5-speakers-r60-a.png
      The three new radio speakers at 72 px: Ceres Hub Control (teal), the Bellwether's captain
      (teal), Harker (amber, a CDF flight suit with the Ascendancy crest stitched over the patch).
  design/campaign/act-5-the-belt/concept/wavering-stormhawk-r60-{a,b}.{png,gif}
      Q10 b's wavering Ceres Wing Stormhawks: the CDF airframe (render/models.ship_c_model, as Rook's
      and the player's) with the Ascendancy crest painted over the roundel and a white IFF light
      blinking 1 Hz (red on a hit); the GIF: a pair enters, holds with a wing-waggle and peels off
      beside a Talon wing in the dock canyon. a "Ceres Wing ochre": CDF grey with sand-ochre panels;
      b "hasty blackout": the CDF scheme half overpainted black with a stencilled gold crest.

Units, backdrops, the homing diamond and helpers are enemies_r60.py's, imported unchanged; the portrait
pipeline is tools/art/portraits.py's, imported unchanged (nothing is written to assets/).
Run: python3 tools/concept/belt_r60.py [sovereign] [moods] [frame] [tug] [vorne] [speakers]
     [mutineers] [a|b] [--no-gif]          (no subject = everything; ~15 minutes on 16 cores)
"""
import multiprocessing
import os
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from functools import lru_cache
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "art"))
import enemies_r03 as e3  # noqa: E402
import enemies_r60 as u60  # noqa: E402  (the round-60 units, backdrops and helpers)
from render import enemy_models as em  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sdf, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import A_FACET, A_GOLD, A_GUN, A_HULL, A_RED, hx  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

FW, FH, FPS, TAU = 480, 540, 15, 2 * np.pi
SUB = "CONCEPT ROUND 60 - THE BELT"
u60.r5.SUB = SUB
DESIGN = ROOT / "design"
DOWN, UP = np.pi / 2, -np.pi / 2
GOLD, CRIMSON = u60.GOLD, u60.CRIMSON
NO_GIF = "--no-gif" in sys.argv
CACHE = os.environ.get("BELT_R60_CACHE")            # optional sprite cache dir while iterating
WORKERS = min(10, os.cpu_count() or 4)

smooth, light, caption, label = u60.smooth, u60.light, u60.caption, u60.label


def times(seconds):
    """Frame times at this script's 15 fps (enemies_r60 runs at 20)."""
    return [i / FPS for i in range(int(seconds * FPS))]


def save_png(img, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.convert("RGB").save(path, optimize=True)
    print(f"wrote {path.relative_to(ROOT)}", flush=True)


def save_gif(frames, path):
    if NO_GIF:
        return
    size = rig.write_gif(frames, path, fps=FPS)
    print(f"wrote {path.relative_to(ROOT)} ({size / 1e6:.1f} MB, {len(frames)} frames)", flush=True)


def panel_grid(cells, cols, pad=8, labels=None, bg=raster.SHEET_BG):
    """Images of any size in a grid with labels under each."""
    rows = [cells[i:i + cols] for i in range(0, len(cells), cols)]
    cw = max(c.width for c in cells)
    rh = [max(c.height for c in r) + (12 if labels else 0) for r in rows]
    img = Image.new("RGBA", (cols * (cw + pad) - pad, sum(rh) + pad * (len(rows) - 1)), bg)
    y = 0
    for ri, r in enumerate(rows):
        for ci, c in enumerate(r):
            x = ci * (cw + pad)
            img.alpha_composite(c.convert("RGBA"), (x, y))
            if labels:
                raster.draw_text(img, x, y + c.height + 2, labels[ri * cols + ci], raster.LABEL_DIM)
        y += rh[ri] + pad
    return img


def lum(img):
    a = np.array(img.convert("RGB")).astype(np.float64)
    return a @ np.array((0.3, 0.55, 0.15))


# ===================================================================== 1 The Iron Sovereign

PX = 40.0                                     # px per model unit on the station
CX, CY = 240, 160                             # the station's centre on the screen
RINGS = {"inner": (70, 100, 6), "middle": (123, 157, 8), "outer": (180, 220, 12)}
SUN = sdf.norm(sdf.vec(-0.55, 0.6, 0.75))      # the belt's sun, top-left (the render's key direction)
A_LAMP, A_STRIP, A_BAY = 7, 8, 9              # extra material slots on the station


def station_mats(lamps=1.0, strip=1.0, bay=0.0):
    mats = u60.belt_mats("3a3442", 1.0, red=lamps)
    return mats + [u60.emissive(np.array((1.0, 0.86, 0.5)), 1.6 * strip),
                   u60.emissive(np.array((1.0, 0.94, 0.8)), 1.4 * strip),
                   u60.emissive(np.array((1.0, 0.8, 0.3)), 0.1 + 1.8 * bay)]


def _round_slab(d2, z, zc, h, rr):
    """A slab of the 2-D region ``d2`` between zc - h and zc + h with rounded top edges."""
    dz = np.abs(z - zc) - h
    a, b = d2 + rr, dz + rr
    return np.sqrt(np.maximum(a, 0) ** 2 + np.maximum(b, 0) ** 2) + np.minimum(np.maximum(a, b), 0) - rr


def segment_scene(v, ring):
    """One hull segment of ``ring`` centred on the +x axis (model units, the ring's centre at the
    origin). a: continuous wheel - the sector is cut flat at its ends so neighbours butt into one
    ring; b: armoured link - a separate block with a gap at each end and a gold-edged cap plate."""
    r0, r1, n = RINGS[ring]
    R0, R1, S = r0 / PX, r1 / PX, TAU / n
    pitch = S / {"outer": 4, "middle": 4, "inner": 3}[ring]

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        r = np.hypot(x, y)
        th = np.arctan2(y, x)
        d_ang = (np.abs(th) - S / 2) * r                # > 0 beyond the segment's ends
        d_rad = np.maximum(R0 - r, r - R1)
        items = []
        if v == "a":
            deck = np.maximum(_round_slab(d_rad, z, 0.08, 0.14, 0.05), d_ang)
            items.append((deck, A_HULL))
            lip = np.maximum(R1 - 0.16 - r, r - R1)
            items.append((np.maximum(_round_slab(lip, z, 0.2, 0.12, 0.04), d_ang), A_GOLD))
            ilip = np.maximum(R0 - r, r - R0 - 0.1)
            items.append((np.maximum(_round_slab(ilip, z, 0.17, 0.1, 0.03), d_ang), A_GOLD))
            tt = (th + pitch / 2) % pitch - pitch / 2       # gold teeth along the outer rim
            tooth = np.maximum(np.abs(tt) * r - 0.13, np.maximum(R1 - 0.1 - r, r - R1 - 0.1))
            items.append((np.maximum(_round_slab(tooth, z, 0.16, 0.12, 0.03), d_ang), A_GOLD))
            rt = (th + S / 4) % (S / 2) - S / 4              # ribs at the segment's quarter points
            rib = np.maximum(np.abs(rt) * r - 0.05, np.maximum(R0 + 0.08 - r, r - R1 + 0.14))
            items.append((np.maximum(_round_slab(rib, z, 0.2, 0.06, 0.02), d_ang), A_FACET))
            mid = (R0 + R1) / 2
            lamp = np.hypot(np.abs(rt) * r, r - mid) - 0.065
            items.append((np.maximum(np.sqrt(np.maximum(lamp, 0) ** 2 + (z - 0.28) ** 2) - 0.03, d_ang), A_RED))
            vent = np.maximum(np.abs(th) * r - 0.26, np.abs(r - (R0 + 0.3)) - 0.07)
            items.append((np.maximum(_round_slab(vent, z, 0.2, 0.04, 0.01), d_ang), A_GUN))
        else:
            gap = 0.075
            d_blk = (np.abs(th) - S / 2) * r + gap
            blk = _round_slab(np.maximum(d_rad, d_blk), z, 0.08, 0.14, 0.06)
            items.append((blk, A_HULL))
            plate = np.maximum(np.maximum(R0 + 0.07 - r, r - R1 + 0.07), d_blk + 0.07)
            items.append((_round_slab(plate, z, 0.22, 0.06, 0.03), A_GOLD))
            cap = np.maximum(np.maximum(R0 + 0.15 - r, r - R1 + 0.17), d_blk + 0.15)
            items.append((_round_slab(cap, z, 0.25, 0.06, 0.03), A_FACET))
            stripe = np.maximum(np.maximum(R1 - 0.12 - r, r - R1 + 0.02), d_blk + 0.1)
            items.append((_round_slab(stripe, z, 0.25, 0.05, 0.02), A_GOLD))
            ends = np.abs(np.abs(th) * r - (S / 2 * r - gap - 0.14))
            lamp = np.hypot(ends, r - (R0 + R1) / 2 + 0.02) - 0.06
            items.append((np.sqrt(np.maximum(lamp, 0) ** 2 + (z - 0.3) ** 2) - 0.03, A_RED))
        return union(*items)
    return scene


def base_scene(v):
    """The static under-structure: a dark disc under the rings with catwalk light strips and
    radial struts in the gaps between them, the core's collar with its four Dart bays."""
    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        r = np.hypot(x, y)
        th = np.arctan2(y, x)
        disc = _round_slab(r - 222 / PX, z, -0.22, 0.12, 0.04)
        items = [(disc, A_FACET)]
        for g0, g1 in ((100, 123), (157, 180)):
            gm = (g0 + g1) / 2 / PX
            walk = np.maximum(np.abs(r - gm) - 0.09, -1e9)
            items.append((_round_slab(walk, z, -0.07, 0.03, 0.01), A_GUN))
            k = 24 if g0 > 120 else 16
            st = (th + np.pi / k) % (TAU / k) - np.pi / k
            strip = np.hypot(np.abs(st) * r, r - gm) - 0.035
            items.append((np.sqrt(np.maximum(strip, 0) ** 2 + (z + 0.02) ** 2) - 0.02, A_STRIP))
            ss = (th + np.pi / 6) % (TAU / 6) - np.pi / 6
            strut = np.maximum(np.abs(ss) * r - 0.07, np.maximum(g0 / PX - r, r - g1 / PX))
            items.append((_round_slab(strut, z, -0.04, 0.04, 0.01), A_HULL))
            items.append((np.sqrt(np.maximum(np.hypot(np.abs(ss) * r, r - gm) - 0.05, 0) ** 2 + (z - 0.02) ** 2) - 0.025,
                          A_LAMP))
        collar = np.maximum(r - 68 / PX, 40 / PX - r)
        items.append((_round_slab(collar, z, 0.02, 0.12, 0.05), A_HULL))
        items.append((_round_slab(np.abs(r - 66 / PX) - 0.05, z, 0.12, 0.05, 0.02), A_GOLD))
        items.append((_round_slab(np.abs(r - 43 / PX) - 0.04, z, 0.12, 0.05, 0.02), A_GOLD))
        for k in range(4):                                   # the four rim bays, diagonals
            a = np.pi / 4 + k * np.pi / 2
            bx, by = 55 / PX * np.cos(a), 55 / PX * np.sin(a)
            q = sdf.rotate_z(p - np.array((bx, by, 0.0)), a)
            items.append((sd_box(q, (0, 0, 0.12), (0.17, 0.12, 0.05), 0.02), A_GUN))
            items.append((sd_box(q, (0, 0, 0.16), (0.12, 0.08, 0.03), 0.01), A_BAY))
        if v == "b":                                          # the links' track, under the gaps
            for rr in (88, 140, 200):
                items.append((_round_slab(np.abs(r - rr / PX) - 0.06, z, -0.06, 0.04, 0.01), A_GOLD))
        return union(*items)
    return scene


def _render_window(scene, mats, cx, cy, size, factor, colors=48):
    """Render model-space window centred on (cx, cy) model units, ``size`` px square, lit by the
    fixed sun (far away, so every segment shares one light and their seams match)."""
    ext = size / PX
    key = SUN * 400.0 / (ext / 2.3) - sdf.vec(cx, cy, 0.0) / (ext / 2.3)
    hi = sdf.render(scene, mats, (size * factor, size * factor), ext, center=(cx, cy), key_pos=key,
                    steps=90)
    return sprite.make_sprite(hi, factor, colors)


def seg_window(ring):
    r0, r1, n = RINGS[ring]
    S = TAU / n
    half = max(r1 * np.sin(S / 2), (r1 - r0 * np.cos(S / 2)) / 2) + 8
    return int(2 * np.ceil(half / 2) * 2)


def seg_render_job(job):
    """(variant, ring, heading in whole degrees, screen clockwise) -> (sprite, offset of the window
    centre from the ring's centre in screen px)."""
    v, ring, deg = job
    if CACHE:
        cp = Path(CACHE) / f"seg-{v}-{ring}-{deg:03d}.png"
        if cp.exists():
            sp = Image.open(cp).convert("RGBA")
            return sp
    r0, r1, n = RINGS[ring]
    rot = -np.radians(deg)                               # screen clockwise = world clockwise
    base = segment_scene(v, ring)
    scene = lambda p, s=base, a=rot: s(sdf.rotate_z(p, a))
    mid = (r0 + r1) / 2 * np.cos(TAU / n / 4) / PX
    cx, cy = mid * np.cos(rot), mid * np.sin(rot)
    sp = _render_window(scene, rig.model_space_materials(station_mats(), rot), cx, cy, seg_window(ring), 3)
    if CACHE:
        Path(CACHE).mkdir(parents=True, exist_ok=True)
        sp.save(Path(CACHE) / f"seg-{v}-{ring}-{deg:03d}.png")
    return sp


def seg_offset(ring, deg):
    r0, r1, n = RINGS[ring]
    mid = (r0 + r1) / 2 * np.cos(TAU / n / 4)
    a = np.radians(deg)
    return mid * np.cos(a), mid * np.sin(a)


SEGS = {}


def load_segments(v):
    """All 360 headings of every ring's segment for variant v (rendered in parallel)."""
    jobs = [(v, ring, d) for ring in RINGS for d in range(360) if (v, ring, d) not in SEGS]
    if jobs:
        with ProcessPoolExecutor(WORKERS, mp_context=multiprocessing.get_context("fork")) as pool:
            for job, sp in zip(jobs, pool.map(seg_render_job, jobs, chunksize=4)):
                SEGS[job] = sp
    return SEGS




@lru_cache(maxsize=None)
def seg_dark(v, ring, deg):
    """A locked ring's segment: lights out (the red lamps dark), the hull a step darker."""
    a = np.array(SEGS[(v, ring, deg)]).astype(np.float64)
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    lamp = (r > 120) & (r > 2.2 * g)
    a[lamp, :3] = (34, 18, 18)
    a[..., :3] *= 0.62
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def base_render(v):
    if CACHE and (Path(CACHE) / f"base-{v}.png").exists():
        return Image.open(Path(CACHE) / f"base-{v}.png").convert("RGBA")
    sp = _render_window(base_scene(v), station_mats(), 0.0, 0.0, 456, 2, colors=64)
    if CACHE:
        sp.save(Path(CACHE) / f"base-{v}.png")
    return sp


def base_dark(sp):
    a = np.array(sp).astype(np.float64)
    hot = (a[..., 0] > 150) & (a[..., 1] > 110)
    a[hot, :3] *= 0.3
    a[..., :3] *= 0.7
    return Image.fromarray(a.astype(np.uint8), "RGBA")


A_REACT, A_SLIT = 10, 11


def core_scene(u=0.0):
    """The core, 80 px across shut: six armoured shutter petals (the crest's gold ray and ring on
    each) over the reactor; ``u`` opens them (sliding out and down)."""
    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        items = [(sd_sphere(p, (0, 0, -0.15), 0.62), A_REACT),
                 (sd_cylinder_z(p, (0, 0, -0.12), 0.98, 0.06), A_SLIT)]
        for k in range(6):
            a = k * TAU / 6 + np.pi / 6
            off = np.array((np.cos(a), np.sin(a), 0.0)) * 0.48 * u - np.array((0, 0, 0.22 * u))
            q = p - off
            r = np.hypot(q[:, 0], q[:, 1])
            th = np.arctan2(q[:, 1], q[:, 0])
            dth = (th - a + np.pi) % TAU - np.pi
            dome = sdf.sd_ellipsoid(q, (0, 0, -0.05), (1.0, 1.0, 0.5))
            wedge = (np.abs(dth) - np.pi / 6) * r + 0.035
            d = np.maximum(np.maximum(dome, wedge), -q[:, 2] - 0.06)
            ray = (np.abs(dth) * r < 0.05) & (r > 0.25)
            ring = np.abs(r - 0.66) < 0.035
            m = np.where(ray | ring, A_GOLD, np.where(r > 0.9, A_FACET, A_HULL))
            items.append((d, m))
        return union(*items)
    return scene


def core_mats(slit=0.0, react=0.4):
    return station_mats() + [u60.emissive(np.array((1.0, 0.86, 0.5)), 0.25 + 0.85 * react),
                             u60.emissive(np.array((1.0, 0.76, 0.28)), 0.08 + 1.9 * slit)]


@lru_cache(maxsize=None)
def core_sprite(u=0.0, slit=0.0, react=0.4):
    u, slit, react = round(u, 2), round(slit, 2), round(react, 2)
    key = f"core-{u:.2f}-{slit:.2f}-{react:.2f}.png"
    if CACHE and (Path(CACHE) / key).exists():
        return Image.open(Path(CACHE) / key).convert("RGBA")
    sp = _render_window(core_scene(u), core_mats(slit, react), 0.0, 0.0, 128, 3, colors=48)
    sp = u60.belt_rim(True)(sp)
    if CACHE:
        sp.save(Path(CACHE) / key)
    return sp


def core_at(u, slit, react):
    return core_sprite(round(u * 10) / 10, round(slit * 2) / 2, round(react * 4) / 4)


# --------------------------------------------------------------------------- batteries

def collar_model(lamp):
    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        items = [(sd_cylinder_z(p, (0, 0, 0.06), 0.95, 0.1), A_HULL),
                 (np.maximum(sd_cylinder_z(p, (0, 0, 0.14), 0.9, 0.06), 0.72 - r), A_GOLD)]
        for k in range(4):
            a = np.pi / 4 + k * np.pi / 2
            items.append((sd_sphere(p, (0.82 * np.cos(a), 0.82 * np.sin(a), 0.22), 0.08), 7))
        return union(*items)
    col = em.RED if lamp == "red" else np.array((1.0, 0.76, 0.3))
    return scene, u60.belt_mats("3a3442", extra=[u60.emissive(col, 1.9)])


BAT = {"outer": ("sam", 40), "middle": ("rail", 48), "inner": ("laser", 44)}
collars = {(k, lamp): rig.AngleSprites(lambda lamp=lamp: collar_model(lamp), n, 4, factor=4, colors=24,
                                       post=u60.belt_rim(True))
           for k, n in ((40, 40), (48, 48), (44, 44)) for lamp in ("gold", "red")}
sam_tur = u60.Turning(lambda **kw: u60.sam_rack("a", **kw), 40, 32, factor=5, colors=28, post=u60.belt_rim(True))
las_tur = u60.Turning(lambda **kw: u60.sentinel_head("a", **kw), 44, 32, factor=4, colors=32,
                      post=u60.belt_rim(True))
_rail = {}


def rail_tur(aim, charge=0.0):
    k = int(round((aim % TAU) / (TAU / 32))) % 32
    c = round(charge * 2) / 2
    if (k, c) not in _rail:
        S = rig.AngleSprites(lambda: u60.bunker_model(k * TAU / 32, c), 48, 4, factor=4, colors=40,
                             post=u60.belt_rim(False))
        _rail[(k, c)] = S.get(UP)
    return _rail[(k, c)]


def battery_sprite(ring, aim, live, charge=0.0, hatch=0.0):
    kind, n = BAT[ring]
    out = collars[(n, "red" if live else "gold")].get(UP).copy()
    if kind == "sam":
        top = sam_tur.get(aim, hatch=round(hatch, 1), lock=1.0 if live and hatch > 0 else 0.0)
    elif kind == "rail":
        top = rail_tur(aim, charge)
    else:
        top = las_tur.get(aim, live=round(charge, 1))
    sprite.paste_center(out, top, n // 2, n // 2)
    return out


@lru_cache(maxsize=None)
def wreck_sprite(ring):
    kind, n = BAT[ring]
    a = np.array(collars[(n, "gold")].get(UP)).astype(np.float64)
    a[..., :3] *= 0.38
    img = Image.fromarray(a.astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(n)
    for _ in range(7):
        x, y = rng.uniform(n * 0.3, n * 0.7, 2)
        d.line([n / 2, n / 2, x, y], fill=(20, 14, 12, 255), width=2)
    d.ellipse([n * 0.36, n * 0.36, n * 0.64, n * 0.64], fill=(18, 12, 10, 255))
    return img


# --------------------------------------------------------------------------- the arena

def ceres_bg():
    """Ceres orbit: belt space with the sunlit dust band, Ceres's cratered curve far below (the
    deep layer, receded), lit from the top-left."""
    img = u60.space_bg(351, FH, band=0.85)
    tex = u60.remap(e3.regolith(FW, FH, 352, craters=30), u60.WARM_ROCK, gain=0.8)
    yy, xx = np.mgrid[0:FH, 0:FW].astype(np.float64)
    cx, cy, R = 700.0, 980.0, 820.0
    d = np.hypot(xx - cx, yy - cy)
    inside = d < R
    nz = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    nx, ny = (xx - cx) / R, (yy - cy) / R
    lit = np.clip(-0.62 * nx - 0.5 * ny + 0.35 * nz, 0.05, 1.0)
    t = np.array(tex.convert("RGB")).astype(np.float64) * (0.25 + 0.6 * lit[..., None]) * 0.62
    a = np.array(img.convert("RGB")).astype(np.float64)
    edge = np.clip((R - d) / 2.0, 0, 1)[..., None]
    a = a * (1 - edge) + t * edge
    limb = np.abs(d - R) < 1.2
    a[limb] = a[limb] * 0.4 + np.array((200, 182, 150)) * 0.6 * lit[limb, None]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def station_rim(layer):
    """The Q11 a rim on the station's silhouette: gold on the edges facing the top-left sun (2 px),
    red on the far edges (unquantised, applied to the whole station layer)."""
    a = np.array(layer).astype(np.float64)
    op = a[..., 3] > 0
    pad = np.pad(op, 2)
    tl = (~pad[2:-2, :-4] | ~pad[:-4, 2:-2] | ~pad[2:-2, 1:-3] | ~pad[1:-3, 2:-2]) & op
    br = (~pad[2:-2, 3:-1] | ~pad[3:-1, 2:-2]) & op & ~tl
    a[tl, :3] = a[tl, :3] * 0.2 + u60.GOLD_HI * 0.8
    a[br, :3] = a[br, :3] * 0.45 + u60.RED_RIM * 0.55
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def ring_state(t, plan):
    """Rotation (screen degrees, clockwise +) of every ring at t, from the plan's speed function."""
    return {ring: plan["angle"][ring][min(len(plan["angle"][ring]) - 1, int(round(t * FPS)))] for ring in RINGS}


def make_plan(dur):
    """The GIF's timeline: ring speeds by phase, the batteries' deaths, the cuts."""
    n = int(dur * FPS) + 2
    ang = {r: [0.0] for r in RINGS}
    base = {"outer": 6.0, "middle": -10.0}
    halt = {"outer": 9.4, "middle": 13.0, "inner": 13.0}
    for i in range(1, n):
        t = i / FPS
        spin = smooth(t / 2.0)
        fast = 1.25 if t >= 9.4 else 1.0
        for ring in RINGS:
            if t >= halt[ring]:
                k = max(0.0, 1 - (t - halt[ring]) / 2.0) if ring == "outer" else 0.0
                w = (base.get(ring, 0) * k) if ring != "inner" else 0.0
            elif ring == "inner":
                cyc = (t - 2.0) % 8.0 if t >= 2.0 else -1
                w = (120 / 4.0 * fast) if 0 <= cyc < 4.0 / fast else 0.0
                if t < 2.0:
                    w = 0.0
            else:
                w = base[ring] * spin * fast
            ang[ring].append(ang[ring][-1] + w / FPS)
    return dict(angle=ang, dur=dur)


def batteries(rot, ring):
    r0, r1, n = RINGS[ring]
    nb = n // 2
    mid = (r0 + r1) / 2
    out = []
    for j in range(nb):
        a = np.radians(rot + j * 360 / nb)
        out.append((CX + mid * np.cos(a), CY + mid * np.sin(a), a))
    return out


def arc_bearing(sx, sy):
    b = np.degrees(np.arctan2(sy - CY, sx - CX))
    return float(np.clip(b, 30, 150))


def in_arc(a_rad, bearing):
    return abs((np.degrees(a_rad) - bearing + 180) % 360 - 180) <= 60


YY, XX = np.mgrid[0:FH, 0:FW].astype(np.float64)
RR, AA = np.hypot(XX - CX, YY - CY), np.degrees(np.arctan2(YY - CY, XX - CX))


def arc_bands(img, bearing, rings, k=1.0):
    """The fire arc drawn on the hull: a faint red band over each live ring's armed sector."""
    near = np.abs((AA - bearing + 180) % 360 - 180)
    a = np.zeros((FH, FW, 4))
    for ring in rings:
        r0, r1, _ = RINGS[ring]
        band = (RR > r0 + 3) & (RR < r1 - 3) & (near <= 60)
        edge = band & ((np.abs(RR - r0 - 4) < 1) | (np.abs(RR - r1 + 4) < 1) | (np.abs(near - 59.5) < 0.6))
        a[band] = (255, 40, 40, 34 * k)
        a[edge] = (255, 60, 50, 150 * k)
    img.alpha_composite(Image.fromarray(a.astype(np.uint8), "RGBA"))
    return img


def station_layer(v, rots, dark, base_on=1.0, drift=None, shake=(0, 0), size=(FW, FH), centre=(CX, CY)):
    """Base + rings (each from its segment sprites at 1-degree headings) + the silhouette rim.
    ``dark`` ring -> True draws its segments unlit; ``drift`` (s, spins) cracks the rings apart."""
    cx, cy = centre[0] + shake[0], centre[1] + shake[1]
    lay = Image.new("RGBA", size, (0, 0, 0, 0))
    b = BASE[v] if base_on >= 1 else BASE_DARK[v]
    if drift is None or drift[0] < 1.6:
        sprite.paste_center(lay, b, cx, cy)
    for ring, (r0, r1, n) in RINGS.items():
        for k in range(n):
            a = rots[ring] + k * 360 / n
            spin, dr = 0.0, 0.0
            if drift is not None:
                s = drift[0]
                spin = s * (5 + 3 * (k % 3)) * (1 if k % 2 else -1)
                dr = s * (14 + 4 * ((k * 7) % 5)) + 6 * s * s
            h = int(round(a + spin)) % 360
            sp = seg_dark(v, ring, h) if dark.get(ring) else SEGS[(v, ring, h)]
            ox, oy = seg_offset(ring, h)
            ar = np.radians(a)
            sprite.paste_center(lay, sp, cx + ox + dr * np.cos(ar), cy + oy + dr * np.sin(ar))
    return station_rim(lay)


BASE, BASE_DARK = {}, {}


def prepare(v):
    load_segments(v)
    if v not in BASE:
        BASE[v] = base_render(v)
        BASE_DARK[v] = base_dark(BASE[v])


def ship_xy(t):
    if t < 9.0:
        return 240 + 120 * np.sin(0.5 * t), 458.0
    if t < 13.0:
        return 240 + 100 * np.sin(0.6 * t + 1.0), 458.0
    if t < 21.0:
        return 240 + 110 * np.sin(0.75 * (t - 13.0)), 446.0
    u = smooth((t - 21.0) / 2.0)
    x0 = 240 + 110 * np.sin(0.75 * 8.0)
    return x0 * (1 - u) + 240 * u, 446 + 24 * u


SOV_DUR = 26.5


class Sovereign:
    """The GIF's timeline (medium, Q7 a): phase 1 (2-9 s), a cut to phase 2 (the outer ring locks),
    a cut to phase 4 (the core), a cut to the death chain."""

    def __init__(self, v):
        self.v = v
        self.plan = make_plan(SOV_DUR)
        self.kill = {ring: [None] * (RINGS[ring][2] // 2) for ring in RINGS}
        for tk in (5.2, 8.0):
            self._kill_nearest("outer", tk)
        last = None
        for j in range(6):
            if self.kill["outer"][j] is None:
                last = j
        for j in range(6):
            if self.kill["outer"][j] is None and j != last:
                self.kill["outer"][j] = 9.0
        self.kill["outer"][last] = 9.4
        for ring in ("middle", "inner"):
            self.kill[ring] = [13.0] * len(self.kill[ring])
        self.homing = u60.Homing("a", ship_xy, speed=110, turn=80, life=4.0)
        self.shots = u60.Shots()
        self.darts = u60.Darts(ship_xy)
        self.salvos, self.rails, self.lasers = [], [], []
        for ts in (2.8, 5.6, 8.4):
            for j, (x, y, a) in enumerate(self.bats("outer", ts)):
                if self.alive("outer", j, ts) and in_arc(a, arc_bearing(*ship_xy(ts))):
                    self.salvos.append((ts, j))
                    for k in range(2):
                        sx, sy = ship_xy(ts)
                        self.homing.launch(ts + 0.25 * k, x, y, np.arctan2(sy - y, sx - x))
        for ts in (3.4, 7.2, 10.6):
            best = None
            for j, (x, y, a) in enumerate(self.bats("middle", ts)):
                if self.alive("middle", j, ts) and in_arc(a, arc_bearing(*ship_xy(ts))):
                    if best is None or y > best[2]:
                        best = (j, x, y)
            if best:
                sx, sy = ship_xy(ts)
                self.rails.append(dict(t0=ts, j=best[0], tx=sx, ty=sy))
        for ts in (6.2,):
            for j, (x, y, a) in enumerate(self.bats("inner", ts)):
                if in_arc(a, arc_bearing(*ship_xy(ts))):
                    sx, sy = ship_xy(ts)
                    self.lasers.append(dict(t0=ts, j=j, b0=np.arctan2(sy - y, sx - x)))
                    break
        self.fans = [3.0, 6.0, 9.0, 10.4, 12.9]
        for tf in self.fans:
            sx, sy = ship_xy(tf)
            self.shots.fan(tf, CX, CY + 30, np.arctan2(sy - CY - 30, sx - CX), 5, np.radians(40), 150, r=4)
        for i in range(int(3.8 * 5)):                          # the spiral, 2 arms, 5 bullets/s an arm
            ts = 17.2 + i * 0.2
            for arm in range(2):
                a = np.radians(72 * (ts - 17.2)) + arm * np.pi + 0.4
                self.shots.fire(ts, CX + np.cos(a) * 30, CY + np.sin(a) * 30, CX + np.cos(a) * 31,
                                CY + np.sin(a) * 31, 130, r=4)
        sx, sy = ship_xy(17.6)
        self.bay = 1 if sx < CX else 0                          # bays at 45 / 135 degrees: down-right, down-left
        ba = np.radians(45 + 90 * self.bay)
        self.bay_xy = (CX + 55 * np.cos(ba), CY + 55 * np.sin(ba))
        for k in range(4):
            self.darts.launch(18.1 + 0.25 * k, *self.bay_xy, ba, kill=(19.6 + 0.3 * k) if k < 2 else None)

    def rots(self, t):
        return ring_state(t, self.plan)

    def bats(self, ring, t):
        return batteries(self.rots(t)[ring], ring)

    def alive(self, ring, j, t):
        k = self.kill[ring][j]
        return k is None or t < k

    def _kill_nearest(self, ring, tk):
        best = None
        for j, (x, y, a) in enumerate(self.bats(ring, tk)):
            if self.kill[ring][j] is None and (best is None or y > best[1]):
                best = (j, y)
        self.kill[ring][best[0]] = tk

    def hp(self, t):
        if t < 2.0:
            return smooth(t / 2.0)
        dead = sum(1 for j, k in enumerate(self.kill["outer"]) if k is not None and t >= k) * 220
        dead += sum(1 for k in self.kill["middle"] if t >= k) * 330 + sum(1 for k in self.kill["inner"] if t >= k) * 440
        core = 0.0 if t < 17.0 else min(1.0, (t - 17.0) / 4.0) * 0.6 * 5000
        if t >= 24.6:
            core = 5000
        return max(0.0, 1 - (dead + core) / 8960)

    def frame(self, bg, t):
        v = self.v
        f = bg.copy()
        rots = self.rots(t)
        locked = {r: t >= (9.4 if r == "outer" else 13.0) for r in RINGS}
        lit = {"outer": t >= 0.4, "middle": t >= 0.9, "inner": t >= 1.4}
        dark = {r: locked[r] and (t >= 9.6 or r != "outer") or not lit[r] for r in RINGS}
        shake = (0, 0)
        if 13.0 <= t < 13.6 or 24.0 <= t < 24.8:
            shake = (int(round(2 * np.sin(t * 90))), int(round(1.5 * np.cos(t * 70))))
        drift = (t - 23.2, None) if t >= 23.2 else None
        lay = station_layer(v, rots, dark, base_on=0 if t >= 13.0 else 1, drift=drift, shake=shake)
        if t >= 23.2:
            lay = u60.fade(lay, max(0.0, 1 - (t - 23.2) / 3.5))
        f.alpha_composite(lay)
        sx, sy = ship_xy(t)
        bearing = arc_bearing(sx, sy)
        if 2.0 <= t < 13.0:
            f = arc_bands(f, bearing, [r for r in RINGS if not locked[r]], k=min(1.0, (t - 2.0) / 0.6))
        # batteries
        for ring in RINGS:
            for j, (x, y, a) in enumerate(self.bats(ring, t)):
                x, y = x + shake[0], y + shake[1]
                if t >= 23.2:
                    continue
                if self.alive(ring, j, t):
                    live = lit[ring] and 2.0 <= t and in_arc(a, bearing)
                    aim = np.arctan2(sy - y, sx - x)
                    hatch = max((1.0 for ts, jj in self.salvos if ring == "outer" and jj == j and ts - 0.4 <= t < ts + 0.6),
                                default=0.0)
                    charge = 0.0
                    if ring == "middle":
                        for rl in self.rails:
                            if rl["j"] == j and rl["t0"] <= t < rl["t0"] + 1.3:
                                charge = min(1.0, (t - rl["t0"]) / 1.0)
                                aim = np.arctan2(rl["ty"] - y, rl["tx"] - x)
                    if ring == "inner":
                        for ls in self.lasers:
                            u = t - ls["t0"]
                            if ls["j"] == j and 0 <= u < 2.4:
                                aim = ls["b0"] - np.radians(60) + np.radians(120) * np.clip((u - 0.8) / 1.6, 0, 1)
                                charge = 0.3 if u < 0.8 else 1.0
                    sprite.paste_center(f, battery_sprite(ring, aim, live, charge, hatch), x, y)
                    k = self.kill[ring][j]
                    if k is not None and k - 0.8 <= t < k and k not in (9.0, 13.0):
                        f = u60.spark(f, x + np.sin(t * 40) * 6, y + 8, ((t - k) % 0.2) / 0.2)
                else:
                    sprite.paste_center(f, wreck_sprite(ring), x, y)
                    k = self.kill[ring][j]
                    if k not in (9.0, 13.0):
                        f = u60.boom(f, x, y, (t - k) / 0.6, "small")
                    if int(t * 6 + j) % 3 == 0:
                        f = light(f, x + 3, y - 2, 4, (255, 140, 60), 0.4)
            # death cook-offs, ring by ring
            if t >= 21.0:
                t0 = {"outer": 21.0, "middle": 21.9, "inner": 22.6}[ring]
                for j, (x, y, a) in enumerate(self.bats(ring, t)):
                    f = u60.boom(f, x, y, (t - t0 - 0.15 * j) / 0.7, "medium", n=12)
        # the core
        u = 0.0 if t < 13.6 else smooth((t - 13.6) / 3.0)
        if t >= 24.0:
            u = max(0.0, 1 - (t - 24.0) / 0.5)
        slit = 1.0 if any(tf - 0.5 <= t < tf for tf in self.fans) else 0.0
        react = 0.4 if t < 13.6 else (0.7 + 0.3 * (16.6 <= t < 17.2 and int(t * 10) % 2))
        if t >= 24.0:
            react = 1.0
        if t < 24.6:
            cs = core_at(u, slit, react)
            sprite.paste_center(f, cs, CX + shake[0], CY + shake[1])
            if slit:
                f = light(f, CX, CY + 20, 26, (255, 190, 80), 0.35)
            if u > 0.5:
                f = light(f, CX, CY, 30, (255, 236, 170), 0.5 * u)
            if 16.6 <= t < 21.0:
                hit = (np.sin(t * 9) * 20, 30) if int(t * 7) % 3 == 0 else None
                f = u60.shield(f, cs.crop((16, 16, 112, 112)), CX, CY, mode="bubble", alpha=0.35, hit=hit,
                               refill=min(1.0, (t - 16.6) / 0.6))
            if 17.6 <= t < 18.1:
                f = light(f, *self.bay_xy, 12, (255, 200, 90), 1.0)
        if 24.0 <= t < 24.6:
            f = light(f, CX, CY, 20 + 120 * (t - 24.0), (255, 240, 200), 1.2)
        if t >= 24.6:
            s = (t - 24.6) / 1.0
            if s < 1:
                big = u60.EXPL["large"][min(11, int(s * 12))]
                sprite.paste_center(f, big.resize((big.width * 2, big.height * 2), Image.NEAREST), CX, CY)
            for k in range(26):
                cx_ = CX + 160 * np.cos(k * 2.4) * min(1, (t - 24.6) / 0.6)
                cy_ = CY + 60 * np.sin(k * 2.4) + 120 * max(0, t - 25.0)
                if t > 25.0:
                    d = ImageDraw.Draw(f)
                    d.rectangle([cx_ - 2, cy_ - 2, cx_ + 2, cy_ + 2], fill=(255, 210, 80, 255), outline=(120, 70, 10, 255))
        # weapons
        for rl in self.rails:
            j = rl["j"]
            u_ = t - rl["t0"]
            if 0 <= u_ < 1.3 and self.alive("middle", j, t):
                x, y, _ = self.bats("middle", t)[j]
                ang = np.arctan2(rl["ty"] - y, rl["tx"] - x)
                if u_ < 1.0:
                    f = u60.rail_line(f, x, y, ang, "a", u_, t)
                else:
                    f = u60.rail_shot(f, x, y, ang, (u_ - 1.0) / 0.3)
        for ls in self.lasers:
            u_ = t - ls["t0"]
            if 0 <= u_ < 2.4:
                x, y, _ = self.bats("inner", t)[ls["j"]]
                lay2 = Image.new("RGBA", f.size, (0, 0, 0, 0))
                d = ImageDraw.Draw(lay2)
                a0, a1 = np.degrees(ls["b0"]) - 60, np.degrees(ls["b0"]) + 60
                if u_ < 0.8:
                    k = 0.4 + 0.6 * u_ / 0.8
                    d.pieslice([x - 300, y - 300, x + 300, y + 300], a0, a1, fill=(255, 48, 56, int(40 * k)),
                               outline=(255, 48, 56, int(220 * k)), width=2)
                    f.alpha_composite(lay2)
                else:
                    aim = ls["b0"] - np.radians(60) + np.radians(120) * min(1, (u_ - 0.8) / 1.6)
                    ex, ey = x + 300 * np.cos(aim), y + 300 * np.sin(aim)
                    d = ImageDraw.Draw(f)
                    d.line([x, y, ex, ey], fill=(40, 6, 10, 255), width=8)
                    d.line([x, y, ex, ey], fill=CRIMSON + (255,), width=6)
                    d.line([x, y, ex, ey], fill=(255, 210, 200, 255), width=2)
        self.shots.draw(f, t)
        f = self.homing.draw(f, t)
        f = self.darts.draw(f, t, "a")
        u60.ship(f, sx, sy, t, shots=t < 23.0)
        # the boss bar and captions
        d = ImageDraw.Draw(f)
        d.rectangle([90, 4, 390, 18], fill=(10, 8, 12, 230), outline=(150, 110, 40, 255))
        hp = self.hp(t)
        if hp > 0:
            d.rectangle([92, 6, 92 + int(296 * hp), 16], fill=(200, 40, 40, 255))
        raster.draw_text(f, 196, 8, "IRON SOVEREIGN", (255, 220, 150))
        if 24.6 <= t < 25.6:
            fl = Image.new("RGBA", f.size, (255, 250, 236, int(230 * (1 - (t - 24.6)))))
            f.alpha_composite(fl)
        cap = None
        for t0, t1, text in CAPTIONS:
            if t0 <= t < t1:
                cap = text
        if cap:
            cap_at(f, cap)
        return f.convert("RGB")


def cap_at(img, text, col=(255, 210, 120)):
    w = raster.text_width(text) + 12
    d = ImageDraw.Draw(img)
    d.rectangle([8, FH - 26, 8 + w, FH - 12], fill=(10, 14, 22, 210), outline=(80, 110, 140, 255))
    raster.draw_text(img, 14, FH - 22, text, col)


CAPTIONS = [(0.0, 2.0, "INTRO: LIGHTS RING BY RING, THE RINGS SPIN UP"),
            (2.0, 5.0, "PHASE 1: ONLY BATTERIES IN THE RED ARC FIRE"),
            (5.0, 9.0, "THE ARC FOLLOWS THE SHIP: STEER WHICH PART IS ARMED"),
            (9.0, 13.0, "LATER - PHASE 2: OUTER RING LOCKED, THE OTHERS X1.25"),
            (13.0, 17.0, "LATER - ALL RINGS LOCKED: THE CORE OPENS (3 S)"),
            (17.0, 21.0, "PHASE 4: HEX SHIELD, SPIRAL, DART STREAM"),
            (21.0, SOV_DUR, "DEATH: COOK-OFFS RING BY RING, THE RINGS CRACK, THE CORE BLOWS")]


def sovereign_budget(v):
    """The one-page estimate from the measured segment sprites: the mean trimmed area of a segment
    over its headings x the heading count. Full sun: 2 px steps at the rim over 360 degrees;
    zenith plan: the segments lit from straight above (the sun's direction carried by a static rim
    overlay), so 90-degree turns and a mirror cover 360 from 45 degrees."""
    rows, full, zen = [], 0.0, 0.0
    for ring, (r0, r1, n) in RINGS.items():
        areas = []
        for d in range(0, 360, 5):
            bb = SEGS[(v, ring, d)].getchannel("A").getbbox()
            areas.append((bb[2] - bb[0]) * (bb[3] - bb[1]))
        heads = int(np.ceil(TAU * r1 / 2.0))
        a = float(np.mean(areas))
        full += a * heads
        zen += a * int(np.ceil(heads / 8))
        rows.append(f"{ring.upper()}: {n} SEGMENTS, {a / 1000:.1f} KPX EACH, {heads} HEADINGS (2 PX AT R {r1})")
    rest = 64 * (40 * 40 + 48 * 48 + 44 * 44) + 6 * 46 * 46 + 16 * 128 * 128 + 456 * 456 + 4 * 32 * 32
    page = 2048 * 2048 * 0.85
    return rows, (full + rest) / page, (zen + rest) / page, rest


# ===================================================================== 2 The mining tug (L31)

T_PX = 40.0
T_HULL, T_YEL, T_GREY, T_DARK, T_WIN, T_LAMP, T_ENG, T_OCHRE, T_BLUE, T_GLASS = range(10)
SECTIONS = {"bow": (1.35, 3.3), "cargo": (-1.35, 1.35), "engines": (-3.3, -1.35)}
SECT_OFF = {"bow": -90, "cargo": 0, "engines": 90}


def _blotch(seed, k):
    """Scorch: small dark burn patches (more and darker with ``k``), the whole section dimmed when lost."""
    def f(p, n):
        x, y = p[:, 0] * 6.3 + seed, p[:, 1] * 5.9 - seed
        v = np.sin(x) * np.sin(y * 1.3 + 0.7) + 0.6 * np.sin(x * 2.3 + y * 1.7) + 0.5 * np.sin(x * 4.1 - y * 3.3)
        burn = np.clip((v - (1.25 - 0.75 * k)) / 0.3, 0, 1)
        return (1 - 0.8 * burn) * (1 - 0.45 * k * k)
    return f


def tug_mats(state="intact", glow=1.0, seed=0.0):
    k = {"intact": 0.0, "damaged": 0.5, "lost": 1.0}[state]
    g = glow * (0.0 if state == "lost" else 1.0)
    pat = _blotch(seed, k) if k else None
    hull_p = (lambda p, n, a=sdf.panel_lines(5.0), b=pat: a(p, n) * (b(p, n) if b else 1.0))
    return [
        Material((0.8, 0.78, 0.72), metal=0.2, shininess=40, spec=0.4, pattern=hull_p),
        Material((0.92, 0.7, 0.14), metal=0.2, shininess=40, spec=0.4, pattern=pat),
        Material((0.5, 0.5, 0.53), metal=0.6, shininess=60, spec=0.6, pattern=pat),
        Material((0.12, 0.12, 0.13), metal=0.4, shininess=30, spec=0.3),
        Material((0.1, 0.08, 0.06), emission=tuple(np.array((1.0, 0.84, 0.52)) * 1.5 * g)),
        Material((0.1, 0.1, 0.12), emission=tuple(np.array((0.62, 0.9, 1.0)) * 1.8 * g)),
        Material((0.1, 0.1, 0.12), emission=tuple(np.array((0.55, 0.8, 1.0)) * 1.7 * g)),
        Material((0.62, 0.4, 0.2), metal=0.3, shininess=30, spec=0.3, pattern=pat),
        Material((0.42, 0.5, 0.58), metal=0.5, shininess=50, spec=0.5, pattern=pat),
        Material((0.2, 0.3, 0.4), metal=0.85, shininess=120, spec=1.1),
    ]


def _stripes(y, m_on, m_off, width=0.22):
    return np.where(((y / width) % 2) < 1, m_on, m_off)


def tug_scene(v, section):
    """The Bellwether (nose up = +y), one section's parts (clipped to its band)."""
    y0, y1 = SECTIONS[section]

    def clip(d, y):
        return np.maximum(d, np.maximum(y0 - y, y - y1))

    def scene(p):
        x, y, z = p[:, 0], p[:, 1], p[:, 2]
        q = sdf.mirror_x(p)
        items = []
        if v == "a":
            spine = sd_box(q, (0.55, -0.05, 0.0), (0.12, 3.15, 0.14), 0.03)
            items.append((clip(spine, y), _stripes(y + x * 0.3, T_YEL, T_DARK)))
            for cy in (1.35, 0.0, -1.35):
                cross = sd_box(p, (0, cy, 0.02), (1.82, 0.1, 0.1), 0.02)
                items.append((clip(cross, y), T_GREY))
            if section == "bow":
                items += [(sd_box(p, (0, 2.35, 0.12), (1.05, 0.62, 0.3), 0.18), T_HULL),
                          (sd_box(p, (0, 2.86, 0.28), (0.8, 0.12, 0.08), 0.05), T_GLASS),
                          (sd_box(p, (0, 2.9, 0.3), (0.72, 0.05, 0.05), 0.02), T_WIN),
                          (sd_box(p, (0, 1.85, 0.44), (0.9, 0.06, 0.05), 0.02), T_YEL),
                          (sd_capsule(p, (0.7, 2.0, 0.5), (1.55, 3.05, 0.6), 0.09, 0.07), T_YEL),
                          (sd_capsule(p, (1.55, 3.05, 0.6), (1.2, 2.55, 0.66), 0.06), T_YEL),
                          (sd_box(p, (1.12, 2.45, 0.66), (0.22, 0.18, 0.1), 0.04), T_GREY),
                          (sd_sphere(q, (1.0, 2.95, 0.3), 0.07), T_LAMP)]
            elif section == "cargo":
                for cx in (-1.35, -0.45, 0.45):
                    items.append((sd_capsule(p, (cx, -1.0, 0.25), (cx, 1.0, 0.25), 0.36), T_HULL))
                    wy = (y + 0.9) % 0.32
                    win = sd_capsule(p, (cx, -0.85, 0.6), (cx, 0.85, 0.6), 0.05)
                    items.append((np.maximum(win, np.abs(wy - 0.16) - 0.07), T_WIN))
                items.append((sd_box(p, (1.4, 0.45, 0.2), (0.38, 0.5, 0.32), 0.04), T_OCHRE))
                items.append((sd_box(p, (1.4, -0.65, 0.2), (0.38, 0.5, 0.32), 0.04), T_OCHRE))
                for cy in (-0.6, 0.6):
                    items.append((sd_capsule(p, (-1.8, cy, 0.62), (1.8, cy, 0.62), 0.025), T_DARK))
                items.append((sd_sphere(q, (1.82, 1.2, 0.2), 0.07), T_LAMP))
            else:
                ring = np.sqrt((np.hypot(x, z - 0.1) - 1.05) ** 2 + (y + 1.75) ** 2) - 0.2
                items.append((ring, T_GREY))
                items.append((sd_cylinder_z(p, (0, -2.05, 0.15), 0.62, 0.28), T_BLUE))
                for cx in (-0.95, 0.0, 0.95):
                    items.append((sd_capsule(p, (cx, -2.25, 0.1), (cx, -3.05, 0.1), 0.24, 0.42), T_GREY))
                    items.append((sd_cylinder_z(p, (cx, -2.45, 0.42), 0.18, 0.06), T_DARK))
                items.append((sd_box(p, (0, -1.6, 0.18), (1.4, 0.16, 0.12), 0.03), T_YEL))
                items.append((sd_sphere(q, (1.55, -1.55, 0.3), 0.07), T_LAMP))
        else:
            waist = 0.2 * np.exp(-((y - 1.35) / 0.12) ** 2) + 0.2 * np.exp(-((y + 1.35) / 0.12) ** 2)
            body = sdf.sd_ellipsoid(p, (0, -0.05, 0.05), (1.3, 3.25, 0.5)) + waist * 0.5
            body = np.maximum(body, np.abs(x) - 1.15 + waist)
            if section == "bow":
                m = np.where(y < 1.62, _stripes(y + x, T_YEL, T_DARK, 0.14), T_HULL)
                items += [(clip(body, y), m),
                          (sd_box(p, (0, 2.55, 0.42), (0.46, 0.18, 0.08), 0.05), T_GLASS),
                          (sd_box(p, (0, 2.58, 0.47), (0.4, 0.06, 0.05), 0.02), T_WIN),
                          (sd_capsule(p, (-0.6, 1.9, 0.5), (-0.95, 2.8, 0.55), 0.07), T_YEL),
                          (sd_sphere(q, (0.85, 2.6, 0.3), 0.07), T_LAMP)]
            elif section == "cargo":
                m = np.where(np.abs(x) < 0.55, T_HULL, T_BLUE)
                items.append((clip(body, y), m))
                for cx in (-1.25, 1.25):
                    items.append((sd_capsule(p, (cx, -1.05, 0.15), (cx, 1.05, 0.15), 0.3), T_GREY))
                wy = (y + 1.0) % 0.3
                win = sd_box(q, (0.38, 0.0, 0.5), (0.05, 1.1, 0.06))
                items.append((np.maximum(win, np.abs(wy - 0.15) - 0.06), T_WIN))
                for cy in (-0.7, 0.0, 0.7):
                    items.append((sd_capsule(p, (-1.5, cy, 0.42), (1.5, cy, 0.42), 0.03), T_DARK))
            else:
                m = np.where(y > -1.62, _stripes(y + x, T_YEL, T_DARK, 0.14), T_GREY)
                items.append((clip(body, y), m))
                items.append((sd_capsule(p, (0, -2.4, 0.2), (0, -3.2, 0.2), 0.3, 0.5), T_GREY))
                for cx, cy in ((-0.78, -2.6), (0.78, -2.6)):
                    items.append((sd_capsule(p, (cx, cy, 0.15), (cx, cy - 0.5, 0.15), 0.14, 0.24), T_GREY))
                items.append((sd_sphere(q, (1.0, -1.9, 0.35), 0.07), T_LAMP))
        return union(*items)
    return scene


TUG_SIZE = (168, 272)


def tug_section_job(job):
    v, section, state, glow = job
    key = f"tug-{v}-{section}-{state}-{glow:.1f}.png"
    if CACHE and (Path(CACHE) / key).exists():
        return Image.open(Path(CACHE) / key).convert("RGBA")
    w, h = TUG_SIZE
    hi = sdf.render(tug_scene(v, section), tug_mats(state, glow, seed={"bow": 1, "cargo": 2, "engines": 3}[section]),
                    (w * 3, h * 3), w / T_PX, steps=90)
    sp = sprite.make_sprite(hi, 3, 40)
    if CACHE:
        Path(CACHE).mkdir(parents=True, exist_ok=True)
        sp.save(Path(CACHE) / key)
    return sp


TUG = {}


def load_tug(v):
    jobs = [(v, s, st, g) for s in SECTIONS for st in ("intact", "damaged", "lost") for g in ((1.0, 0.3) if st == "damaged" else (1.0,))]
    jobs = [j for j in jobs if j not in TUG]
    if jobs:
        with ProcessPoolExecutor(WORKERS, mp_context=multiprocessing.get_context("fork")) as pool:
            for j, sp in zip(jobs, pool.map(tug_section_job, jobs)):
                TUG[j] = sp


def tug_sprite(v, states, flicker=False, name=True):
    out = Image.new("RGBA", TUG_SIZE, (0, 0, 0, 0))
    for s in ("engines", "cargo", "bow"):
        st = states[s]
        g = 0.3 if (st == "damaged" and flicker) else 1.0
        out.alpha_composite(TUG[(v, s, st, g)])
        if st == "lost":
            out = _scorch_outline(out, s)
    if name and states["bow"] != "lost":
        nm = Image.new("RGBA", (64, 9), (0, 0, 0, 0))
        raster.draw_text(nm, 1, 1, "BELLWETHER", (40, 36, 30))
        nm = nm.resize((44, 6), Image.LANCZOS)
        out.alpha_composite(nm, (84 - 22, 32 if v == "a" else 44))
    return out


def _scorch_outline(img, section):
    a = np.array(img).astype(np.float64)
    y0, y1 = SECTIONS[section]
    h = TUG_SIZE[1]
    r0, r1 = int(h / 2 - y1 * T_PX), int(h / 2 - y0 * T_PX)
    band = a[r0:r1]
    op = band[..., 3] > 0
    pad = np.pad(op, 1)
    edge = op & ~(pad[:-2, 1:-1] & pad[2:, 1:-1] & pad[1:-1, :-2] & pad[1:-1, 2:])
    band[edge, :3] = band[edge, :3] * 0.3 + np.array((120, 54, 20)) * 0.7
    band[op & ~edge, :3] *= 0.8
    a[r0:r1] = band
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def section_xy(tx, ty, s):
    return tx, ty + SECT_OFF[s]


TUG_DUR = 9.0
TUG_ST = (240, 390)


def tug_pos(t):
    th = TAU * (t + 3.0) / 14.0
    return TUG_ST[0] + 20 * np.sin(th), TUG_ST[1] + 5 * np.sin(2 * th)


class TugScene:
    """L31 in miniature: the tug on station, swaying; two Hornets off the top send hooked homing
    diamonds at its nearest section: hits flash white, the cargo drops below 50 % (venting,
    flickering lights), the bow is lost (a fire flare, then dark)."""
    HITS = [(1.6, "bow"), (2.6, "cargo"), (3.4, "cargo"), (4.6, "bow"), (5.4, "bow"), (6.2, "bow")]
    STATE = [(3.4, "cargo", "damaged"), (5.4, "bow", "damaged"), (6.2, "bow", "lost")]

    def __init__(self, v):
        self.v = v
        self.bg = u60.Scroll(u60.space_bg, 311, TUG_DUR, 40.0)
        self.hornets = [(120, 70), (360, 60)]
        self.homing = []
        for th, s in self.HITS:
            hx_, hy = self.hornets[0] if tug_pos(th)[0] + 0 < 240 and s == "bow" else self.hornets[len(self.homing) % 2]
            tx, ty = section_xy(*tug_pos(th), s)
            fl = np.hypot(tx - hx_, ty - hy) / 120.0
            self.homing.append(dict(t0=th - fl, t1=th, x0=hx_, y0=hy, s=s))

    def states(self, t):
        st = {s: "intact" for s in SECTIONS}
        for t0, s, state in self.STATE:
            if t >= t0:
                st[s] = state
        return st

    def frame(self, t):
        v = self.v
        f = self.bg.frame(t)
        for k, (rx, ry0, sp) in enumerate(((90, -60, 0), (400, 120, 1), (300, -260, 2))):
            ry = self.bg.y(ry0, t) * 1.2
            sprite.paste_center(f, u60.ROCK_L[sp], rx, ry)
        tx, ty = tug_pos(t)
        st = self.states(t)
        flick = int(t * 9) % 3 == 0
        tsp = tug_sprite(v, st, flicker=flick)
        sprite.paste_center(f, sprite.shadow_of(tsp, opacity=0.35, blur=3.0), tx + 10, ty + 12)
        for s in SECTIONS:                                      # hit flashes
            for th, hs in self.HITS:
                if hs == s and th <= t < th + 0.12:
                    tsp = _flash_band(tsp, s)
        sprite.paste_center(f, tsp, tx, ty)
        for cx in (-38, 0, 38) if v == "a" else (-31, 0, 31):  # drive glow under the living engines
            if st["engines"] != "lost":
                f = light(f, tx + cx, ty + 128, 9, (140, 200, 255), 0.8)
        for s in SECTIONS:
            sx, sy = section_xy(tx, ty, s)
            if st[s] == "damaged":                               # vacuum venting: thin plumes drift back
                d = ImageDraw.Draw(f)
                for k in range(6):
                    age = (t * 1.3 + k / 6) % 1.0
                    px, py = sx + 30 - 18 * age + 6 * np.sin(k), sy - 10 + 70 * age
                    r = 2 + 5 * age
                    d.ellipse([px - r, py - r, px + r, py + r], fill=(200, 205, 210, int(120 * (1 - age))))
            for t0, s2, state in self.STATE:
                if s2 == s and state == "lost" and t0 <= t < t0 + 0.8:
                    f = u60.boom(f, sx, sy, (t - t0) / 0.8, "medium", sparks=(255, 170, 70))
                    f = light(f, sx, sy, 30, (255, 150, 60), 0.9 * (1 - (t - t0) / 0.8))
        for hx_, hy in self.hornets:
            sprite.paste_center(f, u60.hornets["a"].get(DOWN, hatch=0.0), hx_, hy)
        for m in self.homing:
            if m["t0"] <= t < m["t1"]:
                u = (t - m["t0"]) / (m["t1"] - m["t0"])
                tx2, ty2 = section_xy(*tug_pos(m["t1"]), m["s"])
                mx = m["x0"] + (tx2 - m["x0"]) * u + 40 * np.sin(u * np.pi) * (1 if m["x0"] < 240 else -1)
                my = m["y0"] + (ty2 - m["y0"]) * u
                h = np.arctan2(ty2 - my, tx2 - mx)
                sprite.paste_center(f, u60.diamond_at("a", h), mx, my)
            elif m["t1"] <= t < m["t1"] + 0.35:
                tx2, ty2 = section_xy(*tug_pos(m["t1"]), m["s"])
                f = u60.boom(f, tx2, ty2 + 10, (t - m["t1"]) / 0.35, "tiny")
        px = 240 + 150 * np.sin(t * 0.8)
        u60.ship(f, px, 500, t)
        cap = "THE TUG HOLDS STATION; ONLY HOOKED ATTACKS HURT IT" if t < 3.4 else \
            ("BELOW 50 %: VENTING, LIGHTS FLICKER" if t < 6.2 else "SECTION LOST: DARK, THE TUG FLIES ON")
        cap_at(f, cap)
        hud = f"TUG {sum(1 for s in st if st[s] != 'lost')} / 3"
        d = ImageDraw.Draw(f)
        d.rectangle([FW - 96, 6, FW - 8, 40], fill=(10, 14, 22, 210), outline=(80, 110, 140, 255))
        raster.draw_text(f, FW - 90, 10, hud, (200, 230, 255))
        for i, s in enumerate(("bow", "cargo", "engines")):
            col = {"intact": (120, 220, 160), "damaged": (240, 170, 60), "lost": (60, 30, 30)}[st[s]]
            d.rectangle([FW - 90 + i * 27, 24, FW - 68 + i * 27, 34], fill=col + (255,))
        return f.convert("RGB")


def _flash_band(img, section):
    a = np.array(img).astype(np.float64)
    y0, y1 = SECTIONS[section]
    h = TUG_SIZE[1]
    r0, r1 = int(h / 2 - y1 * T_PX), int(h / 2 - y0 * T_PX)
    op = a[r0:r1, :, 3] > 0
    a[r0:r1][op, :3] = a[r0:r1][op, :3] * 0.4 + 255 * 0.6
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# ===================================================================== 3 The wavering Stormhawks (L29, Q10 b)

from render import models  # noqa: E402
import artkit  # noqa: E402

SH_SIZE, SH_EXT, SH_CAM = 40, 2.3, 4.0
SH_PAL = {
    "a": {"hull": (0.72, 0.74, 0.77), "hull_dark": (0.3, 0.32, 0.36), "accent": (0.8, 0.58, 0.28),
          "accent2": (0.5, 0.33, 0.15), "engine": (0.55, 0.8, 1.0)},
    "b": {"hull": (0.7, 0.72, 0.75), "hull_dark": (0.26, 0.28, 0.32), "accent": (0.08, 0.07, 0.09),
          "accent2": (0.95, 0.7, 0.2), "engine": (0.55, 0.8, 1.0)},
}
S_IFF, S_GOLD, S_BLACK = 7, 8, 9


def stormhawk_model(v, heading=DOWN, bank=0.0, iff="off"):
    """The CDF Stormhawk airframe (ship_c_model) in Ceres Wing's colours, the Ascendancy crest (a gold
    sun and ring on black) painted over the roundel on both wings, the white IFF light on the spine."""
    scene0, mats = models.ship_c_model(bank=np.radians(bank), palette=SH_PAL[v])
    rot = -(heading - UP)

    def scene(p):
        p = sdf.rotate_z(p, rot)
        d, m = scene0(p)
        q = sdf.mirror_x(sdf.rotate_y(p, np.radians(bank)))
        rr = np.hypot(q[:, 0] - 0.56, q[:, 1] + 0.22)
        crest = (rr < 0.15) & (m != models.GLASS) & (m != models.ENGINE)
        m = np.where(crest, np.where((rr < 0.06) | (np.abs(rr - 0.115) < 0.018), S_GOLD, S_BLACK), m)
        if v == "b":
            over = (q[:, 0] > 0.36 + 0.04 * np.sin(q[:, 1] * 23)) & (m == models.HULL)
            m = np.where(over, S_BLACK, m)
        lamp = sd_sphere(sdf.rotate_y(p, np.radians(bank)), (0, -0.18, 0.14), 0.07)
        d2 = np.minimum(d, lamp)
        m = np.where(lamp < d, S_IFF, m)
        return d2, m
    col = {"off": (0.25, 0.25, 0.25), "white": (1.0, 1.0, 1.0), "red": (1.0, 0.15, 0.1)}[iff]
    k = 0.2 if iff == "off" else 2.2
    return scene, mats + [Material((0.2, 0.2, 0.2), emission=tuple(np.array(col) * k)),
                          Material((0.95, 0.72, 0.2), metal=0.9, shininess=110, spec=1.1),
                          Material((0.05, 0.05, 0.06), metal=0.4, shininess=60, spec=0.6)]


@lru_cache(maxsize=None)
def stormhawk(v, k16=4, bank=0, iff="off"):
    heading = k16 * TAU / 16
    scene, mats = stormhawk_model(v, heading, bank, iff)
    hi, factor = artkit.render_hi(artkit.perspective(scene, SH_CAM), mats, (SH_SIZE, SH_SIZE), SH_EXT)
    return sprite.make_sprite(hi, factor, 32)


def hawk_at(v, h, bank, iff):
    k = int(round((h % TAU) / (TAU / 16))) % 16
    b = int(np.clip(round(bank / 15) * 15, -30, 30))
    return stormhawk(v, k, b, iff)


def red_canyon_bg(seed, h=FH):
    """Ceres Hub's dock canyon after the turn (L29 section 4): the station hull under red light."""
    img = u60.hull_bg(seed, h)
    a = np.array(img).astype(np.float64)
    a[..., :3] = a[..., :3] * np.array((0.78, 0.5, 0.5))
    img = Image.fromarray(a.astype(np.uint8), "RGBA")
    rng = np.random.default_rng(seed + 5)
    d = ImageDraw.Draw(img)
    for _ in range(h // 60):
        x, y = rng.uniform(0, FW), rng.uniform(0, h)
        d.rectangle([x - 1, y - 1, x + 1, y + 1], fill=(255, 60, 50, 255))
    return img


MUT_DUR = 10.5
PAIR = [(-34, 0.6), (34, 0.9)]                       # x offset, entry delay
PAIR_X = 250


def hawk_state(i, t):
    """(x, y, heading, bank) of wavering craft i: in at 90 px/s, hold with a sway and a wing-waggle,
    peel off to the nearer side at 260 px/s."""
    dx, t0 = PAIR[i]
    x0 = PAIR_X + dx
    hold0 = t0 + (150 + 30) / 90.0
    if t < hold0:
        return x0, -30 + 90 * max(0.0, t - t0), DOWN, 0.0
    if t < hold0 + 6.0:
        u = t - hold0
        bank = 30 * np.sin(u * TAU * 1.2) if (1.0 < u < 2.2 or 3.6 < u < 4.8) else 0.0
        return x0 + 10 * np.sin(u * 1.4 + i), 150 + 4 * np.sin(u * 2.0), DOWN, bank
    u = t - hold0 - 6.0
    side = -1 if x0 < 240 else 1
    turn = min(1.0, u / 0.6)
    h = DOWN - side * turn * np.pi / 2
    return x0 + side * 260 * max(0.0, u - 0.3), 150 + 40 * min(u, 0.6), h, side * 30 * (1 - abs(1 - 2 * turn))


class MutineerScene:
    """L29 section 4 in miniature: a Talon V-wing attacks; a wavering pair enters, blinks its white
    IFF light (1 Hz), holds fire with a wing-waggle and peels off; a stray shot flashes one red."""
    HIT = (0, 4.4)

    def __init__(self, v):
        self.v = v
        self.bg = u60.Scroll(red_canyon_bg, 291, MUT_DUR, 100.0)
        self.shots = u60.Shots()
        for k, tf in enumerate((3.0, 4.2, 5.4, 6.6)):
            x, y = self.talon(1 + k % 3, tf)[:2]
            self.shots.fire(tf, x, y, *self.ship(tf), speed=170, r=4)

    def ship(self, t):
        return 240 + 120 * np.sin(t * 0.7), 470.0

    def talon(self, i, t):
        dx = [0, -30, 30, -60, 60][i]
        dy = [0, -28, -28, -56, -56][i]
        x = 520 - 70 * (t - 1.5) + dx
        y = 60 + 120 * np.sin((t - 1.5) * 0.35) + dy * -1
        return x, y, np.radians(160)

    def frame(self, t):
        v = self.v
        f = self.bg.frame(t)
        for i in range(5):
            if 1.5 <= t < 9.5:
                x, y, h = self.talon(i, t)
                sprite.paste_center(f, u60.talons[("a", i == 0)].get(h), x, y)
        for i in range(2):
            x, y, h, bank = hawk_state(i, t)
            if -40 < x < FW + 40 and -40 < y:
                hold = PAIR[i][1] + 2.0 < t
                iff = "white" if (hold and (t % 1.0) < 0.5) or (not hold and (t % 1.0) < 0.5) else "off"
                if i == self.HIT[0] and self.HIT[1] <= t < self.HIT[1] + 0.4:
                    iff = "red"
                sp = hawk_at(v, h, bank, iff)
                sprite.paste_center(f, sprite.shadow_of(sp, opacity=0.4, blur=1.5), x + 8, y + 12)
                sprite.paste_center(f, sp, x, y)
                if iff == "white":
                    f = light(f, x, y + 4, 7, (255, 255, 255), 0.7)
                if iff == "red":
                    f = light(f, x, y + 4, 8, (255, 60, 40), 0.9)
                    f = u60.spark(f, x - 6, y + 6, (t - self.HIT[1]) / 0.4)
        self.shots.draw(f, t)
        px, py = self.ship(t)
        u60.ship(f, px, py, t)
        d = ImageDraw.Draw(f)
        d.rectangle([FW - 110, 6, FW - 8, 22], fill=(10, 14, 22, 210), outline=(80, 110, 140, 255))
        raster.draw_text(f, FW - 104, 10, "HOLD FIRE" if t < 10.0 else "HOLD FIRE  1/3", (200, 230, 255))
        cap = "CERES WING STORMHAWKS: WHITE IFF BLINK, NO FIRE, NO CONTACT" if t < 4.4 else \
            ("A HIT FLASHES THE IFF RED (A WARNING, NOT A FAIL)" if t < 6.5 else "THEY PEEL OFF TO THE NEARER SIDE")
        cap_at(f, cap)
        return f.convert("RGB")


# ===================================================================== parallel frames

_SCENE = None


def _frame_job(t):
    return _SCENE.frame(t) if not hasattr(_SCENE, "bgimg") else _SCENE.frame(_SCENE.bgimg, t)


def render_frames(scene, dur):
    """All frames of ``scene`` in parallel (forked workers share its precomputed sprites)."""
    global _SCENE
    _SCENE = scene
    ts = times(dur)
    with ProcessPoolExecutor(WORKERS, mp_context=multiprocessing.get_context("fork")) as pool:
        return list(pool.map(_frame_job, ts, chunksize=4))


# ===================================================================== sheets

def sheet(title, panels, notes, game_frame):
    return u60.r5.build_sheet(title, panels, notes, game_frame)


def keyframes(frames, ts, labels, scale=0.3):
    """Frames of a 15 fps GIF at times ``ts`` side by side, scaled down."""
    ims = [frames[min(len(frames) - 1, int(t * FPS))].resize((int(FW * scale), int(FH * scale)), Image.LANCZOS)
           for t in ts]
    img = Image.new("RGBA", (len(ims) * (ims[0].width + 6), ims[0].height + 12), raster.SHEET_BG)
    for i, (im, lb) in enumerate(zip(ims, labels)):
        img.alpha_composite(im.convert("RGBA"), (i * (im.width + 6), 0))
        raster.draw_text(img, i * (im.width + 6), im.height + 2, lb, raster.LABEL_DIM)
    return img


def rest_station(v, rots, size=470):
    """The station at rest on its own canvas: rings, batteries (out of the arc), the shut core."""
    c = size // 2
    bg = u60.space_bg(357, size).crop((0, 0, size, size)) if size <= FW else None
    lay = station_layer(v, rots, {}, size=(size, size), centre=(c, c))
    img = bg.copy()
    img.alpha_composite(lay)
    for ring in RINGS:
        for x, y, a in batteries(rots[ring], ring):
            x, y = x - CX + c, y - CY + c
            sprite.paste_center(img, battery_sprite(ring, a, False), x, y)
    sprite.paste_center(img, core_at(0, 0, 0.4), c, c)
    return img, lay


def sovereign_sheet(v, frames):
    rots = {"outer": 7.0, "middle": 11.0, "inner": 3.0}
    st, lay = rest_station(v, rots)
    gold = u60.gold_share(lay)
    jd = rots["outer"] + 15 + 30 * 2                    # a joint of the outer ring near the bottom
    jx, jy = 235 + 200 * np.cos(np.radians(jd)), 235 + 200 * np.sin(np.radians(jd))
    seam = sprite.enlarge(st.crop((int(jx) - 24, int(jy) - 24, int(jx) + 24, int(jy) + 24)), 4)
    segs = [SEGS[(v, r, 90)] for r in ("outer", "middle", "inner")]
    seg_panel = panel_grid(segs, 3, labels=["OUTER (12)", "MIDDLE (8)", "INNER (6)"])
    bats = []
    labs = []
    for ring in ("outer", "middle", "inner"):
        kind = BAT[ring][0].upper()
        bats += [battery_sprite(ring, DOWN, False), battery_sprite(ring, DOWN, True, 1.0 if kind != "SAM" else 0.0,
                                                                   1.0 if kind == "SAM" else 0.0), wreck_sprite(ring)]
        labs += [f"{kind} ARC OFF", f"{kind} LIVE+TELL", f"{kind} WRECK"]
    bat_panel = u60.row(bats, 52, 2, labels=labs, bg=(26, 24, 30))
    cores = [core_at(0, 0, 0.4), core_at(0, 1, 0.4), core_at(0.5, 0, 0.7), core_at(1.0, 0, 1.0)]
    shielded = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    shielded.alpha_composite(cores[3])
    shielded = u60.shield(shielded, cores[3].crop((16, 16, 112, 112)), 64, 64, mode="bubble", alpha=0.4)
    core_panel = u60.row(cores + [shielded], 130, 1, labels=["SHUT", "FAN TELL", "OPENING", "OPEN", "+ HEX SHIELD"],
                         bg=(26, 24, 30))
    f4 = frames[int(4.0 * FPS)].convert("RGBA")
    rd = [sprite.enlarge(f4.crop((40, 200, 160, 320)), 2), sprite.enlarge(f4.crop((300, 230, 420, 350)), 2)]
    read_panel = panel_grid(rd, 2, labels=["LIVE BATTERY + ARC BAND, 2X", "OUTER RIM OVER CERES, 2X"])
    rows, full, zen, rest = sovereign_budget(v)
    kf = keyframes(frames, [1.0, 4.0, 10.0, 15.0, 18.6, 24.3],
                   ["INTRO", "PHASE 1", "PHASE 2", "CORE OPENS", "PHASE 4", "DEATH"], 0.27)
    name = {"a": "A - CONTINUOUS WHEEL", "b": "B - ARMOURED LINKS"}[v]
    desc = {"a": "SEGMENTS BUTT INTO ONE SMOOTH RING (SEAMS HIDDEN IN THE PANEL JOINTS); GOLD TEETH ON EVERY RIM, "
                 "RED LIGHTS ON THE RIBS.",
            "b": "EVERY SEGMENT A SEPARATE ARMOUR BLOCK: A DARK GAP AND A GOLD-EDGED CAP PLATE; THE GAPS READ THE "
                 "TURNING (SEAMS BY DESIGN)."}[v]
    notes = [f"IRON SOVEREIGN {name} (Q7 A, THE TOP-DOWN RINGS; Q11 A LOOK)",
             desc,
             f"STATION 440 PX: CORE 80, RINGS 70-100 / 123-157 / 180-220; GOLD ON {100 * gold:.0f} % OF THE HULL (RULE >= 20 %).",
             "OUTER 6 SAM 6 DEG/S CW, MIDDLE 4 RAIL 10 DEG/S CCW, INNER 3 LASER 120 DEG IN 4 S + 4 S HOLD.",
             "FIRE ARC 120 DEG ON THE SHIP'S BEARING (CLAMPED 30-150): RED BAND ON THE HULL, LIGHTS GOLD -> RED.",
             "SEGMENTS RENDERED AT 1-DEG HEADINGS UNDER ONE FIXED SUN; THE GOLD/RED RIM ON THE WHOLE SILHOUETTE.",
             "ATLAS (MEASURED, 2048X2048 PAGES AT 85 % PACKING):"] + ["  " + r for r in rows] + [
             f"  + BATTERIES 64 HEADINGS, CORE 16 FRAMES, BASE, BAYS: {rest / 1e6:.2f} MPX",
             f"  FULL SUN, 2 PX STEPS: {full:.1f} PAGES - OVER THE ONE-PAGE BOSS BUDGET.",
             f"  ZENITH-LIT SEGMENTS (90-DEG TURNS + MIRROR, 45 DEG RENDERED) + A STATIC RIM OVERLAY: {zen:.2f} PAGES."]
    panels = [("THE STATION AT REST (1X), RINGS BUILT FROM SEGMENTS", st),
              ("SEGMENTS (1X), HEADING 90", seg_panel),
              ("A SEAM OF THE OUTER RING (4X)", seam),
              ("BATTERIES (2X): THE UNITS THE PLAYER KNOWS, IN THE STATION'S LIVERY", bat_panel),
              ("THE CORE (1X)", core_panel),
              ("READABILITY (FROM THE GIF)", read_panel),
              ("KEY FRAMES", kf)]
    return sheet(f"IRON SOVEREIGN {name}", panels, notes, frames[int(6.8 * FPS)])


def make_sovereign(variants):
    for v in variants:
        prepare(v)
        sov = Sovereign(v)
        sov.bgimg = ceres_bg()
        frames = render_frames(sov, SOV_DUR)
        d = DESIGN / "enemies" / "bosses" / "iron-sovereign" / "concept"
        save_png(sovereign_sheet(v, frames), d / f"iron-sovereign-r60-{v}.png")
        save_gif(frames, d / f"iron-sovereign-r60-{v}.gif")


def tug_sheet(v, frames):
    load_tug(v)
    intact = {s: "intact" for s in SECTIONS}
    t0 = tug_sprite(v, intact)
    boxes = t0.copy()
    d = ImageDraw.Draw(boxes)
    for s, (w, h) in (("bow", (112, 68)), ("cargo", (148, 92)), ("engines", (136, 68))):
        cy = 136 + SECT_OFF[s]
        d.rectangle([84 - w / 2, cy - h / 2, 84 + w / 2, cy + h / 2], outline=(120, 230, 255, 255))
        raster.draw_text(boxes, int(84 - w / 2) + 2, int(cy - h / 2) + 2, s.upper(), (120, 230, 255))
    states = [tug_sprite(v, dict(intact, cargo="damaged")), tug_sprite(v, dict(intact, cargo="damaged"), flicker=True),
              tug_sprite(v, dict(intact, cargo="damaged", bow="lost")),
              tug_sprite(v, {s: "lost" for s in SECTIONS})]
    st_panel = panel_grid(states, 4, labels=["CARGO < 50 %", "  LIGHTS FLICKER", "BOW LOST", "ALL LOST (FAIL)"],
                          bg=(14, 14, 20))
    glance = u60.space_bg(312, 300).crop((0, 0, 360, 300))
    sprite.paste_center(glance, t0, 110, 150)
    sprite.paste_center(glance, u60.talons[("a", True)].get(DOWN), 250, 80)
    sprite.paste_center(glance, u60.hornets["a"].get(DOWN), 300, 190)
    sprite.paste_center(glance, e3.player_sprite(), 250, 260)
    red = np.array(t0).astype(np.float64)
    op = red[..., 3] > 0
    redshare = ((red[..., 0] > 150) & (red[..., 1] < 0.45 * red[..., 0]) & op).sum() / max(1, op.sum())
    kf = keyframes(frames, [1.0, 3.0, 4.5, 6.6], ["ON STATION", "HOOKED HIT", "VENTING", "BOW LOST"], 0.3)
    name = {"a": "A - ORE FRAME", "b": "B - PRESSURE HULL"}[v]
    desc = {"a": "AN OPEN ORE FRAME OF YELLOW-AND-GREY HAZARD GIRDERS: A BLUNT BRIDGE WITH A CRANE AND GRAPPLE, "
                 "WHITE HABITAT CANS",
            "b": "AN ENCLOSED, OLDER PRESSURE HULL: THE SECTIONS TOLD BY COLOUR BANDS AND HAZARD CHEVRONS AT THE "
                 "WAISTS, WINDOW"}[v]
    desc2 = {"a": "LASHED ON WITH CABLES (LIT WINDOWS) BESIDE TWO OCHRE ORE BOXES, THREE BIG DRIVE BELLS IN A TANK RING.",
             "b": "ROWS DOWN THE CARGO, SIDE TANKS STRAPPED ON, ONE BIG BELL AND TWO SMALL ONES."}[v]
    notes = [f"MINING TUG {name} (THE BELLWETHER, LEVEL 31; Q4 A, Q-D1 A)", desc, desc2,
             "160X260 FOOTPRINT ON THE GROUND LAYER, ONE HEADING; BOW (0,-90) 112X68, CARGO (0,0) 148X92, ENGINES (0,+90) 136X68.",
             "STATES PER SECTION: INTACT; < 50 % VENTING + FLICKER; LOST = DARK, SCORCHED OUTLINE, THE TUG FLIES ON.",
             f"READS FRIENDLY: WARM YELLOW AND WHITE, COOL RUNNING LIGHTS, NO RED ({100 * redshare:.1f} % RED PIXELS).",
             "THE NAME BELLWETHER HAND-PAINTED ON THE BOW (A PLACEHOLDER, ROUND 60)."]
    panels = [("THE TUG (1X)", t0), ("SECTIONS AND HIT BOXES (1X)", boxes),
              ("DAMAGE STATES (1X)", st_panel),
              ("AT A GLANCE (1X): THE TUG BESIDE A TALON, A HORNET AND THE PLAYER", glance),
              ("KEY FRAMES", kf)]
    return sheet(f"MINING TUG {name}", panels, notes, frames[int(4.6 * FPS)])


def make_tug(variants):
    for v in variants:
        load_tug(v)
        sc = TugScene(v)
        frames = render_frames(sc, TUG_DUR)
        d = DESIGN / "allies" / "mining-tug" / "concept"
        save_png(tug_sheet(v, frames), d / f"mining-tug-r60-{v}.png")
        save_gif(frames, d / f"mining-tug-r60-{v}.gif")


def mutineer_sheet(v, frames):
    scene, mats = stormhawk_model(v, UP, 0, "white")
    hi = sdf.render(scene, mats, (260, 260), SH_EXT)
    src = sprite.to_image(hi, 0.5) if hasattr(sprite, "to_image") else Image.fromarray((hi * 255).astype(np.uint8))
    banks = [hawk_at(v, DOWN, b, "off") for b in (-30, -15, 0, 15, 30)]
    bank_panel = u60.row(banks, 44, 3, labels=["-30", "-15", "0", "+15", "+30"], bg=(40, 30, 32))
    iffs = [hawk_at(v, DOWN, 0, s) for s in ("off", "white", "red")]
    iff_panel = u60.row(iffs, 44, 3, labels=["IFF OFF", "WHITE (1 HZ)", "RED: HIT"], bg=(40, 30, 32))
    prod = lambda n: Image.open(ROOT / "assets" / "sprites" / f"{n}_2.png").convert("RGBA")
    glance = [hawk_at(v, DOWN, 0, "white"), prod("ship"), prod("rook"), u60.talons[("a", True)].get(DOWN),
              u60.talons[("a", False)].get(DOWN)]
    gl_panel = u60.row(glance, 52, 2, labels=["CERES WING", "PLAYER", "ROOK", "TALON LEAD", "TALON"],
                       bg=(60, 40, 40))
    kf = keyframes(frames, [1.5, 4.5, 6.0, 9.6], ["ENTRY", "HIT: RED", "HOLD", "PEEL OFF"], 0.3)
    name = {"a": "A - CERES WING OCHRE", "b": "B - HASTY BLACKOUT"}[v]
    desc = {"a": "THE CDF AIRFRAME IN CERES WING'S OWN SAND-OCHRE PANELS OVER CDF GREY; THE ASCENDANCY CREST (GOLD SUN "
                 "AND RING ON BLACK)",
            "b": "THE CDF SCHEME HALF OVERPAINTED BLACK ON THE WINGS (A RAGGED BRUSHED EDGE), GOLD TRIM, THE CREST "
                 "STENCILLED"}[v]
    notes = [f"WAVERING STORMHAWK {name} (Q10 B, LEVEL 29'S HOLD FIRE SECONDARY)", desc,
             "FRESHLY PAINTED OVER THE ROUNDEL ON BOTH WINGS; A WHITE IFF LIGHT ON THE SPINE." if v == "a" else
             "OVER THE ROUNDEL; READS HALF-TURNED; THE WHITE IFF LIGHT ON THE SPINE.",
             "40X40 (HITBOX 28), THE STORMHAWK'S CAMERA AND BANK FRAMES; IFF BLINKS 1 HZ, RED FLASH ON A HIT.",
             "A PAIR ENTERS, DESCENDS 90 PX/S TO 150 PX, HOLDS ~6 S (SWAY, WING-WAGGLE), PEELS OFF AT 260 PX/S.",
             "NO FIRE, NO CONTACT, 0 CREDITS, NEVER COUNTED AS ENEMIES; HOMING SHOTS AND ROOK NEVER PICK THEM."]
    panels = [("SOURCE RENDER (IFF ON)", src.resize((260, 260)) if src.size != (260, 260) else src),
              ("BANKING FRAMES (3X)", bank_panel), ("IFF LIGHT (3X)", iff_panel),
              ("AT A GLANCE (2X) OVER THE RED CANYON", gl_panel), ("KEY FRAMES", kf)]
    return sheet(f"WAVERING STORMHAWK {name}", panels, notes, frames[int(4.5 * FPS)])


def make_mutineers(variants):
    for v in variants:
        for k in (4, 0, 8, 2, 6):
            for b in (-30, -15, 0, 15, 30):
                for s in ("off", "white", "red"):
                    stormhawk(v, k, b, s)
        sc = MutineerScene(v)
        frames = render_frames(sc, MUT_DUR)
        d = DESIGN / "campaign" / "act-5-the-belt" / "concept"
        save_png(mutineer_sheet(v, frames), d / f"wavering-stormhawk-r60-{v}.png")
        save_gif(frames, d / f"wavering-stormhawk-r60-{v}.gif")


# ===================================================================== 4 Vorne's production portraits, the Act 5 speakers

import portraits as pt  # noqa: E402  (tools/art/portraits.py: the production style-B pipeline, unchanged)
import portraits_r03 as r03  # noqa: E402
import portraits_r04 as r04  # noqa: E402

VFACES = {
    "neutral": dict(pt.FACES["neutral"], raise_one=0.0, yaw=0.0),
    "smug": dict(brow_inner=0.004, brow_outer=0.006, brow_in=0.0, brow_r=0.016, lid=-0.018, lower=0.008, eyes=0.75,
                 mouth=None, pitch=-0.13, raise_one=0.095, yaw=0.17),
    "rage": dict(brow_inner=-0.1, brow_outer=0.03, brow_in=0.03, brow_r=0.03, lid=-0.02, lower=0.016, eyes=0.55,
                 mouth="bared", pitch=0.08, raise_one=0.0, yaw=-0.05),
}


def vface(q, skin_k=0.06, jaw=1.0, mouth="neutral", eyes_open=1.0, expression="neutral"):
    """portraits.face (tools/art) with Vorne's two extra expressions: ``smug`` raises one brow (his
    left) and keeps the thin smile, ``rage`` is cold: the brows driven down, the eyes narrowed and
    the teeth bared and clenched (no open mouth: he never shouts until Act 6's end)."""
    SKIN, EYE, IRIS, LIPS, BROW = r03.SKIN, r03.EYE, r03.IRIS, r03.LIPS, r03.BROW
    f = VFACES[expression]
    mouth = f["mouth"] or mouth
    eyes_open *= f["eyes"]
    head = sdf.sd_ellipsoid(q, (0, 0.47, -0.02), (0.29, 0.37, 0.32))
    jawd = sdf.sd_ellipsoid(q, (0, 0.30, 0.03), (0.215 * jaw, 0.21, 0.25))
    parts = [
        (sdf.sd_ellipsoid(q, (-0.15, 0.40, 0.17), (0.085, 0.06, 0.08)), 0.05),
        (sdf.sd_ellipsoid(q, (0.15, 0.40, 0.17), (0.085, 0.06, 0.08)), 0.05),
        (sdf.sd_capsule(q, (0, 0.49, 0.29), (0, 0.375, 0.365), 0.028, 0.042), 0.03),
        (sdf.sd_ellipsoid(q, (0, 0.365, 0.325), (0.058, 0.03, 0.04)), 0.03),
        (sdf.sd_capsule(q, (-0.20, 0.54 + f["brow_inner"] * 0.4, 0.22), (0.20, 0.54 + f["brow_inner"] * 0.4, 0.22),
                        0.035), 0.05),
        (sdf.sd_ellipsoid(q, (0, 0.175, 0.19), (0.07, 0.05, 0.05)), 0.04),
        (sdf.sd_ellipsoid(q, (-0.29, 0.44, -0.03), (0.035, 0.075, 0.055)), 0.02),
        (sdf.sd_ellipsoid(q, (0.29, 0.44, -0.03), (0.035, 0.075, 0.055)), 0.02),
    ]
    skin = sdf.smin(head, jawd, skin_k)
    for d, k in parts:
        skin = sdf.smin(skin, d, k)
    for x in (-0.11, 0.11):
        skin = np.maximum(skin, -sdf.sd_ellipsoid(q, (x, 0.463, 0.31), (0.062, 0.03 * eyes_open + 0.01, 0.05)))
    inside = None
    if mouth == "smile":
        cut = np.minimum(sdf.sd_capsule(q, (-0.07, 0.272, 0.29), (0.0, 0.266, 0.31), 0.007),
                         sdf.sd_capsule(q, (0.0, 0.266, 0.31), (0.075, 0.28 + (0.008 if expression == "smug" else 0), 0.29),
                                        0.007))
        if expression == "smug":
            cut = np.minimum(cut, sdf.sd_capsule(q, (0.04, 0.272, 0.305), (0.085, 0.295, 0.285), 0.008))
    elif mouth == "bared":    # lips drawn back from clenched teeth: a wide, shallow cavity, two rows meeting
        cut = sdf.sd_ellipsoid(q, (0, 0.26, 0.31), (0.1, 0.038, 0.07))
        inside = [(sdf.sd_ellipsoid(q, (0, 0.26, 0.262), (0.09, 0.032, 0.03)), IRIS),
                  (sdf.sd_box(q, (0, 0.272, 0.29), (0.085, 0.011, 0.02), 0.003), EYE),
                  (sdf.sd_box(q, (0, 0.247, 0.29), (0.078, 0.011, 0.02), 0.003), EYE)]
    else:
        cut = sdf.sd_capsule(q, (-0.07, 0.27, 0.29), (0.07, 0.27, 0.29), 0.008)
    lips = sdf.sd_ellipsoid(q, (0, 0.27, 0.28), (0.075, 0.03 + (0.008 if mouth == "bared" else 0), 0.035))
    skin = np.maximum(skin, -cut)
    items = [(skin, SKIN), (np.maximum(lips, -cut) + 0.004, LIPS)] + (inside or [])
    for s in (-1, 1):
        x = s * 0.11
        up = f["raise_one"] if s > 0 else 0.0
        items += [(sdf.sd_sphere(q, (x, 0.462, 0.25), 0.052), EYE),
                  (sdf.sd_sphere(q, (x + 0.004, 0.462, 0.281), 0.029), IRIS),
                  (sdf.sd_capsule(q, (x - 0.05, 0.488 + f["lid"] + up * 0.3, 0.283),
                                  (x + 0.05, 0.49 + f["lid"] + up * 0.3, 0.28), 0.02), SKIN),
                  (sdf.sd_capsule(q, (x - 0.045, 0.438 + f["lower"], 0.286), (x + 0.045, 0.437 + f["lower"], 0.283),
                                  0.012), SKIN)]
        ends = {-1: (x - 0.06, 0.525, 0.275), 1: (x + 0.06, 0.53, 0.268)}
        brow = []
        for side, (bx, by, bz) in ends.items():
            inner = side == -s
            dy = (f["brow_inner"] if inner else f["brow_outer"]) + up * (0.6 if inner else 1.0)
            brow.append((bx - s * f["brow_in"] if inner else bx, by + dy, bz))
        items.append((sdf.sd_capsule(q, brow[0], brow[1], f["brow_r"]), BROW))
    return items, skin


class vorne_expression:
    def __init__(self, name):
        self.name = name

    def __enter__(self):
        self.saved = r03.face, r03.turn
        f = VFACES[self.name]
        turn0 = self.saved[1]
        r03.face = lambda q, **kw: vface(q, expression=self.name, **kw)
        r03.turn = lambda p, yaw, pitch=None, **kw: turn0(p, yaw + (f["yaw"] if pitch is not None else 0.0),
                                                          0.0 if pitch is None else pitch + f["pitch"], **kw)

    def __exit__(self, *a):
        r03.face, r03.turn = self.saved


def vorne_office(ramp):
    """Variant b's background: the Chairman's office on the amber screen: a black wall, the gilded
    Ascendancy crest (a rising sun's rays over a ringed planet) behind his head, two gold pilasters."""
    N = pt.N
    yy, xx = np.mgrid[0:N, 0:N].astype(np.float64)
    bg = np.zeros((N, N, 3)) + ramp[0] * 255 * 0.7
    cx, cy = 40.0, 22.0
    d = np.hypot(xx - cx, yy - cy)
    ang = np.arctan2(yy - cy, xx - cx)
    rays = (np.cos(ang * 12) > 0.55) & (d > 15) & (d < 31) & (yy < cy + 4)
    bg[rays] = ramp[3] * 255 * 0.8
    disc = (d < 13)
    bg[disc] = ramp[4] * 255 * 0.85
    ring = np.abs(np.hypot((xx - cx) / 1.0, (yy - cy) / 0.32) - 18) < 1.2
    bg[ring] = ramp[5] * 255 if len(ramp) > 5 else ramp[4] * 255
    for px in (3, N - 5):
        bg[:, px:px + 2] = ramp[3] * 255 * 0.6
    return bg


def tear(radio, seed, k=1.0):
    """Rage's static: rows torn sideways, a bright noise line, a one-pixel channel split."""
    a = np.array(radio).astype(np.float64)
    rng = np.random.default_rng(seed)
    N = a.shape[0]
    for _ in range(int(4 * k)):
        y0 = int(rng.integers(0, N - 4))
        h = int(rng.integers(1, 4))
        a[y0:y0 + h] = np.roll(a[y0:y0 + h], int(rng.integers(2, 6)) * (1 if rng.random() < 0.5 else -1), axis=1)
    y = int(rng.integers(10, N - 10))
    a[y, rng.random(N) > 0.4, :3] = (255, 230, 170)
    a[..., 0] = np.roll(a[..., 0], 1, axis=1)
    a[..., :3] = np.clip(a[..., :3] + rng.normal(0, 10 * k, a[..., :3].shape), 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def briefing_of(radio):
    big = np.array(sprite.enlarge(radio, 2)).astype(np.float64)
    big[1::2, :, :3] *= 0.74
    return Image.fromarray(np.clip(big, 0, 255).astype(np.uint8), "RGBA")


EXPR = ("neutral", "smug", "rage")


def _vorne_fig(name):
    with vorne_expression(name):
        return pt.figure("vorne", r03.render_bust("vorne"))


def vorne_set(v):
    with ProcessPoolExecutor(3, mp_context=multiprocessing.get_context("fork")) as pool:
        figs = list(pool.map(_vorne_fig, EXPR))
    shared = artkit.quantize_set(figs, r04.COLORS)
    if v == "b":
        pt.ROOMS["vorne"] = vorne_office
    out = {}
    for name, fig in zip(EXPR, shared):
        radio, _ = pt.screen("vorne", fig)
        if name == "rage":
            radio = tear(radio, 7)
        out[name] = (radio, briefing_of(radio))
    pt.ROOMS.pop("vorne", None)
    return out


def portrait_sheet(title, sub, rows, notes, width=1000):
    h = 60 + sum(r[1].height + 34 for r in rows) + 16 * len(notes) + 20
    img = raster.sheet(width, h, title, sub)
    y = 50
    for lab, im in rows:
        raster.draw_text(img, 16, y, lab, raster.LABEL_DIM)
        img.alpha_composite(im, (16, y + 14))
        y += im.height + 34
    for i, n in enumerate(notes):
        raster.draw_text(img, 16, y + i * 16, n, raster.ACCENT if i == 0 else raster.LABEL)
    return img


def make_vorne(variants):
    for v in variants:
        s = vorne_set(v)
        brief = panel_grid([s[n][1] for n in EXPR], 3, labels=[f"BRIEFING 144 - {n.upper()}" for n in EXPR])
        radio = panel_grid([s[n][0] for n in EXPR], 3, labels=[n.upper() for n in EXPR])
        radio2 = panel_grid([sprite.enlarge(s[n][0], 3) for n in EXPR], 3, labels=[f"{n.upper()} 3X" for n in EXPR])
        old = Image.open(DESIGN / "story" / "characters" / "vorne" / "concept" / "portrait-r04-a.png").convert("RGBA")
        old.thumbnail((460, 300))
        name = {"a": "A - THE ENEMY SCREEN", "b": "B - THE CHAIRMAN'S OFFICE"}[v]
        notes = [f"VORNE'S PRODUCTION PORTRAITS {name} (THE CHOSEN R04 STYLE B, TOOLS/ART/PORTRAITS.PY'S PIPELINE)",
                 "AMBER ENEMY CHANNEL OVER HIS OWN COLOURS, ONE 36-COLOUR PALETTE OVER THE THREE EXPRESSIONS.",
                 "NEUTRAL: THE FAINT SMILE. SMUG: HEAD BACK, ONE BROW UP, THE SMILE CROOKED.",
                 "RAGE (L35'S CORE PHASE): COLD - BROWS DOWN, EYES NARROWED, TEETH BARED AND CLENCHED; THE SCREEN TEARS.",
                 "  NO OPEN MOUTH: HE NEVER SHOUTS UNTIL ACT 6'S END.",
                 ("A: THE PLAIN AMBER SCREEN WITH ITS DOT GRID, AS THE CAST'S." if v == "a" else
                  "B: HIS OFFICE - THE GILDED CREST (SUN RAYS OVER A RINGED PLANET) BEHIND HIS HEAD, GOLD PILASTERS.")]
        img = portrait_sheet(f"VORNE PORTRAITS {name}", SUB,
                             [("BRIEFING 144X144 (1X)", brief), ("RADIO 72X72 (1X)", radio), ("RADIO (3X)", radio2),
                              ("FOR COMPARISON: THE CHOSEN ROUND-04 CONCEPT", old)], notes, width=760)
        save_png(img, DESIGN / "story" / "characters" / "vorne" / "concept" / f"vorne-portraits-r60-{v}.png")


SKIN, EYE, IRIS, LIPS, HAIR, CLOTH, ACCENT, METAL, GLOW, EXTRA, BROW = range(11)


def _headset(q, side=-1):
    band = np.maximum(r03.sd_torus_z(q, (0, 0.46, -0.03), 0.33, 0.02), -q[:, 1] + 0.46)
    cup = sdf.sd_cylinder_x(q, (side * 0.33, 0.43, -0.02), 0.075, 0.03)
    boom = sdf.sd_capsule(q, (side * 0.34, 0.39, 0.03), (side * 0.12, 0.28, 0.27), 0.012)
    mic = sdf.sd_sphere(q, (side * 0.11, 0.28, 0.275), 0.022)
    led = sdf.sd_sphere(q, (side * 0.35, 0.41, 0.05), 0.013)
    return [(band, METAL), (cup, METAL), (boom, METAL), (mic, METAL), (led, GLOW)]


def ceres_controller():
    """Ceres Hub Control, the loyalist dock controller (L29, L35): a lean man in his forties, dark brown
    skin, the head shaved close, a short greying beard; the hub's pale grey dock coverall with a high
    collar and Ceres's ochre patch, a headset with a boom mic."""
    mats = [
        sdf.Material((0.42, 0.28, 0.2), shininess=18, spec=0.25),
        sdf.Material((0.86, 0.84, 0.8), shininess=60, spec=0.5),
        sdf.Material((0.1, 0.07, 0.05), shininess=90, spec=0.9),
        sdf.Material((0.36, 0.22, 0.18), shininess=24, spec=0.25),
        sdf.Material((0.2, 0.18, 0.17), shininess=6, spec=0.05, pattern=r03.noise_pattern(300, 0.5, 3)),
        sdf.Material((0.48, 0.5, 0.53), shininess=14, spec=0.2, pattern=r03.noise_pattern(70, 0.08, 5)),
        sdf.Material((0.85, 0.55, 0.18), shininess=18, spec=0.25),
        sdf.Material((0.3, 0.31, 0.33), metal=0.6, shininess=70, spec=0.7),
        sdf.Material((0.1, 0.1, 0.1), emission=(0.3, 1.0, 0.8)),
        sdf.Material((0.4, 0.4, 0.42), shininess=8, spec=0.08),
        sdf.Material((0.14, 0.12, 0.11), shininess=8, spec=0.08),
    ]

    def scene(p):
        q = r03.turn(p, -0.18, 0.02)
        items, skin = r03.face(q, jaw=1.0, mouth="neutral")
        scalp = sdf.sd_ellipsoid(q, (0, 0.5, -0.04), (0.3, 0.37, 0.33))
        scalp = np.maximum(scalp, -(q[:, 1] - 0.6 + 0.4 * np.maximum(q[:, 2], 0)))
        beard = sdf.sd_ellipsoid(q, (0, 0.22, 0.08), (0.2, 0.11, 0.22))
        beard = np.maximum(beard, q[:, 1] - 0.29)
        beard = np.maximum(beard, -sdf.sd_ellipsoid(q, (0, 0.27, 0.3), (0.08, 0.03, 0.06)))
        pb = r03.turn(p, -0.06)
        neck, torso = r03.body(pb, neck_r=0.12, shoulders=0.8)
        collar = sdf.sd_cylinder_y(pb, (0, -0.06, -0.04), 0.22, 0.09)
        collar = np.maximum(collar, -sdf.sd_cylinder_y(pb, (0, -0.06, -0.04), 0.15, 0.3))
        patch = sdf.sd_cylinder_z(pb, (0.42, -0.36, 0.24), 0.07, 0.02)
        stripe = sdf.sd_box(pb, (0, -0.62, 0.22), (0.7, 0.025, 0.04), 0.01)
        return sdf.union(*items, (scalp, HAIR), (beard, HAIR), (sdf.smin(neck, skin, 0.05), SKIN), (torso, CLOTH),
                         (collar, CLOTH), (patch, ACCENT), (stripe, ACCENT), *_headset(q, 1))
    return scene, mats


def bellwether_captain():
    """The Bellwether's captain (L31): a weathered belt miner in her fifties, grey hair cropped short and
    pushed back, crow's feet, a pair of work goggles pushed up on the forehead; a scuffed hazard-yellow
    work jacket over a dark undershirt, a headset."""
    mats = [
        sdf.Material((0.76, 0.58, 0.48), shininess=12, spec=0.15),
        sdf.Material((0.86, 0.84, 0.8), shininess=60, spec=0.5),
        sdf.Material((0.2, 0.26, 0.2), shininess=90, spec=0.9),
        sdf.Material((0.56, 0.36, 0.32), shininess=24, spec=0.25),
        sdf.Material((0.5, 0.48, 0.46), shininess=10, spec=0.1, pattern=r03.noise_pattern(220, 0.3, 4)),
        sdf.Material((0.88, 0.66, 0.12), shininess=14, spec=0.2, pattern=r03.noise_pattern(60, 0.22, 5)),
        sdf.Material((0.16, 0.16, 0.18), shininess=10, spec=0.1),
        sdf.Material((0.28, 0.29, 0.3), metal=0.6, shininess=70, spec=0.7),
        sdf.Material((0.1, 0.1, 0.1), emission=(0.3, 1.0, 0.8)),
        sdf.Material((0.35, 0.6, 0.62), metal=0.8, shininess=120, spec=1.1),
        sdf.Material((0.5, 0.48, 0.46), shininess=8, spec=0.08),
    ]

    def scene(p):
        q = r03.turn(p, 0.22, 0.03)
        items, skin = r03.face(q, jaw=0.95, mouth="frown")
        hair = sdf.sd_ellipsoid(q, (0, 0.53, -0.07), (0.31, 0.36, 0.33))
        hair = np.maximum(hair, -(q[:, 1] - 0.6 + 0.3 * np.maximum(q[:, 2] - 0.05, 0)))
        band = np.maximum(r03.sd_torus_z(q, (0, 0.62, 0.0), 0.3, 0.025), np.abs(q[:, 1] - 0.62) - 0.03)
        lenses = [sdf.sd_cylinder_z(q, (s * 0.1, 0.64, 0.29), 0.055, 0.03) for s in (-1, 1)]
        pb = r03.turn(p, 0.08)
        neck, torso = r03.body(pb, neck_r=0.12, shoulders=0.86)
        under = sdf.sd_cylinder_y(pb, (0, -0.05, -0.02), 0.16, 0.08)
        lapel = [sdf.sd_box(rotate_zp(pb, s * 0.5), (s * 0.18, -0.25, 0.22), (0.07, 0.2, 0.03), 0.02) for s in (-1, 1)]
        refl = sdf.sd_box(pb, (0, -0.55, 0.24), (0.75, 0.03, 0.03), 0.01)
        return sdf.union(*items, (hair, HAIR), (band, ACCENT), (np.minimum(*lenses), EXTRA),
                         (sdf.smin(neck, skin, 0.05), SKIN), (torso, CLOTH), (under, ACCENT),
                         (np.minimum(*lapel), CLOTH), (refl, METAL), *_headset(q, -1))
    return scene, mats


def rotate_zp(p, a):
    return sdf.rotate_z(p, a)


def harker():
    """Harker, the mutineers' flight lead (L29): a Ceres garrison pilot in his thirties, dark hair cut
    short, stubble, a hard set mouth; a CDF flight suit with its padded collar ring and, over the CDF
    patch on his chest, the Ascendancy crest freshly stitched (a gold sun and ring on black)."""
    mats = [
        sdf.Material((0.8, 0.64, 0.54), shininess=16, spec=0.2),
        sdf.Material((0.86, 0.84, 0.8), shininess=60, spec=0.5),
        sdf.Material((0.14, 0.18, 0.24), shininess=90, spec=0.9),
        sdf.Material((0.58, 0.38, 0.34), shininess=24, spec=0.25),
        sdf.Material((0.12, 0.1, 0.09), shininess=8, spec=0.06, pattern=r03.noise_pattern(260, 0.3, 4)),
        sdf.Material((0.36, 0.4, 0.34), shininess=16, spec=0.2, pattern=r03.noise_pattern(60, 0.1, 5)),
        sdf.Material((1.0, 0.74, 0.18), metal=0.9, shininess=90, spec=1.0),
        sdf.Material((0.25, 0.26, 0.28), metal=0.6, shininess=70, spec=0.7),
        sdf.Material((0.1, 0.1, 0.1), emission=(1.0, 0.2, 0.1)),
        sdf.Material((0.04, 0.04, 0.05), shininess=40, spec=0.4),
        sdf.Material((0.1, 0.09, 0.08), shininess=8, spec=0.08),
    ]

    def scene(p):
        q = r03.turn(p, -0.24, 0.0)
        items, skin = r03.face(q, jaw=1.08, mouth="frown")
        hair = sdf.sd_ellipsoid(q, (0, 0.54, -0.05), (0.305, 0.35, 0.33))
        hair = np.maximum(hair, -(q[:, 1] - 0.63 + 0.3 * np.maximum(q[:, 2], 0)))
        stub = sdf.sd_ellipsoid(q, (0, 0.25, 0.07), (0.225, 0.15, 0.25))
        stub = np.maximum(stub, q[:, 1] - 0.33)
        stub = np.maximum(stub, -sdf.sd_ellipsoid(q, (0, 0.27, 0.3), (0.08, 0.03, 0.06)))
        pb = r03.turn(p, -0.08)
        neck, torso = r03.body(pb, neck_r=0.13, shoulders=0.86)
        ring_p = np.stack([pb[:, 0], pb[:, 2] + 0.03, pb[:, 1] + 0.05], axis=-1)
        ring = r03.sd_torus_z(ring_p, (0, 0, 0), 0.2, 0.05)
        badge = sdf.sd_cylinder_z(pb, (-0.38, -0.36, 0.24), 0.09, 0.02)
        sun = sdf.sd_cylinder_z(pb, (-0.38, -0.36, 0.26), 0.04, 0.02)
        rim = np.maximum(r03.sd_torus_z(pb, (-0.38, -0.36, 0.265), 0.065, 0.01), -1.0)
        return sdf.union(*items, (hair, HAIR), (stub - 0.003, HAIR), (sdf.smin(neck, skin, 0.05), SKIN),
                         (torso, CLOTH), (ring, METAL), (badge, EXTRA), (sun, ACCENT), (rim, ACCENT), *_headset(q, 1))
    return scene, mats


SPEAKERS = {
    "ceres-control": dict(build=ceres_controller, name="CERES HUB CONTROL", role="CERES HUB DOCKS",
                          key=(-1.5, 2.1, 3.1), rim=(2.4, 1.0, -1.2), rim_col=(0.9, 0.85, 0.75),
                          bg=((4, 12, 16), (30, 50, 60)), fill_col=(0.8, 0.9, 1.0), channel="cdf", exposure=1.0),
    "bellwether": dict(build=bellwether_captain, name="BELLWETHER", role="MINING TUG BELLWETHER",
                       key=(-1.4, 2.2, 3.2), rim=(2.6, 0.9, -1.0), rim_col=(0.6, 0.85, 1.0),
                       bg=((6, 10, 14), (40, 50, 60)), fill_col=(1.0, 0.85, 0.6), channel="cdf", exposure=0.98),
    "harker": dict(build=harker, name="HARKER", role="CERES WING (MUTINEER)",
                   key=(-1.6, 2.0, 3.0), rim=(2.4, 1.0, -1.2), rim_col=(1.0, 0.3, 0.2),
                   bg=((14, 6, 2), (60, 30, 10)), fill_col=(1.0, 0.7, 0.4), channel="enemy", exposure=1.15),
}


def _speaker_fig(slug):
    r03.CAST[slug] = SPEAKERS[slug]
    return pt.figure(slug, r03.render_bust(slug))


def make_speakers(variants):
    for slug, c in SPEAKERS.items():
        r03.CAST[slug] = c
    with ProcessPoolExecutor(3, mp_context=multiprocessing.get_context("fork")) as pool:
        figs = dict(zip(SPEAKERS, pool.map(_speaker_fig, list(SPEAKERS))))
    radios = []
    for slug in SPEAKERS:
        fig = artkit.quantize_set([figs[slug]], r04.COLORS)[0]
        radios.append(pt.screen(slug, fig)[0])
    prod = [Image.open(ROOT / "assets" / "sprites" / "portraits" / f"radio-{s}-neutral.png").convert("RGBA")
            for s in ("generic-colonial", "thera-deep", "okafor")]
    names = ["CERES CTRL", "BELLWETHER", "HARKER"]
    rows = [("RADIO 72X72 (1X)", panel_grid(radios, 3, pad=22, labels=names)),
            ("RADIO (3X)", panel_grid([sprite.enlarge(r, 3) for r in radios], 3, labels=names)),
            ("LINE-UP AT 1X WITH THE PRODUCTION SPEAKERS (ARES CONTROL, THERA DEEP CONTROL, OKAFOR)",
             panel_grid(radios + prod, 6, pad=22, labels=names + ["ARES CTRL", "THERA DEEP", "OKAFOR"]))]
    notes = ["THE ACT 5 RADIO SPEAKERS (STYLE B, TOOLS/ART/PORTRAITS.PY'S PIPELINE, NEUTRAL ONLY, AS THE MINOR SPEAKERS)",
             "CERES HUB CONTROL (TEAL): A LOYALIST DOCK CONTROLLER, SHAVED HEAD, GREYING BEARD, THE HUB'S GREY COVERALL, "
             "OCHRE PATCH.",
             "BELLWETHER (TEAL): THE TUG'S CAPTAIN, A WEATHERED MINER IN HER FIFTIES, GOGGLES UP, HAZARD-YELLOW JACKET.",
             "HARKER (AMBER, ENEMY CHANNEL): A CERES WING PILOT, A CDF FLIGHT SUIT, THE ASCENDANCY CREST STITCHED OVER "
             "THE PATCH."]
    img = portrait_sheet("ACT 5 RADIO SPEAKERS - A", SUB, rows, notes, width=900)
    save_png(img, DESIGN / "story" / "characters" / "concept" / "act5-speakers-r60-a.png")


# ===================================================================== 5 The belt: mood ladder and the Q11 frame

def grade_b(img):
    """The harsh grade (b): deep shadows and a dimmer dust band, as Act 4's darkness."""
    a = np.array(img.convert("RGB")).astype(np.float64) / 255.0
    a = (a ** 1.55) * 0.85 * np.array((0.94, 0.97, 1.06))
    return Image.fromarray(np.clip(a * 255, 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def _buoys(img, t=0.0):
    d = ImageDraw.Draw(img)
    for x in (54, 426):
        for y in range(30, FH, 130):
            d.rectangle([x - 3, y - 8, x + 3, y + 8], fill=(150, 150, 140, 255), outline=(40, 40, 40, 255))
            d.rectangle([x - 6, y - 2, x + 6, y + 2], fill=(90, 92, 96, 255))
            img = light(img, x, y - 9, 6, (255, 190, 80), 0.9)
    return img


def _furnace(img):
    a = np.array(img).astype(np.float64)
    g = np.clip((YY - 330) / 210.0, 0, 1)[..., None]
    a[..., :3] = a[..., :3] * (1 - 0.3 * g) + np.array((255, 120, 30)) * 0.42 * g
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    for x in (150, 240, 330):
        d.rectangle([x - 16, 470, x + 16, 500], fill=(40, 20, 12, 255), outline=(255, 150, 60, 255))
        img = light(img, x, 485, 18, (255, 140, 40), 1.0)
    return img


def mood_stills():
    """(level, title, backdrop, draw(img)) for L29-L35: the backdrop is graded, the draw adds units
    and props (ungraded, as the game draws them over the backdrop)."""
    talon = u60.talons[("a", True)].get(DOWN)
    talon2 = u60.talons[("a", False)].get(DOWN)
    out = []

    def l29(img):
        a = np.array(img).astype(np.float64)
        blk = (XX > 300)[..., None] & (YY < 260)[..., None]
        a[..., :3] = np.where(blk, a[..., :3] * np.array((0.8, 0.45, 0.45)), a[..., :3])
        img = Image.fromarray(a.astype(np.uint8), "RGBA")
        sprite.paste_center(img, u60.sam_bases[("a", "red")].get(UP), 390, 140)
        sprite.paste_center(img, u60.sam_racks["a"].get(DOWN), 390, 140)
        for dx, dy, sp in ((0, 0, talon), (-30, -28, talon2), (30, -28, talon2)):
            sprite.paste_center(img, sp, 200 + dx, 150 + dy)
        return img
    out.append(("29", "CERES HUB: LIT STATION HULL, A BLOCK TURNED RED", u60.hull_bg(291), l29))

    def l30(img):
        img = _buoys(img)
        for k in range(4):
            sprite.paste_center(img, u60.mines["a"].get(UP, lamp=0.6), 150 + 32 * k, 250 + 16 * k)
        sprite.paste_center(img, u60.gunships["a"].get(DOWN), 300, 120)
        return img
    out.append(("30", "SHIPPING LANE: OPEN SPACE, BEACON BUOYS, THE DUST BAND", u60.space_bg(301), l30))

    def l31(img):
        for rx, ry, k in ((80, 120, 0), (400, 300, 1), (130, 470, 2)):
            r = u60.ROCK_L[k]
            sprite.paste_center(img, r.resize((r.width * 3 // 2, r.height * 3 // 2), Image.LANCZOS), rx, ry)
        if ("a", "cargo", "intact", 1.0) in TUG:
            sprite.paste_center(img, tug_sprite("a", {s: "intact" for s in SECTIONS}), 240, 360)
        sprite.paste_center(img, u60.hornets["a"].get(DOWN), 330, 90)
        return img
    out.append(("31", "ROCK FIELD: DRIFTING ROCKS, THE TUG", u60.space_bg(311), l31))

    def l32(img):
        img = _furnace(img)
        sprite.paste_center(img, u60.sen_body["a"].get(UP), 70, 210)
        sprite.paste_center(img, u60.sen_head["a"].get(DOWN), 70, 210)
        return img
    out.append(("32", "DEEP CORE: DARK TUNNELS, THE REFINERY'S GLOW", u60.tunnel_bg(321, galleries=[(210, -1, 70)]), l32))

    def l33(img):
        sprite.paste_center(img, u60.bunker(DOWN), 340, 180)
        for x, y in ((120, 300), (200, 120)):
            sprite.paste_center(img, u60.leeches["a"].get(DOWN), x, y)
        return img
    out.append(("33", "LEECH FIELD: SUNLIT ROCK UNDER VRELL GROWTH", u60.rock_bg(331, growth=True), l33))

    def l34(img):
        sprite.paste_center(img, u60.wt_hull["a"].get(DOWN), 250, 200)
        sprite.paste_center(img, u60.wt_tur["a"].get(DOWN), 250, 200)
        d = ImageDraw.Draw(img)
        d.rectangle([160, 330, 172, 342], fill=(200, 170, 40, 255), outline=(40, 30, 10, 255))
        img = light(img, 166, 330, 4, (255, 60, 40), 0.9)
        return img
    out.append(("34", "SHIP GRAVEYARD: DERELICT HULLS, SALVAGE", u60.derelict_bg(341), l34))

    def l35(img):
        if "a" in BASE:
            lay = station_layer("a", {"outer": 4.0, "middle": 9.0, "inner": 0.0}, {}, centre=(240, -150))
            img.alpha_composite(lay)
        for x in (150, 330):
            sprite.paste_center(img, talon2, x, 200)
        d = ImageDraw.Draw(img)
        d.line([0, 330, FW, 300], fill=(255, 48, 56, 200), width=1)
        return img
    out.append(("35", "CERES ORBIT: THE STATION'S RIM, CERES FAR BELOW", ceres_bg(), l35))
    return out


@lru_cache(maxsize=None)
def stills(grade):
    res = []
    for lv, title, bg, draw in mood_stills():
        g = grade_b(bg) if grade == "b" else bg.convert("RGBA")
        res.append((lv, title, g, draw(g.copy())))
    return res


def darkest_rect(bg, w=200, h=96):
    g = lum(bg)
    ii = np.pad(g, ((1, 0), (1, 0))).cumsum(0).cumsum(1)
    s = ii[h:, w:] - ii[:-h, w:] - ii[h:, :-w] + ii[:-h, :-w]
    y, x = np.unravel_index(np.argmin(s), s.shape)
    return bg.crop((x, y, x + w, y + h)), float(s[y, x] / (w * h))


def make_moods(variants):
    for v in variants:
        sts = stills(v)
        cells, labs = [], []
        for lv, title, bg, img in sts:
            L = lum(bg)
            _, dk = darkest_rect(bg)
            cells.append(img.resize((240, 270), Image.LANCZOS))
            labs.append(f"L{lv}  MEAN {L.mean():.0f}  DARKEST {dk:.0f}")
        ladder = panel_grid(cells, 3, labels=labs)
        big = sts[0][3]
        name = {"a": "A - A STEP LIGHTER (Q11 A)", "b": "B - HARSH (AS ACT 4)"}[v]
        notes = [f"THE BELT, MOOD LADDER L29-L35 {name}",
                 "LIT CERES HUB -> THE LANE -> THE ROCK FIELD -> DARK TUNNELS, THE REFINERY -> THE LEECH FIELD",
                 "  -> THE GRAVEYARD -> CERES ORBIT. TOP-LEFT SUN EVERYWHERE; ORANGE REFINERY LIGHT; COOL WHITE STATION LAMPS.",
                 "MEAN = THE BACKDROP'S MEAN LUMINANCE (0-255); DARKEST = ITS DARKEST 200X96 WINDOW (THE READABILITY GROUND)."]
        notes += [f"  L{lv}: {title}" for lv, title, _, _ in sts]
        if v == "a":
            notes.append("A: THE SUNLIT DUST BAND, WARM SUNLIT ROCK, HULLS A STEP LIGHTER THAN ACT 4 (Q11 A'S HALF).")
        else:
            notes.append("B: THE SAME SCENES UNDER A HARSH GRADE (DEEP SHADOW, A DIMMER BAND): WHAT Q11 B / C MAY KEEP.")
        img = sheet(f"THE BELT - MOOD LADDER {name}", [("L29-L35 (0.5X)", ladder)], notes, big)
        save_png(img, DESIGN / "world" / "belt" / "concept" / f"belt-moods-r60-{v}.png")


def look_make(look, make):
    """A round-60 unit model in one of Q11's looks: a as enemies_r60's (gloss black, gold trim, strong
    rim), b gunmetal hulls with the same gold, c black with the gold dimmed to bronze accents."""
    def m2(**kw):
        scene, mats = make(**kw)
        mats = list(mats)
        if look == "b":
            mats[A_HULL] = replace(mats[A_HULL], albedo=hx("5a606c"), shininess=90)
            mats[A_FACET] = replace(mats[A_FACET], albedo=hx("464b55"))
        elif look == "c":
            mats[A_GOLD] = replace(mats[A_GOLD], albedo=em.GOLD[1], spec=0.6)
            mats[A_RED] = Material((0.05, 0.02, 0.02), emission=tuple(em.RED * 2.6))
        return scene, mats
    return m2


@lru_cache(maxsize=None)
def look_units(look):
    post = {"a": u60.belt_rim(True), "b": u60.belt_rim(False), "c": lambda sp: sprite.quantize(sp, 32)}[look]
    tal = u60.Turning(look_make(look, lambda: u60.talon_model("a", True)), u60.TAL_N, 16, factor=5, colors=32, post=post)
    hor = u60.Turning(look_make(look, lambda **kw: u60.hornet_model("a", **kw)), u60.HOR_N, 4, factor=4, colors=40,
                      post=post)
    base = rig.AngleSprites(look_make(look, lambda: u60.sam_base("a", "red")), 48, 4, factor=5, colors=28, post=post)
    rack = u60.Turning(look_make(look, lambda **kw: u60.sam_rack("a", **kw)), 48, 16, factor=5, colors=28, post=post)
    sam = base.get(UP).copy()
    sprite.paste_center(sam, rack.get(DOWN), 24, 24)
    return [(tal.get(DOWN), "TALON LEAD"), (hor.get(DOWN), "HORNET"), (sam, "SAM NEST")]


def make_frame(variants):
    for look in variants:
        units = look_units(look)
        rows = []
        for grade in ("a", "b"):
            for lv, title, bg, img in stills(grade):
                cell, dk = darkest_rect(bg)
                cell = cell.copy()
                x = 34
                for sp, _ in units:
                    sprite.paste_center(cell, sp, x, 48)
                    x += 66
                rows.append((grade, lv, cell, dk))
        cells = []
        labs = []
        for lv in ("29", "30", "31", "32", "33", "34", "35"):
            for grade in ("a", "b"):
                cell, dk = [(c, d) for g, l, c, d in rows if g == grade and l == lv][0]
                cells.append(cell)
                labs.append(f"L{lv} GRADE {grade.upper()} (DARKEST {dk:.0f})")
        grid = panel_grid(cells, 2, labels=labs)
        zoom = sprite.enlarge(cells[4], 2)
        golds = "  ".join(f"{lab} GOLD {100 * u60.gold_share(sp):.0f} %" for sp, lab in units)
        field = stills("a")[2][2].copy()
        for (sp, _), (x, y) in zip(units, ((150, 160), (330, 230), (240, 380))):
            sprite.paste_center(field, sp, x, y)
        for dx in (-30, 30):
            sprite.paste_center(field, units[0][0], 150 + dx, 132)
        sprite.paste_center(field, e3.player_sprite(), 240, 480)
        name = {"a": "A - GLOSS BLACK, GOLD TRIM, STRONG RIM", "b": "B - GUNMETAL HULLS WITH GOLD",
                "c": "C - BLACK SILHOUETTES, GOLD ACCENTS"}[look]
        desc = {"a": "THE RECOMMENDED LOOK (ENEMIES_R60'S UNITS): GLOSS BLACK, GOLD ON >= 20 %, A 2 PX GOLD TOP-LEFT RIM, "
                     "RED FAR RIM AND LIGHTS.",
                "b": "GUNMETAL / CHARCOAL HULLS WITH THE SAME GOLD: READS BEST, BUT DIVERGES FROM THE FINAL L28 CRUISERS.",
                "c": "BLACK HULLS, THE GOLD DIMMED TO BRONZE ACCENTS, NO RIM; READ BY THE RED LIGHTS: CHEAPEST, POOR AT "
                     "SPEED."}[look]
        notes = [f"Q11 READABILITY FRAME {name}", desc, golds,
                 "EACH CELL: THE DARKEST 200X96 WINDOW OF THE LEVEL'S MOOD STILL IN GRADE A (A STEP LIGHTER) AND B (HARSH),",
                 "  THE THREE UNITS AT PLAY SIZE ON IT. Q11 A PAIRS THIS LOOK WITH GRADE A; B AND C KEEP EITHER GRADE."]
        img = sheet(f"Q11 READABILITY {name}", [("THE DARKEST GROUND PER LEVEL (1X)", grid),
                                                ("L31 GRADE A, 2X", zoom)], notes, field)
        save_png(img, DESIGN / "world" / "belt" / "concept" / f"belt-readability-r60-{look}.png")


# ===================================================================== main

SUBJECTS = {"sovereign": make_sovereign, "tug": make_tug, "mutineers": make_mutineers, "vorne": make_vorne,
            "speakers": make_speakers, "moods": make_moods, "frame": make_frame}


def main(args):
    names = [a for a in args if a in SUBJECTS] or list(SUBJECTS)
    bad = [a for a in args if a not in SUBJECTS and a not in ("a", "b", "c", "--no-gif")]
    if bad:
        sys.exit(f"unknown: {bad}; subjects: {', '.join(SUBJECTS)}")
    picked = [a for a in args if a in ("a", "b", "c")]
    for n in names:
        if n in ("moods", "frame", "sovereign") and "a" not in BASE and n != "sovereign":
            prepare("a")                       # the L35 still draws the station's rim
        if n == "moods":
            load_tug("a")
        vs = picked or (["a", "b", "c"] if n == "frame" else ["a", "b"])
        SUBJECTS[n]([v for v in vs if n == "frame" or v != "c"])


if __name__ == "__main__":
    main([a for a in sys.argv[1:]])
