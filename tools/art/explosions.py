#!/usr/bin/env python3
"""Production art: the fireball size ladder up to large (design/art-direction, animation rules:
explosions; the chosen ladder of explosions-r09-a).

Outputs (assets/sprites/, additive, premultiplied on black):
  explosion-tiny_0..11.png     24x24, 12 frames (Skitter)
  explosion-small_0..11.png    40x40, 12 frames (Needler, loot targets)
  explosion-medium_0..13.png   64x64, 14 frames
  explosion-large_0..13.png    96x96, 14 frames (the ship's death), with the shockwave ring
  design/art-direction/concept/explosions-final-r12-a.png/.gif
  explosion-deep-{tiny,small,medium,large}_N         the under-water kill bursts (M8 part C, step ART-C; the
                                                     sizes and frame counts of the ladder above, additive): a kill
                                                     on the play plane below the waterline (Level 23 on), no smoke
  explosion-deep-cavity-{tiny,small,medium,large}_N  the same sizes and counts, NORMAL blending with stepped
                                                     translucency, drawn under the burst's frame of the same index
  design/art-direction/concept/underwater-burst-final-r52-a.png/.gif and -b   the two proposals of round 52
  explosion-vacuum-{tiny,small,medium,large}_N         the kill bursts in vacuum (M9 part A, step ART-A; the belt's
                                                       vacuum rule: a short flash, debris thrown out in a ring, no
                                                       smoke; the ladder's sizes and frame counts, additive), drawn in
                                                       place of explosion-* in a level with medium vacuum
  explosion-vacuum-debris-{tiny,small,medium,large}_N  the same sizes and counts, NORMAL blending (1-bit alpha,
                                                       ordered-dither fade): the ring of gunmetal hull fragments with
                                                       hot edges cooling, drawn over the burst's frame of the same index
  design/art-direction/concept/vacuum-burst-final-r61-a.png/.gif and -b      the two proposals of round 61

The under-water bursts are two proposals (round 52 picks; DEEP_VARIANT is written to assets/, pending the pick):
  a "pressure flash"  a white-teal flash disc, the bright pressure ring racing out to the rung's radius with a
                      faint second ring behind it, a few teal flecks flung out; the cavity: a dark hollow opening
                      and collapsing under the flash (the depth charge's family, act4_gear.py)
  b "quenched fire"   a short orange-white fireball choked within 0.1 s into a teal-green glow, slow embers
                      fanning out and dimming, a soft turbid ring; the cavity: a grey-teal silt puff spreading
                      and settling
The vacuum bursts are two proposals too (round 61; VACUUM_VARIANT is written, pending the pick):
  a "flash and ring" (recommended, the vacuum rule): a white flash gone within 0.1 s, a thin white-blue ring
                      racing out, hot sparks streaking straight out, the debris ring
  b "flash and fireball" the same with a brief orange fireball core that burns out by a third of the burst
The under-water bursts are analytic 2D light fields supersampled 4x (no bubbles: user feedback); the game picks the rung as the dry
ladder does and draws explosion-deep-* in place of explosion-* for a kill under water.

Every frame of the round-09 fireball (tools/concept/vfx_r09.py: explosion_frames9, white flash ->
fireball -> cooling smoke, sparks and chunks) with the seeds of the chosen sheet; each rung
shares one palette.

Run: python3 tools/art/explosions.py [--review]   (~30 s; also writes the deep set, DEEP_VARIANT)
     python3 tools/art/explosions.py --deep [a|b] [--review]   (only the under-water bursts: writes that
     variant's frames to assets/ and both reviews; --review only the reviews)
     python3 tools/art/explosions.py --vacuum [a|b] [--review]   (only the vacuum bursts, the same way)
"""
import sys
from concurrent.futures import ProcessPoolExecutor

import numpy as np
from PIL import Image

import artkit
from artkit import DESIGN, sprite

import vfx_r09 as v9  # noqa: E402  (concept script, imported unchanged)

SCRIPT = "explosions.py"
SOURCE = artkit.source_note(SCRIPT)
# rung -> (size px, frames, seed of the chosen sheet, colours)
LADDER = {"tiny": (24, 12, 10, 32), "small": (40, 12, 11, 40), "medium": (64, 14, 12, 48),
          "large": (96, 14, 13, 48)}


def render(rung):
    size, frames, seed, colours = LADDER[rung]
    raw = v9.explosion_frames9(size, frames, seed)
    return artkit.quantize_set([artkit.additive(f) for f in raw], colours)


