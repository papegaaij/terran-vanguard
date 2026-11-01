#!/usr/bin/env python3
"""Production art: the Level 37 backdrop, Fall of Aurelia (design/campaign/act-6-jovian-storm/level-37-fall-of-aurelia;
M10 part C batch, concept round 75, step ART-C2): the Aurelia kit, the Vrell turn's growth burst, the backdrop war and
the falling city.

Straight to production from round 72's chosen looks: Q10 a's **L37 grade** (tools/concept/act6_r72.py ``sky_l37``:
Aurelia's gold and white over warm cream cloud, the decks **a step paler and less saturated than the units' gold**;
after the turn fire-orange and teal), the round-06 scene's octagonal decks, gold-glass domes, walkways and lift
balloons, A.12 a's one cream for every cloud (the user: "clouds at different altitudes do not have different colors";
tools/art/jovkit.py ``CREAM``) and **A.13 b** with round 73 R.2's storyboard (design/world/jovian/concept/
falling-city-r73-b.png): the main deck breaks along a **jagged fault** - torn plate, ragged edges, flaps bent outward,
a charred and hot-glowing rim, shards in the widening gap, smoke and fire from both edges, never a clean cut - into
**two city layers** that slow, darken and shrink toward the field's centre at their own rates. Every haze, glow, fire
and smoke is a float field written unquantised (far more than 32 steps, no ordered dither: the 2026-10-30 dither
audit, ``DitherGuardTest``); every glow is additive (premultiplied on black) and drawn once.

The layers (``scroll_factors``): ``deep`` 0.15 (the cream cloud deck far below; the city's far side; the war's far
skirmishes; the sinking city), ``ground`` 1 (Aurelia's decks: **one continuous city** authored row by row from the
level's clock, cut into ``city-NN`` ground pieces of at most 2,040 rows laid back to back on exact rows, since its
landmarks - the pads, the nests' decks, the plaza and the great spire, the locker's awning, the main deck that falls -
stand where the waves need them and a repeating tile set could not hold them), ``low-air`` 1.3 (balloons, lift rings,
falling hulks, the toppling spires, the fall's debris), ``high-air`` 1.7 (smoke wisps after the turn: the atmosphere's
``wisps``).

The city (ground pieces; transparent where the cloud shows between the platforms):
  sections 1-3 before the turn: pale cream decks with gilded rims and red rim lights, gold-glass domes, promenades,
  walkways with gold rails, the shuttle pad decks (pads at x 190 / 290 in the lower half when each pair lifts), the
  nests' decks (the rim's deck edge t~14, the SAM platform t~24, the dome rim t~48, the deck edges either side of
  the pad terrace t~62 with the CDF locker's awning at x 90, t~60, the Spire Plaza's terrace t~79, the SAM sockets
  under the great spire t~95); teal veins creeping into the dome seams from section 2's end (the Graft Turrets).
  the turn (``turn``, t=100): every row entering after it is **post-turn**: teal growth over the plates and along
  the seams, buckled plates, soot, fires (static, baked), broken domes; the screen at the turn is the growth burst.
  the main deck (the last platform, rows from 80 below the bottom edge to 640 above it at ``fall.t``): burning and
  overgrown, two great domes either side of the fault; nothing else of the city is near it.
Tile sets (480 wide):
  cloud-deck   deep 480x1024: cream cloud heads over warm ochre gaps far below the city (sections 1-4), wrapping
  cloud-bands  deep: section 5: the cloud deck opening on the bands (cut from one field at its seam; wraps)
  smoke-wisps  high-air 480x640: dark smoke streaks drifting over (the atmosphere's wisps from the turn on)
Pieces (``-glow`` = additive, drawn at its piece's placements on the piece's frame clock):
  city-01 ..   ground 480xN (N <= 2040): the continuous city, back to back on exact rows (``placed`` with ``bottom``)
  far-side     deep 480x240: the city's far side far below in the haze (section 1)
  balloon-a/-b low-air: tethered lift balloons (cream envelopes, gold bands, a cable stub); balloon-torn: a torn one
  lift-ring, lift-ring-glow  low-air: an anti-grav ring (gold torus) and its pale blue glow (static)
  deck-fire, deck-fire-glow  ground 48x64, 8 frames at 10 fps: a fire on a burning deck (flames and smoke, its glow)
  war-skirmish deep 160x128, 8 frames at 6 fps: gold craft and Vrell swarms fighting far below (no hit boxes)
  hulk-a/-b    low-air 80x112, 8 frames at 8 fps: a burning Ascendancy hulk tumbling past, smoke above it
  debris-plate-a/-b/-c, debris-cable  low-air: deck plates and a cable coil after the fall (static)
The turn (``growth``, drawn by the game's growth pass over the city at the turn, the screen at ``turn``):
  growth-burst         ground 480x572: the post-turn look of the rows on the screen at the turn (teal tendrils
                       along the plaza's seams, buckled plates, soot, fires), alpha where it differs from the city
  growth-burst-reveal  480x572, grey: each pixel's reveal time over ``seconds`` (value = time / seconds x 255;
                       racing from the dome rims along the seams), alpha where growth-burst has any
  growth-burst-glow    480x572, additive: the teal seams' and the fires' glow, revealed by the same map
The fall (``fall``, drawn by the game's falling-city pass from ``fall.t``; the halves take over the ground at once):
  fall-half-west/-east     ground: the main deck's two halves, the city's exact pixels on the ground rows given
                           (their union is the city on those rows: the handover is pixel-exact), the jagged cut in
                           their alpha
  fall-half-west/-east-char  the torn rim: charred plate, flaps bent outward past the cut, smoke clinging (normal
                           blend; faded in as the gap opens, so the handover stays exact)
  fall-half-west/-east-glow  4 frames at 8 fps, additive: the hot-glowing rim and the fires along the cut
  fall-shards              96x160, 8 frames at 8 fps: plate shards tumbling in the widening gap
  balloon-tear             low-air 96x128, 10 frames at 5 fps, one-shot from ``fall.t``: a lift balloon tearing and
                           collapsing (fabric, no pop)
  spire-topple-w/-e        low-air 192x96, 10 frames at 5 fps, one-shot: a tall gold spire toppling west / east as
                           the wing flies down beside it
  fall-veil-west/-east     ground 288x196 (round 75 F.6 redo, step ART-C3): patches of the cloud deck's heads (its
                           look and the smoke haze baked in) that slide in from the sides and close over the sinking
                           halves, then fade into the deck (drawn by the fall pass after the halves, at their keys)
  sinking-city, sinking-city-glow  deep 256x208 (+ 4 glow frames at 6 fps): ART-C2's sinking city, frozen at its
                           20 s curve's end (R75A_HALVES) - Level 38's aurelia-wreck source, no longer drawn in Level 37
The fall's curve (F.6 redo): the halves alone carry the whole fall, t 160-196 (``fall_keys``: [u, x, y, scale, turn,
shade, alpha]): they slow, shrink, turn and darken as before, keep their gap, are whole on the screen from 12.5 s,
sink slowly late and fade from 1.5 s to 0 at 34 s while the veils close over them; nothing of the city after t 194.

Section times: the level's data.yaml when it has sections, else the ``sections:`` of this script's own proposal
(concept/backdrop-proposal.yaml), else the README's Layout. ``turn`` and ``fall`` (t, seconds) likewise. **Re-run after
a section moves**: the whole city is laid out on the clock (every landmark by its time), so the halves, the growth
burst and the chunks follow. ``--proposal`` writes the ``backdrop:`` block (tile sets, pieces, the placements, the
``growth`` and ``fall`` blocks with the halves' curves), the sections and the ground targets' places to
concept/backdrop-proposal.yaml (never into data.yaml).

Review: design/campaign/act-6-jovian-storm/level-37-fall-of-aurelia/concept/fall-final-r75-b.png (round 75 F.6 redo:
the whole fall t 158-200 at half size, 1:1 crops of the late fall, the veils, the curves) and .gif (t 158-200 at play
size, 4 frames a second); round 75 a's backdrop-final-r75-a.png/.gif and growth-final-r75-a.gif (step ART-C2) are
rebuilt only with ``--review-a`` (their fall would show the redo's).
Run: python3 tools/art/backdrop_l37.py [--proposal | --review | --review-a | --check]   (all: about 3.5 min)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
import yaml
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, TAU

import jovkit  # noqa: E402  (the shared Jovian pieces)
import l23kit  # noqa: E402  (held sprites: load from assets/ or build/art-held/)
from jovkit import (CREAM, JOV_SUN, additive_rgb, blur, checker_share, cloud_mass, glow, mass_blobs,  # noqa: E402
                    rgba, smoothstep, soft_box, wrap_score, wrap_score_alpha)
from render import raster  # noqa: E402

SCRIPT = "backdrop_l37.py"
BATCH = "M10 part C batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r75"
artkit.REVIEW_ROUND = ROUND
LEVEL_DIR = DESIGN / "campaign" / "act-6-jovian-storm" / "level-37-fall-of-aurelia"
CONCEPT = LEVEL_DIR / "concept"
PROPOSAL = CONCEPT / "backdrop-proposal.yaml"
OUT = ROOT / "assets" / "backdrop" / "level-37"
W, SCREEN = 480, 540
FACTORS = {"deep": 0.15, "ground": 1.0, "low-air": 1.3, "high-air": 1.7}
MAX_PIECE = 2040                                   # an atlas page is 2048 high (AtlasPacker), 2 px padding a side

# The L37 grade (Q10 a): the decks a step paler and less saturated than the units' gold (enemies_r72 DECK_STOPS).
DECK = np.array((214, 204, 178.0))
DECK_DARK = np.array((180, 168, 136.0))
GOLD = np.array((204, 170, 98.0))                  # gilded trim: the units' (255, 196, 64) desaturated a step
GOLD_GLASS = np.array((196, 156, 88.0))
CREAM_DOME = np.array((232, 222, 196.0))
SLATE = np.array((84, 86, 94.0))                   # the pads
BLACK = np.array((34, 28, 30.0))                   # black accents
RED_LAMP = np.array((255, 64, 44.0))
WARM_LAMP = np.array((255, 214, 150.0))
TISSUE = np.array((26, 74, 68.0))                  # Vrell growth (act6_r72 sky_l37 after)
VEIN = np.array((0, 220, 140.0))
FIRE = np.array((255, 122, 34.0))
FIRE_CORE = np.array((255, 226, 150.0))
SOOT = np.array((44, 34, 32.0))
SMOKE = np.array((58, 48, 46.0))
SUN = np.array((-0.55, -0.6, 0.62))
SUN = SUN / np.linalg.norm(SUN)

# =========================================================================== the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
OWN = yaml.safe_load(PROPOSAL.read_text(encoding="utf-8")) if PROPOSAL.exists() else None
PROPOSED_SECTIONS = [
    {"name": "Aurelia's Rim", "end": 30, "speed": 120, "atmosphere": "clear", "tiles": ["cloud-deck"]},
    {"name": "The Gilded Decks", "end": 70, "speed": 100, "ramp": 2, "atmosphere": "clear", "tiles": ["cloud-deck"]},
    {"name": "The Spire Plaza", "end": 100, "speed": 80, "ramp": 2, "atmosphere": "clear", "tiles": ["cloud-deck"]},
    {"name": "The Turn", "end": 158, "speed": 100, "ramp": 2, "atmosphere": "smoke",
     "tiles": ["cloud-deck", "smoke-wisps"]},
    {"name": "The Fall", "end": 200, "speed": 100, "atmosphere": "smoke", "tiles": ["cloud-bands", "smoke-wisps"]},
]


def _source():
    if DATA and DATA.get("sections"):
        return DATA, "the level's data.yaml"
    if OWN and OWN.get("sections"):
        return OWN, "this script's proposal (concept/backdrop-proposal.yaml)"
    return None, "the README's Layout table"


SRC, SRC_NAME = _source()
SECTIONS = SRC["sections"] if SRC else PROPOSED_SECTIONS
BASE_SPEED = float((SRC or {}).get("scroll_speed", 120))
CLOCK = jovkit.Clock(SECTIONS, BASE_SPEED, FACTORS, SCREEN)
END, STARTS = CLOCK.end, CLOCK.starts
scroll, time_at, seam = CLOCK.scroll, CLOCK.time_at, CLOCK.seam
t_for_layer_row, t_centre_at = CLOCK.t_for_layer_row, CLOCK.t_centre_at


def _block(key):
    for src in (DATA, OWN):
        if src and isinstance(src.get(key), (int, float)):
            return {"t": float(src[key])}
        if src and src.get("backdrop") and src["backdrop"].get(key):
            return src["backdrop"][key]
    return None


TURN_T = float((DATA or {}).get("turn") or (_block("growth") or {}).get("t") or 100.0)
TURN_S = float((_block("growth") or {}).get("seconds") or 4.0)
FALL_T = float((_block("fall") or {}).get("t") or 160.0)
FALL_S = float((_block("fall") or {}).get("seconds") or 20.0)


def top(t):
    """The ground row at the top edge at t."""
    return scroll(t) + SCREEN


def at(t, y_down):
    """The ground row at screen y (down) at t."""
    return scroll(t) + SCREEN - y_down


def enter_t(row, half=26):
    """The time a ground target of ``2 * half`` px with its centre on ``row`` enters at the top edge."""
    return time_at(row - SCREEN - half)


def exact_t(bottom, h, factor=1.0):
    """The placement time putting a piece h px tall with its bottom row on layer row ``bottom``."""
    t = time_at((bottom + h / 2 - SCREEN / 2) / factor)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) * factor + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


FALL_BASE = int(round(scroll(FALL_T)))             # the ground row at the bottom edge when the city falls
HALF_LO, HALF_HI = FALL_BASE - 80, FALL_BASE + 640  # the main deck's rows (the halves)
TURN_BASE = int(round(scroll(TURN_T)))
GROW_LO, GROW_HI = TURN_BASE - 16, TURN_BASE + SCREEN + 16   # the growth burst's rows
POST_ROW = TURN_BASE + SCREEN                      # rows from here on are post-turn (they enter after the turn)
ROW_LO, ROW_HI = -40, HALF_HI + 40


# =========================================================================== the layout (rows y-up, x)

def layout():
    """Every platform of the city by its time: (kind, params). Kinds: walk (x0, r0, x1, r1, w), deck (x, r, a)
    a regular octagon of apothem a, rect (x0, x1, r0, r1, chamfer), plaza (x, r, hx, hy, chamfer), dome (x, r,
    radius), domelets (x, r, a: four small domes round a deck), pad (x, r), sam (x, r), spire (x, r, base), awning
    (x0, x1, r0, r1), mooring (x, r)."""
    L = []
    T = top
    # -- section 1: Aurelia's Rim ------------------------------------------------------------------------------
    d1, d2, d3 = (120, T(1.4)), (360, T(3.5)), (190, T(6.6))
    p1 = (300, 465, T(8.4), T(10.0))
    n1 = (350, T(14) + 70)                            # the rim's deck edge, the first nest (t~14) on its near edge
    d4, d5 = (150, T(19.2)), (390, T(21.0))
    s1 = (60, 250, T(24) - 52, T(24) + 92)            # the rim's air-defence platform (t~24)
    d6 = (300, T(28.6))
    L += [("walk", (*d1, *d2, 12)), ("walk", (*d2, *d3, 12)), ("walk", (d3[0], d3[1], 320, p1[2] + 40, 10)),
          ("walk", (420, p1[3] - 20, n1[0] + 40, n1[1] - 60, 12)), ("walk", (*n1, *d4, 12)), ("walk", (*d4, *d5, 10)),
          ("walk", (d4[0], d4[1], 150, s1[2] + 30, 12)), ("walk", (150, s1[3] - 20, *d6, 12))]
    L += [("deck", (*d1, 62)), ("domelets", (*d1, 62)), ("deck", (*d2, 54)), ("dome", (*d2, 24)),
          ("deck", (*d3, 78)), ("dome", (*d3, 36)), ("rect", (*p1, 18)), ("deck", (*n1, 112)),
          ("dome", (n1[0] + 6, n1[1] + 40, 30)),
          ("deck", (*d4, 66)), ("domelets", (*d4, 66)), ("deck", (*d5, 58)), ("dome", (*d5, 26)),
          ("rect", (*s1, 16)), ("sam", (120, T(24) + 22)), ("sam", (190, T(24) + 22)),
          ("deck", (*d6, 92)), ("dome", (*d6, 44))]
    # -- section 2: The Gilded Decks ---------------------------------------------------------------------------
    d7, p2 = (130, T(32.4)), (250, 455, T(34.0), T(36.6))
    pad1 = at(44, 380)                                # pair 1 on its pads, lower half of the screen as it lifts
    d8, dr = (390, T(42.4)), (300, T(48) + 100)       # the dome rim deck (t~48)
    d9, p3 = (110, T(52.6)), (210, 465, T(55.0), T(57.0))
    lock = top(60) + 10                               # the CDF locker under its awning (t~60, x 90)
    tr = T(62) + 90                                   # the pad terrace between two deck edges (t~62)
    pad2 = at(72, 380)
    L += [("walk", (*d6, *d7, 12)), ("walk", (*d7, 300, p2[2] + 40, 12)),
          ("walk", (380, p2[3] - 30, 240, pad1 - 70, 14)),
          ("walk", (240, pad1 + 70, *d8, 12)), ("walk", (*d8, dr[0] + 60, dr[1] - 80, 12)),
          ("walk", (dr[0] - 90, dr[1] + 60, *d9, 12)), ("walk", (*d9, 280, p3[2] + 30, 12)),
          ("walk", (100, p3[3] - 10, 100, tr - 140, 14)), ("walk", (240, tr + 110, 240, pad2 - 70, 16))]
    L += [("deck", (*d7, 80)), ("dome", (*d7, 38)), ("rect", (*p2, 20)),
          ("domelets", ((p2[0] + p2[1]) / 2, (p2[2] + p2[3]) / 2, 70)),
          ("rect", (130, 350, pad1 - 76, pad1 + 76, 24)), ("pad", (190, pad1)), ("pad", (290, pad1)),
          ("deck", (*d8, 62)), ("dome", (*d8, 26)),
          ("deck", (*dr, 124)), ("dome", (dr[0], dr[1] + 4, 56)),
          ("deck", (*d9, 86)), ("dome", (*d9, 40)), ("rect", (*p3, 20)),
          ("rect", (24, 176, lock - 40, tr + 110, 16)),                    # the left deck edge (the locker's)
          ("rect", (304, 456, tr - 140, tr + 120, 16)),                    # the right deck edge
          ("rect", (176, 304, tr - 60, tr + 60, 10)), ("pad", (240, tr)),  # the pad terrace between them (empty)
          ("awning", (58, 122, lock + 18, lock + 58)),
          ("rect", (130, 350, pad2 - 76, pad2 + 76, 24)), ("pad", (190, pad2)), ("pad", (290, pad2))]
    # -- section 3: The Spire Plaza ----------------------------------------------------------------------------
    ter = T(79) + 70                                  # the terrace (t~79) in front of the plaza
    sams = T(95) + 22                                 # the SAM sockets on the plaza's far side under the spire
    sp = (240, sams - 300)                            # the great spire
    pl_lo, pl_hi = ter + 130, TURN_BASE + 330
    L += [("walk", (240, pad2 + 70, 240, ter - 110, 22)),
          ("rect", (40, 440, ter - 120, ter + 120, 40)), ("domelets", (240, ter, 110)),
          ("rect", (180, 300, ter + 100, pl_lo + 40, 6)),
          ("plaza", (240, (pl_lo + pl_hi) / 2, 222, (pl_hi - pl_lo) / 2, 120)),
          ("inlay", (*sp, 160, 2.2)), ("inlay", (*sp, 250, 1.6)), ("inlay", (*sp, 360, 2.2)),
          ("pool", (206, 274, pl_lo + 60, sp[1] - 190)), ("pool", (58, 138, sp[1] - 300, sp[1] - 120)),
          ("pool", (342, 422, sp[1] - 300, sp[1] - 120)),
          ("dome", (96, sp[1] + 40, 24)), ("dome", (384, sp[1] + 40, 24)),
          ("dome", (96, pl_lo + 120, 22)), ("dome", (384, pl_lo + 120, 22)),
          *[("dome", (sp[0] + 132 * np.cos(a), sp[1] + 132 * np.sin(a), 10, "cream"))
            for a in np.linspace(0, TAU, 8, endpoint=False) + TAU / 16],
          ("spire", (*sp, 92)),
          ("sam", (150, sams)), ("sam", (240, sams)), ("sam", (330, sams)),
          ("dome", (80, TURN_BASE + 120, 28)), ("dome", (400, TURN_BASE + 110, 28)),
          ("dome", (150, TURN_BASE + 250, 24)), ("dome", (330, TURN_BASE + 255, 24))]
    # -- section 4: The Turn (post-turn rows: the growth and the fires are applied over them) -----------------
    d10, d11 = (330, TURN_BASE + 640), (120, TURN_BASE + 980)
    p4 = (230, 455, TURN_BASE + 1240, TURN_BASE + 1480)
    d12 = (300, TURN_BASE + 1800)
    pad3 = at(127, 380)
    bd = (300, T(128) + 92)                           # the burning deck (t~128)
    s2 = T(138) + 22                                  # the last SAM platform (t~138)
    bal = (240, T(144) + 190)                         # the balloon deck (t~144): the last ground units on its edge
    L += [("walk", (240, pl_hi - 40, *d10, 14)), ("walk", (*d10, *d11, 12)), ("walk", (*d11, 300, p4[2] + 40, 12)),
          ("walk", (400, p4[3] - 30, *d12, 12)), ("walk", (*d12, 240, pad3 - 70, 14)),
          ("walk", (240, pad3 + 70, bd[0] - 40, bd[1] - 100, 14)),
          ("walk", (bd[0] - 60, bd[1] + 100, 230, s2 - 60, 14)),
          ("walk", (230, s2 + 60, 240, bal[1] - 170, 18))]
    L += [("deck", (*d10, 84)), ("dome", (*d10, 40)), ("deck", (*d11, 70)), ("domelets", (*d11, 70)),
          ("rect", (*p4, 22)), ("deck", (*d12, 96)), ("dome", (*d12, 46)),
          ("rect", (130, 350, pad3 - 76, pad3 + 76, 24)), ("pad", (190, pad3)), ("pad", (290, pad3)),
          ("deck", (*bd, 124)), ("dome", (bd[0] + 10, bd[1] + 30, 40)),
          ("rect", (60, 410, s2 - 66, s2 + 66, 22)), ("sam", (110, s2)), ("sam", (230, s2)), ("sam", (350, s2)),
          ("plaza", (*bal, 210, 176, 90)), ("dome", (bal[0], bal[1] + 40, 52)),
          ("mooring", (110, bal[1] + 120)), ("mooring", (240, bal[1] + 160)), ("mooring", (370, bal[1] + 120))]
    # -- the main deck: the city's last platform, which breaks at fall.t ---------------------------------------
    m = FALL_BASE + 280
    L += [("walk", (60, m + 200, 150, m + 120, 14)), ("walk", (420, m - 200, 330, m - 120, 14)),
          ("rect", (150, 330, m - 250, m + 260, 60)),
          ("deck", (150, m - 60, 140)), ("deck", (330, m + 40, 150)),
          ("deck", (60, m + 200, 50)), ("domelets", (60, m + 200, 50)), ("deck", (420, m - 200, 56)),
          ("dome", (420, m - 200, 22)),
          ("dome", (130, m - 70, 54)), ("dome", (350, m + 50, 60)), ("domelets", (150, m - 60, 132)),
          ("domelets", (330, m + 40, 140)), ("dome", (238, m + 200, 24)), ("dome", (246, m - 190, 20)),
          ("mooring", (200, FALL_BASE + 516)), ("mooring", (282, FALL_BASE + 520)),
          ("mooring", (52, FALL_BASE + 502))]
    return L


def ground_targets():
    """The ground targets' places on the decks: (label, section, enemy, count, [[t, x], ...]) - the nests on their
    deck edges, rims and terraces, by the README's times."""
    T = top
    n1 = T(14) + 70
    dr = T(48) + 100
    tr = T(62) + 90
    ter = T(79) + 70
    bd = T(128) + 92
    bal = T(144) + 190

    def ring(cx, cr, r, angles):
        return [[round(enter_t(cr + r * np.sin(np.radians(a))), 2), int(round(cx + r * np.cos(np.radians(a))))]
                for a in angles]
    return [
        ("Graft Turret nest x3 on the rim's deck edge (the first, alone)", 1, "graft-turret", 3,
         [[round(enter_t(n1 - 44), 2), 300], [round(enter_t(n1 - 56), 2), 350], [round(enter_t(n1 - 38), 2), 404]]),
        ("SAM Nest pair on the rim's air-defence platform", 1, "sam-nest", 2, [[24, 120], [24, 190]]),
        ("Graft Turret nest x4 round the dome rim", 2, "graft-turret", 4, ring(300, dr + 4, 84, (205, 245, 295, 335))),
        ("Graft Turret nest x5 on the two deck edges beside the pad terrace", 2, "graft-turret", 5,
         [[round(enter_t(tr - 40), 2), 70], [round(enter_t(tr), 2), 130], [round(enter_t(tr - 60), 2), 350],
          [round(enter_t(tr - 20), 2), 410], [round(enter_t(tr + 30), 2), 380]]),
        ("Graft Turret nest x6 on the Spire Plaza's terrace", 3, "graft-turret", 6,
         [[round(enter_t(ter - 60), 2), 90], [round(enter_t(ter - 60), 2), 160], [round(enter_t(ter - 20), 2), 240],
          [round(enter_t(ter - 60), 2), 320], [round(enter_t(ter - 60), 2), 390], [round(enter_t(ter + 50), 2), 240]]),
        ("SAM Nest x3 on the plaza's far side under the great spire", 3, "sam-nest", 3,
         [[95, 150], [95, 240], [95, 330]]),
        ("Graft Turret nest x5 on a burning deck", 4, "graft-turret", 5,
         ring(300, bd, 92, (200, 235, 270, 305, 340))),
        ("SAM Nest x3 on the last platform", 4, "sam-nest", 3, [[138, 110], [138, 230], [138, 350]]),
        ("Graft Turret nest x4 on the balloon deck's edge (the last ground units)", 4, "graft-turret", 4,
         [[round(enter_t(bal - 150), 2), 120], [round(enter_t(bal - 166), 2), 200],
          [round(enter_t(bal - 166), 2), 280], [round(enter_t(bal - 150), 2), 360]]),
    ]


