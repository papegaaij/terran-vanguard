#!/usr/bin/env python3
"""Production art: the Abyss Ray, Act 4's large set-piece manta (design/enemies/naval/abyss-ray; M8 part D batch,
concept round 53, step ART-D). From the chosen round-48 look **b** "bone skate" (tools/concept/enemies_r48.py
``abyss_ray`` b, role colours ``abyss-ray-b`` = bone / violet / lime, imported unchanged): a rounded diamond disc in
bone plates, a ridge of bone tubercles down the back, violet eye spots and mouth, five lime gill slits on each
leading edge (the README's weak point, drawn only), a long whip tail with a spine.

Headings as tools/art/README.md: heading k turns k x 22.5 degrees clockwise from straight down (`16 angles`, the
glide direction), each its own render under the fixed key light. Size from the stat block: wingspan 200x150 on a
208 px canvas (hitbox 130x90), the concept's scale. The wing beat is the model's ``flap`` (wing tips bent up/down)
seen through a mild perspective camera (artkit.perspective, 3.2 model units up) so the raised wings read larger.

Outputs (assets/sprites/, or build/art-held/sprites/ while no data claims the root: SpriteUse claims ``abyss-ray``
once design/enemies/naval/abyss-ray/data.yaml exists, first_level 24):
  abyss-ray_0..47         208x208, normal: heading x 3 + beat: 0 wings level, 1 wings up, 2 wings down; the beat
                          plays 0, 1, 0, 2 over 2.2 s (a slow wing beat, ~1.8 fps)
  abyss-ray-glow_0..15    208x208, additive: per heading the gill slits' lime glow and the violet eye spots and
                          mouth (the model's emission alone with a soft stepped halo), drawn ungraded over every
                          beat frame (the gills sit on the leading edge, which the beat does not move in the top view)
                          and alone in the dark levels (L26)
  abyss-ray-shadow_0..7   208x208, normal with dithered translucency: the tell, the dark blurred shape growing under
                          the ship's lane over the 1.5 s on `sub` (frame i at i / 7 of the 1.5 s), heading 0 (it rises
                          facing the ship); after it the game keeps frame 7 under the rise, fading it out with the rise
  abyss-ray-rise_0..7     208x208, normal, stepped translucency: the rise's 0.9 s at heading 0, from the depth (the
                          body tinted to the water, blurred, 88 % size, faint) to the play plane (crisp, full size);
                          the layer flips to `air` at frame 4; after frame 7 the beat frames take over
  abyss-ray-sink_0..15    208x208, likewise reversed for the sink (0.9 s): 2 sides x 8 steps, side 0 = heading 4
                          (gliding left), side 1 = heading 12 (gliding right); the game picks the side of the glide
  abyss-ray-mouth_0..3    48x48, additive: the fan's violet mouth flash (2 steps a frame from each fan), centred on
                          assets/pivots/abyss-ray.json ``mouth`` (per heading: the fan's origin)
  abyss-ray-death_0..23   208x208, normal, stepped translucency: the `large` break-up, 3 headings (0, 4, 12: the rise
                          and the two glides; the game uses the nearest) x 8 steps (8 fps): the wings fold up over the
                          back, the glow dies, the body darkens toward the water and sinks (shrinks, fades; the
                          last step is empty: gone into the dark under the bloom)
  abyss-ray-bloom_0..9    160x160, additive: the bioluminescent bloom over the break-up (lime and violet: a soft
                          flash, then a ring of motes drifting out and fading; 10 fps), centred on the body
  assets/pivots/abyss-ray.json   per heading the mouth (the fan's origin) and the two gill rows' centres (the weak
                          point, drawn only) in the frame's px; the frame size; the beat order
  design/enemies/naval/concept/abyss-ray-final-r53-a.png/.gif   the review sheet and the GIF of a whole visit on Level
                          24's plain under the review grade (sections 2 and 3): the shadow growing ahead of the ship,
                          the rise, four Barbfins peeling off, three 9-way fans 1.2 s apart, the glide to the far side
                          and the sink; a second pass shows the death

Run: python3 tools/art/abyss_ray.py [--review | fx]   (``fx``: only the shadow, mouth and bloom, step DITH8; ~4 min on 20 cores; --review only rebuilds the review files; run
     barbfin.py and enemy_bullets.py violet first, the review shows the escorts and their needles)
"""
import sys
from concurrent.futures import ProcessPoolExecutor
from dataclasses import replace

import numpy as np
from PIL import Image, ImageFilter

