#!/usr/bin/env python3
"""Concept round 67, row G.1 (QG-1, re-shown 2026-10-29): the Warden Tank in its Act 5 livery on Level 34's hulls,
all three variants rendered side by side (design/campaign/act-5-the-belt/level-34-salvage-run, *Open questions*).

  a (built, recommended): the Helix hulls a step lighter than round 60's black (charcoal grey-gold faces, sunlit gold
    trim), the tank roads plain steel: the production pieces in assets/backdrop/level-34/ as they are
    (tools/art/backdrop_l34.py).
  b: the tank roads on the CDF hulls only: the Helix hulls keep round 60's black and carry no tank road; the convoys
    that drive a Helix keel in a (t 70, 88, 123, 140) move to CDF walkways.
  c: round 60's black Helix hulls with their roads, and a faint ground shadow under every tank (the hull's silhouette
    offset 4, 5 px down-right, away from the top-left sun, blurred, 50 % black).

b's and c's Helix pieces are re-rendered here by backdrop_l34.py's own piece code with the Helix ramp put back to
round 60's black (the darker ramp below, the gold trim kept) and, for b, the Helix roads left out; nothing is written
to assets/. The tanks are the production frames (warden-tank-asc, its turret and wreck: tools/art/warden_tank.py
--livery asc), the stand-ins and the clock backdrop_l34.py's.

Outputs (design/campaign/act-5-the-belt/level-34-salvage-run/concept/):
  tank-readability-r67-b.png, -c.png   each variant as tank-readability-final-r67-a.png: the six grounds at 1:1 (the
                                       livery at headings 8, 6, 12, 0 and the wreck), then three game screens at half
                                       size (the CDF picket t 53.5, the Helix keel t 74, the freighter's deck t 127)
  tank-readability-r67-abc.png         a | b | c: the same six crops at 1:1, one row per variant, the same tank headings

Run: python3 tools/concept/tank_readability_r67.py   (about a minute; the Helix pieces cached in the system temp dir,
     conceptgame-cache/tank-readability-r67/, by this file's and backdrop_l34.py's hash)
"""
import hashlib
import sys
import tempfile
from concurrent.futures import ProcessPoolExecutor
from pathlib import Path

import numpy as np
from PIL import Image, ImageFilter

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent / "art"))
sys.path.insert(1, str(HERE))
import backdrop_l34 as bl  # noqa: E402
import belt_units as bu  # noqa: E402
from render import raster  # noqa: E402

CONCEPT = bl.CONCEPT
CACHE = Path(tempfile.gettempdir()) / "conceptgame-cache" / "tank-readability-r67"
# round 60's black Helix hulls (the L28 cruisers' charcoal black under the same sun), the gold trim kept
HELIX_BLACK = [(0.0, (9, 9, 11)), (0.35, (22, 21, 23)), (0.62, (34, 33, 34)), (0.82, (48, 45, 43)),
               (1.0, (64, 59, 52))]
HELIX_SUPER_BLACK = (40, 38, 36)
HELIX_PIECES = ["helix-hulk-a", "helix-hulk-b", "freighter-stern", "freighter-bow", "tangle-a", "tangle-b",
                "sable-wren", "old-marta", "plate-helix"]
HELIX_CONVOYS = (70, 88, 123, 140)          # the convoys on a Helix road (b moves them to CDF walkways)
GROUNDS = [("HELIX KEEL ROAD", "helix-hulk-a", 176, 820), ("HELIX TRIM + FACE", "helix-hulk-a", 40, 520),
           ("HELIX BAY RIBS", "helix-hulk-a", 80, 700), ("FREIGHTER DECK", "freighter-bow", 190, 600),
           ("CDF WALKWAY", "cdf-frigate-a", 92, 700), ("CDF FLANK", "cdf-frigate-a", 34, 560)]
POSES = ((8, 0), (6, 4), (12, 20), (0, 16))  # hull heading, turret aim (as the a sheet)
CW, CH = 116, 400
SCREENS = [(53.5, "CDF PICKET, T 53.5"), (74.0, "HELIX KEEL, T 74"), (127.0, "FREIGHTER DECK, T 127")]
TITLE = {"a": "A: THE HELIX HULLS A STEP LIGHTER (BUILT)", "b": "B: TANK ROADS ON THE CDF HULLS ONLY",
         "c": "C: A FAINT GROUND SHADOW UNDER EVERY TANK"}
