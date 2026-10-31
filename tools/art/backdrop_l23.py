#!/usr/bin/env python3
"""Production art: the Level 23 backdrop, Through the Ice (design/campaign/act-4-deep-water/
level-23-through-the-ice; M8 part C batch, concept round 52, step ART-C).

Straight to production from the round-48 mood stills `europa-descent-r48-a` (the drilled shaft, the
waterline, the Vrell collars' teal strips) and `europa-thera-deep-r48-a` (Thera Deep's lamp-lit farms on the
chosen round-07 kelp floor), tools/concept/europa_r48.py and scenes_r07.py imported unchanged (r07's floor is
built by a subclass here that leaves out its fixed kelp plots and domes: the racks and the domes are pieces at
the data's rows). Structure as backdrop_l22.py (exact rows, the scroll helpers) and backdrop_l16.py (the wall
texture convention and its helpers, imported). Posterized per image, wide gradients ordered-dithered.

**The waterline row.** The ship crosses the waterline at WL_T = 25.5 s (the start of the first section whose
`medium` is `underwater`; README value without data). The band lies on the ground row the ship's start height
passes then: ground row = scroll(WL_T) + SHIP_Y, SHIP_Y = 96 (sim Ship.START_Y, px above the bottom edge; the
ship's screen y 444), i.e. **ground row 2,646** (terrain row 2,106 = ground row - 540) at 100 px/s. The
`waterline` ground piece holds the band at that row exactly (dry shaft below, wet above, fading into section 2's
`shaft-wet` tile, which begins at its seam 3,090); the translucent `waterline-foam` low-air piece (x1.35) is
placed so it crosses the same screen row at WL_T. Step C4's grade wipe uses the same row: graded above it.

**The under-water grade is the game's** (C4, `UnderwaterGrade`): the tiles carry the unlit scene (Thera Deep's
floor as r07 painted it, ambient 1), the lamp pools are baked into the rack, dome and lamp pieces as gain over that
floor (floor x (1 + 1.4 g WARM) + 70 g WARM), so the grade's ambient 0.58 x tint over the whole ground gives the r48 still.
Part D's request: the grade must stay overridable per section (Level 24 darkens section by section), so the
`underwater_look` block should take a per-section ambient/tint; nothing here assumes one value per level.
Caustics, marine snow and the headlights are the game's animated overlays (none baked; the wet floors are kept
flat enough to carry them).

**The ice-shaft walls (WallProjection; step C4's ice-shaft look of CanyonProfile).** CanyonProfile's texture
convention unchanged (TEX_W 128 x (TEX_PERIOD 1024 + TEX_PAD 256): segment g's columns from NODE_U[g] to
[g + 1], foot (column 0) to top (127), widths 24 / 20 / 32 / 24 / 16 / 12, rows the arc length along the wall,
row 0 of the period at the image's bottom, the image's top 256 rows repeating the period's first 256 so a quad
may run past the period; backdrop_l16.padded). Painted as drilled glacial ice, unlit (the game shades each step
by its normal and adds the headlights): segment 0 the dark wet foot ice, 1 a narrow chipped ledge, 2 the main
drilled face, 3 the collar ledge, 4 the upper face, 5 the pale lip. The drill's spiral grooves run diagonally
across all faces (32 per period, rising 1.5 columns-widths: a helix unrolled); trapped bubbles and frost; four
Vrell collar strips per period (bone bands with a teal core, at rows 128 + 256 k). C4 is free to give the ice
shaft its own setbacks (the shaft is near sheer: small setbacks keep the ledges narrow).
  ice-wall            128x1280  the dry shaft (above the waterline in ground rows: below WATERLINE_ROW)
  ice-wall-wet        128x1280  below the waterline: darker, teal-tinted, the grooves softened (caustic-ready)
  ice-wall-glow       128x1280  additive: the collar strips' teal cores only, so the game can add them unshaded
                                (they glow through the grade and the dark of later levels)
  ice-wall-foot       24x512    the foot band (column 0 the collision line: a dark crack, the lit ice edge, ice
                                chips thinning out by column 23, 1-bit dithered alpha); lit from straight up only,
                                so the right wall's band is this image mirrored in x
  ice-wall-foot-wet   24x512    the same under water
No `plateau` tile: with the shaft's 320 px and the walls' setbacks (48 px) the rim projects beyond the field's
edges (x < 0 / > 480) at every row, so the plateau layer never shows (add one if C4's profile brings the rim in).
No `deep` layer: the ground is opaque everywhere (the shaft's depth is painted into the floor tiles).

Tile sets (480x960, wrapping; section tiles in SECTION_TILES):
  shaft-dry     ground, section 1: the dry shaft seen from above, ice floor receding to a dark centre trough,
                the drill's spiral grooves as chevrons converging on it, a faint warm glow far below (Thera Deep)
  shaft-wet     ground, sections 2-3: the same under water, teal and darker, the warm glow stronger
  thera-farms   ground, section 4: r07's kelp floor (meadows, reefs, anemones, fans) without its plots and domes
  plain-edge    ground, section 5: the dark silt plain, sparse kelp tufts, pale stones
  ice-ceiling   high-air (x2.2), sections 3-5: the ice ceiling's cracks of light, faint, on black (additive at
                40 % by the game, as Level 22's ice-crystals)
Pieces (exact rows where the data fixes them):
  shelf-lip           ground 480x960 at the start: the shelf's ice surface, the shaft's mouth (a ragged lip,
                      its near edge bulging toward the ship, apex at ground row 600, Vrell collars on the rim),
                      the dry shaft beyond, fading into shaft-dry
  waterline           ground 480 wide: the band at WATERLINE_ROW (meniscus, foam fringe, the surface's light)
  waterline-foam      low-air 360x40, translucent foam lace crossing the ship's row at WL_T
  ice-ledge-{left,right}[-wet]  ground 64x56: an ice ledge at a wall's foot under each nest unit (the mount at the
                      piece's centre, a bone-and-teal Vrell collar round it); `probe-ledge` the probe's (torn cable)
  seam-thera, seam-plain        ground 480x320 on the section seams (the shaft's foot into the farm floor; the farms
                      thinning into the plain)
  farm-rack-1..5      ground: the kelp racks under the five siphon pumps (float-line frames, kelp rows, grow-lamp
                      bars, the pump's socket with teal roots spreading into the kelp; rack 3 runs on to carry the
                      t~108 Reef Spitter nest), each with its lamp pool baked over the floor rows under it
  thera-domes-1..4    ground: pressure-dome clusters with tube corridors, warm windows and their pools (r06's
                      dome model), between the racks on the far side from the pumps
  plain-lamps-1..2    ground: the last lamp pylons of Thera Deep on the plain with their pools
Review: design/campaign/act-4-deep-water/level-23-through-the-ice/concept/backdrop-final-r52-a.png/.gif (the
walls are approximated in the review: the game projects them); `--proposal` writes the `backdrop:` block to the
level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections, speeds, ramps, the waterline section, terrain walls, ground
targets: nests, the probe, pumps), otherwise the README's values (gap D1's five sections). Re-run after the data
moves a section, a nest or a pump. Run: python3 tools/art/backdrop_l23.py [--proposal | --review | --check]
(all: about 2 min).
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU, raster

import backdrop_l16 as b16  # noqa: E402  (tools/art: the wall texture convention and helpers)
import backdrop_l22 as b22  # noqa: E402  (tools/art: Europa's ice surface, posterize)
import europa_r48 as eu  # noqa: E402  (concept script, unchanged: Europa's colours, PLAIN, WARM)
import scenes_r06 as s6  # noqa: E402  (concept script, unchanged: the dome model)
import scenes_r07 as s7  # noqa: E402  (concept script, unchanged: the chosen kelp floor)

SCRIPT = "backdrop_l23.py"
BATCH = "M8 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r52"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-23-through-the-ice"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-23"
HELD = ROOT / "build" / "art-held" / "sprites"
W, SCREEN = 480, 540
TILE_H = 960
FEATHER = 32
SHIP_Y = 96                                   # sim Ship.START_Y: px above the bottom edge
FACTORS = {"ground": 1.0, "low-air": 1.35, "high-air": 2.2}
TEAL = np.array((0, 255, 154), float)
BONE = np.array((196, 184, 164), float)
WARM = np.array(eu.WARM, float)
WATER = np.array(eu.WATER, float)
LAMP = (255, 220, 160)
GRADE = np.array((0.8, 0.95, 1.0)) * 0.58     # the review's stand-in for C4's grade (ambient 0.58, cool tint)

# --------------------------------------------------------------------------- the level's data (or the README)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
FALLBACK_SECTIONS = [{"end": 25.5, "speed": 100}, {"end": 28, "speed": 100, "medium": "underwater"},
                     {"end": 72, "speed": 90, "ramp": 2}, {"end": 140, "speed": 80, "ramp": 2},
                     {"end": 165, "speed": 100, "ramp": 2}]
SECTIONS = DATA["sections"] if DATA else FALLBACK_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 100)) if DATA else 100.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speeds():
    out, start, prev = [], 0.0, None
    for s in SECTIONS:
        v = float(s.get("speed", BASE_SPEED if prev is None else prev))
        out.append((start, float(s["end"]), v, float(s.get("ramp", 0)) if prev is not None else 0.0, prev))
        start, prev = float(s["end"]), v
    return out


SPEEDS = _speeds()
DT = 1 / 600


def speed_at(t):
    v = SPEEDS[0][2]
    for start, end, sp, ramp, prev in SPEEDS:
        if t >= start:
            if ramp and prev is not None and t < start + ramp:
                u = (t - start) / ramp
                v = prev + (sp - prev) * u * u * (3 - 2 * u)
            else:
                v = sp
    return v


_T = np.arange(0, END + 16, DT)
_S = np.concatenate([[0.0], np.cumsum([speed_at(t + DT / 2) * DT for t in _T[:-1]])])


def scroll(t):
    return float(np.interp(t, _T, _S))


def time_at(s):
    return float(np.interp(s, _S, _T))


def seam_row(index):
    """Section ``index``'s tile set begins on the ground here (it enters at the top edge at the section's start)."""
    return scroll(STARTS[index]) + SCREEN


def target_centre(t, h):
    """A ground target `at: [t, x]` enters at the top edge at t: its centre h/2 above the top edge."""
    return scroll(t) + SCREEN + h / 2


def place_t(centre_row, factor=1.0):
    """The `t` a piece's centre passes the middle of the screen (LevelData.pieceCentre: scroll(t) x f + 270)."""
    return time_at((centre_row - SCREEN / 2) / factor)


