#!/usr/bin/env python3
"""Production art: the Level 40 backdrop, Honour Guard (design/campaign/act-6-jovian-storm/level-40-honour-guard; M10
part F batch, concept round 78, step ART-F2): the Callisto approach in vacuum.

Straight to production from round 72's chosen look: Q10 a's **L40 grade** (tools/concept/act6_r72.py ``space_l40``:
black space and stars, Callisto's dark blue-grey ice, Jupiter's cream-orange limb at an edge, the fleet's lights) laid
out as the level's Layout has it: Jupiter's limb and night side filling the bottom right in section 1 and sinking out
over the right edge in section 2, a picket station far off, Callisto rising at the top from section 3 and filling the
screen by section 6, three gold glints far ahead in section 4 (they climb away out of the top edge just before the
sting: the aces leaving to make their entries), Callisto's orbital ring crossing below in section 6 with the fleet
closing behind, the **ice line** (Callisto's terminator: the sunlit ice below it, the night side above) entering at the
top as section 7 begins and dividing the screen by t~152, the black citadel's red and gold lights at the top edge by
t~165. Every haze, limb and glow is a float field written unquantised (far more than 32 steps, no ordered dither: the
2026-10-30 dither audit, DitherGuardTest's measure); every glow is additive (premultiplied on black) and drawn once.

**The motion budget** (the README: "the waves are the strong elements; the backdrop only drifts"): the deep layer
scrolls at **0.06** (9.6 px/s at 160 px/s, Level 07's slow-sky rule: the bodies drift rather than rush), so Jupiter,
Callisto and the fleet stay on the screen for tens of seconds; the orbital ring's span on ``far`` at 0.3 (48 px/s)
crosses in ~14 s. **Callisto's growth** is one body coming into view (a cap at the top in section 3, the upper half in
the duel, the whole screen from section 6) instead of the gap check's "3-4 authored sizes": a rigid layer cannot swap
discs in place without a pop (pending the user's review in round 78). The sun is low behind the ship: Jupiter's limb is
lit on the side facing the screen, Callisto's near (lower) side is sunlit, so its ice comes in lit and its night side
with the citadel follows.

**The duel** (section 5, ``arena``): the built clock halts the scroll at the arena's end (t=103) while an ace lives
(LevelClock: a non-crawling arena), so the backdrop holds still behind the duel: Callisto's lit lower limb crossing the
screen at y~330, its sunlit ice above (the readability note's bright element behind the gloss-black aces), black space
with stars below. The README's "20 px/s drift for the duel" is the arena section's own 1 s at 20 px/s; a drifting
arena would need a sim change (named in the report).

The layers (``scroll_factors``): ``deep`` 0.06 (one continuous field down the whole level - space, stars, the moons as
points, Jupiter, Callisto - cut into the sections' tile sets at the rows the game shows them (``LevelData.seam``), so
the section changes have no seam on the screen; each tile's rows past its section crossfade back to its own first rows,
so it wraps), the deep pieces over it with their ``-glow`` lights (additive, drawn by the game at the piece's places on
the real clock: Level 36's carrier group's mechanism; two of them on a ``path``), ``far`` 0.3 (the orbital ring's span).
No ground (``ground: none``), no low-air or high-air art (the dead scout and its recorder are drawn with the tow:
tools/art/scout_recorder.py).

Tile sets (480 wide, ``deep``, opaque; one per section, cut from the one field):
  jupiter-shadow   section 1: Jupiter's lit limb and night side filling the bottom right, stars
  picket-line      section 2: Jupiter sinking out over the right edge, stars, Ganymede and Europa as points
  callisto-rising  section 3: Callisto's sunlit lower limb rising at the top
  guard-lane       section 4 (96-98): Callisto's lower half at the top
  guard-brake      section 4's brake (98-102, eased to 20 px/s): the same
  duel             section 5 (the arena, 102-103): the same, held behind the duel
  callisto-ring    section 6: Callisto's sunlit ice filling the screen
  ice-line         section 7: the terminator coming down, the night side above it
Pieces (``-glow`` = additive at its piece's placements on the real clock; the glows' frames loop at their fps):
  picket-station        deep 112x112: an Ascendancy picket station far off (gloss-black ring and hub, gold panels)
  picket-station-glow   deep 112x112, 8 frames at 4 fps: its amber lamps and a white beacon (no red: the sniper's)
  fleet-far             deep 128x96: the CDF carrier group far off, nose up: the carrier and four escorts, dark
  fleet-far-glow        deep 128x96, 8 frames at 4 fps: their white strobes and blue engines (no red nav lights)
  guard-glints          deep 96x48: three gloss-black specks far ahead (the aces)
  guard-glints-glow     deep 96x48, 12 frames at 6 fps: the sun glinting off their gold in turn
  citadel               deep 256x144: the black citadel on Callisto's night side, its outer line of bunkers below
  citadel-glow          deep 256x144, 8 frames at 4 fps: gold windows and lamps steady, red tower beacons blinking
  ring-span             far 480x128: Callisto's orbital ring crossing (a dark lattice band, gold rails, two black and
                        gold relay platforms with dishes, their gold and white lamps)
Motion budget (checked): at most two animated pieces on the screen at once; a deep piece's edges never on the screen
opaque (paths included).

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout read as eight sections (section 4 split at t=98: "the scroll
at 160 until t=98, eased to the 20 px/s drift by t=102" needs the ease to start at a section's start; the README's px
column holds). ``--proposal`` writes the ``backdrop:`` block, the sections and the recorder's ``tows`` row to
concept/backdrop-proposal.yaml (never into data.yaml). **Re-run after the data moves a section** (the tiles are cut at
the sections' rows and the pieces are placed by time).

Review: design/campaign/act-6-jovian-storm/level-40-honour-guard/concept/backdrop-final-r78-a.png (twelve screens with
stand-ins, the readability crops at 1x, the pieces, the tile sets) and .gif (t 95-103 at half size: the glints climbing
away and the brake, then the held duel backdrop with Harker's entry).
Run: python3 tools/art/backdrop_l40.py [--proposal | --review | --check]   (all: about 2 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the shared Jovian pieces: the clock, the fields, the checks' measures)
import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
from jovkit import (JOV_SUN, additive_rgb, blur, checker_share, cut_tile, glow, rgba, smoothstep,  # noqa: E402
                    wrap_score)
from render import raster  # noqa: E402
from render.sdf import (Material, rotate_z, sd_box, sd_capsule, sd_cylinder_z, sd_sphere, union)  # noqa: E402

SCRIPT = "backdrop_l40.py"
BATCH = "M10 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r78"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-40-honour-guard"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-40"
W, SCREEN, SPEED = 480, 540, 160.0
FACTORS = {"deep": 0.06, "far": 0.3}

SPACE = np.array((3, 4, 9.0))
ICE = [(0.0, (12, 15, 22)), (0.3, (38, 46, 60)), (0.6, (74, 84, 100)), (0.85, (112, 122, 138)),
       (1.0, (150, 160, 176))]                                   # Callisto's dark blue-grey ice (act6_r72 space_l40's)
JUP_NIGHT = np.array((22, 14, 10.0))
CREAM_LIMB = np.array((236, 206, 160.0))
GOLD = np.array((255, 196, 64.0))
AMBER = np.array((255, 170, 70.0))
WHITE = np.array((255, 255, 255.0))
BLUE = np.array((110, 170, 255.0))
RED = np.array((255, 64, 44.0))

# =========================================================================== the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "Jupiter's Shadow", "end": 30, "speed": 160, "atmosphere": "clear", "tiles": ["jupiter-shadow"]},
    {"name": "The Picket Line", "end": 64, "speed": 160, "atmosphere": "clear", "tiles": ["picket-line"]},
    {"name": "Callisto Rising", "end": 96, "speed": 160, "atmosphere": "clear", "tiles": ["callisto-rising"]},
    {"name": "The Guard's Lane", "end": 98, "speed": 160, "atmosphere": "clear", "tiles": ["guard-lane"]},
    {"name": "The Guard's Lane, braking", "end": 102, "speed": 20, "ramp": 4, "atmosphere": "clear",
     "tiles": ["guard-brake"]},
    {"name": "The Duel", "end": 103, "speed": 20, "arena": True, "atmosphere": "clear", "tiles": ["duel"]},
    {"name": "Callisto's Ring", "end": 140, "speed": 160, "ramp": 2, "atmosphere": "clear",
     "tiles": ["callisto-ring"]},
    {"name": "The Ice Line", "end": 170, "speed": 160, "atmosphere": "clear", "tiles": ["ice-line"]},
]
SECTIONS = (DATA["sections"] if DATA and DATA.get("sections") else
            OWN["sections"] if OWN and OWN.get("sections") else PROPOSED_SECTIONS)
BASE_SPEED = float(DATA.get("scroll_speed", SPEED)) if DATA else SPEED
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, seam, section_at_row, t_centre_at = CLOCK.scroll, CLOCK.seam, CLOCK.section_at_row, CLOCK.t_centre_at
DEEP = [(s.get("tiles") or [f"deep-{i + 1}"])[0] for i, s in enumerate(SECTIONS)]
ARENA_T = next((float(s["end"]) for s in SECTIONS if s.get("arena")), 103.0)


def deep_row(t):
    return scroll(t) * FACTORS["deep"]


# =========================================================================== the deep field (rows = layer rows, y-up)

def deep_span():
    """Layer rows the level shows on the deep layer: from the start's bottom edge to the end's top (+4 s)."""
    return -40, int(np.ceil(deep_row(END + 4) + SCREEN))


def jupiter():
    """Jupiter's disc: centre (x, layer row), radius - its limb from the bottom left corner to y~115 on the right
    edge at t=0, gone over the right edge by t~44."""
    return 760.0, -430.0, 900.0


def callisto():
    """Callisto's disc: centre (x, layer row), radius - its lower limb at the top edge at t=66."""
    limb = deep_row(66.0) + SCREEN
    return 280.0, limb + 600.0, 600.0


