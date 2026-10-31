#!/usr/bin/env python3
"""Concept round 72 - the Act 6 "Jovian Storm" bosses, world and people (step ART-R-B, 2026-10-31; everything pending
the user's review in round 72).

Outputs (each PNG a review sheet; storyboards are key frames of the 480x540 play field at 1x drawn by a scripted
scene, laid out at half size with a 1x crop where a detail matters):
  design/enemies/bosses/honour-guard/concept/honour-guard-r72-{a,b}.png
      The three aces (Harker the sniper, Vesper the dancer, Kestrel the feinter) in the Q9 a elite livery (gloss black,
      heavy gold, strong top-left rim, red running lights): the source render, 16 headings at play size (1.5x a Talon,
      72 px), each ace's mark and its tell, their hex shield, the measured gold share, the three over Level 40's
      black space, Callisto's disc and Jupiter's limb at 1x and 2x.
      a "marks on one airframe": the three on the Talon's own airframe, told apart by paint (Harker's sniper barrel
        and red nose band, Vesper's white ribbon stripes, Kestrel's gold chevrons and red tail band).
      b "three silhouettes": each ace's airframe changed so the shape tells it before the paint (Harker's stretched
        nose and long barrel, Vesper's long slim swept wings with trailing ribbons, Kestrel's short body with forward
        canards and twin swept fins).
  design/enemies/bosses/honour-guard/concept/honour-guard-joins-r72-{a,b}.png
      The storyboard of the join order (the sting and the three-segment bar, Harker alone, Vesper's tell from the far
      side, two aces, Kestrel's tell from the rear, three aces under the conductor, the pincer, the first death) and
      the conductor's timeline: three lanes over 60 s, every telegraphed line or dive on its own (a later one waits),
      at most two aces firing at once.
  design/enemies/bosses/ascendant/concept/ascendant-r72-{a,b}.png
      The Ascendant bow-on (Q6 a) in the elite livery: the phase-1 hull with its parts and their states (the bow
      cannon with its hex shield, the two shield generators and their gold beams, the four mirror batteries: whole,
      heating, cracked, down), the phase-2 hull (the keel split, teal-black tissue, the three sacs and the heart), the
      eruption frames, the wreck's two halves, the lifeboat, and the one-atlas-page budget measured from the pieces.
      a "gilded wedge": one smooth wedge, gold ribs fanning down both faces, the Vrell half erupting along the keel
        (symmetric); the lifeboat a round seed-pod with the gold capsule half swallowed.
      b "armoured tiers": the bow stepped in three tiers of identical armour blocks with dark gaps and gold crown
        fins (repeated segments: one block, one fin), the Vrell half bursting through the gaps (asymmetric
        breaches); the lifeboat a long thorned pod, the capsule's canopy at its head, tendrils trailing.
  design/enemies/bosses/ascendant/concept/ascendant-phases-r72-{a,b}.png
      The phase storyboard: the warning before the hull shows, the hull sliding in bow-first, phase 1 (the panels
      from the far side, the spinal lance), the eruption (Vorne's one shout), phase 2 (the sacs, the spore spiral, a
      Chimera brood), the hull breaking (K), the escape (the escape bar `VORNE`, the spore wake) and `ESCAPED`.
  design/world/jovian/concept/jovian-moods-r72-{a,b}.png
      Q10's mood ladder L36-L42 (L37 before and after the turn): each still with its mean luminance and its
      brightest and darkest 200x96 window, and the readability strip: the ship, Rook, an elite ace, a gunmetal unit
      and the enemy bullets (gold needle, magenta orb, a red sniper line, a Vrell spore) laid over each level's
      brightest and darkest window at 1x. a: Q10 a, a grade per level (recommended); b: Q10 b, one storm-dusk grade
      for the whole act.
  design/world/jovian/concept/jovian-banks-r72-{a,b}.png
      Q2's foreground-bank readability frame over Level 36's brightest cream band: Talons, a Chimera-sized body and
      a harvester under a bank at 35 % with their running lights and tells at full, the bullets, the ship, Rook and a
      pickup above it; the measured share of the field under banks and the ship's band rule. a "cream veil": the
      bank in the band's own lit cream (bodies fade toward cream); b "shadowed underside": the bank's underside in a
      cool grey-ochre shade (bodies sink into shadow as dark shapes).
  design/world/jovian/concept/falling-city-r72-{a,b}.png
      Q4's falling-city storyboard (Level 37's last 40 s): the balloons tear and collapse, the deck tilts and drops,
      over 20 s the ground layer slows, darkens and shrinks toward the centre while the ship flies down beside the
      spires through low-air debris and Vrell; the level ends over open cloud. a "the slow tilt": the city goes down
      in one piece; b "breaking up": the deck breaks along a fault line, the two halves sink at their own rates and
      the spires topple.
  design/campaign/act-6-jovian-storm/concept/act-6-still-r72-{a,b}.png  and  act-6-still-r72-sheet.png
      The title card's still, 960x540, and the title card over each (darkened to 55 %, the letterbox, the chrome
      `ACT VI / JOVIAN STORM`). a "the dive": Jupiter's sunlit bands and the Great Red Spot under the CDF carrier group
      diving from orbit, Callisto a dark disc beside the limb; b "Aurelia's gold": the cloud city at golden hour on
      the storm bands, its balloons and anti-grav ring, a gold Talon pair on patrol, Callisto a dark disc above.
  design/story/characters/concept/act6-speakers-r72-{a,b}.png
      The Act 6 radio portraits at 72 px (style B, tools/art/portraits.py's pipeline, neutral only as the minor
      speakers): Harker in the guard's gloss black and gold flight suit (his final L29 face, the crest on the collar),
      Vesper (a black helmet with white ribbon stripes, the gold visor pushed up), Kestrel (black and gold chevrons
      on the helmet, a red band) on the amber enemy channel, and Aurelia Traffic (white-and-gold civil uniform, a
      headset) on the teal friendly channel. a: the plain channel screens, as the cast's; b: their rooms (the aces'
      cockpit canopy struts with the guard's gold crest; Aurelia's tower windows on the gold domes).

Round 73 (step m10-f72, round 72 A.11: a hull breaks along a jagged fracture, never a clean cut):
  design/enemies/bosses/ascendant/concept/ascendant-phases-r73-a.png   (subject ``phases73``)
      The phase storyboard of hull a with frame 8's wreck split along a seeded ragged fracture down the keel
      (ragged_line), torn plate flaps bent outward on both halves, a charred, hot-glowing rim, shards in the gap and
      gas venting from both edges (torn_half, wreck_break).
  design/world/jovian/concept/falling-city-r73-b.png   (subject ``city73``)
      The falling city b with the deck breaking along a ragged version of FAULT: torn strut and deck edges, shards
      flung into the widening gap, smoke and fire venting from both edges (city_break, break_effects).
  The r72 files are left as they were: ``ascendant`` and ``city`` still draw the round-72 clean cut.

Every glow, haze, cloud and shadow here is a smooth float gradient drawn once (no ordered dither, no posterising of
the haze; PNGs are 24-bit), the 2026-10-30 dither audit's rule. Sprites keep their small palettes. Production sprites
(the ship, Rook, bullets) are read from assets/sprites; nothing is written to assets/. Models live in this file on the
frozen ``render`` package; the Talon's helpers and the elite rim come from enemies_r60.py, the Aurelia parts from
render/scene_models.py, the portrait pipeline from tools/art/portraits.py and belt_r60.py's Harker, the chrome title
from tools/art/act_titles.py (all imported unchanged).
Run: python3 tools/concept/act6_r72.py [guard] [ascendant] [moods] [banks] [city] [still] [speakers] [phases73]
     [city73] [a|b]   (no subject = everything; ~10 minutes on 16 cores; round 73: ``phases73 a``, ``city73 b``)
"""
import multiprocessing
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "art"))
import enemies_r60 as u60  # noqa: E402  (belt_mats, belt_rim, gold_share, Turning)
from render import raster, sdf, sprite  # noqa: E402
from render import scene_models as sm  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import A_FACET, A_GLASS, A_GOLD, A_GUN, A_HULL, A_RED, A_ENGINE, hx  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_cylinder_y, sd_cylinder_z,  # noqa: E402
                        sd_ellipsoid, sd_fin, sd_plate, sd_sphere, union)

FW, FH, TAU = 480, 540, 2 * np.pi
SUB = "CONCEPT ROUND 72 - JOVIAN STORM"
DESIGN = ROOT / "design"
SPR = ROOT / "assets" / "sprites"
WORKERS = min(16, multiprocessing.cpu_count() or 4)
DOWN, UP = np.pi / 2, -np.pi / 2
YY, XX = np.mgrid[0:FH, 0:FW].astype(np.float64)
A_X = 7

GOLD = (255, 196, 64)
TEAL = (0, 255, 154)
LIME = (190, 255, 60)
RED = (255, 48, 40)
MAGENTA = (255, 60, 220)


# =========================================================================== common helpers

def save_png(img, path):
    path.parent.mkdir(parents=True, exist_ok=True)
    img.convert("RGB").save(path, optimize=True)
    print(f"wrote {path.relative_to(ROOT)}", flush=True)


def pool():
    return ProcessPoolExecutor(WORKERS, mp_context=multiprocessing.get_context("fork"))


def to_img(rgb, alpha=None):
    return raster.to_rgba_image(np.clip(rgb, 0, 255), alpha)


def arr(img):
    return np.array(img.convert("RGBA")).astype(np.float64)


def lum(img):
    a = arr(img)
    return a[..., 0] * 0.3 + a[..., 1] * 0.59 + a[..., 2] * 0.11


def window(img, w=200, h=96, darkest=True):
    """The darkest (or brightest) w x h window of a backdrop: its box and mean luminance."""
    g = lum(img)
    ii = np.pad(g, ((1, 0), (1, 0))).cumsum(0).cumsum(1)
    s = ii[h:, w:] - ii[:-h, w:] - ii[h:, :-w] + ii[:-h, :-w]
    y, x = np.unravel_index(np.argmin(s) if darkest else np.argmax(s), s.shape)
    return (int(x), int(y), int(x) + w, int(y) + h), float(s[y, x] / (w * h))


def glow_at(rgb, x, y, r, col, k=1.0):
    """Additive smooth light on a float canvas (in place on its crop)."""
    h, w = rgb.shape[:2]
    x0, x1 = max(0, int(x - 3 * r)), min(w, int(x + 3 * r) + 1)
    y0, y1 = max(0, int(y - 3 * r)), min(h, int(y + 3 * r) + 1)
    if x0 >= x1 or y0 >= y1:
        return rgb
    yy, xx = np.mgrid[y0:y1, x0:x1]
    f = np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k
    rgb[y0:y1, x0:x1] += f[..., None] * np.asarray(col, np.float64)
    return rgb


def light(img, x, y, r, col, k=1.0):
    """glow_at on an RGBA image (returns it)."""
    a = arr(img)
    glow_at(a[..., :3], x, y, r, col, k)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def over(rgb, img, x, y, alpha=1.0):
    """Composite an RGBA image with its top-left at (x, y) over a float canvas."""
    a = arr(img)
    h, w = a.shape[:2]
    H, W = rgb.shape[:2]
    x, y = int(round(x)), int(round(y))
    xs, ys, xe, ye = max(0, -x), max(0, -y), min(w, W - x), min(h, H - y)
    if xe <= xs or ye <= ys:
        return rgb
    al = a[ys:ye, xs:xe, 3:4] / 255 * alpha
    t = rgb[y + ys:y + ye, x + xs:x + xe]
    rgb[y + ys:y + ye, x + xs:x + xe] = t * (1 - al) + a[ys:ye, xs:xe, :3] * al
    return rgb


def put(img, sp, x, y, alpha=1.0):
    """Paste a sprite centred at (x, y) onto an RGBA image (in place)."""
    if alpha < 1.0:
        sp = sp.copy()
        sp.putalpha(sp.getchannel("A").point(lambda v: int(v * alpha)))
    sprite.paste_center(img, sp, x, y)
    return img


def fade(sp, alpha):
    s = sp.copy()
    s.putalpha(s.getchannel("A").point(lambda v: int(v * alpha)))
    return s


def tinted(sp, col, k):
    a = arr(sp)
    a[..., :3] = a[..., :3] * (1 - k) + np.asarray(col, np.float64) * k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def darkened(sp, k):
    a = arr(sp)
    a[..., :3] *= k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def text(img, x, y, s, col=raster.LABEL, scale=1):
    raster.draw_text(img, int(x), int(y), s, col, scale=scale)