def exact_t(bottom, h):
    t = place_t(bottom + h / 2)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def _wl_index():
    for i, s in enumerate(SECTIONS):
        if str(s.get("medium", "")).lower() == "underwater":
            return i
    return 1


WL_INDEX = _wl_index()
WL_T = STARTS[WL_INDEX]
WATERLINE_ROW = int(round(scroll(WL_T) + SHIP_Y))


def _walls():
    """The walls' collision lines as (terrain rows, xs) per side; terrain row = ground row - 540."""
    if DATA and DATA.get("terrain"):
        tr = DATA["terrain"]
        return ([(float(p[0]), float(p[1])) for p in tr["left"]], [(float(p[0]), float(p[1])) for p in tr["right"]])
    r = scroll
    left = [(r(6), 0), (r(14), 80), (r(66), 80), (r(74), 0)]
    return left, [(row, W - x) for row, x in left]


LEFT, RIGHT = _walls()


def foot(side, ground_row):
    """The wall's collision line x at a ground row (left: 0 = no wall in the field; right: 480)."""
    pts = LEFT if side == 0 else RIGHT
    rows = np.array([p[0] for p in pts]) + SCREEN
    xs = np.array([p[1] for p in pts])
    return np.interp(ground_row, rows, xs, left=xs[0], right=xs[-1])


def _ats(g):
    a = g.get("at")
    if a is None:
        return []
    return a if isinstance(a, list) and a and isinstance(a[0], list) else [a]


def _targets():
    """(kind, t, x) of the ground targets: the data's, else the README's (nests at the walls' foot band)."""
    if DATA:
        out = []
        for g in DATA.get("ground_targets") or []:
            kind = g.get("enemy") or ("probe" if g.get("trigger") or g.get("reveals") else g.get("sprite", "?"))
            for at in _ats(g):
                out.append((str(kind), float(at[0]), float(at[1])))
        return out
    return ([("spine-turret", 14, 96), ("spine-turret", 20, 384), ("spine-turret", 20.6, 96),
             ("reef-spitter", 36, 96), ("reef-spitter", 36.6, 384), ("spine-turret", 44, 384),
             ("spine-turret", 44.6, 96), ("spine-turret", 45.2, 384), ("probe", 52, 96),
             ("reef-spitter", 56, 384), ("reef-spitter", 56.6, 96)]
            + [("siphon-pump", t, x) for t, x in ((78, 120), (90, 360), (102, 200), (114, 330), (126, 240))]
            + [("reef-spitter", 108, x) for x in (170, 200, 230)])


TARGETS = _targets()
PUMPS = sorted([(t, x) for k, t, x in TARGETS if k == "siphon-pump"])


def wall_side(x, row):
    """0 / 1 when a target at (x, ground row) sits at the left / right wall's foot band, else None."""
    xl, xr = foot(0, row), foot(1, row)
    if xl > 4 and x - xl < 44:
        return 0
    if xr < W - 4 and xr - x < 44:
        return 1
    return None


# --------------------------------------------------------------------------- helpers

pn, ramp, finish, padded, dither_alpha = b16.pn, b16.ramp, b16.finish, b16.padded, b16.dither_alpha
posterize = b22.posterize


def grid(h=TILE_H, w=W):
    return np.mgrid[0:h, 0:w].astype(np.float64)


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    """Ground rows ``bottom``..``bottom+h`` of a tile set as the game lays them (image row = H-1 - row mod H)."""
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(TILE_H - 1 - np.mod(rows, TILE_H)).astype(int)]


def feather_alpha(h, w):
    k = np.arange(h)[:, None]
    a = np.minimum(np.clip((k + 0.5) / FEATHER, 0, 1), np.clip((h - k - 0.5) / FEATHER, 0, 1))
    return dither_alpha(np.broadcast_to(a, (h, w)).copy(), 2)


# --------------------------------------------------------------------------- tile sets

def shaft_floor(wet):
    """The shaft seen from above: the ice floor recedes to a dark trough down the middle, the drill's spiral
    grooves converge on it as chevrons, a warm glow far below (Thera Deep's lights)."""
    yy, xx = grid()
    seed = 2311 if wet else 2301
    n = pn(W, TILE_H, 96, seed, 4)
    f = pn(W, TILE_H, 24, seed + 1, 3)
    m = pn(W, TILE_H, 48, seed + 2, 3)
    dx = np.abs(xx - 240) + 22 * (m - 0.5)
    d = np.clip(dx / 230, 0, 1)
    ph = (yy + 0.62 * dx + 14 * (n - 0.5)) * 40 / TILE_H            # 40 grooves a tile (wraps)
    groove = 0.5 + 0.5 * np.cos(TAU * ph)
    ridge = np.clip(1 - np.abs((ph % 1.0) - 0.5) / 0.08, 0, 1)
    light = 0.42 + 0.58 * d ** 1.1
    alb = ramp([(70, 98, 128), (120, 150, 180), (168, 194, 218), (210, 226, 240)], 0.3 + 0.45 * n + 0.2 * f)
    rgb = alb * light[..., None] * (0.74 + 0.3 * groove - 0.14 * ridge)[..., None]
    rgb += np.array((150, 190, 220)) * (ridge * (0.3 + d) * 0.10)[..., None]       # the lit lip of each groove
    frost = (pn(W, TILE_H, 8, seed + 3, 2) > 0.66) & (d > 0.45)
    rgb = np.where(frost[..., None], rgb * 1.08 + 6, rgb)
    core = np.exp(-(dx / 30) ** 2)
    glow = 0.22 if wet else 0.10
    if wet:
        rgb = rgb * np.array((0.50, 0.74, 0.80)) + WATER * 0.42
        silt = (pn(W, TILE_H, 16, seed + 4, 3) > 0.62) & (d < 0.8)
        rgb = np.where(silt[..., None], rgb * 0.9 + np.array((6, 20, 22)), rgb)
    rgb = rgb * (1 - 0.32 * core)[..., None] + np.array((255, 170, 90)) * (glow * core)[..., None]
    return finish(rgb, 32, amount=0.05)


class _Floor(s7.EuropaR07):
    """r07's kelp floor without its fixed kelp-farm plots and Thera Deep's domes (pieces here), one tile high."""
    def _kelp(self, img, per):
        return img

    def _city(self, img, per):
        return img


FARM_PER = 1008   # r07's floor noise lattices (112 and 56 px) tile only over a multiple of 112: 9 x 112


def thera_farms():
    """r07's floor rendered over FARM_PER rows (its noise wraps there: over 960 rows it did not, step C5a) and
    taken to the tile's 960 rows by nearest rows (48 rows dropped evenly), so the tile wraps as the floor does."""
    sc = _Floor("a")
    rgb = np.array(sc._floor(FARM_PER).convert("RGB")).astype(np.float64)
    rows = np.floor((np.arange(TILE_H) + 0.5) * FARM_PER / TILE_H).astype(int) % FARM_PER
    return finish(rgb[rows], 40, amount=0.0)