SUN_J = np.array((-0.95, -0.15, -0.25))
SUN_J /= np.linalg.norm(SUN_J)
SUN_C = np.array((-0.32, -0.94, 0.12))
SUN_C /= np.linalg.norm(SUN_C)


def stars(n, r0, seed):
    """Sparse stars on black: most faint single points, a few brighter with a soft 1 px glow; two moons as points."""
    rgb = np.zeros((n, W, 3)) + SPACE
    rng = np.random.default_rng(seed)
    count = int(n * W / 1500)
    ys, xs = rng.integers(0, n, count), rng.integers(0, W, count)
    b = rng.uniform(26, 120, count) ** 1.0
    tint = rng.uniform(-0.08, 0.08, count)
    for y, x, v, c in zip(ys, xs, b, tint):
        rgb[y, x] += (v * (1 + c), v, v * (1 - c))
    light = np.zeros((n, W, 3))
    for _ in range(int(count * 0.06)):
        y, x = rng.uniform(0, n), rng.uniform(0, W)
        glow(light, x, y, 0.9, (235, 240, 255), rng.uniform(0.6, 1.4))
    for (mx, mrow, mr, c) in ((96, 980, 2.0, (214, 206, 186)), (412, 1420, 1.5, (200, 196, 176))):
        glow(light, mx, mrow - r0, mr, c, 1.2)                          # Ganymede, Europa (points far off)
    return rgb + light


def jupiter_disc(x, r, seed):
    """(rgb, coverage) of Jupiter: banded (the L36 grade's cream, ochre and rust), its lit crescent along the limb
    that faces the screen, the night side to the bottom right, a thin haze glowing along the lit limb."""
    cx, cr, R = jupiter()
    dx, dy = (x - cx) / R, (r - cr) / R
    rho = np.sqrt(dx * dx + dy * dy)
    disc = smoothstep(1.0 + 1.0 / R, 1.0 - 1.0 / R, rho)
    nz = np.sqrt(np.clip(1 - rho ** 2, 0, 1))
    lat = np.arcsin(np.clip(dy, -1, 1))
    h, w = x.shape
    warp = raster.fbm(w, h, 48, seed, octaves=4, period=False)
    bands = 0.5 + 0.5 * np.sin(lat * 24 + (warp - 0.5) * 2.6)
    st = jovkit.streaks(h, w, seed + 1)
    col = raster.ramp(JOV_SUN, np.clip(0.2 + 0.55 * bands + 0.2 * (st - 0.5), 0, 1))
    lit = np.clip(dx * SUN_J[0] + dy * SUN_J[1] + nz * SUN_J[2], 0, 1)
    lit = smoothstep(0.0, 0.5, lit) * (0.35 + 0.65 * lit)
    rgb = col * (0.06 + 1.0 * lit[..., None]) + JUP_NIGHT * (1 - lit[..., None]) * 0.4
    halo = (np.exp(-np.clip(rho - 1, 0, None) * R / 7.0) * smoothstep(1.0, 1.0 + 2.0 / R, rho)
            * np.clip(dx * SUN_J[0] + dy * SUN_J[1] + 0.25, 0, 1))
    rim = np.exp(-((rho - 1) * R / 3.0) ** 2) * np.clip(dx * SUN_J[0] + dy * SUN_J[1] + 0.2, 0, 1)
    return rgb, disc, (halo * 0.32 + rim * 0.25)[..., None] * CREAM_LIMB


def callisto_disc(x, r, seed):
    """(rgb, coverage, terminator weight) of Callisto: dark blue-grey ice saturated with craters (bright fresh rims),
    a multi-ring basin on the sunlit side, the sun low behind the ship lighting its near side, the night side above a
    soft terminator (the ice line) with a faint warm Jupiter-shine on its lower right."""
    cx, cr, R = callisto()
    dx, dy = (x - cx) / R, (r - cr) / R
    rho = np.sqrt(dx * dx + dy * dy)
    disc = smoothstep(1.0 + 1.0 / R, 1.0 - 1.0 / R, rho)
    nz = np.sqrt(np.clip(1 - rho ** 2, 0, 1))
    h, w = x.shape
    rng = np.random.default_rng(seed)
    hgt = np.zeros((h, w))
    fresh = np.zeros((h, w))
    r0 = float(r[0, 0])
    craters = [(rng.uniform(-40, W + 40), rng.uniform(cr - R, cr + R), rng.uniform(1.4, 4.2) ** 1.3)
               for _ in range(2200)]
    craters += [(rng.uniform(-40, W + 40), rng.uniform(cr - R, cr + R), rng.uniform(8, 16)) for _ in range(90)]
    craters += [(rng.uniform(-40, W + 40), rng.uniform(cr - R, cr + R), rng.uniform(20, 32)) for _ in range(10)]
    for (ccx, crow, rad) in craters:
        yy = crow - r0
        x0, x1 = int(max(0, ccx - 2.2 * rad)), int(min(w, ccx + 2.2 * rad + 1))
        y0, y1 = int(max(0, yy - 2.2 * rad)), int(min(h, yy + 2.2 * rad + 1))
        if x0 >= x1 or y0 >= y1:
            continue
        ly, lx = np.mgrid[y0:y1, x0:x1].astype(np.float64)
        d = np.hypot(lx - ccx, ly - yy) / rad
        bowl = np.where(d < 1, -(1 - d * d), 0.0) * rad * 0.22
        rim = np.exp(-((d - 1.0) / 0.25) ** 2) * rad * 0.08
        hgt[y0:y1, x0:x1] += bowl + rim
        if rad < 4.5 and rng.uniform() < 0.35:                  # Callisto's bright fresh craters: ice specks
            fresh[y0:y1, x0:x1] = np.maximum(fresh[y0:y1, x0:x1], np.exp(-(d / 1.3) ** 2) * rng.uniform(0.5, 1))
    # the multi-ring basin (Valhalla's kind) low on the sunlit side: a bright palimpsest and concentric ridges
    bx, brow = cx - 210, cr - 360
    db = np.hypot(x - bx, r - brow)
    basin = np.exp(-(db / 70) ** 2) * 0.55
    rings = sum(np.exp(-((db - rr) / 3.2) ** 2) for rr in (95, 128, 166, 210)) * smoothstep(240, 120, db)
    hgt += rings * 2.2
    hgt = blur(hgt, 0.7)
    gy, gx = np.gradient(hgt)                                     # gy along the rows (y-up)
    relief = (gx * SUN_C[0] + gy * SUN_C[1]) * 0.12 * np.sqrt(nz)
    lam = dx * SUN_C[0] + dy * SUN_C[1] + nz * SUN_C[2]
    lit = smoothstep(-0.03, 0.12, lam) * np.clip(lam + 0.15, 0, 1) ** 0.7
    tex = raster.fbm(w, h, 34, seed + 1, octaves=5, period=False)
    tex2 = raster.fbm(w, h, 90, seed + 2, octaves=3, period=False)
    alb = np.clip(0.3 + 0.3 * (tex - 0.5) + 0.25 * (tex2 - 0.5) + 0.4 * fresh + basin * 0.6, 0, 1)
    shade = np.clip(lit * (0.3 + 0.6 * alb) + relief * (0.3 + lit), 0, 1)
    rgb = raster.ramp(ICE, np.clip(shade, 0, 1))
    night = 1 - smoothstep(0.0, 0.25, lam)
    jshine = np.clip(dx * 0.55 - dy * 0.62 + nz * 0.35, 0, 1) ** 2 * night * 0.1
    rgb = rgb * (1 - 0.82 * night[..., None]) + (night * 0.1)[..., None] * np.array((24, 30, 42.0))
    rgb += jshine[..., None] * np.array((150, 120, 90.0))
    rim = np.exp(-((rho - 1) * R / 2.0) ** 2) * (0.15 + 0.35 * lit)
    return rgb, disc, rim[..., None] * np.array((170, 184, 206.0)), lam


