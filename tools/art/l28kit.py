"""Shared helpers of the M8 part H production art (Level 28 "Abyssal Maw", step ART-H2, concept round 58):
backdrop_l28.py, l28_props.py and their review material, on top of l27kit / l26kit / l23kit (their held-sprite rule,
paste helpers and light-map pieces, imported unchanged) and artkit.

* **Held sprites.** l23kit's rule (a ground target's or drop's ``sprite`` is claimed once a level's data names it:
  ``recon-sub`` for *Pelagic-3*, ``service-buoy`` for the antechamber's buoy), plus the breakout's FX roots
  ``ice-slab`` and ``ice-shatter``, claimed once vanguard.pipeline.SpriteUse names them (step H5 adds the claim with
  its breakout look). ``install()`` adds the rule to l23kit in this process (the file itself is not changed).
* **The level's clock and layout** (``scroll``, ``target_centre``, ``exact_t``; ``TARGETS``): the data's when
  ``level-28-abyssal-maw/data.yaml`` exists, else the README's ten sections and ART-H2's proposal (the ground targets'
  places), which backdrop_l28.py writes into the proposal file for step H3. ``scroll`` is LevelData.scrollAt
  (content LevelScroll) ported exactly: a section eases from the speed before it over its ``ramp``, a halted arena
  (``speed: 0, arena: true``) stops at once and the section after it eases up from 0 over its ``ramp``.
* **The fixed rows.** The arena: the scroll stands at ``ARENA_SCROLL`` = scroll(the arena's start) while the Maw lives,
  so the screen shows ground rows ARENA_SCROLL .. +539 and the vent's throat at screen (240, 330) is ground row
  ``THROAT_ROW``. The breakout: the first section in air after the water starts at ``BREAK_T`` (t=115); its line is
  the ground row the ship's start height passes then, ``BREAK_ROW`` = scroll(BREAK_T) + 96 (as Level 23's waterline).
* **The hatchery light** (``frame``): the review's stand-in for the game's under-water grade and light map at Level 28's
  proposed per-section ``underwater_look`` (ambient x tint, eased over a section's first 2 s; teal at the gate, teal and
  violet racks, warm amber in the cavern, red-amber in the antechamber, lime-white in the arena, cool light from above
  in the shaft), the static pools (``lights:``) and the glows added after it, ungraded; from the breakout on, air: no
  grade (the grade lifts over 1 s), the night ice is lit in its own pixels.
"""
import math

import numpy as np
import yaml

import l23kit
import l26kit
from artkit import DESIGN, ROOT
from l24kit import ASSETS_SPRITES, HELD, add_c, heading_index, load, paste_c, ship, where, write  # noqa: F401

W, SCREEN = 480, 540
SHIP_Y = 96                                    # sim Ship.START_Y: px above the bottom edge (screen y 444)
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-28-abyssal-maw"
BACKDROP = ROOT / "assets" / "backdrop" / "level-28"
FX_ROOTS = ("ice-slab", "ice-shatter")

# --------------------------------------------------------------------------- held sprites (the breakout's FX)

_plain_claimed = l23kit.claimed


def claimed(name):
    for root in FX_ROOTS:
        if l23kit._rooted(name, root):
            return f'"{root}"' in l23kit.SPRITE_USE.read_text(encoding="utf-8")
    return _plain_claimed(name)


def install():
    """l23kit's ``write``/``load`` decide by its module's ``claimed``: point it at this rule for this process."""
    l23kit.claimed = claimed


install()

# --------------------------------------------------------------------------- the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None

