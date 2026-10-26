#!/usr/bin/env python3
"""Production art: the Level 28 backdrop, Abyssal Maw (design/campaign/act-4-deep-water/level-28-abyssal-maw; M8 part
H batch, concept round 58, step ART-H2).

Straight to production from the round-48 Europa mood stills (tools/concept/europa_r48.py: BASALT, FLESH and the
under-water look) and the act's earlier production backdrops, imported unchanged: Level 27's sea-floor tile maker and
the hatchery gate's ring of arches (backdrop_l27.py ``vent_floor``, ``gate_fields``, ``gate_glow``), Level 23's drill
shaft (backdrop_l23.py: the shaft floor, the ice-shaft wall textures, the ice ceiling's cracks; run in reverse), Level
22's ice (backdrop_l22.py ``ice_shelf``, ``ice_crystals``: Europa's ice, here at night under Jupiter) and the brood
carrier's hull sprite. Structure as backdrop_l27.py (exact rows, the scroll helpers in tools/art/l28kit.py, which ports
LevelData.scrollAt exactly). Posterized per image, wide gradients ordered-dithered.

**The grade is the game's** up to the breakout (``underwater_look`` per section, l28kit's proposal): the tiles and
pieces carry the unlit floor; the light is the section's ambient and tint (the gate's teal, the galleries' cool light
under the racks' teal and violet glows, the cavern's amber, the antechamber's red-amber, the arena's lime-white, the
shaft's cool light from above), the static pools this script lists (``lights:``) and the pieces' additive ``-glow``
(Backdrop.drawPieceGlows). From the breakout line (t=115) the level is in air: no grade, the night ice is lit in its
own pixels (Jupiter's cream from the right, blue shadows).

Tile sets (480x960, wrapping):
  gate-hall          ground, section 1: the hatchery's resin floor past the gate, chitin root veins, spore pods
  brood-gallery      ground, section 2: the gallery floor, resin grooved along the galleries
  spawning-floor     ground, section 3: resin ridges round hatching pools (pale eggs in amber fluid)
  antechamber-floor  ground, sections 4-5: dark basalt under resin, a network of red fissures (the arena's own rows
                     are the caldera piece)
  shaft-climb        ground, section 6: Level 23's wet shaft floor run in reverse, the vent's lime-amber glow below
  night-ice          ground, sections 7-10: Level 22's ice shelf at night under Jupiter
  ice-ceiling        high-air, section 6: Level 23's ice ceiling's cracks of light (additive at 40 % as Level 23's)
  ice-crystals       high-air, sections 8-10: Level 22's crystals, dimmed for the night
Wall textures (the shaft's terrain, WallProjection's ice shaft): ice-wall, ice-wall-wet, ice-wall-glow, ice-wall-foot,
ice-wall-foot-wet: Level 23's, rows reversed (the drill's helix runs the other way: the climb).
Pieces (ground unless named; exact rows):
  gate-ring (+ -glow)        480x720 at the start: Level 27's ring of chitin arches, its centre on the bottom edge
  flesh-lamp (+ -glow)       64x64: lamps of grown flesh on stalks (sections 1-2)
  seam-brood-gallery (+ -glow)  480x320: the gallery's mouth, a chitin lintel with its lamps
  egg-rack-a/-b (+ -glow)    112x480: a gallery rack, a chitin frame packed with pale pods (teal / violet cores)
  egg-rack-nest (+ -glow)    112x480: a rack whose end carries a Spine Turret's seat (t~30, three of them)
  gallery-wall-left/-right   128x200: the gallery wall's bulge where a Siren is grown (t~36)
  seam-spawning-floor        480x320: the galleries opening into the cavern
  collapsed-rack (+ -glow)   208x192 at t~58, x 90: the fallen rack, its scour where *Pelagic-3* lies (the trigger's
                             sprite: tools/art/l28_props.py ``recon-sub``)
  resin-ridge                272x128 at t~66: the Spine Turrets' ridge (five seats, medium's three in the middle)
  far-ridge                  208x112 at t~74: the Polyp Mortars' ridge
  siren-stack                264x160 at t~76: the cavern's last stack, the Sirens' seats (hard's third included)
  seam-antechamber-floor     480x320
  fissure-1..4 (+ -glow)     176x96: the antechamber's steaming fissures (the vent's red-amber)
  caldera (+ -glow)          480x1460: the caldera from its outer rim (the mortars' seats, t~92) to the shaft's foot:
                             the halted arena's whole screen (ground rows ARENA_SCROLL..+539), the throat at screen
                             (240, 330), ~140 px across, glowing lime-white; never moves while the Maw lives
  shaft-mouth                480x720 at the breakout line: the bore's broken ice lip at the ship's row at t=115 (the
                             water below it, the shattered night ice round it)
  ceiling-cracks             high-air 480x600: the ceiling's fractures closing on the ship's row at t=115
  drill-hole-1..2            200x200: other Vrell drill holes in the night ice (bone collars, teal strips, steam)
  vrell-carrier              high-air 168x380: a Vrell carrier risen from a drill hole (the brood carrier's hull)
  ice-split-left/-right      200x960: the open leads where the cruisers rose through the ice (t~120)
Review: design/campaign/act-4-deep-water/level-28-abyssal-maw/concept/backdrop-final-r58-a.png/.gif (the hatchery
light's stand-in, tools/art/l28kit.py; the shaft's walls approximated as flat bands; stand-ins for the units: the
turrets, mortars, Sirens, *Pelagic-3* (l28_props.py when written), the ship); `--proposal` writes the `backdrop:` block,
the sections, the shaft's terrain, the arena's and the breakout's rows, the ground targets' places, the buoy, the
per-section underwater_look and the static lights to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections, ground targets), otherwise the README's values and ART-H2's
proposal (l28kit). Re-run after the data moves a section or a target.
Run: python3 tools/art/backdrop_l28.py [--proposal | --review | --check]   (all: about 3 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
import l28kit as kit
from artkit import ROOT, TAU, raster
from l28kit import (ARENA_SCROLL, BREAK_ROW, BREAK_T, END, SCREEN, SECTIONS, SHIP_Y, THROAT, THROAT_ROW, W, exact_t,
                    scroll, seam_row, section_at_row, target_centre)

import backdrop_l16 as b16  # noqa: E402  (tools/art: periodic noise, ramp, dither_alpha, padded)
import backdrop_l22 as b22  # noqa: E402  (tools/art: posterize, Europa's ice shelf and crystals)
import backdrop_l23 as b23  # noqa: E402  (tools/art: the drill shaft's floor, walls and ice ceiling)
import backdrop_l24 as b24  # noqa: E402  (tools/art: lumps, wrap_score)
import backdrop_l27 as b27  # noqa: E402  (tools/art: vent_floor, the gate's arches and glow)

kit.install()     # backdrop_l27 imported l27kit, whose rule replaced l23kit's: this level's rule again

SCRIPT = "backdrop_l28.py"
BATCH = "M8 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r58"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = kit.LEVEL_DIR
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = kit.BACKDROP
TILE_H = 960
FEATHER = 32
MAX_PIECE = 2040
pn, ramp, dither_alpha = b16.pn, b16.ramp, b16.dither_alpha
posterize = b22.posterize

TEAL = np.array((40, 255, 200), float)
VIOLET = np.array((180, 90, 255), float)
AMBER = np.array((255, 150, 60), float)
EMBER = np.array((255, 84, 36), float)
LIME = np.array((200, 255, 120), float)
LIME_WHITE = np.array((236, 255, 210), float)
POD = np.array((222, 214, 196), float)
RESIN = [(16, 10, 18), (36, 24, 36), (58, 40, 56), (84, 62, 78), (114, 92, 104), (150, 130, 136)]
CHITIN = [(64, 46, 66), (106, 84, 104), (156, 136, 146), (208, 196, 188)]
DARK_BASALT = [(10, 9, 11), (24, 21, 24), (40, 35, 38), (60, 52, 54), (84, 74, 72), (110, 98, 92)]
NIGHT = np.array((0.38, 0.44, 0.62))                 # the night ice's light: Europa's night side, blue shadows
JOVE = np.array((120, 92, 64), float)                # Jupiter's cream shine from the right, added


def grid(h=TILE_H, w=W):
    return np.mgrid[0:h, 0:w].astype(np.float64)


def _mix(a, b, k):
    return [tuple(int(x * (1 - k) + y * k) for x, y in zip(p, q)) for p, q in zip(a, b)]


def rgba(rgb, alpha):
    a = np.dstack([np.clip(rgb, 0, 255), np.clip(alpha, 0, 255)]).astype(np.uint8)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


def additive_img(rgb):
    h, w = rgb.shape[:2]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((h, w), 255.0)]).astype(np.uint8)))


# =========================================================================== tile sets

def _opaque(img):
    a = np.array(img.convert("RGBA"))
    a[..., 3] = 255
    return Image.fromarray(a, "RGBA")


def gate_hall():
    return b27.vent_floor(2801, 1.25, _mix(RESIN, b27.FLESH, 0.5), relief=0.2, ridge=0.1, stones=12, veins=70)


def brood_gallery():
    return b27.vent_floor(2811, 1.2, _mix(RESIN, b27.FLESH, 0.35), relief=0.16, ridge=0.06, ribs=0.16, stones=6,
                          veins=40)


def spawning_floor():
    """Resin ridges round hatching pools: pools of amber fluid with pale eggs, darker rims (wrapping)."""
    base = np.array(b27.vent_floor(2821, 1.3, _mix(RESIN, b27.FLESH, 0.55), relief=0.26, ridge=0.2, stones=8,
                                   veins=30))[..., :3].astype(float)
    yy, xx = grid()
    rng = np.random.default_rng(2822)
    pool = np.zeros((TILE_H, W))
    eggs = np.zeros((TILE_H, W))
    n = pn(W, TILE_H, 24, 2823, 3)
    for _ in range(11):
        cx, cy, rx, ry = rng.uniform(30, W - 30), rng.uniform(0, TILE_H), rng.uniform(22, 40), rng.uniform(16, 30)
        dy = (yy - cy + TILE_H / 2) % TILE_H - TILE_H / 2
        d = np.sqrt(((xx - cx) / rx) ** 2 + (dy / ry) ** 2) + 0.25 * (n - 0.5)
        pool = np.maximum(pool, np.clip((1.0 - d) / 0.12, 0, 1))
        for _ in range(int(rng.integers(6, 14))):
            a, r = rng.uniform(0, TAU), rng.uniform(0, 0.7)
            ex, ey = cx + math.cos(a) * r * rx, cy + math.sin(a) * r * ry
            edy = (yy - ey + TILE_H / 2) % TILE_H - TILE_H / 2
            eggs = np.maximum(eggs, np.clip(1 - np.hypot(xx - ex, edy) / rng.uniform(2.5, 4.5), 0, 1))
    rim = np.clip(pool * (1 - pool) * 4, 0, 1)
    deep = np.clip((pool - 0.5) * 2, 0, 1)
    fluid = np.array((110, 70, 34.0)) * (0.7 + 0.3 * n[..., None]) * (1 - 0.35 * deep[..., None])
    rgb = base * (1 - 0.6 * rim[..., None]) + np.array((150, 118, 120.0)) * (0.35 * rim)[..., None]
    rgb = rgb * (1 - pool[..., None]) + fluid * pool[..., None]
    sheen = (pool > 0.9) & (pn(W, TILE_H, 12, 2824, 2) > 0.72)
    rgb = np.where(sheen[..., None], rgb * 1.25 + 14, rgb)
    rgb = np.where((eggs > 0.3)[..., None] & (pool > 0.5)[..., None], POD * (0.7 + 0.3 * eggs[..., None]), rgb)
    return posterize(rgb, 36)


def antechamber_floor():
    """Dark basalt under a resin skin, a network of fissures with dull red cores (their light is the -glow pieces' and
    the pools'), steam-bleached rims (wrapping)."""
    base = np.array(b27.vent_floor(2831, 1.5, _mix(DARK_BASALT, b27.FLESH, 0.2), relief=0.3, ridge=0.22, crust=0.12,
                                   stones=16, veins=12))[..., :3].astype(float)
    f = pn(W, TILE_H, 96, 2832, 3)
    g = pn(W, TILE_H, 48, 2833, 3)
    crack = np.maximum(np.exp(-((f - 0.5) / 0.010) ** 2), np.exp(-((g - 0.5) / 0.008) ** 2) * (pn(W, TILE_H, 96, 2834,
                                                                                                  2) > 0.45))
    rim = np.clip(np.maximum(np.exp(-((f - 0.5) / 0.03) ** 2), 0) - crack, 0, 1)
    rgb = base * (1 + 0.25 * rim[..., None]) + np.array((30, 26, 24)) * rim[..., None] * 0.4
    rgb = rgb * (1 - crack[..., None]) + np.array((120, 34, 14.0)) * crack[..., None]
    return posterize(rgb, 32)


def shaft_climb():
    """Level 23's wet shaft floor, rows reversed (the climb), the warm glow far below recoloured to the vent's
    lime-amber and the ice darkened toward the deep."""
    a = np.array(b23.shaft_floor(True).convert("RGBA"))[::-1].astype(float)
    yy, xx = grid()
    core = np.exp(-(np.abs(xx - 240) / 60) ** 2)
    rgb = a[..., :3] * np.array((0.5, 0.58, 0.6))
    rgb = rgb * (1 - 0.1 * core[..., None]) + np.array((150, 190, 80.0)) * (0.1 * core)[..., None]
    return posterize(rgb, 32)


def night_ice():
    """Level 22's ice shelf at night: Europa's night side lit by Jupiter (cream from the right), blue shadows."""
    a = np.array(b22.ice_shelf().convert("RGBA")).astype(float)
    yy, xx = grid()
    shine = (xx / W) ** 1.6
    rgb = a[..., :3] * NIGHT + JOVE * (shine * 0.55)[..., None] * (a[..., :3].mean(-1, keepdims=True) / 200)
    return posterize(rgb, 32)


def ice_ceiling():
    return b23.ice_ceiling()


def ice_crystals():
    a = np.array(b22.ice_crystals()).astype(float)
    a[..., :3] *= 0.7
    return Image.fromarray(a.astype(np.uint8), "RGBA")


TILES = {"gate-hall": gate_hall, "brood-gallery": brood_gallery, "spawning-floor": spawning_floor,
         "antechamber-floor": antechamber_floor, "shaft-climb": shaft_climb, "night-ice": night_ice,
         "ice-ceiling": ice_ceiling, "ice-crystals": ice_crystals}
LAYER = {"ice-ceiling": "high-air", "ice-crystals": "high-air"}
SECTION_TILES = ["gate-hall", "brood-gallery", "spawning-floor", "antechamber-floor", "antechamber-floor",
                 "shaft-climb", "night-ice", "night-ice", "night-ice", "night-ice"]
DESC = {"gate-hall": "section 1: the hatchery's resin floor past the gate, chitin root veins",
        "brood-gallery": "section 2: the gallery floor, resin grooved along the galleries",
        "spawning-floor": "section 3: resin ridges round hatching pools (pale eggs in amber fluid)",
        "antechamber-floor": "sections 4-5: dark basalt under resin, red fissures (the arena is the caldera piece)",
        "shaft-climb": "section 6: Level 23's wet shaft floor in reverse, the vent's glow far below",
        "night-ice": "sections 7-10: Level 22's ice shelf at night under Jupiter",
        "ice-ceiling": "section 6: the ice ceiling's cracks of light (additive at 40 % as Level 23's)",
        "ice-crystals": "sections 8-10: Level 22's crystals, dimmed for the night (additive at 40 %)"}


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(TILE_H - 1 - np.mod(rows, TILE_H)).astype(int)]


