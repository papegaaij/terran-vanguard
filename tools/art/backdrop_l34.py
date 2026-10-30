#!/usr/bin/env python3
"""Production art: the Level 34 backdrop, the ship graveyard (design/campaign/act-5-the-belt/level-34-salvage-run;
M9 part G batch, concept round 67, step ART-G).

Straight to production from round 60's L34 look (tools/concept/enemies_r60.py ``derelict_bg``: derelict hull slabs
over belt space, scorched and torn) in the belt's lighter A.18 a grade: the top-left sun raking across the hulls with
long hard shadows, the CDF wrecks' faded white-grey, the Helix hulks black and gold, a few red emergency lights.
Round 67 **G.1 c** (step FIX4; the round-67 a build had them a step lighter, grey-gold): the Helix hulls in round 60's
black (the L28 cruisers' charcoal black under the same sun, the ramp of tools/concept/tank_readability_r67.py), the
gold trim kept on their edges, the tank roads plain steel with no gold; the Warden Tank in livery (A.15 b,
tools/art/warden_tank.py ``--livery asc``) reads on them by its faint ground shadow (``warden-tank-asc-shadow``, drawn
under the stand-ins here as the game does). Heightfields
hill-shaded under the top-left sun with cast shadows (tools/art/backdrop_l31.py ``shade``), posterized per image;
nothing lit is mirrored (a hull facing the other way is its own render).

The ground has **no tile set** (as Levels 31 and 33): open space is the deep layer and the hulls are ground pieces on
it. The tank roads are the hulls' dorsal walkways (CDF: a grated deck between two rails) and keels (Helix: a flat
steel strip between low kerbs), 64 px wide, jogging sideways at the hull plates' seams; ``--proposal`` writes each
convoy unit's path along its road (y below the top edge at the unit's own entry, the sim's walker rule).

The layers (``scroll_factors``): ``deep`` 0.12 (space: the sunlit dust band, far wrecks, one far hulk turning slowly,
Ceres ahead as a hard white point with the Sovereign's ring of gold lights at the end), ``ground`` 1 (the hulls, the
plates, the blinking red lights), ``high-air`` 2.2 (sparse close grit). No low-air layer.

Tile sets (480 wide, wrapping):
  grave-space  deep 480x1024: belt space (backdrop_l30.py ``space_tile``), the sunlit dust band, stars
  grave-dust   high-air 480x960, transparent: close grit and dust motes (backdrop_l30.py ``dust_tile``)
Pieces (the layer named; ``frames`` loop at their fps):
  far-wreck-a, -b       deep 140x64 / 110x80: distant hull silhouettes, hazed
  far-hulk-turn         deep 120x120, 16 frames at 0.5 fps: a far Helix hulk turning slowly (each frame lit)
  ceres-point           deep 48x48: Ceres ahead, a hard white point with a short cross flare (section 6)
  sovereign-lights      deep 128x64: the Iron Sovereign's ring of gold lights beside Ceres
  plate-a, -b, -c       ground 130x100 / 160x116 / 104x140: torn CDF hull plates, bent and scorched
  plate-helix           ground 124x104: a torn Helix plate, its gold trim on one edge
  corvette-spine        ground 140x760: a broken CDF corvette's spine (the keel girder, rib stubs, a hull remnant)
  cdf-frigate-a         ground 184x1080: a CDF frigate, bow up, stern torn off, its dorsal walkway the road
  cdf-frigate-b         ground 200x1040: a frigate broken in two, the halves offset: the road jogs at the break
  vigil                 ground 220x1040: the frigate *Vigil*: the walkway, her bridge tower on the left flank (the
                        blast shutter's place, the first secret), the beacon's mount on the right flank
  helix-hulk-a, -b      ground 240x1120 / 240x1100: gutted Helix ore freighters, the cargo bay opened to its ribs,
                        the keel road along the other side (a: the road right, b: left)
  sable-wren            ground 112x224: the Helix courier *Sable Wren*, her cargo hatch's place (the second secret)
  freighter-stern       ground 480x1000: the huge Helix freighter's stern: three engine bells, the hold open to
                        space with its cradle and the four clamp seats (the Helix crate's place)
  freighter-bow         ground 480x1100: its fore deck with the road for the tank pair, the bridge block and the
                        bow; placed exactly on the stern (they overlap 12 px under the bow's frame girder)
  tangle-a, -b          ground 480x820 / 480x840: wrecks of both fleets tangled
  old-marta             ground 240x300: the prospector's tender *Old Marta* half under a Helix hull (the third
                        secret's airlock)
  red-blink             ground 12x12, 2 frames at 1 fps: an emergency light blinking red (at most two on the screen)

Review: design/campaign/act-5-the-belt/level-34-salvage-run/concept/backdrop-final-r67-a.png (eight screens with
stand-ins: the tanks on their roads in livery, the containers, the charges, the hold's clamps and crate, the secrets'
props, the ship) and .gif (t 104-114 at half size: into the freighter's hold); the round-67 QG-1 sheets are
tank_readability_r67.py's (a's 1:1 sheet, rejected, is no longer written here). ``--proposal`` writes the
``backdrop:`` block, the sections, the convoys' road paths, the ground objects' places (containers, charges, clamps,
the crate, the three secrets, the beacon's drop) and a light debris block to the level's
concept/backdrop-proposal.yaml (never into data.yaml).

Reads the level's data.yaml when it exists (sections), otherwise the README's Layout table as sections. Re-run after
the data moves a section.
Run: python3 tools/art/backdrop_l34.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU

import backdrop_l30 as l30  # noqa: E402  (tools/art: the belt's space kit)
import backdrop_l31 as l31  # noqa: E402  (tools/art: the hill shading)
import belt_units as bu  # noqa: E402  (tools/art: paste, the held-sprite loader)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l34.py"
BATCH = "M9 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r67"
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-34-salvage-run"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-34"
W, SCREEN = 480, 540
SPACE_H, DUST_H = 1024, 960
FACTORS = {"deep": 0.12, "ground": 1.0, "high-air": 2.2}

# =========================================================================== the clock (content LevelScroll, ported)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
TILES = ["grave-space", "grave-dust"]
PROPOSED_SECTIONS = [
    {"name": "Graveyard Edge", "end": 25, "atmosphere": "clear", "tiles": TILES},
    {"name": "The CDF Picket", "end": 65, "atmosphere": "clear", "tiles": TILES},
    {"name": "Helix Hulks", "end": 105, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Freighter", "end": 135, "speed": 110, "ramp": 2, "atmosphere": "clear", "tiles": TILES},
    {"name": "The Tangle", "end": 172, "speed": 130, "ramp": 2, "atmosphere": "clear", "tiles": TILES},
    {"name": "Clear Space", "end": 186, "atmosphere": "clear", "tiles": TILES},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 130)) if DATA else 130.0
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


def entry_row(t, h):
    """The ground row of the centre of a target h px tall entering at the top edge at t."""
    return scroll(t) + SCREEN + h / 2


def t_of_row(row, h):
    """When a target h px tall whose centre is on ground row ``row`` enters at the top edge."""
    return time_at(row - SCREEN - h / 2)


# =========================================================================== colours and shading

SUN = l31.SUN
CDF = [(0.0, (34, 38, 44)), (0.35, (92, 100, 108)), (0.62, (150, 156, 160)), (0.82, (190, 194, 194)),
       (1.0, (222, 224, 220))]
HELIX = [(0.0, (9, 9, 11)), (0.35, (22, 21, 23)), (0.62, (34, 33, 34)), (0.82, (48, 45, 43)),
         (1.0, (64, 59, 52))]                         # G.1 c: round 60's black (the gold trim kept)
HELIX_GREY_GOLD = [(0.0, (20, 19, 20)), (0.35, (50, 48, 46)), (0.62, (82, 77, 68)), (0.82, (112, 103, 86)),
                   (1.0, (142, 129, 100))]    # round 67 a's lighter Helix (rejected here; Level 35's tenders keep it)
MARTA = [(0.0, (30, 28, 24)), (0.4, (96, 84, 64)), (0.7, (150, 128, 90)), (1.0, (196, 170, 120))]
GOLD = np.array((214, 164, 64.0))
DECK_CDF = np.array((150, 152, 150.0))
DECK_HELIX = np.array((124, 118, 106.0))
RAIL = np.array((196, 196, 190.0))
INTERIOR = np.array((22, 22, 26.0))
RIB = np.array((70, 70, 76.0))
RED = np.array((255, 64, 44.0))
ROAD_W = 64


def poly_mask(w, h, polys, width=None):
    img = Image.new("L", (w, h), 0)
    d = ImageDraw.Draw(img)
    for p in polys:
        if width:
            d.line(p, fill=255, width=width, joint="curve")
            for x, y in (p[0], p[-1]):
                d.ellipse([x - width / 2, y - width / 2, x + width / 2, y + width / 2], fill=255)
        else:
            d.polygon(p, fill=255)
    return np.array(img) > 127


def blur(mask, r):
    return np.array(Image.fromarray((mask * 255).astype(np.uint8)).filter(ImageFilter.GaussianBlur(r))) / 255.0


def torn(mask, seed, amp=0.35, r=3, cell=6):
    """A mask's edge torn: the blurred edge cut by noise (the hull's ragged plating)."""
    n = raster.fbm(mask.shape[1], mask.shape[0], cell, seed, octaves=2, period=False)
    return blur(mask, r) + (n - 0.5) * amp > 0.5


def erode(mask, k):
    out = mask.copy()
    for _ in range(k):
        p = np.pad(out, 1)
        out = out & p[:-2, 1:-1] & p[2:, 1:-1] & p[1:-1, :-2] & p[1:-1, 2:]
    return out


def plating(w, h, seed, pw, ph):
    """Hull plating seen from above: staggered panels of slightly different tone, dark seams, a lit lip under and
    beside each seam, fine grain (1 = the plate's mean)."""
    rng = np.random.default_rng(seed)
    rows, cols = np.arange(h)[:, None], np.arange(w)[None, :]
    r = rows // ph
    off = (r % 2) * (pw // 2)
    c = (cols + off) // pw
    vals = rng.uniform(0.86, 1.1, (h // ph + 2, w // pw + 3))
    lum = vals[r, c]
    seam = ((rows % ph) == 0) | (((cols + off) % pw) == 0)
    lip = ((rows % ph) == 1) | (((cols + off) % pw) == 1)
    lum = np.where(seam, 0.62, np.where(lip, lum * 1.08, lum))
    lum = lum * (0.96 + 0.08 * raster.fbm(w, h, 5, seed + 1, octaves=2, period=False))
    return lum, seam


class Hull:
    """A ground piece built from hull parts: a height map (px), an albedo and an opacity, shaded at the end."""

    def __init__(self, w, h, seed):
        self.w, self.h, self.seed = w, h, seed
        self.hgt = np.zeros((h, w))
        self.alb = np.zeros((h, w, 3))
        self.op = np.zeros((h, w), bool)
        self.glow = []                                   # red lights (x, y)
        self.rng = np.random.default_rng(seed)
        self.n = 0

    def part(self, poly, kind="cdf", z0=12.0, tear=0.35, dome=16.0, panels=(48, 32), mask=None, gain=0.68):
        """A hull part (polygon) of ``kind``: torn edges, a domed top, plating; drawn over what is there."""
        self.n += 1
        m = poly_mask(self.w, self.h, [poly]) if mask is None else mask
        if tear:
            m = torn(m, self.seed + 7 * self.n, tear)
        m &= self._inner()
        dom = blur(m, 22)
        bev = np.clip(blur(m, 2.5) * 2 - 1, 0, 1)
        z = z0 + 6 * bev + dome * dom
        if kind == "helix":                              # Helix plating: long strakes, quieter than the CDF's panels
            panels = (panels[0] * 3 // 2, max(18, panels[1] - 2))
        lum, _ = plating(self.w, self.h, self.seed + 11 * self.n, *panels)
        if kind == "helix":
            lum = 1 + (lum - 1) * 0.7
        ramp = {"cdf": CDF, "helix": HELIX, "marta": MARTA}[kind]
        alb = raster.ramp(ramp, np.clip(gain * lum, 0, 1))
        self.hgt = np.where(m, z, self.hgt)
        self.alb = np.where(m[..., None], alb, self.alb)
        self.op |= m
        return m

    def _inner(self):
        inner = np.zeros((self.h, self.w), bool)
        inner[2:-2, 2:-2] = True
        return inner

    def edge_trim(self, m, width=4, col=GOLD):
        """Helix gold trim along a part's outline."""
        band = m & ~erode(m, width)
        self.alb = np.where(band[..., None], col, self.alb)
        self.hgt = np.where(band, self.hgt + 2, self.hgt)

    def bands(self, m, ys, width=5, col=GOLD):
        """Transverse trim bands (rows) across a part."""
        rows = np.zeros((self.h, self.w), bool)
        for y in ys:
            rows[max(0, y - width // 2):y + (width + 1) // 2] = True
        sel = m & rows
        self.alb = np.where(sel[..., None], col, self.alb)
        self.hgt = np.where(sel, self.hgt + 2, self.hgt)

    def road(self, pts, kind="cdf"):
        """A tank road along the polyline (bottom to top): a flat deck 64 px wide, grated (CDF) or ribbed steel
        (Helix), between rails/kerbs; only on the hull (where opaque)."""
        band = poly_mask(self.w, self.h, [pts], width=ROAD_W) & self.op
        inner = poly_mask(self.w, self.h, [pts], width=ROAD_W - 8) & self.op
        z = np.where(band, self.hgt, np.nan)
        level = float(np.nanmedian(z)) if band.any() else 20.0
        yy = np.arange(self.h)[:, None] + np.zeros((1, self.w))
        if kind == "cdf":
            grate = np.where((yy % 6) < 2, 0.78, 1.0) * (0.96 + 0.06 * raster.fbm(self.w, self.h, 4, self.seed + 5,
                                                                                  octaves=2, period=False))
            deck = DECK_CDF * grate[..., None]
        else:
            rib = np.where((yy % 24) < 3, 0.8, 1.0) * (0.95 + 0.08 * raster.fbm(self.w, self.h, 8, self.seed + 5,
                                                                                 octaves=2, period=False))
            deck = DECK_HELIX * rib[..., None]
        self.alb = np.where(inner[..., None], deck, self.alb)
        rail = band & ~inner
        self.alb = np.where(rail[..., None], RAIL if kind == "cdf" else DECK_HELIX * 1.25, self.alb)
        self.hgt = np.where(inner, level, np.where(rail, level + 4, self.hgt))

    def boxes(self, items):
        """Raised superstructure: (x0, y0, x1, y1, height above the hull, albedo, bevel)."""
        yy, xx = np.mgrid[0:self.h, 0:self.w].astype(float)
        for x0, y0, x1, y1, z, col, bev in items:
            inside = (xx >= x0) & (xx <= x1) & (yy >= y0) & (yy <= y1)
            edge = np.minimum.reduce([xx - x0, x1 - xx, yy - y0, y1 - yy])
            top = z * np.clip(edge / max(bev, 1e-6), 0, 1) if bev else np.full_like(xx, z)
            base = np.where(self.op, self.hgt, 8.0)
            self.hgt = np.where(inside, base + top, self.hgt)
            self.alb = np.where(inside[..., None], np.asarray(col, float), self.alb)
            self.op |= inside

    def discs(self, items):
        """Round raised parts (turret stubs, engine bells): (x, y, r, height, albedo, hollow)."""
        yy, xx = np.mgrid[0:self.h, 0:self.w].astype(float)
        for x, y, r, z, col, hollow in items:
            d = np.hypot(xx - x, yy - y)
            inside = d <= r
            prof = z * np.sqrt(np.clip(1 - (d / r) ** 2, 0, 1))
            if hollow:
                prof = np.where(d < r * hollow, z * 0.2, np.maximum(prof, z * 0.7))
            base = np.where(self.op, self.hgt, 8.0)
            self.hgt = np.where(inside, base + prof, self.hgt)
            c = np.where((d < r * (hollow or 0))[..., None], INTERIOR, np.asarray(col, float))
            self.alb = np.where(inside[..., None], c, self.alb)
            self.op |= inside

    def cavity(self, poly, depth=18.0, ribs=14, rib_axis="y", seed=0):
        """A torn-open interior: the plating gone, a dark hold below with frame ribs across it."""
        m = torn(poly_mask(self.w, self.h, [poly]), self.seed + 31 + seed, 0.3) & self.op
        yy, xx = np.mgrid[0:self.h, 0:self.w]
        rib = ((yy if rib_axis == "y" else xx) % ribs) < 3
        base = np.where(m, np.maximum(self.hgt - depth, 2), self.hgt)
        self.hgt = np.where(m & rib, base + depth * 0.55, base)
        self.alb = np.where((m & rib)[..., None], RIB, np.where(m[..., None], INTERIOR, self.alb))
        return m

    def hole(self, poly, seed=0):
        """A hole torn through the hull: space shows through."""
        m = torn(poly_mask(self.w, self.h, [poly]), self.seed + 41 + seed, 0.45)
        self.op &= ~m

    def scorch(self, n, r=(14, 34)):
        yy, xx = np.mgrid[0:self.h, 0:self.w].astype(float)
        k = np.ones((self.h, self.w))
        for _ in range(n):
            x, y, rr = self.rng.uniform(0, self.w), self.rng.uniform(0, self.h), self.rng.uniform(*r)
            k *= 1 - 0.5 * np.exp(-((xx - x) ** 2 + ((yy - y) / 0.7) ** 2) / rr ** 2)
        self.alb *= k[..., None]

    def lights(self, pts):
        self.glow += list(pts)

    def image(self, colours=48, ambient=0.42):
        self.op[:1] = False
        self.op[-1:] = False
        light = l31.shade(self.hgt, k=0.9, ambient=ambient, shadow_len=40)
        rgb = self.alb * np.clip(light * 1.12, 0, 1.4)[..., None]
        op = self.op
        pad = np.pad(op, 1)
        tl = (~pad[1:-1, :-2] | ~pad[:-2, 1:-1]) & op
        br = (~pad[1:-1, 2:] | ~pad[2:, 1:-1]) & op & ~tl
        rgb[tl] = rgb[tl] * 0.5 + np.array((226, 220, 204)) * 0.5
        rgb[br] = rgb[br] * 0.55
        for x, y in self.glow:                            # red emergency lights: a 2 px core, a dim halo
            x, y = int(x), int(y)
            rgb[y - 2:y + 3, x - 2:x + 3] = rgb[y - 2:y + 3, x - 2:x + 3] * 0.5 + RED * 0.35
            rgb[y:y + 2, x:x + 2] = (255, 120, 96)
        return l30.posterize(rgb, colours, np.where(op, 255, 0).astype(np.uint8))


# =========================================================================== hull outlines

def ship(cx, y0, y1, hw, bow=150, bow_kind="point", stern_kind="torn", seed=0, lean=0.0):
    """A vertical hull outline: the bow at y0 (pointed, blunt or torn), the stern at y1 (blunt or torn), half width
    hw; ``lean`` px of sideways drift per 100 px down."""
    rng = np.random.default_rng(seed)
    L = []
    R = []
    for y in np.linspace(y0, y1, 40):
        u = (y - y0) / max(bow, 1)
        k = 1.0 if bow_kind != "point" or u >= 1 else 0.18 + 0.82 * np.sqrt(max(u, 0))
        k = 0.82 + 0.18 * min(1, u * 2) if bow_kind == "blunt" and u < 1 else k
        x = cx + lean * (y - y0) / 100
        L.append((x - hw * k, y))
        R.append((x + hw * k, y))
    top = [(cx, y0 - 4)] if bow_kind == "point" else []
    if bow_kind == "torn":
        top = [(cx - hw + (i + 0.5) * 2 * hw / 7, y0 + rng.uniform(-14, 14)) for i in range(7)]
    bottom = []
    x1 = cx + lean * (y1 - y0) / 100
    if stern_kind == "torn":
        bottom = [(x1 + hw - (i + 0.5) * 2 * hw / 8, y1 + rng.uniform(-22, 22)) for i in range(8)]
    return top + R + bottom + L[::-1]


# =========================================================================== the pieces

CDF_SUPER = (172, 176, 176)
CDF_DARK = (96, 102, 108)
HELIX_SUPER = (40, 38, 36)                    # G.1 c: round 60's black


def plate(name):
    w, h, seed, kind = {"plate-a": (130, 100, 3401, "cdf"), "plate-b": (160, 116, 3403, "cdf"),
                        "plate-c": (104, 140, 3405, "cdf"), "plate-helix": (124, 104, 3407, "helix")}[name]
    hu = Hull(w, h, seed)
    rng = np.random.default_rng(seed)
    pts = [(w / 2 + np.cos(a) * (w / 2 - 8) * rng.uniform(0.75, 1.0),
            h / 2 + np.sin(a) * (h / 2 - 8) * rng.uniform(0.7, 1.0)) for a in np.linspace(0, TAU, 9, endpoint=False)]
    m = hu.part(pts, kind, z0=6, tear=0.5, dome=10, panels=(40, 28))
    if kind == "helix":
        hu.edge_trim(m, 4)
    hu.scorch(2, (10, 22))
    return hu.image(36)


def corvette_spine():
    w, h = 140, 760
    hu = Hull(w, h, 3411)
    spine = hu.part(ship(70, 30, 735, 22, bow=0, bow_kind="torn", stern_kind="torn", seed=3411), "cdf", z0=18, dome=8,
                    panels=(44, 30))
    hu.part([(70, 150), (128, 210), (124, 420), (70, 470)], "cdf", z0=10, tear=0.6, dome=6)       # a hull remnant
    hu.boxes([(14, y, 126, y + 7, 10, CDF_DARK, 2) for y in range(80, 700, 64) if not 150 < y < 470]
             + [(70, y, 126, y + 7, 10, CDF_DARK, 2) for y in range(80, 700, 64) if 150 < y < 470])
    hu.boxes([(58, 30, 82, 735, 6, CDF_SUPER, 2)])                                                  # the keel girder
    hu.scorch(5)
    hu.lights([(66, 300)])
    del spine
    return hu.image(40)


def frigate(name):
    """The CDF picket's frigates; returns (image, the road's polyline)."""
    if name == "cdf-frigate-a":
        w, h, seed = 184, 1080, 3421
        hu = Hull(w, h, seed)
        m = hu.part(ship(92, 24, 1052, 76, bow=200, stern_kind="torn", seed=seed), "cdf", z0=14)
        road = [(92, 1040), (92, 640), (92, 200)]
        hu.road(road)
        hu.discs([(38, 320, 20, 14, CDF_SUPER, 0.5), (146, 320, 20, 14, CDF_SUPER, 0.5),
                  (38, 760, 20, 14, CDF_SUPER, 0.5)])
        hu.cavity([(130, 520), (164, 500), (168, 600), (134, 640)], seed=1)
        hu.boxes([(150, 740, 166, 800, 10, CDF_SUPER, 3), (20, 900, 44, 960, 8, CDF_DARK, 2)])
        hu.scorch(8)
        hu.lights([(150, 330), (34, 880)])
        del m
        return hu.image(), road
    if name == "cdf-frigate-b":
        w, h, seed = 200, 1040, 3431
        hu = Hull(w, h, seed)
        hu.part(ship(84, 520, 1020, 74, bow=0, bow_kind="torn", stern_kind="blunt", seed=seed), "cdf", z0=12)
        hu.part(ship(112, 30, 560, 74, bow=180, stern_kind="torn", seed=seed + 1), "cdf", z0=16)
        road = [(84, 1006), (84, 600), (112, 520), (112, 180)]
        hu.road(road)
        hu.discs([(36, 860, 20, 14, CDF_SUPER, 0.5), (162, 300, 20, 14, CDF_SUPER, 0.5)])
        hu.boxes([(150, 700, 172, 760, 10, CDF_SUPER, 3), (52, 260, 72, 330, 10, CDF_SUPER, 3)])
        hu.cavity([(28, 640), (52, 620), (56, 700), (30, 720)], seed=2)
        hu.scorch(9)
        hu.lights([(170, 690), (60, 250)])
        return hu.image(), road
    raise ValueError(name)


VIGIL_SHUTTER = (45, 760)          # local: the bridge's blast shutter (the secret's trigger)
VIGIL_BEACON = (190, 420)          # local: the CDF beacon's mount (the armour patch's drop)


def vigil():
    w, h, seed = 220, 1040, 3441
    hu = Hull(w, h, seed)
    hu.part(ship(110, 24, 1016, 98, bow=210, stern_kind="torn", seed=seed), "cdf", z0=14, panels=(52, 34))
    road = [(126, 1004), (126, 220)]
    hu.road(road)
    sx, sy = VIGIL_SHUTTER                                          # the bridge tower on the left flank
    hu.boxes([(18, sy - 120, 74, sy + 16, 26, CDF_SUPER, 5), (24, sy - 100, 68, sy - 40, 34, CDF_DARK, 4),
              (30, sy - 160, 40, sy - 120, 40, CDF_DARK, 2)])
    hu.boxes([(sx - 18, sy - 4, sx + 18, sy + 18, 20, (70, 76, 84), 2)])   # the shutter's frame
    bx, by = VIGIL_BEACON                                          # the beacon's mount on the right flank
    hu.discs([(bx, by, 16, 8, CDF_DARK, 0)])
    hu.discs([(186, 820, 18, 14, CDF_SUPER, 0.5), (186, 640, 18, 14, CDF_SUPER, 0.5)])
    hu.cavity([(160, 300), (200, 280), (204, 360), (166, 380)], seed=3)
    hu.scorch(7)
    hu.lights([(30, sy - 150), (202, 700)])
    return hu.image(), road


def helix_hulk(name):
    """The gutted Helix ore freighters: a: the bay left, the keel road right; b: the road left, the bay right."""
    a = name == "helix-hulk-a"
    w, h, seed = (240, 1120, 3451) if a else (240, 1100, 3461)
    hu = Hull(w, h, seed)
    m = hu.part(ship(120, 26, h - 28, 104, bow=170, bow_kind="blunt", stern_kind="torn" if a else "blunt",
                     seed=seed), "helix", z0=14, panels=(64, 24), dome=14)
    hu.edge_trim(m, 4)
    hu.bands(m, list(range(160, h - 60, 150)), 4)
    rx = 176 if a else 64
    road = [(rx, h - 44), (rx, int(h * 0.55)), (rx - 14 if a else rx + 14, int(h * 0.55) - 40), (rx - 14 if a else rx + 14, 200)]
    bay = [(30, 200), (128, 200), (128, h - 160), (30, h - 160)] if a else [(112, 220), (210, 220), (210, h - 120),
                                                                              (112, h - 120)]
    hu.cavity(bay, depth=20, ribs=22, seed=4)
    hu.road(road, "helix")
    hu.boxes([(28 if not a else 150, 70, 92 if not a else 214, 120, 12, HELIX_SUPER, 3)])
    hu.scorch(8)
    hu.lights([(24 if a else 216, 300), (24 if a else 216, h - 300)] + ([(130, 150)] if a else [(108, 160)]))
    return hu.image(52), road


SABLE_HATCH = (56, 128)


def sable_wren():
    w, h = 112, 224
    hu = Hull(w, h, 3471)
    m = hu.part(ship(56, 14, 210, 40, bow=70, bow_kind="point", stern_kind="blunt", seed=3471), "helix", z0=12,
                panels=(30, 18), dome=10, gain=0.6, tear=0.15)
    hu.edge_trim(m, 3)
    hu.boxes([(12, 150, 26, 196, 6, GOLD * 0.8, 2), (86, 150, 100, 196, 6, GOLD * 0.8, 2)])          # engine pods
    hx, hy = SABLE_HATCH
    hu.boxes([(hx - 16, hy - 14, hx + 16, hy + 14, 4, HELIX_SUPER, 2)])
    hu.lights([(56, 40)])
    hu.scorch(2, (8, 16))
    return hu.image(40)


HOLD = (240, 780)                  # local (freighter-stern): the crate's place in the hold (its centre)
CLAMPS = [(-46, -30), (46, -30), (-46, 30), (46, 30)]       # the clamps round the crate: dx, dy
STERN_H, BOW_H, OVERLAP = 1300, 1500, 12


def freighter_stern():
    w, h, seed = W, STERN_H, 3481
    hu = Hull(w, h, seed)
    body = [(24, 0), (456, 0), (456, h - 100), (420, h - 40), (60, h - 40), (24, h - 100)]
    m = hu.part(body, "helix", z0=16, dome=20, panels=(72, 26), tear=0.0)
    hu.edge_trim(m, 5)
    hu.bands(m, [140, 420, 600, h - 200], 5)
    cx, cy = HOLD
    hold = [(cx - 112, cy - 92), (cx + 112, cy - 92), (cx + 112, cy + 92), (cx - 112, cy + 92)]
    mh = hu.cavity(hold, depth=26, ribs=26, rib_axis="x", seed=5)
    hu.edge_trim(mh, 4)
    hu.boxes([(cx - 46, cy - 34, cx + 46, cy + 34, 4, (60, 58, 56), 3)])                          # the cradle
    for dx, dy in CLAMPS:                                                                       # the clamp seats
        hu.boxes([(cx + dx - 12, cy + dy - 10, cx + dx + 12, cy + dy + 10, 8, (84, 80, 74), 2)])
    hu.boxes([(cx - 150, cy - 110, cx - 116, cy + 110, 16, HELIX_SUPER, 4),                      # the doors peeled back
              (cx + 116, cy - 110, cx + 150, cy + 110, 16, HELIX_SUPER, 4)])
    hu.discs([(120, h - 50, 46, 22, (88, 84, 80), 0.55), (240, h - 42, 52, 24, (88, 84, 80), 0.55),
              (360, h - 50, 46, 22, (88, 84, 80), 0.55)])                                       # the engine bells
    hu.cavity([(380, 300), (430, 290), (440, 390), (390, 400)], seed=6)
    hu.cavity([(50, 980), (120, 960), (110, 1080), (60, 1090)], seed=10)
    hu.boxes([(40, 420, 90, 500, 10, HELIX_SUPER, 3), (330, 1000, 420, 1060, 12, HELIX_SUPER, 3)])
    hu.scorch(12)
    hu.lights([(40, 160), (440, 160), (40, h - 200), (440, h - 200), (cx - 100, cy - 80)])
    hu.op[:2] = False                                     # the top rows clear (under the bow's frame girder)
    return hu.image(56)


def freighter_bow():
    w, h, seed = W, BOW_H, 3491
    hu = Hull(w, h, seed)
    body = [(24, h), (456, h), (456, 420), (420, 200), (330, 60), (150, 60), (60, 200), (24, 420)]
    m = hu.part(body, "helix", z0=16, dome=20, panels=(72, 26), tear=0.0)
    hu.edge_trim(m, 5)
    hu.bands(m, [300, 700, 1100], 5)
    hu.boxes([(24, h - OVERLAP - 2, 456, h - 3, 10, GOLD * 0.85, 2)])                     # the frame girder at the seam
    road = ROADS["freighter-bow"]
    hu.road(road, "helix")
    hu.boxes([(300, 300, 420, 460, 28, HELIX_SUPER, 6), (318, 320, 402, 370, 36, (70, 66, 62), 4),     # the bridge block
              (230, 90, 250, 200, 14, GOLD * 0.8, 2)])
    hu.cavity([(330, 900), (420, 890), (430, 1100), (340, 1120)], ribs=18, seed=7)
    hu.cavity([(50, 1200), (110, 1190), (120, 1300), (60, 1320)], ribs=18, seed=11)
    hu.discs([(80, 560, 22, 14, HELIX_SUPER, 0.5), (400, 620, 22, 14, HELIX_SUPER, 0.5)])
    hu.scorch(10)
    hu.lights([(240, 72), (440, 480), (40, 480), (360, 310)])
    hu.op[h - 2:] = False
    return hu.image(56), road


def tangle(name):
    if name == "tangle-a":
        w, h, seed = W, 820, 3501
        hu = Hull(w, h, seed)
        mh = hu.part(ship(330, 30, 800, 92, bow=120, bow_kind="blunt", stern_kind="torn", seed=seed, lean=-4),
                     "helix", z0=12, panels=(64, 24))
        hu.edge_trim(mh, 4)
        hu.part([(10, 520), (60, 470), (300, 120), (360, 160), (120, 600), (40, 640)], "cdf", z0=30, dome=12)
        hu.cavity([(200, 560), (250, 520), (280, 660), (230, 700)], seed=8)
        road = [(318, 790), (318, 560), (300, 520), (300, 360), (316, 330), (316, 60)]
        hu.road(road, "helix")
        hu.discs([(110, 470, 18, 12, CDF_SUPER, 0.5)])
        hu.scorch(10)
        hu.lights([(420, 300), (90, 560)])
        return hu.image(56), road
    if name == "tangle-b":
        w, h, seed = W, 840, 3511
        hu = Hull(w, h, seed)
        hu.part(ship(140, 40, 820, 86, bow=160, stern_kind="torn", seed=seed, lean=3), "cdf", z0=12)
        road = [(140, 806), (148, 520), (176, 470), (176, 120)]
        hu.road(road)
        hm = hu.part([(310, 0 + 30), (470, 20), (476, 330), (330, 360)], "helix", z0=26, panels=(64, 24))
        hu.edge_trim(hm, 4)
        hu.part([(330, 600), (476, 560), (476, 700), (350, 740)], "helix", z0=10, tear=0.5)
        hu.scorch(9)
        hu.lights([(440, 200), (60, 600), (300, 130)])
        return hu.image(56), road
    raise ValueError(name)


MARTA_LOCK = (110, 214)            # local (old-marta): her airlock (the third secret's trigger)


def old_marta():
    """The prospector's tender Old Marta half under a Helix hull: her rusty ochre bow and airlock out in the sun, her
    stern under the hull's torn plating."""
    w, h = 240, 300
    hu = Hull(w, h, 3515)
    hu.part([(56, 120), (164, 110), (176, 250), (136, 290), (84, 290), (46, 250)], "marta", z0=8, dome=10,
            tear=0.12, panels=(26, 20))
    mx, my = MARTA_LOCK
    hu.discs([(mx, my, 16, 6, (120, 104, 76), 0)])
    hu.boxes([(62, 150, 80, 190, 6, (150, 128, 90), 2), (140, 150, 158, 190, 6, (150, 128, 90), 2)])
    hm = hu.part([(4, 10), (236, 4), (236, 150), (150, 176), (60, 164), (4, 140)], "helix", z0=24, panels=(64, 24),
                 tear=0.4)
    hu.edge_trim(hm, 4)
    hu.lights([(170, 240)])
    hu.scorch(3, (10, 20))
    return hu.image(48)


def red_blink(i):
    img = Image.new("RGBA", (12, 12), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    if i == 0:
        d.ellipse([2, 2, 9, 9], fill=(150, 30, 24, 150))
        d.rectangle([5, 5, 6, 6], fill=(255, 150, 120, 255))
        d.point([(4, 5), (7, 6), (5, 4), (6, 7)], fill=(255, 70, 50, 255))
    else:
        d.rectangle([5, 5, 6, 6], fill=(90, 20, 18, 255))
    return img


# --------------------------------------------------------------------------- deep pieces

def far_wreck(name):
    w, h, seed = {"far-wreck-a": (140, 64, 3521), "far-wreck-b": (110, 80, 3523)}[name]
    hu = Hull(w, h, seed)
    if name == "far-wreck-a":
        hu.part([(8, 34), (40, 14), (130, 22), (132, 46), (30, 54)], "cdf", z0=4, dome=6, panels=(16, 10), tear=0.4)
    else:
        m = hu.part([(14, 70), (30, 12), (96, 8), (100, 60)], "helix", z0=4, dome=6, panels=(18, 8), tear=0.4)
        hu.edge_trim(m, 2)
    img = hu.image(24)
    a = np.array(img)
    a[:1], a[-1:], a[:, :1], a[:, -1:] = 0, 0, 0, 0
    return l30.hazed(Image.fromarray(a, "RGBA"), 0.7, haze=0.42)


def far_hulk_turn(i, n=16):
    """A far Helix hulk turning slowly: the model's heightfield turned per frame and lit under the fixed sun."""
    s = 360
    hu = Hull(s, s, 3531)
    m = hu.part(ship(180, 60, 300, 70, bow=80, bow_kind="blunt", stern_kind="torn", seed=3531), "helix", z0=10,
                dome=30, panels=(40, 16))
    hu.edge_trim(m, 6)
    hu.cavity([(130, 140), (170, 140), (170, 270), (130, 270)], seed=9)
    ang = 360.0 * i / n
    hg = Image.fromarray(hu.hgt.astype(np.float32), "F").rotate(ang, Image.BILINEAR)
    al = Image.fromarray(np.clip(hu.alb, 0, 255).astype(np.uint8), "RGB").rotate(ang, Image.BILINEAR)
    op = Image.fromarray((hu.op * 255).astype(np.uint8)).rotate(ang, Image.BILINEAR)
    hu.hgt = np.array(hg).astype(float)
    hu.alb = np.array(al).astype(float)
    hu.op = np.array(op) > 127
    hu.glow = []
    img = hu.image(24).resize((120, 120), Image.BOX)
    a = np.array(img)
    a[..., 3] = np.where(a[..., 3] > 127, 255, 0)
    a[:1], a[-1:], a[:, :1], a[:, -1:] = 0, 0, 0, 0
    return l30.hazed(Image.fromarray(a, "RGBA"), 0.72, haze=0.4)


def ceres_point(size=48):
    """Ceres ahead: a hard white point with a short four-ray flare (stepped alpha)."""
    yy, xx = np.mgrid[0:size, 0:size].astype(float) + 0.5
    x, y = xx - size / 2, yy - size / 2
    r, a = np.hypot(x, y), np.arctan2(y, x)
    f = np.exp(-(r / 2.8) ** 2) * 1.8 + np.exp(-(r / 8) ** 2) * 0.35
    f += np.clip(np.cos(a * 2) ** 60, 0, 1) * np.exp(-(r / 18) ** 2) * 0.7
    f *= np.clip((size / 2 - 2 - r) / 4, 0, 1)
    rgb = np.array((255, 250, 240.0))[None, None] * np.clip(f, 0, 1)[..., None]
    img = Image.fromarray(np.dstack([rgb, np.clip(f * 1.5, 0, 1) * 255]).astype(np.uint8), "RGBA")
    return artkit.stepped_alpha(img, 6)


def sovereign_lights(w=128, h=64):
    """The Iron Sovereign far ahead: a tilted ring of gold lights (an ellipse of points), a dim dark hull inside."""
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    cx, cy, rx, ry = w / 2, h / 2, 52, 19
    d.ellipse([cx - rx + 8, cy - ry + 4, cx + rx - 8, cy + ry - 4], fill=(30, 28, 32, 170))
    for k in range(22):
        a = TAU * k / 22
        x, y = cx + rx * np.cos(a), cy + ry * np.sin(a)
        lit = 0.55 + 0.45 * (0.5 - 0.5 * np.sin(a))
        d.rectangle([x, y, x + 1, y + 1], fill=(255, int(170 + 60 * lit), int(70 + 40 * lit), 255))
        if k % 2 == 0:
            d.point([(x - 1, y), (x + 2, y + 1)], fill=(220, 170, 70, 170))
    return img


# =========================================================================== build

GROUND = ["plate-a", "plate-b", "plate-c", "plate-helix", "corvette-spine", "cdf-frigate-a", "cdf-frigate-b",
          "vigil", "helix-hulk-a", "helix-hulk-b", "sable-wren", "freighter-stern", "freighter-bow", "tangle-a",
          "tangle-b", "old-marta"]


def _render_job(name):
    if name.startswith("plate"):
        return plate(name), None
    if name == "corvette-spine":
        return corvette_spine(), None
    if name.startswith("cdf-frigate"):
        return frigate(name)
    if name == "vigil":
        return vigil()
    if name.startswith("helix-hulk"):
        return helix_hulk(name)
    if name == "sable-wren":
        return sable_wren(), None
    if name == "freighter-stern":
        return freighter_stern(), None
    if name == "freighter-bow":
        return freighter_bow()
    if name.startswith("tangle"):
        return tangle(name)
    if name == "old-marta":
        return old_marta(), None
    if name.startswith("far-hulk-turn"):
        return far_hulk_turn(int(name.rsplit("_", 1)[1])), None
    raise ValueError(name)


_ROADS = {}


def roads():
    """Every piece's road polyline (local, bottom to top), computed without rendering."""
    if not _ROADS:
        _ROADS.update(ROADS)
    return _ROADS


# the roads, as the pieces draw them (kept here so the proposal needs no render)
ROADS = {
    "cdf-frigate-a": [(92, 1040), (92, 640), (92, 200)],
    "cdf-frigate-b": [(84, 1006), (84, 600), (112, 520), (112, 180)],
    "vigil": [(126, 1004), (126, 220)],
    "helix-hulk-a": [(176, 1076), (176, 616), (162, 576), (162, 200)],
    "helix-hulk-b": [(64, 1056), (64, 605), (78, 565), (78, 200)],
    "freighter-bow": [(150, BOW_H - 30), (150, 900), (190, 850), (190, 160)],
    "tangle-a": [(318, 790), (318, 560), (300, 520), (300, 360), (316, 330), (316, 60)],
    "tangle-b": [(140, 806), (148, 520), (176, 470), (176, 120)],
}


def road_x(name):
    """The x (local) of a piece's road at its lowest point (warden_tank.py's livery review centres it)."""
    return ROADS[name][0][0]


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    save("grave-space", l30.space_tile(seed=3541))
    save("grave-dust", l30.dust_tile(seed=3542))
    save("ceres-point", ceres_point())
    save("sovereign-lights", sovereign_lights())
    for name in ("far-wreck-a", "far-wreck-b"):
        save(name, far_wreck(name))
    jobs = GROUND + [f"far-hulk-turn_{i}" for i in range(16)]
    with ProcessPoolExecutor() as pool:
        out = list(pool.map(_render_job, jobs, chunksize=1))
    turn = []
    for name, (img, road) in zip(jobs, out):
        if name.startswith("far-hulk-turn"):
            turn.append(img)
            continue
        if road is not None:
            assert [tuple(p) for p in road] == [tuple(p) for p in ROADS[name]], name
        save(name, img)
    for i, f in enumerate(artkit.quantize_set(turn, 24)):
        save(f"far-hulk-turn_{i}", f)
    for i in range(2):
        save(f"red-blink_{i}", red_blink(i))
    return pieces_spec(), placements()


SIZES = {"plate-a": (130, 100), "plate-b": (160, 116), "plate-c": (104, 140), "plate-helix": (124, 104),
         "corvette-spine": (140, 760), "cdf-frigate-a": (184, 1080), "cdf-frigate-b": (200, 1040),
         "vigil": (220, 1040), "helix-hulk-a": (240, 1120), "helix-hulk-b": (240, 1100), "sable-wren": (112, 224),
         "freighter-stern": (W, STERN_H), "freighter-bow": (W, BOW_H), "tangle-a": (W, 820), "tangle-b": (W, 840),
         "old-marta": (240, 300)}


def pieces_spec():
    out = {"far-wreck-a": {"layer": "deep", "size": [140, 64]}, "far-wreck-b": {"layer": "deep", "size": [110, 80]},
           "far-hulk-turn": {"layer": "deep", "size": [120, 120], "frames": 16, "fps": 0.5},
           "ceres-point": {"layer": "deep", "size": [48, 48]},
           "sovereign-lights": {"layer": "deep", "size": [128, 64]}}
    for name in GROUND:
        out[name] = {"layer": "ground", "size": list(SIZES[name])}
    out["red-blink"] = {"layer": "ground", "size": [12, 12], "frames": 2, "fps": 1}
    return out


# =========================================================================== the places

TANK = 76
CONTAINER = (40, 28)
CHARGE = 24
CLAMP = 32
CRATE = (60, 40)
SECRET = 40
BEACON = 40


def centre_row(t_place):
    return scroll(t_place) + SCREEN / 2


def local_to_ground(name, t_place, x_place, px, py):
    """(screen x, ground row) of a piece's local point."""
    w, h = SIZES[name]
    return x_place - w / 2 + px, centre_row(t_place) + h / 2 - py


def place_for(name, py, row):
    """The placement time putting local row ``py`` of piece ``name`` on ground row ``row``."""
    h = SIZES[name][1]
    return t_for_layer_row(row - h / 2 + py, 1.0)


def ground_to_local(name, t_place, x_place, x, row):
    w, h = SIZES[name]
    return x - x_place + w / 2, h / 2 - (row - centre_row(t_place))


# the convoys (README *Ground targets*): (t, count, the piece carrying the road, x of the piece, the road's local y
# under the first tank's entry)
CONVOYS = [(32.0, 2, "cdf-frigate-a", 150, 960), (50.0, 3, "cdf-frigate-b", 330, 960),
           (70.0, 2, "helix-hulk-a", 300, 1030), (88.0, 3, "helix-hulk-b", 170, 1010),
           (123.0, 2, "freighter-bow", 240, 1000), (140.0, 3, "tangle-a", 240, 760),
           (158.0, 3, "tangle-b", 240, 800)]
INTERVAL = 1.5
TANK_SPEED = 40.0
PATH_LEN = 720.0


def convoy_places():
    """{piece: t_place} of the road pieces, from the convoys."""
    out = {}
    for t, _, name, x, py in CONVOYS:
        if name == "freighter-bow":
            continue
        out[name] = place_for(name, py, entry_row(t, TANK))
    return out


def freighter_places():
    """The stern placed by the crate (its centre entering at CRATE_T), the bow exactly on it."""
    row = entry_row(CRATE_T, CRATE[1])
    ts = place_for("freighter-stern", HOLD[1], row)
    top_row = centre_row(ts) + STERN_H / 2                 # the stern's top edge (ground row)
    bow_centre = top_row - OVERLAP + BOW_H / 2
    return ts, t_for_layer_row(bow_centre, 1.0)


CRATE_T = 108.3


def road_ground(name, t_place, x_place):
    return [local_to_ground(name, t_place, x_place, px, py) for px, py in ROADS[name]]


def x_on_road(pts, row):
    for (x0, r0), (x1, r1) in zip(pts, pts[1:]):
        if r0 <= row <= r1:
            u = (row - r0) / (r1 - r0)
            return x0 + (x1 - x0) * u
    return None


def unit_path(pts, t_unit):
    """A tank's path along a road (ground x, row points bottom to top), entering at the top edge at t_unit: the sim's
    walker path (x, y below the top edge at its entry), PATH_LEN px of road from the entry point."""
    r0 = scroll(t_unit) + SCREEN + 40                     # its centre at y = -40 at entry
    x0 = x_on_road(pts, r0)
    if x0 is None:
        raise ValueError(f"the road does not reach row {r0:.0f}")
    out = [(x0, r0)]
    for x, r in pts:
        if r0 < r < r0 + PATH_LEN:
            out.append((x, r))
    end = min(r0 + PATH_LEN, pts[-1][1])
    if end > out[-1][1] + 1:
        out.append((x_on_road(pts, end), end))
    s = scroll(t_unit)
    return [(round(x, 1), round(SCREEN - r + s, 1)) for x, r in out]


def placements():
    """Every placed piece: dict(name, t, x, note)."""
    out = []
    for name, t, x, note in SCENERY:
        out.append(dict(name=name, t=t_centre_at(t, SCREEN / 2, 1.0), x=x, note=note, role="scenery"))
    cp = convoy_places()
    for t, n, name, x, py in CONVOYS:
        if name == "freighter-bow":
            continue
        out.append(dict(name=name, t=cp[name], x=x, note=f"{name}: its road carries the convoy of t {t:g} (x{n})"))
    ts, tb = freighter_places()
    out.append(dict(name="freighter-stern", t=ts, x=240, note=f"the freighter's stern: the hold's crate at x {HOLD[0]} "
                                                              f"entering at t {CRATE_T:g} (the clamps round it)"))
    out.append(dict(name="freighter-bow", t=tb, x=240, note="the freighter's bow, exactly on the stern (12 px overlap "
                                                            "under its frame girder): the far deck's road (t 125)"))
    out.append(dict(name="vigil", t=VIGIL_T, x=VIGIL_X, note=f"the Vigil: her bridge's shutter at x {VIGIL_X - 110 + 45}"
                                                              f" (t 56), the beacon's mount at x "
                                                              f"{VIGIL_X - 110 + VIGIL_BEACON[0]} (t 60)"))
    out.append(dict(name="sable-wren", t=SABLE_T, x=SABLE_X, note="the Sable Wren wedged between two hulks (t 96)"))
    out.append(dict(name="old-marta", t=place_for("old-marta", MARTA_LOCK[1], entry_row(165.0, SECRET)),
                    x=240 - MARTA_LOCK[0] + 120, note="Old Marta under a Helix hull: her airlock at x 240 (t 165)"))
    for t, x, y in BLINKS:
        out.append(dict(name="red-blink", t=t_centre_at(t, y, 1.0), x=x,
                        note="emergency lights blinking red" if t == BLINKS[0][0] else ""))
    out += [dict(name="far-wreck-a", t=20.0, x=380, note="far wrecks"), dict(name="far-wreck-b", t=60.0, x=90, note=""),
            dict(name="far-hulk-turn", t=100.0, x=400, note="a far hulk turning slowly"),
            dict(name="far-wreck-a", t=140.0, x=110, note="")]
    out.append(dict(name="ceres-point", t=t_centre_at(END, 120, FACTORS["deep"]), x=300,
                    note=f"Ceres ahead, a hard white point, 120 px down at the end (t={END:g})"))
    out.append(dict(name="sovereign-lights", t=t_centre_at(END, 132, FACTORS["deep"]), x=380,
                    note="beside it the Sovereign's ring of gold lights"))
    pieces = pieces_spec()
    for p in out:
        p["layer"] = pieces[p["name"]]["layer"]
        p["t"] = round(float(p["t"]), 3)
    return out


def _vigil_place():
    """The Vigil placed by her shutter (entering at t 56 at x 90)."""
    row = entry_row(56.0, SECRET)
    return place_for("vigil", VIGIL_SHUTTER[1], row)


VIGIL_X = 155                       # the shutter at x 155 - 110 + 45 = 90, the beacon at 235
SABLE_X = 404                       # the hatch at x 404 - 56 + 56 = 404


def _sable_place():
    return place_for("sable-wren", SABLE_HATCH[1], entry_row(96.0, SECRET))


VIGIL_T = None
SABLE_T = None

# scenery pieces (no targets): (name, t of the centre at the screen's middle, x, note)
SCENERY = [("plate-a", 3.0, 380, "the graveyard's edge: scattered plates"), ("plate-c", 8.0, 96, ""),
           ("corvette-spine", 20.0, 170, "a broken corvette's spine (the first container on it)"),
           ("plate-b", 22.0, 370, ""), ("plate-helix", 27.0, 410, ""),
           ("cdf-frigate-b", 31.0, 375, "a third frigate of the picket"),
           ("plate-b", 42.5, 70, ""), ("plate-a", 47.0, 160, ""),
           ("plate-c", 63.0, 400, ""), ("plate-helix", 66.0, 80, ""),
           ("helix-hulk-a", 97.0, 560, "a hulk at the right edge (the Sable Wren wedged against it)"),
           ("helix-hulk-b", 83.0, 420, ""), ("plate-helix", 80.0, 60, ""),
           ("plate-a", 104.0, 80, ""),
           ("cdf-frigate-a", 134.0, 110, "the tangle begins"), ("helix-hulk-b", 137.0, 400, ""),
           ("cdf-frigate-b", 153.0, 380, ""), ("helix-hulk-a", 155.0, 90, ""),
           ("plate-b", 151.0, 420, ""),
           ("plate-c", 168.0, 90, "the last plates"), ("plate-b", 172.2, 330, "the last container's plate"),
           ("plate-a", 176.0, 140, ""), ("plate-helix", 180.5, 380, "")]
# the red lights blinking (t, x, y at that t): never more than two on the screen
BLINKS = [(30.0, 130, 200), (58.0, 60, 300), (84.0, 360, 260), (113.0, 400, 220), (146.0, 420, 300),
          (166.0, 70, 240)]


def _resolve():
    global VIGIL_T, SABLE_T
    VIGIL_T = _vigil_place()
    SABLE_T = _sable_place()


_resolve()


def piece_at(name, placed):
    """The placement of the piece that carries the level's targets (not a scenery copy)."""
    for p in placed:
        if p.get("piece", p.get("name")) == name and p.get("role") != "scenery":
            return p
    raise KeyError(name)


# the containers: (kind, t, the piece they stand on, its x on the piece (local)); the piece is the placement of that
# name on the screen at t
CONTAINERS = [("cargo-container-cdf", 18.0, "corvette-spine", 70), ("cargo-container-cdf", 38.0, "cdf-frigate-a", 40),
              ("cargo-container-cdf", 58.0, "vigil", 182), ("cargo-container-helix", 80.0, "helix-hulk-b", 64),
              ("cargo-container-helix", 150.0, "cdf-frigate-b", 140), ("cargo-container-cdf", 170.0, "plate-b", 84)]


def placement_near(name, t, placed):
    """The placement of piece ``name`` whose rows hold a target entering at the top edge at t."""
    row = entry_row(t, CONTAINER[1])
    for p in placed:
        if p["name"] == name:
            c = centre_row(p["t"])
            if abs(row - c) < SIZES[name][1] / 2:
                return p
    raise KeyError(f"{name} at t {t}")


def charges(placed):
    """The three mining charges beside the roads of the convoys of t 50, 88, 140: on the road's flank (36 px from its
    centre), on the row the first tank reaches with its centre at y 250 (the charge then 36 px off its side)."""
    out = []
    for tc, side in ((50.0, 1), (88.0, 1), (140.0, -1)):
        t, n, name, x, py = next(c for c in CONVOYS if c[0] == tc)
        p = piece_at(name, placed)
        pts = road_ground(name, p["t"], p["x"])
        r0 = scroll(t) + SCREEN + 40
        tm = t + 290 / 90                                   # the first tank at y 250
        row = r0 + TANK_SPEED * (tm - t)
        cx = x_on_road(pts, row) + side * 36
        out.append(("mining-charge", round(t_of_row(row, CHARGE), 2), int(round(cx)), name))
    return out


def objects(placed):
    """Every ground object: dict(kind, t, x, piece, size)."""
    out = []
    for kind, t, piece, px in CONTAINERS:
        p = placement_near(piece, t, placed)
        x = p["x"] - SIZES[piece][0] / 2 + px
        out.append(dict(kind=kind, t=t, x=int(round(x)), piece=piece, size=CONTAINER))
    for kind, t, x, piece in charges(placed):
        out.append(dict(kind=kind, t=t, x=x, piece=piece, size=(CHARGE, CHARGE)))
    st = piece_at("freighter-stern", placed)
    for i, (dx, dy) in enumerate(CLAMPS):
        x, row = local_to_ground("freighter-stern", st["t"], st["x"], HOLD[0] + dx, HOLD[1] + dy)
        out.append(dict(kind="hold-clamp-" + ("left" if dx < 0 else "right"), t=round(t_of_row(row, CLAMP), 2),
                        x=int(round(x)), piece="freighter-stern", size=(CLAMP, CLAMP)))
    x, row = local_to_ground("freighter-stern", st["t"], st["x"], *HOLD)
    out.append(dict(kind="helix-core", t=round(t_of_row(row, CRATE[1]), 2), x=int(round(x)), piece="freighter-stern",
                    size=CRATE))
    v = piece_at("vigil", placed)
    x, row = local_to_ground("vigil", v["t"], v["x"], *VIGIL_SHUTTER)
    out.append(dict(kind="vigil-shutter", t=round(t_of_row(row, SECRET), 2), x=int(round(x)), piece="vigil",
                    size=(SECRET, SECRET)))
    s = piece_at("sable-wren", placed)
    x, row = local_to_ground("sable-wren", s["t"], s["x"], *SABLE_HATCH)
    out.append(dict(kind="sable-wren-hatch", t=round(t_of_row(row, SECRET), 2), x=int(round(x)), piece="sable-wren",
                    size=(SECRET, SECRET)))
    tb = piece_at("old-marta", placed)
    x, row = local_to_ground("old-marta", tb["t"], tb["x"], *MARTA_LOCK)
    out.append(dict(kind="old-marta-airlock", t=round(t_of_row(row, SECRET), 2), x=int(round(x)), piece="old-marta",
                    size=(SECRET, SECRET)))
    return out


def beacon(placed):
    """The CDF beacon on the Vigil's mount: (t when it is at y 160 (380 up), x)."""
    v = piece_at("vigil", placed)
    x, row = local_to_ground("vigil", v["t"], v["x"], *VIGIL_BEACON)
    return round(time_at(row - SCREEN + 160), 2), int(round(x))


def convoys(placed):
    out = []
    for t, n, name, x, py in CONVOYS:
        p = piece_at(name, placed)
        pts = road_ground(name, p["t"], p["x"])
        paths = [unit_path(pts, t + i * INTERVAL) for i in range(n)]
        if n < 3:                                           # a third unit's path where the road reaches (hard)
            try:
                paths.append(unit_path(pts, t + n * INTERVAL))
            except ValueError:
                pass
        out.append(dict(t=t, count=n, piece=name, paths=paths))
    return out


# --------------------------------------------------------------------------- debris (the belt's small rocks)

SMALL = [(5.0, 120), (11.5, 360), (19.0, 260), (31.0, 420), (44.0, 230), (56.5, 380), (72.0, 70), (83.5, 300),
         (95.0, 160), (141.5, 70), (152.0, 260), (163.0, 400)]


def debris_lines():
    lines = ["debris:",
             "  # Proposal (ART-G): a few of the belt's small rocks drifting through sections 1-3 and 5 (part B's",
             "  # debris-belt-a/b/c, 6 HP), none in the freighter's section (t 105-135) or clear space; the Mites' own",
             "  # rocks are their waves' on_rocks. No big rocks in Level 34.",
             "  chunks:",
             "    belt-a: {size: [40, 36], hp: 6}",
             "    belt-b: {size: [36, 32], hp: 6}",
             "    belt-c: {size: [32, 28], hp: 6}",
             "  clearance: 120",
             "  placed:"]
    for i, (t, x) in enumerate(SMALL):
        lines.append(f"    - {{t: {t:g}, x: {x}, chunk: belt-{'abc'[i % 3]}, drift: [{(-1) ** i * (16 + 8 * (i % 4))}, "
                     f"-{170 + 10 * (i % 3)}]}}")
    return lines


def proposal_text(pieces, placed):
    src = "the level's data.yaml" if DATA else "the README's Layout table (no data.yaml yet: re-run after G2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 34's ship graveyard (tools/art/backdrop_l34.py, step ART-G, from {src}).",
             "  # A ground layer without a tile set: open space is the deep layer (the base); the ground holds the",
             "  # hulls (CDF white-grey, Helix grey-gold with gold trim: QG-1 a) with the tank roads on their walkways",
             "  # and keels, the freighter (two pieces placed exactly: stern + bow, 12 px overlap) and the scattered",
             "  # plates; red-blink is the only animated ground piece (at most two on the screen). Deep: the dust band,",
             "  # far wrecks, a far hulk turning, Ceres's point and the Sovereign's gold ring at the end.",
             "  scroll_factors: {deep: 0.12, far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far, low-air unused",
             "  ramp: 2",
             "  haze_colour: 09090e",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:",
             f"    grave-space: {{layer: deep, height: {SPACE_H}}}   # space, the sunlit dust band, stars",
             f"    grave-dust: {{layer: high-air, height: {DUST_H}}}   # close grit and dust motes (transparent)",
             "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']:g}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p["note"]:
            lines.append(f"    # {p['note']}")
        lines.append(f"    - {{piece: {p['name']}, t: {p['t']}, x: {p['x']}}}")
    lines.append("sections:   # ART-G's reading of the README's Layout (130 px/s, the freighter 110 with 2 s eases); G2 "
                 "writes the real ones")
    for s in SECTIONS:
        lines.append("  - {" + ", ".join(f"{k}: {v}" for k, v in s.items()) + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px)")
    lines += ["convoys:   # the Warden Tank convoys on their roads: one path per unit (unit i enters at t + i x "
              f"{INTERVAL:g} s, its",
              "  # path in screen x, y below the top edge at its own entry: the sim's walker rule), "
              f"{PATH_LEN:g} px of road",
              "  # from the entry; a path per unit (a pair's third path where its road reaches, for a hard extra). The",
              "  # roads jog at the plates' seams (the tank's 60 deg/s turn). Another interval moves the paths:",
              "  # re-run --proposal after changing INTERVAL in the generator."]
    for c in convoys(placed):
        lines.append(f"  - t: {c['t']:g}   # x{c['count']} on {c['piece']}")
        lines.append(f"    interval: {INTERVAL:g}")
        lines.append("    paths:")
        for path in c["paths"]:
            lines.append("      - [" + ", ".join(f"[{x:g}, {y:g}]" for x, y in path) + "]")
    lines += ["ground_objects:   # at: [t, x], when the object enters at the top edge and its x (on its hull's pixels).",
              "  # Containers 40x28 (12 HP, `sprite: cargo-container-cdf|-helix`: _0..2 intact/damaged/wrecked + -break);",
              "  # charges `mining-charge` 24x24 (E's), on a road's flank (64 px blast); the hold's clamps 32x32 (15 HP,",
              "  # group helix-clamps, `sprite: hold-clamp-left|-right`, the jaws facing the crate); the crate's place",
              "  # (60x40, the story pickup `helix-core` rises there: helix-core, -rise, -drift, -glow); the secrets' triggers 40x40 (20 HP,",
              "  # 28 hard) with their own looks: vigil-shutter, sable-wren-hatch, old-marta-airlock (_0..2: shut, hit,",
              "  # open)."]
    for o in objects(placed):
        lines.append(f"  - {{kind: {o['kind']}, at: [{o['t']:g}, {o['x']}], size: [{o['size'][0]}, {o['size'][1]}]}}"
                     f"   # on {o['piece']}")
    bt, bx = beacon(placed)
    lines += ["drops:   # the CDF beacon on the Vigil (Level 33's nav-buoy frames: BuoyLooks.MOORED)",
              f"  - {{t: {bt:g}, x: {bx}, y: 380, pickup: armour patch, sprite: nav-buoy}}"]
    lines += debris_lines()
    return "\n".join(lines) + "\n"


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data


# =========================================================================== composite, review, check

_CACHE = {}


def piece_images(name, spec):
    n = spec.get("frames")
    if n:
        return [Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)]
    return [Image.open(OUT / f"{name}.png").convert("RGBA")]


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
    f = FACTORS[spec["layer"]]
    centre = scroll(float(p["t"])) * f + SCREEN / 2
    k = int(t * spec["fps"]) % spec["frames"] if spec.get("frames") else 0
    return p["x"], scroll(t) * f + SCREEN - centre, k


def blit(img, p, pieces, t, layer):
    spec = pieces[p["piece"]]
    if spec["layer"] != layer:
        return img
    x, y, k = place_of(p, spec, t)
    frame = images(p["piece"], spec)[k]
    if y - frame.height / 2 > SCREEN or y + frame.height / 2 < 0:
        return img
    return bu.paste(img, frame, x, y)


def _held(name):
    try:
        return bu.load(name)
    except FileNotFoundError:
        return None


def compose(t, pieces, placed, data=None, units=True):
    g = scroll(t)
    img = Image.fromarray(tile_rows("grave-space", SPACE_H, g * FACTORS["deep"]), "RGBA")
    for layer in ("deep", "ground"):
        for p in placed:
            img = blit(img, p, pieces, t, layer)
    if units and data:
        img = stand_ins(img, t, data)
    dust = Image.fromarray(tile_rows("grave-dust", DUST_H, g * FACTORS["high-air"]), "RGBA")
    img.alpha_composite(dust)
    return img


def screen_y(t, t_enter, h):
    return scroll(t) - scroll(t_enter) - h / 2


def tank_at(path, t_unit, t):
    """(x, y, heading in rad (atan2, y down)) of a tank on its path at t (40 px/s along it, plus the scroll)."""
    s = scroll(t) - scroll(t_unit)
    d = TANK_SPEED * (t - t_unit)
    pts = [(x, y) for x, y in path]
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        seg = np.hypot(x1 - x0, y1 - y0)
        if d <= seg:
            u = d / seg
            return x0 + (x1 - x0) * u, y0 + (y1 - y0) * u + s, np.arctan2(y1 - y0, x1 - x0)
        d -= seg
    x1, y1 = pts[-1]
    return x1, y1 + s, -np.pi / 2


def heading_frame(ang, n):
    return int(round((ang - np.pi / 2) / (TAU / n))) % n


def stand_ins(img, t, data):
    hull, tur, shade = _held("warden-tank-asc"), _held("warden-tank-asc-turret"), _held("warden-tank-asc-shadow")
    looks = {k: _held(k) for k in ("cargo-container-cdf", "cargo-container-helix", "mining-charge", "hold-clamp-left",
                                   "hold-clamp-right", "helix-core", "vigil-shutter", "sable-wren-hatch",
                                   "old-marta-airlock", "nav-buoy")}
    for o in data["ground_objects"]:
        nt, x = o["at"]
        h = o["size"][1]
        y = screen_y(t, nt, h)
        fr = looks.get(o["kind"])
        if fr and -60 < y < SCREEN + 60:
            img = bu.paste(img, fr[0], x, y)
    d = data["drops"][0]
    yb = SCREEN - 380 + scroll(t) - scroll(d["t"])
    if looks["nav-buoy"] and -40 < yb < SCREEN + 40:
        img = bu.paste(img, looks["nav-buoy"][0], d["x"], yb)
    if hull and tur:
        ship_x = 240 + 140 * np.sin(t * 0.5)
        for c in data["convoys"]:
            n = 2 if c["t"] in (32, 70, 123) else 3
            for i in range(n):
                tu = c["t"] + i * INTERVAL
                if t < tu:
                    continue
                x, y, ang = tank_at(c["paths"][i], tu, t)
                if -50 < y < SCREEN + 50:
                    if shade:                       # G.1 c: the faint ground shadow under the hull
                        img = bu.paste(img, shade[heading_frame(ang, 16)], x, y)
                    img = bu.paste(img, hull[heading_frame(ang, 16) * 2 + int(t * 9) % 2], x, y)
                    aim = np.arctan2(440 - y, ship_x - x)
                    img = bu.paste(img, tur[heading_frame(aim, 32) * 2], x, y)
    return bu.paste(img, artkit.load_frames("ship")[2], 240 + 140 * np.sin(t * 0.5), 440)


SHOTS = [(20.5, "GRAVEYARD EDGE: THE CORVETTE'S SPINE"), (36.0, "CDF PICKET: THE FIRST CONVOY"),
         (53.5, "CDF PICKET: A CHARGE BY THE ROAD"), (59.5, "THE VIGIL: HER BRIDGE, THE BEACON"),
         (74.0, "HELIX HULKS: A CONVOY ON THE KEEL"), (99.0, "THE SABLE WREN, WEDGED"),
         (111.0, "THE FREIGHTER'S HOLD: CRATE AND CLAMPS"), (127.0, "THE FAR DECK: THE TANK PAIR"),
         (143.5, "THE TANGLE: A CONVOY, A CHARGE"), (167.5, "THE TANGLE: OLD MARTA"),
         (END - 0.5, "CLEAR SPACE: CERES, THE SOVEREIGN")]


def review():
    pieces, placed, data = load_proposal()
    shots = [(compose(t, pieces, placed, data), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    pieces_h = 300
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + pieces_h + 30,
                         "LEVEL 34 BACKDROP (FINAL R67): THE SHIP GRAVEYARD", f"PRODUCTION ART, {BATCH.upper()} - R67")
    raster.draw_text(sheet, 16, 38, "ELEVEN SCREENS AT HALF SIZE WITH STAND-INS (THE TANKS IN LIVERY ON THEIR ROADS, "
                                    "CONTAINERS, CHARGES, THE CLAMPS AND CRATE, THE SECRETS' PROPS, THE SHIP)",
                     raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 10
    raster.draw_text(sheet, 16, y, "THE PIECES ON OPEN SPACE (THE HULLS AT A QUARTER, THE REST AT 1X)", raster.LABEL)
    x = 16
    space = Image.open(OUT / "grave-space.png").convert("RGBA")
    for name in ["cdf-frigate-a", "cdf-frigate-b", "vigil", "helix-hulk-a", "helix-hulk-b", "freighter-stern",
                 "freighter-bow", "tangle-a", "tangle-b", "corvette-spine", "sable-wren", "old-marta", "plate-b",
                 "plate-helix",
                 "far-hulk-turn", "sovereign-lights"]:
        fr = images(name, pieces[name])[0]
        if fr.height > 200:
            fr = fr.resize((fr.width // 4, fr.height // 4), Image.BOX)
        if x + fr.width > sheet.width - 16:
            break
        cell = space.crop((0, 0, fr.width + 8, fr.height + 8))
        cell.alpha_composite(fr, (4, 4))
        sheet.alpha_composite(cell, (x, y + 16))
        x += fr.width + 10
    artkit.REVIEW_ROUND = ROUND
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    import enemies_r60 as e60  # noqa: E402
    frames = [compose(104 + i / 8, pieces, placed, data).resize((sw, sh), Image.BOX) for i in range(80)]
    size = e60.rig.write_gif(frames, gif, fps=8, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")


def check():
    """The space tile wraps and is opaque, sizes match the proposal, Source chunks, the deep pieces' edges clear, the
    ground pieces' top and bottom rows clear, every placement on the screen, at most two animated ground pieces on the
    screen at once, every object on opaque hull pixels and >= 20 px inside the field, every tank path on its road's
    opaque pixels for the 280 px it drives on the screen, each charge within 48 px of its road."""
    problems = []
    pieces, placed, data = load_proposal()
    a = np.array(Image.open(OUT / "grave-space.png").convert("RGBA"))
    if l30.wrap_score(a) > 1.5:
        problems.append(f"grave-space: wrap score {l30.wrap_score(a):.2f}")
    if (a[..., 3] < 255).any():
        problems.append("grave-space: not opaque")
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
                animated += bool(spec.get("frames")) and spec["layer"] == "ground"
        worst = max(worst, (animated, t))
    if worst[0] > 2:
        problems.append(f"t={worst[1]:.1f}: {worst[0]} animated pieces on the screen")
    for i, p in enumerate(placed):
        if i not in seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")

    def opaque_at(x, row):
        for p in placed:
            spec = pieces[p["piece"]]
            if spec["layer"] != "ground" or spec.get("frames"):
                continue
            im = images(p["piece"], spec)[0]
            centre = scroll(p["t"]) + SCREEN / 2
            py = int(round(spec["size"][1] / 2 - (row - centre)))
            px = int(round(x - p["x"] + spec["size"][0] / 2))
            if 0 <= py < im.height and 0 <= px < im.width and np.array(im)[py, px, 3] > 0:
                return True
        return False
    for o in data["ground_objects"]:
        nt, x = o["at"]
        if not 20 <= x <= W - 20:
            problems.append(f"{o['kind']} x {x}: not 20 px inside the field")
        if not opaque_at(x, entry_row(nt, o["size"][1])):
            problems.append(f"{o['kind']} x {x} (t {nt}) not on a hull's pixels")
    for c in data["convoys"]:
        for i, path in enumerate(c["paths"]):
            tu = c["t"] + i * INTERVAL
            s = scroll(tu)
            pts = [(x, SCREEN - y + s) for x, y in path]
            d0 = 0.0
            for (x0, r0), (x1, r1) in zip(pts, pts[1:]):
                seg = np.hypot(x1 - x0, r1 - r0)
                for u in np.linspace(0, 1, max(2, int(seg / 8))):
                    if d0 + u * seg > 280:
                        break
                    if not opaque_at(x0 + (x1 - x0) * u, r0 + (r1 - r0) * u):
                        problems.append(f"convoy t {c['t']:g} unit {i}: off the hull at "
                                        f"({x0 + (x1 - x0) * u:.0f}, row {r0 + (r1 - r0) * u:.0f})")
                        break
                d0 += seg
    for o in data["ground_objects"]:
        if o["kind"] != "mining-charge":
            continue
        nt, x = o["at"]
        row = entry_row(nt, CHARGE)
        best = 1e9
        for c in data["convoys"]:
            s = scroll(c["t"])
            pts = [(px, SCREEN - y + s) for px, y in c["paths"][0]]
            xr = x_on_road(pts, row)
            if xr is not None:
                best = min(best, abs(xr - x))
        if best > 48:
            problems.append(f"charge t {nt}: {best:.0f} px from the nearest road")
    for msg in problems:
        print("PROBLEM", msg)
    print(f"{len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}; {len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--proposal" in args:
        write_proposal(pieces_spec(), placements())
        sys.exit(0)
    if "--review" not in args:
        pc, pl = build_all()
        write_proposal(pc, pl)
    review()
