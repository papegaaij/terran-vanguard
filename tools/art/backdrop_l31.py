#!/usr/bin/env python3
"""Production art: the Level 31 backdrop, the open belt from Bright Seam to the Vesta relay
(design/campaign/act-5-the-belt/level-31-mining-rig-escort; M9 part D batch, concept round 64, step ART-D).

Straight to production from round 60's belt look (tools/concept/enemies_r60.py ``space_bg``, the L31 mood still of
belt_r60.py: the rock field with the tug) in **A.18 a**, the lighter belt grade the user picked: harsh sun from the
top left, but no near-black screen, so the gunmetal units and the homing diamonds always have a lighter ground
somewhere near. Built on tools/art/backdrop_l30.py's space kit (its dust-band space, grit, far rocks, hazing and
strobes, imported unchanged) and backdrop_l29.py's loyalist ore hauler. Posterized per image (median cut), wide
gradients ordered-dithered; nothing lit is mirrored.

Level 31 keeps a **ground layer** (not a space-only level): the escorted tug lies on it (the game draws it in the
ground pass, it is not part of the backdrop), and so do the Bright Seam dock it leaves and the two rock outcrops
with their SAM Nests (the Bomb Rack works over them). The ground has **no tile set**: open space is the deep layer
(the base) and the ground pieces lie on it. The nests sit on the outcrops' pads at **x <= 100 or >= 380** only, never
under the tug's swayed footprint (x 130-350, y 227-533: m9d-gaps.md §6).

The layers (``scroll_factors``): ``deep`` 0.12 (space: the sunlit dust band, stars; Ceres a fading crescent behind,
far rocks, the ambushers' carrier lights in the dust band, the Vesta relay's beacon tower and a CDF frigate at the
end), ``ground`` 1 (the Bright Seam dock, the two outcrops), ``low-air`` 1.35 (two loyalist ore haulers with refugees
falling in behind the tug at the start, on paths; slow dimmed rocks below the play plane), ``high-air`` 2.2 (sparse
close grit).

Tile sets (480 wide, wrapping):
  open-space   deep 480x1024: space a step lighter than Act 4, the sunlit warm dust band winding down the screen,
               stars, far rock specks (all sections)
  open-dust    high-air 480x960, transparent: sparse close grit and pale dust motes
Pieces (the layer named; ``frames`` loop at their fps; ``headings`` pick a frame by the path's motion):
  ceres-crescent    deep 360x360: Ceres behind, a thin sunlit crescent on its top-left limb, fading (section 2)
  far-rock-a, -b, -c  deep 120x100 / 84x72 / 56x48: distant lit rocks, hazed
  carrier-lights    deep 140x72: the ambushers' carrier in the dust band (section 5): a long dark hull sliver with
                    its gold and red running lights (lights, not a craft)
  vesta-relay       deep 140x300, 8 frames at 4 fps: the Vesta relay's beacon tower (a lattice mast, a dish, solar
                    vanes) with its green strobe: one flash in 2 s (section 6)
  cdf-frigate       deep 64x112: a CDF frigate waiting at the relay, grey and blue, its white running lights
  bright-seam-dock  ground 480x560: the mining rock Bright Seam's dock: its sunlit rock face cut straight at the
                    quarry edge, two gantries reaching over the berth, the tug's empty cradle, work lamps
                    (section 1; it scrolls away as the tug rises into its station)
  outcrop-1         ground 480x440: a large asteroid's sunlit face passing under the tug (section 3), three nest
                    pads on its edges (x 56, 96 left; 424 right)
  outcrop-2         ground 480x400: the second face (section 4), pads x 44 left; 404, 440 right
  ore-hauler        low-air 64x64, 16 headings: a loyalist ore hauler (backdrop_l29.py's), refugees aboard
  tumbling-rock     low-air 104x104, 16 frames at 2 fps: a big rock tumbling slowly past, dimmed
  drift-rock-a, -b  low-air 88x80 / 64x56: rocks drifting past below the play plane, dimmed

Review: design/campaign/act-5-the-belt/level-31-mining-rig-escort/concept/backdrop-final-r64-a.png (the tile sets,
the pieces, eight screens with stand-ins: the tug on station, the nests on their pads, the rocks, the ship) and .gif
(t 0-8 at half size: the tug rising out of the dock into its station). ``--proposal`` writes the ``backdrop:``
block, the sections, the ground targets' places on the pads, the tug's station band and the debris block (the
belt's rocks, Dacosta's claim) to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections. Re-run after
the data moves a section.
Run: python3 tools/art/backdrop_l31.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU

import backdrop_l29 as l29  # noqa: E402  (tools/art: the loyalist ore hauler)
import backdrop_l30 as l30  # noqa: E402  (tools/art: the belt's space kit)
import belt_units as bu  # noqa: E402  (tools/art: the belt's rock model, paste)
import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged)
from render import raster  # noqa: E402
from render.sdf import Material, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union  # noqa: E402

SCRIPT = "backdrop_l31.py"
BATCH = "M9 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r64"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-31-mining-rig-escort"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-31"
W, SCREEN = 480, 540
SPACE_H, DUST_H = 1024, 960
FACTORS = {"deep": 0.12, "ground": 1.0, "low-air": 1.35, "high-air": 2.2}
WARM_ROCK = [(k, raster.hexrgb(c)) for k, c in e60.WARM_ROCK]
SUN = np.array((-0.55, -0.6, 0.75)) / np.linalg.norm((-0.55, -0.6, 0.75))   # screen x right, y down, z up

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
TILES = ["open-space", "open-dust"]
PROPOSED_SECTIONS = [
    {"name": "Bright Seam Dock", "end": 24, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Open Belt", "end": 58, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Linked Line", "end": 92, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Drift", "end": 128, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Ambush", "end": 164, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Vesta Relay", "end": 184, "atmosphere": "clear", "tiles": TILES},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speed(i):
    s = SECTIONS[i]
    return float(s["speed"]) if s.get("speed") is not None else BASE_SPEED


def _travelled(i, a, b):
    speed, start, end = _speed(i), STARTS[i], float(SECTIONS[i]["end"])
    px = (b - a) * speed
    ramp = float(SECTIONS[i].get("ramp", 0) or 0)
    if i > 0 and ramp > 0:
        ramp = min(ramp, end - start)
        px -= (speed - _speed(i - 1)) * ramp * (l30._ease_out(l30._u(b, start, ramp)) -
                                                l30._ease_out(l30._u(a, start, ramp)))
    return px


def scroll(t):
    """LevelData.scrollAt: the ground layer's scroll at level time t, px (before the start at the first speed)."""
    s = 0.0
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]) or i == len(SECTIONS) - 1:
            return s + _travelled(i, STARTS[i], t)
        s += _travelled(i, STARTS[i], float(sec["end"]))
    raise AssertionError


