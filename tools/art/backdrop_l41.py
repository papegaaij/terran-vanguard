#!/usr/bin/env python3
"""Production art: the Level 41 backdrop, Callisto Fortress (design/campaign/act-6-jovian-storm/level-41-callisto-fortress;
M10 part G batch, concept round 79, step ART-G2): Callisto's ice and the black citadel, the three gates' blast doors,
the empty berth, and the gate layouts' ground positions of the level's ``--proposal``.

Straight to production from round 72's chosen look: Q10 a's **L41 grade** (tools/concept/act6_r72.py ``ground_l41``:
dark blue-grey ice, the black citadel with gold trim, red and gold lamps, the low Sun's long shadows) laid out as the
level's Layout has it. **No Jupiter** (the user, 2026-11-01, round 77 F.1 redo: no Jupiter on top-down ground levels):
the ice covers the screen from t = 0. The Sun is low in the top left (21 degrees up): every structure casts a shadow
about 2.6 times its height to the bottom right. The citadel's decks are a step lighter than the gloss-black units on
them (the readability note); the walls, towers and roofs are black with gold coping. Every glow is a float field
written unquantised (far more than 32 steps, no ordered dither: the 2026-10-30 dither audit, DitherGuardTest's
measure); the drive glow is additive (premultiplied on black) and drawn once.

The ground is **one continuous fortress** laid out on the level's clock (every landmark by its time or by its place on
a gate's halted screen), shaded once with the low Sun's cast shadows and cut into ``fortress-NN`` ground pieces of at
most 2,040 rows placed back to back on exact rows (no ground tile set: Level 37's city's way), opaque from row -16 (a
margin under the screen's bottom edge at t 0) except the empty berth's well, where ``berth-depth`` (deep) shows:
  section 1   Callisto's cratered ice (bright fresh-ice rims), the crater-rim outpost (t~8) with its SAM deck and
              mast, the outer causeway's gatehouse (t~15) grafted by the Vrell
  section 2   the crevasse field and the two causeways forking from the gatehouse road to the outer wall's two portals;
              the outer wall (t~32) with its central tower (the Sentinel's) and the **Vrell growth** over its left half;
              inside, the outer bailey's deck, Gate One's right bastion and archive core A's well
  section 3   **Gate One** (the outer gate): its wall, the two pylons' towers in the upper corners, the gateway
  section 4   the hangar decks: the launch deck up the middle with its rails, hangar roofs either side with open bays
              (parked Talons, dimmed: scenery), the Rail Bunkers' pads, a SAM roof
  section 5   **Gate Two** (the hangar gate): the left bastion's pylon tower, the right corner tower, core B's well
  section 6   the citadel ring: the ring road curving round the core's black wall, buttresses and lamps
  section 7   **Gate Three** (the inner gate): its towers and right bastion, core C's well
  section 8   the inner court, Varga's old research wing (its roof cracked open over the lab vault), the Halo's mount
  section 9   the Halo's mount (the platform's sprite is the game's, tools/art/halo_platform.py)
  section 10  Helix HQ's stepped roofs and comm masts (their shadows the longest on the level), then the dock: the
              **empty berth**, its clamp arms open, the gantries, the loose umbilicals
Other pieces:
  gate-1-doors, gate-2-doors, gate-3-doors  ground 160x104, 12 frames: the gate's blast doors, frame 0 closed, frame
              11 open; the leaves part along the scroll's ease (opening share = smoothstep of the frame's time, the
              scroll's own 2 s ramp), so the game plays them at 6 fps from the gate's GATE_OPENED and holds the last
              frame (G4's hook: the proposal's ``doors:`` rows, not ``placed:``); each frame's edge rows and columns
              are the fortress piece's pixels there
  drive-glow  deep 64x176, additive: the Ascendant's gold drive glow and its plume, climbing away through the berth
              (a ``path``), seen only through the well
  berth-depth deep tile 480x512: the berth well's dark depth (black-blue, a few faint lamps far down)
Motion budget (checked): the doors move only when their gate opens; the drive glow is the one other moving piece.

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout (ten sections; the halts are 2 s sections at speed 0, the
clock standing at their ends). ``--proposal`` writes the ``backdrop:`` block, the sections, the gates (each pylon's
place, facing and dome, what the dome covers, the archive core, the doors), the other ground placements, the roads'
paths and the Halo's place to concept/backdrop-proposal.yaml (never into data.yaml). **Re-run after the data moves a
section** (the fortress is laid out on the clock and the pieces are placed by time).

Review: design/campaign/act-6-jovian-storm/level-41-callisto-fortress/concept/backdrop-final-r79-a.png (sixteen screens
with stand-ins, the readability crops at 1x, the doors' frames, the whole fortress as a strip) and .gif (Gate One's halt
at half size: the domes up, the second pylon falls, the doors part as the scroll eases up).
Run: python3 tools/art/backdrop_l41.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys

import numpy as np
import yaml
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the shared Jovian pieces: the clock, the fields, the checks' measures)
import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
from jovkit import additive_rgb, blur, checker_share, glow, rgba, smoothstep, wrap_score  # noqa: E402
from render import raster  # noqa: E402

SCRIPT = "backdrop_l41.py"
BATCH = "M10 part G batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r79"
VARIANT = "a"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-41-callisto-fortress"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-41"
W, SCREEN = 480, 540
FACTORS = {"deep": 0.15, "ground": 1.0}
MAX_PIECE = 2040                           # an atlas page is 2048 high (AtlasPacker), 2 px padding a side
DOOR_FRAMES, DOOR_FPS = 12, 6              # 2 s: the scroll's ramp out of a halt

# The L41 grade (Q10 a, act6_r72 ground_l41): dark blue-grey ice, the black citadel, red and gold lamps.
ICE = [(0.0, (28, 35, 47)), (0.45, (58, 68, 84)), (0.8, (88, 99, 117)), (1.0, (114, 125, 142))]
FRESH = np.array((168, 184, 206.0))        # fresh ice on young craters' rims (bright specks)
CREVASSE = np.array((10, 14, 26.0))
DECK = np.array((104, 110, 124.0))         # the citadel's decks: a step lighter than the gloss-black units
DECK_DARK = np.array((78, 84, 98.0))
ROAD = np.array((88, 92, 104.0))
BLACK = np.array((30, 31, 38.0))           # walls, towers, roofs
ROOF = np.array((52, 55, 64.0))
GOLD = np.array((184, 146, 70.0))          # the coping: the units' gold a step darker (the units' gold reads over it)
GOLD_DIM = np.array((120, 96, 52.0))
SLATE = np.array((86, 90, 100.0))          # the towers' top pads (under the pylons)
BAY = np.array((20, 21, 26.0))
TISSUE = np.array((26, 74, 68.0))          # the Vrell growth (Level 37's)
TISSUE_DARK = np.array((18, 40, 44.0))
VEIN = np.array((0, 220, 140.0))
RED_LAMP = (255, 60, 40)
GOLD_LAMP = (255, 186, 90)
WHITE_LAMP = (220, 230, 255)
SUN = np.array((-0.62, -0.70, 0.36))
SUN = SUN / np.linalg.norm(SUN)            # low in the top left: shadows 2.6x a structure's height
AMBIENT = 0.46
SHADOW_TINT = np.array((0.86, 0.92, 1.06))  # the shadows a touch bluer (the ice's sky light)

# =========================================================================== the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "The Ice Field", "end": 20, "speed": 140, "atmosphere": "clear", "tiles": ["berth-depth"]},
    {"name": "The Outer Causeways", "end": 42, "speed": 120, "ramp": 2, "atmosphere": "clear", "tiles": ["berth-depth"]},
    {"name": "Gate One", "end": 44, "speed": 0, "ramp": 2, "holds": "pylons", "atmosphere": "clear",
     "tiles": ["berth-depth"]},
    {"name": "The Hangar Decks", "end": 74, "speed": 120, "ramp": 2, "atmosphere": "clear", "tiles": ["berth-depth"]},
    {"name": "Gate Two", "end": 76, "speed": 0, "ramp": 2, "holds": "pylons", "atmosphere": "clear",
     "tiles": ["berth-depth"]},
    {"name": "The Citadel Ring", "end": 98, "speed": 120, "ramp": 2, "atmosphere": "clear", "tiles": ["berth-depth"]},
    {"name": "Gate Three", "end": 100, "speed": 0, "ramp": 2, "holds": "pylons", "atmosphere": "clear",
     "tiles": ["berth-depth"]},
    {"name": "The Inner Court", "end": 112, "speed": 120, "ramp": 2, "atmosphere": "clear", "tiles": ["berth-depth"]},
    {"name": "The Halo", "end": 114, "speed": 0, "ramp": 2, "holds": "halo", "atmosphere": "clear",
     "tiles": ["berth-depth"]},
    {"name": "Helix HQ", "end": 146, "speed": 140, "ramp": 2, "atmosphere": "clear", "tiles": ["berth-depth"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", 140)) if DATA else 140.0
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll = CLOCK.scroll
HALTS = [i for i, s in enumerate(SECTIONS) if s.get("speed") is not None and float(s["speed"]) == 0]
assert len(HALTS) == 4, HALTS              # Gate One, Two, Three, the Halo


def halt_t(k):
    """The level time the k-th halt stands at (its section's end; k = 0..3)."""
    return float(SECTIONS[HALTS[k]]["end"])


def halt_top(k):
    """The ground row at the top edge of the k-th halt's held screen."""
    return scroll(halt_t(k)) + SCREEN


def hrow(k, y_down):
    """A place on the k-th halt's held screen (y down from the top edge) -> its ground row."""
    return halt_top(k) - y_down


def t_row(t, half):
    """The ground row of a unit's centre that enters at t (its centre half its size above the top edge)."""
    return scroll(t) + SCREEN + half


def t_of(row, half):
    """The entry time of a ground unit (2 * half px) whose centre stands on ``row``."""
    return round(CLOCK.time_at(row - SCREEN - half), 2)


def exact_t(bottom, h, factor=1.0):
    """The placement time putting a piece h px tall with its bottom row on layer row ``bottom``."""
    t = CLOCK.time_at((bottom + h / 2 - SCREEN / 2) / factor)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) * factor + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def centre_t(row, factor=1.0):
    return round(CLOCK.time_at((row - SCREEN / 2) / factor), 3)


# =========================================================================== the gates (held screens, y down)

def facing_centre(x, y, facing, dist=140.0):
    a = np.radians(facing)
    return x + dist * np.cos(a), y + dist * np.sin(a)


# Each gate on its held screen: the wall's y, the gateway (x0, x1), the towers and bastions (polygons in held-screen
# px), each pylon (x, y, facing in degrees from +x clockwise, y down; 16 facings) with what its dome covers, the core.
# The pylon rule: in the upper half, never under a dome (hard's 125 px too), the domes' centres at y <= 360; the cores
# outside every dome (hard's 125 too); a covered unit within 95 px of its dome's centre (easy's radius).
GATES = [
    {"name": "Gate One", "wall": 110, "gate": (165, 315),
     "pylons": [
         {"id": "A", "x": 80, "y": 96, "facing": 22.5,
          "covers": ("warden-tank", "the Warden pair parked in the gateway (driving in from t~38)",
                     [(214, 150), (262, 168)])},
         {"id": "B", "x": 400, "y": 96, "facing": 90.0,
          "covers": ("sam-nest", "a SAM Nest group on the right bastion", [(370, 226), (420, 210), (418, 262)])}],
     "bastions": [[(340, 128), (472, 128), (472, 236), (412, 304), (352, 266)]],
     "core": (240, 400)},
    {"name": "Gate Two", "wall": 116, "gate": (165, 315),
     "pylons": [
         {"id": "A", "x": 430, "y": 100, "facing": 112.5,
          "covers": ("rail-bunker", "the Rail Bunker pair (t~70) on their deck pads", [(340, 210), (412, 252)])},
         {"id": "B", "x": 60, "y": 100, "facing": 22.5,
          "covers": ("warden-tank", "a Warden pair parked in the gateway", [(214, 160), (268, 146)])},
         {"id": "C", "x": 100, "y": 260, "facing": 22.5,
          "covers": ("graft-turret", "a Graft Turret nest of 3 on the apron beside the left bastion",
                     [(200, 320), (250, 345), (262, 300)])}],
     "bastions": [[(8, 134), (140, 134), (148, 236), (104, 302), (8, 290)]],
     "core": (120, 390)},
    {"name": "Gate Three", "wall": 110, "gate": (165, 315),
     "pylons": [
         {"id": "A", "x": 420, "y": 100, "facing": 157.5,
          "covers": ("warden-tank", "the Warden pair parked in the gateway (driving in from t~96)",
                     [(300, 150), (240, 140)])},
         {"id": "B", "x": 50, "y": 132, "facing": 67.5,
          "covers": ("rail-bunker", "a Rail Bunker on its pad", [(104, 261)])},
         {"id": "C", "x": 410, "y": 262, "facing": 180.0,
          "covers": ("sam-nest", "a SAM Nest group on the apron", [(236, 280), (280, 310), (306, 276)])}],
     "bastions": [[(340, 128), (472, 128), (472, 280), (430, 304), (366, 284)]],
     "core": (360, 390)},
]
HALO_CORE = (240, 190)                     # the Halo's core on its held screen (its README)
HALO_R = 120                               # half its 240 px
DOME_R = {"easy": 95, "medium": 110, "hard": 125}
HALF = {"warden-tank": 32, "rail-bunker": 36, "sentinel-tower": 40, "sam-nest": 22, "graft-turret": 22,
        "shield-pylon": 28, "archive-core": 20, "lab-vault": 20}
