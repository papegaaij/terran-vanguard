#!/usr/bin/env python3
"""Production art: the glow-only frames of the units Level 26 flies in the dark (design/campaign/act-4-deep-water/
level-26-blackwater, *Darkness*; europa's *Darkness (level 26)*; M8 part F batch, concept round 55, step ART-F).

In the black water a unit outside the light shows only its glow: the teal, violet and lime parts and its eyes, the
body a faint rim (the 15 % rim is the game's light-map pool, gap check m8f §5 F3, not baked). The Glow Angler's glow
is tools/art/glow_angler.py's; the Siren's (``siren-glow``) and the Spiral Nautilus's (``spiral-nautilus-lip``,
``-glow``) were made with them in part E and are used unchanged. This script adds the two that were missing:

  driftjelly-glow_0..3     40x40, additive: the Driftjelly's veined look in the dark (its "veined re-tint"): the bell's
                           lime vein network and its knob as crisp emission with a 1 px stepped halo, at the four pulse
                           frames (rest .. contracted), brighter than ``driftjelly-pulse`` at rest so a drifting jelly
                           reads as its veins; drawn ungraded over ``driftjelly-deep`` (the pulse bloom stays the tell)
  eel-swarm-glow_0..47     48x48, additive: the Eel Swarm at rest in the dark, heading x 3 + part (0 head, 1 segment,
                           2 tail; centred on the part's centre as ``eel-swarm-stripe``): the violet stripe at its resting
                           glow, the head's violet eyes and lime gills: "a dark snake lit by its stripe" (eel_swarm.py's
                           note); ``eel-swarm-stripe`` stays the tell over it
  design/enemies/naval/concept/dark-units-final-r55-a.png/.gif   the review: every unit Level 26 uses (Driftjelly, Eel
                           Swarm, Spiral Nautilus shut and open, Siren, Glow Angler) in the cone, in the dark (rim and
                           glow) and glow only; the GIF on the fault scarp (backdrop_l26.py) in the dark look's stand-in:
                           jellies drifting, a snake crossing, a Nautilus pair rolling and one uncoiling, the scarp's
                           Sirens telling and singing in their cave, then the Sonar Pulse lighting all of them

Run: python3 tools/art/l26_glows.py [--review]   (~1 min; --review only rebuilds the review files; run glow_angler.py
and backdrop_l26.py first)
"""
import json
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import driftjelly as dj
import eel_swarm as eel
import l26kit as kit
from artkit import DESIGN, TAU

from render import raster, sprite  # noqa: E402

SCRIPT = "l26_glows.py"
BATCH = "M8 part F batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r55"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
HEAD = eel.HEAD
REST_STRIPE = 0.35                             # the stripe's resting glow in the dark (the tell is 1.6-1.8)


def halo(rgb, blur=1.3, k=0.6):
    """Emission with a 1 px stepped halo (siren.py's treatment), as an additive frame."""
    halo_ = np.stack([np.array(Image.fromarray(np.clip(rgb[..., c], 0, 255).astype(np.uint8))
                               .filter(ImageFilter.GaussianBlur(blur)), float) for c in range(3)], -1)
    out = np.maximum(rgb, np.floor(halo_ / 255 * 3 + 0.5) / 3 * 255 * k * (halo_.max(-1, keepdims=True) > 12))
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def jelly_glow(i):
    pulse = dj.PULSES[i]
    scene, _ = dj.m6.driftjelly(pulse)
    hi, f = artkit.render_hi(scene, dj.glow_mats(pulse), dj.SIZE, dj.EXTENT, shadows=False)
    lo = sprite.downsample(hi, f)
    em = np.clip(lo[..., :3] * lo[..., 3:4], 0, 1)
    lum = em.max(-1)
    gain = 0.7 + 0.3 * pulse
    v = np.clip(lum * 1.8, 0, 1) * gain
    rgb = dj.LIME[None, None] * artkit.ordered_dither(v, 6)[..., None] + 60 * np.clip(v - 0.8, 0, 1)[..., None]
    return halo(rgb, 1.1, 0.5)


