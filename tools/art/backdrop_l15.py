#!/usr/bin/env python3
"""Production art: the Level 15 backdrop, Olympus Descent (design/campaign/act-3-red-dust/
level-15-olympus-descent; M7 part B batch, concept round 38, step ART-B).

Straight to production from the Olympus mood still `mars-olympus-r36-a` (tools/concept/mars_r36.py:
its Mars ramps, the caldera's nested pits, radial lava channels, the water-ice cloud tones with pale
peach tops and cool mauve undersides, imported unchanged) and the art direction's Mars approach,
parallax r02-c (rust ground, ochre dust plumes on low-air, dust streaks on high-air). Lit from the
upper left, receding (rule 3), posterized per image, wide gradients ordered-dithered.

**The descent** (sections 1-2) has no ground: the `deep` layer (x0.15) carries Mars from high up,
`mars-high` (the Tharsis plain at a small scale: dark lava lobes, crater fields with wind streaks,
graben) with the piece `olympus` (the caldera's nested pits, the radial flank and its basal scarp,
passing under the corridor in section 1), then `mars-flank` (the flank closer: leveed lava channels
and lobate flows running along the flight). The piece `mars-blend` hides the deep seam between them
(exact rows, re-run after the data moves section 2's start). `high-air` (x2.2, additive at 40 % by
the game) carries the thin water-ice `cirrus` (sections 1-2; an empty band where its tile set ends at
section 3's seam) and `entry-streaks`, the plasma streaks and glow of the entry: the wisps of the
`clear` atmosphere, which the level's entry-glow peak (0-8 s) uses, so the game's atmosphere ramp
fades them out round t=8. The Mars transport (`transport`, a high-air piece) passes in the first 3 s
with its drop bay open; it is drawn as a huge overhead pass (75 %, normal blending: rule 5), which
needs a data key and the game's draw (see the proposal). `low-air` (x1.35): the thick deck as the
`medium` atmosphere's banks (`deck-banks`, ~30 %, opaque cores, section 2), the thin dust veils of
`light` (`dust-thin`), the crest streaks of the dunes (`dust-streaks`, section 5's own tile set) and
the dust peak's banks (`dust-peak`, `heavy`, 170-180 s, translucent so the Burrowers' bulges read).

**The close-over** (risk 7): the deep -> ground swap is a horizontal line where the ground's opaque
tiles begin. The ground piece `slopes-lead` (the slopes' tile set continued down below section 3's
seam, exact rows) brings that line forward so that it crosses the screen at t~61.4-64.6, and the
opaque low-air piece `deck-close` (a 1,000 px core: the whole field white for ~2 s round t=63) passes
over it with the line inside its core all the way (`--check` proves it); the deck parts over the
slopes by t~66, two seconds before the first Burrower.

**The ground** (sections 3-5, opaque, wrapping): `slopes` (the lower flank: stepped lava terraces with
lobate fronts, basalt knobs, dusty regolith fields with burrow furrows), `escarpment` (the foot of the
Lycus escarpment: regolith, scree fans and boulders toward the cliff on the right), `dunes` (the
Tharsis dune sea: crests across the wind from the left, dark slip faces, dark sand in the troughs).
`seam-escarpment` and `seam-dunes` hide their seams (exact rows). Set pieces: `lava-terrace` (the
mortars' terrace), `basalt-outcrop` (the turret nest's), Lycus Survey Station overrun (`lycus-pads`,
`lycus-hab` drawn flat, `lycus-road` with Vrell growths), `drop-pod-ledge` (the secret's ledge),
`crashed-lander` (the dune-crest nest's), `rail-line` with three `rail-pylon` towers; the great
cliff is `escarpment-cliff`, a perspective tower (h 1.2, k 1.25 as the canyon rule MA.1) along the
right edge with the wall texture `wall-escarpment`, three placements with ravines between them.
Pieces sit under the ground targets read from the level's data (or the defaults below).

Outputs (assets/backdrop/level-15/):
  mars-high, mars-flank                 deep tile sets 480x960
  olympus, mars-blend                   deep pieces
  slopes, escarpment, dunes             ground tile sets 480x960 (opaque)
  slopes-lead, seam-escarpment, seam-dunes   ground pieces on the swap line and the seams
  lava-terrace, basalt-outcrop, lycus-pads, lycus-hab, lycus-road, drop-pod-ledge, crashed-lander,
  rail-line                             ground pieces
  escarpment-cliff, rail-pylon          tower roofs; wall-escarpment, wall-pylon their wall textures
  deck-banks, dust-thin, dust-streaks, dust-peak   low-air tile sets; deck-close low-air piece
  cirrus, entry-streaks                 high-air tile sets; transport high-air piece
  design/campaign/act-3-red-dust/level-15-olympus-descent/concept/backdrop-final-r38-a.png/.gif
  design/campaign/act-3-red-dust/level-15-olympus-descent/concept/backdrop-proposal.yaml (--proposal)

Reads the level's data.yaml when it exists (sections with speed and `ease`, ground targets), otherwise
the README's values. Run:
python3 tools/art/backdrop_l15.py [--proposal | --review | --check | --strip t,.. out.png] [id ...]
(all: about 4 min)
"""
import math
import sys
from concurrent.futures import ProcessPoolExecutor
from functools import lru_cache

import numpy as np
import yaml
from PIL import Image, ImageFilter

import artkit
from artkit import DESIGN, ROOT

import backdrop_l08 as k8  # noqa: E402  (tools/art: relief lighting, wall quads, blit, wrap score, YAML flow)
import mars_r36 as m36  # noqa: E402  (concept script, unchanged: the Mars ramps and cloud tones)
from render import raster, sdf  # noqa: E402
from render.sdf import Material, mirror_x, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid, union  # noqa: E402

SCRIPT = "backdrop_l15.py"
BATCH = "M7 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
LEVEL_DIR = DESIGN / "campaign" / "act-3-red-dust" / "level-15-olympus-descent"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-15"
REVIEW = CONCEPT / "backdrop-final-r38-a.png"
W, SCREEN = 480, 540
MID = SCREEN / 2
TILE_H = 960
CAMERA = 6.0

FACTORS = {"deep": 0.15, "far": 0.5, "ground": 1.0, "low-air": 1.35, "high-air": 2.2}
HAZE_COLOUR = "d6a670"                  # mars_r36.SKY: the butterscotch sky
RAMP = 4
ATMOSPHERE = {
    "clear": {"wisps": "entry-streaks", "haze": 0.34},   # the entry-glow peak 0-8: streaks, the surface washed out
    "light": {"banks": "dust-thin", "haze": 0.2},         # sections 1, 3-5: thin dust veils
    "medium": {"banks": "deck-banks", "haze": 0.26},      # section 2: the thick water-ice deck in banks
    "heavy": {"banks": "dust-peak", "haze": 0.3},         # the dust peak 170-180: rolling dust, translucent
}

# --------------------------------------------------------------------------- the level's timeline

DEFAULT_SECTIONS = [   # the README's Layout until the level data exists ("each change eased over 1 s")
    {"name": "Entry Corridor", "end": 30, "speed": 190, "atmosphere": "light",
     "peak": {"atmosphere": "clear", "from": 0, "to": 8}},
    {"name": "Cloud Deck", "end": 65, "speed": 170, "atmosphere": "medium", "ease": 1},
    {"name": "Olympus Slopes", "end": 115, "speed": 150, "atmosphere": "light", "ease": 1},
    {"name": "Lycus Escarpment", "end": 150, "speed": 150, "atmosphere": "light"},
    {"name": "Tharsis Dunes", "end": 190, "speed": 160, "atmosphere": "light", "ease": 1,
     "peak": {"atmosphere": "heavy", "from": 170, "to": 180}},
]
# The ground targets the pieces sit under (README *Ground targets*), [t, x]: the target's top edge at the
# screen's top edge at t (as the game places it); the level's data replaces them.
DEFAULT_TARGETS = {
    "terrace": [[79.0, 150.0], [79.0, 214.0]],                         # Polyp Mortar x2 on the lava terrace
    "outcrop": [[91.0, 300.0], [91.0, 356.0], [91.45, 328.0]],         # Spine Turret nest x3 on the basalt outcrop
    "pads": [[118.0, 96.0], [118.0, 176.0], [119.3, 96.0], [119.3, 176.0]],   # Spine Turret x4 on the station's pads
    "hab": [[124.6, 256.0], [124.6, 300.0]],                           # Polyp Mortar x2 on the hab's roof
    "containers": [[122.0, 216.0], [123.6, 60.0], [126.2, 214.0], [127.8, 300.0]],   # supply containers (3 HP)
    "rubble": [[128.0, 400.0]],                                        # the drop pod's rubble (the secret)
    "lander": [[159.0, 128.0], [159.0, 184.0], [159.45, 156.0]],       # Spine Turret nest x3 on the crashed lander
    "rail": [[178.0, 120.0], [178.0, 360.0]],                          # hard: a Spine Turret pair at the rail line
}
DEFAULT_ROAD_X = 336.0                  # the station's road (the Creeper convoy, t=118)
SIZES = {"spine-turret": 32, "polyp-mortar": 32, "lycus-container": 24, "drop-pod-rubble": 40}


def _load(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


DATA = _load(LEVEL_DIR / "data.yaml")
# The atmospheres and peaks this backdrop is built for (the proposal); the data gives the names, ends, speeds, eases.
PROPOSED_ATMOS = [("light", {"atmosphere": "clear", "from": 0, "to": 8}), ("medium", None), ("light", None),
                  ("light", None), ("light", {"atmosphere": "heavy", "from": 170, "to": 180})]


def _sections():
    out = []
    for i, s in enumerate(DATA.get("sections") or DEFAULT_SECTIONS):
        s = dict(s)
        if i < len(PROPOSED_ATMOS):
            s["atmosphere"], peak = PROPOSED_ATMOS[i]
            s.pop("peak", None)
            if peak:
                s["peak"] = peak
        out.append(s)
    return out


SECTIONS = _sections()
SPEED = float(DATA.get("scroll_speed", 150))
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]
END = float(SECTIONS[-1]["end"])
OUTRO_END = END + 15                    # LevelData.OUTRO_SECONDS


def speed(i):
    return float(SECTIONS[i].get("speed", SPEED))


def ease(i):
    """Section i's ease-in (M7 part B, Section.easeSeconds): from the section before's speed
    to its own over its first `ease` s, smoothstep; only between two moving sections."""
    if i == 0 or speed(i) == 0 or speed(i - 1) == 0 or SECTIONS[i].get("arena"):
        return 0.0
    return float(SECTIONS[i].get("ease", SECTIONS[i].get("ramp", 0)) or 0)


def _travelled(i, a, b):
    """px section i scrolls from a to b s (inside it, or past its end)."""
    v, s = speed(i), STARTS[i]
    px = (b - a) * v
    e = ease(i)
    if e > 0:
        old = speed(i - 1)

        def lost(t):          # ∫ (1 - S(u)) du * e, u = (t - s) / e clamped: the ease's shortfall
            u = min(max((t - s) / e, 0.0), 1.0)
            return e * (u - u ** 3 + u ** 4 / 2)
        px -= (v - old) * (lost(b) - lost(a))
    return px


def scroll_at(t):
    """LevelData.scrollAt with the M7 part B ease: before the start and after the end the first and the
    last section's speed carry on."""
    scroll = 0.0
    for i, s in enumerate(SECTIONS):
        if t < s["end"] or i == len(SECTIONS) - 1:
            return scroll + _travelled(i, STARTS[i], t)
        scroll += _travelled(i, STARTS[i], s["end"])
    raise AssertionError


def t_at(scroll):
    """The level time at a ground scroll (bisection: scroll_at grows strictly)."""
    lo, hi = -60.0, OUTRO_END + 60
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll_at(mid) < scroll:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


jr = k8.java_round


def seam(layer, s):
    """LevelData.seam rounded as Backdrop rounds it: where section s's tile set begins on the layer."""
    return jr(scroll_at(STARTS[s]) * FACTORS[layer] + SCREEN)


def t_for_bottom(layer, bottom, h):
    """A placement time whose piece (height h) has its bottom edge exactly at ``bottom`` (Backdrop:
    java_round(centre - h / 2)), to 4 decimals."""
    f = FACTORS[layer]
    t = round(t_at((bottom + h / 2 - MID) / f), 4)
    for dt in (0, 1e-4, -1e-4, 2e-4, -2e-4, 3e-4, -3e-4):
        if jr(scroll_at(t + dt) * f + MID - h / 2) == bottom:
            return round(t + dt, 4)
    raise AssertionError((layer, bottom, h, t))


def t_for_centre(layer, centre):
    return round(t_at((centre - MID) / FACTORS[layer]), 3)


def target_pos(t, h):
    """A ground target's centre on the ground layer: its top edge at the screen's top edge at t."""
    return scroll_at(t) + SCREEN + h / 2


# --------------------------------------------------------------------------- the ground targets

def _targets():
    """The ground targets' spots by role from the data (sections, enemies, sprites), or the defaults."""
    found = {}
    for g in DATA.get("ground_targets") or []:
        spots = sorted({tuple(a) for v in [g] + [g.get(d) or {} for d in ("easy", "hard")] for a in v.get("at", [])})
        if not spots:
            continue
        t0 = spots[0][0]
        enemy, sprite = g.get("enemy"), g.get("sprite", "")
        role = None
        if sprite == "lycus-container" or "container" in g.get("target", ""):
            role = "containers"
        elif sprite.startswith("drop-pod") or g.get("trigger"):
            role = "rubble"
        elif enemy == "polyp-mortar":
            role = "terrace" if t0 < 100 else "hab"
        elif enemy == "spine-turret":
            role = "outcrop" if t0 < 100 else "pads" if t0 < 140 else "lander" if t0 < 170 else "rail"
        if role:
            found.setdefault(role, []).extend(list(s) for s in spots)
    return {k: found.get(k) or v for k, v in DEFAULT_TARGETS.items()}


TARGETS = _targets()


def _convoy():
    """(entry t, path x at entry, units at most) of the Creeper convoy on the station's road."""
    for w in DATA.get("waves") or []:
        if w.get("enemy") == "creeper":
            n = max([w.get("count", 3)] + [(w.get(d) or {}).get("count", 0) for d in ("easy", "hard")] + [4])
            x = float(w["paths"][0][0][0]) if w.get("paths") else DEFAULT_ROAD_X
            return float(w["t"]), x, n
    return 118.0, DEFAULT_ROAD_X, 4


CONVOY_T, ROAD_X, CONVOY_N = _convoy()
CONVOY_EVERY = 1.5                      # Formations: a convoy's units 1.5 s apart


def pad_pairs():
    """The station's turrets in pairs by side (west, east): each pair stands on one lycus-pads piece."""
    west = [a for a in TARGETS["pads"] if a[1] < 240]
    east = [a for a in TARGETS["pads"] if a[1] >= 240]
    return [p for p in (west, east) if p]


def spots_in(spots, size, w, h, cx, cpos):
    """Target spots [(t, x)] as piece coordinates (x right, y down from the top) round (cx, cpos)."""
    return [(a[1] - cx + w / 2, h / 2 - (target_pos(a[0], size) - cpos)) for a in spots]


def centre_of(spots, size):
    return sum(a[1] for a in spots) / len(spots), sum(target_pos(a[0], size) for a in spots) / len(spots)