WALL_THICK = 36                            # rows
APRON_Y = 470                              # a gate's apron reaches this far down its held screen (the cores at 390-400)
WALL_H, TOWER_H = 22.0, 30.0


def gate_rows(k):
    g = GATES[k]
    return hrow(k, g["wall"])


# =========================================================================== the rest of the layout (rows y-up, x)

def road_paths():
    """The roads as polylines of (x, row): the outer causeway, its two causeways, the bailey lanes, the ring road."""
    g1 = gate_rows(0)
    g2 = gate_rows(1)
    g3 = gate_rows(2)
    wall = outer_wall_row()
    gh = t_row(15, 0)
    fork = STARTS_ROW(1) + 120
    return {
        "outer-causeway": [(240, gh - 120), (240, gh), (240, fork)],
        "left-causeway": [(240, fork), (200, fork + 260), (140, fork + 640), (128, fork + 1000), (128, wall + 40)],
        "right-causeway": [(240, fork), (282, fork + 260), (340, fork + 640), (352, fork + 1000), (352, wall + 40)],
        "left-lane": [(128, wall + 40), (140, wall + 400), (180, hrow(0, 400)), (214, g1 - 110), (230, g1 - 20)],
        "right-lane": [(352, wall + 40), (340, wall + 400), (300, hrow(0, 400)), (266, g1 - 110), (250, g1 - 20)],
        "launch-deck": [(240, g1 + 20), (240, g2 - 20)],
        "ring-road": [(240, g2 + 20), (190, g2 + 240), (112, g2 + 640), (104, g2 + 1080), (170, g2 + 1560),
                      (290, g2 + 1980), (300, g3 - 260), (246, g3 - 20)],
    }


def STARTS_ROW(i):  # noqa: N802  (a section's first row at the top edge: LevelData.seam)
    return int(round(scroll(STARTS[i]) + SCREEN))


def outer_wall_row():
    """The outer wall's middle row: the Sentinel on its central tower enters at t~32."""
    return t_row(32, HALF["sentinel-tower"])


def mount_row():
    return hrow(3, HALO_CORE[1])


def berth():
    """The empty berth's well: (x0, x1, row0, row1) - the screen at the level's end shows it whole."""
    top = scroll(END) + SCREEN
    return 160, 320, top - 470, top - 70


# The other ground targets (the README's Ground targets outside the gates): (enemy, note, [(t, x)]) - placed on the
# fortress's decks, roofs and walls; the parked Wardens and the gates' covered units are in ``GATES``.
GUARDS = [
    ("sam-nest", "SAM Nest group on the crater-rim outpost's deck", [(8.0, 300), (8.0, 345), (8.0, 390)]),
    ("graft-turret", "Graft Turret nest on the gatehouse roof (the Vrell growth round it)",
     [(15.0, 192), (15.0, 288), (15.45, 192), (15.45, 288)]),
    ("warden-tank", "Warden Tank pair on the left causeway (they drive down it: path left-causeway)",
     [(24.0, None), (24.6, None)]),
    ("sentinel-tower", "Sentinel Tower on the outer wall's central tower", [(32.0, 240)]),
    ("rail-bunker", "Rail Bunker pair on the hangar decks' pads", [(50.0, 96), (50.6, 384)]),
    ("graft-turret", "Graft Turret nest on the launch deck's edges", [(58.0, 150), (58.0, 330), (58.5, 150),
                                                                      (58.5, 330)]),
    ("sam-nest", "SAM Nest group on a hangar roof (right)", [(64.0, 362), (64.0, 406), (64.3, 450)]),
    ("sentinel-tower", "Sentinel Tower on the citadel core's wall", [(80.0, 352)]),
    ("sam-nest", "SAM Nest group on the core's terrace", [(84.0, 312), (84.0, 356), (84.3, 400)]),
    ("warden-tank", "Warden Tank convoy on the ring road (path ring-road)", [(86.0, None), (86.6, None),
                                                                            (87.2, None)]),
    ("graft-turret", "Graft Turret nest in the inner court (left)", [(104.0, 64), (104.0, 112), (104.4, 64),
                                                                     (104.4, 112)]),
    ("sentinel-tower", "Sentinel Tower in the inner court", [(108.0, 112)]),
    ("rail-bunker", "Rail Bunker pair on HQ's roofs", [(120.0, 100), (120.6, 380)]),
    ("sam-nest", "SAM Nest group on HQ's roofs", [(126.0, 300), (126.0, 344), (126.3, 388)]),
    ("sentinel-tower", "Sentinel Tower on HQ's roofs", [(132.0, 120)]),
    ("graft-turret", "Graft Turret nest on HQ's last roof", [(138.0, 330), (138.0, 378), (138.4, 330),
                                                             (138.4, 378)]),
]
VAULT = (104.0, 410)                       # Varga's lab vault (t, x): the README's t~104, x~410
BEACON = (240, 380)                        # the CDF beacon's patch at t=20 (README: y-up on the screen)
PARKED = [("left-causeway", 2), ("ring-road", 3)]


def guard_at(kind, t, x):
    """A guard's (x, row); a convoy's x follows its road."""
    row = t_row(t, HALF[kind])
    if x is None:
        name = "left-causeway" if t < 30 else "ring-road"
        x = path_x(road_paths()[name], row)
    return x, row


def path_x(pts, row):
    pts = sorted(pts, key=lambda p: p[1])
    rs = [p[1] for p in pts]
    xs = [p[0] for p in pts]
    return float(np.interp(row, rs, xs))


# =========================================================================== the field

ROW_LO = -16                               # under the screen's bottom edge at t 0
PAD = 220                                  # shading margin (the longest shadow ~156 px)


def row_hi():
    return int(np.ceil(scroll(END) + SCREEN)) + 24


class Field:
    """The fortress's arrays over ground rows [lo, hi) in image order (index i = hi - 1 - row: the top row first)."""

    def __init__(self, lo, hi):
        self.lo, self.hi = lo, hi
        h = hi - lo
        self.hgt = np.zeros((h, W), np.float32)
        self.alb = np.zeros((h, W, 3), np.float32)
        self.emis = np.zeros((h, W, 3), np.float32)
        self.alpha = np.ones((h, W), np.float32)
        self.lamps = []

    def win(self, x0, x1, ra, rb):
        """The window over x [x0, x1), rows [ra, rb): (slices, X, ROW) at the pixel centres; None when empty."""
        h = self.hi - self.lo
        i0, i1 = int(max(0, self.hi - np.ceil(rb))), int(min(h, self.hi - np.floor(ra)))
        j0, j1 = int(max(0, np.floor(x0))), int(min(W, np.ceil(x1)))
        if i0 >= i1 or j0 >= j1:
            return None
        ii, jj = np.mgrid[i0:i1, j0:j1]
        return (slice(i0, i1), slice(j0, j1)), (jj + 0.5).astype(np.float64), (self.hi - 1 - ii + 0.5).astype(
            np.float64)

    def paint(self, sl, m, h=None, col=None, add_h=None):
        m = np.clip(np.asarray(m, np.float32), 0, 1)
        if col is not None:
            col = np.asarray(col, np.float32)
            self.alb[sl] = self.alb[sl] * (1 - m[..., None]) + col * m[..., None]
        if h is not None:
            self.hgt[sl] = self.hgt[sl] * (1 - m) + np.asarray(h, np.float32) * m
        if add_h is not None:
            self.hgt[sl] += np.asarray(add_h, np.float32) * m

    def lamp(self, x, row, colour, r=1.8, k=1.0):
        self.lamps.append((x, row, colour, r, k))


def sd_box(dx, dy, hx, hy, ch=0.0):
    ax, ay = np.abs(dx), np.abs(dy)
    return np.maximum(np.maximum(ax - hx, ay - hy), (ax + ay - hx - hy + ch) / np.sqrt(2))


def aa(sd, w=0.7):
    return smoothstep(w, -w, sd)


def box(f, cx, cr, hx, hy, h, col, ch=0.0, top=None, rim=None, rim_w=2.0):
    """A chamfered block (half sizes hx, hy) at height h; ``top`` an inset top colour, ``rim`` a coping band."""
    w = f.win(cx - hx - 2, cx + hx + 2, cr - hy - 2, cr + hy + 2)
    if w is None:
        return
    sl, X, R = w
    sd = sd_box(X - cx, R - cr, hx, hy, ch)
    f.paint(sl, aa(sd), h, col)
    if rim is not None:
        f.paint(sl, aa(sd) * aa(-sd - rim_w), None, rim)
    if top is not None:
        f.paint(sl, aa(sd + rim_w + 2), None, top)


def disc(f, cx, cr, r, h=None, col=None, add_h=None, r_in=None):
    w = f.win(cx - r - 2, cx + r + 2, cr - r - 2, cr + r + 2)
    if w is None:
        return
    sl, X, R = w
    d = np.hypot(X - cx, R - cr)
    m = aa(d - r)
    if r_in is not None:
        m = m * aa(r_in - d)
    f.paint(sl, m, h, col, add_h)


def poly_mask(f, pts, ss=4):
    """An anti-aliased mask of a polygon of (x, row) points over its bounding window: (slices, mask) or None."""
    xs, rs = [p[0] for p in pts], [p[1] for p in pts]
    w = f.win(min(xs) - 2, max(xs) + 2, min(rs) - 2, max(rs) + 2)
    if w is None:
        return None
    sl, X, R = w
    j0, i0 = sl[1].start, sl[0].start
    hh, ww = X.shape
    im = Image.new("L", (ww * ss, hh * ss))
    ImageDraw.Draw(im).polygon([((x - j0) * ss, (f.hi - 1 - r + 0.5 - i0) * ss) for x, r in pts], fill=255)
    m = np.array(im.resize((ww, hh), Image.BOX)).astype(np.float64) / 255
    return sl, m, X, R


def seg_dist(X, R, pts):
    d = np.full(X.shape, 1e9)
    for (x0, r0), (x1, r1) in zip(pts, pts[1:]):
        vx, vr = x1 - x0, r1 - r0
        L2 = max(vx * vx + vr * vr, 1e-9)
        u = np.clip(((X - x0) * vx + (R - r0) * vr) / L2, 0, 1)
        d = np.minimum(d, np.hypot(X - x0 - u * vx, R - r0 - u * vr))
    return d


def smooth_path(pts, n=12):
    """A Catmull-Rom spline through the points (x, row)."""
    p = [pts[0]] + list(pts) + [pts[-1]]
    out = []
    for k in range(1, len(p) - 2):
        p0, p1, p2, p3 = (np.array(q, float) for q in p[k - 1:k + 3])
        for s in np.linspace(0, 1, n, endpoint=False):
            out.append(tuple(0.5 * ((2 * p1) + (-p0 + p2) * s + (2 * p0 - 5 * p1 + 4 * p2 - p3) * s * s
                                    + (-p0 + 3 * p1 - 3 * p2 + p3) * s ** 3)))
    out.append(tuple(pts[-1]))
    return out


# --------------------------------------------------------------------------- the ice

def ice(f, seed=4101):
    h = f.hi - f.lo
    n = raster.fbm(W, h, 64, seed, octaves=5, period=False)
    tex = raster.fbm(W, h, 8, seed + 1, octaves=2, period=False)
    frost = raster.fbm(W, h, 24, seed + 2, octaves=3, period=False)
    f.alb[:] = raster.ramp(ICE, np.clip(0.2 + 0.62 * n + 0.08 * (tex - 0.5), 0, 1)).astype(np.float32)
    bright = smoothstep(0.66, 0.8, frost)
    f.alb[:] = f.alb * (1 - 0.35 * bright[..., None]) + FRESH * 0.35 * bright[..., None]
    f.hgt[:] = (blur(n, 3.0) * 6).astype(np.float32)
    rng = np.random.default_rng(seed + 3)
    area = (f.hi - f.lo) * W
    for _ in range(int(area / 2600)):                       # Callisto: saturated with craters, small ones most
        r = 3.5 * (1 - rng.uniform(0, 0.985)) ** -0.62
        r = min(r, 70)
        crater(f, rng.uniform(-20, W + 20), rng.uniform(f.lo, f.hi), r, rng.uniform() < 0.22)


