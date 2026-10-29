#!/usr/bin/env python3
"""Production art: the Level 32 backdrop, Deep Core (design/campaign/act-5-the-belt/level-32-deep-core; M9 part E
batch, concept round 65, step ART-E).

Straight to production from round 60's belt mood a (tools/concept/belt_r60.py ``mood_stills`` L32: dark tunnels in
warm asteroid rock, cool white work lamps along the walls, the refinery's orange glow; tools/concept/enemies_r60.py
``tunnel_bg``), the tunnel kit of the README's *Layout*: Kleopatra's sunlit face and the shaft's mouth, the galleries
cut into rock with lamp strings and the Ascendancy's red running lights along the wall feet, gallery notches and launch
bays, two fork islands with their two dressings each (fork one: the old drift left, the charge seam right; fork two:
the cooling ducts left, the ore chute right), the pumping hall, the refinery's furnace floor with the crucible and the
gantry, the ore chute's mouth onto space. The structure is backdrop_l25.py's (its wall projection, wall-field and
noise helpers imported and reused, never edited; its globals rebound to this level in-process as backdrop_l26.py does)
and the islands are backdrop_l16.py's butte model.

**The terrain this art is drawn for** (``proposal_terrain``; the data's when it exists): the README's wall table
(the shaft closing from t=4, the galleries 70/410, fork one's island x 165-315 at rows 6,838-9,230, the hall 30/450,
fork two's island at 14,638-17,225, the refinery 40/440, the chute's mouth opening by t=186) with **QE-1 a** (pending
round 65): every Sentinel's 28 px emitter at least 40 px inside the field (centre x 54 or 426), each tower's notch only
as deep as that allows (the galleries' 56 px, the hall's 16 px, the crucible's 26 px), the hall pair's notches offset
221 rows (t=79 left, t=80.7 right); the launch bays 56 deep; every notch 140 rows long. Blast doors are flush in the
wall face (no notch): the lock 42 px from the wall's foot (12 px half box: 40 px inside the field).

**QE-2 a** (pending round 65): at each fork's head the richer channel's mouth carries the Ascendancy's red running
lights and a lit (open) bulkhead, the safer one white work lamps on stands and the consortium's faded stripes. The
caches' own blast doors cannot show before the choice (DeepCoreChecks: a branch's item comes after the latest choice),
so the mouth's bulkhead is the backdrop's.

Tile sets (480x960, wrapping, ground):
  tunnel-floor      sections 1-2: cut rock, dust and rubble, old track grooves, cable runs, dark
  fork-drift-seam   section 3 (fork one): left the old drift (cold grey dust, rusted rails, old bore holes), right the
                    charge seam (fresh pale cuts, drill scars, yellow charge marks, a red-lit cable)
  pumping-hall      section 4: the hall's floor plates, pipe runs along it, drains
  fork-ducts-chute  section 5 (fork two): left the cooling ducts (blue-grey duct runs, frosted grilles), right the ore
                    chute (two conveyor belts with ore)
  furnace-floor     section 6: cast floor plates, slag channels glowing orange, rails
Wall textures (CanyonProfile's convention, backdrop_l25.py's ice path: ice-wall is the projected wall, not ice):
  ice-wall          128x1280  cut rock: the dark foot face, a cable tray, the main drill-scored face with its lamp
                              string (a lamp every 128 rows), a ledge, the upper face, then dark (the rock overhead)
  ice-wall-glow     128x1280  additive: the lamps (white-hot cores, a soft wash on the face) and the red running lights
                              along the foot (every 64 rows)
  ice-wall-foot     24x512    the foot band: a dark crack, rubble and dust thinning out
  drift-rock-face, duct-rock-face   128x528  the islands' faces (the same rock and lamp strings, period 512)
  drift-rock-top_0..2, duct-rock-top_0..2   the islands' caps at 1.25 on the canvas of their foot x 1.25, far end
                              first (dark rock, cable bundles)
Pieces (ground; exact rows):
  shaft-mouth       480x2040 at t=0: Kleopatra's sunlit face round the shaft's lip, gantries over the lip, the
                    shaft floor going into shadow; fades into tunnel-floor at its top
  seam-<tile>       480x320 on each section seam
  drift-mouth, seam-mouth, ducts-mouth, conveyor-mouth   160x128 (+ -glow): the channels' mouths at each island's tip
                    (QE-2 a: seam-mouth and ducts-mouth richer: red lights and a lit bulkhead; the others white lamps)
  pump              112x112 (+ -glow): a pumping-hall pump (three of them)
  refinery-gate     420x72 (+ -glow): the refinery's gate across the rejoin (the repair crate on it)
  crucible          144x144 (+ -glow): the crucible by the crucible gallery
  gantry            420x88 (+ -glow): the gantry across the furnace floor (the SAM trio stands on it)
  chute-end, chute-space   480x2040 each, stacked: the ore chute's conveyors to the mouth's lip, then belt space
                    (stars, the dust band) to the outro's end
  gallery-<side>-<depth>  NOT placed: a notch's mouth seen from above, drawn by the game over the wall at each terrain
                    notch of that side and depth (E4): the foot line at column depth + 16 (left) / 28 (right), the
                    notch's middle at the image's middle row; gallery-right-56, gallery-left-56, gallery-left-16,
                    gallery-right-16, gallery-left-26
Review: design/campaign/act-5-the-belt/level-32-deep-core/concept/backdrop-final-r65-a.png/.gif (the walls and islands
projected as WallProjection does, approximately; stand-ins for the units from belt_units_e.py); `--proposal` writes the
`backdrop:` block, the sections' tiles, the proposed `terrain:` block (walls, islands, notches, forks) and the ground
targets this art assumes to the level's concept/backdrop-proposal.yaml (never into data.yaml).

Run: python3 tools/art/backdrop_l32.py [--proposal | --review | --check]   (all: about 2 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
import backdrop_l16 as b16
import backdrop_l25 as b25            # noqa: E402  (tools/art: the wall projection, wall fields, helpers)
import l23kit
from artkit import DESIGN, ROOT, TAU, raster

import enemies_r60 as e60  # noqa: E402  (concept script, imported unchanged: the caption, the GIF writer)

SCRIPT = "backdrop_l32.py"
BATCH = "M9 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r65"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-5-the-belt" / "level-32-deep-core"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-32"
W, SCREEN, TILE_H = 480, 540, 960
FEATHER = 32
MAX_PIECE = 2040
OUTRO = 15
LAMP = np.array((236, 242, 255), float)
RED = np.array((255, 44, 32), float)
ORANGE = np.array((255, 128, 40), float)
GOLD = np.array((200, 152, 60), float)
pn, ramp, finish, padded, dither_alpha = b16.pn, b16.ramp, b16.finish, b16.padded, b16.dither_alpha
posterize = b25.posterize
TEX_W, TEX_PERIOD, TEX_PAD, FACE_PAD = b16.TEX_W, b16.TEX_PERIOD, b16.TEX_PAD, b16.FACE_PAD
ISLAND_PERIOD = 512
ISLAND_SCALE = 0.5            # CanyonProfile.islandScale's default (a look that is neither mesa nor pillar)

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
FALLBACK_SECTIONS = [{"name": "The Shaft Mouth", "end": 18}, {"name": "The Galleries", "end": 54},
                     {"name": "Fork One", "end": 74}, {"name": "The Pumping Hall", "end": 114},
                     {"name": "Fork Two", "end": 136}, {"name": "The Refinery", "end": 186, "speed": 120, "ramp": 2}]
SECTIONS = DATA["sections"] if DATA else FALLBACK_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 130)) if DATA else 130.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
SECTION_TILES = ["tunnel-floor", "tunnel-floor", "fork-drift-seam", "pumping-hall", "fork-ducts-chute",
                 "furnace-floor"]


# --------------------------------------------------------------------------- the clock (as backdrop_l25)

def _speeds():
    out, prev = [], None
    for start, s in zip(STARTS, SECTIONS):
        sp = float(s.get("speed", BASE_SPEED))
        out.append((start, float(s["end"]), sp, float(s.get("ramp", 2)), prev))
        prev = sp
    return out


SPEEDS = _speeds()
DT = 1 / 600


def speed_at(t):
    v = SPEEDS[0][2]
    for start, end, sp, rmp, prev in SPEEDS:
        if t >= start:
            if rmp and prev is not None and prev != sp and t < start + rmp:
                u = (t - start) / rmp
                v = prev + (sp - prev) * u * u * (3 - 2 * u)
            else:
                v = sp
    return v


_T = np.arange(0, END + OUTRO + 20, DT)
_S = np.concatenate([[0.0], np.cumsum([speed_at(t + DT / 2) * DT for t in _T[:-1]])])


def scroll(t):
    return float(np.interp(t, _T, _S))


def time_at(s):
    return float(np.interp(s, _S, _T))


def seam_row(index):
    """The ground layer's row where section ``index`` begins (its tile set starts at the top edge)."""
    return scroll(STARTS[index]) + SCREEN


def section_at_row(row):
    s = 0
    for i in range(1, len(SECTIONS)):
        if row >= seam_row(i):
            s = i
    return s


def _f(t):
    return int(round(scroll(t) / 10.0) * 10)


# --------------------------------------------------------------------------- the terrain (data, or the proposal)

TOWER = 80                     # the Sentinel's sprite (and the notch's tower); its 28 px emitter
LOCK = 24                      # a blast-door lock's box
INSET = 40                     # QE-1 a: emitters and locks at least 40 px inside the field
NOTCH_W = 140
TOWERS = [("gallery", 24.0, "right"), ("hall", 79.0, "left"), ("hall", 80.7, "right"), ("crucible", 150.0, "left")]
BAYS = [("left", 15.5, "the shaft mouth's launch bay (the Darts at t=15.5)"),
        ("right", 42.5, "the right gallery's launch bay (the Darts at t=42.5)")]


def proposal_walls():
    left = [(520, 0, 0.0), (1820, 70), (6240, 70), (6760, 10), (9230, 10), (9620, 30), (14300, 30), (14638, 10),
            (17225, 10), (17680, 40), (22480, 40), (23680, 0, 0.0)]
    right = [(p[0], W - p[1]) + tuple(p[2:]) for p in left]
    return left, right


