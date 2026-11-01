#!/usr/bin/env python3
"""Production art: the Level 39 backdrop, Io Flyby (design/campaign/act-6-jovian-storm/level-39-io-flyby; M10 part E
batch, concept round 77, step ART-E): Io's surface, the refinery kit and the vents, and the ground placements and the 27
vents of the level's ``--proposal``.

Straight to production from round 72's chosen look: Q10 a's **L39 grade** (tools/concept/act6_r72.py ``ground_l39``:
black lava, desaturated sulphur yellow and ochre plains, white frost, orange vent glow; a harsh small Sun from the top
left), the Jovian document's Io (design/world/jovian *Sub-locations per level*, *Media per level*: Io's near-vacuum, no
weather, plumes as umbrellas). Every haze, glow and shadow is a float field written unquantised (far more than 32
steps, no ordered dither: the 2026-10-30 dither audit, DitherGuardTest's measure); every glow is additive
(premultiplied on black) and drawn once. The units on it are tools/art/refinery_units.py's (the towers, the pumps,
the vents' tells and rocks).

**Io's plains from t = 0, no Jupiter** (round 77 F.1 redo, the user 2026-11-01: "no Jupiter on top-down ground
levels"; Jupiter stays in the briefing images and the space levels 36-38 and 40): the level opens on the plains, the
ground covering the screen from t = 0. Step ART-E's opening over Io's limb (Jupiter's disc low on ``deep``, Tvashtar's
side-on umbrella plume on the limb, the limb coming down) is gone: the user read the limb as a flat disc with no
perspective, and a curve that tight would have Io pass under the ship in about 10 s. Section 1's ground is still two
exact pieces (no ground tile set in section 1: the sections are E2's), now opaque from row -16 (a margin under the
screen's bottom edge at t 0) to section 2's seam + 160.

The layers (``scroll_factors``): ``deep`` 0.15 (``io-space``: black, stars; the sections' deep tile set, covered by
the ground throughout), ``ground`` 1 (Io: the
tiles, the strip, the seams, the works, the vents), ``high-air`` 1.7 (``pele-veil``: Pele's plume haze, the light
atmosphere's wisps in sections 4-5, at most 16 %). No low-air.

The ground: one **periodic base terrain** (480x1040, every noise lattice divides 1040 = 8 s at 130 px/s), so every
tile set repeats seamlessly and the section seams only change the features: sulphur flats in the mood's desaturated
ochre with relief lit from the top left, black lava flows, white frost; per section a dressing:
  tvashtar-a, -b   ground pieces (section 1, from row -16 to section 2's seam + 160, opaque): the plains with frost
                   fields and wide black lava flows; their top rows are loki-yard's pixels (no seam)
  loki-yard        section 2: the yard's graded road (x ~ 70) and the pipeline rack (x 240), worked dust
  pipeline-run     section 3: the lava channel (orange core, black crust banks, x ~ 300 meandering) and the rack (x 150)
  pele-yard        section 4: Pele's red-orange fallout on the plains, the road (x ~ 180), the rack (x 240)
  plume-field      section 5: pale fallout, vent crusts, the rack crossing it (x 150)
  prometheus-yard  section 6: frost fields, the road (x ~ 90), the rack (x 250)
  seam-3 .. seam-6 ground 480x320 at the seams of sections 3-6 (exact rows): the racks bending from one section's x to
                   the next, the channel's crusted ends, the dressings crossfading
The refinery kit (one kit, three dressings: Loki rust-stained, Pele under fallout, Prometheus frosted):
  loki-works, pele-works, prometheus-works  ground ~480x420: the yard slab under the two towers (their octagonal pads
                   with the pipe collars), storage tanks, a control shed, lamp posts, the rack through the yard
  pipes-west, pipes-east (+ -glow)  ground: a tower's pipe run from its pad to the rack (the tower west / east of the
                   rack, 90 px from it); the glow (additive) is the hot line, which the game switches off when that
                   tower's group is cleared (``dark_with``: E4's BackdropPieces hook)
  pump-rack (+ -glow)  ground 40x240: the rack's flanges at a pump station and the hot line along the rack either side
                   of it, dark when that pump dies (``dark_with: P<n>``)
  pele-gate        ground 480x384: the works' wall across the field, the road's gateway, the Sentinels' two blockhouses
                   (step E4: the right one 1.7 s = 221 rows further up, as E2 enters the right Sentinel 1.7 s after the
                   left; the wall jogs up beside it)
  supply-depot     ground 144x112: the refinery supply depot (its latch is the game's ``depot-latch`` sprite)
  survey-hut       ground 192x176: a pre-war Coalition survey hut half buried on a lava island in the channel, its door
                   lock dark (the lock is the game's ``survey-hut-lock`` sprite)
  vent-a, -b, -c   ground 64x64: a cold vent (a black mouth, a pale crust rim, stains), placed under each of the 27
                   vents (the tell, the burst, the rocks and the umbrella are refinery_units.py's sprites)
  io-space         deep 480x512: black space, stars, two moons as points (never seen: the ground covers it)
  pele-veil        high-air 480x960: Pele's plume haze (thin sulphur veils, normal blend, at most 16 %)

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout (six sections at 130 px/s). **Re-run after the data moves a
section or a tower** (the seams are cut at the sections' rows and the works stand under the towers). ``--proposal``
writes the ``backdrop:`` block, the sections, the ground placements (the towers, the pump stations and the guards, the
hut and the depot), the 27 vents with their ``first`` and the holds to concept/backdrop-proposal.yaml (never into
data.yaml).

Review: design/campaign/act-6-jovian-storm/level-39-io-flyby/concept/backdrop-final-r77-b.png (twelve screens with
stand-ins, the readability crops at 1x, the pieces, the tile sets) and .gif (the first 10 s at 130 px/s: the plains
from t 0, the first vent telling and throwing at t ~13 just after; the r77-a GIF's Loki hold is unchanged and stays
in round 77's record).
Run: python3 tools/art/backdrop_l39.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys

import numpy as np
import yaml
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the shared Jovian pieces: the clock, the fields, the checks' measures)
import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
from jovkit import (additive_rgb, blur, checker_share, glow, rgba, smoothstep, wrap_score,  # noqa: E402
                    wrap_score_alpha)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l39.py"
BATCH = "M10 part E batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r77"
VARIANT = "b"                               # round 77 F.1 redo (r77-a: the limb opening)
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-39-io-flyby"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-39"
W, SCREEN, SPEED = 480, 540, 130.0
FACTORS = {"deep": 0.15, "ground": 1.0, "high-air": 1.7}
P = 1040                                   # the ground tiles' period (8 s at 130 px/s; every lattice divides it)
VEIL_H = 960

# The L39 grade (Q10 a, act6_r72 ground_l39): desaturated sulphur and ochre, black lava, white frost.
IO = [(0.0, (52, 46, 30)), (0.4, (126, 112, 70)), (0.7, (168, 152, 102)), (1.0, (206, 196, 150))]
LAVA = np.array((20, 16, 16.0))
LAVA_SHEEN = np.array((40, 34, 34.0))
FROST = np.array((232, 232, 224.0))
PELE = np.array((150, 84, 50.0))           # Pele's red-orange fallout (desaturated)
FALLOUT = np.array((214, 206, 168.0))      # the plume field's pale fallout
DUST = np.array((150, 140, 112.0))         # worked ground
ROAD = np.array((168, 158, 124.0))
STEEL = np.array((58, 58, 62.0))
STEEL_LIT = np.array((124, 124, 128.0))
CHARCOAL = np.array((34, 34, 37.0))
GOLD = np.array((160, 130, 70.0))          # the refinery's gold a step desaturated (the units' gold reads over it)
CONCRETE = np.array((150, 142, 122.0))
SHADOW = np.array((14, 11, 8.0))
LAVA_GLOW = np.array((150, 46, 14.0))       # the channel's lava: a deep red-orange, crusted (amber bullets read over it)
LAVA_HOT = np.array((236, 120, 44.0))
GRADE = 0.88                               # the plains a step darker: round 72's L39 mood (mean 115)
HOT_PIPE = np.array((255, 120, 36.0))
SPACE = np.array((3, 4, 9.0))

# =========================================================================== the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
GROUND_TILES = ["loki-yard", "pipeline-run", "pele-yard", "plume-field", "prometheus-yard"]
PROPOSED_SECTIONS = [
    {"name": "Tvashtar Plains", "end": 20, "speed": 130, "atmosphere": "clear", "tiles": ["io-space"]},
    {"name": "Loki Works", "end": 62, "speed": 130, "atmosphere": "clear", "tiles": ["io-space", "loki-yard"]},
    {"name": "The Pipeline Run", "end": 104, "speed": 130, "atmosphere": "clear",
     "tiles": ["io-space", "pipeline-run"]},
    {"name": "Pele Works", "end": 142, "speed": 130, "atmosphere": "light", "tiles": ["io-space", "pele-yard"]},
    {"name": "The Plume Field", "end": 164, "speed": 130, "atmosphere": "light", "tiles": ["io-space", "plume-field"]},
    {"name": "Prometheus Works", "end": 192, "speed": 130, "atmosphere": "clear",
     "tiles": ["io-space", "prometheus-yard"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam = CLOCK.scroll, CLOCK.seam
KINDS = ["s1", "s2", "s3", "s4", "s5", "s6"]


def section_at_row(row):
    return CLOCK.section_at_row("ground", row)


def exact_t(bottom, h, factor=1.0):
    """The placement time putting a piece h px tall with its bottom row on layer row ``bottom``."""
    t = CLOCK.time_at((bottom + h / 2 - SCREEN / 2) / factor)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) * factor + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def centre_t(row, factor=1.0):
    """The placement time putting a piece's centre on layer row ``row``."""
    return round(CLOCK.time_at((row - SCREEN / 2) / factor), 3)


def enter_t(row, half):
    """The time a ground unit (or object) of ``2 * half`` px with its centre on ``row`` enters at the top edge."""
    return CLOCK.time_at(row - SCREEN - half)


def unit_row(t, half):
    return scroll(t) + SCREEN + half


def vent_row(t):
    """A vent enters at the top edge (its centre on the top edge: sim Eruptions.ventY)."""
    return scroll(t) + SCREEN


# =========================================================================== the layout (ground rows y-up, x)

# the towers: (group, x, entry t); each 96x96, its centre entering 48 px above the top edge
TOWERS = [("Loki A", 150, 43.0), ("Loki B", 330, 44.0), ("Pele A", 330, 125.0), ("Pele B", 150, 126.0),
          ("Prometheus A", 160, 175.0), ("Prometheus B", 340, 176.0)]
WORKS = {"loki": (("Loki A", "Loki B"), 240), "pele": (("Pele A", "Pele B"), 240),
         "prometheus": (("Prometheus A", "Prometheus B"), 250)}