def build():
    with ProcessPoolExecutor() as pool:
        for rung, frames in zip(LADDER, pool.map(render, LADDER)):
            artkit.write_frames(f"explosion-{rung}", frames, SOURCE)


# =========================================================================== under water (M8 part C)

DEEP_VARIANT = "a"                     # pending round 52's pick
DEEP_SOURCE = artkit.source_note(SCRIPT, "M8 part C batch")
SS = 4
WHITE_TEAL = np.array((226, 255, 248), float)
TEAL = np.array((40, 230, 200), float)
DEEP_TEAL = np.array((0, 110, 130), float)
ORANGE = np.array((255, 170, 70), float)
EMBER = np.array((255, 120, 40), float)
CAVITY = np.array((6, 22, 30), float)
SILT = np.array((92, 120, 118), float)


def _grid(size):
    n = size * SS
    yy, xx = np.mgrid[0:n, 0:n].astype(np.float64)
    c = n / 2
    return (xx + 0.5 - c) / SS, (yy + 0.5 - c) / SS


def _down(arr, size):
    return arr.reshape(size, SS, size, SS, -1).mean(axis=(1, 3))


def _flecks(seed, n):
    rng = np.random.default_rng(seed)
    return [(rng.uniform(0, 2 * np.pi), rng.uniform(0.55, 1.0), rng.uniform(0.6, 1.2)) for _ in range(n)]


def deep_frame(variant, rung, i):
    """One frame of the under-water burst: (additive light, cavity) at the rung's size."""
    size, frames, seed, _ = LADDER[rung]
    R = size / 2 - 2
    u = i / (frames - 1)
    x, y = _grid(size)
    r = np.hypot(x, y)
    ang = np.arctan2(y, x)
    wob = 1 + 0.06 * np.sin(ang * 5 + seed) + 0.04 * np.sin(ang * 9 - seed * 2)
    light = np.zeros(r.shape + (3,))
    cav_a = np.zeros(r.shape)
    cav_c = CAVITY
    if variant == "a":
        flash = np.exp(-u * 9) * np.exp(-(r / (R * (0.28 + 0.25 * u))) ** 2)
        rr = R * (0.2 + 0.8 * (1 - (1 - min(1, u / 0.75)) ** 2)) * wob
        width = R * (0.07 + 0.08 * u)
        ring = np.exp(-((r - rr) / width) ** 2) * (1 - u) ** 1.3
        rr2 = R * 0.62 * (1 - (1 - min(1, max(0, u - 0.15) / 0.7)) ** 2) * wob
        ring2 = np.exp(-((r - rr2) / (width * 1.4)) ** 2) * 0.35 * (1 - u) ** 1.6 * (u > 0.15)
        light += WHITE_TEAL * np.clip(flash * 1.6, 0, 1)[..., None]
        light += (TEAL * (1 - u) + DEEP_TEAL * u) * np.clip(ring + ring2, 0, 1)[..., None]
        glow = np.exp(-(r / (R * 0.6)) ** 2) * 0.35 * np.exp(-u * 4)
        light += TEAL * glow[..., None]
        for a, reach, sp in _flecks(seed + 7, 6 if size > 30 else 4):
            fr = R * reach * (1 - (1 - u) ** 2) * sp
            fx, fy = np.cos(a) * fr, np.sin(a) * fr
            d = np.hypot(x - fx, y - fy)
            light += WHITE_TEAL * (np.exp(-(d / 0.9) ** 2) * (1 - u) * 0.9)[..., None]
        rc = R * 0.42 * np.sin(np.pi * min(1.0, u / 0.7)) * wob
        cav_a = np.clip((rc - r) / 1.5 + 0.5, 0, 1) * 0.75 * (1 - u) ** 0.5 * (u > 0.05)
    else:
        q = min(1.0, u / 0.25)                              # the fire choking
        core = np.exp(-(r / (R * (0.45 - 0.1 * q))) ** 2) * np.exp(-u * 4.5)
        hot = np.exp(-(r / (R * 0.14)) ** 2) * (1 - q)
        fire = ORANGE * (1 - q) + TEAL * q
        light += fire[None, None] * np.clip(core * 1.5, 0, 1)[..., None] + WHITE_TEAL * hot[..., None]
        rr = R * (0.3 + 0.6 * (1 - (1 - u) ** 2)) * wob
        ring = np.exp(-((r - rr) / (R * 0.16)) ** 2) * 0.45 * (1 - u) ** 1.5 * q
        light += DEEP_TEAL * ring[..., None]
        for a, reach, sp in _flecks(seed + 11, 9 if size > 30 else 5):
            fr = R * reach * 0.8 * (1 - (1 - u) ** 1.6) * sp
            fx, fy = np.cos(a) * fr, np.sin(a) * fr
            d = np.hypot(x - fx, y - fy)
            k = np.exp(-(d / 1.0) ** 2) * (1 - u) ** 1.2
            m = min(1.0, u / 0.6)
            col = EMBER * (1 - m) + TEAL * 0.6 * m
            light += col * k[..., None]
        rc = R * (0.35 + 0.5 * u) * wob
        puff = np.clip((rc - r) / (R * 0.25), 0, 1) * (0.55 * np.sin(np.pi * min(1.0, u / 0.35 + 0.0)) if u < 0.35
                                                         else 0.55 * (1 - (u - 0.35) / 0.65) ** 1.2)
        cav_a = puff * (u > 0.05)
        cav_c = SILT
    lit = np.clip(_down(light, size), 0, 255)
    alpha = np.where(lit.max(axis=-1) >= 1, 255, 0)
    add = Image.fromarray(np.dstack([lit, alpha]).astype(np.uint8), "RGBA")
    ca = _down(cav_a[..., None], size)[..., 0]
    ca = artkit.smooth_steps(ca)
    cav = np.dstack([np.broadcast_to(cav_c, ca.shape + (3,)), ca * 255]).astype(np.uint8)
    cav[ca == 0] = 0
    return add, Image.fromarray(cav, "RGBA")