def plain_edge():
    yy, xx = grid()
    n = pn(W, TILE_H, 96, 2341, 5)
    ridge = 1 - np.abs(2 * pn(W, TILE_H, 48, 2342, 2) - 1)
    hgt = 0.42 + 0.3 * n + 0.18 * ridge ** 4
    hgt += 0.02 * np.sin(TAU * (yy + 0.35 * xx + 60 * pn(W, TILE_H, 96, 2343, 2)) * 160 / TILE_H)
    stops = [tuple(min(255, int(c * 1.45)) for c in s) for s in eu.PLAIN]
    rgb = eu.m36.shade_rgb(hgt, stops, strength=16, shadow=0.004, periodic=True)
    rgb *= (0.92 + 0.16 * pn(W, TILE_H, 8, 2345, 2))[..., None]
    img = posterize(rgb, 32)
    lay = Image.new("RGBA", (W, TILE_H), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    rng = np.random.default_rng(2344)
    for _ in range(22):                                       # kelp tufts, sparse
        cx, cy = rng.uniform(10, W - 10), rng.uniform(0, TILE_H)
        col = (30, 84, 62) if rng.random() < 0.6 else (24, 66, 52)
        for _ in range(int(rng.integers(4, 8))):
            a, ln = rng.uniform(0, TAU), rng.uniform(6, 15)
            c = tuple(int(v * rng.uniform(0.75, 1.15)) for v in col) + (255,)
            for dy in (-TILE_H, 0, TILE_H):
                d.line([cx, cy + dy, cx + math.cos(a) * ln, cy + dy + math.sin(a) * ln], fill=c, width=2)
    for _ in range(60):                                       # pale stones and shells
        cx, cy, r = rng.uniform(0, W), rng.uniform(0, TILE_H), rng.uniform(1.0, 2.4)
        for dy in (-TILE_H, 0, TILE_H):
            d.ellipse([cx - r, cy + dy - r, cx + r, cy + dy + r], fill=(120, 130, 118, 255))
            d.point([(cx - r * 0.4, cy + dy - r * 0.4)], fill=(170, 176, 160, 255))
    img.alpha_composite(lay)
    return img


def ice_ceiling():
    """High-air: the ice ceiling's cracks of light, thin cool fracture lines with a faint halo, on black (additive).
    Step C5a: fractures, not the noise's isolines (C4's capture read those as white scribbles over the whole field):
    a few long cracks of short straight runs with small turns, each forking once or twice, drawn at 3x and boxed down
    to about a pixel wide; dim (core 0.5, forks 0.3, halo 0.12), so at the game's 40 % additive they are a hint of
    light through the ice, not lines over the play."""
    ss = 3
    rng = np.random.default_rng(2351)
    lay = Image.new("L", (W * ss, TILE_H * ss), 0)
    d = ImageDraw.Draw(lay)

    def crack(x, y, ang, length, value, forks):
        pts = [(x, y)]
        run = 0.0
        while run < length:
            step = rng.uniform(10, 26)
            ang += rng.normal(0, 0.28)
            x, y = x + math.cos(ang) * step, y + math.sin(ang) * step
            pts.append((x, y))
            run += step
            if forks and rng.random() < 0.09:
                forks -= 1
                crack(x, y, ang + rng.choice((-1, 1)) * rng.uniform(0.5, 1.0), length * rng.uniform(0.25, 0.45),
                      value * 0.6, 0)
        c = int(255 * value)
        for dy in (-TILE_H, 0, TILE_H):
            d.line([(px * ss, (py + dy) * ss) for px, py in pts], fill=c, width=ss)

    for _ in range(7):
        crack(rng.uniform(20, W - 20), rng.uniform(0, TILE_H), rng.uniform(0, TAU), rng.uniform(140, 260), 0.5, 2)
    line = np.array(lay.resize((W, TILE_H), Image.BOX)).astype(np.float64) / 255.0
    halo = np.array(Image.fromarray((line * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(2.5))) / 255.0
    v = np.clip(line + halo * 0.12 / max(halo.max(), 1e-6), 0, 1)
    v = artkit.smooth_steps(v)
    rgb = np.array((130, 186, 214)) * v[..., None]
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(rgb, 0, 255), alpha]).astype(np.uint8), "RGBA")


TILES = {"shaft-dry": lambda: shaft_floor(False), "shaft-wet": lambda: shaft_floor(True),
         "thera-farms": thera_farms, "plain-edge": plain_edge, "ice-ceiling": ice_ceiling}
SECTION_TILES = [["shaft-dry"], ["shaft-wet"], ["shaft-wet", "ice-ceiling"], ["thera-farms", "ice-ceiling"],
                 ["plain-edge", "ice-ceiling"]]


# --------------------------------------------------------------------------- the ice-shaft walls

TEX_W, TEX_PERIOD, TEX_PAD = b16.TEX_W, b16.TEX_PERIOD, b16.TEX_PAD
WIDTHS = [s[4] for s in b16.SEGMENTS]                    # 24, 20, 32, 24, 16, 12
FACE = (True, False, True, False, True, False)          # faces (else ledges), as CanyonProfile.CLIFF
ICE_STEPS = (((52, 74, 100), (90, 118, 146)),           # the dark foot ice
             ((150, 174, 196), (134, 160, 186)),        # a chipped ledge
             ((112, 142, 172), (176, 200, 222)),        # the main drilled face
             ((150, 176, 198), (138, 164, 190)),        # the collar ledge
             ((166, 192, 214), (204, 222, 238)),        # the upper face
             ((214, 228, 242), (232, 240, 250)))        # the pale lip
COLLAR_ROWS = [128 + 256 * k for k in range(4)]


def _wall_fields(th=TEX_PERIOD):
    tw = TEX_W
    g_of = np.repeat(np.arange(6), WIDTHS)
    start = np.concatenate([[0], np.cumsum(WIDTHS)])[:-1]
    tcol = (np.arange(tw) - start[g_of] + 0.5) / np.array(WIDTHS)[g_of]
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    return yy, xx, np.broadcast_to(g_of, (th, tw)), np.broadcast_to(tcol, (th, tw))


def _collar(yy, xx, th):
    """The collar strips: a 9-row bone band with a 3-row teal core, following the grooves' slope a little."""
    v = th - 1 - yy                                     # rows from the bottom (row 0 of the period)
    c = np.zeros(yy.shape)
    core = np.zeros(yy.shape)
    for r in COLLAR_ROWS:
        dv = (v - r - 0.08 * xx + th / 2) % th - th / 2
        c = np.maximum(c, np.clip(1 - np.abs(dv) / 4.5, 0, 1) > 0)
        core = np.maximum(core, np.abs(dv) < 1.6)
    return c, core