TOWER_HALF, PUMP_HALF = 48, 20
HOLD_DEPTH, HOLD_SPEEDS, HOLD_RAMP = 200, {"easy": 30, "medium": 40, "hard": 50}, 1.0
TELL = {"easy": 1.6, "medium": 1.2, "hard": 1.0}
RISE = 0.5
# the pump stations: (name, x, entry t), on the racks of sections 3 and 5
PUMPS = [("P1", 150, 68.0), ("P2", 150, 76.0), ("P3", 150, 84.0), ("P4", 150, 92.0),
         ("P5", 150, 146.0), ("P6", 150, 152.0), ("P7", 150, 158.0), ("P8", 150, 162.5)]
# the rack's x per section (s1 none); the road's (x, wobble phase) or None
RACK = {"s1": None, "s2": 240.0, "s3": 150.0, "s4": 240.0, "s5": 150.0, "s6": 250.0}
ROADS = {"s2": (70.0, 0.4), "s4": (180.0, 2.1), "s6": (92.0, 4.0)}
CHANNEL_X = 300.0
# the 27 vents: (t entering at the top edge, x, first: s after entry to the first tell, note); the works' vents are
# computed (``hold_vents``) from the towers so that they stand on the field through the hold
VENTS_OPEN = [
    (12.0, 340, 1.0, "section 1: the eruption taught, alone (no wave from t 8 to 14.5)"),
    (30.0, 150, 0.8, "section 2: by the yard road"),
    (66.0, 60, 0.8, "section 3: west of the rack"), (72.0, 420, 0.9, "east of the channel"),
    (79.0, 60, 0.8, "west"), (86.0, 420, 1.0, "east"), (94.0, 60, 0.8, "west"), (100.0, 420, 0.9, "east"),
    (113.0, 300, 0.9, "section 4: inside the gate"),
    (170.0, 380, 0.8, "section 6: before the Prometheus works"),
]
# the plume field's four rows of three, rippling 0.4 s a vent (each row erupts once at 130 px/s)
PLUME_ROWS = [(145.0, (240, 330, 420)), (150.0, (60, 300, 420)), (155.0, (250, 340, 430)), (160.0, (60, 270, 380))]
PLUME_FIRST = 0.3
# the works' vents: (works, x, ground row offset from the first tower's centre row)
HOLD_VENTS = [("loki", 430, 100), ("loki", 60, 80), ("pele", 40, 100), ("pele", 235, 65), ("prometheus", 60, 62)]
# the guards and objects (E2's; x here so the backdrop's yards, gate and racks are under them)
GUARDS = [
    ("strider", "Strider pair down the Loki yard road, 1.5 s apart; the second drops a shield cell", [(32.0, 90),
                                                                                                     (33.5, 90)]),
    ("sam-nest", "SAM Nest pair by the Loki rack", [(38.0, 200), (38.0, 290)]),
    ("sam-nest", "SAM Nest on the section 3 rack, between P2 and P3", [(80.0, 150)]),
    ("sentinel-tower", "Sentinel Tower pair on the Pele gate's blockhouses (emitters >= 40 px inside the field); "
                       "the left drops an overdrive; the right 1.7 s later (E2)", [(110.0, 64), (111.7, 416)]),
    ("strider", "Strider pair walking up the Pele yard toward Pele B (x 150)", [(118.0, 110), (118.0, 200)]),
    ("sam-nest", "SAM Nest trio in the Pele yard, east of the rack", [(122.0, 290), (122.0, 360), (122.0, 430)]),
    ("sam-nest", "SAM Nest pair on the plume field beside row 2's vents", [(150.0, 210), (150.0, 360)]),
    ("strider", "a lone Strider walking toward Prometheus A (x 160)", [(168.0, 230)]),
    ("sam-nest", "SAM Nest pair at the Prometheus works' gate", [(172.0, 80), (172.0, 420)]),
]
HUT_T, DEPOT_T, DEPOT_X = 96.0, 103.0, 64.0
STRIP_LO = -16                             # section 1's ground from this row (under the screen's bottom edge at t 0)


def tower_rows():
    return {g: (x, unit_row(t, TOWER_HALF)) for g, x, t in TOWERS}


def hold_start(works):
    """Script time a works' hold starts: its first tower's centre reaches HOLD_DEPTH below the top edge."""
    groups = WORKS[works][0]
    first = min(t for g, _, t in TOWERS if g in groups)
    return first + (2 * TOWER_HALF + HOLD_DEPTH - TOWER_HALF) / SPEED


def hold_vents():
    rows = tower_rows()
    out = []
    for works, x, dy in HOLD_VENTS:
        groups = WORKS[works][0]
        row = rows[groups[0]][1] + dy
        tv = round(enter_t(row, 0), 3)
        first = round(hold_start(works) - tv - 0.2, 2)
        out.append((tv, x, max(0.3, first), f"{works} hold: beside the towers"))
    return out


def hold_vents_of(works):
    return [v for v in hold_vents() if v[3].startswith(works)]


def all_vents():
    vents = list(VENTS_OPEN) + hold_vents()
    for t, xs in PLUME_ROWS:
        for k, x in enumerate(xs):
            vents.append((t, x, round(PLUME_FIRST + 0.4 * k, 2), f"plume field row t {t:g}"))
    return sorted(vents)


def channel_x(r):
    u = TAU * np.asarray(r, float) / P
    return CHANNEL_X + 26 * np.sin(u) + 10 * np.sin(2 * u + 1.0)


def road_x(kind, r):
    x0, ph = ROADS[kind]
    return x0 + 8 * np.sin(TAU * np.asarray(r, float) / P + ph)


def hut_place():
    row = unit_row(HUT_T, 12)
    return float(channel_x(row)), row


# =========================================================================== the base terrain (periodic, 480 x P)

_BASE = {}


def base():
    if _BASE:
        return _BASE
    seed = 3901
    n = raster.fbm(W, P, 80, seed, octaves=5)
    lava = raster.fbm(W, P, 80, seed + 1, octaves=4)
    frost = raster.fbm(W, P, 40, seed + 2, octaves=4)
    hue = raster.fbm(W, P, 80, seed + 3, octaves=3)
    tex = raster.fbm(W, P, 10, seed + 4, octaves=2)
    flows = smoothstep(0.012, 0.0, np.abs(lava - 0.46)) * smoothstep(0.5, 0.62, n)
    relief = blur(n * 30 - smoothstep(0.7, 0.76, lava) * 8, 2.0, wrap_rows=True)
    gy, gx = np.gradient(np.pad(relief, ((1, 1), (0, 0)), mode="wrap"))
    gy, gx = gy[1:-1], gx[1:-1]
    # image rows are y-down; the field here is indexed by row mod P from the bottom (y-up): flip the light's y
    shade = np.clip(1.0 - (gx - gy) * 0.18, 0.72, 1.24)
    plates = raster.fbm(W, P, 20, 3921, octaves=3)
    _BASE.update(n=n, lava=lava, frost=frost, hue=hue, tex=tex, flows=flows, shade=shade, plates=plates)
    return _BASE


LOOK = {   # per section: frost share, lava threshold (lower: wider flows), Pele tint, fallout, worked dust
    "s1": dict(frost=0.75, lava=0.69, pele=0.0, fall=0.0, dust=0.0),
    "s2": dict(frost=0.4, lava=0.73, pele=0.0, fall=0.0, dust=0.22),
    "s3": dict(frost=0.3, lava=0.72, pele=0.04, fall=0.0, dust=0.0),
    "s4": dict(frost=0.25, lava=0.74, pele=0.26, fall=0.08, dust=0.18),
    "s5": dict(frost=0.3, lava=0.75, pele=0.14, fall=0.22, dust=0.0),
    "s6": dict(frost=0.62, lava=0.73, pele=0.0, fall=0.0, dust=0.16),
}


def k3(v):
    return np.asarray(v)[..., None] if np.ndim(v) else v


def plains(rows, look):
    """The plains on absolute ground rows (array) with a look dict (floats or per-row arrays): rgb float."""
    b = base()
    idx = np.mod(np.asarray(rows, int), P)
    n, lava, frost, hue, tex, flows, shade = (b[k][idx] for k in ("n", "lava", "frost", "hue", "tex", "flows",
                                                                  "shade"))
    col = lambda v: np.asarray(v, float).reshape(-1, 1) if np.ndim(v) else float(v)  # noqa: E731
    rgb = raster.ramp(IO, np.clip(0.25 + 0.6 * n + 0.06 * (tex - 0.5), 0, 1))
    yellow = np.array((184, 170, 98.0))                     # sulphur flats a touch yellower, still desaturated
    rgb = rgb * (1 - 0.22 * hue[..., None]) + yellow * 0.22 * hue[..., None]
    flow = smoothstep(col(look["lava"]), col(look["lava"]) + 0.05, lava) + 0.8 * flows
    flow = np.clip(flow, 0, 1)
    sheen = smoothstep(0.4, 0.9, tex)
    black = LAVA * (1 - sheen[..., None] * 0.6) + LAVA_SHEEN * sheen[..., None] * 0.6
    rgb = rgb * (1 - flow[..., None]) + black * flow[..., None]
    fr = smoothstep(1.0 - col(look["frost"]) * 0.42, 1.12 - col(look["frost"]) * 0.42, frost) * (1 - flow)
    rgb = rgb * (1 - 0.55 * fr[..., None]) + FROST * 0.55 * fr[..., None]
    pk = k3(col(look["pele"]))
    rgb = rgb * (1 - pk) + PELE * pk
    fk = col(look["fall"]) * (0.6 + 0.8 * (1 - n))
    rgb = rgb * (1 - fk[..., None]) + FALLOUT * fk[..., None]
    dk = col(look["dust"]) * (1 - flow)
    rgb = rgb * (1 - dk[..., None]) + DUST * dk[..., None]
    return rgb * shade[..., None] * GRADE, flow


def blend_looks(ka, kb, u):
    return {k: LOOK[ka][k] * (1 - u) + LOOK[kb][k] * u for k in LOOK[ka]}


# =========================================================================== features (drawn per row)

def draw_road(rgb, rows, xc, strength):
    """A graded road 34 px wide: compacted pale dust, two tyre tracks, low berms (strength 0..1 per row)."""
    x = np.arange(W, dtype=np.float64)[None, :]
    d = x - np.asarray(xc).reshape(-1, 1)
    s = np.asarray(strength, float).reshape(-1, 1)
    bed = smoothstep(19, 15, np.abs(d)) * s
    tracks = (np.exp(-((np.abs(d) - 8) / 1.8) ** 2)) * s * 0.35
    berm = np.exp(-((np.abs(d) - 19) / 2.2) ** 2) * s * 0.25
    tex = base()["tex"][np.mod(rows, P)]
    road = ROAD * (0.94 + 0.12 * tex[..., None])
    rgb[:] = rgb * (1 - bed[..., None]) + road * bed[..., None]
    rgb[:] = rgb * (1 - tracks[..., None] * 0.6) + np.array((98, 90, 70.0)) * tracks[..., None] * 0.6
    rgb[:] = rgb * (1 + berm[..., None] * 0.3)