def floor_rows(bottom, h):
    out = np.zeros((h, W, 4), np.uint8)
    for k in range(h):
        row = bottom + h - 1 - k
        out[k] = tile(SECTION_TILES[section_at_row(row)])[TILE_H - 1 - int(row) % TILE_H]
    return out


def feather_alpha(h, w, top=True, bottom=True):
    k = np.arange(h)[:, None]
    a = np.ones((h, 1))
    if top:
        a = np.minimum(a, np.clip((k + 0.5) / FEATHER, 0, 1))
    if bottom:
        a = np.minimum(a, np.clip((h - k - 0.5) / FEATHER, 0, 1))
    return dither_alpha(np.broadcast_to(a, (h, w)).copy(), 2)


# =========================================================================== the shaft's walls (Level 23's, reversed)

def wall_rgb(wet):
    return b23.ice_wall_rgb(wet)[::-1]


def ice_wall(wet=False):
    return b23.finish(b23.padded(wall_rgb(wet)), 32, amount=0.04)


def ice_wall_glow():
    a = np.array(b23.ice_wall_glow()).astype(np.uint8)
    period = a[b16.TEX_PAD:][::-1]
    return Image.fromarray(np.vstack([period[-b16.TEX_PAD:], period]), "RGBA")


def ice_wall_foot(wet=False):
    a = np.array(b23.ice_wall_foot(wet))
    return Image.fromarray(a[::-1].copy(), "RGBA")


WALLS = {"ice-wall": lambda: ice_wall(False), "ice-wall-wet": lambda: ice_wall(True), "ice-wall-glow": ice_wall_glow,
         "ice-wall-foot": lambda: ice_wall_foot(False), "ice-wall-foot-wet": lambda: ice_wall_foot(True)}

# the shaft's terrain (terrain row r is on the top edge when the scroll is r): ART-H2's proposal for H3
WALL_IN = 80                                         # Level 23's 320 px shaft
SHAFT_FROM = int(ARENA_SCROLL + 40)                  # just above the arena's top edge: never seen during the fight
SHAFT_FULL = SHAFT_FROM + 400
LIP_TERRAIN = BREAK_ROW - SCREEN                     # the bore's lip: the breakout line
SHAFT_OPEN = LIP_TERRAIN - 260


def terrain_rows():
    return {"left": [[SHAFT_FROM, 0, 0], [SHAFT_FULL, WALL_IN], [SHAFT_OPEN, WALL_IN], [LIP_TERRAIN, 0, 0]],
            "right": [[SHAFT_FROM, W, 0], [SHAFT_FULL, W - WALL_IN], [SHAFT_OPEN, W - WALL_IN], [LIP_TERRAIN, W, 0]]}


def wall_x(terrain_row):
    """The left wall's foot x at a terrain row (linear between the rows; 0 outside the shaft)."""
    pts = terrain_rows()["left"]
    rows = [p[0] for p in pts]
    xs = [p[1] for p in pts]
    if terrain_row <= rows[0] or terrain_row >= rows[-1]:
        return 0.0
    return float(np.interp(terrain_row, rows, xs))


# =========================================================================== piece helpers

def _place(t, x, w, h, clamp=True):
    row0 = target_centre(t, 0)
    x0 = int(np.clip(round(x - w / 2), 0, W - w)) if clamp else int(round(x - w / 2))
    bottom = int(round(row0 - h / 2))
    return bottom, x0, x - x0, h / 2


def _edges(a, x0, w):
    a = a.copy()
    a[0, :] = a[-1, :] = 0
    xs = np.arange(w) + x0
    a[:, (xs < 0) | (xs >= W)] = 0
    if x0 > 0:
        a[:, 0] = 0
    if x0 + w < W:
        a[:, -1] = 0
    return a


def floor_crop(bottom, x0, w, h):
    floor = np.zeros((h, w, 3))
    fr = floor_rows(bottom, h)
    lo, hi = max(0, x0), min(W, x0 + w)
    floor[:, lo - x0:hi - x0] = fr[:, lo:hi, :3]
    return floor


def relief(bottom, x0, w, h, hgt, mask, albedo, colours=36, shadow=(6, 8), blur=3, extra=None, k=1.0):
    """A shaded relief (lit from the upper left) over the floor rows: opaque where ``mask`` covers, its cast shadow
    toward the lower right on the floor round it; ``extra`` = (rgb, cover) drawn on the floor under it."""
    floor = floor_crop(bottom, x0, w, h)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 + (gx + gy) * k, 0.3, 1.6)
    rgb = albedo * shade[..., None]
    m = np.clip(mask, 0, 1)
    sm = np.roll(np.roll(m, shadow[1], 0), shadow[0], 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(blur))) / 255.0
    under = floor * (1 - 0.5 * sm[..., None])
    if extra is not None:
        under = np.where((extra[1] > 0.5)[..., None], extra[0], under)
    out = np.where((m > 0.5)[..., None], rgb, under)
    a = np.maximum((m > 0.5).astype(float), np.clip(sm * 1.6, 0, 1) * (sm > 0.08))
    if extra is not None:
        a = np.maximum(a, extra[1])
    a = _edges(a, x0, w)
    return posterize(np.clip(out, 0, 255), colours, dither_alpha(a, 2))


def glow_spots(w, h, spots, x0=0):
    """Additive: (cx, cy, r, colour, k) soft cores with halos; cropped to the field."""
    yy, xx = grid(h, w)
    rgb = np.zeros((h, w, 3))
    for cx, cy, r, col, k in spots:
        d = np.hypot(xx - cx, yy - cy)
        f = (np.exp(-(d / max(1.2, r * 0.45)) ** 2) * 0.9 + np.exp(-(d / (r * 1.6)) ** 2) * 0.35) * k
        rgb += artkit.ordered_dither(np.clip(f, 0, 1), 8)[..., None] * np.asarray(col)[None, None]
    xs = np.arange(w) + x0
    rgb[:, (xs < 0) | (xs >= W)] = 0
    return additive_img(rgb)


def seat_rings(alb, hgt, seats, cy, x0, top, r=9.0):
    """The units' seats: shallow scorched rings on a plateau."""
    yy, xx = grid(*hgt.shape)
    for sx in seats:
        d = np.hypot(xx - (sx - x0), yy - cy)
        ring = np.clip(1 - np.abs(d - r) / 2.2, 0, 1)
        alb = np.where((ring > 0.4)[..., None] & (hgt > top)[..., None], alb * 0.5, alb)
    return alb


# =========================================================================== pieces: the gate and the galleries

GATE_H = 720
GATE_CENTRE_ROW = 0


def gate_ring():
    """Level 27's ring of arches seen from inside the hatchery: the chitin ribs and their buttresses over the gate
    hall's floor (no pit: the wing is through), its centre on the screen's bottom edge at the start."""
    w, h = W, GATE_H
    bottom = GATE_CENTRE_ROW - h // 2
    hgt, pit, r, th = b27.gate_fields()
    floor = floor_crop(bottom, 0, w, h)
    crust = raster.fbm(w, h, 5, 2862, octaves=3, period=False)
    alb = ramp(CHITIN, np.clip(0.15 + hgt / 34 + 0.25 * crust, 0, 1))
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 + (gx + gy) * 0.5, 0.35, 1.45)
    ribs = hgt > 1.5
    sm = np.roll(np.roll(ribs.astype(float), 9, 0), 7, 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(4))) / 255.0
    rgb = np.where(ribs[..., None], alb * shade[..., None], floor * (1 - 0.45 * sm[..., None]))
    cover = np.maximum(ribs.astype(float), np.clip(sm * 2, 0, 1) * (sm > 0.08))
    yy, _ = grid(h, w)
    cover *= (yy > 1)
    return posterize(np.clip(rgb, 0, 255), 40, dither_alpha(np.clip(cover, 0, 1), 2)), bottom


