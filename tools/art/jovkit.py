"""Shared helpers of the Act 6 Jovian backdrops (M10; design/world/jovian): the pieces Level 36's backdrop
(tools/art/backdrop_l36.py, step ART-B) made first and Level 37's (tools/art/backdrop_l37.py, step ART-C2) and the
storm kit of Level 38 reuse, moved here unchanged (step ART-C2: Level 36's files stay byte-identical).

* **The clock** (``Clock``): content's ``LevelScroll`` ported for a level without halts (a section's speed change eased
  over its ramp), the seams (``LevelData.seam``, rounded as ``BackdropTiles`` does) and the placement times of pieces.
* **The one cream** of every cloud (round 72 A.12 a, the user: "clouds at different altitudes do not have different
  colors"): ``CREAM`` for the deep layer's cloud heads, the low-air tops and wisps and the foreground banks alike; the
  Level 36 grade's ramps (``JOV_SUN``, ``RUST``, ``STORM``) and hazes.
* **Smooth fields**: every band, haze, cloud and glow is a float field written unquantised (far more than 32 steps, no
  ordered dither: the 2026-10-30 dither audit, ``DitherGuardTest``); a glow is additive, premultiplied on black, drawn
  once (``glow``, ``additive_rgb``).
* **Clouds**: cumulus heads from blobs (``mass_blobs``, ``billow_height``, ``billow_shade``, ``cloud_mass``), streaks,
  a separable float blur, the cream-veil bank (``bank``).
* **Tiles**: ``cut_tile`` (a section's rows of one continuous field, its rows past the section crossfading back so it
  wraps), ``clear_rows`` (a transparent tile kept clear where another section's tile set meets it).
* **Checks**: ``wrap_score``/``wrap_score_alpha`` (BackdropSeamsTest's measure) and ``checker_share``
  (DitherGuardTest's).
"""
import numpy as np
from PIL import Image

from artkit import TAU
from render import raster  # noqa: E402

W = 480

# The one cream ramp of every cloud (A.12 a): shade 0 (the underside) .. 1 (the lit top) - round 72's bank tones a.
CREAM = [(0.0, (196, 160, 120)), (0.5, (236, 214, 178)), (1.0, (255, 248, 230))]
JOV_SUN = [(0.0, (110, 50, 28)), (0.25, (168, 92, 50)), (0.5, (206, 146, 86)), (0.75, (236, 204, 150)),
           (1.0, (252, 240, 214))]                                         # act6_r72.JOV_SUN (the L36 grade)
RUST = [(0.0, (78, 32, 20)), (0.3, (128, 54, 30)), (0.55, (166, 80, 44)), (0.8, (206, 140, 88)),
        (1.0, (236, 204, 156))]
STORM = [(0.0, (46, 38, 36)), (0.35, (82, 70, 62)), (0.65, (120, 106, 94)), (1.0, (164, 150, 132))]
HAZE = np.array((236, 224, 204), float)                                    # the pale haze (sky_l36's)
HIGH_HAZE = np.array((226, 224, 222), float)                               # the dive's high haze, a touch cooler
BANK_ALPHA = 0.65                                                          # the veil: a body under it at ~35 %


# =========================================================================== the clock (content LevelScroll, ported)

