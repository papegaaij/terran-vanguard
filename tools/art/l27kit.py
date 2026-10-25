"""Shared helpers of the M8 part G production art (Level 27 "Hydrothermal Vents", step ART-G, concept round 57):
backdrop_l27.py, l27_props.py, depth_hunter.py, relay_beacon.py and their review material, on top of l24kit / l26kit
(their held-sprite rule, paste helpers and light-map pieces, imported unchanged) and artkit.

* **Held sprites.** l23kit's rule (an enemy's root is claimed once ``design/enemies/<category>/<slug>/data.yaml``
  exists, a ground target's or drop's ``sprite``/``look`` once a level names it), plus one root of this part: the
  plumes' ``heat-plume*`` once a level's ``vents:`` block names ``look: heat-plume`` (vanguard.pipeline.SpriteUse claims
  a vents look's root). ``install()`` adds that rule to l23kit in this process (the file itself is not changed).
* **The level's clock and layout** (``scroll``, ``target_centre``, ``exact_t``; ``PLUME_ROWS``, ``TARGETS``): the data's
  when ``level-27-hydrothermal-vents/data.yaml`` exists, else the README's sections and ART-G's proposal (the plume rows'
  xs, the ground targets' places), which backdrop_l27.py writes into the proposal file for step G2.
* **The vent light** (``frame``): the review's stand-in for the game's under-water grade and light map at Level 27's
  proposed ``underwater_look`` (gap check m8g §5 G10): not dark; the section's ambient times its tint (dim blue 0.45 with
  the headlights in sections 1 and 4, vent-lit amber 0.6 in sections 2-3, the gate's teal 0.6 in section 5, eased over a
  section's first 2 s), the smokers' amber pools (the static ``lights:``) and each plume's pool by its phase; the light
  posterized to twelfths. Glows, bullets and additive light are added after it, ungraded.
"""
import math

import numpy as np
import yaml
from PIL import Image

import artkit
import l23kit
import l26kit
from artkit import DESIGN, ROOT
from l24kit import ASSETS_SPRITES, HELD, add_c, heading_index, load, paste_c, ship, where, write  # noqa: F401

W, SCREEN = 480, 540
LEVEL_DIR = DESIGN / "campaign" / "act-4-deep-water" / "level-27-hydrothermal-vents"
BACKDROP = ROOT / "assets" / "backdrop" / "level-27"
OUTRO = 15
PLUME_ROOT = "heat-plume"

# --------------------------------------------------------------------------- held sprites (the plume's root)

_plain_claimed = l23kit.claimed


def _plume_claimed():
    """The level's vents name the look and vanguard.pipeline.SpriteUse claims the root (step G4 adds it)."""
    if f'"{PLUME_ROOT}"' not in l23kit.SPRITE_USE.read_text(encoding="utf-8"):
        return False
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        vents = (l23kit._yaml(f) or {}).get("vents") or {}
        if isinstance(vents, dict) and vents.get("look") == PLUME_ROOT:
            return True
    return False


def claimed(name):
    if l23kit._rooted(name, PLUME_ROOT):
        return _plume_claimed()
    return _plain_claimed(name)


def install():
    """l23kit's ``write``/``load`` decide by its module's ``claimed``: point it at this rule for this process."""
    l23kit.claimed = claimed


install()

# --------------------------------------------------------------------------- the clock

DATA_PATH = LEVEL_DIR / "data.yaml"
DATA = yaml.safe_load(DATA_PATH.read_text(encoding="utf-8")) if DATA_PATH.exists() else None
FALLBACK_SECTIONS = [{"end": 24, "speed": 120}, {"end": 56, "speed": 110, "ramp": 2},
                     {"end": 90, "speed": 110}, {"end": 124, "speed": 120, "ramp": 2},
                     {"end": 150, "speed": 130, "ramp": 2}]
SECTIONS = DATA["sections"] if DATA else FALLBACK_SECTIONS
BASE_SPEED = float(DATA.get("scroll_speed", 120)) if DATA else 120.0
END = float(SECTIONS[-1]["end"])
STARTS = [0.0] + [float(s["end"]) for s in SECTIONS[:-1]]


def _speeds():
    out, prev = [], None
    for start, s in zip(STARTS, SECTIONS):
        sp = float(s.get("speed", BASE_SPEED if prev is None else prev))
        out.append((start, float(s["end"]), sp, float(s.get("ramp", 2)), prev))
        prev = sp
    return out


