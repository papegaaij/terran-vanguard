#!/usr/bin/env python3
"""Production art: the Ion Beam's effects (design/player/weapons/ion-beam; M7 part A batch, concept
round 37), the look round 36 chose (G.1 = a): the round-08/09 beam family (tools/concept/vfx_r09.py
``beam_segment`` and ``contact_frames``, imported unchanged) drawn per level, with the first-target
contact flare, the rock-scorch impact and the glance spark over hardened ground of
tools/concept/gear_r36.py (variant a).

Outputs (assets/sprites/, all additive, premultiplied on black, unless noted):
  ion-beam-column-<n>_0..7   the column's body per level n = 1..5 and 6 = overdrive (6, 8, 10, 12,
                             14, 18 px wide; the sprite is 6 x half-width + 8 px wide for the halo) x
                             64 px: a tile that repeats seamlessly upwards, drawn from the nose up to the
                             contact point (cut there) or off the top edge; 8 frames, one per game step
                             (the ripples run up the column at 240 px/s)
  ion-beam-tip-<n>_0..7      the contact end per level, 8 px tall: the last 6 px narrow and whiten so the
                             beam reads as boring in, not cut off; drawn with its top row at the contact
                             point (the beam bores 4 px into the target), the column below it
  ion-beam-contact_0..5      64x52 contact flare on a target 50 px or larger (round 09's flare at scale
                             0.8), the hit point at (32, 22); 6-frame loop, 2 steps a frame
  ion-beam-contact-small_0..5  the same at scale 0.55, for targets under 50 px and tendril links
  ion-beam-rock_0..5         64x52 rock-scorch flare (the large flare tinted with dust, the white core
                             kept, a blue-white light on the rock), hit point at (32, 22)
  ion-beam-dust_0..9         80x52, normal blending: dust thrown back and sideways from a wall hit,
                             stepped translucency, the hit point at (40, 10); 10-frame loop, 4 steps
  ion-beam-scorch            16x12, normal blending: the scorch mark left on rock (a translucent dark
                             smudge, centre (8, 7)); the game lays one every 2 steps at the contact
                             point, scrolling with the ground and fading over 0.8 s
  ion-beam-scorch-glow       16x12: the fresh mark's faint cyan glow over it, faded the same way
  ion-beam-glance_0..3       28x20 glance spark where the beam passes over a hardened ground target,
                             centre (14, 10); 3 steps a frame
  design/player/weapons/ion-beam/concept/ion-beam-final-r37-a.png/.gif   review sheet and loop

The steady heat tint on the target is the game's (a HitFlash variant), not an asset. The family's
periods are snapped so the tile loops (64 px tall, 8 frames): the width wobble 4 cycles and the
ripple bands 2 per tile instead of round 09's 0.35 and 0.22 rad/px; the look is the chosen one.

Run: python3 tools/art/ion_beam.py [--review]   (~15 s)
"""
import sys

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

import act3kit
import artkit
from artkit import DESIGN, TAU, sprite

import vfx_r08 as v8  # noqa: E402  (concept scripts, imported unchanged)
import vfx_r09 as v9  # noqa: E402

SCRIPT = "ion_beam.py"
SOURCE = artkit.source_note(SCRIPT, act3kit.BATCH)
ROOT_NAME = "ion-beam"
CONCEPT = DESIGN / "player" / "weapons" / "ion-beam" / "concept"
PS, WHITE = v8.PS, v8.WHITE
Canvas, gauss, solid = v8.Canvas, v8.gauss, v8.solid

WIDTHS = [6, 8, 10, 12, 14, 18]          # px per level 1..5 and the overdrive (data: width)
TILE_H = 64
FRAMES = 8
TIP_H = 8
TAPER = 6
DUST = (196, 160, 128)
ARC = (200, 235, 255)
CYAN = (120, 200, 255)
STEP = 60