def gate_ring_glow():
    a = np.array(b27.gate_glow()).astype(np.uint8)
    return Image.fromarray(a, "RGBA")


LAMP = 64


def flesh_lamp():
    """A lamp of grown flesh: a ribbed stalk's foot, a translucent bulb, pale veins; seen from above."""
    w = h = LAMP
    yy, xx = grid(h, w)
    cx, cy = w / 2, h / 2
    r = np.hypot(xx - cx, yy - cy)
    n = raster.fbm(w, h, 4, 2871, octaves=3, period=False)
    hgt = np.clip(1 - r / 14, 0, 1) ** 0.6 * 10 + np.clip(1 - r / 22, 0, 1) * 3 * (n > 0.5)
    th = np.arctan2(yy - cy, xx - cx)
    roots = (np.abs(np.sin(th * 3.5 + n * 2)) < 0.18) & (r > 10) & (r < 26 + 6 * n)
    alb = np.where((r < 12)[..., None], np.array((190, 160, 170.0)), ramp(CHITIN, 0.3 + 0.4 * n))
    alb = np.where(roots[..., None], np.array((90, 60, 84.0)), alb)
    mask = ((hgt > 0.5) | roots).astype(float)
    gy, gx = np.gradient(hgt)
    rgb = alb * np.clip(1 + (gx + gy) * 0.6, 0.4, 1.5)[..., None]
    a = mask * (r < 30)
    return posterize(rgb, 20, dither_alpha(a, 2))


def flesh_lamp_glow():
    return glow_spots(LAMP, LAMP, [(LAMP / 2 - 1, LAMP / 2 - 1, 9, TEAL, 0.9), (LAMP / 2, LAMP / 2, 3, LIME_WHITE, 0.6)])


RACK_W, RACK_H = 112, 480


def rack_fields(seed, nest=False):
    """A rack: two chitin rails along the gallery, cross ribs every 40 px, pods packed between (ellipses), pods
    glowing through their skins (the cores)."""
    w, h = RACK_W, RACK_H
    yy, xx = grid(h, w)
    rng = np.random.default_rng(seed)
    n = raster.fbm(w, h, 6, seed, octaves=3, period=False)
    hgt = np.zeros((h, w))
    sway = 3.5 * np.sin(yy / 57.0 + seed) + 2.0 * np.sin(yy / 23.0 + seed * 0.7)
    for k, rx in enumerate((14, w - 14)):                        # bone rails, wavy, knotted every ~60 px
        width = 5 + 1.6 * np.sin(yy / 9.0 + k * 2) ** 8 + 2.5 * (np.abs(np.sin(yy / 31.0 + k)) > 0.97)
        hgt = np.maximum(hgt, np.clip(1 - np.abs(xx - rx - sway * (1 - 2 * k)) / width, 0, 1) * 9)
    y = 26.0
    while y < h - 20:                                              # ribs at uneven spacing, slightly bowed
        bow = 4 * np.sin((xx - 14) / (w - 28) * np.pi)
        hgt = np.maximum(hgt, np.clip(1 - np.abs(yy - y - bow) / 2.6, 0, 1) * 6 * ((xx > 12) & (xx < w - 12)))
        y += rng.uniform(34, 50)
    pods = np.zeros((h, w))
    cores = []
    for k in range(h // 40):
        for j in range(3):
            if rng.random() < 0.12:
                continue
            px = 30 + j * 26 + rng.uniform(-4, 4)
            py = k * 40 + 20 + rng.uniform(-7, 7)
            rx_, ry_ = rng.uniform(7, 12), rng.uniform(10, 16)
            d = ((xx - px) / rx_) ** 2 + ((yy - py) / ry_) ** 2
            if rng.random() < 0.15:                                 # a hatched pod: a torn husk, dark inside
                pods = np.maximum(pods, np.clip(1 - np.abs(np.sqrt(d) - 0.85) / 0.15, 0, 1) * 5)
                continue
            pods = np.maximum(pods, np.sqrt(np.clip(1 - d, 0, 1)) * 8)
            cores.append((px, py, rng.uniform(0.25, 1.0)))
    ends = 26 if nest else 8                    # the rails' ends: tapered; a nest's end widened into its socket
    taper = np.clip(np.minimum(yy - 2, h - ends - yy) / 12, 0, 1)
    hgt = np.maximum(hgt, pods) * taper
    if nest:                                   # the seat: a chitin socket at the rack's lower end
        sd = np.hypot(xx - w / 2, yy - (h - 30))
        hgt = np.maximum(hgt, np.clip(1 - sd / 26, 0, 1) ** 0.5 * 10)
    hgt = hgt + (hgt > 0) * 1.6 * n
    return hgt, pods * taper, cores


def egg_rack(seed, tone, nest=False):
    """A gallery rack (ground piece): its frame, its pods; opaque on the frame and pods, transparent between."""
    w, h = RACK_W, RACK_H
    hgt, pods, cores = rack_fields(seed, nest)
    mask = (hgt > 0.6).astype(float)
    pod = (pods > 0.6) & (hgt > 0.6)
    alb = ramp(CHITIN, np.clip(0.2 + hgt / 16, 0, 1))
    alb = np.where(pod[..., None], POD * (0.62 + 0.04 * pods[..., None]) + tone * 0.06, alb)
    gy, gx = np.gradient(hgt)
    rgb = alb * np.clip(1 + (gx + gy) * 0.5, 0.35, 1.5)[..., None]
    yy, xx = grid(h, w)
    if nest:
        sd = np.hypot(xx - w / 2, yy - (h - 30))
        ring = np.clip(1 - np.abs(sd - 9) / 2.2, 0, 1) > 0.4
        rgb = np.where(ring[..., None], rgb * 0.5, rgb)
    sm = np.roll(np.roll(mask, 6, 0), 5, 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3))) / 255.0
    a = np.maximum(mask, np.clip(sm * 1.4, 0, 1) * (sm > 0.1) * 0.7)
    rgb = np.where((mask > 0.5)[..., None], rgb, np.array((8, 6, 10.0)))
    a = _edges(a, 1, w)
    return posterize(np.clip(rgb, 0, 255), 32, dither_alpha(a, 2)), cores


def egg_rack_glow(cores, tone):
    return glow_spots(RACK_W, RACK_H, [(x, y, 7.5, tone, 0.2 + 0.35 * k) for x, y, k in cores])


def gallery_wall(side):
    """The gallery wall's bulge on one flank where a Siren is grown: chitin ribbing over resin, a ledge (its seat)."""
    w, h = 128, 200
    yy, xx = grid(h, w)
    u = xx if side == "left" else (w - 1 - xx)                      # 0 at the field's edge
    n = raster.fbm(w, h, 8, 2881 if side == "left" else 2882, octaves=3, period=False)
    bulge = np.clip(1 - ((u / 70) ** 2 + ((yy - h / 2) / 92) ** 2), 0, 1)
    hgt = np.sqrt(bulge) * 18 + n * 3 * (bulge > 0)
    ribs = (np.abs(np.sin(yy / 7.0 + u * 0.05)) < 0.22) & (bulge > 0.2)
    hgt = hgt + ribs * 2
    seat_x = 60 if side == "left" else w - 60
    sd = np.hypot(xx - seat_x, yy - h / 2)
    ledge = np.clip(1 - sd / 24, 0, 1)
    hgt = np.maximum(hgt, 14 + ledge * 4 * (ledge > 0))
    hgt *= np.clip(np.minimum(yy - 2, h - 3 - yy) / 8, 0, 1) * (bulge > 0) + (ledge > 0) * np.clip(
        np.minimum(yy - 2, h - 3 - yy) / 8, 0, 1)
    mask = (hgt > 1).astype(float)
    alb = ramp(CHITIN, np.clip(0.1 + hgt / 26 + 0.2 * n, 0, 1))
    alb = np.where(ribs[..., None], alb * 0.7, alb)
    ring = (np.abs(sd - 9) < 2.2)
    alb = np.where(ring[..., None], alb * 0.5, alb)
    return alb, hgt, mask


# =========================================================================== pieces: the cavern and the antechamber

def lump_line(w, h, pts, seed, rx=(16, 28), ry=(12, 20), z=(7, 13), jitter=6):
    rng = np.random.default_rng(seed)
    blobs = []
    for x, y in pts:
        blobs.append((x + rng.uniform(-jitter, jitter), y + rng.normal(0, jitter / 2), rng.uniform(*rx),
                      rng.uniform(*ry), rng.uniform(*z)))
    return b24.lumps(w, h, blobs, seed + 1)