def island_poly(r0, r1, x0=165, x1=315):
    """A fork's island: 150 px wide, its tip (the lowest row) blunt, the far end rounded."""
    xm = (x0 + x1) / 2
    return [(xm, r0), (xm + 42, r0 + 14), (x1 - 4, r0 + 52), (x1, r0 + 110), (x1, r1 - 70), (x1 - 8, r1 - 24),
            (xm + 30, r1 - 4), (xm, r1), (xm - 30, r1 - 4), (x0 + 8, r1 - 24), (x0, r1 - 70), (x0, r0 + 110),
            (x0 + 4, r0 + 52), (xm - 42, r0 + 14)]


def _terrain():
    if DATA and DATA.get("terrain"):
        tr = DATA["terrain"]
        left = [tuple(p) for p in tr["left"]]
        right = [tuple(p) for p in tr["right"]]
        islands = [(i.get("look", f"island-{k}"), [tuple(p) for p in i["points"]])
                   for k, i in enumerate(tr.get("islands") or [])]
        notches = [{"side": n["side"], "scroll": float(n["scroll"]), "width": float(n.get("width", NOTCH_W)),
                    "depth": float(n.get("depth", 56)), "why": "data"} for n in tr.get("notches") or []]
        return left, right, islands, notches, True
    left, right = proposal_walls()
    islands = [("drift-rock", island_poly(6838, 9230)), ("duct-rock", island_poly(14638, 17225))]
    return left, right, islands, None, False


LEFT, RIGHT, ISLANDS, _NOTCHES, FROM_DATA = _terrain()


def foot(side, trow):
    return b25._wall_x(LEFT if side == 0 else RIGHT, trow)


def wall_height(side, trow):
    return b25._wall_h(LEFT if side == 0 else RIGHT, trow)


def tower_x(side):
    return INSET + 14 if side == "left" else W - INSET - 14


def proposal_notches():
    out = []
    for name, t, side in TOWERS:
        row = scroll(t) + TOWER / 2
        s = 0 if side == "left" else 1
        x = tower_x(side)
        wall = float(foot(s, row))
        back = x - TOWER / 2 if side == "left" else x + TOWER / 2
        depth = round(abs(wall - back))
        out.append({"side": side, "scroll": round(row), "width": NOTCH_W, "depth": depth,
                    "why": f"the {name} Sentinel (t={t:g}, x {x}: its emitter {INSET} px inside the field)"})
    for side, t, why in BAYS:
        row = max(round(scroll(t) - 150), 1890)            # off the shaft's bend (the wall at 70 from row 1,820)
        out.append({"side": side, "scroll": row, "width": NOTCH_W, "depth": 56, "why": why})
    return sorted(out, key=lambda n: n["scroll"])


NOTCHES = _NOTCHES if _NOTCHES is not None else proposal_notches()
for _look, _poly in ISLANDS:            # this process only: backdrop_l16's butte model on our feet
    b16.ISLANDS[_look] = [tuple(p[:2]) for p in _poly]
    b16.ISLAND_SCALE[_look] = ISLAND_SCALE


def island_rows(k):
    pts = ISLANDS[k][1]
    return min(p[1] for p in pts), max(p[1] for p in pts)


def latest_choice(k):
    """DeepCoreChecks.latestChoice: the island's tip at the bottom edge."""
    return time_at(island_rows(k)[0] + SCREEN)


# --------------------------------------------------------------------------- the ground targets this art assumes

def lock_x(side, trow):
    """A blast door's lock 42 px from the wall's foot (12 px half box: its box 40 px inside the field)."""
    wall = float(foot(0 if side == "left" else 1, trow))
    return round(wall + 42) if side == "left" else round(wall - 42)


def proposal_targets():
    """(kind, t, x, note) of the ground targets the art is laid out for (E2 places the real ones)."""
    out = [("sentinel-tower", t, tower_x(side), f"the {name} Sentinel, in its notch") for name, t, side in TOWERS]
    out += [("blast-door-right (lock 20 HP, branch fork 1 right)", 66.0, lock_x("right", scroll(66) + 12),
             "the charge seam's cache (crate 50)"),
            ("blast-door-sealed-right (lock 30 HP)", 100.0, lock_x("right", scroll(100) + 12),
             "the consortium store (the secret, 100)"),
            ("blast-door-left (lock 20 HP, branch fork 2 left)", 128.0, lock_x("left", scroll(128) + 12),
             "the cooling ducts' cache (crate 50)"),
            ("repair-crate (lock 10 HP)", 134.6, 240, "the refinery gate's armour patch")]
    out += [("mining-charge (branch fork 1 right)", t, x, "the charge seam, by the SAM pair and the Strider's path")
            for t, x in ((60.4, 452), (61.4, 392), (63.4, 336))]
    out += [("sam-nest (branch fork 1 right)", 62.0, x, "the charge seam's SAM pair") for x in (352, 432)]
    out += [("sam-nest", 92.0, x, "the hall's SAM pair (a topple's mark)") for x in (300, 360)]
    out += [("mining-charge", t, x, "by the hall's SAM pair") for t, x in ((91.4, 250), (92.6, 410))]
    out += [("sam-nest (branch fork 2 left)", t, x, "the cooling ducts' SAM pairs")
            for t in (119.0, 126.0) for x in (60, 120)]
    out += [("mining-charge (branch fork 2 left)", t, x, "the cooling ducts") for t, x in
            ((119.8, 90), (123.0, 46), (126.8, 90))]
    out += [("sam-nest", 162.0, x, "the gantry's SAM trio") for x in (180, 240, 300)]
    out += [("mining-charge", t, x, "on the gantry by the trio") for t, x in
            ((161.4, 150), (161.4, 330), (162.8, 210), (162.8, 270))]
    return out


TARGETS = proposal_targets()


# --------------------------------------------------------------------------- texture helpers

def grid(h=TILE_H, w=W):
    return np.mgrid[0:h, 0:w].astype(np.float64)


@lru_cache(maxsize=None)
def tile(name):
    return np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))


def tile_rows(name, bottom, h):
    t = tile(name)
    rows = bottom + h - 1 - np.arange(h)
    return t[(t.shape[0] - 1 - np.mod(rows, t.shape[0])).astype(int)]


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


def stamps(h, w, pts, fn):
    """A periodic-in-rows field: ``fn(dx, dy)`` summed (max) over the points (x, y), wrapped by h."""
    yy, xx = grid(h, w)
    f = np.zeros((h, w))
    for x, y in pts:
        for dy in (-h, 0, h):
            f = np.maximum(f, fn(xx - x, yy - y - dy))
    return f


def warm_rock(t):
    return ramp([(28, 25, 23), (52, 46, 41), (78, 70, 62), (104, 94, 82), (128, 116, 100)], t)


def rock_floor(seed, gain=1.0, h=TILE_H):
    """Cut rock floor: broad relief, rubble, dust; lit from the upper left (b15.lit), wrapping."""
    n = pn(W, h, 96, seed, 4)
    fine = pn(W, h, 8, seed + 1, 3)
    rub = pn(W, h, 16, seed + 2, 3)
    hgt = 6 * n + 1.6 * fine + 3.5 * np.clip((rub - 0.62) * 4, 0, 1)
    t = np.clip(0.42 + 0.35 * (n - 0.5) + 0.25 * (fine - 0.5) + 0.2 * np.clip((rub - 0.6) * 3, 0, 1), 0, 1)
    gy = (np.roll(hgt, -1, 0) - np.roll(hgt, 1, 0)) / 2
    gx = (np.roll(hgt, -1, 1) - np.roll(hgt, 1, 1)) / 2
    shade = np.clip(1.0 + (gx + gy) * 0.35, 0.45, 1.5)
    return warm_rock(t) * shade[..., None] * gain


def lines_along(rgb, xs, width, col, k=1.0, wob=0.0, seed=0, h=TILE_H):
    """Straight runs along the rows (rails, pipes, belts): a column band at each x."""
    yy, xx = grid(h, rgb.shape[1])
    for x in xs:
        off = wob * (pn(rgb.shape[1], h, 64, seed + int(x), 2)[:, :1] - 0.5) if wob else 0
        band = np.abs(xx - x - off) < width / 2
        rgb = np.where(band[..., None], rgb * (1 - k) + np.asarray(col, float) * k, rgb)
    return rgb


# --------------------------------------------------------------------------- tile sets

def tunnel_floor():
    rgb = rock_floor(3301, 0.82)
    yy, xx = grid()
    # old track grooves and a cable run along the floor, dust in the middle
    for x in (150, 330):
        g = np.abs(xx - x) < 1.2
        rgb = np.where(g[..., None], rgb * 0.62, rgb)
        rgb = np.where((np.abs(xx - x - 2) < 0.6)[..., None], rgb * 1.18 + 6, rgb)
    cab = np.abs(xx - 96 - 3 * np.sin(yy / TILE_H * TAU * 3)) < 1.1
    rgb = np.where(cab[..., None], np.array((22, 20, 20.0)), rgb)
    dust = pn(W, TILE_H, 48, 3303, 3)
    rgb = rgb * (1 + 0.12 * (dust[..., None] - 0.5))
    return finish(rgb, 32, amount=0.1)


def fork_drift_seam():
    yy, xx = grid()
    left = rock_floor(3311, 0.66)
    lum = left @ np.array((0.3, 0.55, 0.15))
    left = left * 0.55 + lum[..., None] * np.array((0.42, 0.44, 0.47)) * 1.0      # cold, grey, dusty
    for x in (64, 92):                                          # the old drift's rusted rails and their sleepers
        left = lines_along(left, [x], 2.2, (70, 44, 30), 0.85)
    sleep = (np.abs(((yy + 4) % 24) - 12) < 2.5) & (xx > 56) & (xx < 100)
    left = np.where(sleep[..., None], left * 0.6 + np.array((40, 32, 26.0)) * 0.4, left)
    bores = stamps(TILE_H, W, [(40, 140), (120, 410), (30, 700), (130, 860), (200, 300)],
                   lambda dx, dy: np.clip(1 - np.hypot(dx, dy) / 9, 0, 1))
    left = left * (1 - 0.75 * np.clip(bores * 2.5, 0, 1))[..., None]
    right = rock_floor(3313, 0.95)
    lum = right @ np.array((0.3, 0.55, 0.15))
    scar = np.abs(((yy * 0.9 + 18 * np.sin(xx / 31)) % 40) - 20) < 1.2           # fresh drill scars, pale
    right = np.where((scar & (xx > 300))[..., None], right * 0.85 + np.array((168, 156, 136.0)) * 0.15, right)
    cab = np.abs(xx - 446 - 4 * np.sin(yy / TILE_H * TAU * 2)) < 1.2
    right = np.where(cab[..., None], np.array((26, 22, 22.0)), right)
    marks = stamps(TILE_H, W, [(380, 120), (420, 380), (360, 640), (430, 880)],
                   lambda dx, dy: (np.hypot(dx, dy) < 3.2).astype(float))
    right = np.where((marks > 0)[..., None], np.array((214, 168, 40.0)), right)
    k = np.clip((xx - 230) / 20, 0, 1)[..., None]              # the dressings meet under the island
    return finish(left * (1 - k) + right * k, 36, amount=0.1)