SPEEDS = _speeds()
DT = 1 / 600


def speed_at(t):
    v = SPEEDS[0][2]
    for start, end, sp, ramp, prev in SPEEDS:
        if t >= start:
            if ramp and prev is not None and prev != sp and t < start + ramp:
                u = (t - start) / ramp
                v = prev + (sp - prev) * u * u * (3 - 2 * u)
            else:
                v = sp
    return v


_T = np.arange(0, END + OUTRO + 16, DT)
_S = np.concatenate([[0.0], np.cumsum([speed_at(t + DT / 2) * DT for t in _T[:-1]])])


def scroll(t):
    return float(np.interp(t, _T, _S))


def time_at(s):
    return float(np.interp(s, _S, _T))


def seam_row(index):
    return scroll(STARTS[index]) + SCREEN


def target_centre(t, h):
    """The ground row of the centre of a thing h px tall that enters at the top edge at t."""
    return scroll(t) + SCREEN + h / 2


def place_t(centre_row):
    return time_at(centre_row - SCREEN / 2)


def exact_t(bottom, h):
    """The placement time that puts a piece h px tall with its bottom row exactly on ground row ``bottom``."""
    t = place_t(bottom + h / 2)
    for digits in (3, 4, 5, 6):
        tt = round(t, digits)
        if round(scroll(tt) + SCREEN / 2 - h / 2) == bottom:
            return tt
    return round(t, 6)


def section_at_t(t):
    s = 0
    for i, st in enumerate(STARTS):
        if t >= st:
            s = i
    return s


def section_at_row(row):
    s = 0
    for i in range(1, len(SECTIONS)):
        if row >= seam_row(i):
            s = i
    return s


def screen_y(t, row):
    """The screen y (down) of ground row ``row`` at level time t."""
    return scroll(t) + SCREEN - 1 - row


# --------------------------------------------------------------------------- the layout (data, or ART-G's proposal)

# ART-G's proposal (README "Hazards", gap check §1.2): 16 rows at the README's times, 2-3 discs each, the ripple's
# order alternating ends, neighbouring discs >= 164 px apart centre to centre (a gap of >= 100 px between 64 px discs;
# the validator's 100 px centre rule holds with room); the first row (t=26) alone, before any wave reaches it.
PROPOSED_PLUMES = [
    (26, [120, 360]), (33, [230, 60]), (40, [250, 420]), (47, [420, 250, 80]), (52, [160, 380]),
    (58, [410, 240, 70]), (65, [60, 400]), (72, [290, 60]), (79, [400, 215]), (85, [410, 240, 70]),
    (94, [240, 420]), (101, [250, 80]), (108, [60, 420]), (115, [420, 240]), (126, [80, 250, 420]),
    (133, [400, 160])]


def _plumes():
    if DATA and isinstance(DATA.get("vents"), dict) and DATA["vents"].get("rows"):
        return [(float(r["t"]), [float(x) for x in r["xs"]]) for r in DATA["vents"]["rows"]]
    return [(float(t), [float(x) for x in xs]) for t, xs in PROPOSED_PLUMES]


PLUME_ROWS = _plumes()
PLUME_WIDTH = 64
TIMING = {"easy": (1.8, 1.6, 6.0), "medium": (1.4, 1.6, 4.5), "hard": (1.0, 1.6, 4.0)}   # tell, burn, period
RIPPLE = 0.5