def draw_channel(rgb, rows, xc, width):
    """The lava channel: an orange core with dark crust plates and bright cracks, black crust banks, a dark stain."""
    x = np.arange(W, dtype=np.float64)[None, :]
    d = np.abs(x - np.asarray(xc).reshape(-1, 1))
    wk = np.asarray(width, float).reshape(-1, 1)
    core_w = 15 * smoothstep(0.45, 1.0, wk) + 1e-3
    bank_w = 34 * smoothstep(0.0, 0.5, wk) + 2e-3
    stain = smoothstep(bank_w + 26, bank_w, d) * np.minimum(1, wk * 3)
    rgb[:] = rgb * (1 - 0.45 * stain[..., None])
    bank = smoothstep(bank_w + 1.5, bank_w - 1.5, d) * np.minimum(1, wk * 4)
    tex = base()["tex"][np.mod(rows, P)]
    lava_n = base()["lava"][np.mod(rows, P)]
    crust = LAVA * (0.9 + 0.3 * tex[..., None])
    rgb[:] = rgb * (1 - bank[..., None]) + crust * bank[..., None]
    core = smoothstep(core_w + 1.2, core_w - 1.2, d) * (wk > 0.05)
    plates = smoothstep(0.45, 0.6, base()["plates"][np.mod(rows, P)])
    plates = np.clip(plates * 1.3, 0, 1)
    hot = LAVA_GLOW * (1 - plates[..., None] * 0.7) + LAVA_HOT * np.clip(0.35 - plates, 0, 1)[..., None] * 0.7
    hot *= (0.85 + 0.3 * lava_n[..., None])
    rgb[:] = rgb * (1 - core[..., None]) + hot * core[..., None]
    halo = np.exp(-np.clip(d - core_w, 0, None) / 7) * smoothstep(bank_w, core_w, d) * (core_w > 1) * 0.18
    rgb[:] += halo[..., None] * LAVA_GLOW


RACK_W = 13                                  # the rack's half width
SLEEPER = 26                                 # px between its cross-members (divides P)


def draw_rack(rgb, alpha, rows, xc, strength=1.0):
    """The pipeline rack from above: a cross-member every 26 rows, two pipes lit from the left (dark steel, a pale
    highlight), a gold collar every 104 rows, the rack's soft shadow to the right."""
    rows = np.asarray(rows)
    x = np.arange(W, dtype=np.float64)[None, :]
    d = x - np.asarray(xc).reshape(-1, 1)
    s = np.asarray(strength, float).reshape(-1, 1) * np.ones((1, 1))
    sh = smoothstep(RACK_W + 8, RACK_W + 1, d - 4) * smoothstep(-RACK_W - 2, -RACK_W + 4, d) * 0.5 * s
    rgb[:] = rgb * (1 - sh[..., None]) + SHADOW * sh[..., None]
    rm = np.mod(rows, SLEEPER).reshape(-1, 1)
    sleeper = ((rm < 4) & (np.abs(d) < RACK_W)) * s
    rgb[:] = rgb * (1 - sleeper[..., None]) + CHARCOAL * sleeper[..., None]
    for off in (-5.5, 5.5):
        dp = d - off
        pipe = smoothstep(4.4, 3.4, np.abs(dp)) * s
        lit = np.clip(0.55 - dp / 4.0 * 0.5, 0, 1)
        col = STEEL * (1 - lit[..., None]) + STEEL_LIT * lit[..., None] ** 1.6
        collar = ((np.mod(rows, 104).reshape(-1, 1) < 3) & (np.abs(dp) < 4.8)) * s
        rgb[:] = rgb * (1 - pipe[..., None]) + col * pipe[..., None]
        rgb[:] = rgb * (1 - collar[..., None]) + GOLD * collar[..., None]
    if alpha is not None:
        alpha[:] = np.maximum(alpha, np.clip(sh * 2 + (np.abs(d) < RACK_W) * s, 0, 1))


def crusts(rgb, rows, strength, seed=3925):
    """The plume field's vent crusts: dark spots with pale rims scattered over the plains (periodic)."""
    rng = np.random.default_rng(seed)
    yy = np.asarray(rows, float).reshape(-1, 1)
    x = np.arange(W, dtype=np.float64)[None, :]
    s = np.asarray(strength, float).reshape(-1, 1)
    for _ in range(26):
        cx, cy, rr = rng.uniform(20, W - 20), rng.uniform(0, P), rng.uniform(5, 12)
        dy = np.mod(yy - cy + P / 2, P) - P / 2
        d = np.hypot(x - cx, dy)
        rim = np.exp(-((d - rr) / 2.2) ** 2) * 0.55 * s
        pit = smoothstep(rr * 0.9, rr * 0.4, d) * 0.6 * s
        rgb[:] = rgb * (1 - rim[..., None]) + FROST * rim[..., None]
        rgb[:] = rgb * (1 - pit[..., None]) + LAVA * pit[..., None]


def ground_rows(rows, kind_lo, kind_hi=None, u=None):
    """The ground on absolute rows for one section's look, or blending from ``kind_lo`` to ``kind_hi`` by ``u`` (per
    row, 0..1): the plains, then the features of both (the racks bending, the channel's ends, the roads fading)."""
    rows = np.asarray(rows)
    if kind_hi is None:
        kind_hi, u = kind_lo, np.zeros(len(rows))
    look = blend_looks(kind_lo, kind_hi, u)
    rgb, _ = plains(rows, look)
    ease = u * u * (3 - 2 * u)
    for kind, k in ((kind_lo, 1 - ease), (kind_hi, ease)):
        if kind in ROADS and (k > 0).any():
            draw_road(rgb, rows, road_x(kind, rows), k)
    cw = (kind_lo == "s3") * (1 - u) + (kind_hi == "s3") * u
    if np.any(cw > 0):
        draw_channel(rgb, rows, channel_x(rows), cw)
    fk = LOOK[kind_lo]["fall"] * (1 - u) + LOOK[kind_hi]["fall"] * u
    if np.any(fk > 0.2):
        crusts(rgb, rows, smoothstep(0.2, 0.3, fk))
    xa, xb = RACK[kind_lo], RACK[kind_hi]
    if xa is not None or xb is not None:
        strength = 1.0
        if xa is None:                                       # the rack begins (section 2's), its end capped
            strength = (ease >= 0.35).astype(float)
            xa = xb
        if xb is None:
            strength = (ease < 0.65).astype(float)
            xb = xa
        draw_rack(rgb, None, rows, xa + (xb - xa) * ease, strength)
    return rgb


def to_image(rgb, alpha=None):
    """Rows y-up (index 0 = the lowest row) -> an image (top row = the highest)."""
    rgb = rgb[::-1]
    if alpha is None:
        return Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    return rgba(rgb, alpha[::-1])


# =========================================================================== tiles, the strip, the seams

def ground_tile(kind):
    rows = np.arange(P)
    return to_image(ground_rows(rows, kind))


def seam_piece(i):
    """Section i's seam (i = 2..5, 0-based: sections 3..6): rows [seam - 160, seam + 160), exact."""
    s = seam("ground", i)
    rows = np.arange(s - 160, s + 160)
    u = smoothstep(s - 160, s + 159, rows.astype(float))
    return to_image(ground_rows(rows, KINDS[i - 1], KINDS[i], u)), s - 160


def strip():
    """Section 1's ground from row STRIP_LO (under the screen's bottom edge at t 0) to section 2's seam + 160: the
    plains (s1), opaque, the top 320 rows blending into loki-yard. Returns (rgb, alpha, bottom row)."""
    s2 = seam("ground", 1)
    lo = STRIP_LO
    hi = s2 + 160
    rows = np.arange(lo, hi)
    u = smoothstep(s2 - 160, s2 + 159, rows.astype(float))
    rgb = ground_rows(rows, "s1", "s2", u)
    return rgb, np.ones(rgb.shape[:2]), lo


STRIP_PIECES = ["tvashtar-a", "tvashtar-b"]


def strip_pieces():
    rgb, alpha, lo = strip()
    n = len(rgb)
    cut = n // 2
    return {"tvashtar-a": (to_image(rgb[:cut], alpha[:cut]), lo),
            "tvashtar-b": (to_image(rgb[cut:], alpha[cut:]), lo + cut)}


# =========================================================================== the refinery kit

