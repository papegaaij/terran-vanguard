#!/usr/bin/env python3
"""Production art: the Level 33 backdrop, the Leech Field (design/campaign/act-5-the-belt/level-33-leech-field; M9
part F batch, concept round 66, step ART-F2).

Straight to production from round 60's belt look (the L33 rock with the Vrell growth of tools/concept/enemies_r60.py
``rock_bg``: black-violet lumps, violet and teal specks) in the lighter belt grade of round 60's A.18 a: the belt's
top-left sun on warm brown-grey rock, a step lighter than Act 4 (Q11 b) so the Talons' gunmetal reads. Built on
tools/art/backdrop_l31.py's rock faces (``face_field``, ``shade``, ``blob_mask``, imported unchanged) and
backdrop_l30.py's space kit (dust-band space, grit, far rocks, hazing, strobes). Posterized per image (median cut),
wide gradients ordered-dithered; nothing lit is mirrored.

The ground has **no tile set** (as Level 31's): open space is the deep layer (the base) and the asteroid surfaces are
ground pieces on it; the Rail Bunkers stand on cut pads on the faces (the pads' x/y are in ``FACES``) and *Claim 114*
on a cut ledge of the feeding ground's claim rock. The growth is still (the motion budget's third element); only the
leech sacs pulse (``leech-sacs``, at most two animated pieces on the screen).

The layers (``scroll_factors``): ``deep`` 0.12 (space: the sunlit dust band, the Sun as a bright point at the start,
far rocks, Ceres's light ahead at the end), ``ground`` 1 (the asteroid faces, the sacs), ``low-air`` 1.35 (the drifting
wrecks of leeched Talons in the feeding ground, dimmed), ``high-air`` 2.2 (sparse close grit).

Tile sets (480 wide, wrapping):
  field-space  deep 480x1024: space a step lighter than Act 4, the sunlit warm dust band, stars, far rock specks
  field-dust   high-air 480x960, transparent: sparse close grit and pale dust motes
Pieces (the layer named; ``frames`` loop at their fps):
  sun-point         deep 96x96: the Sun as a bright point with a short cross flare (section 1)
  far-rock-a, -b    deep 120x100 / 84x72: distant lit rocks, hazed
  ceres-ahead       deep 300x300: Ceres ahead, its sunlit limb a broad warm crescent (section 6)
  edge-rock-a, -b   ground 260x220 / 200x170: small bare sunlit asteroid faces (the field's edge, section 1)
  grown-face-1      ground 480x460: a large face overgrown, a bunker pad on its left shoulder (section 2's bunker)
  grown-face-2      ground 480x420: a large face overgrown (sections 2 and 5, no pad)
  ridge-1, ridge-2  ground 480x400: the bunker ridge, a long rock band across the field with the growth on its flanks,
                    a bunker pad at each end (the pairs of t≈68 and t≈86; ridge-2 a third at x 240 for hard's trio)
  feeding-face      ground 480x520: the growth at its thickest; the claim rock on the right with its cut ledge
                    (*Claim 114*'s place, x 380)
  nest-face-1, -2, -3  ground 480x460 / 480x440 / 480x420: the growth with leech nests (burrows lit violet, sac
                    clusters); bunker pads (the pair of t≈132 on -1 and hard's third at x 240 between them, the single
                    of t≈150 on -2; -3 has none)
  clear-face-a, -b  ground 480x400 / 260x220: bare rock lit gold, the growth gone (section 6)
  leech-sacs        ground 64x48, 4 frames at 2 fps: a cluster of leech sacs pulsing violet (on the grown faces)
  talon-wreck-a, -b low-air 64x64: a leeched Talon's drifting wreck, dark, its leech dead on its back, dimmed
                    (the feeding ground's scenery; from tools/art/belt_units.py's and belt_units_f.py's frames)

Review: design/campaign/act-5-the-belt/level-33-leech-field/concept/backdrop-final-r66-a.png (eight screens with
stand-ins: the bunkers on their pads, Claim 114, the rocks, the ship; the pieces; the tiles) and .gif (t 60-70 at half
size: the ridge and its first pair). ``--proposal`` writes the ``backdrop:`` block, the sections, the ground targets'
places (the bunkers on the pads, Claim 114), the debris block (the grown rocks) and the nav buoy's drop to the level's
concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections. Re-run after
the data moves a section.
Run: python3 tools/art/backdrop_l33.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU

import backdrop_l30 as l30  # noqa: E402  (tools/art: the belt's space kit)
import backdrop_l31 as l31  # noqa: E402  (tools/art: the rock faces)
import belt_units as bu  # noqa: E402  (tools/art: paste, the held-sprite loader)
import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l33.py"
BATCH = "M9 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r66"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-33-leech-field"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-33"
W, SCREEN = 480, 540
SPACE_H, DUST_H = 1024, 960
FACTORS = {"deep": 0.12, "ground": 1.0, "low-air": 1.35, "high-air": 2.2}
WARM_ROCK = l31.WARM_ROCK
GROWTH_DARK = np.array((24, 11, 30), float)
GROWTH_LIT = np.array((70, 44, 84), float)
VIOLET = np.array(e60.VIOLET, float)
TEAL = np.array(e60.TEAL, float)

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
TILES = ["field-space", "field-dust"]
PROPOSED_SECTIONS = [
    {"name": "Field Edge", "end": 24, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Grown Rocks", "end": 60, "atmosphere": "clear", "tiles": TILES},
    {"name": "Bunker Ridge", "end": 95, "speed": 130, "ramp": 2, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Feeding Ground", "end": 118, "speed": 150, "ramp": 2, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Swarm", "end": 163, "atmosphere": "clear", "tiles": TILES},
    {"name": "Clear Rock", "end": 178, "atmosphere": "clear", "tiles": TILES},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 150)) if DATA else 150.0
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


# =========================================================================== the faces (ground)

def growth_field(w, h, mask, seed, density):
    """Where the growth lies (0..1) inside the face: fbm blobs thresholded by ``density``, thicker away from the
    face's sunlit top-left rim, and its lump heights (px)."""
    n = raster.fbm(w, h, 40, seed + 11, octaves=4, period=False)
    fine = raster.fbm(w, h, 7, seed + 13, octaves=2, period=False)
    inside = np.array(Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(10))) / 255.0
    cover = np.clip((n + 0.35 * fine - (1.0 - density)) * 5.0, 0, 1) * (inside > 0.6)
    lumps = cover * (6 + 9 * np.clip(fine + 0.5, 0, 1))
    return cover, lumps


