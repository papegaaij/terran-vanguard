#!/usr/bin/env python3
"""Production art: the Level 21 backdrop, Dust Colossus (design/campaign/act-3-red-dust/level-21-dust-colossus;
M7 part H, step ART-H1, review in round 44).

Straight to production from the approved Hellas mood still `mars-hellas-r36-a` (tools/concept/mars_r36.py
`hellas()`, imported unchanged for its ochre ramp and lime) and in Level 20's style (tools/art/backdrop_l20.py, which
shares the basin): the rim with the CDF line held at the end of the retreat, the basin wall's slope below it, the
inner wall's terraces with landslide fans of red sand, the pale dune sea with old breach craters, the lime hollow's
crust cracked in rings round the centre with sinkholes and Vrell growth, the arena's seamless basin floor and the
settled dust after the kill. Lit from the upper left (backdrop_l15.lit / backdrop_l17.finish_piece), receding,
posterized per image. Nothing lit is mirrored. The level flies into the basin, so the rim is at the bottom of the
level and the risers face up the screen (toward the basin), as on Level 20's strip.

The ground is opaque everywhere (no `deep` layer, as Levels 17-20). Layers: ground (x1), low-air (x1.35), high-air
(x2: section 6's Vrell lifting off).

**The arena and section 6** (round 43, H.6): the arena's tile set `basin-floor` repeats every 512 px (the tile rows
are the ground position mod 512, as Backdrop.drawTiles counts them), so the crawl (20 px/s on real seconds, the clock
held at t=245) never runs out of ground and the kill's jump moves no visible ground. Section 6's tile set
`settled-floor` is the same floor under fallen dust, also 512 px and on the same pattern, so it may start at any
arena seam: **the game starts it at the next multiple of 512 at or above the top edge** when section 6 begins (at
most 512 px, 20 s at 25 px/s) and lays `seam-settle` (480x320) centred on that row, its ragged boundary hiding the
change (its rows match both tile sets at any multiple of 512). The collapsed crater `colossus-sinkhole` and the
ridge's sunken furrow `colossus-furrow` (16 headings, 96x96 lengths laid every 16 px along the ridge's last track,
as the ridge's own lengths, tools/art/l20_props.py) are placed by the game at the kill; they are declared pieces with
no `placed` entry. Section 6's high-air placements are timed in script time from t=245: the game draws them shifted
by the kill's jump (scrollAt(245) less the ground at the kill), as the crater (pending HA).

Tile sets (480 wide): ground `rim-wall` (960: the basin wall's upper slope, gullies, strata, the retreat's shell
craters and scorch), `inner-wall` (960: four terrace steps of layered rock, red sand on the treads), `dune-sea`
(960: transverse dunes, crests across the screen), `hollow-crust` (960: a lime-veined crust in polygons, cracked in
rings round the centre ahead, the cracks lit lime from below), `basin-floor` (512: the hollow's floor, the arena: a
faint polygon crust, sand drifts and ripples, lime veins glowing), `settled-floor` (512: the same floor under fallen
dust, the veins gone dark); low-air `rim-dust` (light: dust streaks off the rim), `basin-haze` (medium),
`hollow-dust` (heavy: denser dust lit lime from below).

Pieces (ground): `rim-line` (480x960, exact rows: the level's start: the rust plain behind the line, two scorched
pads, the CDF berm and trench along the rim's edge, the crest, the slope's rows of `rim-wall` above; its bottom row
below the screen at t=0); `cdf-gun-pit` (backdrop_l17.gun_pit: the line's emplacements); `scorched-pad`;
`burnt-crawler-a`/`-b` (Level 20's crawler model, burnt), `cdf-walker-wreck` (a toppled militia walker); seams
`seam-wall`, `seam-dunes`, `seam-hollow`, `seam-arena` (480x320, exact rows) and `seam-settle` (the game's);
`landslide-a`/`-b` (fans of red sand spreading down the wall, up the screen); `cdf-hauler-wreck`
(tools/art/l21_props.py: the t~36 turret nest stands on it); `dune-crest` (the t~57 mortars' crest); `old-crater-a`
/`-b` (old breach craters filling with sand); `sinkhole-a`/`-b`; `vrell-growth-a`/`-b`; `colossus-sinkhole`,
`colossus-furrow` (the game's, at the kill). High-air: `vrell-liftoff` (2 headings, 40x120: a Vrell shape climbing
on its spore trail; heading 0 up).

Outputs: assets/backdrop/level-21/*.png; the review sheet and GIF
design/campaign/act-3-red-dust/level-21-dust-colossus/concept/backdrop-final-r44-a.png/.gif;
`--proposal` writes the level's `backdrop:` block to briefs/l21-backdrop.yaml (for the data step to merge; never
into the level's data.yaml); `--check` checks the tile wraps, sizes, edges, seams and the crawler's tread.

Reads the level's data.yaml when it exists (sections with speed and `ease`), otherwise the README's values (1 s
eases, the arena's 2 s). The seams sample the tile sets at their rows: after the data moves a section, re-render
them (`backdrop_l21.py seam-wall seam-dunes seam-hollow seam-arena`, which rewrites the proposal too).
Run: python3 tools/art/backdrop_l21.py [--proposal | --review | --check] [id ...] (all: ~2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, write_gif

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, YAML flow, wrap score)
import backdrop_l15 as b15  # noqa: E402  (tools/art: periodic noise, lighting, posterizing, banks, dunes)
import backdrop_l17 as b17  # noqa: E402  (tools/art: the Mars plain base, piece finishing, Vrell growth, gun pit)
import backdrop_l20 as b20  # noqa: E402  (tools/art: the Hellas palette, the militia crawler model)
import l20_props as l20  # noqa: E402  (tools/art: the ridge's per-heading frame, flat finishing)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps)
from render import raster  # noqa: E402
from render.sdf import Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, union  # noqa: E402

SCRIPT = "backdrop_l21.py"
BATCH = "M7 part H"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-21-dust-colossus"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = ROOT / "briefs" / "l21-backdrop.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-21"
REVIEW = CONCEPT / "backdrop-final-r44-a.png"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
FLOOR_H = 512                           # the arena's and section 6's period (H.6)
FACTORS = {"ground": 1.0, "low-air": 1.35, "high-air": 2.0}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
RAMP = 3
ATMOSPHERE = {
    "clear": {"haze": 0.03},
    "light": {"banks": "rim-dust", "haze": 0.06},
    "medium": {"banks": "basin-haze", "haze": 0.09},
    "heavy": {"banks": "hollow-dust", "haze": 0.12},
}
OCHRE, PLAIN, RUST = m36.OCHRE, m36.PLAIN, m36.RUST
ROCK, STRATA = b20.ROCK, b20.STRATA
LIME = np.array(m36.LIME, float)
REDSAND = [(92, 40, 24), (132, 62, 36), (170, 88, 54), (198, 118, 76), (220, 150, 104)]

pn, aniso, smooth, ramp, pdist = b15.pn, b15.aniso, b15.smooth, b15.ramp, b15.pdist
lit, recede, posterize_rgb, dither_alpha = b15.lit, b15.recede, b15.posterize_rgb, b15.dither_alpha
finish, finish_piece, sdf_piece = b17.finish, b17.finish_piece, b17.sdf_piece
jr = k8.java_round


# =========================================================================== the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists (1 s eases; the arena's 2 s, Q7a)
    {"name": "Hellas Rim", "end": 22, "speed": 160, "atmosphere": "light"},
    {"name": "Inner Wall", "end": 50, "speed": 170, "ease": 1, "atmosphere": "light"},
    {"name": "Dune Sea", "end": 78, "speed": 150, "ease": 1, "atmosphere": "medium"},
    {"name": "Lime Hollow", "end": 105, "speed": 140, "ease": 1, "atmosphere": "heavy"},
    {"name": "Dust Colossus", "end": 245, "speed": 20, "ease": 2, "arena": True, "atmosphere": "heavy"},
    {"name": "Settling Dust", "end": 277, "speed": 25, "ease": 1, "atmosphere": "clear"},
]
SECTION_TILES = [["rim-wall"], ["inner-wall"], ["dune-sea"], ["hollow-crust"], ["basin-floor"], ["settled-floor"]]
OUTRO = 15.0                             # LevelData.OUTRO_SECONDS
KILL = 235.0                             # the review's kill (real s): the arrival at 105 + the spec's 130 s par


def _load(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


DATA = _load(LEVEL_DIR / "data.yaml")
SECTIONS = [dict(s) for s in (DATA.get("sections") or DEFAULT_SECTIONS)]
SPEED = float(DATA.get("scroll_speed", 150))
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
END = float(SECTIONS[-1]["end"])
ARENA = next((i for i, s in enumerate(SECTIONS) if s.get("arena")), 4)
HOLD = float(SECTIONS[ARENA]["end"])     # the clock holds here while the Colossus lives


def speed(i):
    return float(SECTIONS[i].get("speed", SPEED))


def ease(i):
    if i == 0 or speed(i) == speed(i - 1):
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
    """LevelData.scrollAt with the M7 part B ease (as backdrop_l19.scroll_at)."""
    scroll = 0.0
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            return scroll + _travelled(i, STARTS[i], t)
        scroll += _travelled(i, STARTS[i], s["end"])
    raise AssertionError


def t_at(scroll):
    lo, hi = -60.0, END + 80
    for _ in range(80):
        m = (lo + hi) / 2
        if scroll_at(m) < scroll:
            lo = m
        else:
            hi = m
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


def t_centre(pos, layer="ground"):
    return round(t_at((pos - MID) / FACTORS[layer]), 3)


def top_pos(t, layer="ground"):
    return scroll_at(t) * FACTORS[layer] + SCREEN


def piece_bottom(p, h, layer="ground"):
    return jr(scroll_at(p["t"]) * FACTORS[layer] + MID - h / 2)


# =========================================================================== periodic fields

def grid(h=TILE_H):
    return np.mgrid[0:h, 0:W].astype(np.float64)


def from_bottom(yy, h=TILE_H):
    return h - 1 - yy


def fnoise(h, seed, kmax=3, terms=24):
    """Low-frequency noise 0..1 periodic over W x h for any h (sums of whole-cycle waves)."""
    yy, xx = grid(h)
    rng = np.random.default_rng(seed)
    acc = np.zeros(xx.shape)
    for _ in range(terms):
        kx, ky = rng.integers(-kmax, kmax + 1, 2)
        if kx == 0 and ky == 0:
            continue
        acc += np.cos(2 * np.pi * (kx * xx / W + ky * yy / h) + rng.uniform(0, 2 * np.pi)) / math.hypot(kx, ky)
    return (acc - acc.min()) / (acc.max() - acc.min())


def cells(seed, n, h, warp=6.0, cell=32):
    """A periodic Voronoi network over W x h: (distance to the nearest edge, the cell's index)."""
    yy, xx = grid(h)
    rng = np.random.default_rng(seed)
    px, py = rng.uniform(0, W, n), rng.uniform(0, h, n)
    wx = xx + warp * (pn(W, h, cell, seed + 1, 3) - 0.5) * 2
    wy = yy + warp * (pn(W, h, cell, seed + 2, 3) - 0.5) * 2
    d1 = np.full(xx.shape, np.inf)
    d2 = np.full(xx.shape, np.inf)
    idx = np.zeros(xx.shape, int)
    for k, (x0, y0) in enumerate(zip(px, py)):
        d = np.hypot(pdist(wx, x0, W), pdist(wy, y0, h))
        closer = d < d1
        d2 = np.where(closer, d1, np.minimum(d2, d))
        idx = np.where(closer, k, idx)
        d1 = np.where(closer, d, d1)
    return (d2 - d1) / 2, idx


def blur_wrap(m, n=3):
    h = m.astype(np.float64)
    for _ in range(n):
        h = (h + np.roll(h, 1, 0) + np.roll(h, -1, 0) + np.roll(h, 1, 1) + np.roll(h, -1, 1)) / 5
    return h


def lime_into(rgb, mask, amount=0.7, halo=0.4):
    """Cracks lit lime from below over the lit ground: a soft halo and the line (wrapping)."""
    m = np.clip(mask.astype(np.float64), 0, 1)
    if halo > 0:
        hh = np.clip(blur_wrap(m) * 2.2, 0, 1) * halo
        rgb = rgb * (1 - hh[..., None]) + LIME * 0.8 * hh[..., None]
    return rgb * (1 - amount * m[..., None]) + LIME * amount * m[..., None]


# =========================================================================== tile sets

def rim_wall():
    """Section 1: the basin wall's upper slope below the rim: gullies running down it (up the screen), strata
    outcrops across, rock blocks, scree and sand pockets; the retreat's shell craters, scorch and scattered
    debris (the fight for the line)."""
    yy, xx = grid()
    n = pn(W, TILE_H, 48, 2101, 4)
    gully = aniso(W, TILE_H, 1, 6, 8, 2102, 3)
    band = np.sin(2 * np.pi * (12 * yy / TILE_H) + 4 * pn(W, TILE_H, 160, 2103, 2))
    edge, cell = cells(2104, 60, TILE_H, warp=14, cell=48)
    block = smooth(0.5, 3.0, edge)
    tone = np.random.default_rng(2105).uniform(-1, 1, 60)[cell]
    rng = np.random.default_rng(2106)
    scree = rng.random(xx.shape) > 0.975
    pocket = smooth(0.6, 0.74, pn(W, TILE_H, 48, 2107, 3))
    craters, ejecta = b15.crater_field(W, TILE_H, 14, 4.0, 15.0, 2108, depth=1.3)
    scorch = smooth(0.05, 0.6, blur_wrap(np.clip(-craters, 0, None), 6) * 3) * (0.6 + 0.6 * (n - 0.5))
    debris = (rng.random(xx.shape) > 0.9965) & (scorch > 0.15)
    hgt = 3.4 * gully + 0.8 * n + 0.5 * band + 0.5 * block + 0.6 * tone + 0.9 * scree + 1.6 * craters + 0.8 * debris
    rock = raster.ramp(STRATA, np.clip(0.5 + 0.12 * band + 0.12 * tone + 0.2 * (n - 0.5), 0, 1)) * 0.92
    rock = rock * (1 - 0.2 * scree[..., None]) * (1 - 0.1 * (1 - block[..., None]))
    sand = ramp(OCHRE[1:5], np.clip(0.55 + 0.25 * (n - 0.5), 0, 1))
    pocket = np.clip(pocket + 0.5 * smooth(0.55, 0.8, gully), 0, 1)
    alb = rock * (1 - pocket[..., None]) + sand * pocket[..., None] + 10 * ejecta[..., None]
    alb = alb * (1 - 0.55 * np.clip(scorch, 0, 1)[..., None])
    alb = np.where(debris[..., None], np.array((96, 98, 92), float), alb)
    return finish(lit(hgt, alb, ambient=0.45, key=0.7), 36)


RISERS = (900, 680, 430, 190)           # the inner wall's risers (tile rows from the bottom); check() keeps the
# survey crawler (t~44, x 80) on a tread
RISE, FACE = 30.0, 24.0


def riser_offset(xx, k):
    rng = np.random.default_rng(2120 + k)
    return sum(a * np.sin(2 * np.pi * c * xx / W + rng.uniform(0, 6.3)) for c, a in ((1, 16), (2, 9), (5, 4), (9, 2)))


def inner_wall():
    """Section 2: the inner wall's terraces falling into the basin: four steps a tile (uneven treads), each riser a
    band of layered rock lit as it faces the basin (up the screen), boulders at its foot, red sand from the slides
    drifted on the treads (backdrop_l20.terraces' build, the wall's own steps and sand)."""
    yy, xx = grid()
    rb = from_bottom(yy)
    n = pn(W, TILE_H, 48, 2121, 4)
    hgt = np.zeros(xx.shape)
    face = np.zeros(xx.shape)
    lip = np.zeros(xx.shape)
    foot = np.zeros(xx.shape)
    strata = np.zeros(xx.shape)
    for k, r in enumerate(RISERS):
        u = ((r + riser_offset(xx, k) + 4 * (n - 0.5) - rb + TILE_H / 2) % TILE_H) - TILE_H / 2   # > 0 below it
        s = smooth(-FACE, FACE, u)
        hgt += RISE * (s - (u + TILE_H / 2) / TILE_H)
        f = np.clip(1 - np.abs(u) / FACE, 0, 1)
        face = np.maximum(face, f)
        strata = np.where(f > 0, (u + FACE) / (2 * FACE) + 0.12 * (pn(W, TILE_H, 24, 2125 + k, 2) - 0.5), strata)
        lip = np.maximum(lip, np.exp(-((u - FACE - 3) / 3.5) ** 2))
        foot = np.maximum(foot, np.exp(-((u + FACE + 8) / 7) ** 2))
    rng = np.random.default_rng(2122)
    boulders = (rng.random(xx.shape) > 0.88) * foot
    boulders = np.maximum(boulders, np.roll(boulders, 1, 1) * 0.8)
    boulders = np.maximum(boulders, np.roll(boulders, 1, 0) * 0.6)
    rip = b20.sand_ripples(yy, xx, 2123, cycles=200)
    bare = smooth(0.45, 0.8, 0.9 * lip + 0.25 * pn(W, TILE_H, 24, 2124, 3)) * (1 - face)
    sandy = (1 - bare) * (1 - face)
    red = 0.75 * smooth(0.52, 0.68, pn(W, TILE_H, 96, 2126, 4))   # the slides' red sand on the treads
    hgt = hgt + 0.5 * n + 2.0 * boulders + sandy * (0.1 * rip + 0.8 * red) + 1.2 * lip \
        + 1.5 * bare * pn(W, TILE_H, 16, 2127, 3)
    rock = raster.ramp(STRATA, np.clip(strata + 0.06 * (n - 0.5), 0, 1))
    rockb = ramp(ROCK[1:], np.clip(0.52 + 0.3 * (n - 0.5), 0, 1))
    ochre = ramp(OCHRE[1:5], np.clip(0.64 + 0.2 * (n - 0.5) + 0.05 * rip, 0, 1))
    reds = ramp(REDSAND[1:], np.clip(0.6 + 0.25 * (n - 0.5) + 0.05 * rip, 0, 1))
    sand = ochre * (1 - red[..., None]) + reds * red[..., None]
    alb = rockb * bare[..., None] + sand * sandy[..., None]
    alb = np.where((face > 0)[..., None], rock, alb)
    alb = alb * (1 - 0.3 * boulders[..., None]) * (1 - 0.22 * foot[..., None] * (1 - face[..., None]))
    return finish(lit(hgt, alb, ambient=0.45, key=0.7), 36)


def dune_sea():
    """Section 3: the dune sea (backdrop_l20.basin_dunes' build): transverse dunes with their crests across the
    screen, gentle stoss slopes, slip faces toward the rim, ripples on the stoss; its own dunes."""
    yy, xx = grid()
    ty, tx = np.mgrid[0:W, 0:TILE_H].astype(np.float64)
    prof, u = b15.dune_field(TILE_H, W, 2131, ty, tx)
    prof, u = prof.T, u.T
    rip = b20.sand_ripples(yy, xx, 2132, cycles=280) * (u < 0.74)
    n = pn(W, TILE_H, 48, 2133, 3)
    hgt = 12 * prof + 0.22 * rip + 1.2 * n
    trough = np.clip(1 - prof / 0.25, 0, 1)
    t = np.clip(0.62 + 0.22 * (n - 0.5) + 0.22 * prof - 0.3 * trough, 0, 1)
    alb = ramp(OCHRE[1:], t)
    return finish(lit(hgt, alb, ambient=0.46, key=0.66), 32)


RING_R, RING_GAP = 1100.0, 160          # the hollow's ring cracks: radius of the arcs (px), rows between them


def hollow_crust():
    """Section 4: the lime hollow, the basin's lowest point: a pale crust of polygons with curled rims and dark
    cracks, the sand thin over it, cracked in rings round the centre ahead (arcs bowed toward the rim, one every
    160 rows), lime veins and the ring cracks lit from below."""
    yy, xx = grid()
    rb = from_bottom(yy)
    edge, idx = cells(2141, 160, TILE_H, warp=6, cell=48)
    tone_cell = np.random.default_rng(2142).uniform(-1, 1, 160)[idx]
    n = pn(W, TILE_H, 48, 2143, 4)
    crack = smooth(1.6, 0.5, edge)
    rim = smooth(4.0, 1.6, edge) * (1 - crack)
    wob = 5 * (pn(W, TILE_H, 32, 2144, 3) - 0.5)
    u = (rb - (xx - W / 2) ** 2 / (2 * RING_R) + wob) % RING_GAP
    ring = np.exp(-((u - RING_GAP / 2) / 1.8) ** 2)
    ring_rise = np.exp(-((u - RING_GAP / 2 - 7) / 6) ** 2)
    drift = smooth(0.6, 0.76, aniso(W, TILE_H, 4, 1, 40, 2145, 3) * 0.7 + pn(W, TILE_H, 160, 2146, 3) * 0.45)
    vein = (np.abs(pn(W, TILE_H, 96, 2147, 3) - 0.5) < 0.010) & (pn(W, TILE_H, 160, 2148, 2) > 0.48)
    hgt = 0.5 * n + 0.8 * rim - 1.0 * crack - 2.6 * ring + 2.0 * ring_rise
    hgt = hgt * (1 - drift) + drift * (1.2 + 0.8 * n)
    crust = ramp([(120, 112, 84), (160, 146, 106), (196, 178, 132), (220, 204, 160)],
                 np.clip(0.6 + 0.12 * tone_cell + 0.2 * (n - 0.5) + 0.12 * rim, 0, 1))
    crust = crust * (1 - 0.45 * crack[..., None]) + np.array((84, 78, 52), float) * 0.45 * crack[..., None]
    crust = crust * (1 - 0.4 * ring[..., None])
    sand = ramp(OCHRE[2:6], np.clip(0.45 + 0.25 * (n - 0.5), 0, 1))
    alb = crust * (1 - drift[..., None]) + sand * drift[..., None]
    rgb = lit(hgt, alb, ambient=0.46, key=0.64)
    rgb = lime_into(rgb, (ring > 0.45) * (1 - 0.8 * drift), amount=0.7, halo=0.6)
    rgb = lime_into(rgb, vein * (1 - 0.7 * drift), amount=0.55, halo=0.3)
    return finish(rgb, 36)


def floor_fields():
    """The hollow's floor at the centre (period 512): a faint polygon crust under drifted sand and ripples, small
    pits; the lime veins' mask. Shared by the arena's and section 6's tile sets."""
    yy, xx = grid(FLOOR_H)
    edge, idx = cells(2151, 44, FLOOR_H, warp=10)
    tone_cell = np.random.default_rng(2152).uniform(-1, 1, 44)[idx]
    n = pn(W, FLOOR_H, 32, 2153, 5)
    big = fnoise(FLOOR_H, 2154, kmax=3)
    crack = smooth(1.5, 0.4, edge)
    drift = smooth(0.42, 0.7, big * 0.8 + n * 0.35)
    warp = pn(W, FLOOR_H, 32, 2155, 3)
    rip = np.sin(2 * np.pi * (96 * yy / FLOOR_H + 2 * xx / W) + 9 * warp)
    rng = np.random.default_rng(2156)
    pits = rng.random(xx.shape) > 0.9975
    pits = blur_wrap(pits, 2) * 6
    vein = (np.abs(pn(W, FLOOR_H, 32, 2157, 4) - 0.5) < 0.012) & (fnoise(FLOOR_H, 2158, kmax=2) > 0.5)
    hgt = 0.5 * n - 0.45 * crack + 0.2 * tone_cell - 1.2 * pits
    hgt = hgt * (1 - drift) + drift * (1.0 + 0.7 * n + 0.25 * rip)
    crust = ramp([(136, 120, 88), (172, 154, 112), (204, 184, 138), (222, 204, 158)],
                 np.clip(0.58 + 0.1 * tone_cell + 0.2 * (n - 0.5), 0, 1))
    crust = crust * (1 - 0.18 * crack[..., None])
    sand = ramp([(160, 128, 90), (196, 166, 120), (220, 196, 150), (236, 218, 178)],
                np.clip(0.42 + 0.22 * (n - 0.5) + 0.05 * rip, 0, 1))
    alb = crust * (1 - drift[..., None]) + sand * drift[..., None]
    return hgt, alb, vein, drift, n


def floor_pair():
    """The arena's `basin-floor` (the veins lit lime from below, calm enough for the breaches to read) and section
    6's `settled-floor` (the same floor under a blanket of fallen dust: the relief softened, paler, the veins dark):
    one palette for both, so `seam-settle` joins them on equal terms."""
    hgt, alb, vein, drift, n = floor_fields()
    arena = lit(hgt, alb, ambient=0.46, key=0.62)
    arena = lime_into(arena, vein * (1 - 0.6 * drift), amount=0.6, halo=0.35)
    yy, xx = grid(FLOOR_H)
    blanket = 0.86 + 0.14 * fnoise(FLOOR_H, 2159, kmax=2)
    ripple = np.sin(2 * np.pi * (64 * yy / FLOOR_H + 1 * xx / W) + 6 * pn(W, FLOOR_H, 32, 2160, 3))
    soft = hgt * (1 - 0.85 * blanket) + blanket * (1.1 + 0.5 * n + 0.12 * ripple)
    dust = ramp(OCHRE[2:6], np.clip(0.55 + 0.2 * (n - 0.5), 0, 1))
    salb = alb * (1 - 0.85 * blanket[..., None]) + dust * 0.85 * blanket[..., None]
    salb = np.where((vein & (blanket < 0.85))[..., None], salb * 0.8, salb)
    settled = lit(soft, salb, ambient=0.5, key=0.55)
    imgs = k8.quantize_lit([raster.to_rgba_image(recede(arena, 0.2)), raster.to_rgba_image(recede(settled, 0.2))],
                           36)
    return {"basin-floor": imgs[0], "settled-floor": imgs[1]}


def streaks(seed, max_a, colours):
    """Dust streaks off the rim on low-air: wind-stretched bands across the screen (wrapping), stepped alpha."""
    n = aniso(W, TILE_H, 6, 1, 16, seed, 4)
    n2 = pn(W, TILE_H, 96, seed + 1, 3)
    a = np.clip((n * 0.7 + n2 * 0.5 - 0.7) / 0.16, 0, 1) * max_a / 255
    rgb = ramp(colours, np.clip(0.3 + 0.8 * (n - 0.4), 0, 1))
    a = np.round(a * 6) / 6
    img = raster.to_rgba_image(rgb, a * 255)
    return artkit.quantize_set([img], 12)[0]


def rim_dust():
    return streaks(2161, 60, OCHRE[2:6])


def basin_haze():
    return b15.cloud_bank(W, TILE_H, 2163, 0.72, 0.16, 66, colours=OCHRE[1:5])


def hollow_dust():
    return b15.cloud_bank(W, TILE_H, 2165, 0.62, 0.18, 88,
                          colours=[(104, 100, 58), (146, 138, 84), (184, 176, 116), (214, 206, 150)])


# =========================================================================== seams

FEATHER = 32
SEAM_H = 320
SEAMS = {   # name -> (section whose start it marks, the tile set below, the one above, seed); None: the game's
    "seam-wall": (1, "rim-wall", "inner-wall", 2171),
    "seam-dunes": (2, "inner-wall", "dune-sea", 2173),
    "seam-hollow": (3, "dune-sea", "hollow-crust", 2175),
    "seam-arena": (4, "hollow-crust", "basin-floor", 2177),
    "seam-settle": (None, "basin-floor", "settled-floor", 2179),
}


def seam_row(name):
    s = SEAMS[name][0]
    return 0 if s is None else seam("ground", s)        # seam-settle: any multiple of 512 (both tiles' period)


def tile_strip(name, bottom, h):
    """A tile set's rows for layer positions [bottom, bottom + h) as an image array (top row = highest)."""
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def feathered(out, h, bottom=True):
    k = np.arange(h)[:, None]
    a = np.clip((k + 0.5) / FEATHER, 0, 1)
    if bottom:
        a = np.minimum(a, np.clip((h - k - 0.5) / FEATHER, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where(alpha > 0, 255, 0)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def seam_piece(name):
    """backdrop_l19.seam_piece: the lower tile set below a ragged boundary at the seam, the upper's above, a darker
    line on the boundary (none under the settling dust), the ends faded into the tile sets (1-bit dither)."""
    _, lower, upper, seed = SEAMS[name]
    g = seam_row(name)
    bottom, h = g - SEAM_H // 2, SEAM_H
    up = tile_strip(upper, bottom, h)
    lo = tile_strip(lower, bottom, h)
    rows = bottom + h - 1 - np.arange(h)[:, None]
    amps = (6, 4, 2, 1) if name == "seam-dunes" else (16, 10, 5, 3)
    edge = g + b20.wobble(seed, amps)
    if name == "seam-settle":                           # a soft dithered edge: the dust thins out downward
        d = rows - edge
        a = np.clip(0.5 + d / 60, 0, 1)
        pick = artkit.ordered_dither(np.broadcast_to(a, (h, W)), 2) > 0
        out = np.where(pick[..., None], up, lo)
    else:
        out = np.where((rows >= edge)[..., None], up, lo)
        d = rows - edge
        r = (d >= 0) & (d < 2)
        out[..., :3] = np.where(r[..., None], out[..., :3] * 0.78, out[..., :3])
    return feathered(out, h)


# =========================================================================== the start: the CDF line on the rim

LINE_H = 960
LINE_BOTTOM = -40                       # below the screen's bottom edge at t=0 (the scroll starts at 0)
PADS = ((110, 150), (360, 190))         # the scorched pads (x, layer position)
BERM, CREST = 400.0, 470.0              # the CDF berm's and the rim crest's layer positions
PITS = ((52, 376), (168, 382), (318, 378), (432, 384))


def rim_line():
    """The level's start: the rust plain behind the CDF line (backdrop_l17.plain_base, scorched by the fighting),
    the line's sandbag berm with its trench behind it along the rim's edge, the crest lit facing the basin, then
    the slope falling into the basin; above the crest it dithers into `rim-wall`'s own rows, so its top joins the
    section's tiles exactly."""
    h = LINE_H
    bottom = LINE_BOTTOM
    up = tile_strip("rim-wall", bottom, h)
    hgt, alb, yy, xx = b17.plain_base(2181, W, h)
    pos = bottom + h - 1 - yy
    nn = pn(W, h, 32, 2182, 4)
    crest = CREST + b20.wobble(2183, (10, 6, 3, 2))[0][None, :] * np.ones((h, 1))
    u = pos - crest                                     # > 0 beyond the crest (toward the basin)
    hgt = hgt + 14 * np.exp(-(np.maximum(u, 0) / 34) ** 2) * np.exp(-(np.minimum(u, 0) / 60) ** 2)
    berm_y = BERM + 4 * np.sin(2 * np.pi * xx / W * 2 + 0.6)
    v = pos - berm_y
    bags = np.exp(-(v / 4.5) ** 2) * (0.85 + 0.15 * np.cos(xx * 0.9 + (v > 0) * 1.7) ** 2)
    trench = np.exp(-((v + 11) / 3.5) ** 2)
    hgt = hgt + 3.5 * bags - 3.0 * trench
    craters, ej = b15.crater_field(W, h, 22, 4.0, 13.0, 2184, depth=1.4)
    near = np.exp(-((pos - 330) / 170) ** 2)
    hgt = hgt + craters * (0.4 + near)
    scorch = smooth(0.05, 0.5, blur_wrap(np.clip(-craters, 0, None) * near, 6) * 3)
    for x0, p0 in PADS:                                 # the pads' footprints: flat
        r = np.hypot(xx - x0, pos - p0)
        hgt = np.where(r < 50, hgt * 0.2 + 0.4, hgt)
    alb = alb * (1 - 0.5 * scorch[..., None])
    bagc = ramp([(120, 104, 76), (160, 142, 106), (190, 172, 132)], np.clip(0.5 + 0.4 * (nn - 0.5), 0, 1))
    alb = alb * (1 - np.clip(bags * 1.5, 0, 1)[..., None]) + bagc * np.clip(bags * 1.5, 0, 1)[..., None]
    alb = alb * (1 - 0.5 * trench[..., None])
    rock = raster.ramp(STRATA, np.clip(0.42 + 0.004 * u + 0.25 * (nn - 0.5), 0, 1)) * 0.92
    rk = smooth(-30, 10, u)[..., None]
    alb = alb * (1 - rk) + rock * rk
    rgb = finish(lit(hgt, alb, wrap=False, ambient=0.45, key=0.72), 36)
    own = np.array(rgb).astype(np.float64)
    a = smooth(CREST + 70, CREST + 220, pos)            # 0 own art .. 1 the tile's rows
    pick = artkit.ordered_dither(np.clip(a, 0, 1), 2) > 0
    out = np.where(pick[..., None], up, own)
    return feathered(out, h, bottom=False)


# =========================================================================== pieces

def scorched_pad():
    """A CDF landing pad behind the line: a raised concrete hexagon, its yellow ring half burnt away, a black blast
    scorch from the last lift-off, debris on it."""
    w = h = 96
    f = 2
    yy, xx = b15.piece_canvas((w, h), f)
    dx, dy = xx - w / 2, yy - h / 2
    ang = np.arctan2(dy, dx)
    hexr = 40 / np.cos((ang % (np.pi / 3)) - np.pi / 6)
    r = np.hypot(dx, dy)
    n = raster.fbm(w * f, h * f, 16, 2191, octaves=3, period=False)
    disc = r < hexr
    hgt = 1.2 * disc + 0.25 * n
    alb = np.zeros(yy.shape + (3,)) + np.array((156, 148, 136)) * (0.92 + 0.12 * n[..., None])
    ring = (np.abs(r - 31) < 1.8) & (n > 0.42)
    alb = np.where(ring[..., None], np.array((200, 160, 58), float), alb)
    scorch = np.clip(1.1 - r / 30 + 0.5 * (n - 0.5), 0, 1)
    alb = alb * (1 - 0.8 * scorch[..., None])
    rng = np.random.default_rng(2192)
    bits = (rng.random(r.shape) > 0.996) & disc
    hgt = hgt + 1.2 * bits
    cover = np.clip(1 - (r - hexr) / 2, 0, 1)
    return finish_piece(hgt, alb, cover, f, colours=24)


def cdf_gun_pit():
    """backdrop_l17.gun_pit, padded to 64 px so its shadow clears the edges."""
    pit = b17.gun_pit()
    canvas = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    canvas.alpha_composite(pit.convert("RGBA"), ((64 - pit.width) // 2, (64 - pit.height) // 2))
    return canvas


def burnt_crawler_a():
    return sdf_piece(b20.crawler_scene(math.pi * 0.86, wreck=True), b20.crawler_mats(wreck=True), (64, 64), z_top=30,
                     f=4, shadow=(3, 4), colours=24)


def burnt_crawler_b():
    return sdf_piece(b20.crawler_scene(math.pi * 1.27, wreck=True), b20.crawler_mats(wreck=True), (64, 64), z_top=30,
                     f=4, shadow=(3, 4), colours=24)


def walker_scene():
    """A toppled Ares militia walker (about 46 px long): the box hull on its side, the cockpit glass smashed, one leg
    folded under it, the other stretched out behind, burnt drab with the amber band."""
    def scene(p):
        q = rotate_z(p, 0.5)
        items = [(sd_box(q, (0, 0, 6.0), (8.0, 11.0, 5.0), 2.0), 0),
                 (sd_box(q, (0, 10.0, 7.0), (5.0, 3.0, 3.0), 1.2), 3),
                 (sd_box(q, (0, -2.0, 11.2), (8.2, 1.0, 0.6), 0.3), 4),
                 (sd_capsule(q, (5.0, -8.0, 4.0), (12.0, -18.0, 3.0), 2.2), 2),
                 (sd_capsule(q, (12.0, -18.0, 3.0), (10.0, -27.0, 2.0), 1.8), 2),
                 (sd_box(q, (10.0, -29.0, 1.0), (4.0, 3.0, 1.0), 0.5), 2),
                 (sd_capsule(q, (-5.0, -6.0, 4.0), (-11.0, 2.0, 2.0), 2.0), 2),
                 (sd_capsule(q, (-11.0, 2.0, 2.0), (-7.0, 9.0, 1.5), 1.6), 2),
                 (sd_cylinder_z(q, (-3.0, 3.0, 12.0), 2.6, 1.0), 2),
                 (sd_capsule(q, (-3.0, 3.0, 12.6), (-3.0, 15.0, 12.0), 0.8), 2)]
        return union(*items)
    return scene


def cdf_walker_wreck():
    return sdf_piece(walker_scene(), b20.crawler_mats(wreck=True), (72, 72), z_top=30, f=4, shadow=(3, 4), colours=24)


def landslide(size, seed, spread=0.6):
    """A landslide fan of red sand down the inner wall: from its chute at the bottom (the riser it broke from, bare
    rock) it spreads up the screen (down the slope, toward the basin) into a rounded, lobed toe heaped with
    boulders; radial streaks of paler sand on its surface."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    n = raster.fbm(w * f, h * f, 24, seed, octaves=4, period=False)
    lob = raster.fbm(w * f, h * f, 10, seed + 1, octaves=2, period=False)
    ax, ay = w / 2, h - 14.0
    dx, dy = xx - ax, ay - yy                          # dy > 0 up the screen
    r = np.hypot(dx, dy)
    ang = np.arctan2(dx, np.maximum(dy, 1e-3))
    lobes = 0.05 * np.cos(ang * 11 + seed) + 0.04 * np.cos(ang * 17 + 2 * seed) + 0.1 * (n - 0.5)
    side = np.clip(1 - (np.abs(ang) / spread) ** 2, 0, 1)
    reach = (h - 26) * (0.86 + lobes + 0.12 * (lob - 0.5)) * side ** 0.25 + 1e-3
    k = np.clip(1 - r / reach, 0, 1)
    thick = (dy > -6) * side * smooth(0.0, 0.3, k) * (0.4 + 0.6 * (1 - k))
    toe = (dy > 0) * side * np.exp(-((r - reach * 0.9) / 8) ** 2)
    rng = np.random.default_rng(seed + 2)
    boulders = (rng.random(r.shape) > 0.982) * np.clip(toe + 0.2 * thick, 0, 1)
    boulders = np.maximum(boulders, np.roll(boulders, 1, 1) * 0.7)
    scar = np.exp(-((dy - 5) / 6) ** 2) * (np.abs(dx) < 13 + 5 * n)
    hgt = 6.5 * thick + 2.4 * lob * thick + 2.2 * boulders + 1.4 * toe - 2.0 * scar
    streak = (np.sin(ang * 15 + 4 * n) > 0.72) * thick
    alb = ramp(REDSAND[1:], np.clip(0.42 + 0.3 * (n - 0.5) + 0.25 * thick, 0, 1))
    alb = alb * (1 - 0.4 * streak[..., None]) + np.array(OCHRE[3], float) * 0.4 * streak[..., None]
    alb = alb * (1 - 0.3 * boulders[..., None])
    sk = np.clip(scar, 0, 1)[..., None]
    alb = alb * (1 - sk) + ramp(ROCK[1:], np.clip(0.45 + 0.3 * (n - 0.5), 0, 1)) * sk
    cover = smooth(0.0, 0.3, thick + 0.12 * (n - 0.5)) + np.clip(boulders, 0, 1) + scar * 0.9
    return finish_piece(hgt, alb, np.clip(cover, 0, 1), f, colours=28)


def landslide_a():
    return landslide((160, 200), 2201)


def landslide_b():
    return landslide((128, 160), 2205, spread=0.55)


def dune_crest():
    """A sharp dune crest across the field, the t~57 mortars on it (backdrop_l20.dune_ridge's build, its own)."""
    size = (380, 128)
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    crest = h * 0.58 + 8 * np.sin(2 * np.pi * xx / w * 1.4 + 2.1) + 4 * np.sin(2 * np.pi * xx / w * 3.3 + 0.4)
    u = yy - crest
    prof = np.where(u < 0, np.exp(-(u / 26) ** 2), np.exp(-(u / 7) ** 2))
    ends = np.clip(np.minimum(xx, w - xx) / 90, 0, 1) ** 0.8
    n = raster.fbm(w * f, h * f, 24, 2211, octaves=3, period=False)
    hgt = 26 * prof * ends + 1.0 * n + 0.25 * np.sin(yy / 1.6 + 3 * n) * (u < -4)
    cover = smooth(0.0, 0.3, prof * ends + 0.1 * (n - 0.5))
    alb = ramp(OCHRE[1:], np.clip(0.44 + 0.3 * prof + 0.2 * (n - 0.5), 0, 1))
    return finish_piece(hgt, alb, cover, f, colours=28)


def old_crater(size, seed):
    """An old breach crater on the dune sea: a wide shallow bowl, its rim of broken crust plates tilted outward,
    sand filling it in a smooth floor with ripples, a dune tongue spilling over its windward lip; no glow (old)."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    R = w * 0.36
    n = raster.fbm(w * f, h * f, 14, seed, octaves=4, period=False)
    dx, dy = xx - w / 2, yy - h / 2
    ang = np.arctan2(dy, dx)
    r = np.hypot(dx, dy) * (1 + 0.08 * np.sin(3 * ang + seed) + 0.06 * (n - 0.5))
    bowl = -4.5 * np.clip(1 - (r / R) ** 2, 0, 1)
    plates = np.exp(-((r - R * 1.02) / (R * 0.12)) ** 2) * (0.6 + 0.8 * (np.sin(ang * 9 + 4 * n) > 0))
    fill = np.clip(1 - r / (R * 0.75), 0, 1)
    rip = np.sin((dx * 0.25 + dy) / 2.2 + 4 * n) * fill
    tongue = np.exp(-((ang + 2.2) / 0.5) ** 2) * np.clip(1 - np.abs(r - R) / (R * 0.5), 0, 1)
    hgt = bowl * (1 - 0.6 * fill) + 2.6 * plates + 0.25 * rip + 2.0 * tongue + 0.6 * n
    t = np.clip(0.55 + 0.2 * (n - 0.5) + 0.1 * fill - 0.15 * plates, 0, 1)
    alb = ramp(OCHRE[1:], t)
    crust = ramp([(112, 96, 70), (150, 132, 98), (186, 168, 128)], np.clip(0.5 + 0.4 * (n - 0.5), 0, 1))
    pk = np.clip(plates * 1.2, 0, 1)[..., None]
    alb = alb * (1 - pk) + crust * pk
    cover = smooth(R * 1.32, R * 1.12, r + 6 * (n - 0.5))
    return finish_piece(hgt, alb, cover, f, colours=28)


def old_crater_a():
    return old_crater((128, 128), 2221)


def old_crater_b():
    return old_crater((96, 96), 2225)


def sinkhole(size, seed, glow=0.8):
    """A sinkhole in the hollow's crust: crust plates broken in rings and tilting inward, sand streaming down
    radial runnels into a black throat, the throat's lip lit lime from below."""
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    R = w * 0.36
    n = raster.fbm(w * f, h * f, 12, seed, octaves=4, period=False)
    dx, dy = xx - w / 2, yy - h / 2
    ang = np.arctan2(dy, dx)
    r = np.hypot(dx, dy) * (1 + 0.07 * np.sin(4 * ang + seed) + 0.08 * (n - 0.5))
    funnel = -9 * np.exp(-(r / (R * 0.75)) ** 2)
    throat = r < R * 0.3
    ring = np.exp(-((r - R * 0.95) / 2.0) ** 2) + 0.7 * np.exp(-((r - R * 0.66) / 1.6) ** 2)
    run = (np.abs(np.sin(ang * 11 + 3 * n)) < 0.18) & (r < R * 0.95) & ~throat
    hgt = funnel - 1.4 * ring - 0.6 * run + 0.5 * n
    hgt = np.where(throat, -14.0, hgt)
    crust = ramp([(110, 102, 74), (152, 138, 100), (190, 172, 128), (214, 196, 150)],
                 np.clip(0.55 + 0.3 * (n - 0.5) + 0.2 * (r / R - 0.6), 0, 1))
    sand = ramp(OCHRE[2:5], np.clip(0.5 + 0.2 * (n - 0.5), 0, 1))
    alb = np.where(run[..., None], sand, crust)
    alb = np.where(throat[..., None], np.array((14, 12, 10), float), alb)
    lip = np.exp(-((r - R * 0.32) / 1.8) ** 2)
    emission = (LIME / 255)[None, None, :] * (lip * glow)[..., None]
    alb = alb * (1 - 0.5 * lip[..., None]) + LIME * 0.5 * lip[..., None] * glow
    cover = smooth(R * 1.3, R * 1.08, r + 5 * (n - 0.5))
    return finish_piece(hgt, alb, cover, f, colours=28, emission=emission)


def sinkhole_a():
    return sinkhole((112, 112), 2231)


def sinkhole_b():
    return sinkhole((80, 80), 2235, glow=0.6)


def vrell_growth_a():
    return b17.vrell_growth((160, 120), 2241, 0.55)


def vrell_growth_b():
    return b17.vrell_growth((120, 100), 2243, 0.5)


def colossus_sinkhole():
    """The collapsed crater the Colossus leaves at the kill (placed by the game, H.6): a wide funnel slumped in
    three sand terraces, broken crust plates tilted on its rim, sand pouring down runnels into a dark settling
    throat, rock debris, a last dim lime light deep in the throat."""
    size = (224, 224)
    w, h = size
    f = 2
    yy, xx = b15.piece_canvas(size, f)
    R = 88.0
    n = raster.fbm(w * f, h * f, 16, 2251, octaves=4, period=False)
    dx, dy = xx - w / 2, yy - h / 2
    ang = np.arctan2(dy, dx)
    r = np.hypot(dx, dy) * (1 + 0.06 * np.sin(3 * ang + 1.1) + 0.05 * np.sin(7 * ang) + 0.06 * (n - 0.5))
    steps = np.clip(1 - r / R, 0, 1) * 3
    terr = (np.floor(steps) + smooth(0.0, 0.25, steps % 1)) / 3
    hgt = -26 * terr ** 1.2 + 0.8 * n
    plates = np.exp(-((r - R * 1.0) / 8) ** 2) * (0.5 + 0.9 * (np.sin(ang * 13 + 5 * n) > 0.1))
    run = (np.abs(np.sin(ang * 9 + 4 * n)) < 0.16) & (r < R) & (r > R * 0.2)
    throat = r < R * 0.18
    rng = np.random.default_rng(2252)
    rocks = (rng.random(r.shape) > 0.993) * (r < R * 1.1)
    rocks = np.maximum(rocks, np.roll(rocks, 1, 1) * 0.8)
    hgt = hgt + 3.0 * plates - 0.8 * run + 2.4 * rocks
    hgt = np.where(throat, -34.0, hgt)
    sand = ramp(OCHRE[1:5], np.clip(0.55 + 0.25 * (n - 0.5) - 0.25 * terr, 0, 1))
    crust = ramp([(108, 98, 70), (148, 134, 98), (186, 168, 126)], np.clip(0.5 + 0.4 * (n - 0.5), 0, 1))
    pk = np.clip(plates, 0, 1)[..., None]
    alb = sand * (1 - pk) + crust * pk
    alb = np.where(run[..., None], ramp(OCHRE[3:6], np.clip(0.4 + 0.3 * (n - 0.5), 0, 1)), alb)
    alb = alb * (1 - 0.3 * rocks[..., None])
    alb = np.where(throat[..., None], np.array((16, 14, 10), float), alb)
    deep = np.exp(-(r / (R * 0.14)) ** 2) * (n > 0.52)
    emission = (LIME / 255)[None, None, :] * (deep * 0.35)[..., None]
    cover = smooth(R * 1.24, R * 1.08, r + 6 * (n - 0.5))
    return finish_piece(hgt, alb, cover, f, colours=32, emission=emission)


FURROW, FURROW_HEADINGS = 96, 16


def furrow_piece(k):
    """One length of the ridge's sunken furrow at heading k (l20_props' frame and finishing, as the ridge's own
    lengths): a trough ~20 px across along the axis with slumped sand banks either side, constant along it with its
    ends faded, so copies laid every 16 px along the ridge's last track join."""
    a, c, f = l20.local(FURROW, k)
    n = raster.fbm(FURROW * f, FURROW * f, 12, 2261 + k, octaves=3, period=False)
    wide = raster.fbm(FURROW * f, FURROW * f, 24, 2281 + k, octaves=2, period=False)
    c = c + 3 * (wide - 0.5)
    trough = -5.0 * np.exp(-(c / 9) ** 2)
    banks = 2.2 * np.exp(-((np.abs(c) - 15) / 5) ** 2) * (0.7 + 0.6 * (n - 0.5))
    hgt = trough + banks + 0.8 * (n - 0.5)
    cover_along = np.clip(1 - (np.abs(a) - 12) / 30, 0, 1)
    cover = np.clip((np.exp(-(c / 20) ** 2) + 0.2 * (n - 0.5) - 0.18) * 4, 0, 1) * cover_along
    t = np.clip(0.6 - 0.12 * np.exp(-(c / 9) ** 2) + 0.14 * (n - 0.5), 0, 1)
    alb = ramp(OCHRE[1:], t)
    return l20.finish_flat(hgt, alb, cover, f, colours=32)


def colossus_furrow():
    with ProcessPoolExecutor(max_workers=8) as pool:
        frames = list(pool.map(furrow_piece, range(FURROW_HEADINGS)))
    return artkit.quantize_set(frames, 32)


LIFT = (40, 120)


def liftoff_frame():
    """A Vrell shape far off over the basin climbing on its spore trail (heading up): a dark violet-slate body with
    lobed vanes and teal glints at the top, the trail of spores fading below it (stepped translucency)."""
    w, h = LIFT
    f = 2
    yy, xx = b15.piece_canvas(LIFT, f)
    cx, cy = w / 2, 16.0
    dx, dy = xx - cx, yy - cy
    body = (dx / 6.0) ** 2 + ((dy + 1) / 13) ** 2 < 1
    vanes = ((np.abs(dx) - 9 - 0.25 * dy) / 4.0) ** 2 + ((dy - 5) / 7.5) ** 2 < 1
    n = raster.fbm(w * f, h * f, 8, 2271, octaves=3, period=False)
    trail_w = 3 + (yy - cy) * 0.11
    tn = raster.fbm(w * f, h * f, 6, 2272, octaves=3, period=False)
    along = np.clip((yy - cy - 6) / (h - cy - 10), 0, 1)
    trail = (np.abs(dx + 2 * np.sin(yy / 9)) < trail_w * (0.7 + 0.6 * tn)) & (yy > cy + 4)
    ta = trail * (1 - along) ** 1.4 * (0.45 + 0.4 * tn)
    rgb = np.zeros(yy.shape + (3,)) + ramp([(150, 120, 104), (196, 166, 140), (226, 204, 176)], tn)[..., :3]
    a = ta
    solid = body | vanes
    shade = np.clip(0.55 + 0.6 * (-(dx + dy) / 14) + 0.2 * (n - 0.5), 0.25, 1.2)
    bodyc = np.array((70, 40, 84), float) * shade[..., None]
    glint = solid & (np.abs(n - 0.5) < 0.03)
    bodyc = np.where(glint[..., None], np.array((40, 200, 150), float), bodyc)
    rgb = np.where(solid[..., None], bodyc, rgb)
    a = np.where(solid, 1.0, a)
    a[:2 * f], a[-2 * f:], a[:, :2 * f], a[:, -2 * f:] = 0, 0, 0, 0
    small = np.dstack([rgb, a]).reshape(h, f, w, f, 4).mean(axis=(1, 3))
    alpha = np.round(np.clip(small[..., 3], 0, 1) * 4) / 4
    img = raster.to_rgba_image(small[..., :3], alpha * 255)
    return artkit.quantize_set([img], 20)[0]


def vrell_liftoff():
    up = liftoff_frame()
    return [up, up.rotate(180)]


# =========================================================================== registry

L21_PROPS = ("cdf-hauler-wreck",)        # written by tools/art/l21_props.py
GENERATORS = {
    "rim-wall": rim_wall, "inner-wall": inner_wall, "dune-sea": dune_sea, "hollow-crust": hollow_crust,
    "floor-pair": floor_pair, "rim-dust": rim_dust, "basin-haze": basin_haze, "hollow-dust": hollow_dust,
    "scorched-pad": scorched_pad, "cdf-gun-pit": cdf_gun_pit, "burnt-crawler-a": burnt_crawler_a,
    "burnt-crawler-b": burnt_crawler_b, "cdf-walker-wreck": cdf_walker_wreck, "landslide-a": landslide_a,
    "landslide-b": landslide_b, "dune-crest": dune_crest, "old-crater-a": old_crater_a,
    "old-crater-b": old_crater_b, "sinkhole-a": sinkhole_a, "sinkhole-b": sinkhole_b,
    "vrell-growth-a": vrell_growth_a, "vrell-growth-b": vrell_growth_b, "colossus-sinkhole": colossus_sinkhole,
    "vrell-liftoff": vrell_liftoff,
}
SECOND = {name: (lambda n=name: seam_piece(n)) for name in SEAMS}
SECOND["rim-line"] = rim_line
LOCAL = {"colossus-furrow": colossus_furrow}     # runs its own pool
TILE_SETS = {   # id -> (layer, height, drift, note)
    "rim-wall": ("ground", TILE_H, None, "section 1: the basin wall's upper slope, gullies, strata, the retreat's shell craters and scorch"),
    "inner-wall": ("ground", TILE_H, None, "section 2: the inner wall's terraces, four steps a tile, red sand from the slides on the treads"),
    "dune-sea": ("ground", TILE_H, None, "section 3: the dune sea, crests across the screen"),
    "hollow-crust": ("ground", TILE_H, None, "section 4: the lime hollow's crust, cracked in rings round the centre, veins lit lime"),
    "basin-floor": ("ground", FLOOR_H, None, "section 5, the arena: the hollow's floor, repeating every 512 px (H.6), lime veins"),
    "settled-floor": ("ground", FLOOR_H, None, "section 6: the same floor under fallen dust, from the next 512 px seam (the game's)"),
    "rim-dust": ("low-air", TILE_H, 10, "light: dust streaks off the rim, at most 33 %"),
    "basin-haze": ("low-air", TILE_H, 6, "medium: ochre dust banks, at most 26 %"),
    "hollow-dust": ("low-air", TILE_H, 8, "heavy (the hollow, the arena): denser dust lit lime from below, at most 35 %"),
}
PIECES = {   # id -> (layer, note, extra)
    "rim-line": ("ground", "the start: the plain behind the CDF line, pads, berm and trench, the rim's crest, the slope (exact rows)", {}),
    "scorched-pad": ("ground", "a CDF landing pad behind the line, scorched by the last lift-off", {}),
    "cdf-gun-pit": ("ground", "an emplacement of the CDF line on the rim (backdrop_l17.gun_pit)", {}),
    "burnt-crawler-a": ("ground", "a burnt-out CDF crawler of the retreat", {}),
    "burnt-crawler-b": ("ground", "another, turned the other way", {}),
    "cdf-walker-wreck": ("ground", "a toppled, burnt-out militia walker", {}),
    "seam-wall": ("ground", "section 2's seam: the rim slope onto the inner wall's terraces (exact rows)", {}),
    "seam-dunes": ("ground", "section 3's seam: the last terrace onto the dune sea (exact rows)", {}),
    "seam-hollow": ("ground", "section 4's seam: the dune sea onto the hollow's crust (exact rows)", {}),
    "seam-arena": ("ground", "section 5's seam: the hollow's crust onto the arena's floor (exact rows)", {}),
    "seam-settle": ("ground", "section 6's seam, laid by the game at the next 512 px seam with the settled floor (no placed entry)", {}),
    "landslide-a": ("ground", "a landslide fan of red sand down the inner wall", {}),
    "landslide-b": ("ground", "a smaller landslide fan", {}),
    "cdf-hauler-wreck": ("ground", "the CDF hauler's wreck on the inner wall: the t~36 turret nest stands on it (tools/art/l21_props.py)", {}),
    "dune-crest": ("ground", "a sharp dune crest across the field: the t~57 mortars on it", {}),
    "old-crater-a": ("ground", "an old breach crater filling with sand", {}),
    "old-crater-b": ("ground", "a smaller old breach crater", {}),
    "sinkhole-a": ("ground", "a sinkhole in the hollow's crust, its throat lit lime from below", {}),
    "sinkhole-b": ("ground", "a smaller sinkhole", {}),
    "vrell-growth-a": ("ground", "Vrell growth in the hollow", {}),
    "vrell-growth-b": ("ground", "Vrell growth, smaller", {}),
    "colossus-sinkhole": ("ground", "the collapsed crater, laid by the game at the kill (H.6; no placed entry)", {}),
    "colossus-furrow": ("ground", "the ridge's sunken furrow, lengths laid by the game along the ridge's last track at the kill (no placed entry)",
                        {"headings": FURROW_HEADINGS}),
    "vrell-liftoff": ("high-air", "section 6: a Vrell shape climbing out of the frame on its spore trail (scenery)", {"headings": 2}),
}
GAME_PLACED = {"seam-settle", "colossus-sinkhole", "colossus-furrow"}


def render_job(name):
    fn = GENERATORS.get(name) or SECOND[name]
    return name, fn()


def save(name, img):
    OUT.mkdir(parents=True, exist_ok=True)
    if isinstance(img, dict):
        for k, v in img.items():
            save(k, v)
    elif isinstance(img, list):
        for old in OUT.glob(f"{name}_*.png"):
            old.unlink()
        for i, im in enumerate(img):
            artkit.save_png(im, OUT / f"{name}_{i}.png", SOURCE)
        print(f"  {name}: {len(img)} frames {img[0].size[0]}x{img[0].size[1]}")
    else:
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
        print(f"  {name}: {img.size[0]}x{img.size[1]}")


def write_all(wanted):
    alias = {"basin-floor": "floor-pair", "settled-floor": "floor-pair"}
    wanted = {alias.get(w, w) for w in wanted}
    tiles = {"rim-wall", "inner-wall", "dune-sea", "hollow-crust", "floor-pair"}
    first = [n for n in GENERATORS if not wanted or n in wanted]
    second = [n for n in SECOND if not wanted or n in wanted or wanted & tiles]
    with ProcessPoolExecutor(max_workers=12) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
        for name, img in pool.map(render_job, second):
            save(name, img)
    for name, fn in LOCAL.items():
        if not wanted or name in wanted:
            save(name, fn())


# =========================================================================== placements and the block

def image_of(name, frame=0):
    p = OUT / f"{name}.png"
    return p if p.exists() else OUT / f"{name}_{frame}.png"


def size_of(name):
    return list(Image.open(image_of(name)).size)


SURVEY = (44.0, 80)                     # the survey crawler (the secret): its t and x (the README)
HAULER = (36.0, 330)                    # the hauler's wreck under the turret nest


def survey_row():
    """The survey crawler's centre: its inner-wall tile row from the bottom."""
    return jr(scroll_at(SURVEY[0]) + MID) % TILE_H


def lift_paths():
    """Section 6's Vrell lift-offs: (t at which it starts climbing, x); each rests on the far basin (scrolling down
    with high-air) until then, then climbs 80 px/s up its layer (net 30 px/s up the screen) out of the top."""
    return ((248.5, 120), (252.0, 350), (256.5, 210), (261.0, 420), (266.0, 70))


def placements():
    out = []
    out.append(("# 1. the start: the CDF line on the rim (exact rows; its bottom below the screen at t=0)",
                {"piece": "rim-line", "t": t_for_bottom("ground", LINE_BOTTOM, LINE_H), "x": 240}))
    for x, p in PADS:
        out.append(("", {"piece": "scorched-pad", "t": t_centre(p), "x": x}))
    for x, p in PITS:
        out.append(("", {"piece": "cdf-gun-pit", "t": t_centre(p), "x": x}))
    out.append(("# 1. the retreat's wrecks down the slope", {"piece": "burnt-crawler-a", "t": 6.4, "x": 404}))
    for name, t, x in (("burnt-crawler-b", 9.8, 86), ("cdf-walker-wreck", 13.6, 300), ("burnt-crawler-a", 16.8, 150),
                       ("cdf-walker-wreck", 19.2, 432), ("burnt-crawler-b", 21.0, 250)):
        out.append(("", {"piece": name, "t": t, "x": x}))
    first = True
    for name, (s, _, _, _) in SEAMS.items():
        if s is None:
            continue
        g = seam("ground", s)
        out.append(("# the section seams (exact rows; re-render them after the data moves a section)" if first else "",
                    {"piece": name, "t": t_for_bottom("ground", g - SEAM_H // 2, SEAM_H), "x": 240}))
        first = False
    out.append(("# 2. the inner wall: slides, the hauler's wreck (the turret nest on it), a crawler",
                {"piece": "landslide-a", "t": 25.4, "x": 384}))
    for name, t, x in (("landslide-b", 29.6, 116), ("cdf-hauler-wreck", HAULER[0], HAULER[1]), ("burnt-crawler-b", 39.2, 160),
                       ("landslide-b", 41.2, 300), ("landslide-a", 47.6, 400)):
        out.append(("", {"piece": name, "t": t, "x": x}))
    out.append(("# 3. the dune sea: old breach craters, the mortars' crest", {"piece": "old-crater-a", "t": 53.2, "x": 120}))
    for name, t, x in (("dune-crest", 57.0, 250), ("old-crater-b", 61.6, 396), ("old-crater-a", 70.4, 330),
                       ("old-crater-b", 75.6, 86)):
        out.append(("", {"piece": name, "t": t, "x": x}))
    out.append(("# 4. the lime hollow: sinkholes and Vrell growth", {"piece": "sinkhole-a", "t": 81.6, "x": 384}))
    for name, t, x in (("vrell-growth-a", 85.0, 236), ("sinkhole-b", 88.4, 96), ("sinkhole-a", 94.8, 300),
                       ("vrell-growth-b", 97.2, 64), ("sinkhole-b", 101.4, 420)):
        out.append(("", {"piece": name, "t": t, "x": x}))
    first = True
    for t0, x in lift_paths():
        top = scroll_at(t0) * FACTORS["high-air"] + 390      # its centre 150 px below the top edge at t0
        out.append(("# 6. the Vrell lifting off far over the basin (high-air, script time from t=245; the game shifts "
                    "them by the kill's jump, pending HA)" if first else "",
                    {"piece": "vrell-liftoff", "t": t_centre(top, "high-air"), "x": x,
                     "path": [[t0, 0, 0], [round(t0 + 16, 2), 0, 1280]]}))
        first = False
    return out


def write_proposal():
    places = placements()
    flow = k8.flow
    lines = ["backdrop:",
             "  # Presentation only: Dust Colossus (tools/art/backdrop_l21.py, from mars-hellas-r36-a, Level 20's style).",
             "  # The ground is opaque; no deep layer. Seams sit at exact rows: re-render them (`backdrop_l21.py",
             "  # seam-wall seam-dunes seam-hollow seam-arena`) after the data moves a section. The arena's tile repeats",
             "  # every 512 px; section 6's starts at the next 512 px seam with seam-settle, and the collapsed crater and",
             "  # the furrow are laid at the kill: the game's (H.6), declared here without a placed entry.",
             f"  scroll_factors: {flow({'far': 0.5, **FACTORS})}   # far unused (no deep layer), given as the loader asks",
             f"  ramp: {RAMP}",
             f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
             "  atmosphere:"]
    notes = {"clear": "section 6, the settling dust: haze only (the heavy dust ramps out)",
             "light": "the rim and the inner wall: dust streaks off the rim",
             "medium": "the dune sea: ochre dust banks",
             "heavy": "the lime hollow and the arena: dust lit lime from below"}
    for k, v in ATMOSPHERE.items():
        lines.append(f"    {k}: {flow(v)}   # {notes[k]}")
    lines.append("  tile_sets:")
    for name, (layer, h, drift, note) in TILE_SETS.items():
        spec = {"layer": layer, "height": h}
        if drift is not None:
            spec["drift"] = drift
        lines.append(f"    {name}: {flow(spec)}   # {note}")
    lines.append("  pieces:")
    for name, (layer, note, extra) in PIECES.items():
        lines.append(f"    {name}: {flow({'layer': layer, 'size': size_of(name), **extra})}   # {note}")
    lines.append("  placed:")
    for comment, p in places:
        if comment:
            lines.append(f"    {comment}")
        lines.append(f"    - {flow(p)}")
    lines.append("  # Section tiles (`sections[].tiles`): " + "; ".join(
        f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    lines.append("  # Atmospheres: 1, 2 light; 3 medium; 4, 5 heavy; 6 clear.")
    lines.append(f"  # The survey crawler (t {SURVEY[0]:g}, x {SURVEY[1]}) lies on a tread of inner-wall; the hauler's wreck "
                 f"at t {HAULER[0]:g}, x {HAULER[1]}: its deck spots in briefs/l21-art-names.md.")
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== check

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def riser_distance(row):
    """The survey crawler's distance (rows) to the nearest inner-wall riser, the risers' wobble included."""
    xs = np.arange(SURVEY[1] - 30, SURVEY[1] + 31, dtype=np.float64)
    best = math.inf
    for k, r in enumerate(RISERS):
        line = r + riser_offset(xs, k)
        d = np.abs(((line - row + TILE_H / 2) % TILE_H) - TILE_H / 2)
        best = min(best, float(d.min()))
    return best


def check():
    problems = []
    for name, (layer, h, _, _) in TILE_SETS.items():
        im = cached(name)
        if im.shape[:2] != (h, W):
            problems.append(f"{name}: {im.shape[1]}x{im.shape[0]} is not {W}x{h}")
        score = k8.wrap_score(im)
        print(f"  wrap {name}: rows {score:.2f}")
        if score > 1.5:
            problems.append(f"{name}: a line where it repeats ({score:.2f})")
        if layer == "ground" and (im[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    for name, (layer, _, extra) in PIECES.items():
        n = extra.get("headings") or extra.get("frames") or 1
        for i in range(n):
            stem = name if n == 1 else f"{name}_{i}"
            path = OUT / f"{stem}.png"
            if not path.exists():
                problems.append(f"{stem}: missing (tools/art/l21_props.py writes cdf-hauler-wreck)")
                continue
            a = cached(stem)[..., 3] > 0
            full_width = name in SEAMS or name == "rim-line"
            top_or_bottom = a[0].any() or (a[-1].any() and name != "rim-line")
            if top_or_bottom:
                problems.append(f"{stem}: its top or bottom row is opaque")
            if not full_width and (a[:, 0].any() or a[:, -1].any()):
                problems.append(f"{stem}: a side column is opaque")
    placed = [p for _, p in placements()]
    for name, (s, _, _, _) in SEAMS.items():
        if s is None:
            continue
        p = next(p for p in placed if p["piece"] == name)
        if piece_bottom(p, SEAM_H) != seam("ground", s) - SEAM_H // 2:
            problems.append(f"{name} is not centred on its seam")
    p = next(p for p in placed if p["piece"] == "rim-line")
    if piece_bottom(p, LINE_H) != LINE_BOTTOM:
        problems.append("rim-line's bottom is not at its row")
    if seam("ground", 1) - SEAM_H // 2 < LINE_BOTTOM + LINE_H:
        problems.append("rim-line reaches into seam-wall")
    for name in ("basin-floor", "settled-floor"):
        if TILE_SETS[name][1] != FLOOR_H:
            problems.append(f"{name}: not on the arena's 512 px period")
    d = riser_distance(survey_row())
    print(f"  the survey crawler: {d:.0f} rows from the nearest riser (tile row {survey_row()})")
    if d < 40:
        problems.append(f"the survey crawler lies on a riser ({d:.0f} rows)")
    for p in placed:
        if p["piece"] in GAME_PLACED:
            problems.append(f"{p['piece']} is the game's: no placed entry")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"check: {len(problems)} problem(s)")
    return not problems


# =========================================================================== review

def over(dst, src, x, y):
    h, w = src.shape[:2]
    y0, y1 = max(0, y), min(dst.shape[0], y + h)
    x0, x1 = max(0, x), min(dst.shape[1], x + w)
    if y1 <= y0 or x1 <= x0:
        return
    s = src[y0 - y:y1 - y, x0 - x:x1 - x]
    a = s[..., 3:4] / 255
    dst[y0:y1, x0:x1] = s[..., :3] * a + dst[y0:y1, x0:x1] * (1 - a)


def kill_ground():
    return scroll_at(KILL)


def next_seam():
    """Section 6's first row: the next multiple of 512 at or above the top edge at the kill (the game's, H.6)."""
    return int(math.ceil((kill_ground() + SCREEN) / FLOOR_H) * FLOOR_H)


def clock(t):
    """The review's real time t -> (script time, ground scroll): a kill at KILL jumps the clock to the hold, not
    the ground; section 6 runs on from the ground at the kill."""
    if t <= KILL:
        return t, scroll_at(t)
    ts = HOLD + (t - KILL)
    return ts, kill_ground() + scroll_at(ts) - scroll_at(HOLD)


def section_of(ts):
    for i, s in enumerate(SECTIONS):
        if ts < s["end"] or i == len(SECTIONS) - 1:
            return i


def furrow_track():
    """The ridge's last track at the kill (ground x, position), tail first, ending at the crater: a stand-in for
    the boss's path (the game's)."""
    cx, cp = 250.0, kill_ground() + SCREEN - 220
    pts = []
    for i in range(30):
        s = i / 29
        pts.append((cx + 260 * (1 - s) + 40 * math.sin(s * 3.1), cp + 150 * (1 - s) ** 1.3 - 90 * math.sin(s * 2.4)))
    out, run = [pts[0]], 0.0
    for a, b in zip(pts, pts[1:]):
        seg = math.hypot(b[0] - a[0], b[1] - a[1])
        steps = int(seg // 4) + 1
        for j in range(1, steps + 1):
            x = a[0] + (b[0] - a[0]) * j / steps
            y = a[1] + (b[1] - a[1]) * j / steps
            run += seg / steps
            if run >= l20.SPACING:
                out.append((x, y))
                run = 0.0
    return out[:-4], (cx, cp)


def frame_at(t, placed):
    ts, view = clock(t)
    frame = np.zeros((SCREEN, W, 3))
    rows = jr(view) + (SCREEN - 1 - np.arange(SCREEN))
    bounds = [seam("ground", s) for s in range(1, len(SECTIONS))]
    if t > KILL:
        bounds[len(SECTIONS) - 2] = next_seam()
    else:
        bounds[len(SECTIONS) - 2] = 10 ** 9
    for s in range(len(SECTIONS)):
        lo = -math.inf if s == 0 else bounds[s - 1]
        hi = bounds[s] if s < len(bounds) else math.inf
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(SECTION_TILES[s][0])
            hh = img.shape[0]
            frame[sel] = img[[hh - 1 - (int(r) % hh) for r in rows[sel]]][..., :3]
    shift = scroll_at(HOLD) - kill_ground() if t > KILL else 0.0
    ground_pieces = [p for p in placed if PIECES[p["piece"]][0] == "ground"]
    if t > KILL:
        ground_pieces = ground_pieces + [{"piece": "seam-settle", "pos": next_seam(), "x": 240}]
    for p in ground_pieces:
        img = cached(p["piece"])
        h, w = img.shape[:2]
        bottom = p["pos"] - h // 2 if "pos" in p else piece_bottom(p, h)
        top_y = SCREEN - (bottom + h - jr(view))
        if -h < top_y < SCREEN:
            over(frame, img, jr(p["x"] - w / 2), top_y)
    if t > KILL:
        track, (cx, cp) = furrow_track()
        for i, (x, pos) in enumerate(track):
            j = min(i + 1, len(track) - 1)
            k0 = max(j - 2, 0)
            dx, dpos = track[j][0] - track[k0][0], track[j][1] - track[k0][1]
            k = l20.nearest_heading(dx, -dpos)
            img = cached(f"colossus-furrow_{k}")
            over(frame, img, jr(x - FURROW / 2), jr(SCREEN - (pos - view) - FURROW / 2))
        img = cached("colossus-sinkhole")
        over(frame, img, jr(cx - img.shape[1] / 2), jr(SCREEN - (cp - view) - img.shape[0] / 2))
    i = section_of(ts)
    look = SECTIONS[i].get("atmosphere", "light")
    weight = 1.0
    if t > KILL:
        prev = ATMOSPHERE[SECTIONS[ARENA].get("atmosphere", "heavy")]
        prog = np.clip((ts - HOLD) / RAMP + 0.5, 0, 1)
        weight = 1 - prog * prog * (3 - 2 * prog)
        if prev.get("banks") and weight > 0:
            bank_over(frame, prev["banks"], view, t, weight)
    atm = ATMOSPHERE[look]
    if atm.get("banks") and t <= KILL:
        bank_over(frame, atm["banks"], view, t, 1.0)
    for p in placed:
        if PIECES[p["piece"]][0] != "high-air":
            continue
        img = cached(f"{p['piece']}_0")
        h, w = img.shape[:2]
        f = FACTORS["high-air"]
        hv = jr((view + shift) * f)
        dy = path_dy(p, ts)
        bottom = piece_bottom(p, h, "high-air") + jr(dy)
        top_y = SCREEN - (bottom + h - hv)
        if -h < top_y < SCREEN:
            over(frame, img, jr(p["x"] - w / 2), top_y)
    return np.clip(frame, 0, 255).astype(np.uint8)


def path_dy(p, ts):
    pts = p["path"]
    if ts <= pts[0][0]:
        return pts[0][2]
    if ts >= pts[-1][0]:
        return pts[-1][2]
    a, b = pts[0], pts[1]
    return a[2] + (b[2] - a[2]) * (ts - a[0]) / (b[0] - a[0])


def bank_over(frame, name, view, t, weight):
    img = cached(name).copy()
    img[..., 3] *= weight
    lr = jr(view * FACTORS["low-air"]) + (SCREEN - 1 - np.arange(SCREEN))
    bank = np.roll(img[[TILE_H - 1 - (int(r) % TILE_H) for r in lr]], int(TILE_SETS[name][2] * t) % W, 1)
    over(frame, bank, 0, 0)


REVIEW_TIMES = [1.0, 10.0, 21.0, 28.0, 36.0, 47.0, 57.0, 66.0, 76.0, 84.0, 96.0, 104.0, 112.0, 122.0, 180.0,
                KILL + 3, KILL + 14, KILL + 24, KILL + 34, KILL + 44]


def review():
    placed = [p for _, p in placements()]
    scale = 0.5
    cw, ch = int(W * scale), int(SCREEN * scale)
    cols = 10
    sheet = raster.sheet(16 + cols * (cw + 10), 60 + 2 * (ch + 24) + 300,
                         "LEVEL 21 DUST COLOSSUS - BACKDROP (FINAL R44)", "PRODUCTION ART, M7 PART H - R44")
    raster.draw_text(sheet, 16, 38, f"THE PLAY FIELD (GROUND, LOW-AIR DUST, HIGH-AIR) AT 0.5X; NO UNITS. A KILL AT "
                     f"T={KILL:g} (REAL): THE CLOCK JUMPS TO {HOLD:g}, THE GROUND RUNS ON; SECTION 6 FROM THE NEXT 512 PX "
                     "SEAM, THE CRATER AND FURROW LAID AT THE KILL (STAND-INS FOR THE GAME'S)", raster.LABEL)
    for i, t in enumerate(REVIEW_TIMES):
        fr = Image.fromarray(frame_at(t, placed), "RGB")
        x, y = 16 + (i % cols) * (cw + 10), 54 + (i // cols) * (ch + 24)
        sheet.paste(fr.resize((cw, ch), Image.BOX), (x, y))
        ts, _ = clock(t)
        label = f"T={t:g} {SECTIONS[section_of(ts)]['name'].upper()}" if t <= KILL else \
            f"KILL+{t - KILL:g}S (T={ts:g})"
        raster.draw_text(sheet, x, y + ch + 4, label, raster.LABEL_DIM)
    y = 54 + 2 * (ch + 24) + 6
    raster.draw_text(sheet, 16, y, "PIECES AT 0.5X (THE GAME'S: SEAM-SETTLE, COLOSSUS-SINKHOLE, COLOSSUS-FURROW 0,4,8,12; "
                     "VRELL-LIFTOFF AT 1X)", raster.LABEL)
    x = 16
    names = ["scorched-pad", "cdf-gun-pit", "burnt-crawler-a", "burnt-crawler-b", "cdf-walker-wreck", "landslide-a",
             "landslide-b", "cdf-hauler-wreck", "dune-crest", "old-crater-a", "old-crater-b", "sinkhole-a",
             "sinkhole-b", "vrell-growth-a", "vrell-growth-b", "colossus-sinkhole", "colossus-furrow_0",
             "colossus-furrow_4", "colossus-furrow_8", "colossus-furrow_12", "vrell-liftoff_0", "seam-settle"]
    row_y = y + 16
    for name in names:
        img = Image.open(OUT / f"{name}.png").convert("RGBA")
        if name != "vrell-liftoff_0":
            img = img.resize((max(1, img.width // 2), max(1, img.height // 2)), Image.BOX)
        if x + img.width > sheet.width - 16:
            x = 16
            row_y += 130
        sheet.alpha_composite(img, (x, row_y))
        raster.draw_text(sheet, x, row_y + img.height + 2, name.upper()[:18], raster.LABEL_DIM)
        x += max(img.width, 70) + 12
    CONCEPT.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(REVIEW, optimize=True)
    times = list(np.arange(0.0, 108.0, 0.6)) + list(np.arange(108.0, 130.0, 1.0)) + \
        list(np.arange(KILL - 4, KILL + 47, 0.6))
    gif = [Image.fromarray(frame_at(float(t), placed), "RGB").resize((240, 270), Image.BOX) for t in times]
    write_gif(gif, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif")


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    if "--proposal" in sys.argv[1:]:
        write_proposal()
    elif "--review" in sys.argv[1:]:
        review()
    elif "--check" in sys.argv[1:]:
        sys.exit(0 if check() else 1)
    else:
        write_all(args)
        write_proposal()
        ok = check()
        review()
        sys.exit(0 if ok else 1)
