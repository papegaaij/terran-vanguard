#!/usr/bin/env python3
"""Production art: the Level 17 backdrop, Dome Siege (design/campaign/act-3-red-dust/level-17-dome-siege;
M7 part D batch, concept round 40, step ART-D).

Straight to production from the Ares Landing mood still `mars-ares-landing-r36-a` (tools/concept/
mars_r36.py, imported unchanged: the plain's ramp, Vrell growth in violet with teal veins, the Helix site
model `helix_site`) and the chosen dome a (MA.3: the dome itself is the ally's sprite, tools/art/
siege_units.py; the backdrop carries its apron and greenhouse annexes). Lit from the upper left, receding
(rule 3), posterized per image, wide gradients ordered-dithered. Nothing lit is mirrored.

The ground is opaque everywhere (no `deep` layer: the README's "Olympus Mons on the horizon" cannot show
under a top-down ground that covers the screen; pending the user's review in round 40). Layers: ground
(x1), low-air (x1.35), no far/deep/high-air.

Tile sets (480 wide, 960 tall, wrapping): `ares-plain` (section 1: the rust plain north of the colony,
wind ripples, dust drifts, small craters and rocks), `colony-ground` (sections 2 and 4: the plain worked
by the colony, rover tracks, cable trenches, pale concrete dust), `burnt-fields` (section 3: the burnt
outer greenhouse fields, soot and ash streaks, burn scars), `spaceport-apron` (section 5: concrete slabs,
joints, stains, painted taxi lines); low-air `dust-wisps` (`light`, at most 30 %) and `smoke-banks`
(`medium` 72-80: the smoke over the burnt greenhouses, at most 45 %).

Pieces (ground unless noted): `seam-domes`, `seam-burnt`, `seam-inner`, `seam-apron` (480x320, exact
rows: the section seams); the rail line from the north: `rail-track` (96x320, tiling along itself: the
embankment, ballast, sleepers every 8 px, two rails, a cable trough), `rail-bend-a` (x 310 -> 240) and
`rail-bend-b` (240 -> 160), both 192x640, and `rail-yard` (240x960: sidings, two wagons, the buffer stops
where the line ends), all drawn from one field in ground positions so that, placed at their exact rows, they
join without a seam; `freight-train` (the stranded train on the line in section 1); `dome-apron-west`,
`dome-apron-east` (184x184: a dome's concrete apron with its greenhouse annexes on that side and the road
stub on the other, toward the rail) and `dome-apron-eden` (216x216, annexes both sides) under the five
domes; `vrell-growth-a`, `vrell-growth-b` (Vrell nest growth creeping toward the rings and under the floes
and nests, never within 100 px of the compound); `burnt-greenhouses` (section 3: three burnt tube
greenhouses, collapsed); `helix-compound` (section 3, x 290-470, t~78-85: mars_r36.helix_site as Level 16's
station, x1.25, turned with its long side along the flight; a clean, lit gravel yard, a white fence, two
lamp masts, no growth); `ring-road-north`, `ring-road-south` (480x160: the inner ring road's arcs as the
flight enters and leaves the ring); `gun-pit` (56x56, an unmanned militia flak pit: sandbags and a twin
gun); `landing-pad` (128x128) and `hangar` (152x120) on the spaceport; low-air `smoke-plume` (96x96, at
most 45 %) over the burnt greenhouses.

Outputs: assets/backdrop/level-17/*.png; the review sheet and GIF
design/campaign/act-3-red-dust/level-17-dome-siege/concept/backdrop-final-r40-a.png/.gif (the domes drawn
from the ally's sprites at their placements); `--proposal` writes the level's `backdrop:` block to
briefs/m7d-backdrop.yaml (for the main agent to merge; never into the level's data.yaml).

Reads the level's data.yaml (sections with speed and `ramp`, the allies' domes, the ground targets),
otherwise the README's values. Run: python3 tools/art/backdrop_l17.py [--proposal | --review] [id ...]
(all: about 2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, write_gif

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, reduce, quantize, YAML flow)
import backdrop_l15 as b15  # noqa: E402  (tools/art: periodic noise, lighting, posterizing, growth helpers)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps, the Helix site, the dome's glass)
from render import raster, sdf, sprite  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, union  # noqa: E402

SCRIPT = "backdrop_l17.py"
BATCH = "M7 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-17-dome-siege"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = ROOT / "briefs" / "m7d-backdrop.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-17"
REVIEW = CONCEPT / "backdrop-final-r40-a.png"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
FACTORS = {"ground": 1.0, "low-air": 1.35}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
RAMP = 3
ATMOSPHERE = {
    "clear": {"haze": 0.06},                                  # the spaceport: haze only
    "light": {"banks": "dust-wisps", "haze": 0.12},           # thin dust wisps over the plain
    "medium": {"banks": "smoke-banks", "haze": 0.18},         # the smoke over the burnt greenhouses (72-80)
}
DUST = m36.DUST
TEAL = np.array((40, 200, 150), float)

pn, aniso, sample, smooth, ramp = b15.pn, b15.aniso, b15.sample, b15.smooth, b15.ramp
lit, recede, posterize_rgb, dither_alpha = b15.lit, b15.recede, b15.posterize_rgb, b15.dither_alpha
jr = k8.java_round


# =========================================================================== the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists (2 s eases)
    {"name": "Rail Approach", "end": 30, "atmosphere": "light"},
    {"name": "Outer Domes", "end": 72, "speed": 80, "ramp": 2, "atmosphere": "light"},
    {"name": "Helix Compound", "end": 98, "speed": 110, "ramp": 2, "atmosphere": "light",
     "peak": {"atmosphere": "medium", "from": 72, "to": 80}},
    {"name": "Inner Ring", "end": 140, "speed": 70, "ramp": 2, "atmosphere": "light"},
    {"name": "Spaceport", "end": 172, "speed": 130, "ramp": 2, "atmosphere": "clear"},
]
SECTION_TILES = [["ares-plain"], ["colony-ground"], ["burnt-fields"], ["colony-ground"], ["spaceport-apron"]]
DEFAULT_DOMES = [("Bradbury", 38, 130, 120), ("Lowell", 46, 350, 120), ("Schiaparelli", 104, 110, 120),
                 ("Burroughs", 110, 370, 120), ("Eden", 126, 240, 144)]


def _load(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


DATA = _load(LEVEL_DIR / "data.yaml")
SECTIONS = [dict(s) for s in (DATA.get("sections") or DEFAULT_SECTIONS)]
SPEED = float(DATA.get("scroll_speed", 130))
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
END = float(SECTIONS[-1]["end"])


def speed(i):
    return float(SECTIONS[i].get("speed", SPEED))


def ease(i):
    if i == 0 or speed(i) == 0 or speed(i - 1) == 0:
        return 0.0
    return float(SECTIONS[i].get("ease", SECTIONS[i].get("ramp", 0)) or 0)


def _travelled(i, a, b):
    v, s = speed(i), STARTS[i]
    px = (b - a) * v
    e = ease(i)
    if e > 0:
        old = speed(i - 1)

        def lost(t):
            u = min(max((t - s) / e, 0.0), 1.0)
            return e * (u - u ** 3 + u ** 4 / 2)
        px -= (v - old) * (lost(b) - lost(a))
    return px


def scroll_at(t):
    """LevelData.scrollAt with the M7 part B ease (as backdrop_l16.scroll_at)."""
    scroll = 0.0
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            return scroll + _travelled(i, STARTS[i], t)
        scroll += _travelled(i, STARTS[i], s["end"])
    raise AssertionError


def t_at(scroll):
    lo, hi = -60.0, END + 80
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll_at(mid) < scroll:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def seam(layer, s):
    """Where section s's tile set begins on the layer."""
    return jr(scroll_at(STARTS[s]) * FACTORS[layer] + SCREEN)


def t_for_bottom(layer, bottom, h):
    """A placement time whose piece (height h) has its bottom edge exactly at layer position ``bottom``."""
    f = FACTORS[layer]
    t = round(t_at((bottom + h / 2 - MID) / f), 4)
    for dt in (0, 1e-4, -1e-4, 2e-4, -2e-4, 3e-4, -3e-4, 4e-4, -4e-4):
        if jr(scroll_at(t + dt) * f + MID - h / 2) == bottom:
            return round(t + dt, 4)
    raise AssertionError((layer, bottom, h, t))