# =========================================================================== the city field (one continuous field)

def sd_chamfer(dx, dy, hx, hy, ch):
    """A chamfered rectangle's signed distance (half sizes hx, hy; ch the corner cut): a regular octagon of apothem a
    is hx = hy = a, ch = a (2 - sqrt 2)."""
    ax, ay = np.abs(dx), np.abs(dy)
    return np.maximum(np.maximum(ax - hx, ay - hy), (ax + ay - hx - hy + ch) / np.sqrt(2))


def chamfer_outline(hx, hy, ch):
    return [(hx, -(hy - ch)), (hx, hy - ch), (hx - ch, hy), (-(hx - ch), hy), (-hx, hy - ch), (-hx, -(hy - ch)),
            (-(hx - ch), -hy), (hx - ch, -hy)]


def along(poly, spacing, inset):
    """Points every ``spacing`` px along a closed polygon (counter-clockwise, y-up), moved ``inset`` px inward."""
    pts = []
    n = len(poly)
    carry = spacing / 2
    for k in range(n):
        (x0, y0), (x1, y1) = poly[k], poly[(k + 1) % n]
        seg = np.hypot(x1 - x0, y1 - y0)
        if seg < 1e-6:
            continue
        ux, uy = (x1 - x0) / seg, (y1 - y0) / seg
        nx, ny = -uy, ux                                     # inward for a counter-clockwise polygon
        s = carry
        while s < seg:
            pts.append((x0 + ux * s + nx * inset, y0 + uy * s + ny * inset))
            s += spacing
        carry = s - seg
    return pts


def _hash(*vals):
    h = np.zeros_like(np.asarray(vals[0], np.float64))
    for k, v in enumerate(vals):
        h = h + np.asarray(v, np.float64) * (127.1 + 311.7 * k)
    return np.modf(np.sin(h) * 43758.5453)[0] % 1.0


class Field:
    """The city's arrays over ground rows [r0, r1) in image order (index i = r1 - 1 - row: the top row first)."""

    def __init__(self, r0, r1):
        self.r0, self.r1 = r0, r1
        h = r1 - r0
        self.hgt = np.zeros((h, W), np.float32)
        self.alpha = np.zeros((h, W), np.float32)
        self.alb = np.zeros((h, W, 3), np.float32)
        self.emis = np.zeros((h, W, 3), np.float32)
        self.seam = np.zeros((h, W), np.float32)         # plate seams (the growth's paths)
        self.kind = np.zeros((h, W), np.uint8)           # 1 deck, 2 dome, 3 walk, 4 pad, 5 spire
        self.rim = np.zeros((h, W), bool)                # dome rims: where the turn's growth bursts from
        self.lamps = []                                  # (x, row, colour, r, k): emissive points, added after

    def win(self, x0, x1, ra, rb):
        """The window over x [x0, x1), rows [ra, rb): (slices, X, ROW) at the pixel centres; None when empty."""
        i0, i1 = int(max(0, self.r1 - np.ceil(rb))), int(min(self.r1 - self.r0, self.r1 - np.floor(ra)))
        j0, j1 = int(max(0, np.floor(x0))), int(min(W, np.ceil(x1)))
        if i0 >= i1 or j0 >= j1:
            return None
        ii, jj = np.mgrid[i0:i1, j0:j1]
        return (slice(i0, i1), slice(j0, j1)), jj + 0.5, (self.r1 - 1 - ii) + 0.5

    def paint(self, sl, m, h, col, kind=None, seam=None):
        m = np.clip(m, 0, 1).astype(np.float32)
        on = m > 0.5
        self.alpha[sl] = np.maximum(self.alpha[sl], m)
        self.alb[sl] = self.alb[sl] * (1 - m[..., None]) + np.asarray(col, np.float32) * m[..., None]
        self.hgt[sl] = np.where(on, h, self.hgt[sl])
        if kind is not None:
            self.kind[sl] = np.where(on, kind, self.kind[sl])
        self.seam[sl] = np.where(on, 0 if seam is None else seam, self.seam[sl])

    def lamp(self, x, row, colour, r=1.6, k=1.0):
        self.lamps.append((x, row, colour, r, k))


def _rim_lamps(f, cx, cr, hx, hy, ch, spacing=38):
    for x, y in along(chamfer_outline(hx, hy, ch), spacing, 3.5):
        f.lamp(cx + x, cr + y, RED_LAMP, 1.3, 0.9)


