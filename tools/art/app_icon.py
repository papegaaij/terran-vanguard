#!/usr/bin/env python3
"""Production art: the application icon (design/tech/release, concept question Q9 of M6).

The AF-12 Stormhawk (design/player/ship) seen from above, rendered from the same model as its
production sprite (tools/concept/vfx_r08.py, level frame, no pods) at icon resolution, on a dark
navy tile with the title logo's blue trim (assets/ui/title-logo.png: steel blue, glow blue).

Outputs:
  assets/icon/icon-<16|32|48|64|128|256>.png   the window icons (LWJGL3 setWindowIcon picks the
                                               size the platform asks for); Source chunk
  desktop/icons/terran-vanguard.ico            the Windows exe icon (16-256 px)
  desktop/icons/terran-vanguard.icns           the macOS app icon (16-1024 px)
  design/tech/release/concept/app-icon-r35-a.png   review sheet: every size on dark and light

Every size is drawn from the 1024 px master; the sizes up to 48 px get a mild unsharp mask so
the hull's outline survives the reduction.

Run: python3 tools/art/app_icon.py   (~1 min)
"""

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import artkit
from artkit import DESIGN, ROOT, sprite

import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged)
from render import sdf  # noqa: E402
from render.palette import B  # noqa: E402

SCRIPT = "app_icon.py"
SOURCE = artkit.source_note(SCRIPT, "M6 release")
MASTER = 1024
RENDER = 1024                       # ship render (px) before the 2x box downsample
FACTOR = 2
EXTENT = 2.3                        # model units across the render, as the sprite
CAMERA = 4.0                        # the sprite's perspective camera
SHIP_FILL = 0.80                    # the ship's longer side as a share of the tile
WINDOW_SIZES = (16, 32, 48, 64, 128, 256)
ICO_SIZES = (16, 24, 32, 48, 64, 128, 256)
ICNS_SIZES = (16, 32, 64, 128, 256, 512, 1024)

# the title logo's colours (sampled from assets/ui/title-logo.png)
NAVY_TOP = (14, 24, 52)
NAVY_BOTTOM = (4, 7, 18)
TRIM = (51, 97, 168)
TRIM_LIGHT = (183, 199, 218)
GLOW = (80, 167, 254)

ICON_DIR = ROOT / "assets" / "icon"
BUNDLE_DIR = ROOT / "desktop" / "icons"
CONCEPT = DESIGN / "tech" / "release" / "concept"


def ship():
    """The Stormhawk's level frame as a float RGBA render, cropped to its hull."""
    scene, mats = v8.ship_model(0.0, None, B.ship_colors())
    hi = sdf.render(artkit.perspective(scene, CAMERA), mats, (RENDER * FACTOR, RENDER * FACTOR), EXTENT)
    img = sprite.to_image(sprite.downsample(hi, FACTOR))
    return img.crop(img.getchannel("A").getbbox())


def rounded_mask(size, radius, inset=0):
    mask = Image.new("L", (size, size), 0)
    ImageDraw.Draw(mask).rounded_rectangle([inset, inset, size - 1 - inset, size - 1 - inset], radius, fill=255)
    return mask