NOTE = {"a": "GREY-GOLD HELIX FACES, SUNLIT GOLD TRIM, PLAIN STEEL ROADS (THE PRODUCTION PIECES)",
        "b": "ROUND 60'S BLACK HELIX HULLS WITHOUT ROADS: NO TANK ON THEM; THE T 70/88/123/140 CONVOYS MOVE TO CDF WALKWAYS",
        "c": "ROUND 60'S BLACK HELIX HULLS WITH THEIR ROADS; EACH TANK CASTS A SOFT SHADOW DOWN-RIGHT (50 % BLACK)"}


def _key():
    h = hashlib.sha1(Path(__file__).read_bytes() + Path(bl.__file__).read_bytes())
    return h.hexdigest()[:12]


def _variant(v):
    """Put backdrop_l34's Helix look back to round 60's black (b, c); b also draws no road on a Helix part."""
    bl.HELIX = HELIX_BLACK
    bl.HELIX_SUPER = HELIX_SUPER_BLACK
    if v == "b":
        road = bl.Hull.road

        def no_helix_road(self, pts, kind="cdf"):
            if kind != "helix":
                road(self, pts, kind)
        bl.Hull.road = no_helix_road


def _job(job):
    v, name = job
    _variant(v)
    img = bl.plate(name) if name.startswith("plate") else bl._render_job(name)[0]
    return img


def helix_pieces(v):
    """b's or c's Helix pieces (name -> image), rendered once and cached."""
    CACHE.mkdir(parents=True, exist_ok=True)
    key = _key()
    paths = {n: CACHE / f"{key}-{v}-{n}.png" for n in HELIX_PIECES}
    todo = [n for n, p in paths.items() if not p.exists()]
    if todo:
        with ProcessPoolExecutor() as pool:
            for n, img in zip(todo, pool.map(_job, [(v, n) for n in todo], chunksize=1)):
                img.save(paths[n])
    return {n: Image.open(p).convert("RGBA") for n, p in paths.items()}


def shadow(frame, dx=4, dy=5, k=0.5):
    """The tank's ground shadow: its silhouette offset away from the top-left sun, blurred, k black."""
    a = frame.getchannel("A").point(lambda p: 255 if p > 40 else 0)
    pad = Image.new("L", (frame.width + 8, frame.height + 8), 0)
    pad.paste(a, (4 + dx, 4 + dy))
    pad = pad.filter(ImageFilter.GaussianBlur(1.6)).point(lambda p: int(p * k))
    out = Image.new("RGBA", pad.size, (0, 0, 0, 0))
    out.putalpha(pad)
    return out


def tank(img, hull, turret, x, y, shade):
    if shade:
        img = bu.paste(img, shadow(hull), x, y)
    img = bu.paste(img, hull, x, y)
    return bu.paste(img, turret, x, y) if turret is not None else img


def frames():
    h, t, w = bl._held("warden-tank-asc"), bl._held("warden-tank-asc-turret"), bl._held("warden-tank-asc-wreck")
    if not h:
        sys.exit("no warden-tank-asc frames (tools/art/warden_tank.py --livery asc)")
    return h, t, w


def piece(v, name, pieces, alt):
    if v != "a" and name in alt:
        return alt[name]
    return bl.images(name, pieces[name])[0]


