#!/usr/bin/env python3
"""Concept round 84 - the Act 7 "Beyond the Gate" bosses, world and people (step ART-R-B, M11 design phase,
2026-11-05; everything pending the user's review in round 84).

Each subject is drawn as the draft documents say (the boss documents' *Presentation*, Vrell space's *Moods per
level (Q10)* and *Hazards & set pieces*, the act README's *Act intro and ending*, Level 50's closing gate, the
Choir's and Vorne's portrait briefs). Two variants a/b per subject that differ in something the user can judge.
Storyboards are key frames of the 480x540 play field at 1x drawn by a scripted scene, laid out at half size.

Outputs (sheets are PNG review sheets ~1000 px wide; GIFs are the 480x540 field at 1x):
  design/enemies/bosses/vornes-chimera/concept/vornes-chimera-r84-{a,b}.{png,gif}
      Vorne's Chimera grown from the chosen lifeboat (round 72's seed-pod, its gold capsule half swallowed) at three
      times its size, in Level 48's sickly gold and teal: the source render, the three phases at 1x over the
      citadel (phase 1 the gold Ascendancy parts as grafts, phase 2 the sockets burst into tissue sacs with their
      torn gold rims, phase 3 the capsule torn open with Vorne's silhouette in the seat behind the gold shimmer),
      the fusion's burst and the capsule's opening as frames, the banking frames, the rear sweep, the death (the
      tissue closing over the capsule), the measured gold share.
      a "swollen pod": the gold parts sunk into two swollen tissue shoulders, a short tendril tail (compact, round).
      b "thorned graft": the gold parts held out on bone-thorn arms, plates of the Ascendant's gold hull embedded in
        the tissue like armour, a long thorned tail (spiky, wider, more gold).
  design/enemies/bosses/choir-heart/concept/choir-heart-r84-{a,b}.{png,gif}
      The Choir Heart at the top of the field over Level 50's chamber: the source render, phase 1 (the six
      membranes on its rim, closed, opening, open with their singer's colour, dead), the closing (the walls grow,
      the light goes out), phase 2 (the 320 px corridor, the dark at 8 % ambient with the ship's headlight cone, the
      lit wall edges and the four lime nodes), the opening (the face splitting), phase 3 (the lime-white core, the
      storm), the death (the chain, the collapse). The pulse is a smooth light layer at <= 1 Hz.
      a "the heart": a lobed heart-shaped mass, ribbed top to bottom, the membranes as petals on its lower rim.
      b "the crown": a radial organ of concentric rings (the Herald's ring markings) round a sealed central iris,
        the membranes as six mouths set in the outer ring.
  design/world/vrell-space/concept/vrell-echo-r84-{a,b}.png
      One echo (the Brood Carrier, Level 49's first, the production hull) in the spectral tint over its remembered
      Luna dissolving into tissue, its own bullets as built. a: the tint as the bosses README words it (its colours
      desaturated ~40 % toward violet-white, translucent edges fading to 0 inside its canvas); b "afterimage": the
      same tint, the hull 80 % opaque with two soft afterimages trailing its motion and a slow violet scan band.
  design/world/vrell-space/concept/vrell-moods-r84-{a,b}.png
      Q10's eight moods L43-L50 (L43-L47 are ART-R-A's level stand-ins, enemies_r84.py, imported unchanged so the
      units' and the world's sheets agree; L48-L50 drawn here): each still with its mean luminance and its brightest
      and darkest 200x96 window, and the readability set laid over both windows at 1x (the ship, Rook, a Choir
      Herald and a Needler, the larger, brighter, outlined Vrell bullets). a: Q10 a, a grade per level; b: Q10 b, one
      Vrell palette (round 06's violet and teal), only the light changing.
  design/world/vrell-space/concept/gate-transit-r84-{a,b}.{png,gif}
      Q11's transit storyboard at Level 43's end (the event plane filling the ring's mouth, the formation, the warp,
      the debrief over the violet, Level 44 opening) with the frame-by-frame mean luminance plotted under it.
      a: Q11 a, a 1.5 s smooth warp toward the ring's centre grading to violet (brightness one way only);
      b: Q11 b, the sketch's 0.5 s full colour inversion (shown for the comparison; the plot shows the flash).
  design/campaign/act-7-beyond-the-gate/concept/act-7-still-r84-{a,b}.png  and  act-7-still-r84-sheet.png
      The title card's still, 960x540, and the card over each (darkened, the letterbox, the chrome
      `ACT VII / BEYOND THE GATE`). a "the ring": the gate filling the frame, its glyphs lit, the fleet's lights small
      at its rim (J1: from Level 43's backdrop); b "the mouth": looking through the ring at the event plane's violet,
      the Steadfast's group's lights strung toward it.
  design/campaign/act-7-beyond-the-gate/concept/closing-gate-r84-{a,b}.png
      Level 50's closing-gate scene: the hive greying, the race through the dead chambers, the event plane closing in
      its ring, the warp reversed, the near side with Neptune and the Steadfast's group, the plane going out and the
      glyphs going dark one by one; the luminance plot (no flash, no inversion). a: the plane shrinks to a point
      (the document's); b "the iris": six stone leaves of the ring slide shut over it like a camera iris.
  design/campaign/act-7-beyond-the-gate/concept/ending-pages-r84-{a,b}.png
      The four ending pages' images at the briefing size (672x240): a as the act README describes them (the plot of
      the solar system greyed out, Varga's lock diagram and Vorne's flagged file, Rook on the flight deck, the dark
      ring with the listening buoy); b the same beats as scenes (a Vrell swarm hanging still before Earth, the ring's
      glyphs over Varga's lab window, Rook's helmet on a scorched wing, the buoy small before the ring from afar).
  design/story/characters/concept/choir-frame-r84-{a,b}.{png,gif}
      The Choir's last line: its portrait (the production pattern, round 08's sigil) forming something face-like,
      then scattering, at radio 72 px and briefing 144 px. a "the sigil gathers": the rings draw into two hollows and
      a mouth; b "pressed through": a pale mask pushes out of the tissue like a face under a membrane, then sinks.
  design/story/characters/concept/vorne-fused-r84-{a,b}.png
      Vorne's fused portrait for Act 7 (the enemy channel's amber screen): his production portraits (neutral, smug,
      rage; radio 72 px, briefing 144 px) overgrown by the Choir's teal pattern. a "the edge": teal tissue and veins
      creeping in from the left edge over his cheek and collar; b "the chorus": the Choir's pattern ghosted through
      the whole screen behind him and teal veins under the skin from the temple (his Act 5 shimmer grown).

Every glow, haze, pulse and shadow here is a smooth float gradient drawn once (no ordered dither, no posterising of
the haze; PNGs are 24-bit) and fades to 0 inside its canvas. Production sprites (the ship, Rook, the Ascendant's parts,
the Vrell units, the portraits) are read from assets/; nothing is written to assets/. Models live in this file on the
frozen ``render`` package; act6_r72.py's helpers and enemies_r84.py's backdrops and Vrell bullets are imported
unchanged; the chrome title from tools/art/act_titles.py.
Run: python3 tools/concept/act7_r84.py [chimera] [heart] [echo] [moods] [transit] [still] [closing] [ending] [frame]
     [fused] [a|b]   (no subject = everything)
"""
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE))
sys.path.insert(0, str(HERE.parent / "art"))
import act6_r72 as b6  # noqa: E402  (sheets, glows, the boss bar, the Ascendant's materials and render_region)
import enemies_r84 as u84  # noqa: E402  (ART-R-A's L43-L47 backdrops, the Vrell bullets)
from render import raster, sdf  # noqa: E402
from render import enemy_models as em  # noqa: E402
from render.config import ROOT  # noqa: E402
from render.enemy_models import A_GLASS, A_GOLD, A_GUN  # noqa: E402
from render.sdf import (Material, mirror_x, sd_box, sd_capsule, sd_cylinder_z, sd_ellipsoid,  # noqa: E402
                        sd_sphere, union)

FW, FH, TAU = 480, 540, 2 * np.pi
SW, SH = 960, 540
SUB = "CONCEPT ROUND 84 - BEYOND THE GATE"
DESIGN = ROOT / "design"
SPR = ROOT / "assets" / "sprites"
YY, XX = np.mgrid[0:FH, 0:FW].astype(np.float64)
V_BODY, V_DARK, V_SEAM, V_GLOW, V_EYE, V_BONE, V_SAC = b6.V_BODY, b6.V_DARK, b6.V_SEAM, b6.V_GLOW, b6.V_EYE, \
    b6.V_BONE, b6.V_SAC

GOLD = (255, 196, 64)
TEAL = (0, 255, 154)
LIME = (190, 255, 60)
VIOLET = (154, 77, 255)
VIOLET_W = (226, 206, 255)
RV = (200, 60, 150)                       # the Heart's red-violet pulse
AMBER = (255, 176, 40)

save_png, to_img, arr, lum, window = b6.save_png, b6.to_img, b6.arr, b6.lum, b6.window
glow_at, light, over, put, fade, tinted = b6.glow_at, b6.light, b6.over, b6.put, b6.fade, b6.tinted
text, wrap, panel_grid, field_cell = b6.text, b6.wrap, b6.panel_grid, b6.field_cell
ship, rook, spore, line_glow, boss_bar, warn_banner = b6.ship, b6.rook, b6.spore, b6.line_glow, b6.boss_bar, \
    b6.warn_banner
vorb, vneedle, smooth_steps = u84.vorb, u84.needle, u84.smooth_steps
_fbm, _ramp, _arr = u84._fbm, u84._ramp, u84._arr


def compose(title, sections, notes, width=1000):
    return b6.compose(title, sections, notes, width=width, sub=SUB)


def spr(name):
    p = SPR / f"{name}.png"
    if not p.exists():
        p = SPR / f"{name}_0.png"
    return Image.open(p).convert("RGBA")


