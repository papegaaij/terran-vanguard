#!/usr/bin/env python3
"""Production art: Level 50 "Choir Heart", the hive core and the closing gate (design/campaign/act-7-beyond-the-gate/
level-50-choir-heart; M11 part H batch, concept round 95, step ART-H2): the hive-core backdrop kit, the seed vault, the
closing-gate scene kit. The Heart itself is tools/art/choir_heart.py's (ART-H; its review is drawn over ``arena_bg``
here); the four ending pages and Level 50's two briefing images are tools/art/briefing_images.py's (batch r95); the
Choir's face frame is tools/art/portraits.py's (``the-choir-face``).

Straight to production from round 84's chosen looks: Q10 b's **one Vrell palette** with Level 50's light (design/world/
vrell-space, *Light per level*: the palette's near-black violet tissue lit by the Heart's slow red-violet pulse, <= 1 Hz,
smooth, never a flash; the pulse's brightest below the bullets' cores; the units a step brighter than the walls) and
A.10 a's closing gate (tools/concept/act7_r84.py ``Closing``: the hive greying from the dead Heart outward, the race
through the grey chambers, the folds behind, the event plane growing while its rim contracts, the warp reversed, the
near side with Neptune and the *Steadfast*'s group, the plane shrinking to a point inside the ring, the glyphs going
dark one by one). The ring and the plane are round 84's painters (``ring_paint``, ``plane_rgb``, imported unchanged,
as tools/art/backdrop_l43.py), the *Steadfast*'s group Level 40's (tools/art/backdrop_l40.py ``fleet_far``). Every
glow, haze and light is a float field written unquantised (far more than 32 steps, no ordered dither: DitherGuardTest's
measure) that fades to 0 inside its canvas; nothing is ruler-straight (``--check``: step FIXC-SPINE's row/column scan
and FIXE-CITADEL's alpha-silhouette scan). Vacuum: no haze.

The layers (``scroll_factors``): ``deep`` 0.12 (one continuous field: the Throat's rib rings, the Choir Chambers' vaults,
the Brood Halls' folds, the Last Chamber's veins converging on the Heart, the Heart's chamber at the top; cut into the
sections' tile sets at the rows the game shows them, ``LevelData.seam``, so a section change has no seam), ``ground`` 1
(section 3 only: the Brood Halls' living floor), ``low-air`` 1.3 (the Throat's ribbed walls, the singing ribs, the vein
cables; transparent in the middle). ``far`` and ``high-air`` unused.

Tile sets (480 wide, rows y-up as the game reads them; each wraps in y):
  deep-1 .. deep-N     ``deep``, opaque: the field cut at the sections' seams (the arena's and the scene's: its top)
  throat-ribs          ``low-air``, 480x1024, section 1: the tube's ribbed walls along both sides (at most x 150 / 330
                       at a rib's tip), slow-pulsing violet veins on the ribs; open in the middle
  singing-ribs         ``low-air``, 480x1024, section 2: crystalline ribs sweeping in from both sides, pale violet, the
                       Heralds' ring markings grown into them (their light in ``singing-ribs-glow``)
  vein-cables          ``low-air``, 480x1024, section 4: three vein cables a side, the inner ones bending inward
                       (toward the Heart ahead), red-violet light inside them
  brood-floor          ``ground``, 480x1024, section 3, opaque: the living tissue floor in rounded lobes, creases with
                       dim red-violet veins, sealed spawning pits (puckered shut: no glow, no hazard), the hall's walls
                       darker toward the sides
  <set>-veins          additive, at half size (drawn at 2x; not listed in ``tile_sets``): the veins' pulsing share (the sets hold
                       their veins at about 45 %); ``singing-ribs-glow`` the ring markings' light (the singing)
  Every low-air set is cleared (alpha 0) along a ragged band at the rows where its section meets another (the seams
  the proposal computes), so no straight cut shows where a set ends; this repeats every 1024 rows (a gap in the walls).
Pieces:
  floor-lead           ground 480x256 at section 3's ground seam (exact rows): the floor's own rows (as the tile) above
                       the seam, below it the floor ending in a ragged lip (beyond it the deep shows)
  floor-end            ground 480x256 at section 4's ground seam (exact rows): the floor's rows below the seam, above it
                       the floor ending in a ragged lip (the cut pieces abut their tile row for row: the edge exception)
  pulse-light          512x512 additive: the Heart's pulse as a smooth light (red-violet, (1 - d^2)^3 to 0 at the
                       canvas's edge); drawn by the game screen-space, centred and scaled per section (the proposal's
                       ``pulse`` block: section 1 none, reaching further each chamber, the arena round the Heart), alpha
                       = the section's peak x the pulse (0..1, smooth, in time with HEARTBEAT; <= 1 Hz)
Sprites (build/art-held/sprites/ until the level's data names the root, then assets/sprites/: belt_units' rule):
  seed-vault_0..2        40x40 normal: the seed vault (the secret, a 30 HP ground trigger sunk in the floor): a
                         translucent-looking chitin dome in pale amber-violet, ribbed, packed with pale mint seeds
                         showing through its windows; 0 whole, 1 cracked, 2 broken open (the crate is out)
  seed-vault-break_0..7  64x64 normal: the dome bursting: chitin chips and seeds flung out, shrinking

The closing gate (``closing_gate`` in the proposal; drawn by step H4's ClosingGateLooks from K+10.5, not by the
section tiles: the scene's layers change faster than a tile set's seam allows):
  arena-grey           deep 480x700: the deep field's rows round the halted arena in the dead hive's grey (its rows
                       exactly, centred on the screen at the halt; ragged ends 80 rows outside the screen)
  grey-mask            480x540 RGB data image (not drawn): R = the greying threshold, 0 at the dead Heart (240, 150)
                       .. 1 at the farthest corner, smooth noise; grey shown = smoothstep(R - 0.08, R + 0.08,
                       p x 1.16 - 0.08), p 0 -> 1 over 3 s from K+10.5 (arena-grey over the arena): the luminance
                       one-way, no flash
  race-1 .. race-5     240x576 each (stored at half size, drawn at 2x: 480x1152), opaque (race-1's bottom 150 rows a ragged lip): ONE continuous grey strip of
                       5760 rows, race-1 the lowest: the Last Chamber (veins), the Brood Halls (the floor, its sealed
                       pits), the Choir Chambers (vaults, crystal spokes), the Throat (rib rings), all still and grey,
                       in that (reverse) order; stacked, strip row z at screen y = offset - z, offset = the ground
                       scroll since K+12 (race-1's lip enters at the top edge as the scroll starts)
  fold_0..7            480x200 normal: the tissue folding closed behind, grey lobes rolling (8 fps loop), anchored
                       on the screen's bottom edge (its bottom row opaque: anchored below the screen)
  plane-disc           512x512 normal: the event plane (round 84's plane_rgb, its streaks bent) in a disc of radius 244,
                       its rim shimmering +-3 px over a 2.5 px edge (0 by r 250); scaled by the game (the race: growing ahead, its rim contracting; the near
                       side: in the ring's mouth, shrinking to a point and fading)
  plane-rim            576x576 additive: the plane's violet rim glow at r 244, scaled with the disc
  near-space           480x1024 opaque, wraps in y: the near side's ordinary starfield (stars, a faint grey-blue
                       dust), drawn at the deep factor after the warp
  neptune / neptune-glow   40x40 normal / 72x72 additive: Neptune small and blue
  steadfast-group / -glow_0..7   128x96: Level 40's carrier group, as Level 43's (4 fps strobes)
  ring-arc             480x440 normal: the ring's upper arc behind the wing (the ring centre at canvas (240, 390),
                       R 300, half 56; transparent in its mouth), its glyphs unlit; runs off the canvas's sides and
                       bottom (anchored there: the screen's edges)
  ring-arc-glyphs      480x440 additive: the glyphs' cold teal-white light (Level 43's 0.8)
  glyph-order          480x440 RGB data image (not drawn): R = a glyph's group / 8 (group 1..8, uniform over a glyph;
                       the data's GLYPH_DARK beats 1..8 at K+35 .. K+37.8): GLYPH_DARK n fades group n's light out
                       over 0.3 s, so the glyphs go dark one by one round the arc from its top

Review (round 95): the level's concept/ ``backdrop-final-r95-a`` (.png: ten screens of the approach and the arena
with stand-ins and bullets, the kit; .gif: the approach in a time-lapse), ``closing-gate-final-r95-a`` (.png: the scene
from K+10.5 to K+40 with the luminance plot; .gif at 1x), ``seed-vault-final-r95-a`` (.png/.gif).
``--proposal`` writes the ``backdrop:`` block, the sections, the pulse and the closing gate's numbers to
concept/backdrop-proposal.yaml (step H2 owns the sections; re-run after the data moves a section). Held in
build/art-held/backdrop/level-50/ until the level's data.yaml has a ``backdrop`` block, then assets/backdrop/level-50/.
Run: nice -n 19 python3 tools/art/act7_core.py [backdrop] [vault] [--review] [--proposal] [--check]   (~2-3 min)
"""
import os
import sys
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
import yaml
from PIL import Image

import act7_citadel as cit
import act7_memory as mem
import act7_reef as reef
import act7_sphere as sph
import artkit
import belt_units as bu
import jovkit
import l23kit
from artkit import DESIGN, ROOT, TAU
from jovkit import additive_rgb, checker_share, rgba, smoothstep, wrap_score, wrap_score_alpha

sys.path.insert(0, str(Path(__file__).resolve().parents[1] / "concept"))
import act7_r84 as a84  # noqa: E402  (concept: the ring, the plane, the nebula, the warp, the bullets)
from render import raster  # noqa: E402
from render.sdf import Material, sd_capsule, sd_ellipsoid, sd_sphere, union  # noqa: E402

SCRIPT = "act7_core.py"
BATCH = "M11 part H batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r95"
artkit.REVIEW_ROUND = ROUND                     # (the imports set their own rounds)
LEVEL_DIR = DESIGN / "campaign" / "act-7-beyond-the-gate" / "level-50-choir-heart"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
ASSETS_OUT = ROOT / "assets" / "backdrop" / "level-50"
HELD_OUT = ROOT / "build" / "art-held" / "backdrop" / "level-50"
OWN_BACKDROP = bool(DATA and DATA.get("backdrop") and DATA["backdrop"].get("images", "level-50") == "level-50")
OUT = ASSETS_OUT if OWN_BACKDROP else HELD_OUT       # (a stand-in block naming another level's images keeps it held)
W, SCREEN, SPEED = 480, 540, 100.0
FACTORS = {"deep": 0.12, "far": 0.35, "ground": 1.0, "low-air": 1.3, "high-air": 2.0}
SEED = 5000
WORKERS = min(6, os.cpu_count() or 1)
TILE_H = 1024

# The palette (Q10 b, Level 50's light): near-black violet tissue, the Heart's red-violet.
TISSUE = [(0.0, (3, 2, 7)), (0.4, (12, 6, 18)), (0.7, (26, 12, 34)), (1.0, (50, 24, 60))]
WALL = [(0.0, (6, 3, 10)), (0.45, (20, 9, 28)), (0.8, (40, 19, 52)), (1.0, (66, 34, 82))]
CRYSTAL = [(0.0, (22, 16, 36)), (0.5, (58, 48, 90)), (0.85, (100, 86, 142)), (1.0, (138, 122, 182))]
GREY = [(0.0, (9, 9, 11)), (0.45, (30, 29, 33)), (0.8, (54, 52, 58)), (1.0, (82, 80, 88))]
VEIN_V = np.array((84, 40, 124), float)         # the violet veins (sections 1-2)
VEIN_R = np.array((124, 34, 92), float)         # the red-violet veins (section 3 on), a step under the Heart's
MARK = np.array((170, 126, 255), float)         # the Heralds' ring markings, singing
RV = np.array(a84.RV, float)                    # the Heart's red-violet (200, 60, 150)
PULSE_COL = RV * 0.55                           # the pulse light's peak (110, 33, 82): far below a bullet's core
EV = np.array(a84.EV_VIOLET, float)             # the event plane's violet rim
VEIN_SHARE = 0.45                               # the tiles hold their veins at this share; -veins adds the rest

# =========================================================================== the clock