class Clock:
    """A level's scroll clock from its sections (``end``, ``speed``, ``ramp``) for a level without halts."""

    def __init__(self, sections, base_speed, factors, screen=540):
        self.sections = sections
        self.base_speed = float(base_speed)
        self.factors = factors
        self.screen = screen
        self.end = float(sections[-1]["end"])
        self.starts = [0.0] + [float(s["end"]) for s in sections[:-1]]

    def _speed(self, i):
        s = self.sections[i]
        return float(s["speed"]) if s.get("speed") is not None else self.base_speed

    @staticmethod
    def _u(t, start, ramp):
        return min(max((t - start) / ramp, 0.0), 1.0) if ramp > 0 else (1.0 if t >= start else 0.0)

    @staticmethod
    def _ease_out(u):
        return u - u ** 3 + u ** 4 / 2

    def _travelled(self, i, a, b):
        """backdrop_l30.py's port of LevelScroll for a level without halts: a speed change eased over the ramp."""
        speed, start, end = self._speed(i), self.starts[i], float(self.sections[i]["end"])
        px = (b - a) * speed
        ramp = float(self.sections[i].get("ramp", 0) or 0)
        if i > 0 and ramp > 0:
            ramp = min(ramp, end - start)
            px -= (speed - self._speed(i - 1)) * ramp * (self._ease_out(self._u(b, start, ramp))
                                                         - self._ease_out(self._u(a, start, ramp)))
        return px

    def scroll(self, t):
        """LevelData.scrollAt: the ground scroll at level time t, px (before the start at the first speed)."""
        s = 0.0
        for i, sec in enumerate(self.sections):
            if t < float(sec["end"]) or i == len(self.sections) - 1:
                return s + self._travelled(i, self.starts[i], t)
            s += self._travelled(i, self.starts[i], float(sec["end"]))
        raise AssertionError

    def speed_at(self, t, dt=1e-3):
        return (self.scroll(t + dt) - self.scroll(t - dt)) / (2 * dt)

    def time_at(self, s):
        lo, hi = -120.0, self.end + 60
        for _ in range(80):
            mid = (lo + hi) / 2
            if self.scroll(mid) < s:
                lo = mid
            else:
                hi = mid
        return (lo + hi) / 2

    def seam(self, layer, i):
        """LevelData.seam, rounded as BackdropTiles does: the layer row where section i's tile set begins."""
        return int(round(self.scroll(self.starts[i]) * self.factors[layer] + self.screen))

    def t_for_layer_row(self, row, factor):
        """The placement time putting a piece's centre on layer row ``row`` (centre = scroll(t) x factor + 270)."""
        return round(self.time_at((row - self.screen / 2) / factor), 3)

    def t_centre_at(self, t, y_down, factor):
        """The placement time of a piece whose centre is at screen y (down) at level time t."""
        return self.t_for_layer_row(self.scroll(t) * factor + (self.screen - y_down), factor)

    def section_at_row(self, layer, row):
        i = 0
        for k in range(1, len(self.sections)):
            if row >= self.seam(layer, k):
                i = k
        return i


# =========================================================================== shared fields

def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def blur(a, sigma, wrap_rows=False):
    """A separable Gaussian blur in float (no 8-bit steps); rows wrap for a tile."""
    if sigma <= 0:
        return a
    r = int(np.ceil(3 * sigma))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, ((r, r), (0, 0)), mode="wrap" if wrap_rows else "edge")
    a = sum(k[i] * p[i:i + a.shape[0], :] for i in range(2 * r + 1))
    p = np.pad(a, ((0, 0), (r, r)), mode="edge")
    return sum(k[i] * p[:, i:i + a.shape[1]] for i in range(2 * r + 1))