def resin_relief(t, x, w, h, seats, seed, z=12, colours=36):
    bottom, x0, cx, cy = _place(t, x, w, h)
    rng = np.random.default_rng(seed + 7)
    blobs = []
    for k in range(w // 9 + 2):                                        # a craggy crest: small lumps, two staggered rows
        x_ = -6 + k * 9 + rng.uniform(-4, 4)
        end = min(1.0, max(0.25, min(x_, w - x_) / 50))
        for row in (-1, 1):
            if rng.random() < 0.3:
                continue
            blobs.append((x_, cy + row * rng.uniform(4, 14) + 8 * math.sin(k * 0.5 + seed), rng.uniform(8, 16),
                          rng.uniform(7, 13), rng.uniform(z - 4, z + 2) * end))
    hgt, crust = b24.lumps(w, h, blobs, seed + 1)
    yy, xx = grid(h, w)
    spines = np.zeros((h, w))
    for _ in range(w // 14):                                           # chitin rods set in the resin, slanting
        sx, sy, ang = rng.uniform(10, w - 10), cy + rng.normal(0, 10), rng.uniform(-1.2, 1.2) + math.pi / 2
        L = rng.uniform(10, 22)
        u = (xx - sx) * math.cos(ang) + (yy - sy) * math.sin(ang)
        v = -(xx - sx) * math.sin(ang) + (yy - sy) * math.cos(ang)
        spines = np.maximum(spines, (np.abs(v) < 1.3) * (np.abs(u) < L) * (1 - np.abs(u) / L))
    hgt = np.maximum(hgt, spines * (z + 4) * (hgt > 2))
    for sx in seats:                                                   # a flat top under each seat
        d = np.hypot(xx - (sx - x0), yy - cy)
        hgt = np.maximum(hgt, np.clip(1 - d / 20, 0, 1) ** 0.3 * (z + 2))
    mask = (hgt > 0.6).astype(float) * (np.minimum(yy - 4, h - 5 - yy) > 0)
    alb = ramp([(40, 26, 36), (70, 48, 62), (104, 78, 90), (140, 116, 120)], np.clip(0.15 + hgt / 22 + 0.3 * crust, 0, 1))
    alb = np.where((spines > 0.3)[..., None] & (hgt > 2)[..., None], np.array((176, 160, 150.0)), alb)
    veins = (np.abs(raster.fbm(w, h, 10, seed + 9, octaves=3, period=False) - 0.5) < 0.02) & (hgt > 3)
    alb = np.where(veins[..., None], np.array((40, 120, 100.0)), alb)
    alb = seat_rings(alb, hgt, seats, cy, x0, z * 0.6)
    return relief(bottom, x0, w, h, hgt, mask, alb, colours=colours, shadow=(7, 10), blur=3), bottom, x0


def collapsed_rack():
    """The fallen rack at the cavern's mouth: its frame lying from the upper right to the lower left, pods burst and
    spilled, the scour in the resin where *Pelagic-3* lies nose-down under it (the trigger's sprite on top)."""
    t, x = kit.targets_of("recon-sub")[0] if kit.targets_of("recon-sub") else (58.0, 90.0)
    w, h = 208, 192
    bottom, x0, cx, cy = _place(t, x, w, h)
    yy, xx = grid(h, w)
    rng = np.random.default_rng(2891)
    n = raster.fbm(w, h, 6, 2891, octaves=3, period=False)
    ang = math.radians(-34)
    ca, sa = math.cos(ang), math.sin(ang)
    u = (xx - cx) * ca + (yy - cy) * sa                           # along the fallen rack
    v = -(xx - cx) * sa + (yy - cy) * ca
    hgt = np.zeros((h, w))
    for vr in (-26, 26):                                           # the rails, broken in places
        rail = np.clip(1 - np.abs(v - vr) / 5, 0, 1) * 8 * (np.abs(u) < 92) * (np.abs(np.sin(u / 23 + vr)) > 0.15)
        hgt = np.maximum(hgt, rail)
    for k in range(-4, 5):
        rib = np.clip(1 - np.abs(u - k * 22) / 3, 0, 1) * 6 * (np.abs(v) < 30)
        hgt = np.maximum(hgt, rib * (rng.random() > 0.25))
    pods = np.zeros((h, w))
    cores = []
    for _ in range(16):
        pu, pv = rng.uniform(-80, 80), rng.uniform(-44, 44)
        if abs(pu) < 26 and abs(pv) < 20:
            continue
        px, py = cx + pu * ca - pv * sa, cy + pu * sa + pv * ca
        d = ((xx - px) / rng.uniform(7, 10)) ** 2 + ((yy - py) / rng.uniform(8, 12)) ** 2
        burst = rng.random() < 0.5
        pods = np.maximum(pods, np.sqrt(np.clip(1 - d, 0, 1)) * (4 if burst else 7))
        if not burst:
            cores.append((px, py, rng.uniform(0.2, 0.6)))
    scour = np.clip(1 - np.hypot((xx - cx) / 34, (yy - cy) / 30), 0, 1)
    hgt = np.maximum(hgt, pods) * (scour < 0.45) + (hgt > 0) * 1.2 * n
    mask = (hgt > 0.6).astype(float)
    alb = ramp(CHITIN, np.clip(0.15 + hgt / 14, 0, 1))
    alb = np.where((pods > 0.6)[..., None] & (hgt > 0.6)[..., None], POD * 0.6, alb)
    floor = floor_crop(bottom, x0, w, h)
    sc = floor * (1 - 0.55 * np.clip(scour * 1.6, 0, 1)[..., None])        # the scour, dark resin
    spill = (n > 0.55) & (scour < 0.9) & (np.hypot(xx - cx, yy - cy) < 90)
    sc = np.where(spill[..., None], sc * 0.6 + np.array((90, 70, 40)) * 0.4, sc)
    cover = np.clip(np.maximum(scour * 2, spill * 1.0), 0, 1)
    img = relief(bottom, x0, w, h, hgt, mask, alb, colours=36, shadow=(6, 8), extra=(sc, cover))
    return img, glow_spots(w, h, [(px, py, 5, TEAL, k) for px, py, k in cores], x0), bottom, x0


def stack_piece(t, x, w, h, seats, seed):
    """The cavern's last stack: a broad resin-and-chitin mound, flat on top for the Sirens' seats."""
    bottom, x0, cx, cy = _place(t, x, w, h)
    yy, xx = grid(h, w)
    rng = np.random.default_rng(seed)
    blobs = [(cx, cy, w * 0.3, h * 0.26, 18)]
    for _ in range(46):                                            # a craggy mound: many small lumps round a core
        a, r = rng.uniform(0, TAU), rng.uniform(10, w * 0.42)
        blobs.append((cx + math.cos(a) * r, cy + math.sin(a) * r * 0.5, rng.uniform(8, 18), rng.uniform(7, 14),
                      rng.uniform(6, 16) * (1 - r / (w * 0.6))))
    hgt, crust = b24.lumps(w, h, blobs, seed + 1)
    for sx in seats:
        d = np.hypot(xx - (sx - x0), yy - cy)
        hgt = np.maximum(hgt, np.clip(1 - d / 24, 0, 1) ** 0.3 * 20)
    mask = (hgt > 0.6).astype(float) * (np.minimum(yy - 4, h - 5 - yy) > 0)
    alb = ramp([(40, 26, 36), (72, 52, 66), (110, 88, 100), (160, 140, 146)], np.clip(0.1 + hgt / 28 + 0.3 * crust, 0, 1))
    ribs = (np.abs(np.sin((xx - cx) / 9.0)) < 0.2) & (hgt > 8) & (hgt < 16)
    alb = np.where(ribs[..., None], alb * 0.72, alb)
    alb = seat_rings(alb, hgt, seats, cy, x0, 12)
    return relief(bottom, x0, w, h, hgt, mask, alb, colours=36, shadow=(9, 12), blur=4), bottom, x0


FISSURES = [(81.5, 70.0), (84.5, 330.0), (87.5, 190.0), (90.0, 420.0)]     # t (centre enters), x


def fissure(t, x, seed):
    """A steaming fissure: a jagged crack with a dull red core and pale, steam-bleached lips."""
    w, h = 176, 96
    bottom, x0, cx, cy = _place(t, x, w, h)
    yy, xx = grid(h, w)
    rng = np.random.default_rng(seed)
    pts = []
    px, py, a = cx - 70, cy + rng.uniform(-14, 14), rng.uniform(-0.3, 0.3)
    while px < cx + 70:
        pts.append((px, py))
        a += rng.normal(0, 0.35)
        a = max(-0.7, min(0.7, a))
        px, py = px + 8 * math.cos(a), py + 8 * math.sin(a)
    d = np.full((h, w), 99.0)
    for (ax, ay), (bx, by) in zip(pts, pts[1:]):
        vx, vy = bx - ax, by - ay
        L2 = vx * vx + vy * vy
        s = np.clip(((xx - ax) * vx + (yy - ay) * vy) / L2, 0, 1)
        d = np.minimum(d, np.hypot(xx - ax - s * vx, yy - ay - s * vy))
    along = np.clip(1 - np.abs(xx - cx) / 76, 0, 1)
    width = 1.2 + 3.2 * along
    core = d < width
    lip = (d < width + 5) & ~core
    bleach = (d < width + 16) & ~core & ~lip & (raster.fbm(w, h, 5, seed + 1, octaves=3, period=False) > 0.5)
    floor = floor_crop(bottom, x0, w, h)
    rgb = floor.copy()
    rgb = np.where(bleach[..., None], rgb * 0.8 + np.array((150, 140, 130)) * 0.2, rgb)
    rgb = np.where(lip[..., None], rgb * 0.5 + np.array((120, 110, 100)) * 0.5, rgb)
    rgb = np.where(core[..., None], np.array((150, 40, 16.0)) * (0.7 + 0.3 * along[..., None]), rgb)
    a = (core | lip | bleach).astype(float)
    img = posterize(np.clip(rgb, 0, 255), 24, dither_alpha(_edges(a, x0, w), 2))
    g = np.exp(-(d / (width + 1)) ** 2) * along * 0.6 + np.exp(-(d / 14) ** 2) * along * 0.12
    v = artkit.ordered_dither(np.clip(g, 0, 1), 8)
    grgb = EMBER[None, None] * v[..., None] + (AMBER - EMBER)[None, None] * np.clip(g - 0.6, 0, 1)[..., None] * 2
    xs = np.arange(w) + x0
    grgb[:, (xs < 0) | (xs >= W)] = 0
    return img, additive_img(grgb), bottom, x0


# =========================================================================== the caldera (the halted arena)

CAL_BOTTOM_PAD = 130                                   # rows of the rim's foot below the mortars' row


def caldera_rows():
    mortars = kit.targets_of("polyp-mortar", 88, 96) or [(92.0, 110.0), (92.0, 370.0)]
    rim_row = target_centre(mortars[0][0], 44)
    bottom = int(round(rim_row - CAL_BOTTOM_PAD))
    top = int(round(ARENA_SCROLL + SCREEN + 200))
    return bottom, top - bottom, rim_row, mortars


def caldera():
    """The hatchery's caldera from the outer rim to the shaft's foot. In screen terms during the halt (rows ARENA_SCROLL
    .. +539): the basin's crust filling the field, cracked in rings round the throat at (240, 330) and in radial
    fissures, mineral terraces stepping down to it, the throat a ~140 px pit of lime-white light. Below the halted
    screen: the slope up to the outer rim (the mortars' seats on it at t~92); above it: the far wall rising into the
    shaft's floor."""
    bottom, h, rim_row, mortars = caldera_rows()
    w = W
    yy, xx = grid(h, w)
    row = bottom + h - 1 - yy                                        # ground row of each pixel
    tx, trow = THROAT[0], THROAT_ROW
    dy = row - trow
    r = np.hypot(xx - tx, dy)
    th = np.arctan2(dy, xx - tx)
    rim_r = trow - rim_row                                           # the outer rim's radius (the mortars on it)
    n = raster.fbm(w, h, 8, 2901, octaves=4, period=False)
    m = raster.fbm(w, h, 32, 2902, octaves=3, period=False)
    # the basin's height: the rim ridge, the slope down, terraces, the floor, the throat's funnel
    hgt = np.where(r > rim_r, 0.0, 0.0)
    rim = np.exp(-((r - rim_r) / 28) ** 2) * 26
    slope = np.clip((r - 120) / (rim_r - 120), 0, 1)
    terr = np.floor(slope * 7 + 0.6 * (m - 0.5)) / 7
    hgt = rim + terr * 18 + 3 * n
    funnel = np.clip(1 - r / 120, 0, 1)
    hgt = hgt - funnel ** 1.5 * 22
    # albedo: the outer slope dark basalt, the terraces resin-stained with lime mineral lips, the crust paler
    base = ramp(DARK_BASALT, np.clip(0.35 + hgt / 60 + 0.3 * (n - 0.5), 0, 1))
    crust = ramp([(46, 50, 40), (70, 76, 58), (100, 108, 80), (138, 146, 108)], np.clip(0.3 + 0.5 * m + 0.2 * n, 0, 1))
    inner = np.clip((rim_r - 30 - r) / 60, 0, 1)
    alb = base * (1 - inner[..., None]) + crust * inner[..., None]
    lip = (np.abs((slope * 7 + 0.6 * (m - 0.5)) % 1.0 - 0.02) < 0.05) & (r < rim_r - 20) & (r > 125)
    alb = np.where(lip[..., None], alb * 0.6 + LIME * 0.18, alb)
    # cracks: rings round the throat and radial fissures, dark with faint lime cores
    ringc = np.abs(np.sin((r + 14 * (n - 0.5)) / 21.0)) < 0.06
    radial = (np.abs(np.sin(th * 9 + 3 * (m - 0.5))) < 0.035 + 0.02 * n) & (r > 70)
    crack = (ringc | radial) & (r < rim_r - 40)
    alb = np.where(crack[..., None], np.array((22, 30, 16.0)) + LIME * 0.12 * np.clip(1 - r / 360, 0, 1)[..., None],
                   alb)
    # the throat: a dark funnel's walls, then the core of lime-white light (its additive light is caldera-glow)
    fun = (r < 90) & (r >= 46)
    alb = np.where(fun[..., None], alb * (0.35 + 0.65 * ((r - 46) / 44))[..., None], alb)
    core = r < 46
    alb = np.where(core[..., None], LIME_WHITE * (0.55 + 0.45 * np.clip(1 - r / 46, 0, 1))[..., None], alb)
    gy, gx = np.gradient(hgt)
    shade = np.clip(1.0 + (gx + gy) * 0.35, 0.45, 1.5)
    rgb = np.where(core[..., None], alb, alb * shade[..., None])
    # the mortars' seats on the rim
    for mt, mx in mortars:
        my = h - 1 - (target_centre(mt, 44) - bottom)
        d = np.hypot(xx - mx, yy - my)
        ring = np.clip(1 - np.abs(d - 10) / 2.2, 0, 1) > 0.4
        rgb = np.where(ring[..., None], rgb * 0.5, rgb)
    # below the rim: the antechamber's floor; above the far wall: the shaft's floor (fading into the tile sets)
    floor = floor_crop(bottom, 0, w, h)
    outside = np.clip((r - rim_r - 18) / 24, 0, 1)
    far = np.clip((row - (ARENA_SCROLL + SCREEN + 10)) / 180, 0, 1)        # the far wall's top, into the shaft
    wall = np.clip((row - (ARENA_SCROLL + SCREEN - 30)) / 60, 0, 1) * (1 - far)
    rgb = rgb * (1 - 0.55 * wall[..., None]) + np.array((10, 16, 18.0)) * (0.3 * wall)[..., None]
    rgb = rgb * (1 - outside[..., None]) + floor * outside[..., None]
    rgb = rgb * (1 - far[..., None]) + floor * far[..., None]
    inside = (outside < 0.99) & (far < 0.99)
    img = posterize(np.clip(rgb, 0, 255), 48, feather_alpha(h, w) * inside)
    hot = r < 92                                                      # the throat on a palette of its own
    q = np.array(posterize(np.where(hot[..., None], rgb, LIME_WHITE * 0.5), 16))
    a = np.array(img)
    a[hot, :3] = q[hot, :3]
    img = Image.fromarray(a, 'RGBA')
    # the glow: the throat's core and halo, the cracks' faint lime near it
    g = np.exp(-(r / 26) ** 2) * 0.75 + np.exp(-(r / 60) ** 2) * 0.22 + np.exp(-(r / 150) ** 2) * 0.05
    g = g + crack * np.clip(1 - r / 240, 0, 1) * 0.3
    v = artkit.ordered_dither(np.clip(g, 0, 1), 10)
    grgb = LIME[None, None] * v[..., None] + (LIME_WHITE - LIME)[None, None] * np.clip(g - 0.7, 0, 1)[..., None] * 3
    return img, additive_img(grgb), bottom


# =========================================================================== the breakout and the night ice

MOUTH_H = 720


def shaft_mouth():
    """The bore's lip at the breakout line (its apex on BREAK_ROW, the ship's row at t=115): the shaft's dark water
    below, a ragged ring of shattered ice blocks round the bore's mouth (320 px across, as the walls), foam and spray
    on the ice round it, radial cracks in the night ice beyond."""
    w, h = W, MOUTH_H
    bottom = int(BREAK_ROW - 220)
    yy, xx = grid(h, w)
    row = bottom + h - 1 - yy
    cx, crow, R = 240.0, BREAK_ROW - 160, 160.0                   # the bore's circle: its far edge on BREAK_ROW
    r = np.hypot(xx - cx, row - crow)
    th = np.arctan2(row - crow, xx - cx)
    n = raster.fbm(w, h, 6, 2911, octaves=4, period=False)
    floor = floor_crop(bottom, 0, w, h)
    ice = tile_rows("night-ice", bottom, h)[..., :3].astype(float)
    edge = R + 10 * (n - 0.5) * 2 + 6 * np.sin(th * 7)
    hole = (r < edge) & (row < crow + R + 20)
    water = floor * 0.6 + np.array((4, 10, 16.0)) * 0.4
    foam = (np.abs(r - edge) < 10 + 8 * n) & (n > 0.42)
    blocks = np.zeros((h, w))
    rng = np.random.default_rng(2912)
    for _ in range(70):
        a, rr = rng.uniform(0, math.pi), edge.mean() + rng.uniform(-6, 30)
        bx, by = cx + math.cos(a) * rr, crow + math.sin(a) * rr
        if not (0 < bx < w):
            continue
        s = rng.uniform(4, 11)
        rot = rng.uniform(0, math.pi)
        u = (xx - bx) * math.cos(rot) + (row - by) * math.sin(rot)
        v = -(xx - bx) * math.sin(rot) + (row - by) * math.cos(rot)
        blocks = np.maximum(blocks, ((np.abs(u) < s) & (np.abs(v) < s * rng.uniform(0.5, 0.9))) * rng.uniform(0.6, 1.0))
    cracks = (np.abs(np.sin(th * 13 + 2 * n)) < 0.04) & (r > edge + 12) & (r < edge + 150 + 60 * n)
    rgb = np.where(hole[..., None], water, ice)
    below = row < crow                                                    # the shaft's own water below the circle
    rgb = np.where((below & (np.abs(xx - cx) < R))[..., None], water, rgb)
    rgb = np.where(cracks[..., None], rgb * 0.55, rgb)
    rgb = np.where(foam[..., None], np.array((200, 214, 226.0)) * (0.8 + 0.2 * n[..., None]), rgb)
    lit = np.array((214, 226, 238.0)) * NIGHT * 1.2 + JOVE * 0.3
    rgb = np.where((blocks > 0)[..., None], lit * blocks[..., None] + rgb * (1 - blocks[..., None]) * 0.3, rgb)
    a = np.ones((h, w))
    a = np.minimum(a, np.clip((yy + 0.5) / FEATHER, 0, 1))
    wall_side = (np.abs(xx - cx) >= R) & (row < crow)                     # under the walls' tops: the floor's
    a = np.where(wall_side, np.clip((row - (crow - 120)) / 60, 0, 1) * a, a)
    a = np.minimum(a, np.clip((h - yy - 0.5) / FEATHER, 0, 1))
    return posterize(np.clip(rgb, 0, 255), 40, dither_alpha(a, 2)), bottom


CRACKS_H = 600


def ceiling_cracks():
    """High-air: the ceiling's fractures converging on a point ahead of the ship, bright and close (the shatter itself
    is the game's FX: l28_props.py ``ice-shatter``, ``ice-slab``); black = transparent (additive-ready)."""
    ss = 3
    rng = np.random.default_rng(2921)
    lay = Image.new("L", (W * ss, CRACKS_H * ss), 0)
    d = ImageDraw.Draw(lay)
    cx, cy = W / 2, CRACKS_H / 2

    def crack(x, y, ang, length, value, forks):
        pts = [(x, y)]
        run = 0.0
        while run < length:
            step = rng.uniform(8, 20)
            ang += rng.normal(0, 0.22)
            x, y = x + math.cos(ang) * step, y + math.sin(ang) * step
            pts.append((x, y))
            run += step
            if forks and rng.random() < 0.12:
                forks -= 1
                crack(x, y, ang + rng.choice((-1, 1)) * rng.uniform(0.4, 0.9), length * rng.uniform(0.3, 0.5),
                      value * 0.6, 0)
        d.line([(px * ss, py * ss) for px, py in pts], fill=int(255 * value), width=ss)

    for k in range(14):
        a = k * TAU / 14 + rng.uniform(-0.15, 0.15)
        crack(cx + math.cos(a) * 6, cy + math.sin(a) * 6, a, rng.uniform(150, 290), 0.85, 3)
    for k in range(5):                                                  # rings of the star
        rr = 40 + k * 46 + rng.uniform(-8, 8)
        pts = []
        for j in range(60):
            a = j * TAU / 59
            pts.append((cx + math.cos(a) * rr * (1 + 0.08 * math.sin(a * 5 + k)),
                        cy + math.sin(a) * rr * (1 + 0.08 * math.sin(a * 5 + k))))
        for (ax, ay), (bx, by) in zip(pts, pts[1:]):
            if rng.random() < 0.55:
                d.line([(ax * ss, ay * ss), (bx * ss, by * ss)], fill=int(255 * (0.55 - 0.07 * k)), width=ss)
    line = np.array(lay.resize((W, CRACKS_H), Image.BOX)).astype(np.float64) / 255.0
    halo = np.array(Image.fromarray((line * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(3))) / 255.0
    yy, xx = grid(CRACKS_H, W)
    veil = np.exp(-(np.hypot(xx - cx, yy - cy) / 120) ** 2) * 0.25
    fade = np.clip(np.minimum(yy, CRACKS_H - 1 - yy) / 60, 0, 1)
    v = np.clip(line + halo * 0.3 + veil, 0, 1) * fade
    v = artkit.ordered_dither(v, 8)
    rgb = np.array((190, 226, 246)) * v[..., None]
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(rgb, 0, 255), alpha]).astype(np.uint8), "RGBA")


