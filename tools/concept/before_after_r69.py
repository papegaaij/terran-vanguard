#!/usr/bin/env python3
"""Round 69's before | after sheets of three built side effects (M9 part I, step I3), each at the same view.

Outputs (concept renderings for the review, the game's files untouched):
  design/campaign/act-5-the-belt/level-34-salvage-run/concept/far-hulk-before-after-r69-a.png
      Level 34's far Helix hulk (frames 0, 4, 8, 12 of `far-hulk-turn`) and `far-wreck-b`: before = the files of
      commit 73c8a916 (round 67 a's grey-gold Helix), after = `assets/backdrop/level-34/` now (step FIX4's black Helix
      ramp, round 67 G.1 c), at 2x on the deep layer's dark.
  design/campaign/act-5-the-belt/level-35-iron-sovereign/concept/tenders-before-after-r69-a.png
      Level 35's Sovereign tenders: as built = `assets/backdrop/level-35/tender-a`, `tender-b` (FIX4 pinned them to the
      grey-gold ramp they were built with), beside the same tenders rendered by `tools/art/backdrop_l35.py`'s
      `tender()` with Level 34's black ramp (what they would be had they followed it), at 2x.
  design/campaign/act-5-the-belt/level-33-leech-field/concept/nest-face-before-after-r69-a.png
      Level 33's `nest-face-1`: before = `tools/art/backdrop_l33.py`'s face with step F's middle nest at (250, 230) and
      no hard pad, after = `assets/backdrop/level-33/nest-face-1.png` (step FIX3: hard's pad at x 240, the nest moved to
      (320, 160)), at 1:1, the pads and the nest marked on a third copy.

    python3 tools/concept/before_after_r69.py
"""
import io
import subprocess
import sys
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
sys.path.insert(0, str(ROOT / "tools" / "art"))
sys.path.insert(0, str(ROOT / "tools" / "concept"))
ACT5 = ROOT / "design" / "campaign" / "act-5-the-belt"
BEFORE_COMMIT = "73c8a916"
FONT = ImageFont.truetype("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf", 13)
PLATE = (12, 13, 18)


def git_png(path):
    data = subprocess.run(["git", "-C", str(ROOT), "show", f"{BEFORE_COMMIT}:{path}"], capture_output=True,
                          check=True).stdout
    if data.startswith(b"version https://git-lfs"):
        data = subprocess.run(["git", "-C", str(ROOT), "lfs", "smudge"], input=data, capture_output=True,
                              check=True).stdout
    return Image.open(io.BytesIO(data)).convert("RGBA")


def on_plate(im, scale, plate=PLATE):
    bg = Image.new("RGBA", im.size, plate + (255,))
    bg.alpha_composite(im.convert("RGBA"))
    if scale != 1:
        bg = bg.resize((im.width * scale, im.height * scale), Image.NEAREST)
    return bg.convert("RGB")


def row(ims, gap=8):
    w = sum(i.width for i in ims) + gap * (len(ims) - 1)
    h = max(i.height for i in ims)
    out = Image.new("RGB", (w, h), (24, 24, 30))
    x = 0
    for i in ims:
        out.paste(i, (x, 0))
        x += i.width + gap
    return out


def labelled(blocks, title):
    """blocks: [(label, image)] stacked, a title line on top."""
    w = max(im.width for _, im in blocks) + 16
    h = 26 + sum(im.height + 24 for _, im in blocks)
    out = Image.new("RGB", (w, h), (24, 24, 30))
    d = ImageDraw.Draw(out)
    d.text((8, 5), title, font=FONT, fill=(255, 230, 140))
    y = 26
    for label, im in blocks:
        d.text((8, y + 3), label, font=FONT, fill=(220, 220, 220))
        out.paste(im, (8, y + 20))
        y += im.height + 24
    return out


def far_hulk():
    names = [f"far-hulk-turn_{i}" for i in (0, 4, 8, 12)] + ["far-wreck-b"]
    base = "assets/backdrop/level-34/"
    before = row([on_plate(git_png(base + n + ".png"), 2) for n in names])
    after = row([on_plate(Image.open(ROOT / base / (n + ".png")), 2) for n in names])
    return labelled([("before (round 67 a's grey-gold Helix, as committed)", before),
                     ("after (step FIX4: the black Helix ramp of G.1 c)", after)],
                    "Level 34: the far Helix hulk (turn frames 0, 4, 8, 12) and far-wreck-b, 2x")


def tenders():
    import backdrop_l34 as l34
    import backdrop_l35 as l35  # sets l34.HELIX to the grey-gold ramp on import
    base = ROOT / "assets/backdrop/level-35"
    built = row([on_plate(Image.open(base / "tender-a.png"), 2), on_plate(Image.open(base / "tender-b.png"), 2)])
    grey_gold = l34.HELIX
    l34.HELIX = [(0.0, (9, 9, 11)), (0.35, (22, 21, 23)), (0.62, (34, 33, 34)), (0.82, (48, 45, 43)),
                 (1.0, (64, 59, 52))]  # Level 34's black ramp (backdrop_l34.HELIX, G.1 c)
    black = row([on_plate(l35.tender(72, 148, 3571), 2), on_plate(l35.tender(64, 124, 3573), 2)])
    l34.HELIX = grey_gold
    return row([labelled([("as built (pinned grey-gold)", built)], "Level 35: the Sovereign's tenders, 2x"),
                labelled([("had they followed Level 34 (black)", black)], " ")], gap=0)


def nest_face():
    import backdrop_l33 as l33
    name = "nest-face-1"
    after = Image.open(ROOT / "assets/backdrop/level-33" / (name + ".png")).convert("RGBA")
    spec = list(l33.FACES[name])
    spec[6] = [(200, 140), (300, 330), (250, 230)]  # step F's middle nest
    saved_face, saved_pads = l33.FACES[name], l33.HARD_PADS.get(name)
    l33.FACES[name] = tuple(spec)
    l33.HARD_PADS.pop(name, None)
    before = l33.face(name)
    l33.FACES[name] = saved_face
    if saved_pads is not None:
        l33.HARD_PADS[name] = saved_pads
    marked = on_plate(after, 1)
    d = ImageDraw.Draw(marked)
    for x, y in [(100, 250), (380, 250), (240, 250)]:
        d.ellipse((x - 30, y - 30, x + 30, y + 30), outline=(120, 200, 255), width=2)
    for x, y in [(250, 230)]:
        d.ellipse((x - 14, y - 14, x + 14, y + 14), outline=(255, 90, 90), width=2)
    for x, y in [(320, 160)]:
        d.ellipse((x - 14, y - 14, x + 14, y + 14), outline=(255, 230, 120), width=2)
    return labelled([("before (step F: the middle nest at 250, 230; no pad for hard's third bunker)",
                      on_plate(before, 1)),
                     ("after (step FIX3: hard's pad at x 240; the nest moved to 320, 160)", on_plate(after, 1)),
                     ("after, marked: pads blue, the old nest place red, the new one yellow", marked)],
                    "Level 33: nest-face-1 (the t=132 nests), 1:1")


def main():
    outs = {
        ACT5 / "level-34-salvage-run/concept/far-hulk-before-after-r69-a.png": far_hulk,
        ACT5 / "level-35-iron-sovereign/concept/tenders-before-after-r69-a.png": tenders,
        ACT5 / "level-33-leech-field/concept/nest-face-before-after-r69-a.png": nest_face,
    }
    for path, fn in outs.items():
        im = fn()
        im.save(path, optimize=True)
        print(path.relative_to(ROOT), im.size)


if __name__ == "__main__":
    main()
