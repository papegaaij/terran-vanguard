#!/usr/bin/env python3
"""Concept round 70, rows B.1 and B.2: Level 29's friendly marker for the wavering Ceres Wing Stormhawks and the HUD
label of the "spare them" secondary (design/campaign/act-5-the-belt/level-29-betrayal-at-ceres, *Objective*).

Input (a real game still, not hand-edited): concept/friendly-marker-capture-r70-a.png, two 960x540 game frames side by
side, shot with `--level 29 --invulnerable --shots 4500,4800` (auto-fire off, medium, the debug start's fit) on Xvfb:
left t=75.0 s (the pincer's banner "WARNING - HOSTILES FROM THE LEFT AND RIGHT", the first wavering pair not yet in;
its first pixel is t=77.2), right t=80.0 s (the first pair holding at 150 px below the top among shielded Talons).
The pair's craft are found by matching the production sprites (assets/sprites/wavering-stormhawk-iff_*.png); the
text is drawn with the game's own bitmap fonts (assets/fonts/*.fnt) in the HUD's colours (Glass.GREEN, AMBER, ALERT).

Variants (each: the full play field at 1:1, the pair's 1:1 crop and the same crop at 2x):
  a  IFF bracket: HUD-green corner brackets round each wavering craft while it is on the screen, a small FRIENDLY tag
     under each bracket.
  b  friendly glow: a soft green-white outline round the craft's silhouette and much bigger, brighter white blinkers
     (canopy and both wingtips, 1 Hz, drawn lit).
  c  pre-entry banner: "FRIENDLIES INBOUND - HOLD FIRE" in the warning banner's shape, CDF green instead of amber,
     about 2 s before each pair; drawn in its own slot under the hostile banner (the t=77 pincer's banner shows then).
  d  c + a: the banner at t=75 and the brackets at t=80 (two fields).

Outputs (design/campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept/):
  friendly-marker-r70-a.png, -b.png, -c.png, -d.png   one variant each
  friendly-marker-r70-abcd.png                        the four side by side: half-size fields over the 1:1 crops
  spare-label-r70-abc.png                             the left panel's secondary box with three labels (a SPARE
                                                      WHITES as built, b SPARE ALLIES, c DON'T SHOOT), each counting
                                                      "2 / 6" and FAILED, at 1:1 and 2x

Run: python3 tools/concept/friendly_marker_r70.py   (a few seconds)
"""
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "design/campaign/act-5-the-belt/level-29-betrayal-at-ceres/concept"
SPRITES = ROOT / "assets/sprites"
FONTS = ROOT / "assets/fonts"

GREEN = (80, 255, 120)
GREEN_DARK = (6, 34, 16)
AMBER = (255, 200, 0)
ALERT = (255, 80, 60)
WHITE = (224, 230, 255)
PANEL_BG = (8, 9, 32)
FIELD = (240, 0, 720, 540)  # the play field in the 960x540 frame
CROP = (90, 95, 390, 205)  # the pair's 1:1 crop, field coordinates (300x110)


class Font:
    """A BMFont text file + its page, drawn tinted (the game's fonts are white on alpha)."""

    def __init__(self, name):
        self.chars = {}
        page = None
        for line in (FONTS / f"{name}.fnt").read_text().splitlines():
            parts = line.split()
            if not parts:
                continue
            kv = dict(p.split("=", 1) for p in parts[1:] if "=" in p)
            if parts[0] == "page":
                page = kv["file"].strip('"')
            elif parts[0] == "char":
                self.chars[int(kv["id"])] = {k: int(v) for k, v in kv.items() if k != "letter"}
            elif parts[0] == "common":
                self.line = int(kv["lineHeight"])
        self.page = Image.open(FONTS / page).convert("RGBA")

    def width(self, text):
        return sum(self.chars.get(ord(c), self.chars[32])["xadvance"] for c in text)

    def draw(self, img, x, y, text, color):
        for ch in text:
            c = self.chars.get(ord(ch), self.chars[32])
            if c["width"] and c["height"]:
                g = self.page.crop((c["x"], c["y"], c["x"] + c["width"], c["y"] + c["height"]))
                tint = Image.new("RGBA", g.size, color + (255,))
                tint.putalpha(g.getchannel("A"))
                img.paste(tint, (x + c["xoffset"], y + c["yoffset"]), tint)
            x += c["xadvance"]


BODY = Font("body-10x20")
LABEL = Font("label-8x12")


def frames():
    both = Image.open(OUT / "friendly-marker-capture-r70-a.png").convert("RGB")
    return both.crop((0, 0, 960, 540)), both.crop((960, 0, 1920, 540))


