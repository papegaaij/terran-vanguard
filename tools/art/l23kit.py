"""Shared helpers of the M8 part C production art (Level 23 "Through the Ice", step ART-C, concept round 52):
eel_swarm.py, glassfin.py, siphon_pump.py, l23_props.py and the review material of explosions.py's under-water
bursts, on top of artkit.

* **Held sprites.** The atlas packer fails on a sprite no data claims (vanguard.pipeline.SpriteUse): an enemy's
  sprites are claimed by its slug once ``design/enemies/<category>/<slug>/data.yaml`` exists (its first level's
  atlas), a ground target's or a drop's ``sprite`` once a level's data names it. Until then a frame set is written to
  ``build/art-held/sprites/`` instead of ``assets/sprites/`` (its review is built from there); rerun the script once
  the data is in and it lands in ``assets/``. Roots of claimed units (``driftjelly-*``, ``reef-spitter-*``,
  ``ship-*``, ``rook-*``, ``explosion-*``) go straight to ``assets/``.
* **Review plates.** The review sheets and GIFs stand on Level 23's production backdrop when
  tools/art/backdrop_l23.py has written it (``assets/backdrop/level-23/``), else on the chosen round-07 kelp farm
  floor of the concept (tools/concept/enemies_r48.py ``farm_floor``), both under ``grade``: a stand-in for the game's
  under-water grade (step C4, ``UnderwaterGrade``: the round-48 lamp-lit look, ambient 0.58 toward a dark teal), for
  the review only. Bullets, glows and additive light are added after the grade, as the game does.
"""
import contextlib
import re

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import DESIGN, ROOT

HELD = ROOT / "build" / "art-held" / "sprites"
ASSETS_SPRITES = ROOT / "assets" / "sprites"
BACKDROP = ROOT / "assets" / "backdrop" / "level-23"
SPRITE_USE = ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java"
ALWAYS = ("driftjelly", "reef-spitter", "ship", "rook", "explosion", "water")
WATER_TINT = np.array((8, 58, 70), float)       # the grade's dark teal (round 48 a, L23 lamp-lit)
AMBIENT = 0.58


def _rooted(name, root):
    return name == root or name.startswith(root + "-")


def _yaml(path):
    return (yaml.safe_load(path.read_text(encoding="utf-8")) or {}) if path.exists() else {}


def level_roots():
    """The roots the levels' data claims beyond the enemies: ground targets' and drops' ``sprite``/``look``."""
    roots = set()
    for f in (DESIGN / "campaign").glob("*/level-*/data.yaml"):
        data = _yaml(f)
        for g in data.get("ground_targets") or []:
            for key in ("sprite", "look"):
                if isinstance(g, dict) and g.get(key):
                    roots.add(str(g[key]))
        for d in data.get("drops") or []:
            for key in ("sprite", "look"):
                if isinstance(d, dict) and d.get(key):
                    roots.add(str(d[key]))
    return roots


def enemy_slugs():
    return {p.parent.name for p in (DESIGN / "enemies").glob("*/*/data.yaml")}


def claimed(name):
    if any(_rooted(name, r) for r in ALWAYS):
        return True
    if any(_rooted(name, r) for r in enemy_slugs()):
        return True
    return any(_rooted(name, r) for r in level_roots())


@contextlib.contextmanager
def sprites_of(name):
    target = ASSETS_SPRITES if claimed(name) else HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        yield target
    finally:
        artkit.SPRITES = before


def write(name, frames, source, single=False):
    """Write a frame set to assets/sprites (claimed) or build/art-held/sprites (not yet); the other folder's stale
    copy of the same set is removed, so a set never lives in both."""
    with sprites_of(name) as target:
        artkit.write_frames(name, frames, source, single=single)
    other = HELD if target == ASSETS_SPRITES else ASSETS_SPRITES
    pattern = re.compile(re.escape(name) + r"(_\d+)?\.png")
    for old in other.glob(f"{name}*.png"):
        if pattern.fullmatch(old.name):
            old.unlink()
    print(f"{name}: {len(frames)} frames -> {target.relative_to(ROOT)}")
    return target


def load(name):
    for folder in (ASSETS_SPRITES, HELD):
        before = artkit.SPRITES
        artkit.SPRITES = folder
        try:
            frames = artkit.load_frames(name)
        finally:
            artkit.SPRITES = before
        if frames:
            return frames
    raise FileNotFoundError(name)


def where(name):
    return "assets/sprites" if claimed(name) else "build/art-held/sprites"


# --------------------------------------------------------------------------- review plates

_PLATE = {}


def _floor():
    if "floor" not in _PLATE:
        cands = sorted(BACKDROP.glob("*farm*.png")) + sorted(BACKDROP.glob("*thera*.png"))
        if cands:
            img = Image.open(cands[0]).convert("RGBA")
        else:
            import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen r07 farm floor)
            img = e48.farm_floor()
        _PLATE["floor"] = img
    return _PLATE["floor"]


def grade(img, ambient=AMBIENT, tint=0.32):
    """The review's stand-in for the game's under-water grade: the lit scene times the ambient, pulled toward the
    water's dark teal (straight alpha kept)."""
    a = np.array(img.convert("RGBA")).astype(np.float64)
    rgb = a[..., :3] * (1 - tint) + WATER_TINT * tint
    a[..., :3] = rgb * (ambient + (1 - ambient) * 0.45)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def plate(w, h, x0=0, y0=0, graded=True):
    """A w x h crop of the farm floor (wrapping), graded."""
    src = _floor()
    sw, sh = src.size
    out = Image.new("RGBA", (w, h))
    for oy in range(-(y0 % sh), h, sh):
        for ox in range(-(x0 % sw), w, sw):
            out.paste(src, (ox, oy))
    return grade(out) if graded else out


def paste_c(dst, img, x, y):
    _paste_clip(dst, img, int(round(x - img.width / 2)), int(round(y - img.height / 2)))
    return dst


def _paste_clip(dst, img, x0, y0):
    x1, y1 = max(0, x0), max(0, y0)
    x2, y2 = min(dst.width, x0 + img.width), min(dst.height, y0 + img.height)
    if x2 <= x1 or y2 <= y1:
        return
    dst.alpha_composite(img.crop((x1 - x0, y1 - y0, x2 - x0, y2 - y0)), (x1, y1))


def add_c(dst, img, x, y, k=1.0):
    """Additive sprite (premultiplied on black) added onto dst at centre (x, y)."""
    a = np.array(dst).astype(np.float64)
    g = np.array(img.convert("RGBA")).astype(np.float64)[..., :3] * k
    x0, y0 = int(round(x - img.width / 2)), int(round(y - img.height / 2))
    x1, y1 = max(0, x0), max(0, y0)
    x2, y2 = min(dst.width, x0 + img.width), min(dst.height, y0 + img.height)
    if x2 > x1 and y2 > y1:
        a[y1:y2, x1:x2, :3] += g[y1 - y0:y2 - y0, x1 - x0:x2 - x0]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def heading_index(dx, dy, n=16):
    """The sprite heading of a motion (dx, dy) in screen px: 0 = down, clockwise."""
    return int(round(np.arctan2(-dx, dy) / (2 * np.pi) * n)) % n


def ship(frame=2):
    try:
        return load("ship-hydro")[frame]
    except FileNotFoundError:
        return artkit.load_frames("ship")[frame]
