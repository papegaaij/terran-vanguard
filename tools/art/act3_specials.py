#!/usr/bin/env python3
"""Production art: the Act 3 specials' effects (design/player/specials; M7 part A batch, concept
round 37): the EMP Burst and the Shield Overcharge in the looks round 36 chose (G.3 = a, G.4 = b,
tools/concept/gear_r36.py ``emp_ring``/``crackle`` and ``oc_bubble``/``oc_activation``), and the
Orbital Lance, which round 36 showed no look for: an a/b proposal in round 37, which chose b
("descending rings", P.2 = b, user 2026-10-13), the variant written to assets/ (LANCE).

Outputs (assets/sprites/, all additive, premultiplied on black, unless noted):
  emp-burst-ring_0..3          60x128: the ring as a strip, x across the ring (40 px inside the leading
                               edge to 20 px beyond it), y along the circumference, repeating every
                               128 px of arc length (round to whole repeats round the circle); the
                               game draws it as a band of quads round the circle like the Smart Bomb's
                               ring; the four frames are the crackling arcs along it, 2 steps a frame
                               (the ring covers the field in 0.4 s; it fades to 65 % as it spreads)
  emp-burst-crackle_0..3       32x32: the 4-frame electric crackle overlay on a stunned unit under
                               32 px, centred on the unit, 3 steps a frame;
  emp-burst-crackle-medium_0..3  56x56 for units under 64 px; emp-burst-crackle-large_0..3 96x96 above.
                               The dimmed glow and the machines' lights going out are the game's tints
  orbital-lance-line_0..3      12x64: the red targeting line (0.6 s), a tile repeated down the field at
                               the ship's x, 2 steps a frame
  orbital-lance-column_0..7    72x64: the 40 px column (white-hot core, blue halo, b: bright rings
                               travelling down it) as a tile repeated over the field's height, 1 step
                               a frame; the 0.3 s fade is alpha
  orbital-lance-scorch         48x32, normal blending: the burnt ground under the column, tiling
                               vertically; the game lays a column of it down the field whenever the
                               lance has moved 16 px across or the ground 32 px since the last, scrolling
                               with the ground
  orbital-lance-embers_0..3    48x32: the fresh scorch's glowing embers over it, 4 steps a frame
  shield-overcharge-shell_0..7 72x72: the white double shell round the ship while overcharged (the
                               production shield ring recoloured white, a second ring just outside it
                               and a crackle running round), centred on the ship, 3 steps a frame
  shield-overcharge-flare_0..6 110x110: the activation flare (two white rings opening, a white core),
                               0.35 s, 3 steps a frame
  review files: design/player/specials/concept/emp-burst-final-r37-a, shield-overcharge-final-r37-a
  (chosen looks), orbital-lance-r37-b (the chosen proposal) and concept/rejected/orbital-lance-r37-a
  (round 37 closed 2026-10-13: the unchosen variant is written to rejected/), .png/.gif

Each special's frames are claimed by its name as a slug (vanguard.pipeline.SpriteUse) once
design/player/specials/data.yaml lists it; until then they go to build/art-held/sprites (act3kit).

Run: python3 tools/art/act3_specials.py [emp] [lance] [overcharge] [--review]   (~30 s)
"""
import sys

import numpy as np
from PIL import Image

import act3kit
import artkit
from artkit import DESIGN, TAU, sprite

import vfx_r08 as v8  # noqa: E402  (concept scripts, imported unchanged)
import gear_r36 as g  # noqa: E402

SCRIPT = "act3_specials.py"
SOURCE = artkit.source_note(SCRIPT, act3kit.BATCH)
CONCEPT = DESIGN / "player" / "specials" / "concept"
PS, WHITE = v8.PS, v8.WHITE
Canvas, gauss, solid = v8.Canvas, v8.gauss, v8.solid
ARC = g.ARC
RED = (255, 60, 50)
FW, FH = 480, 540
LANCE = "b"   # the Orbital Lance's look in the game: round 37 P.2 = b, "descending rings" (user, 2026-10-13)