# The ground targets' places (kind, t, x) as ART-G proposes them for G2 (README "Ground targets", "Secrets"; the
# Siren nest is gap check §5 G8's ground-mounted `turret nest`); x per unit. The pieces under them are drawn for these.
PROPOSED_TARGETS = (
    [("spine-turret", 34, x) for x in (340, 384, 428)]                       # the nest on a smoker chimney (stack 1)
    + [("marlin", 40, 90)]                                                   # the secret, nose-down by a stack
    + [("polyp-mortar", 80, x) for x in (70, 128)]                           # on a mound
    + [("siren", 81, x) for x in (360, 430)]                                 # grown on a chimney stack (stack 2)
    + [("relay-beacon", 94, 80), ("relay-beacon", 101, 400), ("relay-beacon", 108, 240), ("relay-beacon", 116, 90)]
    + [("spine-turret", 99.5, x) for x in (130, 172)] + [("spine-turret", 99.5, 310)]   # the ridge's shoulders
)
HARD_EXTRA = [("spine-turret", 99.5, 88), ("spine-turret", 99.5, 352)]       # hard's 5 on the shoulders
WRECK = (70.0, 175.0)                    # the hull-code wreck (a placed piece): mid-screen at t=70, x 175
SIZES = {"spine-turret": 40, "polyp-mortar": 44, "siren": 56, "relay-beacon": 96, "marlin": 64}


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
        kind = g.get("enemy") or ("marlin" if g.get("trigger") else g.get("sprite", "?"))
        for at in _ats(g):
            out.append((str(kind), float(at[0]), float(at[1])))
    for w in DATA.get("waves") or []:
        if isinstance(w, dict) and w.get("on_ground"):
            xs = w["on_ground"] if isinstance(w["on_ground"], list) else (w["on_ground"].get("medium") or [])
            for x in xs:
                out.append((str(w.get("enemy", "siren")), float(w["t"]), float(x)))
    return out or list(PROPOSED_TARGETS)


TARGETS = _targets()
BEACONS = [(t, x) for k, t, x in TARGETS if k == "relay-beacon"] or [(94, 80), (101, 400), (108, 240), (116, 90)]


# --------------------------------------------------------------------------- the vent light (review stand-in)

BLUE = np.array((0.50, 0.67, 0.92))          # 80aceb: Levels 25-26's dim blue
AMBER = np.array((0.88, 0.74, 0.62))         # e0bd9e: the vent-lit water (the colour is in the pools)
TEALT = np.array((0.50, 0.90, 0.84))         # 80e6d6: the hatchery's teal
POOL = np.array((1.0, 0.62, 0.25))           # ff9e40: a smoker's or a plume's amber pool
LOOKS = [  # ART-G's per-section underwater_look proposal (round 57; gap check §5 G10): (ambient, tint, headlights)
    (0.45, BLUE, 0.8), (0.60, AMBER, 0.0), (0.62, AMBER, 0.0), (0.45, BLUE, 0.8), (0.60, TEALT, 0.0)]
LOOK_YAML = [
    "{ambient: 0.45, tint: 80aceb, caustics: 0.0, snow: 90, headlights: {strength: 0.8, reach: 260, angle: 70}}",
    "{ambient: 0.60, tint: e0bd9e, caustics: 0.0, snow: 70, headlights: {strength: 0.0}}",
    "{ambient: 0.62, tint: e0bd9e, caustics: 0.0, snow: 70, headlights: {strength: 0.0}}",
    "{ambient: 0.45, tint: 80aceb, caustics: 0.0, snow: 90, headlights: {strength: 0.8, reach: 260, angle: 70}}",
    "{ambient: 0.60, tint: 80e6d6, caustics: 0.0, snow: 60, headlights: {strength: 0.0}}"]


def look_at(t):
    """(ambient, tint, headlights) at t: the section's, eased from the previous over its first 2 s."""
    s = section_at_t(t)
    amb, tint, head = LOOKS[s]
    if s > 0 and t < STARTS[s] + 2:
        u = (t - STARTS[s]) / 2
        u = u * u * (3 - 2 * u)
        pa, pt, ph = LOOKS[s - 1]
        return pa + (amb - pa) * u, pt + (tint - pt) * u, ph + (head - ph) * u
    return amb, tint, head


def plume_phase(t, row_t, i, difficulty="medium"):
    """A disc's phase at t: ('cold' | 'tell' | 'burn', u in 0..1) on the stand-in clock: the row's cycle starts when it
    enters, each disc ``RIPPLE`` s after the previous."""
    tell, burn, period = TIMING[difficulty]
    if t < row_t:
        return "cold", 0.0
    c = (t - row_t - i * RIPPLE) % period if t - row_t - i * RIPPLE >= 0 else -1
    if c < 0 or c >= tell + burn:
        return "cold", 0.0
    if c < tell:
        return "tell", c / tell
    return "burn", (c - tell) / burn