def streaks(h, w, seed, cell=8, squeeze=6):
    """Fine horizontal streaks: noise drawn narrow and stretched along x (act6_r72.bands' streak)."""
    s = raster.fbm(max(8, w // squeeze), h, cell, seed, octaves=3, period=False)
    img = Image.fromarray(np.clip(s * 255, 0, 255).astype(np.uint8)).resize((w, h), Image.BICUBIC)
    return np.array(img, np.float64) / 255


def billow_height(h, w, blobs, wrap=False):
    """Towering cloud heads as a smooth height field from blobs (x, y_down, radius); rows wrap when ``wrap``."""
    hgt = np.zeros((h, w))
    for bx, by, rr in blobs:
        for dy in ((-h, 0, h) if wrap else (0,)):
            cy = by + dy
            x0, x1 = int(max(0, bx - 2 * rr)), int(min(w, bx + 2 * rr + 1))
            y0, y1 = int(max(0, cy - 2 * rr)), int(min(h, cy + 2 * rr + 1))
            if x0 >= x1 or y0 >= y1:
                continue
            yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64)
            d2 = ((xx - bx) ** 2 + (yy - cy) ** 2) / rr ** 2
            hgt[y0:y1, x0:x1] = np.maximum(hgt[y0:y1, x0:x1], rr * np.sqrt(np.clip(1 - d2 / 2.2, 0, 1)))
    return blur(hgt, 2.0, wrap)


def billow_shade(hgt, wrap=False, edge=None):
    """(shade 0..1 lit from the top left with soft self-shadowing and dark creases between the heads, coverage
    0..1) of a height field (act6_r72.billows' lighting, its creases deepened); ``edge`` (0..1 noise) frays the
    outline."""
    alpha = smoothstep(3, 10, hgt + (0 if edge is None else 9 * (edge - 0.5)))
    if wrap:
        p = np.pad(hgt, ((1, 1), (0, 0)), mode="wrap")
        gy, gx = np.gradient(p)
        gy, gx = gy[1:-1], gx[1:-1]
    else:
        gy, gx = np.gradient(hgt)
    shade = np.clip(0.5 - (gx + gy) * 0.42 + hgt / 80, 0, 1)
    sh = np.roll(np.roll(hgt, 6, 0), 6, 1)
    shade *= 1 - 0.38 * smoothstep(0, 10, sh - hgt)
    crease = np.clip(blur(hgt, 5.0, wrap) - hgt, 0, None)
    shade *= 1 - 0.5 * smoothstep(0, 6, crease)
    return blur(shade, 0.6, wrap), alpha


def cloud_mass(h, w, blobs, seed, wrap=False, detail=0.4):
    """Cumulus heads: the blobs' height field broken up by noise (cauliflower edges, no clean spheres), lit from
    the top left -> (shade 0..1, coverage 0..1)."""
    hgt = billow_height(h, w, blobs, wrap)
    n1 = raster.fbm(w, h, 16, seed, octaves=2, period=wrap)
    n2 = raster.fbm(w, h, 36, seed + 1, octaves=2, period=wrap)
    hgt = hgt * (1 - detail / 2 + detail * (0.4 * n1 + 0.6 * n2))
    return billow_shade(blur(hgt, 0.7, wrap), wrap, n1)


def mass_blobs(rng, cx, cy, rx, ry, n, size):
    """Blobs in an ellipse round (cx, cy), bigger toward its middle (a cloud's towering core)."""
    out = []
    for _ in range(n):
        a, u = rng.uniform(0, TAU), np.sqrt(rng.uniform(0, 1))
        k = 1 - 0.55 * u
        out.append((cx + np.cos(a) * u * rx, cy + np.sin(a) * u * ry, rng.uniform(*size) * k))
    return out


# =========================================================================== tiles

def cut_tile(field, r0, a, b, w=W):
    """A tile set of the rows [a, b) of the field (y-up), its height the span + 160 rounded up to 32; the rows past b
    crossfade back into the rows below a, so the tile wraps. Returns the image (top row = its highest)."""
    span = b - a
    h = int(np.ceil((span + 160) / 32) * 32)
    out = np.zeros((h, w, 3))
    for j in range(h):
        r = a + ((j - a) % h)
        if r < b:
            val = field[r - r0]
        else:
            wgt = smoothstep(b, a + h, r)
            val = (1 - wgt) * field[r - r0] + wgt * field[r - h - r0]
        out[h - 1 - j] = val
    return Image.fromarray(np.clip(np.round(out), 0, 255).astype(np.uint8), "RGB").convert("RGBA")


def clear_rows(h, rows, width=110):
    """A tile's alpha multiplier: 0 over the tile rows of the layer rows ``rows`` (where another section's tile set
    meets this one), easing back over ``width`` rows (the image is top row = highest)."""
    m = np.ones(h)
    for lr in rows:
        j = np.arange(h)
        d = np.abs(((j - lr % h) + h / 2) % h - h / 2)
        m = np.minimum(m, smoothstep(width * 0.35, width, d))
    return m[::-1][:, None]


def rgba(rgb, alpha):
    a = np.clip(np.round(np.asarray(alpha) * 255), 0, 255)
    rgb = np.where(a[..., None] > 0, np.clip(np.round(rgb), 0, 255), 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


def soft_box(w, h, edge):
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    d = np.minimum.reduce([xx + 0.5, w - 0.5 - xx, yy + 0.5, h - 0.5 - yy])
    return smoothstep(1.0, edge, d)


def bank(w, h, seed):
    """A foreground bank: a cloud mass (heads towering in its middle, lumpy all round) in the cream veil, at most
    BANK_ALPHA; cropped to its own bounds, so the image is its bank's rectangle."""
    rng = np.random.default_rng(seed)
    blobs = mass_blobs(rng, w / 2, h / 2, w * 0.4, h * 0.28, int(w * h / 420), (h * 0.11, h * 0.24))
    shade, cov = cloud_mass(h, w, blobs, seed + 1)
    yy = np.mgrid[0:h, 0:w][0].astype(np.float64)
    shade = np.clip(shade - 0.18 * smoothstep(h * 0.35, h * 0.9, yy), 0, 1)      # the underside a step darker
    a = np.clip(cov, 0, 1) * soft_box(w, h, 4) * BANK_ALPHA
    img = rgba(raster.ramp(CREAM, shade), a)
    x0, y0, x1, y1 = img.getbbox()
    img = img.crop((x0 - 1, y0 - 1, x1 + 1, y1 + 1))
    return img.crop((0, 0, img.width + img.width % 2, img.height + img.height % 2))


# =========================================================================== glows (additive, premultiplied on black)

def glow(light, x, y, r, col, k):
    h, w = light.shape[:2]
    rr = int(np.ceil(r * 4))
    x0, x1, y0, y1 = max(0, int(x - rr)), min(w, int(x + rr) + 1), max(0, int(y - rr)), min(h, int(y + rr) + 1)
    if x0 >= x1 or y0 >= y1:
        return
    yy, xx = np.mgrid[y0:y1, x0:x1].astype(np.float64) + 0.5
    f = np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k
    light[y0:y1, x0:x1] += f[..., None] * np.array(col, float)


def additive_rgb(light):
    """A float light field (0..255+) -> premultiplied on black, alpha 1 wherever it adds light (unquantised)."""
    rgb = np.clip(np.round(light), 0, 255)
    a = np.where(rgb.max(axis=-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([rgb, a]).astype(np.uint8), "RGBA")


# =========================================================================== checks

def wrap_score(a):
    lum = a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))
    h = len(lum)

    def change(i, j):
        d = np.sort(np.abs(lum[i % h] - lum[j % h]))
        return d[:int(len(d) * 0.9)].mean()
    ref = sorted(change(k, k + 1) for k in range(h - 40, h + 40) if k < h - 4 or k > h + 2)
    return max(change(h - 2, h - 1), change(h - 1, 0), change(0, 1)) / max(ref[len(ref) // 2], 0.5)


def wrap_score_alpha(a):
    """A transparent tile's wrap: the premultiplied change across the wrap vs. the mean change between rows."""
    pm = (a[..., :3].astype(float) @ np.array((0.299, 0.587, 0.114))) * a[..., 3] / 255
    rows = np.abs(np.diff(pm, axis=0)).mean(axis=1)
    return np.abs(pm[-1] - pm[0]).mean() / max(np.percentile(rows, 95), 0.05)


def checker_share(img):
    """DitherGuardTest.checkerCells: the share of visible pixels that are a 1 px checker cell."""
    a = np.array(img.convert("RGBA")).astype(np.int64)
    c = (a[..., 0] << 24) | (a[..., 1] << 16) | (a[..., 2] << 8) | a[..., 3]
    vis = a[1:-1, 1:-1, 3] > 0
    mid = c[1:-1, 1:-1]
    left, right, up, down = c[1:-1, :-2], c[1:-1, 2:], c[:-2, 1:-1], c[2:, 1:-1]
    chk = vis & (left == right) & (left != mid) & (up == down) & (up != mid)
    return int(vis.sum()), int(chk.sum())