def arcs(cv, pts, col, glow=1.0, core=1.0, width=0.5, halo=1.6):
    """A jagged light line on a Canvas: a soft halo and a thin core per segment."""
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        s = cv.seg(x0, y0, x1, y1)
        cv.add(col, gauss(s, halo) * 0.45 * glow)
        cv.add(WHITE, gauss(s, width) * core)


# =========================================================================== EMP Burst

RING_IN, RING_OUT, RING_ALONG = 40, 20, 128


def emp_ring(f):
    w, h = RING_IN + RING_OUT, RING_ALONG
    cv = Canvas(w, h)
    e = cv.x - RING_IN
    cv.add(PS[1], gauss(e, 9) * 0.7)
    cv.add(PS[2], gauss(e, 3.5) * 0.9)
    cv.add(WHITE, gauss(e, 1.3) * 1.2)
    cv.add(PS[1], (e < 0) * np.clip(1 + e / RING_IN, 0, 1) * 0.10)
    rng = np.random.default_rng(3600 + f)
    for _ in range(4):                               # arcs crackling along the ring, wrapping in y
        y0 = rng.uniform(0, h)
        length = rng.uniform(18, 40)
        pts = g.jagged((RING_IN + rng.uniform(-2, 2), y0), (RING_IN + rng.uniform(-2, 2), y0 + length),
                       rng, depth=3, rough=0.6)
        for dy in (-h, 0, h):
            arcs(cv, [(x, y + dy) for x, y in pts], ARC, core=0.9)
    fade = np.clip(np.minimum(cv.x, w - cv.x) / 6.0, 0, 1)
    cv.rgb *= fade[..., None]
    cv.a *= fade
    return artkit.additive(cv.image())


def crackle(size, f):
    """Arcs between random points inside the unit's disc (gear_r36 ``crackle`` a, without a sprite)."""
    cv = Canvas(size, size)
    rng = np.random.default_rng(size * 13 + f * 7)
    c = size / 2
    rr = size / 2 - 5
    n = 2 + size // 18
    for _ in range(n):
        p = []
        for _ in range(2):
            a, r = rng.uniform(0, TAU), rr * np.sqrt(rng.uniform(0.1, 1))
            p.append((c + np.cos(a) * r, c + np.sin(a) * r * 0.85))
        arcs(cv, g.jagged(p[0], p[1], rng, depth=3, rough=0.5), ARC, core=1.0,
             width=0.35 if size < 50 else 0.5, halo=0.9 if size < 50 else 1.6)
    return artkit.additive(cv.image())


def build_emp():
    with act3kit.sprites_of("emp-burst"):
        artkit.write_frames("emp-burst-ring", act3kit.quantize_bright([emp_ring(f) for f in range(4)], 24), SOURCE)
        for name, size in (("emp-burst-crackle", 32), ("emp-burst-crackle-medium", 56), ("emp-burst-crackle-large", 96)):
            artkit.write_frames(name, act3kit.quantize_bright([crackle(size, f) for f in range(4)], 16), SOURCE)
    print(f"emp burst: {act3kit.where('emp-burst')}")


def field_ring(cx, cy, radius, strip, gain=1.0):
    """The strip mapped round a circle as the game draws it (x across, y along the arc length)."""
    yy, xx = np.mgrid[0:FH, 0:FW] + 0.5
    d = np.hypot(xx - cx, yy - cy)
    e = d - radius
    col = np.clip(np.round(e + RING_IN).astype(int), 0, RING_IN + RING_OUT - 1)
    reps = max(1, int(round(TAU * radius / RING_ALONG)))
    s = (np.arctan2(yy - cy, xx - cx) % TAU) / TAU * reps * RING_ALONG
    row = np.clip(s.astype(int) % RING_ALONG, 0, RING_ALONG - 1)
    a = np.array(strip).astype(np.float64)
    inside = (e >= -RING_IN) & (e < RING_OUT)
    rgb = a[row, col, :3] * inside[..., None] * gain
    return Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((FH, FW), 255)]).astype(np.uint8), "RGBA")