def crater(f, cx, cr, r, fresh):
    w = f.win(cx - 2.2 * r - 2, cx + 2.2 * r + 2, cr - 2.2 * r - 2, cr + 2.2 * r + 2)
    if w is None:
        return
    sl, X, R = w
    d = np.hypot(X - cx, R - cr) / r
    bowl = -0.22 * r * np.clip(1 - d * d, 0, 1)
    rim = 0.07 * r * np.exp(-((d - 1) / 0.16) ** 2)
    ejecta = 0.02 * r * np.exp(-np.clip(d - 1, 0, None) / 0.35) * (d > 1)
    f.hgt[sl] += (bowl + rim + ejecta).astype(np.float32)
    floor = smoothstep(0.9, 0.4, d) * 0.18
    f.alb[sl] = f.alb[sl] * (1 - floor[..., None])
    if fresh:
        k = (np.exp(-((d - 1.02) / 0.12) ** 2) * 0.55 + np.exp(-np.clip(d - 1, 0, None) / 0.5) * (d > 1) * 0.18)
        f.alb[sl] = f.alb[sl] * (1 - k[..., None]) + FRESH * k[..., None]


def crevasses(f, ra, rb, seed=4111):
    """The crevasse field: branching dark cracks (ridged noise lines) between rows ra and rb, faded at its ends."""
    w = f.win(0, W, ra, rb)
    sl, X, R = w
    hh = X.shape[0]
    a = raster.fbm(W, hh, 90, seed, octaves=4, period=False)
    b = raster.fbm(W, hh, 50, seed + 1, octaves=3, period=False)
    fade = smoothstep(ra, ra + 260, R) * smoothstep(rb, rb - 200, R)
    width = 0.010 + 0.012 * b
    m = smoothstep(width, width * 0.35, np.abs(a - 0.5)) * fade
    m2 = smoothstep(0.006, 0.002, np.abs(b - 0.42)) * fade * smoothstep(0.35, 0.6, a)
    m = np.clip(np.maximum(m, m2), 0, 1)
    rim = smoothstep(0.0, 0.8, blur(m, 2.5)) * (1 - m)
    f.paint(sl, rim * 0.5, None, FRESH)
    f.paint(sl, m, -28.0, CREVASSE)


# --------------------------------------------------------------------------- the citadel's parts

def deck(f, x0, x1, ra, rb, h=4.0, tone=DECK, plate=32, seed=4121, edge=True):
    """A deck slab: plates with joints, a little wear; hazard stripes along its edges."""
    w = f.win(x0, x1, ra, rb)
    if w is None:
        return
    sl, X, R = w
    tex = raster.fbm(X.shape[1], X.shape[0], 12, seed, octaves=3, period=False)
    col = tone * (0.9 + 0.16 * tex[..., None])
    joint = ((np.mod(X - x0, plate) < 1.0) | (np.mod(R - ra, plate) < 1.0))
    col = col * (1 - 0.16 * joint[..., None])
    sd = sd_box(X - (x0 + x1) / 2, R - (ra + rb) / 2, (x1 - x0) / 2, (rb - ra) / 2)
    m = aa(sd)
    f.paint(sl, m, h, col)
    if edge:
        band = m * aa(-sd - 5)
        stripe = (np.mod(X + R, 16) < 8)
        f.paint(sl, band * stripe, None, GOLD_DIM)
        f.paint(sl, band * ~stripe, None, BLACK)


def wall(f, x0, x1, row, h=WALL_H, thick=WALL_THICK, lamps=True, crenel=True):
    """A black wall across x0..x1 centred on ``row``: gold coping on its sunlit (upper) face, a dark lower lip,
    crenels, gold lamps along its top."""
    box(f, (x0 + x1) / 2, row, (x1 - x0) / 2, thick / 2, h, BLACK)
    w = f.win(x0, x1, row - thick / 2, row + thick / 2)
    if w is None:
        return
    sl, X, R = w
    cop = smoothstep(row + thick / 2 - 6, row + thick / 2 - 3, R) * aa(R - (row + thick / 2) + 0.5)
    f.paint(sl, cop, None, GOLD)
    if crenel:
        notch = (np.mod(X - x0, 28) < 10) & (np.abs(R - row - 2) < 6)
        f.paint(sl, notch.astype(float), h - 5, BLACK * 0.8)
    if lamps:
        for x in np.arange(x0 + 14, x1 - 6, 56):
            f.lamp(x, row - thick / 2 + 4, GOLD_LAMP, 1.6, 0.8)


def tower(f, cx, cr, r=34, h=TOWER_H, pad=True, beacon=True):
    """A gate tower: a black octagon with a gold rim and a slate top pad (a pylon stands on it)."""
    ch = r * (2 - np.sqrt(2))
    box(f, cx, cr, r, r, h, BLACK, ch=ch, rim=GOLD, rim_w=2.5)
    if pad:
        box(f, cx, cr, r - 6, r - 6, h, SLATE, ch=(r - 6) * (2 - np.sqrt(2)))
        disc(f, cx, cr, r - 12, None, SLATE * 0.82, r_in=r - 14)
    if beacon:
        for a in (TAU / 8, 3 * TAU / 8, 5 * TAU / 8, 7 * TAU / 8):
            f.lamp(cx + (r - 3) * np.cos(a), cr + (r - 3) * np.sin(a), RED_LAMP, 1.6, 0.9)


def poly(f, pts, h, col):
    pm = poly_mask(f, pts)
    if pm is None:
        return
    sl, m, X, R = pm
    f.paint(sl, m, h, col)


def shrink(pts, k):
    """A polygon moved ``k`` px toward its centroid (a roof inset)."""
    cx, cr = np.mean([p[0] for p in pts]), np.mean([p[1] for p in pts])
    out = []
    for x, r in pts:
        d = max(np.hypot(x - cx, r - cr), 1e-6)
        out.append((x - (x - cx) / d * k, r - (r - cr) / d * k))
    return out


def road(f, pts, half, h, col, lanes=True, lamps=None, rim=None):
    xs, rs = [p[0] for p in pts], [p[1] for p in pts]
    w = f.win(min(xs) - half - 4, max(xs) + half + 4, min(rs) - half - 4, max(rs) + half + 4)
    if w is None:
        return
    sl, X, R = w
    d = seg_dist(X, R, pts)
    m = aa(d - half)
    tex = raster.fbm(X.shape[1], X.shape[0], 10, 4131, octaves=2, period=False)
    f.paint(sl, m, h, col * (0.92 + 0.12 * tex[..., None]))
    if rim is not None:
        f.paint(sl, m * aa(half - 4 - d), None, rim)
    if lanes:
        dash = (np.mod(R, 40) < 22)
        f.paint(sl, aa(np.abs(d) - 1.0) * dash, None, GOLD_DIM)
    if lamps:
        acc = 0.0
        for (x0, r0), (x1, r1) in zip(pts, pts[1:]):
            L = np.hypot(x1 - x0, r1 - r0)
            if L < 1e-6:
                continue
            ux, ur = (x1 - x0) / L, (r1 - r0) / L
            s = (lamps - acc) % lamps
            while s < L:
                for side in (-1, 1):
                    f.lamp(x0 + ux * s - ur * side * (half - 3), r0 + ur * s + ux * side * (half - 3), GOLD_LAMP,
                           1.4, 0.7)
                s += lamps
            acc = (acc + L) % lamps


def growth(f, cx, cr, rx, ry, seed, veins=True, h=6.0):
    """Vrell growth: dark teal tissue in lumpy lobes, tendrils creeping out, glowing veins."""
    w = f.win(cx - rx * 1.6, cx + rx * 1.6, cr - ry * 1.6, cr + ry * 1.6)
    if w is None:
        return
    sl, X, R = w
    hh, ww = X.shape
    n = raster.fbm(ww, hh, 18, seed, octaves=4, period=False)
    t = raster.fbm(ww, hh, 7, seed + 1, octaves=3, period=False)
    d = np.hypot((X - cx) / rx, (R - cr) / ry)
    body = smoothstep(1.0, 0.75, d + (n - 0.5) * 0.9)
    tendril = smoothstep(0.035, 0.0, np.abs(t - 0.5)) * smoothstep(1.55, 0.9, d)
    m = np.clip(np.maximum(body, tendril * 0.9), 0, 1)
    col = TISSUE_DARK * (1 - n[..., None]) + TISSUE * n[..., None]
    f.paint(sl, m, None, col, add_h=h * body * (0.5 + n))
    if veins:
        v = smoothstep(0.02, 0.0, np.abs(n - 0.55)) * body
        f.emis[sl] += (v[..., None] * VEIN * 0.45).astype(np.float32)


def core_well(f, x, row):
    """An archive core's housing: a recessed round well with a gold ring and a dark teal glass floor (the hatch and
    the core's glow are the game's sprites)."""
    disc(f, x, row, 30, 3.0, DECK_DARK)
    disc(f, x, row, 30, None, GOLD, r_in=27)
    disc(f, x, row, 25, -4.0, np.array((22, 34, 40.0)))
    for a in np.linspace(0, TAU, 6, endpoint=False):
        f.lamp(x + 28.5 * np.cos(a), row + 28.5 * np.sin(a), (60, 200, 190), 1.3, 0.6)


def pad(f, x, row, r=40):
    """A unit pad on a deck: a darker octagon with a gold ring and bolts."""
    ch = r * (2 - np.sqrt(2))
    box(f, x, row, r, r, None, DECK_DARK, ch=ch)
    w = f.win(x - r - 2, x + r + 2, row - r - 2, row + r + 2)
    if w is None:
        return
    sl, X, R = w
    sd = sd_box(X - x, R - row, r, r, ch)
    f.paint(sl, aa(sd) * aa(-sd - 2), None, GOLD_DIM)


def mast(f, x, row, h=60.0, k=1.0):
    """A comm mast: a thin lattice post (tall: the longest shadows), a red beacon at its tip."""
    box(f, x, row, 3, 3, h, BLACK * 1.3)
    for a in (0, TAU / 3, 2 * TAU / 3):
        w = f.win(x - 14, x + 14, row - 14, row + 14)
        sl, X, R = w
        ux, ur = np.cos(a), np.sin(a)
        along = (X - x) * ux + (R - row) * ur
        across = np.abs(-(X - x) * ur + (R - row) * ux)
        m = aa(across - 0.6) * (along > 0) * (along < 12)
        f.paint(sl, m, h * 0.2, BLACK)
    f.lamp(x - 1.5, row + 1.5, RED_LAMP, 2.2, 1.1 * k)


def dish(f, x, row, r=14):
    disc(f, x, row, r, 10.0, np.array((150, 156, 168.0)))
    w = f.win(x - r, x + r, row - r, row + r)
    sl, X, R = w
    d = np.hypot(X - x, R - row) / r
    f.paint(sl, aa((d - 1) * r) * smoothstep(0.2, 1.0, d) * 0.6, None, np.array((96, 100, 112.0)), add_h=-4 * (1 - d))
    disc(f, x, row, 2.5, 14.0, GOLD)


# --------------------------------------------------------------------------- the talons parked in the bays

def parked_talon(f, x, row):
    """A Talon parked in a bay, nose toward the launch deck, dimmed (scenery under the bay's roof shadow)."""
    try:
        fr = l23kit.load("talon")[0]
    except FileNotFoundError:
        return
    a = np.array(fr).astype(np.float64)
    lum = a[..., :3] @ np.array((0.299, 0.587, 0.114))
    rgb = (a[..., :3] * 0.4 + lum[..., None] * 0.6) * 0.5
    al = a[..., 3] / 255
    h, w = al.shape
    sl_w = f.win(x - w / 2, x + w / 2, row - h / 2, row + h / 2)
    if sl_w is None:
        return
    sl, X, R = sl_w
    ii = np.clip((h / 2 - (R - row) - 0.5).astype(int), 0, h - 1)
    jj = np.clip((X - x + w / 2 - 0.5).astype(int), 0, w - 1)
    f.paint(sl, al[ii, jj], None, rgb[ii, jj], add_h=4.0 * al[ii, jj])


# =========================================================================== the fortress, section by section