def t_centre(pos):
    """The placement time of a ground piece centred on ground position ``pos``."""
    return round(t_at(pos - MID), 3)


def top_pos(t):
    """The ground position at the screen's top edge at time t."""
    return scroll_at(t) + SCREEN


def domes():
    """(name, enter t, x, footprint): the allies' units of the data, else the README's table."""
    units = (DATA.get("allies") or {}).get("units")
    if not units:
        return DEFAULT_DOMES
    out = []
    for u in units:
        t, x = u["at"]
        out.append((u["name"], float(t), float(x), int((u.get("size") or [120])[0])))
    return out


def dome_pos(t, size):
    """The rail's and the growth's reference position for a dome: ART-D's top-edge reading (their layout keeps it)."""
    return top_pos(t) - size / 2


def dome_centre(t, size):
    """A dome's centre position as the simulation places it: its footprint's lower edge at the top edge at its enter
    time (step D4; the aprons and the review's dome sprites)."""
    return top_pos(t) + size / 2


def targets():
    """{name: [(t, x), ...]} of the data's ground targets (the target's top edge at the top edge at t)."""
    out = {}
    for g in DATA.get("ground_targets") or []:
        out[g["target"]] = [tuple(map(float, a)) for a in g["at"]]
    return out or {"embankment nest": [(18, 290), (18.4, 330), (18.8, 290), (19.2, 330)],
                   "outer floe": [(64, 200), (64.5, 250), (65, 220)], "seed vault": [(76, 70)],
                   "rail yard nest": [(90, 80), (90.4, 130), (90.8, 100)],
                   "inner floe": [(117, 180), (117.5, 230), (118, 200)],
                   "apron nest": [(150, 160), (150.4, 200), (150.8, 280), (151.2, 320)]}


def target_centre(name, size=32):
    spots = targets()[name]
    pos = sum(top_pos(t) - size / 2 for t, _ in spots) / len(spots)
    x = sum(x for _, x in spots) / len(spots)
    return pos, x


# =========================================================================== shared

def finish(rgb, colours, alpha=None, amount=0.2):
    return posterize_rgb(recede(rgb, amount), colours, alpha)


def finish_piece(hgt, alb, cover, f=2, colours=32, emission=None, wrap=False, full_height=False,
                 ambient=0.42, key=0.7):
    """backdrop_l15.finish_piece, optionally keeping the top and bottom rows (a piece that tiles along
    itself, the rail)."""
    a = k8.relief(hgt, alb, np.zeros_like(alb) if emission is None else emission, np.ones(hgt.shape), f,
                  wrap=wrap, ambient=ambient, key=key, ao=0.11, gradient=0.06)
    c = np.clip(cover, 0, 1)
    if not full_height:
        c[:f], c[-f:] = 0, 0
    c[:, :f], c[:, -f:] = 0, 0
    a[..., 3] = c
    nat = k8.reduce(a, f)
    nat[..., 3] = np.clip((nat[..., 3] - 0.5) * 2.5 + 0.5, 0, 1)
    rgb = recede(nat[..., :3] * 255, 0.18)
    alpha = dither_alpha(nat[..., 3], 2)
    img = posterize_rgb(rgb, colours, alpha)
    arr = np.array(img)
    arr[arr[..., 3] == 0] = 0
    return Image.fromarray(arr, "RGBA")


def sdf_piece(scene, mats, size, z_top=40.0, f=4, scale=1.0, shadow=(5, 6), shadow_alpha=0.45, colours=40):
    """An SDF model (1 unit = 1 px at ``scale`` 1) rendered top-down for a ground piece, with its shadow cast
    to the lower right on the ground (dithered), posterized with the lit pixels kept."""
    w, h = size
    hi = sdf.render(scene, mats, (w * f, h * f), float(w) / scale, z_top=z_top, steps=140)
    body = artkit.native(hi, f, crisp=60)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    if shadow:
        sh = sprite.shadow_of(body, opacity=1.0, blur=1.0)
        a = np.array(sh)
        a[..., 3] = (np.array(dither_alpha(a[..., 3] / 255 * shadow_alpha * 1.6, 3)) * shadow_alpha).astype(np.uint8)
        img.alpha_composite(Image.fromarray(a, "RGBA"), shadow)
    img.alpha_composite(body)
    return k8.quantize_lit([img], colours)[0]


def rect_mats():
    off = Material((0.8, 0.78, 0.72), metal=0.25, shininess=50, spec=0.5, pattern=sdf.panel_lines(0.12, 0.05, 0.85))
    amber = Material((0.86, 0.52, 0.16), metal=0.3, shininess=50, spec=0.5)
    dark = Material((0.18, 0.18, 0.2), metal=0.4, shininess=40, spec=0.5)
    steel = Material((0.55, 0.56, 0.6), metal=0.7, shininess=90, spec=0.9)
    lamp = Material((0.1, 0.1, 0.1), emission=(1.6, 1.2, 0.5))
    return [off, amber, dark, steel, lamp]


# =========================================================================== tile sets