def yard_piece(works, seed):
    """A works' yard: the slab under both towers (soft dust edge), the towers' octagonal pads with their pipe
    collars, the rack through the yard (the tile's, redrawn), storage tanks, a control shed, lamp posts.
    Returns (image, bottom row)."""
    groups, rack = WORKS[works]
    rows_t = tower_rows()
    ys = [rows_t[g][1] for g in groups]
    lo, hi = int(min(ys) - 200), int(max(ys) + 200)
    rows = np.arange(lo, hi)
    kind = {"loki": "s2", "pele": "s4", "prometheus": "s6"}[works]
    rgb = ground_rows(rows, kind)
    h = len(rows)
    yy = (rows - lo).astype(np.float64)[:, None] * np.ones((1, W))
    x = np.ones((h, 1)) * np.arange(W, dtype=np.float64)[None, :]
    rng = np.random.default_rng(seed)
    tex = raster.fbm(W, h, 16, seed, octaves=4, period=False)
    # the slab: x 24..456, rows ys -130 .. +130, plates 40 px, joints, stains; a soft dust edge
    y0, y1 = min(ys) - 130 - lo, max(ys) + 130 - lo
    edge = np.minimum.reduce([x - 24, 456 - x, yy - y0, y1 - yy])
    slab = smoothstep(-2, 10, edge + (tex - 0.5) * 14)
    for vt, vx, _, note in hold_vents_of(works):                 # rows y-up here: the vent's offset from lo
        slab *= smoothstep(30, 40, np.hypot(x - vx, yy - (vent_row(vt) - lo)) + (tex - 0.5) * 10)
    tone = {"loki": np.array((156, 140, 116.0)), "pele": np.array((160, 132, 108.0)),
            "prometheus": np.array((172, 170, 160.0))}[works]
    conc = tone * (0.9 + 0.18 * tex[..., None])
    joint = ((np.mod(x, 40) < 1) | (np.mod(yy, 40) < 1)) * 0.12
    conc *= (1 - joint[..., None])
    stain = smoothstep(0.62, 0.75, raster.fbm(W, h, 32, seed + 1, octaves=3, period=False))
    stain_col = {"loki": np.array((110, 70, 44.0)), "pele": np.array((140, 80, 50.0)),
                 "prometheus": np.array((210, 212, 210.0))}[works]
    conc = conc * (1 - 0.35 * stain[..., None]) + stain_col * 0.35 * stain[..., None]
    rgb = rgb * (1 - slab[..., None]) + conc * slab[..., None]
    alpha = np.clip(slab * 1.0, 0, 1)
    # the rack through the yard (the tile's, so it continues under the piece's soft edge)
    draw_rack(rgb, alpha, rows, np.full(h, float(rack)))
    img = to_image(rgb, alpha)
    d = ImageDraw.Draw(img)

    def yd(row):                                              # a ground row -> the image's y (down)
        return h - 1 - (row - lo)
    shadows = Image.new("RGBA", img.size)
    sd = ImageDraw.Draw(shadows)
    props = []
    # the towers' pads: octagons r 44 with a dark ring and anchor bolts (the sprite stands on them)
    for g in groups:
        tx, ty = rows_t[g][0], yd(rows_t[g][1])
        pts = [(tx + 46 * np.cos(a), ty + 46 * np.sin(a)) for a in np.linspace(0, TAU, 8, endpoint=False) + TAU / 16]
        d.polygon(pts, fill=(124, 118, 104, 255), outline=(70, 66, 58, 255))
        pts2 = [(tx + 38 * np.cos(a), ty + 38 * np.sin(a)) for a in np.linspace(0, TAU, 8, endpoint=False) + TAU / 16]
        d.polygon(pts2, outline=(96, 90, 80, 255))
        for a in np.linspace(0, TAU, 8, endpoint=False):
            d.ellipse([tx + 41 * np.cos(a) - 1.5, ty + 41 * np.sin(a) - 1.5, tx + 41 * np.cos(a) + 1.5,
                       ty + 41 * np.sin(a) + 1.5], fill=(60, 58, 54, 255))
        props.append(("pad", tx, ty))
    # storage tanks (spheres from above), a control shed, lamp posts: on the side of the yard away from the rack's
    # busy middle, never under a tower's pad or the pipe runs
    side_x = {"loki": (400, 420), "pele": (60, 76), "prometheus": (410, 70)}[works]
    t_rows = sorted(ys)
    vents = [(vx, yd(vent_row(vt))) for vt, vx, _, _ in hold_vents_of(works)]
    tank_spots = []
    for want_y, want_x in ((yd(t_rows[0]) + 70, side_x[0]), (yd(t_rows[1]) - 70, side_x[1])):
        cands = [(abs(cx - want_x) + abs(cy - want_y) * 0.5, cx, cy) for cx in range(50, 440, 10)
                 for cy in (want_y - 20, want_y, want_y + 20)
                 if all(np.hypot(cx - vx, cy - vy) > 66 for vx, vy in vents)
                 and all(np.hypot(cx - rows_t[g][0], cy - yd(rows_t[g][1])) > 76 for g in groups)
                 and abs(cx - rack) > 46]
        tank_spots.append(min(cands)[1:])
    tanks = []
    for k, (cx, cy) in enumerate(tank_spots):
        r = 21 + 3 * k
        sd.ellipse([cx - r + 7, cy - r + 7, cx + r + 9, cy + r + 9], fill=(14, 11, 8, 110))
        tanks.append((cx, cy, r))
    shed = (side_x[0] - 6 if works != "pele" else side_x[1] + 6, (yd(t_rows[0]) + yd(t_rows[1])) / 2, 22, 16)
    sx, sy, sw, sh = shed
    sd.rectangle([sx - sw + 6, sy - sh + 6, sx + sw + 8, sy + sh + 8], fill=(14, 11, 8, 110))
    shadows = Image.fromarray(np.dstack([np.array(shadows)[..., :3],
                                         np.clip(blur(np.array(shadows)[..., 3].astype(float), 2.0), 0, 255)
                                         .astype(np.uint8)]), "RGBA")
    img.alpha_composite(shadows)
    arr = np.array(img).astype(np.float64)
    for cx, cy, r in tanks:                                   # shaded spheres, a gold band, a dark valve
        yy2, xx2 = np.mgrid[0:h, 0:W].astype(np.float64)
        dx, dy = (xx2 - cx) / r, (yy2 - cy) / r
        inside = dx * dx + dy * dy < 1
        nz = np.sqrt(np.clip(1 - dx * dx - dy * dy, 0, 1))
        lit = np.clip(-0.55 * dx - 0.55 * dy + 0.62 * nz, 0, 1)
        col = np.array((112, 108, 100.0))[None, None] * (0.35 + 0.85 * lit[..., None])
        band = (np.abs(dy - 0.05) < 0.07) & inside
        col[band] = GOLD * (0.5 + 0.7 * lit[band][..., None])
        aa = smoothstep(1.02, 0.97, np.sqrt(dx * dx + dy * dy))
        arr[..., :3] = arr[..., :3] * (1 - aa[..., None]) + col * aa[..., None]
        arr[..., 3] = np.maximum(arr[..., 3], aa * 255)
    img = Image.fromarray(np.clip(np.round(arr), 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    d.rectangle([sx - sw, sy - sh, sx + sw, sy + sh], fill=(70, 68, 66, 255), outline=(30, 30, 32, 255))
    d.rectangle([sx - sw + 3, sy - sh + 3, sx + sw - 3, sy - 1], fill=(92, 90, 86, 255))
    d.line([(sx - sw + 2, sy), (sx + sw - 2, sy)], fill=(40, 40, 42, 255))
    lamps = [(sx + sw - 4, sy - sh + 4, (255, 60, 40))]
    for k, (lx, ly) in enumerate(((30, yd(max(ys)) - 110), (450, yd(max(ys)) - 110), (30, yd(min(ys)) + 110),
                                  (450, yd(min(ys)) + 110))):
        d.rectangle([lx - 2, ly - 2, lx + 2, ly + 2], fill=(40, 40, 44, 255))
        lamps.append((lx, ly, (255, 170, 70) if k % 2 == 0 else (255, 60, 40)))
    arr = np.array(img).astype(np.float64)
    light = np.zeros((h, W, 3))
    for lx, ly, c in lamps:
        glow(light, lx, ly, 2.2, c, 0.9)
    arr[..., :3] = np.clip(arr[..., :3] + light * (arr[..., 3:4] / 255), 0, 255)
    img = Image.fromarray(np.clip(np.round(arr), 0, 255).astype(np.uint8), "RGBA")
    return img, lo


def pipes_piece(side):
    """A tower's pipe run to the rack (the tower ``side`` of the rack, 90 px from it): a pipe from the pad's collar
    (32 px from the tower's centre) to the rack's edge, on low supports, with a valve; and its hot line (additive).
    Both 46x24 (side west: the pipe runs east); the piece's centre is the run's middle."""
    w, h = 46, 24
    x = np.arange(w, dtype=np.float64)[None, :] * np.ones((h, 1))
    y = np.arange(h, dtype=np.float64)[:, None] * np.ones((1, w))
    rgb = np.zeros((h, w, 3))
    alpha = np.zeros((h, w))
    light = np.zeros((h, w, 3))
    for yc in (12.0,):
        dy = y - yc
        pipe = smoothstep(3.4, 2.4, np.abs(dy))
        lit = np.clip(0.55 - dy / 3.0 * 0.5, 0, 1)
        col = STEEL * (1 - lit[..., None]) + STEEL_LIT * lit[..., None] ** 1.6
        sh = smoothstep(3.6, 1.0, np.abs(dy - 3)) * 0.4 * (1 - pipe)
        rgb = rgb * (1 - pipe[..., None]) + col * pipe[..., None]
        rgb = np.where((sh > alpha)[..., None] & (pipe < 0.5)[..., None], SHADOW, rgb)
        alpha = np.maximum(alpha, np.maximum(pipe, sh))
        heat = 0.8 + 0.2 * np.sin(x / 4.3 + 0.7) * np.sin(x / 2.9)          # a little heat variation along the run
        light += (np.exp(-(dy / 1.7) ** 2) * smoothstep(0, 8, x) * smoothstep(w, w - 8, x) * heat)[..., None] \
            * HOT_PIPE * 0.6
    for cx in (9, 37):
        sup = (np.abs(x - cx) < 2) & (np.abs(y - 12) < 7)
        rgb[sup] = CHARCOAL
        alpha[sup] = 1
    valve = (np.abs(x - 23) < 4) & (np.abs(y - 12) < 6)
    rgb[valve] = GOLD
    alpha[valve] = 1
    body = rgba(rgb, alpha)
    glow_img = additive_rgb(light)
    if side == "east":
        # mirrored geometry, lit again from the top left (the pipes' shading is symmetric about their axis)
        body = body.transpose(Image.FLIP_LEFT_RIGHT)
        glow_img = glow_img.transpose(Image.FLIP_LEFT_RIGHT)
    return body, glow_img


PUMP_RACK = (40, 240)


def pump_rack():
    """At a pump station: the rack's flanges and a concrete footing under the pump (40x40 in the middle), and the hot
    line along the rack's two pipes 100 px either way (additive), dark when the pump dies."""
    w, h = PUMP_RACK
    x = np.arange(w, dtype=np.float64)[None, :] * np.ones((h, 1))
    y = np.arange(h, dtype=np.float64)[:, None] * np.ones((1, w))
    rgb = np.zeros((h, w, 3)) + CONCRETE * 0.8
    foot = (np.abs(x - w / 2) < 19) & (np.abs(y - h / 2) < 24)
    alpha = foot * 1.0
    for yc in (h / 2 - 26, h / 2 + 26):
        fl = (np.abs(y - yc) < 2) & (np.abs(x - w / 2) < 9)
        rgb[fl] = GOLD
        alpha[fl] = 1
    light = np.zeros((h, w, 3))
    for off in (-5.5, 5.5):
        line = np.exp(-((x - w / 2 - off) / 1.2) ** 2) * smoothstep(0, 40, y) * smoothstep(h, h - 40, y)
        light += line[..., None] * HOT_PIPE * 0.5
    return rgba(rgb, alpha), additive_rgb(light)


# the gate piece: its height, the left blockhouse's row from its top (the right's GATE_RISE rows above it: E2's right
# Sentinel 1.7 s after the left at 130 px/s)
GATE_H, GATE_LEFT_Y = 384, 304
GATE_RISE = round(1.7 * 130)


def gate_piece(seed=3935):
    """The Pele works' wall across the field: a charcoal wall with gold coping, the road's gateway (x ~ 180), and the
    Sentinels' two blockhouses (x 64 and 416, 56 px square) with red lamps; its shadow to the bottom right. Step E4:
    the right blockhouse GATE_RISE rows further up the scroll (E2's right Sentinel enters 1.7 s after the left), the
    wall jogging up to it along x 388 (a bastion), then on to the right edge at its row."""
    w, h = W, GATE_H
    img = Image.new("RGBA", (w, h))
    shadows = Image.new("RGBA", (w, h))
    sd = ImageDraw.Draw(shadows)
    d = ImageDraw.Draw(img)
    wy = GATE_LEFT_Y
    wr = wy - GATE_RISE                                        # the right blockhouse's row
    jog = 388                                                  # the wall's jog up to it
    gate = (150, 210)                                          # the gateway over the road (x ~180)
    houses = ((64, wy), (416, wr))
    segs = [(0, gate[0], wy), (gate[1], jog, wy), (jog, w, wr)]
    for a, b, y in segs:
        sd.rectangle([a + 6, y - 6 + 8, b + 6, y + 6 + 8], fill=(14, 11, 8, 120))
    sd.rectangle([jog - 6 + 6, wr - 6 + 8, jog + 6 + 6, wy + 6 + 8], fill=(14, 11, 8, 120))
    for bx, by in houses:
        sd.rectangle([bx - 28 + 10, by - 28 + 10, bx + 28 + 10, by + 28 + 10], fill=(14, 11, 8, 120))
    sh = np.array(shadows).astype(np.float64)
    sh[..., 3] = blur(sh[..., 3], 2.0)
    img = Image.fromarray(np.clip(sh, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    for a, b, y in segs:
        d.rectangle([a, y - 6, b, y + 6], fill=(46, 46, 50, 255))
        d.rectangle([a, y - 6, b, y - 4], fill=(150, 124, 70, 255))
        d.rectangle([a, y + 5, b, y + 6], fill=(28, 28, 30, 255))
        for px in range(a + 12, b, 32):
            d.rectangle([px - 1, y - 3, px + 1, y + 4], fill=(36, 36, 40, 255))
    d.rectangle([jog - 6, wr - 6, jog + 6, wy + 6], fill=(46, 46, 50, 255))   # the jog, its coping lit from the left
    d.rectangle([jog - 6, wr - 6, jog - 4, wy + 6], fill=(150, 124, 70, 255))
    d.rectangle([jog + 5, wr - 6, jog + 6, wy + 6], fill=(28, 28, 30, 255))
    for py in range(wr + 12, wy, 32):
        d.rectangle([jog - 3, py - 1, jog + 4, py + 1], fill=(36, 36, 40, 255))
    for gx in gate:                                            # the gateway's posts
        d.rectangle([gx - 5, wy - 10, gx + 5, wy + 10], fill=(60, 60, 64, 255), outline=(26, 26, 28, 255))
    for bx, by in houses:
        d.rectangle([bx - 28, by - 28, bx + 28, by + 28], fill=(62, 62, 66, 255), outline=(26, 26, 28, 255))
        d.rectangle([bx - 24, by - 24, bx + 24, by - 18], fill=(150, 124, 70, 255))
        d.rectangle([bx - 22, by - 16, bx + 22, by + 22], fill=(74, 74, 78, 255))
    arr = np.array(img).astype(np.float64)
    light = np.zeros((h, w, 3))
    for bx, by in houses:
        for lx, ly in ((bx - 24, by + 24), (bx + 24, by + 24)):
            glow(light, lx, ly, 2.0, (255, 60, 40), 0.9)
    for gx in gate:
        glow(light, gx, wy - 8, 2.0, (255, 170, 70), 0.8)
    arr[..., :3] = np.clip(arr[..., :3] + light * (arr[..., 3:4] / 255), 0, 255)
    return Image.fromarray(np.clip(np.round(arr), 0, 255).astype(np.uint8), "RGBA")


DEPOT = (144, 112)


def depot_piece():
    """The refinery supply depot: a small hangar (charcoal walls, a ribbed roof lit from the top left), crates on its
    apron, its door facing down the screen (the latch, ``depot-latch``, sits on it: offset (0, +28) from the centre)."""
    w, h = DEPOT
    img = Image.new("RGBA", (w, h))
    sh = Image.new("RGBA", (w, h))
    ImageDraw.Draw(sh).rectangle([30, 22, 122, 92], fill=(14, 11, 8, 120))
    a = np.array(sh).astype(np.float64)
    a[..., 3] = blur(a[..., 3], 2.0)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    apron = smoothstep(0, 8, np.minimum.reduce([xx - 2, w - 2 - xx, yy - 2, h - 2 - yy]))
    base_rgb = CONCRETE * (0.88 + 0.16 * raster.fbm(w, h, 8, 3937, octaves=3, period=False))[..., None]
    out = rgba(base_rgb, apron * 0.95)
    out.alpha_composite(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"))
    d = ImageDraw.Draw(out)
    d.rectangle([24, 14, 114, 82], fill=(54, 54, 58, 255), outline=(24, 24, 26, 255))
    for k, x in enumerate(range(28, 112, 8)):
        c = 96 - k * 3
        d.rectangle([x, 18, x + 5, 78], fill=(c, c, c + 4, 255))
    d.rectangle([52, 78, 86, 90], fill=(40, 40, 44, 255), outline=(150, 124, 70, 255))
    for cx, cy in ((10 + 12, 96), (40, 98), (112, 96), (124, 100)):
        d.rectangle([cx - 6, cy - 6, cx + 6, cy + 6], fill=(120, 96, 52, 255), outline=(60, 46, 26, 255))
        d.line([(cx - 6, cy), (cx + 6, cy)], fill=(80, 62, 34, 255))
    return out


HUT = (192, 176)


def hut_piece():
    """The survey hut: a lava island in a widened pool of the channel (glowing lava round black crust), the hut half
    buried in sulphur on it (a pale grey prefab with CDF-blue stripes, sulphur drifted over its back), its door lock
    dark (no lights); offset of the lock from the centre: (0, +15)."""
    w, h = HUT
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    cx, cy = w / 2, h / 2
    n = raster.fbm(w, h, 16, 3939, octaves=4, period=False)
    r = np.hypot((xx - cx) / 1.05, yy - cy)
    pool = smoothstep(86, 70, r + (n - 0.5) * 18)
    island = smoothstep(52, 46, r + (n - 0.5) * 14)
    lava = LAVA_GLOW * (0.8 + 0.4 * n[..., None]) + LAVA_HOT * smoothstep(0.66, 0.82, n)[..., None] * 0.35
    crust_ring = smoothstep(70, 60, r) * (1 - island)
    rgb = lava * (1 - crust_ring[..., None] * 0.6) + LAVA * crust_ring[..., None] * 0.6
    rock = raster.ramp(IO, np.clip(0.3 + 0.5 * n, 0, 1)) * (0.7 + 0.3 * smoothstep(48, 20, r))[..., None]
    rgb = rgb * (1 - island[..., None]) + rock * island[..., None]
    halo = np.exp(-np.clip(r - 52, 0, None) / 6) * (1 - island) * 0.12
    rgb = rgb + halo[..., None] * LAVA_HOT
    img = rgba(rgb, pool)
    d = ImageDraw.Draw(img)
    hx, hy = cx, cy - 4
    d.ellipse([hx - 30 + 8, hy - 18 + 8, hx + 30 + 8, hy + 18 + 8], fill=(20, 16, 12, 140))
    d.rounded_rectangle([hx - 30, hy - 18, hx + 30, hy + 18], 9, fill=(176, 178, 176, 255), outline=(80, 82, 84, 255))
    for k in range(5):
        c = 196 - 12 * k
        d.line([(hx - 26, hy - 14 + 7 * k), (hx + 26, hy - 14 + 7 * k)], fill=(c, c, c - 4, 255))
    d.rectangle([hx - 30, hy - 3, hx + 30, hy + 1], fill=(56, 92, 150, 255))
    sand = Image.new("RGBA", (w, h))
    sa = smoothstep(0.0, 1.0, (yy - (hy - 22)) / -14) * (np.abs(xx - hx) < 34) * smoothstep(40, 20, np.abs(yy - hy + 26))
    sand = rgba(np.zeros((h, w, 3)) + np.array((182, 168, 104.0)), np.clip(sa * 0.85, 0, 1))
    img.alpha_composite(sand)
    d = ImageDraw.Draw(img)
    d.rectangle([hx - 9, hy + 14, hx + 9, hy + 24], fill=(40, 40, 44, 255))
    return img


VENT_PIECES = ["vent-a", "vent-b", "vent-c"]


def vent_piece(k, w=64):
    """A cold vent: a black mouth, a pale sulphur-frost crust rim, rust stains and cracks, a soft edge."""
    rng = np.random.default_rng(3941 + k)
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    n = raster.fbm(w, w, 8, 3945 + k, octaves=3, period=False)
    wob = raster.fbm(w, w, 16, 3949 + k, octaves=2, period=False)
    r = np.hypot(xx - w / 2, yy - w / 2) * (0.9 + 0.22 * wob) * rng.uniform(0.95, 1.05)
    mouth = smoothstep(9.5, 7.0, r + (n - 0.5) * 4)
    rim = np.exp(-((r - 13) / 3.6) ** 2)
    stain = smoothstep(26, 12, r) * (0.5 + 0.5 * n)
    rgb = raster.ramp(IO, np.clip(0.3 + 0.5 * n, 0, 1))
    rgb = rgb * (1 - 0.5 * stain[..., None]) + np.array((128, 70, 40.0)) * 0.5 * stain[..., None]
    rgb = rgb * (1 - 0.55 * rim[..., None]) + FROST * 0.55 * rim[..., None]
    lit = np.clip(0.5 - 0.5 * ((xx - w / 2) + (yy - w / 2)) / max(1.0, 1) / 14, 0, 1)
    rgb = rgb * (0.85 + 0.3 * (rim * lit)[..., None])
    rgb = rgb * (1 - mouth[..., None]) + np.array((10, 8, 8.0)) * mouth[..., None]
    alpha = smoothstep(31, 22, r + (n - 0.5) * 6)
    return rgba(rgb, alpha)


def io_space(seed=3953, h=512):
    """Black space with sparse stars and two of the other moons as points (tiny discs): wraps top to bottom."""
    rgb = np.zeros((h, W, 3)) + SPACE
    rng = np.random.default_rng(seed)
    for _ in range(70):
        i, j = rng.integers(0, h), rng.integers(0, W)
        b = rng.uniform(50, 190)
        rgb[i, j] = (b, b * 0.97, b * 0.92)
    for (mx, my, mr, c) in ((96, 140, 2.2, (210, 200, 170)), (380, 380, 1.6, (170, 176, 190))):
        light = np.zeros((h, W, 3))
        glow(light, mx, my, mr, c, 1.0)
        rgb += light
    return rgba(np.clip(rgb, 0, 255), np.ones((h, W)))


def pele_veil(seed=3955):
    """Pele's plume haze over sections 4-5: thin pale sulphur veils drifting over (normal blend, at most 16 %),
    soft-edged sheets with fine filaments; wraps top to bottom."""
    h = VEIL_H
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    a = np.zeros((h, W))
    for _ in range(10):
        cx, cy = rng.uniform(-60, W + 60), rng.uniform(0, h)
        rx, ry, amp = rng.uniform(140, 260), rng.uniform(40, 90), rng.uniform(0.6, 1.0)
        for dy in (-h, 0, h):
            a = np.maximum(a, np.exp(-(((xx - cx) / rx) ** 2 + ((yy - cy - dy) / ry) ** 2) * 2.2) * amp)
    tex = raster.fbm(W, h, 40, seed + 1, octaves=4, period=True)
    st = raster.fbm(W, h, 8, seed + 2, octaves=2, period=True)
    a = np.clip(a * (0.5 + 0.8 * tex) * (0.85 + 0.3 * st), 0, 1)
    a = blur(a, 1.5, True) * 0.16
    rgb = np.zeros((h, W, 3)) + np.array((222, 206, 150.0))
    return rgba(rgb, a)


# =========================================================================== build

PIECE_SPEC = {}


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()
    PIECE_SPEC.clear()
    placed = []

    def save(name, img, layer=None, frames=None, fps=None):
        if frames:
            for i, f in enumerate(img):
                artkit.save_png(f, OUT / f"{name}_{i}.png", SOURCE)
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

    save("io-space", io_space())
    for k, name in enumerate(GROUND_TILES):
        save(name, ground_tile(KINDS[k + 1]))
    save("pele-veil", pele_veil())
    for name, (img, lo) in strip_pieces().items():
        save(name, img, "ground")
        placed.append({"piece": name, "t": exact_t(lo, img.height), "x": 240,
                       "note": f"section 1's ground: rows {lo}-{lo + img.height} (exact)"})
    for i in range(2, len(SECTIONS)):
        img, lo = seam_piece(i)
        name = f"seam-{i + 1}"
        save(name, img, "ground")
        placed.append({"piece": name, "t": exact_t(lo, img.height), "x": 240,
                       "note": f"section {i + 1}'s seam: rows {lo}-{lo + img.height} (exact)"})
    rows_t = tower_rows()
    for works, seed in (("loki", 3961), ("pele", 3963), ("prometheus", 3965)):
        img, lo = yard_piece(works, seed)
        name = f"{works}-works"
        save(name, img, "ground")
        placed.append({"piece": name, "t": exact_t(lo, img.height), "x": 240,
                       "note": f"the {works.capitalize()} works' yard under its towers (exact rows {lo}-"
                               f"{lo + img.height})"})
    for side in ("west", "east"):
        body, g = pipes_piece(side)
        save(f"pipes-{side}", body, "ground")
        save(f"pipes-{side}-glow", g, "ground")
    for works, (groups, rack) in WORKS.items():
        for g in groups:
            tx, ty = rows_t[g]
            side = "west" if tx < rack else "east"
            mid = (tx + 32 + rack - RACK_W) / 2 if side == "west" else (tx - 32 + rack + RACK_W) / 2
            placed.append({"piece": f"pipes-{side}", "t": centre_t(ty), "x": round(mid, 1), "dark_with": g,
                           "note": f"{g}'s pipe run to the rack; its glow goes dark with the group"})
    body, g = pump_rack()
    save("pump-rack", body, "ground")
    save("pump-rack-glow", g, "ground")
    for name, x, t in PUMPS:
        placed.append({"piece": "pump-rack", "t": centre_t(unit_row(t, PUMP_HALF)), "x": x, "dark_with": name,
                       "note": f"pump station {name}'s rack segment; its glow goes dark with the pump"})
    save("pele-gate", gate_piece(), "ground")
    sen_t = next(pl for kind, _, pl in GUARDS if kind == "sentinel-tower")[0][0]
    placed.append({"piece": "pele-gate", "t": centre_t(unit_row(sen_t, 32) - GATE_H / 2 + GATE_LEFT_Y), "x": 240,
                   "note": "the Pele works' gate: the Sentinels on its blockhouses (x 64; x 416 1.7 s later)"})
    save("supply-depot", depot_piece(), "ground")
    placed.append({"piece": "supply-depot", "t": centre_t(unit_row(DEPOT_T, 12) + 28), "x": DEPOT_X,
                   "note": "the supply depot before the Pele gate; its latch (the 10 HP ground object) on its door"})
    save("survey-hut", hut_piece(), "ground")
    hx, hrow = hut_place()
    placed.append({"piece": "survey-hut", "t": centre_t(hrow + 15), "x": round(hx, 1),
                   "note": "the survey hut on its lava island in the channel; its lock (the 30 HP trigger) on its door"})
    for k, name in enumerate(VENT_PIECES):
        save(name, vent_piece(k), "ground")
    for i, (t, x, first, note) in enumerate(all_vents()):
        placed.append({"piece": VENT_PIECES[i % 3], "t": centre_t(vent_row(t)), "x": x,
                       "note": f"vent {i + 1} (enters t {t:g}): {note}" if i % 3 == 0 or "hold" in note else None})
    for p in placed:
        if p["piece"].endswith("-glow"):
            raise AssertionError(p)
    return dict(PIECE_SPEC), placed


# =========================================================================== the proposal

def tile_heights():
    return {name: Image.open(OUT / f"{name}.png").height for name in GROUND_TILES + ["io-space", "pele-veil"]}


def fmt(v):
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, dict):
        return "{" + ", ".join(f"{k}: {fmt(x)}" for k, x in v.items()) + "}"
    if isinstance(v, float):
        return f"{v:g}"
    if isinstance(v, str) and any(c in v for c in "':#,{}[]"):
        return '"' + v + '"'
    return str(v)


def eruptions_in_hold(works, vt, first, difficulty="medium", period=5.0):
    """Throws of a vent (entering at script time vt) inside its works' hold window at a difficulty, with the towers
    alive through the window (their worst case), one period throughout: (count, times after the hold's start)."""
    hs = hold_start(works)
    speed = HOLD_SPEEDS[difficulty]
    dt = 0.01
    t_script, t_real, y = vt, 0.0, SCREEN                    # the vent's y (up) from its entry
    tower_y = SCREEN + TOWER_HALF - (vt - min(t for g, _, t in TOWERS if g in WORKS[works][0])) * SPEED
    tell = TELL[difficulty]
    inside, pending, nxt = [], [], first
    held = None
    while y > 0 and t_real < 40:
        if held is None and t_script >= hs:
            held = t_real
        v = SPEED if held is None else SPEED + (speed - SPEED) * min(1.0, (t_real - held) / HOLD_RAMP)
        if t_real >= nxt:                                    # a tell starts (the vent is on the screen)
            pending.append(t_real + tell + RISE)
            nxt += period
        if pending and t_real >= pending[0]:                 # its rocks leave the vent
            pending.pop(0)
            if held is not None:
                inside.append(round(t_real - held, 1))
        t_real += dt
        t_script += dt * v / SPEED
        y -= v * dt
        tower_y -= v * dt
        if held is not None and tower_y < -TOWER_HALF:       # the window's end (the tower leaves: failed)
            break
    return len(inside), inside


def throw_y(t_v, first, difficulty):
    """A vent outside the holds: its y (up) when it throws at 130 px/s."""
    return SCREEN - SPEED * (first + TELL[difficulty] + RISE)


def proposal_text(pieces, placed):
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    h = tile_heights()
    L = ["backdrop:",
         f"  # Presentation only: Level 39's Io (tools/art/backdrop_l39.py, step ART-E, sections from {src}).",
         "  # Section 1 has no ground tile set: its ground is the two tvashtar pieces (exact rows, opaque from row -16),",
         "  # so Io's plains cover the screen from t 0 (round 77 F.1 redo: no Jupiter on a top-down ground level; io-space",
         "  # on deep is never seen). Every other section's ground tile is periodic in 1040",
         "  # rows; the seam pieces (exact rows) carry the racks' bends between sections. `dark_with` (E4's",
         "  # BackdropPieces hook, presentation only): the piece's -glow is no longer drawn once that tower's group is",
         "  # cleared (GROUP_CLEARED) or that pump station dies (P1-P8 = the pump stations in ground_placements order).",
         "  # Motion: no animated piece; the vents, the towers' flares and the rocks are sprites.",
         "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far, low-air unused",
         "  ramp: 2",
         "  haze_colour: d8cc98   # Pele's sulphur haze (no haze is used: the ground covers deep)",
         "  atmosphere:",
         "    clear: {haze: 0.0}",
         "    light: {haze: 0.0, wisps: pele-veil}   # sections 4-5: Pele's plume haze over the play field (<= 16 %)",
         "  tile_sets:",
         f"    io-space: {{layer: deep, height: {h['io-space']}}}   # black space, stars (never seen: the ground covers it)"]
    notes = {"loki-yard": "section 2: the yard road (x ~70), the rack (x 240)",
             "pipeline-run": "section 3: the lava channel (x ~300), the rack (x 150)",
             "pele-yard": "section 4: Pele's fallout, the road (x ~180), the rack (x 240)",
             "plume-field": "section 5: pale fallout, vent crusts, the rack (x 150)",
             "prometheus-yard": "section 6: frost, the road (x ~90), the rack (x 250)"}
    for name in GROUND_TILES:
        L.append(f"    {name}: {{layer: ground, height: {h[name]}}}   # {notes[name]}")
    L.append(f"    pele-veil: {{layer: high-air, height: {h['pele-veil']}}}   # the light atmosphere's wisps (normal)")
    L.append("  pieces:")
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        L.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    L.append("  placed:")
    for p in placed:
        if p.get("note"):
            L.append(f"    # {p['note']}")
        extra = f", dark_with: {fmt(p['dark_with'])}" if p.get("dark_with") else ""
        L.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {fmt(float(p['x']))}{extra}}}")
    L.append("sections:   # ART-E's reading of the README's Layout (130 px/s throughout); E2 writes the real ones")
    for s in SECTIONS:
        L.append("  - " + "{" + ", ".join(f"{k}: {fmt(v)}" for k, v in s.items() if k != "notes") + "}")
    L.append("holds:   # README Layout: when a works' first tower reaches 200 px below the top edge; window 9.7 s")
    for works, (groups, _) in WORKS.items():
        L.append(f"  - {{groups: [{', '.join(groups)}], depth: {HOLD_DEPTH}, speed: 40, easy: {{speed: 30}}, "
                 f"hard: {{speed: 50}}, ramp: 1}}   # starts at script t {hold_start(works):.2f}")
    L.append("ground_placements:   # at: [t, x] (the centre entering half its size above the top edge), as Level 37's")
    L.append('  - target: "cracking towers, each its own group (96x96, hitbox 72x72; on their works\' pads)"')
    L.append("    enemy: cracking-tower")
    L.append("    groups: [" + ", ".join(g for g, _, _ in TOWERS) + "]")
    L.append("    at: [" + ", ".join(f"[{t:g}, {x}]" for _, x, t in TOWERS) + "]")
    L.append('  - target: "pump stations P1-P8 on the racks (40x40; escapes pump-station, allowed 2)"')
    L.append("    enemy: pump-station")
    L.append("    at: [" + ", ".join(f"[{t:g}, {x}]" for _, x, t in PUMPS) + "]")
    for kind, note, at in GUARDS:
        L.append(f'  - target: "{note}"')
        L.append(f"    enemy: {kind}")
        L.append("    at: [" + ", ".join(f"[{t:g}, {x}]" for t, x in at) + "]")
    hx, hrow = hut_place()
    L.append("ground_objects:   # the survey hut's lock (the secret's trigger, 30 HP: E1's `hp`) and the depot's latch")
    L.append(f"  - {{t: {HUT_T:g}, x: {hx:.0f}, size: [20, 20], hp: 30, crate: 100, secret: survey hut, "
             "look: survey-hut-lock}   # on the hut's door (its piece above)")
    L.append(f"  - {{t: {DEPOT_T:g}, x: {DEPOT_X:g}, size: [20, 20], hp: 10, drop: armour-patch, look: depot-latch}}"
             "   # on the depot's door")
    L.append("eruptions:   # 27 vents (README Hazards; m10e-gaps §4: the works' vents retimed into their holds, QE-1 a's")
    L.append("  # `first` = s after the vent enters to its first tell; then every period on real time). No vent within")
    L.append("  # 100 px of a tower or a pump station (checked by --check). Outside the holds `first` makes the rocks")
    L.append("  # leave in the lower half (medium throw y up ~150-300); in a hold the first throw ~1.5 s after it starts.")
    L.append("  vents:")
    for t, x, first, note in all_vents():
        L.append(f"    - {{t: {t:g}, x: {x}, first: {first:g}, period: [5, 7], rocks: [3, 5]}}   # {note}")
    L.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); deep "
             f"{int(round(scroll(END) * FACTORS['deep']))}, high-air {int(round(scroll(END) * FACTORS['high-air']))}")
    return "\n".join(L) + "\n"


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data


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
    return arr[h - 1 - (rows % h)], rows