def build_field():
    lo, hi = ROW_LO - PAD, row_hi() + PAD
    f = Field(lo, hi)
    ice(f)
    roads = road_paths()
    s2, s3 = STARTS_ROW(1), STARTS_ROW(2)
    wall_row = outer_wall_row()
    g = [gate_rows(k) for k in range(3)]
    # ---- section 1: the crater-rim outpost (t~8), the outer causeway's gatehouse (t~15)
    orow = t_row(8, HALF["sam-nest"])
    crater(f, 352, orow + 96, 54, True)
    deck(f, 268, 426, orow - 34, orow + 32, h=6.0, seed=4141)
    box(f, 446, orow - 6, 18, 24, 14.0, BLACK, rim=GOLD, rim_w=1.5)
    f.lamp(456, orow + 12, RED_LAMP, 1.8, 1.0)
    mast(f, 446, orow + 30, 52.0)
    gh = t_row(15, 0)
    road(f, smooth_path(roads["outer-causeway"]), 22, 8.0, ROAD, lamps=48)
    road(f, [(240, gh - 260), (240, gh - 60)], 18, 2.0, ROAD * 0.85, lanes=False)
    box(f, 240, gh, 70, 42, 18.0, BLACK, rim=GOLD, rim_w=2.0, top=ROOF)
    for gx in (192, 288):
        for gr in (gh - 22, gh + 22):
            growth(f, gx, gr, 30, 20, 4151 + gx + int(gr) % 7, h=4.0)
    growth(f, 240, gh + 6, 46, 30, 4155, h=5.0)
    f.lamp(172, gh - 40, RED_LAMP, 1.8, 1.0)
    f.lamp(308, gh - 40, RED_LAMP, 1.8, 1.0)
    # ---- section 2: the crevasse field, the causeways, the outer wall with the Vrell growth
    crevasses(f, s2 - 140, wall_row - 40)
    for name in ("left-causeway", "right-causeway"):
        pts = smooth_path(roads[name])
        road(f, pts, 22, 8.0, ROAD, lamps=48, rim=GOLD_DIM)
    # inside the wall: the outer bailey's deck up to Gate One
    deck(f, 0, W, wall_row + 10, g[0] + 10, h=3.0, tone=DECK * 0.94, plate=40, seed=4161, edge=False)
    gate_apron(f, 0)
    for name in ("left-lane", "right-lane"):
        road(f, smooth_path(roads[name]), 20, 3.5, ROAD, lamps=56)
    # the outer wall with its two portals and the central (Sentinel) tower
    for a, b in ((0, 96), (160, 320), (384, W)):
        wall(f, a, b, wall_row, h=24.0, thick=40)
    for px in (96, 160, 320, 384):
        box(f, px, wall_row, 12, 26, 30.0, BLACK, rim=GOLD, rim_w=2.0)
        f.lamp(px, wall_row + 20, RED_LAMP, 1.7, 1.0)
    tower(f, 240, wall_row, r=46, h=32.0, pad=True, beacon=True)
    growth(f, 70, wall_row + 6, 110, 52, 4171, h=8.0)       # the Vrell growth over the wall's left half
    growth(f, 150, wall_row - 44, 60, 40, 4173, h=5.0)
    growth(f, 30, wall_row - 80, 46, 70, 4175, h=4.0)
    growth(f, 196, wall_row + 30, 40, 26, 4177, h=5.0)
    # ---- section 4: the hangar decks
    hangars(f, g[0] + WALL_THICK / 2, hrow(1, APRON_Y))
    gate_apron(f, 1)
    # ---- section 6: the citadel ring
    ring(f, g[1] + WALL_THICK / 2, hrow(2, APRON_Y))
    gate_apron(f, 2)
    # ---- sections 8-9: the inner court, the research wing, the Halo's mount
    court(f, g[2] + WALL_THICK / 2, mount_row() + 160)
    # ---- section 10: Helix HQ's roofs and masts, the dock
    hq(f, mount_row() + 154, f.hi)
    # ---- the gates' walls, bastions and towers last (the next section's decks never cover them)
    for k in range(3):
        gate(f, k)
    for kind, _, at in GUARDS:                                # every guard on a pad or its roof spot
        if kind in ("rail-bunker", "sentinel-tower"):
            for t, x in at:
                gx, gr = guard_at(kind, t, x)
                if kind == "rail-bunker":
                    pad(f, gx, gr, 40)
    return f


def gate_apron(f, k):
    """The deck in front of gate k's wall (the held screen's lower part), the core's well, the covered bunkers' pads."""
    g = GATES[k]
    top = halt_top(k)
    wrow = hrow(k, g["wall"])
    deck(f, 0, W, hrow(k, APRON_Y), wrow, h=3.5, tone=DECK, plate=32, seed=4181 + k, edge=False)
    w = f.win(0, W, hrow(k, APRON_Y), hrow(k, APRON_Y) + 8)
    sl, X, R = w
    stripe = (np.mod(X + R, 16) < 8)
    f.paint(sl, stripe.astype(float), None, GOLD_DIM)            # the apron's hazard edge
    f.paint(sl, (~stripe).astype(float), None, BLACK)
    for e in gate_entries(k):
        if e["kind"] == "rail-bunker":
            for x, y in e["units"]:
                pad(f, x, top - y, 40)


def gate(f, k):
    """Gate k's bastions, wall, posts, gateway floor and towers (the pylons stand on the towers)."""
    g = GATES[k]
    top = halt_top(k)
    wrow = hrow(k, g["wall"])
    cx, cy = g["core"]
    core_well(f, cx, top - cy)
    for pts in g["bastions"]:
        rows = [(x, top - y) for x, y in pts]
        poly(f, rows, 16.0, BLACK)
        poly(f, shrink(rows, 5), 16.0, ROOF)
        for (x0, y0), (x1, y1) in zip(pts, pts[1:] + pts[:1]):
            for s in np.linspace(0.2, 0.8, 2):
                f.lamp(x0 + (x1 - x0) * s, top - (y0 + (y1 - y0) * s), GOLD_LAMP, 1.4, 0.6)
    gx0, gx1 = g["gate"]
    wall(f, 0, gx0 - 12, wrow)
    wall(f, gx1 + 12, W, wrow)
    for px in (gx0 - 6, gx1 + 6):                             # the gate posts (the doors slide into them)
        box(f, px, wrow, 12, WALL_THICK / 2 + 6, 28.0, BLACK, rim=GOLD, rim_w=2.0)
        f.lamp(px, wrow - WALL_THICK / 2 - 3, GOLD_LAMP, 1.8, 1.0)
        f.lamp(px, wrow + WALL_THICK / 2 + 3, RED_LAMP, 1.8, 1.0)
    passage(f, k)
    for p in g["pylons"]:
        tower(f, p["x"], top - p["y"], r=36)


def passage(f, k):
    """The gateway's floor between the posts: a deck with guide rails and a gold threshold (the doors cover it)."""
    g = GATES[k]
    wrow = hrow(k, g["wall"])
    gx0, gx1 = g["gate"]
    w = f.win(gx0, gx1, wrow - WALL_THICK / 2 - 1, wrow + WALL_THICK / 2 + 1)
    sl, X, R = w
    f.paint(sl, np.ones(X.shape), 1.0, DECK_DARK)
    rails = (np.abs(np.mod(X - gx0, 30) - 15) < 1.0)
    f.paint(sl, rails.astype(float), None, GOLD_DIM)
    thr = np.abs(R - wrow) < 2
    f.paint(sl, thr.astype(float), None, GOLD)


def hangars(f, ra, rb):
    """The hangar decks: the launch deck (x 168-312) up the middle with its rails and chevrons, hangar roofs either side
    with open bays toward the deck (parked Talons), the pads and roofs the guards stand on."""
    deck(f, 0, W, ra, rb, h=3.0, tone=DECK * 0.9, plate=40, seed=4191, edge=False)
    deck(f, 168, 312, ra, rb, h=5.0, tone=DECK, plate=24, seed=4193)
    w = f.win(168, 312, ra, rb)
    sl, X, R = w
    for rx in (204, 276):
        f.paint(sl, aa(np.abs(X - rx) - 1.2), None, GOLD)
    chev = (np.abs(np.mod(R - np.abs(X - 240) * 0.8, 120) - 8) < 3) & (np.abs(X - 240) < 30)
    f.paint(sl, chev.astype(float) * 0.85, None, GOLD_DIM)
    for r in np.arange(ra + 60, rb - 20, 120):
        for x in (176, 304):
            f.lamp(x, r, WHITE_LAMP, 1.4, 0.6)
    rng = np.random.default_rng(4195)
    # the hangar blocks: (x0, x1, row0, row1, side) - each with an open bay toward the launch deck
    r = ra + 140
    blocks = []
    while r < rb - 200:
        hgt = rng.integers(300, 460)
        blocks.append((10, 150, r, min(r + hgt, rb - 40), "w"))
        blocks.append((330, 470, r + 160, min(r + 160 + hgt, rb - 40), "e"))
        r += hgt + 90
    for x0, x1, r0, r1, side in blocks:
        if r1 - r0 < 160:
            continue
        hangar(f, x0, x1, r0, r1, side, rng)