def pumping_hall():
    yy, xx = grid()
    rgb = rock_floor(3321, 0.8)
    plate = (np.abs(((xx - 30) % 60) - 30) < 0.8) | (np.abs((yy % 80) - 40) < 0.8)
    deck = (xx > 120) & (xx < 360)
    steel = np.array((62, 64, 66.0)) * (0.85 + 0.25 * pn(W, TILE_H, 16, 3323, 2))[..., None]
    rgb = np.where(deck[..., None], steel, rgb)
    rgb = np.where((deck & plate)[..., None], rgb * 0.55, rgb)
    for x, w_ in ((70, 14), (96, 8), (384, 14), (410, 8)):   # the pipe runs, lit along their upper left
        band = np.abs(xx - x) < w_ / 2
        shade = np.clip(1.25 - (xx - x + w_ / 2) / w_ * 0.9, 0.35, 1.25)
        pipe = np.array((88, 92, 96.0)) * shade[..., None]
        rgb = np.where(band[..., None], pipe, rgb)
        flange = band & (np.abs((yy % 120) - 60) < 2)
        rgb = np.where(flange[..., None], np.array((120, 114, 100.0)), rgb)
    drain = stamps(TILE_H, W, [(240, 200), (240, 680)], lambda dx, dy: ((np.abs(dx) < 16) & (np.abs(dy) < 8)) * 1.0)
    grille = (drain > 0) & (np.abs((xx % 4) - 2) < 1)
    rgb = np.where((drain > 0)[..., None], np.array((20, 20, 22.0)), rgb)
    rgb = np.where(grille[..., None], np.array((70, 70, 72.0)), rgb)
    return finish(rgb, 32, amount=0.1)


def fork_ducts_chute():
    yy, xx = grid()
    left = rock_floor(3331, 0.72)
    left = left * np.array((0.86, 0.94, 1.08))
    for x, w_ in ((52, 30), (110, 22)):                      # the cooling ducts: square ducts with frosted grilles
        band = np.abs(xx - x) < w_ / 2
        shade = np.clip(1.2 - (xx - x + w_ / 2) / w_ * 0.6, 0.5, 1.2)
        duct = np.array((84, 98, 112.0)) * shade[..., None]
        left = np.where(band[..., None], duct, left)
        seam = band & (np.abs((yy % 96) - 48) < 1)
        left = np.where(seam[..., None], left * 0.6, left)
        gr = band & (np.abs((yy % 96) - 20) < 8) & (np.abs(((xx - x) % 4) - 2) < 1)
        left = np.where(gr[..., None], np.array((176, 200, 214.0)), left)
    right = rock_floor(3333, 0.8)
    for x0, x1 in ((352, 392), (408, 448)):                  # the ore chute's two conveyor belts
        belt = (xx >= x0) & (xx < x1)
        rubber = np.array((34, 32, 32.0)) * (0.9 + 0.2 * (np.abs((yy % 10) - 5) < 1))[..., None]
        right = np.where(belt[..., None], rubber, right)
        edge = (np.abs(xx - x0) < 1.5) | (np.abs(xx - x1 + 1) < 1.5)
        right = np.where(edge[..., None], np.array((150, 128, 70.0)), right)
        roll = belt & (np.abs((yy % 48) - 24) < 1)
        right = np.where(roll[..., None], np.array((76, 74, 70.0)), right)
    ore = stamps(TILE_H, W, [(360 + (k * 37) % 30, (k * 131) % TILE_H) for k in range(28)] +
                 [(416 + (k * 53) % 30, (k * 97 + 40) % TILE_H) for k in range(28)],
                 lambda dx, dy: np.clip(1 - np.hypot(dx, dy * 1.2) / 3.4, 0, 1))
    right = np.where((ore > 0.1)[..., None], np.array((122, 104, 86.0)) * (0.6 + 0.6 * ore)[..., None], right)
    k = np.clip((xx - 230) / 20, 0, 1)[..., None]
    return finish(left * (1 - k) + right * k, 36, amount=0.1)


def furnace_floor():
    yy, xx = grid()
    rgb = rock_floor(3341, 0.55) * np.array((1.0, 0.92, 0.84))
    plates = (np.abs(((xx - 20) % 80) - 40) < 0.9) | (np.abs(((yy + 13) % 96) - 48) < 0.9)
    iron = np.array((46, 40, 38.0)) * (0.85 + 0.3 * pn(W, TILE_H, 16, 3343, 2))[..., None]
    deck = (xx > 60) & (xx < 420)
    rgb = np.where(deck[..., None], iron, rgb)
    rgb = np.where((deck & plates)[..., None], rgb * 0.5, rgb)
    glow = np.exp(-((xx - 240 - 6 * np.sin(yy / TILE_H * TAU * 4)) / 5.0) ** 2)    # the slag channel down the middle
    rgb = rgb * (1 - 0.5 * glow[..., None]) + ORANGE * 0.85 * glow[..., None]
    for x in (20, 460):                                      # the runner rails by the walls
        rgb = lines_along(rgb, [x], 2.0, (96, 84, 72), 0.9)
    warm = 0.08 + 0.1 * pn(W, TILE_H, 120, 3345, 2)
    rgb = rgb + ORANGE * warm[..., None] * 0.6               # the furnaces' orange light over everything
    return finish(rgb, 36, amount=0.06)


TILES = {"tunnel-floor": tunnel_floor, "fork-drift-seam": fork_drift_seam, "pumping-hall": pumping_hall,
         "fork-ducts-chute": fork_ducts_chute, "furnace-floor": furnace_floor}


# --------------------------------------------------------------------------- the walls (CanyonProfile convention)

STEPS = (((40, 36, 33), (70, 63, 56)),       # 0 the dark lower face (soot, the red lights' housings near the foot)
         ((42, 40, 38), (62, 58, 54)),       # 1 the cable tray ledge
         ((74, 66, 58), (126, 114, 100)),    # 2 the main drill-scored face with the lamp string
         ((38, 35, 32), (56, 51, 46)),       # 3 a ledge
         ((38, 35, 32), (60, 55, 50)),       # 4 the upper face going into the dark
         ((14, 13, 13), (24, 22, 21)))       # 5 the rock overhead (unlit)
LAMP_EVERY = 128
RED_EVERY = 64
LAMP_COL_SEG = 2


def _starts():
    return np.concatenate([[0], np.cumsum(b25.WIDTHS)])[:-1]


