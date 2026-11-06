#!/usr/bin/env python3
"""Concept round 88, QA-1 over the real tiles (step A4, M11 part A): the Swarm Call's drone a/b beside the Needler.

ART-A's proposal sheets (swarm_call_r88.py) judged the two shells over round 84's mood stand-ins; this sheet repeats its
readability check (three drones hunting round two production Needlers, two Vrell orbs, the ship's nose) over ART-B's
real backdrop tiles: Level 43's deep field (``assets/backdrop/level-43/deep-2.png``, the Pickets), Level 44's event
plane in the Current (``plane-2.png``) and at its brightest band in the Narrows (``plane-4.png``, the brightest window
of the tile by mean luminance), and Level 46's graveyard (no tiles yet: round 84's stand-in still, the darkest window).
Each window 200x120 at 2x; variant a's row above variant b's. No new art: the drones are swarm_call_r88's
``drone_sprite`` (the production frames' model), imported unchanged.

Outputs: design/player/specials/concept/swarm-call-drone-tiles-r88-a.png (both variants, one sheet)
Run: python3 tools/concept/swarm_tiles_r88.py   (~30 s)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
import swarm_call_r88 as sc  # noqa: E402

ROOT = HERE.parents[1]
TILES = ROOT / "assets" / "backdrop"
OUT = ROOT / "design" / "player" / "specials" / "concept" / "swarm-call-drone-tiles-r88-a.png"


def window(img, bright, w=200, h=120):
    """The brightest (or darkest) w x h window of an image by mean luminance."""
    a = np.array(img.convert("RGB")).astype(float) @ np.array([0.3, 0.59, 0.11])
    best = []
    for y in range(0, img.height - h, 16):
        for x in range(0, img.width - w, 16):
            best.append((a[y:y + h, x:x + w].mean(), x, y))
    best.sort()
    m, x, y = best[-1] if bright else best[len(best) // 2]
    return img.crop((x, y, x + w, y + h)).convert("RGBA")


def backgrounds():
    l46 = sc.stills()[1]
    return [
        ("L43 deep field", window(Image.open(TILES / "level-43" / "deep-2.png"), False)),
        ("L44 the Current", window(Image.open(TILES / "level-44" / "plane-2.png"), False)),
        ("L44 brightest band", window(Image.open(TILES / "level-44" / "plane-4.png"), True)),
        ("L46 graveyard (stand-in)", sc.windows(l46)[1][0].convert("RGBA")),
    ]


def main():
    bgs = backgrounds()
    w, h, s, pad, top = 200, 120, 2, 8, 22
    sheet = Image.new("RGB", (len(bgs) * (w * s + pad) + pad, 2 * (h * s + top) + pad), (18, 16, 24))
    d = ImageDraw.Draw(sheet)
    for r, v in enumerate("ab"):
        for c, (name, bg) in enumerate(bgs):
            x0, y0 = pad + c * (w * s + pad), r * (h * s + top) + top
            d.text((x0, y0 - 16), f"{v}: {name}", fill=(220, 220, 230))
            tile = sc.readability(bg.copy(), v).convert("RGB")
            sheet.paste(tile.resize((w * s, h * s), Image.NEAREST), (x0, y0))
    sheet.save(OUT)
    print("wrote", OUT.relative_to(ROOT))


if __name__ == "__main__":
    main()