def column(hw, f, tip=False):
    """Round 09's beam_segment with the periods snapped to the tile: frame f of 8."""
    h = TIP_H if tip else TILE_H
    cv = Canvas(int(hw * 6 + 8), h)
    cx = cv.w / 2
    dx = np.abs(cv.x - cx)
    y = cv.y
    ph = TAU * f / FRAMES
    wob = 1 + 0.15 * np.sin(-y * TAU * 4 / TILE_H + ph) if not tip else np.ones_like(y)
    tp = np.clip(y / TAPER, 0, 1) if tip else np.ones_like(y)
    k = 0.45 + 0.55 * tp
    cv.add(PS[1], gauss(dx, hw * 1.6 * wob * k) * 0.7)
    cv.over(PS[2], solid(dx, hw * 0.6 * wob * k) * 0.9)
    rip = (np.sin(y * TAU * 2 / TILE_H + ph) > 0.6) * solid(dx, hw * 0.75 * k)
    if not tip:
        cv.add(PS[3], rip * 0.5)
    pulse = 1 + 0.12 * np.sin(ph * 2) if tip else 1
    cv.over(WHITE, solid(dx, hw * 0.25 * wob * (0.6 + 0.8 * (1 - tp)) + 0.01))
    if tip:
        cv.add(WHITE, gauss(dx, hw * 0.9) * (1 - tp) * 1.2 * pulse)
    return artkit.additive(cv.image())


def dusty(fr, k=0.45):
    """The flare tinted with dust for the rock-scorch impact (the white core stays)."""
    a = np.array(fr).astype(np.float64)
    lum = a[..., :3].mean(axis=-1, keepdims=True) / 255
    keep = np.clip((lum - 0.75) / 0.25, 0, 1)
    a[..., :3] = a[..., :3] * (1 - k * (1 - keep)) + np.array(DUST) * k * (1 - keep)
    return Image.fromarray(np.clip(a, 0, 255).astype(np.uint8), "RGBA")


def rock_flares():
    out = []
    for fr in v9.contact_frames(scale=0.8):
        img = dusty(fr)
        cv = Canvas(64, 52)
        cv.add((190, 220, 255), gauss(cv.dist(32, 23), 7) * 0.5)    # the light on the rock
        lay = cv.image()
        img.alpha_composite(lay)
        out.append(artkit.additive(img))
    return out


def dust_frames(n=10):
    """Dust thrown back and sideways from the contact (gear_r36 rock_kick a), as its own frames."""
    out = []
    w, h, x, y = 80, 52, 40, 10
    for i in range(n):
        lay = Image.new("RGBA", (w * 4, h * 4), (0, 0, 0, 0))
        d = ImageDraw.Draw(lay)
        for k in range(10):
            ph = (i / n + k * 0.1) % 1.0
            ang = np.radians(20 + 140 * ((k * 0.37) % 1))
            r = 6 + ph * 26
            cx, cy = x + np.cos(ang) * r * 1.2, y + np.sin(ang) * r * 0.7 + ph * 10
            a = int(150 * (1 - ph)) // 2
            rr = 2 + ph * 4
            d.ellipse([(cx - rr) * 4, (cy - rr) * 4, (cx + rr) * 4, (cy + rr) * 4], fill=DUST + (a,))
        lay = lay.filter(ImageFilter.GaussianBlur(3.2)).resize((w, h), Image.BOX)
        out.append(artkit.stepped_alpha(lay, 4))
    return out


def scorch_mark():
    """The scorch decal (normal blending) and its fresh cyan glow (additive)."""
    w, h, x, y = 16, 12, 8, 7
    yy, xx = np.mgrid[0:h * 4, 0:w * 4] / 4 + 0.125
    g = np.exp(-(((xx - x) / 4.5) ** 2 + ((yy - y) / 3.0) ** 2))
    g = g.reshape(h, 4, w, 4).mean(axis=(1, 3))
    dark = np.zeros((h, w, 4))
    dark[..., :3] = (22, 14, 10)
    dark[..., 3] = np.floor(0.45 * g * 4 + 0.5) / 4 * 255
    glow = np.zeros((h, w, 4))
    glow[..., :3] = np.array((90, 170, 230)) * 0.5 * g[..., None] ** 2 / 0.5
    glow[..., 3] = 255
    mark = Image.fromarray(dark.astype(np.uint8), "RGBA")
    shine = Image.fromarray(np.clip(glow, 0, 255).astype(np.uint8), "RGBA")
    return mark, artkit.additive(shine)


def glance_frames(n=4):
    """The glance spark over hardened ground (gear_r36 ``glance``): a faint cyan light and four
    short sparks, squashed to the ground plane."""
    out = []
    w, h = 28, 20
    for i in range(n):
        rng = np.random.default_rng(7 * i + 3)
        cv = Canvas(w, h)
        cv.add(CYAN, gauss(cv.dist(14, 10), 4.5) * 0.45)
        for _ in range(4):
            a = rng.uniform(0, TAU)
            r0, r1 = rng.uniform(4, 6), rng.uniform(8, 13)
            seg = cv.seg(14 + np.cos(a) * r0, 10 + np.sin(a) * r0 * 0.6,
                         14 + np.cos(a) * r1, 10 + np.sin(a) * r1 * 0.6)
            cv.add(ARC, gauss(seg, 0.5) * 0.85)
        cv.add(WHITE, gauss(cv.dist(14, 10), 1.2) * 0.6)
        out.append(artkit.additive(cv.image()))
    return out