HOLES = [(117.5, 140.0), (120.5, 330.0)]                 # t (centre mid-screen), x: other drill holes


def drill_hole(t, x, seed):
    """Another Vrell drill hole in the night ice: a dark bore ringed by a bone collar with teal strips, steam."""
    w = h = 200
    bottom = int(round(scroll(t) + SCREEN / 2 - h / 2))
    x0 = int(round(x - w / 2))
    yy, xx = grid(h, w)
    cx, cy = w / 2, h / 2
    r = np.hypot(xx - cx, yy - cy)
    th = np.arctan2(yy - cy, xx - cx)
    n = raster.fbm(w, h, 6, seed, octaves=4, period=False)
    ice = floor_crop(bottom, x0, w, h)
    R = 46 + 6 * (n - 0.5)
    hole = r < R
    collar = (r >= R) & (r < R + 12)
    strip = collar & (np.abs(r - R - 6) < 1.5) & (np.abs(np.sin(th * 8)) > 0.3)
    cracks = (np.abs(np.sin(th * 11 + 3 * n)) < 0.05) & (r > R + 12) & (r < R + 70 + 30 * n)
    steam = np.clip(1 - r / 90, 0, 1) * (raster.fbm(w, h, 12, seed + 1, octaves=3, period=False) > 0.5)
    rgb = ice.copy()
    rgb = np.where(cracks[..., None], rgb * 0.6, rgb)
    rgb = np.where(collar[..., None], np.array((150, 140, 124.0)) * NIGHT * 1.4, rgb)
    rgb = np.where(strip[..., None], np.array((30, 150, 110.0)), rgb)
    rgb = np.where(hole[..., None], np.array((4, 8, 12.0)) + np.array((10, 26, 30)) * np.clip(1 - r / R, 0, 1)[..., None],
                   rgb)
    q = artkit.ordered_dither(steam * 0.6, 3)
    rgb = rgb * (1 - q[..., None]) + np.array((150, 164, 180.0)) * q[..., None]
    a = (hole | collar | cracks | (q > 0)).astype(float)
    a = np.maximum(a, np.clip(1 - (r - R - 12) / 30, 0, 1) * (r < R + 42))
    return posterize(np.clip(rgb, 0, 255), 28, dither_alpha(_edges(a, x0, w), 2)), bottom, x0


CARRIERS = [(116.8, 52.0), (119.8, 430.0)]               # high-air: t (centre mid-screen), x


