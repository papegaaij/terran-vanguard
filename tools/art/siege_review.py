"""Review sheets and GIFs of tools/art/siege_units.py (M7 part D batch, concept round 40): the Ares Landing
dome, the Choir Herald, the Shellback and Level 17's secret, composited the way the game draws them (the
additive frames added, the dome's crack overlays over its body frames) on the Level 17 backdrop's tiles
(tools/art/backdrop_l17.py) where they exist. Run through siege_units.py (``--review``)."""
import math

import numpy as np
from PIL import Image

import artkit
from artkit import ROOT, TAU, sprite

import siege_units as su
from render import raster  # noqa: E402
from render.enemy_rigs import write_gif  # noqa: E402

artkit.REVIEW_ROUND = su.REVIEW_ROUND
BATCH = su.BATCH


def plate(name, w, h, x0=0, y0=0, fallback=(150, 96, 60)):
    tile = su.BACKDROP / f"{name}.png"
    if tile.exists():
        return Image.open(tile).convert("RGBA").crop((x0, y0, x0 + w, y0 + h))
    return Image.new("RGBA", (w, h), fallback + (255,))


def paste_c(dst, img, x, y, alpha=1.0):
    if alpha < 1:
        a = np.array(img).copy()
        a[..., 3] = (a[..., 3] * alpha).astype(np.uint8)
        img = Image.fromarray(a, "RGBA")
    dst.alpha_composite(img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))


def add_c(dst, img, x, y, gain=1.0):
    if gain != 1.0:
        a = np.array(img).astype(np.float64)
        a[..., :3] *= gain
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    out = artkit.add_light(dst, img, (int(round(x - img.width / 2)), int(round(y - img.height / 2))))
    dst.paste(out)


def stacked(base, *over):
    img = base.copy()
    for o in over:
        img.alpha_composite(o)
    return img


def save(sheet, gif, concept, subject, fps=10):
    artkit.save_review(sheet, gif, concept, subject, fps=fps)


# =========================================================================== the dome

def dome_review():
    rows = []
    for name in su.DOMES:
        body, cracks = su.load(name), [su.load(f"{name}-crack-{h}")[0] for h in (1, 2, 3)]
        zoom = 2 if name == "ares-dome" else 1
        rows += [(f"{name.upper()} BREATHING LOOP", body, zoom, False),
                 (f"{name.upper()} HITS 1-3 (CRACK OVERLAYS OVER THE BODY), FLICKER UNDER CRACK-2", [
                     stacked(body[0], cracks[0]), stacked(body[0], cracks[1]), stacked(body[0], cracks[2])]
                  + [stacked(f, cracks[1]) for f in su.load(f"{name}-flicker")], zoom, False),
                 (f"{name.upper()}-FLASH (ADDITIVE)", su.load(f"{name}-flash"), zoom, True),
                 (f"{name.upper()}-BROKEN (CAVE-IN, LAST FRAME STAYS)", su.load(f"{name}-broken"), zoom, False),
                 (f"{name.upper()}-DUST", su.load(f"{name}-dust")[::2], 1, False)]
    rows.append(("DOME-PIP (HUD, TINTED BY THE TRACKER)", su.load("dome-pip"), 4, False))
    sheet = artkit.review_sheet("ARES LANDING DOME - FINAL SPRITES (GEODESIC GLASS, MA.3 = A)", rows, width=1300, batch=BATCH)
    gif = []
    br, eden = su.load("ares-dome"), su.load("ares-dome-eden")
    fl, broken, dust = su.load("ares-dome-flicker"), su.load("ares-dome-broken"), su.load("ares-dome-dust")
    cracks = [su.load(f"ares-dome-crack-{h}")[0] for h in (1, 2, 3)]
    flash = su.load("ares-dome-flash")[0]
    apron_w = su.BACKDROP / "dome-apron-west.png"
    apron_e = su.BACKDROP / "dome-apron-eden.png"
    for k in range(70):
        t = k / 10
        f = plate("colony-ground", 400, 240)
        if apron_w.exists():
            paste_c(f, Image.open(apron_w).convert("RGBA"), 110, 120)
        if apron_e.exists():
            paste_c(f, Image.open(apron_e).convert("RGBA"), 290, 120)
        paste_c(f, eden[int(t * 4) % 4], 290, 120)
        hits = sum(t >= h for h in (1.0, 2.4, 3.8))
        if hits < 3:
            body = fl[int(t * 7) % 4] if hits == 2 else br[int(t * 4) % 4]
            paste_c(f, body, 110, 120)
            if hits:
                paste_c(f, cracks[hits - 1], 110, 120)
            if any(0 <= t - h < 0.1 for h in (1.0, 2.4)):
                add_c(f, flash, 110, 120)
        else:
            j = int((t - 3.8) * 10)
            paste_c(f, broken[min(j, len(broken) - 1)], 110, 120)
            if j < len(dust):
                paste_c(f, dust[j], 110, 120)
        gif.append(f.convert("RGB"))
    save(sheet, gif, su.DOME_CONCEPT, "ares-dome")


