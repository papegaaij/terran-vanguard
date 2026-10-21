#!/usr/bin/env python3
"""Production art: the act title cards' chrome lettering (round 44 X.l, approved by the user on 2026-10-20: moved
here from the concept tool tools/concept/ui_assets.py, the pixels unchanged).

Outputs (assets/ui/, PNGs in Git LFS, each with the ``Source`` chunk that marks final art):
  assets/ui/<act>-title.png   an act title card's chrome lettering (the act number above the act's name) on a
                              transparent canvas, one per act data file (design/campaign/act-*/data.yaml)

The lettering uses the chosen act title card's chrome treatment (tools/concept/ui_r08.py's chrome_title,
act-title-r08-a, imported unchanged) with the act number and name of the act's `title_card`; the game draws the
still, the rules and the smaller lines itself.

Run: python3 tools/art/act_titles.py [--check]   (--check: compare the pixels with the files in assets/ui/,
write nothing)
"""
import sys

import numpy as np
import yaml
from PIL import Image

import artkit
from artkit import ROOT

import ui_r08  # noqa: E402  (concept: the chrome title treatment, frozen)

SCRIPT = "act_titles.py"
SOURCE = artkit.source_note(SCRIPT, "UI batch")
UI = ROOT / "assets" / "ui"
ACT_NAME_Y = 76                     # the act name's top below the act number's top on act-title-r08-a (214 - 138)


def title(card):
    """The chrome act number and name of an act's title card, stacked and centred as on the concept."""
    act = ui_r08.chrome_title(card["act"], 70, tracking=30, depth=6, width=230 * len(card["act"]) // 6)
    name = ui_r08.chrome_title(card["name"], 150, tracking=16, depth=14)
    if name.width > 760:
        name = name.resize((760, int(name.height * 760 / name.width)), Image.LANCZOS)
    width = max(act.width, name.width)
    img = Image.new("RGBA", (width, ACT_NAME_Y + name.height), (0, 0, 0, 0))
    img.alpha_composite(act, ((width - act.width) // 2, 0))
    img.alpha_composite(name, ((width - name.width) // 2, ACT_NAME_Y))
    return img


def main(check=False):
    differ = 0
    for data in sorted((ROOT / "design" / "campaign").glob("act-*/data.yaml")):
        card = yaml.safe_load(data.read_text(encoding="utf-8"))["title_card"]
        img = title(card)
        path = UI / f"{data.parent.name}-title.png"
        if check:
            same = path.exists() and np.array_equal(np.array(Image.open(path).convert("RGBA")), np.array(img))
            differ += not same
            print(f"{path.relative_to(ROOT)}: {'same' if same else 'DIFFERS'}")
            continue
        path.parent.mkdir(parents=True, exist_ok=True)
        artkit.save_png(img, path, SOURCE)
        print("wrote", path.relative_to(ROOT))
    return differ


if __name__ == "__main__":
    sys.exit(1 if main("--check" in sys.argv[1:]) else 0)