def piece_y(p_t, factor, t):
    return SCREEN - (scroll(p_t) * factor + SCREEN / 2 - scroll(t) * factor)


def held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def compose(t, prop=None, units=True, extra=None, dead=()):
    """The screen at t (480x540, script time): deep (io-space), the ground tiles by row, the ground pieces
    (glows added unless ``dark_with`` is in ``dead``), the ground units' stand-ins, ``extra(canvas, t)``, Pele's veil."""
    prop = prop or load_proposal()
    bd = prop["backdrop"]
    specs, placed = bd["pieces"], bd["placed"]
    arr, _ = tile_rows("io-space", scroll(t) * FACTORS["deep"])
    canvas = Image.fromarray(arr.copy(), "RGBA")
    for p in placed:
        sp = specs[p["piece"]]
        if sp["layer"] != "deep":
            continue
        f = frames_of(p["piece"], sp)[0]
        y = piece_y(p["t"], FACTORS["deep"], t)
        if -f.height < y < SCREEN + f.height:
            l23kit.paste_c(canvas, f, p["x"], y)
    g_arr = np.array(canvas)
    off = scroll(t)
    rows = int(round(off)) + SCREEN - 1 - np.arange(SCREEN)
    for i, sec in enumerate(SECTIONS):
        names = [n for n in sec["tiles"] if n in GROUND_TILES]
        if not names:
            continue
        tarr, _ = tile_rows(names[0], off)
        sel = np.array([section_at_row(r) == i and r >= seam("ground", i) for r in rows])
        g_arr[sel] = tarr[sel]
    canvas = Image.fromarray(g_arr, "RGBA")
    glows = []
    for p in placed:
        sp = specs[p["piece"]]
        if sp["layer"] != "ground":
            continue
        fr = frames_of(p["piece"], sp)
        f = fr[int(t * sp.get("fps", 0)) % len(fr)] if sp.get("frames") else fr[0]
        y = piece_y(p["t"], 1.0, t)
        if -f.height / 2 < y + f.height / 2 and y - f.height / 2 < SCREEN:
            l23kit.paste_c(canvas, f, p["x"], y)
            gname = p["piece"] + "-glow"
            if gname in specs and p.get("dark_with") not in dead:
                glows.append((frames_of(gname, specs[gname])[0], p["x"], y))
    for g, x, y in glows:
        canvas = l23kit.add_c(canvas, g, x, y)
    if units:
        canvas = stand_ins(canvas, t, prop, dead)
    if extra:
        canvas = extra(canvas, t)
    if atmosphere_at(t) == "light":
        varr, _ = tile_rows("pele-veil", scroll(t) * FACTORS["high-air"])
        canvas.alpha_composite(Image.fromarray(varr, "RGBA"))
    return canvas