def vrell_carrier():
    """High-air: a Vrell carrier risen through the ice (the brood carrier's hull sprite, scaled, under the night's
    light), steam and ice shedding off its plates."""
    src = Image.open(kit.ASSETS_SPRITES / "brood-carrier-hull_0.png").convert("RGBA")
    k = 168 / src.width
    im = src.resize((168, int(round(src.height * k))), Image.Resampling.LANCZOS)
    a = np.array(im).astype(float)
    h, w = a.shape[:2]
    pad = (380 - h) // 2
    out = np.zeros((380, w, 4))
    out[pad:pad + h] = a
    yy, xx = grid(380, w)
    rgb = out[..., :3] * NIGHT * 1.15 + JOVE * 0.25 * (xx / w)[..., None] * (out[..., 3:4] > 0)
    return posterize(np.clip(rgb, 0, 255), 40, out[..., 3])


SPLIT_H = 960
SPLIT_T = 120.0


def ice_split(side):
    """The open lead where a cruiser rose through the ice along a flank: black water, ice plates afloat, ragged edges
    toward the field (the cruiser's hull, 220 px, covers x < 110 / > 370 in low-air above it)."""
    w, h = 200, SPLIT_H
    bottom = int(round(scroll(SPLIT_T) + SCREEN / 2 - h / 2))
    x0 = 0 if side == "left" else W - w
    yy, xx = grid(h, w)
    u = xx if side == "left" else w - 1 - xx                       # 0 at the field's edge
    n = raster.fbm(w, h, 10, 2941 if side == "left" else 2942, octaves=4, period=False)
    m = raster.fbm(w, h, 40, 2943 if side == "left" else 2944, octaves=3, period=False)
    edge = 124 + 20 * (m - 0.5) * 2 + 8 * (n - 0.5)
    taper = np.clip(np.minimum(yy, h - 1 - yy) / 160, 0, 1)
    edge = edge * taper
    water = u < edge
    pl = raster.fbm(w, h, 36, 2945 if side == 'left' else 2946, octaves=2, period=False)
    plates = water & (pl > 0.66) & (u < edge - 10)
    ice = floor_crop(bottom, x0, w, h)
    rgb = np.where(water[..., None], np.array((6, 10, 18.0)) + JOVE * 0.06 * (pl[..., None] > 0.5), ice)
    rgb = np.where(plates[..., None], ice * 0.85, rgb)
    rim = (np.abs(u - edge) < 3) & (edge > 4)
    rgb = np.where(rim[..., None], np.array((210, 224, 236.0)) * NIGHT * 1.3, rgb)
    a = (water | rim).astype(float)
    return posterize(np.clip(rgb, 0, 255), 24, dither_alpha(_edges(a, x0, w), 2)), bottom, x0


# =========================================================================== seams

def seam_piece(index, seed, h=320, glow=False):
    lower, upper = SECTION_TILES[index - 1], SECTION_TILES[index]
    bottom = int(round(seam_row(index))) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = grid(h, W)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    rgb = np.where((yy < line)[..., None], up, lo)
    return rgb, line, bottom