# ART-H2's proposal from the README's Layout table (sections, speeds) with the eases the data would give them.
PROPOSED_SECTIONS = [
    {"name": "Hatchery Gate", "end": 22, "tiles": ["gate-hall"]},
    {"name": "Brood Galleries", "end": 50, "speed": 110, "ramp": 2, "tiles": ["brood-gallery"]},
    {"name": "Spawning Cavern", "end": 80, "speed": 110, "tiles": ["spawning-floor"]},
    {"name": "Antechamber", "end": 104, "speed": 100, "ramp": 2, "tiles": ["antechamber-floor"]},
    {"name": "The Vent", "end": 105, "speed": 0, "arena": True, "tiles": ["antechamber-floor"]},
    {"name": "The Shaft", "end": 115, "speed": 150, "ramp": 2, "tiles": ["shaft-climb", "ice-ceiling"]},
    {"name": "The Breakout", "end": 118, "speed": 150, "medium": "air", "tiles": ["night-ice"]},
    {"name": "The Rise", "end": 122, "speed": 150, "medium": "air", "tiles": ["night-ice", "ice-crystals"]},
    {"name": "The Run", "end": 157, "speed": 200, "ramp": 2, "medium": "air", "tiles": ["night-ice", "ice-crystals"]},
    {"name": "Orbit", "end": 162, "speed": 400, "ramp": 3, "medium": "air", "tiles": ["night-ice", "ice-crystals"]},
]
SECTIONS = DATA["sections"] if DATA else PROPOSED_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speed(i):
    s = SECTIONS[i]
    return float(s["speed"]) if s.get("speed") is not None else BASE_SPEED


def _arena(i):
    return bool(SECTIONS[i].get("arena"))


def _timed_halt(i):
    return _speed(i) == 0 and not _arena(i)


def _ease(i):
    if _timed_halt(i) or _speed(i) == 0 or (_arena(i) and SECTIONS[i].get("speed") is None):
        return 0.0
    return float(SECTIONS[i].get("ramp", 0) or 0)


def _u(t, start, ramp):
    return min(max((t - start) / ramp, 0.0), 1.0) if ramp > 0 else (1.0 if t >= start else 0.0)


def _ease_in(u):
    return u ** 3 - u ** 4 / 2


def _ease_out(u):
    return u - u ** 3 + u ** 4 / 2


def _travelled(i, a, b):
    """content LevelScroll.travelled: the px section i scrolls from a to b s."""
    speed, start, end = _speed(i), STARTS[i], float(SECTIONS[i]["end"])
    px = (b - a) * speed
    if speed == 0:
        return px
    if i + 1 < len(SECTIONS) and _timed_halt(i + 1):
        ramp = min(float(SECTIONS[i + 1].get("ramp", 2)), end - start)
        px -= speed * ramp * (_ease_in(_u(b, end - ramp, ramp)) - _ease_in(_u(a, end - ramp, ramp)))
    if i > 0 and _timed_halt(i - 1):
        ramp = min(float(SECTIONS[i - 1].get("ramp", 2)), end - start)
        px -= speed * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    elif i > 0 and _ease(i) > 0:
        ramp = min(_ease(i), end - start)
        before = _speed(i - 1)
        px -= (speed - before) * ramp * (_ease_out(_u(b, start, ramp)) - _ease_out(_u(a, start, ramp)))
    return px


def scroll(t):
    """LevelData.scrollAt: the ground layer's scroll distance at level time t, px."""
    s = 0.0
    for i, sec in enumerate(SECTIONS):
        if t < float(sec["end"]) or i == len(SECTIONS) - 1:
            return s + _travelled(i, STARTS[i], t)
        s += _travelled(i, STARTS[i], float(sec["end"]))
    raise AssertionError


def time_at(s):
    """The earliest level time the scroll reaches s."""
    lo, hi = -40.0, END + 60
    for _ in range(80):
        mid = (lo + hi) / 2
        if scroll(mid) < s:
            lo = mid
        else:
            hi = mid
    return (lo + hi) / 2


def seam_row(index):
    return scroll(STARTS[index]) + SCREEN


def target_centre(t, h):
    """The ground row of the centre of a thing h px tall that enters at the top edge at t."""
    return scroll(t) + SCREEN + h / 2


def exact_t(bottom, h, factor=1.0):
    """The placement time that puts a piece h px tall with its bottom row exactly on layer row ``bottom`` (a piece's
    centre passes the screen's middle at its time: centre = scroll(t) x factor + 270)."""
    t = time_at((bottom + h / 2 - SCREEN / 2) / factor)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) * factor + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def section_at_t(t):
    s = 0
    for i, st in enumerate(STARTS):
        if t >= st:
            s = i
    return s


def section_at_row(row):
    """The section whose tile set lies at ground row ``row`` (the tile sets change at the seam rows; a halted arena's
    section has no rows of its own)."""
    s = 0
    for i in range(1, len(SECTIONS)):
        if row >= seam_row(i):
            s = i
    return s


def screen_y(t, row):
    """The screen y (down) of ground row ``row`` at level time t."""
    return scroll(t) + SCREEN - 1 - row


