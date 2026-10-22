"""Shared helpers of the M8 part D production art (Level 24 "Abyssal Plain", step ART-D, concept round 53):
abyss_ray.py, barbfin.py, spawning_reef.py, l24_props.py and their review material, on top of l23kit and artkit.

* **Held sprites.** l23kit's rule unchanged (``write``/``load``/``claimed``/``where`` are l23kit's): an enemy's root is
  claimed once ``design/enemies/<category>/<slug>/data.yaml`` exists, a ground target's or drop's ``sprite``/``look``
  once a level names it; until then the frames wait in ``build/art-held/sprites/``. Rerun the script after the data.
* **Review plates.** The plain under the per-section grade: Level 24's production floor when
  tools/art/backdrop_l24.py has written it (``assets/backdrop/level-24/``), else the concept's plain silt floor
  (tools/concept/enemies_r48.py ``seafloor``), under ``grade``: the review's stand-in for the game's under-water grade
  with the per-section ambient ART-D proposes for round 53 (``SECTION_AMBIENT``: the plain darkens section by section,
  "warm behind, dark ahead", round 48 Q2 a). Glows, bullets and additive light are added after the grade, as the game
  draws them ungraded.
"""
import numpy as np
from PIL import Image

import l23kit
from l23kit import (HELD, ASSETS_SPRITES, add_c, claimed, heading_index, load, paste_c, ship,  # noqa: F401
                    where, write)
from artkit import ROOT

BACKDROP = ROOT / "assets" / "backdrop" / "level-24"
WATER_TINT = np.array((4, 34, 48), float)            # darker than L23's: the plain's blue-black water
SECTION_AMBIENT = {1: 0.50, 2: 0.32, 3: 0.18, 4: 0.14}   # ART-D's proposal (m8d-gaps §1.6), round 53
SECTION_TINT = {1: 0.30, 2: 0.36, 3: 0.42, 4: 0.44}

_PLATE = {}


def _floor():
    if "floor" not in _PLATE:
        cands = sorted(BACKDROP.glob("*plain*.png")) + sorted(BACKDROP.glob("*reef*.png"))
        cands = [c for c in cands if not c.stem.split("_")[-1].isdigit() or c.stem.endswith("_0")]
        if cands:
            img = Image.open(cands[0]).convert("RGBA")
        else:
            import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the plain silt floor)
            img = e48.seafloor(2401)
        _PLATE["floor"] = img
    return _PLATE["floor"]


def grade(img, section=2, ambient=None, tint=None):
    """The review's stand-in for the game's under-water grade at a section's ambient (straight alpha kept)."""
    amb = SECTION_AMBIENT[section] if ambient is None else ambient
    k = SECTION_TINT.get(section, 0.36) if tint is None else tint
    a = np.array(img.convert("RGBA")).astype(np.float64)
    rgb = a[..., :3] * (1 - k) + WATER_TINT * k
    a[..., :3] = rgb * (amb + (1 - amb) * 0.35)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def plate(w, h, x0=0, y0=0, section=2, graded=True):
    """A w x h crop of the plain's floor (wrapping), graded at the section's ambient."""
    src = _floor()
    sw, sh = src.size
    out = Image.new("RGBA", (w, h))
    for oy in range(-(y0 % sh), h, sh):
        for ox in range(-(x0 % sw), w, sw):
            out.paste(src, (ox, oy))
    return grade(out, section) if graded else out


def lit(img, section=2):
    """A unit sprite on the play plane under the section's grade (the game grades everything but glows)."""
    return grade(img, section)


__all__ = ["HELD", "ASSETS_SPRITES", "BACKDROP", "SECTION_AMBIENT", "add_c", "claimed", "grade", "heading_index",
           "lit", "load", "paste_c", "plate", "ship", "where", "write", "l23kit"]