def deep_field(seed=4031):
    """The whole deep layer as one float RGB field, row 0 = layer row ``r0`` (y-up). Returns (rgb, r0)."""
    lo, hi = deep_span()
    r0, r1 = lo - 1600, hi + 1600
    n = r1 - r0
    r = (np.arange(n) + r0).astype(np.float64)[:, None] * np.ones((1, W))
    x = np.ones((n, 1)) * np.arange(W, dtype=np.float64)[None, :]
    rgb = stars(n, r0, seed)
    jcol, jdisc, jhalo = jupiter_disc(x, r, seed + 10)
    rgb = rgb * (1 - jdisc[..., None]) + jcol * jdisc[..., None] + jhalo
    ccol, cdisc, crim, _ = callisto_disc(x, r, seed + 20)
    rgb = rgb * (1 - cdisc[..., None]) + ccol * cdisc[..., None] + crim * cdisc[..., None]
    return np.clip(rgb, 0, 255), r0


def deep_tiles():
    field, r0 = deep_field()
    lo, hi = deep_span()
    bounds = [lo] + [seam("deep", i) for i in range(1, len(SECTIONS))] + [hi]
    return {name: cut_tile(field, r0, bounds[i], max(bounds[i + 1], bounds[i] + 1)) for i, name in enumerate(DEEP)}


# =========================================================================== deep pieces

STATION = (112, 112)
FLEET = (128, 96)
GLINTS = (96, 48)
CITADEL = (256, 144)
RING = (480, 128)