def _plated(f, sl, X, R, d, cx, cr, hx, hy, ch, z, pattern, tone):
    """A deck surface: plates in Art Deco rings and sectors (or square tiles), a gilded rim, a black edge."""
    q = -d
    m = np.clip(0.5 - d, 0, 1)
    if pattern == "rings":
        ang = np.arctan2(R - cr, X - cx)
        rings = q // 18
        sect = np.floor((ang + np.pi) / (TAU / 16))
        seam = ((q % 18) < 1.2) | (np.abs(((ang + np.pi) % (TAU / 16)) - TAU / 32) > TAU / 32 - 0.9 / np.maximum(
            np.hypot(X - cx, R - cr), 1))
        var = _hash(rings, sect, cx)
    else:
        u, v = X - cx, R - cr
        seam = ((u % 22) < 1.1) | ((v % 22) < 1.1)
        var = _hash(u // 22, v // 22, cx + cr)
    col = tone[None, None, :] * (0.94 + 0.08 * var)[..., None]
    col = np.where(seam[..., None], col * 0.8, col)
    gold = (q < 7) & (q >= 1.5)
    col = np.where(gold[..., None], GOLD, col)
    col = np.where((q < 1.5)[..., None], BLACK, col)
    if pattern == "rings":                                           # a gold inlay ring
        inlay = np.abs(q - (min(hx, hy) * 0.55)) < 1.6
        col = np.where(inlay[..., None], GOLD * 0.96, col)
    h = z * np.clip(q / 2.5, 0, 1) + np.where(gold, 1.5, 0)
    f.paint(sl, m, h, col, 1, np.where(seam & ~gold & (q >= 7), 1.0, 0.0))


def deck(f, cx, cr, a, z=6.0, tone=DECK):
    hx = hy = a
    ch = a * (2 - np.sqrt(2))
    w = f.win(cx - a - 2, cx + a + 2, cr - a - 2, cr + a + 2)
    if w is None:
        return
    sl, X, R = w
    _plated(f, sl, X, R, sd_chamfer(X - cx, R - cr, hx, hy, ch), cx, cr, hx, hy, ch, z, "rings", tone)
    _rim_lamps(f, cx, cr, hx, hy, ch)


def rect(f, x0, x1, r0, r1, ch, z=6.0, tone=DECK, pattern="tiles"):
    cx, cr, hx, hy = (x0 + x1) / 2, (r0 + r1) / 2, (x1 - x0) / 2, (r1 - r0) / 2
    w = f.win(x0 - 2, x1 + 2, r0 - 2, r1 + 2)
    if w is None:
        return
    sl, X, R = w
    _plated(f, sl, X, R, sd_chamfer(X - cx, R - cr, hx, hy, ch), cx, cr, hx, hy, ch, z, pattern, tone)
    _rim_lamps(f, cx, cr, hx, hy, ch, 44)


def plaza(f, cx, cr, hx, hy, ch):
    w = f.win(cx - hx - 2, cx + hx + 2, cr - hy - 2, cr + hy + 2)
    if w is None:
        return
    sl, X, R = w
    _plated(f, sl, X, R, sd_chamfer(X - cx, R - cr, hx, hy, ch), cx, cr, hx, hy, ch, 6.0, "rings", DECK)
    _rim_lamps(f, cx, cr, hx, hy, ch, 46)


def walk(f, x0, r0, x1, r1, wd, under=False):
    pad = wd / 2 + 2
    w = f.win(min(x0, x1) - pad, max(x0, x1) + pad, min(r0, r1) - pad, max(r0, r1) + pad)
    if w is None:
        return
    sl, X, R = w
    vx, vy = x1 - x0, r1 - r0
    ln = max(np.hypot(vx, vy), 1e-6)
    s = np.clip(((X - x0) * vx + (R - r0) * vy) / (ln * ln), 0, 1)
    dist = np.hypot(X - (x0 + s * vx), R - (r0 + s * vy))
    d = dist - wd / 2
    q = -d
    m = np.clip(0.5 - d, 0, 1)
    along_s = s * ln
    planks = DECK_DARK * (0.96 + 0.04 * ((along_s // 12) % 2))[..., None]
    col = np.where((np.abs(dist) < 1.0)[..., None], DECK_DARK * 0.9, planks)
    rail = (q < 2.6) & (q >= 0.9)
    col = np.where(rail[..., None], GOLD, col)
    col = np.where((q < 0.9)[..., None], BLACK, col)
    h = 3.5 * np.clip(q / 2, 0, 1) + np.where(rail, 1.8, 0)
    if under:                                                   # a filler's walkway passes under what stands
        m = m * (f.alpha[sl] < 0.05)
    f.paint(sl, m, h, col, 3, ((along_s % 12) < 1).astype(np.float32))
    for k in range(1, int(ln // 40) + 1):
        u = k * 40 / ln
        if u < 0.97:
            f.lamp(x0 + u * vx, r0 + u * vy, WARM_LAMP, 1.0, 0.55)


def dome(f, cx, cr, rad, kind="glass"):
    w = f.win(cx - rad - 4, cx + rad + 4, cr - rad - 4, cr + rad + 4)
    if w is None:
        return
    sl, X, R = w
    d = np.hypot(X - cx, R - cr)
    base = float(f.hgt[sl].max()) if f.hgt[sl].size else 6.0
    collar = (d < rad + 3) & (d >= rad)
    m = np.clip(rad + 3.5 - d, 0, 1)
    ang = np.arctan2(R - cr, X - cx)
    ribs = np.abs(((ang + np.pi) % (TAU / 12)) - TAU / 24) < 0.6 / np.maximum(d, 1)
    rings = (np.abs(d - rad * 0.42) < 0.9) | (np.abs(d - rad * 0.74) < 0.9)
    tone = GOLD_GLASS if kind == "glass" else CREAM_DOME
    col = np.where((ribs | rings)[..., None], tone * 0.72, tone)
    col = np.where(collar[..., None], CREAM_DOME, col)
    h = np.where(d < rad, base + 0.62 * np.sqrt(np.clip(rad * rad - d * d, 0, None)), base + 2.0)
    f.paint(sl, m, h, col, 2, np.where(ribs & (d < rad), 1.0, 0.0))
    f.rim[sl] |= (d > rad - 3) & (d < rad + 4)
    f.lamp(cx, cr, WARM_LAMP, 1.4, 0.7)


def domelets(f, cx, cr, a):
    r = float(np.clip(a * 0.15, 7, 14))
    for k in range(4):
        ang = np.pi / 4 + k * np.pi / 2
        x, y = cx + 0.64 * a * np.cos(ang), cr + 0.64 * a * np.sin(ang)
        dome(f, x, y, r, "cream")


def pad(f, cx, cr, rad=32):
    w = f.win(cx - rad - 2, cx + rad + 2, cr - rad - 2, cr + rad + 2)
    if w is None:
        return
    sl, X, R = w
    d = np.hypot(X - cx, R - cr)
    m = np.clip(rad + 0.5 - d, 0, 1)
    col = np.zeros(d.shape + (3,)) + SLATE
    ring = (d > rad - 6) & (d < rad - 3.5)
    inner = np.abs(d - rad * 0.5) < 1.1
    bars = ((np.abs(X - cx) < 2.2) | (np.abs(R - cr) < 2.2)) & (d < rad * 0.42)
    col = np.where(ring[..., None], np.array((236, 232, 220.0)), col)
    col = np.where(inner[..., None], np.array((236, 186, 70.0)), col)
    col = np.where(bars[..., None], np.array((232, 228, 214.0)), col)
    col = np.where((d > rad - 1.5)[..., None], BLACK, col)
    f.paint(sl, m, 8.0 * np.clip((rad - d) / 2, 0, 1), col, 4)
    for k in range(8):
        a = k * TAU / 8 + TAU / 16
        f.lamp(cx + (rad - 2.5) * np.cos(a), cr + (rad - 2.5) * np.sin(a), np.array((210, 255, 220.0)), 1.1, 0.7)


def sam(f, cx, cr, rad=21):
    w = f.win(cx - rad - 2, cx + rad + 2, cr - rad - 2, cr + rad + 2)
    if w is None:
        return
    sl, X, R = w
    ang = np.arctan2(R - cr, X - cx)
    sec = TAU / 6
    d = np.hypot(X - cx, R - cr) * np.cos(((ang + sec / 2) % sec) - sec / 2) - rad * np.cos(sec / 2)
    m = np.clip(0.5 - d, 0, 1)
    col = np.zeros(d.shape + (3,)) + np.array((64, 64, 72.0))
    col = np.where((np.abs(d + 4) < 1.6)[..., None], GOLD, col)
    col = np.where((d > -1.2)[..., None], BLACK, col)
    f.paint(sl, m, 7.0 * np.clip(-d / 2, 0, 1), col, 4)
    f.lamp(cx - 9, cr + 9, RED_LAMP, 1.2, 1.0)
    f.lamp(cx + 9, cr - 9, RED_LAMP, 1.2, 1.0)


def spire(f, cx, cr, base):
    tiers = [(1.0, 14, CREAM_DOME), (0.78, 30, GOLD), (0.58, 48, CREAM_DOME), (0.4, 68, GOLD), (0.24, 92, CREAM_DOME)]
    for k, h, col in tiers:
        a = base * k
        w = f.win(cx - a - 2, cx + a + 2, cr - a - 2, cr + a + 2)
        sl, X, R = w
        d = sd_chamfer(X - cx, R - cr, a, a, a * (2 - np.sqrt(2)))
        m = np.clip(0.5 - d, 0, 1)
        q = -d
        c = np.where((q < 2)[..., None], col * 0.7, col)
        c = np.where(((q % 9) < 1)[..., None], c * 0.86, c)
        f.paint(sl, m, h + 4 * np.clip(q / 6, 0, 1), c, 5)
    w = f.win(cx - 5, cx + 5, cr - 5, cr + 5)
    sl, X, R = w
    d = np.hypot(X - cx, R - cr)
    f.paint(sl, np.clip(3.5 - d, 0, 1), 120.0, GOLD, 5)
    f.lamp(cx, cr, RED_LAMP, 1.6, 1.2)


def awning(f, x0, x1, r0, r1):
    w = f.win(x0, x1, r0, r1)
    sl, X, R = w
    q = np.minimum.reduce([X - x0, x1 - X, R - r0, r1 - R])
    col = np.where((((X - x0) // 8) % 2 == 0)[..., None], np.array((236, 232, 222.0)), GOLD)
    col = np.where((q < 1.2)[..., None], BLACK, col)
    base = float(f.hgt[sl].max())
    f.paint(sl, np.clip(q + 0.5, 0, 1), base + 10 + 2 * np.clip(q / 6, 0, 1), col, 1)


def mooring(f, cx, cr):
    w = f.win(cx - 16, cx + 16, cr - 16, cr + 48)
    sl, X, R = w
    d = np.hypot(X - cx, R - cr)
    base = float(f.hgt[sl].max())
    ring = (d < 13) & (d > 8)
    m = np.clip(13.5 - d, 0, 1)
    col = np.where(ring[..., None], GOLD, BLACK + 10)
    f.paint(sl, m, base + np.where(ring, 6.0, 2.0), col, 1)
    for dx in (-6, 0, 6):                                  # cable stubs out to the balloon (north)
        cab = (np.abs(X - (cx + dx * (1 + (R - cr) / 40))) < 0.9) & (R > cr + 12) & (R < cr + 44)
        f.paint(sl, cab.astype(np.float32), base + 7.0, np.array((70, 60, 54.0)), 1)


def inlay(f, cx, cr, rad, wd):
    """A gold inlay ring in a plaza's paving (only over a deck: no height of its own)."""
    w = f.win(cx - rad - wd - 1, cx + rad + wd + 1, cr - rad - wd - 1, cr + rad + wd + 1)
    if w is None:
        return
    sl, X, R = w
    m = (np.abs(np.hypot(X - cx, R - cr) - rad) < wd) & (f.kind[sl] == 1) & (f.alpha[sl] > 0.99)
    f.alb[sl] = np.where(m[..., None], GOLD * 0.95, f.alb[sl])
    f.seam[sl] = np.where(m, 1.0, f.seam[sl])


def pool(f, x0, x1, r0, r1):
    """A reflecting pool in the plaza: dark blue-grey water a step below the paving, a gold kerb."""
    w = f.win(x0, x1, r0, r1)
    sl, X, R = w
    q = np.minimum.reduce([X - x0, x1 - X, R - r0, r1 - R])
    water = np.array((86, 112, 122.0)) * (0.92 + 0.08 * np.sin((R - r0) / 5.0))[..., None]
    col = np.where((q < 3)[..., None], GOLD, water)
    base = float(f.hgt[sl].min())
    f.paint(sl, np.clip(q + 0.5, 0, 1), base + np.where(q < 3, 1.0, -2.0), col, 1)


DRAW = {"inlay": inlay, "pool": pool, "walk": walk, "deck": deck, "rect": rect, "plaza": plaza, "dome": dome,
        "domelets": domelets, "pad": pad,
        "sam": sam, "spire": spire, "awning": awning, "mooring": mooring}


def fillers(f, centres):
    """Smaller decks in the open cloud between the landmarks (about three a screen), each with a dome or four
    domelets and a walkway to the nearest platform; never in the gap before the main deck or on it."""
    rng = np.random.default_rng(3707)
    stop = top(144) + 420
    n = 0
    for _ in range(2600):
        a = rng.uniform(34, 72)
        x, r = rng.uniform(a + 6, W - a - 6), rng.uniform(260, stop - a)
        w = f.win(x - a - 26, x + a + 26, r - a - 26, r + a + 26)
        if w is None or f.alpha[w[0]].max() > 0:
            continue
        near = min(centres, key=lambda c: (c[0] - x) ** 2 + (c[1] - r) ** 2)
        if np.hypot(near[0] - x, near[1] - r) > 420:
            continue
        walk(f, x, r, near[0], near[1], 10, under=True)
        if rng.uniform() < 0.5:
            deck(f, x, r, a)
            dome(f, x, r, a * rng.uniform(0.32, 0.46), "glass" if rng.uniform() < 0.6 else "cream")
        else:
            hx, hy = a * rng.uniform(0.9, 1.4), a * rng.uniform(0.6, 0.9)
            rect(f, x - hx, x + hx, r - hy, r + hy, min(hx, hy) * 0.35)
            domelets(f, x, r, min(hx, hy) * 1.3)
        centres.append((x, r))
        n += 1
    return n


def build_geometry():
    f = Field(ROW_LO, ROW_HI)
    centres = []
    for kind, args in layout():
        DRAW[kind](f, *args)
        if kind in ("deck", "plaza"):
            centres.append(args[:2])
        elif kind == "rect":
            centres.append(((args[0] + args[1]) / 2, (args[2] + args[3]) / 2))
    fillers(f, centres)
    return f


# --------------------------------------------------------------------------- the creep, the turn

def creep(f):
    """Before the turn: teal veins creeping into the dome seams from section 2's end (the Graft Turrets' growth),
    thickening through the plaza."""
    r_a, r_b = top(STARTS[1] + 24), TURN_BASE + SCREEN
    i0, i1 = f.r1 - int(r_b), f.r1 - int(r_a)
    rows = (f.r1 - 1 - np.arange(i0, i1))[:, None]
    k = smoothstep(r_a, TURN_BASE, rows)
    near = blur(f.rim[i0:i1].astype(np.float64), 10.0) * 14
    n = raster.fbm(W, i1 - i0, 24, 3711, octaves=3, period=False)
    v = (f.seam[i0:i1] > 0.5) & (n > 0.62 - 0.14 * k) & (near * k > 0.25) & (f.alpha[i0:i1] > 0.5)
    f.alb[i0:i1] = np.where(v[..., None], f.alb[i0:i1] * 0.4 + TISSUE * 0.6, f.alb[i0:i1])
    f.emis[i0:i1] += (v * k)[..., None].astype(np.float32) * (VEIN * 0.18).astype(np.float32)


class Post:
    """The post-turn look over ground rows [lo, hi): the noise and the fire sites by absolute row, so the growth
    burst's rows and the city's rows after the turn come out alike."""

    def __init__(self, lo, hi):
        self.lo, self.hi = lo, hi
        n = hi - lo
        self.g1 = raster.fbm(W, n, 48, 3721, octaves=4, period=False)
        self.g2 = raster.fbm(W, n, 20, 3722, octaves=3, period=False)
        self.ridge = raster.fbm(W, n, 64, 3723, octaves=4, period=False)
        self.soot = raster.fbm(W, n, 60, 3724, octaves=4, period=False)
        rng = np.random.default_rng(3725)
        self.fires = [(rng.uniform(20, W - 20), rng.uniform(lo, hi), rng.uniform(0.7, 1.2)) for _ in range(n // 45)]
        self.buckle = rng.uniform(0, 1, (n // 22 + 2, W // 22 + 2))

    def rows(self, r_hi, n):
        """Index slice into the noise for the image rows of ground rows (r_hi - n, r_hi] (top row first)."""
        a = self.hi - r_hi
        return slice(a, a + n)

    def apply(self, hgt, alpha, alb, emis, seam, kind, rim, r_top, lamps_out):
        """Apply the turn to arrays whose first row is ground row ``r_top`` (image order)."""
        n = len(hgt)
        sl = self.rows(r_top + 1, n)
        rows = (r_top - np.arange(n))[:, None].astype(np.float64)
        spread = smoothstep(POST_ROW, HALF_LO, rows)                    # the growth spreading toward the fall
        deckish = (alpha > 0.5) & ((kind == 1) | (kind == 3) | (kind == 4))
        g = smoothstep(0.68 - 0.08 * spread, 0.74 - 0.08 * spread, self.g1[sl] + 0.12 * (self.g2[sl] - 0.5)) * deckish
        tissue = TISSUE * (0.8 + 0.4 * self.g2[sl])[..., None]
        alb[:] = alb * (1 - 0.92 * g[..., None]) + tissue * 0.92 * g[..., None]
        hgt[:] = hgt + 1.6 * g
        near = blur(g.astype(np.float64), 6.0)
        vein = (seam > 0.5) & (near > 0.08) & (self.g2[sl] > 0.4) & (alpha > 0.5)
        tend = np.exp(-((self.ridge[sl] - 0.5) / 0.012) ** 2) * (alpha > 0.5) * (kind != 5) * (self.g2[sl] > 0.45)
        vk = np.clip(vein * 0.8 + tend, 0, 1)
        alb[:] = alb * (1 - 0.7 * vk[..., None]) + VEIN * 0.45 * 0.7 * vk[..., None]
        emis[:] += (vk[..., None] * VEIN * 0.24).astype(np.float32)
        # buckled plates: a fifth of the 22 px cells tilted, the lifted edge catching the light, the low one dark
        cols = np.arange(W)[None, :]
        ci, cj = ((rows - self.lo) // 22).astype(int), cols // 22
        b = (self.buckle[np.clip(ci, 0, self.buckle.shape[0] - 1), cj] < 0.2) & deckish & (kind != 3)
        tilt = (np.mod(rows, 22) - 11) / 11 * 2.6 + (np.mod(cols, 22) - 11) / 11 * 1.4
        hgt[:] = hgt + np.where(b, tilt, 0)
        alb[:] = np.where(b[..., None], alb * 0.86, alb)
        # soot
        s = smoothstep(0.52, 0.82, self.soot[sl]) * (alpha > 0.5)
        alb[:] = alb * (1 - 0.6 * s[..., None]) + SOOT * 0.6 * s[..., None]
        # broken domes: dark cracks and holes in the glass
        crack = (kind == 2) & (np.exp(-((self.ridge[sl] - 0.42) / 0.012) ** 2) > 0.3)
        hole = (kind == 2) & (self.g2[sl] > 0.74)
        alb[:] = np.where((crack | hole)[..., None], alb * 0.25, alb)
        # fires on the decks (static: baked glow inside the deck, soot round them)
        for fx, fr, k in self.fires:
            i = int(r_top - fr)
            if not -24 <= i < n + 24:
                continue
            j = int(fx)
            if 0 <= i < n and not deckish[i, j]:
                continue
            yy, xx = np.mgrid[max(0, i - 24):min(n, i + 24), max(0, j - 24):min(W, j + 24)]
            d2 = (yy - i) ** 2 + (xx - j) ** 2
            ring = np.exp(-d2 / (2 * 14.0 ** 2)) * (alpha[yy, xx] > 0.5)
            alb[yy, xx] = alb[yy, xx] * (1 - 0.55 * ring[..., None]) + SOOT * 0.55 * ring[..., None]
            lamps_out.append((fx, fr, FIRE, 9.0 * k, 0.9))
            lamps_out.append((fx + 1, fr + 2, FIRE, 4.0 * k, 1.0))
            lamps_out.append((fx, fr, FIRE_CORE, 2.4 * k, 1.2))


def shade(hgt, alb, ambient=0.42, shadow_len=170):
    """Hill-shade a height field (px) under the top-left sun with cast shadows (backdrop_l29.py ``shade``)."""
    gy = (np.roll(hgt, -1, axis=0) - np.roll(hgt, 1, axis=0)) / 2
    gx = np.gradient(hgt, axis=1)
    n = np.dstack([-gx * 0.9, -gy * 0.9, np.ones_like(hgt)])
    n /= np.linalg.norm(n, axis=-1, keepdims=True)
    diff = np.clip(n @ SUN, 0, 1)
    lit = np.ones_like(hgt)
    hz = np.hypot(SUN[0], SUN[1])
    sx, sy, sz = -SUN[0] / hz, -SUN[1] / hz, SUN[2] / hz
    for s in range(2, shadow_len, 2):
        dx, dy = int(round(sx * s)), int(round(sy * s))                # toward the sun: up and left
        if s * sz > hgt.max():
            break
        shifted = np.roll(np.roll(hgt, dy, axis=0), dx, axis=1)
        if dx > 0:
            shifted[:, :dx] = 0
        lit = np.minimum(lit, np.where(shifted > hgt + s * sz, 0.0, 1.0))
    light = ambient + (1 - ambient) * diff * (0.35 + 0.65 * lit)
    return alb * light[..., None]


def light_lamps(rgb, lamps, r_top):
    """The emissive points (rim lights, lamps, fires) added onto rows whose first is ground row ``r_top``."""
    n = len(rgb)
    for x, row, col, r, k in lamps:
        i = r_top - row
        if -4 * r <= i < n + 4 * r:
            glow(rgb, x, i, r, col, k)


def render_rows(f, i0, i1, lamps, pad=180):
    """The shaded RGB of the field's image rows [i0, i1) (with ``pad`` rows round it for the shadows)."""
    a, b = max(0, i0 - pad), min(len(f.hgt), i1 + pad)
    rgb = shade(f.hgt[a:b].astype(np.float64), f.alb[a:b].astype(np.float64)) * 1.18
    rgb = rgb * 0.9 + np.array((236, 226, 206.0)) * 0.1                  # a step paler (gold on gold)
    rgb += f.emis[a:b]
    light_lamps(rgb, lamps, f.r1 - 1 - a)
    return rgb[i0 - a:i1 - a]


_CITY = {}


def city():
    """(the field's final RGBA uint8 in image order, the field, the post-turn helper, the lamps) - cached."""
    if "rgba" in _CITY:
        return _CITY["rgba"], _CITY["f"], _CITY["post"], _CITY["lamps"]
    f = build_geometry()
    creep(f)
    post = Post(GROW_LO - 200, ROW_HI)
    lamps = list(f.lamps)
    pre_arrays = None
    i_post = f.r1 - POST_ROW                                       # image rows [0, i_post): post-turn
    g0, g1 = f.r1 - GROW_HI - 200, f.r1 - GROW_LO + 200          # the growth burst's rows (pre-turn copy kept)
    pre_arrays = tuple(np.copy(a[g0:g1]) for a in (f.hgt, f.alpha, f.alb, f.emis, f.seam, f.kind, f.rim))
    post_lamps = []
    post.apply(f.hgt[:i_post], f.alpha[:i_post], f.alb[:i_post], f.emis[:i_post], f.seam[:i_post],
               f.kind[:i_post], f.rim[:i_post], f.r1 - 1, post_lamps)
    lamps_all = lamps + post_lamps
    rgb = np.zeros((len(f.hgt), W, 3), np.float32)
    step = 1024
    for i in range(0, len(f.hgt), step):
        rgb[i:i + step] = render_rows(f, i, min(len(f.hgt), i + step), lamps_all)
    alpha = np.clip(np.round(f.alpha * 255), 0, 255).astype(np.uint8)
    out = np.dstack([np.clip(np.round(rgb), 0, 255).astype(np.uint8), alpha])
    out[alpha == 0] = 0
    _CITY.update(rgba=out, f=f, post=post, lamps=(lamps, post_lamps), pre=pre_arrays, g=(g0, g1))
    return out, f, post, (lamps, post_lamps)


def rows_of(rgba, lo, hi):
    """The field's ground rows [lo, hi) as an image (top row = hi - 1)."""
    return rgba[ROW_HI - hi:ROW_HI - lo]


# =========================================================================== the deep layer (one cream)

DEEP_H = 1024


def _band_noise(h, seed):
    v = raster.fbm(W, h, 64, seed, octaves=4, period=True)
    s = raster.fbm(W // 4, h, 16, seed + 1, octaves=3, period=True)
    s = np.array(Image.fromarray(np.clip(s * 255, 0, 255).astype(np.uint8)).resize((W, h), Image.BICUBIC), float) / 255
    return v, s


def cloud_deck_tile(seed=3741):
    """The cream cloud deck far below the city (sections 1-4): cloud heads in the one cream over warm ochre gaps
    under a pale haze; rows wrap (480x1024)."""
    h = DEEP_H
    v, s = _band_noise(h, seed)
    yy = np.arange(h)[:, None]
    phase = TAU * yy / h * 5 + (v - 0.5) * 2.4
    base = raster.ramp(JOV_SUN, np.clip(0.64 + 0.12 * np.sin(phase) + 0.14 * (s - 0.5), 0, 1))
    rng = np.random.default_rng(seed + 2)
    blobs = []
    for _ in range(30):
        blobs += mass_blobs(rng, rng.uniform(-30, W + 30), rng.uniform(0, h), 120, 46, 44, (10, 24))
    shade, cov = cloud_mass(h, W, blobs, seed + 3, wrap=True)
    rgb = base * (1 - 0.85 * cov[..., None]) + raster.ramp(CREAM, 0.15 + 0.85 * shade) * 0.85 * cov[..., None]
    rgb = rgb * 0.8 + jovkit.HAZE * 0.2
    # Step C4a: the field wraps, so where its rows start is free: rolled DECK_ROLL rows, the wrap lands between two
    # calm rows (unrolled, a cloud head's edge sat on rows 0-1 and the game's seam test scored the wrap 1.57 > 1.5).
    rgb = np.roll(rgb, DECK_ROLL, axis=0)
    return Image.fromarray(np.clip(np.round(rgb), 0, 255).astype(np.uint8), "RGB").convert("RGBA")


DECK_ROLL = 6


def deep_span():
    return -40, int(np.ceil(scroll(END + 4) * FACTORS["deep"] + SCREEN))


def cloud_bands(deck_img, seed=3745):
    """Section 5: the cloud deck opening on Jupiter's bands far below (the bands the city sinks into). Its first rows
    are the cloud deck's own, crossfading into the bands over 160 rows, so the seam does not show; it wraps."""
    s5 = seam("deep", len(SECTIONS) - 1)
    lo, hi = s5, deep_span()[1]
    r0 = lo - 400
    n = hi + 1600 - r0
    rows = np.arange(n) + r0
    deck = np.array(deck_img.convert("RGB"), float)
    deck_rows = deck[DEEP_H - 1 - (rows % DEEP_H)]
    v = raster.fbm(W, n, 96, seed, octaves=3, period=False)
    st = jovkit.streaks(n, W, seed + 1)
    phase = np.cumsum(np.full(n, TAU / 170))[:, None]
    val = 0.5 + 0.5 * np.sin(phase + (v - 0.5) * 2.6)
    bands = raster.ramp(JOV_SUN, np.clip(0.42 + 0.42 * (0.62 * val + 0.38 * st), 0, 1))
    rng = np.random.default_rng(seed + 2)
    blobs = []
    for _ in range(int(n / 80)):
        blobs += mass_blobs(rng, rng.uniform(-30, W + 30), rng.uniform(0, n), 70, 22, 18, (5, 12))
    shade, cov = cloud_mass(n, W, blobs, seed + 3)
    bands = bands * (1 - 0.6 * cov[..., None]) + raster.ramp(CREAM, shade) * 0.6 * cov[..., None]
    bands = bands * 0.84 + jovkit.HAZE * 0.16
    wgt = smoothstep(lo, lo + 160, rows)[:, None, None]
    field = deck_rows * (1 - wgt) + bands * wgt
    return jovkit.cut_tile(field, r0, lo, hi)


def smoke_wisps(seed=3747):
    """high-air 480x640: dark smoke streaks drifting over the burning city (the atmosphere's wisps from the turn
    on): smooth alpha at most 0.34, rows wrap."""
    h = 640
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:W].astype(np.float64)
    a = np.zeros((h, W))
    for _ in range(12):
        cx, cy = rng.uniform(-60, W + 60), rng.uniform(0, h)
        length, thick = rng.uniform(160, 340), rng.uniform(8, 20)
        tilt = rng.uniform(-0.25, 0.1)
        for dy in (-h, 0, h):
            u = (xx - cx) / (length / 2)
            v = (yy - cy - dy - (xx - cx) * tilt) / thick
            a = np.maximum(a, np.exp(-v ** 2) * np.clip(1 - u ** 2, 0, 1) ** 1.4)
    tex = raster.fbm(W, h, 32, seed + 1, octaves=4, period=True)
    a = np.clip(a * (0.4 + 0.9 * tex), 0, 1) * 0.34
    rgb = SMOKE[None, None, :] * (0.8 + 0.4 * tex)[..., None]
    return rgba(rgb, blur(a, 1.2, True))


# =========================================================================== pieces (SDF renders)

def _render(job):
    """One SDF job: (kind, args) -> PIL RGBA."""
    from render import scene_models as sm
    from render.sdf import rotate_y, sd_cylinder_z, sd_plate, sd_capsule, sd_sphere, union, Material  # noqa: F401
    kind, args = job
    if kind == "balloon":
        rx, ry = args
        scene, mats, (w, h) = sm.aurelia_balloon(rx, ry)
        w, h = int(np.ceil(w)), int(np.ceil(h))
        hi, f = artkit.render_hi(scene, mats, (w, h), float(w), z_top=float(max(w, h)) * 1.5, steps=150)
        return artkit.native(hi, f, 60)
    if kind == "ring":
        R, r = args
        mats = sm.aurelia_mats()

        def scene(p):
            q = np.hypot(np.hypot(p[:, 0], p[:, 1]) - R, p[:, 2]) - r
            hub = np.abs(np.hypot(p[:, 0], p[:, 1]) - R) < r * 0.4
            m = np.where(hub & (np.abs(np.sin(np.arctan2(p[:, 1], p[:, 0]) * 8)) < 0.18), sm.BLACK, sm.GOLD_M)
            return q, m
        s = int(2 * (R + r) + 8)
        hi, f = artkit.render_hi(scene, mats, (s, s), float(s), z_top=r * 3)
        return artkit.native(hi, f, 60)
    if kind == "spire":
        angle, toward, size, base_x = args
        mats = sm.aurelia_mats()
        oct_ = sm._octagon
        tiers = [(22, 0, 26), (17, 26, 58), (12.5, 58, 92), (8.5, 92, 120), (5, 120, 140)]

        def upright(p):
            items = []
            for k, (r, z0, z1) in enumerate(tiers):
                items.append((sd_plate(p, oct_(r), (z0 + z1) / 2, (z1 - z0) / 2, 1.0),
                              sm.CREAM if k % 2 == 0 else sm.GOLD_M))
                items.append((sd_plate(p, oct_(r + 1.5), z1 - 1, 1.2, 0.5), sm.GOLD_M))
            items.append((sd_capsule(p, (0, 0, 140), (0, 0, 168), 2.4, 0.5), sm.GOLD_M))
            items.append((sd_sphere(p, (0, 0, 169), 1.8), sm.RED_L))
            return union(*items)

        def scene(p):
            q = p.copy()
            q[:, 0] -= base_x
            return upright(rotate_y(q, -toward * angle))
        w, h = size
        hi, f = artkit.render_hi(scene, mats, (w, h), float(w), z_top=200.0, steps=170)
        return artkit.native(hi, f, 50)
    raise ValueError(kind)


SPRITE_JOBS = {}


def renders():
    """Every SDF render, in parallel: balloons, the lift ring, the spires' topple frames."""
    if SPRITE_JOBS:
        return SPRITE_JOBS
    jobs = {"balloon-a": ("balloon", (26, 40)), "balloon-b": ("balloon", (20, 31)), "ring": ("ring", (58, 7))}
    for side, toward, base in (("e", 1, -70.0), ("w", -1, 70.0)):
        for k in range(SPIRE_FRAMES):
            ang = np.radians(78) * smoothstep(0, 1, k / (SPIRE_FRAMES - 1)) ** 1.0
            jobs[f"spire-topple-{side}_{k}"] = ("spire", (ang, toward, SPIRE, base))
    with ProcessPoolExecutor() as pool:
        out = dict(zip(jobs, pool.map(_render, jobs.values())))
    SPRITE_JOBS.update(out)
    return SPRITE_JOBS


SPIRE = (192, 96)
SPIRE_FRAMES = 10


def balloon(name):
    """A tethered lift balloon with a cable stub out of its gondola end (south)."""
    sp = renders()[name]
    img = Image.new("RGBA", (sp.width + 4, sp.height + 18), (0, 0, 0, 0))
    img.alpha_composite(sp, (2, 0))
    d = ImageDraw.Draw(img)
    cx = img.width // 2
    d.line([cx, sp.height - 4, cx + 2, img.height - 1], fill=(70, 60, 54, 255), width=1)
    return artkit.quantize_set([img], 40)[0]


def balloon_tear():
    """10 frames, one-shot: a lift balloon tearing and collapsing (no pop): a ragged rip opens along its top, the
    envelope sags and creases, flaps hang loose, it ends a crumpled shape."""
    sp = renders()["balloon-a"]
    w, h = 96, 128
    frames = []
    base = np.array(sp).astype(np.float64)
    bh, bw = base.shape[:2]
    rng = np.random.default_rng(3761)
    rip_x = np.cumsum(rng.uniform(-1.6, 1.6, bh)) * 0.6 + bw / 2
    for k in range(10):
        u = k / 9
        sy, sx = 1 - 0.5 * u ** 1.2, 1 + 0.18 * u
        img = Image.fromarray(base.astype(np.uint8), "RGBA").resize((int(bw * sx), max(8, int(bh * sy))), Image.BICUBIC)
        a = np.array(img).astype(np.float64)
        hh, ww = a.shape[:2]
        yy, xx = np.mgrid[0:hh, 0:ww]
        creases = 0.5 + 0.5 * np.sin(xx / (3 + 4 * (1 - u)) + yy / 7 + k)
        a[..., :3] *= (1 - 0.35 * u * creases)[..., None]
        rx = np.interp(yy[:, 0] / hh * bh, np.arange(bh), rip_x) * sx
        open_w = 9 * u * np.clip(1 - yy[:, 0] / (hh * (0.25 + 0.7 * u)), 0, 1)
        slit = np.abs(xx - rx[:, None]) < open_w[:, None]
        a[slit, 3] = 0
        edge = (np.abs(np.abs(xx - rx[:, None]) - open_w[:, None]) < 1.2) & (a[..., 3] > 0) & (open_w[:, None] > 0.5)
        a[edge, :3] = a[edge, :3] * 0.5
        a[..., 3] = np.where(a[..., 3] > 127, 255, 0)
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
        c = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        c.alpha_composite(img, ((w - img.width) // 2, 6 + int(26 * u ** 2)))
        d = ImageDraw.Draw(c)
        for side in (-1, 1):                                   # loose flaps hanging from the rip
            if u > 0.2:
                x0 = w / 2 + side * 6 * u
                y0 = 10 + 26 * u ** 2
                d.polygon([(x0, y0), (x0 + side * 14 * u, y0 + 18 * u), (x0 + side * 6 * u, y0 + 30 * u)],
                          fill=(196, 180, 150, 255))
        d.line([w / 2, h - 20 + 8 * u, w / 2 + 3, h - 1], fill=(70, 60, 54, 255), width=1)
        frames.append(c)
    return artkit.quantize_set(frames, 48)


def balloon_torn():
    """Static debris: the torn balloon's last frame darkened (soot) - a crumpled envelope drifting down."""
    f = np.array(balloon_tear()[-1]).astype(np.float64)
    f[..., :3] *= 0.72
    img = Image.fromarray(f.astype(np.uint8), "RGBA")
    return img.crop(img.getbbox())


def lift_ring():
    """The anti-grav ring (a gold torus from above) and its static pale blue glow (additive)."""
    ring = renders()["ring"]
    s = ring.width + 24
    body = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    body.alpha_composite(ring, (12, 12))
    light = np.zeros((s, s, 3))
    yy, xx = np.mgrid[0:s, 0:s].astype(np.float64) + 0.5
    d = np.hypot(xx - s / 2, yy - s / 2)
    R = 58
    light += (np.exp(-((d - R) / 6.0) ** 2) * 0.55 + np.exp(-((d - R) / 16.0) ** 2) * 0.25)[..., None] \
        * np.array((150, 200, 255.0))
    light += (np.exp(-(d / 30.0) ** 2) * 0.18)[..., None] * np.array((150, 200, 255.0))
    return artkit.quantize_set([body], 40)[0], additive_rgb(light)


def spire_frames(side):
    return [renders()[f"spire-topple-{side}_{k}"] for k in range(SPIRE_FRAMES)]


# =========================================================================== pieces (drawn)

def deck_fire(seed=3771):
    """8 frames at 10 fps: a fire on a burning deck - flame tongues rising from a hot base with a smoke puff; the
    flames' bright core in the frame (normal blend: one animated element, no separate glow)."""
    w, h = 48, 64
    rng = np.random.default_rng(seed)
    frames = []
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    tongues = [(rng.uniform(16, 32), rng.uniform(0.7, 1.2), rng.uniform(0, TAU)) for _ in range(5)]
    for k in range(8):
        ph = TAU * k / 8
        heat = np.zeros((h, w))
        for x0, s, p in tongues:
            top_ = h - 14 - 26 * s * (0.75 + 0.25 * np.sin(ph * 2 + p))
            sway = 3 * np.sin(ph + p + (h - yy) / 9)
            u = np.clip((h - 10 - yy) / max(h - 10 - top_, 1), 0, 1)
            wd = (6 * s) * (1 - u) ** 0.8 + 0.5
            heat = np.maximum(heat, np.exp(-((xx - x0 - sway) / wd) ** 2) * (u < 1) * (1 - u ** 2))
        heat = blur(heat, 1.0)
        base = np.exp(-(((xx - 24) / 14) ** 2 + ((yy - (h - 10)) / 4) ** 2))
        heat = np.clip(heat + base, 0, 1)
        col = raster.ramp([(0.0, (120, 30, 10)), (0.4, (230, 90, 24)), (0.7, (255, 170, 60)), (1.0, (255, 240, 190))],
                          heat)
        a = smoothstep(0.05, 0.45, heat)
        smoke = np.exp(-(((xx - 24 - 4 * np.sin(ph)) / 13) ** 2 + ((yy - (14 - 3 * np.sin(ph))) / 11) ** 2)) * 0.5
        sa = smoke * (1 - a)
        rgb = col * a[..., None] + SMOKE * sa[..., None]
        frames.append(rgba(rgb / np.maximum(a + sa, 1e-6)[..., None], np.clip(a + sa, 0, 1)))
    return frames


def war_skirmish(seed=3781):
    """8 frames at 6 fps, deep: gold Ascendancy craft weaving through Vrell swarms far below; small flashes and
    tracers; hazed toward the cream (it is far below the city). Normal blend, one animated element."""
    w, h = 160, 128
    rng = np.random.default_rng(seed)
    craft = [(rng.uniform(30, 130), rng.uniform(30, 100), rng.uniform(18, 34), rng.uniform(0, TAU), rng.choice((-1, 1)))
             for _ in range(4)]
    swarms = [(rng.uniform(20, 140), rng.uniform(20, 108), rng.uniform(0, TAU)) for _ in range(5)]
    frames = []
    for k in range(8):
        ph = TAU * k / 8
        img = Image.new("RGBA", (w * 4, h * 4), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        light = np.zeros((h, w, 3))
        for cx, cy, r, p, s in craft:
            a = p + s * ph
            x, y = cx + r * np.cos(a), cy + r * 0.6 * np.sin(a)
            hd = a + s * np.pi / 2
            pts = [(x + 4 * np.cos(hd), y + 4 * np.sin(hd)), (x + 3 * np.cos(hd + 2.5), y + 3 * np.sin(hd + 2.5)),
                   (x + 3 * np.cos(hd - 2.5), y + 3 * np.sin(hd - 2.5))]
            d.polygon([(px * 4, py * 4) for px, py in pts], fill=(214, 172, 80, 255), outline=(60, 44, 30, 255))
            if (k + int(cx)) % 3 == 0:                          # a burst of tracer fire
                d.line([x * 4, y * 4, (x + 12 * np.cos(hd)) * 4, (y + 12 * np.sin(hd)) * 4], fill=(255, 210, 120, 255),
                       width=2)
        for sx, sy, p in swarms:
            for j in range(14):
                a = p + j * 0.9 + ph * 1.3
                x = sx + 9 * np.cos(a) + 4 * np.sin(j * 2.1 + ph)
                y = sy + 7 * np.sin(a * 1.1) + 3 * np.cos(j * 1.7 + ph)
                d.ellipse([x * 4 - 3, y * 4 - 3, x * 4 + 3, y * 4 + 3], fill=(40, 170, 130, 255))
        for j in range(2):                                      # flashes: a craft or a swarm clump hit
            fx, fy = rng.uniform(20, 140), rng.uniform(20, 108)
            glow(light, fx, fy, 2.5, (255, 220, 160), 1.0)
            glow(light, fx, fy, 5.0, (255, 150, 60), 0.5)
        small = np.array(img.resize((w, h), Image.BOX)).astype(np.float64)
        a = small[..., 3] / 255
        rgb = small[..., :3] / np.maximum(a, 1e-6)[..., None]
        la = np.clip(light.max(axis=-1) / 255, 0, 1)
        rgb = (rgb * a[..., None] + light) / np.maximum(a + la, 1e-6)[..., None]
        a = np.clip(a + la, 0, 1)
        rgb = rgb * 0.62 + jovkit.HAZE * 0.38                    # far below: hazed
        frames.append(rgba(rgb, a * 0.85))
    return frames


def hulk(base_name, seed):
    """8 frames at 8 fps, low-air: a burning Ascendancy hulk tumbling past - the unit's sprite charred, turning a
    full circle over the loop, fire on its hull and a smoke trail streaming up behind it."""
    w, h = 80, 112
    try:
        sp = l23kit.load(base_name)[0]
    except Exception:
        sp = artkit.load_frames(base_name)[0]
    sp = sp.convert("RGBA")
    k_ = 46 / max(sp.size)
    sp = sp.resize((max(1, int(sp.width * k_)), max(1, int(sp.height * k_))), Image.BOX)
    a = np.array(sp).astype(np.float64)
    a[..., :3] = a[..., :3] * 0.5 + SOOT * 0.25
    sp = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    rng = np.random.default_rng(seed)
    frames = []
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    for k in range(8):
        ph = TAU * k / 8
        cx, cy = 40, 78
        trail = np.zeros((h, w))
        for j in range(18):
            ty = cy - 8 - j * 4
            tx = cx + 3 * np.sin(j * 0.5 + ph)
            r = 4 + j * 0.7
            trail = np.maximum(trail, np.exp(-(((xx - tx) / r) ** 2 + ((yy - ty) / r) ** 2)) * (1 - j / 18))
        tex = raster.fbm(w, h, 12, seed + k, octaves=3, period=False)
        ta = np.clip(trail * (0.55 + 0.6 * tex), 0, 1) * 0.7
        img = rgba(SMOKE * (0.8 + 0.4 * tex)[..., None], ta)
        rot = sp.rotate(np.degrees(ph), resample=Image.BICUBIC, expand=True)
        r_ = np.array(rot)
        r_[..., 3] = np.where(r_[..., 3] > 127, 255, 0)
        rot = Image.fromarray(r_, "RGBA")
        img.alpha_composite(rot, (cx - rot.width // 2, cy - rot.height // 2))
        light = np.zeros((h, w, 3))
        for _ in range(3):
            fx, fy = cx + rng.uniform(-10, 10), cy + rng.uniform(-10, 10)
            glow(light, fx, fy, 4.0, FIRE, 0.9)
            glow(light, fx, fy, 1.6, FIRE_CORE, 1.0)
        arr = np.array(img).astype(np.float64)
        la = np.clip(light.max(axis=-1) / 255, 0, 1)
        al = arr[..., 3] / 255
        rgb = (arr[..., :3] * al[..., None] + light) / np.maximum(al + la, 1e-6)[..., None]
        frames.append(rgba(rgb, np.clip(al + la, 0, 1)))
    return frames


def debris(kind, seed):
    """low-air, static: a deck plate torn from the falling city (its gilded edge, scorched, a small fire) or a cable
    coil."""
    rng = np.random.default_rng(seed)
    if kind == "cable":
        img = Image.new("RGBA", (64, 48), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        pts = [(32 + (6 + j * 0.5) * np.cos(j * 0.45), 24 + (5 + j * 0.35) * np.sin(j * 0.45)) for j in range(52)]
        d.line(pts, fill=(64, 56, 50, 255), width=2)
        d.line(pts[::2], fill=(150, 126, 84, 255), width=1)
        return img
    out, _, _, _ = city()
    w, h = {"a": (72, 56), "b": (56, 72), "c": (48, 40)}[kind]
    for _ in range(200):                                     # a plate from the post-turn city's decks
        r = int(rng.uniform(POST_ROW + 200, HALF_LO - 600))
        x = int(rng.uniform(0, W - w))
        patch = rows_of(out, r, r + h)[:, x:x + w]
        if (patch[..., 3] > 0).mean() > 0.97:
            break
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    poly = [(rng.uniform(2, 10), rng.uniform(2, 10)), (w - rng.uniform(2, 12), rng.uniform(2, 8)),
            (w - rng.uniform(2, 8), h * rng.uniform(0.4, 0.6)), (w - rng.uniform(4, 14), h - rng.uniform(2, 8)),
            (rng.uniform(6, 16), h - rng.uniform(2, 10)), (rng.uniform(2, 6), h * rng.uniform(0.3, 0.6))]
    m = Image.new("L", (w, h), 0)
    ImageDraw.Draw(m).polygon(poly, fill=255)
    a = patch.astype(np.float64).copy()
    a[..., 3] = np.array(m)
    a[..., :3] *= 0.7
    edge = (np.array(m.filter(ImageFilter.MinFilter(3))) == 0) & (a[..., 3] > 0)
    a[edge, :3] = GOLD * 0.7
    light = np.zeros((h, w, 3))
    glow(light, w * 0.6, h * 0.45, 3.0, FIRE, 0.9)
    a[..., :3] = np.where(a[..., 3:4] > 0, np.clip(a[..., :3] + light, 0, 255), 0)
    rot = Image.fromarray(a.astype(np.uint8), "RGBA").rotate(rng.uniform(0, 360), resample=Image.NEAREST, expand=True)
    rot = rot.crop(rot.getbbox())
    return rot.crop((0, 0, rot.width + rot.width % 2, rot.height + rot.height % 2))


def far_side(seed=3791):
    """deep 480x240: the city's far side far below in the haze - deck silhouettes, domes, spires and balloons in a
    cream-hazed gold (soft edges all round)."""
    w, h = 480, 240
    rng = np.random.default_rng(seed)
    img = Image.new("RGBA", (w * 2, h * 2), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for _ in range(26):
        x, y, r = rng.uniform(40, 920), rng.uniform(60, 420), rng.uniform(14, 40)
        pts = [(x + r * np.cos(a), y + r * np.sin(a))
               for a in np.linspace(np.pi / 8, TAU + np.pi / 8, 8, endpoint=False)]
        d.polygon(pts, fill=(196, 182, 150, 255), outline=(140, 120, 90, 255))
        if rng.uniform() < 0.7:
            rr = r * 0.4
            d.ellipse([x - rr, y - rr, x + rr, y + rr], fill=(200, 160, 90, 255))
    for _ in range(14):
        x0, y0, x1, y1 = rng.uniform(40, 920), rng.uniform(60, 420), rng.uniform(40, 920), rng.uniform(60, 420)
        d.line([x0, y0, x1, y1], fill=(170, 150, 116, 255), width=3)
    for _ in range(8):
        x, y = rng.uniform(40, 920), rng.uniform(30, 440)
        d.ellipse([x - 10, y - 15, x + 10, y + 15], fill=(226, 214, 186, 255), outline=(170, 140, 90, 255))
    a = np.array(img.resize((w, h), Image.BOX)).astype(np.float64)
    al = a[..., 3] / 255
    rgb = a[..., :3] / np.maximum(al, 1e-6)[..., None]
    rgb = rgb * 0.5 + jovkit.HAZE * 0.5
    return rgba(rgb, al * 0.8 * soft_box(w, h, 40))


# =========================================================================== the turn's growth burst

def _relax(cost, src, iters):
    """A chamfer distance over ``cost`` (per pixel) from ``src`` (bool): repeated min over the 8 neighbours."""
    big = 1e9
    d = np.where(src, 0.0, big)
    steps = [(-1, 0, 1.0), (1, 0, 1.0), (0, -1, 1.0), (0, 1, 1.0), (-1, -1, 1.41), (-1, 1, 1.41), (1, -1, 1.41),
             (1, 1, 1.41)]
    for _ in range(iters):
        prev = d
        for dy, dx, k in steps:
            sh = np.roll(np.roll(d, dy, 0), dx, 1)
            if dy == 1:
                sh[0] = big
            if dy == -1:
                sh[-1] = big
            if dx == 1:
                sh[:, 0] = big
            if dx == -1:
                sh[:, -1] = big
            d = np.minimum(d, sh + k * cost)
        if np.array_equal(prev, d):
            break
    return d


_GROW = {}


def growth_burst():
    """(growth-burst, growth-burst-reveal, growth-burst-glow, bottom row): the post-turn look of the rows on the
    screen at the turn where it differs from the city, each pixel's reveal time (racing from the dome rims along the
    seams over ``TURN_S``; rows near the top edge early, since the rows entering after the turn are grown already),
    and the additive glow of the new teal seams and fires."""
    if _GROW:
        return _GROW["burst"], _GROW["reveal"], _GROW["glow"], GROW_LO
    out, f, post, (lamps, _) = city()
    g0, g1 = _CITY["g"]
    hgt, alpha, alb, emis, seam_, kind, rim = (np.copy(a) for a in _CITY["pre"])
    r_top = f.r1 - 1 - g0
    fire_lamps = []
    post.apply(hgt, alpha, alb, emis, seam_, kind, rim, r_top, fire_lamps)
    rgb = shade(hgt.astype(np.float64), alb.astype(np.float64)) * 1.18
    rgb = rgb * 0.9 + np.array((236, 226, 206.0)) * 0.1 + emis
    light_lamps(rgb, lamps + fire_lamps, r_top)
    a0, a1 = (f.r1 - GROW_HI) - g0, (f.r1 - GROW_LO) - g0
    rgb = rgb[a0:a1]
    base = out[f.r1 - GROW_HI:f.r1 - GROW_LO].astype(np.float64)
    post_rgb = np.clip(np.round(rgb), 0, 255)
    on = base[..., 3] > 0
    diff = (np.abs(post_rgb - base[..., :3]).max(axis=-1) > 2) & on
    burst = np.dstack([np.where(diff[..., None], post_rgb, 0), np.where(diff, base[..., 3], 0)]).astype(np.uint8)
    # the glow: the new emission's bloom (teal seams, fires), beyond the deck edges too
    em_new = np.zeros((len(hgt), W, 3))
    light_lamps(em_new, fire_lamps, r_top)
    vk = (emis - _CITY["pre"][3])[a0:a1].astype(np.float64)
    em = em_new[a0:a1] * 0.55 + vk * 1.2
    light = np.dstack([blur(em[..., c], 3.0) for c in range(3)]) * 0.9
    glow_img = additive_rgb(light)
    # the reveal map, at half resolution
    hh = (a1 - a0) // 2
    rows = (GROW_HI - 1 - np.arange(a1 - a0))[:, None]
    m = (base[..., 3] > 0)[: hh * 2].reshape(hh, 2, W // 2, 2).max(axis=(1, 3))
    sm_ = (seam_[a0:a1] > 0.5)[: hh * 2].reshape(hh, 2, W // 2, 2).max(axis=(1, 3))
    src = rim[a0:a1][: hh * 2].reshape(hh, 2, W // 2, 2).max(axis=(1, 3)) & m
    cost = np.where(m, np.where(sm_, 1.0, 2.4), 30.0)
    d = _relax(cost, src, 400)
    d = np.repeat(np.repeat(d, 2, 0), 2, 1)
    d = np.vstack([d, d[-1:]])[: a1 - a0]
    lim = np.percentile(d[diff], 97) if diff.any() else 1.0
    rev = np.clip(d / max(lim, 1e-6), 0, 1)
    rev = rev * smoothstep(POST_ROW + 2, POST_ROW - 240, rows)            # near the top edge early
    rev = blur(rev, 1.0)
    shown = diff | (np.array(glow_img)[..., 3] > 0)
    v = np.clip(np.round(rev * 255), 0, 255).astype(np.uint8)
    reveal = np.dstack([v, v, v, np.where(shown, 255, 0).astype(np.uint8)])
    reveal[~shown] = 0
    _GROW.update(burst=Image.fromarray(burst, "RGBA"), reveal=Image.fromarray(reveal, "RGBA"), glow=glow_img)
    return _GROW["burst"], _GROW["reveal"], _GROW["glow"], GROW_LO


# =========================================================================== the fall

# Round 75 F.6 redo (step ART-C3; the user: "The platform should disappear in the clouds completely. During the fall
# it should fade slowly. Also, in the last frame some parts are cut off at the bottom."): the halves now carry the whole
# fall themselves - one curve over FALL_SPAN s (t 160-196) that slows, shrinks and darkens them as before but fades
# their alpha slowly from the break to 0 by FALL_GONE s, keeps every pixel of them on the screen from FALL_WHOLE s on
# (ART-C2's 20 s curve ended with the east half's bottom on the screen's edge and handed over to the 256x208
# sinking-city on deep, whose canvas cut the halves' tops and bottoms off and which then slid out through the bottom
# edge at the deep layer's 15 px/s), and two cloud-top veils close over them (FALL_VEILS). The sinking city is no
# longer drawn in Level 37; its files stay (frozen at ART-C2's curve end, R75A_HALVES) as Level 38's source.
FALL_HALVES = {   # the two city layers (A.13 b): west sinks faster and darker, east a little slower
    "west": dict(scale=0.20, drift=-40, turn=-7.0, shade=0.52, tau=0.85, floor=0.10),
    "east": dict(scale=0.23, drift=36, turn=6.0, shade=0.46, tau=1.05, floor=0.12),
}
FALL_SPAN = 36.0          # the halves' keys run u 0..FALL_SPAN (the data's fall.seconds)
FALL_GONE = 34.0          # their alpha reaches 0 here (t 194): >= 6 s of calm before the level end
FALL_FADE_FROM = 1.5      # ...from 1 at this u (the handover and the break stay whole)
FALL_WHOLE = 12.5         # from this u every pixel of each half (its rim's pad, turned) is on the screen
FALL_SHRINK_TAU = 12.0    # the shrink's time scale (front-loaded: most of it in the first 20 s)
FALL_CENTRE_Y = 160.0     # the point the halves shrink toward (y down): above the field's centre, so they end whole
FALL_SINK_V = 5.5         # px/s: the late sinking on the screen (eased in round u = FALL_SINK_FROM)
FALL_SINK_FROM = 11.0
R75A_HALVES = {   # ART-C2's curve (round 75 a), kept for the frozen sinking-city (Level 38's aurelia-wreck source)
    "west": dict(scale=0.38, drift=-30, turn=-6.0, shade=0.64, tau=0.85, floor=0.10),
    "east": dict(scale=0.46, drift=26, turn=5.0, shade=0.56, tau=1.05, floor=0.12),
}
R75A_SECONDS = 20.0
FALL_DARK = np.array((30, 22, 22.0))                 # what a half darkens toward (shade = its share)


def fall_cut():
    """The jagged fault: the cut's x on every row of the main deck [HALF_LO, HALF_HI) - sharp zigzags 9-24 rows
    apart round a line that runs between the two great domes (never straight)."""
    rng = np.random.default_rng(3801)
    pts = []
    r, side = HALF_LO - 20, 1
    while r < HALF_HI + 40:
        u = (r - HALF_LO) / (HALF_HI - HALF_LO)
        bx = 262 - 30 * u + 14 * np.sin(u * 7.0)
        pts.append((r, bx + side * rng.uniform(5, 15)))
        side = -side
        r += rng.uniform(9, 24)
    rows = np.arange(HALF_LO, HALF_HI)
    return np.interp(rows, [p[0] for p in pts], [p[1] for p in pts])


_FALL = {}


def halves():
    """{side: (image, x0, bottom row)}: the main deck's two halves, the city's own pixels (their union is the city
    on those rows), each cropped to its box."""
    if _FALL:
        return _FALL
    out, _, _, _ = city()
    deck_rows = rows_of(out, HALF_LO, HALF_HI)                      # image order: top row = HALF_HI - 1
    cut = fall_cut()[::-1]                                          # per image row
    xx = np.arange(W)[None, :] + 0.5
    west = xx < cut[:, None]
    for side, mask in (("west", west), ("east", ~west)):
        a = deck_rows.copy()
        a[~mask] = 0
        img = Image.fromarray(a, "RGBA")
        x0, y0, x1, y1 = img.getbbox()
        _FALL[side] = (img.crop((x0, y0, x1, y1)), x0, HALF_HI - y1)
    return _FALL


def _cut_points(side):
    """The cut's points on a half's edge in that half's image (x, y), every row."""
    img, x0, bottom = halves()[side]
    cut = fall_cut()
    pts = []
    for k, row in enumerate(range(HALF_LO, HALF_HI)):
        y = (bottom + img.height - 1) - row
        if 0 <= y < img.height:
            x = cut[k] - x0 + (-0.5 if side == "west" else 0.5)
            xi = int(np.clip(x, 0, img.width - 1))
            if np.array(img)[y, xi, 3] > 0:
                pts.append((x, y))
    return pts


CHAR_PAD = 14


def half_char(side, seed):
    """The torn rim over a half (normal blend, its image + CHAR_PAD a side): charred plate along the cut, flaps of
    torn plating bent outward past the cut, smoke clinging to both edges (smooth)."""
    img, _, _ = halves()[side]
    w, h = img.width + 2 * CHAR_PAD, img.height + 2 * CHAR_PAD
    rng = np.random.default_rng(seed)
    out = np.zeros((h, w, 4))
    body = np.zeros((h, w))
    body[CHAR_PAD:CHAR_PAD + img.height, CHAR_PAD:CHAR_PAD + img.width] = np.array(img)[..., 3] > 0
    pts = _cut_points(side)
    if not pts:
        return Image.fromarray(out.astype(np.uint8), "RGBA")
    px = np.array([p[0] for p in pts]) + CHAR_PAD
    py = np.array([p[1] for p in pts]) + CHAR_PAD
    cutx = np.full(h, np.nan)
    cutx[py.astype(int)] = px
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    out_dir = 1 if side == "west" else -1                            # toward the gap
    valid = ~np.isnan(cutx)
    cx = np.interp(np.arange(h), np.arange(h)[valid], cutx[valid])
    dist = (cx[:, None] - xx) * out_dir                             # > 0 inside the half
    n = raster.fbm(w, h, 8, seed + 1, octaves=3, period=False)
    band = (dist > -0.5) & (dist < 6 + 7 * n) & (body > 0) & (yy >= py.min()) & (yy <= py.max())
    char = np.array((44, 30, 26.0)) * (0.8 + 0.4 * n)[..., None]
    k = np.clip(1 - dist / (6 + 7 * n), 0, 1) * 0.85
    out[..., :3] = np.where(band[..., None], char, 0)
    out[..., 3] = np.where(band, k, 0)
    pil = Image.fromarray(np.dstack([np.clip(out[..., :3], 0, 255),
                                     np.clip(out[..., 3] * 255, 0, 255)]).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(pil)
    y = py.min() + rng.uniform(4, 16)
    while y < py.max() - 8:                                         # torn plate flaps, bent outward
        y0, y1 = int(y), int(min(py.max(), y + rng.uniform(6, 12)))
        xa, xb = cx[y0], cx[y1]
        tip = rng.uniform(5, 10)
        tipy = (y0 + y1) / 2 + rng.uniform(-3, 3)
        poly = [(xa, y0), (xb, y1), ((xa + xb) / 2 + out_dir * tip, tipy)]
        lit = (226, 212, 176) if rng.uniform() < 0.5 else (150, 136, 108)
        d.polygon(poly, fill=lit + (255,), outline=(52, 40, 34, 255))
        y += rng.uniform(14, 30)
    a = np.array(pil).astype(np.float64)
    smoke = np.zeros((h, w))
    for _ in range(int(len(pts) / 16) + 2):                       # smoke clinging to the edge
        j = rng.integers(0, len(pts))
        sx, sy = px[j] + out_dir * rng.uniform(-4, 6), py[j]
        r = rng.uniform(5, 11)
        smoke = np.maximum(smoke, np.exp(-(((xx - sx) / r) ** 2 + ((yy - sy) / (r * 1.4)) ** 2)))
    sa = np.clip(smoke * (0.5 + 0.6 * n), 0, 1) * 0.5
    al = a[..., 3] / 255
    rgb = (a[..., :3] * al[..., None] + SMOKE * sa[..., None] * (1 - al[..., None])) / np.maximum(
        al + sa * (1 - al), 1e-6)[..., None]
    return rgba(rgb, np.clip(al + sa * (1 - al), 0, 1))


def half_glow(side, seed, frames=4):
    """4 frames, additive (image + CHAR_PAD a side): the hot-glowing rim along the cut and the fires venting from it,
    flickering."""
    img, _, _ = halves()[side]
    w, h = img.width + 2 * CHAR_PAD, img.height + 2 * CHAR_PAD
    pts = _cut_points(side)
    rng = np.random.default_rng(seed)
    sites = [pts[i] for i in rng.integers(0, len(pts), 7)] if pts else []
    out = []
    for k in range(frames):
        light = np.zeros((h, w, 3))
        for j, (x, y) in enumerate(pts[::2]):
            fl = 0.75 + 0.25 * np.sin(j * 0.7 + k * TAU / frames + side.__len__())
            glow(light, x + CHAR_PAD, y + CHAR_PAD, 1.4, (255, 120, 40), 0.5 * fl)
        for j, (x, y) in enumerate(sites):
            fl = 0.6 + 0.4 * np.sin(k * TAU / frames + j * 1.9)
            glow(light, x + CHAR_PAD, y + CHAR_PAD, 7.0 * fl + 2, FIRE, 0.7)
            glow(light, x + CHAR_PAD, y + CHAR_PAD - 2, 2.4, FIRE_CORE, 0.9 * fl)
        out.append(additive_rgb(light))
    return out


def fall_shards(seed=3811):
    """8 frames at 8 fps, 96x160: shards of plating tumbling in the widening gap (gold, cream, a few still glowing),
    fading in at the top and out at the bottom so the loop never pops."""
    w, h = 96, 160
    rng = np.random.default_rng(seed)
    shards = [(rng.uniform(10, 86), rng.uniform(0, h), rng.uniform(2, 5), rng.uniform(0, TAU), rng.choice((-1, 1)),
               rng.uniform() < 0.25) for _ in range(16)]
    frames = []
    for k in range(8):
        img = Image.new("RGBA", (w * 4, h * 4), (0, 0, 0, 0))
        d = ImageDraw.Draw(img)
        for x, y0, s, a0, sp, hot in shards:
            y = (y0 + k * h / 8) % h
            fade = smoothstep(0, 18, y) * smoothstep(h, h - 18, y)
            a = a0 + sp * k * TAU / 8
            pts = [((x + s * np.cos(a + j * 2.2)) * 4, (y + s * 0.7 * np.sin(a + j * 2.2)) * 4) for j in range(3)]
            col = (255, 150, 60) if hot else ((220, 200, 150) if (k + int(x)) % 2 else (150, 130, 96))
            d.polygon(pts, fill=col + (int(255 * fade),))
        frames.append(img.resize((w, h), Image.BOX))
    return frames


def fall_keys(side):
    """The half's curve: [[u, x, y, scale, turn, shade, alpha], ...] every 0.1 s for the first 2 s, then every 0.5 s to
    FALL_SPAN (u s after fall.t; x, y the screen centre of its image, y down; scale about its centre; turn degrees
    clockwise; shade the share it has darkened toward FALL_DARK; alpha its opacity, rim and glow included). The first
    key is where the ground has it at fall.t, its speed the ground's. It lags behind the ground as before (slowing to
    ``floor`` of its speed), shrinks toward (240, FALL_CENTRE_Y) - fast at first, then slowly to ``scale`` -, sinks on
    the screen at FALL_SINK_V late, and fades from FALL_FADE_FROM to 0 at FALL_GONE (smooth, linear between keys)."""
    p = FALL_HALVES[side]
    img, x0, bottom = halves()[side]
    xc = x0 + img.width / 2
    yc = SCREEN - (bottom - FALL_BASE) - img.height / 2
    v0 = CLOCK.speed_at(FALL_T + 1e-3)
    keys = []
    us = [k / 10 for k in range(20)] + [2 + k / 2 for k in range(int((FALL_SPAN - 2) * 2) + 1)]
    for u in us:
        y_pre = yc + v0 * (p["floor"] * u + (1 - p["floor"]) * p["tau"] * (1 - np.exp(-u / p["tau"])))
        s = p["scale"] + (1 - p["scale"]) * (0.8 * np.exp(-(u / FALL_SHRINK_TAU) ** 2)
                                             + 0.2 * (1 - smoothstep(0, FALL_SPAN, u)))
        x = 240 + (xc - 240) * s + p["drift"] * smoothstep(0, 24.0, u)     # the gap stays open as they shrink
        sink = FALL_SINK_V * 3 * (np.log1p(np.exp((u - FALL_SINK_FROM) / 3)) - np.log1p(np.exp(-FALL_SINK_FROM / 3)))
        y = FALL_CENTRE_Y + (y_pre - FALL_CENTRE_Y) * s + sink
        alpha = 1 - smoothstep(FALL_FADE_FROM, FALL_GONE, u) ** 1.5           # slow at first, then into the veils
        keys.append([round(u, 1), round(float(x), 1), round(float(y), 1), round(float(s), 4),
                     round(float(p["turn"] * smoothstep(0.5, FALL_SPAN, u)), 2),
                     round(float(p["shade"] * smoothstep(0, 24.0, u) ** 0.8), 3), round(float(alpha), 3)])
    keys[0][3], keys[0][6] = 1, 1
    return keys


def _r75a_end(side):
    """ART-C2's curve's last key (u = 20 s), [u, x, y, scale, turn, shade]: the frozen sinking city's layout."""
    p = R75A_HALVES[side]
    img, x0, bottom = halves()[side]
    xc = x0 + img.width / 2
    yc = SCREEN - (bottom - FALL_BASE) - img.height / 2
    v0 = CLOCK.speed_at(FALL_T + 1e-3)
    u = R75A_SECONDS
    y_pre = yc + v0 * (p["floor"] * u + (1 - p["floor"]) * p["tau"] * (1 - np.exp(-u / p["tau"])))
    s = p["scale"]
    x = 240 + (xc + p["drift"] - 240) * s
    y = SCREEN / 2 + (y_pre - SCREEN / 2) * s
    return [u, round(float(x), 1), round(float(y), 1), round(float(s), 4), round(float(p["turn"]), 2),
            round(float(p["shade"]), 3)]


def half_extent(side, key):
    """(left, top, right, bottom) on the screen of a half drawn at ``key`` with its rim's pad (turned, scaled)."""
    img = halves()[side][0]
    _, x, y, s, turn = key[:5]
    th = np.radians(turn)
    w, h = img.width + 2 * CHAR_PAD, img.height + 2 * CHAR_PAD
    hw = s * (w / 2 * abs(np.cos(th)) + h / 2 * abs(np.sin(th)))
    hh = s * (h / 2 * abs(np.cos(th)) + w / 2 * abs(np.sin(th)))
    return x - hw, y - hh, x + hw, y + hh


def transformed(img, key, dark=FALL_DARK):
    """A half (or its overlay) drawn as its key says: darkened, turned, scaled (bilinear: it is far below)."""
    s, turn, sh = key[3:6]
    a = np.array(img).astype(np.float64)
    if dark is not None:
        a[..., :3] = a[..., :3] * (1 - sh) + dark * sh
    im = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    if turn:
        im = im.rotate(-turn, resample=Image.BICUBIC, expand=True)
    if s != 1:
        im = im.resize((max(1, int(round(im.width * s))), max(1, int(round(im.height * s)))), Image.BILINEAR)
    return im


SINK = (256, 208)


def sinking_city(seed=3821):
    """deep 256x208 + 4 glow frames: the two halves as their curves leave them (dark, small, turned), hazed a step
    toward the bands, smoke rising off them (smooth), the fires' glow. Returns (body, glows, centre on the screen at
    the fall's end)."""
    hv = halves()
    ends = {side: _r75a_end(side) for side in hv}
    cx = np.mean([ends[s][1] for s in hv])
    cy = np.mean([ends[s][2] for s in hv])
    w, h = SINK
    canvas = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for side in ("west", "east"):
        im = transformed(hv[side][0], ends[side])
        canvas.alpha_composite(im, (int(round(ends[side][1] - cx + w / 2 - im.width / 2)),
                                    int(round(ends[side][2] - cy + h / 2 - im.height / 2))))
    a = np.array(canvas).astype(np.float64)
    al = a[..., 3] / 255
    rgb = a[..., :3] * 0.8 + jovkit.HAZE * 0.06
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64) + 0.5
    n = raster.fbm(w, h, 24, seed, octaves=4, period=False)
    rng = np.random.default_rng(seed)
    smoke = np.zeros((h, w))
    for _ in range(14):
        sx, sy = w / 2 + rng.uniform(-60, 60), h / 2 + rng.uniform(-40, 30)
        r = rng.uniform(14, 30)
        smoke = np.maximum(smoke, np.exp(-(((xx - sx) / r) ** 2 + ((yy - sy + 20) / (r * 1.5)) ** 2)))
    sa = np.clip(smoke * (0.45 + 0.7 * n), 0, 1) * 0.55 * soft_box(w, h, 30)
    out_a = np.clip(al + sa * (1 - al), 0, 1)
    out_rgb = (rgb * al[..., None] + SMOKE * 0.8 * (sa * (1 - al))[..., None]) / np.maximum(out_a, 1e-6)[..., None]
    body = rgba(out_rgb, out_a * soft_box(w, h, 6))
    sites = [(w / 2 + rng.uniform(-50, 50), h / 2 + rng.uniform(-36, 36)) for _ in range(30)]
    sites = [p for p in sites if al[int(p[1]), int(p[0])] > 0.5][:9]
    glows = []
    for k in range(4):
        light = np.zeros((h, w, 3))
        for j, (x, y) in enumerate(sites):
            fl = 0.6 + 0.4 * np.sin(k * TAU / 4 + j * 2.3)
            glow(light, x, y, 4.0 * fl + 1.5, FIRE, 0.6)
            glow(light, x, y, 1.4, FIRE_CORE, 0.7 * fl)
        glows.append(additive_rgb(light))
    return body, glows, (float(cx), float(cy))


VEIL = (288, 196)
HAZE_COLOUR = np.array((0x5a, 0x44, 0x3c), float)   # the proposal's haze_colour and the smoke atmosphere's haze, which
SMOKE_HAZE = 0.35                                   # the game draws over deep only: a veil on ground carries it itself
FALL_VEILS = {   # the cloud tops closing over the sinking halves (normal blend, over the halves, under low-air)
    # dx: from..to px beside the pair's centre over u0..u1; dy below it; alpha 0 at u0 -> 1 at u_full, held to
    # u_hold, -> 0 at u_end (each veil fades into the deck after the halves are gone)
    "west": dict(seed=3841, dx=(-140, -50), dy=10, u0=10.0, u_full=24.0, u_hold=30.0, u_end=36.5),
    "east": dict(seed=3843, dx=(140, 46), dy=-14, u0=12.0, u_full=26.0, u_hold=31.0, u_end=37.5),
}


def fall_veil(side):
    """288x196: a patch of the cloud deck's own heads drifting over the city (cloud_deck_tile's look: cumulus heads in
    the one cream, lit from the top left, hazed a fifth toward the pale haze and by the smoke atmosphere's haze as the
    deck is in the game), a few clusters, denser toward the
    halves' side and broken into separate heads toward its outer side; its alpha the heads' coverage up to 0.92, 0 on
    its edges (smooth, no ordered dither)."""
    v = FALL_VEILS[side]
    w, h = VEIL
    rng = np.random.default_rng(v["seed"])
    inner = 1 if side == "west" else -1                              # toward the pair's centre
    blobs = []
    for cx, cy, rx, ry, n in ((w / 2 + inner * 30, h / 2, 96, 54, 64), (w / 2 + inner * 70, h / 2 - 34, 60, 30, 24),
                              (w / 2 + inner * 64, h / 2 + 40, 64, 30, 26), (w / 2 - inner * 70, h / 2 - 20, 50, 34, 18),
                              (w / 2 - inner * 92, h / 2 + 34, 34, 22, 10)):
        blobs += mass_blobs(rng, cx, cy, rx, ry, n, (10, 22))
    shade, cov = cloud_mass(h, w, blobs, v["seed"] + 1, detail=0.45)
    rgb = raster.ramp(CREAM, np.clip(0.15 + 0.85 * shade, 0, 1)) * 0.8 + jovkit.HAZE * 0.2
    rgb = rgb * (1 - SMOKE_HAZE) + HAZE_COLOUR * SMOKE_HAZE     # the deck's smoke haze baked in (drawn on ground)
    a = np.clip(cov, 0, 1) * 0.92 * soft_box(w, h, 10)
    return rgba(rgb, a)


def fall_veil_keys(side):
    """[[u, x, y, alpha], ...] every 0.5 s over u0..u_end: the veil's centre on the screen (y down) beside the halves'
    pair (their keys' mean centre), sliding in from its side, and its opacity."""
    v = FALL_VEILS[side]
    kw, ke = np.array(fall_keys("west"), float), np.array(fall_keys("east"), float)
    keys = []
    u = v["u0"]
    while u <= v["u_end"] + 1e-9:
        px = (np.interp(u, kw[:, 0], kw[:, 1]) + np.interp(u, ke[:, 0], ke[:, 1])) / 2
        py = (np.interp(u, kw[:, 0], kw[:, 2]) + np.interp(u, ke[:, 0], ke[:, 2])) / 2
        dx = v["dx"][0] + (v["dx"][1] - v["dx"][0]) * smoothstep(v["u0"], v["u_hold"], u)
        a = smoothstep(v["u0"], v["u_full"], u) * (1 - smoothstep(v["u_hold"], v["u_end"], u))
        keys.append([round(u, 1), round(float(px + dx), 1), round(float(py + v["dy"]), 1), round(float(a), 3)])
        u += 0.5
    return keys


# =========================================================================== the city's ground pieces

def chunk_rows():
    """The city's ground pieces: (bottom, top) ground rows, back to back over every row with something on it, at
    most MAX_PIECE rows each (a run longer than that is split at its emptiest row)."""
    out = city()[0]
    count = (out[..., 3] > 0).sum(axis=1)[::-1]                      # per ground row, ROW_LO upward
    occ = count > 0
    runs, r = [], 0
    n = len(occ)
    while r < n:
        if not occ[r]:
            r += 1
            continue
        s = r
        gap = 0
        while r < n and gap < 8:
            gap = 0 if occ[r] else gap + 1
            r += 1
        e = r - gap
        runs.append((s, e))
    pieces = []
    for s, e in runs:
        while e - s > MAX_PIECE:
            lo = s + MAX_PIECE - 400
            cut = lo + int(np.argmin(count[lo:s + MAX_PIECE]))
            pieces.append((s, cut))
            s = cut
        pieces.append((s, e))
    return [(a + ROW_LO, b + ROW_LO) for a, b in pieces]


def city_pieces():
    out = city()[0]
    return {f"city-{k + 1:02d}": (Image.fromarray(rows_of(out, lo, hi), "RGBA"), lo) for k, (lo, hi) in
            enumerate(chunk_rows())}


# =========================================================================== build

PIECES = {}   # name -> spec (layer, size, frames, fps)


def build_all():
    OUT.mkdir(parents=True, exist_ok=True)
    for old in OUT.glob("*.png"):
        old.unlink()
    PIECES.clear()

    def save(name, img, layer=None, frames=None, fps=None):
        artkit.save_png(img, OUT / f"{name}.png", SOURCE)
        if layer and frames is None:
            PIECES[name] = {"layer": layer, "size": list(img.size)}

    def save_frames(name, frames, layer, fps, one_shot=False):
        for i, f in enumerate(frames):
            artkit.save_png(f, OUT / f"{name}_{i}.png", SOURCE)
        PIECES[name] = {"layer": layer, "size": list(frames[0].size), "frames": len(frames), "fps": fps}
        if one_shot:
            PIECES[name]["one_shot"] = True

    deck = cloud_deck_tile()
    save("cloud-deck", deck)
    save("cloud-bands", cloud_bands(deck))
    save("smoke-wisps", smoke_wisps())
    for name, (img, _) in city_pieces().items():
        save(name, img, "ground")
    save("far-side", far_side(), "deep")
    for name in ("balloon-a", "balloon-b"):
        save(name, balloon(name), "low-air")
    save("balloon-torn", balloon_torn(), "low-air")
    ring, ring_glow = lift_ring()
    save("lift-ring", ring, "low-air")
    save("lift-ring-glow", ring_glow, "low-air")
    save_frames("deck-fire", deck_fire(), "ground", 10)
    save_frames("war-skirmish", war_skirmish(), "deep", 6)
    save_frames("hulk-a", hulk("gilded-gunship", 3791), "low-air", 8)
    save_frames("hulk-b", hulk("talon", 3793), "low-air", 8)
    for k, kind in enumerate(("a", "b", "c")):
        save(f"debris-plate-{kind}", debris(kind, 3801 + k), "low-air")
    save("debris-cable", debris("cable", 3805), "low-air")
    burst, reveal, glow_img, _ = growth_burst()
    save("growth-burst", burst, "ground")
    save("growth-burst-reveal", reveal, "ground")
    save("growth-burst-glow", glow_img, "ground")
    for side in ("west", "east"):
        img, _, _ = halves()[side]
        save(f"fall-half-{side}", img, "ground")
        save(f"fall-half-{side}-char", half_char(side, 3831 if side == "west" else 3833), "ground")
        save_frames(f"fall-half-{side}-glow", half_glow(side, 3835 if side == "west" else 3837), "ground", 8)
    save_frames("fall-shards", fall_shards(), "ground", 8)
    save_frames("balloon-tear", balloon_tear(), "low-air", 5, one_shot=True)
    for side in ("w", "e"):
        save_frames(f"spire-topple-{side}", spire_frames(side), "low-air", 5, one_shot=True)
    body, glows, _ = sinking_city()
    save("sinking-city", body, "deep")
    save_frames("sinking-city-glow", glows, "deep", 6)
    for side in ("west", "east"):
        save(f"fall-veil-{side}", fall_veil(side), "ground")
    for name, spec in PIECES.items():
        print(f"{name}: {spec['size'][0]}x{spec['size'][1]}" + (f" x{spec['frames']}" if spec.get("frames") else ""))
    return dict(PIECES)


# =========================================================================== the places

def deck_x(t, want, kinds=(1,), y_down=270, reach=120):
    """An x on a deck (of ``kinds``) at screen y (down) at t, nearest ``want``."""
    out, f, _, _ = city()
    row = int(round(at(t, y_down)))
    i = f.r1 - 1 - row
    best = None
    for dx in range(0, reach):
        for x in (want - dx, want + dx):
            if 20 <= x < W - 20 and all(f.kind[i + dy, x + ddx] in kinds and f.alpha[i + dy, x + ddx] > 0.9
                                        for dy in (-14, 0, 14) for ddx in (-12, 0, 12)):
                best = x
                break
        if best is not None:
            break
    return best if best is not None else want


def placements():
    """[(piece, t, x, note)] of the placed pieces (city chunks by exact rows)."""
    out = []
    for k, (lo, hi) in enumerate(chunk_rows()):
        out.append((f"city-{k + 1:02d}", exact_t(lo, hi - lo), 240,
                    f"the city, ground rows {lo}..{hi - 1} (exact: its bottom row on {lo})"))
    fd = FACTORS["deep"]
    out.append(("far-side", t_centre_at(4.0, 150, fd), 240, "the city's far side far below in the haze (section 1)"))
    out.append(("war-skirmish", t_centre_at(122.0, 270, fd), 130,
                "the backdrop war far below: gold craft and Vrell swarms (on the screen t~100-144)"))
    la = FACTORS["low-air"]
    for t, x, name in ((3.0, 430, "balloon-a"), (9.0, 50, "balloon-b"), (17.0, 440, "balloon-b"),
                       (27.0, 40, "balloon-a"), (38.0, 450, "balloon-a"), (51.0, 30, "balloon-b"),
                       (66.0, 446, "balloon-b"), (76.0, 36, "balloon-a"), (90.0, 446, "balloon-a")):
        out.append((name, t_centre_at(t, 270, la), x, None))
    for t, x in ((20.0, 380), (58.0, 100)):
        out.append(("lift-ring", t_centre_at(t, 270, la), x, "an anti-grav ring; lift-ring-glow at the same place"))
        out.append(("lift-ring-glow", t_centre_at(t, 270, la), x, None))
    for t, x, name in ((110.0, 380, "hulk-a"), (126.0, 80, "hulk-b"), (140.0, 400, "hulk-a")):
        out.append((name, t_centre_at(t, 270, la), x, "a burning hulk falling past"))
    for t, want in ((104.0, 330), (118.0, 120), (133.0, 380), (148.0, 160)):
        out.append(("deck-fire", round(t, 3), deck_x(t, want), "a fire on a burning deck"))
    for t, x, name in ((166.0, 110, "debris-plate-a"), (171.0, 380, "debris-plate-b"), (174.0, 300, "debris-cable"),
                       (176.0, 190, "debris-plate-c"), (168.0, 50, "balloon-torn"), (183.0, 340, "debris-plate-a"),
                       (189.0, 90, "debris-plate-b")):
        out.append((name, t_centre_at(t, 270, la), x, "the fall's debris" if name == "debris-plate-a" else None))
    return out


def fall_block():
    la = FACTORS["low-air"]
    hv = halves()
    return {
        "halves": [{"piece": f"fall-half-{s}", "x0": hv[s][1], "bottom": hv[s][2], "size": list(hv[s][0].size),
                    "char": f"fall-half-{s}-char", "glow": f"fall-half-{s}-glow", "keys": fall_keys(s)}
                   for s in ("west", "east")],
        "shards": {"piece": "fall-shards", "from": 0.4, "to": 9.0},
        "balloons": [{"piece": "balloon-tear", "t": t_centre_at(FALL_T, 110, la), "x": 120},
                     {"piece": "balloon-tear", "t": t_centre_at(FALL_T, 70, la), "x": 380}],
        "spires": [{"piece": "spire-topple-e", "t": t_centre_at(FALL_T + 5.0, 270, la), "x": 110, "topple_at": 3.4},
                   {"piece": "spire-topple-w", "t": t_centre_at(FALL_T + 10.5, 270, la), "x": 370, "topple_at": 8.8},
                   {"piece": "spire-topple-e", "t": t_centre_at(FALL_T + 16.0, 270, la), "x": 96, "topple_at": 14.4}],
        "veils": [{"piece": f"fall-veil-{s}", "keys": fall_veil_keys(s)} for s in ("west", "east")],
    }


# =========================================================================== proposal

def _flow(d):
    return "{" + ", ".join(f"{k}: {v}" for k, v in d.items()) + "}"


def proposal_text(pieces):
    placed = placements()
    fall = fall_block()
    burst = Image.open(OUT / "growth-burst.png")
    lines = ["backdrop:",
             f"  # Presentation only: Level 37's Aurelia (tools/art/backdrop_l37.py, step ART-C2, sections from {SRC_NAME}).",
             "  # The ground is ONE continuous city laid out on the level's clock (every landmark by its time), cut into",
             "  # city-NN ground pieces of at most 2,040 rows placed back to back on exact rows (no ground tile set);",
             "  # transparent between the platforms, where the deep cloud deck shows. Re-run the generator after moving a",
             "  # section: the city, the growth burst, the halves and their rows follow the clock.",
             "  # Animated pieces: war-skirmish (deep, t~100-144), the hulks (low-air) and the deck fires (ground), never",
             "  # more than two on the screen at once (checked); the -glow pieces are additive at their piece's places.",
             "  scroll_factors: {deep: 0.15, far: 0.5, ground: 1, low-air: 1.3, high-air: 1.7}   # far unused",
             "  ramp: 2",
             "  haze_colour: 5a443c   # the turn's darker grade: smoke over the cloud deck",
             "  atmosphere:",
             "    clear: {haze: 0.0}   # sections 1-3: the warm cream is in the tiles",
             "    smoke: {haze: 0.35, wisps: smoke-wisps}   # from the turn: the deck a step darker, smoke drifting over",
             "  tile_sets:",
             f"    cloud-deck: {{layer: deep, height: {Image.open(OUT / 'cloud-deck.png').height}}}   # sections 1-4: the cream cloud deck far below",
             f"    cloud-bands: {{layer: deep, height: {Image.open(OUT / 'cloud-bands.png').height}}}   # section 5: the deck opening on the bands",
             f"    smoke-wisps: {{layer: high-air, height: {Image.open(OUT / 'smoke-wisps.png').height}}}   # the smoke atmosphere's wisps",
             "  pieces:"]
    for name, spec in pieces.items():
        if name.startswith(("growth-burst", "fall-", "balloon-tear", "spire-topple", "sinking-city")):
            continue
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("    # the fall's and the turn's pieces (drawn by the growth and falling-city passes, not placed):")
    for name, spec in pieces.items():
        if not name.startswith(("growth-burst", "fall-", "balloon-tear", "spire-topple", "sinking-city")):
            continue
        if name == "sinking-city":
            lines.append("    # Level 38's source (its aurelia-wreck), no longer drawn in Level 37 (the F.6 redo):")
        extra = f", frames: {spec['frames']}, fps: {spec['fps']}" if spec.get("frames") else ""
        lines.append(f"    {name}: {{layer: {spec['layer']}, size: [{spec['size'][0]}, {spec['size'][1]}]{extra}}}")
    lines.append("  placed:")
    for name, t, x, note in placed:
        if note:
            lines.append(f"    # {note}")
        lines.append(f"    - {{piece: {name}, t: {t}, x: {x}}}")
    lines += ["  growth:",
              "    # The Vrell turn's burst (Level 37 *The Vrell turn*): over the city on the screen at `t`, growth-burst is",
              "    # drawn where growth-burst-reveal's value <= (now - t) / seconds x 255 (normal blend; one texture and a",
              "    # threshold), growth-burst-glow additive under the same reveal. It lies on the ground rows from `bottom`",
              "    # (a ground piece's rows: drawn as a placed ground piece with that bottom row would be). Every city row",
              "    # entering after `t` is post-turn already; the burst's rows near the top edge reveal first.",
              f"    t: {TURN_T:g}",
              f"    seconds: {TURN_S:g}",
              "    piece: growth-burst",
              "    reveal: growth-burst-reveal",
              "    glow: growth-burst-glow",
              f"    bottom: {GROW_LO}   # ground rows {GROW_LO}..{GROW_LO + burst.height - 1}; x 0..479",
              "  fall:",
              "    # The falling city (A.13 b, round 73 R.2): from `t` the city pieces are not drawn; the two halves are the",
              "    # ground. Each half's image is the city's own pixels on its rows (x0 = its left column, bottom = its bottom",
              "    # ground row): drawn at `t` as a ground piece with that bottom row, the halves together are exactly the",
              "    # city on the screen (the handover is pixel-exact; nothing else of the city is near them).",
              "    # keys: [u, x, y, scale, turn, shade, alpha] every 0.1 s to 2 s, then 0.5 s: u s after t; x, y the screen",
              "    # centre of the half's image (y down from the top edge); scale (bilinear) about that centre; turn degrees",
              "    # clockwise; shade the share it has darkened toward 1e1616 (smooth); alpha its opacity (round 75 F.6 redo:",
              f"    # 1 to {FALL_FADE_FROM:g} s, fading slowly to 0 at {FALL_GONE:g} s; its char and glow and the shards with it).",
              "    # The first key is the ground's place at t and its speed the ground's; interpolate linearly. `char`",
              "    # (normal) and `glow` (additive, its frames on the real clock) are drawn with the half (their image",
              "    # CHAR_PAD = 14 px larger a side, same centre) and fade in over the first second as the gap opens, so the",
              f"    # handover stays exact. Draw the west half first. From {FALL_WHOLE:g} s every pixel of a half is on the screen.",
              f"    t: {FALL_T:g}",
              f"    seconds: {FALL_SPAN:g}",
              "    halves:"]
    for hdef in fall["halves"]:
        lines.append(f"      - piece: {hdef['piece']}")
        lines.append(f"        x0: {hdef['x0']}")
        lines.append(f"        bottom: {hdef['bottom']}   # size {hdef['size'][0]}x{hdef['size'][1]}")
        lines.append(f"        char: {hdef['char']}")
        lines.append(f"        glow: {hdef['glow']}")
        lines.append("        keys:")
        for k in hdef["keys"]:
            lines.append(f"          - [{', '.join(f'{v:g}' for v in k)}]")
    sh = fall["shards"]
    lines += ["    # the shards tumbling in the widening gap: at the middle of the two halves' cut edges, from..to s after t",
              f"    shards: {_flow(sh)}",
              "    # the balloons over the main deck's far edge: low-air pieces placed as usual (t, x) until `t`, then their",
              "    # frames once from frame 0 at fps (they tear and collapse, no pop) and hold the last",
              "    balloons:"]
    for b in fall["balloons"]:
        lines.append(f"      - {_flow(b)}")
    lines += ["    # the spires the wing flies down beside (low-air, placed as usual): frame 0 upright until topple_at s after",
              "    # t, then the topple once at the piece's fps, holding the last frame",
              "    spires:"]
    for s in fall["spires"]:
        lines.append(f"      - {_flow(s)}")
    lines += ["    # the cloud tops closing over the sinking halves (round 75 F.6 redo): each veil drawn after the halves (and",
              "    # their rims and glows), normal blend, unshaded, unscaled, centred on its keys' (x, y) (screen, y down),",
              "    # at its keys' alpha; keys [u, x, y, alpha] s after t, linear between them, nothing drawn outside them.",
              "    # The halves are gone at their alpha 0; the veils fade into the deck after them. No sinking city (the",
              "    # old `sinking:` crossfade on deep is gone: its canvas cut the halves and it slid out at the bottom).",
              "    veils:"]
    for v in fall["veils"]:
        lines.append(f"      - piece: {v['piece']}")
        lines.append("        keys:")
        for k in v["keys"]:
            lines.append(f"          - [{', '.join(f'{x:g}' for x in k)}]")
    lines.append(f"sections:   # ART-C2's reading of the README's Layout ({SRC_NAME}); C2 writes the real ones")
    for s in SECTIONS:
        lines.append("  - " + "{" + ", ".join(f"{k}: {v}" for k, v in s.items() if k != "notes") + "}")
    lines.append(f"turn: {TURN_T:g}")
    lines.append(f"# rows: the turn's screen {TURN_BASE}..{TURN_BASE + 539}; the fall's {FALL_BASE}..{FALL_BASE + 539}; the end's "
                 f"bottom edge {int(round(scroll(END)))} (ground px); deep {int(round(scroll(END) * FACTORS['deep']))}")
    lines += ["ground_targets:   # at: [t, x], when the target enters at the top edge and its x - on the decks, rims and",
              "  # terraces laid out for them (graft-turret 52 px, sam-nest 44 px; the nests stand inside their deck's edge)"]
    for label, sec, enemy, count, at_ in ground_targets():
        lines += [f"  - target: {label}", f"    section: {sec}", f"    enemy: {enemy}", f"    count: {count}",
                  "    at: [" + ", ".join(f"[{a[0]:g}, {a[1]}]" for a in at_) + "]"]
    pads = [(44, at(44, 380)), (72, at(72, 380)), (127, at(127, 380))]
    lines += ["shuttle_pads:   # each pair on its pads (x 190 and 290) in the lower half as it lifts: screen y down at the lift",
              "  # time; the pad decks are laid out on those rows (C2: the allies' groups' pads)"]
    for t, row in pads:
        lines.append(f"  - {{t: {t}, pads: [[190, 380], [290, 380]], row: {int(round(row))}}}")
    lines += ["secret:   # the CDF locker under its awning on the left deck edge (README: t~60, x 90)",
              f"  - {{t: 60, x: 90, row: {int(round(top(60) + 10))}}}"]
    return "\n".join(lines) + "\n"


def write_proposal(pieces):
    PROPOSAL.parent.mkdir(parents=True, exist_ok=True)
    PROPOSAL.write_text(proposal_text(pieces), encoding="utf-8")
    print(f"proposal: {PROPOSAL.relative_to(ROOT)}")


def load_proposal():
    return yaml.safe_load(PROPOSAL.read_text(encoding="utf-8"))


# =========================================================================== composite (the review's stand-in renderer)

_IMG = {}


def img(name, k=None):
    key = (name, k)
    if key not in _IMG:
        _IMG[key] = Image.open(OUT / (f"{name}_{k}.png" if k is not None else f"{name}.png")).convert("RGBA")
    return _IMG[key]


def frames_of(name, spec):
    n = spec.get("frames")
    return [img(name, k) for k in range(n)] if n else [img(name)]


def section_at(t):
    i = 0
    for k in range(1, len(SECTIONS)):
        if t >= STARTS[k]:
            i = k
    return i


def deep_rows(t):
    """The deep layer on the screen at t from the sections' tile sets (the game's seams)."""
    off = scroll(t) * FACTORS["deep"]
    out = np.zeros((SCREEN, W, 4), np.uint8)
    rows = int(round(off)) + SCREEN - 1 - np.arange(SCREEN)
    for i, sec in enumerate(SECTIONS):
        name = next(n for n in sec["tiles"] if n in ("cloud-deck", "cloud-bands"))
        arr = np.array(img(name))
        h = len(arr)
        sel = np.array([CLOCK.section_at_row("deep", r) == i for r in rows])
        out[sel] = arr[h - 1 - (rows[sel] % h)]
    return out


def wisps_rows(t):
    if section_at(t) < 3:
        return None
    arr = np.array(img("smoke-wisps"))
    h = len(arr)
    rows = int(round(scroll(t) * FACTORS["high-air"])) + SCREEN - 1 - np.arange(SCREEN)
    k = smoothstep(TURN_T, TURN_T + TURN_S, t)
    a = arr[h - 1 - (rows % h)].copy()
    a[..., 3] = (a[..., 3] * k).astype(np.uint8)
    return Image.fromarray(a, "RGBA")


def piece_y(p_t, factor, h, t):
    """Screen y (down) of a placed piece's centre at t."""
    return SCREEN - (scroll(p_t) * factor + SCREEN / 2 - scroll(t) * factor)


def blit(canvas, im, x, y, add=False, alpha=1.0):
    if alpha < 1:
        a = np.array(im).astype(np.float64)
        if add:
            a[..., :3] *= alpha
        else:
            a[..., 3] *= alpha
        im = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return l23kit.add_c(canvas, im, x, y) if add else l23kit.paste_c(canvas, im, x, y)


def interp_key(keys, u):
    ks = np.array(keys, float)
    return [float(np.interp(u, ks[:, 0], ks[:, c])) for c in range(ks.shape[1])]


def compose(t, prop, units=True):
    """The screen at t (480x540): the deep layer (its haze after the turn as the atmosphere's stand-in) and pieces,
    the city (or the growth burst over it, or the falling halves), the low-air pieces, stand-ins, the smoke wisps."""
    bd = prop["backdrop"]
    specs = bd["pieces"]
    placed = bd["placed"]
    deep = deep_rows(t).astype(np.float64)
    hz = 0.35 * smoothstep(TURN_T, TURN_T + TURN_S, t)
    deep[..., :3] = deep[..., :3] * (1 - hz) + np.array((0x5a, 0x44, 0x3c)) * hz
    canvas = Image.fromarray(deep.astype(np.uint8), "RGBA")
    fall = bd["fall"]
    u = t - FALL_T

    def layer(name):
        return specs[name]["layer"]
    for p in placed:                                         # deep pieces
        if layer(p["piece"]) != "deep":
            continue
        sp = specs[p["piece"]]
        fr = frames_of(p["piece"], sp)
        f = fr[int(t * sp.get("fps", 0)) % len(fr)]
        y = piece_y(p["t"], FACTORS["deep"], f.height, t)
        if -f.height < y < SCREEN + f.height:
            canvas = blit(canvas, f, p["x"], y)
    sk = fall.get("sinking")
    if sk and u >= sk["from"]:
        k = smoothstep(sk["from"], sk["from"] + sk["fade"], u)
        y = piece_y(sk["t"], FACTORS["deep"], 0, t)
        canvas = blit(canvas, img(sk["piece"]), sk["x"], y, alpha=k)
        canvas = blit(canvas, img(sk["glow"], int(t * 6) % 4), sk["x"], y, add=True, alpha=k)
    if u < 0:                                                # the city
        rows = rows_of(city()[0], int(round(scroll(t))), int(round(scroll(t))) + SCREEN)
        canvas.alpha_composite(Image.fromarray(rows, "RGBA"))
        g = bd["growth"]
        e = t - g["t"]
        if 0 <= e:
            burst = np.array(img(g["piece"]))
            rev = np.array(img(g["reveal"]))
            glw = np.array(img(g["glow"])).astype(np.float64)
            show = (rev[..., 3] > 0) & (rev[..., 0] <= e / g["seconds"] * 255)
            b = burst.copy()
            b[~show] = 0
            glw[~show] = 0
            y_bottom = SCREEN - (g["bottom"] - scroll(t))
            yc = y_bottom - burst.shape[0] / 2
            canvas = blit(canvas, Image.fromarray(b, "RGBA"), 240, yc)
            canvas = blit(canvas, Image.fromarray(glw.astype(np.uint8), "RGBA"), 240, yc, add=True)
        for p in placed:
            if layer(p["piece"]) == "ground" and specs[p["piece"]].get("frames"):
                sp = specs[p["piece"]]
                f = frames_of(p["piece"], sp)[int(t * sp["fps"]) % sp["frames"]]
                y = piece_y(p["t"], 1.0, f.height, t)
                if -f.height < y < SCREEN + f.height:
                    canvas = blit(canvas, f, p["x"], y)
    else:                                                    # the halves, then the veils over them
        sink_fade = 1 - smoothstep(sk["from"], sk["from"] + sk["fade"], u) if sk else 1.0
        mids = []
        for hdef in fall["halves"]:
            key = interp_key(hdef["keys"], u)
            fade = sink_fade * (key[6] if len(key) > 6 else 1.0)
            mids.append(key)
            if fade <= 0:
                continue
            k_in = smoothstep(0, 1.0, u)
            body = transformed(img(hdef["piece"]), key)
            canvas = blit(canvas, body, key[1], key[2], alpha=fade)
            ch = transformed(img(hdef["char"]), key)
            canvas = blit(canvas, ch, key[1], key[2], alpha=k_in * fade)
            gl = transformed(img(hdef["glow"], int(t * 8) % 4), key, dark=None)
            canvas = blit(canvas, gl, key[1], key[2], add=True, alpha=k_in * fade)
        shd = fall["shards"]
        if shd["from"] <= u <= shd["to"]:
            mx = (mids[0][1] + mids[1][1]) / 2
            my = (mids[0][2] + mids[1][2]) / 2
            k = smoothstep(shd["from"], shd["from"] + 0.6, u) * smoothstep(shd["to"], shd["to"] - 2, u)
            k *= mids[1][6] if len(mids[1]) > 6 else 1.0
            canvas = blit(canvas, img(shd["piece"], int(t * 8) % 8), mx, my, alpha=k)
        for v in fall.get("veils", []):
            ks = v["keys"]
            if ks[0][0] <= u <= ks[-1][0]:
                vk = interp_key(ks, u)
                if vk[3] > 0:
                    canvas = blit(canvas, img(v["piece"]), vk[1], vk[2], alpha=vk[3])
    for p in placed:                                         # low-air pieces
        if layer(p["piece"]) != "low-air":
            continue
        sp = specs[p["piece"]]
        fr = frames_of(p["piece"], sp)
        f = fr[int(t * sp.get("fps", 0)) % len(fr)]
        y = piece_y(p["t"], FACTORS["low-air"], f.height, t)
        if -f.height < y < SCREEN + f.height:
            canvas = blit(canvas, f, p["x"], y, add=p["piece"].endswith("-glow"))
    for b in fall["balloons"]:
        sp = specs[b["piece"]]
        k = 0 if u < 0 else min(sp["frames"] - 1, int(u * sp["fps"]))
        f = img(b["piece"], k)
        y = piece_y(b["t"], FACTORS["low-air"], f.height, t)
        if -f.height < y < SCREEN + f.height:
            canvas = blit(canvas, f, b["x"], y)
    for s in fall["spires"]:
        sp = specs[s["piece"]]
        e = u - s["topple_at"]
        k = 0 if e < 0 else min(sp["frames"] - 1, int(e * sp["fps"]))
        f = img(s["piece"], k)
        y = piece_y(s["t"], FACTORS["low-air"], f.height, t)
        if -f.height < y < SCREEN + f.height:
            canvas = blit(canvas, f, s["x"], y)
    if units:
        canvas = stand_ins(canvas, t)
    w = wisps_rows(t)
    if w is not None:
        canvas.alpha_composite(w)
    return canvas


def load_held(name):
    try:
        return l23kit.load(name)
    except Exception:
        try:
            return artkit.load_frames(name)
        except Exception:
            return None


def stand_ins(canvas, t):
    """Stand-ins on the play plane: a Talon line, the Gunship, a shuttle pair on station, the ship."""
    talon, gun, shuttle = load_held("talon"), load_held("gilded-gunship"), load_held("evacuation-shuttle")
    chim = load_held("chimera")
    if talon:
        for t0, n in ((6, 4), (28, 4), (57, 4), (91, 4), (103, 5), (117, 5), (169, 4)):
            e = t - t0
            if 0 <= e <= 6:
                for k in range(n):
                    x = 240 + (k - (n - 1) / 2) * 52
                    y = -20 + 140 * e - abs(k - (n - 1) / 2) * 26
                    if -30 < y < SCREEN + 30:
                        canvas = l23kit.paste_c(canvas, talon[0], x, y)
    if chim:
        for t0 in (113.0, 147.0, 165.0, 178.0):
            e = t - t0
            if 0 <= e <= 5:
                for k in range(3):
                    canvas = l23kit.paste_c(canvas, chim[0], 140 + 100 * k, -20 + 130 * e - 20 * abs(k - 1))
    if gun and 116 <= t <= 124:
        canvas = l23kit.paste_c(canvas, gun[0], 260, -40 + 50 * (t - 116))
    if shuttle:
        for t0 in (44.0, 72.0, 127.0):
            e = t - t0
            if 5 <= e <= 13:
                for k, y in enumerate((170, 240)):
                    canvas = l23kit.paste_c(canvas, shuttle[0], 240 + 16 * np.sin(e * 0.8 + k), y)
    ship = artkit.load_frames("ship")
    if ship:
        canvas = l23kit.paste_c(canvas, ship[2], 240, 470)
    return canvas


# =========================================================================== review

SHOTS = [(4.0, "T=4 THE RIM"), (24.5, "T=24.5 SAM PLATFORM"), (44.0, "T=44 PAIR 1'S PADS"),
         (60.5, "T=60.5 LOCKER AWNING X 90"), (79.5, "T=79.5 PLAZA TERRACE"), (93.0, "T=93 THE GREAT SPIRE"),
         (TURN_T + 0.6, "TURN +0.6 S: THE BURST"), (TURN_T + 2.0, "TURN +2 S"), (TURN_T + 4.5, "TURN +4.5 S: DONE"),
         (128.5, "T=128.5 BURNING DECK"), (151.0, "T=151 THE MAIN DECK"), (FALL_T, "FALL +0: WHOLE"),
         (FALL_T + 2.0, "+2 S: THE JAGGED BREAK"), (FALL_T + 6.0, "+6 S: SLOWER, DARKER"),
         (FALL_T + 12.0, "+12 S: BESIDE THE SPIRES"), (FALL_T + 21.0, "+21 S: SINKING CITY"),
         (END - 6.0, "END -6 S: OPEN CLOUD"), (END - 0.5, "THE END")]


def readability(prop):
    """1x crops: the units' gold (Talon, Gunship, Chimera) and the amber bullets over the pre-turn decks and the
    burnt, overgrown ones."""
    units = [load_held(n) for n in ("talon", "gilded-gunship", "chimera", "needle", "orb")]
    cells = []
    for t, lab in ((44.0, "PRE-TURN DECK"), (93.0, "THE PLAZA"), (128.5, "POST-TURN DECK"), (FALL_T, "THE MAIN DECK")):
        c = compose(t, prop, units=False).crop((120, 150, 360, 300))
        x = 30
        for u in units[:3]:
            if u:
                c = l23kit.paste_c(c, u[0], x, 75)
                x += 70
        if units[3]:
            for k in range(3):
                c = l23kit.paste_c(c, units[3][0], 200 + 8 * k, 30 + 22 * k)
        if units[4]:
            c = l23kit.paste_c(c, units[4][0], 222, 120)
        cells.append((lab, c))
    return cells


def review_a():
    """Round 75 a's sheet and GIFs (step ART-C2; the fall there was ART-C2's 20 s curve and the sinking city: a rerun
    now shows the F.6 redo's fall in the a files, so it is run only on request, ``--review-a``)."""
    prop = load_proposal()
    bd = prop["backdrop"]
    cols = 6
    sw, sh = W // 3, SCREEN // 3
    shots = [(compose(t, prop), lab) for t, lab in SHOTS]
    rows_n = -(-len(shots) // cols)
    read = readability(prop)
    pieces_h = 650
    sheet = raster.sheet(16 + cols * (sw + 12), 70 + rows_n * (sh + 26) + 190 + pieces_h + 260,
                         "LEVEL 37 BACKDROP (FINAL R75): FALL OF AURELIA", f"PRODUCTION ART, {BATCH.upper()} - R75")
    raster.draw_text(sheet, 16, 38, "SCREENS AT THIRD SIZE WITH STAND-INS (TALONS, CHIMERAS, THE GUNSHIP, A SHUTTLE PAIR "
                                    "ON STATION, THE SHIP); THE DEEP HAZE AFTER THE TURN STANDS IN FOR THE ATMOSPHERE",
                     raster.LABEL)
    for i, (im, lab) in enumerate(shots):
        x, y = 16 + (i % cols) * (sw + 12), 56 + (i // cols) * (sh + 26)
        sheet.alpha_composite(im.resize((sw, sh), Image.BOX), (x, y))
        raster.draw_text(sheet, x, y + sh + 4, lab[:30], raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 26) + 6
    raster.draw_text(sheet, 16, y, "READABILITY AT 1X: TALON, GUNSHIP, CHIMERA, NEEDLES AND AN ORB OVER THE DECKS "
                                   "(GOLD ON GOLD: THE DECKS A STEP PALER)", raster.LABEL)
    for k, (lab, c) in enumerate(read):
        x0 = 16 + k * (c.width + 14)
        sheet.alpha_composite(c, (x0, y + 16))
        raster.draw_text(sheet, x0, y + 18 + c.height, lab, raster.LABEL_DIM)
    y += 190
    raster.draw_text(sheet, 16, y, "PIECES AT HALF SIZE: THE HALVES WITH THEIR TORN RIMS AND GLOW, THE GROWTH BURST AND "
                                   "ITS REVEAL MAP; AT 1X: SPIRES (FRAMES 0/3/6/9), BALLOON TEAR, HULKS, FIRE, WAR, DEBRIS",
                     raster.LABEL)
    x = 16
    plate = Image.new("RGBA", (300, 330), (70, 60, 56, 255))
    for side in ("west", "east"):
        c = plate.copy()
        hb = img(f"fall-half-{side}")
        ch = img(f"fall-half-{side}-char")
        gl = img(f"fall-half-{side}-glow", 0)
        full = Image.new("RGBA", ch.size, (0, 0, 0, 0))
        full.alpha_composite(hb, (CHAR_PAD, CHAR_PAD))
        full.alpha_composite(ch)
        full = l23kit.add_c(full, gl, full.width / 2, full.height / 2)
        c.alpha_composite(full.resize((full.width // 2, full.height // 2), Image.BOX), (4, 4))
        sheet.alpha_composite(c.crop((0, 0, 160, 330)), (x, y + 16))
        x += 166
    for name in ("growth-burst", "growth-burst-reveal"):
        g = img(name)
        c = Image.new("RGBA", g.size, (70, 60, 56, 255))
        c.alpha_composite(g)
        sheet.alpha_composite(c.resize((g.width // 2, g.height // 2), Image.BOX), (x, y + 16))
        x += g.width // 2 + 8
    x2, y2 = 16, y + 16 + 340
    for name in ("spire-topple-e", "spire-topple-w"):
        for k in (0, 3, 6, 9):
            f = img(name, k)
            c = Image.new("RGBA", f.size, (196, 170, 130, 255))
            c.alpha_composite(f)
            sheet.alpha_composite(c, (x2, y2))
            x2 += f.width + 6
    x2, y2 = 16, y2 + 104
    strip = [img("balloon-tear", k) for k in (0, 3, 6, 9)] + [img("hulk-a", 0), img("hulk-b", 3), img("deck-fire", 2),
                                                               img("war-skirmish", 0), img("fall-shards", 0),
                                                               img("balloon-a"), img("lift-ring"),
                                                               img("debris-plate-a"), img("debris-plate-b"),
                                                               img("debris-cable"), img("sinking-city")]
    for f in strip:
        c = Image.new("RGBA", f.size, (196, 170, 130, 255))
        c.alpha_composite(f)
        if x2 + f.width > sheet.width - 16:
            break
        sheet.alpha_composite(c, (x2, y2))
        x2 += f.width + 6
    y += pieces_h
    raster.draw_text(sheet, 16, y, "THE TILE SETS (QUARTER SIZE): CLOUD-DECK, CLOUD-BANDS, SMOKE-WISPS (ON A GREY PLATE); "
                                   "THE CITY'S GROUND PIECES AT 1/16", raster.LABEL)
    x = 16
    for name in ("cloud-deck", "cloud-bands", "smoke-wisps"):
        tl = img(name)
        small = tl.resize((tl.width // 4, tl.height // 4), Image.BOX)
        pl = Image.new("RGBA", small.size, (60, 64, 76, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl.crop((0, 0, small.width, min(small.height, 230))), (x, y + 16))
        x += small.width + 10
    for name in sorted(n for n in bd["pieces"] if n.startswith("city-")):
        tl = img(name)
        small = tl.resize((tl.width // 16, max(1, tl.height // 16)), Image.BOX)
        pl = Image.new("RGBA", small.size, (196, 170, 130, 255))
        pl.alpha_composite(small)
        sheet.alpha_composite(pl, (x, y + 16 + 230 - small.height))
        x += small.width + 6
    png, gif = artkit.review_paths(CONCEPT, "backdrop")
    sheet.convert("RGB").save(png, optimize=True)
    import enemies_r60 as e60  # noqa: E402
    half = (W // 2, SCREEN // 2)
    frames = [compose(FALL_T - 3 + i / 4, prop).resize(half, Image.BOX) for i in range(int((FALL_S + 5) * 4))]
    size = e60.rig.write_gif(frames, gif, fps=4, colors=128)
    g_png, g_gif = artkit.review_paths(CONCEPT, "growth")
    frames = [compose(TURN_T - 0.5 + i / 10, prop).resize(half, Image.BOX) for i in range(int((TURN_S + 2) * 10))]
    size2 = e60.rig.write_gif(frames, g_gif, fps=10, colors=128)
    print(f"review: {png.relative_to(ROOT)}, {gif.name} ({size / 1e6:.1f} MB), {g_gif.name} ({size2 / 1e6:.1f} MB)")


FALL_SHOTS = [158, 160, 161.5, 163, 166, 169, 172, 175, 178, 181, 184, 187, 190, 193, 196, 200]


def review_fall():
    """Round 75 F.6 redo (b): the whole fall, t 158-200 - a sheet of screens at half size (with stand-ins), 1:1 crops of
    the screen's lower half late in the fall (nothing cut), the halves' alpha/shade/scale by second and the veils;
    and a GIF of t 158-200 at play size, 4 frames a second."""
    prop = load_proposal()
    fall = prop["backdrop"]["fall"]
    sw, sh = W // 2, SCREEN // 2
    cols = 8
    rows_n = -(-len(FALL_SHOTS) // cols)
    crops_t = (176, 186, 192, 196)
    sheet = raster.sheet(16 + cols * (sw + 10), 70 + rows_n * (sh + 24) + 26 + 270 + 30 + 226 + 20,
                         "LEVEL 37 THE FALL (FINAL R75 B): F.6 REDO - THE CITY SINKS INTO THE CLOUDS",
                         f"PRODUCTION ART, {BATCH.upper()} - R75 B (STEP ART-C3)")
    raster.draw_text(sheet, 16, 38, "T 158-200 AT HALF SIZE WITH STAND-INS: THE BREAK, THE HALVES SLOWING, SHRINKING, "
                                    "DARKENING AND FADING, THE CLOUD TOPS CLOSING OVER THEM, GONE BY T 194; OPEN CLOUD TO "
                                    "THE END", raster.LABEL)
    for i, t in enumerate(FALL_SHOTS):
        x, y = 16 + (i % cols) * (sw + 10), 56 + (i // cols) * (sh + 24)
        sheet.alpha_composite(compose(t, prop).resize((sw, sh), Image.BOX), (x, y))
        ah = [interp_key(hd["keys"], t - FALL_T)[6] if t >= FALL_T else 1.0 for hd in fall["halves"]]
        raster.draw_text(sheet, x, y + sh + 4, f"T={t:g}  ALPHA {ah[0]:.2f}/{ah[1]:.2f}", raster.LABEL_DIM)
    y = 56 + rows_n * (sh + 24) + 4
    raster.draw_text(sheet, 16, y, "1:1, THE SCREEN'S LOWER HALF (NO STAND-INS) AT T " +
                     ", ".join(f"{t:g}" for t in crops_t) + ": EVERY PIXEL OF THE HALVES ON THE SCREEN FROM T "
                     f"{FALL_T + FALL_WHOLE:g}, NOTHING CUT", raster.LABEL)
    for k, t in enumerate(crops_t):
        c = compose(t, prop, units=False).crop((0, 270, W, SCREEN))
        sheet.alpha_composite(c, (16 + k * (W + 12), y + 18))
    y += 18 + 270 + 12
    raster.draw_text(sheet, 16, y, "THE CLOUD TOPS (FALL-VEIL-WEST/-EAST, 288X196) ON THE DECK'S BANDS AND ON GREY; "
                                   "THE HALVES' CURVES (WEST / EAST) BY SECOND", raster.LABEL)
    bands = img("cloud-bands")
    x = 16
    for name in ("fall-veil-west", "fall-veil-east"):
        v = img(name)
        for plate in (bands.crop((0, 0, v.width, v.height)), Image.new("RGBA", v.size, (70, 72, 80, 255))):
            c = plate.copy()
            c.alpha_composite(v)
            sheet.alpha_composite(c, (x, y + 18))
            x += v.width + 8
    kw, ke = fall["halves"][0]["keys"], fall["halves"][1]["keys"]
    ty = y + 18
    tx = x + 8
    raster.draw_text(sheet, tx, ty, "U    ALPHA      SHADE      SCALE", raster.LABEL_DIM)
    for u in (0, 4, 8, 12, 16, 20, 24, 28, 32, 34):
        a, b = interp_key(kw, u), interp_key(ke, u)
        ty += 12
        raster.draw_text(sheet, tx, ty, f"{u:<4d} {a[6]:.2f}/{b[6]:.2f}  {a[5]:.2f}/{b[5]:.2f}  {a[3]:.2f}/{b[3]:.2f}",
                         raster.LABEL_DIM)
    png, gif = artkit.review_paths(CONCEPT, "fall", "b")
    sheet.convert("RGB").save(png, optimize=True)
    import enemies_r60 as e60  # noqa: E402
    frames = [compose(158 + i / 4, prop) for i in range(int((END - 158) * 4) + 1)]
    size = e60.rig.write_gif(frames, gif, fps=4, colors=128, max_mb=12.0)
    print(f"review: {png.relative_to(ROOT)} {sheet.size}, {gif.name} ({size / 1e6:.1f} MB, {len(frames)} frames)")


# =========================================================================== check

def alpha_levels(im):
    a = np.array(im.convert("RGBA"))[..., 3]
    return len(np.unique(a[(a > 0) & (a < 255)]))


def check():
    """The deep tiles opaque and wrapping, the wisps wrapping, sizes as the proposal, Source chunks, no checker
    (DitherGuardTest's measure under 5 %), every piece within an atlas page, the smooth sets with >= 32 alpha or
    light levels, the deep pieces' edges never opaque on the screen, at most two animated pieces at once, the city's
    chunks back to back on exact rows covering every row with something on it, the halves' union = the city on their
    rows (the handover), nothing of the city left near the main deck, the growth burst over the screen at the turn and
    its reveal within the seconds, the halves' curves (monotonic scale and shade, the first key the ground's place and
    speed), every ground target on a deck, the pads on their pads, the deep seam at section 5 invisible."""
    problems = []
    prop = load_proposal()
    bd = prop["backdrop"]
    specs = bd["pieces"]
    out, f, _, _ = city()
    for name in ("cloud-deck", "cloud-bands"):
        a = np.array(img(name))
        if (a[..., 3] < 255).any():
            problems.append(f"{name}: not opaque")
        if wrap_score(a) > 1.5:
            problems.append(f"{name}: wrap score {wrap_score(a):.2f}")
    if wrap_score_alpha(np.array(img("smoke-wisps"))) > 1.5:
        problems.append("smoke-wisps: wrap")
    for name, spec in specs.items():
        n = spec.get("frames")
        files = [OUT / f"{name}_{k}.png" for k in range(n)] if n else [OUT / f"{name}.png"]
        for fl in files:
            im = Image.open(fl)
            if list(im.size) != list(spec["size"]):
                problems.append(f"{fl.name}: {im.size} vs {spec['size']}")
            if max(im.size) > MAX_PIECE:
                problems.append(f"{fl.name}: {im.size} over an atlas page")
    vis = chk = 0
    for fl in sorted(OUT.glob("*.png")):
        im = Image.open(fl)
        if im.text.get(artkit.SOURCE_KEY) != SOURCE:
            problems.append(f"{fl.name}: no Source chunk")
        v, c = checker_share(im)
        if v and c / v >= 0.05:
            problems.append(f"{fl.name}: {c / v * 100:.1f} % checker")
        vis, chk = vis + v, chk + c
    for name in ("smoke-wisps", "fall-half-west-char", "sinking-city", "far-side", "growth-burst-glow",
                 "fall-half-east-glow_0", "lift-ring-glow", "fall-veil-west", "fall-veil-east"):
        im = Image.open(OUT / f"{name}.png")
        if name.endswith(("glow", "glow_0")):
            lv = len(np.unique(np.array(im)[..., :3].max(-1)))
        else:
            lv = alpha_levels(im)
        if lv < 32:
            problems.append(f"{name}: {lv} levels (smooth sets need >= 32)")
    for p in bd["placed"]:                                   # deep pieces' edges
        spec = specs[p["piece"]]
        if spec["layer"] != "deep":
            continue
        a = np.array(frames_of(p["piece"], spec)[0])[..., 3]
        if a[0].any() or a[-1].any() or a[:, 0].any() or a[:, -1].any():
            problems.append(f"{p['piece']}: alpha on its edge (a deep piece)")
    for name in ("sinking-city", "fall-veil-west", "fall-veil-east"):   # free-standing pieces: no cut edge
        a = np.array(img(name))[..., 3]
        if a[0].any() or a[-1].any() or a[:, 0].any() or a[:, -1].any():
            problems.append(f"{name}: alpha on its edge (its image would show a cut)")
    worst = 0
    for step in range(int(END * 10)):                         # the motion budget (placed pieces)
        t = step / 10
        anim = 0
        for p in bd["placed"]:
            spec = specs[p["piece"]]
            if not spec.get("frames"):
                continue
            y = piece_y(p["t"], FACTORS[spec["layer"]], spec["size"][1], t)
            if -spec["size"][1] / 2 < y < SCREEN + spec["size"][1] / 2:
                anim += 1
        worst = max(worst, anim)
        if anim > 2:
            problems.append(f"t={t}: {anim} animated pieces on the screen")
            break
    chunks = sorted((p for p in bd["placed"] if p["piece"].startswith("city-")), key=lambda p: p["t"])
    prev_top = None
    for p in chunks:                                          # exact rows, back to back
        h = specs[p["piece"]]["size"][1]
        bottom = round(scroll(p["t"]) + SCREEN / 2 - h / 2)
        a = np.array(img(p["piece"]))
        if not np.array_equal(a, rows_of(out, bottom, bottom + h)):
            problems.append(f"{p['piece']}: not the city's rows {bottom}..{bottom + h - 1}")
        if prev_top is not None and bottom < prev_top:
            problems.append(f"{p['piece']}: overlaps the piece below")
        prev_top = bottom + h
    covered = np.zeros(ROW_HI - ROW_LO, bool)
    for p in chunks:
        h = specs[p["piece"]]["size"][1]
        bottom = round(scroll(p["t"]) + SCREEN / 2 - h / 2)
        covered[bottom - ROW_LO:bottom - ROW_LO + h] = True
    need = (out[..., 3] > 0).any(axis=1)[::-1]
    if (need & ~covered).any():
        problems.append(f"{int((need & ~covered).sum())} city rows in no ground piece")
    fall = bd["fall"]
    union = np.zeros((HALF_HI - HALF_LO, W, 4), np.uint8)
    for hd in fall["halves"]:                                # the handover: the halves' union = the city
        im = np.array(img(hd["piece"]))
        h_, w_ = im.shape[:2]
        y0 = HALF_HI - (hd["bottom"] + h_)
        reg = union[y0:y0 + h_, hd["x0"]:hd["x0"] + w_]
        if (reg[..., 3] > 0).any() & (im[..., 3] > 0).any() and ((reg[..., 3] > 0) & (im[..., 3] > 0)).any():
            problems.append(f"{hd['piece']}: overlaps the other half")
        union[y0:y0 + h_, hd["x0"]:hd["x0"] + w_] = np.where(im[..., 3:4] > 0, im, reg)
        k0 = hd["keys"][0]
        xc = hd["x0"] + w_ / 2
        yc = SCREEN - (hd["bottom"] - FALL_BASE) - h_ / 2
        if abs(k0[1] - xc) > 0.06 or abs(k0[2] - yc) > 0.06 or k0[3] != 1 or k0[4] != 0 or k0[5] != 0:
            problems.append(f"{hd['piece']}: its first key {k0} is not the ground's place ({xc}, {yc})")
        ks = np.array(hd["keys"], float)
        v = (ks[1, 2] - ks[0, 2]) / (ks[1, 0] - ks[0, 0])
        if abs(v - CLOCK.speed_at(FALL_T + 0.25)) > 12:
            problems.append(f"{hd['piece']}: starts at {v:.1f} px/s, the ground {CLOCK.speed_at(FALL_T):.1f}")
        if (np.diff(ks[:, 3]) > 1e-9).any() or (np.diff(ks[:, 5]) < -1e-9).any():
            problems.append(f"{hd['piece']}: scale or shade not monotonic")
        if ks.shape[1] != 7 or k0[6] != 1 or (np.diff(ks[:, 6]) > 1e-9).any() or ks[-1, 6] != 0:
            problems.append(f"{hd['piece']}: alpha not 1 -> 0, monotonic (the F.6 redo: it fades over the fall)")
        else:
            gone = ks[np.argmax(ks[:, 6] <= 0), 0]
            if FALL_T + gone > END - 4:
                problems.append(f"{hd['piece']}: still visible at t={FALL_T + gone:g} (gone >= 4 s before the end)")
            if ks[ks[:, 0] <= 20, 6].min() < 0.3:
                problems.append(f"{hd['piece']}: faded below 0.3 by u=20 (it should fade slowly)")
        side = hd["piece"].rsplit("-", 1)[1]
        for u in np.arange(0, ks[-1, 0] + 1e-9, 0.1):        # nothing cut: whole on the screen from FALL_WHOLE
            key = interp_key(hd["keys"], u)
            if u < FALL_WHOLE or key[6] <= 0:
                continue
            x0_, y0_, x1_, y1_ = half_extent(side, key)
            if x0_ < 0 or y0_ < 0 or x1_ > W or y1_ > SCREEN:
                problems.append(f"{hd['piece']}: at u={u:.1f} ({x0_:.0f}, {y0_:.0f})-({x1_:.0f}, {y1_:.0f}) leaves the screen")
                break
    if not np.array_equal(union, rows_of(out, HALF_LO, HALF_HI)):
        problems.append("the halves' union differs from the city on their rows (the handover)")
    if "sinking" in fall:
        problems.append("fall.sinking: the F.6 redo draws no sinking city in Level 37")
    if fall["halves"][-1]["keys"][-1][0] > FALL_SPAN:
        problems.append("the halves' keys run past FALL_SPAN")
    for v in fall.get("veils", []):                            # the veils: closing over, gone before the end
        ks = np.array(v["keys"], float)
        im = img(v["piece"])
        if ks[0, 3] != 0 or ks[-1, 3] != 0 or FALL_T + ks[-1, 0] > END - 1.5:
            problems.append(f"{v['piece']}: not 0 at both ends or still there at t={FALL_T + ks[-1, 0]:g}")
        if ks[:, 3].max() < 0.95:
            problems.append(f"{v['piece']}: never closes (alpha at most {ks[:, 3].max()})")
        for k in ks:
            if k[3] >= 0.35 and (k[1] - im.width / 2 < -40 or k[1] + im.width / 2 > W + 40
                                 or k[2] - im.height / 2 < 0 or k[2] + im.height / 2 > SCREEN):
                problems.append(f"{v['piece']}: at u={k[0]:g} (alpha {k[3]:g}) it leaves the screen")
                break
    for u in np.arange(FALL_WHOLE, FALL_SPAN, 0.5):           # the veils cover the halves as they go
        hk = [interp_key(hd["keys"], u) for hd in fall["halves"]]
        if not 0 < hk[0][6] <= 0.25:
            continue
        vs = [interp_key(v["keys"], u) for v in fall.get("veils", []) if v["keys"][0][0] <= u <= v["keys"][-1][0]]
        if not vs or max(k[3] for k in vs) < 0.6:
            problems.append(f"u={u:g}: the halves at alpha {hk[0][6]:.2f} with no veil over them")
            break
    near = rows_of(out, HALF_LO - 300, HALF_LO)
    if (near[..., 3] > 0).any():
        problems.append("the city reaches within 300 rows below the main deck (it would vanish at the fall)")
    vis_t = rows_of(out, FALL_BASE, FALL_BASE + SCREEN)
    if not (vis_t[..., 3] > 0).any():
        problems.append("nothing of the main deck on the screen at the fall")
    g = bd["growth"]
    if not (g["bottom"] <= TURN_BASE and g["bottom"] + img(g["piece"]).height >= TURN_BASE + SCREEN):
        problems.append("the growth burst does not cover the screen at the turn")
    rev = np.array(img(g["reveal"]))
    bur = np.array(img(g["piece"]))
    if ((bur[..., 3] > 0) & (rev[..., 3] == 0)).any():
        problems.append("growth-burst pixels without a reveal time")
    for label, sec, enemy, count, at_ in ground_targets():   # targets on decks
        for t, x in at_:
            row = int(round(top(t) + 22))
            i = f.r1 - 1 - row
            if not (f.alpha[i, int(x)] > 0.9 and f.kind[i, int(x)] in (1, 4)):
                problems.append(f"{label}: ({t}, {x}) is not on a deck")
            if count != len(at_):
                problems.append(f"{label}: {len(at_)} places for {count}")
    for t in (44, 72, 127):
        for x in (190, 290):
            i = f.r1 - 1 - int(round(at(t, 380)))
            if f.kind[i, x] != 4:
                problems.append(f"shuttle pad at t={t} x={x}: not a pad")
    i5 = len(SECTIONS) - 1
    t5 = STARTS[i5] + 0.4
    arr = deep_rows(t5).astype(np.float64)
    lum = arr[..., :3] @ np.array((0.299, 0.587, 0.114))
    y = int(round(SCREEN - (seam("deep", i5) - scroll(t5) * FACTORS["deep"]))) - 1
    if 2 <= y < SCREEN - 3:
        d = lambda a_, b_: np.abs(lum[a_] - lum[b_]).mean()  # noqa: E731
        ref = np.median([d(k, k + 1) for k in range(max(0, y - 30), min(SCREEN - 1, y + 30))])
        if d(y, y + 1) / max(ref, 0.5) > 2.0:
            problems.append(f"deep seam 5: a visible jump ({d(y, y + 1) / max(ref, 0.5):.2f}x)")
    px = sum(np.prod(Image.open(fl).size) for fl in OUT.glob("*.png"))
    print(f"level-37 backdrop: {len(list(OUT.glob('*.png')))} files, {px / 1e6:.2f} M px (~{px / 2048 ** 2:.2f} pages "
          f"of 2048x2048 before packing); at most {worst} animated pieces at once; checker "
          f"{chk / max(vis, 1) * 100:.2f} % of {vis} px")
    for p in problems:
        print("PROBLEM", p)
    print("check:", "ok" if not problems else f"{len(problems)} problems")
    return not problems


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--check" in args:
        sys.exit(0 if check() else 1)
    if "--review-a" in args:
        review_a()
        sys.exit(0)
    if "--review" in args:
        review_fall()
        sys.exit(0)
    write_proposal(build_all())
    if "--proposal" not in args:
        review_fall()