ARENA_INDEX = next(i for i in range(len(SECTIONS)) if _arena(i))
ARENA_T = STARTS[ARENA_INDEX]
ARENA_SCROLL = scroll(ARENA_T)                       # the halted screen: ground rows ARENA_SCROLL .. +539
THROAT = (240.0, 330.0)                              # the vent's throat on the halted screen (boss spec, Arena)
THROAT_ROW = int(round(ARENA_SCROLL + SCREEN - 1 - THROAT[1]))


def _break_index():
    wet = (DATA or {}).get("medium", "underwater") == "underwater" if DATA else True
    for i, s in enumerate(SECTIONS):
        m = s.get("medium")
        if m == "air" and wet:
            return i
        if m is not None:
            wet = m == "underwater"
    return 6


BREAK_INDEX = _break_index()
BREAK_T = STARTS[BREAK_INDEX]
BREAK_ROW = int(round(scroll(BREAK_T) + SHIP_Y))     # the breakout line (ground row the ship's row passes at BREAK_T)
RISE_T, RUN_T = STARTS[BREAK_INDEX + 1], STARTS[BREAK_INDEX + 2]

# --------------------------------------------------------------------------- the layout (data, or ART-H2's proposal)

# ART-H2's proposal (README "Ground targets", "Waves", "Secrets"; t = the top edge, x the unit's centre).
PROPOSED_TARGETS = (
    [("spine-turret", 30, x) for x in (96, 240, 384)]                         # one on each rack's end
    + [("siren", 36, x) for x in (60, 420)]                                    # grown on the gallery walls (on_ground)
    + [("recon-sub", 58, 90)]                                                  # Pelagic-3 under the collapsed rack
    + [("spine-turret", 66, x) for x in (300, 344, 388)]                       # the resin ridge (hard + 256, 432)
    + [("polyp-mortar", 74, x) for x in (84, 150)]                             # the far ridge
    + [("siren", 76, x) for x in (330, 410)]                                   # the cavern's last stack (hard + 250)
    + [("polyp-mortar", 92, x) for x in (110, 370)]                            # the caldera's rim
)
HARD_EXTRA = [("spine-turret", 66, 256), ("spine-turret", 66, 432), ("siren", 76, 250)]
BUOY = (88.0, 240.0, 380.0)                     # the service buoy's drop: t, x, y (up)
SIZES = {"spine-turret": 40, "polyp-mortar": 44, "siren": 56, "recon-sub": 64}


def _ats(g):
    a = g.get("at")
    if a is None:
        return []
    return a if isinstance(a, list) and a and isinstance(a[0], list) else [a]


def _targets():
    if not DATA:
        return list(PROPOSED_TARGETS)
    out = []
    for g in DATA.get("ground_targets") or []:
        kind = g.get("enemy") or ("recon-sub" if g.get("trigger") else g.get("sprite", "?"))
        for at in _ats(g):
            out.append((str(kind), float(at[0]), float(at[1])))
    for w in DATA.get("waves") or []:
        if isinstance(w, dict) and w.get("on_ground"):
            xs = w["on_ground"] if isinstance(w["on_ground"], list) else (w["on_ground"].get("medium") or [])
            for x in xs:
                out.append((str(w.get("enemy", "siren")), float(w["t"]), float(x)))
    return out or list(PROPOSED_TARGETS)


TARGETS = _targets()


def targets_of(kind, t0=None, t1=None):
    return [(t, x) for k, t, x in TARGETS + HARD_EXTRA if k == kind and (t0 is None or t0 <= t <= t1)]


# --------------------------------------------------------------------------- the hatchery light (review stand-in)

def _rgb(h):
    return np.array([int(h[i:i + 2], 16) / 255 for i in (0, 2, 4)])


# ART-H2's per-section underwater_look proposal (round 58): (ambient, tint hex, caustics, snow, headlights); None = air.
LOOKS = [(0.55, "80e6d6", 0.0, 60, 0.0),      # 1 the gate: L27's last teal
         (0.52, "a8c8e0", 0.0, 50, 0.0),      # 2 the galleries: cool; the racks' teal and violet are their glows
         (0.58, "e8c09a", 0.0, 60, 0.0),      # 3 the cavern: warm amber from below
         (0.56, "f0a888", 0.0, 70, 0.0),      # 4 the antechamber: the vent's red-amber
         (0.62, "d8f0c0", 0.0, 40, 0.0),      # 5 the arena: lime-white from the throat
         (0.58, "a8d8f0", 0.25, 50, 0.0),     # 6 the shaft: cool light from above, caustics on the ice
         None, None, None, None]              # 7-10 air: the night ice is lit in its own pixels