def hangar(f, x0, x1, r0, r1, side, rng):
    cx, cr = (x0 + x1) / 2, (r0 + r1) / 2
    box(f, cx, cr, (x1 - x0) / 2, (r1 - r0) / 2, 18.0, BLACK, rim=GOLD, rim_w=2.0)
    w = f.win(x0 + 4, x1 - 4, r0 + 4, r1 - 4)
    sl, X, R = w
    rib = 0.5 + 0.5 * np.cos((R - r0) / 6.0 * np.pi)
    f.paint(sl, np.ones(X.shape), 18.0, ROOF * (0.86 + 0.18 * rib[..., None]))
    # the open bay: a recess in the roof toward the deck, a Talon parked inside
    bh = 92
    br = r0 + rng.uniform(40, max(41, r1 - r0 - bh - 40))
    bx0, bx1 = (x1 - 78, x1) if side == "w" else (x0, x0 + 78)
    box(f, (bx0 + bx1) / 2, br + bh / 2, (bx1 - bx0) / 2, bh / 2, -2.0, BAY)
    box(f, (bx0 + bx1) / 2, br + bh / 2, (bx1 - bx0) / 2 - 4, bh / 2 - 4, -2.0, DECK_DARK * 0.62)   # the lit floor
    w = f.win(bx0, bx1, br, br + bh)
    sl, X, R = w
    f.paint(sl, aa(np.abs(R - br - bh / 2) - 0.8) * 0.7, None, GOLD_DIM)                       # its taxi line
    for k in range(3):
        f.lamp(bx0 + 10 + 29 * k, br + bh - 6, WHITE_LAMP, 1.2, 0.5)
    parked_talon(f, (bx0 + bx1) / 2 + (6 if side == "w" else -6), br + bh / 2)
    for k in range(int((r1 - r0) // 120)):
        f.lamp(x0 + 6 if side == "w" else x1 - 6, r0 + 60 + 120 * k, RED_LAMP, 1.5, 0.8)


def ring(f, ra, rb):
    """The citadel ring: the ring road curving round the core's black wall (its arc on the road's right), terraces and
    barracks roofs on the outside, buttresses and lamps."""
    deck(f, 0, W, ra, rb, h=3.0, tone=DECK * 0.88, plate=48, seed=4201, edge=False)
    pts = smooth_path(road_paths()["ring-road"])
    # the core's wall: a long curve 64 px right of the road, then the core's mass beyond it
    core_pts = [(x + 70, r) for x, r in pts if ra + 160 < r < rb - 120]
    if core_pts:
        right = core_pts + [(W + 20, core_pts[-1][1]), (W + 20, core_pts[0][1])]
        poly(f, right, 26.0, BLACK)
        inner = [(x + 14, r) for x, r in core_pts] + [(W + 20, core_pts[-1][1] - 14), (W + 20, core_pts[0][1] + 14)]
        poly(f, inner, 26.0, ROOF)
        for x, r in core_pts[::5]:
            box(f, x - 2, r, 9, 14, 26.0, BLACK, rim=GOLD, rim_w=1.5)
            f.lamp(x - 10, r, GOLD_LAMP, 1.5, 0.8)
        core_roof(f, core_pts)
    # the core's terrace (the SAMs, the Sentinel): a lighter deck set into the core's roof
    srow = t_row(84, HALF["sam-nest"])
    deck(f, 290, 422, srow - 34, srow + 34, h=26.5, tone=DECK, plate=24, seed=4203)
    snt = guard_at("sentinel-tower", 80.0, 352)
    tower(f, snt[0], snt[1], r=44, h=30.0, pad=True)
    road(f, pts, 34, 4.0, ROAD, lamps=64, rim=GOLD_DIM)
    # outside the road: barracks roofs in rows
    rng = np.random.default_rng(4205)
    r = ra + 40
    while r < rb - 60:
        x = path_x(pts, r)
        left = x - 34 - 18
        if left > 60:
            hh = rng.integers(90, 170)
            box(f, left / 2, r + hh / 2, left / 2 - 10, hh / 2 - 6, 14.0, BLACK, rim=GOLD_DIM, rim_w=1.5,
                top=ROOF * 1.05)
            f.lamp(left - 14, r + 10, RED_LAMP, 1.4, 0.7)
            r += hh + 14
        else:
            r += 60


def core_roof(f, core_pts):
    """The citadel core's roof beyond its wall: plating seams parallel to the wall, gold light strips, raised vent
    blocks and dishes, red lamps (the core's mass would read flat without them)."""
    rows = np.array([r for _, r in core_pts])
    xs = np.array([x for x, _ in core_pts])
    w = f.win(xs.min(), W, rows.min(), rows.max())
    sl, X, R = w
    xw = np.interp(R, rows, xs)
    d = X - xw                                                # px inside the wall's line
    inside = smoothstep(16, 20, d)
    seam = (np.abs(np.mod(d - 20, 46) - 23) > 22.2) * inside
    f.paint(sl, seam * 0.8, None, BLACK * 0.7)
    cross = (np.mod(R, 64) < 1.2) * inside
    f.paint(sl, cross * 0.6, None, BLACK * 0.7)
    strip = aa(np.abs(d - 40) - 1.0) * inside
    f.paint(sl, strip, None, GOLD_DIM)
    rng = np.random.default_rng(4207)
    for k in range(0, len(core_pts), 3):
        x, r = core_pts[k]
        for off in (78, 150, 228):
            cx = x + off + rng.uniform(-10, 10)
            if cx > W - 16:
                continue
            kind = rng.uniform()
            if kind < 0.45:
                box(f, cx, r, rng.uniform(12, 22), rng.uniform(10, 18), 32.0, BLACK, rim=GOLD_DIM, rim_w=1.2,
                    top=ROOF * 1.15)
            elif kind < 0.6:
                dish(f, cx, r, 11)
            elif kind < 0.75:
                f.lamp(cx, r, RED_LAMP, 1.6, 0.9)


def court(f, ra, rb):
    """The inner court before Helix HQ: a paved court with lamp rows, the Graft nest's corner, Varga's old research
    wing (its roof cracked open over the lab vault), the Sentinel's tower, the Halo's mount."""
    deck(f, 0, W, ra, rb, h=3.0, tone=DECK * 0.96, plate=36, seed=4211, edge=False)
    vx, vrow = VAULT[1], t_row(VAULT[0], HALF["lab-vault"])
    # the research wing: a long block on the right, its roof cracked and fallen in round the vault
    x0, x1, r0, r1 = 330, 474, vrow - 110, vrow + 110
    box(f, (x0 + x1) / 2, (r0 + r1) / 2, (x1 - x0) / 2, (r1 - r0) / 2, 16.0, BLACK, rim=GOLD_DIM, rim_w=1.5,
        top=ROOF * 1.1)
    w = f.win(x0 + 6, x1 - 6, r0 + 6, r1 - 6)
    sl, X, R = w
    n = raster.fbm(X.shape[1], X.shape[0], 10, 4213, octaves=3, period=False)
    hole = smoothstep(1.0, 0.85, np.hypot((X - vx) / 46, (R - vrow) / 40) + (n - 0.5) * 0.5)
    f.paint(sl, hole, 1.0, np.array((70, 76, 88.0)) * (0.8 + 0.4 * n[..., None]))       # the lab's floor, lit
    crack = smoothstep(0.03, 0.0, np.abs(n - 0.5)) * smoothstep(1.6, 1.0, np.hypot((X - vx) / 46, (R - vrow) / 40))
    f.paint(sl, crack * (1 - hole), 10.0, BAY)
    for k in range(5):
        f.lamp(x0 + 14 + 28 * k, r0 + 10, (150, 200, 255), 1.3, 0.5)
    # the walkway from Gate Three to the mount: dark paving with gold borders and a medallion halfway
    mrow = mount_row()
    road(f, [(240, ra), (240, mrow - 150)], 34, 3.5, DECK_DARK, lanes=False, rim=GOLD_DIM)
    disc(f, 240, (ra + mrow - 150) / 2, 30, None, GOLD_DIM, r_in=27)
    disc(f, 240, (ra + mrow - 150) / 2, 18, None, GOLD_DIM, r_in=16)
    # the court's west range: a low colonnade block along the left edge, a parapet along the right
    box(f, 14, (ra + mrow) / 2, 14, (mrow - ra) / 2 - 60, 14.0, BLACK, rim=GOLD_DIM, rim_w=1.5, top=ROOF)
    for r in np.arange(ra + 80, mrow - 60, 48):
        f.lamp(30, r, GOLD_LAMP, 1.4, 0.6)
    # the Graft nest's corner (left) with its growth, the Sentinel's tower
    grow = t_row(104.2, 22)
    growth(f, 88, grow, 60, 50, 4215, h=4.0)
    snt = guard_at("sentinel-tower", 108.0, 112)
    tower(f, snt[0], snt[1], r=44, h=26.0)
    # lamp rows down the court
    for r in np.arange(ra + 80, rb - 260, 90):
        for x in (176, 304):
            box(f, x, r, 3, 3, 10.0, BLACK)
            f.lamp(x, r, GOLD_LAMP, 1.6, 0.8)
    # the Halo's mount: a round plinth over the main core, a recessed ring channel, radial ribs, a gold rim
    mx, mr = HALO_CORE[0], mount_row()
    disc(f, mx, mr, 150, 6.0, DECK_DARK)
    disc(f, mx, mr, 150, None, GOLD, r_in=146)
    disc(f, mx, mr, 128, -6.0, np.array((40, 42, 50.0)))
    w = f.win(mx - 130, mx + 130, mr - 130, mr + 130)
    sl, X, R = w
    d = np.hypot(X - mx, R - mr)
    ang = np.arctan2(R - mr, X - mx)
    ribs = (np.abs(np.mod(ang * 12 / TAU, 1) - 0.5) > 0.44) & (d < 126) & (d > 50)
    f.paint(sl, ribs.astype(float), -2.0, BLACK)
    f.paint(sl, aa(np.abs(d - 96) - 1.5), None, GOLD_DIM)
    disc(f, mx, mr, 46, 2.0, np.array((26, 40, 46.0)))
    for a in np.linspace(0, TAU, 16, endpoint=False):
        f.lamp(mx + 148 * np.cos(a), mr + 148 * np.sin(a), RED_LAMP if a % (TAU / 4) < 1e-6 else GOLD_LAMP, 1.6,
               0.8)


def hq(f, ra, rb):
    """Helix HQ: stepped roofs of different heights with comm masts and dishes, a central avenue, then the dock and
    the empty berth."""
    bx0, bx1, br0, br1 = berth()
    deck(f, 0, W, ra, rb, h=3.0, tone=DECK * 0.92, plate=40, seed=4221, edge=False)
    road(f, [(240, ra), (240, br0 - 20)], 30, 4.0, ROAD, lamps=72, rim=GOLD_DIM)
    rng = np.random.default_rng(4223)
    for side, (x0, x1) in (("w", (6, 196)), ("e", (284, 474))):
        r = ra + 60 + (0 if side == "w" else 140)
        while r < br0 - 220:
            hh = int(rng.integers(220, 420))
            hgt = float(rng.choice([16.0, 22.0, 30.0]))
            cx, cr = (x0 + x1) / 2, r + hh / 2
            box(f, cx, cr, (x1 - x0) / 2, hh / 2, hgt, BLACK, rim=GOLD, rim_w=2.0, top=ROOF)
            # a raised step on the roof
            sx = cx + rng.uniform(-30, 30)
            box(f, sx, cr + rng.uniform(-40, 40), 50, min(70, hh / 2 - 30), hgt + 10, BLACK, rim=GOLD_DIM, rim_w=1.5,
                top=ROOF * 1.08)
            mast(f, x0 + 24 if side == "w" else x1 - 24, r + hh - 40, 56.0 + rng.uniform(0, 14))
            dish(f, (x0 + x1) / 2 + (40 if side == "w" else -40), r + 50, 14)
            for k in range(int(hh // 70)):
                f.lamp(x1 - 5 if side == "w" else x0 + 5, r + 30 + 70 * k, GOLD_LAMP, 1.4, 0.7)
            r += hh + 70
    # the guards' roof spots (lighter deck squares)
    for kind, _, at in GUARDS:
        for t, x in at:
            if t < 115:
                continue
            gx, gr = guard_at(kind, t, x)
            if kind == "sentinel-tower":
                tower(f, gx, gr, r=44, h=34.0)
            elif kind in ("sam-nest", "graft-turret"):
                box(f, gx, gr, 30, 30, None, DECK, ch=8)
    for t, x in next(at for k, n, at in GUARDS if k == "graft-turret" and at[0][0] > 135):
        growth(f, x, t_row(t, 22), 34, 26, 4225 + int(x), h=4.0)
    # the dock: the apron round the berth, the gantries, the well, the clamp arms (open), the umbilicals
    deck(f, 40, 440, br0 - 90, br1 + 60, h=4.0, tone=DECK, plate=32, seed=4227)
    for gx in (bx0 - 34, bx1 + 34):
        box(f, gx, (br0 + br1) / 2, 18, (br1 - br0) / 2 + 30, 26.0, BLACK, rim=GOLD, rim_w=2.0)
        for r in np.arange(br0, br1 + 1, 50):
            f.lamp(gx + (10 if gx > 240 else -10), r, GOLD_LAMP, 1.5, 0.8)
            box(f, gx + (-22 if gx > 240 else 22), r, 8, 4, 22.0, BLACK)     # the umbilical arms
    w = f.win(bx0 - 4, bx1 + 4, br0 - 4, br1 + 4)
    sl, X, R = w
    sd = sd_box(X - (bx0 + bx1) / 2, R - (br0 + br1) / 2, (bx1 - bx0) / 2, (br1 - br0) / 2, 40)
    lip = aa(sd + 0.5) * aa(-sd - 6)
    stripe = (np.mod(X + R, 18) < 9)
    f.paint(sl, lip * stripe, -1.0, GOLD)
    f.paint(sl, lip * ~stripe, -1.0, BLACK)
    hole = aa(sd + 6)
    f.alpha[sl] = np.minimum(f.alpha[sl], 1 - hole)
    f.alb[sl] = f.alb[sl] * (1 - hole[..., None])
    f.hgt[sl] = f.hgt[sl] * (1 - hole) + (-60.0) * hole
    # the clamp arms swung open: heavy gold-trimmed black arms hinged on the rim, pointing outward
    for k, r in enumerate(np.linspace(br0 + 70, br1 - 70, 4)):
        for side in (-1, 1):
            hx = bx0 + 2 if side < 0 else bx1 - 2
            ang = np.radians(200 if side < 0 else -20) + (0.15 if k % 2 else -0.1) * side
            ex, er = hx + 52 * np.cos(ang), r + 52 * np.sin(ang)
            pts = [(hx, r - 8), (ex, er - 6), (ex, er + 6), (hx, r + 8)]
            poly(f, pts, 12.0, BLACK)
            poly(f, [(hx, r - 3), (ex, er - 2), (ex, er + 2), (hx, r + 3)], 13.0, GOLD)
            f.lamp(ex, er, RED_LAMP, 1.6, 0.9)
    for k in range(5):                                        # the loose umbilicals lying over the rim
        x = bx0 + 18 + k * 31
        r = br1 - 2 + (k % 2) * 4
        road(f, [(x, r + 30), (x + 4, r), (x - 6, r - 26)], 2.2, 6.0, np.array((40, 40, 46.0)), lanes=False)
    f.lamp(bx0 + 10, br1 + 14, RED_LAMP, 2.0, 1.0)
    f.lamp(bx1 - 10, br1 + 14, RED_LAMP, 2.0, 1.0)


# =========================================================================== shading

def shade(hgt, alb, emis):
    """Hill-shade the height field under the low top-left Sun with long cast shadows; emissive added after."""
    hgt = hgt.astype(np.float32)
    gy = (np.roll(hgt, -1, axis=0) - np.roll(hgt, 1, axis=0)) / 2
    gx = np.gradient(hgt, axis=1)
    n = np.dstack([-gx, -gy, np.ones_like(hgt)])
    n /= np.linalg.norm(n, axis=-1, keepdims=True)
    rel = np.clip((n @ SUN.astype(np.float32)) / SUN[2], 0, 2.0)
    lit = np.ones_like(hgt)
    hz = np.hypot(SUN[0], SUN[1])
    sx, sy, sz = -SUN[0] / hz, -SUN[1] / hz, SUN[2] / hz
    hmax = float(hgt.max())
    for s in range(1, 170):
        if s * sz > hmax + 30:
            break
        dx, dy = int(round(sx * s)), int(round(sy * s))
        shifted = np.roll(np.roll(hgt, dy, axis=0), dx, axis=1)
        if dx > 0:
            shifted[:, :dx] = -1e9
        lit = np.minimum(lit, np.clip(1 - (shifted - hgt - s * sz) / 1.5, 0, 1))
    lit = blur(lit.astype(np.float64), 0.7).astype(np.float32)
    direct = np.clip(rel, 0, 1.6) * lit
    light = AMBIENT * SHADOW_TINT[None, None, :] + (1 - AMBIENT) * direct[..., None]
    return alb * light + emis


_FIELD = {}


def field():
    if not _FIELD:
        f = build_field()
        rgb = shade(f.hgt, f.alb, f.emis).astype(np.float64)
        for x, row, col, r, k in f.lamps:
            glow(rgb, x, f.hi - 1 - row + 0.5, r, col, k)
        _FIELD.update(f=f, rgb=rgb)
    return _FIELD["f"], _FIELD["rgb"]


def rows_of(rgb, f, lo, hi, alpha=True):
    """The field's ground rows [lo, hi) as an image (top row = the highest)."""
    i0, i1 = f.hi - hi, f.hi - lo
    a = f.alpha[i0:i1] if alpha else np.ones((i1 - i0, W))
    return rgba(rgb[i0:i1], a)


# =========================================================================== the doors

DOOR_W, DOOR_H = 208, 104


def door_box(k):
    """The door piece's window: x [160, 368), rows [wall - 18 - 64, wall + 18 + 4): the leaves and their long shadow
    down and right (2.6 x 24 px), so every frame's edges are the fortress's own pixels."""
    wrow = int(round(gate_rows(k)))
    return 160, 160 + DOOR_W, wrow + WALL_THICK // 2 + 4 - DOOR_H, wrow + WALL_THICK // 2 + 4


def door_frames(k):
    """The blast doors of gate k: 12 frames, the leaves parting by smoothstep(frame / 11) (the scroll's ease)."""
    f, rgb_all = field()
    g = GATES[k]
    gx0, gx1 = g["gate"]
    wrow = gate_rows(k)
    x0, x1, ra, rb = door_box(k)
    pa, pb = ra - PAD, rb + PAD
    i0, i1 = f.hi - pb, f.hi - pa
    frames = []
    for n in range(DOOR_FRAMES):
        o = float(smoothstep(0, 1, n / (DOOR_FRAMES - 1)))
        hgt = f.hgt[i0:i1].copy()
        alb = f.alb[i0:i1].copy()
        emis = f.emis[i0:i1].copy()
        sub = Field(pa, pb)
        sub.hgt, sub.alb, sub.emis = hgt, alb, emis
        half = (gx1 - gx0) / 2
        mid = (gx0 + gx1) / 2
        leaves = []
        if o < 0.999:
            leaves = [(gx0, mid - half * o, 1), (mid + half * o, gx1, -1)]
        for a, b, side in leaves:
            if b - a < 0.5:
                continue
            w = sub.win(a, b, wrow - WALL_THICK / 2 + 2, wrow + WALL_THICK / 2 - 2)
            if w is None:
                continue
            sl, X, R = w
            m = aa(np.maximum(a - X, X - b))
            col = BLACK * np.ones(X.shape + (3,))
            # heavy gold banding: the leaf's frame, three horizontal bands, the crest's half at its meeting edge
            edge = np.minimum.reduce([X - a, b - X, R - (wrow - WALL_THICK / 2 + 2), (wrow + WALL_THICK / 2 - 2) - R])
            col = np.where((edge < 2.5)[..., None], GOLD, col)
            band = (np.abs(np.mod(R - wrow + 16, 11) - 5.5) < 1.0)
            col = np.where(band[..., None], GOLD * 0.75, col)
            meet = mid - half * o if side > 0 else mid + half * o
            crest = np.hypot(X - meet, R - wrow) < 11
            col = np.where(crest[..., None], GOLD, col)
            crest_in = np.hypot(X - meet, R - wrow) < 7
            col = np.where(crest_in[..., None], BLACK * 1.4, col)
            sub.paint(sl, m, 24.0, col)
        rgb = shade(sub.hgt, sub.alb, sub.emis).astype(np.float64)
        for x, row, colour, r, kk in f.lamps:
            if pa - 20 < row < pb + 20:
                glow(rgb, x, sub.hi - 1 - row + 0.5, r, colour, kk)
        if 0 < n < DOOR_FRAMES - 1:                           # the leaves' red warning lamps while they move
            for a, b, side in leaves:
                ex = b - 3 if side > 0 else a + 3
                for rr in (wrow - 10, wrow + 10):
                    glow(rgb, ex, sub.hi - 1 - rr + 0.5, 1.8, RED_LAMP, 1.0)
        j0 = sub.hi - rb
        frames.append(rgba(rgb[j0:j0 + DOOR_H, x0:x1], np.ones((DOOR_H, x1 - x0))))
    return frames


# =========================================================================== deep: the berth's depth, the drive glow

def berth_depth(seed=4231, h=512):
    """The berth well's depth: black-blue, a faint vertical ice texture, a few dim lamps far down; wraps."""
    rng = np.random.default_rng(seed)
    n = raster.fbm(W, h, 32, seed, octaves=4, period=True)
    rgb = np.zeros((h, W, 3)) + np.array((6, 8, 14.0)) + (n[..., None] * np.array((10, 12, 18.0)))
    light = np.zeros((h, W, 3))
    for _ in range(18):
        x, y = rng.uniform(0, W), rng.uniform(0, h)
        c = RED_LAMP if rng.uniform() < 0.4 else GOLD_LAMP
        for dy in (-h, 0, h):
            glow(light, x, y + dy, 1.4, c, 0.25)
    return rgba(np.clip(rgb + light, 0, 255), np.ones((h, W)))


GLOW_SIZE = (64, 176)


def drive_glow():
    """The Ascendant's gold drive glow climbing away: a white-gold point at the top, a soft gold halo, its plume
    trailing down (additive, unquantised)."""
    w, h = GLOW_SIZE
    light = np.zeros((h, w, 3))
    cx, cy = w / 2, 30
    glow(light, cx, cy, 3.0, (255, 250, 230), 1.6)
    glow(light, cx, cy, 9.0, (255, 196, 90), 0.75)
    glow(light, cx, cy, 20.0, (200, 130, 40), 0.30)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    u = np.clip((yy - cy) / (h - cy - 8), 0, 1)
    plume = np.exp(-((xx - cx) / (2.5 + 9 * u)) ** 2) * (1 - u) ** 1.6 * (yy > cy)
    light += plume[..., None] * np.array((230, 160, 70.0)) * 0.55
    fade = smoothstep(h, h - 24, yy) * smoothstep(0, 6, yy) * smoothstep(0, 6, xx) * smoothstep(w, w - 6, xx)
    return additive_rgb(light * fade[..., None])


def glow_place():
    """The drive glow's placement: in the berth's well at t~144 (y ~330 on the screen), climbing ~40 px/s on its
    path against the deep layer's 21 px/s drift, so it rises out through the well's top edge by the level's end."""
    bx0, bx1, br0, br1 = berth()
    t0, y0 = 143.0, 360.0
    deep_row = scroll(t0) * FACTORS["deep"] + (SCREEN - y0)
    pt = round(CLOCK.time_at((deep_row - SCREEN / 2) / FACTORS["deep"]), 3)
    return {"piece": "drive-glow", "t": pt, "x": 240, "path": [[141.0, 0, -80], [150.0, 0, 280]],
            "note": "the Ascendant's drive glow climbing away, seen only through the empty berth's well (t~143-146)"}


# =========================================================================== build

PIECE_SPEC = {}


def fortress_cuts():
    lo, hi = ROW_LO, row_hi()
    n = int(np.ceil((hi - lo) / MAX_PIECE))
    edges = [lo + int(round((hi - lo) * k / n)) for k in range(n + 1)]
    return list(zip(edges, edges[1:]))


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()
    PIECE_SPEC.clear()
    placed = []

    def save(name, img, layer=None, frames=None, fps=None):
        if frames:
            for i, fr in enumerate(img):
                artkit.save_png(fr, OUT / f"{name}_{i}.png", SOURCE)
            size = img[0].size
        else:
            artkit.save_png(img, OUT / f"{name}.png", SOURCE)
            size = img.size
        if layer:
            spec = {"layer": layer, "size": list(size)}
            if frames:
                spec.update(frames=frames, fps=fps)
            PIECE_SPEC[name] = spec
        print(f"{name}: {size[0]}x{size[1]}" + (f" x{frames}" if frames else ""))

    save("berth-depth", berth_depth())
    f, rgb = field()
    for k, (a, b) in enumerate(fortress_cuts()):
        name = f"fortress-{k + 1:02d}"
        save(name, rows_of(rgb, f, a, b), "ground")
        placed.append({"piece": name, "t": exact_t(a, b - a), "x": 240,
                       "note": f"the fortress, ground rows {a}..{b - 1} (exact: its bottom row on {a})"})
    doors = []
    for k in range(3):
        name = f"gate-{k + 1}-doors"
        save(name, door_frames(k), "ground", DOOR_FRAMES, DOOR_FPS)
        x0, x1, ra, rb = door_box(k)
        doors.append({"gate": k + 1, "piece": name, "t": exact_t(ra, rb - ra), "x": (x0 + x1) / 2,
                      "seconds": DOOR_FRAMES / DOOR_FPS})
    save("drive-glow", drive_glow(), "deep")
    placed.append(glow_place())
    return dict(PIECE_SPEC), placed, doors


# =========================================================================== the proposal

def fmt(v):
    if isinstance(v, (list, tuple)):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, dict):
        return "{" + ", ".join(f"{k}: {fmt(x)}" for k, x in v.items()) + "}"
    if isinstance(v, (float, np.floating)):
        return f"{float(v):g}"
    if isinstance(v, (int, np.integer)):
        return str(int(v))
    if isinstance(v, str) and any(c in v for c in "':#,{}[]"):
        return '"' + v + '"'
    return str(v)


def gate_entries(k):
    """Each pylon and covered unit of gate k with its held-screen place, its ground row and its entry time."""
    g = GATES[k]
    out = []
    for p in g["pylons"]:
        cx, cy = facing_centre(p["x"], p["y"], p["facing"])
        kind, note, units = p["covers"]
        out.append({"pylon": p, "dome": (cx, cy), "kind": kind, "note": note, "units": units})
    return out


def proposal_text(pieces, placed, doors):
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    L = ["backdrop:",
         f"  # Presentation only: Level 41's Callisto fortress (tools/art/backdrop_l41.py, step ART-G2, sections from {src}).",
         "  # The ground is ONE continuous fortress laid out on the level's clock, shaded once under the low Sun (long",
         "  # shadows to the bottom right) and cut into fortress-NN ground pieces of at most 2,040 rows placed back to",
         "  # back on exact rows (no ground tile set), opaque from row -16 (no Jupiter: the ice covers the screen from",
         "  # t 0, the user 2026-11-01) except the empty berth's well, where berth-depth (deep) and the drive glow show.",
         "  # Re-run the generator after moving a section: the fortress, the gates and the doors follow the clock.",
         "  # The doors (G4's hook, presentation only, not placed): each gate's door piece is drawn at its row from the",
         "  # level start on frame 0 (closed); on that gate's GATE_OPENED its frames play once at 6 fps (2 s, the",
         "  # leaves parting along the scroll's own ease) and the last frame (open) holds.",
         "  # Motion: the doors only when a gate opens; the drive glow climbing through the berth (t~143-146).",
         "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, low-air, high-air unused",
         "  ramp: 2",
         "  haze_colour: 1c2230   # unused: vacuum, no haze",
         "  atmosphere:",
         "    clear: {haze: 0.0}",
         "  tile_sets:",
         f"    berth-depth: {{layer: deep, height: {Image.open(OUT / 'berth-depth.png').height}}}   # the berth well's dark "
         "depth (seen only through the well)",
         "  pieces:"]
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        L.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    L.append("  placed:")
    for p in placed:
        if p.get("note"):
            L.append(f"    # {p['note']}")
        extra = f", path: {fmt(p['path'])}" if p.get("path") else ""
        L.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {fmt(float(p['x']))}{extra}}}")
    L.append("  doors:   # G4: {gate: n (the held gates' order, GATE_OPENED's value), piece, t, x}; frame 0 until the")
    L.append("  # gate opens, then the frames once over `seconds`, the last held")
    for d in doors:
        L.append(f"    - {{gate: {d['gate']}, piece: {d['piece']}, t: {d['t']}, x: {fmt(float(d['x']))}, "
                 f"seconds: {fmt(d['seconds'])}}}")
    L.append("sections:   # ART-G2's reading of the README's Layout (each halt a 2 s section at speed 0: the clock stands")
    L.append("  # at its end while the gate holds); G2 writes the real ones (arena: true and the holders)")
    for s in SECTIONS:
        L.append("  - " + "{" + ", ".join(f"{k}: {fmt(v)}" for k, v in s.items() if k != "notes") + "}")
    L.append("gates:   # each gate's held screen (480x540, y down from the top edge, the clock standing at `halt`):")
    L.append("  # the pylons on their towers (x, y; `facing` in degrees clockwise from +x, `crown` its shield_pylon.py facing")
    L.append("  # k = k x 22.5 degrees clockwise from straight down; the dome's centre")
    L.append("  # 140 px along it), what each dome covers (its units within 95 px of its centre: easy's radius), the")
    L.append("  # archive core's well. `at: [t, x]` is the entry (the centre entering half its size above the top")
    L.append("  # edge: pylon 56, Warden 64, Rail Bunker 72, SAM/Graft 44, the core's hatch assumed 40); the Wardens")
    L.append("  # drive in and park at their `park` places (`row`: the ground row).")
    for k, g in enumerate(GATES):
        L.append(f"  - name: {g['name']}")
        L.append(f"    gate: {k + 1}")
        L.append(f"    halt: {halt_t(k):g}   # the held screen shows ground rows {int(round(halt_top(k) - SCREEN))}.."
                 f"{int(round(halt_top(k)))}; the wall at y {g['wall']} (row {int(round(gate_rows(k)))})")
        L.append("    pylons:")
        for e in gate_entries(k):
            p = e["pylon"]
            row = hrow(k, p["y"])
            dx, dy = e["dome"]
            crown = int(round(((p["facing"] - 90) % 360) / 22.5)) % 16
            L.append(f"      - {{id: {p['id']}, x: {p['x']}, y: {p['y']}, facing: {fmt(p['facing'])}, crown: {crown}, "
                     f"at: [{t_of(row, HALF['shield-pylon']):g}, {p['x']}], dome: [{dx:.0f}, {dy:.0f}]}}")
        L.append("    covered:")
        for e in gate_entries(k):
            kind = e["kind"]
            places = []
            for x, y in e["units"]:
                row = hrow(k, y)
                if kind == "warden-tank":
                    places.append(f"{{park: [{x}, {y}], row: {row:.0f}}}")
                else:
                    places.append(f"{{x: {x}, y: {y}, at: [{t_of(row, HALF[kind]):g}, {x}]}}")
            L.append(f"      - {{under: {e['pylon']['id']}, enemy: {kind}, places: [{', '.join(places)}]}}"
                     f"   # {e['note']}")
        cx, cy = g["core"]
        L.append(f"    core: {{x: {cx}, y: {cy}, at: [{t_of(hrow(k, cy), HALF['archive-core']):g}, {cx}]}}"
                 f"   # archive core {'ABC'[k]}'s well (the hatch and its glow are the game's)")
        L.append(f"    doors: {doors[k]['piece']}")
    hr = mount_row()
    L.append(f"halo: {{x: {HALO_CORE[0]}, y: {HALO_CORE[1]}, halt: {halt_t(3):g}, at: [{t_of(hr, HALO_R):g}, "
             f"{HALO_CORE[0]}], row: {hr:.0f}}}   # its core on the held screen over the mount (r 150 plinth, the ring "
             "channel at r 96)")
    L.append("ground_placements:   # the other ground targets, placed on the fortress's decks, roofs and walls: at: [t, x];")
    L.append("  # a convoy's x follows its road (`paths`), so it is left to G2's WalkPath")
    for kind, note, at in GUARDS:
        L.append(f'  - target: "{note}"')
        L.append(f"    enemy: {kind}")
        L.append("    at: [" + ", ".join(f"[{t:g}, {fmt(x) if x is not None else 'path'}]" for t, x in at) + "]")
    vx = VAULT[1]
    L.append("ground_objects:")
    L.append(f"  - {{t: {VAULT[0]:g}, x: {vx}, size: [40, 40], hp: 20, hard: {{hp: 28}}, crate: 100, secret: lab vault}}"
             "   # in the research wing's cracked-open roof (its floor lit round it)")
    L.append(f"  - {{t: 20, x: {BEACON[0]}, y_up: {BEACON[1]}, drop: armour-patch, look: beacon}}   # the CDF beacon on "
             "the causeways' fork")
    L.append("paths:   # the roads as polylines [x, ground row] (the Wardens' WalkPaths follow them)")
    for name, pts in road_paths().items():
        L.append(f"  {name}: [" + ", ".join(f"[{x:.0f}, {r:.0f}]" for x, r in pts) + "]")
    L.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); the fortress rows {ROW_LO}.."
             f"{row_hi() - 1}; deep {int(round(scroll(END) * FACTORS['deep']))}")
    return "\n".join(L) + "\n"


