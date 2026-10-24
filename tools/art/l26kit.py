"""Shared helpers of the M8 part F production art (Level 26 "Blackwater", step ART-F, concept round 55):
glow_angler.py, l26_glows.py, rook_loss.py, l26_props.py, backdrop_l26.py and their review material, on top of
l24kit (its held-sprite rule and paste helpers, unchanged) and artkit.

* **Held sprites.** l23kit's rule unchanged (``write``/``load``/``claimed`` are l24kit's): an enemy's root is claimed
  once its ``data.yaml`` exists, a ground target's or drop's ``sprite``/``look`` once a level names it; until then the
  frames wait in ``build/art-held/sprites/``. Rerun the script after the data.
* **The dark look** (``dark``): the review's stand-in for the game's Level 26 light map (gap check m8f §1.4 and §5 F1,
  F3; europa's *Darkness* with round 48's look a): the section's ambient (6 %, 20 % with the *Dark levels assist*)
  times a cold tint; the hydro-kit's twin headlights lighting a 70 degree sector 260 px ahead of the ship with a hard
  edge and a small pool round the hull; Rook's 56 px lamp; per unit a soft **rim** pool (half its size, 0.15) and its
  kind's glow pool; static lights (lamp posts, the barge's lamp); the Sonar Pulse lighting every unit whole (pool 1.0).
  The light is posterized to twelfths, as the concept did. Everything up to the ship is multiplied by it; the glows,
  bullets, pickups' gold halos and the HUD are added after it, ungraded. The marine snow shows only where the light is.
"""
import numpy as np
from PIL import Image

import artkit
import l24kit
from artkit import ROOT
from l24kit import ASSETS_SPRITES, HELD, add_c, claimed, heading_index, load, paste_c, ship, where, write  # noqa: F401

W, H = 480, 540
BACKDROP = ROOT / "assets" / "backdrop" / "level-26"
AMBIENT = 0.06
ASSIST = 0.20
TINT = np.array((0.50, 0.68, 0.92))          # the black water's cold ambient (proposal: underwater_look tint 80aceb)
WARM = np.array((1.0, 0.93, 0.80))           # the headlights: the world at its own colour, slightly warm
LAMP = np.array((1.0, 0.80, 0.52))           # sodium lamps, the barge's lamp, Rook's lamp
GOLD = np.array((255, 196, 64), float)       # the pickups' halo
TEAL = np.array((0, 255, 154), float)
REACH, HALF_ANGLE = 260.0, 35.0
ROOK_LAMP = 56.0
RIM = 0.15

_YY, _XX = np.mgrid[0:H, 0:W].astype(np.float64)


def _grid(h, w):
    if (h, w) == (H, W):
        return _YY, _XX
    return np.mgrid[0:h, 0:w].astype(np.float64)


def cone(px, py, h=H, w=W, reach=REACH, half=HALF_ANGLE):
    """The twin headlights' union: a 70 degree sector 260 px ahead (up the screen), a hard edge, a small hull pool."""
    yy, xx = _grid(h, w)
    dx, dy = xx - px, yy - (py - 10)
    r = np.hypot(dx, dy)
    ang = np.degrees(np.abs(np.arctan2(dx, -dy)))
    inside = np.clip((half + 2 - ang) / 4, 0, 1) * np.clip((reach - r) / 18, 0, 1) * np.clip(-dy / 10, 0, 1)
    along = 1.0 - 0.45 * np.clip(r / reach, 0, 1) ** 1.5
    halo = np.clip(1 - np.hypot(xx - px, yy - py) / 44, 0, 1) ** 1.2 * 0.8
    return np.maximum(inside * along, halo)


def pool(x, y, r, h=H, w=W):
    yy, xx = _grid(h, w)
    return np.clip(1 - np.hypot(xx - x, yy - y) / max(r, 1), 0, 1) ** 1.3


class Dark:
    """One frame's light map: ``ambient`` x the tint, then the pools added; ``light()`` posterizes it."""

    def __init__(self, ambient=AMBIENT, h=H, w=W):
        self.h, self.w = h, w
        self.L = np.ones((h, w, 3)) * ambient * TINT
        self.units = []

    def headlights(self, px, py, k=1.0):
        c = cone(px, py, self.h, self.w)
        self.L = np.maximum(self.L, (c * k)[..., None] * WARM)
        return self

    def lamp(self, x, y, r, k=1.0, col=LAMP):
        self.L = self.L + (pool(x, y, r, self.h, self.w) * k)[..., None] * np.asarray(col)
        return self

    def rim(self, x, y, size, k=RIM):
        """A unit's rim: a soft pool of half its size at 0.15 (F3), so its body shows faintly outside the cone."""
        return self.lamp(x, y, max(10.0, size / 2 + 4), k, (0.85, 0.95, 1.0))

    def light(self):
        return np.round(np.clip(self.L, 0, 1.4) * 12) / 12