def find_pair(frame):
    """The two wavering craft: (heading index, centre x, centre y) in frame pixels, by exact sprite match."""
    fr = np.asarray(frame).astype(float)
    found = []
    for gx, gy in ((400, 150), (560, 150)):
        best = (1e18, 0, gx, gy)
        for k in range(80):
            sp = np.asarray(Image.open(SPRITES / f"wavering-stormhawk-iff_{k}.png").convert("RGBA")).astype(float)
            m = sp[..., 3] > 200
            for dx in range(-6, 7):
                for dy in range(-6, 7):
                    x0, y0 = gx - 20 + dx, gy - 20 + dy
                    d = np.abs(fr[y0:y0 + 40, x0:x0 + 40] - sp[..., :3])[m].mean()
                    if d < best[0]:
                        best = (d, k, x0 + 20, y0 + 20)
        assert best[0] < 1.0, f"no wavering craft near {gx},{gy}"
        found.append(best[1:])
    return found


def bracket(img, cx, cy, half=26, arm=9):
    d = ImageDraw.Draw(img)
    for sx in (-1, 1):
        for sy in (-1, 1):
            x, y = cx + sx * half, cy + sy * half
            for w, col in ((4, (0, 20, 8)), (2, GREEN)):
                d.line([(x, y), (x - sx * arm, y)], fill=col, width=w)
                d.line([(x, y), (x, y - sy * arm)], fill=col, width=w)
    tag = "FRIENDLY"
    w = LABEL.width(tag)
    tx, ty = cx - w // 2, cy + half + 4
    d.rectangle([tx - 3, ty - 1, tx + w + 1, ty + 10], fill=(0, 20, 8), outline=(30, 110, 50))
    LABEL.draw(img, tx, ty, tag, GREEN)


def glow(img, k, cx, cy):
    sp = Image.open(SPRITES / f"wavering-stormhawk-iff_{k}.png").convert("RGBA")
    a = sp.getchannel("A").point(lambda v: 255 if v > 90 else 0)
    pad = 12
    big = Image.new("L", (40 + 2 * pad, 40 + 2 * pad), 0)
    big.paste(a, (pad, pad))
    outer = big.filter(ImageFilter.MaxFilter(5))
    ring = Image.fromarray(np.clip(np.asarray(outer, int) - np.asarray(big, int), 0, 255).astype(np.uint8))
    halo = outer.filter(ImageFilter.GaussianBlur(4)).point(lambda v: int(v * 0.55))
    x0, y0 = cx - 20 - pad, cy - 20 - pad
    img.paste(Image.new("RGB", big.size, (120, 255, 160)), (x0, y0), halo)
    img.paste(Image.new("RGB", big.size, (210, 255, 220)), (x0, y0), ring)
    img.paste(sp, (cx - 20, cy - 20), sp)
    # the blinkers: canopy and both wingtips, big and lit
    arr = np.asarray(sp.getchannel("A"))
    ys, xs = np.nonzero(arr > 200)
    row = int(np.median(ys[(xs == xs.min()) | (xs == xs.max())]))
    lights = [(cx - 0.5, cy - 4.5), (cx - 20 + xs.min() + 1, cy - 20 + row), (cx - 20 + xs.max() - 1, cy - 20 + row)]
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    d = ImageDraw.Draw(layer)
    for lx, ly in lights:
        d.ellipse([lx - 10, ly - 10, lx + 10, ly + 10], fill=(255, 255, 255, 150))
    layer = layer.filter(ImageFilter.GaussianBlur(3))
    img.paste(layer, (0, 0), layer)
    d = ImageDraw.Draw(img)
    for lx, ly in lights:
        d.ellipse([lx - 3.5, ly - 3.5, lx + 3.5, ly + 3.5], fill=(255, 255, 255))