def ice_wall_rgb(wet):
    th = TEX_PERIOD
    yy, xx, G, T = _wall_fields(th)
    seed = 2361
    lo = np.array([c[0] for c in ICE_STEPS], float)[G]
    hi = np.array([c[1] for c in ICE_STEPS], float)[G]
    rgb = lo + (hi - lo) * T[..., None]
    fine = pn(TEX_W, th, 8, seed, 3)
    mid = pn(TEX_W, th, 32, seed + 1, 3)
    rgb *= (0.9 + 0.2 * mid)[..., None] * (0.95 + 0.1 * fine)[..., None]
    face = np.isin(G, [g for g in range(6) if FACE[g]])
    v = th - 1 - yy
    ph = (v * 32 / th + xx / TEX_W * 1.5 + 0.06 * (mid - 0.5)) % 1.0       # the drill's helix, unrolled
    groove = np.clip(1 - np.abs(ph - 0.5) / 0.12, 0, 1) ** 1.3
    ridge = np.clip(1 - np.abs(np.abs(ph - 0.5) - 0.34) / 0.1, 0, 1)
    k = 0.18 if wet else 0.30
    rgb = np.where(face[..., None], rgb * (1 - k * groove + 0.08 * ridge)[..., None], rgb)
    rng = np.random.default_rng(seed + 2)
    bub = np.zeros((th, TEX_W))
    for _ in range(th // 3):                             # trapped bubbles in the faces
        px, py, r = rng.uniform(0, TEX_W), rng.uniform(0, th), rng.uniform(0.6, 1.6)
        ys = np.arange(int(py) - 2, int(py) + 3) % th
        xs = np.clip(np.arange(int(px) - 2, int(px) + 3), 0, TEX_W - 1)
        dd = np.hypot(xs[None, :] - px, ys[:, None] - py)
        bub[np.ix_(ys, xs)] = np.maximum(bub[np.ix_(ys, xs)], dd < r)
    rgb = np.where((face & (bub > 0))[..., None], rgb * 0.72 + np.array((10, 20, 40)), rgb)
    # ledges: ice chips, a light front lip, a dark toe under the face above
    chips = pn(TEX_W, th, 4, seed + 3, 2)
    rgb = np.where((~face)[..., None], rgb * (0.9 + 0.2 * chips)[..., None], rgb)
    rgb = np.where((~face & (T < 0.14))[..., None], rgb * 1.12 + 6, rgb)
    rgb = np.where((~face & (T > 0.8))[..., None], rgb * 0.84, rgb)
    rgb = np.where((face & (T < 0.06))[..., None], rgb * 0.8, rgb)
    rim = (G == 5) & (T > 0.8)
    rgb = np.where(rim[..., None], np.array((240, 246, 252.0)) * (0.96 + 0.06 * fine)[..., None], rgb)
    c, core = _collar(yy, xx, th)
    rgb = np.where((c > 0)[..., None], BONE * (0.86 + 0.18 * fine)[..., None], rgb)
    rgb = np.where((core > 0)[..., None], np.array((40, 200, 140.0)), rgb)
    if wet:
        rgb = rgb * np.array((0.58, 0.78, 0.84)) + WATER * 0.35
        rgb = np.where((core > 0)[..., None], np.array((40, 200, 140.0)), rgb)
    return np.clip(rgb, 0, 255)


def ice_wall(wet=False):
    return finish(padded(ice_wall_rgb(wet)), 32, amount=0.04)


def ice_wall_glow():
    yy, xx, G, T = _wall_fields()
    _, core = _collar(yy, xx, TEX_PERIOD)
    soft = np.array(Image.fromarray((core * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(1.6))) / 255
    v = artkit.ordered_dither(np.clip(core + 0.6 * soft, 0, 1), 4)
    rgb = padded(TEAL * 0.85 * v[..., None])
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def ice_wall_foot(wet=False):
    tw, th = b16.FOOT_TEX
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    n = pn(tw, th, 8, 2371, 2)
    rng = np.random.default_rng(2373)
    peb = np.zeros((th, tw))
    for _ in range(300):
        px, py = abs(rng.normal(0, 7)), rng.uniform(0, th)
        r = rng.uniform(0.8, 2.4) * (1.3 - px / 30)
        dy = (yy - py + th / 2) % th - th / 2
        peb = np.maximum(peb, np.clip(1 - np.hypot(xx - px, dy) / max(r, 0.5), 0, 1) * r)
    hgt = 2.2 * peb + 0.8 * n + 3 * np.clip(1 - xx / 12, 0, 1)
    t = np.clip(0.45 + 0.35 * (n - 0.5) + 0.14 * peb, 0, 1)
    alb = ramp([(84, 110, 140), (132, 160, 188), (176, 200, 222), (214, 228, 242)], t)
    rgb = b16.lit_up(hgt, alb)
    rgb[:, 0:2] = np.array((22, 34, 54), float)
    rgb[:, 2:5] = ramp([(196, 216, 234), (232, 242, 250)], n[:, 2:5])
    if wet:
        rgb = rgb * np.array((0.58, 0.78, 0.84)) + WATER * 0.35
    cover = np.clip(1.25 - xx / 18, 0, 1)
    cover = np.maximum(cover, (peb > 0.4) * np.clip(1.4 - xx / 26, 0, 1))
    return finish(rgb, 20, dither_alpha(cover, 2), amount=0.04)


WALLS = {"ice-wall": lambda: ice_wall(False), "ice-wall-wet": lambda: ice_wall(True), "ice-wall-glow": ice_wall_glow,
         "ice-wall-foot": lambda: ice_wall_foot(False), "ice-wall-foot-wet": lambda: ice_wall_foot(True)}


# --------------------------------------------------------------------------- pieces

LIP_ROW = 600


def shelf_lip():
    """Rows 0..959: the shelf's ice (Level 22's surface), the mouth's ragged lip (apex at LIP_ROW, bulging toward
    the ship), the dry shaft beyond in the lip's shadow, fading into shaft-dry at the top."""
    h = TILE_H
    hgt, xx, yy = b22.shelf_height(2381)
    hgt += b22.double_ridges(xx, yy, 2382, 3)
    crack = b22.fissures(2383)
    hgt -= 0.03 * crack
    ice = b22.colour_ice(hgt)
    ice = ice * (1 - 0.3 * crack)[..., None] + np.array((60, 120, 170)) * 0.3 * crack[..., None]
    rows = h - 1 - yy                                          # ground rows (piece bottom = row 0)
    wob = 9 * np.sin(xx / 23 + 1.0) + 5 * np.sin(xx / 7.3) + 8 * (pn(W, h, 48, 2384, 3) - 0.5)
    lip = LIP_ROW + 0.0036 * (xx - 240) ** 2 + wob
    shaft = tile_rows("shaft-dry", 0, h)[..., :3].astype(float)
    beyond = rows > lip
    shade = np.clip(0.35 + (rows - lip) / 90, 0.35, 1.0)
    rgb = np.where(beyond[..., None], shaft * shade[..., None], ice)
    d = lip - rows                                             # px below the lip on the ice
    blocks = (pn(W, h, 8, 2385, 2) > 0.45) & (d > 0) & (d < 9 + 6 * pn(W, h, 16, 2386, 2))
    rgb = np.where(blocks[..., None], np.array((174, 196, 218.0)) * (0.9 + 0.2 * pn(W, h, 4, 2387, 2))[..., None],
                   rgb)
    rgb = np.where(((d >= 0) & (d < 2.2))[..., None], np.array((238, 244, 252.0)), rgb)   # the lit rim
    rgb = np.where(((d < 0) & (d > -3))[..., None], rgb * 0.5, rgb)                     # under the overhang
    tail = (d > 0) & (d < 120) & (pn(W, h, 24, 2388, 3) > 0.6)                         # drill tailings
    rgb = np.where(tail[..., None], rgb * 0.92 + np.array((6, 10, 14)), rgb)
    stain = (d > 0) & (d < 70) & (pn(W, h, 32, 2389, 3) > 0.7)                         # Vrell teal stains
    rgb = np.where(stain[..., None], rgb * 0.6 + np.array((10, 70, 64)) * 0.4, rgb)
    img = posterize(rgb, 40)
    dr = ImageDraw.Draw(img)
    for x in (40, 128, 240, 352, 440):                           # the collars on the rim: bone with a teal core
        y = int(h - 1 - (LIP_ROW + 0.0036 * (x - 240) ** 2))
        dr.rectangle([x - 9, y - 3, x + 9, y + 3], fill=(196, 184, 164, 255), outline=(90, 80, 70, 255))
        dr.line([x - 7, y, x + 7, y], fill=(40, 220, 150, 255), width=1)
    a = np.array(img)
    k = np.arange(h)[:, None]
    fade = np.clip((k + 0.5) / FEATHER, 0, 1)                      # only the top edge fades (into shaft-dry)
    a[..., 3] = np.where(dither_alpha(np.broadcast_to(fade, (h, W)).copy(), 2) > 0, 255, 0)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA"), 0


def waterline():
    """The waterline band at WATERLINE_ROW: the dry shaft below, the wet shaft above (fading into section 2's
    shaft-wet at its seam), the meniscus line, a foam fringe and the surface's light just ahead."""
    bottom = WATERLINE_ROW - 48
    top = int(round(seam_row(WL_INDEX))) + FEATHER + 32
    h = top - bottom
    yy, xx = grid(h)
    rows = bottom + h - 1 - yy
    dry = tile_rows("shaft-dry", bottom, h)[..., :3].astype(float)
    wet = tile_rows("shaft-wet", bottom, h)[..., :3].astype(float)
    line = WATERLINE_ROW + 2.2 * np.sin(xx / 9.0) + 1.2 * np.sin(xx / 3.7 + 1)
    d = rows - line
    rgb = np.where((d >= 0)[..., None], wet, dry)
    sheen = np.clip(1 - d / 46, 0, 1) * (d >= 0)
    rgb = rgb + np.array((40, 90, 100)) * (artkit.ordered_dither(sheen * 0.8, 4))[..., None]
    rgb = np.where(((d < 0) & (d > -4))[..., None], rgb * 0.62, rgb)
    foam = (d >= 2) & (d < 12) & (pn(W, 64, 4, 2391, 2)[np.mod(yy, 64).astype(int), xx.astype(int)] >
                                   0.45 + 0.05 * d)
    rgb = np.where(foam[..., None], np.array((200, 230, 240.0)), rgb)
    rgb = np.where(((d >= 0) & (d < 2))[..., None], np.array((224, 246, 255.0)), rgb)
    alpha = feather_alpha(h, W)
    return posterize(rgb, 40, alpha), bottom


def waterline_foam():
    """Low-air: the foam lace along the waterline, translucent (stepped alpha), no bubbles."""
    w, h = 360, 40
    yy, xx = grid(h, w)
    line = h / 2 + 2.5 * np.sin(xx / 11.0) + 1.5 * np.sin(xx / 4.1 + 2)
    d = np.abs(yy - line)
    lace = (raster.fbm(w, h, 6, 2395, octaves=2) > 0.5) & (d < 7)
    a = np.where(d < 1.5, 0.75, 0.0) + np.where(lace, 0.55 * np.clip(1 - d / 7, 0, 1), 0)
    ends = np.clip(np.minimum(xx, w - 1 - xx) / 40, 0, 1)
    a = np.clip(a, 0, 1) * ends
    alpha = np.round(a * 4) / 4 * 255
    rgb = np.ones((h, w, 3)) * np.array((226, 244, 252.0))
    return posterize(rgb, 8, alpha)


def ledge(side, wet, probe=False, seed=2401):
    """An ice ledge at a wall's foot: a slab of chipped ice jutting from the wall (left: from column 0), the mount
    at the piece's centre (32, 28) with a bone-and-teal Vrell collar round it. Lit from the upper left; the right
    ledge is its own render (nothing lit mirrored)."""
    w, h, ss = 64, 56, 4
    yy, xx = np.mgrid[0:h * ss, 0:w * ss].astype(np.float64) / ss
    u = xx if side == 0 else (w - 1 - xx)                     # px from the wall side
    n = raster.fbm(w * ss, h * ss, 24, seed + side, octaves=3)
    # step C5a: the slab tapers to nothing at its top and bottom rows (a rounded outline, no flat cut showing past
    # the wall's foot band) and leaves the wall-side column clear (the foot band covers the join)
    taper = np.sqrt(np.clip(1 - ((yy - h / 2) / (h / 2 - 2.5)) ** 2, 0, 1))
    edge = (46 + 8 * np.sin(yy / 6.0 + side) + 10 * (n - 0.5)) * taper
    inside = (u < edge) & (u >= 1)
    hgt = np.where(inside, 6 + 3 * n - 0.06 * u, 0)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1 - (gx + gy) * 1.8, 0.45, 1.35)
    base = np.array((156, 182, 206.0)) if not wet else np.array((92, 128, 146.0))
    rgb = base * shade[..., None] * (0.92 + 0.16 * n)[..., None]
    rim = inside & (u > edge - 2.5)
    rgb = np.where(rim[..., None], rgb * 0.62, rgb)
    r = np.hypot(xx - 32, yy - 28)
    ring = inside & (r > 12) & (r < 17)
    rgb = np.where(ring[..., None], BONE * (0.9 + 0.15 * n)[..., None] * (0.7 if wet else 1.0), rgb)
    core = inside & (np.abs(r - 14.5) < 0.9)
    rgb = np.where(core[..., None], np.array((40, 210, 146.0)), rgb)
    if probe:                                                 # scrape marks and the probe's torn cable
        scr = inside & (np.abs((yy - 0.6 * u) % 9 - 4.5) < 0.6) & (r > 18)
        rgb = np.where(scr[..., None], rgb * 0.7, rgb)
        cab = inside & (np.abs(yy - (40 + 6 * np.sin(u / 7))) < 1.2) & (u > 4) & (r > 17)
        rgb = np.where(cab[..., None], np.where(((u // 3) % 2 == 0)[..., None], np.array((220, 180, 40.0)),
                                                np.array((30, 30, 34.0))), rgb)
    a = inside.astype(float)
    rgb = rgb.reshape(h, ss, w, ss, 3).mean(axis=(1, 3))
    a = a.reshape(h, ss, w, ss).mean(axis=(1, 3))
    return posterize(rgb, 24, (a >= 0.5) * 255)


def seam_piece(lower, upper, index, seed, band, h=320):
    """A section seam at exact rows: the lower tile set below a ragged line, the upper above it, a band along the
    line (``band``: 'shaft' rubble of ice at the shaft's foot, 'plain' silt), both ends feathered."""
    seam = seam_row(index)
    bottom = int(round(seam)) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = grid(h)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    above = yy < line
    rgb = np.where(above[..., None], up, lo)
    d = yy - line
    rng = np.random.default_rng(seed)
    img = posterize(rgb, 40, feather_alpha(h, W))
    lay = Image.new("RGBA", (W, h), (0, 0, 0, 0))
    dr = ImageDraw.Draw(lay)
    for _ in range(90 if band == "shaft" else 40):
        x = rng.uniform(0, W)
        yl = h / 2 + 34 * math.sin(x / 83 + seed) + 30 * (n[int(h / 2), int(min(W - 1, x))] - 0.5) * 2
        y = yl + rng.normal(0, 10)
        if band == "shaft":
            s = rng.uniform(2, 6)
            c = tuple(int(v) for v in (np.array((150, 178, 196)) * rng.uniform(0.6, 1.0))) + (255,)
            dr.polygon([(x - s, y), (x, y - s * 0.8), (x + s, y + 0.3 * s), (x + 0.2 * s, y + s)], fill=c)
            dr.line([(x - s, y), (x, y - s * 0.8)], fill=(214, 228, 240, 255))
        else:
            for _ in range(5):
                a, ln = rng.uniform(0, TAU), rng.uniform(5, 12)
                dr.line([x, y, x + math.cos(a) * ln, y + math.sin(a) * ln], fill=(30, 84, 62, 255), width=2)
    la = np.array(lay)
    la[..., 3] = np.where(np.array(img)[..., 3] > 0, la[..., 3], 0)
    img.alpha_composite(Image.fromarray(la, "RGBA"))
    _ = d
    return img, bottom


# Racks and domes: drawn over the floor rows under them with their lamp pools baked (floor x (1 + 1.4 g WARM) + 70 g WARM).

def lamp_gain(lamps, h, w, x0, bottom):
    """The lamps' light (sum of Gaussians, peak 1 each) on a piece's pixels; lamps are (x, ground row, r)."""
    yy, xx = grid(h, w)
    rows = bottom + h - 1 - yy
    g = np.zeros((h, w))
    for lx, lr, r in lamps:
        g += np.exp(-(((xx + x0) - lx) ** 2 + (rows - lr) ** 2) / (2 * r * r))
    return np.clip(g, 0, 1.3)


def lit_piece(bottom, x0, w, h, draw, lamps, mask=None, colours=48):
    """The floor rows under a piece, the drawing over them, lit by the lamps; opaque where lit or drawn, dithered
    out where the pool fades (those pixels equal the floor under them anyway)."""
    floor = tile_rows("thera-farms", bottom, h)[:, x0:x0 + w, :3].astype(float) if "plain" not in draw.__name__ \
        else tile_rows("plain-edge", bottom, h)[:, x0:x0 + w, :3].astype(float)
    ss = 2
    big = Image.fromarray(floor.astype(np.uint8), "RGB").convert("RGBA").resize((w * ss, h * ss), Image.NEAREST)
    lay = Image.new("RGBA", big.size, (0, 0, 0, 0))
    drawn = draw(lay, ss)
    big.alpha_composite(lay)
    rgb = np.array(big.resize((w, h), Image.BOX).convert("RGB")).astype(float)
    cover = np.array(lay.resize((w, h), Image.BOX))[..., 3] / 255.0
    g = lamp_gain(lamps, h, w, x0, bottom)
    yy, xx = grid(h, w)
    dl = xx if x0 > 0 else np.full_like(xx, 1e9)          # a piece on the field's edge needs no fade there
    dr = (w - 1 - xx) if x0 + w < W else np.full_like(xx, 1e9)
    edge = np.minimum(np.minimum(dl, dr), np.minimum(yy, h - 1 - yy))
    u = np.clip(edge / 48.0, 0, 1)
    g = g * u * u * (3 - 2 * u)                           # the pool fades out inside the piece: no box edge
    rgb = rgb * (1 + 1.4 * g[..., None] * WARM) + 70 * g[..., None] * WARM
    a = np.clip((g - 0.03) / 0.10, 0, 1)
    a = np.maximum(a, (cover > 0.5).astype(float))
    alpha = dither_alpha(a, 2)
    _ = drawn
    return posterize(np.clip(rgb, 0, 255), colours, alpha)


def rack_layout(i, t, x):
    """A rack under pump i: its extent in ground rows and x, the lamp bars, the pump's socket row."""
    prow = target_centre(t, 40)
    lo, hi = prow - 70, prow + 70
    nest = [(tt, xx) for k, tt, xx in TARGETS if k == "reef-spitter" and wall_side(xx, target_centre(tt, 36)) is None
            and abs(xx - x) < 120 and 0 < tt - t < 12]
    for tt, _ in nest:
        hi = max(hi, target_centre(tt, 36) + 60)
    rw = 176
    rx0 = int(np.clip(x - rw / 2, 8, W - 8 - rw))
    bars = [lo - 8, hi + 8] + list(np.arange(lo + 160, hi - 80, 180))
    return dict(prow=prow, lo=lo, hi=hi, rx0=rx0, rw=rw, bars=bars, x=x)


def rack_piece(i, t, x):
    L = rack_layout(i, t, x)
    m = 100
    bottom = int(math.floor(L["lo"] - m))
    h = int(math.ceil(L["hi"] + m)) - bottom
    w = min(W, L["rw"] + 2 * m)
    x0 = int(np.clip(L["rx0"] + L["rw"] / 2 - w / 2, 0, W - w))
    lamps = [(L["rx0"] + L["rw"] * k / 4, b, 60) for b in L["bars"] for k in (0.5, 1.5, 2.5, 3.5)]
    rng = np.random.default_rng(2410 + i)

    def y_of(row, ss):
        return (bottom + h - 1 - row) * ss

    def draw_rack(lay, ss):
        d = ImageDraw.Draw(lay)
        X0, X1 = (L["rx0"] - x0) * ss, (L["rx0"] + L["rw"] - x0) * ss
        Y0, Y1 = y_of(L["hi"], ss), y_of(L["lo"], ss)
        dark, mid, hi_, tip = (6, 44, 28), (22, 104, 56), (70, 160, 90), (120, 200, 120)
        ry = Y0 + 10 * ss
        while ry < Y1 - 6 * ss:                               # kelp rows: lit strips, fronds, notches
            d.rectangle([X0 + 5 * ss, ry - 4 * ss, X1 - 5 * ss, ry + 4 * ss], fill=dark + (255,))
            d.rectangle([X0 + 5 * ss, ry - 4 * ss, X1 - 5 * ss, ry - 2 * ss], fill=mid + (255,))
            rx = X0 + 8 * ss
            while rx < X1 - 8 * ss:
                d.rectangle([rx, ry - 4 * ss, rx + ss, ry + 4 * ss], fill=dark + (255,))
                fl = rng.uniform(3, 6) * ss
                d.line([rx + 2 * ss, ry, rx + 2 * ss + fl * 0.6, ry - fl], fill=hi_ + (255,), width=ss)
                d.point([(rx + 3 * ss, ry - 3 * ss)], fill=tip + (255,))
                rx += int(rng.integers(9, 14)) * ss
            ry += 13 * ss
        d.rectangle([X0, Y0, X1, Y1], outline=(70, 90, 100, 255), width=2 * ss)        # the float-line frame
        for row in (L["lo"], L["hi"]):
            for k in range(0, L["rw"] + 1, 22):
                cx, cy = X0 + k * ss, y_of(row, ss)
                d.rectangle([cx - 2 * ss, cy - 2 * ss, cx + 2 * ss, cy + 2 * ss], fill=(110, 122, 140, 255))
        for b in L["bars"]:                                      # the grow-lamp bars across the rack
            by = y_of(b, ss)
            d.rectangle([X0 - 4 * ss, by - 2 * ss, X1 + 4 * ss, by + 2 * ss], fill=(60, 66, 76, 255))
            for k in range(6, L["rw"], 12):
                cx = X0 + k * ss
                d.rectangle([cx - 2 * ss, by - ss, cx + 2 * ss, by + ss], fill=LAMP + (255,))
        px, py = (L["x"] - x0) * ss, y_of(L["prow"], ss)         # the pump's socket and its teal roots
        for k in range(7):
            a = k * TAU / 7 + rng.uniform(-0.3, 0.3)
            ln = rng.uniform(26, 44) * ss
            pts = [(px, py)]
            for s in range(1, 6):
                aa = a + rng.normal(0, 0.25)
                pts.append((pts[-1][0] + math.cos(aa) * ln / 5, pts[-1][1] + math.sin(aa) * ln / 5))
            d.line(pts, fill=(40, 180, 140, 255), width=2 * ss)
        r = 17 * ss
        d.ellipse([px - r, py - r, px + r, py + r], fill=(26, 18, 34, 255), outline=(60, 150, 120, 255),
                  width=2 * ss)
        return True

    img = lit_piece(bottom, x0, w, h, draw_rack, lamps)
    return img, bottom, x0


@lru_cache(maxsize=None)
def _dome_sprites():
    glass = s6.sm.PaletteShim(s6.B, {("EARTH ORBIT", 3): (60, 120, 150), ("UTC HULL", 4): (120, 130, 150)})
    return {r: s6.station.render_part(s6.station.dome_part(r), glass) for r in (15, 20, 28)}


DOME_SETS = [  # (dx, dy, radius) round the cluster's centre; tubes from the first
    [(0, 0, 28), (64, -38, 20), (56, 52, 15), (-58, 40, 20)],
    [(0, 0, 20), (-60, -30, 28), (54, -48, 15), (40, 46, 20)],
]


def dome_piece(i, t, x):
    spec = DOME_SETS[i % 2]
    row0 = target_centre(t, 0)
    w, h = 220, 200
    x0 = int(np.clip(x - w / 2, 0, W - w))
    bottom = int(round(row0 - h / 2))
    cx, cy = x - x0, h / 2
    lamps = [(x + dx, row0 - dy, 22 + r) for dx, dy, r in spec]

    def draw_domes(lay, ss):
        sp = _dome_sprites()
        d = ImageDraw.Draw(lay)
        ax, ay = (cx + spec[0][0]) * ss, (cy + spec[0][1]) * ss
        for dx, dy, _ in spec[1:]:
            bx, by = (cx + dx) * ss, (cy + dy) * ss
            d.line([ax, ay, bx, by], fill=(40, 48, 60, 255), width=11 * ss)
            d.line([ax, ay, bx, by], fill=(110, 122, 140, 255), width=7 * ss)
            d.line([ax - ss, ay - 2 * ss, bx - ss, by - 2 * ss], fill=(170, 182, 196, 255), width=2 * ss)
        for dx, dy, r in spec:
            s = sp[r].resize((sp[r].width * ss, sp[r].height * ss), Image.NEAREST)
            sh = artkit.sprite.shadow_of(s, opacity=0.6, blur=1.5 * ss)
            lay.alpha_composite(sh, (int((cx + dx) * ss - s.width / 2 + 5 * ss), int((cy + dy) * ss - s.height / 2
                                                                                     + 7 * ss)))
            lay.alpha_composite(s, (int((cx + dx) * ss - s.width / 2), int((cy + dy) * ss - s.height / 2)))
        return True

    return lit_piece(bottom, x0, w, h, draw_domes, lamps), bottom, x0


def plain_lamp_piece(i, t, x):
    w, h = 140, 140
    row0 = target_centre(t, 0)
    x0 = int(np.clip(x - w / 2, 0, W - w))
    bottom = int(round(row0 - h / 2))
    cx, cy = x - x0, h / 2

    def draw_plain(lay, ss):
        d = ImageDraw.Draw(lay)
        X, Y = cx * ss, cy * ss
        d.ellipse([X - 9 * ss, Y - 9 * ss, X + 9 * ss, Y + 9 * ss], fill=(48, 54, 62, 255), outline=(20, 24, 30, 255),
                  width=ss)
        d.ellipse([X - 4 * ss, Y - 4 * ss, X + 4 * ss, Y + 4 * ss], fill=LAMP + (255,))
        for k in range(3):                                        # the pylon's anchor cables
            a = k * TAU / 3 + 0.5
            d.line([X, Y, X + math.cos(a) * 24 * ss, Y + math.sin(a) * 24 * ss], fill=(70, 76, 84, 255), width=ss)
        return True

    return lit_piece(bottom, x0, w, h, draw_plain, [(x, row0, 36)], colours=32), bottom, x0


def rack_rects():
    """Each rack piece's (row lo, row hi, x lo, x hi), as rack_piece cuts it."""
    out = []
    for i, (t, x) in enumerate(PUMPS, 1):
        L = rack_layout(i, t, x)
        w = min(W, L["rw"] + 200)
        x0 = int(np.clip(L["rx0"] + L["rw"] / 2 - w / 2, 0, W - w))
        out.append((L["lo"] - 100, L["hi"] + 100, x0, x0 + w))
    return out


def dome_spots():
    """Between the racks, on the far side from the pumps (README: domes as scenery with their lamps), never over a
    rack's piece (its pool would be cut)."""
    if len(PUMPS) < 2:
        return []
    rects = rack_rects()
    out = []
    mids = [((t0 + t1) / 2, (x0 + x1) / 2) for (t0, x0), (t1, x1) in zip(PUMPS, PUMPS[1:])]
    mids.append((PUMPS[-1][0] + 7, PUMPS[-1][1]))
    for tm, xm in mids:
        for x in ((400, 80) if xm < 240 else (80, 400)):
            r = target_centre(tm, 0)
            hit = any(lo - 100 < r < hi + 100 and xa - 110 < x < xb + 110 - 0 and not (x + 110 <= xa or x - 110 >= xb)
                      for lo, hi, xa, xb in rects)
            if not hit:
                out.append((tm, x))
                break
    return out


def lamp_spots():
    return [(END - 14, 380), (END - 2.2, 96)]


# --------------------------------------------------------------------------- build

def _tile_job(name):
    return TILES[name]()


def _wall_job(name):
    return WALLS[name]()


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in list(OUT.glob("*.png")):
        old.unlink()
    with ProcessPoolExecutor() as ex:
        imgs = list(ex.map(_tile_job, list(TILES)))
        walls = list(ex.map(_wall_job, list(WALLS)))
    for name, img in zip(TILES, imgs):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    for name, img in zip(WALLS, walls):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    tile.cache_clear()
    pieces, placed = {}, []

    def put(name, img, layer, note, t, x):
        if name not in pieces:
            artkit.save_png(img, OUT / f"{name}.png", SOURCE)
            pieces[name] = {"layer": layer, "size": [img.width, img.height]}
        placed.append((note, name, t, x))

    img, bottom = shelf_lip()
    put("shelf-lip", img, "ground", "1. the shelf's ice and the shaft's mouth (lip apex at ground row {}); exact rows"
        .format(LIP_ROW), exact_t(bottom, img.height), 240)
    img, bottom = waterline()
    put("waterline", img, "ground", f"the waterline band at ground row {WATERLINE_ROW} = scroll({WL_T:g}) + {SHIP_Y} "
        "(the ship's start height); exact rows", exact_t(bottom, img.height), 240)
    foam = waterline_foam()
    tf = round(time_at(scroll(WL_T) - (SCREEN / 2 - SHIP_Y) / FACTORS["low-air"]), 4)
    put("waterline-foam", foam, "low-air", f"the waterline's foam (low-air x1.35) crossing the ship's row at t={WL_T:g}",
        tf, 240)
    ledges = {}
    for k, t, x in TARGETS:
        row = target_centre(t, 32)
        side = wall_side(x, row)
        if side is None:
            continue
        wet = row > WATERLINE_ROW
        probe = k == "probe"
        name = "probe-ledge" if probe else f"ice-ledge-{'left' if side == 0 else 'right'}{'-wet' if wet else ''}"
        if name not in ledges:
            ledges[name] = ledge(side, wet, probe)
        put(name, ledges[name], "ground", f"the {k} at t={t:g} x {x:g} on its ledge ({'left' if side == 0 else 'right'}"
            " wall)", round(place_t(row), 4), int(round(x)))
    for i, (lower, upper, band) in ((3, ("shaft-wet", "thera-farms", "shaft")), (4, ("thera-farms", "plain-edge",
                                                                                     "plain"))):
        if i >= len(SECTIONS):
            continue
        img, bottom = seam_piece(lower, upper, i, 2420 + i, band)
        name = "seam-thera" if upper == "thera-farms" else "seam-plain"
        put(name, img, "ground", f"section {i + 1}'s seam (ground {seam_row(i):.0f}): {lower} into {upper}; exact rows",
            exact_t(bottom, img.height), 240)
    for i, (t, x) in enumerate(PUMPS, 1):
        img, bottom, x0 = rack_piece(i, t, x)
        put(f"farm-rack-{i}", img, "ground", f"rack {i} under the siphon pump at t={t:g} x {x:g} (its lamp pool baked); "
            "exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    for i, (t, x) in enumerate(dome_spots(), 1):
        img, bottom, x0 = dome_piece(i, t, x)
        put(f"thera-domes-{i}", img, "ground", f"Thera Deep's domes (t~{t:g}); exact rows", exact_t(bottom, img.height),
            x0 + img.width // 2)
    for i, (t, x) in enumerate(lamp_spots(), 1):
        img, bottom, x0 = plain_lamp_piece(i, t, x)
        put(f"plain-lamps-{i}", img, "ground", "the last lamps of Thera Deep on the plain; exact rows",
            exact_t(bottom, img.height), x0 + img.width // 2)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets, {len(WALLS)} wall textures and {len(pieces)} pieces to "
          f"{OUT.relative_to(ROOT)}; waterline row {WATERLINE_ROW}")


# --------------------------------------------------------------------------- proposal

DESC = {
    "shaft-dry": "sections 1: the dry shaft from above, the ice receding to a dark trough, the drill's grooves, a "
                 "warm glow far below",
    "shaft-wet": "sections 2-3: the shaft under water, teal and darker, the glow stronger",
    "thera-farms": "section 4: the chosen round-07 kelp floor (the racks and domes are pieces)",
    "plain-edge": "section 5: the dark silt plain, sparse kelp",
    "ice-ceiling": "sections 3-5: the ice ceiling's cracks of light (additive at 40 %)",
}
META = None


def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if DATA else "the README (no data.yaml yet: re-run after C2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 23's shaft and Thera Deep (tools/art/backdrop_l23.py, from {src}). No deep or",
             "  # plateau layer (the ground is opaque; the walls' rims project beyond the field's edges). The walls are the",
             "  # terrain's (WallProjection): textures ice-wall (dry) / ice-wall-wet (below the waterline), ice-wall-glow",
             "  # (additive collar cores), ice-wall-foot / ice-wall-foot-wet, in CanyonProfile's convention.",
             f"  # The waterline: ground row {WATERLINE_ROW} = scroll({WL_T:g}) + {SHIP_Y} (terrain row {WATERLINE_ROW - SCREEN});",
             "  # the grade wipes above it. underwater_look (C4): keep ambient/tint overridable per section (part D: Level 24",
             "  # darkens section by section); the lamp pools are baked into the rack, dome and lamp pieces.",
             "  scroll_factors: {ground: 1, low-air: 1.35, high-air: 2.2}",
             "  ramp: 2",
             "  haze_colour: 0a4a5c  # the water's teal (europa_r48 WATER)",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:"]
    for name in TILES:
        layer = "high-air" if name == "ice-ceiling" else "ground"
        lines.append(f"    {name}: {{layer: {layer}, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in m["pieces"].items():
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]}}")
    lines.append("  placed:")
    for note, piece, t, x in m["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    lines.append("# sections' tiles: " + "; ".join(f"{i + 1}: [{', '.join(t)}]" for i, t in enumerate(SECTION_TILES)))
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.write_text(proposal_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite, checks and review

def placed_pieces(layer="ground"):
    out = []
    for note, piece, t, x in meta()["placed"]:
        if meta()["pieces"][piece]["layer"] != layer:
            continue
        img = np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))
        h = img.shape[0]
        f = FACTORS[layer]
        bottom = int(round(scroll(float(t)) * f + SCREEN / 2 - h / 2))
        out.append((piece, bottom, int(round(float(x) - img.shape[1] / 2)), img))
    return out


def section_at_row(row):
    s = 0
    for i in range(1, len(SECTIONS)):
        if row >= seam_row(i):
            s = i
    return s


def _paste_rows(img, arr, x0, y_top):
    h, w = arr.shape[:2]
    if x0 < 0:
        arr, x0 = arr[:, -x0:], 0
    if x0 + arr.shape[1] > W:
        arr = arr[:, :W - x0]
    img.alpha_composite(Image.fromarray(np.ascontiguousarray(arr), "RGBA"), (x0, y_top)) if -h < y_top < SCREEN \
        else None


def ground_view(t, pieces):
    base = scroll(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        row = int(math.floor(base + SCREEN - 1 - y))
        out[y] = tile(SECTION_TILES[section_at_row(row)][0])[TILE_H - 1 - row % TILE_H]
    img = Image.fromarray(out, "RGBA")
    for _, bottom, x0, arr in pieces:
        h = arr.shape[0]
        y_top = int(round(base + SCREEN - 1 - (bottom + h - 1)))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        _paste_rows(img, arr, x0, y_top)
    return img


def draw_walls(img, t):
    """The walls approximated (the game projects them round the field's centre): each screen row maps the wall's
    columns from the foot to the field's edge, the left wall shaded (key light from the upper left), the foot band."""
    a = np.array(img).astype(np.float64)
    base = scroll(t)
    texd, texw = tile("ice-wall"), tile("ice-wall-wet")
    glow = tile("ice-wall-glow")
    fd, fw = tile("ice-wall-foot"), tile("ice-wall-foot-wet")
    for y in range(SCREEN):
        row = base + SCREEN - 1 - y
        trow = row - SCREEN
        wet = row > WATERLINE_ROW
        tex, ft = (texw, fw) if wet else (texd, fd)
        vr = int(TEX_PERIOD + TEX_PAD - 1 - (trow % TEX_PERIOD))
        frow = int(ft.shape[0] - 1 - (trow % ft.shape[0]))
        for side in (0, 1):
            fx = foot(side, row)
            span = fx if side == 0 else W - fx
            if span < 1:
                continue
            for k in range(24):                                 # the foot band on the floor
                x = int(fx) + k if side == 0 else int(fx) - 1 - k
                if 0 <= x < W and ft[frow, k, 3]:
                    a[y, x, :3] = ft[frow, k, :3]
            xs = np.arange(int(fx)) if side == 0 else np.arange(int(math.ceil(fx)), W)
            if len(xs) == 0:
                continue
            u = ((fx - xs) / span if side == 0 else (xs - fx) / span) * (TEX_W - 1)
            u = np.clip(u, 0, TEX_W - 1).astype(int)
            shade = 0.62 if side == 0 else 1.0
            shade_u = shade * (0.85 + 0.25 * u / (TEX_W - 1))
            a[y, xs, :3] = tex[vr, u, :3] * shade_u[:, None] + glow[vr, u, :3] * 0.8
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def grade(img, t, everywhere=False):
    """The review's stand-in for C4's under-water grade: ambient 0.58 x a cool tint above the waterline row."""
    a = np.array(img).astype(np.float64)
    base = scroll(t)
    rows = base + SCREEN - 1 - np.arange(SCREEN)
    under = (rows > WATERLINE_ROW) if not everywhere else np.ones(SCREEN, bool)
    g = a[..., :3] * GRADE + np.array((2, 10, 14))
    a[..., :3] = np.where(under[:, None, None], g, a[..., :3])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def held_frames(name):
    for folder in (artkit.SPRITES, HELD):
        single = folder / f"{name}.png"
        if single.exists():
            return [Image.open(single).convert("RGBA")]
        fs = sorted(folder.glob(f"{name}_*.png"), key=lambda p: int(p.stem.rsplit("_", 1)[1]) if
                    p.stem.rsplit("_", 1)[1].isdigit() else 0)
        fs = [p for p in fs if p.stem.rsplit("_", 1)[1].isdigit()]
        if fs:
            return [Image.open(p).convert("RGBA") for p in fs]
    return []


def composite(t, gpieces, lpieces, hpieces):
    img = ground_view(t, gpieces)
    img = draw_walls(img, t)
    base = scroll(t)
    stand = {"spine-turret": held_frames("spine-turret"), "reef-spitter": held_frames("reef-spitter"),
             "siphon-pump": held_frames("siphon-pump")}
    for k, tt, x in TARGETS:
        fr = stand.get(k) or []
        if not fr:
            continue
        y = base + SCREEN - target_centre(tt, fr[0].height)
        if -60 < y < SCREEN + 60:
            artkit.sprite.paste_center(img, fr[0], x, y)
    for _, bottom, x0, arr in lpieces:                     # low-air at x1.35
        h = arr.shape[0]
        y_top = int(round(base * FACTORS["low-air"] + SCREEN - 1 - (bottom + h - 1)))
        if -h < y_top < SCREEN:
            _paste_rows(img, arr, x0, y_top)
    ship = artkit.load_frames("ship-hydro") or artkit.load_frames("ship")
    artkit.sprite.paste_center(img, ship[2], 240, SCREEN - SHIP_Y)
    img = grade(img, t)
    hi = Image.new("RGBA", (W, SCREEN), (0, 0, 0, 255))
    rows = base * FACTORS["high-air"] + SCREEN - 1 - np.arange(SCREEN)
    if section_at_row(base + SCREEN / 2) >= 2:
        c = tile("ice-ceiling")
        hi = Image.fromarray(c[(TILE_H - 1 - np.mod(rows.astype(int), TILE_H))], "RGBA")
        a = np.array(img).astype(np.float64)
        a[..., :3] += np.array(hi)[..., :3] * 0.4
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return img


def wrap_score(a):
    """BackdropSeamsTest.wrapScore: the mean luminance change (lowest 90 % of the columns) of the row pairs at the
    wrap over the median of the 80 rows around it (at least 0.5); step C5a (thera-farms scored 4.84)."""
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def check():
    problems = []
    for name in TILES:
        a = tile(name)[..., :3].astype(float)
        jump = np.abs(a[0] - a[-1]).mean()
        inner = np.abs(a[1:] - a[:-1]).mean()
        if jump > 3 * inner + 6:
            problems.append(f"{name}: wrap seam {jump:.1f} vs {inner:.1f}")
        if name != "ice-ceiling" and wrap_score(tile(name)) > 1.5:   # the game's BackdropSeamsTest measure
            problems.append(f"{name}: wrap score {wrap_score(tile(name)):.2f} (BackdropSeamsTest's limit 1.5)")
    for name in ("ice-wall", "ice-wall-wet"):                 # the pad repeats the period's first rows
        a = tile(name)
        if not np.array_equal(a[:TEX_PAD], a[TEX_PERIOD:TEX_PERIOD + TEX_PAD]):
            problems.append(f"{name}: pad rows differ from the period's first rows")
    for p in placed_pieces():                                   # exact rows reproduce
        pass
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in problems:
        print("  " + p)
    return problems


TIMES = [(3, "1 THE SHELF, THE MOUTH AHEAD"), (11, "1 OVER THE LIP, WALLS CLOSING"), (20, "1 THE DRY SHAFT, NESTS"),
         (25.5, "1/2 THE WATERLINE UNDER THE SHIP"), (40, "3 THE WET SHAFT, LEDGES"), (55, "3 THE PROBE'S LEDGE"),
         (75, "4 THE SHAFT OPENS ON THE FARMS"), (84, "4 RACK 1 AND ITS PUMP"), (111, "4 RACK 3, THE NEST, DOMES"),
         (150, "5 THE PLAIN'S EDGE, LAST LAMPS")]


def review():
    g, l_, hpieces = placed_pieces("ground"), placed_pieces("low-air"), placed_pieces("high-air")
    shots = [composite(t, g, l_, hpieces) for t, _ in TIMES]
    cols = 5
    cw, ch = 240, 270
    right = 3 * (100 + 8) + 16
    sheet = raster.sheet(16 + cols * (cw + 8) + 8 + right, 60 + 2 * (ch + 24) + 330,
                         "LEVEL 23 BACKDROP - THROUGH THE ICE (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R52")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/2 (WALLS APPROXIMATED, A STAND-IN GRADE ABOVE THE WATERLINE ROW "
                     f"{WATERLINE_ROW}; STAND-INS: NESTS, PUMPS, SHIP)", raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"T={t:g} {label}"[:40], raster.LABEL_DIM)
    x0 = 16 + cols * (cw + 8) + 8
    raster.draw_text(sheet, x0, 38, "TILES (1/4)", raster.LABEL)
    for k, name in enumerate(["shaft-dry", "shaft-wet", "thera-farms", "plain-edge"]):
        im = Image.fromarray(tile(name), "RGBA").resize((100, 200), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + (k % 3) * 108, 56 + (k // 3) * 226))
        raster.draw_text(sheet, x0 + (k % 3) * 108, 56 + (k // 3) * 226 + 204, name.upper()[:16], raster.LABEL_DIM)
    for k, name in enumerate(["ice-wall", "ice-wall-wet"]):
        im = Image.fromarray(tile(name), "RGBA").crop((0, 0, 128, 400)).resize((64, 200), Image.Resampling.BOX)
        sheet.alpha_composite(im, (x0 + 108 + k * 72, 282))
    raster.draw_text(sheet, x0 + 108, 486, "ICE-WALL DRY/WET", raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24)
    raster.draw_text(sheet, 16, y, "AT 1X: THE WATERLINE (T=25.5), RACK 1 (T=84), THE MOUTH (T=3)", raster.LABEL)
    sheet.alpha_composite(shots[3].crop((0, 300, 480, 600 - 30)), (16, y + 16))
    sheet.alpha_composite(shots[7].crop((0, 270, 480, 540)), (16 + 488, y + 16))
    sheet.alpha_composite(shots[0].crop((0, 0, 300, 270)), (16 + 2 * 488, y + 16))
    frames = [composite(t, g, l_, hpieces).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(20, 30, 0.1)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=10)


def main(argv):
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv and (OUT / "shaft-dry.png").exists() and PROPOSAL.exists():
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