def lamp_points(th):
    c0 = _starts()[LAMP_COL_SEG]
    return [(c0 + 9, y) for y in range(LAMP_EVERY // 2, th, LAMP_EVERY)]


def red_points(th):
    return [(3, y) for y in range(RED_EVERY // 4, th, RED_EVERY)]


def cut_rock_rgb(th=TEX_PERIOD, seed=3351):
    """Cut rock in the wall's six steps: blocks with dark cracks and a lit bevel, drill-score grooves on the faces, a
    cable tray, the lamp string's cable sagging between brackets, red light housings on the lower face."""
    yy, xx, G, T = b25._wall_fields(th)
    lo = np.array([c[0] for c in STEPS], float)[G]
    hi = np.array([c[1] for c in STEPS], float)[G]
    rgb = lo + (hi - lo) * (0.35 + 0.3 * T)[..., None]
    fine = pn(TEX_W, th, 8, seed, 3)
    mid = pn(TEX_W, th, 32, seed + 1, 3)
    face = np.isin(G, [0, 2, 4])
    f1, f2, ids = b25._worley(th, TEX_W, 10, 32, seed + 4)
    rnd = np.random.default_rng(seed + 5).uniform(0.78, 1.18, ids.max() + 1)[ids]
    crack = np.clip(1 - (f2 - f1) / 0.12, 0, 1)
    gy, gx = np.gradient(f1)
    bevel = np.clip(-(gx * 3 + gy * 1.2) * 8, -1, 1) * (f1 > 0.25)
    blk = rnd * (1 + 0.12 * bevel) * (0.9 + 0.2 * mid) * (0.94 + 0.12 * fine)
    rgb = np.where(face[..., None], rgb * blk[..., None] * (1 - 0.55 * crack)[..., None], rgb)
    score = face & (np.abs(((yy + 6 * np.sin(xx / 7.0 + yy / 90.0)) % 16) - 8) < 0.8)     # drill scoring
    rgb = np.where(score[..., None], rgb * 0.7, rgb)
    rgb = np.where((~face)[..., None], rgb * (0.85 + 0.3 * fine)[..., None], rgb)
    s = _starts()
    tray = (G == 1) & (T > 0.2) & (T < 0.8)                    # the cable tray: a steel channel, three cables
    rgb = np.where(tray[..., None], np.array((58, 60, 62.0)) * (0.9 + 0.2 * fine)[..., None], rgb)
    for c in (0.35, 0.5, 0.65):
        cab = (G == 1) & (np.abs(T - c) < 0.05)
        rgb = np.where(cab[..., None], np.array((20, 18, 18.0)), rgb)
    c0 = s[LAMP_COL_SEG] + 9
    sag = 3.5 * (1 - np.cos(TAU * ((yy - LAMP_EVERY // 2) % LAMP_EVERY) / LAMP_EVERY)) / 2
    cable = np.abs(xx - (c0 + sag)) < 0.9
    rgb = np.where(cable[..., None], np.array((18, 16, 16.0)), rgb)
    for x, y in lamp_points(th):                               # lamp brackets: a dark housing, a pale lens
        for dy in (-th, 0, th):
            box = (np.abs(xx - x) < 3) & (np.abs(yy - y - dy) < 3.5)
            rgb = np.where(box[..., None], np.array((40, 42, 46.0)), rgb)
            lens = (np.abs(xx - x) < 1.5) & (np.abs(yy - y - dy) < 2)
            rgb = np.where(lens[..., None], np.array((226, 232, 236.0)), rgb)
    for x, y in red_points(th):                                # the red running lights' housings by the foot
        for dy in (-th, 0, th):
            box = (np.abs(xx - x) < 2.5) & (np.abs(yy - y - dy) < 2.5)
            rgb = np.where(box[..., None], np.array((36, 30, 30.0)), rgb)
            lens = (np.abs(xx - x) < 1.2) & (np.abs(yy - y - dy) < 1.2)
            rgb = np.where(lens[..., None], np.array((200, 40, 30.0)), rgb)
    return np.clip(rgb, 0, 255)


def cut_rock_glow(th=TEX_PERIOD):
    yy, xx, G, T = b25._wall_fields(th)
    f_w = np.zeros((th, TEX_W))
    f_r = np.zeros((th, TEX_W))
    for x, y in lamp_points(th):
        for dy in (-th, 0, th):
            d = np.hypot(xx - x, (yy - y - dy) * 0.8)
            f_w = np.maximum(f_w, np.clip(1 - d / 2.2, 0, 1) + 0.45 * np.clip(1 - d / 5, 0, 1)
                             + 0.26 * np.clip(1 - d / 28, 0, 1))
    for x, y in red_points(th):
        for dy in (-th, 0, th):
            d = np.hypot(xx - x, yy - y - dy)
            f_r = np.maximum(f_r, np.clip(1 - d / 1.6, 0, 1) + 0.4 * np.clip(1 - d / 4, 0, 1))
    w = artkit.ordered_dither(np.clip(f_w, 0, 1), 6)
    r = artkit.ordered_dither(np.clip(f_r, 0, 1), 5)
    rgb = np.maximum(LAMP * w[..., None] * 0.9, RED * r[..., None])
    return rgb


def ice_wall():
    return finish(padded(cut_rock_rgb()), 32, amount=0.04)


def ice_wall_glow():
    rgb = padded(cut_rock_glow())
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def ice_wall_foot():
    tw, th = b16.FOOT_TEX
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    n = pn(tw, th, 8, 3361, 2)
    rng = np.random.default_rng(3363)
    peb = np.zeros((th, tw))
    for _ in range(320):
        px, py = abs(rng.normal(0, 7)), rng.uniform(0, th)
        r = rng.uniform(0.8, 2.6) * (1.3 - px / 30)
        dy = (yy - py + th / 2) % th - th / 2
        peb = np.maximum(peb, np.clip(1 - np.hypot(xx - px, dy) / max(r, 0.5), 0, 1) * r)
    hgt = 2.2 * peb + 0.8 * n + 3 * np.clip(1 - xx / 12, 0, 1)
    t = np.clip(0.45 + 0.35 * (n - 0.5) + 0.14 * peb, 0, 1)
    alb = ramp([(34, 31, 28), (56, 50, 45), (82, 74, 66), (108, 98, 86)], t)
    rgb = b16.lit_up(hgt, alb)
    rgb[:, 0:2] = np.array((10, 9, 9), float)
    rgb[:, 2:4] = ramp([(96, 88, 78), (124, 114, 100)], n[:, 2:4])
    cover = np.clip(1.25 - xx / 18, 0, 1)
    cover = np.maximum(cover, (peb > 0.4) * np.clip(1.4 - xx / 26, 0, 1))
    return finish(rgb, 20, dither_alpha(cover, 2), amount=0.04)


def island_face(seed):
    def make():
        return finish(padded(cut_rock_rgb(ISLAND_PERIOD, seed), FACE_PAD), 28, amount=0.04)
    return make


def island_top(look, seed):
    """An island's cap at 1.25: dark rock (the rock overhead seen down the shaft), a few cable bundles, a lit lip
    toward the upper left; sliced in parts of at most 1,000 rows, far end first."""
    k = b16.PLATEAU_K
    poly = dict(ISLANDS)[look]
    x0, x1 = min(p[0] for p in poly), max(p[0] for p in poly)
    s0, s1 = min(p[1] for p in poly), max(p[1] for p in poly)
    w, h = math.ceil((x1 - x0) * k), math.ceil((s1 - s0) * k)
    cap = b16.island_cap(look)
    cover = b16.polygon_mask([tuple(p) for p in cap], w, h, x0, s1, k)
    cover = np.array(Image.fromarray((cover * 255).astype(np.uint8)).filter(
        ImageFilter.MaxFilter(3)), float) / 255
    n = raster.fbm(w, h, 12, seed, octaves=4, period=False)
    alb = ramp([(14, 13, 13), (24, 22, 21), (34, 31, 29)], n)
    yy, xx = grid(h, w)
    for cx in (w * 0.4, w * 0.62):                          # cable bundles along the island's length
        cab = np.abs(xx - cx - 3 * np.sin(yy / 140 + cx)) < 1.4
        alb = np.where(cab[..., None], np.array((8, 8, 8.0)), alb)
    inner = np.array(Image.fromarray((cover * 255).astype(np.uint8)).filter(ImageFilter.MinFilter(5)), float) / 255
    edge = np.clip(cover - inner, 0, 1)
    gy, gx = np.gradient(inner)
    facing = -(gx + gy)
    lip = np.where(facing[..., None] > 0, np.array((86, 78, 70), float), np.array((40, 36, 33), float))
    rgb = np.where((edge > 0.15)[..., None], lip, alb)
    img = finish(rgb, 20, np.where(cover > 0.5, 255, 0), amount=0.02)
    parts = max(1, math.ceil(h / 1000))
    cut = [round(h * i / parts) for i in range(parts + 1)]
    return [img.crop((0, cut[i], w, cut[i + 1])) for i in range(parts)]


WALLS = {"ice-wall": ice_wall, "ice-wall-glow": ice_wall_glow, "ice-wall-foot": ice_wall_foot,
         "drift-rock-face": island_face(3371), "duct-rock-face": island_face(3373)}


# --------------------------------------------------------------------------- pieces

def glow_img(rgb):
    rgb = np.clip(rgb, 0, 255)
    alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def lights(h, w, pts, col, core=1.8, halo=6.0, wash=0.0, wash_r=24.0, levels=6):
    yy, xx = grid(h, w)
    f = np.zeros((h, w))
    for x, y in pts:
        d = np.hypot(xx - x, yy - y)
        f = np.maximum(f, np.clip(1 - d / core, 0, 1) + 0.4 * np.clip(1 - d / halo, 0, 1)
                       + wash * np.clip(1 - d / wash_r, 0, 1))
    v = artkit.ordered_dither(np.clip(f, 0, 1), levels)
    return np.asarray(col, float)[None, None] * v[..., None]


def clear_border(img):
    a = np.array(img)
    a[0, :, 3] = a[-1, :, 3] = 0
    a[:, 0, 3] = a[:, -1, 3] = 0
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


def shaft_mouth():
    """Layer rows 0..2039 (terrain -540..1499): Kleopatra's sunlit regolith round the shaft's lip; inside the walls'
    feet the shaft floor in shadow, darker with depth; two gantries over the lip; the top fades into tunnel-floor."""
    h, bottom = MAX_PIECE, 0
    yy, xx = grid(h)
    lrows = bottom + h - 1 - yy
    trows = lrows - SCREEN
    floor = floor_rows(bottom, h)[..., :3].astype(float)
    n = raster.fbm(W, h, 48, 3383, octaves=4, period=False)
    fine = raster.fbm(W, h, 6, 3384, octaves=2, period=False)
    surf = warm_rock(np.clip(0.6 + 0.35 * (n - 0.5) + 0.2 * (fine - 0.5), 0, 1))
    craters = np.zeros((h, W))
    rng = np.random.default_rng(3385)
    for _ in range(14):
        cx, cy, r = rng.uniform(0, W), rng.uniform(0, 900), rng.uniform(8, 26)
        d = np.hypot(xx - cx, yy - (h - 1 - cy))
        craters += np.clip(1 - np.abs(d - r) / 3, 0, 1) * 0.35 - np.clip(1 - d / r, 0, 1) * 0.3
    surf = surf * (1.18 + 0.4 * craters[..., None])           # sunlit: a step lighter than the tunnels
    xl, xr = foot(0, trows[:, 0]), foot(1, trows[:, 0])
    inside = np.isnan(xl)[:, None] | ((xx > np.nan_to_num(xl, nan=-1)[:, None])
                                      & (xx < np.nan_to_num(xr, nan=W + 1)[:, None]))
    lip_row = 380.0 + 26 * np.sin(xx / 53) + 14 * (n - 0.5) * 2          # terrain row of the shaft's lip
    into = trows - lip_row
    depth = np.clip(into / 900, 0, 1)
    shadow = floor * (0.95 - 0.45 * depth[..., None] ** 0.6) + surf * 0.25 * np.clip(1 - into / 120, 0, 1)[..., None]
    rgb = np.where(((into > 0) & inside)[..., None], shadow, surf)
    rgb = np.where((~inside & (trows > 520))[..., None], surf * 0.8, rgb)
    edge = (into > -3) & (into < 3) & inside
    rgb = np.where(edge[..., None], np.array((150, 136, 114.0)), rgb)
    rgb = np.where(((into > 3) & (into < 26) & inside)[..., None], rgb * 0.6, rgb)
    img = Image.fromarray(np.clip(rgb, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    d = ImageDraw.Draw(img)
    for r_t, x0, x1 in ((300, 30, 300), (180, 210, 470)):    # the gantries over the lip (terrain rows)
        y = h - 1 - (r_t + SCREEN - bottom)
        d.rectangle([x0, y - 7, x1, y + 7], fill=(52, 54, 58, 255))
        d.line([x0, y - 7, x1, y - 7], fill=(126, 126, 120, 255))
        for x in range(x0 + 6, x1, 14):
            d.line([x, y - 6, x + 8, y + 6], fill=(34, 34, 38, 255))
        for x in (x0 + 4, x1 - 4):
            d.rectangle([x - 4, y - 10, x + 4, y + 10], fill=(70, 70, 74, 255))
    rgb = np.array(img)[..., :3].astype(float)
    k = np.arange(h)[:, None]
    fade = np.clip((k + 0.5) / FEATHER, 0, 1)
    alpha = dither_alpha(np.broadcast_to(fade, (h, W)).copy(), 2)
    glow = lights(h, W, [(36, h - 1 - (300 + SCREEN)), (296, h - 1 - (300 + SCREEN)), (214, h - 1 - (180 + SCREEN)),
                         (466, h - 1 - (180 + SCREEN))], RED, wash=0.0)
    return posterize(rgb, 40, alpha), glow_img(glow), bottom


def seam_piece(index, seed, h=320):
    lower, upper = SECTION_TILES[index - 1], SECTION_TILES[index]
    seam = seam_row(index)
    bottom = int(round(seam)) - h // 2
    lo = tile_rows(lower, bottom, h)[..., :3].astype(float)
    up = tile_rows(upper, bottom, h)[..., :3].astype(float)
    yy, xx = grid(h)
    n = raster.fbm(W, h, 40, seed, octaves=4, period=False)
    line = h / 2 + 34 * np.sin(xx / 83 + seed) + 30 * (n - 0.5) * 2
    rgb = np.where((yy < line)[..., None], up, lo)
    return posterize(rgb, 40, feather_alpha(h, W)), bottom


MOUTH = (160, 128)


def mouth(kind, seed):
    """A channel's mouth at an island's tip, 160 x 128 (the channel's 155 px floor): richer (QE-2 a): two jamb blocks
    with an open bulkhead's lit frame across the floor, a threshold strip, red running lights along both sides;
    safer: two white work lamps on tripod stands, the consortium's faded hazard stripes on the floor."""
    w, h = MOUTH
    yy, xx = grid(h, w)
    n = raster.fbm(w, h, 10, seed, octaves=3, period=False)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    pts_light = []
    if kind == "rich":
        for x0 in (6, w - 22):                              # the jambs, charcoal with gold trim
            d.rectangle([x0, 30, x0 + 16, 98], fill=(46, 48, 54, 255), outline=(20, 20, 24, 255))
            d.line([x0 + 1, 31, x0 + 1, 97], fill=(196, 150, 64, 255))
            d.rectangle([x0 + 3, 40, x0 + 13, 88], fill=(32, 34, 38, 255))
            pts_light += [(x0 + 8, 36), (x0 + 8, 92)]
        d.rectangle([22, 58, w - 22, 70], fill=(36, 36, 40, 255))          # the threshold: the bulkhead's sill
        d.line([22, 58, w - 22, 58], fill=(196, 150, 64, 255))
        for x in range(26, w - 22, 12):
            d.rectangle([x, 62, x + 5, 66], fill=(196, 150, 64, 255))
        for y in (8, 118):                                  # red lights on posts down the channel's sides
            for x in (10, w - 10):
                d.rectangle([x - 2, y - 2, x + 2, y + 2], fill=(40, 30, 30, 255))
                pts_light.append((x, y))
        col = RED
    else:
        for x, y in ((18, 64), (w - 18, 64)):               # work lamps on tripods
            for a in (-2.3, -0.9, 1.6):
                d.line([x, y, x + 12 * math.cos(a), y + 12 * math.sin(a)], fill=(70, 66, 58, 255), width=2)
            d.ellipse([x - 5, y - 5, x + 5, y + 5], fill=(60, 60, 62, 255), outline=(24, 24, 26, 255))
            pts_light.append((x, y - 1))
        for k in range(6):                                  # faded hazard stripes across the floor
            x = 30 + k * 18
            d.polygon([(x, 54), (x + 10, 54), (x + 18, 74), (x + 8, 74)], fill=(150, 124, 52, 150))
        col = LAMP
    a = np.array(img).astype(float)
    a[..., :3] *= (0.85 + 0.3 * n)[..., None]
    body = clear_border(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"))
    body = posterize(np.array(body)[..., :3], 24, np.array(body)[..., 3])
    glow = lights(h, w, pts_light, col, core=1.8, halo=5.0, wash=0.22 if kind == "safe" else 0.1, wash_r=26)
    return body, clear_border(glow_img(glow))


def pump(seed=3391):
    w = h = 112
    yy, xx = grid(h, w)
    r = np.hypot(xx - 56, yy - 56)
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([14, 14, 98, 98], fill=(54, 56, 58, 255), outline=(22, 22, 24, 255))
    d.ellipse([26, 26, 86, 86], fill=(76, 80, 84, 255), outline=(30, 30, 32, 255))
    d.ellipse([38, 38, 74, 74], fill=(46, 48, 52, 255))
    for a in np.linspace(0, TAU, 8, endpoint=False):
        d.ellipse([56 + 25 * math.cos(a) - 2, 56 + 25 * math.sin(a) - 2, 56 + 25 * math.cos(a) + 2,
                   56 + 25 * math.sin(a) + 2], fill=(120, 118, 110, 255))
    d.rectangle([0, 50, 14, 62], fill=(84, 88, 92, 255))
    d.rectangle([98, 50, 112, 62], fill=(84, 88, 92, 255))
    d.line([15, 15, 97, 15], fill=(120, 120, 116, 255))
    d.line([15, 15, 15, 97], fill=(110, 110, 106, 255))
    a = np.array(img).astype(float)
    shade = np.clip(1.15 - (xx + yy) / 260, 0.7, 1.15)
    a[..., :3] *= shade[..., None]
    body = clear_border(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"))
    glow = lights(h, w, [(20, 20), (92, 92)], (120, 255, 140), core=1.4, halo=3.5)
    return posterize(np.array(body)[..., :3], 24, np.array(body)[..., 3]), clear_border(glow_img(glow))


def refinery_gate():
    w, h = 420, 72
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([2, 26, w - 3, 46], fill=(40, 40, 44, 255))
    for k in range(0, w - 20, 22):
        d.polygon([(k + 6, 26), (k + 16, 26), (k + 26, 46), (k + 16, 46)], fill=(170, 134, 46, 255))
    d.line([2, 26, w - 3, 26], fill=(130, 126, 116, 255))
    for x0 in (4, w - 28):
        d.rectangle([x0, 6, x0 + 24, 66], fill=(52, 52, 58, 255), outline=(20, 20, 22, 255))
        d.line([x0 + 1, 7, x0 + 1, 65], fill=(196, 150, 64, 255))
    body = clear_border(img)
    glow = lights(h, w, [(16, 10), (16, 62), (w - 16, 10), (w - 16, 62)], RED)
    return posterize(np.array(body)[..., :3], 24, np.array(body)[..., 3]), clear_border(glow_img(glow))


def crucible():
    w = h = 144
    yy, xx = grid(h, w)
    r = np.hypot(xx - 72, yy - 72)
    rgb = np.zeros((h, w, 3)) + np.array((50, 44, 42.0))
    shade = np.clip(1.2 - (xx + yy) / 300, 0.6, 1.2)
    rgb = rgb * shade[..., None]
    rim = np.abs(r - 50) < 6
    rgb = np.where(rim[..., None], np.array((84, 74, 66.0)) * shade[..., None], rgb)
    melt = r < 44
    n = raster.fbm(w, h, 10, 3401, octaves=3, period=False)
    hot = ramp([(150, 40, 10), (255, 120, 30), (255, 210, 120)], np.clip(n * 1.2 - r / 160, 0, 1))
    rgb = np.where(melt[..., None], hot * 0.75, rgb)
    alpha = np.where(r < 60, 255, 0)
    for a in (0.6, 2.2, 3.8, 5.3):                          # the cradle's four arms
        arm = (np.abs((xx - 72) * math.sin(a) - (yy - 72) * math.cos(a)) < 4) & (r > 50) & (r < 70)
        rgb = np.where(arm[..., None], np.array((66, 62, 60.0)), rgb)
        alpha = np.where(arm, 255, alpha)
    body = clear_border(posterize(rgb, 32, alpha))
    g = np.clip(1 - r / 46, 0, 1) * 0.9 + np.clip(1 - r / 70, 0, 1) * 0.25
    glow = ORANGE[None, None] * artkit.ordered_dither(np.clip(g, 0, 1), 6)[..., None]
    return body, clear_border(glow_img(glow))


def gantry():
    w, h = 420, 88
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 20, w - 5, 68], fill=(46, 44, 46, 255), outline=(18, 18, 20, 255))
    for x in range(10, w - 10, 10):
        d.line([x, 22, x, 66], fill=(34, 32, 34, 255))
    d.line([4, 20, w - 5, 20], fill=(120, 114, 106, 255))
    d.line([4, 68, w - 5, 68], fill=(26, 24, 24, 255))
    for x in (8, 140, 280, w - 12):                          # the legs' feet
        d.rectangle([x - 6, 10, x + 6, 78], fill=(56, 54, 56, 255), outline=(18, 18, 20, 255))
    body = clear_border(img)
    glow = lights(h, w, [(8, 14), (140, 14), (280, 14), (w - 12, 14), (8, 74), (w - 12, 74)], RED)
    return posterize(np.array(body)[..., :3], 24, np.array(body)[..., 3]), clear_border(glow_img(glow))


def belt_space(h, seed, band=1.0):
    yy, xx = grid(h)
    base = np.zeros((h, W, 3)) + np.array((10, 10, 16.0))
    phase = (xx * 0.55 - yy) % 900.0
    dist = np.minimum(phase, 900.0 - phase) - 160
    dust = np.exp(-(dist / 150.0) ** 2) * (0.55 + 0.6 * raster.fbm(W, h, 64, seed, octaves=4, period=False))
    base += dust[..., None] * np.array((92, 74, 58.0)) * band
    stars = np.array(raster.starfield(W, h, seed + 1, density=0.0022)).astype(np.float64)[..., :3]
    return np.maximum(base, stars * (1 - 0.5 * dust[..., None]) * 0.9)


def chute_pieces():
    """The ore chute's end (terrain rows from the walls' last 1,500 to the mouth's lip at ~23,500: two conveyors down
    the middle to the lip) and above it belt space to beyond the outro's end."""
    h = MAX_PIECE
    top_lip = 23500.0
    bottom_a = int(round(top_lip + SCREEN - 1500))
    yy, xx = grid(h)
    lrows = bottom_a + h - 1 - yy
    trows = lrows - SCREEN
    floor = floor_rows(bottom_a, h)[..., :3].astype(float)
    space = belt_space(h, 3411)
    n = raster.fbm(W, h, 40, 3413, octaves=4, period=False)
    lip = top_lip + 18 * np.sin(xx / 47) + 22 * (n - 0.5) * 2
    beyond = trows > lip
    rgb = floor.copy()
    for x0, x1 in ((180, 226), (254, 300)):                  # the conveyors to the mouth
        belt = (xx >= x0) & (xx < x1) & (trows > 21800)
        rubber = np.array((34, 32, 32.0)) * (0.9 + 0.2 * (np.abs((lrows % 10) - 5) < 1))[..., None]
        rgb = np.where(belt[..., None], rubber, rgb)
        edge = ((np.abs(xx - x0) < 1.5) | (np.abs(xx - x1 + 1) < 1.5)) & (trows > 21800)
        rgb = np.where(edge[..., None], np.array((150, 128, 70.0)), rgb)
    near = (~beyond) & (trows > lip - 40)
    rgb = np.where(near[..., None], rgb * (0.55 + 0.45 * np.clip((lip - trows) / 40, 0, 1))[..., None], rgb)
    rgb = np.where(((trows > lip - 3) & ~beyond)[..., None], np.array((150, 132, 108.0)), rgb)
    rgb = np.where(beyond[..., None], space, rgb)
    k = np.arange(h)[:, None]
    fade = np.clip((h - k - 0.5) / FEATHER, 0, 1)
    alpha = dither_alpha(np.broadcast_to(fade, (h, W)).copy(), 2)
    a = posterize(rgb, 40, alpha)
    b = Image.fromarray(np.dstack([np.clip(belt_space(h, 3415), 0, 255),
                                   np.full((h, W), 255)]).astype(np.uint8), "RGBA")
    b = posterize(np.array(b)[..., :3], 24)
    return (a, bottom_a), (b, bottom_a + h)


GALLERY_RIM, GALLERY_SPILL = 16, 28


def gallery_mouth(side, depth, seed=3421, width=NOTCH_W):
    """A notch seen from above (left wall: rock left, the tunnel right): a square-cut recess ``depth`` px into the
    rock and ``width`` rows long, its floor steel-lined, darkening toward the back, the cut faces lit where they face
    the upper left, two red lamps at the mouth's corners, the rock rim dithering out into the projected wall."""
    w = depth + GALLERY_RIM + GALLERY_SPILL
    h = width + 32
    fc = depth + GALLERY_RIM if side == 0 else GALLERY_SPILL
    yy, xx = grid(h, w)
    yy, xx = yy + 0.5, xx + 0.5
    into = (fc - xx) if side == 0 else (xx - fc)
    dv = yy - h / 2
    half = width / 2
    n = raster.fbm(w, h, 8, seed + side + depth, octaves=3, period=False)
    hole = (into > -0.5) & (into < depth) & (np.abs(dv) < half)
    k = np.clip(into / max(depth, 1), 0, 1)
    deck = np.array((58, 60, 62.0)) * (0.85 + 0.25 * n)[..., None]
    plate = (np.abs((yy % 20) - 10) < 0.6)
    deck = np.where(plate[..., None], deck * 0.6, deck)
    rgb = deck * (1 - 0.65 * k ** 0.8)[..., None]
    band = hole & ((half - np.abs(dv) < 3) | (depth - into < 3))
    lit = (dv > 0) | ((side == 1) & (depth - into < 3))
    rgb = np.where(band[..., None], np.where(lit[..., None], np.array((96, 88, 78.0)), np.array((30, 28, 26.0))),
                   rgb)
    ex = np.hypot(np.maximum(np.abs(dv) - half, 0), np.maximum(into - depth, 0))
    reach = 7 + 6 * n
    rim = ~hole & (into > -0.5) & (ex < reach)
    rcol = np.array((58, 52, 46.0)) * (0.7 + 0.5 * n)[..., None] * (0.7 if side == 0 else 1.0)
    rgb = np.where(rim[..., None], rcol, rgb)
    out_ = (into <= -0.5) & (into > -GALLERY_SPILL + 6) & (np.abs(dv) < half - 6) & (n > 0.55 - 0.02 * into)
    rgb = np.where(out_[..., None], np.array((70, 64, 56.0)) * (0.8 + 0.3 * n)[..., None], rgb)
    cover = np.where(hole, 1.0, 0.0)
    cover = np.maximum(cover, rim * np.clip((reach - ex) / 5, 0, 1))
    cover = np.maximum(cover, out_ * 1.0)
    alpha = np.where(hole, 255, dither_alpha(np.clip(cover, 0, 1), 2))
    for sy in (-1, 1):                                     # the red lamps on the mouth's corners
        y = int(h / 2 + sy * (half - 4))
        x = int(fc + (-3 if side == 0 else 2))
        rgb[y - 1:y + 2, x - 1:x + 2] = (200, 40, 30)
    return clear_border(posterize(rgb, 28, alpha))


def gallery_names():
    out = []
    for nt in NOTCHES:
        name = f"gallery-{nt['side']}-{int(round(nt['depth']))}"
        if name not in out:
            out.append(name)
    return out


# --------------------------------------------------------------------------- build

def _job(name):
    if name in TILES:
        return TILES[name]()
    return WALLS[name]()


META = None


def target_centre(t, h):
    """The ground layer row of a ground target's centre (its bottom edge at the top edge at t)."""
    return scroll(t) + SCREEN + h / 2


def place_t(centre_row):
    return time_at(centre_row - SCREEN / 2)


def exact_t(bottom, h):
    t = place_t(bottom + h / 2)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def build():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in list(OUT.glob("*.png")):
        old.unlink()
    names = list(TILES) + list(WALLS)
    with ProcessPoolExecutor() as ex:
        imgs = list(ex.map(_job, names))
    for name, img in zip(names, imgs):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    for look, seed in (("drift-rock", 3375), ("duct-rock", 3377)):
        for i, img in enumerate(island_top(look, seed)):
            artkit.save_png(img, OUT / f"{look}-top_{i}.png", SOURCE)
    tile.cache_clear()
    pieces, placed = {}, []

    def put(name, img, note, t, x, glow=None):
        if name not in pieces:
            artkit.save_png(img, OUT / f"{name}.png", SOURCE)
            pieces[name] = {"layer": "ground", "size": [img.width, img.height]}
            if glow is not None:
                artkit.save_png(glow, OUT / f"{name}-glow.png", SOURCE)
                pieces[f"{name}-glow"] = {"layer": "ground", "size": [glow.width, glow.height]}
        if t is not None:
            placed.append((note, name, t, x))

    img, glow, bottom = shaft_mouth()
    put("shaft-mouth", img, "1. Kleopatra's sunlit face round the shaft's lip, the gantries; exact rows",
        exact_t(bottom, img.height), 240, glow)
    for i in range(1, len(SECTIONS)):
        if SECTION_TILES[i] == SECTION_TILES[i - 1]:
            continue
        img, bottom = seam_piece(i, 3430 + i)
        put(f"seam-{SECTION_TILES[i]}", img, f"section {i + 1}'s seam (ground {seam_row(i):.0f}): "
            f"{SECTION_TILES[i - 1]} into {SECTION_TILES[i]}; exact rows", exact_t(bottom, img.height), 240)
    for k, (rich_x, safe_x, rich, safe) in enumerate(((392, 88, "seam-mouth", "drift-mouth"),
                                                      (88, 392, "ducts-mouth", "conveyor-mouth"))):
        r0 = island_rows(k)[0]
        centre_layer = r0 + 150 + SCREEN                       # just past the tip: on screen when the tip reaches the ship
        for name, x, kind in ((rich, rich_x, "rich"), (safe, safe_x, "safe")):
            body, gl = mouth(kind, 3440 + k * 2 + (kind == "rich"))
            bottom = int(round(centre_layer - MOUTH[1] / 2))
            put(name, body, f"fork {k + 1}'s {'richer' if kind == 'rich' else 'safer'} channel's mouth at the "
                f"island's tip (QE-2 a)", exact_t(bottom, MOUTH[1]), x, gl)
    body, gl = pump()
    for t, x in ((83.0, 112), (95.0, 370), (106.0, 110)):
        bottom = int(round(target_centre(t, 112) - 56))
        put("pump", body, f"a pump of the pumping hall (t={t:g})", exact_t(bottom, 112), x, gl)
    body, gl = refinery_gate()
    bottom = int(round(target_centre(134.6, 24) - 36))
    put("refinery-gate", body, "the refinery's gate (the repair crate's lock on it)", exact_t(bottom, 72), 240, gl)
    body, gl = crucible()
    bottom = int(round(target_centre(150.6, 0) - 72))
    put("crucible", body, "the crucible beside the crucible gallery's Sentinel", exact_t(bottom, 144), 150, gl)
    body, gl = gantry()
    bottom = int(round(target_centre(162.0, 48) - 44))
    put("gantry", body, "the gantry across the furnace floor (the SAM trio on it at t=162)", exact_t(bottom, 88), 240,
        gl)
    (a, ba), (b, bb) = chute_pieces()
    put("chute-end", a, "the ore chute's conveyors to the mouth's lip; exact rows", exact_t(ba, a.height), 240)
    put("chute-space", b, "belt space beyond the chute's mouth, to the outro's end; exact rows",
        exact_t(bb, b.height), 240)
    for name in gallery_names():
        _, side, depth = name.split("-")
        put(name, gallery_mouth(0 if side == "left" else 1, int(depth)), "", None, None)
    global META
    META = {"pieces": pieces, "placed": [list(p) for p in placed]}
    print(f"wrote {len(TILES)} tile sets, {len(WALLS)} wall/island textures, the island caps and {len(pieces)} "
          f"pieces to {OUT.relative_to(ROOT)}")


# --------------------------------------------------------------------------- proposal

DESC = {
    "tunnel-floor": "sections 1-2: cut rock, dust, old track grooves, a cable run",
    "fork-drift-seam": "section 3: the old drift left (cold, rails, bore holes), the charge seam right (fresh cuts)",
    "pumping-hall": "section 4: the hall's plates, pipe runs, drains",
    "fork-ducts-chute": "section 5: the cooling ducts left, the ore chute's conveyors right",
    "furnace-floor": "section 6: cast plates, the slag channel, the furnaces' orange light",
}


def meta():
    if META is not None:
        return META
    block = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"]
    return {"pieces": block["pieces"], "placed": [["", p["piece"], p["t"], p["x"]] for p in block["placed"]]}


def _pt(p):
    return "[" + ", ".join(f"{v:g}" if isinstance(v, float) else str(v) for v in p) + "]"


def terrain_block():
    src = "the level's data.yaml" if FROM_DATA else "ART-E's proposal (the README's wall table, QE-1 a)"
    lines = [f"# terrain this art is drawn for ({src}); E2: adopt it or re-run the generator after changing it",
             "terrain:", "  left:"]
    for p in LEFT:
        lines.append(f"    - {_pt(p)}   # t={time_at(p[0]):.1f} at the top edge")
    lines.append("  right:")
    for p in RIGHT:
        lines.append(f"    - {_pt(p)}")
    lines.append("  islands:")
    for k, (look, poly) in enumerate(ISLANDS):
        r0, r1 = island_rows(k)
        lines += [f"    - name: fork {k + 1}   # tip row {r0:g} (reaches the ship's row at t~{time_at(r0 + 440):.2f}, "
                  f"the bottom edge at t={latest_choice(k):.2f}); channels 10-165 and 315-470 (155 px)",
                  f"      look: {look}",
                  "      points: [" + ", ".join(_pt(p[:2]) for p in poly) + "]"]
    lines.append("  forks: [{island: 0}, {island: 1}]   # fork 1: left the old drift (safer), right the charge seam; "
                 "fork 2: left the cooling ducts (richer), right the ore chute")
    lines.append("  notches:   # side, scroll = the terrain row of the notch's middle, width along the wall, depth into it")
    for nt in NOTCHES:
        lines.append(f"    - {{side: {nt['side']}, scroll: {int(round(nt['scroll']))}, width: {int(nt['width'])}, "
                     f"depth: {int(round(nt['depth']))}}}   # {nt['why']}; t={time_at(nt['scroll']):.1f} at the top edge")
    lines.append("  contact: {damage: {easy: 6, medium: 10, hard: 10}, push: 28, push_seconds: 0.12, cooldown: 0.5}")
    lines.append("# the notches' mouths: gallery-<side>-<depth> (backdrop_l32.py, not placed; E4 draws one per notch of that")
    lines.append(f"# side and depth over the wall: the foot line at column depth + {GALLERY_RIM} (left) / {GALLERY_SPILL} "
                 "(right), the notch's middle on the image's middle row)")
    lines.append("# ground targets this art is laid out for (t at the top edge, x; QE-1 a: the emitters and the locks")
    lines.append("# at least 40 px inside the field; the branch items after their fork's latest choice and while")
    lines.append(f"# its island is on screen: fork 1 t {latest_choice(0):.2f}-{time_at(island_rows(0)[1]):.1f}, "
                 f"fork 2 t {latest_choice(1):.2f}-{time_at(island_rows(1)[1]):.1f}):")
    lines.append("proposed_targets:")
    for kind, t, x, note in TARGETS:
        lines.append(f"  - {{what: '{kind}', t: {t:g}, x: {x}}}   # {note}")
    return "\n".join(lines) + "\n"


def proposal_block():
    m = meta()
    src = "the level's data.yaml" if DATA else "the README and ART-E's terrain proposal (no data.yaml yet: re-run after E2)"
    lines = ["backdrop:",
             f"  # Presentation only: Level 32's tunnels (tools/art/backdrop_l32.py, step ART-E, from {src}).",
             "  # No deep layer (the ground is opaque; the walls' rims project beyond the field's edges). The walls and the",
             "  # islands are the terrain's (WallProjection's ice path, CanyonProfile's convention): ice-wall (cut rock",
             "  # with the lamp strings), ice-wall-glow (the lamps and the red running lights, additive), ice-wall-foot,",
             "  # drift-rock-face/-top_0..2 and duct-rock-face/-top_0..2 (the islands' looks). A piece's -glow is added",
             "  # over it at its placement. gallery-<side>-<depth> are drawn by the game at the terrain's notches (E4).",
             "  scroll_factors: {far: 0.5, ground: 1, low-air: 1.35, high-air: 2.2}   # far unused, given as the loader asks",
             "  ramp: 2",
             "  haze_colour: 120e0a  # the refinery's smoke",
             "  atmosphere:",
             "    clear: {haze: 0.0}",
             "  tile_sets:"]
    for name in TILES:
        lines.append(f"    {name}: {{layer: ground, height: {TILE_H}}}   # {DESC[name]}")
    lines.append("  pieces:")
    for name, spec in m["pieces"].items():
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]}}")
    lines.append("  placed:")
    for note, piece, t, x in m["placed"]:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {piece}, t: {t}, x: {x}}}")
    lines.append("# sections' tiles: " + "; ".join(f"{i + 1}: [{t}]" for i, t in enumerate(SECTION_TILES)))
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_block() + "\n" + terrain_block(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# --------------------------------------------------------------------------- composite (the walls projected)

def piece_frames(piece):
    return [np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))]


def placed_pieces():
    out = []
    for note, piece, t, x in meta()["placed"]:
        imgs = piece_frames(piece)
        h, w = imgs[0].shape[:2]
        bottom = int(round(scroll(float(t)) + SCREEN / 2 - h / 2))
        glow = OUT / f"{piece}-glow.png"
        g = np.array(Image.open(glow).convert("RGBA")) if glow.exists() else None
        out.append((piece, bottom, int(round(float(x) - w / 2)), imgs, g))
    return out


_paste_rows = b25._paste_rows


def ground_view(t, pieces, glow):
    base = scroll(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for y in range(SCREEN):
        row = int(math.floor(base + SCREEN - 1 - y))
        out[y] = tile(SECTION_TILES[section_at_row(row)])[TILE_H - 1 - row % TILE_H]
    img = Image.fromarray(out, "RGBA")
    for name, bottom, x0, arrs, g in pieces:
        h = arrs[0].shape[0]
        y_top = int(round(base + SCREEN - 1 - (bottom + h - 1)))
        if y_top >= SCREEN or y_top + h <= 0:
            continue
        _paste_rows(img, arrs[0], x0, y_top)
        if g is not None:
            gl = Image.new("RGBA", (W, SCREEN), (0, 0, 0, 0))
            _paste_rows(gl, g, x0, y_top)
            glow += np.array(gl)[..., :3].astype(float)
    return img


def _bind():
    """backdrop_l25's projection reads its module's clock, walls and folder: point them at this level (in-process)."""
    b25.scroll, b25.foot, b25.wall_height, b25.OUT = scroll, foot, wall_height, OUT
    b25.tile.cache_clear()


def draw_islands(img, t):
    """The islands as WallProjection draws a butte (approx.): backdrop_l25.draw_pillar's rings with our looks."""
    base = scroll(t)
    a = np.array(img)
    yy, xx = np.mgrid[0:SCREEN, 0:W]
    for k, (look, poly) in enumerate(ISLANDS):
        r0, r1 = island_rows(k)
        if not (r0 - SCREEN - 200 < base < r1 + 400):
            continue
        pts, per, nrm, rings, perimeter = b16.island_rings(look)
        face = np.array(Image.open(OUT / f"{look}-face.png").convert("RGBA"))
        for z in b25.ZS:
            s = np.interp(z, b25.NODE_Z, b25.SETBACK_CUM) * ISLAND_SCALE
            kk = 6.0 / (6.0 - 1.2 * z)
            ring = pts + nrm * s
            px = b25.CX + (ring[:, 0] - b25.CX) * kk
            py = b25.CY + ((base - 1 - ring[:, 1]) - b25.CY) * kk
            m = Image.new("L", (W, SCREEN), 0)
            ImageDraw.Draw(m).polygon([(px[i], py[i]) for i in range(len(ring))], fill=255)
            mm = np.array(m) > 0
            if not mm.any():
                continue
            u = int(np.clip(b25._z_col(z), 0, TEX_W - 1))
            vr = (face.shape[0] - 1 - np.mod(base - 1 - yy, ISLAND_PERIOD)).astype(int)
            c = face[vr, u, :3].astype(float)
            mid = b25.CX + (np.mean(pts[:, 0]) - b25.CX) * kk
            sh = np.where(xx < mid, b25._shade(1, z), b25._shade(0, z))
            a[mm, :3] = np.clip(c[mm] * sh[mm][:, None], 0, 255).astype(np.uint8)
            a[mm, 3] = 255
        out = Image.fromarray(a, "RGBA")
        kk = 1.25
        x0 = min(p[0] for p in poly)
        left = b25.CX + (x0 - b25.CX) * kk
        edge = b25.CY + ((base - 1 - r1) - b25.CY) * kk
        i = 0
        while (OUT / f"{look}-top_{i}.png").exists():
            sl = Image.open(OUT / f"{look}-top_{i}.png").convert("RGBA")
            lay = Image.new("RGBA", (W, SCREEN), (0, 0, 0, 0))
            lay.paste(sl, (int(round(left)), int(round(edge))))
            out.alpha_composite(lay)
            edge += sl.height
            i += 1
        a = np.array(out)
    return Image.fromarray(a, "RGBA")


def draw_notches(img, t):
    base = scroll(t)
    for nt in NOTCHES:
        side = 0 if nt["side"] == "left" else 1
        y = base - 1 - nt["scroll"]
        name = f"gallery-{nt['side']}-{int(round(nt['depth']))}"
        if -120 < y < SCREEN + 120 and (OUT / f"{name}.png").exists():
            g = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
            fx = float(foot(side, nt["scroll"]))
            col = int(round(nt["depth"])) + GALLERY_RIM if side == 0 else GALLERY_SPILL
            _paste_rows(img, g, int(round(fx - col)), int(round(y - g.shape[0] / 2)))


def held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def stand_ins(img, t, glow):
    """The ground targets this art assumes, from belt_units_e.py's frames when written (held or in assets)."""
    base = scroll(t)
    looks = {"sentinel-tower": ("sentinel-tower", 80), "blast-door-right": ("blast-door-right", 24),
             "blast-door-left": ("blast-door-left", 24), "blast-door-sealed-right": ("blast-door-sealed-right", 24),
             "repair-crate": ("repair-crate", 24), "mining-charge": ("mining-charge", 24), "sam-nest": ("sam-nest", 48)}
    for kind, tt, x, _ in TARGETS:
        key = kind.split(" ")[0]
        if key not in looks:
            continue
        name, hh = looks[key]
        row = scroll(tt) + hh / 2                     # terrain row of the centre
        y = base - 1 - row
        if not (-80 < y < SCREEN + 80):
            continue
        fr = held(name)
        if fr is None:
            continue
        if key == "sam-nest":
            l23kit.paste_c(img, held("sam-nest-base")[0], x, y)
        l23kit.paste_c(img, fr[0], x, y)
        if key == "sentinel-tower":
            hd = held("sentinel-tower-head")
            if hd is not None:
                l23kit.paste_c(img, hd[8 if x > 240 else 24], x, y)


def composite(t, pieces, ship=True):
    glow = np.zeros((SCREEN, W, 3))
    img = ground_view(t, pieces, glow)
    wall_glow = np.zeros((SCREEN, W, 3))
    img = b25.draw_walls(img, t, wall_glow)
    img = draw_islands(img, t)
    draw_notches(img, t)
    stand_ins(img, t, glow)
    if ship:
        try:
            l23kit.paste_c(img, artkit.load_frames("ship")[2], 240, SCREEN - 100)
        except Exception:
            pass
    a = np.array(img).astype(float)
    a[..., :3] += glow + wall_glow
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


TIMES = [(1.0, "1 KLEOPATRA'S FACE, THE SHAFT'S LIP"), (11.0, "1 THE SHAFT CLOSING"),
         (21.5, "2 THE GALLERIES: THE SENTINEL'S NOTCH"), (40.0, "2 A LAUNCH BAY"),
         (55.4, "3 FORK ONE'S HEAD: RICHER RIGHT"), (62.0, "3 THE TWO DRESSINGS"),
         (77.5, "4 THE HALL: THE SENTINEL PAIR"), (97.5, "4 THE CONSORTIUM STORE"),
         (115.4, "5 FORK TWO'S HEAD: RICHER LEFT"), (124.0, "5 DUCTS AND CONVEYORS"),
         (132.0, "6 THE REFINERY'S GATE"), (147.0, "6 THE CRUCIBLE GALLERY"),
         (158.0, "6 THE GANTRY"), (181.0, "6 THE ORE CHUTE'S MOUTH")]


def review():
    _bind()
    pieces = placed_pieces()
    shots = []
    for t, label in TIMES:
        f = composite(t, pieces)
        e60.caption(f, f"T={t:g}  {label}")
        shots.append(f)
    cols = 7
    small = [s.resize((240, 270), Image.BOX) for s in shots]
    sheet_w = 16 + cols * 244
    rows = -(-len(small) // cols)
    sheet = raster.sheet(sheet_w, 60 + rows * 274 + 300 + 80, "LEVEL 32 DEEP CORE - BACKDROP FINAL (ROUND 65)",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, "THE LEVEL AT 0.5X (THE WALLS AND ISLANDS PROJECTED AS THE GAME DOES, APPROX.; "
                                    "STAND-INS FROM BELT_UNITS_E.PY)", raster.LABEL)
    for i, s in enumerate(small):
        sheet.alpha_composite(s, (16 + (i % cols) * 244, 54 + (i // cols) * 274))
    y = 60 + rows * 274
    raster.draw_text(sheet, 16, y, "QE-2 A, BOTH WAYS: FORK ONE'S HEAD (RICHER RIGHT) AND FORK TWO'S (RICHER LEFT), "
                                   "1X CROPS OF THE MOUTHS; A WALL AT 1X", raster.LABEL)
    crops = [shots[4].crop((0, 0, 480, 280)), shots[8].crop((0, 0, 480, 280))]
    for i, c in enumerate(crops):
        sheet.alpha_composite(c, (16 + i * 488, y + 14))
    wall = shots[2].crop((330, 0, 480, 280))
    sheet.alpha_composite(wall, (16 + 2 * 488, y + 14))
    notes = ["THE WALLS: CUT ROCK, A LAMP EVERY 128 ROWS ON THE MAIN FACE (WHITE), THE RED RUNNING LIGHTS ALONG THE FOOT EVERY 64;",
             "THE NOTCHES (QE-1 A) ONLY AS DEEP AS THE EMITTER'S 40 PX INSET ALLOWS: GALLERY 56, HALL 16, CRUCIBLE 26; BLAST DOORS",
             "FLUSH IN THE WALL. THE FORKS' MOUTHS (QE-2 A): RED LIGHTS AND A LIT BULKHEAD ON THE RICHER, WHITE WORK LAMPS ON THE SAFER."]
    for i, line in enumerate(notes):
        raster.draw_text(sheet, 16, y + 304 + 16 * i, line, raster.LABEL_DIM)
    png = CONCEPT / f"backdrop-final-{ROUND}-a.png"
    sheet.convert("RGB").save(png, optimize=True)
    gif = [composite(50.0 + i * 0.2, pieces).convert("RGB") for i in range(60)]
    size = e60.rig.write_gif(gif, CONCEPT / f"backdrop-final-{ROUND}-a.gif", fps=5, colors=96)
    print(f"review: {png.relative_to(ROOT)}, backdrop-final-{ROUND}-a.gif ({size / 1e6:.1f} MB)")


# --------------------------------------------------------------------------- check

def check():
    """Wraps (BackdropSeamsTest's measure), sizes, the edge rule, the geometry rules, QE-1 a's insets."""
    _bind()
    problems = []
    for name in TILES:
        if b25.wrap_score(tile(name)) > 1.5:
            problems.append(f"{name}: wrap score {b25.wrap_score(tile(name)):.2f} (BackdropSeamsTest's limit 1.5)")
    for name, size in (("ice-wall", (TEX_W, TEX_PERIOD + TEX_PAD)), ("ice-wall-glow", (TEX_W, TEX_PERIOD + TEX_PAD)),
                       ("ice-wall-foot", b16.FOOT_TEX)):
        if Image.open(OUT / f"{name}.png").size != tuple(size):
            problems.append(f"{name}: size {Image.open(OUT / f'{name}.png').size} != {size}")
    for p in OUT.glob("*.png"):
        if Image.open(p).text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{p.name}: no Source chunk")
    m = meta()
    for note, piece, t, x in m["placed"]:
        img = np.array(Image.open(OUT / f"{piece}.png").convert("RGBA"))
        h, w = img.shape[:2]
        bottom = round(scroll(float(t)) + SCREEN / 2 - h / 2)
        x0 = round(float(x) - w / 2)
        for k in range(0, int((END + OUTRO) * 60) + 1):
            tt = k / 60
            y = bottom - round(scroll(tt))
            if 0 < y < SCREEN and img[-1, max(0, -x0):min(w, W - x0), 3].any():
                below = any(q != piece and abs(round(scroll(float(qt)) + SCREEN / 2 +
                                                     np.array(Image.open(OUT / f"{q}.png")).shape[0] / 2) - bottom) < 2
                            for _, q, qt, _ in m["placed"])
                if not below:
                    problems.append(f"{piece} (t={t}): bottom edge opaque on screen at t={tt:.2f}")
                    break
            if 0 < y + h < SCREEN and img[0, max(0, -x0):min(w, W - x0), 3].any():
                covered = any(q != piece and abs(round(scroll(float(qt)) + SCREEN / 2 -
                                                       np.array(Image.open(OUT / f"{q}.png")).shape[0] / 2) - (bottom + h)) < 2
                              for _, q, qt, _ in m["placed"])
                if not covered:
                    problems.append(f"{piece} (t={t}): top edge opaque on screen at t={tt:.2f}")
                    break
            if 0 < x0 < W and img[:, 0, 3].any() and -h < y < SCREEN:
                problems.append(f"{piece}: left edge opaque")
                break
            if 0 < x0 + w < W and img[:, -1, 3].any() and -h < y < SCREEN:
                problems.append(f"{piece}: right edge opaque")
                break
    rows = np.arange(0, 23680, 4.0)
    xl, xr = foot(0, rows), foot(1, rows)
    for r, a, b in zip(rows, xl, xr):
        if np.isnan(a) or np.isnan(b):
            continue
        spans = [(a, b)]
        for k, (look, poly) in enumerate(ISLANDS):
            r0, r1 = island_rows(k)
            if r0 <= r <= r1:
                xs = [p[0] for p in poly]
                spans = [(a, min(xs)), (max(xs), b)]
        if len(spans) == 1 and b - a < 180 - 1e-6:
            problems.append(f"row {r:g}: width {b - a:g} < 180")
            break
        if len(spans) == 2 and min(s[1] - s[0] for s in spans) < 130 - 1e-6:
            problems.append(f"row {r:g}: a channel under 130")
            break
    for pts in (LEFT, RIGHT):
        for p, q in zip(pts, pts[1:]):
            if abs(q[1] - p[1]) / max(1e-9, q[0] - p[0]) > 0.5:
                problems.append(f"wall slope over 0.5 between rows {p[0]} and {q[0]}")
    for kind, t, x, _ in TARGETS:
        half = 14 if kind.startswith("sentinel") else LOCK / 2 if kind.startswith("blast-door") else None
        if half is not None and (x - half < INSET - 1e-6 or x + half > W - INSET + 1e-6):
            problems.append(f"{kind} at x {x}: not {INSET} px inside the field (QE-1 a)")
        if "branch fork" in kind:
            k = int(kind.split("branch fork ")[1][0]) - 1
            if t < latest_choice(k) - 1e-6 or scroll(t) > island_rows(k)[1]:
                problems.append(f"{kind} at t {t}: outside its fork's window")
    for name, t, side in TOWERS:
        row = scroll(t) + TOWER / 2
        for r in np.arange(row - 220, row + 220, 4):
            wdt = float(foot(1, r)) - float(foot(0, r))
            isl = any(island_rows(k)[0] <= r <= island_rows(k)[1] for k in range(len(ISLANDS)))
            if isl or wdt < 300 - 1e-6:
                problems.append(f"the {name} Sentinel (t={t}): passage {wdt:g} or an island at row {r:g}")
                break
    for name in gallery_names():
        if not (OUT / f"{name}.png").exists():
            problems.append(f"{name}.png missing")
    for p in problems:
        print("PROBLEM", p)
    print(f"check: {len(problems)} problems")
    return not problems


def main(argv):
    _bind()
    if "--check" in argv:
        sys.exit(0 if check() else 1)
    if "--review" in argv:
        review()
        return
    if "--proposal" in argv:
        write_proposal()
        return
    build()
    write_proposal()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
