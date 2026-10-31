#!/usr/bin/env python3
"""Concept round 73 - explosions over Level 36's bright bands (round 72's A.7 note; step ART-B's measure: the built
``explosion-small`` lifts the brightest cream only +34..+55, 27-33 % of its core clipped; a pale smudge).

Three proposals, each a short loop over three plates cut from the game's own Level 36 files
(``assets/backdrop/level-36``): the brightest cream band, a foreground bank on that cream (``bank-c`` drawn over it at
the bank's own alpha) and the rust band; in each plate a ``small`` (40 px) and a ``medium`` (64 px) burst, at the game's
frame timing (4 simulation steps a frame, 60 a second), shown 2x (the play field's scale on a 1080p screen):

  a "as built": the production frames (tools/art/explosions.py), additive only (GL_SRC_ALPHA, GL_ONE), unchanged;
  b "soot under the flash": the same additive frames over a soft, normal-blended soot puff: a warm dark grey, at most
    0.42 opaque, a smooth radial falloff with no edge (feathered to nothing over its outer 60 %), lumpy only by a
    low-frequency noise, in with the flash (under its peak, so the flash has a darker ground to light), growing and
    rising a few px as it fades out by 1.3x the flash's life;
  c "deeper core": the same frames recoloured toward a deeper, more saturated orange (Act 6 only), still additive.

Writes design/art-direction/concept/explosion-bright-r73-{a,b,c}.gif (the loop, 30 fps) and .png (the frames at key
moments and the measured lift). Every layer is a smooth float field drawn once (no ordered dither, no posterising).
Run: python3 tools/concept/explosion_bright_r73.py [a] [b] [c]   (no args = all three, ~10 s)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "art"))
import artkit  # noqa: E402
from render import enemy_rigs as rig  # noqa: E402
from render import raster  # noqa: E402
from render.config import ROOT  # noqa: E402

OUT = ROOT / "design" / "art-direction" / "concept"
L36 = ROOT / "assets" / "backdrop" / "level-36"
SUB = "CONCEPT ROUND 73"
NAMES = {"a": "A - AS BUILT (ADDITIVE ONLY)", "b": "B - SOOT UNDER THE FLASH (RECOMMENDED)",
         "c": "C - DEEPER ORANGE CORE (ADDITIVE)"}
P = 150                     # a plate's side, px at 1x
STEPS = 4                   # simulation steps a frame (EnemyLooks.DEATH_FRAME_TICKS)
LOOP = 96                   # steps in the loop (1.6 s)
BURSTS = (("small", 52, 62, 0), ("medium", 98, 92, 14))   # (rung, x, y, start step)
SOOT = np.array([58, 48, 42], np.float64)                 # warm dark grey
SOOT_ALPHA = 0.42


def plates():
    """The three 1x plates: (label, RGB float array)."""
    cream = Image.open(L36 / "cream-bands.png").convert("RGBA")
    lum = np.asarray(cream.convert("L"), np.float64)
    box = np.cumsum(np.cumsum(np.pad(lum, ((1, 0), (1, 0))), 0), 1)
    sums = box[P:, P:] - box[:-P, P:] - box[P:, :-P] + box[:-P, :-P]   # every 150 px window's sum
    top, left = np.unravel_index(int(sums.argmax()), sums.shape)          # the brightest window
    bright = cream.crop((left, top, left + P, top + P))
    bank = bright.copy()
    piece = Image.open(L36 / "bank-c.png").convert("RGBA")
    bank.alpha_composite(piece, ((P - piece.width) // 2, (P - piece.height) // 2 + 8))
    rust = Image.open(L36 / "rust-band.png").convert("RGBA")
    rl = np.convolve(np.asarray(rust.convert("L"), np.float64).mean(1), np.ones(P) / P, mode="valid")
    rtop = int(np.abs(rl - np.median(rl)).argmin())
    rust = rust.crop((165, rtop, 165 + P, rtop + P))
    return [("THE BRIGHTEST CREAM", np.asarray(bright.convert("RGB"), np.float64)),
            ("A BANK ON THE CREAM", np.asarray(bank.convert("RGB"), np.float64)),
            ("THE RUST BAND", np.asarray(rust.convert("RGB"), np.float64))]


def deeper(frame):
    """Variant c: a frame's light recoloured along a deeper, saturated orange ramp (its alpha kept)."""
    a = np.asarray(frame, np.float64)
    lum = (a[..., :3] @ np.array([0.30, 0.55, 0.15])) / 255.0
    stops = [(0.0, (0, 0, 0)), (0.30, (120, 26, 0)), (0.60, (235, 82, 6)), (0.85, (255, 142, 30)),
             (1.0, (255, 196, 110))]
    out = a.copy()
    out[..., :3] = raster.ramp(stops, lum)
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGBA")