def write_proposal(pieces, placed, doors):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed, doors), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    return yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))


# =========================================================================== composite

_CACHE = {}


def img(name, k=None):
    key = (name, k)
    if key not in _CACHE:
        f = OUT / (f"{name}_{k}.png" if k is not None else f"{name}.png")
        _CACHE[key] = Image.open(f).convert("RGBA")
    return _CACHE[key]


def frames_of(name, spec):
    n = spec.get("frames")
    return [img(name, k) for k in range(n)] if n else [img(name)]


def tile_rows(name, offset):
    arr = np.array(img(name))
    h = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[h - 1 - (rows % h)]


def path_offset(p, t):
    pts = p.get("path")
    if not pts:
        return 0.0, 0.0
    if t <= pts[0][0]:
        return pts[0][1], pts[0][2]
    for a, b in zip(pts, pts[1:]):
        if t < b[0]:
            u = (t - a[0]) / (b[0] - a[0])
            return a[1] + (b[1] - a[1]) * u, a[2] + (b[2] - a[2]) * u
    return pts[-1][1], pts[-1][2]


def piece_xy(p, factor, t):
    dx, dy = path_offset(p, t)
    return p["x"] + dx, SCREEN - (scroll(p["t"]) * factor + SCREEN / 2 + dy - scroll(t) * factor)


