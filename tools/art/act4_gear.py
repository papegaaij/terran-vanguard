#!/usr/bin/env python3
"""Production art: the Act 4 "Deep Water" gear of M8 part A (concept round 48's picks, final review in
round 50): Harpoon Torpedoes (G1 a), Depth Charges (G2 a), Sonar Pulse (G3 b), the Bulwark heavy drone
(G4 a), the Stormhawk's hydro-kit (E10 a) with Rook's variant, and the Decoy Flares (round 09's a).
The looks are the chosen concept generators' (tools/concept/gear_r48.py, europa_r48.py ``kit_model``,
vfx_r09.py ``flare_sprite``), imported unchanged and cut to game frames here. The hangar icons of the
four new items are tools/art/icons.py ``--act4``.

Outputs (assets/sprites/; ``additive`` = premultiplied on black, blended GL_SRC_ALPHA, GL_ONE; ``normal``
= straight alpha; frame times in game steps of 1/60 s; headings k x 360/n degrees clockwise from up):

  Harpoon Torpedoes (design/player/weapons/harpoon-torpedoes)
  harpoon-torpedoes-shot_0..31      24x24 normal: the barbed harpoon (6x22 body) at 32 headings, centre
                                    (12, 12), the teal motor ring lit in the model
  harpoon-torpedoes-glow            9x9 additive: the motor's light, centred 11 px behind the shot's
                                    centre along its heading
  harpoon-torpedoes-cable           3x4 additive: the harpoon's line, a 1 px teal-white core with a dim
                                    halo column either side; the game stretches it along the shot's
                                    path (x across, y along), 150 px long, alpha 0.92 x (1 - l / 150)
                                    at l px behind the shot, offset sideways by 1.8 x (l / 150) x
                                    sin(0.08 l - 9 t) px (the slack wave), fading out over 0.35 s after
                                    a hit
  harpoon-torpedoes-muzzle          20x20 additive: the launch light at the nose for 6 steps
  harpoon-torpedoes-cavity_0..2     16x16 normal: the dark cavity flash of an impact (the thud), drawn
                                    under hit frames 0..2
  harpoon-torpedoes-hit_0..7        48x48 additive: the impact, centre (24, 24): the teal pressure ring
                                    out to the 20 px blast radius, five barb sparks, the light; 3 steps
                                    a frame
  Depth Charges (design/player/weapons/depth-charges)
  depth-charges-shot_0..47          16x16 normal: the navy drum with its yellow bands, index
                                    stage x 16 + spin: spin k = k x 11.25 degrees (the drum repeats
                                    every 180), stage 0 / 1 / 2 = sinking (scale 1 / 0.89 / 0.78,
                                    darker); stage 1 from a third of the fuse, stage 2 from two thirds
  depth-charges-lamp                8x8 additive: the fuse lamp on the drum's centre, blinking: period
                                    0.32 - 0.24 p s (p = age / fuse), lit in each period's first half
  depth-charges-burst-<R>_0..9      (2R + 12)^2 additive, R = 40 / 46 / 52 / 58 / 64 (the levels' burst
                                    radius): the pressure dome, white-teal flash disc, the bright rim
                                    ring reaching R in 0.19 s, the faint silt ring; 3 steps a frame
  depth-charges-cavity-<R>_0..3     (2R + 12)^2 normal: the dark cavity collapsing, drawn under burst
                                    frames 2..5
  Sonar Pulse (design/player/specials)
  sonar-pulse-dash                  4x1 additive: one dash of the sweep; the game lays 36 dashes of
                                    6 degrees every 10 degrees on two rings, the outer at r (alpha 240)
                                    and the inner at r - 7 (alpha 130), stretched along the arc
  sonar-pulse-corner_0..1           6x6 additive: a contact bracket's top-left corner (mirror it for
                                    the others), 0 = the ping (full), 1 = steady (170); placed 3 px
                                    outside the unit's sprite box, snapping in from 8 px further out
                                    over 0.3 s, frame 0 for 0.4 s, hidden on alternate 1/8 s in the
                                    window's last second
  Decoy Flares (design/player/specials)
  decoy-flares-flare_0..3           24x24 additive: the white-hot magnesium flare with sparkle spikes,
                                    a flicker loop, 2 steps a frame
  decoy-flares-glow                 48x48 additive: its warm halo (9 steps, ordered dither), under the flare
  Bulwark (design/player/wingmen; held until the game data claims ``bulwark``, see below)
  bulwark_0..2                      36x36 normal: the turtle drone: 0 intact, 1 hit (the white flash),
                                    2 armour under half (scorched plates)
  bulwark-jets_0..2                 36x36 additive: the twin pump-jets' glow, aligned with the hull
                                    frame, a flicker loop, 2 steps a frame
  bulwark-lure_0..9                 56x56 additive: the cyan lure halo breathing out from 15 to 23 px
                                    once a second, 6 steps a frame (drawn under the hull)
  bulwark-shot                      6x22 additive: the cannon's cyan slug (top, centre (3, 5)) with its
                                    straight wake trailing down
  bulwark-muzzle                    14x14 additive: the muzzle light, centred 18 px ahead of the hull's
                                    centre, for 6 steps
  bulwark-spark_0..3                28x28 additive: a bullet hit's sparks, centred 6 px ahead of the
                                    hull's centre, 4 steps a frame
  bulwark-loss_0..9                 160x160 additive: the under-water burst of its loss (flash, light,
                                    pressure ring); 3 steps a frame, after 6 steps of frame 1
  bulwark-debris_0..9               160x160 normal: the loss's dark debris, drawn over loss frames
  Hydro-kit (design/world/europa: the sealed refit, E10 a)
  ship-hydro_0..4                   48x48 normal: the Stormhawk's banking frames with the kit (pump-jet
                                    shrouds, ballast saddles, seal plates, twin headlights, nav lights),
                                    the same camera and mounts as ship_0..4 (pivots/ship.json and
                                    pods.json apply unchanged)
  rook-hydro_0..4                   42x42 normal: Rook's craft with the same kit parts on its airframe
                                    (pivots/rook.json applies unchanged)

  Review sheets and GIFs of round 50: <part's concept/>/<subject>-final-r50-a.png/.gif

Held sprites (tools/art/act3kit.py): a frame set whose root the game data does not claim yet is written
to build/art-held/sprites/ instead (the harpoon and the charges until their data.yaml exists, the Sonar
Pulse until design/player/specials/data.yaml lists it, the Bulwark until
pipeline/.../SpriteUse.java names ``bulwark``); rerun the part once it is claimed. ``ship-*`` and
``rook-*`` are always claimed, as are the Decoy Flares.

Run: python3 tools/art/act4_gear.py [harpoon] [charges] [sonar] [flares] [bulwark] [hydro] [--review]
     (no part = all, ~1 min; --review only rebuilds the sheets from the written frames)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import act3kit
import artkit
from artkit import DESIGN, ROOT, TAU, sprite

import enemies_r48 as e48  # noqa: E402  (concept scripts, imported unchanged)
import gear_r36 as g36  # noqa: E402
import gear_r48 as g48  # noqa: E402
import vfx_r08 as v8  # noqa: E402
import vfx_r09 as v9  # noqa: E402
from render import raster  # noqa: E402
from render.palette import B  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "act4_gear.py"
BATCH = "M8 part A batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r50"
artkit.REVIEW_ROUND = ROUND
PLAYER = DESIGN / "player"
CONCEPT = {"harpoon": PLAYER / "weapons" / "harpoon-torpedoes" / "concept",
           "charges": PLAYER / "weapons" / "depth-charges" / "concept",
           "sonar": PLAYER / "specials" / "concept", "flares": PLAYER / "specials" / "concept",
           "bulwark": PLAYER / "wingmen" / "concept", "hydro": DESIGN / "world" / "europa" / "concept"}
SPIPELINE = ROOT / "pipeline" / "src" / "main" / "java" / "vanguard" / "pipeline" / "SpriteUse.java"
TEAL_W = g48.TEAL_W
SONAR_B = g48.SONAR_B
WHITE = (255, 255, 255)
PS = v8.PS
ACC_C = v8.ACC_C
FPS = 60


# =========================================================================== shared

def black(w, h):
    return Image.new("RGBA", (w, h), (0, 0, 0, 255))


def from_black(img):
    """A frame drawn onto opaque black -> the additive sprite: premultiplied on black, alpha 1
    wherever it adds light."""
    a = np.array(img.convert("RGB")).astype(np.uint8)
    alpha = np.where(a.max(axis=-1) >= 1, 255, 0).astype(np.uint8)
    return Image.fromarray(np.dstack([a, alpha]), "RGBA")


def layer(w, h):
    lay = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    return lay, ImageDraw.Draw(lay)


def ring(d, x, y, r, col, a, w=1):
    if r > 0.5:
        d.ellipse([x - r, y - r, x + r, y + r], outline=tuple(col) + (int(np.clip(a, 0, 255)),), width=w)


def disc(d, x, y, r, col, a):
    if r > 0.3:
        d.ellipse([x - r, y - r, x + r, y + r], fill=tuple(col) + (int(np.clip(a, 0, 255)),))


def turned(scene, clockwise):
    """A model (nose +y) turned ``clockwise`` radians on the screen."""
    return lambda p: scene(rotate_z(p, -clockwise))


def claimed(root):
    if root == "bulwark":
        return '"bulwark"' in SPIPELINE.read_text(encoding="utf-8")
    return act3kit.claimed(root)


def write(root, sets):
    """Write ``{name: frames}`` (or a single image for ``name`` without frames) under ``root``'s
    folder: assets/sprites when the data claims it, build/art-held/sprites until then."""
    target = act3kit.ASSETS_SPRITES if claimed(root) else act3kit.HELD
    target.mkdir(parents=True, exist_ok=True)
    before = artkit.SPRITES
    artkit.SPRITES = target
    try:
        for name, frames in sets.items():
            if isinstance(frames, Image.Image):
                artkit.write_frames(name, [frames], SOURCE, single=True)
            else:
                artkit.write_frames(name, frames, SOURCE)
    finally:
        artkit.SPRITES = before
    print(f"{root}: {sum(1 if isinstance(f, Image.Image) else len(f) for f in sets.values())} frames -> "
          f"{target.relative_to(ROOT)}")


def load(name):
    """A written frame set back (assets/sprites, else the held folder)."""
    for folder in (act3kit.ASSETS_SPRITES, act3kit.HELD):
        before = artkit.SPRITES
        artkit.SPRITES = folder
        try:
            frames = artkit.load_frames(name)
        finally:
            artkit.SPRITES = before
        if frames:
            return frames
    raise FileNotFoundError(name)


def water_plate(w, h, seed=31, sx=None, sy=None):
    """A crop of the round-48 under-water grade (L24's lamps' edge) as the review's background."""
    water = e48.Water("edge", seed=seed)
    frame = water.frame(0.0, sx if sx is not None else 240, sy if sy is not None else 470)
    return frame.crop((240 - w // 2, 540 - h, 240 - w // 2 + w, 540)).convert("RGBA")


def put(img, sp, x, y):
    sprite.paste_center(img, sp, x, y)


def add(img, glow, x, y):
    """An additive frame centred at (x, y), as the game blends it."""
    return artkit.add_light(img, glow, (int(round(x - glow.width / 2)), int(round(y - glow.height / 2))))


def save_review(part, subject, title, rows, gif, fps=20, width=1300, zoom=2):
    sheet = artkit.review_sheet(title, rows, width=width, batch=BATCH)
    artkit.save_review(sheet, [sprite.enlarge(f, zoom) for f in gif], CONCEPT[part], subject, fps=fps)


# =========================================================================== Harpoon Torpedoes

H_SIZE, H_EXTENT, H_HEADINGS = 24, 2.2, 32
HIT = 48
CABLE = 150.0


def _harpoon_job(k):
    scene, mats = g48.harpoon_model("a")
    hi, factor = artkit.render_hi(turned(scene, TAU * k / H_HEADINGS), mats, (H_SIZE, H_SIZE), H_EXTENT)
    return artkit.native(hi, factor)


def harpoon_frames():
    with ProcessPoolExecutor() as pool:
        shots = artkit.quantize_set(list(pool.map(_harpoon_job, range(H_HEADINGS))), 24)
    glow = from_black(raster.add_light(black(9, 9), 4, 4, 3.5, TEAL_W, 0.7))
    cable = black(3, 4)
    a = np.array(cable)
    a[:, 1, :3] = TEAL_W
    a[:, (0, 2), :3] = np.round(np.array((60, 160, 170)) * 0.35)
    cable = from_black(Image.fromarray(a, "RGBA"))
    muzzle = from_black(raster.add_light(black(20, 20), 10, 10, 5, TEAL_W, 0.6))
    cavity = []
    for i in range(3):
        s = i * 0.08
        lay, d = layer(16, 16)
        disc(d, 8, 8, 6, (4, 18, 24), 210 * (1 - s / 0.25))
        cavity.append(artkit.stepped_alpha(lay, 5))
    hits = []
    for i in range(8):
        s = i * 3 / FPS / 0.45
        f = black(HIT, HIT)
        lay, d = layer(HIT, HIT)
        c = HIT / 2
        ring(d, c, c, 4 + 16 * e48.smooth(s / 0.6), TEAL_W, 230 * (1 - s), 2 if s < 0.3 else 1)
        for k in range(5):
            ang = k * TAU / 5 + 0.5
            r0 = 3 + 14 * s
            d.line([(c + np.cos(ang) * r0, c + np.sin(ang) * r0), (c + np.cos(ang) * (r0 + 4), c + np.sin(ang) * (r0 + 4))],
                   fill=WHITE + (int(255 * (1 - s)),), width=1)
        f = raster.add_light(f, c, c, 4 + 8 * (1 - s), TEAL_W, 1.3 * (1 - s))
        f.alpha_composite(lay)
        hits.append(from_black(f))
    hits = act3kit.quantize_bright(hits, 48)
    return {"harpoon-torpedoes-shot": shots, "harpoon-torpedoes-glow": glow, "harpoon-torpedoes-cable": cable,
            "harpoon-torpedoes-muzzle": muzzle, "harpoon-torpedoes-cavity": cavity, "harpoon-torpedoes-hit": hits}


def draw_cable(img, pts, t, fade=1.0):
    """The game's cable rule (see the header) drawn with the cable sprite's colours."""
    lay, d = layer(*img.size)
    acc = 0.0
    for (x0, y0), (x1, y1) in zip(pts[:-1], pts[1:]):
        seg = np.hypot(x1 - x0, y1 - y0)
        if seg < 1e-6:
            continue
        l0, acc = acc, acc + seg
        if l0 > CABLE:
            break
        nx, ny = -(y1 - y0) / seg, (x1 - x0) / seg
        o0 = 1.8 * (l0 / CABLE) * np.sin(l0 * 0.08 - t * 9)
        o1 = 1.8 * (acc / CABLE) * np.sin(acc * 0.08 - t * 9)
        a = 235 * (1 - l0 / CABLE) * fade
        p0, p1 = (x0 + nx * o0, y0 + ny * o0), (x1 + nx * o1, y1 + ny * o1)
        d.line([p0, p1], fill=(60, 160, 170, int(a * 0.35)), width=3)
        d.line([p0, p1], fill=TEAL_W + (int(a),), width=1)
    img.alpha_composite(lay)


def harpoon_review():
    shots = load("harpoon-torpedoes-shot")
    hits, cavity = load("harpoon-torpedoes-hit"), load("harpoon-torpedoes-cavity")
    glow, muzzle = load("harpoon-torpedoes-glow")[0], load("harpoon-torpedoes-muzzle")[0]
    cable = load("harpoon-torpedoes-cable")[0]
    # the loop: two torpedoes (L5) run up from the nose, curve onto a stand-in Needler and hit it
    W, H = 200, 300
    base = water_plate(W, H)
    ship = g36.asset("ship_2")
    target = g36.asset("needler_0")
    gif = []
    tx, ty = 130, 60
    for i in range(40):
        t = i / 20
        f = base.copy()
        put(f, target, tx, ty)
        for off, t_fire in ((-6, 0.0), (6, 0.0), (-6, 1.0), (6, 1.0)):
            if t < t_fire:
                continue
            x, y, h, hist = 100 + off, 250 - 22, 0.0, []
            tt, gone = 0.0, None
            while tt < t - t_fire:
                want = np.arctan2(tx - x, -(ty - y))
                h += np.clip((want - h + np.pi) % TAU - np.pi, -np.radians(90) / 60, np.radians(90) / 60)
                s = (360 + 160 * min(1, tt / 0.6)) * 0.7 / 60
                x, y = x + np.sin(h) * s, y - np.cos(h) * s
                hist.append((x, y))
                tt += 1 / 60
                if np.hypot(tx - x, ty - y) < 12:
                    gone = tt
                    break
            if gone is None:
                if hist:
                    draw_cable(f, [(x, y)] + hist[::-1], t)
                k = int(round(h / TAU * H_HEADINGS)) % H_HEADINGS
                f = add(f, glow, x - np.sin(h) * 11, y + np.cos(h) * 11)
                put(f, shots[k], x, y)
            else:
                gone += t_fire
                draw_cable(f, hist[::-1], t, max(0.0, 1 - (t - gone) / 0.35))
                j = int((t - gone) * 20)
                if j < 3:
                    put(f, cavity[j], x, y)
                if j < 8:
                    f = add(f, hits[j], x, y)
        put(f, ship, 100, 250)
        if t % 1.0 < 0.1:
            f = add(f, muzzle, 100, 228)
        gif.append(f)
    rows = [("32 HEADINGS, 11.25 DEG CLOCKWISE FROM UP (NORMAL)", shots[:16], 3, False),
            ("HEADINGS 16..31", shots[16:], 3, False),
            ("IMPACT: DARK CAVITY (NORMAL, UNDER HIT 0..2) AND HIT RING + BARB SPARKS (ADDITIVE, 3 STEPS)", cavity + hits, 3, True),
            ("MOTOR GLOW, MUZZLE LIGHT, CABLE TILE (ADDITIVE)", [glow, muzzle, cable.resize((9, 12), Image.NEAREST)], 4, True),
            ("IN THE WATER: L5 PAIR, CABLE, HIT (1X FRAMES)", [gif[k] for k in (6, 14, 22, 30, 34)], 1, False)]
    save_review("harpoon", "harpoon-torpedoes", "HARPOON TORPEDOES - FINAL (ROUND 48 G1 = A)", rows, gif)


# =========================================================================== Depth Charges

DC = 16
DC_EXTENT = 1.7
STAGES = [(1.0, 0.0), (0.89, 0.13), (0.78, 0.25)]   # (scale, darkening) while sinking
RADII = [40, 46, 52, 58, 64]


def _charge_job(job):
    stage, k = job
    scale, _ = STAGES[stage]
    scene, mats = g48.charge_model("a")
    hi, factor = artkit.render_hi(turned(scene, np.pi * k / 16), mats, (DC, DC), DC_EXTENT / scale)
    return artkit.native(hi, factor)


def charge_frames():
    jobs = [(s, k) for s in range(len(STAGES)) for k in range(16)]
    with ProcessPoolExecutor() as pool:
        raw = list(pool.map(_charge_job, jobs))
    out = []
    for (stage, _), img in zip(jobs, raw):
        a = np.array(img).astype(np.float64)
        dark = STAGES[stage][1]
        a[..., :3] = a[..., :3] * (1 - dark) + np.array((10, 34, 44)) * dark * 0.6
        out.append(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"))
    shots = artkit.quantize_set(out, 24)
    lamp = from_black(raster.add_light(black(8, 8), 4, 4, 3.0, (210, 250, 255), 1.2))
    sets = {"depth-charges-shot": shots, "depth-charges-lamp": lamp}
    for R in RADII:
        n = 2 * R + 12
        c = n / 2
        bursts, cav = [], []
        for i in range(10):
            s = i * 3 / FPS / 0.55
            f = black(n, n)
            if s < 0.3:
                f = raster.add_light(f, c, c, R * 0.6, (160, 240, 230), 1.1 * (1 - s / 0.3))
            lay, d = layer(n, n)
            if s < 0.2:
                disc(d, c, c, R * 0.55 * e48.smooth(s / 0.2), (200, 255, 245), 210 * (1 - s / 0.2))
            rr = R * e48.smooth(min(1.0, s / 0.35))
            ring(d, c, c, rr, (170, 255, 240), 240 * (1 - s), 3 if s < 0.25 else 2)
            ring(d, c, c, rr - 4, (90, 200, 200), 120 * (1 - s), 1)
            if s > 0.35:
                ring(d, c, c, R, (120, 180, 170), 70 * (1 - s), 1)
            f.alpha_composite(lay)
            bursts.append(from_black(f))
            if 2 <= i <= 5:
                lay, d = layer(n, n)
                disc(d, c, c, R * 0.3 * (1 - (s - 0.12) / 0.48), (4, 16, 22), 170)
                cav.append(artkit.stepped_alpha(lay, 4))
        sets[f"depth-charges-burst-{R}"] = act3kit.quantize_bright(bursts, 48)
        sets[f"depth-charges-cavity-{R}"] = cav
    return sets


def charges_review():
    shots, lamp = load("depth-charges-shot"), load("depth-charges-lamp")[0]
    W, H = 220, 300
    base = water_plate(W, H, seed=57)
    ship = g36.asset("ship_2")
    gif = []
    sx, sy = 110, 60
    fuse, R = 0.8, 52
    bursts, cav = load(f"depth-charges-burst-{R}"), load(f"depth-charges-cavity-{R}")
    for i in range(48):
        t = i / 30
        f = base.copy()
        put(f, ship, sx, sy)
        for t0 in (0.0, 1 / 1.3):
            tau = t - t0
            if tau < 0:
                continue
            s = min(tau, 0.6)
            run = 147 * (s - s * s / 1.2)
            x, y = sx, sy + 24 + run + 30 * max(0.0, tau - 0.6)
            if tau < fuse:
                spin = 5.0 * s - 2.5 * s * s / 0.6 + 0.6 * max(0, tau - 0.6)
                k = int(round(spin / np.pi * 16)) % 16
                p = tau / fuse
                stage = 0 if p < 1 / 3 else 1 if p < 2 / 3 else 2
                put(f, shots[stage * 16 + k], x, y)
                period = 0.32 - 0.24 * p
                if (tau / period) % 1.0 < 0.5:
                    f = add(f, lamp, x, y)
            else:
                j = int((tau - fuse) * FPS / 3)
                if 2 <= j <= 5:
                    put(f, cav[j - 2], x, y)
                if j < 10:
                    f = add(f, bursts[j], x, y)
        gif.append(f)
    rows = [("16 SPINS (11.25 DEG, 180 DEG PERIOD), STAGE 0 (NORMAL)", shots[:16], 4, False),
            ("STAGES 0 / 1 / 2 WHILE SINKING (SPIN 0 AND 4)", [shots[s * 16 + k] for s in range(3) for k in (0, 4)], 4, False),
            ("FUSE LAMP (ADDITIVE)", [lamp], 4, True)]
    for R in (40, 52, 64):
        rows.append((f"BURST R {R} (ADDITIVE, 3 STEPS A FRAME)", load(f"depth-charges-burst-{R}")[:7], 1, True))
    rows.append(("CAVITY R 52 (NORMAL, UNDER BURST 2..5)", load("depth-charges-cavity-52"), 1, False))
    rows.append(("BEHIND THE SHIP: ROLL-OUT, HOVER, SINK, BURST (1X)", [gif[k] for k in (4, 14, 24, 30, 36)], 1, False))
    save_review("charges", "depth-charges", "DEPTH CHARGES - FINAL (ROUND 48 G2 = A)", rows, gif, fps=15)


# =========================================================================== Sonar Pulse

def sonar_frames():
    dash = black(4, 1)
    dash.paste(SONAR_B + (255,), (0, 0, 4, 1))
    corners = []
    for a in (255, 170):
        f = black(6, 6)
        d = ImageDraw.Draw(f)
        col = tuple(int(round(c * a / 255)) for c in SONAR_B) + (255,)
        d.line([(0, 0), (5, 0)], fill=col)
        d.line([(0, 0), (0, 5)], fill=col)
        corners.append(from_black(f))
    return {"sonar-pulse-dash": from_black(dash), "sonar-pulse-corner": corners}


def draw_sonar_ring(img, ox, oy, r, dash):
    """The game's ring rule: 36 dashes of 6 degrees every 10, outer at r (240), inner at r - 7 (130)."""
    col = np.array(dash).reshape(-1, 4)[0, :3]
    lay, d = layer(*img.size)
    for rr, al in ((r, 240), (r - 7, 130)):
        if rr <= 0:
            continue
        for k in range(36):
            d.arc([ox - rr, oy - rr, ox + rr, oy + rr], k * 10, k * 10 + 6, fill=tuple(int(c) for c in col) + (al,), width=1)
    img.alpha_composite(lay)


def draw_brackets(img, corners, x, y, sp, since):
    w, h = sp.width / 2 + 3, sp.height / 2 + 3
    grow = max(0.0, 1 - since / 0.3) * 8
    c = corners[0] if since < 0.4 else corners[1]
    for fx in (False, True):
        for fy in (False, True):
            im = c.transpose(Image.FLIP_LEFT_RIGHT) if fx else c
            im = im.transpose(Image.FLIP_TOP_BOTTOM) if fy else im
            cx = x + (w + grow) - 5 if fx else x - (w + grow)
            cy = y + (h + grow) - 5 if fy else y - (h + grow)
            img.paste(artkit.add_light(img, im, (int(round(cx)), int(round(cy)))))


def sonar_review():
    dash, corners = load("sonar-pulse-dash")[0], load("sonar-pulse-corner")
    W, H = 240, 300
    plate = Image.new("RGBA", (W, H), (4, 8, 12, 255))
    ship = g36.asset("ship_2")
    units = [("needler", 60, 70), ("needler", 180, 60), ("driftjelly", 120, 140), ("needler", 50, 200), ("driftjelly", 200, 190)]
    gif, pings = [], {}
    ox, oy = 120, 260
    far = max(np.hypot(cx - ox, cy - oy) for cx in (0, W) for cy in (0, H))
    for i in range(60):
        t = i / 20 - 0.3
        f = plate.copy()
        r = far * t / 0.5 if t >= 0 else -1
        for k, (kind, x, y) in enumerate(units):
            sp = g36.asset(f"{kind}_0")
            if r >= np.hypot(x - ox, y - oy):
                pings.setdefault(k, t)
                put(f, sp, x, y)
                draw_brackets(f, corners, x, y, sp, t - pings[k])
            else:
                put(f, g36.dim(sp, 0.08), x, y)
        if 0 <= r < 1400:
            draw_sonar_ring(f, ox, oy, r, dash)
        put(f, ship, ox, oy)
        gif.append(f)
    rows = [("DASH (4X1) AND CONTACT CORNERS: PING / STEADY (ADDITIVE)", [dash.resize((16, 4), Image.NEAREST)] + corners, 6, True),
            ("THE SWEEP IN THE BLACK: DASHED DOUBLE RING, BRACKETS SNAPPING IN (1X)", [gif[k] for k in (6, 9, 12, 20, 40)], 1, False)]
    save_review("sonar", "sonar-pulse", "SONAR PULSE - FINAL (ROUND 48 G3 = B)", rows, gif)


# =========================================================================== Decoy Flares

def flare_frames():
    flares = [from_black(Image.alpha_composite(black(24, 24), v9.flare_sprite(i))) for i in range(4)]
    flares = act3kit.quantize_bright(flares, 32)
    f = black(48, 48)
    yy, xx = np.mgrid[0:48, 0:48]
    d = np.hypot(xx - 23.5, yy - 23.5)
    g = np.exp(-(d / 12.0) ** 2)
    k = np.where(g < 0.06, 0.0, artkit.ordered_dither(g, 9)) * 0.55
    a = np.array(f).astype(np.float64)
    a[..., :3] = k[..., None] * np.array((255, 236, 200))
    glow = from_black(Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA"))
    return {"decoy-flares-flare": flares, "decoy-flares-glow": glow}


def flares_review():
    flares, glow = load("decoy-flares-flare"), load("decoy-flares-glow")[0]
    W, H = 240, 260
    base = water_plate(W, H, seed=73)
    ship = g36.asset("ship_2")
    sx, sy = 120, 60
    fan = [np.radians(a) for a in (-60, -20, 20, 60)]
    gif = []
    for i in range(40):
        t = i / 20
        f = base.copy()
        put(f, ship, sx, sy)
        for j, a in enumerate(fan):
            tt = min(t, 0.6)
            dist = 300 * 0.7 * (tt - tt * tt / 1.2)
            x, y = sx + np.sin(a) * dist, sy + 18 + np.cos(a) * dist + 6 * t
            f = add(f, glow, x, y)
            f = add(f, flares[(i + j) % 4], x, y)
        gif.append(f)
    rows = [("FLARE FLICKER LOOP, 2 STEPS A FRAME (ADDITIVE)", flares, 4, True),
            ("WARM HALO UNDER IT (ADDITIVE, 9 STEPS, ORDERED DITHER)", [glow], 3, True),
            ("FOUR FLARES IN A 120 DEG FAN BEHIND THE SHIP (1X)", [gif[k] for k in (2, 6, 12, 30)], 1, False)]
    save_review("flares", "decoy-flares", "DECOY FLARES - FINAL (ROUND 09 = A)", rows, gif)


# =========================================================================== Bulwark

BW = 36
BW_EXTENT = 2.05
LOSS = 160


def bulwark_frames():
    scene, mats = g48.bulwark_model("a")
    hi, factor = artkit.render_hi(scene, mats, (BW, BW), BW_EXTENT)
    hull = artkit.native(hi, factor)
    states = artkit.quantize_set([hull, g36.flash_sprite(hull, 0.6), g48.damaged(hull)], 32)
    jets = []
    for k in (0.45, 0.62, 0.7):
        f = black(BW, BW)
        for o in (-5, 5):
            f = raster.add_light(f, BW / 2 + o, BW / 2 + 13, 3, ACC_C[2], k)
        jets.append(from_black(f))
    lure = []
    for i in range(10):
        s = i / 10
        f = black(56, 56)
        lay, d = layer(56, 56)
        ring(d, 28, 28, 15 + 8 * s, ACC_C[2], 120 * (1 - s), 1)
        f.alpha_composite(lay)
        lure.append(from_black(f))
    shot = black(6, 22)
    lay, d = layer(6, 22)
    d.line([(3, 9), (3, 21)], fill=(170, 230, 240, 110), width=3)
    d.rectangle([1.5, 1, 4.5, 9], fill=PS[2] + (255,))
    d.rectangle([2.5, 2, 3.5, 8], fill=WHITE + (255,))
    shot.alpha_composite(lay)
    muzzle = from_black(raster.add_light(black(14, 14), 7, 7, 5, PS[3], 1.1))
    sparks = []
    for i in range(4):
        s = i / 4
        f = black(28, 28)
        rng = np.random.default_rng(7)
        lay, d = layer(28, 28)
        for _ in range(5):
            a = rng.uniform(-np.pi, 0)
            r = 3 + 9 * s
            d.point((14 + np.cos(a) * r, 14 + np.sin(a) * r), fill=(230, 250, 255, int(255 * (1 - s))))
        f = raster.add_light(f, 14, 14, 4, (200, 240, 255), 1.0 * (1 - s))
        f.alpha_composite(lay)
        sparks.append(from_black(f))
    loss, debris = [], []
    size, col, c = 1.3, (200, 240, 255), LOSS / 2
    for i in range(10):
        s = i / 10
        rr = (6 + 16 * s) * size
        f = raster.add_light(black(LOSS, LOSS), c, c, rr * 1.4, col, (1.0 - s) * 1.1)
        lay, d = layer(LOSS, LOSS)
        if s < 0.35:
            disc(d, c, c, rr * 0.5 * (1 - s), (255, 240, 210), 255 * (1 - s / 0.35))
        r1 = (10 + 46 * s) * size
        ring(d, c, c, r1, (170, 230, 230), 170 * (1 - s), 1)
        f.alpha_composite(lay)
        loss.append(from_black(f))
        lay, d = layer(LOSS, LOSS)
        for k in range(6):
            a = k * 1.05 + 0.4
            dd = rr * (0.4 + 1.1 * s)
            d.rectangle([c + np.cos(a) * dd - 1, c + np.sin(a) * dd - 1 + 8 * s,
                         c + np.cos(a) * dd + 1, c + np.sin(a) * dd + 1 + 8 * s], fill=(90, 70, 80, int(255 * (1 - s))))
        debris.append(artkit.stepped_alpha(lay, 4))
    return {"bulwark": states, "bulwark-jets": jets, "bulwark-lure": act3kit.quantize_bright(lure, 24),
            "bulwark-shot": from_black(shot), "bulwark-muzzle": muzzle, "bulwark-spark": sparks,
            "bulwark-loss": act3kit.quantize_bright(loss, 48), "bulwark-debris": debris}


def bulwark_review():
    states, jets, lure = load("bulwark"), load("bulwark-jets"), load("bulwark-lure")
    shot, muzzle, sparks = load("bulwark-shot")[0], load("bulwark-muzzle")[0], load("bulwark-spark")
    loss, debris = load("bulwark-loss"), load("bulwark-debris")
    W, H = 240, 300
    base = water_plate(W, H, seed=91)
    ship = g36.asset("ship_2")
    gif = []
    for i in range(60):
        t = i / 20
        f = base.copy()
        sx, sy = 90 + 20 * np.sin(t * 1.2), 230
        bx, by = sx + 56 - 10 * np.cos(t * 1.2), sy + 20
        put(f, ship, sx, sy)
        lost = t >= 2.2
        if not lost:
            f = add(f, lure[i % 10], bx, by)
            hull = states[2] if t > 1.2 else states[0]
            if 0.9 <= t < 0.95 or 1.6 <= t < 1.65:
                hull = states[1]
            sh = sprite.shadow_of(hull, opacity=0.45, blur=1.2, scale=0.85)
            put(f, sh, bx + 14, by + 20)
            f = add(f, jets[i % 3], bx, by)
            put(f, hull, bx, by)
            for t0 in (0.5, 1.5):
                if 0 <= t - t0 < 0.1:
                    f = add(f, muzzle, bx, by - 18)
                if t >= t0:
                    f = add(f, shot, bx, by - 17 - 420 * (t - t0) + 6)
            for th in (0.9, 1.6):
                if 0 <= t - th < 0.25:
                    f = add(f, sparks[int((t - th) / 0.0625)], bx, by - 6)
        else:
            j = int((t - 2.2) * 20)
            if j < 10:
                f = add(f, loss[j], bx, by)
                put(f, debris[j], bx, by)
        gif.append(f)
    rows = [("36X36: INTACT, HIT, UNDER HALF (NORMAL)", states, 4, False),
            ("PUMP-JETS (ADDITIVE, HULL-ALIGNED), LURE HALO LOOP (ADDITIVE, 6 STEPS)", jets + lure[::2], 2, True),
            ("SHOT, MUZZLE, HIT SPARKS (ADDITIVE)", [shot, muzzle] + sparks, 3, True),
            ("LOSS: LIGHT + RING (ADDITIVE) AND DEBRIS (NORMAL), 3 STEPS A FRAME", loss[::2] + debris[::3], 1, True),
            ("IN THE WING SLOT, FIRING, HIT, LOST (1X)", [gif[k] for k in (8, 14, 32, 46, 50)], 1, False)]
    save_review("bulwark", "bulwark", "BULWARK - FINAL (ROUND 48 G4 = A)", rows, gif)


# =========================================================================== hydro-kit

import stormhawk  # noqa: E402
import rook  # noqa: E402


def _kit_parts(q, pr, v_ship, KIT, HAZ, LAMPM, NAVR, NAVG):
    """The sealed refit's parts (europa_r48.kit_model, variant a) for airframe ``v_ship``: the
    Stormhawk's (``a``) at the concept's places, Rook's ship-C airframe (``c``) moved onto its
    engines, wing roots, intakes, canards and wingtips."""
    from render.sdf import sd_box, sd_capsule, sd_cylinder_y, sd_ellipsoid, subtract
    nav = np.where(pr[:, 0] > 0, NAVG, NAVR).astype(np.int32)
    if v_ship == "a":
        eng, r_out, r_in, sad, seal, lamp, tip = (0.2, -0.9, -0.01), 0.115, 0.078, (0.42, -0.36, 0.02, 0.05), \
            (0.17, 0.05, 0.075), (0.13, 0.66, 0.05), (0.95, 0.36, 0.0)
    else:
        eng, r_out, r_in, sad, seal, lamp, tip = (0.15, -0.72, 0.0), 0.105, 0.07, (0.31, -0.36, -0.04, 0.045), \
            (0.13, 0.04, 0.085), (0.12, 0.58, 0.045), (0.93, -0.62, 0.0)
    shroud = subtract((sd_cylinder_y(q, eng, r_out, 0.09), KIT), sd_cylinder_y(q, eng, r_in, 0.2))
    return [shroud,
            (sd_capsule(q, (sad[0], sad[1], sad[3]), (sad[0], sad[2], sad[3]), 0.055, 0.05), HAZ),
            (sd_box(q, seal, (0.035, 0.2, 0.02), 0.01), KIT),
            (sd_ellipsoid(q, lamp, (0.03, 0.04, 0.03)), LAMPM),
            (sd_ellipsoid(q, tip, (0.022, 0.03, 0.022)), nav)], sad


def kit_model(v_ship, bank):
    from render import models
    from render.sdf import Material, mirror_x, rotate_y, union
    if v_ship == "a":
        base, mats = models.ship_a_model(bank=bank, palette=B.ship_colors())
    else:
        base, mats = models.ship_c_model(bank=bank, palette=rook.EMBER)
    n0 = len(mats)
    KIT, HAZ, LAMPM, NAVR, NAVG = range(n0, n0 + 5)
    mats = mats + [Material((0.17, 0.19, 0.23), metal=0.45, shininess=30, spec=0.4),
                   Material((0.95, 0.62, 0.12), metal=0.25, shininess=40, spec=0.5),
                   Material((0.1, 0.1, 0.1), emission=(2.4, 2.25, 1.8)),
                   Material((0.1, 0.05, 0.05), emission=(2.2, 0.25, 0.2)),
                   Material((0.05, 0.1, 0.05), emission=(0.25, 2.0, 0.7))]

    def scene(p):
        d, m = base(p)
        pr = rotate_y(p, bank)
        q = mirror_x(pr)
        parts, sad = _kit_parts(q, pr, v_ship, KIT, HAZ, LAMPM, NAVR, NAVG)
        d, m = union((d, m), *parts, k=0.008)
        band = (np.abs(q[:, 1] - (sad[1] + sad[2]) / 2 - 0.02) < 0.03) & (np.abs(q[:, 0] - sad[0]) < 0.08)
        m = np.where(band & (m == HAZ), KIT, m)
        return d, m
    return scene, mats


def _hydro_job(job):
    v_ship, bank = job
    scene, mats = kit_model(v_ship, np.radians(bank))
    size = stormhawk.SIZE if v_ship == "a" else rook.SIZE
    hi, factor = artkit.render_hi(artkit.perspective(scene, stormhawk.CAMERA), mats, (size, size), stormhawk.EXTENT)
    return artkit.native(hi, factor)


def hydro_frames():
    jobs = [("a", b) for b in stormhawk.BANKS] + [("c", b) for b in rook.BANKS]
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(_hydro_job, jobs))
    n = len(stormhawk.BANKS)
    return {"ship-hydro": artkit.quantize_set(res[:n], stormhawk.SHIP_COLOURS),
            "rook-hydro": artkit.quantize_set(res[n:], rook.HULL_COLOURS)}


def hydro_review():
    import json
    ship, kit = artkit.load_frames("ship"), load("ship-hydro")
    rook_, rkit = artkit.load_frames("rook"), load("rook-hydro")
    pods = json.loads((artkit.PIVOTS / "pods.json").read_text(encoding="utf-8"))

    def with_pod(hull, bank, pod):
        img = hull.copy()
        for side in ("left", "right"):
            name = f"pod-{pod}-{side}"
            img.alpha_composite(artkit.load_frames(name)[bank], tuple(pods["offsets"][name][bank]))
        return img
    W, H = 200, 200
    base = water_plate(W, H, seed=31)
    seq = [2, 1, 0, 0, 1, 2, 3, 4, 4, 3]
    gif = []
    for i in range(40):
        b = seq[(i // 2) % len(seq)]
        f = base.copy()
        hull = with_pod(kit[b], b, "torpedo") if i >= 20 else kit[b]
        put(f, sprite.shadow_of(hull, opacity=0.45, blur=1.2, scale=0.85), 100 + 14, 90 + 20)
        put(f, hull, 100, 90)
        put(f, sprite.shadow_of(rkit[b], opacity=0.45, blur=1.2, scale=0.85), 152 + 14, 130 + 20)
        put(f, rkit[b], 152, 130)
        gif.append(f)
    rows = [("STORMHAWK STOCK (SHIP_0..4)", ship, 3, False),
            ("STORMHAWK HYDRO-KIT (SHIP-HYDRO_0..4)", kit, 3, False),
            ("HYDRO-KIT WITH THE MICRO-MISSILE AND TORPEDO PODS AT THEIR PIVOTS",
             [with_pod(kit[b], b, p) for p in ("micro-missile", "torpedo") for b in (0, 2, 4)], 3, False),
            ("ROOK STOCK (ROOK_0..4)", rook_, 3, False),
            ("ROOK HYDRO-KIT (ROOK-HYDRO_0..4)", rkit, 3, False),
            ("UNDER WATER (1X, NO GRADE: THE GRADE AND THE PUMP WAKE ARE PART C)", [gif[k] for k in (0, 6, 14, 24)], 1, False)]
    save_review("hydro", "hydro-kit", "HYDRO-KIT - FINAL (ROUND 48 E10 = A), STORMHAWK AND ROOK", rows, gif, fps=10)


# =========================================================================== main

PARTS = {"harpoon": ("harpoon-torpedoes", harpoon_frames, harpoon_review),
         "charges": ("depth-charges", charge_frames, charges_review),
         "sonar": ("sonar-pulse", sonar_frames, sonar_review),
         "flares": ("decoy-flares", flare_frames, flares_review),
         "bulwark": ("bulwark", bulwark_frames, bulwark_review),
         "hydro": (None, hydro_frames, hydro_review)}


def main(args):
    names = [a for a in args if a in PARTS] or list(PARTS)
    for name in names:
        root, frames, review = PARTS[name]
        if "--review" not in args:
            sets = frames()
            if root is None:          # ship-* and rook-* are always claimed (SpriteUse.GAME_WIDE)
                for k, v in sets.items():
                    artkit.write_frames(k, v, SOURCE)
                print(f"hydro-kit: {sum(len(v) for v in sets.values())} frames -> assets/sprites")
            else:
                write(root, sets)
        review()


if __name__ == "__main__":
    main(sys.argv[1:])