def atmosphere_at(t):
    for i, s in enumerate(SECTIONS):
        if t < float(s["end"]) or i == len(SECTIONS) - 1:
            return s["atmosphere"]


def ground_y(t_enter, half, t):
    """A ground unit's screen y (down) at t."""
    return SCREEN - (unit_row(t_enter, half) - scroll(t))


def stand_ins(canvas, t, prop, dead=()):
    tower, flare = held("cracking-tower"), held("cracking-tower-flare")
    wreck = held("cracking-tower-wreck")
    pump, pump_w = held("pump-station"), held("pump-station-wreck")
    sam_base, sam = held("sam-nest-base"), held("sam-nest")
    sen, sen_head = held("sentinel-tower"), held("sentinel-tower-head")
    legs, torso = held("strider-legs"), held("strider")
    latch, lock = held("depot-latch"), held("survey-hut-lock")
    for g, x, te in TOWERS:
        y = ground_y(te, TOWER_HALF, t)
        if -60 < y < SCREEN + 60 and tower:
            if g in dead and wreck:
                l23kit.paste_c(canvas, wreck[0], x, y)
            else:
                l23kit.paste_c(canvas, tower[0], x, y)
                canvas = l23kit.add_c(canvas, flare[int(t * 10) % len(flare)], x, y)
    for name, x, te in PUMPS:
        y = ground_y(te, PUMP_HALF, t)
        if -30 < y < SCREEN + 30 and pump:
            l23kit.paste_c(canvas, (pump_w if name in dead else pump)[0], x, y)
    for kind, _, at in GUARDS:
        for te, x in at:
            half = {"sam-nest": 24, "sentinel-tower": 40, "strider": 48}[kind]
            y = ground_y(te, half, t)
            if not -60 < y < SCREEN + 60:
                continue
            if kind == "sam-nest" and sam_base and sam:
                l23kit.paste_c(canvas, sam_base[0], x, y)
                l23kit.paste_c(canvas, sam[0], x, y)
            elif kind == "sentinel-tower" and sen:
                l23kit.paste_c(canvas, sen[0], x, y)
                if sen_head:
                    l23kit.paste_c(canvas, sen_head[0], x, y)
            elif kind == "strider" and legs and torso:
                l23kit.paste_c(canvas, legs[int(t * 8) % len(legs)], x, y)
                l23kit.paste_c(canvas, torso[0], x, y)
    hx, _ = hut_place()
    y = ground_y(HUT_T, 12, t)
    if -20 < y < SCREEN + 20 and lock:
        l23kit.paste_c(canvas, lock[0], hx, y)
    y = ground_y(DEPOT_T, 12, t)
    if -20 < y < SCREEN + 20 and latch:
        l23kit.paste_c(canvas, latch[0], DEPOT_X, y)
    return canvas