import artkit
import l24kit as kit
from artkit import DESIGN, TAU

import enemies_r48 as e48  # noqa: E402  (concept script, unchanged: the chosen look b's model)
from render import raster  # noqa: E402
from render.enemy_rigs import local_to_screen, model_rotation, model_space_materials  # noqa: E402
from render.sdf import rotate_z  # noqa: E402

SCRIPT = "abyss_ray.py"
BATCH = "M8 part D batch"
SOURCE = artkit.source_note(SCRIPT, BATCH)
ROUND = "r53"
artkit.REVIEW_ROUND = ROUND
CONCEPT = DESIGN / "enemies" / "naval" / "concept"
HEADINGS = 16
SIZE, EXTENT = 208, 2.3
SCALE = SIZE / EXTENT
CAMERA = 3.2
BEATS = (0.0, 3.0, -3.0)                 # the model's flap: level, up, down
MOUTH = (0.0, 0.66)                      # model units (+Y forward): the violet mouth
GILLS = (0.52, 0.36)                     # the middle of the five gill slits on the right leading edge
DEATH_HEADINGS = (0, 4, 12)
SINK_HEADINGS = (4, 12)
MOUTH_W, BLOOM_W = 48, 160
DEEP = np.array((4, 30, 44), float)
VIOLET = np.array(e48.VIOLET, float)
LIME = np.array(e48.LIME, float)


def model(h, flap=0.0, glow=1.0, fold=0.0, black=False):
    """(scene, mats) of look b at heading index h (float ok), seen through the perspective camera. ``fold`` 0..1
    folds the wings up over the back (the death): the span narrows and the wing tips rise."""
    scene0, mats = e48.abyss_ray("b", flap + 6.0 * fold, glow)
    if black:
        mats = [replace(m, albedo=(0.0, 0.0, 0.0), spec=0.0, metal=0.0, pattern=None) for m in mats]
    sx = 1.0 - 0.5 * fold

    def folded(p):
        q = p.copy()
        q[:, 0] = q[:, 0] / sx
        q[:, 2] = q[:, 2] - 0.18 * fold * np.abs(q[:, 0])
        d, m = scene0(q)
        return d * sx, m
    rot = model_rotation(np.pi / 2 + TAU * h / HEADINGS)
    scene = (lambda p: folded(rotate_z(p, rot))) if fold > 0 else (lambda p: scene0(rotate_z(p, rot)))
    return artkit.perspective(scene, CAMERA), model_space_materials(mats, rot)


def _render(job):
    h, flap, glow, fold, black = job
    scene, mats = model(h, flap, glow, fold, black)
    hi, factor = artkit.render_hi(scene, mats, (SIZE, SIZE), EXTENT)
    if black:
        img = artkit.native(hi, factor, crisp=0)
    else:
        img = artkit.native(hi, factor, crisp=55)
    return img


def pivot(lx, ly, k):
    dx, dy = local_to_screen(lx, ly, np.pi / 2 + TAU * k / HEADINGS, SCALE)
    return [round(SIZE / 2 + dx, 1), round(SIZE / 2 + dy, 1)]


# --------------------------------------------------------------------------- derived frames

def glow_frame(black):
    """The emission-only render -> an additive sprite: the slits crisp, a soft halo stepped to a few levels."""
    a = np.array(black).astype(np.float64)
    rgb = a[..., :3] * (a[..., 3:4] / 255)
    halo = np.stack([np.array(Image.fromarray(rgb[..., c].astype(np.uint8)).filter(ImageFilter.GaussianBlur(3.5)),
                              float) for c in range(3)], -1)
    lum = halo.max(-1, keepdims=True) / 255
    step = artkit.ordered_dither(np.clip(lum[..., 0] * 2.2, 0, 1), 4)[..., None]
    halo = np.where(lum > 1e-3, halo / np.maximum(lum, 1e-3) * step * 0.55, 0)
    out = np.maximum(rgb, halo)
    alpha = np.where(out.max(-1) >= 1, 255, 0)
    return Image.fromarray(np.dstack([np.clip(out, 0, 255), alpha]).astype(np.uint8), "RGBA")