def time_at(s):
    lo, hi = -120.0, END + 60
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll(mid) < s:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def t_for_layer_row(row, factor):
    """The placement time putting a piece's centre on layer row ``row`` (centre = scroll(t) x factor + 270)."""
    return round(time_at((row - SCREEN / 2) / factor), 3)


def t_centre_at(t, y_down, factor):
    """The placement time of a piece whose centre is at screen y (down) at level time t."""
    return t_for_layer_row(scroll(t) * factor + (SCREEN - y_down), factor)


# =========================================================================== rock faces (ground)

def face_field(w, h, mask, seed, relief=34.0):
    """A sunlit rock face: a heightfield (px) of broad lumps, ridges and craters inside ``mask`` (falling away at
    its edge), hill-shaded under the top-left sun with cast shadows, coloured along the warm rock ramp."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    n1 = raster.fbm(w, h, 96, seed, octaves=4, period=False)
    n2 = raster.fbm(w, h, 24, seed + 3, octaves=3, period=False)
    n3 = raster.fbm(w, h, 6, seed + 5, octaves=2, period=False)
    inside = np.array(Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(14))) / 255.0
    hgt = relief * (0.7 * n1 + 0.25 * n2 + 0.05 * n3) + 46 * inside
    rng = np.random.default_rng(seed)
    for _ in range(int(w * h / 9000)):                       # craters: a bowl and a raised lip
        cx, cy, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(6, 26)
        d = np.hypot(xx - cx, yy - cy) / r
        hgt += np.where(d < 1, -0.5 * r * (1 - d ** 2), 0) + 0.18 * r * np.exp(-((d - 1) / 0.18) ** 2)
    return hgt


def shade(hgt, k=1.0, ambient=0.3, shadow_len=60):
    gy, gx = np.gradient(hgt)
    nrm = np.dstack([-gx * k, -gy * k, np.ones_like(hgt)])
    nrm /= np.linalg.norm(nrm, axis=-1, keepdims=True)
    diff = np.clip(nrm @ SUN, 0, 1)
    lit = np.ones_like(hgt)
    sx, sy = -SUN[0] / np.hypot(SUN[0], SUN[1]), -SUN[1] / np.hypot(SUN[0], SUN[1])
    sz = SUN[2] / np.hypot(SUN[0], SUN[1])
    h, w = hgt.shape
    for s in range(2, shadow_len, 2):
        dx, dy = int(round(-sx * s)), int(round(-sy * s))
        shifted = np.full_like(hgt, -1e9)
        ys0, xs0 = max(0, dy), max(0, dx)
        shifted[ys0:, xs0:] = hgt[:h - ys0, :w - xs0]
        lit = np.minimum(lit, np.where(shifted > hgt + s * sz * 0.9, 0.0, 1.0))
    return ambient + (1 - ambient) * diff * (0.3 + 0.7 * lit)


def blob_mask(w, h, cx, cy, rx, ry, seed, wobble=0.18):
    """An irregular asteroid face outline: an ellipse whose radius wanders with the angle."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    a = np.arctan2((yy - cy) / ry, (xx - cx) / rx)
    rng = np.random.default_rng(seed)
    rad = 1.0
    for k, amp in ((2, wobble), (3, wobble * 0.7), (5, wobble * 0.45), (9, wobble * 0.2), (15, wobble * 0.12)):
        rad = rad + amp * np.sin(k * a + rng.uniform(0, TAU))
    return (np.hypot((xx - cx) / rx, (yy - cy) / ry) < rad).astype(float)


def face_image(hgt, mask, albedo=None, colours=48, rim=True):
    light = shade(hgt)
    tex = 0.5 + 0.5 * raster.fbm(hgt.shape[1], hgt.shape[0], 12, 3171, octaves=3, period=False)
    lum = np.clip(light * (0.75 + 0.35 * tex), 0, 1.25)
    rgb = raster.ramp(WARM_ROCK, np.clip(lum * 0.92, 0, 1))
    if albedo is not None:
        rgb = rgb * albedo[..., None] if albedo.ndim == 2 else rgb * albedo
    op = mask > 0.5
    if rim:                                                     # the face's lit top-left edge and its dark far edge
        pad = np.pad(op, 1)
        tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
        br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
        rgb[tl] = rgb[tl] * 0.5 + np.array((214, 196, 168)) * 0.5
        rgb[br] = rgb[br] * 0.55
    return l30.posterize(rgb, colours, np.where(op, 255, 0).astype(np.uint8))


def pads(img, pads_xy, seed):
    """The nests' pads: a flattened ledge (lighter, smoother), a dark ring and two small marker lamps."""
    a = np.array(img).astype(np.float64)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    for x, y in pads_xy:
        d = np.hypot(xx - x, (yy - y) * 1.1)
        flat = (d < 24) & (a[..., 3] > 0)
        a[flat, :3] = a[flat, :3] * 0.55 + np.array((150, 136, 116)) * 0.45
        ring = (np.abs(d - 24) < 1.0) & (a[..., 3] > 0)
        a[ring, :3] *= 0.45
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    for x, y in pads_xy:
        for lx in (x - 18, x + 18):
            d.point((lx, y + 10), fill=(255, 196, 80, 255))
    return img


OUTCROPS = {
    # name: size, the face's ellipse (cx, cy, rx, ry), seed, pads (x, y in the piece)
    "outcrop-1": ((480, 440), (230, 230, 300, 190), 3141, [(56, 132), (96, 196), (424, 286)]),
    "outcrop-2": ((480, 400), (250, 200, 290, 170), 3151, [(44, 250), (404, 130), (440, 190)]),
}