def build():
    with act3kit.sprites_of(ROOT_NAME):
        for n, w in enumerate(WIDTHS, 1):
            hw = w / 2
            body = [column(hw, f) for f in range(FRAMES)]
            tip = [column(hw, f, tip=True) for f in range(FRAMES)]
            frames = act3kit.quantize_bright(body + tip, 24)
            artkit.write_frames(f"ion-beam-column-{n}", frames[:FRAMES], SOURCE)
            artkit.write_frames(f"ion-beam-tip-{n}", frames[FRAMES:], SOURCE)
        big = [artkit.additive(f) for f in v9.contact_frames(scale=0.8)]
        small = [artkit.additive(f) for f in v9.contact_frames(scale=0.55)]
        artkit.write_frames("ion-beam-contact", act3kit.quantize_bright(big, 32), SOURCE)
        artkit.write_frames("ion-beam-contact-small", act3kit.quantize_bright(small, 32), SOURCE)
        artkit.write_frames("ion-beam-rock", act3kit.quantize_bright(rock_flares(), 32), SOURCE)
        artkit.write_frames("ion-beam-dust", artkit.quantize_set(dust_frames(), 12), SOURCE)
        mark, shine = scorch_mark()
        artkit.write_frames("ion-beam-scorch", [mark], SOURCE, single=True)
        artkit.write_frames("ion-beam-scorch-glow", [shine], SOURCE, single=True)
        artkit.write_frames("ion-beam-glance", act3kit.quantize_bright(glance_frames(), 16), SOURCE)
    print(f"ion beam: {act3kit.where(ROOT_NAME)}")


# =========================================================================== review

def draw_beam(img, x, nose, end, n, f, tipped):
    """The beam as the game draws it: the body tile repeated from the nose up to ``end`` (the
    contact point, or off the top), the tip at the contact."""
    body = artkit.load_frames(f"ion-beam-column-{n}")[f % FRAMES]
    tip = artkit.load_frames(f"ion-beam-tip-{n}")[f % FRAMES]
    top = end + (TIP_H if tipped else 0)
    y = nose
    layer = Image.new("RGBA", img.size, (0, 0, 0, 0))
    while y > top:
        piece_h = min(TILE_H, y - top)
        piece = body.crop((0, TILE_H - piece_h, body.width, TILE_H))
        layer.paste(piece, (int(x - body.width / 2), int(y - piece_h)))
        y -= piece_h
    if tipped:
        layer.paste(tip, (int(x - tip.width / 2), int(end)))
    return artkit.add_light(img, layer)