def depth_look(img, depth, k=0.9):
    """A body under the play plane: tinted to the deep water, darker, softened, faded (depth 0..1)."""
    a = np.array(img).astype(np.float64)
    tint = 0.2 + 0.6 * depth
    a[..., :3] = (a[..., :3] * (1 - tint) + DEEP * tint) * (k * (1 - 0.6 * depth) + 0.1)
    im = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    if depth > 0.05:
        im = im.filter(ImageFilter.GaussianBlur(0.4 + 1.6 * depth))
    sc = 1 - 0.12 * depth
    n = int(round(SIZE * sc))
    small = im.resize((n, n), Image.Resampling.LANCZOS)
    cell = Image.new("RGBA", (SIZE, SIZE))
    cell.alpha_composite(small, ((SIZE - n) // 2, (SIZE - n) // 2))
    return fade_stepped(cell, 1 - 0.6 * depth)


def fade_stepped(img, opacity):
    """Opacity stepped to the 90s translucency levels (1, 2/3, 1/3) on a 1-bit silhouette."""
    a = np.array(img)
    inside = a[..., 3] >= 128
    level = 255 if opacity > 0.83 else 170 if opacity > 0.5 else 85 if opacity > 0.17 else 0
    a[..., 3] = np.where(inside, level, 0)
    a[a[..., 3] == 0] = 0
    return Image.fromarray(a, "RGBA")


def shadow_frame(body, i):
    u = i / 7
    s = u * u * (3 - 2 * u)
    scale, blur, opacity = 0.55 + 0.37 * s, 9 - 6 * s, 0.25 + 0.6 * s
    al = body.getchannel("A")
    n = int(round(SIZE * scale))
    al = al.resize((n, n), Image.Resampling.BILINEAR)
    cell = Image.new("L", (SIZE, SIZE))
    cell.paste(al, ((SIZE - n) // 2, (SIZE - n) // 2 + 6))
    cell = cell.filter(ImageFilter.GaussianBlur(blur))
    dens = np.array(cell, float) / 255 * opacity
    alpha = artkit.smooth_steps(dens)
    out = np.zeros((SIZE, SIZE, 4), np.uint8)
    out[..., 0], out[..., 1], out[..., 2] = 0, 6, 10
    out[..., 3] = (alpha * 255).astype(np.uint8)
    out[out[..., 3] == 0] = 0
    return Image.fromarray(out, "RGBA")


def death_frame(img, i):
    """The folded render of step i dimmed, glow gone, sinking in the last steps."""
    u = i / 7
    a = np.array(img).astype(np.float64)
    sink = max(0.0, (u - 0.35) / 0.65)
    tint = 0.15 + 0.6 * sink
    a[..., :3] = (a[..., :3] * (1 - tint) + DEEP * tint) * (1 - 0.45 * u)
    im = Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")
    sc = 1 - 0.2 * sink
    n = int(round(SIZE * sc))
    small = im.resize((n, n), Image.Resampling.LANCZOS)
    cell = Image.new("RGBA", (SIZE, SIZE))
    cell.alpha_composite(small, ((SIZE - n) // 2, (SIZE - n) // 2))
    return fade_stepped(cell, 1 - 0.85 * sink)


def _light(w, field, col, levels=8):
    q = artkit.smooth_steps(field)
    rgb = col * q[..., None] if col.ndim == 3 else np.asarray(col, float)[None, None] * q[..., None]
    return artkit.additive(Image.fromarray(np.dstack([np.clip(rgb, 0, 255), np.full((w, w), 255.0)]).astype(np.uint8)))


def mouth_frame(i):
    w = MOUTH_W
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    r = np.hypot(xx - w / 2, yy - w / 2)
    s = [1.0, 1.3, 1.1, 0.8][i]
    f = (np.exp(-(r / (3.0 * s)) ** 2) * 1.1 + np.exp(-(r / (9.0 * s)) ** 2) * 0.55) * [1.0, 0.9, 0.6, 0.3][i]
    core = np.exp(-(r / (2.2 * s)) ** 2)[..., None]
    col = VIOLET[None, None] * (1 - core) + np.array((235, 215, 255))[None, None] * core
    return _light(w, f, col)


MOTES = [(a, 0.6 + 0.4 * ((k * 37) % 11) / 10, k % 3) for k, a in enumerate(np.linspace(0, TAU, 14, endpoint=False))]


def bloom_frame(i):
    """A soft lime-violet flash (the body's light let go), then a ring of motes drifting out and fading."""
    w = BLOOM_W
    yy, xx = np.mgrid[0:w, 0:w].astype(np.float64) + 0.5
    x, y = xx - w / 2, yy - w / 2
    u = i / 9
    r = np.hypot(x, y * 1.15)
    flash = np.exp(-(r / (14 + 40 * u)) ** 2) * (1 - u) ** 1.6 * 1.2
    field = flash.copy()
    colw = np.zeros((w, w, 3))
    colw += (LIME * 0.55 + VIOLET * 0.45)[None, None] * flash[..., None]
    for a, reach, kind in MOTES:
        d = 18 + 55 * reach * (1 - (1 - u) ** 1.5)
        cx, cy = np.cos(a + 0.3 * u) * d, np.sin(a + 0.3 * u) * d * 0.85 + 6 * u
        m = np.exp(-((x - cx) ** 2 + (y - cy) ** 2) / (2.2 + 1.5 * u) ** 2) * (1 - u) ** 0.9 * min(1.0, i / 2 + 0.2)
        field = np.maximum(field, m)
        colw += (LIME if kind else VIOLET)[None, None] * m[..., None]
    col = colw / np.maximum(field[..., None], 1e-6)
    return _light(w, np.clip(field, 0, 1), np.clip(col, 0, 255))


def intel_model():
    """(scene, mats) for the hangar intel portrait: the ray nose down, scaled to fit the 30 px card, wings level."""
    scene0, mats = e48.abyss_ray("b", 0.0, 1.3)
    rot = model_rotation(np.pi / 2)
    k = 0.92

    def scene(p):
        d, m = scene0(rotate_z(p, rot) / k)
        return d * k, m
    return scene, model_space_materials(mats, rot)


# --------------------------------------------------------------------------- build

def build():
    beat_jobs = [(k, fl, 1.0, 0.0, False) for k in range(HEADINGS) for fl in BEATS]
    glow_jobs = [(k, 0.0, 1.0, 0.0, True) for k in range(HEADINGS)]
    death_jobs = [(k, 0.0, 1.0 - i / 7, min(1.0, i / 5), False) for k in DEATH_HEADINGS for i in range(8)]
    with ProcessPoolExecutor() as ex:
        out = list(ex.map(_render, beat_jobs + glow_jobs + death_jobs))
    beats = out[:len(beat_jobs)]
    glows = out[len(beat_jobs):len(beat_jobs) + len(glow_jobs)]
    deaths = out[len(beat_jobs) + len(glow_jobs):]
    beats = artkit.quantize_set(beats, 40)
    kit.write("abyss-ray", beats, SOURCE)
    kit.write("abyss-ray-glow", artkit.quantize_set([glow_frame(g) for g in glows], 24), SOURCE)
    level0 = beats[0]
    kit.write("abyss-ray-shadow", [shadow_frame(level0, i) for i in range(8)], SOURCE)
    rise = [depth_look(level0, 1 - i / 7) for i in range(8)]
    sink = [depth_look(beats[k * 3], i / 7) for k in SINK_HEADINGS for i in range(8)]
    kit.write("abyss-ray-rise", artkit.quantize_set(rise, 40), SOURCE)
    kit.write("abyss-ray-sink", artkit.quantize_set(sink, 40), SOURCE)
    kit.write("abyss-ray-mouth", [mouth_frame(i) for i in range(4)], SOURCE)
    kit.write("abyss-ray-death", artkit.quantize_set([death_frame(d, i % 8) for i, d in enumerate(deaths)], 32),
              SOURCE)
    kit.write("abyss-ray-bloom", [bloom_frame(i) for i in range(10)], SOURCE)
    artkit.write_pivots("abyss-ray", SOURCE, {
        "frame": [SIZE, SIZE], "headings": HEADINGS, "beat": [0, 1, 0, 2], "beat_seconds": 2.2,
        "mouth": [pivot(*MOUTH, k) for k in range(HEADINGS)],
        "gills": [[pivot(-GILLS[0], GILLS[1], k), pivot(GILLS[0], GILLS[1], k)] for k in range(HEADINGS)],
        "death_headings": list(DEATH_HEADINGS), "sink_headings": list(SINK_HEADINGS)})


# =========================================================================== review

FPS = 20
SHIP = np.array((240.0, 470.0))
SHADOW_T, RISE_T, GLIDE_T, SINK_T = 0.0, 1.5, 2.4, 7.4          # the sim's phases (m8d-gaps §1.2)
FANS = (2.4, 3.6, 4.8)                                          # the first fan at the rise's end, 1.2 s apart
END = 8.6
ENTRY = np.array((240.0, 270.0))                                # the ship's x, ~200 px ahead of it


def ship_at(t):
    return SHIP + np.array((40 * np.sin(t * 0.6), 0.0))


def ray_at(t):
    """(centre, heading index, phase): shadow and rise in place facing down, then the glide to the far side."""
    if t < GLIDE_T:
        return ENTRY + np.array((0.0, 4 * np.sin(t))), 0, "shadow" if t < RISE_T else "rise"
    u = t - GLIDE_T
    turn = min(1.0, u / 1.2)
    k = int(round(16 - 4 * turn)) % 16
    pos = ENTRY + np.array((70 * u * (0.5 + 0.5 * turn), 10 * u))
    return pos, k, "glide" if t < SINK_T else "sink"


def escorts(t0):
    out = []
    for i, ang in enumerate((-2.3, -0.8, 0.8, 2.3)):
        hold = ENTRY + np.array((np.sin(ang) * 160, -np.cos(ang) * 60 + 70 * (abs(ang) > 1)))
        hold[0] = float(np.clip(hold[0], 40, 440))
        out.append((t0 + 0.08 * i, hold, 0.9 + 0.35 * i))
    return out


def review():
    body, glow, shadow, rise, sink, mouth, death, bloom = (kit.load(n) for n in (
        "abyss-ray", "abyss-ray-glow", "abyss-ray-shadow", "abyss-ray-rise", "abyss-ray-sink", "abyss-ray-mouth",
        "abyss-ray-death", "abyss-ray-bloom"))
    bf = kit.load("barbfin")
    try:
        bfg = kit.load("barbfin-glow")
    except FileNotFoundError:
        bfg = None
    needle, orb = artkit.load_frames("needle-violet"), artkit.load_frames("orb")
    mouths = [pivot(*MOUTH, k) for k in range(HEADINGS)]
    half = [f.resize((104, 104), Image.Resampling.BOX) for f in body]
    rows = [("16 HEADINGS, WINGS LEVEL (1/2)", half[0::3], 1, False),
            ("THE BEAT AT HEADINGS 0 AND 4: LEVEL, UP, DOWN (1/2)", half[0:3] + half[12:15], 1, False),
            ("GILL AND SPOT GLOW (ADDITIVE, 1/2), EVERY 2ND HEADING",
             [g.resize((104, 104), Image.Resampling.BOX) for g in glow[0::2]], 1, True),
            ("THE SHADOW GROWING 1.5 S (1/2)", [f.resize((104, 104), Image.Resampling.BOX) for f in shadow], 1, False),
            ("THE RISE 0.9 S (1/2)", [f.resize((104, 104), Image.Resampling.BOX) for f in rise], 1, False),
            ("THE SINK, GLIDING LEFT (1/2)", [f.resize((104, 104), Image.Resampling.BOX) for f in sink[:8]], 1, False),
            ("THE BREAK-UP AT HEADING 0 (1/2)", [f.resize((104, 104), Image.Resampling.BOX) for f in death[:8]], 1,
             False),
            ("MOUTH FLASH AND BLOOM (ADDITIVE, BLOOM 1/2)",
             mouth + [b.resize((80, 80), Image.Resampling.BOX) for b in bloom[0::2]], 1, True)]
    sheet = artkit.review_sheet("ABYSS RAY - FINAL (ROUND 48 LOOK B, BONE SKATE)", rows, width=1000, batch=BATCH)

    def visit(kill=None, section=2, y0=600):
        esc = escorts(RISE_T + 0.45)
        shots = []
        for tf in FANS:
            c, k, _ = ray_at(tf)
            m = c + np.array(mouths[k]) - SIZE / 2
            sp = ship_at(tf)
            aim = np.arctan2(sp[1] - m[1], sp[0] - m[0])
            for j in range(9):
                a = aim + np.radians(110) * (j / 8 - 0.5)
                shots.append((tf, m, np.array((np.cos(a), np.sin(a))) * 110 * 0.7, "orb"))
        for (te, hold, first) in esc:
            ts = te + 1.0 + first
            while ts < END:
                o = hold.copy()
                sp = ship_at(ts)
                d = sp - o
                shots.append((ts, o, d / np.linalg.norm(d) * 150 * 0.7, "needle"))
                ts += 2.4
        frames = []
        for fi in range(int(END * FPS)):
            t = fi / FPS
            img = kit.plate(480, 540, 0, y0 - int(t * 80), section=section)
            glows = []
            c, k, phase = ray_at(t)
            dead = kill is not None and t >= kill
            if dead:
                s = t - kill
                kd = min(DEATH_HEADINGS, key=lambda h: min((h - k) % 16, (k - h) % 16))
                step = int(s * 8)
                if step < 8:
                    kit.paste_c(img, death[DEATH_HEADINGS.index(kd) * 8 + step], c[0], c[1] + 10 * s)
                if int(s * 10) < 10:
                    glows.append((bloom[int(s * 10)], c))
            elif phase == "shadow":
                kit.paste_c(img, shadow[min(7, int(t / RISE_T * 8))], *c)
            elif phase == "rise":
                u = (t - RISE_T) / (GLIDE_T - RISE_T)
                if u < 0.6:
                    kit.paste_c(img, shadow[7], *c)
                kit.paste_c(img, kit.lit(rise[min(7, int(u * 8))], section), *c)
            elif phase == "glide":
                beat = (0, 1, 0, 2)[int((t - GLIDE_T) / 2.2 * 4) % 4]
                kit.paste_c(img, kit.lit(body[k * 3 + beat], section), *c)
                glows.append((glow[k], c))
                for tf in FANS:
                    if 0 <= t - tf < 0.2:
                        glows.append((mouth[min(3, int((t - tf) * 20))], c + np.array(mouths[k]) - SIZE / 2))
            elif t < SINK_T + 0.9:
                u = (t - SINK_T) / 0.9
                side = 0 if k in (3, 4, 5) else 1
                kit.paste_c(img, kit.lit(sink[side * 8 + min(7, int(u * 8))], section), *c)
            for i, (te, hold, first) in enumerate(esc):
                if t < te:
                    continue
                u = min(1.0, (t - te) / 1.0)
                p = ENTRY + (hold - ENTRY) * (1 - (1 - u) ** 2)
                sp = ship_at(t)
                kb = kit.heading_index(*(sp - p), n=32) if u >= 1 else kit.heading_index(*(hold - ENTRY), n=32)
                kit.paste_c(img, kit.lit(bf[kb * 3 + int(t * 4 + i) % 2], section), *p)
                if bfg:
                    glows.append((bfg[kb], p))
            img.alpha_composite(kit.ship(2), tuple(int(v) for v in ship_at(t) - 24))
            for ts, o, v, kind in shots:
                if ts <= t and not (dead and kind == "orb" and ts > kill):
                    q = o + v * (t - ts)
                    if -10 < q[1] < 550 and -10 < q[0] < 490:
                        sp = needle[kit.heading_index(*v)] if kind == "needle" else orb[(fi // 4) % 4]
                        kit.paste_c(img, sp, *q)
            for g, p in glows:
                img = kit.add_c(img, g, *p)
            raster.draw_text(img, 8, 8, "ABYSS RAY B - T={:.1f}{}".format(t, " (KILLED)" if kill else ""), raster.LABEL)
            frames.append(img.convert("RGB"))
        return frames

    frames = visit(section=2) + visit(kill=4.2, section=3, y0=200)[: int(6.0 * FPS)]
    stills = [frames[int(t * FPS)] for t in (0.9, 2.0, 3.7)] + [frames[int((END + 4.5) * FPS)]]
    big = Image.new("RGBA", (sheet.width, sheet.height + 300), artkit.SHEET_BG)
    big.alpha_composite(sheet, (0, 0))
    raster.draw_text(big, 16, sheet.height, "AT 1/2: THE SHADOW (T=0.9), THE RISE (T=2.0), A FAN WITH THE ESCORTS "
                                            "(T=3.7), THE BREAK-UP (SECTION 3 GRADE)", raster.LABEL)
    for i, s in enumerate(stills):
        big.alpha_composite(s.convert("RGBA").resize((240, 270), Image.Resampling.BOX), (16 + i * 248, sheet.height + 18))
    artkit.save_review(big, [f.resize((360, 405), Image.Resampling.BOX) for f in frames[::2]], CONCEPT, "abyss-ray",
                       fps=FPS // 2)


def build_fx():
    """Step DITH8: only the shadow, mouth and bloom (smooth steps, no ordered dither), from the beat in assets/."""
    level0 = kit.load("abyss-ray")[0]
    kit.write("abyss-ray-shadow", [shadow_frame(level0, i) for i in range(8)], SOURCE)
    kit.write("abyss-ray-mouth", [mouth_frame(i) for i in range(4)], SOURCE)
    kit.write("abyss-ray-bloom", [bloom_frame(i) for i in range(10)], SOURCE)


def main(argv):
    if "fx" in argv:
        build_fx()
        return
    if "--review" not in argv:
        build()
    review()


if __name__ == "__main__":
    main(sys.argv[1:])