def gallery_mouth():
    """Section 2's seam: the gallery's mouth, a chitin lintel across the field (ribbed, buttressed) over the seam,
    its lamps of grown flesh (the -glow)."""
    rgb, line, bottom = seam_piece(1, 2951)
    h = 320
    yy, xx = grid(h, W)
    n = raster.fbm(W, h, 6, 2952, octaves=3, period=False)
    band = np.clip(1 - np.abs(yy - h / 2 - 6 * np.sin(xx / 50)) / 20, 0, 1)
    arch = (np.abs(np.sin(xx / 40.0 * math.pi)) > 0.18)
    hgt = band ** 0.6 * 16 * arch + (band > 0) * 2 * n
    ribs = (np.abs(np.sin(xx / 7.0)) < 0.25) & (band > 0.3)
    hgt = hgt + ribs * 2
    for k in range(7):                                             # buttress feet below the lintel
        fx = 20 + k * 73
        d = np.hypot((xx - fx) / 10, (yy - h / 2 - 22) / 16)
        hgt = np.maximum(hgt, np.clip(1 - d, 0, 1) * 12)
    alb = ramp(CHITIN, np.clip(0.15 + hgt / 22 + 0.2 * n, 0, 1))
    gy, gx = np.gradient(hgt)
    lit = alb * np.clip(1 + (gx + gy) * 0.5, 0.35, 1.5)[..., None]
    mask = hgt > 1
    sm = np.roll(np.roll(mask.astype(float), 8, 0), 6, 1)
    sm = np.array(Image.fromarray((sm * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(4))) / 255.0
    out = np.where(mask[..., None], lit, rgb * (1 - 0.45 * sm[..., None]))
    lamps = [(fx, h / 2 - 2) for fx in (56, 166, 312, 424)]
    for lx, ly in lamps:
        d = np.hypot(xx - lx, yy - ly)
        out = np.where((d < 5)[..., None], np.array((200, 170, 176.0)), out)
    img = posterize(np.clip(out, 0, 255), 40, feather_alpha(h, W))
    g = glow_spots(W, h, [(lx, ly, 10, TEAL, 0.9) for lx, ly in lamps])
    return img, g, bottom


def plain_seam(index, seed):
    rgb, line, bottom = seam_piece(index, seed)
    return posterize(np.clip(rgb, 0, 255), 40, feather_alpha(320, W)), bottom


# =========================================================================== the layout

RACK_ROWS = [  # (t centre mid-screen, x, kind): the galleries' racks beyond the nests' three; clear of the nests
    (40.0, 56, "b"), (41.5, 424, "a"), (46.5, 150, "a"), (48.0, 330, "b"), (52.0, 64, "a"), (53.0, 418, "b")]
LAMPS = [(6.0, 40), (10.5, 440), (15.0, 60), (19.0, 420), (26.0, 36), (33.5, 444), (44.0, 240)]


def nest_racks():
    """The three racks whose lower ends carry the t~30 Spine Turrets (seat centre on the turret's centre)."""
    out = []
    for t, x in kit.targets_of("spine-turret", 25, 35) or [(30, 96), (30, 240), (30, 384)]:
        row = target_centre(t, 40)
        bottom = int(round(row - 30))                     # the seat is 30 px above the rack's bottom row
        out.append((bottom, int(round(x - RACK_W / 2)), t, x))
    return out


def _tile_job(name):
    return TILES[name]()


def _wall_job(name):
    return WALLS[name]()


META = None


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in list(OUT.glob("*.png")):
        old.unlink()
    with ProcessPoolExecutor() as ex:
        imgs = list(ex.map(_tile_job, list(TILES)))
        walls = list(ex.map(_wall_job, list(WALLS)))
    for name, img in zip(TILES, imgs):
        artkit.save_png(_opaque(img) if LAYER.get(name, "ground") == "ground" else img, OUT / f"{name}.png", SOURCE)
    for name, img in zip(WALLS, walls):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    tile.cache_clear()
    pieces, placed = {}, []

    def put(name, img, note, t, x, layer="ground"):
        if name not in pieces:
            artkit.save_png(img, OUT / f"{name}.png", SOURCE)
            pieces[name] = {"layer": layer, "size": [img.width, img.height]}
        if t is not None:
            placed.append((note, name, t, x))

    img, bottom = gate_ring()
    put("gate-ring", img, "Level 27's ring of arches seen from inside, its centre on the start's bottom edge; exact rows",
        exact_t(bottom, img.height), 240)
    put("gate-ring-glow", gate_ring_glow(), "", None, None)
    put("flesh-lamp", flesh_lamp(), "", None, None)
    put("flesh-lamp-glow", flesh_lamp_glow(), "", None, None)
    for t, x in LAMPS:
        placed.append((f"a lamp of grown flesh at t~{t:g}", "flesh-lamp", t, x))
    img, g, bottom = gallery_mouth()
    put("seam-brood-gallery", img, f"section 2's seam (ground {seam_row(1):.0f}): the gallery's mouth, its lintel and "
        "lamps; exact rows", exact_t(bottom, img.height), 240)
    put("seam-brood-gallery-glow", g, "", None, None)
    rack_a, cores_a = egg_rack(2961, TEAL)
    rack_b, cores_b = egg_rack(2962, VIOLET)
    nest, cores_n = egg_rack(2963, TEAL, nest=True)
    put("egg-rack-a", rack_a, "", None, None)
    put("egg-rack-a-glow", egg_rack_glow(cores_a, TEAL), "", None, None)
    put("egg-rack-b", rack_b, "", None, None)
    put("egg-rack-b-glow", egg_rack_glow(cores_b, VIOLET), "", None, None)
    put("egg-rack-nest", nest, "", None, None)
    put("egg-rack-nest-glow", egg_rack_glow(cores_n, TEAL), "", None, None)
    for bottom, x0, t, x in nest_racks():
        placed.append((f"the rack under the Spine Turret at t={t:g} x {x:g} (its seat 30 px above the rack's foot); "
                       "exact rows", "egg-rack-nest", exact_t(bottom, RACK_H), x0 + RACK_W // 2))
    for t, x, kind in RACK_ROWS:
        placed.append((f"a gallery rack ({kind}) at t~{t:g}", f"egg-rack-{kind}", t, x))
    sirens = kit.targets_of("siren", 30, 40) or [(36.0, 60.0), (36.0, 420.0)]
    for side, (t, x) in zip(("left", "right"), sorted(sirens, key=lambda p: p[1])):
        alb, hgt, mask = gallery_wall(side)
        w, h = alb.shape[1], alb.shape[0]
        bottom = int(round(target_centre(t, 56) - h / 2))
        x0 = 0 if side == "left" else W - w
        img = relief(bottom, x0, w, h, hgt, mask, alb, colours=32, shadow=(6, 8), k=0.5)
        put(f"gallery-wall-{side}", img, f"the gallery wall where the Siren at t={t:g} x {x:g} is grown; exact rows",
            exact_t(bottom, h), x0 + w // 2)
    img, bottom = plain_seam(2, 2971)
    put("seam-spawning-floor", img, f"section 3's seam (ground {seam_row(2):.0f}): the galleries open into the cavern; "
        "exact rows", exact_t(bottom, img.height), 240)
    img, g, bottom, x0 = collapsed_rack()
    sub = kit.targets_of("recon-sub") or [(58.0, 90.0)]
    put("collapsed-rack", img, f"the fallen rack and Pelagic-3's scour (the trigger at t={sub[0][0]:g}, x {sub[0][1]:g}); "
        "exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    put("collapsed-rack-glow", g, "", None, None)
    tur = sorted(x for t, x in kit.targets_of("spine-turret", 60, 70)) or [256, 300, 344, 388, 432]
    ridge_t = (kit.targets_of("spine-turret", 60, 70) or [(66.0, 0)])[0][0]
    img, bottom, x0 = resin_relief(ridge_t, 344, 272, 128, tur, 2981)
    put("resin-ridge", img, f"the Spine Turrets' resin ridge (t={ridge_t:g}: x {', '.join(f'{x:g}' for x in tur)}, "
        "medium's three in the middle); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    mor = kit.targets_of("polyp-mortar", 70, 78) or [(74.0, 84.0), (74.0, 150.0)]
    img, bottom, x0 = resin_relief(mor[0][0], sum(x for _, x in mor) / len(mor), 208, 112, [x for _, x in mor], 2982,
                                   z=11)
    put("far-ridge", img, f"the Polyp Mortars' far ridge (t={mor[0][0]:g}: x {', '.join(f'{x:g}' for _, x in mor)}); "
        "exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    sst = sorted(x for t, x in kit.targets_of("siren", 70, 80)) or [250, 330, 410]
    st_t = (kit.targets_of("siren", 70, 80) or [(76.0, 0)])[0][0]
    img, bottom, x0 = stack_piece(st_t, 344, 264, 160, sst, 2991)
    put("siren-stack", img, f"the cavern's last stack (t={st_t:g}: the Sirens at x {', '.join(f'{x:g}' for x in sst)}, "
        "hard's third included); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    img, bottom = plain_seam(3, 2995)
    put("seam-antechamber-floor", img, f"section 4's seam (ground {seam_row(3):.0f}): the cavern into the antechamber; "
        "exact rows", exact_t(bottom, img.height), 240)
    for i, (t, x) in enumerate(FISSURES, 1):
        img, g, bottom, x0 = fissure(t, x, 3000 + i)
        put(f"fissure-{i}", img, f"a steaming fissure at t~{t:g}, x {x:g}; exact rows", exact_t(bottom, img.height),
            x0 + img.width // 2)
        put(f"fissure-{i}-glow", g, "", None, None)
    img, g, bottom = caldera()
    put("caldera", img, f"the caldera: the outer rim (the mortars' seats, t~92) to the shaft's foot; the halted arena's "
        f"screen (ground {ARENA_SCROLL:.0f}..{ARENA_SCROLL + 539:.0f}), the throat on ground row {THROAT_ROW} x 240 "
        "(screen 240, 330); exact rows", exact_t(bottom, img.height), 240)
    put("caldera-glow", g, "", None, None)
    img, bottom = shaft_mouth()
    put("shaft-mouth", img, f"the bore's broken lip, its apex on the breakout line (ground {BREAK_ROW} = scroll({BREAK_T:g})"
        f" + {SHIP_Y}); exact rows", exact_t(bottom, img.height), 240)
    cc = ceiling_cracks()
    f = 2.2
    centre = scroll(BREAK_T) * f + SHIP_Y                                  # crossing the ship's row at the breakout
    put("ceiling-cracks", cc, f"high-air (x{f}): the ceiling's fractures, their centre crossing the ship's row at "
        f"t={BREAK_T:g}; exact rows", exact_t(int(round(centre - CRACKS_H / 2)), CRACKS_H, f), 240, layer="high-air")
    for i, (t, x) in enumerate(HOLES, 1):
        img, bottom, x0 = drill_hole(t, x, 3010 + i)
        put(f"drill-hole-{i}", img, f"another Vrell drill hole in the night ice (t~{t:g}, x {x:g}); exact rows",
            exact_t(bottom, img.height), x0 + img.width // 2)
    vc = vrell_carrier()
    for t, x in CARRIERS:
        put("vrell-carrier", vc, f"high-air: a Vrell carrier risen beside the wing (t~{t:g}, x {x:g})", t, x,
            layer="high-air")
    for side in ("left", "right"):
        img, bottom, x0 = ice_split(side)
        put(f"ice-split-{side}", img, f"the open lead where the {side} cruiser rose (centre mid-screen at "
            f"t={SPLIT_T:g}); exact rows", exact_t(bottom, img.height), x0 + img.width // 2)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets, {len(WALLS)} wall textures and {len(pieces)} pieces to "
          f"{OUT.relative_to(ROOT)}")


# =========================================================================== proposal

def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def light_rows():
    """The static light pools (underwater_look's `lights:`): scroll = the terrain row of the pool's centre (ground row
    less 540)."""
    rows = []
    for t, x in LAMPS:
        rows.append((int(round(scroll(t) + SCREEN / 2 - SCREEN)), int(x), 70, "40ffc8", f"the flesh lamp at t~{t:g}"))
    for i, (t, x) in enumerate(FISSURES, 1):
        rows.append((int(round(target_centre(t, 0) - SCREEN)), int(x), 110, "ff5a24", f"fissure-{i}'s red-amber"))
    rng = np.random.default_rng(3020)
    for t in (54, 60, 66, 72, 78):                                     # the cavern's hatching pools, warm from below
        rows.append((int(round(target_centre(t, 0) - SCREEN)), int(rng.uniform(80, 400)), 140, "ffa040",
                     f"the cavern's warmth at t~{t:g}"))
    rows.append((int(THROAT_ROW - SCREEN), int(THROAT[0]), 260, "d8ffa0",
                 "the vent's throat (screen 240, 330 on the halted arena)"))
    rows.append((int(BREAK_ROW - SCREEN - 200), 240, 300, "c8e8ff", "the light from above in the shaft's last stretch"))
    return sorted(rows)


def sections_block():
    lines = ["# ART-H2's sections for H3 (README Layout): the art is drawn for these times, speeds and eases (a halted",
             "# arena stops at once; the shaft eases up from 0 over its ramp). Level-wide `medium: underwater`; the",
             "# breakout's section switches to air (the first water->air crossing).",
             "scroll_speed: 120",
             "medium: underwater",
             "sections:"]
    for i, s in enumerate(kit.PROPOSED_SECTIONS):
        parts = [f"name: {s['name']}", f"end: {s['end']}"]
        for key in ("speed", "ramp"):
            if key in s:
                parts.append(f"{key}: {s[key]}")
        if s.get("arena"):
            parts.append("arena: true")
        if s.get("medium"):
            parts.append(f"medium: {s['medium']}")
        parts.append("atmosphere: clear")
        parts.append(f"tiles: [{', '.join(s['tiles'])}]")
        lines.append(f"  - {{{', '.join(parts)}}}")
        if kit.LOOK_YAML[i]:
            lines.append(f"    # underwater_look: {kit.LOOK_YAML[i]}")
    return "\n".join(lines) + "\n"


def terrain_block():
    tr = terrain_rows()
    lines = ["# the shaft (Level 23's drill shaft run in reverse; gap §1.3, §5 H9: the walls push, no damage). Terrain row r",
             "# is on the top edge when the scroll is r. The walls rise at the field's edges just above the halted arena",
             f"# (never seen during the fight: the arena's top edge is terrain row {ARENA_SCROLL:.0f}), close to Level 23's",
             f"# 320 px and flatten to the bore's lip on the breakout line (terrain {LIP_TERRAIN}: the ship's row at t="
             f"{BREAK_T:g}).",
             "terrain:",
             "  left:"]
    lines += [f"    - {p}" for p in tr["left"]]
    lines.append("  right:")
    lines += [f"    - {p}" for p in tr["right"]]
    lines.append("  contact: {damage: {easy: 0, medium: 0, hard: 0}, push: 30, push_seconds: 0.12, cooldown: 1.0}   # §5 H9")
    return "\n".join(lines) + "\n"


def rows_block():
    lines = ["# fixed rows the art is drawn for (H1/H3/H5):",
             f"#   the arena: the scroll halts at {ARENA_SCROLL:.0f} (t={kit.ARENA_T:g}); the screen shows ground rows "
             f"{ARENA_SCROLL:.0f}..{ARENA_SCROLL + 539:.0f};",
             f"#     the vent's throat at screen (240, 330) = ground row {THROAT_ROW}, x 240, ~140 px across (the caldera "
             "piece);",
             "#     the boss's geometry (gap §1.1, the boss spec's Arena): head (240, 95), throat (240, 135), sacs (178, 80),",
             "#     (302, 80), (190, 128), (290, 128); the coil's inner flanks x 50 / 430 / y 505; the wall clamp x 62-418,",
             "#     y <= 493; implants (110, 60), (40, 300), (440, 300): all screen space, drawn by MawLooks over the",
             "#     caldera (the backdrop has no coil);",
             f"#   the breakout line: ground row {BREAK_ROW} = scroll({BREAK_T:g}) + {SHIP_Y} (the shaft-mouth piece's lip "
             "apex; the grade lifts above it,",
             "#     as Level 23's waterline), the ceiling-cracks high-air piece centred on the ship's row at the same t;",
             f"#   the rise: the ice-split pieces centred mid-screen at t={SPLIT_T:g} under the cruisers' flanks (hull inner "
             "edges x 110 / 370);",
             f"#   the Vrell carriers (high-air) at t~{CARRIERS[0][0]:g} x {CARRIERS[0][1]:g} and t~{CARRIERS[1][0]:g} x "
             f"{CARRIERS[1][1]:g}, before the run (nothing over the play in the run)."]
    return "\n".join(lines) + "\n"


def targets_block():
    def xs(kind, t0, t1):
        return ", ".join(f"{x:g}" for _, x in sorted(kit.targets_of(kind, t0, t1), key=lambda p: p[1]))
    lines = ["# ground places this art is drawn for (H3's `ground_targets:` and the Sirens' `on_ground` xs; t = the top",
             "# edge, x the unit's centre):",
             f"#   Spine Turret x3 on the racks' ends (egg-rack-nest): t 30, x {xs('spine-turret', 25, 35)}",
             f"#   Sirens x2 grown on the gallery walls (`on_ground`, every difficulty): t 36, x {xs('siren', 30, 40)}",
             "#   Pelagic-3 (`trigger`, `sprite: recon-sub`, size [64, 64], hitbox proposed [44, 36], 20 / hard 28 HP):"
             f" t 58, x {xs('recon-sub', 50, 65)}",
             f"#   Spine Turret x3 on the resin ridge: t 66, x {xs('spine-turret', 60, 70)} (hard x5: the ridge's five "
             "seats; medium's three = the middle ones)",
             f"#   Polyp Mortar x2 on the far ridge: t 74, x {xs('polyp-mortar', 70, 78)}",
             f"#   Sirens on the cavern's last stack: t 76, x {xs('siren', 70, 80)} (hard's third = the leftmost; §5 H4)",
             f"#   Polyp Mortar x2 on the caldera's rim: t 92, x {xs('polyp-mortar', 88, 96)}",
             f"#   the buoy (`drops:`): {{t: {kit.BUOY[0]:g}, x: {kit.BUOY[1]:g}, y: {kit.BUOY[2]:g}, pickup: armour patch, "
             "sprite: service-buoy}  (tools/art/l28_props.py: Level 25's buoy in Thera Deep's orange)"]
    return "\n".join(lines) + "\n"


def lights_block():
    lines = ["# static lights this art assumes (H3's underwater_look `lights:`; the pools are the game's light map, not",
             "# baked):", "#   lights:"]
    for s, x, r, col, note in light_rows():
        lines.append(f"#     - {{scroll: {s}, x: {x}, radius: {r}, colour: {col}}}   # {note}")
    return "\n".join(lines) + "\n"


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if kit.DATA else "the README and ART-H2's layout (no data.yaml yet: re-run after H3)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 28's hatchery, the caldera, the shaft and the night ice (tools/art/backdrop_l28.py,",
             f"  # step ART-H2, from {src}). Up to the breakout the grade's per-section underwater_look lights it (the",
             "  # sections block below), plus the static lights; every piece's -glow is added over the grade",
             "  # (drawPieceGlows): the gate's teal, the flesh lamps, the racks' pods, the fissures, the vent's throat. The",
             "  # shaft's walls are the terrain's (WallProjection's ice shaft): ice-wall, -wet, -glow, -foot, -foot-wet",
             "  # (Level 23's, rows reversed). From the breakout the level is in air: the night ice is lit in its own pixels.",
             "  scroll_factors: {far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused (opaque ground)",
             "  ramp: 2",
             "  haze_colour: 0e1a1c  # the hatchery's water",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:"]
    for name in TILES:
        lines.append(f"    {name}: {{layer: {LAYER.get(name, 'ground')}, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in m["pieces"].items():
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]}}")
    lines.append("  placed:")
    for note, piece, t, x in m["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_block() + "\n" + sections_block() + "\n" + terrain_block() + "\n" + rows_block() + "\n"
                        + targets_block() + "\n" + lights_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== composite (review)

def piece_img(piece):
    return np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))


def placed_pieces(layer="ground"):
    out = []
    pieces = meta()["pieces"]
    f = 2.2 if layer == "high-air" else 1.0
    for note, piece, t, x in meta()["placed"]:
        if pieces[piece]["layer"] != layer:
            continue
        arr = piece_img(piece)
        h, w = arr.shape[:2]
        bottom = int(round(scroll(float(t)) * f + SCREEN / 2 - h / 2))
        glow = piece_img(piece + "-glow") if piece + "-glow" in pieces else None
        out.append((piece, bottom, int(round(float(x) - w / 2)), arr, glow))
    return out


def _paste_rows(img, arr, x0, y_top):
    h = arr.shape[0]
    if x0 < 0:
        arr, x0 = arr[:, -x0:], 0
    if x0 + arr.shape[1] > W:
        arr = arr[:, :W - x0]
    if -h < y_top < SCREEN:
        img.alpha_composite(Image.fromarray(np.ascontiguousarray(arr), "RGBA"), (x0, y_top))


def walls_band(img, t):
    """The shaft's walls approximated for the review: the wall texture as flat bands outside the foot line (the game
    projects them), the foot band at the line."""
    s = scroll(t)
    wall = np.array(Image.open(OUT / "ice-wall-wet.png").convert("RGBA"))[b16.TEX_PAD:]
    out = np.array(img)
    for y in range(SCREEN):
        r = s - 1 - y
        wx = wall_x(r)
        if wx <= 0.5:
            continue
        k = int(r) % wall.shape[0]
        row = wall[wall.shape[0] - 1 - k]
        cols = np.clip((np.arange(int(wx)) / max(wx, 1) * 127).astype(int), 0, 127)
        out[y, :int(wx), :3] = row[cols[::-1], :3]
        out[y, W - int(wx):, :3] = row[cols, :3]
        for xx in (int(wx) - 1, W - int(wx)):
            if 0 <= xx < W:
                out[y, xx, :3] = (24, 36, 52)
    return Image.fromarray(out, "RGBA")


def ground_view(t, pieces, high):
    base = scroll(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        row = int(math.floor(base + SCREEN - 1 - y))
        out[y] = tile(SECTION_TILES[section_at_row(row)])[TILE_H - 1 - row % TILE_H]
    img = Image.fromarray(out, "RGBA")
    glows = []
    for name, bottom, x0, arr, glow in pieces:
        h = arr.shape[0]
        y_top = int(round(base + SCREEN - 1 - (bottom + h - 1)))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        _paste_rows(img, arr, x0, y_top)
        if glow is not None:
            glows.append((Image.fromarray(glow, "RGBA"), x0 + glow.shape[1] / 2, y_top + glow.shape[0] / 2, 1.0))
    img = walls_band(img, t)
    over = []
    hb = base * 2.2
    sec = kit.section_at_t(t)
    for name in SECTIONS[sec].get("tiles", [])[1:]:
        tl = tile(name)
        a = np.zeros((SCREEN, W, 4), np.uint8)
        for y in range(SCREEN):
            row = int(math.floor(hb + SCREEN - 1 - y))
            a[y] = tl[TILE_H - 1 - row % TILE_H]
        a[..., :3] = (a[..., :3] * 0.4).astype(np.uint8)
        glows.append((artkit.additive(Image.fromarray(a, "RGBA")), W / 2, SCREEN / 2, 1.0))
    for name, bottom, x0, arr, glow in high:
        h = arr.shape[0]
        y_top = int(round(hb + SCREEN - 1 - (bottom + h - 1)))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        if name == "ceiling-cracks":
            glows.append((artkit.additive(Image.fromarray(arr, "RGBA")), x0 + arr.shape[1] / 2, y_top + h / 2, 1.0))
        else:
            over.append((Image.fromarray(arr, "RGBA"), x0 + arr.shape[1] / 2, y_top + h / 2))
    return img, glows, over


def _held(name):
    try:
        return kit.load(name)
    except FileNotFoundError:
        return []


@lru_cache(maxsize=None)
def stand_ins():
    out = {}
    for kind, name in (("spine-turret", "spine-turret"), ("polyp-mortar", "polyp-mortar"), ("siren", "siren"),
                       ("recon-sub", "recon-sub")):
        f = _held(name)
        out[kind] = f[0] if f else None
    return out


def composite(t, gp, hp, ship=(240.0, 444.0)):
    img, glows, over = ground_view(t, gp, hp)
    st = stand_ins()
    base = scroll(t)
    solids = []
    for kind, tt, x in kit.TARGETS:
        im = st.get(kind)
        if im is None:
            continue
        y = base + SCREEN - target_centre(tt, im.height)
        if -80 < y < SCREEN + 80:
            solids.append((im, x, y))
    pools = []
    for s, x, r, col, note in light_rows():
        y = base - 1 - s
        if -r < y < SCREEN + r:
            pools.append((x, y, r, 1.0, kit._rgb(col)))
    return kit.frame(img, t, solids=solids, glows=glows, ship_xy=ship, pools=pools, over=over)


# =========================================================================== checks and review

def check():
    """Wraps (BackdropSeamsTest's measure), opaque ground tiles, sizes, the edge rule of BackdropAssetsTest, the
    caldera covering the whole halted screen, the shaft-mouth's lip on the breakout line, the arena's targets."""
    problems = []
    for name in TILES:
        a = tile(name)
        if b24.wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {b24.wrap_score(a):.2f} (BackdropSeamsTest's limit 1.5)")
        if LAYER.get(name, "ground") == "ground" and (a[..., 3] < 255).any():
            problems.append(f"{name}: a ground tile must be opaque")
    for name, spec in meta()["pieces"].items():
        arr = piece_img(name)
        if arr.shape[0] > MAX_PIECE or tuple(spec["size"]) != (arr.shape[1], arr.shape[0]):
            problems.append(f"{name}: size {arr.shape[1]}x{arr.shape[0]} (spec {spec['size']})")
    last = scroll(END) * 2.2 + SCREEN
    for layer in ("ground", "high-air"):
        for name, bottom, x0, arr, _ in placed_pieces(layer):
            h, w = arr.shape[:2]
            a = arr[..., 3] > 0
            top_on = (scroll(END) * (2.2 if layer == "high-air" else 1) + SCREEN) > bottom + h
            if a[-1].any() and bottom > 0:
                problems.append(f"{name} (bottom {bottom}): opaque bottom row")
            if a[0].any() and top_on and last > 0:
                problems.append(f"{name} (bottom {bottom}): opaque top row comes on screen")
            if a[:, 0].any() and x0 > 0:
                problems.append(f"{name}: opaque left column at x {x0}")
            if a[:, -1].any() and x0 + w < W:
                problems.append(f"{name}: opaque right column at x {x0 + w}")
    cal = [p for p in placed_pieces() if p[0] == "caldera"]
    if cal:
        name, bottom, x0, arr, _ = cal[0]
        h = arr.shape[0]
        rows = slice(h - 1 - int(ARENA_SCROLL + SCREEN - 1 - bottom), h - int(ARENA_SCROLL - bottom))
        if (arr[rows, :, 3] < 255).any():
            problems.append("caldera: not opaque over the whole halted screen")
        y = h - 1 - (THROAT_ROW - bottom)
        if arr[y, 240, :3].mean() < 160:
            problems.append("caldera: the throat is not bright at (240, 330)")
    print("check: " + ("ok" if not problems else f"{len(problems)} problems"))
    for p in sorted(set(problems)):
        print("  " + p)
    return problems


TIMES = [(0.5, "1 THE GATE'S RING"), (14, "1 THE GATE HALL'S LAMPS"), (24.5, "2 THE GALLERY'S MOUTH"),
         (33, "2 THE NESTS ON THE RACKS"), (45, "2 THE GALLERIES"), (59, "3 PELAGIC-3'S RACK"),
         (68, "3 THE RESIN RIDGE"), (77, "3 FAR RIDGE AND STACK"), (90, "4 THE FISSURES"),
         (98, "4 THE CALDERA'S RIM"), (104.5, "5 THE ARENA (HALTED)"), (109, "6 THE SHAFT"),
         (113.5, "6 THE CEILING AHEAD"), (115.5, "7 THE BREAKOUT"), (119.5, "8 THE RISE"), (140, "9 THE RUN")]


def review():
    g, hgh = placed_pieces("ground"), placed_pieces("high-air")
    shots = [composite(t, g, hgh) for t, _ in TIMES]
    cols = 8
    cw, ch = 120, 135
    sheet = raster.sheet(16 + cols * (cw + 8) + 8, 60 + 2 * (ch + 24) + 40 + 300 + 300,
                         "LEVEL 28 BACKDROP - ABYSSAL MAW (FINAL)", f"PRODUCTION ART, {BATCH.upper()} - R58")
    raster.draw_text(sheet, 16, 38, "THE SCREEN AT 1/4 UNDER THE HATCHERY LIGHT'S STAND-IN (TEAL / RACKS / AMBER / RED-AMBER "
                     "/ LIME-WHITE / FROM ABOVE; AIR FROM T=115)", raster.LABEL)
    for i, ((t, label), img) in enumerate(zip(TIMES, shots)):
        x, y = 16 + (i % cols) * (cw + 8), 56 + (i // cols) * (ch + 24)
        sheet.alpha_composite(img.resize((cw, ch), Image.Resampling.BOX), (x, y))
        raster.draw_text(sheet, x, y + ch + 4, f"{t:g} {label}"[:21], raster.LABEL_DIM)
    y = 56 + 2 * (ch + 24) + 8
    raster.draw_text(sheet, 16, y, "TILES 1/4, UNLIT (THE LAST TWO HIGH-AIR) AND THE SHAFT'S WALL", raster.LABEL)
    for k, name in enumerate(TILES):
        im = Image.fromarray(tile(name), "RGBA").resize((96, 192), Image.Resampling.BOX)
        bg = Image.new("RGBA", im.size, (0, 0, 0, 255))
        bg.alpha_composite(im)
        sheet.alpha_composite(bg, (16 + k * 104, y + 16))
        raster.draw_text(sheet, 16 + k * 104, y + 214, name.upper()[:12], raster.LABEL_DIM)
    wall = Image.open(OUT / "ice-wall-wet.png").convert("RGBA").crop((0, 256, 128, 640)).resize((64, 192))
    sheet.alpha_composite(wall, (16 + 8 * 104, y + 16))
    y += 240
    raster.draw_text(sheet, 16, y, "AT 1X: THE ARENA'S SCREEN WITH THE THROAT (T=104.5), THE NESTS' RACKS (T=33), THE "
                     "BREAKOUT (T=115.5)", raster.LABEL)
    sheet.alpha_composite(shots[10].crop((40, 120, 440, 480)), (16, y + 16))
    sheet.alpha_composite(shots[3].crop((40, 120, 440, 480)), (16 + 408, y + 16))
    sheet.alpha_composite(shots[13].crop((40, 120, 440, 480)).resize((200, 180)), (16 + 816, y + 16))
    frames = [composite(t, g, hgh).resize((240, 270), Image.Resampling.BOX).convert("RGB")
              for t in np.arange(106, 124, 0.25)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


def main(argv):
    CONCEPT.mkdir(parents=True, exist_ok=True)
    if "--check" in argv:
        sys.exit(1 if check() else 0)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv and (OUT / "gate-hall.png").exists() and PROPOSAL.exists():
        write_proposal()
        return
    build()
    write_proposal()
    check()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