def cap(img, s, y=None, col=(255, 214, 140)):
    """A short label in a dark box at the bottom-left of a field frame (in place)."""
    lines = wrap(s, (img.width - 24) // 6)
    y = img.height - 8 - 10 * len(lines) if y is None else y
    d = ImageDraw.Draw(img)
    w = max(raster.text_width(t) for t in lines) + 10
    d.rectangle([4, y - 3, 4 + w, y + 10 * len(lines)], fill=(8, 8, 14, 215))
    for i, t in enumerate(lines):
        text(img, 9, y + 10 * i, t, col)
    return img


def soft(a, sigma):
    return b6.gaussian_filter(a, sigma)


def smooth01(x):
    x = np.clip(x, 0, 1)
    return x * x * (3 - 2 * x)


def save_gif(frames, path, ms=100):
    path.parent.mkdir(parents=True, exist_ok=True)
    rgb = [f.convert("RGB") for f in frames]
    picks = rgb[:: max(1, len(rgb) // 12)]                 # one shared palette from a montage of the frames
    w, h = picks[0].size
    mont = Image.new("RGB", (w, h * len(picks)))
    for i, f in enumerate(picks):
        mont.paste(f, (0, h * i))
    ref = mont.quantize(colors=255, method=Image.Quantize.MEDIANCUT, dither=Image.Dither.NONE)
    pal = [f.quantize(palette=ref, dither=Image.Dither.NONE) for f in rgb]
    pal[0].save(path, save_all=True, append_images=pal[1:], duration=ms, loop=0, optimize=True, disposal=1)
    print(f"wrote {path.relative_to(ROOT)}", flush=True)


def lum_plot(values, width=960, height=70, marks=(), label="MEAN LUMINANCE PER FRAME"):
    """A small line plot of the frames' mean luminance (0..255): the photosensitivity check."""
    img = Image.new("RGBA", (width, height), (16, 18, 26, 255))
    d = ImageDraw.Draw(img)
    n = len(values)
    for g in (64, 128, 192):
        y = height - 4 - g / 255 * (height - 14)
        d.line([0, y, width, y], fill=(40, 44, 60, 255))
    pts = [(4 + i * (width - 8) / max(1, n - 1), height - 4 - v / 255 * (height - 14)) for i, v in enumerate(values)]
    d.line(pts, fill=(255, 214, 120, 255), width=2)
    for i, s in marks:
        x = 4 + i * (width - 8) / max(1, n - 1)
        d.line([x, 10, x, height], fill=(120, 160, 220, 255))
        text(img, x + 3, 12, s, (150, 180, 230))
    text(img, 6, 2, label, raster.LABEL_DIM)
    return img


# =========================================================================== backdrops L48-L50 (Q10 a)

def citadel_bg(seed=4801, h=FH):
    """Level 48: the Vrell-grown citadel's decks seen from above: dull gold wreckage plates (a step paler and less
    saturated than the Chimera's gold) overgrown by teal-black tissue with teal seams; tower stumps."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    hgt = np.zeros((h, w))
    plate = np.zeros((h, w))
    for _ in range(int(w * h / 9000)):                    # the gold wreckage: tilted rectangular plates
        cx, cy = rng.uniform(-30, w + 30), rng.uniform(-30, h + 30)
        a = rng.uniform(0, np.pi)
        L, W = rng.uniform(30, 80), rng.uniform(14, 36)
        c, s = np.cos(a), np.sin(a)
        u, v = (xx - cx) * c + (yy - cy) * s, -(xx - cx) * s + (yy - cy) * c
        inside = np.clip(np.minimum(L - np.abs(u), W - np.abs(v)) / 2.0, 0, 1)
        z = rng.uniform(0.3, 1.0)
        hgt = np.maximum(hgt, inside * z)
        plate = np.maximum(plate, inside)
    growth = _fbm(w, h, 70, seed + 1, 5)
    tissue = smooth01((growth - 0.42) * 4.0)              # 0 = bare gold, 1 = grown over
    tex = _fbm(w, h, 10, seed + 2, 3)
    th = 0.6 * soft(tissue, 3.0) + 0.15 * tex
    H = hgt * (1 - tissue) * 0.8 + th
    gy, gx = np.gradient(soft(H, 1.2) * 22)
    shade = np.clip(0.8 + (-gx - gy) * 0.8, 0.3, 1.35)
    gold = _ramp(0.25 + 0.6 * hgt, [(0, "2a2010"), (0.45, "6a5530"), (0.8, "9a8250"), (1, "b6a072")])
    gold = gold * (1 - 0.18 * (np.abs(((xx + yy * 0.3) % 22) - 11) < 0.7))[..., None]
    flesh = _ramp(np.clip(0.2 + 0.6 * th, 0, 1), [(0, "040a0a"), (0.5, "0e2826"), (1, "1e4a46")])
    rgb = (gold * plate[..., None] + np.array((6, 10, 10)) * (1 - plate[..., None])) * (1 - tissue[..., None]) \
        + flesh * tissue[..., None]
    rgb *= shade[..., None]
    seam = np.exp(-(((growth - 0.42) * 4.0) / 0.12) ** 2) * (0.5 + 0.5 * tex)
    vein = np.exp(-(np.abs(np.sin(_fbm(w, h, 30, seed + 3, 3) * 22)) / 0.08) ** 2) * tissue
    rgb += smooth_steps(np.clip(seam * 0.3 + vein * 0.15, 0, 1))[..., None] * np.array((0, 150, 110))
    haze = smooth_steps(np.clip((_fbm(w, h, 160, seed + 4, 3) - 0.45) * 1.4, 0, 1)) * 0.18
    rgb = rgb * (1 - haze[..., None]) + np.array((20, 70, 64)) * haze[..., None]
    return _arr(rgb)


def spectral(rgb, k=0.4):
    """The echoes' spectral tint: colours desaturated by k toward a cold violet-white of their own brightness."""
    l = rgb @ np.array([0.3, 0.59, 0.11])
    vw = np.array(VIOLET_W) / 255.0
    return rgb * (1 - k) + (l[..., None] * vw * 1.08) * k


def luna_mem(seed=4901, h=FH):
    """Luna as the Choir remembers it: grey regolith and craters, low contrast (the memory is soft)."""
    w = FW
    rng = np.random.default_rng(seed)
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    H = _fbm(w, h, 60, seed, 5) * 0.5
    for _ in range(26):
        cx, cy, r = rng.uniform(0, w), rng.uniform(0, h), rng.uniform(8, 46)
        d = np.hypot(xx - cx, yy - cy) / r
        H += (-0.6 * np.clip(1 - d, 0, 1) ** 1.5 + 0.35 * np.exp(-((d - 1) / 0.18) ** 2)) * (r / 46) ** 0.5
    gy, gx = np.gradient(soft(H, 1.0) * 30)
    shade = np.clip(0.85 + (-gx - gy) * 0.9, 0.35, 1.4)
    return _ramp(0.35 + 0.4 * H, [(0, "2a2a2e"), (0.5, "6a6a6e"), (1, "a4a4a6")]) * shade[..., None]


def mars_mem(seed=4911, h=FH):
    w = FW
    H = _fbm(w, h, 50, seed, 5)
    gy, gx = np.gradient(soft(H, 1.0) * 26)
    shade = np.clip(0.85 + (-gx - gy) * 0.9, 0.35, 1.4)
    return _ramp(H, [(0, "3a1a10"), (0.5, "8a4426"), (1, "c27a4a")]) * shade[..., None]


def dissolve_mask(seed, h=FH, edge=0.26, t=0.0):
    """1 = the remembered world, 0 = the dark tissue it dissolves into at its sides (t grows the tissue)."""
    w = FW
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    n = _fbm(w, h, 46, seed, 4)
    side = np.minimum(xx, w - 1 - xx) / (w * edge)
    return smooth01((side - 0.4 + (n - 0.5) * 0.8 - t) * 2.6)


def tissue_rgb(seed, h=FH, base=("05030a", "1a0c22", "3a1c40")):
    w = FW
    n = _fbm(w, h, 26, seed, 4)
    vein = np.exp(-(np.abs(np.sin(_fbm(w, h, 40, seed + 1, 3) * 18)) / 0.1) ** 2)
    rgb = _ramp(n, [(0, base[0]), (0.6, base[1]), (1, base[2])])
    return rgb + smooth_steps(vein * 0.6)[..., None] * np.array((90, 40, 110)) * 0.6


def memory_bg(seed=4901, h=FH, world="luna", t=0.0):
    """Level 49: a remembered world in the spectral tint dissolving into dark tissue at its edges."""
    base = luna_mem(seed, h) if world == "luna" else mars_mem(seed + 10, h)
    m = dissolve_mask(seed + 20, h, t=t)
    tis = tissue_rgb(seed + 30, h)
    rim = np.exp(-((m - 0.5) / 0.16) ** 2) * 0.5
    rgb = spectral(base, 0.4) * m[..., None] + tis * (1 - m[..., None])
    rgb += smooth_steps(rim)[..., None] * np.array((150, 110, 200))
    return _arr(rgb)


def chamber_rgb(seed=5001, h=FH, pulse=0.6, light_k=1.0):
    """Level 50: the hive core, near-black violet tissue ribs converging on the Heart above, lit by its slow
    red-violet pulse (pulse 0..1, smooth; its brightest stays well below a bullet core)."""
    w = FW
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    hx_, hy = 240.0, -60.0
    ang = np.arctan2(xx - hx_, yy - hy)
    dist = np.hypot(xx - hx_, yy - hy)
    warp = (_fbm(w, h, 90, seed, 3) - 0.5) * 0.5
    rib = np.abs(np.sin((ang + warp) * 16)) ** 0.6
    ring = 0.5 + 0.5 * np.sin(dist / 34.0 + warp * 6)
    tex = _fbm(w, h, 16, seed + 1, 3)
    v = np.clip(0.18 + 0.32 * rib * (0.6 + 0.4 * ring) + 0.12 * (tex - 0.5), 0, 1)
    rgb = _ramp(v, [(0, "020104"), (0.4, "0c0510"), (0.75, "1a0c20"), (1, "2a1630")])
    fall = np.exp(-(dist / (330 + 60 * pulse)) ** 2)
    rgb = rgb * (0.45 + 0.8 * fall * (0.5 + 0.5 * pulse) * light_k)[..., None]
    rgb += (fall * (0.12 + 0.2 * pulse) * light_k)[..., None] * np.array((90, 20, 60))
    return rgb


def chamber_bg(seed=5001, h=FH, pulse=0.6):
    return _arr(chamber_rgb(seed, h, pulse))


# =========================================================================== 1 Vorne's Chimera

ST = (240, 150)                          # the station: model origin in screen px (PPU 100: 1 unit = 100 px)
CH_BOX = (90, 0, 300, 290)               # the body's render box on the screen (x0, y0, w, h)
BATT = [(-70, -10), (70, -10)]            # parts relative to the station centre, screen px (y down)
RACK, BAY, CAPS = (0, -60), (0, 54), (0, 8)


def chim_mats(open_=0.0):
    mats = b6.asc_mats(glow=1.0, core=0.6 + 0.6 * open_)
    vr = u84.e60.vrell_mats("1a5450", "0a201e", "9a9a80", TEAL, LIME, glow=0.45)
    for i, m in enumerate(vr):
        mats[b6.V0 + i] = m
    mats[A_GOLD] = Material((0.66, 0.5, 0.2), metal=0.85, shininess=80, spec=0.95,
                            emission=tuple(np.array(TEAL) / 255 * 0.55), emission_pattern=em._veins(16, 0.7, 0.12))
    mats[A_GLASS] = Material((0.08, 0.06, 0.03), emission=(1.0, 0.75, 0.35))
    mats[A_GUN] = Material((0.05, 0.05, 0.06), metal=0.3, shininess=40, spec=0.4,
                           emission=tuple(np.array(TEAL) / 255 * 0.35), emission_pattern=em._veins(30, 1.3, 0.2))
    return mats


def _seat(p):
    """Vorne grown into the seat: head and shoulders, a dark silhouette (the capsule open, phase 3)."""
    head = sd_sphere(p, (0.0, -0.08, 0.3), 0.085)
    sh = sd_ellipsoid(p, (0.0, 0.08, 0.26), (0.19, 0.1, 0.08))
    return np.minimum(head, sh)


def chimera_model(v, open_=0.0):
    """The Chimera's body (no parts): the lifeboat's seed-pod grown three times its size, the gold capsule half
    swallowed in its centre (canopy facing down: the boss faces the player), the sockets' gold collars."""
    def scene(p):
        q = mirror_x(p)
        items = []
        if v == "a":
            pod = sd_ellipsoid(p, (0, 0.05, 0.0), (0.74, 0.8, 0.44))
            sh = sd_ellipsoid(q, (0.7, 0.06, 0.02), (0.42, 0.36, 0.32))
            lobes = sd_ellipsoid(q, (0.38, -0.5, 0.0), (0.34, 0.36, 0.3))
            body = sdf.smin(sdf.smin(pod, sh, 0.14), lobes, 0.1)
            tails = [sd_capsule(q, (0.22, 0.7, 0.0), (0.36 + 0.1 * k, 1.08 + 0.12 * k, -0.06), 0.09, 0.03)
                     for k in range(2)]
            tails.append(sd_capsule(p, (0.0, 0.76, 0.0), (0.03, 1.2, -0.05), 0.1, 0.035))
            items += [(np.minimum.reduce(tails), V_BODY)]
            bulge = sd_ellipsoid(q, (0.76, 0.12, 0.2), (0.24, 0.22, 0.18))      # the tissue lipping the graft
            body = sdf.smin(body, bulge, 0.08)
            ribs = []                                      # the Ascendant's gold ribs, grown over and fanned out
            for k, (x1, y1) in enumerate(((0.3, -0.38), (0.5, -0.18), (0.62, 0.32), (0.5, 0.56))):
                pts = [(0.06 + (x1 - 0.06) * s_, 0.6 + (y1 - 0.6) * s_) for s_ in np.linspace(0, 1, 4)]
                zs = [0.44 * np.sqrt(max(0.05, 1 - (x / 0.78) ** 2 - ((y - 0.05) / 0.84) ** 2)) - 0.02 for x, y in pts]
                for j in range(3):
                    ribs.append(sd_capsule(q, (pts[j][0], pts[j][1], zs[j]), (pts[j + 1][0], pts[j + 1][1], zs[j + 1]),
                                           0.08 - 0.012 * j, 0.068 - 0.012 * j))
            items += [(np.minimum.reduce(ribs), A_GOLD)]
        else:
            pod = sd_ellipsoid(p, (0, 0.02, 0.0), (0.46, 0.84, 0.42))
            lobes = sd_ellipsoid(q, (0.3, -0.52, 0.0), (0.26, 0.32, 0.26))
            body = sdf.smin(pod, lobes, 0.08)
            arm = np.minimum(sd_capsule(q, (0.3, 0.14, 0.04), (0.92, 0.1, 0.1), 0.12, 0.08),
                             sd_capsule(q, (0.92, 0.1, 0.1), (1.04, -0.22, 0.04), 0.08, 0.02))
            thorns = [sd_capsule(q, (0.5 + 0.12 * k, 0.16, 0.12), (0.6 + 0.16 * k, 0.4 + 0.04 * k, 0.04), 0.035, 0.006)
                      for k in range(2)]
            thorns += [sd_capsule(q, (0.36, -0.62, 0.05), (0.62, -0.78, -0.02), 0.04, 0.006),
                       sd_capsule(q, (0.44, 0.5, 0.05), (0.74, 0.62, -0.02), 0.035, 0.006)]
            tails = [sd_capsule(q, (0.12, 0.72, 0.0), (0.26 + 0.12 * k, 1.12 + 0.14 * k, -0.06), 0.07, 0.025)
                     for k in range(2)]
            tails.append(sd_capsule(p, (0.0, 0.8, 0.0), (0.0, 1.32, -0.05), 0.08, 0.03))
            spines = [sd_capsule(q, (0.08, 0.98 + 0.12 * k, 0.04), (0.2, 1.04 + 0.12 * k, 0.0), 0.025, 0.005)
                      for k in range(3)]
            items += [(arm, V_BONE), (np.minimum.reduce(thorns + spines), V_BONE),
                      (np.minimum.reduce(tails), V_BODY)]
            plates = []
            for (x, y, a, w_, h_) in ((0.2, 0.42, 0.5, 0.15, 0.1), (0.3, 0.2, 0.2, 0.13, 0.09),
                                      (0.1, 0.62, 0.9, 0.12, 0.08), (0.32, -0.22, -0.3, 0.12, 0.09),
                                      (0.24, -0.02, 0.1, 0.1, 0.08), (0.18, 0.84, 1.2, 0.09, 0.06)):
                z = 0.42 * np.sqrt(max(0.05, 1 - (x / 0.46) ** 2 - ((y - 0.02) / 0.84) ** 2))
                pr = sdf.rotate_z(q - np.array((x, y, z)), a)
                plates.append(sd_box(pr, (0, 0, 0), (w_, h_, 0.035), 0.025))
            items += [(np.minimum.reduce(plates), A_GOLD)]
        cy, cyy = CAPS[1] / -100.0, 0.0
        cut = 0.3 + 0.12 * open_
        body = np.maximum(body, -sd_sphere(p, (0.0, cy - 0.14, 0.42), cut))      # the capsule shows through
        capsule = sd_capsule(p, (0.0, cy + 0.2, 0.1), (0.0, cy - 0.24, 0.18), 0.23)
        collar = np.maximum(sd_cylinder_z(p, (0, cy - 0.04, 0.14), 0.3, 0.06),
                            -sd_cylinder_z(p, (0, cy - 0.04, 0.14), 0.25, 0.2))
        if open_ > 0:
            cav = sd_ellipsoid(p, (0.0, cy - 0.12, 0.42), (0.17, 0.25, 0.22 * open_ + 0.01))
            capsule = np.maximum(capsule, -cav)
            seat = _seat(p - np.array((0.0, cy - 0.12 + cyy, 0.0)))
            back = sd_ellipsoid(p, (0.0, cy - 0.12, 0.2), (0.15, 0.22, 0.03))       # the lit cockpit behind him
            items += [(seat, V_DARK), (back, A_GLASS)]
            lid = sd_ellipsoid(sdf.rotate_x(p - np.array((0.0, cy + 0.08, 0.36)), -0.9 * open_),
                               (0.0, -0.06, 0.0), (0.13, 0.12, 0.03))
            items += [(lid, A_GLASS)]
        else:
            can = sd_ellipsoid(p, (0.0, cy - 0.24, 0.34), (0.11, 0.16, 0.07))
            items += [(can, A_GLASS)]
        sockets = [np.maximum(sd_cylinder_z(p, (x / 100, -y / 100, 0.3), 0.27, 0.05),
                              -sd_cylinder_z(p, (x / 100, -y / 100, 0.3), 0.21, 0.2)) for x, y in BATT]
        sockets.append(np.maximum(sd_cylinder_z(p, (0, -RACK[1] / 100, 0.36), 0.22, 0.05),
                                  -sd_cylinder_z(p, (0, -RACK[1] / 100, 0.36), 0.17, 0.2)))
        bay = np.maximum(sd_box(p, (0, -BAY[1] / 100, 0.3), (0.3, 0.13, 0.05), 0.02),
                         -sd_box(p, (0, -BAY[1] / 100, 0.3), (0.25, 0.09, 0.2)))
        items = [(body, V_SAC), (capsule, A_GOLD), (collar, A_GOLD), (np.minimum.reduce(sockets), A_GOLD),
                 (bay, A_GOLD)] + items
        return union(*items, k=0.015)
    return scene, chim_mats(open_)


CH = {}


def load_chimera(variants):
    with b6.pool() as ex:
        for v in variants:
            for o in (0.0, 0.5, 1.0):
                key = (v, o)
                if key not in CH:
                    CH[key] = b6.render_region(ex, chimera_model, (v, o), CH_BOX, factor=3, n=12)


def chim_parts():
    """The Ascendant's production parts the Chimera carries as grafts (assets/sprites, read only)."""
    if "batt" not in CH:
        CH["batt"] = spr("ascendant-battery_0")
        CH["batt-dead"] = spr("ascendant-battery_2")
        CH["panel"] = spr("ascendant-panel_0")
        CH["sac"] = [spr(f"ascendant-sac_{i}") for i in range(5)]
        CH["sac-dead"] = spr("ascendant-sac-dead")
        CH["rack"] = (spr("ascendant-rack-l_0"), spr("ascendant-rack-r_0"))
        CH["rack-open"] = (spr("ascendant-rack-l_3"), spr("ascendant-rack-r_3"))
        CH["bay"] = (spr("ascendant-bay-l_0"), spr("ascendant-bay-r_0"))
        CH["bay-open"] = (spr("ascendant-bay-l_3"), spr("ascendant-bay-r_3"))
    return CH


def torn_rim(img, x, y, r, seed, k=1.0):
    """A socket's torn gold rim: jagged gold shards round a hole (phase 2)."""
    d = ImageDraw.Draw(img)
    rng = np.random.default_rng(seed)
    for a in np.linspace(0, TAU, 14, endpoint=False) + rng.uniform(0, 0.4):
        rr = r + rng.uniform(-2, 4)
        L = rng.uniform(4, 9)
        pts = [(x + np.cos(a - 0.16) * rr, y + np.sin(a - 0.16) * rr), (x + np.cos(a) * (rr + L), y + np.sin(a) * (rr + L)),
               (x + np.cos(a + 0.18) * rr, y + np.sin(a + 0.18) * rr)]
        d.polygon(pts, fill=(int(170 * k), int(130 * k), int(56 * k), 255), outline=(40, 30, 10, 255))


def chimera_layer(v, phase=1, open_=None, dead=(), sacs=(1, 1, 1), burst=0.0, flare=0.0, closing=0.0, bank=0.0,
                  t=0.0):
    """The Chimera with its parts in a phase's state, an RGBA image of CH_BOX's size (the station at ST)."""
    P = chim_parts()
    o = (1.0 if phase == 3 else 0.0) if open_ is None else open_
    body = CH[(v, min((0.0, 0.5, 1.0), key=lambda s: abs(s - o)))].copy()
    img = Image.new("RGBA", body.size, (0, 0, 0, 0))
    img.alpha_composite(body)
    ox, oy = ST[0] - CH_BOX[0], ST[1] - CH_BOX[1]
    if phase == 1:
        for i, (x, y) in enumerate(BATT):
            if f"batt{i}" in dead:
                put(img, P["batt-dead"], ox + x, oy + y)
                continue
            put(img, P["batt"], ox + x, oy + y)
            side = -1 if x < 0 else 1
            pn = P["panel"].rotate(side * 40, resample=Image.BICUBIC, expand=False)    # convex 40 deg outward
            put(img, pn, ox + x, oy + y)
        rk = P["rack"] if "rack" not in dead else P["rack-open"]
        put(img, rk[0], ox + RACK[0] - 13, oy + RACK[1])
        put(img, rk[1], ox + RACK[0] + 13, oy + RACK[1])
        by = P["bay"] if "bay" not in dead else P["bay-open"]
        put(img, by[0], ox + BAY[0] - 18, oy + BAY[1])
        put(img, by[1], ox + BAY[0] + 18, oy + BAY[1])
    else:
        socks = [BATT[0], BATT[1], RACK]
        for i, (x, y) in enumerate(socks):
            torn_rim(img, ox + x, oy + y, 20, 84 + i)
            s = sacs[i]
            if s <= 0:
                put(img, P["sac-dead"], ox + x, oy + y)
            else:
                sp = P["sac"][int(t * 6 + i) % 5]
                if s < 1:
                    sp = sp.resize((max(8, int(sp.width * (0.5 + 0.5 * s))),) * 2, Image.LANCZOS)
                put(img, sp, ox + x, oy + y)
        by = P["bay-open"]
        put(img, by[0], ox + BAY[0] - 18, oy + BAY[1])
        put(img, by[1], ox + BAY[0] + 18, oy + BAY[1])
    a = arr(img)
    rgb = a[..., :3]
    if burst > 0:                                       # the fusion's pain: teal light from every seam
        for x, y in [BATT[0], BATT[1], RACK, (0, 0)]:
            glow_at(rgb, ox + x, oy + y, 22 + 16 * burst, TEAL, 0.55 * burst)
    if flare > 0:                                       # the rear sweep's tell: the far flank's tissue flares teal
        glow_at(rgb, ox + 110 * flare_side(v), oy + 10, 40, TEAL, 0.8 * flare)
    if phase == 3 or o > 0:                             # the gold hex shimmer over the open capsule
        cx, cy = ox + CAPS[0], oy + CAPS[1] + 4
        hexes = hex_shimmer(a.shape[1], a.shape[0], cx, cy, 34, t)
        rgb += hexes[..., None] * np.array((255, 214, 120)) * 0.3 * min(1.0, o * 2)
    if closing > 0:                                     # the tissue closing over the open capsule
        cx, cy = ox + CAPS[0], oy + CAPS[1]
        yy, xx = np.mgrid[0:a.shape[0], 0:a.shape[1]]
        r = np.hypot((xx - cx) / 40, (yy - cy) / 48)
        cov = smooth01((closing * 1.05 - r) * 4) * (a[..., 3] > 0)
        z = np.sqrt(np.clip(1 - r ** 2, 0, 1))
        shade = np.clip(0.35 + 0.45 * z - 0.35 * (xx - cx) / 40 - 0.35 * (yy - cy) / 48, 0.15, 1.2)
        nn = u84._fbm(a.shape[1], a.shape[0], 8, 9, 3)
        tis = np.array((16, 60, 56)) * shade[..., None] * (0.8 + 0.4 * nn)[..., None]
        vein = np.exp(-(np.abs(np.sin(nn * 20)) / 0.12) ** 2) * z
        tis += vein[..., None] * np.array(TEAL) * 0.35
        rgb[:] = rgb * (1 - cov[..., None]) + tis * cov[..., None]
    a[..., :3] = np.clip(rgb, 0, 255)
    out = Image.fromarray(a.astype(np.uint8), "RGBA")
    if bank:
        out = out.rotate(-bank, resample=Image.BICUBIC, center=(ox, oy))
    return out


def flare_side(v):
    return 1


def hex_shimmer(w, h, cx, cy, r, t):
    """The capsule's gold hex shield: faint hex lines on a soft disc fading to 0 at its rim (smooth)."""
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    u, v = (xx - cx) / 7.0, (yy - cy) / 7.0
    q = np.stack([u * 2 / 3 * 1.5, (-u / 3 + np.sqrt(3) / 3 * v) * 1.5], -1)
    fr = np.abs(((q + 0.5) % 1.0) - 0.5)
    line = np.exp(-(np.min(np.abs(fr - 0.5), axis=-1) / 0.06) ** 2)
    disc = np.exp(-(np.hypot(xx - cx, yy - cy) / r) ** 4)
    return (0.25 + 0.75 * line) * disc * (0.75 + 0.25 * np.sin(t * 5))


def put_chimera(img, lay, x=ST[0], y=ST[1] - 10):
    """Paste a chimera_layer with its station at (x, y) on a field image (in place)."""
    img.alpha_composite(lay, (int(x - (ST[0] - CH_BOX[0])), int(y - (ST[1] - CH_BOX[1]))))
    return img


def gold_share(lay):
    """The share of the silhouette that reads gold (warm hue, saturated, not dark)."""
    a = arr(lay)
    on = a[..., 3] > 128
    r, g, b = a[..., 0], a[..., 1], a[..., 2]
    gold = on & (r > 90) & (r > b * 1.5) & (g > b * 1.15) & (r >= g * 0.95)
    return gold.sum() / max(1, on.sum())


def spiral_vrell(img, x, y, t, arms=2, n=7, col=TEAL, r0=24, dr=17):
    for a in range(arms):
        for k in range(n):
            ang = t * 1.05 + a * TAU / arms - k * 0.32
            rr = r0 + dr * k
            vorb(img, x + np.cos(ang) * rr, y + np.sin(ang) * rr, 4, col)


def chim_frame(v, i, bg):
    """The storyboard's frame i on the citadel."""
    img = bg.copy()
    sx_, sy_ = ST[0], ST[1] - 10
    k = ""
    if i == 0:
        lay = chimera_layer(v, 1)
        put_chimera(img, lay, sx_, sy_)
        boss_bar(img, "VORNE'S CHIMERA", [(1.0, True), (1.0, True), (1.0, True)])
        line_glow(img, [sx_, sy_ + 38, 220, 470], (255, 40, 30), 2, 0.85, core=(255, 200, 180))
        k = "PHASE 1 - THE GOLD: THE LANCE'S LINE FROM THE CAPSULE'S NOSE, THE BATTERIES FROM THE FAR SIDE"
        for j in range(4):
            b6.orb(img, 150 + 40 * j, 300 + 12 * j)
        ship(img, 220, 470)
        rook(img, 270, 490)
    elif i == 1:
        lay = chimera_layer(v, 1, dead=("batt0", "rack"), burst=0.8)
        put_chimera(img, lay, sx_, sy_)
        boss_bar(img, "VORNE'S CHIMERA", [(0.0, True), (1.0, True), (1.0, True)])
        k = "THE FUSION'S PAIN: THE GOLD PLATES BUCKLE, TEAL LIGHT FROM EVERY SEAM (3 S, INVULNERABLE)"
        ship(img, 260, 470)
        rook(img, 300, 490)
    elif i == 2:
        lay = chimera_layer(v, 2, sacs=(1, 0.6, 1), t=0.4)
        put_chimera(img, lay, sx_, sy_)
        boss_bar(img, "VORNE'S CHIMERA", [(0.0, True), (0.7, True), (1.0, True)])
        spiral_vrell(img, sx_, sy_, 0.8, col=TEAL)
        k = "PHASE 2 - THE TISSUE: THE SACS IN THE TORN GOLD SOCKETS, THE SPORE SPIRAL"
        ship(img, 200, 480)
        rook(img, 250, 500)
    elif i == 3:
        lay = chimera_layer(v, 2, sacs=(0.8, 1, 0), t=0.2)
        lay = fade(lay, 1.0)
        put_chimera(img, lay, 380, 420)
        a = arr(img)
        for j in range(3):
            vorb(img, 360 - 24 * j, 360 + 18 * j, 4, TEAL)
        k = "THE REAR SWEEP: OUT OF THE TOP EDGE, UP FROM THE BOTTOM ON THE FAR SIDE, SACS FACING THE REAR MOUNTS"
        ship(img, 140, 400)
        rook(img, 100, 430)
        del a
    elif i == 4:
        lay = chimera_layer(v, 3, t=0.5)
        put_chimera(img, lay, sx_, sy_)
        boss_bar(img, "VORNE'S CHIMERA", [(0.0, True), (0.0, True), (0.8, True)])
        k = "PHASE 3 - BOTH: THE CAPSULE TORN OPEN, VORNE GROWN INTO THE SEAT BEHIND THE GOLD SHIMMER"
        spiral_vrell(img, sx_, sy_ + 10, 2.4, col=LIME, n=5)
        ship(img, 260, 470)
        rook(img, 300, 490)
    else:
        lay = chimera_layer(v, 3, closing=0.85, t=0.5)
        put_chimera(img, lay, sx_, sy_)
        k = "K: THE TISSUE CLOSES OVER THE CAPSULE AS HE SPEAKS; AT 'TRYING TO-' THE CAPSULE BURSTS GOLD-WHITE"
        ship(img, 240, 470)
    return cap(img, k)


def make_chimera(variants):
    load_chimera(variants)
    bg = citadel_bg()
    out = DESIGN / "enemies" / "bosses" / "vornes-chimera" / "concept"
    for v in variants:
        src = CH[(v, 0.0)]
        big = src.resize((src.width * 2, src.height * 2), Image.LANCZOS)
        src_cell = panel_grid([b6.checker(big.width, big.height).convert("RGBA")], 1)
        src_cell.alpha_composite(big)
        p1, p2, p3 = chimera_layer(v, 1), chimera_layer(v, 2, t=0.3), chimera_layer(v, 3, t=0.3)
        shares = [gold_share(p) for p in (p1, p2, p3)]
        phases = panel_grid([over_bg(p, bg) for p in (p1, p2, p3)], 3, pad=8,
                            labels=[f"PHASE 1 THE GOLD  GOLD {shares[0]:.0%}", f"PHASE 2 THE TISSUE  GOLD {shares[1]:.0%}",
                                    f"PHASE 3 BOTH, THE CAPSULE OPEN  GOLD {shares[2]:.0%}"])
        seq = [chimera_layer(v, 1, burst=b) for b in (0.3, 0.7, 1.0)] + \
              [chimera_layer(v, 2, sacs=(0.4, 0.4, 0.4), burst=0.5)] + \
              [chimera_layer(v, 3, open_=o, t=0.2) for o in (0.5, 1.0)] + \
              [chimera_layer(v, 3, closing=c) for c in (0.4, 1.0)]
        seq_cells = [s.resize((s.width // 2, s.height // 2), Image.LANCZOS) for s in seq]
        seq_img = panel_grid(seq_cells, 4, pad=4, labels=["BURST .3", "BURST .7", "BURST 1", "SACS GROW", "OPEN .5",
                                                          "OPEN 1", "CLOSING", "CLOSED"])
        banks = [chimera_layer(v, 1, bank=b).resize((CH_BOX[2] // 2, CH_BOX[3] // 2), Image.LANCZOS)
                 for b in (-12, -6, 0, 6, 12)]
        banks.append(chimera_layer(v, 1, flare=1.0).resize((CH_BOX[2] // 2, CH_BOX[3] // 2), Image.LANCZOS))
        bank_img = panel_grid(banks, 6, pad=4, labels=["-12", "-6", "0", "+6", "+12", "SWEEP TELL"])
        frames = [chim_frame(v, i, bg) for i in range(6)]
        story = panel_grid([field_cell(f, None, 0.5) for f in frames], 4, pad=6)
        what = {"a": "A 'SWOLLEN POD': THE GOLD PARTS SUNK INTO TWO SWOLLEN TISSUE SHOULDERS, THE TISSUE LIPPING OVER "
                     "EACH GRAFT; A SHORT TENDRIL TAIL. COMPACT AND ROUND: THE LIFEBOAT'S SEED-POD GROWN THREE TIMES.",
                "b": "B 'THORNED GRAFT': THE GOLD PARTS HELD OUT ON BONE-THORN ARMS, PLATES OF THE ASCENDANT'S GOLD HULL "
                     "EMBEDDED IN THE TISSUE LIKE ARMOUR, A LONG THORNED TAIL. SPIKIER, WIDER, MORE GOLD."}[v]
        notes = [what,
                 "THE BODY IS AN SDF RENDER GROWN FROM ROUND 72'S CHOSEN LIFEBOAT (THE ROUND SEED-POD, THE GOLD CAPSULE "
                 "HALF SWALLOWED, ITS CANOPY THE ONE WARM SPOT); THE BATTERIES, MIRROR PANELS, RACK, BAY DOORS AND SACS "
                 "ARE THE ASCENDANT'S PRODUCTION SPRITES AS GRAFTS. THE GOLD IS DULLED AND VEINED TEAL (LEVEL 48'S "
                 "SICKLY GOLD AND TEAL); THE CITADEL'S WRECKAGE GOLD IS A STEP PALER (GOLD-ON-GOLD RULE).",
                 "PARTS AT THE DRAFT'S PLACES: BATTERIES (+-70, -10), RACK (0, -60), CAPSULE (0, 10), BAY (0, 50) FROM "
                 "THE STATION. PHASE 2 KEEPS THE SOCKETS' TORN GOLD RIMS; PHASE 3 SHOWS VORNE AS A DARK SILHOUETTE IN "
                 "THE SEAT BEHIND THE HEX SHIMMER. GOLD SHARE = WARM SATURATED PIXELS OF THE SILHOUETTE (BRIEF: ABOUT 35 % "
                 "IN PHASE 1).",
                 "EVERY GLOW (THE BURST, THE FLANK FLARE, THE SHIMMER) IS A SMOOTH FLOAT GRADIENT FADING TO 0 INSIDE THE "
                 "CANVAS; NO ORDERED DITHER."]
        sheet = compose(f"VORNE'S CHIMERA {v.upper()} (R84)",
                        [("SOURCE RENDER, PHASE 1 BODY WITHOUT PARTS (2X)", src_cell),
                         ("THE THREE PHASES AT 1X OVER LEVEL 48'S CITADEL", phases),
                         ("THE FUSION'S BURST, THE SACS, THE CAPSULE OPENING, THE TISSUE CLOSING (0.5X)", seq_img),
                         ("BANKING FRAMES (+-12 DEG, 5 STEPS) AND THE REAR SWEEP'S FLANK TELL (0.5X)", bank_img),
                         ("STORYBOARD (0.5X)", story)], notes)
        save_png(sheet, out / f"vornes-chimera-r84-{v}.png")
        save_gif(chim_gif(v, bg), out / f"vornes-chimera-r84-{v}.gif", ms=100)


def over_bg(lay, bg, pad=0):
    crop = bg.crop((CH_BOX[0], 0, CH_BOX[0] + CH_BOX[2], CH_BOX[3])).copy()
    crop.alpha_composite(lay)
    return crop


def chim_gif(v, bg):
    """The fight's states in the field at 1x: phase 1 weaving, the burst, phase 2 with the spiral, the capsule
    opening, phase 3, the tissue closing. 10 fps."""
    out = []
    seq = [("1", 18), ("burst", 10), ("2", 18), ("open", 8), ("3", 16), ("close", 10)]
    t = 0.0
    for name, n in seq:
        for j in range(n):
            u = j / max(1, n - 1)
            img = bg.copy()
            x = ST[0] + 40 * np.sin(t * 0.9)
            bank = -8 * np.cos(t * 0.9)
            if name == "1":
                lay = chimera_layer(v, 1, bank=bank, t=t)
            elif name == "burst":
                lay = chimera_layer(v, 1, dead=("batt0", "batt1", "rack", "bay"), burst=smooth01(u * 1.4), t=t)
            elif name == "2":
                lay = chimera_layer(v, 2, sacs=(1, 1, 1), bank=bank, t=t)
                spiral_vrell(img, x, ST[1] - 10, t * 2, col=TEAL)
            elif name == "open":
                lay = chimera_layer(v, 3, open_=u, t=t)
            elif name == "3":
                lay = chimera_layer(v, 3, bank=bank, t=t)
            else:
                lay = chimera_layer(v, 3, closing=u, t=t)
            put_chimera(img, lay, x, ST[1] - 10)
            ship(img, 240 + 50 * np.sin(t * 0.6), 470)
            rook(img, 290 + 50 * np.sin(t * 0.6), 492)
            out.append(img)
            t += 0.1
    return out


# =========================================================================== 2 the Choir Heart

HT_BOX = (0, 0, 480, 236)                 # the Heart's render box (the field's top; it runs on above the edge)
MEMB = [(90, 150), (150, 185), (210, 200), (270, 200), (330, 185), (390, 150)]
CORE = (240, 150)
NODES = [("l", 220), ("l", 400), ("r", 220), ("r", 400)]
SONG = [("HERALD'S SONG", (170, 110, 255)), ("NEEDLER THORNS", (200, 220, 236)), ("SPORE DROP", LIME),
        ("SEED SCATTER", TEAL), ("CHOIR SPIRAL", (236, 220, 255)), ("MOTE CLOUD", (255, 120, 200))]
H_BODY, H_RIB, H_DARK, H_CORE, H_PALE = range(5)


def _rib_pattern(v):
    def f(p, n):
        if v == "a":
            x = p[:, 0] + 0.12 * np.sin(p[:, 1] * 3.0)
            return np.exp(-(np.abs(np.sin(x * 7.5)) / 0.42) ** 2)
        r = np.hypot(p[:, 0], p[:, 1] - 2.0)
        ring = np.exp(-(np.abs(np.sin(r * 9.0)) / 0.4) ** 2)
        spoke = np.exp(-(np.abs(np.sin(np.arctan2(p[:, 0], 2.0 - p[:, 1]) * 30)) / 0.3) ** 2) * 0.3
        return np.maximum(ring, spoke * (r > 1.0))
    return f


def heart_mats(v, pulse=0.5, core=0.0):
    rv = np.array(RV) / 255.0
    return [Material((0.16, 0.07, 0.17), metal=0.15, shininess=70, spec=0.8, pattern=em._ridges(22, 0.25, axis=0)),
            Material((0.2, 0.08, 0.2), metal=0.1, shininess=60, spec=0.7,
                     emission=tuple(rv * (0.22 + 0.5 * pulse)), emission_pattern=_rib_pattern(v)),
            Material((0.05, 0.02, 0.06), metal=0.1, shininess=30, spec=0.3),
            Material((0.5, 0.75, 0.3), metal=0.0, shininess=60, spec=0.8,
                     emission=tuple(np.array((0.5, 0.85, 0.3)) * (0.2 + 0.75 * core)),
                     emission_pattern=em._veins(24, 0.5, 0.5)),
            Material((0.6, 0.48, 0.7), metal=0.05, shininess=90, spec=0.8,
                     emission=tuple(np.array((0.3, 0.18, 0.4)) * (0.35 + 0.4 * pulse)))]


def heart_model(v, split=0.0, pulse=0.5):
    """The Heart's mass (no membranes): a: a lobed heart, ribbed top to bottom; b: a domed crown banded with the
    Herald's rings. ``split`` peels the sealed face back from the lime-white core (phase 3)."""
    cy = (150 - CORE[1]) / 100.0                         # the core in model units (screen y 150 -> 0)

    def scene(p):
        q = mirror_x(p)
        if v == "a":
            lobes = sd_ellipsoid(q, (1.0, 1.25, 0.0), (1.35, 1.05, 0.62))
            low = sd_ellipsoid(p, (0.0, 0.62, 0.0), (1.62, 0.95, 0.66))
            tip = sd_ellipsoid(p, (0.0, -0.05, -0.05), (0.62, 0.62, 0.46))
            body = sdf.smin(sdf.smin(lobes, low, 0.3), tip, 0.3)
            vessels = [sd_capsule(q, (0.5 + 0.5 * k, 1.9, 0.3), (0.3 + 0.55 * k, 0.6 - 0.1 * k, 0.5 - 0.05 * k), 0.07,
                                  0.04) for k in range(3)]
            items = [(body, H_RIB), (np.minimum.reduce(vessels), H_BODY)]
        else:
            dome = sd_ellipsoid(p, (0.0, 2.0, -0.2), (2.62, 2.52, 0.95))
            ridges = [np.maximum(np.abs(np.hypot(p[:, 0], p[:, 1] - 2.0) - r) - 0.035,
                                 sd_ellipsoid(p, (0.0, 2.0, -0.12), (2.62, 2.52, 0.95))) for r in (0.95, 1.45, 1.95)]
            boss_ = sd_ellipsoid(p, (0.0, cy, 0.42), (0.42, 0.36, 0.26))
            items = [(sdf.smin(dome, boss_, 0.25), H_RIB), (np.minimum.reduce(ridges), H_BODY)]
        d, m = union(*items, k=0.04)
        if split > 0:                                     # the sealed face peels back from the core
            lens = sd_ellipsoid(p, (0.0, cy, 0.6), (0.18 + 0.4 * split, 0.55 + 0.1 * split, 0.6))
            d = np.maximum(d, -lens)
            cav = sd_ellipsoid(p, (0.0, cy, 0.0), (0.2 + 0.36 * split, 0.5 + 0.08 * split, 0.38))
            core_ = sd_sphere(p, (0.0, cy, 0.18), 0.32)
            flaps = sd_ellipsoid(q, (0.3 + 0.32 * split, cy, 0.42), (0.12, 0.5, 0.1))
            d2, m2 = union((cav, H_DARK), (core_, H_CORE), (flaps, H_BODY))
            m = np.where(d2 < d, m2, m)
            d = np.minimum(d, np.maximum(d2, -lens * 0 + d2))
        else:
            seam = sd_ellipsoid(p, (0.0, cy, 0.0), (0.03, 0.5, 0.8))
            m = np.where((seam < 0.03) & (d < 0.05), H_DARK, m)
        return d, m
    return scene, heart_mats(v, pulse, core=split)


def membrane_model(state="closed", col=(1.0, 1.0, 1.0)):
    """A membrane: five pale violet petals over a singer organ glowing in its song's colour when open."""
    open_ = {"closed": 0.0, "opening": 0.5, "open": 1.0, "dead": 0.15}[state]

    def scene(p):
        petals = []
        for k in range(5):
            a = k * TAU / 5 + 0.3
            pr = sdf.rotate_z(p, -a)
            pr = sdf.rotate_x(pr - np.array((0.0, 0.03 + 0.07 * open_, 0.0)), 1.25 * (1 - open_) + 0.6 * open_)
            petals.append(sd_ellipsoid(pr, (0.0, 0.09, 0.0), (0.085, 0.13, 0.035)))
        organ = sd_sphere(p, (0.0, 0.0, 0.0), 0.08 + 0.05 * open_)
        base = sd_ellipsoid(p, (0.0, 0.0, -0.09), (0.25, 0.25, 0.05))
        return union((base, H_BODY), (organ, H_CORE), (np.minimum.reduce(petals), H_PALE), k=0.01)
    mats = heart_mats("a", 0.5)
    k = {"closed": 0.25, "opening": 1.2, "open": 2.2, "dead": 0.0}[state]
    mats[H_CORE] = Material((0.05, 0.05, 0.05), emission=tuple(np.asarray(col) * k))
    if state == "dead":
        mats[H_PALE] = Material((0.28, 0.24, 0.3), metal=0.0, shininess=20, spec=0.2)
        mats[H_BODY] = Material((0.08, 0.06, 0.08), metal=0.0, shininess=20, spec=0.2)
    return scene, mats


def node_model():
    """A wall node: a lime knot of nerve."""
    def scene(p):
        strands = []
        for k in range(4):
            a = k * TAU / 4 + 0.4
            c, s_ = np.cos(a), np.sin(a)
            strands.append(sd_capsule(p, (0.22 * c, 0.22 * s_, 0.0), (-0.12 * s_, 0.12 * c, 0.12), 0.06, 0.04))
        core_ = sd_sphere(p, (0.0, 0.0, 0.08), 0.1)
        return union((np.minimum.reduce(strands), H_BODY), (core_, H_CORE), k=0.04)
    mats = heart_mats("a", 0.5)
    mats[H_BODY] = Material((0.3, 0.55, 0.12), metal=0.1, shininess=60, spec=0.8,
                            emission=(0.25, 0.5, 0.08), emission_pattern=em._veins(30, 0.4, 0.3))
    mats[H_CORE] = Material((0.05, 0.05, 0.05), emission=(0.85, 1.0, 0.4))
    return scene, mats


def _small_job(job):
    key, builder, args, size, factor = job
    scene, mats = builder(*args)
    w, h = size
    hi = sdf.render(scene, mats, (w * factor, h * factor), w / 100.0, z_top=2.0, steps=110)
    img = u84.sprite.to_image(u84.sprite.downsample(hi, factor), 0.5)
    return key, u84.sprite.sharpen(img, 60)


HT = {}


def load_heart(variants):
    jobs = []
    for i, (_, col) in enumerate(SONG):
        c = np.array(col) / 255.0
        for st in ("closed", "opening", "open"):
            jobs.append(((f"memb{i}", st), membrane_model, (st, tuple(c)), (56, 56), 4))
    jobs.append((("memb", "dead"), membrane_model, ("dead",), (56, 56), 4))
    jobs.append((("node",), node_model, (), (48, 48), 4))
    with b6.pool() as ex:
        for key, im in ex.map(_small_job, [j for j in jobs if j[0] not in HT]):
            HT[key] = im
        for v in variants:
            for sp in (0.0, 0.5, 1.0):
                for pl in (0.0, 1.0):
                    if (v, sp, pl) not in HT:
                        HT[(v, sp, pl)] = b6.render_region(ex, heart_model, (v, sp, pl), HT_BOX, factor=3, n=16)


def heart_body(v, split=0.0, pulse=0.5):
    """The body blended smoothly between its low and high pulse renders (the pulse is a smooth light layer)."""
    s0 = min((0.0, 0.5, 1.0), key=lambda s: abs(s - split))
    lo, hi = arr(HT[(v, s0, 0.0)]), arr(HT[(v, s0, 1.0)])
    pulse = float(np.clip(pulse, 0, 1))
    a = lo * (1 - pulse) + hi * pulse
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def heart_layer(v, phase=1, pulse=0.5, membs=None, split=None, grey=0.0, shrink=0.0):
    """The Heart with its membranes in a state: membs = list of 'closed'|'opening'|'open'|'dead' per membrane."""
    sp = (1.0 if phase == 3 else 0.0) if split is None else split
    img = Image.new("RGBA", (FW, HT_BOX[3]), (0, 0, 0, 0))
    img.alpha_composite(heart_body(v, sp, pulse))
    membs = membs or ["closed"] * 6
    if phase == 1 or membs != ["closed"] * 6:
        for i, (x, y) in enumerate(MEMB):
            st = membs[i]
            key = ("memb", "dead") if st == "dead" else (f"memb{i}", st)
            put(img, HT[key], x, y)
            if st == "open":
                light(img, x, y, 22, SONG[i][1], 0.35)
    a = arr(img)
    if grey > 0:
        l = a[..., :3] @ np.array([0.3, 0.59, 0.11])
        a[..., :3] = a[..., :3] * (1 - grey) + (l[..., None] * np.array((0.55, 0.52, 0.56))) * grey
    out = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    if shrink > 0:
        w, h = out.size
        sm = out.resize((int(w * (1 - 0.25 * shrink)), int(h * (1 - 0.35 * shrink))), Image.LANCZOS)
        out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
        out.alpha_composite(sm, ((w - sm.width) // 2, 0))
    return out


def wall_layer(close=1.0, breathe=0.0, seed=5011, lit=1.0):
    """The phase-2 walls: ribbed tissue growing in from both sides to the 320 px corridor (x 80-400), the inner
    edge lit violet-white so the field's shape reads in the dark; returns (RGBA, inner x left, inner x right)."""
    w = 80 * close + breathe
    yy, xx = YY, XX
    n = _fbm(FW, FH, 30, seed, 4)
    wob = (n - 0.5) * 14 + 6 * np.sin(yy / 37.0)
    dl = (w + wob) - xx                                  # > 0 inside the left wall
    dr = xx - (FW - w - wob)
    d = np.maximum(dl, dr)
    on = smooth01(d / 2.0)
    rib = 0.5 + 0.5 * np.sin(yy / 9.0 + n * 4)
    depth = np.clip(d / 40.0, 0, 1)
    rgb = _ramp(np.clip(0.2 + 0.35 * rib * (1 - 0.5 * depth) + 0.2 * (n - 0.5), 0, 1),
                [(0, "06030a"), (0.5, "1c0e22"), (1, "3c2244")])
    edge = np.exp(-(d / 3.5) ** 2) * (d > -2)
    rgb += (smooth_steps(edge) * lit)[..., None] * np.array((200, 170, 240))
    a = np.dstack([rgb, on * 255])
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"), w, FW - w


def darkness(img, keep, ambient=0.08):
    """Act 4's darkness as a look: everything outside the light pools at ``ambient``; keep = list of
    ('pool', x, y, r) or ('cone', x, y, half_angle_deg, length)."""
    a = arr(img)
    L = np.full((FH, FW), ambient)
    for k in keep:
        if k[0] == "pool":
            _, x, y, r = k
            L = np.maximum(L, np.exp(-((XX - x) ** 2 + (YY - y) ** 2) / (r * r)))
        else:
            _, x, y, half, ln = k
            ang = np.degrees(np.abs(np.arctan2(XX - x, -(YY - y))))
            dist = np.hypot(XX - x, YY - y)
            c = smooth01((half - ang) / 8.0) * np.exp(-(dist / ln) ** 2) * (YY < y + 6)
            L = np.maximum(L, 0.85 * c)
    a[..., :3] *= L[..., None]
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def ring_shots(img, x, y, n, r, col=(170, 110, 255), phase=0.0):
    for a in np.linspace(0, TAU, n, endpoint=False) + phase:
        vorb(img, x + np.cos(a) * r, y + np.sin(a) * r, 4, col)


def heart_frame(v, i):
    """The storyboard's frame i (the field at 1x)."""
    pulse = 0.5 + 0.5 * np.sin(i * 1.3)
    bg = chamber_bg(pulse=pulse)
    img = bg.copy()
    k = ""
    if i == 0:
        lay = heart_layer(v, 1, pulse=0.2)
        img.alpha_composite(fade(lay, 0.55), (0, -110))
        warn_banner(img, "WARNING")
        k = "THE WARNING BEFORE IT SHOWS: THE SCROLL SLOWS, THE PULSE LIGHTS THE CHAMBER FROM THE BOTTOM UP"
        ship(img, 240, 470)
        rook(img, 290, 492)
    elif i == 1:
        img.alpha_composite(heart_layer(v, 1, pulse, membs=["open", "closed", "closed", "closed", "closed", "closed"]))
        boss_bar(img, "CHOIR HEART", [(1.0, True), (1.0, True), (1.0, True)])
        ring_shots(img, *MEMB[0], 16, 60, phase=0.1)
        ring_shots(img, *MEMB[0], 16, 120, phase=0.3)
        k = "PHASE 1 - THE MEMBRANES: ONE IRISES OPEN WHILE IT SINGS (THE HERALD'S RING), THE REST SEALED"
        ship(img, 260, 470)
        rook(img, 310, 492)
    elif i == 2:
        img.alpha_composite(heart_layer(v, 1, pulse, membs=["dead", "dead", "open", "closed", "open", "dead"]))
        boss_bar(img, "CHOIR HEART", [(0.5, True), (1.0, True), (1.0, True)])
        spiral_vrell(img, MEMB[4][0], MEMB[4][1], 1.0, arms=3, n=5, col=(236, 220, 255))
        for j in range(3):
            u84.needle(img, 220 - 6 * j, 260 + 30 * j, np.pi / 2 + 0.1)
        k = "PHASE 1 FROM ITS HALF: TWO SING AT ONCE (THORNS AND THE SPIRAL); THREE DEAD, GREY AND SHRIVELLED"
        ship(img, 200, 470)
        rook(img, 250, 492)
    elif i == 3:
        img.alpha_composite(heart_layer(v, 2, pulse=0.8, membs=["dead"] * 6))
        wl, _, _ = wall_layer(0.6)
        img.alpha_composite(wl)
        img = darkness(img, [("pool", 240, 60, 260), ("cone", 240, 470, 18, 320)], ambient=0.35)
        k = "THE CLOSING: THE WALLS GROW OUT OF THE SIDES, THE LIGHT GOES OUT BUT FOR THE HEART'S GLOW (4 S)"
        ship(img, 240, 470)
        rook(img, 290, 492)
    elif i == 4:
        img.alpha_composite(heart_layer(v, 2, pulse, membs=["dead"] * 6))
        wl, xl, xr = wall_layer(1.0, breathe=6)
        img.alpha_composite(wl)
        pools = [("pool", 240, 60, 230), ("cone", 210, 470, 16, 300), ("pool", 260, 492, 40)]
        for side, y in NODES[:3]:
            x = xl - 6 if side == "l" else xr + 6
            put(img, HT[("node",)], x, y)
            pools.append(("pool", x, y, 46))
        img = darkness(img, pools)
        line_glow(img, [240, 150, 200, 470], (226, 206, 255), 2, 0.8, core=(255, 255, 255))
        vorb(img, 120, 300, 4, VIOLET)
        vorb(img, 360, 340, 4, VIOLET)
        k = "PHASE 2 - THE ARENA: THE 320 PX CORRIDOR, DARK (8 %) BUT THE LIT WALL EDGES, NODES, HEART AND HEADLIGHT"
        ship(img, 210, 470)
        rook(img, 260, 492)
    elif i == 5:
        img.alpha_composite(heart_layer(v, 3, pulse=0.9, membs=["dead"] * 6, split=0.5))
        k = "THE OPENING: THE WALLS FALL BACK, THE SEALED FACE SPLITS ALONG ITS SEAMS FROM THE CORE (4 S)"
        ship(img, 240, 470)
        rook(img, 290, 492)
    elif i == 6:
        img.alpha_composite(heart_layer(v, 3, pulse, membs=["dead"] * 6))
        light(img, CORE[0], CORE[1], 30, (200, 255, 160), 0.35)
        boss_bar(img, "CHOIR HEART", [(0.0, True), (0.0, True), (0.6, True)])
        ring_shots(img, *CORE, 16, 80)
        spiral_vrell(img, CORE[0], CORE[1], 2.0, arms=3, n=6, col=(236, 220, 255), r0=40)
        k = "PHASE 3 - THE CORE: ONE LIME-WHITE WEAK POINT, TWO ATTACKS AT ONCE: THE STORM"
        ship(img, 280, 470)
        rook(img, 330, 492)
    else:
        img.alpha_composite(heart_layer(v, 3, pulse=0.0, membs=["dead"] * 6, grey=0.7, shrink=0.4))
        for j, (x, y) in enumerate(((240, 150), (180, 110), (310, 90), (220, 60))):
            light(img, x, y, 18 + 6 * j, (220, 255, 200), 0.7)
        k = "K: THE HEARTBEAT STOPS, A CHAIN OF LIME-WHITE BURSTS, THE TISSUE COLLAPSING INWARD AND GREYING"
        ship(img, 240, 470)
        rook(img, 290, 492)
    return cap(img, k)


def make_heart(variants):
    load_heart(variants)
    out = DESIGN / "enemies" / "bosses" / "choir-heart" / "concept"
    for v in variants:
        src = heart_layer(v, 1, pulse=0.5)
        src_cell = b6.checker(src.width, src.height).convert("RGBA")
        src_cell.alpha_composite(src)
        mems = []
        for i in range(6):
            for st in ("closed", "opening", "open"):
                mems.append(HT[(f"memb{i}", st)].resize((112, 112), Image.LANCZOS))
        mems.append(HT[("memb", "dead")].resize((112, 112), Image.LANCZOS))
        mems.append(HT[("node",)].resize((96, 96), Image.LANCZOS))
        mlab = [f"{SONG[i][0][:14]} {st[:4].upper()}" for i in range(6) for st in ("closed", "opening", "open")]
        mem_img = panel_grid(mems, 8, pad=4, labels=mlab + ["DEAD", "WALL NODE"])
        pul = panel_grid([heart_body(v, 0.0, p).resize((240, 118), Image.LANCZOS) for p in (0.0, 0.5, 1.0)] +
                         [heart_body(v, 1.0, 0.7).resize((240, 118), Image.LANCZOS)], 4, pad=4,
                         labels=["PULSE LOW", "PULSE MID", "PULSE HIGH (BELOW A BULLET CORE)", "SPLIT, THE CORE"])
        frames = [heart_frame(v, i) for i in range(8)]
        story = panel_grid([field_cell(f, None, 0.5) for f in frames], 4, pad=6)
        what = {"a": "A 'THE HEART': A LOBED HEART-SHAPED MASS OF NEAR-BLACK VIOLET TISSUE, RIBBED TOP TO BOTTOM WITH "
                     "RED-VIOLET LIGHT, VESSELS OVER ITS SHOULDERS; THE SIX MEMBRANES AS PETALS ON ITS LOWER RIM; THE "
                     "FACE SPLITS DOWN ITS MIDDLE SEAM.",
                "b": "B 'THE CROWN': A VAST DOMED ORGAN BANDED WITH CONCENTRIC RINGS (THE HERALD'S RING MARKINGS) AND "
                     "SPOKES, A SEALED BOSS OVER THE CORE; THE SIX MEMBRANES AS MOUTHS ON ITS RIM; THE BOSS SPLITS."}[v]
        notes = [what,
                 "THE MEMBRANES' SINGERS GLOW IN THEIR SONG'S COLOUR WHEN OPEN (HERALD VIOLET, NEEDLER CRYSTAL, SPORE "
                 "LIME, SEED TEAL, SPIRAL VIOLET-WHITE, MOTES PINK); THE WALL NODES ARE LIME KNOTS OF NERVE.",
                 "THE PULSE IS A SMOOTH LIGHT LAYER (TWO RENDERS BLENDED, <= 1 HZ IN PLAY), ITS BRIGHTEST BELOW THE "
                 "BULLETS' WHITE CORES. PHASE 2'S DARK IS THE PLAY FIELD'S ONLY: AMBIENT 8 %, THE SHIP'S HEADLIGHT "
                 "CONE, ROOK'S LAMP, THE NODES' AND THE HEART'S POOLS; THE WALLS KEEP A LIT EDGE; BULLETS AT FULL.",
                 "EVERY GLOW, POOL AND EDGE IS A SMOOTH FLOAT GRADIENT FADING TO 0 INSIDE ITS CANVAS; NO ORDERED DITHER."]
        sheet = compose(f"CHOIR HEART {v.upper()} (R84)",
                        [("SOURCE RENDER AT 1X: THE HEART SEALED, ITS MEMBRANES CLOSED (THE FIELD'S TOP 236 PX)", src_cell),
                         ("THE PULSE AND THE SPLIT (0.5X)", pul),
                         ("MEMBRANES: CLOSED, OPENING, OPEN PER SONG; DEAD; THE WALL NODE (2X)", mem_img),
                         ("STORYBOARD (0.5X)", story)], notes)
        save_png(sheet, out / f"choir-heart-r84-{v}.png")
        save_gif(heart_gif(v), out / f"choir-heart-r84-{v}.gif", ms=100)


def heart_gif(v):
    """The pulse and the three phases at 1x, 10 fps: membranes singing in turn, the walls and the dark, the split
    and the storm."""
    frames = []
    for f in range(84):
        t = f / 10.0
        pulse = 0.5 + 0.5 * np.sin(TAU * 0.8 * t)                 # 0.8 Hz, smooth
        img = chamber_bg(pulse=pulse)
        if f < 30:
            m = ["closed"] * 6
            j = (f // 10) % 6
            m[[0, 4, 2][j % 3]] = "open" if f % 10 > 2 else "opening"
            img.alpha_composite(heart_layer(v, 1, pulse, membs=m))
            if f % 10 > 2:
                x, y = MEMB[[0, 4, 2][j % 3]]
                ring_shots(img, x, y, 12, 20 + 14 * (f % 10), col=SONG[[0, 4, 2][j % 3]][1])
        elif f < 58:
            img.alpha_composite(heart_layer(v, 2, pulse, membs=["dead"] * 6))
            wl, xl, xr = wall_layer(min(1.0, (f - 30) / 8), breathe=6 * np.sin(TAU * 0.5 * t))
            img.alpha_composite(wl)
            pools = [("pool", 240, 60, 220 + 20 * pulse), ("cone", 240, 470, 16, 300)]
            for side, y in NODES:
                x = xl - 6 if side == "l" else xr + 6
                put(img, HT[("node",)], x, y)
                pools.append(("pool", x, y, 46))
            img = darkness(img, pools, ambient=0.08 + 0.9 * max(0.0, 1 - (f - 30) / 6))
        else:
            sp = min(1.0, (f - 58) / 8)
            img.alpha_composite(heart_layer(v, 3, pulse, membs=["dead"] * 6, split=sp))
            if sp >= 1:
                ring_shots(img, *CORE, 16, 30 + 12 * ((f - 66) % 12), phase=0.2 * f)
        ship(img, 240 + 40 * np.sin(t * 0.7), 470)
        rook(img, 290 + 40 * np.sin(t * 0.7), 492)
        frames.append(img)
    return frames


# =========================================================================== 3 people: Vorne fused, the Choir's frame

PORT = ROOT / "assets" / "sprites" / "portraits"
CHARS = DESIGN / "story" / "characters" / "concept"


def tissue_overlay(w, h, seed, mask, vein_k=1.0):
    """Teal tissue over a portrait (float rgb, alpha): dark teal-black flesh with glowing teal veins, its edge
    reaching in as thin branching veins; mask 0..1 is where the flesh has grown."""
    n = _fbm(w, h, max(6, w // 6), seed, 4)
    flesh = _ramp(n, [(0, "021412"), (0.6, "0a3430"), (1, "1a5a52")])
    vv = _fbm(w, h, max(6, w // 5), seed + 1, 3)
    vein = np.exp(-(np.abs(np.sin(vv * 9)) / 0.14) ** 2)
    rgb = flesh * (0.75 + 0.5 * soft(n, 1.0))[..., None] + smooth_steps(vein * 0.8)[..., None] * np.array(TEAL) * 0.6 * vein_k
    reach = np.exp(-(np.abs(np.sin(vv * 9)) / 0.08) ** 2)
    return rgb, mask, reach


def fused(img, v, seed=8401, choir=None):
    """Vorne's portrait overgrown by the Choir's teal (a: from the left edge; b: the pattern ghosted through the
    screen and veins from his temple). img: RGBA portrait (72 or 144)."""
    a = arr(img)
    h, w = a.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    rgb = a[..., :3]
    if v == "a":
        n = _fbm(w, h, max(6, w // 8), seed + 5, 4)
        edge = 0.22 + 0.1 * np.sin(yy / h * 5.0) + (n - 0.5) * 0.28
        mask = smooth01((edge - xx / w) / 0.05)
        fl, m, reach = tissue_overlay(w, h, seed, mask)
        rgb = rgb * (1 - m[..., None]) + fl * m[..., None]
        ahead = smooth01(((edge - xx / w) + 0.16) / 0.16) * (1 - m)        # veins reaching ahead of the edge
        rgb += smooth_steps(np.clip(reach * ahead, 0, 1))[..., None] * np.array(TEAL) * 0.75
        glow = soft(m, max(1.0, w / 40)) * (1 - m)
        rgb += glow[..., None] * np.array((0, 120, 90)) * 0.5
    else:
        if choir is not None:
            c = arr(choir.resize((w, h), Image.LANCZOS))[..., :3]
            dark = np.clip(1 - (rgb @ np.array([0.3, 0.59, 0.11])) / 90.0, 0, 1)
            rgb = rgb + c * (0.25 + 0.5 * dark)[..., None]
        tx, ty = 0.665 * w, 0.33 * h
        d = np.hypot(xx - tx, (yy - ty) * 1.2) / w
        vv = _fbm(w, h, max(4, w // 16), seed + 7, 3)
        vein = np.exp(-(np.abs(np.sin(vv * 30 + np.arctan2(yy - ty, xx - tx) * 2)) / 0.09) ** 2)
        reach = vein * smooth01((0.2 - d) / 0.12)
        rgb += smooth_steps(np.clip(reach, 0, 1))[..., None] * np.array(TEAL) * 0.8
        glow_at(rgb, tx, ty, w * 0.06, TEAL, 0.5)
    a[..., :3] = np.clip(rgb, 0, 255)
    return Image.fromarray(a.astype(np.uint8), "RGBA")


def make_fused(variants):
    moods = ("neutral", "smug", "rage")
    choir = Image.open(PORT / "radio-the-choir-neutral_8.png").convert("RGBA")
    orig = [Image.open(PORT / f"briefing-vorne-{m}.png").convert("RGBA") for m in moods]
    rad = [Image.open(PORT / f"radio-vorne-{m}.png").convert("RGBA") for m in moods]
    for v in variants:
        big = [fused(o, v, 8401 + i, choir) for i, o in enumerate(orig)]
        small = [fused(r, v, 8411 + i, choir) for i, r in enumerate(rad)]
        row1 = panel_grid(big + [orig[0]], 4, pad=8, labels=[f"BRIEFING 144 {m.upper()}" for m in moods] +
                          ["ACT 5-6 PORTRAIT (UNCHANGED)"])
        row2 = panel_grid([s_.resize((216, 216), Image.NEAREST) for s_ in small] + [small[0], small[2]], 5, pad=8,
                          labels=[f"RADIO 72 AT 3X {m.upper()}" for m in moods] + ["RADIO 1X", "RAGE 1X"])
        what = {"a": "A 'THE EDGE': TEAL TISSUE GROWN IN FROM THE SCREEN'S LEFT EDGE OVER HIS CHEEK AND COLLAR, THIN "
                     "TEAL VEINS REACHING AHEAD OF IT; THE REST OF HIM AS HE WAS.",
                "b": "B 'THE CHORUS': THE CHOIR'S OWN PATTERN (ITS PRODUCTION PORTRAIT) GHOSTED THROUGH THE WHOLE SCREEN "
                     "BEHIND HIM, AND TEAL VEINS SPREADING UNDER THE SKIN FROM HIS TEMPLE (HIS ACT 5 SHIMMER GROWN)."}[v]
        notes = [what,
                 "VORNE'S FUSED PORTRAIT FOR LEVEL 48 (THE ENEMY CHANNEL'S AMBER SCREEN; HIS FUSED VOICE): THE THREE "
                 "PRODUCTION PORTRAITS (ROUND 60, M9 PART B) WITH THE OVERGROWTH LAID OVER THEM; THE CHOIR'S TEAL STAYS "
                 "TEAL ON THE AMBER SCREEN SO IT READS AT 72 PX. THE SHOUT (LINE 5) USES THE RAGE FRAME.",
                 "THE GROWTH'S GLOW IS SMOOTH AND FADES TO 0 INSIDE THE FRAME; NO ORDERED DITHER ADDED."]
        sheet = compose(f"VORNE FUSED {v.upper()} (R84)", [("BRIEFING SIZE 1X", row1), ("RADIO SIZE", row2)], notes)
        save_png(sheet, CHARS / f"vorne-fused-r84-{v}.png")


def choir_frames(n=48):
    return [Image.open(PORT / f"radio-the-choir-neutral_{k % 32}.png").convert("RGBA") for k in range(n)]


FACE_T = (0.0, 1.2, 2.8, 4.0)             # pattern -> face formed -> held -> scattered (s)


def face_amount(t):
    if t < FACE_T[1]:
        return smooth01(t / FACE_T[1])
    if t < FACE_T[2]:
        return 1.0
    return 1 - smooth01((t - FACE_T[2]) / (FACE_T[3] - FACE_T[2]))


def face_curves(w):
    """The face's curves in px of a w-wide frame: two eye rings, brows from the sigil's arms, a singing mouth."""
    c = []
    for sx_, tilt in ((0.35, 0.25), (0.65, -0.25)):         # hollow sockets, slanting down to the middle
        c.append([(w * (sx_ + 0.1 * np.cos(a)), w * (0.42 + 0.06 * np.sin(a) + tilt * 0.1 * np.cos(a)))
                  for a in np.linspace(0, TAU, 44)])
        c.append([(w * (sx_ + 0.05 * np.cos(a)), w * (0.43 + 0.035 * np.sin(a))) for a in np.linspace(0, TAU, 20)])
    c.append([(w * (0.5 + 0.07 * np.cos(a)), w * (0.74 + 0.11 * np.sin(a))) for a in np.linspace(0, TAU, 44)])
    c.append([(w * (0.5 + 0.3 * np.cos(a)), w * (0.52 + 0.4 * np.sin(a))) for a in np.linspace(0.15 * np.pi, 0.85 * np.pi, 40)])
    return c


def choir_face(base, v, t, seed=8421):
    """The Choir's frame at time t: the production pattern fading while the face forms, then scattering."""
    w = 144
    b = arr(base.resize((w, w), Image.LANCZOS))
    f = face_amount(t)
    scatter = smooth01((t - FACE_T[2]) / (FACE_T[3] - FACE_T[2])) if t > FACE_T[2] else 0.0
    rgb = b[..., :3].copy()
    bgcol = np.array((14, 6, 26))
    rgb = rgb * (1 - 0.5 * f) + bgcol * 0.5 * f
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64)
    if v == "a":
        lay = np.zeros((w, w, 3))
        rng = np.random.default_rng(seed)
        for curve in face_curves(w):
            for (x, y) in curve:
                dx, dy = x - w / 2, y - w / 2
                if scatter > 0:
                    k = rng.uniform(0.6, 1.6)
                    x, y = x + dx * scatter * k * 0.9, y + dy * scatter * k * 0.9 + rng.uniform(-6, 6) * scatter
                else:
                    sw = (1 - f)                                   # drawn in from the sigil's centre
                    x, y = w / 2 + dx * (1 - sw * 0.8), w / 2 + dy * (1 - sw * 0.8)
                x, y = x + rng.normal(0, 1.4), y + rng.normal(0, 1.4)
                col = np.array((170, 90, 255)) if rng.random() < 0.6 else np.array(TEAL)
                glow_at(lay, x, y, 2.6, col * 0.5, 1.0)
                glow_at(lay, x, y, 0.8, (230, 220, 255), 0.3)
        k = f if scatter == 0 else (1 - scatter) ** 1.2
        rgb += lay * min(1.0, k * 1.2)
    else:
        u, vv = (xx - w * 0.5) / (w * 0.3), (yy - w * 0.5) / (w * 0.38)
        head = np.clip(1 - u ** 2 - vv ** 2, 0, 1)
        H = np.sqrt(head)
        for ex in (-0.38, 0.38):
            H -= 0.45 * np.exp(-(((u - ex) / 0.2) ** 2 + ((vv + 0.12) / 0.13) ** 2))
        H += 0.25 * np.exp(-((u / 0.1) ** 2 + ((vv - 0.1) / 0.3) ** 2))
        H -= 0.35 * np.exp(-((u / 0.28) ** 2 + ((vv - 0.52) / 0.06) ** 2))
        H += 0.06 * _fbm(w, w, 10, seed, 3)
        gy, gx = np.gradient(soft(H, 1.2) * 18)
        shade = np.clip(0.55 + (-gx * 0.7 - gy * 0.9), 0.1, 1.4)
        mask = smooth01(head * 6) * f
        face = np.array((176, 150, 210)) * shade[..., None] * (0.55 + 0.45 * H)[..., None]
        vv2 = _fbm(w, w, 9, seed + 2, 3)
        vein = np.exp(-(np.abs(np.sin(vv2 * 24)) / 0.1) ** 2)
        face += smooth_steps(vein * 0.6)[..., None] * np.array(TEAL) * 0.4
        if scatter > 0:
            n = _fbm(w, w, 12, seed + 4, 3)
            mask *= smooth01((n - scatter) * 5 + 0.5)
        rgb = rgb * (1 - mask[..., None]) + face * mask[..., None]
        rgb = rgb + b[..., :3] * 0.3 * f
    out = b.copy()
    out[..., :3] = np.clip(rgb, 0, 255)
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def make_frame(variants):
    base = choir_frames(48)
    for v in variants:
        ts = [i * 0.1 for i in range(48)]
        frames = [choir_face(base[i], v, t) for i, t in enumerate(ts)]
        keys = [0, 6, 12, 20, 30, 34, 38, 44]
        row = panel_grid([frames[k] for k in keys], 6, pad=6, labels=[f"T {ts[k]:.1f} S" for k in keys])
        row72 = panel_grid([frames[k].resize((72, 72), Image.LANCZOS) for k in keys], 8, pad=6)
        what = {"a": "A 'THE SIGIL GATHERS': THE FIVE-ARMED SIGIL'S LIGHT DRAWS INTO TWO EYE RINGS, BROWS AND A SINGING "
                     "MOUTH OF VIOLET AND TEAL MOTES, HOLDS, THEN SCATTERS OUTWARD AS MOTES AND THE PATTERN RETURNS.",
                "b": "B 'PRESSED THROUGH': A PALE MASK PUSHES OUT OF THE TISSUE LIKE A FACE UNDER A MEMBRANE (HOLLOW "
                     "EYES, A RIDGE, AN OPEN MOUTH, TEAL VEINS), HOLDS, THEN CRUMBLES BACK INTO THE PATTERN."}[v]
        notes = [what,
                 "THE CHOIR'S LAST LINE (THE HEART'S LINE 5, K + 5 S): '...IT IS BEHIND US... IT HAS ALWAYS BEEN "
                 "BEHIND US...'. 0-1.2 S THE FACE FORMS, 1.2-2.8 S IT HOLDS, 2.8-4.0 S IT SCATTERS. OVER THE "
                 "PRODUCTION PATTERN (ROUND 08'S SIGIL, ASSETS/SPRITES/PORTRAITS/RADIO-THE-CHOIR-NEUTRAL_*), CLEAN "
                 "(NO STATIC: IN ACT 7 IT IS CLOSE). SMOOTH, NO FLASH."]
        sheet = compose(f"THE CHOIR'S FACE-LIKE FRAME {v.upper()} (R84)",
                        [("BRIEFING SIZE 144 PX", row), ("RADIO SIZE 72 PX AT 1X", row72)], notes)
        save_png(sheet, CHARS / f"choir-frame-r84-{v}.png")
        save_gif([f_.resize((288, 288), Image.LANCZOS) for f_ in frames], CHARS / f"choir-frame-r84-{v}.gif", ms=100)


# =========================================================================== 4 the echo, 5 the moods

VRELL = DESIGN / "world" / "vrell-space" / "concept"


def spectral_sprite(sp, v, k=0.4, edge=10.0):
    """An echo: the sprite's colours desaturated by k toward violet-white, its edges translucent (alpha fading
    smoothly to 0 inside its own canvas over ``edge`` px)."""
    a = arr(sp)
    rgb = spectral(a[..., :3] / 255.0, k) * 255 * 0.74 + np.array(VIOLET_W) * 0.26     # a step brighter
    al = a[..., 3] / 255.0
    inner = soft(al, edge / 2.5)
    fall = smooth01((inner - 0.5) / 0.5) if v == "a" else smooth01((inner - 0.35) / 0.65) * 0.8
    out = np.dstack([np.clip(rgb, 0, 255), al * fall * 255])
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def echo_frame(v, carrier, t=0.0, bg=None):
    img = (bg or memory_bg()).copy()
    sp = spectral_sprite(carrier, v)
    x, y = 240 + 30 * np.sin(t), -140
    if v == "b":
        for k, al in ((2, 0.18), (1, 0.32)):
            put(img, fade(sp, al), x - 14 * k * np.cos(t), y - 6 * k)
    put(img, sp, x, y)
    if v == "b":                                         # a slow violet scan band over the echo (smooth, not a flash)
        a = arr(img)
        band = np.exp(-((YY - (120 + 60 * np.sin(t * 0.7))) / 18.0) ** 2) * (XX > x - 150) * (XX < x + 150)
        a[..., :3] += band[..., None] * np.array((60, 40, 90)) * 0.5
        img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    for j in range(5):
        b6.orb(img, 200 + 20 * j, 240 + 18 * j)
    b6.orb(img, 300, 300, big=True)
    ship(img, 230, 470)
    rook(img, 280, 492)
    return img


def make_echo(variants):
    carrier = spr("brood-carrier-hull")
    luna = memory_bg(4901, world="luna")
    for v in variants:
        f = echo_frame(v, carrier, 0.0, luna)
        crop = f.crop((90, 0, 390, 180)).resize((600, 360), Image.LANCZOS)
        orig = luna.copy()
        put(orig, carrier, 240, -140)
        trans = [memory_bg(4901, world="luna", t=tt) for tt in (0.0, 0.5, 1.0)] + \
                [memory_bg(4901, world="mars", t=tt) for tt in (0.8, 0.0)]
        trans_img = panel_grid([field_cell(x_, None, 0.4) for x_ in trans], 5, pad=6,
                               labels=["LUNA", "DISSOLVING", "TISSUE", "MARS FORMING", "MARS"])
        top = panel_grid([field_cell(orig, "THE ORIGINAL SPRITE", 0.75), field_cell(f, "THE ECHO", 0.75)], 2, pad=8)
        what = {"a": "A: THE TINT AS WORDED (THE BOSSES README): ITS OWN COLOURS DESATURATED ABOUT 40 % TOWARD A COLD "
                     "VIOLET-WHITE, A STEP BRIGHTER THAN ITS TINTED WORLD, THE EDGES TRANSLUCENT, FADING TO 0 OVER 10 PX "
                     "INSIDE ITS CANVAS; ITS BULLETS AS BUILT (THE TINT NEVER REACHES THEM).",
                "b": "B 'AFTERIMAGE': THE SAME TINT, THE HULL 80 % OPAQUE WITH A WIDER SOFT EDGE, TWO FAINT AFTERIMAGES "
                     "TRAILING ITS MOTION AND A SLOW VIOLET SCAN BAND (A MEMORY REPLAYING; SMOOTH, NEVER A FLICKER)."}[v]
        notes = [what,
                 "THE BROOD CARRIER (LEVEL 49'S FIRST ECHO) FROM ITS PRODUCTION HULL OVER LUNA AS THE CHOIR REMEMBERS "
                 "IT: GREY REGOLITH IN THE SAME TINT, ITS EDGES DISSOLVING INTO DARK VIOLET TISSUE WITH A PALE RIM; "
                 "BETWEEN THE ECHOES THE MEMORY MELTS INTO TISSUE AND THE NEXT ONE FORMS (BOTTOM ROW).",
                 "EVERY EDGE, RIM AND BAND IS A SMOOTH FLOAT GRADIENT; NO ORDERED DITHER."]
        sheet = compose(f"ECHO IN THE SPECTRAL TINT {v.upper()} (R84)",
                        [("THE ORIGINAL AND THE ECHO (0.75X)", top), ("THE ECHO'S EDGE (2X)", crop),
                         ("LEVEL 49'S MEMORY BETWEEN ECHOES (0.4X)", trans_img)], notes)
        save_png(sheet, VRELL / f"vrell-echo-r84-{v}.png")


def mood_list():
    ev = u84.EventPlane(4401, 1.0).frame(0.0)
    return [("L43", "THE TETHER GATE", u84.gate_bg(4301)), ("L44", "THE EVENT PLANE", ev),
            ("L45", "HIVE REEF", u84.reef_bg(4501)), ("L46", "GRAVEYARD", u84.graveyard_bg(4601)),
            ("L47", "THE INNER SPHERE", u84.sphere_bg(4701)), ("L48", "THE CITADEL", citadel_bg()),
            ("L49", "THE MEMORY (LUNA)", memory_bg()), ("L50", "THE HEART'S CHAMBER", chamber_bg(pulse=1.0))]


R06 = [(0.0, (4, 2, 10)), (0.25, (30, 12, 44)), (0.5, (78, 34, 104)), (0.72, (40, 120, 124)), (0.9, (150, 230, 210)),
       (1.0, (230, 250, 245))]


def one_palette(img):
    """Q10 b: one Vrell palette (round 06's violet and teal) for every level: each level keeps its light (its
    luminance), only the palette is shared."""
    a = arr(img)
    l = np.clip((a[..., :3] @ np.array([0.3, 0.59, 0.11])) / 255.0, 0, 1)
    rgb = _ramp(np.clip(l * 1.15, 0, 1), R06)
    a[..., :3] = rgb
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def mood_overlay(crop):
    c = crop.copy()
    put(c, spr("choir-herald"), 150, 44)
    put(c, spr("needler"), 104, 30)
    vorb(c, 14, 14, 4, VIOLET)
    vorb(c, 40, 22, 4, TEAL)
    spore(c, 64, 12, 5)
    vneedle(c, 196 - 12, 80, np.pi / 2 + 0.3)
    vorb(c, 184, 16, 4, (236, 220, 255))
    ship(c, 26, 66)
    rook(c, 64, 74)
    return c


def make_moods(variants):
    base = mood_list()
    for v in variants:
        cells, labs, pairs, plabs = [], [], [], []
        for key, title, img in base:
            img = img.convert("RGBA") if v == "a" else one_palette(img)
            L = lum(img)
            bb, bl = window(img, darkest=False)
            db, dl = window(img, darkest=True)
            cell = img.copy()
            d = ImageDraw.Draw(cell)
            d.rectangle(bb, outline=(255, 255, 255, 255), width=2)
            d.rectangle(db, outline=(80, 200, 255, 255), width=2)
            cells.append(cell.resize((240, 270), Image.LANCZOS))
            labs.append(f"{key} {title[:20]}  MEAN {L.mean():.0f}")
            pairs.append(panel_grid([mood_overlay(img.crop(bb)), mood_overlay(img.crop(db))], 2, pad=4))
            plabs.append(f"{key}: BRIGHTEST {bl:.0f} (WHITE) | DARKEST {dl:.0f} (BLUE)")
        ladder = panel_grid(cells, 4, labels=labs)
        strip = panel_grid(pairs, 2, pad=16, labels=plabs)
        name = {"a": "A - A GRADE PER LEVEL (Q10 A)", "b": "B - ONE VRELL PALETTE (Q10 B)"}[v]
        notes = [f"VRELL SPACE'S MOODS L43-L50 {name}. MEAN = THE BACKDROP'S MEAN LUMINANCE (0-255); THE WINDOWS ARE "
                 "ITS BRIGHTEST AND DARKEST 200X96 AT 1X, WITH THE READABILITY SET OVER THEM: A NEEDLER AND A CHOIR "
                 "HERALD (PRODUCTION), THE SHIP, ROOK, AND THE LARGER, BRIGHTER, OUTLINED VRELL BULLETS (VIOLET, TEAL, "
                 "LIME SPORE, VIOLET-WHITE, A CRYSTAL NEEDLE).",
                 "L43 THE GATE'S DARK STONE AND COLD GLYPHS UNDER THE NEBULA; L44 THE EVENT PLANE, THE ACT'S ONE LIGHT "
                 "FIELD (PALE VIOLET-WHITE, DARK STREAKS); L45 PINK VEINS ON CHITIN, THE PITS' WARM GLOW; L46 GREY-BROWN "
                 "HULKS, ONE COLD LIGHT, THE STARLESS PATCH; L47 PALE BONE RIBS LIT GOLD-WHITE; L48 TEAL-BLACK GROWTH "
                 "OVER DULL GOLD WRECKAGE; L49 LUNA REMEMBERED IN THE SPECTRAL TINT, DISSOLVING INTO TISSUE; L50 "
                 "NEAR-BLACK VIOLET TISSUE AT THE PULSE'S PEAK.",
                 ("A: EACH LEVEL ITS OWN GRADE (VRELL SPACE'S TABLE). L43-L47 ARE ART-R-A'S LEVEL STAND-INS "
                  "(ENEMIES_R84.PY) SO THE UNITS' SHEETS AND THESE AGREE. THE RISK IS L44'S PALE FIELD: THE BULLETS' DARK "
                  "OUTLINE AND THE SHIP'S DARK HULL CARRY THE READ THERE." if v == "a" else
                  "B: THE SAME SCENES THROUGH ONE VRELL PALETTE (ROUND 06'S VIOLET AND TEAL), EACH KEEPING ITS LIGHT: "
                  "CHEAPER AND COHERENT, BUT THE EIGHT LEVELS LOOK ALIKE, THE TEAL BULLETS SINK INTO THE TEAL HIGHS AND "
                  "THE ACT LOSES ITS JOURNEY (GATE STONE, FLESH, ASH, BONE, GOLD)."),
                 "EVERY HAZE AND GLOW IS A SMOOTH FLOAT GRADIENT (NO ORDERED DITHER)."]
        img = compose(f"VRELL SPACE MOODS {name}", [("THE LADDER (0.5X)", ladder), ("READABILITY (1X)", strip)], notes)
        save_png(img, VRELL / f"vrell-moods-r84-{v}.png")


# =========================================================================== 6 the gate: transit, title still, closing

EV_VIOLET = np.array((150, 128, 192))


def space_rgb(w, h, seed, k=1.0):
    """Deep space under the violet and teal nebula (round 06's / L43's), smooth."""
    rgb = np.zeros((h, w, 3)) + (6, 5, 12)
    rgb += np.clip((_fbm(w, h, 160, seed + 1) - 0.42) * 1.9, 0, 1)[..., None] * np.array((74, 28, 100)) * k
    rgb += np.clip((_fbm(w, h, 120, seed + 2) - 0.55) * 2.2, 0, 1)[..., None] * np.array((8, 70, 78)) * k
    rgb += u84._stars(w, h, seed + 3, 0.0016)
    return rgb


def plane_rgb(w, h, seed):
    """The event plane (the nebula inverted): pale violet-white with dark violet streaks."""
    n = _fbm(w, h, 150, seed + 1)
    st = _fbm(w, h // 4 + 1, 30, seed + 2, 4)
    st = np.array(Image.fromarray((st * 255).astype(np.uint8)).resize((w, h), Image.BICUBIC)) / 255.0
    rgb = _ramp(np.clip(0.25 + 0.75 * n, 0, 1), [(0, (88, 60, 128)), (0.45, (150, 128, 192)), (0.8, (196, 184, 224)),
                                                (1, (214, 204, 236))])
    dark = np.clip((st - 0.52) * 2.4, 0, 1)
    return rgb * (1 - 0.5 * dark[..., None]) + np.array((44, 24, 74)) * 0.5 * dark[..., None]


def ring_paint(rgb, cx, cy, R, half, seed, glyphs=1.0, glyph_dark=None, plane=None, plane_r=None, iris=0.0):
    """The Tether Gate's ring (dark stone, bevelled, seams, two glyph rows of cold teal-white) painted over rgb
    (float, in place). plane: an rgb image shown inside the ring's mouth out to plane_r (the event plane);
    glyph_dark(angle) -> 0..1 darkens glyphs one by one; iris 0..1 closes six stone leaves over the mouth."""
    h, w = rgb.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    r = np.hypot(xx - cx, yy - cy)
    ang = np.arctan2(yy - cy, xx - cx)
    if plane is not None:
        pr = R - half if plane_r is None else plane_r
        inside = smooth01((pr - r) / 3.0)
        if iris > 0:                                         # six leaves sliding shut toward the centre
            a6 = (ang + np.pi) % (TAU / 6)
            leaf_edge = (R - half) * (1 - iris) + (a6 / (TAU / 6)) * 18 * iris
            leaf = smooth01((r - leaf_edge) / 2.0) * (r < R - half + 2)
            inside = inside * (1 - leaf)
            tex = _fbm(w, h, 20, seed + 9, 3)
            stone_l = _ramp(0.3 + 0.3 * tex, [(0, "0c0b0f"), (0.5, "24222a"), (1, "3a3640")])
            seam = np.exp(-((a6 - 0.02) / 0.02) ** 2)
            stone_l *= (1 - 0.5 * seam)[..., None]
            m = leaf * (r < R - half + 2)
            rgb[:] = rgb * (1 - m[..., None]) + stone_l * m[..., None]
        glowk = np.exp(-((r - pr) / 26.0) ** 2) * (r > pr) * 0.45
        rgb[:] = rgb * (1 - inside[..., None]) + plane * inside[..., None]
        rgb += (glowk * (pr > 4))[..., None] * EV_VIOLET * 0.6
    dr = r - R
    band = np.clip(half - np.abs(dr) + 0.5, 0, 1)
    tex = _fbm(w, h, 18, seed + 4, 4)
    u = dr / half
    bevel = np.clip(1 - np.abs(u) ** 6, 0, 1)
    lit = np.cos(ang - np.radians(-135))                 # the top-left face catches the soft violet light
    shade = 0.55 + 0.25 * (-u) * np.sign(dr + 1e-6) * 0 + 0.2 * lit * (-u) + 0.25 * (tex - 0.5)
    stone = _ramp(np.clip(0.22 + 0.4 * shade * bevel, 0, 1), [(0, "0c0b0f"), (0.4, "1d1b22"), (0.75, "2c2932"),
                                                             (1, "48444e")])
    cells = ang * R / 13.0
    ci = np.floor(cells)
    hsh = (np.sin(ci * 12.9898) * 43758.5453) % 1.0
    fu = cells - ci
    gk = np.ones_like(r) * glyphs
    if glyph_dark is not None:
        gk = gk * (1 - glyph_dark(ang))
    for rr in (-half * 0.55, half * 0.52):
        mark = ((np.abs(dr - rr) < 2.6) & (np.abs(fu - 0.5) < 0.2) & (hsh > 0.3)).astype(float)
        mark = np.maximum(mark, ((np.abs(dr - rr) < 0.9) & (hsh > 0.75)).astype(float))
        stone += (mark * gk)[..., None] * np.array((120, 196, 190)) * 0.5
        halo = soft(mark * gk, 2.0) * 0.6
        stone += halo[..., None] * np.array((60, 140, 130))
    seams = (np.abs(((ang * R) % 90.0) - 45) > 43.8) & (band > 0)
    stone = np.where(seams[..., None], stone * 0.55, stone)
    rgb[:] = rgb * (1 - band[..., None]) + stone * band[..., None]
    return rgb


def warp_img(rgb, s, cx=240.0, cy=200.0, R=260.0):
    """Q11 a's warp: the image pulled toward the ring's centre (strongest at the centre), s 0..1."""
    h, w = rgb.shape[:2]
    yy, xx = np.mgrid[0:h, 0:w].astype(np.float64)
    dx, dy = xx - cx, yy - cy
    r = np.hypot(dx, dy)
    a = 1 + 1.6 * s * np.exp(-(r / R) ** 2) + 0.25 * s
    th = 0.35 * s * np.exp(-(r / R) ** 2)
    c, sn = np.cos(th), np.sin(th)
    sx_ = cx + (dx * c - dy * sn) * a
    sy_ = cy + (dx * sn + dy * c) * a
    def mirror(v, n):                                    # mirrored sampling past the edges (no clamp streaks)
        m = 2 * (n - 1.001)
        v = np.mod(v, m)
        return np.where(v > n - 1.001, m - v, v)
    sx_, sy_ = mirror(sx_, w), mirror(sy_, h)
    x0, y0 = np.floor(sx_).astype(int), np.floor(sy_).astype(int)
    fx, fy = (sx_ - x0)[..., None], (sy_ - y0)[..., None]
    out = (rgb[y0, x0] * (1 - fx) * (1 - fy) + rgb[y0, x0 + 1] * fx * (1 - fy) + rgb[y0 + 1, x0] * (1 - fx) * fy
           + rgb[y0 + 1, x0 + 1] * fx * fy)
    return out


class Transit:
    """Level 43's last seconds: the ring's mouth ahead, the formation, the warp; then L44's opening."""

    def __init__(self):
        self.space = space_rgb(FW, FH, 4301)
        self.plane = plane_rgb(FW, FH, 4401)

    def scene(self, t):
        """t in level seconds 256..268 (the warp 266.5-268)."""
        k = smooth01((t - 254) / 13.0)
        R = 150 + 110 * k
        cy = 120 + 50 * k
        rgb = self.space.copy()
        ring_paint(rgb, 240, cy, R, 44 + 26 * k, 4302, plane=self.plane)
        return rgb

    def frame(self, t, v):
        if t <= 268:
            rgb = self.scene(min(t, 266.5))
            if v == "a" and t > 266.5:                          # the ship and Rook bend with the image
                im0 = _arr(rgb)
                ship(im0, 240, 420)
                rook(im0, 286, 438)
                rgb = arr(im0)[..., :3]
            if t > 266.5:
                u = (t - 266.5) / 1.5
                if v == "a":
                    s = smooth01(u)
                    rgb = warp_img(rgb, s, cy=170.0)
                    g = smooth01(u)
                    rgb = rgb * (1 - g) + self.plane * g
                else:
                    if u < 1 / 3:
                        rgb = 255 - rgb                         # Q11 b: 0.5 s full inversion
                    else:
                        rgb = self.plane.copy()
            img = _arr(rgb)
            if t < 266.5 or v == "b":
                sx_, sy_ = 240, 420 if t >= 262 else 470
                ship(img, sx_, sy_)
                rook(img, sx_ + 46, sy_ + 18)
            return img, "L43"
        u = (t - 268) / 1.0                                   # L44 opens: the warp unbending over 1 s
        rgb = warp_img(self.plane, 1 - smooth01(u)) if v == "a" else self.plane.copy()
        img = _arr(rgb)
        ship(img, 240, 470)
        rook(img, 286, 488)
        return img, "L44"


def debrief_box(img):
    d = ImageDraw.Draw(img)
    d.rectangle([60, 150, 420, 330], fill=(10, 8, 18, 200), outline=(150, 128, 192, 255))
    text(img, 80, 168, "LEVEL 43 COMPLETE", (255, 214, 120), 2)
    for i, s_ in enumerate(("THE TETHER GATE", "KILLS / SECRETS / CREDITS ...", "THE DEBRIEF OPENS OVER THE VIOLET")):
        text(img, 80, 204 + 18 * i, s_, raster.LABEL)
    return img


def make_transit(variants):
    tr = Transit()
    for v in variants:
        ts = list(np.arange(258.0, 266.5, 0.5)) + list(np.arange(266.5, 268.0, 0.1)) + [268.0] + \
             list(np.arange(268.1, 269.05, 0.1))
        frames = [tr.frame(t, v)[0] for t in ts]
        lums = [float(lum(f).mean()) for f in frames]
        keys = [(256.0, "T 256 THE CALM: THE PLANE FILLS THE RING'S MOUTH AHEAD"),
                (262.0, "T 262 THE FORMATION: SCRIPTED, NO INPUT, ROOK BESIDE"),
                (266.5, "T 266.5 THE WARP STARTS"), (267.0, "T 267.0"), (267.5, "T 267.5"),
                (268.0, "T 268 LEVEL END INSIDE IT"), (268.0, "THE DEBRIEF OVER THE VIOLET"),
                (268.5, "LEVEL 44 OPENS IN THE SAME VIOLET, THE WARP UNBENDING (1 S)")]
        cells = []
        for t, lab in keys:
            im = tr.frame(t, v)[0]
            if lab.startswith("THE DEBRIEF"):
                im = debrief_box(tr.frame(268.0, v)[0])
            cells.append(field_cell(cap(im, lab), None, 0.5))
        story = panel_grid(cells, 4, pad=6)
        warp_at = ts.index(266.5)
        plot = lum_plot(lums, width=968, marks=[(warp_at, "WARP"), (ts.index(268.0), "L44")])
        what = {"a": "A (Q11 A, RECOMMENDED): A 1.5 S WARP. THE WHOLE IMAGE BENDS SMOOTHLY TOWARD THE RING'S CENTRE (A "
                     "RADIAL PULL WITH A SLIGHT TWIST, STRONGEST AT THE CENTRE) WHILE ITS COLOURS GRADE IN ONE STEP TO "
                     "THE EVENT PLANE'S VIOLET. THE BRIGHTNESS RISES ONE WAY ONLY (THE PLOT): NO FLASH, NO INVERSION.",
                "b": "B (Q11 B, THE SKETCH'S): A 0.5 S FULL COLOUR INVERSION, THEN THE VIOLET. THE PLOT SHOWS THE FLASH: "
                     "THE MEAN JUMPS FROM THE DARK APPROACH TO NEAR WHITE AND BACK IN TWO FRAMES (A PHOTOSENSITIVITY "
                     "RISK; SHOWN FOR THE COMPARISON)."}[v]
        notes = [what,
                 "LEVEL 43'S END: >= 4 S OF CALM AFTER THE LAST WAVE, THE EVENT PLANE FILLING THE RING'S MOUTH AHEAD, THE "
                 "FORMATION FROM T=262, THE WARP T=266.5-268, THE LEVEL ENDING INSIDE IT; LEVEL 44 OPENS IN THE SAME "
                 "VIOLET. THE HUD STAYS DRAWN OVER THE EFFECT IN THE GAME (LEFT OUT HERE).",
                 "THE RING, THE NEBULA AND THE PLANE ARE SMOOTH FLOAT GRADIENTS (NO ORDERED DITHER)."]
        sheet = compose(f"GATE TRANSIT {v.upper()} (R84)", [("STORYBOARD (0.5X)", story),
                                                          ("MEAN LUMINANCE PER FRAME, T 258-269 (10 FPS IN THE WARP)", plot)],
                        notes)
        save_png(sheet, VRELL / f"gate-transit-r84-{v}.png")
        gif_ts = list(np.arange(262.0, 266.5, 0.25)) + list(np.arange(266.5, 268.0, 0.1)) + \
            list(np.arange(268.0, 269.5, 0.1))
        save_gif([tr.frame(t, v)[0] for t in gif_ts], VRELL / f"gate-transit-r84-{v}.gif", ms=100)


def still_a():
    """'The ring': the gate filling the frame, its glyphs lit, the fleet's lights small at its rim."""
    rgb = space_rgb(SW, SH, 8401)
    plane = space_rgb(SW, SH, 8402, k=1.4) * 0.8 + np.array((20, 10, 34))
    ring_paint(rgb, 640, 330, 520, 120, 8403, plane=plane)
    img = _arr(rgb)
    rng = np.random.default_rng(8404)
    for k in range(16):                                     # the fleet's running lights, small at the ring's rim
        a = np.radians(rng.uniform(196, 226))
        rr = 520 + 120 + rng.uniform(14, 90)
        x, y = 640 + np.cos(a) * rr, 330 + np.sin(a) * rr
        if x < 4 or y < 4:
            continue
        big = 1.0 + (1.2 if k % 5 == 0 else 0.0)
        light(img, x, y, 1.4 * big, (235, 240, 255), 0.95)
        light(img, x, y, 5.0 * big, (150, 170, 255), 0.18)
        light(img, x + 3 * big, y + 1, 0.9, (255, 80, 60), 0.6)
    return img


def still_b():
    """'The mouth': through the ring at the event plane's violet; the Steadfast's group's lights strung toward it."""
    rgb = space_rgb(SW, SH, 8411)
    plane = plane_rgb(SW, SH, 8412)
    ring_paint(rgb, 480, 270, 300, 70, 8413, plane=plane)
    img = _arr(rgb)
    for k in range(7):
        u = k / 6
        x, y = 160 + 300 * u, 470 - 190 * u
        light(img, x, y, 2.2 - 1.4 * u, (235, 240, 255), 0.9)
        line_glow(img, [x - 18 * (1 - u) - 4, y + 12 * (1 - u) + 3, x, y], (120, 150, 255), 1, 0.35)
    return img


def title_card(img):
    import act_stills as acts  # noqa: E402
    import act_titles  # noqa: E402
    card = Image.new("RGBA", (SW, SH), (0, 0, 0, 255))
    card.alpha_composite(Image.blend(Image.new("RGBA", (SW, SH), (0, 0, 0, 255)), img.convert("RGBA"), acts.BRIGHTNESS))
    card.paste((0, 0, 0, 255), (0, 0, SW, acts.BAR))
    card.paste((0, 0, 0, 255), (0, SH - acts.BAR, SW, SH))
    letters = act_titles.title({"act": "ACT VII", "name": "BEYOND THE GATE"})
    card.alpha_composite(letters, ((SW - letters.width) // 2, 138))
    s_ = "THE TETHER GATE - NOVEMBER 2185"
    text(card, SW / 2 - raster.text_width(s_) / 2, 392, s_, (210, 210, 220))
    return card


ACT7 = DESIGN / "campaign" / "act-7-beyond-the-gate" / "concept"
STILL_WHAT = {"a": "A 'THE RING': THE GATE FILLING THE FRAME, ITS GLYPHS LIT COLD TEAL-WHITE, THE FLEET'S RUNNING LIGHTS SMALL "
                   "AT ITS RIM (J1: FROM LEVEL 43'S BACKDROP)",
              "b": "B 'THE MOUTH': THROUGH THE RING AT THE EVENT PLANE'S VIOLET, THE STEADFAST'S GROUP'S LIGHTS STRUNG TOWARD IT"}


def make_still(variants):
    stills = {}
    for v in variants:
        img = still_a() if v == "a" else still_b()
        stills[v] = img
        save_png(img, ACT7 / f"act-7-still-r84-{v}.png")
    if all((ACT7 / f"act-7-still-r84-{v}.png").exists() for v in "ab"):
        hw, hh = SW // 2, SH // 2
        sheet = raster.sheet(1000, 40 + 2 * (hh + 30) + 60, "ACT 7 TITLE-CARD STILL (R84)",
                             "LEFT: THE STILL, RIGHT: THE TITLE CARD OVER IT")
        y = 38
        for v in "ab":
            im = stills.get(v) or Image.open(ACT7 / f"act-7-still-r84-{v}.png").convert("RGBA")
            text(sheet, 16, y, STILL_WHAT[v][:150], raster.LABEL)
            sheet.alpha_composite(im.resize((hw, hh), Image.LANCZOS), (16, y + 14))
            sheet.alpha_composite(title_card(im).resize((hw, hh), Image.LANCZOS), (16 + hw + 8, y + 14))
            y += hh + 30
        lines = wrap("THE STILLS ARE SMOOTH 24-BIT RENDERS (THE 2026-10-30 RULE FOR HAZE AND GLOW); THE PRODUCTION STILL IS "
                     "RENDERED BY TOOLS/ART/ACT_STILLS.PY FROM LEVEL 43'S PRODUCTION BACKDROP (M11 PART B). THE CARD'S "
                     "DATE LINE IN THE GAME'S FONT; THE CHROME LETTERING FROM TOOLS/ART/ACT_TITLES.PY.", 158)
        for i, n in enumerate(lines):
            text(sheet, 16, y + i * 14, n, raster.ACCENT if i == 0 else raster.LABEL)
        save_png(sheet, ACT7 / "act-7-still-r84-sheet.png")


def grey_chamber(seed, h=FH, k=1.0):
    """The hive gone still: the chamber's tissue greying (k 0..1)."""
    rgb = chamber_rgb(seed, h, pulse=0.0)
    l = rgb @ np.array([0.3, 0.59, 0.11])
    return rgb * (1 - k) + (l[..., None] * np.array((2.3, 2.2, 2.4)) + 14) * k


def neptune(rgb, cx, cy, r):
    yy, xx = np.mgrid[0:rgb.shape[0], 0:rgb.shape[1]].astype(np.float64)
    d = np.hypot(xx - cx, yy - cy) / r
    on = smooth01((1 - d) * r / 1.5)
    nx, ny = (xx - cx) / r, (yy - cy) / r
    lit = np.clip(0.4 - 0.7 * nx - 0.5 * ny + np.sqrt(np.clip(1 - d ** 2, 0, 1)) * 0.5, 0.05, 1)
    col = np.array((70, 120, 220)) * lit[..., None] + np.array((20, 40, 90))
    rgb[:] = rgb * (1 - on[..., None]) + col * on[..., None]
    rgb += (np.exp(-((d - 1) / 0.15) ** 2) * (d > 1) * 0.25)[..., None] * np.array((90, 140, 255))


class Closing:
    """Level 50's closing gate after the Heart (times from K)."""

    def __init__(self):
        self.plane = plane_rgb(FW, FH, 5011)
        self.space = space_rgb(FW, FH, 5012, k=0.35) * 0.9
        self.cham = [grey_chamber(5020 + i) for i in range(3)]

    def far(self, t):
        """Far side: K+10.5 - K+29: the grey chambers streaming past (reverse), the plane ahead, its rim contracting."""
        g = smooth01((t - 10.5) / 3.0)
        base = self.cham[int((t - 10) / 6) % 3]
        speed = smooth01((t - 12) / 3.0)
        off = int(((t - 12) * 300 * speed) % (2 * FH)) if t > 12 else 0
        tex = chamber_rgb(5020, FH, 0.0) * (1 - g) + base * g
        tex = np.concatenate([tex, tex[::-1]], axis=0)        # mirrored so the scroll has no seam
        rgb = np.roll(tex, off, axis=0)[:FH]
        if speed > 0:                                        # motion: a soft vertical smear
            rgb = rgb * 0.5 + np.roll(rgb, 6, 0) * 0.3 + np.roll(rgb, 12, 0) * 0.2
        k = smooth01((t - 10.5) / 18.5)
        R = 40 + 200 * k                                     # the plane ahead grows as the wing nears it
        rim = R * (1 - 0.35 * smooth01((t - 16) / 13.0))     # and contracts inside it (the gate closing)
        cy = -20 + 150 * k
        yy, xx = YY, XX
        d = np.hypot(xx - 240, yy - cy)
        inside = smooth01((rim - d) / 4.0)
        rgb = rgb * (1 - inside[..., None]) + self.plane * inside[..., None]
        rgb += (np.exp(-((d - rim) / 18.0) ** 2) * 0.4)[..., None] * EV_VIOLET
        fold = smooth01((t - 13) / 2.0)                      # the tissue folding closed behind, from the bottom
        edge = FH - 40 * fold + 10 * np.sin(XX / 30.0)
        f = smooth01((YY - edge) / 10.0) * fold
        rgb = rgb * (1 - f[..., None]) + np.array((40, 36, 44)) * f[..., None]
        return rgb

    def near(self, t, v):
        """Near side, K+30.5 on: the starfield, Neptune, the group ahead; the ring's upper arc in the lower field,
        the plane in it closing (a: shrinking to a point; b: six stone leaves sliding shut), the glyphs going dark."""
        rgb = self.space.copy()
        neptune(rgb, 380, 90, 16)
        shut = smooth01((t - 31.0) / 4.0)
        pr = 244 * (1 - shut) if v == "a" else 244
        iris = shut if v == "b" else 0.0
        dark_t = smooth01((t - 35.0) / 4.0)

        def gd(ang):                                         # the glyphs go dark one by one round the arc
            order = (np.cos(ang - np.radians(-90)) + 1) / 2 * 0.8 + ((np.sin(ang * 7.3) + 1) / 2) * 0.2
            return smooth01((dark_t * 1.2 - (1 - order)) * 6)
        plane = self.plane if (v == "a" and pr > 1) or v == "b" else None
        ring_paint(rgb, 240, 540, 300, 56, 5013, plane=plane, plane_r=pr if v == "a" else None, iris=iris,
                   glyph_dark=gd)
        return rgb

    def frame(self, t, v):
        if t < 29.0:
            rgb = self.far(t)
        elif t < 30.5:                                       # the warp reversed: violet back to the stars
            u = smooth01((t - 29.0) / 1.5)
            far = warp_img(self.far(29.0), 0.6 * u, cy=130.0)
            rgb = far * (1 - u) + self.near(30.5, v) * u
        else:
            rgb = self.near(t, v)
        img = _arr(rgb)
        if 30.5 <= t:
            for k in range(5):                               # the Steadfast's group ahead
                light(img, 180 + 30 * k, 60 + 8 * (k % 2), 1.8, (235, 240, 255), 0.9)
        if t >= 12:
            ship(img, 240, 420)
            rook(img, 286, 438)
        elif t >= 10.5:
            ship(img, 220, 460)
            rook(img, 270, 480)
        if 10.5 <= t < 14:
            text(img, 240 - raster.text_width("THE GATE IS CLOSING", 2) / 2, 220, "THE GATE IS CLOSING", (226, 206, 255), 2)
        return img


def make_closing(variants):
    cl = Closing()
    for v in variants:
        keys = [(10.6, "K+10.5 THE HIVE GOES STILL, GREYING; THE PLANE'S LIGHT AHEAD"),
                (18.0, "K+18 THE RACE: THE DEAD CHAMBERS IN REVERSE, THE TISSUE FOLDING SHUT BEHIND"),
                (27.0, "K+27 THE PLANE NEAR, ITS RIM CONTRACTING INSIDE THE RING"),
                (29.8, "K+29.8 THE WARP REVERSED: VIOLET BACK TO THE STARS"),
                (30.6, "K+30.5 THE NEAR SIDE: NEPTUNE, THE GROUP AHEAD, THE RING BEHIND"),
                (33.0, "K+33 " + ("THE PLANE SHRINKING TO A POINT" if v == "a" else "THE STONE LEAVES SLIDING SHUT")),
                (36.5, "K+36.5 THE GLYPHS GOING DARK ONE BY ONE"), (39.0, "K+38 THE GATE IS DARK; THE CLOSING LINES")]
        cells = [field_cell(cap(cl.frame(t, v), lab), None, 0.5) for t, lab in keys]
        story = panel_grid(cells, 4, pad=6)
        ts = list(np.arange(10.0, 29.0, 0.5)) + list(np.arange(29.0, 30.6, 0.1)) + list(np.arange(30.6, 40.0, 0.5))
        frames = [cl.frame(t, v) for t in ts]
        lums = [float(lum(f).mean()) for f in frames]
        plot = lum_plot(lums, width=968, marks=[(0, "K+10"), (ts.index(29.0), "WARP"), (len(ts) - 1, "K+40")])
        what = {"a": "A (THE DOCUMENT'S): THE EVENT PLANE SHRINKS TO A POINT INSIDE THE RING AND GOES OUT (A FADE), THEN "
                     "THE GLYPHS GO DARK ONE BY ONE ROUND THE ARC.",
                "b": "B 'THE IRIS': SIX STONE LEAVES OF THE RING SLIDE SHUT OVER THE PLANE LIKE A CAMERA IRIS (THE LOCK "
                     "CLOSING, VARGA'S PAGE 2), THEN THE GLYPHS GO DARK ONE BY ONE."}[v]
        notes = [what,
                 "LEVEL 50'S SCENE AFTER THE KILL (NO INPUT, NO HITS, NO BAR): THE HIVE GREYING, THE RACE AT 300 PX/S, "
                 "THE PLANE AHEAD GROWING WHILE ITS RIM CONTRACTS, THE WARP REVERSED (LEVEL 43'S IN REVERSE), THE NEAR "
                 "SIDE WITH NEPTUNE SMALL AND BLUE AND THE STEADFAST'S GROUP AHEAD, THE RING'S UPPER ARC BEHIND.",
                 "PHOTOSENSITIVITY: THE PLOT IS EACH FRAME'S MEAN LUMINANCE; NO FLASH AND NO INVERSION (THE WARP'S "
                 "GRADE MOVES ONE WAY); THE ACT BOSS'S FLASH AT K+3.5 IS THE FIGHT'S, BEFORE THIS SCENE."]
        sheet = compose(f"THE CLOSING GATE {v.upper()} (R84)",
                        [("STORYBOARD (0.5X)", story), ("MEAN LUMINANCE PER FRAME, K+10 TO K+40", plot)], notes)
        save_png(sheet, ACT7 / f"closing-gate-r84-{v}.png")


# =========================================================================== 7 the ending pages

PW, PH = 672, 240                                          # the briefing image size


def page_bg(seed, k=1.0):
    return space_rgb(PW, PH, seed, k)


def disp(img, box, col=(60, 90, 110)):
    d = ImageDraw.Draw(img)
    d.rectangle(box, fill=(6, 12, 16, 255), outline=col + (255,), width=2)


def end_plot(v):
    """a: the Steadfast's plot of the solar system, every Vrell contact greyed at once; b: a Vrell swarm hanging
    still before Earth."""
    if v == "a":
        img = Image.new("RGBA", (PW, PH), (4, 10, 14, 255))
        d = ImageDraw.Draw(img)
        cx, cy = 120, 120
        for r in (14, 24, 34, 52, 80, 120, 170, 230, 300):
            d.ellipse([cx - r, cy - r * 0.42, cx + r, cy + r * 0.42], outline=(30, 70, 80, 255))
        light(img, cx, cy, 6, (255, 220, 140), 0.9)
        rng = np.random.default_rng(8501)
        for r, col in ((34, (90, 160, 255)), (52, (210, 120, 80)), (120, (220, 180, 120)), (300, (90, 140, 255))):
            a = rng.uniform(0, TAU)
            light(img, cx + r * np.cos(a), cy + r * 0.42 * np.sin(a), 2.5, col, 1.0)
        for _ in range(60):                                   # the Vrell contacts, every one greyed out
            r = rng.uniform(30, 300)
            a = rng.uniform(0, TAU)
            x, y = cx + r * np.cos(a), cy + r * 0.42 * np.sin(a)
            if 0 < x < PW and 0 < y < PH:
                d.line([x - 3, y - 3, x + 3, y + 3], fill=(110, 110, 116, 255))
                d.line([x - 3, y + 3, x + 3, y - 3], fill=(110, 110, 116, 255))
        text(img, 420, 20, "VRELL CONTACTS", (120, 200, 210))
        text(img, 420, 34, "NEPTUNE - EARTH", (120, 200, 210))
        text(img, 420, 56, "ACTIVE   0", (200, 200, 200))
        text(img, 420, 70, "STILL    ALL", (150, 150, 156))
        return img
    rgb = page_bg(8511, 0.4)
    yy, xx = np.mgrid[0:PH, 0:PW].astype(np.float64)
    d = np.hypot(xx - 520, yy - 520) / 420
    on = smooth01((1 - d) * 120)
    lit = np.clip(0.35 + (xx - 520) / -900 + (yy - 520) / -900, 0.1, 1)
    earth = _ramp(_fbm(PW, PH, 30, 8512, 4), [(0, (20, 50, 120)), (0.55, (40, 90, 160)), (0.7, (230, 236, 240)),
                                              (1, (250, 250, 255))]) * lit[..., None]
    rgb = rgb * (1 - on[..., None]) + earth * on[..., None]
    rgb += (np.exp(-((d - 1) / 0.03) ** 2) * 0.6)[..., None] * np.array((120, 170, 255))
    img = _arr(rgb)
    rng = np.random.default_rng(8513)
    sw = spr("needler").resize((14, 14), Image.LANCZOS)
    for _ in range(70):                                        # a swarm hanging still, its glows out
        x, y = rng.normal(260, 90), rng.normal(110, 45)
        put(img, b6.darkened(sw, 0.55), x, y)
    return img


def end_lock(v):
    """a: Varga's screen: the ring's glyphs as a lock's wards, Vorne's file header flagged red; b: the lock diagram
    as a hologram over her lab bench."""
    img = Image.new("RGBA", (PW, PH), (6, 8, 12, 255))
    cx, cy = (190, 120) if v == "a" else (336, 110)
    if v == "a":
        disp(img, (20, 14, 360, 226))
        disp(img, (380, 14, 652, 226), (120, 60, 50))
    a = arr(img)
    rgb = a[..., :3]
    yy, xx = np.mgrid[0:PH, 0:PW].astype(np.float64)
    r, ang = np.hypot(xx - cx, yy - cy), np.arctan2(yy - cy, xx - cx)
    col = np.array((120, 210, 200)) if v == "a" else np.array((90, 200, 220))
    for R in (30, 52, 74, 92):                                 # the wards: concentric rings broken by notches
        notch = (np.sin(ang * (5 + R // 20) + R) > -0.6)
        rgb += (np.exp(-((r - R) / 1.4) ** 2) * notch)[..., None] * col * 0.8
    key = (np.abs(xx - cx) < 5) & (yy > cy - 8) & (yy < cy + 26) | (np.hypot(xx - cx, yy - cy + 10) < 9)
    rgb += soft(key.astype(float), 1.0)[..., None] * np.array((255, 214, 140)) * 0.7
    cells = ang * 92 / 9.0
    mark = (np.abs(r - 104) < 3) & ((np.sin(np.floor(cells) * 12.9) * 4375.5) % 1.0 > 0.35) & (np.abs(cells % 1 - 0.5) < 0.25)
    rgb += mark[..., None] * col * 0.9
    if v == "b":
        rgb += (smooth01(1 - r / 140) * 0.25)[..., None] * col
        bench = yy > 190
        rgb[:] = np.where(bench[..., None], np.array((20, 22, 28)) + (yy[..., None] - 190) * 0.3, rgb)
        for x0 in (60, 520):
            rgb[150:190, x0:x0 + 90] = np.array((30, 36, 44))
            rgb[156:184, x0 + 6:x0 + 84] = np.array((10, 30, 34))
    a[..., :3] = np.clip(rgb, 0, 255)
    img = Image.fromarray(a.astype(np.uint8), "RGBA")
    if v == "a":
        text(img, 396, 28, "ASCENDANCY ARCHIVE", (200, 200, 200))
        text(img, 396, 42, "OWNER  S. VORNE", (200, 200, 200))
        text(img, 396, 56, "SUBJECT  THE SILENCE", (255, 90, 70))
        d = ImageDraw.Draw(img)
        d.rectangle([392, 74, 560, 90], fill=(150, 30, 24, 255))
        text(img, 398, 79, "FLAGGED - DR. VARGA", (255, 230, 220))
        for i in range(6):
            d.line([396, 104 + 16 * i, 396 + 120 + 90 * ((i * 37) % 3) / 2, 104 + 16 * i], fill=(80, 80, 86, 255), width=3)
    return img


def end_rook(v):
    """a: Rook on the flight deck beside two scorched Stormhawks, helmet under his arm; b: his helmet resting on a
    scorched wing (close)."""
    rgb = np.zeros((PH, PW, 3)) + (10, 12, 16)
    yy, xx = np.mgrid[0:PH, 0:PW].astype(np.float64)
    deck = yy > 120
    rgb[deck] = np.array((34, 36, 40))
    for k in range(-8, 9):                                      # deck lines in perspective
        x0 = 336 + k * 40
        m = (np.abs(xx - (336 + (x0 - 336) * (yy - 100) / 140)) < 1.2) & deck
        rgb[m] = (60, 62, 52)
    for x in range(60, PW, 120):                                 # overhead lights
        glow_at(rgb, x, 12, 26, (220, 220, 200), 0.4)
    img = _arr(rgb)
    if v == "a":
        hawk = spr("ship_2")
        for x, s_ in ((170, 4), (500, 4)):
            h = hawk.resize((hawk.width * s_, hawk.height * s_), Image.NEAREST)
            ha = arr(h)
            sc = smooth01((_fbm(h.width, h.height, 10, x, 3) - 0.45) * 4)       # soot and scorch
            ha[..., :3] *= (0.62 - 0.4 * sc)[..., None]
            put(img, Image.fromarray(np.clip(ha, 0, 255).astype(np.uint8), "RGBA"), x, 150)
        d = ImageDraw.Draw(img)
        x, y = 336, 120                                          # Rook: a silhouette, helmet under the arm
        d.ellipse([x - 10, y - 26, x + 10, y - 6], fill=(26, 24, 26, 255))
        d.polygon([(x - 20, y - 4), (x + 20, y - 4), (x + 16, y + 52), (x - 16, y + 52)], fill=(40, 46, 52, 255))
        d.rectangle([x - 15, y + 52, x - 4, y + 96], fill=(34, 38, 44, 255))
        d.rectangle([x + 4, y + 52, x + 15, y + 96], fill=(34, 38, 44, 255))
        d.ellipse([x + 14, y + 10, x + 38, y + 34], fill=(70, 80, 96, 255), outline=(20, 20, 24, 255))
        d.ellipse([x + 20, y + 16, x + 34, y + 26], fill=(240, 180, 80, 255))
        light(img, x - 10, y - 20, 8, (255, 220, 160), 0.25)
        return img
    d = ImageDraw.Draw(img)
    d.polygon([(0, 240), (0, 150), (672, 90), (672, 240)], fill=(70, 78, 90, 255))       # a wing, close
    d.line([(0, 150), (672, 90)], fill=(140, 150, 160, 255), width=3)
    a = arr(img)
    sc = smooth01(1 - np.hypot(xx - 420, yy - 170) / 140) * (yy > 100)
    a[..., :3] *= (1 - 0.75 * sc)[..., None]
    img = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    d.ellipse([230, 96, 330, 176], fill=(76, 86, 104, 255), outline=(20, 22, 28, 255), width=2)
    d.chord([244, 112, 318, 160], 200, 340, fill=(240, 176, 70, 255))
    light(img, 270, 120, 16, (255, 240, 200), 0.3)
    return img


def end_ring(v):
    """a: the dark ring from the near side, a listening buoy's small light before it, a faint trace on its screen;
    b: the buoy small before the ring from afar, Neptune beside."""
    rgb = page_bg(8531, 0.3)
    if v == "a":
        ring_paint(rgb, 336, 520, 470, 90, 8532, glyphs=0.0)
        img = _arr(rgb)
        light(img, 336, 150, 2.0, (255, 220, 140), 1.0)
        light(img, 336, 150, 9, (255, 200, 120), 0.25)
        disp(img, (500, 20, 650, 90), (60, 90, 110))
        d = ImageDraw.Draw(img)
        pts = [(506 + i, 60 - (14 * np.exp(-((i - 92) / 3.0) ** 2) + 2 * np.sin(i * 0.4)) * (1 if i > 40 else 0.3))
               for i in range(138)]
        d.line(pts, fill=(140, 200, 160, 255), width=1)
        text(img, 506, 24, "BUOY 1 - LISTENING", (120, 200, 200))
        return img
    neptune(rgb, 520, 70, 26)
    ring_paint(rgb, 250, 150, 130, 26, 8533, glyphs=0.0)
    img = _arr(rgb)
    light(img, 420, 190, 1.6, (255, 220, 140), 1.0)
    light(img, 420, 190, 7, (255, 200, 120), 0.25)
    return img


PAGES = [("act-7-end-still", "OKAFOR: THE WAR IS OVER", end_plot), ("act-7-end-lock", "VARGA: THE GATE WAS A LOCK", end_lock),
         ("act-7-end-rook", "ROOK: WE DON'T ANSWER", end_rook), ("act-7-end-dark-ring", "OKAFOR: ONE BUOY STAYS", end_ring)]


def make_ending(variants):
    for v in variants:
        cells = [f(v) for _, _, f in PAGES]
        img = panel_grid(cells, 1, pad=10, labels=[f"PAGE {i + 1} {n} - {lab}" for i, (n, lab, _) in enumerate(PAGES)])
        what = {"a": "A: THE ACT README'S IDEAS: THE STEADFAST'S PLOT OF THE SOLAR SYSTEM WITH EVERY VRELL CONTACT FROM "
                     "NEPTUNE TO EARTH GREYED OUT; VARGA'S SCREEN, THE RING'S GLYPHS AS A LOCK'S WARDS AND VORNE'S FILE "
                     "HEADER FLAGGED RED; ROOK ON THE FLIGHT DECK BESIDE TWO SCORCHED STORMHAWKS, HELMET UNDER HIS ARM; "
                     "THE DARK RING FROM THE NEAR SIDE, A LISTENING BUOY'S LIGHT AND A FAINT TRACE ON ITS SCREEN.",
                "b": "B: THE SAME BEATS AS SCENES: A VRELL SWARM HANGING STILL BEFORE EARTH; THE LOCK DIAGRAM AS A "
                     "HOLOGRAM OVER VARGA'S BENCH; ROOK'S HELMET RESTING ON A SCORCHED WING; THE BUOY SMALL BEFORE THE "
                     "DARK RING FROM AFAR, NEPTUNE BESIDE."}[v]
        notes = [what,
                 "IMAGE IDEAS AT THE BRIEFING SIZE (672X240) FOR THE FOUR ENDING PAGES (TRACK 27); THE PRODUCTION IMAGES "
                 "ARE PAINTED IN M11 WITH THE OTHER BRIEFING IMAGES; THESE ARE COMPOSITION SKETCHES. LANCER HAS NO FACE; THE SILENCE IS NEVER SHOWN "
                 "(PAGE 4'S TRACE IS ONLY A LINE, NOT THE CHOIR'S PATTERN)."]
        sheet = compose(f"THE ENDING PAGES {v.upper()} (R84)", [("THE FOUR PAGES AT 1X", img)], notes)
        save_png(sheet, ACT7 / f"ending-pages-r84-{v}.png")


# =========================================================================== main

SUBJECTS = {"chimera": make_chimera, "heart": make_heart, "fused": make_fused, "frame": make_frame, "echo": make_echo, "moods": make_moods, "transit": make_transit, "still": make_still, "closing": make_closing, "ending": make_ending}


def main(args):
    names = [a for a in args if a in SUBJECTS] or list(SUBJECTS)
    bad = [a for a in args if a not in SUBJECTS and a not in ("a", "b")]
    if bad:
        sys.exit(f"unknown: {bad}; subjects: {', '.join(SUBJECTS)}")
    variants = [a for a in args if a in ("a", "b")] or ["a", "b"]
    for n in names:
        SUBJECTS[n](variants)


if __name__ == "__main__":
    main(sys.argv[1:])