def soften(img, edge=6):
    """A deep piece's alpha eased to 0 toward its frame (BackdropAssetsTest: no opaque edge on the screen)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    a[..., 3] *= jovkit.soft_box(img.width, img.height, edge)
    a[a[..., 3] < 0.5] = 0
    return Image.fromarray(np.clip(np.round(a), 0, 255).astype(np.uint8), "RGBA")


def station_model():
    """An Ascendancy picket station from above: a gloss-black ring on three spokes round a gold-panelled hub, gun
    blisters on the ring, a mast with a beacon at the hub."""
    mats = [Material((0.06, 0.06, 0.07), metal=0.6, shininess=110, spec=1.0),
            Material((0.85, 0.66, 0.28), metal=0.7, shininess=70, spec=0.8),
            Material((0.2, 0.2, 0.22), metal=0.5, shininess=40, spec=0.4)]

    def scene(p):
        rho = np.hypot(p[:, 0], p[:, 1])
        ring = np.hypot(rho - 1.0, p[:, 2]) - 0.09
        hub = sd_cylinder_z(p, (0, 0, 0), 0.3, 0.1)
        cap = sd_cylinder_z(p, (0, 0, 0.1), 0.18, 0.05)
        items = [(ring, 0), (hub, 0), (cap, 1)]
        for k in range(3):
            a = TAU * k / 3 + 0.3
            items.append((sd_capsule(p, (0.28 * np.cos(a), 0.28 * np.sin(a), 0), (0.95 * np.cos(a), 0.95 * np.sin(a), 0),
                                     0.045, 0.04), 2))
            items.append((sd_sphere(p, (1.0 * np.cos(a + 1.05), 1.0 * np.sin(a + 1.05), 0.06), 0.1), 1))
        for k in range(12):
            a = TAU * k / 12
            items.append((sd_box(rotate_z(p - np.array((np.cos(a), np.sin(a), 0.05)), -a), (0, 0, 0),
                                 (0.06, 0.1, 0.02), 0.01), 1))
        return union(*items, k=0.02)
    return scene, mats


STATION_LAMPS = [(np.cos(TAU * k / 3 + 1.35) * 1.0, np.sin(TAU * k / 3 + 1.35) * 1.0) for k in range(3)]


def _station():
    scene, mats = station_model()
    hi, f = artkit.render_hi(scene, mats, STATION, 2.6, z_top=3.0)
    return artkit.native(hi, f, 50)


def picket_station():
    body = _station()
    a = np.array(body.convert("RGBA")).astype(np.float64)
    a[..., :3] = a[..., :3] * 0.72 + SPACE * 0.28                       # far off: a step into the black
    body = soften(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"), 4)
    scale = STATION[0] / 2.6
    c = STATION[0] / 2
    frames = []
    for f in range(8):
        light = np.zeros((STATION[1], STATION[0], 3))
        for lx, ly in STATION_LAMPS:
            glow(light, c + lx * scale, c - ly * scale, 1.2, AMBER, 1.3)
            glow(light, c + lx * scale, c - ly * scale, 3.5, AMBER, 0.18)
        k = {0: 1.0, 1: 0.35}.get(f, 0.0)
        if k:
            glow(light, c, c, 1.3, WHITE, 2.0 * k)
            glow(light, c, c, 5.0, (200, 220, 255), 0.3 * k)
        frames.append(additive_rgb(light))
    return body, frames


def _hull(job):
    """One render of a fleet hull: the CDF heavy transport (tools/art/outro_l14_images.py, Level 36's carrier group's
    model) nose up the screen, ``extent`` model units across the ``size`` canvas."""
    import outro_l14_images as o14  # noqa: E402
    size, extent = job
    scene, mats = o14.transport_scene()
    hi, f = artkit.render_hi(lambda p: scene(rotate_z(p, np.pi / 2)), mats, size, extent, z_top=40.0)
    return artkit.native(hi, f, 50)


FLEET_PX = 0.08                                                       # px per transport unit (~20 px long)
FLEET_GROUP = [((64, 38), 1.0), ((32, 56), 0.5), ((96, 54), 0.5), ((48, 76), 0.45), ((82, 78), 0.45)]
FLEET_CANVAS = {1.0: (28, 32), 0.5: (16, 18), 0.45: (14, 16)}
FLEET_LIGHTS = [(-8, -126, "engine"), (8, -126, "engine"), (-42, -121, "engine"), (42, -121, "engine"),
                (-79, -98, "strobe"), (79, -98, "strobe"), (0, 128, "strobe")]   # transport units


def fleet_far():
    """The carrier and four escorts far off, nose up: dark hulls with a cool rim (the sun behind them), their white
    strobes (one flash in 2 s, staggered per hull) and blue engines as additive frames."""
    jobs = [(FLEET_CANVAS[s], FLEET_CANVAS[s][0] / (FLEET_PX * s)) for s in (1.0, 0.5, 0.45)]
    with ProcessPoolExecutor() as pool:
        rend = dict(zip((1.0, 0.5, 0.45), pool.map(_hull, jobs)))
    img = Image.new("RGBA", FLEET, (0, 0, 0, 0))
    for (cx, cy), s in FLEET_GROUP:
        sp = rend[s]
        img.alpha_composite(sp, (int(round(cx - sp.width / 2)), int(round(cy - sp.height / 2))))
    a = np.array(img).astype(np.float64)
    a[..., :3] = a[..., :3] * 0.22 + np.array((16, 18, 26.0)) * 0.5     # far off, the sun behind them: dark
    body = soften(artkit.quantize_set([Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")], 32)[0], 3)
    frames = []
    for f in range(8):
        light = np.zeros((FLEET[1], FLEET[0], 3))
        for j, ((cx, cy), s) in enumerate(FLEET_GROUP):
            k = s * FLEET_PX
            hw, hh = FLEET_CANVAS[s]
            cx0, cy0 = int(round(cx - hw / 2)) + hw / 2, int(round(cy - hh / 2)) + hh / 2
            for mx, my, kind in FLEET_LIGHTS:
                x, y = cx0 + mx * k, cy0 - my * k
                if kind == "strobe":
                    v = {0: 1.0, 1: 0.35}.get((f + 2 * j) % 8, 0.0)
                    if v:
                        glow(light, x, y, 0.9 * (0.7 + 0.3 * s), WHITE, 2.0 * v)
                        glow(light, x, y, 3.0 * (0.7 + 0.3 * s), (200, 220, 255), 0.3 * v)
                else:
                    glow(light, x, y + 1.0 * s, 1.1 * (0.6 + 0.4 * s), BLUE, 1.3)
        frames.append(additive_rgb(light))
    return body, frames


GLINT_SPOTS = [(30, 30), (48, 18), (66, 30)]                           # the three aces in a loose V, nose up


def guard_glints():
    """Three gloss-black specks far ahead (a 3 px wedge with a gold fleck each) and the sun glinting off their gold in
    turn: a soft four-spiked star (smooth, gold-white), 12 frames at 6 fps (each glint peaks once in 2 s)."""
    h, w = GLINTS[1], GLINTS[0]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    rgb = np.zeros((h, w, 3))
    alpha = np.zeros((h, w))
    for gx, gy in GLINT_SPOTS:
        wedge = smoothstep(1.9, 1.0, np.abs(xx - gx) + 0.55 * np.abs(yy - gy - 0.5)) * smoothstep(-2.6, -1.6, yy - gy - 2)
        alpha = np.maximum(alpha, wedge)
        rgb += wedge[..., None] * np.array((14, 12, 16.0))
    rgb[GLINT_SPOTS[0][1] - 1, GLINT_SPOTS[0][0]] = (150, 116, 50)
    body = rgba(rgb, alpha)
    peaks = {0: 0, 4: 1, 8: 2}
    frames = []
    for f in range(12):
        light = np.zeros((h, w, 3))
        for j, (gx, gy) in enumerate(GLINT_SPOTS):
            glow(light, gx, gy - 1, 0.8, GOLD, 0.8)
            for pf, pj in peaks.items():
                if pj != j:
                    continue
                v = {0: 1.0, 1: 0.45, 11: 0.3}.get((f - pf) % 12, 0.0)
                if v:
                    glow(light, gx, gy - 1, 1.2, (255, 236, 190), 2.4 * v)
                    glow(light, gx, gy - 1, 4.0, GOLD, 0.45 * v)
                    for sx, sy, ln in ((1, 0, 9), (0, 1, 7)):
                        d_al = (xx - gx) * sx + (yy - gy + 1) * sy
                        d_ac = (xx - gx) * sy - (yy - gy + 1) * sx
                        spike = np.exp(-(d_ac / 0.6) ** 2) * np.exp(-np.abs(d_al) / (ln * 0.35))
                        light += (spike * 1.4 * v)[..., None] * np.array((255, 220, 150.0))
        frames.append(additive_rgb(light))
    return body, frames


CITADEL_BLOCKS = [(-78, -28, 44, 26), (78, -30, 46, 24), (-70, 14, 34, 22), (70, 12, 36, 24), (0, -52, 52, 20),
                  (-34, 34, 24, 16), (34, 36, 24, 16)]               # (dx, dy, w, h) round the keep, y down
OUTER_LINE = [(-108 + 36.0 * k, 122 - 0.0016 * (36.0 * k - 108) ** 2) for k in range(7)]   # (dx, y): an arc below


def citadel():
    """The black citadel on Callisto's night side from above: an octagonal keep with its spire, hangar blocks and
    causeways round it cut into the ice (a soft dark trench), the outer line's bunkers on an arc below, all gloss
    black under a faint cool rim; gold window strips and lamps (steady) and red tower beacons (blinking once a second)
    as additive frames."""
    w, h = CITADEL
    k = 4
    big = Image.new("L", (w * k, h * k), 0)
    rim = Image.new("L", (w * k, h * k), 0)
    from PIL import ImageDraw
    d, dr = ImageDraw.Draw(big), ImageDraw.Draw(rim)
    cx, cy = w / 2, 62.0

    def box(x0, y0, x1, y1):
        d.rectangle([x0 * k, y0 * k, x1 * k, y1 * k], fill=255)
        dr.line([(x0 * k, y1 * k), (x0 * k, y0 * k), (x1 * k, y0 * k)], fill=255, width=k)

    for bx, by, bw, bh in CITADEL_BLOCKS:
        box(cx + bx - bw / 2, cy + by - bh / 2, cx + bx + bw / 2, cy + by + bh / 2)
    oct_pts = [(cx + 24 * np.cos(a + np.pi / 8), cy + 24 * np.sin(a + np.pi / 8)) for a in np.arange(8) * np.pi / 4]
    d.polygon([(x * k, y * k) for x, y in oct_pts], fill=255)
    dr.line([(x * k, y * k) for x, y in oct_pts[3:7]], fill=255, width=k)
    for ang in (np.pi * 0.5, np.pi * 0.17, np.pi * 0.83):                     # causeways to the outer line
        x1, y1 = cx + np.cos(ang) * 60, cy + np.sin(ang) * 60
        d.line([(cx * k, cy * k), (x1 * k, y1 * k)], fill=255, width=5 * k)
    d.line([(cx * k, (cy - 24) * k), (cx * k, (cy - 60) * k)], fill=255, width=6 * k)   # north causeway
    for ox, oy in OUTER_LINE:
        d.rectangle([(cx + ox - 7) * k, (oy - 5) * k, (cx + ox + 7) * k, (oy + 5) * k], fill=255)
    pts = [((cx + ox) * k, oy * k) for ox, oy in OUTER_LINE]
    d.line(pts, fill=255, width=2 * k)
    cov = np.array(big.resize((w, h), Image.BOX)).astype(np.float64) / 255
    rimv = np.array(rim.resize((w, h), Image.BOX)).astype(np.float64) / 255 * cov
    trench = np.clip(blur(cov, 3.0) * 1.8, 0, 1) * 0.6
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    spire = np.exp(-((xx - cx) ** 2 + (yy - cy) ** 2) / 30.0)
    roof = np.zeros((h, w))
    for bx, by, bw, bh in CITADEL_BLOCKS:
        inside = (np.abs(xx - cx - bx) < bw / 2) & (np.abs(yy - cy - by) < bh / 2)
        u = ((xx - cx - bx) / bw + (yy - cy - by) / bh)
        panels = 0.5 + 0.5 * np.cos((yy - cy - by) * TAU / 6.0)
        roof = np.where(inside, 0.55 - 0.45 * u + 0.18 * panels, roof)
    block = (np.zeros((h, w, 3)) + np.array((8, 9, 12.0)) + (roof * 7)[..., None] * np.array((1.0, 1.05, 1.3))
             + rimv[..., None] * np.array((18, 21, 28.0)) + spire[..., None] * np.array((24, 24, 30.0)))
    alpha = np.maximum(cov, trench)
    rgb = (block * cov[..., None] + np.array((3, 4, 7.0)) * (trench * (1 - cov))[..., None]) \
        / np.maximum(alpha, 1e-6)[..., None]
    body = rgba(rgb, alpha * jovkit.soft_box(w, h, 8))
    rng = np.random.default_rng(4061)
    strips = []
    for bx, by, bw, bh in CITADEL_BLOCKS:
        for _ in range(2):
            n = int(rng.integers(3, 6))
            x0 = cx + bx + rng.uniform(-bw / 2 + 4, bw / 2 - 4 - 3 * n)
            yv = cy + by + rng.uniform(-bh / 2 + 3, bh / 2 - 3)
            strips += [(x0 + 3.0 * j, yv) for j in range(n)]
    strips += [(cx + 19 * np.cos(a), cy + 19 * np.sin(a)) for a in np.arange(8) * np.pi / 4]
    beacons = [(cx + bx + bw / 2 - 3, cy + by - bh / 2 + 2) for bx, by, bw, bh in CITADEL_BLOCKS[:5]] + [(cx, cy)]
    frames = []
    for f in range(8):
        light = np.zeros((h, w, 3))
        for x, y in strips:
            glow(light, x, y, 0.75, GOLD, 0.75)
            glow(light, x, y, 3.0, GOLD, 0.05)
        glow(light, cx, cy, 6.0, GOLD, 0.12)
        for ox, oy in OUTER_LINE:
            glow(light, cx + ox, oy - 1, 0.9, GOLD, 1.3)
            glow(light, cx + ox, oy - 1, 3.0, GOLD, 0.12)
        v = {0: 1.0, 1: 0.55}.get(f, 0.0)
        for x, y in beacons:
            glow(light, x, y, 1.0, RED, 1.5 * v + 0.3)
            glow(light, x, y, 3.5, RED, 0.25 * v)
        frames.append(additive_rgb(light))
    return body, frames


# =========================================================================== far: the orbital ring

def platform_model():
    """A relay platform of Callisto's ring from above: a black hexagonal deck with a gold rim, a dish on a mast,
    two radiator vanes."""
    mats = [Material((0.07, 0.07, 0.08), metal=0.6, shininess=100, spec=0.9),
            Material((0.85, 0.66, 0.28), metal=0.7, shininess=70, spec=0.8),
            Material((0.62, 0.62, 0.64), metal=0.3, shininess=40, spec=0.5)]

    def scene(p):
        ang = np.arctan2(p[:, 1], p[:, 0])
        hexr = np.cos(np.pi / 6) / np.cos((ang % (np.pi / 3)) - np.pi / 6)
        rho = np.hypot(p[:, 0], p[:, 1])
        deck = np.maximum(rho - 0.55 * hexr, np.abs(p[:, 2]) - 0.08)
        rim = np.maximum(np.abs(rho - 0.55 * hexr) - 0.03, np.abs(p[:, 2] - 0.06) - 0.03)
        dish = np.maximum(np.hypot(p[:, 0] + 0.15, p[:, 1] - 0.1) - 0.2, np.abs(p[:, 2] - 0.22) - 0.03)
        mast = sd_cylinder_z(p, (-0.15, 0.1, 0.14), 0.04, 0.08)
        vanes = sd_box(p, (0, 0, 0), (1.05, 0.06, 0.015), 0.01)
        return union((deck, 0), (rim, 1), (dish, 2), (mast, 0), (vanes, 1), k=0.015)
    return scene, mats


def _platform():
    scene, mats = platform_model()
    hi, f = artkit.render_hi(scene, mats, (96, 64), 2.4, z_top=3.0)
    return artkit.native(hi, f, 50)


PLATFORMS = [(140, 0.0), (352, 0.0)]                                    # x on the span, (the curve's y there)


def ring_y(x):
    """The span's centre line on the piece (y down): an arc concentric with Callisto's limb (it bows down)."""
    R = 900.0
    return 84 - (R - np.sqrt(np.clip(R * R - (x - 240) ** 2, 0, None)))


def ring_span():
    """Callisto's orbital ring crossing below: a dark lattice band (cross members every 12 px) with gold rails along
    both edges, two black and gold relay platforms on it with dishes, their gold and white lamps (baked: the far layer
    has no glows); it bows like Callisto's limb."""
    w, h = RING
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    cy = ring_y(xx)
    dv = yy - cy
    band = smoothstep(7.5, 6.0, np.abs(dv))
    lattice = 0.55 + 0.45 * smoothstep(1.2, 0.3, np.abs(((xx + 3) % 12) - 6) - 4.5)
    rail = np.exp(-((np.abs(dv) - 6.5) / 0.9) ** 2)
    rgb = np.zeros((h, w, 3)) + np.array((20, 20, 24.0)) * lattice[..., None]
    rgb = rgb * (1 - rail[..., None]) + rail[..., None] * np.array((120, 94, 46.0)) * (0.6 + 0.4 * (dv < 0))[..., None]
    alpha = np.clip(band * (0.5 + 0.4 * lattice) + rail * 0.8, 0, 1)
    img = rgba(rgb, alpha)
    plat = _platform()
    light = np.zeros((h, w, 3))
    for px, _ in PLATFORMS:
        py = float(ring_y(np.array(px)))
        img.alpha_composite(plat, (int(round(px - plat.width / 2)), int(round(py - plat.height / 2))))
        for lx, ly in ((-22, -8), (22, -8), (0, 14)):
            glow(light, px + lx, py + ly, 0.9, GOLD, 1.3)
        glow(light, px - 6, py - 4, 1.0, WHITE, 1.2)
    for x in range(20, w, 60):
        glow(light, x, float(ring_y(np.array(x))) - 6.5, 0.7, GOLD, 0.9)
    a = np.array(img).astype(np.float64)
    a[..., :3] = np.clip(a[..., :3] + light, 0, 255)
    lit = light.max(axis=-1) > 30
    a[..., 3] = np.maximum(a[..., 3], np.where(lit, np.clip(light.max(axis=-1), 0, 255), 0))
    return Image.fromarray(np.clip(np.round(a), 0, 255).astype(np.uint8), "RGBA")