def review():
    import gear_r36 as g  # noqa: E402  (concept script, imported unchanged: the scene's ground and sprites)
    with act3kit.sprites_of(ROOT_NAME):
        cols = [artkit.load_frames(f"ion-beam-column-{n}") for n in range(1, 7)]
        tips = [artkit.load_frames(f"ion-beam-tip-{n}") for n in range(1, 7)]
        contact = artkit.load_frames("ion-beam-contact")
        small = artkit.load_frames("ion-beam-contact-small")
        rock = artkit.load_frames("ion-beam-rock")
        dust = artkit.load_frames("ion-beam-dust")
        mark = artkit.load_frames("ion-beam-scorch")[0]
        shine = artkit.load_frames("ion-beam-scorch-glow")[0]
        glance = artkit.load_frames("ion-beam-glance")
        # the scene: Martian ground, the ship at L2 sweeping from a Needler to a rock island and
        # over a hardened Hive Node, then L5 and the overdrive into open sky
        ground = g.r36.mars_ground(8, 540).crop((0, 0, 480, 540))
        ground, mask = g.rock_island(ground, 340, 170, seed=5)
        needler = g.asset("needler_0")
        hive = g.asset("hive-node_0")
        fps = 30
        frames = []
        marks = []
        n_frames = int(fps * 6)
        for i in range(n_frames):
            t = i / fps
            step = i * 2
            img = ground.copy()
            sprite.paste_center(img, hive, 150, 330)
            level = 2 if t < 4 else (5 if t < 5 else 6)
            n = level
            hw = WIDTHS[n - 1] / 2
            keys = [(0, 210), (1.4, 210), (2.0, 330), (3.2, 330), (3.6, 150), (6, 150)]
            for (t0, x0), (t1, x1) in zip(keys[:-1], keys[1:]):
                if t0 <= t <= t1:
                    px = x0 + (x1 - x0) * g.smooth((t - t0) / (t1 - t0))
            py = 470
            nose = py - 24
            alive = t < 1.2
            for wx, wy, t0 in marks:
                age = (t - t0) / 0.8
                if age < 1:
                    faded = mark.copy()
                    faded.putalpha(mark.getchannel("A").point(lambda v, k=1 - age: int(v * k)))
                    img.alpha_composite(faded, (int(wx - 8), int(wy - 7)))
                    glow = shine.copy()
                    glow.putalpha(shine.getchannel("A").point(lambda v, k=1 - age: int(v * k)))
                    img = artkit.add_light(img, glow, (int(wx - 8), int(wy - 7)))
            if alive:
                sprite.paste_center(img, needler, 210, 120)
            elif t < 1.8:
                g.explosion(img, 210, 120, (t - 1.2) / 0.6)
            # first target along the column
            end, kind = -4, None
            if alive and abs(px - 210) < 12:
                end, kind = 120 + 11 - 4, "air"
            ws = g.wall_stop(mask, 0, px, hw, nose)
            if ws is not None and ws > end:
                end, kind = ws, "wall"
            img = draw_beam(img, px, nose, end, n, step, kind is not None)
            if abs(px - 150) < 26 and (kind is None or end < 330):
                for gy in (322, 336):
                    gl = glance[(step // 3) % 4]
                    img = artkit.add_light(img, gl, (int(px - 14), int(gy - 10)))
            if kind == "air":
                img = artkit.add_light(img, small[(step // 2) % 6], (int(px - 32), int(end - 22)))
            elif kind == "wall":
                img = artkit.add_light(img, rock[(step // 2) % 6], (int(px - 32), int(end - 22)))
                img.alpha_composite(dust[(step // 4) % 10], (int(px - 40), int(end - 10)))
                if step % 2 == 0:
                    marks.append((px, end - 2, t))
            g.draw_ship(img, px, py, t, pods="micro-missile")
            g.label(img, 8, 540 - 14, f"ION BEAM {'OD' if n == 6 else 'L' + str(n)} ({WIDTHS[n - 1]} PX)",
                    (150, 200, 240))
            frames.append(img)
        rows = [(f"ION-BEAM-COLUMN-{n} ({'OVERDRIVE' if n == 6 else 'L' + str(n)}, {WIDTHS[n - 1]} PX): "
                 "64 PX TILE, 8 FRAMES (1 STEP), ADDITIVE", cols[n - 1], 3, True) for n in range(1, 7)]
        rows += [(f"ION-BEAM-TIP-1..6 (FRAME 0 OF EACH LEVEL): THE CONTACT END, 8 PX", [t[0] for t in tips], 6, True),
                 ("ION-BEAM-CONTACT (TARGETS 50 PX AND UP), HIT POINT (32, 22), 2 STEPS", contact, 3, True),
                 ("ION-BEAM-CONTACT-SMALL (SMALL TARGETS, TENDRIL LINKS)", small, 3, True),
                 ("ION-BEAM-ROCK: ROCK-SCORCH FLARE, DUST-TINTED", rock, 3, True),
                 ("ION-BEAM-DUST: DUST THROWN BACK FROM A WALL HIT (NORMAL BLEND), 4 STEPS", dust, 3, False),
                 ("ION-BEAM-SCORCH (NORMAL BLEND) AND -GLOW (ADDITIVE), FADE 0.8 S", [mark, shine], 6, False),
                 ("ION-BEAM-GLANCE: OVER HARDENED GROUND, 3 STEPS", glance, 4, True),
                 ("IN THE SCENE (1X): NEEDLER, ROCK ISLAND, HARDENED HIVE NODE, L5, OVERDRIVE",
                  [frames[k].crop((60, 0, 420, 540)).resize((180, 270), Image.NEAREST) for k in (15, 75, 105, 140, 170)],
                  1, False)]
        sheet = artkit.review_sheet("ION BEAM - FINAL EFFECTS (ROUND 36 G.1 = A)", rows, width=1500, batch=act3kit.BATCH)
        artkit.REVIEW_ROUND = act3kit.ROUND
        artkit.save_review(sheet, [f.resize((360, 405), Image.NEAREST) for f in frames], CONCEPT, "ion-beam", fps=fps)


if __name__ == "__main__":
    artkit.REVIEW_ROUND = act3kit.ROUND
    if "--review" not in sys.argv[1:]:
        build()
    review()