def plain_base(seed, w=W, h=TILE_H):
    rh, ralb = b15.regolith(w, h, seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    warp = pn(w, h, 48, seed + 1, 3)
    ripple = np.sin(xx * 0.05 + 2 * np.pi * 30 * yy / h + 7 * warp) * (0.2 + 0.4 * pn(w, h, 96, seed + 2, 2))
    craters, ej = b15.crater_field(w, h, 12, 2.5, 11.0, seed + 3, depth=0.9)
    drift = smooth(0.55, 0.75, aniso(w, h, 1, 4, 24, seed + 4, 3))
    plain = ramp(m36.PLAIN, np.clip(0.55 + 0.35 * (pn(w, h, 160, seed + 5, 3) - 0.5) + 0.25 * drift, 0, 1))
    alb = ralb * 0.3 + plain * 0.7 + 12 * ej[..., None] + 18 * drift[..., None]
    hgt = rh + ripple + craters + 1.2 * drift
    return hgt, alb, yy, xx


def ares_plain():
    """Section 1: the rust plain north of the colony: wind ripples, pale dust drifts streaked along the
    wind, small craters, scattered rocks."""
    hgt, alb, _, _ = plain_base(1701)
    rocks = (np.random.default_rng(1702).random(hgt.shape) > 0.994) * 2.4
    rocks = np.maximum(rocks, np.roll(rocks, 1, 1) * 0.7)
    return finish(lit(hgt + rocks, alb), 32)


def colony_ground():
    """Sections 2 and 4: the colony's worked ground: the plain with rover tracks (paired ruts), cable
    trenches with their marker posts, pale concrete dust round the works, fewer craters."""
    hgt, alb, yy, xx = plain_base(1711)
    for path in b15.periodic_paths(TILE_H, 3, 1713, 40, 440, 60, cycles=(1, 2)):
        d = xx - path[yy.astype(int) % TILE_H]
        for off in (-5.0, 5.0):
            rut = np.exp(-((d - off) / 1.4) ** 2)
            hgt -= 0.9 * rut
            alb = alb * (1 - 0.18 * rut[..., None])
    for path in b15.periodic_paths(TILE_H, 2, 1715, 80, 400, 30, cycles=(1,)):
        d = np.abs(xx - path[yy.astype(int) % TILE_H])
        trench = np.clip(1 - d / 2.5, 0, 1)
        hgt -= 1.2 * trench
        alb = alb * (1 - 0.3 * trench[..., None]) + np.array((120, 110, 100)) * 0.3 * trench[..., None]
        post = (d < 2) & ((yy % 48) < 2)
        hgt += 2.0 * post
        alb = np.where(post[..., None], np.array((200, 190, 170), float), alb)
    dust = smooth(0.6, 0.8, pn(W, TILE_H, 96, 1717, 3))
    alb = alb * (1 - 0.25 * dust[..., None]) + np.array((176, 160, 140)) * 0.25 * dust[..., None]
    return finish(lit(hgt, alb), 32)


def burnt_fields():
    """Section 3: the burnt outer greenhouse fields: the plain blackened in wide scars, grey ash streaked
    along the wind, soot rings round burnt-out craters, cinders."""
    hgt, alb, yy, xx = plain_base(1721)
    scar = smooth(0.45, 0.62, pn(W, TILE_H, 160, 1723, 4))
    ash = smooth(0.6, 0.72, aniso(W, TILE_H, 1, 5, 16, 1725, 3)) * scar
    soot = np.array((38, 30, 28), float)
    alb = alb * (1 - 0.78 * scar[..., None]) + soot * 0.78 * scar[..., None]
    alb = alb * (1 - 0.5 * ash[..., None]) + np.array((128, 120, 112)) * 0.5 * ash[..., None]
    cind = (np.random.default_rng(1727).random(hgt.shape) > 0.993) & (scar > 0.5)
    hgt = hgt + 1.4 * cind
    alb = np.where(cind[..., None], np.array((24, 20, 20), float), alb)
    return finish(lit(hgt, alb), 32)


SLAB = 48


def spaceport_apron():
    """Section 5: the spaceport's apron: 48 px concrete slabs, recessed joints, each slab its own tone,
    oil stains and tyre marks, red dust drifted in from the left, a dashed yellow taxi line along x 240 and
    white guide lines."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rng = np.random.default_rng(1731)
    tone = rng.uniform(-0.06, 0.06, (h // SLAB, w // SLAB))
    # step D4: the slab grid and the taxi dashes shifted off the tile's wrap (BackdropSeamsTest: no seam
    # where it repeats)
    sy = yy + SLAB // 2
    t = tone[((sy // SLAB) % (h // SLAB)).astype(int), (xx // SLAB).astype(int)]
    n = pn(w, h, 16, 1733, 3)
    alb = np.array((168, 160, 150), float) * (0.95 + t + 0.08 * (n - 0.5))[..., None]
    joint = ((sy % SLAB) < 1.2) | ((xx % SLAB) < 1.2)
    hgt = -0.8 * joint + 0.3 * n
    alb = np.where(joint[..., None], alb * 0.62, alb)
    stain = smooth(0.7, 0.76, pn(w, h, 24, 1735, 3))
    alb = alb * (1 - 0.16 * stain[..., None])
    dust = smooth(0.35, 0.9, pn(w, h, 96, 1737, 3)) * np.clip(1 - xx / 300, 0, 1)
    alb = alb * (1 - 0.45 * dust[..., None]) + np.array((176, 110, 70)) * 0.45 * dust[..., None]
    taxi = (np.abs(xx - 240) < 1.6) & (((yy + 7) % 32) < 18)
    alb = np.where(taxi[..., None], np.array((214, 172, 60), float), alb)
    guide = (np.abs(xx - 96) < 1.0) | (np.abs(xx - 384) < 1.0)
    alb = np.where(guide[..., None], np.array((210, 206, 196), float), alb)
    return finish(lit(hgt, alb, ambient=0.5, key=0.6), 32, amount=0.16)


def dust_wisps():
    return b15.cloud_bank(W, TILE_H, 1741, 0.68, 0.14, 76, colours=DUST[1:])


def smoke_banks():
    return b15.cloud_bank(W, TILE_H, 1743, 0.55, 0.18, 115,
                          colours=[(34, 30, 30), (58, 52, 50), (86, 80, 76), (120, 112, 106)])


# =========================================================================== seams

FEATHER = 32
SEAM_H = 320
SEAMS = {"seam-domes": (1, "ares-plain", "colony-ground", 1751), "seam-burnt": (2, "colony-ground", "burnt-fields", 1753),
         "seam-inner": (3, "burnt-fields", "colony-ground", 1755), "seam-apron": (4, "colony-ground", "spaceport-apron", 1757)}


def tile_strip(name, bottom, h):
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def seam_piece(name):
    """backdrop_l16.blend on this level's tile sets: the lower section's tiles below a ragged boundary at
    the seam, the upper's above, the ends faded into the tile sets (ordered-dithered 1-bit alpha)."""
    s, lower, upper, seed = SEAMS[name]
    g = seam("ground", s)
    bottom, h = g - SEAM_H // 2, SEAM_H
    up = tile_strip(upper, bottom, h)
    lo = tile_strip(lower, bottom, h)
    rows = bottom + h - 1 - np.arange(h)[:, None]
    xs = np.arange(W)[None, :]
    rng = np.random.default_rng(seed)
    wob = sum(a * np.sin(2 * np.pi * c * xs / W + rng.uniform(0, 6.3)) for c, a in zip((1, 2, 5, 11), (14, 9, 5, 3)))
    edge = g + wob + (np.random.default_rng(seed + 1).random((1, W)) - 0.5) * 3
    out = np.where((rows >= edge)[..., None], up, lo)
    d = rows - edge
    r = (d >= 0) & (d < 2)
    out[..., :3] = np.where(r[..., None], out[..., :3] * 0.75, out[..., :3])
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where(alpha > 0, 255, 0)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


# =========================================================================== the rail line

RAIL_P = 320                       # the rail's texture period along the line (sleepers every 8 px)
RAIL_HALF = 48
BEND_H = 640


def rail_layout():
    """The line in ground positions: straight at x 310 from the level's start (section 1, the embankment
    nest's x 290-330), bending to x 240 between the outer domes before Bradbury, to x 160 after Lowell, on
    to the rail yard in section 3 where it ends. Every break on a multiple of RAIL_P, so the straight tiles
    meet the bends exactly. Returns (b1, b2, y0): the bends' bottoms and the yard's bottom."""
    ds = {n: (t, x, s) for n, t, x, s in domes()}
    brad = dome_pos(ds["Bradbury"][0], 120) - 60 - 120
    b1 = int((brad - BEND_H) // RAIL_P) * RAIL_P
    low = dome_pos(ds["Lowell"][0], 120) + 60 + 80
    b2 = int(math.ceil(low / RAIL_P)) * RAIL_P
    yard_pos, _ = target_centre("rail yard nest")
    y0 = int((yard_pos - 480) // RAIL_P) * RAIL_P
    return b1, b2, y0


def rail_x(pos):
    b1, b2, y0 = rail_layout()
    pos = np.asarray(pos, float)

    def bend(a, x0, x1):
        u = np.clip((pos - a) / BEND_H, 0, 1)
        return (x1 - x0) * (u * u * (3 - 2 * u))
    return 310 + bend(b1, 310, 240) + bend(b2, 240, 160)


_rail_noise = {}


def rail_noise(f):
    if f not in _rail_noise:
        _rail_noise[f] = pn(2 * RAIL_HALF * f, RAIL_P * f, 16, 1761, 3)
    return _rail_noise[f]


def rail_fields(dx, pos, f, end=None):
    """Height, albedo and cover of the line at offsets dx from its centre and ground positions pos (both
    at f x): the embankment, ballast, sleepers, two rails, the cable trough on its west side; past ``end``
    (the buffer stops) the sleepers and rails stop."""
    n = sample(rail_noise(f), (pos % RAIL_P) * f, (dx + RAIL_HALF) * f)
    emb = np.clip(1 - (np.abs(dx) - 14) / 24 + 0.4 * (n - 0.5), 0, 1)
    hgt = 3.0 * emb + 0.4 * n
    on = np.ones(dx.shape, bool) if end is None else pos < end
    sleepers = (np.abs(dx) < 11) & ((pos % 8) < 3) & on
    rails = (np.abs(np.abs(dx) - 6) < 1.0) & on
    trough = np.abs(dx + 19) < 2.0
    hgt = hgt + 0.9 * sleepers + 1.4 * rails + 0.8 * trough
    alb = np.zeros(dx.shape + (3,)) + np.array((150, 104, 74))
    alb = np.where((np.abs(dx) < 14)[..., None], np.array((112, 100, 92)) * (0.9 + 0.2 * n[..., None]), alb)
    alb = np.where(sleepers[..., None], np.array((86, 70, 60)), alb)
    alb = np.where(rails[..., None], np.array((150, 150, 156)), alb)
    alb = np.where(trough[..., None], np.array((120, 116, 110)), alb)
    cover = np.clip(emb * 1.4, 0, 1)
    return hgt, alb, cover


RAIL_PAD = 1   # step D4: a rail piece is RAIL_PAD rows taller at either end, those rows transparent, so it
#               overlaps its neighbours and no opaque edge row shows (BackdropAssetsTest's edge rule)


def rail_piece(bottom, h, x0, w, end=None, extra=None):
    """A rail piece covering ground positions bottom .. bottom + h and x0 .. x0 + w, drawn RAIL_PAD rows
    beyond either end with those outer rows left transparent (its centre and so its placement unchanged)."""
    f = 2
    bottom, h = bottom - RAIL_PAD, h + 2 * RAIL_PAD
    yy, xx = b15.piece_canvas((w, h), f)
    pos = bottom + h - yy
    x = x0 + xx
    dx = x - rail_x(pos)
    hgt, alb, cover = rail_fields(dx, pos, f, end)
    if extra is not None:
        hgt, alb, cover = extra(x, pos, hgt, alb, cover)
    img = np.array(finish_piece(hgt, alb, cover, f, full_height=True, wrap=False))
    img[:RAIL_PAD] = 0
    img[-RAIL_PAD:] = 0
    return Image.fromarray(img, "RGBA")


def clear_edges(img, n=8):
    """step D4: the piece's alpha faded to nothing over its outer ``n`` px (1-bit dithered), so no opaque
    edge shows over the opaque ground (an apron's road stub, the compound's fence, a gun pit's sandbags)."""
    arr = np.array(img)
    h, w = arr.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w]
    d = np.minimum(np.minimum(xx, w - 1 - xx), np.minimum(yy, h - 1 - yy)).astype(float)
    ramp = np.clip(d / n, 0, 1)
    a = np.asarray(dither_alpha(arr[..., 3] / 255.0 * ramp, 2)).astype(np.uint8)
    keep = ramp >= 1
    arr[..., 3] = np.where(keep, arr[..., 3], a)
    arr[arr[..., 3] == 0] = 0
    return Image.fromarray(arr, "RGBA")


def rail_track():
    b1, _, _ = rail_layout()
    return rail_piece(0, RAIL_P, 310 - RAIL_HALF, 2 * RAIL_HALF)


BEND_W = 192


def rail_bend(which):
    b1, b2, _ = rail_layout()
    bottom, xc = (b1, 275) if which == "a" else (b2, 200)
    return rail_piece(bottom, BEND_H, xc - BEND_W // 2, BEND_W)


YARD = (240, 960)


def rail_yard():
    """The rail yard in section 3's left half: the line splitting into three sidings with their points, two
    stranded wagons, a loading platform, the buffer stops where the line ends; the nest's turrets stand
    between the sidings (x 80-130)."""
    _, _, y0 = rail_layout()
    w, h = YARD
    x0 = 40
    end = y0 + h - 140
    sidings = [(-70, y0 + 140), (70, y0 + 220)]

    def extra(x, pos, hgt, alb, cover):
        for off, start in sidings:
            u = np.clip((pos - start) / 200, 0, 1)
            xc = 160 + off * (u * u * (3 - 2 * u))
            h2, a2, c2 = rail_fields(x - xc, pos, 2, end - 40 * (off > 0))
            m = (c2 > cover) & (pos > start)
            hgt = np.where(m, h2, np.maximum(hgt, h2 * (pos > start)))
            alb = np.where(m[..., None], a2, alb)
            cover = np.maximum(cover, c2 * (pos > start))
        for xc in (90, 160, 230):                     # buffer stops
            stop = (np.abs(x - xc) < 9) & (np.abs(pos - end + 4) < 3)
            hgt = np.where(stop, 4.0, hgt)
            alb = np.where(stop[..., None], np.array((190, 60, 40), float), alb)
        plat = (np.abs(x - 120) < 10) & (pos > y0 + 380) & (pos < y0 + 640)
        hgt = np.where(plat, 3.5, hgt)
        alb = np.where(plat[..., None], np.array((150, 146, 138), float), alb)
        cover = np.maximum(cover, plat * 1.0)
        # step D4: past the buffer stops the embankment fades out before the piece's top edge
        cover = cover * np.clip((y0 + h - 8 - pos) / 60, 0, 1)
        return hgt, alb, cover
    img = rail_piece(y0, h, x0, w, end=end, extra=extra)
    for xc, pc, n in ((90, y0 + 520, 2), (230, y0 + 300, 1)):      # wagons on the sidings
        wag = wagon_train(n, burnt=xc == 90)
        img.alpha_composite(wag, (int(xc - x0 - wag.width / 2), int(y0 + h - pc - wag.height / 2)))
    return img


def wagon_scene(n, loco=False, burnt=False):
    mats = rect_mats()
    if burnt:
        mats[0] = Material((0.3, 0.28, 0.26), metal=0.2, shininess=20, spec=0.2)
    L = 34.0

    def scene(p):
        items = []
        for k in range(n):
            y = (k - (n - 1) / 2) * (L + 4)
            if loco and k == 0:
                items += [(sd_box(p, (0, y, 6), (8, L / 2, 5), 2.0), 0), (sd_box(p, (0, y + L / 2 - 6, 11.5), (6, 4, 1.2), 0.8), 2),
                          (sd_box(p, (0, y - 4, 11.5), (5, 8, 0.8), 0.5), 3), (sd_box(p, (0, y + L / 2 - 1, 7), (7.5, 0.8, 1.2)), 1)]
            else:
                items += [(sd_box(p, (0, y, 6), (8, L / 2, 5), 1.2), 0),
                          (sd_box(p, (0, y, 11.2), (8.2, L / 2 - 3, 0.4)), 1 if k % 2 else 0),
                          (sd_box(p, (0, y + L / 4, 11.4), (5, 2.5, 0.6)), 2)]
            if k:
                items.append((sd_box(p, (0, y + L / 2 + 2, 3), (1.2, 2.2, 1.0)), 3))
        return union(*items)
    return scene, mats


def wagon_train(n, burnt=False, loco=False):
    scene, mats = wagon_scene(n, loco, burnt)
    h = int(n * 38 + 16)
    return sdf_piece(scene, mats, (32, h), z_top=30, shadow=(3, 4), colours=24)


def freight_train():
    """The stranded freight train on the line in section 1: a locomotive and five wagons, the colony's
    off-white with amber bands, two wagons scorched and one burst open."""
    return wagon_train(6, loco=True)


# =========================================================================== the domes' aprons

def apron(side, size, scale):
    """A dome's concrete apron (the ring sits on it), its greenhouse annexes on ``side`` (west, east or
    both), the road stub out the other way toward the rail; the annexes' glass is the dome's (mars_r36)."""
    _, mats, _ = m36.dome_a(R=46 * scale, annex=False)
    glass = mats[0]
    conc = Material((0.56, 0.52, 0.47), metal=0.05, shininess=20, spec=0.15,
                    pattern=lambda p, n: np.where((np.abs(np.arctan2(p[:, 1], p[:, 0]) * 6 / np.pi % 1 - 0.5) > 0.47)
                                                  | (np.abs(np.hypot(p[:, 0], p[:, 1]) - 66 * scale) < 0.7), 0.8, 1.0))
    road = Material((0.36, 0.33, 0.3), metal=0.0, shininess=10, spec=0.1,
                    pattern=lambda p, n: np.where((np.abs(p[:, 1]) < 0.6) & (p[:, 0] % 8 < 4), 2.0, 1.0))
    strut = Material((0.86, 0.88, 0.86), metal=0.3, shininess=60, spec=0.6)
    R = 46 * scale
    Rr = R + 9
    angles = {"west": (2.55, 3.75), "east": (5.65, 0.6), "both": (2.75, 5.9)}[side]
    road_dir = {"west": 0.0, "east": math.pi, "both": None}[side]

    def scene(p):
        items = [(sd_cylinder_z(p, (0, 0, 0), Rr + 10, 0.8), 1)]
        for k, ang in enumerate(angles):
            ln = (30 if k == 0 else 24) * scale
            a0 = (math.cos(ang) * (Rr - 2), math.sin(ang) * (Rr - 2), 0.8)
            a1 = (math.cos(ang) * (Rr + ln), math.sin(ang) * (Rr + ln), 0.8)
            tube = np.maximum(sd_capsule(p, a0, a1, 7.5 * scale), -(p[:, 2] - 0.8))
            items.append((tube, 0))
            for j in range(1, 6):                      # the annex's ribs
                c = [a0[i] + (a1[i] - a0[i]) * j / 6 for i in range(3)]
                q = rotate_z(p - np.array(c), -ang)
                rib = np.maximum(np.abs(np.hypot(q[:, 1], q[:, 2]) - 7.6 * scale) - 0.5, np.abs(q[:, 0]) - 0.6)
                items.append((np.maximum(rib, -(p[:, 2] - 0.8)), 3))
        if road_dir is not None:
            q = rotate_z(p, -road_dir)
            items.append((sd_box(q, (Rr + 26, 0, 0.4), (24, 6, 0.4)), 2))
        return union(*items)
    n = int(size)
    return sdf_piece(scene, [glass, conc, road, strut], (n, n), z_top=30, shadow=(3, 4), colours=40)


# =========================================================================== Vrell growth

def vrell_growth(size, seed, density=0.5):
    """Vrell nest growth: lumpy violet-slate tissue in rounded lobes, glowing teal veins running through it
    (the mood still's colours), its edge thinning into the ground in fingers."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    n = raster.fbm(w * f, h * f, 40, seed, octaves=3, period=False)
    lump = raster.fbm(w * f, h * f, 8, seed + 1, octaves=2, period=False)
    ridge = raster.fbm(w * f, h * f, 24, seed + 2, octaves=3, period=False)
    fall = np.clip(1 - np.hypot((xx - w / 2) / (w / 2), (yy - h / 2) / (h / 2)), 0, 1)
    g = smooth(0.5 - 0.3 * density, 0.58 - 0.3 * density, n * 0.55 + fall * 0.75)
    vein = (np.abs(ridge - 0.5) < 0.022) & (g > 0.6)
    hgt = 2.6 * g + 1.4 * g * lump - 0.6 * vein
    alb = np.array((66, 34, 78), float) * (0.75 + 0.5 * lump[..., None]) * np.ones(yy.shape + (1,))
    alb = np.where(vein[..., None], np.array((30, 150, 110), float), alb)
    emission = np.where(vein[..., None], TEAL / 255 * 0.85, 0.0) * np.ones(yy.shape + (1,))
    return finish_piece(hgt, alb, g, f, colours=24, emission=emission)


# =========================================================================== section 3

def burnt_greenhouses():
    """Three burnt tube greenhouses side by side: the frames fallen in, ribs black and bent, the glass gone
    but for a few shards, soot spread round them on the ground."""
    rng = np.random.default_rng(1771)
    rib = Material((0.24, 0.22, 0.21), metal=0.3, shininess=30, spec=0.3)
    pale = Material((0.36, 0.34, 0.32), metal=0.1, shininess=20, spec=0.2)
    pad = Material((0.3, 0.27, 0.25), metal=0.0, shininess=10, spec=0.1,
                   pattern=lambda p, n: 0.85 + 0.15 * np.sin(p[:, 0] * 0.4 + np.sin(p[:, 1] * 0.13) * 3))
    shard = Material((0.42, 0.56, 0.52), metal=0.4, shininess=120, spec=1.0)
    tubes = [(-60, 0.9), (0, 0.55), (60, 0.25)]
    hoops = [(x, y, rng.uniform(0.2, 1.0) * s, rng.random() < 0.18) for x, s in tubes for y in range(-100, 104, 12)]
    shards = [(rng.uniform(-80, 80), rng.uniform(-110, 110), rng.uniform(0, 6.3)) for _ in range(16)]

    def scene(p):
        items = []
        for x, y, sag, gone in hoops:
            if gone:
                continue
            q = p - np.array([x, y, 0.0])
            hoop = np.maximum(np.abs(np.hypot(q[:, 0], q[:, 2] / max(sag, 0.15)) - 18) - 1.0, np.abs(q[:, 1]) - 1.0)
            items.append((np.maximum(hoop * max(sag, 0.15), -(p[:, 2] - 1.0)), 0))
        for x, s in tubes:
            items.append((sd_capsule(p, (x - 4, -104, 1 + 16 * s), (x + 3, 104, 1 + 14 * s), 1.2), 0))
            items.append((sd_box(p, (x, 0, 1.2), (19, 106, 0.4)), 1))
        for x, y, a in shards:
            q = rotate_z(p - np.array([x, y, 1.6]), a)
            items.append((sd_box(q, (0, 0, 0), (3.5, 2.0, 0.3)), 3))
        return union(*items)
    return sdf_piece(scene, [rib, pale, pad, shard], (200, 248), z_top=30, shadow=(3, 4), colours=32)


HELIX_K = 1.25


def helix_compound():
    """The Helix compound: mars_r36.helix_site unchanged, rendered at x1.25 and turned with its long side
    along the flight as Level 16's station (backdrop_l16.helix_station), on a clean, raked gravel yard
    inside a white fence with a gate to the west, two lamp masts with warm pools, no growth, no scorch;
    black and gold, its skylights, windows and mast lit, unlabelled."""
    orig = m36.sdf_sprite

    def turned(scene, mats, w, h, factor=4, colors=40, z_top=None, crisp=60):
        rot = (lambda p: scene(rotate_z(p, -math.pi / 2)))
        hi = m36.sdf.render(rot, mats, (int(h * HELIX_K) * factor, int(w * HELIX_K) * factor), float(h),
                            z_top=z_top or max(w, h), steps=160)
        return m36.sprite.make_sprite(hi, factor, colors, crisp=crisp)
    m36.sdf_sprite = turned
    try:
        st = m36.helix_site(116, 88)
    finally:
        m36.sdf_sprite = orig
    w, h = 184, 264
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    fx0, fx1, fy0, fy1 = 12, w - 12, 14, h - 14
    inside = (xx >= fx0) & (xx <= fx1) & (yy >= fy0) & (yy <= fy1)
    n = raster.fbm(w, h, 8, 1781, octaves=3, period=False)
    rake = 0.5 + 0.5 * np.sin(yy * 1.6)
    gravel = np.array((128, 120, 110), float) * (0.92 + 0.08 * n + 0.04 * rake)[..., None]
    clean = np.clip(1 - np.maximum(np.maximum(fx0 - xx, xx - fx1), np.maximum(fy0 - yy, yy - fy1)) / 12, 0, 1)
    ground = np.array((176, 120, 82), float) * (0.9 + 0.15 * n)[..., None]     # the clean plain round the fence
    rgb = np.where(inside[..., None], gravel, ground)
    for lx, ly in ((fx0 + 18, fy0 + 20), (fx1 - 18, fy1 - 22)):                  # the lamps' warm pools
        pool = np.exp(-((xx - lx) ** 2 + (yy - ly) ** 2) / 900)
        rgb = rgb + np.array((60, 40, 14)) * pool[..., None]
    rgb = recede(rgb, 0.14)
    a = dither_alpha(np.where(inside, 1.0, clean * 0.999), 2)
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), a]).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    for x in range(fx0, fx1 + 1, 6):                   # the fence: white posts, a lit top wire
        for y in (fy0, fy1):
            if not (y == fy1 and abs(x - w / 2) < 16):
                d.point((x, y), fill=(236, 232, 222, 255))
    for y in range(fy0, fy1 + 1, 6):
        for x in (fx0, fx1):
            if not (x == fx0 and abs(y - h / 2) < 14):
                d.point((x, y), fill=(236, 232, 222, 255))
    d.line([(fx0 + 1, fy0), (fx1 - 1, fy0)], fill=(206, 200, 188, 255))
    d.line([(fx0, h / 2 - 14), (0, h / 2 - 14)], fill=(120, 112, 104, 255))
    d.line([(fx0, h / 2 + 14), (0, h / 2 + 14)], fill=(120, 112, 104, 255))
    sh = sprite.shadow_of(st, opacity=0.5, blur=1.2)
    img.alpha_composite(sh, (int(w / 2 - st.width / 2) + 5, int(h / 2 - st.height / 2) + 6))
    img.alpha_composite(st, (int(w / 2 - st.width / 2), int(h / 2 - st.height / 2)))
    for lx, ly in ((fx0 + 18, fy0 + 20), (fx1 - 18, fy1 - 22)):
        d.ellipse([lx - 2, ly - 2, lx + 2, ly + 2], fill=(20, 20, 22, 255))
        d.point((lx, ly), fill=(255, 226, 150, 255))
    return k8.quantize_lit([img], 48)[0]


# =========================================================================== section 4 and 5

def ring_road(sign):
    """An arc of the inner ring road across the field: compacted grey regolith, kerbs, a dashed white
    centre line; ``sign`` +1 bows toward the top (entering the ring), -1 toward the bottom (leaving it)."""
    w, h = 480, 160
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    yc = h / 2 + sign * (-28 + 56 * ((xx - 240) / 240) ** 2)
    d = yy - yc
    n = raster.fbm(w * f, h * f, 16, 1791 + sign, octaves=3, period=False)
    road = np.abs(d) < 13
    kerb = np.abs(np.abs(d) - 13.5) < 1.2
    line = (np.abs(d) < 0.8) & ((xx % 20) < 11)
    hgt = 0.5 * road + 1.0 * kerb + 0.3 * n
    alb = np.zeros(yy.shape + (3,)) + np.array((150, 104, 74))
    alb = np.where(road[..., None], np.array((96, 92, 88)) * (0.9 + 0.2 * n[..., None]), alb)
    alb = np.where(kerb[..., None], np.array((170, 164, 154), float), alb)
    alb = np.where(line[..., None], np.array((214, 210, 200), float), alb)
    cover = np.clip(1 - (np.abs(d) - 15) / 5 + 0.4 * (n - 0.5), 0, 1)
    return finish_piece(hgt, alb, cover, f, colours=24)


def gun_pit():
    """An unmanned militia flak pit: a ring of sandbags round a twin-barrel gun on its mount, barrels raised
    toward the north-east, ammunition boxes."""
    bag = Material((0.62, 0.54, 0.42), metal=0.0, shininess=12, spec=0.1,
                   pattern=lambda p, n: 0.8 + 0.2 * np.cos(np.arctan2(p[:, 1], p[:, 0]) * 18) ** 2)
    floor = Material((0.36, 0.3, 0.26), metal=0.0, shininess=10, spec=0.1)
    gun = Material((0.4, 0.42, 0.38), metal=0.6, shininess=70, spec=0.8)
    box = Material((0.42, 0.44, 0.3), metal=0.2, shininess=30, spec=0.3)

    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1])
        ring = np.hypot(r - 20, p[:, 2] - 2.5) - 4.0
        items = [(np.maximum(ring, -p[:, 2]), 0), (sd_cylinder_z(p, (0, 0, 0), 18, 0.4), 1),
                 (sd_cylinder_z(p, (0, 0, 2), 5, 2), 2), (sd_box(p, (0, 0, 5), (4, 3, 1.5), 0.8), 2)]
        for s in (-1.6, 1.6):
            a = (s, 0, 5.5)
            b = (s + 10, 10, 8.5)
            items.append((sd_capsule(p, a, b, 0.8), 2))
        items += [(sd_box(p, (-9, 8, 1.5), (3, 2, 1.5), 0.4), 3), (sd_box(p, (-11, 2, 1.5), (2, 3, 1.5), 0.4), 3)]
        return union(*items)
    return sdf_piece(scene, [bag, floor, gun, box], (56, 56), z_top=20, f=8, shadow=(2, 3), colours=24)


def landing_pad():
    """A spaceport landing pad: a raised concrete disc, a yellow ring and cross, blast scorch in the middle,
    amber edge lights."""
    w = h = 128
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    r = np.hypot(xx - w / 2, yy - h / 2)
    n = raster.fbm(w * f, h * f, 16, 1801, octaves=3, period=False)
    disc = r < 58
    hgt = 1.2 * disc + 0.2 * n
    alb = np.zeros(yy.shape + (3,)) + np.array((168, 160, 150)) * (0.94 + 0.1 * n[..., None])
    ring = np.abs(r - 46) < 2.0
    cross = ((np.abs(xx - w / 2) < 2.5) | (np.abs(yy - h / 2) < 2.5)) & (r < 30)
    alb = np.where((ring | cross)[..., None], np.array((214, 172, 60), float), alb)
    scorch = np.clip(1 - r / 34 + 0.3 * (n - 0.5), 0, 1)
    alb = alb * (1 - 0.6 * scorch[..., None])
    lights = np.zeros(r.shape, bool)
    for k in range(12):
        a = 2 * math.pi * k / 12
        lights |= np.hypot(xx - w / 2 - 54 * math.cos(a), yy - h / 2 - 54 * math.sin(a)) < 1.6
    alb = np.where(lights[..., None], np.array((255, 190, 90), float), alb)
    emission = np.where(lights[..., None], np.array((1.0, 0.7, 0.3)), 0.0) * np.ones(yy.shape + (1,))
    cover = np.clip(1 - (r - 58) / 3, 0, 1)
    return finish_piece(hgt, alb, cover, f, colours=28, emission=emission)


def hangar():
    """A spaceport hangar: a long arched roof (the colony's off-white, ribbed), its big doors facing the
    apron (east), a lit door gap, a small annex."""
    mats = rect_mats()

    def scene(p):
        roof = np.maximum(sdf.sd_cylinder_y(p, (0, 0, -8), 32, 50), -p[:, 2])
        items = [(roof, 0)]
        for y in range(-44, 48, 11):
            q = p - np.array([0, y, -8.0])
            rib = np.maximum(np.abs(np.hypot(q[:, 0], q[:, 2]) - 32.3) - 0.8, np.abs(q[:, 1]) - 1.0)
            items.append((np.maximum(rib, -p[:, 2]), 3))
        items += [(sd_box(p, (31, 0, 7), (1.5, 40, 7)), 2), (sd_box(p, (32.2, 0, 4), (0.6, 4, 4)), 4),
                  (sd_box(p, (-12, 56, 5), (12, 6, 5), 1.0), 0), (sd_box(p, (-12, 56, 10.3), (12.2, 6.2, 0.4)), 1)]
        return union(*items)
    return sdf_piece(scene, mats, (120, 152), z_top=40, shadow=(5, 6), colours=32)


def smoke_plume():
    w = h = 96
    n = raster.fbm(w, h, 12, 1811, octaves=4, period=False)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    r = np.hypot((xx - w / 2) / (w / 2), (yy - h / 2 - 10) / (h / 2))
    body = np.clip(1 - r, 0, 1) * (0.4 + 0.9 * n)
    lit_ = np.clip(0.5 + 5 * (body - np.roll(np.roll(body, 2, 0), 2, 1)), 0, 1)
    shades = np.array([(30, 26, 26), (54, 48, 46), (82, 76, 72), (112, 106, 100)], float)
    rgb = shades[np.clip((lit_ * 0.7 + body * 0.4) * 3.99, 0, 3).astype(int)]
    alpha = artkit.ordered_dither(np.clip((body - 0.15) / 0.5, 0, 1) * 0.45, 5)
    out = np.dstack([rgb, alpha * 255]).astype(np.uint8)
    out[alpha == 0] = 0
    return Image.fromarray(out, "RGBA")


# =========================================================================== registry

GENERATORS = {
    "ares-plain": ares_plain, "colony-ground": colony_ground, "burnt-fields": burnt_fields,
    "spaceport-apron": spaceport_apron, "dust-wisps": dust_wisps, "smoke-banks": smoke_banks,
    "rail-track": rail_track, "rail-bend-a": lambda: rail_bend("a"), "rail-bend-b": lambda: rail_bend("b"),
    "rail-yard": rail_yard, "freight-train": freight_train,
    "dome-apron-west": lambda: clear_edges(apron("west", 184, 1.0)),
    "dome-apron-east": lambda: clear_edges(apron("east", 184, 1.0)),
    "dome-apron-eden": lambda: clear_edges(apron("both", 216, 1.2)),
    "vrell-growth-a": lambda: vrell_growth((160, 144), 1821, 0.5),
    "vrell-growth-b": lambda: vrell_growth((224, 184), 1823, 0.55),
    "burnt-greenhouses": burnt_greenhouses, "helix-compound": lambda: clear_edges(helix_compound(), 3),
    "ring-road-north": lambda: ring_road(1), "ring-road-south": lambda: ring_road(-1),
    "gun-pit": lambda: clear_edges(gun_pit(), 3), "landing-pad": landing_pad, "hangar": hangar, "smoke-plume": smoke_plume,
}
SECOND = {name: (lambda n=name: seam_piece(n)) for name in SEAMS}
TILE_SETS = {   # id -> (layer, drift, note)
    "ares-plain": ("ground", None, "section 1: the rust plain north of the colony, wind ripples, dust drifts, small craters"),
    "colony-ground": ("ground", None, "sections 2 and 4: the colony's worked ground, rover tracks, cable trenches, concrete dust"),
    "burnt-fields": ("ground", None, "section 3: the burnt outer greenhouse fields, soot scars, ash streaks, cinders"),
    "spaceport-apron": ("ground", None, "section 5: the spaceport's concrete slabs, stains, a dashed taxi line at x 240"),
    "dust-wisps": ("low-air", 6, "light: thin dust wisps, at most 30 % opaque"),
    "smoke-banks": ("low-air", 4, "medium (72-80): the smoke over the burnt greenhouses, at most 45 %"),
}
PIECES = {   # id -> (layer, note)
    "seam-domes": ("ground", "section 2's seam: the plain into the colony's ground (exact rows)"),
    "seam-burnt": ("ground", "section 3's seam: the colony's ground into the burnt fields (exact rows)"),
    "seam-inner": ("ground", "section 4's seam: the burnt fields back into the colony's ground (exact rows)"),
    "seam-apron": ("ground", "section 5's seam: the colony's ground into the spaceport's apron (exact rows)"),
    "rail-track": ("ground", "the rail line from the north, tiling along itself every 320 px (exact rows: joins the bends)"),
    "rail-bend-a": ("ground", "the line bending from x 310 to 240 before Bradbury (exact rows)"),
    "rail-bend-b": ("ground", "the line bending from x 240 to 160 after Lowell (exact rows)"),
    "rail-yard": ("ground", "the rail yard, sidings, wagons and the buffer stops where the line ends (exact rows; the nest's turrets)"),
    "freight-train": ("ground", "the stranded freight train on the line in section 1"),
    "dome-apron-west": ("ground", "a dome's apron, annexes on the west, the road stub east toward the rail (Bradbury, Schiaparelli)"),
    "dome-apron-east": ("ground", "a dome's apron, annexes on the east, the road stub west (Lowell, Burroughs)"),
    "dome-apron-eden": ("ground", "Eden's larger apron, annexes both sides"),
    "vrell-growth-a": ("ground", "Vrell nest growth, violet with teal veins"),
    "vrell-growth-b": ("ground", "a larger patch of Vrell nest growth (under the floes)"),
    "burnt-greenhouses": ("ground", "three burnt tube greenhouses, collapsed (section 3)"),
    "helix-compound": ("ground", "the Helix compound: black and gold, lit, a clean fenced yard, unlabelled, no growth within 100 px"),
    "ring-road-north": ("ground", "the inner ring road's arc as the flight enters the ring"),
    "ring-road-south": ("ground", "the inner ring road's arc as the flight leaves it"),
    "gun-pit": ("ground", "an unmanned militia flak pit"),
    "landing-pad": ("ground", "a spaceport landing pad"),
    "hangar": ("ground", "a spaceport hangar, doors to the east"),
    "smoke-plume": ("low-air", "smoke over the burnt greenhouses, at most 45 %"),
}


def render_job(name):
    fn = GENERATORS.get(name) or SECOND[name]
    return name, fn()


def save(name, img):
    OUT.mkdir(parents=True, exist_ok=True)
    artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    print(f"  {name}: {img.size[0]}x{img.size[1]}")


def write_all(wanted):
    first = [n for n in GENERATORS if not wanted or n in wanted]
    second = [n for n in SECOND if not wanted or n in wanted
              or any(w in wanted for w in ("ares-plain", "colony-ground", "burnt-fields", "spaceport-apron"))]
    with ProcessPoolExecutor(max_workers=12) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
        for name, img in pool.map(render_job, second):
            save(name, img)


# =========================================================================== placements and the block

def size_of(name):
    return list(Image.open(OUT / f"{name}.png").size)


def placements():
    out = []
    for name, (s, _, _, _) in SEAMS.items():
        g = seam("ground", s)
        out.append((f"# the section seam under {name} (exact rows; re-run after the data moves a section's start)",
                    {"piece": name, "t": t_for_bottom("ground", g - SEAM_H // 2, SEAM_H), "x": 240}))
    b1, b2, y0 = rail_layout()
    straight = [(p, 310) for p in range(0, b1, RAIL_P)] + [(p, 240) for p in range(b1 + BEND_H, b2, RAIL_P)] + \
               [(p, 160) for p in range(b2 + BEND_H, y0, RAIL_P)]
    first = True
    for p, x in straight:
        out.append(("# the rail line from the north: straight tiles, bends and the yard at exact rows (re-run after the data moves a section)"
                    if first else "", {"piece": "rail-track", "t": t_for_bottom("ground", p, RAIL_P), "x": x}))
        first = False
    out.append(("", {"piece": "rail-bend-a", "t": t_for_bottom("ground", b1, BEND_H), "x": 275}))
    out.append(("", {"piece": "rail-bend-b", "t": t_for_bottom("ground", b2, BEND_H), "x": 200}))
    out.append(("# 3. the rail yard round the nest's turrets (x 80-130), the line's end",
                {"piece": "rail-yard", "t": t_for_bottom("ground", y0, YARD[1]), "x": 40 + YARD[0] // 2}))
    out.append(("# 1. the stranded freight train on the line, before the embankment nest",
                {"piece": "freight-train", "t": round(t_at(top_pos(8.0) - MID), 3), "x": 310}))
    nest_pos, nest_x = target_centre("embankment nest")
    out.append(("# 1. nest growth on the embankment under the spine turrets",
                {"piece": "vrell-growth-a", "t": t_centre(nest_pos), "x": round(nest_x)}))
    first = True
    for name, t, x, size in domes():
        piece = "dome-apron-eden" if size > 120 else ("dome-apron-west" if x < 240 else "dome-apron-east")
        out.append(("# 2, 4. the domes' aprons under the ally's sprites (centred on each dome)" if first else "",
                    {"piece": piece, "t": t_centre(dome_centre(t, size)), "x": round(x)}))
        first = False
    ds = {n: (t, x, s) for n, t, x, s in domes()}
    growth = [
        ("# 2. nest growth creeping toward the outer rings; under the outer floe", "vrell-growth-a", ds["Bradbury"], -88, 40),
        ("", "vrell-growth-a", ds["Lowell"], 96, -30),
        ("# 4. growth toward the inner ring; under the inner floe", "vrell-growth-a", ds["Schiaparelli"], -90, 50),
        ("", "vrell-growth-a", ds["Burroughs"], 96, 60),
        ("", "vrell-growth-a", ds["Eden"], -130, -60),
    ]
    for comment, piece, (t, x, size), dx, dpos in growth:
        out.append((comment, {"piece": piece, "t": t_centre(dome_pos(t, size) - dpos), "x": round(min(max(x + dx, 40), 440))}))
        if piece == "vrell-growth-a" and comment.startswith("# 2."):
            fp, fx = target_centre("outer floe")
            out.append(("", {"piece": "vrell-growth-b", "t": t_centre(fp), "x": round(fx)}))
        if comment.startswith("# 4."):
            fp, fx = target_centre("inner floe")
            out.append(("", {"piece": "vrell-growth-b", "t": t_centre(fp), "x": round(fx)}))
    vault_pos, _ = target_centre("seed vault", 48)
    out.append(("# 3. the burnt outer greenhouses, the secret's frame (x 70) among them, smoke over them",
                {"piece": "burnt-greenhouses", "t": t_centre(vault_pos + 170), "x": 150}))
    out.append(("", {"piece": "burnt-greenhouses", "t": t_centre(vault_pos - 120), "x": 190}))
    out.append(("", {"piece": "smoke-plume", "t": 72.5, "x": 120, "repeat": {"count": 4, "every": 2.2}}))
    out.append(("", {"piece": "smoke-plume", "t": 73.6, "x": 210, "repeat": {"count": 3, "every": 2.4}}))
    out.append(("# 3. the Helix compound (x 290-470, on screen t~78-85): nothing placed within 100 px of its fence",
                {"piece": "helix-compound", "t": helix_t(), "x": 380}))
    s4 = scroll_at(STARTS[3]) + SCREEN
    s5 = scroll_at(STARTS[4]) + SCREEN
    out.append(("# 4. the inner ring road: entering and leaving the ring", {"piece": "ring-road-north", "t": t_centre(s4 + 260), "x": 240}))
    out.append(("", {"piece": "ring-road-south", "t": t_centre(s5 - 160), "x": 240}))
    out.append(("# 4. unmanned militia gun pits round the inner ring", {"piece": "gun-pit", "t": t_centre(s4 + 700), "x": 40}))
    out.append(("", {"piece": "gun-pit", "t": t_centre(s4 + 1300), "x": 446}))
    out.append(("", {"piece": "gun-pit", "t": t_centre(s4 + 2000), "x": 36}))
    out.append(("", {"piece": "gun-pit", "t": t_centre(s4 + 2700), "x": 440}))
    apron_pos, _ = target_centre("apron nest")
    out.append(("# 5. the spaceport: pads and hangars clear of the apron nest; flak pits (the militia's guns from t~160)",
                {"piece": "landing-pad", "t": t_centre(apron_pos + 820), "x": 110}))
    out.append(("", {"piece": "landing-pad", "t": t_centre(apron_pos + 560), "x": 380}))
    out.append(("", {"piece": "hangar", "t": t_centre(apron_pos - 520), "x": 70}))
    out.append(("", {"piece": "landing-pad", "t": t_centre(apron_pos - 900), "x": 360}))
    out.append(("", {"piece": "hangar", "t": t_centre(apron_pos - 1500), "x": 412}))
    out.append(("", {"piece": "gun-pit", "t": t_centre(apron_pos - 1100), "x": 40}))
    out.append(("", {"piece": "gun-pit", "t": t_centre(apron_pos - 1900), "x": 200}))
    return out


def helix_t():
    """On screen t~78-85 (its centre at the middle at ~81.6)."""
    return round(t_at((top_pos(78) + top_pos(85) - SCREEN) / 2 - MID), 3)


def write_proposal():
    places = placements()
    flow = k8.flow
    lines = ["backdrop:",
             "  # Presentation only: Dome Siege (tools/art/backdrop_l17.py, from mars-ares-landing-r36-a and dome a).",
             "  # The ground is opaque; no deep layer (the README's Olympus horizon cannot show under it: pending round 40).",
             "  # The domes are the ally's sprites (ares-dome, ares-dome-eden); their aprons and annexes are pieces here.",
             "  # The rail pieces and seams sit at exact rows: re-run `backdrop_l17.py --proposal` after the data moves a section.",
             f"  scroll_factors: {flow(FACTORS)}",
             f"  ramp: {RAMP}",
             f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
             "  atmosphere:"]
    notes = {"clear": "the spaceport (section 5): haze only", "light": "thin dust wisps over the plain",
             "medium": "the smoke over the burnt greenhouses (section 3's peak 72-80)"}
    for k, v in ATMOSPHERE.items():
        lines.append(f"    {k}: {flow(v)}   # {notes[k]}")
    lines.append("  tile_sets:")
    for name, (layer, drift, note) in TILE_SETS.items():
        spec = {"layer": layer, "height": TILE_H}
        if drift is not None:
            spec["drift"] = drift
        lines.append(f"    {name}: {flow(spec)}   # {note}")
    lines.append("  pieces:")
    for name, (layer, note) in PIECES.items():
        lines.append(f"    {name}: {flow({'layer': layer, 'size': size_of(name)})}   # {note}")
    lines.append("  placed:")
    for comment, p in places:
        if comment:
            lines.append(f"    {comment}")
        lines.append(f"    - {flow(p)}")
    lines.append("  # Section tiles (D2's `sections[].tiles`): " + "; ".join(
        f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("  # Atmospheres as the README's Layout: 1, 2, 4 light; 3 light with the medium peak 72-80 (smoke); 5 clear.")
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== review

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def expanded(placed):
    out = []
    for p in placed:
        rep = p.get("repeat")
        for i in range((rep or {}).get("count", 1)):
            q = dict(p)
            q.pop("repeat", None)
            q["t"] = p["t"] + i * (rep or {}).get("every", 0)
            out.append(q)
    return out


def over(dst, src, x, y):
    """src (float RGBA) with its top-left at (x, y) on dst (float RGB, top-down)."""
    h, w = src.shape[:2]
    y0, y1 = max(0, y), min(dst.shape[0], y + h)
    x0, x1 = max(0, x), min(dst.shape[1], x + w)
    if y1 <= y0 or x1 <= x0:
        return
    s = src[y0 - y:y1 - y, x0 - x:x1 - x]
    a = s[..., 3:4] / 255
    dst[y0:y1, x0:x1] = s[..., :3] * a + dst[y0:y1, x0:x1] * (1 - a)


def atmosphere_at(t):
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            pk = s.get("peak")
            if pk and pk["from"] <= t < pk["to"]:
                return pk["atmosphere"]
            return s.get("atmosphere", "light")


def dome_sprite(size, state=0):
    import siege_units
    name = "ares-dome-eden" if size > 120 else "ares-dome"
    frames = siege_units.load(name)
    return np.array(frames[state % len(frames)]).astype(np.float64) if frames else None


def frame_at(t, placed):
    view = scroll_at(t)
    frame = np.zeros((SCREEN, W, 3))
    rows = jr(view) + (SCREEN - 1 - np.arange(SCREEN))
    for s in range(len(SECTIONS)):
        lo = -math.inf if s == 0 else seam("ground", s)
        hi = seam("ground", s + 1) if s + 1 < len(SECTIONS) else math.inf
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(SECTION_TILES[s][0])
            frame[sel] = img[[TILE_H - 1 - (int(r) % TILE_H) for r in rows[sel]]][..., :3]
    for layer in ("ground", "low-air"):
        f = FACTORS[layer]
        if layer == "low-air":
            bank = ATMOSPHERE[atmosphere_at(t)].get("banks")
            if bank:
                img = cached(bank)
                lrows = jr(view * f) + (SCREEN - 1 - np.arange(SCREEN))
                over(frame, img[[TILE_H - 1 - (int(r) % TILE_H) for r in lrows]], 0, 0)
        for p in expanded(placed):
            if PIECES[p["piece"]][0] != layer:
                continue
            img = cached(p["piece"])
            h, w = img.shape[:2]
            bottom = jr(scroll_at(p["t"]) * f + MID - h / 2)
            top_y = SCREEN - (bottom + h - jr(view * f))
            over(frame, img, jr(p["x"] - w / 2), top_y)
        if layer == "ground":
            for name, te, x, size in domes():
                sp = dome_sprite(size, int(t * 4))
                if sp is None:
                    continue
                y = SCREEN - (dome_centre(te, size) - view) - size / 2
                over(frame, sp, jr(x - size / 2), jr(y))
    haze = ATMOSPHERE[atmosphere_at(t)]["haze"]
    sky = np.array(m36.SKY, float)
    frame = frame * (1 - haze) + sky * haze
    return np.clip(frame, 0, 255).astype(np.uint8)


def review():
    placed = [p for _, p in placements()]
    times = [10, 20, 42, 49, 76, 82, 90, 108, 114, 130, 150, 162]
    frames = [Image.fromarray(frame_at(t, placed), "RGB") for t in times]
    scale = 0.5
    cw, ch = int(W * scale), int(SCREEN * scale)
    cols = 6
    sheet = raster.sheet(16 + cols * (cw + 10), 60 + 2 * (ch + 24) + 240,
                         "LEVEL 17 DOME SIEGE - BACKDROP (FINAL R40)", "PRODUCTION ART, M7 PART D BATCH - R40")
    raster.draw_text(sheet, 16, 38, "THE PLAY FIELD AT 0.5X AT THESE TIMES (THE DOMES FROM THE ALLY'S SPRITES)", raster.LABEL)
    for i, (t, fr) in enumerate(zip(times, frames)):
        x, y = 16 + (i % cols) * (cw + 10), 54 + (i // cols) * (ch + 24)
        sheet.paste(fr.resize((cw, ch), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t}", raster.LABEL_DIM)
    y = 54 + 2 * (ch + 24) + 6
    raster.draw_text(sheet, 16, y, "PIECES AT 0.5X", raster.LABEL)
    x = 16
    for name in ("helix-compound", "dome-apron-west", "dome-apron-east", "dome-apron-eden", "burnt-greenhouses",
                 "rail-yard", "vrell-growth-b", "hangar", "landing-pad", "gun-pit", "freight-train"):
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        img = img.resize((max(1, img.width // 2), max(1, img.height // 2)), Image.BOX)
        if x + img.width > sheet.width - 16:
            break
        sheet.alpha_composite(img, (x, y + 16))
        x += img.width + 10
    sheet.convert("RGB").save(REVIEW, optimize=True)
    gif = [Image.fromarray(frame_at(t, placed), "RGB").resize((240, 270), Image.BOX)
           for t in np.arange(0.0, END, 0.5)]
    write_gif(gif, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif")


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--proposal" in sys.argv[1:]:
        write_proposal()
    elif "--review" in sys.argv[1:]:
        review()
    else:
        write_all(args)
        write_proposal()
        review()