# =========================================================================== the Choir Herald

def herald_review():
    beat, crest = su.load("choir-herald"), su.load("choir-herald-crest")
    rows = [("CHOIR-HERALD WING BEAT (ONE HEADING, FACES DOWN)", beat, 2, False),
            ("WITH THE CREST BLOOM ADDED", [artkit.add_light(beat[0], c) for c in crest], 2, False),
            ("CHOIR-HERALD-CREST (ADDITIVE)", crest, 2, True),
            ("CHOIR-HERALD-RING 0-6 SEGMENTS (ADDITIVE, SCALED TO THE UNIT)", su.load("choir-herald-ring"), 2, True),
            ("CHOIR-HERALD-RING-FLASH (ABSORB)", su.load("choir-herald-ring-flash"), 2, True),
            ("CHOIR-HERALD-THREAD (TILES ALONG X)", su.load("choir-herald-thread"), 6, True),
            ("CHOIR-HERALD-AURA (90 PX RADIUS)", su.load("choir-herald-aura"), 1, True)]
    sheet = artkit.review_sheet("CHOIR HERALD - FINAL SPRITES (SERAPH, U.3 = B)", rows, width=1300, batch=BATCH)
    needler = [Image.open(ROOT / "assets" / "sprites" / f"needler_{k}.png").convert("RGBA") for k in range(6)]
    rings, flash = su.load("choir-herald-ring"), su.load("choir-herald-ring-flash")
    thread, aura = su.load("choir-herald-thread"), su.load("choir-herald-aura")[0]
    gif = []
    die = 6.0
    for k in range(80):
        t = k / 10
        f = plate("colony-ground", 360, 320, 40, 200)
        hx, hy = 180 + 30 * math.sin(t * 0.6), 150
        alive = t < die
        fade = 1.0 if alive else max(0.0, 1 - (t - die) / 0.3)
        if alive:
            pulse = 0.55 + 0.25 * math.sin(t * 4)
            add_c(f, aura, hx, hy, pulse)
        pts = []
        for i in range(4):
            a = TAU * i / 4 + t * 0.7
            pts.append((hx + 72 * math.cos(a), hy + 72 * math.sin(a)))
        for i, (x, y) in enumerate(pts):
            if alive:
                n = int(math.hypot(x - hx, y - hy) / 16)
                ang = math.atan2(y - hy, x - hx)
                strip = thread[int(t * 12) % 4].rotate(-math.degrees(ang), expand=True, resample=Image.NEAREST)
                for j in range(1, n):
                    add_c(f, strip, hx + (x - hx) * j / n, hy + (y - hy) * j / n, 0.7)
            paste_c(f, needler[int(t * 8 + i) % 6], x, y)
            if fade > 0:
                lit = 6 if i else max(0, 6 - int(max(0.0, t - 1.5) * 3))
                ring = rings[lit].resize((52, 52), Image.NEAREST)
                add_c(f, ring, x, y, fade)
                if i == 0 and 1.5 <= t < 3.5 and int(t * 10) % 3 == 0:
                    add_c(f, flash[int(t * 10) % 3].resize((52, 52), Image.NEAREST), x, y)
        if alive:
            paste_c(f, beat[int(t * 10) % len(beat)], hx, hy)
            add_c(f, su.load("choir-herald-crest")[int(t * 6) % 4], hx, hy)
        gif.append(f.convert("RGB"))
    save(sheet, gif, su.AIR_CONCEPT, "choir-herald")


# =========================================================================== the Shellback