# Proposed sections (clock time; step H2 owns them): the approach at 100 px/s, the Heart's arena halted (the scroll
# eased to 0 over 11 s from the warning: in place at t 193), the clock jumped to the arena's end at the kill K (paper
# 349), hold_after_boss 12 s, the race at 300 px/s (eased over 3 s; the warp at its end), the near side at 60 px/s.
PROPOSED_SECTIONS = [
    {"name": "The Throat", "end": 45, "atmosphere": "clear", "tiles": ["deep-1", "throat-ribs"]},
    {"name": "The Choir Chambers", "end": 97, "atmosphere": "clear", "tiles": ["deep-2", "singing-ribs"]},
    {"name": "The Brood Halls", "end": 135, "atmosphere": "clear", "tiles": ["deep-3", "brood-floor"]},
    {"name": "The Last Chamber", "end": 182, "atmosphere": "clear", "tiles": ["deep-4", "vein-cables"]},
    {"name": "The Heart", "end": 349, "speed": 0, "ramp": 11, "arena": True, "atmosphere": "clear",
     "tiles": ["deep-5"]},
    {"name": "The hive goes still", "end": 361, "speed": 0, "atmosphere": "clear", "tiles": ["deep-6"]},
    {"name": "The race", "end": 379.5, "speed": 300, "ramp": 3, "atmosphere": "clear", "tiles": ["deep-7"]},
    {"name": "The near side", "end": 407, "speed": 60, "ramp": 2, "atmosphere": "clear", "tiles": ["deep-8"]},
]
SECTIONS = DATA["sections"] if DATA and DATA.get("sections") else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock([{"end": s["end"], "speed": s.get("speed"), "ramp": s.get("ramp", 0)} for s in SECTIONS],
                     BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
seam = CLOCK.seam
DEEP = [f"deep-{i + 1}" for i in range(len(SECTIONS))]
I_ARENA = next(i for i, s in enumerate(SECTIONS) if s.get("arena"))
K = float(SECTIONS[I_ARENA]["end"])             # the kill on the level clock (the arena's end: the clock stands there)
HOLD = float(SECTIONS[I_ARENA].get("hold_after_boss", 0) or 0)


def scene_clock(tk):
    """The level clock at K + tk s: the data's clock stands for hold_after_boss s after K (H2's sections); the
    proposal's own sections hold in a section of their own (the clock running on)."""
    return K + max(0.0, tk - HOLD) if HOLD else K + tk


T_RACE = scene_clock(12.0)
# section index -> its low-air set (the vein cables on to the arena's start: a braking stretch keeps them)
LOW_SETS = {0: "throat-ribs", 1: "singing-ribs", **{i: "vein-cables" for i in range(3, I_ARENA)}}
FLOOR_SECTION = 2


def drow(t):
    """The deep layer row at the screen's bottom edge at level time t."""
    return CLOCK.scroll(t) * FACTORS["deep"]


D_HALT = drow(K)                                 # the deep at the halt (the scroll stands from the Heart's arrival)
HEART = (240.0, 150.0)                           # the Heart's centre on the screen in its arena
HEART_ROW = D_HALT + SCREEN - HEART[1]           # its deep row


# =========================================================================== small kit

def grid(h, w=W):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    return yy + 0.5, xx + 0.5


def nf(w, h, cell, seed, octaves=4):
    return raster.fbm(w, h, max(1, int(round(cell))), seed, octaves=octaves, period=False)


def pf(h, cx, cy, seed, octaves=4):
    return mem.pf(h, cx, cy, seed, octaves)


def ramp(stops, v):
    return raster.ramp(stops, np.clip(v, 0, 1))


def relief(hgt, k, wrap_y=False):
    """A relief shade from a height field (image rows down), the key light from the top left."""
    gy = (np.roll(hgt, -1, 0) - np.roll(hgt, 1, 0)) / 2 if wrap_y else np.gradient(hgt, axis=0)
    gx = np.gradient(hgt, axis=1)
    return np.clip(0.85 + (gx + gy) * k, 0.35, 1.45)


def wrapped(d, period):
    return (d + period / 2) % period - period / 2


def ragged1(w, seed, amp, cell=64):
    """A ragged line's offsets along x (px): smooth noise plus a short sine, never flat for long."""
    x = np.arange(w) + 0.5
    n = nf(w, 8, cell, seed, 3)[4]
    return amp * (n - 0.5) * 2 + 0.22 * amp * np.sin(x / 19.0 + seed % 7) + 0.12 * amp * np.sin(x / 7.3 + seed % 5)


def voronoi(h, w, cell, seed, wrap_y=False, jitter=0.85, warp=None):
    """F1, F2 and the nearest site's index of jittered sites on a cell grid (wrapping in y when asked: h % cell == 0);
    ``warp`` = (dx, dy) fields bending the domain, so the cells' walls curve (a plain Voronoi's are straight)."""
    rng = np.random.default_rng(seed)
    yy, xx = grid(h, w)
    if warp is not None:
        xx, yy = xx + warp[0], yy + warp[1]
    nx, ny = int(np.ceil(w / cell)), int(np.ceil(h / cell))
    js = range(ny) if wrap_y else range(-1, ny + 1)
    f1 = np.full((h, w), 1e9)
    f2 = np.full((h, w), 1e9)
    idx = np.zeros((h, w), int)
    k = 0
    for j in js:
        for i in range(-1, nx + 1):
            px = (i + 0.5 + jitter * (rng.random() - 0.5)) * cell
            py = (j + 0.5 + jitter * (rng.random() - 0.5)) * cell
            dy = wrapped(yy - py, h) if wrap_y else yy - py
            d = np.hypot(xx - px, dy)
            closer = d < f1
            f2 = np.where(closer, f1, np.minimum(f2, d))
            idx = np.where(closer, k, idx)
            f1 = np.minimum(f1, d)
            k += 1
    return f1, f2, idx


def to_grey(rgb, gain=1.0, tex=None):
    """The dead hive: the colour's brightness in the grey ramp (the veins gone dark)."""
    lum = rgb @ np.array((0.299, 0.587, 0.114))
    v = np.clip(lum / 46.0 * gain, 0, 1) ** 0.75
    if tex is not None:
        v = v * (0.85 + 0.3 * tex)
    return ramp(GREY, 0.12 + 0.8 * v)


def edge_clear(h, w, rows, seed, width=120):
    """A low-air set's alpha multiplier: 0 along a ragged band round the image rows ``rows`` (wrapping)."""
    yy, _ = grid(h, w)
    m = np.ones((h, w))
    for k, r in enumerate(rows):
        off = ragged1(w, seed + 17 * k, 46)[None, :]
        d = np.abs(wrapped(yy - r - off, h))
        m = np.minimum(m, smoothstep(width * 0.25, width, d))
    return m


def low_seam_rows(name):
    """The image rows of a low-air set at which its section meets another section's set (or none)."""
    rows = []
    for i, nm in LOW_SETS.items():
        if nm != name:
            continue
        for k, other in ((i, i - 1), (i + 1, i + 1)):
            if 0 < k < len(SECTIONS) and LOW_SETS.get(other) != name:
                lr = seam("low-air", k)
                rows.append(TILE_H - 1 - (lr % TILE_H))
    return sorted(set(rows))


# =========================================================================== the deep field (one, cut at the seams)

def throat_look(xx, rr, seed, scale=1.0):
    """The Throat: the tube's rib rings bowing toward the Heart ahead, the walls rising at the sides; violet veins in the
    valleys and along the tube."""
    h, w = xx.shape
    n1 = nf(w, h, 70 * scale, seed, 3)
    n2 = nf(w, h, 22 * scale, seed + 1, 4)
    n3 = nf(w, h, 110 * scale, seed + 2, 3)
    side = ((xx - 240) / 240) ** 2
    ph = (rr + 34 * scale * side + 16 * scale * (n1 - 0.5)) / (58.0 * scale)
    rib = (0.5 + 0.5 * np.cos(TAU * ph)) ** 4
    hgt = 0.55 * rib * (0.7 + 0.3 * n3) + 0.3 * n2 + 0.3 * side
    sh = relief(hgt * 10, 1.0)
    v = 0.14 + 0.46 * rib * (0.55 + 0.45 * side) + 0.22 * (n2 - 0.5) + 0.22 * side
    rgb = ramp(TISSUE, v) * sh[..., None]
    valley = np.exp(-((np.abs(np.sin(np.pi * (ph + 0.5)))) / 0.16) ** 2) * smoothstep(0.45, 0.75, n3)
    lv = np.exp(-(np.abs(np.sin(nf(w, h, 90 * scale, seed + 3, 3) * 15)) / 0.07) ** 2) * (0.35 + 0.65 * side)
    vein = np.clip(0.55 * valley + 0.6 * lv, 0, 1)
    return rgb, vein[..., None] * VEIN_V


def chamber_look(xx, rr, seed, scale=1.0, rows0=0.0):
    """The Choir Chambers: vaulted chambers in a crowded cell pattern, crystalline ribs between them and spoking out
    from each vault's centre, the Heralds' ring markings in its floor (the light is the veins' channel)."""
    h, w = xx.shape
    cell = 150.0 * scale
    wx, wy = nf(w, h, 70 * scale, seed + 11, 3), nf(w, h, 70 * scale, seed + 12, 3)
    f1, f2, idx = voronoi(h, w, cell, seed, warp=(46 * scale * (wx - 0.5), 46 * scale * (wy - 0.5)))
    n2 = nf(w, h, 24 * scale, seed + 1, 4)
    edge = f2 - f1
    wall = np.exp(-(edge / (7.0 * scale)) ** 2)
    vault = np.clip(f1 / (0.55 * cell), 0, 1)
    rng = np.random.default_rng(seed + 5)
    turn = rng.uniform(0, TAU, idx.max() + 1)[idx]
    gy, gx = np.gradient(f1)                          # the direction from the vault's centre (F1's gradient)
    ang = np.arctan2(gy, gx) + turn
    spoke = np.exp(-((np.abs(np.sin((ang + 0.012 * f1 / scale) * 4))) * f1 / (3.2 * scale)) ** 2) \
        * smoothstep(18 * scale, 40 * scale, f1) * (1 - vault) ** 0.5
    marks = np.zeros((h, w))
    for k, rk in enumerate((24, 40, 56)):
        ring = np.exp(-((f1 - rk * scale) / (1.4 * scale)) ** 2)
        gap = smoothstep(-0.4, 0.1, np.cos(5 * ang + k * 1.7))
        marks += ring * gap
    marks *= smoothstep(0.75, 0.55, vault)
    hgt = 0.6 * vault ** 1.5 + 0.5 * wall + 0.35 * spoke + 0.25 * n2
    sh = relief(hgt * 9, 1.0)
    base = ramp(TISSUE, 0.12 + 0.42 * vault + 0.18 * (n2 - 0.5)) * sh[..., None]
    cr = np.clip(np.maximum(wall, spoke), 0, 1)
    crystal = ramp(CRYSTAL, 0.18 + 0.5 * cr * sh / 1.45 + 0.12 * (n2 - 0.5))
    rgb = base * (1 - 0.7 * cr[..., None]) + crystal * 0.75 * (0.7 * cr[..., None])
    light = np.clip(marks, 0, 1)[..., None] * MARK * 0.55 + (wall * 0.25 * (n2 > 0.55))[..., None] * VEIN_V
    return rgb, light


def brood_deep_look(xx, rr, seed, scale=1.0):
    """Under the Brood Halls' floor (seen only past its lips): the hall's dark folded pit walls."""
    h, w = xx.shape
    n1 = nf(w, h, 80 * scale, seed, 4)
    n2 = nf(w, h, 26 * scale, seed + 1, 3)
    fold = np.abs(np.sin(n1 * 9.0))
    hgt = 0.7 * fold + 0.3 * n2
    rgb = ramp(TISSUE, 0.06 + 0.3 * fold + 0.12 * (n2 - 0.5)) * relief(hgt * 8, 1.0)[..., None] * 0.8
    vein = np.exp(-(np.abs(np.sin(nf(w, h, 100 * scale, seed + 3, 3) * 13)) / 0.06) ** 2) * 0.5
    return rgb, vein[..., None] * VEIN_R


def last_look(xx, rr, seed, centre_row, scale=1.0):
    """The Last Chamber and the Heart's chamber: veins converging on the Heart's point ahead (thicker near it, a fine
    branching between them), the chamber's rib rings round it, the tissue a step lighter toward it."""
    h, w = xx.shape
    dx = xx - HEART[0]
    dy = centre_row - rr
    dist = np.hypot(dx, dy) + 1.0
    ang = np.arctan2(dx, np.abs(dy) + 1e-6) * np.sign(dy + 1e-6)
    n1 = nf(w, h, 90 * scale, seed, 3)
    n2 = nf(w, h, 22 * scale, seed + 1, 4)
    n4 = nf(w, h, 34 * scale, seed + 4, 3)
    th = ang + 0.22 * np.log1p(dist / 90.0) + 0.6 * (n1 - 0.5) + 0.12 * (n4 - 0.5)
    veins = np.zeros((h, w))
    for n, wd, k in ((22, 2.4, 0.8), (44, 1.2, 0.4)):
        perp = dist * (2.0 / n) * np.abs(np.sin(th * n / 2))
        width = (wd + 3.0 * np.exp(-dist / (520.0 * scale))) * scale
        which = np.round(th * n / TAU)                                   # each vein its own strength
        strength = 0.3 + 0.7 * ((np.sin(which * 12.9898 + seed) * 43758.5453) % 1.0)
        strength = strength * (0.55 + 0.45 * smoothstep(0.3, 0.7, nf(w, h, 60 * scale, seed + 5 + n, 3)))
        veins = np.maximum(veins, np.exp(-(perp / width) ** 2) * k * strength
                           * (1 if n == 22 else smoothstep(60, 260, dist)))
    ring = (0.5 + 0.5 * np.cos(TAU * dist / (46.0 * scale) + 4 * (n1 - 0.5))) ** 3
    lift = 1 + 0.4 * np.exp(-dist / 420.0)
    hgt = 0.45 * ring + 0.4 * veins + 0.25 * n2
    sh = relief(hgt * 9, 1.0)
    rgb = ramp(TISSUE, 0.12 + 0.3 * ring + 0.16 * (n2 - 0.5)) * (sh * lift)[..., None]
    rgb = rgb * (1 - 0.35 * veins[..., None]) + veins[..., None] * np.array((34, 12, 30.0))
    return rgb, np.clip(veins * (0.45 + 0.3 * np.exp(-dist / 600.0)), 0, 1)[..., None] * VEIN_R


def deep_span():
    return -40, int(np.ceil(drow(END + 4) + SCREEN))


def boundary(xx, rr, row, seed, width=110.0):
    """0 below a ragged boundary round deep row ``row``, 1 above (no straight band)."""
    n = nf(xx.shape[1], xx.shape[0], 70, seed, 3)
    return a84.smooth01((rr - row) / width + 0.5 + 0.9 * (n - 0.5) + 0.12 * np.sin(xx / 21.0 + seed % 5))


def deep_field():
    """The whole deep layer as float fields (rgb, vein light), row 0 = layer row r0 (y-up)."""
    lo, hi = deep_span()
    r0, r1 = lo - 400, hi + 400
    n = r1 - r0
    rr = (np.arange(n) + r0)[:, None] * np.ones((1, W))
    xx = np.ones((n, 1)) * (np.arange(W) + 0.5)[None, :]
    b = [seam("deep", k) for k in (1, 2, 3)]
    out_rgb = np.zeros((n, W, 3))
    out_v = np.zeros((n, W, 3))
    looks = [lambda x, r: throat_look(x, r, SEED + 10), lambda x, r: chamber_look(x, r, SEED + 20),
             lambda x, r: brood_deep_look(x, r, SEED + 30), lambda x, r: last_look(x, r, SEED + 40, HEART_ROW)]
    bands = [(r0, b[0] + 260), (b[0] - 260, b[1] + 260), (b[1] - 260, b[2] + 260), (b[2] - 260, r1)]
    for k, (look, (a, bb)) in enumerate(zip(looks, bands)):
        i0, i1 = max(0, int(a - r0)), min(n, int(bb - r0))
        rgb, v = look(xx[i0:i1], rr[i0:i1])
        if k == 0:
            out_rgb[i0:i1], out_v[i0:i1] = rgb, v
            continue
        m = boundary(xx[i0:i1], rr[i0:i1], b[k - 1], SEED + 50 + k)[..., None]
        if k < 3:
            top = int(min(n, bb - r0))
            m[i0 + (top - i0):] = 1
        out_rgb[i0:i1] = out_rgb[i0:i1] * (1 - m) + rgb * m
        out_v[i0:i1] = out_v[i0:i1] * (1 - m) + v * m
    return out_rgb, out_v, r0


def cut_float(field, r0, a, b):
    """jovkit.cut_tile for a float field (the veins' light): rows [a, b), wrapping by a cross-fade; top row highest."""
    span = b - a
    h = int(np.ceil((span + 160) / 32) * 32)
    out = np.zeros((h,) + field.shape[1:])
    for j in range(h):
        r = a + ((j - a) % h)
        if r < b:
            val = field[r - r0]
        else:
            wgt = smoothstep(b, a + h, r)
            val = (1 - wgt) * field[r - r0] + wgt * field[r - h - r0]
        out[h - 1 - j] = val
    return out


def deep_bounds():
    lo, hi = deep_span()
    return [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]


def deep_tiles():
    rgb, v, r0 = deep_field()
    lit = rgb + v * VEIN_SHARE
    bounds = deep_bounds()
    out = {}
    for i, name in enumerate(DEEP):
        a, b = bounds[i], max(bounds[i + 1], bounds[i] + 1)
        out[name] = jovkit.cut_tile(lit, r0, a, b)
        veins = cut_float(v * (1 - VEIN_SHARE), r0, a, b)
        out[name + "-veins"] = additive_rgb(veins if name in FULL_VEINS else half_light(veins))
    return out


# =========================================================================== the low-air sets

def side_u(xx):
    """The distance from the nearer side and which side (0 left, 1 right)."""
    return np.minimum(xx, W - xx), (xx > W / 2).astype(int)


def throat_ribs(seed=SEED + 100, h=TILE_H):
    """The tube's ribbed walls along both sides: ribs every ~85 rows reaching inward and curving down, veins on them."""
    yy, xx = grid(h)
    u, sd = side_u(xx)
    nA = pf(h, 120, 64, seed)
    nB = pf(h, 96, 128, seed + 1)
    n2 = pf(h, 24, 32, seed + 2)
    base = 52 + 30 * nA + 6 * np.sin(TAU * yy / (h / 22)) + 4 * np.sin(TAU * yy / (h / 9) + sd * 2.1) \
        + 2.2 * np.sin(TAU * yy / (h / 53) + sd) + 1.2 * np.sin(TAU * yy / (h / 97))
    ph = (yy + 0.32 * u + 0.004 * u * u + 26 * (nB - 0.5) + sd * 41) / (h / 12)
    rib = (0.5 + 0.5 * np.cos(TAU * ph)) ** 3
    reach = base + 92 * rib * (0.6 + 0.6 * nB)
    a = smoothstep(-1.0, 1.0, reach - u)
    depth = np.clip((reach - u) / 34.0, 0, 1)
    hgt = 0.6 * depth ** 0.5 + 0.45 * rib + 0.2 * n2
    sh = relief(hgt * 9, 1.0, wrap_y=True)
    rgb = ramp(WALL, 0.18 + 0.68 * depth ** 0.6 * (0.5 + 0.5 * rib) + 0.15 * (n2 - 0.5)) * sh[..., None]
    lipv = np.exp(-((reach - u - 2.5) / 2.0) ** 2) * a
    rgb += (lipv * (0.6 + 0.6 * (1 - sd)))[..., None] * np.array((52, 28, 66.0))   # the lit inner lip
    shadow = np.exp(-np.maximum(u - reach, 0) / 9.0) * smoothstep(-0.5, 1.5, u - reach) * 0.55
    rgb = np.where((a > 0.01)[..., None], rgb, 0.0)
    a = np.maximum(a, shadow)                               # a soft shadow on the deep inside the wall's edge
    vn = pf(h, 120, 128, seed + 3, 3)
    vein = np.exp(-(np.abs(np.sin(vn * 18)) / 0.08) ** 2) * smoothstep(0.2, 0.6, depth) * (0.4 + 0.6 * rib)
    a = a * edge_clear(h, W, low_seam_rows("throat-ribs"), seed + 7)
    light = vein[..., None] * VEIN_V * a[..., None]
    return rgba(rgb + light * VEIN_SHARE, a), light * (1 - VEIN_SHARE)


def singing_ribs(seed=SEED + 110, h=TILE_H):
    """Crystalline ribs sweeping in from both sides out of a thin tissue wall, pale violet facets lit from the top left,
    the Heralds' ring markings grown into them (their light separate: the singing)."""
    yy, xx = grid(h)
    u, sd = side_u(xx)
    rng = np.random.default_rng(seed)
    n2 = pf(h, 24, 32, seed + 2)
    nA = pf(h, 120, 64, seed + 3)
    wall_reach = 24 + 16 * nA + 4 * np.sin(TAU * yy / (h / 26))
    cov = smoothstep(-1.0, 1.0, wall_reach - u)
    rgb = ramp(WALL, 0.1 + 0.4 * np.clip((wall_reach - u) / 20, 0, 1) + 0.15 * (n2 - 0.5)) * cov[..., None]
    alpha = cov.copy()
    marks = np.zeros((h, W))
    for side in (0, 1):
        on = sd == side
        for k in range(5):
            y0 = (k + rng.uniform(-0.25, 0.25)) * h / 5 + side * h / 10
            length = rng.uniform(130, 200)
            a1, a2 = rng.uniform(0.15, 0.45), rng.uniform(0.0006, 0.0016)
            dyc = wrapped(yy - (y0 + a1 * u + a2 * u * u), h)
            t = np.clip(u / length, 0, 1)
            thick = 2.0 + 14.0 * (1 - t) ** 0.7
            s = dyc / thick
            inside = smoothstep(-1.0, 1.0, (1 - np.abs(s)) * thick) * (u < length) * on
            lit = np.where(s < 0, 0.62 + 0.2 * (1 - t), 0.3) + 0.12 * np.cos(u / 9.0 + k) - 0.18 * np.abs(s) ** 3
            col = ramp(CRYSTAL, np.clip(0.2 + 0.62 * lit * (1 - 0.35 * np.abs(s)), 0, 1))
            col += (np.exp(-(s / 0.18) ** 2) * (1 - t))[..., None] * np.array((40, 34, 60.0))     # the ridge
            rgb = rgb * (1 - inside[..., None]) + col * inside[..., None]
            alpha = np.maximum(alpha, inside)
            for j in range(rng.integers(2, 4)):
                um = rng.uniform(40, length * 0.7)
                ym = y0 + a1 * um + a2 * um * um
                xm = um if side == 0 else W - um
                d = np.hypot(xx - xm, wrapped(yy - ym, h))
                for rk in (3.0, 6.0):
                    marks += np.exp(-((d - rk) / 0.9) ** 2) * inside
    marks = np.clip(marks, 0, 1)
    alpha = alpha * edge_clear(h, W, low_seam_rows("singing-ribs"), seed + 7)
    light = (marks[..., None] * MARK * 0.8 + reef.blur3(marks[..., None] * MARK, 2.4) * 0.5) * alpha[..., None]
    return rgba(rgb + light * VEIN_SHARE * 0.6, alpha), light * (1 - VEIN_SHARE * 0.6)


def vein_cables(seed=SEED + 120, h=TILE_H):
    """Three vein cables a side, lengthwise, the inner ones bending inward toward the Heart ahead; red-violet light
    inside them through the skin."""
    yy, xx = grid(h)
    rng = np.random.default_rng(seed)
    rgb = np.zeros((h, W, 3))
    alpha = np.zeros((h, W))
    light = np.zeros((h, W, 3))
    n2 = pf(h, 24, 32, seed + 2)
    for side in (0, 1):
        for k, (bx, r0_) in enumerate(((14, 21), (62, 14), (104, 9))):
            m1, m2 = int(rng.integers(1, 3)), int(rng.integers(3, 6))
            p1, p2 = rng.uniform(0, TAU, 2)
            amp = 10 + 14 * k
            m3, m4 = int(rng.integers(9, 15)), int(rng.integers(21, 30))
            xc = bx + amp * np.sin(TAU * m1 * yy / h + p1) + 6 * np.sin(TAU * m2 * yy / h + p2) \
                + 3.0 * np.sin(TAU * m3 * yy / h + p1 * 2) + 1.4 * np.sin(TAU * m4 * yy / h + p2 * 3)
            xc = np.where(side == 0, xc, W - xc)
            r = r0_ * (1 + 0.18 * np.sin(TAU * m2 * yy / h + p1))
            s = (xx - xc) / r
            inside = smoothstep(-1.0, 1.0, (1 - np.abs(s)) * r)
            nz = np.sqrt(np.clip(1 - s * s, 0, 1))
            lam = np.clip(0.3 + 0.7 * (-0.55 * s + 0.75 * nz), 0.1, 1.3)
            rings = 0.85 + 0.15 * np.cos(TAU * yy / 11.0 + k)
            col = ramp(WALL, 0.18 + 0.55 * lam * rings + 0.1 * (n2 - 0.5))
            new = inside * (1 - alpha)
            rgb = rgb * (1 - new[..., None]) + col * new[..., None]
            core = np.exp(-(s / 0.32) ** 2) * inside * (0.65 + 0.35 * np.sin(TAU * yy / h * 3 + k))
            light += core[..., None] * VEIN_R * (0.9 - 0.15 * k)
            alpha = np.maximum(alpha, inside)
    alpha = alpha * edge_clear(h, W, low_seam_rows("vein-cables"), seed + 7)
    light = light * alpha[..., None]
    return rgba(rgb + light * VEIN_SHARE, alpha), light * (1 - VEIN_SHARE)


# =========================================================================== the Brood Halls' floor

PITS = 7


def floor_fields(seed=SEED + 130, h=TILE_H):
    """The living floor (float rgb, vein light): rounded lobes, creases, sealed pits, darker toward the hall's walls."""
    yy, xx = grid(h)
    wx, wy = pf(h, 48, 64, seed + 11, 3), pf(h, 48, 64, seed + 12, 3)
    f1, f2, idx = voronoi(h, W, 64, seed, wrap_y=True, warp=(30 * (wx - 0.5), 30 * (wy - 0.5)))
    n2 = pf(h, 24, 32, seed + 2)
    n3 = pf(h, 96, 128, seed + 3, 3)
    edge = f2 - f1
    lobe = np.clip(edge / 26.0, 0, 1) ** 0.6
    crease = np.exp(-(edge / 3.2) ** 2)
    hgt = 0.7 * lobe + 0.25 * n2
    rng = np.random.default_rng(seed + 9)
    pits = []
    while len(pits) < PITS:
        px, py, pr = rng.uniform(40, W - 40), rng.uniform(0, h), rng.uniform(15, 23)
        if all(np.hypot(px - qx, wrapped(py - qy, h)) > qr + pr + 70 for qx, qy, qr in pits):
            pits.append((px, py, pr))
    lip_all = np.zeros((h, W))
    hole = np.zeros((h, W))
    slit = np.zeros((h, W))
    for px, py, pr in pits:
        dy = wrapped(yy - py, h)
        d = np.hypot(xx - px, dy)
        ang = np.arctan2(dy, xx - px)
        lip = np.exp(-((d - pr) / (0.28 * pr)) ** 2)
        pucker = np.clip(1 - d / pr, 0, 1)
        hgt += 0.6 * lip - 0.8 * pucker ** 0.7 * (0.75 + 0.25 * np.cos(9 * ang))
        lip_all = np.maximum(lip_all, lip)
        hole = np.maximum(hole, pucker)
        sx = (xx - px) / pr
        line = np.abs(dy / pr - 0.18 * np.sin(sx * 2.6)) - 0.06 * (1 - sx * sx)
        slit = np.maximum(slit, np.exp(-(np.maximum(line, 0) / 0.05) ** 2) * (np.abs(sx) < 0.8) * (d < pr))
    sh = relief(hgt * 10, 1.0, wrap_y=True)
    v = 0.16 + 0.5 * lobe * (0.75 + 0.25 * n3) + 0.14 * (n2 - 0.5) + 0.2 * lip_all - 0.25 * hole
    rgb = ramp(WALL, v) * sh[..., None]
    rgb = rgb * (1 - 0.8 * slit[..., None])
    u = np.minimum(xx, W - xx) + 18 * (pf(h, 120, 128, seed + 5, 3) - 0.5)
    rgb = rgb * (0.5 + 0.5 * smoothstep(0, 80, u))[..., None]
    vein = crease * smoothstep(0.42, 0.62, n3) * 0.65
    return rgb, vein[..., None] * VEIN_R, pits


def brood_floor():
    rgb, light, _ = floor_fields()
    return rgba(rgb + light * VEIN_SHARE, np.ones((TILE_H, W))), light * (1 - VEIN_SHARE)


_FLOOR = {}


def floor_rows(rows):
    """The floor tile's pixels at ground rows (as BackdropTiles draws it: image row H-1 - row mod H)."""
    if not _FLOOR:
        _FLOOR["f"] = floor_fields()
    rgb, light, _ = _FLOOR["f"]
    i = TILE_H - 1 - (np.asarray(rows, int) % TILE_H)
    return rgb[i], light[i]


def floor_piece(lead):
    """The floor's lead (section 3's ground seam: the floor reaching raggedly below it) or its end (section 4's seam:
    the floor reaching raggedly above it). Rows [s - 128, s + 128), top row highest."""
    s = seam("ground", FLOOR_SECTION + (0 if lead else 1))
    rows = s + 127 - np.arange(256)
    rgb, light = floor_rows(rows)
    edge = (s - 66 if lead else s + 66) + ragged1(W, SEED + 140 + lead, 42)
    d = (rows[:, None] - edge[None, :]) * (1 if lead else -1)      # > 0 on the floor
    keep = smoothstep(-0.5, 0.8, d)
    fall = smoothstep(0, 40, d)
    rgb = rgb * (0.35 + 0.65 * fall)[..., None] + (np.exp(-((d - 3) / 2.4) ** 2) * keep)[..., None] \
        * np.array((44, 20, 52.0))
    light = light * (keep * (0.3 + 0.7 * fall))[..., None]
    return rgba(rgb + light * VEIN_SHARE, keep), light * (1 - VEIN_SHARE), s


# =========================================================================== the pulse light

def pulse_light(n=512):
    yy, xx = grid(n, n)
    d = np.hypot(xx - n / 2, yy - n / 2) / (n / 2)
    f = np.clip(1 - d * d, 0, 1) ** 3
    return f[..., None] * PULSE_COL


# The pulse per section (H4's screen-space light: the pulse-light texture centred at ``centre`` (screen px), scaled to
# ``reach`` px (its radius; the texture's is 256), alpha = ``peak`` x the pulse; ``[from, to]`` eases over the section).
PULSE = {0: None,
         1: {"centre": [240, -330], "reach": [380, 440], "peak": [0.2, 0.3]},
         2: {"centre": [240, -270], "reach": [460, 540], "peak": [0.32, 0.45]},
         3: {"centre": [240, -200], "reach": [560, 720], "peak": [0.4, 0.55]},
         4: {"centre": [240, 150], "reach": [640, 640], "peak": [0.35, 0.35]}}


def pulse_role(i):
    """A section's pulse (PULSE's key): the four chambers by order, a section between the Last Chamber and the arena
    (a braking stretch) the Last Chamber's end, the arena its own; none after it."""
    if i == I_ARENA:
        return 4
    if i > I_ARENA:
        return None
    return min(i, 3)


def pulse_of(i):
    role = pulse_role(i)
    p = PULSE.get(role) if role is not None else None
    if p is not None and role == 3 and i > 3:
        p = {"centre": p["centre"], "reach": [p["reach"][1]] * 2, "peak": [p["peak"][1]] * 2}
    return p


def pulse_at(t):
    """(centre, reach, peak) at level time t (review: each section's from -> to, 2 s eases between sections)."""
    i = 0
    for k in range(len(SECTIONS)):
        if t >= STARTS[k]:
            i = k
    p = pulse_of(i)
    if p is None:
        return None
    a, b = STARTS[i], float(SECTIONS[i]["end"])
    u = np.clip((t - a) / max(b - a, 1e-6), 0, 1)
    reach = p["reach"][0] + (p["reach"][1] - p["reach"][0]) * u
    peak = p["peak"][0] + (p["peak"][1] - p["peak"][0]) * u
    peak *= a84.smooth01((t - a) / 2.0) if i > 0 else 1.0
    return p["centre"], reach, peak


# =========================================================================== the scene kit

def arena_grey():
    """The deep field's rows round the halted arena (screen rows exactly), grey and still; ragged ends off the screen."""
    rgb, v, r0 = deep_field()
    a = int(round(D_HALT)) - 80
    rows = a + 699 - np.arange(700)
    src = rgb[rows - r0] + v[rows - r0] * 0.2
    tex = nf(W, 700, 30, SEED + 200, 3)
    grey = to_grey(src, 1.2, tex)
    yy, xx = grid(700)
    top = 80 + ragged1(W, SEED + 201, 34)[None, :] - 40
    bot = 620 + ragged1(W, SEED + 202, 34)[None, :] + 40
    al = smoothstep(-0.5, 0.8, yy - top) * smoothstep(-0.5, 0.8, bot - yy)
    al *= smoothstep(1.0, 3.0, yy) * smoothstep(1.0, 3.0, 700 - yy)
    return rgba(grey, al), a


def grey_mask():
    yy, xx = grid(SCREEN)
    d = np.hypot(xx - HEART[0], yy - HEART[1])
    dmax = np.hypot(240, SCREEN - HEART[1])
    n = nf(W, SCREEN, 60, SEED + 210, 4)
    r = np.clip(0.04 + 0.86 * d / dmax + 0.16 * (n - 0.5), 0, 1)
    g = np.zeros_like(r)
    return Image.fromarray(np.dstack([np.round(r * 255), g, g]).astype(np.uint8), "RGB")


RACE_N, RACE_H = 5, 1152
RACE_JOINS = [1150, 2300, 3450]                 # the strip's chambers: Last Chamber | Brood Halls | Choir Chambers | Throat


def race_strip():
    """The race's one grey strip (row 0 = its lowest, y-up), the four chambers in reverse order with ragged joins."""
    n = RACE_N * RACE_H
    rr = np.arange(n)[:, None] * np.ones((1, W))
    xx = np.ones((n, 1)) * (np.arange(W) + 0.5)[None, :]
    out = np.zeros((n, W, 3))
    bands = [(0, 1300), (1000, 2450), (2150, 3600), (3300, n)]
    joins = RACE_JOINS
    for k, (a, b) in enumerate(bands):
        x_, r_ = xx[a:b], rr[a:b]
        if k == 0:
            rgb, v = last_look(x_, r_, SEED + 300, n + 2600, scale=1.6)
        elif k == 1:
            rgb, v = floor_rows(np.arange(a, b) + 7000)
        elif k == 2:
            rgb, v = chamber_look(x_, r_, SEED + 320, scale=1.6)
        else:
            rgb, v = throat_look(x_, r_, SEED + 330, scale=1.6)
        tex = nf(W, b - a, 30, SEED + 340 + k, 3)
        grey = to_grey(rgb + v * 0.15, 1.15, tex)
        crease = np.clip(v.max(axis=-1) / 120.0, 0, 1)[..., None]
        grey = grey * (1 - 0.45 * crease)                             # the dead veins: dark creases
        if k == 0:
            out[a:b] = grey
            continue
        m = boundary(x_, r_, joins[k - 1], SEED + 350 + k, width=140)[..., None]
        out[a:b] = out[a:b] * (1 - m) + grey * m
    return out


def race_pieces():
    strip = race_strip()
    yy, _ = grid(220)
    lip = 120 + ragged1(W, SEED + 360, 40)[None, :]                    # race-1's lip: strip above it, nothing below
    pieces = {}
    for k in range(RACE_N):
        rows = strip[k * RACE_H:(k + 1) * RACE_H][::-1]                   # top row highest
        a = np.ones((RACE_H, W))
        rgb = rows.copy()
        if k == 0:
            keep = smoothstep(-0.5, 0.8, (lip - yy))
            fall = smoothstep(0, 36, (lip - yy))
            a[-220:] = keep
            rgb[-220:] = rgb[-220:] * (0.4 + 0.6 * fall)[..., None] \
                + (np.exp(-(((lip - yy) - 3) / 2.4) ** 2) * keep)[..., None] * np.array((40, 38, 46.0))
        pieces[f"race-{k + 1}"] = rgba(rgb, a)
    return pieces


def fold_frames(n=8, h=200):
    yy, xx = grid(h)
    out = []
    for f in range(n):
        ph = TAU * f / n
        lobes = 18 * np.sin(xx / 61.0 + ph) + 10 * np.sin(xx / 23.0 - 1.3 * ph + 1.1) + 6 * np.sin(xx / 9.7 + ph * 2)
        top = 64 + lobes + 14 * (nf(W, 8, 80, SEED + 400, 3)[4] - 0.5)
        d = yy - top
        a = smoothstep(-0.8, 0.8, d)
        crease = np.exp(-((np.sin((yy * 0.11 + 0.6 * np.sin(xx / 37.0 + ph)) * 1.0) * 1.0) ** 2) / 0.05)
        v = 0.25 + 0.45 * np.exp(-d / 40.0) - 0.15 * crease * smoothstep(0, 30, d)
        rgb = ramp(GREY, v) * (0.75 + 0.35 * np.exp(-((d - 4) / 6.0) ** 2))[..., None]
        out.append(rgba(rgb, a))
    return out


def plane_disc(n=512, r=244.0):
    yy, xx = grid(n, n)
    d = np.hypot(xx - n / 2, yy - n / 2)
    rgb = a84.plane_rgb(n, n, SEED + 410)
    rgb = displace(rgb, 7 * (nf(n, n, 48, SEED + 411, 3) - 0.5) * 2, 3 * (nf(n, n, 48, SEED + 412, 3) - 0.5) * 2)
    th = np.arctan2(yy - n / 2, xx - n / 2)
    edge = r + 1.8 * np.sin(7 * th + 0.4) + 1.1 * np.sin(13 * th + 1.7) + 0.7 * np.sin(29 * th)
    a = a84.smooth01((edge + 1.5 - d) / 2.5)                      # a crisp rim (a soft one reads as a flat band)
    return rgba(rgb, a)


def plane_rim(n=576, r=244.0):
    yy, xx = grid(n, n)
    d = np.hypot(xx - n / 2, yy - n / 2)
    g = np.exp(-((d - r) / 20.0) ** 2) * np.where(d > r, 1.0, 0.6) * (1 - smoothstep(r + 18, n / 2 - 2, d))
    return g[..., None] * EV * 0.6


def near_space(h=TILE_H):
    n = pf(h, 160, 256, SEED + 420, 4)
    n2 = pf(h, 96, 128, SEED + 421, 3)
    rgb = np.zeros((h, W, 3)) + np.array((3, 4, 9.0))
    rgb += np.clip((n - 0.5) * 1.6, 0, 1)[..., None] * np.array((16, 20, 34.0))
    rgb += np.clip((n2 - 0.6) * 2.0, 0, 1)[..., None] * np.array((10, 12, 20.0))
    rgb += mem.stars(h, W, SEED + 422, density=0.0011, periodic=True)
    return rgba(rgb, np.ones((h, W)))


def neptune():
    rgb, cov, nz, lam = mem.sphere(40, 40, 20, 20, 17.5, np.array((70, 120, 222.0)), ambient=0.08)
    yy, xx = grid(40, 40)
    band = 0.06 * np.sin((yy - 20) / 2.2)
    rgb = rgb * (1 + band)[..., None] + np.array((10, 20, 40.0)) * cov[..., None] * 0.4
    return rgba(rgb, cov)


def neptune_glow(n=72):
    yy, xx = grid(n, n)
    d = np.hypot(xx - n / 2, yy - n / 2)
    g = np.exp(-((d - 17.5) / 4.5) ** 2) * 0.55 * (1 - smoothstep(24, 34, d))
    g += np.exp(-(d / 22.0) ** 2) * 0.12 * (1 - smoothstep(26, 35, d))
    return g[..., None] * np.array((90, 150, 255.0))


RING_C, RING_R, RING_HALF, RING_H = (240.0, 390.0), 300.0, 56.0, 440


def glyph_order_of(ang):
    """The glyphs' turn to go dark (A.10 a's order: from the arc's top round both ways, a little scattered)."""
    order = (np.cos(ang - np.radians(-90)) + 1) / 2 * 0.8 + ((np.sin(ang * 7.3) + 1) / 2) * 0.2
    return np.clip(0.05 + 0.9 * (1 - order), 0.05, 0.95)


def ring_arc():
    seed = SEED + 430
    cx, cy = RING_C
    stone = np.zeros((RING_H, W, 3))
    a84.ring_paint(stone, cx, cy, RING_R, RING_HALF, seed, glyphs=0.0)
    lit = np.zeros((RING_H, W, 3))
    a84.ring_paint(lit, cx, cy, RING_R, RING_HALF, seed, glyphs=0.8)
    glyphs = np.clip(lit - stone, 0, None)
    yy, xx = grid(RING_H)
    r = np.hypot(xx - cx, yy - cy)
    band = np.clip(RING_HALF - np.abs(r - RING_R) + 0.5, 0, 1)
    ang = np.arctan2(yy - cy, xx - cx)
    ci = np.floor(ang * RING_R / 13.0)
    ang_c = (ci + 0.5) * 13.0 / RING_R
    cells, inv = np.unique(ci, return_inverse=True)                # the glyph cells round the arc, in their turn,
    lit = np.bincount(inv.ravel(), weights=glyphs.sum(axis=-1).ravel())   # cut in 8 groups of equal visible light
    seq = np.argsort(glyph_order_of((cells + 0.5) * 13.0 / RING_R), kind="stable")
    cum = np.empty(len(cells))
    cum[seq] = np.cumsum(lit[seq]) - lit[seq] / 2
    group = 1 + np.minimum((cum / max(lit.sum(), 1e-6) * 8).astype(int), 7)
    order = group[inv].reshape(ci.shape) / 8.0                       # group 1..8 (GLYPH_DARK's subject) / 8
    dx = 1.6 * (nf(W, RING_H, 40, seed + 5, 3) - 0.5) * 2 + 0.8 * np.sin(yy / 6.1)
    dy = 1.6 * (nf(W, RING_H, 40, seed + 6, 3) - 0.5) * 2 + 0.8 * np.sin(xx / 5.3)
    stone, band, glyphs = displace(stone, dx, dy), displace(band, dx, dy), displace(glyphs, dx, dy)
    order = displace(order * (band > 0), dx, dy, nearest=True)
    om = Image.fromarray(np.dstack([np.round(order * 255), np.zeros_like(order),
                                    np.zeros_like(order)]).astype(np.uint8), "RGB")
    return rgba(stone, band), glyphs, om


def displace(a, dx, dy, nearest=False):
    """a sampled at (x + dx, y + dy), bilinear (or nearest), clamped at the borders."""
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    x, y = np.clip(xx + dx, 0, w - 1), np.clip(yy + dy, 0, h - 1)
    if nearest:
        return a[np.round(y).astype(int), np.round(x).astype(int)]
    x0, y0 = np.floor(x).astype(int), np.floor(y).astype(int)
    x1, y1 = np.minimum(x0 + 1, w - 1), np.minimum(y0 + 1, h - 1)
    fx, fy = x - x0, y - y0
    if a.ndim == 3:
        fx, fy = fx[..., None], fy[..., None]
    return (a[y0, x0] * (1 - fx) * (1 - fy) + a[y0, x1] * fx * (1 - fy) + a[y1, x0] * (1 - fx) * fy
            + a[y1, x1] * fx * fy)


# =========================================================================== the seed vault

VAULT = 40


def vault_scene(state):
    """A sealed Vrell organ: a translucent-looking chitin dome (pale amber-violet, ribbed) packed with pale mint seeds
    showing through its windows; 1 cracked; 2 broken open (the halves split away, the hollow empty)."""
    mats = [Material((0.46, 0.34, 0.40), metal=0.05, shininess=90, spec=0.9),
            Material((0.62, 0.95, 0.82), metal=0.0, shininess=40, spec=0.4),
            Material((0.10, 0.05, 0.12), shininess=10, spec=0.1),
            Material((0.30, 0.18, 0.30), metal=0.05, shininess=60, spec=0.6)]
    windows = [(np.cos(a) * 0.32, np.sin(a) * 0.32, 0.36) for a in np.linspace(0, TAU, 6, endpoint=False) + 0.3]
    seeds = [(np.cos(a) * 0.26, np.sin(a) * 0.26, 0.24) for a in np.linspace(0, TAU, 6, endpoint=False) + 0.3] + \
        [(0.0, 0.0, 0.36)]

    def scene(p):
        dome = sd_ellipsoid(p, (0, 0, 0.02), (0.62, 0.62, 0.42))
        rim = sd_ellipsoid(p, (0, 0, -0.06), (0.74, 0.72, 0.16))
        ribs = None
        for a in np.linspace(0, TAU, 6, endpoint=False):
            c = sd_capsule(p, (np.cos(a) * 0.12, np.sin(a) * 0.12, 0.42), (np.cos(a) * 0.6, np.sin(a) * 0.6, 0.06),
                           0.045, 0.06)
            ribs = c if ribs is None else np.minimum(ribs, c)
        holes = None
        for (x, y, z) in windows:
            hd = sd_sphere(p, (x, y, z), 0.13)
            holes = hd if holes is None else np.minimum(holes, hd)
        sd_ = None
        for (x, y, z) in seeds:
            q = sd_sphere(p, (x, y, z), 0.1)
            sd_ = q if sd_ is None else np.minimum(sd_, q)
        if state == 0:
            shell = np.maximum(dome, -holes)
            items = [(shell, 0), (ribs, 3), (rim, 3), (sd_, 1)]
        elif state == 1:
            cracks = np.minimum(sd_capsule(p, (-0.55, 0.25, 0.4), (0.45, -0.15, 0.42), 0.045, 0.045),
                                sd_capsule(p, (0.05, -0.55, 0.4), (0.2, 0.45, 0.42), 0.04, 0.04))
            shell = np.maximum(np.maximum(dome, -holes), -cracks)
            items = [(shell, 0), (np.maximum(ribs, -cracks), 3), (rim, 3), (sd_, 1)]
        else:
            half_a = np.maximum(np.maximum(dome, p[:, 0] + 0.3), -sd_ellipsoid(p, (0, 0, 0.02), (0.52, 0.52, 0.32)))
            half_b = np.maximum(sd_ellipsoid(p, (0.22, 0.05, -0.02), (0.44, 0.5, 0.1)), -(p[:, 0] + 0.18))
            hollow = np.maximum(sd_ellipsoid(p, (0.05, 0, -0.04), (0.5, 0.5, 0.1)),
                                -sd_ellipsoid(p, (0.05, 0, 0.0), (0.42, 0.42, 0.1)))
            items = [(half_a, 0), (half_b, 3), (rim, 3), (hollow, 2)]
        return union(*items, k=0.02)
    return scene, mats


def _vault(state):
    scene, mats = vault_scene(state)
    return reef.render(scene, mats, (VAULT, VAULT), VAULT / 24.0, turn=False)


def build_vault():
    with ProcessPoolExecutor(max_workers=min(3, WORKERS)) as pool:
        res = list(pool.map(_vault, range(3)))
    bu.write("seed-vault", artkit.quantize_set(res, 40), SOURCE)
    chit = np.array((118, 88, 104.0))
    bu.write("seed-vault-break", reef.chips_frames(8, 64, [chit, chit * 0.7, np.array((158, 242, 210.0)),
                                                          np.array((76, 46, 76.0))], 5094, count=14,
                                                   speed=(30, 46), size=(1.8, 3.2)), SOURCE)


# =========================================================================== build

TILES = {"throat-ribs": ("low-air", throat_ribs), "singing-ribs": ("low-air", singing_ribs),
         "vein-cables": ("low-air", vein_cables), "brood-floor": ("ground", brood_floor)}
SCENE_GLOWS = {"plane-rim": plane_rim, "neptune-glow": neptune_glow, "pulse-light": pulse_light}
DATA_IMAGES = {"grey-mask", "glyph-order"}


def _tile(name):
    img, light = TILES[name][1]()
    return name, img, light


def _glow(name):
    return name, SCENE_GLOWS[name]()


def build_backdrop():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
    with ProcessPoolExecutor(max_workers=WORKERS) as pool:
        tiles = pool.map(_tile, list(TILES))
        glows = pool.map(_glow, list(SCENE_GLOWS))
        for name, img, light in tiles:
            save(name, img)
            save(name + ("-glow" if name == "singing-ribs" else "-veins"), additive_rgb(half_light(light)))
        for name, light in glows:
            save(name, additive_rgb(light))
    for name, img in deep_tiles().items():
        save(name, img)
    for lead, name in ((True, "floor-lead"), (False, "floor-end")):
        img, light, _ = floor_piece(lead)
        save(name, img)
        save(name + "-veins", additive_rgb(light))
    img, _ = arena_grey()
    save("arena-grey", img)
    save("grey-mask", grey_mask())
    for name, img in race_pieces().items():
        save(name, half_rgba(img))
    for i, f in enumerate(fold_frames()):
        save(f"fold_{i}", f)
    save("plane-disc", plane_disc())
    save("near-space", near_space())
    save("neptune", neptune())
    import backdrop_l40 as bd40  # noqa: E402  (Level 40's carrier group, unchanged, as Level 43's)
    artkit.REVIEW_ROUND = ROUND
    body, frames = bd40.fleet_far()
    save("steadfast-group", body)
    for i, f in enumerate(frames):
        save(f"steadfast-group-glow_{i}", f)
    stone, glyphs, order = ring_arc()
    save("ring-arc", stone)
    save("ring-arc-glyphs", additive_rgb(glyphs))
    save("glyph-order", order)
    print(f"backdrop: {len(list(OUT.glob('*.png')))} files in {OUT.relative_to(ROOT)}")


# =========================================================================== the proposal

def fmt(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, float):
        return f"{v:g}"
    if isinstance(v, str) and any(c in v for c in "':,"):
        return '"' + v + '"'
    return str(v)


def sizes():
    return {p.stem: Image.open(p).size for p in OUT.glob("*.png")}


def proposal_text():
    sz = sizes()
    src = "the level's data.yaml" if DATA and DATA.get("sections") else "this proposal's own sections"
    t_lead = CLOCK.t_for_layer_row(seam("ground", FLOOR_SECTION), 1.0)
    t_end = CLOCK.t_for_layer_row(seam("ground", FLOOR_SECTION + 1), 1.0)
    grey_row = int(round(D_HALT)) - 80
    t_grey = round(K, 3)
    race_k12 = round(CLOCK.scroll(T_RACE), 1)
    lines = [
        "backdrop:",
        f"  # Presentation only: Level 50's hive core (tools/art/act7_core.py, step ART-H2; sections from {src}).",
        "  # Vacuum: no haze. Deep: one continuous field cut at the sections' seams (re-run the generator after",
        "  # moving a section): the Throat's rib rings, the Choir Chambers' vaults, the Brood Halls' folds, the",
        "  # Last Chamber's veins converging on the Heart, the Heart's chamber (deep-5 onward, halted). Low-air:",
        "  # a set a section (1, 2, 4), each cleared along a ragged band where its section meets another. Ground:",
        "  # section 3's living floor, its lead and end pieces at the exact rows of its seams.",
        "  # H4 hooks: (1) VeinPulse extended to every layer of this level: each `<set>-veins` image and",
        "  # `singing-ribs-glow` (additive, not listed here) added with the pulse k(t) <= 1 Hz, in time with",
        "  # HEARTBEAT once the Heart lives; (2) the pulse light (`pulse` below): the additive `pulse-light`",
        "  # (512 px, radius 256) drawn screen-space, centred at `centre`, scaled to `reach` (radius px), alpha",
        "  # `peak` x the pulse, [from, to] eased over the section, 2 s eases between sections; none after K.",
        "  scroll_factors: {deep: 0.12, far: 0.35, ground: 1, low-air: 1.3, high-air: 2.0}   # far, high-air unused",
        "  ramp: 2",
        "  haze_colour: 1a0c22",
        "  atmosphere:",
        "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
        "  tile_sets:"]
    for i, name in enumerate(DEEP):
        lines.append(f"    {name}: {{layer: deep, height: {sz[name][1]}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    for name, (layer, _) in TILES.items():
        lines.append(f"    {name}: {{layer: {layer}, height: {sz[name][1]}}}")
    lines += ["  pieces:",
              f"    floor-lead: {{layer: ground, size: [{sz['floor-lead'][0]}, {sz['floor-lead'][1]}]}}",
              f"    floor-end: {{layer: ground, size: [{sz['floor-end'][0]}, {sz['floor-end'][1]}]}}",
              "  placed:",
              "    # the Brood Halls' floor reaching raggedly below section 3's ground seam (exact rows)",
              f"    - {{piece: floor-lead, t: {t_lead}, x: 240}}",
              "    # the floor ending raggedly above section 4's ground seam (exact rows); beyond it the deep",
              f"    - {{piece: floor-end, t: {t_end}, x: 240}}",
              "  pulse:   # H4: per section index (0-based), the pulse light's centre (screen px), reach and peak"]
    for i in range(min(len(SECTIONS), I_ARENA + 1)):
        p = pulse_of(i)
        if p is None:
            lines.append(f"    - {{section: {i}}}   # {SECTIONS[i]['name']}: none (lit by the units and the veins)")
        else:
            lines.append(f"    - {{section: {i}, centre: {fmt(p['centre'])}, reach: {fmt(p['reach'])}, "
                         f"peak: {fmt(p['peak'])}}}   # {SECTIONS[i]['name']}")
    lines.append(f"sections:   # {'the data' + chr(39) + 's' if DATA and DATA.get('sections') else 'proposed'} "
                 "(step H2 owns them), with this kit's tile sets per section")
    for i, sec in enumerate(SECTIONS):
        tl = [DEEP[i]] + ([LOW_SETS[i]] if i in LOW_SETS else []) + (["brood-floor"] if i == FLOOR_SECTION else [])
        sec = {**{k: v for k, v in sec.items() if k != "notes"}, "tiles": tl}
        lines.append("  - {" + ", ".join(f"{k}: {fmt(v)}" for k, v in sec.items()) + "}")
    lines += [
        "closing_gate:   # H4's ClosingGateLooks (not a data key): the scene's kit and numbers, times from the kill K",
        "  # From K+10.5 the scene draws over the section tiles (its layers change faster than a tile seam allows).",
        f"  arena_grey: {{image: arena-grey, deep_row: {grey_row}, at: {t_grey}}}   # its bottom row's deep row; "
        "centred on the halted screen",
        "  greying: {mask: grey-mask, from: 10.5, seconds: 3, band: 0.08}   # grey = smoothstep(R - band, R + band, "
        "p * 1.16 - 0.08)",
        "  heart_dead: {from: 10.5, seconds: 3}   # the Heart's collapse frames greyed the same way (ART-H's last frame)",
        f"  race: {{pieces: [race-1, race-2, race-3, race-4, race-5], height: {RACE_H}, scale: 2, from: 12}}   # stacked, "
        "race-1 lowest; strip row z at screen y = offset - z, offset = ground px since K+12"
        f" (ground {race_k12} at K+12 on the proposed clock)",
        "  folds: {frames: fold, fps: 8, from: 13, rise: 2, height: 40}   # anchored on the bottom edge; the "
        "doc's 160 px clearance from the ship at y 420 would keep it off the screen (H2/H4: 40 px as A.10 a)",
        "  plane_race: {image: plane-disc, rim: plane-rim, from: 10.5, radius: [40, 240], centre_y: [-20, 130], "
        "grow: 18.5, rim_contract: {from: 16, seconds: 13, to: 0.65}}",
        "  group_markers: {image: steadfast-group, scale: 0.35, into_plane: 20}   # the group's lights ahead, "
        "fading into the plane",
        "  warp: {from: 29, seconds: 1.5, reverse: true, strength: 0.6, centre: [240, 270]}   # TransitWarp reversed: the near side unbending from 0.6 while it cross-fades in (the review's luminance one way)",
        "  near_side: {from: 30.5, space: near-space, factor: 0.12, neptune: [380, 90], group: [240, 64]}",
        f"  ring: {{image: ring-arc, glyphs: ring-arc-glyphs, order: glyph-order, top_y: 110, "
        f"centre_in_image: [{int(RING_C[0])}, {int(RING_C[1])}], radius: {int(RING_R)}, mouth: "
        f"{int(RING_R - RING_HALF)}}}",
        "  plane_out: {image: plane-disc, from: 31, seconds: 4, fade_last: 0.6}   # in the ring's mouth, shrinking "
        "to a point and fading (no flash)",
        "  glyphs_dark: {events: glyph-dark, groups: 8, fade: 0.3}   # GLYPH_DARK n darkens the glyphs whose "
        "glyph-order R x 8 rounds to n (from the arc's top round both ways), each over 0.3 s",
    ]
    return "\n".join(lines) + "\n"


def write_proposal():
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


# =========================================================================== composition (the review)

_CACHE = {}


def img(name):
    if name not in _CACHE:
        p = OUT / f"{name}.png"
        a = np.array(Image.open(p).convert("RGBA")).astype(np.float64) if p.exists() else None
        _CACHE[name] = double(a) if a is not None and halved(name) else a
    return _CACHE[name]


# Step H4 (the atlas budget: Level 50 at 9 pages): the tile sets' pulsing light (``<set>-veins``,
# ``singing-ribs-glow``) and the race's grey strip are stored at half size (2x2 means, premultiplied) and drawn at 2x
# with linear filtering by the game (VeinPulse, ClosingGateLooks): smooth light and a strip at 300 px/s lose nothing
# at the screen's scale. The tiles themselves, the ground pieces' veins and everything else stay 1:1.
HALF_NAMES = {f"race-{k + 1}" for k in range(5)}


# The near side's sparse deep veins stay 1:1: at half size their few thin lines read as a 1 px checker (DitherGuardTest).
FULL_VEINS = {"deep-8"}


def halved(name):
    """Whether ``name`` is stored at half size (the game draws it at 2x)."""
    tiled = name.endswith("-veins") and not name.startswith(("floor-lead", "floor-end") + tuple(FULL_VEINS))
    return tiled or name == "singing-ribs-glow" or name in HALF_NAMES


def half_light(light):
    """A float light field (h, w, 3) at half size: the means of 2x2 blocks (h and w even)."""
    h, w = light.shape[:2]
    return light.reshape(h // 2, 2, w // 2, 2, -1).mean(axis=(1, 3))


def half_rgba(im):
    """An RGBA image at half size: premultiplied 2x2 means."""
    a = np.array(im.convert("RGBA")).astype(np.float64)
    h, w = a.shape[:2]
    al = a[..., 3:4] / 255.0
    pm = np.concatenate([a[..., :3] * al, al], axis=-1).reshape(h // 2, 2, w // 2, 2, 4).mean(axis=(1, 3))
    out = np.zeros_like(pm)
    out[..., 3] = pm[..., 3] * 255.0
    out[..., :3] = np.where(pm[..., 3:4] > 0, pm[..., :3] / np.maximum(pm[..., 3:4], 1e-9), 0)
    return Image.fromarray(np.clip(np.round(out), 0, 255).astype(np.uint8), "RGBA")


def double(a):
    """A half-size image back at 1:1 for the reviews (repeated pixels, as the game's 2x before its filtering)."""
    return np.repeat(np.repeat(a, 2, axis=0), 2, axis=1)


def tile_rows(name, rows):
    arr = img(name)
    return arr[len(arr) - 1 - (np.asarray(rows, int) % len(arr))]


def section_of(layer, row):
    i = 0
    for k in range(1, len(SECTIONS)):
        if row >= seam(layer, k):
            i = k
    return i


def put(base, arr, x, y, k=1.0, add=False):
    """Draw arr (h, w, 4) centred at (x, y) onto base (rgb float), normal or additive."""
    if arr is None:
        return base
    h, w = arr.shape[:2]
    x0, y0 = int(round(x - w / 2)), int(round(y - h / 2))
    x1, y1, x2, y2 = max(0, x0), max(0, y0), min(base.shape[1], x0 + w), min(base.shape[0], y0 + h)
    if x2 <= x1 or y2 <= y1:
        return base
    src = arr[y1 - y0:y2 - y0, x1 - x0:x2 - x0]
    if add:
        base[y1:y2, x1:x2] += src[..., :3] * k
    else:
        a = src[..., 3:4] / 255.0 * k
        base[y1:y2, x1:x2] = base[y1:y2, x1:x2] * (1 - a) + src[..., :3] * a
    return base


def scaled(arr, s):
    h, w = arr.shape[:2]
    nw, nh = max(1, int(round(w * s))), max(1, int(round(h * s)))
    im = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGBA").resize((nw, nh), Image.LANCZOS)
    return np.array(im).astype(np.float64)


def layer_rows(scroll, factor):
    return np.round(scroll * factor).astype(int) + SCREEN - 1 - np.arange(SCREEN)


def pulse_wave(t):
    return 0.5 - 0.5 * np.cos(TAU * t / 3.0)


def draw_tiles(rgb, name_of_row, rows, k_veins, veins=True):
    """Rows by row: the set each row belongs to (None: nothing), its veins added with k."""
    names = [name_of_row(r) for r in rows]
    for nm in set(n for n in names if n):
        sel = np.array([n == nm for n in names])
        t_ = tile_rows(nm, rows[sel])
        a = t_[..., 3:4] / 255.0
        rgb[sel] = rgb[sel] * (1 - a) + t_[..., :3] * a
        if veins:
            vn = nm + ("-glow" if nm == "singing-ribs" else "-veins")
            if img(vn) is not None:
                rgb[sel] += tile_rows(vn, rows[sel])[..., :3] * k_veins
    return rgb


def compose(t, units=True, heart=True, pulse=None):
    s = CLOCK.scroll(t)
    pw = pulse_wave(t) if pulse is None else pulse
    kv = 0.35 + 0.65 * pw
    rgb = np.zeros((SCREEN, W, 3))
    rows = layer_rows(s, FACTORS["deep"])
    rgb = draw_tiles(rgb, lambda r: DEEP[section_of("deep", r)], rows, kv)
    grows = layer_rows(s, 1.0)
    s3, s4 = seam("ground", FLOOR_SECTION), seam("ground", FLOOR_SECTION + 1)
    rgb = draw_tiles(rgb, lambda r: "brood-floor" if s3 <= r < s4 else None, grows, kv)
    for name, sp in (("floor-lead", s3), ("floor-end", s4)):
        y = s + SCREEN - sp                                           # the piece's centre row sp on the screen
        put(rgb, img(name), 240, y)
        put(rgb, img(name + "-veins"), 240, y, kv, add=True)
    lrows = layer_rows(s, FACTORS["low-air"])
    rgb = draw_tiles(rgb, lambda r: LOW_SETS.get(section_of("low-air", r)), lrows, kv)
    p = pulse_at(t)
    if p is not None:
        (cx, cy), reach, peak = p
        put(rgb, scaled(img("pulse-light"), reach / 256.0), cx, cy, peak * pw, add=True)
    out = Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    if heart and t >= K - 6:
        out = draw_heart(out, t, pw)
    if units:
        out = stand_ins(out, t)
    return out


def held_frame(name, i=None):
    try:
        frames = l23kit.load(name)
    except FileNotFoundError:
        return None
    if not frames:
        return None
    return frames[0 if i is None else min(i, len(frames) - 1)]


def draw_heart(out, t, pw):
    """The Heart in phase 1 (ART-H's held frames, its review's own drawing), slid in with the scroll's end."""
    import choir_heart as ch  # noqa: E402  (lazy: choir_heart's review imports this module for arena_bg)
    artkit.REVIEW_ROUND = ROUND
    dy = -(CLOCK.scroll(K) - CLOCK.scroll(t))
    try:
        return ch.heart(out, 1, pw, dy=int(round(dy)))
    except (FileNotFoundError, IndexError):
        return out


UNITS = {0: [("skitter", 0), ("needler", 0), ("stinger", 0), ("whirl-seed", 0), ("mote-swarm", 0)],
         1: [("choir-herald", 0), ("choir-seraph", 0), ("rift-skater", 0), ("lamprey", 0), ("wraith", 0)],
         2: [("chimera", 0), ("harrow", 0), ("spine-turret", 0), ("polyp-mortar", 0)],
         3: [("gate-warden", 0), ("chimera", 0), ("lamprey", 0), ("harrow", 0), ("choir-seraph", 0)]}


def sprite(name):
    p = ROOT / "assets" / "sprites" / f"{name}.png"
    return Image.open(p).convert("RGBA") if p.exists() else None


def stand_ins(out, t):
    i = 0
    for k in range(len(SECTIONS)):
        if t >= STARTS[k]:
            i = k
    for k, (name, f) in enumerate(UNITS.get(i, [])):
        sp = sprite(f"{name}_{f}")
        if sp:
            l23kit.paste_c(out, sp, 90 + k * 76, 150 + (k % 2) * 70)
    if i == FLOOR_SECTION:
        v = held_frame("seed-vault")
        vy = SCREEN / 2 + (CLOCK.scroll(t) - CLOCK.scroll(118.0))
        if v is not None and -30 < vy < SCREEN + 30:
            l23kit.paste_c(out, v, 240, vy)
    for k in range(7):
        a84.vorb(out, 150 + k * 32, 300 + (k % 3) * 18, 4, (170, 110, 255) if k % 2 else (90, 240, 200))
    if i >= I_ARENA:
        a84.ring_shots(out, 240, 150, 12, 120 + 30 * ((t * 0.7) % 1), col=(170, 110, 255))
    for name, x, y in (("ship_2", 240, 470), ("rook_2", 290, 488)):
        sp = sprite(name)
        if sp:
            l23kit.paste_c(out, sp, x, y)
    return out


def arena_bg(pulse=0.6):
    """The Heart's arena at the halt (the deep layer, its veins with the pulse, the pulse light), 480x540 RGBA: the
    background tools/art/choir_heart.py's review draws its frames over (step ART-H's stand-in replaced)."""
    key = ("arena", round(float(pulse), 3))
    if key not in _CACHE:
        _CACHE[key] = compose(K, units=False, heart=False, pulse=pulse)
    return _CACHE[key].copy()


SCREENS = (8.0, 34.0, 58.0, 88.0, 99.0, 118.0, 134.0, 150.0, 178.0, K)
LABELS = ("THE THROAT", "THE THROAT, RIBBED WALLS", "THE CHOIR CHAMBERS", "THE SINGING RIBS", "THE FLOOR'S LIP",
          "THE BROOD HALLS, THE VAULT", "THE FLOOR ENDS", "THE LAST CHAMBER", "THE VEINS CONVERGE", "THE HEART'S ARENA")


def lum(im):
    a = np.array(im.convert("RGB")).astype(float)
    return float((a @ np.array((0.299, 0.587, 0.114))).mean())


def review_backdrop():
    _CACHE.clear()
    sw, sh = 192, 216
    w = 1000
    sheet = raster.sheet(w, 40 + 2 * (sh + 18) + 20 + 330, "LEVEL 50 BACKDROP (FINAL R95): THE HIVE CORE",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    for i, t in enumerate(SCREENS):
        x, y = 12 + (i % 5) * (sw + 6), 38 + (i // 5) * (sh + 18)
        raster.draw_text(sheet, x, y, f"T={t:g} {LABELS[i]}", raster.LABEL)
        sheet.alpha_composite(compose(t, pulse=1.0).resize((sw, sh), Image.LANCZOS), (x, y + 11))
    y = 38 + 2 * (sh + 18) + 6
    raster.draw_text(sheet, 12, y, "THE SETS (TOP 256 ROWS OVER THE DEEP, VEINS AT THE PULSE'S PEAK), THE PULSE LIGHT "
                                   "AND THE FLOOR'S LEAD AND END (1/3)", raster.LABEL)
    x = 12
    for name in DEEP[:4] + [DEEP[I_ARENA]] + list(TILES):
        n = min(256, len(img(name)))
        arr = img(name)[:n].copy()
        base = img("deep-1")[:n, ..., :3] if name in TILES else np.zeros((n, W, 3))
        a = arr[..., 3:4] / 255.0
        comp = base * (1 - a) + arr[..., :3] * a
        vn = name + ("-glow" if name == "singing-ribs" else "-veins")
        if img(vn) is not None:
            comp = comp + img(vn)[:n, ..., :3]
        im = Image.fromarray(np.clip(comp, 0, 255).astype(np.uint8), "RGB").resize((96, 80), Image.LANCZOS)
        sheet.paste(im, (x, y + 14))
        raster.draw_text(sheet, x, y + 98, name.upper()[:13], raster.LABEL_DIM)
        x += 102
    x, yy = 12, y + 112
    for name in ("pulse-light", "floor-lead", "floor-end"):
        im = Image.open(OUT / f"{name}.png").convert("RGBA")
        bg = Image.new("RGBA", im.size, (20, 10, 26, 255))
        bg.alpha_composite(im)
        sm = bg.resize((im.width // 3, im.height // 3), Image.LANCZOS)
        sheet.alpha_composite(sm, (x, yy))
        x += sm.width + 8
    vault = [held_frame("seed-vault", i) for i in range(3)]
    for i, v in enumerate(vault):
        if v is not None:
            sheet.alpha_composite(v.resize((80, 80), Image.NEAREST), (x + i * 88, yy))
    raster.draw_text(sheet, x, yy + 86, "SEED VAULT 0-2 (2X)", raster.LABEL_DIM)
    raster.draw_text(sheet, 12, yy + 200, "PULSE: SECTION 1 NONE (UNITS AND VEINS ONLY), REACHING FURTHER EACH CHAMBER; "
                                           "SCREENS AT THE PULSE'S PEAK. MEAN LUMINANCE:", raster.LABEL_DIM)
    raster.draw_text(sheet, 12, yy + 212, "  ".join(f"T{t:g} {lum(compose(t, units=False, heart=False, pulse=1.0)):.0f}"
                                                    for t in SCREENS), raster.LABEL_DIM)
    frames = [compose(float(t)).resize((240, 270), Image.LANCZOS) for t in np.arange(0.0, 212.0, 2.0)]
    artkit.save_review(sheet, frames, CONCEPT, "backdrop", fps=8)


# --------------------------------------------------------------------------- the closing gate (review emulation)

def scene_frame(tk):
    """The closing gate at K + tk (s) as ClosingGateLooks would draw it from the kit (the warp emulated)."""
    if tk < 29.0:
        rgb = scene_far(tk)
    elif tk < 30.5:
        u = a84.smooth01((tk - 29.0) / 1.5)                       # Level 43's warp reversed: the near side
        rgb = scene_far(29.0) * (1 - u) + a84.warp_img(scene_near(30.5), 0.6 * (1 - u), cy=270.0) * u   # unbends
    else:
        rgb = scene_near(tk)
    out = Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    pos = ((240, 420), (286, 438)) if tk >= 13.5 else ((240 - 20 * (13.5 - tk) / 3, 420 + 40 * (13.5 - tk) / 3),
                                                      (286 - 16 * (13.5 - tk) / 3, 438 + 42 * (13.5 - tk) / 3))
    for name, (x, y) in zip(("ship_2", "rook_2"), pos):
        sp = sprite(name)
        if sp:
            l23kit.paste_c(out, sp, x, y)
    if 10.5 <= tk < 13.5:
        txt = "THE GATE IS CLOSING"
        raster.draw_text(out, 240 - raster.text_width(txt, 2) / 2, 220, txt, (226, 214, 236), 2)
    return out


def ground_since(tk):
    return max(0.0, CLOCK.scroll(scene_clock(tk)) - CLOCK.scroll(T_RACE))


def scene_far(tk):
    rgb = np.array(arena_bg(0.0))[..., :3].astype(np.float64)
    g = np.clip((tk - 10.5) / 3.0, 0, 1)
    if g > 0:
        mk = np.array(Image.open(OUT / "grey-mask.png")).astype(float)[..., 0] / 255.0
        vis = smoothstep(mk - 0.08, mk + 0.08, g * 1.16 - 0.08)[..., None]
        gr = img("arena-grey")
        top = SCREEN - (gr.shape[0] - 80)
        dy = (CLOCK.scroll(scene_clock(tk)) - CLOCK.scroll(K)) * FACTORS["deep"]
        canvas = np.zeros((SCREEN, W, 4))
        y0 = int(round(top + dy))
        a0, b0 = max(0, y0), min(SCREEN, y0 + gr.shape[0])
        canvas[a0:b0] = gr[a0 - y0:b0 - y0]
        a = canvas[..., 3:4] / 255.0 * vis
        rgb = rgb * (1 - a) + canvas[..., :3] * a
    off = ground_since(tk)
    if tk >= 12.0:
        strip = [img(f"race-{k + 1}") for k in range(RACE_N)]
        z = off - np.arange(SCREEN)                                   # strip row at each screen row (y down)
        canvas = np.zeros((SCREEN, W, 4))
        for y in range(SCREEN):
            zz = int(np.floor(z[y]))
            if 0 <= zz < RACE_N * RACE_H:
                k, j = divmod(zz, RACE_H)
                canvas[y] = strip[k][RACE_H - 1 - j]
        a = canvas[..., 3:4] / 255.0
        rgb = rgb * (1 - a) + canvas[..., :3] * a
    k_ = a84.smooth01((tk - 10.5) / 18.5)
    rad = 40 + 200 * k_
    rim = rad * (1 - 0.35 * a84.smooth01((tk - 16) / 13.0))
    cy = -20 + 150 * k_
    if tk >= 10.5:
        if tk < 20.5:                                                 # the group's lights ahead, into the plane
            gp = scaled(img("steadfast-group"), 0.35)
            fade = 1 - a84.smooth01((tk - 17.5) / 3.0)
            put(rgb, gp, 240 + 30 * np.sin(tk * 0.3), cy + rim * 1.15 + 30 * (1 - k_), fade)
        put(rgb, scaled(img("plane-disc"), rim / 244.0), 240, cy, min(1.0, (tk - 10.5) / 1.0))
        put(rgb, scaled(img("plane-rim"), rim / 244.0), 240, cy, min(1.0, (tk - 10.5) / 1.0), add=True)
    if tk >= 13.0:
        rise = 40 * a84.smooth01((tk - 13.0) / 2.0)
        fr = img(f"fold_{int(tk * 8) % 8}")
        put(rgb, fr, 240, SCREEN + 100 - rise)
    return rgb


def scene_near(tk):
    s0 = CLOCK.scroll(scene_clock(30.5))
    dy = (CLOCK.scroll(scene_clock(tk)) - s0) * FACTORS["deep"]
    rows = layer_rows(CLOCK.scroll(scene_clock(tk)), FACTORS["deep"])
    rgb = tile_rows("near-space", rows)[..., :3].copy()
    put(rgb, img("neptune-glow"), 380, 90 + dy * 0.3, add=True)
    put(rgb, img("neptune"), 380, 90 + dy * 0.3)
    cy_ring = 110 + RING_H / 2 + dy
    shut = a84.smooth01((tk - 31.0) / 4.0)
    rad = (RING_R - RING_HALF) * (1 - shut)
    if rad > 0.6:
        fade = 1 - a84.smooth01((tk - 34.4) / 0.6)
        put(rgb, scaled(img("plane-disc"), rad / 244.0), RING_C[0], 110 + RING_C[1] + dy, fade)
        put(rgb, scaled(img("plane-rim"), rad / 244.0), RING_C[0], 110 + RING_C[1] + dy, 0.6 * fade, add=True)
    put(rgb, img("ring-arc"), 240, cy_ring)
    gl = img("ring-arc-glyphs").copy()
    group = np.round(img("glyph-order")[..., 0] / 255.0 * 8)
    out_at = 35.0 + 0.4 * (group - 1)                                # GLYPH_DARK n at K+35 + 0.4 (n - 1)
    gl[..., :3] *= (1 - np.clip((tk - out_at) / 0.3, 0, 1))[..., None]
    put(rgb, gl, 240, cy_ring, add=True)
    gp = img("steadfast-group")
    put(rgb, gp, 240, 64 - dy * 2)
    gi = img(f"steadfast-group-glow_{int(tk * 4) % 8}")
    put(rgb, gi, 240, 64 - dy * 2, add=True)
    return rgb


def race_label(tk):
    z = ground_since(tk) - SCREEN / 2
    names = ("THE LAST CHAMBER", "THE BROOD HALLS", "THE CHOIR CHAMBERS", "THE THROAT")
    return names[sum(z >= j for j in RACE_JOINS)]


SCENE_KEYS = [(10.6, "K+10.5 THE HIVE GOES STILL; THE PLANE'S LIGHT AHEAD"), (12.0, "K+12 GREY FROM THE HEART OUT"),
              (14.5, "K+14.5 THE RACE: " + race_label(14.5)), (19.0, "K+19 " + race_label(19.0)),
              (23.0, "K+23 " + race_label(23.0)), (27.5, "K+27.5 " + race_label(27.5) + ", THE PLANE NEAR"),
              (29.8, "K+29.8 THE WARP REVERSED"), (30.6, "K+30.5 THE NEAR SIDE"), (33.0, "K+33 THE PLANE SHRINKING"),
              (36.5, "K+36.5 THE GLYPHS GOING DARK"), (39.0, "K+39 THE GATE IS DARK")]


def review_closing():
    _CACHE.clear()
    sw, sh = 192, 216
    w = 1000
    rows_ = -(-len(SCENE_KEYS) // 5)
    ts = list(np.arange(10.0, 29.0, 0.25)) + list(np.arange(29.0, 30.5, 0.1)) + list(np.arange(30.5, 40.25, 0.25))
    frames = [scene_frame(t) for t in ts]
    lums = [lum(f) for f in frames]
    plot = a84.lum_plot(lums, width=968, marks=[(0, "K+10"), (ts.index(29.0), "WARP"), (len(ts) - 1, "K+40")])
    sheet = raster.sheet(w, 40 + rows_ * (sh + 18) + 30 + plot.height + 60,
                         "LEVEL 50 CLOSING GATE (FINAL R95): THE SCENE", f"PRODUCTION ART, {BATCH.upper()} - "
                         f"{ROUND.upper()}")
    for i, (t, lab) in enumerate(SCENE_KEYS):
        x, y = 12 + (i % 5) * (sw + 6), 38 + (i // 5) * (sh + 18)
        raster.draw_text(sheet, x, y, lab[:34], raster.LABEL)
        sheet.alpha_composite(scene_frame(t).resize((sw, sh), Image.LANCZOS), (x, y + 11))
    x0 = 12 + (len(SCENE_KEYS) % 5) * (sw + 6)
    y0 = 38 + (len(SCENE_KEYS) // 5) * (sh + 18) + 11
    for k, name in enumerate(("race-1", "race-3", "fold_0", "plane-disc", "ring-arc")):
        im = Image.open(OUT / f"{name}.png").convert("RGBA")
        bg = Image.new("RGBA", im.size, (20, 18, 24, 255))
        bg.alpha_composite(im)
        s = 60.0 / im.height if name != "ring-arc" else 50.0 / im.height
        sm = bg.resize((max(1, int(im.width * s)), max(1, int(im.height * s))), Image.LANCZOS)
        sheet.alpha_composite(sm, (x0 + (k % 2) * 100, y0 + (k // 2) * 66))
    y = 38 + rows_ * (sh + 18) + 6
    raster.draw_text(sheet, 12, y, "MEAN LUMINANCE PER FRAME, K+10 TO K+40 (ONE WAY THROUGH THE WARP, NO FLASH, NO "
                                   "INVERSION)", raster.LABEL)
    sheet.alpha_composite(plot, (12, y + 14))
    warp = [lums[i] for i, t in enumerate(ts) if 29.0 <= t <= 30.6]
    mono = all(b <= a + 0.05 for a, b in zip(warp, warp[1:])) or all(b >= a - 0.05 for a, b in zip(warp, warp[1:]))
    print(f"warp luminance {warp[0]:.1f} -> {warp[-1]:.1f}, one way: {mono}; brightest {max(lums):.1f}")
    raster.draw_text(sheet, 12, y + 20 + plot.height,
                     f"WARP K+29..K+30.5: LUMINANCE {warp[0]:.1f} -> {warp[-1]:.1f}, ONE WAY: {'YES' if mono else 'NO'};"
                     f" BRIGHTEST FRAME {max(lums):.1f} (K+{ts[int(np.argmax(lums))]:.1f})", raster.LABEL_DIM)
    gif = [f.resize((240, 270), Image.LANCZOS) for f in frames[::2]]
    artkit.save_review(sheet, gif, CONCEPT, "closing-gate", fps=8)
    return mono


def review_vault():
    _CACHE.clear()
    vault = [held_frame("seed-vault", i) for i in range(3)]
    brk = l23kit.load("seed-vault-break")
    sheet = raster.sheet(1000, 420, "SEED VAULT (FINAL R95): LEVEL 50'S SECRET",
                         f"PRODUCTION ART, {BATCH.upper()} - {ROUND.upper()}")
    raster.draw_text(sheet, 12, 38, "WHOLE, CRACKED, BROKEN OPEN (4X); THE BREAK (2X); IN PLACE ON THE BROOD FLOOR AT 1X",
                     raster.LABEL)
    for i, v in enumerate(vault):
        sheet.alpha_composite(v.resize((160, 160), Image.NEAREST), (12 + i * 170, 56))
    for i, f in enumerate(brk):
        sheet.alpha_composite(f.resize((128, 128), Image.NEAREST), (12 + (i % 4) * 134, 226 + (i // 4) * 0))
        if i >= 3:
            break
    floor = np.array(Image.open(OUT / "brood-floor.png").convert("RGB"))[300:600, 120:420]
    place = Image.fromarray(floor).convert("RGBA")
    l23kit.paste_c(place, vault[0], 150, 150)
    for x in (60, 240):
        sp = sprite("polyp-mortar_0")
        if sp:
            l23kit.paste_c(place, sp, x, 150)
    sheet.alpha_composite(place.resize((300, 300)), (560, 56))
    gif = []
    for f in range(24):
        im = place.copy()
        if f >= 8:
            im = Image.fromarray(floor).convert("RGBA")
            l23kit.paste_c(im, vault[2], 150, 150)
            if f - 8 < len(brk):
                l23kit.paste_c(im, brk[f - 8], 150, 150)
        elif f >= 4:
            im = Image.fromarray(floor).convert("RGBA")
            l23kit.paste_c(im, vault[1], 150, 150)
        gif.append(im)
    artkit.save_review(sheet, gif, CONCEPT, "seed-vault", fps=8)


# =========================================================================== check

def check():
    bad = 0
    worst = (0, "")
    sil = (0, "")
    tiles = set(TILES) | set(DEEP) | {"near-space"}
    glows = {p.stem for p in OUT.glob("*.png") if p.stem.endswith(("-veins", "-glow")) or p.stem.startswith(
        "steadfast-group-glow")} | set(SCENE_GLOWS) | {"ring-arc-glyphs"}
    anchored = {"ring-arc", "floor-lead", "floor-end"} | {f"race-{k + 1}" for k in range(RACE_N)} | \
        {f"fold_{i}" for i in range(8)} | {"arena-grey"}
    for p in sorted(OUT.glob("*.png")):
        im = Image.open(p)
        arr = np.array(im.convert("RGBA"))
        v, c = checker_share(im)
        share = c / max(v, 1)
        stem = p.stem
        ok, msg = im.text.get(artkit.SOURCE_KEY) == SOURCE and share < 0.05, []
        base = stem[:-6] if stem.endswith("-veins") else stem[:-5] if stem.endswith("-glow") else stem
        if stem in tiles or base in tiles:
            ws = max(wrap_score(arr), wrap_score_alpha(arr)) if arr[..., 3].min() < 255 else wrap_score(arr)
            ok &= ws <= 1.5
            msg.append(f"y wrap {ws:.2f}")
        if stem in DATA_IMAGES:
            pass
        elif stem in glows:
            mx = arr[..., :3].max(axis=-1)
            levels = len(np.unique(mx))
            msg.append(f"{levels} light levels (max {mx.max()})")
            ok &= levels >= min(32, int(mx.max()) * 0.9) or stem.startswith("steadfast-group-glow")
            if base not in tiles and stem not in ("ring-arc-glyphs",) and base not in ("floor-lead", "floor-end"):
                lit = max(arr[0, :, :3].max(), arr[-1, :, :3].max(), arr[:, 0, :3].max(), arr[:, -1, :3].max())
                ok &= lit == 0
                msg.append(f"edge {lit}")
        else:
            if stem not in tiles and stem not in anchored and not stem.startswith("steadfast-group"):
                edge = max(arr[0, :, 3].max(), arr[-1, :, 3].max(), arr[:, 0, 3].max(), arr[:, -1, 3].max())
                ok &= edge == 0
                msg.append(f"edge alpha {edge}")
            rs, _, cs, _ = cit.straight_scan(im, wrap=stem in tiles)
            run = max(rs, cs)
            lim = 60 if (stem in tiles or stem in anchored) else 120
            ok &= run < lim
            worst = max(worst, (run, stem))
            sr, sc = sph.silhouette_runs(im)
            srun = max(sr, sc)
            ok &= srun < 40
            sil = max(sil, (srun, stem))
            msg.append(f"straight {run} px (limit {lim}), silhouette {srun} px")
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {p.relative_to(ROOT)}: {im.size}, checker {share * 100:.1f} %, "
              f"{', '.join(msg)}")
    for name, n, size in (("seed-vault", 3, (VAULT, VAULT)), ("seed-vault-break", 8, (64, 64))):
        try:
            frames = l23kit.load(name)
        except FileNotFoundError:
            frames = []
        where = bu.where(name)
        files = [f for f in (ROOT / where).glob(f"{name}_*.png") if f.stem.rsplit("_", 1)[0] == name]
        src = bool(files) and all(Image.open(f).text.get(artkit.SOURCE_KEY) == SOURCE for f in files)
        run = max((max(cit.straight_scan(fr)[0], cit.straight_scan(fr)[2]) for fr in frames), default=0)
        srun = max((max(sph.silhouette_runs(fr)) for fr in frames), default=0)
        ok = len(frames) == n and all(fr.size == size for fr in frames) and src and run < 30 and srun < 40
        bad += not ok
        print(f"{'ok ' if ok else 'BAD'} {where}/{name}: {len(frames)}/{n} frames, straight {run} px, "
              f"silhouette {srun} px")
    print(f"straight-line scan: longest {worst[0]} px ({worst[1]}); alpha silhouette: longest {sil[0]} px ({sil[1]})")
    return bad == 0


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    subjects = [a for a in args if not a.startswith("--")] or ["backdrop", "vault"]
    if "--proposal" in args:
        write_proposal()
        sys.exit(0)
    if "--review" not in args:
        if "vault" in subjects:
            build_vault()
        if "backdrop" in subjects:
            build_backdrop()
            write_proposal()
    if "backdrop" in subjects:
        review_backdrop()
        review_closing()
    if "vault" in subjects:
        review_vault()