def render_deep(job):
    variant, rung = job
    frames = LADDER[rung][1]
    out = [deep_frame(variant, rung, i) for i in range(frames)]
    adds = artkit.quantize_set([a for a, _ in out], LADDER[rung][3])
    return adds, [c for _, c in out]


def deep_sets(variant):
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(render_deep, [(variant, r) for r in LADDER]))
    return dict(zip(LADDER, res))


def build_deep(variant):
    for rung, (adds, cavs) in deep_sets(variant).items():
        artkit.write_frames(f"explosion-deep-{rung}", adds, DEEP_SOURCE)
        artkit.write_frames(f"explosion-deep-cavity-{rung}", cavs, DEEP_SOURCE)
    print(f"explosion-deep-*: variant {variant} -> assets/sprites")


def deep_review():
    """Both proposals' sheets and GIFs (rendered in memory; the review shows them over a graded farm plate)."""
    import l23kit as kit
    artkit.REVIEW_ROUND = "r52"
    plate = kit.plate(260, 120, 40, 200)
    names = {"a": "A PRESSURE FLASH (CAVITY UNDER)", "b": "B QUENCHED FIRE (SILT PUFF UNDER)"}
    for variant in "ab":
        sets = deep_sets(variant)
        rows = []
        for rung, (adds, cavs) in sets.items():
            z = 2 if adds[0].width <= 40 else 1
            rows.append((f"{rung.upper()} LIGHT (ADDITIVE)", adds, z, True))
            rows.append((f"{rung.upper()} CAVITY (NORMAL, UNDER)", cavs, z, False))
        sheet = artkit.review_sheet(f"UNDER-WATER BURST {names[variant]}", rows, width=1000, batch="M8 part C batch")
        gif = []
        longest = max(len(a) for a, _ in sets.values())
        for i in range(longest + 6):
            img = plate.copy()
            x = 14
            for adds, cavs in sets.values():
                w = adds[0].width
                cy = 60
                if i < len(adds):
                    img.alpha_composite(cavs[i], (x, cy - w // 2))
                    img = kit.add_c(img, adds[i], x + w / 2, cy)
                x += w + 14
            gif.append(sprite.enlarge(img, 3).convert("RGB"))
        artkit.save_review(sheet, gif, DESIGN / "art-direction" / "concept", "underwater-burst", fps=15,
                           variant=variant)


def review():
    sets = {rung: artkit.load_frames(f"explosion-{rung}") for rung in LADDER}
    rows = [(f"{rung.upper()}", frames, 2 if frames[0].width <= 40 else 1, True) for rung, frames in sets.items()]
    sheet = artkit.review_sheet("EXPLOSION LADDER - FINAL FRAMES", rows, width=1400)
    gif = []
    longest = max(len(f) for f in sets.values())
    for i in range(longest + 4):
        img = Image.new("RGBA", (24 + 40 + 64 + 96 + 50, 106), artkit.PLATE)
        x = 10
        for frames in sets.values():
            if i < len(frames):
                img = artkit.add_light(img, frames[i], (x, 53 - frames[0].height // 2))
            x += frames[0].width + 10
        gif.append(sprite.enlarge(img, 3))
    artkit.save_review(sheet, gif, DESIGN / "art-direction" / "concept", "explosions", fps=15)


# =========================================================================== vacuum (M9 part A)

VACUUM_VARIANT = "a"                   # pending round 61's pick
VACUUM_SOURCE = artkit.source_note(SCRIPT, "M9 part A batch")
FLASH_BLUE = np.array((220, 236, 255), float)
RING_BLUE = np.array((150, 190, 255), float)
SPARK_HOT = np.array((255, 236, 200), float)
FIRE = np.array((255, 150, 60), float)
METAL = np.array((70, 74, 86), float)
METAL_LIT = np.array((150, 156, 172), float)
HOT_EDGE = np.array((255, 140, 60), float)
DEBRIS = {"tiny": 6, "small": 9, "medium": 12, "large": 16}


def _shard(rng):
    """A small irregular hull fragment: polygon corners (radius, angle)."""
    n = int(rng.integers(3, 6))
    return [(rng.uniform(0.6, 1.0), a) for a in np.sort(rng.uniform(0, 2 * np.pi, n))]


def vacuum_frame(variant, rung, i):
    """One frame of the vacuum burst: (additive light, debris) at the rung's size."""
    size, frames, seed, _ = LADDER[rung]
    R = size / 2 - 2
    u = i / (frames - 1)
    x, y = _grid(size)
    r = np.hypot(x, y)
    light = np.zeros(r.shape + (3,))
    flash = np.exp(-u * 16) * np.exp(-(r / (R * (0.30 + 0.2 * u))) ** 2)
    light += FLASH_BLUE * np.clip(flash * 1.8, 0, 1)[..., None]
    rr = R * (0.15 + 0.85 * (1 - (1 - min(1.0, u / 0.6)) ** 2))
    ring = np.exp(-((r - rr) / (R * 0.03 + 0.35)) ** 2) * (1 - u) ** 3.5
    light += RING_BLUE * np.clip(ring, 0, 1)[..., None] * 0.9
    if variant == "b":
        fire = np.exp(-(r / (R * (0.32 + 0.15 * u))) ** 2) * max(0.0, 1 - u / 0.35) ** 1.4
        light += FIRE * np.clip(fire * 1.4, 0, 1)[..., None]
        light += SPARK_HOT * np.clip(fire * 0.6 * (u < 0.15), 0, 1)[..., None]
    for a, reach, sp in _flecks(seed + 23, 8 if size > 30 else 5):
        far = R * reach * (1 - (1 - min(1.0, u * 1.3)) ** 2) * sp
        near = max(0.0, far - R * 0.25)
        dx, dy = np.cos(a), np.sin(a)
        h = np.clip(x * dx + y * dy, near, far)
        d = np.hypot(x - dx * h, y - dy * h)
        light += SPARK_HOT * (np.exp(-(d / 0.5) ** 2) * (1 - u) ** 1.5 * 0.9)[..., None]
    lit = np.clip(_down(light, size), 0, 255)
    alpha = np.where(lit.max(axis=-1) >= 1, 255, 0)
    add = Image.fromarray(np.dstack([lit, alpha]).astype(np.uint8), "RGBA")
    # the debris ring: fragments flung straight out, tumbling, their hot edges cooling, gone at the end
    rng = np.random.default_rng(seed * 31 + 7)
    n = DEBRIS[rung]
    cover = np.zeros(r.shape)
    col = np.zeros(r.shape + (3,))
    for j in range(n):
        a = 2 * np.pi * j / n + rng.uniform(-0.25, 0.25)
        speed = rng.uniform(0.75, 1.05)
        dist = R * 0.95 * speed * (1 - (1 - u) ** 1.7)
        cx, cy = np.cos(a) * dist, np.sin(a) * dist
        scale = max(1.3, size / 18) * rng.uniform(1.0, 1.6)
        spin = rng.uniform(-1, 1) * 6 * u + rng.uniform(0, 2 * np.pi)
        corners = [(cx + rad * scale * np.cos(t + spin), cy + rad * scale * np.sin(t + spin))
                   for rad, t in _shard(rng)]
        inside = np.ones(r.shape, bool)
        for (x0, y0), (x1, y1) in zip(corners, corners[1:] + corners[:1]):
            inside &= (x1 - x0) * (y - y0) - (y1 - y0) * (x - x0) >= 0
        lit_side = np.clip(0.5 - 0.5 * ((x - cx) + (y - cy)) / scale, 0, 1)       # top-left lit
        heat = max(0.0, 1 - u / 0.55)
        shade = METAL * (1 - lit_side[..., None]) + METAL_LIT * lit_side[..., None]
        shade = shade * (1 - heat * 0.6) + HOT_EDGE * heat * 0.6
        col = np.where(inside[..., None], shade, col)
        cover = np.maximum(cover, inside.astype(float))
    if i == 0:
        cover[:] = 0
    da = _down(cover[..., None], size)[..., 0]
    fade = 1.0 if u < 0.6 else max(0.0, 1 - (u - 0.6) / 0.4)
    da = artkit.ordered_dither(np.clip((da > 0.45) * fade, 0, 1), 4)
    dc = _down(col, size)
    deb = np.dstack([np.clip(dc / np.maximum(_down(cover[..., None], size), 1e-6), 0, 255), da * 255])
    deb[da == 0] = 0
    return add, Image.fromarray(deb.astype(np.uint8), "RGBA")


def render_vacuum(job):
    variant, rung = job
    frames = LADDER[rung][1]
    out = [vacuum_frame(variant, rung, i) for i in range(frames)]
    adds = artkit.quantize_set([a for a, _ in out], LADDER[rung][3])
    debris = artkit.quantize_set([d for _, d in out], 16)
    return adds, debris


def vacuum_sets(variant):
    with ProcessPoolExecutor() as pool:
        res = list(pool.map(render_vacuum, [(variant, r) for r in LADDER]))
    return dict(zip(LADDER, res))


def build_vacuum(variant):
    for rung, (adds, debris) in vacuum_sets(variant).items():
        artkit.write_frames(f"explosion-vacuum-{rung}", adds, VACUUM_SOURCE)
        artkit.write_frames(f"explosion-vacuum-debris-{rung}", debris, VACUUM_SOURCE)
    print(f"explosion-vacuum-*: variant {variant} -> assets/sprites")


def vacuum_review():
    """Both proposals' sheets and GIFs, over belt space (round 60's lighter grade)."""
    import enemies_r60 as e60
    artkit.REVIEW_ROUND = "r61"
    plate = e60.space_bg(13, 300).crop((0, 90, 300, 210)).convert("RGBA")
    names = {"a": "A FLASH AND DEBRIS RING (NO SMOKE)", "b": "B FLASH, BRIEF FIREBALL, DEBRIS RING"}
    for variant in "ab":
        sets = vacuum_sets(variant)
        rows = []
        for rung, (adds, debris) in sets.items():
            z = 2 if adds[0].width <= 40 else 1
            rows.append((f"{rung.upper()} LIGHT (ADDITIVE)", adds, z, True))
            rows.append((f"{rung.upper()} DEBRIS (NORMAL, OVER)", debris, z, False))
        sheet = artkit.review_sheet(f"VACUUM BURST {names[variant]} (ROUND 61)", rows, width=1000,
                                    batch="M9 part A batch")
        gif = []
        longest = max(len(a) for a, _ in sets.values())
        for i in range(longest + 6):
            img = plate.copy()
            x = 14
            for adds, debris in sets.values():
                w = adds[0].width
                cy = 60
                if i < len(adds):
                    img = artkit.add_light(img, adds[i], (x, cy - w // 2))
                    img.alpha_composite(debris[i], (x, cy - w // 2))
                x += w + 14
            gif.append(sprite.enlarge(img, 3).convert("RGB"))
        artkit.save_review(sheet, gif, DESIGN / "art-direction" / "concept", "vacuum-burst", fps=15,
                           variant=variant)


if __name__ == "__main__":
    args = sys.argv[1:]
    if "--vacuum" in args:
        v = next((a for a in args if a in ("a", "b")), VACUUM_VARIANT)
        if "--review" not in args:
            build_vacuum(v)
        vacuum_review()
        sys.exit()
    if "--deep" in args:
        v = next((a for a in args if a in ("a", "b")), DEEP_VARIANT)
        if "--review" not in args:
            build_deep(v)
        deep_review()
    else:
        if "--review" not in args:
            build()
            build_deep(DEEP_VARIANT)
            build_vacuum(VACUUM_VARIANT)
        review()