def grown_face(w, h, cx, cy, rx, ry, seed, density=0.45, pads=(), ledge=None, nests=(), sacs=0, gold=0.0):
    """A sunlit asteroid face (backdrop_l31.py's heightfield and shading) with the Leech Field's growth: black-violet
    lumps in the hollows, a violet sheen where they face the sun, violet and teal specks, baked leech sacs; the bunker
    ``pads`` cut flat and clear of growth with a dark rim and two amber marker lamps (Ascendancy work), the claim's
    ``ledge`` a cut flat with no lamps, ``nests`` burrows lit violet; ``gold`` warms the light (the clear rock)."""
    mask = l31.blob_mask(w, h, cx, cy, rx, ry, seed)
    mask[:2], mask[-2:] = 0, 0
    hgt = l31.face_field(w, h, mask, seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    cover, lumps = growth_field(w, h, mask, seed, density) if density > 0 else (np.zeros((h, w)), np.zeros((h, w)))
    flats = list(pads) + ([ledge] if ledge else [])
    for x, y in flats:                                          # cut the pads flat, clear the growth off them
        d = np.hypot(xx - x, yy - y)
        k = np.clip((44 - d) / 8, 0, 1)
        hgt = hgt * (1 - k) + np.mean(hgt[int(y) - 4:int(y) + 4, int(x) - 4:int(x) + 4]) * k
        cover = cover * np.clip((d - 40) / 10, 0, 1)
        lumps = lumps * np.clip((d - 40) / 10, 0, 1)
    for x, y in nests:                                          # burrows: a pit in the growth
        d = np.hypot(xx - x, (yy - y) * 1.3)
        hgt -= np.where(d < 9, 10 * (1 - d / 9), 0)
        cover = np.maximum(cover, np.clip((26 - d) / 6, 0, 1) * (mask > 0.5))
    total = hgt + lumps
    light = l31.shade(total)
    tex = 0.5 + 0.5 * raster.fbm(w, h, 12, 3171, octaves=3, period=False)
    lum = np.clip(light * (0.75 + 0.35 * tex), 0, 1.25)
    rgb = raster.ramp(WARM_ROCK, np.clip(lum * 0.92, 0, 1))
    if gold:
        rgb = rgb * (1 + gold * np.array((0.12, 0.02, -0.18))) + gold * 14
    g = np.clip(lum, 0, 1.2)[..., None]
    grow = GROWTH_DARK * (0.55 + 0.45 * g) + GROWTH_LIT * np.clip(g - 0.7, 0, 1) * 1.3
    rgb = rgb * (1 - cover[..., None]) + grow * cover[..., None]
    rng = np.random.default_rng(seed + 17)
    op = mask > 0.5
    ys, xs = np.nonzero((cover > 0.8) & op)
    for i in rng.choice(len(xs), size=min(len(xs), int(len(xs) / 260)), replace=False) if len(xs) else []:
        rgb[ys[i], xs[i]] = (140, 70, 230) if rng.uniform() < 0.6 else (0, 200, 130)
    pad = np.pad(op, 1)                                         # the face's lit top-left edge and its dark far edge
    tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
    br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
    rgb[tl] = rgb[tl] * 0.5 + np.array((214, 196, 168)) * 0.5
    rgb[br] = rgb[br] * 0.55
    for x, y in pads:                                           # the pad's ring (a cut edge)
        d = np.hypot(xx - x, (yy - y) * 1.1)
        ring = (np.abs(d - 40) < 1.0) & op
        rgb[ring] *= 0.45
        flat = (d < 40) & op
        rgb[flat] = rgb[flat] * 0.75 + np.array((140, 130, 118)) * 0.25
    img = l30.posterize(rgb, 56, np.where(op, 255, 0).astype(np.uint8))
    d = ImageDraw.Draw(img)
    for x, y in pads:
        for lx in (x - 34, x + 34):
            d.point((lx, y + 14), fill=(255, 196, 80, 255))
    if ledge:                                                   # the prospector's cut: survey stakes
        x, y = ledge
        for sx, sy in ((x - 30, y - 22), (x + 30, y - 22), (x - 30, y + 24), (x + 30, y + 24)):
            d.rectangle([sx - 1, sy - 1, sx + 1, sy + 1], fill=(220, 200, 120, 255))
    for k in range(sacs):                                        # baked sacs (still): violet bulbs in the growth
        ys, xs = np.nonzero((cover > 0.95) & op)
        if not len(xs):
            break
        i = rng.integers(len(xs))
        img = sac_cluster(img, xs[i], ys[i], 0.35, rng)
    for x, y in nests:
        img = l30.strobe(img, x, y, 0.55, tuple(int(c) for c in VIOLET), 8)
        img = sac_cluster(img, x + 12, y - 8, 0.4, rng)
    return img


def sac_cluster(img, x, y, k, rng, n=4):
    """A few leech sacs: dark-violet bulbs with a lit core (k: the pulse 0..1)."""
    d = ImageDraw.Draw(img)
    for _ in range(n):
        sx, sy, r = x + rng.uniform(-6, 6), y + rng.uniform(-4, 4), rng.uniform(2.0, 3.6)
        d.ellipse([sx - r, sy - r * 0.8, sx + r, sy + r * 0.8], fill=(54, 20, 70, 255))
        c = tuple(int(v) for v in (60 + 100 * k, 30 + 50 * k, 90 + 160 * k))
        d.ellipse([sx - r * 0.5, sy - r * 0.4, sx + r * 0.3, sy + r * 0.2], fill=c + (255,))
    return img


def leech_sacs(i, w=64, h=48):
    """The animated cluster: six sacs on a growth mound, their cores pulsing (4 steps), a faint violet halo."""
    k = (0.2, 0.6, 1.0, 0.55)[i]
    img = Image.new("RGBA", (w, h))
    yy, xx = np.mgrid[0:h, 0:w].astype(float)
    mound = ((xx - w / 2) / 26) ** 2 + ((yy - h / 2) / 16) ** 2 < 1
    a = np.zeros((h, w, 4))
    lit = np.clip(1.2 - ((xx - w / 2 + 8) / 26) ** 2 - ((yy - h / 2 + 6) / 16) ** 2, 0, 1)
    a[mound, :3] = GROWTH_DARK * 0.8 + GROWTH_LIT * 0.5 * lit[mound, None]
    a[mound, 3] = 255
    img = Image.fromarray(a.astype(np.uint8), "RGBA")
    halo = np.exp(-(((xx - w / 2) / 20) ** 2 + ((yy - h / 2) / 13) ** 2)) * 0.35 * k
    h_img = np.zeros((h, w, 4))
    h_img[..., :3] = VIOLET * halo[..., None]
    h_img[..., 3] = np.clip(halo * 255 * 1.6, 0, 255) * mound
    img.alpha_composite(Image.fromarray(h_img.astype(np.uint8), "RGBA"))
    rng = np.random.default_rng(3361)
    return sac_cluster(img, w / 2, h / 2, k, rng, n=6)


FACES = {
    # name: size, the face's ellipse (cx, cy, rx, ry), seed, growth density, bunker pads, claim ledge, nests, sacs, gold
    "edge-rock-a": ((260, 220), (130, 110, 112, 92), 3301, 0.0, [], None, [], 0, 0.0),
    "edge-rock-b": ((200, 170), (100, 85, 86, 70), 3303, 0.15, [], None, [], 0, 0.0),
    "grown-face-1": ((480, 460), (240, 230, 290, 200), 3311, 0.38, [(112, 300)], None, [], 4, 0.0),
    "grown-face-2": ((480, 420), (250, 210, 300, 180), 3313, 0.45, [], None, [], 5, 0.0),
    "ridge-1": ((480, 400), (240, 200, 330, 120), 3321, 0.34, [(92, 196), (388, 196)], None, [], 3, 0.0),
    "ridge-2": ((480, 400), (240, 200, 330, 125), 3323, 0.38, [(84, 210), (396, 190)], None, [], 3, 0.0),
    "feeding-face": ((480, 520), (230, 260, 310, 230), 3331, 0.6, [], (380, 270), [], 7, 0.0),
    "nest-face-1": ((480, 460), (240, 230, 300, 200), 3341, 0.52, [(100, 250), (380, 250)], None,
                    [(200, 140), (300, 330), (320, 160)], 3, 0.0),   # FIX3: the middle nest off hard's pad
    "nest-face-2": ((480, 440), (240, 220, 290, 190), 3343, 0.52, [(320, 230)], None, [(140, 160), (190, 320)], 3,
                    0.0),
    "nest-face-3": ((480, 420), (250, 210, 290, 180), 3345, 0.55, [], None, [(160, 200), (330, 150), (290, 300)], 3,
                    0.0),
    "clear-face-a": ((480, 400), (240, 200, 280, 170), 3351, 0.0, [], None, [], 0, 0.6),
    "clear-rock-b": ((260, 220), (130, 110, 110, 92), 3353, 0.0, [], None, [], 0, 0.6),
}


# Round 66's C.7 b (the user): hard's third bunker of the t≈86 and t≈132 nests stands on a pad of its own at x 240
# between the pair's (the face's local x, y; y where the bunker of data.yaml's hard `at` stands: t 86.1 and t 132).
HARD_PADS = {"ridge-2": [(240, 197)], "nest-face-1": [(240, 250)]}
HARD_AT = {"ridge-2": [(86.1, 240)], "nest-face-1": [(132.0, 240)]}


def face(name):
    (w, h), (cx, cy, rx, ry), seed, density, pads, ledge, nests, sacs, gold = FACES[name]
    pads = list(pads) + HARD_PADS.get(name, [])
    return grown_face(w, h, cx, cy, rx, ry, seed, density, pads, ledge, nests, sacs, gold)


# =========================================================================== deep and air pieces

def sun_point(size=96):
    """The Sun from the belt: a small white-hot disc, a soft warm halo and a short four-ray flare (stepped alpha)."""
    yy, xx = np.mgrid[0:size, 0:size].astype(float) + 0.5
    x, y = xx - size / 2, yy - size / 2
    r, a = np.hypot(x, y), np.arctan2(y, x)
    f = np.exp(-(r / 3.0) ** 2) * 1.4 + np.exp(-(r / 14) ** 2) * 0.35
    f += np.clip(np.cos(a * 2) ** 40, 0, 1) * np.exp(-(r / 30) ** 2) * 0.7
    f *= np.clip((size / 2 - 2 - r) / 6, 0, 1)
    rgb = np.array((255, 236, 200.0))[None, None] * np.clip(f, 0, 1)[..., None]
    alpha = np.clip(f * 1.4, 0, 1) * 255
    img = Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")
    return artkit.stepped_alpha(img, 6)


def ceres_ahead(size=300, seed=3365):
    """Ceres ahead at the field's end: the sun high on its left, a broad warm crescent, the night side faint."""
    import enemies_r03 as e3  # noqa: E402  (concept script: the regolith texture)
    yy, xx = np.mgrid[0:size, 0:size].astype(float)
    c, R = size / 2, size / 2 - 8
    d = np.hypot(xx - c, yy - c)
    tex = np.array(e3.regolith(size, size, seed, craters=30).convert("L")).astype(float) / 255
    nz = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    nx, ny = (xx - c) / R, (yy - c) / R
    lit = np.clip(-0.75 * nx - 0.45 * ny + 0.25 * nz, 0.0, 1.0)
    rgb = raster.ramp(WARM_ROCK, np.clip(0.35 + 0.6 * tex, 0, 1)) * (0.08 + 1.15 * lit[..., None])
    limb = np.abs(d - R) < 1.5
    rgb[limb] = rgb[limb] * 0.5 + np.array((230, 200, 150)) * 0.5 * np.clip(lit[limb, None] * 2, 0.15, 1)
    img = l30.posterize(rgb, 36, np.where(d < R, 255, 0).astype(np.uint8))
    return l30.hazed(img, 0.85, haze=0.25)


def talon_wreck(heading, seed):
    """A leeched Talon's drifting wreck: the Talon's frame darkened and scorched, its dead leech (the prey frame,
    sac dark) on its back, hazed (below the play plane)."""
    import belt_units_c as buc  # noqa: E402
    tal = bu.load("talon")[heading]
    try:
        leech = bu.load("void-leech-prey")[heading * 4]
    except FileNotFoundError:
        leech = None
    img = Image.new("RGBA", (64, 64))
    img.alpha_composite(buc.scorch(tal, seed, 0.45), (8, 8))
    if leech is not None:
        a = np.array(leech).astype(np.float64)
        a[..., :3] *= 0.55
        img.alpha_composite(Image.fromarray(a.astype(np.uint8), "RGBA"), (8, 8))
    return l30.hazed(img, 0.75, haze=0.3)


def _render_job(job):
    kind = job[0]
    if kind == "face":
        return face(job[1])
    raise ValueError(kind)


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    save("field-space", l30.space_tile(seed=3371))
    save("field-dust", l30.dust_tile(seed=3372))
    save("sun-point", sun_point())
    save("ceres-ahead", ceres_ahead())
    for name, (w, h, seed) in {"far-rock-a": (120, 100, 3381), "far-rock-b": (84, 72, 3383)}.items():
        rock = np.array(l30.far_rock(w, h, seed))
        rock[:1], rock[-1:], rock[:, :1], rock[:, -1:] = 0, 0, 0, 0          # the deep layer's edge rule
        save(name, Image.fromarray(rock, "RGBA"))
    with ProcessPoolExecutor() as pool:
        faces = list(pool.map(_render_job, [("face", n) for n in FACES], chunksize=1))
    for name, img in zip(FACES, faces):
        save(name, img)
    for i, f in enumerate(artkit.quantize_set([leech_sacs(i) for i in range(4)], 24)):
        save(f"leech-sacs_{i}", f)
    save("talon-wreck-a", artkit.quantize_set([talon_wreck(3, 3391)], 28)[0])
    save("talon-wreck-b", artkit.quantize_set([talon_wreck(11, 3393)], 28)[0])
    return pieces_spec(), placements()


def pieces_spec():
    out = {"sun-point": {"layer": "deep", "size": [96, 96]},
           "far-rock-a": {"layer": "deep", "size": [120, 100]},
           "far-rock-b": {"layer": "deep", "size": [84, 72]},
           "ceres-ahead": {"layer": "deep", "size": [300, 300]}}
    for name, spec in FACES.items():
        out[name] = {"layer": "ground", "size": list(spec[0])}
    out["leech-sacs"] = {"layer": "ground", "size": [64, 48], "frames": 4, "fps": 2}
    out["talon-wreck-a"] = {"layer": "low-air", "size": [64, 64]}
    out["talon-wreck-b"] = {"layer": "low-air", "size": [64, 64]}
    return out


# =========================================================================== the places

BUNKER = 72
SHACK = 48


def target_row(t, h=BUNKER):
    """The ground row of the centre of a target h px tall entering at the top edge at t (backdrop_l29.py's)."""
    return scroll(t) + SCREEN + h / 2


def face_t_for(name, pad, first_t, h=BUNKER):
    """The placement time of face ``name`` putting its pad (x, y in the piece) under a target h px tall entering at
    the top edge at ``first_t``."""
    w_h = FACES[name][0]
    centre = target_row(first_t, h) + (pad[1] - w_h[1] / 2)
    return round(time_at(centre - SCREEN / 2), 3)


def pad_times(name, face_t, h=BUNKER):
    """(t, x) of the target on every pad of a face placed at ``face_t``."""
    (w, hh), _, _, _, pads, _, _, _, _ = FACES[name]
    centre = scroll(face_t) + SCREEN / 2
    out = []
    for x, y in sorted(pads, key=lambda p: -p[1]):
        row = centre - (y - hh / 2)
        out.append((round(time_at(row - SCREEN - h / 2), 2), x))
    return out


# the README's bunkers: (face, the pad entering first, its t)
BUNKER_FACES = [("grown-face-1", (112, 300), 33.5), ("ridge-1", (92, 196), 68.0), ("ridge-2", (84, 210), 86.0),
                ("nest-face-1", (100, 250), 132.0), ("nest-face-2", (320, 230), 150.0)]
CLAIM_T, CLAIM_X = 104.0, 380
# the other faces, by section: (name, t of the centre at the screen's middle, x)
SCENERY = [("edge-rock-a", 6.0, 380), ("edge-rock-b", 13.0, 90), ("edge-rock-a", 20.0, 120),
           ("edge-rock-b", 26.0, 400),
           ("grown-face-2", 42.0, 260), ("edge-rock-b", 48.5, 60), ("grown-face-2", 54.0, 200),
           ("grown-face-2", 77.0, 250), ("edge-rock-b", 95.5, 70),
           ("grown-face-2", 112.0, 230), ("nest-face-3", 124.0, 240),
           ("grown-face-2", 141.0, 260), ("nest-face-3", 157.0, 250),
           ("clear-face-a", 167.0, 240), ("clear-rock-b", 172.5, 90), ("clear-face-a", 177.0, 300)]
SACS = [(30.0, 300, 380), (45.0, 120, 330), (80.0, 400, 300), (110.0, 140, 360), (135.0, 260, 330),
        (146.0, 330, 360), (159.0, 180, 320)]
WRECKS = [("talon-wreck-a", 100.0, 120), ("talon-wreck-b", 108.0, 300), ("talon-wreck-a", 115.0, 70)]


def placements():
    """Every placed piece: dict(name, layer, t, x, note)."""
    out = []
    for name, pad, t in BUNKER_FACES:
        ft = face_t_for(name, pad, t)
        out.append(dict(name=name, t=ft, x=240, note=f"{name}: its pads carry the bunkers at "
                        + ", ".join(f"x {x} (t {bt:g})" for bt, x in pad_times(name, ft))))
    ft = round(time_at(target_row(CLAIM_T, SHACK) + (FACES["feeding-face"][5][1] - FACES["feeding-face"][0][1] / 2)
                       - SCREEN / 2), 3)
    out.append(dict(name="feeding-face", t=ft, x=240, note=f"the feeding ground: Claim 114's ledge at x {CLAIM_X} "
                                                           f"(t {CLAIM_T:g})"))
    for name, t, x in SCENERY:
        out.append(dict(name=name, t=t_centre_at(t, SCREEN / 2, 1.0), x=x, note=""))
    for t, x, y in SACS:
        out.append(dict(name="leech-sacs", t=t_centre_at(t, y, 1.0), x=x, note="leech sacs pulsing in the growth"
                        if t == SACS[0][0] else ""))
    for name, t, x in WRECKS:
        out.append(dict(name=name, t=t_centre_at(t, SCREEN / 2, FACTORS["low-air"]), x=x,
                        note="a leeched Talon's wreck drifting below the play plane" if t == WRECKS[0][1] else ""))
    out.append(dict(name="sun-point", t=t_centre_at(0.0, 120, FACTORS["deep"]), x=96,
                    note="the Sun as a bright point (section 1)"))
    out += [dict(name="far-rock-a", t=30.0, x=400, note="far rocks"), dict(name="far-rock-b", t=70.0, x=80, note=""),
            dict(name="far-rock-a", t=120.0, x=90, note=""), dict(name="far-rock-b", t=150.0, x=410, note="")]
    out.append(dict(name="ceres-ahead", t=t_centre_at(END, 120, FACTORS["deep"]), x=300,
                    note=f"Ceres's light ahead, centred 120 px down at the end (t={END:g})"))
    pieces = pieces_spec()
    for p in out:
        p["layer"] = pieces[p["name"]]["layer"]
        p["t"] = round(float(p["t"]), 3)
    return out


def targets():
    """The proposed ground targets: the bunkers on the pads, Claim 114 on its ledge."""
    out = []
    sections = {33.5: 2, 68.0: 3, 86.0: 3, 132.0: 5, 150.0: 5}
    for name, pad, t in BUNKER_FACES:
        ft = face_t_for(name, pad, t)
        at = pad_times(name, ft)
        grp = {"target": f"Rail Bunker {'pair' if len(at) == 2 else 'single'} on {name}",
               "section": sections[t], "enemy": "rail-bunker", "count": len(at), "at": at}
        if name in HARD_AT:
            grp["hard"] = at + HARD_AT[name]          # round 66's C.7 b: the third on its own pad at x 240
        out.append(grp)
    out.append({"target": "Claim 114", "section": 4, "at": [(CLAIM_T, CLAIM_X)]})
    return out


# Level 33's debris (Level 03's rules, the belt's chunks): the grown rocks (tools/art/belt_units_f.py). The README:
# rocks cross every section, small ones carry the Mites (the waves' on_rocks), big ones block shots and give the leeches
# cover; at most two big on the screen. Big rocks every ~9-12 s from a side lane, none in the Choir's quiet window
# (t 115-121) or the last 10 s; a few free small rocks between the riders' waves.
LARGE = [(9, 70), (19, 410), (36, 60), (46, 420), (58, 70), (73, 410), (84, 60), (101, 420), (111, 70), (124, 410),
         (134, 60), (145, 420), (155, 70)]
SMALL_X = (110, 370, 200, 300, 150, 330, 60, 420, 240)


def debris_rows():
    rows = []
    for i, (t, x) in enumerate(LARGE):
        rows.append((float(t), x, "grown-large-a" if i % 2 == 0 else "grown-large-b", [12 if x < 240 else -12, -165]))
    t, i = 3.5, 0
    while t < 168:
        if not 114 < t < 122:
            rows.append((round(t, 1), SMALL_X[i % len(SMALL_X)], "grown-" + "abc"[i % 3],
                         [(-1) ** i * (20 + 10 * (i % 5)), -(180 + 10 * (i % 4))]))
        t += 6.5
        i += 1
    rows.sort(key=lambda r: (r[0], r[1]))
    return rows


def debris_lines():
    lines = ["debris:",
             "  # Proposal (ART-F2): the Leech Field's grown rocks (tools/art/belt_units_f.py); small ones break (6 HP,",
             "  # part B's), large ones are walls (contact `large` 20, the belt's hazards). The Mites' rocks are the",
             "  # waves' on_rocks (use the grown-a/b/c chunks). At most two big rocks on the screen; F2 checks the",
             "  # waves (no rock line within 40 px of a spawn point, no big rock in the ship's lowest 120 px while a",
             "  # rear leech wave enters).",
             "  chunks:",
             "    grown-large-a: {size: [88, 80], damage: 20}",
             "    grown-large-b: {size: [66, 60], damage: 20}",
             "    grown-a: {size: [40, 36], hp: 6}",
             "    grown-b: {size: [36, 32], hp: 6}",
             "    grown-c: {size: [32, 28], hp: 6}",
             "  clearance: 120",
             "  max_large: 2",
             "  easy: {leave_out_large: 4}",
             "  placed:"]
    for t, x, chunk, drift in debris_rows():
        lines.append(f"    - {{t: {t:g}, x: {x}, chunk: {chunk}, drift: [{drift[0]}, {drift[1]}]}}")
    return lines


def proposal_text(pieces, placed):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after F2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 33's Leech Field (tools/art/backdrop_l33.py, step ART-F2, from {src}).",
             "  # A ground layer without a tile set: open space is the deep layer (the base); the ground holds the",
             "  # asteroid faces (bare at the edge, overgrown in sections 2-5, gold-lit clear rock at the end) with the",
             "  # bunkers' pads and Claim 114's ledge, and the pulsing leech sacs (the only animated pieces: at most two",
             "  # on the screen). Low-air holds the leeched Talons' drifting wrecks (dimmed); high-air the close grit.",
             "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused",
             "  ramp: 2",
             "  haze_colour: 09090e",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:",
             f"    field-space: {{layer: deep, height: {SPACE_H}}}   # space, the sunlit dust band, stars",
             f"    field-dust: {{layer: high-air, height: {DUST_H}}}   # close grit and dust motes (transparent)",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}}}")
    lines.append("sections:   # ART-F2's reading of the README's Layout (150 px/s, the ridge 130 with 2 s eases); F2 "
                 "writes the real ones")
    for s in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {v}" for k, v in s.items()) + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); low-air "
                 f"{int(round(scroll(END) * FACTORS['low-air']))}")
    lines += ["ground_targets:  # at: [t, x], when the target enters at the top edge and its x (on the faces' pads;",
              "  # a bunker 72 px, Claim 114 48 px: the shack's sprite `claim-shack`, 20 HP, 28 on hard). The pads'",
              "  # pairs stand at one row: their lines cross over the ship; the first delays' stagger (half the",
              "  # interval) is F2's. Every bunker x is >= 84 px inside the 480 px field."]
    for grp in targets():
        lines.append(f"  - target: {grp['target']}")
        lines.append(f"    section: {grp['section']}")
        if grp.get("enemy"):
            lines += [f"    enemy: {grp['enemy']}", f"    count: {grp['count']}"]
        else:
            lines += ["    sprite: claim-shack", "    size: [48, 48]"]
        lines.append("    at: [" + ", ".join(f"[{t:g}, {x}]" for t, x in grp["at"]) + "]")
        if grp.get("hard"):
            lines.append("    hard: {at: [" + ", ".join(f"[{t:g}, {x}]" for t, x in grp["hard"]) + "]}")
    lines += ["drops:   # the CDF nav buoy (tools/art/belt_units_f.py `nav-buoy`, BuoyLooks' frames: F4 adds it to "
              "MOORED)",
              "  - {t: 160, x: 240, y: 380, pickup: armour patch, sprite: nav-buoy}"]
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
    """(x, y_down of the centre, frame index) of a placed piece at level time t."""
    f = FACTORS[spec["layer"]]
    centre = scroll(float(p["t"])) * f + SCREEN / 2
    k = int(t * spec["fps"]) % spec["frames"] if spec.get("frames") else 0
    return p["x"], scroll(t) * f + SCREEN - centre, k