def shellback_review():
    walk, curl, roll = su.load("shellback"), su.load("shellback-curl"), su.load("shellback-roll")
    belly, husk = su.load("shellback-belly"), su.load("shellback-husk")
    vent, dead = su.load("shellback-vent"), su.load("shellback-vent-dead")[0]
    piv = su.vent_pivots()

    def with_vent(k, over=None, glow=None):
        img = walk[k * su.WALK].copy()
        x, y = piv[k]
        if over is not None:
            img.alpha_composite(over, (int(x - over.width / 2), int(y - over.height / 2)))
        if glow is not None:
            img = artkit.add_light(img, glow, (int(x - glow.width / 2), int(y - glow.height / 2)))
        return img
    rows = [("SHELLBACK, 16 HEADINGS (WALK FRAME 0)", [walk[k * su.WALK] for k in range(16)], 1, False),
            ("WALK CYCLE, HEADING 0", walk[:su.WALK], 2, False),
            ("SHELLBACK-CURL, HEADING 0 (UNCURL BACKWARDS)", curl[:4], 2, False),
            ("SHELLBACK-ROLL, HEADING 0 (8 FRAMES = ONE BAND PAIR)", roll[:8], 2, False),
            ("VENT SWELL (SHELLBACK-VENT ADDED AT ITS PIVOT), VENT-DEAD", [with_vent(0, glow=v) for v in vent]
             + [with_vent(0, over=dead), with_vent(5, over=dead)], 2, False),
            ("BELLY WINDOW (SHELLBACK-BELLY ADDED), 4 HEADINGS", [artkit.add_light(walk[k * su.WALK], belly[k])
                                                                  for k in (0, 4, 8, 12)], 2, False),
            ("SHELLBACK-HUSK, 4 HEADINGS", [husk[k] for k in (0, 4, 8, 12)], 2, False)]
    sheet = artkit.review_sheet("SHELLBACK - FINAL SPRITES (ROUND 05 A)", rows, width=1300, batch=BATCH)
    gif = []
    dist = 0.0
    for k in range(100):
        t = k / 10
        f = plate("colony-ground", 300, 420, 60, 100)
        y = 40 + 34 * min(t, 3.4) + (140 * min(max(t - 4.0, 0), 2.5)) + 24 * max(t - 7.1, 0)
        x = 150 + 30 * math.sin(y / 120)
        h = 0
        if t < 3.4 or t >= 7.1:
            dist = y
            body = walk[h * su.WALK + int(dist / 6) % su.WALK]
            paste_c(f, body, x, y)
            vx, vy = piv[h]
            if t < 3.4 and 1.2 <= (t % 2.0) < 1.7:
                add_c(f, vent[min(2, int(((t % 2.0) - 1.2) / 0.5 * 3))], x - 56 + vx, y - 56 + vy)
            if 7.7 <= t < 9.2:
                add_c(f, belly[h], x, y, 0.7 + 0.3 * math.sin(t * 12))
        elif t < 4.0:
            paste_c(f, curl[h * 4 + min(3, int((t - 3.4) / 0.15))], x, y)
        elif t < 6.5:
            paste_c(f, roll[h * 8 + int(y / 3.7) % 8], x, y)
        else:
            paste_c(f, curl[h * 4 + 3 - min(3, int((t - 6.5) / 0.15))], x, y)
        gif.append(f.convert("RGB"))
    save(sheet, gif, su.GROUND_CONCEPT, "shellback")


# =========================================================================== the secret

def props_review():
    """Round 45 (round 44 G.c): the frame split off its pad; the hit flash (the frame's opaque pixels whitened)
    shows that it follows the structure's shape, not the pad's rectangle."""
    frames, cap, glow = su.load("greenhouse-frame"), su.load("seed-capsule"), su.load("seed-capsule-glow")
    pad = su.load("greenhouse-pad")[0]

    def over_pad(f):
        out = pad.copy()
        out.alpha_composite(f)
        return out

    def flash(f, k=0.75):
        a = np.array(f).astype(np.float64)
        a[..., :3] = a[..., :3] * (1 - k) + 255 * k
        return Image.fromarray(a.astype(np.uint8), "RGBA")
    rows = [("GREENHOUSE-FRAME: WHOLE, HIT, BADLY HIT, LIFTED (TRANSPARENT ROUND THE STRUCTURE)", frames, 4, False),
            ("GREENHOUSE-PAD (GROUND DECAL, UNDER EVERY STATE)", [pad], 4, False),
            ("THE FRAME OVER ITS PAD, AS THE GAME DRAWS IT", [over_pad(f) for f in frames], 4, False),
            ("THE HIT FLASH OVER THE PAD (THE FRAME'S PIXELS ONLY)", [over_pad(flash(f)) for f in frames[:3]], 4, False),
            ("SEED-CAPSULE (FREED)", cap, 4, False),
            ("SEED-CAPSULE-GLOW (ADDITIVE, UNDER THE FRAME)", glow, 4, True),
            ("THE FRAME WITH THE GLOW ADDED", [artkit.add_light(over_pad(frames[0]), g, (20, 16)) for g in glow[:3]], 4,
             False)]
    before = artkit.REVIEW_ROUND
    artkit.REVIEW_ROUND = "r45"
    try:
        sheet = artkit.review_sheet("LEVEL 17 - THE SEED-VAULT SECRET: THE FRAME AND ITS PAD (FINAL)", rows, width=1240,
                                    batch=BATCH)
        gif = []
        for k in range(50):
            t = k / 10
            f = plate("burnt-fields", 200, 140, 20, 300)
            state = 0 if t < 1.5 else 1 if t < 2.5 else 2 if t < 3.2 else 3
            paste_c(f, pad, 100, 70)
            hit = state < 3 and any(h <= t < h + 0.08 for h in (0.6, 1.0, 1.5, 2.0, 2.5, 2.9, 3.15))
            paste_c(f, flash(frames[state]) if hit else frames[state], 100, 70)
            if state < 3:
                add_c(f, glow[int(t * 6) % 6], 100, 69)
            else:
                paste_c(f, cap[0], 100 + min(1.0, (t - 3.2) * 4) * 6, 70 - min(1.0, (t - 3.2) * 4) * 10)
            gif.append(f.resize((400, 280), Image.NEAREST).convert("RGB"))
        save(sheet, gif, su.LEVEL / "concept", "greenhouse-frame")
    finally:
        artkit.REVIEW_ROUND = before


def review(only=None):
    groups = {"domes": dome_review, "herald": herald_review, "shellback": shellback_review, "props": props_review}
    for g, fn in groups.items():
        if not only or g in only:
            fn()