def held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def compose(t, prop=None, units=True, door_open=None, dead=(), clock=None):
    """The screen at level time t: deep (berth-depth and the drive glow), the fortress pieces, the doors (frame per
    ``door_open``: {gate: seconds since it opened}), the stand-ins (``dead``: pylon ids 'g<k><id>' fallen)."""
    prop = prop or load_proposal()
    bd = prop["backdrop"]
    specs, placed = bd["pieces"], bd["placed"]
    canvas = Image.fromarray(tile_rows("berth-depth", scroll(t) * FACTORS["deep"]).copy(), "RGBA")
    for p in placed:
        sp = specs[p["piece"]]
        if sp["layer"] != "deep":
            continue
        x, y = piece_xy(p, FACTORS["deep"], t)
        canvas = l23kit.add_c(canvas, frames_of(p["piece"], sp)[0], x, y)
    for p in placed:
        sp = specs[p["piece"]]
        if sp["layer"] != "ground":
            continue
        fr = frames_of(p["piece"], sp)[0]
        x, y = piece_xy(p, 1.0, t)
        if y + fr.height / 2 > 0 and y - fr.height / 2 < SCREEN:
            l23kit.paste_c(canvas, fr, x, y)
    door_open = door_open or {}
    for d in bd.get("doors", []):
        sp = specs[d["piece"]]
        fr = frames_of(d["piece"], sp)
        s = door_open.get(d["gate"])
        k = 0 if s is None else min(len(fr) - 1, int(s * sp["fps"]))
        x, y = piece_xy(d, 1.0, t)
        if y + fr[0].height / 2 > 0 and y - fr[0].height / 2 < SCREEN:
            l23kit.paste_c(canvas, fr[k], x, y)
    if units:
        canvas = stand_ins(canvas, t, prop, dead)
    return canvas


def ground_y(row, t):
    return SCREEN - (row - scroll(t))


def stand_pylon(canvas, x, y, facing):
    """The pylon (tools/art/shield_pylon.py: the base and the crown at facing k = k x 22.5 degrees clockwise from
    straight down), or a drawn stand-in while its frames are not there."""
    base, crown = held("shield-pylon"), held("shield-pylon-crown")
    if base and crown:
        n = len(crown)
        k = int(round(((facing - 90) % 360) / 360 * n)) % n
        l23kit.paste_c(canvas, base[0], x, y)
        return l23kit.paste_c(canvas, crown[k], x, y)
    d = ImageDraw.Draw(canvas)
    pts = [(x + 26 * np.cos(a), y + 26 * np.sin(a)) for a in np.linspace(0, TAU, 8, endpoint=False) + TAU / 16]
    d.polygon(pts, fill=(18, 16, 20, 255), outline=(240, 190, 70, 255))
    a = np.radians(facing)
    d.ellipse([x - 9, y - 9, x + 9, y + 9], fill=(255, 200, 80, 255))
    d.line([(x, y), (x + 20 * np.cos(a), y + 20 * np.sin(a))], fill=(255, 220, 120, 255), width=3)
    return canvas


def stand_unit(canvas, kind, x, y, t):
    if kind == "warden-tank":
        hull, tur = held("warden-tank-asc"), held("warden-tank-asc-turret")
        if hull:
            l23kit.paste_c(canvas, hull[0], x, y)
            l23kit.paste_c(canvas, tur[0], x, y)
    elif kind == "rail-bunker":
        b, tur = held("rail-bunker"), held("rail-bunker-turret")
        if b:
            l23kit.paste_c(canvas, b[0], x, y)
            l23kit.paste_c(canvas, tur[0], x, y)
    elif kind == "sam-nest":
        b, s = held("sam-nest-base"), held("sam-nest")
        if b:
            l23kit.paste_c(canvas, b[0], x, y)
        if s:
            l23kit.paste_c(canvas, s[0], x, y)
    elif kind == "graft-turret":
        b, s = held("graft-turret-base"), held("graft-turret")
        if b:
            l23kit.paste_c(canvas, b[0], x, y)
        if s:
            l23kit.paste_c(canvas, s[0], x, y)
    elif kind == "sentinel-tower":
        b, s = held("sentinel-tower"), held("sentinel-tower-head")
        if b:
            l23kit.paste_c(canvas, b[0], x, y)
        if s:
            l23kit.paste_c(canvas, s[0], x, y)
    return canvas


def stand_ins(canvas, t, prop, dead=()):
    dome = held("shield-pylon-dome")
    for k, g in enumerate(GATES):
        for e in gate_entries(k):
            p = e["pylon"]
            pid = f"g{k}{p['id']}"
            for x, yy in e["units"]:
                y = ground_y(hrow(k, yy), t)
                if -60 < y < SCREEN + 60:
                    canvas = stand_unit(canvas, e["kind"], x, y, t)
            y = ground_y(hrow(k, p["y"]), t)
            if -200 < y < SCREEN + 200 and pid not in dead:
                dx, dy = e["dome"]
                yd = ground_y(hrow(k, dy), t)
                if dome:
                    canvas = l23kit.add_c(canvas, dome[int(t * 6) % len(dome)], dx, yd, 0.85)
                canvas = stand_pylon(canvas, p["x"], y, p["facing"])
        cx, cy = g["core"]
        y = ground_y(hrow(k, cy), t)
        hatch = held("archive-core") or held("archive-core-hatch")
        if hatch and -30 < y < SCREEN + 30:
            l23kit.paste_c(canvas, hatch[0], cx, y)
    for kind, _, at in GUARDS:
        for te, x in at:
            gx, gr = guard_at(kind, te, x)
            y = ground_y(gr, t)
            if -60 < y < SCREEN + 60:
                canvas = stand_unit(canvas, kind, gx, y, t)
    y = ground_y(mount_row(), t)
    if -140 < y < SCREEN + 140:
        try:                                                  # the Halo as tools/art/halo_platform.py composes it
            import halo_platform  # noqa: E402
            canvas = halo_platform.platform(canvas, HALO_CORE[0], int(round(y)), 20.0 * t)
        except Exception:                                     # its frames not built yet: no stand-in
            pass
    return canvas


def air_stand_ins(canvas, kinds=("talon", "chimera")):
    for k, kind in enumerate(kinds):
        fr = held(kind)
        if not fr:
            continue
        for j in range(3):
            l23kit.paste_c(canvas, fr[0], 120 + 120 * j, 60 + 44 * k + 20 * abs(j - 1))
    ship = artkit.load_frames("ship")
    if ship:
        l23kit.paste_c(canvas, ship[2], 240, 470)
    return canvas


# =========================================================================== review

SHOTS = [(0.0, "T=0 CALLISTO'S ICE"), (10.5, "T=10.5 THE CRATER-RIM OUTPOST"), (17.5, "T=17.5 THE GATEHOUSE"),
         (26.0, "T=26 THE CAUSEWAYS"), (34.5, "T=34.5 THE OUTER WALL, VRELL GROWTH"), (44.0, "T=44 GATE ONE HALT"),
         (58.0, "T=58 THE HANGAR DECKS"), (76.0, "T=76 GATE TWO HALT"), (88.0, "T=88 THE CITADEL RING"),
         (100.0, "T=100 GATE THREE HALT"), (105.0, "T=105 THE RESEARCH WING"), (114.0, "T=114 THE HALO'S MOUNT"),
         (122.0, "T=122 HQ'S ROOFS AND MASTS"), (134.0, "T=134 HQ, THE SENTINEL"), (143.5, "T=143.5 THE DOCK"),
         (146.0, "T=146 THE EMPTY BERTH")]