def soot_field(size, seed):
    """A smooth, lumpy, edge-less soot mask on [0, 1], ``size`` px across."""
    n = raster.value_noise(size, size, max(4, size // 3), seed, period=False)
    yy, xx = np.mgrid[0:size, 0:size]
    c = (size - 1) / 2
    r = np.hypot(xx - c, yy - c) / (size / 2)
    r = r * (0.85 + 0.30 * (n - 0.5))                            # a low-frequency lumpiness, no edge
    t = np.clip((1.0 - r) / 0.6, 0, 1)                           # feathered over the outer 60 %
    return t * t * (3 - 2 * t)


def frames_of(rung, v):
    fr = artkit.load_frames("explosion-" + rung)
    return [deeper(f) for f in fr] if v == "c" else fr


def render(v, plate, art, soots):
    """The loop's frames (1x RGB arrays) of variant ``v`` over one plate."""
    out = []
    for step in range(0, LOOP, 2):
        img = plate.copy()
        for (rung, x, y, start), soot in zip(BURSTS, soots):
            fr = art[rung]
            age = step - start
            life = len(fr) * STEPS
            if v == "b" and 0 <= age < int(life * 1.3):
                s = age / (life * 1.3)
                grow = 0.9 + 0.45 * s                                 # 0.9 -> 1.35 of the burst's size
                side = fr[0].width
                d = max(4, int(side * grow))
                m = np.asarray(Image.fromarray((soot * 255).astype(np.uint8)).resize((d, d), Image.BILINEAR),
                               np.float64) / 255.0
                fade = min(1.0, age / (life * 0.15)) * (1.0 - max(0.0, (s - 0.5) / 0.5))
                alpha = SOOT_ALPHA * fade * m
                x0, y0 = int(round(x - d / 2)), int(round(y - d / 2 - 6 * s))   # rising a few px
                xa, ya, xb, yb = max(0, x0), max(0, y0), min(P, x0 + d), min(P, y0 + d)
                sub = alpha[ya - y0:yb - y0, xa - x0:xb - x0, None]
                img[ya:yb, xa:xb] = img[ya:yb, xa:xb] * (1 - sub) + SOOT * sub
            if 0 <= age < life:
                f = np.asarray(fr[age // STEPS], np.float64)
                w = f.shape[1]
                x0, y0 = int(round(x - w / 2)), int(round(y - w / 2))
                light = f[..., :3] * f[..., 3:4] / 255.0
                img[y0:y0 + w, x0:x0 + w] = np.minimum(255, img[y0:y0 + w, x0:x0 + w] + light)
        out.append(img)
    return out


def lift(plate, frames):
    """The small burst over its plate: the brightest frame's mean luminance gain in the core (12 px disc), that
    frame's share of clipped pixels there, and the mean luminance change against the plate in the burst's 24 px disc
    over its life (how much the burst stands out, lighter or darker)."""
    x, y = BURSTS[0][1], BURSTS[0][2]
    yy, xx = np.mgrid[0:P, 0:P]
    core = np.hypot(xx - x, yy - y) < 12
    ring = (np.hypot(xx - x, yy - y) > 22) & (np.hypot(xx - x, yy - y) < 30)
    lum = lambda a: a @ np.array([0.299, 0.587, 0.114])           # noqa: E731
    base = lum(plate)[core].mean()
    best = max(frames, key=lambda f: lum(f)[core].mean() - base)
    gain = lum(best)[core].mean() - base
    clipped = (best[core] >= 254.5).mean() * 100
    life = len(artkit.load_frames("explosion-small")) * STEPS // 2
    disc = np.hypot(xx - x, yy - y) < 24
    change = np.mean([np.abs(lum(f) - lum(plate))[disc].mean() for f in frames[:life]])
    del ring
    return gain, clipped, change


def build(v):
    art = {r: frames_of(r, v) for r, *_ in BURSTS}
    soots = [soot_field(64, 7), soot_field(64, 11)]
    rows, notes = [], []
    for label, plate in plates():
        fr = render(v, plate, art, soots)
        rows.append((label, fr))
        g, c, k = lift(plate, fr)
        notes.append(f"{label}: SMALL BURST'S CORE +{g:.0f} LUMINANCE AT ITS PEAK ({c:.0f} % CLIPPED); "
                     f"IT CHANGES ITS 24 PX DISC BY {k:.0f} ON AVERAGE OVER ITS LIFE")
    gap, scale = 4, 2
    w = 3 * P * scale + 2 * gap
    gif = []
    for i in range(len(rows[0][1])):
        im = Image.new("RGB", (w, P * scale), (10, 12, 26))
        for j, (_, fr) in enumerate(rows):
            tile = Image.fromarray(np.clip(fr[i], 0, 255).astype(np.uint8)).resize((P * scale, P * scale),
                                                                                 Image.NEAREST)
            im.paste(tile, (j * (P * scale + gap), 0))
        gif.append(im)
    keys = (4, 10, 18, 30, 40)                                  # loop frames (2 steps each)
    sheet = raster.sheet(1060, 48 + 3 * (P + 20) + 16 + 14 * len(notes),
                         f"EXPLOSIONS OVER BRIGHT BANDS {NAMES[v]}", SUB)
    y = 38
    for (label, fr) in rows:
        raster.draw_text(sheet, 16, y, label + " (1X; STEPS " + ", ".join(str(2 * k) for k in keys) + "; THE SMALL BURST 2X AT STEP 20)",
                         raster.LABEL)
        for n, k in enumerate(keys):
            sheet.paste(Image.fromarray(np.clip(fr[k], 0, 255).astype(np.uint8)), (16 + n * (P + 6), y + 14))
        peak = Image.fromarray(np.clip(fr[keys[1]], 0, 255).astype(np.uint8))      # the small burst 2x at step 20
        x, yb = BURSTS[0][1], BURSTS[0][2]
        sheet.paste(peak.crop((x - 37, yb - 37, x + 38, yb + 38)).resize((P, P), Image.NEAREST),
                    (16 + 5 * (P + 6) + 14, y + 14))
        y += P + 20
    for line in notes:
        raster.draw_text(sheet, 16, y, line, raster.LABEL_DIM)
        y += 14
    OUT.mkdir(parents=True, exist_ok=True)
    png, out = OUT / f"explosion-bright-r73-{v}.png", OUT / f"explosion-bright-r73-{v}.gif"
    sheet.convert("RGB").save(png, optimize=True)
    size = rig.write_gif(gif, out, fps=30, colors=128)
    print(f"{png.relative_to(ROOT)}, {out.name} ({size / 1e6:.1f} MB)")
    for line in notes:
        print("   ", line.lower())


if __name__ == "__main__":
    for v in [a for a in sys.argv[1:] if a in NAMES] or list(NAMES):
        build(v)