def air_stand_ins(canvas, t, kinds=("talon", "chimera")):
    for k, kind in enumerate(kinds):
        fr = held(kind)
        if not fr:
            continue
        for j in range(3):
            x = 120 + 120 * j
            y = 60 + 40 * k + 20 * abs(j - 1)
            l23kit.paste_c(canvas, fr[0], x, y)
    ship = artkit.load_frames("ship")
    orb, needle = held("orb"), held("needle")
    if orb and needle:
        for k in range(4):
            l23kit.paste_c(canvas, needle[0], 200 + 22 * k, 240 + 30 * k)
        l23kit.paste_c(canvas, orb[0], 330, 300)
    if ship:
        l23kit.paste_c(canvas, ship[2], 240, 470)
    return canvas


# =========================================================================== review

SHOTS = [(0.0, "T=0 IO'S PLAINS"), (6.0, "T=6 THE TVASHTAR PLAINS"), (12.5, "T=12.5 THE FIRST VENT"),
         (38.0, "T=38 LOKI: SAMS, ROAD"), (45.0, "T=45 THE LOKI HOLD"), (70.0, "T=70 PIPELINE RUN, P1"),
         (98.0, "T=98 THE SURVEY HUT"), (106.0, "T=106 THE DEPOT"), (112.5, "T=112.5 THE PELE GATE"),
         (127.0, "T=127 THE PELE HOLD"), (151.0, "T=151 THE PLUME FIELD"), (177.0, "T=177 PROMETHEUS HOLD")]