def review_emp():
    with act3kit.sprites_of("emp-burst"):
        ring = artkit.load_frames("emp-burst-ring")
        cr = {k: artkit.load_frames(n) for k, n in (("s", "emp-burst-crackle"), ("m", "emp-burst-crackle-medium"),
                                                    ("l", "emp-burst-crackle-large"))}
    ground = g.r36.mars_ground(41, craters=7, dunes=0.03)
    needler = g.asset("needler_0")
    ghost = g.r36.ghost["a"].get(np.pi / 2, glow=1.0)
    ghost_dark = g.r36.ghost["a"].get(np.pi / 2, glow=0.0)
    units = [(140, 150, ghost, ghost_dark, 3.0, "s"), (330, 120, ghost, ghost_dark, 3.0, "s"),
             (240, 220, needler, g.dim(needler, 0.72), 1.5, "s"), (380, 260, needler, g.dim(needler, 0.72), 1.5, "s")]
    sx, sy = 240, 440
    reach = np.hypot(max(sx, FW - sx), max(sy, FH - sy))
    fps = 30
    frames = []
    at = 0.5
    for i in range(fps * 5):
        t = i / fps
        step = i * 2
        img = ground.copy()
        p = (t - at) / 0.4
        radius = reach * p if 0 <= p <= 1 else -1
        for x, y, sp, dark, stun, size in units:
            hit_t = at + 0.4 * np.hypot(x - sx, y - sy) / reach
            stunned = hit_t <= t < hit_t + stun
            dx = 0 if stunned or t < at else 0
            sprite.paste_center(img, dark if stunned else sp, x + dx, y + (0 if stunned else 4 * np.sin(t * 2 + x)))
            if stunned:
                img = artkit.add_light(img, cr[size][(step // 3) % 4], (x - 16, y - 16))
        g.draw_ship(img, sx, sy, t)
        if radius > 0:
            img = artkit.add_light(img, field_ring(sx, sy, radius, ring[(step // 2) % 4], 1 - 0.35 * p))
        g.label(img, 8, FH - 14, "EMP BURST: RING 0.4 S, GHOST DRONES (MACHINES) 3 S, NEEDLERS (VRELL) 1.5 S",
                (150, 200, 240))
        frames.append(img)
    rows = [("EMP-BURST-RING: STRIP ACROSS (40 IN, 20 OUT) X ALONG 128 PX, 4 FRAMES, 2 STEPS", ring, 3, True),
            ("EMP-BURST-CRACKLE (32), 3 STEPS", cr["s"], 5, True),
            ("EMP-BURST-CRACKLE-MEDIUM (56)", cr["m"], 3, True),
            ("EMP-BURST-CRACKLE-LARGE (96)", cr["l"], 2, True),
            ("ON THE FIELD (1X, CROPPED)", [f.crop((60, 60, 420, 500)).resize((180, 220), Image.NEAREST)
                                            for f in frames[18:48:5]], 1, False)]
    sheet = artkit.review_sheet("EMP BURST - FINAL EFFECTS (ROUND 36 G.3 = A)", rows, width=1400, batch=act3kit.BATCH)
    artkit.save_review(sheet, [f.resize((360, 405), Image.NEAREST) for f in frames], CONCEPT, "emp-burst", fps=fps)


# =========================================================================== Orbital Lance

COL_W, COL_TILE, COL_FRAMES = 72, 64, 8
LINE_W, LINE_TILE = 12, 64


def lance_line(v, f):
    cv = Canvas(LINE_W, LINE_TILE)
    c = LINE_W / 2
    dx = np.abs(cv.x - c)
    run = ((cv.y - f * LINE_TILE / 4) % LINE_TILE) / LINE_TILE          # runs down the field
    if v == "a":
        cv.add(RED, gauss(dx, 2.2) * 0.35)
        cv.add(RED, solid(dx, 0.5) * 0.9)
        cv.add((255, 180, 170), solid(dx, 0.5) * gauss(np.abs(run - 0.5), 0.12) * 0.9)
    else:
        for o in (-2.5, 2.5):
            dash = ((cv.y + f * 4) % 8 < 5)
            cv.add(RED, solid(np.abs(cv.x - c - o), 0.5) * dash * 0.85)
        ch = np.abs(((cv.y - f * 4) % 16) - 8 + np.abs(cv.x - c) * 0.9)
        cv.add(RED, gauss(ch, 0.6) * (dx < 4.5) * 0.7)
        cv.add(RED, gauss(dx, 3) * 0.2)
    return artkit.additive(cv.image())


def lance_column(v, f):
    cv = Canvas(COL_W, COL_TILE)
    c = COL_W / 2
    dx = np.abs(cv.x - c)
    ph = TAU * f / COL_FRAMES
    y = cv.y
    halo = (130, 190, 255)
    if v == "a":                                 # piercing ray: striations racing down
        wob = 1 + 0.05 * np.sin(-y * TAU * 2 / COL_TILE - ph)
        cv.add(PS[1], gauss(dx, 22 * wob) * 0.8)
        cv.add(halo, gauss(dx, 14 * wob) * 0.7)
        cv.over(PS[3], solid(dx, 20 * wob) * 0.55)
        stri = np.sin(cv.x * 1.7 + np.sin(y * TAU / COL_TILE * 4 + ph * 4) * 0.9)
        cv.add(WHITE, (stri > 0.7) * solid(dx, 16) * 0.35)
        cv.over(WHITE, solid(dx, 11 * wob))
    else:                                        # ring pulses descending the column
        cv.add(PS[1], gauss(dx, 22) * 0.8)
        cv.add(halo, gauss(dx, 13) * 0.6)
        cv.over(PS[3], solid(dx, 19) * 0.45)
        band = ((y - f * COL_TILE / COL_FRAMES) % 32) / 32
        cv.add(WHITE, gauss(np.abs(band - 0.5), 0.06) * solid(dx, 20) * 0.6)
        cv.add(halo, gauss(np.abs(band - 0.5), 0.1) * gauss(dx - 20, 2.5) * 0.9)
        cv.over(WHITE, solid(dx, 9 + np.sin(-y * TAU * 2 / COL_TILE - ph) * 0.6))
    return artkit.additive(cv.image())


def lance_scorch(v):
    w, h = 48, 32
    rng = np.random.default_rng(1700)
    yy, xx = np.mgrid[0:h, 0:w] + 0.5
    n = g.raster.fbm(w, h, 8, 77, octaves=3, period=True) if hasattr(g.raster, "fbm") else rng.uniform(0, 1, (h, w))
    edge = np.clip((20 + 3 * (n - 0.5) * 2 - np.abs(xx - w / 2)) / 4, 0, 1)
    a = np.zeros((h, w, 4))
    a[..., :3] = (18, 12, 10)
    a[..., 3] = np.floor((0.2 + 0.25 * (0.6 + 0.4 * n)) * edge * 4 + 0.5) / 4 * 255
    mark = Image.fromarray(a.astype(np.uint8), "RGBA")
    embers = []
    pts = [(rng.uniform(8, w - 8), rng.uniform(0, h), rng.uniform(0, 1)) for _ in range(14)]
    for f in range(4):
        cv = Canvas(w, h)
        for x, y, ph in pts:
            k = 0.5 + 0.5 * np.sin(TAU * (ph + f / 4))
            for dy in (-h, 0, h):
                cv.add((255, 200, 150) if v == "a" else (180, 220, 255), gauss(cv.dist(x, y + dy), 0.8) * 0.9 * k)
        embers.append(artkit.additive(cv.image()))
    return mark, embers


def lance_art(v):
    mark, embers = lance_scorch(v)
    return dict(line=act3kit.quantize_bright([lance_line(v, f) for f in range(4)], 12),
                column=act3kit.quantize_bright([lance_column(v, f) for f in range(COL_FRAMES)], 32),
                scorch=mark, embers=act3kit.quantize_bright(embers, 12))


def build_lance(v):
    art = lance_art(v)
    if v == LANCE:
        with act3kit.sprites_of("orbital-lance"):
            artkit.write_frames("orbital-lance-line", art["line"], SOURCE)
            artkit.write_frames("orbital-lance-column", art["column"], SOURCE)
            artkit.write_frames("orbital-lance-scorch", [art["scorch"]], SOURCE, single=True)
            artkit.write_frames("orbital-lance-embers", art["embers"], SOURCE)
        print(f"orbital lance {v}: {act3kit.where('orbital-lance')}")
    return art


def tile_down(img, tile, x, top=0, bottom=FH, alpha=1.0, normal=False):
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    y = top
    while y < bottom:
        layer.paste(tile, (int(x - tile.width / 2), int(y)))
        y += tile.height
    if alpha < 1:
        layer.putalpha(layer.getchannel("A").point(lambda a: int(a * alpha)))
    if normal:
        img.alpha_composite(layer)
        return img
    return artkit.add_light(img, layer)


def review_lance(v, art):
    ground = g.r36.mars_ground(55, craters=6, dunes=0.03)
    needler = g.asset("needler_0")
    hive = g.asset("hive-node_0")
    fps = 30
    frames, scorch = [], []
    lx, last = None, None
    foes = [(170, 110), (215, 190), (275, 150), (300, 235), (250, 300)]
    dead = {}
    speed = 40.0                                  # the ground scrolls in the review
    for i in range(int(fps * 5.2)):
        t = i / fps
        step = i * 2
        off = speed * t
        img = ground.crop((0, 0, FW, FH))
        img = Image.fromarray(np.roll(np.array(ground), int(off) % FH, axis=0), "RGBA")
        sx = 240 + 90 * np.sin(max(0.0, t - 1.0) * 0.9)
        sy = 450
        for wx, wy, t0 in scorch:
            img.alpha_composite(art["scorch"], (int(wx - 24), int(wy + off - 16)))
            if t - t0 < 0.8:
                em = art["embers"][(step // 4) % 4].copy()
                em.putalpha(em.getchannel("A").point(lambda a, k=1 - (t - t0) / 0.8: int(a * k)))
                img = artkit.add_light(img, em, (int(wx - 24), int(wy + off - 16)))
        sprite.paste_center(img, hive, 240, int(380 + off) % FH)
        firing = 0.6 <= t < 3.6
        fade = 1.0 if t < 3.6 else max(0.0, 1 - (t - 3.6) / 0.3)
        if t < 0.6:
            lx = sx
        elif lx is not None:
            lx += np.clip(sx - lx, -240 / fps, 240 / fps)
        for k, (x, y) in enumerate(foes):
            if k in dead:
                if t - dead[k] < 0.6:
                    g.explosion(img, x, y, (t - dead[k]) / 0.6)
                continue
            sprite.paste_center(img, needler, x, y)
            if firing and abs(x - lx) < 20 + 12:
                dead[k] = t
        g.draw_ship(img, sx, sy, t)
        if t < 0.6 and (step // 6) % 2 == 0:
            img = tile_down(img, art["line"][(step // 2) % 4], lx, bottom=sy - 20)
        if t >= 0.6 and fade > 0:
            if firing and (last is None or abs(lx - last[0]) >= 16 or off - last[1] >= 32):
                last = (lx, off)
                for yy in range(0, FH, 32):
                    scorch.append((lx, yy - off, t))
            img = tile_down(img, art["column"][step % COL_FRAMES], lx, alpha=fade)
        g.label(img, 8, FH - 14, f"ORBITAL LANCE ({v.upper()}): LOCK 0.6 S, 40 PX COLUMN 3 S FOLLOWING AT 240 PX/S",
                (150, 200, 240))
        frames.append(img)
    rows = [("ORBITAL-LANCE-LINE: 12X64 TILE, 4 FRAMES, 2 STEPS", art["line"], 4, True),
            ("ORBITAL-LANCE-COLUMN: 72X64 TILE, 8 FRAMES, 1 STEP", art["column"], 2, True),
            ("ORBITAL-LANCE-SCORCH (NORMAL BLEND) AND -EMBERS (ADDITIVE), TILING DOWN", [art["scorch"]] + art["embers"], 3, False),
            ("ON THE FIELD (1X, CROPPED)", [f.crop((60, 0, 420, 540)).resize((180, 270), Image.NEAREST)
                                            for f in frames[6:150:24]], 1, False)]
    title = {"a": "A: PIERCING RAY, RED LINE", "b": "B: DESCENDING RINGS, CHEVRON LINE"}[v]
    sheet = artkit.review_sheet(f"ORBITAL LANCE - PROPOSAL {title} (ROUND 37)", rows, width=1400, batch=act3kit.BATCH)
    act3kit.save_proposal(sheet, [f.resize((300, 338), Image.NEAREST) for f in frames[::2]], CONCEPT,
                          "orbital-lance", v, fps=fps // 2, chosen=LANCE)


# =========================================================================== Shield Overcharge

SHELL, SHELL_FRAMES = 72, 8
FLARE, FLARE_FRAMES = 110, 7
OC_COL = g.OC["b"]["col"]


class Light:
    """A float light accumulator in premultiplied-on-black form."""

    def __init__(self, w, h):
        self.a = np.zeros((h, w, 3))

    def add(self, img, x, y, k=1.0):
        s = np.array(img.convert("RGBA")).astype(np.float64)
        x0, y0 = int(round(x - img.width / 2)), int(round(y - img.height / 2))
        h, w = self.a.shape[:2]
        sx0, sy0 = max(0, -x0), max(0, -y0)
        dx0, dy0 = max(0, x0), max(0, y0)
        ww, hh = min(w - dx0, img.width - sx0), min(h - dy0, img.height - sy0)
        part = s[sy0:sy0 + hh, sx0:sx0 + ww]
        self.a[dy0:dy0 + hh, dx0:dx0 + ww] += part[..., :3] * part[..., 3:4] / 255 * k

    def image(self):
        rgb = np.clip(np.round(self.a), 0, 255)
        alpha = np.where(rgb.max(axis=-1) >= 1, 255, 0)
        return Image.fromarray(np.dstack([rgb, alpha]).astype(np.uint8), "RGBA")


def shell(f):
    base = Image.open(act3kit.ASSETS_SPRITES / "ship-shield_3.png").convert("RGBA")
    t = f / SHELL_FRAMES
    pulse = 0.75 + 0.25 * np.sin(TAU * t)
    lt = Light(SHELL, SHELL)
    sh = g.recolor(base, OC_COL, 1.1)
    lt.add(sh, 36, 36)
    lt.add(sh.resize((70, 70), Image.BILINEAR), 36, 36, 0.7 * pulse)
    cv = Canvas(SHELL, SHELL)
    d = cv.dist(36, 36)
    cv.add(WHITE, gauss(d - 34, 0.9) * 0.55 * pulse)
    rng = np.random.default_rng(4100 + f)
    for _ in range(2):                           # crackle running round the outer shell
        a0 = rng.uniform(0, TAU)
        pts = [(36 + np.cos(a0 + u * 0.5) * 34 + rng.normal(0, 0.8), 36 + np.sin(a0 + u * 0.5) * 34 + rng.normal(0, 0.8))
               for u in np.linspace(0, 1, 6)]
        arcs(cv, pts, (200, 240, 255), glow=0.9, core=0.8)
    lt.add(cv.image(), 36, 36)
    return lt.image()


def flare(f):
    s = f / (FLARE_FRAMES - 1)
    cv = Canvas(FLARE, FLARE)
    d = cv.dist(55, 55)
    r = 14 + 38 * s
    for rr, k in ((r, 1.0), (r * 0.72, 0.7)):
        cv.add(OC_COL, gauss(d - rr, 1.6) * 1.3 * k * (1 - s))
    cv.add(WHITE, gauss(d, 20 * (1 - s) + 2) * 1.5 * (1 - s) ** 2)
    return artkit.additive(cv.image())


def build_overcharge():
    with act3kit.sprites_of("shield-overcharge"):
        artkit.write_frames("shield-overcharge-shell", act3kit.quantize_bright([shell(f) for f in range(SHELL_FRAMES)], 32), SOURCE)
        artkit.write_frames("shield-overcharge-flare", act3kit.quantize_bright([flare(f) for f in range(FLARE_FRAMES)], 24), SOURCE)
    print(f"shield overcharge: {act3kit.where('shield-overcharge')}")


def review_overcharge():
    with act3kit.sprites_of("shield-overcharge"):
        sh = artkit.load_frames("shield-overcharge-shell")
        fl = artkit.load_frames("shield-overcharge-flare")
    ring = artkit.load_frames("ship-shield")
    ground = g.r36.mars_ground(83, craters=8, dunes=0.02)
    fps = 30
    frames = []
    for i in range(fps * 4):
        t = i / fps
        step = i * 2
        img = ground.crop((120, 300, 360, 540))
        x, y = 120, 120
        g.draw_ship(img, x, y, t)
        if 0.5 <= t < 3.2:
            img = artkit.add_light(img, sh[(step // 3) % SHELL_FRAMES], (x - 36, y - 36))
        age = int((t - 0.5) * 60) // 3
        if 0 <= age < FLARE_FRAMES:
            img = artkit.add_light(img, fl[age], (x - 55, y - 55))
        if t < 0.5 and (step // 4) % 4 == 0:
            img = artkit.add_light(img, ring[(step // 2) % 4], (x - 30, y - 30))
        g.label(img, 6, 226, "SHIELD OVERCHARGE: FLARE, DOUBLE SHELL" if t < 3.2 else "SHIELD OVERCHARGE: ENDED", (150, 200, 240))
        frames.append(img)
    rows = [("SHIELD-OVERCHARGE-SHELL: 8 FRAMES, 3 STEPS (THE WHITE DOUBLE SHELL)", sh, 3, True),
            ("SHIELD-OVERCHARGE-FLARE: 7 FRAMES, 3 STEPS (0.35 S)", fl, 2, True),
            ("ROUND THE SHIP (1X)", [f.crop((60, 60, 180, 180)) for f in frames[10:100:12]], 2, False)]
    sheet = artkit.review_sheet("SHIELD OVERCHARGE - FINAL EFFECTS (ROUND 36 G.4 = B)", rows, width=1400, batch=act3kit.BATCH)
    artkit.save_review(sheet, [f.resize((360, 360), Image.NEAREST) for f in frames], CONCEPT, "shield-overcharge", fps=fps)


if __name__ == "__main__":
    artkit.REVIEW_ROUND = act3kit.ROUND
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    review_only = "--review" in sys.argv[1:]
    todo = args or ["emp", "lance", "overcharge"]
    if "emp" in todo:
        if not review_only:
            build_emp()
        review_emp()
    if "lance" in todo:
        for v in ("a", "b"):
            art = lance_art(v) if review_only else build_lance(v)
            review_lance(v, art)
    if "overcharge" in todo:
        if not review_only:
            build_overcharge()
        review_overcharge()