def outcrop(name):
    (w, h), (cx, cy, rx, ry), seed, pad_xy = OUTCROPS[name]
    mask = blob_mask(w, h, cx, cy, rx, ry, seed)
    edge_clear = np.zeros_like(mask)
    edge_clear[2:-2, :] = 1                                    # the top and bottom rows clear (edges on screen)
    mask = mask * edge_clear
    hgt = face_field(w, h, mask, seed)
    for x, y in pad_xy:                                        # flatten the pads
        yy, xx = np.mgrid[0:h, 0:w].astype(float)
        d = np.hypot(xx - x, yy - y)
        k = np.clip((28 - d) / 6, 0, 1)
        hgt = hgt * (1 - k) + np.mean(hgt[int(y) - 4:int(y) + 4, int(x) - 4:int(x) + 4]) * k
    return pads(face_image(hgt, mask), pad_xy, seed)


def girder(d, x0, y0, x1, y1, width=9):
    """A lattice girder from (x0, y0) to (x1, y1): two dark rails, cross bracing, lit on the top-left rail."""
    v = np.array((x1 - x0, y1 - y0), float)
    ln = np.linalg.norm(v)
    u = v / ln
    nrm = np.array((-u[1], u[0])) * width / 2
    a0, a1 = np.array((x0, y0)) + nrm, np.array((x1, y1)) + nrm
    b0, b1 = np.array((x0, y0)) - nrm, np.array((x1, y1)) - nrm
    steps = int(ln // (width * 1.2))
    for i in range(steps):
        p, q = i / steps, (i + 1) / steps
        d.line([tuple(a0 + (a1 - a0) * p), tuple(b0 + (b1 - b0) * q)], fill=(70, 72, 76, 255))
    lit_first = nrm[0] + nrm[1] < 0
    d.line([tuple(a0), tuple(a1)], fill=(150, 148, 140, 255) if lit_first else (48, 50, 54, 255), width=2)
    d.line([tuple(b0), tuple(b1)], fill=(48, 50, 54, 255) if lit_first else (150, 148, 140, 255), width=2)


def bright_seam_dock(w=W, h=560, seed=3161):
    """The Bright Seam dock: the rock's sunlit face cut straight along the quarry edge (its upper rows open space),
    the berth's floor dark in the middle, two lattice gantries reaching over it, the tug's empty cradle (two yellow
    arms) at the bottom, cool-white work lamps."""
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    edge = 150 + 10 * np.sin(xx / 37 + 1.2) + 6 * np.sin(xx / 13 + 0.4) + 20 * np.exp(-((xx - 240) / 90) ** 2)
    mask = (yy > edge).astype(float)
    hgt = face_field(w, h, mask, seed, relief=26)
    berth = (np.abs(xx - 240) < 120) & (yy > edge + 40)
    hgt = np.where(berth, hgt * 0.25 - 30, hgt)
    alb = np.where(berth, 0.55, 1.0)
    img = face_image(hgt, mask, alb)
    d = ImageDraw.Draw(img)
    for gx in (110, 370):                                      # the gantries: posts on the rim, booms over the berth
        girder(d, gx, h - 10, gx, 230, 10)
        girder(d, gx, 236, 240 + (gx - 240) * 0.25, 236, 8)
    for cx in (196, 284):                                      # the empty cradle's arms
        d.rectangle([cx - 6, h - 160, cx + 6, h - 40], fill=(176, 150, 70, 255), outline=(60, 54, 40, 255))
        for y in range(h - 156, h - 44, 14):
            d.line([cx - 6, y, cx + 6, y + 7], fill=(40, 36, 30, 255), width=2)
    d.rectangle([150, h - 50, 330, h - 36], fill=(120, 122, 126, 255), outline=(50, 52, 56, 255))
    img = l30.strobe(img, 110, 226, 0.6, (230, 240, 255), 9)  # work lamps (baked: steady)
    img = l30.strobe(img, 370, 226, 0.6, (230, 240, 255), 9)
    for x, y in ((60, 220), (430, 200), (240, 300)):
        img = l30.strobe(img, x, y, 0.45, (230, 240, 255), 7)
    a = np.array(img)
    a[:2] = 0
    return Image.fromarray(a, "RGBA")


# =========================================================================== deep pieces

def ceres_crescent(size=360, seed=3165):
    """Ceres behind the tug: the sun beyond its top-left limb, a thin lit crescent, the night side barely there,
    pushed back by the haze (it fades as it falls behind)."""
    import enemies_r03 as e3  # noqa: E402  (concept script: the regolith texture)
    yy, xx = np.mgrid[0:size, 0:size].astype(float)
    c, R = size / 2, size / 2 - 8
    d = np.hypot(xx - c, yy - c)
    tex = np.array(e3.regolith(size, size, seed, craters=36).convert("L")).astype(float) / 255
    nz = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    nx, ny = (xx - c) / R, (yy - c) / R
    lit = np.clip(-0.8 * nx - 0.75 * ny - 0.35 * nz, 0.0, 1.0)
    rgb = raster.ramp(WARM_ROCK, np.clip(0.35 + 0.6 * tex, 0, 1)) * (0.07 + 1.1 * lit[..., None])
    limb = np.abs(d - R) < 1.5
    rgb[limb] = rgb[limb] * 0.5 + np.array((210, 190, 160)) * 0.5 * np.clip(lit[limb, None] * 2, 0.15, 1)
    img = l30.posterize(rgb, 36, np.where(d < R, 255, 0).astype(np.uint8))
    return l30.hazed(img, 0.8, haze=0.3)


def carrier_lights(w=140, h=72):
    """The ambushers' carrier in the dust band: a long dark hull sliver (hazed), its gold bow lights and red running
    lights (1 px cores, stepped halos)."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(10, 40), (120, 30), (132, 36), (120, 42), (12, 48)], fill=(30, 28, 32, 255))
    d.line([(14, 40), (118, 31)], fill=(70, 62, 50, 255))
    glow = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    lights = [(130, 36, (255, 200, 90)), (100, 33, (255, 60, 40)), (70, 36, (255, 60, 40)), (40, 39, (255, 60, 40)),
              (14, 44, (255, 200, 90))]
    for lx, ly, col in lights:
        gd.ellipse([lx - 2.5, ly - 2.5, lx + 2.5, ly + 2.5], fill=col + (70,))
        gd.point((lx, ly), fill=col + (255,))
    img.alpha_composite(glow)
    return artkit.stepped_alpha(l30.hazed(img, 0.9, haze=0.2), 4)


def relay_model():
    """The Vesta relay's beacon tower (+Y up the screen, the top-down sprites' convention for tall things): a lattice
    mast on a round base, a dish, two solar vanes, the beacon housing at the top."""
    mats = [Material(np.array(raster.hexrgb("9aa0a8")) / 255.0, metal=0.5, shininess=60, spec=0.6),
            Material(np.array(raster.hexrgb("4a4e54")) / 255.0, metal=0.5, shininess=40, spec=0.5),
            Material(np.array(raster.hexrgb("2c3c5c")) / 255.0, metal=0.3, shininess=90, spec=1.1),
            Material((0.14, 0.14, 0.16), metal=0.6, shininess=120, spec=1.0),
            Material(np.array(raster.hexrgb("d8d8d0")) / 255.0, shininess=40, spec=0.4)]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, -2.9, 0.1), 0.9, 0.14), 0),
                 (sd_cylinder_z(p, (0, -2.9, 0.26), 0.5, 0.06), 1)]
        for sx in (-0.18, 0.18):
            items.append((sd_capsule(p, (sx, -2.7, 0.3), (sx * 0.5, 2.7, 0.3), 0.05), 1))
        for k in range(12):
            y0 = -2.6 + k * 0.44
            items.append((sd_capsule(p, (-0.17, y0, 0.3), (0.17, y0 + 0.4, 0.3), 0.025), 1))
        items += [(sd_box(p, (-0.62, 0.6, 0.3), (0.42, 0.22, 0.02)), 2), (sd_box(p, (0.62, 0.6, 0.3), (0.42, 0.22, 0.02)), 2),
                  (sd_sphere(p, (0.36, -0.9, 0.42), 0.3), 4),
                  (sd_sphere(p, (0, 2.84, 0.32), 0.14), 3)]
        return union(*items)
    return scene, mats


def frigate_model():
    """A CDF frigate (+Y forward): a long grey hull, a blue stripe, the bridge block, two drive bells."""
    mats = [Material(np.array(raster.hexrgb("8a929c")) / 255.0, metal=0.5, shininess=60, spec=0.6),
            Material(np.array(raster.hexrgb("3c5c8c")) / 255.0, metal=0.3, shininess=40, spec=0.4),
            Material(np.array(raster.hexrgb("40444a")) / 255.0, metal=0.5, shininess=40, spec=0.5),
            Material((0.1, 0.1, 0.12), emission=(0.6, 0.85, 1.0))]

    def scene(p):
        return union((sd_capsule(p, (0, -1.6, 0.2), (0, 1.7, 0.2), 0.42, 0.3), 0),
                     (sd_box(p, (0, 0.2, 0.55), (0.06, 1.3, 0.04)), 1),
                     (sd_box(p, (0, -0.6, 0.5), (0.3, 0.4, 0.2), 0.05), 2),
                     (sd_capsule(p, (-0.26, -1.7, 0.2), (-0.26, -2.1, 0.2), 0.14, 0.2), 2),
                     (sd_capsule(p, (0.26, -1.7, 0.2), (0.26, -2.1, 0.2), 0.14, 0.2), 2),
                     (sd_sphere(p, (0, 1.9, 0.3), 0.08), 3))
    return scene, mats


RELAY = (140, 300)
FRIGATE = (64, 112)
ROCK_PIECES = {"tumbling-rock": (104, 104, 3171), "drift-rock-a": (88, 80, 3173), "drift-rock-b": (64, 56, 3177)}


def _render_job(job):
    kind = job[0]
    if kind == "relay":
        scene, mats = relay_model()
        return l30.render_model(scene, mats, RELAY, RELAY[0] / 22.0, k=8)
    if kind == "frigate":
        scene, mats = frigate_model()
        return l30.render_model(scene, mats, FRIGATE, FRIGATE[0] / 16.0, k=8)
    if kind == "rock":
        name, k = job[1], job[2]
        w, h, seed = ROCK_PIECES[name]
        radius = min(w, h) / 2 / 20 * 0.86
        scene, mats = bu.rock_model(radius, seed)
        return l30.render_model(scene, mats, (w, h), w / 20.0, k=k, crisp=70)
    if kind == "hauler":
        return l29._render_traffic(("ore-hauler", job[1]))
    if kind == "outcrop":
        return outcrop(job[1])
    if kind == "dock":
        return bright_seam_dock()
    raise ValueError(kind)


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    save("open-space", l30.space_tile(seed=3101))
    save("open-dust", l30.dust_tile(seed=3102))
    save("ceres-crescent", ceres_crescent())
    for name, (w, h, seed) in {"far-rock-a": (120, 100, 3121), "far-rock-b": (84, 72, 3123),
                               "far-rock-c": (56, 48, 3129)}.items():
        rock = np.array(l30.far_rock(w, h, seed))
        rock[:1], rock[-1:], rock[:, :1], rock[:, -1:] = 0, 0, 0, 0          # the deep layer's edge rule
        save(name, Image.fromarray(rock, "RGBA"))
    save("carrier-lights", carrier_lights())
    jobs = [("relay",), ("frigate",), ("dock",), ("outcrop", "outcrop-1"), ("outcrop", "outcrop-2")]
    jobs += [("rock", "tumbling-rock", k) for k in range(16)] + [("rock", "drift-rock-a", 5),
                                                                  ("rock", "drift-rock-b", 9)]
    jobs += [("hauler", k) for k in range(16)]
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(_render_job, jobs, chunksize=1))
    relay, frigate, dock, oc1, oc2 = res[:5]
    relay = l30.hazed(relay, 0.85, haze=0.3)
    top = l30._top_light(relay)
    frames = [l30.strobe(relay, *top, k, (90, 255, 120), 14) for k in (1.0, 0.45, 0, 0, 0, 0, 0, 0)]
    for i, f in enumerate(artkit.quantize_set(frames, 32)):
        save(f"vesta-relay_{i}", f)
    fr = l30.hazed(frigate, 0.8, haze=0.3)
    for x, y in ((32, 8), (22, 70), (42, 70)):
        fr = l30.strobe(fr, x, y, 0.5, (230, 240, 255), 5)
    save("cdf-frigate", artkit.quantize_set([fr], 24)[0])
    save("bright-seam-dock", dock)
    save("outcrop-1", oc1)
    save("outcrop-2", oc2)
    tumble = artkit.quantize_set([l30.hazed(f) for f in res[5:21]], 24)
    for i, f in enumerate(tumble):
        save(f"tumbling-rock_{i}", f)
    save("drift-rock-a", artkit.quantize_set([l30.hazed(res[21])], 20)[0])
    save("drift-rock-b", artkit.quantize_set([l30.hazed(res[22])], 20)[0])
    haul = artkit.quantize_set(res[23:39], 32)
    for i, f in enumerate(haul):
        save(f"ore-hauler_{i}", f)
    return pieces_spec(), placements()


def pieces_spec():
    return {
        "ceres-crescent": {"layer": "deep", "size": [360, 360]},
        "far-rock-a": {"layer": "deep", "size": [120, 100]},
        "far-rock-b": {"layer": "deep", "size": [84, 72]},
        "far-rock-c": {"layer": "deep", "size": [56, 48]},
        "carrier-lights": {"layer": "deep", "size": [140, 72]},
        "vesta-relay": {"layer": "deep", "size": list(RELAY), "frames": 8, "fps": 4},
        "cdf-frigate": {"layer": "deep", "size": list(FRIGATE)},
        "bright-seam-dock": {"layer": "ground", "size": [W, 560]},
        "outcrop-1": {"layer": "ground", "size": list(OUTCROPS["outcrop-1"][0])},
        "outcrop-2": {"layer": "ground", "size": list(OUTCROPS["outcrop-2"][0])},
        "ore-hauler": {"layer": "low-air", "size": [64, 64], "headings": 16},
        "tumbling-rock": {"layer": "low-air", "size": [104, 104], "frames": 16, "fps": 2},
        "drift-rock-a": {"layer": "low-air", "size": [88, 80]},
        "drift-rock-b": {"layer": "low-air", "size": [64, 56]},
    }


# =========================================================================== the places

NEST = 44


def target_row(t, h=NEST):
    """The ground row of the centre of a target h px tall entering at the top edge at t (backdrop_l29.py's)."""
    return scroll(t) + SCREEN + h / 2


def outcrop_t(name, first_t):
    """The placement time of an outcrop whose first pad (the lowest in the piece: a piece's bottom enters at the top
    edge first) is under the nest entering at the top edge at ``first_t``; and every pad's nest: (t, x)."""
    (w, h), _, _, pad_xy = OUTCROPS[name]
    low_pad = max(pad_xy, key=lambda p: p[1])
    # the piece's centre row: the pad's row (piece y down from its top) on the nest's centre row
    centre = target_row(first_t) + (low_pad[1] - h / 2)
    t = time_at(centre - SCREEN / 2)
    nests = []
    for x, y in sorted(pad_xy, key=lambda p: -p[1]):
        row = centre - (y - h / 2)
        nests.append((round(time_at(row - SCREEN - NEST / 2), 2), x))
    return round(t, 3), nests


OUTCROP_T = {"outcrop-1": 60.0, "outcrop-2": 119.0}
TARGETS = {name: outcrop_t(name, t)[1] for name, t in OUTCROP_T.items()}


def hauler_path(t0, t1, t2, x, y0, y1, dx):
    """A hauler path (backdrop_l29.py's ``path``: [t, dx, dy] keyframes, dy up the layer): it enters at screen y y0
    at t0, holds y1 behind the tug from t1 (drifting dx sideways), then falls behind from t1 + 8 and leaves by t2."""
    f = FACTORS["low-air"]
    centre = scroll(t0) * f + SCREEN - y0

    def dy(t, y):
        return round(scroll(t) * f + SCREEN - centre - y, 1)
    pts = [[t0, 0, 0.0], [t1, round(dx * 0.5), dy(t1, y1)], [t1 + 8, dx, dy(t1 + 8, y1 + 10)], [t2, dx, dy(t2, 620)]]
    return t_for_layer_row(centre, f), x, pts


def placements():
    """Every placed piece: dict(name, layer, t, x, note[, path])."""
    out = []
    dock_t = t_centre_at(0.0, 230 + 280, 1.0)                  # the dock's top at y 230 at t=0: the cradle under the tug
    out.append(dict(name="bright-seam-dock", t=dock_t, x=240, note="Bright Seam's dock scrolling away as the tug "
                                                                   "rises out of its cradle (t 0-6)"))
    for name, first in OUTCROP_T.items():
        t, nests = outcrop_t(name, first)
        out.append(dict(name=name, t=t, x=240, note=f"{name}: its pads carry the nests at "
                        + ", ".join(f"x {x} (t {nt:g})" for nt, x in nests)))
    out.append(dict(name="ceres-crescent", t=t_centre_at(0.0, 470, FACTORS["deep"]), x=360,
                    note="Ceres a fading crescent behind (sections 1-2)"))
    out += [dict(name="far-rock-a", t=16.0, x=80, note="a far rock"), dict(name="far-rock-c", t=44.0, x=410, note=""),
            dict(name="far-rock-b", t=78.0, x=130, note=""), dict(name="far-rock-a", t=104.0, x=400, note=""),
            dict(name="far-rock-c", t=122.0, x=60, note="")]
    out.append(dict(name="carrier-lights", t=t_centre_at(140.0, 150, FACTORS["deep"]), x=330,
                    note="the ambushers' carrier in the dust band (section 5)"))
    out.append(dict(name="vesta-relay", t=t_centre_at(END, 200, FACTORS["deep"]), x=330,
                    note=f"the Vesta relay's beacon tower, centred 200 px down at the end (t={END:g})"))
    out.append(dict(name="cdf-frigate", t=t_centre_at(END, 260, FACTORS["deep"]), x=200,
                    note="a CDF frigate waiting at the relay"))
    for t0, t1, t2, x, y0, y1, dx, note in ((-1.0, 6.0, 20.0, 64, 620, 470, 6, "a refugee hauler falling in behind "
                                                                                 "the tug (left)"),
                                            (0.5, 7.5, 22.0, 416, 640, 486, -6, "the second hauler (right)")):
        pt, px_, path = hauler_path(t0, t1, t2, x, y0, y1, dx)
        out.append(dict(name="ore-hauler", t=pt, x=px_, path=path, note=note))
    out += [dict(name="drift-rock-a", t=34.0, x=420, note="rocks drifting below the play plane, dimmed"),
            dict(name="tumbling-rock", t=50.0, x=60, note=""), dict(name="drift-rock-b", t=84.0, x=420, note=""),
            dict(name="tumbling-rock", t=100.0, x=430, note=""), dict(name="drift-rock-a", t=114.0, x=50, note=""),
            dict(name="drift-rock-b", t=146.0, x=60, note="")]
    pieces = pieces_spec()
    for p in out:
        p["layer"] = pieces[p["name"]]["layer"]
        p["t"] = round(float(p["t"]), 3)
    return out


# Level 31's debris (Level 03's rules, the belt's chunks; tools/art/belt_units.py, belt_units_c.py and
# belt_units_d.py's Dacosta's claim). The README: the first rocks at t~26; section 2 at most 2 big ones on the screen;
# section 3 sparse; section 4 the densest drift (up to 3 big, 6 small); section 5 thinning; none in the relay's
# approach. Drift y: the scroll (120) plus 30-60 for small, plus 20 for big (y up, negative: down the screen);
# x: 20-60 px/s across for small ones, slower for big ones. They pass over the tug (ground layer).
LARGE = [(30, 60), (44, 420), (52, 60), (70, 420), (84, 60), (94, 420), (97, 60), (101, 420), (105, 240),
         (109, 60), (113, 420), (117, 60), (121, 420), (125, 60), (138, 420), (152, 60)]
SMALL_X = (100, 380, 200, 300, 140, 340, 60, 420, 240)


def debris_rows():
    rows = []
    for i, (t, x) in enumerate(LARGE):
        rows.append((t, x, "belt-large-a" if i % 2 == 0 else "belt-large-b", [10 if x < 240 else -10, -140]))
    t, i = 26.0, 0
    while t < 160:
        gap = 3.0 if t < 58 else 4.6 if t < 92 else 1.2 if t < 128 else 3.4
        rows.append((round(t, 1), SMALL_X[i % len(SMALL_X)], "belt-" + "abc"[i % 3],
                     [(-1) ** i * (20 + 10 * (i % 5)), -(150 + 10 * (i % 4))]))
        t += gap
        i += 1
    rows.append((100.0, 300, "claim-rock", [-18, -146]))
    rows.sort(key=lambda r: (r[0], r[1]))
    return rows


def debris_lines():
    lines = ["debris:",
             "  # Proposal (ART-D): the belt's rocks; small ones break (6 HP, part B's), large ones are walls (contact",
             "  # `large` 20 per the belt's hazards). Dacosta's claim (the secret, 12 HP, its crate on break) as a",
             "  # chunk of its own (sprites debris-claim-rock: tools/art/belt_units_d.py). D2 checks the README's rules",
             "  # against the waves: no rock line within 40 px of a spawn point, no big rock in the ship's lowest",
             "  # 120 px while a rear wave enters, Mites' rides on small ones.",
             "  chunks:",
             "    belt-large-a: {size: [88, 80], damage: 20}",
             "    belt-large-b: {size: [66, 60], damage: 20}",
             "    belt-a: {size: [40, 36], hp: 6}",
             "    belt-b: {size: [36, 32], hp: 6}",
             "    belt-c: {size: [32, 28], hp: 6}",
             "    claim-rock: {size: [34, 30], hp: 12}",
             "  clearance: 120",
             "  max_large: 3",
             "  easy: {leave_out_large: 4}",
             "  placed:"]
    for t, x, chunk, drift in debris_rows():
        note = "  # Dacosta's claim (secret)" if chunk == "claim-rock" else ""
        lines.append(f"    - {{t: {t:g}, x: {x}, chunk: {chunk}, drift: [{drift[0]}, {drift[1]}]}}{note}")
    return lines


def proposal_text(pieces, placed):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after D2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 31's open belt (tools/art/backdrop_l31.py, step ART-D, from {src}).",
             "  # A ground layer without a tile set: open space is the deep layer (the base); the ground holds the",
             "  # Bright Seam dock and the two outcrops (the nests' pads) and, drawn by the game, the tug. Low-air",
             "  # holds the refugee haulers (paths) and the dimmed rocks; high-air the close grit. At most two animated",
             "  # pieces on the screen at once (the relay's strobe, a tumbling rock).",
             "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused",
             "  ramp: 2",
             "  haze_colour: 09090e",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:",
             f"    open-space: {{layer: deep, height: {SPACE_H}}}   # space, the sunlit dust band, stars",
             f"    open-dust: {{layer: high-air, height: {DUST_H}}}   # close grit and dust motes (transparent)",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        extra += f", headings: {spec['headings']}" if spec.get("headings") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        extra = f", path: {p['path']}" if p.get("path") else ""
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}{extra}}}")
    lines.append("sections:   # ART-D's reading of the README's Layout (120 px/s throughout); D2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {v}" for k, v in s.items()) + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); low-air "
                 f"{int(round(scroll(END) * FACTORS['low-air']))}")
    lines += ["# The tug's station band (m9d-gaps.md §6, r60 J.6 b; the escort's hull block is D2's): centre (240, 380)",
              "# y down, footprint x 140-340, y 230-530; with the sway (20 px across, 5 px up and down, 14 s) x 130-350,",
              "# y 227.5-532.5. No ground target under it: the nests stand on the outcrops' pads at x <= 100 or >= 380.",
              "ground_targets:  # at: [t, x], when the nest enters at the top edge and its x (on the outcrops' pads)"]
    for i, (name, nests) in enumerate(TARGETS.items()):
        lines += [f"  - target: SAM Nest trio on {name.replace('-', ' ')}",
                  f"    section: {3 if i == 0 else 4}",
                  "    enemy: sam-nest",
                  "    count: 3",
                  "    at: [" + ", ".join(f"[{t:g}, {x}]" for t, x in nests) + "]"]
    lines += debris_lines()
    return "\n".join(lines) + "\n"


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["debris"], data["ground_targets"]