def caption(img, s, y=8, col=(255, 210, 120)):
    lines = wrap(s, (img.width - 30) // 6)
    w = max(raster.text_width(t) for t in lines) + 12
    d = ImageDraw.Draw(img)
    d.rectangle([8, y, 8 + w, y + 4 + 10 * len(lines)], fill=(10, 14, 22, 210), outline=(80, 110, 140, 255))
    for i, t in enumerate(lines):
        text(img, 14, y + 4 + 10 * i, t, col)


def wrap(s, n=118):
    out, line = [], ""
    for word in s.split():
        if len(line) + len(word) + 1 > n:
            out.append(line)
            line = "  " + word
        else:
            line = f"{line} {word}" if line else word
    return out + ([line] if line else [])


def panel_grid(cells, cols, pad=8, labels=None, bg=raster.SHEET_BG):
    rows = [cells[i:i + cols] for i in range(0, len(cells), cols)]
    cw = max(c.width for c in cells)
    rh = [max(c.height for c in r) + (12 if labels else 0) for r in rows]
    img = Image.new("RGBA", (cols * (cw + pad) - pad, sum(rh) + pad * (len(rows) - 1)), bg)
    y = 0
    for ri, r in enumerate(rows):
        for ci, c in enumerate(r):
            x = ci * (cw + pad)
            img.alpha_composite(c.convert("RGBA"), (x, y))
            if labels:
                text(img, x, y + c.height + 2, labels[ri * cols + ci], raster.LABEL_DIM)
        y += rh[ri] + pad
    return img


def compose(title, sections, notes, width=1000, sub=None):
    """A review sheet: sections stacked (label, image), then the notes (wrapped)."""
    lines = []
    for n in notes:
        lines += wrap(n, (width - 32) // 6)
    h = 40 + sum(im.height + 22 for _, im in sections) + 14 * len(lines) + 16
    img = raster.sheet(width, h, title, sub or SUB)
    y = 36
    for lab, im in sections:
        text(img, 16, y, lab, raster.LABEL_DIM)
        img.alpha_composite(im.convert("RGBA"), (16, y + 12))
        y += im.height + 22
    for i, n in enumerate(lines):
        text(img, 16, y + i * 14, n, raster.ACCENT if i == 0 else raster.LABEL)
    return img


def checker(w, h, cell=8):
    c = ((np.arange(h)[:, None] // cell + np.arange(w)[None, :] // cell) % 2).astype(np.float64)
    v = 34 + 12 * c
    return to_img(np.dstack([v, v, v + 6]))


def frames(name):
    files = sorted(SPR.glob(f"{name}_*.png"), key=lambda p: int(p.stem.rsplit("_", 1)[1]))
    return [Image.open(p).convert("RGBA") for p in files] or [Image.open(SPR / f"{name}.png").convert("RGBA")]


class Prod:
    """Production sprites read once: the player's ship, Rook, the enemy bullets."""
    _c = {}

    @classmethod
    def get(cls, name, i=0):
        if name not in cls._c:
            cls._c[name] = frames(name)
        fs = cls._c[name]
        return fs[i % len(fs)]


def ship(img, x, y, bank=0):
    return put(img, Prod.get("ship", 2 + bank), x, y)


def rook(img, x, y, bank=0):
    return put(img, Prod.get("rook", 2 + bank), x, y)


def needle(img, x, y, ang):
    """The gold needle (16 headings, 0 = up, clockwise)."""
    k = int(round(((ang + np.pi / 2) % TAU) / TAU * 16)) % 16
    return put(img, Prod.get("needle", k), x, y)


def orb(img, x, y, big=False):
    return put(img, Prod.get("orb-medium" if big else "orb"), x, y)


def spore(img, x, y, r=5):
    """A Vrell spore: the larger, brighter Vrell bullet that reads over teal (a dark rim, lime ring, white core)."""
    d = ImageDraw.Draw(img)
    d.ellipse([x - r - 2, y - r - 2, x + r + 2, y + r + 2], fill=(10, 24, 18, 255))
    d.ellipse([x - r, y - r, x + r, y + r], fill=(170, 255, 70, 255))
    d.ellipse([x - r + 2, y - r + 2, x + r - 2, y + r - 2], fill=(245, 255, 220, 255))
    return img


def line_glow(img, pts, col, width, k=1.0, core=None):
    """A smooth glowing line (a telegraph, a beam, a gold feed): drawn once on its own layer."""
    lay = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    d.line(pts, fill=tuple(col) + (int(255 * k),), width=width)
    if core:
        d.line(pts, fill=tuple(core) + (int(255 * k),), width=max(1, width // 3))
    img.alpha_composite(raster.glow(lay, max(1.5, width * 0.8), 0.9))
    return img


def sniper_line(img, x0, y0, x1, y1, k):
    """Harker's / the lance's telegraph: a thin red line brightening (k 0..1)."""
    return line_glow(img, [x0, y0, x1, y1], (255, 40, 30), 1 if k < 0.6 else 2, 0.35 + 0.65 * k,
                     core=(255, 200, 180) if k > 0.6 else None)


def chevron_tell(img, x, y, side, name, k=1.0):
    """An ace's entry tell: a gold chevron with its call sign at the edge it enters by, its engines' glint."""
    d = ImageDraw.Draw(img)
    s = 9
    if side == "top":
        pts, tx, ty = [(x - s, y - 4), (x, y + 6), (x + s, y - 4), (x, y + 1)], x - raster.text_width(name) / 2, y + 10
    elif side == "bottom":
        pts, tx, ty = [(x - s, y + 4), (x, y - 6), (x + s, y + 4), (x, y - 1)], x - raster.text_width(name) / 2, y - 18
    elif side == "left":
        pts, tx, ty = [(x - 4, y - s), (x + 6, y), (x - 4, y + s), (x + 1, y)], x + 12, y - 3
    else:
        pts, tx, ty = [(x + 4, y - s), (x - 6, y), (x + 4, y + s), (x - 1, y)], x - 12 - raster.text_width(name), y - 3
    a = int(255 * k)
    d.polygon(pts, fill=(255, 200, 70, a), outline=(40, 24, 0, a))
    text(img, tx, ty, name, (255, 214, 120))
    gx = {"top": x, "bottom": x, "left": 0, "right": FW - 1}[side]
    gy = {"top": 0, "bottom": FH - 1, "left": y, "right": y}[side]
    return light(img, gx, gy, 10, (255, 200, 140), 0.8 * k)


def boss_bar(img, name, segs, tags=None, y=6):
    """The boss bar on the field: ``segs`` = [(fill 0..1, lit)] (hatched while unlit)."""
    d = ImageDraw.Draw(img)
    x0, x1 = 90, FW - 90
    d.rectangle([x0 - 2, y, x1 + 2, y + 13], fill=(8, 8, 12, 230), outline=(140, 110, 50, 255))
    n = len(segs)
    w = (x1 - x0 - 4 * (n - 1)) / n
    for i, (f, lit) in enumerate(segs):
        sx = x0 + i * (w + 4)
        if lit:
            d.rectangle([sx, y + 3, sx + w * f, y + 10], fill=(255, 190, 60, 255))
        else:
            for hx_ in range(int(sx), int(sx + w), 4):
                d.line([hx_, y + 10, hx_ + 4, y + 3], fill=(120, 96, 50, 255))
        if tags:
            text(img, sx + 2, y + 16, tags[i], (255, 214, 120) if lit else (120, 100, 70))
    nw = raster.text_width(name)
    d.rectangle([x0 - nw - 10, y, x0 - 4, y + 13], fill=(8, 8, 12, 230))
    text(img, x0 - nw - 6, y + 4, name, (255, 214, 120))
    return img


def warn_banner(img, s, k=1.0):
    d = ImageDraw.Draw(img)
    y = 200
    d.rectangle([0, y, FW, y + 26], fill=(60, 8, 6, int(200 * k)))
    for x in range(-30, FW, 22):
        d.polygon([(x, y), (x + 10, y), (x + 20, y + 26), (x + 10, y + 26)], fill=(200, 40, 20, int(160 * k)))
    w = raster.text_width(s, 2)
    raster.draw_text(img, (FW - w) // 2, y + 7, s, (255, 230, 200), scale=2)
    return img


def field_cell(img, label=None, scale=0.5):
    out = img.resize((int(img.width * scale), int(img.height * scale)), Image.LANCZOS)
    if label:
        text(out, 6, out.height - 12, label, (255, 230, 180))
    return out


# =========================================================================== backdrops (the moods per level)

def stars(rgb, seed, n=260, k=1.0):
    rng = np.random.default_rng(seed)
    H, W = rgb.shape[:2]
    for _ in range(n):
        x, y = rng.integers(0, W), rng.integers(0, H)
        b = rng.uniform(40, 200) * k
        rgb[y, x] += (b, b * 0.97, b * 0.92)
    return rgb


def gaussian_filter(a, sigma):
    """A separable Gaussian blur of a 2D float array (edges extended), in float: smooth, no 8-bit steps."""
    if sigma <= 0:
        return a
    r = int(np.ceil(3 * sigma))
    k = np.exp(-(np.arange(-r, r + 1) ** 2) / (2 * sigma * sigma))
    k /= k.sum()
    p = np.pad(a, r, mode="edge")
    p = sum(k[i] * p[:, i:i + a.shape[1]] for i in range(2 * r + 1))
    return sum(k[i] * p[i:i + a.shape[0], :] for i in range(2 * r + 1))


def smoothstep(a, b, x):
    t = np.clip((x - a) / (b - a), 0, 1)
    return t * t * (3 - 2 * t)


def bands(h, w, seed, scale=38.0, warp_k=2.6, streak_k=0.4):
    """Jupiter's storm bands as a 0..1 field: wavy horizontal bands, warped, with streaks."""
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    warp = raster.fbm(w, h, 96, seed, octaves=3, period=False)
    streak = raster.fbm(max(8, w // 6), h, 8, seed + 1, octaves=3, period=False)
    streak = np.array(Image.fromarray((streak * 255).astype(np.uint8)).resize((w, h), Image.BICUBIC),
                      np.float64) / 255
    v = 0.5 + 0.5 * np.sin(yy / scale + warp * warp_k + 0.35 * np.sin(xx / 90 + seed))
    return np.clip((1 - streak_k) * v + streak_k * streak, 0, 1)


def billows(h, w, seed, n, y_range=(0, 1), size=(14, 34), spread=(46, 30)):
    """Towering cloud heads as a smooth height field -> (shade 0..1, alpha 0..1), lit from the top-left with soft
    self-shadowing; no posterising."""
    rng = np.random.default_rng(seed)
    hgt = np.zeros((h, w))
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    for _ in range(n):
        cx, cy = rng.uniform(-40, w + 40), rng.uniform(h * y_range[0], h * y_range[1])
        for _ in range(16):
            bx, by = cx + rng.normal(0, spread[0]), cy + rng.normal(0, spread[1])
            rr = rng.uniform(*size)
            x0, x1 = int(max(0, bx - 3 * rr)), int(min(w, bx + 3 * rr))
            y0, y1 = int(max(0, by - 3 * rr)), int(min(h, by + 3 * rr))
            if x0 >= x1 or y0 >= y1:
                continue
            d2 = ((xx[y0:y1, x0:x1] - bx) ** 2 + (yy[y0:y1, x0:x1] - by) ** 2) / rr ** 2
            hgt[y0:y1, x0:x1] = np.maximum(hgt[y0:y1, x0:x1], rr * np.sqrt(np.clip(1 - d2 / 2.2, 0, 1)))
    hgt = gaussian_filter(hgt, 2.0)
    alpha = smoothstep(3, 9, hgt)
    gy, gx = np.gradient(hgt)
    shade = np.clip(0.55 - (gx + gy) * 0.2 + hgt / 70, 0, 1)
    sh = np.roll(np.roll(hgt, 7, 0), 7, 1)
    shade *= 1 - 0.35 * smoothstep(0, 10, sh - hgt)
    return gaussian_filter(shade, 0.7), alpha


JOV_SUN = [(0.0, (110, 50, 28)), (0.25, (168, 92, 50)), (0.5, (206, 146, 86)), (0.75, (236, 204, 150)),
           (1.0, (252, 240, 214))]


def sky_l36(h=FH, w=FW, seed=3601):
    """L36: high sunlit bands, cream, ochre and rust under a pale haze, the sun top-left; towering banks."""
    v = bands(h, w, seed)
    rgb = raster.ramp(JOV_SUN, 0.18 + 0.72 * v)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rgb = rgb * 0.82 + np.array((236, 222, 200)) * 0.18                       # the pale haze
    shade, a = billows(h, w, seed + 5, 6, size=(16, 36))
    tones = [(0.0, (120, 70, 44)), (0.4, (190, 140, 96)), (0.75, (236, 212, 170)), (1.0, (255, 246, 226))]
    rgb = rgb * (1 - a[..., None]) + raster.ramp(tones, shade) * a[..., None]
    d = np.hypot(xx + 60, yy + 80)
    rgb += (np.exp(-(d / 300) ** 2) * 0.32)[..., None] * np.array((255, 236, 200))
    return rgb


def city_sprites():
    if "deck" not in _CITY:
        _CITY["deck"] = sm.render_px_model(sm.aurelia_deck(78), factor=3, colors=48)
        _CITY["deck_s"] = sm.render_px_model(sm.aurelia_deck(56), factor=3, colors=48)
        _CITY["pad"] = sm.render_px_model(sm.aurelia_deck(22), factor=4, colors=32)
        _CITY["spire"] = sm.render_px_model(sm.aurelia_spire(18), factor=4)
        _CITY["balloon"] = sm.render_px_model(sm.aurelia_balloon(), factor=4)
    return _CITY


_CITY = {}
CITY_LAYOUT = [("deck", 300, 170), ("deck_s", 130, 400), ("pad", 420, 340), ("spire", 420, 340), ("pad", 70, 220),
               ("spire", 70, 220), ("deck_s", 360, 470)]
WALKS = [((300, 170), (130, 400)), ((130, 400), (360, 470)), ((300, 170), (420, 340)), ((70, 220), (130, 400))]
BALLOONS = [(200, 80), (440, 120), (40, 470), (250, 300)]


def city_layer(w=FW, h=FH, layout=CITY_LAYOUT, walks=WALKS, balloons=BALLOONS, pale=0.12):
    """Aurelia's decks, spires, walkways and lift balloons on a transparent layer (the decks a step paler than the
    units' gold: the Q10 note)."""
    c = city_sprites()
    lay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    for (x0, y0), (x1, y1) in walks:
        for wd, col in ((14, (40, 26, 20)), (10, (224, 206, 170)), (2, (230, 170, 40))):
            d.line([x0, y0, x1, y1], fill=col + (255,), width=wd)
    for name, x, y in layout:
        sprite.paste_center(lay, c[name], x, y)
    for x, y in balloons:
        d.line([x, y + 40, x + 14, y + 80], fill=(60, 40, 30, 200), width=1)
        sprite.paste_center(lay, c["balloon"], x, y)
    a = arr(lay)
    a[..., :3] = a[..., :3] * (1 - pale) + np.array((236, 226, 206)) * pale
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def shadow_rgb(rgb, layer, dx, dy, k=0.45, blur=4.0):
    al = arr(layer)[..., 3] / 255
    al = gaussian_filter(np.roll(np.roll(al, dy, 0), dx, 1), blur)
    return rgb * (1 - k * al[..., None])


def sky_l37(after=False, seed=3701):
    v = bands(FH, FW, seed, scale=46, warp_k=2.0)
    rgb = raster.ramp(JOV_SUN, 0.35 + 0.55 * v) * 0.86 + np.array((240, 220, 190)) * 0.14
    city = city_layer()
    if after:
        rgb = rgb * np.array((0.62, 0.52, 0.5)) + np.array((30, 10, 16))       # the sky darkens a step
    rgb = shadow_rgb(rgb, city, 26, 34, 0.42)
    rgb = over(rgb, city, 0, 0)
    if after:
        rng = np.random.default_rng(seed + 9)
        growth = raster.fbm(FW, FH, 40, seed + 3, octaves=4, period=False)
        cm = arr(city)[..., 3] / 255
        g = smoothstep(0.55, 0.72, growth) * gaussian_filter(cm, 3)
        seam = np.exp(-((growth - 0.62) / 0.012) ** 2) * gaussian_filter(cm, 3)
        rgb = rgb * (1 - g[..., None]) + np.array((22, 70, 66)) * g[..., None]
        rgb += seam[..., None] * np.array((0, 220, 140)) * 0.7
        smoke = gaussian_filter(raster.fbm(FW, FH, 60, seed + 4, octaves=4, period=False), 2)
        sm_ = smoothstep(0.5, 0.85, smoke)
        rgb = rgb * (1 - 0.55 * sm_[..., None]) + np.array((40, 32, 34)) * 0.55 * sm_[..., None]
        for _ in range(9):
            x, y = rng.uniform(40, 440), rng.uniform(60, 500)
            if cm[int(y), int(x)] > 0.5:
                glow_at(rgb, x, y, rng.uniform(10, 22), (255, 120, 30), 0.9)
                glow_at(rgb, x, y, 5, (255, 220, 140), 0.8)
    return rgb


STORM = [(0.0, (16, 18, 28)), (0.3, (40, 44, 62)), (0.6, (72, 76, 98)), (0.85, (104, 106, 130)),
         (1.0, (140, 140, 162))]


def sky_l38(seed=3801):
    v = bands(FH, FW, seed, scale=30, warp_k=3.2, streak_k=0.5)
    rgb = raster.ramp(STORM, 0.1 + 0.6 * v)
    shade, a = billows(FH, FW, seed + 5, 7, size=(18, 40), spread=(60, 36))
    tones = [(0.0, (14, 14, 24)), (0.5, (44, 42, 60)), (0.8, (78, 76, 98)), (1.0, (112, 110, 132))]
    rgb = rgb * (1 - a[..., None]) + raster.ramp(tones, shade) * a[..., None]
    d = np.hypot(XX - FW - 60, YY - FH - 40)
    rgb += (np.exp(-(d / 260) ** 2) * 0.35)[..., None] * np.array((200, 70, 40))   # the Red Spot's rim, far off
    return rgb


def lightning_column(img, x, k=1.0):
    """J3's telegraph: a 40 px column, faint edges, a thin blue-white line down its middle brightening."""
    a = arr(img)
    col = np.exp(-((XX[:img.height, :img.width] - x) / 20) ** 8) * 0.18 * k
    a[..., :3] += col[..., None] * np.array((170, 200, 255))
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    return line_glow(img, [x, 0, x, img.height], (190, 215, 255), 1, 0.4 + 0.6 * k)


IO = [(0.0, (52, 46, 30)), (0.4, (126, 112, 70)), (0.7, (168, 152, 102)), (1.0, (206, 196, 150))]


def ground_l39(seed=3901):
    n = raster.fbm(FW, FH, 70, seed, octaves=5, period=False)
    rgb = raster.ramp(IO, 0.25 + 0.6 * n)
    lava = raster.fbm(FW, FH, 90, seed + 1, octaves=4, period=False)
    flow = smoothstep(0.7, 0.76, lava) + 0.8 * smoothstep(0.012, 0.0, np.abs(lava - 0.46)) * smoothstep(0.5, 0.6, n)
    flow = gaussian_filter(np.clip(flow, 0, 1), 1.0)
    rgb = rgb * (1 - flow[..., None]) + np.array((18, 14, 14)) * flow[..., None]
    frost = smoothstep(0.7, 0.8, raster.fbm(FW, FH, 50, seed + 2, octaves=4, period=False)) * (1 - flow)
    rgb = rgb * (1 - 0.6 * frost[..., None]) + np.array((236, 236, 228)) * 0.6 * frost[..., None]
    gy, gx = np.gradient(gaussian_filter(n * 30 - flow * 8, 2))
    rgb *= np.clip(1.0 - (gx + gy) * 0.18, 0.7, 1.25)[..., None]
    img = to_img(rgb)
    d = ImageDraw.Draw(img)
    for y in (150, 380):                                                       # the refinery lines
        d.rectangle([0, y - 9, FW, y + 9], fill=(40, 36, 34, 255))
        d.rectangle([0, y - 6, FW, y - 3], fill=(96, 88, 80, 255))
        d.rectangle([0, y + 3, FW, y + 6], fill=(96, 88, 80, 255))
        for x in range(20, FW, 60):
            d.rectangle([x - 12, y - 16, x + 12, y + 16], fill=(58, 52, 48, 255), outline=(20, 18, 16, 255))
    rgb = arr(img)[..., :3]
    rgb = shadow_rgb(rgb, img.crop((0, 0, FW, FH)), 0, 0, 0.0)
    for x in range(20, FW, 60):
        for y in (150, 380):
            glow_at(rgb, x, y, 3, (255, 190, 70), 1.0)
    for x, y in ((90, 270), (360, 250), (250, 470)):                           # vent glows
        glow_at(rgb, x, y, 18, (255, 130, 40), 0.8)
        glow_at(rgb, x, y, 6, (255, 230, 170), 0.9)
    return rgb


def space_l40(seed=4001, callisto=(230, 760, 420), jupiter=(-560, 160, 640)):
    rgb = np.zeros((FH, FW, 3)) + np.array((3, 4, 9.0))
    stars(rgb, seed)
    cx, cy, r = callisto
    d = np.hypot(XX - cx, YY - cy)
    disc = smoothstep(r + 1.5, r - 1.5, d)
    tex = raster.fbm(FW, FH, 30, seed + 1, octaves=5, period=False)
    lit = np.clip(((XX - cx) * -0.5 + (YY - cy) * -0.8) / r * 0.9 + 0.25, 0, 1)
    ice = raster.ramp([(0.0, (14, 18, 26)), (0.5, (52, 60, 74)), (1.0, (110, 120, 136))], lit * (0.6 + 0.4 * tex))
    rgb = rgb * (1 - disc[..., None]) + ice * disc[..., None]
    rgb += (np.exp(-((d - r) / 6) ** 2) * 0.25 * lit)[..., None] * np.array((150, 170, 200))
    jx, jy, jr = jupiter
    dj = np.hypot(XX - jx, YY - jy)
    jd = smoothstep(jr + 2, jr - 2, dj)
    jb = bands(FH, FW, seed + 2, scale=14, warp_k=1.6)
    ang = np.arctan2(YY - jy, XX - jx)
    jl = np.clip(np.cos(ang - np.arctan2(-0.6, 0.8)) * 0.9 + 0.1, 0, 1)
    jc = raster.ramp(JOV_SUN, 0.2 + 0.6 * jb) * (0.25 + 0.75 * jl)[..., None]
    rgb = rgb * (1 - jd[..., None]) + jc * jd[..., None]
    rgb += (np.exp(-((dj - jr) / 10) ** 2) * 0.4)[..., None] * np.array((230, 190, 140))
    rng = np.random.default_rng(seed + 3)
    for _ in range(14):                                                        # the fleet's lights
        x, y = rng.uniform(140, 470), rng.uniform(20, 300)
        glow_at(rgb, x, y, 1.6, (170, 230, 255), 1.4)
    return rgb


def ground_l41(seed=4101):
    n = raster.fbm(FW, FH, 60, seed, octaves=5, period=False)
    rgb = raster.ramp([(0.0, (26, 32, 42)), (0.5, (60, 70, 86)), (1.0, (106, 116, 132))], 0.2 + 0.6 * n)
    cracks = smoothstep(0.02, 0.0, np.abs(raster.fbm(FW, FH, 45, seed + 1, octaves=3, period=False) - 0.5))
    rgb *= (1 - 0.45 * cracks)[..., None]
    lay = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    d = ImageDraw.Draw(lay)
    blocks = [(40, 60, 170, 200), (300, 30, 450, 150), (210, 260, 300, 420), (40, 330, 150, 520), (340, 300, 460, 500)]
    d.rectangle([150, 230, 360, 250], fill=(20, 20, 24, 255))                 # a causeway
    d.rectangle([245, 0, 265, FH], fill=(20, 20, 24, 255))
    for b in blocks:
        d.rectangle(b, fill=(14, 13, 16, 255), outline=(150, 112, 40, 255))
        d.rectangle([b[0] + 6, b[1] + 6, b[2] - 6, b[3] - 6], outline=(40, 36, 42, 255))
    rgb = shadow_rgb(rgb, lay, 22, 30, 0.6, 3.0)                              # the low Sun: long shadows
    rgb = over(rgb, lay, 0, 0)
    for b in blocks:
        for x in np.linspace(b[0] + 10, b[2] - 10, 4):
            glow_at(rgb, x, b[1] + 3, 2.2, (255, 60, 40), 1.2)
        glow_at(rgb, b[2] - 4, b[3] - 4, 2.5, (255, 200, 80), 1.0)
    return rgb


def cruiser_shape(img, x, y, w, h, k=1.0):
    """A home-fleet cruiser far off: a gloss-black wedge, gold edge, red lights (the L28 cruisers)."""
    d = ImageDraw.Draw(img)
    pts = [(x, y - h / 2), (x + w / 2, y + h / 2 - h * 0.12), (x, y + h / 2), (x - w / 2, y + h / 2 - h * 0.12)]
    d.polygon(pts, fill=(int(18 * k), int(16 * k), int(22 * k), 255), outline=(int(90 * k), int(70 * k),
                                                                              int(34 * k), 255))
    return img


def space_l42(seed=4201):
    rgb = space_l40(seed, callisto=(240, 900, 520), jupiter=(620, -380, 520))
    img = to_img(rgb)
    for x, y, w, h in ((70, 120, 40, 90), (400, 330, 34, 74), (150, 420, 26, 60)):
        cruiser_shape(img, x, y, w, h, 0.8)
    rgb = arr(img)[..., :3]
    rng = np.random.default_rng(seed + 5)
    for x, y, w, h in ((70, 120, 40, 90), (400, 330, 34, 74), (150, 420, 26, 60)):
        for j in range(3):
            glow_at(rgb, x + rng.uniform(-w / 4, w / 4), y + rng.uniform(-h / 4, h / 4), 1.1, (255, 60, 40), 0.9)
    for _ in range(10):                                                        # the CDF fleet's lights
        glow_at(rgb, rng.uniform(250, 470), rng.uniform(80, 260), 1.6, (160, 235, 255), 1.4)
    return rgb


def grade_dusk(rgb):
    """Q10 b: one storm-dusk grade for the whole act (darker, violet shadows, amber highs)."""
    a = np.clip(rgb / 255.0, 0, 1)
    l = a @ np.array([0.3, 0.59, 0.11])
    a = a ** 1.3 * 0.78
    a = a * (1 - 0.35 * (1 - l))[..., None] + (0.35 * (1 - l))[..., None] * np.array((0.16, 0.1, 0.2))
    a = a * np.array((1.04, 0.92, 0.95))
    return np.clip(a * 255, 0, 255)


def mood_list():
    """(key, title, rgb) for the ladder: L36, L37 before and after the turn, L38 (a strike's telegraph), L39 .. L42."""
    out = [("L36", "HIGH SUNLIT BANDS", sky_l36()),
           ("L37", "AURELIA, BEFORE THE TURN", sky_l37(False)),
           ("L37+", "AURELIA AFTER THE TURN", sky_l37(True)),
           ("L38", "THE STORM LAYER", sky_l38()),
           ("L39", "IO'S REFINERIES", ground_l39()),
           ("L40", "THE CALLISTO APPROACH", space_l40()),
           ("L41", "CALLISTO HQ", ground_l41()),
           ("L42", "CALLISTO ORBIT", space_l42())]
    return out


# =========================================================================== 1 the Honour Guard

ACES = ("harker", "vesper", "kestrel")
ACE_NAME = {"harker": "HARKER", "vesper": "VESPER", "kestrel": "KESTREL"}
ACE_PX = 30.0                                  # px per model unit: 1.5x the Talon's 20 (the brief's 1.5x)
ACE_N = 76
ACE_EXT = ACE_N / ACE_PX
W_WHITE, W_RED, W_BARREL = A_X, A_X + 1, A_X + 2


def elite_mats(glow=1.0):
    """Q9 a's elite livery: the gloss black hull (the L28 cruisers'), heavy gold, red running lights; the marks' paint."""
    return u60.belt_mats("2a2a30", glow, red=1.4, extra=[
        Material((0.93, 0.93, 0.9), metal=0.1, shininess=90, spec=0.8),
        Material((0.72, 0.07, 0.05), metal=0.2, shininess=80, spec=0.7),
        Material((0.32, 0.33, 0.36), metal=0.85, shininess=120, spec=1.1)])


def ace_model(v, ace, glow=1.0):
    """The Talon's airframe (em.talon's plates, nose +Y) in the elite livery with the ace's mark (a) or its own
    silhouette (b)."""
    body = [(0.0, 1.02), (0.16, 0.45), (0.22, -0.2), (0.26, -0.72), (0.0, -0.62)]
    wing = [(0.12, 0.05), (0.92, 0.38), (0.98, 0.26), (0.70, -0.20), (0.14, -0.55)]
    tail = [(0.18, -0.45), (0.46, -0.78), (0.42, -0.90), (0.18, -0.72)]
    barrel = 1.12
    if v == "b" and ace == "harker":
        body = [(0.0, 1.2), (0.12, 0.62), (0.2, -0.2), (0.24, -0.72), (0.0, -0.62)]
        barrel = 1.26
    if v == "b" and ace == "vesper":
        wing = [(0.12, 0.08), (1.16, 0.0), (1.2, -0.1), (0.62, -0.36), (0.14, -0.55)]
    if v == "b" and ace == "kestrel":
        body = [(0.0, 0.84), (0.18, 0.36), (0.24, -0.2), (0.28, -0.7), (0.0, -0.6)]
        wing = [(0.12, 0.0), (0.86, 0.22), (0.9, 0.1), (0.66, -0.26), (0.14, -0.5)]
    lead_k = (wing[1][1] - wing[0][1]) / (wing[1][0] - wing[0][0])

    def scene(p):
        q = mirror_x(p)
        roof = lambda x, y: np.clip(1.0 - 3.4 * np.abs(x), 0.25, 1.0)
        items = [(sd_plate(q, body, 0.0, 0.14, 0.0, taper=roof), A_HULL),
                 (sd_plate(q, wing, -0.02, 0.05, 0.0, taper=lambda x, y: np.clip(1.2 - x, 0.25, 1)), A_FACET),
                 (sd_plate(q, tail, 0.03, 0.04, 0.0), A_FACET),
                 (sd_plate(p, [(0, 0.58), (0.07, 0.28), (0.0, 0.12), (-0.07, 0.28)], 0.12, 0.03, 0.0), A_GLASS),
                 (sd_cylinder_y(q, (0.12, -0.72, 0.0), 0.07, 0.03), A_ENGINE),
                 (sd_sphere(q, (wing[2][0] - 0.02, wing[2][1] + 0.02, -0.02), 0.035), A_RED)]
        if ace == "harker":
            items.append((sd_capsule(p, (0, 0.5, -0.1), (0, barrel, -0.1), 0.04, 0.03), W_BARREL))
        else:
            items.append((sd_capsule(q, (0.6, 0.08, -0.02), (0.6, 0.45, -0.02), 0.03, 0.02), A_GUN))
        if ace == "kestrel":
            fin = [(-0.36, 0.0), (-0.82, 0.0), (-0.92, 0.3 if v == "a" else 0.4), (-0.6, 0.3 if v == "a" else 0.4)]
            items.append((sd_fin(q, fin, 0.2, 0.025, 0.0), A_FACET))
            if v == "b":
                items.append((sd_plate(q, [(0.14, 0.62), (0.42, 0.46), (0.4, 0.38), (0.14, 0.42)], 0.02, 0.03),
                              A_FACET))
        if ace == "vesper" and v == "b":
            for s in (-1, 1):
                items.append((sd_capsule(p, (s * 0.7, -0.3, -0.02), (s * 0.82, -0.98, -0.02), 0.022, 0.01), W_WHITE))
        d, m = union(*items)
        x, y = q[:, 0], q[:, 1]
        yl = wing[0][1] + (x - wing[0][0]) * lead_k                    # the wing's leading edge
        fac = m == A_FACET
        m = np.where(fac & ((y > yl - 0.09) | (x > 0.58)), A_GOLD, m)   # gold leading edge and outer panels
        m = np.where((m == A_HULL) & (x < 0.05) & (y < 0.4) & (y > -0.6), A_GOLD, m)
        m = np.where((m == A_FACET) & (y < -0.62), A_GOLD, m)           # gold tail tips
        if ace == "harker":
            m = np.where((m == A_HULL) & (y > 0.62) & (y < 0.8), W_RED, m)
            m = np.where((m == A_GOLD) & (y > 0.62) & (y < 0.8), W_RED, m)
        if ace == "vesper":
            m = np.where(np.isin(m, (A_FACET, A_GOLD)) & (np.abs(y - (yl - 0.16)) < 0.035) & (x > 0.2), W_WHITE, m)
            m = np.where((m == A_FACET) & (y < -0.5) & (y > -0.62), W_WHITE, m)
        if ace == "kestrel":
            chev = ((y + 0.7 * x) % 0.34) < 0.13
            m = np.where((m == A_FACET) & chev, A_GOLD, m)
            m = np.where((m == A_GOLD) & ~chev & (x > 0.2) & (x < 0.62), A_FACET, m)
            m = np.where(np.isin(m, (A_HULL, A_FACET, A_GOLD)) & (y < -0.56) & (y > -0.7), W_RED, m)
        return d, m
    return scene, elite_mats(glow)


def _ace_job(job):
    v, ace, k = job
    t = u60.Turning(lambda: ace_model(v, ace), ACE_N, 16, factor=5, colors=40, post=u60.belt_rim(True),
                    extent=ACE_EXT)
    return job, t.frame(k)


ACE_SP = {}


def load_aces(variants):
    jobs = [(v, a, k) for v in variants for a in ACES for k in range(16) if (v, a, k) not in ACE_SP]
    if jobs:
        with pool() as ex:
            for job, sp in ex.map(_ace_job, jobs):
                ACE_SP[job] = sp


def ace(v, name, heading):
    k = int(round((heading % TAU) / (TAU / 16))) % 16
    return ACE_SP[(v, name, k)]


def ace_source(v, name, disp=230):
    scene, mats = ace_model(v, name)
    from render import enemy_rigs as rig
    r = rig.model_rotation(DOWN)
    hi = sdf.render(lambda p: scene(sdf.rotate_z(p, r)), rig.model_space_materials(mats, r), (disp, disp), 2.8)
    cell = checker(disp, disp)
    cell.alpha_composite(sprite.to_image(hi))
    return cell


def ace_tell(img, v, name, x, y, k=1.0):
    """The ace's tell drawn over its sprite (heading down): Harker's barrel glints red, Vesper's ribbons flare
    white, Kestrel's engines flare white and its tail band glows."""
    s = ACE_PX
    if name == "harker":
        return light(img, x, y + (1.26 if v == "b" else 1.12) * s, 5, (255, 60, 40), 1.4 * k)
    if name == "vesper":
        for sx in (-1, 1):
            img = light(img, x + sx * 0.75 * s, y - 0.05 * s, 9, (255, 255, 255), 0.6 * k)
        return img
    for sx in (-1, 1):
        img = light(img, x + sx * 0.12 * s, y - 0.78 * s, 6, (255, 240, 220), 1.3 * k)
    return img


def guard_backdrop():
    if "l40" not in _BG:
        _BG["l40"] = space_l40()
    return to_img(_BG["l40"])


_BG = {}


def explosion(img, x, y, size="medium", i=5):
    return put(img, u60.EXPL[size][min(11, i)], x, y)


def aimed_fan(img, x0, y0, tx, ty, n, spread, dist, kind="needle"):
    a0 = np.arctan2(ty - y0, tx - x0)
    for j in range(n):
        a = a0 + (j - (n - 1) / 2) * np.radians(spread) / max(1, n - 1)
        bx, by = x0 + np.cos(a) * dist, y0 + np.sin(a) * dist
        if kind == "needle":
            needle(img, bx, by, a)
        else:
            orb(img, bx, by)
    return img


def spiral_shots(img, x, y, arms, t, n=6, r0=18, dr=16):
    for a in range(arms):
        for j in range(n):
            ang = TAU * a / arms + t * np.radians(90) - j * 0.35
            orb(img, x + np.cos(ang) * (r0 + j * dr), y + np.sin(ang) * (r0 + j * dr))
    return img


def heading_to(x0, y0, x1, y1):
    return np.arctan2(y1 - y0, x1 - x0)


GUARD_PANELS = [
    ("0 S: THE STING, THE BAR'S THREE SEGMENTS HATCHED, HARKER'S TELL AT THE TOP"),
    ("3 S: HARKER ALONE - THE SNIPER LINE (1.0 S RED LINE, THEN THE SHOT)"),
    ("16 S: HARKER AT 2/3 - VESPER'S TELL ON THE FAR SIDE"),
    ("20 S: TWO ACES - VESPER'S SPIRAL LOOP, HARKER SLIDES (NO FIRE)"),
    ("31 S: KESTREL'S TELL FROM THE REAR (ITS OWN CHEVRON, NO EDGE WARNING)"),
    ("40 S: THREE ACES - KESTREL'S DIVE TOLD, HARKER'S LINE WAITS"),
    ("48 S: THE PINCER - FLANKERS CROSS, HARKER HOLDS FIRE"),
    ("55 S: VESPER DOWN - THE SURVIVORS SHARPEN (TRIPLE TAP, 85 PX)"),
]


def guard_panel(v, i):
    img = guard_backdrop()
    sx, sy = 240, 462
    segs = [(1.0, False), (1.0, False), (1.0, False)]
    tags = ["HARKER", "VESPER", "KESTREL"]
    if i == 0:
        chevron_tell(img, 240, 4, "top", "HARKER")
        ship(img, sx, sy)
        rook(img, sx + 52, sy + 14)
        caption(img, "MINI-BOSS STING  -  HARKER'S TAUNT (ENEMY CHANNEL)", y=40)
    elif i == 1:
        segs[0] = (0.9, True)
        hx_, hy = 200, 120
        put(img, ace(v, "harker", heading_to(hx_, hy, sx, sy)), hx_, hy)
        ace_tell(img, v, "harker", hx_, hy, 0.6)
        sniper_line(img, hx_ + 6, hy + 34, sx + (sx - hx_) * 0.6, FH, 0.8)
        ship(img, sx, sy)
        rook(img, sx + 52, sy + 14)
        caption(img, "SNIPER LINE: LOCKED 0.2 S BEFORE IT FIRES; A DOUBLE TAP RE-AIMS 0.6 S LATER", y=40)
    elif i == 2:
        segs[0] = (0.66, True)
        hx_, hy = 330, 110
        put(img, ace(v, "harker", heading_to(hx_, hy, sx - 30, sy)), hx_, hy)
        aimed_fan(img, hx_, hy + 30, sx - 30, sy, 2, 6, 90)
        aimed_fan(img, hx_, hy + 30, sx - 30, sy, 2, 6, 150)
        chevron_tell(img, 4, 240, "left", "VESPER")
        ship(img, sx - 30, sy)
        rook(img, sx + 22, sy + 14)
        caption(img, "VESPER JOINS FROM THE SIDE AWAY FROM HARKER (2/3 OF HIS HP OR 20 S)", y=40)
    elif i == 3:
        segs[0], segs[1] = (0.58, True), (1.0, True)
        put(img, ace(v, "harker", np.radians(160)), 150, 130)
        vx, vy = 300, 250
        put(img, ace(v, "vesper", np.radians(250)), vx, vy)
        ace_tell(img, v, "vesper", vx, vy, 0.5)
        spiral_shots(img, vx, vy, 2, 0.7)
        ship(img, 170, sy)
        rook(img, 222, sy + 14)
        caption(img, "SPIRAL LOOP (2 ARMS); HARKER'S SLIDE IS HIS BREATH WITH TWO OR MORE FLYING", y=40)
    elif i == 4:
        segs[0], segs[1] = (0.47, True), (0.85, True)
        hx_, hy = 260, 105
        put(img, ace(v, "harker", heading_to(hx_, hy, 300, sy)), hx_, hy)
        vx, vy = 140, 220
        put(img, ace(v, "vesper", np.radians(20)), vx, vy)
        aimed_fan(img, vx + 20, vy + 20, 300, sy, 5, 40, 70)
        chevron_tell(img, 120, FH - 4, "bottom", "KESTREL")
        ship(img, 300, sy - 20)
        rook(img, 352, sy - 6)
        caption(img, "KESTREL'S TELL AT THE BOTTOM EDGE, 2.5 S AHEAD; VESPER'S ROLL FAN", y=40)
    elif i == 5:
        segs = [(0.4, True), (0.7, True), (1.0, True)]
        hx_, hy = 330, 120
        put(img, darkened(ace(v, "harker", heading_to(hx_, hy, 260, 440)), 0.75), hx_, hy)
        d = ImageDraw.Draw(img)
        d.rectangle([hx_ - 24, hy - 50, hx_ + 24, hy - 38], fill=(10, 10, 14, 220), outline=(255, 60, 40, 255))
        text(img, hx_ - 20, hy - 47, "WAITS", (255, 120, 100))
        vx, vy = 140, 230
        put(img, ace(v, "vesper", np.radians(0)), vx, vy)
        for j in range(8):
            bx = 40 + j * 54 + (50 if j >= 3 else 0) + (50 if j >= 6 else 0)
            if bx < FW - 10:
                orb(img, bx, 250 + (j % 2) * 3)
        kx, ky = 120, 420
        put(img, ace(v, "kestrel", heading_to(kx, ky, 260, 440)), kx, ky)
        ace_tell(img, v, "kestrel", kx, ky, 1.0)
        for t in np.linspace(0.1, 0.9, 9):                       # the dive's line, to the pull-out at 110 px
            px_, py_ = kx + (260 - 110 - kx) * t, ky + (440 - ky) * t
            d.ellipse([px_ - 1, py_ - 1, px_ + 1, py_ + 1], fill=(255, 240, 220, 200))
        ship(img, 260, 440)
        rook(img, 312, 454)
        caption(img, "AT MOST ONE TELEGRAPHED LINE OR DIVE; AT MOST TWO ACES FIRING (VESPER'S RIBBON + KESTREL)",
                y=40)
    elif i == 6:
        segs = [(0.34, True), (0.55, True), (0.82, True)]
        hx_, hy = 240, 100
        put(img, darkened(ace(v, "harker", DOWN), 0.6), hx_, hy)
        ty = sy - 160
        put(img, ace(v, "vesper", 0.0), 90, ty)
        put(img, ace(v, "kestrel", np.pi), 390, ty + 10)
        ace_tell(img, v, "vesper", 90, ty, 0.6)
        aimed_fan(img, 110, ty + 10, 240, sy, 5, 40, 60)
        aimed_fan(img, 370, ty + 20, 240, sy, 5, 40, 40)
        ship(img, 240, sy)
        rook(img, 292, sy + 14)
        caption(img, "PINCER (AT MOST EVERY FOURTH DRAW, ALL THREE FLYING): HARKER'S CREST DIMS", y=40)
    else:
        segs = [(0.3, True), (0.0, True), (0.7, True)]
        tags[1] = "VESPER X"
        explosion(img, 300, 240, "large", 4)
        hx_, hy = 170, 110
        put(img, ace(v, "harker", heading_to(hx_, hy, 250, sy)), hx_, hy)
        sniper_line(img, hx_ + 6, hy + 34, 250 + (250 - hx_) * 0.55, FH, 0.5)
        kx, ky = 330, 390
        put(img, ace(v, "kestrel", np.radians(-60)), kx, ky)
        aimed_fan(img, kx - 10, ky + 10, 250, sy, 5, 24, 30, kind="orb")
        d = ImageDraw.Draw(img)
        d.ellipse([250 - 85, sy - 85, 250 + 85, sy + 85], outline=(255, 255, 255, 90))
        text(img, 250 + 62, sy - 84, "85 PX", (220, 220, 220))
        ship(img, 250, sy)
        rook(img, 302, sy + 14)
        caption(img, "REST -30 %; HARKER'S DOUBLE TAP -> TRIPLE; KESTREL COMMITS: PULL-OUT 85 PX, BURST OF 5", y=40)
    boss_bar(img, "HONOUR GUARD", segs, tags)
    return img


# ---- the conductor's timeline: a seeded draw under the rules, every telegraphed line or dive on its own

POOLS = {"harker": [("SNIPER", 2.1, True, True), ("PAIRS", 1.6, False, True), ("FAN", 1.0, False, True),
                    ("SLIDE", 1.2, False, False)],
         "vesper": [("SPIRAL", 2.5, False, True), ("ROLL FAN", 1.2, False, True), ("RIBBON", 1.5, False, True)],
         "kestrel": [("FEINT DIVE", 1.6, True, True), ("REAR RAKE", 2.4, False, True),
                     ("OVERHEAD", 2.4, True, True)]}
JOIN = {"harker": 2.5, "vesper": 16.0, "kestrel": 31.0}
DEATH = {"vesper": 52.0}
TELL = 0.8


def conductor(seed=72, dur=60.0, dt=0.05):
    """Run the playbook draw with the conductor's rules; returns per-ace blocks (start, tell_end, end, name, line,
    fires) and the measured worst case (lines at once, aces firing at once)."""
    rng = np.random.default_rng(seed)
    state = {a: dict(free=JOIN[a] + 2.5, last=None) for a in ACES}
    blocks = {a: [] for a in ACES}
    t = 0.0
    while t < dur:
        alive = [a for a in ACES if JOIN[a] <= t and t < DEATH.get(a, 1e9)]
        rest = 1.2 if len(alive) == 1 else 2.0
        if t >= DEATH["vesper"]:
            rest *= 0.7
        for a in alive:
            st = state[a]
            if t < st["free"]:
                continue
            active = [b for o in ACES if o != a for b in blocks[o] if b[0] <= t < b[2]]
            telling = any(b[0] <= t < b[1] for b in active)
            line_on = any(b[4] for b in active)
            firing = sum(1 for b in active if b[5])
            opts = [p for p in POOLS[a] if p[0] != st["last"] and (len(alive) > 1 or p[0] != "SLIDE")]
            if telling:
                continue
            pick = opts[rng.integers(len(opts))]
            if pick[2] and line_on:
                quiet = [p for p in opts if not p[2]]
                if not quiet:
                    continue
                pick = quiet[rng.integers(len(quiet))]
            if pick[3] and firing >= 2:
                quiet = [p for p in opts if not p[3]]
                if not quiet:
                    st["free"] = t + 0.4
                    continue
                pick = quiet[0]
            blocks[a].append((t, t + TELL, t + TELL + pick[1], pick[0], pick[2], pick[3]))
            st["last"], st["free"] = pick[0], t + TELL + pick[1] + rest
        t += dt
    worst_line = worst_fire = 0
    for tt in np.arange(0, dur, dt):
        act = [b for a in ACES for b in blocks[a] if b[0] <= tt < b[2]]
        worst_line = max(worst_line, sum(1 for b in act if b[4]))
        worst_fire = max(worst_fire, sum(1 for b in act if b[5] and b[1] <= tt))
    return blocks, worst_line, worst_fire


def conductor_strip(width=984):
    blocks, wl, wf = conductor()
    lane, x0, dur = 34, 70, 60.0
    img = Image.new("RGBA", (width, 30 + lane * 3 + 30), raster.SHEET_BG)
    d = ImageDraw.Draw(img)
    k = (width - x0 - 10) / dur
    for s in range(0, 61, 5):
        x = x0 + s * k
        d.line([x, 18, x, 22 + lane * 3], fill=(50, 56, 72, 255))
        text(img, x - 6, 6, f"{s}S", raster.LABEL_DIM)
    for i, a in enumerate(ACES):
        y = 22 + i * lane
        text(img, 6, y + 12, ACE_NAME[a], (255, 214, 120))
        jx = x0 + JOIN[a] * k
        d.polygon([(jx - 5, y + 2), (jx + 5, y + 2), (jx, y + 9)], fill=(255, 200, 70, 255))
        if a in DEATH:
            dx_ = x0 + DEATH[a] * k
            d.line([dx_ - 5, y + 6, dx_ + 5, y + 26], fill=(255, 80, 60, 255), width=2)
            d.line([dx_ + 5, y + 6, dx_ - 5, y + 26], fill=(255, 80, 60, 255), width=2)
        for t0, t1, t2, name, line, fires in blocks[a]:
            xa, xb, xc = x0 + t0 * k, x0 + t1 * k, x0 + t2 * k
            col = (200, 50, 40, 255) if line else ((190, 150, 60, 255) if fires else (70, 80, 100, 255))
            for hx_ in range(int(xa), int(xb), 3):
                d.line([hx_, y + 26, hx_ + 3, y + 8], fill=col)
            d.rectangle([xb, y + 8, xc, y + 26], fill=col, outline=(20, 20, 26, 255))
            if xc - xb > raster.text_width(name) + 4:
                text(img, xb + 2, y + 14, name, (255, 245, 230))
    text(img, x0, 26 + lane * 3,
         f"RED: A TELEGRAPHED LINE OR DIVE, GOLD: FIRING, GREY: NO FIRE, HATCHED: THE 0.8 S TELL. MEASURED OVER "
         f"THE RUN: LINES AT ONCE {wl}, ACES FIRING AT ONCE {wf}.", raster.LABEL)
    return img


def guard_sheet(v):
    srcs = [ace_source(v, a) for a in ACES]
    shares = {a: u60.gold_share(ace(v, a, DOWN)) for a in ACES}
    src = panel_grid(srcs, 3, labels=[f"{ACE_NAME[a]} - GOLD {shares[a] * 100:.0f} %" for a in ACES], pad=12)
    rows = []
    for a in ACES:
        rows.append(panel_grid([ace(v, a, k * TAU / 16) for k in range(0, 16, 2)], 8, pad=6))
    turning = panel_grid(rows, 1, pad=4, labels=[f"{ACE_NAME[a]}: 8 OF 16 HEADINGS AT 1X ({ACE_N} PX)" for a in ACES])
    tells = []
    bg = guard_backdrop().crop((140, 20, 340, 220))
    for a in ACES:
        for mode in ("rest", "tell", "shield"):
            c = bg.copy()
            sp = ace(v, a, DOWN)
            put(c, sp, 100, 100)
            if mode == "tell":
                c = ace_tell(c, v, a, 100, 100, 1.0)
            if mode == "shield":
                u60.shield(c, sp, 100, 100, alpha=0.4)
            tells.append(c.resize((100, 100), Image.LANCZOS))
    tell_grid = panel_grid(tells, 9, pad=6, labels=[f"{ACE_NAME[a][:4]} {m.upper()}" for a in ACES
                                                    for m in ("rest", "tell", "shield")])
    field = guard_backdrop()
    put(field, ace(v, "harker", DOWN), 240, 110)
    put(field, ace(v, "vesper", np.radians(200)), 120, 250)
    put(field, ace(v, "kestrel", np.radians(-70)), 380, 420)
    ship(field, 240, 470)
    rook(field, 292, 484)
    zoom = guard_backdrop().crop((0, 60, 200, 260))
    put(zoom, ace(v, "harker", DOWN), 50, 90)
    put(zoom, ace(v, "vesper", np.radians(30)), 140, 120)
    zoom = zoom.resize((400, 400), Image.NEAREST)
    combo = panel_grid([field.crop((0, 0, 480, 540)), zoom], 2, pad=12,
                       labels=["THE THREE OVER LEVEL 40 AT 1X", "OVER JUPITER'S CREAM LIMB, 2X"])
    name = {"a": "A - MARKS ON ONE AIRFRAME", "b": "B - THREE SILHOUETTES"}[v]
    notes = [f"HONOUR GUARD {name}: VORNE'S THREE ACES IN THE Q9 A ELITE LIVERY (GLOSS BLACK, HEAVY GOLD, THE STRONG "
             f"TOP-LEFT GOLD RIM AND THE RED FAR-SIDE RIM, RED RUNNING LIGHTS), 1.5X A TALON ({ACE_N} PX CELLS, "
             f"{ACE_PX:.0f} PX A MODEL UNIT).",
             "GOLD SHARE (THE BRIEF ASKS 35-45 %): " + ", ".join(f"{ACE_NAME[a]} {shares[a] * 100:.0f} %"
                                                                 for a in ACES) + ".",
             "TELLS (0.8 S): HARKER'S BARREL GLINTS RED BEFORE THE SNIPER LINE; VESPER'S RIBBONS FLARE WHITE INTO A LOOP OR "
             "ROLL; KESTREL'S ENGINES FLARE WHITE AS ITS NOSE SWINGS TO THE SHIP (A DIVE). THE HEX SHIELD IN GOLD AT 40 %.",
             ("A: ONE AIRFRAME, THE TALON'S; THE ACES TOLD BY PAINT: HARKER'S SNIPER BARREL UNDER THE NOSE AND A RED NOSE "
              "BAND; VESPER'S WHITE RIBBON STRIPES ON THE WINGS AND TAIL; KESTREL'S GOLD CHEVRONS, RED TAIL BAND AND TWIN "
              "SWEPT FINS. CHEAP AND CLOSE TO THE TALONS THE PLAYER KNOWS; AT PLAY SIZE THE PAINT CARRIES THE READ."
              if v == "a" else
              "B: THREE AIRFRAMES: HARKER'S NOSE STRETCHED WITH A LONGER BARREL (A SNIPER'S NEEDLE); VESPER'S LONG, SLIM, "
              "SWEPT-BACK WINGS WITH TRAILING WHITE RIBBONS; KESTREL SHORT AND WIDE WITH FORWARD CANARDS AND BIGGER TWIN "
              "FINS. THE SHAPE TELLS THE ACE BEFORE THE PAINT, EVEN UNDER THE SHIELD OR IN A FLASH."),
             "WORRIES: GLOSS BLACK OVER BLACK SPACE LEANS ON THE RIM AND THE GOLD (SEE THE 1X FIELD); THE SNIPER'S RED "
             "LINE MUST NOT BE READ AS A FLEET LIGHT (IT IS THE ONLY RED LINE ON THE FIELD)."]
    img = compose(f"HONOUR GUARD {name}", [("SOURCE RENDERS (HEADING DOWN)", src), ("AT PLAY SIZE", turning),
                                           ("REST / TELL / SHIELD, 0.5X OF A 200 PX CROP", tell_grid),
                                           ("READABILITY", combo)], notes)
    save_png(img, DESIGN / "enemies" / "bosses" / "honour-guard" / "concept" / f"honour-guard-r72-{v}.png")


def guard_storyboard(v):
    cells = [field_cell(guard_panel(v, i)) for i in range(len(GUARD_PANELS))]
    board = panel_grid(cells, 4, pad=8, labels=[f"{i + 1}. {GUARD_PANELS[i].split(':')[0]}" for i in range(len(cells))])
    name = {"a": "A - MARKS ON ONE AIRFRAME", "b": "B - THREE SILHOUETTES"}[v]
    notes = [f"THE JOIN ORDER AND THE CONDUCTOR (Q5 A, STAGGERED THEN TOGETHER), {name}."]
    notes += [f"{i + 1}. {p}." for i, p in enumerate(GUARD_PANELS)]
    notes += ["THE CONDUCTOR (BOTTOM): A SEEDED RUN OF THE THREE PLAYBOOKS UNDER ITS RULES - NO ACE STARTS WHILE "
              "ANOTHER'S TELL RUNS, A LINE OR DIVE WAITS FOR THE ONE IN FLIGHT, A THIRD ACE PICKS A NON-FIRING MOVE "
              "WHILE TWO FIRE; REST 1.2 S ALONE, 2.0 S WITH TWO OR THREE, -30 % AFTER A DEATH.",
              "WORRY: WITH THREE ACES THE SCREEN IS BUSY; THE RULE KEEPS EVERY THREAT TOLD ON ITS OWN, BUT A PLAYER "
              "MAY READ THE WAITS AS A PATTERN. THE PINCER IS LEFT OUT OF THE TIMELINE RUN (AT MOST EVERY FOURTH DRAW)."]
    img = compose(f"HONOUR GUARD - JOINS {name}", [("STORYBOARD (0.5X)", board),
                                                   ("THE CONDUCTOR, A 60 S RUN", conductor_strip())], notes)
    save_png(img, DESIGN / "enemies" / "bosses" / "honour-guard" / "concept" / f"honour-guard-joins-r72-{v}.png")


def make_guard(variants):
    load_aces(variants)
    for v in variants:
        guard_sheet(v)
        guard_storyboard(v)



# =========================================================================== 2 the Ascendant

PPU = 100.0                                   # px per model unit: the hull is drawn 1:1 in screen px
KEY = np.array((-2.2, 2.6, 3.2))
V0 = 10                                       # first Vrell slot after the elite ones
V_BODY, V_DARK, V_SEAM, V_GLOW, V_EYE, V_BONE, V_SAC = range(V0, V0 + 7)


def asc_mats(glow=1.0, core=1.0):
    mats = elite_mats(glow)
    mats[A_GUN] = Material(hx("3a3a44"), metal=0.8, shininess=110, spec=1.0)
    vr = u60.vrell_mats("1e5c5a", "0c2422", "8a9a7a", TEAL, LIME, glow=glow)
    vr[4] = Material((0.04, 0.04, 0.04), emission=tuple(np.array(LIME) / 255 * 1.6 * core))
    return mats + vr


def sx(x):
    return (x - 240) / PPU


def sy(y):
    return (150 - y) / PPU


def _strip(job):
    """Rows i of n of a region render in screen px (x0, y0, w, h); the key light fixed in model space."""
    builder, args, (x0, y0, w, h), factor, i, n = job
    scene, mats = builder(*args)
    E = w / PPU
    C = np.array((sx(x0 + w / 2), sy(y0 + h / 2)))
    L = KEY * (E / 2.3) + np.array((C[0], C[1], 0.0))
    r0, r1 = h * i // n, h * (i + 1) // n
    cs = np.array((C[0], sy(y0 + (r0 + r1) / 2)))
    kp = (L - np.array((cs[0], cs[1], 0.0))) / (E / 2.3)
    hi = sdf.render(scene, mats, (w * factor, (r1 - r0) * factor), E, center=tuple(cs), z_top=2.0, steps=120,
                    key_pos=tuple(kp))
    return i, hi


def render_region(ex, builder, args, box, factor=3, n=8):
    """A model drawn 1:1 over the screen box (x0, y0, w, h) -> native RGBA sprite (unquantised, 1-bit alpha)."""
    parts = dict(ex.map(_strip, [(builder, args, box, factor, i, n) for i in range(n)]))
    hi = np.concatenate([parts[i] for i in range(n)], axis=0)
    img = sprite.to_image(sprite.downsample(hi, factor), 0.5)
    return sprite.sharpen(img, 70)


# ---- variant a: the gilded wedge

WEDGE = [(0.0, sy(262)), (sx(330), sy(190)), (sx(420), sy(80)), (sx(440), sy(-20)), (0.0, sy(-20))]
LIGHTS_A = [(sx(240 + 22 * k), sy(262 - 21 * k)) for k in range(1, 9)]


def split_w(Y, split):
    return split * (0.1 + 0.36 * np.clip((Y - sy(240)) / 2.6, 0, 1))


def hull_a(split=0.0, glow=1.0):
    breaches = [(sx(150), sy(125)), (sx(330), sy(125))]

    def scene(p):
        q = mirror_x(p)
        X, Y = q[:, 0], q[:, 1]
        roof = lambda x, y: np.clip(1.0 - 0.44 * np.abs(x), 0.1, 1.0)
        d = sd_plate(q, WEDGE, 0.0, 0.85, 0.03, taper=roof)
        items = [(d, A_HULL)]
        crest = sd_cylinder_z(p, (0, sy(160), 0.85), 0.16, 0.04)
        items.append((crest, A_GOLD))
        for lx, ly in LIGHTS_A:
            items.append((sd_sphere(q, (lx - 0.07, ly + 0.03, 0.85 * (1 - 0.44 * (lx - 0.07)) - 0.02), 0.03), A_RED))
        if split > 0:
            w = split_w(Y, split)
            cut = np.where(Y > sy(250), np.abs(X) - w, 1.0)
            d0, m0 = union(*items)
            d0 = np.maximum(d0, -cut)
            for bx_, by_ in breaches:
                d0 = np.maximum(d0, -(np.hypot(p[:, 0] - bx_, p[:, 1] - by_) - 0.2 * split))
            items = [(d0, m0)]
            tis = np.full(len(p), 9.0)
            for k, yy in enumerate(np.linspace(sy(245), sy(-20), 11)):
                r = split_w(yy, split)
                tis = sdf.smin(tis, sd_ellipsoid(p, (0.05 * np.sin(k * 2.3), yy, 0.55 + 0.25 * split),
                                                 (r * 1.2 + 0.04, 0.17, 0.2 * split + 0.08)), 0.1)
            for bx_, by_ in breaches:
                hz = 0.85 * (1 - 0.44 * abs(bx_))
                tis = sdf.smin(tis, sd_ellipsoid(p, (bx_, by_, hz), (0.2 * split, 0.18 * split, 0.14 * split)), 0.08)
            items.append((tis, V_SAC))
            for k, yy in enumerate(np.linspace(sy(225), sy(5), 7)):          # plates peeled outward
                w0 = split_w(yy, split)
                pc = sdf.rotate_y(q - np.array((w0 + 0.06, yy, 0.86 - 0.44 * w0)), -0.7 * split)
                items.append((sd_box(pc, (0, 0, 0), (0.08, 0.1, 0.014)), A_GOLD if k % 2 else A_FACET))
        d, m = union(*items)
        hullm = np.isin(m, (A_HULL, A_FACET))
        ang = np.arctan2(X, 3.4 - Y)
        rib = ((ang * 9.5) % 1.0) < 0.27
        m = np.where((m == A_HULL) & (np.floor(ang * 9.5) % 2 == 1) & (X > 0.16), A_FACET, m)
        m = np.where(np.isin(m, (A_HULL, A_FACET)) & rib & (X > 0.16), A_GOLD, m)
        edge = sdf.sd_polygon2(X, Y, WEDGE) > -0.1
        m = np.where(hullm & edge, A_GOLD, m)
        m = np.where((m == A_HULL) & (X < 0.035), A_GOLD, m)
        band = (np.abs(Y - sy(100)) < 0.03) | (np.abs(Y - sy(200)) < 0.03)
        m = np.where((m == A_HULL) & band, A_FACET, m)
        return d, m
    return scene, asc_mats(glow)


# ---- variant b: the armoured tiers (one block, one fin, repeated)

BLOCK = (56, 40)
ROWS_B = [(18 + r * 40, 7 - r) for r in range(6)]


def block_positions():
    out = []
    for r, (y, n) in enumerate(ROWS_B):
        for i in range(n):
            out.append((240 + (i - (n - 1) / 2) * 60, y, r, i, n))
    return out


def fin_positions():
    out = []
    for r, (y, n) in enumerate(ROWS_B[1:], 1):
        for s in (-1, 1):
            out.append((240 + s * ((n - 1) / 2 * 60 + 34), y - 20))
    return out


def block_model(glow=1.0):
    def scene(p):
        d = sd_box(p, (0, 0, 0), (0.27, 0.19, 0.14), 0.03)
        top = sd_box(p, (0, 0.02, 0.12), (0.22, 0.13, 0.04), 0.02)
        items = [(d, A_HULL), (top, A_FACET), (sd_sphere(p, (0.22, -0.15, 0.13), 0.022), A_RED)]
        d, m = union(*items)
        m = np.where((m == A_HULL) & ((p[:, 1] < -0.08) | (np.abs(p[:, 0]) > 0.22)), A_GOLD, m)
        m = np.where((m == A_FACET) & ((np.abs(p[:, 0]) < 0.06) | (p[:, 1] < -0.06)), A_GOLD, m)
        return d, m
    return scene, asc_mats(glow)


def fin_model(glow=1.0):
    def scene(p):
        d = sd_plate(p, [(0.0, -0.18), (0.1, 0.12), (0.0, 0.2), (-0.1, 0.12)], 0.1, 0.06, 0.01,
                     taper=lambda x, y: np.clip(1 - 4 * np.abs(x), 0.2, 1))
        return d, np.full(len(p), A_GOLD)
    return scene, asc_mats(glow)


def breach_model(k=1.0, glow=1.0):
    """b's tissue bursting through a gap: a bulge of teal-black tissue with glowing seams."""
    def scene(p):
        d = 9.0
        for j, (bx_, by_, r) in enumerate([(0, 0, 0.22), (-0.16, 0.06, 0.13), (0.15, -0.05, 0.14), (0.04, 0.16, 0.1),
                                           (-0.06, -0.15, 0.11)]):
            d = sdf.smin(d, sd_ellipsoid(p, (bx_ * k, by_ * k, 0.05), (r * k, r * k * 0.9, r * k * 0.8)), 0.06)
        return d, np.where(p[:, 2] > 0.08 * k, V_SAC, V_BODY)
    return scene, asc_mats(glow)


# ---- parts (shared by both)

def cannon_model(glow=1.0):
    def scene(p):
        q = mirror_x(p)
        base = sd_cylinder_z(p, (0, 0.04, 0.0), 0.24, 0.12)
        dome = sd_ellipsoid(p, (0, 0.06, 0.1), (0.2, 0.17, 0.14))
        barrel = sd_box(q, (0.07, -0.17, 0.12), (0.035, 0.16, 0.04), 0.015)
        lens = sd_sphere(p, (0, -0.06, 0.2), 0.05)
        collar = np.maximum(sd_cylinder_z(p, (0, 0.04, 0.05), 0.27, 0.05), -sd_cylinder_z(p, (0, 0.04, 0.05), 0.22, 0.2))
        return union((base, A_GUN), (collar, A_GOLD), (dome, A_HULL), (barrel, A_GUN), (lens, A_RED))
    return scene, asc_mats(glow)


def generator_model(core=1.0):
    def scene(p):
        collar = np.maximum(sd_cylinder_z(p, (0, 0, 0.0), 0.24, 0.1), -sd_cylinder_z(p, (0, 0, 0.0), 0.15, 0.3))
        base = sd_cylinder_z(p, (0, 0, -0.04), 0.22, 0.06)
        orb_ = sd_sphere(p, (0, 0, 0.06), 0.13)
        fins = [sd_box(sdf.rotate_z(p, a), (0.2, 0, 0.06), (0.06, 0.02, 0.05)) for a in np.linspace(0, TAU, 9)[:-1]]
        return union((base, A_HULL), (collar, A_GOLD), (np.minimum.reduce(fins), A_GOLD), (orb_, A_ENGINE))
    mats = asc_mats()
    mats[A_ENGINE] = Material((0.08, 0.06, 0.02), emission=tuple(np.array((1.0, 0.86, 0.5)) * 1.8 * core))
    return scene, mats


def battery_model(glow=1.0):
    def scene(p):
        q = mirror_x(p)
        base = sd_cylinder_z(p, (0, 0, 0.0), 0.18, 0.1)
        top = sd_ellipsoid(p, (0, 0.02, 0.08), (0.14, 0.13, 0.09))
        guns = sd_box(q, (0.06, -0.14, 0.1), (0.025, 0.1, 0.03), 0.01)
        return union((base, A_GUN), (top, A_HULL), (guns, A_GUN), (sd_sphere(p, (0, 0.06, 0.16), 0.03), A_RED))
    return scene, asc_mats(glow)


def panel_model(glow=1.0):
    """A mirror panel: a polished silver-gold crescent in front of the battery (facing down)."""
    def scene(p):
        r = np.hypot(p[:, 0], p[:, 1] - 0.18)
        ring = np.maximum(np.abs(r - 0.36) - 0.05, -(0.18 - p[:, 1]) - 0.04)
        ring = np.maximum(ring, np.abs(p[:, 2] - 0.12) - 0.035)
        return ring, np.full(len(p), A_GLASS)
    mats = asc_mats(glow)
    mats[A_GLASS] = Material((0.95, 0.86, 0.62), metal=1.0, shininess=200, spec=1.6)
    return scene, mats


def sac_model(swell=1.0):
    def scene(p):
        body = sd_ellipsoid(p, (0, 0, 0.05), (0.19 * swell, 0.18 * swell, 0.15 * swell))
        core = sd_sphere(p, (0.0, -0.02, 0.15 * swell), 0.07 * swell)
        veins = sd_ellipsoid(p, (0, 0, 0.04), (0.22 * swell, 0.21 * swell, 0.1))
        return union((veins, V_DARK), (body, V_SAC), (core, V_EYE))
    return scene, asc_mats()


def heart_model(open_=0.0):
    def scene(p):
        mem = sd_ellipsoid(p, (0, 0, 0.06), (0.28, 0.26, 0.16))
        lips = np.maximum(mem, -sd_ellipsoid(p, (0, 0, 0.2), (0.08 + 0.12 * open_, 0.2, 0.2)))
        core = sd_ellipsoid(p, (0, 0, 0.08), (0.14, 0.17, 0.12))
        return union((lips, V_BODY), (core, V_EYE))
    return scene, asc_mats(core=0.4 + 0.8 * open_)


def lifeboat_model(v):
    """Vorne's lifeboat: grown, not built - Vrell tissue with the gold escape capsule half swallowed, its gold canopy
    showing (nose up: it flees up the screen). a: a round seed-pod; b: a long thorned pod trailing tendrils."""
    def scene(p):
        q = mirror_x(p)
        if v == "a":
            pod = sd_ellipsoid(p, (0, -0.04, 0.0), (0.28, 0.33, 0.2))
            lobes = sd_ellipsoid(q, (0.15, -0.2, 0.0), (0.13, 0.17, 0.13))
            pod = sdf.smin(pod, lobes, 0.06)
            pod = np.maximum(pod, -sd_sphere(p, (0.05, 0.14, 0.2), 0.13))      # the gold capsule shows through
            cap = sd_capsule(p, (0.07, -0.14, 0.08), (0.04, 0.2, 0.1), 0.095)
            can = sd_ellipsoid(p, (0.04, 0.17, 0.19), (0.05, 0.075, 0.04))
            items = [(pod, V_SAC), (cap, A_GOLD), (can, A_GLASS)]
        else:
            pod = sd_ellipsoid(p, (0, -0.08, 0.0), (0.17, 0.4, 0.15))
            thorns = [sd_capsule(q, (0.1, y, 0.04), (0.26, y - 0.1, 0.0), 0.035, 0.008) for y in (0.1, -0.1, -0.3)]
            tend = [sd_capsule(q, (0.05, -0.45, 0.0), (0.12 + 0.05 * k, -0.62 - 0.06 * k, -0.02), 0.025, 0.01)
                    for k in range(2)]
            pod = np.maximum(pod, -sd_sphere(p, (0.0, 0.22, 0.15), 0.1))
            cap = sd_capsule(p, (0.0, -0.02, 0.05), (0.0, 0.26, 0.07), 0.075)
            can = sd_ellipsoid(p, (0.0, 0.24, 0.13), (0.045, 0.07, 0.04))
            items = [(pod, V_SAC), (np.minimum.reduce(thorns), V_BONE), (np.minimum.reduce(tend), V_BODY),
                     (cap, A_GOLD), (can, A_GLASS)]
        return union(*items, k=0.02)
    mats = asc_mats()
    mats[A_GLASS] = Material((0.08, 0.06, 0.03), emission=(1.0, 0.75, 0.35))
    return scene, mats


def _part_job(job):
    name, builder, args, size, factor, colors = job
    scene, mats = builder(*args)
    w, h = size
    hi = sdf.render(scene, mats, (w * factor, h * factor), w / PPU, z_top=2.0, steps=110)
    sp = sprite.make_sprite(hi, factor, colors, crisp=80)
    return name, sp


ASC = {}


def load_ascendant(variants):
    parts = [("cannon", cannon_model, (), (70, 50), 5, 48),
             ("generator", generator_model, (1.0,), (52, 52), 5, 40),
             ("generator-dim", generator_model, (0.35,), (52, 52), 5, 40),
             ("generator-dead", generator_model, (0.0,), (52, 52), 5, 40),
             ("battery", battery_model, (), (48, 48), 5, 40),
             ("panel", panel_model, (), (48, 48), 5, 32),
             ("sac", sac_model, (1.0,), (44, 44), 5, 40),
             ("sac-small", sac_model, (0.7,), (44, 44), 5, 40),
             ("heart", heart_model, (0.0,), (64, 64), 5, 40),
             ("heart-open", heart_model, (1.0,), (64, 64), 5, 40),
             ("block", block_model, (), BLOCK, 5, 40),
             ("fin", fin_model, (), (24, 44), 5, 24),
             ("breach", breach_model, (1.0,), (70, 56), 5, 40),
             ("breach-small", breach_model, (0.5,), (70, 56), 5, 40),
             ("lifeboat-a", lifeboat_model, ("a",), (64, 84), 5, 48),
             ("lifeboat-b", lifeboat_model, ("b",), (64, 84), 5, 48)]
    with pool() as ex:
        for name, sp in ex.map(_part_job, [p for p in parts if p[0] not in ASC]):
            ASC[name] = sp
        if "a" in variants and "hull-a" not in ASC:
            ASC["hull-a"] = render_region(ex, hull_a, (0.0,), (20, 0, 440, 270))
            ASC["hull-a2"] = render_region(ex, hull_a, (1.0,), (20, 0, 440, 270))
            for s in (0.35, 0.7):
                ASC[f"keel-a-{s}"] = render_region(ex, hull_a, (s,), (130, 0, 220, 270), n=4)
            for k in ("hull-a", "hull-a2"):
                ASC[k] = shade_faces(ASC[k], 20)
            for s in (0.35, 0.7):
                ASC[f"keel-a-{s}"] = shade_faces(ASC[f"keel-a-{s}"], 130)


def shade_faces(img, x0):
    """The gloss-black faces told apart as the top-left sun would: the left face lifted with a soft blue-grey sheen
    and a long specular streak, the right face a step darker (gold and lights untouched)."""
    a = arr(img)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    X = xx + x0
    dark = (a[..., :3].max(-1) < 70) & (a[..., 3] > 0)
    left = X < 240
    k = np.where(left, 1.55 + 0.25 * np.exp(-((X - 150 - (yy - 60) * 0.55) / 26) ** 2), 0.8)
    lift = np.where(left, 1.0, 0.0)[..., None] * np.array((10, 12, 18.0))
    a[..., :3] = np.where(dark[..., None], a[..., :3] * k[..., None] + lift, a[..., :3])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def hull_b(phase=1, erupt=1.0):
    """b's hull composed from the repeated block and fin over a tiled dark base (code-drawn here)."""
    img = Image.new("RGBA", (480, 290), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    pts = []
    for r, (y, n) in enumerate(ROWS_B):
        hw = (n - 1) / 2 * 60 + 30
        pts.append((240 - hw, y - 20))
        pts.append((240 - hw, y + 20))
    left = pts
    right = [(480 - x, y) for x, y in reversed(pts)]
    d.polygon(left + [(240, 262)] + right, fill=(16, 14, 20, 255))
    base = arr(img)
    base[..., :3] *= (1.5 - 0.7 * np.clip(XX[:290, :480] / 480 + YY[:290, :480] / 580, 0, 1))[..., None]
    img = Image.fromarray(np.clip(base, 0, 255).astype(np.uint8), "RGBA")
    rng = np.random.default_rng(42)
    breaches = [(1, 2), (2, 1), (3, 2), (2, 4), (4, 1), (0, 5)]
    for x, y, r, i, n in block_positions():
        b = ASC["block"]
        if phase == 2 and (r, i) in breaches and erupt > 0:
            ang = rng.uniform(-25, 25) * erupt
            off = rng.uniform(6, 14) * erupt
            b = b.rotate(ang, resample=Image.BICUBIC, expand=True)
            put(img, darkened(b, 0.8), x + np.sign(x - 240 + 0.1) * off, y - off * 0.4)
            continue
        put(img, b, x, y)
    for x, y in fin_positions():
        put(img, ASC["fin"], x, y)
    if phase == 2:
        for (r, i) in breaches:
            y, n = ROWS_B[r]
            x = 240 + (i - (n - 1) / 2) * 60
            br = ASC["breach"] if erupt >= 0.9 else ASC["breach-small"]
            if (r + i) % 2:
                br = br.transpose(Image.FLIP_LEFT_RIGHT)
            put(img, br.resize((max(1, int(br.width * (0.5 + 0.5 * erupt))), max(1, int(br.height * (0.5 + 0.5 * erupt)))),
                               Image.BICUBIC), x, y)
    return img


BATT = [(150, 130, -1), (185, 190, -1), (330, 130, 1), (295, 190, 1)]
GENS = [(110, 60), (370, 60)]
SACS = [(150, 125), (330, 125), (240, 60)]
HEART = (240, 152)


def panel_state(sp, state):
    if state == "heating":
        sp = light(tinted(sp, (255, 120, 40), 0.45), sp.width / 2, sp.height / 2, 10, (255, 90, 30), 0.6)
    elif state == "cracked":
        sp = tinted(sp, (255, 70, 30), 0.3)
        d = ImageDraw.Draw(sp)
        c = sp.width / 2
        for a in (0.3, 1.3, 2.4):
            d.line([c, c + 6, c + np.cos(a) * 18, c + 6 + np.sin(a) * 12], fill=(30, 10, 6, 255), width=1)
    elif state == "down":
        sp = darkened(sp, 0.35)
    return sp


def hull_sprite(v, phase=1, erupt=1.0):
    if v == "a":
        if phase == 1:
            return ASC["hull-a"], (20, 0)
        img = ASC["hull-a2"].copy()
        if erupt < 0.5:
            img = ASC["hull-a"].copy()
            img.alpha_composite(ASC["keel-a-0.35"], (110, 0))
        elif erupt < 1.0:
            img = ASC["hull-a"].copy()
            img.alpha_composite(ASC["keel-a-0.7"], (110, 0))
        return img, (20, 0)
    return hull_b(phase, erupt), (0, 0)


def ascendant_layer(v, phase=1, erupt=1.0, dead=(), panels=None, shield=True, heart_open=False, sacs=(1, 1, 1),
                    dy=0, lamps=1.0):
    """The whole boss at its arena place on a 480x540 layer."""
    lay = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    hull, (ox, oy) = hull_sprite(v, phase, erupt)
    lay.alpha_composite(hull if lamps >= 1 else darkened(hull, 0.55 + 0.45 * lamps), (ox, oy + dy))
    if phase == 1:
        for i, (x, y) in enumerate(GENS):
            g = ASC["generator-dead" if f"gen{i}" in dead else "generator"]
            put(lay, g, x, y + dy)
        for i, (x, y, s) in enumerate(BATT):
            put(lay, ASC["battery"], x, y + dy)
            st = (panels or {}).get(i, "whole")
            pa = panel_state(ASC["panel"], st).rotate(-s * 45 + (panels or {}).get(f"turn{i}", 0), resample=Image.BICUBIC,
                                                      expand=True)
            put(lay, pa, x - s * 8, y + 10 + dy)
        put(lay, ASC["cannon"], 240, 235 + dy)
        if shield:
            u60.shield(lay, ASC["cannon"], 240, 235 + dy, mode="bubble", alpha=0.4)
    else:
        for i, (x, y) in enumerate(SACS):
            if sacs[i]:
                put(lay, ASC["sac"] if sacs[i] == 1 else ASC["sac-small"], x, y + dy)
        put(lay, ASC["heart-open" if heart_open else "heart"], HEART[0], HEART[1] + dy)
    return lay


def gold_beams(img, dead=(), dy=0):
    for i, (x, y) in enumerate(GENS):
        if f"gen{i}" not in dead:
            line_glow(img, [x, y + dy, 240, 235 + dy], (255, 200, 80), 2, 0.7, core=(255, 250, 220))
    return img


def l42():
    if "l42" not in _BG:
        _BG["l42"] = space_l42()
    return to_img(_BG["l42"])


def escape_bar(img, frac_time, frac_cap, name="VORNE", label=None):
    d = ImageDraw.Draw(img)
    x0, x1, y = 120, FW - 60, 6
    d.rectangle([x0 - 2, y, x1 + 2, y + 13], fill=(8, 8, 12, 230), outline=(140, 110, 50, 255))
    d.rectangle([x0, y + 3, x0 + (x1 - x0) * frac_time, y + 6], fill=(255, 150, 40, 255))
    d.rectangle([x0, y + 8, x0 + (x1 - x0) * frac_cap, y + 10], fill=(255, 214, 90, 255))
    nw = raster.text_width(name)
    d.rectangle([x0 - nw - 10, y, x0 - 4, y + 13], fill=(8, 8, 12, 230))
    text(img, x0 - nw - 6, y + 4, name, (255, 214, 120))
    if label:
        text(img, (x0 + x1) / 2 - raster.text_width(label) / 2, y + 17, label, (255, 230, 170))
    return img


def wake(img, x, y, n=5):
    for j in range(n):
        spore(img, x + (8 if j % 2 else -8), y + 24 + j * 26, 4)
    return img


# --------------------------------------------------------------------------- the jagged break (round 73)
# Round 72 A.11's rule: a hull breaks along a jagged fracture - torn plating, a ragged edge, debris and venting along
# the break - never a straight clean cut. Shared by the Ascendant's wreck (storyboard frame 8) and the falling city (b).

def ragged_line(pts, seed, amp=6.0, step=3.0):
    """A seeded irregular fracture through the control points: sampled every ``step`` px and pushed sideways by a few
    long kinks (low frequency), small alternating teeth at every sample and an odd big spike, so no stretch of it is
    straight. Returns an (n, 2) float array, top to bottom."""
    rng = np.random.default_rng(seed)
    P = []
    for (x0, y0), (x1, y1) in zip(pts, pts[1:]):
        n = max(2, int(np.hypot(x1 - x0, y1 - y0) / step))
        t = np.arange(n) / n
        P.append(np.stack([x0 + (x1 - x0) * t, y0 + (y1 - y0) * t], 1))
    P = np.concatenate(P + [np.array([pts[-1]], np.float64)])
    d = np.gradient(P, axis=0)
    nrm = np.stack([-d[:, 1], d[:, 0]], 1)
    nrm /= np.linalg.norm(nrm, axis=1, keepdims=True) + 1e-9
    s = np.r_[0, np.cumsum(np.hypot(*np.diff(P, axis=0).T))]
    low = sum(rng.uniform(0.6, 1.0) * np.sin(s / rng.uniform(10, 34) + rng.uniform(0, TAU)) for _ in range(3)) / 1.6
    teeth = rng.uniform(0.3, 1.0, len(P)) * np.where(np.arange(len(P)) % 2, 1.0, -1.0)
    spikes = np.where(rng.random(len(P)) < 0.12, rng.choice((-1.0, 1.0), len(P)) * rng.uniform(1.2, 2.0, len(P)), 0.0)
    return P + nrm * (amp * (low + 0.55 * teeth + spikes))[:, None]


def ragged_masks(size, line):
    """The pixels left and right of a ragged line that runs from above the layer to below it."""
    m = Image.new("L", size, 0)
    y0, y1 = line[0][1], line[-1][1]
    ImageDraw.Draw(m).polygon([tuple(p) for p in line] + [(-20, y1), (-20, y0)], fill=255)
    left = np.array(m) > 0
    return left, ~left


def _grow(mask, n):
    """Distance in px (1..n) from a mask's pixels, by repeated 3x3 growth; 0 inside, n + 1 beyond."""
    dist = np.full(mask.shape, n + 1, np.int32)
    dist[mask] = 0
    m = Image.fromarray(mask.astype(np.uint8) * 255, "L")
    for i in range(1, n + 1):
        m = m.filter(ImageFilter.MaxFilter(3))
        g = np.array(m) > 0
        dist[g & (dist > i)] = i
    return dist


def torn_half(layer, keep, line, seed, side, band=6, rim=(214, 196, 160), hot=(255, 120, 40), flaps=True,
              plate=(78, 80, 94), lip=(200, 170, 110), size=(6, 11), every=(7, 13)):
    """One half of a broken layer with a torn edge along the break: the plating charred dark toward the edge, bent
    plate lips catching the light in ragged runs, the very edge glowing hot in places, and torn plate flaps bent
    outward across the break (``side`` -1 for the left half, +1 for the right)."""
    a = arr(layer)
    a[~keep, 3] = 0
    h, w = keep.shape
    dist = _grow(~keep, band)
    solid = a[..., 3] > 40
    edge = solid & (dist <= band)
    k = np.clip(dist / band, 0, 1)
    rgb = a[..., :3]
    rgb[edge] *= (0.3 + 0.7 * k[edge])[:, None]
    lit = raster.fbm(w, h, 10, seed + 7, octaves=3, period=False)
    run = edge & (dist >= 2) & (dist <= 3) & (lit > 0.55)
    rgb[run] = rgb[run] * 0.35 + np.asarray(rim, np.float64) * 0.65
    burn = raster.fbm(w, h, 8, seed + 11, octaves=3, period=False)
    glow = edge & (dist <= 2)
    g = (smoothstep(0.45, 0.75, burn) * (1.0 - (dist - 1) / 2.0))[glow]
    rgb[glow] += np.asarray(hot, np.float64) * g[:, None] * 0.8
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    if flaps:
        rng = np.random.default_rng(seed + 23)
        d = ImageDraw.Draw(img)
        i = int(rng.integers(3, 8))
        while i < len(line) - 2:
            (x0, y0), (x1, y1) = line[i - 1], line[i + 1]
            n = np.hypot(x1 - x0, y1 - y0) + 1e-9
            tx, ty = (x1 - x0) / n, (y1 - y0) / n
            nx, ny = ty, -tx                                   # a normal of the break ...
            nx, ny = (nx, ny) if nx * side < 0 else (-nx, -ny)  # ... pointing across it, away from this half
            px, py = line[i]
            ix, iy = int(px - nx * 5), int(py - ny * 5)        # a point just inside this half
            if 0 <= ix < w and 0 <= iy < h and solid[iy, ix] and keep[iy, ix]:
                base = tuple(int(c) for c in np.clip(a[iy, ix, :3] * 0.3 + np.asarray(plate) * 0.7, 0, 255))
                ln = rng.uniform(*size)
                half_w = rng.uniform(size[0] / 2, size[1] / 2)
                bend = rng.uniform(-0.6, 0.6)
                tip = (px + (nx + bend * tx) * ln, py + (ny + bend * ty) * ln)
                b0 = (px - tx * half_w - nx, py - ty * half_w - ny)
                b1 = (px + tx * half_w - nx, py + ty * half_w - ny)
                d.polygon([b0, tip, b1], fill=base + (255,))
                d.line([b0, tip], fill=tuple(lip) + (255,), width=1)   # the bent lip in the light
            i += int(rng.integers(*every))
    return img


def shards(img, pts, seed, n, size=(3, 8), plate=(36, 36, 44), rim=(214, 196, 160), gold=None, spread=(14, 10)):
    """Debris shards: small irregular torn plates (dark, one lit edge, now and then a gold one) around points."""
    rng = np.random.default_rng(seed)
    d = ImageDraw.Draw(img)
    for j in range(n):
        cx, cy = pts[int(rng.integers(len(pts)))]
        cx += rng.normal(0, spread[0])
        cy += rng.normal(0, spread[1])
        r = rng.uniform(*size)
        ang = rng.uniform(0, TAU)
        poly = [(cx + np.cos(ang + q) * r * f, cy + np.sin(ang + q) * r * f)
                for q, f in ((0, 1.0), (rng.uniform(1.4, 2.0), rng.uniform(0.4, 0.8)),
                             (rng.uniform(2.6, 3.4), rng.uniform(0.6, 1.0)), (rng.uniform(4.0, 5.2), rng.uniform(0.3, 0.6)))]
        fill = gold if gold and j % 4 == 0 else plate
        d.polygon(poly, fill=tuple(fill) + (255,))
        d.line([poly[0], poly[1]], fill=tuple(rim) + (255,), width=1)
    return img


def vent(img, x, y, dx, dy, col, length=26, r0=2.0, k=0.9, steps=12):
    """Gas venting from a break: a smooth additive plume of Gaussians along (dx, dy), widening and fading."""
    a = arr(img)
    n = np.hypot(dx, dy) + 1e-9
    for j in range(steps):
        t = j / (steps - 1)
        glow_at(a[..., :3], x + dx / n * length * t, y + dy / n * length * t, r0 + 4.5 * t, col, k * (1 - t) ** 1.4)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def smoke_puff(img, x, y, r, col=(44, 36, 36), k=0.6):
    """A smooth dark puff (a Gaussian blend toward ``col``), for venting smoke over a bright backdrop."""
    a = arr(img)
    h, w = a.shape[:2]
    x0, x1 = max(0, int(x - 3 * r)), min(w, int(x + 3 * r) + 1)
    y0, y1 = max(0, int(y - 3 * r)), min(h, int(y + 3 * r) + 1)
    if x0 < x1 and y0 < y1:
        yy, xx = np.mgrid[y0:y1, x0:x1]
        f = (np.exp(-((xx - x) ** 2 + (yy - y) ** 2) / (r * r)) * k)[..., None]
        a[y0:y1, x0:x1, :3] = a[y0:y1, x0:x1, :3] * (1 - f) + np.asarray(col, np.float64) * f
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def moved_points(pts, scale_x, scale_y, rot_deg, cx, cy, dx=0.0, dy=0.0):
    """Where affine_about() puts layer points (the forward map of its inverse)."""
    a = np.radians(rot_deg)
    c, s_ = np.cos(a), np.sin(a)
    m = np.array([[c / scale_x, s_ / scale_x], [-s_ / scale_y, c / scale_y]])
    p = (np.asarray(pts, np.float64) - (cx, cy)) @ np.linalg.inv(m).T
    return p + (cx + dx, cy + dy)


ASC_BREAK = [(244, -6), (236, 40), (247, 100), (233, 170), (243, 230), (238, 300)]


def ascendant_wreck(v, seed=7301):
    """Frame 8's wreck (round 73): the phase-2 hull split along a ragged fracture down the keel, both halves torn,
    darkened and drifting apart (each turned 6 degrees outward), shards between them and gas venting from both broken
    edges. Returns the layer and the break's points on each moved half."""
    hull = darkened(ascendant_layer(v, phase=2, heart_open=False, sacs=(0, 0, 0)), 0.55)
    line = ragged_line(ASC_BREAK, seed, amp=5.0)
    lm, rm = ragged_masks(hull.size, line)
    lay = Image.new("RGBA", (FW, FH), (0, 0, 0, 0))
    edges = []
    for half, rot, dx in ((torn_half(hull, lm, line, seed, -1), -6, -70),
                          (torn_half(hull, rm, line, seed + 50, 1), 6, 70)):
        lay.alpha_composite(affine_about(half, 1, 1, rot, 240, 270, dx, 60))
        edges.append(moved_points(line, 1, 1, rot, 240, 270, dx, 60))
    return lay, edges


def wreck_break(img, v, seed=7301):
    """The wreck in frame 8 with its debris and venting: shards in the gap and drifting off both broken edges, gas
    (hot orange from the plating, teal from the Vrell tissue) venting from the edges into the gap, smooth glows."""
    lay, (le, re) = ascendant_wreck(v, seed)
    img.alpha_composite(lay)
    ys = lambda e: e[(e[:, 1] > 70) & (e[:, 1] < 300)]       # noqa: E731  (the stretch along the hull)
    le, re = ys(le), ys(re)
    rng = np.random.default_rng(seed + 3)
    for e, sgn in ((le, 1), (re, -1)):
        for j in rng.choice(len(e), 6, replace=False):
            x, y = e[j]
            col = (255, 130, 50) if j % 3 else (60, 255, 170)
            img = vent(img, x, y, sgn * rng.uniform(0.6, 1.0), rng.uniform(-0.9, 0.6), col, length=rng.uniform(14, 34),
                       k=rng.uniform(0.5, 0.9))
    shards(img, np.concatenate([le, re]), seed + 5, 22, size=(3, 8), plate=(74, 76, 90), rim=(190, 186, 176),
           gold=(200, 150, 50), spread=(16, 12))
    return img


# --------------------------------------------------------------------------- the storyboard

ASC_PANELS = ["THE WARNING BEFORE THE HULL SHOWS", "THE HULL SLIDES IN BOW-FIRST", "PHASE 1 FROM THE FAR SIDE",
              "PHASE 1 LATE: GENERATORS DOWN", "THE ERUPTION: VORNE'S ONE SHOUT", "PHASE 2: THE VRELL HALF",
              "K: THE HULL BREAKS", "THE ESCAPE", "ESCAPED"]


def asc_panel(v, i, jagged=False):
    img = l42()
    shipx, shipy = 240, 470
    if i == 0:
        warn_banner(img, "WARNING")
        ship(img, shipx, shipy)
        rook(img, shipx + 52, shipy + 14)
        caption(img, "THE BOSS WARNING (TRACK 22) AS THE SCROLL SLOWS; NOTHING OF THE HULL YET", y=30)
    elif i == 1:
        img.alpha_composite(ascendant_layer(v, dy=-150, lamps=0.4, shield=False))
        boss_bar(img, "ASCENDANT", [(0.6, True)])
        ship(img, shipx, shipy)
        rook(img, shipx + 52, shipy + 14)
        caption(img, "6 S: BOW FIRST FROM THE TOP EDGE, LIGHTS COMING ON FROM THE BOW BACK; INVULNERABLE TILL IN PLACE",
                y=30)
    elif i == 2:
        img = gold_beams(img)
        img.alpha_composite(ascendant_layer(v, panels={0: "whole", 1: "heating", "turn0": 15, "turn1": 18}))
        sniper_line(img, 240, 262, 380, FH, 0.85)
        for t in (0.25, 0.45):
            orb(img, 380 + (185 - 380) * t, 470 + (190 - 470) * t, big=True)
        line_glow(img, [185, 205, 230, 260], (255, 60, 220), 1, 0.6)
        ship(img, 380, 470)
        rook(img, 432, 484)
        boss_bar(img, "ASCENDANT", [(0.92, True)])
        caption(img, "THE LEFT FACE'S PANELS OPEN TO SHOTS FROM THE RIGHT; THE SPINAL LANCE TOLD 1.2 S", y=30)
    elif i == 3:
        dead = ("gen0", "gen1")
        img.alpha_composite(ascendant_layer(v, dead=dead, shield=False,
                                            panels={0: "down", 1: "cracked", 2: "heating", 3: "whole"}))
        aimed_fan(img, 240, 262, 200, 470, 9, 80, 70)
        aimed_fan(img, 240, 262, 200, 470, 9, 80, 130)
        explosion(img, 110, 60, "medium", 6)
        ship(img, 200, 470)
        rook(img, 252, 484)
        boss_bar(img, "ASCENDANT", [(0.6, True)])
        caption(img, "GENERATORS DEAD: THE CANNON'S SHIELD FALLS; GOLD FAN; A PANEL CRACKED, ONE DOWN 3 S", y=30)
    elif i == 4:
        img.alpha_composite(ascendant_layer(v, phase=2, erupt=0.5 if v == "a" else 0.6, sacs=(2, 2, 0)))
        img = img.transform(img.size, Image.AFFINE, (1, 0, 3, 0, 1, -2))
        ship(img, 240, 470)
        rook(img, 292, 484)
        boss_bar(img, "ASCENDANT", [(0.55, False)])
        caption(img, "4 S, INVULNERABLE: THE KEEL SPLITS, THE VRELL HALF ERUPTS; TRACK 19 -> 18", y=30)
        caption(img, "VORNE: \"NO! WE HAD AN AGREEMENT!\" (HIS ONLY SHOUT)", y=48, col=(255, 160, 80))
    elif i == 5:
        img.alpha_composite(ascendant_layer(v, phase=2, heart_open=True, sacs=(0, 1, 2)))
        spiral_shots(img, 240, 152, 3, 0.4, n=5, r0=40, dr=22)
        for a in range(3):
            for j in range(4):
                ang = TAU * a / 3 + 0.4 - j * 0.3
                spore(img, 240 + np.cos(ang) * (60 + 26 * j), 152 + np.sin(ang) * (60 + 26 * j) + 30)
        for k in range(4):
            put(img, Prod.get("mote-swarm", k), 330 + k * 22, 200 + k * 14)
        ship(img, 150, 470)
        rook(img, 202, 484)
        boss_bar(img, "ASCENDANT", [(0.3, True)])
        caption(img, "SACS REGENERATE; THE HEART OPENS AFTER TWO SACS; SPORE SPIRAL, A BROOD (MOTES STAND IN)", y=30)
    elif i == 6:
        img.alpha_composite(ascendant_layer(v, phase=2, heart_open=True, sacs=(0, 0, 0)))
        for k, y in enumerate((60, 110, 160, 210, 250)):
            explosion(img, 240 + (12 if k % 2 else -12), y, "large", 3 + k)
        a = arr(img)
        a[..., :3] = a[..., :3] * 0.6 + 255 * 0.4
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
        for k in range(14):
            put(img, Prod.get("pickup-salvage-medium"), 160 + (k * 37) % 180, 120 + (k * 53) % 160)
        ship(img, 240, 470)
        caption(img, "THE HEART AT 0: A CHAIN DOWN THE KEEL 3 S, THE ACT-BOSS FLASH, THE CREDIT SHOWER", y=30)
    elif i == 7 and jagged:                                  # round 73: the jagged break (round 72 A.11)
        img = wreck_break(img, v)
        for x, y in ((150, 160), (330, 170), (60, 260), (420, 250)):
            img = light(img, x, y, 10, (255, 120, 40), 0.9)
        lb = ASC[f"lifeboat-{v}"]
        put(img, lb, 250, 150)
        wake(img, 250, 150)
        ship(img, 260, 440)
        rook(img, 312, 454)
        escape_bar(img, 0.55, 0.7)
        caption(img, "14 S: THE LIFEBOAT CLIMBS AND WEAVES; HITS PAY UP TO 1,400; THE WRECK'S HALVES DRIFT APART, TORN "
                     "ALONG A JAGGED BREAK", y=30)
    elif i == 7:
        hull = ascendant_layer(v, phase=2, heart_open=False, sacs=(0, 0, 0))
        hull = darkened(hull, 0.55)
        left, right = hull.crop((0, 0, 240, FH)), hull.crop((240, 0, FW, FH))
        img.alpha_composite(left.rotate(-6, resample=Image.BICUBIC), (-70, 60))
        img.alpha_composite(right.rotate(6, resample=Image.BICUBIC), (310, 60))
        for x, y in ((150, 160), (330, 170), (60, 260), (420, 250)):
            glow_at_img = light(img, x, y, 10, (255, 120, 40), 0.9)
            img = glow_at_img
        lb = ASC[f"lifeboat-{v}"]
        put(img, lb, 250, 150)
        wake(img, 250, 150)
        ship(img, 260, 440)
        rook(img, 312, 454)
        escape_bar(img, 0.55, 0.7)
        caption(img, "14 S: THE LIFEBOAT CLIMBS AND WEAVES; HITS PAY UP TO 1,400; THE WRECK'S HALVES DRIFT APART", y=30)
    else:
        lb = ASC[f"lifeboat-{v}"]
        put(img, fade(lb, 0.5), 240, 52)
        img = light(img, 240, 46, 26, (255, 230, 170), 1.2)
        ship(img, 240, 440)
        rook(img, 292, 454)
        escape_bar(img, 0.0, 1.0, label="ESCAPED")
        caption(img, "AT THE TOP EDGE IT FLASHES ONCE TOWARD THE GATE: 'ESCAPED', THE ESCAPE STING (TRACK 33)", y=30)
    return img


def atlas_budget(v):
    """The pieces the game would pack for the boss and their share of one 2048x2048 page (2 px padding)."""
    def area(sp, n=1):
        return (sp.width + 2) * (sp.height + 2) * n
    rows = [("cannon (whole, damaged, dead)", area(ASC["cannon"], 3)),
            ("generator (lit, dim, dead)", area(ASC["generator"], 3)),
            ("battery + panel (whole, heating, cracked, down; 5 facings a state)", area(ASC["battery"]) +
             area(ASC["panel"], 4 * 5)),
            ("sacs (2 sizes x whole, healing, burst) + heart (sealed, open)", area(ASC["sac"], 6) + area(ASC["heart"], 2)),
            ("lifeboat (8 bank/weave frames)", area(ASC[f"lifeboat-{v}"], 8))]
    if v == "a":
        rows += [("hull phase 1 (one piece, 440x270)", area(ASC["hull-a"])),
                 ("hull phase 2 (one piece)", area(ASC["hull-a2"])),
                 ("eruption keel frames (2 x 220x270)", area(ASC["keel-a-0.35"], 2)),
                 ("wreck halves (phase 2 hull's two halves: no extra region)", 0)]
    else:
        rows += [("armour block (one, used 27 times) + 3 damage states", area(ASC["block"], 4)),
                 ("crown fin (one, used 10 times)", area(ASC["fin"])),
                 ("breach tissue (2 sizes, flipped and scaled in place)", area(ASC["breach"], 2)),
                 ("dark base (a tiled 32 px plate)", 34 * 34),
                 ("wreck: the blocks thrown (no extra region)", 0)]
    total = sum(a for _, a in rows)
    return rows, total, total / (2048 * 2048)


def ascendant_sheet(v):
    hull1 = ascendant_layer(v).crop((0, 0, FW, 290))
    hull2 = ascendant_layer(v, phase=2, heart_open=True).crop((0, 0, FW, 290))
    pair = panel_grid([hull1, hull2], 2, pad=12, labels=["PHASE 1: THE GOLD HULL (1X)", "PHASE 2: THE VRELL HALF (1X)"])
    part_cells, labs = [], []

    def cell(sp, lab, w=90):
        c = checker(w, w)
        put(c, sp, w / 2, w / 2)
        part_cells.append(c)
        labs.append(lab)
    cell(ASC["cannon"], "CANNON")
    c = checker(90, 90)
    put(c, ASC["cannon"], 45, 45)
    u60.shield(c, ASC["cannon"], 45, 45, mode="bubble", alpha=0.4)
    part_cells.append(c)
    labs.append("CANNON SHIELD")
    cell(ASC["generator"], "GENERATOR")
    cell(ASC["generator-dead"], "GEN DEAD")
    for st in ("whole", "heating", "cracked", "down"):
        c = checker(90, 90)
        put(c, ASC["battery"], 45, 40)
        put(c, panel_state(ASC["panel"], st), 45, 52)
        part_cells.append(c)
        labs.append(f"MIRROR {st.upper()}")
    cell(ASC["sac"], "SAC")
    cell(ASC["sac-small"], "SAC HEALING")
    cell(ASC["heart"], "HEART SEALED")
    cell(ASC["heart-open"], "HEART OPEN")
    cell(ASC[f"lifeboat-{v}"], "LIFEBOAT")
    if v == "b":
        cell(ASC["block"], "ARMOUR BLOCK")
        cell(ASC["fin"], "CROWN FIN")
        cell(ASC["breach"], "BREACH")
    parts = panel_grid([p.resize((90, 90)) for p in part_cells], 10, pad=6, labels=labs)
    big = panel_grid([sprite.enlarge(ASC[f"lifeboat-{v}"], 3), sprite.enlarge(ASC["heart-open"], 2),
                      sprite.enlarge(ASC["sac"], 3)], 3, pad=12, labels=["LIFEBOAT 3X", "HEART 2X", "SAC 3X"])
    erupt = [ascendant_layer(v, phase=2, erupt=e, sacs=(2, 2, 0) if e < 1 else (1, 1, 1)).crop((60, 0, 420, 290))
             .resize((240, 193), Image.LANCZOS) for e in (0.3, 0.6, 1.0)]
    wreck = ascendant_layer(v, phase=2, sacs=(0, 0, 0))
    wreck = darkened(wreck, 0.55)
    wl = Image.new("RGBA", (FW, 290), (0, 0, 0, 0))
    wl.alpha_composite(wreck.crop((0, 0, 240, 290)).rotate(-6, resample=Image.BICUBIC), (-30, 10))
    wl.alpha_composite(wreck.crop((240, 0, FW, 290)).rotate(6, resample=Image.BICUBIC), (270, 10))
    erupt.append(wl.resize((240, 145), Image.LANCZOS))
    erow = panel_grid(erupt, 4, pad=8, labels=["ERUPTION 1", "ERUPTION 2", "ERUPTION END", "THE WRECK'S HALVES"])
    field = l42()
    field = gold_beams(field)
    field.alpha_composite(ascendant_layer(v))
    ship(field, 330, 470)
    rook(field, 382, 484)
    boss_bar(field, "ASCENDANT", [(1.0, True)])
    field2 = l42()
    field2.alpha_composite(ascendant_layer(v, phase=2, heart_open=True))
    for j in range(5):
        spore(field2, 140 + j * 50, 330 + (j % 2) * 20)
        orb(field2, 160 + j * 50, 380)
    ship(field2, 240, 470)
    fields = panel_grid([field, field2], 2, pad=12, labels=["PHASE 1 IN LEVEL 42 (1X)", "PHASE 2: SPORES (LIME) VS "
                                                                                        "BOUNCED SHOTS (MAGENTA)"])
    rows, total, share = atlas_budget(v)
    gs1 = u60.gold_share(hull1)
    name = {"a": "A - THE GILDED WEDGE", "b": "B - THE ARMOURED TIERS"}[v]
    notes = [f"ASCENDANT {name} (Q6 A, BOW-ON OVER A HALTED ARENA; THE ELITE LIVERY OF Q9 A). PARTS AT THE ARENA "
             f"TABLE'S PLACES: GENERATORS (110,60) (370,60), MIRROR BATTERIES (150,130) (185,190) (330,130) (295,190), "
             f"THE BOW CANNON (240,235).",
             f"GOLD SHARE OF THE PHASE-1 SILHOUETTE: {gs1 * 100:.0f} % (THE BRIEF ASKS 35-45 %).",
             ("A: ONE SMOOTH GLOSS-BLACK WEDGE, GOLD RIBS FANNING DOWN BOTH FACES, A GOLD SPINE AND EDGE, THE CREST ON "
              "THE BOW; THE VRELL HALF ERUPTS ALONG THE KEEL (THE PLATES PEEL OUTWARD) AND THROUGH TWO BREACHES. THE "
              "LIFEBOAT: A ROUND SEED-POD, THE GOLD CAPSULE HALF SWALLOWED, ITS CANOPY THE ONE WARM SPOT."
              if v == "a" else
              "B: THE BOW STEPPED IN SIX TIERS OF ONE ARMOUR BLOCK (27 TIMES) WITH DARK GAPS AND GOLD CROWN FINS AT THE "
              "STEPS; THE VRELL HALF BURSTS THROUGH THE GAPS (BLOCKS THROWN ASIDE, SIX BREACHES, ASYMMETRIC). THE "
              "LIFEBOAT: A LONG THORNED POD, THE CAPSULE'S CANOPY AT ITS HEAD, TENDRILS TRAILING (ACT 7'S CHIMERA SEED)."),
             f"ATLAS BUDGET (ONE 2048X2048 PAGE): {total:,} PX = {share * 100:.1f} % OF A PAGE. " +
             "; ".join(f"{n}: {a:,}" for n, a in rows) + ".",
             "THE MIRROR PANELS TURN IN 2D (THE HULL IS FIXED, BOW DOWN); THE BOUNCED BULLETS ARE THE PLAYER'S MAGENTA, "
             "THE VRELL SPORES LIME WITH A DARK RIM.",
             "WORRIES: " + ("THE ONE-PIECE HULL COSTS THE MOST PAGE (STILL WELL UNDER ONE PAGE); THE ERUPTION NEEDS ITS OWN "
                            "KEEL FRAMES. " if v == "a" else
                            "THE STEPPED SILHOUETTE READS MORE 'BUILT' THAN 'SHIP'; THE BREACHES ARE ONE SPRITE FLIPPED AND "
                            "SCALED, SO THE ERUPTION IS LESS ORGANIC. ") +
             "THE HULL'S BLACK OVER BLACK SPACE LEANS ON ITS GOLD AND RIM; CALLISTO'S CURVE BEHIND THE SHIP KEEPS THE "
             "LOWER FIELD READABLE."]
    img = compose(f"ASCENDANT {name}", [("THE HULL", pair), ("PARTS AND STATES (0.75X CELLS)", parts),
                                       ("ZOOMS", big), ("THE ERUPTION AND THE WRECK (0.67X)", erow),
                                       ("READABILITY OVER LEVEL 42", fields)], notes)
    save_png(img, DESIGN / "enemies" / "bosses" / "ascendant" / "concept" / f"ascendant-r72-{v}.png")


def ascendant_storyboard(v, rnd=72):
    """The phase storyboard; ``rnd`` 73 is round 73's: frame 8's wreck torn along a jagged break (round 72 A.11)."""
    cells = [field_cell(asc_panel(v, i, jagged=rnd >= 73), scale=0.5) for i in range(len(ASC_PANELS))]
    board = panel_grid(cells, 3, pad=8, labels=[f"{i + 1}. {ASC_PANELS[i]}" for i in range(len(cells))])
    name = {"a": "A - THE GILDED WEDGE", "b": "B - THE ARMOURED TIERS"}[v]
    notes = [f"THE ASCENDANT'S PHASE STORYBOARD, {name}: THE WARNING BEFORE THE HULL SHOWS, PHASE 1'S PLAYBOOK (SPINAL "
             "LANCE, GOLD FAN, MISSILE SALVO, DART STREAM), THE ERUPTION, PHASE 2'S VRELL PLAYBOOK (CHIMERA BROOD, SPORE "
             "SPIRAL, BILE LOBS, GRAFTED GUNS), K, THE ESCAPE AND ESCAPED.",
             "THE BOSS BAR SHOWS THE HULL'S TOTAL HP; THE LIFEBOAT GETS THE ESCAPE BAR 'VORNE' (AMBER: THE TIME LEFT; "
             "GOLD: THE SHARE OF THE 1,400 CAP; 'MAX' AT THE CAP).",
             "PANEL 6 USES THE MOTE SWARM AS A STAND-IN FOR THE CHIMERA BROOD (ITS ART IS ART-R-A'S)."]
    if rnd >= 73:
        notes.append("ROUND 73 (ROUND 72 A.11): IN PANEL 8 THE HULL BREAKS ALONG A JAGGED FRACTURE DOWN THE KEEL - A RAGGED "
                     "EDGE, TORN PLATE FLAPS BENT OUTWARD ON BOTH HALVES, A CHARRED AND HOT-GLOWING RIM, SHARDS IN THE GAP "
                     "AND GAS VENTING FROM BOTH EDGES (ORANGE FROM THE PLATING, TEAL FROM THE TISSUE) - NEVER A CLEAN CUT.")
    img = compose(f"ASCENDANT - PHASES {name}", [("STORYBOARD (0.5X)", board)], notes, width=800,
                  sub=SUB.replace("72", str(rnd)))
    save_png(img, DESIGN / "enemies" / "bosses" / "ascendant" / "concept" / f"ascendant-phases-r{rnd}-{v}.png")


def make_ascendant(variants):
    load_ascendant(variants)
    for v in variants:
        ascendant_sheet(v)
        ascendant_storyboard(v)



# =========================================================================== 3 Jupiter's moods per level (Q10) and the banks (Q2)

def unit_set():
    """The readability set: the ship, Rook, an elite ace (a), a gunmetal Talon (production), the bullets."""
    load_aces(["a"])
    return {"ace": ace("a", "harker", DOWN), "talon": Prod.get("talon", 12)}


def overlay(crop, units):
    c = crop.copy()
    sniper_line(c, 0, 6, c.width, 26, 1.0)
    spore(c, 18, 16, 5)
    put(c, units["ace"], 112, 44)
    put(c, units["talon"], 166, 44)
    ship(c, 26, 58)
    rook(c, 66, 64)
    needle(c, 92, 82, DOWN)
    needle(c, 104, 86, DOWN + 0.3)
    orb(c, 142, 84)
    orb(c, 186, 80, big=True)
    return c


def mood_rgb(rgb, v):
    return rgb if v == "a" else grade_dusk(rgb)


def make_moods(variants):
    units = unit_set()
    base = mood_list()
    for v in variants:
        cells, labs, pairs, plabs = [], [], [], []
        for key, title, rgb in base:
            img = to_img(mood_rgb(rgb, v))
            if key == "L38":
                img = lightning_column(img, 320, 0.8)
            L = lum(img)
            bb, bl = window(img, darkest=False)
            db, dl = window(img, darkest=True)
            cell = img.copy()
            d = ImageDraw.Draw(cell)
            d.rectangle(bb, outline=(255, 255, 255, 255), width=2)
            d.rectangle(db, outline=(80, 200, 255, 255), width=2)
            cells.append(cell.resize((240, 270), Image.LANCZOS))
            labs.append(f"{key} {title[:22]}  MEAN {L.mean():.0f}")
            pairs.append(panel_grid([overlay(img.crop(bb), units), overlay(img.crop(db), units)], 2, pad=4))
            plabs.append(f"{key}: BRIGHTEST {bl:.0f} (WHITE BOX) | DARKEST {dl:.0f} (BLUE BOX)")
        ladder = panel_grid(cells, 4, labels=labs)
        strip = panel_grid(pairs, 2, pad=16, labels=plabs)
        name = {"a": "A - A GRADE PER LEVEL (Q10 A)", "b": "B - ONE STORM-DUSK GRADE (Q10 B)"}[v]
        notes = [f"JUPITER'S MOODS L36-L42 {name}. MEAN = THE BACKDROP'S MEAN LUMINANCE (0-255); THE WINDOWS ARE ITS "
                 "BRIGHTEST AND DARKEST 200X96 AT 1X, WITH THE READABILITY SET LAID OVER THEM: A RED SNIPER LINE, A VRELL "
                 "SPORE, AN ELITE ACE (GLOSS BLACK AND GOLD), A GUNMETAL TALON, THE SHIP, ROOK, GOLD NEEDLES, MAGENTA ORBS.",
                 "L36 HIGH SUNLIT BANDS; L37 AURELIA'S GOLD DECKS (A STEP PALER THAN THE UNITS' GOLD) THEN THE TURN: THE "
                 "SKY DARKENS, FIRE AND VRELL TEAL ON THE DECKS; L38 STORM-DARK SLATE, A LIGHTNING COLUMN'S BLUE-WHITE "
                 "TELEGRAPH (NEVER THE SNIPER'S RED); L39 IO'S DESATURATED SULPHUR, BLACK LAVA, FROST, VENT GLOW AND THE "
                 "REFINERY LINES; L40 BLACK SPACE, CALLISTO'S DISC, JUPITER'S LIMB; L41 THE BLACK CITADEL ON BLUE-GREY "
                 "ICE, THE LOW SUN'S LONG SHADOWS; L42 CALLISTO ORBIT, THE HOME FLEET'S CRUISERS FAR OFF.",
                 ("A: EACH LEVEL ITS OWN GRADE (THE JOVIAN README'S TABLE). THE BRIGHT CREAM OF L36/L37 IS THE RISK: THE "
                  "GOLD NEEDLES AND THE GUNMETAL TALON KEEP THEIR DARK OUTLINE THERE; THE ACE READS BY ITS BLACK HULL."
                  if v == "a" else
                  "B: THE SAME SCENES UNDER ONE STORM-DUSK GRADE (DARKER, VIOLET SHADOWS, AMBER HIGHS): SAFER OVER CREAM, "
                  "BUT THE ACT LOSES ITS SUNLIT START AND THE SPACE LEVELS GO MUDDY; THE BLACK UNITS SINK IN L38 AND L41."),
                 "EVERY CLOUD, HAZE AND GLOW IS A SMOOTH FLOAT GRADIENT, DRAWN ONCE (NO ORDERED DITHER, NO POSTERISING)."]
        img = compose(f"JUPITER'S MOODS {name}", [("THE LADDER (0.5X)", ladder),
                                                  ("READABILITY (1X)", strip)], notes)
        save_png(img, DESIGN / "world" / "jovian" / "concept" / f"jovian-moods-r72-{v}.png")


def bank_field(seed=3611):
    """A foreground bank's alpha over the field: billows in the upper and middle field, never resting on the ship's
    band (the bottom 140 px), about a quarter of the area."""
    shade, a = billows(FH, FW, seed, 3, y_range=(0.25, 0.5), size=(18, 40), spread=(70, 34))
    a = a * smoothstep(FH - 140, FH - 200, YY)
    return shade, a


def bank_colour(shade, v):
    if v == "a":
        tones = [(0.0, (196, 160, 120)), (0.5, (236, 214, 178)), (1.0, (255, 248, 230))]
    else:
        tones = [(0.0, (70, 62, 62)), (0.5, (118, 106, 96)), (1.0, (168, 152, 132))]
    return raster.ramp(tones, shade)


def make_banks(variants):
    units = unit_set()
    bg = sky_l36()
    shade, a = bank_field()
    share = float((a > 0.5).mean())
    band = float((a[FH - 140:] > 0.5).mean())
    under = [("talon", 120, 210, DOWN), ("talon", 175, 230, DOWN), ("talon", 230, 210, DOWN),
             ("ace", 330, 260, DOWN), ("mote", 400, 190, 0)]
    for v in variants:
        def frame(with_bank=True):
            img = to_img(bg)
            for kind, x, y, h in under:
                sp = units["talon"] if kind == "talon" else (units["ace"] if kind == "ace" else Prod.get("mote-swarm"))
                put(img, sp, x, y)
            if with_bank:
                rgb = arr(img)[..., :3]
                k = (0.65 * a)[..., None]
                rgb = rgb * (1 - k) + bank_colour(shade, v) * k
                img = to_img(rgb)
            for kind, x, y, h in under:                               # running lights and tells at full
                if kind == "mote":
                    img = light(img, x, y, 5, (120, 255, 160), 0.8)
                    continue
                for dx in (-14, 14):
                    img = light(img, x + dx, y - 4, 2.2, (255, 50, 40), 1.4)
            img = light(img, 175, 246, 4, (255, 70, 40), 1.4)        # a Talon's muzzle tell under the bank
            for j in range(4):
                needle(img, 175 + (j - 1.5) * 10, 270 + j * 26, DOWN)
            orb(img, 300, 300)
            orb(img, 330, 320, big=True)
            put(img, Prod.get("pickup-crate"), 290, 380)
            ship(img, 240, 470)
            rook(img, 292, 484)
            return img
        f = frame(True)
        bare = frame(False)
        crop = (90, 170, 290, 290)
        zoom = panel_grid([f.crop(crop).resize((400, 240), Image.NEAREST),
                           bare.crop(crop).resize((400, 240), Image.NEAREST)], 2, pad=12,
                          labels=["UNDER THE BANK, 2X", "THE SAME WITHOUT THE BANK, 2X"])
        name = {"a": "A - CREAM VEIL", "b": "B - SHADOWED UNDERSIDE"}[v]
        notes = [f"Q2 A'S FOREGROUND BANKS OVER LEVEL 36'S BRIGHTEST CREAM BAND, {name}.",
                 "THE BANK SITS ON HIGH-AIR, DRAWN OVER AIR BODIES: A BODY UNDER IT SHOWS AT ABOUT 35 % (THE BANK 65 %), "
                 "ITS RUNNING LIGHTS, ENGINE GLOW AND ATTACK TELLS AT FULL. THE BULLETS, THE SHIP, ROOK AND PICKUPS ARE "
                 "ALWAYS ABOVE IT.",
                 f"MEASURED: {share * 100:.0f} % OF THE FIELD UNDER BANKS (THE RULE: AT MOST A THIRD), "
                 f"{band * 100:.0f} % OF THE SHIP'S BAND (THE BOTTOM 140 PX; THE RULE: NEVER RESTING THERE OVER 2 S).",
                 ("A: THE BANK IN THE BAND'S OWN LIT CREAM - BODIES FADE TOWARD CREAM, THE RED LIGHTS AND THE GOLD NEEDLES "
                  "POP; THE DARK GUNMETAL READS AS A SOFT GREY SHAPE. CLOSEST TO THE CHOSEN R06 SCENE."
                  if v == "a" else
                  "B: THE BANK'S UNDERSIDE IN A COOL GREY-OCHRE SHADE - BODIES SINK AS DARK SHAPES, THE LIGHTS GLOW HARDER; "
                  "THE BANK READS AS A DARK BLOT ON THE BRIGHT BAND, AND THE SHIP'S BULLETS STAY BRIGHT OVER IT."),
                 "STAND-INS: THE MOTE SWARM FOR A CHIMERA-SIZED BODY (ITS ART IS ART-R-A'S), PRODUCTION TALONS AND "
                 "BULLETS, A HARKER IN THE ELITE LIVERY."]
        img = compose(f"LEVEL 36 FOREGROUND BANKS {name}", [("THE PLAY FIELD (1X)", panel_grid([f], 1)),
                                                          ("ZOOM", zoom)], notes, width=860)
        save_png(img, DESIGN / "world" / "jovian" / "concept" / f"jovian-banks-r72-{v}.png")



# =========================================================================== 4 the falling city (Q4, Level 37's last 40 s)

def sky_after(seed=3701, depth=0.0):
    """The after-turn sky: the bands darkened a step; ``depth`` 0..1 shifts and darkens them as the ship descends."""
    v = bands(FH + 200, FW, seed, scale=46, warp_k=2.0)
    off = int(depth * 200)
    v = v[200 - off:200 - off + FH]
    rgb = raster.ramp(JOV_SUN, 0.35 + 0.55 * v) * 0.86 + np.array((240, 220, 190)) * 0.14
    k = 1.0 - 0.25 * depth
    return rgb * np.array((0.62, 0.52, 0.5)) * k + np.array((30, 10, 16))


def city_burning(seed=3701):
    """Aurelia after the turn as its own layer: Vrell growth and fire on the decks only (the alpha is the city's)."""
    city = city_layer(balloons=[])
    a = arr(city)
    rgb, cm = a[..., :3], a[..., 3] / 255
    growth = raster.fbm(FW, FH, 40, seed + 3, octaves=4, period=False)
    g = smoothstep(0.55, 0.72, growth) * cm
    seam = np.exp(-((growth - 0.62) / 0.012) ** 2) * cm
    rgb = rgb * (1 - g[..., None]) + np.array((22, 70, 66)) * g[..., None]
    rgb += seam[..., None] * np.array((0, 220, 140)) * 0.7
    rng = np.random.default_rng(seed + 9)
    for _ in range(12):
        x, y = rng.uniform(40, 440), rng.uniform(60, 500)
        if cm[int(y), int(x)] > 0.5:
            glow_at(rgb, x, y, rng.uniform(8, 16), (255, 120, 30), 0.9)
            glow_at(rgb, x, y, 4, (255, 220, 140), 0.8)
    return Image.fromarray(np.clip(np.dstack([rgb, a[..., 3]]), 0, 255).astype(np.uint8), "RGBA")


def affine_about(img, scale_x, scale_y, rot_deg, cx, cy, dx=0.0, dy=0.0):
    """img scaled and turned about (cx, cy), then moved by (dx, dy), on a same-size canvas (bicubic, smooth)."""
    a = np.radians(rot_deg)
    c, s_ = np.cos(a), np.sin(a)
    # inverse map: out (x, y) -> in
    m = np.array([[c / scale_x, s_ / scale_x], [-s_ / scale_y, c / scale_y]])
    ox, oy = cx + dx, cy + dy
    a0, b0 = m[0]
    d0, e0 = m[1]
    cc = cx - a0 * ox - b0 * oy
    ff = cy - d0 * ox - e0 * oy
    return img.transform(img.size, Image.AFFINE, (a0, b0, cc, d0, e0, ff), resample=Image.BICUBIC)


def sink_tone(layer, k):
    """A sinking piece: darker and hazier (toward the deep cloud's rust) as ``k`` goes 0 -> 1."""
    a = arr(layer)
    a[..., :3] = a[..., :3] * (1 - 0.75 * k) + np.array((70, 40, 36)) * 0.45 * k
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


FAULT = [(60, 0), (150, 140), (210, 230), (190, 330), (280, 430), (330, 540)]


def split_masks():
    m = Image.new("L", (FW, FH), 0)
    ImageDraw.Draw(m).polygon(FAULT + [(0, FH), (0, 0)], fill=255)
    left = np.array(m) > 0
    return left, ~left


CITY_PANELS = [(-40, "THE BALLOONS TEAR"), (-36, "THE DECK DROPS"),
               (-32, "SLOWER, DARKER, SMALLER"), (-26, "DOWN BESIDE THE SPIRES"),
               (-20, "THE CITY FAR BELOW"), (-14, "THE LAST WAVE"), (-6, "LAST KILL, OKAFOR"),
               (0, "OPEN CLOUD, THE END")]


def fall_k(t):
    """0 at t=-36 (the drop starts) -> 1 at t=-16 (20 s of fall)."""
    return float(np.clip((t + 36) / 20.0, 0, 1))


CITY_BREAK = {}


def city_break(city, seed=7311):
    """Round 73's jagged fault for b (round 72 A.11): the deck's halves cut along a ragged line through FAULT, each
    with a torn edge, and the points where the break crosses the city (where shards fly and the decks vent)."""
    if "L" not in CITY_BREAK:
        line = ragged_line([(FAULT[0][0] - 5, -10)] + FAULT + [(FAULT[-1][0] + 5, FH + 10)], seed, amp=6.0)
        lm, rm = ragged_masks(city.size, line)
        kw = dict(band=4, rim=(232, 214, 176), plate=(126, 118, 104), lip=(236, 220, 186), size=(4, 8), every=(3, 6))
        CITY_BREAK["L"] = torn_half(city, lm, line, seed, -1, **kw)
        CITY_BREAK["R"] = torn_half(city, rm, line, seed + 50, 1, **kw)
        solid = _grow(arr(city)[..., 3] > 40, 3) <= 3
        ok = [(x, y) for x, y in line if 0 <= int(x) < FW and 0 <= int(y) < FH and solid[int(y), int(x)]]
        CITY_BREAK["cross"] = np.array(ok)
    return CITY_BREAK["L"], CITY_BREAK["R"], CITY_BREAK["cross"]


def break_effects(img, t, k, vis, moves, cross, seed=7313):
    """The break as the halves part: dark smoke and fire venting from both torn edges, shards in the gap (smooth
    glows and puffs; the shards sink and darken with the halves)."""
    if not len(cross) or vis <= 0:
        return img
    rng = np.random.default_rng(seed + int(t))
    ends = [moved_points(cross, *m) for m in moves]
    for e in ends:
        for j in rng.choice(len(e), min(len(e), 5), replace=False):
            x, y = e[j]
            img = smoke_puff(img, x + rng.normal(0, 3), y - 4 - rng.uniform(0, 8), 7 + 8 * rng.random(),
                             k=0.5 * vis * (1 - 0.5 * k))
            img = light(img, x, y, 3 + 2 * rng.random(), (255, 120, 40), 0.9 * vis * (1 - 0.6 * k))
    mid = (ends[0] + ends[1]) / 2
    tone = 1 - 0.6 * k
    shards(img, mid, seed + int(t) * 7, int(10 + 8 * k), size=(2 + 3 * (1 - k), 4 + 5 * (1 - k)),
           plate=tuple(int(c * tone) for c in (126, 118, 104)), rim=tuple(int(c * tone) for c in (236, 220, 186)),
           gold=tuple(int(c * tone) for c in (220, 170, 80)), spread=(6 + 26 * k, 6 + 18 * k))
    return img


def city_frame(v, t, city, spire, balloon, jagged=False):
    k = fall_k(t)
    depth = float(np.clip((t + 40) / 40.0, 0, 1))
    img = to_img(sky_after(depth=depth))
    scale = 1.0 - 0.58 * (k ** 0.8)
    if v == "a":
        pieces = [(city, scale * (1 - 0.18 * k), scale, 7 * k, 0, 18 * k)]
    elif jagged and t < -36:                                          # round 73: whole until the deck drops
        pieces = [(city, scale, scale, 0, 0, 0)]
    else:
        if jagged:
            L, R, cross = city_break(city)
        else:
            left, right = split_masks()
            ca = arr(city)
            la, ra = ca.copy(), ca.copy()
            la[~left, 3] = 0
            ra[~right, 3] = 0
            L = Image.fromarray(la.astype(np.uint8), "RGBA")
            R = Image.fromarray(ra.astype(np.uint8), "RGBA")
        kb = float(np.clip((t + 36) / 22.0, 0, 1))
        pieces = [(L, scale * (1 - 0.1 * k), scale, -11 * k, -60 * k, 10 * k),
                  (R, scale * 0.94 ** (1 + 3 * kb), scale * 0.94 ** (1 + 3 * kb), 9 * k, 50 * k, 30 * k)]
    for lay, sx_, sy_, rot, dx, dy in pieces:
        moved = affine_about(lay, sx_, sy_, rot, 240, 270, dx, dy)
        if k < 0.35:
            rgb = shadow_rgb(arr(img)[..., :3], moved, int(26 * (1 - k)), int(34 * (1 - k)), 0.42 * (1 - 2 * k))
            img = to_img(rgb)
        vis = float(np.clip((-4 - t) / 12.0, 0, 1)) if t > -16 else 1.0
        img.alpha_composite(fade(sink_tone(moved, k), vis))
    if v == "b" and jagged and t >= -36:
        vis = float(np.clip((-4 - t) / 12.0, 0, 1)) if t > -16 else 1.0
        moves = [(sx_, sy_, rot, 240, 270, dx, dy) for _, sx_, sy_, rot, dx, dy in pieces]
        img = break_effects(img, t, k, vis, moves, cross)
    if v == "b" and -16 >= t > -36:                                    # spires toppling on the halves
        for x, y in ((420, 340), (70, 220)):
            sp = spire.rotate(70 * k, resample=Image.BICUBIC, expand=True)
            sp = sp.resize((max(1, int(sp.width * scale)), max(1, int(sp.height * scale))), Image.BICUBIC)
            put(img, sink_tone(sp, k), 240 + (x - 240) * scale + (-60 if x < 240 else 50) * k,
                270 + (y - 270) * scale + 20 * k)
    if t <= -38:                                              # the balloons tear: squashed, darkened, tatters
        for j, (x, y) in enumerate(BALLOONS):
            b = balloon.resize((balloon.width, int(balloon.height * (0.7 - 0.1 * j % 2))), Image.BICUBIC)
            put(img, tinted(b, (120, 70, 50), 0.35), x, y + 8)
            d = ImageDraw.Draw(img)
            for q in range(4):
                d.line([x - 10 + q * 7, y + 18, x - 14 + q * 8, y + 34], fill=(200, 180, 150, 200), width=1)
    elif t < -30:
        for j, (x, y) in enumerate(BALLOONS):
            b = balloon.resize((int(balloon.width * 0.8), int(balloon.height * 0.35)), Image.BICUBIC)
            put(img, sink_tone(tinted(b, (90, 60, 50), 0.5), k), 240 + (x - 240) * scale, 270 + (y - 270) * scale + 30)
    rgb = arr(img)[..., :3]
    if -36 <= t <= -10:                                       # smoke from the falling decks, smooth
        n = raster.fbm(FW, FH, 50, 3777 + int(t), octaves=4, period=False)
        col = np.exp(-((XX - 240) / (90 + 60 * k)) ** 2) * smoothstep(0.35, 0.8, n) * 0.55 * (1 - 0.5 * k)
        rgb = rgb * (1 - col[..., None]) + np.array((44, 36, 36)) * col[..., None]
    img = to_img(rgb)
    if -30 <= t <= -18:                                       # the spires pass the ship on low-air (x1.35)
        for x, y0 in ((60, 120), (430, 300)):
            y = (y0 + (t + 30) * 60) % 700 - 80
            big = spire.resize((int(spire.width * 2.2), int(spire.height * 2.2)), Image.BICUBIC)
            put(img, sink_tone(big, 0.2), x, y)
    if -34 <= t <= -10:                                       # low-air debris
        rng = np.random.default_rng(int(t) + 99)
        d = ImageDraw.Draw(img)
        for _ in range(10):
            x, y = rng.uniform(0, FW), rng.uniform(0, FH)
            w = rng.uniform(4, 12)
            d.polygon([(x, y), (x + w, y + w * 0.3), (x + w * 0.6, y + w), (x - w * 0.2, y + w * 0.6)],
                      fill=(40, 30, 28, 255), outline=(200, 150, 70, 255))
    if t in (-26, -20, -14):
        for j in range(5):
            put(img, Prod.get("mote-swarm", j), 100 + j * 60, 160 + (j % 2) * 40)
            spore(img, 110 + j * 60, 210 + (j % 2) * 40, 4)
    if t == -14:
        for j in range(3):
            put(img, Prod.get("talon", 12), 300 + j * 40, 120 + j * 20)
    if t == -6:
        explosion(img, 330, 160, "medium", 5)
    sx0 = 240 if t >= -30 else 200
    ship(img, sx0, 450 if -30 <= t <= -18 else 470)
    rook(img, sx0 + 52, (450 if -30 <= t <= -18 else 470) + 14)
    if t == -6:
        caption(img, "OKAFOR: HER AURELIA LINE OVER THE FALL (THE LEVEL DOCUMENT'S)", y=30, col=(120, 220, 255))
    if t == 0:
        caption(img, "AT LEAST 4 S OF CALM AFTER THE LAST ENEMY; THE CITY GONE INTO THE CLOUD", y=30)
    return img


def city_strip():
    """The 40 s as a bar: the turn's end, the tear, the 20 s fall, the last wave, the calm."""
    img = Image.new("RGBA", (984, 46), raster.SHEET_BG)
    d = ImageDraw.Draw(img)
    x0, k = 20, 940 / 40.0
    spans = [(-40, -36, (200, 120, 60), "TEAR"), (-36, -16, (150, 70, 50), "THE FALL: 20 S (SLOWER, DARKER, SMALLER)"),
             (-16, -4, (90, 70, 90), "LAST WAVES, VRELL"), (-4, 0, (40, 90, 80), "CALM")]
    for a, b, col, lab in spans:
        d.rectangle([x0 + (a + 40) * k, 8, x0 + (b + 40) * k, 26], fill=col + (255,), outline=(20, 20, 24, 255))
        text(img, x0 + (a + 40) * k + 4, 14, lab, (255, 240, 220))
    for t, _ in CITY_PANELS:
        x = x0 + (t + 40) * k
        d.line([x, 4, x, 30], fill=(255, 214, 120, 255))
        text(img, x - 8, 32, f"{t}S", raster.LABEL_DIM)
    return img


def make_city(variants, rnd=72):
    """The falling-city storyboards; ``rnd`` 73 is round 73's b with the jagged break (round 72 A.11)."""
    c = city_sprites()
    city = city_burning()
    for v in variants:
        cells = [field_cell(city_frame(v, t, city, c["spire"], c["balloon"], jagged=rnd >= 73))
                 for t, _ in CITY_PANELS]
        board = panel_grid(cells, 4, pad=8, labels=[f"{i + 1}. {t} S: {n}" for i, (t, n) in enumerate(CITY_PANELS)])
        name = {"a": "A - THE SLOW TILT", "b": "B - BREAKING UP"}[v]
        notes = [f"THE FALLING CITY (Q4 A, LEVEL 37'S LAST 40 S) {name}. TIMES FROM THE LEVEL'S END.",
                 "THE BALLOONS TEAR AND COLLAPSE (NO POP); THE MAIN DECK TILTS AND DROPS; OVER 20 S THE GROUND LAYER SLOWS, "
                 "DARKENS AND SHRINKS TOWARD THE CENTRE (SCALE 1.0 -> 0.42, HAZING TO THE DEEP CLOUD'S RUST) WHILE THE "
                 "SHIP FLIES DOWN BESIDE THE SPIRES (LOW-AIR, X1.35) THROUGH DEBRIS AND VRELL; THE LEVEL ENDS OVER OPEN "
                 "CLOUD. GROUND UNITS ON THE DECKS STOP FIRING AS THE FALL STARTS AND LEAVE PLAY UNPAID.",
                 ("A: THE CITY GOES DOWN IN ONE PIECE, TILTING SLOWLY (THE DECK FORESHORTENS AS IT TURNS AWAY): ONE "
                  "TRANSFORMED LAYER, THE CHEAPEST RENDERER SET PIECE."
                  if v == "a" else
                  "B: THE DECK BREAKS ALONG A FAULT LINE; THE TWO HALVES DRIFT APART, TURN OPPOSITE WAYS AND SINK AT THEIR "
                  "OWN RATES, THE SPIRES TOPPLE: MORE DRAMATIC, TWO LAYERS AND A CUT LINE TO AUTHOR (THE PRODUCTION "
                  "BACKDROP PART GROWS)."),
                 "WORRIES: THE SHRINKING GROUND LAYER IS AN ENGINE FEATURE (THE GAP ANALYSIS'S 2.10 RISK); THE SMOKE "
                 "AND THE DARKENING ARE SMOOTH LAYERS DRAWN ONCE. THE MOTE SWARM STANDS IN FOR THE VRELL ATTACKERS."]
        if rnd >= 73:
            notes.append("ROUND 73 (ROUND 72 A.11): THE DECK BREAKS ALONG A JAGGED FAULT - A RAGGED EDGE, TORN PLATE FLAPS "
                         "BENT OUTWARD ON BOTH HALVES, A CHARRED AND HOT-GLOWING RIM, SHARDS FLUNG INTO THE WIDENING GAP "
                         "AND SMOKE AND FIRE VENTING FROM BOTH EDGES - NEVER A CLEAN CUT.")
        img = compose(f"THE FALLING CITY {name}", [("STORYBOARD (0.5X)", board), ("THE LAST 40 S", city_strip())],
                      notes, sub=SUB.replace("72", str(rnd)))
        save_png(img, DESIGN / "world" / "jovian" / "concept" / f"falling-city-r{rnd}-{v}.png")



# =========================================================================== 5 the act's title-card still

SW, SH = 960, 540
SYY, SXX = np.mgrid[0:SH, 0:SW].astype(np.float64)


def _a5():
    import act5_still_r63 as a5  # noqa: E402  (posed, strip, rx/ry/rz: imported unchanged)
    return a5


def carrier_job(size, yaw, pitch):
    a5 = _a5()
    import outro_l14_images as o14  # noqa: E402  (the CDF heavy transport, standing in for the carrier group's hulls)
    scene, mats = o14.transport_scene()
    rot = a5.rx(pitch * a5.DEG) @ a5.rz(yaw * a5.DEG)
    sc, mm = a5.posed(scene, mats, rot, size / 270 / a5.PPU)
    return sc, mm, (int(size * 1.1), int(size * 0.8), 3, 70), dict(key_pos=(-2.4, 2.0, 1.6), ambient=0.2, fill=0.25)


def ace_still_job(size, yaw, roll):
    a5 = _a5()
    scene, mats = ace_model("a", "harker")
    sc, mm = a5.posed(scene, mats, a5.talon_rot(yaw, roll), size / 2.1 / a5.PPU)
    return sc, mm, (size + 8, size + 8, 4, 80), dict(key_pos=(-3.4, -0.2, 1.4), exposure=1.05)


def deck_job(scale, tilt):
    a5 = _a5()
    scene, mats, (w, h) = sm.aurelia_deck(78)
    sc, mm = a5.posed(scene, mats, a5.rx(-tilt * a5.DEG), scale / a5.PPU)
    return sc, mm, (int(w * scale) + 20, int(h * scale * 0.75) + 40, 3, 70), dict(key_pos=(-3.0, 0.4, 1.4),
                                                                                    ambient=0.26, z_top=4.0)


def balloon_job(scale, tilt):
    a5 = _a5()
    scene, mats, (w, h) = sm.aurelia_balloon()
    sc, mm = a5.posed(scene, mats, a5.rx(-tilt * a5.DEG), scale / a5.PPU)
    return sc, mm, (int(w * scale) + 16, int(h * scale) + 16, 3, 70), dict(key_pos=(-3.0, 0.4, 1.4), ambient=0.3)


def _still_strip(job):
    a5 = _a5()
    key, builder, args, i, n = job
    return key, i, a5.strip(builder, args, i, n)


def still_models(ex, jobs, n=6):
    out = {}
    tasks = [(key, b, args, i, n) for key, b, args in jobs for i in range(n)]
    parts = {}
    for key, i, im in ex.map(_still_strip, tasks):
        parts.setdefault(key, {})[i] = im
    for key, d in parts.items():
        ims = [d[i] for i in range(n)]
        img = Image.new("RGBA", (ims[0].width, sum(im.height for im in ims)), (0, 0, 0, 0))
        y = 0
        for im in ims:
            img.alpha_composite(im, (0, y))
            y += im.height
        out[key] = img
    return out


def planet(rgb, cx, cy, r, sun, seed, tilt=0.12, spot=None, band_scale=1.0):
    """Jupiter's disc: bands by latitude on the sphere, lit from ``sun`` (a unit vector), the limb's haze; the Great
    Red Spot at (lat, lon) when given."""
    u, v = (SXX - cx) / r, (SYY - cy) / r
    rr = u * u + v * v
    inside = rr < 1
    z = np.sqrt(np.clip(1 - rr, 0, 1))
    c, s_ = np.cos(tilt), np.sin(tilt)
    ut, vt = u * c - v * s_, u * s_ + v * c
    lat = np.arcsin(np.clip(vt, -1, 1))
    lon = np.arctan2(ut, z)
    warp = raster.fbm(SW, SH, 120, seed, octaves=4, period=False)
    streak = raster.fbm(SW, SH, 24, seed + 1, octaves=3, period=False)
    b = 0.5 + 0.5 * np.sin(lat * 26 * band_scale + warp * 2.0 + 0.6 * np.sin(lon * 3 + lat * 9))
    b = 0.7 * b + 0.3 * streak
    col = raster.ramp(JOV_SUN, 0.15 + 0.75 * b)
    if spot is not None:
        sl, so = spot
        dd = ((lat - sl) / 0.07) ** 2 + ((lon - so) / 0.16) ** 2
        sp = np.exp(-dd * 1.6)
        swirl = 0.5 + 0.5 * np.sin(np.sqrt(dd) * 9 - np.arctan2(lat - sl, lon - so) * 2)
        col = col * (1 - sp[..., None] * 0.85) + (np.array((176, 74, 44)) * (0.75 + 0.35 * swirl)[..., None]) * \
            sp[..., None] * 0.85
    nx, ny, nz = u, v, z
    lam = np.clip(nx * sun[0] + ny * sun[1] + nz * sun[2], 0, 1)
    shade = 0.06 + 0.94 * lam ** 0.8
    col = col * shade[..., None]
    edge = smoothstep(1.0, 0.985, np.sqrt(rr))
    rgb = rgb * (1 - edge[..., None]) + col * edge[..., None]
    halo = np.exp(-((np.sqrt(rr) - 1.0) * r / 14) ** 2) * (np.sqrt(rr) > 0.98) * \
        np.clip(nx * sun[0] + ny * sun[1] + 0.5, 0, 1)
    rgb += halo[..., None] * np.array((230, 200, 160)) * 0.5
    return rgb


def moon_disc(rgb, cx, cy, r, sun, seed, base=(60, 66, 78)):
    u, v = (SXX - cx) / r, (SYY - cy) / r
    rr = u * u + v * v
    z = np.sqrt(np.clip(1 - rr, 0, 1))
    lam = np.clip(u * sun[0] + v * sun[1] + z * sun[2], 0, 1)
    tex = raster.fbm(SW, SH, 8, seed, octaves=3, period=False)
    col = np.array(base, float) * (0.5 + 0.5 * tex)[..., None] * (0.12 + 0.95 * lam)[..., None]
    edge = smoothstep(1.0, 0.94, np.sqrt(rr))
    return rgb * (1 - edge[..., None]) + col * edge[..., None]


def still_space(seed):
    rgb = np.zeros((SH, SW, 3)) + np.array((3, 4, 9.0))
    rng = np.random.default_rng(seed)
    for _ in range(260):
        x, y = rng.integers(0, SW), rng.integers(0, SH)
        b = rng.uniform(40, 210)
        rgb[y, x] += (b, b * 0.97, b * 0.92)
    return rgb


def still_a(parts):
    sun = sdf.norm(sdf.vec(-0.75, -0.45, 0.5))
    rgb = still_space(7201)
    rgb = glow_at(rgb, -40, -60, 240, (255, 236, 200), 0.6)
    rgb = moon_disc(rgb, 846, 92, 30, sun, 7202)
    rgb = planet(rgb, 760, 1180, 1060, sun, 7203, tilt=0.18, spot=(-0.98 + 0.0, -0.32))
    img = to_img(rgb)
    rgb = arr(img)[..., :3]
    for (x, y, k), key in (((300, 270, 1.0), "carrier"), ((520, 190, 0.6), "escort1"), ((170, 160, 0.5), "escort2")):
        sp = parts[key]
        for j in range(1, 30):                                  # the descent's trail: soft, behind and up-left
            glow_at(rgb, x - j * 9 * k, y - j * 5 * k, 7 * k + j * 0.6, (255, 190, 120), 0.05 * (1 - j / 30))
        over(rgb, sp, x - sp.width / 2, y - sp.height / 2)
        glow_at(rgb, x - sp.width * 0.33, y - sp.height * 0.12, 9 * k, (120, 200, 255), 0.9)
    for x, y in ((610, 300), (650, 330), (420, 380), (455, 405)):   # Stormhawks: engine points
        glow_at(rgb, x, y, 2.2, (150, 210, 255), 1.6)
    return to_img(rgb)


def still_b(parts):
    sun = sdf.norm(sdf.vec(-0.9, 0.1, 0.25))
    hz = 250
    yy = SYY
    t = np.clip(yy / hz, 0, 1)
    sky = raster.ramp([(0.0, (40, 22, 40)), (0.55, (150, 76, 50)), (0.85, (236, 160, 80)), (1.0, (255, 220, 150))], t)
    rgb = sky.copy()
    rgb = moon_disc(rgb, 760, 70, 26, sdf.norm(sdf.vec(-0.9, 0.2, 0.3)), 7211, base=(70, 70, 84))
    below = yy > hz
    depth = np.clip((yy - hz) / (SH - hz), 0.001, 1)
    zz = 1.0 / (depth + 0.04)                                    # the cloud sea in perspective
    warp = raster.fbm(SW, SH, 140, 7212, octaves=4, period=False)
    bb = 0.5 + 0.5 * np.sin(zz * 2.2 + warp * 3.0 + (SXX - 480) / 260)
    sea = raster.ramp([(0.0, (90, 40, 26)), (0.4, (170, 90, 50)), (0.75, (232, 170, 104)), (1.0, (255, 230, 180))],
                      0.2 + 0.7 * bb)
    sea = sea * (0.6 + 0.4 * np.exp(-((SXX - 140) / 420) ** 2))[..., None]
    haze = np.exp(-(yy - hz) / 30.0)
    sea = sea * (1 - 0.6 * haze[..., None]) + np.array((255, 214, 150)) * 0.6 * haze[..., None]
    rgb = np.where(below[..., None], sea, rgb)
    shade, a = billows(SH, SW, 7213, 7, y_range=(0.36, 0.47), size=(16, 40), spread=(80, 16))
    tones = [(0.0, (110, 50, 40)), (0.5, (200, 110, 66)), (1.0, (255, 200, 130))]
    rgb = rgb * (1 - a[..., None]) + raster.ramp(tones, shade) * a[..., None]
    rgb = glow_at(rgb, 60, hz - 4, 120, (255, 220, 150), 0.7)
    rgb = glow_at(rgb, 60, hz - 4, 26, (255, 250, 230), 1.2)
    img = to_img(rgb)
    dk = parts["deck"]
    cx, cy = 640, 360
    d = ImageDraw.Draw(img)
    for bx, by in ((560, 150), (720, 160), (640, 120)):
        d.line([bx, by + 50, cx + (bx - cx) * 0.6, cy - 30], fill=(60, 40, 30, 255), width=1)
    rgb = arr(img)[..., :3]
    ring = np.exp(-((np.hypot((SXX - cx) / 1.0, (SYY - cy - 46) / 0.32) - 190) / 5) ** 2) * 0.8
    rgb += ring[..., None] * np.array((150, 220, 255))
    for bx, by in ((560, 150), (720, 160), (640, 120)):
        over(rgb, parts["balloon"], bx - parts["balloon"].width / 2, by - parts["balloon"].height / 2)
    over(rgb, dk, cx - dk.width / 2, cy - dk.height / 2)
    for (x, y), key in (((300, 330), "ace1"), ((360, 370), "ace2")):
        sp = parts[key]
        over(rgb, sp, x - sp.width / 2, y - sp.height / 2)
        glow_at(rgb, x + sp.width * 0.3, y + sp.height * 0.1, 4, (255, 120, 60), 0.9)
    return to_img(rgb)


def title_card(img):
    import act_stills as acts  # noqa: E402  (the card's numbers)
    import act_titles  # noqa: E402  (the chrome lettering, imported unchanged)
    card = Image.new("RGBA", (SW, SH), (0, 0, 0, 255))
    card.alpha_composite(Image.blend(Image.new("RGBA", (SW, SH), (0, 0, 0, 255)), img.convert("RGBA"), acts.BRIGHTNESS))
    card.paste((0, 0, 0, 255), (0, 0, SW, acts.BAR))
    card.paste((0, 0, 0, 255), (0, SH - acts.BAR, SW, SH))
    letters = act_titles.title({"act": "ACT VI", "name": "JOVIAN STORM"})
    card.alpha_composite(letters, ((SW - letters.width) // 2, 138))
    text(card, SW / 2 - raster.text_width("JUPITER - SEPTEMBER 2185") / 2, 392, "JUPITER - SEPTEMBER 2185",
         (210, 210, 220))
    return card


STILL_WHAT = {"a": "A 'THE DIVE': JUPITER'S SUNLIT BANDS AND THE GREAT RED SPOT UNDER THE CDF CARRIER GROUP DIVING FROM "
                   "ORBIT, CALLISTO A DARK DISC BESIDE THE LIMB",
              "b": "B 'AURELIA'S GOLD': THE CLOUD CITY AT GOLDEN HOUR, ITS BALLOONS AND ANTI-GRAV RING, A GOLD TALON PAIR, "
                   "CALLISTO ABOVE"}


def make_still(variants):
    jobs = []
    if "a" in variants:
        jobs += [("carrier", carrier_job, (300, -28, -40)), ("escort1", carrier_job, (170, -24, -44)),
                 ("escort2", carrier_job, (140, -32, -36))]
    if "b" in variants:
        jobs += [("deck", deck_job, (2.4, 58)), ("balloon", balloon_job, (1.8, 30)),
                 ("ace1", ace_still_job, (70, 200, 30)), ("ace2", ace_still_job, (56, 200, 30))]
    with pool() as ex:
        parts = still_models(ex, jobs)
    out = DESIGN / "campaign" / "act-6-jovian-storm" / "concept"
    stills = {}
    for v in variants:
        img = still_a(parts) if v == "a" else still_b(parts)
        stills[v] = img
        save_png(img, out / f"act-6-still-r72-{v}.png")
    if all((out / f"act-6-still-r72-{v}.png").exists() for v in "ab"):
        hw, hh = SW // 2, SH // 2
        sheet = raster.sheet(1000, 40 + 2 * (hh + 30) + 60, "ACT 6 TITLE-CARD STILL (R72)",
                             "LEFT: THE STILL, RIGHT: THE TITLE CARD OVER IT")
        y = 38
        for v in "ab":
            im = stills.get(v) or Image.open(out / f"act-6-still-r72-{v}.png").convert("RGBA")
            text(sheet, 16, y, STILL_WHAT[v][:150], raster.LABEL)
            sheet.alpha_composite(im.resize((hw, hh), Image.LANCZOS), (16, y + 14))
            sheet.alpha_composite(title_card(im).resize((hw, hh), Image.LANCZOS), (16 + hw + 8, y + 14))
            y += hh + 30
        lines = wrap("THE STILLS ARE SMOOTH 24-BIT RENDERS (NOT ACT_STILLS.PY'S 96-COLOUR ORDERED DITHER: THE 2026-10-30 "
                     "RULE FOR HAZE AND GLOW). THE CARD'S DATE LINE IS DRAWN IN THE GAME'S FONT. STAND-INS: THE CDF "
                     "HEAVY TRANSPORT (ACT 3) FOR THE CARRIER GROUP'S HULLS; HARKER'S ACE MODEL FOR THE GOLD TALONS.", 158)
        for i, n in enumerate(lines):
            text(sheet, 16, y + i * 14, n, raster.ACCENT if i == 0 else raster.LABEL)
        save_png(sheet, out / "act-6-still-r72-sheet.png")



# =========================================================================== 6 the Act 6 radio portraits

def _pt():
    import artkit  # noqa: E402
    import belt_r60 as b60  # noqa: E402  (Harker's face and the headset, imported unchanged)
    import portraits as pt  # noqa: E402  (tools/art/portraits.py: the production style-B pipeline, unchanged)
    import portraits_r03 as r03  # noqa: E402
    import portraits_r04 as r04  # noqa: E402
    return artkit, b60, pt, r03, r04


SKIN, EYE, IRIS, LIPS, HAIR, CLOTH, ACCENT, METAL, GLOW, EXTRA, BROW = range(11)


def _skin(col):
    return Material(col, shininess=16, spec=0.2)


def _base_mats(skin, iris, lips, hair, cloth, accent, metal, glow, extra, brow):
    return [_skin(skin), Material((0.86, 0.84, 0.8), shininess=60, spec=0.5), Material(iris, shininess=90, spec=0.9),
            Material(lips, shininess=24, spec=0.25), hair, cloth, accent, metal, glow, extra, brow]


GLOSS_BLACK = Material((0.05, 0.05, 0.06), metal=0.4, shininess=110, spec=1.1)
GUARD_GOLD = Material((1.0, 0.74, 0.18), metal=0.9, shininess=90, spec=1.0)


def harker_guard():
    """Harker of Level 29 (his final face, belt_r60.harker's) in the Honour Guard's gloss black and gold flight suit:
    gold piping on the collar ring and the shoulders, the Ascendancy crest on the collar (no CDF patch any more)."""
    _, b60, _, r03, _ = _pt()
    scene0, mats = b60.harker()
    mats = list(mats)
    mats[CLOTH] = GLOSS_BLACK
    mats[METAL] = GUARD_GOLD
    mats[EXTRA] = Material((0.03, 0.03, 0.035), shininess=40, spec=0.4)

    def scene(p):
        d, m = scene0(p)
        pb = r03.turn(p, -0.08)
        crest = sdf.sd_cylinder_z(pb, (0.17, -0.02, 0.2), 0.05, 0.02)
        pipe = r03.sd_torus_z(np.stack([pb[:, 0], pb[:, 2] + 0.03, pb[:, 1] + 0.05], axis=-1), (0, 0, 0), 0.25, 0.018)
        stripes = [sdf.sd_capsule(pb, (s * 0.3, -0.16, 0.12), (s * 0.62, -0.3, 0.06), 0.02) for s in (-1, 1)]
        badge_hide = sdf.sd_cylinder_z(pb, (-0.38, -0.36, 0.24), 0.11, 0.05)
        m = np.where((badge_hide < 0.01) & np.isin(m, (EXTRA, ACCENT)), CLOTH, m)
        return union((d, m), (crest, ACCENT), (pipe, METAL), (np.minimum(*stripes), METAL))
    return scene, mats


def _helmet(q, visor_up=True):
    shell = sdf.sd_ellipsoid(q, (0, 0.52, -0.05), (0.37, 0.42, 0.39))
    shell = np.maximum(shell, -sdf.sd_ellipsoid(q, (0, 0.52, -0.05), (0.33, 0.38, 0.35)))
    opening = sdf.sd_box(q, (0, 0.4, 0.3), (0.25, 0.24, 0.3), 0.06)
    shell = np.maximum(shell, -opening)
    shell = np.maximum(shell, -(q[:, 1] - 0.12))
    visor = sdf.sd_ellipsoid(q, (0, 0.6, 0.0), (0.385, 0.33, 0.41))
    visor = np.maximum(visor, -sdf.sd_ellipsoid(q, (0, 0.6, 0.0), (0.36, 0.31, 0.39)))
    visor = np.maximum(visor, -(q[:, 2] - 0.12))
    visor = np.maximum(visor, -(q[:, 1] - 0.67))
    visor = np.maximum(visor, q[:, 1] - 0.8)
    return shell, visor


def vesper():
    """Vesper, the dancer: a woman in her late twenties, an Aurelia-born cloud racer; a black flight helmet with white
    ribbon stripes (her Talon's mark), the gold visor pushed up, a strand of dark hair; the guard's black suit."""
    _, b60, _, r03, _ = _pt()
    mats = _base_mats((0.78, 0.6, 0.5), (0.2, 0.14, 0.1), (0.62, 0.34, 0.34),
                      Material((0.1, 0.07, 0.06), shininess=20, spec=0.3, pattern=r03.strand_pattern(90, 0.3)),
                      GLOSS_BLACK, GUARD_GOLD, Material((0.06, 0.06, 0.07), metal=0.5, shininess=120, spec=1.2),
                      Material((0.1, 0.1, 0.1), emission=(1.0, 0.2, 0.1)),
                      Material((0.95, 0.95, 0.92), metal=0.1, shininess=90, spec=0.8),
                      Material((0.1, 0.07, 0.06), shininess=8, spec=0.08))

    def scene(p):
        q = r03.turn(p, 0.26, 0.02)
        items, skin = r03.face(q, jaw=0.9, mouth="smile")
        shell, visor = _helmet(q)
        strand = sdf.sd_capsule(q, (0.2, 0.62, 0.22), (0.24, 0.36, 0.24), 0.03, 0.015)
        pb = r03.turn(p, 0.1)
        neck, torso = r03.body(pb, neck_r=0.115, shoulders=0.74)
        collar = sdf.sd_cylinder_y(pb, (0, -0.02, -0.04), 0.2, 0.07)
        d, m = sdf.union(*items, (shell, METAL), (visor, ACCENT), (strand, HAIR), (sdf.smin(neck, skin, 0.05), SKIN),
                         (torso, CLOTH), (collar, CLOTH))
        rib = (np.abs(np.abs(q[:, 0]) - 0.12) < 0.045) & (q[:, 1] > 0.78)
        m = np.where((m == METAL) & rib, EXTRA, m)
        piping = np.abs(pb[:, 1] + 0.02) < 0.03
        m = np.where((m == CLOTH) & piping, ACCENT, m)
        return d, m
    return scene, mats


def kestrel():
    """Kestrel, the feinter: a Helix test pilot in his twenties, quick and cocky; black and gold chevrons on the helmet
    and a red band (his Talon's tail mark), the gold visor up, a half grin; the guard's black suit."""
    _, b60, _, r03, _ = _pt()
    mats = _base_mats((0.7, 0.52, 0.42), (0.12, 0.16, 0.2), (0.56, 0.34, 0.3),
                      Material((0.12, 0.09, 0.07), shininess=10, spec=0.1),
                      GLOSS_BLACK, GUARD_GOLD, Material((0.06, 0.06, 0.07), metal=0.5, shininess=120, spec=1.2),
                      Material((0.1, 0.1, 0.1), emission=(1.0, 0.2, 0.1)),
                      Material((0.75, 0.08, 0.06), metal=0.2, shininess=80, spec=0.7),
                      Material((0.12, 0.09, 0.07), shininess=8, spec=0.08))

    def scene(p):
        q = r03.turn(p, -0.3, -0.03)
        items, skin = r03.face(q, jaw=1.02, mouth="grin")
        shell, visor = _helmet(q)
        pb = r03.turn(p, -0.1)
        neck, torso = r03.body(pb, neck_r=0.13, shoulders=0.84)
        collar = sdf.sd_cylinder_y(pb, (0, -0.02, -0.04), 0.2, 0.07)
        d, m = sdf.union(*items, (shell, METAL), (visor, ACCENT), (sdf.smin(neck, skin, 0.05), SKIN),
                         (torso, CLOTH), (collar, CLOTH))
        chev = (((q[:, 1] - 0.9 * np.abs(q[:, 0])) % 0.12) < 0.05) & (q[:, 1] > 0.86)
        m = np.where((m == METAL) & chev, ACCENT, m)
        band = np.abs(q[:, 1] - 0.82) < 0.025
        m = np.where((m == METAL) & band, EXTRA, m)
        return d, m
    return scene, mats


def aurelia_traffic():
    """Aurelia Traffic: the city's civil traffic controller, a woman in her thirties, born on the city; dark hair pulled
    back, a steady look; Aurelia's white-and-gold civil uniform (gold collar trim, an epaulette), a headset."""
    _, b60, _, r03, _ = _pt()
    mats = _base_mats((0.62, 0.44, 0.34), (0.16, 0.1, 0.06), (0.52, 0.3, 0.28),
                      Material((0.08, 0.06, 0.05), shininess=24, spec=0.35, pattern=r03.strand_pattern(110, 0.3)),
                      Material((0.9, 0.89, 0.86), shininess=20, spec=0.25, pattern=r03.noise_pattern(70, 0.04)),
                      Material((1.0, 0.76, 0.24), metal=0.9, shininess=90, spec=1.0),
                      Material((0.28, 0.29, 0.3), metal=0.6, shininess=70, spec=0.7),
                      Material((0.1, 0.1, 0.1), emission=(0.3, 1.0, 0.8)),
                      Material((0.86, 0.86, 0.84), shininess=10, spec=0.1),
                      Material((0.08, 0.06, 0.05), shininess=8, spec=0.08))

    def scene(p):
        q = r03.turn(p, 0.2, 0.0)
        items, skin = r03.face(q, jaw=0.9, mouth="neutral")
        hair = sdf.sd_ellipsoid(q, (0, 0.53, -0.07), (0.31, 0.37, 0.33))
        hair = np.maximum(hair, -(q[:, 1] - 0.62 + 0.36 * np.maximum(q[:, 2], 0)))
        bun = sdf.sd_ellipsoid(q, (0, 0.5, -0.36), (0.13, 0.12, 0.1))
        hair = sdf.smin(hair, bun, 0.05)
        pb = r03.turn(p, 0.08)
        neck, torso = r03.body(pb, neck_r=0.115, shoulders=0.74)
        collar = sdf.sd_cylinder_y(pb, (0, -0.04, -0.04), 0.19, 0.09)
        collar = np.maximum(collar, -sdf.sd_cylinder_y(pb, (0, -0.04, -0.04), 0.14, 0.3))
        trim = np.maximum(sdf.sd_cylinder_y(pb, (0, 0.04, -0.04), 0.195, 0.015), -sdf.sd_cylinder_y(pb, (0, 0.04, -0.04),
                                                                                                     0.15, 0.3))
        ep = sdf.sd_box(pb, (-0.5, -0.22, 0.02), (0.13, 0.03, 0.12), 0.02)
        return sdf.union(*items, (hair, HAIR), (sdf.smin(neck, skin, 0.05), SKIN), (torso, CLOTH), (collar, CLOTH),
                         (trim, ACCENT), (ep, ACCENT), *b60._headset(q, 1))
    return scene, mats


SPEAKERS6 = {
    "harker-guard": dict(build=harker_guard, name="HARKER", role="HONOUR GUARD", key=(-1.6, 2.0, 3.0),
                         rim=(2.4, 1.0, -1.2), rim_col=(1.0, 0.72, 0.2), bg=((14, 6, 2), (60, 30, 10)),
                         fill_col=(1.0, 0.7, 0.4), channel="enemy", exposure=1.15),
    "vesper": dict(build=vesper, name="VESPER", role="HONOUR GUARD", key=(-1.6, 2.2, 3.0), rim=(2.4, 1.0, -1.2),
                   rim_col=(1.0, 0.85, 0.5), bg=((14, 6, 2), (60, 30, 10)), fill_col=(1.0, 0.7, 0.4), channel="enemy",
                   exposure=1.2),
    "kestrel": dict(build=kestrel, name="KESTREL", role="HONOUR GUARD", key=(-1.6, 2.0, 3.0), rim=(2.4, 1.0, -1.2),
                    rim_col=(1.0, 0.4, 0.2), bg=((14, 6, 2), (60, 30, 10)), fill_col=(1.0, 0.7, 0.4), channel="enemy",
                    exposure=1.2),
    "aurelia-traffic": dict(build=aurelia_traffic, name="AURELIA TRAFFIC", role="AURELIA", key=(-1.5, 2.1, 3.1),
                            rim=(2.4, 1.0, -1.2), rim_col=(1.0, 0.85, 0.6), bg=((4, 12, 16), (30, 50, 60)),
                            fill_col=(1.0, 0.9, 0.7), channel="cdf", exposure=1.0),
}


def cockpit_room(ramp):
    """b for the aces: their cockpit - two canopy struts crossing the amber screen, the guard's gold crest top right."""
    _, _, pt, _, _ = _pt()
    N = pt.N
    yy, xx = np.mgrid[0:N, 0:N].astype(np.float64)
    bg = np.zeros((N, N, 3)) + ramp[0] * 255 * 0.8
    sky = np.clip(1 - yy / N, 0, 1)[..., None]
    bg += sky * ramp[1] * 255 * 0.5
    for x0, x1 in ((6, 22), (N - 4, N - 22)):
        strut = np.abs(xx - (x0 + (x1 - x0) * yy / N)) < 2.2
        bg[strut] = ramp[3] * 255 * 0.7
    d = np.hypot(xx - 60, yy - 11)
    bg[d < 6] = ramp[4] * 255 * 0.9
    rays = (np.cos(np.arctan2(yy - 11, xx - 60) * 10) > 0.5) & (d > 6) & (d < 10)
    bg[rays] = ramp[3] * 255 * 0.8
    return bg


def tower_room(ramp):
    """b for Aurelia Traffic: the tower's windows on the gold domes - mullions, the domes' curves low behind her."""
    _, _, pt, _, _ = _pt()
    N = pt.N
    yy, xx = np.mgrid[0:N, 0:N].astype(np.float64)
    bg = np.zeros((N, N, 3)) + ramp[1] * 255 * 0.7
    for cx, cy, r in ((14, 74, 24), (58, 78, 26), (36, 86, 20)):
        dome = np.hypot(xx - cx, yy - cy) < r
        bg[dome] = ramp[3] * 255 * 0.85
        rim = np.abs(np.hypot(xx - cx, yy - cy) - r) < 1.0
        bg[rim] = ramp[4] * 255
    for x in (2, 24, 48, 70):
        bg[:, max(0, x - 1):x + 2] = ramp[0] * 255
    bg[18:20, :] = ramp[0] * 255
    return bg


def _speaker6_fig(slug):
    _, _, pt, r03, _ = _pt()
    r03.CAST[slug] = SPEAKERS6[slug]
    return slug, pt.figure(slug, r03.render_bust(slug))


def make_speakers(variants):
    artkit, b60, pt, r03, r04 = _pt()
    for slug, c in SPEAKERS6.items():
        r03.CAST[slug] = c
    with pool() as ex:
        figs = dict(ex.map(_speaker6_fig, list(SPEAKERS6)))
    for v in variants:
        radios = []
        for slug in SPEAKERS6:
            if v == "b":
                pt.ROOMS[slug] = tower_room if slug == "aurelia-traffic" else cockpit_room
            fig = artkit.quantize_set([figs[slug]], r04.COLORS)[0]
            radios.append(pt.screen(slug, fig)[0])
            pt.ROOMS.pop(slug, None)
        names = ["HARKER", "VESPER", "KESTREL", "AURELIA TRAFFIC"]
        prod = [Image.open(SPR / "portraits" / f"radio-{s}-neutral.png").convert("RGBA")
                for s in ("harker", "ceres-control", "okafor")]
        rows = [("RADIO 72X72 (1X)", panel_grid(radios, 4, pad=22, labels=names)),
                ("RADIO (3X)", panel_grid([sprite.enlarge(r, 3) for r in radios], 4, pad=8, labels=names)),
                ("LINE-UP AT 1X WITH THE PRODUCTION SPEAKERS (HARKER L29, CERES HUB CONTROL, OKAFOR)",
                 panel_grid(radios + prod, 7, pad=22, labels=["HARKER", "VESPER", "KESTREL", "TRAFFIC", "HARKER L29",
                                                               "CERES CTRL", "OKAFOR"]))]
        name = {"a": "A - THE PLAIN CHANNEL SCREENS", "b": "B - THEIR ROOMS"}[v]
        notes = [f"THE ACT 6 RADIO SPEAKERS {name} (STYLE B, TOOLS/ART/PORTRAITS.PY'S PIPELINE, NEUTRAL ONLY AS THE "
                 "MINOR SPEAKERS; THE PIPELINE'S OWN SOFT DITHER AND 36-COLOUR PALETTE, AS EVERY PORTRAIT).",
                 "HARKER (AMBER): HIS FINAL L29 FACE IN THE GUARD'S GLOSS BLACK AND GOLD FLIGHT SUIT, GOLD PIPING, THE "
                 "CREST ON THE COLLAR (THE STITCHED CDF PATCH GONE). VESPER (AMBER): A BLACK HELMET WITH WHITE RIBBON "
                 "STRIPES, THE GOLD VISOR PUSHED UP, A STRAND OF HAIR, A FAINT SMILE. KESTREL (AMBER): BLACK AND GOLD "
                 "CHEVRONS ON THE HELMET, A RED BAND, THE VISOR UP, A HALF GRIN. AURELIA TRAFFIC (TEAL): THE CITY'S "
                 "WHITE-AND-GOLD CIVIL UNIFORM, HAIR PULLED BACK, A HEADSET.",
                 ("A: THE PLAIN SCREENS WITH THE DOT GRID, AS THE CAST'S AND THE ACT 5 SPEAKERS'."
                  if v == "a" else
                  "B: THEIR ROOMS - THE ACES IN THEIR COCKPITS (TWO CANOPY STRUTS, THE GUARD'S GOLD CREST TOP RIGHT); "
                  "AURELIA TRAFFIC AT THE TOWER'S WINDOWS ON THE GOLD DOMES (THE STORY DRAFT'S BRIEF)."),
                 "WORRY: THREE AMBER ACES AT 72 PX - THE HELMETS (RIBBONS, CHEVRONS) AND HARKER'S BARE HEAD TELL THEM "
                 "APART; THE MID-BOSS BAR'S CALL-SIGN TAGS DO THE REST."]
        img = compose(f"ACT 6 RADIO SPEAKERS {name}", rows, notes, width=900)
        save_png(img, DESIGN / "story" / "characters" / "concept" / f"act6-speakers-r72-{v}.png")


# =========================================================================== main

def make_phases73(variants):
    """Round 73: the Ascendant's phase storyboard with frame 8's jagged break (round 72 A.11; hull a was chosen)."""
    load_ascendant(variants)
    for v in variants:
        ascendant_storyboard(v, rnd=73)


def make_city73(variants):
    """Round 73: the falling city b (chosen in round 72 A.13) breaking along a jagged fault (round 72 A.11's rule)."""
    make_city([v for v in variants if v == "b"], rnd=73)


SUBJECTS = {"guard": make_guard, "ascendant": make_ascendant, "moods": make_moods, "banks": make_banks,
            "city": make_city, "still": make_still,
            "speakers": make_speakers, "phases73": make_phases73, "city73": make_city73}


def main(args):
    names = [a for a in args if a in SUBJECTS] or list(SUBJECTS)
    bad = [a for a in args if a not in SUBJECTS and a not in ("a", "b")]
    if bad:
        sys.exit(f"unknown: {bad}; subjects: {', '.join(SUBJECTS)}")
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    for n in names:
        SUBJECTS[n](variants)


if __name__ == "__main__":
    main(sys.argv[1:])