def tile():
    """The dark tile: navy gradient, a faint radial glow behind the ship, the logo's blue trim."""
    s = MASTER
    yy, xx = np.mgrid[0:s, 0:s].astype(np.float64) / (s - 1)
    arr = np.array(NAVY_TOP) * (1 - yy[..., None]) + np.array(NAVY_BOTTOM) * yy[..., None]
    d = np.hypot(xx - 0.5, yy - 0.48)
    arr += np.exp(-(d / 0.32) ** 2)[..., None] * np.array(GLOW) * 0.22
    base = Image.fromarray(np.clip(arr, 0, 255).astype(np.uint8), "RGB").convert("RGBA")
    radius = int(s * 0.18)
    out = Image.new("RGBA", (s, s), (0, 0, 0, 0))
    # trim: an outer steel-blue band, a light bevel line inside it
    out.paste(Image.new("RGBA", (s, s), TRIM + (255,)), (0, 0), rounded_mask(s, radius))
    band = int(s * 0.035)
    out.paste(Image.new("RGBA", (s, s), TRIM_LIGHT + (255,)), (0, 0), rounded_mask(s, radius - band // 3, band // 3))
    out.paste(Image.new("RGBA", (s, s), TRIM + (255,)), (0, 0), rounded_mask(s, radius - band // 2, band // 2))
    out.paste(base, (0, 0), rounded_mask(s, radius - band, band))
    return out


def master():
    hull = ship()
    scale = SHIP_FILL * MASTER / max(hull.size)
    hull = hull.resize((round(hull.width * scale), round(hull.height * scale)), Image.LANCZOS)
    img = tile()
    pos = ((MASTER - hull.width) // 2, (MASTER - hull.height) // 2 + int(MASTER * 0.01))
    # a soft drop shadow below and right of the ship (the key light comes from the top left)
    shadow = Image.new("RGBA", hull.size, (0, 0, 0, 0))
    shadow.putalpha(hull.getchannel("A").point(lambda a: int(a * 0.55)))
    shadow = shadow.filter(ImageFilter.GaussianBlur(MASTER * 0.012))
    img.alpha_composite(shadow, (pos[0] + int(MASTER * 0.02), pos[1] + int(MASTER * 0.03)))
    img.alpha_composite(hull, pos)
    return img


def scaled(img, size):
    out = img.resize((size, size), Image.LANCZOS)
    if size <= 48:
        alpha = out.getchannel("A")
        out = out.convert("RGB").filter(ImageFilter.UnsharpMask(radius=0.8, percent=80, threshold=1)).convert("RGBA")
        out.putalpha(alpha)
    return out


def review(sizes):
    pad = 24
    widths = [s for s in WINDOW_SIZES] + [512]
    w = sum(widths) + pad * (len(widths) + 1)
    h = 2 * (512 + 2 * pad)
    sheet = Image.new("RGBA", (w, h), (28, 30, 36, 255))
    ImageDraw.Draw(sheet).rectangle([0, h // 2, w, h], fill=(236, 238, 242, 255))
    for row in range(2):
        x = pad
        for s in widths:
            sheet.alpha_composite(sizes[s], (x, row * h // 2 + pad + 512 - s))
            x += s + pad
    return sheet.convert("RGB")


def build():
    big = master()
    sizes = {s: scaled(big, s) for s in sorted(set(WINDOW_SIZES + ICO_SIZES + ICNS_SIZES + (512,)))}
    ICON_DIR.mkdir(parents=True, exist_ok=True)
    BUNDLE_DIR.mkdir(parents=True, exist_ok=True)
    CONCEPT.mkdir(parents=True, exist_ok=True)
    for s in WINDOW_SIZES:
        artkit.save_png(sizes[s], ICON_DIR / f"icon-{s}.png", SOURCE)
    sizes[256].save(BUNDLE_DIR / "terran-vanguard.ico", sizes=[(s, s) for s in ICO_SIZES],
                    append_images=[sizes[s] for s in ICO_SIZES if s != 256])
    sizes[1024].save(BUNDLE_DIR / "terran-vanguard.icns", append_images=[sizes[s] for s in ICNS_SIZES if s != 1024])
    review(sizes).save(CONCEPT / "app-icon-r35-a.png", optimize=True)
    for path in [*(ICON_DIR / f"icon-{s}.png" for s in WINDOW_SIZES), BUNDLE_DIR / "terran-vanguard.ico",
                 BUNDLE_DIR / "terran-vanguard.icns", CONCEPT / "app-icon-r35-a.png"]:
        print(path.relative_to(ROOT), path.stat().st_size, "bytes")


if __name__ == "__main__":
    build()