def road_span():
    """(bottom, top) of the station's road on the ground: from below the convoy's entry to the hab it serves
    (the convoy's first point is 40 px above the top edge at each unit's entry)."""
    lo = jr(scroll_at(CONVOY_T) + SCREEN - 80)
    hx, hp = centre_of(TARGETS["hab"], 32)
    hi = jr(scroll_at(CONVOY_T + CONVOY_EVERY * (CONVOY_N - 1)) + SCREEN + 360)
    if abs(hx - ROAD_X) < 110 and hp > lo:
        hi = min(hi, jr(hp + 10))
    return lo, hi


# --------------------------------------------------------------------------- the layout

TILE_SETS = {   # id -> (layer, height, drift, note)
    "mars-high": ("deep", TILE_H, None, "section 1: Mars from high up, the Tharsis plain small (dark lava lobes, crater fields with wind streaks, graben)"),
    "mars-flank": ("deep", TILE_H, None, "sections 2-5: Olympus Mons' flank closer: leveed lava channels and lobate flows along the flight (under the ground from section 3)"),
    "slopes": ("ground", TILE_H, None, "section 3: the lower flank: stepped lava terraces, basalt knobs, dusty regolith fields with burrow furrows"),
    "escarpment": ("ground", TILE_H, None, "section 4: the foot of the Lycus escarpment: regolith, scree fans and boulders toward the cliff on the right"),
    "dunes": ("ground", TILE_H, None, "section 5: the Tharsis dune sea, crests across the wind from the left, dark sand in the troughs"),
    "cirrus": ("high-air", TILE_H, None, "sections 1-2: thin water-ice cirrus (additive at 40 %); empty where it ends at section 3's seam"),
    "entry-streaks": ("high-air", TILE_H, None, "the `clear` look's wisps: the entry's plasma streaks and glow (the entry-glow peak 0-8, faded by the ramp)"),
    "deck-banks": ("low-air", TILE_H, 5, "medium: the thick water-ice deck in banks, ~30 %, opaque cores, pale peach tops, mauve undersides"),
    "dust-thin": ("low-air", TILE_H, 8, "light: thin dust veils stretched along the wind, ~14 %, at most 35 % opaque"),
    "dust-streaks": ("low-air", TILE_H, 12, "section 5: dust streaks blown off the dune crests, ~12 %, translucent; empty where section 5 begins"),
    "dust-peak": ("low-air", TILE_H, 16, "heavy: the dust peak's rolling banks, ~30 %, at most 55 % opaque (the Burrowers' bulges read under them)"),
}
SECTION_TILES = [["mars-high", "cirrus"], ["mars-flank", "cirrus"], ["mars-flank", "slopes"],
                 ["mars-flank", "escarpment"], ["mars-flank", "dunes", "dust-streaks"]]

# the close-over: the swap line (slopes-lead's bottom) crosses the screen centred on CLOSE_T under deck-close
CLOSE_T = 63.0
DECK_CORE, DECK_FRINGE = 1000, 180
DECK_H = DECK_CORE + 2 * DECK_FRINGE
FEATHER = 32                            # rows a piece fades into the tile set it continues
BLEND_H = 320                           # a seam piece's height (the ground seams)
DEEP_BLEND_H = 400
OLYMPUS = (440, 440)
TRANSPORT = (400, 660)
TRANSPORT_T = 0.48                      # its centre passes the middle of the screen: the bay at the top edge at t=0
CLIFF_FOOT = (96, 1600)                 # the escarpment tower's footprint
CLIFF_H = 1.2                           # k = 1.25: the canyon rule MA.1's plateau scale
CLIFF_X = 480.0                         # the footprint's centre: its west edge at 432, the face 432-480
CLIFF_GAP = 120                         # the ravines between the three placements
PYLON_FOOT, PYLON_H = (14, 14), 0.9

PIECES = {      # id -> (layer, (w, h), extra spec, mid-size, note)
    "olympus": ("deep", OLYMPUS, {}, False, "Olympus Mons from the entry corridor: the caldera's nested pits, the radial flank, the basal scarp (edges transparent)"),
    "mars-blend": ("deep", (W, DEEP_BLEND_H), {}, False, "on the deep seam of section 2: mars-high into mars-flank along a lava margin (exact rows)"),
    "slopes-lead": ("ground", (W, 0), {}, False, "the slopes continued below section 3's seam: the ground's opaque edge (the swap line) crosses the screen under deck-close (exact rows)"),
    "seam-escarpment": ("ground", (W, BLEND_H), {}, False, "on section 4's seam: the slopes into the escarpment's regolith along a lava front (exact rows)"),
    "seam-dunes": ("ground", (W, BLEND_H), {}, False, "on section 5's seam: the escarpment's regolith into the dune sea (exact rows)"),
    "lava-terrace": ("ground", (W, 300), {}, True, "a raised lava terrace across the flank, flat top, lobate front: the two mortars stand on it"),
    "basalt-outcrop": ("ground", (208, 160), {}, True, "a basalt outcrop, knobs round a flat top: the turret nest of three"),
    "lycus-pads": ("ground", (160, 176), {}, True, "a pair of Lycus Survey Station's landing pads, overrun: scorched concrete, Vrell growth with teal veins (two turrets; placed twice)"),
    "lycus-hab": ("ground", (152, 152), {}, True, "the station's domed hab drawn flat (a low dome): ribs, airlock, panels, growth (two mortars on its roof)"),
    "lycus-road": ("ground", (112, 0), {}, False, "the station's graded road with tyre ruts and Vrell growths along it (the Creeper convoy)"),
    "drop-pod-ledge": ("ground", (136, 120), {}, False, "the ledge at the cliff's foot the rubble lies on: a rock shelf, the pod's impact furrow"),
    "crashed-lander": ("ground", (216, 176), {}, True, "a crashed colony lander on a dune crest, a leg folded, sand drifted against it (the nest of three)"),
    "rail-line": ("ground", (W, 104), {}, False, "the colony rail line across the field on its embankment, sleepers, cable trough"),
    "escarpment-cliff": ("ground", CLIFF_FOOT, {"tower": {"height": CLIFF_H, "wall": "wall-escarpment", "shade": 0.55}}, False,
                         "the great cliff along the right edge in perspective (scenery: nothing gameplay touches): the plateau's rim, strata on its face"),
    "rail-pylon": ("ground", PYLON_FOOT, {"tower": {"height": PYLON_H, "wall": "wall-pylon", "shade": 0.5}}, False,
                   "a rail line's catenary pylon in perspective"),
    "deck-close": ("low-air", (W, DECK_H), {}, False, "the opaque close-over: the deck covers the whole field ~2 s round t=63 while the ground takes over (1,000 px core)"),
    "transport": ("high-air", TRANSPORT, {}, False, "the Mars transport passing over the launch, its drop bay open (a huge overhead pass: 75 %, normal blending)"),
}
WALLS = {"wall-escarpment": (128, 48), "wall-pylon": (8, 32)}


def tower_scale(h):
    return CAMERA / (CAMERA - h)


def roof_size(name):
    layer, (w, h), extra, _, _ = PIECES[name]
    k = tower_scale(extra["tower"]["height"])
    return jr(w * k), jr(h * k)


# ---- the exact rows of the seam pieces

def deep_seam():
    return seam("deep", 1)


def ground_seams():
    return {s: seam("ground", s) for s in (2, 3, 4)}


def swap_line():
    """(bottom of slopes-lead on the ground layer, t it enters the top edge, t it leaves the bottom)."""
    def mid_of(tb):
        te = t_at(scroll_at(tb) + SCREEN)
        return (tb + te) / 2
    lo, hi = CLOSE_T - 4, CLOSE_T
    for _ in range(60):
        m = (lo + hi) / 2
        if mid_of(m) < CLOSE_T:
            lo = m
        else:
            hi = m
    tb = (lo + hi) / 2
    return jr(scroll_at(tb) + SCREEN), tb, t_at(scroll_at(tb) + SCREEN)


def lead_span():
    """(bottom, height) of slopes-lead: from the swap line to FEATHER rows above section 3's seam."""
    bottom, _, _ = swap_line()
    top = ground_seams()[2] + FEATHER
    return bottom - 16, top - (bottom - 16)


def deck_centre_t():
    """deck-close's time: the swap line stays nearest the middle of its core while it crosses."""
    line, tb, te = swap_line()
    f = FACTORS["low-air"]
    best = None
    for k in range(0, 4001):
        tc = CLOSE_T - 2 + k * 0.001
        worst = 0.0
        for t in np.linspace(tb - 0.3, te + 0.3, 41):
            centre_screen = MID + (scroll_at(tc) - scroll_at(t)) * f
            line_screen = line - scroll_at(t)
            worst = max(worst, abs(line_screen - centre_screen))
        if best is None or worst < best[0]:
            best = (worst, tc)
    return round(best[1], 3)


def cliff_times():
    """The three escarpment towers: their footprints' centres along section 4, ravines between them."""
    g = ground_seams()
    lo, hi = g[3] + 90, g[4] - 90
    n = 3
    foot = CLIFF_FOOT[1]
    span = n * foot + (n - 1) * CLIFF_GAP
    start = lo + (hi - lo - span) / 2
    return [t_for_centre("ground", start + foot / 2 + k * (foot + CLIFF_GAP)) for k in range(n)]


