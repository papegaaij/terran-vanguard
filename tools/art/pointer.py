#!/usr/bin/env python3
"""Production art: the glass mouse pointer of the out-of-game screens (design/ui, Mouse; M6 part A,
Q6 = b): a small classic arrow in the glass UI kit's look (tools/art/ui_kit.py), a bevelled steel
body in palette B's UTC HULL ramp lit from the top-left, an amber lit left edge like the kit's
cursors, and a 1 px near-black navy outline so it reads on light and dark backdrops alike.

Outputs:
  assets/ui/pointer.png       16x16 RGBA, the arrow 12x16, its tip (the hotspot) at pixel (0, 0);
                              the game scales it by the integer pixel scale (nearest neighbour)
                              and pads it to a power of two for the hardware cursor
  design/ui/concept/pointer-r35-a.png   review sheet: 1x/2x/3x over a glass menu item, a bright
                              and a dark backdrop

The body is SDF ray-marched at 8x with the HUD's light (ui_kit.render / shaded) as a plate with a
rounded bevel, mapped through the steel ramp; the outline and the lit edge are pixel work, since a
ray-march gives a 1 px line one colour. The PNG carries the ``Source`` chunk.

Run: python3 tools/art/pointer.py [--review]   (~2 s; --review only rebuilds the sheet)
"""
import importlib.util
import sys
from pathlib import Path

import numpy as np
from PIL import Image

import artkit
from artkit import DESIGN, ROOT

from render import raster, sdf, sprite  # noqa: E402

_spec = importlib.util.spec_from_file_location("art_ui_kit", Path(__file__).with_name("ui_kit.py"))
ui_kit = importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(ui_kit)

SCRIPT = "pointer.py"
SOURCE = artkit.source_note(SCRIPT, "UI batch")
OUT = ROOT / "assets" / "ui" / "pointer.png"
CONCEPT = DESIGN / "ui" / "concept"
ROUND = "r35"
SIZE = (16, 16)
OUTLINE = (0, 0, 30, 255)                   # the kit's text shadow navy
EDGE = (255, 255, 120, 255)                 # the lit left edge, the kit's amber
EDGE_ROWS = 10                              # how far down the left side it runs

# The arrow's body in pixel coordinates (y down), inside the outline: the tip at (1, 1) so that the
# outline's corner pixel, the hotspot, is (0, 0).
BODY = [(1.0, 1.0), (1.0, 13.2), (4.0, 10.4), (6.6, 15.0), (8.4, 14.2), (6.0, 9.6), (10.4, 9.6)]


def body():
    """The steel body: a plate over the arrow polygon with a rounded bevel, shaded on the ramp."""
    w, h = SIZE
    poly = [(x - w / 2, h / 2 - y) for x, y in BODY]

    def scene(p):
        return sdf.sd_plate(p, poly, 0.0, 2.0, round_=2.2), ui_kit.ones(p)
    return ui_kit.shaded(ui_kit.render(scene, SIZE), ui_kit.STEEL_RAMP, ui_kit.BRIGHT)


def pointer():
    """The body with its lit left edge and the dark 1 px outline (8-neighbour, so (0, 0) is set)."""
    rgba = body().astype(np.int32)
    solid = rgba[..., 3] > 0
    # The lit edge: the body's straight left side, down to where the tail's notch starts.
    rows = np.nonzero(solid[:, 1])[0]
    rgba[rows[0]:rows[0] + EDGE_ROWS, 1] = EDGE
    grown = solid.copy()
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            shifted = np.zeros_like(solid)
            ys = slice(max(dy, 0), solid.shape[0] + min(dy, 0))
            yd = slice(max(-dy, 0), solid.shape[0] + min(-dy, 0))
            xs = slice(max(dx, 0), solid.shape[1] + min(dx, 0))
            xd = slice(max(-dx, 0), solid.shape[1] + min(-dx, 0))
            shifted[ys, xs] = solid[yd, xd]
            grown |= shifted
    rgba[grown & ~solid] = OUTLINE
    rgba[~grown] = 0
    return Image.fromarray(rgba.astype(np.uint8), "RGBA")