def crop_cell(v, ground, pieces, alt, tanks, space):
    lab, name, px, py = ground
    hull, tur, wreck = tanks
    src = piece(v, name, pieces, alt)
    cell = space.crop((0, 0, CW, CH))
    cell.alpha_composite(src.crop((px - CW // 2, py - CH // 2, px + CW // 2, py + CH // 2)))
    if v == "b" and name != "cdf-frigate-a":
        return cell, ("NO ROAD:", "NO TANKS HERE")
    for k, (hd, aim) in enumerate(POSES):
        cell = tank(cell, hull[hd * 2], tur[aim * 2], CW // 2, 44 + k * 78, v == "c")
    cell = tank(cell, wreck[8], None, CW // 2, 44 + 4 * 78, v == "c")
    return cell, None


def screen(v, t, pieces, placed, data, tanks):
    """A game screen at t with the stand-ins; the tanks drawn here (b: none on a Helix road; c: with shadows)."""
    hull, tur, _ = tanks
    quiet = dict(data, convoys=[])
    img = bl.compose(t, pieces, placed, quiet)
    ship_x = 240 + 140 * np.sin(t * 0.5)
    for c in data["convoys"]:
        if v == "b" and c["t"] in HELIX_CONVOYS:
            continue
        n = 2 if c["t"] in (32, 70, 123) else 3
        for i in range(n):
            tu = c["t"] + i * bl.INTERVAL
            if t < tu:
                continue
            x, y, ang = bl.tank_at(c["paths"][i], tu, t)
            if -50 < y < bl.SCREEN + 50:
                aim = np.arctan2(440 - y, ship_x - x)
                img = tank(img, hull[bl.heading_frame(ang, 16) * 2 + int(t * 9) % 2],
                           tur[bl.heading_frame(aim, 32) * 2], x, y, v == "c")
    return img


def variant_sheet(v, pieces, placed, data, alt, tanks, space):
    sw, sh = bl.W // 2, bl.SCREEN // 2
    width = 16 + len(GROUNDS) * (CW + 12) + 4
    sheet = raster.sheet(width, 60 + CH + 24 + 20 + sh + 30, f"QG-1 {TITLE[v]}", "CONCEPT, ROUND 67 G.1 - R67")
    raster.draw_text(sheet, 16, 38, NOTE[v], raster.LABEL)
    for i, g in enumerate(GROUNDS):
        cell, note = crop_cell(v, g, pieces, alt, tanks, space)
        sx, sy = 16 + i * (CW + 12), 56
        sheet.alpha_composite(cell, (sx, sy))
        raster.draw_text(sheet, sx, sy + CH + 4, g[0], raster.LABEL_DIM)
        if note:
            raster.draw_text(sheet, sx + 4, sy + CH // 2, note[0], raster.LABEL)
            raster.draw_text(sheet, sx + 4, sy + CH // 2 + 12, note[1], raster.LABEL)
    y = 56 + CH + 24
    raster.draw_text(sheet, 16, y, "IN THE LEVEL AT HALF SIZE (THE STAND-INS: TANKS ON THEIR ROADS, OBJECTS, THE SHIP)",
                     raster.LABEL)
    for i, (t, lab) in enumerate(SCREENS):
        im = screen(v, t, pieces, placed, data, tanks).resize((sw, sh), Image.BOX)
        x = 16 + i * (sw + 12)
        sheet.alpha_composite(im, (x, y + 16))
        raster.draw_text(sheet, x, y + 16 + sh + 4, lab, raster.LABEL_DIM)
    return sheet


def abc_sheet(pieces, alts, tanks, space):
    width = 16 + len(GROUNDS) * (CW + 12) + 4
    sheet = raster.sheet(width, 60 + 3 * (CH + 40), "QG-1: A | B | C, THE SAME CROPS AT 1:1",
                         "CONCEPT, ROUND 67 G.1 - R67")
    raster.draw_text(sheet, 16, 38, "THE LIVERY AT HEADINGS 8 6 12 0 + WRECK ON EACH GROUND; ONE ROW PER VARIANT",
                     raster.LABEL)
    for j, v in enumerate("abc"):
        y = 56 + j * (CH + 40)
        raster.draw_text(sheet, 16, y, TITLE[v], (255, 210, 120))
        for i, g in enumerate(GROUNDS):
            cell, note = crop_cell(v, g, pieces, alts[v], tanks, space)
            sx = 16 + i * (CW + 12)
            sheet.alpha_composite(cell, (sx, y + 14))
            raster.draw_text(sheet, sx, y + 14 + CH + 4, g[0], raster.LABEL_DIM)
            if note:
                raster.draw_text(sheet, sx + 4, y + 14 + CH // 2, note[0], raster.LABEL)
                raster.draw_text(sheet, sx + 4, y + 26 + CH // 2, note[1], raster.LABEL)
    return sheet


def main():
    pieces, placed, data = bl.load_proposal()
    tanks = frames()
    space = Image.open(bl.OUT / "grave-space.png").convert("RGBA")
    alts = {"a": {}, "b": helix_pieces("b"), "c": helix_pieces("c")}
    for v in "bc":
        bl._CACHE.clear()
        bl._CACHE.update({n: [im] for n, im in alts[v].items()})   # compose() reads the variant's Helix pieces
        sheet = variant_sheet(v, pieces, placed, data, alts[v], tanks, space)
        path = CONCEPT / f"tank-readability-r67-{v}.png"
        sheet.convert("RGB").save(path, optimize=True)
        print(f"wrote {path.relative_to(bl.ROOT)} {sheet.size}")
    bl._CACHE.clear()
    sheet = abc_sheet(pieces, alts, tanks, space)
    path = CONCEPT / "tank-readability-r67-abc.png"
    sheet.convert("RGB").save(path, optimize=True)
    print(f"wrote {path.relative_to(bl.ROOT)} {sheet.size}")


if __name__ == "__main__":
    main()