def blit(img, p, pieces, t, layer):
    spec = pieces[p["piece"]]
    if spec["layer"] != layer:
        return img
    x, y, k = place_of(p, spec, t)
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


def compose(t, pieces, placed, debris=None, targets_=None, units=True):
    """The screen at level time t (480x540): the deep tile and pieces, the ground pieces, the bunkers and the shack
    (stand-ins: the game draws them), low-air pieces, the rocks and the ship, high-air grit."""
    g = scroll(t)
    img = Image.fromarray(tile_rows("field-space", SPACE_H, g * FACTORS["deep"]), "RGBA")
    for layer in ("deep", "ground"):
        for p in placed:
            img = blit(img, p, pieces, t, layer)
    if units:
        img = ground_stand_ins(img, t, targets_)
    for p in placed:
        img = blit(img, p, pieces, t, "low-air")
    if units:
        img = air_stand_ins(img, t, debris)
    dust = Image.fromarray(tile_rows("field-dust", DUST_H, g * FACTORS["high-air"]), "RGBA")
    img.alpha_composite(dust)
    return img


def ground_stand_ins(img, t, targets_):
    if not targets_:
        return img
    base, tur, shack = _held("rail-bunker"), _held("rail-bunker-turret"), _held("claim-shack")
    for grp in targets_:
        h = BUNKER if grp.get("enemy") else SHACK
        for nt, x in grp["at"] + (grp.get("hard") or {}).get("at", []):
            y = scroll(t) - scroll(nt) - h / 2
            if -60 < y < SCREEN + 60:
                if grp.get("enemy") and base and tur:
                    img = bu.paste(img, base[0], x, y)
                    img = bu.paste(img, tur[24 if x < 240 else 8], x, y)
                elif not grp.get("enemy") and shack:
                    img = bu.paste(img, shack[int(t * 4) % 4], x, y)
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
    return bu.paste(img, artkit.load_frames("ship")[2], 240 + 140 * np.sin(t * 0.5), 440)


