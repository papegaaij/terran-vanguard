#!/usr/bin/env python3
"""Production art: the Dust Kite, Act 3's Martian popcorn glider (design/enemies/air/dust-kite; M7 part B
batch, concept round 36, U.7 = b "crescent sail"). Straight to production from its chosen concept: the
model ``kite_b`` of tools/concept/enemies_r36.py (imported unchanged: a broad swept rust membrane on
bone finger struts, a slim body whose plate gaps glow teal, three teal spine nodes, a forked tail), at
the concept's scale and render factor, with the concept's light rim (an ochre lit edge toward the key
light, a dark rust outline elsewhere) so the 24 px glider reads over the rust ground.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the slug):
  dust-kite_0..63        24x24, `16 angles`: 16 headings x 4 membrane-flex frames, indexed heading * 4
                         + frame (EnemyLooks.frame at 10 fps); heading k glides k x 22.5 degrees
                         clockwise from straight down (4 left, 8 up, 12 right). Every heading its own
                         render under the fixed key light; one 24-colour palette over the set. The
                         flex frames: 0 flat, 1 membrane up, 2 flat, 3 membrane down (a loop)
  dust-kite-death_0..7   32x32, additive: the teal spark (a white-teal pin flash, three short teal
                         streaks, a few motes), 2 steps a frame, after the tiny unit's glow delay
  dust-kite-tatters_0..11 40x40, solid: the puff of rust dust (lit, ragged, 4 translucency steps),
                         ochre and rust motes and two membrane scraps tumbling off, 2 steps a frame
                         (the game's tiny-unit rate: 0.4 s)
  design/enemies/air/concept/dust-kite-final-r38-a.png/.gif   review sheet and a play-field GIF
                         (480x540 at 1x: a stream, a V-wing and a swarm over the still L15 ground stand-in)

The 0.3 s entry fade (blended into the dust, fading to full contrast) is drawn by the game (alpha by
on-screen age); the review GIF shows it with the concept's kite_fade.

Run: python3 tools/art/dust_kite.py [--review]   (~1 min; --review only rebuilds the review files)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image, ImageDraw

import artkit
from artkit import DESIGN, TAU, sprite

import burrower  # noqa: E402  (tools/art: held sprites, the Mars ground stand-in, Relief)
import vfx_r08 as v8  # noqa: E402  (concept script, imported unchanged)
import vrell_deaths as vd  # noqa: E402  (tools/art: the motes)
import vrell_fx as fx  # noqa: E402  (tools/art: easing, timing)
import enemies_r03 as e3  # noqa: E402  (concept script: place() with the air shadow)
import enemies_r36 as u36  # noqa: E402  (concept round 36: the chosen kite_b, imported unchanged)
from render import enemy_models as em  # noqa: E402
from render import raster  # noqa: E402
from render.enemy_rigs import model_rotation, model_space_materials, path_heading  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "dust_kite.py"
BATCH = "M7 part B batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
REVIEW_ROUND = "r38"
SLUG = "dust-kite"
CONCEPT = DESIGN / "enemies" / "air" / "concept"
STEP = 60

SIZE = (24, 24)
EXTENT = 2.3                                    # the concept's (ModelSpaceAngleSprites default)
HEADINGS, FLEX = 16, 4
FPS = 10                                        # EnemyLooks.ORGANIC_FPS
COLOURS = 24
DEATH_SIZE, DEATH_FRAMES = (32, 32), 8
TATTER_SIZE, TATTER_FRAMES = (40, 40), 12
TINY_STEPS, TINY_GLOW_DELAY = 2, 4              # EnemyLooks.TINY_DEATH_FRAME_TICKS, TINY_GLOW_DELAY_TICKS

TEAL = np.array(em.hx(em.GLOWS["teal"])) * 255
WHITE_TEAL = (225, 255, 240)
DUST_RGB = np.array([176, 98, 60], float)
LIGHT = np.array([-0.5, -0.6, 0.62]) / np.linalg.norm([-0.5, -0.6, 0.62])


# =========================================================================== the kite

def rotation(k):
    """Model rotation for game heading k (0 = down the screen, clockwise as seen on the screen)."""
    return model_rotation(np.pi / 2 + TAU * k / HEADINGS)


def model(k, flex=0, glow=1.0):
    scene0, mats = u36.kite_b(flex, glow)
    rot = rotation(k)
    return (lambda p: scene0(rotate_z(p, rot))), model_space_materials(mats, rot)


def rim(img):
    """The concept's kite_rim without its per-frame palette (the set gets one palette)."""
    a = np.array(img)
    op = a[..., 3] > 0
    pad = np.pad(op, 1)
    left, up = ~pad[1:-1, :-2], ~pad[:-2, 1:-1]
    right, down = ~pad[1:-1, 2:], ~pad[2:, 1:-1]
    out = a.astype(np.float64)
    for mask, col, k in (((left | up) & op, u36.KITE_LIT, 0.4),
                         ((right | down) & op & ~(left | up), u36.KITE_DARK, 0.7)):
        out[mask, :3] = out[mask, :3] * (1 - k) + np.array(col) * k
    return Image.fromarray(out.astype(np.uint8), "RGBA")