def build():
    img = pointer()
    alpha = np.array(img)[..., 3]
    assert alpha[0, 0] == 255, "the hotspot (0, 0) must be opaque"
    ys, xs = np.nonzero(alpha)
    OUT.parent.mkdir(parents=True, exist_ok=True)
    artkit.save_png(img, OUT, SOURCE)
    print(f"pointer: {img.width}x{img.height}, arrow {xs.max() + 1}x{ys.max() + 1}, "
          f"{artkit.colour_count([img])} colours")


# --------------------------------------------------------------------------- review

def menu_item():
    """A crop of a glass panel with a selected menu item, as the main menu draws it."""
    scene = Image.open(ROOT / "assets" / "ui" / "title-scene.png").convert("RGBA").crop((560, 200, 760, 290))
    arr = np.array(scene).astype(np.float64)
    arr[..., :3] *= 0.5
    img = Image.fromarray(arr.astype(np.uint8), "RGBA")
    ui_kit.patch(img, "glass", 10, 10, 180, 70)
    ui_kit.patch(img, "frame", 10, 10, 180, 70)
    ui_kit.patch(img, "selection", 12, 30, 176, 26)
    ui_kit.patch(img, "cursor-large", 20, 35)
    return img


def backdrops():
    """A bright and a dark backdrop, 120x90 each: a sky gradient and the near-black field."""
    bright = np.zeros((90, 120, 4), np.uint8)
    bright[..., 0] = np.linspace(200, 250, 90)[:, None]
    bright[..., 1] = np.linspace(220, 245, 90)[:, None]
    bright[..., 2] = 255
    bright[..., 3] = 255
    dark = np.zeros((90, 120, 4), np.uint8)
    dark[..., :3] = (4, 6, 18)
    dark[..., 3] = 255
    return [("GLASS MENU ITEM", menu_item()), ("BRIGHT", Image.fromarray(bright, "RGBA")),
            ("DARK", Image.fromarray(dark, "RGBA"))]


def review():
    cur = Image.open(OUT).convert("RGBA")
    sheet = raster.sheet(940, 680, "MOUSE POINTER (Q6 B): GLASS ARROW, HOTSPOT AT ITS TIP",
                         f"PRODUCTION ART, UI BATCH - {ROUND.upper()}")
    raster.draw_text(sheet, 16, 38, "THE POINTER ALONE, 1X 4X 8X (16X16 CANVAS ON A CHECKER)", raster.LABEL)
    x = 16
    for zoom in (1, 4, 8):
        sheet.alpha_composite(artkit.on_background(cur, zoom), (x, 54))
        x += 16 * zoom + 24
    y = 200
    for zoom in (1, 2, 3):
        raster.draw_text(sheet, 16, y, f"{zoom}X, AS THE HARDWARE CURSOR AT PIXEL SCALE {zoom}", raster.LABEL)
        x = 16
        for name, back in backdrops():
            # A 100x60 crop round the middle, with the pointer's tip over the selection bar.
            cx, cy = (58, 38) if name.startswith("GLASS") else (back.width // 2, back.height // 2)
            crop = back.crop((cx - 50, cy - 30, cx + 50, cy + 30))
            crop.alpha_composite(cur, (50, 26))
            big = sprite.enlarge(crop, zoom) if zoom > 1 else crop
            sheet.alpha_composite(big, (x, y + 14))
            raster.draw_text(sheet, x, y + 16 + big.height, name, raster.LABEL_DIM)
            x += big.width + 16
        y += 14 + 60 * zoom + 28
    path = CONCEPT / f"pointer-{ROUND}-a.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"review: {path.relative_to(ROOT)}")


if __name__ == "__main__":
    if "--review" not in sys.argv[1:]:
        build()
    review()