def placements():
    """(entry, comment) in drawing order per layer (later pieces drawn over earlier ones)."""
    out = []
    # deep
    out.append(({"piece": "olympus", "t": 16.0, "x": 256}, "1. Olympus Mons under the entry corridor (deep x0.15: on screen the whole section)"))
    d = deep_seam()
    out.append(({"piece": "mars-blend", "t": t_for_bottom("deep", d - DEEP_BLEND_H // 2, DEEP_BLEND_H), "x": 240},
                f"2. the deep seam (deep position {d}): mars-high into mars-flank; exact rows, re-run after the data moves section 2's start"))
    # ground: the swap line and the seams
    b, h = lead_span()
    line, tb, te = swap_line()
    g = ground_seams()
    out.append(({"piece": "slopes-lead", "t": t_for_bottom("ground", b, h), "x": 240},
                f"2-3. the swap line (ground {line}) crosses the screen t {tb:.2f}-{te:.2f} under deck-close; the slopes run on into section 3's seam ({g[2]})"))
    out.append(({"piece": "seam-escarpment", "t": t_for_bottom("ground", g[3] - BLEND_H // 2, BLEND_H), "x": 240},
                f"4. section 4's seam (ground {g[3]}): the slopes into the escarpment"))
    out.append(({"piece": "seam-dunes", "t": t_for_bottom("ground", g[4] - BLEND_H // 2, BLEND_H), "x": 240},
                f"5. section 5's seam (ground {g[4]}): the escarpment into the dunes"))
    # ground set pieces under the targets
    x, p = centre_of(TARGETS["terrace"], 32)
    out.append(({"piece": "lava-terrace", "t": t_for_centre("ground", p - 6), "x": 240}, "3. the lava terrace across the flank under the two mortars"))
    x, p = centre_of(TARGETS["outcrop"], 32)
    out.append(({"piece": "basalt-outcrop", "t": t_for_centre("ground", p - 4), "x": jr(x)}, "3. the basalt outcrop under the turret nest"))
    lo, hi = road_span()
    out.append(({"piece": "lycus-road", "t": t_for_centre("ground", (lo + hi) / 2), "x": jr(ROAD_X)},
                f"4. Lycus Survey Station: the road (the Creeper convoy enters at x {ROAD_X:g}), two pairs of pads (two turrets each), the hab (two mortars on its roof)"))
    for pair in pad_pairs():
        x, p = centre_of(pair, 32)
        out.append(({"piece": "lycus-pads", "t": t_for_centre("ground", p), "x": jr(x)}, None))
    x, p = centre_of(TARGETS["hab"], 32)
    out.append(({"piece": "lycus-hab", "t": t_for_centre("ground", p - 8), "x": jr(x)}, None))
    x, p = centre_of(TARGETS["rubble"], 40)
    out.append(({"piece": "drop-pod-ledge", "t": t_for_centre("ground", p - 6), "x": jr(x)}, "4. the ledge at the cliff's foot under the drop pod's rubble (the secret)"))
    x, p = centre_of(TARGETS["lander"], 32)
    out.append(({"piece": "crashed-lander", "t": t_for_centre("ground", p), "x": jr(x)}, "5. the crashed lander on a dune crest under the nest of three"))
    rail_t = next((float(z["t"]) for z in DATA.get("no_surface") or [] if z.get("size", [0])[0] >= W), 187.0)
    out.append(({"piece": "rail-line", "t": round(rail_t, 3), "x": 240}, "5. the colony rail line across the field at the end (the hard turret pair beside it)"))
    for k, ct in enumerate(cliff_times()):
        out.append(({"piece": "escarpment-cliff", "t": ct, "x": CLIFF_X},
                    "4. the Lycus escarpment along the right edge, three stretches with ravines (perspective towers)" if k == 0 else None))
    for k, px in enumerate((84, 252, 420)):
        out.append(({"piece": "rail-pylon", "t": round(rail_t + 0.22, 3), "x": px},
                    "5. the rail line's pylons (perspective)" if k == 0 else None))
    # low-air, high-air
    out.append(({"piece": "deck-close", "t": deck_centre_t(), "x": 240},
                "2. the close-over: its core covers the whole field ~2 s and the swap line all the way across"))
    out.append(({"piece": "transport", "t": TRANSPORT_T, "x": 240}, "1. the launch: the transport passes over in the first 3 s"))
    return out


def piece_sizes():
    out = {}
    for name, (layer, size, extra, mid, note) in PIECES.items():
        if name == "slopes-lead":
            size = (W, lead_span()[1])
        if name == "lycus-road":
            lo, hi = road_span()
            size = (size[0], hi - lo)
        out[name] = size
    return out


def block():
    tile_sets = {}
    for name, (layer, height, drift, _) in TILE_SETS.items():
        spec = {"layer": layer, "height": height}
        if drift:
            spec["drift"] = drift
        tile_sets[name] = spec
    sizes = piece_sizes()
    pieces = {}
    for name, (layer, _, extra, mid, _) in PIECES.items():
        spec = {"layer": layer, "size": list(sizes[name]), **extra}
        if mid:
            spec["mid_size"] = True
        pieces[name] = spec
    return {"scroll_factors": dict(FACTORS), "ramp": RAMP, "haze_colour": HAZE_COLOUR,
            "atmosphere": ATMOSPHERE, "tile_sets": tile_sets, "pieces": pieces,
            "placed": [e for e, _ in placements()]}


flow = k8.flow


def write_proposal():
    b = block()
    line, tb, te = swap_line()
    lines = [
        "# Level 15 backdrop PROPOSAL (tools/art/backdrop_l15.py --proposal; M7 part B batch, round 38, step ART-B).",
        "# For the level-data steps (B2/B3) to merge into data.yaml: `backdrop` is the level's block, every section takes",
        "# the `tiles` below, and the two `peak`s use the atmospheres named here.",
        "#",
        "# sections[i].tiles (by section, in order), atmosphere and peak:",
    ]
    peaks = {0: "peak: {atmosphere: clear, from: 0, to: 8}   # the entry glow: entry-streaks, the deep washed out",
             4: "peak: {atmosphere: heavy, from: 170, to: 180}   # the dust peak: dust-peak banks"}
    for i, (s, tiles) in enumerate(zip(SECTIONS, SECTION_TILES)):
        lines.append(f"#   {s['name']}: tiles: [{', '.join(tiles)}]; atmosphere: {s['atmosphere']}"
                     + (f"; {peaks[i]}" if i in peaks else ""))
    lines += [
        "#",
        "# Built round this timeline (re-run --proposal and the images after the data moves a section end or speed):",
        "#   " + ", ".join(f"{s['name']} to {s['end']:g} at {speed(i):g} px/s" + (f" (ease {ease(i):g} s)" if ease(i) else "")
                          for i, s in enumerate(SECTIONS)) + ".",
        f"# The deck close-over (risk 7): slopes-lead's bottom edge, the swap line at ground {line}, crosses the screen",
        f"#   t {tb:.2f}-{te:.2f}; deck-close's 1,000 px core covers the whole field ~2 s round t {CLOSE_T:g} and the line all the",
        "#   way across (--check); section 2's medium atmosphere ramps to section 3's light over 63-67.",
        "# Exact rows: mars-blend, slopes-lead, seam-escarpment and seam-dunes lie on the seams (t to 4 decimals on",
        "#   purpose; each fades 32 rows into the tile set it continues, so a few px of drift stay invisible).",
        "# The cirrus' and dust-streaks' tile sets are empty round the row where they end/begin (section 3's high-air",
        "#   seam, section 5's low-air seam): re-run after the data moves those section starts.",
        "# NEW KEY (B3 data + B4 game): `transport` is a huge overhead pass on high-air (art direction rule 5): drawn with",
        "#   normal blending at 75 % instead of additive at 40 %, as `overhead_pass: true` on the piece (proposal): only on",
        "#   high-air; BackdropCheck may require it off the screen before the first wave (t 7). Without it the game draws it",
        "#   additive at 40 % and only its lit edges show.",
        "# Ground targets the pieces are built under (re-run after the data moves them): "
        + "; ".join(f"{k} {v}" for k, v in TARGETS.items()) + f"; the road at x {ROAD_X:g}.",
        "# No-surface (B2): besides the pads, the hab and the rail line, the cliff's face: x >= 436 from t 115 to 150.",
        "# Motion budget: drifting deck-banks / dust-thin / dust-peak are the atmosphere's banks (one element), dust-streaks",
        "#   the second in section 5; no animated or moving pieces; the cliff's roof slides 37.5 px/s (k 1.25 at 150).",
        "",
        "backdrop:",
        "  # Presentation only: Olympus Descent (tools/art/backdrop_l15.py, from mars-olympus-r36-a and parallax r02-c).",
        "  # Sections 1-2 have no ground: Mars far below on deep (x0.15), the cloud decks on low-air and high-air; the",
        "  # ground takes over under the opaque deck-close; the escarpment's cliff and the rail pylons are towers.",
        "  scroll_factors: " + flow(b["scroll_factors"]),
        f"  ramp: {RAMP}",
        f"  haze_colour: {HAZE_COLOUR}  # the butterscotch sky (mars_r36.SKY)",
        "  atmosphere:",
    ]
    notes = {"clear": "the entry-glow peak 0-8: the plasma streaks, the surface washed out",
             "light": "sections 1, 3-5: thin dust veils", "medium": "section 2: the water-ice deck in banks ~30 %, opaque cores",
             "heavy": "the dust peak 170-180: rolling dust banks ~30 %, translucent"}
    for key, look in ATMOSPHERE.items():
        lines.append(f"    {key}: {flow(look)}   # {notes[key]}")
    lines.append("  tile_sets:")
    for name, spec in b["tile_sets"].items():
        lines.append(f"    {name}: {flow(spec)}   # {TILE_SETS[name][3]}")
    lines.append("  pieces:")
    for name, spec in b["pieces"].items():
        if name == "transport":
            spec = dict(spec, overhead_pass=True)
        lines.append(f"    {name}: {flow(spec)}   # {PIECES[name][4]}")
    lines.append("  placed:")
    for entry, comment in placements():
        if comment:
            lines.append(f"    # {comment}")
        lines.append(f"    - {flow(entry)}")
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text("\n".join(lines) + "\n", encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def level_backdrop():
    """This script's block (the proposal, rewritten), which the review and the checks draw; the level data's
    merged copy is compared with it (data_differences)."""
    write_proposal()
    return yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))["backdrop"], PROPOSAL.name


def data_differences(backdrop):
    """Where the level data's merged backdrop block (and the sections' tiles) differ from the proposal."""
    own = DATA.get("backdrop") or {}
    out = []
    for key in ("scroll_factors", "ramp", "haze_colour", "atmosphere", "tile_sets", "pieces"):
        mine, theirs = backdrop.get(key), own.get(key)
        if isinstance(mine, dict) and isinstance(theirs, dict):
            out += [f"{key}.{k}" for k in sorted(set(mine) | set(theirs)) if mine.get(k) != theirs.get(k)]
        elif mine != theirs:
            out.append(key)
    mp = [(p["piece"], round(float(p["t"]), 3), float(p["x"])) for p in backdrop["placed"]]
    tp = [(p["piece"], round(float(p["t"]), 3), float(p["x"])) for p in own.get("placed") or []]
    out += [f"placed {e}" for e in mp if e not in tp] + [f"placed (data only) {e}" for e in tp if e not in mp]
    for i, s in enumerate(DATA.get("sections") or []):
        if i < len(SECTION_TILES) and list(s.get("tiles") or []) != SECTION_TILES[i]:
            out.append(f"sections[{i}].tiles")
    return out


# =========================================================================== shared texture helpers

RUST, OCHRE, BASALT, SKY = m36.RUST, m36.OCHRE, m36.BASALT, np.array(m36.SKY, float)
CLOUD_D, DUST, CLOUD = m36.CLOUD_D, m36.DUST, m36.CLOUD
TEAL, LIME = np.array(m36.TEAL, float), np.array(m36.LIME, float)


def pn(w, h, cell, seed, octaves=4, gain=0.5):
    """Periodic fbm 0..1: every octave's lattice (cell >> o) divides w and h, or the tile would not wrap."""
    for o in range(octaves):
        c = max(1, cell >> o)
        assert w % c == 0 and h % c == 0, f"noise cell {c} does not divide {w}x{h}"
    return raster.fbm(w, h, cell, seed, octaves=octaves, gain=gain, period=True)


def sample(field, yy, xx):
    """Bilinear sample with wrap both ways."""
    h, w = field.shape
    y0, x0 = np.floor(yy).astype(int), np.floor(xx).astype(int)
    fy, fx = yy - y0, xx - x0
    y0, x0 = y0 % h, x0 % w
    y1, x1 = (y0 + 1) % h, (x0 + 1) % w
    return ((field[y0, x0] * (1 - fx) + field[y0, x1] * fx) * (1 - fy)
            + (field[y1, x0] * (1 - fx) + field[y1, x1] * fx) * fy)


def aniso(w, h, sx, sy, cell, seed, octaves=4):
    """Periodic noise stretched sx times along x and sy along y (w/sx, h/sy multiples of the cells)."""
    small = pn(w // sx, h // sy, cell, seed, octaves)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    return sample(small, yy / sy, xx / sx)


def pdist(a, b, per):
    d = np.abs(a - b) % per
    return np.minimum(d, per - d)


def smooth(a, b, x):
    return sdf.smoothstep(a, b, x)


def ramp(stops, t):
    return raster.ramp([(i / (len(stops) - 1), c) for i, c in enumerate(stops)], np.clip(t, 0, 1))


def lit(height, albedo, wrap=True, ambient=0.42, key=0.7, ao=0.11):
    """k8.relief at 1x: the key light from the upper left, cast shadows, occlusion. Returns rgb 0..255."""
    a = k8.relief(height, albedo, np.zeros_like(albedo), np.ones(height.shape), 1, wrap=wrap,
                  ambient=ambient, key=key, ao=ao, gradient=0.0)
    return a[..., :3] * 255


def recede(rgb, amount=0.22, towards=None):
    """Rule 3: backgrounds darker and less saturated than the sprites over them."""
    lum = (rgb @ np.array([0.299, 0.587, 0.114]))[..., None]
    out = rgb * (1 - amount * 0.5) + lum * amount * 0.5
    out = out * (1 - amount * 0.45)
    if towards is not None:
        out = out * 0.88 + np.array(towards, float) * 0.12
    return out


def posterize_rgb(rgb, colours, alpha=None):
    img = raster.to_rgba_image(rgb, alpha)
    return k8.quantize_lit([img], colours)[0]


def dither_alpha(a, levels=2):
    """Coverage 0..1 -> stepped alpha by 4x4 Bayer (1-bit when levels=2), as 0..255."""
    return artkit.ordered_dither(np.clip(a, 0, 1), levels) * 255


def crater_field(w, h, n, rmin, rmax, seed, depth=1.0, rim=0.35):
    """Periodic craters: (height, ejecta-brightness) fields; bowls with raised rims."""
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    hgt = np.zeros((h, w))
    ejecta = np.zeros((h, w))
    for _ in range(n):
        cx, cy = rng.uniform(0, w), rng.uniform(0, h)
        r = rmin * (rmax / rmin) ** rng.random() ** 1.8
        x0, x1 = int(cx - 3 * r) - 1, int(cx + 3 * r) + 2
        y0, y1 = int(cy - 3 * r) - 1, int(cy + 3 * r) + 2
        ys = np.arange(y0, y1) % h
        xs = np.arange(x0, x1) % w
        sub_y, sub_x = np.meshgrid(np.arange(y0, y1), np.arange(x0, x1), indexing="ij")
        d = np.hypot(sub_x - cx, sub_y - cy) / r
        bowl = np.where(d < 1, -(1 - d ** 2), 0) * depth * r * 0.45
        rimh = np.exp(-((d - 1) / 0.22) ** 2) * rim * r * 0.45 * depth
        ej = np.clip(1 - (d - 1) / 1.6, 0, 1) * (d > 0.9)
        hgt[np.ix_(ys, xs)] += bowl + rimh
        ejecta[np.ix_(ys, xs)] = np.maximum(ejecta[np.ix_(ys, xs)], ej)
    return hgt, ejecta


def periodic_paths(h, n, seed, x_lo, x_hi, amp, cycles=(1, 2, 3)):
    """n x(y) curves periodic over h: sums of sines with whole cycles."""
    rng = np.random.default_rng(seed)
    y = np.arange(h, dtype=np.float64)
    out = []
    for _ in range(n):
        x = np.full(h, rng.uniform(x_lo, x_hi))
        for c in cycles:
            x += rng.uniform(0.3, 1.0) * amp / c * np.sin(2 * np.pi * c * y / h + rng.uniform(0, 2 * np.pi))
        out.append(x)
    return out


def path_dist(path, yy, xx):
    """Horizontal distance to a path x(y) (good enough for near-vertical curves)."""
    return np.abs(xx - path[yy.astype(int) % len(path)])


# =========================================================================== deep

def mars_high():
    """Section 1's deep tile: the Tharsis plain from very high up, small scale, hazed."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    base = pn(w, h, 160, 1501, 5)
    fine = pn(w, h, 32, 1503, 3)
    # dark lava lobes elongated along the flight (flowing away from Olympus, up the screen)
    lobes = aniso(w, h, 1, 3, 40, 1505, 4)
    warp = pn(w, h, 96, 1507, 3)
    lobes = sample(lobes, yy + 0 * warp, xx + 40 * (warp - 0.5))
    flow = smooth(0.52, 0.58, lobes)
    # graben: thin straight-ish troughs, two families (radial to Tharsis)
    g1 = np.abs(np.sin((xx * 0.9 + yy * 0.5) / 960 * 2 * np.pi * 4 + 1.3)) < 0.012
    g2 = np.abs(np.sin((xx * 0.3 - yy * 1.0) / 960 * 2 * np.pi * 5 + 0.4)) < 0.010
    mask_g = (g1 | g2) & (pn(w, h, 96, 1509, 2) > 0.55)
    craters, ejecta = crater_field(w, h, 70, 1.6, 9.0, 1511, depth=1.0)
    hgt = 3.0 * base + 1.2 * flow + 0.6 * fine + craters * 0.8 - 0.8 * mask_g
    # albedo: rust plains, dark basalt lobes, bright dust, wind streaks (bright tails to the lower right of craters)
    t = np.clip(0.55 + 0.35 * (base - 0.5) + 0.15 * (fine - 0.5) - 0.42 * flow, 0, 1)
    alb = ramp([BASALT[1], BASALT[3], RUST[2], RUST[3], RUST[4]], t)
    streak = np.zeros((h, w))
    for dy, dx in ((k * 1.0, k * 1.8) for k in range(1, 22)):
        streak = np.maximum(streak, np.roll(np.roll(ejecta, int(dy), 0), int(dx), 1) * (1 - dy / 22))
    alb = alb * (1 + 0.16 * streak[..., None]) + 10 * ejecta[..., None]
    rgb = lit(hgt, alb, ambient=0.5, key=0.6)
    rgb = recede(rgb, 0.3) * 0.92
    rgb = rgb * 0.82 + SKY * 0.18                                    # far below: hazed (the game adds its veil)
    return posterize_rgb(rgb, 24)


def olympus():
    """The volcano from the entry corridor: mars_r36.olympus()'s summit (nested pits, radial channels,
    the flank's outer scarp) on its own, its basal scarp ringing it; transparent beyond the scarp."""
    w, h = OLYMPUS
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    cx, cy = w / 2, h / 2
    n = m36.nfield(w, h, 55, 1521, octaves=5)
    n2 = m36.nfield(w, h, 22, 1523, octaves=3)
    r = np.hypot(xx - cx, yy - cy)
    th = np.arctan2(yy - cy, xx - cx)
    rb = 196 * (1 + 0.05 * np.sin(3 * th + 0.7) + 0.06 * (n - 0.5))       # the basal scarp
    hgt = 0.9 - r / 260 + 0.05 * n
    lava = 0.5 + 0.5 * np.sin(th * 52 + n * 9 + np.sin(r / 30))
    hgt += 0.03 * lava ** 6 * np.clip((r - 60) / 80, 0, 1)
    pits = np.zeros_like(hgt)
    for px, py, pr, dep in ((cx + 6, cy - 4, 46, 0.12), (cx - 24, cy + 20, 28, 0.09), (cx + 26, cy - 30, 21, 0.07),
                            (cx - 4, cy - 28, 16, 0.05)):
        d = np.hypot(xx - px, yy - py) / (pr * (1 + 0.14 * (n2 - 0.5)))
        pit = sdf.smoothstep(1.0, 0.8, d)
        hgt -= dep * pit
        pits = np.maximum(pits, pit)
    hgt -= 0.03 * sdf.smoothstep(118, 130, r + 30 * n2)                  # terraced upper flank
    hgt -= 0.16 * sdf.smoothstep(rb - 6, rb + 2, r)                      # the basal cliff
    gy, gx = np.gradient(hgt)
    sh = np.clip(1 - (gx + gy) * 120, 0.35, 1.7)
    tt = np.clip(0.35 + 0.5 * n + 0.15 * (1 - r / 220), 0, 1)
    flank = [(0, BASALT[1]), (0.45, BASALT[2]), (0.7, (128, 66, 40)), (1, (170, 104, 66))]
    col = raster.ramp(flank, tt) * sh[..., None] * (0.55 + 0.45 * m36.cast_np(hgt, 30, 0.0016))[..., None]
    col *= (1 - 0.3 * pits)[..., None]
    col = recede(col, 0.25) * 0.95
    col = col * 0.84 + SKY * 0.16
    # beyond the scarp the plain: fade out over an aureole, dithered (the deep edge rule)
    cover = np.clip(1 - (r - rb - 4) / 30, 0, 1) * np.clip((min(w, h) / 2 - 4 - r) / 10, 0, 1)
    alpha = dither_alpha(cover, 5)
    img = posterize_rgb(col, 24, alpha)
    a = np.array(img)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


def mars_flank():
    """Sections 2-5's deep tile: the flank closer: leveed channels and lobate flows along the flight."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    base = pn(w, h, 160, 1531, 4)
    fine = pn(w, h, 16, 1533, 3)
    flows = aniso(w, h, 1, 4, 40, 1535, 4)
    warp = pn(w, h, 48, 1537, 3)
    flows = sample(flows, yy, xx + 30 * (warp - 0.5))
    lobe = smooth(0.45, 0.62, flows)
    hgt = 6 * base + 5 * lobe + 0.8 * fine
    dark = np.zeros((h, w))
    for k, path in enumerate(periodic_paths(h, 7, 1539, 20, 460, 46)):
        d = path_dist(path, yy, xx)
        hgt += 2.2 * np.exp(-((d - 5) / 2.0) ** 2) - 2.6 * np.exp(-(d / 3.2) ** 2)    # levees, channel floor
        dark = np.maximum(dark, np.exp(-(d / 3.0) ** 2))
    craters, ejecta = crater_field(w, h, 26, 2.0, 7.0, 1541, depth=1.2)
    hgt += craters
    t = np.clip(0.5 + 0.3 * (base - 0.5) + 0.2 * (fine - 0.5) - 0.25 * lobe - 0.3 * dark, 0, 1)
    alb = ramp([BASALT[0], BASALT[2], BASALT[3], (128, 66, 40), (164, 100, 64)], t)
    dust = smooth(0.55, 0.75, pn(w, h, 96, 1543, 3)) * (1 - lobe)
    alb = alb * (1 - dust[..., None] * 0.35) + np.array(RUST[3], float) * dust[..., None] * 0.35
    rgb = lit(hgt, alb, ambient=0.48, key=0.66)
    rgb = recede(rgb, 0.3) * 0.94
    rgb = rgb * 0.86 + SKY * 0.14
    return posterize_rgb(rgb, 28)


# =========================================================================== ground tile sets

def regolith(w, h, seed):
    """Dusty rust regolith: albedo field and height (gentle swells, pebbles)."""
    n = pn(w, h, 160, seed, 4)
    f = pn(w, h, 16, seed + 2, 3)
    peb = np.random.default_rng(seed).random((h, w)) > 0.985
    hgt = 4 * n + 0.8 * f + 1.2 * peb
    t = np.clip(0.55 + 0.35 * (n - 0.5) + 0.25 * (f - 0.5) - 0.2 * peb, 0, 1)
    alb = ramp([(92, 46, 28), (126, 66, 40), (152, 88, 56), (176, 112, 74), (196, 136, 94)], t)
    return hgt, alb


def slopes():
    """Section 3's ground: the volcano's lower flank."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rh, ralb = regolith(w, h, 1551)
    # stepped lava terraces with lobate fronts facing up the screen (downhill is ahead)
    tn = aniso(w, h, 2, 1, 80, 1553, 4)
    warp = pn(w, h, 48, 1555, 3)
    lev = (2.2 * tn + 0.6 * (warp - 0.5)) * 2.2
    step = np.floor(lev) + smooth(0.0, 0.35, lev % 1)
    rock = smooth(0.5, 0.56, pn(w, h, 160, 1557, 4) * 0.7 + 0.3 * tn)
    terr = step * 4.0 * rock
    knob_n = pn(w, h, 32, 1559, 3)
    knobs = np.clip((knob_n - 0.62) * 22, 0, 6) * rock
    vesic = pn(w, h, 16, 1561, 2)
    hgt = rh * (1 - rock) + (terr + knobs + 1.5 * vesic) * rock + 2.5 * rock
    # burrow furrows in the regolith fields: sinuous shallow troughs with tiny rims
    furrow = np.zeros((h, w))
    for path in periodic_paths(h, 5, 1563, 40, 440, 70, cycles=(1, 3, 5)):
        d = path_dist(path, yy, xx)
        furrow += (-1.2 * np.exp(-(d / 2.2) ** 2) + 0.6 * np.exp(-((d - 3.6) / 1.3) ** 2))
    furrow *= (1 - rock) * (pn(w, h, 96, 1565, 2) > 0.42)
    craters, ej = crater_field(w, h, 18, 3.0, 11.0, 1567, depth=1.0)
    hgt = hgt + furrow + craters * (1 - rock * 0.6)
    t = np.clip(0.5 + 0.35 * (vesic - 0.5) + 0.08 * knobs, 0, 1)
    balb = ramp([BASALT[0], BASALT[1], BASALT[2], BASALT[3], (124, 82, 62)], t)
    alb = ralb * (1 - rock[..., None]) + balb * rock[..., None]
    alb += np.array((30, 14, 6)) * (furrow < -0.3)[..., None] * -0.6
    alb += 14 * ej[..., None] * (1 - rock[..., None])
    rgb = lit(hgt, alb)
    return posterize_rgb(recede(rgb, 0.22), 32)


def escarpment():
    """Section 4's ground: regolith at the cliff's foot, scree fans and boulders toward the right."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rh, ralb = regolith(w, h, 1571)
    nearc = np.clip((xx - 250) / 190, 0, 1) ** 1.5                        # toward the cliff (x ~ 440)
    fan_n = pn(w, h, 48, 1573, 3)
    fans = np.zeros((h, w))
    rng = np.random.default_rng(1575)
    for k in range(7):                                                    # scree fans spreading from the cliff's gullies
        fy = (k + rng.uniform(0.1, 0.9)) * h / 7
        dy = pdist(yy, fy, h)
        fans = np.maximum(fans, np.clip(1 - np.hypot((xx - 452) / 150, dy / (60 + 0.5 * (452 - xx))) - 0.25 * fan_n, 0, 1))
    fans *= smooth(0.0, 0.3, fans)
    bould = np.zeros((h, w))
    for _ in range(260):                                               # faceted boulders, denser toward the cliff
        bx = 456 - abs(rng.normal(0, 60))
        by = rng.uniform(0, h)
        br = rng.uniform(1.6, 4.6) * (1 + 0.6 * (bx > 390))
        rot = rng.uniform(0, np.pi / 2)
        r = int(br * 2) + 2
        ys = np.arange(int(by) - r, int(by) + r + 1)
        sy, sx = np.meshgrid(ys, np.arange(int(bx) - r, int(bx) + r + 1), indexing="ij")
        ux, uy = (sx - bx) * np.cos(rot) + (sy - by) * np.sin(rot), -(sx - bx) * np.sin(rot) + (sy - by) * np.cos(rot)
        dd = (0.55 * np.maximum(np.abs(ux), np.abs(uy) * 1.2) + 0.45 * np.hypot(ux, uy)) / br
        bump = np.clip(1.15 - dd, 0, 1) ** 0.35 * br * 1.1
        ok = (sx >= 0) & (sx < w)
        sxc = np.clip(sx, 0, w - 1)
        bould[sy % h, sxc] = np.maximum(bould[sy % h, sxc], bump * ok)
    old_flow = smooth(0.6, 0.66, aniso(w, h, 1, 3, 40, 1577, 3)) * (1 - nearc)
    hgt = rh + 3 * fans + bould + 2.2 * old_flow
    t = np.clip(0.45 + 0.4 * (fan_n - 0.5), 0, 1)
    scree = ramp([(96, 56, 40), (128, 80, 56), (158, 108, 78), (182, 136, 102)], t)
    alb = ralb * (1 - fans[..., None]) + scree * fans[..., None]
    alb = alb * (1 - old_flow[..., None] * 0.22) + np.array(BASALT[2], float) * old_flow[..., None] * 0.22
    alb = np.where((bould > 0.4)[..., None], ramp([BASALT[1], BASALT[3], (132, 92, 70)], np.clip(bould / 6, 0, 1)), alb)
    # old tyre tracks of the station's rovers, faint
    for path in periodic_paths(h, 2, 1579, 120, 300, 30, cycles=(1, 2)):
        d = path_dist(path, yy, xx)
        alb *= 1 - 0.12 * ((np.abs(d - 3) < 0.9) | (np.abs(d + 3) < 0.9))[..., None] * (pn(w, h, 48, 1581, 2) > 0.45)[..., None]
    rgb = lit(hgt, alb)
    return posterize_rgb(recede(rgb, 0.22), 32)


DUNE_LAMBDA = 120.0


def dune_field(w, h, seed, yy, xx):
    """Height of transverse dunes across a wind from the left: a gentle stoss slope, the crest, a steep slip
    face to the right; crests sinuous (whole cycles over h)."""
    crest_shift = np.zeros(h)
    rng = np.random.default_rng(seed)
    y = np.arange(h, dtype=np.float64)
    for c, a in ((1, 26), (2, 18), (3, 10), (5, 6)):
        crest_shift += a * np.sin(2 * np.pi * c * y / h + rng.uniform(0, 2 * np.pi))
    shift = crest_shift[yy.astype(int) % h]
    pha = (xx - shift) / DUNE_LAMBDA + 1.1 * (pn(w, h, 160, seed + 3, 3) - 0.5) + 0.35 * (pn(w, h, 48, seed + 5, 2) - 0.5)
    u = pha % 1.0                                                   # 0 trough .. crest at 0.78 .. slip face
    stoss = np.clip(u / 0.78, 0, 1) ** 1.3
    slip = np.clip(1 - (u - 0.78) / 0.22, 0, 1) ** 0.8
    prof = np.where(u < 0.78, stoss, slip)
    amp = 0.45 + 0.55 * smooth(0.25, 0.7, pn(w, h, 160, seed + 1, 3))
    return prof * amp, u


def dunes():
    """Section 5's ground: the Tharsis dune sea."""
    h, w = TILE_H, W
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    prof, u = dune_field(w, h, 1591, yy, xx)
    rip = np.sin((xx + 4 * np.sin(yy * 2 * np.pi * 42 / h)) / 3.1) * 0.22 * (u < 0.74)
    n = pn(w, h, 48, 1593, 3)
    hgt = 16 * prof + rip + 1.5 * n
    trough = np.clip(1 - prof / 0.25, 0, 1)
    t = np.clip(0.58 + 0.25 * (n - 0.5) + 0.25 * prof - 0.45 * trough, 0, 1)
    alb = ramp([(70, 44, 36), (110, 66, 44), (150, 96, 60), (182, 128, 84), (204, 156, 108)], t)
    rgb = lit(hgt, alb, ambient=0.44, key=0.72)
    return posterize_rgb(recede(rgb, 0.2), 32)


# =========================================================================== ground pieces

def tile_strip(name, bottom, h):
    """A tile set's rows for layer positions [bottom, bottom + h) as an image array (top row = highest)."""
    img = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    hgt = img.shape[0]
    pos = bottom + h - 1 - np.arange(h)
    return img[[hgt - 1 - (int(p) % hgt) for p in pos]]


def blend_piece(lower, upper, bottom, h, boundary, seed, shadow=0.0, rim=None):
    """A piece over a seam: the rows of ``lower`` below a ragged boundary (layer position ``boundary`` +
    noise), of ``upper`` above it, faded FEATHER rows into the tile sets at its two ends (ordered-dithered
    alpha: the same rows as the tiles under them). ``lower`` None: transparent below the boundary."""
    up = tile_strip(upper, bottom, h)
    lo = tile_strip(lower, bottom, h) if lower else np.zeros_like(up)
    rows = bottom + h - 1 - np.arange(h)[:, None]
    xs = np.arange(W)[None, :]
    rng = np.random.default_rng(seed)
    wob = sum(a * np.sin(2 * np.pi * c * xs / W + rng.uniform(0, 6.3)) for c, a in ((1, 14), (2, 9), (5, 5), (11, 3)))
    fine = (np.random.default_rng(seed + 1).random((1, W)) - 0.5) * 3
    edge = boundary + wob + fine
    above = rows >= edge
    out = np.where(above[..., None], up, lo)
    if shadow:
        d = edge - rows                                      # px below the boundary
        s = np.clip(1 - d / 7, 0, 1) * (d >= 0) * shadow
        out[..., :3] *= (1 - s)[..., None]
    if rim is not None:
        d = rows - edge
        r = (d >= 0) & (d < 2)
        out[..., :3] = np.where(r[..., None], out[..., :3] * 0.55 + np.array(rim) * 0.45, out[..., :3])
    a = np.ones((h, W))
    k = np.arange(h)[:, None]
    a = np.minimum(a, np.clip((k + 0.5) / FEATHER, 0, 1))                 # the top rows fade into `upper`
    if lower:
        a = np.minimum(a, np.clip((h - k - 0.5) / FEATHER, 0, 1))         # the bottom rows into `lower`
    else:
        a = np.where(above, a, 0.0)
        a = np.minimum(a, np.clip((h - k - 0.5) / 16, 0, 1))
    alpha = dither_alpha(np.broadcast_to(a, (h, W)), 2)
    out[..., 3] = np.where(alpha > 0, 255, 0)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def mars_blend():
    d = deep_seam()
    return blend_piece("mars-high", "mars-flank", d - DEEP_BLEND_H // 2, DEEP_BLEND_H, d, 1601, shadow=0.35)


def slopes_lead():
    b, h = lead_span()
    return blend_piece(None, "slopes", b, h, b + 16, 1603)


def seam_escarpment():
    g = ground_seams()[3]
    return blend_piece("slopes", "escarpment", g - BLEND_H // 2, BLEND_H, g - 10, 1605, shadow=0.3, rim=(120, 84, 66))


def seam_dunes():
    g = ground_seams()[4]
    return blend_piece("escarpment", "dunes", g - BLEND_H // 2, BLEND_H, g, 1607, shadow=0.0)


def piece_canvas(size, f=2):
    w, h = size
    yy, xx = np.mgrid[0:h * f, 0:w * f].astype(np.float64)
    return yy / f, xx / f


def finish_piece(hgt, alb, cover, f=2, colours=32, emission=None, wrap=False, ambient=0.42, key=0.7, full_width=False):
    """Light a piece's height field at f x, reduce, dither the cover's fringe to 1-bit alpha."""
    a = k8.relief(hgt, alb, np.zeros_like(alb) if emission is None else emission, np.ones(hgt.shape), f,
                  wrap=wrap, ambient=ambient, key=key, ao=0.11, gradient=0.06)
    c = np.clip(cover, 0, 1)
    c[:f], c[-f:] = 0, 0
    if not full_width:
        c[:, :f], c[:, -f:] = 0, 0
    a[..., 3] = c
    nat = k8.reduce(a, f)
    nat[..., 3] = np.clip((nat[..., 3] - 0.5) * 2.5 + 0.5, 0, 1)            # tight edges: a narrow dithered rim
    rgb = recede(nat[..., :3] * 255, 0.18)
    alpha = dither_alpha(nat[..., 3], 2)
    img = posterize_rgb(rgb, colours, alpha)
    arr = np.array(img)
    arr[arr[..., 3] == 0] = 0
    return Image.fromarray(arr, "RGBA")


def soft_blob(yy, xx, cx, cy, rx, ry, n=None, rough=0.0):
    d = np.hypot((xx - cx) / rx, (yy - cy) / ry)
    if n is not None:
        d = d * (1 + rough * (n - 0.5))
    return d


def piece_spots(name, role, size, placed_x=None):
    """The role's target spots in the piece's coordinates (the piece centred under them, or at placed_x)."""
    w, h = piece_sizes()[name]
    spots = TARGETS[role]
    cx, cpos = centre_of(spots, size)
    return spots_in(spots, size, w, h, cx if placed_x is None else placed_x, cpos)


def platforms(yy, xx, spots, r, n=None, rough=0.0):
    """1 on discs of radius r round the spots (flat ground for the targets), soft beyond."""
    d = np.full(yy.shape, np.inf)
    for sx, sy in spots:
        d = np.minimum(d, np.hypot(xx - sx, yy - sy))
    if n is not None:
        d = d + rough * (n - 0.5) * r
    return d


def lava_terrace():
    """A lava flow's terrace across the flank: its flat top carries the mortars, its lobate front faces up the
    screen (downhill), a dark basalt skirt; fades into the slopes at both ends of the band."""
    w, h = PIECES["lava-terrace"][1]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    spots = piece_spots("lava-terrace", "terrace", 32, 240)
    n = raster.fbm(w * f, h * f, 32, 1611, octaves=4, period=False)
    n2 = raster.fbm(w * f, h * f, 8, 1613, octaves=3, period=False)
    # the terrace band follows a line through the mortars (lower edge below them, the front above)
    (x1, y1), (x2, y2) = spots[0], spots[-1]
    slope = (y2 - y1) / (x2 - x1) if x2 != x1 else 0.0
    mid_y = y1 + slope * (xx - x1)
    front = mid_y - 30 - 14 * np.sin(xx / 37 + 1.3) - 9 * np.sin(xx / 13) - 18 * (n - 0.5)      # lobes, uphill side
    back = mid_y + 34 + 10 * np.sin(xx / 29) + 14 * (n - 0.5)
    inside = np.clip(np.minimum(yy - front, back - yy) / 5, 0, 1)
    hgt = 9 * inside ** 0.7 + 0.8 * n2 + 0.6 * np.clip((front + 12 - yy) / 12, 0, 1) * (yy < front)
    t = np.clip(0.4 + 0.45 * (n2 - 0.5) + 0.1 * inside, 0, 1)
    alb = ramp([BASALT[0], BASALT[1], BASALT[2], BASALT[3]], t)
    skirt = np.clip(1 - np.minimum(np.abs(yy - front), np.abs(yy - back)) / 22, 0, 1) * (inside < 1)
    dust = smooth(0.35, 0.65, n) * (1 - inside)
    alb = alb * (1 - dust[..., None] * 0.55) + np.array((150, 88, 56)) * dust[..., None] * 0.55
    ends = np.clip(np.minimum(xx, w - xx) / 3, 0, 1)
    cover = np.clip(inside + skirt * (0.4 + 0.6 * n), 0, 1) * ends
    cover *= np.clip(np.minimum(yy, h - yy) / 4, 0, 1)
    return finish_piece(hgt, alb, cover, f, full_width=True)


def basalt_outcrop():
    """A basalt outcrop: flat ledges under the three turrets, knobs and columns round them."""
    w, h = PIECES["basalt-outcrop"][1]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    spots = piece_spots("basalt-outcrop", "outcrop", 32)
    n = raster.fbm(w * f, h * f, 24, 1621, octaves=4, period=False)
    d0 = platforms(yy, xx, spots, 26, n, 0.5)
    plat = smooth(26, 20, d0)
    hgt = 8 * plat
    rng = np.random.default_rng(1623)
    cx, cy = np.mean([s[0] for s in spots]), np.mean([s[1] for s in spots])
    for _ in range(30):                                                    # knobs round the ledges, never on a spot
        a = rng.uniform(0, 2 * np.pi)
        rr = rng.uniform(40, 86)
        kx, ky = cx + np.cos(a) * rr * 1.1, cy + np.sin(a) * rr * 0.8
        if min(np.hypot(kx - sx, ky - sy) for sx, sy in spots) < 30:
            continue
        r = rng.uniform(5, 12)
        dk = np.hypot(xx - kx, yy - ky) / r
        hgt = np.maximum(hgt, np.clip(1 - dk ** 2, 0, 1) ** 0.5 * rng.uniform(7, 13))
    hgt += 0.9 * n
    t = np.clip(0.35 + 0.5 * (n - 0.5) + hgt / 40, 0, 1)
    alb = ramp([BASALT[0], BASALT[1], BASALT[2], (104, 70, 56)], t)
    cover = np.clip(smooth(40, 28, d0) + (hgt > 0.8), 0, 1)
    return finish_piece(hgt, alb, cover, f)


def growth(yy, xx, seed, density, centres=None):
    """Vrell growth: lumpy slate-violet tissue with teal veins; (height, coverage, vein) fields."""
    n = raster.fbm(xx.shape[1], xx.shape[0], 16, seed, octaves=3, period=False)
    g = smooth(1 - density, 1 - density + 0.12, n)
    if centres is not None:
        g = g * centres
    vein = (np.abs(np.sin(xx * 0.55 + np.sin(yy * 0.31) * 2.4 + n * 6)) < 0.13) & (g > 0.4)
    return 2.4 * g + 0.8 * g * n, g, vein


GROWTH_ALB = np.array((64, 50, 70), float)
VEIN_EMIT = TEAL / 255 * 0.55


def clear_of(yy, xx, spots, r):
    return 1 - np.clip(sum(np.exp(-(np.hypot(xx - sx, yy - sy) / r) ** 2) for sx, sy in spots), 0, 1)


def lycus_pads():
    """Two of the station's landing pads (a pair of turrets on them): scorched concrete with faded beige
    markings, a taxi strip, Vrell growth creeping over the edges; placed under each pair."""
    w, h = PIECES["lycus-pads"][1]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    pair = pad_pairs()[0]
    cx, cpos = centre_of(pair, 32)
    spots = spots_in(pair, 32, w, h, cx, cpos)
    n = raster.fbm(w * f, h * f, 24, 1631, octaves=3, period=False)
    alb = np.zeros(yy.shape + (3,)) + np.array((150, 92, 62))
    concrete = np.zeros(yy.shape)
    marks = np.zeros(yy.shape, bool)
    scorch = np.zeros(yy.shape)
    cover = np.zeros(yy.shape)
    for k, (px, py) in enumerate(spots):
        d = np.hypot(xx - px, yy - py)
        concrete = np.maximum(concrete, d < 34)
        marks |= ((np.abs(d - 29) < 1.1) | ((np.abs(xx - px) < 1.3) & (d < 15)) | ((np.abs(yy - py) < 1.3) & (d < 15))) & (d < 34)
        scorch = np.maximum(scorch, np.clip(1 - d / 24, 0, 1) * (0.5 + 0.5 * n))
        cover = np.maximum(cover, smooth(48, 36, d))
    (ax, ay), (bx, by) = spots[0], spots[-1]
    seg = np.clip(((xx - ax) * (bx - ax) + (yy - ay) * (by - ay)) / ((bx - ax) ** 2 + (by - ay) ** 2), 0, 1)
    ds = np.hypot(xx - (ax + seg * (bx - ax)), yy - (ay + seg * (by - ay)))
    strip = ds < 9
    concrete = np.maximum(concrete, strip)
    hgt = 1.6 * concrete + 0.3 * n
    calb = np.array((132, 128, 120)) * (0.88 + 0.2 * n[..., None])
    alb = np.where(concrete[..., None] > 0, calb, alb)
    alb = np.where(marks[..., None], np.array((150, 138, 104)), alb)        # faded beige markings (not loot amber)
    alb *= (1 - 0.5 * scorch * concrete)[..., None]
    gh, gm, vein = growth(yy, xx, 1633, 0.45)
    gm = gm * clear_of(yy, xx, spots, 14) * smooth(60, 40, platforms(yy, xx, spots, 1))
    hgt += gh * gm
    alb = alb * (1 - gm[..., None]) + GROWTH_ALB * gm[..., None]
    emis = np.where((vein & (gm > 0.4))[..., None], VEIN_EMIT * 255, 0.0)
    cover = np.clip(np.maximum.reduce([cover, strip * 1.0, smooth(0.2, 0.5, gm)]), 0, 1)
    cover *= np.clip(np.minimum.reduce([xx, w - xx, yy, h - yy]) / 4, 0, 1)
    return finish_piece(hgt, alb, cover, f, emission=emis, colours=36)


def lycus_hab():
    """The station's domed hab drawn flat (a low dome: no lean): ribbed shell, an airlock tunnel east to the
    road, a solar panel, Vrell growth over it; two mortars on its roof."""
    w, h = PIECES["lycus-hab"][1]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    spots = piece_spots("lycus-hab", "hab", 32)
    cx, cy = np.mean([s[0] for s in spots]), np.mean([s[1] for s in spots]) - 4
    R = 54
    d = np.hypot(xx - cx, yy - cy)
    th = np.arctan2(yy - cy, xx - cx)
    dome = np.sqrt(np.clip(1 - (d / R) ** 2, 0, 1))
    hgt = 15 * dome ** 0.5 * (d < R)
    ribs = (np.abs(np.sin(th * 8)) < 0.08) & (d < R) & (d > 8)
    rings = (np.abs(d - R * 0.64) < 0.8) | (np.abs(d - R * 0.3) < 0.8)
    hgt += 0.6 * (ribs | rings)
    side = 1 if ROAD_X >= cx else -1
    lock = (np.abs(yy - cy - 6) < 7) & (side * (xx - cx) > R - 6) & (side * (xx - cx) < R + 18)
    hgt = np.maximum(hgt, 6 * lock)
    panel = (-side * (xx - cx) > R - 4) & (-side * (xx - cx) < R + 16) & (np.abs(yy - cy + 22) < 13)
    hgt = np.maximum(hgt, 3 * panel)
    alb = np.zeros(yy.shape + (3,)) + np.array((150, 146, 138))
    alb = np.where((ribs | rings)[..., None], np.array((112, 110, 108)), alb)
    alb = np.where(panel[..., None], np.array((40, 52, 74)), alb)
    n = raster.fbm(w * f, h * f, 16, 1641, octaves=3, period=False)
    alb *= (0.85 + 0.25 * n)[..., None]
    gh, gm, vein = growth(yy, xx, 1643, 0.5)
    gm = gm * smooth(R + 18, R - 12, d) * clear_of(yy, xx, spots, 12)
    hgt += gh * gm
    alb = alb * (1 - gm[..., None]) + GROWTH_ALB * gm[..., None]
    emis = np.where((vein & (gm > 0.4))[..., None], VEIN_EMIT * 255, 0.0)
    cover = np.clip((d < R + 2) + lock + panel + smooth(0.2, 0.5, gm) * (d < R + 20), 0, 1)
    cover *= np.clip(np.minimum.reduce([xx, w - xx, yy, h - yy]) / 3, 0, 1)
    return finish_piece(hgt, alb, cover, f, emission=emis, colours=36)


def lycus_road():
    """The station's graded road straight up the convoy's entry line to the hab: compacted regolith, tyre ruts,
    berms, Vrell growths along the shoulders; it fades out at the bottom."""
    w, h = piece_sizes()["lycus-road"]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    y = yy[:, :1]
    cxl = w / 2 + 3 * np.sin(y / 61) + 2 * np.sin(y / 23)
    dx = xx - cxl
    n = raster.fbm(w * f, h * f, 24, 1651, octaves=3, period=False)
    road = np.abs(dx) < 18
    ruts = ((np.abs(np.abs(dx) - 8) < 1.3)) & road
    berm = np.exp(-((np.abs(dx) - 21) / 2.4) ** 2)
    hgt = 0.4 * n + 1.5 * berm - 0.5 * ruts
    alb = np.zeros(yy.shape + (3,)) + np.array((166, 110, 76))
    alb = np.where(road[..., None], np.array((178, 128, 92)) * (0.92 + 0.12 * n[..., None]), alb)
    alb = np.where(ruts[..., None], np.array((130, 84, 58)), alb)
    side = np.clip(1 - (np.abs(dx) - 20) / 22, 0, 1) * (np.abs(dx) > 18)
    gh, gm, vein = growth(yy, xx, 1653, 0.55, centres=side)
    hgt += gh * gm
    alb = alb * (1 - gm[..., None]) + GROWTH_ALB * gm[..., None]
    emis = np.where((vein & (gm > 0.4))[..., None], VEIN_EMIT * 255, 0.0)
    bottom_fade = np.clip((h - yy) / 120, 0, 1)
    top_fade = np.clip(yy / 6, 0, 1)
    cover = np.where(np.abs(dx) < 23, 1.0, smooth(0.25, 0.5, gm))
    cover = cover * np.clip(bottom_fade * (0.75 + 0.5 * n), 0, 1) * top_fade
    return finish_piece(hgt, alb, cover, f, emission=emis, colours=32)


def drop_pod_ledge():
    """The ledge at the cliff's foot the rubble lies on: a raised rock shelf, the pod's impact furrow."""
    w, h = PIECES["drop-pod-ledge"][1]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    n = raster.fbm(w * f, h * f, 24, 1661, octaves=4, period=False)
    d = soft_blob(yy, xx, w / 2, h / 2 + 2, w / 2 - 16, h / 2 - 18, n, 0.5)
    shelf = smooth(1.0, 0.9, d)
    furrow = np.exp(-((xx - w / 2 + 0.55 * (yy - h / 2)) / 6) ** 2) * (yy < h / 2) * shelf
    hgt = 7 * shelf - 1.5 * furrow + 0.8 * n
    t = np.clip(0.45 + 0.4 * (n - 0.5) + 0.1 * shelf, 0, 1)
    alb = ramp([(112, 76, 56), (146, 104, 76), (172, 128, 94), (190, 148, 110)], t)   # a dusty shelf: the dark rubble reads on it
    alb *= (1 - 0.35 * furrow)[..., None]
    cover = smooth(1.35, 1.12, d)
    return finish_piece(hgt, alb, cover, f)


def rail_line():
    """The colony rail line across the field: its embankment, sleepers, two rails, a cable trough."""
    w, h = PIECES["rail-line"][1]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    dy = yy - h / 2
    n = raster.fbm(w * f, h * f, 16, 1671, octaves=3, period=False)
    emb = np.clip(1 - (np.abs(dy) - 16) / 22, 0, 1)
    hgt = 3.0 * emb + 0.4 * n
    sleepers = (np.abs(dy) < 11) & ((xx % 9) < 3.2)
    rails = (np.abs(np.abs(dy) - 6) < 1.0)
    trough = (np.abs(dy + 19) < 2.0)
    hgt += 0.9 * sleepers + 1.4 * rails + 0.8 * trough
    alb = np.zeros(yy.shape + (3,)) + np.array((150, 104, 74))
    alb = np.where((np.abs(dy) < 14)[..., None], np.array((112, 100, 92)) * (0.9 + 0.2 * n[..., None]), alb)
    alb = np.where(sleepers[..., None], np.array((86, 70, 60)), alb)
    alb = np.where(rails[..., None], np.array((150, 150, 156)), alb)
    alb = np.where(trough[..., None], np.array((120, 116, 110)), alb)
    cover = np.where(np.abs(dy) < 18, 1.0, np.clip(emb * 1.3 + (n - 0.5) * 0.3, 0, 1))
    cover *= np.clip(np.minimum(yy, h - yy) / 3, 0, 1)
    return finish_piece(hgt, alb, cover, f, full_width=True)


# ---- the crashed lander (SDF on its sand drift)

def lander_mats():
    return [Material((0.62, 0.60, 0.56), metal=0.35, shininess=30, spec=0.35, pattern=sdf.panel_lines(0.16, 0.05, 0.8)),
            Material((0.20, 0.30, 0.40), metal=0.4, shininess=40, spec=0.4),
            Material((0.18, 0.17, 0.18), metal=0.5, shininess=30, spec=0.3),
            Material((0.60, 0.42, 0.28), metal=0.0, shininess=6, spec=0.02),
            Material((0.12, 0.10, 0.10), metal=0.2, shininess=10, spec=0.1)]


def crashed_lander():
    """A colony lander crashed on a dune crest: its gouge in the sand behind it (relief), the squat hull tilted
    with a leg folded under, its flat top deck under the middle turret, hull plates strewn downwind."""
    w, h = PIECES["crashed-lander"][1]
    spots = piece_spots("crashed-lander", "lander", 32)
    c = spots[len(spots) // 2]
    f = 2
    yy, xx = piece_canvas((w, h), f)
    n = raster.fbm(w * f, h * f, 24, 1701, octaves=3, period=False)
    # the gouge from the lower left up to the hull, sand piled at its sides
    ax, ay = c[0] - 70, c[1] + 62
    t_ = np.clip(((xx - ax) * (c[0] - ax) + (yy - ay) * (c[1] - ay)) / ((c[0] - ax) ** 2 + (c[1] - ay) ** 2), 0, 1)
    dd = np.hypot(xx - (ax + t_ * (c[0] - ax)), yy - (ay + t_ * (c[1] - ay)))
    trench = np.exp(-(dd / 9) ** 2) * (0.3 + 0.7 * t_)
    berm = np.exp(-((dd - 13) / 5) ** 2) * (0.3 + 0.7 * t_)
    hgt = -3 * trench + 2.5 * berm + 0.6 * n
    alb = ramp([(118, 74, 48), (160, 108, 70), (190, 140, 96)], np.clip(0.55 + 0.4 * (n - 0.5) - 0.35 * trench, 0, 1))
    alb *= (1 - 0.3 * trench)[..., None]
    cover = np.clip(trench * 2.2 + berm * 1.6, 0, 1) * (0.7 + 0.6 * n)
    base = np.array(finish_piece(hgt, alb, cover, f, colours=16)).astype(np.float64)
    m = (c[0] - w / 2, h / 2 - c[1])
    tilt = np.radians(-10)

    def scene(p0_):
        p = p0_ - np.array([m[0], m[1], 0.0])
        q = sdf.rotate_y(sdf.rotate_z(p, 0.35), tilt)
        body = sd_cylinder_z(q, (0, 0, 9), 40, 9)
        top = sd_ellipsoid(q, (0, 0, 17), (32, 32, 7))
        deck = sd_box(q, (0, 0, 22), (15, 15, 1.2), 1.0)
        ring = sd_box(q, (0, 0, 9), (44, 44, 2), 2)
        legs = []
        for k, a in enumerate((0.6, 2.2, 3.8, 5.3)):
            lz = 1.0 if k != 2 else -6.0
            legs.append(sd_capsule(q, (np.cos(a) * 32, np.sin(a) * 32, 8), (np.cos(a) * 52, np.sin(a) * 52, lz), 2.6))
        nozz = sd_cylinder_z(q, (0, -10, 1), 10, 3)
        plates = [sd_box(sdf.rotate_z(p - np.array([x, y, 1.5]), r), (0, 0, 0), (bw, bh, 1.2), 0.5) for x, y, bw, bh, r in
                  ((62, 40, 9, 5, 0.4), (78, 8, 5, 8, 1.1), (56, -60, 7, 4, 2.0), (-70, 54, 6, 4, 0.8), (90, -30, 4, 3, 0.2))]
        d, mm = union((body, 0), (top, 0), (deck, 2), (ring, 3), *[(l, 2) for l in legs], (nozz, 2),
                      *[(pl, 0) for pl in plates], k=0.6)
        band = (np.abs(q[:, 2] - 13) < 1.6) & (mm == 0)
        mm = np.where(band, 1, mm)
        return d, mm
    hi = sdf.render(scene, lander_mats(), (w * 2, h * 2), float(w), z_top=60.0, steps=150)
    nat = np.array(artkit.native(hi, 2, crisp=60)).astype(np.float64)
    nat[..., :3] = recede(nat[..., :3], 0.18)
    # its shadow on the sand (down-right), then the lander over the gouge
    sh = (nat[..., 3] > 0).astype(np.float64)
    sh = np.roll(np.roll(sh, 5, 0), 4, 1)
    base[..., :3] = np.where((sh > 0)[..., None] & (base[..., 3:] > 0), base[..., :3] * 0.6, base[..., :3])
    shadow_only = (sh > 0) & (base[..., 3] == 0)
    out = base.copy()
    out[shadow_only] = (60, 38, 28, 0)
    a = nat[..., 3:] / 255
    out[..., :3] = nat[..., :3] * a + out[..., :3] * (1 - a)
    out[..., 3] = np.maximum(out[..., 3], nat[..., 3])
    out[0], out[-1], out[:, 0], out[:, -1] = 0, 0, 0, 0
    img = posterize_rgb(out[..., :3], 40, out[..., 3])
    arr = np.array(img)
    arr[arr[..., 3] == 0] = 0
    return Image.fromarray(arr, "RGBA")


# ---- towers: the escarpment and the pylons

def cliff_roof():
    """The plateau above the escarpment at its drawn size: lava plain with dust, the lit rim on its west
    edge and its two ends (the face below is the wall texture)."""
    rw, rh = roof_size("escarpment-cliff")
    yy, xx = np.mgrid[0:rh, 0:rw].astype(np.float64)
    n = raster.fbm(rw, rh, 32, 1681, octaves=4, period=False)
    n2 = raster.fbm(rw, rh, 8, 1683, octaves=3, period=False)
    flows = smooth(0.5, 0.6, raster.fbm(rw, rh, 64, 1685, octaves=3, period=False))
    hgt = 3 * n + 1.2 * flows + 0.6 * n2
    edge = np.minimum.reduce([xx, yy, rh - 1 - yy])
    hgt -= 4 * np.exp(-edge / 3)
    t = np.clip(0.45 + 0.35 * (n - 0.5) - 0.25 * flows, 0, 1)
    alb = ramp([BASALT[1], BASALT[3], (136, 80, 50), (170, 110, 72)], t)
    rim = edge < 2.5
    alb = np.where(rim[..., None], np.array((196, 150, 112)), alb)
    rgb = lit(hgt, alb, wrap=False, ambient=0.5, key=0.66)
    rgb = recede(rgb, 0.15) * 1.04
    return posterize_rgb(rgb, 24)


def wall_escarpment():
    """The cliff face: columns run along the wall, rows from the foot (bottom) up to the rim (top). Two or three
    irregular strata (dark basalt flows between rust tuffs), deep gullies with their shadows, a talus apron at
    the foot, the bright rim; the west face is lit (the game draws the texture as it is on lit walls)."""
    tw, th = WALLS["wall-escarpment"]
    yy, xx = np.mgrid[0:th, 0:tw].astype(np.float64)
    z = 1 - yy / (th - 1)                                       # 0 at the foot, 1 at the rim
    n = pn(tw, th, 16, 1691, 3)
    g = pn(tw, th, 16, 1693, 2)
    gx = (xx / tw * 5 + 0.8 * (pn(tw, th, 16, 1695, 2) - 0.5)) % 1.0     # five gullies per repeat, wandering
    gully = np.clip(1.4 - np.abs(gx - 0.5) / (0.06 + 0.07 * g), 0, 1) * smooth(0.1, 0.25, z)
    lit_side = np.clip((gx - 0.5) * 8, -1, 1)                    # the gully wall facing the light
    zz = z + 0.06 * (n - 0.5)
    strata = ramp([(158, 104, 70), (190, 136, 94), (210, 162, 116)], 0.5 + 0.5 * np.sin(zz * 2 * np.pi * 1.6 + 0.6))
    basalt = (np.abs(zz - 0.42) < 0.07) | (np.abs(zz - 0.78) < 0.05)
    strata = np.where(basalt[..., None], np.array((84, 56, 44), float), strata)
    rgb = strata * (1 - 0.62 * gully[..., None] * (lit_side[..., None] < 0)) * (1 + 0.1 * gully[..., None] * (lit_side[..., None] > 0))
    talus = z < 0.14 + 0.08 * n
    rgb = np.where(talus[..., None], ramp([(130, 86, 60), (168, 120, 88)], n), rgb)
    rgb = np.where((z > 0.95)[..., None], np.array((226, 180, 132)), rgb)
    rgb *= (0.84 + 0.22 * z)[..., None]
    return posterize_rgb(recede(rgb, 0.12), 24)


def pylon_roof():
    rw, rh = roof_size("rail-pylon")
    a = np.zeros((rh, rw, 3)) + np.array((124, 124, 128))
    a[:, :2] = (160, 160, 164)
    a[:2, :] = (160, 160, 164)
    a[:, -2:] = (80, 80, 86)
    a[-2:, :] = (80, 80, 86)
    a[rh // 2 - 1:rh // 2 + 1, :] = (60, 60, 66)               # the cross-arm
    return raster.to_rgba_image(a)


def wall_pylon():
    tw, th = WALLS["wall-pylon"]
    a = np.zeros((th, tw, 4))
    a[..., :3] = (96, 96, 102)
    a[..., 3] = 255
    for y in range(th):                                          # lattice: the legs and the X bracing
        for x in range(tw):
            leg = x in (0, tw - 1)
            brace = abs((x - (y % tw))) < 1 or abs((tw - 1 - x) - (y % tw)) < 1
            if not (leg or brace):
                a[y, x] = 0
    return Image.fromarray(a.astype(np.uint8), "RGBA")


# =========================================================================== low-air

def cloud_bank(w, h, seed, cover, soft, max_alpha, stretch=1, x_period=True, colours=None, light=5.0):
    """A water-ice bank wrapping both ways (it drifts): r03's bank on periodic noise, blurred on a 3x3 tiling."""
    import parallax_r03 as r03
    n = pn(w, h, 96, seed, 3)
    big = np.tile(n, (3, 3))
    img = r03.bank(big, cover, soft, colours or CLOUD_D, max_alpha=max_alpha, light=light)
    img = img.crop((w, h, 2 * w, 2 * h))
    return r03.step_alpha(artkit.quantize_set([img], 12)[0], 6)


def deck_banks():
    return cloud_bank(W, TILE_H, 1701, 0.6, 0.08, 255)


def dust_layer(seed, cover, soft, max_alpha, stretch, band=None, levels=6, colours=12, cell=None, octaves=3):
    """Ochre dust stretched along the wind (from the left), wrapping both ways; ``band`` (centre row, half
    height) is kept empty (where a section tile set begins or ends)."""
    import parallax_r03 as r03
    w, h = W, TILE_H
    n = aniso(w, h, stretch, 1, cell or (20 if stretch >= 4 else 40), seed, octaves)
    if band is not None:
        yy = np.arange(h, dtype=np.float64)[:, None]
        dc = pdist(yy, band[0], h)
        n = n * np.clip((dc - band[1]) / 60, 0, 1)
    big = np.tile(n, (3, 3))
    img = r03.bank(big, cover, soft, DUST[1:], max_alpha=max_alpha, light=3.0)
    img = img.crop((w, h, 2 * w, 2 * h))
    return r03.step_alpha(artkit.quantize_set([img], colours)[0], levels)


def dust_thin():
    return dust_layer(1711, 0.64, 0.18, 72, 3, levels=8)


def dust_streaks():
    s = seam("low-air", 4)
    return dust_layer(1713, 0.62, 0.12, 110, 6, band=(TILE_H - 1 - (s % TILE_H) - 20, 110), levels=8)


def dust_peak():
    return dust_layer(1715, 0.42, 0.3, 140, 3, levels=10, cell=80, octaves=4)   # big cells: smooth rows (the wrap test)


def deck_close():
    """The opaque close-over: the deck's billowed top, lit from the upper left (peach tops, mauve shadows), a core
    DECK_CORE rows tall that is opaque everywhere, ragged billowy fringes above and below."""
    import parallax_r03 as r03
    w, h = W, DECK_H
    n = raster.fbm(w, h, 96, 1721, octaves=4, period=False)
    yy = np.arange(h, dtype=np.float64)[:, None]
    env = np.clip(np.minimum(yy, h - 1 - yy) / DECK_FRINGE, 0, 1)
    n2 = raster.fbm(w, h, 32, 1723, octaves=3, period=False)
    field = np.clip(0.5 + 0.45 * n - (1 - env) * 0.95 + (1 - env) * 0.45 * (n2 - 0.5), 0, 0.95)
    img = r03.bank(field, 0.45, 0.06, CLOUD_D, max_alpha=255, light=4.0)
    a = np.array(img)
    core = (yy >= DECK_FRINGE) & (yy < h - DECK_FRINGE)
    a[..., 3] = np.where(np.broadcast_to(core, a.shape[:2]), 255, a[..., 3])
    a[0], a[-1] = 0, 0
    return r03.step_alpha(artkit.quantize_set([Image.fromarray(a, "RGBA")], 12)[0], 6)


# =========================================================================== high-air

def cirrus():
    """Thin water-ice cirrus: fibrous streaks sheared 27 deg (x + y/2 wraps after 960 rows), pale peach-white;
    empty round the row where its tile set ends at section 3's seam."""
    w, h = W, TILE_H
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    coarse = pn(w // 4, h, 40, 1731, 3)
    fibre = pn(w // 8, h, 20, 1733, 2)
    xs = (xx + yy * 0.5) % w
    c = sample(coarse, yy, xs / 4)
    fb = sample(fibre, yy * 1.0, xs / 8)
    body = smooth(0.52, 0.8, c)
    strand = np.clip((fb - 0.35) * 2.2, 0, 1)
    a = body * (0.45 + 0.55 * strand)
    s = seam("high-air", 2)
    row = TILE_H - 1 - (s % TILE_H)                              # the tile row at the seam (top of the empty band)
    d = pdist(yy, row + 30, h)
    a *= np.clip((d - 110) / 70, 0, 1)
    col = np.zeros((h, w, 3)) + np.array((250, 238, 222))
    alpha = np.round(np.clip(a, 0, 1) * 10) / 10 * 170
    return raster.to_rgba_image(col, alpha)


def entry_streaks():
    """The entry's ionisation streaks along the flight with a faint warm sheen (additive, high-air)."""
    w, h = W, TILE_H
    rng = np.random.default_rng(1741)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    light = 0.18 * smooth(0.4, 0.9, pn(w, h, 96, 1743, 3))         # the glow sheen
    for k in range(76):
        x = rng.uniform(0, w)
        y0 = rng.uniform(0, h)
        length = rng.uniform(70, 320)
        width = rng.uniform(0.5, 1.4) if k % 9 else rng.uniform(5, 11)
        strength = rng.uniform(0.35, 1.0) if k % 9 else rng.uniform(0.25, 0.4)
        dy = (yy - y0) % h
        along = np.clip(dy / length, 0, 1) * (dy < length)
        prof = np.sin(np.pi * along) ** 0.7 * (dy < length)
        across = np.exp(-((xx - x) / width) ** 2)
        light = np.maximum(light, strength * prof * across)
    col = np.zeros((h, w, 3)) + np.array((255, 214, 172))
    col = col * (0.85 + 0.15 * light[..., None])
    alpha = np.round(np.clip(light, 0, 1) * 10) / 10 * 255
    return artkit.additive(raster.to_rgba_image(col, alpha))


# ---- the transport (SDF): CDF hull, nose up, its drop bay open through the hull

def transport_mats():
    hull = sdf.panel_lines(0.055, 0.05, 0.8)
    return [Material((0.46, 0.48, 0.52), metal=0.55, shininess=40, spec=0.45, pattern=hull),       # 0 hull
            Material((0.16, 0.30, 0.62), metal=0.4, shininess=40, spec=0.45),                      # 1 CDF blue
            Material((0.15, 0.16, 0.19), metal=0.5, shininess=30, spec=0.3),                       # 2 dark machinery
            Material((0.34, 0.35, 0.38), metal=0.6, shininess=50, spec=0.5, pattern=hull),         # 3 wings, pods
            Material((0.25, 0.20, 0.18), emission=(1.5, 0.95, 0.55)),                              # 4 nozzle glow
            Material((0.9, 0.9, 0.9), emission=(1.2, 1.2, 1.1)),                                   # 5 running lights
            Material((0.40, 0.38, 0.30), metal=0.3, shininess=30, spec=0.3)]                       # 6 bay lining


def transport():
    """The CDF transport seen from above, nose up: a long hull with a dorsal cargo spine and modules, swept
    stub wings with engine pods, the engine block astern with glowing nozzles, and the drop bay open
    through the hull (launch rails inside; the backdrop shows through it), CDF-blue bands, running lights."""
    w, h = TRANSPORT
    by, bw, bh = 70.0, 40.0, 62.0                 # the bay's centre y and half size

    def scene(p):
        q = mirror_x(p)
        hull = sd_box(p, (0, 0, 0), (62, 248, 24), 18)
        nose = sd_ellipsoid(p, (0, 246, 0), (62, 74, 24))
        spine = sd_box(p, (0, -40, 26), (20, 190, 9), 6)
        mods = [sd_box(q, (40, y, 22), (13, 24, 7), 4) for y in (-200, -140, -80, 170)]
        wing = sd_box(sdf.rotate_z(q - np.array([112.0, -90.0, -2.0]), 0.42), (0, 0, 0), (66, 30, 5), 3)
        pod = sdf.sd_cylinder_y(q, (166, -104, 3), 15, 56)
        intake = sdf.sd_cylinder_y(q, (166, -44, 3), 11, 6)
        block = sd_box(p, (0, -262, 0), (54, 18, 20), 6)
        noz = [sdf.sd_cylinder_y(q, (x, -284, 0), 11, 8) for x in (0, 30)]
        pnoz = sdf.sd_cylinder_y(q, (166, -164, 3), 10, 6)
        cut = sd_box(p, (0, by, 0), (bw, bh, 100), 3)
        rails = sd_box(q, (20, by, -12), (2.5, bh, 2.5), 0.5)
        lining = sd_box(p, (0, by, 0), (bw + 3, bh + 3, 23), 2)
        d, m = union((hull, 0), (nose, 0), (spine, 0), *[(md, 0) for md in mods], (wing, 3), (pod, 3), (intake, 2),
                     (block, 2), *[(nz, 2) for nz in noz], (pnoz, 2), k=4)
        d = np.maximum(d, -cut)                                           # the bay, open through the hull
        m = np.where((lining < 0.5) & (m == 0), 6, m)
        d, m = union((d, m), (rails, 2))
        band = ((np.abs(p[:, 1] - 196) < 7) | (np.abs(p[:, 1] + 226) < 5)) & (m == 0)
        m = np.where(band, 1, m)
        tip = (q[:, 0] > 150) & (np.abs(p[:, 1] + 60) < 8) & (m == 3)
        m = np.where(tip, 1, m)
        glow = ((p[:, 1] < -288) & (m == 2)) | ((p[:, 1] < -168) & (np.abs(q[:, 0] - 166) < 9) & (m == 2))
        m = np.where(glow, 4, m)
        lamps = (np.hypot(q[:, 0] - 172, p[:, 1] + 50) < 3.5) | (np.hypot(p[:, 0], p[:, 1] - 314) < 3.5) | \
                (np.hypot(q[:, 0] - 46, p[:, 1] + 278) < 3)
        m = np.where(lamps & (d < 2), 5, m)
        return d, m
    hi = sdf.render(scene, transport_mats(), (w * 2, h * 2), float(w), z_top=80.0, steps=140)
    nat = artkit.native(hi, 2, crisp=60)
    return k8.quantize_lit([nat], 44)[0]


# =========================================================================== jobs, output

GENERATORS = {
    "mars-high": mars_high, "mars-flank": mars_flank, "olympus": olympus,
    "slopes": slopes, "escarpment": escarpment, "dunes": dunes,
    "cirrus": cirrus, "entry-streaks": entry_streaks,
    "deck-banks": deck_banks, "dust-thin": dust_thin, "dust-streaks": dust_streaks, "dust-peak": dust_peak,
    "deck-close": deck_close, "transport": transport,
    "lava-terrace": lava_terrace, "basalt-outcrop": basalt_outcrop, "lycus-pads": lycus_pads, "lycus-hab": lycus_hab,
    "lycus-road": lycus_road, "drop-pod-ledge": drop_pod_ledge, "rail-line": rail_line, "crashed-lander": crashed_lander,
    "escarpment-cliff": cliff_roof, "wall-escarpment": wall_escarpment, "rail-pylon": pylon_roof, "wall-pylon": wall_pylon,
}
# the seam pieces read the tile sets back from assets: written after them
SECOND = {"mars-blend": mars_blend, "slopes-lead": slopes_lead, "seam-escarpment": seam_escarpment, "seam-dunes": seam_dunes}


def render_job(name):
    fn = GENERATORS.get(name) or SECOND[name]
    return name, fn()


def save(name, img):
    OUT.mkdir(parents=True, exist_ok=True)
    artkit.save_png(img, OUT / f"{name}.png", SOURCE)


def write_all(wanted):
    first = [n for n in GENERATORS if not wanted or n in wanted]
    second = [n for n in SECOND if not wanted or n in wanted or any(w in wanted for w in ("mars-high", "mars-flank", "slopes", "escarpment", "dunes"))]
    with ProcessPoolExecutor(max_workers=8) as pool:
        for name, img in pool.map(render_job, first):
            save(name, img)
            print(f"  {name}: {img.size[0]}x{img.size[1]}")
        for name, img in pool.map(render_job, second):
            save(name, img)
            print(f"  {name}: {img.size[0]}x{img.size[1]}")


# =========================================================================== the game's draw (review, checks)

_cache = {}


def cached(name):
    if name not in _cache:
        _cache[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA")).astype(np.float64)
    return _cache[name]


def section_tiles():
    return SECTION_TILES


def draw_tiles(backdrop, frame, layer, view, additive=None):
    f = backdrop["scroll_factors"][layer]
    scroll = jr(view * f)
    rows = scroll + (SCREEN - 1 - np.arange(SCREEN))
    tiles = section_tiles()
    for s in range(len(SECTIONS)):
        names = [n for n in tiles[s] if backdrop["tile_sets"][n]["layer"] == layer]
        if not names:
            continue
        lo = -math.inf if s == 0 else seam(layer, s)
        hi = seam(layer, s + 1) if s + 1 < len(SECTIONS) else math.inf
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(names[0])
            hgt = img.shape[0]
            part = img[[hgt - 1 - (int(r) % hgt) for r in rows[sel]]]
            sub = frame[sel]
            (k8.over(sub, part) if additive is None else k8.add(sub, part, additive))
            frame[sel] = sub


def piece_bottom(backdrop, placed):
    spec = backdrop["pieces"][placed["piece"]]
    return jr(scroll_at(placed["t"]) * backdrop["scroll_factors"][spec["layer"]] + MID - spec["size"][1] / 2)


def piece_rect(backdrop, placed, view):
    spec = backdrop["pieces"][placed["piece"]]
    w, h = spec["size"]
    f = backdrop["scroll_factors"][spec["layer"]]
    return jr(placed["x"] - w / 2), piece_bottom(backdrop, placed) - jr(view * f), w, h


def tower_geometry(backdrop, placed, view):
    """(foot x0, y0, w, h; roof x0, y0, w, h; k) on screen, y up, as TowerProjection draws it."""
    spec = backdrop["pieces"][placed["piece"]]
    fw, fh = spec["size"]
    k = tower_scale(spec["tower"]["height"])
    rw, rh = jr(fw * k), jr(fh * k)
    centre = scroll_at(placed["t"]) + MID
    fx = jr(placed["x"] - fw / 2)
    fy = int(jr(centre - fh / 2) - jr(view))
    rx = jr(240 + (placed["x"] - 240) * k - rw / 2)
    ry = jr((SCREEN - 297) + (centre - view - (SCREEN - 297)) * k - rh / 2)
    return (fx, fy, fw, fh), (rx, ry, rw, rh), k


def on_screen(backdrop, placed, view):
    spec = backdrop["pieces"][placed["piece"]]
    if "tower" in spec:
        (fx, fy, fw, fh), (rx, ry, rw, rh), _ = tower_geometry(backdrop, placed, view)
        return max(fx + fw, rx + rw) > 0 and min(fx, rx) < W and max(fy + fh, ry + rh) > 0 and min(fy, ry) < SCREEN
    x, y, w, h = piece_rect(backdrop, placed, view)
    return x + w > 0 and x < W and y + h > 0 and y < SCREEN


def draw_pieces(backdrop, frame, layer, view, additive=None, alpha=1.0):
    for placed in backdrop["placed"]:
        spec = backdrop["pieces"][placed["piece"]]
        if spec["layer"] != layer or "tower" in spec or not on_screen(backdrop, placed, view):
            continue
        x, y, w, h = piece_rect(backdrop, placed, view)
        img = cached(placed["piece"])
        if additive is None and alpha < 1:
            img = img.copy()
            img[..., 3] *= alpha
        k8.blit(frame, img, x, y, additive)


def draw_towers(backdrop, frame, view):
    """TowerProjection: per tower its walls facing the projection centre (SOUTH shaded, NORTH lit, WEST lit,
    EAST shaded), then its roof; lowest first."""
    towers = [p for p in backdrop["placed"] if "tower" in backdrop["pieces"][p["piece"]]]
    for placed in sorted(towers, key=lambda p: backdrop["pieces"][p["piece"]]["tower"]["height"]):
        if not on_screen(backdrop, placed, view):
            continue
        spec = backdrop["pieces"][placed["piece"]]["tower"]
        (fx, fy, fw, fh), (rx, ry, rw, rh), k = tower_geometry(backdrop, placed, view)
        tex = cached(spec["wall"])
        shade = spec.get("shade", 0.5)
        fx1, fy1, rx1, ry1 = fx + fw, fy + fh, rx + rw, ry + rh
        h = spec["height"]
        if ry > fy:
            k8.wall_quad(frame, tex, shade, ((fx, fy), (fx1, fy)), ((rx, ry), (rx1, ry)), h)
        if ry1 < fy1:
            k8.wall_quad(frame, tex, 1.0, ((fx1, fy1), (fx, fy1)), ((rx1, ry1), (rx, ry1)), h)
        if rx > fx:
            k8.wall_quad(frame, tex, 1.0, ((fx, fy1), (fx, fy)), ((rx, ry1), (rx, ry)), h)
        if rx1 < fx1:
            k8.wall_quad(frame, tex, shade, ((fx1, fy), (fx1, fy1)), ((rx1, ry), (rx1, ry1)), h)
        k8.blit(frame, cached(placed["piece"]), rx, ry)


def stretches():
    out = []
    for i, s in enumerate(SECTIONS):
        start, end = STARTS[i], float(s["end"])
        pk = s.get("peak")
        if pk:
            if pk["from"] > start:
                out.append((start, pk["from"], s["atmosphere"]))
            out.append((pk["from"], pk["to"], pk["atmosphere"]))
            if pk["to"] < end:
                out.append((pk["to"], end, s["atmosphere"]))
        else:
            out.append((start, end, s["atmosphere"]))
    return out


def looks_at(backdrop, t):
    st = stretches()
    current = next((i for i, s in enumerate(st) if t < s[1]), len(st) - 1)
    a = b = backdrop["atmosphere"][st[current][2]]
    weight = 1.0
    for i in range(1, len(st)):
        progress = (t - st[i][0]) / backdrop["ramp"] + 0.5
        if 0 < progress < 1:
            a, b = backdrop["atmosphere"][st[i - 1][2]], backdrop["atmosphere"][st[i][2]]
            weight = progress * progress * (3 - 2 * progress)
    return a, b, weight


def draw_look_tiles(backdrop, frame, look_a, look_b, weight, key, layer, view, t, additive=None):
    scroll = jr(view * backdrop["scroll_factors"][layer])
    rows = scroll + (SCREEN - 1 - np.arange(SCREEN))
    pairs = ((look_a, 1 - weight), (look_b, weight)) if look_a is not look_b else ((look_b, 1.0),)
    for look, share in pairs:
        name = look.get(key)
        if not name or share <= 0:
            continue
        img = cached(name)
        hgt = img.shape[0]
        drift = backdrop["tile_sets"][name].get("drift", 0) or 0
        part = np.roll(img[[hgt - 1 - (int(r) % hgt) for r in rows]], jr(drift * t) % W, axis=1)
        (k8.over(frame, part, share) if additive is None else k8.add(frame, part, additive * share))


def draw_drifting_tiles(backdrop, frame, layer, view, t):
    """A low-air section tile set that drifts (dust-streaks) as the game draws it."""
    f = backdrop["scroll_factors"][layer]
    scroll = jr(view * f)
    rows = scroll + (SCREEN - 1 - np.arange(SCREEN))
    tiles = section_tiles()
    for s in range(len(SECTIONS)):
        names = [n for n in tiles[s] if backdrop["tile_sets"][n]["layer"] == layer]
        if not names:
            continue
        lo = -math.inf if s == 0 else seam(layer, s)
        hi = seam(layer, s + 1) if s + 1 < len(SECTIONS) else math.inf
        sel = (rows >= lo) & (rows < hi)
        if sel.any():
            img = cached(names[0])
            hgt = img.shape[0]
            drift = backdrop["tile_sets"][names[0]].get("drift", 0) or 0
            part = np.roll(img[[hgt - 1 - (int(r) % hgt) for r in rows[sel]]], jr(drift * t) % W, axis=1)
            sub = frame[sel]
            k8.over(sub, part)
            frame[sel] = sub


@lru_cache(maxsize=None)
def stand_in(name, i=0):
    frames = artkit.load_frames(name)
    if not frames:
        held = ROOT / "build" / "art-held" / "sprites"
        f = held / f"{name}_{i}.png"
        if f.exists():
            frames = [Image.open(f).convert("RGBA")]
    if not frames:
        return None
    return np.array(frames[min(i, len(frames) - 1)]).astype(np.float64)


def blit_centre(frame, img, x, y_up):
    if img is None:
        return
    h, w = img.shape[:2]
    k8.blit(frame, img, jr(x - w / 2), jr(y_up - h / 2))


def draw_targets(frame, view):
    """The ground targets as stand-ins (their sprites where they exist), at their spots."""
    for role, spots in TARGETS.items():
        if role == "rail":
            continue                                        # hard only
        name = {"terrace": "polyp-mortar", "hab": "polyp-mortar", "containers": "lycus-container",
                "rubble": "drop-pod-rubble"}.get(role, "spine-turret")
        size = 24 if role == "containers" else 40 if role == "rubble" else 32
        for t, x in spots:
            y = target_pos(t, size) - view
            if -40 < y < SCREEN + 40:
                blit_centre(frame, stand_in(name), x, y)


def ship_view(frame):
    blit_centre(frame, stand_in("ship", 2), 240, 110)


def composite(backdrop, t, view=None, units=True, deep_haze=True):
    """The play field at level time t as the game draws it: deep (tiles, pieces), the haze veil, ground (tiles,
    flat pieces, towers), stand-ins, low-air (section tiles, banks, pieces), the ship, high-air (tiles and
    wisps additive at 40 %, the transport at 75 % as proposed)."""
    view = scroll_at(t) if view is None else view
    frame = np.zeros((SCREEN, W, 4))
    frame[..., 3] = 255
    look_a, look_b, weight = looks_at(backdrop, t)
    draw_tiles(backdrop, frame, "deep", view)
    draw_pieces(backdrop, frame, "deep", view)
    veil = look_a.get("haze", 0) * (1 - weight) + look_b.get("haze", 0) * weight if look_a is not look_b else look_b.get("haze", 0)
    if deep_haze and veil > 0:
        frame[..., :3] = frame[..., :3] * (1 - veil) + SKY * veil
    draw_tiles(backdrop, frame, "ground", view)
    draw_pieces(backdrop, frame, "ground", view)
    draw_towers(backdrop, frame, view)
    if units:
        draw_targets(frame, view)
    draw_drifting_tiles(backdrop, frame, "low-air", view, t)
    draw_look_tiles(backdrop, frame, look_a, look_b, weight, "banks", "low-air", view, t)
    draw_pieces(backdrop, frame, "low-air", view)
    if units:
        ship_view(frame)
    draw_tiles(backdrop, frame, "high-air", view, additive=0.4)
    draw_pieces(backdrop, frame, "high-air", view, alpha=0.75)
    draw_look_tiles(backdrop, frame, look_a, look_b, weight, "wisps", "high-air", view, t, additive=0.4)
    return Image.fromarray(np.clip(frame[..., :3], 0, 255).astype(np.uint8), "RGB")


# =========================================================================== checks

def check_all(backdrop):
    problems = []
    sizes = piece_sizes()
    for name, spec in backdrop["tile_sets"].items():
        im = Image.open(OUT / f"{name}.png")
        if im.size != (W, spec["height"]):
            problems.append(f"{name}: {im.size} is not {(W, spec['height'])}")
        score = k8.wrap_score(cached(name))
        cols = k8.wrap_score(cached(name), columns=True) if spec.get("drift") else 0.0
        print(f"  wrap {name}: rows {score:.2f}" + (f", columns {cols:.2f}" if spec.get("drift") else ""))
        if max(score, cols) > 1.5:
            problems.append(f"{name}: a line where it repeats ({score:.2f} / {cols:.2f})")
    for name, spec in backdrop["pieces"].items():
        size = tuple(spec["size"])
        if "tower" in spec:
            size = roof_size(name)
        im = Image.open(OUT / f"{name}.png")
        if im.size != size:
            problems.append(f"{name}: {im.size} is not {size}")
        if "tower" in spec:
            continue
        a = cached(name)[..., 3] > 0
        if a[0].any() or a[-1].any():
            problems.append(f"{name}: its top or bottom row is opaque")
        for placed in backdrop["placed"]:
            if placed["piece"] != name:
                continue
            x = placed["x"] - size[0] / 2
            if (0 < x < W and a[:, 0].any()) or (0 < x + size[0] < W and a[:, -1].any()):
                problems.append(f"{name}: a side column is opaque on screen")
    # the swap line under the deck's core all the way across (and its fringe never shows the line)
    line, tb, te = swap_line()
    deck = next(p for p in backdrop["placed"] if p["piece"] == "deck-close")
    f = backdrop["scroll_factors"]["low-air"]
    worst = 0.0
    for t in np.arange(tb - 0.2, te + 0.2, 1 / 60):
        view = scroll_at(t)
        bottom = piece_bottom(backdrop, deck) - jr(view * f)
        core_lo, core_hi = bottom + DECK_FRINGE, bottom + DECK_FRINGE + DECK_CORE
        y = line - jr(view)
        if -2 <= y <= SCREEN + 2:
            margin = min(y - core_lo, core_hi - y)
            worst = min(worst, margin) if worst < 0 else (margin if worst == 0 else min(worst, margin))
            if margin < 24:
                problems.append(f"t={t:.2f}: the swap line ({y} px up) is {margin:.0f} px inside the deck's core")
                break
    full = [t for t in np.arange(58, 70, 1 / 60)
            if piece_bottom(backdrop, deck) - jr(scroll_at(t) * f) + DECK_FRINGE <= 0
            and piece_bottom(backdrop, deck) - jr(scroll_at(t) * f) + DECK_FRINGE + DECK_CORE >= SCREEN]
    print(f"  close-over: the swap line crosses t {tb:.2f}-{te:.2f}, the field white t {full[0]:.2f}-{full[-1]:.2f}"
          f" ({full[-1] - full[0]:.2f} s), the line at least {worst:.0f} px inside the core")
    gone = next(t for t in np.arange(64, 75, 1 / 60) if piece_bottom(backdrop, deck) - jr(scroll_at(t) * f) + DECK_H <= 0)
    print(f"  the deck has left the field at t {gone:.2f}")
    if gone > 68:
        problems.append(f"the deck still on the field at t {gone:.2f} (the first Burrower at 68)")
    # the seam pieces lie on their seams
    for name, s, layer in (("mars-blend", 1, "deep"), ("seam-escarpment", 3, "ground"), ("seam-dunes", 4, "ground")):
        p = next(p for p in backdrop["placed"] if p["piece"] == name)
        b = piece_bottom(backdrop, p)
        hgt = backdrop["pieces"][name]["size"][1]
        if b != seam(layer, s) - hgt // 2:
            problems.append(f"{name} at {b} is not centred on its seam {seam(layer, s)}")
    lead = next(p for p in backdrop["placed"] if p["piece"] == "slopes-lead")
    lb = piece_bottom(backdrop, lead)
    if lb + backdrop["pieces"]["slopes-lead"]["size"][1] != seam("ground", 2) + FEATHER:
        problems.append("slopes-lead does not end FEATHER rows above section 3's seam")
    # density and motion, every second step to the outro's end
    seen = [False] * len(backdrop["placed"])
    for step in range(0, int(OUTRO_END * 60) + 1, 2):
        t = step / 60
        view = scroll_at(t)
        mid = []
        for i, placed in enumerate(backdrop["placed"]):
            if on_screen(backdrop, placed, view):
                seen[i] = True
                if backdrop["pieces"][placed["piece"]].get("mid_size"):
                    mid.append(placed["piece"])
        if len(mid) > 3:
            problems.append(f"t={t:.2f}: {len(mid)} mid-size pieces {mid}")
        moving = set()
        for name in [n for s, tl in enumerate(section_tiles()) for n in tl if backdrop["tile_sets"][n].get("drift")]:
            moving.add(name)
        look_a, look_b, w_ = looks_at(backdrop, t)
        banks = any(backdrop["tile_sets"].get(l.get("banks"), {}).get("drift") for l in (look_a, look_b) if l.get("banks"))
        ndrift = sum(1 for s in range(len(SECTIONS)) if STARTS[s] - 6 <= t <= float(SECTIONS[s]["end"]) + 6
                     for n in section_tiles()[s] if backdrop["tile_sets"][n].get("drift"))
        if ndrift + (1 if banks else 0) > 2:
            problems.append(f"t={t:.2f}: more than two drifting elements")
    problems += [f"{p['piece']} at t {p['t']} is never on screen" for p, s in zip(backdrop["placed"], seen) if not s]
    # no tower leans over a ground target
    spots = [(a[1], target_pos(a[0], 32), role) for role, v in TARGETS.items() for a in v]
    for placed in backdrop["placed"]:
        if "tower" not in backdrop["pieces"][placed["piece"]]:
            continue
        for t in np.arange(placed["t"] - 8, placed["t"] + 8, 0.1):
            view = scroll_at(t)
            (fx, fy, fw, fh), (rx, ry, rw, rh), _ = tower_geometry(backdrop, placed, view)
            for x, p, role in spots:
                y = p - view
                if (min(fx, rx) - 16 < x < max(fx + fw, rx + rw) + 16) and (min(fy, ry) - 16 < y < max(fy + fh, ry + rh) + 16) \
                        and placed["piece"] != "rail-pylon":
                    problems.append(f"{placed['piece']} at t {placed['t']} leans over the {role} target at x {x:g}")
                    break
    # cirrus under 40 %: its alpha x the game's 0.4 stays thin
    ca = cached("cirrus")[..., 3].max() / 255 * 0.4
    print(f"  cirrus: at most {ca * 100:.0f} % over the play plane; deck-banks cover "
          f"{(cached('deck-banks')[..., 3] > 0).mean() * 100:.0f} %, dust-peak {(cached('dust-peak')[..., 3] > 0).mean() * 100:.0f} %"
          f" at most {cached('dust-peak')[..., 3].max() / 2.55:.0f} %")
    return sorted(set(problems))[:30]


def run_checks():
    backdrop, src = level_backdrop()
    print(f"checks on the block from {src}:")
    problems = check_all(backdrop)
    diff = data_differences(backdrop)
    if diff:
        print(f"  the level data's merged block differs from the proposal in {len(diff)} place(s) (re-merge): "
              + "; ".join(diff[:12]) + (" ..." if len(diff) > 12 else ""))
    for p in problems:
        print("  PROBLEM:", p)
    print(f"  {len(problems)} problem(s)")
    return problems


def atlas_area(backdrop):
    px = sum(W * s["height"] for s in backdrop["tile_sets"].values())
    for name in backdrop["pieces"]:
        im = Image.open(OUT / f"{name}.png")
        px += im.width * im.height
    for name in WALLS:
        im = Image.open(OUT / f"{name}.png")
        px += im.width * im.height
    return px


# =========================================================================== review

TIMES = [(0.6, "1 THE LAUNCH: THE TRANSPORT, ENTRY STREAKS"), (5, "1 THE ENTRY GLOW"), (16, "1 OLYMPUS FROM THE CORRIDOR"),
         (40, "2 THE CLOUD DECK, THE FLANK"), (61.6, "2 THE DECK CLOSES"), (63, "2 CLOSED: THE GROUND TAKES OVER"),
         (65.4, "3 THE DECK PARTS OVER THE SLOPES"), (81.5, "3 THE LAVA TERRACE (MORTARS)"), (93.5, "3 THE BASALT NEST"),
         (121, "4 LYCUS STATION, THE CLIFF"), (129.5, "4 THE LEDGE, THE SECRET"), (161.5, "5 THE CRASHED LANDER"),
         (175, "5 THE DUST PEAK"), (182.5, "5 THE RAIL LINE")]


def review():
    backdrop, src = level_backdrop()
    artkit.REVIEW_ROUND = "r38"
    half = lambda n, d=2: Image.open(OUT / f"{n}.png").convert("RGBA").resize(
        (Image.open(OUT / f"{n}.png").width // d, Image.open(OUT / f"{n}.png").height // d), Image.NEAREST)
    deep = [half(n) for n in ("mars-high", "mars-flank")] + [Image.open(OUT / "olympus.png"), half("mars-blend")]
    ground = [half(n) for n in ("slopes", "escarpment", "dunes")]
    seams = [half(n) for n in ("slopes-lead", "seam-escarpment", "seam-dunes")]
    low = [half(n) for n in ("deck-banks", "dust-thin", "dust-streaks", "dust-peak")] + [half("deck-close", 3)]
    high = [half(n) for n in ("cirrus", "entry-streaks")] + [half("transport")]
    pieces = [Image.open(OUT / f"{n}.png") for n in ("lava-terrace", "basalt-outcrop", "lycus-pads", "lycus-hab",
                                                     "drop-pod-ledge", "crashed-lander")]
    long_ = [half("lycus-road"), Image.open(OUT / "rail-line.png"), half("escarpment-cliff"),
             Image.open(OUT / "wall-escarpment.png").resize((192, 128), Image.NEAREST),
             Image.open(OUT / "rail-pylon.png").resize((52, 52), Image.NEAREST),
             Image.open(OUT / "wall-pylon.png").resize((32, 128), Image.NEAREST)]
    comps = []
    for t, label in TIMES:
        img = composite(backdrop, t).convert("RGBA")
        raster.draw_text(img, 4, SCREEN - 12, f"T={t:g} {label}", raster.LABEL)
        comps.append(img)
    px = atlas_area(backdrop)
    rows = [
        ("DEEP (HALF SIZE): MARS-HIGH, MARS-FLANK; OLYMPUS (1X); MARS-BLEND", deep, 1, False),
        ("GROUND TILE SETS (HALF): SLOPES, ESCARPMENT, DUNES", ground, 1, False),
        ("SEAM PIECES (HALF): SLOPES-LEAD (THE SWAP LINE), SEAM-ESCARPMENT, SEAM-DUNES", seams, 1, False),
        ("LOW-AIR (HALF): DECK-BANKS, DUST-THIN, DUST-STREAKS, DUST-PEAK; DECK-CLOSE (1/3)", low, 1, False),
        ("HIGH-AIR (HALF; CIRRUS AND STREAKS ARE ADDED AT 40 %): CIRRUS, ENTRY-STREAKS, TRANSPORT", high, 1, False),
        ("GROUND PIECES: LAVA TERRACE, BASALT OUTCROP, LYCUS PADS, HAB, LEDGE, CRASHED LANDER", pieces, 1, False),
        ("ROAD (HALF), RAIL LINE; TOWERS: CLIFF ROOF (HALF), ITS WALL (2X), PYLON ROOF AND WALL (4X)", long_, 1, False),
        ("COMPOSITES AS THE GAME DRAWS THEM (STAND-INS: TURRETS, MORTARS, THE PROPS, THE SHIP)", comps[0:3], 1, False),
        ("", comps[3:6], 1, False), ("", comps[6:9], 1, False), ("", comps[9:12], 1, False), ("", comps[12:], 1, False),
    ]
    sheet = artkit.review_sheet(f"LEVEL 15 BACKDROP - OLYMPUS DESCENT ({px / 1e6:.2f} M PX, ABOUT {px / 2048 ** 2:.1f} PAGES)",
                                rows, width=1520, batch=BATCH)
    REVIEW.parent.mkdir(parents=True, exist_ok=True)
    sheet.convert("RGB").save(REVIEW, optimize=True)
    frames = []
    for t in list(np.arange(0.0, 9.0, 0.25)) + list(np.arange(58.0, 69.0, 0.2)):
        frames.append(composite(backdrop, float(t)).resize((240, 270), Image.BOX))
    from render.enemy_rigs import write_gif
    write_gif(frames, REVIEW.with_suffix(".gif"), fps=10, colors=128)
    print(f"review: {REVIEW.relative_to(ROOT)} and .gif ({px / 1e6:.2f} M px of backdrop images)")


def strip_review(times, path):
    backdrop, _ = level_backdrop()
    imgs = [composite(backdrop, t) for t in times]
    out = Image.new("RGB", (W * len(imgs) + 10 * (len(imgs) - 1), SCREEN))
    for i, im in enumerate(imgs):
        out.paste(im, (i * (W + 10), 0))
    out.save(path)
    print(f"strip: {path}")


def main(argv):
    if "--proposal" in argv:
        write_proposal()
        return
    if "--check" in argv:
        sys.exit(1 if run_checks() else 0)
    if "--review" in argv:
        review()
        return
    if "--strip" in argv:
        i = argv.index("--strip")
        strip_review([float(v) for v in argv[i + 1].split(",")], argv[i + 2])
        return
    wanted = [a for a in argv if not a.startswith("--")]
    if not wanted:
        write_proposal()
    write_all(wanted)
    if not wanted:
        run_checks()
        review()


if __name__ == "__main__":
    main(sys.argv[1:])