def render(job):
    k, j = job
    return rim(artkit.native(*artkit.render_hi(*model(k, j), SIZE, EXTENT)))


# =========================================================================== death

def death_glow(i):
    """The teal spark: a white-teal pin flash, three short teal streaks flying out, a few motes."""
    w, h = DEATH_SIZE
    t = fx.t_of(i, DEATH_FRAMES)
    c = w / 2
    cv = v8.Canvas(w, h)
    d = cv.dist(c, c)
    fade = np.clip((w / 2 - d) / 3.0, 0, 1)
    cv.add(WHITE_TEAL, v8.gauss(d, 1.4 + 1.5 * t) * 1.3 * np.clip(1 - t * 3.0, 0, 1))
    cv.add(tuple(TEAL), v8.gauss(d, 3.5 + 3 * t) * 0.5 * (1 - t) ** 2 * fade)
    rng = np.random.default_rng(3801)
    for j in range(3):
        a = rng.uniform(0, TAU) + j * TAU / 3
        r0, r1 = 2 + 9 * fx.ease_out(t), 5 + 12 * fx.ease_out(t)
        seg = cv.seg(c + np.cos(a) * r0, c + np.sin(a) * r0, c + np.cos(a) * r1, c + np.sin(a) * r1)
        cv.add(tuple(TEAL), v8.gauss(seg, 0.55) * 1.2 * np.clip(1 - t * 1.6, 0, 1) * fade)
    vd.motes(cv, np.random.default_rng(3802), c, t, 6, (6, 13), (0.45, 0.8), tuple(TEAL), WHITE_TEAL, fade,
             delay=0.1, gain=1.0)
    return artkit.additive(cv.image())


def death_tatters(i):
    """The rust dust puff: lit ragged blobs swelling and thinning (4 translucency steps), dust motes
    and two membrane scraps tumbling off."""
    w, h = TATTER_SIZE
    S = 4
    t = (i + 0.5) / TATTER_FRAMES
    rng = np.random.default_rng(3811)
    yy, xx = np.mgrid[0:h * S, 0:w * S]
    noise = raster.fbm(w * S, h * S, 6 * S, 3812, octaves=3, period=False)
    rgb = np.zeros((h * S, w * S, 3))
    alpha = np.zeros((h * S, w * S))
    for k in range(5):
        ox, oy = rng.normal(0, 1.6, 2)
        r = (3.5 + 7.5 * t ** 0.6 + rng.uniform(0, 2)) * S
        cx = (w / 2 + ox * (1 + 2 * t)) * S
        cy = (h / 2 + oy * (1 + 2 * t)) * S
        nx, ny = (xx - cx) / r, (yy - cy) / (r * 0.85)
        rr = nx ** 2 + ny ** 2
        edge = 1 - rr + (noise - 0.5) * 1.2
        nz = np.sqrt(np.clip(1 - rr, 0, 1))
        lit = np.clip(nx * LIGHT[0] + ny * LIGHT[1] + nz * LIGHT[2], 0, 1)
        a = np.where(edge > 0, np.clip(edge * 1.6, 0, 1), 0) * 0.7 * (1 - t) ** 1.2
        col = (DUST_RGB + rng.uniform(-12, 12)) * (0.55 + 0.7 * lit[..., None])
        rgb = rgb * (1 - a[..., None]) + col * a[..., None]
        alpha = alpha + a * (1 - alpha)
    q = artkit.ordered_dither(np.where(alpha < 0.12, 0, alpha), 4)
    img = Image.fromarray(np.dstack([np.clip(rgb, 0, 255), q * 255]).astype(np.uint8), "RGBA")
    img = img.resize((w, h), Image.BOX)
    a = np.array(img)
    a[..., 3] = np.where(a[..., 3] >= 32, (np.round(a[..., 3] / 255 * 3) / 3 * 255), 0).astype(np.uint8)
    a[a[..., 3] == 0] = 0
    img = Image.fromarray(a, "RGBA")
    d = ImageDraw.Draw(img)
    T = t * 0.4
    for k in range(10):                                          # dust motes
        ang = rng.uniform(0, TAU)
        dist = rng.uniform(25, 60) * (1 - np.exp(-T * 5)) / 5
        mx, my = w / 2 + np.cos(ang) * dist, h / 2 + np.sin(ang) * dist + 6 * T
        if t < 0.85:
            col = (214, 160, 100) if k % 3 == 0 else (150, 72, 40)
            d.point((mx, my), fill=col + (255,))
    for k in range(2):                                           # membrane scraps tumbling off
        ang = rng.uniform(0, TAU)
        dist = 3 + 13 * fx.ease_out(t, 2.0)
        cx, cy = w / 2 + np.cos(ang) * dist, h / 2 + np.sin(ang) * dist + 4 * t
        r, rot = 2.4 * (1 - 0.4 * t), ang + t * 9
        pts = [(cx + np.cos(rot + j * 2.2) * r, cy + np.sin(rot + j * 2.2) * r * 0.6) for j in range(3)]
        if t < 0.9:
            d.polygon(pts, fill=(120, 44, 26, 255))
            d.point(pts[0], fill=(206, 150, 96, 255))
    return img