def eel_glow(job):
    k, part = job
    kind = ("head", "segment", "tail")[part]
    scene, mats = eel.model(kind, REST_STRIPE, 0.0)
    mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    extent = {"head": eel.HEAD_EXTENT, "segment": eel.SEG_EXTENT, "tail": eel.TAIL_EXTENT}[kind]
    size = {"head": eel.HEAD, "segment": eel.SEG, "tail": eel.TAIL}[kind]
    scene, mats = eel.turned(scene, mats, TAU * k / eel.HEADINGS)
    hi, f = artkit.render_hi(scene, mats, (size, size), extent, shadows=False)
    lo = sprite.downsample(hi, f)
    em = np.clip(lo[..., :3] * lo[..., 3:4], 0, 1) * 255
    em = np.where(em.max(-1, keepdims=True) > 20, np.clip(em * 1.5, 0, 255), 0)
    pad = (HEAD - size) // 2
    em = np.pad(em, ((pad, HEAD - size - pad), (pad, HEAD - size - pad), (0, 0)))
    return halo(em, 1.2, 0.55)


def build():
    with ProcessPoolExecutor() as ex:
        jelly = list(ex.map(jelly_glow, range(4)))
        eels = list(ex.map(eel_glow, [(k, p) for k in range(eel.HEADINGS) for p in range(3)]))
    kit.write("driftjelly-glow", artkit.quantize_set(jelly, 12), SOURCE)
    kit.write("eel-swarm-glow", artkit.quantize_set(eels, 16), SOURCE)


# =========================================================================== review

def cell(body, glows, mode, size=96, rim=40, extra=()):
    """One unit on a ``size`` plate: ``mode`` lit (the cone), dark (6 % + the 15 % rim, glows added) or glow (only)."""
    plate = Image.new("RGBA", (size, size), (14, 20, 26, 255))
    base = plate.copy()
    for img, dx, dy in [(body, 0, 0)] + list(extra):
        kit.paste_c(base, img, size / 2 + dx, size / 2 + dy)
    d = kit.Dark(kit.AMBIENT, size, size)
    if mode == "lit":
        d.lamp(size / 2, size / 2, size, 1.0, (1, 0.95, 0.85))
        out = kit.shade(base, d.light())
    elif mode == "dark":
        d.rim(size / 2, size / 2, rim)
        out = kit.shade(base, d.light())
    else:
        out = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    for g, dx, dy in glows:
        out = kit.add_c(out, g, size / 2 + dx, size / 2 + dy)
    return out