def shade(img, L, x0=0, y0=0):
    """``img`` (RGBA) multiplied by the light map crop at (x0, y0) (straight alpha kept)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    h, w = a.shape[:2]
    crop = np.zeros((h, w, 3))
    ys, xs = max(0, y0), max(0, x0)
    ye, xe = min(L.shape[0], y0 + h), min(L.shape[1], x0 + w)
    if ye > ys and xe > xs:
        crop[ys - y0:ye - y0, xs - x0:xe - x0] = L[ys:ye, xs:xe]
    a[..., :3] = a[..., :3] * crop
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def snow(img, L, t, seed=26, n=110, speed=(6, 22)):
    """The marine snow: motes drifting down on the real clock, drawn only where the light reaches (the cone)."""
    a = np.array(img).astype(np.float64)
    rng = np.random.default_rng(seed)
    h, w = a.shape[:2]
    for _ in range(n):
        x0, y0, v, b = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(*speed), rng.uniform(0.4, 1.0)
        y = int((y0 + t * v) % h)
        x = int((x0 + 4 * np.sin(t * 0.7 + x0)) % w)
        lv = float(L[y, x].mean())
        if lv < 0.12:
            continue
        c = 40 + 170 * b * min(1.0, lv)
        a[y, x, :3] = np.maximum(a[y, x, :3], (c, c + 16, c + 22))
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def gold_halo(r=18):
    """A pickup's warm gold halo (additive, stepped): the game's look for pickups in the dark (F13)."""
    s = 2 * r + 1
    yy, xx = np.mgrid[0:s, 0:s].astype(np.float64)
    d = np.hypot(xx - r, yy - r) / r
    f = np.round(np.clip(1 - d, 0, 1) ** 1.1 * 4) / 4
    rgb = GOLD[None, None] * f[..., None]
    return artkit.additive(Image.fromarray(np.dstack([rgb, np.full((s, s), 255.0)]).astype(np.uint8), "RGBA"))


def black_floor(w=W, h=H, y0=0):
    """The black water's floor: Level 26's production tile when backdrop_l26.py has written it, else a dark plate."""
    tiles = sorted(BACKDROP.glob("black-water*.png"))
    if tiles:
        src = Image.open(tiles[0]).convert("RGBA")
        out = Image.new("RGBA", (w, h))
        sh = src.height
        for oy in range(-(y0 % sh), h, sh):
            out.paste(src.crop((0, 0, min(w, src.width), sh)), (0, oy))
        return out
    n = np.random.default_rng(2601).normal(0, 1, (h, w))
    rgb = np.clip(np.array((14, 20, 26.0)) + 3 * n[..., None], 0, 255)
    return Image.fromarray(np.dstack([rgb, np.full((h, w), 255.0)]).astype(np.uint8), "RGBA")


def frame(ground, solids=(), glows=(), ship_xy=None, rook_xy=None, ambient=AMBIENT, lamps=(), t=0.0, sonar=False,
          over=(), adds=(), snow_n=110, headlights=True, rook_lamp=True):
    """A Level 26 frame: ``ground`` (RGBA, the field) and ``solids`` [(img, x, y, rim size or None)] graded by the dark
    light map, then ``glows`` [(additive img, x, y, k)] and ``adds`` (more additive light) added, ``over`` [(img, x, y)]
    (bullets, pickups, markers) drawn bright on top. ``lamps`` [(x, y, r, k, col)] are static light pools."""
    d = Dark(ambient)
    for x, y, r, k, col in lamps:
        d.lamp(x, y, r, k, col)
    if ship_xy is not None and headlights:
        d.headlights(*ship_xy)
    if rook_xy is not None and rook_lamp:
        d.lamp(rook_xy[0], rook_xy[1] - 4, ROOK_LAMP, 0.95)
    for img, x, y, size in solids:
        if size:
            if sonar:
                d.lamp(x, y, size / 2 + 8, 1.0, (1, 1, 1))
            else:
                d.rim(x, y, size)
    L = d.light()
    base = ground.copy()
    for img, x, y, size in solids:
        paste_c(base, img, x, y)
    if rook_xy is not None:
        try:
            paste_c(base, load("rook-hydro")[2], *rook_xy)
        except FileNotFoundError:
            paste_c(base, artkit.load_frames("rook")[2], *rook_xy)
    if ship_xy is not None:
        paste_c(base, ship(2), *ship_xy)
    out = shade(base, L)
    out = snow(out, L, t, n=snow_n)
    for g, x, y, k in glows:
        out = add_c(out, g, x, y, k)
    for g, x, y, k in adds:
        out = add_c(out, g, x, y, k)
    for img, x, y in over:
        paste_c(out, img, x, y)
    return out, L


def light_image(L):
    return Image.fromarray((np.clip(L / 1.2, 0, 1) * 255).astype(np.uint8), "RGB").convert("RGBA")


__all__ = ["ASSIST", "AMBIENT", "BACKDROP", "Dark", "GOLD", "H", "HELD", "LAMP", "RIM", "ROOK_LAMP", "TEAL", "W",
           "add_c", "black_floor", "claimed", "cone", "frame", "gold_halo", "heading_index", "light_image", "load",
           "paste_c", "pool", "shade", "ship", "snow", "where", "write", "l24kit"]