def banner(img, text="FRIENDLIES INBOUND - HOLD FIRE"):
    """The warning banner's shape (WaveBanners) in CDF green, in its own slot under the hostile band (y 150-179)."""
    text = text.replace("-", "—") if "—" in "".join(chr(c) for c in BODY.chars) else text
    w = BODY.width(text)
    x0, x1 = 240 + 20, 720 - 20
    y0, y1 = 186, 214
    d = ImageDraw.Draw(img)
    band = Image.new("RGBA", (x1 - x0, y1 - y0), GREEN_DARK + (225,))
    img.paste(band, (x0, y0), band)
    d.rectangle([x0, y0, x1 - 1, y1 - 1], outline=GREEN, width=2)
    BODY.draw(img, (x0 + x1) // 2 - w // 2, y0 + 4, text, GREEN)


def variant(name, f75, f80, pair):
    if name == "a":
        im = f80.copy()
        for k, cx, cy in pair:
            bracket(im, cx, cy)
        return [im]
    if name == "b":
        im = f80.copy()
        for k, cx, cy in pair:
            glow(im, k, cx, cy)
        return [im]
    if name == "c":
        im = f75.copy()
        banner(im)
        return [im, f80.copy()]
    im = f75.copy()
    banner(im)
    im2 = f80.copy()
    for k, cx, cy in pair:
        bracket(im2, cx, cy)
    return [im, im2]


SHORT = {"a": "a  IFF bracket", "b": "b  glow + blinkers", "c": "c  pre-entry banner", "d": "d  c + a"}


def caption(img, x, y, text, color=WHITE):
    LABEL.draw(img, x, y, text, color)


TITLES = {
    "a": "a  IFF bracket + FRIENDLY tag (t=80)",
    "b": "b  friendly glow + big blinkers (t=80)",
    "c": "c  pre-entry banner (t=75), the pair at t=80",
    "d": "d  c + a: the banner (t=75), the brackets (t=80)",
}


def sheet(name, ims):
    fields = [im.crop(FIELD) for im in ims]
    show = fields[-1]
    crop = show.crop(CROP)
    if name == "c":
        crop = fields[0].crop((0, 140, 480, 230))  # the banners: the hostile one and the friendly slot
    zoom = 2 if crop.width <= 300 else 1  # c's banner crop is 480 wide: 1:1 only
    w = 480 * len(fields) + 16 + crop.width * zoom
    out = Image.new("RGB", (w, max(540 + 24, 70 + crop.height * 3 + 8)), (14, 16, 26))
    caption(out, 6, 6, TITLES[name])
    for i, f in enumerate(fields):
        out.paste(f, (i * 480, 24))
    x = 480 * len(fields) + 16
    caption(out, x, 30, "1:1")
    out.paste(crop, (x, 44))
    if zoom == 2:
        caption(out, x, 44 + crop.height + 10, "2x")
        out.paste(crop.resize((crop.width * 2, crop.height * 2), Image.NEAREST), (x, 70 + crop.height))
    return out, fields, crop


def label_sheet(f80):
    box = (0, 448, 240, 488)  # the secondary box on the left panel
    base = f80.crop(box)
    rows = [("a", "SPARE WHITES", "built"), ("b", "SPARE ALLIES", ""), ("c", "DON'T SHOOT", "")]
    cell = []
    for v, text, note in rows:
        pair = []
        for count, col in (("2 / 6", WHITE), ("FAILED", ALERT)):
            im = base.copy()
            d = ImageDraw.Draw(im)
            d.rectangle([17, 9, 222, 30], fill=PANEL_BG)
            BODY.draw(im, 22, 8, text, WHITE)
            BODY.draw(im, 222 - BODY.width(count) - 2, 8, count, col)
            pair.append(im)
        cell.append((v, text, note, pair))
    out = Image.new("RGB", (16 + 240 * 2 + 24 + 480 * 2, 40 + len(cell) * 96), (14, 16, 26))
    for r, (v, text, note, pair) in enumerate(cell):
        y = 40 + r * 96
        caption(out, 6, y - 16, f"{v}  {text}" + (f"  ({note})" if note else ""))
        for i, im in enumerate(pair):
            out.paste(im, (6 + i * 244, y))
            out.paste(im.resize((480, 80), Image.NEAREST), (16 + 240 * 2 + 24 + i * 484, y))
    caption(out, 6, 4, "the secondary box on the left panel, 1:1 (left) and 2x (right); counting and failed")
    return out


def main():
    f75, f80 = frames()
    pair = find_pair(f80)
    parts = {}
    for name in "abcd":
        out, fields, crop = sheet(name, variant(name, f75, f80, pair))
        out.save(OUT / f"friendly-marker-r70-{name}.png", optimize=True)
        parts[name] = (fields[0], fields[-1].crop(CROP))
    # the four side by side: half-size first fields (c, d: the banner frame) and the 1:1 pair crops
    W = 4 * 300 + 3 * 12
    out = Image.new("RGB", (W, 24 + 270 + 30 + 110 + 10), (14, 16, 26))
    for i, name in enumerate("abcd"):
        x = i * 312
        caption(out, x + 2, 6, SHORT[name])
        f = parts[name][0].resize((240, 270), Image.LANCZOS)
        out.paste(f, (x + 30, 24))
        caption(out, x + 2, 24 + 270 + 12, "1:1 at t=80")
        out.paste(parts[name][1], (x, 24 + 270 + 26))
    out.save(OUT / "friendly-marker-r70-abcd.png", optimize=True)
    label_sheet(f80).save(OUT / "spare-label-r70-abc.png", optimize=True)


if __name__ == "__main__":
    main()