def windows(t, w=200, h=96):
    im = compose(t, units=False)
    lum = np.array(im.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    best = []
    for y in range(0, SCREEN - h - 80, 12):
        for x in range(0, W - w, 12):
            best.append((lum[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return best[0], best[-1], lum.mean(), im


def read_cell(bg, x, y):
    """The readability set at 1x over a 200x96 window: a Talon, a Chimera, a Warden Tank, a SAM Nest, amber needles
    and an orb, the ship."""
    c = bg.crop((x, y, x + 200, y + 96)).copy()
    for k, kind in enumerate(("talon", "chimera")):
        fr = held(kind)
        if fr:
            l23kit.paste_c(c, fr[0], 26 + 40 * k, 28)
    stand_unit(c, "warden-tank", 104, 52, 0)
    stand_unit(c, "sam-nest", 30, 74, 0)
    orb, needle = held("orb"), held("needle")
    for k in range(3):
        if needle:
            l23kit.paste_c(c, needle[0], 140 + 10 * k, 18 + 12 * k)
    if orb:
        l23kit.paste_c(c, orb[0], 180, 30)
    ship = artkit.load_frames("ship")
    if ship:
        l23kit.paste_c(c, ship[2], 164, 72)
    return c


def gate_gif(k=0, fps=10):
    """Gate One's halt at half size: 1.5 s held with the domes up, the second pylon falls at 1.5 s (the first fell
    before), the doors part over 2 s as the scroll eases up, 1.5 s on."""
    prop = load_proposal()
    frames = []
    th = halt_t(k)
    pyl = [f"g{k}{p['id']}" for p in GATES[k]["pylons"]]
    for i in range(int(5.0 * fps)):
        s = i / fps
        dead = tuple(pyl[:1]) if s < 1.5 else tuple(pyl)
        if s < 1.5:
            t, opened = th, None
        else:
            t, opened = th + (s - 1.5), s - 1.5
        canvas = compose(t, prop, door_open={k + 1: opened} if opened is not None else None, dead=dead)
        ship = artkit.load_frames("ship")
        if ship:
            l23kit.paste_c(canvas, ship[2], 240, 470)
        frames.append(canvas.resize((W // 2, SCREEN // 2), Image.BOX))
    return frames


def review():
    prop = load_proposal()
    specs = prop["backdrop"]["pieces"]
    shots = []
    for t, lab in SHOTS:
        im = compose(t, prop, dead=() if t in (44.0, 76.0, 100.0) else ())
        if t in (26.0, 58.0, 88.0, 122.0):
            im = air_stand_ins(im)
        shots.append((im, lab))
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    reads_t = [(6.0, "S1"), (28.0, "S2"), (60.0, "S4"), (90.0, "S6"), (106.0, "S8"), (128.0, "S10")]
    read_h = 16 + 2 * (96 + 20)
    doors_h = 16 + 3 * (DOOR_H // 2 + 8) + 10
    cuts = fortress_cuts()
    strip_h = 30 + max(b - a for a, b in cuts) // 8
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + read_h + 30 + doors_h + strip_h + 20,
                         "LEVEL 41 BACKDROP (FINAL R79 A): CALLISTO FORTRESS", f"PRODUCTION ART, {BATCH.upper()}")
    raster.draw_text(sheet, 16, 38, "SIXTEEN SCREENS AT HALF SIZE WITH STAND-INS (PYLONS AND THEIR DOMES, THE COVERED "
                                    "UNITS, WARDENS, RAIL BUNKERS, SAMS, SENTINELS, GRAFTS AT THEIR PROPOSED PLACES)",
                     raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X: EACH STRETCH'S DARKEST (TOP) AND BRIGHTEST (BOTTOM) 200X96 WINDOW "
                                   "WITH A TALON, A CHIMERA, A WARDEN, A SAM, AMBER NEEDLES, AN ORB, THE SHIP",
                     raster.LABEL)
    means = []
    for j, (t, lab) in enumerate(reads_t):
        (dl, dx, dy), (bl, bx, by), mean, bg = windows(t)
        means.append((lab, t, mean, dl, bl))
        for i, (lum, wx, wy) in enumerate(((dl, dx, dy), (bl, bx, by))):
            c = read_cell(bg, wx, wy)
            x0, y0 = 16 + j * 204, y + 16 + i * 116
            sheet.alpha_composite(c, (x0, y0))
            raster.draw_text(sheet, x0, y0 + 98, f"{lab} T={t:.0f} {'DARKEST' if i == 0 else 'BRIGHTEST'} {lum:.0f}",
                             raster.LABEL_DIM)
    y += read_h + 14
    raster.draw_text(sheet, 16, y, "MEANS: " + "  ".join(f"{lab} {m:.0f} ({d:.0f}-{b:.0f})" for lab, _, m, d, b in means),
                     raster.LABEL_DIM)
    y += 16
    raster.draw_text(sheet, 16, y, "THE BLAST DOORS (HALF SIZE): GATE ONE, TWO, THREE; FRAMES 0 (CLOSED) .. 11 (OPEN), "
                                   "6 FPS FROM GATE_OPENED", raster.LABEL)
    for k in range(3):
        name = f"gate-{k + 1}-doors"
        for n, fr in enumerate(frames_of(name, specs[name])):
            if n % 2 and n != DOOR_FRAMES - 1:
                continue
            small = fr.resize((DOOR_W // 2, DOOR_H // 2), Image.BOX)
            x0 = 16 + (n // 2 if n != DOOR_FRAMES - 1 else 6) * (DOOR_W // 2 + 8)
            sheet.alpha_composite(small, (x0, y + 16 + k * (DOOR_H // 2 + 8)))
    y += doors_h
    raster.draw_text(sheet, 16, y, "THE WHOLE FORTRESS (EIGHTH SIZE), FORTRESS-01 (LEFT, FROM THE LEVEL START) .. "
                                   f"FORTRESS-{len(cuts):02d} (RIGHT, THE BERTH AT ITS TOP)", raster.LABEL)
    x = 16
    for k in range(len(cuts)):
        p = img(f"fortress-{k + 1:02d}")
        small = p.resize((p.width // 8, p.height // 8), Image.BOX)
        bg = Image.new("RGBA", small.size, (0, 0, 0, 255))
        bg.alpha_composite(small)
        sheet.alpha_composite(bg, (x, y + 16 + (strip_h - 30) - small.height))
        x += small.width + 6
    png, gif = artkit.review_paths(CONCEPT, "backdrop", VARIANT)
    sheet.convert("RGB").save(png, optimize=True)
    import enemies_r60 as e60  # noqa: E402
    size = e60.rig.write_gif(gate_gif(0), gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")
    for lab, t, m, d, b in means:
        print(f"{lab} t={t}: mean {m:.0f}, darkest window {d:.0f}, brightest {b:.0f}")


# =========================================================================== check

def check():
    """Sizes as the proposal, Source chunks, no checker (DitherGuardTest's measure under 5 %), the drive glow smooth
    (>= 32 levels, additive), the deep tile's wrap, the fortress pieces back to back on exact rows and opaque but for
    the berth's well (whole on the screen at the end), each door frame's edges = the fortress there, the gate rule
    (pylons in the upper half and outside every dome at hard's radius, the domes' centres at y <= 360, the covered units
    within easy's radius of their dome and outside the others' at medium where it says so, the cores outside every
    dome at hard's), at most two animated pieces on the screen, the atlas area."""
    problems = []
    prop = load_proposal()
    bd = prop["backdrop"]
    pieces, placed = bd["pieces"], bd["placed"]
    a = np.array(img("berth-depth"))
    if wrap_score(a) > 1.5:
        problems.append(f"berth-depth: wrap score {wrap_score(a):.2f}")
    for name, spec in pieces.items():
        n = spec.get("frames")
        files = [OUT / f"{name}_{k}.png" for k in range(n)] if n else [OUT / f"{name}.png"]
        for f in files:
            im = Image.open(f)
            if list(im.size) != list(spec["size"]):
                problems.append(f"{f.name}: {im.size} vs {spec['size']}")
    vis = chk = 0
    distinct = {}
    for f in sorted(OUT.glob("*.png")):
        im = Image.open(f)
        if im.text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{f.name}: no Source chunk")
        v, c = checker_share(im)
        if v and c / v >= 0.05:
            problems.append(f"{f.name}: {c / v * 100:.1f} % checker")
        vis, chk = vis + v, chk + c
        distinct.setdefault(im.tobytes() + bytes(str(im.size), "ascii"), (im.width + 2) * (im.height + 2))
    g = np.array(Image.open(OUT / "drive-glow.png"))
    lv = len({tuple(c) for c in g[..., :3].reshape(-1, 3)})
    if lv < 32 or not (g[..., 3] % 255 == 0).all():
        problems.append(f"drive-glow: not a smooth additive glow ({lv} colours)")
    # the fortress back to back, opaque but for the berth
    fort = sorted((p for p in placed if p["piece"].startswith("fortress-")), key=lambda p: p["t"])
    prev_top = None
    for p in fort:
        h = pieces[p["piece"]]["size"][1]
        bottom = int(round(scroll(p["t"]) + SCREEN / 2 - h / 2))
        if prev_top is not None and bottom != prev_top:
            problems.append(f"{p['piece']}: bottom row {bottom}, the piece below ends at {prev_top}")
        prev_top = bottom + h
        al = np.array(img(p["piece"]))[..., 3]
        holes = (al < 255).sum()
        if holes and p is not fort[-1]:
            problems.append(f"{p['piece']}: {holes} transparent px (only the berth's piece has a well)")
    if fort and int(round(scroll(fort[0]["t"]) + SCREEN / 2 - pieces[fort[0]["piece"]]["size"][1] / 2)) > 0:
        problems.append("the fortress starts above row 0")
    if prev_top is not None and prev_top < scroll(END) + SCREEN:
        problems.append(f"the fortress ends at row {prev_top}, the screen at the end reaches {scroll(END) + SCREEN:.0f}")
    bx0, bx1, br0, br1 = berth()
    yb0, yb1 = ground_y(br1, END), ground_y(br0, END)
    if not (0 < yb0 and yb1 < SCREEN):
        problems.append(f"the berth at the end spans y {yb0:.0f}..{yb1:.0f} (not whole on the screen)")
    # the doors' edges against the fortress
    f, rgb = field()
    for d in bd.get("doors", []):
        k = d["gate"] - 1
        x0, x1, ra, rb = door_box(k)
        ref = np.array(rows_of(rgb, f, ra, rb))[:, x0:x1].astype(float)
        for n, fr in enumerate(frames_of(d["piece"], pieces[d["piece"]])):
            a = np.array(fr).astype(float)
            for nm, sl in (("top", np.s_[0, :]), ("bottom", np.s_[-1, :]), ("left", np.s_[:, 0]),
                           ("right", np.s_[:, -1])):
                diff = np.abs(a[sl][..., :3] - ref[sl][..., :3]).mean()
                if diff > 3:
                    problems.append(f"{d['piece']} frame {n}: its {nm} edge differs from the fortress by {diff:.1f}")
        bottom = int(round(scroll(d["t"]) + SCREEN / 2 - DOOR_H / 2))
        if bottom != ra:
            problems.append(f"{d['piece']}: placed with its bottom on row {bottom}, not {ra}")
    # the gate rule
    for k, g in enumerate(GATES):
        domes = [e["dome"] for e in gate_entries(k)]
        for e in gate_entries(k):
            p = e["pylon"]
            if p["y"] > SCREEN / 2:
                problems.append(f"{g['name']} pylon {p['id']}: y {p['y']} not in the upper half")
            if e["dome"][1] > 360:
                problems.append(f"{g['name']} dome {p['id']}: centre y {e['dome'][1]:.0f} > 360")
            for (dx, dy) in domes:
                if np.hypot(p["x"] - dx, p["y"] - dy) < DOME_R["hard"]:
                    problems.append(f"{g['name']} pylon {p['id']}: under a dome at hard ({dx:.0f}, {dy:.0f})")
            for x, y in e["units"]:
                dd = np.hypot(x - e["dome"][0], y - e["dome"][1])
                if dd > DOME_R["easy"]:
                    problems.append(f"{g['name']} {e['kind']} ({x}, {y}): {dd:.0f} px from its dome (> easy's 95)")
                for o in gate_entries(k):
                    if o["pylon"]["id"] != p["id"] and np.hypot(x - o["dome"][0], y - o["dome"][1]) < DOME_R["medium"]:
                        problems.append(f"{g['name']} {e['kind']} ({x}, {y}): also under dome {o['pylon']['id']}")
        cx, cy = g["core"]
        for (dx, dy) in domes:
            if np.hypot(cx - dx, cy - dy) < DOME_R["hard"]:
                problems.append(f"{g['name']} core ({cx}, {cy}) under a dome at hard")
        print(f"{g['name']}: pylons " + ", ".join(f"{e['pylon']['id']} ({e['pylon']['x']}, {e['pylon']['y']}) -> dome "
                                                  f"({e['dome'][0]:.0f}, {e['dome'][1]:.0f})" for e in gate_entries(k))
              + f"; core {g['core']}")
    # motion: at most two animated pieces on the screen (the doors: one gate at a time; the drive glow)
    for step in range(int((END + 2) * 10)):
        t = step / 10
        animated = 0
        for d in bd.get("doors", []):
            x, y = piece_xy(d, 1.0, t)
            if -DOOR_H / 2 < y < SCREEN + DOOR_H / 2:
                animated += 1
        if animated > 2:
            problems.append(f"t={t}: {animated} door pieces on the screen at once")
            break
    area = sum(distinct.values())
    print(f"atlas: {len(distinct)} distinct images, {area / 1e6:.2f} M px with padding = "
          f"{area / 2048 ** 2:.2f} pages of 2048^2 before packing losses; checker {chk / max(vis, 1) * 100:.2f} % "
          f"of {vis} px")
    for p in problems:
        print("PROBLEM", p)
    print("check:", "ok" if not problems else f"{len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review" in args:
        review()
        sys.exit(0)
    built = build_all()
    write_proposal(*built)
    if "--proposal" not in args:
        review()