# =========================================================================== build

GLOWING = {"picket-station": (STATION, 8, 4), "fleet-far": (FLEET, 8, 4), "guard-glints": (GLINTS, 12, 6),
           "citadel": (CITADEL, 8, 4)}


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()

    def save(name, img):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)

    for name, img in deep_tiles().items():
        save(name, img)
        print(f"{name}: {img.width}x{img.height}")
    for name, make in (("picket-station", picket_station), ("fleet-far", fleet_far), ("guard-glints", guard_glints),
                       ("citadel", citadel)):
        body, frames = make()
        save(name, body)
        for i, f in enumerate(frames):
            save(f"{name}-glow_{i}", f)
        print(f"{name}: {body.size}, {len(frames)} glow frames")
    save("ring-span", ring_span())
    return pieces_spec(), placements()


def tile_heights():
    return {name: Image.open(OUT / f"{name}.png").height for name in DEEP}


def pieces_spec():
    spec = {}
    for name, (size, n, fps) in GLOWING.items():
        spec[name] = {"layer": "deep", "size": list(size)}
        spec[name + "-glow"] = {"layer": "deep", "size": list(size), "frames": n, "fps": fps}
    spec["ring-span"] = {"layer": "far", "size": list(RING)}
    return spec


# =========================================================================== the places

def placements():
    f, ff = FACTORS["deep"], FACTORS["far"]
    out = [
        {"piece": "fleet-far", "t": round(t_centre_at(0.0, 470.0, f), 3), "x": 352,
         "path": [[0, 0, 0], [30, 0, 450]],
         "note": "section 1: the carrier group rising off Jupiter's limb (its path climbs 15 px/s against the deep "
                 "layer's 9.6: y 470 at t=0, y~310 at t=30), then sinking out at the bottom by t~54"},
        {"piece": "picket-station", "t": round(t_centre_at(34.0, -50.0, f), 3), "x": 386,
         "note": "section 2: the picket station far off on the right (the recorder drifts down the left edge at t~50),\n"
                 "    # entering the top at t~34 as Jupiter sinks out below it; on the screen to t~100"},
        {"piece": "guard-glints", "t": round(t_centre_at(99.0, 64.0, f), 3), "x": 262,
         "path": [[100.6, 0, 0], [102.4, 0, 200], [102.5, 0, 1000]],
         "note": "section 4: three gold glints far ahead (in from the top at t~90), climbing away out of the top "
                 "edge over t=100.6-102.4 (the aces leaving to make their entries; gone before the sting at 102), then\n"
                 "    # parked 1000 px up for the rest of the level (a path that starts before the duel runs on the real clock\n"
                 "    # after it: its last offset holds)"},
        {"piece": "fleet-far", "t": round(t_centre_at(104.0, 600.0, f), 3), "x": 150,
         "path": [[104, 600, 0], [104.1, 0, 0], [134, 0, 480]],
         "note": "section 6: the fleet closing behind: in from the bottom edge after the duel (16 px/s against the "
                 "deep layer's 9.6: y~600 at t=104, y~410 at t=134), then sinking back out by t~153; until t=104 it\n"
                 "    # waits 600 px off the right edge (its path starts after the duel, so the path keeps the level clock)"},
        {"piece": "citadel", "t": round(t_centre_at(166.0, 66.0, f), 3), "x": 300,
         "note": "section 7: the black citadel on Callisto's night side, its red and gold lights at the top edge "
                 "from t~159, the outer line of lamps at y~130 at the level's end"},
        {"piece": "ring-span", "t": round(t_centre_at(118.0, 270.0, ff), 3), "x": 240,
         "note": "section 6: Callisto's orbital ring and two relay platforms crossing below (far, 0.3): on the "
                 "screen t~111-125"},
    ]
    return out