def review():
    jg, eg = kit.load("driftjelly-glow"), kit.load("eel-swarm-glow")
    L = kit.load
    piv_n = json.loads((artkit.PIVOTS / "spiral-nautilus.json").read_text(encoding="utf-8"))
    piv_a = json.loads((artkit.PIVOTS / "glow-angler.json").read_text(encoding="utf-8"))
    jdeep, jpulse = L("driftjelly-deep"), L("driftjelly-pulse")
    ehead, eseg, etail = L("eel-swarm"), L("eel-swarm-segment"), L("eel-swarm-tail")
    nspin, nlip, nopen, ncrown, nglow = (L(n) for n in ("spiral-nautilus", "spiral-nautilus-lip", "spiral-nautilus-open",
                                                      "spiral-nautilus-crown", "spiral-nautilus-glow"))
    sbody, sglow = L("siren"), L("siren-glow")
    abody, aglow, alure = L("glow-angler"), L("glow-angler-glow"), L("glow-angler-lure")
    cx, cy = piv_n["crown_offset"][0][2]
    lx, ly = (piv_a["lure"][0][0] - 40, piv_a["lure"][0][1] - 40)
    units = [("DRIFTJELLY", jdeep[0], [(jg[0], 0, 0), (jpulse[0], 0, 0)], [], 36),
             ("EEL HEAD", ehead[0], [(eg[0], 0, 0)], [], 44),
             ("EEL BODY", eseg[0], [(eg[1], 0, 0)], [], 36),
             ("NAUTILUS SHUT", nspin[0], [(nlip[0], 0, 0)], [], 48),
             ("NAUTILUS OPEN", nopen[0], [(nglow[0], cx, cy)], [(ncrown[3], cx, cy)], 56),
             ("SIREN (RIGHT WALL)", sbody[6], [(sglow[6], 0, 0)], [], 56),
             ("GLOW ANGLER", abody[0], [(aglow[0], 0, 0), (alure[4], lx, ly)], [], 40)]
    rows = [("DRIFTJELLY-GLOW: THE VEINED LOOK AT 4 PULSE FRAMES (ADDITIVE)", jg, 3, True),
            ("EEL-SWARM-GLOW: HEAD, HEADINGS 0-14 EVEN (ADDITIVE)", eg[0:48:6], 2, True),
            ("EEL-SWARM-GLOW: BODY EEL AND TAIL, HEADINGS 0-12 BY 4", [eg[k * 3 + p] for k in (0, 4, 8, 12)
                                                                      for p in (1, 2)], 2, True)]
    sheet = artkit.review_sheet("LEVEL 26 - THE UNITS' GLOW-ONLY FRAMES (FINAL)", rows, width=1000, batch=BATCH)
    cw = 96
    grid = Image.new("RGBA", (sheet.width, 3 * (cw * 1 + 6) + 70), artkit.SHEET_BG)
    raster.draw_text(grid, 16, 4, "EACH UNIT IN THE CONE / IN THE DARK (6 % AMBIENT + THE 15 % RIM POOL, GLOWS ADDED) / "
                                  "GLOW ONLY, 1X", raster.LABEL)
    for i, (name, body, glows, extra, rim) in enumerate(units):
        x = 16 + i * (cw + 40)
        raster.draw_text(grid, x, 18, name[:16], raster.LABEL_DIM)
        for j, mode in enumerate(("lit", "dark", "glow")):
            img = cell(body, glows, mode, cw, rim, extra)
            grid.alpha_composite(img, (x, 32 + j * (cw + 6)))
    big = Image.new("RGBA", (sheet.width, sheet.height + grid.height), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    big.alpha_composite(grid, (0, sheet.height))
    frames = scene_frames()
    y0 = big.height
    stills = [(frames[int(t * FPS)], cap) for t, cap in ((1.5, "THE SCARP IN THE DARK"), (3.4, "A SIREN'S SONG"),
                                                         (5.0, "A NAUTILUS OPEN"), (7.0, "SONAR PULSE"))]
    out = Image.new("RGBA", (big.width, y0 + 310), artkit.SHEET_BG)
    out.alpha_composite(big, (0, 0))
    raster.draw_text(out, 16, y0, "THE GIF'S MOMENTS AT 1/2: LEVEL 26'S SCARP (T=62) IN THE DARK LOOK'S STAND-IN",
                     raster.LABEL)
    for i, (f, cap) in enumerate(stills):
        out.alpha_composite(f.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, y0 + 14))
        raster.draw_text(out, 16 + i * 248, y0 + 290, cap, raster.LABEL_DIM)
    artkit.save_review(out, [f.resize((360, 405), Image.Resampling.BOX) for f in frames], CONCEPT, "dark-units",
                       fps=FPS)


FPS, DUR, T0 = 12, 8.5, 60.6
SONAR = 6.3