# =========================================================================== composite, review, check

def piece_images(name, spec):
    n = spec.get("frames") or spec.get("headings")
    if n:
        return [Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)]
    return [Image.open(OUT / f"{name}.png").convert("RGBA")]


_CACHE = {}


def images(name, spec):
    if name not in _CACHE:
        _CACHE[name] = piece_images(name, spec)
    return _CACHE[name]


def tile_rows(name, height, offset):
    if name not in _CACHE:
        _CACHE[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[name]
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[height - 1 - (rows % height)]


def place_of(p, spec, t):
    """(x, y_down of the centre, frame index) of a placed piece at level time t, or None when off the screen (the
    path's keyframes as backdrop_l29.py's blit reads them)."""
    f = FACTORS[spec["layer"]]
    centre = scroll(float(p["t"])) * f + SCREEN / 2
    dx = dy = 0.0
    k = 0
    if p.get("path"):
        pts = p["path"]
        if t < pts[0][0] or t > pts[-1][0]:
            return None
        for a, b in zip(pts, pts[1:]):
            if a[0] <= t <= b[0]:
                u = (t - a[0]) / (b[0] - a[0])
                dx, dy = a[1] + (b[1] - a[1]) * u, a[2] + (b[2] - a[2]) * u
                mx, my = b[1] - a[1], -(b[2] - a[2])         # screen-down motion against the layer
                k = int(round(np.arctan2(-mx, my) / TAU * 16)) % 16
    elif spec.get("frames"):
        k = int(t * spec["fps"]) % spec["frames"]
    y = scroll(t) * f + SCREEN - centre - dy
    return p["x"] + dx, y, k


def blit(img, p, pieces, t, layer):
    spec = pieces[p["piece"]]
    if spec["layer"] != layer:
        return img
    at = place_of(p, spec, t)
    if at is None:
        return img
    x, y, k = at
    ims = images(p["piece"], spec)
    frame = ims[k % len(ims)]
    if y - frame.height / 2 > SCREEN or y + frame.height / 2 < 0:
        return img
    return bu.paste(img, frame, x, y)


def _held(name):
    try:
        return bu.load(name)
    except FileNotFoundError:
        return None


def compose(t, pieces, placed, debris=None, targets=None, units=True):
    """The screen at level time t (480x540): the deep tile and pieces, the ground pieces, the tug and the nests
    (stand-ins: the game draws them), low-air pieces, the play plane's stand-ins, high-air grit."""
    g = scroll(t)
    img = Image.fromarray(tile_rows("open-space", SPACE_H, g * FACTORS["deep"]), "RGBA")
    for layer in ("deep", "ground"):
        for p in placed:
            img = blit(img, p, pieces, t, layer)
    if units:
        img = ground_stand_ins(img, t, targets)
    for p in placed:
        img = blit(img, p, pieces, t, "low-air")
    if units:
        img = air_stand_ins(img, t, debris)
    dust = Image.fromarray(tile_rows("open-dust", DUST_H, g * FACTORS["high-air"]), "RGBA")
    img.alpha_composite(dust)
    return img


def tug_at(t):
    """The tug's centre (y down) at level time t: rising from y 690 to 380 over t 0-6, then on station, swaying."""
    th = TAU * t / 14.0
    x, y = 240 + 10 * np.sin(th), 380 + 2.5 * np.sin(2 * th)
    if t < 6:
        u = t / 6
        y = 690 + (380 - 690) * (u * u * (3 - 2 * u))
    return x, y


def ground_stand_ins(img, t, targets):
    if targets:
        base, rack = _held("sam-nest-base"), _held("sam-nest")
        for grp in targets:
            for nt, x in grp["at"]:
                y = scroll(t) - scroll(nt) - NEST / 2
                if -40 < y < SCREEN + 40:
                    if base:
                        img = bu.paste(img, base[0], x, y)
                    if rack:
                        img = bu.paste(img, rack[0], x, y)
    pv = None
    try:
        import json
        import belt_units_d as bud  # noqa: E402  (the tug's production sprites)
        pv = json.loads(bud.pivot_path("mining-tug").read_text(encoding="utf-8"))
    except (FileNotFoundError, ImportError):
        pass
    if pv:
        x, y = tug_at(t)
        for s in ("engines", "cargo", "bow"):
            fr = _held(f"mining-tug-{s}")
            if fr:
                ox, oy = pv["sections"][s]["offset"]
                img = bu.paste(img, fr[0], x + ox, y + oy)
    return img


def air_stand_ins(img, t, debris):
    if debris:
        for row in debris["placed"]:
            u = t - row["t"]
            if u < 0:
                continue
            fr = _held(f"debris-{row['chunk']}")
            if not fr:
                continue
            x = row["x"] + row["drift"][0] * u
            y = -20 - row["drift"][1] * u
            if -60 < y < SCREEN + 60:
                img = bu.paste(img, fr[int(u * 2) % len(fr)], x, y)
    return bu.paste(img, artkit.load_frames("ship")[2], 240 + 140 * np.sin(t * 0.5), 200)


SHOTS = [(0.0, "BRIGHT SEAM DOCK: THE TUG IN ITS CRADLE"), (3.0, "THE TUG RISING, THE DOCK SCROLLS AWAY"),
         (30.0, "THE OPEN BELT: CERES BEHIND, THE FIRST ROCKS"), (62.0, "OUTCROP 1: THREE NESTS BESIDE THE HULL"),
         (104.0, "THE DRIFT: DACOSTA'S CLAIM"), (121.0, "OUTCROP 2"), (142.0, "THE AMBUSH: THE CARRIER'S LIGHTS"),
         (END - 0.5, "THE VESTA RELAY AND A CDF FRIGATE")]


def review():
    pieces, placed, debris, targets = load_proposal()
    shots = [(compose(t, pieces, placed, debris, targets), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    piece_names = ["bright-seam-dock", "outcrop-1", "outcrop-2", "vesta-relay", "cdf-frigate", "carrier-lights",
                   "ceres-crescent", "ore-hauler", "tumbling-rock", "drift-rock-a", "far-rock-a"]
    pieces_h = 250
    tile_h = SPACE_H // 4
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + pieces_h + tile_h + 70,
                         "LEVEL 31 BACKDROP (FINAL R64): THE OPEN BELT",
                         f"PRODUCTION ART, {BATCH.upper()} - R64")
    raster.draw_text(sheet, 16, 38, "EIGHT SCREENS AT HALF SIZE WITH STAND-INS (THE TUG ON STATION, THE NESTS ON "
                                    "THEIR PADS, THE ROCKS IN THEIR PROPOSED PLACES, THE SHIP)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 10
    raster.draw_text(sheet, 16, y, "THE PIECES ON THE OPEN SPACE (THE GROUND PIECES AT HALF SIZE, THE REST AT 1X; "
                                   "THE RELAY'S FLASH)", raster.LABEL)
    x = 16
    space = Image.open(OUT / "open-space.png").convert("RGBA")
    for name in piece_names:
        fr = images(name, pieces[name])[8 if name == "ore-hauler" else 0]
        if pieces[name]["layer"] == "ground":
            fr = fr.resize((fr.width // 2, fr.height // 2), Image.BOX)
        if fr.height > 230:
            fr = fr.resize((fr.width * 230 // fr.height, 230), Image.BOX)
        if x + fr.width > sheet.width - 16:
            break
        cell = space.crop((0, 0, fr.width + 8, fr.height + 8))
        cell.alpha_composite(fr, (4, 4))
        sheet.alpha_composite(cell, (x, y + 16))
        x += fr.width + 12
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (QUARTER SIZE): OPEN-SPACE; OPEN-DUST (ON A GREY PLATE)", raster.LABEL)
    x = 16
    for name in ("open-space", "open-dust"):
        tl = Image.open(OUT / f"{name}.png").convert("RGBA")
        small = tl.resize((tl.width // 4, tl.height // 4), Image.BOX) if name == "open-space" else tl.resize(
            (tl.width // 2, tl.height // 4), Image.NEAREST)
        pl = Image.new("RGBA", small.size, (60, 64, 76, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl, (x, y + 16))
        x += small.width + 12
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    frames = [compose(i / 10, pieces, placed, debris, targets).resize((sw, sh), Image.BOX) for i in range(80)]
    size = e60.rig.write_gif(frames, gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def check():
    """The space tile wraps (BackdropSeamsTest's measure) and is opaque, sizes match the proposal, Source chunks, the
    deep pieces' edges transparent (BackdropAssetsTest's edge rule: the deep layer is the base), the ground pieces'
    top and bottom rows clear, every placement on the screen, at most two animated pieces on the screen at once
    (BackdropCheck's motion budget), the nests at x <= 100 or >= 380 and on opaque pad pixels."""
    problems = []
    pieces, placed, _, targets = load_proposal()
    a = np.array(Image.open(OUT / "open-space.png").convert("RGBA"))
    if l30.wrap_score(a) > 1.5:
        problems.append(f"open-space: wrap score {l30.wrap_score(a):.2f}")
    if (a[..., 3] < 255).any():
        problems.append("open-space: not opaque")
    for name, spec in pieces.items():
        for im in piece_images(name, spec):
            if list(im.size) != list(spec["size"]):
                problems.append(f"{name}: {im.size} vs {spec['size']}")
                break
    for f in OUT.glob("*.png"):
        if Image.open(f).text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
    for name, spec in pieces.items():
        for im in piece_images(name, spec):
            al = np.array(im)[..., 3] > 0
            if spec["layer"] == "deep" and (al[0].any() or al[-1].any() or al[:, 0].any() or al[:, -1].any()):
                problems.append(f"{name}: an opaque edge on the deep (base) layer")
            if spec["layer"] == "ground" and name != "bright-seam-dock" and (al[0].any() or al[-1].any()):
                problems.append(f"{name}: an opaque top or bottom row on the ground")
    seen = set()
    worst = (0, 0.0, ())
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = set()
        for i, p in enumerate(placed):
            spec = pieces[p["piece"]]
            at = place_of(p, spec, t)
            if at and at[1] + spec["size"][1] / 2 > 0 and at[1] - spec["size"][1] / 2 < SCREEN:
                seen.add(i)
                if spec.get("frames"):
                    animated.add(p["piece"])
        if len(animated) > worst[0]:
            worst = (len(animated), t, tuple(sorted(animated)))
    if worst[0] > 2:
        problems.append(f"t={worst[1]:.1f}: {worst[0]} animated pieces on the screen: {worst[2]}")
    for i, p in enumerate(placed):
        if i not in seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")
    for grp in targets:                                         # the nests: beside the hull, on their pads
        for nt, x in grp["at"]:
            if 100 < x < 380:
                problems.append(f"nest x {x} (t {nt}) under the tug's swayed footprint")
            row = target_row(nt)
            on = False
            for p in placed:
                if p["piece"].startswith("outcrop"):
                    spec = pieces[p["piece"]]
                    im = images(p["piece"], spec)[0]
                    centre = scroll(p["t"]) + SCREEN / 2
                    py = int(round(spec["size"][1] / 2 - (row - centre)))
                    px = int(round(x - p["x"] + spec["size"][0] / 2))
                    if 0 <= py < im.height and 0 <= px < im.width and np.array(im)[py, px, 3] > 0:
                        on = True
            if not on:
                problems.append(f"nest x {x} (t {nt}) not on an outcrop's pixels")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"{len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}; {len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" not in args:
        pc, pl = build_all()
        write_proposal(pc, pl)
    if "--proposal" not in args:
        review()