SHOTS = [(4.0, "FIELD EDGE: BARE ROCKS, THE SUN"), (35.0, "GROWN ROCKS: THE FIRST BUNKER"),
         (52.0, "GROWN ROCKS: SACS, BIG ROCKS"), (70.0, "BUNKER RIDGE: THE FIRST PAIR"),
         (88.0, "BUNKER RIDGE: THE SECOND PAIR"), (106.0, "FEEDING GROUND: CLAIM 114"),
         (134.0, "THE SWARM: A BUNKER PAIR"), (END - 0.5, "CLEAR ROCK: GOLD LIGHT, CERES AHEAD")]


def review():
    pieces, placed, debris, targets_ = load_proposal()
    shots = [(compose(t, pieces, placed, debris, targets_), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    names = ["grown-face-1", "ridge-1", "feeding-face", "nest-face-1", "clear-face-a", "edge-rock-a", "leech-sacs",
             "talon-wreck-a", "sun-point", "ceres-ahead"]
    pieces_h = 260
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + pieces_h + 30,
                         "LEVEL 33 BACKDROP (FINAL R66): THE LEECH FIELD", f"PRODUCTION ART, {BATCH.upper()} - R66")
    raster.draw_text(sheet, 16, 38, "EIGHT SCREENS AT HALF SIZE WITH STAND-INS (THE BUNKERS ON THEIR PADS, CLAIM 114, "
                                    "THE GROWN ROCKS IN THEIR PROPOSED PLACES, THE SHIP)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 10
    raster.draw_text(sheet, 16, y, "THE PIECES ON THE OPEN SPACE (THE BIG FACES AT A THIRD, THE REST AT 1X)", raster.LABEL)
    x = 16
    space = Image.open(OUT / "field-space.png").convert("RGBA")
    for name in names:
        fr = images(name, pieces[name])[2 if name == "leech-sacs" else 0]
        if fr.width > 200:
            fr = fr.resize((fr.width // 3, fr.height // 3), Image.BOX)
        if x + fr.width > sheet.width - 16:
            break
        cell = space.crop((0, 0, fr.width + 8, fr.height + 8))
        cell.alpha_composite(fr, (4, 4))
        sheet.alpha_composite(cell, (x, y + 16))
        x += fr.width + 12
    artkit.REVIEW_ROUND = ROUND                 # (belt_units_c, imported for the wrecks, sets its own round)
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    frames = [compose(60 + i / 8, pieces, placed, debris, targets_).resize((sw, sh), Image.BOX) for i in range(80)]
    size = e60.rig.write_gif(frames, gif, fps=8, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def check():
    """The space tile wraps (BackdropSeamsTest's measure) and is opaque, sizes match the proposal, Source chunks, the
    deep pieces' edges transparent, the ground pieces' top and bottom rows clear, every placement on the screen, at most
    two animated pieces on the screen at once, every bunker and Claim 114 on opaque face pixels and >= 40 px inside the
    field, at most two big rocks on the screen."""
    problems = []
    pieces, placed, debris, targets_ = load_proposal()
    a = np.array(Image.open(OUT / "field-space.png").convert("RGBA"))
    if l30.wrap_score(a) > 1.5:
        problems.append(f"field-space: wrap score {l30.wrap_score(a):.2f}")
    if (a[..., 3] < 255).any():
        problems.append("field-space: not opaque")
    for f in OUT.glob("*.png"):
        if Image.open(f).text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
    for name, spec in pieces.items():
        for im in piece_images(name, spec):
            if list(im.size) != list(spec["size"]):
                problems.append(f"{name}: {im.size} vs {spec['size']}")
            al = np.array(im)[..., 3] > 0
            if spec["layer"] == "deep" and (al[0].any() or al[-1].any() or al[:, 0].any() or al[:, -1].any()):
                problems.append(f"{name}: an opaque edge on the deep (base) layer")
            if spec["layer"] == "ground" and (al[0].any() or al[-1].any()):
                problems.append(f"{name}: an opaque top or bottom row on the ground")
    seen = set()
    worst = (0, 0.0)
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = 0
        for i, p in enumerate(placed):
            spec = pieces[p["piece"]]
            _, y, _ = place_of(p, spec, t)
            if y + spec["size"][1] / 2 > 0 and y - spec["size"][1] / 2 < SCREEN:
                seen.add(i)
                animated += bool(spec.get("frames"))
        worst = max(worst, (animated, t))
        big = sum(1 for r in debris["placed"] if "large" in r["chunk"] and 0 <= t - r["t"] and
                  -60 < -20 - r["drift"][1] * (t - r["t"]) < SCREEN + 60)
        if big > 2:
            problems.append(f"t={t:.1f}: {big} big rocks on the screen")
            break
    if worst[0] > 2:
        problems.append(f"t={worst[1]:.1f}: {worst[0]} animated pieces on the screen")
    for i, p in enumerate(placed):
        if i not in seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")
    for grp in targets_:
        h = BUNKER if grp.get("enemy") else SHACK
        for nt, x in grp["at"] + (grp.get("hard") or {}).get("at", []):
            if not 40 <= x <= W - 40:
                problems.append(f"{grp['target']} x {x}: not 40 px inside the field")
            row = target_row(nt, h)
            on = False
            for p in placed:
                spec = pieces[p["piece"]]
                if spec["layer"] != "ground" or spec.get("frames"):
                    continue
                im = images(p["piece"], spec)[0]
                centre = scroll(p["t"]) + SCREEN / 2
                py = int(round(spec["size"][1] / 2 - (row - centre)))
                px = int(round(x - p["x"] + spec["size"][0] / 2))
                if 0 <= py < im.height and 0 <= px < im.width and np.array(im)[py, px, 3] > 0:
                    on = True
            if not on:
                problems.append(f"{grp['target']} x {x} (t {nt}) not on a face's pixels")
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