def scene_frames():
    import backdrop_l26 as b26                              # noqa: E402  (the scarp and its notches)
    L = kit.load
    jdeep, jpulse, jg = L("driftjelly-deep"), L("driftjelly-pulse"), L("driftjelly-glow")
    ehead, eseg, etail, eg = L("eel-swarm"), L("eel-swarm-segment"), L("eel-swarm-tail"), L("eel-swarm-glow")
    nspin, nlip, nopen, ncrown, nglow = (L(n) for n in ("spiral-nautilus", "spiral-nautilus-lip", "spiral-nautilus-open",
                                                      "spiral-nautilus-crown", "spiral-nautilus-glow"))
    sbody, sglow = L("siren"), L("siren-glow")
    piv_n = json.loads((artkit.PIVOTS / "spiral-nautilus.json").read_text(encoding="utf-8"))
    pieces = b26.placed_pieces()
    notch = b26.NOTCHES[0]
    jellies = [(70, 120, 0.0), (140, 260, 1.3), (60, 390, 2.1), (190, 60, 0.6), (120, 470, 3.0)]
    frames = []
    hist = []
    for fi in range(int(DUR * FPS)):
        t = fi / FPS
        lt = T0 + t
        base = b26.scroll(lt)
        sonar = t >= SONAR
        solids, glows = [], []
        for x, y, ph in jellies:                            # drifting jellies (ride the scroll, drift right)
            p = int((t * 2.2 + ph) * 2) % 4
            jy = (y + 120 * t * 0.25) % 560 - 10
            solids.append((jdeep[p], x + 6 * t, jy, 36))
            glows += [(jg[p], x + 6 * t, jy, 1.0), (jpulse[p], x + 6 * t, jy, 1.0)]
        # the snake: head along a sine path, the body following the path history
        hx, hy = 180 + 70 * np.sin(t * 0.9), -40 + 70 * t
        hist.insert(0, (hx, hy))
        pts = [hist[0]]
        dist = 0.0
        for a, b in zip(hist, hist[1:]):
            dist += np.hypot(a[0] - b[0], a[1] - b[1])
            if dist >= 24 * len(pts) + 6:
                pts.append(b)
            if len(pts) > 8:
                break
        for j, (px, py) in enumerate(pts):
            nx, ny = (pts[j - 1] if j else (px + 70 * 0.9 * np.cos(t * 0.9) / FPS, py + 70 / FPS))
            k = kit.heading_index(nx - px, ny - py) if j else kit.heading_index(63 * np.cos(t * 0.9), 70)
            part = 0 if j == 0 else (2 if j == len(pts) - 1 else 1)
            img = ehead[k * 3] if part == 0 else (etail if part == 2 else eseg)[k * 2]
            solids.append((img, px, py, 30))
            glows.append((eg[k * 3 + part], px, py, 1.0))
        for i, (ox, oy, ph) in enumerate(((300, 120, 0.0), (180, 210, np.pi))):     # a Nautilus pair
            a = t * 0.8 + ph
            nx_, ny_ = ox + 40 * np.cos(a), oy + 30 * np.sin(a) + 10 * t
            if i == 0 and 4.0 <= t:
                h = kit.heading_index(240 - nx_, 444 - ny_)
                s = min(2, int((t - 4.0) / 0.2)) if t < 4.6 else 3 + int(t * 5) % 2
                cx, cy = piv_n["crown_offset"][h][min(s, 2)]
                solids += [(nopen[h], nx_, ny_, 48), (ncrown[h * 6 + s], nx_ + cx, ny_ + cy, 0)]
                glows.append((nglow[h * 3 + (2 if s < 3 else 0)], nx_ + cx, ny_ + cy, 1.0))
            else:
                k = int(a * 24 / TAU * 3) % 24
                solids.append((nspin[k], nx_, ny_, 48))
                glows.append((nlip[k], nx_, ny_, 1.0))
        y = base - 1 - notch["scroll"]                       # the scarp's Sirens: one across the cave, one beside it
        fx = float(b26.foot(1, notch["scroll"]))
        for j, (dy, te) in enumerate(((0, 0.6), (180, 2.1))):
            yy = y + dy
            age = t - te
            if age < 0:
                continue
            if 0.4 <= age < 1.2:
                kk = 2 + min(2, int((age - 0.4) / 0.27))
            elif 1.2 <= age < 1.35:
                kk = 5
            else:
                kk = int(lt * 2) % 2
            sx = fx - 6 if dy else fx + 22
            solids.append((sbody[6 + kk], sx, yy, 56))
            glows.append((sglow[6 + kk], sx, yy, 1.0))
        ship = (250 + 30 * np.sin(t * 0.6), 444.0)
        img = b26.composite(lt, pieces, real=t, ship=ship, rook=(ship[0] - 64, 474.0), solids=solids, glows=glows,
                            sonar=sonar)
        if sonar:
            r = (t - SONAR) * 600
            if r < 760:
                a = np.array(img).astype(np.float64)
                yy_, xx_ = np.mgrid[0:540, 0:480]
                band = np.abs(np.hypot(xx_ - ship[0], yy_ - ship[1]) - r) < 2
                a[band, :3] = np.minimum(255, a[band, :3] + np.array((60, 200, 170)))
                img = Image.fromarray(a.astype(np.uint8), "RGBA")
            raster.draw_text(img, 8, 24, "SONAR PULSE: EVERY UNIT LIT 6 S", (120, 255, 220))
        raster.draw_text(img, 8, 8, "L26 THE SCARP IN THE DARK  T={:.1f}".format(lt), raster.LABEL)
        frames.append(img.convert("RGB"))
    return frames


def main(argv):
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