def plumes_on_screen(t, difficulty="medium"):
    """[(x, screen y, phase, u)] of the discs on the screen at t."""
    out = []
    for row_t, xs in PLUME_ROWS:
        y = screen_y(t, target_centre(row_t, PLUME_WIDTH))
        if -60 < y < SCREEN + 60:
            for i, x in enumerate(xs):
                ph, u = plume_phase(t, row_t, i, difficulty)
                out.append((x, y, ph, u))
    return out


def plume_light(phase, u):
    if phase == "tell":
        return 0.25 + 0.55 * u
    if phase == "burn":
        return 1.0 - 0.3 * u
    return 0.12


def frame(ground, t, solids=(), glows=(), ship_xy=(240.0, 444.0), pools=(), over=(), adds=(), plumes=True,
          snow_n=70):
    """A Level 27 frame: ``ground`` (RGBA, the field) and ``solids`` [(img, x, y)] lit by the vent light at t, then
    ``glows`` and ``adds`` [(additive img, x, y, k)] added, ``over`` [(img, x, y)] drawn bright on top. ``pools``
    [(x, y, r, k)] are amber static pools (the smokers' mats); the plumes' pools follow their phase."""
    amb, tint, head = look_at(t)
    d = l26kit.Dark(amb)
    d.L = np.ones((SCREEN, W, 3)) * amb * tint
    for x, y, r, k in pools:
        d.lamp(x, y, r, k * 0.55, POOL)
    if plumes:
        for x, y, ph, u in plumes_on_screen(t):
            d.lamp(x, y, 60 + (30 * u if ph == "burn" else 0), plume_light(ph, u) * 0.75, POOL)
    if ship_xy is not None and head > 0.01:
        c = l26kit.cone(*ship_xy)
        d.L = d.L + (c * head * 0.5)[..., None] * l26kit.WARM
    L = d.light()
    base = ground.copy()
    for img, x, y in solids:
        paste_c(base, img, x, y)
    if ship_xy is not None:
        paste_c(base, ship(2), *ship_xy)
    out = l26kit.shade(base, L)
    out = l26kit.snow(out, L, t, n=snow_n)
    for g, x, y, k in list(glows) + list(adds):
        out = add_c(out, g, x, y, k)
    for img, x, y in over:
        paste_c(out, img, x, y)
    return out


def floor_plate(w=W, h=SCREEN, t=60.0):
    """A plate of the level's floor at t (backdrop_l27.py's tiles when written), unlit; else a basalt plate."""
    s = section_at_t(t)
    names = ["vent-edge", "smoker-field", "chimney-garden", "ridge-basalt", "bowl-floor"]
    f = BACKDROP / f"{names[min(s, 4)]}.png"
    if f.exists():
        src = Image.open(f).convert("RGBA")
        out = Image.new("RGBA", (w, h))
        y0 = int(scroll(t)) % src.height
        for oy in range(-y0, h, src.height):
            out.paste(src.crop((0, 0, min(w, src.width), src.height)), (0, oy))
        return out
    n = np.random.default_rng(2701).normal(0, 1, (h, w))
    rgb = np.clip(np.array((44, 40, 42.0)) + 5 * n[..., None], 0, 255)
    return Image.fromarray(np.dstack([rgb, np.full((h, w), 255.0)]).astype(np.uint8), "RGBA")


def lit_sprite(img, t=60.0, k=1.0):
    """A sprite under the flat vent light of t's section (no pools): for review sheets' rows."""
    amb, tint, _ = look_at(t)
    a = np.array(img.convert("RGBA")).astype(np.float64)
    a[..., :3] *= np.round(np.clip(amb * tint * k + 0.25 * POOL * k, 0, 1.4) * 12) / 12
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def dist(a, b):
    return math.hypot(a[0] - b[0], a[1] - b[1])


__all__ = ["ASSETS_SPRITES", "BACKDROP", "BEACONS", "DATA", "END", "HELD", "LEVEL_DIR", "LOOKS", "LOOK_YAML",
           "PLUME_ROWS", "PLUME_WIDTH", "SCREEN", "SECTIONS", "STARTS", "TARGETS", "W", "WRECK", "add_c", "claimed",
           "exact_t", "floor_plate", "frame", "heading_index", "install", "lit_sprite", "load", "paste_c",
           "plume_phase", "plumes_on_screen", "scroll", "screen_y", "section_at_row", "section_at_t", "ship",
           "target_centre", "time_at", "where", "write"]