def windows(t, w=200, h=96):
    im = compose(t, units=False)
    lum = np.array(im.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    best = []
    for y in range(0, SCREEN - h, 12):
        for x in range(0, W - w, 12):
            best.append((lum[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return best[0], best[-1], lum.mean(), im


def read_cell(bg, x, y):
    """The readability set at 1x over a 200x96 window: a Talon and a Chimera (gold), amber needles and an orb, a
    burning rock, the ship."""
    c = bg.crop((x, y, x + 200, y + 96)).copy()
    for k, kind in enumerate(("talon", "chimera", "harrow")):
        fr = held(kind)
        if fr:
            l23kit.paste_c(c, fr[0], 26 + 40 * k, 30)
    orb, needle = held("orb"), held("needle")
    rock, rglow = held("eruption-rock"), held("eruption-rock-glow")
    for k in range(3):
        if needle:
            l23kit.paste_c(c, needle[0], 140 + 10 * k, 18 + 12 * k)
    if orb:
        l23kit.paste_c(c, orb[0], 180, 30)
    if rock:
        l23kit.paste_c(c, rock[3], 120, 72)
        c = l23kit.add_c(c, rglow[0], 120, 72)
    ship = artkit.load_frames("ship")
    if ship:
        l23kit.paste_c(c, ship[2], 160, 70)
    return c


def hold_gif(works="loki", seconds=8.0, fps=10):
    """The hold at medium on real time: the scroll easing to 40 px/s, a vent beside the towers telling, bursting and
    throwing its rocks in a fan, its umbrella; Loki A falling at 4.5 s."""
    tell, burst = held("eruption-vent-tell"), held("eruption-vent-burst")
    rocks, rglow, umb = held("eruption-rock"), held("eruption-rock-glow"), held("eruption-umbrella")
    fall, fall_g = held("cracking-tower-fall"), held("cracking-tower-fall-glow")
    hs = hold_start(works) - 0.8
    vents = [(t, x, f) for t, x, f, note in all_vents() if note.startswith(works)]
    prop = load_proposal()
    rng = np.random.default_rng(3971)
    fans = [[np.radians(90 + rng.uniform(-50, 50)) for _ in range(4)] for _ in vents]
    frames = []
    t_script, held_at = hs, None
    dt = 1 / fps
    groups = WORKS[works][0]
    for i in range(int(seconds * fps)):
        tr = i * dt
        if held_at is None and t_script >= hold_start(works):
            held_at = tr
        dead = (groups[0],) if tr > 5.5 else ()
        canvas = compose(t_script, prop, dead=dead)
        if 4.5 <= tr < 5.5 and fall:
            _, gx, gte = next(tw for tw in TOWERS if tw[0] == groups[0])
            gy = ground_y(gte, TOWER_HALF, t_script)
            k = min(len(fall) - 1, int((tr - 4.5) * 12))
            l23kit.paste_c(canvas, fall[k], gx, gy)
            canvas = l23kit.add_c(canvas, fall_g[k], gx, gy)
        for (vt, vx, first), fan in zip(vents, fans):
            age = tr - (vt - hs)                              # real s since the vent entered (1:1 before the hold)
            vy = SCREEN - (vent_row(vt) - scroll(t_script))
            for cyc in range(3):
                t0 = first + 5.6 * cyc
                u = age - t0
                if 0 <= u < 1.2 and tell:
                    canvas = l23kit.add_c(canvas, tell[min(11, int(u / 1.2 * 12))], vx, vy)
                b = u - 1.2
                if 0 <= b < 0.5 and burst:
                    canvas = l23kit.add_c(canvas, burst[min(7, int(b / 0.5 * 8))], vx, vy)
                s = u - 1.7
                if 0 <= s < 4.0 and umb:
                    l23kit.paste_c(canvas, umb[min(11, int(s / 4 * 12))], vx, vy)
                if 1.2 <= u < 4.6 and rocks:
                    for j, ang in enumerate(fan):
                        if u < 1.7:
                            sc, x, y = 0.45 + 0.55 * (u - 1.2) / 0.5, vx, vy
                        else:
                            fs = min(u - 1.7, 2.5)
                            dist = 120 * fs - 12 * fs * fs
                            x, y = vx + np.cos(ang) * dist, vy - np.sin(ang) * dist
                            sc = 1.0 if u < 4.2 else 1 - 0.55 * (u - 4.2) / 0.4
                        fr = rocks[(i + 3 * j) % 16]
                        if sc < 0.999:
                            sz = max(4, int(24 * sc))
                            fr = fr.resize((sz, sz), Image.LANCZOS)
                        l23kit.paste_c(canvas, fr, x, y)
                        canvas = l23kit.add_c(canvas, rglow[0], x, y, sc)
        frames.append(canvas.resize((W // 2, SCREEN // 2), Image.BOX))
        v = SPEED if held_at is None else SPEED + (HOLD_SPEEDS["medium"] - SPEED) * min(1.0, (tr - held_at) / HOLD_RAMP)
        t_script += dt * v / SPEED
    return frames


def opening_gif(seconds=10.0, fps=10):
    """The first 10 s at 130 px/s (half size): Io's plains under the ship from t 0, no limb, no Jupiter."""
    prop = load_proposal()
    ship = artkit.load_frames("ship")
    frames = []
    for i in range(int(seconds * fps)):
        canvas = compose(i / fps, prop)
        if ship:
            l23kit.paste_c(canvas, ship[2], 240, 470)
        frames.append(canvas.resize((W // 2, SCREEN // 2), Image.BOX))
    return frames


def review():
    prop = load_proposal()
    specs = prop["backdrop"]["pieces"]
    shots = [(compose(t, prop, extra=air_stand_ins if 40 < t < 50 else None), lab) for t, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    reads_t = [(10.0, "S1"), (30.0, "S2"), (90.0, "S3"), (120.0, "S4"), (156.0, "S5"), (185.0, "S6")]
    read_h = 16 + 2 * (96 + 20)
    pieces_h = 170
    tile_h = P // 6 + 40
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + read_h + 30 + pieces_h + tile_h + 30,
                         "LEVEL 39 BACKDROP (FINAL R77 B): IO FLYBY", f"PRODUCTION ART, {BATCH.upper()} - R77 F.1 REDO")
    raster.draw_text(sheet, 16, 38, "TWELVE SCREENS AT HALF SIZE WITH STAND-INS (TOWERS, PUMPS, SAMS, SENTINELS, STRIDERS, "
                                    "THE LATCH AND THE LOCK AT THEIR PROPOSED PLACES)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X: EACH SECTION'S DARKEST (TOP) AND BRIGHTEST (BOTTOM) 200X96 WINDOW "
                                   "WITH A TALON, A CHIMERA, A HARROW, AMBER NEEDLES, AN ORB, A ROCK, THE SHIP",
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
    raster.draw_text(sheet, 16, y, "PIECES (HALF SIZE): THE WORKS, THE GATE, THE DEPOT, THE HUT, THE "
                                   "VENTS, THE PIPES AND THE PUMP RACK WITH THEIR GLOWS ADDED", raster.LABEL)
    x = 16
    for name in ["loki-works", "pele-works", "prometheus-works", "pele-gate",
                 "supply-depot", "survey-hut"] + VENT_PIECES + ["pipes-west", "pipes-east", "pump-rack"]:
        sp = specs[name]
        f = frames_of(name, sp)[0]
        bg = Image.new("RGBA", f.size, (24, 22, 20, 255))
        bg.alpha_composite(f)
        if name + "-glow" in specs:
            bg = l23kit.add_c(bg, frames_of(name + "-glow", specs[name + "-glow"])[0], f.width / 2, f.height / 2)
        k = 4 if max(f.width, f.height) > 300 else 2
        small = bg.resize((max(1, f.width // k), max(1, f.height // k)), Image.BOX)
        if x + small.width > sheet.width - 16:
            break
        sheet.alpha_composite(small, (x, y + 16))
        x += small.width + 8
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (SIXTH SIZE): " + ", ".join(n.upper() for n in GROUND_TILES)
                     + "; IO-SPACE; PELE-VEIL (ON BLACK)", raster.LABEL)
    x = 16
    for name in GROUND_TILES + ["io-space", "pele-veil"]:
        tl = img(name)
        small = tl.resize((tl.width // 6, tl.height // 6), Image.BOX)
        pl = Image.new("RGBA", small.size, (0, 0, 0, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl, (x, y + 16))
        x += small.width + 10
    png, gif = artkit.review_paths(CONCEPT, "backdrop", VARIANT)
    sheet.convert("RGB").save(png, optimize=True)
    import enemies_r60 as e60  # noqa: E402
    size = e60.rig.write_gif(opening_gif(), gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB)")
    for lab, t, m, d, b in means:
        print(f"{lab} t={t}: mean {m:.0f}, darkest window {d:.0f}, brightest {b:.0f}")


# =========================================================================== check

def seam_jump(i):
    """The luminance change across ground seam i at the screen vs. the rows round it."""
    t = STARTS[i] + 1.0
    im = compose(t, units=False)
    lum = np.array(im.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    y = int(round(SCREEN - (seam("ground", i) - scroll(t)))) - 1
    if not 2 <= y < SCREEN - 3:
        return 0.0
    d = lambda a, b: np.abs(lum[a] - lum[b]).mean()  # noqa: E731
    ref = np.median([d(k, k + 1) for k in range(max(0, y - 30), min(SCREEN - 1, y + 30))])
    return d(y, y + 1) / max(ref, 0.5)


def check():
    """Tiles opaque and wrapping (BackdropSeamsTest's measure), the seams and the strip's top invisible, the veil's
    wrap, sizes as the proposal, Source chunks, no checker (DitherGuardTest's measure under 5 %), smooth glows and
    hazes (>= 32 levels), the vents >= 100 px from every tower and pump station, the works' vents erupting >= 2 times
    in their hold at medium, the open vents throwing in the lower half, at most two animated pieces on the screen, the
    atlas area."""
    problems = []
    prop = load_proposal()
    pieces, placed = prop["backdrop"]["pieces"], prop["backdrop"]["placed"]
    for name in GROUND_TILES + ["io-space"]:
        a = np.array(img(name))
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    a = np.array(img("pele-veil"))
    if wrap_score_alpha(a) > 1.5:
        problems.append(f"pele-veil: wrap score {wrap_score_alpha(a):.2f}")
    for i in range(1, len(SECTIONS)):
        j = seam_jump(i)
        if j > 2.0:
            problems.append(f"ground seam {i}: a visible jump ({j:.2f}x the rows round it)")
    # the exact pieces against the tiles they meet: their outer rows must be the tile's rows
    for p in placed:
        if not (p["piece"].startswith("seam-") or p["piece"] == "tvashtar-b"):
            continue
        im = np.array(img(p["piece"])).astype(np.float64)
        top_row = int(round(scroll(p["t"]) + SCREEN / 2 + im.shape[0] / 2)) - 1
        i = section_at_row(top_row)
        tile = np.array(img(GROUND_TILES[i - 1])).astype(np.float64)
        th = len(tile)
        diff = np.abs(im[0, :, :3] - tile[th - 1 - (top_row % th), :, :3]).mean()
        if diff > 3:
            problems.append(f"{p['piece']}: its top row differs from {GROUND_TILES[i - 1]} by {diff:.1f}")
        if p["piece"].startswith("seam-"):
            bot_row = top_row - im.shape[0] + 1
            k = section_at_row(bot_row)
            tile = np.array(img(GROUND_TILES[k - 1])).astype(np.float64)
            th = len(tile)
            diff = np.abs(im[-1, :, :3] - tile[th - 1 - (bot_row % th), :, :3]).mean()
            if diff > 3:
                problems.append(f"{p['piece']}: its bottom row differs from {GROUND_TILES[k - 1]} by {diff:.1f}")
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
    for name in ["pele-veil"]:
        a = np.array(Image.open(OUT / f"{name}.png"))[..., 3]
        lv = len(np.unique(a[(a > 0) & (a < 255)]))
        if lv < 32:
            problems.append(f"{name}: {lv} alpha levels (< 32)")
    for name in ["pipes-west-glow", "pump-rack-glow"]:
        a = np.array(Image.open(OUT / f"{name}.png"))
        lv = len({tuple(c) for c in a[..., :3].reshape(-1, 3)})
        if lv < 32 or not (a[..., 3] % 255 == 0).all():
            problems.append(f"{name}: not a smooth additive glow ({lv} colours)")
    # vents vs. towers and pumps (ground px, holds ignored: the README's rule)
    things = [(g, x, unit_row(t, TOWER_HALF)) for g, x, t in TOWERS] + [(n, x, unit_row(t, PUMP_HALF))
                                                                         for n, x, t in PUMPS]
    vents = all_vents()
    if len(vents) != 27:
        problems.append(f"{len(vents)} vents (README: 27)")
    near = []
    for t, x, first, note in vents:
        vr = vent_row(t)
        for name, tx, tr in things:
            dist = np.hypot(x - tx, vr - tr)
            if dist < 100:
                problems.append(f"vent t {t:g} x {x}: {dist:.0f} px from {name}")
            near.append(dist)
    print(f"vents: 27 placed, the nearest tower or pump {min(near):.0f} px away")
    for works in WORKS:
        for t, x, first, note in vents:
            if not note.startswith(works):
                continue
            counts = {}
            for diff in ("easy", "medium", "hard"):
                for period in (5.0, 7.0):
                    counts[(diff, period)] = eruptions_in_hold(works, t, first, diff, period)
            med = min(counts[("medium", 5.0)][0], counts[("medium", 7.0)][0])
            print(f"  {works} vent x {x} (t {t:g}, first {first:g}): throws in the hold medium "
                  f"{counts[('medium', 5.0)][1]} / {counts[('medium', 7.0)][1]} (period 5 / 7), easy "
                  f"{counts[('easy', 7.0)][0]}, hard {counts[('hard', 7.0)][0]} (period 7)")
            if med < 2:
                problems.append(f"{works} vent x {x}: {med} throws in the hold at medium (< 2)")
    for t, x, first, note in vents:
        if "hold" in note:
            continue
        ys = {d: throw_y(t, first, d) for d in TELL}
        if not (100 <= ys["easy"] and ys["hard"] <= 340):
            problems.append(f"vent t {t:g}: throws at y {ys['easy']:.0f} (easy) .. {ys['hard']:.0f} (hard)")
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = set()
        for p in placed:
            spec = pieces.get(p["piece"])
            if not spec or not spec.get("frames"):
                continue
            f = FACTORS[spec["layer"]]
            y = SCREEN - (scroll(p["t"]) * f + SCREEN / 2 - scroll(t) * f)
            if y + spec["size"][1] / 2 > 0 and y - spec["size"][1] / 2 < SCREEN:
                animated.add((p["piece"], p["t"]))
        if len(animated) > 2:
            problems.append(f"t={t}: {sorted(animated)} animated at once")
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