# =========================================================================== proposal

TOWS = """tows:
  # Proposal (ART-F2, tools/art/scout_recorder.py's sprites, held in build/art-held/sprites/ until the data claims
  # them): Sentry Two's recorder (the secret; README: t~50, x~70, drifting down the left edge; housing 4 hits,
  # `beam_touch`, crate 100). Level 30's buoy rule: the tow's place is the recorder (its housing = the cable box,
  # tether [0, 0]); the look (step F4: TowLooks.LOOKS 40 = `debris-sentry-recorder`) draws the dead scout
  # `debris-sentry-recorder-scout` at (-16, +22) px from it (the recorder trailing below and right of the wreck on its
  # torn cable, y up; one turn in 8 s), the recorder `debris-sentry-recorder` tumbling (one turn in 2.4 s), its beacon
  # `debris-sentry-recorder-glow` (additive, the same frame) on 0.16 s once a second, then `debris-sentry-recorder-break`
  # (0.07 s a frame) at the cut; the scout drifts on at the tow's place after the break.
  - {t: 50, x: 70, drift: [3, -90], boat: [20, 20], pod: [16, 16], tether: [0, 0], cable: [20, 20], hits: 4, reveals: Sentry Two's recorder, beam_touch: true}
"""


def fmt(v):
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, list):
        return "[" + ", ".join(fmt(x) for x in v) + "]"
    if isinstance(v, dict):
        return "{" + ", ".join(f"{k}: {fmt(x)}" for k, x in v.items()) + "}"
    if isinstance(v, str) and ("'" in v or ":" in v or "," in v):
        return '"' + v + '"'
    return str(v)


def proposal_text(pieces, placed):
    src = ("the level's data.yaml" if DATA and DATA.get("sections") else
           "its own proposal's sections" if OWN and OWN.get("sections") else "the README's Layout table")
    h = tile_heights()
    lines = ["backdrop:",
             f"  # Presentation only: Level 40's Callisto approach (tools/art/backdrop_l40.py, step ART-F2, sections from {src}).",
             "  # No ground (`ground: none`): the deep layer is the base, scrolling at 0.06 (the bodies drift: the README's",
             "  # motion budget). Its tile sets are cut from one continuous field at the rows each section's tiles begin",
             "  # (LevelData.seam): re-run the generator after moving a section. Animated: the -glow pieces, additive at",
             "  # their piece's placements on the real clock (two on a `path`), at most two on the screen at once (checked).",
             "  # The scroll halts at the arena's end while an ace lives (the built clock): the backdrop holds still behind",
             "  # the duel, Callisto's lit lower limb crossing the screen at y~330.",
             "  scroll_factors: {deep: 0.06, far: 0.3, ground: 1, low-air: 1.3, high-air: 1.7}   # ground, low-air, high-air unused",
             "  ramp: 2",
             "  haze_colour: 03040a",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # vacuum: no haze, no wisps",
             "  tile_sets:"]
    for i, name in enumerate(DEEP):
        lines.append(f"    {name}: {{layer: deep, height: {h[name]}}}   # section {i + 1}: {SECTIONS[i]['name']}")
    lines.append("  pieces:")
    for name, spec in pieces.items():
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for p in placed:
        if p.get("note"):
            lines.append(f"    # {p['note']}")
        extra = f", path: {fmt(p['path'])}" if p.get("path") else ""
        lines.append(f"    - {{piece: {p['piece']}, t: {p['t']}, x: {p['x']}{extra}}}")
    lines.append("sections:   # ART-F2's reading of the README's Layout (section 4 split at t=98 so the ease to the 20 px/s")
    lines.append("            # drift starts there: the README's px column); F2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - " + "{" + ", ".join(f"{k}: {fmt(v)}" for k, v in s.items() if k != "notes") + "}")
    lines.append(f"# rows: the end's bottom edge {int(round(scroll(END)))} (ground px); deep "
                 f"{int(round(deep_row(END)))}, far {int(round(scroll(END) * FACTORS['far']))}")
    return "\n".join(lines) + "\n" + TOWS


def write_proposal(pieces, placed):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces, placed), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    data = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))
    return data["backdrop"]["pieces"], data["backdrop"]["placed"], data["tows"]


# =========================================================================== composite

_CACHE = {}


def images(name, spec):
    if name not in _CACHE:
        n = spec.get("frames")
        _CACHE[name] = ([Image.open(OUT / f"{name}_{k}.png").convert("RGBA") for k in range(n)] if n
                        else [Image.open(OUT / f"{name}.png").convert("RGBA")])
    return _CACHE[name]


def tile_rows(name, offset):
    if name not in _CACHE:
        _CACHE[name] = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
    arr = _CACHE[name]
    height = len(arr)
    rows = int(round(offset)) + SCREEN - 1 - np.arange(SCREEN)
    return arr[height - 1 - (rows % height)], rows


def layer_tiles(t):
    """The screen's rows of the deep layer at t, each from its section's tile set (the game's seams)."""
    off = deep_row(t)
    out = np.zeros((SCREEN, W, 4), np.uint8)
    for i in range(len(SECTIONS)):
        arr, rows = tile_rows(DEEP[i], off)
        sel = np.array([section_at_row("deep", r) == i for r in rows])
        out[sel] = arr[sel]
    return Image.fromarray(out, "RGBA")


def path_offset(p, t):
    """BackdropData.PlacedPiece.offsetX/offsetY: linear between the waypoints, held before and after."""
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


def piece_xy(p, spec, t):
    f = FACTORS[spec["layer"]]
    dx, dy = path_offset(p, t)
    centre = scroll(p["t"]) * f + SCREEN / 2 + dy
    return p["x"] + dx, SCREEN - (centre - scroll(t) * f)


def on_screen(p, spec, t):
    x, y = piece_xy(p, spec, t)
    w, h = spec["size"]
    return -h / 2 < y < SCREEN + h / 2 and -w / 2 < x < W + w / 2


def blit(img, p, pieces, t, layer, glow_layer=False, clock=None):
    name = p["piece"] + ("-glow" if glow_layer else "")
    spec = pieces.get(name)
    if not spec or spec["layer"] != layer:
        return img
    ims = images(name, spec)
    real = t if clock is None else clock
    frame = ims[int(np.floor(real * spec.get("fps", 0))) % len(ims)] if spec.get("frames") else ims[0]
    x, y = piece_xy(p, pieces[p["piece"]], t)
    if y - frame.height / 2 > SCREEN or y + frame.height / 2 < 0:
        return img
    return l23kit.add_c(img, frame, x, y) if glow_layer else l23kit.paste_c(img, frame, x, y)


def load_held(name):
    try:
        return l23kit.load(name)
    except FileNotFoundError:
        return None


def compose(t, units=True, real=None, duel=None):
    """The screen at t (480x540): the deep tiles, the deep pieces (glows added), the far span, the play plane's
    stand-ins; ``duel`` = seconds into the duel (the backdrop held at the arena's end) for the aces' stand-ins."""
    pieces, placed, tows = load_proposal()
    img = layer_tiles(t)
    for p in placed:
        if pieces[p["piece"]]["layer"] != "deep":
            continue
        img = blit(img, p, pieces, t, "deep")
        img = blit(img, p, pieces, t, "deep", glow_layer=True, clock=real)
    for p in placed:
        img = blit(img, p, pieces, t, "far")
    if units:
        img = stand_ins(img, t, tows)
        if duel is not None:
            img = aces(img, duel)
        img = ship_and_shots(img, t)
    return img


