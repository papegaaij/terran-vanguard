#!/usr/bin/env python3
"""Concept round 74 (M10 part B, step m10-b5, 2026-10-31): QB-5, the harvester pair's drift on Level 36, as three
top-down path diagrams of one pair over the play field; pending the user's review.

Outputs (design/enemies/air/harvester/concept/):
  harvester-drift-r74-a.png  a, as built (step B2): mirrored 8 px/s apart, the hulls entering at x 160 / 320
  harvester-drift-r74-b.png  b, the README's 30 px/s the same way for both (no mirror), the pair entering at x 60 / 220
  harvester-drift-r74-c.png  c, 30 px/s toward each other for the first 1.5 s, then straight down

Each: the 480x540 play field at 1x (y down), the hulls' 72 px hit boxes every second from the top edge (50 px/s down,
the level's numbers) until they leave the field, the path of each hull's centre, the gap between the centres at the
start and at the end, and where a hull leaves (bottom: escaped; a side: off the field) or overlaps the other.
Deterministic (no randomness). Run: python3 tools/concept/harvester_drift_r74.py
"""
from pathlib import Path

from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[2]
OUT = ROOT / "design" / "enemies" / "air" / "harvester" / "concept"
W, H = 480, 540
VY = 50.0
HULL_W, HULL_H = 72, 24
PAD = 40


def path_a(t):
    return [160 - 8 * t, 320 + 8 * t]


def path_b(t):
    return [60 + 30 * t, 220 + 30 * t]


def path_c(t):
    k = min(t, 1.5)
    return [160 + 30 * k, 320 - 30 * k]


VARIANTS = {
    "a": ("a - as built: 8 px/s apart, mirrored", path_a),
    "b": ("b - 30 px/s the same way, from x 60 / 220", path_b),
    "c": ("c - 30 px/s together for 1.5 s, then down", path_c),
}


def draw(key):
    title, path = VARIANTS[key]
    img = Image.new("RGB", (W + 2 * PAD, H + 2 * PAD + 60), (18, 22, 30))
    d = ImageDraw.Draw(img)
    ox, oy = PAD, PAD + 30
    d.rectangle((ox, oy, ox + W, oy + H), fill=(150, 96, 62), outline=(230, 220, 200))
    d.text((PAD, 8), title, fill=(255, 220, 140))
    colours = [(120, 200, 255), (255, 170, 90)]
    t, step = 0.0, 0.05
    trail = [[], []]
    events = []
    gone = [None, None]
    while t < 14 and None in gone:
        xs = path(t)
        y = -HULL_H / 2 + VY * t
        for i, x in enumerate(xs):
            if gone[i] is not None:
                continue
            if x - HULL_W / 2 < 0 or x + HULL_W / 2 > W:
                gone[i] = ("side", t, x, y)
                continue
            if y - HULL_H / 2 > H:
                gone[i] = ("bottom", t, x, y)
                continue
            trail[i].append((ox + x, oy + y))
        if gone[0] is None and gone[1] is None and abs(xs[1] - xs[0]) < HULL_W and not events:
            events.append((t, (xs[0] + xs[1]) / 2, y))
        t += step
    for i in range(2):
        if len(trail[i]) > 1:
            d.line(trail[i], fill=colours[i], width=2)
    for s in range(0, 14):
        xs = path(s)
        y = -HULL_H / 2 + VY * s
        if y - HULL_H / 2 > H:
            break
        for i, x in enumerate(xs):
            if gone[i] is not None and gone[i][1] <= s:
                continue
            d.rectangle((ox + x - HULL_W / 2, oy + y - HULL_H / 2, ox + x + HULL_W / 2, oy + y + HULL_H / 2),
                        outline=colours[i])
        d.text((ox + 4, oy + y - 6), f"{s} s", fill=(240, 240, 240))
    x0 = path(0)
    notes = [f"start gap {abs(x0[1] - x0[0]):.0f} px"]
    for i in range(2):
        if gone[i] is not None:
            kind, tt, x, y = gone[i]
            notes.append(f"hull {i + 1} leaves by the {kind} at {tt:.1f} s, x {x:.0f}")
            d.ellipse((ox + x - 5, oy + min(y, H) - 5, ox + x + 5, oy + min(y, H) + 5), outline=(255, 80, 80), width=2)
    if events:
        tt, x, y = events[0]
        notes.append(f"hit boxes overlap from {tt:.1f} s")
        d.text((ox + x - 30, oy + y + 14), "overlap", fill=(255, 80, 80))
    for n, note in enumerate(notes):
        d.text((PAD, oy + H + 8 + 14 * n), note, fill=(220, 225, 235))
    OUT.mkdir(parents=True, exist_ok=True)
    img.save(OUT / f"harvester-drift-r74-{key}.png", optimize=True)
    print(key, notes)


if __name__ == "__main__":
    for key in VARIANTS:
        draw(key)