LOOK_YAML = [None if lk is None else
             f"{{ambient: {lk[0]:.2f}, tint: {lk[1]}, caustics: {lk[2]:.2f}, snow: {lk[3]}, headlights: {{strength: "
             f"{lk[4]:.1f}}}}}" for lk in LOOKS]


def _look(i):
    lk = LOOKS[min(i, len(LOOKS) - 1)]
    if lk is None:
        return 1.0, np.ones(3), 0.0
    return lk[0], _rgb(lk[1]), lk[4]


def look_at(t):
    """(ambient, tint, air share 0..1) at t: the section's, eased from the previous over its first 2 s (1 s for the
    grade's lift at the breakout)."""
    s = section_at_t(t)
    amb, tint, _ = _look(s)
    air = 1.0 if LOOKS[min(s, len(LOOKS) - 1)] is None else 0.0
    ease = 1.0 if s == BREAK_INDEX else 2.0
    if s > 0 and t < STARTS[s] + ease:
        u = (t - STARTS[s]) / ease
        u = u * u * (3 - 2 * u)
        pa, pt, _ = _look(s - 1)
        pair = 1.0 if LOOKS[s - 1] is None else 0.0
        return pa + (amb - pa) * u, pt + (tint - pt) * u, pair + (air - pair) * u
    return amb, tint, air


def frame(ground, t, solids=(), glows=(), ship_xy=(240.0, 444.0), pools=(), over=(), adds=(), snow_n=60):
    """A Level 28 frame: ``ground`` (RGBA, the field) and ``solids`` [(img, x, y)] lit by the hatchery light at t, then
    ``glows`` and ``adds`` [(additive img, x, y, k)] added, ``over`` [(img, x, y)] drawn bright on top. ``pools``
    [(x, y, r, k, colour 0..1)] are the static light pools. In air (from the breakout) the light is 1 (no grade)."""
    amb, tint, air = look_at(t)
    d = l26kit.Dark(amb)
    d.L = np.ones((SCREEN, W, 3)) * amb * tint
    for x, y, r, k, col in pools:
        d.lamp(x, y, r, k * 0.6, col)
    L = d.light() * (1 - air) + air
    base = ground.copy()
    for img, x, y in solids:
        paste_c(base, img, x, y)
    if ship_xy is not None:
        paste_c(base, ship(2), *ship_xy)
    out = l26kit.shade(base, L)
    if air < 1:
        out = l26kit.snow(out, L, t, n=int(snow_n * (1 - air)))
    for g, x, y, k in list(glows) + list(adds):
        out = add_c(out, g, x, y, k)
    for img, x, y in over:
        paste_c(out, img, x, y)
    return out


def lit_sprite(img, t=60.0, k=1.0):
    """A sprite under the flat light of t's section (no pools): for review sheets' rows."""
    amb, tint, air = look_at(t)
    a = np.array(img.convert("RGBA")).astype(np.float64)
    g = np.round(np.clip(amb * tint * k, 0, 1.4) * 12) / 12 * (1 - air) + air
    a[..., :3] *= g
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def dist(a, b):
    return math.hypot(a[0] - b[0], a[1] - b[1])


__all__ = ["ARENA_SCROLL", "ARENA_T", "ASSETS_SPRITES", "BACKDROP", "BREAK_INDEX", "BREAK_ROW", "BREAK_T", "BUOY",
           "DATA", "END", "HARD_EXTRA", "HELD", "LEVEL_DIR", "LOOKS", "LOOK_YAML", "RISE_T", "RUN_T", "SCREEN",
           "SECTIONS", "SHIP_Y", "SIZES", "STARTS", "TARGETS", "THROAT", "THROAT_ROW", "W", "add_c", "claimed",
           "exact_t", "frame", "heading_index", "install", "lit_sprite", "load", "paste_c", "scroll", "screen_y",
           "section_at_row", "section_at_t", "ship", "target_centre", "targets_of", "time_at", "where", "write"]