def _job(job):
    kind, i = job
    if kind == "kite":
        return render((i // FLEX, i % FLEX))
    if kind == "death":
        return death_glow(i)
    return death_tatters(i)


def build():
    jobs = ([("kite", i) for i in range(HEADINGS * FLEX)] + [("death", i) for i in range(DEATH_FRAMES)]
            + [("tatters", i) for i in range(TATTER_FRAMES)])
    with ProcessPoolExecutor() as pool:
        out = list(pool.map(_job, jobs))
    n = HEADINGS * FLEX
    sets = [(SLUG, artkit.quantize_set(out[:n], COLOURS)),
            (f"{SLUG}-death", artkit.quantize_set(out[n:n + DEATH_FRAMES], 16)),
            (f"{SLUG}-tatters", artkit.quantize_set(out[n + DEATH_FRAMES:], 24))]
    with burrower.sprites_for(SLUG):
        for name, frames in sets:
            artkit.write_frames(name, frames, SOURCE)
            print(f"{name}: {len(frames)} frames, {frames[0].width}x{frames[0].height}")
    print(f"written to {burrower.where(SLUG)}")


# =========================================================================== review

def load(name):
    with burrower.sprites_for(SLUG):
        return artkit.load_frames(name)


def cell(img, kind="dunes", size=(40, 40), glow=False, xy=(200, 200), shadow=False):
    c = burrower.mars_plate(480, 540, 151, kind).crop((xy[0], xy[1], xy[0] + size[0], xy[1] + size[1])).copy()
    if shadow:
        sh = sprite.shadow_of(img)
        c.alpha_composite(sh, ((size[0] - img.width) // 2 + 4, (size[1] - img.height) // 2 + 6))
    if glow:
        return artkit.add_light(c, img, ((size[0] - img.width) // 2, (size[1] - img.height) // 2))
    burrower.paste_into(c, img, size[0] / 2, size[1] / 2)
    return c


def review():
    artkit.REVIEW_ROUND = REVIEW_ROUND
    kite = load(SLUG)
    death, tatters = load(f"{SLUG}-death"), load(f"{SLUG}-tatters")
    tiny = artkit.load_frames("explosion-tiny")
    together = []
    for age in range(0, TATTER_FRAMES * TINY_STEPS + 6, 2):
        c = cell(Image.new("RGBA", (1, 1)), "dunes", TATTER_SIZE)
        for frames, steps, glow, delay in ((tatters, TINY_STEPS, False, 0), (tiny, TINY_STEPS, True, 0),
                                           (death, TINY_STEPS, True, TINY_GLOW_DELAY)):
            fr = fx.timed(frames, steps, age - delay)
            if fr is not None:
                xy = ((TATTER_SIZE[0] - fr.width) // 2, (TATTER_SIZE[1] - fr.height) // 2)
                c = artkit.add_light(c, fr, xy) if glow else (c.alpha_composite(fr, xy) or c)
        together.append(c)
    rows = [
        ("16 HEADINGS (FLEX FRAME 0), 0 = DOWN, CLOCKWISE", [kite[k * FLEX] for k in range(HEADINGS)], 3, False),
        ("MEMBRANE FLEX, 4 FRAMES (10 FPS): HEADING 0 AND HEADING 3", kite[0:4] + kite[12:16], 4, False),
        ("ON THE L15 GROUND STAND-IN WITH THE AIR SHADOW: DUNES, HEADINGS 0-7",
         [cell(kite[k * FLEX + k % 4], "dunes", shadow=True, xy=(60 + 20 * k, 300)) for k in range(8)], 3, False),
        ("ON THE BASALT REGOLITH OF THE SLOPES, HEADINGS 8-15",
         [cell(kite[k * FLEX + k % 4], "slopes", shadow=True, xy=(60 + 20 * k, 300)) for k in range(8, 16)], 3, False),
        ("ALL 64 FRAMES, INDEX = HEADING * 4 + FLEX", kite[:32], 2, False),
        ("(CONT.)", kite[32:], 2, False),
        ("DEATH: TEAL SPARK (ADDITIVE, 2 STEPS A FRAME, 4 STEPS LATE)", death, 4, True),
        ("DEATH: RUST DUST PUFF AND SCRAPS (SOLID, 2 STEPS A FRAME)", tatters, 3, False),
        ("TOGETHER ON THE DUNES WITH EXPLOSION-TINY, EVERY 2ND STEP", together, 3, False),
    ]
    sheet = artkit.review_sheet("DUST KITE - FINAL SPRITES (CRESCENT SAIL)", rows, width=1500, batch=BATCH)
    artkit.save_review(sheet, play_field(kite, death, tatters, tiny), CONCEPT, SLUG, fps=20)


def play_field(kite, death, tatters, tiny):
    """The concept's scene (stream, V-wing of 7, swarm; the swoop across the ship's lane; the
    concept's ship path and kills) drawn with the production frames over the L15 ground stand-in
    (still, as the concept's: a scrolling ground would not fit the GIF in 8 MB)."""
    fw, fh, scroll, dur, fps = 480, 540, 0.0, u36.DK_DUR, 20
    plate = burrower.mars_plate(fw, fh + int(scroll * dur) + 8, 159, "dunes")
    tracks = u36._kite_tracks()
    ends = u36.kite_events(tracks, dur)
    ship = artkit.load_frames("ship")[2]
    out = []
    for f in range(int(dur * fps)):
        t = f / fps
        top = plate.height - fh - int(scroll * t)
        img = plate.crop((0, top, fw, top + fh)).copy()
        px, py = u36.dk_player(t)
        glows = []
        for i, tr in enumerate(tracks):
            if t < tr.t0:
                continue
            end = ends.get(i)
            if end and t >= end[0]:
                x, y = tr(end[0])
                age = int((t - end[0]) * STEP)
                fr = fx.timed(tatters, TINY_STEPS, age)
                if fr is not None:
                    burrower.paste_into(img, fr, x, y)
                for frames, delay in ((tiny, 0), (death, TINY_GLOW_DELAY)):
                    g = fx.timed(frames, TINY_STEPS, age - delay)
                    if g is not None:
                        glows.append((g, x, y))
                continue
            x, y = tr(t)
            if not (-30 < x < fw + 30 and -30 < y < fh + 30):
                continue
            hd = path_heading(tr, t)
            k = int(round(((hd - np.pi / 2) % TAU) / (TAU / HEADINGS))) % HEADINGS
            sp = kite[k * FLEX + int(t * FPS + i) % FLEX]
            u = (t - tr.t0 - 14 / u36.DK_GLIDE) / u36.DK_FADE if y > -12 else 0.0
            if end is not None and end[1] == "shot" and end[0] - t < 0.1:
                sp = u36.flash(sp, True)
            e3.place(img, u36.kite_fade(sp, max(0.0, u)), x, y, "air")
        for g, x, y in glows:
            img = artkit.add_light(img, g, (int(round(x - g.width / 2)), int(round(y - g.height / 2))))
        e3.place(img, ship, px, py, "air")
        for k in range(4):
            yb = py - 40 - ((t * 420 + k * 105) % 420)
            if yb > -10:
                e3.bolt(img, px - 10, yb)
                e3.bolt(img, px + 10, yb)
        raster.draw_text(img, 8, fh - 14, f"DUST KITE FINAL R38 - {t:4.2f} S", raster.LABEL)
        out.append(img)
    return out


def intel_model():
    """(scene, mats) for the hangar intel portrait: nose down (heading 0), membrane up (flex 1)."""
    return model(0, 1)


if __name__ == "__main__":
    artkit.REVIEW_ROUND = REVIEW_ROUND
    if "--review" not in sys.argv[1:]:
        build()
    review()