def heading16(dx, dy):
    return int(round((np.degrees(np.arctan2(-dx, dy)) % 360) / 22.5)) % 16


# the stand-ins: (t0, kind, n, edge) - a few of the README's waves
STANDS = [(3.0, "chimera", 4, "top"), (14.0, "talon", 5, "top"), (31.0, "gilded-gunship", 1, "top"),
          (35.5, "mirror-interceptor", 2, "sides"), (48.0, "talon", 5, "bottom"), (65.5, "harrow", 8, "cross"),
          (75.5, "mirror-interceptor", 3, "bottom"), (88.0, "hornet", 2, "top"), (108.0, "talon", 5, "top"),
          (116.0, "chimera", 5, "top"), (126.0, "gilded-gunship", 1, "top"), (150.0, "harrow", 4, "sides"),
          (160.0, "talon", 5, "top")]


def stand_ins(img, t, tows):
    body = {k: load_held(k) for k in ("talon", "chimera", "mirror-interceptor", "harrow", "gilded-gunship", "hornet")}
    sac = load_held("harrow-sac")
    for t0, kind, n, edge in STANDS:
        u = t - t0
        fr = body.get(kind)
        if not 0 <= u <= 6 or not fr:
            continue
        for k in range(n):
            if kind == "harrow":
                uu = u - 0.4 * (k // 4)
                if uu < 0:
                    continue
                side = ("top", "left", "right", "bottom")[k % 4] if edge == "cross" else ("left", "right")[k % 2]
                sx, sy, dx, dy = {"top": (200 + 30 * (k // 4), -20, 0, 1), "left": (-20, 160 + 40 * (k // 4), 1, 0.3),
                                  "right": (500, 140 + 40 * (k // 4), -1, 0.3),
                                  "bottom": (280, 560, -0.15, -1)}[side]
                x, y = sx + dx * 120 * uu, sy + dy * 120 * uu
                hk = heading16(dx, dy)
                img = l23kit.paste_c(img, fr[hk], x, y)
                if sac:
                    img = l23kit.add_c(img, sac[hk], x, y, 0.6)
            elif kind == "mirror-interceptor":
                if edge == "sides":
                    side = (-1, 1)[k % 2]
                    x, y = 240 + side * (300 - 90 * min(u, 2.4)), 150 + 40 * k
                    img = l23kit.paste_c(img, fr[4 if side > 0 else 12], x, y)
                else:
                    x, y = 120 + 120 * k, 580 - 110 * min(u, 3.0)
                    img = l23kit.paste_c(img, fr[8], x, y)
            elif edge == "bottom":
                x = 240 + (k - (n - 1) / 2) * 56
                y = 580 - 130 * u + abs(k - (n - 1) / 2) * 24
                img = l23kit.paste_c(img, fr[8], x, y)
            else:
                x = 240 + (k - (n - 1) / 2) * 56 + (60 if kind == "gilded-gunship" else 0)
                y = -30 + 130 * u - abs(k - (n - 1) / 2) * 24
                if kind in ("hornet", "gilded-gunship"):
                    y = min(y, 140)
                img = l23kit.paste_c(img, fr[0], x, y)
    scout, rec, recg = load_held("debris-sentry-recorder-scout"), load_held("debris-sentry-recorder"), \
        load_held("debris-sentry-recorder-glow")
    for row in tows:
        u = t - row["t"]
        if not 0 <= u < 8:
            continue
        x, y = row["x"] + row["drift"][0] * u, -20 - row["drift"][1] * u
        if scout:
            img = l23kit.paste_c(img, scout[int(u * 2) % len(scout)], x - 16, y - 22)   # one turn in 8 s
        if rec:
            k = int(u * 16 / 2.4) % len(rec)
            img = l23kit.paste_c(img, rec[k], x, y)
            if recg and (u % 1.0) < 0.16:
                img = l23kit.add_c(img, recg[k], x, y)
    return img


ACE_PLAN = {"harker": (lambda u: (240 + 60 * np.sin(u * 0.9), -40 + min(u, 2.0) * 70), 32),
            "vesper": (lambda u: (90 + 70 * np.sin(u * 1.3), 240 + 50 * np.sin(u * 2.6)), 40),
            "kestrel": (lambda u: (380, 600 - min(u, 2.2) * 50), 0)}


def aces(img, duel):
    """The duel's stand-ins (ART-F1's held sets): Harker from the top; with ``duel`` > 20 Vesper on the left and
    Kestrel from the bottom (the three together)."""
    for name, (path, heading) in ACE_PLAN.items():
        if name != "harker" and duel < 20:
            continue
        fr = load_held(f"honour-guard-{name}")
        if not fr:
            continue
        u = duel if name == "harker" else duel - 20
        x, y = path(u)
        img = l23kit.paste_c(img, fr[heading], x, y)
    return img


def ship_and_shots(img, t):
    ship = artkit.load_frames("ship")
    orb, needle = load_held("orb"), load_held("needle")
    if 10 < t and orb and needle:
        for k in range(4):
            img = l23kit.paste_c(img, needle[0], 196 + 22 * k, 250 + 34 * k)
        img = l23kit.paste_c(img, orb[0], 330, 300)
    if ship:
        img = l23kit.paste_c(img, ship[2], 240, 470)
    return img


# =========================================================================== review

SHOTS = [(0.5, None, "T=0.5 JUPITER'S LIMB, THE FLEET RISING"),
         (15.0, None, "T=15 JUPITER'S SHADOW: TALONS"),
         (36.0, None, "T=36 THE PICKET LINE: THE STATION, A PINCER"),
         (51.0, None, "T=51 THE DEAD SCOUT AND ITS RECORDER"),
         (70.0, None, "T=70 CALLISTO RISING: THE HARROW CROSS"),
         (97.0, None, "T=97 THE GUARD'S LANE: THREE GLINTS"),
         (101.6, None, "T=101.6 THE BRAKE: THE GLINTS CLIMB AWAY"),
         (ARENA_T, 3.0, "THE DUEL: HARKER'S ENTRY (HELD BACKDROP)"),
         (ARENA_T, 26.0, "THE DUEL: THE THREE TOGETHER"),
         (118.0, None, "T=118 CALLISTO'S RING, THE FLEET BEHIND"),
         (150.0, None, "T=150 THE ICE LINE COMING DOWN"),
         (169.0, None, "T=169 THE CITADEL'S LIGHTS")]


def windows(t, w=200, h=96):
    """The screen's darkest and brightest 200x96 windows at t, from the backdrop alone."""
    img = compose(t, units=False)
    lum = np.array(img.convert("RGB")).astype(np.float64) @ np.array((0.299, 0.587, 0.114))
    best = []
    for y in range(0, SCREEN - h, 12):
        for x in range(0, W - w, 12):
            best.append((lum[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    return best[0], best[-1], lum.mean(), img


def read_cell(bg, x, y):
    """The readability set at 1x over a 200x96 window: a Talon, Harker (gloss black), a Harrow, needles, an orb,
    the ship."""
    c = bg.crop((x, y, x + 200, y + 96)).copy()
    tal, hk, har = load_held("talon"), load_held("honour-guard-harker"), load_held("harrow")
    orb, needle = load_held("orb"), load_held("needle")
    if tal:
        c = l23kit.paste_c(c, tal[0], 26, 28)
    if hk:
        c = l23kit.paste_c(c, hk[0], 80, 36)
    if har:
        c = l23kit.paste_c(c, har[2], 126, 26)
    for k in range(3):
        c = l23kit.paste_c(c, needle[0], 150 + 10 * k, 20 + 12 * k)
    c = l23kit.paste_c(c, orb[0], 184, 30)
    ship = artkit.load_frames("ship")
    if ship:
        c = l23kit.paste_c(c, ship[2], 150, 72)
    return c


def gif_frames():
    frames = []
    for i in range(80):
        t = 95.0 + i / 10
        frames.append(compose(t).resize((W // 2, SCREEN // 2), Image.BOX))
    for i in range(30):
        u = i / 10
        frames.append(compose(ARENA_T, real=ARENA_T + u, duel=u).resize((W // 2, SCREEN // 2), Image.BOX))
    return frames


def review():
    pieces, placed, _ = load_proposal()
    shots = [(compose(t, duel=d, real=None if d is None else t + d), lab) for t, d, lab in SHOTS]
    cols = 4
    sw, sh = W // 2, SCREEN // 2
    rows_n = -(-len(shots) // cols)
    reads_t = [(10.0, "S1"), (45.0, "S2"), (80.0, "S3"), (ARENA_T, "DUEL"), (125.0, "S6"), (165.0, "S7")]
    read_h = 16 + 2 * (96 + 20)
    pieces_h = 200
    tile_h = 2200 // 8 + 40
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + read_h + 30 + pieces_h + tile_h + 30,
                         "LEVEL 40 BACKDROP (FINAL R78): THE CALLISTO APPROACH", f"PRODUCTION ART, {BATCH.upper()} - R78")
    raster.draw_text(sheet, 16, 38, "TWELVE SCREENS AT HALF SIZE WITH STAND-INS (HELD FRAMES: TALONS, CHIMERAS, MIRRORS, "
                                    "HARROWS, GUNSHIPS, HORNETS, THE ACES, THE SCOUT AND ITS RECORDER)", raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab, raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X: THE DARKEST (TOP) AND BRIGHTEST (BOTTOM) 200X96 WINDOW WITH A "
                                   "TALON, HARKER, A HARROW, NEEDLES, AN ORB, THE SHIP", raster.LABEL)
    means = []
    for j, (t, lab) in enumerate(reads_t):
        (dl, dx, dy), (bl, bx, by), mean, bg = windows(t)
        means.append((lab, t, mean, dl, bl))
        for i, (lum, wx, wy) in enumerate(((dl, dx, dy), (bl, bx, by))):
            c = read_cell(bg, wx, wy).resize((160, 77), Image.BOX)
            x0, y0 = 16 + j * 166, y + 16 + i * 116
            sheet.alpha_composite(c, (x0, y0))
            raster.draw_text(sheet, x0, y0 + 80, f"{lab} {'DARK' if i == 0 else 'BRIGHT'} {lum:.0f}", raster.LABEL_DIM)
    y += read_h + 14
    raster.draw_text(sheet, 16, y, "MEANS: " + "  ".join(f"{lab} {m:.0f} ({d:.0f}-{b:.0f})" for lab, _, m, d, b in means),
                     raster.LABEL_DIM)
    y += 16
    raster.draw_text(sheet, 16, y, "THE PIECES (1X): STATION, FLEET, GLINTS, CITADEL (EACH WITH GLOW FRAME 0 ADDED) ON "
                                   "THE LAYER UNDER THEM; THE RING SPAN (HALF SIZE)", raster.LABEL)
    x = 16
    for name, t in (("picket-station", 50.0), ("fleet-far", 2.0), ("guard-glints", 97.0), ("citadel", 175.0)):
        body = images(name, pieces[name])[0]
        g = images(name + "-glow", pieces[name + "-glow"])[0]
        c = layer_tiles(t).crop((0, 0, body.width, body.height))
        c = l23kit.add_c(l23kit.paste_c(c, body, body.width / 2, body.height / 2), g, body.width / 2, body.height / 2)
        sheet.alpha_composite(c, (x, y + 16))
        x += c.width + 10
    rs = images("ring-span", pieces["ring-span"])[0]
    c = layer_tiles(118.0).crop((0, 0, rs.width, rs.height))
    c.alpha_composite(rs)
    c = c.resize((rs.width // 2, rs.height // 2), Image.BOX)
    sheet.alpha_composite(c, (x, y + 16))
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (EIGHTH SIZE): " + ", ".join(n.upper() for n in DEEP), raster.LABEL)
    x = 16
    for name in DEEP:
        tl = Image.open(OUT / f"{name}.png").convert("RGBA")
        small = tl.resize((tl.width // 8, max(1, tl.height // 8)), Image.BOX)
        sheet.alpha_composite(small, (x, y + 16))
        x += small.width + 10
    artkit.save_review(sheet, gif_frames(), CONCEPT, "backdrop", fps=10)
    for lab, t, m, d, b in means:
        print(f"{lab} t={t}: mean {m:.0f}, darkest window {d:.0f}, brightest {b:.0f}")


# =========================================================================== check

def seam_jump(i):
    t = STARTS[i] + 0.4
    arr = np.array(layer_tiles(t)).astype(np.float64)
    lum = arr[..., :3] @ np.array((0.299, 0.587, 0.114))
    y = int(round(SCREEN - (seam("deep", i) - deep_row(t)))) - 1
    if not 2 <= y < SCREEN - 3:
        return 0.0
    d = lambda a, b: np.abs(lum[a] - lum[b]).mean()  # noqa: E731
    ref = np.median([d(k, k + 1) for k in range(max(0, y - 30), min(SCREEN - 1, y + 30))])
    return d(y, y + 1) / max(ref, 0.5)


def glow_levels(img):
    """Distinct non-zero levels of a glow frame's brightest channel (the smooth-steps measure: >= 32)."""
    a = np.array(img.convert("RGBA"))[..., :3].max(axis=-1)
    return len(np.unique(a[a > 0]))


def check():
    """The deep tiles opaque and wrapping (BackdropSeamsTest's measure), the section seams invisible, sizes as the
    proposal, Source chunks, no checker (DitherGuardTest's measure under 5 %), every glow in >= 32 levels, the deep
    pieces' edges never on the screen opaque (paths included), at most two animated pieces on the screen at once, the
    atlas area."""
    problems = []
    pieces, placed, _ = load_proposal()
    for name in DEEP:
        a = np.array(Image.open(OUT / f"{name}.png").convert("RGBA"))
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
    for i in range(1, len(SECTIONS)):
        j = seam_jump(i)
        if j > 2.0:
            problems.append(f"deep seam {i}: a visible jump ({j:.2f}x the rows round it)")
    for name, spec in pieces.items():
        n = spec.get("frames")
        files = [OUT / f"{name}_{k}.png" for k in range(n)] if n else [OUT / f"{name}.png"]
        for f in files:
            im = Image.open(f)
            if list(im.size) != list(spec["size"]):
                problems.append(f"{f.name}: {im.size} vs {spec['size']}")
        if name.endswith("-glow"):
            lv = max(glow_levels(Image.open(f)) for f in files)
            if lv < 32:
                problems.append(f"{name}: its brightest frame has {lv} levels (< 32)")
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
    worst = 0
    for step in range(int((END + 4) * 10)):
        t = step / 10
        animated = set()
        for p in placed:
            spec = pieces.get(p["piece"] + "-glow")
            if not spec:
                continue
            if on_screen(p, spec, t):
                animated.add((p["piece"], p["t"]))
        worst = max(worst, len(animated))
        if len(animated) > 2:
            problems.append(f"t={t}: {sorted(animated)} animated at once")
            break
    for p in placed:
        spec = pieces[p["piece"]]
        if spec["layer"] != "deep":
            continue
        im = np.array(images(p["piece"], spec)[0])[..., 3]
        edges = {"top": im[0].any(), "bottom": im[-1].any(), "left": im[:, 0].any(), "right": im[:, -1].any()}
        if any(edges.values()):
            if any(on_screen(p, spec, step / 10) for step in range(0, int((END + 4) * 10))):
                problems.append(f"{p['piece']}: an opaque edge ({[k for k, v in edges.items() if v]})")
    for p in placed:
        spec = pieces[p["piece"]]
        seen = any(on_screen(p, spec, s / 10) for s in range(0, int(END * 10)))
        if not seen:
            problems.append(f"{p['piece']} at t={p['t']}: never on the screen")
    area = sum(distinct.values())
    print(f"atlas: {len(distinct)} distinct images, {area / 1e6:.2f} M px with padding = "
          f"{area / 2048 ** 2:.2f} pages of 2048^2 before packing losses; checker {chk / max(vis, 1) * 100:.2f} % "
          f"of {vis} px; at most {worst} animated pieces on the screen")
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
