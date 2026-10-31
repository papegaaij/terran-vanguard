#!/usr/bin/env python3
"""Concept round 73 - M10 part A's two open art questions (step ART-A; m10a-gaps §7), a/b proposals over round 72's
Level 36 mood grade (tools/concept/act6_r72.py ``sky_l36``, the L36 panel of jovian-moods-r72-a.png at 1x) and, for
the lightning, Level 38's storm grade (``sky_l38``), where the strikes live.

  QA-2 the lightning strike (design/world/jovian/concept/lightning-strike-r73-{a,b}.png/.gif):
    a "jagged bolt" (recommended): a jagged blue-white bolt filling the 40 px column top to bottom, a white core and
      two short side forks, Level 12's sky flash with it;
    b "light column": a straight full-height column of white light with a jagged core line, its edges fading in over
      the 0.25 s.
    Both after the same telegraph (the 40 px column's faint edges and the thin blue-white line, brightening for 1.0 s).
  QA-3 the Reflex Shield's bolt (design/player/shields/concept/reflex-bolt-r73-{a,b}.png/.gif):
    a "white-gold bullet": the enemy bullet's round shape in the shield's white-gold, a dark rim, a white core, a short
      gold trail;
    b "gold needle": a thin gold needle with a white core and a faint trail, so a bolt never resembles an enemy bullet.
    Each beside the enemy orb, the Ascendancy's gold needle and the mirror's bounced magenta orb, and in flight: a
    stream of orbs on the ship's full Reflex Shield, each hit flashing gold and sending a bolt straight up.

The frames are the production generator's (tools/art/act6_gear.py: ``lightning_set``, ``strike_scene``,
``reflex_set``, ``reflex_scene``; variant a is the one it writes to the game's files); this script only lays out the
proposals. Every glow is a smooth float light field drawn once (no ordered dither, no posterising).
Run: python3 tools/concept/act6_r73.py [lightning] [bolt] [a|b]   (no args = everything, ~20 s)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "art"))
import act6_gear as g  # noqa: E402
import artkit  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster, sprite  # noqa: E402
from render.config import ROOT  # noqa: E402

DESIGN = ROOT / "design"
SUB = "CONCEPT ROUND 73 - M10 PART A"
OUT = {"lightning": (DESIGN / "world" / "jovian" / "concept", "lightning-strike"),
       "bolt": (DESIGN / "player" / "shields" / "concept", "reflex-bolt")}
NAMES = {"lightning": {"a": "A - JAGGED BOLT (RECOMMENDED)", "b": "B - LIGHT COLUMN"},
         "bolt": {"a": "A - WHITE-GOLD BULLET", "b": "B - GOLD NEEDLE"}}


def concept_sheet(title, rows, width=1300):
    """Labelled rows of frames at their zoom on the sheet background: rows = [(label, frames, zoom, glow), ...]."""
    gap = 8
    heights = [24 + max(f.height for f in frames) * zoom + gap for _, frames, zoom, _ in rows]
    img = raster.sheet(width, 40 + sum(heights) + 10, title, SUB)
    y = 38
    for (text, frames, zoom, glow), h in zip(rows, heights):
        raster.draw_text(img, 16, y, text, raster.LABEL)
        x = 16
        for f in frames:
            cell = artkit.on_background(f, zoom, glow)
            if x + cell.width > width - 16:
                break
            img.alpha_composite(cell, (x, y + 14))
            x += cell.width + 6
        y += h
    return img


def save(kind, v, sheet, gif, fps=g.FPS):
    folder, subject = OUT[kind]
    folder.mkdir(parents=True, exist_ok=True)
    png, out = folder / f"{subject}-r73-{v}.png", folder / f"{subject}-r73-{v}.gif"
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(gif, out, fps=fps, colors=128)
    print(f"{png.relative_to(ROOT)}, {out.name} ({size / 1e6:.1f} MB)")


# ===================================================================== QA-2 the lightning strike

def lightning(v):
    art = g.lightning_set(v)
    l36 = g.strike_scene(v, art, g.plate("l36", 360, g.a6.FH), x=150, ship_x=250)
    l38 = g.strike_scene(v, art, g.plate("l38", 360, g.a6.FH), x=150, ship_x=250)
    gif = []
    for a, b in zip(l36, l38):
        row = Image.new("RGBA", (728, g.a6.FH), (10, 12, 26, 255))
        row.alpha_composite(a, (0, 0))
        row.alpha_composite(b, (368, 0))
        gif.append(row)
    half = [s.resize((s.width // 2, s.height // 2), Image.BILINEAR) for s in art["strikes"]]
    keys = (21, 31, 35, 37)
    rows = [("THE STRIKE'S FRAMES (112X540, ADDITIVE) AT 0.5X" + (": THREE BOLT SHAPES, 2 STEPS EACH, FADED IN THE LAST "
             "0.1 S" if v == "a" else ": THE COLUMN OVER THE 0.25 S, ITS EDGES FADING TO THE CORE"), half, 1, True),
            ("OVER ROUND 72'S LEVEL 36 GRADE AT 1X: THE TELL AT 0.7 S, THE STRIKE AT 0.03 / 0.17 / 0.23 S "
             "(THE FLASH IN THE FIRST 0.1 S); THE TALON IN THE COLUMN DIES",
             [l36[k].crop((40, 40, 300, 420)) for k in keys], 1, False),
            ("OVER LEVEL 38'S STORM (WHERE THE STRIKES LIVE), THE SAME MOMENTS",
             [l38[k].crop((40, 40, 300, 420)) for k in keys], 1, False)]
    sheet = concept_sheet(f"QA-2 LIGHTNING STRIKE {NAMES['lightning'][v]}", rows)
    save("lightning", v, sheet, gif)


# ===================================================================== QA-3 the Reflex bolt

def bolt(v):
    art = g.reflex_set(v)
    orb = artkit.load_frames("orb")[0]
    needle = artkit.load_frames("needle")[8]
    bounced = artkit.load_frames("orb-bounced")[0]
    still = g.plate("l36", 160, 80)
    side = still.copy()
    for (x, sp) in ((24, None), (62, orb), (100, needle), (136, bounced)):
        if sp is None:
            side = artkit.add_light(side, art["trail"], (int(x - g.TRAIL_AT), int(40 - g.TRAIL_AT)))
            sprite.paste_center(side, art["bolt"][0], x, 40)
        else:
            sprite.paste_center(side, sp, x, 40)
    bg = g.plate("l36", 240, 360)
    gif = g.reflex_scene(art, bg, frames=96)
    needles = artkit.load_frames("needle")
    bounce = artkit.load_frames("orb-bounced")
    for i, img in enumerate(gif):                       # enemy needles and a bounced orb drifting through
        for j, (x0, y0) in enumerate(((40, -20), (200, -60), (70, -140))):
            sprite.paste_center(img, needles[8 if j != 1 else 7], x0 + (j - 1) * 0.6 * i, y0 + 5.0 * i)
        sprite.paste_center(img, bounce[2], 200 - 1.4 * i, 30 + 3.2 * i)
    big = [sprite.enlarge(f, 2) for f in gif]
    rows = [("THE BOLT'S FRAMES (" + ("15X15, NORMAL BLEND: THE ORB'S SHAPE IN WHITE-GOLD" if v == "a" else
             "7X24, NORMAL BLEND: A THIN GOLD NEEDLE") + ") AND ITS TRAIL (11X30, ADDITIVE), 4X",
             art["bolt"] + [art["trail"]], 4, False),
            ("ON ROUND 72'S LEVEL 36 GRADE: THE BOLT, THE ENEMY ORB, THE ASCENDANCY'S GOLD NEEDLE, THE MIRROR'S BOUNCED "
             "ORB (1X AND 3X)", [side, sprite.enlarge(side, 3)], 1, False),
            ("IN FLIGHT AT 1X: ORBS ON THE FULL REFLEX SHIELD, EACH HIT A GOLD FLASH AND A BOLT UP AT 640 PX/S; GOLD "
             "NEEDLES AND A BOUNCED ORB DRIFTING THROUGH", [gif[k] for k in (40, 48, 58, 70)], 1, False)]
    sheet = concept_sheet(f"QA-3 REFLEX BOLT {NAMES['bolt'][v]}", rows)
    save("bolt", v, sheet, big)


if __name__ == "__main__":
    args = sys.argv[1:]
    subjects = [a for a in args if a in ("lightning", "bolt")] or ["lightning", "bolt"]
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    for s in subjects:
        for v in variants:
            (lightning if s == "lightning" else bolt)(v)
